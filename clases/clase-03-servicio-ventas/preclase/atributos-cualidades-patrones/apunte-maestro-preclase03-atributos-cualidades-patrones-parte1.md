# Apunte Maestro — Preclase 03 · Parte 1: Atributos de Calidad (ISO 25010)

**Sobre este documento.** Es la primera de tres partes del apunte de la preclase 03. Cubre **atributos de calidad de software**. La Parte 2 cubre **cualidades de diseño** (acoplamiento, cohesión, simplicidad, robustez, flexibilidad) y la Parte 3, **patrones de diseño** (concepto y clasificación).

**De dónde venís.** Se asume la teoría de objetos de Paradigmas (clase, instancia, interfaz, herencia, polimorfismo) — se usa acá sin re-explicarla. No se necesita nada más.

> **Por qué esto importa todo el año.** Los atributos de calidad y las cualidades de diseño no son un tema que se ve una vez y se cierra: son el **vocabulario transversal de la materia**. En cada ejercicio y en cada clase vas a tener que nombrar mantenibilidad, acoplamiento, disponibilidad, performance, etc., y **justificar decisiones de diseño en base a ellos**. Esta parte es la base de todo lo que sigue.

---

## 0. El problema que resuelven (antes de la definición)

Arranquemos con la situación concreta, que es la que le da sentido a todo lo demás.

Tenés que **elegir entre dos diseños** de un sistema —o entre dos formas de resolver una misma funcionalidad— y alguien te pregunta: *"¿por qué elegiste ese camino y no el otro?"*. Si respondés "porque me parece mejor", perdiste: es subjetivo.

Pensalo con una analogía de supermercado. Si te pregunto por qué elegís un yogur y no otro, cada uno va a decir cosas distintas: precio, sabor, marca, la figurita del paquete. Todas subjetivas. Pero si querés comparar **objetivamente** cuál es "mejor", tenés que mirar algo medible: la tabla nutricional, los ingredientes. Recién ahí podés defender una decisión sin que sea "gusto mío".

Con el software pasa lo mismo. **Los atributos de calidad son esa tabla nutricional**: el conjunto de criterios objetivos con los que podés medir qué tan bueno es un diseño y justificar por qué una alternativa es mejor que otra.

> 🕳️ **Madriguera — "mejor" no siempre es lo mejor objetivo.** A veces el diseño objetivamente superior no es el que te conviene, porque no se **adecúa** a lo que necesitás (igual que el yogur más sano puede no gustarte). Eso ya es un atributo puntual —adecuación funcional— y lo vemos abajo. *Volvé al camino.*

### Qué es calidad de software (ISO 25000)

La norma **ISO 25000** —una familia de estándares de calidad **específica para software** (viene a suceder a la vieja ISO 9126)— la define así:

> La calidad de un producto software es el **grado en que ese producto satisface los requisitos de sus usuarios, aportando un valor**. Esos requisitos se representan en un **modelo de calidad**, que categoriza la calidad en **características** y **subcaracterísticas**.

Dos ideas para retener desde ya:

- **La calidad no se agrega al final.** Es aseguramiento *desde el principio* del diseño y el desarrollo. No es "termino el sistema y después le pongo calidad".
- **Producto o servicio, da lo mismo** a estos fines: hablamos de lo que estás construyendo.

### 🔴 La regla de oro (con esto se aprueba el parcial)

Existen **varias clasificaciones** de atributos de calidad, no una sola:

- **ISO** (la familia 25000, que es la que usamos como base),
- **IEEE** (la organización de ingenieros eléctricos y electrónicos, que también tiene bibliografía de calidad),
- **MITRE Corporation** (EE.UU.), con sus propios estándares.

Como las clasificaciones **se mezclan y no coinciden entre sí**, la cátedra **no evalúa definiciones de memoria**. Evalúa que sepas **aplicar** y **justificar**. De ahí sale la regla que rige toda la materia:

> **Cuando justifiques un atributo de calidad, primero DEFINILO vos, y después justificá contra esa definición.**
> No se evalúa si tu definición coincide palabra por palabra con la de la cátedra; se evalúa que tu justificación sea coherente **con la definición que vos diste**.

Y no alcanza con nombrar el atributo: tenés que poder mostrar **dónde se ve, cómo se prueba, cómo se mide, y qué decisión de diseño tomaste para garantizarlo**.

