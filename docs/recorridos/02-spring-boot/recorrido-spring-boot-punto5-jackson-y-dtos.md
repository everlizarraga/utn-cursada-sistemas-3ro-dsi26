# Recorrido Spring Boot — Punto 5
## De JSON a objetos: Jackson y los DTOs

---

## 📄 Sobre este documento

**Qué cubre:** por qué en JavaScript un JSON se convierte en objeto gratis y en Java hay que **declarar su forma de antemano**. Qué es un DTO. Las tres anotaciones de Jackson del repo (`@JsonProperty`, `@JsonIgnoreProperties`) y por qué Jackson depende del `@NoArgsConstructor` de Lombok. Cierra con el bug que el profe cometió en vivo y lo que enseña.

**Qué NO cubre:** el camino inverso (convertir tus objetos **a** JSON para exponer una API). Eso es la clase 4. Acá solo **recibimos**.

**Cuánto es:** denso en código, liviano en conceptos. Es un solo mecanismo repetido tres veces.

---

## 🎒 De dónde venís

- **Puntos 1-4:** el contenedor, los beans, la inyección, la configuración. Ya sabés quién crea qué y de dónde salen los valores.
- **De preclase02:** Java, tipos, `Map`, `List`, y **Lombok** (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`). Acá no lo re-explicamos: usamos un ángulo nuevo de algo que ya conocés.
- **De tu entrenamiento en JS:** `fetch()`, `JSON.parse()`, `await res.json()`. Es el ancla de todo el punto.
- **Recordá del Punto 2:** los DTOs **no son beans**. Están en el paquete escaneado pero no tienen `@Component`. Hoy vas a entender por qué.

---

## 1. Lo que en JS es gratis 🔴

Empecemos por lo que ya sabés hacer dormido:

```javascript
// JavaScript — consumir la MISMA API que usa el repo del profe
const res  = await fetch("https://restcountries.com/v3.1/name/argentina");
const data = await res.json();          // 👈 ACÁ. Una línea.

console.log(data[0].name.common);       // "Argentina"
console.log(data[0].capital[0]);        // "Buenos Aires"
console.log(data[0].population);        // 45376763
// Resultado esperado: los tres valores impresos, sin haber declarado NADA.
```

**Nunca le dijiste a JavaScript qué forma tiene la respuesta.** `res.json()` te devuelve un objeto que se adapta a lo que vino. Si el JSON traía 40 campos, el objeto tiene 40 campos. Si mañana la API agrega uno, aparece solo.

Eso es posible porque **JavaScript es de tipado dinámico**: los objetos se arman en tiempo de ejecución, con la forma que tenga el dato.

**Java no puede hacer eso.** Java es de **tipado estático**: el compilador tiene que saber, *antes de correr el programa*, que existe una clase `Pais` con un atributo `nombre` de tipo `NombrePais`. Si escribís `pais.getNombre()` y esa clase no lo declaró, **no compila**. Ni siquiera llega a ejecutarse.

> **El precio y la ganancia, sin vueltas:**
>
> | | JavaScript | Java |
> |---|---|---|
> | ¿Declarás la forma del JSON? | ❌ No | ✅ **Sí, obligatorio** |
> | Escribir un campo mal (`pais.nombr`) | `undefined` en runtime. Silencioso. | **No compila.** El IDE te lo marca en rojo. |
> | La API cambia y saca un campo | Te enterás en producción, con un `undefined` | Te enterás al testear, o antes |
> | Autocompletado del IDE | Ninguno (no sabe qué campos hay) | Total (los campos están declarados) |
>
> **En Java pagás por adelantado escribiendo el molde. Lo que comprás es que el compilador te cubra las espaldas.**

Y ese molde tiene nombre.

---

## 2. Qué es un DTO 🔴

> **DTO — Data Transfer Object.** Una clase cuyo único trabajo es **transportar datos**. Sin lógica, sin decisiones, sin comportamiento. Solo atributos.
>
> En este repo, los DTOs son el **molde con la forma del JSON** que devuelve la API. Escobar los llamó exactamente así en clase: *"objetos moldes"*.

Es la respuesta a la pregunta que quedó abierta en el Punto 2 (*"¿por qué `Pais` no lleva `@Component`?"*):

| | Un **bean** (`BuscadorDePaises`) | Un **DTO** (`Pais`) |
|---|---|---|
| Qué es | Un **servicio**: hace cosas | Un **dato**: transporta cosas |
| ¿Tiene lógica? | Sí (los métodos de búsqueda) | **No.** Solo atributos. |
| ¿Cuántas instancias hay? | **Una** (singleton), viva toda la app | **250**, una por país — creadas y descartadas en cada llamada |
| ¿Quién lo crea? | Spring, al arrancar | **Jackson**, en cada respuesta HTTP |
| ¿Lleva `@Component`? | ✅ Sí | ❌ **No, jamás** |

En clase alguien preguntó *"¿a `Pais` no se le pone `@Component`?"* y Escobar contestó *"no, todavía no"*. La respuesta completa es: **nunca**. Ponerle `@Component` a un DTO significaría "Spring, creá **un** `Pais` al arrancar y compartilo con todos" — que es exactamente lo contrario de lo que un DTO es.

⚠️ Ojo con el nombre "DTO": Escobar lo tiró y frenó (*"no le den bolilla a esto que se llama DTO todavía"*). El concepto tiene más filo del que necesitás hoy — vas a volver a él cuando la cátedra hable de **capas** en la clase 4, y otra vez cuando aparezca la diferencia entre un DTO y un objeto de dominio. Por ahora: **un DTO es el molde con la forma del JSON**. Con eso alcanza.

---

## 3. El JSON real, y el molde que lo espeja 🔴

Esto es lo que devuelve la API cuando le pedís Argentina, **recortado a los campos que el repo pide** (los de la constante `CAMPOS`):

```json
[                                       ← ⚠️ ARRAY. Siempre. Aunque venga un solo país.
  {
    "name": {                           ← objeto anidado
      "common":   "Argentina",
      "official": "Argentine Republic"
    },
    "capital":   ["Buenos Aires"],      ← ⚠️ array de strings, no un string suelto
    "region":    "Americas",
    "subregion": "South America",
    "area":      2780400.0,             ← decimal
    "population": 45376763,             ← entero grande
    "currencies": {                     ← ⚠️ objeto con CLAVES VARIABLES
      "ARS": { "name": "Argentine peso", "symbol": "$" }
    },
    "languages": {                      ← ídem: claves variables
      "grn": "Guaraní",
      "spa": "Spanish"
    },
    "cca2": "AR",
    "cca3": "ARG"
  }
]
```

*(Los valores numéricos son ilustrativos y la API los actualiza; la **estructura** es la del repo.)*

**Tres rarezas que definen el molde entero:**

1. **La respuesta es siempre un array** (`[...]`), aunque busques un solo país. Por eso el repo trabaja con `Pais[]` y no con `Pais`.
2. **`capital` es un array de strings**, no un string. Un país puede tener más de una capital (Bolivia, Sudáfrica). Por eso el atributo se llama `capitales`, en plural.
3. **`currencies` y `languages` son objetos con claves impredecibles.** La clave es `"ARS"` para Argentina, `"PEN"` para Perú. No podés declarar un atributo por cada moneda del mundo. **Eso es exactamente para lo que existe `Map`** (que viste en preclase02): claves que no conocés de antemano.

Ahora mirá el molde principal, y compará campo por campo:

```java
package ar.edu.utn.ba.ddsi.countries.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;   // ← Jackson, no Spring
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)   // ← sección 5
@Data                                          // ← Lombok: getters + setters
@NoArgsConstructor                             // ← Lombok: constructor vacío  → sección 6 (CLAVE)
@AllArgsConstructor                            // ← Lombok: constructor con todo
public class Pais {

