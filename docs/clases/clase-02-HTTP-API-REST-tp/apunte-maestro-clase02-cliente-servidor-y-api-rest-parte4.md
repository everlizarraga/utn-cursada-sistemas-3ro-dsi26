# Apunte Maestro — Clase 02 · Parte 4
## Práctica: consumir una API REST

---

## 🧭 De dónde venís

**Parte 1:** el patrón cliente-servidor. **Parte 2:** HTTP. **Parte 3:** API y API REST.

**Ahora se pone en código.** Vamos a consumir una API REST real de tres formas distintas —navegador, Postman y Java con Spring Boot—, y cerramos con la información operativa del TPA.

> **Nota sobre esta parte:** el bloque de Java introduce **Spring Boot**, que no estaba en el material previo. Acá está **todo lo necesario para entender el código y usarlo en el TPA**. Si querés el *porqué* profundo de cada anotación del framework —qué es el contenedor, por qué no hay `new`, cómo funciona la inyección de dependencias— eso está desarrollado a fondo en el **recorrido de Spring Boot de esta misma unidad**. Este apunte se para solo; el recorrido es la lupa.

---

## 1. La API del ejemplo 🟡

**REST Countries** (`restcountries.com/v3.1`): devuelve información sobre más de 250 países. Es **abierta y gratuita**: no hace falta registrarse ni pagar.

**Nosotros somos los clientes.** Ese componente expone una interfaz REST; nosotros nos comunicamos con él por HTTP, usando sus verbos y sus rutas. **Es literalmente el diagrama de la Parte 1, con nosotros del lado izquierdo.**

**Sus endpoints principales** *(un **endpoint** es una ruta concreta que el servicio expone y a la que podés llamar — el término lo vas a escuchar en todos lados)*:

```
   GET /all                    → todos los países
   GET /name/{nombre}          → busca por nombre
   GET /currency/{codigo}      → busca por código de moneda
   GET /region/{region}        → busca por región
   GET /capital/{capital}      → busca por capital
```

---

## 2. Consumir desde el navegador 🔴

Escribís esto en la barra de direcciones y apretás Enter:

```
https://restcountries.com/v3.1/currency/ars
```

Y te devuelve un JSON con Argentina. Abrís `F12` → **Network**, y ves:

| Lo que ves | Qué es |
|---|---|
| **Request URL** | exactamente lo que tipeaste |
| **Request Method: GET** | ← 👀 |
| **Status Code: 200 OK** | Parte 2, sección 6 |
| **Remote Address: …:443** | puerto 443 → **HTTPS** |
| **Content-Type: application/json** | el servidor te contestó en JSON |

### 🔴 La conclusión de esta demo

> **Cada vez que escribís algo en la barra del navegador y apretás Enter, estás haciendo una solicitud HTTP de tipo `GET`.**
>
> Siempre. Todos los días. Sin excepción.

**El navegador es un cliente HTTP.** Ese es su trabajo.

### 🔴 Y su límitación, que es la razón por la que existe Postman

**Desde la barra del navegador solo podés hacer `GET`.** No hay forma de escribir un `POST` en la barra de direcciones.

Para hacer un `POST` desde un navegador necesitás **un formulario HTML** o **código JavaScript ejecutándose** en la página. Y un `PATCH` o un `DELETE`, ni con eso.

**Entonces el navegador se te queda corto** apenas querés probar algo que no sea leer. Y para eso existen los **clientes HTTP**.

---

## 3. Consumir desde Postman 🔴

> **Postman** es un **cliente HTTP**: un programa que instalás en tu máquina y que te deja armar cualquier request HTTP —con cualquier método, cualquier header y cualquier body— y ver la respuesta completa.

Es lo que el navegador no puede ser. Y es la herramienta que vas a usar todo el año.

### La pantalla, solapa por solapa

| Elemento | Para qué |
|---|---|
| **Método** | Elegís: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS. **Acá sí podés.** |
| **URL** | La ruta a la que vas |
| **Solapa Authorization** | Si la API es **cerrada**: acá va tu credencial |
| **Solapa Headers** | Los headers de la request (Parte 2, sección 3) |
| **Solapa Body** | Los datos a enviar. **Solo se usa en POST, PUT y PATCH.** |
| **Botón Send** | Dispara la request |
| **Panel de respuesta** | Código de estado, tiempo en ms, tamaño, y el body |

> **Fijate cómo la interfaz de Postman ES la anatomía de una request HTTP.** Método, URL, headers, body. No es casualidad: es la Parte 2, con botones.

