# Complemento — Preclase 03: Atributos, Cualidades y Patrones

*Cierra la unidad: aclaraciones que la sesión dejó (Parte A) y las respuestas de los tres checkpoints del apunte maestro en formato examen (Parte B).*

---

## Parte A — Aclaraciones destiladas

Dos puntos finos que conviene tener resueltos, más allá de lo que el maestro ya desarrolla.

### A.1 — Por qué la interoperabilidad es solo una ventaja *parcial* de una API sobre HTTPS

De HTTPS obtenés **confidencialidad** e **integridad** de lleno (los datos viajan encriptados y sin ser alterados). Su aporte a la **interoperabilidad**, en cambio, es indirecto y débil, por dos motivos:

1. Interoperar de verdad implica que dos sistemas **modelen las mismas entidades bajo un estándar compartido** (no solo que se comuniquen). HTTPS solo aporta un **protocolo estándar de transporte**: favorece la integración, pero no garantiza esa definición plena.
2. La interoperabilidad **no depende puntualmente de HTTPS**: podrías exponer la misma API REST sobre otro protocolo y dos sistemas seguirían intercambiando información igual.

> **Para el parcial, si te preguntan:** confidencialidad e integridad son ventajas **directas** de HTTPS; interoperabilidad, a lo sumo **parcial** — justificala como "cierto nivel de interoperabilidad", no como la definición completa.

### A.2 — Cómo se relacionan los atributos de calidad y las cualidades de diseño

Es normal marearse porque hay conceptos que aparecen "de los dos lados". La clave: son **dos lentes distintas sobre las mismas preocupaciones**.

- Los **atributos de calidad** (Parte 1) describen la calidad del **producto** — lo que el software exhibe hacia afuera (es mantenible, es seguro, es disponible).
- Las **cualidades de diseño** (Parte 2) son las **herramientas internas** del diseño con las que se consiguen varios de esos atributos (acoplamiento, cohesión, simplicidad).

Por eso los cruces no son contradicción ni redundancia:

| Aparece como atributo… | …y como cualidad de diseño | Es… |
|---|---|---|
| Madurez / Tolerancia a fallos (Fiabilidad) | **Robustez** | el mismo "no romperse ante fallas", visto como propiedad del producto o como criterio de diseño |
| Mantenibilidad → subcaracterística **Modularidad** | **bajo acoplamiento + alta cohesión** | la modularidad se *consigue* con esas dos cualidades |
| Mantenibilidad (evolutiva/correctiva/perfectiva) | **Flexibilidad** (Extensibilidad + Mantenibilidad) | la misma idea reordenada |

> **Para el parcial, si te preguntan:** usá el término que te pidan. Si preguntan por el **atributo de calidad**, decí Madurez; si estás **justificando una decisión de diseño**, hablá de Robustez / acoplamiento / cohesión.

---

## Parte B — Respuestas del checkpoint

### Parte 1 — Atributos de calidad

**1.** Sirven como criterios objetivos para comparar alternativas de diseño y justificar decisiones sin caer en lo subjetivo. Resuelven el problema de "¿por qué este diseño y no el otro?": aportan una vara medible en lugar de un "me parece mejor".

**2.** Definí vos el atributo con tus palabras y justificá contra esa definición, mostrando dónde se ve, cómo se mide y qué decisión de diseño lo garantiza. Existe porque hay varias clasificaciones (ISO, IEEE, MITRE) que se mezclan y no coinciden, así que no se evalúa la definición de memoria sino la coherencia de tu justificación con la definición que diste.

**3.** Significa que un atributo solo es útil si podés medirlo con una métrica y bajo condiciones explícitas, porque el mismo sistema rinde distinto según el escenario. Mal planteado: "las búsquedas tienen que ser rápidas"; medible: "< 100 ms con conectividad ≥ 10 Mbps, latencia ≤ 20 ms y ≤ 1.000.000 de registros".

**4.** Porque los atributos entran en conflicto: al empujar uno casi siempre se resigna otro, así que diseñar es elegir qué priorizar y con qué pagarlo. Ejemplos de pares en tensión: eficiencia vs. seguridad, o extensibilidad vs. simplicidad.

**5.** No se ven en un diagrama de clases (son de ejecución): eficiencia/performance, usabilidad y disponibilidad. Sí se ve: la mantenibilidad, a través del acoplamiento y la cohesión.

**6.** Integración es que dos sistemas puedan comunicarse (p. ej. vía una API con un protocolo común). Interoperabilidad es un paso más arriba: además de comunicarse, modelan las mismas entidades bajo un mismo estándar (p. ej. HL7 FHIR), sin necesidad de una capa que traduzca.

**7.** Falla la Madurez (subcaracterística de Fiabilidad): un HTTP 500 es una excepción no manejada, y un software maduro atrapa esos errores en vez de romperse. No conviene responder "Robustez" porque Robustez es una cualidad de diseño, no un atributo de calidad ISO; van de la mano, pero el término del atributo es Madurez.

**8.** La tríada CIA es Confidencialidad, Integridad y Disponibilidad. En la definición general de seguridad la disponibilidad es uno de sus pilares, así que comprometer la seguridad suele tumbar también la disponibilidad; que en la ISO 25010 la disponibilidad figure bajo Fiabilidad es solo el cruce entre clasificaciones, no una contradicción.

**9.** Portabilidad es que un componente se mueva a otra plataforma o entorno y siga funcionando; compatibilidad es que dos componentes convivan o interactúen en el mismo entorno. Ejemplo: un número de celular es portable (te lo llevás a otra compañía) y compatible (funciona con todas las apps del teléfono).

