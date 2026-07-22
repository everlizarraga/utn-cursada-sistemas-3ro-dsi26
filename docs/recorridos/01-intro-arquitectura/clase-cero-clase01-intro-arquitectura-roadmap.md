# 🧭 Clase desde Cero — Clase 01: Introducción a DSI y a la Arquitectura de Software — ROADMAP

**Unidad:** `clase01` — Arquitectura de Software (25/03/2026)
**Serie:** 5 módulos + este roadmap
**Material pedagógico extra** — no reemplaza a los archivos oficiales de la unidad; los prepara.

---

## Sobre esta serie

Esta serie enseña, desde cero y en orden, todo el contenido de la Clase 01 de la cursada: qué significa *diseñar* un sistema, qué son los requerimientos, qué es la arquitectura de software, cuáles son sus entradas, y cómo se comunica con dos diagramas UML (componentes y despliegue). Cierra con un caso integrador completo y una reflexión sobre por qué los sistemas terminan pareciéndose a las organizaciones que los construyen.

Es la primera clase del año: casi todo es **vocabulario y encuadre**. No hay código. La dificultad real es baja; lo que hay es *ancho* — muchos términos nuevos que el resto de la materia va a usar sin volver a explicar. El objetivo de la serie es que ese vocabulario quede **operativo**: no recitar definiciones, sino poder mirar un caso y decidir con esas palabras.

> **Cómo se evalúa esta materia (leelo antes de empezar).** Acá no se toma definición de memoria. Se evalúa que **decidas y justifiques**: *"pongo esto acá **porque** priorizo [tal cualidad], y estoy pagando con [tal otra]"*. Los checkpoints de esta serie ya están escritos en ese molde: casos chicos donde elegís y justificás, no listas para memorizar.

## De dónde venís (se asume sabido, no se re-explica)

- **Preclase 01** (misma unidad): el paradigma orientado a objetos y la notación de **diagrama de clases** UML. Cuando un módulo diga "esto tiene un nivel de abstracción más alto que un diagrama de clases", esa base es la que se está pisando.
- Nada más. Todo lo demás se explica acá antes de usarse.

---

## El arco en una frase

**Del problema al plano:** primero entendés qué te piden (requerimientos, M1) → después qué disciplina convierte eso en decisiones de estructura (arquitectura, M2) → con qué insumos decide (entradas y niveles de abstracción, M3) → cómo se dibuja lo decidido (componentes y despliegue, M4) → y todo junto sobre un caso real, más la advertencia de Conway (M5).

## Fases y módulos

### Fase A — El problema (qué te piden)

| Módulo | Título | Qué cubre | Importancia | Carga conceptual |
|---|---|---|---|---|
| **M1** | Diseñar y requerir | Qué es diseño (de sistemas / de software), por qué importa, requerimiento vs. requisito, las 6 cualidades de un buen requisito, RF vs. RNF, restricciones y reglas de negocio | 🔴 | Media — 8-9 términos nuevos, todos livianos |

### Fase B — La disciplina (quién decide y con qué)

| Módulo | Título | Qué cubre | Importancia | Carga conceptual |
|---|---|---|---|---|
| **M2** | Qué es la arquitectura de software | Arquitectura (estructura, componentes, relaciones), componente e interfaz de componente, niveles Enterprise / Sistema / Software, características de la arquitectura y de las decisiones arquitectónicas | 🔴 | Media — la analogía del edificio sostiene todo |
| **M3** | Entradas y niveles de abstracción | Las 7 entradas de la arquitectura (con los RNF / atributos de calidad como protagonistas) y la jerarquía Estilo Arquitectónico ⊃ Patrón Arquitectónico ⊃ Patrón de Diseño | 🔴 | **Alta — el módulo más importante de la serie.** Corto, pero todo lo que dice lo cobra el resto del año |

### Fase C — La práctica (cómo se comunica y se aplica)

