# 🧩 Clase desde cero — Clase 04 · Módulo 4: Las clases típicas (II). Services, controllers y el mapa completo

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 4 de 9.** Fase A (teoría). Último módulo de teoría.

## Sobre este documento

**Qué cubre:** las dos piezas que faltan. Los **services**, que orquestan el caso de uso, y los **controllers**, que lo exponen hacia afuera. Con ellos se cierran las preguntas abiertas de M1, M2 y M3, aparecen las **reglas 2, 3 y 4**, y armamos el **mapa completo** de qué clase va en qué capa y quién conoce a quién.
**Qué NO cubre:** cómo se escriben estas clases en Spring Boot. Eso es la Fase B (M5 a M9), donde todo lo de este módulo se vuelve código del servicio de ventas.

## De dónde venís

- De **M1**: las tres capas, la regla entre ellas, y tres preguntas: *¿quién instancia a `Calificacion`?*, *¿quién verifica que es un docente?*, *¿quién guarda?* (esta última ya la cerró M3).
- De **M2**: `Usuario → Rol → * Permiso`, `tenesPermiso(permiso)`, y que roles y permisos viven persistidos. Y la secuencia "quién sos → qué rol → qué permiso → ejecutás o 403".
- De **M3**: entidades en el dominio, repositorios en datos, la **Regla 1** (los repositorios conocen a las entidades, nunca al revés) y la pregunta que dejó: **¿quién llama al repositorio?**
- De la **clase 02**: HTTP, verbos, rutas, status codes. El 404 (*Not Found*).

---

## 1. El caso: registrar una calificación, paso a paso 🔴

Antes de nombrar la pieza nueva, el caso de uso que la necesita. **Registrar una calificación de un alumno.** ¿Qué tiene que pasar, en orden?

1. **Validar que el alumno exista.**
2. **Validar que la materia exista.**
3. **Crear la calificación.**
4. **Agregarla al alumno.**
5. **Persistir (guardar) la calificación.**

Ahora, paso por paso, ¿quién lo resuelve?

- **Paso 1.** ¿Cómo sé si el alumno existe? Le pregunto a quien tiene a todos los alumnos: **el repositorio de alumnos**. `alumnosRepository.buscar(legajo)`. Si devuelve algo, sigo. Si no, corto: no puedo calificar a un alumno que no existe, y hacia afuera eso se responde, típicamente, con un **404** (el recurso que se pidió no existe).
- **Paso 2.** Lo mismo, con **el repositorio de materias**: `materiasRepository.buscar(codigo)`. Si no está, corto.
- **Paso 3.** ¿Qué implica crear una calificación? Un **`new Calificacion(...)`**: como viste en M1, eso es dar de alta. Lo que faltaba saber es quién lo escribe.
- **Paso 4.** `alumno.agregarCalificacion(calificacion)`: un método de la entidad, que la suma a su colección.
- **Paso 5.** `calificacionesRepository.guardar(calificacion)`. O, si las calificaciones se guardan en cascada (M3), `alumnosRepository.guardar(alumno)`.

⚠️ Fijate en el paso 1. Al alumno **solo se le verifica que exista**. No se le verifica ningún permiso: el alumno es **la entidad sobre la que se opera** (a quien se le carga la nota), no **el usuario que ejecuta** (el docente que la carga). Los permisos se le verifican a quien ejecuta. Es la misma distinción de M2 entre el acceso de quien ejecuta y la entidad del dominio.

Mirá quién aparece en ese flujo: dos repositorios, una entidad que se instancia, un método de otra entidad, un tercer repositorio. **Alguien tiene que coordinar todo eso.** No puede ser `Alumno` (las entidades no conocen a los repositorios: Regla 1). No puede ser el repositorio (solo persiste). Hace falta una clase nueva.

---

## 2. Los services: la capa de dominio intermedia 🔴

### 2.1. Qué son

Los **services** (*servicios*) son las clases que **orquestan la ejecución de una operación de negocio**. Llaman a las entidades de dominio, llaman a los repositorios y a otros objetos utilitarios. Son, casi, las clases más relevantes del sistema.

¿En qué capa están? En la de **dominio**, pero en una parte específica: es una **capa de dominio intermedia, entre el dominio puro y la capa de presentación**.

