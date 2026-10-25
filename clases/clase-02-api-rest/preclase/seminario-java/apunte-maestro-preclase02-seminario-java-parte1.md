# 📘 APUNTE MAESTRO — Preclase 02 · Parte 1
## Herramientas: Git y Maven

**Unidad:** `preclase02` — material previo obligatorio de la clase 2 (01/04/2026)
**Eje temático:** Arquitectura de Software (la clase 2 en sí trata Cliente-Servidor, HTTP y API REST)
**Fuentes fusionadas:** los 4 videos del *Seminario de Tecnología Aplicada al Diseño*, sus slides, el enunciado y el diagrama de "Vuelos y Aeropuertos", y el repositorio de la cátedra.

---

## 🧭 Cómo leer esto

Este apunte cubre **todo** el material previo de la clase 2, sin omisiones. Pero no todo pesa igual, así que hay dos rutas:

| Ruta | Para quién | Qué leer |
|---|---|---|
| **Completa** | Primera vez con Java / Maven | Todo, en orden. Las partes están ordenadas por dependencia de conceptos, no por orden de los videos. |
| **Repaso** | Ya lo viste, querés chequear que no te falte nada | Solo las secciones 🔴, los bloques **"Para el parcial"**, y el **checkpoint** del final. Si respondés el checkpoint sin abrir el apunte, no te falta nada. Si uno te traba, ahí está tu hueco. |

**Las marcas:**
- 🔴 **Central.** Se usa todo el año, y/o es evaluable.
- 🟡 **Secundario.** Hay que saberlo, pero no es el corazón.
- 🟢 **Al pasar.** Cultura general. Saber que existe alcanza.

---

## 0. Qué es este material y de dónde sale 🟡

La cátedra pide, como **lectura previa obligatoria** de la clase 2, un *"Video de introducción a Java + Testing unitario con JUnit"*. Ese enlace apunta a una carpeta con **cuatro videos**, que en conjunto forman el **Seminario de Tecnología Aplicada al Diseño**:

| Video | Contenido | Dónde vive en este apunte |
|---|---|---|
| 1 | Git, Maven, teoría de Java | Partes 1, 2 y 3 |
| 2 | Crear el proyecto Maven y configurar el `pom.xml` | Parte 1 |
| 3 | Práctica en vivo: implementar "Vuelos y Aeropuertos" | Partes 3 y 5 |
| 4 | Tu primer test con JUnit | Parte 4 |

**Dos advertencias de encuadre, y son importantes:**

**1. Es material de archivo.** Los videos son de años anteriores. Sus slides hablan de **Java 8** y **JUnit 4**; el repositorio de la cátedra también. **Este año la materia trabaja con Java 21, Maven 3.9+, JUnit 5 y Lombok.** Todo lo que enseñan los videos sigue siendo válido y corre igual en Java 21 (Java es compatible hacia atrás), pero **las versiones de los videos están vencidas**. Este apunte está escrito para el stack de este año.

**2. Es material habilitante, no evaluable.** Esto no te suma puntos en un parcial de *Diseño de Sistemas*: te habilita a leer el código que los profes van a mostrar en clase y a escribir el Trabajo Práctico Anual. Es infraestructura. Se paga una vez y sirve todo el año.

> 🕳️ **Madriguera — ¿por qué Java, si esto es una materia de diseño?**
> Porque el diseño orientado a objetos necesita un lenguaje donde practicarlo, y Java lo expresa de forma explícita: las interfaces, las clases abstractas y los tipos están **escritos** en el código, no implícitos. Eso hace que un diagrama UML y su implementación se parezcan. La materia usa Java como vehículo, no como tema.
> *Volvé al camino — el lenguaje es la herramienta; lo evaluable es el diseño.*

---

## 1. Git 🟢

**Compacto a propósito.** Si venís usando Git y GitHub, esta sección es un repaso de cinco minutos: pasá a Maven. Está acá porque el material previo la incluye y porque el TPA se entrega por GitHub.

### 1.1 Qué es

**Git es un sistema de control de versiones.** Lo creó Linus Torvalds (el de Linux) en 2005. Gestiona el código fuente de un proyecto: qué cambió, quién lo cambió y cuándo.

