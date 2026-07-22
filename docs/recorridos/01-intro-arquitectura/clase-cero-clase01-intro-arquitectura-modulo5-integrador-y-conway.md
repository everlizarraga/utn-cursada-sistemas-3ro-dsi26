# 🧱 Clase desde Cero — Clase 01 · Módulo 5: Todo junto + Ley de Conway

**Unidad:** `clase01` — Arquitectura de Software (25/03/2026)
**Serie:** Módulo 5 de 5 — el mapa completo está en el roadmap de la serie.

---

## Sobre este documento

**Qué cubre:** el caso integrador — una clínica que quiere digitalizar la gestión de turnos — recorrido con las tres preguntas del arquitecto, desde las entradas hasta la propuesta de despliegue; y la **Ley de Conway**, con el caso de los trámites universitarios.

**Qué NO cubre:** conceptos nuevos. Este módulo no agrega vocabulario — **usa** el de los módulos 1 a 4. Si algo de acá no te cierra, la deuda está atrás, no adelante.

## De dónde venís

De toda la serie: requerimientos y sus tipos (M1), decisiones arquitectónicas (M2), las siete entradas y la jerarquía de abstracción (M3), los componentes típicos y los dos diagramas (M4). Este módulo es el examen de manejo de todo eso.

---

## 1. El caso: la clínica de turnos 🟡

**Escenario.** Una clínica quiere digitalizar la gestión de turnos. Hoy todo se hace **por teléfono y en papel**. Nos contratan para diseñar el sistema.

Veamos qué preguntas podría hacerse el arquitecto del proyecto. Son tres — y no es casualidad que mapeen una a una con los módulos que venís de leer.

### Pregunta 1 — ¿Cuáles son las entradas?

Primero, juntar los insumos (Módulo 3). Lo que la clínica trae:

- **RF:** los pacientes sacan turnos online, los médicos ven su agenda, la administración genera reportes.
- **RNF:** debe funcionar en el celular, soportar 500 turnos diarios, estar disponible 24/7.
- **Restricción técnica:** el cliente **ya tiene un servidor propio** (no quiere nube).
- **Restricción de negocio:** presupuesto acotado, debe estar listo en **3 meses**.

Fijate que es la tabla del Módulo 3 llenada con un caso real: qué debe hacer (RF), cómo debe funcionar (RNF), y los bordes de la cancha que no se negocian (restricciones). Cada uno de estos renglones va a dejar su huella visible en la propuesta final — tenelos a mano al llegar al diagrama.

### Pregunta 2 — ¿Cuáles son los componentes típicos?

Segundo, plantear las piezas (Módulo 4):

- **Frontend web:** para que los pacientes saquen turnos desde el navegador.
- **Frontend mobile (futuro):** app para los médicos. — Un componente etiquetado "futuro": el requerimiento futuro del Módulo 3 apareciendo como pieza anticipada del plano.
- **Backend:** gestiona la lógica de turnos, las notificaciones, la disponibilidad.
- **Base de datos:** almacena pacientes, médicos, turnos, historiales.

### Pregunta 3 — ¿Qué decisiones arquitectónicas hay que tomar?

Tercero, decidir (Módulo 2 — temprano, porque después es caro):

- **¿Monolito o microservicios?** → **Dado el presupuesto y el tiempo: monolito.**
- **¿Cómo se notifica al paciente?** → ¿Email, SMS, WhatsApp?
- **¿Cómo acceden los médicos?** → ¿Web o app?

Pará en la primera, porque es la joya del caso. Es una decisión de **estilo arquitectónico** (el nivel más abstracto del Módulo 3), y mirá **cómo está justificada**: no dice "monolito porque los monolitos son mejores" — dice "monolito, **dado el presupuesto y el tiempo**". Dos **entradas** (restricción de negocio: presupuesto acotado, 3 meses) empujando una decisión de estilo. Ese es el molde de razonamiento de toda la materia: *elijo [X] porque [entrada/prioridad], aunque resigno [otra cosa]* — acá se resigna, por ejemplo, la escalabilidad independiente de cada parte que un estilo de piezas separadas daría. Primera vez que lo ves completo; no va a ser la última.

---

## 2. La propuesta 🟡

Las tres preguntas respondidas se dibujan — y como la pregunta del cliente es "¿dónde va a correr esto?", el diagrama elegido es el de **despliegue**:

```
   Sistema de Turnos Médicos — Diagrama de Despliegue

        ☺            ☺            ☺
     Paciente      Médico    Administrador
        │ usa        │ usa        │ usa
        └────────────┼────────────┘
                     ▼
   ╔═ Dispositivo del Usuario ══════════╗
   ║      ┌──┬────────────────┐         ║
   ║      ├──┤  Navegador Web │         ║
   ║      └──┴────────────────┘         ║
   ╚═════════════════╤══════════════════╝
                     │ HTTP/REST
                     ▼
   ╔═ Servidor de Aplicación ═══════════╗
   ║   ┌──┬──────────────────┐          ║     ╔═ Servidor de Base ═╗
   ║   ├──┤ Backend API REST │──────────║─────║   de Datos         ║
   ║   └──┴──┬──────┬──────┬─┘          ║     ║  ⛁ BD Turnos       ║
   ║  ┌─ Módulos ───┼──────┼─────────┐  ║     ║     Médicos        ║
   ║  │ ┌──┬────▼┐ ┌──┬▼─────┐ ┌──┬─▼──────┐  ╚════════════════════╝
   ║  │ ├──┤Gest.│ ├──┤Gest. │ ├──┤ Notifi-│
   ║  │ └──┴Turnos┘└──┴Usuarios└──┴caciones│
   ║  └──────────────────────────────┼───┘  ║
   ╚═════════════════════════════════╪══════╝
                                     │ SMTP
                                     ▼
                          ⌒⌒⌒⌒⌒⌒⌒⌒⌒⌒⌒⌒⌒⌒⌒
                        ⌇ Servicios Externos  ⌇
                        ⌇  ┌──┬─────────────┐ ⌇
                        ⌇  ├──┤ Servicio de │ ⌇
                        ⌇  └──┴ Email (SMTP)┘ ⌇
                          ⌣⌣⌣⌣⌣⌣⌣⌣⌣⌣⌣⌣⌣⌣⌣
```

### Leer el diagrama con las entradas en la mano

Todo lo decidido quedó dibujado — recorrelo entrada por entrada:

- **Tres actores** (Paciente, Médico, Administrador): los tres RF tienen su usuario en el dibujo.
- **Un solo "Dispositivo del Usuario" con Navegador Web:** todos entran por la web — coherente con la decisión pendiente "¿los médicos por web o app?" resuelta, por ahora, del lado web; la app mobile quedó como componente futuro, fuera de esta propuesta.
- **Un solo Servidor de Aplicación:** el **servidor propio** del cliente (restricción técnica: no quiere nube) corriendo el **monolito** (la decisión de estilo): un único Backend API REST con sus módulos adentro — Gestión de Turnos, Gestión de Usuarios y Notificaciones como subcomponentes, no como sistemas separados.
- **El Servidor de Base de Datos** conectado al backend — la matrioshka lógica del Módulo 4 desplegada en su propio nodo físico.
- **Y un nodo con forma de nube: Servicios Externos.** La decisión "¿cómo notificamos?" aterrizó en email: el módulo de Notificaciones sale por **SMTP** (el protocolo estándar de envío de correo electrónico) hacia un **servicio de email de terceros**. Detalle fino: ¿el cliente no quería nube? No quería *su sistema* en la nube — pero mandar emails no es su sistema: es un servicio que se consume de afuera, como la luz o el agua. El dibujo distingue con honestidad qué es propio (nodos servidor) y qué es ajeno (la nube de servicios externos).

El hilo 4 de la serie se cierra acá: si la pregunta hubiera sido "¿qué piezas tiene el sistema y cómo se conectan?", el mismo sistema se contestaba con su diagrama de **componentes**; como fue "¿dónde corre y qué contrata el cliente?", se contestó con **despliegue**. Un sistema, dos estructuras, cada diagrama para su pregunta.

> **Para el parcial, si te preguntan — resolvé un caso como arquitecto.**
> El recorrido es: **1)** identificar las entradas (RF, RNF, restricciones técnicas y de negocio, futuros); **2)** plantear los componentes típicos (frontend, backend con sus módulos, base de datos); **3)** tomar las decisiones arquitectónicas justificándolas con las entradas — con el molde "elijo [X] porque [entrada], resignando [Y]" —; y **4)** comunicar la propuesta con el diagrama que responde la pregunta del caso: componentes (qué piezas y cómo se conectan) o despliegue (dónde corre y cómo se comunica).

---

## 3. La Ley de Conway 🟢

Cierre de la serie, con una pregunta en apariencia inocente:

**¿Qué piensan de los trámites en el ámbito universitario?**

…Exacto. Y hay una ley con nombre y apellido que explica por qué son así:

> **"Las organizaciones diseñan sistemas que replican su estructura de comunicación."**
> — Melvin Conway (1968), científico informático estadounidense.

Si las áreas de una organización no se hablan entre sí, sus sistemas tampoco lo van a hacer. El software hereda la forma de la organización que lo construye.

### El caso: gestión de trámites — excepción de correlativas

- Proceso **fragmentado entre áreas** (Alumnos, Gestión Académica, Departamento de Sistemas).
- **Sistemas y formularios separados por sector.**
- **Reingreso y duplicación de datos** — lo que cargaste en una ventanilla, lo volvés a cargar en la siguiente.
- **El alumno termina integrando el proceso** — el síntoma más elocuente: como ningún sistema conecta las áreas, la integración la hace una persona, a pie, llevando papeles de una oficina a otra.
- **Demoras por falta de visión end-to-end** (de punta a punta: mirar el proceso completo desde que empieza hasta que termina, no cada tramo por separado).

