# 🎓 APUNTE MAESTRO — Seminario "Proceso de diseño empleando IA Generativa" · Encuentro 1
## Parte 5 — Madurez: los 8 niveles, y qué modelo usar para cada tarea

**Unidad:** `seminario-ia-encuentro1` · **Parte 5 de 6** · Diseño de Sistemas de Información — UTN FRBA, 2026

> Hasta acá hablamos de cosas técnicas (modelo, harness, ventana) y de cosas filosóficas (nuestro rol, los riesgos), y de las herramientas (skills, MCP, rules, hooks, subagentes). Ahora, **ubicarnos**: ¿dónde estamos parados, nosotros o una organización, y cuál es el próximo paso? Eso cierra el Bloque 2. Después abre el Bloque 3 con una pregunta práctica: **qué modelo usar** para cada cosa.

---

## 1. 🔴 Antes de subir: qué es el agente, ahora con todas las piezas

Ya podemos decirlo completo. **El agente es el modelo más todo el harness que ponemos alrededor.** Todo ese andamiaje es lo que nos da mejor manejo de contexto y mejor resultado, y el seminario entero trata de mejorarlo. **El nivel de madurez es, en el fondo, cómo es ese harness:** si usa skills, si usa planes, qué más tiene.

| Pieza | Qué hace |
|---|---|
| **Modelo** | Predice palabra a palabra y **decide cuándo llamar** a una herramienta u otra. **No las ejecuta**: se lo pide al harness. |
| **Harness** | **Ejecuta** las herramientas, se conecta con los MCPs, maneja las skills. |
| **Contexto** | Lo que **nosotros** le brindamos. |

Todo esto hace al trabajo que tenemos nosotros.

---

## 2. 🔴 La pirámide: ocho niveles de delegación

```
                        ╱╲
                       ╱ 7╲        ORQUESTACIÓN DE AGENTES
                      ╱────╲
                     ╱  6   ╲      EVALS
                    ╱────────╲
                   ╱    5     ╲    LOOPS (cron)
                  ╱────────────╲
                 ╱      4       ╲  SEGUNDA MEMORIA (compartida)
                ╱────────────────╲ ─────────────────────────────  frontera actual
               ╱        3         ╲  ENTORNO: iterar el harness
              ╱────────────────────╲
             ╱          2           ╲  ARTEFACTOS: rules, skills, MCP
            ╱────────────────────────╲
           ╱            1             ╲  PLAN con pasos verificables
          ╱────────────────────────────╲
         ╱              0               ╲  VIBE CODING
        ╱────────────────────────────────╲
```

Los niveles 0 a 3 son los que hoy recorre la mayoría; los 4 a 7 son la frontera, y el seminario los presenta para investigar.

### 2.1 Nivel 0 — Vibe coding

Abrís el IDE, abrís un chat y decís: *"cambiame esta funcionalidad"*. Copiar y pegar texto, autocompletado.

¿Está mal? No: *la estás rockeando*. Pero **tenés un techo mucho más grande** de cosas que se pueden hacer. Lo que caracteriza este nivel: **no hay contexto que se mantenga entre sesiones.** Trabajás en una única sesión, te vas a dormir, volvés al día siguiente y **arrancás de cero**: nada de lo anterior existe.

> **Glosa —** *vibe coding*: programar "a sensación", pidiéndole cosas al chat sin estructura ni plan.

### 2.2 Nivel 1 — Plan y pasos verificables

Seguís sin contexto entre sesiones, pero **le das formato** a la conversación: generás un **plan**, con **pasos verificables**, y empezás a **orquestar modelos** dentro de esa conversación:

```
   modelo de FRONTERA (el que mejor puntúa)  ──►  arma el PLAN
                                                     │
   modelo BALANCEADO (más barato)            ──►  implementa los pasos verificables
```

Pero seguís en una única sesión: el contexto no persiste.

### 2.3 Nivel 2 — El conocimiento vive en artefactos

Acá empieza a crecer: **lo que sabés pasa a vivir en artefactos** (Parte 4):

- **Rules** (`CLAUDE.md`, `AGENTS.md`; cada proveedor tiene su abstracción): el contexto que **se mantiene** entre sesiones.
- **Contenido reutilizable:** las tareas que se volvían repetitivas van a **skills**.
- **Conexión a sistemas externos:** cuando querés conectarte a una herramienta, ya no le mandás una conexión ni un link a mano: tenés un **MCP**.

Sobre el MCP vale una precisión: **no es más que ponerle una capa exterior a un sistema y ofrecer tools en vez de endpoints.**

```
   SIN MCP                                  CON MCP
   ────────────────────────                 ───────────────────────────────────
   el sistema expone ENDPOINTS              una capa expone TOOLS
   (rutas HTTP que vos conocés)             el agente pregunta "¿qué tools hay?"
                                            el MCP responde con la lista
                                            el agente decide cuál llamar
```

