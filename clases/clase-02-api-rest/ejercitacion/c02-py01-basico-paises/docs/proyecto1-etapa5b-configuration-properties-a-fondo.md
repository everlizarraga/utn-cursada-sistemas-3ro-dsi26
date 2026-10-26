# 🌱 Proyecto 1 — Etapa 5b: `@ConfigurationProperties` a fondo

> **Objetivo:** profundizar lo que armaste en la Etapa 5. Vas a ver qué pasa cuando el YAML y la clase no coinciden, cómo registrar muchas clases de properties, las cuatro formas de convertirlas en bean, cómo hacerlas **inmutables** para que nadie te cambie la configuración por accidente, y cómo poner valores por defecto.
>
> **El momento clave:** un diagrama de decisión que explica **cuándo Spring llena tu clase con setters y cuándo por el constructor**. Con eso, todo lo demás encaja.
>
> **Pre-requisito:** Etapa 5 completa (`PaisesProperties` con `@ConfigurationProperties` y `@EnableConfigurationProperties` funcionando).
>
> **Tiempo estimado:** 50-60 minutos.
>
> **Versiones:** Java 21 · Spring Boot 4.0.x · Lombok (la versión que fija el parent de Spring Boot).

---

## 🧭 Mapa de esta etapa

1. El YAML y la clase no coinciden: valores de más y de menos.
2. Varias clases de properties: listarlas o escanearlas.
3. Las cuatro formas de convertir una clase de properties en bean.
4. Mutable vs inmutable: por qué importa.
5. Tres formas de hacerla inmutable: clase `final`, Lombok `@Value`, `record`.
6. Cómo decide Spring: ¿setters o constructor?
7. Valores por defecto y valores que faltan.
8. Que falle al arrancar si falta un valor obligatorio.
9. Más de un archivo YAML.
10. Experimentos.
11. Checkpoint.

Todos los ejemplos usan los nombres de la Etapa 5: `PaisesApplication`, `PaisesProperties`, `CatalogoDePaises`, el package `ar.edu.utn.ba.paises` y este YAML:

```yaml
paises:
    url: https://restcountries.com/v3.1
    nombre-catalogo: Catálogo Mundial
```

---

## 🧩 Parte 1: El YAML y la clase no coinciden

En la Etapa 5 viste un caso: si un valor **falta** en el YAML (Experimento 2), el atributo queda en `null`. Falta el caso inverso.

### El YAML tiene valores de más

```yaml
paises:
    url: https://restcountries.com/v3.1
    nombre-catalogo: Catálogo Mundial
    region-favorita: Americas          # ← la clase no tiene un atributo para esto
```

```java
@ConfigurationProperties(prefix = "paises")
@Data
public class PaisesProperties {
    private String url;                // ← se llena
    private String nombreCatalogo;     // ← se llena
}                                      // region-favorita: se ignora, sin error
// Resultado esperado: la app arranca normal. region-favorita no va a ningún lado.
```

**Por defecto, Spring llena lo que encuentra en la clase y descarta el resto en silencio.**

Eso es cómodo, pero tiene un riesgo: es lo que pasó en el Experimento 4 de la Etapa 5. Escribiste `urlllll`, Spring lo ignoró como un valor "de más" y `url` quedó en `null` sin ningún aviso.

### Pedirle que falle cuando sobra algo

```java
@ConfigurationProperties(prefix = "paises", ignoreUnknownFields = false)
@Data
public class PaisesProperties {
    private String url;
    private String nombreCatalogo;
}
// Resultado esperado con region-favorita (o con urlllll) en el YAML:
// la app NO arranca, y el error menciona la propiedad que no tiene dónde ir.
```

`ignoreUnknownFields = false` significa "no ignores los valores desconocidos: avisame". Sirve para detectar typos en el YAML al arrancar, en vez de descubrirlos por un `null` más tarde.

### Resumen

