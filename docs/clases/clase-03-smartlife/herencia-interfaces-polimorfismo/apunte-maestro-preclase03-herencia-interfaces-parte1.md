# 📘 APUNTE MAESTRO — Preclase 03 · Parte 1
## Herencia

---

## 🧭 Cómo leer esto

Este apunte unifica el material de la preclase en un solo texto: no manda a ver ningún video ni abrir ningún repo, todo lo necesario está acá. Se apoya en lo ya visto en las preclases anteriores (POO y Java: clases, objetos, atributos, métodos, constructores, encapsulamiento), que se da por sabido y no se vuelve a explicar.

Cada bloque de código está comentado, con su **resultado esperado como comentario**.

**Leyenda:** 🔴 central/evaluable · 🟡 secundario · 🟢 al pasar · 🕳️ madriguera (tangente que no seguimos hoy) · 📌 respuesta modelo de parcial.

---

## 1. El problema que resuelve la herencia 🔴

Cuando varias clases comparten casi todo y difieren en poco, escribirlas por separado obliga a **repetir** lo común en cada una. Y el código repetido es frágil: el día que hay que cambiar algo compartido, hay que acordarse de cambiarlo en todos lados.

La **herencia** resuelve esto: se escribe lo común **una sola vez** en una clase "padre", y cada caso específico la hereda y le agrega lo suyo.

---

## 2. Qué es la herencia 🔴

**Herencia** es una relación entre dos clases donde una —la **subclase** (o clase hija)— recibe automáticamente los atributos y métodos de otra —la **superclase** (o clase padre)— y puede sumar los propios.

```
                    ┌───────────────────────┐
                    │        Animal         │   ← superclase: lo común
                    ├───────────────────────┤
                    │ + comer()             │
                    │ + dormir()            │
                    └───────────────────────┘
                               △
                               │  "hereda"
              ┌────────────────┼────────────────┐
              │                │                │
   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
   │     Perro    │  │     Gato     │  │    Pájaro    │
   ├──────────────┤  ├──────────────┤  ├──────────────┤
   │ + ladrar()   │  │ + maullar()  │  │ + volar()    │   ← lo propio
   │ + moverCola()│  │ + arañar()   │  │ + cantar()   │     de cada una
   └──────────────┘  └──────────────┘  └──────────────┘
```

`Perro`, `Gato` y `Pájaro` **heredan** `comer()` y `dormir()` de `Animal` —no hay que reescribirlos— y cada uno **agrega** lo suyo.

> **La flecha en UML:** la herencia (llamada *generalización*) se dibuja con una **línea llena** rematada en un **triángulo hueco** que apunta al padre.

### La relación "es-un" 🔴

La herencia expresa una relación **"es-un"**: un `Perro` **es un** `Animal`. Esta frase es la prueba que se usa para decidir si la herencia corresponde: si podés decir con honestidad "X **es un** Y", la herencia tiene sentido. Si tenés que forzarla, probablemente sea un error de diseño (esto se retoma en la Parte 4, con la composición).

---

## 3. Extender vs. redefinir 🔴

Cuando una subclase hereda un método del padre, tiene dos caminos con él.

**Extender** — dejar el método del padre como está y **sumarle** métodos nuevos. La subclase conserva todo lo heredado y agrega lo propio.

**Redefinir** — **reemplazar** un método heredado por una versión propia, porque la del padre no sirve para este caso. Mismo nombre y firma, distinto comportamiento.

Se ve claro con un ejemplo mínimo. El padre define un sonido genérico:

```java
public class Animal {
    private String nombre;

    public Animal(String nombre) {
        this.nombre = nombre;
    }

    public void hacerSonido() {
        System.out.println("Hacer sonido");
    }
}
```

Un `Perro` **redefine** `hacerSonido`, porque no hace un sonido genérico, hace "Guau!":

```java
public class Perro extends Animal {

    public Perro(String nombre) {
        super(nombre);   // (super se explica en la sección 4)
    }

    @Override
    public void hacerSonido() {
        System.out.println("Guau!");   // versión propia del perro
    }
}

// ¿CÓMO FUNCIONA?
//     new Animal("bicho").hacerSonido();   // imprime: Hacer sonido
//     new Perro("Firulais").hacerSonido(); // imprime: Guau!
//
// Aunque el Perro también ES-UN Animal, al pedirle hacerSonido() se ejecuta
// SU versión. La subclase gana sobre el padre.
```

Y si en vez de reemplazar del todo querés **agregarle** algo al comportamiento del padre, lo llamás con `super.` y después sumás lo tuyo:

```java
@Override
public void hacerSonido() {
    super.hacerSonido();          // primero lo del padre: "Hacer sonido"
    System.out.println("Guau!");  // y le agrega lo suyo
}
// Ahora imprime las dos líneas. Eso es EXTENDER el comportamiento, no reemplazarlo.
```

---

## 4. Las herramientas de la herencia 🔴

### `extends`

Declara la relación: `class Perro extends Animal` = "Perro es un Animal y hereda todo lo suyo". Desde ahí, la subclase tiene los atributos y métodos del padre sin escribir una línea.

### `@Override`

Marca que un método **redefine** uno heredado. Si te equivocás en el nombre o los parámetros —creyendo que redefinís cuando en realidad creás un método nuevo— el compilador te frena. Es opcional, pero conviene ponerla siempre en cada método que redefinas.

### `super`: dos usos 🔴

`super` referencia al padre, y se usa de dos formas que conviene no mezclar.

**(a) `super(...)` — llama al constructor del padre.** Al crear un objeto de la subclase, la parte heredada también necesita inicializarse. `super(...)` invoca el constructor del padre y le pasa lo que necesita.
**Regla dura:** si el padre tiene constructor con parámetros, `super(...)` **debe ser la primera línea** del constructor de la hija. El padre se inicializa antes que la hija.

**(b) `super.metodo()` — llama al método del padre desde la hija.** Lo viste recién con `super.hacerSonido()`: cuando redefinís pero querés aprovechar la versión del padre en lugar de descartarla.

### La herencia es única 🔴

En Java, **una clase hereda de una sola clase padre.** `class Hija extends PadreA, PadreB` no existe. *(Por qué la restricción, y en qué se diferencia de las interfaces, se ve en la Parte 3.)*

---

## 5. El caso completo: puntaje de donaciones 🔴

Modelamos las **donaciones** de un sistema. Una donación tiene fecha, el donante que la hizo y una cantidad, y suma un **puntaje**. Pero no todas puntúan igual:

- una **donación de dinero** tiene además un **monto**;
- una **donación de viandas** tiene un **tipo de comida** y puede estar **vencida** — y si lo está, no debería sumar puntaje.

Lo común (fecha, donante, cantidad, la idea de puntaje) va en la superclase; lo propio, en cada subclase.

### El donante

```java
package ar.edu.utn.frba.dds.model.entities;

public class Donante {
    private String nombre;
    private String apellido;
    private String dni;

    public Donante(String nombre, String apellido, String dni) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
    }
}
```

### La superclase: `Donacion`

Acá vive lo común a cualquier donación:

```java
package ar.edu.utn.frba.dds.model.entities.donacion;

import ar.edu.utn.frba.dds.model.entities.Donante;
import java.time.LocalDate;

public class Donacion {
    private LocalDate fecha;
    private Donante donante;
    private int cantidad;

    public Donacion(LocalDate fecha, Donante donante, int cantidad) {
        this.fecha = fecha;
        this.donante = donante;
        this.cantidad = cantidad;
    }

    // El puntaje POR DEFECTO de una donación es su cantidad.
    // Las subclases que necesiten otra cosa lo van a REDEFINIR.
    public int obtenerPuntaje() {
        return cantidad;
    }

    // Getters: como los atributos son private, las subclases no pueden
    // acceder a "fecha" ni "cantidad" directamente; los leen por acá.
    public LocalDate getFecha() {
        return fecha;
    }

    public int getCantidad() {
        return cantidad;
    }
}
```

🔴 **Un punto sobre `private` y herencia.** Un atributo `private` del padre **se hereda** (existe en la hija), pero la hija **no puede tocarlo directamente**: la privacidad se respeta incluso entre padre e hija. Por eso las subclases van a usar `getCantidad()` y `getFecha()` en lugar de `cantidad` y `fecha`. Heredar un atributo no es lo mismo que poder accederlo libremente.

### Subclase 1: `DonarDinero` — hereda sin redefinir

```java
package ar.edu.utn.frba.dds.model.entities.donacion;

import ar.edu.utn.frba.dds.model.entities.Donante;
import java.time.LocalDate;

public class DonarDinero extends Donacion {   // ES-UNA Donacion
    private Double monto;   // lo propio de una donación de dinero

    public DonarDinero(LocalDate fecha, Donante donante, int cantidad, Double monto) {
        super(fecha, donante, cantidad);   // inicializa la parte Donacion. PRIMERA LÍNEA.
        this.monto = monto;
    }

    // 👀 NO hay obtenerPuntaje() acá: DonarDinero NO lo redefine.
    //    Hereda el del padre tal cual → return cantidad.
}
```

