# 🧩 Apunte Maestro — Clase 03, Parte 1: Diseño del dominio del *Servicio de Ventas* (SmartLife)

**Unidad:** `clase03` — Modelado de Dominio en Objetos · Arquitectura de Software (08/04/2026)
**Parte 1 de 3.** Acá va: el caso, qué significa *diseñar* en esta materia, el modelado de **Comercio** y todo el rompecabezas de **cómo modelar Producto** (las cuatro alternativas con sus trade-offs) hasta la regla de oro de la clase. La **Parte 2** toma los **impuestos**, la implementación en código y los tests. La **Parte 3** cierra con **Venta** y el **patrón Observer**.

**Marcas de importancia:** 🔴 central y evaluable · 🟡 secundario · 🟢 mencionado al pasar. 🕳️ = madriguera (tangente que podés saltear sin culpa).

> **Qué se asume de antes** (se construye encima sin re-explicar): el paradigma de objetos y la **notación de diagrama de clases** (preclase 01); **atributos de calidad, cualidades de diseño y el concepto/clasificación de patrones** (preclase 03); la orientación a **servicios** y el modelo cliente-servidor (clase 02); y los **componentes** de un sistema — frontend, backend, base de datos — con sus diagramas (clase 01).

---

## 1. El caso: SmartLife y el Servicio de Ventas 🔴

Antes de tirar una sola clase, el dominio.

**SmartLife** es una plataforma **orientada a servicios** que gestiona distintos aspectos de una ciudad inteligente: gestión de ventas e impuestos de comercios, análisis de tendencias de consumo, seguridad para hogares conectados y un registro transversal de errores. Cada uno de esos aspectos es un **servicio** autónomo. Este año, todo lo que modelemos en la práctica cuelga de SmartLife: es el **ejercicio integrador** que acompaña la cursada (y espeja la estructura del TPA — servicios independientes, un diagrama de clases por servicio).

Hoy trabajamos **un solo** servicio: el **Servicio de Ventas** (*Sales Service*). Y de ese servicio, **solo la capa de dominio** — nada de API REST (eso es la clase que viene) ni de publicar eventos (eso, varias clases más adelante).

> 🕳️ **Madriguera — API REST y eventos del enunciado**
> El enunciado del Servicio de Ventas también pide exponer una API REST (`POST /sales-service/ventas`, etc.) y publicar un evento `VentaRegistrada` en un broker. Nada de eso entra hoy: la clase se queda en el **modelo de objetos**. La API REST se ve la próxima clase; los eventos, cuando lleguemos al broker.
> *Volvé al camino.*

### 1.1. El dominio, en sus reglas

**Contexto.** Los comercios adheridos registran sus ventas a través de la plataforma. Cada venta debe **calcular impuestos** y **notificar a distintos interesados**.

**Reglas de negocio de impuestos.** Hoy los productos se clasifican en **Electrónicos** y de **Hogar**. A **todos** se les aplica **IVA**; además, a los electrónicos se les suma el **EO** y a los de hogar el **EI** (EO y EI son impuestos ficticios inventados para el ejercicio). El cálculo:

| Impuesto | Fórmula | Sobre un precio base de 100 |
|---|---|---|
| **IVA** | `0.21 · precioBase` | 21 |
| **EO** | `0.5 · precioBase + 4 · GananciasEO` (GananciasEO = 4) | 50 + 16 = 66 |
| **EI** | `precioBase / 4 + 0.3 · GananciasEI` (GananciasEI = 3.5) | 25 + 1.05 = 26.05 |

**Precio final** = `precioBase + suma(impuestos aplicados)`. Dos avisos del enunciado que van a pesar en cada decisión de diseño: los **porcentajes y ganancias pueden cambiar en el futuro**, y **pueden agregarse nuevos impuestos por tipo de producto**.

**Casos de uso mínimos.** Gestionar productos (alta, baja, modificación, listado), **registrar ventas** (una venta involucra uno o varios productos) y consultar precios finales.

**Observadores de la venta.** Cuando se registra una venta hay que avisarle a dos interesados: **SFFA** (Servicio de Facturación Ficticia Argentina — nos da una **SDK** para notificarle) y **SVIBAA** (Servicio de Verificación de Impuestos Bien Aplicados — expondrá una **API REST**, todavía en desarrollo). Esto lo modelamos entero en la Parte 3.