### 🔴 Path params vs Query params

Esta distinción es de las más evaluables de la clase, y en Postman se ve clarísima.

#### Path param — **identifica** un recurso

Va **dentro de la ruta**:

```
   GET  https://restcountries.com/v3.1/name/{name}
                                             ↑
                              placeholder: lo reemplazás por el valor

   GET  https://restcountries.com/v3.1/name/argentina   → devuelve Argentina
   GET  https://restcountries.com/v3.1/name/uruguay     → devuelve Uruguay
```

**La ruta cambia según el recurso que pedís.** Eso es identificar. (Es el criterio ④ de REST: *los recursos se identifican mediante URI*.)

#### Query param — **filtra o modifica** la consulta

Va **después del `?`**, con formato **clave=valor**, y se encadenan con `&`:

```
   GET  https://restcountries.com/v3.1/all?fields=name,flags&currency=ars
                                          ↑              ↑  ↑
                                          │              │  └── separa: &
                                          │              └───── valor
                                          └── acá empiezan los query params
```

**El recurso es el mismo (`/all`); lo que cambia es qué te traés de él.** Eso es filtrar.

| | **Path param** | **Query param** |
|---|---|---|
| Dónde va | En la ruta: `/name/argentina` | Después del `?`: `?fields=name` |
| Para qué | **Identifica** un recurso | **Filtra o modifica** la consulta |
| Formato | Parte del path | `clave=valor`, encadenados con `&` |

*(Hay un límite de query params, pero es altísimo. No lo vas a alcanzar.)*

### 🔴 El descubrimiento en vivo: el 400

En clase quitaron el query param `fields` y llamaron a `/all` pelado. El servidor contestó:

```
   400 Bad Request
   { "message": "'fields' query parameter is required" }
```

**Esa API exige `fields`.** Sin él, no responde — porque devolver los 250 países completos le costaría carísimo.

> 🔴 **La lección, que vale más que el ejemplo:** *"Cada API es distinta. **Siempre tenés que leer su documentación.**"* Esta acepta que le filtres campos; otra no. Esta exige `fields`; otra no lo conoce. **No hay reglas universales más allá del estándar REST.**

---

## 4. Consumir desde Java + Spring Boot 🔴

Acá aparece el stack de toda la materia.

> **Spring Boot** es el **framework** que vas a usar todo el año, tanto en las prácticas de clase como en el TPA. Un **framework** es código que te da el esqueleto de la aplicación ya armado: vos escribís las piezas, y **él las crea, las conecta y las ejecuta**. Es distinto de una biblioteca, donde vos llamás al código de otro; acá **el framework llama al tuyo**.
>
> *(La cátedra lo adoptó explícitamente para que te lleves esa experiencia al mercado.)*

### 4.1 Crear el proyecto — Spring Initializr 🟡

`start.spring.io` es la web que genera el esqueleto del proyecto. Elegís y te bajás un `.zip`.

**Lo que se eligió en clase, y lo que vas a elegir en el TPA:**

| Campo | Valor |
|---|---|
| **Lenguaje** | Java |
| **Gestor de dependencias** | **Maven** |
| **Versión de Spring Boot** | **4.0.5** |
| **Versión de Java** | **21** |
| **Group** | `ar.edu.utn.ba.ddsi` — convención de la cátedra *(Argentina · educativo · UTN · Buenos Aires · DDSI)* |
| **Artifact** | el nombre del proyecto |
| **Formato de configuración** | **YAML** (`.yml`), no `.properties` |
| **Dependencias** | **Spring Web MVC** · **Configuration Processor** · **DevTools** *(+ Lombok)* |

🔴 **Regla dura del TPA:** **todos los integrantes del equipo tienen que usar la misma versión de Java (21).** Con versiones distintas, el proyecto no compila igual para todos.

### 4.2 La estructura del proyecto 🔴

```
   proyecto/
   ├── .idea/          ← ❌ NO SE SUBE AL REPO. Config de IntelliJ, local tuya.
   ├── .mvn/           ← ✅ sí se sube. Config de Maven, general del proyecto.
   ├── src/            ← ✅ ACÁ VA TU CÓDIGO. La carpeta que importa.
   │   ├── main/       ←    código fuente y reglas de negocio
   │   │   ├── java/            → las clases
   │   │   └── resources/       → configuración (application.yml)
   │   └── test/       ←    los tests
   ├── target/         ← ❌ NO SE SUBE AL REPO. Código Java compilado.
   └── pom.xml         ← ✅ el archivo de Maven: acá viven las dependencias
```

