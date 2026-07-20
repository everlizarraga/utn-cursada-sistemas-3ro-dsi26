# Apunte Maestro — Preclase 03 · Parte 3: Patrones de Diseño

**Dónde estamos.** Tercera y última parte de la unidad. La Parte 1 dio los **atributos de calidad** (qué querés lograr) y la Parte 2 las **cualidades de diseño** (cómo lo lográs, con acoplamiento, cohesión, simplicidad, etc.). Los **patrones de diseño** son el siguiente escalón: **soluciones ya probadas** a problemas de diseño que aparecen una y otra vez. Es el tema que se trabaja durante buena parte del primer cuatrimestre.

**El puente con la Parte 2.** Guardá esto desde ahora, porque es la clave de todo el bloque: un patrón **solo se usa cuando el problema que resuelve realmente aparece**. Meter patrones "de más" es exactamente lo que **KISS y YAGNI** te dicen que no hagas. Los patrones y la simplicidad no son enemigos: la simplicidad es la que decide si un patrón corresponde o no.

**De dónde venís.** De Paradigmas ya conocés al menos un patrón (el Singleton) y, sobre todo, **teoría de objetos**. Acá esa base es innegociable — más sobre esto en el §5.

---

## 1. 🟡 Qué es un patrón de diseño

**Arranquemos por la palabra.** "Patrón" ya te sugiere la idea: **algo que se repite**. Y si se repite, es algo que **ya existe** — alguien ya se lo encontró antes.

Esa es toda la idea de fondo:

> Un patrón de diseño es una **solución conocida a un problema conocido y frecuente** del desarrollo de software.

Dos definiciones "de libro" que vas a encontrar, para tenerlas:

- **GoF** (Gamma, Helm, Vlissides y Johnson — "la Gang of Four", los autores del libro fundacional *Design Patterns*): *descripciones de clases y objetos relacionados, adaptados para resolver un problema de diseño general en un contexto determinado*.
- **Debrauwer**: *un diagrama de objetos que forma una solución a un problema conocido y frecuente*.

De estas dos, quedate con **"problema conocido y frecuente"**. Y una aclaración importante: **un patrón es mucho más que un diagrama de objetos.** Tiene varios componentes (los vemos en el §2); el diagrama es solo una parte.

**El objetivo.** No **reinventar la rueda**. Si alguien ya pensó, estudió, testeó y validó una buena solución a un problema que se repite, ¿para qué vas a gastar tiempo inventando otra desde cero? Un patrón te deja **reutilizar esa experiencia acumulada**.

Un par de precisiones de contexto:

- Los patrones que vemos son **orientados a objetos**: la materia está enmarcada en el diseño OO.
- **Diagrama de clases ≠ diagrama de objetos.** El de clases muestra la relación **estática** entre clases; el de objetos muestra **instancias** en un momento dado (es como "una foto"). No son lo mismo.

---

## 2. 🟡 Anatomía de un patrón

Un patrón "completo" (como lo estudiarías en el libro de GoF, donde cada uno ocupa unas 15 páginas) se describe con estas partes: **Propósito · Motivación · Participantes · Colaboraciones · Consecuencias · Implementación (código de ejemplo) · Usos conocidos · Patrones relacionados**.

Pero para esta materia lo que importa son las **4 partes esenciales**:

- **Nombre** — en una o dos palabras. No es un detalle: **te da vocabulario de diseño** (poder decir "acá va un Observer" y que el otro entienda todo el paquete) y muchas veces ya te anticipa qué intenta resolver.
- **Problema** — describe **qué** problema resuelve y en qué contexto; indica **cuándo** se aplica.
- **Solución** — cómo resolverlo en términos de elementos, relaciones, responsabilidades y colaboraciones. Es **abstracta a propósito**, para poder aplicarse en situaciones distintas. En la práctica termina siendo un diagrama de clases + qué hace cada participante.
- **Consecuencias** — los **efectos** de aplicar la solución.

### 🔴 Dos cosas sobre "problema", "solución" y "consecuencias" que sí caen

**Sobre el problema — no hay reglas mecánicas.** En esta área (a diferencia de análisis matemático o álgebra) **no existe** un "si ocurre X, entonces usá el patrón Y". No hay recetario automático. Tenés que **evaluar el caso**: un mismo patrón puede ser buena solución en un contexto y mala en otro. El problema se **vivencia** (se lo entiende resolviendo ejercicios), no se memoriza de una lista.

**Sobre la solución — el diagrama "frío" no vale nada.** Aprenderte de memoria el diagrama de clases de, digamos, el patrón State **no sirve para nada**. Sin entender el problema que resuelve y sus consecuencias, ese diagrama es un dibujo vacío. La solución se construye **entendiendo el problema**, no copiándola.

**Sobre las consecuencias — son lo más importante.** 🔴 Las consecuencias (los efectos, buenos y malos, de aplicar el patrón) son **críticas al momento de comparar alternativas de diseño**, y son las que te permiten **justificar** por qué aplicaste —o por qué evitaste— un patrón. Si tuvieras que priorizar qué estudiar de cada patrón, son las consecuencias.

