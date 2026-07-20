# Apunte Maestro — Preclase 03 · Parte 2: Cualidades de Diseño

**Dónde estamos.** Segunda de tres partes. La Parte 1 cubrió los **atributos de calidad** (ISO 25010): el "qué" querés lograr (mantenibilidad, seguridad, disponibilidad…). Esta parte cubre las **cualidades de diseño**: el "cómo" lo lográs. Son las herramientas concretas —acoplamiento, cohesión, simplicidad, robustez, flexibilidad— con las que vas a **tomar y justificar decisiones de diseño durante todo el primer cuatrimestre**. La Parte 3 cierra con **patrones de diseño**.

**El puente con la Parte 1.** ¿Te acordás que la mantenibilidad era el atributo más importante del arranque del año, y que se apoyaba en *modularidad*? Bueno: **modularidad = bajo acoplamiento + alta cohesión**. Acá desarmamos esas piezas. Y "Robustez", que en la Parte 1 dejamos afuera de los atributos ISO, encuentra su lugar acá.

**De dónde venís.** De Paradigmas ya conocés dos de estas cualidades (acoplamiento y cohesión). Se retoman completas, no se dan por sabidas.

---

## 0. Qué son y para qué sirven

Cuando diseñás, casi nunca hay **una** solución: hay varias propuestas posibles, y tenés que elegir. Las **cualidades de diseño son los criterios con los que comparás esas alternativas** y tomás una decisión más formada (menos "me parece", más "por esto").

Dos aclaraciones desde el arranque:

- **No son los únicos criterios.** La experiencia y el conocimiento de quien diseña también pesan. Las cualidades te dan un lenguaje común para justificar, pero no reemplazan el criterio.
- **Son criterios de diseño, se ven en el papel.** A diferencia de varios atributos de la Parte 1 (disponibilidad, performance), varias de estas cualidades —sobre todo acoplamiento y cohesión— **sí se pueden evaluar mirando un diagrama de clases**. Por eso son las que más vas a usar para justificar en objetos.

El mapa de esta parte:

```
                    ┌──────────────────────┐
                    │  Cualidades de       │
                    │      Diseño          │
                    └──────────┬───────────┘
      ┌───────────────┬────────┴───────┬───────────────┬──────────────┐
      │               │                │               │              │
┌─────▼──────┐ ┌──────▼─────┐ ┌────────▼─────┐ ┌───────▼──────┐ ┌─────▼────┐
│Simplicidad │ │ Robustez   │ │ Flexibilidad │ │ Acoplamiento │ │ Cohesión │
│ 🔴         │ │ 🟡         │ │ 🔴          │ │ 🔴           │ │ 🔴       │
│ KISS·YAGNI │ │            │ │      │       │ │              │ │          │
└────────────┘ └────────────┘ └──────┴────────┐└──────────────┘ └──────────┘
                              ┌──────▼─────┐ ┌▼──────────────┐
                              │Extensibilid│ │ Mantenibilidad│
                              └────────────┘ └───────────────┘
```

---

## 1. 🔴 Acoplamiento

**Caso primero.** Tenés dos clases, `A` y `B`, donde `A` usa a `B`. Cambiás **una línea** de un método de `A`… y se te rompe toda `B`, tenés que ir a tocarla también. Eso es un síntoma de que `A` y `B` están **demasiado acopladas**: se conocen de más.

**Definición.** El acoplamiento es el **grado de dependencia entre dos componentes**; es decir, el **grado de conocimiento que un componente tiene sobre el otro**.

Un detalle de vocabulario: decimos **componente** (no "clase") a propósito, para generalizar. Un componente puede ser una clase, un conjunto de clases, un sistema entero. La idea de acoplamiento aplica en todos esos niveles.

**La regla:** cuanto **más** conoce un componente al otro, **más acoplados** están, y **más se propagan** los cambios y errores de uno hacia el otro. Eso es **malo**.

