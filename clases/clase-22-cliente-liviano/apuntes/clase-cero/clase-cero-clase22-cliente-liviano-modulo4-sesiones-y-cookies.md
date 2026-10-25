# Clase desde cero — Cliente Liviano — Módulo 4
# Sesiones y cookies: cómo el servidor se acuerda de quién sos

**Fase C — Identidad** · Densidad general: 🔴 (definiciones de examen, y el modelo mental sobre el que se apoyan los módulos 5, 6 y 7)

---

## Sobre este documento

**Cubre:** por qué HTTP no tiene memoria y qué problema concreto genera; qué es una **sesión** (el espacio que el servidor reserva por persona), qué guarda y por qué cuesta; qué es una **cookie**, quién la crea, dónde vive y cómo viaja; el flujo completo de un login con sesión, que resuelve el enigma del módulo 1; qué pasa si alguien te roba la cookie; cookie contra `localStorage`; cómo se guarda y se lee algo de la sesión en código; y dónde viven las sesiones. Se cierra el **hilo H4**.

**NO cubre:** la alternativa sin sesión (tokens, módulo 5); cómo Spring Security hace el login por vos y protege rutas (6); cómo se combinan las dos cosas en el TP (7).

## De dónde venís

- **Módulo 1:** HTTP no tiene memoria; el administrador y el docente hacen el mismo `GET /alumnos` y ven cosas distintas, y con lo sabido hasta ahí no había forma de explicarlo.
- **Módulo 3:** los atributos flash "viven en un lugar del servidor" entre el `POST` y el `GET` siguiente. Ese lugar es el tema de este módulo.
- **De clases anteriores:** un request HTTP tiene método, ruta, **cabeceras** (*headers*: pares clave-valor con información sobre el pedido) y cuerpo; la respuesta tiene código de estado, cabeceras y cuerpo.

---

## 1. 🔴 El problema: cada pedido llega solo

Un sistema de gestión de alumnos con dos tipos de persona: **administrador** (puede todo) y **docente** (solo mira). Hay una pantalla de login. Después del login, los dos van a `/alumnos`.

```
   1. POST /login  (usuario=admin, clave=1234)  ────►  servidor: "ok, sos admin"
   2. GET  /alumnos                              ────►  servidor: ... ¿y vos quién sos?
```

Ese segundo pedido es el problema. **HTTP no tiene memoria.** El `GET /alumnos` llega al servidor como un pedido nuevo, completo en sí mismo, sin ninguna referencia al `POST /login` de hace un segundo. El servidor no tiene forma, por sí mismo, de saber que ese `GET` lo mandó la misma persona que acaba de loguearse. Podría ser cualquiera.

Y sin embargo el sistema funciona: el administrador ve la tabla con botones, el docente sin botones, cada uno con el mismo `GET`. **Algo pasa en el servidor que le permite saber quién le está hablando.** Ese algo tiene dos partes que trabajan juntas: la sesión y la cookie.

---

## 2. 🔴 La sesión: un espacio reservado en el servidor para cada persona

> **Sesión:** un espacio que el servidor **reserva** para una persona a partir de que se identifica, donde guarda los datos que necesita recordar de ella entre un pedido y el siguiente.

Qué guarda una sesión, típicamente:

| Dato | Para qué |
|---|---|
| **Identificador de sesión** (*session ID*) | Un código único que distingue esta sesión de todas las demás. |
| **Quién es** (identificador de usuario, nombre) | Para saber a quién le está respondiendo. |
| **Roles y permisos** | Para decidir qué puede ver y hacer (módulo 6). |
| Cualquier otra cosa que haga falta recordar | Un carrito de compras, un atributo flash del módulo 3, un token del módulo 7. |

Es útil imaginarla como una tabla que el servidor mantiene en memoria:

```
   SESIONES ACTIVAS (en el servidor)
   ┌──────────────┬──────────┬───────────────┬──────────────────────┐
   │ session ID   │ usuario  │ rol           │ creada / vence       │
   ├──────────────┼──────────┼───────────────┼──────────────────────┤
   │ 7f3a9c...    │ admin    │ ADMINISTRADOR │ 10:02 / 10:32        │
   │ b81e04...    │ docente  │ DOCENTE       │ 10:05 / 10:35        │
   │ ...          │ ...      │ ...           │ ...                  │
   └──────────────┴──────────┴───────────────┴──────────────────────┘
```

Tres propiedades que importan:

1. **Es temporal.** Se crea en el login y tiene que terminar: o porque la persona cierra sesión, o porque vence por inactividad. Una sesión que nunca termina es un agujero (sección 5) y un desperdicio (punto 2).
2. **Ocupa memoria del servidor.** Cada fila de esa tabla es memoria. Diez usuarios, diez filas: nada. Un millón de usuarios conectados a la vez: un millón de filas que el servidor tiene que sostener. **Hilo H7 abierto:** esto tiene un costo, y el módulo 5 muestra la alternativa.
3. **Vive en el servidor, no en el navegador.** La persona nunca ve su sesión. Lo único que tiene es la llave para que el servidor la encuentre: eso es la cookie.

🎓 **Si te lo preguntan — ¿qué es una sesión?**
Es un espacio que el servidor reserva para un usuario una vez que se identifica, donde guarda la información que necesita recordar entre requests (identificador de sesión, usuario, roles, permisos). Es temporal, ocupa memoria del servidor y debe liberarse al cerrar sesión o al expirar.

---

## 3. 🔴 La cookie: el hilo entre cada pedido y su sesión

Falta resolver cómo el `GET /alumnos` le dice al servidor "soy la sesión `7f3a9c`". Eso lo hace la cookie.

> **Cookie:** un pequeño dato **clave = valor** que **el servidor crea** y le manda al navegador en una respuesta, que **el navegador guarda** en su propio almacenamiento, y que el navegador **envía automáticamente** en **cada** pedido siguiente al mismo sitio.

Las tres partes de esa definición son las tres cosas que hay que retener:

**El servidor la crea.** En la respuesta al login exitoso, el servidor agrega una cabecera:

```
HTTP/1.1 200 OK
Set-Cookie: session=7f3a9c...
```

**El navegador la guarda.** La asocia al sitio (`localhost:8090`, o el dominio que sea). No la muestra, no la interpreta: la guarda.

**El navegador la manda sola en cada pedido a ese sitio.** Sin que la persona ni el JavaScript hagan nada:

```
GET /alumnos HTTP/1.1
Cookie: session=7f3a9c...
```

Y del otro lado, el servidor lee esa cabecera, toma el `7f3a9c...`, va a su tabla de sesiones, y encuentra la fila: *admin, ADMINISTRADOR*. Ya sabe quién es.

```
   NAVEGADOR                                            SERVIDOR
   (guarda: session=7f3a9c)                             (tabla de sesiones)
       │                                                     │
       │──── GET /alumnos ─────────────────────────────────►│
       │     Cookie: session=7f3a9c                          │ busca 7f3a9c en la tabla
       │                                                     │ → admin, ADMINISTRADOR
       │◄─── HTML con la tabla Y los botones de admin ──────│
```

**La cookie no lleva quién sos. Lleva un número para que el servidor te busque.** Toda la información está en la sesión, del lado del servidor. La cookie es solo la referencia.

🎓 **Si te lo preguntan — ¿qué es una cookie y cómo viaja?**
Es un archivo clave-valor que el servidor genera y envía al navegador (`Set-Cookie`), que el navegador almacena y reenvía automáticamente en la cabecera `Cookie` de cada request posterior al mismo sitio. En una arquitectura con sesión, transporta el identificador de sesión con el que el servidor recupera los datos del usuario.

---

## 4. 🔴 El flujo completo: login con sesión

Las dos piezas juntas, de punta a punta. Esto es un diagrama para leer con calma, porque el módulo 6 lo implementa y el módulo 7 lo extiende.

