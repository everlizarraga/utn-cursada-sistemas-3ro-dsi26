# 🌱 Proyecto 1 — Etapa 0: Crear el proyecto Spring Boot desde cero

> **Objetivo:** crear tu primer proyecto Spring Boot **entendiendo qué estás creando**, no apretando botones a ciegas. Vas a ver las distintas formas de hacerlo, qué hace cada una por debajo, y cómo se conecta con el proyecto Maven que ya sabés armar.
>
> **De dónde venís:** sabés crear un proyecto Maven a mano en IntelliJ, entendés `pom.xml`, `src/main/java`, los packages. **Eso es exactamente la base que necesitás.** Spring Boot no tira nada de eso a la basura — lo usa.
>
> **Tiempo estimado:** 30-45 minutos (la mayoría es lectura para entender; crear el proyecto en sí son 5 minutos).

---

## 🧭 Mapa de esta etapa

1. La pregunta de fondo: ¿qué es "crear un proyecto Spring"?
2. Las formas de crear un proyecto Spring (panorama completo).
3. Qué es Spring Initializr y qué hace por debajo.
4. **Manos a la obra:** crearlo con el plugin de IntelliJ (la forma que vas a usar).
5. Qué te generó: recorrido por cada archivo y carpeta.
6. Comparación lado a lado con tu Maven manual del Proyecto 0.
7. El `pom.xml` que te generaron, desarmado.
8. Criterios de "Etapa 0 completa".
9. Checkpoint.

---

## 🎯 Primero: definamos el dominio

Antes de arrancar, una decisión rápida. El Proyecto 0 fue sobre "países". Para el Proyecto 1 podemos seguir con países (aprovechás la intuición) o cambiar de dominio.

**Mi sugerencia: seguí con países.** Razón: ya tenés el modelo mental armado (qué es un `Pais`, qué campos tiene), así que tu cabeza puede enfocarse 100% en **lo nuevo de Spring** en vez de gastar energía en entender un dominio nuevo. Menos carga cognitiva en lo que ya sabés = más foco en lo que importa.

Pero si "países" te aburre y preferís algo más motivante (libros, juegos, tareas, recetas, lo que sea), **decímelo y lo cambiamos**. El aprendizaje es idéntico.

> **Para el resto de este material asumo "países".** Si cambiás de dominio, solo cambian los nombres de las clases, no los conceptos.

---

## 🤔 Parte 1: ¿Qué significa "crear un proyecto Spring"?

Empecemos por desarmar la magia. Cuando tu profe fue a una página web, apretó botones, y "apareció un proyecto Spring funcionando", **¿qué pasó realmente?**

La respuesta te va a tranquilizar: **un proyecto Spring Boot ES un proyecto Maven.** El mismo `pom.xml`, la misma estructura `src/main/java`, los mismos packages que ya sabés armar.

La diferencia es **qué dependencias trae** y **un par de archivos extra**. Nada más. No es un tipo de proyecto exótico — es tu proyecto Maven de siempre, con:

1. Una **dependencia padre** especial (Spring Boot) en el `pom.xml`.
2. Un **par de "starters"** (dependencias que agrupan otras).
3. Una **clase main** con una annotation especial.
4. Un **archivo de configuración** (`application.properties` o `application.yml`).
5. Un **plugin de Maven** de Spring Boot.

**Eso es todo.** Cuando lo veas armado, vas a reconocer el 90% de lo que ya sabés.

### Entonces, ¿por qué usar una página web o un asistente?

Porque armar a mano ese `pom.xml` con las versiones correctas de Spring Boot, los starters bien elegidos, y la estructura inicial **es tedioso y propenso a errores**. Spring Boot tiene **cientos de versiones de librerías que tienen que ser compatibles entre sí**. Elegirlas a mano sería una pesadilla.

Por eso existe **Spring Initializr**: un generador que te arma ese esqueleto correcto en segundos. No es magia — es un **generador de andamiaje** (scaffolding). Te ahorra el trabajo manual, pero **lo que genera es un proyecto Maven común y corriente** que vos podrías (en teoría) haber escrito a mano.

> **Tranquilidad mental:** no estás usando una "caja negra". Estás usando un atajo que genera algo que vas a entender completamente. Después de esta etapa, si te dieran el `pom.xml` vacío, podrías agregarle Spring a mano. El Initializr solo te ahorra tipeo.

