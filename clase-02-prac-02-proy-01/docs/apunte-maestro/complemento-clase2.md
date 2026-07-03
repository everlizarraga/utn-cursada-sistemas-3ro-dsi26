# 📗 Complemento — Clase 2: Cliente-Servidor, HTTP, API REST y Spring Boot

**Materia:** Diseño de Sistemas de Información (DSI) — UTN FRBA  
**Cubre:** Partes 1 a 3 del apunte maestro de la clase 2  
**Contiene:** Aclaraciones destiladas del chat + Respuestas de checkpoints  

---

# PARTE A: Aclaraciones Destiladas

---

## 1. URI vs URL — Qué es cada una

URI (Uniform Resource **Identifier**) es el identificador genérico de un recurso. URL (Uniform Resource **Locator**) es un subconjunto de URI que además de identificar el recurso, indica *dónde* encontrarlo (incluye protocolo y servidor). En la práctica se usan casi como sinónimos, pero técnicamente toda URL es una URI, no al revés.

En el código de la clase, `UriComponentsBuilder` construye un objeto `URI` de Java (`java.net.URI`) que contiene la dirección completa: protocolo + dominio + path + query params. `RestTemplate` recibe ese objeto para saber a dónde hacer el request.

**Para el parcial, si te preguntan:** *"¿Qué diferencia hay entre URI y URL?"* — URI (Uniform Resource Identifier) identifica un recurso de forma unívoca. URL (Uniform Resource Locator) es un tipo de URI que además indica la ubicación del recurso (protocolo, host, path). En REST, cada recurso se identifica mediante una URI.

---

## 2. Agregar dependencias después de crear el proyecto Spring Boot

Spring Initializr es solo el punto de partida. Las dependencias se pueden agregar en cualquier momento del desarrollo directamente en el `pom.xml`. El flujo es: buscar la dependencia en Maven Central (mvnrepository.com), copiar el bloque XML `<dependency>...</dependency>`, pegarlo dentro de `<dependencies>` en el `pom.xml`, y hacer click en el ícono de recarga de IntelliJ para que baje la nueva dependencia.

Es el equivalente exacto a hacer `npm install nueva-dependencia` en un proyecto Node.js — se agrega al archivo de configuración y se descarga. Esto se hace constantemente durante el desarrollo del TPA a medida que surgen nuevas necesidades.

---

## 3. El archivo `application.yml` — cómo se genera

Spring Initializr genera automáticamente un archivo de configuración en `src/main/resources/`. Por defecto lo crea como `application.properties` (formato clave=valor). El profe lo cambió a `application.yml` (formato YAML) porque es más legible para configuraciones anidadas. Son dos formatos para lo mismo — funcionalmente equivalentes. Si Spring Initializr generó `application.properties`, simplemente se renombra a `application.yml` y se adapta la sintaxis.

---

## 4. Java es más verboso que JavaScript — pero tiene su razón

Java requiere más "ceremonia" que JavaScript para lograr lo mismo (DTOs, annotations, configuración, tipado explícito). Esa verbosidad tiene propósitos concretos: el tipado fuerte detecta errores en compilación (antes de ejecutar), las annotations hacen explícito el comportamiento, y la estructura forzada facilita el mantenimiento en proyectos grandes con equipos. Herramientas como IntelliJ (Generate, auto-complete) y Lombok (@Data, @NoArgsConstructor) reducen significativamente el trabajo manual. Con el uso se vuelve automático.

---

# PARTE B: Respuestas de Checkpoints

---

## Checkpoint Parte 1 — Cliente-Servidor, HTTP, API REST

**1. ¿Qué es el patrón Cliente-Servidor y quién inicia siempre la comunicación?**
Es un patrón arquitectural donde participan dos componentes: un servidor que provee servicios a través de una interfaz y un cliente que los consume. El cliente siempre inicia la comunicación; el servidor escucha en un puerto esperando solicitudes.

**2. ¿Qué diferencia hay entre cliente liviano y cliente pesado? ¿Qué consecuencias tiene cada uno?**
En un cliente liviano, el servidor posee la mayor lógica de negocio y genera la vista; el cliente solo presenta datos (bajo procesamiento, poca carga de recursos). En un cliente pesado, la lógica está distribuida entre ambos: el cliente tiene lógica de presentación, interacción y parte de la lógica de negocio (mayor procesamiento del lado del cliente, más consumo de recursos). La elección impacta en mantenibilidad, performance, consumo de batería en móviles y complejidad de cada componente.

**3. ¿Qué significa que HTTP es "sin estado" (stateless)?**
Cada solicitud HTTP es independiente. El servidor no recuerda solicitudes anteriores. Si necesito información de una operación previa, debo enviarla nuevamente en la solicitud actual. Cada request contiene toda la información necesaria para que el servidor pueda procesarla.

**4. ¿Cuáles son las tres partes de una request HTTP y las tres partes de una response?**
Request: línea de inicio (método + URL + versión), headers (metadatos como Content-Type, Authorization) y body (datos enviados, solo en POST/PUT/PATCH). Response: línea de estado (versión + código + descripción), headers (metadatos como Content-Type, Content-Length) y body (datos devueltos por el servidor).

