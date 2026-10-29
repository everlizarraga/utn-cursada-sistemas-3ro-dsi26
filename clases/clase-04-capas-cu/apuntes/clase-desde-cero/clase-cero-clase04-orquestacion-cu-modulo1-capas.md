# 🧩 Clase desde cero — Clase 04 · Módulo 1: Un caso de uso no es un método. El modelo en capas

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 1 de 9.** Fase A (teoría).

## Sobre este documento

**Qué cubre:** el error de diseño más común al pasar de Paradigmas a Diseño (ponerle a una clase-actor un método con nombre de caso de uso), por qué está mal, y la herramienta para arreglarlo: el **modelo en capas**. Qué es cada capa, cuál es la regla entre ellas, qué ventajas y desventajas tiene dividir en capas, y qué queda abierto al final.
**Qué NO cubre:** qué clases concretas viven en cada capa (eso es M3 y M4), cómo se modelan usuarios y permisos (M2), ni una línea de Spring Boot (Fase B). Tampoco entra la base de datos ni la interfaz gráfica: las dos se ven en el segundo cuatrimestre. Hoy la capa de datos va a guardar en memoria y la presentación va a ser una API.

## De dónde venís

- De la **clase 03** traés el dominio del servicio de ventas de SmartLife modelado y probado: `Comercio`, `Producto`, `TipoProducto`, `Impuesto` (IVA, EO, EI), `Venta`, `ItemVenta`, `ObservadorVenta`. Y una idea que vas a volver a usar: que dos objetos se conozcan mutuamente (A conoce a B y B conoce a A) es **doble acoplamiento**, y se evita.
- De la **clase 02** traés qué es una API REST y cómo se consume.
- De la **clase 01**, qué componentes tiene un sistema (frontend, backend, base de datos).
- De **Análisis de Sistemas**, actores, casos de uso y su diagrama.
- De **Paradigmas**, el reflejo de modelar todo como objetos con comportamiento. Ese reflejo es justamente el tema de hoy.

---

## 1. El caso: el SIU Guaraní y dos requerimientos 🔴

Antes de cualquier teoría, un problema concreto. Tomá el sistema de gestión académica que usás todos los cuatrimestres y estos dos requerimientos:

- *El sistema debe permitir que los alumnos visualicen sus calificaciones.*
- *El sistema debe permitir la administración (alta, baja y modificación) de calificaciones por parte de los docentes.*

El diagrama de casos de uso sale solo:

```
            ┌─ pkg SIU ────────────────────────────┐
     o      │     ╭──────────────────────╮         │
    /|\ ────┼─────│  Ver calificaciones  │         │
    / \     │     ╰──────────────────────╯         │
  Alumno    │                                      │
     o      │  ╭──────────────────────────────╮    │
    /|\ ────┼──│  Administrar calificaciones  │    │
    / \     │  ╰──────────────────────────────╯    │
  Docente   └──────────────────────────────────────┘
```

Dos actores, dos casos de uso. Ahora la pregunta de diseño: **¿qué clases hacen falta para implementar esto?**

### 1.1. El reflejo: una clase por actor, un método por caso de uso ❌

Lo primero que se le ocurre a casi todo el mundo que viene de Paradigmas es esto:

```
┌────────────────────────────┐      ┌─────────────────────────────────────┐
│          Alumno            │      │              Docente                │
├────────────────────────────┤      ├─────────────────────────────────────┤
│ + verCalificaciones()      │      │ + administrarCalificaciones()       │
└────────────────────────────┘      └─────────────────────────────────────┘
```

Si te sentiste identificado, perfecto: este módulo está escrito para vos. En Paradigmas los enunciados no tenían **actores**, ni roles, ni usuarios: había objetos con comportamiento y nada más. En Diseño aparecen los actores, y el reflejo es convertirlos en clases y colgarles los casos de uso como métodos.

El reflejo falla. Para ver por qué, hacete la única pregunta que importa: **¿qué hace ese método por dentro?**

