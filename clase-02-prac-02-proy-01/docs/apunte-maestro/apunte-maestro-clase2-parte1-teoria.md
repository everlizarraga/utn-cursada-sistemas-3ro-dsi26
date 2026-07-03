# 📘 Apunte Maestro — Clase 2 — Parte 1: Cliente-Servidor, HTTP y API REST

**Materia:** Diseño de Sistemas de Información (DSI) — UTN FRBA  
**Clase:** 2 — 01/04/2026 (virtual)  
**Profes:** Lucas (teoría + práctica), Ezequiel (intros + cierre), Ailu y Gonza (TPA)  
**Fuente:** Transcripción de clase + slides  

---

## Contexto

Primera clase virtual de la materia. La clase tiene tres bloques: teoría (Cliente-Servidor, HTTP, API REST), práctica (consumir API REST desde Java con Spring Boot) y presentación del TPA. Esta parte cubre el bloque teórico. Mucho de lo que se ve acá ya lo conocés de tu entrenamiento de Async JS — el foco está en la terminología de la cátedra y en lo que es nuevo para vos.

---

## 🔴 Patrón Cliente-Servidor

### Definición

Es un patrón arquitectural donde participan dos componentes de software: un **servidor** que provee uno o más servicios a través de una interfaz, y un **cliente** que consume esos servicios.

### Reglas fundamentales

- El **cliente siempre inicia** la comunicación (nunca el servidor, en la versión clásica).
- El servidor **escucha en un puerto** determinado esperando solicitudes.
- Cada interacción sigue el ciclo: **Solicitud → Procesamiento → Respuesta**.
- El servidor puede atender **múltiples clientes** de forma simultánea.

### Ejemplos concretos que dio el profe

| Cliente | Servidor | Protocolo |
|---|---|---|
| Navegador (Chrome) | Servidor web (Apache, Nginx) | HTTP |
| App de home banking | Backend del banco | HTTP |
| Cliente de email (Outlook) | Servidor SMTP/IMAP | SMTP/IMAP |
| App mobile de delivery | Backend del sistema | HTTP |
| Videojuego online | Servidor del juego | Varía |

> **Punto clave del profe:** Fijate que los clientes y servidores pueden ser componentes con funcionalidades muy distintas, pero siempre cumplen un rol: el cliente inicia la conexión, el servidor provee el servicio. La comunicación puede usar distintos protocolos (HTTP, SMTP, etc.), pero el patrón es el mismo.

### 🟡 Clasificación según responsabilidades

| Clasificación | Dónde está la lógica | Ejemplo |
|---|---|---|
| **Cliente Activo, Servidor Pasivo** | Lógica en el cliente, servidor solo persiste datos | Sistema de gestión desktop con base de datos central |
| **Cliente Pasivo, Servidor Pasivo** | Ambos con baja lógica, componentes intermedios | Componente de integración que conecta dos sistemas |
| **Cliente Pasivo, Servidor Activo** ("Cliente liviano") | Lógica en el servidor, cliente solo presenta datos | Web tradicional donde el servidor genera el HTML |
| **Cliente Activo, Servidor Activo** ("Cliente pesado") | Lógica distribuida entre ambos | SPA con React/Angular + backend con lógica de negocio |

**Lo que enfatizó el profe:** esto no es para memorizar clasificaciones, sino para entender que **dónde pongas la lógica tiene consecuencias reales**: procesamiento, consumo de batería en dispositivos móviles, uso de memoria, mantenibilidad, etc. La decisión de hacer cliente liviano o pesado responde a necesidades técnicas y de negocio, no es arbitraria.

> **Para el parcial, si te preguntan:** *"¿Qué diferencia hay entre cliente liviano y cliente pesado?"* — En un cliente liviano, el servidor posee la mayor parte de la lógica de negocio y genera la vista; el cliente se limita a presentar los datos. En un cliente pesado, la lógica de negocio está distribuida entre ambos componentes — el cliente tiene lógica de presentación, interacción y parte de la lógica de negocio. Tecnologías como React o Angular implementan clientes pesados.

### Ventajas y desventajas del Cliente Pasivo - Servidor Activo (liviano)

**Ventajas:**

