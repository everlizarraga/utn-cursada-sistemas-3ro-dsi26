# 🎓 APUNTE MAESTRO — Seminario "Proceso de diseño empleando IA Generativa" · Encuentro 1
## Parte 4 — Context engineering y skills

**Unidad:** `seminario-ia-encuentro1` · **Parte 4 de 6** · Diseño de Sistemas de Información — UTN FRBA, 2026

> Arranca el **Bloque 2: cómo diseñar un agente efectivo**. Ya sabemos qué es un modelo, cómo lo rodea el harness, que la ventana de contexto es finita y se acumula, y que delegar tiene riesgos. Ahora la pregunta es constructiva: **cómo le damos al agente el contexto correcto** para que itere mejor, dé buenos resultados y no cueste tanto. Primero la disciplina y su mapa de herramientas; después la herramienta central, las **skills**, en profundidad.

---

## 1. 🔴 Context engineering: la disciplina

### 1.1 Un caso: la presentación que uno armó y otro quiere seguir

Una persona trabaja varias sesiones con un agente sobre el repositorio de una presentación. Toma decisiones, descarta alternativas, ajusta el estilo. Al día siguiente, un compañero abre el mismo repo con su propio agente para seguir iterando. **Le falta todo el contexto** que el primero construyó en sus sesiones: las decisiones no están en ningún lado más que en una conversación que ya no existe para ese agente.

Ese es un problema de contexto. Y **context engineering** (ingeniería de contexto) es la disciplina que se ocupa de eso: **pensar en qué momento le doy un contexto al agente, cómo se lo doy y qué contexto le doy.** Abarca un conjunto de herramientas específicas de los agentes (skills, rules, hooks, MCP, subagentes) que vemos en §3.

El objetivo es doble: **mejor performance** (mejores resultados, menos alucinaciones) y **recursos optimizados** (menos costo).

### 1.2 Los problemas que el contexto tiene que resolver

| Problema | Qué significa |
|---|---|
| **Recursos finitos** | La ventana de contexto está limitada a lo que el modelo puede procesar (Parte 2, §4). |
| **Capacidades acotadas** | No todos los agentes usan las mismas herramientas; no todos pueden, por ejemplo, interactuar con un browser. |
| **El código no refleja las decisiones de producto** | Las decisiones más importantes no están escritas en el código; alguien tiene que dárselas al agente cuando hacen falta. |
| **Conocimiento ausente** | Lo que el agente no sabe del producto y del negocio (Parte 2, §8). |
| **Memoria no compartida** | Entre sesiones, o entre personas de un equipo que trabajan sobre el mismo código. El caso de arriba. |
| **Ruido que distrae** | La tentación es darle **más** contexto. Pero hay contexto que no sirve para ese momento, o que está **desactualizado** (documentación vieja). Eso empeora los resultados. |

---

## 2. 🔴 Gestionar las conversaciones

Todos los proveedores permiten resolverlo de la misma forma. Cinco prácticas:

### 2.1 Un chat por tarea

Estás iterando una feature (un endpoint nuevo, un flujo nuevo) y en paralelo aparece otra tarea. **No uses ese mismo chat.** Creá uno nuevo, para que el contexto de la feature no se ensucie con lo nuevo. Es el concepto más básico y el primero.

### 2.2 Compactación

Cuando la ventana de contexto llega a un punto alto, hay tokens ocupando la memoria del agente, y en cada iteración nueva es posible que **se pisen** para hacer lugar a lo nuevo. **Compactar** es pedirle al agente que **resuma** su ventana de contexto, indicándole **explícitamente qué conceptos guardar**, y seguir iterando desde ahí.

Dos beneficios: reduce costos, y mejora resultados, porque **cuanto más llena está la ventana, más posibilidades de alucinar.**

### 2.3 Scripts determinísticos

Diferenciar **cuándo usar un agente y cuándo un script**. Un script hace siempre lo mismo, sin razonar; hay tareas donde eso es exactamente lo que queremos. Se desarrolla en §4.4.

### 2.4 Mejorar el prompt

Antes de mandar un prompt, preguntarse cómo mejorarlo. Ser **muy específico** con:

