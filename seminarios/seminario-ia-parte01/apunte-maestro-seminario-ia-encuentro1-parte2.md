# 🎓 APUNTE MAESTRO — Seminario "Proceso de diseño empleando IA Generativa" · Encuentro 1
## Parte 2 — El agente por dentro y el rol del ingeniero

**Unidad:** `seminario-ia-encuentro1` · **Parte 2 de 6** · Diseño de Sistemas de Información — UTN FRBA, 2026

> Esta parte abre el agente por dentro: qué es el "alrededor" del modelo, cómo itera, dónde vive su memoria y por qué el costo crece con cada mensaje. Después vuelve al ingeniero: qué no sabe el agente y por qué la validación sigue siendo nuestra.

---

## 1. 🟡 El problema de partida: 15.000 desarrolladores y una licencia

Imaginate una empresa con más de 15.000 desarrolladores a los que hay que darles una licencia de un agente de código. Hay un límite claro: **cuántos tokens se gastan** para mejorar la productividad. Cada mensaje que manda cada desarrollador cuesta plata.

Para gastar bien hay que entender **cómo funciona el ciclo agéntico** y **cómo interactúa el agente con sus herramientas**. Esa comprensión es lo que permite mejorar la efectividad del uso. De eso trata la primera mitad de esta parte.

---

## 2. 🔴 El harness: las manos del modelo

### 2.1 Un caso: "leé este archivo"

Le pedimos a un agente de código que lea un archivo del proyecto. ¿Qué pasa?

```
   Vos ──► "leé el archivo Pedido.java"
                    │
                    ▼
   ┌─────────────── HARNESS ────────────────┐
   │ arma el contexto y se lo manda ──────► │ ──►  MODELO
   │                                        │      "quiero usar la herramienta READ
   │ recibe el pedido, valida permisos,  ◄──│ ◄──   sobre Pedido.java"
   │ ejecuta READ, guarda el resultado      │
   │ en la memoria y se lo manda ─────────► │ ──►  MODELO
   │                                        │      "listo, el archivo dice..."
   └────────────────────────────────────────┘
                    │
                    ▼
   Vos ◄── respuesta en texto
```

Fijate quién hace qué: **el modelo no ejecuta nada**. Solo pide. **El harness ejecuta.**

### 2.2 Qué es el harness

El modelo, solo, es simple: recibe una entrada (nuestro texto, el prompt) y devuelve una salida (también texto). Para que funcione como **agente de código** tiene que haber **todo un sistema alrededor** que le permita interactuar con la computadora, con sus herramientas o con el entorno donde esté levantado.

Ese sistema es el **harness**: **todo lo que rodea al modelo de lenguaje y le permite interactuar con nuestro sistema.** Son sus manos. Concretamente:

- interactúa con las **herramientas**,
- **valida permisos**,
- tiene el control de la **memoria de una sesión**,
- permite **gestionar el contexto**.

> **Glosa —** *sesión*: una conversación con el agente, desde que la abrís hasta que la cerrás.

Cuando usamos herramientas como **Codex, Claude Code, Gemini o Copilot**, por detrás hay dos cosas: primero un **sistema** que valida toda la información que se le va a mandar al modelo; después el **modelo**, que solicita qué herramientas tiene que ejecutar ese sistema. El sistema es el harness.

### 2.3 Qué carga el harness desde el inicio

Antes de que escribas nada, el harness ya tiene un contexto cargado, y se lo manda **siempre** al modelo:

| Qué | Qué es |
|---|---|
| **System prompt** | La instrucción principal del agente: cómo tiene que comportarse. |
| **Archivos** | Los que estén configurados para cargarse al inicio. |
| **Skills** | Instrucciones reutilizables; se explican en la Parte 4. Por ahora: "documentos que el agente sabe cuándo leer". |
| **Hooks** | Scripts que se disparan ante ciertos eventos; también en la Parte 4. |
| **Descripciones de tools** | Todo lo que el modelo necesita conocer sobre cada herramienta disponible para poder pedirla. |

