# 🛠️ Proyecto 0 — Etapa 9: IO de archivos (leer países desde CSV)

> **Objetivo:** reemplazar el hardcodeo del catálogo por una carga desde un archivo CSV. Aprender la API moderna de IO de Java (NIO), `try-with-resources` aplicado, parseo de archivos línea por línea, y manejo de errores en IO.
>
> **Tiempo estimado:** 45-60 minutos.
>
> **Pre-requisito:** Etapa 8 completa (excepciones).

---

## 🎯 Conceptos que vas a tocar

- **NIO** (New IO) — la API moderna `java.nio.file.*`.
- **`Path`** y **`Files`** — los reemplazos modernos de `File`/`FileReader`.
- Leer un archivo en distintas formas: `readAllLines`, `lines` (stream), `readString`.
- **Try-with-resources** aplicado a `BufferedReader` y `Stream`.
- Parsear líneas CSV con `split`.
- Manejar `IOException` (checked exception).
- Cargar el catálogo desde el archivo + tests del flujo.

---

## 🧠 Mini-charla: IO en Java

Java tiene **dos APIs de IO**, históricas y modernas:

| API | Paquete | Estado |
|---|---|---|
| IO viejo | `java.io.*` (`File`, `FileReader`, `BufferedReader`) | Funciona pero verboso |
| NIO ("New IO") | `java.nio.file.*` (`Path`, `Files`, `Paths`) | **Lo idiomático hoy** |

Vamos a usar **NIO** porque:
- API mucho más ergonómica.
- Mejor integración con streams (`Files.lines()` devuelve un `Stream<String>`).
- Mejor manejo de paths multiplataforma (Windows vs Unix).

> No te confundas: "New IO" se llama "new" pero **existe desde Java 7** (2011). Lo "new" es relativo a `java.io.*` que es de Java 1.0.

---

## 📂 Paso 1: Crear el archivo CSV

Maven tiene una carpeta especial para archivos no-código: `src/main/resources`. Todo lo que pongas ahí queda disponible en runtime.

En IntelliJ:
1. Click derecho sobre `src/main/resources`.
2. **New → File**.
3. Nombre: `paises.csv`.

Tipeá adentro:

```csv
nombre,capital,region,poblacion
Argentina,Buenos Aires,Americas,45000000
Brasil,Brasilia,Americas,210000000
Chile,Santiago,Americas,19000000
España,Madrid,Europe,47000000
Francia,París,Europe,67000000
Japón,Tokio,Asia,125000000
Alemania,Berlín,Europe,83000000
Italia,Roma,Europe,60000000
México,Ciudad de México,Americas,128000000
Colombia,Bogotá,Americas,51000000
```

> **Formato CSV simple:** separadores por coma, cada línea es un país, primera línea son los headers. **No incluimos monedas e idiomas** para simplificar — los vas a agregar como ejercicio bonus al final.

### Por qué `resources` y no otra carpeta

Cuando Maven empaqueta el `.jar`, todo lo que esté en `src/main/resources/` **se incluye adentro del .jar**. Por eso es el lugar idiomático para archivos de configuración, plantillas, datos de seed, etc.

Tu CSV va a viajar dentro del jar y va a poder leerse incluso después del deploy.

---

## 📥 Paso 2: Las 3 formas de leer un archivo

### Forma A: Leer TODO de una (archivos chicos)

```java
String contenido = Files.readString(Paths.get("src/main/resources/paises.csv"));
System.out.println(contenido);
```

`readString` te devuelve **todo el archivo como un String único**. Útil para archivos chicos. Para CSVs grandes, **no escala** — cargás todo en memoria.

### Forma B: Leer como lista de líneas

```java
List<String> lineas = Files.readAllLines(Paths.get("src/main/resources/paises.csv"));
lineas.forEach(System.out::println);
```

`readAllLines` te devuelve una `List<String>` con cada línea. Útil para archivos chicos a medianos.

### Forma C: Leer como Stream (mejor para archivos grandes)

```java
try (Stream<String> lineas = Files.lines(Paths.get("src/main/resources/paises.csv"))) {
    lineas.forEach(System.out::println);
}
```

`Files.lines` te da un `Stream<String>` **perezoso** — lee línea por línea según las necesitás, sin cargar todo el archivo en memoria. **Es lo más eficiente** para archivos grandes.

> Importante: `Files.lines` **debe ir dentro de un `try-with-resources`** porque mantiene el archivo abierto. Si no lo cerrás, hay leak de recursos.

