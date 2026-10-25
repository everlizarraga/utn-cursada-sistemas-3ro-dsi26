# 🛠️ Proyecto 0 — Anexo de Etapa 0: Correr el proyecto desde consola

> **Para qué es este archivo:** entender qué hace IntelliJ por debajo cuando apretás el triángulo verde. Aprender a compilar y correr Java desde la terminal, tanto a mano (`javac` + `java`) como con Maven.
>
> **Por qué importa:** en el mundo real, los servidores no tienen IntelliJ. Los pipelines de CI/CD corren comandos en consola. Saber esto te saca de la dependencia visual del IDE.

---

## 🧭 Mapa del anexo

1. Qué hace IntelliJ por debajo
2. Forma 1: a mano con `javac` + `java`
3. Forma 2: con Maven Wrapper (`mvnw`)
4. Forma 3: empaquetar y correr un `.jar`
5. El ciclo de vida de Maven (lifecycle)
6. Cuándo usar cada forma

---

## 1. Qué hace IntelliJ por debajo

Cuando apretás el triángulo verde, IntelliJ internamente hace **dos pasos**:

1. **Compilar** el código fuente (`.java`) a bytecode (`.class`).
2. **Ejecutar** ese bytecode con la JVM.

Eso es todo. Lo único que IntelliJ agrega es el atajo visual y el manejo del classpath (dónde buscar clases). Si entendés esos dos pasos, podés hacerlo desde cualquier terminal.

---

## 2. Forma 1: a mano con `javac` + `java`

Esta es la forma más cruda. No la vas a usar en proyectos reales, pero te muestra qué pasa por debajo.

### 2.1 Abrir terminal en la carpeta del proyecto

Hay dos formas:

**Opción A — terminal integrada de IntelliJ:**
- Menú `View → Tool Windows → Terminal` (o `Alt+F12`).
- Se abre abajo, ya posicionada en la raíz del proyecto.

**Opción B — terminal del sistema:**
- Windows: en el explorador, navegá hasta la carpeta `paises-local`, click en la barra de dirección, tipeá `cmd` + Enter.
- Mac/Linux: abrí Terminal, `cd /ruta/a/paises-local`.

Confirmá que estás en el lugar correcto:

```bash
ls    # Mac/Linux
dir   # Windows
```

Tenés que ver: `pom.xml`, `src/`, etc.

### 2.2 Compilar con `javac`

`javac` es el **compilador de Java**. Convierte `.java` → `.class`.

```bash
javac -d target/classes src/main/java/ar/edu/utn/ba/proyecto0/Main.java
```

**Qué dice esto:**

| Trozo | Significa |
|---|---|
| `javac` | "Compilador, vení acá" |
| `-d target/classes` | "Poné los `.class` en la carpeta `target/classes`" |
| `src/.../Main.java` | "Compilá este archivo" |

Si no hay errores, no imprime nada (silencio = éxito). Si hay errores, te los muestra.

Andá a explorar `target/classes/` — vas a encontrar `Main.class` adentro de la jerarquía de carpetas del package (`ar/edu/utn/ba/proyecto0/Main.class`).

### 2.3 Ejecutar con `java`

`java` es la **JVM**. Ejecuta bytecode.

```bash
java -cp target/classes ar.edu.utn.ba.proyecto0.Main
```

**Qué dice esto:**

| Trozo | Significa |
|---|---|
| `java` | "JVM, arrancá" |
| `-cp target/classes` | "Buscá las clases en esta carpeta" (`cp` = classpath) |
| `ar.edu.utn.ba.proyecto0.Main` | **Nombre completo de la clase** (package + clase). **NO** `Main.class`, **NO** ruta de archivo. |

Vas a ver tu salida normal:

```
Hola desde Países Local
Hola, soy Ever Lizarraga y este es mi Proyecto 0
...
```

### 2.4 Por qué esto no escala

Hicimos esto con **un solo archivo**. Cuando tu proyecto tenga 50 clases y use librerías externas (Lombok, Jackson, Spring), vas a tener que:

- Listar **todos los `.java`** en `javac`.
- Especificar **todos los `.jar` de las dependencias** en el `-cp`.
- Hacerlo en el orden correcto.

Inmanejable. Por eso existe Maven.

---

## 3. Forma 2: con Maven Wrapper (`mvnw`)

Maven Wrapper es el `mvnw` (Mac/Linux) o `mvnw.cmd` (Windows) que vive en la raíz de tu proyecto, junto con la carpeta `.mvn/`. **Lo tenés sí o sí porque IntelliJ lo generó.**

> **Recordá del `.gitignore`:** ese `maven-wrapper.jar` se versiona aposta para que cualquiera que clone el repo pueda usar el wrapper sin instalar Maven.

### 3.1 Comandos básicos

