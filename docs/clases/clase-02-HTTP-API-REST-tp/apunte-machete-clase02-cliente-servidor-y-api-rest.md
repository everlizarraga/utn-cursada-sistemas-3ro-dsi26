# Machete — Clase 02 · Cliente-Servidor, HTTP, API REST

---

# PARTE 1 — CLIENTE-SERVIDOR

## 1. Qué es
No es un protocolo. **Patrón arquitectónico.**
Define: **componentes** + **forma de conexión** + **vocabulario**.
Servidor: provee servicios por una **interfaz**. Cliente: los **usa**.
Independiente del protocolo (HTTP, SMTP, IMAP…). Ambos deben hablar el mismo.

## 2. Las cuatro reglas
1. El cliente **siempre** inicia. Nunca el servidor.
2. El servidor **escucha en un puerto**.
3. Ciclo: **Solicitud → Procesamiento → Respuesta.**
4. Atiende **múltiples clientes simultáneamente**.

Notificaciones push → **hay que salir de HTTP/1.1** (HTTP/2, /3, WebSockets).

## 3. Ejemplos
Navegador ↔ Apache/Nginx (HTTP) · Home banking ↔ servidor del banco · **Outlook ↔ SMTP/IMAP** · App delivery ↔ backend · Videojuego ↔ servidor del juego.

Cliente ≠ navegador. **Backend que le pega a otro backend = cliente.**
El que **expone** → servidor. El que **consume** → cliente. **Roles, no identidades.**

## 4. Clasificación por responsabilidades
Eje: **dónde vive la lógica de negocio.**

| Tipo | Lógica | Nombre |
|---|---|---|
| Cliente activo, Servidor pasivo | Casi toda en el cliente; servidor solo persiste | — |
| Cliente pasivo, Servidor pasivo | Poca en ambos; componentes intermedios | — |
| Cliente pasivo, Servidor activo | Casi toda en el servidor | **Cliente liviano** |
| Cliente activo, Servidor activo | Distribuida | **Cliente pesado** |

No es pregunta de parcial. Es vocabulario para justificar.
Más lógica en el cliente → ↑ procesamiento, ↑ batería, ↑ memoria, ↓ carga del servidor.
Lo define **el diseño y las tecnologías**, no la aplicación. **React/Angular → cliente pesado.**

## 5. Ventajas / desventajas del cliente liviano
**✅ Mantenibilidad** — cambios centralizados.
**✅ Seguridad** — control de acceso centralizado.
**❌ Eficiencia** — cuello de botella. *(un solo servidor)*
**❌ Disponibilidad** — único punto de falla. *(un solo servidor)*

## 6. Trade-offs entre atributos de calidad
```
+ servidores → ✅ eficiencia · ✅ disponibilidad
             → ❌ mantenibilidad · ❌ COSTO
```
**No hay arquitectura mejor. Hay una que privilegia unos atributos a costa de otros.**

Punto único de falla = **el eslabón más débil**. 2 apps + 1 BD → el punto de falla **se mudó**.
Cualquier componente sin redundancia **es** un punto único de falla.

**EL MOLDE DE TODA RESPUESTA:**
> Pongo X **acá**, porque priorizo **[atributo]**, y pago con **[otro atributo]**.

---

# PARTE 2 — HTTP + ARQUITECTURA WEB

## 1. Qué es HTTP
HyperText Transfer Protocol. **RFC 2616.**

| | |
|---|---|
| Capa | **Aplicación (7, OSI)** |
| Modo | **Sincrónico** |
| Estado | **SIN ESTADO (stateless)** |
| Patrón | Cliente/Servidor · Solicitud/Respuesta |
| Puertos | **80 / 443** |

Métodos: **GET · POST · PUT · DELETE** (+ PATCH).
Capa 7 también: SMTP (envío mail), IMAP/POP3 (recepción), MQTT. Abajo: TCP, IP, UDP.

## 2. Sincrónico y sin estado
**Sincrónico** = pedís y esperás. Sin streaming en HTTP/1.1 → *polling*.

