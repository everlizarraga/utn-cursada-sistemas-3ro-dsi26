# 📘 APUNTE MAESTRO — Preclase 02 · Parte 3
## Colecciones y Streams

**Unidad:** `preclase02` — material previo obligatorio de la clase 2
**Viene de:** Parte 2 (Java: el lenguaje). Ya conocés los tipos, las clases y las interfaces.
**Peso:** 🔴 **Esta es la parte más importante de las cinco.** Todo el código que vas a leer y escribir este año pasa por acá.

---

## 1. El mapa 🔴

Java organiza sus colecciones alrededor de **tres interfaces**. Cada una responde a una pregunta distinta:

| Interfaz | Pregunta que responde | ¿Admite repetidos? | ¿Tiene orden? | ¿Acceso por índice? |
|---|---|:---:|:---:|:---:|
| **`List`** | "una secuencia de cosas, en orden" | **Sí** | Sí | **Sí** — `get(3)` |
| **`Set`** | "un conjunto sin repetidos" | **No** | Depende de la implementación | No |
| **`Map`** | "cosas asociadas a una clave" | Claves no; valores sí | Depende de la implementación | **No** — acceso **por clave** |

> ⚠️ **Error en la slide del material.** La tabla comparativa del Video 1 dice que **`Map` tiene acceso por índice: Sí**. **No lo tiene.** Un `Map` se accede **por clave** (`get("MAD")`), no por posición (`get(3)`). El propio video, una slide antes, lo dice bien: *"la clave, en vez de ser un índice…"*. La tabla se contradice con la slide de al lado. La versión correcta es la de arriba.

`List` y `Set` extienden de una interfaz común, `Collection`. **`Map` no** — porque no modela una colección de elementos, sino una **estructura clave→valor**.

🔴 **Las tres son interfaces: no se instancian.** No existe `new List()`. Instanciás una **clase que las implementa**.

### 1.1 `equals`: qué significa "repetido" 🔴

Antes de seguir: cuando decimos que un `Set` "no admite repetidos", ¿qué es un repetido?

**Lo define el método `equals()`.** Está declarado en `Object`, así que **todas** las clases de Java lo tienen. Por defecto compara **identidad**: dos objetos son iguales solo si son *el mismo objeto en memoria*.

Pero se puede **sobrescribir**. Y es una práctica muy común:

```java
public class Pasajero extends Persona {
    private Integer nroDePasaporte;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                    // el mismo objeto: obvio que sí
        if (!(o instanceof Pasajero)) return false;    // ni siquiera es un Pasajero
        Pasajero otro = (Pasajero) o;
        return this.nroDePasaporte.equals(otro.nroDePasaporte);
        // A partir de acá, "el mismo pasajero" = "mismo número de pasaporte",
        // aunque sean dos objetos distintos en memoria.
    }
}
```

**Por qué importa:** desde el momento en que sobrescribís `equals`, cambia el comportamiento de **todas** las colecciones. Un `Set` deja de aceptar dos pasajeros con el mismo pasaporte. `list.contains(pasajero)` empieza a encontrarlo aunque le pases otro objeto con el mismo pasaporte.

⚠️ **Regla que el material no menciona y te va a morder:** **si sobrescribís `equals()`, tenés que sobrescribir `hashCode()` también.** `HashSet` y `HashMap` usan `hashCode()` para decidir en qué "cajón" buscar, y solo comparan con `equals()` dentro de ese cajón. Si dos objetos son `equals` pero tienen `hashCode` distinto, el `HashSet` los guarda en cajones distintos y **acepta el duplicado**. IntelliJ te genera los dos juntos: `Alt+Insert → equals() and hashCode()`.

---

## 2. `List` — secuencias ordenadas 🔴

Admite repetidos, mantiene el orden de inserción, y tiene acceso por índice.

**Tres implementaciones:**

| Clase | Por dentro | Rápida en | Lenta en | Usala cuando |
|---|---|---|---|---|
| **`ArrayList`** | Array dinámico | **Acceso por índice** (`get`) | Insertar/borrar en el medio (hay que desplazar todo) | **Casi siempre.** Es el default. |
| **`LinkedList`** | Lista doblemente enlazada | Insertar/borrar en cualquier posición | **Acceso por índice** (hay que recorrer) | Muchas inserciones y borrados en el medio |
| **`Vector`** | Igual que `ArrayList`, pero sincronizada | — | Todo (por la sincronización) | Nunca, en la práctica. Es *legacy*. |