- **qué salidas** esperás,
- **qué resultados** querés que valide: no solo "corré los tests y que buildee", sino "probá este flujo completo", y si es un frontend, "validá con imágenes".

Un resultado preciso y concreto evita que el agente se vaya por las ramas.

### 2.5 Agent loops y evals

Dos conceptos más complejos (loops de agentes y evaluaciones). Se ven en la Parte 5.

---

## 3. 🔴 El mapa: cuatro propósitos, siete herramientas

Las herramientas que los ADE ofrecen alrededor del agente se pueden agrupar por **propósito**:

```
                     ┌───────────────────────────────────┐
                     │  FLUJOS                           │
                     │  comandos · skills                │
   ┌─────────────────┼───────────────────────────────────┼─────────────────┐
   │  CALIDAD Y      │                                   │  CONTROL Y      │
   │  CONTEXTO       │        HERRAMIENTAS               │  SEGURIDAD      │
   │  skills · MCP   │        AGÉNTICAS                  │  hooks          │
   │  rules          │                                   │                 │
   └─────────────────┼───────────────────────────────────┼─────────────────┘
                     │  EFICIENCIA Y PARALELISMO         │
                     │  subagentes                       │
                     └───────────────────────────────────┘
```

### 3.1 Flujos: comandos y skills

Secuencias de pasos **que son siempre las mismas** y que el agente puede ejecutar. *Comando* y *skill* se usan de la misma forma; solo cambia el nombre según la herramienta.

La diferencia con un script: dentro de la secuencia puede haber una **validación no determinística**, es decir, un paso donde el agente razona en vez de seguir una entrada y una salida precisas.

Ejemplo: querés validar ciertas características de la base de datos, cuántos usuarios están comprando cierta mercadería, y que el agente lo **analice**. Lo ponés en una skill, y todos los días consultás cómo vienen esas consultas.

### 3.2 Calidad y contexto: skills, MCP y rules

**Skills (visto por arriba; §4 en profundidad).** Documentación que el agente **va descubriendo a lo largo del tiempo**, sin leer un documento entero: archivitos separados en una carpeta, cada uno con una descripción muy clara para que el agente sepa cuándo usarlo.

**MCP (Model Context Protocol).** Un **protocolo** que permite al agente comunicarse con **herramientas externas**: Google Drive, Figma, Jira. El agente usa herramientas que no están en su entorno, le pide a ese sistema externo que haga cosas y trae información. Sirve para **consultar información externa** y para **ejecutar tools externas**. Nada más que eso.

> **Glosas —** *Figma*: herramienta de diseño de interfaces. *Jira*: herramienta de gestión de tareas.

**Rules (reglas, políticas).** Las conocés como **`AGENTS.md`** o **`CLAUDE.md`**: un archivo donde ponés políticas que el agente **siempre** tiene que seguir. Ejemplos: "escribí siempre en snake_case", "hacé los tests de esta forma específica", "llegá al 90 % de cobertura en todos los cambios".

> **Glosas —** *snake_case*: convención de nombres con guiones bajos (`calcular_total`). *cobertura*: porcentaje del código que los tests ejercitan.

**Cuidado:** las rules caen en la memoria del agente y son **parte del contexto que se manda siempre**, junto al system prompt y al prompt del usuario, en cada vuelta del ciclo agéntico (Parte 2, §2.3). Cada línea que agregás la pagás en todas las iteraciones.

### 3.3 Control y seguridad: hooks

**Scripts completamente determinísticos** que se ejecutan **ante una acción del agente**:

- cuando ejecuta una herramienta,
- cuando el usuario manda un prompt,
- cuando el agente responde,
- cuando inicia una sesión (el hook de *session start*: "cada vez que arranca una sesión, que pase X").

Con hooks podés **cargar o actualizar el system prompt**, darle una instrucción específica en un momento dado a partir de ciertas validaciones, o **capturar qué tool se está usando**. No tienen mucho uso todavía, pero son la herramienta para **validación, control y seguridad** del propio agente. Es la forma determinística de poner los límites de la Parte 3, y en la Parte 6 son las compuertas entre etapas de una metodología.

### 3.4 Eficiencia y paralelismo: subagentes

Los agentes tienen una ventana de contexto. ¿Y si el agente pudiera **delegar una tarea a otro agente, con otra ventana**?

