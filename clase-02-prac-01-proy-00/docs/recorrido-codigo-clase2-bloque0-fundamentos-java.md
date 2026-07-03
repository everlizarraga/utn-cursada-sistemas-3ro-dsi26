# 📘 Bloque 0 — Fundamentos de Java para alguien que viene de JS/TS

> **Para qué es este archivo:** prepararte el ojo para leer el código del proyecto `rest-paises` (Clase 2 de DSI). No es un curso completo de Java — solo cubre lo que **efectivamente aparece en ese código**.
>
> **Cómo usarlo:** leelo una vez de corrido. Los Bloques 1 al 5 van a usar todo esto y no van a re-explicar la sintaxis.

---

## 🧭 Mapa del archivo

1. El "feel" de Java vs JS/TS — modelo mental
2. Cómo se organiza un archivo Java (`package` + `import`)
3. La clase como unidad universal
4. Modificadores: `public`, `private`, `static`, `final`
5. Métodos y signatures
6. Constructores
7. Tipos: primitivos vs wrappers, `String`
8. Generics — `List<String>`, `Map<K, V>`
9. `var` — inferencia de tipos
10. `Optional<T>` — el nullable explícito
11. Annotations — metadata leída por frameworks
12. Lambdas y method references
13. Collections básicos: `List`, `Map`, `Arrays`

Al final: cheatsheet de equivalencias JS/TS ↔ Java + checkpoint.

---

## 1. El "feel" de Java vs JS/TS

Antes de la sintaxis, el modelo mental.

| Aspecto | JS/TS | Java |
|---|---|---|
| Tipado | Dinámico (JS) / opcional (TS) | **Estático fuerte y obligatorio** |
| Runtime | V8, Node.js, Deno | **JVM** (Java Virtual Machine) |
| Compilación | TS → JS (opcional, descartable) | **`.java` → `.class` (bytecode)** obligatoria |
| Top-level libre | Sí (podés tener código suelto) | **No, todo va dentro de una clase** |
| Imports | `import X from "y"` | `import paquete.Subpaquete.Clase;` |
| Convención de archivos | Cualquier nombre | **Una clase pública por archivo, mismo nombre que la clase** |

**El cambio más importante:** en Java **no podés escribir código suelto**. Todo método, toda variable, todo `if` vive dentro de una clase. No existe "el script". Existe "la aplicación", arrancada por un método llamado `main` dentro de una clase.

> **Por qué Java es así:** fue diseñado en los 90 para sistemas grandes y de larga vida. La rigidez es deliberada — el compilador atrapa errores antes de ejecutar. Es lo opuesto al "deploy fast, fix later" de JS.

**Otra diferencia clave:** Java se *compila* antes de correr. El proceso es:

1. Escribís `.java`.
2. Maven (o el IDE) lo compila a `.class` (bytecode).
3. La JVM ejecuta el bytecode.

Si tu código tiene un typo de tipo, **no compila** y por lo tanto **no corre**. Errores que en JS te explotan en runtime, en Java te explotan al compilar. Es más fricción al escribir, menos sorpresas al deployar.

---

## 2. Cómo se organiza un archivo Java

Mirá el inicio de cualquier `.java` del proyecto:

```java
package ar.edu.utn.ba.ddsi.countries.services;        // ← 1. Dónde vive este archivo

import ar.edu.utn.ba.ddsi.countries.config.RestCountriesProperties;   // ← 2. Qué importa
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Component                                              // ← 3. Annotations sobre la clase
public class BuscadorDePaises {                         // ← 4. La clase
    // ...
}
```

### 2.1 `package`

`package ar.edu.utn.ba.ddsi.countries.services;` declara la "carpeta lógica" donde vive este archivo. La estructura de carpetas en disco **debe coincidir exactamente** con el package:

```
src/main/java/
└── ar/
    └── edu/
        └── utn/
            └── ba/
                └── ddsi/
                    └── countries/
                        └── services/
                            └── BuscadorDePaises.java   ← Acá vive
```

