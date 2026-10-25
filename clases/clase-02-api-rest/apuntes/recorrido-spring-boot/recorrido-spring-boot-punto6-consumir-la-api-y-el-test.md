# Recorrido Spring Boot — Punto 6
## Consumir la API: `RestTemplate`, `UriComponentsBuilder` y el test

---

## 📄 Sobre este documento

**Qué cubre:** la llamada HTTP real. `RestTemplate` (el `fetch()` de Java), `UriComponentsBuilder` (armar la URL sin romperla), los cinco métodos de `BuscadorDePaises` línea por línea, y el test completo. Cierra el recorrido.

**Qué NO cubre:** mocking. Y no es un olvido: **el punto termina haciéndote sentir que falta.** Eso es a propósito.

**Cuánto es:** el punto más largo en código, el más liviano en conceptos nuevos. Casi todo es ensamblar lo que ya sabés.

---

## 🎒 De dónde venís

- **Punto 2:** hay un bean `RestTemplate` en el contenedor, fabricado por un método `@Bean`. Nunca te dije qué hace. Hoy sí.
- **Punto 3:** `BuscadorDePaises` lo recibe por constructor.
- **Punto 4:** la URL base vive en `application.yml` y llega por `propiedades.getBaseUrl()`. Te prometí que ibas a ver por qué tanto lío.
- **Punto 5:** los DTOs son el molde del JSON, y Jackson los llena.
- **De JS:** `fetch()`, `await res.json()`, query params, path params.

---

## 1. `RestTemplate`: el `fetch()` de Java 🔴

> **`RestTemplate`:** el cliente HTTP de Spring. Le pasás una URL, hace el pedido, recibe la respuesta, **y de yapa se la entrega a Jackson para que la convierta en objetos**. Todo en una línea.

Comparalo con lo que ya sabés:

```javascript
// JavaScript — tres pasos
const res  = await fetch(url);          // 1. el pedido HTTP
const data = await res.json();          // 2. parsear el JSON
// 3. no hay paso 3: ya tenés el objeto
```

```java
// Java con RestTemplate — un paso
Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
//               └──────┬──────┘  └─┬─┘   └───┬────┘
//                      │           │         └── "convertilo a ESTE tipo"  ← Jackson (Punto 5)
//                      │           └──────────── la URL ya armada
//                      └──────────────────────── hacé un GET
//
// UNA línea: pedido HTTP + parseo de JSON + mapeo a objetos tipados.
```

**`getForObject` traducido:** *"hacé un **GET** a esta URI, y el resultado dámelo **como objeto** de esta clase"*.

⚠️ **`Pais[].class` — dos rarezas de Java que valen una línea cada una:**

- **`.class`** no es una instancia: es una referencia **a la clase misma** (el "molde", no el objeto). Es cómo le decís a un método *"quiero que me devuelvas cosas de este tipo"* sin darle un objeto. Jackson lo lee y sabe qué construir.
- **`Pais[]`** (array primitivo) y no `List<Pais>`. Es una limitación real de Java, no una elección de estilo — y por eso el código tiene que convertir después.

> 🕳️ **Madriguera — Type erasure**
> En Java, los genéricos **se borran en tiempo de compilación**: `List<Pais>` y `List<String>` son, en runtime, la misma cosa (`List` pelada). Por eso no podés escribir `List<Pais>.class` — no existe. La solución de la industria es pedir un array (`Pais[].class`, que sí conserva el tipo) y convertirlo a `List` después. Eso es exactamente lo que hace la última línea de cada método del repo.
> *Volvé al camino — no se profundiza en DSI. Regla práctica: con `RestTemplate` pedís arrays, y convertís.*

**Otros métodos de `RestTemplate`** (existen, no los usa el repo): `postForObject`, `put`, `delete`, `exchange`. Cubren el resto de los verbos HTTP que viste en la Parte I de la clase. El repo **solo consume**, así que solo usa GET.

---

## 2. `UriComponentsBuilder`: armar la URL sin romperla 🔴

Escobar, mientras lo escribía: *"esta parte es re tediosa, pero la tenemos que hacer porque es muy alojada"*. Y después: *"acá hay un patrón de diseño metido que otro día se los cuento"*.

**Primero, por qué existe.** El camino "obvio" es concatenar strings:

```java
// ❌ La tentación
String url = propiedades.getBaseUrl() + "/name/" + nombre + "?fields=" + CAMPOS;
```

