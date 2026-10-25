# 🧱 Clase desde Cero — Clase 01 · Módulo 2: Qué es la arquitectura de software

**Unidad:** `clase01` — Arquitectura de Software (25/03/2026)
**Serie:** Módulo 2 de 5 — el mapa completo está en el roadmap de la serie.

---

## Sobre este documento

**Qué cubre:** qué es la arquitectura de software · qué es un componente y qué es su interfaz · la analogía del edificio, completa · los tres niveles de la arquitectura (Enterprise, Sistema, Software) · qué caracteriza a la arquitectura de sistema y a las decisiones arquitectónicas.

**Qué NO cubre:** con qué insumos se decide la arquitectura (Módulo 3), cómo se dibuja en UML (Módulo 4). Acá todavía no se dibuja nada: primero hay que saber **qué** se va a dibujar.

## De dónde venís

- **Del Módulo 1:** diseñar = el plano del edificio, decidido antes de construir; los RNF describen cómo debe funcionar el sistema.
- **De la preclase 01:** objetos que se comunican mandándose mensajes — lo vas a reencontrar enseguida, en un lugar inesperado.

---

## 1. Qué es la arquitectura de software 🔴

Imaginá que vas a construir un edificio. Antes de poner el primer ladrillo, alguien tiene que decidir: ¿cuántos pisos va a tener? ¿Dónde van las escaleras? ¿Hay cochera? ¿Los caños de agua pasan por dónde?

Eso es exactamente lo que hace la arquitectura de software: son **las decisiones grandes que tomás antes de escribir código, y que después son muy difíciles de cambiar**.

Con la imagen puesta, la definición formal:

> La arquitectura de software representa **la estructura o estructuras del sistema**, que consiste en **componentes de software**, las **propiedades externas visibles** de esos componentes y las **relaciones entre ellos**.

Desarmémosla, porque cada pieza carga significado:

- **Estructura(s):** cómo está organizado el sistema en partes. Dice "estructura *o estructuras*" porque un mismo sistema admite más de una mirada estructural (lo vas a comprobar en el Módulo 4, donde dos diagramas distintos muestran dos estructuras del mismo sistema).
- **Componentes de software:** las partes. Siguiente sección.
- **Propiedades externas visibles:** de cada componente, a la arquitectura le importa **lo que se ve desde afuera** — qué ofrece, cómo se le habla — no su cocina interna. El adentro de cada componente es problema del diseño detallado, no del plano general.
- **Relaciones entre ellos:** quién habla con quién. Un plano no es una lista de habitaciones: es habitaciones **conectadas**.

---

## 2. El componente 🔴

> Un **componente** es una pieza de software que puede ser código fuente, código binario, un ejecutable, o una biblioteca con una interfaz definida.

Es decir: no hay una única forma física de componente. Puede ser un módulo de tu propio código, un programa completo, o una biblioteca que instalaste (código de terceros que tu programa usa, sin que vos lo hayas escrito).

Lo que distingue a un componente de "un pedazo cualquiera de código" es que **tiene su propio ciclo de vida**:

- su **repositorio** (el lugar donde vive y se versiona su código),
- un **equipo que lo mantiene**,
- un **esquema de versionado** (la convención con la que numera sus versiones — 1.0, 1.1, 2.0…),
- una **agenda de despliegues** (cuándo y cómo se publican sus versiones nuevas),
- etcétera.

Un componente es una pieza con vida propia: nace, evoluciona, se publica y se mantiene **como unidad**. Por eso la arquitectura razona en componentes y no en archivos sueltos.

---

## 3. La interfaz del componente 🔴

Si de cada componente lo que importa son sus "propiedades externas visibles"… ¿dónde viven esas propiedades? En su interfaz:

> Una **interfaz** establece las **operaciones externas** de un componente, las cuales determinan una parte del comportamiento del mismo.

La interfaz es el **contrato con el mundo exterior**: qué se le puede pedir al componente. Ejemplos de interfaces de componentes:

- **El conjunto de mensajes que entiende un objeto.** ¿Te suena? Es exactamente lo de la preclase 01: un objeto expone los mensajes que sabe responder y esconde el resto. La idea de interfaz que venís usando a escala de objetos es **la misma idea**, ahora a escala de componentes.
- **El conjunto de métodos públicos utilizables de una biblioteca.** Cuando usás una biblioteca, no leés su código interno: usás lo que publica.
- **Una API REST expuesta por un sistema** (una forma estándar de exponer operaciones a través de la web — la vas a estudiar en detalle muy pronto en la cursada).
- Etcétera.