Un conjunto de tools que permiten interactuar con un sistema externo. Eso es todo.

### 2.4 Nivel 3 — Iterar el entorno

Donde está hoy la mayoría de las empresas de punta. Ya tenés un **entorno pensado para que el agente trabaje bien**: las skills, los MCPs, el trabajo con plan, todo **en un mismo harness**, usado a diario.

Y hay un cambio de mentalidad que es lo importante del nivel: **no te enfocás en corregir la salida.** Tuviste un problema, no lo hizo como esperabas, alucinó... sí, lo corregís. Pero lo que hacés en el nivel 3 es ir a **diseñar una solución en el harness** (una skill, una rule, un hook) **que asegure que eso no vuelva a suceder.** Trabajás e iterás el entorno constantemente para tener mejor resultado.

> **Para el parcial, si te preguntan:** *¿Qué diferencia a alguien en el nivel 3 de alguien en el nivel 2, si los dos usan skills y MCP?*
> La actitud ante el error. En el nivel 2 el conocimiento ya vive en artefactos (rules, skills, MCP), pero cuando el agente falla se corrige la salida. En el nivel 3 el harness se trata como un sistema que se diseña e itera: ante una falla se agrega o ajusta la skill, la rule o el hook que impide que se repita. Se corrige el sistema, no el resultado.

### 2.5 Hacia dónde va: los cuatro niveles de frontera

Hasta el nivel 3 desarrollamos software **y** el entorno donde trabajan los agentes. Lo que se suma ahora es **desarrollar agentes**: cómo trabajan varios agentes juntos para construir un producto escalable, constantemente. Cuatro conceptos, presentados para investigar:

#### Nivel 4 — Segunda memoria (memoria compartida)

Varias sesiones, o varios agentes, trabajan sobre un mismo producto: por ejemplo, el proyecto del front y el proyecto de la API, y querés que cierto conocimiento sea **compartido** entre esos agentes. La solución: un **repositorio de documentos** (archivos `.md`) que funciona como **segunda memoria**: los agentes la consultan, **agregan sus aprendizajes** y la información de las features que desarrollan. El conocimiento queda democratizado en un solo lugar. Herramientas más exploratorias, como **Obsidian**, permiten usar esos archivos como un **grafo** y consultarlos de forma más determinística y rápida.

> **Glosa —** *Obsidian*: aplicación de notas en Markdown que enlaza los archivos entre sí y los muestra como grafo.

#### Nivel 5 — Loops (cron)

Un **loop** es levantar un agente **cada cierto tiempo** con un **cron**: el mismo scheduler que se ve en la materia, que ejecuta algo cada X tiempo o en un horario. Ejemplo: trabajás con dependencias que pueden volverse vulnerables; una vez por semana se levanta un agente que **analiza las versiones**, busca vulnerabilidades y las fixea. Para tareas **programadas y repetitivas**.

> *Es el mismo cron de `@Scheduled` de la materia; acá el trabajo programado es un agente.*

#### Nivel 6 — Evals

Un **eval** es un **test que le hacemos a un agente** que tenemos productivo, o a una **skill**, para validar que da el **resultado esperado**. Formas de hacerlo:

- un **modelo más avanzado** que el que usamos evalúa la respuesta,
- un sistema **determinístico** verifica que la respuesta tenga ciertas características o ciertas palabras.

Caso típico: un equipo grande comparte una skill para todo el frontend, para mantener reglas comunes. El eval garantiza que esa skill es **productiva**, que la pueden usar varias personas y va a dar un resultado eficaz. Hay muchas estrategias, con criterios ingenieriles interesantes para investigar.

#### Nivel 7 — Orquestación de agentes

Lo que está saliendo ahora; el nivel actual de madurez en la industria. Se construye con los tres anteriores más algunas piezas:

| Ingrediente | Para qué |
|---|---|
| **Cron (loop)** | Disparar agentes para tareas repetitivas. |
| **Evals** | Validar que funcionan. |
| **Memoria externa** | Que compartan lo que aprenden. |
| **Harness adecuado** | Para ejecutar agentes con tareas **muy específicas**: uno para tests, otro para validar código, otro para agregar features. |
| **Goals (objetivos) claros** | La salida que esperás de cada agente. Dos agentes sobre un mismo proyecto sin una salida clara **se chocan, inventan, alucinan**. Cómo especificar es el tema de la Parte 6. |
| **Monitor** | Un agente **escuchando eventos**: ante una alerta del sistema, se levanta, investiga qué pasó y entrega su análisis. |
| **Métricas de rendimiento** | ¿Esta cantidad de agentes me está funcionando? ¿Estoy gastando mucha plata? ¿Lo haría yo más fácil? |

En resumen, para automatizar un flujo con agentes autónomos hacen falta tres cosas:

```
   DISPARADOR            +     EVALUACIÓN         +     MEMORIA COMPARTIDA
   (cron: cada X tiempo         (¿funciona de verdad?      (varios agentes leen,
    monitor: ante un evento)     se mejora con el tiempo)   escriben, dejan registro)
```

Todo lo que se ve en la facultad termina relacionado con este tipo de desarrollo. Y nuestro rol ya no es solo hacer un producto que funcione: es hacer que **los agentes funcionen cada vez mejor**, para dedicarnos a las decisiones críticas.

---

## 3. 🔴 Qué modelo usar: no existe "el mejor"

### 3.1 El amigo del modelo de moda

Todos conocemos a ese amigo (o somos nosotros): necesita una tarea, agarra **el modelo que mejor califica hoy en X**, el que está en las noticias, y lo usa **para todo**.

Es erróneo, y por la misma base de siempre: como ingenieros resolvemos problemas de manera **eficaz y eficiente**. ¿Por qué usar constantemente un modelo que sabemos que es más caro? Consume más recursos, y más plata. Y aunque te dé igual porque pagás una cuota fija de 20 dólares, **esos recursos son los mismos que después necesitás para desarrollar las funcionalidades.** Hay que aprovecharlos al máximo y saber administrarlos.

**Regla práctica: el modelo que mejor resuelva la tarea, de la forma más barata.** Todos los problemas convergen a lo mismo.

Un enfoque que ya viste en el nivel 1: **un modelo razonador planifica, un modelo más económico implementa** los pasos verificables. Se trata de decidir cuándo conviene un razonador y cuándo uno liviano.

### 3.2 No existe un modelo óptimo

"El mejor modelo" es el mejor... ¿para qué? ¿Para resolver todas las tareas? Si lo que necesito es **clasificar algo muy puntual**, que requiere procesar un par de líneas, no necesito el mejor modelo: **necesito el que mejor se ajusta a esa tarea.**

### 3.3 Cuatro dimensiones para clasificar modelos

| Dimensión | Un extremo | El otro extremo |
|---|---|---|
| **Acceso** | **Abierto:** podés acceder a los pesos (a veces no), tenés acceso más profundo al modelo (Parte 3, §4). | **Cerrado:** siempre hablás con una **API propietaria**; la organización dueña controla el modelo. |
| **Razonamiento** | **Liviano.** | **Razonador / pesado:** más cálculo en prefill y decode (Parte 1, §4), más GPU y memoria, **más recursos**, generalmente de una computadora en la nube. |
| **Dónde corre** | **Local:** en tu máquina. | **Remoto:** en la nube de un tercero. |
| **Alcance** | **Generalista:** sirve para la mayoría de las tareas. | **Especializado:** afinado para una tarea particular. |

Sobre la última dimensión aparece el **fine-tuning**: **afinar** el funcionamiento de un modelo para que se adapte a una tarea particular. Ejemplo: hay un modelo de Amazon enfocado en medicina. Estos modelos **pierden amigabilidad**, o tienen cosas que no encajan con los modelos conversacionales: están hechos para resolver **su** tarea de forma eficiente, no para charlar con cualquier humano o cliente.

> 🕳️ **Madriguera — Fine-tuning**
> Volver a entrenar un modelo ya entrenado con datos de una tarea específica para que rinda mejor ahí. Cómo se hace es tema aparte.
> *Volvé al camino — esto se profundiza aparte, otro día.*

### 3.4 Las gamas: qué modelo para qué tarea

```
   costo por millón de tokens
        ▲
        │                                              ● EXTREMO
        │                                    ● PREMIUM
        │                       ● BALANCEADO
        │            ● ECONÓMICO
        └──────────────────────────────────────────────────► razonamiento
```

El costo por millón de tokens crece **casi en relación directa** con el razonamiento, por lo de siempre: más cálculo, más recursos, salida más cara.

| Gama | Para qué se usa |
|---|---|
| **Económico** | Análisis puntuales de archivos de código, buscar información, resúmenes de archivos. |
| **Balanceado** | Implementar, testing, revisar una implementación que hizo otro agente. |
| **Premium** | Definir la arquitectura, diseñar, planificar. |
| **Extremo** | Razonamiento muy avanzado, muy creativos; consumen muchos más tokens. **Solo** cuando se necesita ese razonamiento o cuando **el costo del error es demasiado caro**. |

Ejemplos de la gama premium: en Claude, **Opus**; en Gemini, **Gemini Pro**; en Codex, su modelo de tope de gama. Los nombres cambian cada semana; la gama es lo que importa.

### 3.5 Los modelos también son componentes