> 🔴 **`.idea/` y `target/` van al `.gitignore`.** El profe lo dijo dos veces y aclaró *"primera y última vez que lo digo"* — o sea, **lo va a mirar en las entregas**.
>
> **`.idea/`** tiene la configuración de *tu* IntelliJ: si un compañero tiene otra versión u otras preferencias, van a generar **conflictos de merge constantes**.
> **`target/`** es código **compilado**: se regenera solo. Subirlo es subir basura.

**`pom.xml`** es el archivo de **Maven**, el gestor de dependencias. Es **el equivalente del `package.json` de Node** (con Maven donde iría npm): declarás qué bibliotecas necesitás, y Maven las baja. Cuando agregás una dependencia, apretás el ícono de Maven → **Refresh** para que se descargue.

**`main/` vs `test/`:** en `main` va el código de tu aplicación; en `test`, los tests.

### 4.3 El código, línea por línea 🔴

El objetivo: **un componente que consulte países y devuelva objetos Java.** Y el orden en que se construye no es casual — es el orden en que conviene pensarlo:

```
   ① La interfaz    → qué métodos voy a exponer      (el CONTRATO)
   ② El molde       → qué forma tiene el JSON        (los DATOS)
   ③ El uso         → cómo se va a usar              (el TEST)
   ④ La conexión    → la llamada HTTP                (lo tedioso, al final)
```

#### ① El componente y su interfaz

```java
package ar.edu.utn.ba.ddsi.countries.services;

import org.springframework.stereotype.Component;

@Component
// ↑ Le dice a Spring: "esta clase es tuya — instanciala vos al arrancar
//   y guardala, para que cualquiera pueda pedírtela."
//   ⚠️ NUNCA vamos a hacer `new BuscadorDePaises(...)`. Lo hace el framework.
public class BuscadorDePaises {

    // La INTERFAZ de este componente = sus métodos públicos.
    // Esto es lo que otros van a poder pedirle. Es el contrato de la clase 1.

    public List<Pais> buscarTodos() { ... }
    public Optional<Pais> buscarPorNombre(String nombre) { ... }
    public List<Pais> buscarPorMoneda(String codigoMoneda) { ... }
    public List<Pais> buscarPorRegion(String region) { ... }
    public List<Pais> buscarPorCapital(String capital) { ... }
}
```

🔴 **`Optional<Pais>` vs `List<Pais>` — y esto es diseño, no sintaxis:**

- **`buscarPorNombre` devuelve `Optional`** porque un nombre identifica a **un solo país**: existe o no existe. `Optional` es la forma explícita de decir *"esto puede no venir"*.
- **`buscarPorMoneda` devuelve `List`** porque **muchos países comparten una moneda** (el euro, veinte). Es una colección por naturaleza.

**La firma del método comunica el contrato**: quien la lee sabe qué esperar sin abrir el cuerpo.

#### ② Los moldes: DTOs

**Acá está la diferencia que Java te impone y JavaScript no.**

En JS hacés `await res.json()` y ya tenés el objeto. **En Java, que es fuertemente tipado, tenés que declarar de antemano la forma que va a tener el JSON.** El compilador necesita saber que existe un `Pais` con un atributo `nombre` — si no, ni siquiera compila.

Esas clases-molde se llaman **DTO** (*Data Transfer Object*): objetos **sin lógica**, cuyo único trabajo es **transportar datos**. En clase se los llamó *"objetos moldes"*.

**Este es el JSON que devuelve la API** *(recortado a lo que pedimos)*:

```json
[                                   ← ⚠️ ARRAY. Siempre, aunque venga un solo país.
  {
    "name": { "common": "Argentina", "official": "Argentine Republic" },
    "capital": ["Buenos Aires"],    ← ⚠️ array: un país puede tener varias capitales
    "region": "Americas",
    "currencies": { "ARS": { "name": "Argentine peso", "symbol": "$" } },
    "cca2": "AR"
  }
]
```

**Y este es el molde que lo espeja:**