Dos propiedades definen cómo se trabaja con él:

- **Es distribuido.** Cada desarrollador tiene una copia **completa** del proyecto en su máquina. Podés romper lo que quieras localmente: hasta que no sincronices con el repositorio remoto, no afectás a nadie.
- **Registra cada cambio.** Cuando algo se rompe y llega al repositorio central, el historial te dice **en qué momento** se rompió y **qué** lo rompió. Sin eso, tenés que revisar el proyecto entero.

### 1.2 Cómo se estructura

- **Repositorio:** la caja donde vive el proyecto. Por convención, **uno por proyecto**.
- **Ramas (branches):** copias de trabajo dentro del repositorio. Arrancás con una sola —`main` o `master`— y se van abriendo ramas para trabajar sin tocarla. Un cambio no entra a `main` hasta estar aprobado.

⚠️ **Git ≠ GitHub.** Git es la tecnología; GitHub, GitLab y Bitbucket son **plataformas que la usan**. La materia usa GitHub.

### 1.3 Comandos

Podés usar interfaz gráfica (GitHub Desktop, o el panel de IntelliJ) o la línea de comandos. La cátedra pide que, aunque uses botones, **sepas a qué comando equivale cada botón**.

Esta es la lista que da el seminario, tal cual:

| Comando | Qué hace |
|---|---|
| `git config --global user.name "TU NOMBRE"` | Setea tu nombre de usuario (una sola vez) |
| `git config --global user.email "TU EMAIL"` | Ídem, tu email |
| `git clone <url_repositorio_remoto>` | Baja el repositorio remoto a tu máquina |
| `git status` | Qué archivos modificaste, cuáles están en *stage* |
| `git pull` | Trae los cambios que tus compañeros subieron |
| `git add <path>` | Manda un archivo al *stage* (`git add .` = todos) |
| `git reset <path>` | Lo contrario: saca del *stage* |
| `git commit -m "Algún mensaje"` | Confirma los cambios del *stage*, con mensaje |
| `git push origin HEAD` | Sube tus commits al repositorio remoto |
| `git checkout <path>` | Te movés a otra rama (`-b` la crea) |

**El ciclo de trabajo es:** `pull` → programás → `add` → `commit` → `push`.

🔴 **La regla que más se olvida:** **siempre `pull` antes de `push`.** Si un compañero subió algo después de tu último `pull`, tu historial está atrasado y Git te va a rechazar el `push`. Traés lo suyo primero, después subís lo tuyo.

**Commits chicos.** Si un commit toca 70 archivos, el mensaje no puede explicar qué hiciste, y cuando algo se rompa no vas a poder aislar la causa. Cambios chicos, mensajes claros.

> 🕳️ **Madriguera — `merge` y `rebase`**
> Son las dos formas de traer los cambios de una rama a otra. **`merge`** fusiona las dos historias y deja un commit de unión que registra que hubo dos caminos. **`rebase`** reescribe tus commits para que parezca que siempre trabajaste sobre la última versión: la historia queda lineal y limpia, pero *reescribe* commits (nunca lo hagas sobre una rama que tus compañeros ya se bajaron). En el TPA, con `merge` alcanza.
> *Volvé al camino — con el ciclo `pull → add → commit → push` cursás la materia.*

---

## 2. Maven 🔴

Acá sí frená. Maven es la herramienta con la que vas a construir, compilar y testear **todo** lo que hagas este año.

### 2.1 Qué es y qué resuelve

**Maven es una herramienta de gestión y construcción de proyectos.** Se lo suele resumir como "gestor de dependencias", que es su función más visible, pero hace tres cosas:

1. **Gestiona dependencias:** las bibliotecas de terceros que tu proyecto necesita. Vos declarás cuáles querés; Maven las descarga y las pone a disposición.
2. **Estandariza la estructura del proyecto:** todo proyecto Maven tiene las carpetas en el mismo lugar. Abrís cualquier proyecto Maven del mundo y sabés dónde está cada cosa.
3. **Automatiza las tareas de construcción:** compilar, correr los tests, empaquetar.

Sirve para Java y para cualquier lenguaje que corra sobre la JVM (Kotlin, Scala, Groovy).

**🎯 Si venís de JavaScript, este es el mapa que necesitás:**

