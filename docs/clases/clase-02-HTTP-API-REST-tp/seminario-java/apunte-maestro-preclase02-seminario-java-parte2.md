# 📘 APUNTE MAESTRO — Preclase 02 · Parte 2
## Java: el lenguaje

**Unidad:** `preclase02` — material previo obligatorio de la clase 2
**Viene de:** Parte 1 (Git y Maven). Ya sabés construir y compilar un proyecto; ahora vemos qué hay adentro.
**Marcas:** 🔴 central · 🟡 secundario · 🟢 al pasar.

---

## 1. Qué es Java 🟡

**Java es un lenguaje de programación orientado a objetos**, creado por Sun Microsystems. Su lema es **WORA** — *Write Once, Run Anywhere*: escribís el código una vez y corre en cualquier sistema operativo sin recompilarlo. Ya vamos a ver por qué puede prometer eso.

Dos propiedades que definen cómo se escribe:

- **Fuertemente tipado.** Cada variable y cada expresión tiene un tipo declarado, y no cambia de forma arbitraria. No podés meter un texto en una variable que dijiste que era un número.
- **Estáticamente tipado.** Los tipos se verifican **en tiempo de compilación**, no de ejecución. El error aparece antes de que el programa arranque.

> **🎯 Si venís de JavaScript / TypeScript:** el modelo mental de TypeScript es exactamente este, pero TS es una capa que se borra al compilar — el JavaScript resultante sigue siendo dinámico. En Java el tipado **no se borra**: es parte del lenguaje y del bytecode. Lo que en TS era una ayuda del editor, en Java es la ley.

### 1.1 Compilado *e* interpretado 🔴

Esta es **la** particularidad de Java, y la que explica el WORA.

- Un lenguaje **compilado** (C, Rust) se traduce a **código de máquina** antes de ejecutarse. Rapidísimo, pero el binario resultante solo corre en el sistema para el que se compiló.
- Un lenguaje **interpretado** (JavaScript, Python) se traduce a código de máquina **mientras se ejecuta**, línea por línea, por un intérprete. Portable, pero más lento.

**Java es un punto medio: usa los dos.**

```
   Tu código            Compilador            Formato               Máquina
   .java        →       javac         →       .class        →       virtual
  (Source Code)                            (Bytecode)              (JVM)
                                                                      ↓ ↑
                                                            Sistema Operativo
```

**El camino, paso a paso:**

1. Escribís `Vuelo.java` — texto plano, legible por humanos.
2. El compilador **`javac`** lo traduce a **bytecode**: un lenguaje intermedio, de instrucciones simples, que **no es código de máquina de ninguna computadora real**. Queda en un archivo `Vuelo.class`.
3. La **JVM** (Java Virtual Machine) lee ese bytecode y lo **interpreta**, traduciéndolo al código de máquina del sistema donde está corriendo.

**Y ahí está el truco del WORA:** el bytecode es siempre el mismo. Lo que cambia es la JVM — hay una para Windows, otra para Linux, otra para Mac. Vos compilás una vez; cada JVM se encarga de su plataforma. **La JVM es la capa que absorbe la diferencia entre sistemas operativos.**

> 📌 **Para el parcial, si te preguntan: ¿Java es compilado o interpretado?**
> Las dos cosas. El código fuente se **compila** a bytecode con `javac`, y ese bytecode lo **interpreta** la JVM en tiempo de ejecución, traduciéndolo al código de máquina de la plataforma. Esa doble etapa es lo que hace posible el principio *Write Once, Run Anywhere*.

### 1.2 JDK, JRE y JVM 🟡

Tres siglas que se confunden todo el tiempo. Son **cajas, una dentro de la otra**:

