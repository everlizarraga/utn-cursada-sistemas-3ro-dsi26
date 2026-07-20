# Apunte Maestro — Clase 02 · Parte 1
## Cliente – Servidor

---

## 🧭 Cómo leer esto

Esta es la clase 2 de DSI (01/04/2026), dada de nuevo: completa, ordenada, sin las idas y vueltas del aula. La clase tuvo tres bloques —teoría, práctica en código y presentación del TPA— y este apunte los cubre en cuatro partes:

| Parte | Contenido |
|---|---|
| **1** (esta) | **Cliente – Servidor** |
| **2** | **Protocolo HTTP + Arquitectura Web** |
| **3** | **API y API REST** |
| **4** | **Práctica: consumir una API REST** (navegador → Postman → Java/Spring) + info operativa del TPA |

**Marcas de importancia:**

| | |
|---|---|
| 🔴 | **Central y evaluable.** El profe le dedicó tiempo, dio ejemplos, o es prerrequisito de lo que viene. |
| 🟡 | **Secundario.** Hay que saberlo, pero no es el eje. |
| 🟢 | Mencionado al pasar. |
| 📌 | **Para el parcial, si te preguntan** — respuesta modelo, formato examen. |
| ⚠️ | Advertencia: algo que se dijo de una forma y conviene matizar. |
| 🕳️ | **Madriguera.** Un tema que asoma y no seguimos. Leela y seguí de largo. |

---

## 0. Qué buscaba esta clase 🟡

La clase 2 arma, en un solo movimiento, **la columna vertebral de toda la materia**. Y lo hace en escalera — cada tema es el piso del siguiente:

```
   Cliente – Servidor     →  el PATRÓN. Quién pide, quién responde.
          │
          ▼
   Protocolo HTTP         →  el IDIOMA con el que se hablan.
          │
          ▼
   Arquitectura Web       →  el patrón + el idioma, andando en internet.
          │
          ▼
   API                    →  cómo un componente expone lo que sabe hacer.
          │
          ▼
   API REST               →  el ESTÁNDAR concreto para hacer eso sobre HTTP.
                              ← acá se juntan las cuatro capas anteriores.
```

Los propios docentes lo dijeron al cerrar la teoría: *"es como el cierre, donde juntamos todo lo que estuvimos charlando"*.

**Y no es teoría suelta.** El TPA entero se construye sobre esto: vas a **consumir** APIs REST de terceros y a **exponer** las tuyas. Todo lo que sigue lo vas a escribir con las manos.

**Recordá de la clase 1:** un **componente de software** es una unidad que hace algo y que se comunica con las demás a través de una **interfaz** — un contrato público que dice qué le podés pedir, sin que tengas que saber cómo lo resuelve por dentro. Esos dos conceptos son el andamio de toda esta clase: cliente y servidor son componentes, y la API es una interfaz.

---

## 1. Qué es Cliente – Servidor 🔴

Arranquemos por lo que **no** es, porque en clase esa fue la primera corrección.

❌ **No es un protocolo.** Un protocolo es un idioma concreto (HTTP, SMTP, IMAP). Cliente-servidor no es un idioma: es un **esquema de conversación**, y adentro pueden usarse muchos idiomas distintos.

✅ **Es un patrón arquitectónico.** Y es un patrón, específicamente, porque hace tres cosas:

- **Define componentes** (hay un cliente y hay un servidor).
- **Define una forma de conexión** (uno pide, el otro responde).
- **Define un vocabulario.** Y ese es el punto que más rinde: cuando decís *"esto lo hacemos cliente-servidor"*, tu interlocutor **ya sabe** de qué estás hablando sin que expliques nada más. Un patrón, antes que nada, es un nombre compartido.

### La definición formal

Participan **dos componentes**:

- Un **servidor**, que provee uno o más servicios a través de una **interfaz**.
- Un **cliente**, que **usa** esos servicios como parte de su operación.

```
                          Petición
        ┌──────────┐  ───────────────────►  ┌──────────┐
        │  CLIENTE │         HTTP           │ SERVIDOR │
        │          │  ◄───────────────────  │          │
        └──────────┘        Respuesta       └──────────┘
```

**Fijate que el diagrama dice HTTP, pero podría decir otra cosa.** HTTP es el protocolo que vamos a usar nosotros, y el más común — pero un cliente de mail habla **SMTP** o **IMAP** con su servidor, y sigue siendo cliente-servidor. El patrón es independiente del idioma. Lo único innegociable es que **ambos hablen el mismo**.

