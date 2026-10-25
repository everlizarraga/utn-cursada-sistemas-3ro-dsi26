# 🛠️ Proyecto 0 — Etapa 0: Crear el proyecto desde cero

> **Objetivo:** terminar esta etapa con un proyecto Maven funcionando en IntelliJ que imprime "Hola desde Países Local" cuando lo corrés.
>
> **Tiempo estimado:** 20-40 minutos. La mayoría es entender lo que estás haciendo, no escribir código.
>
> **Regla de oro:** todo lo que aparezca en bloques de código, **tipealo vos mismo**. No copies y pegues. Los dedos necesitan tocar el código para que el cerebro registre.

---

## 🎯 Conceptos que vas a tocar

- Qué es Maven y por qué se usa
- Qué es un `pom.xml` y qué hay adentro
- Cómo se estructura un proyecto Java estándar (`src/main/java`, etc.)
- Cómo crear y correr un programa Java mínimo en IntelliJ
- El método `main` (qué es y por qué tiene esa firma rara)

---

## ✅ Pre-requisitos

Antes de arrancar, chequeá:

### 1. Java 21 instalado

Abrí una terminal (Símbolo del sistema en Windows, Terminal en Mac/Linux) y tipeá:

```bash
java -version
```

Tenés que ver algo como `openjdk version "21..."` o `java version "21..."`.

> **Si no tenés Java 21:** descargá un JDK 21 de https://adoptium.net/ (Eclipse Temurin). Es free y funciona en cualquier sistema operativo. Instalá, reiniciá la terminal, y volvé a chequear.

### 2. IntelliJ IDEA Community Edition

Lo bajás de https://www.jetbrains.com/idea/download/ (versión **Community**, no Ultimate — aunque siendo estudiante UTN podés pedir Ultimate gratis después).

### 3. Conexión a internet la primera vez

Maven necesita descargar dependencias la primera vez. Después funciona offline.

---

## 📂 Paso 1: Crear el proyecto en IntelliJ

### 1.1 Abrir el wizard

- Abrí IntelliJ.
- Si es la primera vez: click en **"New Project"**.
- Si ya tenés proyectos abiertos: **File → New → Project**.

### 1.2 Configurar el proyecto

En el wizard que aparece, completá:

| Campo | Valor | Por qué |
|---|---|---|
| **Name** | `paises-local` | Nombre de la carpeta del proyecto |
| **Location** | Donde vos quieras (ej: `~/Documents/dsi/`) | Carpeta donde vivirá el proyecto |
| **Language** | Java | Obvio |
| **Build system** | **Maven** | Lo que estamos aprendiendo |
| **JDK** | 21 (el que instalaste) | Versión de Java |
| **Add sample code** | ✅ destildado | Queremos crear nuestro `Main.java` a mano |
| **GroupId** | `ar.edu.utn.ba.proyecto0` | Tu "organización" (dominio invertido) |
| **ArtifactId** | `paises-local` | Nombre técnico del proyecto |

> **¿GroupId vs ArtifactId?**
> - **GroupId**: identifica a quién pertenece el proyecto (la organización). Es el package raíz. Convención: dominio invertido.
> - **ArtifactId**: identifica el proyecto en sí dentro de esa organización. Convención: minúsculas con guiones.
> - Juntos forman las **coordenadas únicas** del proyecto en el mundo Maven.

### 1.3 Click en **Create**

IntelliJ va a crear el proyecto y descargar algunas cosas. Esperá a que termine de indexar (ves la barra de progreso abajo).

---

## 🗂️ Paso 2: Explorar lo que se generó

En el panel izquierdo (Project) deberías ver algo así:

```
paises-local/
├── .idea/                    ← Configuración local de IntelliJ
├── src/
│   ├── main/
│   │   ├── java/             ← Acá van TUS archivos .java
│   │   └── resources/         ← Acá van archivos de config (yaml, properties)
│   └── test/
│       ├── java/             ← Acá van los tests (más adelante)
│       └── resources/         
├── target/                   ← Acá va el código compilado (no lo tocás vos)
└── pom.xml                   ← La "receta" de Maven
```

### Qué es cada cosa

| Item | Para qué |
|---|---|
| `.idea/` | Configuración local del IDE. **No se sube a Git** (va al `.gitignore`). |
| `src/main/java/` | Tu código fuente Java. Estructura por packages adentro. |
| `src/main/resources/` | Archivos no-Java: configuración, imágenes, etc. |
| `src/test/java/` | Código de tests. Misma estructura que `src/main/java`. |
| `target/` | Lo que Maven genera al compilar (`.class`, jars). **No se sube a Git**. Se regenera. |
| `pom.xml` | **El archivo más importante.** Define todo: dependencias, versión de Java, plugins, metadata. |

