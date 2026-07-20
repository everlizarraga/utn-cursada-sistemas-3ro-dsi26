# 📝 Apunte Resumen — Clase 03: Servicio de Ventas (SmartLife)

**Unidad:** `clase03` — Modelado de Dominio en Objetos · Arquitectura de Software (08/04/2026)
Destilación del apunte maestro (Partes 1-3) para **repetición espaciada**. Cobertura total, prosa al hueso, **código completo**.

**Marcas — Sistema 1** (importancia): 🔴 central · 🟡 secundario · 🟢 al pasar. **Sistema 2** (uso): 🎯 esencial para aplicar · 📘 contexto para parcial. *Leyendo solo los 🎯 tenés un machete improvisado.*

---

## 1. El caso: Servicio de Ventas 🔴🎯

**SmartLife** = plataforma orientada a **servicios** (ciudad inteligente). Hoy: **Servicio de Ventas**, **solo capa de dominio** (nada de API REST ni eventos, que son de clases siguientes). Los comercios registran ventas; cada venta calcula impuestos y notifica interesados.

**Reglas de impuestos** (precio final = base + suma de impuestos aplicados):

| Impuesto | Fórmula | Sobre base 100 |
|---|---|---|
| **IVA** (todos) | `0.21·base` | 21 |
| **EO** (electrónicos) | `0.5·base + 4·GananciasEO(=4)` | 66 |
| **EI** (hogar) | `base/4 + 0.3·GananciasEI(=3.5)` | 26.05 |

Los porcentajes/ganancias **pueden cambiar** y **pueden sumarse impuestos nuevos**. Interesados a notificar: **SFFA** (facturación, vía **SDK**) y **SVIBAA** (verificación, vía **REST**, aún sin implementar).

## 2. Qué significa diseñar 🔴🎯

**Diseñar = plantear alternativas, evaluar ventajas/desventajas, elegir una justificando.** No hay "la respuesta". La materia evalúa que **decidas y justifiques** ("elijo A **porque** priorizo tal cualidad, y pago con tal otra"); cuando menciones un atributo de calidad, **definilo vos**. Herramientas: **PlantUML** (diagrama de clases por texto) sobre proyecto **Spring Boot**.

## 3. Comercio: lo mínimo necesario 🔴🎯

Se empieza por **Comercio** (venta y producto le pertenecen). Se modela **solo el `id`**: no hay caso de uso "gestionar comercios" y probablemente los administre **otro servicio** (SOA + YAGNI). **🔴 Regla:** una **colección se inicializa en el constructor** (adentro de la clase), nunca por parámetro/setter — la clase mantiene la consistencia de su colección. Altas siempre por método (`agregarProducto`, con **varargs**). *(Código completo del Comercio en la sección 10.)*

> **Para el parcial, si te preguntan** — *"¿Por qué el Comercio solo tiene un `id` y no sus datos completos?"*
> Porque en una arquitectura orientada a servicios cada servicio es dueño de su dominio. El Servicio de Ventas no gestiona comercios (no hay caso de uso para eso); solo necesita **referenciarlos** para asociarles productos y ventas. Con el `id` alcanza para pedirle el resto a quien sí los administre. Modelar de más acoplaría este servicio a datos que no le pertenecen.

## 4. Cómo modelar Producto 🔴🎯

Producto tiene `precioBase` + descripción, y debe calcular `precioFinal()` y `totalImpuestos()`. Los impuestos varían **por tipo**. Cuatro alternativas:

1. **Clase abstracta + subclases (Electrónico/Hogar)** 🟡📘 — tipo nuevo = clase nueva (no en runtime); repetición de lógica; ¿es el producto responsable de los impuestos?
2. **Interfaz + concretas** 🟡📘 — **peor**: la interfaz no lleva comportamiento ni atributos → repetís lógica **y** atributos.
3. **✅ Producto concreto + `TipoProducto` concreto** 🔴🎯 — "Electrónico"/"Hogar" son **instancias** de `TipoProducto` (cada una con su lista de impuestos). Tipo nuevo = **instancia nueva en runtime** (desde una UI), **sin tocar código**. Producto **delega** los impuestos a su tipo.
4. **`TipoProducto` como enum** 🟡📘 — descartada: enum = valores fijos sin comportamiento; tipo nuevo obliga a tocar código; mapeo tipo↔impuestos hardcodeado.