```java
package ar.edu.utn.ba.ddsi.countries.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
// ↑ "Si el JSON trae campos que yo no declaré, IGNORALOS."
//   ⚠️ Sin esto, un campo desconocido lanza EXCEPCIÓN y te tira el programa.
//   Es el seguro contra que la API agregue campos sin avisarte.

@Data                  // ← Lombok: genera getters Y SETTERS de todos los atributos
@NoArgsConstructor     // ← Lombok: genera el constructor VACÍO
@AllArgsConstructor    // ← Lombok: genera el constructor con todos los parámetros
//   ⚠️ Estas tres NO son opcionales. Ver el "¿cómo funciona?" de abajo.

public class Pais {

    @JsonProperty("name")            // el JSON lo llama "name"...
    private NombrePais nombre;       // ...yo lo llamo "nombre". Es OTRO DTO (objeto anidado).

    @JsonProperty("capital")
    private List<String> capitales;  // array de strings → List<String>

    @JsonProperty("region")
    private String region;

    @JsonProperty("population")
    private Long poblacion;

    @JsonProperty("currencies")
    private Map<String, DetalleMoneda> monedas;
    // ↑ Map, porque la CLAVE es impredecible: "ARS" para Argentina, "PEN" para Perú.
    //   No podés declarar un atributo por cada moneda del mundo.

    @JsonProperty("cca2")
    private String cca2;             // código ISO de 2 letras: "AR"
}
```

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor
public class NombrePais {            // ← el molde del objeto "name" anidado
    @JsonProperty("common")
    private String comun;            // "Argentina"
    @JsonProperty("official")
    private String oficial;          // "Argentine Republic"
}
```

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor
public class DetalleMoneda {         // ← el molde del VALOR de cada moneda
    @JsonProperty("name")
    private String nombre;           // "Argentine peso"
    @JsonProperty("symbol")
    private String simbolo;          // "$"
}
```

> **`@JsonProperty` es el puente entre dos idiomas.** El JSON está en inglés (lo escribió quien hizo la API) y el código en castellano (lo escribimos nosotros). **Nadie los va a alinear por vos.** Si el nombre coincidiera, la anotación no haría falta.
>
> ⚠️ **`@JsonProperty` es de Jackson, no de Spring.** **Jackson** es la biblioteca estándar de Java para convertir JSON ↔ objetos — el `JSON.parse` de Java. Entra sola con el starter web, sin declararla en el `pom.xml`.

#### `// ¿CÓMO FUNCIONA?` — por qué Lombok acá es obligatorio 🔴

Jackson recibe un texto y tiene que devolverte un `Pais` lleno. Su algoritmo es:

```
 1. Crea el objeto VACÍO:        new Pais()          👈 necesita el CONSTRUCTOR SIN ARGUMENTOS
                                                        → lo da @NoArgsConstructor
 2. Recorre el JSON campo por campo y, por cada uno, llama al SETTER:
                                 pais.setNombre(...)  👈 necesita los SETTERS
                                 pais.setRegion(...)     → los da @Data
 3. Devuelve el objeto lleno.
```

**Por eso:**
- Sin **`@NoArgsConstructor`** → Jackson **no puede crear** el objeto. Excepción.
- Sin **`@Data`** (los setters) → Jackson crea el objeto pero **no puede llenarlo**. Todos los campos en `null`.
- **`@AllArgsConstructor`** Jackson **no la usa**: está para que *vos* puedas crear un `Pais` a mano.

> ⚠️ **La trampa de Java que hay debajo:** el lenguaje te regala un constructor vacío **solo si no declarás ningún constructor**. En cuanto `@AllArgsConstructor` declara uno con parámetros, **el vacío desaparece**. Por eso `@NoArgsConstructor` no es un lujo: **está reponiendo el constructor que el otro te robó**.

#### ③ El uso: el test

```java
package ar.edu.utn.ba.ddsi.countries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// ↑ "Antes de correr estos tests, levantá TODO Spring."
//   Sin esto, no hay quién cree los objetos y nada funciona.
class BuscadorDePaisesIT {

    @Autowired
    // ↑ "Spring: meteme acá el componente que vos creaste."
    //   ⚠️ Nunca hacemos `new BuscadorDePaises(...)`. Lo instancia el framework.
    private BuscadorDePaises buscadorDePaises;

    @Test
    void buscarPorNombrePeruDevuelvePeru() {
        var opt = buscadorDePaises.buscarPorNombre("peru");
        //  ↑ `var`: el compilador infiere el tipo (es Optional<Pais>).
        //    ⚠️ NO es el `let` de JavaScript: Java sigue siendo tipado, solo que inferido.

        assertThat(opt).isPresent();                              // ¿vino algo?
        assertThat(opt.get().getNombre().getComun()).isEqualTo("Peru");
        assertThat(opt.get().getCca2()).isEqualTo("PE");
        //         └────┬───┘  └────┬────┘
        //              │           └── getter generado por @Data (no está escrito en el DTO)
        //              └── .get() sobre el Optional: solo seguro DESPUÉS de isPresent()
    }

    @Test
    void buscarPorCapitalBuenosAiresDevuelveArgentina() {
        var lista = buscadorDePaises.buscarPorCapital("buenos aires");
        //                                             └──────┬─────┘
        //                                    👆 CON ESPACIO. Ver sección ④: se escapa solo.
        assertThat(lista).isNotEmpty();
        assertThat(lista.getFirst().getCca2()).isEqualTo("AR");
        //               └────┬───┘
        //               👆 getFirst() es de JAVA 21. Antes se escribía lista.get(0).
    }
}
```

