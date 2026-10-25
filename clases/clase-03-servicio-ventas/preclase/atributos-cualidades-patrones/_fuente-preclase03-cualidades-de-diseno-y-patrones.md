# Diseño de Sistemas — Cualidades de Diseño y Patrones de Diseño

> Conversión fiel a Markdown de Clase_02_-_2025__1_.pdf (43 slides).

## Notas de conversión

1. **Los nombres de las subcaracterísticas no coinciden entre la slide-resumen 7 y las slides de detalle.** Ejemplos: Usabilidad → *Inteligibilidad / Aprendizaje / Operabilidad* (slide 7) vs. *Adecuación / Capacidad de aprendizaje / Capacidad para ser usado* (slide 15); Fiabilidad → *Capacidad de recuperación* (slide 7) vs. *Recuperabilidad* (slide 17); Portabilidad → *Facilidad de instalación* (slide 7) vs. *Capacidad para ser instalado* (slide 23). En Seguridad, además, el orden de los dos últimos ítems se invierte entre la slide 7 y la 19. Todo se transcribe tal como figura en cada slide.
2. **Slide 21, Modularidad:** el original dice *"Capacidad de un que permite que un cambio…"* — falta una palabra. Se transcribe tal cual.
3. **Slide 39:** la viñeta *"Implementación Código de ejemplo"* aparece en una sola línea, con una sola viñeta. Se transcribe tal cual.
4. **Slide 42:** el original escribe *"Chain of Responsability"*. Se transcribe tal cual.
5. **Slides 39 y 40:** no tienen título en la diapositiva.
6. **Slide 34:** la palabra "Link" es un hipervínculo; la URL se extrajo de las anotaciones del PDF y va inline.
7. **Slides 9, 11, 13, 15, 17, 19, 21 y 23:** presentan las subcaracterísticas como una pila de cajas redondeadas de colores (de violeta a verde), cada una con su nombre y su definición en viñeta. Se transcriben como listas; la disposición no agrega información.
8. El pie de página institucional ("Cátedra Diseño de Sistemas de Información – UTN BA") y el número de slide se repiten en todas las diapositivas y no se transcriben.

---

## Slide 1 — Diseño de Sistemas

*(Portada.)*

**Descripción del elemento visual:** portada con una **onda violeta** en la mitad inferior sobre la que se lee el título **"Diseño de Sistemas"** en blanco. En la mitad superior, sobre fondo blanco: a la izquierda, un **diagrama de clases UML** en línea negra (cajas de clases vinculadas con multiplicidades `1`, `1..*`, `0..*`, `*` y una flecha de generalización); en el centro, un **esquema pequeño de cajas conectadas** tipo modelo de datos; a la derecha, la ilustración de una **hoja con un organigrama y una lupa**, con una **lamparita encendida** arriba; y en el extremo superior derecho, el ícono de una **ventana de navegador con `</>`**. Abajo a la derecha, el logo **UTN.BA — Cátedra Diseño de Sistemas de Información**.

---

## Slide 2 — Agenda

- Cualidades de Diseño y/o Atributos de Calidad de Software
- Patrones de Diseño y sus Clasificaciones

**Descripción del elemento visual:** en la parte superior, dos **fotografías**: a la izquierda, un escritorio con una notebook, una taza de café, una lapicera y documentos anillados; a la derecha, un grupo de estudiantes trabajando juntos en una biblioteca, con libros, una tablet y notebooks.

---

## Slide 3 — Calidad de Software

Para la norma ISO 25000:

> "La calidad del producto software se puede interpretar como el grado en que dicho producto satisface los requisitos de sus usuarios aportando de esta manera un valor. Son precisamente estos requisitos (funcionalidad, rendimiento, seguridad, mantenibilidad, etc.) los que se encuentran representados en el modelo de calidad, el cual categoriza la calidad del producto en características y subcaracterísticas."

---

## Slide 4 — Calidad de Software – ISO 25000

El modelo de calidad del producto definido por la ISO 25010 se encuentra compuesto por las siguientes ocho características…

---

## Slide 5 — *(Diagrama: las ocho características)*