```
   AGENTE PADRE (orquesta)
   ventana propia: la conversación con vos
        │
        ├──► SUBAGENTE A  (ventana propia · modelo específico · tools limitadas)
        │       tarea acotada, en paralelo
        │
        └──► SUBAGENTE B  (ventana propia · ...)
                tarea acotada, en paralelo
```

Ventajas: **paralelizar**, y que el agente padre **no ensucie su propia ventana** con el detalle de cada tarea, lo que también significa **menos costo** del lado del padre, que es el que maneja toda la interacción con tu sistema. Un subagente hace una tarea específica, con un modelo específico y limitado a herramientas específicas. A veces el agente los lanza solo; otras se lo pedís vos.

> **Para el parcial, si te preguntan:** *Tenés que hacer que un agente valide todos los días cuántos pedidos quedaron en estado "pendiente" más de 48 hs y te diga si el patrón es normal. ¿Qué herramienta del mapa usás y por qué no las otras?*
> Una skill (flujo): es una secuencia siempre igual (consultar la base, agrupar por estado y antigüedad) que termina en una validación no determinística (interpretar si el patrón es normal), que es lo que un script no puede hacer. No es una rule, porque no es una política permanente sino una tarea; no es un hook, porque no responde a una acción del agente; y un MCP solo haría falta si la base viviera en un sistema externo al que el agente no accede.

---

## 4. 🔴 Skills, en profundidad

### 4.1 El camino que todos hacemos

Día 1. Le pedís al agente: *"creá un endpoint para dar de alta usuarios"*. Te da un resultado.
Día 2. Le pedís lo mismo. Te da **otro** resultado.
Día 3. Otro distinto.

Es lógico: es probabilístico (Parte 1, §2). No importa cuál esté mejor; importa que **son distintos**, y eso no es lo que queremos. Queremos que trabaje **bajo nuestro criterio**, nuestra forma de pensar y de trabajar.

La solución que todos encontramos: **decirle siempre lo mismo.** El párrafo de siempre: cómo queremos que trabaje, cómo gestione errores, cómo piense. Lo escribimos en cada sesión. Una, dos, tres. Y se vuelve **tedioso**: cada vez lo escribimos distinto, hay que pegarlo, actualizarlo, mantenerlo, pasárselo a un compañero.

Y aparece un segundo problema: ¿cómo hago para que el agente **pueda leerlo siempre** y lo tenga al alcance?

Ahí aparece la skill. **Una skill es nuestro criterio de siempre, llevado a un lugar donde el agente puede ir a buscarlo, leerlo y ejecutarlo.**

Ventajas, que son exactamente los problemas de arriba resueltos:

- está escrito **una sola vez**,
- **se versiona con el código**,
- **todos tienen acceso**.

### 4.2 La skill no se queda quieta

Nuestro criterio cambia con el tiempo; la skill también. Es dinámica, y se itera:

```
   CREAR ──► USAR ──► VER EL RESULTADO ──► EVALUARLO ──► NUEVO CRITERIO ──┐
     ▲                                                                    │
     └───────────────────────── MEJORAR LA SKILL ◄────────────────────────┘
```

Y se puede hacer **con el mismo agente**: *"usé esta skill, me dio este resultado, prefiero que estas cosas cambien, esto no lo hizo tan exacto"*. Con el tiempo, la skill mejora.

### 4.3 Anatomía

Una skill tiene dos partes:

```markdown
---
name: crear-endpoint                      # ← ENCABEZADO: nombre
description: Cómo creo endpoints HTTP     #    descripción (la más importante, ver §4.6)
  en este proyecto. Usar cuando se pida
  crear o modificar un endpoint.
model: balanceado                         #    a veces: qué modelo usar
effort: medio                             #    a veces: cuánto esfuerzo
allowed-tools: [read, grep, edit]         #    a veces: herramientas permitidas / no
---
                                          # ← CUERPO: texto puro con tu criterio
Antes de escribir código, preguntame el nombre del recurso
y qué validaciones necesita.
Los errores se manejan con excepciones propias del dominio,
nunca con códigos numéricos sueltos.
Todo endpoint nuevo lleva su test de integración.
```
*(Ejemplo ilustrativo. Los nombres exactos de los campos varían según la herramienta; lo importante es la separación encabezado / cuerpo.)*

