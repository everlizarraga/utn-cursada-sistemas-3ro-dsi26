# 🎓 APUNTE MAESTRO — Seminario "Proceso de diseño empleando IA Generativa" · Encuentro 1
## Parte 6 — Metodología: Spec-Driven Development, y el cierre del encuentro

**Unidad:** `seminario-ia-encuentro1` · **Parte 6 de 6** · Diseño de Sistemas de Información — UTN FRBA, 2026

> Última parte. Salimos un momento del scope tecnológico para ir a los **enfoques metodológicos**: suena menos llamativo, pero no es menos importante, y es donde todo lo anterior (skills, hooks, subagentes, elección de modelos) encaja en un flujo de trabajo. Después, el **cierre del encuentro**: los componentes que siempre están, las preguntas que la IA no responde por vos y cómo seguir.

---

## 1. 🔴 Qué es un método y para qué sirve

### 1.1 El método científico como ejemplo

Todos lo vimos en el secundario: observación, hipótesis, experimento, conclusión. **Cada etapa está decidida de antemano**: en cada una sabemos qué vamos a hacer, qué vamos a pensar, qué vamos a resolver. Y no hace falta argumentar cuán efectivo fue: permitió una cantidad enorme de avances, conclusiones correctas y descartar hipótesis incorrectas.

Eso es un **método**: **decidir el orden antes de empezar.** El orden de los pasos, de las ejecuciones, de los pensamientos. Darle una **estructura** al trabajo. Y un **enfoque metodológico** es aplicar un método para lograr un objetivo.

### 1.2 Cuatro beneficios

| Beneficio | Qué te da |
|---|---|
| **Predecible** | Como está todo estructurado de antemano, sabés en qué paso estás, cuáles pasaron y cuáles siguen. |
| **Trazable** | Entendés dónde estás, qué se hizo, qué se decidió, qué se trabajó. |
| **Transferible** | Consecuencia directa de las dos anteriores: avanzaste hasta cierto punto y se lo podés pasar a otro sin mucho esfuerzo. ¿Cuántas veces, en un equipo de la facultad, explicar lo que pasó y lo que sigue tardó más que el trabajo en sí? |
| **Barato** | El más valioso. **No** es barato de construir, sostener ni trabajar. Es barato porque **los errores aparecen en la etapa más temprana posible: donde pertenecen.** |

Sobre el último: cuántas veces, desarrollando, encontramos un bug y descubrimos que no era un error de implementación sino **del requerimiento, del diseño o del plan de tareas**: un error anterior que consumió mucho más tiempo por no detectarlo a tiempo. El método lo hace aparecer en su lugar.

### 1.3 Los métodos que ya existían

En diseño y desarrollo de software hay cuatro conocidos, que parecen un trabalenguas: **TDD, BDD, DDD y SDD**. Cada uno pone su valor principal en algo:

| Método | Enfoca el trabajo en... |
|---|---|
| **TDD** (Test-Driven) | los **tests** |
| **BDD** (Behavior-Driven) | el **comportamiento** |
| **DDD** (Domain-Driven) | el **negocio** (el dominio) |
| **SDD** (Spec-Driven) | las **especificaciones** |

> **Glosa —** *driven*: "guiado por". TDD es desarrollo guiado por los tests, y así con los demás.

### 1.4 Por qué casi nadie los usaba antes de la IA

Muchos conocen estos métodos; menos los usaron; **muy pocos los usaban antes de la IA**, y bastantes más los usan después. ¿Por qué? No era pereza: era **economía básica**.

Los beneficios se conocían (errores tempranos, cada decisión escrita, cada cambio se propaga). Pero el **costo de entrada** era muy alto: redactar y formatear documentos, mantenerlos actualizados, propagar cualquier cambio, retroceder en el proyecto para modificar algo. Ese tiempo nublaba la visión de las ventajas.

