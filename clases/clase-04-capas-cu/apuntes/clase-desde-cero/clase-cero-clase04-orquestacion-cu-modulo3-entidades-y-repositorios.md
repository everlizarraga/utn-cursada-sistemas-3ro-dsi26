# 🧩 Clase desde cero — Clase 04 · Módulo 3: Las clases típicas (I). Entidades y repositorios

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 3 de 9.** Fase A (teoría).

## Sobre este documento

**Qué cubre:** empezamos a poblar con clases concretas las dos capas de abajo. Primero las **entidades de dominio** (qué son, y la diferencia entre un modelo anémico y uno enriquecido); después los **repositorios**, los objetos de la capa de datos. Acá aparece la **primera de las cuatro reglas** de quién conoce a quién, y se cierra la tercera pregunta de M1: *¿quién guarda?*
**Qué NO cubre:** quién **llama** a los repositorios (M4), ni cómo se implementa un repositorio de verdad (M5, en memoria; con base de datos, segundo cuatrimestre).

## De dónde venís

- De **M1**: las tres capas (presentación, dominio, datos), la regla "la superior usa a la inferior, nunca al revés", y tres preguntas abiertas. La de este módulo: **¿quién guarda?**
- De **M2**: el acceso (`Usuario`) separado de las entidades (`Alumno`, `Docente`).
- De la **clase 03**: todo el dominio del servicio de ventas.
- De la **clase 01**: agregación y composición, y que la diferencia entre las dos habla del **ciclo de vida** de los objetos (la parte, ¿puede existir sin el todo?).
- De **Paradigmas**: interfaces, y qué significa que una clase implemente una.

---

## 1. La capa de dominio: entidades 🔴

### 1.1. Qué hay en esta capa

En la capa de dominio viven las **entidades de dominio**: en el ejemplo del sistema académico, `Calificacion`, `Alumno`, `Docente`, `Materia`. En el servicio de ventas, todo lo de la clase 03.

Si te preguntás "¿cuáles de las clases que conozco van acá?", la respuesta es **todas**: todo lo de Paradigmas y todo lo de la clase 03. Ninguna de esas clases pertenece a otra capa. Es lo que en M1 llamamos *darle sostén estructural al caso de uso*. Lo que vas a ver en este módulo y en el que sigue son **otros tipos de clases**, con otras responsabilidades, que hasta ahora no existían en tus diseños.

> ⚠️ "Clase de dominio" y "entidad de dominio" se usan como sinónimos: una clase que modela algo del negocio. (En M4 aparece otra parte de la capa de dominio, los services, que no son entidades.)

### 1.2. Con comportamiento o sin él 🟡

Las clases de esta capa **pueden tener comportamiento o no tenerlo**. Que una clase no tenga métodos no la descalifica: hay clases que existen porque hace falta tener esos datos modelados. En la clase 03, `TipoProducto` arrancó siendo solo un nombre; el comportamiento (`totalImpuestos`) llegó después. "No le encontré comportamiento, entonces no es clase" es un razonamiento equivocado.

### 1.3. Modelo anémico vs. modelo enriquecido 🟡

Hay dos enfoques para construir esta capa, y los dos existen en la industria:

| | **Modelo anémico** | **Modelo enriquecido** |
|---|---|---|
| Las entidades… | Son "moldes de clases": solo atributos, que representan estructuras de una base de datos | Tienen atributos **y** comportamiento |
| El comportamiento… | Se delega a otro lado | Vive en la entidad, donde están los datos |
| Ejemplo | `Producto` solo con atributos, sin métodos propios: el precio final se calcula afuera | `Producto.precioFinal()` y `Producto.totalImpuestos()`, como en la clase 03 |

**En esta materia se trabaja con modelo enriquecido**, igual que en Paradigmas: una capa de entidades fuerte en comportamiento. Siempre que encuentres comportamiento que le corresponde a un objeto, va en ese objeto.

> ⚠️ **Para el TP.** Son dos enfoques con ventajas y desventajas, y vas a cruzártelos en el mercado laboral. La Entrega 1 pide argumentar cuál conviene: empezá por la tabla de esta sección.

---

## 2. La capa de datos: repositorios 🔴

### 2.1. El caso: ¿dónde están todos los alumnos?

En M1 quedó dicho que la capa de datos responde preguntas como *"¿dónde obtengo todos los alumnos del sistema?"*. Pero en un diseño orientado a objetos, "la capa de datos" no puede ser una nube: tiene que estar hecha de **objetos**. Esos objetos se llaman **repositorios**.