```
   NAVEGADOR                                                SERVIDOR
       │                                                        │
       │  ① GET /login                                          │
       │───────────────────────────────────────────────────────►│
       │◄──── pantalla de login (HTML) ─────────────────────────│
       │                                                        │
       │  ② POST /login  usuario=admin  clave=1234              │
       │───────────────────────────────────────────────────────►│
       │                                                        │ ③ busca el usuario, compara la clave
       │                                                        │ ④ crea la sesión: 7f3a9c → {admin, ADMINISTRADOR}
       │◄──── 302 redirect a /alumnos ──────────────────────────│
       │      Set-Cookie: session=7f3a9c                        │ ⑤ le manda la llave
       │                                                        │
       │  (el navegador guarda la cookie)                       │
       │                                                        │
       │  ⑥ GET /alumnos                                        │
       │      Cookie: session=7f3a9c                            │ ⑦ lee la cookie, busca la sesión,
       │───────────────────────────────────────────────────────►│    encuentra: admin, ADMINISTRADOR
       │◄──── tabla CON botones de administrador ───────────────│ ⑧ responde según lo que encontró
       │                                                        │
       │  ⑨ GET /alumnos/nuevo   Cookie: session=7f3a9c         │
       │───────────────────────────────────────────────────────►│ ... misma sesión, mismo usuario
       │                                                        │
       │  ⑩ POST /logout         Cookie: session=7f3a9c         │
       │───────────────────────────────────────────────────────►│ ⑪ borra la fila 7f3a9c de la tabla
       │◄──── redirect a /login,  Set-Cookie: session= (vacía) ─│ ⑫ y le dice al navegador que la olvide
```

Ahora el enigma del módulo 1 tiene respuesta: el docente hace el mismo `GET /alumnos`, pero **su cookie apunta a otra fila de la tabla**, donde dice DOCENTE, y el servidor responde con la versión sin botones.

```
// ¿CÓMO FUNCIONA? (dos personas, el mismo GET)
//  admin:   GET /alumnos, Cookie: session=7f3a9c  → tabla[7f3a9c] = {admin, ADMINISTRADOR}   → HTML con botones
//  docente: GET /alumnos, Cookie: session=b81e04  → tabla[b81e04] = {docente, DOCENTE}       → HTML sin botones
//  Mismo controller. Lo que cambia es lo que el servidor encuentra en la sesión.
//  (Cómo el controller o la plantilla "preguntan" qué rol tiene la sesión: módulo 6.)
```

Y el logout: **cerrar sesión es borrar la fila.** Aunque alguien tuviera la cookie `7f3a9c` guardada, ya no apunta a nada. El servidor tiene el control total de qué sesiones existen. Esto va a importar en el módulo 5, porque con la alternativa (tokens) ese control se pierde.

Esta arquitectura, donde el servidor guarda estado de cada persona entre pedidos, se llama **stateful** (con estado). El módulo 5 le pone nombre a la contraria.

---

## 5. 🔴 Lo que la cookie es y lo que no es: el riesgo

De la sección 3: la cookie es la llave, no la identidad. Consecuencia directa, y es la que hay que entender bien:

> **Quien tenga la cookie, es vos.** El servidor no verifica nada más: lee `session=7f3a9c`, encuentra la fila, y responde como si fueras el administrador.

Antes del experimento, mirala: logueate, abrí las herramientas de desarrollador, pestaña *Application* → *Storage* → *Cookies* → tu `localhost`. Hay una fila `JSESSIONID` con un valor de 32 caracteres hexadecimales, tipo `52D1D82CE6CB355FA500D768336D0365`. Eso es tu `session=7f3a9c`. Y en *Network*, en cualquier request a tu servidor, los *Request headers* traen `Cookie: ...; JSESSIONID=52D1D82C...` — el navegador la mandó solo. (Si en esa cabecera ves también otras cookies con nombres raros, son de otras aplicaciones que corrieron en `localhost` alguna vez; el navegador las manda todas juntas y a tu servidor solo le importa la suya.)

