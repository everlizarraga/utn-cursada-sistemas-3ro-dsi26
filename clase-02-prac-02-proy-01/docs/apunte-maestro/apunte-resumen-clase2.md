# 📘 Apunte Resumen — Clase 2

**Materia:** Diseño de Sistemas de Información (DSI) — UTN FRBA
**Clase:** 2 — 01/04/2026 (virtual)
**Cubre:** Parte 1 (teoría: Cliente-Servidor, HTTP, API REST) + Parte 2 (práctica: Spring Boot) + Parte 3 (TPA DonaTrack — Entrega 1)

---

# 📕 PARTE 1 — Cliente-Servidor, HTTP y API REST

## 🔴 Patrón Cliente-Servidor 📘

Patrón arquitectural con dos componentes: un **servidor** que provee servicios y un **cliente** que los consume.

**Reglas fundamentales:**
- El **cliente siempre inicia** la comunicación (nunca el servidor en la versión clásica).
- El servidor **escucha en un puerto** esperando solicitudes.
- Ciclo: **Solicitud → Procesamiento → Respuesta**.
- Un servidor puede atender múltiples clientes simultáneamente.

**Ejemplos:** navegador↔servidor web (HTTP), app de banking↔backend (HTTP), Outlook↔servidor SMTP/IMAP, app mobile↔backend, juego online↔servidor del juego.

> **Punto clave del profe:** clientes y servidores pueden ser muy distintos, pero el rol es el mismo: cliente inicia, servidor provee. El protocolo varía (HTTP, SMTP, etc.), el patrón no.

### 🟡 Clasificación según responsabilidades 📘

| Clasificación | Dónde está la lógica | Ejemplo |
|---|---|---|
| **Cliente Activo, Servidor Pasivo** | Lógica en el cliente, servidor solo persiste | Sistema desktop con BD central |
| **Cliente Pasivo, Servidor Pasivo** | Ambos con baja lógica | Componente de integración |
| **Cliente Pasivo, Servidor Activo** ("Cliente liviano") | Lógica en el servidor, cliente presenta | Web tradicional (servidor genera HTML) |
| **Cliente Activo, Servidor Activo** ("Cliente pesado") | Lógica distribuida | SPA con React/Angular + backend |

Lo importante: **dónde pongas la lógica tiene consecuencias** (procesamiento, batería, memoria, mantenibilidad). No es arbitrario.

> **Para el parcial, si te preguntan:** *"¿Qué diferencia hay entre cliente liviano y cliente pesado?"* — En un cliente liviano, el servidor posee la mayor parte de la lógica de negocio y genera la vista; el cliente se limita a presentar los datos. En un cliente pesado, la lógica de negocio está distribuida entre ambos componentes — el cliente tiene lógica de presentación, interacción y parte de la lógica de negocio. Tecnologías como React o Angular implementan clientes pesados.

### Ventajas y desventajas del Cliente Pasivo - Servidor Activo (liviano) 📘

**Ventajas:**
- **Mantenibilidad:** cambios de lógica en un solo lugar (servidor). Actualizo servidor → todos los clientes tienen la nueva versión.
- **Seguridad:** control de acceso centralizado.

**Desventajas** (con un único servidor):
- **Eficiencia:** cuello de botella, el tiempo de respuesta crece con la concurrencia.
- **Disponibilidad:** único punto de falla.

**Mitigación:** más servidores + balanceo de carga. Trade-off: gano eficiencia/disponibilidad pero subo costo y complejidad.

> **Concepto clave:** el único punto de falla se puede **mover pero no eliminar fácilmente**. Si pongo dos servidores de app pero uno de BD, moví el punto de falla a la BD. "Es como ponerle rejas a todas las ventanas menos a una."

> **Para el parcial, si te preguntan:** *"¿Qué ventajas y desventajas tiene un cliente liviano con servidor activo?"* — Ventajas: mantenibilidad (cambios centralizados en el servidor) y seguridad (control de acceso centralizado). Desventajas (con un solo servidor): eficiencia (cuello de botella ante alta concurrencia) y disponibilidad (único punto de falla). Estas desventajas se mitigan agregando más servidores, pero con impacto en costo y complejidad.

---

## 🔴 Protocolo HTTP 🎯

**HyperText Transfer Protocol.** 40+ años. Originalmente para hipertexto, hoy soporta toda la web.

