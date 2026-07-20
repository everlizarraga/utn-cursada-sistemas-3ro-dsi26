# 📘 APUNTE MAESTRO — Preclase 02 · Parte 4
## Testing con JUnit 5

**Unidad:** `preclase02` — material previo obligatorio de la clase 2
**Viene de:** Parte 3 (Colecciones y Streams). Ya sabés leer y escribir el código que vamos a testear.
**Peso:** 🔴 Se usa todo el año. El TPA se entrega con tests.

---

## 1. Por qué esto importa — el bug del repositorio de la cátedra 🔴

Antes de la teoría, mirá esto. Es el código real del repo de "Vuelos y Aeropuertos", el que te dieron como solución del ejercicio.

El enunciado pide, en el requerimiento 5: *"la cantidad de vuelos totales que realizó un pasajero"*. El repo lo resuelve así, y está perfecto:

```java
public class Pasajero extends Persona {
    private List<Vuelo> vuelos;

    public Integer cantVuelosTotales() {
        return this.vuelos.size();       // impecable
    }
}
```

Ahora mirá cómo se le agregan pasajeros a un vuelo:

```java
public class Vuelo {
    private List<Pasajero> pasajeros;

    public void agregarPasajeros(Pasajero ... pasajeros) {
        Collections.addAll(this.pasajeros, pasajeros);
        // El vuelo se guarda al pasajero. ✅
        // El pasajero NO se guarda el vuelo. ❌
    }
}
```

**La lista `vuelos` del pasajero nunca se llena.** `cantVuelosTotales()` devuelve **0**. Siempre. Y `cantVecesQueVisitaste(ciudad)` también.

**Dos de los diez requerimientos del enunciado no funcionan.** ¿Por qué nadie se dio cuenta?

**Porque no hay ningún `PasajeroTest`.** El repo tiene tres tests: `CiudadTest`, `VueloTest` y `ViajeTest`. `Pasajero` no tiene ninguno.

> 🔴 **Esa es toda la tesis del testing, y no hace falta que te la explique nadie más:**
> **lo que no testeás, no sabés si funciona.** Y en un proyecto que crece, "no sabés si funciona" y "está roto" terminan siendo lo mismo.

Y hay un detalle que lo confirma: el mismo repo **sí** hace la bidireccionalidad en los otros cuatro lugares (`Ciudad`↔`Pais`, `Aeropuerto`↔`Ciudad`, `Vuelo`↔`Aeropuerto`, `Vuelo`↔`Tripulacion`). No es que el autor no supiera. Es que se le escapó **uno solo** — el único que ningún test tocaba.

---

## 2. Qué es un test unitario 🟡

Un **test unitario** es código que prueba **una unidad** de tu código (típicamente, un método) de forma aislada, y falla ruidosamente si el resultado no es el esperado.

**No es un `System.out.println` que mirás con los ojos.** Es una afirmación verificable que corre sola, mil veces, sin que vos estés mirando.

**Los tests viven en `src/test/java`**, replicando el mismo árbol de paquetes que el código (ver Parte 1). Si tu clase es `domain/viajes/Vuelo.java`, su test es `src/test/java/domain/viajes/VueloTest.java`.

---

## 3. Tu primer test 🔴

Partimos de esta clase:

```java
package domain.entities;

import lombok.Getter;
import lombok.Setter;

@Getter                       // Lombok genera getNombre(), getApellido()
@Setter                       // Lombok genera setNombre(...), setApellido(...)
public class Persona {
    private String nombre;
    private String apellido;
    private Direccion direccion;

    public String obtenerNombreCompleto() {
        return nombre + " " + apellido;
    }
}
```

Y este es su test:

```java
package domain.entities;

import org.junit.jupiter.api.Assertions;      // ← JUnit 5 (Jupiter). Ojo con este import.
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersonaTest {

    @Test                                     // ① esto le dice a JUnit: "este método es un test"
    @DisplayName("Test obtener nombre completo de persona")   // ② nombre legible en el reporte
    public void obtenerNombreCompleto() {

        // ── ARRANGE: preparo el escenario ──
        Persona persona = new Persona();
        persona.setNombre("Ada");
        persona.setApellido("Lovelace");

        // ── ACT + ASSERT: ejecuto y verifico ──
        Assertions.assertEquals("Ada Lovelace", persona.obtenerNombreCompleto());
        //                       ↑ lo ESPERADO   ↑ lo REAL
    }
}

// ¿CÓMO FUNCIONA?
// - Si obtenerNombreCompleto() devuelve "Ada Lovelace" → el test pasa. ✅ verde.
// - Si devuelve otra cosa → el test FALLA, y JUnit te muestra las dos cosas:
//     Expected : Ada Lovelace
//     Actual   : Ada  Lovelace     ← (dos espacios: un bug tipográfico real)
//   Ese diff es la mitad del valor de un test: no solo te dice QUE está mal, te dice QUÉ.
```

