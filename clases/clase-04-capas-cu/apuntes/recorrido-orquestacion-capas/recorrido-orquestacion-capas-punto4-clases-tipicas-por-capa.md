# 🧭 RECORRIDO — Orquestación de CU en Capas — PUNTO 4: Las Clases Típicas de Cada Capa

**Unidad:** clase04 · **Densidad global:** 🔴🔴 (el corazón de la unidad — leelo con tiempo)

---

## Sobre este documento

**Qué cubre:** los cuatro tipos de clase que pueblan las capas de un sistema — entidades de dominio, repositorios, services y controllers — con la responsabilidad, las reglas y los límites de cada uno; el árbol de conocimiento (quién conoce a quién y quién NO); el flujo completo de un caso de uso orquestado, con el código donde el chequeo de permisos del Punto 3 encuentra su lugar; y la ubicación gráfica final de todas las piezas.

**Qué NO cubre:** quién instancia a los services y repositorios (queda abierto a propósito — Punto 5) · el proyecto real completo con su API funcionando (Punto 6).

## De dónde venís

- **Puntos 1-3:** las tres capas y su regla de dirección; el antipatrón y la conclusión (un CU se desglosa); la entidad `Calificacion` y el modelo Usuario–Rol–Permiso, con sus dos preguntas pendientes.
- **clase03:** el dominio del servicio de ventas (Comercio, Producto, TipoProducto, impuestos, observadores) y sus tests.
- **preclase02:** excepciones en Java (`throw`, jerarquía de excepciones) y tests con JUnit.

---

## 1. Las preguntas que ordenan el punto 🔴

Quedaron tres preguntas sin dueño, y las tres son variantes de la misma:

- ¿**Dónde** podríamos **instanciar** nuestros objetos de dominio?
- ¿Dónde los podríamos **configurar** (setear sus atributos, armarlos)?
- ¿Dónde podríamos **verificar** que quien intenta la acción **tiene los permisos** para hacerla?

La respuesta llega presentando las **clases típicas** que vas a encontrar en cada capa. "Típicas" es la palabra justa: son las que aparecen en la mayoría de los sistemas, pero no un mandato — sobre el final del punto vas a ver que algunos entornos las acomodan distinto.

---

## 2. Capa de Dominio: las entidades 🔴

Empecemos por terreno conocido. En la capa de dominio viven las **entidades de dominio**: en el SIU serían `Calificacion`, `Alumno`, `Docente`, `Materia`; en el servicio de ventas son **todas las clases que ya modelaste** — `Comercio`, `Producto`, `TipoProducto`, los impuestos, los observadores. Todas. Hasta acá, no existe ninguna clase de las que conocés que no sea capa de dominio.

Dos precisiones que amplían lo que ya sabías:

**Con comportamiento o sin él — ambas valen.** Las entidades *pueden* tener comportamiento (el `Producto` calcula su precio final, el `Comercio` valida que la venta sea de productos propios) o pueden no tenerlo. Hay clases que necesitás modelar aunque no les encuentres ningún método: simplemente necesitás tener esos datos representados. "No le encontré comportamiento, entonces no es clase" es un razonamiento equivocado — son **entidades sin comportamiento**, y son legítimas.

**Modelo enriquecido vs modelo anémico.** En esta materia se trabaja con **modelo enriquecido**: la capa de dominio es fuerte en comportamiento — cada regla vive en el objeto al que le corresponde, como venís haciendo desde siempre. Existe otro enfoque, el **modelo anémico**: clases que son puros "moldes" con atributos (reflejando la estructura de una base de datos) y cero comportamiento, delegado todo a otras clases. Te lo vas a cruzar en proyectos reales.

🕳️ **Madriguera — El debate anémico vs enriquecido.** Ambos enfoques tienen defensores, ventajas y desventajas; es una discusión larga de la industria. Para esta materia: enriquecido, siempre. *Volvé al camino.*

