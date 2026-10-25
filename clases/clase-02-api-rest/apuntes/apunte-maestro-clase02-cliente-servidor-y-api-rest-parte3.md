# Apunte Maestro — Clase 02 · Parte 3
## API y API REST

---

## 🧭 De dónde venís

**Parte 1:** el patrón cliente-servidor. **Parte 2:** el protocolo HTTP — sincrónico, sin estado, con sus métodos y códigos de estado.

**Esta parte es el cierre de la teoría**, y los docentes lo dijeron así: *"por eso fuimos entrando de cliente-servidor, HTTP, ahora API… es el cierre, donde juntamos todo lo que estuvimos charlando"*.

---

## 1. Qué es una API 🔴

> **API** — *Application Programming Interface*, interfaz de programación de aplicaciones.
>
> Un conjunto de herramientas, definiciones y protocolos que se usan para diseñar e integrar aplicaciones. **Permite que un producto o servicio se comunique con otros, sin necesidad de saber cómo se implementan internamente.**

**Recordá de la clase 1:** un componente expone una **interfaz** — un contrato público que dice *qué le podés pedir*, ocultando *cómo lo resuelve*. **Una API es exactamente eso, escrito de forma concreta.**

### 🔴 El error nº 1: creer que toda API es una API web

*"Tenemos un problema en la industria: decimos API y ya nos imaginamos que es **una** API. Y no siempre."*

**Una API no es necesariamente algo que corre en otro servidor.** Mirá los ejemplos que dio la cátedra — y fijate que **ninguno de los cinco es un servicio remoto**:

| API | Qué expone | ¿Corre en otro nodo? |
|---|---|---|
| **Stream API de Java** | Métodos para manipular flujos de datos | ❌ **No.** Es una biblioteca. Está en tu proyecto. |
| **JPA** (Java Persistence API) | El estándar para el mapeo objeto-relacional | ❌ No. Es una dependencia. |
| **JDBC** (Java Database Connectivity) | Clases e interfaces para hablar con bases relacionales | ❌ No (aunque la base sí esté afuera). |
| **Win32 API** (Windows) | Funciones para acceder a ventanas, archivos, procesos, memoria | ❌ No. Es el sistema operativo. |
| **POSIX API** (Linux) | Operaciones de bajo nivel: archivos, procesos, concurrencia | ❌ No. |

> **La conclusión, que es la que hay que llevarse:**
>
> **API es un concepto genérico: cualquier interfaz que un componente expone para que otro la use.** Puede ser una biblioteca, un framework, el sistema operativo, o un servicio remoto.
>
> **API REST es UN caso particular** — el caso en que esa interfaz **es web y distribuida**: se expone por HTTP, y quien la consume corre en otra máquina.

Cuando en la clase 4 uses la **cámara del celular desde una app**, no vas a hablar con el hardware: vas a llamar a la **API que el sistema operativo te expone**. Misma idea, otro contexto.

---

## 2. Qué es REST 🔴

> **REST** — *REpresentational State Transfer*.
>
> Un **estilo de diseño de comunicación** entre cliente y servidor.

### ⚠️ Lo que la cátedra remarca — y hay que responder así

Escobar frenó explícitamente para aclararlo:

> *"Lo que no queremos que se confunda es que **REST no es un estilo arquitectónico**, ni tampoco **es un patrón de diseño**. REST es una forma particular de diseñar la comunicación cliente-servidor, a través de una serie de criterios."*

⚠️ **Nota honesta:** en la literatura (empezando por la tesis de Roy Fielding, que lo definió) a REST **sí** se lo llama habitualmente *architectural style* — estilo arquitectónico. Es una divergencia real de terminología.

**Para el parcial: respondé con la definición de la cátedra.** REST es un **estilo de diseño de comunicación**, definido por un conjunto de criterios, y **no** es ni un estilo arquitectónico ni un patrón de diseño. El profe es quien evalúa.

### Para qué sirve, en una frase

Un alumno lo resumió en clase y el profe le dijo *"exacto"*:

> **REST define un estándar para que dos partes se pongan de acuerdo sin negociar nada.**

Cuando decís *"te expongo una API REST"*, tu interlocutor **ya sabe** —sin que le digas una palabra más— que:

- van a hablar por **HTTP**,
- vas a usar los **verbos HTTP** (GET, POST, PUT, PATCH, DELETE),
- vas a devolver **códigos de estado HTTP**,
- vas a **identificar recursos por URI**,
- y el **formato de intercambio va a ser JSON**.

Cinco acuerdos, gratis, con dos palabras. **Ese es todo el negocio de un estándar.**

---

## 3. Los criterios de REST 🔴

### ① Se apoya sobre el protocolo HTTP