**🔴 Regla de oro — heredar por tipificación es un error grave:** se hereda **solo si hay comportamiento distinto** (código distinto), nunca por atributos compartidos ni por estructura. `Producto` **no** hereda (todos calculan igual, delegando). `Impuesto` **sí** justificará clases separadas (cada uno calcula distinto).

```java
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Producto {
    private Long id;
    private TipoProducto tipo;             // TIENE un tipo (composición), no ES un tipo (herencia)
    private double precioBase;
    private String descripcion;

    public double totalImpuestos() {
        return this.tipo.totalImpuestos(this);   // delega en el tipo (le pasa this)
    }
    public double precioFinal() {
        return this.precioBase + this.totalImpuestos();   // base + impuestos
    }
}
```

```java
import ...impuestos.Impuesto;
import java.util.*;
import lombok.Getter; import lombok.Setter;

@Getter
public class TipoProducto {
    @Setter private String descripcion;         // "Electrónico", "Hogar", "Jardinería"...
    private final List<Impuesto> impuestos;     // los impuestos de ESTE tipo

    public TipoProducto(String descripcion) {
        this.descripcion = descripcion;
        this.impuestos = new ArrayList<>();     // colección inicializada adentro
    }
    public void agregarImpuestos(Impuesto... nuevos) {
        Collections.addAll(this.impuestos, nuevos);
    }
    public double totalImpuestos(Producto producto) {
        return this.impuestos.stream()                    // recorro impuestos...
                .mapToDouble(i -> i.calcular(producto))    // ...cada uno calcula su monto...
                .sum();                                    // ...y sumo.
    }
}
```

> **Para el parcial, si te preguntan** — *"¿Por qué `TipoProducto` es una clase y `Hogar`/`Electrónico` son instancias, en vez de subclases?"*
> Porque los tipos **no tienen comportamiento distinto** entre sí: solo cambian qué impuestos llevan (dato), no *cómo* hacen algo. Modelarlos como instancias hace el diseño **extensible en tiempo de ejecución** — un tipo nuevo es una instancia nueva (dable de alta desde una UI), sin tocar ni recompilar código. Con subclases, cada tipo nuevo obliga a escribir y compilar una clase.

> **Para el parcial, si te preguntan** — *"Te dan un dominio con 'categorías' o 'tipos'. ¿Modelás con herencia?"*
> Primero preguntá: **¿hay comportamiento distinto entre esos tipos?** Si **sí** (cada uno hace algo de forma distinta), la herencia (o una interfaz con implementaciones) se justifica. Si **no** (solo cambian datos/atributos, o es pura tipificación), **no heredés**: modelá el tipo como una **entidad/instancia aparte**. Heredar por tipificación acopla, obliga a tocar código para cada tipo nuevo y suele arrastrar repetición de lógica. Justificá siempre con el criterio de comportamiento.

## 5. Cómo modelar Impuesto 🔴🎯

**🔴 Hay comportamiento distinto en cada impuesto** (IVA multiplica, EO suma ganancias, EI divide) → acá **sí** se separan en clases. Con qué forma:

1. **Una sola clase concreta** 🟡📘 — baja cohesión (sabe de todos); impuesto nuevo = tocar esa clase.
2. **Clase abstracta + hijas** 🟡📘 — no hay estado/comportamiento común real que factorizar arriba; además gasta la única herencia de Java.
3. **✅ Interfaz `Impuesto` + IVA/EO/EI concretas** 🔴🎯 — alta cohesión (una clase por impuesto); impuesto nuevo = clase nueva sin tocar las demás. Interfaz > abstracta porque: **(a)** en Java implementás N interfaces pero heredás de 1 sola clase (la interfaz no gasta la herencia); **(b)** no hay nada genuino que compartir "arriba".

> **Para el parcial, si te preguntan** — *"¿Interfaz o clase abstracta?"*
> **Interfaz** cuando las implementaciones solo comparten un **contrato** (qué saben hacer) y no un estado/comportamiento común real. **Clase abstracta** cuando además comparten **estado o comportamiento** que conviene escribir una sola vez "arriba". Recordá el costo en Java: una interfaz no gasta la herencia (podés implementar N); una abstracta sí (heredás de una sola). Si dudás y no hay nada genuino para factorizar arriba → interfaz.