Y sobre eso, el **histórico**: una memoria donde se guarda toda la conversación. El harness es quien guarda las sesiones y los mensajes. Y acá está el detalle que casi nadie tiene en cuenta: **cada vez que mandás un prompt, todo eso se vuelve a enviar al modelo, al proveedor.**

> **Glosa —** *proveedor*: la empresa cuyo modelo estás usando (la que cobra los tokens).

Volvé al caso de los 15.000 desarrolladores: si ninguno tiene en cuenta que cada prompt reenvía toda la conversación, los costos se disparan.

### 2.4 Las piezas del harness

Dentro del harness hay un **constructor de contexto**: el que arma todo lo que se le manda al modelo juntando las partes de arriba (system prompt, historial, skills, descripciones de tools).

El harness también controla **a qué herramientas tiene acceso** el agente. Somos **nosotros** quienes le damos ese acceso. No se le puede dar un bypass ni libre albedrío: es muy común (ya salió en noticias) que un agente intente **borrar una base de datos** porque "se lo imaginó". El control lo tenemos nosotros como ingenieros, y esa posibilidad nos la da el harness.

En resumen, el harness:

1. **controla permisos**,
2. **controla el contexto**,
3. **ejecuta las herramientas que el modelo solicita** y **valida** el resultado,
4. y entra en un **loop constante**: ejecuta una herramienta → guarda en memoria → valida → el modelo solicita otra → ... hasta que el modelo interpreta que resolvió el problema.

> **Para el parcial, si te preguntan:** *¿Quién ejecuta las herramientas en un agente de código: el modelo o el harness?*
> El harness. El modelo solo predice texto y, dentro de ese texto, solicita qué herramienta usar (por ejemplo, "leer este archivo"). El harness, que es el sistema que rodea al modelo, valida los permisos, ejecuta la herramienta (read, grep, una shell, una API), guarda el resultado en la memoria de la sesión y se lo devuelve al modelo en el próximo ciclo.

---

## 3. 🔴 El ciclo agéntico

Con las piezas ya nombradas, el bucle completo:

```
                 ┌──────────────────────────────────────────────────┐
                 │                    HARNESS                       │
                 │                                                  │
  prompt ──────► │  1. CONSTRUCTOR DE CONTEXTO                      │
                 │     system prompt + historial + skills + tools   │
                 │                     │                            │
                 │                     ▼                            │
                 │  2. envía al ────────────────────►  MODELO       │
                 │     proveedor                       (predice;    │
                 │                                      pide tool)  │
                 │                     ◄────────────────┘           │
                 │  3. VALIDA PERMISOS                              │
                 │     ¿puede usar esa herramienta?                 │
                 │                     │                            │
                 │                     ▼                            │
                 │  4. EJECUTA LA TOOL  (shell · API · read · grep) │
                 │     e interactúa con el entorno                  │
                 │                     │                            │
                 │                     ▼                            │
                 │  5. GUARDA en MEMORIA y vuelve a 1  ─────────┐   │
                 │                                              │   │
                 │        ... hasta que el modelo decide que    │   │
                 │            el problema está resuelto ◄───────┘   │
                 └──────────────────────────────────────────────────┘
                                       │
                                       ▼
                               respuesta final
```

> **Glosas —** *shell*: la terminal; el agente puede ejecutar comandos en ella. *API*: interfaz para pedirle cosas a otro sistema (ya la conocés de las clases de REST; acá es una herramienta más del agente).

El ciclo agéntico es, entonces, **el que itera constantemente y rearma el contexto** para mandárselo al modelo en cada vuelta. El harness ejecuta; el modelo decide y valida la respuesta.

---

## 4. 🔴 La ventana de contexto: memoria finita y acumulativa

Del lado del modelo entra un concepto central: la **ventana de contexto**. Es la **memoria del agente**. Como la nuestra, es **limitada**: hoy ronda el millón de tokens.

¿Qué hay adentro? **Todo lo que el modelo lee en una llamada.** Y **se acumula**: cada prompt que mandás es una acumulación más sobre los mensajes guardados.