Llevado a objetos: si `A` conoce los detalles internos de `B`, cualquier cambio en `B` puede obligarte a tocar `A`. Si en cambio `A` solo conoce la **interfaz** de `B` (el contrato, no las tripas), podés cambiar el interior de `B` sin que `A` se entere. Por eso el bajo acoplamiento es lo deseable.

### Qué ganás si minimizás el acoplamiento

- **Mejorás la mantenibilidad** — un cambio en un componente no se propaga a los demás.
- **Aumentás la reutilización** — un componente bien desacoplado lo podés usar en este proyecto y en otro distinto. Dos clases fuertemente acopladas no te sirven "sueltas".
- **Mejorás la analizabilidad** — si algo falla en facturación, buscás el problema **en facturación**, no en las otras 500 clases. Sabés dónde mirar.
- **Mejorás la testeabilidad** — si cada unidad está separada, armás casos de prueba unitarios sin tener que instanciar 100 cosas para probar una sola.

### ⚠️ Siempre hay un mínimo de acoplamiento

Si dos componentes **trabajan juntos**, va a existir *algún* acoplamiento — es inevitable y no está mal. Acoplamiento **nulo** solo puede existir entre dos componentes que **nunca** se relacionan por nada. Lo que buscamos no es "cero acoplamiento", sino el **mínimo necesario** y **lo más prolijo posible**: que cada componente conozca lo mínimo indispensable del otro.

> 📌 **Para el parcial / TP (regla de modelado):** en un diagrama de clases que presentes, **no te deberían quedar dos clases "sueltas"** (sin ninguna relación con el resto). Toda clase del modelo tiene que relacionarse, mínimamente, con alguna otra — porque necesitás ese mínimo grado de acoplamiento. Dos clases totalmente sueltas serían, en la práctica, dos sistemas independientes que no tienen nada que ver entre sí.

### Cómo se ve, y una técnica que vas a usar mucho

El acoplamiento se representa con una **relación de dependencia** (una flecha punteada: "`A` usa/conoce a `B`"):

```
   ┌─────┐  usa/conoce   ┌─────┐
   │  A  │ - - - - - - ->│  B  │      ← A depende de B (acoplamiento directo)
   └─────┘               └─────┘
```

Una forma concreta de **bajar** el acoplamiento —que vas a ver en muchísimos ejercicios de acá en adelante— es **diseñar orientado a interfaces**: hacer que dos clases concretas **no se conozcan entre sí**, sino que ambas conozcan solo una **interfaz** intermedia.

```
   ┌─────┐   ┌───────────────┐   ┌─────┐
   │  A  │ - >│ «interface» I │<- │  B  │   ← A y B no se conocen entre sí,
   └─────┘   └───────────────┘   └─────┘      solo conocen el contrato I
```

> 🕳️ **Madriguera — "orientar a interfaces".** Es la base de varias soluciones de desacoplamiento y de casi todos los patrones de la Parte 3. Por ahora quedate con la idea: *depender del contrato, no de la implementación concreta*. Se trabaja a fondo con los ejercicios que vienen. *Volvé al camino.*

---

## 2. 🔴 Cohesión

**Definición.** La cohesión es la **cantidad de responsabilidades asignadas a un componente**. Un componente cohesivo tiene todos sus elementos **abocados a resolver el mismo problema**. La regla: **cuantas más responsabilidades tenga un componente, menos cohesivo será.**

Llevado a objetos: una clase cohesiva tiene responsabilidades **claras, específicas y, en lo posible, un fin común** — hace algo particular y bien, no "un poco de todo". El anti-ejemplo es la **god class** (clase-dios): esa clase de miles de líneas que hace de todo, con funcionalidades que ni siquiera le corresponden, y de la que ya nadie sabe bien qué hace. Eso es **baja cohesión**.

Una ayuda para lograr alta cohesión: **buenas abstracciones**. Buscar las entidades justas y necesarias, para poder repartir las responsabilidades donde corresponden y no sobrecargar a una sola clase con cosas ajenas.

### 🔴 El lema (grabátelo)

> **Alta cohesión, bajo acoplamiento.**

