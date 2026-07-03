# 🛠️ Proyecto 0 — Etapa 9 Anexo: Recursos, leaks y `try-with-resources`

> **Objetivo:** entender desde cero qué son los **recursos del sistema operativo**, qué es un **file handle**, qué es un **resource leak**, y por qué Java tiene **`try-with-resources`** y **`AutoCloseable`** para resolver todo eso.
>
> **Pre-requisito:** estar haciendo la Etapa 9 (IO de archivos) y haber visto el Paso 2 con las tres formas de leer archivos.
>
> **Lectura previa recomendada:** Etapa 8 (excepciones), específicamente lo de `try/catch/finally`.

---

## 🧭 Mapa del anexo

1. La metáfora del guardarropas — qué es un "recurso del SO".
2. File handles — los "tickets" del sistema operativo.
3. Por qué hay un límite (y qué pasa cuando se llena).
4. Qué es un "leak" y por qué te puede dormir tranquilo... hasta que no.
5. Eager vs Lazy reading — la clave de las 3 formas de leer archivos.
6. El estilo viejo (Java 6 y anteriores) — `try/finally` a mano.
7. El estilo moderno — `try-with-resources`.
8. `AutoCloseable` — la interfaz que lo permite todo.
9. Detalles que pueden pasarte: múltiples recursos, excepciones en `close`, suppressed exceptions.
10. Resumen mental.

---

## 🧠 Mini-charla previa: por qué este tema importa

En programación hay temas que **podés ignorar y funcionar bien** durante meses, hasta que un día explota algo y descubrís que estuviste haciendo las cosas mal sin saberlo.

**Los recursos del sistema operativo son ESE tema.**

En tu Proyecto 0, abrís 1 archivo, lo leés, programa termina. **No vas a notar nada raro**. Pero el día que tengas que escribir un servidor que procese 10000 requests por hora, cada uno abriendo y leyendo archivos, **el bug "invisible" se vuelve un crash diario**.

Por eso vale la pena entenderlo bien **ahora**, con un ejemplo chico, antes de que aparezca en algo grande.

---

## 🎟️ Parte 1: La metáfora del guardarropas

Tu programa Java corre **adentro** de la JVM. La JVM corre **adentro** del sistema operativo. Tu código NO toca el hardware directamente:

```
┌──────────────────────────────────┐
│   Tu código Java                 │
├──────────────────────────────────┤
│   JVM (Java Virtual Machine)     │
├──────────────────────────────────┤
│   Sistema Operativo (SO)         │
├──────────────────────────────────┤
│   Hardware (disco, RAM, red)     │
└──────────────────────────────────┘
```

Cuando querés leer un archivo del disco, **el SO es el intermediario**. Tu programa le dice "abrí esto" y el SO se encarga del trabajo sucio (mover la aguja del disco, leer bytes, etc.).

### La metáfora

Pensá el SO como **un guardarropas con tickets numerados**:

```
[Tu programa]                      [Sistema Operativo]
     │                                      │
     │── "abrime archivo.csv" ─────────────►│
     │                                      │
     │◄──────────── ticket #42 ─────────────│
     │                                      │
     │   *guarda el ticket*                 │
     │── "leeme con ticket #42" ───────────►│
     │◄────── "linea: Argentina,BA..." ─────│
     │                                      │
     │── "leeme con ticket #42" ───────────►│
     │◄────── "linea: Brasil,Brasilia..."───│
     │                                      │
     │── "ya terminé, devolvé #42" ────────►│
     │◄──────── "OK, liberado" ─────────────│
```

Ese "ticket" en jerga técnica se llama:
- **File handle** (Windows / lenguaje general)
- **File descriptor** o **fd** (Unix/Linux)

Es un número entero que identifica el recurso abierto. Internamente Java guarda ese número adentro del objeto `Stream` que te devuelve.

---

## 🎫 Parte 2: Qué son los file handles, en concreto

Cuando hacés:

```java
Stream<String> lineas = Files.lines(archivo);
```

Por debajo está pasando esto:

1. Java llama al SO con una syscall como `open()`.
2. El SO abre el archivo en el disco y reserva una entrada en su **tabla de archivos abiertos**.
3. El SO te devuelve un número (ej: `7`).
4. Java mete ese número adentro del objeto `Stream` que vos recibís.

Cada vez que iterás el Stream:

```java
lineas.forEach(System.out::println);
```

Java internamente llama al SO con `read(7, ...)`, "lee del archivo asociado al fd #7".

**Cuando vos hacés `lineas.close()`**, Java llama a `close(7)`, el SO libera la entrada en su tabla, y el fd #7 queda disponible para futuros usos.

---

## 🚧 Parte 3: Por qué hay un límite

El SO **no puede entregar tickets infinitos**. Cada fd ocupa memoria en el kernel (la tabla de archivos abiertos vive en RAM del SO). Para evitar abuso, hay límites por proceso:

| Sistema | Límite default (file descriptors por proceso) |
|---|---|
| Linux | 1024 |
| macOS | 256 (modificable a 10240) |
| Windows | ~2048 (más flexible) |

**Mil archivos abiertos suena mucho.** Pensá una app real:

- Servidor web atendiendo 100 clientes simultáneos.
- Cada cliente abre 3 archivos (log, config, datos).
- Hay conexiones a la BD también ocupando handles.
- Sockets de red consumen handles.

Llegás al límite **mucho más rápido de lo que pensás**.

### Qué pasa cuando llegás al límite

Tu programa intenta abrir un archivo más y el SO le dice:

```
java.io.IOException: Too many open files
```

**Tu programa no puede abrir más archivos.** En producción esto significa caída del servicio. Los logs se llenan de este error. Los usuarios reciben 500. **Reiniciar el proceso libera todos los handles** automáticamente (el SO los limpia al ver que el proceso murió), pero como el código tiene el leak, en cuanto la app empieza a procesar requests de vuelta, los handles se vuelven a acumular hasta llenar el límite. Reiniciar no arregla el bug, solo lo posterga.

---

## 💧 Parte 4: Qué es un "leak"

**Leak** en inglés es **"fuga"**. Como una canilla que gotea.

Un **resource leak** es cuando abrís un recurso y nunca lo cerrás. Va "goteando" handles que se acumulan.

### El leak en cámara lenta

```java
public void procesarMuchosArchivos() {
    for (int i = 0; i < 100000; i++) {
        Stream<String> lineas = Files.lines(Paths.get("archivo-" + i + ".txt"));
        lineas.forEach(System.out::println);
        // NUNCA cerramos `lineas`
    }
}
```

En cada iteración:

1. Java pide al SO abrir un archivo → SO da fd #N.
2. Procesa.
3. La variable `lineas` se sobrescribe en la siguiente iteración.
4. **El Stream anterior se pierde** (queda sin referencias).
5. **El fd queda asignado** al proceso porque el SO no sabe que ya no se usa.

A la iteración ~1020 (en Linux), el SO dice basta: `Too many open files`. Tu programa muere.

### Por qué en tu prueba no se notó

```java
public static void main(String[] args) {
    String contenido = Files.readString(path);
    System.out.println(contenido);
    // programa termina acá
}
```

**Cuando un proceso Java termina**, la JVM se apaga y el SO **libera todos los handles que tenía asignados** automáticamente. Es como cuando cierra el boliche al final de la noche: el guardarropas tira todos los tickets que quedaron sin recoger.

Por eso tus pruebas funcionaron — el proceso termina enseguida y el SO limpia todo. **No notaste nada raro.**

**El problema aparece cuando:**
- Tu programa **vive mucho tiempo** (servidor 24/7).
- Tu programa **procesa muchos archivos en loop** sin terminar.
- Tu programa **maneja muchas conexiones** que abren handles.

En el Proyecto 0 no lo vas a sentir. En tu TPA o en código laboral, sí. Por eso vale la pena hacerlo bien desde el principio.

---

## 🚀 Parte 5: Eager vs Lazy reading — la clave

Volvé a mirar las tres formas que viste en el Paso 2:

```java
// Forma A: TODO de una
String contenido = Files.readString(path);

// Forma B: lista de líneas
List<String> lineas = Files.readAllLines(path);

// Forma C: Stream lazy
Stream<String> lineas = Files.lines(path);
```

### Eager (formas A y B)

**"Eager" = "ansioso"**. Lee TODO el archivo de una sola vez, cierra el archivo, y te devuelve los datos completos en memoria.