Desde la raíz del proyecto:

**Mac/Linux:**
```bash
./mvnw compile          # compila
./mvnw clean            # borra target/
./mvnw clean compile    # los dos seguidos
```

**Windows:**
```bash
mvnw.cmd compile
mvnw.cmd clean
mvnw.cmd clean compile
```

> A partir de acá uso `./mvnw` por brevedad. Si estás en Windows, reemplazá por `mvnw.cmd`.

### 3.2 Probemos: compilar

```bash
./mvnw clean compile
```

Vas a ver un montón de salida tipo:

```
[INFO] Scanning for projects...
[INFO] ----------< ar.edu.utn.ba.proyecto0:paises-local >----------
[INFO] Building paises-local 1.0-SNAPSHOT
[INFO] ...
[INFO] Compiling 1 source file to .../target/classes
[INFO] BUILD SUCCESS
```

Maven hizo lo mismo que `javac` pero:
- **Encontró solo** todos los archivos `.java` en `src/main/java/`.
- **Resolvió** las dependencias (si las hubiera).
- **Generó** `target/classes/` con los `.class`.

### 3.3 Correr la clase (Java directo después de Maven compilar)

Maven **solo compila** con `mvnw compile`. Para correr, podés:

**Opción A: usar `java` igual que antes**

```bash
java -cp target/classes ar.edu.utn.ba.proyecto0.Main
```

Funciona porque Maven dejó los `.class` exactamente donde `java` los espera.

**Opción B: usar el plugin `exec` de Maven** (más limpio)

Esto requiere agregar un plugin al `pom.xml`. Abrí `pom.xml` y agregá esto **adentro del tag `<project>`**, después del bloque `<properties>`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <version>3.5.0</version>
            <configuration>
                <mainClass>ar.edu.utn.ba.proyecto0.Main</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> **¿Qué es un plugin de Maven?** Maven es un "esqueleto" — cada cosa que hace (compilar, empaquetar, correr tests, deployar) la hace un plugin. Vos elegís cuáles instalar. `exec-maven-plugin` agrega el comando `exec:java`.

Después corré:

```bash
./mvnw compile exec:java
```

Y vas a ver tu salida:

```
[INFO] --- exec:3.5.0:java (default-cli) @ paises-local ---
Hola desde Países Local
Hola, soy Ever Lizarraga y este es mi Proyecto 0
...
[INFO] BUILD SUCCESS
```

> **Comparado con Java a mano:** `mvnw compile exec:java` es **un solo comando** que compila y corre. No tenés que listar archivos ni manejar classpath. Cuando agreguemos Lombok en la Etapa 3, todo esto va a seguir andando sin que vos toques nada.

---

## 4. Forma 3: empaquetar y correr un `.jar`

Esta es **la forma de deployar en producción**. Un `.jar` (Java ARchive) es un archivo comprimido que contiene tu código compilado + metadatos.

### 4.1 Empaquetar

```bash
./mvnw clean package
```

Maven va a:
1. Compilar.
2. Empaquetar en un `.jar`.
3. Dejarlo en `target/paises-local-1.0-SNAPSHOT.jar`.

> El nombre del jar viene del `artifactId` + `version` que pusiste en el `pom.xml`. Si cambiás la versión, cambia el nombre.

### 4.2 Correr el `.jar`

```bash
java -cp target/paises-local-1.0-SNAPSHOT.jar ar.edu.utn.ba.proyecto0.Main
```

> **Limitación importante:** así como está, el `.jar` no es "ejecutable" directamente. Si probás `java -jar target/paises-local-1.0-SNAPSHOT.jar`, te va a decir "no main manifest attribute". Para hacerlo ejecutable necesitás configurar el `maven-jar-plugin` con un Manifest. Eso lo vemos más adelante si querés. Por ahora con `-cp` te alcanza.

### 4.3 Por qué importa esto

En producción, un deploy típico es:
1. CI corre `./mvnw clean package`.
2. Te genera el `.jar`.
3. Lo subís a un servidor.
4. El servidor corre `java -jar tu-app.jar`.

Esto es exactamente lo que Spring Boot hace, solo que con un `.jar` "fat" (que incluye todas las dependencias adentro). Vas a ver el patrón cuando llegues al Bloque 1.

---

## 5. El ciclo de vida de Maven (lifecycle)

Cuando tipeás `./mvnw compile` o `./mvnw package`, no estás eligiendo un comando suelto — estás eligiendo un **paso** dentro de un ciclo. Maven los ejecuta **en orden** hasta el paso que pediste.

### Las 8 fases principales

