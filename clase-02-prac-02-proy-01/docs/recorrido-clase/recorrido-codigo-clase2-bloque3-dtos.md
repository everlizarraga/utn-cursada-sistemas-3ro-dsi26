# 📘 Bloque 3 — DTOs y Jackson

> **Objetivo:** desarmar los 3 DTOs del proyecto (`Pais`, `NombrePais`, `DetalleMoneda`). Entender qué hace **Jackson**, cómo mapea JSON ↔ Java automáticamente, y por qué cada annotation está donde está.
>
> **Buenas noticias:** este bloque es **más fácil que el Bloque 2**. La mitad ya la sabés del Proyecto 0 (Lombok, `Map<String, DetalleMoneda>`, `Optional`).
>
> **Pre-requisito:** Bloque 2 completo. Idealmente Proyecto 0 hasta Etapa 6 (Maps anidados).

---

## 🧭 Mapa del bloque

1. Qué es un DTO (repaso del Proyecto 0).
2. **Jackson** — el deserializador JSON ↔ Java de Spring Boot.
3. `NombrePais.java` — el más simple, arrancamos por acá.
4. `DetalleMoneda.java` — el patrón se repite.
5. `Pais.java` — el complejo, con composición.
6. Ejemplo de JSON real de la API y cómo se mapea.
7. El flow completo: HTTP → JSON → Jackson → objeto Java.
8. Por qué cada annotation está donde está (resumen).
9. Comparación lado a lado con tu Proyecto 0.

---

## 📦 Parte 1: Qué es un DTO (repaso rápido)

**DTO = Data Transfer Object.** Una clase que solo guarda datos para "moverlos" entre sistemas. Sin lógica de negocio, sin comportamiento — solo getters, setters, y eventualmente un `equals/hashCode/toString`.

Es exactamente lo que vos hiciste en el Proyecto 0 con `Pais`:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pais {
    private String nombre;
    private String capital;
    // ...
}
```

Los DTOs del profe son lo mismo, **pero con dos cosas extra**:

1. **Annotations de Jackson** (`@JsonProperty`, `@JsonIgnoreProperties`) — porque vienen de un JSON externo.
2. **Composición**: un Pais tiene un `NombrePais` adentro, y un `Map<String, DetalleMoneda>`.

---

## 🎭 Parte 2: Jackson — el traductor JSON ↔ Java

**Jackson** es una librería para convertir JSON a objetos Java y viceversa. Está incluida automáticamente en `spring-boot-starter-webmvc` (lo viste en Bloque 1) — no la declarás, ya está ahí.

### El problema que resuelve

La API `restcountries.com` te devuelve esto:

```json
{
    "name": {
        "common": "Argentina",
        "official": "Argentine Republic"
    },
    "capital": ["Buenos Aires"],
    "region": "Americas",
    "population": 45376763
}
```

Tu código Java necesita un objeto `Pais` con campos `nombre`, `capitales`, `region`, `poblacion`. **Jackson hace la traducción automáticamente.**

```
JSON                          Pais (Java)
{                             ┌──────────────────┐
  "name": {...},     ──────►  │ nombre: NombrePais│
  "capital": [...],  ──────►  │ capitales: List   │
  "region": "...",   ──────►  │ region: String    │
  "population": 0    ──────►  │ poblacion: Long   │
}                             └──────────────────┘
```

### Cómo Jackson sabe qué mapear

Por **convención**: si tu campo Java se llama igual al campo JSON, **mapeo automático**. No hace falta annotation.

**Pero hay tres casos donde necesitás ayudarlo:**

1. **Los nombres son distintos** (JSON usa `name`, Java usa `nombre`) → `@JsonProperty("name")`.
2. **El JSON tiene campos que no querés en tu DTO** → `@JsonIgnoreProperties(ignoreUnknown = true)`.
3. **El campo Java es complejo** (objeto anidado, lista, map) → Jackson lo detecta por el tipo y aplica reglas recursivas.

---

## 🔍 Parte 3: `NombrePais.java` — el caso más simple

```java
package ar.edu.utn.ba.ddsi.countries.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NombrePais {

    @JsonProperty("common")
    private String comun;

    @JsonProperty("official")
    private String oficial;
}
```

### El JSON correspondiente

Esto es lo que la API devuelve para `name`:

```json
{
    "common": "Argentina",
    "official": "Argentine Republic",
    "nativeName": {
        "spa": {
            "official": "República Argentina",
            "common": "Argentina"
        }
    }
}
```

Notá que el JSON tiene **3 campos** (`common`, `official`, `nativeName`). El DTO `NombrePais` **solo guarda 2** (`comun`, `oficial`). El tercero se ignora.

### Desarmado de las annotations

**`@JsonIgnoreProperties(ignoreUnknown = true)`**
```java
@JsonIgnoreProperties(ignoreUnknown = true)
```

**Lectura:** "si en el JSON aparece un campo que **no existe** en mi clase Java, **ignoralo** sin romper".

Sin esto, Jackson tira excepción si encuentra `nativeName` en el JSON pero no lo encuentra como atributo en `NombrePais`.

**Esta annotation se pone en TODOS los DTOs que recibís de APIs externas.** Porque:
- La API puede agregar campos nuevos en el futuro.
- Vos solo querés mapear los campos que te importan.
- Sin `ignoreUnknown`, cualquier cambio en la API rompe tu app.

**`@JsonProperty("common")` y `@JsonProperty("official")`**
```java
@JsonProperty("common")
private String comun;