**Convención del nombre:** dominio invertido. Tu organización es `ddsi.utn.ba.edu.ar` → invertís → `ar.edu.utn.ba.ddsi`. Sirve para evitar choques de nombre con código de otras organizaciones.

**Equivalente en JS:** no hay equivalente directo. Es como `package.json` + estructura de carpetas combinados. En JS importás por path relativo (`./services/foo`); en Java declarás el package y el compilador resuelve dónde está el archivo.

### 2.2 `import`

`import paquete.Clase;` trae a esta clase al alcance del archivo. Es **más explícito y menos automágico** que ES modules:

- Importás **clase por clase** (o usás `import paquete.*;` para traer todas — desaconsejado).
- No hay `default export`. Importás siempre con nombre.
- Las clases del package `java.lang` (`String`, `Integer`, `System`, `Object`, etc.) están **implícitamente importadas**. Por eso nunca ves `import java.lang.String;`.

**Comparación rápida:**

| JS/TS | Java |
|---|---|
| `import { Foo } from "./services/foo";` | `import ar.edu.utn.ba.ddsi.countries.services.Foo;` |
| `import * as utils from "./utils";` | `import ar.edu.utn.ba.ddsi.utils.*;` |
| `import _ from "lodash";` | `import lombok.Data;` (no hay default; siempre con nombre) |

---

## 3. La clase como unidad universal

Toda línea de código Java vive dentro de una clase. **Sin excepciones.**

```java
public class BuscadorDePaises {
    // ← Atributos (campos)
    private final RestTemplate restTemplate;

    // ← Constructor
    public BuscadorDePaises(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ← Métodos
    public List<Pais> buscarTodos() {
        // ...
    }
}
```

**Reglas:**

- Una clase pública por archivo `.java`.
- El archivo se llama **igual** que la clase pública (`BuscadorDePaises.java` ↔ `public class BuscadorDePaises`).
- Convención de orden dentro de la clase: atributos arriba, constructores en el medio, métodos abajo. No es obligatorio pero es lo estándar.
- **No hay funciones sueltas.** Para tener algo "global", se mete como método `static` en una clase utilitaria. Ejemplo: `Math.sqrt()` — la "función" `sqrt` vive como método estático de la clase `Math`.

**Comparación:**

```typescript
// JS/TS — todo vale, podés tener funciones sueltas
export function buscarTodos() { ... }

const restTemplate = new RestTemplate();
```

```java
// Java — todo dentro de clases
public class BuscadorDePaises {
    public List<Pais> buscarTodos() { ... }
}
```

---

## 4. Modificadores: `public`, `private`, `static`, `final`

Cada declaración (clase, atributo, método) puede llevar modificadores que controlan visibilidad y comportamiento. Los cuatro que vas a ver en este proyecto:

### `public` — accesible desde cualquier parte

```java
public class BuscadorDePaises { ... }        // Cualquiera puede usar la clase
public List<Pais> buscarTodos() { ... }      // Cualquiera puede llamar al método
```

### `private` — accesible solo desde adentro de esta clase

```java
private final RestTemplate restTemplate;     // Solo BuscadorDePaises ve este atributo
```

**Por qué importa:** encapsulamiento. Si dejás `restTemplate` privado, nadie de afuera puede modificarlo arbitrariamente — controlás cómo se accede vía métodos.

### `static` — pertenece a la clase, no a cada instancia

```java
private static final String CAMPOS = "name,capital,region,...";
```

- Sin `static`: el atributo existe en **cada instancia** del objeto.
- Con `static`: el atributo existe **una sola vez**, asociado a la clase, no a cada objeto.

**Analogía en TS:**

```typescript
class Foo {
    static SHARED = "soy uno solo";       // se accede como Foo.SHARED
    instance = "soy de cada Foo";          // foo1.instance, foo2.instance
}
```

### `final` — no se puede reasignar después

```java
private final RestTemplate restTemplate;     // No se puede reasignar
```

Equivalente a `const` en JS, pero con el mismo gotcha: protege la **referencia**, no el contenido del objeto. `final List<X> lista` no impide modificar la lista por dentro (agregar/sacar elementos); solo impide reasignar `lista` a otra lista.

