# 🛠️ Proyecto 0 — Etapa 7: Tests con JUnit 5 y AssertJ

> **Objetivo:** aprender a escribir tests unitarios profesionales para `CatalogoPaises`. Configurar JUnit 5 y AssertJ. Entender el patrón AAA (Arrange-Act-Assert). Después de esta etapa, estás listo para leer los tests del profe.
>
> **Tiempo estimado:** 45-60 minutos.
>
> **Pre-requisito:** Etapa 6 completa (Maps en el modelo).

---

## 🎯 Conceptos que vas a tocar

- Por qué se escriben tests (no es perder tiempo).
- Configurar **JUnit 5** y **AssertJ** vía Maven.
- El patrón **AAA**: Arrange / Act / Assert.
- Annotations clave: `@Test`, `@BeforeEach`, `@DisplayName`.
- Diferencia entre **`assertEquals`** (JUnit) y **`assertThat(...).isEqualTo(...)`** (AssertJ).
- Convenciones de nombres: `XxxTest` vs `XxxIT` (unit vs integration).
- Cómo correr tests desde IntelliJ y desde consola con Maven.
- Tests parametrizados (bonus).

---

## 🧠 Por qué tests (mini-charla)

Tests son lo que separa "código que funciona en mi máquina" de "código que funciona, punto". Resumen rápido:

1. **Detectás bugs antes de que lleguen a producción.** Si un test fallaba antes y sigue fallando, no podés mergear.

2. **Tests son documentación viva.** Quien lea `buscarPorRegion_devuelve3paises_paraAmericas` entiende qué hace el método sin mirar implementación.

3. **Podés refactorizar sin miedo.** Cambiás un loop por un stream, corrés los tests, ves verde. Sabés que no rompiste nada.

4. **Es lo que el profe usa.** El archivo `BuscadorDePaisesIT.java` que ya viste tiene 5 tests con AssertJ. Vas a poder leerlos después de esta etapa.

---

## 📦 Paso 1: Agregar dependencias al `pom.xml`

Abrí `pom.xml`. En el bloque `<dependencies>` (donde ya tenés Lombok), agregá estas dos:

```xml
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.11.3</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.26.3</version>
            <scope>test</scope>
        </dependency>
```

### Qué dice cada dependencia

**JUnit 5 (Jupiter):** es el framework de testing. Provee `@Test`, `@BeforeEach`, `assertEquals`, etc. Es el estándar de facto en Java moderno.

**AssertJ:** una librería de assertions con API fluida. **No reemplaza a JUnit** — coexisten. JUnit hace correr los tests; AssertJ provee una sintaxis mucho más expresiva para verificar resultados.

### El `<scope>test</scope>`

Es el equivalente al `provided` que usamos para Lombok, pero para tests. Significa:
- Solo está disponible al compilar/correr tests.
- **No se incluye en el `.jar` final** de producción.
- Tu app deployada no carga JUnit ni AssertJ en runtime. Coherente, ¿no?

### Plugin de Surefire (necesario para `mvn test`)

También en el `pom.xml`, dentro del bloque `<build><plugins>` (donde ya tenés `exec-maven-plugin`), agregá:

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.5.2</version>
            </plugin>
```

**Por qué este plugin:** es el que Maven usa para descubrir y correr los tests. Sin la versión actualizada, `./mvnw test` puede no encontrar tus tests JUnit 5.

### Recargar Maven

Click en la notificación **"Load Maven Changes"** (o `Ctrl + Shift + O` con el `pom.xml` abierto). Esperá a que descargue.

---

## 📂 Paso 2: Estructura de carpetas para tests

Maven y todos los proyectos Java siguen esta convención:

```
src/
├── main/
│   └── java/
│       └── ar/edu/utn/ba/proyecto0/
│           ├── catalogo/
│           ├── modelo/
│           └── Main.java
└── test/
    └── java/
        └── ar/edu/utn/ba/proyecto0/        ← MISMA estructura de packages
            ├── catalogo/                   ← MISMOS subpackages
            │   └── CatalogoPaisesTest.java
            └── modelo/
                └── PaisTest.java