> 📌 **Para el parcial, si te preguntan (molde de una respuesta correcta):**
> *"Priorizo [atributo], que para mí es [definición]. Lo garantizo con [decisión de diseño], y lo puedo medir/ver en [dónde]. A cambio, estoy resignando algo de [otro atributo]."*
> Esa última frase (el trade-off) no es opcional — ver §1.2.

---

## 1. Tres cosas que valen para TODOS los atributos

Antes de ir uno por uno, tres ideas de fondo que atraviesan a todos. No están en la lista de subcaracterísticas, pero son las que más se usan para justificar.

### 1.1 🔴 Tienen que ser cuantificables y medibles (bajo condiciones)

Para que esto sea *ingeniería* y no opinión, un atributo tiene que poder **medirse**, y siempre **bajo condiciones definidas**. No podés decir "el sistema es rápido" a secas.

**Ejemplo aterrizado:**

```
❌ Vago:   "las búsquedas tienen que ser rápidas"

✅ Medible: "el tiempo de búsqueda de un producto debe ser < 100 ms,
            bajo estas condiciones:
              - conectividad del usuario: ≥ 10 Mbps
              - latencia al servidor: ≤ 20 ms
              - cantidad de registros en la base: ≤ 1.000.000"
// Resultado: ahora SÍ podés medirlo y verificar si se cumple o no.
```

Sin las condiciones, la métrica no significa nada: el mismo sistema responde distinto según el escenario.

### 1.2 🔴 Entran en conflicto — no se maximizan todos a la vez (trade-offs)

Este es central. **Nunca vas a poder maximizar todos los atributos de calidad simultáneamente.** Cuando empujás uno, casi siempre hay otro que se te viene abajo.

- Si empujás **eficiencia**, quizás resignás algo de **seguridad**.
- Si empujás **flexibilidad/extensibilidad**, quizás resignás **simplicidad**.

Por eso una decisión de diseño no es "elijo lo mejor de todo", sino "elijo **qué priorizo** y **con qué lo pago**".

> 🕳️ **Madriguera — la mecánica de los trade-offs.** El análisis fino de tensiones entre atributos (y cómo balancearlos a nivel arquitectura) se trabaja de lleno más adelante, en la clase del **caso de estudio de arquitectura (clase 11)**. Por ahora alcanza con internalizar que **el conflicto existe y hay que nombrarlo al justificar**. *Volvé al camino.*

### 1.3 🔴 Estáticos vs. de ejecución (qué se ve en un diagrama de clases y qué no)

Algunos atributos se pueden analizar mirando un **diagrama de clases** (algo estático, en tiempo de diseño). Otros **no**: solo se manifiestan con el sistema **corriendo** (tiempo de ejecución). Esto es un **criterio de justificación** que la cátedra usa mucho: si en un parcial justificás un atributo "en el diagrama de clases" donde no corresponde, está mal.

| Atributo | ¿Se puede evaluar mirando un diagrama de clases? |
|---|---|
| Adecuación funcional | Parcialmente (¿modela lo que el negocio pide?) |
| Eficiencia / performance | **No** — es de ejecución |
| Compatibilidad | Casi no; interoperabilidad, solo si se ve que cumplís un estándar |
| Usabilidad | **No** — necesitás una interfaz gráfica para evaluarla |
| Fiabilidad (madurez) | Algo — manejo de excepciones se ve algo a nivel código |
| Disponibilidad | **No** — es de ejecución |
| Seguridad | Algunas cosas sí (p. ej. modelar auditoría), la mayoría no |
| Mantenibilidad | **Sí** — acoplamiento y cohesión se ven en el diagrama |
| Portabilidad | Solo mirando **tecnologías**, no el diseño de objetos |

**Regla práctica:** usabilidad y disponibilidad **nunca** las justifiques sobre un diagrama de clases. Mantenibilidad (vía acoplamiento/cohesión) es de las pocas que **sí** se ve ahí — por eso es tan central en la primera parte del año.

---

## 2. El modelo ISO 25010: las 8 características

El modelo de calidad del producto de la **ISO 25010** organiza la calidad en **8 características**, y cada una se divide en **subcaracterísticas**:

```
                        ┌─────────────────────────────┐
                        │  Calidad del Producto        │
                        │        Software              │
                        └───────────────┬─────────────┘
   ┌───────────┬───────────┬────────────┼───────────┬───────────┬───────────┬───────────┐
   │           │           │            │           │           │           │           │
┌──▼───────┐ ┌─▼────────┐ ┌▼─────────┐ ┌▼────────┐ ┌▼────────┐ ┌▼───────┐ ┌─▼────────┐ ┌▼────────┐
│Adecuación│ │Eficiencia│ │Compatibi-│ │Usabili- │ │Fiabili- │ │Seguri- │ │Manteni-  │ │Portabi- │
│funcional │ │de desem- │ │lidad     │ │dad      │ │dad      │ │dad     │ │bilidad   │ │lidad    │
│  🟡      │ │peño  🔴  │ │  🟡      │ │  🟡     │ │  🔴     │ │  🔴    │ │   🔴     │ │  🟡     │
└──────────┘ └──────────┘ └──────────┘ └─────────┘ └─────────┘ └────────┘ └──────────┘ └─────────┘
```

**Característica vs. atributo:** la ISO las llama "características", pero en la práctica de la materia vas a escuchar que a varias las tratamos como **atributos de calidad** (seguridad, mantenibilidad, etc.). Es lo mismo — no te enredes con la palabra.

Las marcas (🔴 central · 🟡 secundario) reflejan cuánto pesa cada una **en esta materia**: mantenibilidad, seguridad, fiabilidad y eficiencia se usan y se profundizan durante todo el año; compatibilidad, usabilidad, portabilidad y adecuación funcional aparecen menos como eje de justificación.

---

## 3. Las 8 características, una por una

Para cada una: definición, subcaracterísticas y un ejemplo que la aterrice.

### 3.1 🟡 Adecuación funcional

**Definición.** Capacidad del producto software para **proveer las funciones que satisfacen las necesidades explícitas e implícitas**, cuando se usa bajo condiciones específicas.

Subcaracterísticas:

- **Completitud funcional** — el conjunto de funcionalidades cubre **todas** las tareas y objetivos del usuario especificados.
- **Corrección funcional** — provee resultados **correctos** con el nivel de precisión requerido.
- **Pertinencia funcional** — provee un conjunto **apropiado** de funciones para las tareas/objetivos (ni de más, ni de menos).

**Cómo pensarla.** Es difícil que un software productivo no cumpla adecuación funcional: si no hace para lo que fue hecho, directamente no lo usás. Por eso, del punto de vista técnico, **este atributo casi no se usa para comparar** dos alternativas de diseño — lo damos por dado. Sirve más para el análisis inverso: detectar que un sistema **existente** no se adecúa a lo que el negocio necesita, y decidir reemplazarlo o rehacerlo.

> 🕳️ **Madriguera — "hacer vs. comprar".** Muchas veces no diseñás desde cero: evaluás si una aplicación que **ya existe** se adecúa a tu necesidad. Ahí adecuación funcional es el criterio para decir "esta me sirve / esta no". Se profundiza en Análisis y en clases de arquitectura. *Volvé al camino.*

### 3.2 🔴 Eficiencia de desempeño

**Definición.** Representa el desempeño **relativo a la cantidad de recursos utilizados** bajo determinadas condiciones. (Muchas veces la vas a escuchar como **performance**.)

Subcaracterísticas:

- **Comportamiento temporal** — tiempos de respuesta, de procesamiento y *throughput* del sistema, medidos contra un *benchmark* y bajo condiciones dadas. (Es el "lag" que sufre cualquiera que jugó online.)
- **Utilización de recursos** — cantidades y tipos de recursos que usa: memoria, CPU, GPU, batería, tráfico de red, espacio en disco.
- **Capacidad** — el **límite máximo** que soporta un parámetro (p. ej. "el sistema soporta N usuarios concurrentes con estas características"). Como el "capacidad máxima: 6 personas" de un ascensor.

**Ojo con el recurso: ¿dónde está?** No es lo mismo consumir recursos en el **dispositivo del cliente** (una app móvil) que en el **servidor** (una app web). Una decisión de diseño típica para mejorar eficiencia es **mover procesamiento** del dispositivo del usuario al servidor.

### 3.3 🟡 Compatibilidad

**Definición.** Capacidad de **dos o más sistemas o componentes** para **intercambiar información y/o llevar a cabo sus funciones** cuando **comparten el mismo entorno** hardware o software.

