# Apunte Resumen — Clase 02
## Cliente-Servidor, HTTP, API REST

> Destilación del apunte maestro de la clase 02 (01/04/2026). **Cobertura total, sin omisiones.** Para repetición espaciada.

---

## 🧭 Leyenda

**Sistema 1 — importancia:** 🔴 central-evaluable · 🟡 secundario · 🟢 mencionado al pasar
**Sistema 2 — uso:** 🎯 esencial para aplicar (TPA, código, diseño) · 📘 contexto para el parcial

*Leyendo solo los 🎯 tenés un machete improvisado.*

**El hilo de la clase:** Cliente-Servidor (el patrón) → HTTP (el idioma) → Arquitectura Web (los dos andando) → API (la interfaz) → API REST (el estándar) → el código que lo consume.

**Recordá de la clase 1:** un **componente** es una unidad que hace algo y se comunica por una **interfaz** (contrato público: qué le podés pedir, sin saber cómo lo resuelve).

---

# PARTE 1 — Cliente-Servidor

## 1. Qué es 🔴 📘

❌ **No es un protocolo** (un protocolo es un idioma: HTTP, SMTP, IMAP).
✅ **Es un patrón arquitectónico.** Define **componentes** + **forma de conexión** + **vocabulario**. Decir "esto lo hacemos cliente-servidor" transmite todo eso sin explicar nada.

**Definición:** participan dos componentes. Un **servidor** que provee servicios a través de una **interfaz**, y un **cliente** que los **usa**.

```
                    Petición
   ┌──────────┐  ─────────────►  ┌──────────┐
   │  CLIENTE │      HTTP        │ SERVIDOR │
   └──────────┘  ◄─────────────  └──────────┘
                   Respuesta
```

El diagrama dice HTTP, pero podría decir otra cosa. **El patrón es independiente del protocolo** — lo único innegociable es que ambos hablen el mismo.

## 2. Las cuatro reglas 🔴 📘

1. **El cliente SIEMPRE inicia la comunicación. Nunca el servidor.**
2. El servidor **escucha en un puerto** determinado, esperando solicitudes.
3. Ciclo: **Solicitud → Procesamiento → Respuesta.** Cerrado.
4. El servidor puede atender **múltiples clientes simultáneamente**.

⚠️ **El matiz de la regla ①.** ¿Y las notificaciones push? **En HTTP/1.1 —que es lo que estudia la materia— el servidor NO puede iniciar.** Punto. Streaming, push y comunicación bidireccional existen, pero requieren **salir de HTTP/1.1** (HTTP/2, HTTP/3, WebSockets).

## 3. Ejemplos 🟢 📘

| Cliente | Servidor | Protocolo |
|---|---|---|
| Navegador (Chrome) | Servidor web (Apache, Nginx) | HTTP |
| App de home banking | Servidor del banco | HTTP |
| Cliente de email (Outlook) | Servidor de correo | **SMTP / IMAP** |
| App de delivery | Backend del sistema | HTTP |
| Videojuego online | Servidor del juego | (propio) |

**Dos cosas:** el cliente **no es "el navegador"** — es cualquier componente que inicia y consume (un backend que le pega a otro backend es cliente de ese otro). Y **el protocolo cambia** (fila de Outlook).

**Cómo identificarlo en un diagrama:** el que **expone** hace de servidor; el que **consume** hace de cliente. **Son roles, no identidades:** tu backend es servidor del frontend y cliente de la base de datos.

## 4. Clasificación según responsabilidades 🟡 📘

Eje: **dónde vive la lógica de negocio.**

| Tipo | Lógica | Nombre | Ejemplo |
|---|---|---|---|
| **Cliente activo, Servidor pasivo** | Casi toda en el cliente; el servidor solo **persiste** | — | ERP de escritorio viejo: la app corre en la PC, el servidor es solo la BD |
| **Cliente pasivo, Servidor pasivo** | Poca en ambos: **componentes intermedios** de algo más grande | — | Componente de integración que trae datos de otro sistema |
| **Cliente pasivo, Servidor activo** | Casi toda en el servidor; el cliente solo **presenta** | **Cliente liviano** | Web tradicional: el servidor arma el HTML |
| **Cliente activo, Servidor activo** | **Distribuida**: el cliente tiene presentación **e interacción** | **Cliente pesado** | SPA con React o Angular |

⚠️ **Textual del profe: "este sistema NO es una pregunta de parcial".** No memorices la tabla. Es **vocabulario para justificar**, no contenido evaluable en sí.

🔴 **Lo que sí importa — la consecuencia de mover la lógica.** Más lógica en el cliente → ↑ procesamiento en el dispositivo, ↑ batería, ↑ memoria, ↓ carga del servidor. Y al revés.

⚠️ **El tipo de cliente NO lo define la aplicación: lo definen las decisiones de diseño y las tecnologías.** El mismo home banking, resuelto por dos equipos, puede ser liviano en uno y pesado en el otro.
**Señal fuerte: si usa React o Angular → cliente pesado.**

🕳️ **Madriguera — Cliente liviano/pesado en serio.** Cómo se genera la vista, template engines, SPA. *Cae en las clases 23 y 25.*

## 5. Ventajas y desventajas del cliente liviano 🔴 🎯 📘

Cada una **tiene nombre de atributo de calidad**, y ese nombre es el que hay que usar.

**✅ Ventajas**
- **Mantenibilidad** — *cambios centralizados.* Actualizás el servidor, el usuario aprieta F5. (Con lógica instalada en cada cliente, tendrías que distribuir la actualización a cada máquina.)
- **Seguridad** — *control de acceso centralizado.* Un solo componente que proteger; lógica distribuida = más superficie de ataque.

**❌ Desventajas** *(considerando un único servidor)*
- **Eficiencia (tiempo de respuesta)** — el servidor es un **cuello de botella**. Más solicitudes concurrentes → tiempos que suben → en algún punto deja de responder.
- **Disponibilidad** — **único punto de falla.** Se cae y no hay sistema.

⚠️ **"Considerando un único servidor" no es un detalle: es la puerta al resto de la materia.** Las dos desventajas existen *porque* hay uno solo.

## 6. El razonamiento que la materia evalúa 🔴 🎯 📘

**Los atributos de calidad se pisan entre sí.**

```
   Agrego un segundo servidor y balanceo la carga
       ├──► ✅ gano EFICIENCIA      (la carga se reparte)
       ├──► ✅ gano DISPONIBILIDAD  (si uno cae, responde el otro)
       ├──► ❌ pierdo MANTENIBILIDAD (más nodos = más difícil de mantener)
       └──► ❌ sube el COSTO         (decisión de negocio, no técnica)
```

> 🔴 **LA idea de la clase:** **no existe una arquitectura "mejor". Existe una que privilegia unos atributos a costa de otros.** Mejorar uno casi siempre empeora otro. El trabajo del diseñador no es optimizar todo —es imposible— sino **elegir qué sacrificar y justificar por qué.**

**El único punto de falla es tu eslabón más débil.** *(Casa con rejas en todas las ventanas menos una: tu seguridad es la de esa ventana.)*

```
   ❌ Duplico el servidor de aplicación... y dejo UNA sola base de datos.
      [App 1] ─┐
               ├─► [ BD ]   ← el punto de falla no desapareció: SE MUDÓ.
      [App 2] ─┘
```
**Regla:** cualquier componente sin redundancia es, por sí solo, un punto único de falla.

### 🔴 El molde de toda respuesta correcta en esta materia

Textual: *"La materia es de aplicación. Resolveme cómo lo vas a hacer, y justificame por qué lo hacés así."*

```
   "Pongo [la lógica / el componente] ACÁ,
    porque priorizo [atributo de calidad],
    y estoy aceptando pagar con [otro atributo de calidad]."
```

---

## 📌 Para el parcial, si te preguntan