⚠️ **`assertThat(...)` es AssertJ, no JUnit.** Son cosas distintas y **conviven**: **JUnit es el motor** que corre los tests (entiende `@Test`); **AssertJ es una biblioteca de aserciones** más expresiva que se monta encima. Entra sola con el starter de test.

| Lo que querés verificar | JUnit 5 | AssertJ |
|---|---|---|
| Son iguales | `assertEquals("Peru", x)` | `assertThat(x).isEqualTo("Peru")` |
| No está vacía | `assertFalse(l.isEmpty())` | `assertThat(l).isNotEmpty()` |
| Contiene X | *(loop a mano)* | `assertThat(l).contains(x)` |

#### ④ La conexión: la llamada HTTP

Primero, **la URL no se hardcodea**. Vive en el archivo de configuración:

```yaml
# src/main/resources/application.yml
rest-countries:
    base-url: https://restcountries.com/v3.1
```

**¿Por qué?** Porque si mañana cambia, **la cambiás en un archivo de texto y no recompilás nada**. Es el mismo principio que el `.env` de Node.

Y una clase la lee:

```java
@ConfigurationProperties(prefix = "rest-countries")
// ↑ "Spring: andá al application.yml, buscá el bloque 'rest-countries', y volcalo acá."
@Data   // ← ⚠️ por los SETTERS: Spring llena esta clase igual que Jackson llena un DTO
public class RestCountriesProperties {
    private String baseUrl;
    // ⚠️ En el yml es "base-url" y acá "baseUrl". Y funciona:
    //    Spring normaliza los nombres (saca guiones) antes de comparar.
}
```

*(Esta clase hay que habilitarla en la clase principal, con `@EnableConfigurationProperties(RestCountriesProperties.class)`. Sin eso, Spring no la crea.)*

**Y ahora sí, el método completo:**

```java
@Component
public class BuscadorDePaises {

    private static final String CAMPOS =
            "name,capital,region,subregion,population,currencies,languages,area,cca2,cca3";
    // ↑ Los campos que le pedimos a la API. ⚠️ Son EXACTAMENTE los atributos del DTO.
    //   Recordá: esta API EXIGE el query param `fields` — sin él devuelve 400.

    private final RestTemplate restTemplate;              // el cliente HTTP
    private final RestCountriesProperties propiedades;    // la config del yml

    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }
    // ↑ El constructor DECLARA lo que la clase necesita — y no lo construye.
    //   Spring lee esta firma, busca esos objetos, y se los pasa. Se llama
    //   INYECCIÓN DE DEPENDENCIAS. (El porqué: en el recorrido de esta unidad.)

    public List<Pais> buscarTodos() {

        URI uri = UriComponentsBuilder
                .fromUriString(propiedades.getBaseUrl())   // "https://restcountries.com/v3.1"
                .path("/all")                              // → .../v3.1/all
                .queryParam("fields", CAMPOS)              // → ...?fields=name,capital,...
                .build()                                   // sin placeholders → build() a secas
                .toUri();
        // ↑ ⚠️ ¿Por qué no concatenar strings con "+"?
        //   Porque los ESPACIOS y los caracteres especiales rompen la URL.
        //   UriComponentsBuilder arma la URL por partes y ESCAPA los valores.

        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        // ↑ 👈 LA LÍNEA. Acá pasa TODO, en una:
        //     1. abre la conexión y manda el GET
        //     2. recibe el JSON como texto
        //     3. se lo pasa a Jackson, que lo convierte en objetos usando los DTOs
        //     4. te devuelve un array tipado
        //   Es el equivalente de:  const data = await (await fetch(url)).json();
        //
        //   `Pais[].class` = "convertilo a este tipo". ⚠️ Se pide un ARRAY y no un List
        //   por una limitación de Java (los genéricos se borran al compilar).
        //   Resultado esperado: cuerpo.length ≈ 250

        return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
        // ↑ a) Si no vino nada → devuelvo LISTA VACÍA, nunca null.
        //      ⚠️ Devolver null obliga a quien te llama a defenderse en cada uso.
        //   b) Arrays.asList(...) convierte el array primitivo en List.
    }

    public Optional<Pais> buscarPorNombre(String nombre) {

        URI uri = UriComponentsBuilder
                .fromUriString(propiedades.getBaseUrl())
                .path("/name/{nombre}")                    // 👈 PLACEHOLDER (path param)
                .queryParam("fields", CAMPOS)              //    y el query param
                .buildAndExpand(nombre)                    // 👈 rellena {nombre} Y LO ESCAPA
                .toUri();                                  //    "buenos aires" → "buenos%20aires"

        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        // ⚠️ La API devuelve un ARRAY aunque busques uno solo. Siempre.

        if (cuerpo == null || cuerpo.length == 0) {
            return Optional.empty();                       // "no hay país" — explícito
        }
        return Optional.of(cuerpo[0]);                     // me quedo con el primero
    }

    // buscarPorMoneda / buscarPorRegion / buscarPorCapital:
    //   IDÉNTICOS. Lo único que cambia es la ruta del .path():
    //     .path("/currency/{codigo}")  ·  .path("/region/{region}")  ·  .path("/capital/{capital}")
}
```