Un **repositorio** es un objeto cuya responsabilidad es **manipular la persistencia** de un tipo de entidad: guardarla, recuperarla, modificarla, eliminarla. Para los alumnos, un `AlumnosRepository`:

```
┌───────────────────────────────────────┐
│     «interface» AlumnosRepository     │
├───────────────────────────────────────┤
│ + guardar(alumno)                     │
│ + modificar(alumno)                   │
│ + eliminar(alumno)                    │
│ + buscar(legajo): Alumno              │
│ + buscarTodos(): Alumno[]             │
│ + ...                                 │
└───────────────────┬───────────────────┘
                    ┊  usa / conoce
                    ▼
            ┌───────────────┐
            │    Alumno     │   (capa de dominio)
            ├───────────────┤
            │ ...           │
            └───────────────┘
```

El contrato típico de cualquier repositorio: **guardar**, **modificar**, **eliminar**, **buscar** (uno, por algún criterio) y **buscar todos**. Puede haber métodos más particulares ("buscar productos por nombre"). Esos cinco son la base, no una obligación: según la regla de negocio, una factura se da de alta y se consulta, pero no se modifica, así que su repositorio no necesita `modificar`.

Con un repositorio se habla así. En el servicio de ventas va a haber un repositorio de productos, y le vas a decir: *"tomá, guardame este producto"*, *"dame todos los productos"*, *"buscame el producto con este id"*.

Fijate que `Alumno` está en la capa de dominio y `AlumnosRepository` en la capa de datos. Dos clases, dos capas, dos responsabilidades.

### 2.2. Qué hace un repositorio por dentro

Depende de **dónde** persiste. Si el repositorio guarda en una base de datos relacional:

| Le pedís al repositorio | Se traduce en |
|---|---|
| `guardar(alumno)` | Una sentencia `INSERT` |
| `eliminar(alumno)` (o por legajo) | Una sentencia `DELETE` |

…y del mismo modo, `modificar` y `buscar` se traducen en un `UPDATE` y un `SELECT`.

Si persiste en otra cosa (un archivo JSON, un servicio externo, una lista en memoria como vas a ver en M5), hace otras acciones. Lo que no cambia es la responsabilidad: **el repositorio conoce a la entidad y la traduce** a la estructura con la que trabaja el medio persistente. Es lo que quedó dicho en M1 sobre `Calificacion`: la capa de datos la conoce y la traduce, pero no le pertenece.

> 🕳️ **Madriguera — Qué es una sentencia SQL**
> `INSERT`, `UPDATE`, `DELETE` y `SELECT` son las instrucciones con las que se le habla a una base de datos relacional. La persistencia en base de datos se ve en el segundo cuatrimestre.
> *Volvé al camino.*

### 2.3. La primera regla

> **Regla 1 (para anotar): los repositorios conocen a las entidades de dominio. Las entidades de dominio jamás conocen a los repositorios.**

```
   CAPA DE DATOS                     CAPA DE DOMINIO
 ┌─────────────────────┐          ┌─────────────────────┐
 │  AlumnosRepository  │ ───────► │       Alumno        │
 └─────────────────────┘ conoce   └─────────────────────┘
                                     no sabe que existe
                                     ningún repositorio
```

¿Por qué? **No es por seguridad.** Es por **acoplamiento y mantenibilidad**: si las entidades conocieran a los repositorios, habría doble acoplamiento entre las dos capas, que es lo que el modelo en capas quiere evitar (M1). Las entidades son **agnósticas** de la persistencia: viven, tienen su comportamiento, y no tienen idea de que alguien las está guardando. Los repositorios, en cambio, sí saben de ellas: necesitan conocerlas para poder traducirlas.

¿Hay alguna situación en la que una entidad tenga que conocer a un repositorio? **No. Nunca.**

⚠️ *¿Esto no contradice la regla de M1 (la capa de arriba usa a la de abajo)?* No. Que el repositorio "conozca" a `Alumno` quiere decir que necesita ese tipo para poder guardarlo y traducirlo. Quien **llama** al repositorio sigue estando arriba: es otra parte de la capa de dominio, que aparece en M4. Lo que no pasa nunca es que una **entidad** llame a un repositorio.

Y eso abre la pregunta inmediata. Si `Alumno` no conoce a `AlumnosRepository`, ¿quién le dice al repositorio "guardá este alumno"? Ese alguien todavía no apareció: es la pieza central de M4.