Funciona... hasta que no. Tres formas de romperse:

1. **Espacios.** `buscarPorCapital("buenos aires")` → la URL queda con un espacio en el medio → **URL inválida**. Una URL no puede tener espacios: hay que codificarlo como `%20`.
2. **Caracteres especiales.** Un `&`, un `?` o un acento en el valor y la URL se parte al medio.
3. **Barras dobles.** Si el `base-url` termina en `/` y vos concatenás `/all`, te queda `//all`.

`UriComponentsBuilder` **arma la URL por partes, y escapa cada valor**. Es un ensamblador seguro.

**Vemos las dos variantes que usa el repo, porque se confunden fácil.**

### Variante A — `build()` : solo query params

```java
URI uri = UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
        //                     └────────────┬──────────────────────┘
        //                        "https://restcountries.com/v3.1"    ← del yml (PUNTO 4)
                .path("/all")
        //       └───┬───┘  agrega el segmento fijo → ".../v3.1/all"
                .queryParam("fields", CAMPOS)
        //       └──────┬──────┘  agrega  ?fields=name,capital,region,...
                .build()          // ← construye. Sin placeholders que rellenar.
                .toUri();         // ← lo convierte al tipo URI de Java

// URL final:  https://restcountries.com/v3.1/all?fields=name,capital,region,...
```

### Variante B — `buildAndExpand(valor)` : query params **+ path param**

```java
URI uri = UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                .path("/name/{nombre}")
        //       └────────┬────────┘
        //          👆 {nombre} es un PLACEHOLDER. Un hueco. No es el valor.
                .queryParam("fields", CAMPOS)
                .buildAndExpand(nombre)
        //       └──────┬──────┘
        //          👆 "expandir" = rellenar los huecos con los valores.
        //             Y AL RELLENAR, LOS ESCAPA:  "buenos aires" → "buenos%20aires"
                .toUri();

// buscarPorNombre("argentina")     →  .../v3.1/name/argentina?fields=...
// buscarPorCapital("buenos aires") →  .../v3.1/capital/buenos%20aires?fields=...
//                                                                ↑ escapado solo. Ese es el punto.
```

### La diferencia, y por qué importa 🔴

| | `build()` | `buildAndExpand(valores...)` |
|---|---|---|
| Cuándo se usa | La ruta es **fija** | La ruta tiene **placeholders** `{...}` |
| En el repo | `buscarTodos()` | los otros cuatro métodos |
| Qué hace de más | — | **rellena los huecos y escapa los valores** |
| Si te confundís | placeholder sin rellenar → `.../name/%7Bnombre%7D` 💥 | — |

**Y esto conecta con la teoría de la Parte I de la clase**, que no es casualidad:

| Tipo de parámetro | Dónde va en la URL | En el código | Qué es |
|---|---|---|---|
| **Path param** | En la ruta: `/name/argentina` | `.path("/name/{nombre}")` + `buildAndExpand` | **Identifica** un recurso |
| **Query param** | Después del `?`: `?fields=name` | `.queryParam("fields", CAMPOS)` | **Filtra o modifica** la consulta |

Es exactamente lo que Escobar mostró en Postman antes de tocar Java. Ahora lo ves en el código.

> 🕳️ **Madriguera — El patrón que hay adentro**
> `UriComponentsBuilder` es una implementación del patrón **Builder**: construís un objeto complejo paso a paso, encadenando llamadas (`.path().queryParam().build()`), en vez de pasar quince parámetros a un constructor. Esa cadena de métodos que devuelven `this` tiene nombre propio y razón de ser.
> **📍 Dónde cae:** **clase 8** — *"Patrón Builder"*. Es literalmente lo que el profe prometió (*"otro día se los cuento"*).
> *Volvé al camino — cuando llegue la clase 8, volvé a mirar este código. Va a ser obvio.*

---

## 3. La constante `CAMPOS` 🟡

```java
private static final String CAMPOS =
        "name,capital,region,subregion,population,currencies,languages,area,cca2,cca3";
```

**No es un capricho: es una imposición de la API.** En clase lo descubrieron en vivo — sacaron el `fields` en Postman y el servidor contestó:

```
400 Bad Request
```

REST Countries **exige** el parámetro `fields` en el endpoint `/all` (para que nadie le pida los 250 países completos y le funda el servidor). Sin él, no hay respuesta.