### Subclase 2: `DonarVianda` — redefine el puntaje

```java
package ar.edu.utn.frba.dds.model.entities.donacion;

import ar.edu.utn.frba.dds.model.entities.Donante;
import java.time.LocalDate;

public class DonarVianda extends Donacion {   // ES-UNA Donacion
    private String tipoComida;

    // Método propio: ¿está vencida? Usa getFecha() (no "fecha") porque el
    // atributo del padre es private.
    public boolean estaVencida() {
        return getFecha().isBefore(LocalDate.now());
    }

    public DonarVianda(LocalDate fecha, Donante donante, int cantidad, String tipoComida) {
        super(fecha, donante, cantidad);
        this.tipoComida = tipoComida;
    }

    // 👇 ACÁ SÍ redefinimos: una vianda no puntúa como una donación común.
    @Override
    public int obtenerPuntaje() {
        if (estaVencida()) {
            return 0;               // vencida → no suma
        }
        return getCantidad() * 3;   // en buen estado → cantidad por 3
    }
}

// LA JERARQUÍA, DE UN VISTAZO:
//   Donacion     → obtenerPuntaje() devuelve cantidad   (el default)
//   DonarDinero  → NO redefine → hereda el default → cantidad
//   DonarVianda  → REDEFINE    → 0 si vencida, si no cantidad*3
```

### El diagrama de la jerarquía

```
              ┌─────────────────────────────────┐
              │            Donacion             │
              ├─────────────────────────────────┤
              │ - fecha: LocalDate              │
              │ - donante: Donante              │
              │ - cantidad: int                 │
              ├─────────────────────────────────┤
              │ + obtenerPuntaje(): int         │
              └─────────────────────────────────┘
                             △
                             │ (línea llena, triángulo hueco = herencia)
              ┌──────────────┴──────────────┐
   ┌──────────────────────┐   ┌──────────────────────────┐
   │     DonarDinero      │   │       DonarVianda        │
   ├──────────────────────┤   ├──────────────────────────┤
   │ - monto: Double      │   │ - tipoComida: String     │
   ├──────────────────────┤   ├──────────────────────────┤
   │ (no redefine nada)   │   │ + obtenerPuntaje(): int  │  ← redefinido
   │                      │   │ + estaVencida(): boolean │  ← propio
   └──────────────────────┘   └──────────────────────────┘
```

En UML, un método heredado que **no** se redefine no se vuelve a listar en la subclase: por eso `obtenerPuntaje()` aparece en `DonarVianda` (lo redefine) y **no** en `DonarDinero`. El diagrama, leído bien, ya dice quién redefine y quién no.

### Poniéndolo a correr

```java
import ar.edu.utn.frba.dds.model.entities.Donante;
import ar.edu.utn.frba.dds.model.entities.donacion.Donacion;
import ar.edu.utn.frba.dds.model.entities.donacion.DonarDinero;
import ar.edu.utn.frba.dds.model.entities.donacion.DonarVianda;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        Donante donante = new Donante("Juan", "Swift", "44678356");

        // Una lista del tipo del PADRE (List<Donacion>) con objetos de las dos
        // subclases adentro. Java lo permite porque cada subclase ES-UNA Donacion.
        List<Donacion> donaciones = List.of(
                new DonarDinero(LocalDate.now(), donante, 1, 5000.0),
                new DonarVianda(LocalDate.now(), donante, 3, "Arroz"),
                new DonarVianda(LocalDate.of(2023, 3, 4), donante, 7, "Fideos")
        );

        int puntajeTotal = 0;

        for (Donacion d : donaciones) {
            int puntaje = d.obtenerPuntaje();
            puntajeTotal += puntaje;
            // getClass().getSimpleName() devuelve el nombre de la clase real:
            // "DonarDinero" o "DonarVianda".
            System.out.println(d.getClass().getSimpleName() + " -> Puntaje: " + puntaje);
        }

        System.out.println("Puntaje total: " + puntajeTotal);
    }
}
```

### La salida, explicada 🔴

```
DonarDinero -> Puntaje: 1
DonarVianda -> Puntaje: 9
DonarVianda -> Puntaje: 0
Puntaje total: 10
```