```
┌──────────────────────────────────────────────────┐
│ JDK — Java Development Kit                       │
│ Lo que necesitás para DESARROLLAR.               │
│ Es lo que instalás. Trae todo lo de abajo.       │
│                                                   │
│   • javac (el compilador)                        │
│   • herramientas de desarrollo                   │
│                                                   │
│   ┌──────────────────────────────────────────┐   │
│   │ JRE — Java Runtime Environment           │   │
│   │ Lo que necesitás para EJECUTAR.          │   │
│   │                                           │   │
│   │   • bibliotecas estándar                 │   │
│   │                                           │   │
│   │   ┌──────────────────────────────────┐   │   │
│   │   │ JVM — Java Virtual Machine       │   │   │
│   │   │ La máquina que corre el bytecode │   │   │
│   │   └──────────────────────────────────┘   │   │
│   └──────────────────────────────────────────┘   │
└──────────────────────────────────────────────────┘
```

**En la práctica: instalás el JDK y ya tenés todo.** Este año, **JDK 21**.

Dos cosas más sobre la JVM que conviene saber:

- **No es exclusiva de Java.** Kotlin, Scala y Groovy también compilan a bytecode y corren sobre ella.
- **Es una *especificación*, no un programa.** Distintos proveedores la implementan: Oracle, Amazon (Corretto), Eclipse (Temurin). Por eso vas a ver "Amazon Corretto 21" en el selector de IntelliJ: es una JVM, hecha por Amazon.

---

## 2. La JVM por dentro 🟢

**Compacto a propósito.** Esto es cultura general de la plataforma: te explica **de dónde salen dos errores que vas a ver**, y poco más. La probabilidad de que un parcial de *Diseño de Sistemas* te pregunte qué guarda el Metaspace es prácticamente nula. Leelo una vez, entendé los dos errores, seguí.

Al ejecutar un programa, la JVM organiza la memoria en dos zonas principales:

### El Heap (montículo) — dónde viven los objetos

Se crea al arrancar el programa. Guarda **todas las instancias de clases**. Es **compartido entre todos los hilos** de la aplicación.

> 🕳️ **Madriguera — hilos (threads)**
> Un hilo es la unidad más chica de ejecución dentro de un proceso: un flujo independiente de instrucciones. Una aplicación puede tener muchos corriendo a la vez. El problema aparece cuando dos hilos tocan el mismo objeto al mismo tiempo — como el Heap es compartido, eso es posible — y se pisan: se llama **condición de carrera**.
> *Volvé al camino — en el TPA vas a trabajar con un solo hilo; esto aparece más adelante en la materia.*

El Heap se divide en **Young Generation** (objetos recién creados: primero al *Eden*, después a los *Survivor* S1 y S2) y **Old Generation** (objetos que sobrevivieron mucho tiempo). Esa división existe para que el **Garbage Collector** trabaje más rápido.

🔴 **`OutOfMemoryError`:** se llenó el Heap. Estás creando objetos más rápido de lo que el Garbage Collector puede liberar memoria.

### El Stack (pila) — dónde viven las llamadas

A diferencia del Heap, **cada hilo tiene su propio Stack**. Guarda las **variables locales** y las **llamadas a métodos**.

Cada vez que invocás un método se crea un **frame** (marco) que se apila; cuando el método retorna, el frame se destruye.

🔴 **`StackOverflowError`:** el Stack se llenó de frames. La causa típica es una **recursión infinita**: un método que se llama a sí mismo sin condición de corte apila frames hasta reventar.

### El Garbage Collector 🟡

Es el mecanismo que **libera memoria automáticamente**: recorre el Heap y elimina los objetos que ya no tienen ninguna referencia apuntándoles.

Esto es una diferencia enorme con lenguajes como C, donde el programador reserva memoria (`malloc`) y la tiene que liberar a mano (`free`) — y si se olvida, la pierde. En Java **no existe el `free`**.

**El trade-off, honesto:** el GC te ahorra un trabajo inmenso (y una fuente inmensa de bugs), pero corre cuando él decide, no cuando vos querés. Un manejo manual y cuidadoso puede ser más eficiente. En la práctica no importa: Java es uno de los lenguajes más usados del mundo comercial precisamente porque el intercambio vale la pena.

### Lo demás, en tres líneas

