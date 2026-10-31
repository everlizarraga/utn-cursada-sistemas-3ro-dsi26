# 🎓 APUNTE MAESTRO — Seminario "Proceso de diseño empleando IA Generativa" · Encuentro 1
## Parte 1 — Modelos de lenguaje y el salto del IDE al ADE

**Unidad:** `seminario-ia-encuentro1` · **Parte 1 de 6** · Diseño de Sistemas de Información — UTN FRBA, 2026

---

### Sobre esta unidad

El seminario lo organizan en conjunto las cátedras de **Diseño de Sistemas de Información** y **Desarrollo de Software**. Son **dos encuentros**; este apunte cubre el primero, que se estructura en tres bloques:

1. **¿Qué es un agente de código?** → Partes 1, 2 y 3.
2. **¿Cómo se diseña un agente efectivo?** → Parte 4 y primera mitad de la Parte 5.
3. **¿Cómo se desarrolla de forma efectiva con agentes?** → Segunda mitad de la Parte 5 y Parte 6 (más el cierre del encuentro).

Cada bloque termina con un quiz (por QR) y un intervalo. El segundo encuentro es presencial, en laboratorio, y pide confirmación de asistencia.

**Cómo leer las marcas:** 🔴 central · 🟡 secundario · 🟢 mencionado al pasar. El seminario no figura en el cronograma de la cátedra, así que la señalización refleja el peso que el propio seminario le dio a cada tema. Los bloques *"Para el parcial, si te preguntan"* son respuestas modelo por si el tema aparece en una evaluación o en una defensa del TPA.

---

## 1. 🔴 La premisa: el cuello de botella se movió

Durante mucho tiempo, en la profesión, la actividad que más tiempo se llevaba era **codificar**. Como urgía "hacer que funcione", quedaban relegadas otras actividades: el análisis, el diseño, el diseño de los casos de prueba. Siempre fueron importantes, pero se las postergaba. Las metodologías ágiles empujaron en la misma dirección: *"no documentemos, hay que salir a producción, hay que hacer que esto funcione"*.

Hoy esa relación se invirtió. La codificación está, en buena parte, resuelta por herramientas de IA generativa. Y eso, lejos de quitarle valor a la profesión, **la potencia**: devuelve el tiempo para pensar una solución de calidad, mantenible, resiliente, que perdure.

> **Glosa —** *IA generativa*: modelos que producen contenido nuevo (texto, código) a partir de una instrucción en lenguaje natural. El resto de esta parte explica cómo funcionan por dentro.

Esto no es solo intuición. El cuatrimestre pasado, en Desarrollo de Software, se les propuso a unos 80 alumnos algo que los incomodaba: implementar **un sistema completo** en una tecnología que **no se había explicado** en la materia, usando una IA generativa como principal fuente de desarrollo. La entrega venía acompañada de preguntas sobre cómo habían vivido la experiencia. Una de las respuestas lo resume:

> *"Me encargué de orquestar, diseñar, más que programar."*

Hacia ahí va la profesión. La conclusión que abre el seminario es doble:

- La herramienta **acompaña y amplifica** la actividad de desarrollo; permite agilizarla.
- Pero **nadie puede rechazar código generado con criterio si ese criterio nunca lo ganó**. Por eso hay que volver a las bases: entender lo que se genera, sea escrito por nosotros o por una IA.

De esa convicción nace el seminario: aprender a usar estas herramientas **con un marco ingenieril**. No es una charla de purismo de diseño ni de desarrollo; es el marco de ingeniería que le damos a estas herramientas para aplicarlas en el trabajo diario.

> **Para el parcial, si te preguntan:** *¿Por qué la IA generativa potencia el rol del ingeniero en vez de reemplazarlo?*
> Porque resuelve la parte más costosa en tiempo (la codificación) y devuelve ese tiempo a las actividades que se relegaban por urgencia: análisis, diseño y casos de prueba. Pero el código generado solo se puede aceptar o rechazar con criterio, y ese criterio (entender lo que se genera) sigue siendo del ingeniero.

---

## 2. 🔴 Qué es un modelo de lenguaje

### 2.1 Arranquemos con un caso

Escribimos:

```
La capital de Francia es
```

y lo que hay del otro lado nos devuelve, para el próximo fragmento de texto, algo así como una **lista de candidatos con su probabilidad**:

```
 París         ████████████████████████████  (muy probable)
 Roma          ▏                             (poco probable)
 Buenos Aires  ▏                             (poco probable)
 ...
```

Elige el más probable: **París**. Eso es todo lo que hace un modelo de lenguaje, si lo simplificamos al máximo: **es una función que, dada una secuencia de tokens, predice cuál es el token más probable que sigue.**

> **Glosa —** *token*: la unidad con la que trabaja el modelo; por ahora pensalo como "fragmento de texto". Se define con precisión en §3.

Cada vez que escuchás que salió un modelo nuevo (Opus, Sonnet y los cientos que aparecen semana a semana en X), por detrás hay mucho marketing. Lo que no cambia es esta idea: una función que predice el siguiente token. Quedémonos con eso.

### 2.2 Lo probable, lo plausible y lo verídico: la alucinación

Como el modelo predice **lo más probable**, hay que distinguir entre lo **plausible** (lo que suena bien) y lo **verídico** (lo que es cierto). Dos casos:

| Situación | Qué pasó |
|---|---|
| Lo más probable **coincide** con lo verídico | El modelo **acertó**: fue por el camino que esperábamos. |
| Lo más probable **no coincide** con lo verídico | El modelo **alucinó**: "se fue por las ramas", dio algo que no queríamos. |

Lo clave: **el modelo hace ambas cosas con la misma seguridad.** No cambia el tono cuando acierta y cuando se equivoca.

Y hay algo más importante todavía: **una alucinación no es un error humano. El modelo no se equivoca.** Es una función, y siempre predice lo que considera más probable. Cuando lo probable y lo verídico difieren, no es que "falló": simplemente hizo lo único que sabe hacer.

De ahí sale la responsabilidad que atraviesa todo el seminario: como ingenieros, como programadores o como cualquier persona que use IA, tenemos que **proveer herramientas, estructura y andamiaje** para que el modelo se mantenga donde queremos que esté.

> **Para el parcial, si te preguntan:** *¿Qué es una alucinación y por qué no se la considera un "error" del modelo?*
> Una alucinación es la diferencia entre el token más probable que predijo el modelo y lo verídico (lo que esperábamos). No es un error porque el modelo es una función que siempre devuelve lo más probable; cuando eso no coincide con la verdad, el modelo cumplió su función igual. Por eso la responsabilidad de mantenerlo en el camino correcto es de quien lo usa, aportando contexto, herramientas y estructura.

### 2.3 La definición un poco más formal

Ahora sí, las tres palabras de la sigla **LLM** (*Large Language Model*, modelo de lenguaje amplio):

- **Modelo de lenguaje:** un algoritmo de **deep learning**, es decir, con **múltiples capas** que le permiten un proceso de **inferencia** (el cálculo que produce la predicción) del token más probable.
- **Amplio (Large):** está entrenado con una cantidad enorme de datos, llamada **corpus**.
- Ese corpus llega **hasta una fecha determinada**. Por eso, si le preguntamos por una noticia reciente sin pasarle un link al que pueda acceder, es muy probable que no pueda responder.

> 🕳️ **Madriguera — Transformer**
> La arquitectura de red neuronal que está por detrás de los modelos actuales, la que hace la predicción del próximo token. Saber cómo funciona por dentro no hace falta para usar bien un agente.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 3. 🔴 Tokens: el modelo no ve palabras

Decimos "me quedé sin tokens", "¿te alcanzan los tokens para esto?". Pero ¿qué es un token?

Un token es el resultado de un **preprocesamiento** del lenguaje natural que le enviamos al modelo: transforma las palabras (o fragmentos de palabras) en **códigos** que pertenecen a un **diccionario conocido por el modelo**. Y con eso trabaja:

```
 Lo que escribimos:   "La capital de Francia es"
                              │  preprocesamiento (tokenización)
                              ▼
 Lo que ve el modelo:  ⟨1042⟩ ⟨8871⟩ ⟨322⟩ ⟨25910⟩ ⟨613⟩      ← números ilustrativos
```

**El modelo no ve palabras. Solo ve códigos.**

### 3.1 El caso "strawberry"

En un momento se popularizó una prueba para "medir la calidad" de un modelo: preguntarle cuántas letras **r** tiene la palabra *strawberry*. Es una prueba errática. Como el modelo no lee palabras sino tokens, preguntarle eso es equivalente a preguntarle:

> *"¿Cuántas ocurrencias de la letra R hay en la página 101-830?"*

No tiene acceso a las letras: tiene acceso a códigos. Entender esto acerca a usarlo bien.

### 3.2 Consecuencia: necesita herramientas

Por la misma razón por la que no puede contar letras, cuando le pedimos que **haga una suma**, una operación matemática, o que **busque en internet**, necesita **algo extra** que lo ayude con esa tarea. El modelo está pensado para predecir palabra a palabra, no para hacer operaciones algorítmicas o matemáticas.

Ese "algo extra" son las **herramientas**. Si le pedimos que sume, le damos una **calculadora** para que pueda hacer la operación. Este es el germen de lo que en §7 se llama *agente*.

---

## 4. 🟡 Cómo procesa: prefill, decode y por qué la salida es más cara

El proceso del modelo tiene dos partes:

```
   ENTRADA (prefill)                          SALIDA (decode)
   ─────────────────                          ───────────────
   todos los tokens que mandamos              token por token
   se procesan EN PARALELO                    se generan EN SECUENCIA
   (mucho uso de GPU)                         (cada token nuevo usa TODOS los anteriores)
         │                                            │
   ⟨1042⟩⟨8871⟩⟨322⟩⟨25910⟩⟨613⟩  ──►  modelo  ──►  ⟨París⟩ → ⟨.⟩ → ...
```

> **Glosa —** *GPU*: el procesador gráfico; es el hardware que hace los cálculos pesados del modelo.

- **Prefill** (la entrada): todos los tokens que le mandamos se procesan **de forma paralela**, usando muchos recursos, principalmente de GPU.
- **Decode** (la salida): se hace **de forma secuencial**, porque la salida se genera de manera **autoregresiva**: cada vez que el modelo tiene que predecir el próximo token, usa **todos los anteriores** para hacer el cálculo.

En el medio de todos esos cálculos pesados hay un componente llamado **KV cache** (*key-value*, clave-valor): guarda ciertos tokens ya procesados para que, cuando vuelvan a aparecer, estén disponibles y faciliten los cálculos.

**Esto explica algo que se ve en cualquier benchmark o tabla de precios de modelos:** los tokens de entrada y los de salida tienen **precios distintos**, y el de salida es más caro. La razón es exactamente esta: la salida implica más cálculo, secuencial, token por token, y por ende es un proceso más complejo y más costoso.

> **Glosa —** *benchmark*: prueba estandarizada con la que se comparan modelos entre sí; las tablas de precios suelen acompañar esas comparaciones.

---

## 5. 🔴 Del IDE al ADE

### 5.1 Una herramienta nueva

Como programadores venimos trabajando con una herramienta que conocemos bien: el **IDE** (*entorno de desarrollo integrado*, el programa donde escribimos, compilamos, testeamos y debuggeamos código). Eso está cambiando. Empieza a ganar protagonismo el **ADE**: un **entorno de desarrollo agéntico**, pensado para que, en vez de haber un humano tipeando código, haya **un humano orquestando agentes**.

> **Glosa —** *agente*: por ahora, "un modelo de lenguaje con herramientas alrededor". La definición precisa está en §7.

### 5.2 Ubicarlo en el mapa de abstracciones

A lo largo de la carrera fuimos conociendo **niveles de abstracción** que se fueron apilando:

```
   ┌────────────────────────────────────────────┐
   │  APIs · frameworks · plataformas           │   ← nos desentendemos de todo lo de abajo
   ├────────────────────────────────────────────┤
   │  Lenguajes de alto nivel                   │   ← mecanismos para no pensar en memoria
   ├────────────────────────────────────────────┤
   │  Lenguajes de bajo nivel                   │   ← manejan memoria
   ├────────────────────────────────────────────┤
   │  Assembler                                 │   ← trabaja con registros, no con bits
   ├────────────────────────────────────────────┤
   │  Bits · hardware                           │
   └────────────────────────────────────────────┘
```

> **Glosas —** *assembler*: lenguaje de muy bajo nivel, casi instrucciones directas del procesador. *Registros*: pequeñas memorias dentro del procesador. *API / framework / plataforma*: capas ya construidas que usamos sin ver cómo funcionan por dentro.