```
                        ┌───────────────────────────┐
                        │   Calidad del Producto     │
                        │         Software           │
                        └─────────────┬─────────────┘
        ┌───────────┬───────────┬─────┴─────┬───────────┬───────────┬───────────┬───────────┐
        │           │           │           │           │           │           │           │
 ┌──────▼─────┐ ┌───▼──────┐ ┌──▼────────┐ ┌▼─────────┐ ┌▼─────────┐ ┌▼────────┐ ┌▼─────────┐ ┌▼────────┐
 │ Adecuación │ │Eficiencia│ │Compatibi- │ │Usabilidad│ │Fiabilidad│ │Seguridad│ │Manteni-  │ │Portabi- │
 │ funcional  │ │   de     │ │  lidad    │ │          │ │          │ │         │ │bilidad   │ │lidad    │
 │            │ │Desempeño │ │           │ │          │ │          │ │         │ │          │ │         │
 └────────────┘ └──────────┘ └───────────┘ └──────────┘ └──────────┘ └─────────┘ └──────────┘ └─────────┘
```

**Descripción del elemento visual:** diagrama jerárquico de un solo nivel. Una caja superior rotulada **"Calidad del Producto Software"** se conecta hacia abajo con **ocho cajas** de igual tamaño, en degradé de violeta a verde: **Adecuación funcional**, **Eficiencia de Desempeño**, **Compatibilidad**, **Usabilidad**, **Fiabilidad**, **Seguridad**, **Mantenibilidad** y **Portabilidad**.

---

## Slide 6 — Calidad de Software – ISO 25000

A su vez, estas grandes características se subdividen en varias subcaracterísticas…

---

## Slide 7 — *(Tabla: características y subcaracterísticas)*

| Adecuación funcional | Eficiencia de Desempeño | Compatibilidad | Usabilidad | Fiabilidad | Seguridad | Mantenibilidad | Portabilidad |
|---|---|---|---|---|---|---|---|
| Completitud funcional | Comportamiento temporal | Coexistencia | Inteligibilidad | Madurez | Confidencialidad | Modularidad | Adaptabilidad |
| Corrección funcional | Utilización de recursos | Interoperabilidad | Aprendizaje | Disponibilidad | Integridad | Reusabilidad | Facilidad de instalación |
| Pertinencia funcional | Capacidad | | Operabilidad | Tolerancia a fallos | No repudio | Analizabilidad | Capacidad de ser reemplazado |
| | | | Protección frente a errores de usuario | Capacidad de recuperación | Autenticidad | Capacidad de ser modificado | |
| | | | Estética | | Responsabilidad | Capacidad de ser probado | |
| | | | Accesibilidad | | | | |

**Descripción del elemento visual:** ocho columnas verticales, cada una encabezada por una caja de color con el nombre de la característica (violeta → verde, mismos colores que la slide 5) y, debajo, las cajas más claras con sus subcaracterísticas en el orden de la tabla.

---

## Slide 8 — Adecuación funcional (Funcionalidad)

*La capacidad del producto software para proveer las funciones que satisfacen las necesidades explícitas e implícitas cuando el software se utiliza bajo condiciones específicas.*

---

## Slide 9 — Adecuación funcional (Funcionalidad)

**Completitud funcional**
- Grado en el cual el conjunto de funcionalidades cubre todas las tareas y los objetivos del usuario especificados.

**Corrección funcional**
- Capacidad del producto o sistema para proveer resultados correctos con el nivel de precisión requerido.

**Pertinencia funcional**
- Capacidad del producto software para proporcionar un conjunto apropiado de funciones para tareas y objetivos de usuario especificados.

---

## Slide 10 — Eficiencia de desempeño

*Esta característica representa el desempeño relativo a la cantidad de recursos utilizados bajo determinadas condiciones.*

---

## Slide 11 — Eficiencia de desempeño

**Comportamiento temporal**
- Los tiempos de respuesta y procesamiento y los ratios de *throughput* de un sistema cuando lleva a cabo sus funciones bajo condiciones determinadas en relación con un banco de pruebas (*benchmark*) establecido.

**Utilización de recursos**
- Las cantidades y tipos de recursos utilizados cuando el software lleva a cabo su función bajo condiciones determinadas.

**Capacidad**
- Grado en que los límites máximos de un parámetro de un producto o sistema software cumplen con los requisitos.

---

## Slide 12 — Compatibilidad

*Capacidad de dos o más sistemas o componentes para intercambiar información y/o llevar a cabo sus funciones requeridas cuando comparten el mismo entorno hardware o software.*

---

## Slide 13 — Compatibilidad