### `Path` vs `Paths.get(...)` vs `Path.of(...)`

Hay dos formas equivalentes de crear un `Path`:

```java
Paths.get("ruta/al/archivo")    // viejo, todavía funciona
Path.of("ruta/al/archivo")      // moderno, Java 11+
```

Ambos hacen lo mismo. Usá `Path.of(...)` en código nuevo.

---

## 🛡️ Paso 3: `IOException` es **checked**

Recordá de Etapa 8: hay excepciones que el compilador te obliga a manejar. `IOException` es una de ellas.

```java
public List<String> leerLineas(String path) throws IOException {
    return Files.readAllLines(Path.of(path));
}
```

**Notá el `throws IOException`** en la firma. **Es obligatorio.** Si no lo declarás, el código no compila.

Quien llame a este método tiene dos opciones:
- **Capturar** la excepción con `try/catch`.
- **Propagarla** declarándola en su propia firma con `throws`.

---

## 🏗️ Paso 4: Crear `CargadorDePaisesDesdeCSV`

Vamos a crear una clase nueva responsable de leer el CSV y construir los `Pais`. Separar responsabilidades.

Creá un nuevo package `ar.edu.utn.ba.proyecto0.io`.

Y adentro creá `CargadorDePaisesDesdeCSV.java`:

```java
package ar.edu.utn.ba.proyecto0.io;

import ar.edu.utn.ba.proyecto0.modelo.Pais;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CargadorDePaisesDesdeCSV {

    public List<Pais> cargarDesde(Path archivo) throws IOException {
        try (Stream<String> lineas = Files.lines(archivo)) {
            return lineas
                .skip(1)                              // saltar el header
                .map(this::parsearLinea)
                .toList();
        }
    }

    private Pais parsearLinea(String linea) {
        String[] campos = linea.split(",");
        return new Pais(
            campos[0],                                // nombre
            campos[1],                                // capital
            campos[2],                                // region
            Long.parseLong(campos[3]),                // poblacion
            Map.of(),                                 // monedas (vacío por ahora)
            Map.of()                                  // idiomas (vacío por ahora)
        );
    }
}
```

### Qué pasa acá

**1. `try-with-resources` con `Stream<String>`**
```java
try (Stream<String> lineas = Files.lines(archivo)) {
    ...
}
```
El `Stream` se cierra automáticamente al salir del bloque, cerrando el archivo subyacente.

**2. `.skip(1)` para el header**
La primera línea del CSV son los nombres de columnas. La salteamos con `skip(1)`.

**3. `.map(this::parsearLinea)`**
Cada línea String se transforma en un `Pais`. `this::parsearLinea` es un method reference al método de la misma clase.

**4. `String.split(",")`**
Divide el string en un array de strings usando la coma como separador. **Cuidado:** este split simple **no maneja comas dentro de valores escapados**. Para CSVs reales se usan librerías como **OpenCSV** o **Apache Commons CSV**. Para este ejercicio, alcanza.

**5. `Long.parseLong(campos[3])`**
Convierte el String "45000000" a un `long`. Si el string no es un número válido, **tira `NumberFormatException`** (unchecked).

---

## 🔧 Paso 5: Usar el cargador desde `CatalogoPaises`

Modificá `CatalogoPaises.java` para que tenga un constructor alternativo que cargue desde archivo:

```java
public class CatalogoPaises {

    private List<Pais> paises;

    // Constructor sin args — sigue cargando hardcodeado (como antes)
    public CatalogoPaises() {
        this.paises = new ArrayList<>();
        this.paises.add(new Pais("Argentina", "Buenos Aires", "Americas", 45000000L, Map.of(), Map.of()));
        // ... resto del hardcodeo
    }

    // Constructor nuevo — carga desde CSV
    public CatalogoPaises(Path archivoCsv) throws IOException {
        CargadorDePaisesDesdeCSV cargador = new CargadorDePaisesDesdeCSV();
        this.paises = new ArrayList<>(cargador.cargarDesde(archivoCsv));
    }

    // ... el resto de los métodos sin cambios
}
```

### Detalle: `new ArrayList<>(...)`

`cargador.cargarDesde(...)` devuelve una lista **inmutable** (porque `.toList()` lo es). Como `CatalogoPaises` espera poder modificar internamente, envolvemos en `new ArrayList<>(...)` para obtener una lista mutable.

---

## 🎬 Paso 6: Usar todo desde `Main`

