# 🧩 Apunte Maestro — Clase 03, Parte 2: Impuestos, implementación y tests

**Unidad:** `clase03` — Modelado de Dominio en Objetos · Arquitectura de Software (08/04/2026)
**Parte 2 de 3.** Acá abrimos la caja de los **impuestos** (por qué terminan siendo una **interfaz**), escribimos el **código** completo del cálculo y lo **probamos con tests**. La **Parte 3** cierra con **Venta**, el **patrón Observer** y el checkpoint del apunte.

**Marcas:** 🔴 central y evaluable · 🟡 secundario · 🟢 mencionado al pasar. 🕳️ = madriguera.

> **Dónde quedamos (Parte 1).** Modelamos `Comercio` (solo `id`), y `Producto` como clase concreta que **delega** el cálculo de impuestos a su `TipoProducto`. `TipoProducto` guarda una `List<Impuesto>` y, en `totalImpuestos(producto)`, recorre esa lista llamando a `impuesto.calcular(producto)`. Es decir: dejamos `Impuesto` como una **caja por abrir**. La abrimos ahora.

---

## 1. Retomamos: ¿qué es `Impuesto`? 🔴

En la Parte 1 quedó una pista clave. La regla de tipificación decía: **se separa en clases solo si hay comportamiento distinto.** `Producto` **no** se separaba (todos calculan el precio final igual, delegando). Con los impuestos pasa **lo contrario**:

> **🔴 HAY COMPORTAMIENTO DISTINTO EN CADA IMPUESTO.**

El IVA se calcula de una forma, el EO de otra y el EI de otra (mirá las fórmulas de la Parte 1: uno multiplica por un porcentaje, otro suma un término de ganancias, otro divide). Como **cada uno hace algo distinto** (código distinto), acá **sí** se justifica tener piezas separadas. Falta decidir con **qué forma** las separamos. De nuevo: alternativas y trade-offs.

---

## 2. Cómo modelar Impuesto — las alternativas 🔴

### 2.1. Alternativa 1 — una sola clase concreta `Impuesto` 🟡

Meter todo en una única clase `Impuesto` que sepa calcular *cualquier* impuesto (con un `switch`/`if` por tipo, o con parámetros que digan "soy IVA / soy EO").

**Qué duele:**
- **Baja cohesión:** una sola clase que sabe de **todos** los impuestos a la vez. Hace demasiado.
- **Mala mantenibilidad:** agregar un impuesto nuevo (el enunciado avisa que puede pasar) obliga a **abrir esa clase** y meterle otro `if`. Un solo archivo que se toca para todo.

### 2.2. Alternativa 2 — clase abstracta `Impuesto` + hijas 🟡

Una superclase abstracta `Impuesto` con `IVA`, `EO`, `EI` como hijas que redefinen el cálculo.

**Qué duele:** una clase abstracta se justifica cuando las hijas **comparten estado o comportamiento real** que conviene factorizar "arriba". Acá, ¿qué tienen en común IVA, EO y EI **de verdad**? Nada de comportamiento (cada cálculo es propio) y ningún atributo común genuino (los parámetros de cada uno son distintos). Poner una clase padre **vacía de contenido compartido** solo para "agrupar" es forzar la herencia. Y además te **gasta** la única herencia que Java te permite (ver abajo).

### 2.3. Alternativa 3 (la elegida) — `Impuesto` como interfaz 🔴

`Impuesto` es una **interfaz**: define **qué** sabe hacer un impuesto (`calcular`), sin decir **cómo**. Después, `IVA`, `EO` y `EI` son clases concretas que la **implementan**, cada una con su propio cálculo.

```
              ┌───────────────────────────┐
              │      «interface»          │
              │      Impuesto             │
              │  + calcular(p): double    │
              └────────────┬──────────────┘
                           ┆ (implementa)
        ┌──────────────────┼──────────────────┐
        ┆                  ┆                   ┆
 ┌─────────────┐    ┌─────────────┐     ┌─────────────┐
 │     IVA     │    │     EO      │     │     EI      │
 │ calcular(): │    │ calcular(): │     │ calcular(): │
 │ 0.21·base   │    │ 0.5·base+16 │     │ base/4+1.05 │
 └─────────────┘    └─────────────┘     └─────────────┘
```

**Por qué gana la interfaz sobre la clase abstracta** (esto es lo evaluable):