### 4.1 Un caso concreto

1. Primer mensaje: *"analizá esta feature"*.
2. Segundo mensaje: *"ahora hacele un refactor"*.

> **Glosas —** *feature*: una funcionalidad del sistema. *refactor*: reescribir código sin cambiar lo que hace, para mejorarlo.

En el segundo ciclo, la ventana de contexto contiene el **primer** prompt, el **segundo**, las respuestas, **más todas las descripciones de herramientas** que interactúan con el agente. Todo eso viaja de nuevo al modelo. **Más contexto = más costo.**

### 4.2 De qué está hecha una ventana real

Mirando la ventana de una conversación real de trabajo, la distribución es aproximadamente esta:

```
   mensajes (la conversación)   ████████████████████████████████  ~80 %
   system prompt y otros        ██████                            resto
   skills                       ▏                                 ~2 %
```

La ventana está **dominada por los mensajes**. Y crece iteración a iteración:

```
   iteración 1:   1.400 tokens
   iteración 2:   1.400 + lo nuevo     (se acumula)
   iteración 3:   todo lo anterior + lo nuevo
   ...
   en algún momento la ventana se LLENA y los costos ya crecieron
```

Por eso es importante tener el control de **qué contexto rodea a nuestro sistema agéntico**. Cómo trabajarlo de forma efectiva es el tema de la Parte 4 (*context engineering*); acá alcanza con el concepto.

---

## 5. 🟡 La caché del proveedor y por qué no todos los tokens cuestan igual

Los proveedores tienen una **caché**: en vez de volver a procesar la ventana de contexto entera en cada llamada, guardan los tokens del **contexto repetido** ya procesados.

> **Glosa —** *caché*: memoria rápida donde se guarda algo ya calculado para no calcularlo de nuevo.

Cuando mandás un system prompt, o una frase, que ya se usó constantemente, se busca en la caché: si está, hay un **hit** y se manda al modelo con los tokens ya procesados. Si **no** está, se procesa de nuevo y cuesta distinto.

De ahí sale la tabla de precios:

| Tipo de token | Costo relativo |
|---|---|
| **Input** (entra por primera vez) | bajo |
| **Escritura en caché** | algo más que el input |
| **Lectura de caché** (hit) | mucho más barata |
| **Output** | **el más caro** (lo vimos en la Parte 1, §4: generación secuencial) |

La ventaja práctica: si tenés **documentos que el agente lee constantemente** (las skills son el ejemplo típico), este mecanismo hace que ese contexto esté siempre cacheado y salga barato.

---

## 6. 🟡 Cómo saber cuánto estás gastando

Cada ADE trae comandos para verlo; en Claude Code son **`/usage`** y **`/cost`**. Muestran cuántos tokens se gastaron, con qué modelo, cuántos gastó cada modelo, y **qué porcentaje de la ventana de contexto** está ocupado.

Ese porcentaje es una validación clave, porque la ventana siempre se está acumulando. **Regla práctica: si llegás al 80 % de la ventana, lo mejor es empezar una sesión nueva o compactar.**

> **Glosa —** *compactar*: pedirle al agente que resuma su ventana de contexto para liberar espacio. Se desarrolla en la Parte 4.

### Los tres conceptos de esta mitad

1. Hay una **ventana de contexto**: una memoria **finita**.
2. Hay un **harness**: el sistema que rodea al modelo y le permite interactuar con nuestro entorno.
3. Hay un **ciclo agéntico**: el que itera constantemente y **rearma el contexto** para que el modelo lo procese.

> **Para el parcial, si te preguntan:** *¿Por qué una conversación larga con un agente cuesta cada vez más, y qué se hace al respecto?*
> Porque la ventana de contexto es acumulativa: en cada iteración el harness reenvía al modelo todo el historial más el system prompt y las descripciones de herramientas, así que cada mensaje cuesta más que el anterior. Se controla monitoreando el porcentaje de ventana ocupada (`/usage`, `/cost`) y, cerca del 80 %, abriendo una sesión nueva o compactando. La caché del proveedor abarata el contexto repetido, pero no evita la acumulación.