🔴 **En la vida real, y en el TPA: `ArrayList`.** Las otras dos son cultura general. `LinkedList` gana solo en casos muy específicos, y `Vector` está efectivamente muerta.

```java
List<Vuelo> vuelos = new ArrayList<>();   // ← LA línea que vas a escribir mil veces
vuelos.add(unVuelo);
vuelos.size();       // cuántos hay
vuelos.get(0);       // el primero
```

### 2.1 Tipar con la interfaz, no con la implementación 🔴

Mirá bien la línea de arriba:

```java
List<Vuelo> vuelos = new ArrayList<>();
//  ↑                     ↑
//  el TIPO es la         la IMPLEMENTACIÓN
//  INTERFAZ              es la clase concreta
```

**Nunca escribas `ArrayList<Vuelo> vuelos = new ArrayList<>();`.**

**El porqué:** el tipo de la variable es un **contrato con el resto del código**. Si declarás `List`, estás diciendo "esto es una lista, no te importa cuál". Si mañana necesitás cambiar a `LinkedList`, tocás **una línea** y nada más se entera. Si declarás `ArrayList`, atás a todo el código que la use a esa implementación concreta.

🔴 **La regla general: declará siempre con el tipo más genérico que te sirva.** Es una de las primeras aplicaciones prácticas del principio de **programar contra interfaces, no contra implementaciones** — que es literalmente el corazón de esta materia. La vas a ver una y otra vez, con nombres cada vez más grandes.

---

## 3. `Set` — conjuntos sin repetidos 🟡

No admite duplicados (según `equals`), y **no tiene acceso por índice**.

| Clase | Orden | Usala cuando |
|---|---|---|
| **`HashSet`** | **Ninguno.** El orden es impredecible. | No te importa el orden y querés máxima velocidad. Es el default. |
| **`LinkedHashSet`** | **De inserción.** Salen como entraron. | Necesitás que el orden se respete. |
| **`TreeSet`** | **Ordenado.** Natural, o según un `Comparator` que vos definas. | Necesitás los elementos ordenados automáticamente. |

```java
Set<String> codigos = new HashSet<>();
codigos.add("EZE");
codigos.add("MAD");
codigos.add("EZE");     // ← se ignora: ya estaba
codigos.size();         // Resultado esperado: 2
```

> 🕳️ **Madriguera — cómo hace `HashSet` para ser tan rápido**
> Por dentro es un `HashMap` disfrazado: guarda tus elementos como **claves** y pone un valor de relleno constante. Como buscar por clave en una tabla hash es prácticamente instantáneo (no recorre nada: calcula dónde debería estar y va), preguntar "¿ya está este elemento?" cuesta lo mismo con 10 elementos que con 10.000. Ese es todo el truco.
> *Volvé al camino — te alcanza con saber que es rápido y que no garantiza orden.*

---

## 4. `Map` — claves y valores 🟡

Una estructura **clave → valor**. En otros lenguajes se llama *diccionario*, *hash* o *array asociativo*.

**Dos métodos y ya sabés usarlo:**

```java
Map<String, Aeropuerto> porCodigo = new HashMap<>();

porCodigo.put("EZE", aeropuertoEzeiza);   // guardar: clave → valor
Aeropuerto a = porCodigo.get("EZE");      // recuperar por clave

// Resultado esperado: `a` es el aeropuerto de Ezeiza.
// Si la clave no existe, get() devuelve null.
```

| Clase | Orden | Permite `null` |
|---|---|---|
| **`HashMap`** | Ninguno | Sí, en clave y valor |
| **`LinkedHashMap`** | De inserción | Sí |
| **`TreeMap`** | Ordenado por clave | **No en la clave**, sí en el valor |