🔴 **`build()` vs `buildAndExpand()` — no los confundas:**

| | Cuándo |
|---|---|
| **`build()`** | La ruta es **fija**, sin placeholders. → `buscarTodos()` |
| **`buildAndExpand(valor)`** | La ruta tiene **placeholders** `{...}`. Los rellena **y los escapa**. → los otros cuatro |

**Y ahí está el path param vs query param de la sección 3, ahora en código:** `.path("/name/{nombre}")` es el path param; `.queryParam("fields", CAMPOS)` es el query param.

> **Falta una pieza que el código usa y no se ve acá:** `RestTemplate` no se crea con `new`. Se declara en una clase de configuración aparte (`@Configuration` + `@Bean`) porque es una clase **de Spring**, no tuya, y no podés anotarla. Son 8 líneas y están explicadas en el **recorrido de esta unidad**.

### 4.4 El bug del profe — y la lección 🔴

Escobar corrió el test y **falló**: `expecting not blank but was null`. Los 250 países habían llegado, pero `getNombre().getComun()` devolvía `null`.

**La causa:** había escrito `@JsonProperty("comon")` — con una sola M. El JSON trae `"common"`.

**¿Y por qué el programa no explotó?** Por `@JsonIgnoreProperties(ignoreUnknown = true)`. Jackson buscó un campo `comon`, no lo encontró, **y no se quejó**: dejó el atributo en `null` y siguió.

> 🔴 **El trade-off que hay que entender:**
>
> | | Con `ignoreUnknown = true` | Sin él |
> |---|---|---|
> | La API agrega un campo | ✅ Sigue andando | 💥 Explota en producción |
> | Escribís mal un `@JsonProperty` | ⚠️ **`null` silencioso** | 💥 Explota al toque |
>
> **El mismo chaleco antibalas que te protege de la API te esconde tus propios typos.** Todo el mundo elige `ignoreUnknown = true` igual — pero pagás con este bug.
>
> **📌 Grabate el síntoma:** *un campo del DTO viene en `null` y todos los demás bien* → **es casi siempre un `@JsonProperty` mal escrito.** No es la red, no es Spring: es una letra.

**Y fijate cómo lo debuggeó, porque el método es transferible:** no se puso a leer el código buscando el error. Hizo un `System.out.println` de la lista y **miró qué había llegado realmente**. Vio los objetos con `common: null` y `official` lleno → supo al toque que el problema no era la conexión (los datos llegaban) sino **el mapeo de ese campo puntual**. **Primero determinás dónde está el problema; después leés esa zona.**

---

## 5. Información operativa 🔴

Todo lo que se dijo en clase sobre la cursada y el TPA.

### 5.1 Herramientas