**SIN ESTADO** = no recuerda nada de la solicitud anterior.
```
GET /clientes/5   →  OK
GET /facturas     →  ¿de qué cliente? 🤷
GET /clientes/5/facturas   ✅
```
> **Cada solicitud lleva TODA la información necesaria.**

Estado de la **conexión** → **TCP**. Estado de la **operación** → **nadie**.

## 3. Request — 3 partes
```http
GET /usuarios HTTP/1.1              ← ① Método + URL + Versión
Host: api.miapp.com                 ← ② Headers (metadatos)
Content-Type: application/json
Authorization: Bearer <token>

{ "nombre": "Juan" }                ← ③ Body
```
**Body solo en POST, PUT, PATCH.**

**Token ≠ cifrado.** Token protege **el recurso**. HTTPS protege **el viaje**. En producción: **los dos**.

## 4. Response — 3 partes
```http
HTTP/1.1 200 OK                     ← ① Versión + Código + Descripción
Content-Type: application/json      ← ② Headers
Content-Length: 342

{ "id": 1, "nombre": "Juan" }       ← ③ Body
```

## 5. Métodos
| | | Body |
|---|---|---|
| **GET** | Obtiene | ❌ |
| **POST** | Crea | ✅ |
| **PUT** | Modifica completo | ✅ |
| **PATCH** | Modifica parcial | ✅ |
| **DELETE** | Borra | ❌ |

## 6. Códigos de estado
| | |
|---|---|
| **1xx** | Informativas |
| **2xx** | Correctas |
| **3xx** | Redirecciones |
| **4xx** | Error **del cliente** |
| **5xx** | Error **del servidor** |

**Los genera SIEMPRE el servidor.** La familia indica **de quién fue la culpa**, no quién emite.

| | |
|---|---|
| **200** | OK |
| **201** | Created → **POST** |
| **204** | No Content → **DELETE** |
| **302** | Redirect |
| **400** | Bad Request |
| **401** | **No autenticado** (sin token / token inválido) |
| **403** | **Autenticado, sin permiso** |
| **404** | Not Found |
| **405** | Method Not Allowed |
| **500 / 503 / 504** | Server Error / Unavailable / Timeout |

⚠️ En la grabación 401 y 403 quedaron **invertidos**. El estándar es el de arriba.
500 → **genérico, sin detalle** (no le des info a un atacante).

## 7. HTTPS y puertos
| | HTTP | HTTPS |
|---|---|---|
| Puerto | 80 | **443** |
| Cifrado | ❌ | **TLS/SSL** |
| Certificado | ❌ | **✅** |
| Uso | Solo local | **Producción siempre** |

SSL → deprecado → **TLS**. Certificado se renueva.
Puertos **0-1023 reservados** (80 HTTP · 443 HTTPS · 22 SSH).
**Dos apps NO pueden escuchar en el mismo puerto en el mismo nodo.** (Se cambia en `application.yml`.)

## 8. Arquitectura Web — 8 pasos
```
1. URL en el navegador
2. DNS → IP                          (primario + secundario; caché local)
3. Conexión TCP
4. Negociación TLS                   (si HTTPS)
5. HTTP GET /
6. Servidor devuelve HTML            (Content-Type: text/html)
7. Navegador pide CSS, JS, imágenes  (los cacheados no se repiden)
8. Render
```
El navegador recibiendo HTML ya armado = **CLIENTE LIVIANO**.

## 9. DevTools
`F12` → **Network**. Ves: URL · método · código · IP:puerto · Content-Type · body · cascada de recursos.

---

# PARTE 3 — API y API REST

## 1. Qué es una API
Interfaz que un componente expone para que otros lo usen **sin saber cómo está implementado**.

**NO toda API es web:** Stream API de Java · JPA · JDBC · Win32 API · POSIX API.
**API REST = caso particular:** interfaz **web y distribuida**, sobre HTTP.

## 2. Qué es REST
REpresentational State Transfer. **Estilo de diseño de comunicación** cliente-servidor.

**Cátedra: NO es estilo arquitectónico. NO es patrón de diseño.**
*(La literatura sí lo llama estilo arquitectónico. En el parcial: responder con la cátedra.)*

Decir "API REST" ya acuerda: **HTTP · verbos HTTP · códigos de estado · URI · JSON.**