**Coexistencia**
- Capacidad del producto para coexistir con otro software independiente, en un entorno común, compartiendo recursos comunes sin detrimento.

**Interoperabilidad**
- Capacidad de dos o más sistemas o componentes para intercambiar información y utilizar la información intercambiada.

---

## Slide 14 — Usabilidad

*La capacidad del producto software de ser entendido, aprendido, usado y atractivo al usuario, cuando es usado bajo las condiciones especificadas.*

---

## Slide 15 — Usabilidad

**Adecuación**
- Capacidad del producto que permite al usuario entender si el software es adecuado para sus necesidades.

**Capacidad de aprendizaje**
- Capacidad del producto que permite al usuario aprender su aplicación.

**Capacidad para ser usado**
- Capacidad del producto que permite al usuario operarlo y controlarlo con facilidad.

**Protección contra errores de usuario**
- Capacidad del sistema para proteger a los usuarios de hacer errores.

**Estética de la interfaz**
- Capacidad de la interfaz de usuario de agradar y satisfacer la interacción con el usuario.

**Accesibilidad**
- Capacidad del producto que permite que sea utilizado por usuarios con determinadas características y discapacidades.

---

## Slide 16 — Fiabilidad

*Capacidad de un sistema o componente para desempeñar las funciones especificadas, cuando se usa bajo unas condiciones y periodo de tiempo determinados*

---

## Slide 17 — Fiabilidad

**Madurez**
- Es la capacidad del producto software para evitar fallas como resultado de errores en el software.

**Disponibilidad**
- Capacidad del sistema o componente de estar operativo y accesible para su uso cuando se requiere

**Tolerancia a fallos**
- Es la capacidad del producto software para mantener un nivel especificado de funcionamiento en caso de errores del software o de incumplimiento de su interfaz especificada.

**Recuperabilidad**
- Es la capacidad del producto software para reestablecer un nivel especificado de funcionamiento y recuperar los datos afectados directamente en el caso de una falla.

---

## Slide 18 — Seguridad

*Capacidad de protección de la información y los datos de manera que personas o sistemas no autorizados no puedan leerlos o modificarlos.*

---

## Slide 19 — Seguridad

**Confidencialidad**
- Capacidad de protección contra el acceso de datos e información no autorizados, ya sea accidental o deliberadamente.

**Integridad**
- Capacidad del sistema o componente para prevenir accesos o modificaciones no autorizados a datos o programas de ordenador.

**No repudio**
- Capacidad de demostrar las acciones o eventos que han tenido lugar, de manera que dichas acciones o eventos no puedan ser repudiados posteriormente.

**Responsabilidad**
- Capacidad de rastrear de forma inequívoca las acciones de una entidad.

**Autenticidad**
- Capacidad de demostrar la identidad de un sujeto o un recurso.

---

## Slide 20 — Mantenibilidad

*Esta característica representa la capacidad del producto software para ser modificado efectiva y eficientemente, debido a necesidades evolutivas, correctivas o perfectivas.*

---

## Slide 21 — Mantenibilidad

**Modularidad**
- Capacidad de un que permite que un cambio en un componente tenga un impacto mínimo en los demás.

**Reusabilidad**
- Capacidad de un activo que permite que sea utilizado en más de un sistema software o en la construcción de otros activos.

**Analizabilidad**
- Facilidad con la que se puede evaluar el impacto de un determinado cambio sobre el resto del software, diagnosticar las deficiencias o causas de fallos en el software, o identificar las partes a modificar.

**Capacidad para ser modificado**
- Capacidad del producto que permite que sea modificado de forma efectiva y eficiente sin introducir defectos o degradar el desempeño.

**Capacidad para ser probado ("Testeabilidad")**
- Facilidad con la que se pueden establecer criterios de prueba para un sistema o componente y con la que se pueden llevar a cabo las pruebas para determinar si se cumplen dichos criterios.

---

## Slide 22 — Portabilidad

*Capacidad del producto o componente de ser transferido de forma efectiva y eficiente de un entorno hardware, software, operacional o de utilización a otro.*

---

## Slide 23 — Portabilidad

**Adaptabilidad**
- La capacidad del producto software para ser adaptado a diferentes entornos definidos sin aplicar acciones o medios diferentes de los previstos para el propósito del software considerado.

**Capacidad para ser instalado**
- Facilidad con la que el producto se puede instalar y/o desinstalar de forma exitosa en un determinado entorno.

