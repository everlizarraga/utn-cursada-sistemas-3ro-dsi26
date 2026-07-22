# 🧱 Clase desde Cero — Clase 01 · Módulo 4: Componentes típicos y sus diagramas

**Unidad:** `clase01` — Arquitectura de Software (25/03/2026)
**Serie:** Módulo 4 de 5 — el mapa completo está en el roadmap de la serie.

---

## Sobre este documento

**Qué cubre:** los componentes típicos de un Sistema de Información — Frontend, Backend y Base de Datos — y las dos herramientas UML para comunicar la arquitectura: el **diagrama de componentes** y el **diagrama de despliegue**, con la notación de cada uno y la diferencia entre ambos.

**Qué NO cubre:** el caso integrador que usa todo esto junto (Módulo 5).

Es el módulo con más notación nueva de la serie. La buena noticia: son dos diagramas, y cada uno responde **una** pregunta.

## De dónde venís

- **Del Módulo 2:** componente (pieza de software con ciclo de vida propio) e interfaz (su contrato con el exterior) — acá se cierra el hilo pendiente: cómo se **dibuja** esa interfaz.
- **De la preclase 01:** la notación de diagrama de **clases**. Uno de los diagramas de hoy se define, literalmente, por contraste con él.

---

## 1. Los componentes típicos de un Sistema de Información 🔴

A muy alto nivel, en los Sistemas de Información nos vamos a encontrar principalmente con dos grandes componentes arquitectónicos — **Backend** y **Frontend** — más un tercero con entidad propia, la **Base de Datos**, que como vas a ver enseguida vive conceptualmente *adentro* de uno de los dos.

### Backend — el que trabaja

- Es la capa del sistema encargada del **procesamiento de datos**, de **gran parte de la lógica de negocio**, y de la **gestión de la comunicación** entre el cliente (frontend) y la base de datos o servicios externos.
- **El usuario nunca lo ve directamente.**
- Funciona **del lado del servidor** y generalmente **expone una API que el frontend consume** — ¿te suena? Es la interfaz de componente del Módulo 2, en su versión más típica.
- Tecnologías típicas: Java, Python, PHP, etc.

### Frontend — el que da la cara

- Es la **capa de presentación gráfica** del sistema, con la que interactúa el usuario.
- Es **lo que el usuario ve y toca**.
- Se ejecuta **en el navegador o en una aplicación cliente** (una app mobile, por ejemplo) y se encarga de la presentación de la información y de la experiencia de usuario.
- Tecnologías típicas: React, Angular, Vue (web) / Swift, Kotlin (mobile).

### Base de Datos — la memoria permanente

- Es el componente encargado de la **persistencia** de los datos que gestiona y manipula todo el sistema.
- Es la **"memoria permanente"**: sin ella, todo lo que pasa en el sistema **se pierde al apagar el servidor**.
- Ubicación conceptual — atendé a la matrioshka: la base de datos está dentro de la **capa de persistencia de datos**, y esa capa, a su vez, está **dentro del Backend**:

```
   ┌─ BACKEND ────────────────────────────┐
   │  lógica de negocio, procesamiento…   │
   │  ┌─ capa de persistencia ─────────┐  │
   │  │   ┌──────────────────────┐     │  │
   │  │   │    Base de Datos     │     │  │
   │  │   └──────────────────────┘     │  │
   │  └────────────────────────────────┘  │
   └──────────────────────────────────────┘
```

- Tecnologías típicas: PostgreSQL, MySQL, MongoDB, Oracle.

Guardate esa matrioshka: dentro de un rato va a generar una aparente contradicción — y resolverla enseña algo importante.

> **Para el parcial, si te preguntan — los componentes típicos.**
> Son el **Frontend** (capa de presentación gráfica: lo que el usuario ve y toca, corre en el navegador o app cliente), el **Backend** (procesamiento de datos, lógica de negocio y comunicación con la base de datos y servicios externos; corre del lado del servidor y expone una API que el frontend consume) y la **Base de Datos** (persistencia — la memoria permanente del sistema), que conceptualmente vive en la capa de persistencia, dentro del backend.

---

## 2. El diagrama de componentes 🔴

Primera herramienta para dibujar el plano. Empecemos por el símbolo:

> Un componente se representa como un **rectángulo con dos pequeños rectángulos superpuestos perpendicularmente en el lado izquierdo**.

```
   ┌──┬────────────────────┐
   ├──┤                    │
   ├──┤    NombreDelComp   │
   └──┴────────────────────┘
```

*(En muchas herramientas modernas los dos rectanguitos se achican a un ícono en la esquina superior derecha de la caja — es el mismo símbolo.)*

Y una frase que ubica al diagrama en el mapa que ya conocés:

> El diagrama de componentes tiene **un nivel más alto de abstracción que un diagrama de clases**.

En el diagrama de clases de la preclase 01, cada caja era una clase con sus atributos y métodos. Acá cada caja es un **componente entero** — que por dentro puede tener decenas de clases que este diagrama, deliberadamente, no muestra. Mismo sistema, zoom más lejos.

