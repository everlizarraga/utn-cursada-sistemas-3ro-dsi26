# 🛠️ Proyecto 0 — Etapa 5: Streams y lambdas

> **Objetivo:** reescribir los métodos de búsqueda del catálogo en **una línea cada uno** usando streams. Aprender lambdas, method references, y las operaciones clave de la Stream API.
>
> **Tiempo estimado:** 45-60 minutos. Es la etapa más densa, pero todo lo que aprendas acá lo vas a usar en cada línea de Java moderno.
>
> **Pre-requisito:** Etapa 4 completa (Optional andando).

---

## 🎯 Conceptos que vas a tocar

- **Lambdas** — funciones anónimas, equivalentes a arrow functions de JS.
- **Method references** — `Pais::getNombre`, el atajo definitivo.
- **Stream API** — la abstracción para procesar colecciones de forma declarativa.
- **Operaciones intermedias** (`filter`, `map`, `sorted`) **vs terminales** (`toList`, `count`, `findFirst`).
- **Lazy evaluation** — por qué un stream no hace nada hasta que llega una operación terminal.
- **Collectors** básicos: `joining`, `groupingBy`, `counting`.
- Cuándo usar streams y cuándo seguir con `for` clásico.

---

## 📖 Parte 1: Lambdas — funciones anónimas

### Qué es una lambda

Una lambda es **una función sin nombre**, que se puede pasar como argumento a otro método. Es **idéntico** al concepto de arrow function de JS, solo cambia la sintaxis.

```java
// Sintaxis general:
(parámetros) -> { cuerpo }

// Si tiene UN solo statement, podés omitir las llaves y el return:
parámetro -> expresión
```

### Comparación lado a lado con JS

```javascript
// JS:
const sumar = (a, b) => a + b;
const doble = x => x * 2;
const saludar = () => "hola";
const imprimir = p => { console.log(p); };
```

```java
// Java equivalente:
(a, b) -> a + b
x -> x * 2
() -> "hola"
p -> { System.out.println(p); }
```

**Diferencias de sintaxis:**

| JS | Java |
|---|---|
| `=>` (flecha gruesa) | `->` (flecha fina) |
| Un parámetro: `x => ...` o `(x) => ...` | Un parámetro: `x -> ...` o `(x) -> ...` |
| Sin parámetros: `() => ...` | Sin parámetros: `() -> ...` |
| Varios parámetros: `(a, b) => ...` | Varios parámetros: `(a, b) -> ...` |
| Tipo opcional (TS): `(x: number) => ...` | Tipo opcional: `(int x) -> ...` |

### Las lambdas que ya escribiste en Etapa 4

```java
.ifPresent(p -> System.out.println("Encontré: " + p))
```

Eso `p -> System.out.println(...)` es una lambda. Recibe un `Pais` (lo llamás `p`) y lo imprime. **Ya las estuviste usando**, solo que ahora les pongo nombre.

---

## 📖 Parte 2: Method references — el atajo

Cuando una lambda **solo llama a un método existente sin agregar lógica**, hay una sintaxis más corta:

```java
// Lambda:
list.forEach(p -> System.out.println(p));

// Method reference (idéntico, más corto):
list.forEach(System.out::println);
```

El `::` (dos dos puntos) significa "este método, pero no lo ejecutes ahora — pasámelo como referencia".

### Las 4 formas de method reference

| Tipo | Sintaxis | Ejemplo | Equivalente lambda |
|---|---|---|---|
| Método estático | `Clase::metodoEstatico` | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Método de instancia (de objeto específico) | `objeto::metodo` | `System.out::println` | `x -> System.out.println(x)` |
| Método de instancia (de cualquier objeto del tipo) | `Clase::metodoDeInstancia` | `Pais::getNombre` | `p -> p.getNombre()` |
| Constructor | `Clase::new` | `Pais::new` | `() -> new Pais()` |

**El que más vas a usar es el tercero:** `Pais::getNombre` significa "tomá un Pais y llamale getNombre".

### Cuándo usar lambda vs method reference

- Si la lambda **solo llama un método sin agregar lógica** → method reference.
- Si la lambda **combina cosas o tiene lógica** → lambda.

