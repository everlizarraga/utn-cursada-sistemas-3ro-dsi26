# 🛠️ Proyecto 0 — Etapa 8: Manejo de excepciones

> **Objetivo:** aprender a manejar errores con `try/catch`, distinguir excepciones checked vs unchecked, crear excepciones personalizadas, y aplicar todo en el `CatalogoPaises` para validar entradas.
>
> **Tiempo estimado:** 45-60 minutos.
>
> **Pre-requisito:** Etapa 7 completa (tests andando).

---

## 🎯 Conceptos que vas a tocar

- Qué es una excepción y cómo se "propaga".
- **`try / catch / finally`** — la estructura clásica.
- **Checked vs unchecked exceptions** — la división que confunde a todo el mundo.
- **`throw`** vs **`throws`** — lanzar vs declarar.
- Crear tus propias excepciones extendiendo `RuntimeException` o `Exception`.
- **Try-with-resources** — la forma moderna de cerrar recursos.
- Cómo testear excepciones con AssertJ.
- Buenas prácticas: cuándo lanzar, cuándo capturar, cuándo no hacer nada.

---

## 🧠 Qué es una excepción

Una **excepción** es un objeto que Java crea cuando algo sale mal en runtime. Representa una "interrupción" del flujo normal.

```java
int[] numeros = {1, 2, 3};
int x = numeros[10];   // 💥 ArrayIndexOutOfBoundsException
```

Cuando se lanza una excepción:

1. **El flujo normal se interrumpe.** La línea siguiente no se ejecuta.
2. **La excepción "sube"** por la cadena de llamadas (call stack) buscando alguien que la capture.
3. **Si nadie la captura**, el programa termina con un stack trace.

### Anatomía de un stack trace

Cuando una excepción no se captura, Java imprime algo así:

```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 10 out of bounds for length 3
    at Main.main(Main.java:5)
```

Te dice:
- **El tipo** de excepción (`ArrayIndexOutOfBoundsException`).
- **El mensaje** ("Index 10 out of bounds for length 3").
- **El lugar exacto** donde se lanzó (archivo Main.java, línea 5).

> En código complejo, el stack trace puede tener decenas de líneas. **Leer el stack trace es una habilidad clave** — siempre miralo desde arriba (donde se originó la excepción) hacia abajo.

---

## 🛡️ Parte 1: `try / catch / finally`

La estructura base para capturar excepciones:

```java
try {
    // código que puede tirar excepción
    int x = Integer.parseInt("no soy un número");
} catch (NumberFormatException e) {
    // qué hacer si pasa eso
    System.out.println("Error al parsear: " + e.getMessage());
} finally {
    // se ejecuta siempre, haya o no excepción
    System.out.println("Cleanup");
}
```

### Las tres partes

| Bloque | Cuándo se ejecuta |
|---|---|
| `try` | Siempre — es el código "principal" |
| `catch` | **Solo si** se lanza una excepción del tipo declarado |
| `finally` | **Siempre** — pase lo que pase. Útil para cleanup (cerrar archivos, conexiones) |

### Múltiples catch

Podés capturar distintos tipos de excepción con bloques separados:

```java
try {
    String texto = leerArchivo("config.txt");
    int valor = Integer.parseInt(texto);
} catch (IOException e) {
    System.out.println("Error de archivo: " + e.getMessage());
} catch (NumberFormatException e) {
    System.out.println("El archivo no tiene un número válido: " + e.getMessage());
}
```

O en un solo catch con `|`:

```java
} catch (IOException | NumberFormatException e) {
    System.out.println("Error: " + e.getMessage());
}
```

### Capturar la madre de todas: `Exception`

Como todas las excepciones heredan de `Exception`, podés capturar todo con:

```java
} catch (Exception e) {
    System.out.println("Algo falló: " + e.getMessage());
}
```

**Anti-patrón:** capturar `Exception` genérica te oculta qué falló específicamente. **Solo usalo en el "último nivel"** (ej: en el `main` para no dejar que el programa muera con stack trace feo). En código interno, capturá lo específico.

---

## 🔀 Parte 2: Checked vs unchecked — la división que confunde

Java tiene **dos familias** de excepciones, y se comportan distinto.

### Jerarquía