> 🕳️ **Madriguera — `Hashtable` y `ConcurrentHashMap`**
> `Hashtable` es un `HashMap` sincronizado: seguro para que varios hilos lo toquen a la vez, pero lento porque **bloquea la tabla entera** en cada operación. Está obsoleta. Su reemplazo moderno es `ConcurrentHashMap`, que bloquea solo el pedacito que está tocando, así que varios hilos pueden trabajar en paralelo. Si algún día necesitás un mapa compartido entre hilos, ese es el que va — nunca `Hashtable`.
> *Volvé al camino — en el TPA, `HashMap` y listo.*

---

## 5. Streams 🔴

**Acá está el corazón de todo.** Si de este apunte te llevás una sola sección, es esta.

### 5.1 Qué son y por qué existen

En Java, las operaciones funcionales sobre colecciones (`filter`, `map`, `reduce`) **no se hacen sobre la colección directamente**. Por decisión de diseño, hay que pedirle a la colección un objeto intermedio: el **Stream**.

```java
List<Vuelo> vuelos = ...;

vuelos.filter(...)             // ❌ NO EXISTE. Una List no entiende filter.
vuelos.stream().filter(...)    // ✅ Así sí.
```

**Un Stream es una tubería de procesamiento.** Tres propiedades que definen todo su comportamiento:

1. **No almacena datos.** No es una colección: es un flujo que pasa **a través** de tus datos.
2. **Se encadena.** Cada operación devuelve otro Stream, así que podés enganchar una tras otra.
3. **Es de un solo uso.** Una vez consumido, se terminó. Necesitás otro `.stream()`.

### 5.2 🎯 El mapa desde JavaScript

**Esto es traducción, no concepto nuevo.** Ya sabés lo que hace cada operación; lo que cambia es el envoltorio.

| JavaScript | Java | Nota |
|---|---|---|
| `arr.filter(x => x.esta)` | `list.stream().filter(x -> x.esta())` | La flecha es `->`, no `=>` |
| `arr.map(x => x.nombre)` | `list.stream().map(x -> x.getNombre())` | igual |
| `arr.flat()` / `arr.flatMap()` | `.flatMap(...)` | igual |
| `arr.reduce(...)` | `.reduce(...)` | igual |
| `arr.forEach(...)` | `.forEach(...)` | igual |
| `arr.length` | `.count()` | ⚠️ devuelve `long` |
| `arr.some(...)` | `.anyMatch(...)` | |
| `arr.every(...)` | `.allMatch(...)` | |
| `arr.find(...)` | `.filter(...).findFirst()` | devuelve un `Optional` |
| **el resultado ya es un array** | **hay que hacer `.collect(...)`** | 🔴 **la gran diferencia** |

🔴 **La única diferencia de fondo:** en JavaScript, `filter` te devuelve un array y listo. **En Java, `filter` te devuelve un Stream, no una lista.** Para volver a tener una colección, tenés que **cerrar la tubería** explícitamente. Eso se hace con `collect`.

Es el error nº 1 de todo el que llega de JS. Y es lo que el video llama *"engorroso, pero se lo van a tener que acordar"*.

### 5.3 Operaciones intermedias vs. terminales 🔴

Es **la** distinción que ordena toda la API.

**Intermedias** — devuelven **otro Stream**. Se pueden encadenar sin límite.

| Operación | Qué hace |
|---|---|
| `filter(x -> condición)` | Se queda con los que cumplen |
| `map(x -> otraCosa)` | **Transforma** cada elemento en otra cosa |
| `flatMap(...)` | Aplana una colección de colecciones (sección 5.6) |
| `sorted()` | Ordena |
| `limit(n)` | Se queda con los primeros n |

**Terminales** — **cierran la tubería** y devuelven un resultado que **ya no es un Stream**. Solo puede haber una, y va al final.

| Operación | Qué devuelve |
|---|---|
| `collect(Collectors.toList())` | Una `List` 🔴 |
| `count()` | Un `long` — cuántos quedaron |
| `sum()` | La suma (solo sobre `IntStream`/`DoubleStream`, ver 5.5) |
| `forEach(x -> ...)` | Nada. Ejecuta algo por cada elemento |
| `anyMatch` / `allMatch` | Un `boolean` |
| `findFirst()` | Un `Optional` con el primero, si hay |
| `reduce(...)` | Un único valor acumulado |

