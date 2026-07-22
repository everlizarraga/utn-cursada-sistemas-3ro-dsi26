# 🗺️ RECORRIDO — Orquestación de CU en Capas — ROADMAP

**Unidad:** clase04 · **Materia:** Diseño de Sistemas de Información (DSI)
**Tipo:** Material pedagógico extra — puente hacia el material oficial de la unidad
**Fecha:** Julio 2026

---

## Sobre este documento

Este roadmap es el **mapa del recorrido**: te muestra los 7 puntos que lo componen, qué cubre cada uno, en qué orden se leen y qué preguntas se van abriendo y cerrando a lo largo del camino.

**Qué es el recorrido:** una serie de archivos cortos y autocontenidos que te llevan, con curva suave, desde lo que ya viste en la materia hasta el contenido completo de esta unidad. Al terminarlo, el apunte maestro oficial de la clase 04 se va a sentir como un repaso — ese es el objetivo.

**Qué NO es:** no reemplaza al apunte maestro (ese se genera después, con cobertura plena, y es el material del que derivan el resumen y el machete). El recorrido es el puente para *entender*; el apunte es el artefacto para *estudiar*.

---

## De dónde venís (lo que el recorrido asume)

El recorrido se para **solo** sobre unidades anteriores de esta materia. Cada punto declara lo suyo, pero el andamiaje global es:

| Unidad | Qué aporta al recorrido |
|---|---|
| **clase01** | Que un sistema tiene componentes: un frontend, un backend, una base de datos |
| **preclase02** | Java, Maven, JUnit, excepciones (checked / unchecked), colecciones y streams |
| **clase02** | Cliente-servidor, HTTP (verbos, status codes, request/response), API REST, JSON |
| **preclase03** | Acoplamiento, cohesión, atributos de calidad y sus trade-offs |
| **clase03** | El dominio del servicio de ventas de SmartLife: Comercio, Producto, TipoProducto, impuestos, observadores de venta |
| **preclase04** | Inyección de dependencias, biblioteca vs framework (inversión de control), mockeo |

Nada más se asume. Todo término nuevo que aparezca trae su línea de explicación.

---

## Leyenda

| Marca | Significado |
|---|---|
| 🔴 | Contenido **central y evaluable** — el corazón de la unidad |
| 🟡 | Contenido **secundario** — importa, pero sostiene a lo central |
| 🟢 | Mencionado al pasar — saber que existe alcanza |
| 🕳️ | **Madriguera** — tangente desactivada: 1-3 líneas para saber que existe, y seguís. Se saltea sin culpa |
| 🎯 | **Pattern en contexto** — qué es, por qué se usa, dónde lo ves en ESTE código |
| ✅ Checkpoint | 5-10 preguntas al cierre de cada punto, **sin respuestas** — estilo "decidí y justificá", como evalúa la materia. Las respondés vos; las respuestas correctas decantan después al complemento |

---

## El problema que ordena todo el recorrido

En la clase 03 quedó modelado el **dominio** del servicio de ventas de SmartLife: comercios, productos, tipos, impuestos, observadores. Funciona, tiene tests, calcula precios.

Pero el enunciado pide más: *"los comercios deben poder dar de alta productos"*, *"registrar ventas"*, *"consultar precios"*. Esos son **casos de uso** — y un caso de uso no es un método de una clase. Entonces:

> **¿Dónde vive cada pedazo de un caso de uso cuando el sistema es real?** ¿Quién recibe el pedido? ¿Quién valida? ¿Quién instancia? ¿Quién guarda? ¿Quién decide si el que pide *puede* pedir eso? ¿Y quién le responde?

Las 7 paradas de este recorrido contestan esa pregunta, una capa a la vez, hasta dejarte parado dentro del proyecto completo del servicio de ventas con la API funcionando.

---

## Los 7 puntos

### Punto 1 — Modelo en capas 🟡
`recorrido-orquestacion-capas-punto1-modelo-en-capas.md`

**Sobre qué se para:** clase01 (componentes), preclase03 (acoplamiento y cohesión).

Por qué dividir un problema grande en capas y qué es exactamente una capa. Las tres capas mínimas de un sistema de información — **Presentación, Dominio/Negocio, Datos** — y la regla de dirección: las de arriba usan a las de abajo, nunca al revés, nunca salteando. Qué se gana y qué se paga con este diseño (con la conexión directa a los atributos de calidad que ya conocés). La arquitectura web típica dibujada completa, y la diferencia entre cliente liviano y cliente pesado. Dos diagramas ASCII para fijar las dos vistas del mismo modelo.

🕳️ previstas: MVC (es otra cosa — un patrón de interacción, más adelante en la materia).

---

### Punto 2 — La orquestación: el problema 🔴
`recorrido-orquestacion-capas-punto2-orquestacion-el-problema.md`

