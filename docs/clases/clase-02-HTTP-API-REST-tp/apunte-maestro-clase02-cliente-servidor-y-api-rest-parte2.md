# Apunte Maestro — Clase 02 · Parte 2
## Protocolo HTTP + Arquitectura Web

---

## 🧭 De dónde venís

**Parte 1:** el patrón cliente-servidor. Sabés que el cliente siempre inicia, que el servidor escucha en un puerto, que la interacción es solicitud → procesamiento → respuesta, y que toda decisión arquitectónica es un trade-off entre atributos de calidad.

**Ahora vamos por el idioma.** El patrón dice *que* se hablan; HTTP dice **cómo**.

---

## 1. Qué es HTTP 🔴

> **HTTP** — *HyperText Transfer Protocol*, protocolo de transferencia de hipertexto.

**De dónde sale el nombre.** Se diseñó para transferir **hipertexto**: texto con **hipervínculos** (links) adentro. Hacías clic en un link y saltabas a otro documento, que tenía otros links. Navegabas de hipervínculo en hipervínculo. Eso era todo lo que se pensaba hacer con internet.

De ahí a hoy pasaron treinta y pico de años, internet se volvió otra cosa, aparecieron protocolos de soporte — y **HTTP sigue siendo el protocolo principal de la web**. Es el que vas a usar en toda la materia y en todo el TPA.

### La ficha técnica 🔴

Esto es lo que hay que saber recitar. Son seis renglones y **cada uno tiene consecuencias**:

| Característica | Qué significa |
|---|---|
| **Capa de aplicación** (capa 7 del modelo OSI) | Es un protocolo que usan las **aplicaciones** para comunicarse. |
| **Sincrónico** | Pedís, esperás, te responden. Sección 2. |
| **Sin estado** *(stateless)* | No recuerda nada de la solicitud anterior. **Sección 2 — es el punto más importante.** |
| **Cliente / Servidor** | Implementa el patrón de la Parte 1. |
| **Solicitud / Respuesta** | Un pedido, una respuesta. Y se cerró. |
| **Puertos 80 y 443** | 80 para HTTP, 443 para HTTPS. Sección 7. |

**Métodos principales:** `GET` · `POST` · `PUT` · `DELETE` *(y `PATCH`, que la slide no lista pero se usa igual — sección 5)*.

Su definición está en la **RFC 2616**, la especificación oficial de HTTP/1.1.

> ⚠️ **Corrección del material:** las slides dicen "RFC-216". Es un typo — el número correcto es **RFC-2616**, y el propio enlace de la slide apunta ahí.

> **Recordá — modelo OSI (una línea):** es un modelo de capas que ordena la comunicación en red. Abajo del todo está la capa física (el cable); arriba de todo, la capa 7 o **de aplicación**, donde viven los protocolos que usan los programas: HTTP, SMTP (envío de mails), IMAP y POP3 (recepción), MQTT. Más abajo trabajan TCP, IP y UDP. En DSI **solo te importa la capa 7**; el resto lo vas a ver en Comunicación de Datos y en Redes.

---

## 2. Sincrónico y sin estado 🔴

Estas dos palabras aparecen en una viñeta cada una y **valen media materia**. Van despacio.

### Sincrónico

**Mandás una solicitud y esperás la respuesta.** Bloqueante, uno a uno, ida y vuelta. No mandás algo y seguís con tu vida: te quedás esperando.

Esto tiene un límite que se ve enseguida: **con HTTP/1.1 no hay streaming, no hay comunicación continua.** Si querés hacer un chat, no tenés forma de que el servidor te avise que llegó un mensaje: tendrías que **preguntarle cada segundo si hay algo nuevo** (eso se llama *polling*, y es exactamente tan feo como suena).

> 🕳️ **Madriguera — Otras formas de comunicarse**
> Existen protocolos pensados para el flujo continuo de datos entre componentes (gRPC, por ejemplo) y arquitecturas asincrónicas donde nadie espera a nadie. Son la respuesta a la limitación de arriba.
> *Volvé al camino — la comunicación asincrónica cae en la clase 7 (sincronismo/asincronismo, cron tasks) y en la 12 (broker, RabbitMQ). gRPC y GraphQL, en la clase 30.*