**Fijate en la coherencia:** esos diez campos son **exactamente** los diez atributos de `Pais` (Punto 5). La constante y el DTO están sincronizados a mano. Si mañana agregás un campo al DTO y te olvidás de agregarlo a `CAMPOS`, **la API no te lo manda** y el atributo queda en `null`. Silencioso. **Es el primo hermano del bug del `comon`**, y con el mismo síntoma.

`static final` = una sola copia, compartida por todas las instancias, inmutable. La sintaxis ya la viste en preclase02.

---

## 4. Los métodos, línea por línea 🔴

Los cinco son **el mismo esqueleto**. Dominás uno, dominás los cinco.

```java
@Component
public class BuscadorDePaises {

    private static final String CAMPOS =
            "name,capital,region,subregion,population,currencies,languages,area,cca2,cca3";

    private final RestTemplate restTemplate;                    // PUNTO 2 y 3
    private final RestCountriesProperties propiedades;          // PUNTO 4

    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;                       // ← inyección por constructor
        this.propiedades = propiedades;
    }

    // ─────────────────────────────────────────────────────────────────────
    // MÉTODO 1 — buscarTodos()   (el único que se codeó completo en clase)
    // ─────────────────────────────────────────────────────────────────────
    public List<Pais> buscarTodos() {

        URI uri = UriComponentsBuilder
                .fromUriString(propiedades.getBaseUrl())   // "https://restcountries.com/v3.1"
                .path("/all")                              // → .../v3.1/all
                .queryParam("fields", CAMPOS)              // → ...?fields=name,capital,...
                .build()                                   // sin placeholders → build() a secas
                .toUri();

        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        // ↑ Acá pasa TODO:
        //   1. Se abre la conexión HTTP y se manda el GET.
        //   2. Llega el JSON como texto plano.
        //   3. Jackson lo mastica y arma un array de Pais.   (PUNTO 5)
        //   4. Te lo devuelve tipado.
        //   Resultado esperado: cuerpo.length == 250 aprox.

        return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
        // ↑ DOS cosas en una línea:
        //   a) DEFENSA: si vino null o vacío → devuelvo List.of() (lista vacía inmutable).
        //      ⚠️ Devolver una lista vacía, NUNCA null.
        //         Quien te llama puede hacer .isEmpty() sin chequear null primero.
        //         "Null es la peor respuesta posible" — te obliga a defenderte en cada uso.
        //   b) CONVERSIÓN: Arrays.asList(cuerpo) → convierte el array primitivo en List.
        //      Es el peaje del type erasure (madriguera de la sección 1).
    }

    // ─────────────────────────────────────────────────────────────────────
    // MÉTODO 2 — buscarPorNombre()   (quedó en TODO en clase; el repo lo trae)
    // ─────────────────────────────────────────────────────────────────────
    public Optional<Pais> buscarPorNombre(String nombre) {
        //     └───┬────┘
        //     👆 Optional, NO List. Buscás UN país por nombre: hay uno, o no hay ninguno.
        //        Optional es la forma explícita de decir "esto puede no existir".
        //        (Lo viste en preclase02. Acá está el caso de uso canónico.)

        URI uri = UriComponentsBuilder
                .fromUriString(propiedades.getBaseUrl())
                .path("/name/{nombre}")                    // 👈 PLACEHOLDER
                .queryParam("fields", CAMPOS)
                .buildAndExpand(nombre)                    // 👈 rellena {nombre} y lo escapa
                .toUri();

        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        // ⚠️ La API devuelve un ARRAY aunque busques uno solo. Siempre. (PUNTO 5, sección 3)

        if (cuerpo == null || cuerpo.length == 0) {
            return Optional.empty();                       // "no hay país" — explícito, no null
        }
        return Optional.of(cuerpo[0]);                     // me quedo con el primero
    }

    // ─────────────────────────────────────────────────────────────────────
    // MÉTODOS 3, 4 y 5 — moneda, región, capital
    // Idénticos entre sí. Solo cambia la ruta. Copio uno:
    // ─────────────────────────────────────────────────────────────────────
    public List<Pais> buscarPorMoneda(String codigoMoneda) {
        URI uri = UriComponentsBuilder
                .fromUriString(propiedades.getBaseUrl())
                .path("/currency/{codigo}")                // ← lo ÚNICO que cambia
                .queryParam("fields", CAMPOS)
                .buildAndExpand(codigoMoneda)
                .toUri();

        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
    }

    // buscarPorRegion(String region)   → .path("/region/{region}")
    // buscarPorCapital(String capital) → .path("/capital/{capital}")
    // Todo lo demás, calcado.
}
```

