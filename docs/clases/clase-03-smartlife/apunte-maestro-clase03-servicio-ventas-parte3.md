# 🧩 Apunte Maestro — Clase 03, Parte 3: Venta, patrón Observer y cierre

**Unidad:** `clase03` — Modelado de Dominio en Objetos · Arquitectura de Software (08/04/2026)
**Parte 3 de 3.** Cierre: modelamos la **Venta**, completamos el **Comercio**, llegamos al primer **patrón de diseño con nombre** (el **Observer**), vemos la pregunta abierta **paralelo vs. secuencial**, distinguimos **SDK / biblioteca / framework**, corremos el **test completo** y cerramos con el **checkpoint** de toda la clase.

**Marcas:** 🔴 central y evaluable · 🟡 secundario · 🟢 mencionado al pasar. 🕳️ = madriguera.

> **Dónde quedamos (Parte 2).** `Producto` delega en `TipoProducto`, que recorre su `List<Impuesto>` llamando a `impuesto.calcular(producto)`. `Impuesto` es una **interfaz**, con `IVA`/`EO`/`EI` concretas (comportamiento distinto ⇒ clases separadas). Ya probamos que un electrónico de base 100 da **187**. Falta: **registrar una venta** y **avisarle a los interesados**.

---

## 1. Modelar la Venta (y por qué aparece ItemVenta) 🔴

Del enunciado: una **venta involucra uno o varios productos**, y de cada producto puede venderse **más de una unidad** (2 Smart TVs iguales). Entonces necesitamos guardar, por producto, **cuántas unidades**.

Si la venta tuviera directamente una `List<Producto>`, **perderíamos la cantidad**. Por eso aparece una pieza intermedia: **`ItemVenta`**, que asocia **un producto con su cantidad**. La venta tiene, entonces, una lista de **ítems** (no de productos sueltos).

> 💡 **Analogía:** es lo mismo que en una **factura**: la factura no lista "productos", lista **renglones** (ítem), cada uno con producto, cantidad y subtotal. `Venta` es la factura; `ItemVenta` es el renglón.

**El código de ItemVenta** — producto + cantidad, y sabe calcular sus subtotales:

```java
@Getter                                       // Lombok: getters automáticos
public class ItemVenta {

    private final Producto producto;          // qué se vendió (final: un ítem no cambia de producto)
    private final int cantidad;               // cuántas unidades (final: tampoco cambia)

    public ItemVenta(Producto producto, int cantidad) {
        if (cantidad <= 0) {                                              // 👈 invariante de dominio:
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero"); // no existe un ítem de 0 unidades
        }
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public double subtotalPrecioBase() {      // base × cantidad
        return producto.getPrecioBase() * cantidad;
    }

    public double totalImpuestos() {          // impuestos del producto × cantidad  (delega en Producto, Parte 2)
        return producto.totalImpuestos() * cantidad;
    }

    public double totalFinal() {              // precio final del producto × cantidad
        return producto.precioFinal() * cantidad;
    }
}
```

**El código de Venta** — id, fecha, e items; sus totales **delegan en los ítems**:

```java
@Getter
public class Venta {

    private final long id;                    // id de la venta (final: no cambia una vez creada)
    private LocalDate fechaRegistro;          // cuándo se registró
    private List<ItemVenta> items;            // los renglones de la venta

    public Venta(long id) {
        this.id = id;
        this.fechaRegistro = LocalDate.now(); // la fecha se toma al crear la venta (el "ahora")
        this.items = new ArrayList<>();       // colección inicializada adentro (misma regla de siempre)
    }

    public void agregarItem(ItemVenta ... items) {   // varargs: uno o varios renglones
        Collections.addAll(this.items, items);
    }

    public double totalPrecioBase() {         // suma de subtotales base de cada ítem
        return this.items.stream().mapToDouble(ItemVenta::subtotalPrecioBase).sum();
    }

    public double totalImpuestos() {          // suma de impuestos de cada ítem
        return this.items.stream().mapToDouble(ItemVenta::totalImpuestos).sum();
    }

    public double totalFinal() {              // suma de finales de cada ítem
        return this.items.stream().mapToDouble(ItemVenta::totalFinal).sum();
    }
}
```

```
// ¿CÓMO FUNCIONA? la cadena de delegación de totales (nadie calcula de más)
//   venta.totalFinal()
//      └─▶ por cada ItemVenta:  item.totalFinal()
//                                  └─▶ producto.precioFinal() × cantidad
//                                          └─▶ (base + impuestos)   [cadena de la Parte 2]
//   Venta suma ítems · el ítem multiplica por cantidad · el producto sabe su precio final.
//   Cada objeto hace SU parte: responsabilidades repartidas de punta a punta.
```