- **Class Loader:** carga las clases en memoria cuando el programa las necesita.
- **Metaspace:** guarda los metadatos de las clases (nombres, métodos, campos). Antes de Java 8 se llamaba *PermGen*.
- **Compilador JIT** (*Just In Time*): mientras el programa corre, detecta el código que más se ejecuta y lo compila a código de máquina nativo para acelerarlo. Es lo que hace que Java, siendo interpretado, no sea lento.

---

## 3. Tipos: primitivos y wrappers 🔴

Java tiene **dos familias de tipos** y la diferencia importa.

**Primitivos** (van en minúscula): `int`, `double`, `boolean`, `char`, `long`, `float`, `short`, `byte`. **No son objetos.** Guardan el valor y nada más. Son rápidos y livianos.

**Wrappers** (van en mayúscula): `Integer`, `Double`, `Boolean`, `Character`, `Long`… Son **clases** que *envuelven* (de ahí el nombre) a un primitivo. Al ser objetos, entienden mensajes: podés llamarles métodos.

```java
int cantidad = 5;              // primitivo: es el número 5, y punto
Integer cantidad2 = 5;         // wrapper: es un OBJETO que contiene un 5

// Al ser objeto, el wrapper entiende mensajes:
String s = cantidad2.toString();        // "5"
int i = Integer.parseInt("42");         // 42  ← método de clase, muy usado

// Resultado esperado: los dos guardan un 5, pero solo el segundo es un objeto.
```

🔴 **Las dos diferencias que te van a morder:**

**1. Un wrapper puede ser `null`. Un primitivo no.**

```java
int a;              // vale 0 por defecto. NUNCA es null.
Integer b = null;   // perfectamente legal
b + 1;              // 💥 NullPointerException
```

**2. Los wrappers se comparan con `.equals()`, no con `==`.** Con objetos, `==` compara **si son el mismo objeto en memoria**, no si valen lo mismo. Es la misma trampa que `String`: nunca compares strings con `==`.

**¿Cuándo usar cada uno?** Regla práctica: **primitivos para variables locales y cálculos; wrappers cuando necesitás que el valor pueda ser `null`, o cuando lo metés en una colección** (las colecciones de Java solo guardan objetos: no existe `List<int>`, tiene que ser `List<Integer>`).

En el repositorio de la cátedra vas a ver `Integer` y `Double` por todos lados, incluso donde alcanzaría un primitivo. Es una elección de estilo de ellos, no una regla.

---

## 4. Modificadores de acceso 🔴

Definen **quién puede ver** una clase, un atributo o un método. Son cuatro, y el material previo **solo enseña tres** — el que falta es justamente el que aparece cuando te olvidás de escribir alguno.

| Modificador | Misma clase | Mismo paquete | Subclase (otro paquete) | Cualquiera |
|---|:---:|:---:|:---:|:---:|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(ninguno)* — **default / package-private** | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

⚠️ **El `default` (no escribir nada) NO es lo mismo que `public`.** Es visible solo dentro del mismo paquete. Es un error clásico: te olvidás del `private`, el código compila, y creés que es público cuando no lo es.

🔴 **La convención de la materia, que es la de siempre:** **todos los atributos van `private`**, y se accede a ellos por getters y setters. Eso es **encapsulamiento**: la clase controla su propio estado, nadie lo toca desde afuera sin pasar por sus métodos.

> **Recordá que** el encapsulamiento es uno de los pilares de la orientación a objetos: el objeto esconde sus datos y expone solo el comportamiento. Cuanto menos visible es algo, menos código puede romperlo.

**`protected` sirve para la herencia:** un atributo `protected` **sí** lo ve la subclase, aunque esté en otro paquete. Un `private`, no — ni siquiera la clase hija lo ve.

---

## 5. Clases, objetos y `static` 🟡

### 5.1 Crear objetos: el operador `new`

```java
Vehiculo chevrolet = new Vehiculo();
//  ↑        ↑            ↑
//  tipo   nombre de   operador new + constructor
//         la variable
```

### 5.2 `this` 🟡

**`this` es una referencia al objeto actual.** Dentro de un método, `this.velocidad` significa "el atributo `velocidad` **de este objeto**".