> 💡 **Término — SDK** (*Software Development Kit*): un "kit" que un servicio externo te entrega **como código** (una biblioteca con clases y métodos ya listos) para integrarte con él sin implementar la comunicación a mano. Ej.: la SDK de Mercado Pago. Lo profundizamos en la Parte 3.

---

## 2. Qué significa *diseñar* en esta materia 🔴

Esto es lo más importante que te llevás de la clase, más que cualquier clase concreta.

**Diseñar es plantear varias alternativas, evaluar sus ventajas y desventajas, y quedarse con una — justificando por qué.** No hay "la respuesta". Hay decisiones, cada una con lo que **ganás** y lo que **pagás**. Todo el resto de esta parte es exactamente eso: vamos a modelar Producto de cuatro formas distintas, ver qué duele en cada una, y sesgar hacia la que menos duele.

> **Cómo se evalúa esto (calibración para el parcial).** En esta materia **no** se toma definición de memoria. Se evalúa que **decidas y justifiques**: *"pongo esto acá **porque** priorizo [tal cualidad], y estoy pagando con [tal otra]"*. Cuando en un parcial menciones un atributo de calidad, **definilo vos** en tu respuesta: te evalúan tu justificación contra la definición que diste, no contra una definición "oficial". Por eso, en este apunte, cada alternativa viene con su **por qué**, no con una regla suelta.

**Las herramientas de hoy** 🟢: para dibujar el diagrama de clases se usa **PlantUML** (un plugin que dibuja el diagrama a partir de un pseudocódigo de texto), dentro de un proyecto **Spring Boot** abierto en IntelliJ. La herramienta de diagramado es lo de menos —

> 🕳️ **Madriguera — herramientas de diagramas**
> PlantUML es una opción; también existen Star UML, Enterprise Architect, LucidChart y otras. El diagrama es el mismo; elegí la que te resulte cómoda.
> *Volvé al camino.*

---

## 3. Por dónde empezar: la entidad Comercio 🔴

Cuando arrancás un modelo, **¿por dónde?** Acá conviene empezar por **Comercio**, y el criterio es de **jerarquía / pertenencia**:

- La **venta** ocurre en un comercio (alguien la registra: el comercio).
- El **producto** lo vende alguien: el comercio.

Como todo lo demás cuelga de él, arrancamos por ahí. (Ojo: esto **no** es una regla universal — a veces conviene empezar por lo más complejo, o por lo que en un parcial te lleva más tiempo. Acá, por orden, empezamos por Comercio.)

### 3.1. Modelar *lo mínimo necesario* 🔴

Acá aparece el criterio más importante de esta entidad, y es puro **enfoque orientado a servicios**:

El enunciado **no** tiene un caso de uso "gestionar comercios" (dar de alta, modificar comercios). Se asume que los comercios **ya existen**. Es más: probablemente **otro servicio** (uno de gestión de comercios) sea el dueño de esos datos, y este Servicio de Ventas solo necesite **identificar** al comercio para colgarle sus productos y ventas.

**Conclusión de diseño:** del Comercio modelamos **lo mínimo necesario** — un `id` (identificador único). Ni razón social, ni CUIT, ni dirección: si algún día los necesitáramos, se los pediríamos al servicio dueño usando ese `id`. Esto es aplicar **YAGNI** ("You Aren't Gonna Need It": no agregues lo que todavía no necesitás) llevado al recorte por límites de servicio.

> **Para el parcial, si te preguntan** — *"¿Por qué el Comercio solo tiene un `id` y no sus datos completos?"*
> Porque en una arquitectura orientada a servicios cada servicio es dueño de su dominio. El Servicio de Ventas no gestiona comercios (no hay caso de uso para eso); solo necesita **referenciarlos** para asociarles productos y ventas. Con el `id` alcanza para pedirle el resto a quien sí los administre. Modelar de más acoplaría este servicio a datos que no le pertenecen.

### 3.2. El código de Comercio (por ahora)

Comercio va a **crecer**: en la Parte 3 le sumamos las ventas y los observadores. Por ahora, lo mínimo para sostener productos:

```java
// Comercio: la entidad dueña de los productos (y, más adelante, de las ventas).
@Getter                                   // Lombok escribe los getters por vos en tiempo de compilación
                                          //   (visto en el seminario de Java): no los tipeás a mano.
public class Comercio {

    private long id;                      // identificador único del comercio.
                                          //   Es TODO lo que modelamos de él: lo demás vive en otro servicio.
    private List<Producto> productos;     // la colección de productos que este comercio vende.
                                          //   (ventas y observadores se agregan en la Parte 3)

    public Comercio(long id) {            // constructor: recibe el id...
        this.id = id;                     //   ...lo guarda...
        this.productos = new ArrayList<>(); // ...e inicializa la colección VACÍA acá adentro.
    }                                     //   👈 la colección la crea la clase, no le llega de afuera (ver nota).

    // Alta de productos con "parámetros variables" (varargs): puedo llamarlo con 1, 2 o N productos.
    public void agregarProducto(Producto... productos) {   // "Producto..." = cero o más Producto
        Collections.addAll(this.productos, productos);      // agrega TODOS los recibidos a mi colección
    }
}
```

**🔴 Regla de diseño escondida en el constructor.** Cuando una clase tiene una **colección**, esa colección **se inicializa adentro de la clase** (en el constructor), no le llega por parámetro ni por un setter desde afuera. La clase es **responsable de mantener la consistencia** de su propia colección: nadie de afuera le enchufa una lista ya armada. Por eso `productos = new ArrayList<>()` vive en el constructor, y las altas pasan **siempre** por `agregarProducto(...)`.

> 💡 **Término — varargs** (`Producto...`): "parámetros variables". Te deja llamar al método con cualquier cantidad de argumentos de ese tipo: `agregarProducto(tv)`, `agregarProducto(tv, cafetera)`, etc. Adentro se recibe como un arreglo. (Los tres puntitos van en la **firma** del método, no al llamarlo.)

> 🕳️ **Madriguera — relación uni o bidireccional**
> ¿El Comercio conoce a sus Productos, o el Producto conoce a su Comercio? A nivel de objetos **da igual** para este ejercicio: las relaciones pueden ser unidireccionales o bidireccionales. Acá elegimos que el Comercio conozca a sus productos por simplicidad y porque hay una regla de negocio (Parte 3) que lo aprovecha.
> *Volvé al camino.*

---

## 4. El corazón de la clase: cómo modelar Producto 🔴

Acá está el rompecabezas. Léelo entero: si entendés **este** razonamiento, no vas a cometer los errores clásicos en los parciales.

**Qué sabemos del producto** (del enunciado): todo producto tiene un **precio base** y, como mínimo, un **nombre/descripción**. No agregamos SKU ni stock: no aparecen en el enunciado y no los necesitamos para registrar la venta (lo mínimo necesario, otra vez).

**Qué tiene que saber hacer** un producto: calcular su **precio final** (`precioFinal`) y el **total de sus impuestos** (`totalDeImpuestos`). Ese es su comportamiento; lo vamos a necesitar para saber cuánto sale una venta.

La pregunta que dispara todo: los productos vienen en dos "tipos" (electrónico, hogar) y **los impuestos dependen del tipo**. ¿Cómo modelamos esa variación? Vamos por partes.

### 4.1. Alternativa 1 — clase abstracta `Producto` + subclases 🟡

La primera idea intuitiva: una superclase abstracta y una subclase por tipo.

```
        ┌──────────────────────┐
        │   «abstract»         │
        │   Producto           │
        │  - precioBase: double│
        │  - descripcion: String│
        └──────────┬───────────┘
                   │ (herencia: "es un")
        ┌──────────┴──────────┐
        │                     │
┌───────────────┐     ┌───────────────┐
│  Electronico  │     │     Hogar     │
└───────────────┘     └───────────────┘
```

La idea sería: el IVA (que todos pagan) lo calculás en la clase padre, y cada hija **sobreescribe** `totalDeImpuestos` para sumarle lo suyo (EO en Electrónico, EI en Hogar).

**Qué duele.** Hacé las dos preguntas de estrés que vamos a repetir en cada alternativa:

1. **¿Qué pasa si surge un tipo nuevo?** (por ejemplo, "Jardinería.") → Tenés que **escribir una clase nueva**. No podés dar de alta un tipo en tiempo de ejecución: es código.
2. **¿Qué pasa si a un tipo hay que aplicarle EO *y* EI?** → Terminás **repitiendo lógica** que ya vive en clases "hermanas".

Y la pregunta de fondo, la más importante: **¿es realmente responsabilidad del Producto saber calcular los impuestos?** Si metés todo el cálculo de impuestos en la jerarquía de producto, la clase termina con **baja cohesión** (hace demasiado) y baja mantenibilidad.

> ⚠️ Esta alternativa **compila y anda**. No está "prohibida": simplemente tiene muchas desventajas y nos conviene otra. Diseñar es elegir, no es que las demás sean imposibles.

### 4.2. Alternativa 2 — interfaz `Producto` + clases concretas 🟡