---

## 🛠️ Parte 2: Las formas de crear un proyecto Spring

Hay **tres formas principales**. Te las muestro todas para que tengas el panorama, y te marco cuál vas a usar.

### Forma A: Spring Initializr web (start.spring.io)

Es la página web. Vas a `https://start.spring.io`, elegís opciones en un formulario, apretás "Generate", y te descarga un `.zip` con el proyecto. Lo descomprimís y lo abrís en IntelliJ.

**Es lo que probablemente usó tu profe.** Funciona en cualquier IDE (incluso sin IDE).

### Forma B: El plugin de Spring Initializr dentro de IntelliJ

IntelliJ tiene integrado el mismo Initializr. En vez de ir a la web, hacés `File → New → Project → Spring Boot` (o "Spring Initializr" según la versión), llenás el mismo formulario **dentro del IDE**, y te crea el proyecto directamente, sin descargar zips ni descomprimir.

**Es la forma más cómoda** si trabajás en IntelliJ. **Es la que vas a usar.**

> **Nota:** en IntelliJ **Community** (la gratuita, que es la que usás), el plugin de Spring Initializr **puede no venir incluido** según la versión. Si no lo tenés, no pasa nada — usamos la Forma A (web) que es idéntica en resultado. Más abajo te explico cómo verificar y qué hacer en cada caso.

### Forma C: A mano (sin Initializr)

Crear un proyecto Maven vacío (como hacías en el Proyecto 0) y **agregarle Spring a mano** editando el `pom.xml`.

**No lo vamos a hacer ahora**, pero quiero que sepas que **es posible**. De hecho, después de esta etapa, vas a entender el `pom.xml` lo suficiente como para hacerlo. Es la prueba de que no hay magia: si entendés qué genera el Initializr, podés reproducirlo a mano.

> **Lo dejo marcado como "madriguera para después":** si en algún momento querés hacer el ejercicio de armar un Spring Boot a mano desde un Maven vacío, lo hacemos. Es un ejercicio buenísimo para consolidar. Pero **no ahora** — ahora usamos el atajo.

### Resumen de las formas

| Forma | Dónde | Cuándo conviene | ¿La usás ahora? |
|---|---|---|---|
| **A. Web (start.spring.io)** | Navegador | Cualquier IDE, o sin IDE | Plan B |
| **B. Plugin IntelliJ** | Dentro del IDE | Trabajás en IntelliJ | ✅ Plan A |
| **C. A mano** | Editando pom.xml | Para entender a fondo / casos especiales | 🚪 Después |

---

## 🔍 Parte 3: Qué es Spring Initializr y qué hace por debajo

Tanto la web como el plugin de IntelliJ son **la misma herramienta**: **Spring Initializr**. El plugin de IntelliJ literalmente le hace pedidos al servidor de start.spring.io por detrás.

### Qué le decís al Initializr

Cuando lo usás, llenás un formulario con estas cosas:

| Campo | Qué es | Qué vas a poner |
|---|---|---|
| **Project** | Maven o Gradle (sistema de build) | **Maven** (lo que ya sabés) |
| **Language** | Java, Kotlin, Groovy | **Java** |
| **Spring Boot version** | Qué versión del framework | La que use tu cátedra (ver abajo) |
| **Group** | El `groupId` de Maven | ej: `ar.edu.utn.ba` |
| **Artifact** | El `artifactId` de Maven | ej: `paises` |
| **Name** | Nombre del proyecto | ej: `paises` |
| **Package name** | Package raíz | ej: `ar.edu.utn.ba.paises` |
| **Packaging** | Jar o War | **Jar** |
| **Java version** | Versión de Java | **21** |
| **Dependencies** | Qué starters incluir | (ver abajo) |

**Reconocés varios de estos**, ¿no? `Group`, `Artifact`, `packaging` son **exactamente** los conceptos de Maven que ya manejás. El Initializr solo te los pide en un formulario en vez de que los escribas a mano en el `pom.xml`.

### Qué hace con eso

El Initializr toma tus respuestas y **genera**:

1. Un `pom.xml` con el padre de Spring Boot, los starters que elegiste, y el plugin.
2. La estructura de carpetas `src/main/java`, `src/main/resources`, `src/test/java`.
3. La clase main con `@SpringBootApplication`.
4. Un `application.properties` vacío.
5. Una clase de test básica.
6. El Maven Wrapper (`mvnw`).
7. Un `.gitignore`.

**Todo eso lo genera en segundos.** Es el esqueleto correcto, con versiones compatibles, listo para arrancar.

### Sobre la versión de Spring Boot

Spring Boot va por la versión **4.x** actualmente. Pero **tu cátedra usa Spring Boot 4.0.5** específicamente. Para evitarte cualquier fricción con el material del profe o con tus compañeros, **vamos a usar la misma versión que la cátedra: 4.0.5** (o la 4.0.x que te ofrezca el Initializr más cercana).

> **No te compliques con las versiones ahora.** El Initializr te va a ofrecer una lista. Si está 4.0.5, elegila. Si no, elegí la 4.0.x más cercana. Las diferencias entre patches menores (4.0.5 vs 4.0.6) son irrelevantes para lo que vamos a hacer. **Lo importante es quedarte en la línea 4.0.x** para alinear con tu cátedra.

---

## 🚀 Parte 4: Manos a la obra

Vamos a crear el proyecto. Te doy **las dos formas** (plugin IntelliJ y web), empezá por la del plugin; si no lo tenés, caés a la web.

### Primero: ¿tenés el plugin de Spring en IntelliJ?

1. Abrí IntelliJ.
2. `File → New → Project...`
3. Mirá la lista de la izquierda. Si ves una opción que diga **"Spring Boot"** o **"Spring Initializr"** → tenés el plugin, usá la **Forma B**.
4. Si **NO** la ves → usá la **Forma A** (web). Es igual de buena.

> En IntelliJ Community el soporte de Spring históricamente venía limitado o ausente, aunque versiones recientes (2024+) suelen incluir el generador de Initializr. Si no aparece, no es un problema tuyo — usás la web.

---

### Forma B: Plugin de IntelliJ (si lo tenés)

1. `File → New → Project...`
2. Seleccioná **"Spring Boot"** (o "Spring Initializr") en la lista de la izquierda.
3. Llená el formulario:

   | Campo | Valor |
   |---|---|
   | Name | `paises` |
   | Location | donde guardás tus proyectos |
   | Language | Java |
   | Type | **Maven** |
   | Group | `ar.edu.utn.ba` |
   | Artifact | `paises` |
   | Package name | `ar.edu.utn.ba.paises` |
   | JDK | 21 |
   | Java | 21 |
   | Packaging | Jar |

4. **Next**.
5. **Versión de Spring Boot:** elegí **4.0.5** (o la 4.0.x más cercana disponible).
6. **Dependencies:** acá es donde elegís los starters. **Por ahora, NO agregues ninguna dependencia.** Dejá el proyecto pelado.

   > **¿Por qué sin dependencias?** Porque queremos arrancar con lo **mínimo absoluto** y ver qué hace Spring Boot vacío. En la Etapa 1 vamos a entender el arranque sin ruido. Las dependencias (web, etc.) las sumamos cuando las necesitemos y entendamos para qué sirven. Recordá la filosofía: una pieza por vez.

7. **Create**.

IntelliJ crea el proyecto y empieza a descargar las dependencias de Spring Boot (puede tardar un minuto la primera vez).

---

### Forma A: Web (si no tenés el plugin)

1. Andá a `https://start.spring.io`
2. Llená el formulario:

   | Campo | Valor |
   |---|---|
   | Project | **Maven** |
   | Language | Java |
   | Spring Boot | **4.0.5** (o la 4.0.x más cercana) |
   | Group | `ar.edu.utn.ba` |
   | Artifact | `paises` |
   | Name | `paises` |
   | Package name | `ar.edu.utn.ba.paises` |
   | Packaging | Jar |
   | Java | 21 |

3. **Dependencies:** dejá la lista **vacía**. Sin agregar nada. (Misma razón que arriba.)
4. Apretá **"GENERATE"** (o `Ctrl + Enter`). Te descarga un `paises.zip`.
5. **Descomprimí** el zip en la carpeta donde guardás tus proyectos.
6. En IntelliJ: `File → Open...` → seleccioná la carpeta `paises` descomprimida → **Open**.
7. IntelliJ detecta que es un proyecto Maven y empieza a descargar las dependencias (puede tardar un minuto la primera vez).

