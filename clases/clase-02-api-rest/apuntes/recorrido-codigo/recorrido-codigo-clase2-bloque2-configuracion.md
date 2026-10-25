# 📘 Bloque 2 — IoC, Beans y Configuración

> **Objetivo:** entender el concepto que define a Spring: **Inversión de Control**. Aprender qué es un **bean**, cómo se declara con `@Bean` o `@Component`, cómo se **inyecta** en otras clases, y cómo Spring lee `application.yml` y lo convierte en un objeto Java usable.
>
> **Por qué importa:** este bloque es **el "clic mental" más grande** del recorrido. Si entendés esto, todo el resto del código del profe se cae solo. Si no, todo va a parecer magia oscura.
>
> **Pre-requisito:** Bloque 1 completo.

---

## 🧭 Mapa del bloque

1. El problema que IoC resuelve (qué pasaba en tu Proyecto 0).
2. **IoC** y **DI** explicados con analogía concreta.
3. Qué es un **bean**.
4. Las dos formas de declarar beans: `@Component` vs `@Bean`.
5. `RestTemplateConfig.java` desarmado.
6. `RestCountriesProperties.java` desarmado.
7. Inyección de dependencias por constructor (lo que vas a ver en Bloque 4).
8. Cómo todo se conecta cuando Spring arranca.
9. Comparación con tu Proyecto 0.

---

## 🎯 Parte 1: El problema que esto resuelve

Volvé mentalmente al Proyecto 0. ¿Cómo construías instancias?

```java
// Main.java del Proyecto 0
public static void main(String[] args) {
    CatalogoPaises catalogo = new CatalogoPaises();
    // ...
}
```

`new CatalogoPaises()`. Vos sabés cómo construir un catálogo. Llamás al constructor a mano.

Ahora imaginá que `CatalogoPaises` necesita un `RestTemplate` para hacer HTTP. Tendrías que hacer:

```java
RestTemplate restTemplate = new RestTemplate();
CatalogoPaises catalogo = new CatalogoPaises(restTemplate);
```

Y si `CatalogoPaises` también necesitara un `Logger`, un `Cache`, un `Validador`...

```java
Logger logger = LoggerFactory.getLogger(...);
Cache cache = new RedisCache(host, port, credenciales);
Validador validador = new ValidadorEstandar(reglas);
RestTemplate restTemplate = new RestTemplate();
CatalogoPaises catalogo = new CatalogoPaises(restTemplate, logger, cache, validador);
```

**Tu `Main` ahora sabe construir todo eso.** Si el `RestTemplate` necesita configuraciones, también las pone el `Main`. Si querés cambiar de `RedisCache` a `MemoryCache`, modificás el `Main`.

Resultado: **el `Main` se vuelve un quilombo gigante** que sabe demasiado. Y si en algún otro lugar también querés un `CatalogoPaises`, **duplicás todo ese setup**.

Esto se llama **acoplamiento alto**: el código que **usa** un objeto está acoplado al código que **lo crea**. Y eso es lo que IoC viene a romper.

---

## 🔄 Parte 2: IoC e DI — los conceptos centrales

### Inversión de Control (IoC)

**Lectura literal:** "el control se invierte".

En tu Proyecto 0, **vos** tenías el control. **Vos** decidías cuándo crear el catálogo, cuándo crear el RestTemplate, en qué orden, con qué configuración.

En Spring, **el framework** tiene el control. Spring decide cuándo crear el catálogo, cuándo crear el RestTemplate, en qué orden, con qué configuración. **Vos solo declarás "necesito esto"** y Spring se encarga.

> El control sobre la **creación** y el **ciclo de vida** de los objetos se "invierte" — pasó de estar en tus manos al framework.

### Inyección de Dependencias (DI)

DI es **la técnica** que IoC usa para llegar a tus clases. En vez de que vos hagas `new RestTemplate()` adentro de tu clase, Spring **te entrega** un RestTemplate ya construido. Te lo "inyecta".

```java
// SIN inyección (como en Proyecto 0):
public class BuscadorDePaises {
    private RestTemplate restTemplate = new RestTemplate();  // construido acá adentro
    // ...
}

// CON inyección (Spring):
public class BuscadorDePaises {
    private final RestTemplate restTemplate;
    
    // Spring le pasa el RestTemplate construido afuera
    public BuscadorDePaises(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
```

**La clase no sabe ni le importa cómo se construye el RestTemplate.** Solo dice "necesito uno". Spring lo crea según las reglas de configuración y se lo pasa por el constructor.

### Analogía concreta: el restaurante

Imaginá dos formas de comer pasta:

**Modelo "vos cocinás" (sin IoC):**
- Vas al super, comprás harina, huevos, salsa.
- Volvés a tu casa, amasás, cocinás, salseás.
- Comés.
- Si querés cambiar de receta, comprás otros ingredientes y rehacés todo el proceso.

**Modelo "restaurante" (con IoC):**
- Te sentás en el restaurante.
- Pedís: "una pasta con bolognesa".
- Te traen el plato listo.
- Si querés otro plato, **solo cambiás el pedido**. No te enterás de cómo cambia la cocina.

**Spring es el restaurante.** Vos sos el cliente que pide. La cocina (Spring) sabe qué ingredientes (beans) necesita combinar para servirte el plato. Vos no te enterás de cómo se preparó. **Si la cocina cambia el proveedor de carne**, vos seguís pidiendo "bolognesa" y todo funciona.

---

## 🫘 Parte 3: ¿Qué es un Bean?

**Un bean es un objeto que Spring administra.** Eso es todo.

Cuando Spring arranca, crea un "contenedor" llamado **ApplicationContext** (vas a oír este nombre mucho). Adentro del contexto viven los beans:

```
ApplicationContext
├── restTemplate              (bean, instancia única)
├── restCountriesProperties   (bean, instancia única)
└── buscadorDePaises          (bean, instancia única)
```

**Cuándo Spring crea los beans:** al arrancar, todos juntos. Los beans existen **antes** de que tu código los pida.

**Cuántas instancias hay:** **una sola por bean por default** (esto se llama "singleton scope"). Si dos clases inyectan `RestTemplate`, **reciben la misma instancia**.

**Cuándo se destruyen:** cuando la app termina. Mientras la app vive, los beans viven.

---

## 🏷️ Parte 4: Las dos formas de declarar un bean

Spring necesita que vos le digas **qué clases son beans**. Hay dos mecanismos:

### Forma A: Annotation `@Component` (y sus primos)

Marcás la clase directamente:

```java
@Component
public class BuscadorDePaises {
    // ...
}
```

Spring escanea los packages (por `@ComponentScan` que recordás de Bloque 1) y registra **automáticamente** cualquier clase anotada con:
- `@Component` (genérico)
- `@Service` (sinónimo semántico — para clases de "lógica de negocio")
- `@Repository` (sinónimo semántico — para clases de acceso a datos)
- `@Controller` / `@RestController` (sinónimo semántico — para endpoints HTTP)

**Las cuatro hacen lo mismo técnicamente**: registrar la clase como bean. La diferencia es **semántica** — al lector le dice qué rol cumple la clase.

### Forma B: Annotation `@Bean` dentro de una clase `@Configuration`

Cuando **NO podés** ponerle `@Component` a una clase (típicamente porque viene de una librería externa, ej. `RestTemplate`), creás un método que la produce:

```java
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

- `@Configuration` marca la clase como "fuente de beans".
- `@Bean` marca el método como "este método produce un bean cuando Spring lo llame".

**Spring llama el método una sola vez** al arrancar y guarda el resultado en el contexto. A partir de ahí, cualquiera que pida `RestTemplate` recibe ese mismo objeto.

### Cuándo usar cada uno

| Situación | Annotation |
|---|---|
| Es **tu** clase, escrita por vos | `@Component` (o `@Service`, etc.) |
| Es una clase de **librería externa** (no podés modificarla) | `@Bean` dentro de `@Configuration` |
| Necesitás **lógica de construcción** (config compleja, condicionales) | `@Bean` dentro de `@Configuration` |

> El profe usa **`@Component`** para `BuscadorDePaises` (su propia clase) y **`@Bean`** para `RestTemplate` (clase de Spring). Pura aplicación de la regla.

---

## 🔍 Parte 5: `RestTemplateConfig.java` desarmado

```java
package ar.edu.utn.ba.ddsi.countries.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### Línea por línea

**`@Configuration`**
```java
@Configuration
public class RestTemplateConfig {
```

Le dice a Spring: "esta clase declara beans. Cuando me escanees, leé los métodos `@Bean` adentro y ejecutalos para crear beans".

**`@Bean`**
```java
@Bean
public RestTemplate restTemplate() {
```

"Cuando arranques, llamá este método y guardá lo que devuelve como un bean".

**Detalle sobre el nombre del bean:**

Por convención, **el nombre del bean es el nombre del método**. En este caso el bean se llama `restTemplate` (camelCase del nombre del método).

Esto importa si dos métodos `@Bean` devuelven el mismo tipo:

```java
@Bean
public RestTemplate restTemplateInterno() { ... }   // bean "restTemplateInterno"

@Bean
public RestTemplate restTemplateExterno() { ... }   // bean "restTemplateExterno"
```