> **Nota de fidelidad (repo vs. clase):** el cálculo del **total de la venta** quedó planteado en clase como **ejercicio** ("que retorne el total, resuélvanlo ustedes"). Así lo resuelve el repo: `Venta` suma sus ítems y el ítem multiplica por cantidad. La **validación `cantidad > 0`** de `ItemVenta` también es un agregado razonable del repo (invariante de dominio) que no se caminó en vivo; lo incluyo marcado.

---

## 2. Completar el Comercio 🔴

Ahora `Comercio` recibe lo que le faltaba: su lista de **ventas**, su lista de **observadores** (sección 3) y el método clave **`agregarVenta`**.

```java
@Getter
public class Comercio {

    private long id;
    private List<Producto> productos;
    private List<Venta> ventas;                       // ahora sí, la venta tiene tipo (Venta)
    private List<ObservadorVenta> observadores;       // los interesados a notificar (sección 3)

    public Comercio(long id) {
        this.id = id;
        this.productos = new ArrayList<>();
        this.observadores = new ArrayList<>();
        this.ventas = new ArrayList<>();              // las tres colecciones se inicializan acá adentro
    }

    public void agregarProducto(Producto ... productos) {
        Collections.addAll(this.productos, productos);
    }

    public void agregarObservadores(ObservadorVenta ... observadores) {
        Collections.addAll(this.observadores, observadores);
    }

    // El método central: registra una venta.
    public void agregarVenta(Venta venta) {
        // 1) Regla de negocio: no puedo vender lo que no es mío.
        if (!sonTodosProductosPropios(venta.getItems().stream().map(ItemVenta::getProducto).toList())) {
            throw new IllegalArgumentException("El comercio no puede registrar ventas de productos que no vende");
        }
        // 2) Guardo la venta.
        this.ventas.add(venta);
        // 3) 🔴 Notifico a TODOS los observadores interesados (acá vive el patrón Observer):
        this.observadores.forEach(o -> o.serNotificadoDe(venta));
    }

    public void eliminarObservador(ObservadorVenta observador) {
        this.observadores.remove(observador);
    }

    // ¿Los productos de la venta pertenecen todos a este comercio?
    private boolean sonTodosProductosPropios(List<Producto> productos) {
        return this.productos.containsAll(productos);   // containsAll: ¿mi colección contiene a todos estos?
    }
}
```

> **Nota de fidelidad (repo vs. clase):** la **validación de productos propios** (`sonTodosProductosPropios`) y `eliminarObservador` son agregados del repo, no caminados en vivo. En cambio, la parte 3 de `agregarVenta` — **notificar a los observadores** — es el **corazón** de la clase y lo que sigue.

---

## 3. El patrón Observer 🔴

Este es el primer **patrón de diseño con nombre** de la materia. Prestale atención: va a volver.

**El problema.** Cuando se registra una venta hay que avisarle a dos interesados: **SFFA** (facturación) y **SVIBAA** (verificación de impuestos). Y el enunciado avisa que **pueden aparecer más** interesados. ¿Cómo lo modelamos?

### 3.1. Alternativa 1 — el Comercio conoce a cada servicio 🟡

`Comercio` tiene un campo por cada interesado (`servicioFacturacion`, `servicioVerificacion`) y en `agregarVenta` los llama por nombre, uno por uno.

**Qué duele:**
- **Alto acoplamiento:** `Comercio` depende de las clases concretas de cada servicio.
- **Mala extensibilidad:** un interesado nuevo = **tocar `Comercio`** (agregar campo + agregar llamada). Cada vez.

### 3.2. Alternativa 2 (la elegida) — una interfaz intermedia 🔴

Metemos una **interfaz** en el medio: `ObservadorVenta`, con un método `serNotificadoDe(Venta)`. Los interesados (`ObservadorSffa`, `ObservadorSvibaa`) la **implementan**. `Comercio` guarda una `List<ObservadorVenta>` y, al registrar una venta, le avisa a **cada uno** — **sin saber quién es cada uno**, solo que cumplen el contrato.

```java
// El contrato: "algo a lo que se le puede avisar de una venta".
public interface ObservadorVenta {
    void serNotificadoDe(Venta venta);
}
```

