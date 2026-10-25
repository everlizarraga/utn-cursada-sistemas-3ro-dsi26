# 📦 Apunte — Optional en Java (referencia rápida)

> **Qué es esto:** la versión destilada de Optional para **repasar**, no para aprender de cero. Métodos por intención, árbol de decisión, malas prácticas y los puentes a JS. El detalle pedagógico completo está en los entrenamientos; esto es el mapa de bolsillo.
>
> **Las 3 cosas que más muerden viniendo de JS:**
> 1. Optional **no** es una Promise. Es presencia ("¿lo tengo o no?"), no tiempo ("¿cuándo llega?"). Equivale a `T | null`, no a `Promise<T>`. Y acá **no hay async** en ningún lado.
> 2. `orElse(fabricar())` ejecuta `fabricar()` **siempre** (es un argumento de método). El `??` de JS era perezoso; `orElse` no.
> 3. `.get()` no se usa. Nunca.

---

## 1. 🧠 Qué es

`Optional<T>` = una caja que **contiene UN valor de tipo T, o está vacía**. Es el `null` de JS hecho objeto explícito, que la firma anuncia y que viene con herramientas para no olvidarte de chequear.

```java
Optional[Producto(...)]   // "busqué y encontré"
Optional.empty            // "busqué y no había"
```

**El porqué:** `Pais buscar(...)` no avisa que puede devolver null → NPE olvidable. `Optional<Pais> buscar(...)` lo pone **en el tipo** → imposible no enterarte. La ausencia pasa de detalle olvidable a parte del contrato.

> No es colección (nunca "varios"), no es promesa (la respuesta ya es final), no es magia (un objetito con un campo que puede estar vacío).

---

## 2. 🏭 De dónde salen

**a) De streams** (varias terminales devuelven Optional, porque podrían no hallar nada):
```java
lista.stream().filter(...).findFirst();   // Optional<T>
lista.stream().min(comparator);           // Optional<T>  (¿el mínimo de vacío?)
```

**b) De tus buscadores** (el patrón que vas a escribir mil veces):
```java
public Optional<Producto> buscarPorNombre(String nombre) {
    return productos.stream()
            .filter(p -> nombre.equalsIgnoreCase(p.getNombre()))
            .findFirst();   // filter + findFirst YA fabrican el Optional
}
```

**c) A mano (las 3 fábricas):**

| Fábrica | Si el valor es null... | Cuándo |
|---|---|---|
| `Optional.of(v)` | **explota** (NPE) | lo construí yo con `new`, sé que hay algo |
| `Optional.ofNullable(v)` | caja **vacía** | **el puente** desde APIs que devuelven null (ej. `Map.get`) |
| `Optional.empty()` | — | devolver "no hay" explícito |

> Regla `of` vs `ofNullable`: ¿lo acabás de crear vos? → `of`. ¿Viene de afuera (un get, un parámetro, una API)? → `ofNullable`.

---

## 3. 🎯 Los métodos, por INTENCIÓN

No memorices la lista — preguntate **qué querés hacer**. Dos grupos: los que te dejan **en la caja** (encadenables) y los que te **sacan** (aterrizar una sola vez, al final).

### Procesar EN la caja (devuelven otro Optional → se encadenan)

| Querés... | Método | En JS |
|---|---|---|
| Transformar (la función da un **valor plano**) | `map(f)` | `x?.campo` |
| Transformar (la función da **otro Optional**) | `flatMap(f)` | `x?.a?.b` (cada salto puede fallar) |
| Descartar si no cumple una condición | `filter(pred)` | `(x && cond) ? x : null` |
| Probar OTRA fuente si está vacío | `or(() -> otra)` | `buscarA() ?? buscarB()` |

### Aterrizar — salir de la caja (UNA vez, al final)

| Querés... | Método | En JS |
|---|---|---|
| El valor, o un default barato | `orElse(v)` | `x ?? v` |
| El valor, o un default **costoso** | `orElseGet(() -> v)` | `x ?? fabricar()` (lazy) |
| El valor, o reventar claro | `orElseThrow(() -> new ...)` | `if (!x) throw` |
| Hacer algo si está (efecto) | `ifPresent(v -> ...)` | `if (x) hacer(x)` |
| Una acción para cada rama | `ifPresentOrElse(v -> ..., () -> ...)` | `if/else` de efectos |
| Solo el sí/no | `isPresent()` / `isEmpty()` | `x != null` / `x == null` |
| ~~Sacar sin chequear~~ | ~~`get()`~~ ❌ | usar sin chequear |

**El patrón estrella (el 80% del uso real):**
```java
buscar(nombre).map(Producto::getPrecio).orElse(-1.0);   // en JS: producto?.precio ?? -1.0
```
`map` (el `?.`) + `orElse` (el `??`). Procesás con la red puesta, aterrizás en un valor concreto.

---

## 4. ⚖️ Las dos distinciones finas