¿Qué significa una **abstracción**? Que **nos desentendemos de lo que hay por debajo**. Cuando programo en un lenguaje de bajo nivel no pienso en los registros del assembler; cuando programo en alto nivel no pienso en cómo se maneja la memoria (salvo operaciones muy particulares). Es una capa nueva y **delego por completo** la responsabilidad en la capa inferior.

### 5.3 Por qué el ADE no entra en esa escalera

El IDE siempre nos acompañó **en paralelo**: hacíamos refactoring, renames, testing, debugging, y **no teníamos que revisar** que esas actividades funcionaran. Nadie verifica que el mecanismo de debugging del IDE funcione correctamente; se confía.

Con el ADE eso cambia. **Necesitamos supervisión.** Entramos en un ciclo constante sobre lo que se va generando:

```
        ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
   ───► │ GENERAR  │ ──► │   LEER   │ ──► │ VALIDAR  │ ──► │ CORREGIR │ ──┐
        └──────────┘     └──────────┘     └──────────┘     └──────────┘   │
        ▲                                                                 │
        └─────────────────────────────────────────────────────────────────┘
```

Entonces, ¿el ADE es **una capa más** de abstracción? **No.** En una abstracción real nos olvidamos de la capa de abajo; con el ADE, en cambio, tenemos que **volver a supervisar** constantemente lo que se hizo. En el IDE el código puede compilar y pasar; en el ADE hace falta **una capa extra de revisión**.

### 5.4 Resumen: IDE vs. ADE

| | IDE | ADE |
|---|---|---|
| **Qué recibe como input** | **Código** | **Contexto** |
| **Quién hace el trabajo** | Un humano tipeando | Un humano **orquestando agentes** |
| **¿Hay que revisar el mecanismo?** | No (refactor, rename, testing, debugging funcionan solos) | **Sí**: ciclo generar → leer → validar → corregir |
| **¿Es una abstracción real?** | Sí | **No**: no se delega por completo |

Son herramientas **complementarias**: una no reemplaza a la otra y **no compiten entre sí**. Seguimos usando las herramientas de debugging y de testing; el ADE se suma.

> **Para el parcial, si te preguntan:** *¿El ADE es una nueva capa de abstracción, como lo fueron los lenguajes de alto nivel o los frameworks?*
> No. Una abstracción real nos permite desentendernos de la capa inferior y delegar por completo. El ADE no admite eso: lo que genera hay que leerlo, validarlo y corregirlo en un ciclo constante. Por eso su input no es código sino contexto, y la supervisión del resultado sigue siendo del ingeniero.

---

## 6. 🔴 Qué se delega y qué se conserva: el contexto como responsabilidad

Toda la idea del seminario se apoya en esta distinción:

- **Se conserva:** el **diseño** y las **decisiones arquitectónicas**. Entender el diseño es nuestra responsabilidad.
- **Se delega:** ciertas tareas, **de forma controlada** y **con criterio** sobre qué se delega.

Y la **calidad** de lo que sale depende totalmente del diseño de algo que hasta ahora nombramos al pasar: el **contexto**. El contexto es, básicamente, **lo que nosotros le entregamos al modelo**. Gestionarlo es nuestra responsabilidad.

Dos aclaraciones que evitan malentendidos:

1. Esto **no** va de escribir prompts cada vez más largos ni de darle más y más contexto. Va de hacerlo **eficiente**.
2. Todo converge a lo mismo de siempre en ingeniería: **resolver un problema de manera eficaz y eficiente.**

> **Glosa —** *prompt*: la instrucción en lenguaje natural que le escribimos al modelo.

Las herramientas para trabajar el contexto se desarrollan en las Partes 4 y 5. Lo que hay que llevarse de acá es el principio: **el contexto es nuestra responsabilidad.**

---

## 7. 🔴 Herramientas: del modelo al agente, y el riesgo que traen

En §3 vimos que el modelo no puede hacer operaciones matemáticas y que por eso le damos una calculadora. Generalicemos. Ejemplos de herramientas que se le pueden dar a un modelo:

- **búsqueda de código** (encontrar algo dentro de un proyecto),
- **control de versiones** (Git, para ver y modificar el historial del código),
- **herramientas de plataforma** (interactuar con el entorno donde corre el sistema).

