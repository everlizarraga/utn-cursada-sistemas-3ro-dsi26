# 🧱 Clase desde Cero — Clase 01 · Módulo 1: Diseñar y requerir

**Unidad:** `clase01` — Arquitectura de Software (25/03/2026)
**Serie:** Módulo 1 de 5 — el mapa completo, los hilos y el orden están en el roadmap de la serie.

---

## Sobre este documento

**Qué cubre:** qué es la asignatura y qué pide para aprobarla · qué significa *diseñar* un sistema · qué es un requerimiento y qué es un requisito · las seis cualidades de un buen requisito · requerimientos funcionales vs. no funcionales · para qué sirven los requerimientos.

**Qué NO cubre:** qué es la arquitectura de software (Módulo 2), cuáles son sus entradas (Módulo 3), cómo se dibuja (Módulo 4), el caso integrador (Módulo 5). En este módulo no hay código: es vocabulario y criterio — pero es el vocabulario sobre el que se para todo lo demás.

## De dónde venís

De la preclase 01: el paradigma orientado a objetos y la notación de diagrama de clases. Este módulo casi no los usa; los vas a necesitar del Módulo 2 en adelante. No se asume nada más.

---

## 1. La asignatura: qué se ve y qué te piden 🟡

Diseño de Sistemas de Información se organiza en **tres grandes bloques temáticos** que se van trenzando durante el año:

```
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│  Modelo de Dominio  │  │  Modelado de Datos  │  │   Arquitectura de   │
│     en Objetos      │  │  & Persistencia de  │  │     Sistemas y      │
│                     │  │        Datos        │  │     de Software     │
└─────────────────────┘  └─────────────────────┘  └─────────────────────┘
```

- **Modelo de Dominio en Objetos:** cómo representar el negocio con clases, objetos y patrones de diseño.
- **Modelado de Datos & Persistencia de Datos:** cómo guardar eso en una base de datos y no perderlo al apagar el servidor.
- **Arquitectura de Sistemas y de Software:** las decisiones grandes de estructura — el tema que abre esta unidad.

### Las reglas del juego

**Para regularizar** (poder rendir final):

1. Aprobar los dos parciales, en cualquier instancia, con nota **≥ 6**.
2. Aprobar el Trabajo Práctico Anual.
3. Aprobar las prácticas de clases calificadas.

**Para promocionar** (aprobar sin final):

1. Aprobar los dos parciales con nota **≥ 8**. Si un parcial está aprobado pero con nota menor a 8, **se lo puede recuperar** — el recuperatorio no es solo para desaprobados: también sirve para levantar un 6 o un 7 hasta el 8.
2. Aprobar el TPA con **8 como nota individual** (la nota es de cada integrante, no del grupo), antes de la primera instancia final de diciembre.
3. Aprobar las prácticas de clases calificadas.

**El Trabajo Práctico Anual (TPA):** acompaña los temas de toda la asignatura; la finalidad es diseñar e implementar un Sistema de Información completo, pasando por todas las etapas del diseño. Es grupal — equipos de **6 integrantes como máximo** (algunos grupos de 5) — y tiene **6 entregas**: 3 en el primer cuatrimestre y 3 en el segundo. Cierra con una entrega final **expositiva y defensiva** por parte del equipo.

---

## 2. Qué significa "diseñar" 🔴

Antes de escribir una línea de código, alguien tiene que decidir **qué forma va a tener la solución**. Eso es diseñar:

> **Diseñar es pensar, modelar y diagramar una solución conceptual al problema.**

La imagen que lo sostiene todo: el diseño es **el plano de un edificio**. El plano tiene todas las especificaciones que le dan forma y estructura a la construcción — cuántos pisos, dónde van las escaleras, por dónde pasan los caños — y se dibuja **antes** de poner el primer ladrillo. El diseño de un sistema es exactamente eso: el plano general, con todas las especificaciones que le dan forma y estructura al sistema.

Y hay una parte de la definición que conviene subrayar, porque trae el gesto central de toda la materia:

> El diseño incluye **la evaluación de las distintas soluciones alternativas** y la especificación de una de ellas de forma detallada.

Diseñar no es encontrar "la" solución — es poner **varias alternativas sobre la mesa, compararlas y justificar la elegida**. Ese movimiento (alternativas → comparación → elección justificada) es lo que esta materia entrena y lo que evalúa. Guardalo: va a volver en cada clase.