```
                Throwable
                /        \
             Error      Exception   ← checked
                       /         \
              IOException      RuntimeException   ← unchecked
                              /              \
                NullPointerException    IllegalArgumentException
                                        ArithmeticException
                                        NumberFormatException
                                        ...
```

### Checked exceptions

Heredan de `Exception` pero NO de `RuntimeException`. Ejemplos típicos: `IOException`, `SQLException`, `InterruptedException`.

**Reglas:**
- **Si tu método las puede lanzar, TENÉS que declararlo** con `throws` o capturarlas con `try/catch`. **El compilador te obliga.**

```java
public String leerArchivo(String path) throws IOException {
    //                                  ^^^^^^^^^^^^^^^^^
    //                            "este método puede tirar IOException"
    return Files.readString(Paths.get(path));
}
```

### Unchecked exceptions (RuntimeException y sus hijas)

**No tenés que declararlas ni capturarlas.** El compilador no te obliga. Ejemplos: `NullPointerException`, `IllegalArgumentException`, `IndexOutOfBoundsException`.

```java
public int parsearEntero(String s) {
    return Integer.parseInt(s);   // puede tirar NumberFormatException
    // no necesitás "throws NumberFormatException" en la firma
}
```

### Cuándo usar cada uno

**Checked** = "errores **esperables** del entorno externo que el caller debería manejar".
Ejemplos: archivo no existe, conexión cae, red falla.

**Unchecked** = "errores de **lógica del programa** que en teoría no deberían pasar".
Ejemplos: dividir por 0, acceder a un null, índice fuera de rango.

### La controversia

La distinción suena lógica pero en la práctica **es muy criticada**. Muchas APIs modernas (Spring, Hibernate, JDK reciente) **lanzan unchecked incluso para errores externos** porque las checked obligan a poner `throws` en cascada que ensucia todo el código.

**Para tus propias excepciones, por defecto extendé `RuntimeException`** salvo que tengas una razón explícita para forzar al caller a manejarlas.

---

## 🚀 Parte 3: `throw` — lanzar excepciones

`throw` lanza una excepción manualmente. Útil para validaciones:

```java
public Pais buscarPorNombreObligatorio(String nombre) {
    if (nombre == null) {
        throw new IllegalArgumentException("El nombre no puede ser null");
    }
    if (nombre.isBlank()) {
        throw new IllegalArgumentException("El nombre no puede estar vacío");
    }
    return buscarPorNombre(nombre)
        .orElseThrow(() -> new RuntimeException("País no encontrado: " + nombre));
}
```

### Excepciones built-in útiles

| Excepción | Cuándo usarla |
|---|---|
| `IllegalArgumentException` | Argumento inválido (null, vacío, fuera de rango) |
| `IllegalStateException` | El objeto está en un estado inválido para la operación |
| `NullPointerException` | Algo es null cuando no debería |
| `UnsupportedOperationException` | Operación no soportada (ej: intentar modificar una lista inmutable) |
| `RuntimeException` | Genérica — usá una más específica si podés |

### `Objects.requireNonNull` — el atajo del JDK

En vez de:
```java
if (nombre == null) {
    throw new NullPointerException("nombre");
}
```

Usá:
```java
Objects.requireNonNull(nombre, "nombre no puede ser null");
```

Hace lo mismo en una línea. Muy idiomático en Java moderno.

---

## 🏗️ Parte 4: Crear excepciones personalizadas

Para representar errores específicos de **tu dominio**, creá tu propia clase:

```java
package ar.edu.utn.ba.proyecto0.catalogo;

public class PaisNoEncontradoException extends RuntimeException {
    public PaisNoEncontradoException(String nombre) {
        super("País no encontrado: " + nombre);
    }
}
```

Y la usás:

```java
public Pais buscarPorNombreObligatorio(String nombre) {
    return buscarPorNombre(nombre)
        .orElseThrow(() -> new PaisNoEncontradoException(nombre));
}
```

### Beneficios

1. **Especificidad.** El catch puede ser `catch (PaisNoEncontradoException e)`, distinguiéndolo de otros errores.
2. **Documentación.** El tipo en sí mismo dice qué pasó, no hay que leer el mensaje.
3. **Datos extra.** Podés agregar atributos:

```java
public class PaisNoEncontradoException extends RuntimeException {
    private final String nombreBuscado;

    public PaisNoEncontradoException(String nombreBuscado) {
        super("País no encontrado: " + nombreBuscado);
        this.nombreBuscado = nombreBuscado;
    }

    public String getNombreBuscado() {
        return nombreBuscado;
    }
}
```

Y quien capture la excepción puede acceder a `e.getNombreBuscado()`.

---

## 🎯 Parte 5: Aplicación al catálogo

Aplicá todo lo aprendido. Agregá estos métodos al `CatalogoPaises`:

```java
public Pais buscarPorNombreObligatorio(String nombre) {
    Objects.requireNonNull(nombre, "El nombre no puede ser null");
    if (nombre.isBlank()) {
        throw new IllegalArgumentException("El nombre no puede estar vacío");
    }
    return buscarPorNombre(nombre)
        .orElseThrow(() -> new PaisNoEncontradoException(nombre));
}
```

Y la excepción personalizada en el mismo package:

```java
// PaisNoEncontradoException.java
package ar.edu.utn.ba.proyecto0.catalogo;

public class PaisNoEncontradoException extends RuntimeException {
    private final String nombreBuscado;

    public PaisNoEncontradoException(String nombreBuscado) {
        super("País no encontrado: " + nombreBuscado);
        this.nombreBuscado = nombreBuscado;
    }

    public String getNombreBuscado() {
        return nombreBuscado;
    }
}
```

### Probalo en `Main`

```java
try {
    Pais argentina = catalogo.buscarPorNombreObligatorio("Argentina");
    System.out.println("OK: " + argentina);

    Pais atlantis = catalogo.buscarPorNombreObligatorio("Atlantis");
    System.out.println("OK: " + atlantis);
} catch (PaisNoEncontradoException e) {
    System.out.println("Error de búsqueda: " + e.getMessage());
    System.out.println("Buscábamos: " + e.getNombreBuscado());
} catch (IllegalArgumentException e) {
    System.out.println("Argumento inválido: " + e.getMessage());
}
```

**Salida esperada:**
```
OK: Pais(...Argentina...)
Error de búsqueda: País no encontrado: Atlantis
Buscábamos: Atlantis
```

---

## 🧪 Parte 6: Testear excepciones con AssertJ

Acordate de Etapa 7. Para verificar que un método lanza la excepción esperada:

```java
@Test
void buscarPorNombreObligatorio_paisInexistente_lanzaExcepcion() {
    assertThatThrownBy(() -> catalogo.buscarPorNombreObligatorio("Atlantis"))
        .isInstanceOf(PaisNoEncontradoException.class)
        .hasMessageContaining("Atlantis");
}

@Test
void buscarPorNombreObligatorio_null_lanzaIllegalArgument() {
    assertThatThrownBy(() -> catalogo.buscarPorNombreObligatorio(null))
        .isInstanceOf(NullPointerException.class);
}

@Test
void buscarPorNombreObligatorio_vacio_lanzaIllegalArgument() {
    assertThatThrownBy(() -> catalogo.buscarPorNombreObligatorio(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("vacío");
}
```

**`assertThatThrownBy(lambda)`** ejecuta la lambda y verifica que tire excepción. Si la lambda **NO** tira excepción, el test falla. Si tira una de tipo distinto al esperado, también falla.

Encadenable con:
- `.isInstanceOf(Clase.class)` — verifica el tipo.
- `.hasMessage("...")` — mensaje exacto.
- `.hasMessageContaining("...")` — el mensaje contiene esto.
- `.hasMessageStartingWith("...")` — empieza con esto.

---

## 🧹 Parte 7: Try-with-resources (preview para Etapa 9)

Cuando trabajás con archivos, conexiones, streams de IO, **tenés que cerrarlos** al terminar. Antes de Java 7 esto era un infierno con `finally`:

```java
// Estilo viejo:
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("archivo.txt"));
    String line = reader.readLine();
    // ...
} finally {
    if (reader != null) {
        try {
            reader.close();
        } catch (IOException e) { /* qué hago acá... */ }
    }
}
```

Desde Java 7 existe **try-with-resources**:

```java
// Estilo moderno:
try (BufferedReader reader = new BufferedReader(new FileReader("archivo.txt"))) {
    String line = reader.readLine();
    // ...
}
// El reader se cierra automáticamente al salir del try.
```