@JsonProperty("official")
private String oficial;
```

**Lectura:** "el campo `comun` (Java) se mapea al campo `common` (JSON). El campo `oficial` (Java) se mapea al campo `official` (JSON)".

Sin `@JsonProperty`, Jackson buscaría en el JSON un campo llamado **exactamente igual al de Java** (`comun` y `oficial`). No lo encontraría — el JSON dice `common`/`official`. **Resultado:** `comun` y `oficial` quedarían `null`.

> **Detalle de estilo:** el profe traduce los nombres al español (`comun`, `oficial` en vez de dejar `common`, `official`). Es opcional. Algunos equipos prefieren mantener los nombres originales del JSON. Es discusión de equipo, no hay una respuesta correcta.

**`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`**

Ya las conocés de Etapa 3 del Proyecto 0. Repaso rápido sobre **por qué Jackson las necesita**:

- **`@NoArgsConstructor` es CRÍTICO.** Jackson construye objetos así:
  1. Crea instancia vacía: `new NombrePais()` — necesita el constructor sin args.
  2. Llama setters: `nombrePais.setComun("Argentina")`, etc. — necesita los setters.

  Sin `@NoArgsConstructor`, Jackson **no puede crear el objeto** y tira excepción.

- **`@Data` aporta los setters** (también los getters, equals, hashCode, toString).

- **`@AllArgsConstructor` NO es estrictamente necesario para Jackson**, pero está bueno tenerlo para construir manualmente en tests o en otros lugares.

> Esto vale la pena recordarlo: **Jackson no usa el constructor con argumentos por default.** Usa el constructor vacío + setters. Por eso `@NoArgsConstructor` es obligatorio.

---

## 🔍 Parte 4: `DetalleMoneda.java` — mismo patrón

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleMoneda {

    @JsonProperty("name")
    private String nombre;

    @JsonProperty("symbol")
    private String simbolo;
}
```

**Es idéntico estructuralmente a `NombrePais`.** Solo cambian los nombres de campos.

### El JSON correspondiente

```json
{
    "name": "Argentine peso",
    "symbol": "$"
}
```

Si entendiste `NombrePais`, entendiste `DetalleMoneda`. **El patrón se repite.**

---