- **Mantenibilidad:** los cambios de lógica se hacen en un solo lugar (el servidor). Actualizo el servidor y todos los clientes ya tienen la nueva versión — no necesito actualizar cada cliente individualmente.
- **Seguridad:** control de acceso centralizado en el servidor. Un solo componente que securizar.

**Desventajas** (considerando un único servidor):

- **Eficiencia (tiempo de respuesta):** el servidor puede ser cuello de botella. A medida que suben los clientes concurrentes, el tiempo de respuesta crece. En algún momento el sistema puede dejar de responder.
- **Disponibilidad:** único punto de falla. Si el servidor se cae, todos los clientes quedan sin servicio.

**Solución a las desventajas:** agregar más servidores y balancear carga. Pero eso tiene trade-off: gano eficiencia y disponibilidad, pero aumento costo y potencialmente pierdo mantenibilidad (más nodos que mantener).

> **Concepto clave que el profe enfatizó:** el único punto de falla se puede mover pero no eliminar fácilmente. Si pongo dos servidores de aplicación pero uno solo de base de datos, moví el punto de falla del servidor de aplicación a la base de datos. "Es como ponerle rejas a todas las ventanas menos a una."

> **Para el parcial, si te preguntan:** *"¿Qué ventajas y desventajas tiene un cliente liviano con servidor activo?"* — Ventajas: mantenibilidad (cambios centralizados en el servidor) y seguridad (control de acceso centralizado). Desventajas (con un solo servidor): eficiencia (cuello de botella ante alta concurrencia) y disponibilidad (único punto de falla). Estas desventajas se mitigan agregando más servidores, pero con impacto en costo y complejidad.

---

## 🔴 Protocolo HTTP

### Qué es

Protocolo de Transferencia de Hipertexto (HyperText Transfer Protocol). Tiene más de 40 años. Originalmente pensado para transferir hipertexto (documentos con links), evolucionó para soportar todo lo que conocemos como la web moderna.

### Características clave

| Característica | Qué significa |
|---|---|
| **Capa de aplicación** | Capa 7 del modelo OSI — la que usan las aplicaciones |
| **Sincrónico** | Envío solicitud → espero respuesta. Ida y vuelta |
| **Sin estado (stateless)** | Cada solicitud es independiente. El servidor no recuerda solicitudes anteriores |
| **Solicitud/Respuesta** | El cliente pide, el servidor responde |
| **Puerto 80 (HTTP) / 443 (HTTPS)** | Puertos por defecto del protocolo |

**Sin estado — el profe lo explicó así:** si le pido al servidor el cliente con ID 1, y después le pido "las facturas del último cliente que te pedí", el servidor me dice "¿qué cliente? no sé de qué me hablás." Cada solicitud tiene que llevar TODA la información necesaria. No puedo asumir que el servidor recuerda nada de solicitudes anteriores.

> **Conexión con lo que ya sabés:** En tu entrenamiento de Async JS usaste Fetch API para hacer requests HTTP. Ya sabés que un `fetch` envía una solicitud y recibe una respuesta. Los conceptos de métodos (GET, POST), headers, body, status codes — todo eso lo manejaste. Lo nuevo acá es la formalización teórica y la terminología que usa la cátedra.

### Versiones de HTTP

La cátedra trabaja con HTTP 1.1 (la clásica, sincrónica, sin estado). HTTP/2 y HTTP/3 agregan cosas como streams bidireccionales, server push, etc. — pero eso se ve más adelante en la cursada.

### 🔴 Estructura de una Request HTTP

Tres partes:

```
1. LÍNEA DE INICIO: Método + URL + Versión
   GET /usuarios HTTP/1.1

2. HEADERS (cabeceras): Metadatos
   Host: api.miapp.com
   Content-Type: application/json
   Authorization: Bearer <token>

3. BODY (cuerpo): Datos — solo en POST, PUT, PATCH
   { "nombre": "Juan", "email": "juan@mail.com" }
```

**El body no viaja en GET ni en DELETE** — solo en los métodos que envían datos al servidor (POST, PUT, PATCH). En un GET, si necesitás enviar datos, van como query params en la URL: `/usuarios?nombre=Juan`.

### 🔴 Estructura de una Response HTTP

Tres partes:

```
1. LÍNEA DE ESTADO: Versión + Código + Descripción
   HTTP/1.1 200 OK

2. HEADERS: Metadatos de la respuesta
   Content-Type: application/json
   Content-Length: 342

3. BODY: Los datos devueltos
   { "id": 1, "nombre": "Juan", "email": "juan@mail.com" }
```

### Métodos HTTP principales

| Método | Acción | Body | Ejemplo |
|---|---|---|---|
| **GET** | Obtener un recurso | No | `GET /users/1` |
| **POST** | Crear un recurso nuevo | Sí | `POST /users` + body con datos |
| **PUT** | Actualizar recurso completo | Sí | `PUT /users/1` + body completo |
| **PATCH** | Actualizar recurso parcialmente | Sí | `PATCH /users/1` + body parcial |
| **DELETE** | Borrar un recurso | No | `DELETE /users/1` |

### 🔴 Códigos de Estado HTTP

Los códigos siempre los genera el servidor. Se agrupan por familia:

| Familia | Significado | Ejemplos comunes |
|---|---|---|
| **1xx** | Informativo — solicitud recibida, procesando | 100 Continue |
| **2xx** | Éxito — solicitud procesada correctamente | 200 OK, 201 Created, 204 No Content |
| **3xx** | Redirección — el recurso se movió | 301 Moved Permanently, 302 Found |
| **4xx** | Error del cliente — la solicitud tiene un problema | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 405 Method Not Allowed |
| **5xx** | Error del servidor — algo falló internamente | 500 Internal Server Error, 503 Service Unavailable, 504 Gateway Timeout |

**Diferencia entre 401 y 403 que explicó el profe:**
- **401 Unauthorized:** no mandaste token o el token no es válido. "No sé quién sos."
- **403 Forbidden:** mandaste un token válido, pero no tenés permiso para acceder a ese recurso. "Sé quién sos, pero no podés entrar acá."

**Sobre los 500:** el profe enfatizó que en producción nunca se devuelve detalle del error al cliente — solo un 500 genérico. No querés darle información interna de tu sistema a un potencial atacante.

> **Para el parcial, si te preguntan:** *"¿Qué significan los códigos de estado HTTP 2xx, 4xx y 5xx?"* — Los 2xx indican que la solicitud fue procesada correctamente (200 OK, 201 Created). Los 4xx indican errores causados por el cliente (404 Not Found, 401 Unauthorized). Los 5xx indican errores del lado del servidor (500 Internal Server Error). Los códigos siempre los genera el servidor como parte de la response.

### 🟡 HTTP vs HTTPS

| | HTTP | HTTPS |
|---|---|---|
| **Puerto** | 80 | 443 |
| **Seguridad** | Sin cifrado — datos viajan en texto plano | Cifrado TLS/SSL — datos viajan cifrados |
| **Certificado** | No requerido | Requerido (se renueva periódicamente) |
| **Uso** | Solo desarrollo local | Producción siempre |

**Punto importante del profe:** son dos capas de seguridad distintas. HTTPS cifra los datos en tránsito (que nadie pueda leer lo que viaja). El token de autorización controla quién puede acceder a qué recurso. En producción se usan las dos juntas.

---

## 🟡 Arquitectura Web — Qué pasa cuando entrás a una URL

El profe hizo un paso a paso y lo demostró en vivo con la consola de Chrome (F12 → Network):