## 6. El código de los impuestos 🔴🎯

```java
package ...impuestos;
import ...productos.Producto;

public interface Impuesto {
    double calcular(Producto producto);      // contrato: dado un producto, el monto del impuesto
}
```

```java
import ...productos.Producto; import lombok.Getter; import lombok.Setter;

public class IVA implements Impuesto {
    @Getter @Setter private static double porcentaje = 0.21;   // static = atributo de CLASE
    @Override public double calcular(Producto p) {
        return IVA.porcentaje * p.getPrecioBase();             // 0.21·base → sobre 100 = 21
    }
}
```

```java
public class EO implements Impuesto {
    @Getter @Setter private static double factorPrecioBase   = 0.5;
    @Getter @Setter private static double coeficienteGanancias = 4;
    @Getter @Setter private static double gananciasImpositivas = 4;
    @Override public double calcular(Producto p) {
        return EO.factorPrecioBase * p.getPrecioBase()
             + EO.coeficienteGanancias * EO.gananciasImpositivas;   // 0.5·base + 4·4 → 66
    }
}
```

```java
public class EI implements Impuesto {
    @Getter @Setter private static double divisorPrecioBase = 4;
    @Getter @Setter private static double factorGanancias   = 0.3;
    @Getter @Setter private static double gananciasImpositivas = 3.5;
    @Override public double calcular(Producto p) {
        return p.getPrecioBase() / EI.divisorPrecioBase
             + EI.factorGanancias * EI.gananciasImpositivas;        // base/4 + 0.3·3.5 → 26.05
    }
}
```

### 6.1. Por qué `calcular` recibe el `Producto` entero 🟡📘
Para poder contemplar reglas futuras que miren otros atributos (exenciones por marca/importado). **Trade-off:** acopla `Impuesto`→`Producto`; pasar solo `precioBase` desacoplaría pero perdería esa flexibilidad. Se eligió flexibilidad sabiendo el costo.

### 6.2. Parámetros `static` 🟡📘
Los porcentajes/ganancias son **`static`** = atributo de **clase** (el 21% es el mismo para todo el sistema; da igual 1 o 500 instancias de IVA) y **configurables** por setter (pueden cambiar). Efecto colateral: estado compartido → los tests deben **resetearlos** antes de cada uno.

## 7. Packages 🟢📘
Agrupados por concepto: `models/entities/{comercio, productos, impuestos, venta, observers}` + `utils`. Tocás impuestos → sabés dónde ir.

## 8. Tests con JUnit 🔴🎯

`@BeforeEach setUp()` resetea los `static` de impuestos y crea los tipos con sus impuestos. Se verifica el cálculo a nivel producto (el **187**):

```java
@BeforeEach void setUp() {
    IVA.setPorcentaje(0.21); EO.setFactorPrecioBase(0.5); /* ...resto de statics... */
    tipoElectronico = new TipoProducto("Electrónico"); tipoElectronico.agregarImpuestos(new IVA(), new EO());
    tipoHogar = new TipoProducto("Hogar"); tipoHogar.agregarImpuestos(new IVA(), new EI());
}

@Test void electronico_ivaMasEo() {
    Producto tv = new Producto(null, tipoElectronico, 100, "Smart TV 50");
    assertEquals(87,  tv.totalImpuestos(), 1);   // 21 + 66  (esperado CALCULADO, no clavado a ojo)
    assertEquals(187, tv.precioFinal(),    1);   // 100 + 87 ; el 3er arg (1) = delta por los double
}
```

**🔴 Lección:** el valor esperado se **calcula** (o se usan constantes), nunca "a ojo"; si no, asegurá al menos una propiedad verdadera (final > base). Bonus: crear un tipo nuevo en un test es trivial (`new TipoProducto("Jardinería")`) — es el pago del diseño de la sección 4.

## 9. Venta e ItemVenta 🔴🎯