🔴 **El orden de los argumentos importa y es contraintuitivo: primero lo esperado, después lo real.** `assertEquals(esperado, real)`. Si los invertís, el test funciona igual pero el mensaje de error te va a mentir sobre cuál es cuál.

### 3.1 Las tres partes de un test — Arrange, Act, Assert 🔴

Todo test tiene la misma anatomía. El video la describe sin nombrarla; el nombre estándar de la industria es **AAA**:

| Fase | Qué hacés | En el ejemplo |
|---|---|---|
| **Arrange** (preparar) | Instanciás los objetos y armás el escenario | `new Persona()`, los setters |
| **Act** (actuar) | Ejecutás el método que querés probar | `persona.obtenerNombreCompleto()` |
| **Assert** (verificar) | Comparás el resultado con lo esperado | `assertEquals(...)` |

En tests chicos, *act* y *assert* suelen escribirse en la misma línea (como arriba). En tests grandes conviene separarlos: se lee mucho mejor.

### 3.2 Crear el test en IntelliJ

No hace falta escribirlo desde cero. Click derecho sobre el nombre de la clase → **`Generate` → `Test…`** → elegís **JUnit 5** y los métodos que querés testear. Te arma el archivo, en el paquete correcto, dentro de `src/test/java`.

---

## 4. Assertions 🔴

`Assertions` es la clase de JUnit 5 que trae todas las verificaciones. Las que vas a usar:

```java
// ── Igualdad ──
Assertions.assertEquals(4, madrid.cantPasajerosQueLlegaron(dia));
Assertions.assertNotEquals(0, vuelo.cantPasajeros());

// ── Booleanos ──
Assertions.assertTrue(grupo.puedeEntrar(persona3));
Assertions.assertFalse(vuelo.estaCompleto());

// ── Nulos ──
Assertions.assertNull(pais.getContinente());
Assertions.assertNotNull(vuelo.getAvion());

// ── Excepciones: verificar que algo EXPLOTA como debe ──
Assertions.assertThrows(SaldoInsuficienteException.class, () -> cuenta.retirar(1000.0));
// Resultado esperado: el test PASA si retirar() lanza esa excepción.
//                     Si NO la lanza, el test FALLA. Es la forma de testear los caminos de error.
```

🔴 **Comparar `double`: siempre con delta.**

```java
// ❌ FRÁGIL: los decimales no son exactos en binario.
//    0.1 + 0.2 no da exactamente 0.3 en ninguna computadora.
Assertions.assertEquals(270.00, viaje.duracionTotalEnMins());

// ✅ CORRECTO: el tercer parámetro es la tolerancia aceptada.
Assertions.assertEquals(270.00, viaje.duracionTotalEnMins(), 0.001);
//                                                            ↑ delta
// Ahora el test pasa si el resultado está a menos de 0.001 del esperado.
```

El repo de la cátedra usa `0.0` como delta (tolerancia cero). Funciona en esos casos puntuales porque las cuentas son simples, pero es frágil: en cuanto aparezca una división, se rompe. **Usá un delta chico pero real.**

### 4.1 `@DisplayName` 🟡

Sin él, el reporte de tests muestra el nombre del método: `puedeAgregarPersonaAGrupo`. Con él, muestra una frase:

```java
@Test
@DisplayName("La tercera persona puede entrar al grupo porque no se supera la capacidad")
public void puedeAgregarPersonaAGrupo() { ... }
```

**No es cosmética.** Cuando un test falla dentro de tres meses, el `@DisplayName` te dice **qué regla de negocio se rompió** sin que tengas que leer el código. Escribilos como afirmaciones de negocio, no como descripciones técnicas.

---

## 5. Fixtures: no repetir el escenario 🔴

Diez tests que necesitan los mismos objetos = diez veces el mismo `Arrange`. Eso no escala y, peor, cuando cambies algo tenés que tocar diez lugares.

**`@BeforeEach` corre antes de cada test.** Ahí armás el escenario común:

```java
class VueloTest {

    private Vuelo vuelo;
    private Avion avion;

    @BeforeEach                  // ← se ejecuta ANTES DE CADA @Test, de cero
    void inicializar() {
        this.avion = new Avion(40);
        this.vuelo = new Vuelo();
        this.vuelo.setAvion(this.avion);
    }

    @Test
    void vueloVacioEstaAlCeroPorCiento() {
        Assertions.assertEquals(0.0, vuelo.capacidadRealOcupadaPorPasajeros(), 0.001);
    }

    @Test
    void cuatroPasajerosEnAvionDe40EsDiezPorCiento() {
        vuelo.agregarPasajeros(p1, p2, p3, p4);
        Assertions.assertEquals(10.0, vuelo.capacidadRealOcupadaPorPasajeros(), 0.001);
    }
}
```

🔴 **`@BeforeEach` corre antes de CADA test, no una sola vez.** Y eso es deliberado: cada test arranca con objetos **frescos**. Si un test ensucia el escenario, no contamina al siguiente. **Los tests tienen que ser independientes entre sí** — si el orden en que corren cambia el resultado, tenés un problema.

(Existe `@BeforeAll`, que corre una única vez para toda la clase. Se usa para setup caro, como levantar una conexión. Casi nunca lo vas a necesitar.)

### 5.1 El patrón `BaseTest` — lo mejor del repositorio 🔴

El repo de la cátedra hace algo que vale la pena robar. En vez de repetir el fixture en cada clase de test, arma **una clase abstracta base** con todo el escenario, y **los tests heredan de ella**:

```java
public abstract class BaseTest {                    // abstracta: no es un test, es el escenario
    protected List<Pais> paises;
    protected List<Ciudad> ciudades;
    protected List<Aeropuerto> aeropuertos;
    protected List<Avion> aviones;
    protected List<Pasajero> pasajeros;

    @BeforeEach
    public void inicializar() {
        this.inicializarPaises();          // 8 países reales
        this.inicializarCiudades();        // 9 ciudades
        this.inicializarAeropuertos();     // AEP, EZE, MAD, HND, BER...
        this.inicializarAviones();         // aviones de 40, 80, 100, 120 asientos
        this.inicializarPasajeros();       // 7 pasajeros
    }

    // Buscadores: para que los tests pidan por nombre lo que necesitan
    protected Aeropuerto buscarAeropuerto(String codigo) {
        return this.aeropuertos.stream()
                .filter(a -> a.getCodigoInternacional().equals(codigo))
                .findFirst()
                .orElse(null);
    }
    // ... buscarCiudad, buscarPais, buscarPasajero, buscarAvion
}
```

Y después, cada test es **puro negocio**, sin ruido de setup:

```java
public class VueloTest extends BaseTest {           // ← hereda TODO el escenario

    @Test
    public void vueloOcupadoAl10() {
        Vuelo vuelo = new Vuelo();
        vuelo.setAvion(buscarAvionPorCantAsientos(40));      // ← se lee como una frase
        vuelo.setOrigen(buscarAeropuerto("EZE"));
        vuelo.setDestino(buscarAeropuerto("MAD"));
        vuelo.agregarPasajeros(
                buscarPasajero("Angie", "Rodriguez"),
                buscarPasajero("Carlos", "Polanco"),
                buscarPasajero("Laura", "Puerto"),
                buscarPasajero("Jorge", "Marin")
        );

        Assertions.assertEquals(10.00, vuelo.capacidadRealOcupadaPorPasajeros(), 0.001);
        //                      ↑ 4 pasajeros en un avión de 40 asientos = 10%
    }
}
```

🎯 **Anotate este patrón: lo vas a usar en el TPA.** Un dominio con muchas entidades entrelazadas necesita un escenario compartido, o cada test se vuelve cincuenta líneas de `new`. `BaseTest` + buscadores por nombre = tests que se leen como el enunciado.

---

## 6. El repo está en JUnit 4 — la tabla de traducción 🟡

El repositorio de "Vuelos y Aeropuertos" usa **JUnit 4**. La materia usa **JUnit 5**. Cuando leas ese código, traducí:

| | **JUnit 4** (el repo) | **JUnit 5** (lo que usás vos) |
|---|---|---|
| Import | `org.junit.*` | `org.junit.jupiter.api.*` |
| Test | `@Test` | `@Test` *(igual)* |
| Setup | **`@Before`** | **`@BeforeEach`** |
| Setup único | `@BeforeClass` | `@BeforeAll` |
| Aserciones | **`Assert`**`.assertEquals(...)` | **`Assertions`**`.assertEquals(...)` |
| Nombre legible | *(no existe)* | `@DisplayName` |
| Excepciones | `@Test(expected = X.class)` | `Assertions.assertThrows(...)` |

⚠️ **No se mezclan.** Si importás `org.junit.Test` (JUnit 4) en un proyecto configurado con JUnit 5, el test **simplemente no se ejecuta** — y no falla, que es peor: se lo saltea en silencio y vos creés que pasó.