1. Escribís `https://www.google.com` en el navegador
2. **DNS** traduce el dominio a una dirección IP (si no está en caché local, consulta al servidor DNS)
3. Se establece **conexión TCP** con el servidor (three-way handshake)
4. Se negocia **cifrado TLS** (si es HTTPS)
5. El navegador envía un **HTTP GET /**
6. El servidor procesa y devuelve **HTML**
7. El navegador lee el HTML y descarga recursos adicionales: **CSS, JS, imágenes, fuentes**
8. La página se **renderiza** en pantalla

**Demo en vivo:** el profe abrió Chrome → F12 → Network → Ctrl+F5 en google.com y mostró cómo el primer request es GET a "/" que devuelve HTML, y después se disparan decenas de requests adicionales para CSS, JS, imágenes, fuentes, etc. Cada uno es un request HTTP independiente.

> **Tip del profe:** la consola de desarrollador (F12) es "algo fundamental para ustedes". Solapa Network para ver requests, solapa Sources para ver de dónde vienen los recursos, solapa Elements para inspeccionar el DOM.

---

## 🔴 API y API REST

### Qué es una API

**Application Programming Interface** — un conjunto de herramientas, definiciones y protocolos para integrar aplicaciones. Permite que un componente se comunique con otro sin saber cómo funciona internamente.

**Punto clave del profe:** API no siempre significa "un servicio web remoto". Una API puede ser una biblioteca local (como la API de Streams de Java, JPA, la API de Win32). El concepto es más amplio: es la interfaz que un componente expone para que otros lo usen.

### Qué es REST

**RE**presentational **S**tate **T**ransfer — un estilo de diseño de comunicación entre cliente y servidor. **No es** un estilo arquitectónico ni un patrón de diseño — es una forma específica de diseñar la comunicación.

**Lo que establece REST:**

- Usa **protocolo HTTP**.
- Cada solicitud contiene **toda la información necesaria** (stateless).
- Usa los **verbos HTTP** (GET, POST, PUT, PATCH, DELETE) para las operaciones.
- Los recursos se identifican con **URIs** (cada recurso tiene una ruta única).
- El formato de intercambio es **JSON** (o XML, pero JSON es el estándar actual).

### 🔴 Rutas REST y convenciones

```
GET    /users        → Obtener todos los usuarios         → 200 OK
GET    /users/1      → Obtener el usuario con ID 1        → 200 OK
POST   /users        → Crear un usuario nuevo             → 201 Created
PUT    /users/1      → Actualizar usuario completo        → 200 OK
DELETE /users/1      → Borrar usuario con ID 1            → 204 No Content
```

**Buenas prácticas que enfatizó el profe:**

- Las rutas siempre llevan **nombres de recursos** (sustantivos), no verbos. `GET /users` — no `GET /obtenerUsuarios`.
- El verbo HTTP ya indica la acción — no necesitás ponerla en la ruta.
- Se usan los **códigos de estado HTTP** estándar para indicar el resultado.
- **REST "a rajatabla"** dice que no se mezclan recursos: si pedís un usuario, te devuelve datos del usuario pero no objetos anidados de otras entidades. Si necesitás el detalle de algo relacionado, hacés otro request. En la práctica, a veces se relaja esto por pragmatismo.

### Endpoint

Término que vas a escuchar constantemente. Un **endpoint** es una ruta específica de un servicio web a la que podés hacer requests. `GET /users` es un endpoint. `POST /users` es otro endpoint (misma ruta, distinto verbo).

### 🟢 JSON — Formato de intercambio

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

Soporta: strings, números, booleanos, arrays, objetos y null. No soporta `undefined` (eso es de JavaScript, no de JSON).

> **Para el parcial, si te preguntan:** *"¿Qué es REST y qué establece?"* — REST (Representational State Transfer) es un estilo de diseño de comunicación cliente-servidor. Establece: uso del protocolo HTTP, solicitudes stateless con toda la información necesaria, uso de verbos HTTP para las operaciones (GET, POST, PUT, DELETE), identificación de recursos mediante URIs, y JSON como formato de intercambio. Cada recurso tiene una ruta única y las operaciones se indican con el verbo HTTP, no en la ruta.

---

## Checkpoint

1. ¿Qué es el patrón Cliente-Servidor y quién inicia siempre la comunicación?
2. ¿Qué diferencia hay entre cliente liviano y cliente pesado? ¿Qué consecuencias tiene cada uno?
3. ¿Qué significa que HTTP es "sin estado" (stateless)?
4. ¿Cuáles son las tres partes de una request HTTP y las tres partes de una response?
5. ¿Qué métodos HTTP existen y cuáles llevan body?
6. ¿Qué diferencia hay entre un error 4xx y un error 5xx? ¿Y entre 401 y 403?
7. ¿Qué diferencia hay entre HTTP y HTTPS?
8. ¿Qué es una API? ¿Siempre implica un servicio web remoto?
9. ¿Qué es REST y qué establece como convenciones?
10. ¿Por qué las rutas REST usan sustantivos y no verbos?

---

**Fin de la Parte 1 — Clase 2**