- **Encabezado:** nombre, descripción, y a veces modelo, esfuerzo y herramientas permitidas o prohibidas.
- **Cuerpo:** texto puro, **tu criterio**: qué querés que piense, cómo querés que vea las cosas, qué necesita **preguntar y saber antes de actuar**.

Hay muchas formas de hacerlo bien. Un atajo útil al empezar: buscar una **skill para crear skills**, y así desligarse del formato y aportar solo las ideas.

### 4.4 Skill vs. script

Una skill **no es solo texto**: puede contener **scripts** y otros archivos.

¿Cuándo un script y no una skill? Cuando la serie de pasos es **determinística**, la conocemos, y **no nos interesa que el agente piense**: levantar un entorno, correr migraciones, generar el changelog, formatear código.

| | Script | Skill |
|---|---|---|
| **Pasos** | Fijos y conocidos | Fijos, pero con razonamiento en el medio |
| **¿El agente piensa?** | No | Sí, donde hace falta |
| **Ejemplos** | Levantar entorno, migraciones, changelog, formateo | Crear un endpoint con mi criterio, analizar consultas |

Y se combinan: la skill **aprovecha** el script (primero ejecuta el script para obtener resultados, después trabaja sobre ellos) y puede **referenciar otros archivos**.

### 4.5 El día de las 30 skills

Alguien descubre las skills y piensa: *"creo 30 para mi proyecto, va a funcionar mejor"*. Y funciona mejor. **La primera vez.** La segunda, no. La tercera, tampoco. Mirando el razonamiento del agente: la primera vez leyó la skill; las demás, no.

No es que el contenido esté mal escrito. Es que el agente **no sabe cuál elegir** de todas las que tiene, o **ni siquiera sabe que la tiene**.

### 4.6 Cómo hacer que la lea: tres disparadores

1. **La descripción** (del encabezado). Es la forma de **venderle** la skill al agente: qué hace, en qué momento se usa. Si no somos buenos vendiéndosela, nunca va a saber cuándo usarla.
2. **Palabras clave.** Términos que sabemos de antemano que vamos a usar en el lenguaje cotidiano con el agente: si siempre decimos *"creame un endpoint"*, *"modificá el endpoint"*, *HTTP*, esas palabras precargadas en la skill hacen que se dispare sola.
3. **Extensión de archivo.** Si el agente va a abrir un `.py`, se dispara la skill de nomenclatura en Python.

### 4.7 Por qué no mil skills

La tentación es obvia: mil skills, agente más inteligente, sabe hacer de todo. No es así, y el criterio para **elegir cada skill** es nuestro:

1. **Ocupan lugar siempre.** El **encabezado** (no el texto largo, solo el encabezado) **está siempre cargado en el contexto, se use o no.** Es poco, pero si se acumulan, degradan el funcionamiento general del agente.
2. **Compiten entre ellas.** Con mil skills es casi imposible que sean todas muy distintas. Cuando dos se parecen, el agente duda; un día elige una, otro día otra, y volvemos al problema inicial: resultados distintos.
3. **No todo pide criterio.** No se crea una skill para algo donde el agente no necesita pensar (eso es un script).
4. **Los modelos mejoran.** No es lo mismo un modelo de 2023 que uno de esta semana. **Cada modelo nuevo necesita menos skills**, menos ayuda, para generar mejores cosas.

### 4.8 Cuándo sí vale la pena

1. **Un criterio que le repetís.** El caso más común y el problema que la skill viene a resolver: "hiciste mal esto porque la gestión de errores en mi proyecto es de esta forma", "quiero que uses esta tecnología".
2. **Un problema que no querés que se repita.** Encontraste un error en el código o en la implementación; si no le decís cómo resolverlo **antes** de que suceda, es muy probable que vuelva.
3. **Un objetivo que costó lograr.** Tardaste tanto que los pasos exactos quedaron difusos. Aprovechás el contexto que el agente tiene en ese momento: *"armame una skill con todo lo que trabajamos hoy, estructurado, para replicarlo en el futuro"*.

### 4.9 Lo que hay que llevarse de las skills