### Por qué `buscarPorNombre` devuelve `Optional` y los otros `List` 🔴

**No es capricho. Es la semántica del recurso, y es una decisión de diseño que te van a hacer justificar:**

| Método | Devuelve | Por qué |
|---|---|---|
| `buscarPorNombre` | `Optional<Pais>` | Un nombre identifica **un** país. Existe o no existe. |
| `buscarPorMoneda` | `List<Pais>` | El euro lo usan **20 países**. Es una colección por naturaleza. |
| `buscarPorRegion` | `List<Pais>` | "Europe" son decenas de países. |
| `buscarPorCapital` | `List<Pais>` | Buscás "buenos aires" y podría matchear más de uno. |
| `buscarTodos` | `List<Pais>` | Obvio. |

**La firma del método comunica el contrato.** `Optional` grita *"esto puede no existir, manejalo"*. `List` grita *"esto puede venir vacío, pero nunca es null"*. Quien te lea el código sabe qué esperar **sin abrir el cuerpo del método**.

Esto Escobar lo decidió **en vivo** y le costó: primero puso `List<Pais> buscarPorNombre`, después dijo *"podemos devolver un `Optional`, vamos a ver un solo país, más fácil"*, y lo cambió. Vos estás viendo la versión pensada.

---

## 5. El test 🔴

```java
package ar.edu.utn.ba.ddsi.countries;

import ar.edu.utn.ba.ddsi.countries.services.BuscadorDePaises;
import org.junit.jupiter.api.Test;                                    // JUnit 5 (preclase02)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;             // ← AssertJ. Ver sección 6.

@SpringBootTest
// ↑ "Antes de correr estos tests, LEVANTÁ EL CONTENEDOR COMPLETO."
//   Corre todo el arranque del Punto 1: escanea, instancia, inyecta.
//   ⚠️ POR ESO el @Autowired de abajo funciona: hay un contenedor de donde sacar el bean.
//      Sin @SpringBootTest no habría contenedor, y buscadorDePaises sería null.
class BuscadorDePaisesIT {
//                    └┬┘
//                  👆 ACORDATE DE ESTAS DOS LETRAS. Sección 7.

    @Autowired
    private BuscadorDePaises buscadorDePaises;    // PUNTO 3 — inyección por campo

    @Test
    void buscarTodosDevuelveVariosPaises() {
        var lista = buscadorDePaises.buscarTodos();
        //  └┬┘
        //  👆 `var` (Java 10+): el compilador infiere el tipo. Es List<Pais>, pero no lo escribís.
        //     NO es el `let` de JS: sigue siendo tipado estático, solo que inferido.
        //     Escobar lo aclaró en clase: "no se puede poner LET. LET es de JavaScript".

        assertThat(lista).isNotEmpty();
        assertThat(lista.getFirst().getNombre().getComun()).isNotBlank();
        //                └───┬───┘  └────┬───┘  └───┬───┘
        //                    │           │          └── getter de NombrePais (@Data)
        //                    │           └───────────── getter de Pais (@Data)
        //                    └───────────────────────── ⚠️ getFirst() es de JAVA 21.
        //                                               (SequencedCollection). No existe antes.
        //                                               Antes: lista.get(0)
    }

    @Test
    void buscarPorNombrePeruDevuelvePeru() {
        var opt = buscadorDePaises.buscarPorNombre("peru");   // → Optional<Pais>

        assertThat(opt).isPresent();                          // ¿vino algo?
        assertThat(opt.get().getNombre().getComun()).isEqualTo("Peru");
        assertThat(opt.get().getCca2()).isEqualTo("PE");
        //         └────┬───┘
        //         👆 .get() sobre el Optional: "dame el contenido".
        //            Solo es seguro DESPUÉS de verificar isPresent().
    }

    @Test
    void buscarPorMonedaARSincluyeArgentina() {
        var lista = buscadorDePaises.buscarPorMoneda("ars");
        assertThat(lista).extracting(p -> p.getNombre().getComun()).contains("Argentina");
        //                └────┬────┘  └──────────┬──────────────┘  └───┬───┘
        //                     │                  │                     └── "que esté Argentina"
        //                     │                  └──── lambda: de cada país, sacá el nombre común
        //                     └── AssertJ: "transformá la lista y asertá sobre el resultado"
        //                         Escrito a mano serían 4 líneas con un stream + map + collect.
    }

    @Test
    void buscarPorRegionEuropeNoVacia() {
        var lista = buscadorDePaises.buscarPorRegion("europe");
        assertThat(lista).isNotEmpty();
        assertThat(lista).allMatch(p -> "Europe".equals(p.getRegion()));
        //                └──┬───┘
        //                👆 "TODOS los elementos cumplen esto". Si uno falla, falla el test.
        //                   Fijate el orden: "Europe".equals(p.getRegion()) y no al revés.
        //                   Es defensa contra null: si getRegion() fuera null, no explota.
    }

    @Test
    void buscarPorCapitalBuenosAiresDevuelveArgentina() {
        var lista = buscadorDePaises.buscarPorCapital("buenos aires");
        //                                             └──────┬─────┘
        //                                        👆 CON ESPACIO. Es la prueba viva de que
        //                                           buildAndExpand lo escapa: → buenos%20aires
        assertThat(lista).isNotEmpty();
        assertThat(lista.getFirst().getCca2()).isEqualTo("AR");
    }
}
```