### Sin estado (*stateless*) 🔴🔴

**Esta es la palabra más importante de la clase.** Y es la que más se malinterpreta, así que vamos con el ejemplo que la fija.

> **HTTP no recuerda absolutamente nada de las solicitudes anteriores.**
>
> Cada request es un desconocido que toca la puerta por primera vez.

```
   Cliente:   GET /clientes/5                          ← "dame el cliente 5"
   Servidor:  200 OK  { id: 5, nombre: "Juan" }        ← acá tenés

   Cliente:   GET /facturas                            ← "dame las facturas
                                                          del cliente que te pedí recién"
   Servidor:  ¿QUÉ cliente?  🤷
              No tengo la más mínima idea de quién sos
              ni de qué me pediste hace dos segundos.
```

**El servidor no está siendo desagradable: literalmente no tiene forma de saberlo.** El protocolo **no guarda** el contexto de las operaciones anteriores. Si querés las facturas del cliente 5, **tenés que volver a decirle que es el 5**:

```
   Cliente:   GET /clientes/5/facturas                 ← ✅ toda la info, otra vez
   Servidor:  200 OK  [ ... ]
```

**Consecuencia directa, y esto es lo que hay que grabarse:**

> 🔴 **Cada solicitud tiene que llevar TODA la información necesaria para ser procesada.** El servidor no completa nada con memoria, porque no tiene.

Esta característica **es la razón de ser de la mitad de las cosas que vas a ver en la materia** — desde cómo se diseñan las rutas REST (Parte 3) hasta por qué existen los tokens, las cookies y las sesiones.

### ⚠️ El matiz técnico que preguntaron en clase (y que vale)

Un alumno objetó: *"pero HTTP va encapsulado en TCP, y si un paquete se pierde, se retransmite. Entonces sí hay estado."*

**La objeción es correcta en los hechos, y no contradice nada. Son dos estados distintos:**

| | Quién lo maneja |
|---|---|
| **Estado de la conexión** (¿se perdió un paquete? ¿hay que retransmitir?) | **TCP**, la capa de abajo. Sí lo maneja. |
| **Estado de la operación** (¿qué me pediste en el request anterior?) | **NADIE.** Ni HTTP ni TCP. Ese es el punto. |

**HTTP delega el estado de la conexión a TCP, y el estado de la operación no lo maneja nadie.** Cuando decimos "HTTP es *stateless*" hablamos del segundo.

> **Recordá:** **TCP** es el protocolo de la capa de transporte que garantiza que los datos lleguen completos y en orden, retransmitiendo lo que se pierda. HTTP se apoya sobre él.

---

## 3. Anatomía de una request 🔴

Toda solicitud HTTP tiene **tres partes**. Siempre.

```http
GET /usuarios HTTP/1.1                          ← ① LÍNEA DE INICIO
Host: api.miapp.com                             ← ② HEADERS (cabeceras)
Content-Type: application/json
Authorization: Bearer <token>

{ "nombre": "Juan", "email": "juan@mail.com" }  ← ③ BODY (cuerpo)
```

### ① Línea de inicio — `Método + URL + Versión`

`GET` (qué quiero hacer) · `/usuarios` (sobre qué recurso) · `HTTP/1.1` (con qué versión del protocolo).

### ② Headers (cabeceras) — **metadatos** de la solicitud

Datos *sobre* el pedido, no el pedido en sí. Los tres del ejemplo:

- **`Host`** — a qué servidor le estás pidiendo.
- **`Content-Type`** — en qué formato va lo que estás mandando (acá, JSON).
- **`Authorization: Bearer <token>`** — tu credencial. El servidor la valida y decide si te deja o no.

> 🔴 **Un token NO es lo mismo que cifrado.** Son dos cosas distintas y hay que tenerlas separadas:
>
> | | Qué protege |
> |---|---|
> | **El token** | Protege **el recurso**. Decide *quién puede pedirlo*. |
> | **HTTPS** | Protege **el viaje**. Hace que *nadie pueda leer los datos en el camino*. |
>
> **Si mandás un token por HTTP (sin S), viaja en texto plano y cualquiera que intercepte el tráfico lo lee.** Tenés control de acceso y cero confidencialidad.
>
> **Por eso en producción se usan las dos cosas siempre:** HTTPS para que viaje cifrado, y token para proteger el endpoint.