```
Files.readString:
  1. Abre archivo → handle #7
  2. Lee TODOS los bytes
  3. Convierte a String
  4. Cierra handle #7  ← se cierra ACÁ, internamente
  5. Devuelve el String

[tu código recibe el String]
[el archivo ya está cerrado, no hay nada que hacer]
```

**Cuando vos recibís el `String` o el `List`, el archivo ya está cerrado.** Tu variable contiene **datos puros en memoria** — no hay handle del SO involucrado.

**Por eso no necesitan try-with-resources.** El cierre lo hace Java por vos antes de devolverte el resultado.

**Costo:** todo el archivo entra en memoria de tu programa. Para un CSV de 10 líneas, perfecto. Para un archivo de 50GB, **tu programa explota con `OutOfMemoryError`**.

### Lazy (forma C)

**"Lazy" = "perezoso"**. NO lee todo de una. Te devuelve un Stream que **leerá líneas a medida que vos las consumas**.

```
Files.lines:
  1. Abre archivo → handle #7
  2. Crea un Stream que sabe usar el handle #7
  3. Devuelve el Stream
     ← el handle SIGUE ABIERTO

[tu código tiene el Stream]
[el archivo está abierto, esperando que vos pidas líneas]

stream.forEach(...) → "dame línea 1" → lee usando handle #7
                  → "dame línea 2" → lee usando handle #7
                  → "dame línea 3" → lee usando handle #7
                  ...

[cuando vos termines y cierres el Stream]
[recién ahí se libera el handle]
```

**El archivo está abierto mientras el Stream "viva".** Si nunca cerrás el Stream, el archivo queda abierto para siempre.

**Ventaja:** podés procesar archivos enormes (gigabytes) sin cargarlos enteros en memoria. Solo una línea a la vez.

**Costo:** vos sos responsable de cerrar.

### Comparación

| Característica | `readString` / `readAllLines` | `Files.lines` |
|---|---|---|
| Lee todo de una | ✅ Sí | ❌ No, línea por línea |
| Carga todo en memoria | ✅ Sí | ❌ No |
| Cierra el archivo solo | ✅ Sí, internamente | ❌ No, vos tenés que cerrarlo |
| Para archivos chicos | ✅ Ideal | ✅ También funciona |
| Para archivos grandes (gigas) | ❌ Explota con OutOfMemory | ✅ Maneja bien |
| Requiere try-with-resources | ❌ No | ✅ Sí |

---

## 🛠️ Parte 6: El estilo viejo — `try/finally` manual

Antes de Java 7 (año 2011), el cierre se hacía a mano:

```java
Stream<String> lineas = null;
try {
    lineas = Files.lines(archivo);
    lineas.forEach(System.out::println);
} finally {
    if (lineas != null) {
        try {
            lineas.close();
        } catch (IOException e) {
            // qué hago acá?
        }
    }
}
```

**Por qué `finally`:** acordate de Etapa 8 — el bloque `finally` se ejecuta **siempre**, pase lo que pase. Eso garantiza que `close()` se llame aunque el código adentro tire excepción.

**Problemas del estilo viejo:**

1. **Verboso:** muchas líneas para algo simple.
2. **Fácil de olvidar:** si te olvidás del `finally`, leak.
3. **El `if (lineas != null)`:** si la apertura falla (ej: archivo no existe), `lineas` es null y `null.close()` explotaría.
4. **El `close()` puede tirar excepción**: y dentro de `finally` no podés relanzarla limpiamente (te machacaría la excepción original).
5. **Con varios recursos**: anidás try/finally adentro de try/finally. Pesadilla.

---

## ✨ Parte 7: El estilo moderno — `try-with-resources`

En Java 7 agregaron **sintaxis especial** para resolver todo esto:

```java
try (Stream<String> lineas = Files.lines(archivo)) {
    lineas.forEach(System.out::println);
}
// ← al salir del bloque, Java llama lineas.close() AUTOMÁTICAMENTE
```

### Anatomía

```
try (DECLARACIÓN_DE_RECURSO) {
    // código que usa el recurso
}
```

- El paréntesis después del `try` se llama **"resource declaration"**.
- Adentro declarás recursos (uno o varios, separados por `;`).
- Cualquier recurso ahí se cierra **automáticamente** al salir del bloque.
- **Funciona aunque haya excepciones**. Si el código adentro explota, el close igual se llama antes de que la excepción suba.

### Comparación lado a lado