🔴 **La regla de oro:** *sin operación terminal, el Stream no hace nada.* Es una tubería armada pero sin abrir la canilla.

### 5.4 El pipeline completo

```java
List<String> nombres = personas               // una List<Persona>
        .stream()                             // ① abro la tubería
        .filter(p -> p.esMayorDeEdad())       // ② intermedia: me quedo con los mayores
        .map(p -> p.getNombre())              // ③ intermedia: transformo Persona → String
        .collect(Collectors.toList());        // ④ TERMINAL: cierro y recupero una List

// ¿CÓMO FUNCIONA?
// Fijate cómo va cambiando el TIPO que viaja por la tubería:
//   personas          →  List<Persona>
//   .stream()         →  Stream<Persona>
//   .filter(...)      →  Stream<Persona>   (filter NO cambia el tipo: solo descarta)
//   .map(...)         →  Stream<String>    (map SÍ cambia el tipo: Persona → String)
//   .collect(toList())→  List<String>      (volvimos a una colección de verdad)
//
// Resultado esperado: una lista con los nombres de las personas mayores de edad.
```

🔴 **`filter` no cambia el tipo, `map` sí.** Esa es la diferencia entre las dos, y si la tenés clara nunca más te confundís.

### 5.5 Contar y sumar: dos trampas 🔴

**Trampa 1 — `count()` devuelve `long`, no `int`.**

```java
public Integer cantVecesQueVisitaste(Ciudad unaCiudad) {
    return (int) this.vuelos              // ← el cast (int) es OBLIGATORIO
            .stream()
            .filter(v -> v.tuDestinoEs(unaCiudad))
            .count();                     // count() devuelve long
}
// Sin el (int), no compila: "incompatible types: possible lossy conversion from long to int"
```

**Trampa 2 — para sumar, necesitás un `IntStream`.**

Un `Stream<Integer>` normal **no tiene `.sum()`**. Hay que convertirlo con `mapToInt` (o `mapToDouble`), que devuelve un `IntStream` — un stream especializado en números, que sí sabe sumar.

```java
// ❌ NO COMPILA: un Stream<Integer> no entiende sum()
vuelos.stream().map(v -> v.cantPasajeros()).sum();

// ✅ mapToInt devuelve un IntStream, que SÍ tiene sum()
vuelos.stream().mapToInt(v -> v.cantPasajeros()).sum();
//              ↑                                 ↑
//         Stream<Vuelo> → IntStream         ahora sí
```

Lo mismo con decimales: `mapToDouble(...).sum()`, que es lo que hace el repositorio para sumar duraciones.

### 5.6 `flatMap` 🔴 — el que más cuesta

**El problema que resuelve.** Una ciudad tiene **varios** aeropuertos. Cada aeropuerto te devuelve **una lista** de vuelos. Si hacés `map`, ¿qué obtenés?

```java
this.aeropuertos.stream()
    .map(a -> a.vuelosQueLlegaronElDia(dia))
// Tipo del resultado: Stream<List<Vuelo>>
//                            ↑↑↑↑
// Una colección DE COLECCIONES. No es lo que querías.
```

Vos querés **una sola lista de vuelos**, no una lista de listas. **`flatMap` aplana.**

```
map:      [ [v1, v2, v3] , [v5, v7, v8] ]   ← lista de listas 😞
flatMap:  [  v1, v2, v3  ,  v5, v7, v8  ]   ← una sola lista 🎉
```

Con números, para verlo limpio: si tenés `[[1,2,3], [5,7,8]]`, `flatMap` te da `[1,2,3,5,7,8]`.

**Cómo se escribe** (del repositorio de la cátedra, comentado):