> 🕳️ **Madriguera — Autenticación y autorización**
> Qué es exactamente un token, cómo se genera, qué es *Bearer*, y la diferencia entre autenticar (probar quién sos) y autorizar (decidir qué podés hacer).
> *Volvé al camino — cae completo en la clase 14 (Seguridad de la Información).*

### ③ Body (cuerpo) — **los datos** que enviás

⚠️ **Solo va en `POST`, `PUT` y `PATCH`.** Es decir: **solo cuando estás mandando algo** (creando o modificando). En un `GET` o un `DELETE` **no hay body**: no tenés nada que enviar más allá de la identificación del recurso, que va en la URL.

---

## 4. Anatomía de una response 🔴

Simétrica. También tres partes.

```http
HTTP/1.1 200 OK                                        ← ① LÍNEA DE ESTADO
Content-Type: application/json                         ← ② HEADERS
Content-Length: 342

{ "id": 1, "nombre": "Juan", "email": "juan@mail.com" } ← ③ BODY
```

**① Línea de estado** — `Versión + Código + Descripción`. El `200 OK` de arriba: salió bien. Los códigos, en la sección 5.

**② Headers** — metadatos de la respuesta. `Content-Type` te dice **qué te está devolviendo**: puede ser `application/json`, pero también `text/html` (una página), o un PDF, o una imagen. `Content-Length`, el tamaño en bytes.

**③ Body** — los datos. Lo que pediste.

---

## 5. Los métodos HTTP 🔴

| Método | Qué hace | ¿Lleva body? |
|---|---|---|
| **GET** | **Obtiene** un recurso (o un listado) | ❌ No |
| **POST** | **Crea** un recurso nuevo | ✅ Sí — los datos del recurso |
| **PUT** | **Modifica** un recurso **completo** | ✅ Sí — el recurso entero |
| **PATCH** | **Modifica** un recurso **parcialmente** | ✅ Sí — solo los campos a cambiar |
| **DELETE** | **Borra** un recurso | ❌ No |

### PUT vs PATCH 🟡

En clase surgió, y la respuesta fue clara: **lo dice la norma.**

> Si querés actualizar **todo** el recurso → **PUT**.
> Si querés actualizarlo **parcialmente** → **PATCH**.

⚠️ *"Después cada uno hace lo que quiere"*, dijo el profe — y es cierto que en la industria vas a ver `PUT` usado para todo. **Pero el criterio de la materia es el de la norma**, y en el parcial se responde con la norma.

**Y hay más métodos.** HTTP define varios más (`HEAD`, `OPTIONS`, y otros que vas a ver como opciones en Postman). Las slides listan solo los principales porque son los que se usan.

---

## 6. Códigos de estado 🔴

Un número de tres cifras que el servidor devuelve **siempre**, en toda respuesta, diciendo cómo terminó la operación.

### Las cinco familias

| Familia | Significado | Los que vas a ver |
|---|---|---|
| **1xx** | **Informativas.** La petición se recibió y se está procesando. | (casi nunca) |
| **2xx** | **Correctas.** Se procesó bien. | `200 OK` · `201 Created` · `204 No Content` |
| **3xx** | **Redirecciones.** Hacen falta más acciones para completar. | `302 Found` |
| **4xx** | **Errores causados por el CLIENTE.** | `400` · `401` · `403` · `404` · `405` |
| **5xx** | **Errores causados por el SERVIDOR.** | `500` · `503` · `504` |

### 🔴 La distinción que hay que tener clarísima

En clase se discutió esto varios minutos, así que se pregunta:

> **Los códigos de estado los genera SIEMPRE el servidor. Siempre. El cliente nunca genera un código.**
>
> Lo que indica la familia **no es quién lo emite** — sino **de quién fue la culpa**.