> 🧵 **HILO CERRADO (Punto 4).** *"¿Tanto lío para no hardcodear una URL?"*
> **Buscá una URL en este test. No hay ninguna.** El test usa **exactamente la misma configuración que producción** — el mismo `application.yml`, el mismo bean de properties, la misma URL. Es **imposible** que se desincronicen, porque **hay una sola**. Si mañana la API cambia de dominio, tocás una línea del `.yml` y el código y los tests siguen andando sin que los abras.
> **Ese es el pago.** Y es la respuesta que el profe no llegó a dar.

---

## 6. AssertJ vs JUnit: la tabla de traducción 🟡

En `preclase02` aprendiste las aserciones de **JUnit 5**. El repo usa **AssertJ**. Y en clase Escobar usó las de JUnit. **Van a convivir todo el año, así que fijalas ahora.**

**No son rivales.** JUnit es el **motor** (corre los tests, entiende `@Test`); AssertJ es una **biblioteca de aserciones** más expresiva que se monta encima. Entra sola con `spring-boot-starter-webmvc-test`.

| Lo que querés verificar | JUnit 5 (preclase02) | AssertJ (el repo) |
|---|---|---|
| Son iguales | `assertEquals("Peru", x)` | `assertThat(x).isEqualTo("Peru")` |
| Es verdadero | `assertTrue(opt.isPresent())` | `assertThat(opt).isPresent()` |
| No es nulo | `assertNotNull(x)` | `assertThat(x).isNotNull()` |
| Lista no vacía | `assertFalse(lista.isEmpty())` | `assertThat(lista).isNotEmpty()` |
| String no en blanco | `assertFalse(s.isBlank())` | `assertThat(s).isNotBlank()` |
| La lista contiene X | *(loop o stream a mano)* | `assertThat(lista).contains(x)` |
| Todos cumplen algo | *(loop a mano)* | `assertThat(lista).allMatch(...)` |

**La diferencia real está en las dos últimas filas.** Con JUnit escribís un loop; con AssertJ, una línea. Por eso las bibliotecas de aserciones existen.

**El patrón mental:** AssertJ **siempre** arranca con `assertThat(loQueTengo)` y encadena `.loQueEspero(...)`. Se lee como una oración en inglés. Si te acostumbrás a ese ritmo, no necesitás memorizar la tabla.

⚠️ **Ojo con el import**, que es la fuente clásica de confusión:

```java
import static org.assertj.core.api.Assertions.assertThat;      // ✅ AssertJ
import static org.junit.jupiter.api.Assertions.assertEquals;   // ✅ JUnit 5
// Las dos clases se llaman "Assertions". Fijate SIEMPRE el paquete.
```

---

## 7. El sufijo `IT` — y el dolor que la clase 4 va a curar 🔴

```java
class BuscadorDePaisesIT { ... }
//                     ↑↑
```

**`IT` = Integration Test.** No es decorativo: es una **convención de nomenclatura** que le avisa al que lee (y a las herramientas de build) que este test **no es unitario**.

El README del repo no se anda con vueltas: *"las pruebas dependen de **conectividad** hacia la API de REST Countries"*.

**Traducción: estos tests le pegan a internet. De verdad. Cada vez que los corrés.**

### Frená y hacé la cuenta de todo lo que puede salir mal 🔴

