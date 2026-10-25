# Clase desde cero — Cliente Liviano — Módulo 1
# Qué es un cliente liviano y qué cambia

**Fase B — La herramienta** · Densidad general: 🔴 (todo lo de acá es evaluable: son las definiciones sobre las que se apoya la unidad entera)

---

## Sobre este documento

**Cubre:** por qué lo que armaste en el módulo 0 se llama *cliente liviano*, cuál es la alternativa (*cliente pesado*) y qué se gana y se pierde con cada una; las dos formas de cliente liviano (monolítico y desacoplado) y cuál pide el TP; la anatomía completa de un pedido de pantalla, incluido el *redirect*; qué es un **recurso** y cómo define qué pantallas y rutas existen; y una pregunta que queda abierta a propósito: cómo sabe el servidor quién le está hablando.

**NO cubre:** la sintaxis de plantillas (módulo 2), formularios (3), la respuesta a la pregunta abierta (4), ni cómo se conecta el servidor de front con los servicios (7). El cliente pesado se ve solo lo justo para contrastarlo; su desarrollo completo es de una clase posterior.

## De dónde venís

- **Módulo 0:** viste el flujo controller → `Model` → plantilla → HTML → navegador, y que el navegador pide los CSS/JS por separado después. Eso es lo único que este módulo asume de la unidad.
- **De clases anteriores:** el patrón cliente-servidor y el protocolo HTTP; qué es una API REST y que devuelve JSON; que un DTO es lo que entra y sale de un *service*; y la relación de **composición** entre objetos (un objeto que "es parte de" otro y no tiene sentido sin él).

---

## 1. 🔴 El problema: alguien tiene que armar la pantalla

Hasta ahora los servicios que venís construyendo exponen una API REST: reciben un pedido, devuelven JSON.

```json
[
  { "legajo": "12345", "nombre": "Juan", "apellido": "Pérez" },
  { "legajo": "67890", "nombre": "María", "apellido": "González" }
]
```

Eso lo lee un programa. Una persona no quiere ver eso: quiere ver una tabla con un botón de "editar" al lado de cada fila.

En el momento en que aparece **una persona** del otro lado, aparece la pregunta que organiza toda esta unidad:

> Cuando se tiene un sistema con arquitectura web y hace falta una capa de presentación visual, surge la necesidad de definir **quién va a tener la responsabilidad de generar las vistas**.

"Vista" = pantalla. Y hay exactamente dos respuestas posibles.

---

## 2. 🔴 Cliente liviano vs cliente pesado

### 2.1 Cliente liviano (*server side render*)

**El servidor arma la pantalla.** Recibe el pedido, va a buscar los datos, genera el HTML completo con los datos adentro, y lo devuelve. El navegador lo único que hace es mostrarlo.

```
   NAVEGADOR                              SERVIDOR
   (cliente liviano)                      (hace todo el trabajo)
       │                                       │
       │──── GET /alumnos ────────────────────►│
       │                                       │  1. busca los alumnos
       │                                       │  2. arma el HTML con la tabla y los botones
       │◄─── HTML completo ────────────────────│
       │                                       │
       │  3. lo dibuja. Nada más.              │
```

Es **exactamente** lo que hiciste en el módulo 0: el controller puso datos en el `Model`, Thymeleaf armó el HTML, el navegador lo mostró. Se lo llama *server side render* (SSR): renderizar = generar la vista; *server side* = del lado del servidor.

Formalmente:

- Las vistas se generan **del lado del servidor**.
- Las respuestas a las solicitudes son **casi siempre HTML**.
- El navegador se encarga **solamente de mostrar** el contenido.
- En ciertos casos puede haber lógica en JavaScript del lado del cliente, pero para funcionalidades chicas (abrir un cuadro de confirmación, agregar un campo a un formulario). El grueso está en el servidor.

### 2.2 Cliente pesado (*client side render*)

**El navegador arma la pantalla.** El servidor no devuelve HTML: devuelve JSON, como tus servicios de siempre. En el navegador corre una aplicación JavaScript que recibe ese JSON y construye la vista con él.

```
   NAVEGADOR                              SERVIDOR
   (cliente pesado: tiene una app JS)     (solo da datos)
       │                                       │
       │──── GET /api/alumnos ────────────────►│
       │                                       │  busca los alumnos
       │◄─── JSON ─────────────────────────────│
       │                                       │
       │  la app JS arma la tabla,             │
       │  los botones, todo.                   │
```