**¿Cuándo es obligatorio?** Cuando hay **colisión de nombres**: si un parámetro se llama igual que un atributo, `this` desambigua. Por eso todos los setters lo llevan:

```java
public void setNombre(String nombre) {
    this.nombre = nombre;
    //  ↑              ↑
    //  el atributo    el parámetro
    // Sin `this`, estarías asignando el parámetro a sí mismo: no hace nada.
}
```

**¿Cuándo conviene aunque no sea obligatorio?** La cátedra recomienda **usarlo siempre para atributos**, incluso donde el compilador no lo exige. El motivo es de lectura: en un método de 30 líneas, `this.aeropuertos` te dice de un vistazo que eso es **estado del objeto**, y no una variable local perdida más arriba. Es una convención, no una regla del lenguaje — pero el código de la cátedra la sigue.

### 5.3 `static`: variables y métodos **de clase** 🟡

Lo normal es que un atributo o un método pertenezca a **cada instancia**: cada `Vuelo` tiene su propia lista de pasajeros. Eso son **variables y métodos de instancia**: necesitás un objeto para usarlos.

Con `static`, en cambio, el atributo o el método pertenece a **la clase misma**, y es **compartido por todas las instancias**. No necesitás instanciar nada para usarlo:

```java
public class Configuracion {
    // Variable de clase: existe UNA sola, compartida por todas las instancias.
    private static int comprasMaximas = 10;

    // Método de clase: se invoca desde la CLASE, no desde un objeto.
    public static int getComprasMaximas() {
        return comprasMaximas;
    }
}

// Se usa así — sin new, sin instancia:
int max = Configuracion.getComprasMaximas();
// Resultado esperado: 10
```

🔴 **La regla que se olvida:** un método `static` **no puede acceder a atributos ni métodos de instancia directamente.** Y tiene todo el sentido: el método pertenece a la clase, y la clase no sabe de qué objeto le estás hablando — puede que no exista ninguno.

Ya lo usaste sin saberlo: `Integer.parseInt("42")`, `Collections.addAll(...)`, `List.of(...)` son todos métodos `static`.

---

## 6. POO en Java 🔴

**Los conceptos de orientación a objetos ya los sabés** — herencia, polimorfismo, interfaces, clases abstractas, encapsulamiento. No los vamos a re-explicar. Esta sección es **la sintaxis Java** de lo que ya conocés, más las **tres reglas propias del lenguaje** que sí son nuevas.

### 6.1 Herencia: `extends` — y es **única** 🔴

```java
public class Pasajero extends Persona {
    private Integer nroDePasaporte;
}
```

🔴 **En Java una clase puede heredar de UNA sola clase.** No hay herencia múltiple. Punto. (Sí podés implementar **muchas** interfaces — lo vemos en un momento.)

**Consecuencia práctica:** tu único cupo de herencia es caro. No lo gastes por conveniencia ("me viene bien reusar este método"), gastalo cuando la relación sea **semánticamente cierta**: un `Pasajero` *es una* `Persona`. Si no lo es, el diseño se va a torcer más adelante.

### 6.2 `super` — llamar al padre 🔴

**Dos usos, y conviene no mezclarlos:**

**(a) Llamar al método del padre desde el hijo**, cuando lo redefinís y querés *agregarle* comportamiento en vez de reemplazarlo del todo:

```java
public class Auto extends Vehiculo {

    @Override                        // le avisa al compilador: esto redefine un método del padre
    public void encendete() {
        super.encendete();           // ← primero hacé lo que hacía el padre (arrancar el motor)
        this.prenderLuces();         //   y ADEMÁS, lo mío
    }
}
```

**(b) Llamar al constructor del padre.** Y acá está la regla nueva:

🔴 **Si la superclase tiene un constructor con parámetros, la subclase está obligada a llamarlo.**