Todas hacen lo mismo: **amplían las capacidades del modelo**. Y ese es el salto conceptual: un modelo que solo predecía palabra a palabra, cuando tiene herramientas, **empieza a hacer cosas que antes no podía**. Recién ahí podemos llamarlo **agente**.

```
   ┌──────────────┐                         ┌──────────────────────────┐
   │    MODELO    │   + herramientas   ──►  │          AGENTE          │
   │ predice el   │   (calculadora,         │ predice Y actúa sobre    │
   │ próximo token│    código, Git...)      │ el entorno               │
   └──────────────┘                         └──────────────────────────┘
```

Todo el seminario apunta a hacer este uso más **efectivo y eficiente**, trabajando bien con estas herramientas.

### 7.1 Más herramientas no es mejor trabajo

Que le demos más herramientas **no significa necesariamente que haga el trabajo mejor**. ¿Le estamos dando más capacidades? Sin discusión. Pero hay que reconocer lo que viene junto: **cuanta más capacidad le damos, cuanto más espacio tiene para actuar sobre las distintas etapas del proceso de desarrollo, más exposición al riesgo tenemos.** No es un detalle menor y hay que tenerlo en mente. Delegar exige poner límites; ese tema se desarrolla en la Parte 3.

> **Para el parcial, si te preguntan:** *¿Qué diferencia a un agente de un modelo de lenguaje, y qué trade-off aparece al agregarle herramientas?*
> Un agente es un modelo de lenguaje al que se le dieron herramientas (calculadora, búsqueda de código, control de versiones, plataforma) que amplían sus capacidades y le permiten actuar sobre el entorno, no solo predecir texto. El trade-off: cada herramienta suma capacidad, pero también suma exposición al riesgo, porque el agente puede actuar sobre más partes del proceso de desarrollo. Más herramientas no garantiza mejor trabajo.

---

## Síntesis de la Parte 1

| Idea | En una línea |
|---|---|
| Cuello de botella | Pasó de codificar a diseñar, validar y decidir. |
| Modelo de lenguaje | Función que predice el token más probable. |
| Alucinación | Lo probable ≠ lo verídico, dicho con la misma seguridad. No es un error del modelo. |
| Token | Código de un diccionario; el modelo no ve palabras. |
| Prefill / decode | Entrada en paralelo, salida secuencial y autoregresiva; por eso el output cuesta más. |
| IDE → ADE | De tipear código a orquestar agentes; el input pasa de código a contexto. |
| ¿Abstracción? | No: el ADE exige supervisar (generar → leer → validar → corregir). |
| Contexto | Lo que le entregamos al modelo. Es nuestra responsabilidad, y tiene que ser eficiente. |
| Agente | Modelo + herramientas. Más herramientas = más capacidad **y** más riesgo. |

---

## ✅ Checkpoint (sin respuestas)

1. Explicá con tus palabras qué hace un modelo de lenguaje, usando el ejemplo de la capital de Francia.
2. Un compañero dice: "el modelo se equivocó, me inventó una función que no existe". ¿Qué le corregirías de esa frase y por qué?
3. ¿Por qué pedirle a un modelo que cuente las letras de una palabra es una mala prueba de su calidad?
4. ¿Por qué los tokens de salida cuestan más que los de entrada? Explicalo a partir de prefill y decode.
5. Un lenguaje de alto nivel te permite olvidarte de la memoria. ¿Qué te permite olvidar el ADE? Justificá.
6. Si el input del IDE es código y el del ADE es contexto, ¿qué es exactamente "contexto" hasta lo visto en esta parte?
7. Tenés que decidir si darle a un agente acceso a Git y a la base de datos de producción. Con lo visto en §7, ¿qué argumento a favor y qué argumento en contra podés dar?
8. ¿Por qué el seminario insiste en "volver a las bases" (entender lo que se genera) justo cuando la codificación se automatiza? ¿Qué pasaría si no lo hiciéramos?

---

## Qué viene en la Parte 2

La Parte 2 abre el agente por dentro: el **harness** (el sistema que rodea al modelo y ejecuta las herramientas), el **ciclo agéntico**, la **ventana de contexto** y cómo el costo se acumula en cada interacción. Después, el rol del ingeniero: qué no sabe el agente sobre tu sistema y tu negocio, y por qué la validación sigue siendo tuya.

---

**FIN DE LA PARTE 1 — Modelos de lenguaje y el salto del IDE al ADE**