Segundo intento: en vez de clase abstracta, una **interfaz** `Producto` que implementan `Hogar` y `Electronico`.

```
        ┌──────────────────────┐
        │   «interface»        │
        │   Producto           │
        └──────────┬───────────┘
                   ┆ (implementa / realización)
        ┌──────────┴──────────┐
        │                     │
┌───────────────┐     ┌───────────────┐
│  Electronico  │     │     Hogar     │
│  - precioBase │     │  - precioBase │  👈 atributos repetidos en cada una
└───────────────┘     └───────────────┘
```

Suena a "mejora", pero es **peor** que la 1, y por una razón concreta:

- Una **interfaz no es una clase**: en POO puro **no lleva comportamiento** (*) ni **atributos**. Entonces el cálculo del IVA, que en la Alt. 1 vivía una sola vez en la clase padre, ahora **hay que repetirlo** en cada clase concreta. Repetición de lógica.
- Peor todavía: como la interfaz no tiene atributos, tenés que **repetir la escritura de `precioBase`, `descripcion`, etc. en cada clase concreta**.

Las dos preguntas de estrés dan lo mismo o peor (tipo nuevo → clase nueva con todo repetido; EO+EI → lógica duplicada entre hermanas). Y reaparece la pregunta de fondo: *¿es responsabilidad del producto calcular impuestos?*

> **(\*) Ojo:** algunos lenguajes permiten métodos con cuerpo en interfaces (los *default methods* de Java). Pero si nos ponemos rigurosos con el paradigma de objetos, **una interfaz no debería llevar comportamiento**: describe *qué* se puede hacer, no *cómo*.

**Conclusión:** la Alt. 2 es un poco peor que la 1. Las dos comparten el mismo pecado — meter el cálculo de impuestos dentro de la jerarquía de producto.

### 4.3. Alternativa 3 (la elegida) — `Producto` concreta + `TipoProducto` concreta 🔴

El salto conceptual que resuelve todo: **separar el producto de su tipo.**

Una cosa es el **producto** concreto ("Smart TV 50, marca X, $100") y otra es su **tipo** ("Electrónico" / "Hogar"). Son **dos conceptos distintos**. Y lo clave del enunciado: **los impuestos no varían por producto, varían por tipo**. Entonces el tipo se merece ser una entidad con nombre propio.

```
┌──────────────┐  *  ┌────────────────────┐     ┌──────────────────┐
│  Comercio    │────▶│     Producto       │────▶│  TipoProducto    │
│  - id        │     │  - id              │     │  - descripcion   │
└──────────────┘     │  - descripcion     │     │  - impuestos: *  │──▶ (Impuesto: Parte 2)
                     │  - precioBase      │     │                  │
                     │  - tipo ───────────┼────▶│                  │
                     │  + precioFinal()   │     │ + totalImpuestos(p)
                     │  + totalImpuestos()│     └──────────────────┘
                     └────────────────────┘
```

Lo importante: **"Electrónico" y "Hogar" dejan de ser clases y pasan a ser INSTANCIAS** de `TipoProducto`. Cada instancia de tipo lleva su nombre y la **colección de impuestos** que le aplican.

**Por qué esta gana — la palabra clave es *flexible / extensible*:**

Hacé la pregunta de estrés: *¿qué pasa si surge un tipo nuevo (Jardinería)?* → **No escribís código**. Creás **una instancia nueva** de `TipoProducto` y le asociás sus impuestos. Y como es solo instanciar, eso se puede hacer **en tiempo de ejecución** — por ejemplo, desde un formulario en una interfaz gráfica, donde un administrador da de alta el tipo y tilda los impuestos que le aplican. En las Alt. 1 y 2 eso era imposible: siempre terminabas tocando código.

**El código de esta decisión.** `Producto` queda como clase **concreta** (sin herencia), y **delega** el cálculo de impuestos a su tipo:

```java
// Producto: clase concreta. NO hereda de nada. Delega los impuestos a su tipo.
@Getter @Setter                    // Lombok: getters y setters automáticos
@NoArgsConstructor                 // Lombok: constructor vacío
@AllArgsConstructor                // Lombok: constructor con TODOS los atributos (lo usamos en los tests, Parte 2)
public class Producto {

    private Long id;               // identificador del producto
    private TipoProducto tipo;     // 👈 el producto TIENE un tipo (composición), no ES un tipo (herencia)
    private double precioBase;     // el precio sin impuestos
    private String descripcion;    // nombre/descripción; lo mínimo necesario

    // Total de impuestos del producto: NO lo calcula el producto, lo delega a su tipo.
    public double totalImpuestos() {
        return this.tipo.totalImpuestos(this);   // "tipo, calculame MIS impuestos" (me paso a mí mismo)
    }

    // Precio final = base + impuestos. Se apoya en el método de arriba.
    public double precioFinal() {
        return this.precioBase + this.totalImpuestos();
    }
}
```

