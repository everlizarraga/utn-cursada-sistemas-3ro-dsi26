# 📘 APUNTE MAESTRO — Preclase 03 · Parte 4
## Composición: cuándo NO heredar

---

## 🧭 Cómo leer esto

Cierra la unidad. Se apoya en las Partes 1 (Herencia), 2 (Interfaces y polimorfismo) y 3 (Múltiples interfaces), que se dan por vistas. Todo el material está unificado acá.

Es la parte más importante para el parcial: acá se juega el **criterio de diseño**, que es lo que la materia evalúa.

**Leyenda:** 🔴 central/evaluable · 🟡 secundario · 🟢 al pasar · 🕳️ madriguera · 📌 respuesta modelo de parcial.

---

## 1. Lo que falta 🔴

Ya tenés dos herramientas para relacionar clases, y las dos dan polimorfismo: la **herencia** (Parte 1) y las **interfaces** (Parte 2). Queda la pregunta de diseño: **¿cuándo uso cada una?** Y falta una **tercera** relación, distinta de heredar, que muchas veces es la mejor: la **composición**.

---

## 2. Interfaces vs. herencia 🔴

Puestas una al lado de la otra, dicen cosas distintas sobre las clases que relacionan:

| | **Interfaces** | **Herencia** |
|---|---|---|
| **Qué define** | un contrato: *qué se puede hacer* | una relación: *qué es* una cosa |
| **¿Aporta código?** | no (solo firmas) | sí: la subclase reutiliza el del padre |
| **¿Cuántas?** | múltiples (`implements A, B, C`) | una sola clase padre |
| **Estado** | no (a lo sumo constantes) | hereda y agrega atributos |
| **Qué obliga** | a implementar todos sus métodos | a nada; podés heredar y/o redefinir |

En una frase: **una interfaz dice "esto sabe hacer tal cosa"; la herencia dice "esto es un tipo de tal otra cosa".** Esa diferencia guía la elección:

- comportamiento que comparten clases sin otra cosa en común → **interfaz** (como los notificadores de la Parte 2: solo comparten *saber notificar*);
- una clase que es genuinamente un caso particular de otra, con estado y código común → **herencia** (como `DonarVianda`, que *es una* `Donacion`, en la Parte 1).

Pero hay un tercer caso que ninguna de las dos captura: cuando una clase **no es** otra ni comparte un comportamiento con ella, sino que **está armada con** otra. Ahí entra la composición.

---

## 3. Qué es la composición 🔴

**Composición** es una relación donde una clase **tiene** a otra como parte: un objeto contiene a otro y lo usa para trabajar. No hereda de él ni firma un contrato: lo tiene adentro, como atributo.

```
              ┌───────────────────────┐
              │         Auto          │
              ├───────────────────────┤
              │ + motor: Motor        │   ← el Auto TIENE un Motor
              └───────────────────────┘
                         ┊ "tiene"
                         ▽
              ┌───────────────────────┐
              │         Motor         │
              ├───────────────────────┤
              │ + encender()          │
              │ + apagar()            │
              └───────────────────────┘
```

Un `Auto` **tiene un** `Motor`. No *es* un motor: lo **contiene** y le **delega** lo que el motor sabe hacer.

```java
public class Auto {
    private Motor motor;   // composición: el Auto TIENE un Motor

    public void arrancar() {
        motor.encender();  // le DELEGA el trabajo al motor
    }
}
```

> **Delegar:** que un objeto le pase el trabajo a otro que tiene adentro, en vez de hacerlo él. El auto no sabe encender; le pide al motor que se encienda.

### La relación "tiene-un" 🔴

Así como la herencia expresa **"es-un"**, la composición expresa **"tiene-un"**. Esta oposición —*es-un* vs *tiene-un*— es la herramienta para decidir entre heredar y componer.

---

## 4. El criterio: "es-un" vs. "tiene-un" 🔴

Ante dos clases relacionadas, hacé la prueba de la frase:

- si **"X es un Y"** suena verdadero y natural → **herencia**;
- si **"X tiene un Y"** describe mejor la relación → **composición**.

| Frase | ¿Suena bien? | Decisión |
|---|---|---|
| una vianda **es una** donación | ✅ es-un | herencia (Parte 1) |
| un auto **tiene un** motor | ✅ tiene-un | composición |
| un donante **tiene una** forma de donar | ✅ tiene-un | composición |
| un donante **es una** forma de donar | ❌ forzado | *no* herencia |

Esa última fila es la trampa: una situación donde heredar *parece* razonable —compila, funciona— y es un error, porque la frase "es-un" está forzada.

---

## 5. La trampa: heredar cuando había que componer 🔴

Supongamos que queremos que un donante calcule su puntaje según cómo dona. Alguien podría hacer subclases de `Donante`:

```
   ❌ HERENCIA MAL USADA
              ┌───────────────────────────┐
              │          Donante          │
              │ + obtenerPuntaje(): int   │
              └───────────────────────────┘
                          △
              ┌───────────┴───────────┐
   ┌────────────────────┐   ┌─────────────────────┐
   │  DonanteDeDinero   │   │  DonanteDeViandas   │
   │ +obtenerPuntaje()  │   │ +obtenerPuntaje()   │
   └────────────────────┘   └─────────────────────┘
```