```java
// ✅ Method reference: solo llama getNombre
paises.stream().map(Pais::getNombre)

// ✅ Lambda: hay lógica adicional (filtrado por contenido)
paises.stream().filter(p -> p.getRegion().equals("Europe"))

// ❌ No podés usar method reference porque hay un .equals() en el medio
paises.stream().filter(Pais::esDeEuropa)   // funcionaría SI tuvieras un método "esDeEuropa" en Pais
```

> Method references hacen el código **más legible**, no más rápido. Es solo azúcar sintáctica.

---

## 📖 Parte 3: La Stream API

### Qué es un Stream

Un **stream** es un "flujo" de elementos sobre el que vas a aplicar operaciones. **No es una colección.** No guarda datos. Es una abstracción sobre la iteración.

```
Lista                   Stream
┌──────┐               ┌──────────────────┐
│ Pais1│               │ →→→ Pais1 →→→    │
│ Pais2│  ←convertís→  │ →→→ Pais2 →→→    │
│ Pais3│  .stream()    │ →→→ Pais3 →→→    │
│ Pais4│               │ →→→ Pais4 →→→    │
└──────┘               └──────────────────┘
 Datos                  Flujo de procesamiento
```

### El patrón típico de uso

```java
list.stream()              // Convertir lista → stream
    .filter(...)            // Operaciones intermedias
    .map(...)
    .sorted()
    .collect/findFirst/...; // Operación terminal
```

Es como una **cadena de producción**:
1. Arranca con `.stream()` (entrada de datos).
2. Pasa por varias "estaciones" que filtran, transforman, ordenan (operaciones **intermedias**).
3. Termina con una operación **terminal** que produce un resultado final.

### Operaciones intermedias vs terminales

**Operaciones intermedias** — devuelven otro Stream. No ejecutan nada todavía:

| Operación | Qué hace |
|---|---|
| `filter(predicado)` | Se queda solo con los que cumplen el predicado |
| `map(transformación)` | Transforma cada elemento (ej: Pais → String) |
| `sorted()` | Ordena (necesita Comparable o un Comparator) |
| `distinct()` | Saca duplicados |
| `limit(n)` | Toma solo los primeros n |
| `skip(n)` | Saltea los primeros n |
| `peek(acción)` | Ejecuta algo en cada elemento sin modificarlo (debug) |

**Operaciones terminales** — devuelven un valor concreto. **Disparan la ejecución**:

| Operación | Devuelve |
|---|---|
| `toList()` | `List<T>` |
| `count()` | `long` (cuántos quedan) |
| `findFirst()` | `Optional<T>` (el primero, o vacío) |
| `findAny()` | `Optional<T>` (cualquiera, útil en paralelo) |
| `anyMatch(pred)` | `boolean` (¿al menos uno cumple?) |
| `allMatch(pred)` | `boolean` (¿todos cumplen?) |
| `noneMatch(pred)` | `boolean` (¿ninguno cumple?) |
| `max(comparator)` | `Optional<T>` (el mayor) |
| `min(comparator)` | `Optional<T>` (el menor) |
| `forEach(acción)` | `void` (ejecuta algo en cada uno) |
| `collect(collector)` | Lo que defina el collector (List, Map, String, etc.) |

### Lazy evaluation — el detalle clave

Las operaciones **intermedias no hacen nada hasta que llegue una terminal**. Mirá:

```java
paises.stream()
    .filter(p -> {
        System.out.println("Filtrando " + p.getNombre());
        return p.getRegion().equals("Europe");
    });
// ↑ Esta cadena NO imprime nada. Es solo "definición".
```

Vs.

```java
paises.stream()
    .filter(p -> {
        System.out.println("Filtrando " + p.getNombre());
        return p.getRegion().equals("Europe");
    })
    .toList();   // ← Ahora SÍ se ejecuta todo. Vas a ver los prints.
```

**Implicancias prácticas:**

- Una cadena de streams sin terminal es código muerto. El compilador no te avisa, pero no hace nada.
- Esto permite **optimizaciones**. Si la cadena es `filter → findFirst`, en cuanto se encuentra el primero que cumple, Java **deja de procesar el resto**.
- Un stream se consume una sola vez. Si lo guardás en variable y aplicás dos terminales, la segunda tira excepción.

---

## ✂️ Parte 4: Refactorizar el catálogo con streams

Esta es la parte satisfactoria. Vamos a reescribir los métodos que tenés.

### 4.1 `buscarPorNombre`