```java
public abstract class Persona {
    protected String nombre;

    public Persona(String nombre) {      // el padre EXIGE un nombre
        this.nombre = nombre;
    }
}

public class Pasajero extends Persona {
    private Integer nroDePasaporte;

    public Pasajero(String nombre, Integer nroDePasaporte) {
        super(nombre);                   // ← OBLIGATORIO, y va SIEMPRE en la primera línea
        this.nroDePasaporte = nroDePasaporte;   // el hijo puede pedir MÁS cosas que el padre
    }
}

// ¿CÓMO FUNCIONA?
// Un objeto no se puede construir "por la mitad": antes de que exista un Pasajero,
// tiene que existir la Persona que hay dentro de él. Por eso el constructor del padre
// corre primero, y por eso el compilador te obliga.
// Si no ponés super(...), la clase hija no compila. IntelliJ te lo genera con Alt+Enter.
```

### 6.3 Clases abstractas 🔴

Una **clase abstracta** es una clase que **no se puede instanciar**. Existe para ser heredada.

```java
public abstract class Persona {        // ← la palabra `abstract`, antes de `class`
    protected String nombre;

    // Método CONCRETO: tiene implementación. Las hijas lo heredan tal cual.
    public String getNombre() {
        return this.nombre;
    }

    // Método ABSTRACTO: solo la firma, sin cuerpo, y termina en `;`
    // Toda subclase concreta está OBLIGADA a implementarlo.
    public abstract String descripcionDelRol();
}

// new Persona();  ← 💥 NO COMPILA. Una clase abstracta no se instancia.
```

**Reglas:**
- Una clase abstracta **puede** tener métodos abstractos (sin cuerpo) y métodos concretos (con cuerpo), en cualquier proporción.
- Si tiene **al menos un** método abstracto, la clase **debe** declararse `abstract`.
- Una clase abstracta puede heredar de otra clase abstracta. Pero **al final de la cadena tiene que haber una clase concreta** que implemente todo lo pendiente — si no, nunca se puede instanciar nada.
- **Tiene estado:** puede tener atributos con valores. Es una clase, al fin y al cabo.

> **⚠️ Error del material.** El Video 1 (49:57) afirma, tres veces, que *"una clase abstracta es una clase que tiene al menos un método abstracto"*. **Es falso.** Una clase abstracta **puede no tener ningún** método abstracto: alcanza con escribir `abstract`, y sirve igual para impedir que se instancie. Lo cierto es **la inversa**: si una clase tiene un método abstracto, entonces está obligada a ser abstracta. Y de hecho, la `Persona` del repositorio de la cátedra **es abstracta y no tiene un solo método abstracto** — el propio código de ellos contradice al video.

### 6.4 Interfaces 🔴

Una **interfaz** define **qué mensajes entiende** algo, sin decir cómo los responde. Es un **contrato**.

```java
public interface Volador {
    void despegar();               // implícitamente public y abstract: es una firma, nada más
    Double altitudMaxima();
}

public class Avion implements Volador {     // ← `implements`, no `extends`

    @Override
    public void despegar() {
        // acá SÍ va la implementación: es obligatorio
    }

    @Override
    public Double altitudMaxima() {
        return 12000.0;
    }
}
```

🔴 **Las dos diferencias con una clase abstracta, que son las que te van a preguntar:**

| | Clase abstracta | Interfaz |
|---|---|---|
| **¿Tiene estado?** | **Sí** — atributos con valores | **No** — solo constantes |
| **¿Cuántas puede tener una clase?** | **Una** (`extends`) | **Muchas** (`implements A, B, C`) |

Y de ahí sale el criterio práctico: si necesitás **compartir estado y comportamiento** entre clases emparentadas, clase abstracta. Si necesitás decir que **cosas distintas entienden el mismo mensaje**, interfaz. La interfaz es la herramienta del **polimorfismo**.

> 🕳️ **Madriguera — `default methods`**
> Desde Java 8, una interfaz **puede** traer métodos con implementación, marcándolos con la palabra `default`. Quien implemente la interfaz los hereda gratis y puede sobrescribirlos si quiere. Se agregaron por una razón muy concreta: permitir que Java sumara métodos a interfaces viejísimas (como `List`) **sin romper** los millones de clases que ya las implementaban. Son una herramienta de compatibilidad, no de diseño.
> *Volvé al camino — para el TPA, pensá las interfaces como contratos puros.*