    @JsonProperty("name")                      // el JSON lo llama "name"...
    private NombrePais nombre;                 // ...yo lo llamo "nombre". Tipo: OTRO DTO.

    @JsonProperty("capital")
    private List<String> capitales;            // array de strings → List<String>

    @JsonProperty("region")
    private String region;

    @JsonProperty("subregion")
    private String subregion;

    @JsonProperty("area")
    private Double superficie;                 // 2780400.0 → Double

    @JsonProperty("population")
    private Long poblacion;                    // 45376763 → Long (no Integer: puede pasarse)

    @JsonProperty("currencies")
    private Map<String, DetalleMoneda> monedas;
    // ↑ EL MÁS INTERESANTE. "ARS" es la clave (String), y su valor es OTRO DTO.
    //   Map porque la clave es impredecible: no sabés qué moneda va a venir.

    @JsonProperty("languages")
    private Map<String, String> idiomas;       // "spa" → "Spanish". Clave y valor, strings.

    @JsonProperty("cca2")
    private String cca2;                       // código ISO de 2 letras: "AR"

    @JsonProperty("cca3")
    private String cca3;                       // código ISO de 3 letras: "ARG"
}
```

**Cero métodos escritos.** Diez atributos y cuatro anotaciones. Eso es un DTO.

Los otros dos son el mismo patrón, más chicos:

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor
public class NombrePais {                      // ← el molde del objeto "name" anidado

    @JsonProperty("common")
    private String comun;                      // "Argentina"     ← el nombre de uso corriente

    @JsonProperty("official")
    private String oficial;                    // "Argentine Republic"  ← el nombre formal
}
```

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor
public class DetalleMoneda {                   // ← el molde del VALOR de cada entrada de "currencies"