**Antes (con `for`):**
```java
public Optional<Pais> buscarPorNombre(String nombre) {
    for (Pais p : this.paises) {
        if (p.getNombre().equals(nombre)) {
            return Optional.of(p);
        }
    }
    return Optional.empty();
}
```

**Después (con stream):**
```java
public Optional<Pais> buscarPorNombre(String nombre) {
    return this.paises.stream()
        .filter(p -> p.getNombre().equals(nombre))
        .findFirst();
}
```

**Lectura:** "del stream de países, quedate con los que tengan ese nombre, devolveme el primero".

> **Notá:** `findFirst()` ya devuelve un `Optional<Pais>`. No tenés que envolverlo manualmente con `Optional.of()` o `Optional.empty()`. Y por **lazy evaluation**, en cuanto encuentra el primero que cumple, deja de iterar. No es menos eficiente que el `for` con `return` temprano.

### 4.2 `buscarPorRegion`

**Antes:**
```java
public List<Pais> buscarPorRegion(String region) {
    List<Pais> resultados = new ArrayList<>();
    for (Pais p : this.paises) {
        if (p.getRegion().equals(region)) {
            resultados.add(p);
        }
    }
    return resultados;
}
```

**Después:**
```java
public List<Pais> buscarPorRegion(String region) {
    return this.paises.stream()
        .filter(p -> p.getRegion().equals(region))
        .toList();
}
```

**De 7 líneas a 3.** Y se lee como pseudocódigo: "filtrá por región, recolectá en lista".

### 4.3 `buscarPorCapital`

Reemplazá tu versión imperativa por:

```java
public Optional<Pais> buscarPorCapital(String capital) {
    return this.paises.stream()
        .filter(p -> p.getCapital().equals(capital))
        .findFirst();
}
```

---

## 🚀 Parte 5: Nuevos métodos que antes eran un dolor

Acá vas a ver el verdadero superpoder de streams. Agregalos al `CatalogoPaises`.

### 5.1 `paisMasPoblado()` — máximo por un criterio

```java
public Optional<Pais> paisMasPoblado() {
    return this.paises.stream()
        .max(Comparator.comparing(Pais::getPoblacion));
}
```

`Comparator.comparing(funciónExtractora)` arma un comparador a partir de "el campo por el cual querés comparar". `max()` devuelve `Optional` porque si la lista está vacía, no hay máximo.

> **Importa esto:** `Comparator` vive en `java.util`. IntelliJ lo importa con `Alt + Enter`.

**Usalo en Main:**
```java
catalogo.paisMasPoblado()
    .ifPresent(p -> System.out.println("Más poblado: " + p.getNombre()));
```

### 5.2 `poblacionTotal()` — sumar

```java
public long poblacionTotal() {
    return this.paises.stream()
        .mapToLong(Pais::getPoblacion)
        .sum();
}
```

**Detalle nuevo:** `mapToLong` en vez de `map`. Esto convierte el stream a un **stream de primitivos `long`** (`LongStream`), que tiene métodos extra como `.sum()`, `.average()`, `.max()`, `.min()` directos. Más eficiente que `map().reduce()` con wrappers.

Existen también `mapToInt` y `mapToDouble`.

### 5.3 `nombresConcatenados()` — `joining`

```java
public String nombresConcatenados() {
    return this.paises.stream()
        .map(Pais::getNombre)
        .collect(Collectors.joining(", "));
}
```

`Collectors.joining(separador)` es como `Array.prototype.join()` de JS. Concatena strings con el separador.

> `Collectors` también vive en `java.util.stream`. Importalo con `Alt + Enter`.

**Salida ejemplo:** `Argentina, Brasil, Chile, España, Francia, Japón`

### 5.4 `contarPorRegion()` — `groupingBy`

**Este es el que más impresiona.** Antes habrías necesitado un `Map` y un loop con contadores. Ahora:

```java
public Map<String, Long> contarPorRegion() {
    return this.paises.stream()
        .collect(Collectors.groupingBy(
            Pais::getRegion,
            Collectors.counting()
        ));
}
```

**Lectura:** "agrupá por región, y para cada grupo contá cuántos hay".

**Usalo en Main:**
```java
Map<String, Long> conteo = catalogo.contarPorRegion();
conteo.forEach((region, cantidad) ->
    System.out.println(region + ": " + cantidad)
);
```

