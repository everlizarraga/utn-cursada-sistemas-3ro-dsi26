# 📘 APUNTE MAESTRO — Preclase 02 · Parte 5
## Práctica: Vuelos y Aeropuertos

**Unidad:** `preclase02` — material previo obligatorio de la clase 2
**Viene de:** Partes 1 a 4. Acá se junta todo: Maven, el lenguaje, las colecciones, los streams y los tests.
**Peso:** 🔴 Es el único ejercicio completo de modelado que tenés antes de arrancar el TPA.

---

## 1. El enunciado 🔴

> **Contexto general.** Hemos sido contratados para diseñar y desarrollar un sistema de Gestión de Vuelos y Aeropuertos.
>
> Una aerolínea ofrece vuelos para transportar pasajeros, en aviones, entre diferentes ciudades. Cada vuelo parte de un aeropuerto de inicio y arriba a un aeropuerto destino. Algunos vuelos pueden tener **escalas** intermedias. Los aeropuertos están localizados en ciudades, **pudiendo una ciudad tener más de un aeropuerto**. A cada vuelo se le asigna un avión y una **tripulación** (2 pilotos, 1 operador de comunicaciones, 2 comisarios de abordo y 4 azafatas). Cada pasajero de un vuelo tiene asignado un asiento.

**Los 10 requerimientos.** El sistema debe informar:

| # | Requerimiento | Dónde vive la respuesta |
|---|---|---|
| 1 | La capacidad de un vuelo ocupada por pasajeros | `Vuelo` |
| 2 | La duración total de un vuelo, contando escalas | `Viaje` |
| 3 | Vuelos que partieron / llegaron a un aeropuerto en un día | `Aeropuerto` |
| 4 | El aeropuerto que recibió **menos** vuelos en escalas | — **sin resolver** (sección 8) |
| 5 | Vuelos totales que realizó un pasajero | `Pasajero` |
| 6 | Cantidad de aeropuertos que tiene una ciudad | `Ciudad` |
| 7 | La ciudad que **más** pasajeros recibió en un día | — **sin resolver** (sección 8) |
| 8 | Veces que un pasajero visitó una ciudad | `Pasajero` |
| 9 | Vuelos que realizó una tripulación en un período | `Tripulacion` |
| 10 | La aerolínea con más pasajeros en un mes | `Aerolinea` |

🎯 **Fijate en la columna de la derecha: cada requerimiento se resuelve en la clase que tiene los datos.** La ciudad sabe de sus aeropuertos; el aeropuerto sabe de sus vuelos; el vuelo sabe de sus pasajeros. Eso no es casualidad, es **el principio central del diseño orientado a objetos**: el comportamiento va donde están los datos. Guardá esta idea — la materia entera gira alrededor de ella.

⚠️ **Este ejercicio es de implementación, no de diseño.** El diagrama de clases **viene dado**: no se discute por qué las entidades son esas ni por qué los métodos están donde están. La discusión de diseño arranca en la clase 3. Acá se trata de traducir un diagrama a Java.

---

## 2. El modelo 🔴

Quince tipos, agrupados en tres paquetes:

```
domain/
├── lugares/     Ciudad · Pais · Aeropuerto · Continente(enum)
├── viajes/      Vuelo · Viaje · Escala · Avion · Aerolinea
└── personas/    Persona(abstracta) · Pasajero · Empleado · Tripulacion
                 TipoDeDocumento(enum) · Cargo(enum)
```

**Las relaciones, en una frase cada una:**

```
Pais ──contiene──> Ciudad ──contiene──> Aeropuerto ──registra──> Vuelo
                                                                   │
                            Persona (abstracta)                    ├──> Avion
                              ├── Pasajero ──vuela en──> Vuelo     ├──> Pasajero (varios)
                              └── Empleado                          └──> Tripulacion
                                     │
                                Tripulacion ──agrupa──> Empleado

Aerolinea ──tiene──> Viaje ──agrupa──> Vuelo + Escala
```

### 2.1 Por qué existen los paquetes 🟡

Un paquete es una carpeta con significado. Resuelve dos cosas:

1. **Organización.** Ciento cincuenta clases sueltas en una carpeta son inencontrables. Agrupadas por concepto, sabés dónde buscar.
2. **Colisión de nombres.** Dos clases pueden llamarse igual **si están en paquetes distintos**. `personas.Persona` y `rrhh.Persona` conviven sin problema.