---

## 7. Code smell: los números mágicos 🟡

Del Video 4. Este método vive en una clase `Grupo`:

```java
public Boolean puedeEntrar(Persona persona) {
    return personas.size() < 10;      // ← ¿qué es 10? ¿por qué 10?
}
```

Ese `10` es un **número mágico**: una constante hardcodeada en medio de la lógica, sin nombre, sin explicación. Los problemas:

- **Nadie sabe qué significa** sin leer el contexto.
- **Si aparece en cinco métodos**, cambiarlo implica encontrar los cinco.

La solución es darle un nombre:

```java
public class Grupo {
    private static final int CAPACIDAD_MAXIMA = 10;
    //     ↑       ↑
    //   static  final → una constante: una sola, y no se puede cambiar.
    //                   Por convención, las constantes van en MAYUSCULAS_CON_GUION_BAJO.

    public boolean puedeEntrar(Persona persona) {
        return this.personas.size() < CAPACIDAD_MAXIMA;   // ahora se lee solo
    }
}
```

**El nombre es la mitad del refactor.** Extraer la constante sin ponerle el nombre correcto no arregla nada — te deja un número mágico con etiqueta equivocada, que es peor que el número pelado.

---

## 📌 Para el parcial, si te preguntan

> **¿Qué es un test unitario y para qué sirve?**
> Es código que verifica automáticamente el comportamiento de una unidad del sistema —típicamente un método— comparando el resultado obtenido con el esperado. Sirve para detectar regresiones: si un cambio rompe algo que antes funcionaba, el test falla y lo dice antes de que el error llegue a producción.

> **¿Cuáles son las tres partes de un test?**
> *Arrange* (preparar el escenario, instanciando los objetos necesarios), *Act* (ejecutar el método a probar) y *Assert* (verificar que el resultado obtenido coincide con el esperado).

---

## ✅ Checkpoint — Parte 4

1. El repo de la cátedra tiene un método `cantVuelosTotales()` correctamente escrito, y sin embargo siempre devuelve 0. ¿Por qué? ¿Y por qué nadie se dio cuenta?
2. ¿En qué carpeta viven los tests, y qué relación tiene su estructura con la del código?
3. ¿Cuál es el orden de los argumentos de `assertEquals`? ¿Qué pasa si los invertís?
4. Nombrá las tres fases de un test y qué se hace en cada una.
5. ¿Por qué comparar dos `double` con `assertEquals` sin delta es frágil?
6. ¿Cuántas veces corre `@BeforeEach`? ¿Por qué es importante que sea así?
7. ¿Para qué sirve `@DisplayName` si el método ya tiene nombre?
8. Estás leyendo el repo de la cátedra y ves `@Before` y `Assert.assertEquals(...)`. ¿En qué versión de JUnit está, y cómo se escribe eso en la que usás vos?
9. Copiás un test del repo a tu proyecto (que usa JUnit 5) sin cambiar los imports. El test no aparece en el reporte. ¿Qué pasó?
10. ¿Qué es un número mágico y por qué extraerlo a una constante no alcanza si el nombre está mal?
11. ¿Cómo testeás que un método **lanza** la excepción que tiene que lanzar?

---

## ⚠️ Errores del material previo (Parte 4)

1. **El refactor del `Grupo` en el Video 4 está mal.** Para matar el número mágico `10` extraen una constante y la llaman `cantidadMinimaDePersonas`, inicializada en `0` — pero `size() < 10` era un **máximo**, una capacidad, no un mínimo. Con `size() < 0`, nadie puede entrar nunca, y el test se rompe. El video presenta esa rotura como la lección ("el test atrapó una regresión"), y **como lección de tests está perfecta**. Pero el refactor que la causó es incorrecto: el nombre no describe lo que el número hace. La versión correcta es `CAPACIDAD_MAXIMA = 10`, y así no se rompe nada.
2. **El `pom` del material declara `junit-jupiter-api`**, que trae la API pero no el motor. Los tests corren en IntelliJ y **no** corren con `mvn test`. Ya lo vimos en la Parte 1: usá `junit-jupiter`.
3. **El repo mezcla los tests de `Grupo` dentro de `PersonaTest`.** El propio autor lo admite en el video ("lo ideal sería un `GrupoTest`"). Un test por clase.

---

**Lo que viene — Parte 5: Vuelos y Aeropuertos.** El ejercicio completo: enunciado, diagrama de clases, y la implementación real, comentada. Cerramos con los **dos requerimientos que el repo no resuelve** — y que quedan como tu ejercicio.

**FIN DE LA PARTE 4**