Se lo llama *client side render* (CSR). Es lo que se hace en Desarrollo de Software con React: el servidor es una API y la pantalla la fabrica el navegador.

### 2.3 La comparación

| | Cliente liviano (SSR) | Cliente pesado (CSR) |
|---|---|---|
| ¿Quién genera la vista? | El servidor | El navegador (una app JS) |
| ¿Qué viaja del servidor al navegador? | HTML ya armado | JSON |
| ¿Qué hace el navegador? | Solo mostrar | Recibir datos y construir la pantalla |
| ¿Dónde corre la lógica de presentación? | En una máquina que controlás vos | En el dispositivo de cada usuario, que no controlás |
| Materia donde se usa | **Diseño de Sistemas (esta)** | Desarrollo de Software |

Ese cuarto renglón es el argumento de diseño: en el cliente pesado, la pantalla se arma en un dispositivo que **no controlás** (puede ser un celular viejo, una red lenta, un navegador raro). En el cliente liviano, todo el procesamiento pasa en tu servidor, y el navegador solo tiene que mostrar. Ninguno es "mejor": son dos repartos distintos de la responsabilidad, con consecuencias distintas.

**En esta materia se trabaja con cliente liviano.** Todo lo que sigue en esta clase desde cero es cliente liviano.

> 🕳️ **Madriguera — casos mixtos**
> Existen clientes intermedios: parte de la pantalla la arma el servidor y parte la completa JavaScript en el navegador. Es "un pesado menos pesado". Se ve en una clase posterior junto con el cliente pesado.
> *Volvé al camino — esto se profundiza aparte, otro día.*

Cómo verificar cuál de los dos tenés enfrente, sin leer código: herramientas de desarrollador, pestaña *Network*, hacé clic en la request de la página y mirá los *Response headers*. Un cliente liviano responde `Content-Type: text/html;charset=UTF-8`. Una API responde `Content-Type: application/json`. El módulo 0 termina en el primero.

🎓 **Si te lo preguntan — ¿qué diferencia hay entre un cliente liviano y uno pesado?**
En el cliente liviano las vistas se generan del lado del servidor y viajan como HTML; el navegador solo las muestra. En el cliente pesado el servidor devuelve datos (JSON) y la vista la genera el navegador con una aplicación JavaScript. La diferencia es quién tiene la responsabilidad de generar la vista.

---

## 3. 🔴 Dos formas de cliente liviano: monolítico y desacoplado

Decidiste que el servidor arma la pantalla. Falta decidir **dónde están los datos** con los que la arma.

### 3.1 Monolítico: todo en un solo proyecto

El mismo servidor que arma las pantallas tiene también las entidades de dominio, los repositorios, la lógica de negocio. Un solo proyecto, todo adentro.

```
   ┌────────────────────────── UN SOLO PROYECTO ──────────────────────────┐
   │                                                                      │
   │   controllers  →  services  →  repositorios  →  entidades (dominio)  │
   │        │                                                             │
   │        └──►  templates/ (las pantallas)                              │
   │                                                                      │
   └──────────────────────────────────────────────────────────────────────┘
```

Es la forma más simple, y por eso es la que usan los **módulos 2 a 6** para enseñar cada pieza sin ruido: el ejemplo de gestión de alumnos que vas a ver ahí tiene sus alumnos guardados en el mismo proyecto que arma las pantallas.

### 3.2 Desacoplado: el servidor de front no tiene los datos

El servidor de front **solo arma pantallas**. No tiene entidades de dominio. No tiene repositorios. No sabe qué es un alumno más allá de "un DTO con estos campos". Cuando necesita datos, **se los pide por HTTP a los servicios** que ya existen, exactamente como lo haría Postman.

```
   NAVEGADOR          SERVIDOR DE FRONT                  SERVICIOS (los que ya tenés)
                      (solo vistas + DTOs)
       │                     │                                    │
       │── GET /donaciones ─►│                                    │
       │                     │── GET /api/donaciones ────────────►│  servicio de donaciones
       │                     │◄─ JSON ────────────────────────────│
       │                     │                                    │
       │                     │  convierte el JSON en DTOs,        │
       │                     │  arma el HTML con Thymeleaf        │
       │◄─ HTML completo ────│                                    │
```