| Situación | Qué pasa | Cómo protegerte |
|---|---|---|
| El YAML trae un valor que la clase no tiene | se ignora sin error | `ignoreUnknownFields = false` → falla al arrancar |
| La clase tiene un atributo que el YAML no trae | queda `null` (o `0` / `false` si es primitivo) | valor por defecto (Parte 7) o validación (Parte 8) |

---

## 📚 Parte 2: Varias clases de properties

En un proyecto real, el YAML agrupa configuración de muchas cosas, cada una con su prefijo:

```yaml
paises:
    url: https://restcountries.com/v3.1
    nombre-catalogo: Catálogo Mundial

monedas:                                # (ejemplo: MonedasProperties no existe en el proyecto)
    url: https://api-de-monedas.com
    base: ARS
```

Y cada prefijo tiene **su propia clase**: `PaisesProperties`, y una `MonedasProperties` si existiera. Una clase gigante para todo sería difícil de mantener.

¿Cómo se registran todas? Hay dos formas.

### Forma A: listarlas en `@EnableConfigurationProperties`

No hace falta repetir la anotación: acepta varias clases **entre llaves**.

```java
@SpringBootApplication
@EnableConfigurationProperties({PaisesProperties.class, MonedasProperties.class})
public class PaisesApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaisesApplication.class, args);
    }
}
// Resultado esperado (si MonedasProperties existiera): dos beans,
// uno PaisesProperties y uno MonedasProperties, cada uno lleno con su prefijo
```

**El package no importa:** le pasás la clase en sí (`PaisesProperties.class`), no la tiene que buscar. Si está en otro package, IntelliJ agrega el `import` y listo.

Conviene cuando son pocas: ves de un vistazo qué clases de configuración tiene la app.

### Forma B: escanearlas con `@ConfigurationPropertiesScan`

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class PaisesApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaisesApplication.class, args);
    }
}
// Resultado esperado: el mismo que la Forma A, sin listar nada
```

Funciona igual que el escaneo de `@Component`: recorre el package de `PaisesApplication` y todos sus subpackages, y registra como bean **toda clase que tenga `@ConfigurationProperties`**.

Es la forma escalable. Por ejemplo, podés juntar todas las clases de configuración en un subpackage:

```
ar.edu.utn.ba.paises/
├── PaisesApplication.java          ← @ConfigurationPropertiesScan
├── CatalogoDePaises.java
└── properties/
    ├── PaisesProperties.java       ← la encuentra sola
    └── MonedasProperties.java      ← la encuentra sola
```

y agregar una clase nueva sin tocar `PaisesApplication` nunca más.

### ¿Hacen lo mismo?

**Sí.** Las dos registran un bean por cada clase de properties y lo llenan con el YAML. La única diferencia es **cómo encuentran las clases**: en la A se las listás vos; en la B las busca sola.

---

## 🔀 Parte 3: Las cuatro formas de convertirla en bean

En la Etapa 5 dijimos que había "otra forma" y que no te detuvieras en ella. Ahora sí: hay cuatro.

### Forma 1: `@EnableConfigurationProperties` (la de la Etapa 5)

La clase lleva solo `@ConfigurationProperties`; el registro va en `PaisesApplication`. Ya la conocés.

### Forma 2: `@ConfigurationPropertiesScan`

La de la Parte 2. Mismo resultado, sin listar.

### Forma 3: `@Component` en la clase de properties

```java
@Component                                    // ← la registra el escaneo normal de componentes
@ConfigurationProperties(prefix = "paises")
@Data
public class PaisesProperties {
    private String url;
    private String nombreCatalogo;
}
// Sin @EnableConfigurationProperties ni scan en PaisesApplication.
// Resultado esperado: funciona. Spring la crea como cualquier @Component,
// ve el @ConfigurationProperties y la llena con los setters.
```

> ⚠️ **No la combines con la Forma 1.** Si la clase tiene `@Component` **y** además está en `@EnableConfigurationProperties`, Spring crea **dos** beans `PaisesProperties`, y `CatalogoDePaises` no sabe cuál inyectar: la app no arranca. Usá una forma o la otra.

Es la más intuitiva ("es un componente, que se registre solo"). Tiene una limitación importante que vas a entender en las Partes 4 a 6: **solo funciona con clases mutables** (con setters).

### Forma 4: un método `@Bean` con `@ConfigurationProperties`

Sirve cuando la clase que querés llenar **no tiene ninguna anotación**, típicamente porque viene de una librería y no la podés editar. La anotación se pone **en el método**, no en la clase.

```java
// Imaginá que esta clase viene de una librería: no tiene anotaciones de Spring
// y no la podés tocar (el @Data es para abreviar: la librería trae sus getters y setters)
@Data
public class ConfiguracionCliente {
    private String url;
    private int timeout;
}
// Resultado esperado: por sí sola, Spring no sabe que existe ni que hay que llenarla
```

```yaml
cliente-paises:
    url: https://restcountries.com/v3.1
    timeout: 5000