> 📌 **Para el parcial, si te preguntan: ¿cuándo usar interfaz y cuándo clase abstracta?**
> Interfaz cuando se quiere definir un contrato de comportamiento que clases no emparentadas pueden cumplir; una clase puede implementar varias. Clase abstracta cuando hay estado y comportamiento común que las subclases comparten por herencia; una clase solo puede extender una. La interfaz no tiene estado de instancia; la clase abstracta sí.

---

## 7. Enums 🔴

Un **enum** representa un **conjunto fijo y finito de valores constantes**.

```java
public enum Continente {
    AMERICA,
    ASIA,
    EUROPA,
    AFRICA,
    OCEANIA
}

// Se usa así:
Pais argentina = new Pais("Argentina", Continente.AMERICA);
```

**Para qué sirve:** evitar los `String` sueltos. Si el continente fuera un `String`, nada te impide escribir `"america"`, `"América"` o `"AMÉRICA"` y que sean tres cosas distintas. Con un enum, **el compilador te frena**: `Continente.AMERIKA` no compila. Ganás legibilidad y seguridad.

**Características:**
- **No se instancian.** No existe `new Continente()`.
- **No se agregan valores en ejecución.** Para sumar uno, tocás el código.
- Se comparan con `==` sin problema (son instancias únicas), aunque `.equals()` también funciona.

⚠️ **Una precisión que el material dice mal.** El Video 3 afirma que *"los enums no son clases, son valores fijos"*. **Técnicamente, un enum SÍ es una clase**, y cada constante es una **instancia única** de esa clase. Por eso pueden tener atributos y métodos. No es una distinción de trivia: es lo que habilita lo que sigue.

### 7.1 Enums con comportamiento — y la contradicción del material 🔴

Un enum **puede** tener métodos, y cada constante puede **sobrescribir** ese método. Eso permite reemplazar un `switch` por polimorfismo:

```java
// ❌ SIN enum con comportamiento: el switch
public String colorDe(Dia dia) {
    switch (dia) {
        case VIERNES: return "verde";
        default:      return "blanco";
    }
}
// El problema: si mañana agregás un día, tenés que acordarte de venir a tocar
// ESTE switch. Y el que está en la otra clase. Y el otro. El compilador no te avisa.

// ✅ CON enum con comportamiento: el color viaja con el día
public enum Dia {
    LUNES, MARTES, MIERCOLES, JUEVES,

    VIERNES {
        @Override
        public String getColor() { return "verde"; }   // el viernes es la excepción
    },

    SABADO, DOMINGO;

    public String getColor() { return "blanco"; }      // comportamiento por defecto
}

// Se usa así, sin un solo `if`:
Dia.VIERNES.getColor();   // Resultado esperado: "verde"
Dia.LUNES.getColor();     // Resultado esperado: "blanco"
```

**El `switch` sobre un enum es un *code smell*** — un síntoma de que le estás preguntando al objeto qué es, en vez de pedirle que haga lo suyo. La alternativa polimórfica es la doctrina central del diseño orientado a objetos, y es la que la materia te va a enseñar (patrón **State**, clase 6).

> **⚠️ Contradicción en el material previo — sin resolver.**
> El Video 1 **recomienda** enums con comportamiento, justamente para matar el `switch`. El Video 3 dice, textual: *"los enums pueden tener comportamiento. **No hagan eso.** No se los recomiendo. Después debátanlo con su docente."*
>
> Son **dos ayudantes distintos contradiciéndose** en el mismo material previo. Ninguno de los dos es palabra final: **la última palabra la tienen los profes** (Escobar y Saclier) y todavía no hablaron de este tema.
>
> **La posta técnica es la del Video 1:** reemplazar `switch` por polimorfismo es diseño correcto, y es hacia donde va la materia. Queda pendiente de confirmación con los profes antes del TPA — quien corrige es el que define.