**10.** Porque es de los pocos atributos que se ven en el diagrama de clases y es la puerta de entrada a las cualidades de diseño con las que se justifica todo el primer cuatrimestre. Conecta con acoplamiento y cohesión: su subcaracterística de modularidad equivale a bajo acoplamiento más alta cohesión.

### Parte 2 — Cualidades de diseño

**1.** Son los criterios con los que comparás alternativas de diseño y tomás una decisión más formada. No son el único criterio: la experiencia y el conocimiento de quien diseña también pesan.

**2.** Acoplamiento es el grado de dependencia o conocimiento que un componente tiene sobre otro. El alto acoplamiento es malo porque los cambios o errores de uno se propagan al otro. Al minimizarlo ganás mantenibilidad, reutilización, analizabilidad y testeabilidad.

**3.** El acoplamiento nulo solo existe entre componentes que nunca se relacionan; si dos trabajan juntos siempre hay un mínimo, y se busca el mínimo necesario. La regla de "no clases sueltas" dice que en un diagrama toda clase debe relacionarse con al menos otra, porque una clase totalmente suelta sería un sistema aparte.

**4.** Cohesión es la cantidad de responsabilidades asignadas a un componente: cuantas más tiene, menos cohesivo es. Una god class tiene baja cohesión porque acumula responsabilidades de todo tipo y termina "haciendo de todo".

**5.** El lema es "alta cohesión, bajo acoplamiento". Son ejes independientes: una clase puede ser muy cohesiva (hace una sola cosa, y bien) y a la vez tener alto acoplamiento, si para modificarla estás obligado a tocar la otra clase de la que depende.

**6.** KISS pide evitar cualquier complejidad innecesaria; YAGNI pide no agregar funcionalidad que no apunte al problema actual. Diseñar para la extensibilidad (dejar la puerta abierta a cambios futuros) es válido; violar YAGNI es distinto: es construir hoy esa funcionalidad futura que nadie pidió.

**7.** Ante un uso inadecuado o una falla, un diseño robusto no genera comportamiento errático, reporta el error y vuelve a un estado consistente, y facilita detectar la causa. Es el mismo fenómeno que el atributo Madurez de la Parte 1, visto ahora como cualidad de diseño.

**8.** Flexibilidad se abre en extensibilidad (agregar características con poco impacto) y mantenibilidad (modificar las existentes con el menor esfuerzo). El cambio evolutivo corresponde a extensibilidad; el correctivo y el perfectivo, a mantenibilidad.

**9.** Es falso porque el polimorfismo flexibiliza cuando hay variación de comportamiento, pero con variación de datos (mismo comportamiento, distintos valores) lo vuelve menos flexible. Ejemplo: modelar cada producto como una clase (ProductoLeche, ProductoPan) obliga a tocar y recompilar código para agregar uno; una sola clase Producto con instancias permite agregarlos en tiempo de ejecución.

**10.** Con la extensibilidad puede haber tensión: a veces dejar algo extensible agrega estructura que resta simplicidad (y hecho de más, viola YAGNI y KISS). Con la mantenibilidad no hay conflicto: simplicidad y mantenibilidad van de la mano.

### Parte 3 — Patrones de diseño

**1.** Un patrón de diseño es una solución conocida a un problema conocido y frecuente del desarrollo de software. Su objetivo es reutilizar la experiencia de quienes ya resolvieron bien ese problema, en vez de reinventar la rueda.

**2.** Es más que un diagrama de objetos porque incluye problema, solución, consecuencias, participantes y colaboraciones, no solo un dibujo. Un diagrama de clases muestra la relación estática entre clases; uno de objetos muestra instancias en un momento dado (una "foto").

**3.** Las cuatro partes esenciales son Nombre, Problema, Solución y Consecuencias. La más importante para justificar son las consecuencias, porque son las que te permiten defender por qué aplicaste —o evitaste— el patrón al comparar alternativas.

**4.** Un patrón se usa únicamente cuando se presenta el problema que resuelve. Meter tres patrones "para demostrar que aprendí" está mal: no se evalúa la cantidad sino el criterio, aplicarlo bien o evitarlo con fundamento.

**5.** Forzar un patrón donde no hace falta genera sobrediseño: le agrega complejidad innecesaria a algo que estaba simple. Conecta directamente con los principios KISS y YAGNI.

**6.** Sí, puede estar perfecto: muchas veces la solución más simple es la correcta y no lleva ningún patrón. Lo que se valora es haber sabido evaluar que no hacía falta.

**7.** Creacionales (problemas de creación/instanciación y configuración de objetos), de comportamiento (interacción entre objetos en tiempo de ejecución) y estructurales (armado o uso de estructuras, o acoplarse a componentes de terceros sin tocarlos).

**8.** Creacional: tengo lógica compleja para instanciar y configurar un objeto según lo que elige el usuario en ejecución. Comportamiento: necesito que un objeto se comporte distinto según el estado en que está. Estructural: quiero usar cinco clases de otra persona sin tocarlas y sin depender de más de ellas.

**9.** Bien aplicados favorecen bajo acoplamiento, mayor extensibilidad, mayor mantenibilidad y reutilización de lógica. Si se abusa de ellos, el sistema se vuelve inmantenible: la ventaja se da vuelta.

**10.** Porque los patrones se apoyan en la teoría de objetos (instancia, clase, interfaz, herencia, polimorfismo, diagramas de clases) y no la reemplazan. Si esa base está floja, los patrones se entienden mal.

---

**FIN DEL COMPLEMENTO — Unidad preclase 03**