```java
// TipoProducto: clase concreta. "Electrónico" y "Hogar" son INSTANCIAS de esto.
@Getter
public class TipoProducto {

    @Setter
    private String descripcion;                 // el nombre del tipo ("Electrónico", "Hogar", "Jardinería"...)
    private final List<Impuesto> impuestos;     // los impuestos que aplican a este tipo
                                                //   (cómo se diseña Impuesto: Parte 2)

    public TipoProducto(String descripcion) {   // se crea con su nombre...
        this.descripcion = descripcion;
        this.impuestos = new ArrayList<>();     // ...y su colección de impuestos arranca vacía (misma regla que Comercio)
    }

    // Alta de impuestos con varargs: puedo asociarle uno o varios de una.
    public void agregarImpuestos(Impuesto... impuestosNuevos) {
        Collections.addAll(this.impuestos, impuestosNuevos);
    }

    // Recorre sus impuestos, calcula cada uno sobre el producto, y suma.
    public double totalImpuestos(Producto producto) {
        return this.impuestos.stream()                    // recorro la colección de impuestos...
                .mapToDouble(i -> i.calcular(producto))    // ...cada impuesto sabe calcular su monto (Parte 2)...
                .sum();                                    // ...y sumo todo.
    }
}
```

```
// ¿CÓMO FUNCIONA? (el flujo del cálculo, sin ver todavía el diseño de Impuesto)
//  producto.precioFinal()
//        └─▶ precioBase + producto.totalImpuestos()
//                              └─▶ tipo.totalImpuestos(producto)
//                                      └─▶ por cada impuesto del tipo: impuesto.calcular(producto)  →  suma
//  Resultado esperado: para un electrónico de precioBase 100  →  100 + (IVA 21 + EO 66) = 187.
//  (El "187" es justamente el número que vamos a verificar en los tests de la Parte 2.)
```

Fijate la **cadena de delegación**: `Producto` no sabe *cómo* se calcula un impuesto; le pregunta a su `TipoProducto`, que a su vez le pregunta a **cada impuesto**. Cada uno hace lo suyo. Eso es **repartir responsabilidades**: el producto no carga con la lógica de impuestos.

> **Para el parcial, si te preguntan** — *"¿Por qué `TipoProducto` es una clase y `Hogar`/`Electrónico` son instancias, en vez de subclases?"*
> Porque los tipos **no tienen comportamiento distinto** entre sí: solo cambian qué impuestos llevan (dato), no *cómo* hacen algo. Modelarlos como instancias hace el diseño **extensible en tiempo de ejecución** — un tipo nuevo es una instancia nueva (dable de alta desde una UI), sin tocar ni recompilar código. Con subclases, cada tipo nuevo obliga a escribir y compilar una clase.

### 4.4. Alternativa 4 (descartada) — `TipoProducto` como *enum* 🟡

Una variante tentadora: dejar `Producto` concreto pero modelar el tipo como un **enum** (`ELECTRONICO`, `HOGAR`).

**Por qué se descarta:**

- Un **enum** es un conjunto de valores **fijos y finitos**, **sin comportamiento**. Para agregar un tipo nuevo tenés que **tocar código** sí o sí (agregar el valor y recompilar). Adiós flexibilidad en tiempo de ejecución.
- No podés **asociar directamente** los impuestos al tipo: tendrías el mapeo "este enum → estos impuestos" **hardcodeado** en algún lado. Cro­ta.

> 💡 **Término — enum** (enumerado): un tipo con un conjunto **cerrado** de valores predefinidos. Sirve para cosas que **no cambian** (los días de la semana, los palos de la baraja). Justamente por eso **no** sirve acá: el enunciado dice que pueden aparecer tipos nuevos.

### 4.5. 🔴 La regla de oro: **heredar por tipificación es un error grave**

Todo lo anterior se condensa en una regla que vale para toda la cursada:

> **🔴 Heredar por tipificación es un error grave. Se hereda SOLO si hay comportamiento distinto — no por atributos compartidos ni por estructura.**