**"¿Qué es el patrón cliente-servidor?"**
Es un patrón arquitectónico en el que participan dos componentes: un servidor que provee uno o más servicios a través de una interfaz, y un cliente que consume esos servicios. El cliente siempre inicia la comunicación; el servidor escucha en un puerto y responde. No es un protocolo: define componentes, forma de conexión y vocabulario, y puede implementarse sobre distintos protocolos (HTTP, SMTP, IMAP).

**"¿Cuáles son las ventajas y desventajas de un cliente liviano?"**
Ventajas: **mantenibilidad**, porque los cambios de funcionalidad quedan centralizados en el servidor y no hay que distribuir actualizaciones a cada cliente; y **seguridad**, porque el control de acceso a los recursos está centralizado en un único componente. Desventajas, considerando un único servidor: **eficiencia**, porque el servidor puede convertirse en un cuello de botella al aumentar las solicitudes concurrentes; y **disponibilidad**, porque constituye un único punto de falla.

**"Se te cae el sistema por sobrecarga. ¿Qué hacés y qué estás sacrificando?"**
Agrego servidores y balanceo la carga entre ellos, ganando eficiencia (la carga se reparte) y disponibilidad (deja de haber un único punto de falla). A cambio pierdo mantenibilidad, porque aumenta la cantidad de nodos a administrar, y aumenta el costo de infraestructura. La decisión de asumir ese costo es de negocio, no técnica.

**"¿Un home banking es cliente liviano o pesado?"**
Depende de las decisiones de diseño y las tecnologías con las que se lo haya resuelto, no del tipo de aplicación. Si la lógica de presentación e interacción corre en el cliente —por ejemplo, con React o Angular— es un cliente pesado; si el servidor genera la vista y el cliente solo la muestra, es liviano. Dos bancos pueden resolver el mismo producto de las dos formas.

---

# PARTE 2 — Protocolo HTTP + Arquitectura Web

## 1. Qué es HTTP 🔴 📘

> **HTTP** — *HyperText Transfer Protocol*. Se diseñó para transferir **hipertexto**: texto con **hipervínculos** (links). Hoy sigue siendo el protocolo principal de la web.

**La ficha técnica — hay que recitarla:**

| Característica | Consecuencia |
|---|---|
| **Capa de aplicación** (capa 7, modelo OSI) | Lo usan las aplicaciones para comunicarse |
| **Sincrónico** | Pedís, esperás, te responden |
| **Sin estado** *(stateless)* | **No recuerda nada de la solicitud anterior** |
| **Cliente / Servidor** | Implementa el patrón de la Parte 1 |
| **Solicitud / Respuesta** | Un pedido, una respuesta, y se cerró |
| **Puertos 80 / 443** | 80 = HTTP · 443 = HTTPS |

**Métodos principales:** `GET` · `POST` · `PUT` · `DELETE` *(+ `PATCH`)*.
Especificación: **RFC 2616** (HTTP/1.1).
⚠️ Las slides dicen "RFC-216" — es un typo; el correcto es **2616**.

**Recordá — modelo OSI:** modelo de capas de la comunicación en red. Abajo, la capa física (el cable); arriba, la **capa 7 o de aplicación**, donde viven HTTP, SMTP (envío de mails), IMAP/POP3 (recepción), MQTT. Más abajo trabajan TCP, IP, UDP. **En DSI solo importa la capa 7.**

## 2. Sincrónico y sin estado 🔴 🎯 📘

**Sincrónico:** mandás una solicitud y **esperás**. Bloqueante, uno a uno.
Límite: **con HTTP/1.1 no hay streaming.** Para un chat tendrías que preguntar cada segundo si hay algo nuevo (*polling*).

🕳️ **Madriguera — Comunicación asincrónica.** *Cae en la clase 7 (sincronismo/asincronismo, cron tasks) y 12 (broker, RabbitMQ). gRPC y GraphQL, clase 30.*

### 🔴🔴 Sin estado (*stateless*) — la palabra más importante de la clase

> **HTTP no recuerda absolutamente nada de las solicitudes anteriores.** Cada request es un desconocido tocando la puerta por primera vez.

```
   Cliente:   GET /clientes/5      → Servidor: 200 OK { id:5, nombre:"Juan" }
   Cliente:   GET /facturas        → "dame las del cliente que te pedí recién"
   Servidor:  ¿QUÉ cliente? 🤷      → no tiene forma de saberlo

   ✅ GET /clientes/5/facturas      → toda la info, otra vez
```

> 🔴 **Consecuencia:** **cada solicitud debe llevar TODA la información necesaria para ser procesada.** El servidor no completa nada con memoria, porque no tiene.

De acá sale **media materia**: el diseño de rutas REST, los tokens, las cookies, las sesiones.

⚠️ **El matiz que preguntaron:** *"pero HTTP va sobre TCP, y TCP retransmite lo perdido — entonces sí hay estado"*. **Correcto, y no contradice nada. Son dos estados distintos:**

| Estado | Quién lo maneja |
|---|---|
| **De la conexión** (¿se perdió un paquete? ¿retransmito?) | **TCP**, la capa de abajo. Sí lo maneja. |
| **De la operación** (¿qué me pediste antes?) | **NADIE.** Ese es el punto. |

**HTTP delega el estado de la conexión a TCP; el de la operación no lo maneja nadie.**

**Recordá — TCP:** protocolo de la capa de transporte que garantiza que los datos lleguen completos y en orden, retransmitiendo lo perdido.

## 3. Anatomía de una request 🔴 🎯 📘

**Tres partes. Siempre.**

```http
GET /usuarios HTTP/1.1                          ← ① LÍNEA DE INICIO: Método + URL + Versión
Host: api.miapp.com                             ← ② HEADERS (metadatos)
Content-Type: application/json
Authorization: Bearer <token>

{ "nombre": "Juan", "email": "juan@mail.com" }  ← ③ BODY (los datos)
```

**② Headers** = datos *sobre* el pedido, no el pedido. `Host` (a qué servidor), `Content-Type` (en qué formato mando), `Authorization: Bearer <token>` (la credencial).

> 🔴 **Un token NO es cifrado.** Son cosas distintas:
>
> | | Qué protege |
> |---|---|
> | **Token** | **El recurso.** Quién puede pedirlo. |
> | **HTTPS** | **El viaje.** Que nadie lea los datos en el camino. |
>
> **Token por HTTP (sin S) = viaja en texto plano y cualquiera lo lee.** En producción se usan **las dos cosas**.

🕳️ **Madriguera — Autenticación y autorización.** Qué es un token, *Bearer*, autenticar vs autorizar. *Cae en la clase 14 (Seguridad).*

**③ Body** — ⚠️ **solo en `POST`, `PUT` y `PATCH`**. Es decir: **solo cuando mandás algo.** En `GET` y `DELETE` no hay body: la identificación del recurso va en la URL.

## 4. Anatomía de una response 🔴 🎯 📘

**Simétrica. También tres partes.**

```http
HTTP/1.1 200 OK                                        ← ① LÍNEA DE ESTADO: Versión+Código+Descripción
Content-Type: application/json                         ← ② HEADERS
Content-Length: 342

{ "id": 1, "nombre": "Juan", "email": "juan@mail.com" } ← ③ BODY
```

`Content-Type` te dice **qué te devuelve**: `application/json`, `text/html` (una página), un PDF, una imagen.

## 5. Los métodos HTTP 🔴 🎯

| Método | Qué hace | ¿Body? |
|---|---|---|
| **GET** | **Obtiene** un recurso o listado | ❌ |
| **POST** | **Crea** un recurso nuevo | ✅ los datos |
| **PUT** | **Modifica** un recurso **completo** | ✅ el recurso entero |
| **PATCH** | **Modifica** **parcialmente** | ✅ solo los campos a cambiar |
| **DELETE** | **Borra** un recurso | ❌ |