**Lo fascinante de la IA es que reduce ese costo de entrada y de mantenimiento.** Redactar pasó de días u horas a **minutos**. Mantener la documentación actualizada se hace **de forma automática dentro de la conversación** de trabajo. Eso es lo que hoy nos permite sostener métodos que antes eran inviables.

---

## 2. 🔴 Spec-Driven Development

### 2.1 Qué es

SDD hizo boom hace alrededor de un año, aunque la idea existe hace mucho. Es **pensar la spec (la especificación) antes que el código**: primero las especificaciones, y después tratarlas como **fuente de verdad** y como **contrato verificable**.

Antes se pasaba de la idea al código. SDD **obliga a pensar de antemano** los requerimientos, los casos de uso, lo que hay que cumplir.

No es nuevo: los **métodos formales** existen desde los años 70; en 1986 nació **Design by Contract**; la idea de SDD circula desde mediados de los 2000. ¿Por qué no se globalizaron? Primero, la información no llegaba a la gente; después, eran **tan complicados de empezar** que resultaban inútiles. Hoy SDD es furor **no porque estemos haciendo algo loco, sino porque volvimos a las bases** gracias a un disruptor que lo hizo posible.

> **Glosa —** *Design by Contract*: diseñar cada pieza de software declarando qué exige (precondiciones) y qué garantiza (postcondiciones), como un contrato.

### 2.2 De un prompt suelto a una spec

**Prompt suelto:** *"hacé una pantalla de login"*. Es una mala forma de pedir algo, porque **delegás todo**: la forma de hacerlo, de pensarlo, de ejecutarlo, de gestionarlo, la tecnología. Todo.

**Con spec**, la estructura cambia: ahora decís tu **intención**, tus **requisitos**, los **escenarios** posibles, los **criterios**. Un ejemplo ilustrativo (lo importante no es la plantilla; es cómo cambia lo que le estás diciendo):

```markdown
# Spec — Ingreso de usuarios registrados

## Intención
Qué: permitir que un usuario registrado acceda a su cuenta.
Para qué: proteger los datos de cada donante y habilitar sus acciones.

## Escenarios de aceptación
- DADO un usuario registrado con credenciales válidas
  CUANDO ingresa email y contraseña correctos
  ENTONCES accede a su panel.
- DADO un usuario registrado
  CUANDO ingresa una contraseña incorrecta tres veces seguidas
  ENTONCES la cuenta queda bloqueada temporalmente y se le informa.

## Flujo del caso de uso
1. El usuario abre la pantalla de ingreso.  2. Completa email y contraseña.
3. El sistema valida.  4a. Válido → panel.  4b. Inválido → mensaje, vuelve a 2.

## Requisitos verificables
- El bloqueo temporal dura 15 minutos.
- La contraseña nunca se muestra ni se registra en logs.

## Criterios medibles
- Los dos escenarios pasan como tests automatizados.
- El ingreso responde en menos de 2 segundos.
```
*(No dice qué framework, qué base de datos ni qué librería: eso no es de la spec.)*

### 2.3 Anatomía de una spec

| Componente | Qué responde |
|---|---|
| **Intención** | ¿Qué queremos hacer? ¿Para qué? |
| **Escenarios de aceptación** | Situaciones concretas, escritas con **Dado / Cuando / Entonces** (*Given / When / Then*): una forma muy específica de decir lo que se quiere, **sin ambigüedad**. |
| **Flujos de caso de uso** | Los mismos que se trabajan en las materias de segundo y tercero. |
| **Requisitos verificables** | Lo que se necesita, dicho de forma que se pueda comprobar. |
| **Criterios medibles** | ¿Cómo sabe el agente que llegó a algo correcto? |

Si la especificación es correcta y fuerte, **la implementación y lo demás sale por sí solo**. Y una regla que ya conocés de las materias: **la spec no menciona la tecnología.** Primero **qué** vamos a hacer, no **cómo**.

