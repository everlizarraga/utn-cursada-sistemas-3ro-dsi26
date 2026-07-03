# 🛠️ Proyecto 0 — Etapa 4: Búsqueda con `Optional<Pais>`

> **Objetivo:** implementar `buscarPorNombre()` que puede o no encontrar el país. Devolver `Optional<Pais>` en vez de `null`. Aprender a consumir Optional con sus métodos clave.
>
> **Tiempo estimado:** 30-45 minutos.
>
> **Pre-requisito:** Etapa 3 completa (Lombok andando).

---

## 🎯 Conceptos que vas a tocar

- Implementar un método que devuelve `Optional<T>`.
- Búsqueda lineal en una `List`.
- `Optional.of()` vs `Optional.empty()` vs `Optional.ofNullable()`.
- Consumir Optional con: `isPresent()`, `get()`, `ifPresent()`, `orElse()`, `orElseThrow()`, `map()`.
- Cuándo devolver `Optional<T>` y cuándo **NO** hacerlo.
- Diferencia entre buscar "0 o 1 resultado" (→ Optional) y "0 o muchos resultados" (→ List).

---

## 🔄 Lo que ya hablamos sobre Optional

Para refrescar, dos cosas que ya vimos:

1. **Optional es una "caja"**: la variable nunca es `null`; la caja siempre existe. Lo que cambia es si **adentro** de la caja hay algo o está vacía.

2. **Es la respuesta de Java al `?.` de JS**: en JS escribís `pais?.getCapital() ?? "default"`. En Java escribís:
   ```java
   catalogo.buscarPorNombre("Argentina")
       .map(Pais::getCapital)
       .orElse("default");
   ```
   No es tan compacto, pero es **explícito**. Te obliga a pensar en el caso "vacío".

Esta etapa es donde vas a **escribir** ese código, no solo leerlo.

---

## 🏗️ Lo que vas a construir

Agregás 3 métodos al `CatalogoPaises`:

```
CatalogoPaises
├── buscarPorNombre(String nombre): Optional<Pais>
├── buscarPorCapital(String capital): Optional<Pais>
└── buscarPorRegion(String region): List<Pais>      ← devuelve List, no Optional. Vas a ver por qué.
```

---

## 🔧 Paso 1: Implementar `buscarPorNombre`

Abrí `CatalogoPaises.java`. Agregá este método debajo de `cantidad()`:

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

IntelliJ va a marcar `Optional` en rojo. **`Alt + Enter`** → importar `java.util.Optional`.

### Qué hace esto, paso a paso

**1. La firma del método**
```java
public Optional<Pais> buscarPorNombre(String nombre)
```
- Recibe un `nombre` (String).
- Devuelve un `Optional<Pais>`: una "caja" que puede contener un `Pais` o estar vacía.
- **Quien use este método tiene que decidir explícitamente qué hacer si no hay resultado.**

**2. El loop**
```java
for (Pais p : this.paises) {
    if (p.getNombre().equals(nombre)) {
        return Optional.of(p);
    }
}
```
- Recorre la lista interna.
- En cada iteración, compara el nombre del país actual con el nombre buscado.
- Comparación con `.equals()` (recordá del Bloque 0: NO usar `==` para Strings).
- **Si encuentra coincidencia, retorna inmediatamente** (sale del método con `return`).
- Sigue iterando solo si no encontró.

**3. El return final**
```java
return Optional.empty();
```
- Si el loop terminó sin encontrar coincidencia, esta línea se ejecuta.
- `Optional.empty()` = "caja vacía".

### Las tres formas de crear un `Optional`

| Constructor | Cuándo usarlo |
|---|---|
| `Optional.of(valor)` | Cuando estás **seguro** de que el valor no es `null`. Si pasás `null` aquí, **tira excepción inmediata**. |
| `Optional.empty()` | Cuando explícitamente no hay valor. La "caja vacía". |
| `Optional.ofNullable(valor)` | Cuando el valor **podría ser** `null`. Si es null → caja vacía. Si no → caja con el valor. |

> **¿Cuándo usar `ofNullable`?** Cuando trabajás con APIs viejas o código legacy que devuelve `null` y querés "envolverlo" en Optional. Por ejemplo: `Optional.ofNullable(map.get("key"))` — porque `Map.get()` puede devolver `null`.