---

### Verificá que se creó bien

Cuando termine de cargar, en el panel **Project** de IntelliJ tenés que ver una estructura parecida a esta:

```
paises/
├── .mvn/
│   └── wrapper/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ar/edu/utn/ba/paises/
│   │   │       └── PaisesApplication.java
│   │   └── resources/
│   │       ├── static/
│   │       ├── templates/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── ar/edu/utn/ba/paises/
│               └── PaisesApplicationTests.java
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

**Si ves eso, ya está.** Tenés tu primer proyecto Spring Boot. **Todavía no lo vamos a correr** — eso es la Etapa 1. Ahora vamos a **entender qué te generó**.

---

## 📂 Parte 5: Qué te generó, archivo por archivo

Vamos a recorrer cada cosa para que **nada sea un misterio**. Esto es lo que más te va a tranquilizar la cabeza.

### `pom.xml`

El archivo de configuración de Maven. **Ya lo conocés**, solo que este tiene cosas de Spring. Lo desarmamos en detalle en la Parte 7.

### `src/main/java/.../PaisesApplication.java`

La clase principal. Es el "main" de tu app. **Reconocés el `main`** del Proyecto 0:

```java
package ar.edu.utn.ba.paises;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PaisesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaisesApplication.class, args);
    }

}
```

**Lo que reconocés:** `public static void main(String[] args)` — el mismo main de siempre. Java sigue siendo Java.

**Lo nuevo:** la annotation `@SpringBootApplication` y la línea `SpringApplication.run(...)`. **No las vamos a explicar ahora** — son el corazón de la Etapa 1. Por ahora solo registrá que existen. En la próxima etapa las vas a entender ejecutándolas.

> Recordá la filosofía: no te pido que entiendas `@SpringBootApplication` ahora. Solo que sepas que ahí está. La Etapa 1 entera es sobre esto.

### `src/main/resources/application.properties`

Un archivo de configuración **vacío**. Acá van las configuraciones de tu app (puertos, URLs, credenciales, lo que sea). Por ahora está vacío. **Lo vamos a usar de verdad en la Etapa 5.**

> **Detalle:** el Initializr genera `application.properties` por default. Existe también `application.yml` (formato distinto, mismo propósito). Tu cátedra usa `.yml`. **No te preocupes por esto ahora** — lo tratamos en la Etapa 5 cuando lo usemos de verdad. Por ahora, dejá el `.properties` como está.

### `src/main/resources/static/` y `templates/`

Dos carpetas vacías. Se usan para apps web que sirven HTML/CSS/JS (`static`) o plantillas de servidor (`templates`). **No las vas a usar** en este proyecto. Podés ignorarlas. El Initializr las crea por las dudas.

### `src/test/java/.../PaisesApplicationTests.java`

Una clase de test básica:

```java
package ar.edu.utn.ba.paises;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaisesApplicationTests {

    @Test
    void contextLoads() {
    }

}
```

**Reconocés `@Test`** del Proyecto 0 (Etapa 7). Lo nuevo es `@SpringBootTest`. Este test verifica que la app **arranca sin errores**. Lo vamos a entender en la etapa de tests.

### `mvnw` y `mvnw.cmd`

El **Maven Wrapper**. Lo conocés del Proyecto 0 (lo creaste con `mvn wrapper:wrapper`). Acá viene incluido de fábrica. Te permite correr Maven sin tenerlo instalado globalmente (`./mvnw` en Linux/Mac, `mvnw.cmd` en Windows).

### `.mvn/wrapper/`

Archivos internos del Maven Wrapper. No los tocás.

### `.gitignore`

Lista de archivos que Git debe ignorar (como `target/`). Ya sabés qué es del Proyecto 0.

---

## 🆚 Parte 6: Comparación con tu Maven manual del Proyecto 0

Esta tabla es la que más te va a aterrizar todo. Compará lo que armaste a mano en el Proyecto 0 con lo que el Initializr generó:

| Elemento | Proyecto 0 (Maven manual) | Proyecto 1 (Spring Boot) |
|---|---|---|
| `pom.xml` | Lo escribiste vos | Generado, con padre de Spring Boot |
| `src/main/java` | ✅ Igual | ✅ Igual |
| `src/main/resources` | Lo creaste cuando necesitaste el CSV | ✅ Viene de fábrica |
| `src/test/java` | ✅ Igual | ✅ Igual |
| Clase con `main` | `Main.java` (la escribiste) | `PaisesApplication.java` (generada) |
| El `main` en sí | Lógica de tu app directa | `SpringApplication.run(...)` |
| Maven Wrapper | Lo creaste con un comando | ✅ Viene de fábrica |
| `.gitignore` | Lo creaste vos | ✅ Viene de fábrica |
| Packages | Los creaste vos | ✅ El raíz viene creado |

**Mirá la columna del medio y la derecha:** la mayoría de las filas dicen "igual" o "viene de fábrica". **El Proyecto 1 es tu Proyecto 0 con Spring agregado y algunas cosas pre-armadas.** No es un mundo nuevo. Es el mismo mundo Maven, con un framework encima.

**Lo único realmente nuevo:**
- El `pom.xml` tiene el padre de Spring Boot y starters.
- El `main` llama a `SpringApplication.run(...)` en vez de tener tu lógica directa.
- La annotation `@SpringBootApplication`.

Y eso es exactamente lo que vamos a entender en la Etapa 1. **El resto ya lo conocés.**

---

## 🔬 Parte 7: El `pom.xml` generado, desarmado

Abrí el `pom.xml`. Te va a parecer más cargado que el del Proyecto 0, pero la mayoría ya lo entendés. Vamos parte por parte.

### El esqueleto general

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project ...>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.5</version>
        <relativePath/>
    </parent>

    <groupId>ar.edu.utn.ba</groupId>
    <artifactId>paises</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>paises</name>
    <description>paises</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

### Lo que ya conocés del Proyecto 0

```xml
<modelVersion>4.0.0</modelVersion>
<groupId>ar.edu.utn.ba</groupId>
<artifactId>paises</artifactId>
<version>0.0.1-SNAPSHOT</version>
<properties>
    <java.version>21</java.version>
