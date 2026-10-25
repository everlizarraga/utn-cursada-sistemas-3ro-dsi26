# c01-py00-hola-mundo

Proyecto mínimo en **Java puro** (sin Spring) para afinar la sintaxis y hacer pruebas. Además, este README es la **guía de referencia** para crear y correr cualquier proyecto del repo.

## Versiones

| Herramienta | Versión | De dónde sale |
|---|---|---|
| Java (JDK) | **21** (21.0.12) | instalado en la Mac (Homebrew, `homebrew-21` en IntelliJ) |
| Maven | **3.9.16** | lo baja el wrapper (`mvnw`, versión 3.3.4) según `.mvn/wrapper/maven-wrapper.properties` |
| Spring Boot | **4.0.7** | solo en los proyectos con Spring (`parent` del `pom.xml`) |
| IDE | IntelliJ IDEA | — |

Convención de nombres del repo (detalle en el README de la raíz): carpeta = `artifactId` = `cNN-pyNN-nombre`; paquete `io.github.everlizarraga.cNNpyNNnombre`.

---

## 1. Crear un proyecto desde IntelliJ

Con el repo abierto en IntelliJ (la carpeta raíz):

1. **File → New → Module…**
   No uses *New → Project*: abre otra ventana aparte, fuera del repo.
2. A la izquierda, elegí el generador:

   | Generador | Para qué | Trae wrapper (`mvnw`) |
   |---|---|---|
   | **Java** | Java puro, sin Spring | ❌ no (ver §3) |
   | **Spring Boot** | proyecto con Spring (usa Spring Initializr) | ✅ sí |

3. Completá los campos:

   | Campo | Valor | Ejemplo |
   |---|---|---|
   | Name | `cNN-pyNN-nombre` | `c01-py00-hola-mundo` |
   | Location | la carpeta `ejercitacion` de la clase | `…/clases/clase-01-intro-arquitectura/ejercitacion` |
   | Build system | **Maven** | |
   | JDK | **21** | `homebrew-21` |
   | Parent | **`<None>`** para un proyecto independiente | |
   | Add sample code | opcional: genera un `Main.java` | |
   | GroupId *(Advanced Settings)* | `io.github.everlizarraga` | |
   | ArtifactId *(Advanced Settings)* | **igual que Name** | `c01-py00-hola-mundo` |

   Debajo de *Location*, IntelliJ muestra dónde va a quedar: `…/ejercitacion/c01-py00-hola-mundo`.

4. **Create.** IntelliJ arma la carpeta con `pom.xml` y `src/`, y la registra en el panel de Maven.

### Módulo dentro de un proyecto multimódulo

Para sumar un módulo a un proyecto que ya es multimódulo (por ejemplo, otra API en `c02-py02-apis-sin-key`):
- **Location:** la carpeta **del proyecto multimódulo**, no `ejercitacion`.
- **Parent:** ese proyecto (`c02-py02-apis-sin-key`).
- **Name / ArtifactId:** con el prefijo del proyecto (`c02-py02-nombre`).

IntelliJ agrega solo la línea `<module>` en el `pom.xml` padre. El módulo **no lleva wrapper propio**: usa el del padre.

---

## 2. Ajustes después de crearlo

### El paquete

IntelliJ deja el `Main.java` en `io.github.everlizarraga` (o en `org.example`), sin el subpaquete del proyecto. Para seguir la convención:

1. Clic derecho en `src/main/java` → **New → Package** → `io.github.everlizarraga.c01py00holamundo`.
2. Arrastrá `Main.java` adentro. IntelliJ actualiza la línea `package` solo.
3. Borrá el paquete viejo si quedó vacío.

```
src/main/java/io/github/everlizarraga/
└── c01py00holamundo/
    └── Main.java      → package io.github.everlizarraga.c01py00holamundo;
```

### Lo que no hace falta tocar

- **La versión** (`1.0-SNAPSHOT` con el generador Java, `0.0.1-SNAPSHOT` con el de Spring Boot): no afecta en nada.
- **El `.gitignore`:** el de la raíz del repo ya ignora `target/`.
- **Los comentarios `//TIP`** del código de ejemplo: son de IntelliJ, se pueden borrar.

### Si moviste archivos por fuera de IntelliJ

Si el editor los muestra en rojo o en el lugar viejo: clic derecho en la carpeta del proyecto → **Reload from Disk**.

---

## 3. Agregar el wrapper (para poder usar la consola)