> **¿Por qué esta estructura es tan complicada para algo tan chico?** Maven la impone porque cuando el proyecto crezca, esta separación entre `main`/`test` y `java`/`resources` te va a salvar de un caos. Es convention over configuration: aceptás la estructura estándar y a cambio Maven sabe automáticamente dónde buscar todo.

---

## 📜 Paso 3: Leer el `pom.xml`

Hacé doble click en `pom.xml` y miralo. Va a ser algo así (puede variar un poco según la versión de IntelliJ):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>ar.edu.utn.ba.proyecto0</groupId>
    <artifactId>paises-local</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

</project>
```

### Línea por línea, qué dice esto

| Línea/bloque | Qué dice |
|---|---|
| `<modelVersion>4.0.0</modelVersion>` | Versión del modelo POM. Siempre 4.0.0 en proyectos modernos. |
| `<groupId>...</groupId>` | El GroupId que pusiste antes. |
| `<artifactId>...</artifactId>` | El ArtifactId que pusiste antes. |
| `<version>1.0-SNAPSHOT</version>` | Versión del proyecto. `SNAPSHOT` = en desarrollo. |
| `<maven.compiler.source>21</maven.compiler.source>` | "Compilá usando Java 21." |
| `<maven.compiler.target>21</maven.compiler.target>` | "Generá bytecode compatible con Java 21." |
| `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>` | Los archivos están en UTF-8. |

> **Comparación con `package.json` de Node:** mismo concepto, sintaxis distinta. El `pom.xml` define el proyecto y sus dependencias; `package.json` también. Maven es a Java lo que npm es a Node.

### Lo que NO ves todavía

No hay `<dependencies>` porque no agregamos ninguna librería externa. Cuando llegue el momento de Lombok (Etapa 3), vas a agregar tu primer bloque `<dependencies>`. Por ahora, vacío está bien.

---

## ✍️ Paso 4: Crear `Main.java`

### 4.1 Crear el package

- Click derecho sobre `src/main/java` (en el panel Project).
- **New → Package**.
- Nombre: `ar.edu.utn.ba.proyecto0`

> **¿Por qué el mismo nombre que el groupId?** Convención. El package raíz suele coincidir con el groupId. No es obligatorio pero es lo estándar.

Vas a ver que aparece una jerarquía de carpetas anidadas: `ar/edu/utn/ba/proyecto0/`. Esa es la estructura física que coincide con el package declarado.

### 4.2 Crear la clase `Main`

- Click derecho sobre el package `ar.edu.utn.ba.proyecto0`.
- **New → Java Class**.
- Nombre: `Main`
- Tipo: Class

Se va a abrir el archivo `Main.java` con algo así:

```java
package ar.edu.utn.ba.proyecto0;

public class Main {
}
```

### 4.3 Agregar el método `main`

**Tipealo vos** (no copies y pegues):

```java
package ar.edu.utn.ba.proyecto0;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hola desde Países Local");
    }
}
```

---

## 🔍 Paso 5: Entender el método `main`

La firma `public static void main(String[] args)` es **especial** en Java. Es la puerta de entrada del programa. Desarmémosla:

```
public  static  void  main(String[] args)
   │       │      │     │        │
   │       │      │     │        └─ Parámetros (argumentos de línea de comando)
   │       │      │     └─ Nombre del método (TIENE que llamarse "main")
   │       │      └─ No devuelve nada
   │       └─ No depende de una instancia de Main, se llama sobre la clase
   └─ Cualquiera puede llamarlo (la JVM, en este caso)
```

**Por qué cada modificador tiene que estar ahí:**

| Modificador | Por qué es obligatorio |
|---|---|
| `public` | La JVM (que está afuera de tu clase) tiene que poder verlo. |
| `static` | La JVM lo llama **sin crear una instancia** de tu clase. Si fuera no-static, primero habría que hacer `new Main()`. |
| `void` | La JVM no espera que tu programa devuelva un valor por esta vía. |
| `main` | Convención fija. Si lo llamás `mein`, la JVM no lo encuentra. |
| `String[] args` | Permite recibir argumentos desde la línea de comandos (ej: `java Main arg1 arg2`). Por ahora no los usás, pero el parámetro tiene que estar. |

> **Comparación con JS:** en JS no hay `main`. Cuando corrés `node app.js`, todo el código suelto del archivo se ejecuta. En Java, la JVM busca **un método llamado `main` con esa firma exacta** y eso es lo que ejecuta primero.

### Qué hace `System.out.println(...)`

- `System` → clase del paquete `java.lang` que da acceso al sistema.
- `out` → atributo estático de `System`, es la "salida estándar" (la consola).
- `println(x)` → método que imprime `x` y agrega un salto de línea.

Equivalente a `console.log(x)` en JS.

---

## ▶️ Paso 6: Correr el programa

### Opción A: con el botón verde

En el editor, al lado de la línea `public static void main...` deberías ver un **triángulo verde** ▶️. Click ahí → **Run 'Main.main()'**.

### Opción B: con teclado

- Windows/Linux: `Shift + F10`
- Mac: `Ctrl + R`

### Qué deberías ver

Abajo, en el panel **Run**:

```
Hola desde Países Local