**PUT vs PATCH** 🟡 — **lo dice la norma:** todo → `PUT`; parcial → `PATCH`.
⚠️ *"Después cada uno hace lo que quiere"* — en la industria vas a ver `PUT` para todo. **El criterio de la materia es el de la norma.**

HTTP tiene más métodos (`HEAD`, `OPTIONS`); las slides listan los principales.

## 6. Códigos de estado 🔴 🎯 📘

| Familia | Significado | Los que vas a ver |
|---|---|---|
| **1xx** | Informativas | *(casi nunca)* |
| **2xx** | **Correctas** | `200` · `201` · `204` |
| **3xx** | **Redirecciones** | `302` |
| **4xx** | **Errores causados por el CLIENTE** | `400` · `401` · `403` · `404` · `405` |
| **5xx** | **Errores causados por el SERVIDOR** | `500` · `503` · `504` |

> 🔴 **La distinción clave (se discutió varios minutos en clase → se pregunta):**
>
> **Los códigos los genera SIEMPRE el servidor. El cliente nunca genera un código.**
> Lo que indica la familia **no es quién lo emite, sino de quién fue la culpa.**
>
> Un `404` **te lo manda el servidor**, pero es 4xx —del cliente— porque **el cliente pidió algo que no existe**. El servidor hizo exactamente lo que debía: avisarte.
> *(Que venga con una página bonita y un gatito no cambia el código: eso es solo el `body`.)*

| Código | Nombre | Cuándo |
|---|---|---|
| **200** | OK | Todo bien. El genérico de éxito. |
| **201** | Created | **Se creó un recurso.** La respuesta natural de un `POST`. |
| **204** | No Content | Salió bien **y no hay nada que devolver.** Natural de un `DELETE`. |
| **302** | Found (redirect) | El recurso se movió; te mandan a la ubicación nueva. |
| **400** | Bad Request | El cliente mandó algo mal formado o le faltó un parámetro obligatorio. |
| **401** | Unauthorized | **No estás autenticado.** No mandaste credenciales, o son inválidas. |
| **403** | Forbidden | **Estás autenticado, pero sin permiso** para ese recurso. |
| **404** | Not Found | El recurso no existe. |
| **405** | Method Not Allowed | El endpoint existe, pero no acepta ese método. |
| **500** | Internal Server Error | Algo explotó del lado del servidor. El genérico. |
| **503** | Service Unavailable | El servicio no está disponible. |
| **504** | Gateway Timeout | Se agotó el tiempo de espera. |

> ⚠️ **401 vs 403 — en la grabación quedaron dichos AL REVÉS.**
> El estándar (y el RFC que la cátedra referencia): **401 = falta autenticación** ("no sé quién sos") · **403 = falta autorización** ("sé quién sos, y no podés").
> El concepto se explicó bien; **los números se asignaron invertidos**. Es un lapsus, no un criterio: el material oficial no lo especifica.
> **Para el parcial: usá el estándar.** Confirmalo en la clase 14.

🔴 **Por qué importa:** en el TPA vas a tener **varios servicios hablándose entre sí**, escritos por distintos integrantes. Si todos respetan los códigos, **cada uno sabe qué esperar del otro sin preguntar**. Ese es el negocio de un estándar: eliminar la conversación.

⚠️ **Seguridad:** los `500` se devuelven **genéricos, sin detalle**, a propósito. Explicarle al cliente qué falló adentro le sirve a un atacante para entender tu estructura.

🕳️ **Madriguera — El 418.** `418 I'm a teapot` existe. Es un chiste de la especificación (1998). *Volvé al camino.*

## 7. HTTP vs HTTPS y los puertos 🔴 🎯 📘

| | **HTTP** | **HTTPS** |
|---|---|---|
| **Puerto** | 80 | **443** |
| **Seguridad** | **Sin cifrado** | **Cifrado TLS/SSL** |
| **Certificado** | No requerido | **Requerido** |
| **Uso recomendado** | **Solo desarrollo local** | **Producción, siempre** |

**Qué hace la S:** agrega una **capa de cifrado (TLS)**. Usuario y contraseña por HTTP viajan **en texto plano** — cualquiera que intercepte el tráfico los lee. Por HTTPS, ese tráfico es **ilegible**.

⚠️ **Nombre:** originalmente **SSL**, hoy deprecado; se llama **TLS**. Se sigue diciendo "certificado SSL" por costumbre.

**El certificado** valida la identidad del servidor y habilita el cifrado. Se renueva periódicamente; si vence, el navegador avisa ("sitio no seguro") y los buscadores penalizan.

📌 **TPA:** en el deploy a la nube, **la cátedra va a pedir los certificados implementados.** Local, no hace falta.

### Puertos 🟡 🎯

Número donde un servidor **escucha**. Del `0` al `1023` están **reservados** (80 = HTTP, 443 = HTTPS, 22 = SSH). De ahí para arriba, libres.

> 🔴 **La regla que te va a morder:** **en un mismo nodo (misma IP) NO pueden correr dos aplicaciones escuchando en el mismo puerto.**
>
> Cuando levantes dos servicios Spring Boot a la vez, el segundo **no arranca**: *"ese puerto ya está ocupado"*. No es un bug tuyo. Se cambia el puerto en el `application.yml`.

*(**Como cliente** sí podés tener muchas conexiones abiertas al 443 de distintos servidores. La restricción es para el que **escucha**.)*

## 8. Arquitectura Web: qué pasa al entrar a una URL 🔴 📘

```
        ① request                     ② request
   ┌─────────┐ ─────────►  ╭───────────╮ ─────────►  ┌──────────┐
   │   web   │             │  Internet │             │   web    │
   │ browser │ ◄─────────  │   (nube)  │ ◄─────────  │  Server  │
   └─────────┘  response ④ ╰───────────╯  response ③ └──────────┘
```

**La nube es una caja negra a propósito.** Qué hay en el medio, cómo se rutea → Comunicación de Datos y Redes. Acá trabajamos en capa de aplicación.

**Los 8 pasos:**

```
 1. El usuario escribe https://www.google.com
 2. El DNS traduce el dominio a una dirección IP
    → DNS (Domain Name Server): convierte nombres en IPs. Tenés uno primario y uno
      secundario configurados. Si ya entraste antes, sale de una caché local.
 3. El navegador establece una conexión TCP con el servidor
 4. Se negocia el cifrado TLS  (solo si es HTTPS)
 5. El navegador envía un HTTP GET /        ← el "/" es la raíz. Y es un GET.
 6. El servidor procesa y devuelve el HTML  ← Content-Type: text/html
 7. El navegador descarga CSS, JS e imágenes adicionales
    → El HTML es SOLO el esqueleto. Trae adentro una LISTA de recursos.
      Cada uno es OTRA request HTTP.  ⚠️ Los cacheados NO se vuelven a pedir.
 8. La página se renderiza
```

> 🔴 **La pregunta de clase:** *"En este ejemplo, ¿qué tipo de cliente es?"*
> **Cliente liviano.** El servidor **arma la vista** (genera el HTML); el navegador solo la **renderiza**. La web tradicional **es** el caso "cliente pasivo – servidor activo".
> *(Una SPA con React sería lo contrario: el servidor manda datos crudos, el cliente arma la vista. Cliente pesado.)*

## 9. DevTools 🟡 🎯

`F12` o clic derecho → Inspeccionar. **Pestaña Network**: te muestra todo lo de esta parte, pasando de verdad.

| Lo que ves | Es esto |
|---|---|
| URL y método (`GET`) | §3 — línea de inicio |
| Código de estado (`200 OK`) | §6 |
| IP remota y puerto (`:443`) | §7 |
| `Content-Type: text/html; charset=UTF-8` | §4 — headers |
| La respuesta completa | §4 — body |
| **La cascada de recursos** | §8, paso 7 |