Un experimento mental que lo demuestra: en esa misma pantalla, copiá el valor de la cookie de sesión. Abrí **otro** navegador, andá al sitio, y antes de loguearte pegá esa cookie a mano. Refrescá. **Estás adentro, como administrador, sin haber escrito la contraseña.** El segundo navegador mandó `Cookie: session=7f3a9c` y el servidor hizo lo único que sabe hacer: buscar la fila.

Eso no es un defecto del mecanismo: es el mecanismo. Y es la razón de tres cosas que vas a ver dando vueltas:

- **HTTPS.** Si la cookie viaja en texto plano por la red, cualquiera en el medio la lee y la copia. Con HTTPS viaja cifrada.
- **Las sesiones expiran.** Una cookie robada de una sesión que venció en 30 minutos sirve poco. Una de una sesión eterna sirve para siempre.
- **La cookie se protege.** No se expone en URLs, no se loguea, no se le da al JavaScript más de lo necesario.

Ninguna de las tres se implementa en este material (son configuración, no concepto), pero el porqué de las tres es este: la cookie **es** la identidad frente al servidor.

---

## 6. 🟡 Cookie contra `localStorage`

Los navegadores tienen otro lugar para guardar datos, `localStorage`, y conviene saber por qué no es lo mismo:

| | Cookie | `localStorage` |
|---|---|---|
| ¿Quién la crea? | El servidor (`Set-Cookie`); también puede el JavaScript | Solo el JavaScript en el navegador |
| ¿Viaja al servidor? | **Sí, sola, en cada request** (cabecera `Cookie`) | **No.** Nunca viaja por sí mismo |
| ¿Cómo la usa el servidor? | La lee directamente de la cabecera | No puede: tendría que ser el JavaScript el que la lea y la mande explícitamente |
| Uso típico | Identificador de sesión | Datos del lado del cliente (preferencias, borradores) |

Para una sesión, la cookie es la herramienta porque **viaja sola**: el servidor la recibe en cada pedido sin que nadie tenga que acordarse de mandarla. Lo que está en `localStorage` es invisible para el servidor hasta que un script lo lea y lo envíe.

---

## 7. 🟡 La sesión en código: guardar y leer

En Spring, la sesión de la persona que hizo el pedido se llama `HttpSession`, y se obtiene desde el request. Guardar algo y leerlo en un pedido posterior son dos líneas:

```java
// En un pedido cualquiera (por ejemplo, después de un login exitoso):
HttpSession session = request.getSession();          // la sesión de ESTA persona. Si no existe, la crea
                                                     // y manda el Set-Cookie por vos.
session.setAttribute("username", "admin");           // clave → valor, como el Model, pero persiste entre pedidos

// En OTRO pedido, más tarde, de la misma persona (el navegador mandó la cookie):
HttpSession session = request.getSession();          // Spring lee la cookie, encuentra la sesión, te la da
String username = (String) session.getAttribute("username");   // "admin". Sobrevivió entre pedidos.
```

```
// ¿CÓMO FUNCIONA?
//  1. Primer pedido: request.getSession() no encuentra cookie de sesión → crea una sesión nueva,
//     le asigna un ID, y agrega Set-Cookie a la respuesta. Guardás "username".
//  2. El navegador guarda la cookie.
//  3. Segundo pedido: llega con Cookie. request.getSession() lee el ID, encuentra la misma sesión,
//     y getAttribute("username") devuelve lo que guardaste.
//
//  Resultado esperado: un valor puesto en un pedido, leído en otro. Eso es lo que HTTP solo no puede hacer.
```

> **`JSESSIONID`:** es el nombre que Spring (y Java en general) le da a la cookie de sesión. Cuando abras las herramientas del navegador vas a ver `JSESSIONID=...`; es el `session=7f3a9c` de los diagramas.

Dos cosas del módulo 3 que ahora cierran:

- **Los atributos flash viven acá.** `addFlashAttribute` guarda el valor en la sesión; en el pedido siguiente Spring lo saca, lo pone en el `Model` y lo borra de la sesión. Por eso sobrevive exactamente un redirect.
- **Por qué el módulo 7 va a usar esto.** En el TP, lo que se guarda en la sesión después del login no es solo el nombre de usuario: son los tokens con los que el servidor de front va a hablar con los otros servicios. Mismo `setAttribute`, otro contenido.