| Donación | Tipo | ¿Redefine? | Cuenta | Puntaje |
|---|---|---|---|---|
| dinero, cantidad 1, monto 5000 | `DonarDinero` | no | hereda `return cantidad` → 1 | **1** |
| vianda "Arroz", cantidad 3, hoy | `DonarVianda` | sí | no vencida → `3 * 3` | **9** |
| vianda "Fideos", cantidad 7, año 2023 | `DonarVianda` | sí | vencida (2023 < hoy) → 0 | **0** |
| | | | **Total** | **10** |

Una sola línea, `d.obtenerPuntaje()`, produjo tres cálculos distintos: el objeto real decidió cuál correr. *(A esto —un mismo llamado que se resuelve distinto según el objeto— se lo llama **polimorfismo**, y se ve a fondo en la Parte 2. Acá lo lográs por herencia; ahí, por interfaces.)*

### 🔴 Heredar sin redefinir: el monto que no cuenta

Mirá la primera donación: es de **dinero**, con **monto 5000**, y sacó **1 punto**. Donaste $5000 y sumaste un punto.

¿Por qué? Porque `DonarDinero` **no redefine** `obtenerPuntaje()`: hereda el del padre, que devuelve `cantidad` (vale 1). **El `monto` no participa del puntaje, porque nada lo hizo participar.**

Este es *el* punto de la sección, hecho visible: **cuando una subclase no redefine un método, se queda con el comportamiento del padre, exactamente como está.** No redefinir no es "no hacer nada": es *elegir* quedarse con lo del padre. Si quisiéramos que el monto influyera, tendríamos que redefinir `obtenerPuntaje()` en `DonarDinero`, igual que se hizo en `DonarVianda`.

---

## 📌 Para el parcial, si te preguntan

**▸ ¿Qué es la herencia y qué relación expresa?**
Es una relación entre clases donde una subclase recibe automáticamente los atributos y métodos de una superclase y puede agregar los propios. Expresa una relación "es-un": la subclase es un caso particular de la superclase, como `DonarVianda` es una `Donacion`.

**▸ ¿Diferencia entre extender y redefinir un método?**
Extender es mantener el método heredado y sumarle métodos nuevos a la subclase. Redefinir es reemplazar un método heredado por una versión propia —mismo nombre y firma, distinto comportamiento— porque la del padre no se ajusta a ese caso. Se marca con `@Override`.

**▸ ¿Para qué sirve `super`?**
Tiene dos usos. `super(...)` llama al constructor del padre para inicializar su parte del objeto, y debe ser la primera línea del constructor de la hija. `super.metodo()` llama a la versión del padre desde un método redefinido, para reutilizarla en lugar de descartarla.

**▸ Si una subclase no redefine un método heredado, ¿qué comportamiento tiene?**
El de la superclase, tal cual. No redefinir es una decisión: la subclase se queda con la implementación del padre. Por eso `DonarDinero`, que no redefine `obtenerPuntaje()`, puntúa con la fórmula heredada de `Donacion`.

---

## ✅ Checkpoint — Parte 1

Respondelas **sin mirar el apunte**.

1. ¿Qué gana el diseño al poner lo común en una superclase en vez de repetirlo? *(§1)*
2. ¿Qué relación en lenguaje natural expresa la herencia? Dá un ejemplo del caso de donaciones. *(§2)*
3. En UML, ¿cómo se dibuja la herencia? *(§2)*
4. Diferencia entre **extender** y **redefinir** un método. *(§3)*
5. Los dos usos de `super`. ¿Cuál va en la primera línea del constructor, y por qué? *(§4)*
6. ¿Por qué `DonarVianda` usa `getFecha()` y `getCantidad()` en vez de `fecha` y `cantidad`? *(§5)*
7. `DonarDinero` da puntaje 1 aunque el monto sea 5000. ¿Por qué? *(§5)*
8. La vianda del año 2023 da 0. ¿Qué parte del código produce ese 0? *(§5)*
9. `d.obtenerPuntaje()` es una línea pero da tres resultados. ¿Quién decide cuál corre, y cuándo? *(§5)*
10. ¿Qué hace `DonarDinero` con `obtenerPuntaje()` si no lo escribe? *(§3, §5)*

*(Las respuestas van en el complemento de la unidad.)*

---

## Lo que viene en la Parte 2

La herencia te dio una forma de que un mismo llamado se resuelva distinto según el objeto. La Parte 2 llega al mismo resultado por otro camino —las **interfaces**— y le pone nombre formal a lo que acá viste funcionar: el **polimorfismo**. Después, en las Partes 3 y 4, se cruzan las dos herramientas y aparece la pregunta de diseño: cuándo heredar, cuándo usar interfaces, y cuándo **componer**.

---

**FIN DE LA PARTE 1 — Herencia**