**Cómo agrupar:** por concepto o funcionalidad. No hay regla estricta; es una decisión de diseño. Acá: lugares, viajes, personas.

---

## 3. Traducir el diagrama a código 🔴

### 3.1 La anatomía de una clase del dominio

```java
package domain.lugares;                    // ① a qué paquete pertenece

import domain.viajes.Vuelo;                // ② lo que uso de OTROS paquetes
import java.time.LocalDate;                //    (IntelliJ los escribe solo: Alt+Enter)
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ciudad {

    // ③ ATRIBUTOS: siempre private (encapsulamiento)
    private String nombre;
    private Pais pais;
    private List<Aeropuerto> aeropuertos;   // ← tipado con la INTERFAZ List, no ArrayList

    // ④ CONSTRUCTOR
    public Ciudad(String nombre, Pais pais) {
        this.aeropuertos = new ArrayList<>();   // 🔴 las colecciones SIEMPRE se inicializan acá
        this.pais = pais;
        this.nombre = nombre;
        pais.agregarCiudades(this);             // ← bidireccionalidad (sección 3.3)
    }

    // ⑤ GETTERS y SETTERS (IntelliJ: Alt+Insert → Getter and Setter)
    public String getNombre() { return nombre; }
    public Pais getPais()     { return pais; }

    // ⑥ COMPORTAMIENTO: los requerimientos del enunciado
    public Integer cantDeAeropuertos() {
        return this.aeropuertos.size();     // requerimiento 6, resuelto
    }
}
```

🔴 **Las colecciones se inicializan en el constructor.** Un `List` que no inicializaste es `null`, no una lista vacía. `this.aeropuertos.add(...)` sobre un `null` es un `NullPointerException` inmediato. Es el error más común al arrancar.

🔴 **No hay setter para la colección.** Hay getter, pero no `setAeropuertos(...)`. **La clase es dueña de su colección**: nadie de afuera se la reemplaza entera. Si querés agregarle algo, pasás por un método suyo (`agregarAeropuertos`). Eso es encapsulamiento aplicado a colecciones, y es una convención que vas a ver todo el año.

### 3.2 Varargs: `Ciudad ... ciudades` 🔴

En vez de un método que agrega **una** ciudad y que hay que llamar diez veces, un método que acepta **cuántas quieras**:

```java
public void agregarCiudades(Ciudad ... ciudades) {
    //                            ↑↑↑
    //                    "parámetros variables" (varargs)
    Collections.addAll(this.ciudades, ciudades);
    //   ↑
    // de java.util. Agrega todos los elementos de golpe a una colección.
}

// Se puede llamar con las que sea — incluso con ninguna:
argentina.agregarCiudades();                                    // 0 → no rompe
argentina.agregarCiudades(caba);                                // 1
argentina.agregarCiudades(caba, ezeiza, cordoba, rosario);      // n
```

**Dos reglas del lenguaje:**
- El vararg va **siempre al final** de la lista de parámetros. `metodo(String x, Ciudad ... c)` ✅ · `metodo(Ciudad ... c, String x)` ❌
- **Solo uno por método.** No podés tener dos varargs.

### 3.3 Bidireccionalidad: el patrón, y dónde el repo lo rompe 🔴

Mirá el constructor de `Ciudad` otra vez:

```java
public Ciudad(String nombre, Pais pais) {
    this.pais = pais;                  // ① la ciudad se guarda su país
    pais.agregarCiudades(this);        // ② y el país se guarda esta ciudad
}
```

**Las dos líneas son la misma relación, vista desde los dos lados.** Si solo hacés la ①, la ciudad conoce a su país pero el país no conoce a la ciudad, y `pais.getCiudades()` viene vacío.

**La técnica:** el objeto que se crea **se auto-registra** en el otro extremo. Así es imposible olvidarse — está dentro del constructor.

El repo lo hace bien en cuatro lugares:

| Relación | Dónde | ¿Registra el otro lado? |
|---|---|---|
| `Ciudad` ↔ `Pais` | constructor de `Ciudad` | ✅ |
| `Aeropuerto` ↔ `Ciudad` | constructor de `Aeropuerto` | ✅ |
| `Vuelo` ↔ `Aeropuerto` (destino) | `Vuelo.setDestino()` | ✅ |
| `Vuelo` ↔ `Tripulacion` | `Vuelo.setTripulacion()` | ✅ |
| **`Vuelo` ↔ `Pasajero`** | **`Vuelo.agregarPasajeros()`** | ❌ **falta** |

🔴 **Y ese es el bug que rompe los requerimientos 5 y 8** (lo vimos en la Parte 4). Así estaría bien:

```java
public void agregarPasajeros(Pasajero ... pasajeros) {
    Collections.addAll(this.pasajeros, pasajeros);

    for (Pasajero pasajero : pasajeros) {
        pasajero.agregarVuelos(this);      // ← la línea que falta en el repo
    }
}
// Con esto, cantVuelosTotales() y cantVecesQueVisitaste() dejan de devolver 0.
```

⚠️ **Ojo con el efecto espejo:** si el registro es mutuo, cuidado con no llamarse en círculo (`agregarPasajeros` → `agregarVuelos` → `agregarPasajeros` → …). La convención es que **un solo lado tiene la iniciativa**: el que expone el método público registra al otro, y el otro solo agrega a su lista sin devolver la llamada.

### 3.4 Fechas: `LocalDate` y `LocalDateTime` 🔴

```java
private LocalDateTime fecha;       // fecha Y hora → un vuelo sale el 20/05 a las 8:35
private LocalDate dia;             // solo fecha   → "los vuelos del 20/05"
```

🎯 **La regla que vale para todo:** *antes de crear una clase que parece obvia, fijate si Java ya la trae.* Nadie necesita escribir una clase `Fecha`. Java tiene `LocalDate`, `LocalDateTime`, `Duration`, `Period`.

**Los métodos que usa el repo:**

```java
vuelo.getFecha()                       // LocalDateTime → 2021-05-20T08:35
     .toLocalDate()                    // LocalDate     → 2021-05-20  (le corta la hora)
     .isEqual(dia);                    // boolean       → ¿es este día?

// isAfter(), isBefore(), isEqual() → comparar fechas. Nunca con ==.

this.fecha.plusMinutes(780);           // sumar tiempo: devuelve un LocalDateTime NUEVO
// 🔴 LocalDateTime es INMUTABLE: plusMinutes NO modifica la fecha original, devuelve otra.
//    Si escribís `this.fecha.plusMinutes(60);` sin asignar el resultado, no pasa nada.
```

### 3.5 Los métodos del dominio, comentados 🔴

**Requerimiento 1 — capacidad ocupada** (en `Vuelo`):

```java
public Double capacidadRealOcupadaPorPasajeros() {
    return (this.cantPasajeros() * 100.00) / this.avion.getCantAsientos();
    //                              ↑↑↑↑↑↑
    // 🔴 El 100.00 (y no 100) es lo que fuerza la división DECIMAL.
    //    Con `* 100` y enteros, Java hace división ENTERA y trunca:
    //       4 pasajeros / 40 asientos → (4*100)/40 = 10        ✅ da igual acá
    //       5 pasajeros / 40 asientos → (5*100)/40 = 12  (¡y era 12.5!)  ❌
}
// Resultado esperado: 4 pasajeros en un avión de 40 → 10.0
```

**Requerimiento 8 — veces que visitó una ciudad** (en `Pasajero`):

```java
public Integer cantVecesQueVisitaste(Ciudad unaCiudad) {
    return (int) this.vuelos                             // el cast: count() da long
            .stream()
            .filter(v -> v.tuDestinoEs(unaCiudad))       // me quedo con los que fueron ahí
            .count();
}
```

Y fijate la **cadena de responsabilidades** que hay detrás de ese `tuDestinoEs`:

```java
// En Vuelo:      "¿mi destino está en esta ciudad?" → le pregunta al aeropuerto
public boolean tuDestinoEs(Ciudad unaCiudad) {
    return this.destino.estasEn(unaCiudad);
}

// En Aeropuerto: "¿yo estoy en esta ciudad?" → compara
public boolean estasEn(Ciudad unaCiudad) {
    return this.ciudad.equals(unaCiudad);
}
```