```java
// SFFA: se integrará por SDK. Por ahora, solo cuenta que fue notificado.
@Getter
@RequiredArgsConstructor                       // Lombok: constructor con los campos obligatorios (acá, ninguno)
public class ObservadorSffa implements ObservadorVenta {

    private long cantLlamadas;                 // cuántas veces me notificaron (andamiaje para testear)

    @Override
    public void serNotificadoDe(Venta venta) {
        // TODO: integración real con la SDK de facturación (pendiente)
        cantLlamadas++;                        // por ahora, registro que me llamaron
    }
}
```

```java
// SVIBAA: se integrará por API REST (todavía en desarrollo). Misma forma.
@Getter
@RequiredArgsConstructor
public class ObservadorSvibaa implements ObservadorVenta {

    private long cantLlamadas = 0;

    @Override
    public void serNotificadoDe(Venta venta) {
        // TODO: integración real por REST (pendiente)
        cantLlamadas++;
    }
}
```

```
// ¿CÓMO FUNCIONA? el flujo del Observer
//   comercio.agregarVenta(venta)
//       └─▶ observadores.forEach(o -> o.serNotificadoDe(venta))
//               ├─ observadorSffa.serNotificadoDe(venta)    → cantLlamadas = 1
//               └─ observadorSvibaa.serNotificadoDe(venta)  → cantLlamadas = 1
//   El Comercio recorre la lista y le avisa a cada observador. NO sabe si son Sffa o Svibaa:
//   solo sabe que son ObservadorVenta (cumplen el contrato). Por eso, sumar un tercero
//   NO toca Comercio: se crea la clase nueva y se la agrega a la lista.
```

```
 Diagrama:
                       ┌──────────────────────┐
                       │       Comercio       │
                       │  observadores: List<ObservadorVenta>
                       │  + agregarVenta(v) ──┼── notifica a cada uno ──┐
                       └──────────────────────┘                        │
                                                                       ▼
                                            ┌───────────────────────────────────┐
                                            │        «interface»                │
                                            │        ObservadorVenta            │
                                            │  + serNotificadoDe(Venta)         │
                                            └──────────────┬────────────────────┘
                                                           ┆ (implementan)
                                            ┌──────────────┴──────────────┐
                                      ┌───────────────┐          ┌────────────────┐
                                      │ ObservadorSffa│          │ObservadorSvibaa│
                                      │  (vía SDK)    │          │  (vía REST)    │
                                      └───────────────┘          └────────────────┘
```

**El patrón, en formato ficha:**

- 🎯 **Qué es:** un objeto (el "sujeto" observable — acá `Comercio`) mantiene una **lista de observadores** interesados y les **avisa automáticamente** cuando ocurre algo (una venta), sin conocer sus clases concretas.
- **Por qué lo usamos:** para **desacoplar**. El comercio no sabe (ni le importa) quiénes son SFFA/SVIBAA; solo que hay observadores a notificar. Gana **extensibilidad** (interesado nuevo sin tocar al sujeto) y **bajo acoplamiento**.
- **Dónde en ESTE código:** `Comercio.observadores` (la lista), `agregarVenta` (el `forEach` que notifica), `ObservadorVenta` (el contrato) y `ObservadorSffa`/`ObservadorSvibaa` (los concretos).
- **Analogía:** una **suscripción a un newsletter**. El emisor no conoce a cada suscriptor; publica y todos los suscriptos reciben. `agregarObservadores`/`eliminarObservador` son suscribirse/desuscribirse.
- **Comparativo:**
  - ❌ **Sin patrón:** `Comercio` con campos `servicioFacturacion`, `servicioVerificacion` y llamadas directas → cada interesado nuevo modifica `Comercio`.
  - ✅ **Con patrón:** `Comercio` conoce solo la interfaz `ObservadorVenta` → interesado nuevo = clase nueva + `agregarObservadores(...)`, sin tocar `Comercio`.

> **Para el parcial, si te preguntan** — *"¿Por qué el Comercio no conoce directamente a los servicios de facturación y verificación?"*
> Porque acoplarlo a las clases concretas lo haría frágil: cada interesado nuevo obligaría a modificar `Comercio`. Con el **patrón Observer**, `Comercio` depende solo de la **interfaz** `ObservadorVenta` y notifica a una lista genérica; los interesados se agregan/quitan sin tocarlo. Se prioriza **extensibilidad** y **bajo acoplamiento**.

> **Nota de fidelidad:** el contador `cantLlamadas` y los `TODO` son andamiaje del repo (para testear la notificación y marcar la integración real como pendiente). La integración de verdad — SFFA por **SDK**, SVIBAA por **REST** — todavía no está implementada.

### 3.3. La pregunta abierta: ¿paralelo o secuencial? 🟡🕳️