---

## 3. 🔴 La regla de oro: cuándo usar un patrón (y cuándo NO)

Este es el punto más importante de toda la Parte 3, y de los más evaluados. Leelo con atención porque va contra la intuición.

> **Un patrón de diseño se usa ÚNICAMENTE cuando se presenta el problema que resuelve.**

No "siempre que se pueda", no "cuanto más mejor", no "depende del tipo". Únicamente cuando **aparece el problema**.

El anti-patrón mental típico —y el error que la cátedra explícitamente busca corregir— es este:

> *"Llego al parcial y meto tres patrones para demostrar que aprendí."* ❌

**Eso está mal.** No se evalúa cuántos patrones sabés ni cuántos metés. Se da por sentado que **entendés** los patrones (que sabés qué es un State, un Observer, etc.). Lo que se evalúa es tu **criterio profesional**: si supiste **aplicarlo correctamente en este contexto**, o si supiste **evitarlo con fundamento** por sus ventajas y desventajas.

Consecuencias prácticas de esto:

- **Salir de un parcial sin haber usado ningún patrón puede estar perfecto.** A veces la solución más simple es la correcta, y no lleva ningún patrón.
- **Forzar un patrón donde no hace falta = sobrediseño.** Le agregás complejidad innecesaria a algo que estaba simple. Eso viola directamente **KISS y YAGNI** (Parte 2).
- La regla operativa: **mantené la solución lo más simple posible; si el problema lo justifica, aplicás el patrón; si solo agrega complejidad, afuera.**

> 📌 **Para el parcial, si te preguntan:** cuando un caso te tiente a meter un patrón, preguntate primero *"¿está apareciendo de verdad el problema que este patrón resuelve?"*. Si la respuesta es no, **no lo pongas**, y sabé justificar por qué NO lo pusiste (por simplicidad, por YAGNI). Esa justificación vale tanto como aplicarlo bien.

---

## 4. 🟡 Clasificación: las tres familias

Los patrones se agrupan en **tres familias**, según el **tipo de problema** que atacan. Esta clasificación viene del propio GoF: observaron qué problemas se repetían al diseñar y los agruparon por naturaleza. Décadas después, sigue vigente.

### Creacionales

Atacan el problema de **crear / instanciar** objetos. Se usan cuando tenés que crear un objeto (complejo o no) **tomando decisiones de forma dinámica en tiempo de ejecución**, o resolviendo su **configuración**.

> *Ejemplo de "problema creacional":* "tengo lógica complicada para **instanciar y configurar** este objeto según lo que elija el usuario en ejecución." → probablemente te ayude un patrón creacional.

### De comportamiento

Resuelven cuestiones (complejas o no) de **interacción entre objetos** en tiempo de ejecución. **Regla mnemotécnica:** si el problema es de **interacción entre objetos**, es de comportamiento.

> *Ejemplo de "problema de comportamiento":* "necesito que un objeto **se comporte distinto según el estado** en el que esté." → problema de comportamiento.

### Estructurales

Resuelven cuestiones (generalmente complejas) de **armado y uso de estructuras**, o de trabajar con cosas **que no están acopladas a tu dominio**. Suelen apuntar a los problemas más complejos.

> *Ejemplo de "problema estructural":* "quiero **usar 5 clases que hizo otra persona, sin tocarlas** y sin depender demasiado de ellas — ¿cómo las acoplo a mi dominio?" → problema estructural. Otro: "quiero seguir con mi diseño **sin haber desarrollado todavía** cierta funcionalidad."

### El catálogo (los que existen)

No se ven todos en la cursada — no daría el cuatrimestre. Pero conviene tener el mapa:

| Creacionales | De comportamiento | Estructurales |
|---|---|---|
| Factory Method | State | Adapter |
| Simple Factory | Strategy | Composite |
| Singleton | Observer | Facade |
| Abstract Factory | Command | Decorator |
| Builder | Template Method | Proxy |
| Prototype | Iterator | Flyweight |
| | Memento | Bridge |
| | Visitor | |
| | Interpreter | |
| | Chain of Responsibility | |
| | Mediator | |

> ⚠️ **Ojo con el Singleton.** Es un creacional que quizás ya viste en Paradigmas, así que lo vas a reconocer — pero **en esta materia no se usa**. Tenelo en cuenta si te tienta ponerlo: la postura de la cátedra es no usarlo. (Sí se menciona para que sepas qué es.)

> 🕳️ **Madriguera — cuáles se ven primero.** Los primeros patrones que vas a trabajar en profundidad son de la familia de **comportamiento** (State, Strategy, Observer). El resto se van sumando a lo largo del año. Acá no hace falta la mecánica de ninguno: alcanza con el concepto, las familias y —sobre todo— la regla de oro del §3. *Volvé al camino.*

---