**Capacidad para ser reemplazado**
- Capacidad del producto para ser utilizado en lugar de otro producto software determinado con el mismo propósito y en el mismo entorno.

---

## Slide 24 — Cualidades de Diseño

- Las Cualidades de Diseño sirven como criterios para analizar distintas propuestas/alternativas y tomar decisiones más formadas respecto a ellas.
- No son los únicos criterios: experiencia y conocimiento de quien esté realizando el proceso de diseño, también serán elementos determinantes.

**Descripción del elemento visual:** el texto va sobre un **fondo violeta** a la izquierda. A la derecha, tres fotografías apiladas: un **"100 %" escrito con tiza** en un pizarrón, un **estudiante con auriculares frente a una notebook** en una biblioteca, y un **lápiz sobre una hoja de examen de opción múltiple** con burbujas a completar.

---

## Slide 25 — *(Diagrama: Cualidades de Diseño)*

```
                    ┌──────────────────────┐
                    │  Cualidades de       │
                    │      Diseño          │
                    └──────────┬───────────┘
        ┌──────────────┬───────┴──────┬──────────────┬──────────────┐
        │              │              │              │              │
 ┌──────▼──────┐ ┌─────▼─────┐ ┌──────▼──────┐ ┌─────▼────────┐ ┌───▼──────┐
 │ Simplicidad │ │ Robustez  │ │ Flexibilidad│ │Acoplamiento  │ │ Cohesión │
 └─────────────┘ └───────────┘ └──────┬──────┘ └──────────────┘ └──────────┘
                                      │
                        ┌─────────────┴─────────────┐
                 ┌──────▼────────┐         ┌────────▼────────┐
                 │ Extensibilidad│         │ Mantenibilidad  │
                 └───────────────┘         └─────────────────┘
```

**Descripción del elemento visual:** diagrama jerárquico. De la caja **"Cualidades de Diseño"** (violeta, a la izquierda) dependen, en una fila superior, **Simplicidad**, **Robustez** y **Flexibilidad**; de **Flexibilidad** cuelgan, en una fila intermedia, **Extensibilidad** y **Mantenibilidad**; y en la fila inferior aparecen **Acoplamiento** y **Cohesión**.

---

## Slide 26 — Acoplamiento

*El acoplamiento se define como el grado de dependencia entre dos componentes, es decir, el grado de conocimiento que un componente tiene sobre el otro.*

Cuanto mayor sea el acoplamiento entre dos componentes, los cambios o errores de uno de ellos impactarán en mayor medida sobre el otro componente.

---

## Slide 27 — Acoplamiento

Si minimizamos el acoplamiento podríamos:

- Mejorar la mantenibilidad
- Aumentar la reutilización
- Evitar que un defecto de un componente se propague a otros
- Evitar tener que inspeccionar y/o modificar múltiples componentes ante una modificación en uno solo de ellos

---

## Slide 28 — Cohesión

- *Un componente cohesivo tiende a tener todos sus elementos abocados a resolver el mismo problema.*
- *La cohesión se define como la cantidad de responsabilidades que están asignadas a un componente.*
- *Mientras más responsabilidades tenga un componente, menos cohesivo será.*

---

## Slide 29 — Simplicidad

**KISS** – *Keep it simple, stupid*

**YAGNI** – *You aren't gonna need it*

---

## Slide 30 — Simplicidad

**KISS** – *Keep it simple, stupid*

*Nos propone evitar cualquier complejidad innecesaria.*

---

## Slide 31 — Simplicidad

**YAGNI** – *You aren't gonna need it*

*Nos propone no agregar funcionalidad nueva que no apunte a la problemática actual.*

---

## Slide 32 — Robustez

Nos dice que ante un uso inadecuado por parte del usuario, sistemas externos o ante fallas internas:

- El sistema no debe generar información o comportamiento inconsistente/errático
- El sistema debe reportar los errores y volver a un estado consistente
- El sistema debe facilitar tanto como sea posible la detección de la causa del problema

---

## Slide 33 — Flexibilidad

Es la capacidad de reflejar cambios en el dominio de manera simple y sencilla.

- *Extensibilidad*: capacidad de agregar nuevas características con "poco" impacto.
- *Mantenibilidad*: capacidad de modificar las características existentes con "el menor" esfuerzo posible.