Un `404 Not Found` **te lo manda el servidor**, pero está clasificado como error 4xx —del cliente— porque **el cliente pidió un recurso que no existe**. El servidor no se equivocó: hizo exactamente lo que tenía que hacer, que era avisarte que eso no está.

*(Y sí: un `404` puede venir con una página bonita, con un dibujito y un gato. Eso no cambia el código: es solo el `body` de la respuesta, personalizado para que el usuario vea algo lindo en vez de un error crudo del servidor.)*

### Los que hay que conocer por número

| Código | Nombre | Cuándo |
|---|---|---|
| **200** | OK | Todo salió bien. La respuesta genérica de éxito. |
| **201** | Created | **Se creó un recurso.** La respuesta natural de un `POST`. |
| **204** | No Content | Salió bien **y no hay nada que devolver.** La respuesta natural de un `DELETE`. |
| **302** | Found (redirect) | El recurso se movió; el servidor te manda a la ubicación nueva. |
| **400** | Bad Request | El cliente mandó algo mal formado (por ejemplo, un body inválido, o le faltó un parámetro obligatorio). |
| **401** | Unauthorized | **No estás autenticado.** No mandaste credenciales, o son inválidas. |
| **403** | Forbidden | **Estás autenticado, pero no tenés permiso** para este recurso. |
| **404** | Not Found | El recurso pedido no existe. |
| **405** | Method Not Allowed | Ese endpoint existe, pero no acepta ese método (querés `PATCH` y solo hay `GET`). |
| **500** | Internal Server Error | Algo explotó del lado del servidor. El genérico. |
| **503** | Service Unavailable | El servicio no está disponible. |
| **504** | Gateway Timeout | Se agotó el tiempo de espera: alguien en el medio no respondió a tiempo. |

> ⚠️ **Atención con 401 vs 403 — en la grabación quedaron dichos al revés.**
>
> El estándar (y el RFC que la propia cátedra referencia) dice:
>
> - **`401 Unauthorized` → falta autenticación.** No mandaste token, o el token es inválido. *"No sé quién sos."*
> - **`403 Forbidden` → falta autorización.** Mandaste un token válido, pero ese usuario **no tiene permiso** para ese recurso. *"Sé quién sos, y no podés."*
>
> En clase el concepto se explicó bien (*"uno es que me validó pero no tengo permiso y el otro es que directamente no"*) pero **los números se asignaron invertidos**. Es un lapsus, no un criterio de la cátedra: el material oficial no lo especifica y el profe remitió explícitamente al RFC como autoridad.
>
> **Para el parcial: usá el estándar (401 = no autenticado, 403 = sin permiso).** Si el tema aparece en la clase de seguridad (clase 14), confirmalo ahí.

### 🔴 Por qué esto importa más de lo que parece

*"La idea es que ustedes, cuando generen sus servicios, cumplan todos estos estándares."*

**No es prolijidad. Es interoperabilidad.** En el TPA vas a tener **varios servicios que se hablan entre sí**, escritos por distintos integrantes del equipo. Si todos respetan los códigos de estado, cada uno sabe qué esperar del otro **sin preguntarle**. Ese es el negocio de un estándar: eliminar la conversación.

**Y un detalle de seguridad, que también apareció:** los `500` se devuelven **genéricos, sin detalles**. A propósito. Nunca le expliques al cliente qué falló adentro de tu sistema: esa información le sirve a un atacante para entender tu estructura. Un `500` dice "algo salió mal" y nada más.

> 🕳️ **Madriguera — El 418**
> Existe el código `418 I'm a teapot` ("soy una tetera"). Es una broma de la especificación, de 1998. No lo vas a usar. Está para que sepas que existe cuando alguien lo tire en una charla.
> *Volvé al camino.*

---

## 7. HTTP vs HTTPS y los puertos 🔴

| | **HTTP** | **HTTPS** |
|---|---|---|
| **Puerto** | 80 | **443** |
| **Seguridad** | **Sin cifrado** | **Cifrado TLS/SSL** |
| **Certificado** | No requerido | **Requerido** |
| **Uso recomendado** | **Solo desarrollo local** | **Producción, siempre** |