Todo el seminario habla de modelos **como compañeros** de desarrollo. Pero **también pueden integrarse como componentes** de nuestras aplicaciones. Un caso reciente: un modelo que **deja de lado lo conversacional** y vuelve a las bases del machine learning, a los modelos que **clasifican y puntúan** en base a una probabilidad. Simplemente clasifica sobre un conocimiento existente y devuelve una puntuación. Eso es un componente de software, no un chat.

> **Glosa —** *machine learning*: la rama de la que salen los modelos de lenguaje; "clasificar y puntuar" es su uso más clásico.

### 3.6 Local vs. remoto: dónde corre

Cuando corrés un modelo en la computadora de un tercero, **compartís información** por necesidad: le tiene que llegar para funcionar (Parte 3). Entonces:

| | Local | Remoto |
|---|---|---|
| **Quién paga los recursos** | Vos, con tu máquina. | Otro (el proveedor). |
| **Cuál es el costo real** | Recursos propios. | **Qué datos** le estás compartiendo a ese tercero. |
| **Caso de uso** | Una clasificación simple para un desarrollo local: no necesitás pagar la API de OpenAI; usás un modelo local y te comunicás directo con él. | Tareas que exceden tu máquina. |

> **Para el parcial, si te preguntan:** *Tenés que (a) definir la arquitectura de un servicio nuevo, (b) implementar los endpoints ya planificados y (c) clasificar 10.000 comentarios de usuarios en positivo/negativo dentro de tu app. ¿Qué modelo usás en cada caso y por qué?*
> (a) Premium: es diseño y planificación, donde el razonamiento vale y el costo del error es alto. (b) Balanceado: la implementación ya tiene plan y pasos verificables; un modelo caro sería gasto sin beneficio. (c) Un modelo económico, especializado si existe, y probablemente local, porque es una clasificación puntual de pocas líneas, corre como componente de la app y los datos de usuarios no deberían salir. La regla es siempre la misma: el más barato que resuelva bien la tarea.

---

## Síntesis de la Parte 5

| Idea | En una línea |
|---|---|
| Agente completo | Modelo (decide) + harness (ejecuta) + contexto (lo damos nosotros). |
| Nivel 0 | Vibe coding: sin contexto entre sesiones. |
| Nivel 1 | Plan con pasos verificables; razonador planifica, barato implementa. |
| Nivel 2 | Rules, skills, MCP: el conocimiento vive en artefactos. MCP = tools en vez de endpoints. |
| Nivel 3 | Iterar el harness: corregir el sistema, no la salida. |
| Nivel 4 | Segunda memoria compartida entre agentes (repo de `.md`, Obsidian). |
| Nivel 5 | Loops: cron que levanta agentes para tareas repetitivas. |
| Nivel 6 | Evals: tests a agentes y skills (por modelo superior o determinísticos). |
| Nivel 7 | Orquestación: cron + evals + memoria + harness específico + goals + monitor + métricas. |
| Modelos | No hay óptimo: el más barato que resuelva bien la tarea. |
| Dimensiones | Abierto/cerrado · liviano/razonador · local/remoto · generalista/especializado. |
| Gamas | Económico → balanceado → premium → extremo; el costo sigue al razonamiento. |
| Componentes | Un modelo también puede ser una pieza de tu sistema (clasificar, puntuar). |

---

## ✅ Checkpoint (sin respuestas)

1. ¿En qué nivel de la pirámide estás vos hoy, con qué evidencia, y qué te falta concretamente para subir uno?
2. "El MCP no es más que tools en vez de endpoints." Explicá esa frase como si se la tuvieras que dictar a un compañero que solo conoce REST.
3. Un agente te devolvió tres veces tests sin asserts. ¿Qué haría alguien de nivel 2 y qué haría alguien de nivel 3?
4. Dos agentes trabajan a la vez sobre DonaTrack: uno en el servicio de Donaciones y otro en el de Logística. ¿Qué dos cosas del nivel 7 necesitás para que no se choquen?
5. ¿Qué diferencia hay entre un hook (Parte 4) y un eval? ¿Cuál corre antes de la acción y cuál después?
6. ¿Por qué pagar una cuota fija no elimina el argumento de "usá el modelo más barato que resuelva la tarea"?
7. Un modelo especializado en medicina "pierde amigabilidad". ¿Por qué eso no es un defecto?
8. ¿Cuándo tiene sentido correr un modelo local aunque sea peor que el remoto? Da dos motivos de dos partes distintas del apunte.

---

## Qué viene en la Parte 6

El Bloque 3 termina con **metodología**. Qué es un método y por qué la IA lo volvió barato; **Spec-Driven Development**: la spec como fuente de verdad, sus componentes, las cinco etapas con intervención humana, skills que encapsulan el método y hooks que impiden saltear etapas, cuándo sí y cuándo no, dónde falla. Y el cierre del encuentro: las preguntas que la IA no puede responder por vos.

---

**FIN DE LA PARTE 5 — Madurez: los 8 niveles, y qué modelo usar para cada tarea**