---

## 7. 🔴 El rol del ingeniero: el cuello de botella se corrió a la revisión

¿La IA nos va a reemplazar? No es la pregunta de esta parte. La pregunta es **qué rol tenemos**, porque ese rol es necesario para que la IA genere buen código y buenas soluciones.

Antes, lo más difícil era **escribir código**: horas sentados resolviendo, diseñando, testeando. Hoy, ¿cuántos escriben código a mano? Pocos. ¿Cuántos **revisan** el código que se escribió? ¿Confiamos o no en ese código? **Ahí está lo fundamental: el cuello de botella se pasó a la revisión.**

### 7.1 El estudio de METR

Una organización llamada METR tomó desarrolladores, les dio **264 tareas** y les pidió resolverlas usando IA.

```
   ANTES de empezar         →  "vamos a ser un 24 % más rápidos"     (expectativa)
   Se cronometró todo       →  tardaron un 19 % MÁS que sin IA        (resultado)
   DESPUÉS de terminar      →  "creo que fui un 20 % más rápido"      (percepción)
```

¿Eran desarrolladores poco experimentados? No. ¿Bajó la calidad del código? Tampoco. Lo que muestra es un **sesgo**: somos demasiado **optimistas** con lo que puede resolver una IA. Generar código hoy es barato y rápido, es verdad; pero **sigue siendo necesario validar y revisar** lo que hizo. El estudio se repitió en 2026 y los resultados fueron muy similares.

> **Para el parcial, si te preguntan:** *¿Qué muestra el estudio de METR sobre trabajar con agentes de código?*
> Que la percepción de velocidad no coincide con la medición: los desarrolladores esperaban ser un 24 % más rápidos y creyeron haberlo sido en un 20 %, pero cronometrados tardaron un 19 % más. No fue falta de experiencia ni pérdida de calidad, sino un sesgo optimista: la generación es barata, pero la validación sigue costando tiempo y sigue siendo indispensable.

---

## 8. 🔴 Tres cosas que el agente no sabe

¿Por qué necesitamos estar presentes en el proceso? Porque el agente tiene tres limitantes.

### 8.1 No conoce tu sistema

¿Conoce las integraciones? ¿Los dominios? ¿Los casos de uso? ¿Los repositorios existentes? No llega a conocer todo; no puede hacer una lectura completa del sistema.

Y ahí entra nuestra responsabilidad de **entender y usar** conceptos que en la facultad parecían un plomo, porque queríamos ir a picar código: **cohesión, cualidades de diseño, atributos de calidad**. Hoy esas bases son las que nos dan **criterio formado** y un **vocabulario común** para guiar al agente a que haga las cosas como queremos.

> *Cohesión y atributos de calidad son los de la materia: los mismos con los que se justifican las decisiones de diseño en DSI.*

### 8.2 No conoce tu negocio

¿Quién entiende las **decisiones de negocio** tomadas antes, las **restricciones** que existen, las **definiciones**? Seguimos siendo nosotros. Si no le damos esa información, o si no existe, ¿qué hace el modelo? **Inventa. Alucina** (Parte 1, §2.2).

Gran parte del seminario explica cómo achicar estos límites. Pero la responsabilidad no se delega: **entender el problema, tomar las decisiones de diseño y crear la arquitectura** es nuestro. Recién después el modelo se encarga de tareas que deleguemos **de manera controlada**.

### 8.3 El producto no es el código

Los tests pueden pasar, todo puede funcionar, la aplicación puede ser resiliente. Y aun así: ¿qué pasa con la **experiencia de usuario**? ¿Con la **adecuación funcional**, con todas las funcionalidades que sabemos que la aplicación tiene que cubrir? Ese análisis, que en algún momento dejamos de lado, hoy vuelve a tener protagonismo. La calidad de lo que entregamos es nuestra responsabilidad.