El `forEach` de `agregarVenta` notifica a los observadores **uno tras otro** (secuencial). Queda planteada — para pensar — si conviene hacerlo **en paralelo**. Los ejes de la decisión:

- **Secuencial (lo actual):** simple y con **orden garantizado**. Pero si un observador **tarda** o **falla**, afecta/bloquea a los que siguen.
- **Paralelo:** puede ser más **rápido** si los observadores son lentos e independientes. Pero **Java no es multihilo por defecto** (hay que orquestarlo explícitamente), **perdés el orden/prioridad** de las notificaciones, y aparecen problemas de **concurrencia**.
- Preguntas para decidir: ¿importa el **orden** en que se notifica? ¿Qué pasa si **uno falla** — se cancelan los demás o siguen? ¿Hay **prioridades** entre interesados?

> No hay una respuesta única acá: es una decisión de diseño con trade-offs, dejada para reflexionar. Quedate con **los ejes**, no con un veredicto.

---

## 4. SDK vs. biblioteca vs. framework 🟡

Apareció que SFFA nos da una **SDK**. Vale distinguir tres cosas que se confunden:

- **SDK** (*Software Development Kit*): un **kit** que un servicio externo te entrega **como código** — típicamente una biblioteca más herramientas/utilidades — para integrarte con él sin implementar la comunicación a mano. Ej.: la SDK de Mercado Pago; la de SFFA. Es "una suite de cosas lista para el desarrollador".
- **Biblioteca** vs. **framework**: la diferencia intuitiva es **quién llama a quién**. Una **biblioteca** la llamás **vos** (tenés el control del flujo, la usás cuando la necesitás). Un **framework** te **llama a vos** (él maneja el flujo y vos completás los huecos — como Spring Boot).

> 🕳️ **Madriguera — lecturas pendientes de esta clase**
> Quedaron para leer/profundizar: la **teoría del patrón Observer**, **mocking**, **inyección de dependencias** y la distinción **biblioteca vs. framework** en detalle. Son la puerta a las próximas clases; acá alcanza con la intuición de arriba.
> *Volvé al camino.*

---

## 5. Cabos sueltos capturados 🟢

Cosas que quedaron pendientes o anotadas para más adelante (útiles para no perder el hilo):

- 🕳️ **Guardar el precio en el `ItemVenta`.** Hoy el ítem le pide el precio al producto. Pero como en una factura, convendría **congelar** el precio del momento de la venta (si el producto cambia de precio después, la venta vieja no debería cambiar). Queda para resolver en otra clase.
- 🕳️ **El `id` del Comercio.** Se agregó "porque estamos orientados a servicios y se va a necesitar la semana que viene". Normalmente el consejo es **no** agregar lo que no se usa todavía; acá es una excepción justificada que se ve al conectar la **API REST** (próxima clase).
- **Herramienta de diagramas:** el diagrama de clases se puede escribir como texto con **PlantUML** (u otras). El diagrama importa, no la herramienta.

---

## 6. El modelo completo del Servicio de Ventas 🔴

```
                          ┌───────────────────────────┐
                          │         Comercio          │
                          │  - id                     │
        ┌─────────────────┤  productos: List<Producto>│
        │   *ventas        │  ventas:    List<Venta>   │────────────┐
        │                 │  observadores: List<ObservadorVenta>    │ *observadores
        ▼                 └─────────────┬─────────────┘            ▼
 ┌──────────────┐                       │ *productos      ┌─────────────────────┐
 │    Venta     │                       ▼                 │   «interface»       │
 │  - id        │  *items       ┌────────────────┐        │   ObservadorVenta   │
 │  - fecha     │──────────────▶│    Producto    │        │  + serNotificadoDe  │
 │  + totales() │               │  - precioBase  │        └──────────┬──────────┘
 └──────┬───────┘               │  + precioFinal │                   ┆
        │ *items                │  - tipo ───────┼──▶ TipoProducto ──▶ «interface» Impuesto
        ▼                       └────────────────┘        (IVA, EO, EI)   ObservadorSffa / Svibaa
 ┌──────────────┐                                                        (implementan)
 │  ItemVenta   │──▶ Producto
 │  - cantidad  │
 └──────────────┘
```

Tres relaciones clave del Comercio: **productos** (qué vende), **ventas** (qué registró) y **observadores** (a quién avisa). Y dos cadenas de delegación: la de **impuestos** (Producto → TipoProducto → Impuesto) y la de **totales** (Venta → ItemVenta → Producto).

---

## 7. El test completo 🔴