**Qué es "tipificar".** Es partir en subclases solo porque el dominio nombra "tipos". Ejemplo: el enunciado dice "hay televisores, tablets y celulares" y vos hacés `Producto` abstracta con `Televisor`, `Tablet`, `Celular` como hijas. Eso es **herencia por tipificación**, y está mal **si esas subclases no hacen nada distinto** entre sí.

**El criterio real, en una línea:** herencia ⇔ **comportamiento distinto** (código distinto en algún método). No alcanza con "comparten atributos" ni con "son tipos del mismo palo".

**Aplicado a este ejercicio:**

- **Producto NO hereda.** Todos los productos calculan su precio final igual (base + impuestos, delegando). No hay comportamiento distinto → nada de subclases por tipo. El "tipo" se modela como **entidad aparte** (`TipoProducto`), no como jerarquía.
- **Impuesto SÍ va a justificar clases separadas** (Parte 2), porque **cada impuesto se calcula de forma distinta**: el IVA no se calcula como el EO ni como el EI. Ahí sí hay **código distinto** → ahí sí tiene sentido separar.

```
   ❌ SIN la regla (herencia por tipificación)        ✅ CON la regla
   ─────────────────────────────────────────         ─────────────────────────────────
   Producto (abstract)                                Producto (concreta) ── tipo ──▶ TipoProducto (instancias)
     ├── Electronico   (no hace nada distinto)        · un tipo nuevo = una instancia, en runtime
     └── Hogar         (no hace nada distinto)         · sin tocar código
   · tipo nuevo = clase nueva (código + recompilar)   · Impuesto SÍ se separa: cada uno calcula distinto
   · repetición de lógica entre "hermanas"
```

> **Para el parcial, si te preguntan** — *"Te dan un dominio con 'categorías' o 'tipos'. ¿Modelás con herencia?"*
> Primero preguntá: **¿hay comportamiento distinto entre esos tipos?** Si **sí** (cada uno hace algo de forma distinta), la herencia (o una interfaz con implementaciones) se justifica. Si **no** (solo cambian datos/atributos, o es pura tipificación), **no heredés**: modelá el tipo como una **entidad/instancia aparte**. Heredar por tipificación acopla, obliga a tocar código para cada tipo nuevo y suele arrastrar repetición de lógica. Justificá siempre con el criterio de comportamiento.

> 🕳️ **Madriguera — "esto se parece a un patrón"**
> Un compañero notó que "delegar el cálculo a otro objeto" se parece al patrón **Strategy**. La intuición es buena, pero los patrones de diseño con nombre (Strategy, State, etc.) los vemos aplicados más adelante en la cursada. Por ahora quedate con el **criterio** (comportamiento distinto ⇒ separar), no con el nombre.
> *Volvé al camino.*

---

## 5. El modelo hasta acá

Cerramos la Parte 1 con el mapa de lo modelado (los impuestos quedan como "caja por abrir" en la Parte 2):

```
┌──────────────┐   *productos    ┌────────────────────┐   tipo    ┌──────────────────┐
│  Comercio    │────────────────▶│     Producto       │──────────▶│   TipoProducto   │
│  - id: long  │                 │  - id              │           │  - descripcion   │
│              │                 │  - precioBase      │           │  - impuestos ────┼──▶ ❓ Impuesto
│ (+ ventas y  │                 │  - descripcion     │           │  + totalImpuestos(p)   (Parte 2)
│  observadores│                 │  - tipo            │           └──────────────────┘
│  en Parte 3) │                 │  + precioFinal()   │
└──────────────┘                 │  + totalImpuestos()│
                                 └────────────────────┘
```

> **🔴 Detalle de notación que se evalúa:** en el diagrama de clases, **herencia** ("es un") e **implementación de interfaz** ("cumple con") se dibujan con **flechas distintas** (ambas con punta triangular hueca, pero la de implementación va con línea **punteada**). No son intercambiables. Repasá las flechas del material de diagrama de clases (preclase 01): confundirlas en un parcial es un error clásico.

---

## Qué viene en la Parte 2

Abrimos la caja de los **impuestos**: por qué `Impuesto` termina siendo una **interfaz** (y no una clase abstracta ni una sola clase concreta), cómo se implementan `IVA`, `EO` y `EI`, y por qué acá **sí** corresponde separar en clases. Después, la **implementación en código** completa (packages, `stream`, delegación) y los **tests con JUnit** que verifican los cuatro casos obligatorios del enunciado — incluido el famoso **187**.

**FIN DE LA PARTE 1**