Spring distingue por nombre cuando hay ambigüedad.

**El método en sí**
```java
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

**Es código Java normal**. Hace `new RestTemplate()` y lo devuelve. **El `new` no desapareció — solo se mudó acá adentro.** La diferencia es que esto se ejecuta **una sola vez al arrancar Spring**, y a partir de ahí ese objeto vive en el contexto.

**¿Por qué no hacer `new RestTemplate()` adentro del `BuscadorDePaises` directamente?**

1. **Centralización:** si necesitás configurar el `RestTemplate` (timeouts, interceptors, autenticación), lo hacés acá, en un solo lugar. Si lo construyera adentro del `BuscadorDePaises`, esa config estaría diseminada.

2. **Reutilización:** si después tenés `BuscadorDeCiudades`, `BuscadorDeUsuarios`, etc., **todos comparten el mismo `RestTemplate`**. Una instancia, varias clases que la usan.

3. **Testeabilidad:** en un test podés inyectar un `RestTemplate` mockeado en vez del real. Si estuviera hardcodeado adentro, no podrías.

4. **Singleton implícito:** Spring garantiza una sola instancia. Sin Spring, podrías crear 10 sin querer.

### Versión "ampliada" típica (para que sepas qué viene)

En proyectos reales, ese método suele tener configuración:

```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplateBuilder()
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(10))
        .additionalInterceptors(loggingInterceptor)
        .build();
}
```

El profe tiene la versión mínima — pero la **estructura es la misma**.

---

## 🔍 Parte 6: `RestCountriesProperties.java` desarmado

```java
package ar.edu.utn.ba.ddsi.countries.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rest-countries")
@Data
public class RestCountriesProperties {

    private String baseUrl;
}
```

Esta clase tiene UN solo campo. ¿Para qué tanta annotation?

### `@ConfigurationProperties(prefix = "rest-countries")`

Le dice a Spring: **"leé el `application.yml` (o `.properties`) y mapeá las keys que empiezan con `rest-countries.` a los atributos de esta clase"**.

Mirá la conexión:

```yaml
# application.yml
rest-countries:                    # ← matchea con el prefix
    base-url: https://...           # ← matchea con baseUrl
```

```java
// RestCountriesProperties.java
@ConfigurationProperties(prefix = "rest-countries")
public class RestCountriesProperties {
    private String baseUrl;        // → recibe "https://..."
}
```

**Conversión automática de nombres:** Spring convierte `base-url` (kebab-case del YAML) a `baseUrl` (camelCase de Java). También acepta `base_url` (snake_case). Es flexible.

### `@Data` (Lombok)

Lombok genera **getters y setters**. **Importante:** Spring **necesita los setters** para asignar los valores del YAML a la clase. Sin `@Data` (o sin setters manuales), Spring no puede asignar `baseUrl`.

Es la razón por la que esta clase **es mutable** (tiene setters). Aunque la usemos como inmutable después, técnicamente Spring necesita poder llamar `setBaseUrl(...)`.

### ¿Por qué NO tiene `@Component`?

Por **separación de conceptos**:

- `@Component` es para clases de **lógica/comportamiento** (servicios, repositorios, controllers).
- `@ConfigurationProperties` es para **POJOs de configuración** (datos puros leídos del YAML).

Como **no tiene `@Component`**, `@ComponentScan` la ignora. **Por eso necesita estar registrada manualmente**, con `@EnableConfigurationProperties(RestCountriesProperties.class)` en `CountriesApplication.java`. ¿Te acordás de esa línea del Bloque 1? **Ahora tiene sentido.**

> Existe una alternativa: poner `@Component` directamente a `RestCountriesProperties`. Funciona pero es menos prolija. La forma idiomática es la que el profe usa.

---

## 💉 Parte 7: Inyección de dependencias por constructor

Acá es donde **todo lo anterior se conecta**. Te muestro **anticipadamente** cómo se usan estos dos beans en el `BuscadorDePaises` (lo vas a ver en detalle en Bloque 4):

```java
@Component
public class BuscadorDePaises {

    private final RestTemplate restTemplate;
    private final RestCountriesProperties propiedades;

    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }

    // métodos que usan restTemplate y propiedades...
}
```

**Lectura:** "Spring, cuando crees una instancia de `BuscadorDePaises`, pasale por el constructor un `RestTemplate` y un `RestCountriesProperties`. Ya sé que los tenés en el contexto".

Spring ve esa firma del constructor y dice "ah, necesita estos dos beans, los tengo, los inyecto". **Automáticamente.**

### Las 3 formas de inyección (para que las reconozcas)

```java
// FORMA 1: Por constructor (LA RECOMENDADA, lo que usa el profe)
@Component
public class BuscadorDePaises {
    private final RestTemplate restTemplate;
    