## 🔍 Parte 5: `Pais.java` — el caso compuesto

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

    @JsonProperty("area")
    private Double superficie;

    @JsonProperty("population")
    private Long poblacion;

    @JsonProperty("currencies")
    private Map<String, DetalleMoneda> monedas;

    @JsonProperty("languages")
    private Map<String, String> idiomas;

    @JsonProperty("cca2")
    private String cca2;

    @JsonProperty("cca3")
    private String cca3;
}
```

**10 campos. Las annotations de clase son las mismas que en NombrePais.** Lo nuevo está en los **tipos de los campos**.

### Lo simple (igual a lo que ya viste)

```java
@JsonProperty("region")
private String region;

@JsonProperty("subregion")
private String subregion;

@JsonProperty("cca2")
private String cca2;

@JsonProperty("cca3")
private String cca3;
```

Cuatro Strings. Sin sorpresas.

> **Datos curiosos:** `cca2` y `cca3` son los códigos ISO de país. `cca2` es de 2 letras (`AR`, `BR`, `US`), `cca3` es de 3 letras (`ARG`, `BRA`, `USA`). Útiles para identificar países de forma única.

### Wrappers en vez de primitivos

```java
@JsonProperty("area")
private Double superficie;     // Double (wrapper), NO double

@JsonProperty("population")
private Long poblacion;         // Long (wrapper), NO long
```

**Acordate del Bloque 0:** los wrappers (`Double`, `Long`) **pueden ser null**. Los primitivos (`double`, `long`) no.

**¿Por qué wrappers acá?** Porque la API podría no traer ese dato para un país específico. Si declaramos `long poblacion` (primitivo) y la API no manda el campo, Jackson **no sabría qué poner** — un primitivo no puede ser null. Con `Long`, el campo queda `null` y todo sigue funcionando.

> **Esta es exactamente la lección del Bloque 0 que te marqué para anotar.** En tu Proyecto 0 usaste `long` (primitivo) porque hardcodeabas. Acá usás `Long` (wrapper) porque los datos vienen de afuera y pueden no estar.

### Composición: un objeto adentro de otro

```java
@JsonProperty("name")
private NombrePais nombre;
```

**El tipo es `NombrePais`, no `String`.** Es **composición**: un `Pais` "tiene un" `NombrePais`.

**Cómo Jackson maneja esto:**

Cuando ve este JSON:
```json
"name": {
    "common": "Argentina",
    "official": "Argentine Republic"
}
```

Y ve que el tipo Java es `NombrePais`, Jackson **aplica recursivamente las mismas reglas**:
1. Crea un `NombrePais` vacío con `new NombrePais()`.
2. Lee el sub-JSON `{"common": ..., "official": ...}`.
3. Mapea `common` → `comun` (gracias a `@JsonProperty` en `NombrePais`).
4. Mapea `official` → `oficial`.
5. Termina con un `NombrePais` poblado.
6. Lo asigna a `pais.setNombre(nombrePais)`.

**Jackson navega objetos anidados sin que vos hagas nada.** Solo necesita que el tipo Java exista con sus propias annotations.

### List<String>: lista de Strings

```java
@JsonProperty("capital")
private List<String> capitales;
```

**El tipo es `List<String>`.** ¿Por qué lista y no String?

Mirá el JSON:
```json
"capital": ["Buenos Aires"]
```

**Es un array JSON, no un string.** Aunque tenga **un solo elemento**, sigue siendo un array. La API decidió que un país **podría tener múltiples capitales** (algunos países como Sudáfrica tienen 3 capitales oficiales).

Jackson mapea arrays JSON → `List` (o `Set`, o array Java, dependiendo del tipo declarado).

> **Vas a verlo más cuando uses esto:** en el `BuscadorDePaises`, para imprimir la capital de Argentina hacés `pais.getCapitales().get(0)` o similar — no `pais.getCapital()` (porque es una lista, no un string único).

### Map<String, DetalleMoneda>: el más interesante

```java
@JsonProperty("currencies")
private Map<String, DetalleMoneda> monedas;
```

Mirá el JSON:
```json
"currencies": {
    "ARS": {
        "name": "Argentine peso",
        "symbol": "$"
    }
}
```

**Es un objeto JSON donde las claves son códigos de moneda y los valores son sub-objetos.** Jackson lo mapea a `Map<String, DetalleMoneda>`:

- Las **claves** del JSON (`"ARS"`) son `String`.
- Los **valores** del JSON (`{"name": ..., "symbol": ...}`) se deserializan como objetos `DetalleMoneda`.

**Esto es exactamente lo que hiciste en Etapa 6 del Proyecto 0.** Estructuralmente, **es el mismo Map que vos armaste a mano**:

```java
// En tu Proyecto 0 Etapa 6:
Map.of("ARS", new DetalleMoneda("Argentine peso", "$"))
```

Jackson lo arma automáticamente leyendo del JSON.

### Map<String, String>: idiomas

```java
@JsonProperty("languages")
private Map<String, String> idiomas;
```

JSON:
```json
"languages": {
    "spa": "Spanish"
}
```

Mismo concepto pero más simple: las claves son códigos de idioma, los valores son nombres. Otra estructura que ya construiste en Etapa 6.

---

## 🌐 Parte 6: Ejemplo de JSON real y mapeo completo

Si vas a `https://restcountries.com/v3.1/name/argentina?fields=name,capital,region,population,currencies,languages`, te devuelve algo así:

```json
[
    {
        "name": {
            "common": "Argentina",
            "official": "Argentine Republic",
            "nativeName": {
                "grn": {
                    "official": "Tetã Argentina",
                    "common": "Argentina"
                },
                "spa": {
                    "official": "República Argentina",
                    "common": "Argentina"
                }
            }
        },
        "capital": ["Buenos Aires"],
        "region": "Americas",
        "population": 45376763,
        "currencies": {
            "ARS": {
                "name": "Argentine peso",
                "symbol": "$"
            }
        },
        "languages": {
            "grn": "Guaraní",
            "spa": "Spanish"
        }
    }
]
```

**Es un array** (los `[...]`) con **un objeto adentro**. Por eso en el código del profe vas a ver:

```java
Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
//                                            ^^^^^^^^^^
//                            "esperá un array de Pais"
```

Y después agarra el primer elemento: `cuerpo[0]`. Lo vas a ver en Bloque 4.

### Cómo se mapea cada campo

| JSON | DTO Java | Tipo |
|---|---|---|
| `"name": {...}` | `pais.nombre` (NombrePais) | objeto anidado |
| → `"common": "Argentina"` | `pais.nombre.comun` | String |
| → `"official": "..."` | `pais.nombre.oficial` | String |
| → `"nativeName": {...}` | **ignorado** (no está en DTO) | — |
| `"capital": [...]` | `pais.capitales` | List<String> |
| `"region": "..."` | `pais.region` | String |
| `"population": 0` | `pais.poblacion` | Long |
| `"currencies": {...}` | `pais.monedas` | Map<String, DetalleMoneda> |
| `"languages": {...}` | `pais.idiomas` | Map<String, String> |

**Lo que no está en la tabla** (no aparece en el JSON pero sí en el DTO): `subregion`, `area`, `cca2`, `cca3`. Cuando hagas la query con `?fields=...`, podés pedir esos también. Si no los pedís, quedan `null` en el DTO.

---

## 🔄 Parte 7: El flow completo

Cuando `BuscadorDePaises.buscarPorNombre("argentina")` se ejecuta, pasa esto:

```
1. BuscadorDePaises construye la URL
   → "https://restcountries.com/v3.1/name/argentina?fields=..."
                ↓
2. RestTemplate hace GET HTTP a esa URL
                ↓
3. La API responde con HTTP 200 OK + cuerpo JSON
   → '[{"name":{"common":"Argentina",...},...}]'
                ↓
4. RestTemplate recibe el body como String
                ↓
5. RestTemplate detecta que el target type es Pais[]
                ↓
6. Internamente invoca Jackson:
   "Tomá este String JSON y convertilo a Pais[]"
                ↓
7. Jackson:
   - Ve que es un array JSON → crea Pais[]
   - Por cada objeto:
     a. new Pais()
     b. Lee "name" → ve que el tipo es NombrePais
        → new NombrePais()
        → asigna common → comun, official → oficial
        → pais.setNombre(nombrePais)
     c. Lee "capital" → es un array → crea ArrayList<String>
     d. Lee "currencies" → es un objeto → crea HashMap<String, DetalleMoneda>
        → para cada entrada, deserializa el value como DetalleMoneda
     e. ... etc para cada campo
   - Termina con un Pais completamente poblado
                ↓
8. RestTemplate devuelve el Pais[] al BuscadorDePaises
                ↓
9. BuscadorDePaises lo envuelve en Optional<Pais> y lo retorna
```

**Cada uno de esos pasos es código Java normal corriendo en runtime.** Jackson hace reflection para navegar las clases y mapear. Es la misma idea que Spring (annotation processing en runtime) pero aplicada a JSON.

---

## 🎯 Parte 8: Por qué cada annotation está donde está

Resumen de las annotations que viste en este bloque:

| Annotation | Dónde se pone | Para qué |
|---|---|---|
| `@JsonIgnoreProperties(ignoreUnknown = true)` | Sobre la clase | Ignorar campos del JSON que no están en el DTO |
| `@JsonProperty("nombreEnJson")` | Sobre el atributo | Mapear nombre del JSON al nombre Java |
| `@Data` (Lombok) | Sobre la clase | Generar getters, setters, toString, equals, hashCode |
| `@NoArgsConstructor` (Lombok) | Sobre la clase | **Crítico** — Jackson lo usa para construir objetos |
| `@AllArgsConstructor` (Lombok) | Sobre la clase | Útil para construir manualmente (tests) |

**El combo "ideal" para un DTO de API externa:**

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiDto {
    @JsonProperty("campo_del_json")
    private String campoEnJava;
}
```

Es la receta. **Vas a ver este patrón mil veces** en proyectos Spring.

---

## 🆚 Parte 9: Comparación con tu Proyecto 0

| Aspecto | Proyecto 0 (Etapa 6) | rest-paises Bloque 3 |
|---|---|---|
| Lombok | `@Data + @NoArgsConstructor + @AllArgsConstructor` | **Idéntico** |
| Composición | `Map<String, DetalleMoneda>` en `Pais` | **Idéntico** |
| `DetalleMoneda` | Tu versión con `nombre` + `simbolo` | **Idéntica estructura** |
| Tipos numéricos | `long poblacion` (primitivo) | `Long poblacion` (wrapper) — para soportar null |
| Source de datos | Hardcodeado en el constructor | JSON parseado por Jackson |
| Annotations Jackson | No tenía | `@JsonProperty`, `@JsonIgnoreProperties` |

**Resumen visual:**

```java
// TU Pais del Proyecto 0:
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