**Hacelo:** abrí cualquier sitio → `F12` → **Network** → `Ctrl+F5`. Vas a ver la request al HTML y después la lluvia de recursos. ⚠️ **En el TPA es tu herramienta de debug del lado cliente.**

---

## 📌 Para el parcial, si te preguntan

**"¿Qué significa que HTTP sea *stateless* (sin estado)?"**
Significa que el protocolo no conserva información de las solicitudes anteriores: cada request es independiente y el servidor no recuerda qué se le pidió antes. La consecuencia directa es que **cada request debe contener toda la información necesaria para ser procesada**. El estado de la conexión (retransmisión de paquetes perdidos) sí se maneja, pero lo maneja TCP, no HTTP.

**"¿Cuáles son las tres partes de una request HTTP?"**
Línea de inicio (método, URL y versión del protocolo), headers o cabeceras (metadatos de la solicitud, como `Host`, `Content-Type` o `Authorization`), y body o cuerpo (los datos enviados, presente únicamente en POST, PUT y PATCH).

**"¿Quién genera los códigos de estado? ¿Qué indica que un código sea 4xx?"**
Los genera siempre el servidor; el cliente nunca genera códigos de estado. Que un código sea 4xx no indica quién lo emite, sino **de quién fue la responsabilidad del error**: un 4xx señala que la causa fue del cliente (por ejemplo, un 404 al pedir un recurso inexistente), mientras que un 5xx señala una falla del servidor.

**"¿Qué diferencia hay entre HTTP y HTTPS?"**
HTTPS agrega una capa de cifrado (TLS/SSL) sobre HTTP: los datos viajan cifrados entre cliente y servidor, requiere un certificado y usa el puerto 443 en lugar del 80. HTTP transmite en texto plano, por lo que cualquiera que intercepte el tráfico puede leerlo. Se usa HTTP solo en desarrollo local; en producción, HTTPS siempre.

**"¿Es lo mismo mandar un token que usar HTTPS?"**
No: protegen cosas distintas. El token protege **el recurso** —determina quién está habilitado a solicitarlo—, mientras que HTTPS protege **el transporte** —impide que los datos sean legibles en el camino—. Un token enviado por HTTP viaja en texto plano y puede ser interceptado, por lo que en producción se usan ambos mecanismos en conjunto.

**"Contá qué pasa desde que escribís una URL hasta que ves la página."**
El DNS traduce el dominio a una dirección IP; el navegador establece una conexión TCP con el servidor y, si es HTTPS, negocia el cifrado TLS; luego envía una solicitud HTTP GET. El servidor procesa y devuelve el HTML, que contiene la lista de recursos adicionales (CSS, JS, imágenes) que el navegador solicita a continuación —salvo los que ya tenga en caché— y finalmente renderiza la página.

---

# PARTE 3 — API y API REST

## 1. Qué es una API 🔴 📘

> **API** — *Application Programming Interface*. Conjunto de herramientas, definiciones y protocolos que permite que un producto se comunique con otros **sin necesidad de saber cómo se implementan internamente**.

**Recordá de la clase 1:** un componente expone una **interfaz** (qué le podés pedir, ocultando cómo lo resuelve). **Una API es exactamente eso, escrito de forma concreta.**

### 🔴 El error nº 1: creer que toda API es web

**Ninguno de los cinco ejemplos de la cátedra es un servicio remoto:**

| API | Qué expone | ¿Otro nodo? |
|---|---|---|
| **Stream API de Java** | Métodos para manipular flujos de datos | ❌ Es una biblioteca |
| **JPA** | El estándar de mapeo objeto-relacional | ❌ Una dependencia |
| **JDBC** | Clases para hablar con bases relacionales | ❌ (aunque la BD sí esté afuera) |
| **Win32 API** | Ventanas, archivos, procesos, memoria | ❌ Es el sistema operativo |
| **POSIX API** | Archivos, procesos, concurrencia | ❌ |

> **API** es genérico: **cualquier interfaz que un componente expone.** Puede ser una biblioteca, un framework, el SO, o un servicio remoto.
> **API REST es UN caso particular:** la interfaz **es web y distribuida** — se expone por HTTP, y quien la consume corre en otra máquina.

## 2. Qué es REST 🔴 📘

> **REST** — *REpresentational State Transfer*. Un **estilo de diseño de comunicación** entre cliente y servidor.

### ⚠️ Lo que la cátedra remarca — y hay que responder así

Textual: *"REST **no es un estilo arquitectónico**, ni tampoco **es un patrón de diseño**. Es una forma particular de diseñar la comunicación cliente-servidor, a través de una serie de criterios."*

⚠️ **En la literatura (Fielding) a REST SÍ se lo llama estilo arquitectónico.** Es una divergencia real de terminología.
**Para el parcial: respondé con la cátedra.** REST = **estilo de diseño de comunicación**, definido por criterios. **No** es estilo arquitectónico ni patrón. **El profe es quien evalúa.**

### Para qué sirve, en una frase

> **REST define un estándar para que dos partes se pongan de acuerdo sin negociar nada.**

Decir *"te expongo una API REST"* transmite, gratis: **HTTP** · **verbos HTTP** · **códigos de estado HTTP** · **recursos identificados por URI** · **JSON**. Cinco acuerdos, con dos palabras.

## 3. Los criterios de REST 🔴 🎯 📘

**① Se apoya sobre HTTP.** No inventa nada.

**② Cada request es autocontenida.** 🔴
*"Un REQUEST tiene toda la información necesaria y solo espera una RESPONSE."*
**No es una regla nueva: es la consecuencia de que HTTP sea *stateless*.** REST no pelea contra esa característica — **la abraza y la convierte en principio de diseño**.

```
   ❌ GET /clientes/5   →   GET /facturas     ← el servidor NO SABE de qué cliente hablás
   ✅ GET /clientes/5   →   GET /clientes/5/facturas
```
**Proceso: solicitud → respuesta → FIN.** ¿Querés otra cosa? **Otra solicitud completa.**

**③ Se apoya en los verbos HTTP.**

| Verbo | Uso en REST |
|---|---|
| **POST** | Crear recursos nuevos |
| **GET** | Obtener un listado o un recurso concreto |
| **PUT** | Modificar un recurso (completo) |
| **PATCH** | Modificar un recurso (parcial) |
| **DELETE** | Borrar un recurso |

**④ Los recursos se identifican mediante URI.** 🔴
⚠️ **URI ≠ URL.** La **L** de URL es de *Locator* (**ubicación**); la **I** de URI es de *Identifier* (**identificador**). En REST **identificamos** el recurso.
**Recurso** = cualquier entidad del sistema (usuarios, ventas, facturas, donaciones). **Cada una con su identificador unívoco en la ruta:** `GET /clientes/5`.

**⑤ Formato de intercambio: JSON (o XML).** JSON está **en negrita en la slide**: es el default.
**¿Por qué JSON?** El **stream es más liviano**: XML repite cada etiqueta dos veces (apertura y cierre), JSON no.

## 4. JSON 🟡 🎯

> **JSON** — *JavaScript Object Notation*. Formato de texto liviano y legible. **Simple para un humano, simple para una máquina.**

**Seis tipos:** strings, números, booleanos, arrays, objetos, `null`.

```json
{
  "id": 42,                        ← número
  "nombre": "María García",        ← string (entre comillas)
  "activo": true,                  ← booleano (SIN comillas — con comillas sería un string)
  "roles": ["admin", "usuario"],   ← array
  "direccion": {                   ← objeto ANIDADO
    "ciudad": "Buenos Aires",
    "pais": "Argentina"
  }
}
```

⚠️ **Dos trampas:**
1. **`true` sin comillas = booleano. `"true"` con comillas = string.** En Java te va a romper.
2. **JSON no tiene `undefined`** (eso es de JavaScript). **Acepta `null`**, y nada más.