**5. ¿Qué métodos HTTP existen y cuáles llevan body?**
GET (obtener recurso, sin body), POST (crear recurso, con body), PUT (actualizar completo, con body), PATCH (actualizar parcial, con body) y DELETE (borrar recurso, sin body).

**6. ¿Qué diferencia hay entre un error 4xx y un error 5xx? ¿Y entre 401 y 403?**
Los 4xx son errores causados por el cliente (solicitud incorrecta, recurso no encontrado, falta de permisos). Los 5xx son errores del lado del servidor (fallo interno, servicio no disponible). El 401 (Unauthorized) indica que no se proporcionó credencial válida ("no sé quién sos"). El 403 (Forbidden) indica que la credencial es válida pero no tiene permiso para ese recurso ("sé quién sos, pero no podés acceder").

**7. ¿Qué diferencia hay entre HTTP y HTTPS?**
HTTP transmite datos en texto plano (sin cifrado). HTTPS agrega una capa TLS/SSL que cifra los datos en tránsito, requiere un certificado digital y usa el puerto 443 en vez del 80. En producción siempre se usa HTTPS; HTTP solo para desarrollo local.

**8. ¿Qué es una API? ¿Siempre implica un servicio web remoto?**
API (Application Programming Interface) es un conjunto de herramientas, definiciones y protocolos que permiten que componentes de software se comuniquen entre sí. No siempre implica un servicio web remoto: puede ser una biblioteca local (API de Streams de Java, JPA, API de Win32) o un componente distribuido (API REST). Es la interfaz que un componente expone para que otros lo usen.

**9. ¿Qué es REST y qué establece como convenciones?**
REST (Representational State Transfer) es un estilo de diseño de comunicación cliente-servidor. Establece: uso del protocolo HTTP, solicitudes stateless (cada request contiene toda la información), uso de verbos HTTP para operaciones, identificación de recursos mediante URIs únicas, y JSON como formato de intercambio.

**10. ¿Por qué las rutas REST usan sustantivos y no verbos?**
Porque la acción ya la indica el verbo HTTP (GET para obtener, POST para crear, PUT para actualizar, DELETE para borrar). Poner verbos en la ruta sería redundante y rompería la convención REST. La ruta identifica el recurso (qué), el verbo indica la operación (qué hacer con él).

---

## Checkpoint Parte 2 — Spring Boot y Consumo de API REST

**1. ¿Qué es Spring Boot y para qué se usa en la materia?**
Spring Boot es un framework para Java que simplifica la creación de aplicaciones. En la materia se usa todo el año para implementar el TPA: cada servicio del sistema (Donaciones, Logística, Incentivos, etc.) es un proyecto Spring Boot independiente.

**2. ¿Qué hace la annotation `@Component` en una clase?**
Le indica a Spring Boot que esa clase es un componente administrado por el framework. Spring se encarga de instanciarla, configurarla e inyectarla donde se necesite. Nunca hacemos `new` de una clase anotada con `@Component` — Spring lo hace por nosotros.

**3. ¿Qué es un DTO y para qué sirve `@JsonProperty`?**
Un DTO (Data Transfer Object) es una clase molde que representa la estructura de datos que se recibe o envía (en este caso, el JSON de la API). `@JsonProperty("name")` mapea un campo del JSON a un atributo de la clase con nombre diferente: el campo `"name"` del JSON se guarda en el atributo `nombre` de Java.

**4. ¿Qué hace `@JsonIgnoreProperties(ignoreUnknown = true)`?**
Le dice a Jackson (la biblioteca de serialización) que ignore campos del JSON que no estén modelados en la clase Java. Sin esto, si el JSON trae un campo que la clase no tiene, lanza una excepción. Con esta annotation, simplemente descarta los campos que no le interesan.

**5. ¿Qué es `RestTemplate` y qué hace `getForObject()`?**
`RestTemplate` es un cliente HTTP de Spring que permite hacer requests. `getForObject(uri, Pais[].class)` hace un GET a la URI indicada y mapea automáticamente la respuesta JSON a un array de objetos `Pais` usando Jackson.

**6. ¿Qué diferencia hay entre `@SpringBootTest` y `@Test` solo?**
`@Test` marca un método como test para JUnit. `@SpringBootTest` arranca el contexto completo de Spring Boot antes de ejecutar los tests, lo que permite usar `@Autowired` para inyectar componentes. Sin `@SpringBootTest`, Spring no se inicializa y la inyección no funciona.

**7. ¿Qué hace `@Autowired` y quién se encarga de instanciar el objeto?**
`@Autowired` le dice a Spring: "inyectá aquí una instancia de esta clase." Spring busca la clase (que debe estar anotada con `@Component` o similar), la instancia si no existe, y la setea en ese atributo. Nosotros nunca la instanciamos manualmente.

**8. ¿Por qué se pone la URL de la API en `application.yml` en vez de hardcodearla?**
Porque si la URL cambia, solo se modifica el archivo de configuración sin tocar código. Es una buena práctica que separa configuración de lógica, facilita el mantenimiento y permite tener configuraciones diferentes por entorno (desarrollo, testing, producción).

---

**Fin del Complemento — Clase 2**