| Fase | Qué hace | Comando |
|---|---|---|
| `validate` | Valida que el proyecto es correcto | `./mvnw validate` |
| `compile` | Compila `src/main/java/` | `./mvnw compile` |
| `test-compile` | Compila `src/test/java/` | (implícito) |
| `test` | Corre los tests unitarios | `./mvnw test` |
| `package` | Empaqueta en `.jar` o `.war` | `./mvnw package` |
| `verify` | Corre tests de integración | `./mvnw verify` |
| `install` | Instala el `.jar` en el repo local (`~/.m2/`) | `./mvnw install` |
| `deploy` | Sube al repo remoto | `./mvnw deploy` |

**Si pedís `./mvnw package`, Maven ejecuta automáticamente `validate → compile → test-compile → test → package`.** En orden, sin que vos pidas cada uno.

### El comodín `clean`

`clean` no es parte del ciclo principal. Borra la carpeta `target/`. Se usa antes de otros pasos para asegurar que arrancás de cero:

```bash
./mvnw clean package    # borra target/ y después empaqueta desde cero
```

> **Cuándo usar `clean`:** cuando algo raro pasa con compilaciones cacheadas, o antes de un deploy para evitar arrastrar basura. En desarrollo local del día a día no hace falta.

---

## 6. Cuándo usar cada forma

| Situación | Forma a usar |
|---|---|
| Desarrollo, probando rápido | IntelliJ (triángulo verde) |
| Quiero ver qué hace IntelliJ por debajo | `javac` + `java` a mano |
| Compilar y correr sin IDE, en cualquier máquina | `./mvnw compile exec:java` |
| Generar un artefacto para deploy | `./mvnw clean package` |
| Correr en un servidor de producción | `java -jar tu-app.jar` |
| CI/CD pipeline | `./mvnw clean verify` o `./mvnw clean package` |

> **El IDE es la "interfaz cómoda" para desarrollo. Maven es la "infraestructura real" del proyecto.** Tu repo tiene que poder compilarse y correr desde consola, sin IntelliJ, en cualquier máquina. Si IntelliJ se rompe, podés seguir trabajando con `./mvnw`.

---

## 🧪 Ejercicios para vos

Hacelos uno por uno desde la terminal:

### Ejercicio 1: Verificar el wrapper
```bash
./mvnw --version
```
Deberías ver la versión de Maven y de Java.

### Ejercicio 2: Compilar a mano y ver el resultado
```bash
./mvnw clean
ls target/        # ¿qué hay? (en Windows: dir target\)
./mvnw compile
ls target/        # ¿y ahora? 
```

### Ejercicio 3: Correr con Maven + exec
- Agregá el plugin `exec-maven-plugin` al `pom.xml` como mostré.
- Corré: `./mvnw compile exec:java`.
- ¿Ves tu salida?

### Ejercicio 4: Empaquetar
```bash
./mvnw clean package
ls target/
```
Tenés que ver un `paises-local-1.0-SNAPSHOT.jar`. Corrélo:
```bash
java -cp target/paises-local-1.0-SNAPSHOT.jar ar.edu.utn.ba.proyecto0.Main
```

### Ejercicio 5: Romper aposta
- En `Main.java`, sacale el `;` a una línea cualquiera.
- Corré `./mvnw compile`.
- Leé el error. Maven te dice **qué archivo, qué línea, qué fase** falló.
- Restaurá el `;` y verifí que vuelve a compilar.

---

## ✅ Checkpoint

1. ¿Qué dos pasos hace IntelliJ cuando apretás el triángulo verde?
2. ¿Qué diferencia hay entre `javac` y `java`?
3. ¿Por qué `java -cp target/classes ar.edu.utn.ba.proyecto0.Main` usa el nombre completo de la clase y no la ruta del archivo `.class`?
4. ¿Por qué Maven se usa en vez de hacer todo a mano con `javac`?
5. Si tipeás `./mvnw package`, ¿qué fases ejecuta Maven en el camino?
6. ¿Cuándo usarías `./mvnw clean`?

---

## 🔗 Lo que aprendiste y cómo conecta

| Aprendiste | Conecta con... |
|---|---|
| Compilar Java a mano (`javac`, `java`) | Entender qué hace cualquier IDE por debajo |
| Maven Wrapper (`./mvnw`) | Cualquier proyecto Java profesional lo usa, incluido el del profe |
| Ciclo de vida de Maven | Mismo lifecycle que el profe corre cuando hace "Run" en su proyecto Spring Boot |
| `package` → `.jar` | Spring Boot genera un `.jar` ejecutable así. Bloque 1 lo va a confirmar. |
| Plugins de Maven (`exec`, `jar`, etc.) | Spring Boot usa el `spring-boot-maven-plugin` para hacer la magia del `java -jar` |

---

**FIN DEL ANEXO**

Volvé al material principal de la Etapa 0 (`proyecto0-etapa0-crear-proyecto.md`) o avanzá a la Etapa 1 cuando quieras.