---

## 2. Las cuatro reglas del patrón 🔴

Estas cuatro definen el comportamiento, y **la primera es la que más se pregunta**:

**① El cliente SIEMPRE inicia la comunicación. Nunca el servidor.**
El servidor es reactivo por naturaleza: no toca la puerta, la abre.

**② El servidor escucha en un puerto determinado, esperando solicitudes.**
Está ahí, quieto, con la oreja puesta en un número de puerto. (Los puertos los vemos en la Parte 2.)

**③ Cada interacción sigue el ciclo: Solicitud → Procesamiento → Respuesta.**
Cerrado. Empieza y termina. El cliente pide, el servidor piensa, el servidor contesta.

**④ El servidor puede atender múltiples clientes de forma simultánea.**
Concurrencia. Y esto va a tener consecuencias serias — lo vemos en la sección 5.

### ⚠️ El matiz de la regla ①: "¿y las notificaciones?"

En clase alguien preguntó lo obvio: *"si me llega una notificación de que hay una promo nueva, ¿no me la está mandando el servidor sin que yo pida nada?"*. Buena pregunta, y la respuesta importa:

> **En HTTP/1.1 —que es la versión que estudia la materia— el servidor NO puede iniciar la comunicación.** Punto.
>
> Las notificaciones push, el streaming bidireccional y todo lo que "viene solo" existen, pero funcionan **sobre otras versiones del protocolo** (HTTP/2, HTTP/3) o sobre otros protocolos (WebSockets). Son mecanismos **posteriores** al modelo clásico.
>
> **Para el parcial: en el modelo cliente-servidor clásico sobre HTTP/1.1, el cliente siempre inicia.** Si te preguntan por notificaciones, la respuesta correcta es que requieren salir de HTTP/1.1.

---

## 3. Ejemplos: el patrón está en todos lados 🟢

| Cliente | Servidor | Protocolo |
|---|---|---|
| Navegador web (Chrome) | Servidor web (Apache, Nginx) | HTTP |
| App de home banking | Servidor del banco | HTTP |
| Cliente de email (Outlook) | Servidor de correo | **SMTP / IMAP** ← no HTTP |
| App de delivery en el celular | Backend del sistema | HTTP |
| Videojuego online | Servidor del juego | (propio del juego) |

**Dos cosas que sacar de esta tabla:**

1. **El cliente no es "el navegador".** Es *cualquier* componente que inicia y consume. Una app mobile es un cliente. Un backend que le pega a otro backend **también es un cliente** (de ese otro).
2. **El protocolo cambia.** La fila de Outlook está para eso: cliente-servidor sobre SMTP/IMAP es tan cliente-servidor como cliente-servidor sobre HTTP.

> **Regla práctica para identificarlo en un diagrama:** si un componente **expone** algo, está haciendo de servidor. Si otro **consume** eso, está haciendo de cliente. Y ojo: **son roles, no identidades.** Un mismo componente puede ser servidor de uno y cliente de otro al mismo tiempo. Tu backend es servidor para el frontend, y cliente de la base de datos.

---

## 4. Clasificación según responsabilidades 🟡

Acá el eje ya no es *quién pide*, sino **dónde vive la lógica de negocio**. Cruzando cliente y servidor, salen cuatro combinaciones:

| Tipo | Dónde está la lógica | Nombre propio | Ejemplo |
|---|---|---|---|
| **Cliente activo, Servidor pasivo** | Casi toda en el cliente. El servidor solo **persiste** (guarda datos). | — | Un ERP de escritorio viejo: la app corre entera en la PC y el servidor es solo la base de datos compartida. |
| **Cliente pasivo, Servidor pasivo** | Poca en ambos. Son **componentes intermedios** de algo más grande. | — | Un componente de integración que trae datos de otro sistema y se los deja al principal. |
| **Cliente pasivo, Servidor activo** | Casi toda en el servidor. El cliente solo **presenta datos**. | **Cliente liviano** | Una web tradicional: el servidor arma el HTML, el navegador solo lo muestra. |
| **Cliente activo, Servidor activo** | **Distribuida** entre los dos. El cliente tiene lógica de presentación **e interacción**. | **Cliente pesado** | Una SPA hecha con React o Angular. |

### ⚠️ Lo que el profe dijo textualmente sobre esto