El servidor de front se comporta, hacia adelante, como un cliente liviano (devuelve HTML) y, hacia atrás, como un **cliente de las APIs REST** (consume JSON). Está en el medio. Por eso también se lo llama **servidor frontend**, y en la jerga de la industria **BFF** (*backend for frontend*: un backend cuyo único cliente es el front).

Lo que **sí** tiene y lo que **no** tiene un servidor de front desacoplado:

| Tiene | No tiene |
|---|---|
| Controllers que devuelven vistas | Entidades de dominio |
| `templates/` y `static/` | Repositorios |
| **DTOs** (los datos que vienen de los servicios, tal como llegan) | Lógica de negocio |
| Services que **llaman a otros servicios por HTTP** | Base de datos propia |

🧩 **En tu TP:** el enunciado pide un **cliente liviano desacoplado**: "un nuevo proyecto independiente para la interfaz gráfica, desacoplado de la lógica de negocio, consumiendo las APIs REST desarrolladas en entregas anteriores". Es exactamente la forma 3.2. Y como tus datos están repartidos en **cuatro** servicios (donaciones, logística, incentivos, notificaciones), el servidor de front va a tener que hablar con los cuatro y **orquestar** lo que le devuelven para armar una sola pantalla. Eso es lo que hace que la arquitectura del TP sea bastante más compleja que un monolito común — y es lo que cierra el módulo 7. **Hilo H6 abierto.**

> 🕳️ **Madriguera — ¿esto es un microservicio más?**
> Ni sí ni no. En la teoría de microservicios el componente que se sienta entre el front y los servicios tiene otro nombre y otras responsabilidades. Acá lo estás construyendo antes de tener esa teoría, sumándole además la responsabilidad de las vistas. Se termina de entender en la clase de microservicios.
> *Volvé al camino — esto se profundiza aparte, otro día.*

🎓 **Si te lo preguntan — ¿qué significa que el cliente liviano sea "desacoplado"?**
Que el servidor que genera las vistas está separado de la lógica de negocio: no tiene entidades de dominio ni repositorios, solo DTOs, y obtiene los datos consumiendo por HTTP las APIs REST de los servicios de aplicación.

---

## 4. 🟡 Anatomía de un pedido de pantalla (completa)

En el módulo 0 viste el camino feliz. Este es el camino completo, con las dos piezas que faltaban: el *service* y el *redirect*.

```
  1. GET /alumnos ──────────────────────────────────────────► [Tomcat] recibe
  2.                                                          [Spring] busca el @GetMapping que matchea
  3.                                                          [Controller] llama al service
  4.                                                          [Service] consigue los datos ← (hilo H2: hoy en memoria,
  5.                                                          [Controller] los mete en el Model       mañana de otro servicio)
  6.                                                          [Controller] devuelve "alumnos/lista"
  7.                                                          [Thymeleaf] plantilla + Model → HTML
  8. ◄──────────────────────────────── 200 OK, text/html ──── [Spring] responde
  9. el navegador dibuja
 10. GET /css/style.css, GET /js/lista.js ──────────────────► [Spring] los sirve desde static/
 11. el JavaScript corre EN EL NAVEGADOR, recién ahora
```

Dos precisiones:

**El controller no calcula, delega.** Su trabajo es recibir el pedido, pedirle los datos al *service*, ponerlos en el `Model` y elegir la plantilla. Si hay que calcular una estadística, eso lo hace el *service* y el controller solo lo pasa. Un controller con lógica adentro es una mala señal.

En números, para que no quede abstracto: la pantalla de listado del ejemplo son **8 requests**: una para `/alumnos` (el documento, ~10 kB, 200) y siete para lo que el HTML referencia (dos CSS externos, `style.css`, dos `.js` propios, el `.js` de Bootstrap, una fuente). Al navegar al detalle de un alumno, otras 8 con la misma forma; los archivos que no cambiaron vienen marcados *(disk cache)*: el navegador no los volvió a pedir. Todo eso se ve en *Network*.

**El JavaScript no es del servidor.** El paso 11 se olvida seguido: un `.js` que está en `static/` del proyecto Spring **no corre en Spring**. Viaja al navegador y corre ahí, después de que la página llegó. Es la parte "chica de lógica del lado del cliente" de la sección 2.1.