| Lo que pasa | Resultado del test |
|---|---|
| Te quedás sin internet | 💥 Falla. Y tu código está perfecto. |
| El servidor de REST Countries se cae | 💥 Falla. Y no es tu culpa. |
| La API se pone lenta | 🐢 El test tarda segundos en vez de milisegundos |
| Corrés los tests 100 veces mientras programás | 100 llamadas HTTP reales. Te pueden **bloquear por rate limit**. |
| Argentina cambia de capital (o la API corrige un dato) | 💥 Falla un test que nunca tocaste |

> ### 🧵 Ese incomodísimo que estás sintiendo es EL PUNTO.
>
> **Un test unitario debería ser rápido, determinístico y aislado.** Estos tests son **lentos, frágiles y dependientes del mundo exterior**. Fallan por razones que no tienen nada que ver con si tu código está bien o mal.
>
> **¿Y cómo se arregla?** Lo tenés en la punta de la lengua, aunque no sepas el nombre:
>
> *"¿Y si en vez de pasarle el `RestTemplate` de verdad, le paso uno **falso** que devuelva un JSON inventado, sin tocar internet?"*
>
> **Y podés hacerlo. Podés justamente porque `BuscadorDePaises` RECIBE su `RestTemplate` por constructor** en vez de fabricarlo adentro (Punto 3). Si hubiera hecho `new RestTemplate()` ahí adentro, estarías atrapado: no habría forma de reemplazarlo.
>
> **Eso se llama MOCKING.** Y ese razonamiento que acabás de hacer solo — *"si la dependencia entra desde afuera, la puedo reemplazar por una falsa"* — **es la razón profunda por la que existe la inyección de dependencias.** No es para ahorrarte `new`. Es para esto.
>
> **📍 CAE EN LA CLASE 4.** Tiene video previo dedicado + repo propio de la cátedra (`github.com/dds-utn/ejemplo-mockeo`).
>
> **No es una madriguera: es el próximo escalón, y ya tenés el pie puesto.** Cuando llegue, no vas a estar aprendiendo mocking — vas a estar poniéndole nombre a un problema que ya sentiste en las manos.

---

## 8. Convergencia: abrí el repo 🔴

**Este es el momento del recorrido.** Abrí `rest-paises` en IntelliJ y recorrelo entero:

```
rest-paises/
├── pom.xml                        → Java 21, Spring Boot 4.0.5, Lombok. (preclase02 + config)
├── src/main/resources/
│   └── application.yml            → la URL base.                        PUNTO 4 ✅
└── src/main/java/.../countries/
    ├── CountriesApplication.java  → @SpringBootApplication, el main.    PUNTO 1 ✅
    │                                 @EnableConfigurationProperties.    PUNTO 4 ✅
    ├── config/
    │   ├── RestTemplateConfig     → @Configuration + @Bean.             PUNTO 2 ✅
    │   └── RestCountriesProperties→ @ConfigurationProperties.           PUNTO 4 ✅
    ├── services/
    │   ├── BuscadorDePaises       → @Component + inyección.          PUNTOS 2,3 ✅
    │   │                             RestTemplate + UriComponentsBuilder. PUNTO 6 ✅
    │   └── dto/
    │       ├── Pais               → @JsonProperty, @JsonIgnoreProperties.
    │       ├── NombrePais         → @Data, @NoArgsConstructor.
    │       └── DetalleMoneda      →                                     PUNTO 5 ✅
    └── src/test/.../
        └── BuscadorDePaisesIT     → @SpringBootTest, @Autowired, AssertJ.
                                      Y el sufijo IT.                    PUNTO 6 ✅
```

**No queda una sola línea sin explicación.** Ni una anotación, ni un método, ni un archivo. Ese era el objetivo.

**Corré los tests.** Deberían pasar los cinco (si tenés internet — y ahora sabés exactamente por qué esa aclaración hace falta).

**Y después rompelo, que es donde se aprende de verdad:**

| Experimento | Qué esperar |
|---|---|
| Sacá el `@Component` de `BuscadorDePaises` | El test explota: no hay bean para inyectar |
| Sacá el `@NoArgsConstructor` de `Pais` | Jackson no puede construirlo → excepción |
| Escribí mal un `@JsonProperty` | Ese campo viene en `null`, los demás bien. **El bug del profe.** |
| Sacá el `@EnableConfigurationProperties` | La app no arranca |
| Cambiá `base-url` en el `.yml` a algo inválido | Falla la llamada, no la compilación |
| Agregá `"flags"` a `CAMPOS` sin tocar el DTO | No pasa nada: `@JsonIgnoreProperties` lo ignora |
| Cortá el wifi y corré los tests | 💥 **Sentí el dolor del que hablamos.** |