**Salida ejemplo:**
```
Americas: 3
Europe: 2
Asia: 1
```

### 5.5 `paisesOrdenadosAlfabeticamente()` — `sorted`

```java
public List<Pais> paisesOrdenadosAlfabeticamente() {
    return this.paises.stream()
        .sorted(Comparator.comparing(Pais::getNombre))
        .toList();
}
```

Para ordenar al revés (alfabéticamente descendente):
```java
.sorted(Comparator.comparing(Pais::getNombre).reversed())
```

---

## 🎬 Parte 6: Main con todo aplicado

Reemplazá tu `Main` para probar todo lo nuevo:

```java
package ar.edu.utn.ba.proyecto0;

import ar.edu.utn.ba.proyecto0.catalogo.CatalogoPaises;
import ar.edu.utn.ba.proyecto0.modelo.Pais;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        CatalogoPaises catalogo = new CatalogoPaises();

        // === Búsquedas con streams ===
        System.out.println("=== Buscar Argentina ===");
        catalogo.buscarPorNombre("Argentina")
            .ifPresent(System.out::println);

        // === País más poblado ===
        System.out.println("\n=== Más poblado ===");
        catalogo.paisMasPoblado()
            .ifPresent(p -> System.out.println(p.getNombre() + ": " + p.getPoblacion()));

        // === Población total ===
        System.out.println("\n=== Población total ===");
        System.out.println("Total: " + catalogo.poblacionTotal());

        // === Nombres concatenados ===
        System.out.println("\n=== Nombres ===");
        System.out.println(catalogo.nombresConcatenados());

        // === Conteo por región ===
        System.out.println("\n=== Por región ===");
        Map<String, Long> conteo = catalogo.contarPorRegion();
        conteo.forEach((region, cantidad) ->
            System.out.println(region + ": " + cantidad)
        );

        // === Ordenado alfabéticamente ===
        System.out.println("\n=== Alfabético ===");
        catalogo.paisesOrdenadosAlfabeticamente()
            .forEach(p -> System.out.println("  - " + p.getNombre()));

        // === Stream inline en Main (sin método del catálogo) ===
        System.out.println("\n=== Países con más de 50M de habitantes ===");
        catalogo.getTodos().stream()
            .filter(p -> p.getPoblacion() > 50_000_000L)
            .map(Pais::getNombre)
            .forEach(n -> System.out.println("  - " + n));
    }
}
```

---

## 🧪 Ejercicios

### Ejercicio 1: Lista de capitales únicas
Devolvé una `List<String>` con las capitales de todos los países, sin duplicados.

Pista: `.map(Pais::getCapital).distinct().toList()`

### Ejercicio 2: Países con población > X
Implementá `List<Pais> paisesConPoblacionMayorA(long limite)` en el catálogo.

### Ejercicio 3: Promedio de población
Implementá `double promedioPoblacion()`. Pista: `mapToLong` tiene un método `.average()` que devuelve `OptionalDouble`.

### Ejercicio 4: ¿Hay algún país de Asia?
Implementá `boolean hayPaisDe(String region)`. Pista: `.anyMatch(...)`.

### Ejercicio 5: ¿Todos los países tienen más de 1M de habitantes?
Implementá `boolean todosConPoblacionMayorA(long limite)`. Pista: `.allMatch(...)`.

### Ejercicio 6: Países agrupados por región (no solo contados)
Implementá `Map<String, List<Pais>> agruparPorRegion()` que para cada región devuelva la lista completa de países, no solo el conteo.

Pista:
```java
return paises.stream()
    .collect(Collectors.groupingBy(Pais::getRegion));
```

### Ejercicio 7: Comparar tiempos antes/después
Volvé a Etapa 2, ejercicio 5 (contar países por región sin streams). Compará tu solución imperativa contra `contarPorRegion()` que acabás de hacer. ¿Cuántas líneas ahorraste?

### Ejercicio 8 (bonus): Buscar con `flatMap`
Ahora que estás en streams, retomá el Ejercicio 5 de Etapa 4. Recordá que era:

```java
Optional<Pais> resultado = catalogo.buscarPorNombre("Argentina")
    .flatMap(p -> catalogo.buscarPorRegion(p.getRegion())
                          .stream()
                          .findFirst());
```