1. **Java permite implementar muchas interfaces, pero heredar de una sola clase.** Usar una interfaz **no consume** la herencia: si mañana un impuesto necesitara heredar de otra cosa, todavía puede. La abstracta le **cerraría** esa puerta. La interfaz es más flexible.
2. **No hay nada real que compartir "arriba".** La clase abstracta solo se paga cuando factoriza estado/comportamiento común genuino. Como acá no lo hay, la interfaz (que solo impone el **contrato** `calcular`) es la herramienta justa.
3. **Alta cohesión:** una clase por impuesto. Cada una hace **una** cosa y la hace bien. Agregar un impuesto nuevo = **una clase nueva** que implementa la interfaz, sin tocar las existentes.

> **Para el parcial, si te preguntan** — *"¿Interfaz o clase abstracta?"*
> **Interfaz** cuando las implementaciones solo comparten un **contrato** (qué saben hacer) y no un estado/comportamiento común real. **Clase abstracta** cuando además comparten **estado o comportamiento** que conviene escribir una sola vez "arriba". Recordá el costo en Java: una interfaz no gasta la herencia (podés implementar N); una abstracta sí (heredás de una sola). Si dudás y no hay nada genuino para factorizar arriba → interfaz.

> 🕳️ **Madriguera — "agregar sin tocar lo existente"**
> Que un impuesto nuevo entre como *clase nueva* sin modificar las anteriores es exactamente lo que persigue el principio **Abierto/Cerrado** (abierto a extensión, cerrado a modificación). Es uno de los principios SOLID; se ve formalmente más adelante en la cursada. Por ahora quedate con la idea, no con la sigla.
> *Volvé al camino.*

---

## 3. El código de los impuestos 🔴

**La interfaz** — el contrato, nada más:

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;

public interface Impuesto {                 // una interfaz: describe QUÉ, no CÓMO
    double calcular(Producto producto);     // "dado un producto, devolveme el monto de este impuesto"
}                                           //   👈 sin cuerpo: cada implementación lo resuelve a su manera
```

**Las tres implementaciones.** Cada una es la prueba viviente de "comportamiento distinto":

```java
// IVA: un porcentaje sobre el precio base.
public class IVA implements Impuesto {

    @Getter @Setter                          // Lombok genera get/set... y como el campo es static, los genera STATIC
    private static double porcentaje = 0.21; // 👈 static = atributo de CLASE, no de instancia (ver 3.2)

    @Override
    public double calcular(Producto producto) {
        return IVA.porcentaje * producto.getPrecioBase();   // 0.21 · precioBase
    }                                                       //   sobre 100 → 21
}
```

```java
// EO: un factor sobre el precio base MÁS un término de ganancias. Cálculo distinto al del IVA.
public class EO implements Impuesto {

    @Getter @Setter private static double factorPrecioBase   = 0.5; // parámetros propios de EO...
    @Getter @Setter private static double coeficienteGanancias = 4; //   ...todos static (de clase)...
    @Getter @Setter private static double gananciasImpositivas = 4; //   ...y configurables (pueden cambiar).

    @Override
    public double calcular(Producto producto) {
        return EO.factorPrecioBase * producto.getPrecioBase()        // 0.5 · base
             + EO.coeficienteGanancias * EO.gananciasImpositivas;    //   + 4 · 4
    }                                                                // sobre 100 → 50 + 16 = 66
}
```

```java
// EI: DIVIDE el precio base y suma otro término. Tercer cálculo, tercera clase.
public class EI implements Impuesto {

    @Getter @Setter private static double divisorPrecioBase   = 4;
    @Getter @Setter private static double factorGanancias     = 0.3;
    @Getter @Setter private static double gananciasImpositivas = 3.5;