**¿Y físicamente, qué es "una capa" en el código?** En Java, la forma de agrupar clases es el **package** (el paquete: la carpeta lógica que ya usás con `import`). Las capas se materializan como packages: todas las entidades juntas en el suyo, y cada tipo de clase que veamos ahora en el propio. Cuando llegues al proyecto real vas a ver que la estructura de carpetas ES el modelo en capas.

---

## 3. Capa de Datos: los repositorios 🔴

Primera clase nueva del día. En la capa de datos viven los **repositorios**: los objetos encargados de manipular la persistencia. Cada repositorio es una clase, y su contrato es siempre una variación de lo mismo:

```
┌───────────────────────────────┐
│   «I» AlumnosRepository       │   ← interfaz (ya vemos por qué)
├───────────────────────────────┤
│ agregar(alumno)               │
│ modificar(alumno)             │
│ eliminar(alumno)              │
│ buscar(legajo): Alumno        │
│ buscarTodos(): Alumno[]       │
│ ...()                         │   ← puede haber más, según necesidad
└──────────────┬────────────────┘
               ┆ conoce
               ▼
        ┌─────────────┐
        │ «C» Alumno  │   ← la entidad de dominio
        └─────────────┘
```

Guardar, modificar, eliminar, buscar uno, buscar todos. ¿Y qué hace "guardar" por dentro? **Depende del medio persistente.** Si el repositorio persiste en una base de datos relacional, `guardar(alumno)` se traduce en una sentencia de *insert*; `eliminar(alumno)`, en un *delete*. Si persiste en un archivo, escribe el archivo. El repositorio es el traductor entre tus objetos y el medio donde se guardan — sea cual sea.

**La regla para anotar** (de las que conviene tener automatizadas):

> **Los repositorios conocen a la capa de dominio — jamás a la inversa.** La capa de dominio no debe saber que los repositorios existen. Las entidades son *agnósticas* de su persistencia: ellas viven, y otro las guarda.

¿Por qué? No por seguridad — por **acoplamiento y mantenibilidad**: si el dominio conociera a sus repositorios, tendríamos el doble acoplamiento que venimos matando desde el Punto 1. Mirá el diagrama de arriba: la flecha va del repositorio hacia `Alumno`, nunca al revés. (Y sí — quizás ya te preguntaste: *"si el dominio no conoce al repo, ¿quién llama al repo?"* Excelente pregunta. Hay un director de orquesta que todavía no presentamos. Dos secciones más.)

**¿Por qué `AlumnosRepository` es una interfaz y no una clase?** Es la ventaja 2 del Punto 1 hecha código: **entre capas se conocen contratos, no implementaciones.** Quien use el repositorio conoce solo las firmas — qué operaciones existen — sin saber si por detrás hay una base de datos, un archivo o una lista en memoria. El día que cambies el medio persistente, la clase concreta cambia y ningún consumidor se entera. Este par interfaz-pública / implementación-escondida se repite en todas las capas y lo vas a ver armado en el proyecto real.

### 3.1 Las dos preguntas finas de los repositorios 🔴

**¿Toda entidad de dominio lleva su repositorio?** **No.** Solo se persisten las entidades que interesa guardar. Hay clases **utilitarias** — con comportamiento útil pero sin necesidad de perdurar en el tiempo — que no se persisten, y por lo tanto no tienen repo.

**¿Y puede una entidad persistirse sin repositorio propio?** Sí — y esto es lo fino: **el guardado en cascada.** Quizás te interesa guardar tanto alumnos como calificaciones, pero *no* existe un `RepositorioDeCalificaciones`: existe solo el de alumnos, y **al guardar un alumno se guardan (o actualizan) también sus calificaciones**, en cascada. ¿Cuándo tiene sentido? Cuando la calificación es *dependiente* del alumno — no puede existir sin él. Te suena, ¿no? Es exactamente la sutileza de las relaciones de **agregación y composición** que leíste al principio de la materia: el ciclo de vida del objeto contenido atado (o no) al del contenedor. Aquella distinción "teórica" define acá una decisión concreta de persistencia.

Dos notas de cierre para los repos:

- **Generalmente, una entidad persistible ↔ un repositorio.** Puede haber excepciones (dos repos manipulando la misma entidad), pero son raras y hay que vigilar las responsabilidades — la alta cohesión sigue valiendo para los repos, que son clases como cualquier otra.
- Los repositorios de distintas entidades quedan **muy parecidos entre sí** (todos guardan/buscan/eliminan). La tentación de unificarlos con genéricos existe; para el trabajo práctico, la recomendación es **no hacerlo**: preferí repetir clases repositorio simples antes que meter complejidad que no todo el equipo domina. En el segundo cuatrimestre, con persistencia real, esto se simplifica solo.

---

## 4. La capa intermedia: los services 🔴🔴

Ahora sí, el director de orquesta. Anotá esto tal cual, porque la ubicación es parte de la definición:

> Los **services** viven en una capa de dominio **intermedia** — intermedia entre el **dominio puro** (las entidades) y la **capa de presentación**.

¿Qué son? Los objetos que **orquestan la ejecución de una operación de negocio**. Son casi los objetos más relevantes del sistema:

- **Llaman** a los objetos de dominio, a los repositorios y a otros objetos utilitarios.
- **Focalizan las reglas del flujo — no las reglas del negocio.**

Esa última frase es la más citable de la unidad, así que desarmémosla con el caso de uso *"registrar una calificación de un alumno"*. Su **flujo** es:

1. Validar que el alumno exista.
2. Validar que la materia exista.
3. Crear la calificación.
4. Asociarla al alumno.
5. Persistir (guardar) la calificación.

Recorrelo preguntándote *quién* hace cada paso:

- **"Validar que el alumno exista"** — ¿a quién le pregunto si existe el legajo 12345? Al **repositorio de alumnos**: `buscar(legajo)`. Si devuelve algo, sigo; si no… corto todo y ya sé qué responder hacia afuera: un **404** (el recurso que me nombraste no existe).
- **"Validar que la materia exista"** — ídem, contra el **repositorio de materias**.
- **"Crear la calificación"** — ¿qué implica crearla? `new Calificacion(...)`. Se terminó el misterio. Y con esto cae una de las grandes preguntas pendientes — **regla para anotar**:

> **¿Quién instancia a las clases de dominio? Los services.** Las entidades de dominio se instancian en los services.

- **"Asociarla al alumno"** — `alumno.agregarCalificacion(unaCalificacion)`: le hablo al objeto de dominio, que se encarga de lo suyo (agregar a su colección, validar lo que tenga que validar — eso es regla de **negocio** y vive en la entidad).
- **"Persistir"** — repositorio de calificaciones, `guardar(unaCalificacion)`. (O, si diseñé cascada, guardar el alumno y listo.)

¿Ves la división? El service sabe **el orden de los pasos, las validaciones de existencia, cuándo cortar y qué responder**: las reglas del *flujo*. Las reglas del *negocio* — cómo se calcula un precio, qué valida una venta, qué hace el alumno con su colección — siguen viviendo en las entidades, donde siempre estuvieron. El service coordina; no reemplaza.

Completemos su carnet:

- **Qué tiene permitido conocer:** los objetos de dominio, los repositorios, **y otros services** (services hermanos pueden llamarse entre sí — si un flujo necesita pasos de otro flujo, ese pasamanos ocurre entre services).
- **Los errores no se quedan acá.** Cuando algo falla (el alumno no existe, el permiso no está), el service **lanza la excepción y la patea hacia afuera** — no la maneja él. Quién la atrapa y la convierte en una respuesta HTTP decente lo vas a ver en el proyecto real.
- **Nombres típicos:** `CalificacionesService`, `AlumnosService`, `ProductoService`… Generalmente, **un service por entidad que se quiere manipular** — no un service por caso de uso: un mismo service tiene varios métodos (crear, eliminar, listar…), es decir, varios casos de uso; y un caso de uso puede apoyarse en más de un service.
- ¿Un service *de permisos*? Solo tendría sentido si tu sistema ofrece un ABM dinámico de permisos — raro. No inventes services para todo.
- **Ya venís simulando services sin saberlo:** los tests que escribiste para el dominio hacían el paso a paso de un flujo — instanciar, asociar, verificar. Eso es un service artesanal. Ahora que existen con nombre, también se pueden testear services.