## 5. Rutas REST 🔴 🎯 📘

Recurso `User`:

| Ruta | Qué hace | Código |
|---|---|---|
| `GET /users` | Obtiene **todos** | `200 OK` |
| `GET /users/1` | Obtiene **el** usuario 1 | `200 OK` |
| `POST /users` | **Crea** *(datos en el body)* | **`201 Created`** |
| `PUT /users/1` | **Actualiza** *(datos en el body)* | `200 OK` |
| `DELETE /users/1` | **Borra** | **`204 No Content`** |

> 🔴 **La gracia entera de REST:** `GET /users/1` y `PUT /users/1` tienen **la misma ruta**. Lo único que cambia es **el verbo**.
> **La ruta identifica el recurso; el verbo dice qué le hacés.**

**Por eso está mal:**
```
   ❌ GET  /users/obtenerUsuarioPorId/1
   ❌ POST /users/actualizarUsuario
   ❌ GET  /users/borrar/1
```
Meten el verbo en el nombre. **Y no hace falta: el verbo ya está en HTTP.**

## 6. Buenas prácticas REST 🔴 🎯 📘

1. **Las rutas llevan nombres de RECURSOS** (sustantivos): `/users`, `/donaciones`, `/facturas`.
2. **No se usan verbos como nombres de rutas** — la acción la indica el verbo HTTP.
3. **Se usan los códigos de estado HTTP** para indicar cómo terminó. No devuelvas `200 OK` con `{"error": "no encontrado"}` adentro: **devolvé `404`**.

**Por qué importa más allá de la nota:** en el TPA hay **varios servicios hablándose**, escritos por distintos integrantes. Con el estándar, **cada uno sabe qué esperar del otro sin preguntar**. Sin él, cada integración es una negociación.

## 7. REST "a rajatabla": el trade-off 🔴 🎯 📘

**El problema.** Pedís un alumno:
```
GET /alumnos/12345
→ { "nombre": "Ever", "cursadas": [101, 205, 307, 412] }   ← SOLO LOS IDs
```
¿Querés el detalle? **Una request por cada uno.** Si cursó 40 materias → **40 requests.**

🔴 **La regla que lo causa:** *"Si respetás REST a rajatabla, **no podés mezclar entidades**."* Cada recurso vive en su ruta y devuelve **lo suyo**. Las cursadas son **otro recurso**, con su propia URI.

**Nombre en la industria: el problema N+1.** Una request para la lista, N para el detalle.

**El matiz honesto:** *"Tampoco hace falta ser tan puristas."* En la industria se rompe la regla todo el tiempo, con buen criterio. Por eso existe la distinción **REST** vs **RESTful**: *cuán* REST es lo que implementaste.

> 🔴🔴 **PERO — la regla del TPA:** **"El TP lo respetamos a rajatabla. NO mezclar entidades."** Instrucción directa, no sugerencia.

🕳️ **Madriguera — Lo que resuelve el N+1.** Se llama **GraphQL**: agrega recursos, permite campos dinámicos, elimina el ida y vuelta. Alternativa a REST, no reemplazo. *Cae en la clase 30 — y el profe deslizó que **puede aparecer en el TPA**.*

📌 **Es exactamente el tipo de pregunta que la materia evalúa:** *"¿Qué desventaja tiene REST y cómo la resolverías?"* — el razonamiento de trade-offs de la Parte 1, aplicado a un estándar.

## 8. Tipos de API 🟡 📘

| | **Abierta** | **Cerrada** |
|---|---|---|
| **Gratuita** | ✅ Sin registro, sin pagar | Te registrás, no pagás |
| **Paga** | *(raro)* | Te registrás **y** pagás |

- **Abierta** = **no necesitás credencial** ni registro.
- **Cerrada** = necesitás registrarte y obtener una credencial (*API key* o token).
- **Gratuita / Paga** = si pagás o no.

⚠️ **No confundas los ejes.** Puede ser **cerrada y gratuita**: te registrás, te dan una clave, no pagás. Es el caso más común de las APIs públicas serias (te registran para limitarte el uso).

## 9. Documentación: Swagger y OpenAPI 🔴 🎯

**Sin documentación no podés consumir una API.** Necesitás saber qué rutas hay, qué parámetros aceptan, qué devuelven y con qué códigos.
**El problema:** cada sitio documenta como quiere. **No hay dos iguales.**

> **OpenAPI** es el **estándar** (versión actual: **3.0**). **Swagger** es la **herramienta que lo renderiza**: documentación ordenada y navegable.

```
   GET  /name/{name}      → "busca un país por nombre"
        ├── parámetro:  name (string, en la ruta)
        ├── ejemplo:    /name/Argentina
        └── respuestas: 200 → el país encontrado
                        404 → no existe
```

> 🔴🔴 **Por qué es 🔴 y no 🟡:**
> **"En el TP van a tener que consumir una API, y se les va a dar la documentación en Swagger."**
> **"Y cuando expongamos nuestras propias APIs, las vamos a documentar en Swagger."**
>
> **Es una herramienta del TPA en las dos direcciones: leer y escribir.**

---

## 📌 Para el parcial, si te preguntan

**"¿Qué es una API?"**
Es una interfaz de programación de aplicaciones: un conjunto de definiciones y protocolos que un componente expone para que otros lo utilicen sin necesidad de conocer cómo está implementado internamente. **No implica que sea distribuida ni web**: la Stream API de Java, JDBC o la API del sistema operativo también son APIs. Una API REST es un caso particular, en el que la interfaz se expone por HTTP y se consume de forma remota.

**"¿Qué es REST?"**
Es un estilo de diseño de comunicación entre cliente y servidor, definido por un conjunto de criterios: se apoya sobre HTTP, cada solicitud contiene toda la información necesaria para ser procesada, utiliza los verbos HTTP para las operaciones, identifica los recursos mediante URI y usa JSON (o XML) como formato de intercambio. **No es un estilo arquitectónico ni un patrón de diseño.**

**"¿Por qué en REST se dice que la solicitud debe ser autocontenida?"**
Porque REST se apoya sobre HTTP, que es un protocolo sin estado: el servidor no conserva información de las solicitudes previas. En consecuencia, cada request debe incluir toda la información necesaria para que el servidor pueda procesarla y responder, sin depender de ningún contexto anterior.

**"Nombrá tres buenas prácticas de diseño de rutas REST."**
Las rutas deben llevar nombres de recursos (sustantivos); no deben usarse verbos en el nombre de la ruta, porque la acción ya la indica el verbo HTTP; y debe utilizarse el código de estado HTTP para indicar cómo finalizó la operación, en lugar de comunicarlo en el cuerpo de la respuesta.

**"¿Qué desventaja tiene REST y cómo la resolverías?"**
Respetado estrictamente, REST no permite mezclar entidades: un recurso devuelve únicamente su propia información, con lo cual obtener datos relacionados exige una solicitud adicional por cada uno (el problema N+1). Se resuelve con tecnologías que permiten agregación de recursos y selección dinámica de campos, como GraphQL, que devuelven en una sola llamada lo que REST resolvería en varias.

**"¿Qué es Swagger?"**
Es la herramienta que renderiza documentación de APIs escrita según el estándar OpenAPI. Permite documentar de forma estructurada las rutas expuestas, sus parámetros, los ejemplos de uso y los códigos de estado que puede devolver cada una, de modo que quien consume la API sepa exactamente qué puede pedir y qué va a recibir.

---

# PARTE 4 — Práctica: consumir una API REST

## 1. La API del ejemplo 🟡 🎯

**REST Countries** (`restcountries.com/v3.1`): 250+ países. **Abierta y gratuita.**
**Nosotros somos los clientes.** Ese componente expone una interfaz REST; nos comunicamos por HTTP con sus verbos y rutas.

