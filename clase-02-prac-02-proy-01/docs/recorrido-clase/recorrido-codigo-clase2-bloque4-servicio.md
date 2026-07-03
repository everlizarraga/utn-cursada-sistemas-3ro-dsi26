# 📘 Bloque 4 — `BuscadorDePaises`, RestTemplate y URIs

> **Objetivo:** desarmar el archivo más importante del proyecto. Ver cómo Spring inyecta las dependencias en este servicio, cómo se arma una URI con `UriComponentsBuilder`, cómo `RestTemplate` hace HTTP y le pasa el JSON a Jackson, y por qué los 5 métodos siguen el mismo patrón.
>
> **Este es el bloque donde todo se aplica junto**: IoC + DTOs + Jackson + Optional + Lists + null-safety.
>
> **Pre-requisito:** Bloques 1, 2 y 3 completos. Idealmente Proyecto 0 hasta Etapa 5 (streams) o 6 (Maps).

---

## 🧭 Mapa del bloque

1. Vista de pájaro de la clase entera.
2. La declaración: `@Component` y la constante `CAMPOS`.
3. Los atributos y el constructor — **la inyección de dependencias en acción**.
4. `buscarTodos()` — el método más simple, desarmado paso a paso.
5. `buscarPorNombre()` — con placeholder en la URL.
6. Los otros 3 métodos (mismo patrón).
7. `UriComponentsBuilder` — por qué no concat de strings.
8. `RestTemplate.getForObject(...)` — cómo HTTP + Jackson se conectan.
9. El patrón "null-safe" en cada return.
10. Comparación con tu `CatalogoPaises` del Proyecto 0.

---

## 🦅 Parte 1: Vista de pájaro

Antes de meternos línea por línea, mirá el archivo como bloques:

```java
@Component                                                          // ← 1. Marca: es bean
public class BuscadorDePaises {

    private static final String CAMPOS = "name,capital,...";        // ← 2. Constante

    private final RestTemplate restTemplate;                        // ← 3. Dependencias
    private final RestCountriesProperties propiedades;

    public BuscadorDePaises(...) {...}                              // ← 4. Constructor

    public List<Pais> buscarTodos() {...}                           // ← 5. Métodos
    public Optional<Pais> buscarPorNombre(String nombre) {...}
    public List<Pais> buscarPorMoneda(String codigoMoneda) {...}
    public List<Pais> buscarPorRegion(String region) {...}
    public List<Pais> buscarPorCapital(String capital) {...}
}
```

**6 bloques estructurales.** Vamos uno por uno.

---

## 🏷️ Parte 2: `@Component` y la constante `CAMPOS`

```java
@Component
public class BuscadorDePaises {

    private static final String CAMPOS =
            "name,capital,region,subregion,population,currencies,languages,area,cca2,cca3";
```

### `@Component`

Ya sabés qué hace (Bloque 2): le dice a Spring "esta es mi clase, registrame una instancia como bean".

**Consecuencias:**
- Spring crea **una sola instancia** de `BuscadorDePaises` al arrancar (singleton).
- Esa instancia queda disponible para inyectar donde se necesite.
- Si dos clases reciben `BuscadorDePaises` inyectado, **reciben la misma instancia**.

> El profe usa `@Component`. Una alternativa **igualmente válida** sería `@Service` — semánticamente es para "clases de lógica de negocio". Los dos hacen exactamente lo mismo técnicamente, pero `@Service` comunica intención mejor para una clase de este tipo. El profe eligió `@Component` (más genérico). No hay error.

### La constante `CAMPOS`

```java
private static final String CAMPOS =
        "name,capital,region,subregion,population,currencies,languages,area,cca2,cca3";
```

**Lectura:** "una constante de clase, inmutable, con la lista de campos que vamos a pedir a la API".

**Cada modificador importa:**

- `private` — solo `BuscadorDePaises` la ve. Encapsulamiento.
- `static` — pertenece a la **clase**, no a cada instancia. Existe una sola vez para todos.
- `final` — no se puede reasignar. Es **realmente** constante.

**Por qué `static`:** la constante es la misma para cualquier instancia. No tiene sentido que cada `BuscadorDePaises` tenga su propia copia (aunque solo haya una por singleton).