| Herramienta | Notas |
|---|---|
| **IntelliJ IDEA** | El IDE de la cursada. La versión **Community (gratuita) alcanza**; con la cuenta estudiantil sacás la Pro por un año. ⚠️ **Consume bastante RAM**; si tenés poca, cerrá pestañas. Trae **guardado automático** activado por defecto (se puede desactivar). |
| **Postman** | Cliente HTTP. Lo instalás en tu máquina. |
| **Maven** | Gestor de dependencias (el `pom.xml`). |
| **Git / GitHub** | Podés usar la cuenta que quieras, pero **anotá cuál usás en el Excel del equipo**. Los repos los da la cátedra. |
| **Discord** | Canal de dudas. Ver 5.3. |
| **Drive del curso** | Ahí se sube **todo**: grabaciones, presentaciones y material, en carpetas por clase (`clase 01`, `clase 02`…). El link está en el aula virtual. |

⚠️ **Visual Studio Code para Java: la cátedra NO lo recomienda.** *"Queda muy corto."* Y sobre las herramientas agénticas: *"no lo van a poder resolver todo con IA"*.

⚠️ **El plugin de estilos de IntelliJ ya no se usa.** Si viste referencias a él en material de años anteriores, ignoralas.

### 5.2 TPA — la Entrega 1 🔴

**Contexto del TPA:** está basado en **UTN Solidaria**, una iniciativa real de la Subsecretaría de Asuntos Estudiantiles (reparto de comida, campañas de donaciones). La cátedra la eligió a propósito: *"siempre queremos que el TP tenga un impacto social"*.

**Estructura:** **6 entregas** en total, y **tutores personales asignados por equipo**.

**Qué pide la Entrega 1:**

| Entregable | Detalle |
|---|---|
| **Diagramas de clases por servicio** | **Dos**: servicio de **donaciones** y servicio de **notificaciones**. ⚠️ **Solo la funcionalidad que pide el enunciado**, no todo el sistema. |
| **Diagrama de arquitectura** | Ya viene dado en el enunciado. Se trata de **demostrarle al tutor que la entendieron**. |
| **Justificaciones** | 🔴 *"Esto es muy importante."* Le muestran al ayudante que entienden lo que están haciendo. |
| **Diagrama general de casos de uso** | Para identificar **qué tiene que hacer el sistema**. |
| **Implementación de los requerimientos** | Código. |
| **Bocetos de interfaz** | ~20, uno por requerimiento. **A mano, en papel o en Paint. Desprolijos está bien.** No es HTML, no es Figma. Son **descartables**: sirven para imaginar cómo el usuario interactúa con el sistema. |
| **Un endpoint web simple** | En el servicio de donaciones: un `GET` que devuelva *"Hola desde el servicio"*. ⚠️ Es **exponer**, no consumir — se ve en la clase 3. |
| **Pregunta de discusión** | **Defensa individual**: *"todos los integrantes tienen que poder sostener una conversación sobre esta pregunta"*. |

🔴 **El orden recomendado por la cátedra — y no lo digas después que no te avisaron:**

```
   1. BOCETOS de interfaz          ← "recontra recomendado"
   2. DIAGRAMA DE CASOS DE USO     ← "esto lo pueden hacer YA, no hace falta que les enseñemos nada"
   3. DIAGRAMA DE CLASES
   4. CÓDIGO                       ← ÚLTIMO. "No se tiren de lleno al código o lo van a tener que rehacer."
```

⚠️ **El modelado se piensa entre todos.** *"La parte de pensar, la parte de modelado, me gustaría que la hagan entre todos. Después se dividen el código como quieran."* Y: *"traten de asistirse lo menos posible, que no les robe esa oportunidad de pensamiento."*

### 5.3 Canales de consulta 🔴

**Dos canales, y no se mezclan:**

| Canal | Para qué | Ejemplo |
|---|---|---|
| **Foro de consultas general** (Discord) | **Solo dudas de enunciado.** Qué hay que hacer, qué te están pidiendo. | *"No entiendo qué implica el requerimiento 7."* |
| **Tu tutor/ayudante personal** | **Implementación y decisiones de diseño.** Es tu checkpoint. | *"Encaramos esto de tal forma, ¿qué opinan?"* |

⚠️ **En el canal general NO responden** preguntas del tipo *"esto lo hice así, ¿está bien?"*. Eso es del tutor.

**Los tutores los evalúan a ustedes.** *"Llévense bien"* — dicho medio en broma, pero: **el tutor es quien aprueba tu entrega.**

---

## 📌 Para el parcial, si te preguntan

**"¿Qué diferencia hay entre un path param y un query param?"**
El **path param** identifica un recurso y forma parte de la ruta (`/name/argentina`); el **query param** filtra o modifica la consulta y va después del signo de pregunta, con formato clave=valor (`?fields=name,capital`), encadenándose con `&`. El primero responde a *cuál* recurso pido; el segundo, a *cómo* lo quiero.