```

```java
@Configuration
public class ClientesConfig {

    @Bean
    @ConfigurationProperties(prefix = "cliente-paises")   // ← en el método, no en la clase
    public ConfiguracionCliente clientePaises() {
        return new ConfiguracionCliente();                // la devolvés vacía...
    }
    // ...y Spring, al registrarla como bean, la llena con los setters
    // usando lo que haya bajo "cliente-paises" en el YAML.
    // Resultado esperado: un bean ConfiguracionCliente con url = "https://..." y timeout = 5000
}
```

Como el prefijo lo decide el método, podés tener **dos beans de la misma clase con configuraciones distintas**: otro método `clienteMonedas()` con `prefix = "cliente-monedas"` que devuelva otra `ConfiguracionCliente`. Como con cualquier `@Bean` (Etapa 4), se distinguen por el nombre del método. Esta forma también necesita setters.

### Resumen

| Forma | Dónde va la anotación | Clase mutable (setters) | Clase inmutable |
|---|---|---|---|
| 1. `@EnableConfigurationProperties(X.class)` | en `PaisesApplication` | ✅ | ✅ |
| 2. `@ConfigurationPropertiesScan` | en `PaisesApplication` | ✅ | ✅ |
| 3. `@Component` | en la clase de properties | ✅ | ❌ |
| 4. `@Bean` + `@ConfigurationProperties` | en un método de un `@Configuration` | ✅ | ❌ |

La columna "inmutable" se explica en las próximas partes.

> 🎓 **Para el parcial, si te preguntan.** En la cursada se usa la Forma 1, con la clase mutable: `@EnableConfigurationProperties(LaClase.class)` en la clase principal, y la clase de properties con `@ConfigurationProperties` + `@Data`. Es exactamente lo que armaste en la Etapa 5, y la que tenés que poder escribir sin dudar.

---

## 🔒 Parte 4: Mutable vs inmutable — por qué importa

> **Una corrección a la Etapa 5.** Ahí dijimos que Spring *necesita* los setters. Es cierto para la clase que armaste: constructor vacío + setters (la versión **mutable**). Pero no es el único camino: Spring también puede pasarle los valores **por el constructor**, sin ningún setter. Esa es la versión **inmutable**, y es lo que vas a ver ahora.

| | Mutable | Inmutable |
|---|---|---|
| Cómo es | atributos comunes + setters (`@Data`) | atributos `final` llenados por el constructor, sin setters |
| Cómo la llena Spring | la crea vacía y llama a `setUrl(...)` | le pasa los valores **al constructor** |
| ¿Se puede cambiar después? | sí: cualquiera puede llamar `properties.setUrl("otra")` | no |

**¿Por qué importa?** Por cómo funcionan los beans (Etapa 3): `PaisesProperties` es **un solo objeto, compartido** por todos los beans que lo inyectan.

```
                    ┌──────────────────────┐
  CatalogoDePaises ─┤                      │
                    │  PaisesProperties    │   ← UN solo objeto
  ServicioDePaises ─┤  url = "https://..." │
                    │                      │
  OtroServicio ─────┤                      │
                    └──────────────────────┘

  Si OtroServicio hace properties.setUrl("cualquier-cosa"),
  CatalogoDePaises y ServicioDePaises ven la URL cambiada. Para toda la app.
