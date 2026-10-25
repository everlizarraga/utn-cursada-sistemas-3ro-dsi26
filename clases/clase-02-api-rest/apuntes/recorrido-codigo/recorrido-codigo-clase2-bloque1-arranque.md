# 📘 Bloque 1 — Arranque del proyecto del profe

> **Objetivo:** entender qué pasa cuando arrancás `rest-paises`. Desarmar el `pom.xml`, leer `application.yml`, y comprender `CountriesApplication.java` con sus annotations de Spring Boot.
>
> **Pre-requisito:** Bloque 0 completo. Idealmente Etapas 0-3 del Proyecto 0 (Maven y Lombok básicos). Si hiciste hasta Etapa 7, todo te va a sonar.

---

## 🧭 Mapa del bloque

1. El `pom.xml` del profe línea por línea — qué hereda, qué dependencias trae.
2. El `application.yml` — formato YAML y dónde lo busca Spring.
3. `CountriesApplication.java` — el punto de entrada con `@SpringBootApplication`.
4. Qué pasa cuando arrancás el proyecto (el "viaje" del `SpringApplication.run`).
5. Comparación lado a lado con tu Proyecto 0.

---

## 🔍 Parte 1: El `pom.xml` desarmado

Lo abrís y ves esto (te muestro solo lo importante):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" ...>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.5</version>
        <relativePath/>
    </parent>
    <groupId>ar.edu.utn.ba.ddsi</groupId>
    <artifactId>countries</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        ...
    </dependencies>
    <build>
        <plugins>
            ...
        </plugins>
    </build>
</project>
```

Lo que ya conocés del Proyecto 0 sigue: `<modelVersion>`, `<groupId>`, `<artifactId>`, `<version>`. Lo nuevo arranca con `<parent>`.

### 1.1 El bloque `<parent>` — herencia de POM

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
</parent>
```

**Qué dice:** "este `pom.xml` **hereda** de otro `pom.xml` llamado `spring-boot-starter-parent`".

Sí, **los POMs se heredan** igual que las clases en Java. Tu `pom.xml` recibe automáticamente todo lo que el padre define, y solo tenés que override lo que querés cambiar.

**Qué te trae `spring-boot-starter-parent`:**

1. **Versiones gestionadas de cientos de dependencias** (Spring, Jackson, Lombok, JUnit, AssertJ, Mockito, etc). Por eso vas a ver dependencias **sin `<version>`** más abajo — la versión la define el parent.

2. **Configuración por defecto de plugins** (compiler, surefire, jar, resources). Por eso no hace falta declarar versiones de plugins.

3. **Encoding UTF-8, Java version, plugins comunes**, etc.

> En tu Proyecto 0 tenías que declarar las versiones de Lombok, JUnit, AssertJ a mano. Con el parent, eso desaparece — Spring Boot ya sabe qué versiones de cada cosa son compatibles entre sí. **Es lo que se llama "BOM" (Bill of Materials)**.

### 1.2 `<properties>`

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

Override de una propiedad del parent. El parent por default probablemente usa Java 17 o 21; acá explícitamente fijás 21.

### 1.3 Las dependencias

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

**Notá que NO tiene `<version>`.** El parent ya sabe qué versión va con Spring Boot 4.0.5.

Este es un **starter**. Un starter es un meta-paquete que **trae un combo de dependencias juntas, ya probadas entre sí**. `spring-boot-starter-webmvc` incluye:

- `spring-web` — clases HTTP, `RestTemplate`, etc.
- `spring-webmvc` — controllers, `@RestController`, MVC.
- `tomcat` embebido — un servidor web adentro de tu app.
- `jackson` — serialización/deserialización JSON.
- Validación, logging, etc.

**Te ahorra declarar 10+ dependencias a mano.** Es una de las razones por las que Spring Boot revolucionó Java.