En **nuestro caso** sabemos que `p` nunca va a ser `null` (los hardcodeamos en el constructor), así que `Optional.of(p)` está bien.

---

## 🎬 Paso 2: Consumir Optional desde `Main` (las cinco formas)

Abrí `Main.java`. Vaciá el `main` y reemplazalo:

```java
package ar.edu.utn.ba.proyecto0;

import ar.edu.utn.ba.proyecto0.catalogo.CatalogoPaises;
import ar.edu.utn.ba.proyecto0.modelo.Pais;

import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        CatalogoPaises catalogo = new CatalogoPaises();

        // ============================================
        // FORMA 1: isPresent() + get() — la más obvia, no la más elegante
        // ============================================
        System.out.println("=== Forma 1: isPresent + get ===");
        Optional<Pais> resultado1 = catalogo.buscarPorNombre("Argentina");
        if (resultado1.isPresent()) {
            Pais pais = resultado1.get();
            System.out.println("Encontré: " + pais);
        } else {
            System.out.println("No existe.");
        }

        // ============================================
        // FORMA 2: ifPresent() — más limpia cuando solo querés "hacer algo si hay"
        // ============================================
        System.out.println("\n=== Forma 2: ifPresent ===");
        catalogo.buscarPorNombre("Brasil")
                .ifPresent(p -> System.out.println("Encontré: " + p));

        // No imprime nada — Atlantis no existe
        catalogo.buscarPorNombre("Atlantis")
                .ifPresent(p -> System.out.println("Encontré: " + p));

        // ============================================
        // FORMA 3: orElse() — valor por defecto si está vacío
        // ============================================
        System.out.println("\n=== Forma 3: orElse ===");
        Pais paisOrDefault = catalogo.buscarPorNombre("Atlantis")
                .orElse(new Pais("Desconocido", "(sin capital)", "(sin región)", 0L));
        System.out.println("Resultado: " + paisOrDefault);

        // ============================================
        // FORMA 4: orElseThrow() — tirar excepción si no hay
        // ============================================
        System.out.println("\n=== Forma 4: orElseThrow ===");
        try {
            Pais p = catalogo.buscarPorNombre("Atlantis")
                    .orElseThrow(() -> new RuntimeException("País no encontrado: Atlantis"));
            System.out.println(p);
        } catch (RuntimeException e) {
            System.out.println("Capturé la excepción: " + e.getMessage());
        }

        // ============================================
        // FORMA 5: map() + orElse() — lo más parecido a ?. de JS
        // ============================================
        System.out.println("\n=== Forma 5: map + orElse ===");
        String capitalArgentina = catalogo.buscarPorNombre("Argentina")
                .map(Pais::getCapital)
                .orElse("(no encontrada)");
        System.out.println("Capital de Argentina: " + capitalArgentina);

        String capitalAtlantis = catalogo.buscarPorNombre("Atlantis")
                .map(Pais::getCapital)
                .orElse("(no encontrada)");
        System.out.println("Capital de Atlantis: " + capitalAtlantis);
    }
}
```

### Correlo y vas a ver

```
=== Forma 1: isPresent + get ===
Encontré: Pais(nombre=Argentina, capital=Buenos Aires, region=Americas, poblacion=45000000)

=== Forma 2: ifPresent ===
Encontré: Pais(nombre=Brasil, capital=Brasilia, region=Americas, poblacion=210000000)

=== Forma 3: orElse ===
Resultado: Pais(nombre=Desconocido, capital=(sin capital), region=(sin región), poblacion=0)

=== Forma 4: orElseThrow ===
Capturé la excepción: País no encontrado: Atlantis

=== Forma 5: map + orElse ===
Capital de Argentina: Buenos Aires
Capital de Atlantis: (no encontrada)
```

---

## 💡 Cuándo usar cada forma — guía práctica

| Situación | Forma recomendada |
|---|---|
| Quiero hacer algo solo si hay valor (ej: imprimir) | `ifPresent(...)` |
| Quiero un valor por defecto si está vacío | `orElse(...)` |
| Quiero abortar con excepción si está vacío | `orElseThrow(...)` |
| Quiero **transformar** el valor (extraer propiedad, formatear) | `map(...).orElse(...)` |
| Quiero usar el valor en muchas líneas de código condicionales | `isPresent() + get()` (única vez justificada) |