> **Para el parcial, si te preguntan:** *¿Puede una entidad de dominio llamar a un repositorio para guardarse a sí misma?*
> No. Los repositorios conocen a las entidades de dominio, pero las entidades nunca conocen a los repositorios: si lo hicieran, habría doble acoplamiento entre la capa de dominio y la de datos. Las entidades son agnósticas de la persistencia; quien llama al repositorio es otra clase, que orquesta el caso de uso.

### 2.4. ¿Por qué el repositorio es una interfaz?

En el diagrama, `AlumnosRepository` es una **interfaz**. No es casualidad: **entre capas, las clases se conocen a través de contratos** (interfaces), no de implementaciones concretas. Es la ventaja de M1 hecha código: *esconder el detalle de cómo se llevan a cabo los servicios que expone* y poder *reemplazar la implementación sin que el consumidor se entere*.

El contrato del repositorio es la interfaz; la clase que lo implementa queda escondida detrás. Quien use el repositorio de alumnos va a conocer `AlumnosRepository` y nunca la clase que guarda de verdad. Hoy esa clase guarda en memoria; mañana, en una base de datos. Quien la usa no se entera del cambio. Lo vas a ver en código en M5.

> 🕳️ **Madriguera — Esto tiene nombre**
> Depender de una abstracción (la interfaz) y no de una implementación concreta es uno de los principios SOLID. Los cinco principios se ven en la clase 06.
> *Volvé al camino.*

---

## 3. ¿Un repositorio por cada entidad? 🔴

Mirá la lista de entidades del servicio de ventas: `Comercio`, `Producto`, `TipoProducto`, los impuestos (IVA, EO, EI), `Venta`, `ItemVenta`, los observadores. ¿Hace falta un repositorio para cada una?

**No.** Por dos razones distintas.

### 3.1. Solo se persiste lo que tiene que durar

Hay entidades que **interesa guardar** porque tienen que sobrevivir en el tiempo (los productos, las ventas) y hay clases que **no**: clases **utilitarias**, que tienen comportamiento pero cuyos objetos no necesitan perdurar. Las utilitarias no se persisten. **No toda entidad de dominio tiene su repositorio.**

### 3.2. Guardado en cascada

La segunda razón es más sutil. Puede pasar que **sí** te interese guardar las calificaciones, y que igual **no** exista un `CalificacionesRepository`. ¿Cómo se guardan, entonces? **A través del alumno.** (Es una opción de diseño: tener un repositorio de calificaciones propio también es válido.)

```
   alumnosRepository.guardar(alumno)
                   │
                   ▼
   ┌──────────────────────────────┐
   │ Alumno                       │      se guarda el alumno…
   │  ├── Calificacion (7)        │      …y en cascada,
   │  ├── Calificacion (9)        │      todas sus calificaciones
   │  └── Calificacion (4)        │
   └──────────────────────────────┘
```

Eso se llama **guardado en cascada**: cuando se guarda el alumno, se guardan también sus calificaciones; cuando se actualiza el alumno, se actualizan también. Tiene sentido cuando una calificación **no puede existir sin su alumno**: depende de él para existir. Es un mecanismo muy usado; cómo se configura en detalle queda para cuando la persistencia sea en base de datos.

Esa frase, "no puede existir sin", te tiene que sonar. Es la diferencia entre **agregación y composición** de la clase 01: en una composición, la parte no existe sin el todo y comparten ciclo de vida. Cuando dibujás un rombo relleno entre `Alumno` y `Calificacion`, ya estás anticipando que probablemente se persistan juntos.

Aplicá el mismo criterio al servicio de ventas: un `ItemVenta` no tiene sentido sin su `Venta` (es el renglón de la factura, como Factura–ItemFactura en la clase 01: una composición). Por eso es candidato natural a guardarse en cascada con la venta. Coherente con eso, en el proyecto (M5) vas a encontrar **cuatro** repositorios: comercios, productos, tipos de producto y ventas. El resto de la lista no tiene el suyo.

> **Para el parcial, si te preguntan:** *¿Es necesario un repositorio por cada entidad de dominio?*
> No. Solo tienen repositorio las entidades que hay que persistir; las clases utilitarias no se persisten. Y aun entre las persistentes, una entidad que depende de otra para existir (una composición, como la calificación de un alumno) puede guardarse en cascada a través del repositorio de la entidad que la contiene, sin repositorio propio.