**Por qué la constante:** Es **DRY** (Don't Repeat Yourself). Los 5 métodos piden los **mismos campos** a la API. Si tuvieras que repetir el string en cada método, cambiarlo en un lugar y olvidar otro sería un bug latente. Acá está **una sola vez**, en un solo lugar.

**Por qué los nombres en inglés:** son los nombres **del JSON de la API**, no de tu DTO. La API entiende `name`, `capital`, `population`, etc. (lo que viste en Bloque 3).

> **Convención:** las constantes en Java se escriben en `SCREAMING_SNAKE_CASE`. Ej: `MAX_RETRIES`, `BASE_URL`, `CAMPOS`. Es lo que vas a ver siempre.

---

## 💉 Parte 3: Atributos + constructor — IoC en vivo

```java
private final RestTemplate restTemplate;
private final RestCountriesProperties propiedades;

public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
    this.restTemplate = restTemplate;
    this.propiedades = propiedades;
}
```

**Esto es exactamente lo que predije al final del Bloque 2.** Te lo recuerdo:

- Dos atributos `private final` — inmutables después del constructor.
- Constructor con dos parámetros: un `RestTemplate` y un `RestCountriesProperties`.
- Sin `@Autowired` — Spring 4.3+ lo asume cuando hay **un solo constructor**.

### Lo que pasa cuando Spring arranca

1. Spring procesa `@Component BuscadorDePaises`.
2. Mira el constructor: pide `RestTemplate` y `RestCountriesProperties`.
3. Busca en el contexto: ¿hay esos beans?
   - `RestTemplate` ✅ (declarado con `@Bean` en `RestTemplateConfig`).
   - `RestCountriesProperties` ✅ (registrado con `@EnableConfigurationProperties`).
4. Hace efectivamente: `new BuscadorDePaises(restTemplate, propiedades)`.
5. Guarda esa instancia como bean `buscadorDePaises`.

**No hay magia.** Es lo de siempre: Spring lee, encuentra, ejecuta.

### Por qué `final` y por qué constructor

Los atributos `final` te garantizan que **nadie puede cambiarlos después** de construir. Es **inmutabilidad efectiva**:

```java
buscadorDePaises.restTemplate = otroRestTemplate;   // ❌ COMPILATION ERROR
```

El compilador te bloquea. **El servicio tiene exactamente las dependencias con las que nació.**

Si Spring usara field injection (`@Autowired` arriba del atributo), no podrías declararlos `final`. Otra razón más para preferir constructor injection.

---

## 🔍 Parte 4: `buscarTodos()` desarmado

Empezamos por el método más simple. **Te lo muestro de nuevo entero y después lo desarmamos:**

```java
public List<Pais> buscarTodos() {
    URI uri =
            UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                    .path("/all")
                    .queryParam("fields", CAMPOS)
                    .build()
                    .toUri();
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
}
```

**Tres líneas lógicas, vamos una por una.**

### 4.1 Armar la URI

```java
URI uri =
        UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                .path("/all")
                .queryParam("fields", CAMPOS)
                .build()
                .toUri();
```

Es un **patrón Builder** (vas a ver este patrón formalmente más adelante en DSI). Encadenás métodos y al final llamás `.build()` para obtener el resultado.

**Paso a paso:**

```java
UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
//                                  ↑
//                  "https://restcountries.com/v3.1" (del YAML, recordá Bloque 1+2)
```
Arranca con la URL base configurada.

```java
.path("/all")
```
Agrega el path. Ahora la URL es `https://restcountries.com/v3.1/all`.

```java
.queryParam("fields", CAMPOS)
```
Agrega un query param. Ahora: `https://restcountries.com/v3.1/all?fields=name,capital,region,...`

```java
.build()
```
Compila los componentes en un `UriComponents` (estructura intermedia).

```java
.toUri()
```
Convierte a `URI` final (la clase `java.net.URI`).

### 4.2 Hacer el HTTP request

```java
Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
```

**Es la línea más densa del método.** Lo que pasa por debajo:

1. `RestTemplate` hace **HTTP GET** a la URI.
2. La API responde con un body JSON: `[{...}, {...}, ...]` (un array de países).
3. `RestTemplate` mira el segundo argumento: `Pais[].class`. Le dice a Jackson: "deserializame el body como un array de `Pais`".
4. Jackson hace su trabajo (lo viste en Bloque 3).
5. `RestTemplate` te devuelve el `Pais[]` poblado.

**Notá:** **es UNA línea de código**. Spring + Jackson cubren toda la complejidad de HTTP + serialización.

### 4.3 Manejo defensivo del null

```java
return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
```

**Operador ternario.** Se lee:

> "Si `cuerpo` es null **o** tiene length 0, devolveme `List.of()` (lista vacía inmutable). Si no, devolveme `Arrays.asList(cuerpo)` (el array convertido a List)".

**¿Por qué tanta defensa?**

- `restTemplate.getForObject(...)` **puede devolver `null`** si el servidor responde con HTTP 204 (No Content) — body vacío.
- La API puede responder con array vacío `[]` — `cuerpo.length == 0`.
- En **ambos casos**, queremos devolver **lista vacía**, NUNCA `null`.

**Esa "regla de oro" la viste en Etapa 4 del Proyecto 0:** un método que devuelve `List` **nunca debería devolver `null`** — siempre lista vacía. Te ahorrás que quien llame al método tenga que hacer `if (lista != null) for (...)`.

---

## 🔍 Parte 5: `buscarPorNombre()` — con placeholder

```java
public Optional<Pais> buscarPorNombre(String nombre) {
    URI uri =
            UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                    .path("/name/{nombre}")               // ← placeholder
                    .queryParam("fields", CAMPOS)
                    .buildAndExpand(nombre)                // ← rellena
                    .toUri();
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    if (cuerpo == null || cuerpo.length == 0) {
        return Optional.empty();
    }
    return Optional.of(cuerpo[0]);
}
```

**Dos cosas nuevas respecto a `buscarTodos()`:**

### 5.1 Placeholder `{nombre}` + `buildAndExpand(nombre)`

```java
.path("/name/{nombre}")
.buildAndExpand(nombre)
```

**Esto NO es string concatenation.** No es `"/name/" + nombre`. Es un **placeholder con sustitución segura**:

- `{nombre}` es un marcador.
- `buildAndExpand(nombre)` reemplaza `{nombre}` con el valor real.

**¿Por qué importa?**

Si el usuario busca **"san martín"** (con espacio), o **"españa"** (con tilde), o **"isla de pascua"**, una concatenación directa rompería:

```java
// ❌ Mala forma:
.path("/name/" + nombre)
// Si nombre = "san martín":
// URL = "/name/san martín"  → URL inválida (espacio sin codificar)

// ✅ Buena forma:
.path("/name/{nombre}").buildAndExpand(nombre)
// URL = "/name/san%20mart%C3%ADn"  → URL codificada correctamente
```

**`buildAndExpand` hace URL-encoding automático**. Tildes, espacios, caracteres especiales — todo se escapa correctamente.

**También protege contra "URL injection"** — si alguien pasara `nombre = "../../delete/all"`, el placeholder lo trataría como texto a buscar, no como path.

### 5.2 Devuelve `Optional<Pais>` (no `List<Pais>`)

```java
public Optional<Pais> buscarPorNombre(String nombre) {
    // ...
    if (cuerpo == null || cuerpo.length == 0) {
        return Optional.empty();
    }
    return Optional.of(cuerpo[0]);
}
```

**¿Por qué Optional y no List acá?**

Es exactamente la **regla mental** que viste en Etapa 4 del Proyecto 0:

| ¿Cuántos resultados esperás? | Tipo de retorno |
|---|---|
| 0 o 1 | `Optional<T>` |
| 0 o muchos | `List<T>` |

`buscarPorNombre` espera **uno solo**: dado un nombre específico, hay **a lo sumo un país** que matchea. Por eso `Optional<Pais>`.

Los otros métodos (`buscarPorRegion`, `buscarPorMoneda`, `buscarPorCapital`) esperan **muchos**: una región tiene varios países, una moneda como EUR la usan varios países, etc. Por eso `List<Pais>`.

> **Detalle del código:** `Optional.of(cuerpo[0])` toma **el primer elemento** del array. Si la API devolviera múltiples matches (raro, pero podría), nos quedamos con el primero. La búsqueda "por nombre" en restcountries usualmente devuelve un solo país, pero la API igual responde con un array (por consistencia).

---

## 🔁 Parte 6: Los otros 3 métodos — mismo patrón

```java
public List<Pais> buscarPorMoneda(String codigoMoneda) {
    URI uri =
            UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                    .path("/currency/{codigo}")
                    .queryParam("fields", CAMPOS)
                    .buildAndExpand(codigoMoneda)
                    .toUri();
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
}
```

```java
public List<Pais> buscarPorRegion(String region) {
    URI uri =
            UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                    .path("/region/{region}")
                    .queryParam("fields", CAMPOS)
                    .buildAndExpand(region)
                    .toUri();
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
}
```

```java
public List<Pais> buscarPorCapital(String capital) {
    URI uri =
            UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                    .path("/capital/{capital}")
                    .queryParam("fields", CAMPOS)
                    .buildAndExpand(capital)
                    .toUri();
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
}
```

**Son los tres prácticamente idénticos**. Solo cambian:
- El path: `/currency/{...}` vs `/region/{...}` vs `/capital/{...}`.
- El parámetro: `codigoMoneda` vs `region` vs `capital`.

**Todo el patrón "armar URI + HTTP + null-safe return"** se repite igual.

### ¿Esto es un olor a código?

**Sí.** Hay duplicación. Un desarrollador experimentado refactorizaría para extraer un método genérico:

```java
private List<Pais> buscarConPath(String path, String valor) {
    URI uri =
            UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                    .path(path)
                    .queryParam("fields", CAMPOS)
                    .buildAndExpand(valor)
                    .toUri();
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
}

public List<Pais> buscarPorRegion(String region) {
    return buscarConPath("/region/{valor}", region);
}

public List<Pais> buscarPorCapital(String capital) {
    return buscarConPath("/capital/{valor}", capital);
}
// ...
```

**¿Por qué el profe no lo hizo así?** Probablemente porque a esta altura de la cursada (clase 2) **es más importante que entiendas qué hace cada método** que ver una abstracción. La repetición acá es **deliberada** y **pedagógica** — más adelante en DSI vas a aprender patrones de diseño (Template Method, Strategy, etc.) que abordan exactamente esto.

> Mantenélo en mente: **el código del profe se va a refactorizar más adelante** cuando lleguen los patrones. Vas a ver el "antes y después".

---

## 🛠️ Parte 7: `UriComponentsBuilder` — por qué no concatenar strings

Acabás de ver `UriComponentsBuilder` en acción. Vamos a justificar **por qué no se concatena strings a mano**.

### El antipatrón clásico

```java
String url = "https://restcountries.com/v3.1/name/" + nombre + "?fields=" + CAMPOS;
URI uri = URI.create(url);
```

**Funciona en el feliz caso**. Pero rompe en muchos otros:

1. **Caracteres especiales:** si `nombre` tiene espacios, tildes, signos, la URL queda inválida.
2. **URL injection:** si alguien hostil pasa `nombre = "../../admin?key=secret"`, podría manipular la URL.
3. **Difícil de leer:** una concatenación larga es confusa.
4. **No es composable:** si querés agregar headers, otro query param, condicionales (ej: "solo agregar `lang` si está presente"), el código se vuelve un quilombo de `if` y concat.

### Lo que UriComponentsBuilder te da

1. **URL encoding automático** (tildes, espacios, etc.).
2. **API fluida** — encadenás `.path()`, `.queryParam()`, `.fragment()`, etc.
3. **Inmutabilidad** — cada método devuelve un **nuevo** builder, no muta el actual. Podés tener configuraciones base y derivarlas:

   ```java
   var base = UriComponentsBuilder.fromUriString(baseUrl).queryParam("fields", CAMPOS);
   var uriArgentina = base.path("/name/argentina").build().toUri();
   var uriBrasil = base.path("/name/brasil").build().toUri();
   // base no se ve afectada por las derivadas
   ```

4. **Placeholders con `{}` + `buildAndExpand`** — sustitución segura.

> En cualquier proyecto profesional Java que haga HTTP, **vas a ver `UriComponentsBuilder` o equivalente**. El profe acá te lo está mostrando temprano para que lo internalices.

---

## 🌐 Parte 8: `RestTemplate.getForObject` — la conexión entre todo

```java
Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
```

**Una sola línea, mucho pasa adentro.** Resumen:

1. **HTTP GET** a la URI.
2. Recibe la respuesta del servidor (status + headers + body).
3. Si el status es 4xx o 5xx, **tira excepción** (`HttpClientErrorException`, `HttpServerErrorException`).
4. Si todo OK, toma el body (string JSON).
5. Le pide a Jackson: "deserializame este body como `Pais[]`".
6. Te devuelve el `Pais[]`.

### Otros métodos de RestTemplate (para que reconozcas)

| Método | Qué hace |
|---|---|
| `getForObject(uri, Class)` | GET, devuelve el body deserializado. **Lo que usa el profe.** |
| `getForEntity(uri, Class)` | GET, devuelve `ResponseEntity<T>` (status + headers + body) |
| `postForObject(uri, body, Class)` | POST, devuelve el body deserializado |
| `put(uri, body)` | PUT, no devuelve cuerpo |
| `delete(uri)` | DELETE |
| `exchange(...)` | Genérico, para cualquier método HTTP |

### RestTemplate vs WebClient (para que sepas)

`RestTemplate` es la API **clásica** de Spring para HTTP. Fue diseñada en 2009. Es **bloqueante** (cada llamada espera la respuesta antes de continuar).

`WebClient` es la API **moderna** (Spring 5+, 2017). Es **reactiva** (no bloquea). Devuelve `Mono<T>` o `Flux<T>` en vez de objetos directos.

**El profe usa RestTemplate** porque:
- Es más simple para empezar.
- Sigue siendo perfectamente válido para la gran mayoría de casos.
- Está marcado como "en mantenimiento" pero NO deprecado — funciona y va a seguir funcionando por años.

> En el mundo real, depende del proyecto. Apps de alta concurrencia usan WebClient. Apps "normales" usan RestTemplate y duermen tranquilas.

---

## 🛡️ Parte 9: El patrón "null-safe" en cada return

```java
return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
```

Esta línea aparece **4 veces** en el archivo (los métodos que devuelven `List`). El método `buscarPorNombre` tiene una variante:

```java
if (cuerpo == null || cuerpo.length == 0) {
    return Optional.empty();
}
return Optional.of(cuerpo[0]);
```

**El principio es el mismo en ambos:** **NUNCA devolver `null` cuando el tipo de retorno permite "vacío" como dato válido.**

| Tipo | Cuándo está "vacío" | Qué NO devolver | Qué SÍ devolver |
|---|---|---|---|
| `List<T>` | Sin elementos | `null` | `List.of()` o `new ArrayList<>()` |
| `Optional<T>` | Sin valor | `null` | `Optional.empty()` |
| `String` | Sin texto | `null` | `""` (string vacío) |
| `Map<K, V>` | Sin entradas | `null` | `Map.of()` o `new HashMap<>()` |

**¿Por qué este patrón?**

Le hace **mucho más fácil la vida** a quien llama al método. Mirá:

```java
// CON el patrón null-safe:
for (Pais p : buscador.buscarPorRegion("Marte")) {
    System.out.println(p.getNombre().getComun());
}
// Si la lista está vacía, el loop no se ejecuta. Punto. No crashea.

// SIN el patrón null-safe (si devolviera null):
List<Pais> paises = buscador.buscarPorRegion("Marte");
if (paises != null) {
    for (Pais p : paises) {
        System.out.println(p.getNombre().getComun());
    }
}
// Tenés que chequear null SIEMPRE. Olvidarte → NullPointerException.
```

**La firma del método es una promesa.** Si decís "devuelvo `List<Pais>`", devolvés una lista — vacía o llena, pero lista.

---

## 🆚 Parte 10: Comparación con `CatalogoPaises` del Proyecto 0

| Aspecto | CatalogoPaises (vos) | BuscadorDePaises (profe) |
|---|---|---|
| `@Component` o annotation | No (era plain Java) | Sí |
| Fuente de datos | List<Pais> hardcodeada | API REST |
| Dependencias | Ninguna | `RestTemplate`, `RestCountriesProperties` |
| Inyección | `new` manual | Por constructor (Spring) |
| `buscarTodos()` | `return List.copyOf(this.paises)` | HTTP GET + Jackson |
| `buscarPorNombre()` | Stream sobre lista local | URL + HTTP + parse + Optional |
| Tipo de retorno cuando hay 0 o 1 | `Optional<Pais>` | `Optional<Pais>` |
| Tipo de retorno cuando hay 0 o muchos | `List<Pais>` | `List<Pais>` |
| Patrón null-safe | Ya lo aplicaste | Mismo patrón |

**El esqueleto es idéntico.** La diferencia es solo **de dónde vienen los datos**.

### Lo más importante de la comparación

Cuando lleguen los patrones de diseño en DSI, vas a entender que `CatalogoPaises` y `BuscadorDePaises` cumplen el **mismo rol** (proveer países) y tienen **la misma interfaz**. La diferencia (fuente local vs API) es un **detalle de implementación**. Esto es el principio fundamental de **abstracción**:

> El que **usa** un buscador de países no debería enterarse si los datos vienen de memoria, de archivo, de base de datos o de la web. Solo le importa que pueda pedir "dame Argentina" y recibir un `Pais`.

Este principio es **el corazón del patrón Repository** que vas a estudiar más adelante. **Tu Proyecto 0 ya lo aplica sin que lo nombres formalmente.**

---

## ✅ Checkpoint

Si podés contestar mentalmente estas, estás listo para Bloque 5:

1. ¿Por qué `BuscadorDePaises` tiene `@Component` y `Pais` no?
2. ¿Cuántas instancias de `BuscadorDePaises` crea Spring? ¿Por qué?
3. ¿Quién es responsable de llenar `restTemplate` y `propiedades` en el constructor?
4. ¿Por qué la constante `CAMPOS` es `private static final`?
5. ¿Por qué `UriComponentsBuilder` con placeholder es mejor que concatenar strings?
6. ¿Qué hace `restTemplate.getForObject(uri, Pais[].class)` por debajo?
7. ¿Por qué `buscarPorNombre` devuelve `Optional<Pais>` y `buscarPorRegion` devuelve `List<Pais>`?
8. ¿Por qué los métodos **nunca** devuelven `null`?
9. ¿Qué diferencia hay entre `.build()` y `.buildAndExpand(valor)`?
10. ¿Por qué `Pais[]` (array) y no `List<Pais>` en la línea de `getForObject`?

---

## 🎯 Mini-experimentos mentales

**Caso 1:** Quitás `@Component` de `BuscadorDePaises`.
→ Spring no lo registra como bean. La app arranca igual (si nadie lo necesita) o falla con `NoSuchBeanDefinitionException` al intentar inyectarlo.

**Caso 2:** Cambiás `.path("/name/{nombre}").buildAndExpand(nombre)` por `.path("/name/" + nombre).build()`.
→ Funciona para nombres simples como "argentina". Falla para "san martín" (espacio) o "españa" (tilde) porque la URL no se codifica correctamente.

**Caso 3:** En `buscarPorNombre`, sacás el chequeo `if (cuerpo == null || cuerpo.length == 0)` y dejás directo `return Optional.of(cuerpo[0])`.
→ Si la API devuelve array vacío, `cuerpo.length == 0`. Hacés `cuerpo[0]` → **`ArrayIndexOutOfBoundsException`**. La app crashea.
→ Si la API devuelve null, hacés `null[0]` → **`NullPointerException`**.

**Caso 4:** Cambiás el tipo de retorno de `buscarPorRegion` de `List<Pais>` a `Optional<List<Pais>>`.
→ Funciona técnicamente, pero **es antipatrón** (lo viste en Proyecto 0). Una lista vacía ya representa "no hay resultados", no necesitás envolverla en Optional. Quien usa el método ahora tiene que hacer `.orElse(List.of())` o `.ifPresent(...)` para nada.

**Caso 5:** La API responde con HTTP 503 (Service Unavailable) en medio de una llamada.
→ `RestTemplate` tira `HttpServerErrorException`. El método del profe **no la captura** — propaga la excepción. Quien llamó al `buscadorDePaises` tiene que decidir qué hacer (reintentar, loggear, etc.). Es buen patrón: no capturar lo que no podés manejar.

---

## 🔗 Lo que viene en Bloque 5

El último bloque: **`BuscadorDePaisesIT.java`** — los **tests del profe**. Vas a ver:

- `@SpringBootTest` — el "modo test" de Spring Boot.
- `@Autowired` en tests (acá sí se usa).
- Los 5 tests del profe usando AssertJ con encadenamiento y `extracting`.
- Por qué se llama `IT` (Integration Test) y no `Test`.
- Cómo difiere de los tests unitarios que escribiste en Etapa 7 del Proyecto 0.

**Es el bloque más corto** y vas a leerlo como propio gracias a la Etapa 7.

---

## ▶️ Próximo paso

Cuando estés con los tres bloques (2, 3, 4) procesados, decime **"vamos al bloque 5"** y cerramos el recorrido del código del profe.