- `alumno.verCalificaciones()`: ¿qué pasa cuando lo llamo? ¿Se abre una pantalla? Un método de una clase de dominio no puede abrir una pantalla: la interfaz gráfica es otro componente, puede estar en otra máquina, puede ni existir todavía. Y si alguien te dice "devuelve la lista de calificaciones", entonces es un **getter** (`getCalificaciones()`), no un "ver". *Ver* es una acción que ocurre en una pantalla, y desde el dominio no sabés siquiera si hay una pantalla de por medio.
- `docente.administrarCalificaciones()`: peor. *Administrar* son cuatro operaciones (alta, baja, modificación, listado). ¿Las cuatro en un método? ¿Con qué parámetros? ¿Qué devuelve?

El problema no es solo el nombre, pero también es el nombre: un método con nombre de **caso de uso**, de **botón** o de **funcionalidad completa** es la señal de que se está mezclando algo.

### 1.2. Y cuando crece, se rompe

Supongamos que insistís. Mañana llega un requerimiento nuevo: *el Bedel también puede administrar calificaciones.*

> **Bedel:** personal administrativo de la facultad que gestiona aulas, toma asistencia y hace trámites internos.

Siguiendo la misma lógica: clase `Bedel`, método `administrarCalificaciones()`. Y ahora tenés la misma lógica dos veces. "Para eso está la herencia": `Docente` y `Bedel` heredan de una superclase que tenga el código en común. ¿Cómo se llama esa superclase? ¿Qué pasa cuando el Administrador de la plataforma también puede, pero además puede otras cosas? Ya estás en un **enchastre estructural** del que es difícil salir, y todo por una decisión tomada en el primer minuto.

### 1.3. El diagnóstico

La discusión **no** pasa por entender que solo los docentes administran y solo los alumnos ven. Eso es cierto y después vamos a modelarlo (M2). La discusión pasa por otra cosa:

> **Ese diseño mezcla responsabilidades de distintas capas del sistema en un solo método.** Quién tiene permiso de hacer algo (un problema), qué datos hay que guardar (otro), cómo se presentan (otro) y qué reglas de negocio aplican (otro más) no pueden vivir juntos en `docente.administrarCalificaciones()`.

Un caso de uso **no se resuelve en un método de una clase**. Un caso de uso **se desglosa en varias clases, cada una con una responsabilidad distinta, en una capa distinta**. Para entender eso hace falta la herramienta de este módulo.

> **Para el parcial, si te preguntan:** *¿Por qué está mal una clase `Alumno` con un método `verCalificaciones()`?*
> Porque mezcla en un método de dominio responsabilidades de distintas capas: "ver" es una acción de la capa de presentación, y el método no puede abrir una pantalla ni saber si la hay. El caso de uso "ver calificaciones" se desglosa en varias clases de distintas capas; en el dominio lo que existe es la entidad `Calificacion` y, a lo sumo, un `getCalificaciones()` en `Alumno`. Nombres de caso de uso, de botón o de funcionalidad completa en un método son la señal de alarma.

---

## 2. El modelo en capas 🔴

### 2.1. Qué es

**Dividir en capas** (*layering*) es una de las técnicas más comunes para resolver un problema de diseño complejo: en lugar de un gran bloque, el sistema se parte en capas donde

- cada capa es **un todo coherente**: tiene alta **cohesión** (sus clases hacen cosas del mismo tipo) y cumple **un único rol** en el sistema;
- **las capas superiores usan servicios de las inferiores**, pero no al revés, ni salteando niveles.

```
        ┌───────────────────────┐
        │     Capa superior     │ ──usa──┐
        ├───────────────────────┤        │
        │      Capa media       │ ◄──────┘ ──usa──┐
        ├───────────────────────┤                 │
        │     Capa inferior     │ ◄───────────────┘
        └───────────────────────┘
          (nunca hacia arriba · nunca salteando)
```

¿Por qué no al revés? Es lo mismo que ya viste con dos objetos: si la capa A conoce a la B y la B conoce a la A, hay **doble acoplamiento**. Lo que buscamos es que **una conozca a la otra, y la otra ni se entere de quién la usa**.