```

La configuración se controla **desde el YAML**: no debería cambiar mientras la app corre. Con setters públicos, cualquier línea de código la puede cambiar por accidente.

**Ojo con una confusión común:** `private` y `final` son cosas distintas.
- **`private`** = nadie de afuera toca el atributo **directamente**.
- **`final`** = nadie lo puede **cambiar**, ni la propia clase, después del constructor.

Con `@Data`, los atributos son `private`, pero los setters son públicos: están escondidos, pero se pueden cambiar a través del setter. **Escondido no es protegido.** Lo que protege es `final` y no tener setters.

---

## 🛡️ Parte 5: Tres formas de hacerla inmutable

Las tres se registran con la Forma 1 o la 2 (`@EnableConfigurationProperties` o el scan). Con `@Component` no funcionan: lo vas a ver en la Parte 6.

### Opción A: clase con atributos `final`, escrita a mano

```java
package ar.edu.utn.ba.paises;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paises")
public class PaisesProperties {

    private final String url;              // final: se asigna una sola vez, en el constructor
    private final String nombreCatalogo;

    // Un solo constructor, con parámetros → Spring le pasa los valores del YAML
    // paises.url → url, paises.nombre-catalogo → nombreCatalogo
    public PaisesProperties(String url, String nombreCatalogo) {
        this.url = url;
        this.nombreCatalogo = nombreCatalogo;
    }

    public String getUrl() { return url; }                       // solo getters
    public String getNombreCatalogo() { return nombreCatalogo; }
    // No hay setters. Y aunque los escribieras, el compilador no te deja
    // asignar un atributo final fuera del constructor.
}
// Resultado esperado: properties.getUrl() → "https://restcountries.com/v3.1"
//                     properties.setUrl("x") → no existe, no compila
```

### Opción B: Lombok `@Value`

```java
package ar.edu.utn.ba.paises;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paises")
@Value          // de Lombok: todos los atributos private final, getters,
                // y un constructor con todos los atributos
public class PaisesProperties {
    String url;
    String nombreCatalogo;
}
// Resultado esperado: idéntico a la Opción A, en 4 líneas
```

> ⚠️ **Dos `@Value` distintos.** Al importar, IntelliJ puede ofrecerte:
> - `lombok.Value` → el de acá: hace la clase inmutable.
> - `org.springframework.beans.factory.annotation.Value` → el de Spring: inyecta **un** valor suelto del YAML, por ejemplo `@Value("${paises.url}")`.
>
> Para esta opción, elegí el de Lombok.

### Opción C: `record`

```java
package ar.edu.utn.ba.paises;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paises")
public record PaisesProperties(String url, String nombreCatalogo) { }
// Resultado esperado: properties.url() → "https://restcountries.com/v3.1"
```

Un `record` es una clase de Java pensada justamente para "un paquete de datos que no cambia": sus atributos son `private final`, tiene un constructor con todos ellos y métodos para leerlos.

**Ojo con el nombre de esos métodos:** un record **sí tiene métodos para leer**, pero se llaman como el atributo, sin "get": `url()` y `nombreCatalogo()`. Lo que **no tiene son setters**.

Si cambiás `PaisesProperties` a record, en `CatalogoDePaises` tenés que cambiar `properties.getUrl()` por `properties.url()`.

### Las tres juntas

| | Clase `final` a mano | Lombok `@Value` | `record` |
|---|---|---|---|
| Inmutable | ✅ | ✅ | ✅ |
| Cómo la llena Spring | por el constructor | por el constructor | por el constructor |
| Cómo leés un valor | `getUrl()` | `getUrl()` | `url()` |
| Código | largo | corto | el más corto |
| Valores por defecto (Parte 7) | ✅ cómodo | ⚠️ incómodo | ✅ cómodo |

---

## 🌳 Parte 6: Cómo decide Spring — ¿setters o constructor?

Spring no adivina por el tipo de clase: sigue una regla.

```
 ¿la registraste con @EnableConfigurationProperties o con @ConfigurationPropertiesScan?
        │
        ├─ SÍ ─► ¿algún constructor tiene @ConstructorBinding?
        │            ├─ SÍ ─► llena por ESE constructor
        │            └─ NO ─► ¿tiene UN solo constructor, y ese constructor tiene parámetros?
        │                         ├─ SÍ ─► llena por CONSTRUCTOR        (record, clase final, Lombok @Value)
        │                         └─ NO ─► constructor vacío + SETTERS  (clase con @Data)
        │
        ├─ NO, con @Component ─► la crea como cualquier bean, y después usa los SETTERS
        │                         (si el constructor pide un String → busca un BEAN String → falla)
        │
        └─ NO, con un @Bean ─► el constructor lo llamás VOS (el new del método), y después
                                Spring usa los SETTERS
                                (con una clase inmutable no hay setters → el YAML no llega,
                                 y el objeto queda con lo que le pasaste al new)