```java
public Integer cantPasajerosQueLlegaronElDia(LocalDate dia) {

    List<Vuelo> vuelosQueLlegaronEseDia = this.aeropuertos
            .stream()                                          // Stream<Aeropuerto>
            .flatMap(a -> a.vuelosQueLlegaronElDia(dia)        // cada aeropuerto → List<Vuelo>
                           .stream())                          // ⚠️ ¡y hay que pasarla a Stream!
            .collect(Collectors.toList());                     // Stream<Vuelo> → List<Vuelo>

    return vuelosQueLlegaronEseDia
            .stream()                                          // Stream<Vuelo>
            .mapToInt(v -> v.cantPasajeros())                  // IntStream (los pasajeros de c/u)
            .sum();                                            // el total
}

// ¿CÓMO FUNCIONA? El razonamiento, paso a paso:
// 1. Quiero: cuántos pasajeros llegaron a esta ciudad tal día.
// 2. La ciudad no conoce pasajeros. Conoce AEROPUERTOS.
// 3. Cada aeropuerto sabe qué VUELOS le llegaron ese día → me devuelve una List<Vuelo>.
// 4. Como tengo VARIOS aeropuertos, me quedan VARIAS listas → flatMap las funde en una.
// 5. Ahora tengo todos los vuelos. Cada vuelo sabe cuántos pasajeros trae.
// 6. mapToInt: transformo cada Vuelo en su número de pasajeros. sum(): los sumo.
//
// Resultado esperado: si a Madrid llegaron 2 vuelos ese día, uno con 4 pasajeros
// y otro con 3, devuelve 7.
```

🔴 **El detalle que rompe a todo el mundo:** `flatMap` **espera un Stream**, no una lista. Por eso hay que escribir `.stream()` **adentro** del `flatMap`. Si te olvidás, no compila y el mensaje de error no ayuda nada.

### 5.7 Method references 🟡

Cuando la lambda **solo llama a un método y nada más**, hay una forma corta:

```java
// Estas dos líneas hacen exactamente lo mismo:
.mapToInt(vuelo -> vuelo.cantPasajeros())   // lambda
.mapToInt(Vuelo::cantPasajeros)             // method reference
//        ↑     ↑
//     Clase :: metodo    ← sin paréntesis
```

🔴 **La regla exacta:** solo funciona si el método **no recibe parámetros**. Si recibe aunque sea uno, lambda sí o sí.

```java
.filter(Vuelo::tuDestinoEs)          // ❌ tuDestinoEs(Ciudad) recibe un parámetro
.filter(v -> v.tuDestinoEs(ciudad))  // ✅ con lambda
```

No es obligatorio usarlas. El repositorio de la cátedra las mezcla con lambdas sin criterio fijo. Usá la que se lea mejor.

### 5.8 Más operaciones que vas a necesitar 🟡

El video te invita explícitamente a explorar la API: *"pongan `.stream().`, miren qué hay y busquen un ejemplo"*. Estas son las que aparecen en el código de la cátedra o que vas a necesitar en el TPA:

```java
// ── ¿Alguno cumple? ¿Todos cumplen? → devuelven boolean
vuelos.stream().anyMatch(v -> v.cantPasajeros() > 100);   // ¿hay alguno lleno?
vuelos.stream().allMatch(v -> v.getAvion() != null);      // ¿todos tienen avión?

// ── Encontrar el primero que cumple → devuelve un Optional
Optional<Pais> resultado = paises.stream()
        .filter(p -> p.getNombre().equals("Argentina"))
        .findFirst();

Pais argentina = resultado.orElse(null);   // sacá el valor, o null si no había
// Resultado esperado: el objeto Pais de Argentina, o null si no está en la lista.

// ── El máximo / el mínimo según un criterio → devuelve un Optional
Optional<Ciudad> laQueMasRecibio = ciudades.stream()
        .max(Comparator.comparing(c -> c.cantPasajerosQueLlegaronElDia(dia)));
//       ↑                        ↑
//   max o min          el criterio por el cual comparar
```

> 🕳️ **Madriguera — `Optional`**
> Es una caja que **puede** contener un valor o estar vacía. Existe para que el `null` deje de ser una sorpresa: en vez de que un método devuelva `null` y explote tres líneas más abajo con un `NullPointerException`, devuelve un `Optional` y **te obliga a decidir qué hacer si no hay nada**. Se abre con `.orElse(valorPorDefecto)`, `.orElseThrow()` o `.isPresent()`. Ojo con lo que hace el repo de la cátedra: usa `Optional` y después lo aplasta con `.orElse(null)` — que es tirar a la basura la protección que el `Optional` te daba.
> *Volvé al camino — con `.findFirst().orElse(...)` te alcanza para leer el código de la materia.*