| Módulo | Título | Qué cubre | Importancia | Carga conceptual |
|---|---|---|---|---|
| **M4** | Componentes típicos y sus diagramas | Frontend, Backend y Base de Datos; el símbolo UML de componente; interfaz bola-y-zócalo; Diagrama de Componentes vs. Diagrama de Despliegue — qué responde cada uno | 🔴 (componentes y diagramas) / 🟡 (detalle de tecnologías) | Media-alta — es el módulo con más notación nueva |
| **M5** | Todo junto + Ley de Conway | Caso integrador: sistema de turnos de una clínica, desde las entradas hasta la propuesta de despliegue; Ley de Conway y el caso de los trámites universitarios | 🟡 (integrador) / 🟢 (Conway) | Baja — acá no hay conceptos nuevos: se **usan** los de M1-M4 |

**Por qué este orden (dependencias, no cronología):** M2 necesita los RNF de M1 (son su entrada principal). M3 necesita "arquitectura" y "componente" de M2 para decir de qué son entradas. M4 necesita "componente" e "interfaz" de M2 para poder dibujarlos. M5 necesita todo: toma entradas (M1/M3), elige componentes (M4) y los despliega (M4). Ningún módulo usa un término que un módulo anterior no haya explicado.

## Mapa de hilos que se cierran

Cabos que se abren a propósito en un módulo y se resuelven en otro. Si al leer sentís que "falta algo", probablemente sea uno de estos — está anunciado y llega:

| # | Hilo | Se siembra en | Se cierra en |
|---|---|---|---|
| 1 | "Los RNF no son decorado: son **la entrada principal** de la arquitectura" | M1 (RF vs. RNF) | M3 (entradas de la arquitectura) |
| 2 | "Todo componente expone una **interfaz** — ¿y eso cómo se dibuja?" | M2 (interfaz de componente) | M4 (notación bola-y-zócalo) |
| 3 | "Estilo ⊃ patrón arquitectónico ⊃ **patrón de diseño**" | M3 (niveles de abstracción) | El resto de la materia — los patrones de diseño son su eje desde la clase 3 en adelante. La serie deja el casillero armado |
| 4 | "Componentes dice *qué* piezas hay; despliegue dice *dónde* corren" | M4 (los dos diagramas) | M5 (el integrador usa los dos sobre el mismo sistema) |

## Leyenda de bloques y marcas

- 🔴 / 🟡 / 🟢 — importancia del contenido: **central y evaluable** / secundario / mencionado al pasar.
- **"Para el parcial, si te preguntan"** — respuesta modelo lista para examen: primera oración ya responde, terminología de la materia, 1-3 oraciones.
- 🕳️ **Madriguera** — tangente real pero fuera del alcance de la materia: qué es en 1-3 líneas, y seguís. No dependas nunca de una madriguera para entender el camino principal.
- **"De dónde venís"** — al inicio de cada módulo: qué asume sabido de módulos anteriores o de la preclase.
- **Checkpoint** — al cierre de cada módulo, 5-10 preguntas **sin respuestas** (se responden después, por chat, y decantan al complemento). Están en el molde real de evaluación: caso + decisión + justificación.

## Cómo usar la serie

1. **En orden, M1 → M5.** El orden es por dependencias: saltear un módulo rompe los siguientes.
2. **Un módulo por sesión alcanza** (M1 y M5 son livianos; podés emparejar M4+M5 si venís con envión). M3 es corto pero pide lectura atenta: es el que más rinde por minuto.
3. Al terminar cada módulo, **intentá el checkpoint sin mirar el material**. Las dudas que te queden, anotalas: se resuelven por chat y quedan en el complemento de la unidad.
4. La serie no incluye la práctica de la unidad (el ejercicio de cálculo de suscripción mensual de SmartLife): esa se trabaja aparte, con las manos, después de la serie.

## Qué sigue después de la serie

Terminados los 5 módulos, la unidad sigue su flujo normal: apunte oficial de la unidad para el ciclo de repaso, complemento con las respuestas de los checkpoints, y la práctica de SmartLife. Con esta serie leída, todo eso se vive como repaso — el entendimiento ya ocurrió acá.

---

**FIN DEL ROADMAP — Clase desde Cero, Clase 01**