Es la brújula del diseño en objetos durante todo el año: buscamos que cada componente **haga algo muy específico y solo eso** (alta cohesión) y que **conozca lo mínimo de los demás** (bajo acoplamiento). No figura tal cual en el catálogo de cualidades, pero es la síntesis operativa de las dos — anotalo aparte.

### ⚠️ No siempre van juntas (esto se pregunta)

Es tentador pensar que alta cohesión y bajo acoplamiento vienen siempre de la mano. **No.** Podés tener una sin la otra:

- **God class** → **baja** cohesión (hace de todo) pero puede tener **bajo** acoplamiento (no conoce a nadie más, se las arregla sola).
- **Clase muy cohesiva** (hace una sola cosa, bien) pero con **alto** acoplamiento (si la modificás, tenés que tocar sí o sí la otra clase de la que depende).

Son dos ejes **independientes**. Por eso se evalúan por separado: *acoplamiento* = nivel de conocimiento entre componentes; *cohesión* = grado de responsabilidad de un componente.

---

## 3. 🔴 Simplicidad

La simplicidad se apoya en dos principios que vas a nombrar constantemente:

### KISS — *Keep it simple, stupid*

**Evitá cualquier complejidad innecesaria.** No generes complejidad donde no la hay: si algo se puede resolver simple, resolvelo simple. No te la rebusques ni la compliques de gusto.

### YAGNI — *You aren't gonna need it*

**No agregues funcionalidad nueva que no apunte a la problemática actual.** Dicho crudo: *no pongas nada que nadie te pidió*. ¿Por qué? Porque gastás tiempo de más en algo que quizás nunca se use (y que nadie te paga). Lo máximo que sí conviene es **diseñar pensando en la extensibilidad** —dejar la puerta abierta para agregar cosas a futuro— pero **sin agregarlas ahora**.

> **La diferencia fina, en una frase:** *diseñá para que sea fácil extender mañana, pero no construyas hoy lo de mañana.* Preparar el terreno (extensibilidad) ≠ implementar de más (violar YAGNI).

---

## 4. 🟡 Robustez

**Definición.** Ante un **uso inadecuado** (del usuario o de sistemas externos) o ante **fallas internas**, un sistema robusto:

- **No** genera información ni comportamiento inconsistente o errático.
- **Reporta** los errores y **vuelve a un estado consistente**.
- **Facilita** al máximo la detección de la **causa** del problema.

**Cómo se aterriza esto:**

- **Volver a estado consistente** = si una operación falla a mitad de camino, no dejás datos guardados "por la mitad". O se completa entera, o vuelve como estaba.
- **Facilitar la detección de la causa** = mecanismos de **logs**. Cuando algo falla, que quede registro de qué, dónde y por qué. (Se conecta con la *analizabilidad* de la Parte 1.)
- **No dejar errores crudos ni genéricos** = ni un "ocurrió un error" pelado que no le dice nada al usuario, ni una excepción cruda que llega hasta la interfaz. Colaborá con el usuario: si está por cometer un error de ingreso, avisale y guialo.
- **Doble validación** = validás del lado de la interfaz **y** del lado del servidor. La del cliente mejora la experiencia; la del servidor es la que te protege de verdad.

> ⚠️ **Robustez ↔ Madurez (el cruce de la Parte 1).** Acá "Robustez" aparece como **cualidad de diseño**. En la Parte 1, el mismo concepto (resistir y tolerar fallos, no romperse ante un input malo) caía bajo el **atributo** de Fiabilidad, como **Madurez** / Tolerancia a fallos. **No es contradicción: es el mismo fenómeno visto desde dos clasificaciones distintas.** Si te piden el *atributo de calidad*, decí Madurez; si estás hablando de *cualidades de diseño*, decí Robustez. Y recordá el error clásico: en un caso de "HTTP 500 por input inválido", el atributo es **Madurez**, no Robustez.

---

## 5. 🔴 Flexibilidad

**Definición.** Es la capacidad de **reflejar cambios en el dominio de manera simple y sencilla**. Se abre en dos:

- **Extensibilidad** — capacidad de **agregar** nuevas características con "poco" impacto.
- **Mantenibilidad** — capacidad de **modificar** las características existentes con "el menor" esfuerzo posible.

**El puente con la Parte 1.** Esto es lo mismo que vimos como atributo de mantenibilidad, reordenado:

| Tipo de cambio (Parte 1) | Cualidad de diseño (acá) |
|---|---|
| Evolutivo (agregar, crecer) | **Extensibilidad** |
| Correctivo / Perfectivo (arreglar / mejorar) | **Mantenibilidad** |

De estas dos, **extensibilidad** es la que más vas a nombrar: la justificación típica es *"quiero que mi diseño sea fácilmente extensible para poder agregar nuevos casos a futuro con poco impacto"*.

---

## 6. Las cualidades entran en conflicto (repaso del trade-off)

Igual que con los atributos: **no podés maximizar todas las cualidades a la vez**. Siempre que empujás una, hay otra que se te contrapone. Diseñar es **elegir qué priorizás y con qué lo pagás**.

Un matiz que conviene tener claro, porque parece contradictorio y no lo es:

- **Simplicidad y mantenibilidad van muy de la mano** — un diseño simple suele ser más fácil de mantener.
- **La tensión real es simplicidad ↔ extensibilidad** — a veces, para dejar algo extensible, agregás estructura que lo hace un poco menos simple. Pero *depende de cómo plantees la extensibilidad*: bien hecha, no te complica de más. Mal hecha (agregando abstracción "por las dudas"), viola YAGNI y KISS.

---

## 7. Para el parcial, si te preguntan

### 7.1 🔴 El error más común de objetos: polimorfismo ≠ siempre más flexible

Este es, textualmente, **uno de los errores más comunes del parcial en la parte de objetos**. Prestale atención.

La creencia equivocada: *"usar polimorfismo siempre me da más flexibilidad"*. **Falso.** El polimorfismo es útil **cuando existe variación de comportamiento**. Cuando lo que varía son **solo los datos** (mismo comportamiento), **meter polimorfismo te hace *menos* flexible**.

- **Variación de comportamiento → polimorfismo SÍ.** Distintos casos que *hacen algo distinto*.
  ```
  Descuento (interfaz)
    ├─ DescuentoPorcentaje   → calcula: precio * (1 - %)
    ├─ DescuentoMontoFijo    → calcula: precio - monto
    └─ Descuento2x1          → calcula: precio / 2
  // Cada uno CALCULA distinto → el comportamiento varía → polimorfismo tiene sentido.
  ```

- **Variación de datos → polimorfismo NO.** Distintos casos que *hacen lo mismo* y solo difieren en los valores.
  ```
  ❌ Con polimorfismo (rígido):
     ProductoLeche, ProductoPan, ProductoAgua...   ← una clase por producto
     // Agregar un producto nuevo = crear una clase = TOCAR y RECOMPILAR el código.

  ✅ Con una clase + instancias (flexible):
     Producto("Leche", 1200)                        ← misma clase Producto
     Producto("Pan", 900)                             para todos
     // Agregar un producto nuevo = crear una instancia EN EJECUCIÓN. No tocás código.
  ```

**El porqué técnico:** no podés extender una clase en tiempo de ejecución, pero **sí** podés crear instancias de objeto en tiempo de ejecución. Entonces, si resolvés una variación de *datos* con polimorfismo, para agregar un caso nuevo tenés que tocar el código **en tiempo de compilación** en lugar de simplemente instanciar en ejecución → **perdiste flexibilidad**.

Y ojo con el **abuso**: sobre-implementar polimorfismo (pasar de 5 clases a 10, 20) vuelve el sistema **menos mantenible**, porque ya no sabés dónde está cada pedazo de código. Recordá que **flexibilidad = extensibilidad + mantenibilidad**: podés ganar extensibilidad pero perder mantenibilidad, y ahí la flexibilidad total no mejoró.