```java
public static void main(String[] args) {
    try {
        Path archivo = Path.of("src/main/resources/paises.csv");
        CatalogoPaises catalogo = new CatalogoPaises(archivo);

        System.out.println("Cargué " + catalogo.cantidad() + " países desde el CSV:");
        catalogo.getTodos().forEach(p -> System.out.println("  - " + p.getNombre()));

        System.out.println("\n=== Países de Americas ===");
        catalogo.buscarPorRegion("Americas")
            .forEach(p -> System.out.println("  - " + p.getNombre()));

    } catch (IOException e) {
        System.err.println("Error leyendo el archivo: " + e.getMessage());
    }
}
```

### Salida esperada

```
Cargué 10 países desde el CSV:
  - Argentina
  - Brasil
  - Chile
  - España
  - Francia
  - Japón
  - Alemania
  - Italia
  - México
  - Colombia

=== Países de Americas ===
  - Argentina
  - Brasil
  - Chile
  - México
  - Colombia
```

---

## 🧪 Paso 7: Test del cargador

Creá `CargadorDePaisesDesdeCSVTest.java` en `src/test/java/.../io/`:

```java
package ar.edu.utn.ba.proyecto0.io;

import ar.edu.utn.ba.proyecto0.modelo.Pais;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CargadorDePaisesDesdeCSVTest {

    @Test
    void cargar_devuelveLaCantidadCorrectaDeLineas() throws IOException {
        // Arrange
        Path csv = crearCsvDePrueba();
        CargadorDePaisesDesdeCSV cargador = new CargadorDePaisesDesdeCSV();

        // Act
        List<Pais> paises = cargador.cargarDesde(csv);

        // Assert
        assertThat(paises).hasSize(2);
    }

    @Test
    void cargar_parseaLosCamposCorrectamente() throws IOException {
        // Arrange
        Path csv = crearCsvDePrueba();
        CargadorDePaisesDesdeCSV cargador = new CargadorDePaisesDesdeCSV();

        // Act
        List<Pais> paises = cargador.cargarDesde(csv);

        // Assert
        assertThat(paises)
            .extracting(Pais::getNombre)
            .containsExactly("Argentina", "Brasil");

        assertThat(paises.get(0).getPoblacion()).isEqualTo(45000000L);
    }

    @Test
    void cargar_archivoInexistente_lanzaIOException() {
        // Arrange
        Path inexistente = Path.of("no-existe.csv");
        CargadorDePaisesDesdeCSV cargador = new CargadorDePaisesDesdeCSV();

        // Act + Assert
        assertThatThrownBy(() -> cargador.cargarDesde(inexistente))
            .isInstanceOf(IOException.class);
    }

    // Helper: crea un CSV temporal para los tests
    private Path crearCsvDePrueba() throws IOException {
        Path tempFile = Files.createTempFile("paises-test", ".csv");
        List<String> lineas = List.of(
            "nombre,capital,region,poblacion",
            "Argentina,Buenos Aires,Americas,45000000",
            "Brasil,Brasilia,Americas,210000000"
        );
        Files.write(tempFile, lineas);
        return tempFile;
    }
}
```

### Cosas nuevas

**`Files.createTempFile(...)`** — crea un archivo temporal en el sistema (típicamente en `/tmp` o `C:\Users\...\AppData\Local\Temp`). El sistema operativo lo limpia automáticamente con el tiempo. Útil para tests.

**`Files.write(path, lineas)`** — escribe líneas a un archivo. Lo opuesto de `Files.readAllLines`.

**Por qué crear el CSV en el test en vez de leer el real:** los tests deben ser **independientes** del entorno. Si tu test depende de `src/main/resources/paises.csv`, alguien que cambie ese archivo rompe tus tests. Mejor: el test crea su propio CSV controlado y lo lee.

---

## 🧪 Ejercicios

### Ejercicio 1: Manejar líneas mal formadas
Modificá el cargador para que si una línea no tiene 4 campos, la saltee con un warning en vez de explotar:

```java
private Pais parsearLinea(String linea) {
    String[] campos = linea.split(",");
    if (campos.length < 4) {
        System.err.println("Línea inválida ignorada: " + linea);
        return null;
    }
    // ... parsear
}
```

Y filtrá los nulls del stream:
```java
.filter(Objects::nonNull)
.toList();
```

### Ejercicio 2: Extender el CSV con monedas
Cambiá el CSV a algo así (separando con `;` los campos secundarios para evitar conflicto con la coma):

```
nombre,capital,region,poblacion,moneda
Argentina,Buenos Aires,Americas,45000000,ARS:Argentine peso:$
...
```

Y parseá el campo de moneda:

```java
private Map<String, DetalleMoneda> parsearMoneda(String texto) {
    String[] partes = texto.split(":");
    return Map.of(partes[0], new DetalleMoneda(partes[1], partes[2]));
}
```

### Ejercicio 3: Excepción custom para errores de parseo
Creá `FormatoCsvInvalidoException extends RuntimeException` y lanzala cuando el split devuelva menos campos de los esperados, con un mensaje que incluya la línea problemática.

### Ejercicio 4: Cargar desde un archivo de classpath
Hasta ahora el path es relativo a la carpeta de ejecución. Mejor: cargar desde el classpath (lo que viaja dentro del jar):

```java
public List<Pais> cargarDesdeClasspath(String nombreRecurso) throws IOException {
    try (var input = getClass().getClassLoader().getResourceAsStream(nombreRecurso);
         var reader = new BufferedReader(new InputStreamReader(input))) {
        return reader.lines()
            .skip(1)
            .map(this::parsearLinea)
            .toList();
    }
}
```

Y se usa así: `cargador.cargarDesdeClasspath("paises.csv")` (sin rutas, solo el nombre del recurso).

### Ejercicio 5: Test parametrizado
Usando lo de Etapa 7, hacé un test parametrizado que verifique que cargar archivos con 1, 2, 3, y 5 líneas devuelve el conteo esperado.

---

## ✅ Criterios de "Etapa 9 completa"

- [ ] Tenés `paises.csv` en `src/main/resources/`.
- [ ] Tenés `CargadorDePaisesDesdeCSV` en un nuevo package `io`.
- [ ] El `CatalogoPaises` tiene un constructor alternativo que carga desde archivo.
- [ ] Podés correr `Main` y ver que los países vienen del CSV (no del hardcodeo).
- [ ] Tenés al menos 2 tests del cargador, uno positivo y uno de error.
- [ ] Usaste `try-with-resources` con `Files.lines`.
- [ ] Podés explicar por qué `IOException` es checked y `NumberFormatException` es unchecked.

---

## ✅ Checkpoint

1. ¿Por qué `Files.lines` debe ir en `try-with-resources`?
2. ¿Cuál es la diferencia entre `Files.readAllLines` y `Files.lines`?
3. ¿Por qué se ponen archivos como CSV en `src/main/resources/`?
4. ¿Qué ventaja tiene cargar desde classpath vs un path relativo?
5. ¿Cuándo crear un archivo temporal con `Files.createTempFile` vs usar un archivo "real"?
6. ¿Por qué los tests crean su propio CSV en vez de depender del archivo real?

---

## 🔗 Conexión con código del profe

Tu cargador desde CSV es **conceptualmente** lo mismo que el `BuscadorDePaises` del profe — ambos son fuentes de datos. La única diferencia:

- **Vos**: leés un archivo local.
- **El profe**: hace HTTP request a una API REST.

El patrón es idéntico:
1. Pedir los datos a la fuente externa.
2. Parsearlos (vos splitéas por coma, él usa Jackson para JSON).
3. Convertirlos en objetos `Pais`.
4. Devolverlos.

**Esa abstracción** ("una clase que sabe traer Países desde algún lado") es la base de los **repositorios** o **fuentes de datos** que vas a estudiar más adelante en DSI cuando veas patrones como Repository, DAO, etc.

---

## 🎉 Final del Proyecto 0

Llegaste al final. Sumá todo lo que tenés:

| Tema | Aplicado |
|---|---|
| Maven + IntelliJ | Proyecto profesional |
| Clases, encapsulamiento | Modelo `Pais` |
| Collections | `CatalogoPaises` con List, Map |
| Lombok | DTOs limpios |
| Optional | Búsquedas que pueden no tener resultado |
| Streams + Lambdas | Procesamiento declarativo |
| Maps anidados | Estructuras complejas |
| JUnit + AssertJ | Tests profesionales |
| Excepciones | Manejo de errores |
| IO de archivos | Cargar datos desde el sistema |

**Sos un programador Java competente.** Podés leer cualquier código moderno de Java sin sufrir. Y la base que armaste te va a soportar todo lo que venga después: Spring Boot, JPA, REST APIs, patrones de diseño.

---

## ▶️ Próximo paso

**Volvé al recorrido del código del profe — Bloque 1.** Ya no es un misterio. Cada línea va a tener significado porque vivenciaste lo que hace.

Cuando lo retomes, decime: **"volvamos al bloque 1"** y arrancamos.

¡Buen viaje! 🚀