Fijate el patrón común a los tres ejemplos: **algo con un adentro escondido y un afuera publicado**. Eso es una interfaz, a cualquier escala.

```
   ┌───────────────┐                    ┌───────────────┐
   │  Componente A │ ───▶ interfaz ───▶ │  Componente B │
   │  (su interior │      de B          │  (su interior │
   │   no se ve)   │   "la puerta"      │   no se ve)   │
   └───────────────┘                    └───────────────┘
```

A solo puede pedirle a B lo que la interfaz de B publica — nunca meterse en su interior. Cómo se **dibuja** esa puerta en UML tiene su notación propia; llega en el Módulo 4.

### La analogía del edificio, completa

| Edificio | Software |
|---|---|
| Edificio | Sistema de software |
| Habitaciones / plantas | Componentes / módulos |
| Puertas entre habitaciones | Interfaces entre componentes |
| El plano del arquitecto | El diagrama de arquitectura |

Las puertas son la parte más fina de la analogía: entre dos habitaciones no pasás atravesando la pared — pasás por la puerta. Entre dos componentes, igual: no se tocan por dentro, se hablan por la interfaz.

> **Para el parcial, si te preguntan — ¿qué es la arquitectura de software? ¿Qué es un componente y su interfaz?**
> La arquitectura de software representa la estructura o estructuras del sistema: sus componentes de software, las propiedades externas visibles de esos componentes y las relaciones entre ellos. Un componente es una pieza de software (código fuente, binario, ejecutable o biblioteca) con interfaz definida y ciclo de vida propio; su interfaz establece sus operaciones externas — el contrato de lo que se le puede pedir desde afuera.

---

## 4. Los tres niveles de la arquitectura 🔴

"Arquitectura" no se dice de una sola cosa: hay tres niveles, anidados como muñecas rusas — cada uno vive adentro del anterior:

```
        ┌─────────────────────────────────────────┐
        │              ENTERPRISE                 │
        │   estrategia tecnológica y de negocio   │
        │        de toda la organización          │
        │   ┌─────────────────────────────────┐   │
        │   │            SISTEMA              │   │
        │   │   arquitectura de software  +   │   │
        │   │        infraestructura          │   │
        │   │   ┌─────────────────────────┐   │   │
        │   │   │        SOFTWARE         │   │   │
        │   │   │  arquitectura para una  │   │   │
        │   │   │ aplicación o subsistema │   │   │
        │   │   └─────────────────────────┘   │   │
        │   └─────────────────────────────────┘   │
        └─────────────────────────────────────────┘
```

- **Enterprise:** define la estrategia tecnológica **y de negocio** de la organización para el desarrollo de sus sistemas. No mira un sistema: los mira a todos, y a la empresa entera.
- **Sistema:** arquitectura de software **e infraestructura** (los servidores, redes y máquinas donde ese software corre). Mira un sistema completo con sus subsistemas.
- **Software:** arquitectura de software para **una aplicación o subsistema**. Mira un componente por dentro.

La forma más rápida de fijar los niveles es ver **qué se ve** parado en cada uno:

| Nivel | ¿Qué se ve? | Ejemplo |
|---|---|---|
| **Enterprise** | Todos los sistemas de la organización y cómo se relacionan | "El sistema de Ventas habla con el de Stock y con el de Facturación" |
| **Sistema** | Un sistema y sus subsistemas principales | "Nuestra app tiene un Frontend, un Backend y una Base de Datos" |
| **Software** | Un componente específico por dentro | "El módulo de autenticación usa JWT y tiene estas clases…" |

*(JWT es solo el nombre de una tecnología puntual de autenticación — el ejemplo lo usa para mostrar el nivel de detalle, no hace falta saber qué es.)*

Trucazo para clasificar una frase: preguntate **cuántos sistemas abarca**. ¿Varios sistemas de la empresa? Enterprise. ¿Uno, con sus partes grandes? Sistema. ¿El interior de una parte? Software.

> **Para el parcial, si te preguntan — los niveles de la arquitectura.**
> Son tres: **Enterprise** (estrategia tecnológica y de negocio de la organización para todos sus sistemas), **Sistema** (arquitectura de software e infraestructura de un sistema y sus subsistemas) y **Software** (arquitectura de una aplicación o subsistema específico). Dada una frase, el nivel se identifica por el alcance: organización entera → Enterprise; un sistema y sus partes → Sistema; el interior de una parte → Software.

---

## 5. Qué caracteriza a la arquitectura (y a sus decisiones) 🔴

Dos listas hermanas que suelen confundirse. Una describe **cómo es la arquitectura de sistema**; la otra, **cómo son las decisiones arquitectónicas** que se toman al hacer arquitectura de software.