> 🕳️ **Madriguera — variación de comportamiento vs. de datos.** Este tema (y cómo resolver elegante la variación en lenguajes híbridos objeto-funcionales) se profundiza en la electiva **TADP (Técnicas Avanzadas de Programación)**. Para esta materia alcanza con la regla: *polimorfismo cuando varía el comportamiento; una clase + instancias cuando varían los datos.* *Volvé al camino.*

### 7.2 "Siempre" / "nunca" en verdadero-falso = sospechá

Los enunciados de V/F con la palabra **"siempre"** (o "nunca") suelen ser **trampa**: en diseño casi todo *depende*. Cuando lo veas, buscá el contraejemplo antes de responder — como en el caso del polimorfismo de arriba.

### 7.3 Casos tipo (caso → respuesta modelo)

**Caso 5.** *¿Qué características son deseables en un diseño?*
> **Alta cohesión** y **bajo acoplamiento**. (Alto acoplamiento y baja cohesión son las indeseables — son las definiciones al revés.)

**Caso 6.** *V/F: el uso de polimorfismo siempre implica mayor flexibilidad.*
> **Falso.** Ver §7.1: es flexible cuando hay variación de comportamiento; con variación de datos es *menos* flexible, y abusar de él daña la mantenibilidad. (Además, la palabra "siempre" ya es señal de alerta.)

**Caso 7.** *V/F: un diseño altamente acoplado es más mantenible porque es más simple.*
> **Falso.** El alto acoplamiento va **en contra** de la mantenibilidad: un cambio en un componente se propaga a los demás. La premisa se cae por definición.

**Caso 8.** *Un colega propone un enfoque muy modular con Generics que leyó en un artículo, agregando múltiples niveles de abstracción y configuraciones genéricas "por si en el futuro se necesitan". ¿Qué principios se están ignorando?*
> **YAGNI** (está agregando cosas que nadie necesita *ahora*, "por si acaso") y **KISS** (múltiples niveles de abstracción "por las dudas" complejizan sin motivo). *No* son REST ni HTTP: HTTP ni siquiera es un principio de diseño, y REST no viene al caso.

---

## 8. Checkpoint (respondé sin mirar arriba)

1. ¿Qué son las cualidades de diseño y para qué las usás? ¿Son el único criterio para decidir un diseño?
2. Definí **acoplamiento** con tus palabras. ¿Por qué el alto acoplamiento es malo? Nombrá tres cosas que ganás al minimizarlo.
3. ¿Puede existir acoplamiento cero? ¿Qué significa la regla de "no dejar clases sueltas en un diagrama"?
4. Definí **cohesión**. ¿Qué es una god class y por qué tiene baja cohesión?
5. Enunciá el lema del diseño en objetos. Dá un ejemplo de alta cohesión con **alto** acoplamiento (para mostrar que son ejes independientes).
6. ¿Qué dice **KISS**? ¿Qué dice **YAGNI**? ¿En qué se diferencia "diseñar para la extensibilidad" de "violar YAGNI"?
7. ¿Qué exige la **robustez** ante una falla o un uso inadecuado? Relacioná "Robustez" (cualidad) con "Madurez" (atributo).
8. ¿En qué dos cualidades se abre la **flexibilidad**? Relacionalas con los cambios evolutivo / correctivo / perfectivo.
9. ¿Por qué "el polimorfismo siempre da más flexibilidad" es falso? Explicá con un ejemplo de variación de datos.
10. ¿Simplicidad y extensibilidad están siempre en conflicto? ¿Y simplicidad y mantenibilidad?

*(Las respuestas van al complemento de la unidad, no acá.)*

---

## Qué viene en la Parte 3

**Patrones de diseño** (concepto y clasificación): qué es un patrón, qué partes lo componen, las tres familias (creacionales, de comportamiento, estructurales) y —lo más importante para el parcial— **cuándo corresponde usar uno y cuándo forzarlo es un error** (spoiler: se conecta directo con KISS y YAGNI de esta parte).

**FIN DE LA PARTE 2**