```
   ┌──────────────────────────────────┐
   │          PRESENTACIÓN            │
   └────────────────┬─────────────────┘
   ┌────────────────▼─────────────────┐  ┐
   │   Dominio intermedio: SERVICES   │  │
   ├──────────────────────────────────┤  ├─ CAPA DE DOMINIO
   │   Dominio puro: ENTIDADES        │  │
   └──────────────────────────────────┘  ┘
   ┌──────────────────────────────────┐
   │      DATOS: REPOSITORIOS         │
   └──────────────────────────────────┘
```

Siguen siendo parte del negocio (por eso son dominio), pero no son entidades: no modelan nada del mundo, **coordinan**.

Y la diferencia con las entidades está en qué reglas tienen:

> **Los services se ocupan de las reglas del flujo, no de las reglas de negocio.**

Las **reglas de negocio** (cómo se calcula el precio final, qué impuestos aplican a un tipo de producto, que una venta notifique a los observadores) viven, en un modelo enriquecido, en las entidades: es el modelo enriquecido de M3. Las **reglas del flujo** (primero validar que el alumno exista, después crear, después asociar, después guardar) viven en el service.

Por eso, en un service vas a encontrar: **validaciones** del flujo, **instanciación** de entidades, **configuración** de esas entidades, llamadas a sus métodos, y llamadas a repositorios.

### 2.2. Las reglas 2 y 3

> **Regla 2 (para anotar): las entidades de dominio las instancian los services.**

El `new Calificacion(...)` del paso 3 está en el service, no en el controller: dar de alta una entidad es un paso del flujo del caso de uso.

> **Regla 3 (para anotar): un service conoce a las entidades de dominio, a los repositorios y a otros services.**

Los services pueden conversar con **services hermanos**. Esta es la conexión "al costado" que M1 anticipó cuando dijo que la regla entre capas parecía más estricta de lo que iba a ser.

Con las reglas 1, 2 y 3, el árbol de quién conoce a quién va así:

| Clase | Conoce a… | No conoce a… |
|---|---|---|
| **Entidad de dominio** | otras entidades | repositorios, services |
| **Repositorio** | entidades | services |
| **Service** | entidades, repositorios, **otros services** | (lo que está más arriba: falta presentarlo) |

Se cierran dos hilos abiertos: **¿quién instancia a `Calificacion`?** El service. **¿Quién llama al repositorio?** El service.

¿Y quién crea al service, y a los repositorios que usa? Por ahora no importa: los repositorios aparecen en M5, y cómo llegan al service (junto con quién crea al service), en M7.

> 💡 Si en los tests de la clase 03 armabas los objetos, los configurabas y los hacías interactuar paso a paso, estabas haciendo **de service a mano**. Ahora esa coordinación tiene una clase propia, y por lo tanto también se puede testear.

### 2.3. ¿Un service por caso de uso?

No. **Un service puede atender varios casos de uso.** Si el enunciado pide "gestionar docentes" (alta, baja, listado), habrá un `DocentesService` con `crearDocente`, `eliminarDocente`, `listarDocentes`. Lo **habitual** es **un service por entidad que se manipula**: `CalificacionesService`, `AlumnosService`, `DocentesService`. Y un mismo service puede ser usado por varios casos de uso. "Habitual" no es "obligatorio": el criterio es la cohesión.

> **Para el parcial, si te preguntan:** *¿Qué es un service y qué responsabilidad tiene?*
> Un service es una clase de la capa de dominio intermedia, entre el dominio puro y la presentación, que orquesta la ejecución de un caso de uso: valida el flujo, instancia y configura las entidades, llama a sus métodos y usa los repositorios para persistir. Se ocupa de las reglas del flujo, no de las reglas de negocio, que viven en las entidades.

> 🕳️ **Madriguera — "Use case" en lugar de "service"**
> En algunos proyectos vas a ver clases llamadas `...UseCase` que hacen lo mismo que un service: orquestar un caso de uso. Suelen aparecer en otra arquitectura, llamada **hexagonal**, que organiza algunas cosas de otra manera. Se menciona hacia fin de año.
> *Volvé al camino.*

---

## 3. El service del caso, en código 🔴

Así se ve el caso de uso de §1 dentro de un service, ahora con la verificación de permisos de M2:

```java
public class CalificacionesService {

    // Los repositorios son atributos del service. Quién los crea se ve en M5;
    // cómo llegan acá, en M7.
    private PermisosRepository permisosRepository;
    private AlumnosRepository alumnosRepository;
    private MateriasRepository materiasRepository;
    private CalificacionesRepository calificacionesRepository;

    public void crearCalificacion(DataCalificacion data, Usuario usuario) {

        // ── Permisos: ¿quien ejecuta puede hacerlo? ──────────────────────────
        // El permiso es una instancia persistida (M2): se la pide al repositorio.
        Permiso crearCalificaciones = permisosRepository.buscar("CREAR_CALIFICACIONES");
        if (!usuario.getRol().tenesPermiso(crearCalificaciones)) {
            throw new PermisoInsuficienteException(crearCalificaciones);   // → afuera, 403
        }

        // ── Flujo del caso de uso ───────────────────────────────────────────
        // (acá van los pasos 1 y 2 de §1: buscar al alumno y a la materia en sus
        //  repositorios, y cortar si alguno no existe → afuera, 404)

        Calificacion calificacion = new Calificacion(/* datos de 'data' */);   // Regla 2
        // ... configurar la calificación y asociarla al alumno (paso 4)

        calificacionesRepository.guardar(calificacion);   // paso 5
    }
}
```

```
// ¿CÓMO FUNCIONA?
//   1. Llega el pedido con los datos de la calificación (data) y el usuario que lo ejecuta.
//   2. El service busca la instancia del permiso "crear calificaciones" y le pregunta
//      al rol del usuario si lo tiene. Si no: lanza una excepción, que hacia afuera
//      termina siendo un 403.
//   3. Si lo tiene: valida el flujo, instancia la Calificacion, la asocia y la guarda.
//   Resultado: la calificación queda persistida, o el pedido termina en 403 o 404.
```

Tres cosas para mirar:

- **En ningún lado dice "docente", "bedel" ni "administrador".** El service no pregunta *quién* sos, pregunta si tu rol **tiene el permiso**. Si mañana aparece un rol nuevo con ese permiso, este código lo deja pasar sin tocar una línea. Es la flexibilidad que se buscó en M2, cobrada.
- **`DataCalificacion`** son los datos que ingresó quien hizo el pedido, sea cual sea su rol. Puede ser un **value object**: un objeto de valor, inmutable, que solo transporta datos. Cómo se escribe uno en Java se ve en M6.
- **La instanciación** acá es un `new` simple. Si fuera compleja (muchas partes, muchas configuraciones), podría ayudar un **patrón creacional**.

> 🕳️ **Madriguera — Patrones creacionales**
> Son los patrones de diseño que se ocupan de cómo se crean los objetos cuando un `new` no alcanza. Se ven en la clase 08.
> *Volvé al camino.*

> 🕳️ **Madriguera — Value object vs. entidad**
> La diferencia entre un objeto de valor (se define por sus datos, es inmutable) y una entidad (tiene identidad propia y cambia en el tiempo) es la lectura previa de la clase 06.
> *Volvé al camino.*

### 3.1. ¿Dónde se verifican los permisos?

Se cierra el hilo de M1 y M2. Lo que viste arriba (verificar el permiso en la primera línea del service) **es válido y muy común**. Pero no es el único lugar posible:

- **en el service**, como acá;
- **en el controller**, la capa que viene ahora;
- **en un middleware**: una pieza que se ejecuta *antes* de que el pedido llegue a cualquier controller, y lo deja pasar o lo rechaza.

Cuál se usa depende del proyecto y de cómo se manejen las **sesiones** (cómo el sistema recuerda quién está conectado entre un pedido y el siguiente). Eso se ve en el segundo cuatrimestre; hasta entonces, la versión de este módulo es la de referencia.

---

## 4. Los controllers: la capa de presentación 🔴

### 4.1. Qué son

Falta una pregunta que el service deja abierta: **¿quién llama al service?** ¿Quién recibe el pedido que llega de afuera? (En código, M8.)

En la capa de presentación viven los **controllers** (*controladores*). Un controller:

- **recibe las solicitudes externas** (web, API, etc.);
- **las traduce al lenguaje del dominio**;
- **las delega en las capas inferiores** para que las resuelvan;
- **devuelve la respuesta esperada**.