#### Las otras dependencias

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**DevTools.** Sirve solo en desarrollo. Hace **hot reload** cuando guardás un archivo Java — recompila y reinicia automáticamente sin que tengas que parar y arrancar la app. `optional=true` significa "esta dependencia no se propaga a quien dependa de esta app" (relevante para libs publicadas, no para apps finales).

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

Lombok. Lo conocés. **Sin `<version>`** porque el parent la define.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
```

El **starter de tests para web**. Trae:
- JUnit 5 (lo conocés).
- AssertJ (lo conocés).
- Mockito (para mocks).
- Spring Test (`@SpringBootTest`, `MockMvc`, etc.).

Cuando llegues al Bloque 5 vas a ver `@SpringBootTest` en acción — viene de acá.

### 1.4 Los plugins

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
```

**El plugin clave de Spring Boot.** Te da:

- `mvn spring-boot:run` — un comando para correr la app sin compilar a `.jar` primero.
- `mvn package` ahora genera un **"fat jar"** (también llamado "uber jar"): un `.jar` que incluye **todas las dependencias adentro**, incluido Tomcat. Podés correrlo con `java -jar app.jar` en cualquier máquina con Java, sin instalar nada más.

> En tu Proyecto 0, `mvn package` generaba un jar **chico** (solo tu código). Faltaban las dependencias. Acá el plugin de Spring Boot las mete todas adentro.

El `<excludes>` de Lombok dice "no incluyas Lombok en el fat jar" — porque Lombok solo se necesita en compilación, no en runtime. Memoria saliendo.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    ...
    <annotationProcessorPaths>
        <path>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
        </path>
        <path>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </path>
    </annotationProcessorPaths>
</plugin>
```

Configura los **annotation processors** (recordá de la Etapa 3 del Proyecto 0). Dos processors activos:

1. **Lombok** — ya lo conocés, genera getters/setters/etc en compilación.
2. **`spring-boot-configuration-processor`** — algo nuevo. Genera metadata sobre las clases `@ConfigurationProperties` para que el IDE te dé autocompletado cuando edités `application.yml`. Pura ergonomía.

---

## 🔍 Parte 2: `application.yml`

```yaml
rest-countries:
    base-url: https://restcountries.com/v3.1
```

**Tres líneas. Eso es todo.** Pero pasa mucho.

### 2.1 ¿Qué es YAML?

YAML es un formato de configuración basado en **indentación** (como Python). Alternativa a `.properties`:

```properties
# El mismo contenido en formato .properties:
rest-countries.base-url=https://restcountries.com/v3.1
```

```yaml
# El mismo contenido en formato .yml:
rest-countries:
    base-url: https://restcountries.com/v3.1
```

**¿Cuándo conviene cada uno?**

- `.properties` — para configs simples y planas. Más viejo, todavía vigente.
- `.yml` — para configs con jerarquía o muchas opciones. Más legible cuando crece.

Spring Boot soporta **los dos formatos**. Si ponés `application.properties` y `application.yml`, te toma el que esté presente (no debería haber ambos, hay precedencia pero es confuso).

### 2.2 ¿Dónde lo busca Spring?

**Por convención**, Spring Boot busca `application.yml` (o `application.properties`) en `src/main/resources/`. Si está ahí, lo lee automáticamente al arrancar.

Si querés tener configs distintas por ambiente (dev, prod, test), podés crear:
- `application.yml` (default)
- `application-dev.yml`
- `application-prod.yml`

Y activarlas con un "profile" (ej: `--spring.profiles.active=dev`). No es lo que el profe usa todavía, pero anotalo para más adelante.

### 2.3 ¿Cómo accedés a estos valores desde el código?

Dos formas:

**Forma A: `@Value` (vieja, todavía se usa)**
```java
@Value("${rest-countries.base-url}")
private String baseUrl;
```

**Forma B: `@ConfigurationProperties` (moderna, lo que el profe usa)**
```java
@ConfigurationProperties(prefix = "rest-countries")
public class RestCountriesProperties {
    private String baseUrl;   // → mapea a rest-countries.base-url
}
```

**Lo vas a ver en detalle en el Bloque 2.** Por ahora con saber que existe alcanza.

---

## 🔍 Parte 3: `CountriesApplication.java` — el punto de entrada

El archivo completo es **chico**:

```java
package ar.edu.utn.ba.ddsi.countries;