**Regla práctica:** evitá `isPresent() + get()` excepto cuando realmente lo necesitás. Es la forma menos idiomática. El resto (`ifPresent`, `orElse`, `map`) leen mucho más limpio.

---

## ⚠️ El error clásico que **NO** quiero que cometas

```java
Optional<Pais> opt = catalogo.buscarPorNombre("Atlantis");
Pais p = opt.get();   // ❌ NoSuchElementException
```

`Optional.get()` **tira excepción si la caja está vacía**. Es el equivalente Java de hacer `.foo()` sobre algo `null`. Por eso `get()` casi nunca se usa solo — siempre va precedido de un `isPresent()` o se reemplaza por `orElse()`.

> **Tip mental:** si escribís `.get()` sin verificar antes, te estás traicionando a vos mismo. Optional existe para que **no haya que usar `.get()`**.

---

## 🧪 Paso 3: Implementá `buscarPorCapital`

Tu turno. Agregá este método al `CatalogoPaises` **sin mirar el código del paso 1**. Tiene que devolver `Optional<Pais>` igual que `buscarPorNombre`, pero comparando contra `getCapital()` en vez de `getNombre()`.

> Si no te sale, mirá el `buscarPorNombre` y replicá el patrón. La estructura es idéntica.

Probalo desde `Main`:

```java
catalogo.buscarPorCapital("Madrid")
        .ifPresent(p -> System.out.println("Capital encontrada: " + p));
```

---

## 🌍 Paso 4: `buscarPorRegion` — devolver `List`, NO `Optional`

Acá viene un concepto sutil pero importante: **cuándo NO usar Optional**.

Cuando buscás por región, **puede haber muchos países en la misma región**. La respuesta no es "encontré uno o no encontré ninguno" sino "encontré 0, 1, 2, 5 países". Para eso, Optional **no** es la herramienta correcta. Lo correcto es devolver una `List<Pais>` que puede estar vacía o tener muchos elementos.

Agregalo:

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

Y desde `Main`:

```java
System.out.println("\n=== Países de Americas ===");
List<Pais> americanos = catalogo.buscarPorRegion("Americas");
System.out.println("Encontré " + americanos.size() + " países:");
for (Pais p : americanos) {
    System.out.println("  - " + p.getNombre());
}

System.out.println("\n=== Países de Antarctica ===");
List<Pais> antarticos = catalogo.buscarPorRegion("Antarctica");
System.out.println("Encontré " + antarticos.size() + " países.");
```

> Notá que cuando no hay resultados, devolvés una **lista vacía** (`new ArrayList<>()`), NO `null`. **Devolver `null` desde un método que retorna `List` es mala práctica** — obliga a quien lo usa a chequear `if (lista != null)` antes de cada acción. Devolviendo lista vacía, quien la usa puede hacer `for (Pais p : lista)` y simplemente no se ejecuta nada.

---

## 🤔 Regla mental: ¿Optional o List?

| Pregunta | Tu método devuelve... |
|---|---|
| ¿Puede haber **a lo sumo uno**? (ej: buscar por ID único, por nombre exacto) | `Optional<T>` |
| ¿Puede haber **muchos**? (ej: buscar por región, por filtro) | `List<T>` (vacía si no hay) |
| ¿Es un cálculo simple que siempre da resultado? (ej: contar, sumar) | El tipo directo (`int`, `long`, `String`...) |

> **Nunca devolver `Optional<List<T>>`.** No tiene sentido. Si la lista puede estar vacía, ya representa la "ausencia". El Optional encima es ruido. Mismo argumento para `Optional<String>` cuando un string vacío `""` ya representa "vacío" en tu dominio.

---

## 🧪 Ejercicios

### Ejercicio 1: Búsqueda insensible a mayúsculas
Modificá `buscarPorNombre` para que `"argentina"`, `"ARGENTINA"` y `"Argentina"` traigan el mismo país. Pista: `String.equalsIgnoreCase()`.

### Ejercicio 2: Imprimir solo la población si existe
Encadená `map()` para obtener solo la población de un país y mostrarla. Si no existe, mostrar `-1`:

```java
long pob = catalogo.buscarPorNombre("Brasil")
    .map(Pais::getPoblacion)
    .orElse(-1L);
```

