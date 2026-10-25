# 🛠️ Proyecto 0 — Etapa 6: Maps y estructuras anidadas

> **Objetivo:** enriquecer el modelo `Pais` con dos atributos de tipo `Map` (monedas e idiomas), agregar búsquedas por moneda, y aprender a recorrer/transformar Maps con la API que ya conocés.
>
> **Tiempo estimado:** 30-45 minutos.
>
> **Pre-requisito:** Etapa 5 completa (streams andando).

---

## 🎯 Conceptos que vas a tocar

- `Map<K, V>` en serio: declarar, inicializar, iterar, transformar.
- `Map.of(...)` para crear maps inmutables literales.
- `new HashMap<>()` con `.put()` para maps mutables.
- Métodos clave de `Map`: `get`, `containsKey`, `keySet`, `values`, `entrySet`.
- Recorrer un Map con `forEach` (recibe dos argumentos).
- Streams sobre Maps usando `.entrySet().stream()`.
- Composición: usar un DTO (`DetalleMoneda`) como **valor** de un Map.

---

## 🏗️ Lo que vas a construir

Vas a agregar **dos atributos nuevos** a `Pais`:

```
┌─────────────────────────────────────────┐
│                Pais                      │
├─────────────────────────────────────────┤
│ - nombre: String                         │
│ - capital: String                        │
│ - region: String                         │
│ - poblacion: long                        │
│ - monedas: Map<String, DetalleMoneda>    │  ← nuevo
│ - idiomas: Map<String, String>           │  ← nuevo
└─────────────────────────────────────────┘
```

`monedas` mapea **código de moneda** (ej: `"ARS"`) → **objeto `DetalleMoneda`** (con nombre y símbolo).
`idiomas` mapea **código ISO del idioma** (ej: `"spa"`) → **nombre del idioma** (ej: `"Spanish"`).

> Es **exactamente** la estructura del DTO `Pais` del código del profe. Cuando llegues al Bloque 3 del recorrido, vas a leerlo sin fricción.

---

## 🔄 Repaso rápido de Maps

Ya viste los conceptos en el Bloque 0 y en el Anexo de Collections. Te dejo el TL;DR aplicado:

```java
Map<String, String> idiomas = new HashMap<>();
idiomas.put("spa", "Spanish");                 // agregar/actualizar
idiomas.put("eng", "English");

String nombre = idiomas.get("spa");             // recuperar (devuelve null si no existe)
boolean hay = idiomas.containsKey("fra");       // chequear si existe
int cantidad = idiomas.size();                  // cuántas entradas

// Iterar:
idiomas.forEach((codigo, nombreIdioma) ->
    System.out.println(codigo + " → " + nombreIdioma)
);
```

Maps en Java tienen **dos jerarquías paralelas a Collection** (no la extienden). Lo más usado: `HashMap` (default, sin orden), `LinkedHashMap` (mantiene orden de inserción), `TreeMap` (ordena por clave).

---

## ✍️ Paso 1: Actualizar `Pais.java`

Agregá los dos atributos nuevos:

```java
package ar.edu.utn.ba.proyecto0.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pais {
    private String nombre;
    private String capital;
    private String region;
    private long poblacion;
    private Map<String, DetalleMoneda> monedas;
    private Map<String, String> idiomas;
}
```

> **Observación:** como tenés `@AllArgsConstructor`, el constructor ahora recibe **6 argumentos** en vez de 4. Vas a tener que actualizar todas las llamadas a `new Pais(...)` en el catálogo. Es un quilombo manejable — y al final te voy a comentar una alternativa más linda con `@Builder` para tu futuro.

---

## ✍️ Paso 2: Actualizar el constructor del catálogo

Esto es lo más tedioso pero educativo. Abrí `CatalogoPaises.java` y reemplazá el constructor:

```java
public CatalogoPaises() {
    this.paises = new ArrayList<>();

    this.paises.add(new Pais(
        "Argentina",
        "Buenos Aires",
        "Americas",
        45000000L,
        Map.of("ARS", new DetalleMoneda("Argentine peso", "$")),
        Map.of("spa", "Spanish")
    ));

    this.paises.add(new Pais(
        "Brasil",
        "Brasilia",
        "Americas",
        210000000L,
        Map.of("BRL", new DetalleMoneda("Brazilian real", "R$")),
        Map.of("por", "Portuguese")
    ));

    this.paises.add(new Pais(
        "Chile",
        "Santiago",
        "Americas",
        19000000L,
        Map.of("CLP", new DetalleMoneda("Chilean peso", "$")),
        Map.of("spa", "Spanish")
    ));

    this.paises.add(new Pais(
        "España",
        "Madrid",
        "Europe",
        47000000L,
        Map.of("EUR", new DetalleMoneda("Euro", "€")),
        Map.of("spa", "Spanish")
    ));

    this.paises.add(new Pais(
        "Francia",
        "París",
        "Europe",
        67000000L,
        Map.of("EUR", new DetalleMoneda("Euro", "€")),
        Map.of("fra", "French")
    ));

    this.paises.add(new Pais(
        "Japón",
        "Tokio",
        "Asia",
        125000000L,
        Map.of("JPY", new DetalleMoneda("Japanese yen", "¥")),
        Map.of("jpn", "Japanese")
    ));
}
```

`Map`, `Map.of` y `DetalleMoneda` se importan automáticamente con `Alt + Enter`.

### Qué hace `Map.of(...)`

Es una factory **inmutable** que crea un map literal. Recibe pares clave-valor alternados:

```java
Map.of("ARS", new DetalleMoneda("Argentine peso", "$"))
//      ↑                    ↑
//   clave              valor (objeto DetalleMoneda completo)
```

Acepta hasta **10 pares**. Más allá de eso necesitás `Map.entry(...)`:

```java
Map.ofEntries(
    Map.entry("ARS", new DetalleMoneda("Argentine peso", "$")),
    Map.entry("USD", new DetalleMoneda("US Dollar", "$"))
)
```

> **Importante:** `Map.of` devuelve un map **inmutable**. Si después intentás `paises.get(0).getMonedas().put(...)` te tira excepción. Para esta etapa está bien porque hardcodeamos y no modificamos.

---

## ▶️ Paso 3: Verificar que sigue funcionando

Antes de seguir, corré `Main` para confirmar que todo lo de Etapa 5 sigue andando. La salida debería incluir los nuevos campos en el `toString()` (Lombok los regenera automáticamente):

```
Pais(nombre=Argentina, capital=Buenos Aires, region=Americas, poblacion=45000000, monedas={ARS=DetalleMoneda(nombre=Argentine peso, simbolo=$)}, idiomas={spa=Spanish})
```

Si no anduvo, revisá que actualizaste todas las llamadas a `new Pais(...)` (algunas pueden estar en tests o en Main si las dejaste).

---

## 🔍 Paso 4: Implementar `buscarPorMoneda`

Agregá este método al `CatalogoPaises`. Devuelve **una lista** porque varios países pueden compartir una moneda (España y Francia comparten EUR):

```java
public List<Pais> buscarPorMoneda(String codigoMoneda) {
    return this.paises.stream()
        .filter(p -> p.getMonedas().containsKey(codigoMoneda))
        .toList();
}
```

**Lectura:** "del stream de países, quedate con los que tengan en su map de monedas la clave `codigoMoneda`".

### Probalo en `Main`

```java
System.out.println("\n=== Países que usan EUR ===");
catalogo.buscarPorMoneda("EUR")
    .forEach(p -> System.out.println("  - " + p.getNombre()));

System.out.println("\n=== Países que usan ARS ===");
catalogo.buscarPorMoneda("ARS")
    .forEach(p -> System.out.println("  - " + p.getNombre()));
```