### Cuadro rápido

| JS/TS | Java |
|---|---|
| `class { x = 1; }` | `public int x = 1;` |
| `class { #x = 1; }` (private) | `private int x = 1;` |
| `class { static x = 1; }` | `public static int x = 1;` |
| `class { readonly x = 1; }` | `public final int x = 1;` |
| `const x = 1;` | `final int x = 1;` (variable local) |

---

## 5. Métodos y signatures

Un método se declara con:

```
modificadores  tipoDeRetorno  nombre(tipo param, tipo param)  { cuerpo }
```

Ejemplos del proyecto:

```java
public List<Pais> buscarTodos() {
    // ...
    return paises;
}

public Optional<Pais> buscarPorNombre(String nombre) {
    // ...
    return Optional.of(pais);
}

public void miMetodoSinRetorno() {
    // void = no devuelve nada
}
```

**Diferencias clave con JS/TS:**

| JS/TS | Java |
|---|---|
| `function foo(x: string): number { }` | `public int foo(String x) { }` |
| `function foo(x) { }` (sin tipos) | **No existe**. Java exige tipo de cada parámetro y del retorno. |
| `return undefined` (implícito) | Si el retorno es `void`, no escribís `return`. Si no, **tenés que retornar algo**. |

**Notación:** en JS te referís a una función como `foo`. En Java te referís a un método como `foo()` o `Clase.foo()` para ser explícito.

---

## 6. Constructores

Un constructor es un "método especial" para crear instancias. Reglas:

- Se llama **igual que la clase**.
- **No tiene tipo de retorno** (ni siquiera `void`).
- Se invoca con `new` (excepto cuando Spring lo hace por vos — ver Bloque 2).

```java
public class BuscadorDePaises {
    private final RestTemplate restTemplate;
    private final RestCountriesProperties propiedades;

    // ← Constructor
    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }
}
```

**`this`:** igual que en JS, hace referencia a "la instancia actual". `this.restTemplate = restTemplate;` significa "asignale al atributo `restTemplate` de esta instancia el valor del parámetro `restTemplate`".

> **Por qué importa este constructor específicamente:** Spring va a leer esta firma y le va a pasar automáticamente un `RestTemplate` y un `RestCountriesProperties` cuando cree la instancia. Esto es **inyección de dependencias por constructor**, y lo vas a desmenuzar en el Bloque 2.

**Comparación:**

```typescript
// TS — shortcut moderno: declara y asigna en una línea
class BuscadorDePaises {
    constructor(
        private restTemplate: RestTemplate,
        private propiedades: RestCountriesProperties
    ) {}
}
```

```java
// Java — no tiene el shortcut. Declarás el atributo y lo asignás en el constructor.
public class BuscadorDePaises {
    private final RestTemplate restTemplate;
    private final RestCountriesProperties propiedades;

    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }
}
```

---

## 7. Tipos: primitivos vs wrappers, `String`

Java tiene **dos clases de tipos**:

### Primitivos (minúscula, no son objetos)

| Primitivo | Para qué |
|---|---|
| `int` | Enteros 32 bits |
| `long` | Enteros 64 bits |
| `double` | Decimales 64 bits |
| `boolean` | true/false |
| `char` | Un carácter |

### Wrappers (mayúscula, son objetos)

Cada primitivo tiene un equivalente como objeto:

| Primitivo | Wrapper |
|---|---|
| `int` | `Integer` |
| `long` | `Long` |
| `double` | `Double` |
| `boolean` | `Boolean` |
| `char` | `Character` |

**¿Por qué dos versiones?**

- Los **primitivos** son rápidos y livianos, pero no se pueden poner en colecciones (`List`, `Map`) y no pueden ser `null`.
- Los **wrappers** son objetos completos: se pueden poner en colecciones, pueden ser `null`, tienen métodos.