> *"Este sistema no es una pregunta de parcial. La idea es que entendamos que hay distintos tipos... pero lo importante acá es entender **dónde pasan las cosas**: qué componente tiene la responsabilidad de qué, y **qué cambia** que ese componente tenga una responsabilidad distinta."*

**Traducido: no memorices la tabla. Entendé la consecuencia.** Nadie te va a pedir "definí cliente pasivo-servidor pasivo". Te van a dar un caso y te van a pedir que **decidas dónde poner la lógica y lo justifiques**. La tabla es vocabulario para esa justificación, no el contenido evaluable.

### 🔴 Lo que sí importa: la consecuencia de mover la lógica

Si ponés más lógica del lado del cliente:

- ↑ **procesamiento** en el dispositivo del usuario
- ↑ **consumo de batería** (si es un celular)
- ↑ **uso de memoria** del dispositivo
- ↓ carga en el servidor

Y al revés. **Esa es la cadena de razonamiento que la materia evalúa.** Una decisión arquitectónica no es "elijo esto porque me gusta": es "elijo esto **porque** el impacto es tal, y estoy dispuesto a pagarlo".

⚠️ **Y ojo con esto, que apareció en clase y es un error clásico:** el tipo de cliente **no lo define la aplicación, lo definen las decisiones de diseño y las tecnologías con las que se resolvió.** Preguntaron si el home banking es cliente liviano y la respuesta fue: *depende del banco*. El mismo producto, resuelto por dos equipos distintos, puede ser liviano en uno y pesado en el otro. Santander cambió de estrategia hace unos años; otros bancos no.

> **Señal fuerte:** si el proyecto usa **React o Angular**, estás ante un **cliente pesado**. Esa es la traducción directa entre tecnología y clasificación.

> 🕳️ **Madriguera — Cliente liviano y cliente pesado, en serio**
> Los dos últimos tipos son un tema en sí mismo: cómo se genera la vista, dónde se renderiza, qué es un *template engine*, qué es una SPA. En clase se frenó a propósito (*"no quiero spoilear la clase de cliente liviano"*).
> *Volvé al camino — esto cae en las clases 23 y 25 (Cliente Liviano, Thymeleaf, SPA).*

---

## 5. Ventajas y desventajas del cliente liviano 🔴

La cátedra analiza en detalle **una** de las cuatro combinaciones: **cliente pasivo – servidor activo (cliente liviano)**, que es la clásica de la web.

Y no las analiza como una lista de pros y contras: las analiza como **atributos de calidad**. Cada ventaja y cada desventaja **tiene un nombre técnico**, y ese nombre es el que hay que usar.

### ✅ Ventajas

**Mantenibilidad** — *cambios de funcionalidad centralizados.*
Toda la lógica está en un solo lugar. Actualizás el servidor, el usuario aprieta F5, y ya tiene la versión nueva. **Compará con lo contrario:** si la lógica viviera en una app instalada en cada cliente, tendrías que distribuir una actualización a *cada máquina* (como hace un sistema operativo, y con el mismo dolor).

**Seguridad** — *centralización del control de acceso a los recursos.*
Un solo componente que proteger. Si la lógica estuviera distribuida, tendrías **más superficie de ataque**: más lugares donde puede aparecer una brecha.

### ❌ Desventajas *(considerando un único servidor)*

⚠️ **Esa aclaración entre paréntesis no es un detalle: es la puerta al resto de la materia.** Las dos desventajas existen **porque hay un solo servidor**. Con más servidores desaparecen... y aparecen otros problemas.

**Eficiencia (tiempo de respuesta)** — *el servidor puede ser un cuello de botella.*
Cuantas más solicitudes concurrentes recibe, más tarda en responder cada una. La degradación es gradual: primero los tiempos suben de a poco, y en algún punto la aplicación **deja de responder**. ¿Cuántas soporta? Depende de su enlace de red, su hardware, la tecnología en la que está hecho, y qué tan pesada es cada operación.

**Disponibilidad** — *único punto de falla.*
Si ese servidor se cae, **no hay sistema.** Nada. Cero.

---

## 6. El razonamiento que la materia evalúa 🔴

Esta sección es **el corazón de la clase**, aunque no tenga una slide propia. Es lo que Saclier construyó a preguntas, y lo que la asignatura te va a exigir en cada parcial y en cada entrega del TPA.

### Los atributos de calidad se pisan entre sí

Mirá qué pasa cuando querés arreglar las desventajas de arriba. La solución obvia es **agregar servidores**:

```
   Agrego un segundo servidor y balanceo la carga entre los dos
              │
              ├──►  ✅ gano EFICIENCIA      (la carga se reparte)
              ├──►  ✅ gano DISPONIBILIDAD  (si uno cae, el otro responde)
              │
              ├──►  ❌ pierdo MANTENIBILIDAD (más nodos = más difícil de mantener)
              └──►  ❌ sube el COSTO         (más servidores = más plata)
```

> 🔴 **Esta es LA idea de la clase, y probablemente la de la materia entera:**
>
> **No existe una arquitectura "mejor". Existe una arquitectura que privilegia unos atributos de calidad a costa de otros.** Mejorar uno casi siempre empeora otro. El trabajo del diseñador no es optimizar todo —**es imposible**— sino **elegir qué sacrificar, y poder justificar por qué**.
>
> Y el **costo** siempre está en la ecuación, aunque no figure en ninguna slide. Toda decisión arquitectónica que suma nodos suma plata. Y esa, al final, **es una decisión de negocio**, no técnica: *"me sale esto, ¿lo vale?"*. La respuesta puede ser sí (*"si el sistema se cae, no vendemos"*) o no.

### El único punto de falla es tu eslabón más débil

La analogía que usó el profe es la que mejor se pega:

> *Tenés una casa. Le ponés rejas a todas las ventanas... menos a una. ¿Cuánta seguridad tenés? La de la ventana sin reja.*

Con la disponibilidad pasa exactamente lo mismo, y hay una trampa clásica:

```
   ❌ Duplico el servidor de aplicación "para tener alta disponibilidad"...
      ...pero dejo UNA sola base de datos.

      [App 1] ──┐
                ├──► [ BASE DE DATOS ]   ← 💀 el punto de falla no desapareció:
      [App 2] ──┘                            SE MUDÓ.
```

**No eliminaste el punto único de falla. Lo moviste.** Si el componente sin redundancia se cae, el sistema se cae — no importa cuántas copias tengas de los otros.

> **La regla:** *cualquier componente sin redundancia es, por sí solo, un punto único de falla.* La disponibilidad de tu sistema es la del componente más frágil de la cadena.

### 🔴 Qué te van a pedir, exactamente

El profe lo dijo con todas las letras y **conviene tomárselo al pie de la letra**:

> *"La materia es de aplicación. La idea es que ustedes entiendan lo que van a ir aplicando, tanto en el TP como en un parcial: **resolveme cómo lo vas a hacer, y justificame por qué lo hacés así**."*

**Entonces la forma de una respuesta correcta en esta materia es siempre la misma:**

```
   "Pongo [la lógica / el componente / la solución] ACÁ,

    porque priorizo [atributo de calidad],

    y estoy aceptando pagar con [otro atributo de calidad]."
```

No es "definí X". Es **decidí y justificá**. Guardate ese molde.

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

## ✅ Checkpoint — Parte 1

Sin mirar arriba.

1. ¿Por qué cliente-servidor **no** es un protocolo? ¿Qué es, entonces, y qué tres cosas define?
2. Enumerá las cuatro reglas del patrón. ¿Cuál de ellas es la que más se pregunta y por qué tiene un matiz?
3. Un componente de tu backend le pega a la API de otro sistema. En esa relación, ¿qué rol cumple?
4. ¿Qué diferencia hay entre un cliente liviano y un cliente pesado? ¿Qué decide cuál es cuál — la aplicación o algo más?
5. Nombrá las dos ventajas y las dos desventajas del cliente liviano, **con el nombre técnico del atributo de calidad** de cada una.
6. ¿Por qué las desventajas llevan la aclaración "considerando un único servidor"?
7. Agregás servidores para mejorar la disponibilidad. ¿Qué **dos** cosas empeoran?
8. Tenés dos servidores de aplicación y una sola base de datos. ¿Eliminaste el punto único de falla? Justificá.
9. Reconstruí de memoria el molde de una respuesta correcta en esta materia (la fórmula de tres partes).

---

## 🎯 Qué viene en la Parte 2

Ya tenés el patrón. Falta **el idioma**: el protocolo HTTP.

Qué significa que sea **sincrónico** y **sin estado** (y por qué esa segunda palabra explica media materia), la anatomía exacta de una request y de una response, los códigos de estado y de quién es la culpa en cada uno, HTTP vs HTTPS, y qué pasa —paso a paso— entre que escribís una URL y ves la página.

---

**FIN DE LA PARTE 1**