> 🕳️ **Madriguera — los `<meta name="_csrf">` del layout**
> En el `<head>` del layout aparecen dos etiquetas `_csrf` y `_csrf_header`. CSRF es un tipo de ataque que se aprovecha justamente de que la cookie viaja sola; Spring Security lo bloquea en aplicaciones con sesión pidiendo un token extra en cada formulario, y Thymeleaf lo agrega solo en cada `th:action`. Esas etiquetas exponen el token para el JavaScript que haga pedidos por su cuenta. Aparece al pasar en el módulo 6.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 8. 🟡 Dónde viven las sesiones

La tabla de la sección 2 tiene que estar en algún lado. Tres opciones:

| Dónde | Qué pasa |
|---|---|
| **En memoria** (lo que hace Spring por defecto) | Rápido y simple. Si el servidor se reinicia, **todas las sesiones se pierden**: todo el mundo queda deslogueado. |
| **En una base de datos** | Sobreviven a un reinicio. Cada lectura de sesión es una consulta. |
| **En archivos** | Sobreviven a un reinicio; poco usual. |

Para el TP, en memoria alcanza. Pero fijate lo que revela: **la sesión es estado del servidor.** Está en la memoria de *esa* máquina. Si mañana el sistema tiene tanto tráfico que hacen falta dos servidores, ¿cuál de los dos tiene tu sesión? Si tu segundo pedido cae en el otro servidor, no te conoce. Ese es el **hilo H7** en toda su dimensión, y la razón por la que existe la arquitectura del módulo 5.

---

🧩 **En tu TP** — el servidor de front va a ser **stateful**: la persona se loguea contra el front, el front le crea una sesión, y el navegador lleva la cookie en cada pedido. Todo lo de este módulo aplica tal cual a la relación *navegador ↔ servidor de front*. Lo que va a cambiar es la relación *servidor de front ↔ servicios*, que no va a usar sesión: módulo 5 (por qué) y módulo 7 (cómo).

---

## Checkpoint del módulo 4

Sin respuestas.

1. ¿Por qué el servidor no puede saber, por sí solo, que el `GET /alumnos` viene de la misma persona que acaba de hacer `POST /login`?
2. ¿Qué es una sesión, dónde vive y qué guarda? Nombrá tres cosas que suele contener.
3. ¿Qué viaja en la cookie: los datos del usuario o una referencia? ¿Qué diferencia práctica tiene eso?
4. ¿En qué cabecera manda el servidor la cookie y en qué cabecera la devuelve el navegador?
5. ¿Qué tiene que hacer el JavaScript de la página para que la cookie viaje en cada request?
6. Dos personas hacen el mismo `GET /alumnos` y reciben respuestas distintas. Explicá el mecanismo con las palabras "cookie", "tabla" y "fila".
7. ¿Qué significa "cerrar sesión" del lado del servidor? ¿Qué pasa con una cookie copiada después de eso?
8. Alguien copia tu cookie de sesión a otro navegador. ¿Qué ve el servidor cuando ese navegador hace un pedido? ¿Por qué eso no es un bug?
9. ¿Por qué `localStorage` no sirve para reemplazar a la cookie en una arquitectura con sesión?
10. Un sistema con sesiones en memoria pasa de un servidor a dos. ¿Qué problema aparece y por qué?

---

## Qué viene en el módulo 5

Sesión y cookie funcionan, pero cuestan: memoria por cada persona conectada, y estado atado a una máquina. El módulo 5 muestra la arquitectura opuesta, **stateless**, donde el servidor **no guarda nada** y toda la información viaja en cada pedido dentro de un **token**. Vas a ver qué es un JWT por dentro, por qué una firma alcanza para confiar en él, qué se gana (escalar) y qué se pierde (no poder "cerrar sesión"), y por qué existen dos tokens en vez de uno. Se cierra la primera mitad del **hilo H7**.

---

**FIN DEL MÓDULO 4**