```

**Por qué funcionan el `record` y Lombok `@Value`:** los dos terminan con **un solo constructor con todos los atributos**. Cuando la clase está registrada como clase de properties (Forma 1 o 2), Spring sabe que tiene que pasarle los valores del YAML a ese constructor.

**Por qué falla `@Component` + `record`:**

```java
@Component
@ConfigurationProperties(prefix = "paises")
public record PaisesProperties(String url, String nombreCatalogo) { }
```

`@Component` hace que Spring cree este objeto **como cualquier bean**, igual que creó `CatalogoDePaises` en la Etapa 3. Para llamar a un constructor con parámetros, Spring busca **un bean de cada tipo** que pide (inyección por constructor). El constructor del record pide dos `String`, no existe ningún bean de tipo `String` en la app, y no arranca.

```
Resultado esperado: la app NO arranca, con un error del tipo
  "Parameter 0 of constructor in ar.edu.utn.ba.paises.PaisesProperties
   required a bean of type 'java.lang.String' that could not be found."
(El texto exacto puede variar según la versión; la idea es la misma.)
```

Es el mismo error que viste en la Etapa 3 cuando un constructor pedía algo que no era un bean.

Por eso, en la tabla de la Parte 3, `@Component` y `@Bean` dicen ❌ en la columna "inmutable": con `@Component` falla al arrancar, y con `@Bean` arranca pero el YAML no llega.

**Si una clase de properties tiene más de un constructor** y ninguno está marcado, Spring no llena por constructor: vuelve a los setters (y necesita un constructor vacío). Para que use uno de esos constructores, se lo marca con `@ConstructorBinding` (`org.springframework.boot.context.properties.bind.ConstructorBinding`). Con un solo constructor, como en todos estos ejemplos, no hace falta.

---

## 🎚️ Parte 7: Valores por defecto y valores que faltan

### Qué llega cuando el YAML no trae un valor

Spring no falla al arrancar. Según cómo se llena la clase:
- **Por setters:** nunca llama a ese setter, así que el atributo queda con el valor que tenía.
- **Por constructor:** llama igual al constructor y, en el parámetro que falta, pasa `null` si es un objeto (`String`, `Integer`, `List`…) o el valor por defecto de Java si es primitivo (`0` en un `int`, `false` en un `boolean`).

En los dos casos, el problema aparece después: un `NullPointerException` cuando algún código usa ese `null`. Para evitarlo, se pone un **valor por defecto**.

Supongamos este YAML, sin `nombre-catalogo` a propósito:

```yaml
paises:
    url: https://restcountries.com/v3.1