| npm / Node | Maven / Java | Comentario |
|---|---|---|
| `package.json` | `pom.xml` | El archivo que define el proyecto |
| `"dependencies": { ... }` | `<dependencies>` | Lista de bibliotecas de terceros |
| npm registry | **Maven Central** | El repositorio público de donde se bajan |
| `node_modules/` | `~/.m2/repository/` | Dónde quedan guardadas localmente |
| `npm install` | `mvn install` | Descargar + construir |
| `npm test` | `mvn test` | Correr los tests |
| `"scripts": { ... }` | *plugins* y *fases* | Maven no inventa scripts: tiene un ciclo de vida fijo |

**La diferencia de mentalidad:** en npm los scripts los definís vos. En Maven el **ciclo de vida está predefinido** y siempre es el mismo: `validate → compile → test → package → verify → install`. Vos no inventás fases, las usás.

Y una diferencia práctica que se agradece: las dependencias **no se guardan dentro del proyecto**. Van a un repositorio local compartido (`~/.m2/`), así que dos proyectos que usan la misma biblioteca la bajan una sola vez. No hay `node_modules` de 400 MB por proyecto.

### 2.2 La estructura estándar

Maven impone dónde va cada cosa. Esta es la estructura, y es la que vas a ver en el repositorio de la cátedra y en tu TPA:

```
mi-proyecto/
├── pom.xml                    ← el archivo de configuración. El corazón.
├── src/
│   ├── main/
│   │   ├── java/              ← ACÁ va tu código. Tus clases .java
│   │   └── resources/         ← archivos de configuración, datos, etc.
│   └── test/
│       └── java/              ← ACÁ van tus tests. Espejan la estructura de main/java
└── target/                    ← lo que Maven GENERA al compilar (.class, .jar)
                                 No se toca a mano. No se sube a Git.
```

🔴 **La regla de oro de la estructura:** los tests viven en `src/test/java` y **replican el mismo árbol de paquetes** que el código. Si tu clase es `domain/viajes/Vuelo.java`, su test es `src/test/java/domain/viajes/VueloTest.java`. Maven lo espera así, e IntelliJ también.

**`target/` es desechable.** Es lo compilado. Se puede borrar entero y regenerar. Por eso está en el `.gitignore`.

### 2.3 El `pom.xml` 🔴

Es el archivo que define el proyecto. XML: etiquetas que abren y cierran, como HTML.

En su versión mínima:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <!-- La versión del formato del POM. Siempre 4.0.0. No la toques. -->
    <modelVersion>4.0.0</modelVersion>

    <!-- LAS COORDENADAS del proyecto: estas tres etiquetas lo identifican
         de forma única en todo el universo Maven. -->
    <groupId>org.example</groupId>          <!-- quién lo hace (dominio invertido) -->
    <artifactId>vuelos</artifactId>         <!-- cómo se llama este proyecto        -->
    <version>1.0-SNAPSHOT</version>         <!-- en qué versión está                -->

</project>
```

**Las tres coordenadas (`groupId` / `artifactId` / `version`)** son el concepto central de Maven, y las vas a ver dos veces:
- **Para identificar tu proyecto** (arriba).
- **Para pedir una dependencia** (abajo). Cuando pedís una biblioteca, la pedís por sus tres coordenadas.

Es la misma idea que `"nombre": "versión"` en npm, solo que con un nivel más de espacio de nombres para evitar colisiones.

> 🕳️ **Madriguera — `SNAPSHOT`**
> El sufijo `-SNAPSHOT` (como en `1.0-SNAPSHOT`) marca una versión **en desarrollo, todavía inestable**. Maven las trata distinto de las versiones finales: las re-descarga periódicamente porque asume que pueden cambiar bajo el mismo número. Una versión sin `SNAPSHOT` es inmutable: `junit:4.12` siempre es el mismo `4.12`.
> *Volvé al camino — en tu TPA, `SNAPSHOT` es simplemente lo que IntelliJ pone por defecto y está bien así.*

### 2.4 Configurar la versión de Java 🔴

Maven necesita saber para qué versión de Java compilar. Se declara en `<properties>`:

```xml
<properties>
    <!-- Estas dos líneas le dicen al compilador: usá Java 21. -->
    <maven.compiler.source>21</maven.compiler.source>   <!-- versión del código que escribo -->
    <maven.compiler.target>21</maven.compiler.target>   <!-- versión del bytecode que genero -->

    <!-- Codificación de los archivos fuente. Sin esto, Maven te avisa y
         los acentos y las ñ pueden salir mal según el sistema operativo. -->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