```java
// VIEJO (Java 6):
Stream<String> lineas = null;
try {
    lineas = Files.lines(archivo);
    lineas.forEach(System.out::println);
} finally {
    if (lineas != null) {
        try {
            lineas.close();
        } catch (IOException e) {
            // ...
        }
    }
}

// MODERNO (Java 7+):
try (Stream<String> lineas = Files.lines(archivo)) {
    lineas.forEach(System.out::println);
}
```

**Mucho más limpio. La responsabilidad de cerrar pasa del programador al compilador.**

### Múltiples recursos

Podés abrir varios recursos en el mismo `try-with-resources`, separados con `;`:

```java
try (
    Stream<String> entrada = Files.lines(archivoEntrada);
    BufferedWriter salida = Files.newBufferedWriter(archivoSalida)
) {
    entrada.forEach(linea -> {
        try {
            salida.write(linea.toUpperCase());
            salida.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
}
// Ambos recursos se cierran automáticamente.
// IMPORTANTE: se cierran en ORDEN INVERSO al de apertura.
// Primero `salida`, después `entrada`.
```

**Por qué orden inverso:** si un recurso depende de otro, primero se cierra el dependiente. Es un detalle pero bueno saberlo.

---

## 🔌 Parte 8: `AutoCloseable` — la interfaz mágica

`try-with-resources` no es magia. Funciona porque los objetos que pongas ahí adentro **implementan una interfaz llamada `AutoCloseable`**:

```java
public interface AutoCloseable {
    void close() throws Exception;
}
```

**Es una interfaz con un solo método: `close()`.** Cualquier objeto que la implemente se puede usar dentro de `try-with-resources`.

### Qué pasa por debajo

Cuando escribís:

```java
try (Stream<String> lineas = Files.lines(archivo)) {
    // ...
}
```

El compilador lo expande a algo como:

```java
Stream<String> lineas = Files.lines(archivo);
try {
    // ...
} finally {
    if (lineas != null) {
        lineas.close();   // ← AutoCloseable.close()
    }
}
```

**Es el `try/finally` viejo, pero escrito automáticamente por el compilador.** No hay magia, hay **azúcar sintáctico**.

### Qué clases implementan AutoCloseable

Casi todo lo que abre recursos:

| Clase | Para qué |
|---|---|
| `Stream<T>` (Files.lines) | Iterar líneas |
| `BufferedReader` / `BufferedWriter` | I/O bufferizado |
| `FileInputStream` / `FileOutputStream` | I/O binario |
| `InputStreamReader` / `OutputStreamWriter` | I/O de caracteres |
| `Connection` (JDBC) | Conexión a BD |
| `PreparedStatement` (JDBC) | Query SQL |
| `Socket` | Conexión TCP |
| `ZipFile` | Archivos ZIP |
| `Scanner` | Lectura formateada |

> **Tip:** IntelliJ te marca con warning amarillo cuando declarás algo que es AutoCloseable sin try-with-resources. Hacele caso.

### Crear tus propias clases AutoCloseable

Podés implementar `AutoCloseable` en tus propias clases si manejan recursos:

```java
public class MiRecurso implements AutoCloseable {
    public MiRecurso() {
        System.out.println("Abriendo recurso");
    }

    public void usar() {
        System.out.println("Usando recurso");
    }

    @Override
    public void close() {
        System.out.println("Cerrando recurso");
    }
}

// Uso:
try (MiRecurso r = new MiRecurso()) {
    r.usar();
}
// Salida:
// Abriendo recurso
// Usando recurso
// Cerrando recurso
```

No vas a necesitarlo pronto, pero está bueno saber que existe.

---

## 🔍 Parte 9: Detalles que se te pueden cruzar

### ¿Qué pasa si hay excepción adentro del try?

```java
try (Stream<String> lineas = Files.lines(archivo)) {
    throw new RuntimeException("algo se rompió");
}
```

**El `close()` se llama de todas formas.** Después la excepción sube.

### ¿Y si el `close()` también tira excepción?

Acá hay un detalle elegante. Si:
- El try tira `ExcepcionPrincipal`.
- Después el close tira `ExcepcionDelClose`.

Java **NO sobrescribe** la principal con la del close. La principal sigue subiendo, y la del close queda como **"suppressed exception"** — pegada a la principal, accesible si la querés ver:

```java
try {
    // ...
} catch (Exception e) {
    e.printStackTrace();
    for (Throwable suppressed : e.getSuppressed()) {
        System.err.println("Suppressed: " + suppressed);
    }
}
```

Esto evita que el error original se pierda. En `try/finally` manual, el `close()` que explote podía **eclipsar** la excepción real y te volvías loco buscando el bug.

### ¿Hay penalización de performance?

**No.** El compilador genera bytecode prácticamente idéntico al `try/finally` manual. Es solo azúcar sintáctica.

### ¿Puedo declarar un recurso afuera y usarlo en try-with-resources?

Desde Java 9 sí, si el recurso es `final` o "efectivamente final":

```java
Stream<String> lineas = Files.lines(archivo);
try (lineas) {           // ← no es declaración, solo uso
    lineas.forEach(System.out::println);
}
```

Útil cuando el recurso lo crea otra parte del código.

### ¿El lenguaje me obliga a cerrar?

**No.** El compilador puede avisarte con warning, pero no es un error. Es tu responsabilidad:

```java
Stream<String> lineas = Files.lines(archivo);
lineas.forEach(System.out::println);
// ← compila y corre, pero hay LEAK.
```

Herramientas como IntelliJ y SonarQube detectan esto y te avisan, pero el lenguaje no lo bloquea.

---

## 🧠 Parte 10: Resumen mental

| Concepto | Resumen |
|---|---|
| **Recurso del SO** | Algo que el SO te presta (archivo, socket, conexión BD). |
| **File handle / fd** | El "ticket" numérico que el SO te da cuando abrís un archivo. |
| **Resource leak** | Recurso abierto que nunca se cierra. Acumula handles, eventualmente rompe. |
| **Por qué `readString` no requiere cerrar** | Lee todo de una vez, cierra internamente, devuelve datos. |
| **Por qué `Files.lines` SÍ requiere cerrar** | Es lazy. El archivo queda abierto mientras el Stream existe. |
| **`try-with-resources`** | Sintaxis Java 7+ que cierra automáticamente al salir del bloque. |
| **`AutoCloseable`** | Interfaz con un solo método `close()`. Habilita try-with-resources. |
| **¿Es magia?** | No. Azúcar sintáctico para `try/finally`. |
| **¿Cuándo lo uso?** | Siempre que abras un AutoCloseable. Sin excepciones. |
| **¿Qué pasa si me olvido?** | Tu programa anda hasta que abre demasiados recursos y muere. |

### La regla de oro

> **Si una clase implementa `AutoCloseable`, abrila dentro de `try-with-resources`.** Sin pensarlo. Es la opción segura por default.

---

## ✅ Checkpoint

Si podés contestar mentalmente estas, ya dominás los recursos:

1. ¿Por qué tu programa Java no puede abrir archivos infinitos en un servidor?
2. ¿Qué diferencia hay entre lectura eager y lazy?
3. ¿Por qué `Files.readString` y `Files.lines` requieren manejo diferente del cierre?
4. ¿Qué hace exactamente `try-with-resources` por debajo?
5. ¿Por qué los recursos se cierran en orden inverso al de apertura?
6. Si una excepción ocurre adentro del try, ¿se ejecuta el close?
7. ¿Qué pasa si el close también tira excepción?
8. ¿Por qué el lenguaje no te obliga a cerrar?

---

## 🔗 Aplicado al Paso 4 de Etapa 9

Cuando vuelvas al Paso 4 de Etapa 9 y veas este código:

```java
public List<Pais> cargarDesde(Path archivo) throws IOException {
    try (Stream<String> lineas = Files.lines(archivo)) {
        return lineas
            .skip(1)
            .map(this::parsearLinea)
            .toList();
    }
}
```

Ahora lo leés con modelo mental completo:

- "Abro un Stream **lazy** sobre el archivo."
- "Adentro del **try-with-resources**, lo proceso."
- "Al salir del bloque, el Stream se cierra **automáticamente**."
- "El SO recibe la devolución del handle."
- "Si una excepción ocurre durante el procesamiento, el close **igual se llama**. **Cero leaks.**"

**Es código completo, sin agujeros, sin riesgo.** Eso es lo que la versión moderna de Java te regala.

---

**Fin del anexo.**

Volvé al Paso 4 con confianza. Si te queda alguna duda específica, traela al chat.