**Salida esperada:**
```
=== Países que usan EUR ===
  - España
  - Francia

=== Países que usan ARS ===
  - Argentina
```

---

## 📦 Paso 5: Las tres formas de recorrer un Map

Es importante que conozcas las tres. Cada una sirve para un caso distinto.

### Forma 1: `forEach` con BiConsumer (la más limpia)

```java
Map<String, DetalleMoneda> monedas = catalogo.buscarPorNombre("Argentina")
    .map(Pais::getMonedas)
    .orElse(Map.of());

monedas.forEach((codigo, detalle) ->
    System.out.println(codigo + " → " + detalle.getNombre() + " (" + detalle.getSimbolo() + ")")
);
```

La lambda recibe **dos argumentos**: la clave y el valor. Es lo más natural cuando querés usar ambos.

### Forma 2: `keySet()` o `values()` cuando solo querés uno de los dos

```java
// Solo claves:
for (String codigo : monedas.keySet()) {
    System.out.println("Código: " + codigo);
}

// Solo valores:
for (DetalleMoneda detalle : monedas.values()) {
    System.out.println("Moneda: " + detalle.getNombre());
}
```

### Forma 3: `entrySet()` cuando querés streams sobre el Map

Esto es lo más útil cuando vas a aplicar `filter`, `map`, etc. sobre las entradas.

```java
monedas.entrySet().stream()
    .filter(entry -> entry.getValue().getSimbolo().equals("$"))
    .forEach(entry ->
        System.out.println(entry.getKey() + " usa el símbolo $")
    );
```

`entrySet()` devuelve un `Set<Map.Entry<K, V>>`. Cada `Entry` tiene `.getKey()` y `.getValue()`.

> **Por qué no podés hacer `monedas.stream()` directamente:** `Map` no extiende `Collection`. No es iterable como List. Tenés que pedirle alguna de sus "vistas" (`keySet`, `values`, `entrySet`), y eso sí es un `Set` o `Collection` que tiene `.stream()`.

---

## 🚀 Paso 6: Operaciones más interesantes con Maps en streams

Ahora que `Pais` tiene Maps, podés hacer cosas piolas. Agregalas al catálogo:

### 6.1 `todosLosIdiomas()` — sacar la unión de idiomas únicos

```java
public Set<String> todosLosIdiomas() {
    return this.paises.stream()
        .flatMap(p -> p.getIdiomas().values().stream())
        .collect(Collectors.toSet());
}
```

**`flatMap`** es el "aplanador". Cada `p.getIdiomas().values().stream()` te da un stream **por país**. `flatMap` los **funde en uno solo**:

```
País 1: stream("Spanish")             ┐
País 2: stream("Portuguese")          │
País 3: stream("Spanish")             │ flatMap los junta:
País 4: stream("Spanish")             │ → stream("Spanish", "Portuguese",
País 5: stream("French")              │           "Spanish", "Spanish",
País 6: stream("Japanese")            ┘           "French", "Japanese")
```

Después `.collect(toSet())` saca duplicados.

**Probalo:**
```java
System.out.println("\n=== Idiomas únicos en el catálogo ===");
catalogo.todosLosIdiomas()
    .forEach(System.out::println);
```

### 6.2 `paisesAgrupadosPorMonedaPrincipal()` — Maps como resultado

```java
public Map<String, List<Pais>> paisesAgrupadosPorMonedaPrincipal() {
    return this.paises.stream()
        .collect(Collectors.groupingBy(
            p -> p.getMonedas().keySet().iterator().next()
        ));
}
```

**Lectura:** "agrupá los países por la primera clave de su map de monedas".

`p.getMonedas().keySet().iterator().next()` es **fea pero correcta**: agarra la primera clave del set. La usamos porque en nuestro dataset cada país tiene **una sola moneda**, pero `monedas` es un Map (podría tener varias).