Dos aclaraciones antes de seguir, porque las dos generan confusión:

1. **La regla, dicha así, parece más estricta de lo que va a ser.** Cuando lleguemos a las clases concretas (M4) vas a ver una capa que tiene conexiones "al costado", con sus pares. La idea de partida es la jerarquía; la excepción se presenta cuando aparezca.
2. **Una capa es un concepto, no una construcción del lenguaje.** En Java no existe "la capa"; lo que existe son los **paquetes** (*packages*), y una capa se **materializa** agrupando sus clases en un paquete. Cuando veas el proyecto (M5), cada paquete va a ser una capa o un pedazo de una.

### 2.2. Las tres capas típicas

Un sistema de información puede dividirse, **mínimamente**, en tres capas. Dos formas de dibujarlas, que dicen lo mismo:

```
   Círculos concéntricos                   Flujo vertical

  ┌──────────────────────────┐            (usuario)   (usuario)
  │ Presentación             │                 \        /
  │  ┌────────────────────┐  │              ┌─────────────────┐
  │  │ Dominio · Negocio  │  │              │   Presentación  │
  │  │  ┌──────────────┐  │  │              └────────┬────────┘
  │  │  │    Datos     │  │  │                       ▼
  │  │  └──────────────┘  │  │              ┌─────────────────┐
  │  └────────────────────┘  │              │     Dominio     │
  └────────────▲─────────────┘              └────────┬────────┘
               │                                     ▼
           (usuario)                        ┌─────────────────┐
                                            │      Datos      │
                                            └───┬─────────┬───┘
                                                ▼         ▼
                                            (base de   (otro
                                             datos)  almacenamiento)
```

El usuario toca la capa de afuera. Lo demás está adentro y él ni lo ve.

**Capa de presentación.** Es la encargada de **presentar los datos al usuario**: es con la que el usuario interactúa. ⚠️ *Presentar* no quiere decir *mostrar en pantalla*. Hay al menos dos formas de presentar datos:

- mediante una **interfaz gráfica**: una aplicación de escritorio, una web, una app móvil;
- mediante una **API**, por ejemplo una API REST: el sistema entrega datos y otro sistema (o un frontend aparte) es el que después los muestra.

Las dos son capa de presentación. Hoy, y durante todo el primer cuatrimestre, la nuestra va a ser la segunda. Lo que hay que retener: **va a haber clases cuya responsabilidad sea entregar datos hacia afuera**.

**Capa de dominio (o de negocio).** Es la encargada de **modelar las reglas de negocio y las entidades del dominio**. Contiene **la parte estructural y de comportamiento que da sostén a todos los casos de uso** del sistema.

Esto ya lo hiciste. Todo lo que modelaste en la clase 03 es capa de dominio: `Comercio`, `Producto`, `TipoProducto`, los impuestos, los observadores. Si el caso de uso es "dar de alta productos", necesitás la clase `Producto`; si el producto es de un tipo, necesitás `TipoProducto`; si le aplican impuestos, necesitás `Impuesto`. Eso es *dar sostén estructural* al caso de uso. Todo lo que conocés de Paradigmas vive en esta capa.

**Capa de datos.** Es la encargada de **la persistencia**: guardar los datos del sistema en algún medio durable y recuperarlos. Preguntas como *"¿de dónde saco todos los alumnos del sistema?"*, *"¿dónde guardo este producto?"*, *"¿los tipos de producto ya están cargados o los tengo que crear?"* se responden acá. Si hay una base de datos, esta capa es la que habla con ella. El medio persistente puede ser una base, un archivo, incluso un servicio externo: lo que define a la capa es que **se ocupa del guardado**, no de qué hay atrás.

> 🕳️ **Madriguera — "¿Esto no es MVC?"**
> No. MVC (Modelo-Vista-Controlador) es un **patrón de interacción**: cómo se reparten la pantalla, los datos y las acciones del usuario. El modelo en capas es un **ordenamiento del sistema**. Se cruzan, pero no son lo mismo. MVC se ve en el segundo cuatrimestre.
> *Volvé al camino.*