### Qué hace la S 🔴

Le agrega una **capa de cifrado** (TLS) a la comunicación. Los datos viajan **cifrados** entre el cliente y el servidor, y se descifran del otro lado.

**Qué significa eso en concreto:** si mandás tu usuario y contraseña por HTTP, viajan **en texto plano**. Cualquiera que intercepte el tráfico en el camino los lee. Con HTTPS, ese mismo tráfico interceptado es **ilegible**.

⚠️ **Nombre:** originalmente el mecanismo se llamaba **SSL**; hoy está *deprecado* y se llama **TLS**. En la práctica se sigue diciendo "certificado SSL" por costumbre. Los dos nombres refieren a lo mismo.

**El certificado** es lo que valida la identidad del servidor y habilita el cifrado. Se renueva periódicamente. Si vence o es inválido, **el navegador te avisa** ("sitio no seguro"), y **los buscadores penalizan** los sitios sin HTTPS.

📌 **En el TPA:** cuando hagas el deploy en la nube, la cátedra **va a pedir los certificados implementados**. Mientras corras local, no hace falta.

### Los puertos 🟡

**Un puerto es el número donde un servidor "escucha".** Los del `0` al `1023` están **reservados** para protocolos conocidos (el 80 es HTTP, el 443 HTTPS, el 22 SSH). De ahí para arriba, son libres.

🔴 **La regla que te va a morder:**

> **En un mismo nodo (misma IP), NO pueden correr dos aplicaciones escuchando en el mismo puerto.**

Cuando en el TPA levantes dos servicios Spring Boot al mismo tiempo, el segundo **no va a arrancar** y te va a decir *"ese puerto ya está ocupado"*. No es un bug tuyo: es esta regla. La solución es cambiarle el puerto a uno de los dos (se configura en el `application.yml`).

*(Ojo con la confusión: **como cliente** sí podés tener muchas conexiones abiertas contra distintos servidores en el puerto 443. La restricción es para el que **escucha**, no para el que llama.)*

---

## 8. Arquitectura Web: qué pasa cuando entrás a una URL 🔴

Acá se juntan la Parte 1 y todo lo anterior. Este es **el recorrido completo**, y hay que poder recitarlo.

```
        ①  request                      ②  request
   ┌─────────┐  ──────────►  ╭───────────╮  ──────────►  ┌──────────┐
   │   web   │               │  Internet │               │   web    │
   │ browser │  ◄──────────  │   (nube)  │  ◄──────────  │  Server  │
   └─────────┘   response ④  ╰───────────╯   response ③  └──────────┘
```

**La nube es una caja negra a propósito.** Qué dispositivos hay en el medio, cómo se rutea, cómo se retransmite — no importa en DSI. Eso lo vas a ver en Comunicación de Datos y Redes. Acá trabajamos en la capa de aplicación.

### Los 8 pasos 🔴

```
 1. El usuario escribe https://www.google.com en el navegador.

 2. El DNS traduce el dominio a una dirección IP.
    → El DNS (Domain Name Server) es el servidor que convierte nombres
      en direcciones IP. Tu equipo tiene configurados uno primario y uno
      secundario (por si el primero no responde).
      Si ya entraste antes, probablemente lo resuelva desde una caché local.

 3. El navegador establece una conexión TCP con el servidor.

 4. Se negocia el cifrado TLS  (solo si es HTTPS).

 5. El navegador envía un HTTP GET /
    → El "/" es la raíz del sitio. Un GET, como todo lo que escribís en la barra.

 6. El servidor procesa y devuelve el HTML.
    → Content-Type: text/html   ← ¿ves? es el header de la sección 4.

 7. El navegador descarga CSS, JS e imágenes adicionales.
    → El HTML es SOLO el esqueleto (la estructura). Trae adentro una LISTA
      de otros recursos que necesita: hojas de estilo (CSS), scripts (JS),
      imágenes, tipografías. Cada uno de ellos es OTRA request HTTP.
      ⚠️ Los que ya tenga cacheados, NO los vuelve a pedir.

 8. La página se renderiza en pantalla.
```