Además, **los controllers exponen los endpoints del sistema** (las rutas a las que se le puede pedir algo: un `GET` a tal ruta, un `POST` a tal otra) y establecen claramente sus firmas. Y una regla de oro: **en un controller nunca hay lógica de negocio real.**

En esta capa también están las **interfaces gráficas**, si las hay. Durante el primer cuatrimestre la presentación es **no gráfica**: una API REST. Los controllers son, para nosotros, la capa de presentación entera.

### 4.2. Qué sabe un controller que el service no

Volvé al `crearCalificacion(DataCalificacion data, Usuario usuario)` de §3. ¿De dónde sale ese `data`?

Afuera, alguien hizo un pedido HTTP: un `POST` con un *body* en JSON. **El controller** recibe ese pedido, toma el body y lo transforma en una instancia de `DataCalificacion`, para pasársela al service. **El service ni se entera** de que alguien está hablando HTTP del otro lado.

```
   afuera                  PRESENTACIÓN                    DOMINIO
                                                       (intermedio)
  POST /calificaciones   ┌─────────────────────────┐   ┌──────────────────────────┐
  { "legajo": 12345, ─►  │ CalificacionesController│──►│  CalificacionesService   │
    "nota": 8, ... }     │ · conoce HTTP           │   │  · no sabe nada de HTTP  │
                         │ · body JSON → Data...   │   │  · recibe un objeto Java │
  ◄── 201 / 403 / 404 ── │ · excepción → status    │◄──│  · lanza excepciones     │
                         └─────────────────────────┘   └──────────────────────────┘
```

Esa es la división: **los controllers conocen el protocolo de interacción con el mundo exterior**: saben que reciben pedidos HTTP, leen el body y los headers, y saben responder con códigos de estado. **Los services no conocen el protocolo.** Por eso los controllers son los que definen los endpoints (el `GET`, el `POST`, el `PUT`, el `DELETE`, las rutas).

### 4.3. Qué hace y qué no hace un controller

| Un controller **sí** | Un controller **no** |
|---|---|
| Define endpoints (verbo + ruta) | Tiene lógica de negocio |
| Traduce el body al objeto que recibe el service (`DataCalificacion`) | Instancia entidades de dominio |
| Puede validar lo mínimo: campos faltantes, consistencia mínima de los datos | Valida reglas del flujo o del negocio |
| Delega en el service | Habla con repositorios o entidades |
| Traduce un error a un código de estado HTTP | Pregunta si algo existe antes de delegar |

Sobre las validaciones: **hay validaciones en todas las capas**, pero de distinto tipo. El controller puede validar que el pedido esté bien formado; el service, el flujo (¿existe el alumno?); las entidades, las reglas de negocio.

⚠️ **El controller no pregunta "¿esto existe?" antes de delegar.** Si para crear una calificación el alumno tiene que existir, el controller **no** le pregunta al service "¿existe el alumno 12345?" para devolver 404 él mismo. Le pasa el pedido al service, y si el service falla con una excepción, el controller la traduce a un código de estado. La validación de existencia es del flujo, y el flujo es del service.

**¿Y quién elige el código de estado?** El service no: lanza una excepción, y la presentación la traduce. Hacerlo con un `try/catch` en cada método del controller repite código por todos lados; la forma prolija es un **manejador global** que traduce cada tipo de excepción a un código de estado en un solo lugar. Lo vas a ver en M8.

### 4.4. La regla 4

> **Regla 4 (para anotar): los controllers conocen solo a los services. No conocen a las entidades, ni a los repositorios, ni a otros controllers.**

Y del otro lado: **los services no conocen a los controllers.** El controller está "más arriba", más cerca del mundo exterior: recibe, y tira hacia abajo.

⚠️ Conocer al service incluye conocer los objetos de su firma, como `DataCalificacion`, que no es una entidad: solo transporta datos. ¿Y el `Usuario` que recibe `crearCalificacion`? Sale de quién está identificado en el pedido. Cómo se obtiene depende de las sesiones (§3.1), que se ven en el segundo cuatrimestre.