### Diseño de sistemas y diseño de software

Dos definiciones formales que conviven, una más abarcativa que la otra:

**Diseño (de sistemas):**

> "Es la estrategia de alto nivel para resolver problemas y construir una solución. Ésta incluye decisiones acerca de la organización del sistema en subsistemas, la asignación de subsistemas a componentes de hardware y software, y decisiones fundamentales, conceptuales y de política, que son las que constituyen un marco de trabajo para el diseño detallado."
> — *Modelado y Diseño Orientados a Objetos*, James Rumbaugh (1997)

Fijate qué decide: organización en subsistemas, asignación a hardware **y** software, decisiones de política. Es estrategia — el marco dentro del cual después se diseña el detalle.

> 🕳️ **Madriguera — Rumbaugh**
> James Rumbaugh es uno de los tres creadores de UML, la notación de diagramas que venís usando desde la preclase 01. Por eso su definición de diseño es la referencia de la materia.
> *Volvé al camino — esto se profundiza aparte, otro día.*

**Diseño de software:**

> Es una descripción de la estructura del software que se va a implementar, los modelos y las estructuras de datos utilizados por el sistema, las interfaces entre componentes del sistema y, en ocasiones, los algoritmos usados.

Más acotado: ya no habla de hardware ni de política organizacional — habla de estructura del software, modelos, datos e interfaces entre componentes.

### Por qué importa

- **Los ingenieros piensan soluciones.** No solo construyen: deciden qué construir.
- Las soluciones deben ser **las más adecuadas para ese problema en ese contexto** — económico, social, cultural, tecnológico. La misma necesidad en dos contextos distintos puede pedir dos diseños distintos.
- Para lograrlo hay que **entender profundamente el problema y su contexto**. Un "buen producto" no es el más sofisticado: es el que resuelve bien la necesidad real. ¿Y cómo se captura la necesidad real? Con lo que viene en la sección siguiente.

El lugar del diseño en el camino completo:

```
  Problema / necesidad
          │  entenderlo (análisis)
          ▼
   Requerimientos ──── "qué hace falta"
          │  decidir la forma (DISEÑO)
          ▼
   Plano del sistema ─ "cómo va a ser"
          │  construirlo (implementación)
          ▼
       Sistema
```

> **Para el parcial, si te preguntan — ¿qué es diseñar un sistema?**
> Diseñar es pensar, modelar y diagramar una solución conceptual al problema: el plano general del sistema, con las especificaciones que le dan forma y estructura. Incluye evaluar distintas soluciones alternativas y especificar en detalle la elegida.

---

## 3. Requerimiento y requisito 🔴

Dos palabras que en el día a día se usan como sinónimos, pero que la materia distingue con precisión:

| | **Requerimiento** | **Requisito** |
|---|---|---|
| Qué es | Todas las **necesidades y deseos** pedidos por el cliente y las personas involucradas en el sistema | Todas las **funcionalidades, características y restricciones** que debería tener el sistema |
| La palabra apunta a… | Una **necesidad** — un concepto orientado a la carencia o falta de algo (así lo define la RAE) | Una **condición necesaria** para algo — una circunstancia que debe cumplirse |

El matiz, aterrizado con el caso que vas a trabajar en esta unidad (SmartLife, un ecosistema de dispositivos inteligentes cuyos clientes pagan una cuota mensual):

- *"Necesito saber cuánto cobrarle a cada cliente por mes"* → eso es un **requerimiento**: una necesidad, dicha en el idioma de quien la padece.
- *"El sistema debe calcular el monto mensual de cada cliente según su tipo de plan"* → eso es un **requisito**: la condición concreta que el sistema debe cumplir para satisfacer aquella necesidad.

> **Para el parcial, si te preguntan — ¿requerimiento o requisito?**
> Requerimiento es la necesidad o deseo pedido por el cliente y los involucrados (orientado a la carencia); requisito es la condición concreta — funcionalidad, característica o restricción — que el sistema debe cumplir. El requerimiento nombra la necesidad; el requisito la condición que la satisface.

---

## 4. Las seis cualidades de un buen requisito 🔴