Subcaracterísticas:

- **Coexistencia** — el producto puede coexistir con **otro software independiente** en un entorno común, compartiendo recursos **sin detrimento**.
  - *Ejemplo:* no podés tener instaladas al mismo tiempo, en el mismo sistema operativo y sin virtualizar, Office de 32 bits y Office de 64 bits. No coexisten.
- **Interoperabilidad** — dos o más sistemas o componentes **intercambian información y la utilizan**.

**⚠️ Interoperabilidad ≠ integración (concepto fino que se evalúa).** Integrar dos sistemas es que puedan comunicarse (p. ej. vía una API REST con un protocolo común). Interoperabilidad va **un paso más arriba**: los sistemas no solo se comunican, sino que **modelan las mismas entidades de la misma manera**, siguiendo un **estándar**, sin necesidad de una capa que traduzca.

> **Ejemplo:** el estándar médico **HL7 FHIR** define cómo se modela un "paciente" (sus campos, su estructura). Si dos sistemas de salud modelan al paciente según FHIR, uno le puede pasar un paciente al otro **sin transformarlo**: son interoperables. Si cada uno lo modela a su manera, hay integración pero no interoperabilidad — necesitás una capa de traducción en el medio.

En resumen: dos sistemas son interoperables si **se entienden** y, además, **lo que hablan es un estándar** (no un acuerdo privado entre ellos).

### 3.4 🟡 Usabilidad

**Definición.** Capacidad del producto software de ser **entendido, aprendido, usado y resultar atractivo** al usuario, bajo condiciones especificadas.

Subcaracterísticas *(entre paréntesis, el nombre alternativo que también vas a encontrar, porque circulan dos traducciones de estos nombres)*:

- **Adecuación** *(Inteligibilidad)* — permite al usuario entender si el software es adecuado para sus necesidades.
- **Capacidad de aprendizaje** *(Aprendizaje)* — permite aprender a usar la aplicación (idealmente sin manual: nadie leyó el manual de Instagram o Netflix).
- **Capacidad para ser usado** *(Operabilidad)* — permite operarlo y controlarlo con facilidad. (Distinto de aprenderlo: una cosa es aprender a usarlo, otra que sea fácil de usar.)
- **Protección contra errores de usuario** — el sistema protege al usuario de cometer errores (validaciones, restringir tipos de dato, guiar el ingreso).
- **Estética de la interfaz** — la interfaz agrada y satisface la interacción.
- **Accesibilidad** — puede ser usado por usuarios **con distintas capacidades y discapacidades** (p. ej. personas no videntes: lectura de descripciones de imágenes; daltonismo: elección de colores).

> ⚠️ **Cuánto se te va a exigir de usabilidad (calibración).** La materia **no** se enfoca en diseño de interfaz de usuario (hay una electiva específica de Usabilidad y Experiencia de Usuario). De usabilidad se espera que **la conozcas y la nombres**, no que la analices en profundidad. Para el examen: entendé el concepto y sus subcaracterísticas; no esperes tener que diseñar una UI accesible.

> 🕳️ **Madriguera — la accesibilidad es ley.** En Argentina la accesibilidad web de sitios del Estado está regulada (**Ley 26.653**): no es opcional para plataformas estatales. Dato de contexto, no entra como tema. *Volvé al camino.*

### 3.5 🔴 Fiabilidad

**Definición.** Capacidad de un sistema o componente para **desempeñar sus funciones especificadas**, bajo condiciones y periodo de tiempo determinados. (Tiene que ver con la **confianza**: que no te falle.)

Subcaracterísticas:

- **Madurez** — capacidad del software para **evitar fallas producto de errores en el propio software**. Un software maduro "ya conoce" los errores que le pueden tirar y no se rompe con ellos. *(Visión moderna: no se trata tanto de evitar TODA falla, sino de **controlarla y monitorearla** — atraparla con manejo de excepciones, loguearla, no dejar que "explote" y llegue un error crudo al cliente.)*
- **Disponibilidad** — capacidad de estar **operativo y accesible cuando se lo requiere**.
- **Tolerancia a fallos** — mantener un nivel de funcionamiento aun **ante errores** del software o incumplimientos de su interfaz.
- **Recuperabilidad** — **reestablecer** el nivel de funcionamiento y **recuperar los datos** afectados tras una falla.
  - *Ejemplo:* el autoguardado de un editor de texto. Si el programa se cae, al reabrir volvés a un **estado consistente** y no perdiste todo.