Cualquier recurso que implemente `AutoCloseable` puede ir en el `try (...)`. Java garantiza que se cierra al salir, haya o no excepción.

**Vas a usar esto en Etapa 9 cuando leamos archivos.**

---

## 🧪 Ejercicios

### Ejercicio 1: Validación en `Pais`
Hacé que el constructor `@AllArgsConstructor` de `Pais` no funcione directamente para validaciones. Creá un método estático `Pais.crear(...)` que valide los argumentos antes de construir:

```java
public static Pais crear(String nombre, String capital, String region, long poblacion, ...) {
    Objects.requireNonNull(nombre, "nombre");
    Objects.requireNonNull(capital, "capital");
    if (poblacion < 0) {
        throw new IllegalArgumentException("La población no puede ser negativa");
    }
    return new Pais(nombre, capital, region, poblacion, ...);
}
```

### Ejercicio 2: Excepción personalizada con datos adicionales
Creá una `RegionDesconocidaException` que reciba el nombre de la región buscada y la lista de regiones disponibles. Usala en `buscarPorRegion` cuando devuelva lista vacía.

### Ejercicio 3: Capturar y relanzar
Modificá `buscarPorNombreObligatorio` para que si captura cualquier `RuntimeException`, la envuelva en una `BusquedaFallidaException` (custom) y la relance:

```java
try {
    // ...
} catch (RuntimeException e) {
    throw new BusquedaFallidaException("Error buscando: " + nombre, e);
}
```

> El segundo argumento del constructor (`e`) es la **causa**. Cuando se imprime el stack trace, vas a ver "Caused by: ..." con el error original. Esto se llama **exception chaining**.

### Ejercicio 4: `finally` con efecto colateral
Hacé un método `loggearBusqueda(String nombre)` que use `try/catch/finally`:
- En el `try`, intenta buscar el país obligatorio.
- En el `catch`, imprime el error.
- En el `finally`, imprime "Búsqueda completada" siempre.

Probalo con un nombre que existe y otro que no. Confirmá que "Búsqueda completada" aparece en ambos casos.

### Ejercicio 5: Tests para todas las excepciones
Para cada validación que hiciste, escribí un test con `assertThatThrownBy`.

---

## ✅ Criterios de "Etapa 8 completa"

- [ ] Tenés `PaisNoEncontradoException` en el package `catalogo`.
- [ ] Tenés `buscarPorNombreObligatorio` que valida y lanza excepciones específicas.
- [ ] Probaste en `Main` que las excepciones se capturan correctamente.
- [ ] Escribiste al menos 2 tests con `assertThatThrownBy`.
- [ ] Podés explicar la diferencia entre checked y unchecked exceptions.
- [ ] Podés explicar para qué sirve `Objects.requireNonNull`.
- [ ] Sabés qué hace `try-with-resources` aunque todavía no lo usaste.

---

## ✅ Checkpoint

1. ¿Qué pasa si una excepción no se captura en ningún `catch`?
2. ¿Por qué `RuntimeException` no obliga a declararla con `throws`?
3. ¿Cuándo es mejor crear una excepción custom vs usar las built-in?
4. ¿Qué hace `finally` y cuándo se ejecuta?
5. ¿Cuál es la diferencia entre `throw` y `throws`?
6. ¿Por qué en general es mala práctica capturar `Exception` genérica?

---

## 🔗 Conexión con código del profe

El profe usa excepciones en su código de Spring/REST, aunque en general son **excepciones manejadas por Spring**:

- `RestClientException` cuando falla una llamada HTTP.
- `HttpClientErrorException` cuando el servidor responde con 4xx.
- `HttpServerErrorException` cuando responde con 5xx.

En su `BuscadorDePaises`, no captura explícitamente — deja que las excepciones suban. **Es buena práctica:** si no sabés qué hacer con una excepción, dejala subir. Solo capturá si **podés agregar valor** (recuperarte, loguear, transformar).

Vas a verlo en detalle cuando lleguemos al Bloque 4 del recorrido del código del profe.

---

## ▶️ Próximo paso

Cuando completes la etapa, podés:
- **"arranquemos etapa 9"** → IO de archivos, leer países desde CSV.
- **"volvamos al bloque 1"** → empezar el recorrido del código del profe (ya tenés todo).

Si te trabás, preguntá por chat.