### 4.1 El redirect: "andá a pedir otra cosa"

A veces el controller no quiere devolver una pantalla sino **mandarte a otra ruta**. Por ejemplo, que la raíz `/` te lleve al listado:

```java
@GetMapping("/")
public String home() {
    return "redirect:/alumnos";     // NO es un nombre de plantilla: el prefijo "redirect:" cambia el significado
}
```

```
// ¿CÓMO FUNCIONA?
//  1. Navegador: GET /
//  2. El controller devuelve "redirect:/alumnos".
//  3. Spring NO busca una plantilla. Responde al navegador: "302, andá a /alumnos".
//  4. El navegador, solo, hace un SEGUNDO pedido: GET /alumnos.
//  5. Ese segundo pedido cae en el controller de /alumnos, que sí devuelve una pantalla.
//
//  Resultado esperado: escribís localhost:8090/ en la barra, y la barra cambia sola a
//  localhost:8090/alumnos y ves el listado. Dos requests, no una.
```

Lo importante: **un redirect es una respuesta que le dice al navegador que haga otro pedido.** No es el servidor "saltando" de una pantalla a otra por dentro. Vas a usar esto muchísimo en el módulo 3 (después de crear algo, redirigir al detalle de lo creado) y en el módulo 6 (después de loguearte, redirigir a la pantalla principal; sin permiso, redirigir a una pantalla de error).

---

## 5. 🔴 Recursos: qué expone tu sistema

Antes de hacer pantallas, hay que decidir **de qué** son las pantallas. Esa decisión tiene nombre.

> **Recurso:** una entidad que puede ser **manipulada**: dada de alta, modificada, eliminada o listada (buscada). Para identificar qué recursos debe exponer un sistema se parte de los **casos de uso**.

En un sistema de gestión de alumnos, *alumno* es un recurso: se listan, se crean, se editan, se eliminan. Cada una de esas acciones termina siendo una ruta y, casi siempre, una pantalla.

### 5.1 Dos tipos de recurso

| Tipo | Qué es | Ejemplo | Consecuencia |
|---|---|---|---|
| **Independiente / autónomo** | Tiene sentido de existencia propio. No depende de nadie. | `Alumno` | Tiene sus propias rutas y pantallas: listar, ver, crear, editar. |
| **Dependiente / anidado** | No es autónomo: depende de la existencia de otro. No tiene sentido listarlo sin su contenedor. | `Contacto` de un alumno (un email, un teléfono) | **No** tiene pantalla propia. Se ve, se crea y se edita **adentro** de la pantalla del alumno. |

A nivel de objetos, el recurso dependiente está en una relación de **composición** con el contenedor: el contacto es parte del alumno, y un contacto sin alumno no existe.

Esto es lo que evita hacer pantallas que no tienen sentido. Nadie quiere una pantalla "listado de todos los teléfonos del sistema", ni una ruta `/contactos`. Los teléfonos se ven en el detalle del alumno al que pertenecen.

### 5.2 Cómo se traduce en rutas

Con un recurso independiente identificado, sus rutas siguen una convención fija: **el recurso en plural, y el identificador dentro de la ruta**. Para `Alumno`, identificado por su legajo:

| Acción | Ruta | Qué devuelve |
|---|---|---|
| Listar | `GET /alumnos` | La pantalla con la tabla |
| Ver uno | `GET /alumnos/{legajo}` | La pantalla de detalle, con sus contactos adentro |
| Formulario de alta | `GET /alumnos/nuevo` | La pantalla con el formulario vacío |
| Formulario de edición | `GET /alumnos/{legajo}/editar` | La pantalla con el formulario ya cargado |
| Crear, actualizar, eliminar | `POST` a `/alumnos/crear`, `/alumnos/{legajo}/actualizar`, `/alumnos/{legajo}/eliminar` | No devuelven pantalla: hacen la acción y **redirigen**. Se ven en el módulo 3. |

`{legajo}` es una **variable de ruta**: `GET /alumnos/12345` cae en el mismo método que `GET /alumnos/67890`, y el método recibe el valor. En el módulo 2 vas a ver cómo se lee.

Fijate que `Contacto` no aparece en ninguna ruta. Está adentro de `/alumnos/{legajo}`. Eso es lo que significa "dependiente" en la práctica.