Una venta tiene varios productos, cada uno con **cantidad** → aparece **`ItemVenta`** (producto + cantidad), como el renglón de una factura. `Venta` tiene lista de ítems y sus totales **delegan** en los ítems (que multiplican por cantidad). *(El total de venta se dejó como ejercicio en clase; el repo lo resuelve. `cantidad > 0` es invariante del repo.)*

```java
import ...productos.Producto; import lombok.Getter;

@Getter
public class ItemVenta {
    private final Producto producto;
    private final int cantidad;
    public ItemVenta(Producto producto, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        this.producto = producto; this.cantidad = cantidad;
    }
    public double subtotalPrecioBase() { return producto.getPrecioBase() * cantidad; }
    public double totalImpuestos()     { return producto.totalImpuestos() * cantidad; }
    public double totalFinal()         { return producto.precioFinal()    * cantidad; }
}
```

```java
import java.time.LocalDate; import java.util.*; import lombok.Getter;

@Getter
public class Venta {
    private final long id;
    private LocalDate fechaRegistro;
    private List<ItemVenta> items;
    public Venta(long id) {
        this.id = id; this.fechaRegistro = LocalDate.now(); this.items = new ArrayList<>();
    }
    public void agregarItem(ItemVenta... items) { Collections.addAll(this.items, items); }
    public double totalPrecioBase() { return items.stream().mapToDouble(ItemVenta::subtotalPrecioBase).sum(); }
    public double totalImpuestos()  { return items.stream().mapToDouble(ItemVenta::totalImpuestos).sum(); }
    public double totalFinal()      { return items.stream().mapToDouble(ItemVenta::totalFinal).sum(); }
}
```

## 10. Comercio completo 🔴🎯

`Comercio` suma sus `ventas`, sus `observadores` y `agregarVenta` (valida productos propios → guarda → **notifica a los observadores**). *(La validación `sonTodosProductosPropios` y `eliminarObservador` son agregados del repo.)*

```java
import ...observers.ObservadorVenta; import ...productos.Producto;
import ...venta.ItemVenta; import ...venta.Venta;
import java.util.*; import lombok.Getter;

@Getter
public class Comercio {
    private long id;
    private List<Producto> productos;
    private List<Venta> ventas;
    private List<ObservadorVenta> observadores;

    public Comercio(long id) {
        this.id = id;
        this.productos = new ArrayList<>();
        this.observadores = new ArrayList<>();
        this.ventas = new ArrayList<>();
    }
    public void agregarProducto(Producto... productos)        { Collections.addAll(this.productos, productos); }
    public void agregarObservadores(ObservadorVenta... obs)   { Collections.addAll(this.observadores, obs); }

    public void agregarVenta(Venta venta) {
        if (!sonTodosProductosPropios(venta.getItems().stream().map(ItemVenta::getProducto).toList()))
            throw new IllegalArgumentException("El comercio no puede registrar ventas de productos que no vende");
        this.ventas.add(venta);
        this.observadores.forEach(o -> o.serNotificadoDe(venta));   // 🔴 acá vive el Observer
    }
    public void eliminarObservador(ObservadorVenta o) { this.observadores.remove(o); }
    private boolean sonTodosProductosPropios(List<Producto> productos) { return this.productos.containsAll(productos); }
}
```

## 11. Patrón Observer 🔴🎯

**Problema:** al registrar una venta hay que avisar a SFFA y SVIBAA, y pueden aparecer más. **Alt 1** 🟡: Comercio conoce a cada servicio directo → alto acoplamiento, interesado nuevo = tocar Comercio. **✅ Alt 2** 🔴: interfaz `ObservadorVenta` en el medio; Comercio guarda `List<ObservadorVenta>` y notifica a cada uno **sin saber quién es**; interesado nuevo = clase nueva + agregar a la lista, **sin tocar Comercio**.

```java
public interface ObservadorVenta { void serNotificadoDe(Venta venta); }
```

```java
import ...venta.Venta; import lombok.Getter; import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public class ObservadorSffa implements ObservadorVenta {   // se integrará por SDK
    private long cantLlamadas;                             // andamiaje de testing
    @Override public void serNotificadoDe(Venta venta) {
        // TODO: integración real (pendiente)
        cantLlamadas++;
    }
}
// ObservadorSvibaa: idéntico (se integrará por REST).
```