### 2.3. Las capas en una aplicación web clásica 🟡

Para ubicarlas en algo real, el modelo en capas de una aplicación web tradicional:

```
┌───────────────┐   ┌──────────────────────────────────────────────┐     ┌────────────────────┐
│ Navegador Web │   │               Aplicación Web                 │  ┌──│ Servicios Externos │
│ ┌───────────┐ │   │ ┌────────────┐ ┌───────────┐ ┌────────────┐  │  │  └────────────────────┘
│ │HTML/CSS/JS│─┼───┼─│Presentación│─│ Lógica de │─│  Acceso a  │──┼──┤
│ └───────────┘ │   │ └────────────┘ │  Negocio  │ │   Datos    │  │  │  ┌────────────────────┐
└───────────────┘   │                └───────────┘ └────────────┘  │  └──│   Base de Datos    │
                    │               ┌───────────┐                  │     └────────────────────┘
                    │               │ Servicios │                  │
                    └───────────────┴─────┬─────┴──────────────────┘
                                          │
                              ┌───────────┴──────────┐
                              │  Otras Aplicaciones  │
                              └──────────────────────┘
```

- **En el navegador** hay HTML (el contenido; es un lenguaje de marcado, no de programación), CSS (el estilo) y JavaScript (la interactividad, ejecutándose del lado del navegador). Eso es *parte* de la presentación, pero no toda: la presentación también vive en el servidor.
- **En la aplicación web**, del lado del servidor, están las tres capas: **presentación** (genera las vistas o entrega los datos), **lógica de negocio** (lo grueso: reglas, validaciones, entidades) y **acceso a datos** (habla con la base y con los servicios externos).
- ⚠️ **La caja "Servicios" de abajo no es la capa de "services" que vas a ver en M4.** Esa caja habla de **integración**: la aplicación brinda servicios a otras aplicaciones o los consume de ellas. Misma palabra, otra cosa. La integración entre sistemas tiene su propia clase más adelante en el año.

Este diagrama es el **clásico** (pensá en una enciclopedia web: el servidor arma el HTML ya listo y el navegador solo lo muestra). Esa variante se llama **cliente liviano**: la mayor parte del trabajo está del lado del servidor. En un **cliente pesado** la aplicación que corre en el dispositivo del usuario (una app móvil, un juego) tiene sus propias capas de presentación, lógica y acceso a datos, y su capa de acceso a datos es la que habla con un backend que, a su vez, tiene lógica propia. Las capas aparecen igual; cambia de qué lado cae cada responsabilidad.

### 2.4. Ventajas y desventajas de dividir en capas

Esto se pregunta tal cual. Son cinco a favor y dos en contra, y en los dos sentidos la palabra que importa es **acoplamiento**.

| ✅ Ventajas | Qué significa en la práctica |
|---|---|
| **Nos mantiene enfocados en el problema a resolver** | Cuando trabajás en la capa de datos pensás en cómo persistir; cuando trabajás en presentación, en cómo entregar los datos. Un problema a la vez |
| **Esconde el detalle de cómo se llevan a cabo los servicios que expone** | Cada capa expone un **contrato** (una interfaz) y no cuenta cómo lo implementa. Es lo que vamos a intentar lograr a nivel código |
| **La implementación de una capa puede reemplazarse de forma transparente para quien la consume** | Si la capa de arriba conoce solo el contrato, se puede cambiar la capa de abajo sin que la de arriba se entere. ⚠️ Es la teoría: en la práctica depende de qué tan prolija sea la implementación. Nosotros vamos a tratar de que se cumpla |
| **Minimiza la dependencia entre componentes** | Porque cada capa conoce de la otra solo una interfaz |
| **Facilita las pruebas** | Cada capa se puede probar por separado |

| ❌ Desventajas | Qué significa en la práctica |
|---|---|
| **Los cambios pueden generar efecto cascada** | Te piden "un campo más en el formulario": tocás presentación (se muestra), lógica de negocio (hay reglas nuevas para ese campo) y datos (hay que guardarlo). Un cambio, tres capas |
| **Demasiadas capas agregan complejidad y afectan el rendimiento** | En un problema sencillo, meter capas de más es innecesario. Las que vas a ver son las típicas, pero **no siempre están todas** en todos los proyectos |