No inventa nada nuevo. **Usa lo que ya viste en la Parte 2.**

### ② Cada request es autocontenida 🔴

> *"Un REQUEST a un servidor tiene **toda la información necesaria** y solo espera una RESPONSE."*

**Esto no es una regla nueva: es la consecuencia directa de que HTTP sea *stateless*** (Parte 2, sección 2). REST no pelea contra esa característica del protocolo — **la abraza y la convierte en principio de diseño**.

```
   ❌ NO podés hacer esto:
      GET /clientes/5           → "dame el cliente 5"
      GET /facturas             → "dame las facturas del cliente que te pedí recién"
                                   ↑ el servidor NO SABE de qué cliente hablás.

   ✅ Tenés que hacer esto:
      GET /clientes/5           → obtengo el cliente y su ID
      GET /clientes/5/facturas  → toda la info va en la solicitud
```

**El proceso es: solicitud → respuesta → FIN.** ¿Querés otra cosa? Armá **otra solicitud completa**.

### ③ Se apoya en los verbos HTTP 🔴

| Verbo | Uso en REST |
|---|---|
| **POST** | Crear recursos nuevos |
| **GET** | Obtener un listado o un recurso concreto |
| **PUT** | Modificar un recurso (completo) |
| **PATCH** | Modificar un recurso (parcial) |
| **DELETE** | Borrar un recurso |

### ④ Los recursos se identifican mediante URI 🔴

> **URI** — *Uniform Resource **Identifier***, identificador uniforme de recursos.

⚠️ **URI ≠ URL, y el profe lo remarcó:** la **L** de URL es de *Locator* (**ubicación**); la **I** de URI es de *Identifier* (**identificador**). En REST hablamos de **identificar** el recurso, no solo de ubicarlo.

**¿Qué es un "recurso"?** Cualquier entidad que tu sistema maneja: usuarios, ventas, facturas, donaciones. **Y cada una tiene que tener su identificador propio y unívoco en la ruta.**

```
   POST  /clientes        → creo un cliente
   GET   /clientes/5      → obtengo EL cliente 5
                     ↑
                  el identificador. Unívoco.
```

### ⑤ El formato de intercambio es JSON (o XML) 🔴

**JSON está en negrita en la slide, y no es casual: es el default.**

**¿Por qué JSON y no XML?** Porque el **stream es más liviano**. XML repite cada etiqueta dos veces (apertura y cierre) — JSON no. Menos bytes viajando por la red, para los mismos datos.

---

## 4. JSON 🟡

> **JSON** — *JavaScript Object Notation*. Formato de texto liviano y legible para el intercambio de datos.

**Simple para que lo lea un humano, simple para que lo interprete una máquina.** Esa doble condición es todo su mérito.

**Soporta seis tipos:** strings, números, booleanos, arrays, objetos y `null`.

```json
{
  "id": 42,                        ← número
  "nombre": "María García",        ← string (entre comillas)
  "activo": true,                  ← booleano (SIN comillas — con comillas sería un string)
  "roles": ["admin", "usuario"],   ← array
  "direccion": {                   ← objeto ANIDADO: un objeto adentro de otro
    "ciudad": "Buenos Aires",
    "pais": "Argentina"
  }
}
```

⚠️ **Dos trampas que salieron en clase:**

1. **`true` sin comillas es un booleano. `"true"` con comillas es un string.** No es lo mismo, y en un lenguaje tipado como Java te va a romper.
2. **JSON no tiene `undefined`.** Eso es de JavaScript. **JSON acepta `null`**, y nada más.

**Vos ya sabés trabajar con JSON.** Lo nuevo empieza en la Parte 4: en Java, un lenguaje **fuertemente tipado**, no podés simplemente hacer `data.nombre` — tenés que **declarar de antemano la forma que va a tener el JSON**.

---

## 5. Rutas REST: el diseño 🔴

Este es el corazón práctico de REST. Tomemos el recurso `User`:

| Ruta | Qué hace | Código de éxito |
|---|---|---|
| `GET /users` | Obtiene **todos** los usuarios | `200 OK` |
| `GET /users/1` | Obtiene **el** usuario con id 1 | `200 OK` |
| `POST /users` | **Crea** un usuario nuevo *(los datos van en el body)* | **`201 Created`** |
| `PUT /users/1` | **Actualiza** el usuario 1 *(los datos van en el body)* | `200 OK` |
| `DELETE /users/1` | **Borra** el usuario 1 | **`204 No Content`** |

### 🔴 La observación que hay que entender, no memorizar

Mirá `GET /users/1` y `PUT /users/1`.

**La ruta es exactamente la misma.** Lo único que cambia es **el verbo HTTP**.