## 3. Los 5 criterios
1. Se apoya sobre **HTTP**.
2. **Cada request es autocontenida** ← consecuencia de que HTTP sea stateless.
3. Usa los **verbos HTTP**: POST crea · GET obtiene · PUT modifica · PATCH parcial · DELETE borra.
4. Los recursos se identifican por **URI**. (**I** = Identifier, no Locator.)
5. Formato: **JSON** (o XML). JSON = stream más liviano (XML repite etiquetas).

## 4. JSON
Tipos: string · número · booleano · array · objeto · `null`.
```json
{ "id": 42, "activo": true, "roles": ["admin"], "dir": { "pais": "AR" } }
```
`true` ≠ `"true"`. **JSON no tiene `undefined`** (eso es de JS). Sí `null`.

## 5. Rutas REST
```
GET    /users        → 200
GET    /users/1      → 200
POST   /users        → 201 Created
PUT    /users/1      → 200
DELETE /users/1      → 204 No Content
```
**Misma ruta, distinto verbo.** La ruta identifica el recurso; el verbo dice qué le hacés.

❌ `/users/obtenerUsuarioPorId/1` · `/users/actualizarUsuario` · `/users/borrar/1`

## 6. Buenas prácticas
1. Rutas = **sustantivos** (recursos).
2. **Sin verbos en la ruta** (ya están en HTTP).
3. **Códigos de estado HTTP**, no `200 OK` con `{"error": ...}` adentro.

## 7. REST a rajatabla — el N+1
```
GET /alumnos/12345 → { cursadas: [101, 205, 307, 412] }   ← solo IDs
→ 4 requests más. 40 materias → 40 requests.
```
**Regla: no mezclar entidades.** Cada recurso devuelve lo suyo.
Industria: se rompe la regla (REST vs **RESTful**).
**TPA: a rajatabla. NO mezclar entidades.**
Lo resuelve **GraphQL** (agregación de recursos) → clase 30.

## 8. Tipos de API
**Abierta** = sin credencial. **Cerrada** = con registro/credencial.
**Gratuita / Paga** = eje independiente. **Puede ser cerrada Y gratuita.**

## 9. Swagger / OpenAPI
**OpenAPI** = el estándar (3.0). **Swagger** = la herramienta que lo renderiza.
Documenta: rutas · parámetros · ejemplos · códigos de respuesta.
**TPA: en las dos direcciones — leer la de la cátedra, escribir la tuya.**

---

# PARTE 4 — PRÁCTICA

## 1. La API del ejemplo
REST Countries `restcountries.com/v3.1`. **Abierta y gratuita.**
**Endpoint** = ruta concreta que el servicio expone.
```
/all · /name/{n} · /currency/{c} · /region/{r} · /capital/{c}
```

## 2. Navegador
**Barra del navegador + Enter = HTTP GET.** Siempre.
**Solo GET.** Para POST → formulario HTML o JS. Para PATCH/DELETE → ni con eso.
**El navegador es un cliente HTTP.**

## 3. Postman
**Cliente HTTP.** Cualquier método, header, body.
Solapas: Método · URL · Authorization · Headers · Body · Send.
**Es la anatomía de una request, con botones.**

**Path param** — identifica. En la ruta: `/name/argentina`
**Query param** — filtra. Después del `?`: `?fields=name,flags&currency=ars` (clave=valor, encadenados con `&`)

`/all` sin `fields` → **400 Bad Request**. Cada API es distinta: **leer la documentación.**

## 4. Java + Spring Boot

### 4.1 Spring Initializr
`start.spring.io` · Maven · **Spring Boot 4.0.5** · **Java 21** · YAML
Group: `ar.edu.utn.ba.ddsi` · Deps: **Web MVC · Configuration Processor · DevTools** (+ Lombok)
**TPA: todos con la misma versión de Java (21).**

### 4.2 Estructura
```
.idea/     ❌ NO al repo
.mvn/      ✅
src/main/  ✅ java/ + resources/application.yml
src/test/  ✅
target/    ❌ NO al repo
pom.xml    ✅ ← el package.json de Java (Maven)
```