Un proyecto creado con el generador **Java** solo corre con el ▶ de IntelliJ o con `mvn` (el Maven instalado en tu máquina). Para usar `./mvnw` como en el resto del repo, hay que agregarle el wrapper.

### Qué copiar

De cualquier proyecto que ya lo tenga (por ejemplo `c01-py01-paises-java`), estos 4 elementos, **tal cual, sin modificarlos**:

| Copiar | Qué es |
|---|---|
| `mvnw` | el script del wrapper para Mac y Linux |
| `mvnw.cmd` | el mismo script, para Windows |
| `.mvn/` (carpeta, con `wrapper/maven-wrapper.properties` adentro) | dice qué versión de Maven bajar (3.9.16) |
| `.gitattributes` | fija los finales de línea de los dos scripts (ver abajo) |

`.mvn` y `.gitattributes` son **ocultos** (empiezan con punto). En el Finder se ven con **`Cmd + Shift + .`**. En la terminal, con `ls -a`.

Si IntelliJ ya creó una carpeta `.mvn/` vacía, reemplazala por la copiada.

Desde la terminal, parado en la carpeta `ejercitacion` de la clase 1, la copia sería:

```bash
cp c01-py01-paises-java/mvnw c01-py01-paises-java/mvnw.cmd c01-py01-paises-java/.gitattributes c01-py00-hola-mundo/
cp -r c01-py01-paises-java/.mvn c01-py00-hola-mundo/
```

### Darle permiso de ejecución a `mvnw`

En Mac y Linux, un script solo se puede correr con `./` si está marcado como ejecutable. Parado en la carpeta del proyecto:

```bash
chmod +x mvnw                       # lo marca ejecutable en tu disco
git add .
git update-index --chmod=+x mvnw    # lo marca ejecutable en git (para quien clone el repo)
```

El `git update-index` va **después** del `git add`: solo funciona sobre archivos que git ya conoce.

**✅ Deberías ver**, con `git ls-files -s mvnw`, que la línea empieza con `100755` (ejecutable) y no con `100644`.

### Para qué sirve cada pieza

- **El wrapper** (`./mvnw algo`) hace lo mismo que `mvn algo`, pero baja y usa la versión de Maven que dice el proyecto. Así compila igual en cualquier máquina, aunque no tenga Maven instalado.
- **`.gitattributes`** existe porque Mac/Linux y Windows marcan el fin de renglón distinto (LF y CRLF), y un script con los finales del otro sistema no corre. Sus dos reglas fijan los finales de cada script, sin importar cómo tenga configurado git cada persona:

  ```
  /mvnw text eol=lf       ← mvnw: siempre finales de Mac/Linux
  *.cmd text eol=crlf     ← mvnw.cmd: siempre finales de Windows
  ```

---

## 4. Correr por consola

Siempre **parado en la carpeta del proyecto** (donde están el `pom.xml` y el `mvnw`). En un multimódulo, en la carpeta del proyecto padre.

Desde la raíz del repo, por ejemplo:

```bash
cd clases/clase-01-intro-arquitectura/ejercitacion/c01-py00-hola-mundo
```

### Proyecto Java puro (sin Spring), como este

**Dos pasos: Maven compila, Java ejecuta.**

```bash
./mvnw compile
java -cp target/classes io.github.everlizarraga.c01py00holamundo.Main
```

- `./mvnw compile` deja las clases compiladas en `target/classes`.
- `java -cp target/classes <clase>` corre el `main`. `-cp` (*classpath*) le dice a Java dónde están las clases; después va el nombre completo de la clase (paquete + nombre).

**✅ Deberías ver:**

```
Hello and welcome!i = 1
i = 2
i = 3
i = 4
i = 5
```

(El primer renglón sale pegado porque el ejemplo de IntelliJ usa `printf` sin salto de línea.)

**Alternativa en un solo comando**, con el plugin `exec` de Maven (lo baja solo la primera vez):

```bash
./mvnw compile exec:java -Dexec.mainClass=io.github.everlizarraga.c01py00holamundo.Main
```

La salida queda mezclada con las líneas `[INFO]` de Maven.

`./mvnw spring-boot:run` **no funciona** en un proyecto sin Spring: no tiene ese plugin.

### Proyecto con Spring Boot

**Un solo comando: compila y levanta.**

```bash
./mvnw spring-boot:run
```

