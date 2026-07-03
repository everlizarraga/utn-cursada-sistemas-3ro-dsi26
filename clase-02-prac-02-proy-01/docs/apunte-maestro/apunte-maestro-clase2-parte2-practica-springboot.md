# 📘 Apunte Maestro — Clase 2 — Parte 2: Consumir API REST desde Java con Spring Boot

**Materia:** Diseño de Sistemas de Información (DSI) — UTN FRBA  
**Clase:** 2 — 01/04/2026 (virtual)  
**Fuente:** Transcripción de clase + código del repo `rest-paises-main`  

---

## Contexto

En esta parte de la clase, el profe muestra cómo consumir una API REST pública (restcountries.com) desde un proyecto Java con Spring Boot. Es la primera vez que se ve Spring Boot en la cursada — el framework que se usa TODO el año para el TPA. El profe fue explícito: "no hace falta que entiendan todo hoy, hay magia de Spring Boot por atrás que vamos a ir explicando clase a clase."

Importante: el profe creó el proyecto en vivo paso a paso, pero al final les compartió el repo completo con más funcionalidad. El repo es el que tenés descargado como `rest-paises-main.zip`.

---

## 🔴 Spring Boot — Primera aproximación

### Qué es

Framework para Java que simplifica la creación de aplicaciones. Lo van a usar todo el año en el TPA y en las prácticas de clase. Es el framework más usado en la industria para backend en Java.

### Crear un proyecto Spring Boot

