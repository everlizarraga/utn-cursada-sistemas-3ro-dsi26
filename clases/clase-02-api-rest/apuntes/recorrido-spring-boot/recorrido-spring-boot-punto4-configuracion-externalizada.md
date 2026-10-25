# Recorrido Spring Boot — Punto 4
## Configuración externalizada

---

## 📄 Sobre este documento

**Qué cubre:** cómo un valor que vive en un archivo de texto (`application.yml`) termina adentro de un objeto Java tipado, sin que vos lo copies. `@ConfigurationProperties`, `@EnableConfigurationProperties`, y el "relaxed binding" que explica por qué `base-url` se convierte en `baseUrl`.

**Qué NO cubre:** perfiles de entorno (dev/prod), variables de sistema, `@Value`. Madrigueras al final.

**Cuánto es:** el punto más corto del recorrido. Un concepto y tres anotaciones.

---

## 🎒 De dónde venís

- **Puntos 1-3:** Spring escanea, instancia e inyecta. Sabés qué es un bean y cómo llega a donde se lo necesita.
- **De DdS:** usaste `.env` en Node. Ese es exactamente el mismo concepto, y lo vamos a usar como ancla.
- **La pieza que falta:** en el contenedor hay un bean llamado `restCountriesProperties` que aparece **con la URL de la API ya adentro**, y esa URL no está escrita en ninguna clase Java.

---

## 1. El problema: la URL hardcodeada 🔴

Escobar, en clase, lo dijo mientras lo hacía: *"todo esto yo no lo voy a tener escrito y hardcodeado en el código, sino que lo voy a tener en un archivo de configuración por si en algún momento llega a cambiar"*.

Suena a una excusa menor. No lo es. Mirá el camino fácil:

```java
// ❌ La URL hardcodeada, adentro de la clase
@Component
public class BuscadorDePaises {

    private static final String BASE_URL = "https://restcountries.com/v3.1";  // 👈

    public List<Pais> buscarTodos() {
        // ... usa BASE_URL
    }
}
```

Funciona. Y arrastra cuatro problemas:

1. **Cambiar la URL exige recompilar.** No es un cambio de configuración: es un cambio de código, con su commit, su build y su deploy.
2. **No podés tener valores distintos por entorno.** En desarrollo querés pegarle a un servidor de prueba; en producción, al real. Con la URL adentro de la clase, es la misma o nada.
3. **Se disemina.** Hoy la usa una clase. Mañana, tres. Y la vas a tener escrita en tres lugares — hasta que un día actualices dos y te olvides del tercero.
4. **Secretos en el repo.** Hoy es una URL pública y no pasa nada. Pero mañana es una API key o la contraseña de la base de datos, y con este patrón **la estás commiteando a GitHub**.

**Esto vos ya lo sabías.** En Node no ponés la URL en el código: la ponés en un `.env` y hacés `process.env.API_URL`. Es exactamente el mismo problema, resuelto con exactamente la misma idea:

> 🎯 **Configuración externalizada**
>
> **Qué es:** todo valor que puede cambiar entre entornos o en el tiempo (URLs, puertos, credenciales, timeouts) vive **fuera del código compilado**, en un archivo de configuración.
>
> **Por qué lo usamos:** el mismo binario (el mismo `.jar`) tiene que poder correr en desarrollo, en testing y en producción sin recompilar. Lo único que cambia es el archivo de configuración que lo acompaña.
>
> **Dónde lo ves en ESTE código:** en `application.yml`, y en la clase `RestCountriesProperties` que lo lee.
>
> **Analogía:** el `.env` de Node, pero **tipado**. Y ahí está la diferencia interesante, que vemos en la sección 4.

---

## 2. `application.yml` — el archivo 🔴

Vive en `src/main/resources/`. Es el archivo de configuración de toda la aplicación, y en este repo tiene **dos líneas**:

```yaml
rest-countries:
    base-url: https://restcountries.com/v3.1
```

Eso es todo.

**YAML en una línea:** es un formato de texto para datos estructurados donde la **indentación es la que define la jerarquía** (como en Python). No lleva llaves ni comillas. Lo de arriba, traducido a JSON, sería:

```json
{ "rest-countries": { "base-url": "https://restcountries.com/v3.1" } }
```

`rest-countries` es un **prefijo**: una carpeta lógica donde agrupás todas las propiedades de un mismo tema. Si mañana necesitaras un timeout y una API key, irían adentro del mismo prefijo:

```yaml
rest-countries:
    base-url: https://restcountries.com/v3.1
    timeout-ms: 5000              # (ejemplo — NO está en el repo real)
    api-key: abc123               # (ejemplo — NO está en el repo real)
```

⚠️ **`.properties` vs `.yml`.** En clase preguntaron por la diferencia y Escobar contestó bien: *"son formas diferentes de escribirlo… se pueden escribir de las dos formas"*. Spring acepta ambos. El `.properties` es plano (`rest-countries.base-url=https://...`), el `.yml` es jerárquico. **Elegís uno.** El profe eligió `.yml` desde el Spring Initializr, y eso es lo que vas a usar todo el año.

---

## 3. `@ConfigurationProperties` — el puente 🔴

Ahora, la clase que lee ese archivo. Ocho líneas:

```java
package ar.edu.utn.ba.ddsi.countries.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rest-countries")
// ↑ LA ORDEN: "Spring, andá al application.yml, buscá el bloque 'rest-countries',
//    y volcá lo que haya adentro en los atributos de esta clase."
//    El prefix es la coordenada: le dice QUÉ bloque leer.

@Data
// ↑ Lombok. Genera getters y setters de todos los atributos.
//    ⚠️ NO es decorativo: SIN los setters, Spring NO PUEDE ESCRIBIR el valor acá adentro.
//    Volvemos sobre esto en el Punto 5 — es el mismo mecanismo que usa Jackson.

public class RestCountriesProperties {

    private String baseUrl;
    // ↑ Un atributo, y ya. Sin lógica, sin métodos, sin nada.
    //   Esta clase NO HACE nada: solo TRANSPORTA un valor de configuración.
    //   El getBaseUrl() que usa BuscadorDePaises lo generó @Data — no está escrito acá.
}
```

### El detalle que hace ruido: `base-url` → `baseUrl` 🔴

Mirá bien:

```yaml
base-url:                    # ← en el archivo, con guion  (kebab-case)
```
```java
private String baseUrl;      // ← en Java, camelCase, sin guion
```

**No coinciden. Y funciona igual.**

Eso se llama **relaxed binding** (enlace flexible): Spring normaliza los nombres antes de comparar. Le saca guiones, guiones bajos, mayúsculas, y recién ahí matchea. Para Spring, todas estas son **la misma propiedad**:

```
base-url        ✅  (kebab-case — la convención en YAML, y la que usa el profe)
base_url        ✅
baseUrl         ✅
BASE_URL        ✅  (mayúsculas: la convención de las variables de entorno del SO)
```

**Por qué existe esta flexibilidad, y no es un capricho:** YAML se escribe naturalmente con guiones, Java exige camelCase, y las variables de entorno de Linux van en MAYÚSCULA_CON_GUION_BAJO. El relaxed binding permite que las tres convenciones convivan sin que tengas que traducir a mano. En producción, esa misma propiedad se puede sobreescribir con una variable de entorno `REST_COUNTRIES_BASE_URL` **sin tocar una línea de código**.

En clase Escobar lo mencionó al pasar (*"este guion medio se va a traducir en que se pueda utilizar esta anotación"*). Esa es la explicación completa.

---

## 4. La diferencia con `.env`: esto es **tipado** 🟡

Acá está lo que Java te da y Node no.

```javascript
// Node — el .env te devuelve SIEMPRE un string. Siempre.
const timeout = process.env.TIMEOUT_MS;    // "5000"  ← ¡es un STRING!
setTimeout(fn, timeout);                   // funciona de casualidad (coerción)

const activo = process.env.FEATURE_ON;     // "false" ← STRING
if (activo) { ... }                        // 💥 BUG: "false" es truthy. Entra siempre.
```