import ar.edu.utn.ba.ddsi.countries.config.RestCountriesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RestCountriesProperties.class)
public class CountriesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CountriesApplication.class, args);
    }

}
```

### 3.1 La firma del `main` es **idéntica** a la tuya

```java
public static void main(String[] args) {
```

Mismo `main` que en el Proyecto 0. Java sigue siendo Java — Spring no cambia ese contrato.

### 3.2 La diferencia: qué hace el `main` adentro

**Tu Main del Proyecto 0:**
```java
public static void main(String[] args) {
    CatalogoPaises catalogo = new CatalogoPaises();
    // hacer cosas con el catálogo
}
```

**El Main del profe:**
```java
public static void main(String[] args) {
    SpringApplication.run(CountriesApplication.class, args);
}
```

**Solo una línea.** ¿Qué hace esa línea? **Arranca todo el framework de Spring** y le entrega el control. Es el equivalente a decir "Spring, tomá las riendas, vos te encargás".

### 3.3 `@SpringBootApplication` — la annotation maestra

```java
@SpringBootApplication
public class CountriesApplication {
```

**Es una meta-annotation**: una annotation que es alias de tres annotations juntas.

| `@SpringBootApplication` = | Significa |
|---|---|
| `@SpringBootConfiguration` | "Esta clase es fuente de configuración de la app" |
| `@EnableAutoConfiguration` | "Activá la autoconfiguración de Spring Boot" |
| `@ComponentScan` | "Escaneá este package y subpackages buscando componentes" |

Vamos una por una:

#### `@SpringBootConfiguration`

Marca esta clase como "el punto principal de configuración". Más adelante en proyectos grandes podés tener más clases `@Configuration`, pero esta es la "raíz".

#### `@EnableAutoConfiguration` — el corazón de Spring Boot

**Esto es lo que hace que Spring Boot sea "mágico".**

Spring Boot escanea las dependencias que tenés en el classpath y **decide automáticamente qué configurar**:

- Vio `spring-boot-starter-webmvc` → "necesitás un servidor Tomcat embebido en port 8080. Lo arranco".
- Vio `spring-boot-starter-data-jpa` → "necesitás un EntityManager y un DataSource. Los configuro".
- Vio `H2` en classpath → "asumo que querés una BD en memoria H2. La levanto".

**Sin esto**, tendrías que configurar a mano Tomcat, Jackson, MVC, etc. — cientos de líneas de XML que existían en Spring antes de "Boot".

> "Spring Boot" = "Spring con auto-configuración + starters". El framework subyacente sigue siendo Spring (existe desde 2002). Boot vino en 2014 a hacer todo automático.

#### `@ComponentScan` — el descubridor de beans

Spring necesita saber qué clases administrar. `@ComponentScan` le dice "escaneá este package y subpackages buscando clases anotadas con `@Component`, `@Service`, `@Repository`, `@Controller`, `@Configuration`".

Por eso `CountriesApplication` está en el package raíz `ar.edu.utn.ba.ddsi.countries` — Spring va a escanear ese package **y todos sus subpackages**:
- `ar.edu.utn.ba.ddsi.countries.config` → `RestTemplateConfig`, `RestCountriesProperties`.
- `ar.edu.utn.ba.ddsi.countries.services` → `BuscadorDePaises`.

Si la `CountriesApplication` estuviera en `ar.edu.utn.ba.ddsi`, Spring escanearía desde ahí. Si estuviera dentro de `services`, **solo escanearía services y nunca encontraría `BuscadorDePaises` ni `RestTemplateConfig`**. Por eso por convención **la clase con `@SpringBootApplication` se pone en el package raíz**.

### 3.4 `@EnableConfigurationProperties(RestCountriesProperties.class)`

```java
@EnableConfigurationProperties(RestCountriesProperties.class)
```

**Lectura:** "che Spring, registrame `RestCountriesProperties` como un bean — quiero que lo manejes vos y lo inyectes donde haga falta".

Como `RestCountriesProperties` no tiene `@Component` (vas a verlo en Bloque 2), Spring no lo registraría automáticamente. Esta annotation le dice "registralo igual, es una clase de configuración POJO".

**Lo vas a ver en detalle en Bloque 2**. Por ahora con entender que es "el registrar manual" de un bean alcanza.

### 3.5 `SpringApplication.run(CountriesApplication.class, args)`

Esta línea es la que **dispara todo**. Veamos qué pasa por dentro (resumen):

```
1. Crea el ApplicationContext (el "contenedor" de Spring)
2. Escanea el classpath por @ComponentScan
3. Lee application.yml y aplica las properties
4. Aplica @EnableAutoConfiguration → configura Tomcat, MVC, Jackson, etc.
5. Inicializa todos los @Component, @Service, @Configuration encontrados
6. Inyecta dependencias entre ellos (lo que vas a ver en Bloque 2)
7. Arranca Tomcat embebido en :8080
8. Devuelve el control al main, pero la JVM NO TERMINA porque Tomcat sigue corriendo
```

**El programa queda corriendo**. Hasta que apretás Ctrl+C o lo matás explícitamente. **Esa es la diferencia más importante con tu Proyecto 0**: el tuyo arrancaba, hacía cosas, y terminaba. Esta app arranca y **queda escuchando**.

---

## 🚀 Parte 4: Qué ves cuando arrancás el proyecto

Si tipeás `mvn spring-boot:run` o corrés el main desde IntelliJ, ves algo así:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

:: Spring Boot ::                (v4.0.5)

2026-05-24 14:35:21 - Starting CountriesApplication using Java 21
2026-05-24 14:35:22 - Tomcat initialized with port(s): 8080 (http)
2026-05-24 14:35:22 - Starting service [Tomcat]
2026-05-24 14:35:22 - Starting Servlet engine: [Apache Tomcat/...]
2026-05-24 14:35:23 - Tomcat started on port(s): 8080 (http)
2026-05-24 14:35:23 - Started CountriesApplication in 2.345 seconds
```

**El log Spring Boot famoso** con el banner ASCII. Cosas a destacar:

- **`Tomcat started on port(s): 8080`** — el servidor web está vivo. Si fueras a `http://localhost:8080/...` (con un endpoint que existiera), te respondería. El proyecto del profe **no expone endpoints HTTP** todavía — solo tiene el `BuscadorDePaises` y los tests. Por eso visitar `localhost:8080` da 404.

- **`Started CountriesApplication in 2.345 seconds`** — esto significa "todo el contexto está inicializado, todos los beans creados, Tomcat escuchando". A partir de acá la app está viva.

- **Quedó corriendo**. La consola sigue ocupada. Para detener, **Ctrl+C** en la terminal o **el botón rojo de stop en IntelliJ**.

---

## 🆚 Parte 5: Comparación lado a lado con tu Proyecto 0

Esta tabla resume todo lo que cambia:

| Aspecto | Proyecto 0 (tuyo) | rest-paises (profe) |
|---|---|---|
| **Estructura Maven** | Plain Maven, definís todo | Hereda de `spring-boot-starter-parent` |
| **Versiones de deps** | Cada una con `<version>` | Sin versions, el parent las maneja |
| **Dependencias** | Lombok, JUnit, AssertJ una por una | Starters (un solo `<dependency>` trae 10+) |
| **El `main`** | Lógica de la app directa | `SpringApplication.run(...)` y nada más |
| **Configuración** | Constantes/hardcodeo en código | `application.yml` externo |
| **Componentes** | `new` manuales (`new CatalogoPaises()`) | Spring crea por annotations (`@Component`, `@Bean`) |
| **Ejecución** | `mvn exec:java` o `java -cp ...` | `mvn spring-boot:run` o `java -jar app.jar` (fat jar) |
| **Termina cuando** | El `main` termina | Cuando matás el proceso |
| **Servidor web** | No tiene | Tomcat embebido en :8080 |

Notá una cosa: **el modelo, los DTOs, los streams, los tests con AssertJ — todo eso es idéntico**. La parte Java moderna que aprendiste en el Proyecto 0 se sigue aplicando. Lo nuevo es **Spring administra los componentes y el ciclo de vida** en vez de vos hacerlo a mano.

---

## ✅ Checkpoint

Si podés contestar mentalmente estas, estás listo para Bloque 2:

1. ¿Qué hace `spring-boot-starter-parent` y por qué evitás declarar versiones?
2. ¿Qué dependencias trae adentro `spring-boot-starter-webmvc`?
3. ¿Qué tres annotations engloba `@SpringBootApplication`?
4. ¿Por qué `CountriesApplication` se ubica en el package raíz `ar.edu.utn.ba.ddsi.countries`?
5. ¿Qué hace `SpringApplication.run(...)` por debajo?
6. ¿Por qué el programa no termina al ejecutar el `main`?
7. ¿Por qué `RestCountriesProperties` necesita `@EnableConfigurationProperties` además de existir en el package escaneado?
8. ¿Cuál es la diferencia entre un fat jar y un jar normal? ¿Quién lo genera?

---

## 🎯 Mini-experimento mental

Pensá qué pasa en cada caso:

**Caso 1:** quitás `<parent>spring-boot-starter-parent</parent>` del `pom.xml`.
→ Las dependencias siguen sin `<version>`. **Maven explota** porque no sabe qué versión bajar. Tendrías que poner `<version>` a cada dependencia.

**Caso 2:** ponés `application.properties` con `rest-countries.base-url=...` en lugar del `application.yml`.
→ **Funciona igual.** Spring Boot soporta los dos formatos.

**Caso 3:** movés `CountriesApplication` al package `services` (al mismo nivel que `BuscadorDePaises`).
→ `@ComponentScan` solo escanea `services` y subpackages. **No encuentra** `RestCountriesProperties` ni `RestTemplateConfig` (que están en `config/`). La app falla al arrancar con un error de "bean no encontrado".

**Caso 4:** quitás `@SpringBootApplication` pero dejás el resto.
→ **Spring no se entera** que esta clase es el punto de configuración. El context no arranca, no se escanean componentes. La app se cuelga o falla.

Si entendés por qué cada caso pasa lo que pasa, dominás el Bloque 1.

---

## 🔗 Lo que viene en el Bloque 2

Vamos a desarmar `RestCountriesProperties.java` y `RestTemplateConfig.java`. Vas a entender:

- Cómo `RestCountriesProperties` lee del YAML.
- Qué es **IoC** (Inversión de Control) — el concepto que define a Spring.
- Qué es un **bean** y qué hace `@Bean`.
- Cómo Spring **inyecta** un `RestTemplate` en `BuscadorDePaises` sin que vos hagas `new`.

**Es el "click mental" más importante de Spring**. Si lo entendés ahí, todo lo demás se cae solo.

---

## ▶️ Próximo paso

Cuando quieras, decime **"vamos al bloque 2"** y arrancamos con IoC, beans y la "magia" de Spring desmitificada.

Si querés volver a una etapa del Proyecto 0 antes (especialmente Etapa 7 si te quedó algo pendiente), también está bien.