> **Para el parcial, si te preguntan:** *¿Qué diferencia hay entre pedirle a un agente "hacé una pantalla de login" y darle una spec?*
> Con el prompt suelto se delega todo: la interpretación de lo que se quiere, el diseño, la tecnología y el criterio de terminado. Una spec fija la intención, escenarios de aceptación sin ambigüedad (Dado/Cuando/Entonces), el flujo del caso de uso, requisitos verificables y criterios medibles, sin nombrar tecnología. Pasa a ser la fuente de verdad y un contrato verificable: el agente sabe qué construir y cómo comprobar que terminó, y los errores de requerimiento aparecen antes de escribir código.

---

## 3. 🔴 Método y contexto van de la mano

Acá se junta todo. En SDD, **los artefactos del método nacen como parte del contexto** del agente. Las etapas son cinco (varían según la implementación: alguna más, alguna menos), del **qué** al **cómo**:

```
   CONSTITUTION ──► SPECIFY ──► PLAN ──► TASKS ──► IMPLEMENT
       ✋              ✋          ✋         ✋           ✋
    reglas        relevamiento  diseño    desglose    código
                  casos de uso  (el cómo) en tareas
                                          ejecutables
```

| Etapa | Qué es |
|---|---|
| **Constitution** | Las **reglas**. Pueden ser las rules (`AGENTS.md` / `CLAUDE.md`) o un artefacto aparte, según la herramienta. |
| **Specify** | La especificación: relevamiento, casos de uso, lo de §2.3. |
| **Plan** | El **diseño del sistema**, cómo se va a trabajar. De acá salen las tareas. |
| **Tasks** | El diseño desglosado en **tareas ejecutables**. |
| **Implement** | Alguien lo implementa. |

La ✋ en cada etapa es **intervención humana**. De nada sirve delegarle todo al agente: se inventa especificaciones, se inventa un diseño, y al llegar a la implementación no tiene nada que ver con lo que querías. Volveríamos al inicio.

### 3.1 La skill encapsula el método; el criterio lo aplica el ingeniero

¿Cómo hago que el agente cree buenas specs, un diseño acorde, y que **no haga cosas sin preguntarme**? Con una **skill** (Parte 4). Es un caso de uso directo: en la skill le explicás **qué estructura** tienen los artefactos, **qué tiene que preguntarte** antes de cerrar cada uno, que **busque casos borde**. Y esto se potencia con cada modelo: cuanto mejor entrenado, más casos borde encuentra.

Lo que no cambia: **el criterio lo ponemos nosotros, no el agente.** El agente tiene que preguntarnos. Nosotros no escribimos el `##` ni el formato; **sí escribimos el contenido real** de la especificación.

### 3.2 Los hooks impiden saltear etapas

¿Cómo evito que avance sin leer, que genere un plan sin specs? Con la **validación determinística de los hooks** (Parte 4, §3.3):

- ¿Querés crear un diseño? Primero **validá que las specs estén aprobadas y terminadas**, y que las hayas leído.
- No creás **tareas** si no tuviste en cuenta la constitución y el plan.
- Y **ni hablar** de escribir código si no están todas las etapas completas.

### 3.3 Cómo lo implementan las herramientas: orquestador y subagentes

Muchas herramientas de SDD trabajan con un **orquestador** que dirige las etapas y el ciclo del método, más **subagentes** por etapa (Parte 4, §3.4). Cada uno sabe lo que hace **por su skill**; que no avancen antes de tiempo lo garantizan **los hooks**. Y cada uno usa un modelo distinto, con el criterio de la Parte 5:

| Rol | Qué hace | Modelo |
|---|---|---|
| **Orquestador** | Dirige las etapas; razona y te ayuda a avanzar. **Razonar no significa resolver tus problemas: te ayuda, pero los resolvés vos.** | **Fuerte** |
| **Implementador** | Ya tiene el plan, la tarea, el camino hecho. | **Mediano o liviano** |
| **Explorador** | Antes de especificar en un proyecto existente, recopila contexto y lo trae. No queremos que razone. | **Muy liviano** |

---