Process finished with exit code 0
```

`exit code 0` = "todo salió bien". Si ves otra cosa, hay algo mal — preguntá por chat.

---

## 🧪 Paso 7: Mini-ejercicios

Hacé estos cambios uno por uno, corré después de cada uno, y observá qué pasa.

### Ejercicio 1: Cambiar el mensaje

Cambialo a algo personal: `"Hola, soy [tu nombre] y este es mi Proyecto 0"`.

### Ejercicio 2: Múltiples líneas

Agregá más líneas:

```java
System.out.println("Línea 1");
System.out.println("Línea 2");
System.out.println("Línea 3");
```

### Ejercicio 3: Concatenar texto y números

```java
int paisesCargados = 0;
System.out.println("Tengo " + paisesCargados + " países cargados.");
```

> **Nota:** en Java se concatena con `+`. **No hay template literals como en JS** (`` `${x}` ``). Existe `String.format()` para casos más sofisticados, pero por ahora `+` te alcanza.

### Ejercicio 4: Operaciones matemáticas

```java
int a = 10;
int b = 3;
System.out.println("a + b = " + (a + b));
System.out.println("a / b = " + (a / b));    // Notá que da 3, no 3.33. División entera.
System.out.println("a % b = " + (a % b));    // Resto: 1
```

> **Atención al `(a + b)` con paréntesis.** Sin ellos, Java empezaría a concatenar de izquierda a derecha y el resultado sería distinto. Probá `"a + b = " + a + b` (sin paréntesis) y vas a ver el bug clásico.

### Ejercicio 5 (bonus): Una variable que cambia

```java
int contador = 0;
contador = contador + 1;
contador++;
System.out.println("Contador: " + contador);   // ¿Qué imprime?
```

---

## ✅ Criterios de "Etapa 0 completa"

Marcá mentalmente cada uno:

- [ ] Tenés un proyecto Maven en IntelliJ llamado `paises-local`.
- [ ] El `pom.xml` declara Java 21 como source y target.
- [ ] El package `ar.edu.utn.ba.proyecto0` existe y tiene la estructura física en carpetas.
- [ ] `Main.java` tiene el método `main` con la firma correcta.
- [ ] Corrés el programa y ves tu mensaje en la consola.
- [ ] Probaste al menos los ejercicios 1, 3 y 4.
- [ ] Podés explicar (a vos mismo, en voz alta) qué es `public static void main(String[] args)` y por qué cada modificador está ahí.

---

## 🆘 Problemas comunes

| Síntoma | Probable causa | Solución |
|---|---|---|
| "Cannot resolve symbol 'System'" | El package no se declaró bien o IntelliJ no indexó | File → Invalidate Caches → Restart |
| El triángulo verde no aparece | El método no se llama exactamente `main` o falta `String[] args` | Revisá la firma carácter por carácter |
| "java: error: invalid source release: 21" | El JDK configurado no es 21 | File → Project Structure → Project SDK → seleccionar JDK 21 |
| "package x does not exist" | Estructura de carpetas no coincide con declaración `package` | Renombrá las carpetas para que coincidan |
| Maven está descargando para siempre | Primera vez puede tardar 5-10 min descargando metadata | Esperá. Si pasa más, reiniciá IntelliJ. |

Si te trabás con algo que no está acá, preguntá por chat con el error exacto y captura si hace falta.

---

## 🔗 Lo que aprendiste y cómo conecta

| Aprendiste | Conecta con... |
|---|---|
| Estructura Maven (`src/main/java`, `pom.xml`) | Bloque 1 del recorrido del código del profe |
| Maven coordinates (groupId, artifactId, version) | Mismo concepto en el `pom.xml` del profe |
| Convención de packages (dominio invertido) | El profe usa `ar.edu.utn.ba.ddsi.countries` — misma idea |
| Método `main` y arranque de un programa Java | Bloque 1 va a explicar cómo Spring "envuelve" este `main` con magia |
| `System.out.println` y operadores básicos | Vas a usar esto para debug durante toda la cursada |

---

## ▶️ Próximo paso

Cuando estés listo, decime **"arranquemos etapa 1"** y vamos a crear tu primera clase con atributos: `Pais.java`.

Si algo de esta etapa no te quedó claro, **preguntá por chat antes** de avanzar.