🎯 **Nadie sale a hurgar en las tripas del otro.** `Pasajero` no escribe `v.getDestino().getCiudad().equals(ciudad)` — le **pregunta** al vuelo, que le pregunta al aeropuerto. Cada objeto responde por lo suyo. Recordá el nombre de esto porque va a volver: **Ley de Demeter** ("hablá con tus amigos, no con los amigos de tus amigos").

⚠️ **Un detalle que el repo deja abierto:** `estasEn` usa `equals()` sobre `Ciudad`, pero `Ciudad` **no sobrescribe `equals`**. Así que compara identidad: funciona en los tests porque son la misma instancia, pero es frágil. Lo correcto es sobrescribir `equals` (y `hashCode`) en `Ciudad`.

**Requerimiento 2 — duración total con escalas** (en `Viaje`):

```java
public Double duracionTotalEnMins() {
    return this.duracionTotalDeEscalasEnMins() + this.duracionTotalDeVuelosEnMins();
}

private Double duracionTotalDeVuelosEnMins() {          // private: es auxiliar, nadie más lo usa
    return this.vuelos.stream()
            .mapToDouble(v -> v.getDuracionEstimadaEnMins())
            .sum();
}

private Double duracionTotalDeEscalasEnMins() {
    return this.escalas.stream()
            .mapToDouble(Escala::getDuracionEnMins)      // method reference: no recibe parámetros
            .sum();
}
// Resultado esperado: 3 vuelos (60+45+75) + 2 escalas (60+30) = 270.0
```

🎯 **Fijate en la descomposición.** El método público se lee en una línea; los dos privados hacen el trabajo. **Métodos chicos, con nombre, que hacen una cosa.** Es la diferencia entre código que se entiende y código que se descifra.

**Requerimiento 7 (parcial) — pasajeros que llegaron a una ciudad** (en `Ciudad`): es el del `flatMap`, ya lo desarmamos en la Parte 3.

---

## 4. Herencia: `Persona` 🔴

```java
public abstract class Persona {          // abstracta: no se instancia
    private String nombre;               // ⚠️ el diagrama dice `protected`; el repo usa `private`
    private String apellido;
    private Integer nroDeDocumento;
    private TipoDeDocumento tipoDeDocumento;

    // + getters y setters
}

public class Pasajero extends Persona {
    private List<Vuelo> vuelos;
    private Integer nroDePasaporte;
    private Pais nacionalidad;

    public Pasajero(String nombre, String apellido) {
        super.setNombre(nombre);         // ← usa el SETTER del padre...
        super.setApellido(apellido);     //   ...porque los atributos son private
        this.vuelos = new ArrayList<>();
    }
}

public class Empleado extends Persona {
    private Cargo cargo;                 // enum: PILOTO, AZAFATA, COMISARIO, OPERADOR_COMUNICACIONES
    private Aerolinea empleador;
}
```

⚠️ **Acá hay una inconsistencia entre las tres fuentes**, y conviene que la veas:

| Fuente | Los atributos de `Persona` son… |
|---|---|
| El diagrama de clases | `#` → **protected** |
| El Video 3 (lo enseña explícitamente) | **protected**, y accede con `super.nombre` |
| **El repo** | **private**, con getters/setters |

Ninguna está *mal*: es una decisión de diseño. Pero el repo, al hacerlos `private`, se obliga a llamar `super.setNombre(nombre)` en el constructor, que es feo. **Lo limpio es un constructor en el padre:**

```java
public abstract class Persona {
    protected String nombre;

    public Persona(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }
}

public class Pasajero extends Persona {
    public Pasajero(String nombre, String apellido) {
        super(nombre, apellido);          // ← el padre construye su parte. Limpio.
        this.vuelos = new ArrayList<>();
    }
}
```

🔴 **Recordá la regla de la Parte 2:** si el padre tiene constructor con parámetros, el hijo **está obligado** a llamarlo con `super(...)`, y va en la primera línea. IntelliJ te lo genera con Alt+Enter.

---

## 5. IntelliJ: lo que te ahorra tiempo 🟡

El Video 3 es, en buena medida, una demostración de esto. Vale la pena:

| Atajo / acción | Qué hace |
|---|---|
| **`Alt+Enter`** sobre el texto en rojo | **Crear la clase / el enum / el método que no existe.** Escribís `private Pais pais;` sin que `Pais` exista, te lo marca en rojo, Alt+Enter → "Create class Pais" → elegís el paquete. Listo. |
| **`Alt+Insert`** (o click derecho → `Generate`) | Constructor · Getters y Setters · `equals()` y `hashCode()` · `toString()` · **Test** |
| Escribir `get` + Enter | Autocompleta el getter del atributo |
| **`// TODO`** | Comentario especial: IntelliJ los indexa. Panel **TODO** abajo → **todos los pendientes del proyecto**, con archivo y línea. |

🎯 **La técnica del `// TODO`, que es más útil de lo que parece.** Cuando estás modelando y no sabés todavía cómo resolver un método, **no te trabes**: dejá el esqueleto, poné un `return` provisorio, marcá el pendiente y seguí.

```java
public Integer cantVuelosQueLlegaronElDia(LocalDate dia) {
    // TODO: resolver filtrando por fecha de llegada
    return 0;                 // provisorio, para que compile
}
```

Así avanzás con la estructura completa del modelo y después completás los agujeros. **Esto vale oro en el TPA**, donde el trabajo se reparte entre seis personas.

⚠️ **Un `return null` provisorio en un método que devuelve una colección es una bomba** (`NullPointerException` en cuanto alguien le haga `.stream()`). Si tenés que devolver algo provisorio, devolvé `Collections.emptyList()`.

---

## 6. Los bugs del repositorio — la lista completa 🔴

El repo es material de ayudantes y **no es palabra final**. Estos son sus problemas reales. Reconocerlos es parte del ejercicio; **no los copies al TPA.**

| # | Problema | Consecuencia |
|---|---|---|
| 1 | `Vuelo.agregarPasajeros()` no registra el vuelo en el pasajero | 🔴 **Requerimientos 5 y 8 devuelven 0.** Siempre. |
| 2 | `Vuelo.setOrigen()` no registra el vuelo en el aeropuerto de origen | 🔴 `cantVuelosQuePartieronElDia()` cuenta mal: el aeropuerto solo conoce los vuelos que **llegan**. |
| 3 | `Ciudad` no sobrescribe `equals()` / `hashCode()` | 🟡 `estasEn()` compara identidad. Frágil. |
| 4 | `Vuelo.cantAsientosOfrecidos` nunca se usa | 🟢 Atributo muerto (la capacidad se calcula con `avion.getCantAsientos()`). |
| 5 | `Viaje.cantPasajerosTotales()` cuenta dos veces al pasajero que toma varios vuelos del mismo viaje | 🟡 **El autor lo sabe**: dejó la versión correcta comentada, con `Collectors.toSet()`. |
| 6 | Los buscadores del `BaseTest` hacen `.orElse(null)` | 🟡 Usan `Optional` y tiran su protección a la basura. |

---

## 7. Ejercicio: los dos requerimientos que faltan 🔴

**El repo no resuelve el requerimiento 4 ni el 7.** No es casualidad: los dos necesitan una operación de streams que ningún video enseña. Son **tu ejercicio**, y son exactamente el escalón siguiente.

### Requerimiento 7 — "la ciudad que más pasajeros recibió en un día"

Ya tenés `Ciudad.cantPasajerosQueLlegaronElDia(dia)`, que te da el número **de una** ciudad. Falta lo que **compara todas** y devuelve la ganadora.

**Hints, escalonados. Parás cuando ya lo veas.**

1. Necesitás una operación terminal que devuelva **el máximo según un criterio**. Poné `.stream().` sobre una `List<Ciudad>` y mirá qué aparece.
2. Se llama `max(...)`, y recibe un `Comparator`: un objeto que sabe comparar dos ciudades.
3. El `Comparator` se arma con `Comparator.comparing(...)`, pasándole **qué valor extraer** de cada ciudad para compararlas.
4. `max()` devuelve un **`Optional<Ciudad>`** (podría no haber ninguna ciudad). Decidí qué hacer si viene vacío.

> **Pregunta que tenés que responderte antes de escribir nada:** *¿en qué clase va este método?* No es una pregunta de sintaxis. ¿Quién conoce a todas las ciudades? ¿`Pais`? ¿`Continente`? ¿Alguna clase que no existe todavía? **Esa decisión es diseño**, y es la parte valiosa del ejercicio.