## 4. 🔴 Cuándo sí, cuándo no, y dónde falla

### 4.1 Cuándo no

- **Un cambio de dos líneas.** No le pedís al agente que cree 10.000 archivos para centrar un div.
- **Un spike, una prueba de concepto, una demo.** Querés probar una idea rápido y después ver cómo hacerlo bien; el método te traba. (Un MVP ya no tanto: tiene mucho más trabajo encima.)
- **Un script de una sola vez.** "Ordename los archivos de tal carpeta." Listo. Se usa y se suelta; no hay nada que sostener.

> **Glosas —** *spike*: prueba corta para reducir una incertidumbre técnica. *MVP*: producto mínimo viable, la primera versión usable.

### 4.2 Cuándo sí

- **Una feature nueva** grande, con muchas funcionalidades. Va a haber **ambigüedad**, y **no la resuelve el agente: la resolvemos nosotros**; el agente ayuda a bajar a tierra lo que pensamos.
- **Mantener lo que ya existe.** Un proyecto grande necesita documentación, y la documentación es **inherente al método**; con IA, nace de tu forma normal de pensar.
- **Cambiar algo.** No se cambia el código sin cambiar la spec o el plan: siempre hay que **burbujear hacia atrás** y arreglar. Antes era tedioso; hoy es ágil.
- **Un proyecto desde cero.** Reglas firmes, casos de uso iniciales, requerimientos.

### 4.3 Dónde falla

Aunque elijas bien el momento y el trabajo, puede fallar:

| Falla | Por qué |
|---|---|
| **Specs ambiguas** | Si no sabés lo que querés, ¿por qué lo sabría el agente? Input basura, salida basura. |
| **Contexto de más** | "Para tocar un componente del front, traeme cómo funciona todo el back." ¿Para qué? Dos partes del seminario lo dijeron: contexto **de calidad**, sí; **de más**, no: alucina. |
| **Aprobar sin leer** | Le escribiste 20 renglones, confiás en la skill, y **no leés la spec** que generó. Error gravísimo: la spec es donde tenés que estar seguro de que escribió lo que querés. Delegaste la escritura, **no la revisión**. |
| **El humano como botón de OK** | ¿Cuántas veces el agente pidió permiso y respondiste "sí, sí, sí"? Ahí no hay método que valga: estás aprobando sin leer ni entender. |

Las dos últimas son la misma: **el humano razona y dirige; el agente ejecuta.**

### 4.4 Herramientas y extrapolación

Hay muchas implementaciones; **Spec Kit** es la oficial y muy usada, y hay otras que lo hacen un poco distinto. Pero lo que hay que llevarse no es cómo usar una: **el método concreto puede cambiar en un mes**, y en un año quizás ni trabajás en software. Lo que se lleva es el principio, que se extrapola a cualquier disciplina: hay quien armó una ingeniería de contexto sobre **el método científico**, con agentes por etapa, y llegó a resultados premiados; alguien que trabaja en RPA se armó la suya sobre las etapas de esa disciplina.

> **Glosa —** *RPA (Robotic Process Automation)*: automatización de procesos repetitivos de oficina con software.

### 4.5 Lo que hay que llevarse de la metodología

1. **El humano razona, piensa y dirige; el agente ejecuta.** No delegamos criterios: delegamos formalidades y lo que antes era tedioso. El agente ayuda a razonar, sí: sabe hacer preguntas cuya respuesta sabíamos pero no dijimos.
2. **No reinventamos la rueda.** Tomamos las bases que servían (los métodos, SDD o el que sea, en cualquier área) y las llevamos a estas herramientas con mirada ingenieril.
3. **Bajó el costo de sostener el método.** La barrera que existía ya no está: cualquiera agarra la IA y puede continuar el método.
4. **Método y contexto se complementan.** ¿Se puede trabajar con método sin contexto? Sí; dudo que salga bien. ¿Contexto sin método? Se trabaja menos organizado, menos formal, y se pierden los beneficios.