## 5. Prerrequisito: la base de objetos tiene que estar sólida

Un aviso que no es decorativo: para entender patrones, **la teoría de objetos tiene que estar impecable** (por algo la materia es correlativa con Paradigmas). Instancia, clase, interfaz, herencia, polimorfismo, y **saber armar diagramas de clases** — todo eso tiene que estar "10 puntos".

Si arrastrás dudas de objetos ("¿qué era una instancia?", "¿cómo modelo esto?"), cuando lleguen los patrones vas a estar en problemas: los patrones **se apoyan** en esa base, no la reemplazan. Si algo de objetos te quedó flojo, este es el momento de aceitarlo, antes de que empiecen los patrones concretos.

---

## 6. Para el parcial, si te preguntan

### 6.1 Los casos tipo (caso → respuesta modelo)

**Caso 9.** *Un patrón de diseño debe usarse…*
> **Únicamente cuando se presenta el problema que resuelve.** No "siempre que se pueda" (eso lleva a sobrediseño y viola KISS), no "lo menos posible" (tampoco es la idea), y no "depende del tipo (creacional/comportamiento/estructural)" — el tipo **clasifica** el patrón, no decide **cuándo** usarlo. *(Fue la pregunta más difícil del repaso: si dudás, volvé al §3.)*

**Caso 10.** *Los patrones de diseño favorecen…*
> **Todas estas:** bajo acoplamiento, mayor extensibilidad, mayor mantenibilidad y reutilización de lógica. Bien aplicados, empujan las cuatro. ⚠️ **La salvedad:** si los **abusás** (metés patrones de más, terminás con 20 clases donde alcanzaban 5), se te da vuelta y el sistema se vuelve **inmantenible**. Como casi todo en diseño, "es un depende".

### 6.2 Resumen de criterio (lo que no puede fallar)

- Un patrón es una **receta conocida** para un **problema conocido**: ventajas, desventajas, **consecuencias** y participantes.
- Se usa **solo si el problema aparece**. Forzarlo = sobrediseño = viola KISS/YAGNI.
- Se evalúa **criterio**, no cantidad: aplicarlo bien **o evitarlo con fundamento** valen igual.
- Lo más importante de cada patrón son las **consecuencias** (te dejan justificar).
- El diagrama de memoria no sirve; entendé **problema + consecuencias**.

---

## 7. Checkpoint (respondé sin mirar arriba)

1. ¿Qué es un patrón de diseño, en una frase? ¿Cuál es su objetivo de fondo?
2. ¿Por qué un patrón es "más que un diagrama de objetos"? ¿Qué diferencia hay entre un diagrama de clases y uno de objetos?
3. Nombrá las 4 partes esenciales de un patrón. ¿Cuál es la más importante para justificar decisiones y por qué?
4. ¿Cuándo se debe usar un patrón de diseño? Refutá la frase "meto tres patrones para demostrar que aprendí".
5. ¿Por qué forzar un patrón puede ser un error? ¿Con qué principios de la Parte 2 se conecta?
6. ¿Puede estar bien resolver un parcial sin usar ningún patrón? Justificá.
7. Nombrá las tres familias de patrones y decí qué tipo de problema ataca cada una.
8. Dame un ejemplo de problema creacional, uno de comportamiento y uno estructural (con tus palabras).
9. ¿Qué favorecen los patrones bien aplicados? ¿Qué pasa si se abusa de ellos?
10. ¿Por qué es innegociable tener sólida la teoría de objetos antes de ver patrones?

*(Las respuestas van al complemento de la unidad, no acá.)*

---

## Cierre de la unidad (preclase 03)

Con esto queda cerrada la unidad. El hilo completo:

- **Parte 1 — Atributos de calidad (ISO 25010):** *qué* querés lograr. Los 8 grupos, la regla de justificación (definí y justificá contra tu definición), y que **no se maximizan todos** (trade-offs).
- **Parte 2 — Cualidades de diseño:** *cómo* lo lográs. **Alta cohesión, bajo acoplamiento** como brújula; KISS y YAGNI; y el error top de objetos (polimorfismo ≠ siempre más flexible).
- **Parte 3 — Patrones de diseño:** *soluciones probadas* para problemas frecuentes, que se usan **solo cuando el problema aparece** — nunca a la fuerza.

Estos tres bloques no se cierran acá: son el **vocabulario con el que vas a diseñar y justificar en cada clase y cada ejercicio del año**. Cuando en una clase te pidan defender una decisión, vas a estar tirando de estas mismas palabras.

**Lo que viene** (ya en la clase, no en la preclase): se baja toda esta teoría a un **caso concreto** de modelado de dominio, trabajando objetos polimórficos con interfaces y/o herencia, y apareciendo el **primer patrón de comportamiento** en acción. Ahí vas a **vivenciar** por primera vez la diferencia entre "meter un patrón" y "resolver el problema".

**FIN DE LA PARTE 3 — Unidad preclase 03 completa**