### Cómo se dibuja una interfaz: bola y zócalo

El hilo pendiente del Módulo 2 se cierra acá. Cuando un componente disponibiliza una interfaz que otro consume, la conexión se dibuja con un **círculo** en el medio de la línea — la notación **bola-y-zócalo**:

```
   ┌──┬─────────────┐              ┌──┬─────────────┐
   ├──┤             │              ├──┤             │
   ├──┤ Componente1 │─────○────────┤  Componente2   │
   └──┴─────────────┘              └──┴─────────────┘
                      Componente2 disponibiliza una
                      interfaz que "consume" Componente1
```

La bola (○) **es** la interfaz: la puerta publicada. El que la ofrece la dibuja de su lado; el que la consume se conecta a ella. Nunca hay una línea directa componente-a-componente sin pasar por ahí — igual que en la analogía: entre habitaciones se pasa por la puerta, no por la pared.

### Un diagrama de componentes real

Sistema de turnos (el caso que el Módulo 5 va a desarrollar completo):

```
   Diagrama de Componentes — Sistema de Turnos

        ┌──┬────────────────┐
        ├──┤  Navegador Web │
        └──┴───────┬────────┘
                   │ consume
                   ▼
                   ○  REST API
        ┌──┬───────┴────────┐
        ├──┤   Backend API  │
        └──┴──┬─────────┬───┘
              │         │
    ┌──┬──────▼───┐ ┌──┬▼───────────┐
    ├──┤ Módulo de│ ├──┤  Módulo de  │
    └──┴─ Turnos ─┘ └──┴─ Usuarios ──┘
```

¿Qué muestra este diagrama?

- **Los componentes principales del sistema y cómo se relacionan entre sí.**
- **La interfaz (REST API) que separa al frontend del backend** — nadie habla "directamente": **siempre hay un contrato**. El Navegador Web no sabe (ni le importa) cómo es el Backend por dentro; solo conoce la interfaz que este publica.
- **Los módulos internos del backend como subcomponentes** — el zoom admite un nivel más de detalle cuando aporta.
- Y tan importante como lo que muestra, lo que **no** muestra: **no habla de servidores ni de infraestructura**. En este dibujo no sabés si todo corre en una máquina o en diez. Eso es tarea del otro diagrama.

---

## 3. El diagrama de despliegue 🔴

Segunda herramienta. Mismo sistema de turnos, otra pregunta:

```
   Diagrama de Despliegue — Sistema de Turnos

                 ☺ Paciente
                 │ usa
                 ▼
   ╔═ PC / Celular ═══════════════╗
   ║   ┌──┬────────────────┐      ║
   ║   ├──┤  Navegador Web │      ║
   ║   └──┴────────────────┘      ║
   ╚═══════════════╤══════════════╝
                   │ HTTP/REST
                   ▼
   ╔═ Servidor de Aplicación ═════╗      ╔═ Servidor de Base ═══╗
   ║   ┌──┬──────────────┐        ║      ║   de Datos           ║
   ║   ├──┤  Backend API │──── SQL ──────║──▶  ⛁ BD Turnos      ║
   ║   └──┴──┬───────┬───┘        ║      ╚══════════════════════╝
   ║  ┌──┬───▼──┐ ┌──┬▼────────┐  ║
   ║  ├──┤Módulo│ ├──┤ Módulo   │ ║
   ║  └──┴Turnos┘ └──┴ Usuarios ┘ ║
   ╚══════════════════════════════╝
```

Elementos nuevos de esta notación:

- Las **cajas dobles** (acá con borde `═`; en las herramientas, cajas con efecto 3D) son **nodos**: las máquinas o dispositivos físicos donde corren los componentes — "PC / Celular", "Servidor de Aplicación", "Servidor de Base de Datos".
- Los **componentes van adentro de los nodos**: el diagrama dice, literalmente, qué pieza vive en qué máquina.
- El **actor** (☺, el monigote que ya conocés de otros diagramas UML) muestra quién usa el sistema y por dónde entra.
- Las flechas entre nodos se rotulan con **cómo viaja la comunicación**: `HTTP/REST` entre el dispositivo del usuario y el servidor de aplicación (HTTP es el protocolo de comunicación de la web — la cursada lo estudia en detalle enseguida), `SQL` entre el backend y la base de datos (el lenguaje estándar de consulta a bases de datos relacionales).

¿Qué agrega este diagrama respecto al de componentes?

- Muestra **dónde vive físicamente cada componente** (en qué servidor o dispositivo).
- Permite ver **cómo fluye la comunicación entre nodos** (HTTP, etc.).
- Ayuda a **identificar riesgos de infraestructura**: ¿qué pasa si el servidor de base de datos se cae?
- Es útil para **hablar con el cliente** sobre costos de hosting, seguridad y escalabilidad.

### La aparente contradicción (y qué enseña)