- **Si tiene el starter web** (`spring-boot-starter-web` o `-webmvc`), arranca Tomcat y **queda escuchando**, en el puerto que diga `server.port` del `application.yaml` (si no dice nada, el **8080**). La terminal queda tomada; se frena con **`Ctrl + C`**.
- **Si no tiene el starter web** (solo `spring-boot-starter`, como `c01-py01-paises-java`), corre el `main` y **termina solo**.

### Proyecto multimódulo

Desde la carpeta del proyecto padre, se nombra el módulo con `-pl`:

```bash
./mvnw -pl c02-py02-jsonph spring-boot:run
./mvnw compile                         # compila todos los módulos
./mvnw -pl c02-py02-pokeapi test       # tests de un solo módulo
```

### Comandos comunes a todos

| Comando | Qué hace |
|---|---|
| `./mvnw -v` | muestra la versión de Maven y de Java |
| `./mvnw compile` | compila |
| `./mvnw test` | compila y corre los tests |
| `./mvnw test -Dtest=NombreDeLaClaseTest` | corre una sola clase de test |
| `./mvnw package` | compila, corre los tests y arma el `.jar` en `target/` |
| `java -jar target/<artifactId>-<version>.jar` | corre el `.jar` sin Maven (en proyectos Spring Boot) |
| `./mvnw clean` | borra `target/` |
| `./mvnw clean package` | borra y arma todo de cero |

**En Windows** cambia solo la forma de llamar al wrapper; lo que sigue es igual:

| Mac / Linux / Git Bash | PowerShell | `cmd` |
|---|---|---|
| `./mvnw compile` | `.\mvnw compile` | `mvnw compile` |

---

## 5. Problemas conocidos en IntelliJ

### Corre bien en la consola y falla con el ▶ (rutas relativas)

**Síntoma:** por consola anda, pero con el ▶ falla al leer un archivo. Pasó con `c01-py01-paises-java`, en la etapa 9:

```
Error leyendo el archivo: src/main/resources/paises.csv
```

**Causa:** el programa resuelve las rutas relativas (`src/main/resources/...`) desde su **directorio de trabajo**, la carpeta desde donde arrancó.

```
 ./mvnw  → arranca parado en la carpeta del proyecto → c01-py01-paises-java/src/... ✓
 ▶       → arranca parado en la raíz del repo        → utn-cursada-.../src/...      ✗
```

Se ve en el log, al final de la línea `Starting ... started by everlizarraga in <carpeta>`.

**Arreglo:** que IntelliJ arranque desde la carpeta del módulo.

1. **Run → Edit Configurations…** y elegí la configuración del proyecto (por ejemplo `C01Py01PaisesJavaApplication`).
2. **Modify options** (o **⌥ M**) → sección **Operating System** → **Working directory**. Aparece el campo.
3. En **Working directory** escribí `$MODULE_WORKING_DIR$`.
4. **Apply** → **OK**.

**✅ Deberías ver** que la línea `Starting ...` del log termina en la carpeta del proyecto.

`$MODULE_WORKING_DIR$` es una **macro** de IntelliJ (una variable que reemplaza por un valor real al ejecutar): significa "la carpeta del módulo que estás corriendo". Equivale a hacer `cd` a la carpeta del proyecto antes de lanzar Java.

**Para que valga en todas las configuraciones nuevas:** en la misma ventana, abajo a la izquierda, **Edit configuration templates…** → elegí **Spring Boot** (y también **Application**, la que usa IntelliJ para correr un `main` de Java puro) → mismos pasos 2 y 3. Las configuraciones que ya existían se arreglan a mano, una por una.

La configuración queda guardada en `.idea/`, que no se sube al repo. Quien clone el repo tiene que hacer este ajuste en su IntelliJ, o correr con `./mvnw`.

### "Lombok requires enabled annotation processing"

**Qué es:** Lombok genera código (getters, constructores, etc.) **al compilar**, con un *procesador de anotaciones*. IntelliJ compila con su propio compilador y, si esa opción está apagada, Lombok no genera nada y aparecen errores de métodos que "no existen".

**Arreglo:** tocar **Enable annotation processing** en el aviso. Queda activado en:

```
Settings → Build, Execution, Deployment → Compiler → Annotation Processors
  → ☑ Enable annotation processing
```

**En la consola no hace falta:** Maven ya lo resuelve con el `annotationProcessorPaths` de los `pom.xml`.

El aviso aparece al abrir el repo con un `.idea/` nuevo, porque esa configuración se guarda ahí.

---

**Más detalle** sobre Maven, el wrapper, las fases y los errores comunes: `playbook-maven-desde-consola.md`.