Ojo: en este ejemplo el service recibe un `Usuario`, que es una entidad, y eso obligaría al controller a conocerla. Es una simplificación del ejemplo. Para respetar la Regla 4, el controller le pasa solo un identificador (por ejemplo, el nombre de usuario que viene en la sesión) y el service busca el `Usuario` en un repositorio de usuarios, igual que hace con el alumno (el mapa de §5.1 no lo dibuja porque usa la versión simplificada).

¿Puede un controller llamar a dos services y usar la respuesta de uno para llamar al otro? **Funciona, pero conceptualmente está mal**: ese pasamanos es una mini lógica de negocio metida en el controller. Ese encadenamiento les corresponde a los services, que sí se pueden conocer entre sí (Regla 3).

¿Hay un solo controller? No: hay uno por cada recurso que el sistema expone. Si hay casos de uso sobre calificaciones, alumnos y docentes, habrá `CalificacionesController`, `AlumnosController`, `DocentesController`.

> **Para el parcial, si te preguntan:** *¿Qué conoce un controller?*
> Un controller conoce solo a los services: no conoce a las entidades, ni a los repositorios, ni a otros controllers. Recibe el pedido externo, lo traduce al lenguaje del dominio, delega en el service y traduce el resultado (o el error) a una respuesta con su código de estado. No tiene lógica de negocio ni instancia entidades.

### 4.5. No todo proyecto tiene services 🟡

Lo que acabás de ver son las clases **típicas**, pero no están en todos los proyectos. Algunos frameworks **suprimen la capa de services**: ponen lo que hace un service y lo que hace un controller en una sola clase, y a toda la llaman *controller*. Es el caso de **Laravel** (PHP) y de **Django** (Python): es su filosofía, ni buena ni mala. Otros frameworks trabajan estrictamente separados en controllers y services: es el caso de **Spring Boot**, el de la materia.

Dependiendo de la tecnología, vas a encontrar estas clases típicas o variantes de ellas.

> 🕳️ **Madriguera — Cuando el otro está en otro sistema**
> Si tu sistema se integra con otro por su API REST, el pedido que hace tu service llega al **controller** del otro sistema. Para tu código es transparente: en el medio está el protocolo. La integración de sistemas tiene su propia clase más adelante.
> *Volvé al camino.*

---

## 5. El mapa completo 🔴

### 5.1. Cada clase en su capa

Volvé al principio de M1: una clase `Alumno` con `verCalificaciones()` y una clase `Docente` con `administrarCalificaciones()`. Así queda ese mismo problema, con cada responsabilidad en su lugar:

```
  (usuario)
      │
      ▼
 ┌─ PRESENTACIÓN ───────────────────────────────────────────────────────┐
 │  gráfica:     HTML / CSS / JS · apps nativas · desktop               │
 │  no gráfica:  CalificacionesController            ◄── API REST       │
 └──────────────────────────────────┬───────────────────────────────────┘
                                    ▼
 ┌─ DOMINIO ────────────────────────────────────────────────────────────┐
 │  intermedio:  CalificacionesService                                  │
 │  ┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄ │
 │  puro:        Calificacion · Alumno · Docente · Materia · Usuario ·  │
 │               Rol · Permiso                                          │
 └──────────────────────────────────┬───────────────────────────────────┘
                                    ▼
 ┌─ DATOS ──────────────────────────────────────────────────────────────┐
 │  PermisosRepository · AlumnosRepository ·                            │
 │  MateriasRepository · CalificacionesRepository                       │
 └──────────────────────────────────┬───────────────────────────────────┘
                                    ▼
                             (base de datos)
```

Dos precisiones sobre este dibujo, porque es fácil leerlo mal:

- **Los controllers son presentación**, no una capa aparte. Son la presentación **no gráfica** (la de los datos puros), al lado de la gráfica.
- **Los services son dominio**, no una capa aparte. Son el dominio **intermedio**, encima del puro.

Recordá la aclaración de M1 §2.3: la caja "Servicios" del diagrama web era integración con otros sistemas, no esta capa.

### 5.2. Quién conoce a quién (las cuatro reglas juntas)

| Clase | Conoce a… | No conoce a… |
|---|---|---|
| **Controller** | services | entidades, repositorios, otros controllers |
| **Service** | entidades, repositorios, otros services | controllers |
| **Repositorio** | entidades | services, controllers |
| **Entidad** | otras entidades | repositorios, services, controllers |