⚠️ **Vas a ver esto configurado de dos formas distintas y hacen lo mismo.** La otra forma es dentro de un bloque `<build>`, con el `maven-compiler-plugin`:

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.1</version>
            <configuration>
                <source>21</source>
                <target>21</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Ambas configuran exactamente lo mismo. **Con `<properties>` alcanza** — es más corto y es lo que hace el material más nuevo de la cátedra. Si ves las dos en el mismo `pom` (el repo del seminario las tiene), no te vuelvas loco: está duplicado, no hay una que "gane" en secreto.

### 2.5 Qué es una dependencia 🔴

Una **dependencia** es una biblioteca hecha por terceros que querés usar en tu proyecto, porque resuelve algo que no tiene sentido que programes vos.

Se declaran así:

```xml
<dependencies>

    <dependency>
        <groupId>org.junit.jupiter</groupId>       <!-- quién la hizo    -->
        <artifactId>junit-jupiter</artifactId>     <!-- qué biblioteca   -->
        <version>5.10.2</version>                  <!-- qué versión      -->
        <scope>test</scope>                        <!-- dónde se usa     -->
    </dependency>

</dependencies>
```

Las tres coordenadas las sacás de **Maven Central** (`mvnrepository.com`): buscás la biblioteca, elegís la versión, y el sitio te da el bloque XML listo para pegar.

🔴 **Pegar el XML NO alcanza.** Cuando pegás una dependencia, por ahora es solo texto. Hay que **recargar el proyecto** para que Maven la descargue: en IntelliJ aparece un ícono de recarga (🔄) arriba a la derecha del editor apenas tocás el `pom`; en consola, `mvn install`. Recién ahí la biblioteca existe.

**`<scope>` — dónde se usa la dependencia:**

| Scope | Significado | Ejemplo |
|---|---|---|
| *(ninguno)* | Disponible en todos lados. Es el default. | La mayoría |
| `test` | **Solo en `src/test`.** No se empaqueta con la aplicación. | JUnit |
| `provided` | Se necesita para compilar, pero **no se empaqueta**: alguien más la provee en ejecución. | Lombok |

**Transitividad:** si pedís una biblioteca que a su vez usa otras tres, **Maven baja las tres también**, sin que las declares. Igual que npm. No tenés que perseguir dependencias de dependencias.

### 2.6 Los comandos de Maven 🔴

| Comando | Qué hace |
|---|---|
| `mvn clean` | **Borra `target/`** — todo lo compilado. |
| `mvn compile` | Compila `src/main/java` → `.class` en `target/` |
| `mvn test` | Compila (si hace falta) y **corre los tests** |
| `mvn package` | Todo lo anterior + empaqueta en un `.jar` |
| `mvn install` | Todo lo anterior + deja el `.jar` en tu repositorio local (`~/.m2/`), disponible para otros proyectos tuyos |

🔴 **Por qué existe `mvn clean` y por qué lo vas a usar todo el tiempo.**

Los comandos de Maven **no borran lo que ya está compilado**. Si `target/` tiene código viejo y corrés `mvn test`, es posible que estés **testeando la versión anterior de tu código** y no la que acabás de escribir. El síntoma es desesperante: cambiás algo, el test sigue fallando igual, y jurás que el cambio está bien.

Por eso el comando que vas a tipear en la vida real es:

```bash
mvn clean install
```

Primero limpia, después construye. Es una sola línea porque **`clean` es un ciclo de vida separado** del resto: no lo hace ningún otro comando.

> **⚠️ Error del material.** En el Video 1 (min. 19:29) se dice que *"`mvn install` hace compile, test y clean"*. **Es incorrecto: `install` NO hace `clean`.** De hecho, el propio video explica un minuto antes por qué hay que limpiar aparte — se contradice solo. Si te lo preguntan: `clean` es un ciclo aparte, y por eso existe el idiom `mvn clean install`.