**Endpoint** = una ruta concreta que el servicio expone y a la que podés llamar.

```
GET /all                 → todos los países
GET /name/{nombre}       → busca por nombre
GET /currency/{codigo}   → busca por código de moneda
GET /region/{region}     → busca por región
GET /capital/{capital}   → busca por capital
```

## 2. Consumir desde el navegador 🔴 🎯 📘

```
https://restcountries.com/v3.1/currency/ars
```

`F12` → **Network** → ves: Request URL · **Method: GET** · **Status: 200 OK** · Remote Address `:443` (**HTTPS**) · `Content-Type: application/json`.

> 🔴 **La conclusión:** **cada vez que escribís algo en la barra del navegador y apretás Enter, hacés una solicitud HTTP de tipo `GET`.** Siempre. **El navegador es un cliente HTTP.**

> 🔴 **Su limitación — y la razón por la que existe Postman:**
> **Desde la barra solo podés hacer `GET`.** Para un `POST` necesitás un **formulario HTML** o **código JavaScript** ejecutándose en la página. Un `PATCH` o `DELETE`, ni con eso.
> **El navegador se te queda corto apenas querés probar algo que no sea leer.**

## 3. Consumir desde Postman 🔴 🎯

> **Postman** = un **cliente HTTP**. Un programa que arma cualquier request —cualquier método, header y body— y te muestra la respuesta completa.

| Elemento | Para qué |
|---|---|
| **Método** | GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS. **Acá sí podés.** |
| **URL** | La ruta |
| **Solapa Authorization** | Si la API es **cerrada**: la credencial |
| **Solapa Headers** | Los headers (Parte 2 §3) |
| **Solapa Body** | Los datos. **Solo en POST, PUT, PATCH.** |
| **Send** | Dispara |
| **Panel de respuesta** | Código, tiempo en ms, tamaño, body |

**La interfaz de Postman ES la anatomía de una request HTTP.** Método, URL, headers, body.

### 🔴 Path params vs Query params

**Path param — identifica un recurso.** Va **dentro de la ruta**:
```
GET  .../v3.1/name/{name}        ← placeholder
GET  .../v3.1/name/argentina     → Argentina
GET  .../v3.1/name/uruguay       → Uruguay
```
**La ruta cambia según el recurso.** Es el criterio ④ de REST.

**Query param — filtra o modifica la consulta.** Después del `?`, formato **clave=valor**, encadenados con `&`:
```
GET  .../v3.1/all?fields=name,flags&currency=ars
                 ↑              ↑  ↑
                 │              │  └── separa: &
                 │              └───── valor
                 └── acá empiezan los query params
```
**El recurso es el mismo (`/all`); cambia qué te traés de él.**

| | **Path param** | **Query param** |
|---|---|---|
| Dónde | En la ruta: `/name/argentina` | Después del `?`: `?fields=name` |
| Para qué | **Identifica** un recurso | **Filtra o modifica** la consulta |
| Formato | Parte del path | `clave=valor`, con `&` |

### 🔴 El 400 en vivo

Quitaron `fields` y llamaron a `/all` pelado:
```
400 Bad Request
{ "message": "'fields' query parameter is required" }
```
**Esa API exige `fields`** — devolver los 250 países completos le costaría carísimo.

> 🔴 **La lección:** *"Cada API es distinta. **Siempre tenés que leer su documentación.**"* Esta acepta filtrar campos; otra no. **No hay reglas universales más allá del estándar REST.**

## 4. Consumir desde Java + Spring Boot 🔴 🎯

> **Spring Boot** = el **framework** de toda la materia (prácticas y TPA). Un **framework** te da el esqueleto ya armado: vos escribís las piezas, y **él las crea, las conecta y las ejecuta**. Distinto de una biblioteca, donde **vos** llamás al código de otro; acá **el framework llama al tuyo**.

### 4.1 Spring Initializr 🟡 🎯

`start.spring.io` genera el esqueleto. Lo que se eligió en clase, y lo que vas a elegir en el TPA:

| Campo | Valor |
|---|---|
| **Lenguaje** | Java |
| **Gestor de dependencias** | **Maven** |
| **Spring Boot** | **4.0.5** |
| **Java** | **21** |
| **Group** | `ar.edu.utn.ba.ddsi` *(Argentina · educativo · UTN · Buenos Aires · DDSI)* |
| **Configuración** | **YAML** (`.yml`), no `.properties` |
| **Dependencias** | **Spring Web MVC** · **Configuration Processor** · **DevTools** *(+ Lombok)* |

🔴 **Regla dura del TPA: todos los integrantes usan la misma versión de Java (21).** Con versiones distintas, el proyecto no compila igual para todos.

### 4.2 Estructura del proyecto 🔴 🎯

```
proyecto/
├── .idea/          ← ❌ NO SE SUBE. Config de IntelliJ, local tuya.
├── .mvn/           ← ✅ sí. Config de Maven, general del proyecto.
├── src/            ← ✅ ACÁ VA TU CÓDIGO
│   ├── main/       ←    java/ (las clases) · resources/ (application.yml)
│   └── test/       ←    los tests
├── target/         ← ❌ NO SE SUBE. Código Java compilado.
└── pom.xml         ← ✅ el archivo de Maven: las dependencias
```

> 🔴 **`.idea/` y `target/` van al `.gitignore`.** El profe lo dijo **dos veces** y aclaró *"primera y última vez que lo digo"* — o sea: **lo va a mirar en las entregas.**
> **`.idea/`** = config de *tu* IntelliJ → conflictos de merge constantes entre compañeros.
> **`target/`** = código **compilado** → se regenera solo. Subirlo es subir basura.

**`pom.xml`** = el archivo de **Maven** (gestor de dependencias). Es **el `package.json` de Java** (con Maven donde iría npm). Agregás dependencia → ícono de Maven → **Refresh**.

### 4.3 El código 🔴 🎯

**Orden de construcción** *(no es casual — es el orden en que conviene pensarlo)*:
```
① La interfaz  → qué métodos expongo     (el CONTRATO)
② El molde     → qué forma tiene el JSON (los DATOS)
③ El uso       → cómo se va a usar       (el TEST)
④ La conexión  → la llamada HTTP         (lo tedioso, al final)
```

#### ① El componente y su interfaz

```java
package ar.edu.utn.ba.ddsi.countries.services;

import org.springframework.stereotype.Component;

@Component
// ↑ "Spring: esta clase es tuya — instanciala vos al arrancar y guardala."
//   ⚠️ NUNCA hacemos `new BuscadorDePaises(...)`. Lo hace el framework.
public class BuscadorDePaises {

    // La INTERFAZ del componente = sus métodos públicos. El contrato de la clase 1.
    public List<Pais> buscarTodos() { ... }
    public Optional<Pais> buscarPorNombre(String nombre) { ... }
    public List<Pais> buscarPorMoneda(String codigoMoneda) { ... }
    public List<Pais> buscarPorRegion(String region) { ... }
    public List<Pais> buscarPorCapital(String capital) { ... }
}
```

🔴 **`Optional<Pais>` vs `List<Pais>` — es diseño, no sintaxis:**
- **`buscarPorNombre` → `Optional`**: un nombre identifica **un solo país**. Existe o no existe. `Optional` es la forma explícita de decir *"esto puede no venir"*.
- **`buscarPorMoneda` → `List`**: **muchos países comparten una moneda** (el euro, veinte). Colección por naturaleza.

**La firma comunica el contrato**: se sabe qué esperar sin abrir el método.

#### ② Los moldes: DTOs

**La diferencia que Java te impone y JavaScript no.** En JS hacés `await res.json()` y ya tenés el objeto. **En Java, fuertemente tipado, tenés que declarar de antemano la forma del JSON** — si no, ni compila.