### Características clave 📘

| Característica | Qué significa |
|---|---|
| **Capa de aplicación** | Capa 7 OSI |
| **Sincrónico** | Solicitud → espero respuesta |
| **Sin estado (stateless)** | Cada solicitud es independiente; el servidor no recuerda solicitudes previas |
| **Solicitud/Respuesta** | Cliente pide, servidor responde |
| **Puerto** | 80 (HTTP) / 443 (HTTPS) |

**Sin estado:** si pido cliente ID 1, y después "facturas del último cliente que te pedí", el servidor no sabe de qué le hablo. Cada solicitud debe llevar TODA la info necesaria.

La cátedra trabaja con **HTTP 1.1**. HTTP/2 y HTTP/3 (streams bidireccionales, server push) se ven más adelante.

### 🔴 Estructura de una Request HTTP 🎯

```
1. LÍNEA DE INICIO: Método + URL + Versión
   GET /usuarios HTTP/1.1

2. HEADERS: Metadatos
   Host: api.miapp.com
   Content-Type: application/json
   Authorization: Bearer <token>

3. BODY: Datos — solo en POST, PUT, PATCH
   { "nombre": "Juan", "email": "juan@mail.com" }
```

**El body no viaja en GET ni DELETE.** Si necesitás enviar datos en un GET, van como query params: `/usuarios?nombre=Juan`.

### 🔴 Estructura de una Response HTTP 🎯

```
1. LÍNEA DE ESTADO: Versión + Código + Descripción
   HTTP/1.1 200 OK

2. HEADERS: Metadatos de la respuesta
   Content-Type: application/json
   Content-Length: 342

3. BODY: Los datos devueltos
   { "id": 1, "nombre": "Juan", "email": "juan@mail.com" }
```

### Métodos HTTP principales 🎯

| Método | Acción | Body | Ejemplo |
|---|---|---|---|
| **GET** | Obtener un recurso | No | `GET /users/1` |
| **POST** | Crear un recurso nuevo | Sí | `POST /users` + body |
| **PUT** | Actualizar recurso completo | Sí | `PUT /users/1` + body completo |
| **PATCH** | Actualizar recurso parcialmente | Sí | `PATCH /users/1` + body parcial |
| **DELETE** | Borrar un recurso | No | `DELETE /users/1` |

### 🔴 Códigos de Estado HTTP 🎯

Siempre los genera el servidor. Agrupados por familia:

| Familia | Significado | Ejemplos |
|---|---|---|
| **1xx** | Informativo | 100 Continue |
| **2xx** | Éxito | 200 OK, 201 Created, 204 No Content |
| **3xx** | Redirección | 301 Moved Permanently, 302 Found |
| **4xx** | Error del cliente | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 405 Method Not Allowed |
| **5xx** | Error del servidor | 500 Internal Server Error, 503 Service Unavailable, 504 Gateway Timeout |

**401 vs 403:**
- **401 Unauthorized:** no mandaste token o el token es inválido. "No sé quién sos."
- **403 Forbidden:** token válido, pero no tenés permisos. "Sé quién sos, pero no podés entrar."

**Sobre los 500:** en producción nunca se devuelve detalle del error al cliente — solo un 500 genérico. No querés dar info interna a un atacante.

> **Para el parcial, si te preguntan:** *"¿Qué significan los códigos de estado HTTP 2xx, 4xx y 5xx?"* — Los 2xx indican que la solicitud fue procesada correctamente (200 OK, 201 Created). Los 4xx indican errores causados por el cliente (404 Not Found, 401 Unauthorized). Los 5xx indican errores del lado del servidor (500 Internal Server Error). Los códigos siempre los genera el servidor como parte de la response.

### 🟡 HTTP vs HTTPS 📘

| | HTTP | HTTPS |
|---|---|---|
| **Puerto** | 80 | 443 |
| **Seguridad** | Sin cifrado | Cifrado TLS/SSL |
| **Certificado** | No requerido | Requerido (se renueva periódicamente) |
| **Uso** | Solo desarrollo local | Producción siempre |

**Importante:** HTTPS cifra los datos en tránsito (nadie puede leer lo que viaja). El token de autorización controla quién accede a qué. **Son dos capas distintas; en producción se usan juntas.**

---

## 🟡 Arquitectura Web — Qué pasa cuando entrás a una URL 📘