> **Para el parcial, si te preguntan:** *Te piden agregar un campo opcional a un formulario, y aparte diseñar el módulo de incentivos completo. ¿Aplicás SDD en los dos? Justificá.*
> No en el primero: es un cambio de dos líneas y el método lo traba sin darle beneficios. Sí en el segundo: es una feature nueva con ambigüedad que tengo que resolver yo, y el método hace que los errores de requerimiento y diseño aparezcan en la spec y el plan, no en el código. Y si después cambio algo del módulo, burbujeo hacia atrás a la spec antes de tocar código.

---

## 5. 🔴 Cierre del encuentro: los componentes que siempre están

Poniendo todo sobre la mesa, hay cuatro componentes que van a seguir apareciendo, incluso más allá del desarrollo de código:

| Componente | Qué es |
|---|---|
| **Modelo** | El que predice (Parte 1). |
| **Prompt** | Las indicaciones, con su receta: qué quiero, cómo lo quiero, el rol. Parece viejo, pero sigue en el día a día; aunque hoy el prompt que escribís se mejora una o dos veces antes de llegar al modelo. |
| **Contexto** | Toda la información que le doy para que haga la tarea mejor. **Ni poco ni mucho: el adecuado** para esa tarea. |
| **Criterio** | Lo más importante. Con él definimos el **objetivo**, **cómo se logra**, **cómo se evalúa**, y somos los **validadores** de la tarea. |

### 5.1 Mantenerse al día

Un poco de perspectiva. ChatGPT salió en 2022; GPT-3, el primer modelo generativo popular, en 2020; y el paper de la arquitectura en la que se basan todos, el **Transformer**, es de **2017**. Estamos usando una arquitectura de casi diez años; lo que se hizo encima es **un proceso de ingeniería**, que es lo importante: dada una tecnología, aprender a usarla y potenciarla. Como la transformada de Fourier, del 1800, que se sigue usando. Y la IA como disciplina existe desde los años 50: **Turing** y su test, ya superado. Más de 75 años; lo que la hizo popular es que **habla nuestro lenguaje**: antes, para programar había que saber un lenguaje y su sintaxis; hoy un prompt te tira código que funciona y "todos nos hicimos full stack".

Cómo seguir: venir a encuentros como este, seguir cuentas, newsletters, videos; y sobre todo **usarla**, en desarrollo y en otras tareas.

### 5.2 La IA participa en todo el proceso

```
   relevamiento ──► requisitos ──► spec general ──► spec técnica ──► repositorio ──► despliegue
   (notas de la      (lo que
    reunión con       quiere el
    el cliente)       cliente)
```

Está en todas las etapas. Por eso hay que darle **permisos** para hacer todo eso, y a la vez **límites** para cómo usar las herramientas que le damos. Y por eso nuestro criterio, conocimientos y fundamentos son clave para tomar buenas decisiones de diseño, desarrollo y despliegue.

### 5.3 Tres preguntas que la IA no responde por vos

1. **¿Estamos resolviendo el problema correcto?** La IA resuelve el problema que **recibió como entrada**. Que esa entrada refleje el problema real queda de nuestro lado.
2. **¿Qué falla y a quién afecta?** No pienses en una prueba de concepto: hay empresas que **despliegan automáticamente a producción** código generado con IA. Si en ese "OK, OK, OK" hubo un OK de más, se desplegó una versión que está usando un usuario final. Han fallado muchos chatbots: desde **vender pasajes a cero pesos** hasta dar una **receta de cocina** cuando tenía que dar un turno médico. Hay que entender dónde puede fallar y qué tan crítico es.
3. **¿Cómo comprobamos que funciona?** ¿Probamos un solo caso y nos quedamos conformes? Para eso se arman **golden datasets**: un set de casos que se ejecuta siempre para verificar que todo lo que tiene que cumplirse se sigue cumpliendo.

> **Glosa —** *golden dataset*: conjunto de casos de prueba con la respuesta correcta conocida, contra el que se valida un sistema de IA cada vez que cambia.