// Pais del profe:
@JsonIgnoreProperties(ignoreUnknown = true)    // ← nuevo: ignorar campos extra del JSON
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pais {
    @JsonProperty("name")                        // ← nuevo: mapear nombre JSON
    private NombrePais nombre;                   // ← cambió: objeto, no String
    
    @JsonProperty("capital")
    private List<String> capitales;              // ← cambió: lista, no String
    
    @JsonProperty("region")
    private String region;
    
    @JsonProperty("population")
    private Long poblacion;                      // ← cambió: wrapper, no primitivo
    
    @JsonProperty("currencies")
    private Map<String, DetalleMoneda> monedas;
    
    @JsonProperty("languages")
    private Map<String, String> idiomas;
    
    // + algunos campos extra (subregion, cca2, cca3, area)
}
```

**Las diferencias son adaptaciones a la realidad de la API.** El **alma de la clase es la misma.**

---

## ✅ Checkpoint

Si podés contestar mentalmente estas, estás listo para Bloque 4:

1. ¿Qué hace `@JsonIgnoreProperties(ignoreUnknown = true)` y por qué es importante?
2. ¿Por qué `@NoArgsConstructor` es **crítico** para que Jackson funcione?
3. ¿Cómo sabe Jackson que el campo `nombre` Java corresponde a `name` del JSON?
4. ¿Por qué `Pais.poblacion` es `Long` (wrapper) y no `long` (primitivo)?
5. ¿Por qué `Pais.capitales` es `List<String>` y no `String`?
6. ¿Qué pasa cuando Jackson encuentra `"name": {...}` y el campo Java es `NombrePais`?
7. ¿Cómo se mapea un objeto JSON con claves dinámicas (como `currencies`) a Java?
8. ¿Tu DTO necesita `@Component` para ser usado en `BuscadorDePaises`?

---

## 🎯 Mini-experimentos mentales

**Caso 1:** Borrás `@JsonIgnoreProperties(ignoreUnknown = true)` de `Pais`.
→ Jackson encuentra `nativeName`, `independent`, `unMember`, `currencies`, etc. en el JSON. Cualquier campo que no esté en tu DTO **dispara `UnrecognizedPropertyException`**. La deserialización falla.

**Caso 2:** Borrás `@NoArgsConstructor` y dejás solo `@AllArgsConstructor`.
→ Cuando Jackson intenta `new Pais()`, no encuentra el constructor sin args. Tira `InvalidDefinitionException`. La deserialización falla.

> Detalle: existe `@JsonCreator` para que Jackson use un constructor con args, pero requiere más annotations y es más verboso. La forma idiomática es tener el no-args constructor.

**Caso 3:** Cambiás `@JsonProperty("name")` por `@JsonProperty("nombre")` en `Pais`.
→ Jackson busca `"nombre"` en el JSON. **No lo encuentra** (el JSON tiene `"name"`). `pais.nombre` queda `null`. La app no crashea, pero los nombres salen vacíos.

**Caso 4:** Declarás `private long poblacion` (primitivo) en vez de `Long`.
→ Si la API trae `"population": 45376763`, **funciona** (Jackson hace autoboxing).
→ Si la API **no manda** el campo `population`, Jackson deja `poblacion = 0` (valor default de `long`). Vas a pensar que el país tiene 0 habitantes cuando en realidad **la API no mandó el dato**.

> Esta es la razón filosófica para usar wrappers en DTOs: distinguir "no hay dato" (null) de "el dato es 0" (0). Con primitivos perdés esa distinción.

**Caso 5:** Cambiás `Map<String, DetalleMoneda>` por `List<DetalleMoneda>` en el campo `monedas`.
→ Jackson recibe `"currencies": {"ARS": {...}}` (un objeto, no un array). **Tira excepción** porque `Map ≠ List`. El JSON tiene **claves dinámicas** (códigos de moneda), no un array — el tipo Java tiene que ser un Map.

---

## 🔗 Lo que viene en Bloque 4

Vamos a desarmar **`BuscadorDePaises.java`** — el corazón funcional del proyecto. Vas a ver:

- Cómo Spring inyecta `RestTemplate` y `RestCountriesProperties` por constructor (lo que predecimos en Bloque 2).
- Cómo se construye una URI con `UriComponentsBuilder`.
- Cómo `RestTemplate.getForObject(...)` hace HTTP GET y deserializa el JSON.
- Los **5 métodos de búsqueda** del profe (`buscarTodos`, `buscarPorNombre`, `buscarPorMoneda`, `buscarPorRegion`, `buscarPorCapital`).
- El manejo de `Optional<Pais>` igual al que vos hiciste en Etapa 4 del Proyecto 0.

**Es el bloque donde todo lo aprendido en los Bloques 1, 2 y 3 se aplica junto.** Es el más jugoso.

---

## ▶️ Próximo paso

Cuando termines con el Bloque 2 y este, decime **"vamos al bloque 4"** y arrancamos con el servicio.

Si tenés dudas conceptuales de Jackson o de los DTOs, esta es la oportunidad de aclararlas.