</properties>
```

**Todo esto es idéntico** a lo que tenías en el Proyecto 0. `groupId`, `artifactId`, `version`, la versión de Java. Cero novedad.

### Lo nuevo #1: El `<parent>`

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
</parent>
```

**Esto es lo más importante del `pom.xml`.** Dice: "este proyecto **hereda** de otro `pom.xml` llamado `spring-boot-starter-parent`".

Sí, **los `pom.xml` se heredan como las clases en Java.** Tu `pom.xml` recibe automáticamente un montón de configuración del "padre":

- **Las versiones de cientos de librerías** ya elegidas y probadas como compatibles entre sí. Por eso vas a ver dependencias **sin `<version>`** más abajo — la versión la define el padre.
- Configuración de plugins, encoding UTF-8, versión de Java, etc.

> **Por qué importa:** en el Proyecto 0 tuviste que poner `<version>` a Lombok, JUnit, AssertJ a mano. Con este padre, **no hace falta** — Spring Boot ya sabe qué versión de cada cosa es compatible. Te ahorra el infierno de elegir versiones compatibles a mano.

**No necesitás entender todo lo que hace el padre ahora.** Solo registrá: "heredo configuración y versiones de Spring Boot". Suficiente.

### Lo nuevo #2: El starter principal

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
```

**Notá que NO tiene `<version>`** (la define el padre).

Esto es un **"starter"**: una dependencia que **trae adentro un montón de otras dependencias agrupadas**. `spring-boot-starter` (el básico) trae el núcleo de Spring: el contenedor de beans, la autoconfiguración, logging, etc. — todo lo mínimo para que Spring funcione.

> **Concepto clave:** un starter es como un "combo". En vez de declarar 10 dependencias sueltas, declarás un starter que las trae todas juntas, ya probadas. Es una de las cosas que hizo a Spring Boot tan popular.

**Este es el único starter "de runtime" que tenés ahora** porque creamos el proyecto sin agregar dependencias. En etapas futuras vamos a sumar otros (como el web, cuando lo necesitemos).

### Lo nuevo #3: El starter de test

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

El starter de testing. Trae **JUnit 5, AssertJ, Mockito y utilidades de test de Spring** — todo en una dependencia. **Reconocés JUnit y AssertJ** del Proyecto 0. Acá vienen incluidos automáticamente.

El `<scope>test</scope>` ya lo conocés: significa "solo disponible en tests, no va al jar final".

### Lo nuevo #4: El plugin

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

El plugin de Maven de Spring Boot. Te da el comando `mvn spring-boot:run` (para correr la app) y hace que `mvn package` genere un "fat jar" (un jar con todo adentro, ejecutable con `java -jar`).

> **No necesitás profundizar en el plugin ahora.** Solo saber que está y que te permite correr la app fácil. Lo vas a usar en la Etapa 1.

### Resumen del `pom.xml`

| Parte | ¿Nuevo o conocido? |
|---|---|
| `groupId`, `artifactId`, `version`, Java 21 | ✅ Conocido del Proyecto 0 |
| `<parent>` Spring Boot | 🆕 Heredás versiones y config |
| `spring-boot-starter` | 🆕 Combo del núcleo de Spring |
| `spring-boot-starter-test` | 🆕 Combo de testing (JUnit + AssertJ + más) |
| `spring-boot-maven-plugin` | 🆕 Plugin para correr y empaquetar |

**Cinco cosas nuevas, todas relacionadas con "traer Spring".** El resto es tu Maven de siempre.

---

## ✅ Parte 8: Criterios de "Etapa 0 completa"

- [ ] Decidiste el dominio (países u otro).
- [ ] Creaste el proyecto Spring Boot (con el plugin de IntelliJ o con la web).
- [ ] El proyecto abrió en IntelliJ y terminó de descargar las dependencias (sin errores rojos).
- [ ] Ves la estructura: `PaisesApplication.java`, `application.properties`, `pom.xml`, `mvnw`.
- [ ] Abriste el `pom.xml` y reconocés qué es cada parte (parent, starters, plugin).
- [ ] Entendés que un proyecto Spring Boot **es un proyecto Maven** con Spring agregado.
- [ ] **NO corriste la app todavía** (eso es la Etapa 1).

---

## ✅ Checkpoint

Si podés contestar mentalmente estas, estás listo para la Etapa 1:

1. ¿Un proyecto Spring Boot es un tipo de proyecto distinto a Maven, o es un proyecto Maven con cosas agregadas?
2. ¿Qué es Spring Initializr y qué hace?
3. ¿Cuáles son las tres formas de crear un proyecto Spring? ¿Cuál usaste?
4. ¿Qué hace el `<parent>` de Spring Boot en el `pom.xml`?
5. ¿Qué es un "starter"? ¿Por qué las dependencias no tienen `<version>`?
6. ¿Por qué creamos el proyecto sin agregar dependencias extra?
7. ¿Qué archivo tiene el `main`? ¿Reconocés la firma del `main`?

> **Si alguna no te sale**, no avances — preguntá. Mejor aclararlo ahora que arrastrar la duda. Recordá: este proyecto está hecho para que **no se te acumulen dudas**.

---

## 🎯 Una reflexión antes de seguir

Fijate lo que pasó acá: el profe fue a una web, apretó botones, y apareció un proyecto. Para él (y para vos en ese momento) eso era **magia**. Ahora **no lo es más**. Sabés:

- Qué generó (un proyecto Maven con Spring).
- Por qué cada archivo está ahí.
- Qué hace el `pom.xml` y por qué tiene esas partes.
- Que podrías (en teoría) haberlo hecho a mano.

**Eso es la diferencia entre "usar" y "entender".** Vamos a hacer esto con cada pieza de Spring: desarmar la magia hasta que sea código que entendés.

---

## ▶️ Próximo paso

Cuando tengas el proyecto creado y hayas recorrido el `pom.xml`, decime **"arranquemos etapa 1"** y vamos a **correr la app por primera vez**.

En la Etapa 1 vas a:
- Correr el Spring Boot vacío y ver el log y el banner.
- Entender qué es ese proceso que queda vivo.
- Entender qué hace `SpringApplication.run(...)` y `@SpringBootApplication`.
- Matar la app y arrancarla de nuevo.

Recién ahí, viendo el arranque con tus ojos, vas a entender **qué es Spring realmente**.

Si algo de esta etapa no te cerró, preguntá antes de avanzar.