```

En cada versión de abajo, `url` va **sin** valor por defecto y `nombreCatalogo` **con** uno.

### En el `record`: `@DefaultValue` en el parámetro

```java
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "paises")
public record PaisesProperties(
        String url,                                                 // sin defecto
        @DefaultValue("Catálogo sin nombre") String nombreCatalogo  // con defecto
) { }
// Resultado esperado: url()            → "https://restcountries.com/v3.1"
//                     nombreCatalogo() → "Catálogo sin nombre"
// Si también faltara paises.url → url() → null
```

En un parámetro de constructor no podés escribir `= "algo"` como en un atributo. Para eso existe `@DefaultValue`.

### En la clase `final` a mano: `@DefaultValue` en el parámetro del constructor

```java
@ConfigurationProperties(prefix = "paises")
public class PaisesProperties {

    private final String url;
    private final String nombreCatalogo;

    public PaisesProperties(String url,                                           // sin defecto
                            @DefaultValue("Catálogo sin nombre") String nombreCatalogo) {
        this.url = url;
        this.nombreCatalogo = nombreCatalogo;
    }

    public String getUrl() { return url; }
    public String getNombreCatalogo() { return nombreCatalogo; }
}
// Resultado esperado: igual que el record (con getUrl() / getNombreCatalogo())
```

### En Lombok `@Value`: no hay forma cómoda

```java
@ConfigurationProperties(prefix = "paises")
@Value
public class PaisesProperties {
    String url;
    String nombreCatalogo;    // no hay dónde poner @DefaultValue: el constructor lo genera Lombok
}
// Resultado esperado: nombreCatalogo → null
```

> ⚠️ **Trampa:** no le pongas `String nombreCatalogo = "Catálogo sin nombre";`. Con `@Value` el atributo es `final`; si lo inicializás, queda fijo para siempre. Lombok lo saca del constructor y **el YAML ya no lo puede cambiar**, ni siquiera cuando trae el valor. Si necesitás valores por defecto, usá el `record` o escribí el constructor a mano.

### En la clase mutable (`@Data`): inicializar el atributo

```java
@ConfigurationProperties(prefix = "paises")
@Data
public class PaisesProperties {
    private String url;                                     // sin defecto
    private String nombreCatalogo = "Catálogo sin nombre";  // con defecto: inicializás el atributo
}
// Spring crea el objeto (nombreCatalogo ya vale "Catálogo sin nombre") y llama a setUrl(...).
// Como el YAML no trae nombre-catalogo, nunca llama a setNombreCatalogo → queda el inicial.
// Si el YAML sí lo trae, el setter lo pisa.
// Resultado esperado: igual que el record
```

`@DefaultValue` no aplica acá: solo se usa en parámetros de constructor.

### Resumen

| Versión | Cómo se pone el valor por defecto | Si falta en el YAML y no hay defecto |
|---|---|---|
| `record` | `@DefaultValue("...")` en el parámetro | `null` (o `0` / `false`) |
| Clase `final` a mano | `@DefaultValue("...")` en el parámetro del constructor | `null` |
| Lombok `@Value` | — (pasate al record) | `null` |
| Mutable (`@Data`) | inicializar el atributo (`= "..."`) | `null` (o `0` / `false`) |

---

## 🚨 Parte 8: Que falle al arrancar si falta un valor obligatorio

Algunos valores no tienen un valor por defecto razonable. Si la URL de la API no está, la app no puede hacer nada útil. En ese caso es mejor que **no arranque** y te diga por qué, en vez de un `null` que explota en medio de una llamada.

Para eso se usa **validación**:

1. Agregá la dependencia al `pom.xml`. Un **starter** es una dependencia que trae agrupadas todas las librerías que hacen falta para una función; este trae las de validación. Va sin versión: la fija el parent de Spring Boot.
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-validation</artifactId>
   </dependency>
   ```
   Recargá Maven. Resultado esperado: al correr, nada cambia todavía.