🕳️ **Madriguera — "Use cases" y arquitectura hexagonal.** En algunos proyectos vas a ver clases llamadas `...UseCase` orquestando lo mismo que acá hace un service: suelen seguir otra manera de organizar el sistema (la arquitectura hexagonal), que se menciona hacia fin de año. Equivalencia rápida: use case ≈ service. *Volvé al camino.*

## 5. El código: `CalificacionesService` 🔴🔴

Y acá se cierra el círculo del Punto 3. El chequeo de permisos que no tenía dónde vivir… vive en el service, como primer paso del flujo. Boceto conceptual (las piezas concretas y cómo se consiguen llegan en los Puntos 5 y 6):

```java
public class CalificacionesService {

    public void crearCalificacion(DataCalificacion data, Usuario usuario) {
        // ↑ Recibe DOS cosas: los datos que vienen de afuera para armar la
        //   calificación (data), y el usuario que está intentando ejecutar
        //   este caso de uso. Fijate que NO recibe "un docente": recibe un usuario.

        Permiso permisoCrearCalificaciones = RepositorioDePermisos.buscar("CREAR_CALIFICACIONES");
        // ↑ Paso 0 del flujo: recuperar del repositorio de permisos la instancia
        //   del permiso que este caso de uso exige. (Los permisos eran objetos
        //   "tontos" preconfigurados y persistidos — de ahí los saco.)

        if(!usuario.getRol().tenesPermiso(permisoCrearCalificaciones)) {
            throw new PermisoInsuficienteException(permisoCrearCalificaciones);
        }
        // ↑ La verificación del Punto 3, por fin ubicada: le pido al ROL del
        //   usuario que responda si tiene el permiso. Si NO lo tiene, lanzo una
        //   excepción y el flujo muere acá. Esa excepción, más adelante, se va a
        //   traducir en un 403 hacia quien llamó. El service la patea; no la maneja.

        Calificacion unaCalificacion = new Calificacion();
        //... (acá faltarían pasos que este boceto obvia: buscar al alumno y a la
        //     materia en sus repos, configurar la calificación con los datos de
        //     `data`, asociarla al alumno...)
        // ↑ El service instanciando dominio: la regla en acción.

        RepositorioDeCalificaciones.guardar(unaCalificacion);
        // ↑ Último paso del flujo: persistir. (Si hubiera cascada por el alumno,
        //   sería guardar el alumno y la calificación viajaría con él.)
    }
}

// ¿CÓMO FUNCIONA?
// 1. Alguien (todavía no sabemos quién) llama a crearCalificacion(data, usuario).
// 2. El service busca el permiso exigido y le pregunta al rol del usuario si lo tiene.
//    → NO lo tiene: excepción, corte, (futuro) 403. Nada se creó, nada se guardó.
//    → SÍ lo tiene: sigue.
// 3. Valida existencias contra los repos (404 si algo no está), instancia la
//    calificación, la configura, la asocia al alumno.
// 4. La persiste vía repositorio. Fin del caso de uso, orquestado completo.
```

Ahora mirá lo que este diseño compró — es la respuesta definitiva al antipatrón del Punto 2:

> **En ningún lado dice "docente".** Ni bedel, ni administrador. Al service no le importa *quién sos* en el dominio: le importa **qué rol tenés y si ese rol tiene el permiso exigido**. Si mañana aparece un rol nuevo con el permiso "CREAR_CALIFICACIONES", este código lo deja pasar **sin tocar una línea**. El caso de uso quedó flexible exactamente donde el diseño de una-clase-por-actor obligaba a duplicar o heredar.

Dos menciones que el flujo dejó al costado, cada una con su línea:

- **`DataCalificacion`** podría ser un *value object* — un objeto de valor: inmutable (no cambia una vez creado), que solo transporta los datos ingresados desde afuera, sin importar el rol de quien los mandó. En el proyecto real vas a ver a esta familia de objetos con nombre propio y en detalle.
- La instanciación de la calificación acá fue un `new` pelado; si armar el objeto fuera más complejo, podría ayudar un **patrón creacional** (una familia de patrones de diseño dedicada a construir objetos — todavía no viste ninguno; llegarán).

⚠️ **Nota honesta sobre dónde vive el chequeo:** la verificación de permisos *en el service* es válida y te la vas a cruzar en muchos proyectos — pero no es el único lugar posible. Puede estar en la capa que veremos a continuación, o en un **middleware** (una pieza intermedia que intercepta las peticiones antes de que lleguen a destino), sobre todo cuando entren en juego las sesiones, más adelante en la materia. Para esta unidad: el service es un lugar correcto, y es el que hay que saber defender.

---

## 6. Capa de Presentación: los controllers 🔴

Última capa, última clase típica. En la capa de presentación viven los **controllers** (controladores) — y también vivirían las interfaces gráficas, si las hubiera; por ahora trabajamos presentación de datos pura, sin pantallas. Su definición, densa pero completa:

> Los controllers **reciben las solicitudes externas** (web, API, etc.), las **traducen al lenguaje de dominio**, las **delegan** en las capas inferiores para que las resuelvan, y **devuelven la respuesta** esperada. **Exponen los endpoints del sistema**, estableciendo claramente sus firmas. **Nunca debe haber lógica de negocio real acá.**

(Refresco de una línea: un **endpoint** es cada punto de entrada de una API — la combinación de ruta y verbo HTTP que viste en la clase 02, tipo `GET /productos`.)

Desarmemos la definición con el flujo concreto:

1. Al sistema llega una **request HTTP** con su body (el JSON de la clase 02) pidiendo crear una calificación.
2. La recibe el **controller** — él es quien definió que exista un endpoint para esto (un POST en tal ruta).
3. El controller **traduce**: convierte ese body JSON en una instancia de `DataCalificacion` — del idioma del protocolo al idioma del dominio.
4. **Delega**: llama a `calificacionesService.crearCalificacion(data, usuario)`. La pelota pasó a la capa de servicios. *(Y con esto se responde quién llamaba al service en el código de la sección 5.)*
5. Cuando el service termina (o explota), el controller **responde**: arma la respuesta HTTP con su código de estado y su contenido.

La consecuencia de diseño más importante de este reparto:

> **El service ni se entera de que afuera se habla HTTP.** Las clases service no conocen el protocolo de comunicación con el exterior — podría ser HTTP hoy y otra cosa mañana, y el service no cambia. Los controllers, en cambio, **sí** lo conocen: ellos definen los GET, POST, PUT, DELETE, las rutas, los códigos de estado. El protocolo es asunto exclusivo de la frontera.

Los límites del controller, que es donde se cae la gente:

- **"Nunca lógica de negocio" en serio.** ¿Puede validar algo? Sí — validaciones *mínimas de forma*: campos faltantes, consistencia básica de los datos que llegan. (De hecho vas a encontrar validaciones en todas las capas — pero de tipos distintos en cada una.) Lo que no puede: reglas de negocio, instanciar entidades de dominio, decidir cosas del dominio.
- **¿Y si el caso de uso tiene precondiciones ("el alumno debe existir")?** El controller NO le pregunta a nadie si el alumno existe: delega al service, y si el service lanza la excepción de no-encontrado, el controller la captura y responde el 404. Preguntar es del flujo; el flujo es del service.
- **¿Un controller puede llamar a varios services y combinar sus respuestas?** Puede — funciona — pero ese pasamanos es lógica de flujo, y conceptualmente corresponde a la capa de services (que para eso pueden conocerse entre sí). Un controller cosiendo respuestas de services es una mini-lógica de negocio contrabandeada a presentación.
- **Errores y códigos de estado: los maneja el controller.** Él conoce el protocolo, así que él traduce excepciones a códigos HTTP. ¿Con `try/catch` en cada método? Se puede… y se puede mucho más prolijo, evitando repetirlos — la herramienta concreta te espera en el proyecto real.