Ya con `Venta` y los observadores modelados, así queda el test que en la Parte 2 dejamos a medias. El `@BeforeEach` ahora arma también el comercio y los observadores:

```java
@BeforeEach
void setUp() {
    // (reseteo de parámetros static de impuestos — ver Parte 2)
    IVA.setPorcentaje(0.21); /* ...EO y EI... */

    tipoElectronico = new TipoProducto("Electrónico");
    tipoElectronico.agregarImpuestos(new IVA(), new EO());
    tipoHogar = new TipoProducto("Hogar");
    tipoHogar.agregarImpuestos(new IVA(), new EI());

    generadorIdSecuencial = new GeneradorIdSecuencial();   // genera ids 1, 2, 3... (agregado del repo)
    observadorSffa   = new ObservadorSffa();
    observadorSvibaa = new ObservadorSvibaa();

    comercio = new Comercio(generadorIdSecuencial.siguiente());
    comercio.agregarObservadores(observadorSvibaa, observadorSffa);   // suscribo a los interesados
}

@Test
void ventaUnProductoElectronico_verificaIvaMasEoYNotifica() {
    Producto tv = new Producto(null, tipoElectronico, 100, "Smart TV 50");

    Venta venta = new Venta(generadorIdSecuencial.siguiente());
    venta.agregarItem(new ItemVenta(tv, 1));

    comercio.agregarProducto(tv);        // el producto es del comercio (si no, agregarVenta tiraría excepción)
    comercio.agregarVenta(venta);        // registra la venta Y notifica a los observadores

    assertEquals(100,    venta.totalPrecioBase(), 1);   // base
    assertEquals(87,     venta.totalImpuestos(),  1);   // IVA 21 + EO 66
    assertEquals(187,    venta.totalFinal(),      1);   // 100 + 87
    assertEquals(1, observadorSffa.getCantLlamadas());  // 👈 se verifica que el observer FUE notificado
    assertEquals(1, observadorSvibaa.getCantLlamadas());
}
```

```
// ¿CÓMO FUNCIONA? qué prueba este test (dos cosas a la vez)
//   1) el CÁLCULO: la venta suma bien (base 100, impuestos 87, final 187).
//   2) el OBSERVER: cada observador registró exactamente 1 notificación (cantLlamadas == 1),
//      lo que confirma que agregarVenta efectivamente los avisó.
//   Resultado esperado: el test pasa. ✅
```

Los otros casos del enunciado siguen la misma forma: **hogar** (IVA + EI → 47.05 / 147.05), **dos electrónicos** (verifica impuestos por producto y el total × 2) y **venta mixta** (electrónico + hogar, cada uno con sus impuestos). Todos calculan el esperado con constantes, nunca "a ojo" (lección de la Parte 2).

---

## 8. Checkpoint de la clase ✅🔴

Respondé de memoria (las respuestas van al **complemento**, no acá). Si alguna te traba, ese es tu tema para repasar.

1. ¿Qué significa "diseñar" en esta materia, y por qué el apunte muestra alternativas descartadas en vez de solo la elegida?
2. El Comercio se modeló solo con un `id`. ¿Con qué criterio, y qué tiene que ver con la orientación a servicios?
3. Enunciá la regla de tipificación. ¿Por qué `Producto` **no** se separa en subclases pero `Impuesto` **sí** en clases?
4. ¿Por qué `Hogar` y `Electrónico` terminan siendo **instancias** de `TipoProducto` y no subclases? ¿Qué gana ese diseño?
5. `Impuesto` es una **interfaz** y no una clase abstracta. Dá las dos razones.
6. ¿Por qué `calcular` recibe el `Producto` entero y no solo el precio base? ¿Qué se gana y qué se paga?
7. ¿Por qué los parámetros de los impuestos (porcentajes, ganancias) son `static`? ¿Qué problema genera eso en los tests y cómo se resuelve?
8. Explicá el patrón **Observer** en este caso: qué desacopla, y qué habría que hacer para sumar un tercer interesado.
9. ¿Por qué `Venta` tiene `ItemVenta` en lugar de una lista de `Producto` directamente?
10. Notificar a los observadores, ¿en paralelo o secuencial? Nombrá dos trade-offs de cada opción.

---

> **Cierre de la unidad.** Con esto, el apunte maestro de la clase 03 está completo (Partes 1-3). Las **respuestas del checkpoint** van al **complemento**; desde este maestro se pueden derivar después el **resumen** y el **machete** de la unidad.

**FIN DE LA PARTE 3 — FIN DEL APUNTE MAESTRO CLASE 03**