Leída hacia abajo: **el controller conoce solo la capa inmediata (services); el service conoce las dos de abajo (entidades y repositorios) y a sus pares; el repositorio conoce a las entidades; las entidades no conocen a nadie fuera del dominio puro.**

⚠️ ¿Que el service conozca "las dos de abajo" no es saltear una capa? No: services y entidades son la misma capa, dominio, y el dominio usa a datos. Lo que la regla de M1 prohíbe es que presentación hable directo con datos. Por eso el controller nunca toca un repositorio.

¿Dónde está el trabajo? **La mayor parte pasa en los services y en las entidades.** Los repositorios solo interactúan con el medio persistente; los controllers solo reciben y responden.

Mirá la cantidad de clases que hicieron falta para un caso de uso que, al principio de M1, alguien quería resolver con un método. Así se orquestan todos los casos de uso: tirando de a poco cada responsabilidad hacia la clase que le corresponde. Por eso lleva tiempo diseñar cada uno.

> **Para el parcial, si te preguntan:** *Ubicá en capas las clases que intervienen en "registrar una calificación".*
> En presentación, `CalificacionesController` (recibe el pedido HTTP, lo traduce y delega); en el dominio intermedio, `CalificacionesService` (verifica permisos, valida que existan alumno y materia, instancia la `Calificacion`, la asocia y la persiste); en el dominio puro, `Calificacion`, `Alumno`, `Docente`, `Materia`, `Usuario`, `Rol` y `Permiso`; en datos, los repositorios de permisos, alumnos, materias y calificaciones (sin el de calificaciones, si se guardan en cascada con el alumno).

---

## 6. Lo que te llevás de este módulo

- Un caso de uso tiene un **flujo** (validar, crear, asociar, guardar) que alguien tiene que coordinar. Ese alguien es el **service**.
- **Services:** dominio intermedio. Reglas del **flujo**, no de negocio. Instancian, configuran, llaman a entidades y repositorios.
- **Regla 2:** las entidades las instancian los services. **Regla 3:** un service conoce a entidades, repositorios y otros services.
- La verificación de permisos puede estar en el service, en el controller o en un middleware.
- **Controllers:** presentación (no gráfica). Conocen el protocolo; definen endpoints; traducen el pedido y delegan; traducen errores a códigos de estado. Nunca lógica de negocio.
- **Regla 4:** los controllers conocen solo a los services. Los services no conocen a los controllers.
- Al alumno se le verifica que exista; los permisos se le verifican a quien ejecuta.
- El controller no pregunta si algo existe: delega y traduce el error.
- No todos los frameworks tienen services (Laravel, Django). Spring Boot sí.

---

## ✅ Checkpoint del Módulo 4

Respondé de memoria. Las respuestas van al complemento.

1. Enumerá los pasos del caso de uso "registrar una calificación" y decí, para cada uno, qué clase lo resuelve.
2. ¿En qué capa está un service, y por qué se dice que es "intermedia"? ¿Qué diferencia hay entre una regla del flujo y una regla de negocio? Da un ejemplo de cada una del servicio de ventas.
3. Enunciá las reglas 2, 3 y 4.
4. En el código del `CalificacionesService`, ¿por qué no aparece la palabra "docente"? ¿Qué se gana con eso?
5. Al registrar una calificación, ¿a quién se le verifican los permisos y a quién solo que exista? ¿Por qué?
6. ¿Qué sabe un controller que un service no sabe? ¿Por qué esa separación es útil?
7. Un compañero escribe un controller que le pregunta al service si el alumno existe y, si no, devuelve 404. ¿Qué está mal?
8. ¿Dónde puede verificarse un permiso? Nombrá los tres lugares.
9. En el mapa final, ¿por qué los controllers no son "una cuarta capa"? ¿Y los services?
10. ¿Qué tienen de particular Laravel y Django respecto de los services?

---

## Qué viene después

**M5 — El proyecto y la capa de datos.** Termina la teoría y empieza el código. Abrimos el proyecto del servicio de ventas de SmartLife, vemos cómo cada paquete materializa una capa, y escribimos la capa de abajo: los cuatro repositorios, guardando en memoria. Ahí se responde la primera mitad de una pregunta que dejó este módulo: **quién crea los repositorios**. Cómo llegan al service, en M7.

**FIN DEL MÓDULO 4**