    @JsonProperty("name")
    private String nombre;                     // "Argentine peso"

    @JsonProperty("symbol")
    private String simbolo;                    // "$"
}
```

**La estructura de los DTOs es un espejo de la estructura del JSON.** Objeto anidado en el JSON → otro DTO en Java. Array → `List`. Objeto de claves variables → `Map`.

```
        JSON                                    JAVA
   ┌──────────────┐                      ┌──────────────────┐
   │ [ {          │                      │  Pais[]          │
   │   name: {…}  │ ──────────────────►  │    nombre     : NombrePais    │
   │   capital:[] │                      │    capitales  : List<String>  │
   │   currencies:│                      │    monedas    : Map<String,   │
   │      { ARS:{}│                      │                    DetalleMoneda>
   │      }       │                      │    …             │
   │ } ]          │                      └──────────────────┘
   └──────────────┘
```

---

## 4. `@JsonProperty` — el puente entre dos idiomas 🔴

```java
@JsonProperty("name")
private NombrePais nombre;
```

**Traducción literal:** *"Jackson: el campo que en el JSON viene como `name`, metelo en este atributo, que yo llamo `nombre`."*

**Por qué hace falta:** el JSON está en inglés (lo escribió quien hizo la API) y el código está en castellano (lo escribió el profe). Son dos idiomas distintos y **nadie los va a alinear por vos**. Escobar lo explicó exactamente así: *"no hay una correlación directa entre lo que yo estoy escribiendo en el código con lo que va a venir de la API… es un match lo que estamos haciendo acá"*.

**Cuándo NO hace falta:** cuando el nombre coincide. En clase preguntaron esto y la respuesta fue sí:

```java
// Si el atributo se llama IGUAL que el campo del JSON, la anotación es redundante:
private String region;                    // ✅ el JSON trae "region" → matchea solo

@JsonProperty("region")                   // ✅ hace exactamente lo mismo. Es explícito, no necesario.
private String region;
```

En el repo, `region`, `subregion`, `cca2` y `cca3` **coinciden** con el JSON: sus `@JsonProperty` son redundantes. El profe los puso igual, y hace bien: **todos los atributos anotados** es más fácil de leer que *"algunos sí y otros no, adiviná cuáles"*. Consistencia > brevedad.

⚠️ **Es de Jackson, no de Spring.** Cuando en clase alguien la llamó "decorator del framework", Escobar corrigió: *"no es parte del framework de Springboot, sino que en realidad es parte de una biblioteca que se llama **Jackson**"*.

> **Jackson en una línea:** la biblioteca estándar de Java para convertir JSON ↔ objetos. Es el `JSON.parse` / `JSON.stringify` de Java, con esteroides. **No está en el `pom.xml`** — entra sola, como dependencia transitiva del starter web. Es la auto-configuración de Spring Boot en acción (madriguera del Punto 1).

---

## 5. `@JsonIgnoreProperties(ignoreUnknown = true)` — el chaleco antibalas 🔴

```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pais { ... }
```

**Traducción:** *"Jackson: si en el JSON viene un campo que yo no declaré, ignoralo. No te quejes."*

**Y sin esta anotación, Jackson SE QUEJA.** Fuerte: lanza una excepción y te tira el programa abajo.

Pensá lo que eso significa acá. La API de REST Countries devuelve **decenas de campos** por país: `flags`, `maps`, `timezones`, `borders`, `tld`, `idd`, `demonyms`, `translations`, `coatOfArms`, `car`, `gini`... El repo declara **diez**. Sin `ignoreUnknown = true`, el primer campo no declarado que llegara reventaría todo.

> **Por qué esto importa más allá del repo:** una API externa **no te avisa** cuando agrega un campo. Un día agregan `"currency_symbol_position"` y, si tu DTO fuera estricto, tu aplicación en producción **empieza a fallar sin que vos hayas tocado una línea de código**. Con `ignoreUnknown = true`, ni te enterás. **Es el seguro contra los cambios de un servidor que no controlás.**

*(El repo pide solo los campos que necesita vía el query param `fields`, así que el JSON ya viene recortado. Pero la anotación está igual, y está bien que esté: es defensa en profundidad — no dependés de que el servidor te obedezca.)*

---

## 6. Por qué Jackson necesita el `@NoArgsConstructor` de Lombok 🔴

Esta es **la conexión que nadie explica**, y es la que convierte a Lombok de "azúcar sintáctico" en "requisito técnico".

**Pregunta:** Jackson recibe un texto JSON y tiene que devolverte un objeto `Pais` lleno. ¿Cómo lo hace?

**Podrías pensar:** "usa el constructor con todos los parámetros, `@AllArgsConstructor`". Es lo intuitivo. **Y no es lo que hace.**

### `// ¿CÓMO FUNCIONA?` — el algoritmo real de Jackson