**`orElse` vs `orElseGet` (eager vs lazy):**
```java
.orElse(fabricar())          // fabricar() corre SIEMPRE (es un argumento de método)
.orElseGet(() -> fabricar()) // fabricar() corre SOLO si está vacío
```
Con constantes (`orElse(0)`) da igual. Con algo costoso o con efectos → `orElseGet`. El `??` de JS era lazy de fábrica; en Java la lambda es la pereza hecha a mano.

**`map` vs `flatMap` (mecánico, ni lo pienses):**
- La función devuelve un **valor plano** (`getTitulo` → String) → `map`.
- La función devuelve un **Optional** (otra búsqueda) → `flatMap` (aplana la caja-dentro-de-caja).
```java
.map(p -> Optional.ofNullable(p.getVencimiento()))      // ❌ Optional<Optional<LocalDate>>
.flatMap(p -> Optional.ofNullable(p.getVencimiento()))  // ✅ Optional<LocalDate>
```

---

## 5. 🌳 Árbol de decisión

**A — Estoy escribiendo un método, ¿devuelvo Optional?**
```
¿Devuelve UNA cosa que puede no encontrarse? → Optional<T>  (y NUNCA null: vacío = Optional.empty())
¿Devuelve una colección?                     → la colección VACÍA. Nunca Optional<List>.
¿La ausencia tiene default natural del dominio? → absorbé el orElse ADENTRO, devolvé tipo plano.
¿Siempre hay resultado?                       → tipo plano, sin caja.
```

**B — Tengo un Optional, ¿qué método uso?**
```
1) Procesar en la caja (encadenable):
   transformar→valor: map | transformar→Optional: flatMap | validar: filter | otra fuente: or
2) Aterrizar (UNA vez, al final):
   default barato: orElse | default costoso: orElseGet | error: orElseThrow
   efecto si está: ifPresent | efecto en ambas ramas: ifPresentOrElse | solo sí/no: isPresent/isEmpty
```

**La forma general de casi todo uso real:**
```java
buscar(...).filter(...).map(...).or(...).orElse(...)   // procesar en la caja → aterrizar al final
```
Si te encontrás aterrizando en el medio (`orElse(x)` y volver a envolver), algo está torcido — misma alarma que el `.toList().stream()` en el medio de un pipeline.

---

## 6. 🚫 Dónde NO usar Optional (malas prácticas)

Optional es para **retornos de métodos que buscan**. Fuera de ahí, estorba.

| ❌ Mal | ✅ Bien | Por qué |
|---|---|---|
| `private Optional<LocalDate> venc;` (campo) | campo nullable normal + `getVencOpt()` que envuelve al consultar | Optional no es serializable; Lombok/JPA/Jackson lo pelean; el campo puede ser él mismo null |
| `void f(Optional<String> x)` (parámetro) | sobrecarga, o parámetro nullable | obliga a todos a envolver; el param tendría 3 estados (null/vacío/lleno) |
| `Optional<List<T>>` | `List<T>` vacía si no hay | la lista vacía YA dice "no encontré" |
| devolver `null` donde la firma dice Optional | `Optional.empty()` | el que llama confió y le hará `.map` → NPE |
| `get()` / `isPresent()+get()` | cadena o método por intención | es usar-sin-chequear / null-check con pasos extra |
| `Optional.of(puedeSerNull)` | `Optional.ofNullable(...)` | `of` con null = NPE |

> ⚠️ **Nuance honesta:** Optional no es religión. Para una variable **local** nullable que usás dos líneas después, un `if (x != null)` es más claro que envolver en `ofNullable(...).ifPresent(...)`. Optional gana en **fronteras** (retornos, contratos entre capas) y en **cadenas** (buscar→validar→transformar→aterrizar), no en el micro-código local.

---

## 📎 Cheatsheet JS ↔ Java

| Concepto | JS | Java Optional |
|---|---|---|
| "puede no estar" | `T \| null` | `Optional<T>` |
| valor o default | `x ?? def` | `.orElse(def)` |
| valor o default lazy | `x ?? fabricar()` | `.orElseGet(() -> fabricar())` |
| navegar adentro | `x?.campo` | `.map(O::getCampo)` |
| navegar + default | `x?.campo ?? def` | `.map(O::getCampo).orElse(def)` |
| saltos que pueden fallar | `a?.b?.c` | `.flatMap(...).flatMap(...)` |
| validar | `(x && cond) ? x : null` | `.filter(pred)` |
| otra fuente | `buscarA() ?? buscarB()` | `.or(() -> buscarB())` |
| hacer si está | `if (x) hacer(x)` | `.ifPresent(v -> ...)` |
| existe / no existe | `x != null` / `x == null` | `.isPresent()` / `.isEmpty()` |

**Lo que tenés que recordar:** Optional = caja con uno o nada (= `T \| null`, NO Promise). Procesás en la caja (`map`/`flatMap`/`filter`/`or`), aterrizás una vez al final (`orElse`/`orElseThrow`/`ifPresent`). Solo en retornos de búsquedas: ni campos, ni parámetros, ni colecciones. `get()` jamás.