```java
// Spring — la clase declara los TIPOS, y Spring convierte al leer
@ConfigurationProperties(prefix = "rest-countries")
@Data
public class RestCountriesProperties {
    private String  baseUrl;       // string  → queda string
    private Integer timeoutMs;     // "5000"  → Spring lo convierte a Integer  ✅
    private Boolean featureOn;     // "false" → Spring lo convierte a Boolean false ✅
    // (los dos últimos son ejemplo — el repo real solo tiene baseUrl)
}
// Y si en el .yml escribís timeout-ms: hola  →  la aplicación NO ARRANCA.
// Falla en el arranque, con un mensaje claro. No a las 3 AM en producción.
```

**Ese es el negocio:** convertís un archivo de texto en un **objeto Java con tipos**, validado al arrancar. La clase `RestCountriesProperties` no es burocracia — es el **contrato tipado** de tu configuración.

---

## 5. `@EnableConfigurationProperties` — el permiso 🟡

Falta una pieza, y es la que hizo aparecer el subrayado rojo en IntelliJ durante la clase.

`@ConfigurationProperties` sola **no alcanza**. Hay que decirle a Spring, explícitamente, que active esa clase. Y eso se hace en `CountriesApplication`:

```java
@SpringBootApplication
@EnableConfigurationProperties(RestCountriesProperties.class)
// ↑ "Spring: activá esta clase de propiedades. Instanciala, leé el yml, llenala,
//    y registrala como bean en el contenedor."
//    Sin esta línea, RestCountriesProperties NO ES UN BEAN.
//    → El constructor de BuscadorDePaises no encontraría el segundo parámetro.
//    → La aplicación NO ARRANCA.
public class CountriesApplication {
	public static void main(String[] args) {
		SpringApplication.run(CountriesApplication.class, args);
	}
}
```

**¿Por qué hace falta este paso extra, si `@Component` no lo necesitaba?**

Porque `@ConfigurationProperties` **no es** una anotación de estereotipo — no le dice a Spring "instanciá esto". Solo dice *"cuando alguien instancie esta clase, llenala desde el yml"*. Es la receta de llenado, no la orden de creación. `@EnableConfigurationProperties` es la orden de creación.

*(Existe un atajo: ponerle `@Component` a la clase de properties también funciona. El profe eligió el camino explícito, que es el idiomático y el que vas a ver en proyectos reales — mantiene la configuración declarada en un solo lugar visible.)*

---

## 6. El recorrido completo de un valor 🔴

Seguí la URL desde el archivo hasta el uso. Cuatro saltos:

```
   ┌────────────────────────────────────────────────────────────────────┐
   │  src/main/resources/application.yml                                │
   │                                                                    │
   │     rest-countries:                                                │
   │         base-url: https://restcountries.com/v3.1     ← EL VALOR    │
   └───────────────────────────┬────────────────────────────────────────┘
                               │  ① Spring lee el archivo al arrancar
                               │     y busca el bloque "rest-countries"
                               ▼
   ┌────────────────────────────────────────────────────────────────────┐
   │  @ConfigurationProperties(prefix = "rest-countries")               │
   │  public class RestCountriesProperties {                            │
   │      private String baseUrl;   ←── ② relaxed binding:              │
   │  }                                  base-url  →  baseUrl           │
   │                                     y el setter de @Data lo escribe│
   └───────────────────────────┬────────────────────────────────────────┘
                               │  ③ @EnableConfigurationProperties
                               │     la registra como BEAN en el contenedor
                               ▼
   ┌────────────────────────────────────────────────────────────────────┐
   │  CONTENEDOR:  "restCountriesProperties" → [baseUrl = "https://..."]│
   └───────────────────────────┬────────────────────────────────────────┘
                               │  ④ inyección por constructor (PUNTO 3)
                               ▼
   ┌────────────────────────────────────────────────────────────────────┐
   │  public class BuscadorDePaises {                                   │
   │      private final RestCountriesProperties propiedades;            │
   │                                                                    │
   │      public List<Pais> buscarTodos() {                             │
   │          ... .fromUriString(propiedades.getBaseUrl()) ...          │
   │                                    ↑                               │
   │                          EL VALOR, USADO.  (Punto 6)               │
   │      }                                                             │
   │  }                                                                 │
   └────────────────────────────────────────────────────────────────────┘
```