**Sobre qué se para:** Punto 1. (Casos de uso y actores se refrescan en una línea — los viste en la carrera; acá se presentan completos con el ejemplo.)

El caso SIU Guaraní: dos requerimientos, dos actores, dos casos de uso. Y el diseño que a casi todo el mundo se le ocurre primero: una clase `Alumno` con un método `verCalificaciones()`, una clase `Docente` con `administrarCalificaciones()`. Este punto desarma ese diseño con una sola pregunta — **¿qué haría ese método por dentro?** — y muestra a dónde lleva insistir con él (repetir lógica, herencias forzadas, un enredo estructural del que es difícil salir). El diagnóstico de fondo: se están mezclando dos cosas que no van juntas (los permisos de un rol y la lógica del negocio) y responsabilidades de capas distintas. La conclusión que ordena toda la unidad: **un caso de uso se desglosa en varias clases, repartidas entre capas, cada una con una responsabilidad distinta.**

**Hilo que se abre (H1):** ok, se desglosa… ¿pero dónde va cada cosa?

---

### Punto 3 — Usuarios, roles y permisos 🔴
`recorrido-orquestacion-capas-punto3-usuarios-roles-permisos.md`

**Sobre qué se para:** Punto 2, clase03 (composición, extensibilidad en tiempo de ejecución).

La primera mitad de la respuesta: separar **quién puede** de **qué hace el sistema**. Qué entidades del enunciado merecen ser clases y cuáles no (Docente y Alumno sí; Administrador y Bedel no — y el criterio para decidirlo vale para cualquier sistema). La entidad **Usuario** como datos de acceso, **Rol** y **Permiso** como su estructura — con el diagrama de clases y un diagrama de instancias en ASCII. Por qué modelar esto con herencia rompe (una misma persona con dos roles al mismo tiempo) y cómo la composición lo resuelve. Las variantes de diseño y qué se paga con cada una (permisos como enumerados, un rol vs colección de roles) — material directo de parcial, porque acá se evalúa justificar la elección. Y la conexión con HTTP que ya conocés: qué status code corresponde cuando el que pide no puede (403) y cuando ni se sabe quién es (401).

**Hilo que se abre (H2):** tengo Usuario, Rol y Permiso modelados… ¿pero en qué lugar del sistema se hace el chequeo?

---

### Punto 4 — Las clases típicas de cada capa 🔴 *(el corazón del recorrido)*
`recorrido-orquestacion-capas-punto4-clases-tipicas-por-capa.md`

**Sobre qué se para:** Puntos 1-3, clase03 (el dominio de SmartLife).

La respuesta completa a "¿dónde va cada cosa?". Los cuatro tipos de clase que vas a encontrar en (casi) todo sistema:

- **Entidades de dominio** — las clases que ya sabés modelar; con o sin comportamiento, y por qué ambas son válidas.
- **Repositorios** — los objetos que hablan con la persistencia: su contrato (guardar, buscar, eliminar…), y las dos preguntas finas: ¿toda entidad lleva repositorio? (no — utilitarias no se persisten) y ¿puede una entidad persistirse *a través de otra*? (sí — guardado en cascada, y su conexión con agregación/composición).
- **Services** — los orquestadores: toman un caso de uso y ejecutan su flujo paso a paso (validar → instanciar → asociar → persistir). La distinción más citable de la unidad: **focalizan las reglas del flujo, no las reglas del negocio.** Y una regla para anotar: quién instancia a las entidades de dominio.
- **Controllers** — la frontera con el mundo exterior: reciben, traducen, delegan, responden. Nunca lógica de negocio.

El punto cierra con dos piezas centrales: el **árbol de conocimiento** (quién conoce a quién y — más importante — quién NO conoce a quién) como diagrama ASCII, y el service de calificaciones del SIU escrito en código, donde el chequeo de permisos del Punto 3 por fin encuentra su lugar (**cierra H2**) y el desglose del Punto 2 se ve materializado en clases concretas (**cierra H1, mitad teórica**).

🕳️ previstas: modelo anémico vs enriquecido · "use cases" y arquitectura hexagonal · frameworks que fusionan service y controller · dónde más pueden vivir los permisos (middleware, sesiones — más adelante en la materia).

**Hilos que se abren:** (H3) los services instancian al dominio… ¿y a los services quién los instancia? · (H4) el controller recibe "solicitudes externas"… ¿cómo llega físicamente una petición HTTP hasta ahí?

---

### Punto 5 — El puente a Spring Boot 🔴
`recorrido-orquestacion-capas-punto5-spring-boot-el-puente.md`

**Sobre qué se para:** Punto 4, preclase04 (inyección de dependencias, biblioteca vs framework), preclase02 (Maven).