Java te convierte uno en otro automáticamente (*autoboxing*/*unboxing*). En los DTOs del proyecto vas a ver `Long poblacion` (wrapper) en vez de `long` porque la API podría devolver `null`, y un `long` primitivo no puede ser `null`.

### `String`

`String` **siempre es un objeto** (no hay primitivo). Se comporta como inmutable: `"hola" + " mundo"` crea un nuevo `String`, no modifica los originales.

**⚠️ Comparación de strings:** en Java se comparan con `.equals()`, **no con `==`**.

```java
String a = "hola";
String b = "hola";
a == b           // true a veces, false otras — NO USAR
a.equals(b)      // ✅ siempre correcto
```

`==` compara referencias de objeto, no contenido. Es el cuco clásico de Java y lo vas a ver en los tests del proyecto:

```java
assertThat(lista).allMatch(p -> "Europe".equals(p.getRegion()));
//                              ^^^^^^^^^ ← String literal primero, evita NullPointerException
```

> **Truco:** poner el literal a la izquierda (`"Europe".equals(x)`) en vez de a la derecha (`x.equals("Europe")`) evita un crash si `x` es `null`.

---

## 8. Generics — `List<String>`, `Map<K, V>`

Cuando ves esto:

```java
private List<String> capitales;
private Map<String, DetalleMoneda> monedas;
public List<Pais> buscarTodos() { ... }
public Optional<Pais> buscarPorNombre(String nombre) { ... }
```

Los `<...>` son **generics**: parametrización de tipos. Son lo mismo conceptualmente que en TS:

| TypeScript | Java |
|---|---|
| `Array<string>` o `string[]` | `List<String>` |
| `Map<string, number>` | `Map<String, Integer>` |
| `Promise<User>` | `Optional<User>` (semánticamente distinto pero rima en sintaxis) |

**Cómo se lee:** `List<Pais>` → "una lista de `Pais`". `Map<String, DetalleMoneda>` → "un map donde las claves son `String` y los valores son `DetalleMoneda`".

**Por qué importa:** sin generics, una `List` podría contener cualquier cosa y el compilador no te ayudaría a detectar errores. Con `List<Pais>`, el compilador verifica que solo metas `Pais` y al sacar te asegura que sale `Pais`. Es lo mismo que el beneficio de TS sobre JS plano.

---

## 9. `var` — inferencia de tipos (Java 10+)

Para evitar repetir tipos largos en variables locales, Java 10 introdujo `var`:

```java
// Sin var, verboso:
List<Pais> lista = buscadorDePaises.buscarTodos();

// Con var, equivalente:
var lista = buscadorDePaises.buscarTodos();
```

**Reglas:**

- ✅ Solo en **variables locales** (dentro de métodos).
- ❌ No se puede usar en atributos de clase ni en parámetros.
- ✅ El tipo se **infiere de la asignación**. `var x = "hola";` → `x` es `String`.
- ❌ **No es como `any` de TS.** Sigue siendo tipado fuerte; el compilador sabe el tipo real.

En los tests del proyecto vas a ver:

```java
var lista = buscadorDePaises.buscarTodos();      // lista es List<Pais>
var opt = buscadorDePaises.buscarPorNombre(...); // opt es Optional<Pais>
```

---

## 10. `Optional<T>` — el nullable explícito

`Optional<T>` es un "envoltorio" que puede contener un valor o no. Equivalente conceptual al `T | null` o `T | undefined` de TS, pero como **objeto explícito**.

```java
public Optional<Pais> buscarPorNombre(String nombre) {
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    if (cuerpo == null || cuerpo.length == 0) {
        return Optional.empty();          // "no encontré nada"
    }
    return Optional.of(cuerpo[0]);        // "acá está el resultado"
}
```

**Cómo usar lo que devuelve:**

```java
Optional<Pais> opt = buscadorDePaises.buscarPorNombre("peru");

// Chequear si tiene valor:
if (opt.isPresent()) {
    Pais p = opt.get();          // sacar el valor (tira excepción si está vacío)
}

// Alternativa: valor por defecto si está vacío
Pais p = opt.orElse(new Pais());

// Alternativa: hacer algo solo si hay valor
opt.ifPresent(pais -> System.out.println(pais.getNombre()));
```

**Por qué existe:** obliga a quien usa el método a pensar en "qué pasa si no hay valor". En JS olvidás manejar `null` y explota en runtime; en Java, si declarás `Optional<Pais>`, quien lo usa **tiene que** decidir qué hacer con la ausencia.

| TypeScript | Java |
|---|---|
| `Pais \| undefined` | `Optional<Pais>` |
| `if (x !== undefined) x.foo()` | `opt.ifPresent(p -> p.foo())` |
| `x ?? defaultPais` | `opt.orElse(defaultPais)` |

---

## 11. Annotations — metadata leída por frameworks

Las annotations son la principal cosa "rara" que vas a ver en este código:

```java
@Component
@Data
@JsonProperty("name")
@SpringBootApplication
@Autowired
@Test
```

**Qué son:** metadata que **se le pega** a una clase, método o atributo. **No hacen nada por sí solas** — son leídas por frameworks (Spring, Jackson, JUnit, Lombok) que las interpretan y generan comportamiento.

**Sintaxis:** `@NombreDeLaAnnotation` o `@NombreDeLaAnnotation(parametros)`.

```java
@Component                                                 // Sin parámetros
public class BuscadorDePaises { ... }

@JsonProperty("name")                                       // Con un parámetro
private NombrePais nombre;

@ConfigurationProperties(prefix = "rest-countries")         // Con parámetro nombrado
public class RestCountriesProperties { ... }
```

**Comparación con TypeScript:** son similares a los **decoradores** de TS (`@Component` en Angular, `@Injectable`). Mismo concepto.

**Quién las "ejecuta":** los frameworks. Ejemplos del proyecto:

| Annotation | Quién la lee | Qué efecto tiene |
|---|---|---|
| `@SpringBootApplication` | Spring Boot | Arranca el framework |
| `@Component` | Spring | "Esta clase es un bean — administrala vos" |
| `@Bean` | Spring | "Este método produce un bean" |
| `@Autowired` | Spring | "Inyectá un bean acá" |
| `@ConfigurationProperties` | Spring | "Leé valores del YAML para acá" |
| `@Data`, `@NoArgsConstructor` | Lombok | Genera getters/setters/constructors en compilación |
| `@JsonProperty`, `@JsonIgnoreProperties` | Jackson | Reglas para serializar/deserializar JSON |
| `@Test` | JUnit | "Esto es un test, ejecutalo" |
| `@SpringBootTest` | Spring | "Arrancá Spring antes del test" |

**Punto clave:** una annotation **no es magia**. Es solo una marca que algún framework lee y reacciona. Cuando veas una nueva, la pregunta es "qué framework la procesa y qué hace cuando la encuentra".

---

## 12. Lambdas y method references

Java soporta funciones anónimas como JS, pero la sintaxis es distinta:

```java
// Lambda (igual concepto que arrow function)
list.stream().filter(p -> p.getRegion().equals("Europe"))

// Method reference (atajo cuando la lambda solo llama a un método)
list.stream().map(Pais::getNombre)
// equivalente a:
list.stream().map(p -> p.getNombre())
```

**Comparación:**

| JS/TS | Java |
|---|---|
| `arr.filter(p => p.region === "Europe")` | `list.stream().filter(p -> "Europe".equals(p.getRegion()))` |
| `arr.map(p => p.getName())` | `list.stream().map(p -> p.getName())` o `list.stream().map(Pais::getName)` |

En este proyecto vas a ver lambdas en los tests:

```java
assertThat(lista).extracting(p -> p.getNombre().getComun()).contains("Argentina");
assertThat(lista).allMatch(p -> "Europe".equals(p.getRegion()));
```

---

## 13. Collections básicos: `List`, `Map`, `Arrays`

### `List<T>` — lista ordenada

```java
List<Pais> paises = new ArrayList<>();   // crear vacía
List<Pais> paises = List.of(p1, p2);     // crear con elementos (inmutable)
paises.size();                            // cantidad de elementos
paises.get(0);                            // acceder por índice
paises.getFirst();                        // el primero (Java 21+)
paises.add(p3);                          // agregar (solo en ArrayList, no en List.of)
```

### `Map<K, V>` — diccionario clave-valor

```java
Map<String, DetalleMoneda> monedas = new HashMap<>();
monedas.put("USD", detalleUsd);
monedas.get("USD");                       // recuperar valor
monedas.containsKey("USD");               // existe esa clave?
```

### `Arrays` — utilitario para arrays "crudos"

```java
Pais[] cuerpo = ...;                      // array clásico (estilo C)
List<Pais> lista = Arrays.asList(cuerpo); // convertir array → List
```

**¿Por qué hay arrays *y* List?** Los arrays son la primitiva de bajo nivel (`Pais[]`). `List` es la abstracción de alto nivel con más métodos. En el proyecto vas a ver: la API REST devuelve un array (`Pais[]`) y nosotros lo convertimos a `List<Pais>` para devolverlo.

| JS/TS | Java |
|---|---|
| `const arr = [];` | `List<T> list = new ArrayList<>();` |
| `arr.length` | `list.size()` |
| `arr[0]` | `list.get(0)` |
| `arr.push(x)` | `list.add(x)` |
| `const map = new Map();` | `Map<K, V> map = new HashMap<>();` |
| `map.set("k", v)` | `map.put("k", v)` |
| `map.get("k")` | `map.get("k")` |

---

## 🎯 Cheatsheet final — equivalencias JS/TS ↔ Java

| Concepto | JS/TS | Java |
|---|---|---|
| Archivo de código | `foo.ts` | `Foo.java` (mismo nombre que la clase) |
| Import | `import { X } from "./x"` | `import paquete.X;` |
| Clase | `class X { }` | `public class X { }` |
| Atributo público | `class X { y = 1; }` | `public int y = 1;` |
| Atributo privado | `class X { #y = 1; }` | `private int y = 1;` |
| Constructor | `constructor(x) { this.x = x; }` | `X(int x) { this.x = x; }` |
| Método | `foo(): number { return 1; }` | `public int foo() { return 1; }` |
| Sin retorno | `foo(): void { }` | `public void foo() { }` |
| Inmutable | `const x = 1;` / `readonly` | `final int x = 1;` |
| Generic | `Array<string>` | `List<String>` |
| Inferencia local | `const x = "hola"` | `var x = "hola"` |
| Nullable | `string \| null` | `Optional<String>` |
| Arrow func | `(x) => x + 1` | `x -> x + 1` |
| Method ref | — | `MiClase::miMetodo` |
| Length | `arr.length` | `list.size()` |
| String equals | `a === b` | `a.equals(b)` |
| Console log | `console.log(x)` | `System.out.println(x)` |
| Annotation / decorator | `@Component` (Angular) | `@Component` (Spring) |
| Package manager | `npm` / `package.json` | `Maven` / `pom.xml` |

---

## ✅ Checkpoint mental antes de avanzar

Si podés contestar mentalmente estas, estás listo para el Bloque 1:

1. ¿Por qué cada archivo Java empieza con `package ar.edu.utn.ba.ddsi.countries...`?
2. ¿Cuál es la diferencia entre `int` y `Integer`? ¿Por qué los DTOs usan `Long poblacion` en vez de `long`?
3. ¿Qué hace `final` en `private final RestTemplate restTemplate`?
4. ¿Qué es un `Optional<Pais>` y cuándo retornaría `Optional.empty()`?
5. ¿Por qué las annotations no hacen nada "por sí solas"?
6. ¿Cómo se compara `String a` con `String b` correctamente y por qué `==` no anda?
7. ¿Por qué `var lista = ...` no es lo mismo que `any` de TS?

Si alguna no te cierra, **preguntá por chat** antes de avanzar al Bloque 1.

---

**FIN DEL BLOQUE 0**

**Siguiente:** Bloque 1 → `pom.xml`, `application.yml`, `CountriesApplication.java` (arranque del proyecto).