---

## ✅ Checkpoint — Punto 6

1. ¿Qué hace `getForObject(uri, Pais[].class)`? Enumerá los cuatro pasos que ocurren en esa línea.
2. ¿Por qué se pide `Pais[].class` y no `List<Pais>`? ¿Qué hay que hacer después, y por qué?
3. ¿Cuál es la diferencia entre `build()` y `buildAndExpand(...)`? ¿Cuándo usás cada uno?
4. ¿Por qué no se concatena la URL con `+`? Nombrá **tres** cosas que se romperían.
5. Path param vs query param: ¿cuál identifica y cuál filtra? Dame el ejemplo de cada uno en este repo.
6. ¿Por qué existe la constante `CAMPOS`? ¿Qué pasa si no la mandás? ¿Y qué pasa si agregás un atributo al DTO y te olvidás de agregarlo acá?
7. ¿Por qué `buscarPorNombre` devuelve `Optional<Pais>` y `buscarPorMoneda` devuelve `List<Pais>`?
8. ¿Por qué los métodos devuelven `List.of()` en vez de `null` cuando no hay resultados?
9. ¿Qué hace `@SpringBootTest`? ¿Por qué sin ella el `@Autowired` no funcionaría?
10. ¿Qué es AssertJ y en qué se diferencia de JUnit? ¿Son alternativas o conviven?
11. **La importante:** ¿qué significa el sufijo `IT`? Enumerá tres razones por las que estos tests pueden fallar **aunque tu código esté perfecto**.
12. **La más importante:** ¿cómo arreglarías el problema de la pregunta anterior? ¿Y qué decisión de diseño del Punto 3 es la que te **permite** arreglarlo?

---

## 🏁 Fin del recorrido

Arrancaste con un repo lleno de anotaciones mágicas y un profe diciéndote *"no lo mires, copy-pasteale"*.

Terminás sabiendo:

- **Qué es un framework** y por qué te quita el control (**inversión de control**).
- **Qué es el contenedor**, qué es un bean, y las dos puertas para entrar (`@Component` / `@Bean`).
- **Quién hace el `new`** — con nombre, apellido y momento exacto.
- **Qué es la inyección de dependencias**, sus dos formas, y por qué la del constructor es mejor.
- **Cómo la configuración vive afuera del código** y llega tipada.
- **Cómo un JSON se convierte en objetos Java**, y por qué Lombok era un requisito y no un lujo.
- **Cómo se consume una API REST** desde Java, de punta a punta.
- **Por qué estos tests están mal** — y qué herramienta lo arregla.

**Lo que costó no fue difícil. Fue que llegó todo junto y disfrazado de magia.**

### Y ahora, lo que el recorrido te deja montado

| Lo que ya tenés en las manos | Dónde lo cobrás |
|---|---|
| `@Component`, DI por constructor | **Clase 4** — capas, `@Service`, `@Repository` |
| El dolor de los tests contra la API real | **Clase 4** — mocking. Ya sabés qué problema resuelve. |
| Consumir una API REST | **Clase 4** — la vuelta de tuerca: **exponerla** |
| El "patrón adentro" de `UriComponentsBuilder` | **Clase 8** — Builder |
| Clases-molde con anotaciones que mapean a un formato externo | **Clases 18-21** — `@Entity`, Hibernate. **Es el mismo patrón.** |
| Todo Spring Boot | **El TPA entero, y las entregas 4, 5 y 6.** |

El mapa completo está en el **roadmap**. Volvé a él cada vez que en una clase aparezca algo que te suene.

---

### Qué sigue

1. **Complemento del recorrido** — las respuestas de los seis checkpoints + las dudas que hayan salido en el chat. Pedilo cuando termines de leer.
2. **Apunte maestro de la clase 02** — el material oficial y permanente: cliente-servidor, HTTP, arquitectura web, API REST y la práctica completa. Se genera con cobertura plena. Lo vas a *vivir* como repaso, porque el entendimiento ya ocurrió acá.

---

**FIN DEL PUNTO 6 — FIN DEL RECORRIDO SPRING BOOT**