**Ficha:** 🎯 sujeto (`Comercio`) mantiene lista de observadores y les **avisa automáticamente** sin conocer sus clases · **desacopla** (extensibilidad + bajo acoplamiento) · en el código: `observadores` + `forEach(serNotificadoDe)` · **analogía:** suscripción a newsletter · ❌ campos concretos con llamadas directas / ✅ solo la interfaz.

> **Para el parcial, si te preguntan** — *"¿Por qué el Comercio no conoce directamente a los servicios de facturación y verificación?"*
> Porque acoplarlo a las clases concretas lo haría frágil: cada interesado nuevo obligaría a modificar `Comercio`. Con el **patrón Observer**, `Comercio` depende solo de la **interfaz** `ObservadorVenta` y notifica a una lista genérica; los interesados se agregan/quitan sin tocarlo. Se prioriza **extensibilidad** y **bajo acoplamiento**.

### 11.1. ¿Paralelo o secuencial? 🟡📘
El `forEach` notifica **secuencial** (simple, orden garantizado; pero si uno tarda/falla afecta a los demás). **Paralelo:** más rápido si son lentos/independientes, pero Java no es multihilo por defecto, perdés orden/prioridad y aparece concurrencia. Preguntas: ¿importa el orden?, ¿qué pasa si uno falla?, ¿hay prioridades? Decisión abierta, con trade-offs.

## 12. SDK / biblioteca / framework 🟡📘
**SDK:** kit de código que un servicio externo te da para integrarte (biblioteca + herramientas; ej. Mercado Pago). **Biblioteca vs framework:** ¿quién llama a quién? La biblioteca **la llamás vos**; el framework **te llama a vos** (Spring Boot). *(Pendiente de leer: teoría de Observer, mocking, inyección de dependencias, biblioteca vs framework en detalle.)*

## 13. Cabos sueltos 🟢📘
🕳️ Congelar el precio en el `ItemVenta` (como en una factura) — pendiente. 🕳️ El `id` del Comercio se agregó de más porque se necesita al conectar REST (próxima clase). Herramienta de diagramas: PlantUML u otra, da igual.

## 14. Modelo completo del servicio 🔴🎯
```
Comercio ──*productos──▶ Producto ──tipo──▶ TipoProducto ──*──▶ «interface» Impuesto (IVA, EO, EI)
   │  └──*ventas──▶ Venta ──*items──▶ ItemVenta ──▶ Producto
   └──*observadores──▶ «interface» ObservadorVenta ◀── ObservadorSffa (SDK) / ObservadorSvibaa (REST)
```
Dos cadenas de delegación: **impuestos** (Producto→TipoProducto→Impuesto) y **totales** (Venta→ItemVenta→Producto). Notación: herencia ≠ implementación (flechas distintas; implementación va punteada).

## 15. Test completo 🔴🎯
El `@BeforeEach` arma además comercio + observadores; el test verifica cálculo (base/impuestos/final) **y** que cada observador registró 1 notificación:
```java
comercio.agregarProducto(tv); comercio.agregarVenta(venta);
assertEquals(187, venta.totalFinal(), 1);
assertEquals(1, observadorSffa.getCantLlamadas());   // el Observer fue notificado
assertEquals(1, observadorSvibaa.getCantLlamadas());
```
Casos del enunciado: electrónico (187), hogar (147.05), dos electrónicos (×2), venta mixta.

## 16. Checkpoint 🔴🎯
*(Preguntas de recall; las respuestas viven en el complemento.)*
1. Qué es diseñar acá, y por qué el apunte muestra alternativas descartadas.
2. Comercio solo con `id`: criterio + relación con SOA.
3. Regla de tipificación: por qué Producto no se separa e Impuesto sí.
4. Por qué Hogar/Electrónico son instancias de TipoProducto.
5. Impuesto interfaz vs abstracta: las dos razones.
6. Por qué `calcular` recibe el Producto entero (gana/paga).
7. Por qué los parámetros son `static`; problema en tests y solución.
8. Observer: qué desacopla y cómo sumar un tercer interesado.
9. Por qué Venta usa ItemVenta y no una lista de Producto.
10. Paralelo vs secuencial: dos trade-offs de cada opción.

---

**FIN DEL RESUMEN — CLASE 03**