Dos aclaraciones de contexto para cerrar:

- **¿Mi service puede terminar hablando con el controller de OTRO sistema?** En una integración, sí — tu sistema consume la API de otro, y del otro lado atiende un controller. Para vos es transparente: en el medio está el protocolo haciendo su magia.
- **No en todo proyecto vas a encontrar capa de services.** Hay tecnologías cuya filosofía fusiona todo lo que acá se repartió entre service y controller en una sola clase llamada "controller" (pasa en frameworks conocidos de PHP y Python). No está bien ni mal: es la filosofía de esa herramienta — y aun ahí podés crear tus services a mano si querés. El entorno con el que vas a trabajar todo el año, en cambio, es estricto con la separación controllers/services tal como la vimos.

---

## 7. El árbol de conocimiento 🔴🔴

Todas las reglas de "quién conoce a quién" que fuimos soltando, juntas en un solo dibujo. Este diagrama ES la unidad — dibujalo de memoria hasta que salga solo:

```
  MUNDO EXTERIOR (HTTP)
        │
        ▼
┌─────────────────┐    conocen a:  services (¡solo eso!)
│   CONTROLLERS   │    NO conocen: otros controllers, dominio, repos
└────────┬────────┘    saben: el protocolo (rutas, verbos, status codes)
         │
         ▼
┌─────────────────┐    conocen a:  entidades + repositorios + otros services
│    SERVICES     │    NO conocen: controllers (ignoran que existen)
└───┬─────────┬───┘    saben: el flujo de cada caso de uso
    │         │
    ▼         ▼
┌─────────┐ ┌──────────────┐    los repos conocen a: entidades
│ENTIDADES│ │ REPOSITORIOS │    NO conocen: services, controllers
│ dominio │◄│ (cap. datos) │
└─────────┘ └──────┬───────┘    las entidades NO conocen A NADIE de
    ▲              ▼             este dibujo: ni repos, ni services,
    │        (medio persistente) ni controllers. Viven, nada más.
    └── el service las instancia
        y les habla
```

Las cuatro reglas, en palabras, para repasar como reflejos:

1. Las **entidades** no conocen a nadie: ni repositorios, ni services, ni controllers. Son agnósticas.
2. Los **repositorios** conocen a las entidades (las traducen para guardarlas) — pero no a services ni controllers.
3. Los **services** conocen a las entidades, a los repositorios y a otros services. No conocen a los controllers.
4. Los **controllers** conocen únicamente a los services. No conocen a otros controllers, ni al dominio, ni a los repos.

Fijate el patrón: **el conocimiento siempre apunta hacia abajo** — es la regla de dirección del Punto 1, ahora con nombres propios. "Más arriba" = más cerca del mundo exterior.

Y la ubicación gráfica final de todo lo que apareció en la unidad, capa por capa:

```
(usuario) ──► ┌─ PRESENTACIÓN ─────────────────────────────────────────┐
              │ • Interfaz gráfica (HTML/CSS/JS, apps nativas...) o APIs│
              │ • CalificacionesController        ← controllers acá     │
              └─────────────────────────────────────────────────────────┘
              ┌─ DOMINIO / NEGOCIO ────────────────────────────────────┐
              │ • CalificacionesService   ← el intermediario           │
              │ • Calificacion · Alumno · Docente   ← dominio puro     │
              └─────────────────────────────────────────────────────────┘
              ┌─ DATOS ────────────────────────────────────────────────┐
              │ • RepositorioDePermisos                                 │
              │ • RepositorioDeCalificaciones                           │
              └────────────────────────────┬────────────────────────────┘
                                           ▼
                                    (base de datos)
```

Precisión de lectura: los controllers **son** capa de presentación (presentación de dato puro, vía API); los services **son** capa de dominio — la franja intermediaria de ese dominio, pegada a presentación. No son capas nuevas: son inquilinos con dirección exacta.