> **Eso es la gracia entera de REST.** La ruta identifica **el recurso**; el verbo dice **qué le hacés**. Un recurso, una ruta, y cinco operaciones sobre ella.

**Por eso está mal esto:**

```
   ❌ GET  /users/obtenerUsuarioPorId/1
   ❌ POST /users/actualizarUsuario
   ❌ GET  /users/borrar/1
```

Todas esas rutas **meten el verbo adentro del nombre**. Y no hace falta: **el verbo ya está en HTTP.**

---

## 6. Buenas prácticas REST 🔴

Tres reglas. Van directo al parcial y al TPA.

**① Las rutas siempre llevan nombres de RECURSOS.**
Sustantivos. `/users`, `/donaciones`, `/facturas`.

**② No se usan verbos como nombres de rutas.**
Un verbo indica una **acción** — y para eso ya están los verbos HTTP. `/actualizarUsuario` está mal; `PUT /users/1` está bien.

**③ Se usan los códigos de estado HTTP para indicar cómo terminó la operación.**
No devuelvas `200 OK` con un `{"error": "no encontrado"}` adentro. **Devolvé un `404`.** Para eso están.

> **Por qué importa, más allá de la nota:** en el TPA vas a tener **varios servicios hablándose entre sí**, escritos por distintos integrantes del equipo. Si todos siguen estas reglas, **cada uno sabe qué esperar del otro sin preguntar**. Sin el estándar, cada integración es una negociación.

---

## 7. REST "a rajatabla": el trade-off 🔴

Esta discusión ocupó varios minutos de clase y **es la más importante de la Parte 3**, porque muestra que REST no es gratis: **tiene un costo, y hay que saber cuál es.**

### El problema

Supongamos que pedís un alumno:

```
   GET /alumnos/12345
```

Y te devuelve:

```json
{
  "nombre": "Ever",
  "apellido": "Lizarraga",
  "fechaNacimiento": "1991-09-25",
  "cursadas": [101, 205, 307, 412]      ← 👈 SOLO LOS IDs. No los datos.
}
```

**¿Y si querés ver el detalle de cada cursada?** Tenés que ir a buscarlas una por una:

```
   GET /cursadas/101      ← una request
   GET /cursadas/205      ← otra
   GET /cursadas/307      ← otra
   GET /cursadas/412      ← otra
```

**Cuatro requests más.** Y si el alumno cursó 40 materias... **cuarenta requests.**

### 🔴 Por qué REST te obliga a esto

> *"Si respetás REST a rajatabla, **no podés mezclar entidades**. No se pueden mezclar recursos."*

**La regla:** cada recurso vive en su propia ruta y devuelve **su propia información**. Un `alumno` no devuelve `cursadas` completas adentro — devuelve sus IDs, y las cursadas **son otro recurso**, con su propia URI.

**El costo tiene nombre en la industria: el problema N+1.** Una request para la lista, N requests para el detalle de cada elemento.

### El matiz honesto de la cátedra

*"Tampoco hace falta ser tan puristas. Capaz que por simplicidad decís: acá devuelvo otros objetos adentro para evitarme una llamada más."*

Y el propio profe se rió del absurdo: *"si el alumno cursó 40 materias, ¿hacemos 40 requests? ¿Qué vas a hacer, no mostrar el nombre de la materia?"*

**En la industria se rompe la regla todo el tiempo, y con buen criterio.** Por eso existe la distinción entre **REST** y **RESTful**: *cuán* REST es lo que implementaste.

### 🔴🔴 PERO — la regla del TPA

> **"El TP lo respetamos a rajatabla. **No mezclar entidades.**"**

**Eso es una instrucción directa, no una sugerencia.** En el TPA, cada recurso devuelve lo suyo. Nada de anidar entidades para ahorrarte llamadas.

### Y de yapa: el pie para más adelante

*"Hay otro tipo de API que hace **agregación de recursos**: devuelve uno solo, con campos dinámicos."*

O sea: **existe una tecnología que resuelve exactamente este problema.** Le pedís todo lo que necesitás en **una sola llamada**, y el servidor te lo arma.

> 🕳️ **Madriguera — Lo que resuelve el N+1**
> Se llama **GraphQL**, y la cátedra lo anticipó a propósito: agrega recursos, permite pedir campos dinámicos, y elimina el ida y vuelta. Es una alternativa a REST, no un reemplazo.
> *Volvé al camino — cae en la clase 30. Y el profe deslizó que **puede aparecer en el TPA**.*

> 📌 **Esto es exactamente el tipo de pregunta que la materia evalúa:** *"¿Qué desventaja tiene REST y cómo la resolverías?"*. No es memoria — es el razonamiento de trade-offs de la Parte 1, aplicado a un estándar concreto.