### 🔴 La pregunta que se hizo en clase, y que hay que saber contestar

> *"En este ejemplo del navegador, ¿de qué tipo de cliente estamos hablando?"*

**Cliente liviano.** El servidor **arma la vista** (genera el HTML) y la manda; el navegador solo la **renderiza**. Toda la lógica está del lado del servidor.

Ahí se cierra el círculo con la Parte 1: la web tradicional **es** el caso "cliente pasivo – servidor activo". *(Una SPA hecha con React sería lo contrario: el servidor manda datos crudos y el cliente arma la vista. Cliente pesado.)*

---

## 9. Las DevTools: la herramienta que tenés que usar 🟡

*"El F12 tiene que ser algo que ustedes se empiecen a mirar."*

La **consola de desarrollador** (`F12` o clic derecho → Inspeccionar, en cualquier navegador) te deja **ver todo lo de este apunte, pasando de verdad**.

**Pestaña Network** — cada request que hace la página, en vivo:

| Lo que ves | Es esto de arriba |
|---|---|
| La URL y el método (`GET`) | Sección 3 — línea de inicio |
| El código de estado (`200 OK`) | Sección 6 |
| La IP remota y el puerto (`:443`) | Sección 7 |
| `Content-Type: text/html; charset=UTF-8` | Sección 4 — headers |
| La respuesta completa | Sección 4 — body |
| **La cascada de recursos** (CSS, JS, imágenes, fuentes) | Sección 8, paso 7 |

**Hacelo ahora, que lleva un minuto:** abrí cualquier sitio, `F12` → pestaña **Network** → `Ctrl+F5`. Vas a ver la primera request al HTML, y después la lluvia de requests de todos los recursos que la página necesita. **Todo lo que leíste en esta parte, ocurriendo.**

⚠️ **En el TPA lo vas a necesitar.** Es la herramienta con la que vas a debuggear todo lo que pase del lado del cliente.

---

## 📌 Para el parcial, si te preguntan

**"¿Qué significa que HTTP sea *stateless* (sin estado)?"**
Significa que el protocolo no conserva información de las solicitudes anteriores: cada request es independiente y el servidor no recuerda qué se le pidió antes. La consecuencia directa es que **cada solicitud debe contener toda la información necesaria para ser procesada**. El estado de la conexión (retransmisión de paquetes perdidos) sí se maneja, pero lo maneja TCP, no HTTP.

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

## ✅ Checkpoint — Parte 2

1. ¿Qué significa que HTTP sea sincrónico? ¿Qué limitación concreta genera?
2. ¿Qué significa *stateless*? Dame el ejemplo del cliente y las facturas.
3. Un alumno dice: "HTTP no es stateless, porque TCP retransmite los paquetes perdidos". ¿Qué le contestás?
4. Enumerá las tres partes de una request y las tres de una response.
5. ¿En qué métodos HTTP hay body y en cuáles no? ¿Por qué?
6. ¿Cuándo usás `PUT` y cuándo `PATCH`? (Respuesta según la norma.)
7. ¿Quién genera los códigos de estado? Entonces, ¿qué indica exactamente que un código sea 4xx?
8. ¿Qué código devuelve un `POST` exitoso? ¿Y un `DELETE` exitoso? ¿Por qué son distintos?
9. Diferencia entre `401` y `403`.
10. ¿Por qué los errores `500` se devuelven genéricos, sin detalle de lo que falló?
11. Tenés dos servicios Spring Boot y el segundo no arranca. ¿Cuál es la causa más probable?
12. En el recorrido de una URL, ¿qué tipo de cliente es el navegador que recibe un HTML ya armado? Justificá.

---

## 🎯 Qué viene en la Parte 3

Tenés el patrón (Parte 1) y el idioma (Parte 2).

Ahora, **el estándar**: qué es una API —y por qué no todas las APIs son web—, qué es REST, sus criterios, cómo se diseñan las rutas, y por qué el TPA te va a exigir respetarlo **a rajatabla**.

Es la parte donde, como dijeron en clase, *"se junta todo lo que estuvimos charlando"*.

---

**FIN DE LA PARTE 2**