    @Override
    public double calcular(Producto producto) {
        return producto.getPrecioBase() / EI.divisorPrecioBase       // base / 4
             + EI.factorGanancias * EI.gananciasImpositivas;         //   + 0.3 · 3.5
    }                                                                // sobre 100 → 25 + 1.05 = 26.05
}
```

```
// ¿CÓMO FUNCIONA? (el cálculo completo de un producto, ya con los impuestos abiertos)
//  producto.precioFinal()
//     └─▶ precioBase + producto.totalImpuestos()
//                          └─▶ tipo.totalImpuestos(producto)                  (TipoProducto, Parte 1)
//                                 └─▶ impuestos.stream().mapToDouble(i -> i.calcular(producto)).sum()
//                                        ├─ IVA.calcular  → 21
//                                        └─ EO.calcular   → 66      (si el tipo es Electrónico)
//                                 = 87
//  Resultado esperado para un electrónico de base 100:  100 + 87 = 187.  ✅ (el "187" que verificamos abajo)
//  Para un hogar (IVA + EI): 21 + 26.05 = 47.05  →  precio final 147.05.
```

Fijate que las tres clases **no se parecen** en el cuerpo de `calcular`: multiplicar, sumar-con-ganancias, dividir. **Eso** es "comportamiento distinto", y **eso** es lo que justifica tres clases (a diferencia de los tipos de producto, que no lo tenían).

### 3.1. Por qué `calcular` recibe el `Producto` entero (y no solo el precio base) 🟡

Podríamos haber hecho `calcular(double precioBase)`. ¿Por qué le pasamos el **`Producto` completo**?

- **A favor de pasar el producto:** si mañana una regla dice "los productos importados tienen una exención" o "según la marca cambia la alícuota", el impuesto **ya tiene** el producto para mirar esos atributos. Es más **flexible/extensible**.
- **El costo (trade-off):** `Impuesto` queda **acoplado** a `Producto` (lo tiene que importar). Pasarle solo `precioBase` desacoplaría más (el impuesto no sabría nada de productos), pero perderías la capacidad de contemplar reglas que miren otros atributos.

Se eligió **flexibilidad sobre desacoplamiento**, sabiendo lo que se paga. (Este tipo de justificación explícita — "elijo A, pago B" — es exactamente lo que se evalúa.)

### 3.2. Los parámetros como atributos `static` 🟡

Los `porcentaje`, `factorPrecioBase`, etc. son **`static`**. Dos razones, ambas del enunciado:

- **Es un atributo de CLASE, no de instancia.** El 21% del IVA es el mismo para *todo* el sistema: da igual tener 1 o 500 instancias de `IVA`, **todas comparten** ese valor. Un dato que es "de todos" se modela `static`.
- **Pueden cambiar en el futuro.** Al ser `static` con setter (`IVA.setPorcentaje(0.25)`), si mañana sube el IVA lo cambiás **en un solo lugar** y toda la app lo toma.

> 💡 **Término — `static`:** un miembro `static` pertenece a la **clase**, no a cada objeto. Se accede por el nombre de la clase (`IVA.porcentaje`, `IVA.setPorcentaje(...)`) y hay **una sola copia** compartida. Sin `static`, cada instancia tendría su propio valor.

> ⚠️ **Ojo con el estado `static` en los tests.** Como el valor es compartido y persiste, si un test lo cambia, **afecta a los demás**. Por eso, más abajo, el `setUp()` los **resetea antes de cada test** (lo vemos en la sección 5).

> **Nota de fidelidad (repo vs. clase en vivo):** en la clase se explicó el **concepto** ("el porcentaje es atributo de clase; los valores pueden cambiar"). La materialización con `@Getter @Setter` sobre campos `static` y los setters configurables es como el **repo** lo deja resuelto. Lo incluyo porque implementa exactamente lo que se dijo.

---

## 4. Dónde vive cada cosa: los packages 🟢

El código se organiza por **paquetes**, agrupando por concepto del dominio. Hasta acá:

```
sales_service/
├── models/entities/
│   ├── comercio/        → Comercio
│   ├── productos/       → Producto, TipoProducto
│   ├── impuestos/       → Impuesto (interface), IVA, EO, EI
│   ├── venta/           → Venta, ItemVenta        (Parte 3)
│   └── observers/       → ObservadorVenta, ...     (Parte 3)
└── utils/               → GeneradorIdSecuencial    (Parte 3)
```

Agrupar por concepto (no "todas las interfaces juntas", sino "todo lo de impuestos junto") mantiene el código navegable: si tocás impuestos, sabés dónde ir.

---

## 5. Probarlo: tests con JUnit 🔴

Diseñar y codear no alcanza: hay que **verificar** que los números dan. Usamos **JUnit** (el framework de tests de Java, visto en el seminario).

### 5.1. El armado común: `@BeforeEach`

```java
private TipoProducto tipoElectronico;
private TipoProducto tipoHogar;