> **Glosa —** *adecuación funcional*: que el sistema haga lo que el usuario necesita, no solo que "funcione".

---

## 9. 🔴 Validar es el trabajo

Resumen de lo anterior: **sin contexto, sin specs, sin validación**, lo que desarrollamos puede derivar en alucinaciones y en productos que no satisfacen lo que se esperaba.

> **Glosa —** *spec*: especificación de lo que se quiere construir. Es el tema de la Parte 6.

Entonces, ¿dónde está nuestra responsabilidad? En **validar** lo que se hace. Y validar **no** significa leer línea por línea:

- puede ser una **lectura estática** del código,
- puede estar **delegada incluso en otro agente**,
- lo importante es que **exista la instancia de validación**.

Porque asegurar la calidad de lo que entregamos sigue siendo nuestro.

```
   NO se delega                          SE delega (controlado)
   ───────────────────────────           ─────────────────────────────
   entender el problema                  tareas acotadas
   decisiones de diseño                  generación de código
   arquitectura                          partes de la validación
   la instancia de validación existe     (incluso a otro agente)
```

Y acá se cierra el círculo con la Parte 1, §7: a más herramientas, más exposición al riesgo. **Delegar exige poner límites.** Eso es la Parte 3.

---

## Síntesis de la Parte 2

| Idea | En una línea |
|---|---|
| Harness | Todo lo que rodea al modelo: ejecuta tools, valida permisos, guarda memoria, gestiona contexto. |
| Modelo vs. harness | El modelo pide; el harness ejecuta. |
| Contexto inicial | System prompt, archivos, skills, hooks, descripciones de tools: siempre cargado y siempre enviado. |
| Ciclo agéntico | Armar contexto → modelo → validar permisos → ejecutar tool → guardar → repetir. |
| Ventana de contexto | Memoria finita y acumulativa; ~80 % son mensajes; cada iteración cuesta más. |
| Caché | Contexto repetido ya procesado: hit barato; output sigue siendo lo más caro. |
| Regla del 80 % | Sesión nueva o compactar. `/usage`, `/cost` para mirarlo. |
| Cuello de botella | Se corrió a la revisión; METR: esperaban +24 %, midieron −19 %, creyeron +20 %. |
| Lo que no sabe | Tu sistema, tu negocio, y que el producto ≠ el código. |
| Validación | Es la responsabilidad que no se delega; la forma sí puede variar. |

---

## ✅ Checkpoint (sin respuestas)

1. Un agente "borró una tabla de la base de datos". ¿Qué componente tendría que haberlo impedido y por qué el modelo, por sí solo, no puede evitarlo?
2. Describí, paso a paso, qué pasa dentro del harness desde que escribís "buscá dónde se calcula el total del pedido" hasta que ves la respuesta.
3. ¿Por qué el mensaje número 30 de una conversación cuesta más que el número 3, aunque sea más corto?
4. Tenés una skill que el agente lee en cada sesión. ¿Qué mecanismo hace que eso no te cueste como si fuera texto nuevo cada vez?
5. Estás al 85 % de la ventana de contexto en medio de una tarea. ¿Qué hacés y qué pagás con cada opción?
6. El estudio de METR midió más lentitud, pero los desarrolladores sintieron más velocidad. ¿Qué implica eso para cómo decidís cuándo usar un agente?
7. Un agente escribió un módulo que compila y pasa todos los tests. Nombrá dos motivos por los que igual podría no servir, usando las limitantes de §8.
8. "Validar no es leer línea por línea." ¿Qué sí es, entonces, y qué es lo único que no se puede saltear?

---

## Qué viene en la Parte 3

Delegar exige límites. La Parte 3 trata los **riesgos**: qué pasa con los datos que le mandás a un proveedor, cómo protegerte (permisos mínimos, aislamiento, no acoplarse), y lo que no controlás: con qué se entrenaron los modelos, dónde corren, cuánto cuestan de verdad, qué huella dejan y qué te hacen a vos como ingeniero.

---

**FIN DE LA PARTE 2 — El agente por dentro y el rol del ingeniero**