### 5.4 ¿Podés explicar la solución que aceptaste?

Ese código que te generó para el TP que entregabas mañana: ¿lo podés explicar? ¿Alguna vez aceptaste código porque sí? Todos. Depende para qué sea. Pero si está en producción, cuidado, porque **si hoy no puedo explicar el código, y mañana viene otro requerimiento que se construye sobre ese código, en un par de semanas no voy a entender nada.**

Entender todo lo que se agrega (aunque se pierda algún detalle técnico de una línea) es lo que evita que **el copiloto pase a ser el piloto**. La IA siempre tiene que ser copiloto; si no podés explicar lo que hace, eso empieza a cambiar. Las preguntas reflexivas: ¿por qué elegimos esto? ¿por qué no otra cosa? ¿qué opciones había? ¿cómo comprobamos que funciona? Eso vale sea SDD o la metodología que venga, sea este modelo o el próximo (incluidos los modelos nuevos que **clasifican y toman decisiones**, que van a complementar a los generativos).

### 5.5 Más autonomía, más cabeza en el diseño

Cuando delegás tareas largas, hay cosas que tenés que definir antes:

- ¿**Cuándo** debe parar? ¿Cuándo y **qué** debe consultarte?
- ¿**Qué contexto** le vas a dar? ¿A **qué herramientas** accede, y **cómo**?
- ¿Acceso a una base de datos? ¿Puede **leer**? ¿Puede **escribir**? ¿Tiene acceso a **internet**?

Porque los modelos se orientan a **objetivos**, y "el fin justifica los medios": sin malicia, el modelo puede llegar al objetivo **por otro camino** que no era el pensado. En experimentos, agentes que se "salieron de control" lograron acceder a internet y a servidores de Hugging Face para **encontrar las respuestas de un benchmark** que tenían que responder.

> **Glosa —** *Hugging Face*: plataforma donde se publican modelos, datasets y benchmarks de IA.

Más capacidad implica **más responsabilidad** de nuestro lado, y a gran escala tiene consecuencias de ciberseguridad.

### 5.6 Un experimento para empezar

Para ganar confianza, lo más importante es **usarla**: probar modelos, casos de uso, ver cómo hace esto, qué pasa si lo pongo así. La evidencia de cómo te va construyendo la confianza para usarla en el día a día **de manera controlada**, en proyectos de la facultad o de afuera, con una metodología, y con límites claros de a qué le das acceso, cómo y para qué.

Para un proyecto concreto:

```
   1. DEFINÍ el resultado que esperás   (no solo el pedido: el resultado)
   2. DALE el contexto adecuado
   3. COMPROBÁ a qué llegó y qué cambió
   4. REGISTRÁ lo que hizo mejor... y también LAS FALLAS
```

Las IA aprenden de lo que hacen bien **y** de lo que hacen mal; eso es input para quienes entrenan modelos y para tu setup local. Registrar lo que no hizo bien hace que en la próxima iteración no lo repita. Así, de a poco, armás una guía (qué te gusta, qué no, qué hizo bien) y las generaciones se acercan a tu estilo y a lo que cada proyecto espera.

### 5.7 🟢 IA en la carrera

En Ingeniería en Sistemas se enseña IA desde el plan 95: la materia **Inteligencia Artificial** ve redes neuronales y los conceptos que un LLM usa por detrás. Hay además **Inteligencia Artificial Avanzada** y una electiva más cercana a este seminario, **IA Generativa y Sistemas Agénticos**. Y hay investigación en el grupo **GEMIS**: benchmarks de **SLMs** (*small language models*: modelos chicos, cuantizados, que corren en una computadora local y generan código sin proveedor externo), **vulnerabilidades** de sistemas de agentes (el chatbot que da una receta en vez de un turno, o datos de otra persona; agentes que vulneran a otros agentes), ingeniería de conocimiento y aplicaciones en salud.