> En código real, esto pediría una decisión de diseño: ¿qué hacer si un país tiene 3 monedas? ¿Lo agrupás en las 3? Ahí es donde `flatMap` se vuelve útil para "explotar" un país en múltiples entradas. Por ahora, suficiente.

**Probalo:**
```java
System.out.println("\n=== Países por moneda ===");
catalogo.paisesAgrupadosPorMonedaPrincipal()
    .forEach((moneda, lista) -> {
        System.out.println(moneda + ":");
        lista.forEach(p -> System.out.println("  - " + p.getNombre()));
    });
```

**Salida esperada:**
```
ARS:
  - Argentina
BRL:
  - Brasil
CLP:
  - Chile
EUR:
  - España
  - Francia
JPY:
  - Japón
```

---

## 🧪 Ejercicios

### Ejercicio 1: País que habla X idioma
Implementá `List<Pais> paisesQueHablan(String codigoIdioma)`. Igual que `buscarPorMoneda` pero sobre `idiomas`.

### Ejercicio 2: Cantidad de idiomas únicos
Implementá `int cantidadIdiomasUnicos()`. Pista: reutilizá `todosLosIdiomas().size()`.

### Ejercicio 3: Lista de símbolos de moneda
Implementá `Set<String> simbolosDeMoneda()` que devuelva todos los símbolos únicos (`$`, `€`, `¥`, `R$`).

Pista: necesitás dos `flatMap` (uno sobre países, otro sobre las monedas) o un `flatMap` + `map`.

### Ejercicio 4: País con más idiomas
Implementá `Optional<Pais> paisConMasIdiomas()`. Pista: `.max(Comparator.comparing(p -> p.getIdiomas().size()))`.

### Ejercicio 5: ¿Hay algún país plurilingüe?
Implementá `boolean hayPaisPlurilingue()`. Pista: `.anyMatch(p -> p.getIdiomas().size() > 1)`.

> Con tu dataset actual la respuesta va a ser `false`. Agregá un país plurilingüe (ej: Suiza con `fra`, `deu`, `ita`) para que el test sea interesante.

### Ejercicio 6: Map invertido — idiomas → países que lo hablan
Implementá `Map<String, List<Pais>> paisesPorIdioma()`. Para cada idioma, qué países lo hablan.

Pista combinada — `flatMap` con `Map.Entry`:

```java
return this.paises.stream()
    .flatMap(p -> p.getIdiomas().keySet().stream()
        .map(idioma -> Map.entry(idioma, p)))
    .collect(Collectors.groupingBy(
        Map.Entry::getKey,
        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
    ));
```

Es la operación más compleja del Proyecto 0. Si te sale, **dominás streams**.

---

## ✅ Criterios de "Etapa 6 completa"

- [ ] `Pais` tiene los atributos `monedas` (Map<String, DetalleMoneda>) e `idiomas` (Map<String, String>).
- [ ] El catálogo carga 6 países con sus monedas e idiomas hardcodeados.
- [ ] Tenés `buscarPorMoneda(String): List<Pais>` con streams.
- [ ] Tenés `todosLosIdiomas(): Set<String>` usando `flatMap`.
- [ ] Probaste las tres formas de iterar un Map (`forEach`, `keySet/values`, `entrySet`).
- [ ] Resolviste al menos los ejercicios 1, 3 y 4.

---

## ✅ Checkpoint

1. ¿Por qué `Map` no tiene método `.stream()` directo, y sí lo tienen `keySet()` y `values()`?
2. ¿Cuándo conviene `Map.of(...)` vs `new HashMap<>()` con `.put()`?
3. ¿Qué hace `flatMap` que `map` no puede hacer?
4. Cuando hacés `monedas.forEach((k, v) -> ...)`, la lambda recibe dos argumentos. ¿Es lo mismo que cuando hacés `lista.forEach(p -> ...)`?
5. Si querés todos los valores únicos de un atributo de tipo Map en una lista de objetos, ¿qué patrón usás?