### Ejercicio 3: `primerPaisPoblacionMayorA(long limite)`
Implementá un método que devuelva `Optional<Pais>` con el **primer** país (en el orden del catálogo) cuya población sea mayor al límite recibido.

```java
public Optional<Pais> primerPaisPoblacionMayorA(long limite) {
    // tu código acá
}
```

Probalo:
```java
catalogo.primerPaisPoblacionMayorA(100_000_000L)
    .ifPresent(p -> System.out.println("Primero con >100M: " + p.getNombre()));
```

> Notá el guion bajo en `100_000_000L`. Desde Java 7 podés escribir literales numéricos con `_` como separador visual — no afecta el valor, solo legibilidad.

### Ejercicio 4: ¿Funciona buscarPorRegion con regiones inexistentes?
Probá `catalogo.buscarPorRegion("Marte")`. ¿Crashea? ¿Qué imprime? Justificá por qué eso es **bueno**.

### Ejercicio 5: Composición de búsquedas
Hacé una búsqueda en cadena: buscá Argentina, y si existe, buscá el primer país de su misma región. Pista:

```java
Optional<Pais> resultado = catalogo.buscarPorNombre("Argentina")
    .flatMap(p -> catalogo.buscarPorRegion(p.getRegion())
                          .stream()
                          .findFirst());
```

> `flatMap` es como `map` pero cuando lo que devuelve la lambda **ya es un Optional**. No te preocupes si esto te marea — lo retomamos en Etapa 5 con streams.

### Ejercicio 6: Romper aposta con `Optional.of(null)`
Probá en `CatalogoPaises`:
```java
return Optional.of(null);    // en vez de Optional.empty()
```
Corré. ¿Qué excepción te tira? Restaurá después.

---

## ✅ Criterios de "Etapa 4 completa"

- [ ] Tu `CatalogoPaises` tiene `buscarPorNombre(String): Optional<Pais>`.
- [ ] Tu `CatalogoPaises` tiene `buscarPorCapital(String): Optional<Pais>`.
- [ ] Tu `CatalogoPaises` tiene `buscarPorRegion(String): List<Pais>`.
- [ ] Probaste **las 5 formas** de consumir Optional desde `Main`.
- [ ] Resolviste al menos los ejercicios 1, 3 y 4.
- [ ] Podés explicar (a vos mismo) por qué `buscarPorRegion` devuelve `List` y no `Optional<List>`.

---

## ✅ Checkpoint

1. ¿Cuál es la diferencia entre `Optional.of(x)` y `Optional.ofNullable(x)`?
2. ¿Cuándo usás `Optional.empty()`?
3. ¿Por qué `isPresent() + get()` es la forma "menos idiomática" de consumir Optional?
4. ¿Cuándo usar `Optional<T>` como tipo de retorno y cuándo `List<T>`?
5. ¿Qué hace `map(Pais::getCapital)` cuando el Optional está vacío?
6. ¿Por qué devolver `null` desde un método que retorna `List<T>` es mala práctica?

---

## 🔗 Conexión con código del profe

Compará tu `buscarPorNombre` con el del profe:

**Tu versión (Etapa 4):**
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

**Del profe (`BuscadorDePaises.java`):**
```java
public Optional<Pais> buscarPorNombre(String nombre) {
    var uri = UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
            .path("/name/" + nombre)
            .queryParam("fields", CAMPOS)
            .build()
            .toUri();
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    if (cuerpo == null || cuerpo.length == 0) {
        return Optional.empty();
    }
    return Optional.of(cuerpo[0]);
}
```

**La firma es idéntica.** La diferencia: él busca **vía API REST** (HTTP a un servidor), vos buscás **en una lista local**. Pero el contrato del método (recibe nombre, devuelve `Optional<Pais>`) es exactamente el mismo. **Quien usa el método ni se entera de qué fuente proviene el dato.**

Esa idea — **abstraer la fuente del dato detrás de una interfaz limpia** — es uno de los principios de diseño que vas a ver mil veces en DSI. Tu Proyecto 0 ya lo está aplicando.

---

## ▶️ Próximo paso

Cuando completes la etapa, decime **"arranquemos etapa 5"** y vamos a hacer algo divertido: **reemplazar los `for` y los `if` por streams y lambdas**. Vas a reescribir los métodos de búsqueda en 1 sola línea por método. Esa es la pega definitiva de "código moderno" en Java.

Si te trabás, preguntá por chat.