```
 1. Jackson recibe el texto JSON:  { "name": {...}, "capital": [...], "region": "Americas", ... }

 2. Necesita crear un objeto Pais. Pero NO SABE en qué orden van los parámetros
    del constructor, ni si el JSON los trae todos, ni si vienen en el mismo orden.
    (En JSON el orden de las claves NO significa nada.)

 3. Entonces hace lo más simple que puede hacer:

    a) Crea un Pais VACÍO:            new Pais()        👈 ¡EL CONSTRUCTOR SIN ARGUMENTOS!
                                                            → lo genera @NoArgsConstructor

    b) Recorre el JSON campo por campo, y por CADA UNO llama al SETTER:
                                      pais.setNombre(...)     👈 ¡LOS SETTERS!
                                      pais.setCapitales(...)     → los genera @Data
                                      pais.setRegion(...)
                                      ...

    c) Devuelve el objeto ya lleno.
```

**Ahora se entienden las dos anotaciones de Lombok como lo que son — engranajes obligatorios:**

| Anotación de Lombok | Qué genera | Por qué Jackson la NECESITA |
|---|---|---|
| `@NoArgsConstructor` | `public Pais() {}` | **El paso 3a.** Sin constructor vacío, Jackson no puede crear el objeto. → excepción. |
| `@Data` | getters + **setters** | **El paso 3b.** Sin setters, Jackson crea el objeto vacío y **no puede llenarlo**. → todos los campos en `null`. |
| `@AllArgsConstructor` | `public Pais(todos...)` | **Jackson NO la usa.** Está para que *vos* puedas crear un `Pais` a mano en un test. |

> ⚠️ **La trampa de Java que hace esto obligatorio.** Si una clase **no declara ningún constructor**, Java le regala uno vacío. Pero **en cuanto declarás uno con parámetros, el regalo desaparece.**
>
> Y `@AllArgsConstructor` **declara uno con parámetros**. Entonces:
>
> ```java
> @Data
> @AllArgsConstructor          // ← declara Pais(nombre, capitales, region, ...)
> // @NoArgsConstructor        ← ⚠️ SI FALTA ESTA LÍNEA...
> public class Pais { ... }
> // ...el constructor vacío YA NO EXISTE (lo pisó el @AllArgsConstructor)
> // → Jackson no puede hacer new Pais()
> // → 💥 InvalidDefinitionException: "cannot construct instance of Pais"
> ```
>
> En clase, Alejandro preguntó justamente esto (*"en Java no existe un constructor implícito?"*) y Escobar contestó, apurado pero correcto: *"pasa que estos se pisan si no los pongo a los dos. Uno pisa al otro, así que tengo que tener cuidado"*.
>
> **Traducción completa: `@NoArgsConstructor` no está por gusto. Está para reponer el constructor vacío que `@AllArgsConstructor` te robó — y sin el cual Jackson no funciona.**

**Y ahora cerrás también el cabo del Punto 4:** ¿te acordás que `RestCountriesProperties` también lleva `@Data`, y te dije que era por los setters? **Spring llena las properties con el mismo mecanismo:** crea el objeto vacío y lo llena a setterazos. Es literalmente el mismo algoritmo. Por eso las dos clases que "solo transportan datos" —`RestCountriesProperties` y los tres DTOs— llevan `@Data` **por obligación**, no por comodidad.

---

## 7. El bug en vivo: la lección más valiosa de la clase 🟡

Esto pasó de verdad, y el profe lo debuggeó en pantalla.