### 3.3. ¿Dos repositorios para la misma entidad? 🟢

Lo habitual es **una entidad, un repositorio**. Puede pasar que dos repositorios manipulen la misma entidad. Es raro y se justifica en casos puntuales, y hay que cuidar qué responsabilidad recibe cada uno: los repositorios siguen siendo clases, y para ellos siguen valiendo la alta cohesión y el bajo acoplamiento.

> 🕳️ **Madriguera — ¿Y `Usuario`?**
> El `Usuario` de M2 **puede vivir separado** de `Alumno` y `Docente` (el administrador es un usuario sin ninguno de los dos), así que lo natural es que tenga su propio repositorio. También podría guardarse en cascada a través de `Alumno` y `Docente`. La gestión de usuarios completa se ve en el segundo cuatrimestre.
> *Volvé al camino.*

---

## 4. Todos los repositorios se parecen 🟡

Si escribís el repositorio de productos, el de ventas, el de comercios y el de tipos de producto, vas a notar que son **casi idénticos**: los mismos métodos, cambiando el tipo de la entidad. La pregunta natural es *"¿no puedo hacer uno solo, genérico, que sirva para todos?"*.

**Se puede.** Java tiene **generics** (tipos parametrizados: una interfaz `Repositorio<T>` que funciona para cualquier `T`). Pero para el TP **la recomendación es no hacerlo**:

- complejiza el proyecto justo cuando todavía cuesta entender las capas;
- si en el equipo hay alguien que nunca vio generics, lo dejás afuera.

**Repetir el código de los repositorios no es un problema** en esta etapa. Si todo el equipo maneja generics con soltura, es decisión del equipo. En el segundo cuatrimestre, cuando la persistencia sea en base de datos, esto se va a simplificar bastante.

---

## 5. Lo que te llevás de este módulo

- **Capa de dominio = entidades.** Todo lo que conocés de Paradigmas y de la clase 03. Pueden tener comportamiento o no. En la materia: **modelo enriquecido**.
- **Capa de datos = repositorios.** Un repositorio manipula la persistencia de una entidad: guardar, modificar, eliminar, buscar, buscar todos. Traduce la entidad al medio persistente que use.
- **Regla 1:** los repositorios conocen a las entidades; **las entidades jamás conocen a los repositorios**. Por acoplamiento, no por seguridad.
- El contrato del repositorio es una **interfaz**: entre capas se conocen contratos.
- **No toda entidad tiene repositorio:** solo las persistentes, y algunas se guardan **en cascada** a través de otra (composición). En el servicio de ventas: cuatro repositorios.
- Repetir repositorios en el TP está bien; generics, solo si todo el equipo los maneja.
- Queda abierto: **¿quién llama al repositorio?**

---

## ✅ Checkpoint del Módulo 3

Respondé de memoria. Las respuestas van al complemento.

1. ¿Qué clases de las que conocías hasta hoy pertenecen a la capa de dominio? ¿Alguna no?
2. ¿Qué diferencia hay entre un modelo anémico y uno enriquecido? ¿Con cuál se trabaja en la materia? Da un ejemplo de modelo enriquecido de la clase 03.
3. ¿Qué es un repositorio y cuáles son sus métodos típicos? ¿En qué se traduce `guardar` si el repositorio persiste en una base relacional?
4. Enunciá la Regla 1. ¿Por qué no es una cuestión de seguridad? ¿Por qué no contradice la regla de M1 ("la capa superior usa a la inferior")?
5. ¿Por qué `AlumnosRepository` es una interfaz y no una clase?
6. ¿Cuáles son las dos razones por las que una entidad de dominio puede no tener repositorio propio?
7. Explicá el guardado en cascada con `Alumno` y `Calificacion`. ¿Qué tiene que ver con la composición de la clase 01? ¿Qué par de clases del servicio de ventas cumple el mismo criterio, y por qué?
8. ¿Por qué no se recomienda un repositorio genérico con generics en el TP?

---

## Qué viene después

**M4 — Las clases típicas (II): services y controllers. El mapa completo.** Aparece la pieza que faltaba: la clase que llama a los repositorios, instancia las entidades y orquesta el caso de uso. Con ella se cierran las dos preguntas de M1 que quedaban (quién instancia, quién verifica permisos) y la que deja este módulo (quién llama al repositorio). Aparecen las reglas 2, 3 y 4, y armamos el mapa completo de quién conoce a quién.

**FIN DEL MÓDULO 3**