Momento. En la sección 1 dijimos que la base de datos vive **dentro del backend** (capa de persistencia)… y en este diagrama la BD está en **otro servidor**, afuera del que corre el backend. ¿Cuál de los dos miente?

Ninguno. Son **dos estructuras distintas del mismo sistema** — ¿te acordás de la definición del Módulo 2, "la estructura *o estructuras* del sistema"? Esta es la prueba. **Lógicamente** (organización en capas), la persistencia es parte del backend: es su capa de abajo. **Físicamente** (despliegue), esa capa puede correr en otra máquina. Una mirada responde a la organización del software; la otra, a su ubicación en fierros. Las dos son verdad a la vez, y por eso hacen falta dos diagramas.

---

## 4. Componentes vs. despliegue: qué responde cada uno 🔴

La síntesis del módulo, y una tabla que conviene saber de memoria — no como taxonomía, sino como reflejo para elegir el diagrama correcto:

| Diagrama | Responde… |
|---|---|
| **Componentes** | ¿**Qué** piezas de software existen y **cómo se conectan**? |
| **Despliegue** | ¿**Dónde** corre cada pieza y **a través de qué red** se comunican? |

- **Se usan juntos:** primero se entiende **qué construir** (componentes), después **dónde desplegarlo** (despliegue).
- En proyectos reales, el diagrama de despliegue lo revisa también el equipo de **infraestructura / DevOps** (el equipo que opera los servidores y automatiza la puesta en producción) — porque habla de sus máquinas.

El reflejo para el parcial y para la vida: te hacen una pregunta sobre el sistema → ¿es de *qué/cómo se conecta* o de *dónde corre/qué pasa si se cae*? La primera se contesta señalando el diagrama de componentes; la segunda, el de despliegue.

> **Para el parcial, si te preguntan — ¿qué muestra cada diagrama y en qué se diferencian?**
> El diagrama de **componentes** muestra qué piezas de software existen, cómo se relacionan y qué interfaces las separan — sin decir nada de servidores ni infraestructura. El diagrama de **despliegue** muestra dónde corre físicamente cada componente (nodos), cómo fluye la comunicación entre nodos (por ejemplo HTTP) y permite razonar riesgos de infraestructura, costos y escalabilidad. Se usan juntos: componentes define qué construir; despliegue, dónde desplegarlo.

---

## ✅ Checkpoint — Módulo 4

*Sin respuestas acá: intentalas sin mirar el material, y las dudas se resuelven por chat.*

1. Asigná cada responsabilidad a Frontend, Backend o Base de Datos, justificando en una línea: **a)** validar que un cliente tenga saldo antes de aceptar la operación · **b)** mostrar el resumen mensual con colores según el estado · **c)** conservar el historial de pagos aunque se reinicie el servidor · **d)** exponer la operación "calcular monto mensual" para que la use la app.
2. "Nadie habla directamente: siempre hay un contrato." Explicá la frase señalando qué elemento del diagrama de componentes es el contrato, y qué ganás con que el Navegador Web no conozca el interior del Backend.
3. Dibujá (o describí caja por caja) el diagrama de componentes de un SmartLife mínimo: un navegador web, un backend que expone una interfaz REST, y dos módulos internos — dispositivos y facturación. ¿Dónde va la bola?
4. El gerente pregunta: *"¿y si se nos cae el servidor donde está la base de datos, qué pasa con el sistema?"*. ¿Qué diagrama abrís para responderle y por qué el otro no te sirve para esa pregunta?
5. ¿Por qué el diagrama de componentes tiene un nivel más alto de abstracción que el de clases? ¿Qué "desaparece" al pasar de uno al otro, y por qué eso es una virtud y no una pérdida?
6. La BD está "dentro del backend" en la vista de capas, pero en otro servidor en el despliegue. Explicá por qué no es una contradicción, usando la idea de "estructura o estructuras" del Módulo 2.
7. Para cada afirmación, decí si pertenece al mundo del diagrama de componentes o al del de despliegue: **a)** "el módulo de usuarios se comunica con el backend API" · **b)** "la comunicación entre el celular y el servidor viaja por HTTP" · **c)** "conviene un segundo servidor de base de datos por si el primero falla" · **d)** "el frontend consume la interfaz REST del backend".
8. ¿Por qué el diagrama de despliegue es el que se usa para hablar con el cliente de costos de hosting? ¿Qué información tiene ese diagrama que el de componentes no?

---

## Qué viene en el Módulo 5

Ya tenés todas las piezas: entradas (M1, M3), decisiones (M2), componentes y sus dos diagramas (M4). El Módulo 5 las usa todas juntas sobre un caso real — una clínica que quiere digitalizar sus turnos — recorriendo las preguntas que se hace un arquitecto desde cero hasta la propuesta de despliegue. Y cierra la serie con una advertencia de 1968 que sigue vigente: la Ley de Conway.

---

**FIN DEL MÓDULO 4**