Escobar escribió el DTO, corrió el test, y **falló**:

```
AssertionError: expecting not blank but was null
```

El objeto había llegado. La lista tenía 250 países. Pero `getNombre().getComun()` devolvía **`null`**.

Miró el código y encontró el error:

```java
@JsonProperty("comon")     // ❌ escribió "comon"  (una sola M)
private String comun;

// el JSON trae:  "common"  (dos M)
```

**Un typo. Una letra.** Lo corrigió, corrió de nuevo, y pasó.

### Por qué esto vale más que el bug 🔴

**Frená y pensá: ¿por qué el programa no explotó?**

Jackson buscó un campo llamado `"comon"` en el JSON. No lo encontró. **Y no dijo nada.** Dejó el atributo en su valor por defecto (`null`) y siguió como si nada.

Y no lo dijo **por la anotación de la sección 5**: `@JsonIgnoreProperties(ignoreUnknown = true)` le ordena tolerar campos que no matchean. Jackson te está obedeciendo.

> 🔴 **La lección que te va a ahorrar horas: el mismo chaleco antibalas que te protege de los cambios de la API, te esconde tus propios typos.**
>
> No hay forma de tener las dos cosas. Es un **trade-off** deliberado:
>
> | | Con `ignoreUnknown = true` | Sin él (estricto) |
> |---|---|---|
> | La API agrega un campo | ✅ Sigue andando | 💥 Explota en producción |
> | Escribís mal un `@JsonProperty` | ⚠️ **`null` silencioso** | 💥 Explota (te enterás al toque) |
>
> **Todo el mundo elige `ignoreUnknown = true`**, porque un campo nuevo en la API es más probable que un typo, y romper producción es peor que un `null`. Pero **pagás con este bug**, y lo vas a pagar más de una vez.
>
> **📌 Grabate el síntoma:** *"un campo del DTO viene en `null` y todos los demás vienen bien"* → **es casi siempre un `@JsonProperty` mal escrito.** No es la API, no es la red, no es Spring. Es una letra.

**Y mirá cómo lo debuggeó, porque el método es transferible:** no se puso a leer el código buscando el error. Hizo un `System.out.println` de la lista y **miró qué había llegado realmente**. Vio los 250 objetos, con `common: null` y `official` lleno. Ahí supo que el problema no era la conexión (los datos llegaban) sino **el mapeo de ese campo puntual**. En 10 segundos tenía el bug acorralado.

> **Bisecar antes de leer.** Primero determinás *dónde* está el problema (¿llegó el dato? ¿llegó vacío? ¿llegó mal mapeado?), y recién después mirás el código de esa zona. Buscar un typo leyendo 40 líneas es lento; imprimir lo que llegó es instantáneo.

---

## 8. El flujo completo 🔴

```
   ┌─────────────────────────────────────────────────────────────────┐
   │  API REST Countries devuelve TEXTO PLANO:                       │
   │                                                                 │
   │  '[{"name":{"common":"Argentina",...},"capital":["Buenos ...    │
   └────────────────────────────┬────────────────────────────────────┘
                                │  Un String. Nada más. Sin tipos.
                                ▼
   ┌─────────────────────────────────────────────────────────────────┐
   │                          JACKSON                                │
   │                                                                 │
   │   ① new Pais()                    ← @NoArgsConstructor (Lombok) │
   │   ② lee "name" del JSON                                         │
   │      busca quién lo reclama       ← @JsonProperty("name")       │
   │      pais.setNombre(...)          ← setter de @Data (Lombok)    │
   │   ③ ¿campo desconocido?  → lo ignora  ← @JsonIgnoreProperties   │
   │   ④ repite por cada campo, y baja recursivamente a los DTOs     │
   │      anidados (NombrePais, DetalleMoneda)                       │
   └────────────────────────────┬────────────────────────────────────┘
                                ▼
   ┌─────────────────────────────────────────────────────────────────┐
   │  Pais[] — objetos Java TIPADOS, con autocompletado en el IDE    │
   │                                                                 │
   │  pais.getNombre().getComun()   → "Argentina"                    │
   │  pais.getCapitales().get(0)    → "Buenos Aires"                 │
   │  pais.getMonedas().get("ARS")  → DetalleMoneda("Argentine peso","$")
   └─────────────────────────────────────────────────────────────────┘
```