Paso a paso (demo del profe con Chrome F12 → Network):

1. Escribís `https://www.google.com` en el navegador.
2. **DNS** traduce dominio a IP (caché local o consulta a DNS).
3. Conexión **TCP** con el servidor (three-way handshake).
4. Negociación **TLS** (si es HTTPS).
5. Navegador envía **HTTP GET /**.
6. Servidor procesa y devuelve **HTML**.
7. Navegador descarga recursos adicionales: **CSS, JS, imágenes, fuentes**.
8. La página se **renderiza**.

> **Tip del profe:** la consola de desarrollador (F12) es "fundamental". Network = requests, Sources = recursos, Elements = DOM.

---

## 🔴 API y API REST

### Qué es una API 📘

**Application Programming Interface** — conjunto de herramientas, definiciones y protocolos para integrar aplicaciones. Permite que un componente se comunique con otro sin saber cómo funciona internamente.

**Importante:** API no siempre significa "servicio web remoto". Una API también puede ser una biblioteca local (Streams de Java, JPA, API Win32). El concepto es más amplio: la interfaz que un componente expone para que otros lo usen.

### Qué es REST 📘

**RE**presentational **S**tate **T**ransfer — un **estilo de diseño** de comunicación cliente-servidor. **No es** un estilo arquitectónico ni un patrón de diseño.

**REST establece:**
- Usa **protocolo HTTP**.
- Cada solicitud contiene **toda la información necesaria** (stateless).
- Usa los **verbos HTTP** (GET, POST, PUT, PATCH, DELETE) para las operaciones.
- Los recursos se identifican con **URIs** (cada recurso tiene una ruta única).
- Formato de intercambio: **JSON** (o XML; JSON es estándar actual).

### 🔴 Rutas REST y convenciones 🎯

```
GET    /users        → Obtener todos los usuarios         → 200 OK
GET    /users/1      → Obtener el usuario con ID 1        → 200 OK
POST   /users        → Crear un usuario nuevo             → 201 Created
PUT    /users/1      → Actualizar usuario completo        → 200 OK
DELETE /users/1      → Borrar usuario con ID 1            → 204 No Content
```

**Buenas prácticas:**
- Las rutas llevan **sustantivos** (recursos), no verbos. `GET /users` — no `GET /obtenerUsuarios`.
- El verbo HTTP indica la acción — no va en la ruta.
- Usar los **códigos de estado HTTP** estándar.
- **REST "a rajatabla":** no se mezclan recursos. Si pedís un usuario, no te devuelve objetos anidados de otras entidades. Necesitás el detalle relacionado → hacés otro request. En la práctica, a veces se relaja por pragmatismo.

### Endpoint 🎯

Una **ruta específica** de un servicio web a la que podés hacer requests. `GET /users` es un endpoint. `POST /users` es otro (misma ruta, distinto verbo).

### 🟢 JSON — Formato de intercambio 🎯

```json
{
  "id": 42,
  "nombre": "María García",
  "activo": true,
  "roles": ["admin", "usuario"],
  "direccion": {
    "ciudad": "Buenos Aires",
    "pais": "Argentina"
  }
}
```

Soporta strings, números, booleanos, arrays, objetos, null. **No soporta `undefined`** (eso es de JavaScript).

> **Para el parcial, si te preguntan:** *"¿Qué es REST y qué establece?"* — REST (Representational State Transfer) es un estilo de diseño de comunicación cliente-servidor. Establece: uso del protocolo HTTP, solicitudes stateless con toda la información necesaria, uso de verbos HTTP para las operaciones (GET, POST, PUT, DELETE), identificación de recursos mediante URIs, y JSON como formato de intercambio. Cada recurso tiene una ruta única y las operaciones se indican con el verbo HTTP, no en la ruta.

---

# 📕 PARTE 2 — Práctica: Consumir API REST desde Java con Spring Boot

## 🔴 Spring Boot — Primera aproximación 🎯

Framework Java que simplifica la creación de aplicaciones. Se usa **todo el año** en TPA y prácticas. Es el framework más usado en la industria para backend en Java.

### Crear un proyecto con Spring Initializr 🎯

(https://start.spring.io)

1. **Java** + **Maven** + Spring Boot **3.4.5**
2. **Group:** `ar.edu.utn.ba.ddsi` (dominio invertido de la organización)
3. **Artifact:** nombre del proyecto (ej: `countries`)
4. **Java 21**
5. **Dependencias:** Spring Web, Lombok, Configuration Processor, DevTools
6. Click **Generate** → .zip → descomprimir → abrir en IntelliJ

### Estructura del proyecto 🎯

```
proyecto/
├── .idea/              ← ⚠️ NO se sube al repo
├── .mvn/               ← Sí se sube
├── src/
│   ├── main/
│   │   ├── java/ar/edu/utn/ba/ddsi/countries/
│   │   │   ├── CountriesApplication.java    ← main
│   │   │   ├── config/                       ← Configuración
│   │   │   └── services/                     ← Lógica de negocio
│   │   │       ├── BuscadorDePaises.java
│   │   │       └── dto/                      ← Clases molde
│   │   │           ├── Pais.java
│   │   │           └── NombrePais.java
│   │   └── resources/
│   │       └── application.yml               ← Configuración
│   └── test/
├── target/             ← ⚠️ NO se sube al repo
└── pom.xml             ← Sí se sube
```

**NO se sube al repo:**
- `.idea/` — config local de IntelliJ (genera conflictos entre miembros del equipo).
- `target/` — código compilado (se regenera con `mvn install`).

### Punto de entrada — `CountriesApplication.java` 🎯

```java
@SpringBootApplication
@EnableConfigurationProperties(RestCountriesProperties.class)
public class CountriesApplication {
    public static void main(String[] args) {
        SpringApplication.run(CountriesApplication.class, args);
    }
}
```

`@SpringBootApplication` le dice a Spring que tome el control. El `main` arranca el framework, que se encarga del resto.

---

## 🔴 DTOs — Clases molde para mapear el JSON 🎯

Cuando consumís una API REST, la respuesta viene en JSON. Necesitás clases Java que "macheen" con la estructura del JSON para que Spring lo convierta automáticamente.

### Ejemplo — JSON que devuelve la API

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
@JsonIgnoreProperties(ignoreUnknown = true)   // Ignorar campos del JSON no modelados
@Data                                          // Lombok: getters, setters, equals, hashCode, toString
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

### Annotations clave 🎯

- **`@JsonProperty("name")`** — mapea un nombre de campo del JSON a un nombre distinto en Java. Si el atributo se llama igual, no hace falta.
- **`@JsonIgnoreProperties(ignoreUnknown = true)`** — Jackson ignora campos del JSON que no modelaste, en vez de tirar error. **Fundamental** porque las APIs devuelven más campos de los necesarios.
- **`@Data`** (Lombok) — genera getters, setters, `equals`, `hashCode`, `toString`. No los ves pero existen en compilación.
- **`@NoArgsConstructor` + `@AllArgsConstructor`** — constructores vacío y completo. Jackson los necesita para deserializar.

> **Conexión con lo que ya sabés:** Los DTOs son el equivalente a las interfaces/types que definías en TypeScript para tipar respuestas de `fetch()`. En TS: `const data: PaisResponse = await response.json()`. En Java es más formal (clases con annotations), pero el concepto es el mismo.

---

## 🟡 Configuración — `application.yml` 🎯

URL de la API en archivo de configuración, no hardcodeada:

```yaml
rest-countries:
    base-url: https://restcountries.com/v3.1
```

Clase que lee la configuración:

```java
@ConfigurationProperties(prefix = "rest-countries")
@Data
public class RestCountriesProperties {
    private String baseUrl;    // Machea con "base-url" del YAML
}
```

Spring lee el YAML, encuentra el prefijo `rest-countries`, setea automáticamente `baseUrl` con `https://restcountries.com/v3.1`.

Habilitar en la clase principal: `@EnableConfigurationProperties(RestCountriesProperties.class)`.

**Por qué no hardcodear:** si cambia la URL, modificás solo el archivo de configuración sin tocar código. Buena práctica estándar.

---

## 🔴 El servicio — `BuscadorDePaises.java` 🎯

Componente de Spring Boot que realmente consume la API.

```java
@Component                    // Spring: "esta clase es un componente, administrala vos"
public class BuscadorDePaises {

    private final RestTemplate restTemplate;                // Cliente HTTP
    private final RestCountriesProperties propiedades;      // Config (URL base)

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

        // 2. GET y mapeo a array de Pais
        Pais[] paises = restTemplate.getForObject(uri, Pais[].class);

        // 3. Array → List (manejar null)
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

### Conceptos clave 🎯

- **`@Component`** — Spring administra la clase. Nunca hacemos `new BuscadorDePaises()`; Spring lo hace.
- **`RestTemplate`** — cliente HTTP de Spring. `getForObject(uri, Pais[].class)` hace un GET y mapea el JSON a un array de `Pais`.
- **`UriComponentsBuilder`** — construye URIs de forma segura (encoding, query params, path variables) sin concatenar strings.
- **`Optional<Pais>`** — contenedor que puede tener un valor o estar vacío. Evita devolver `null`.

### Inyección de Dependencias (intro) 📘

El profe lo mencionó sin profundizar. Lo básico: cuando una clase con `@Component` tiene un constructor con parámetros, Spring busca esos objetos en su contexto y los pasa automáticamente.

```java
// NO hacemos esto nunca:
BuscadorDePaises buscador = new BuscadorDePaises(restTemplate, propiedades);

// Spring lo hace por nosotros cuando ponemos @Component en la clase
// y un constructor donde lo necesitamos
```

> **El profe fue claro:** "esto vamos a profundizarlo en clases siguientes. Hoy solo sepan que Spring se encarga de crear e inyectar los objetos."

---

## 🟡 Tests de integración con `@SpringBootTest` 🎯

```java
@SpringBootTest                                    // Spring Boot toma el control del test
class BuscadorDePaisesIT {

    @Autowired                                     // Spring inyecta el buscador
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

- **`@SpringBootTest`** — JUnit arranca Spring Boot completo antes del test. Sin esto, `@Autowired` no funciona.
- **`@Autowired`** — "Spring, dame una instancia." Busca la clase con `@Component`, la instancia, la setea.
- **`var`** — inferencia de tipos (Java 10+). `var lista = ...` ≡ `List<Pais> lista = ...`. Solo para variables locales.
- **`assertThat`** — AssertJ, más expresivo que `Assertions` de JUnit puro. Permite encadenar: `assertThat(lista).isNotEmpty()`.

> **Diferencia con material previo:** en testing previo usamos `Assertions.assertEquals()` de JUnit puro. Acá `assertThat()` de AssertJ. **Ambos válidos** — la cátedra acepta cualquiera.

---

## 🟡 Postman — Cliente HTTP para probar APIs 🎯

"Navegador para APIs" — interfaz gráfica para hacer requests sin escribir código.

1. Elegís método (GET, POST, PUT, DELETE).
2. URL del endpoint.
3. Si hace falta, headers y body.
4. Send → ves respuesta con status code, headers, body.

**Útil para:** probar APIs antes de consumirlas, debuggear cuando algo falla, explorar APIs nuevas.

> **Conexión:** lo que hacías con `fetch()` en JavaScript, Postman lo hace con interfaz gráfica. Mismo concepto, sin código.

---

## 🟢 Flujo completo 🎯

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

# 📕 PARTE 3 — TPA DonaTrack — Entrega 1

## 🔴 El sistema: DonaTrack 🎯

Sistema de gestión y trazabilidad de donaciones desde la recepción en depósito hasta la entrega a entidades beneficiarias. Arquitectura distribuida en servicios (microservicios). Basado en la iniciativa UTN Solidaria.

### Servicios del sistema

| Servicio | Qué hace |
|---|---|
| **Donaciones** | Gestión de donantes (humanos y jurídicos), donaciones, entidades beneficiarias |
| **Logística** | Camiones, rutas de entrega, trazabilidad |
| **Incentivos** | Analítica de donantes, misiones, insignias, rankings, gamificación |
| **Notificaciones** | Email, SMS, WhatsApp |
| **Autenticación** | Tokens y autenticación |
| **Frontend (SSR)** | Presentación con server-side rendering (Entrega 5) |

**Tecnologías:** Java 21 + Spring Boot + Maven. Cada servicio = proyecto Spring Boot independiente. BD MySQL (relacional) para la mayoría; documental para Logística.

---

## 🔴 Entrega 1 — Lo que hay que presentar 🎯

**Fecha:** Semana del 20 de abril (entrega clase 2, defensa clase 5 el 22/04).

### Alcance

Solo dos servicios: **Donaciones** y **Notificaciones** (primera iteración).

### Entregables concretos

| # | Entregable | Detalle |
|---|---|---|
| 1 | **Diagrama de clases** | Uno por servicio (Donaciones + Notificaciones). Modelo de dominio |
| 2 | **Diagrama de arquitectura** | Despliegue y/o componentes |
| 3 | **Justificaciones de diseño** | Documento con decisiones tomadas |
| 4 | **Diagrama de casos de uso** | General, todos los actores y funcionalidades |
| 5 | **Bocetos de interfaz** | De los 20 reqs de UI/UX. Mano, Paint, lo que sea. No HTML, no Figma |
| 6 | **Implementación** | Código funcionando |
| 7 | **Endpoint simple** | Donaciones expone GET → "Hola desde el servicio de Donaciones" |

### Pregunta de discusión 📘

> *"¿Un modelo de dominio rico es una inversión necesaria para capturar la complejidad del negocio o una sobreingeniería innecesaria frente a un modelo anémico más simple?"*

---

## 🔴 Orden de trabajo recomendado 🎯

Los ayudantes fueron explícitos:

1. **Primero:** Diagrama de casos de uso — "ya lo pueden hacer, no hay nada que tengamos que enseñar."
2. **Segundo:** Bocetos de interfaz — "simple, papel, Paint, desprolijo. Son descartables. No pierdan tiempo."
3. **Tercero:** Diagrama de clases — "pensar el modelo entre todos, sin asistirse por IA. La parte de pensar háganla juntos."
4. **Cuarto:** Implementación — "esto es lo último, no se tiren de lleno al código porque si no lo van a tener que modificar."

### Lo que NO hacer todavía

- ❌ Empezar por el código — primero diseñar.
- ❌ HTML/Figma — los bocetos son dibujitos, no maquetas. Las maquetas son para Entrega 4.
- ❌ Exponer endpoints REST (salvo el "Hola desde el servicio") — eso es la semana siguiente.
- El **endpoint simple** se explica en clase 3. Pueden esperar.

---

## 🟡 Dominio de la Entrega 1 — Resumen 🎯

### Donantes

- **Personas humanas:** nombre, apellido, edad, documento, género, dirección + al menos un medio de contacto (email obligatorio, teléfono y WhatsApp opcionales). Eligen medio predeterminado.
- **Personas jurídicas:** razón social, tipo (Gubernamental, ONG, Empresa, Institución), rubro + medio de contacto. Tienen personas representantes.

### Donaciones y segmentación

- El admin registra la donación en nombre del donante.
- Cada donación: descripción general + bienes. Cada bien: descripción, foto (opcional), categoría → subcategoría, estado (nuevo/usado si aplica), fecha de vencimiento (si perecedero), cantidad + unidad.
- **Segmentación automática:** la carga se divide en múltiples donaciones independientes agrupadas por subcategoría. La subcategoría es la unidad mínima de asignación.

### Entidades beneficiarias

- Organizaciones sin fines de lucro (escuelas, comedores, etc.). Razón social, dirección, teléfono, representantes.
- Registran **necesidades materiales**: subcategoría + descripción.
  - **Extraordinarias:** situaciones excepcionales (inundación, incendio). Se cubren con donaciones parciales hasta alcanzar la cantidad.
  - **Recurrentes:** consumo habitual periódico (ej: 100 paquetes de fideos/semana).

### Importación masiva por CSV

- Importar donantes desde CSV (puede tener 20.000+ filas).
- Si el email existe → actualizar datos. Si no existe → crear usuario + enviar credenciales.

### Notificaciones (primera iteración)

- Componente recibe: destinatario + mensaje + medio (email, SMS, WhatsApp).
- En esta entrega: **simular** el envío (no integración real). Marcar como completada.

---

## 🟢 Info operativa de la clase 📘

- **Tutores personales:** aún no asignados al momento de la clase. Avisan por Discord + canal por equipo.
- **Canal general Discord:** solo dudas de enunciado ("no entiendo qué me piden"). Dudas de implementación → con el tutor.
- **Repos:** la cátedra los va a dar. Esperar el aviso.
- **IDE:** IntelliJ Community alcanza. Con cuenta estudiantil → licencia Pro gratis.
- **Tip IntelliJ:** consume RAM. Varios proyectos abiertos a la vez puede ponerse lento. Auto-save puede trabar si disco lento (deshabilitable).

---

**FIN DEL APUNTE RESUMEN — CLASE 2**