```

**La regla:** los tests **espejan** la estructura del código de `main`. Si tenés `main/.../catalogo/CatalogoPaises`, su test va en `test/.../catalogo/CatalogoPaisesTest`.

### Verificá tu estructura

En IntelliJ, mirá el panel Project. Tenés que ver una carpeta `src/test/java` (creada por Maven cuando inicializó el proyecto). Si no tiene los packages, los vamos a crear ahora.

---

## ✍️ Paso 3: Tu primer test

En IntelliJ:
1. Click derecho sobre `src/test/java`.
2. **New → Package**.
3. Nombre: `ar.edu.utn.ba.proyecto0.catalogo`.

Después:
1. Click derecho sobre ese package recién creado.
2. **New → Java Class**.
3. Nombre: `CatalogoPaisesTest`.

> **Convención:** el archivo se llama como la clase que estás testeando, con sufijo `Test`. Esto le importa al plugin Surefire — descubre automáticamente archivos `*Test.java` y los corre.

Tipeá esto (a mano, como siempre):

```java
package ar.edu.utn.ba.proyecto0.catalogo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CatalogoPaisesTest {

    @Test
    void elCatalogoTieneSeisPaises() {
        // Arrange — preparar
        CatalogoPaises catalogo = new CatalogoPaises();

        // Act — ejecutar
        int cantidad = catalogo.cantidad();

        // Assert — verificar
        assertEquals(6, cantidad);
    }
}
```

### Qué significa cada parte

**1. `@Test`**
```java
@Test
void elCatalogoTieneSeisPaises() {
```
Le dice a JUnit "este método es un test, ejecutalo cuando corras los tests". Cualquier método **sin** `@Test` es ignorado (puede servir como helper interno).

**2. Método `void`**
Los tests son siempre `void`. No devuelven nada. Su único trabajo es **pasar** (no tirar excepción ni fallar un assert) o **fallar**.

**3. Acceso `void` sin `public`**
JUnit 5 permite tests con visibilidad **package-private** (sin `public`). Es la convención actual — más limpio. JUnit 4 requería `public`.

**4. El nombre del método**
Describe **qué se prueba**. Hay tres convenciones populares:
- `camelCase` descriptivo: `elCatalogoTieneSeisPaises` (lo que estamos usando).
- `snake_case`: `el_catalogo_tiene_seis_paises` (más legible para muchos).
- Estilo BDD: `should_returnSix_when_catalogIsCreated`.

**Java permite snake_case en nombres de tests** aunque no en código normal. Es una "licencia" porque la legibilidad importa más que la convención. Elegí la que prefieras y sé consistente.

**5. Estructura AAA**

| Sección | Qué hace |
|---|---|
| **Arrange** (preparar) | Setup del estado inicial. Crear objetos, datos de prueba. |
| **Act** (ejecutar) | La acción que estás testeando. Una sola operación, idealmente. |
| **Assert** (verificar) | Comparar el resultado contra lo esperado. |

Los comentarios `// Arrange`, `// Act`, `// Assert` son opcionales pero te ayudan a estructurar mientras aprendés. Después los podés sacar.

**6. `assertEquals(esperado, obtenido)`**

Importante el orden: **primero el valor esperado, después el obtenido**. Si fallaa, JUnit te dice "esperaba X, obtuve Y" — el orden importa para que el mensaje tenga sentido.

---

## ▶️ Paso 4: Correr tu primer test

### Desde IntelliJ

En el editor, al lado del método `@Test` deberías ver un **triángulo verde**. Click ahí → **Run 'elCatalogoTieneSeisPaises()'**.

Abajo se abre el panel **Run** con el resultado:

```
✅ CatalogoPaisesTest > elCatalogoTieneSeisPaises ()  passed
```

Si te tira **rojo** con "expected: 6 but was: X", revisá que en el catálogo cargues 6 países exactos.

### Desde consola

```bash
./mvnw test
```

Vas a ver una salida como:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running ar.edu.utn.ba.proyecto0.catalogo.CatalogoPaisesTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🎨 Paso 5: Conocer AssertJ (lo que usa el profe)

JUnit tiene assertions básicas (`assertEquals`, `assertTrue`, `assertNull`). Funcionan, pero **AssertJ tiene una API mucho más expresiva**.

Comparalas:

### JUnit nativo
```java
assertEquals(6, catalogo.cantidad());
assertEquals("Buenos Aires", argentina.getCapital());
assertTrue(americanos.size() > 0);
assertTrue(americanos.contains(argentina));
```

### AssertJ
```java
assertThat(catalogo.cantidad()).isEqualTo(6);
assertThat(argentina.getCapital()).isEqualTo("Buenos Aires");
assertThat(americanos).isNotEmpty();
assertThat(americanos).contains(argentina);
```

**Notá el patrón fluido:** todo arranca con `assertThat(loQueQuieroVerificar)`, y después encadenás métodos que describen qué espero.

### Por qué AssertJ es superior

1. **Lee como inglés.** `assertThat(lista).hasSize(3).contains(x)` se lee "assert that this lista has size 3 and contains x".

2. **Mensajes de error mucho mejores.** Si falla un `assertThat(lista).contains(x)`, te muestra qué elementos había, qué faltaba, etc.

3. **Métodos especializados por tipo.** Para colecciones tenés `hasSize`, `contains`, `isEmpty`, `allMatch`, `extracting`, etc. Para Optional tenés `isPresent`, `isEmpty`, `hasValue`. Para Map tenés `containsKey`, `containsEntry`, etc.

4. **Encadenamiento natural.** Podés validar muchas cosas sobre el mismo objeto sin repetir `assertThat`.

---

## ✍️ Paso 6: Tests más interesantes con AssertJ

Agregá estos tests a tu archivo:

```java
package ar.edu.utn.ba.proyecto0.catalogo;

import ar.edu.utn.ba.proyecto0.modelo.Pais;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CatalogoPaisesTest {

    private CatalogoPaises catalogo;

    @BeforeEach
    void setUp() {
        catalogo = new CatalogoPaises();
    }

    @Test
    void elCatalogoTieneSeisPaises() {
        assertThat(catalogo.cantidad()).isEqualTo(6);
    }

    @Test
    void buscarArgentinaDevuelveResultadoPresente() {
        Optional<Pais> resultado = catalogo.buscarPorNombre("Argentina");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCapital()).isEqualTo("Buenos Aires");
    }

    @Test
    void buscarPaisInexistenteDevuelveOptionalVacio() {
        Optional<Pais> resultado = catalogo.buscarPorNombre("Atlantis");

        assertThat(resultado).isEmpty();
    }

    @Test
    void paisesDeAmericasContienenLosTresEsperados() {
        List<Pais> americanos = catalogo.buscarPorRegion("Americas");

        assertThat(americanos)
            .hasSize(3)
            .extracting(Pais::getNombre)
            .containsExactlyInAnyOrder("Argentina", "Brasil", "Chile");
    }

    @Test
    void buscarPorRegionInexistenteDevuelveListaVacia() {
        List<Pais> resultados = catalogo.buscarPorRegion("Antarctica");

        assertThat(resultados).isEmpty();
    }

    @Test
    void elPaisMasPobladoEsBrasil() {
        Optional<Pais> masPoblado = catalogo.paisMasPoblado();

        assertThat(masPoblado)
            .isPresent()
            .map(Pais::getNombre)
            .hasValue("Brasil");
    }

    @Test
    void hayDosPaisesUsandoEuro() {
        List<Pais> pagaEuro = catalogo.buscarPorMoneda("EUR");

        assertThat(pagaEuro)
            .hasSize(2)
            .extracting(Pais::getNombre)
            .containsExactlyInAnyOrder("España", "Francia");
    }
}
```

### Qué cosas nuevas hay acá

**`@BeforeEach`**
```java
@BeforeEach
void setUp() {
    catalogo = new CatalogoPaises();
}
```

Se ejecuta **antes de cada test**. Sirve para no repetir el setup (crear el catálogo) en cada método. Asegura que cada test arranca con un catálogo **nuevo y limpio** — los tests no se "contagian" entre sí.

> **Existen también:** `@BeforeAll` (una sola vez antes de todos los tests), `@AfterEach`, `@AfterAll`. `@BeforeEach` es el que más vas a usar.

**`isPresent()` y `isEmpty()` sobre Optional**

AssertJ tiene assertions específicos para Optional:
```java
assertThat(resultado).isPresent();
assertThat(resultado).isEmpty();
assertThat(resultado).hasValue(esperado);
assertThat(resultado).contains(esperado);   // alias de hasValue
```

**`extracting()` sobre colecciones**

Esto es **lo más útil de AssertJ**. Te permite "transformar" cada elemento de una colección y validar el resultado:

```java
assertThat(americanos)
    .extracting(Pais::getNombre)            // ahora es una "lista" de Strings
    .containsExactlyInAnyOrder("Argentina", "Brasil", "Chile");
```

Es el equivalente declarativo de hacer:
```java
List<String> nombres = americanos.stream().map(Pais::getNombre).toList();
assertThat(nombres).contains(...);
```

**`containsExactlyInAnyOrder` vs `contains`**

| Método | Significa |
|---|---|
| `.contains(x, y)` | "Al menos están x e y, pueden haber más" |
| `.containsExactly(x, y)` | "Están exactamente x, y, en este orden" |
| `.containsExactlyInAnyOrder(x, y)` | "Están exactamente x, y, en cualquier orden" |
| `.containsOnly(x, y)` | "Solo están x e y (sin importar duplicados)" |

Elegí el más específico que tu test necesite.

---

## ▶️ Paso 7: Correr todos los tests

### Desde IntelliJ

- Click derecho sobre `CatalogoPaisesTest.java` → **Run 'CatalogoPaisesTest'**.
- O click derecho sobre el package `test/java` → **Run 'Tests in proyecto0'** para correr todos.

Vas a ver un dashboard con:

```
✅ elCatalogoTieneSeisPaises
✅ buscarArgentinaDevuelveResultadoPresente
✅ buscarPaisInexistenteDevuelveOptionalVacio
✅ paisesDeAmericasContienenLosTresEsperados
✅ buscarPorRegionInexistenteDevuelveListaVacia
✅ elPaisMasPobladoEsBrasil
✅ hayDosPaisesUsandoEuro

Tests passed: 7
```

### Desde consola

```bash
./mvnw test
```

---

## 🧨 Paso 8: Verificar que los tests fallan cuando deben

Esto es importante: **un test que nunca falla es un test inútil**. Vamos a verificar que tus tests detectan problemas.

Editá `CatalogoPaises` y rompé algo aposta:
- Cambiá el nombre "Argentina" por "Argentinaa" en el constructor.

Corré los tests. **Tres tests fallan:**

```
❌ buscarArgentinaDevuelveResultadoPresente  
   AssertionError: expected: Optional[...] to be present but was: Optional.empty

❌ paisesDeAmericasContienenLosTresEsperados
   ...

❌ ...
```

Bien. **Tus tests funcionan.** Detectan el cambio. Restauralo y volvé a verde.

> **Hay un concepto que se llama "test que se autoexamina":** un test debe fallar antes de pasar. Si lo escribís y siempre da verde, no estás seguro de que esté validando algo real. La práctica es: **escribir el test, verificar que falla, escribir el código para que pase**. Eso es **TDD** (Test-Driven Development), un tema que no exploramos acá pero que te recomiendo investigar.

---

## 🧪 Ejercicios

### Ejercicio 1: Test para `buscarPorCapital`
Escribí un test que verifique que `catalogo.buscarPorCapital("Madrid")` devuelve un Optional con España adentro.

### Ejercicio 2: Test para `nombresConcatenados`
Verificá que `nombresConcatenados()` devuelve el string esperado. Pista: `assertThat(str).isEqualTo("Argentina, Brasil, ...")`.

### Ejercicio 3: Test para `contarPorRegion`
Verificá que el map devuelto tiene las regiones correctas con los conteos correctos. Pista:

```java
assertThat(conteo)
    .containsEntry("Americas", 3L)
    .containsEntry("Europe", 2L)
    .containsEntry("Asia", 1L);
```

### Ejercicio 4: Test para `todosLosIdiomas`
Verificá que el set devuelto contiene los idiomas esperados.

### Ejercicio 5: `@DisplayName` (cosmético pero útil)

JUnit permite dar nombres más legibles a los tests con `@DisplayName`:

```java
@Test
@DisplayName("El catálogo debe contener 6 países al iniciarse")
void elCatalogoTieneSeisPaises() { ... }
```

Cuando corrés tests, IntelliJ y los reportes muestran el `@DisplayName` en lugar del nombre del método. Agregalo a todos tus tests.

### Ejercicio 6 (bonus): Test parametrizado
JUnit 5 permite correr el mismo test con distintos valores:

```java
@ParameterizedTest
@ValueSource(strings = {"Argentina", "Brasil", "Chile", "España"})
void todosEstosPaisesExistenEnElCatalogo(String nombre) {
    assertThat(catalogo.buscarPorNombre(nombre)).isPresent();
}
```

Esto corre **el mismo test 4 veces**, una por cada valor. Necesita el import:
```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
```

> **Si no anda:** puede que JUnit Jupiter no incluya el módulo de parametrized por default. Agregá esta dependencia al `pom.xml`:
> ```xml
> <dependency>
>     <groupId>org.junit.jupiter</groupId>
>     <artifactId>junit-jupiter-params</artifactId>
>     <version>5.11.3</version>
>     <scope>test</scope>
> </dependency>
> ```

---

## ✅ Criterios de "Etapa 7 completa"

- [ ] `pom.xml` tiene JUnit 5, AssertJ y surefire plugin configurados.
- [ ] Tenés `CatalogoPaisesTest.java` con al menos 5 tests.
- [ ] Usás `@BeforeEach` para evitar duplicar setup.
- [ ] Usás `assertThat` de AssertJ, no `assertEquals` de JUnit.
- [ ] Probaste qué pasa cuando rompés el código (Paso 8) — los tests fallan correctamente.
- [ ] Corriste los tests **desde IntelliJ y desde `./mvnw test`** al menos una vez.
- [ ] Resolviste al menos los ejercicios 1, 3 y 5.

---

## ✅ Checkpoint

1. ¿Cuál es la diferencia entre JUnit y AssertJ?
2. ¿Qué hace `@BeforeEach` y por qué se usa?
3. En `assertEquals(esperado, obtenido)`, ¿por qué importa el orden de los argumentos?
4. ¿Para qué sirve el sufijo `Test` en el nombre del archivo?
5. ¿Por qué los tests no deberían depender unos de otros?
6. ¿Qué diferencia hay entre `contains(x)` y `containsExactly(x)` en AssertJ?

---

## 🔗 Conexión con código del profe

**Ahora estás listo para leer `BuscadorDePaisesIT.java` del profe.** Mirá un test típico:

```java
@SpringBootTest
class BuscadorDePaisesIT {

    @Autowired
    private BuscadorDePaises buscadorDePaises;

    @Test
    void buscarTodos_devuelveListaNoVacia() {
        var lista = buscadorDePaises.buscarTodos();

        assertThat(lista).isNotEmpty();
    }

    @Test
    void buscarPorNombre_argentina_devuelvePaisCorrecto() {
        var resultado = buscadorDePaises.buscarPorNombre("argentina");

        assertThat(resultado)
            .isPresent()
            .map(Pais::getNombre)
            .map(NombrePais::getComun)
            .hasValue("Argentina");
    }
}
```

**Lo que cambia respecto a tus tests:**

- `@SpringBootTest` en vez de nada → arranca el contexto de Spring antes de correr el test.
- `@Autowired` → inyecta el `BuscadorDePaises` automáticamente (en vez de hacer `new`).
- Sufijo `IT` en vez de `Test` → indica "Integration Test" (porque hace llamadas HTTP reales).

**Lo que es IDÉNTICO a tus tests:**

- AssertJ (`assertThat`, `isPresent`, `hasValue`).
- Estructura AAA (Arrange-Act-Assert).
- Naming descriptivo.
- Una operación por test.

**Cuando llegues al Bloque 5 del recorrido del código del profe, lo vas a leer como tu propio código.** Eso era el objetivo del Proyecto 0.

---

## 🎉 Cerraste el corazón del Proyecto 0

Acabás de aprender:

| Etapa | Tema | Aplicado |
|---|---|---|
| 0 | Maven + IntelliJ | Estructura de proyecto, pom.xml |
| 1 | Clases con atributos | `Pais` con encapsulamiento |
| 2 | List y for | `CatalogoPaises` con List<Pais> |
| 3 | Lombok | `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor` |
| 4 | Optional | `buscarPorNombre` con `Optional<Pais>` |
| 5 | Streams y lambdas | `filter`, `map`, `findFirst`, `groupingBy` |
| 6 | Maps anidados | `Map<String, DetalleMoneda>` en `Pais` |
| 7 | Tests con JUnit + AssertJ | Tests profesionales del catálogo |

**Tenés el toolbox completo de Java moderno aplicado al dominio de países.** Es exactamente lo que el profe usa.

---

## ▶️ Próximo paso

Tres caminos:

1. **Volver al recorrido del código del profe — Bloque 1.** Ahora vas a leerlo con cero fricción. Te recomiendo este camino.

2. **Etapa 8 (Excepciones)** — manejar errores con `try/catch`, checked vs unchecked. Útil pero no urgente.

3. **Etapa 9 (IO de archivos)** — leer países desde un CSV. Útil para preparar la transición a algo real, pero no esencial.

Decime cómo seguimos. Mi sugerencia: **camino 1**, ya estás listo.