2. Marcá la clase con `@Validated` y el atributo obligatorio con una regla:
   ```java
   import jakarta.validation.constraints.NotBlank;
   import org.springframework.validation.annotation.Validated;

   @ConfigurationProperties(prefix = "paises")
   @Validated                                      // ← "validá esta clase al llenarla"
   public record PaisesProperties(
           @NotBlank String url,                   // ← obligatorio: ni null ni vacío
           @DefaultValue("Catálogo sin nombre") String nombreCatalogo
   ) { }
   // Resultado esperado si falta paises.url: la app NO arranca, y el error dice
   // que paises.url no puede estar vacío.
   ```

`@NotBlank` = "no puede ser `null`, ni vacío, ni solo espacios". Hay otras reglas (`@NotNull`, `@Min`, `@Max`, `@Pattern`…) que funcionan igual.

**Combinación recomendada:** `@NotBlank` para lo obligatorio, `@DefaultValue` para lo opcional.

---

## 🗂️ Parte 9: Más de un archivo YAML

Spring Boot **lee solo**:
- `application.yml` (o `application.properties`);
- `application-{perfil}.yml`, por ejemplo `application-dev.yml`, si activás ese perfil. Un **perfil** es un juego de configuración alternativo (desarrollo, producción…) que se activa con `spring.profiles.active`.

Un archivo suelto como `paises.yml` **no lo lee**. Para usarlo, se importa desde `application.yml`:

```yaml
spring:
    config:
        import:
            - classpath:paises.yml
            - optional:classpath:monedas.yml
```

Resultado esperado: Spring carga `paises.yml` como si estuviera escrito en `application.yml`. Si falta `monedas.yml`, arranca igual (por el `optional:`); si falta `paises.yml`, la app no arranca.

- `classpath:` significa "en `src/main/resources/`".
- `optional:` hace que no falle si el archivo no existe.

Lo más común es un solo `application.yml` agrupado por prefijos, como el de la Parte 2. Separar en archivos tiene sentido cuando la configuración crece mucho.

---

## 🧪 Parte 10: Experimentos

Partí de tu `PaisesProperties` de la Etapa 5 (mutable: con `@Data` o con getters y setters a mano). Al terminar los experimentos, **volvé a la versión mutable** antes de pasar a la Etapa 6: la Etapa 6 le agrega atributos y los lee con getters (`getUrl()`), y así la seguís tal cual está escrita.

### Experimento 1: Que el typo falle

1. Agregá `ignoreUnknownFields = false` a `@ConfigurationProperties`.
2. En el YAML, escribí `urlllll` en vez de `url` (como en el Experimento 4 de la Etapa 5).
3. Corré.

**Resultado esperado:** la app **no arranca**, y el error menciona `urlllll`. Compará con la Etapa 5, donde el mismo typo solo dejaba un `null` silencioso.

### Experimento 2: Pasala a `record`

1. Reemplazá `PaisesProperties` por la versión `record` de la Parte 5 (Opción C).
2. En `CatalogoDePaises`, cambiá `properties.getUrl()` por `properties.url()` y `getNombreCatalogo()` por `nombreCatalogo()`.
3. Corré.

**Resultado esperado:** el log muestra los mismos valores que en la Etapa 5. Ahora intentá escribir `properties.setUrl("x")` en `CatalogoDePaises`: **no compila**. La configuración quedó protegida.

### Experimento 3: Rompela con `@Component`

1. Con la versión `record` del experimento anterior, agregale `@Component`.
2. Sacá `@EnableConfigurationProperties` de `PaisesApplication`.
3. Corré.

**Resultado esperado:** la app **no arranca**, con un error que dice que el constructor necesita un bean de tipo `String`. Volvé a sacar `@Component` y a poner `@EnableConfigurationProperties`: arranca de nuevo. Con el diagrama de la Parte 6, explicá por qué.

### Experimento 4: Valor por defecto

1. Con la versión `record`, agregá `@DefaultValue("Catálogo sin nombre")` al parámetro `nombreCatalogo`.
2. Comentá `nombre-catalogo` en el YAML.
3. Corré.