> 📌 **Para el parcial, si te preguntan: "¿Qué es un DTO y para qué se usa?"**
>
> Un DTO (*Data Transfer Object*) es un objeto cuya única responsabilidad es transportar datos entre componentes o sistemas, sin lógica de negocio. En el consumo de una API REST se usa como molde de la respuesta: define en el lenguaje tipado la estructura del JSON que va a llegar, permitiendo que la biblioteca de serialización (Jackson, en Java) convierta el texto recibido en objetos con tipos verificables por el compilador.

> 🕳️ **Madriguera — El camino inverso: serializar**
> Jackson hace las dos direcciones. Acá lo usamos para **deserializar** (JSON → objeto). Cuando **expongas** tu propia API REST, va a hacer lo contrario: tomar tu objeto Java y convertirlo en el JSON que devolvés al cliente. Mismas anotaciones, sentido inverso.
> **📍 Dónde cae:** **clase 4** — *"Exposición de APIs desde proyecto Java sobre framework SpringBoot"*.
> *Volvé al camino.*

> 🕳️ **Madriguera — DTO vs objeto de dominio**
> Hay una discusión seria detrás de la frase de Escobar (*"no le den bolilla a esto que se llama DTO todavía"*): ¿el objeto que devuelve tu API tiene que ser el mismo que modela tu negocio? La respuesta profesional es **no** — se separan, y por buenas razones. Es una decisión de diseño con nombre y consecuencias.
> **📍 Dónde cae:** empieza en la **clase 4** (capas) y se profundiza en el **modelado de dominio** de las clases 3, 6, 8 y 13.
> *Volvé al camino — hoy, DTO = molde del JSON.*

> 🕳️ **Madriguera — `@Entity` de JPA usa el mismo truco**
> Cuando en la clase 20 aparezca Hibernate, vas a ver clases con `@Entity`, `@Id`, `@Column`, `@OneToMany`. Sin lógica, solo atributos anotados. **Es exactamente el mismo patrón que estos DTOs**: una clase-molde cuyas anotaciones le dicen a una biblioteca cómo mapear un formato externo (ahí, tablas de una base de datos; acá, un JSON) contra objetos Java. Si entendiste este punto, la mitad de la clase 20 ya la tenés.
> **📍 Dónde cae:** **clases 18-21** — persistencia, ORM, Hibernate.
> *Volvé al camino — pero acordate de esta madriguera. Es un regalo.*

---

## ✅ Checkpoint — Punto 5

1. ¿Por qué en JavaScript `await res.json()` alcanza y en Java hay que declarar una clase? ¿Qué ganás y qué pagás?
2. ¿Qué es un DTO? Nombrá **tres** diferencias concretas con un bean.
3. ¿Por qué `Pais` NO lleva `@Component`? (Ahora podés contestarlo del todo.)
4. Explicá el algoritmo de Jackson en tres pasos: ¿cómo construye un `Pais` a partir del texto JSON?
5. **La pregunta clave:** ¿por qué `Pais` necesita `@NoArgsConstructor` **y** `@Data`? ¿Qué pasaría si sacaras cada una?
6. Si sacás `@NoArgsConstructor` pero dejás `@AllArgsConstructor`, ¿por qué se rompe? (Pista: es una regla de Java, no de Lombok.)
7. ¿Qué hace `@JsonIgnoreProperties(ignoreUnknown = true)` y qué pasaría sin ella cuando la API agrega un campo nuevo?
8. Escribís `@JsonProperty("populaton")` (con un typo). ¿El programa explota? ¿Qué valor termina teniendo el atributo, y por qué?
9. ¿Por qué `currencies` se mapea a un `Map<String, DetalleMoneda>` y no a una clase con un atributo por moneda?
10. ¿Por qué `capital` es una `List<String>` y no un `String`?

---

## 🎯 Qué viene en el Punto 6

Ya tenés **todas las piezas**: el contenedor, los beans, la inyección, la configuración y los moldes.

Falta **armar el rompecabezas**: la llamada HTTP real. `RestTemplate`, `UriComponentsBuilder`, `getForObject`, y los cinco métodos de `BuscadorDePaises` línea por línea. Ahí se cierra el hilo del Punto 2 (*"¿qué hace `RestTemplate`?"*) y el del Punto 4 (*"¿tanto lío para no escribir una URL?"*).

Y termina con el test: `@SpringBootTest`, AssertJ, y **el sufijo `IT`** — que esconde el problema que la cátedra resuelve en la clase 4. Vas a *sentir* que algo está mal antes de que nadie te lo diga.

Al terminarlo, abrís `rest-paises` y **no queda una línea sin explicación**.

---

**FIN DEL PUNTO 5**