Una pregunta que surge siempre: *"entonces, ¿el acoplamiento entre capas es alto?"* No necesariamente. Puede ser **relativamente bajo**, porque se conocen a través de un contrato. Pero el efecto cascada existe igual: hay un acoplamiento leve e inevitable. Qué tan leve queda, **lo decide tu implementación** y, en parte, el framework que uses.

> **Para el parcial, si te preguntan:** *Nombrá dos ventajas y dos desventajas del modelo en capas.*
> Ventajas: esconde el detalle de implementación detrás de un contrato (cada capa expone una interfaz, no cómo la resuelve) y por eso minimiza la dependencia entre componentes y permite reemplazar una capa sin tocar a sus consumidores; además facilita las pruebas por separado. Desventajas: los cambios generan efecto cascada (un campo nuevo toca presentación, negocio y datos), y demasiadas capas agregan complejidad y costo de rendimiento.

> 🕳️ **Madriguera — Para profundizar**
> El texto de referencia de esta separación es "Presentation Domain Data Layering", de Martin Fowler (martinfowler.com/bliki/PresentationDomainDataLayering.html). Es corto y dice lo mismo que esta sección, con más matices sobre cuándo vale la pena.
> *Volvé al camino.*

---

## 3. Volver al caso con la herramienta nueva 🔴

### 3.1. Lo mínimo que tiene que existir

Con las tres capas en la cabeza, volvamos al SIU. ¿Qué es lo primero que tiene que existir para que "ver calificaciones" y "administrar calificaciones" sean posibles?

**La entidad `Calificacion`.** Mínimamente, con una nota numérica, la fecha y hora, el curso (asignatura, año, cuatrimestre, docente) y el alumno. Es la clase que **no puede faltar estructuralmente**: sin ella no hay caso de uso que se cumpla. De ahí para arriba empieza el modelado con el cliente: ¿modelamos las materias? ¿los docentes? ¿qué más de la calificación interesa?

```
┌──────────────────────────────┐
│         Calificacion         │
├──────────────────────────────┤
│ - nota (numérica)            │
│ - fechaHora                  │
│ - curso                      │   ← asignatura / año / cuatrimestre / docente
│ - alumno                     │      (¿clase aparte? se decide con el cliente)
└──────────────────────────────┘
```

Esta clase pertenece a la **capa de dominio**. Y fijate algo tan simple que se pasa por alto: **cuando `Calificacion` se instancia, alguien está dando de alta una calificación.** El alta no es un método mágico de `Docente`: es un `new Calificacion(...)`.

⚠️ *"¿`Calificacion` no debería estar también en la capa de datos?"* No. Pertenece al dominio. La capa de datos la **conoce**, y la **traduce** a alguna estructura persistente (una fila de una tabla, un objeto JSON en un archivo) para guardarla. Conocerla no es tenerla.

### 3.2. Las tres preguntas que quedan abiertas

Con `Calificacion` resuelto lo estructural, quedan tres preguntas sin respuesta. Anotalas, porque los próximos módulos existen para contestarlas:

| Pregunta | Se contesta en |
|---|---|
| **¿Quién instancia a `Calificacion`?** Alguien tiene que hacer el `new`, configurar el objeto y asociarlo al alumno. ¿Quién? | M4 |
| **¿Cómo me aseguro de que quien la está dando de alta es un docente y no un alumno?** | Con qué: M2. Dónde: M4 |
| **¿Quién la guarda?** Si el dominio no se ocupa de persistir, ¿quién? | M3 |

### 3.3. El mismo razonamiento sobre SmartLife

Ahora el caso que vas a implementar. El servicio de ventas tiene tres casos de uso mínimos:

- **Gestionar productos:** los comercios deben poder dar de alta, modificar, eliminar y listar los productos que venden.
- **Registrar ventas:** los comercios deben poder dar de alta ventas de sus productos; cada venta puede involucrar uno o varios productos.
- **Consultar precios:** los comercios deben poder consultar los precios finales de sus productos.