Del diagrama al proyecto real. Acá es donde la lectura de inyección de dependencias e inversión de control paga: el framework es quien instancia los services y los repositorios y quien les inyecta lo que necesitan por constructor (**cierra H3**) — vos declarás, él construye. Las anotaciones con las que se lo declarás (`@Service`, `@Repository`, `@RestController`), qué es un *bean* (el nombre que Spring le da a los objetos que gestiona), y la decisión de diseño que atraviesa todo el proyecto: **cada capa expone una interfaz y esconde su implementación** — por qué existe el par interfaz + clase concreta, qué atributo de calidad se está comprando y cómo se conecta con la teoría de capas del Punto 1. Cierra mostrando que la organización en packages del proyecto ES el modelo en capas hecho carpetas.

---

### Punto 6 — El servicio de ventas por dentro 🔴
`recorrido-orquestacion-capas-punto6-sales-service-por-dentro.md`

**Sobre qué se para:** Puntos 4-5, clase03 (dominio SmartLife), clase02 (HTTP, JSON, status codes).

El punto más largo: el proyecto completo del servicio de ventas recorrido capa por capa, con **todo el código extraído y comentado línea por línea**. La estructura de packages. El dominio (repaso exprés de lo que ya modelaste). Los repositorios en memoria y el generador de IDs. El `ProductoService` punta a punta — con el caso de uso "dar de alta un producto" seguido paso a paso: validar la entrada, recuperar el comercio y el tipo, instanciar, asociar, guardar, responder. Los **DTO** y los *records* de Java: qué entra al sistema, qué sale, y qué no se expone jamás (con el porqué). El `VentaService` como segunda orquestación — más rica, porque el dominio impone sus propias reglas. Los controllers con el mapeo de rutas y verbos HTTP. Las excepciones propias y el **manejador global de errores**: qué status code sale de cada tipo de falla (**cierra H5**). Los datos semilla para arrancar con algo cargado. Y el cierre del viaje completo: probar la API con Postman, petición por petición, y verificarla también desde un test de integración — sin salir del proyecto (**cierra H4**).

---

### Punto 7 — El mapa completo y el cierre 🟡
`recorrido-orquestacion-capas-punto7-mapa-completo-y-cierre.md`

**Sobre qué se para:** todo lo anterior.

La vista de helicóptero final: el viaje de una request dibujado de punta a punta (HTTP → Controller → Service → Dominio + Repositorios → DTO → respuesta) y el árbol de conocimiento consolidado con las reglas-reflejo de la unidad (las que conviene tener automatizadas para el parcial). Qué implica todo esto para el trabajo práctico: qué capa se implementa en qué momento y qué NO hay que adelantar. Qué temas quedaron sembrados para más adelante en la materia (persistencia real, interfaz gráfica, sesiones). Y la convergencia: con el recorrido terminado, el material oficial de la unidad se lee como repaso.

---

## Mapa de hilos

Preguntas que el recorrido abre a propósito (para que el dolor se sienta primero) y cierra después (cuando aparece la herramienta que lo cura):

| # | El hilo (la pregunta que duele) | Se abre en | Se cierra en |
|---|---|---|---|
| **H1** | Un CU se desglosa en varias clases… ¿dónde va cada cosa? | Punto 2 | Punto 4 (teoría) → Punto 6 (código real) |
| **H2** | ¿En qué lugar del sistema se chequea que quien ejecuta tiene permiso? | Punto 3 | Punto 4 |
| **H3** | Los services instancian al dominio… ¿y a los services quién los instancia? | Punto 4 | Punto 5 |
| **H4** | ¿Cómo entra una petición real del mundo exterior y cómo vuelve la respuesta? | Puntos 1 y 4 | Punto 6 |
| **H5** | Cuando algo falla (no existe, no se puede, no se debe), ¿qué le respondo al que llamó? | Punto 3 | Punto 6 |

Si en medio de un punto sentís que algo quedó sin responder, chequeá esta tabla: es muy probable que sea un hilo abierto a propósito.

---

## Cómo usar el recorrido

1. **En orden, del 1 al 7.** Cada punto asume los anteriores; ninguno asume nada del que sigue.
2. **Las dudas van al chat, en cualquier momento.** No te quedes trabado: preguntá. Las aclaraciones después decantan al complemento del recorrido.
3. **Los checkpoints se responden sin mirar.** Son sin respuestas a propósito: si un checkpoint no te sale, es la señal de qué releer antes de avanzar. Las respuestas modelo llegan con el complemento.
4. **Las madrigueras 🕳️ se saltean sin culpa.** Están para desactivar la tangente, no para abrirla.
5. **Al terminar:** se genera el apunte maestro oficial de la unidad (cobertura plena, como siempre). Si el recorrido hizo su trabajo, lo vas a leer asintiendo.

---

**FIN DEL ROADMAP — Recorrido Orquestación de CU en Capas**