---

## 8. Excepciones 🔴

Una **excepción** es un evento inesperado que interrumpe el flujo normal del programa: un archivo que no está, una entrada inválida, un servicio que no responde.

### 8.1 Las dos familias

🔴 **Esta distinción sí se pregunta.**

| | **Checked** (chequeadas) | **Unchecked** (no chequeadas) |
|---|---|---|
| **Heredan de** | `Exception` | `RuntimeException` |
| **¿El compilador obliga a manejarlas?** | **Sí** | No |
| **Representan** | Problemas del entorno, previsibles y recuperables | **Errores de programación** |
| **Ejemplos** | `FileNotFoundException`, `IOException` | `NullPointerException`, `ArrayIndexOutOfBoundsException` |

**Checked:** el compilador **no te deja compilar** si no las manejás. Tenés dos opciones: declarar que tu método puede lanzarla (`throws`), o atraparla (`try/catch`).

```java
// Opción A — declarar que el método puede lanzarla, y que se arregle quien me llame
public void retirar(Double monto) throws SaldoInsuficienteException {
    if (monto > this.saldo) {
        throw new SaldoInsuficienteException("No te alcanza");
    }
    this.saldo -= monto;
}
```

**Unchecked:** el compilador no dice nada. Son errores de programación — un `null` donde no debía haberlo, un índice fuera de rango. La idea es que **no se atrapan: se arreglan**.

```java
// Una excepción propia, unchecked (hereda de RuntimeException):
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String message) {
        super(message);        // le pasa el mensaje al constructor de RuntimeException
    }
}
```

### 8.2 `try` / `catch` / `finally`

```java
public void comprar() {
    try {
        // código que PUEDE lanzar una SaldoInsuficienteException
        this.cuenta.retirar(this.precio);

    } catch (SaldoInsuficienteException e) {
        // se ejecuta SOLO si la excepción se disparó.
        // Acá decidís qué hacer: loguear, avisar al usuario, reintentar...
        System.out.println("Compra rechazada: " + e.getMessage());

    } finally {
        // se ejecuta SIEMPRE: haya fallado o no. Sirve para limpiar
        // (cerrar un archivo, soltar una conexión).
    }
}

// ¿CÓMO FUNCIONA?
// - Si retirar() sale bien  → corre el try entero, se saltea el catch, corre el finally.
// - Si retirar() explota    → el try se CORTA en esa línea, corre el catch, corre el finally.
// El finally corre en los dos casos. Esa es toda su razón de ser.
```

### 8.3 `Error` no es lo mismo que `Exception` 🟡

Hay una tercera categoría, y es importante no confundirla:

- **`Exception`** → situaciones que el programa **puede** manejar.
- **`Error`** → fallas graves de la JVM, de las que **no se vuelve**. `OutOfMemoryError`, `StackOverflowError` (los dos que vimos en la sección 2).

**No se atrapan.** Si tenés un `StackOverflowError`, no lo manejás: arreglás la recursión.

> 📌 **Para el parcial, si te preguntan: ¿diferencia entre excepción chequeada y no chequeada?**
> Las chequeadas heredan de `Exception` y el compilador obliga a manejarlas, sea declarándolas con `throws` en la firma del método o atrapándolas con `try/catch`; representan condiciones previsibles del entorno. Las no chequeadas heredan de `RuntimeException`, el compilador no exige nada, y representan errores de programación (como un `NullPointerException`). Los `Error` son una categoría aparte: fallas irrecuperables de la JVM, que no se manejan.

---

## 9. Annotations 🟡

Una **annotation** es **metadata**: información que le agregás al código para que la lean el compilador o alguna biblioteca. **No cambia la lógica de tu programa por sí sola** — habilita que otro la cambie.

Se escriben con `@` delante y van pegadas a una clase, un método o un atributo.

Ya usaste tres en este apunte:

```java
@Override    // le dice al COMPILADOR: esto redefine un método del padre.
             // Si te equivocás en el nombre o en la firma, te avisa. Vale oro.

@Getter      // le dice a LOMBOK: generá los getters de esta clase.
@Setter      // ídem con los setters.
```