El actor es el **comercio**. El reflejo diría `Comercio.consultarPrecioDeProducto()`, `Comercio.darDeAltaProducto()`. Ya sabés por qué no.

Lo que sí sabés es **qué les da sostén estructural**, porque lo modelaste en la clase 03:

| Caso de uso | Clases de dominio que lo sostienen |
|---|---|
| Gestionar productos | `Producto`, `TipoProducto`, `Comercio` (que tiene sus productos) |
| Registrar ventas | `Venta`, `ItemVenta`, `Producto`, `Comercio` (que registra la venta y avisa a sus `ObservadorVenta`) |
| Consultar precios | `Producto`, `TipoProducto`, `Impuesto` (IVA, EO, EI) |

Eso es la capa de dominio del servicio de ventas, **completa**. Lo que falta, y es lo que se construye en la Fase B, son las otras capas: quién recibe el pedido de afuera (presentación), quién lo orquesta (una capa intermedia que todavía no nombramos) y quién guarda los productos y las ventas (datos). Un proyecto que en la clase 03 era chico va a crecer mucho en la Fase B, y casi nada de lo que se agrega es dominio.

> **Para el parcial, si te preguntan:** *¿Qué significa "orquestar un caso de uso en capas"?*
> Significa que un caso de uso no se implementa en un método de una clase sino que se **desglosa** en varias clases, cada una con una responsabilidad distinta y ubicada en una capa distinta: las entidades que le dan sostén en el dominio, las que lo persisten en datos y las que lo exponen en presentación. La clase que modela al actor (si existe) no lleva el caso de uso como método.

---

## 4. Lo que te llevás de este módulo

- Un método con nombre de caso de uso, de botón o de funcionalidad completa en una clase-actor es **señal de que se están mezclando capas**.
- **Tres capas mínimas:** presentación (gráfica o API), dominio/negocio (entidades y reglas; todo lo de Paradigmas), datos (persistencia).
- **Regla:** la capa superior usa a la inferior, nunca al revés, nunca salteando. En Java, una capa se materializa como un paquete.
- **Ventajas:** foco, detalle escondido detrás de un contrato, reemplazo transparente (en teoría), menos dependencia, pruebas por separado. **Desventajas:** efecto cascada, capas de más = complejidad.
- **Tres preguntas abiertas:** quién instancia, quién verifica permisos, quién guarda.

---

## ✅ Checkpoint del Módulo 1

Respondé de memoria. Las respuestas van al complemento, no acá.

1. ¿Por qué `alumno.verCalificaciones()` es un mal diseño, aunque compile y "ande"? Da las dos razones (la del nombre y la de fondo).
2. ¿Qué pasa con ese diseño cuando aparece un tercer actor que también administra calificaciones? ¿Por qué la herencia no lo arregla?
3. Enunciá la regla del modelo en capas y explicá, con el concepto de acoplamiento, por qué no vale en sentido contrario.
4. ¿Por qué "capa de presentación" no es sinónimo de "pantalla"? Da las dos formas de presentar datos.
5. ¿En qué capa está todo lo que modelaste en la clase 03? ¿Qué quiere decir que esas clases "dan sostén" a los casos de uso?
6. Explicá la desventaja del efecto cascada con el ejemplo del campo nuevo en un formulario.
7. ¿Qué es lo mínimo que tiene que existir, estructuralmente, para que "administrar calificaciones" sea posible? ¿A qué capa pertenece, y qué hace la capa de datos con ella?
8. ¿Cuáles son las tres preguntas que este módulo deja abiertas?

---

## Qué viene después

**M2 — Usuario, rol y permiso.** Antes de ver qué clases viven en cada capa, hay que resolver la segunda pregunta abierta: cómo se modela *quién puede hacer qué*. Vas a ver por qué la respuesta no es una herencia, qué es un usuario (y qué no), y cómo un mismo acceso puede tener varios roles.

**FIN DEL MÓDULO 1**