**Resultado esperado:** `>>> Nombre del catálogo: Catálogo sin nombre`, en vez del `null` del Experimento 2 de la Etapa 5.

### Experimento 5 (opcional): El escaneo

1. Creá el subpackage `ar.edu.utn.ba.paises.properties` y mové `PaisesProperties` ahí.
2. En `PaisesApplication`, reemplazá `@EnableConfigurationProperties(PaisesProperties.class)` por `@ConfigurationPropertiesScan`.
3. Corré.

**Resultado esperado:** funciona igual. Spring la encontró sola, sin que la nombres.

---

## ✅ Criterios de "Etapa 5b completa"

- [ ] Entendés qué pasa cuando el YAML trae valores de más (se ignoran) y de menos (quedan `null`).
- [ ] Sabés registrar varias clases de properties (lista entre llaves o `@ConfigurationPropertiesScan`).
- [ ] Conocés las cuatro formas de convertir una clase de properties en bean, y cuál usa la cátedra.
- [ ] Podés explicar por qué una clase de properties conviene que sea inmutable.
- [ ] Sabés escribir la versión inmutable con `record`, con clase `final` y con Lombok `@Value`.
- [ ] Podés recorrer el diagrama de la Parte 6 y explicar por qué falla `@Component` + `record`.
- [ ] Sabés poner un valor por defecto en cada versión.
- [ ] Hiciste al menos los Experimentos 1 a 4.
- [ ] Dejaste `PaisesProperties` funcionando (mutable o inmutable) para la Etapa 6.

---

## ✅ Checkpoint

1. El YAML trae `paises.region-favorita` y `PaisesProperties` no tiene ese atributo. ¿Qué pasa al arrancar? ¿Cómo hacés para que falle?
2. Tenés seis clases de properties. ¿Qué dos formas tenés de registrarlas sin escribir seis anotaciones? ¿Cuál elegirías y por qué?
3. ¿Por qué un atributo `private` con setter público no está protegido? ¿Qué lo protege de verdad?
4. `OtroServicio` hace `properties.setUrl("x")`. ¿Qué ven `CatalogoDePaises` y los demás beans que inyectan `PaisesProperties`? ¿Por qué?
5. ¿Por qué `@Component` + `record` no arranca, y `@EnableConfigurationProperties` + `record` sí?
6. ¿Cómo sabe Spring si tiene que llenar la clase con setters o por el constructor?
7. ¿Cómo ponés un valor por defecto en un `record`? ¿Y en una clase con `@Data`? ¿Por qué no sirve `= "..."` en un atributo de una clase Lombok `@Value`?
8. ¿Cuándo conviene un valor por defecto y cuándo `@Validated` + `@NotBlank`?
9. ¿Spring lee un archivo `monedas.yml` que pongas en `src/main/resources/`? ¿Qué tenés que hacer para que lo lea?

---

## 🎯 Una reflexión antes de seguir

En la Etapa 5 la cadena era: archivo → objeto → bean. Acá le agregaste las preguntas que aparecen apenas el proyecto crece:
- **¿Y si son muchas?** Las listás o las escaneás.
- **¿Y si alguien la cambia?** La hacés inmutable.
- **¿Y si falta un valor?** Le das un defecto, o hacés que falle al arrancar.

Y una sola regla las ordena a todas: **registrada como clase de properties + un solo constructor con parámetros → constructor; si no → setters.**

---

## ▶️ Próximo paso

Seguí con la **Etapa 6**. Vas a usar `PaisesProperties` para sacar la URL de la API (y, en la versión con token, la clave).

- **`proyecto1-etapa6-llamada-http-v5.md`**: la versión vigente, para la API que hoy pide token.
- **`proyecto1-etapa6-llamada-http.md`**: la versión original, para la API sin token de cuando se dictó la clase.

Si decidiste quedarte con `PaisesProperties` como `record`, cada atributo que la Etapa 6 agregue va como componente nuevo del record, y se lee sin "get": `properties.url()`, `properties.apiKey()`.