@BeforeEach                     // 👈 JUnit ejecuta este método ANTES DE CADA test, de cero
void setUp() {
    // 1) Reseteamos los parámetros static a su valor conocido (por si otro test los cambió):
    IVA.setPorcentaje(0.21);
    EO.setFactorPrecioBase(0.5);
    EO.setCoeficienteGanancias(4);
    EO.setGananciasImpositivas(4);
    EI.setDivisorPrecioBase(4);
    EI.setFactorGanancias(0.3);
    EI.setGananciasImpositivas(3.5);

    // 2) Creamos los dos tipos y les asociamos sus impuestos:
    tipoElectronico = new TipoProducto("Electrónico");
    tipoElectronico.agregarImpuestos(new IVA(), new EO());   // electrónico = IVA + EO

    tipoHogar = new TipoProducto("Hogar");
    tipoHogar.agregarImpuestos(new IVA(), new EI());          // hogar = IVA + EI
}
```

**Por qué el reseteo:** como los parámetros de impuestos son `static` (sección 3.2), viven compartidos. Reseteándolos en `@BeforeEach` garantizás que **cada test arranca del mismo estado**, sin arrastrar cambios de un test anterior. Es aislamiento de tests.

> *(El `setUp()` del repo además arma el `Comercio`, los observadores y un generador de ids — eso pertenece a la Parte 3. Acá mostramos solo la parte de impuestos/tipos.)*

### 5.2. El test del cálculo (el famoso 187)

Verificamos a nivel **producto** que los impuestos dan lo que esperamos:

```java
private static final double IVA_UNIT = 21;
private static final double EO_UNIT  = 66;
private static final double IMPUESTOS_ELECTRONICO_UNIT = IVA_UNIT + EO_UNIT;   // = 87

@Test
void unProductoElectronico_sumaIvaMasEo() {
    // precio base 100, tipo electrónico (id null: al dominio no le importa para calcular)
    Producto tv = new Producto(null, tipoElectronico, 100, "Smart TV 50");

    // Esperado calculado a mano, NO clavado a ojo: 21 + 66 = 87
    assertEquals(IMPUESTOS_ELECTRONICO_UNIT, tv.totalImpuestos(), 1);  // impuestos = 87
    assertEquals(187, tv.precioFinal(), 1);                            // final = 100 + 87 = 187
}
```

```
// ¿CÓMO FUNCIONA? assertEquals(esperado, real, delta)
//   compara 'esperado' contra 'real'; si difieren en más que 'delta', el test FALLA.
//   El tercer parámetro (1) es la TOLERANCIA: los double tienen imprecisión de punto flotante,
//   así que no exigimos igualdad exacta sino "igual con un margen".
//   Resultado esperado: el test pasa (87 y 187 dan). ✅
```

### 5.3. La lección de testing 🟡

Dos cosas que valen para siempre:

- **No claves el valor esperado "a ojo".** Si escribís `assertEquals(130, tv.precioFinal())` porque "te parecía", y en realidad da **187**, el test miente. El esperado se **calcula** (a mano o con constantes como `IVA_UNIT + EO_UNIT`), no se adivina. Un test con el esperado inventado no prueba nada.
- **Si no querés/podés calcular el número exacto, al menos asegurá una propiedad verdadera:** por ejemplo, que el precio final sea **mayor** que el precio base (porque pasó por impuestos). Es una verificación más débil, pero honesta.

> **Bonus de extensibilidad (la prueba de fuego del diseño):** crear un tipo nuevo en un test es trivial — `new TipoProducto("Jardinería")` y `agregarImpuestos(new IVA())`. Sin escribir ni una clase. Esa facilidad es el **pago** de haber elegido la Alternativa 3 en la Parte 1 (tipos como instancias). El diseño se "cobra" acá.

---

## 6. El modelo hasta acá

```
┌──────────────┐  *  ┌────────────────────┐  tipo  ┌──────────────────┐  * ┌─────────────────┐
│  Comercio    │────▶│     Producto       │───────▶│  TipoProducto    │───▶│  «interface»    │
│  - id        │     │  - precioBase      │        │  - descripcion   │    │   Impuesto      │
│ (ventas y    │     │  + precioFinal()   │        │  + totalImpuestos(p)   │  + calcular(p)  │
│  observadores│     │  + totalImpuestos()│        └──────────────────┘    └────────┬────────┘
│  en Parte 3) │     └────────────────────┘                                 ┌───────┼───────┐
└──────────────┘                                                          IVA      EO      EI
```

La caja de los impuestos quedó **abierta y probada**. Lo que sigue es **cómo se registra una venta** y **a quién se le avisa**.

---

## Qué viene en la Parte 3

Modelamos **Venta** e **ItemVenta** (una venta tiene varios ítems, cada uno con su cantidad), completamos **Comercio** con su lista de ventas, y llegamos al primer **patrón de diseño con nombre** de la materia: el **Observer**, para notificarles a SFFA y SVIBAA cada venta registrada — con la pregunta abierta de si conviene notificar **en paralelo o secuencial**. Cerramos con **SDK vs. biblioteca vs. framework** y el **checkpoint** de toda la clase.

**FIN DE LA PARTE 2**