Releelo. ¿Te hace más sentido ahora? `flatMap` es como `map` pero **cuando la transformación devuelve otro Optional o Stream**. En vez de quedar con un Optional anidado (`Optional<Optional<...>>`), `flatMap` lo "aplana".

---

## ✅ Criterios de "Etapa 5 completa"

- [ ] Refactorizaste `buscarPorNombre`, `buscarPorRegion` y `buscarPorCapital` usando streams.
- [ ] Tu catálogo tiene `paisMasPoblado()`, `poblacionTotal()`, `nombresConcatenados()`, `contarPorRegion()` y `paisesOrdenadosAlfabeticamente()`.
- [ ] Resolviste al menos los ejercicios 1, 2, 4 y 6.
- [ ] Podés explicar (a vos mismo) la diferencia entre operación intermedia y terminal.
- [ ] Podés escribir una cadena de stream desde cero para un caso nuevo.

---

## ✅ Checkpoint

1. ¿Por qué un stream sin operación terminal no ejecuta nada?
2. ¿Qué diferencia hay entre `.map()` y `.filter()`?
3. ¿Cuándo conviene un `method reference` (`Pais::getNombre`) y cuándo una lambda (`p -> p.getNombre()`)?
4. ¿Por qué `max()` y `findFirst()` devuelven `Optional`?
5. ¿Cuál es la diferencia entre `mapToLong(...)` y `map(...)` cuando trabajás con números?
6. ¿Por qué `Collectors.groupingBy(Pais::getRegion)` devuelve un `Map<String, List<Pais>>` y no un `Map<String, Long>`?

---

## 🤔 Cuándo usar stream vs `for` clásico

**Usá stream cuando:**
- Estás filtrando, transformando, agrupando o reduciendo elementos.
- La operación se lee mejor declarativamente ("dame los países de Europa").
- Querés evitar listas temporales y código repetitivo.

**Usá `for` clásico cuando:**
- Necesitás el **índice** del elemento.
- La operación tiene **efectos colaterales complejos** (modificar estado externo, lanzar excepciones checked).
- Querés que el código sea **debuggeable paso a paso** con breakpoints.
- Vas a `break` o `continue` con lógica compleja.

> **Anti-patrón:** usar `.forEach()` para modificar una variable externa. Si dentro de un `forEach` estás haciendo `lista.add(...)` o cambiando un contador, **ese código debería ser un `for` clásico o un `collect(...)`**. Los streams están pensados para ser "puros" (sin efectos colaterales) — usarlos así te quita las garantías de paralelismo y lazy evaluation.

---

## 🔗 Conexión con código del profe

Los streams que aprendiste son **exactamente lo que el profe usa en los tests** de `BuscadorDePaisesIT.java`:

```java
// Del profe:
assertThat(lista).extracting(p -> p.getNombre().getComun())
    .contains("Argentina");

assertThat(lista).allMatch(p -> "Europe".equals(p.getRegion()));
```

`.extracting(lambda)` es esencialmente un `.map(...)` en la API de AssertJ. `.allMatch(...)` es **literalmente** el `allMatch` de Stream. Cuando llegues al Bloque 5 del recorrido del código del profe, vas a leer esos tests sin fricción.

---

## 🎁 Bonus mental: la "tabla de los 5"

Esta es una mnemotecnia que vale memorizar:

| Si querés... | Usá |
|---|---|
| **Filtrar** elementos que cumplen algo | `.filter(predicado)` |
| **Transformar** cada elemento | `.map(transformación)` |
| **Reducir** a un valor único (suma, conteo, max) | `.count()`, `.sum()`, `.max()`, `.reduce()` |
| **Recolectar** en una colección | `.toList()`, `.collect(Collectors.toMap/groupingBy/...)` |
| **Buscar** un elemento | `.findFirst()`, `.findAny()`, `.anyMatch()`, `.allMatch()` |

Con esos 5 verbos cubrís el 95% de los streams que vas a escribir en tu vida.

---

## ▶️ Próximo paso

Cuando completes la etapa, decime **"arranquemos etapa 6"** y vamos a agregar `Map<String, String>` al modelo `Pais` (para monedas e idiomas) y métodos del catálogo que aprovechan eso. Es lo último del Proyecto 0 antes de empezar a hacer cosas más cercanas al código del profe.

Si te trabás, preguntá por chat.