> **Glosa —** *cuantizado*: modelo comprimido reduciendo la precisión de sus pesos para que entre en menos memoria.

### 5.8 Operativa

El **segundo encuentro es presencial, en laboratorio**, y va a incluir una demo de una herramienta concreta; por el espacio, hay que **confirmar asistencia** cuando llegue la información.

---

## Síntesis de la Parte 6

| Idea | En una línea |
|---|---|
| Método | Decidir el orden antes de empezar. Predecible, trazable, transferible, barato (errores donde nacen). |
| Por qué ahora | La IA bajó el costo de entrada: redactar en minutos, mantener automático. |
| SDD | Spec antes que código; fuente de verdad y contrato verificable; no nombra tecnología. |
| Spec | Intención · escenarios Dado/Cuando/Entonces · flujo de caso de uso · requisitos verificables · criterios medibles. |
| Etapas | Constitution → Specify → Plan → Tasks → Implement, con ✋ humana en cada una. |
| Skill / hooks | La skill encapsula el método (criterio del ingeniero); los hooks impiden saltear etapas. |
| Roles | Orquestador (fuerte) · implementador (mediano/liviano) · explorador (muy liviano). |
| Cuándo no / sí | No: dos líneas, spike, script único. Sí: feature grande, mantener, cambiar (burbujear atrás), desde cero. |
| Falla | Specs ambiguas · contexto de más · aprobar sin leer · humano-botón-OK. |
| Cierre | Modelo, prompt, contexto, **criterio**. Tres preguntas: ¿problema correcto? ¿qué falla y a quién? ¿cómo comprobamos? |
| Copiloto | Si no podés explicar lo que aceptaste, el copiloto pasa a ser el piloto. |
| Antes de delegar | Cuándo parar, qué consultar, qué contexto, qué herramientas, qué permisos. |
| Experimento | Definir resultado → contexto → comprobar → registrar aciertos y fallas. |

---

## ✅ Checkpoint (sin respuestas)

1. ¿Por qué el seminario dice que un método es "barato" si construirlo y sostenerlo no lo es? ¿Qué cambió con la IA en esa ecuación?
2. Escribí dos escenarios Dado/Cuando/Entonces para el registro de una donación en DonaTrack, sin nombrar ninguna tecnología.
3. ¿Qué pasa si en la etapa Plan el humano solo pone "OK"? Rastreá el error hasta la etapa Implement.
4. ¿Qué hace la skill y qué hace el hook dentro de SDD? ¿Por qué ninguno de los dos reemplaza al otro?
5. Justificá por qué el explorador usa un modelo muy liviano y el orquestador uno fuerte, usando las gamas de la Parte 5.
6. Te piden cambiar una validación de negocio en un módulo que ya está especificado y planificado. ¿Por dónde empezás y por qué no por el código?
7. Un agente pasó todos los tests de un módulo, pero el módulo resuelve un problema que el cliente no tenía. ¿Cuál de las tres preguntas del cierre falló, y en qué etapa de SDD se tendría que haber detectado?
8. Con lo visto en §5.5, listá qué definirías antes de darle a un agente acceso a la base de datos de DonaTrack para "limpiar registros duplicados".
9. ¿Por qué "registrar las fallas" del agente es tan importante como registrar los aciertos?

---

## Cierre de la unidad

Con esta parte termina el encuentro 1. El recorrido completo fue: qué es un modelo y qué lo convierte en agente (Parte 1); cómo trabaja por dentro y qué no sabe (Parte 2); qué riesgos trae delegar (Parte 3); cómo se le da contexto y qué son las skills (Parte 4); dónde estás parado y qué modelo usar (Parte 5); y cómo se ordena todo eso en un método, con el criterio siempre del lado del ingeniero (Parte 6).

---

**FIN DE LA PARTE 6 — Metodología: Spec-Driven Development, y el cierre del encuentro**
**FIN DE LA UNIDAD — Seminario "Proceso de diseño empleando IA Generativa" · Encuentro 1**