- **Nace de una necesidad.** No se inventa porque sí: hay un problema que resolver. Toda la ingeniería de contexto nace así.
- **El encabezado siempre está cargado**, aunque no se use nunca. Por eso hace falta criterio en cuántas y cuándo.
- **No es solo texto:** scripts, otros archivos, referencias.
- **Criterio para escribirlas y para valorarlas.** Le damos al agente el mejor criterio posible, teniendo criterio nosotros.

Las skills son la base de lo que sigue: rules, la pirámide de madurez (Parte 5) y la metodología (Parte 6).

> **Para el parcial, si te preguntan:** *¿Cuándo escribís una skill y cuándo no, y qué costo tiene cada skill que agregás?*
> Escribo una skill cuando hay un criterio que le repito al agente, un problema que no quiero que se repita, o un objetivo logrado cuyos pasos quiero replicar. No la escribo si la tarea es determinística (va un script) ni si el agente no necesita pensar. Cada skill cuesta en dos frentes: su encabezado se carga en el contexto en todas las sesiones, se use o no, y si se parece a otra compite con ella y el agente duda. Por eso pocas skills, distintas entre sí y bien descriptas.

---

## Síntesis de la Parte 4

| Idea | En una línea |
|---|---|
| Context engineering | Qué contexto, cuándo y cómo. Para performance y para costo. |
| Problemas del contexto | Recursos finitos, capacidades acotadas, decisiones fuera del código, conocimiento ausente, memoria no compartida, ruido. |
| Conversaciones | Un chat por tarea · compactar · script vs. agente · prompt específico con salida verificable. |
| Flujos | Comandos/skills: pasos fijos con razonamiento en el medio. |
| Calidad y contexto | Skills (se descubren), MCP (tools externas), rules (siempre cargadas: cuidado). |
| Control y seguridad | Hooks: scripts determinísticos ante acciones del agente. |
| Eficiencia | Subagentes: otra ventana, en paralelo, el padre no se ensucia. |
| Skill | Tu criterio de siempre, en un lugar donde el agente lo lee. Una vez, versionado, compartido. |
| Anatomía | Encabezado (nombre, descripción, modelo, tools) + cuerpo (criterio). |
| Disparo | Descripción, palabras clave, extensión de archivo. |
| Cuántas | Pocas: el encabezado siempre está cargado y las parecidas compiten. |
| Cuándo | Criterio repetido · error a no repetir · objetivo logrado difuso. |

---

## ✅ Checkpoint (sin respuestas)

1. Estás en medio de una feature y te piden arreglar un bug en otro módulo. ¿Qué hacés con el chat actual y qué pagás si no lo hacés?
2. ¿Qué diferencia hay entre compactar y abrir una sesión nueva? ¿Cuándo elegirías cada una?
3. Clasificá en el mapa de cuatro propósitos: (a) "siempre usá tabs", (b) "antes de cada commit corré el linter", (c) "consultá los tickets de Jira", (d) "investigá el módulo de pagos mientras yo sigo con el front".
4. ¿Por qué una rule cuesta tokens en cada iteración y una skill solo cuando se usa? ¿Qué parte de la skill sí cuesta siempre?
5. Tu skill de "crear endpoint" no se dispara cuando escribís "agregá una ruta nueva". ¿Cuál de los tres disparadores fallaría y cómo lo arreglás?
6. Un compañero propone una skill para "correr las migraciones de la base". ¿Skill o script? Justificá.
7. Después de dos horas de pelearte con una configuración de Docker, finalmente anduvo. ¿Qué le pedís al agente en ese momento y por qué justo en ese momento?
8. ¿Por qué el seminario dice que los modelos nuevos "necesitan menos skills"? ¿Qué implica eso para mantener las que ya tenés?

---

## Qué viene en la Parte 5

Con las herramientas en la mano, la Parte 5 responde **dónde estás parado**: una pirámide de **8 niveles de madurez**, desde el vibe coding hasta la orquestación de agentes con memoria compartida, loops y evals. Y después, **qué modelo usar** para cada tarea: dimensiones, gamas y la regla del más barato que resuelva bien.

---

**FIN DE LA PARTE 4 — Context engineering y skills**