**DTO** (*Data Transfer Object*) = clase **sin lógica**, cuyo único trabajo es **transportar datos**. En clase: *"objetos moldes"*.

**El JSON que llega** *(recortado a lo que pedimos)*:
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

**El molde que lo espeja:**
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
// ↑ "Si el JSON trae campos que no declaré, IGNORALOS."
//   ⚠️ Sin esto, un campo desconocido lanza EXCEPCIÓN.
//   Es el seguro contra que la API agregue campos sin avisarte.

@Data                  // ← Lombok: getters Y SETTERS
@NoArgsConstructor     // ← Lombok: constructor VACÍO
@AllArgsConstructor    // ← Lombok: constructor con todos los parámetros
//   ⚠️ Las tres NO son opcionales. Ver el "¿cómo funciona?" abajo.

public class Pais {

    @JsonProperty("name")            // el JSON lo llama "name"...
    private NombrePais nombre;       // ...yo "nombre". Es OTRO DTO (objeto anidado).

    @JsonProperty("capital")
    private List<String> capitales;  // array de strings → List<String>

    @JsonProperty("region")
    private String region;

    @JsonProperty("population")
    private Long poblacion;

    @JsonProperty("currencies")
    private Map<String, DetalleMoneda> monedas;
    // ↑ Map, porque la CLAVE es impredecible: "ARS" Argentina, "PEN" Perú.
    //   No podés declarar un atributo por cada moneda del mundo.

    @JsonProperty("cca2")
    private String cca2;             // código ISO de 2 letras: "AR"
}
```

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor
public class NombrePais {            // ← molde del objeto "name" anidado
    @JsonProperty("common")
    private String comun;            // "Argentina"
    @JsonProperty("official")
    private String oficial;          // "Argentine Republic"
}
```

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor
public class DetalleMoneda {         // ← molde del VALOR de cada moneda
    @JsonProperty("name")
    private String nombre;           // "Argentine peso"
    @JsonProperty("symbol")
    private String simbolo;          // "$"
}
```

**`@JsonProperty` es el puente entre dos idiomas:** el JSON está en inglés, el código en castellano. **Nadie los alinea por vos.** Si el nombre coincidiera, no haría falta.
⚠️ **Es de Jackson, no de Spring.** **Jackson** = la biblioteca estándar de Java para convertir JSON ↔ objetos (el `JSON.parse` de Java). Entra sola con el starter web.

#### `// ¿CÓMO FUNCIONA?` — por qué Lombok acá es obligatorio 🔴

```
 1. Jackson crea el objeto VACÍO:  new Pais()           👈 CONSTRUCTOR SIN ARGUMENTOS
                                                           → lo da @NoArgsConstructor
 2. Recorre el JSON y por cada campo llama al SETTER:
                                   pais.setNombre(...)   👈 SETTERS
                                   pais.setRegion(...)      → los da @Data
 3. Devuelve el objeto lleno.
```
- Sin **`@NoArgsConstructor`** → Jackson **no puede crear** el objeto. Excepción.
- Sin **`@Data`** (setters) → lo crea pero **no puede llenarlo**. Todo en `null`.
- **`@AllArgsConstructor`**: Jackson **no la usa**. Está para que *vos* crees un `Pais` a mano.

> ⚠️ **La trampa de Java:** el lenguaje regala el constructor vacío **solo si no declarás ninguno**. En cuanto `@AllArgsConstructor` declara uno con parámetros, **el vacío desaparece**. Por eso `@NoArgsConstructor` **repone el constructor que el otro te robó**.

#### ③ El uso: el test

```java
package ar.edu.utn.ba.ddsi.countries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// ↑ "Antes de correr los tests, levantá TODO Spring." Sin esto, no hay quién cree los objetos.
class BuscadorDePaisesIT {

    @Autowired
    // ↑ "Spring: meteme acá el componente que vos creaste."
    private BuscadorDePaises buscadorDePaises;   // ⚠️ nunca un `new`

    @Test
    void buscarPorNombrePeruDevuelvePeru() {
        var opt = buscadorDePaises.buscarPorNombre("peru");
        //  ↑ `var`: el compilador infiere el tipo (Optional<Pais>).
        //    ⚠️ NO es el `let` de JS: Java sigue tipado, solo que inferido.
        assertThat(opt).isPresent();
        assertThat(opt.get().getNombre().getComun()).isEqualTo("Peru");
        assertThat(opt.get().getCca2()).isEqualTo("PE");
        //         └────┬───┘  └────┬────┘
        //              │           └── getter de @Data (no está escrito en el DTO)
        //              └── .get() solo es seguro DESPUÉS de isPresent()
    }

    @Test
    void buscarPorCapitalBuenosAiresDevuelveArgentina() {
        var lista = buscadorDePaises.buscarPorCapital("buenos aires");
        //                                             └──────┬─────┘
        //                                     👆 CON ESPACIO. Se escapa solo (ver ④).
        assertThat(lista).isNotEmpty();
        assertThat(lista.getFirst().getCca2()).isEqualTo("AR");
        //               └────┬───┘  👆 getFirst() es de JAVA 21. Antes: lista.get(0)
    }
}
```

⚠️ **`assertThat(...)` es AssertJ, no JUnit. Conviven:** **JUnit es el motor** (corre los tests, entiende `@Test`); **AssertJ es una biblioteca de aserciones** más expresiva, montada encima. Entra sola con el starter de test.

| Verificar | JUnit 5 | AssertJ |
|---|---|---|
| Son iguales | `assertEquals("Peru", x)` | `assertThat(x).isEqualTo("Peru")` |
| No está vacía | `assertFalse(l.isEmpty())` | `assertThat(l).isNotEmpty()` |
| Contiene X | *(loop a mano)* | `assertThat(l).contains(x)` |

#### ④ La conexión: la llamada HTTP

**La URL NO se hardcodea.** Vive en configuración:
```yaml
# src/main/resources/application.yml
rest-countries:
    base-url: https://restcountries.com/v3.1
```
**¿Por qué?** Si mañana cambia, **la cambiás en un archivo de texto y no recompilás nada.** Mismo principio que el `.env` de Node.

```java
@ConfigurationProperties(prefix = "rest-countries")
// ↑ "Spring: andá al application.yml, buscá el bloque 'rest-countries', y volcalo acá."
@Data   // ← ⚠️ por los SETTERS: Spring llena esta clase igual que Jackson llena un DTO
public class RestCountriesProperties {
    private String baseUrl;
    // ⚠️ En el yml es "base-url" y acá "baseUrl". Funciona:
    //    Spring normaliza los nombres (saca guiones) antes de comparar.
}
// ⚠️ Hay que habilitarla en la clase principal:
//    @EnableConfigurationProperties(RestCountriesProperties.class)
//    Sin eso, Spring no la crea.
```