> 🕳️ **Madriguera — `mvn eclipse:eclipse` y `mvn idea:idea`**
> Las slides del seminario dedican cuatro pantallas a estos comandos: generaban a mano los archivos de configuración del IDE (`.classpath`, `.iml`) a partir del `pom`. **Están obsoletos.** IntelliJ y Eclipse hoy leen el `pom.xml` directamente y se configuran solos: abrís la carpeta y listo. El propio video lo admite ("si usan IntelliJ, háganlo por la interfaz gráfica"). Si alguna vez ves el proyecto "roto" en el IDE sin razón aparente, el equivalente moderno es el botón de recargar el proyecto Maven.
> *Volvé al camino — no vas a tipear estos comandos nunca.*

> 🕳️ **Madriguera — Gradle**
> Es la alternativa a Maven. Hace lo mismo (dependencias + build) pero se configura con código (Groovy o Kotlin) en vez de XML, y suele ser más rápido. Domina el mundo Android. Cuando crees un proyecto en IntelliJ vas a ver la opción al lado de Maven: **elegí Maven**, que es lo que usa la cátedra.
> *Volvé al camino.*

### 2.7 El `pom.xml` completo para este año 🔴

Este es el `pom` mínimo y correcto para el stack de la materia (**Java 21 · JUnit 5 · Lombok**), sin nada de más:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- ── Coordenadas del proyecto ────────────────────────────────── -->
    <groupId>org.example</groupId>
    <artifactId>vuelos</artifactId>
    <version>1.0-SNAPSHOT</version>

    <!-- ── Configuración del compilador ────────────────────────────── -->
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- ── Dependencias ────────────────────────────────────────────── -->
    <dependencies>

        <!-- JUnit 5 (Jupiter). El artefacto "junit-jupiter" es el AGREGADOR:
             trae la API (@Test, Assertions) Y el motor que corre los tests.
             scope=test → no se empaqueta con la aplicación. -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>

        <!-- Lombok. Genera getters, setters y constructores en tiempo de
             compilación, a partir de anotaciones.
             scope=provided → hace falta para compilar, pero no viaja
             dentro del .jar final: el código ya quedó generado. -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.34</version>
            <scope>provided</scope>
        </dependency>

    </dependencies>

</project>
```

🔴 **Ojo con esto, que es la trampa más cara del material.** Los videos y las slides declaran la dependencia de JUnit así:

```xml
<artifactId>junit-jupiter-api</artifactId>   <!-- ⚠️ SOLO la API -->
```

`junit-jupiter-api` trae **la API** (las anotaciones `@Test`, la clase `Assertions`) pero **no el motor que ejecuta los tests**. ¿Qué pasa entonces?

- **En IntelliJ tus tests corren igual** — el IDE trae su propio motor y tapa el problema.
- **`mvn test` desde la consola no corre nada** y te dice algo como *"no tests to run"*.

Y ahí te volvés loco buscando el error en tu código, cuando el problema está en el `pom`. **Usá `junit-jupiter` a secas** (el agregador, como en el bloque de arriba): trae API + motor. Si el TPA se corrige con `mvn test`, esta línea es la diferencia entre "aprobado" y "no corre".

### 2.8 Lombok 🟡

Escribir getters y setters a mano para cada atributo es tedioso y llena las clases de ruido. **Lombok los genera por vos, en tiempo de compilación, a partir de anotaciones.**

```java
import lombok.Getter;
import lombok.Setter;

@Getter   // genera getNombre(), getApellido(), getDireccion()
@Setter   // genera setNombre(...), setApellido(...), setDireccion(...)
public class Persona {
    private String nombre;
    private String apellido;
    private Direccion direccion;

    public String obtenerNombreCompleto() {
        return nombre + " " + apellido;
    }
}