### 5.9 Parallel streams 🟢

`.parallelStream()` procesa los elementos de forma concurrente en vez de uno por uno. Sirve con **volúmenes grandes** de datos; con colecciones chicas es peor el remedio que la enfermedad (el costo de coordinar supera lo que ahorrás).

No lo vas a necesitar en el TPA. Sepan que existe, dice el video, y es exactamente el peso que tiene.

---

## 📌 Para el parcial, si te preguntan

> **¿Qué diferencia hay entre una operación intermedia y una terminal en un Stream?**
> Una operación intermedia devuelve otro Stream y por eso se puede encadenar (`filter`, `map`, `flatMap`, `sorted`); una terminal cierra el pipeline y devuelve un resultado que ya no es un Stream (`collect`, `count`, `sum`, `forEach`). Sin una operación terminal, el Stream no se ejecuta.

> **¿Para qué sirve `flatMap` y en qué se diferencia de `map`?**
> `map` transforma cada elemento en otro, uno a uno; si cada elemento se transforma en una colección, el resultado es una colección de colecciones. `flatMap` hace esa misma transformación y además **aplana** el resultado en una sola colección. Se usa cuando cada elemento devuelve varios elementos.

> **¿Por qué se declara `List<Vuelo> vuelos = new ArrayList<>()` y no `ArrayList<Vuelo> vuelos = ...`?**
> Porque el tipo declarado debe ser el más genérico posible: al tipar con la interfaz, el resto del código depende del contrato (`List`) y no de la implementación (`ArrayList`), de modo que cambiar la implementación no rompe nada. Es programar contra interfaces, no contra implementaciones.

---

## ✅ Checkpoint — Parte 3

1. `List`, `Set` y `Map`: ¿cuál admite repetidos, cuál tiene acceso por índice, y cuál se accede por clave?
2. ¿Qué método define si dos objetos son "el mismo" a los ojos de una colección? ¿Qué otro método hay que sobrescribir junto con él, y qué pasa si no lo hacés?
3. ¿Por qué se escribe `List<Vuelo> vuelos = new ArrayList<>()` y no `ArrayList<Vuelo> vuelos = new ArrayList<>()`?
4. `vuelos.filter(...)` no compila. ¿Por qué, y qué falta?
5. ¿Qué tipo devuelve `filter`? ¿Y `map`? ¿Cuál de los dos puede cambiar el tipo de lo que viaja por la tubería?
6. Escribís `.stream().filter(...)` y nada más. ¿Qué se ejecuta? ¿Por qué?
7. `count()` devuelve `long`. ¿Qué tenés que hacer si tu método declara que devuelve `Integer`?
8. Querés sumar los pasajeros de una lista de vuelos. ¿Por qué `.map(v -> v.cantPasajeros()).sum()` no compila, y cuál es el arreglo?
9. Tenés una lista de aeropuertos y cada uno te devuelve una lista de vuelos. Querés **una sola** lista con todos los vuelos. ¿`map` o `flatMap`? ¿Qué te devolvería el otro?
10. Dentro de un `flatMap`, ¿por qué hay que escribir `.stream()` sobre la colección que devuelve cada elemento?
11. ¿Cuándo podés usar `Vuelo::cantPasajeros` y cuándo estás obligado a escribir la lambda completa?

---

## ⚠️ Errores del material previo (Parte 3)

1. **La tabla comparativa de la slide dice que `Map` tiene acceso por índice.** No lo tiene: se accede **por clave**. La slide anterior del mismo video lo explica bien — la tabla se contradice sola.
2. **El material no menciona `hashCode()`.** Sobrescribir `equals()` sin sobrescribir `hashCode()` rompe silenciosamente los `HashSet` y `HashMap`: aceptan duplicados que deberían rechazar. Van siempre juntos.

---

**Lo que viene — Parte 4: Testing con JUnit 5.** Motivado con un bug real del repositorio de la cátedra: dos de los diez requerimientos del enunciado **no funcionan**, y nadie se dio cuenta porque no había un test que los cubriera.

**FIN DE LA PARTE 3**