**El componente completo:**
```java
package ar.edu.utn.ba.ddsi.countries.services;

import ar.edu.utn.ba.ddsi.countries.config.RestCountriesProperties;
import ar.edu.utn.ba.ddsi.countries.services.dto.Pais;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class BuscadorDePaises {

    private static final String CAMPOS =
            "name,capital,region,subregion,population,currencies,languages,area,cca2,cca3";
    // ↑ Los campos que le pedimos a la API. ⚠️ Son EXACTAMENTE los atributos del DTO.
    //   Esta API EXIGE el query param `fields` — sin él devuelve 400.

    private final RestTemplate restTemplate;              // el cliente HTTP
    private final RestCountriesProperties propiedades;    // la config del yml

    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }
    // ↑ El constructor DECLARA lo que la clase necesita — y no lo construye.
    //   Spring lee esta firma, busca esos objetos y se los pasa.
    //   Se llama INYECCIÓN DE DEPENDENCIAS.

    public List<Pais> buscarTodos() {

        URI uri = UriComponentsBuilder
                .fromUriString(propiedades.getBaseUrl())   // "https://restcountries.com/v3.1"
                .path("/all")                              // → .../v3.1/all
                .queryParam("fields", CAMPOS)              // → ...?fields=name,capital,...
                .build()                                   // sin placeholders → build() a secas
                .toUri();
        // ↑ ⚠️ ¿Por qué no concatenar con "+"?
        //   Los ESPACIOS y los caracteres especiales rompen la URL.
        //   UriComponentsBuilder la arma por partes y ESCAPA los valores.

        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        // ↑ 👈 LA LÍNEA. Acá pasa TODO, en una:
        //     1. abre la conexión y manda el GET
        //     2. recibe el JSON como texto
        //     3. se lo pasa a Jackson, que lo convierte en objetos usando los DTOs
        //     4. te devuelve un array tipado
        //   Equivale a:  const data = await (await fetch(url)).json();
        //   `Pais[].class` = "convertilo a este tipo". ⚠️ Se pide un ARRAY y no un List
        //   por una limitación de Java (los genéricos se borran al compilar).
        //   Resultado esperado: cuerpo.length ≈ 250

        return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
        // ↑ a) Si no vino nada → LISTA VACÍA, nunca null.
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

    // buscarPorMoneda / buscarPorRegion / buscarPorCapital: IDÉNTICOS.
    // Solo cambia la ruta del .path():
    //   .path("/currency/{codigo}") · .path("/region/{region}") · .path("/capital/{capital}")
}
```

🔴 **`build()` vs `buildAndExpand()`:**

| | Cuándo |
|---|---|
| **`build()`** | Ruta **fija**, sin placeholders → `buscarTodos()` |
| **`buildAndExpand(valor)`** | Ruta con **placeholders** `{...}`. Los rellena **y los escapa** → los otros cuatro |

**Ahí está el path param vs query param, en código:** `.path("/name/{nombre}")` = path param · `.queryParam("fields", CAMPOS)` = query param.

⚠️ **`RestTemplate` no se crea con `new`.** Se declara en una clase de configuración aparte (`@Configuration` + `@Bean`) porque es una clase **de Spring**, no tuya, y no podés anotarla.

### 4.4 El bug del profe 🔴 🎯

Test falla: `expecting not blank but was null`. Los 250 países llegaron, pero `getNombre().getComun()` daba `null`.
**Causa:** `@JsonProperty("comon")` — con una sola M. El JSON trae `"common"`.
**¿Por qué no explotó?** Por `@JsonIgnoreProperties(ignoreUnknown = true)`: Jackson buscó `comon`, no lo encontró, **y no se quejó**.

> 🔴 **El trade-off:**
>
> | | Con `ignoreUnknown = true` | Sin él |
> |---|---|---|
> | La API agrega un campo | ✅ Sigue andando | 💥 Explota en producción |
> | Escribís mal un `@JsonProperty` | ⚠️ **`null` silencioso** | 💥 Explota al toque |
>
> **El mismo chaleco antibalas que te protege de la API te esconde tus typos.**
>
> **📌 El síntoma:** *un campo del DTO viene en `null` y todos los demás bien* → **casi siempre es un `@JsonProperty` mal escrito.** No es la red, no es Spring: es una letra.

**El método de debug (transferible):** no leyó código buscando el error. Hizo un `System.out.println` de la lista y **miró qué llegó realmente**. Vio `common: null` y `official` lleno → el problema no era la conexión (los datos llegaban) sino **el mapeo de ese campo**. **Primero determinás dónde está el problema; después leés esa zona.**

## 5. Información operativa 🔴 🎯

### 5.1 Herramientas

| Herramienta | Notas |
|---|---|
| **IntelliJ IDEA** | El IDE de la cursada. **Community (gratuita) alcanza**; con cuenta estudiantil sacás la Pro por un año. ⚠️ Consume bastante **RAM**. Trae **guardado automático** activado (se puede desactivar). |
| **Postman** | Cliente HTTP. Se instala. |
| **Maven** | Gestor de dependencias (`pom.xml`). |
| **Git / GitHub** | Usá la cuenta que quieras, pero **anotala en el Excel del equipo**. Los repos los da la cátedra. |
| **Discord** | Canal de dudas. Ver 5.3. |
| **Drive del curso** | Todo se sube ahí: grabaciones, presentaciones, material, en carpetas por clase. **El link está en el aula virtual.** |

⚠️ **VS Code para Java: la cátedra NO lo recomienda** (*"queda muy corto"*). Sobre herramientas agénticas: *"no lo van a poder resolver todo con IA"*.
⚠️ **El plugin de estilos de IntelliJ ya no se usa.** Si lo ves en material viejo, ignoralo.

### 5.2 TPA — Entrega 1 🔴

**Contexto:** **UTN Solidaria**, iniciativa real de la Subsecretaría de Asuntos Estudiantiles. *"Siempre queremos que el TP tenga un impacto social."*
**Estructura: 6 entregas** en total. **Tutores personales asignados por equipo.**

| Entregable | Detalle |
|---|---|
| **Diagramas de clases por servicio** | **Dos**: **donaciones** y **notificaciones**. ⚠️ **Solo la funcionalidad que pide el enunciado.** |
| **Diagrama de arquitectura** | Ya viene dado. Se trata de **demostrar que lo entendieron**. |
| **Justificaciones** | 🔴 *"Esto es muy importante."* |
| **Diagrama general de casos de uso** | Para identificar **qué tiene que hacer el sistema**. |
| **Implementación de los requerimientos** | Código. |
| **Bocetos de interfaz** | ~20, uno por requerimiento. **A mano, papel o Paint. Desprolijos está bien.** No es HTML ni Figma. **Descartables.** |
| **Un endpoint web simple** | En donaciones: un `GET` que devuelva *"Hola desde el servicio"*. ⚠️ Es **exponer**, no consumir — se ve en la clase 3. |
| **Pregunta de discusión** | **Defensa individual**: *"todos los integrantes tienen que poder sostener una conversación"*. |

🔴 **El orden recomendado por la cátedra:**
```
1. BOCETOS de interfaz        ← "recontra recomendado"
2. DIAGRAMA DE CASOS DE USO   ← "lo pueden hacer YA, no hace falta enseñarles nada"
3. DIAGRAMA DE CLASES
4. CÓDIGO                     ← ÚLTIMO. "No se tiren de lleno al código o lo van a tener que rehacer."
```

⚠️ **El modelado se piensa entre todos.** *"La parte de pensar me gustaría que la hagan entre todos. Después se dividen el código."* Y: *"traten de asistirse lo menos posible, que no les robe esa oportunidad de pensamiento."*

### 5.3 Canales de consulta 🔴

**Dos canales, y no se mezclan:**

| Canal | Para qué |
|---|---|
| **Foro general** (Discord) | **Solo dudas de enunciado.** *"No entiendo qué implica el requerimiento 7."* |
| **Tu tutor / ayudante personal** | **Implementación y decisiones de diseño.** Es tu checkpoint. |

⚠️ **En el canal general NO responden** *"esto lo hice así, ¿está bien?"*. Eso es del tutor.
**Los tutores te evalúan.** *"Llévense bien"* — medio en broma, pero **el tutor es quien aprueba tu entrega.**

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

## 🏁 Las tres cosas de la clase 02

1. **HTTP es *stateless*.** Cada solicitud lleva toda la información. De ahí sale la mitad de lo que viene después.
2. **REST no es una tecnología: es un acuerdo.** Verbos, URIs, códigos de estado, JSON. Y **tiene un costo** (el N+1) que hay que saber justificar.
3. **La materia es de aplicación.** No piden definiciones: piden que **decidas y justifiques**. *"Pongo esto acá, porque priorizo tal atributo de calidad, y estoy pagando con tal otro."*

---

**FIN DEL APUNTE RESUMEN — CLASE 02**