---

## Slide 34 — Más sobre Cualidades de Diseño…

[Link](https://docs.google.com/document/d/14HdvHvS33WqYb6Ak0BGa0IeCTbzeCRSDKs-1Ot-qLDw/edit)

---

## Slide 35 — Patrones de Diseño

*(Slide separadora de sección — solo título.)*

**Descripción del elemento visual:** banda superior **violeta** con el título en blanco, sobre un fondo de **diagrama de secuencia UML** tenue; mitad inferior **negra** con tres imágenes: el ícono de una **ventana de navegador con `</>`**, un **diagrama de flujo de colores** (cajas rosa, naranja, verde y un rombo azul conectados por líneas blancas) y el logo de **UML — Unified Modeling Language**.

---

## Slide 36 — Patrones de Diseño

> "Son descripciones de clases y objetos relacionados que están adaptados para resolver un problema de diseño general en un contexto determinado"
>
> Erich Gamma, Richard Helm, John Vlissides y Ralph Johnson

> "Consiste en un diagrama de objetos que forma una solución a un problema conocido y frecuente"
>
> Laurent Debrauwer - Patrones de diseño en Java

---

## Slide 37 — Patrones de Diseño

> "Soluciones conocidas a problemas conocidos y reiterados en el mundo del Desarrollo de Software"

---

## Slide 38 — Patrones de Diseño

El objetivo es reutilizar la experiencia de quienes ya se han encontrado con problemas similares y han encontrado una buena solución.

---

## Slide 39 — Patrones de Diseño - Estructura

- Propósito
- Motivación
- Participantes
- Colaboraciones
- Consecuencias
- Implementación Código de ejemplo
- Usos conocidos
- Patrones relacionados

---

## Slide 40 — Patrones de Diseño - Partes esenciales

- **Nombre:** Comunica el objetivo del patrón en una o dos palabras. Aumenta el vocabulario sobre diseño.
- **Problema:** Describe el problema que el patrón soluciona y su contexto. Indica cuándo se aplica el patrón.
- **Solución:** Indica cómo resolver el problema en términos de elementos, relaciones, responsabilidades y colaboraciones. La solución debe ser lo suficientemente abstracta para poder ser aplicada en diferentes situaciones.
- **Consecuencias:** Indica los efectos de aplicar la solución. Son críticas al momento de evaluar distintas alternativas de diseño.

---

## Slide 41 — *(Clasificación de patrones)*

**Creacionales**
- Abstraen el proceso de creación/instanciación de los objetos. Se los suele utilizar cuando debemos crear objetos, complejos o no, tomando decisiones dinámicas en momento de ejecución.

**Comportamiento**
- Resuelven cuestiones, complejas o no, de interacción entre objetos en momento de ejecución.

**Estructurales**
- Resuelven cuestiones, generalmente complejas, de generación y/o utilización de estructuras complejas o que no están acopladas al dominio.

**Descripción del elemento visual:** tres columnas de igual ancho, cada una con su encabezado de color (**Creacionales** violeta, **Comportamiento** azul, **Estructurales** verde) y el texto correspondiente debajo.

---

## Slide 42 — *(Patrones por categoría)*

| Creacionales | Comportamiento | Estructurales |
|---|---|---|
| Factory Method | State | Adapter |
| Simple Factory | Strategy | Composite |
| Singleton | Observer | Facade |
| Abstract Factory | Command | Decorator |
| Builder | Template method | Proxy |
| Prototype | Iterator | Flyweight |
| | Memento | Bridge |
| | Visitor | |
| | Interpreter | |
| | Chain of Responsability | |
| | Mediator | |

**Descripción del elemento visual:** tres bloques horizontales apilados, uno por categoría (**Creacionales** violeta, **Comportamiento** azul, **Estructurales** verde), con los patrones listados en viñetas dentro de cada bloque, distribuidos en varias columnas.

---

## Slide 43 — Gracias

**Descripción del elemento visual:** la palabra **"Gracias"** en azul oscuro sobre fondo gris claro, a la izquierda; a la derecha, una fotografía circular de **estudiantes escribiendo en un examen** sobre pupitres. Abajo, el logo **UTN.BA — Cátedra Diseño de Sistemas de Información**.

---

**FIN DEL ARCHIVO FUENTE — Diseño de Sistemas: Cualidades de Diseño y Patrones de Diseño**