Compila. Funciona. Y está **mal**. Los problemas son concretos:

**1 — El donante que dona las dos cosas no entra.** No puede ser `DonanteDeDinero` y `DonanteDeViandas` a la vez (herencia única, Parte 3). Habría que inventar `DonanteDeDineroYViandas`, y con más tipos de donación las combinaciones explotan.

> **Explosión de clases:** cuando modelás cada combinación de características como una clase distinta, la cantidad crece de forma descontrolada.

**2 — El donante que hoy dona dinero y mañana viandas.** Las personas no cambian de clase. Para "convertirlo" habría que crear otro objeto y migrar sus datos. Modelar como *tipo* algo que **cambia con el tiempo** es un error clásico.

**3 — Acoplamiento a una jerarquía rígida.** "Cómo dona" quedó soldado a la identidad del donante. Cambiar las formas de donar obliga a tocar la jerarquía de `Donante`.

La raíz de los tres: **"un donante *es una* forma de donar" es falso.** Donar dinero no es lo que un donante *es* — es lo que *hace*. Y "lo que hace" se modela con composición.

---

## 6. La solución: composición 🔴

Damos vuelta el modelo. El donante **tiene una** forma de donar, descrita por una interfaz `IDonacion` que cada tipo concreto implementa:

```
   ✅ COMPOSICIÓN + INTERFAZ
   ┌───────────────────────────┐        ╔═══════════════════════╗
   │          Donante          │        ║     «interface»       ║
   │ - donacion: IDonacion     │──tiene─▷║      IDonacion        ║
   │ + donar(): void           │        ║ + obtenerPuntaje():int║
   └───────────────────────────┘        ╚═══════════════════════╝
                                                   △
                                    ┌──────────────┴──────────────┐
                         ┌──────────────────┐        ┌──────────────────┐
                         │   DonarDinero    │        │   DonarVianda    │
                         │+obtenerPuntaje() │        │+obtenerPuntaje() │
                         └──────────────────┘        └──────────────────┘
```

```java
public interface IDonacion {
    int obtenerPuntaje();
}
```

```java
public class DonarDinero implements IDonacion {
    private Double monto;
    @Override
    public int obtenerPuntaje() {
        return (int) (monto / 1000);   // su forma propia de puntuar
    }
}
```

```java
public class Donante {
    private String nombre;
    private String apellido;
    private String dni;
    private IDonacion donacion;   // composición: TIENE una forma de donar

    public void donar() {
        // El donante no sabe calcular puntajes; le PIDE a su donación que lo haga.
        int puntaje = donacion.obtenerPuntaje();   // delegación
        System.out.println("Puntaje de la donación: " + puntaje);
    }
}
```

Cada problema de la §5 se disuelve:

| Problema del modelo por herencia | Con composición |
|---|---|
| donante que dona dos cosas | tiene varias `IDonacion`, o cambia según el caso |
| donante que cambia de tipo | un cambio del atributo `donacion` — sigue siendo el mismo objeto |
| agregar un tipo nuevo | una clase nueva que implementa `IDonacion` — **`Donante` no se toca** |
| acoplamiento rígido | "quién es el donante" y "cómo dona" varían por separado |

### 🔴 Por qué `DonarDinero` es una clase concreta y no una interfaz

Un punto que confunde: `DonarDinero` y `DonarVianda` **implementan** `IDonacion`, pero son **clases concretas**, no interfaces. ¿Por qué? Porque tienen que **aportar el código** de `obtenerPuntaje()` —la fórmula real, distinta en cada una—, y una interfaz no tiene cuerpo. La interfaz (`IDonacion`) es el **contrato**; las clases (`DonarDinero`, `DonarVianda`) lo **cumplen** con código real.

> **Regla práctica:** el contrato va como interfaz; la implementación con lógica concreta va como clase. Si algo tiene que *hacer* (calcular, decidir), necesita ser una clase con el código adentro.

### Una aclaración, para que no choque con la Parte 1 🟡

En la Parte 1 modelamos los **tipos de donación** con herencia (`DonarVianda` *es una* `Donacion`). Acá el foco es **otra relación**: la del **donante con su donación** (*tiene una*). Son ejes distintos, y los dos conviven: en un mismo sistema, la herencia sirve para una cosa y la composición para otra. Lo que decide, siempre, es qué frase —*es-un* o *tiene-un*— es honesta.

---

## 7. La regla, y su matiz 🔴

Existe una guía muy citada:

> **"Favorecer la composición por sobre la herencia."**

Significa: ante la duda, empezá considerando la composición, porque acopla menos y es más flexible. La herencia ata fuerte (un solo padre, jerarquía rígida, todo lo del padre viene incluido); la composición ata flojo (cambiás la parte sin tocar el todo).

**Pero no es un dogma.** La herencia **no es mala**: es correcta cuando el "es-un" es genuino. En el propio sistema:

- `DonarVianda` **es una** `Donacion` → herencia **correcta** (Parte 1).
- un `Donante` **es una** forma de donar → herencia **incorrecta** (§5).

**La misma herramienta es correcta en un caso e incorrecta en el otro.** No decide la herramienta: decide si la frase "es-un" es honesta.

> 🕳️ **Madriguera — patrón Strategy.** Darle al `Donante` una `IDonacion` intercambiable a la que le delega es la semilla de un patrón de diseño llamado **Strategy**: encapsular una familia de comportamientos intercambiables detrás de una interfaz, para cambiarlos sin tocar quien los usa. Se ve más adelante.

---

## 📌 Para el parcial, si te preguntan

Esta es la sección clave de la unidad. La materia no evalúa que recites definiciones: evalúa que **justifiques una decisión de diseño**. El molde de una buena justificación es siempre el mismo:

> **"Elijo [X] porque priorizo [atributo de calidad], y pago con [costo]."**

**▸ ¿Por qué modelás la donación con composición y no con herencia del donante?**
Elijo composición —el donante tiene una `IDonacion`— porque priorizo la **flexibilidad** y el **bajo acoplamiento**: separo "quién es el donante" de "cómo dona", así un donante puede cambiar su forma de donar, combinar varias, y agregar un tipo nuevo no obliga a tocar `Donante`. Con herencia del donante tendría explosión de clases y una jerarquía rígida. Pago con una indirección más (un objeto extra al que delegar) y algo más de código inicial, que acá vale la pena.

**▸ ¿Por qué `DonarVianda` sí hereda de `Donacion`?**
Porque el "es-un" es genuino: una vianda *es* una donación, comparte todo su estado y solo difiere en cómo calcula el puntaje. Priorizo la **reutilización** de ese estado y comportamiento común; el costo —quedar atada a la jerarquía— es aceptable porque una vianda nunca deja de ser una donación. Forzar composición acá agregaría complejidad sin beneficio.

**▸ ¿Diferencia entre "es-un" y "tiene-un", y para qué sirve?**
"Es-un" es herencia (la subclase es un caso particular de la superclase); "tiene-un" es composición (una clase contiene a otra y le delega). Sirve como criterio: si "X es un Y" es honesto, se hereda; si lo natural es "X tiene un Y", se compone. Modelar con herencia una relación "tiene-un" lleva a jerarquías rígidas y explosión de clases.

**▸ ¿"Favorecer composición sobre herencia" es una regla absoluta?**
No. Es una guía: ante la duda, la composición acopla menos y es más flexible. Pero la herencia es correcta cuando el "es-un" es genuino y hay estado y comportamiento común real para reutilizar. La herramienta no es buena ni mala; decide si la relación que modela es honesta.

---

## ✅ Checkpoint — Parte 4

Respondelas **sin mirar el apunte**.

1. Nombrá una diferencia entre interfaces y herencia en: qué definen, cuántas se pueden, si aportan código. *(§2)*
2. ¿Qué relación expresa la composición? Dá un ejemplo. *(§3)*
3. ¿Qué es delegar? ¿Cómo se relaciona con la composición? *(§3)*
4. Escribí la "prueba de la frase" para decidir entre heredar y componer. *(§4)*
5. En el modelo malo, ¿qué pasa con un donante que dona dinero y viandas? ¿Cómo se llama el problema de cubrir todas las combinaciones con clases? *(§5)*
6. ¿Por qué "un donante es una forma de donar" es forzado y "tiene una forma de donar" no? *(§5)*
7. En el modelo por composición, ¿qué se toca para agregar un tipo nuevo de donación? ¿Qué no? *(§6)*
8. ¿Por qué `DonarDinero` es una clase concreta y no una interfaz? *(§6)*
9. `DonarVianda extends Donacion` es correcto, pero `DonanteDeDinero extends Donante` no. Las dos son herencia. ¿Qué las distingue? *(§7)*
10. Completá para la decisión de componer el donante con su donación: "Elijo ___ porque priorizo ___, y pago con ___". *(§7, 📌)*

*(Las respuestas van en el complemento de la unidad.)*

---

## Fin de la unidad — Preclase 03

Estas cuatro partes cubren todo el material de la preclase:

- **Parte 1** — Herencia.
- **Parte 2** — Interfaces y polimorfismo.
- **Parte 3** — Múltiples interfaces y el contrato roto (el antipatrón `Object` + casts).
- **Parte 4** — Composición: cuándo NO heredar.

El hilo completo: hay tres formas de relacionar clases —**es-un** (herencia), **sabe hacer** (interfaz) y **tiene-un** (composición)—, dos de ellas dan polimorfismo, y elegir bien entre ellas *es* el diseño. La brújula que se llevan: **antes de heredar, preguntá si "es-un" es honesto; si tenés que forzarlo, componé.**

Lo que sigue de la clase 3 es otro material —**cualidades de diseño y atributos de calidad**, y el caso **SmartLife**—, que se trabaja aparte.

---

**FIN DE LA PARTE 4 — Composición: cuándo NO heredar**