---

## 8. Tipos de API 🟡

Dos ejes independientes, que se cruzan:

| | **Abierta** | **Cerrada** |
|---|---|---|
| **Gratuita** | ✅ La usás sin registrarte y sin pagar | Te registrás, pero no pagás |
| **Paga** | *(raro)* | Te registrás **y** pagás |

- **Abierta** = **no necesitás credencial** ni registrarte. Entrás y consumís.
- **Cerrada** = **necesitás registrarte** y obtener una credencial (una *API key* o un token) para poder llamarla.
- **Gratuita / Paga** = si pagás o no por su uso.

⚠️ **No confundas los ejes.** Una API puede ser **cerrada y gratuita**: te registrás, te dan una clave, y no pagás nada. Es el caso más común de las APIs públicas serias (te registran para poder limitarte el uso).

En la práctica de la clase se usaron **dos APIs abiertas y gratuitas**, justamente para no tener que meterse con credenciales.

---

## 9. Documentación: Swagger y OpenAPI 🔴

**Toda API viene con documentación** — porque **sin ella no podés consumirla.** Necesitás saber qué rutas existen, qué parámetros aceptan, qué devuelven y con qué códigos.

**El problema:** cada sitio documenta como quiere. Una API te lo explica en prosa, otra en tablas, otra con ejemplos sueltos. **No hay dos iguales**, y eso te obliga a aprender a leer cada una.

### La solución: un estándar de documentación

> **OpenAPI** es el estándar (la versión actual es la **3.0**). **Swagger** es la herramienta que lo renderiza: te muestra la documentación de forma ordenada y navegable.

**Qué te da una API documentada en Swagger:**

```
   GET  /all              → "obtiene todos los países"
   GET  /name/{name}      → "busca un país por nombre"
        ├── parámetro:  name (string, en la ruta)
        ├── ejemplo:    /name/Argentina
        └── respuestas:
              200 → el país encontrado
              404 → no existe
```

**Rutas, parámetros, ejemplos, y todos los códigos de estado posibles.** Ordenado, estándar, y siempre igual, sea la API que sea.

### 🔴🔴 Por qué esto es 🔴 y no 🟡

> **"En el trabajo práctico van a tener que consumir una API, y se les va a dar la documentación en Swagger."**
>
> **"Y cuando expongamos nuestras propias APIs, las vamos a documentar en Swagger."**

**No es cultura general. Es una herramienta del TPA, en las dos direcciones:** vas a *leer* Swagger para consumir la API de la cátedra, y vas a *escribir* Swagger para documentar la tuya.

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

## ✅ Checkpoint — Parte 3

1. ¿Qué es una API? Dame dos ejemplos que **no** sean servicios remotos.
2. ¿Cuál es la relación entre "API" y "API REST"?
3. Según la cátedra, ¿qué **no** es REST? (dos cosas) ¿Y qué **es**?
4. Enumerá los cinco criterios de REST.
5. Si decís "te expongo una API REST", ¿qué cinco cosas quedaron acordadas sin decir una palabra más?
6. ¿Por qué REST exige que cada request sea autocontenida? Conectalo con una característica de HTTP.
7. `GET /users/1` y `PUT /users/1` tienen la misma ruta. ¿Por qué está bien? ¿Qué las diferencia?
8. ¿Por qué `POST /users/crearUsuario` está mal? Nombrá la regla que viola.
9. ¿Qué códigos de estado devuelven, respectivamente, un POST y un DELETE exitosos? ¿Por qué son distintos?
10. Explicá el problema N+1 con el ejemplo del alumno y sus cursadas. ¿Cuál es la regla de REST que lo causa?
11. ¿Qué dijo la cátedra sobre respetar REST a rajatabla **en el TPA**?
12. Diferencia entre una API abierta y una gratuita. ¿Puede ser cerrada y gratuita a la vez?
13. ¿Qué es OpenAPI y qué es Swagger? ¿Para qué los vas a necesitar en el TPA — en cuántas direcciones?

---

## 🎯 Qué viene en la Parte 4

**Se terminó la teoría.** En la Parte 4 se consume una API REST de verdad, de tres formas distintas:

1. Desde el **navegador** (y descubrís algo que hacés todos los días sin saberlo).
2. Desde **Postman** (query params, path params, headers, body).
3. Desde un **proyecto Java con Spring Boot** — el código completo, comentado línea por línea.

Y cierra con la **información operativa del TPA**: qué pide la Entrega 1, en qué orden conviene hacerla, las herramientas de la cursada y los canales de consulta.

---

**FIN DE LA PARTE 3**