Se usa **Spring Initializr** (https://start.spring.io):

1. Elegir: **Java** + **Maven** + Spring Boot **3.4.5** (o la versión actual)
2. Group: `ar.edu.utn.ba.ddsi` (convención: dominio invertido de la organización)
3. Artifact: nombre del proyecto (ej: `countries`)
4. Java: **21**
5. Agregar dependencias: **Spring Web**, **Lombok**, **Configuration Processor**, **DevTools**
6. Click **Generate** → descarga un .zip → descomprimir → abrir en IntelliJ

### Estructura del proyecto

```
proyecto/
├── .idea/              ← ⚠️ NO se sube al repo (agregar a .gitignore)
├── .mvn/               ← Sí se sube
├── src/
│   ├── main/
│   │   ├── java/ar/edu/utn/ba/ddsi/countries/
│   │   │   ├── CountriesApplication.java    ← Punto de entrada (main)
│   │   │   ├── config/                       ← Clases de configuración
│   │   │   └── services/                     ← Lógica de negocio
│   │   │       ├── BuscadorDePaises.java
│   │   │       └── dto/                      ← Clases molde (Data Transfer Objects)
│   │   │           ├── Pais.java
│   │   │           └── NombrePais.java
│   │   └── resources/
│   │       └── application.yml               ← Configuración del proyecto
│   └── test/                                 ← Tests
├── target/             ← ⚠️ NO se sube al repo (agregar a .gitignore)
└── pom.xml             ← Sí se sube
```

**Lo que el profe enfatizó que NO se sube al repo:**
- `.idea/` — configuración local de IntelliJ (genera conflictos entre miembros del equipo)
- `target/` — código compilado (se regenera con `mvn install`)

### El punto de entrada — `CountriesApplication.java`

```java
@SpringBootApplication
@EnableConfigurationProperties(RestCountriesProperties.class)
public class CountriesApplication {
    public static void main(String[] args) {
        SpringApplication.run(CountriesApplication.class, args);
    }
}
```

Todo programa Java tiene un `main`. En Spring Boot, el main arranca el framework que se encarga de todo lo demás. La annotation `@SpringBootApplication` le dice a Spring que tome el control.

---

## 🔴 DTOs — Clases molde para mapear el JSON

Cuando consumís una API REST, la respuesta viene en JSON. Necesitás clases Java que "macheen" con la estructura del JSON para que Spring pueda convertir automáticamente el JSON en objetos Java.

### Ejemplo: la API devuelve esto (JSON)

```json
{
  "name": {
    "common": "Argentina",
    "official": "Argentine Republic"
  },
  "capital": ["Buenos Aires"],
  "region": "Americas",
  "population": 45376763
}
```

### Las clases Java que modelan eso

```java
@JsonIgnoreProperties(ignoreUnknown = true)   // Ignorar campos del JSON que no modelé
@Data                                          // Lombok: genera getters, setters, equals, hashCode, toString
@NoArgsConstructor                             // Lombok: constructor vacío
@AllArgsConstructor                            // Lombok: constructor con todos los parámetros
public class Pais {

    @JsonProperty("name")         // "name" del JSON → atributo "nombre" en Java
    private NombrePais nombre;

    @JsonProperty("capital")
    private List<String> capitales;

    @JsonProperty("region")
    private String region;

    @JsonProperty("population")
    private Long poblacion;
}
```

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NombrePais {

    @JsonProperty("common")       // "common" del JSON → atributo "comun" en Java
    private String comun;

    @JsonProperty("official")
    private String oficial;
}
```

### Puntos clave de los DTOs

**`@JsonProperty("name")`** — Mapea un nombre de campo del JSON a un nombre de atributo diferente en Java. Si tu atributo se llama igual que el campo del JSON, no hace falta ponerlo.

**`@JsonIgnoreProperties(ignoreUnknown = true)`** — Le dice a Jackson (la biblioteca de serialización): "si el JSON trae campos que yo no modelé, ignóralos en vez de lanzar error." Fundamental porque las APIs suelen devolver muchos más campos de los que necesitás.

**`@Data`** de Lombok — Genera getters, setters, `equals()`, `hashCode()` y `toString()` para todos los atributos. No los ves en el código, pero existen en tiempo de compilación.

**`@NoArgsConstructor` + `@AllArgsConstructor`** — Generan dos constructores: uno vacío y uno con todos los parámetros. Necesarios para que Jackson pueda instanciar la clase al deserializar el JSON.

> **Conexión con lo que ya sabés:** Los DTOs son el equivalente a las interfaces/types que definías en TypeScript para tipar las respuestas de `fetch()`. En TS hacías `const data: PaisResponse = await response.json()`. En Java es más formal (clases con annotations), pero el concepto es el mismo: definir la forma de los datos que esperás.

---

## 🟡 Configuración — `application.yml`

En vez de hardcodear la URL de la API en el código, se pone en un archivo de configuración:

```yaml
rest-countries:
    base-url: https://restcountries.com/v3.1
```

Y se lee con una clase de configuración:

```java
@ConfigurationProperties(prefix = "rest-countries")
@Data
public class RestCountriesProperties {
    private String baseUrl;    // Machea con "base-url" del YAML
}
```

Spring Boot lee el archivo `application.yml`, encuentra el prefijo `rest-countries`, y setea automáticamente el atributo `baseUrl` con el valor `https://restcountries.com/v3.1`.

Hay que habilitar esto en la clase principal con `@EnableConfigurationProperties(RestCountriesProperties.class)`.

**¿Por qué no hardcodear?** Porque si la URL cambia, solo cambiás el archivo de configuración sin tocar código. Es una buena práctica estándar.

---

## 🔴 El servicio — `BuscadorDePaises.java`

Esta es la clase que realmente consume la API. Es un **componente** de Spring Boot.

```java
@Component                    // Le dice a Spring: "esta clase es un componente, administrala vos"
public class BuscadorDePaises {

    private final RestTemplate restTemplate;                // Cliente HTTP
    private final RestCountriesProperties propiedades;      // Configuración (URL base)

    // Constructor — Spring inyecta los parámetros automáticamente
    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }

    public List<Pais> buscarTodos() {
        // 1. Construir la URI
        URI uri = UriComponentsBuilder
            .fromUriString(propiedades.getBaseUrl())   // "https://restcountries.com/v3.1"
            .path("/all")                               // + "/all"
            .queryParam("fields", "name,capital,region") // + "?fields=name,capital,region"
            .build()
            .toUri();

        // 2. Hacer el GET y mapear la respuesta a un array de Pais
        Pais[] paises = restTemplate.getForObject(uri, Pais[].class);

        // 3. Convertir array a List (manejar null)
        return paises == null ? List.of() : Arrays.asList(paises);
    }

    public Optional<Pais> buscarPorNombre(String nombre) {
        URI uri = UriComponentsBuilder
            .fromUriString(propiedades.getBaseUrl())
            .path("/name/{nombre}")                    // Path variable
            .queryParam("fields", "name,capital,region")
            .buildAndExpand(nombre)                    // Reemplaza {nombre} por el valor
            .toUri();

        Pais[] paises = restTemplate.getForObject(uri, Pais[].class);

        if (paises == null || paises.length == 0) {
            return Optional.empty();                   // No encontró nada
        }
        return Optional.of(paises[0]);                 // Devuelve el primero
    }
}
```

### Desglose de conceptos nuevos

**`@Component`** — Le dice a Spring: "esta clase la administrás vos. Vos la instanciás, vos la inyectás donde se necesite." Nosotros nunca hacemos `new BuscadorDePaises()` — Spring lo hace por nosotros.

**`RestTemplate`** — Cliente HTTP de Spring. Es el componente que realmente hace la request HTTP por nosotros. `restTemplate.getForObject(uri, Pais[].class)` hace un GET a esa URI y mapea la respuesta JSON a un array de `Pais`.

**`UriComponentsBuilder`** — Clase utilitaria para construir URIs de forma segura (en vez de concatenar strings). Maneja encoding de caracteres especiales, query params, path variables, etc.

**`Optional<Pais>`** — Ya lo vimos: un contenedor que puede tener un valor o estar vacío. Si la búsqueda no encuentra nada, devuelve `Optional.empty()` en vez de `null`.

### Inyección de Dependencias (intro)

El profe lo mencionó sin profundizar ("hoy no es la clase de inyección de dependencias"). Lo básico: cuando una clase anotada con `@Component` tiene un constructor con parámetros, Spring busca esos objetos en su contexto y se los pasa automáticamente. No los creamos nosotros.

```java
// NO hacemos esto nunca:
BuscadorDePaises buscador = new BuscadorDePaises(restTemplate, propiedades);

// Spring lo hace por nosotros cuando ponemos @Component en la clase
// y @Autowired o un constructor donde lo necesitamos
```

> **El profe fue claro:** "esto vamos a profundizarlo en clases siguientes. Hoy solo sepan que Spring se encarga de crear e inyectar los objetos."

---

## 🟡 Tests de integración con `@SpringBootTest`

```java
@SpringBootTest                                    // Spring Boot toma el control del test
class BuscadorDePaisesIT {

    @Autowired                                     // Spring inyecta el buscador automáticamente
    private BuscadorDePaises buscadorDePaises;

    @Test
    void buscarTodosDevuelveVariosPaises() {
        var lista = buscadorDePaises.buscarTodos();

        assertThat(lista).isNotEmpty();
        assertThat(lista.getFirst().getNombre().getComun()).isNotBlank();
    }

    @Test
    void buscarPorNombrePeruDevuelvePeru() {
        var opt = buscadorDePaises.buscarPorNombre("peru");

        assertThat(opt).isPresent();
        assertThat(opt.get().getNombre().getComun()).isEqualTo("Peru");
    }
}
```

**`@SpringBootTest`** — Le dice a JUnit que arranque Spring Boot completo antes de ejecutar los tests. Sin esto, `@Autowired` no funciona.

**`@Autowired`** — "Spring, dame una instancia de esta clase." Spring busca la clase anotada con `@Component`, la instancia y la setea acá.

**`var`** — Inferencia de tipos de Java (desde Java 10). `var lista = buscadorDePaises.buscarTodos()` es equivalente a `List<Pais> lista = buscadorDePaises.buscarTodos()`. Solo funciona para variables locales.

**`assertThat`** — La clase usa AssertJ (más expresivo que `Assertions` de JUnit). Permite encadenar: `assertThat(lista).isNotEmpty()`, `assertThat(opt).isPresent()`, etc.

> **Diferencia con los tests del material previo:** en el video de testing usamos `Assertions.assertEquals()` de JUnit puro. Acá se usa `assertThat()` de AssertJ, que es más legible. Ambos son válidos — la cátedra acepta cualquiera de los dos.

---

## 🟡 Postman — Cliente HTTP para probar APIs

El profe mostró Postman brevemente como herramienta para probar endpoints sin escribir código. Es como un "navegador para APIs":

1. Elegís el método (GET, POST, PUT, DELETE)
2. Ponés la URL del endpoint
3. Si necesitás, agregás headers y body
4. Click en Send → ves la respuesta con status code, headers y body

Útil para: probar APIs antes de consumirlas desde código, debuggear cuando algo no funciona, explorar APIs nuevas.

> **Conexión con lo que ya sabés:** Cuando hacías `fetch()` en tus proyectos de JavaScript, Postman hace lo mismo pero con interfaz gráfica. En tu proyecto Recipe Finder consumías TMDb API desde el navegador con Fetch — Postman te deja hacer eso sin escribir código.

---

## 🟢 Resumen del flujo completo

```
1. API externa (restcountries.com)
   Expone endpoints REST que devuelven JSON

2. application.yml
   Tiene la URL base configurada

3. RestCountriesProperties
   Lee la configuración del YAML

4. BuscadorDePaises (@Component)
   Usa RestTemplate para hacer GET a la API
   UriComponentsBuilder construye la URL
   La respuesta JSON se mapea a DTOs (Pais, NombrePais)

5. Test (@SpringBootTest)
   Spring inyecta el BuscadorDePaises con @Autowired
   Se verifican los resultados con assertThat
```

---

## Checkpoint

1. ¿Qué es Spring Boot y para qué se usa en la materia?
2. ¿Qué hace la annotation `@Component` en una clase?
3. ¿Qué es un DTO y para qué sirve `@JsonProperty`?
4. ¿Qué hace `@JsonIgnoreProperties(ignoreUnknown = true)`?
5. ¿Qué es `RestTemplate` y qué hace `getForObject()`?
6. ¿Qué diferencia hay entre `@SpringBootTest` y `@Test` solo?
7. ¿Qué hace `@Autowired` y quién se encarga de instanciar el objeto?
8. ¿Por qué se pone la URL de la API en `application.yml` en vez de hardcodearla?

---

**Fin de la Parte 2 — Clase 2**