🧩 **En tu TP:** el mismo análisis, recurso por recurso. Por ejemplo: *donación* es independiente (se lista, se crea, se ve el detalle); el *estado* de una donación o su *entrega* son dependientes (no tiene sentido una pantalla de "todos los estados": se ven adentro de la donación). *Camión* y *chofer* son independientes dentro de logística. Cada recurso independiente que identifiques es un juego de rutas y pantallas; cada dependiente, una sección adentro de la pantalla de su contenedor. Esa lista es la mitad del diseño del front.

**Hilo H5, abierto en serio:** todas estas rutas, hoy, son públicas. Quien escriba `localhost:8090/alumnos/12345/editar` en la barra entra a editar, sin preguntarle quién es. El módulo 6 lo cierra.

🎓 **Si te lo preguntan — ¿qué es un recurso y qué tipos hay?**
Un recurso es una entidad que el sistema permite manipular: dar de alta, modificar, eliminar o listar. Los recursos independientes tienen sentido de existencia propio y exponen sus propias rutas; los recursos dependientes o anidados dependen de otro (relación de composición) y solo se manipulan a través de su recurso contenedor.

---

## 6. 🟡 Una pregunta que queda abierta: ¿quién me está pidiendo esto?

Cerrá el módulo con esta situación, que es la que arranca la próxima fase.

Un sistema tiene dos tipos de persona: **administrador** y **docente**. Los dos entran a `/alumnos`. El administrador ve la tabla **con** el botón "Nuevo alumno" y los atajos de editar y eliminar. El docente ve la tabla **sin** nada de eso.

```
   administrador ──── GET /alumnos ────► servidor ────► tabla + botón "Nuevo" + editar + eliminar
   docente       ──── GET /alumnos ────► servidor ────► tabla, nada más
```

**Es el mismo `GET /alumnos`.** Misma ruta, mismo método, mismo controller. Y sin embargo la respuesta es distinta. Algo pasa en el servidor que le permite saber cuál de los dos le está hablando.

El problema de fondo es que **HTTP no tiene memoria**: cada pedido llega solo, sin ninguna referencia al pedido anterior. El servidor recibe `GET /alumnos` y, por sí mismo, no tiene forma de saber si ese pedido viene de la misma persona que se logueó hace un minuto o de un desconocido. Con lo que sabés hasta acá, no hay forma de resolverlo.

**Hilo H4 abierto.** El módulo 4 lo cierra, y el nombre de la solución es *sesión*.

---

## Checkpoint del módulo 1

Sin respuestas. Si alguna no te sale, es la duda para el chat antes del módulo 2.

1. ¿Quién genera la vista en un cliente liviano y quién en un cliente pesado? ¿Qué viaja del servidor al navegador en cada caso?
2. ¿Por qué "cliente liviano" se llama también *server side render*?
3. Un compañero dice: "el cliente liviano es mejor porque el servidor hace todo". ¿Qué le falta a esa afirmación?
4. ¿Qué tiene y qué no tiene un servidor de front desacoplado? Nombrá dos cosas de cada lado.
5. ¿Por qué el TP pide específicamente un cliente liviano **desacoplado** y no uno monolítico?
6. Explicá con tus palabras qué hace un `return "redirect:/alumnos"` y por qué termina generando dos requests.
7. ¿Qué diferencia hay entre un recurso independiente y uno dependiente? Dá un ejemplo de cada uno que **no** sea alumno/contacto.
8. ¿Por qué no existe una ruta `/contactos`?
9. En la ruta `GET /alumnos/{legajo}/editar`, ¿qué es `{legajo}`?
10. Dos personas hacen exactamente el mismo `GET /alumnos` y reciben pantallas distintas. ¿Qué tendría que saber el servidor para que eso sea posible, y por qué HTTP por sí solo no se lo dice?

---

## Qué viene en el módulo 2

Plantillas en serio. Hoy tu pantalla muestra dos textos que el controller escribió a mano. En el módulo 2 el controller le pasa una **lista** de alumnos y la plantilla la recorre y arma la tabla sola; muestra un botón solo **si** se cumple algo; arma URLs con el legajo adentro; y, sobre todo, resuelve el **hilo H1**: cómo escribir el `<head>`, la barra de navegación y el pie **una sola vez** para que 30 pantallas los hereden, en vez de copiarlos 30 veces.

---

**FIN DEL MÓDULO 1**