> ⚠️ **Robustez ≠ Madurez (cruce que confunde y se pregunta).** Si en un caso ves errores del tipo "el servidor se rompe (HTTP 500) ante un input inválido", el atributo ISO que falla es la **Madurez** (subcaracterística de Fiabilidad). Cuidado: **"Robustez" NO es un atributo de calidad de la ISO** — es una **cualidad de diseño** (la vemos en la Parte 2). Van muy de la mano —robustez = resistencia/tolerancia a fallos; madurez = qué tan probado y estable está algo—, casi sinónimos en la práctica. Pero si te piden nombrar el **atributo de calidad**, el término ISO correcto es **Madurez** (o Tolerancia a fallos), no Robustez.

> 🕳️ **Madriguera — disponibilidad medida (SLA y "nueves").** La disponibilidad se mide como un **porcentaje anual** y se pacta en un **SLA** (acuerdo de nivel de servicio): 99,9 % permite ~526 min de caída al año; 99,99 %, ~52,6 min; y así. Además, no todo sistema necesita 24/7: muchas veces importa un **momento crítico** (que no se caiga durante el pico de venta de entradas, aunque se caiga a las 3 AM). Todo esto se profundiza en la **clase de disponibilidad (clase 13)**. Por ahora: disponibilidad = estar arriba cuando se lo necesita, y se **mide**. *Volvé al camino.*

### 3.6 🔴 Seguridad

**Definición.** Capacidad de **proteger la información y los datos** de manera que personas o sistemas **no autorizados** no puedan **leerlos ni modificarlos**.

Subcaracterísticas:

- **Confidencialidad** — protección contra el **acceso** a datos no autorizado (accidental o deliberado). Encriptar para que los datos viajen seguros vive acá. *(Se conecta con la Ley de Datos Personales.)*
- **Integridad** — prevenir **modificaciones no autorizadas** a datos o programas. Que el dato llegue **íntegro**, sin ser alterado. *(Se conecta con consistencia.)*
- **No repudio** — poder **demostrar** que una acción ocurrió, de modo que quien la hizo **no pueda negarla después**. *(Se implementa, por ejemplo, con firma digital: firmás con tu clave privada; si el otro puede verificarla con tu clave pública, solo pudiste haberla generado vos.)*
- **Responsabilidad** *(rendición de cuentas)* — **rastrear inequívocamente** las acciones de una entidad (base de la auditoría).
- **Autenticidad** — **demostrar la identidad** de un sujeto o recurso.

> ⚠️ **La tríada de la seguridad — CIA (cruce importante).** La definición general de seguridad se apoya en **tres pilares: Confidencialidad, Integridad y Disponibilidad**. Fijate que ahí la **Disponibilidad** aparece **dentro de la seguridad**, aunque en la ISO 25010 la disponibilidad es subcaracterística de **Fiabilidad**. **No está mal: es el cruce de clasificaciones del que hablamos en §0.** En un caso donde se compromete la seguridad, muchas veces también se cae la disponibilidad — y ambos son válidos. Regla práctica: **cuando "rompés seguridad", suele romperse disponibilidad también.**

> 🕳️ **Madriguera — seguridad en serio.** Autenticación vs. autorización, modelos de control de acceso (RBAC, ABAC), SSO, WAF, y la tríada CIA en detalle se dan en la **clase de Seguridad de la Información (clase 14)**. Acá alcanza con las subcaracterísticas y el cruce CIA. *Volvé al camino.*

### 3.7 🔴 Mantenibilidad

**Definición.** Capacidad del producto software para ser **modificado efectiva y eficientemente**, debido a necesidades **evolutivas, correctivas o perfectivas**.

Las tres necesidades, porque las vas a nombrar seguido:

- **Evolutiva** — agregar algo nuevo, hacer crecer el sistema (esto es **extensibilidad**).
- **Correctiva** — arreglar algo que está mal.
- **Perfectiva** — mejorar algo que ya funciona.

Subcaracterísticas:

- **Modularidad** — que un cambio en un componente tenga un **impacto mínimo** en los demás.
- **Reusabilidad** — que un activo pueda usarse en **más de un sistema** o en la construcción de otros.
- **Analizabilidad** — facilidad para **evaluar el impacto** de un cambio, diagnosticar la causa de un fallo, o identificar las partes a modificar.
- **Capacidad para ser modificado** *(modificabilidad)* — modificarlo sin introducir defectos ni degradar el desempeño.
- **Capacidad para ser probado** *(testeabilidad)* — facilidad para establecer criterios de prueba y ejecutarlos. *(Un sistema difícil de testear deja pasar más bugs a producción.)*

> 🔴 **Por qué es la más importante de la primera parte del año.** La mantenibilidad es la puerta de entrada a las **cualidades de diseño** (Parte 2): **modularidad** se apoya en **bajo acoplamiento** y **alta cohesión**, que son las herramientas concretas con las que vas a diseñar y justificar durante todo el primer cuatrimestre. Y es de los **pocos atributos que sí se ven en un diagrama de clases** (§1.3). Tenela bien fresca — es el puente hacia la Parte 2.

### 3.8 🟡 Portabilidad

**Definición.** Capacidad del producto o componente de ser **transferido de forma efectiva y eficiente de un entorno** (hardware, software, operacional o de uso) **a otro**.

Subcaracterísticas:

- **Adaptabilidad** — adaptarse a distintos entornos sin acciones ni medios distintos de los previstos.
- **Capacidad para ser instalado** — facilidad para instalar/desinstalar exitosamente en un entorno.
- **Capacidad para ser reemplazado** — poder usarse **en lugar de otro** producto con el mismo propósito y entorno (p. ej. cambiar una base de datos por otra que cumple la misma función).

**Ejemplo.** Java es portable: el mismo programa corre en Windows o Linux, porque corre sobre la máquina virtual (JVM) y no directamente sobre el sistema operativo. Conceptos actuales que se relacionan: **contenedores** y las apps móviles **híbridas** (un solo código para iOS y Android).

> ⚠️ **Portabilidad ≠ Compatibilidad (se confunden).** **Compatibilidad** = dos componentes conviven/interactúan **en el mismo entorno** (coexistencia, interoperabilidad). **Portabilidad** = **un** componente se **mueve** a otra plataforma/hardware y sigue funcionando. Ejemplo que las separa: tu número de celular es **portable** (te lo llevás a cualquier compañía) y **compatible** (funciona con todas las apps del teléfono). Además, portabilidad y compatibilidad son más una **decisión previa** que un atributo "complejo de resolver" como disponibilidad, performance o seguridad — por eso pesan menos como eje de justificación.

---

## 4. Para el parcial, si te preguntan

### 4.1 La regla de justificación (repaso del §0)

Definí el atributo con **tus** palabras y justificá contra esa definición; mostrá **dónde se ve, cómo se mide, y qué decisión de diseño lo garantiza**; y nombrá el **trade-off** (qué resignás). No se toma teoría de memoria: se toma criterio aplicado.

### 4.2 Los cuatro cruces que confunden

| Si el caso te lleva a… | El término correcto es… | Por qué |
|---|---|---|
| "Robustez" como atributo de calidad | **Madurez** (o Tolerancia a fallos) | Robustez es cualidad de diseño, no atributo ISO. Van de la mano, pero el atributo es Madurez. |
| Que "rompo seguridad, pero también se cae el sistema" | **Seguridad + Disponibilidad** | Tríada CIA: la disponibilidad es pilar de seguridad, aunque en la ISO cuelgue de Fiabilidad. |
| "Los dos sistemas se comunican" | **Integración**, y si además comparten estándar de modelado, **Interoperabilidad** | Interoperabilidad es un paso arriba de integración. |
| "El sistema se mueve a otra plataforma" | **Portabilidad**, no Compatibilidad | Compatibilidad es convivir en el mismo entorno; portabilidad es moverse. |

### 4.3 Casos tipo (formato de examen: caso → justificación)

**Caso 1.** *Los usuarios experimentan errores HTTP 500 de forma recurrente al enviar formularios con datos vacíos o inválidos. ¿Qué atributo de calidad no se cumple?*

> **Madurez** (Fiabilidad). Un 500 es un error del servidor: una **excepción no manejada**, el sistema "se rompe". Un software maduro atrapa esos errores de input y, en vez de reventar, devuelve un aviso controlado ("no podés ingresar números acá"). ⚠️ Cuidado: no es **Robustez** (esa es cualidad de diseño); el atributo ISO es **Madurez**. No es Seguridad ni Compatibilidad (el caso no va por ahí). Disponibilidad tienta —"me da 500, no puedo usarlo"— pero el sistema **sigue disponible**: responde, solo que se rompe ante ese input. Por eso decanta en Madurez.