    public BuscadorDePaises(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}

// FORMA 2: Por field con @Autowired (estilo viejo)
@Component
public class BuscadorDePaises {
    @Autowired
    private RestTemplate restTemplate;
}

// FORMA 3: Por setter con @Autowired (raro hoy)
@Component
public class BuscadorDePaises {
    private RestTemplate restTemplate;
    
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
```

**Por qué constructor injection es lo mejor:**

1. **Inmutabilidad:** el atributo es `final`. Nadie lo puede cambiar después.
2. **Sin `@Autowired`:** desde Spring 4.3, si la clase tiene **un solo constructor**, Spring lo usa para inyectar automáticamente. No hace falta annotation.
3. **Testeable sin Spring:** podés crear un `new BuscadorDePaises(mockRestTemplate, mockProps)` en un test unitario sin levantar todo Spring.
4. **Las dependencias son explícitas:** la firma del constructor te dice qué necesita la clase. Field injection las esconde.

> **Anti-patrón a evitar:** field injection con `@Autowired`. Spring lo permite por compatibilidad pero está desaconsejado hace años. Si ves `@Autowired` arriba de un atributo, es código viejo.

---

## 🔗 Parte 8: Cómo todo se conecta cuando Spring arranca

Vamos a hacer el "viaje" desde que apretás Run hasta que `BuscadorDePaises` está listo para usarse. Es importante visualizar la secuencia:

```
1. SpringApplication.run(...) arranca
                ↓
2. Crea el ApplicationContext (contenedor vacío)
                ↓
3. Activa @ComponentScan
   → Escanea ar.edu.utn.ba.ddsi.countries.*
   → Encuentra: BuscadorDePaises (@Component)
   → Encuentra: RestTemplateConfig (@Configuration)
                ↓
4. Lee application.yml
                ↓
5. Procesa @EnableConfigurationProperties(RestCountriesProperties.class)
   → Crea instancia de RestCountriesProperties
   → Llama setBaseUrl("https://restcountries.com/v3.1")
   → Guarda como bean "restCountriesProperties" en el contexto
                ↓
6. Procesa @Configuration de RestTemplateConfig
   → Llama método restTemplate()
   → Recibe new RestTemplate()
   → Guarda como bean "restTemplate" en el contexto
                ↓
7. Procesa @Component de BuscadorDePaises
   → Ve que su constructor pide RestTemplate y RestCountriesProperties
   → Busca en el contexto: ¿hay esos beans? ✅ sí
   → Llama new BuscadorDePaises(restTemplate, restCountriesProperties)
   → Guarda como bean "buscadorDePaises"
                ↓
8. Todos los beans creados ✅
                ↓
9. Tomcat arranca en :8080
                ↓
10. App lista. main termina pero JVM sigue.
```

**Punto clave:** Spring **resuelve el orden de creación automáticamente**. Como `BuscadorDePaises` necesita los otros dos, Spring crea esos **primero**, después construye `BuscadorDePaises`. Es como resolver un puzzle: empieza por las piezas que no dependen de nada, y avanza.

> Si hubiera una **dependencia circular** (A necesita B y B necesita A), Spring tira excepción al arrancar diciendo "no puedo resolver esto". Es un olor a código mal diseñado.

---

## 🆚 Parte 9: Comparación con tu Proyecto 0

| Aspecto | Proyecto 0 | rest-paises |
|---|---|---|
| Creación de objetos | `new` manual en el `Main` | Spring lo hace por annotations |
| Lugar de la creación | El código que usa lo crea | Centralizado en `@Configuration` |
| Cómo recibe sus dependencias | Constructor que vos llamás | Constructor que Spring llama |
| Cantidad de instancias | Las que vos crees | Una sola por bean (singleton) |
| Configuración externa | Constantes en código | `application.yml` mapeado a clases |
| Mock para tests | Reemplazás la instancia a mano | Spring permite inyectar mocks |
| Acoplamiento entre clases | Alto (cada una crea las suyas) | Bajo (dependen de tipos, no de cómo crearlos) |

**Ejemplo concreto** del cambio mental:

```java
// Proyecto 0 (acoplado)
public class CatalogoPaises {
    private final RestTemplate rt = new RestTemplate();  // SÉ cómo construirlo
    // ...
}

// rest-paises (desacoplado)
@Component
public class BuscadorDePaises {
    private final RestTemplate rt;
    