### Requerimiento 4 — "el aeropuerto que recibió menos vuelos en escalas"

Más difícil, porque el modelo no te lo sirve en bandeja.

1. La `Escala` conoce **un** aeropuerto. Pero un aeropuerto **no conoce sus escalas**: la relación va en un solo sentido.
2. Entonces: ¿de dónde salen todas las escalas? Seguí el camino desde arriba — `Aerolinea` → `Viaje` → `Escala`.
3. Vas a necesitar **agrupar**: contar cuántas escalas tiene cada aeropuerto. Investigá `Collectors.groupingBy(...)` con `Collectors.counting()`.
4. Y después, el mínimo sobre ese resultado.

> ⚠️ El enunciado del requerimiento 4 es **ambiguo** — *"menos vuelos en escalas"* podría leerse de dos formas. Definí vos qué interpretás, escribilo, y testealo. Ambigüedad en el enunciado es exactamente lo que te va a pasar en el TPA.

**Y para los dos: escribí el test primero.** Ya sabés lo que pasa cuando no hay test.

---

## ✅ Checkpoint — Parte 5

1. ¿Por qué `cantDeAeropuertos()` vive en `Ciudad` y no en una clase `GestorDeAeropuertos`?
2. ¿Dónde se inicializan las colecciones de una clase, y qué pasa si te olvidás?
3. `Ciudad` tiene `getAeropuertos()` pero no `setAeropuertos()`. ¿Por qué no es un olvido?
4. ¿Qué son los varargs y cuáles son sus dos reglas?
5. Explicá qué hace `pais.agregarCiudades(this)` dentro del constructor de `Ciudad`, y qué se rompería si no estuviera.
6. `LocalDate` vs `LocalDateTime`: ¿cuándo usás cada uno? ¿Qué significa que sean inmutables?
7. En `capacidadRealOcupadaPorPasajeros()`, ¿qué pasaría si el `100.00` fuera `100`?
8. `Pasajero` pregunta `v.tuDestinoEs(ciudad)` en vez de `v.getDestino().getCiudad().equals(ciudad)`. ¿Por qué es mejor?
9. Los atributos de `Persona` son `private` en el repo y `protected` en el diagrama. ¿Qué consecuencia tiene esa diferencia en el constructor de `Pasajero`?
10. ¿Por qué `cantVuelosTotales()` de un pasajero siempre devuelve 0? Escribí el arreglo.
11. Estás modelando y no sabés cómo resolver un método. ¿Qué hacés para no trabarte, y qué cuidado hay que tener si el método devuelve una colección?

---

## ⚠️ Errores del material previo (Parte 5)

1. **El repo tiene los seis problemas de la sección 6.** Los dos primeros rompen requerimientos del enunciado.
2. **La versión del repo está vencida:** Java 8, JUnit 4. Se lee como referencia de **modelado**, no se usa como plantilla. El stack de este año es Java 21 + JUnit 5.
3. **El repo, el diagrama y el video no coinciden** en varios nombres (`cantAeropuertos` vs `cantDeAeropuertos`, `TipoDocumento` vs `TipoDeDocumento`) ni en la visibilidad de los atributos de `Persona`. Es normal que la solución final difiera del diagrama propuesto — pero conviene saberlo antes de buscar un método que no existe.

---

## 🏁 Cierre del material previo de la clase 2

Con esto cerrás las cinco partes:

| Parte | Qué te llevás |
|---|---|
| 1 | Git y **Maven**: construir, compilar, gestionar dependencias |
| 2 | **Java** como lenguaje: tipos, modificadores, POO en sintaxis Java, excepciones |
| 3 | **Colecciones y Streams** 🔴 — lo que más vas a usar |
| 4 | **JUnit 5** 🔴 — y por qué lo que no testeás, no funciona |
| 5 | **Vuelos y Aeropuertos** — el modelado completo, de punta a punta |

**Lo que sigue es la clase 2 en sí:** Cliente-Servidor, HTTP, APIs REST, y el consumo de una API externa desde Java. La cátedra da por sabido todo lo de arriba cuando arranca.

**FIN DE LA PARTE 5 — FIN DEL APUNTE MAESTRO DE LA PRECLASE 02**