### La arquitectura de sistema es…

- **Diseño de nivel estratégico** — el de más alto nivel que existe.
- **No solo lógica: también física y organizacional.** Decide sobre software, pero también sobre fierros (en qué máquinas corre) y sobre personas (qué equipos mantienen qué).
- **Contiene las estrategias para resolver los atributos de calidad.** Guardá esta frase: los atributos de calidad son las propiedades del tipo "rápido, disponible, seguro" que ya asomaron como RNF en el Módulo 1 — y el Módulo 3 los pone en el centro de la escena.
- **Define el sistema en términos de elementos y de la interacción entre ellos** — la definición de la sección 1, en acción.
- **Gobierna al sistema durante años.** No es un documento que se archiva: es la estructura con la que el sistema va a vivir.

### Las decisiones arquitectónicas…

- **Afectan a muchas partes del sistema** (por eso son arquitectónicas y no de detalle).
- **Le dan estructura al sistema.**
- **Suelen ser difíciles de cambiar.**
- **Identifican y mitigan riesgos.**
- **Muchas deben tomarse en forma temprana.**

Las dos características del final son las que definen el oficio, y se explican una a la otra:

> Si el arquitecto se equivoca en el plano, derrumbar una pared después sale carísimo. En software, pasa exactamente lo mismo.

Como la decisión afecta a muchas partes, cambiarla después significa tocar todas esas partes — por eso es **difícil de cambiar**. Y como es difícil de cambiar, no podés dejarla para el final: hay que tomarla **temprano**, cuando todavía es barata. Ese es el drama del arquitecto: las decisiones más caras de errar son las que hay que tomar cuando menos información hay. ¿Con qué información se toman, entonces? Esa es exactamente la pregunta del Módulo 3.

> **Para el parcial, si te preguntan — ¿por qué las decisiones arquitectónicas se toman temprano y son difíciles de cambiar?**
> Porque afectan a muchas partes del sistema y le dan su estructura: revertirlas implica modificar todo lo que se construyó encima, como derrumbar una pared del edificio ya construido. Por eso se toman en forma temprana — cuando cambiar todavía es barato — y apuntan a identificar y mitigar riesgos antes de que el sistema exista.

---

## ✅ Checkpoint — Módulo 2

*Sin respuestas acá: intentalas sin mirar el material, y las dudas se resuelven por chat.*

1. Traducí la analogía a un caso: en un sistema para una biblioteca (el edificio), proponé dos componentes (habitaciones) y decí cuál sería la interfaz (puerta) entre ellos.
2. *"El módulo de facturación llama a los métodos públicos que expone el módulo de ventas."* ¿Qué concepto de este módulo describe la frase "métodos públicos que expone"? Justificá con la definición.
3. Clasificá cada frase en su nivel (Enterprise / Sistema / Software), justificando por el alcance: **a)** "La empresa migrará todos sus sistemas a la nube en 3 años" · **b)** "El servicio de notificaciones está hecho con el patrón Observer y estas cinco clases" · **c)** "SmartLife tiene un frontend web, un backend y una base de datos".
4. ¿Puede existir un componente sin interfaz? Respondé usando la definición de componente y explicá qué pasaría con las "relaciones entre componentes" si no la tuviera.
5. Un equipo decide qué base de datos usar recién en el último mes del proyecto, "cuando ya sepamos bien qué necesitamos". ¿Qué característica de las decisiones arquitectónicas está ignorando y qué riesgo corre? (Ojo: la postura del equipo tiene su lógica — nombrala también.)
6. La definición de arquitectura habla de propiedades **externas** visibles de los componentes. ¿Por qué a la arquitectura no le importa el interior de cada componente? ¿Quién se ocupa de ese interior?
7. ¿Qué agrega el nivel Sistema respecto del nivel Software, y qué agrega Enterprise respecto de Sistema? Contestá con las dos palabras clave que suma cada salto.
8. "La arquitectura gobierna al sistema durante años." Uní esa característica con la de "difícil de cambiar": ¿son dos formas de decir lo mismo, o dicen cosas distintas? Justificá.

---

## Qué viene en el Módulo 3

Quedó una pregunta picando: si las decisiones arquitectónicas hay que tomarlas temprano, **¿con qué información se toman?** El Módulo 3 responde con las **siete entradas de la arquitectura** — con los RNF del Módulo 1 como protagonistas, cerrando el hilo que quedó sembrado ahí — y suma la jerarquía que ordena el resto del año: estilo arquitectónico, patrón arquitectónico y patrón de diseño.

---

**FIN DEL MÓDULO 2**