---

## 🔗 Conexión con código del profe

**Mirá el DTO `Pais.java` del proyecto del profe:**

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pais {
    @JsonProperty("name")
    private NombrePais nombre;

    @JsonProperty("capital")
    private List<String> capitales;

    @JsonProperty("region")
    private String region;

    @JsonProperty("subregion")
    private String subregion;

    @JsonProperty("population")
    private Long poblacion;

    @JsonProperty("area")
    private Double area;

    @JsonProperty("currencies")
    private Map<String, DetalleMoneda> monedas;       // ← idéntico al tuyo

    @JsonProperty("languages")
    private Map<String, String> idiomas;              // ← idéntico al tuyo

    @JsonProperty("cca2")
    private String cca2;

    @JsonProperty("cca3")
    private String cca3;
}
```

**Tu modelo es estructuralmente equivalente al del profe.** Lo único que él tiene de más son:
- `@JsonProperty` y `@JsonIgnoreProperties` (annotations de Jackson para mapear JSON).
- Más campos (subregion, area, codigos ISO).
- `NombrePais` como tipo en vez de `String` para el nombre (porque la API REST devuelve `{ "common": "...", "official": "..." }`).

**Todo lo que aprendiste se transfiere directo.** El método `buscarPorNombre`, las búsquedas con streams, los Maps anidados — todo el código del profe ya no es misterioso.

---

## 🎁 Bonus mental: `@Builder` (te lo dejo para que conozcas)

Al pasar de 4 a 6 atributos, el constructor se volvió incómodo:

```java
new Pais(
    "Argentina",
    "Buenos Aires",
    "Americas",
    45000000L,
    Map.of("ARS", new DetalleMoneda("Argentine peso", "$")),
    Map.of("spa", "Spanish")
)
```

Mañana cuando agregues 3 atributos más, te vas a perder en el orden de argumentos.

Lombok tiene una annotation que arregla esto: `@Builder`.

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder    // ← agregala
public class Pais { ... }
```

Y construir un Pais se vuelve:

```java
Pais argentina = Pais.builder()
    .nombre("Argentina")
    .capital("Buenos Aires")
    .region("Americas")
    .poblacion(45000000L)
    .monedas(Map.of("ARS", new DetalleMoneda("Argentine peso", "$")))
    .idiomas(Map.of("spa", "Spanish"))
    .build();
```

Mucho más legible. Cada línea dice qué se asigna. Si te olvidás de un campo, queda en su valor default (null o 0).

**No te pido que lo uses ahora** para no agregar más conceptos. Pero anotalo — cuando trabajes con DTOs grandes, `@Builder` es lo idiomático moderno.

---

## ▶️ Próximo paso

**¡Completaste el corazón del Proyecto 0!** Tenés un proyecto Java con clases, Lombok, Optional, streams, lambdas y Maps. **Es exactamente el toolbox que el profe usa en `rest-paises`.**

A partir de acá te quedan etapas opcionales:

- **Etapa 7** (Tests con JUnit): aprender a escribir tests unitarios profesionales. Es lo que el profe hace en `BuscadorDePaisesIT.java`.
- **Etapa 8** (Excepciones): manejar errores con `try/catch`, excepciones checked vs unchecked.
- **Etapa 9** (IO de archivos): leer países desde un CSV en vez de hardcodearlos.

Mi recomendación: **arrancá con Etapa 7 (Tests)** porque es el último concepto que necesitás para leer el código del profe completo, incluidos los tests.

Pero también podés **pausar el Proyecto 0** acá y volver al recorrido de los Bloques del código del profe (Bloque 1 en adelante). Tenés todo el contexto necesario.

Decime qué preferís:
- "arranquemos etapa 7" → seguir con tests
- "volvamos al recorrido del profe, bloque 1" → empezar a leer el código real
- "etapa 8" o "etapa 9" → cubrir algo más antes