Cerrá el punto con este contraste: la unidad arrancó con una hipotética clase `Alumno` con un método `verCalificaciones()`. Mirá ahora la cantidad de clases que hicieron falta — controller, service, entidades, repositorios de permisos y de calificaciones, el modelo Usuario–Rol–Permiso — cada una con una responsabilidad distinta, en una capa distinta. **Eso es orquestar un caso de uso.** Por eso lleva trabajo; por eso escala.

> **Para el parcial, si te preguntan** *"¿qué diferencia hay entre las reglas del flujo y las reglas del negocio, y dónde vive cada una?"*:
> Las reglas del **flujo** son los pasos de un caso de uso — validar existencias, chequear permisos, instanciar, asociar, persistir, decidir cuándo cortar — y viven en los **services**. Las reglas del **negocio** son el comportamiento propio de las entidades — cómo se calcula un precio, qué valida una venta al agregarse — y viven en el **dominio puro**. El service orquesta llamando a entidades y repositorios; no absorbe su lógica.

> **Para el parcial, si te preguntan** *"¿puede la capa de dominio usar un repositorio para guardarse a sí misma?"*:
> No. El conocimiento entre capas apunta en un solo sentido: los repositorios conocen a las entidades para traducirlas al medio persistente, pero las entidades son agnósticas de su persistencia — no saben que los repositorios existen. Si el dominio conociera a sus repos habría doble acoplamiento entre capas, matando la mantenibilidad y la posibilidad de reemplazar la persistencia de forma transparente. Quien llama al repositorio es el service, que orquesta el flujo.

---

## ✅ Checkpoint del Punto 4

*Respondé sin releer. Sin respuestas a propósito — llegan con el complemento.*

1. Para el caso de uso *"registrar una venta"* del servicio de ventas: enumerá el flujo paso a paso y asigná cada paso a la clase (y capa) que le corresponde. ¿Dónde quedó la regla "la venta solo puede tener productos del comercio" — en el service o en la entidad? ¿Por qué?
2. `TipoProducto` y los impuestos del servicio de ventas: ¿cuáles llevarían repositorio propio y cuáles podrían viajar en cascada o no persistirse? Justificá con el criterio de dependencia y el de utilidad.
3. Un compañero pone `productoRepository.guardar(this)` adentro de la clase `Producto` "para que se guarde solo". Explicale qué regla rompe, qué atributo de calidad paga, y dónde debía ir esa llamada.
4. ¿Por qué el service recibe un `Usuario` y no un `Docente` en `crearCalificacion`? ¿Qué flexibilidad concreta se compró con esa firma?
5. El controller recibe el pedido de crear una calificación de un alumno que no existe. Contá qué hace cada pieza (controller → service → repo) hasta que el que llamó recibe su 404 — y qué NO hace el controller en ese camino.
6. ¿Un service puede llamar a otro service? ¿Un controller a otro controller? ¿Un controller a un repositorio? Respondé las tres con la regla que las gobierna.
7. "El controller valida" y "el controller no tiene lógica de negocio" conviven. ¿Qué tipo de validación le corresponde y cuál le está prohibida? Un ejemplo de cada una.
8. Dibujá de memoria el árbol de conocimiento (las 4 reglas). Después verificá contra el diagrama.
9. Te toca un proyecto donde todo vive en clases llamadas "controller" (sin capa de services). ¿Está necesariamente mal diseñado? ¿Qué le dirías a alguien que quiere agregar services ahí?
10. ¿Quién instancia a las entidades de dominio? ¿Y quién instancia a los services y repositorios? (Si la segunda no la podés responder… perfecto: es el hilo abierto.)

---

## Qué viene en el Punto 5

Quedó UNA pregunta materialmente pendiente — la 10 del checkpoint: los services instancian al dominio, los controllers usan services, los services usan repos… **¿y a los services y repositorios quién los instancia y quién les alcanza lo que necesitan?** En el boceto de la sección 5 hasta aparecían usados de una forma medio rara, sin que nadie los construyera. La respuesta es la lectura de inyección de dependencias que hiciste en la preclase, cobrando vida: hay un tercero que construye todo — y en el Punto 5 lo conocés trabajando.

---

**FIN DEL PUNTO 4**