// ¿CÓMO FUNCIONA?
// 1. Vos escribís la clase con los atributos privados y NADA más.
// 2. Al compilar, Lombok lee las anotaciones y ESCRIBE los getters y setters
//    en el bytecode, como si los hubieras tipeado.
// 3. En tu código fuente no aparecen, pero existen y los podés llamar:
//
//    Persona p = new Persona();
//    p.setNombre("Ada");
//    p.setApellido("Lovelace");
//    System.out.println(p.obtenerNombreCompleto());
//    // Resultado esperado: Ada Lovelace
```

⚠️ **La trampa de Lombok:** necesita que el IDE tenga habilitado el *annotation processing*. Las versiones nuevas de IntelliJ lo activan solas al detectar la dependencia, pero si no, el síntoma es incomprensible: **el IDE te dice que `setNombre()` no existe**, aunque la anotación está puesta. Si te pasa: `Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing`.

Otras anotaciones útiles que vas a ver: `@AllArgsConstructor` y `@NoArgsConstructor` (constructores), `@Data` (el combo: getters + setters + `toString` + `equals` + `hashCode`).

---

## 📌 Para el parcial, si te preguntan

Esta parte es **infraestructura**, y la probabilidad de que la evalúen en un parcial de Diseño de Sistemas es baja. Aun así, los dos conceptos que sí podrían aparecer en una pregunta conceptual:

> **¿Qué es una dependencia y cómo la gestiona Maven?**
> Una dependencia es una biblioteca externa que el proyecto necesita. Se declara en el `pom.xml` mediante sus tres coordenadas (`groupId`, `artifactId`, `version`), y Maven la descarga de un repositorio remoto —típicamente Maven Central— junto con **todas sus dependencias transitivas**, sin que haya que declararlas una por una.

> **¿Qué diferencia hay entre Git y GitHub?**
> Git es el sistema de control de versiones distribuido; GitHub es una plataforma que hospeda repositorios Git y agrega servicios encima (colaboración, permisos, integración continua). GitHub usa Git, pero no son lo mismo: hay otras plataformas —GitLab, Bitbucket— que también lo usan.

---

## ✅ Checkpoint — Parte 1

Sin mirar el apunte. Las respuestas van al complemento.

1. ¿Por qué siempre hay que hacer `pull` antes de `push` cuando trabajás en la misma rama que un compañero?
2. ¿Qué hay dentro de `target/` y por qué no se sube a Git?
3. Nombrá las tres coordenadas que identifican una dependencia en Maven. ¿De dónde se sacan?
4. ¿Qué hace exactamente `mvn clean` y por qué el comando que se usa en la práctica es `mvn clean install` y no `mvn install` a secas?
5. Declarás una dependencia nueva en el `pom.xml` y guardás el archivo. ¿Ya podés usarla en tu código? ¿Por qué?
6. ¿Qué significa `<scope>test</scope>` y por qué JUnit lo lleva?
7. Tus tests corren perfecto en IntelliJ, pero `mvn test` en la consola dice que no encontró ninguno. ¿Cuál es la causa más probable?
8. ¿Qué genera Lombok, en qué momento, y por qué el IDE puede decirte que un `setter` "no existe" aunque la anotación esté puesta?
9. Si tu clase es `src/main/java/domain/viajes/Vuelo.java`, ¿dónde tiene que estar su test y con qué nombre?

---

## ⚠️ Errores del material previo (para que no te pasen a vos)

Los videos son material de ayudantes, de años anteriores. Estos tres errores están en el material y **el apunte de arriba ya los corrige** — se listan solo para que los reconozcas si volvés a las fuentes.

1. **"`mvn install` hace compile, test y clean"** (Video 1, 19:29). **No.** `install` no hace `clean`; `clean` es un ciclo de vida separado. Por eso existe `mvn clean install`.
2. **La dependencia de JUnit del material es `junit-jupiter-api`**, que trae la API pero no el motor de ejecución. Los tests corren en el IDE y no corren con `mvn test`. Usá `junit-jupiter`.
3. **Las versiones del material están vencidas** (Java 8, JUnit 4.12). Este año es **Java 21 y JUnit 5**. Nada de lo que enseñan se rompe en Java 21; solo cambian los números.

---

**Lo que viene — Parte 2: Java, el lenguaje.** Cómo Java es compilado *e* interpretado a la vez, qué hay dentro de la JVM, los tipos primitivos y sus wrappers, los modificadores de acceso (incluido el que el video se saltea), y la sintaxis Java de la POO que ya sabés.

**FIN DE LA PARTE 1**