**Caso 2.** *Un colega sube por accidente a GitHub el archivo `.properties` con las credenciales productivas de la base de datos. Poco después, la base queda inaccesible y su contenido es alterado. ¿Qué atributos se ven afectados?*

> **Seguridad + Disponibilidad.** Seguridad: al filtrarse credenciales reales, un tercero puede acceder y **alterar** los datos → se compromete **integridad** (y confidencialidad). Disponibilidad: la base queda **inaccesible** → no se puede disponer de ella. Y por la tríada CIA, romper seguridad arrastra disponibilidad. (Regla de higiene relacionada: a Git nunca subas credenciales reales, solo credenciales de ejemplo; las reales se manejan por afuera.)

**Caso 3.** *Una API REST expone `GET /users`. Con pocos registros responde rápido; a medida que la base crece, devuelve miles de registros en una sola respuesta porque no implementa paginación. ¿Qué atributos se ven afectados?*

> **Tiempo de respuesta / latencia** y **Utilización de recursos** (traés muchos más datos de los necesarios, la base y la red trabajan de más, y la respuesta tarda). **Experiencia de usuario** también, aunque es más discutible: el usuario final no ve el JSON, ve la interfaz — le pega porque tarda en cargar. ⚠️ Ojo fino: la **latencia** puede darse entre dos componentes internos, en una capa **sin usuario**; ahí hay problema de latencia pero **no** hablás de experiencia de usuario. **Portabilidad queda afuera**: nada tiene que ver con mover el sistema de plataforma.

**Caso 4.** *Ventajas de implementar una API REST sobre HTTPS.*

> **Confidencialidad** (los datos viajan **encriptados** → seguros) e **Integridad** (llegan sin ser modificados). **Interoperabilidad** también, aunque **parcialmente**: trabajar sobre un protocolo estándar favorece la integración entre componentes, pero HTTPS por sí solo no te da la definición completa de sistemas interoperables. **Accesibilidad queda afuera**: eso es diversidad de usuarios/discapacidades y requiere una interfaz gráfica para evaluarse; acá hablamos de una API.

---

## 5. Checkpoint (respondé sin mirar arriba)

1. ¿Para qué sirven los atributos de calidad? ¿Qué problema concreto resuelven al momento de tomar una decisión de diseño?
2. Enunciá la regla de oro de justificación de la cátedra. ¿Por qué existe (qué tiene que ver con que haya varias clasificaciones)?
3. ¿Qué significa que un atributo tenga que ser "medible bajo condiciones"? Dá un ejemplo de una métrica mal planteada y su versión medible.
4. ¿Por qué no se pueden maximizar todos los atributos a la vez? Dá un par de atributos que tiendan a estar en conflicto.
5. Nombrá tres atributos que **no** se puedan evaluar sobre un diagrama de clases y uno que **sí**.
6. ¿Cuál es la diferencia entre **integración** e **interoperabilidad**?
7. Un caso presenta errores 500 recurrentes por inputs inválidos: ¿qué atributo ISO falla y por qué **no** conviene responder "Robustez"?
8. ¿Cuáles son los tres pilares de la tríada CIA? ¿Por qué "rompés seguridad" suele implicar también un problema de disponibilidad, si en la ISO la disponibilidad cuelga de Fiabilidad?
9. Diferenciá **portabilidad** de **compatibilidad** con un ejemplo propio.
10. ¿Por qué la **mantenibilidad** es el atributo más importante de la primera parte del año? ¿Con qué cualidades de diseño se conecta?

*(Las respuestas van al complemento de la unidad, no acá.)*

---

## Qué viene en la Parte 2

**Cualidades de diseño**: acoplamiento, cohesión, simplicidad (KISS y YAGNI), robustez y flexibilidad (extensibilidad y mantenibilidad). Son los criterios y herramientas concretas con las que vas a **diseñar y justificar** durante todo el primer cuatrimestre — y donde "Robustez" (que acá dejamos afuera de los atributos ISO) encuentra su lugar.

**FIN DE LA PARTE 1**