La lectura: **el problema no es solo tecnológico → es organizacional.** El sistema refleja cómo se comunican (o no se comunican) las áreas. Y esto conecta con algo que quedó dicho en el Módulo 2: la arquitectura de sistema *"no es solo lógica sino también física y organizacional"*. Conway es la razón profunda de esa característica — no podés diseñar el sistema ignorando la organización, porque la organización se va a imprimir en el sistema quieras o no.

### ¿Y entonces, por dónde se empieza?

Si tuvieras que mejorar los trámites universitarios, ¿empezarías por el sistema o por la forma en que se organizan y comunican las áreas?

La clave es **diseñar procesos integrados end-to-end y, a partir de ahí, construir sistemas que acompañen esa lógica**, poniendo en el centro la experiencia de la persona usuaria (estudiante, docente, no docente). Primero el proceso, después el software que lo acompaña — al revés, solo se digitaliza la fragmentación.

> **Para el parcial, si te preguntan — la Ley de Conway.**
> La Ley de Conway (Melvin Conway, 1968) dice que las organizaciones diseñan sistemas que replican su estructura de comunicación: si las áreas están fragmentadas y no se comunican, sus sistemas resultan fragmentados y desconectados. Por eso los problemas de un sistema pueden no ser solo tecnológicos sino organizacionales, y la mejora empieza por diseñar el proceso integrado de punta a punta, construyendo después sistemas que acompañen esa lógica.

---

## ✅ Checkpoint — Módulo 5

*Sin respuestas acá: intentalas sin mirar el material, y las dudas se resuelven por chat.*

1. Volvé a la lista de entradas de la clínica y clasificá cada renglón con las categorías del Módulo 3 (RF / RNF / restricción técnica / restricción de negocio). ¿Hay algún requerimiento futuro escondido en el caso? ¿Dónde quedó reflejado en los componentes?
2. La decisión "monolito, dado el presupuesto y el tiempo": reescribila completa con el molde *"elijo [X] porque [entradas], resignando [Y]"* — nombrando explícitamente qué se resigna.
3. Cambio de escenario: la clínica ahora **acepta la nube** y espera **triplicar los turnos diarios** el año próximo. ¿Qué entradas cambiaron? ¿Revisarías la decisión de estilo? Justificá con el molde (no hay única respuesta correcta: se evalúa la justificación).
4. Si la notificación fuera por SMS en vez de email, ¿qué cambia y qué NO cambia en el diagrama de despliegue? ¿Qué te dice eso sobre el valor de tener Notificaciones como módulo separado?
5. El cliente "no quería nube", pero la propuesta tiene un nodo-nube de Servicios Externos. Explicá por qué no es una contradicción — y qué distingue el diagrama entre lo propio y lo ajeno.
6. Un compañero dibuja la propuesta de la clínica como diagrama de **componentes** y otro como diagrama de **despliegue**. El gerente pregunta *"¿cuánto me va a costar el hosting de todo esto?"*. ¿Cuál de los dos diagramas le sirve para responder y qué información tiene que el otro no?
7. "El alumno termina integrando el proceso." Explicá por qué ese síntoma es exactamente lo que la Ley de Conway predice para una organización con áreas fragmentadas.
8. Pensá un sistema que uses seguido y que funcione mal "a lo Conway" (te hace cargar lo mismo dos veces, te pasea entre pantallas que no se conocen). ¿Qué estructura de comunicación organizacional sospechás detrás? ¿Por dónde empezarías a mejorarlo: sistema u organización?

---

## 🏁 Fin de la serie

El arco completo, en retrospectiva: entendiste qué te piden (requerimientos, M1) → conociste a la disciplina que convierte eso en estructura (arquitectura, M2) → viste con qué insumos decide (entradas y abstracción, M3) → aprendiste a dibujar lo decidido (componentes y despliegue, M4) → y lo usaste todo junto sobre un caso real, con la advertencia de Conway de fondo (M5).

Los cuatro hilos de la serie quedaron cerrados — y uno quedó **deliberadamente abierto**: el casillero del *patrón de diseño* ("solución para refinar componentes", M3). Ese lo llena el resto de la materia, empezando muy pronto.

**Qué sigue:** intentá los checkpoints de los cinco módulos y traé las dudas por chat — de ahí sale el complemento de la unidad. Después, el apunte oficial de la unidad para el ciclo de repaso (que ahora se va a sentir como repaso, porque el entendimiento ya ocurrió acá) y la práctica de SmartLife — el cálculo de suscripción mensual — para poner las manos.

---

**FIN DEL MÓDULO 5 — FIN DE LA SERIE**