    public BuscadorDePaises(RestTemplate rt) {
        this.rt = rt;
    }  // Solo digo "necesito uno". No sé cómo se construyó.
}
```

---

## 🪄 Parte 10: Sobre la "magia"

Spring puede parecer mágico. **No lo es.** Es **annotation processing + reflection**:

1. Al arrancar, Spring lee tus annotations (`@Component`, `@Bean`, `@ConfigurationProperties`, etc.).
2. Usa **reflection** (la API de Java para inspeccionar clases en runtime) para descubrir constructores, métodos, atributos.
3. Construye un grafo mental de dependencias.
4. Resuelve el grafo creando los beans en el orden correcto.
5. Inyecta cada bean donde lo necesitan.

**Es código Java normal corriendo en runtime.** Si abrieras el bytecode de Spring, verías loops y `if`s y `Class.forName`, no magia. Pero el resultado para vos es: **declarás intenciones, Spring las realiza**.

> Compará con Lombok (que ya conocés): Lombok hace lo suyo en **tiempo de compilación**. Spring hace lo suyo en **tiempo de ejecución**. Lombok modifica el `.class` antes de que la JVM lo cargue. Spring inspecciona el `.class` cargado y crea instancias dinámicamente. Distintas técnicas, ambos basados en annotations.

---

## ✅ Checkpoint

Si podés contestar mentalmente estas, estás listo para Bloque 3:

1. ¿Qué es IoC y qué problema resuelve?
2. ¿Por qué `RestTemplate` se declara con `@Bean` y `BuscadorDePaises` se declara con `@Component`?
3. ¿Cuántas instancias de `RestTemplate` crea Spring por default?
4. ¿Por qué `RestCountriesProperties` no tiene `@Component` y sí necesita `@EnableConfigurationProperties`?
5. ¿Por qué `@Data` es necesario en `RestCountriesProperties`?
6. ¿Qué hace Spring cuando ve el constructor `BuscadorDePaises(RestTemplate, RestCountriesProperties)`?
7. ¿Por qué la inyección por constructor es preferida sobre `@Autowired` en un field?
8. Si dos clases inyectan `RestTemplate`, ¿reciben la misma instancia o distintas?

---

## 🎯 Mini-experimentos mentales

**Caso 1:** Borrás el `@Configuration` de `RestTemplateConfig`.
→ Spring no procesa los `@Bean` adentro. **No crea el `RestTemplate`**. Cuando llega a `BuscadorDePaises` y necesita inyectarlo, tira `NoSuchBeanDefinitionException` al arrancar.

**Caso 2:** Borrás el `@Component` de `BuscadorDePaises`.
→ Spring no lo registra como bean. **No lo crea**. Si nadie lo necesita, la app arranca normal. Si alguien lo necesita, tira excepción.

**Caso 3:** Cambiás `prefix = "rest-countries"` a `prefix = "rest-paises"` en `RestCountriesProperties` pero **no** cambiás el YAML.
→ Spring busca `rest-paises.base-url` en el YAML. **No lo encuentra**. `baseUrl` queda `null`. La app arranca pero crashea cuando alguien usa `propiedades.getBaseUrl()` para armar una URL.

**Caso 4:** Hacés `new BuscadorDePaises(new RestTemplate(), new RestCountriesProperties())` en algún test.
→ **Funciona.** Spring no es obligatorio — es un framework. Las clases siguen siendo Java normal. Podés construirlas a mano. Spring solo facilita el armado en runtime.

Si entendés por qué pasa cada cosa, dominás el Bloque 2.

---

## 🔗 Lo que viene en Bloque 3

Vamos a desarmar los **DTOs** del profe: `Pais.java`, `NombrePais.java`, `DetalleMoneda.java`.

Cosas que vas a ver:
- Cómo `@Data + @AllArgsConstructor + @NoArgsConstructor` aparecen tal cual los aprendiste.
- **Las annotations de Jackson** (`@JsonProperty`, `@JsonIgnoreProperties`) — cómo Java mapea JSON ↔ objeto.
- **Composición**: un Pais tiene un `NombrePais` (otro objeto) y un `Map<String, DetalleMoneda>` (que ya armaste vos en Etapa 6).
- Por qué necesita constructor sin args (Jackson lo usa por reflection).

Es el bloque **más fácil** comparado con este — la mayor parte ya la viviste en el Proyecto 0.

---

## ▶️ Próximo paso

Cuando quieras, decime **"vamos al bloque 3"** y arrancamos con los DTOs y Jackson.

Si tenés dudas conceptuales de IoC o beans, esta es la oportunidad de aclararlas antes de avanzar. **Es el bloque donde más vale la pena no apurarse.**