No alcanza con tener requisitos: tienen que estar **bien escritos**. Un buen requisito cumple seis cualidades — sin jerarquía entre ellas, las seis a la vez:

| Cualidad | Qué exige |
|---|---|
| **No ambiguo** | El texto debe ser claro, preciso y tener una única interpretación posible. |
| **Conciso** | Debe ser preciso y comprensible por personas no técnicas ni especializadas. |
| **Consistente** | Ningún requisito debe entrar en conflicto con otro, ni con parte de otro. El lenguaje entre requisitos también debe ser consistente. |
| **Completo** | Debe contener en sí mismo toda la información necesaria, sin redirigir a fuentes externas que lo expliquen. |
| **Alcanzable** | Debe ser un objetivo realista, posible con el dinero, el tiempo y los recursos disponibles. |
| **Verificable** | Se debe poder verificar con certeza si se satisfizo o no — por inspección, análisis, demostración o testeo. |

### Verlas trabajar

La forma de fijarlas no es memorizarlas: es agarrar un requisito mal escrito y detectar **cuál falla**.

> *"El sistema debe ser rápido."*

- ❌ **No ambiguo:** ¿rápido para quién? ¿En qué operación? Cada lector entiende algo distinto.
- ❌ **Verificable:** ¿cómo comprobás con certeza que "es rápido"? No hay criterio de verificación posible.

Reescrito para cumplir las seis:

> ✅ *"El sistema debe responder las consultas de monto mensual en menos de 2 segundos."*

Ahora tiene una única interpretación y un criterio medible: se puede testear y decir sí o no. El mismo ejercicio funciona con cualquier cualidad: dos requisitos que se contradicen fallan en **consistente**; uno que dice "ver documento anexo para el detalle" falla en **completo**; "el sistema debe predecir qué plan va a querer cada cliente antes de que lo pida" probablemente falla en **alcanzable**.

> **Para el parcial, si te preguntan — te dan un requisito mal escrito.**
> Nombrá la cualidad que falla, justificá por qué falla ahí (con la definición de la cualidad), y reescribilo cumpliéndola. El molde: "Falla en [cualidad] porque [motivo]; quedaría mejor así: [versión corregida]".

---

## 5. Funcionales y no funcionales 🔴

Los requerimientos se clasifican en dos familias:

```
                                 ┌─────────────────┐
                          ┌─────▶│   Funcionales   │  "QUÉ debe hacer"
   ┌─────────────────┐    │      └─────────────────┘
   │  Requerimientos │────┤
   └─────────────────┘    │      ┌─────────────────┐
                          └─────▶│ No funcionales  │  "CÓMO debe funcionar"
                                 └─────────────────┘
                                         (*)
                                      • Restricciones
                                      • Reglas de negocio
```

### Funcionales (RF) — *lo que el sistema debe hacer*

Son declaraciones de los **servicios** que debe proporcionar el sistema, de cómo debe reaccionar a entradas particulares y de cómo debe comportarse en situaciones particulares. En algunos casos, también declaran explícitamente **lo que el sistema NO debe hacer**.

### No funcionales (RNF) — *cómo debe funcionar el sistema*

- Son **restricciones de las funcionalidades** ofrecidas por el sistema.
- Pueden incluir restricciones sobre el proceso de desarrollo y el uso de estándares.
- No se refieren a funciones específicas, sino a las **propiedades emergentes** del sistema — propiedades que no viven en una función puntual, sino que surgen del sistema funcionando como un todo (que sea rápido, que esté disponible, que sea seguro).

**(*)** Dentro de los no funcionales, el esquema de la materia ubica también las **restricciones** y las **reglas de negocio**.

### Uno al lado del otro, con SmartLife

| Tipo | Ejemplo |
|---|---|
| **RF** | "El sistema debe permitir registrar los dispositivos de cada cliente" |
| **RF** | "El sistema debe calcular el monto mensual según el tipo de plan" |
| **RNF** | "Debe responder en menos de 2 segundos" |
| **RNF** | "Debe estar disponible las 24 horas" |
| **RNF** (restricción) | "Debe desarrollarse en Java" |

El truco para clasificar rápido: si responde **"¿qué hace?"**, es funcional; si responde **"¿cómo lo hace / bajo qué condiciones?"**, es no funcional.