**Se pueden crear propias.** Al declararlas, se configuran dos cosas:
- **`@Retention`**: hasta cuándo vive la anotación — `SOURCE` (se descarta al compilar), `CLASS` (queda en el bytecode) o `RUNTIME` (accesible mientras el programa corre). La más usada es `RUNTIME`.
- **`@Target`**: a qué se le puede aplicar — `TYPE` (clase o interfaz), `METHOD`, `FIELD` (atributo).

> 🕳️ **Madriguera — las annotations de Spring**
> Más adelante en la materia vas a ver `@Service`, `@RestController`, `@Repository` y muchas más. Pertenecen a **Spring**, un framework que construye y conecta los objetos de tu aplicación por vos: en lugar de que vos hagas `new` de cada pieza y las enchufes a mano, marcás las clases con estas anotaciones y Spring las instancia y las inyecta donde hagan falta. Cada anotación declara **qué rol cumple** esa clase (`@RestController` = recibe pedidos HTTP; `@Service` = tiene la lógica de negocio; `@Repository` = habla con la base de datos), y el framework actúa en consecuencia. Es exactamente el mecanismo que estamos viendo — metadata que otro lee y usa — pero a gran escala.
> *Volvé al camino — Spring lo da el profe en la clase 2, en vivo. Acá solo importa entender qué es una annotation.*

---

## ✅ Checkpoint — Parte 2

Sin mirar el apunte.

1. ¿Por qué se dice que Java es compilado *e* interpretado? ¿Qué produce `javac` exactamente, y quién consume eso?
2. ¿Cómo hace Java para correr en Windows, Linux y Mac sin recompilar? ¿Qué pieza absorbe la diferencia?
3. Escribís `Integer cantidad = null;` y después `cantidad + 1`. ¿Compila? ¿Qué pasa en ejecución? ¿Y si fuera `int`?
4. Declarás un atributo sin escribir ningún modificador de acceso. ¿Es público? ¿Quién puede verlo?
5. ¿Qué error tiene esta afirmación: *"una clase abstracta es una clase que tiene al menos un método abstracto"*?
6. Nombrá **las dos** diferencias entre una interfaz y una clase abstracta.
7. Una superclase tiene un constructor que recibe un `String`. ¿Qué está obligada a hacer la subclase, y por qué?
8. ¿Por qué un `switch` sobre un enum se considera un *code smell*? ¿Con qué se reemplaza?
9. ¿Qué diferencia hay entre una excepción chequeada y una no chequeada? Dame un ejemplo de cada una.
10. ¿Cuándo se ejecuta el bloque `finally`?
11. `StackOverflowError` y `OutOfMemoryError`: ¿de qué zona de memoria viene cada uno, y qué los provoca?
12. Un método `static` no puede acceder directamente a un atributo de instancia. ¿Por qué?

---

## ⚠️ Errores del material previo (Parte 2)

1. **"Una clase abstracta tiene al menos un método abstracto"** (Video 1, 49:57). Falso: puede no tener ninguno. Lo cierto es la inversa. La propia `Persona` del repo de la cátedra lo desmiente.
2. **Los modificadores de acceso: el material enseña tres** (`private`, `protected`, `public`) **y se saltea el `default`** (no escribir nada). Son cuatro. Y `protected` también da acceso al mismo paquete, no solo a las subclases.
3. **"Los enums no son clases"** (Video 3). Sí lo son: cada constante es una instancia única. Es justamente por eso que pueden tener comportamiento.
4. **Contradicción sin resolver: enums con comportamiento.** Video 1 los recomienda, Video 3 los desaconseja. Ninguno de los dos es palabra final: la definen los profes. La posta técnica es la del Video 1.

---

**Lo que viene — Parte 3: Colecciones y Streams.** El corazón del material: `List`, `Set`, `Map`, y la API de Streams (`filter`, `map`, `flatMap`, `collect`). Es lo que más vas a usar, todo el año.

**FIN DE LA PARTE 2**