### 4.3 Código
**Orden:** ① interfaz → ② molde (DTO) → ③ test → ④ conexión.

**① Componente**
```java
@Component
public class BuscadorDePaises { ... }   // Spring lo instancia. Nunca hacés new.
```
`buscarPorNombre` → **`Optional<Pais>`** (uno o ninguno)
`buscarPorMoneda` → **`List<Pais>`** (muchos comparten moneda)

**② DTO** — clase sin lógica, molde del JSON. Java es tipado: **hay que declarar la forma antes.**
```java
@JsonIgnoreProperties(ignoreUnknown = true)   // ignora campos no declarados
@Data @NoArgsConstructor @AllArgsConstructor
public class Pais {
    @JsonProperty("name") private NombrePais nombre;   // puente inglés → castellano
    @JsonProperty("capital") private List<String> capitales;
    @JsonProperty("currencies") private Map<String, DetalleMoneda> monedas;  // clave impredecible
}
```
**Jackson:** `new Pais()` vacío → llena con **setters**.
→ `@NoArgsConstructor` (el constructor vacío) + `@Data` (los setters). **Obligatorias.**
`@AllArgsConstructor` **pisa** el constructor vacío implícito de Java → por eso hace falta el otro.

**③ Test**
```java
@SpringBootTest                    // levanta el contenedor
class BuscadorDePaisesIT {
    @Autowired private BuscadorDePaises buscador;   // nunca un new
```
`assertThat(...)` = **AssertJ** (no JUnit). Conviven: JUnit corre, AssertJ asierta.
`var` ≠ `let`. `getFirst()` = Java 21.

**④ Conexión**
```yaml
rest-countries:
    base-url: https://restcountries.com/v3.1
```
```java
@ConfigurationProperties(prefix = "rest-countries")
@Data
public class RestCountriesProperties { private String baseUrl; }
// base-url → baseUrl (Spring normaliza). Habilitar con @EnableConfigurationProperties.
```
```java
URI uri = UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
        .path("/name/{nombre}").queryParam("fields", CAMPOS)
        .buildAndExpand(nombre).toUri();
Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
```
`build()` → ruta fija. **`buildAndExpand(v)` → hay `{placeholder}`. Rellena Y ESCAPA.**
No concatenar con `+` (espacios y caracteres especiales rompen la URL).
`Pais[].class` y no `List<Pais>` (los genéricos se borran al compilar) → `Arrays.asList()`.
Devolver **`List.of()`, nunca `null`.**
`RestTemplate` no se crea con `new`: va en `@Configuration` + `@Bean`.

### 4.4 El bug
`@JsonProperty("comon")` (1 M) → el campo llega en **`null`**. No explota, por `ignoreUnknown`.
**Síntoma: un campo en `null` y el resto bien → `@JsonProperty` mal escrito.**
Debug: `println` de lo que llegó **antes** de leer código.

## 5. Info operativa

### 5.1 Herramientas
**IntelliJ** (Community alcanza; consume RAM) · **Postman** · **Maven** · **Git** (anotar la cuenta en el Excel) · **Discord** · **Drive del curso** (link en el aula virtual).
VS Code para Java: **no recomendado.**

### 5.2 TPA — Entrega 1
Contexto: **UTN Solidaria**. **6 entregas.** Tutores personales por equipo.

- 2 **diagramas de clases**: donaciones + notificaciones (solo lo que pide el enunciado)
- **Diagrama de arquitectura** (ya viene dado)
- **Justificaciones** ← muy importante
- **Diagrama de casos de uso**
- **Implementación** de los requerimientos
- **~20 bocetos** de interfaz (papel/Paint, desprolijos, descartables)
- **Endpoint** `GET` → *"Hola desde el servicio"* (exponer, no consumir)
- **Pregunta de discusión** → **defensa individual**

**ORDEN: bocetos → casos de uso → diagrama de clases → CÓDIGO (último).**
El modelado se piensa **entre todos**.

### 5.3 Canales
**Foro general** → solo **dudas de enunciado**.
**Tutor personal** → **implementación y diseño**. Es quien te evalúa.

---

**FIN DEL MACHETE — CLASE 02**