Y una anticipación de un renglón, porque es el hilo que ata este módulo con el resto de la serie: **los RNF no son el decorado de los RF — son la entrada principal de la arquitectura**. Por qué y cómo, en el Módulo 3.

> **Para el parcial, si te preguntan — RF vs. RNF.**
> Los funcionales describen **qué** debe hacer el sistema (los servicios que proporciona y cómo reacciona a entradas); los no funcionales describen **cómo** debe funcionar: restricciones sobre las funcionalidades y propiedades emergentes del sistema. Ejemplo: "registrar dispositivos" es RF; "responder en menos de 2 segundos" es RNF. Dentro de los no funcionales se ubican también las restricciones y las reglas de negocio.

---

## 6. Para qué sirven los requerimientos 🟡

Cerrando el módulo: por qué todo este aparato importa para el **diseño** de un sistema. Los requerimientos:

- **Son un acuerdo** entre el equipo de desarrollo, los clientes y los usuarios finales — los interesados del proyecto. Todos firman sobre el mismo texto: por eso las cualidades de la sección 4 no son perfeccionismo, son la condición para que el acuerdo funcione.
- **Son la base para el diseño del sistema.** El plano se dibuja a partir de ellos — sin requerimientos claros, el diseño arranca a ciegas.
- **Brindan soporte para verificación y validación** (verificar: comprobar que el sistema se construyó bien; validar: comprobar que se construyó el sistema correcto — el que la necesidad pedía).
- **Brindan soporte para la evolución del sistema:** cuando el sistema crezca, los requerimientos documentan qué se prometió y sobre qué se está construyendo.
- **Permiten la detección temprana de errores:** un error encontrado en el texto de un requisito se corrige borrando una línea; el mismo error encontrado con el sistema construido cuesta muchísimo más.

> 🕳️ **Madriguera — Ingeniería de Requisitos**
> Capturar, escribir, negociar y mantener requerimientos es una disciplina entera, con técnicas propias (entrevistas, casos de uso, prototipos). En la carrera existe incluso como materia electiva. Acá te quedás con lo que esta materia necesita: qué son, cómo se clasifican y qué los hace buenos.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## ✅ Checkpoint — Módulo 1

*Sin respuestas acá: intentalas sin mirar el material, y las dudas se resuelven por chat.*

1. Un cliente te dice: *"quiero que la app sea segura y que no se caiga nunca"*. ¿Eso es un requerimiento o un requisito? ¿Qué le falta para convertirse en buenos requisitos del sistema?
2. Clasificá como RF o RNF, justificando en una línea: **a)** "El sistema debe permitir dar de baja un dispositivo" · **b)** "El sistema debe soportar 500 clientes consultando a la vez" · **c)** "El sistema debe registrar cada pago recibido" · **d)** "El sistema debe desarrollarse usando PostgreSQL".
3. *"El sistema debe soportar muchos usuarios simultáneos."* ¿Qué cualidades falla este requisito y por qué? Reescribilo para que las cumpla.
4. Un requisito dice "los clientes corporativos pagan tarifa personalizada con descuento" y otro dice "todos los clientes pagan la misma tarifa base". ¿Qué cualidad está fallando? ¿Por qué es grave que falle justo esa?
5. La definición de diseño habla de "evaluar las distintas soluciones alternativas". ¿Por qué evaluar **varias**, si con encontrar una que funcione alcanzaría? ¿Qué se pierde si se salta ese paso?
6. ¿Qué diferencia hay entre el diseño de sistemas (Rumbaugh) y el diseño de software? Nombrá algo que decide el primero y no el segundo.
7. ¿Por qué un error detectado en la etapa de requerimientos cuesta menos que el mismo error detectado con el sistema ya construido? Relacionalo con la imagen del plano.
8. De las seis cualidades, ¿cuál te parece que protege más directamente el rol de los requerimientos como "acuerdo entre las partes"? Justificá.

---

## Qué viene en el Módulo 2

Ya sabés qué te piden (requerimientos) y qué significa diseñar (el plano). El Módulo 2 presenta a la disciplina que toma las decisiones más grandes de ese plano: **la arquitectura de software** — qué es un componente, qué es su interfaz, y por qué hay decisiones que, una vez tomadas, gobiernan al sistema durante años.

---

**FIN DEL MÓDULO 1**