> 🧵 **Hilo abierto:** *"¿tanto lío para no escribir una URL?"* Se cierra en el **Punto 6**, cuando veas que **el test usa exactamente la misma URL que producción** — sin copiarla, sin hardcodearla, sin poder desincronizarse. Ese es el pago.

> 📌 **Para el parcial, si te preguntan: "¿Por qué se externaliza la configuración?"**
>
> Para que el mismo artefacto compilado pueda ejecutarse en distintos entornos (desarrollo, testing, producción) sin recompilarse: lo único que cambia es el archivo de configuración. Además evita duplicar valores en varios puntos del código y saca los datos sensibles (credenciales, claves) del repositorio. En Spring Boot se implementa con un archivo `application.yml` (o `.properties`) y clases anotadas con `@ConfigurationProperties`, que mapean esos valores a atributos Java tipados.

> 🕳️ **Madriguera — Perfiles de entorno**
> Spring permite tener `application-dev.yml`, `application-prod.yml`, etc., y elegir cuál se usa al arrancar (con `--spring.profiles.active=prod`). Es el mecanismo real por el que un mismo `.jar` apunta a una base de datos distinta en cada entorno. Existe, es central en cualquier proyecto profesional, **y no lo vas a necesitar en DSI**.
> *Volvé al camino — si algún día deployás algo en serio, googleá "spring profiles".*

> 🕳️ **Madriguera — `@Value`**
> La alternativa vieja: `@Value("${rest-countries.base-url}") private String baseUrl;` inyecta una propiedad suelta, sin clase intermedia. Sirve para casos de un solo valor, pero pierde el tipado agrupado y la validación. Vas a verla en tutoriales y en código legacy.
> *Volvé al camino — el repo usa `@ConfigurationProperties`, que es lo recomendado hoy.*

---

## ✅ Checkpoint — Punto 4

1. Nombrá tres problemas concretos de hardcodear la URL adentro de la clase.
2. ¿Qué es el `prefix` de `@ConfigurationProperties` y para qué sirve?
3. En el `.yml` la propiedad se llama `base-url` y en Java `baseUrl`. ¿Por qué funciona? ¿Cómo se llama ese comportamiento y por qué existe?
4. ¿Por qué `RestCountriesProperties` **necesita** el `@Data` de Lombok? Pista: no es por los getters.
5. ¿Qué pasa si borrás la línea `@EnableConfigurationProperties(...)` de `CountriesApplication`? Sé preciso: ¿falla al compilar, al arrancar, o al usar?
6. ¿Por qué `@ConfigurationProperties` no alcanza por sí sola para crear el bean, si `@Component` sí alcanzaba?
7. ¿Cuál es la ventaja concreta de este mecanismo frente al `.env` de Node?
8. `RestCountriesProperties` no tiene ni un método escrito. ¿Es entonces una clase inútil? Justificá.

---

## 🎯 Qué viene en el Punto 5

Ya entendés **tres cuartas partes** del repo: quién crea los objetos, cómo se conectan, y de dónde sale la configuración.

Falta la parte que más código tiene y que en clase pasó volando: **los DTOs**. Tres clases (`Pais`, `NombrePais`, `DetalleMoneda`) llenas de anotaciones raras, sin un solo método.

En el Punto 5: por qué en JavaScript el objeto te viene gratis con `JSON.parse` y en Java tenés que **declarar la forma del JSON antes de recibirlo**. Qué hacen `@JsonProperty` y `@JsonIgnoreProperties`. Por qué Jackson necesita ese `@NoArgsConstructor` de Lombok.

Y de yapa: **el bug que Escobar cometió en vivo** (escribió `comon` en vez de `common`), por qué el programa **no explotó** sino que devolvió `null` — y qué te enseña eso sobre cómo funciona Jackson por dentro.

---

**FIN DEL PUNTO 4**