**"¿Qué hace el navegador cuando escribís una URL en la barra y apretás Enter?"**
Envía una solicitud HTTP de tipo GET al servidor correspondiente. Es la única operación que permite la barra de direcciones: para enviar un POST, PUT, PATCH o DELETE hace falta un formulario HTML, código JavaScript, o un cliente HTTP como Postman.

**"¿Qué es un DTO y por qué hace falta en Java y no en JavaScript?"**
Un DTO (*Data Transfer Object*) es una clase sin lógica cuyo único fin es transportar datos; en el consumo de una API REST actúa como molde de la respuesta. Hace falta en Java porque es un lenguaje de tipado estático: el compilador necesita conocer la estructura del JSON antes de ejecutar. JavaScript, de tipado dinámico, construye el objeto en tiempo de ejecución con la forma que tenga el dato.

**"¿Qué archivos de un proyecto Java no se suben al repositorio y por qué?"**
La carpeta `.idea/`, porque contiene configuración local del IDE que genera conflictos entre integrantes con distintas versiones o preferencias; y la carpeta `target/`, porque contiene código compilado que se regenera automáticamente. Ambas se agregan al `.gitignore`.

---

## ✅ Checkpoint — Parte 4

1. ¿Qué método HTTP usa el navegador cuando escribís una URL y apretás Enter? ¿Podés hacer un POST desde la barra? ¿Por qué?
2. ¿Qué es Postman y qué limitación del navegador viene a resolver?
3. Diferencia entre path param y query param. Dame el ejemplo de cada uno en la API de países.
4. Quitás el `fields` de `/all` y la API te devuelve `400`. ¿Qué te dice eso, y qué lección general se saca?
5. ¿Qué es un framework y en qué se diferencia de una biblioteca?
6. ¿Qué carpetas del proyecto **no** van al repositorio? ¿Por qué cada una?
7. ¿Qué es un DTO? ¿Por qué en Java hace falta y en JavaScript no?
8. ¿Por qué `Pais` necesita `@NoArgsConstructor` **y** `@Data`? Explicá el algoritmo de Jackson.
9. ¿Para qué sirve `@JsonProperty`? ¿Cuándo **no** haría falta?
10. ¿Qué hace `@JsonIgnoreProperties(ignoreUnknown = true)`? ¿Qué te protege y qué te esconde?
11. ¿Por qué `buscarPorNombre` devuelve `Optional<Pais>` y `buscarPorMoneda` devuelve `List<Pais>`?
12. ¿Por qué no se concatena la URL con `+`? ¿Cuál es la diferencia entre `build()` y `buildAndExpand()`?
13. ¿Por qué la URL base vive en el `application.yml` y no en el código?
14. Un campo de tu DTO viene siempre en `null` y todos los demás llegan bien. ¿Cuál es la causa más probable?
15. **TPA:** ¿en qué orden recomienda la cátedra encarar la Entrega 1? ¿Cuál es el último paso?
16. **TPA:** ¿qué se pregunta en el foro general y qué se le pregunta al tutor personal?

---

## 🏁 Cierre de la clase 02

En una sola clase se armó **la columna vertebral de la materia**:

**Cliente-Servidor** (el patrón) → **HTTP** (el idioma) → **Arquitectura Web** (los dos, andando) → **API** (la interfaz) → **API REST** (el estándar) → **y el código que lo consume**.

**Lo que hay que llevarse, si tuvieras que quedarte con tres cosas:**

1. **HTTP es *stateless*.** Cada solicitud lleva toda la información. De ahí sale la mitad de lo que viene después.
2. **REST no es una tecnología: es un acuerdo.** Verbos, URIs, códigos de estado, JSON. Y **tiene un costo** (el N+1) que hay que saber justificar.
3. **La materia es de aplicación.** No te van a pedir definiciones: te van a pedir que **decidas y justifiques**. *"Pongo esto acá, porque priorizo tal atributo de calidad, y estoy pagando con tal otro."*

**Lo que viene:** la clase 3 arranca con **modelado de dominio en objetos**, patrones de diseño, y el primer ejercicio de SmartLife. Y en la clase 4, la vuelta de tuerca: **exponer** tus propias APIs REST desde Spring Boot.

---

**FIN DE LA PARTE 4 — FIN DEL APUNTE MAESTRO DE LA CLASE 02**
