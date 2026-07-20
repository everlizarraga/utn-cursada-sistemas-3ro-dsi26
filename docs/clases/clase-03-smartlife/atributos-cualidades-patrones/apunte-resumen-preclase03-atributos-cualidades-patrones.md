# Apunte Resumen — Preclase 03: Atributos de Calidad, Cualidades y Patrones de Diseño

*Destilación del apunte maestro (3 partes) para repaso espaciado. Cobertura total, sin omisiones: se condensa dentro de cada punto, no se eliminan puntos.*

**Dos sistemas de marcas por sección:**
- **Importancia (del maestro):** 🔴 central-evaluable · 🟡 secundario · 🟢 al pasar.
- **Uso:** 🎯 esencial para aplicar · 📘 contexto para el parcial. *(Leyendo solo los 🎯, este resumen funciona como machete improvisado.)*

---

# PARTE 1 — Atributos de Calidad (ISO 25010)

## 0. Para qué sirven · 🔴 🎯
Son los **criterios objetivos** para comparar alternativas de diseño y **justificar** decisiones (la "tabla nutricional" del software: sacan la elección del "me parece"). **Calidad (ISO 25000):** grado en que el producto satisface a sus usuarios aportando valor; se modela en **características** y **subcaracterísticas**. No se agrega al final: se asegura desde el diseño.

**🔴 Regla de oro (con esto se aprueba):** existen varias clasificaciones (ISO 25000 / IEEE / MITRE) que se mezclan → **no se toma de memoria**. Definís vos el atributo y justificás **contra tu propia definición**; y mostrás **dónde se ve, cómo se mide y qué decisión de diseño lo garantiza**.

> 📌 **Para el parcial, si te preguntan (molde de una respuesta correcta):**
> *"Priorizo [atributo], que para mí es [definición]. Lo garantizo con [decisión de diseño], y lo puedo medir/ver en [dónde]. A cambio, estoy resignando algo de [otro atributo]."*
> Esa última frase (el trade-off) no es opcional.

## 1. Vale para TODOS los atributos · 🔴 🎯
- **Cuantificables/medibles bajo condiciones** — sin condiciones, la métrica no dice nada. *(ej.: búsqueda < 100 ms con conectividad ≥ 10 Mbps, latencia ≤ 20 ms, ≤ 1.000.000 registros.)*
- **Entran en conflicto → no se maximizan todos** (trade-offs): elegís qué priorizás y con qué lo pagás. *(mecánica fina → clase 11.)*
- **Estáticos vs. de ejecución** — criterio de justificación clave: qué se ve en un diagrama de clases y qué no.

| Se ve en un diagrama de clases | No se ve (es de ejecución) |
|---|---|
| Mantenibilidad (acoplamiento/cohesión), algo de Madurez (código), Seguridad parcial (auditoría) | **Eficiencia/performance, Usabilidad, Disponibilidad** |

**Regla práctica:** usabilidad y disponibilidad **nunca** se justifican sobre un diagrama de clases; mantenibilidad **sí**.

## 2. El modelo ISO 25010 · 📘
Ocho características (la palabra "característica" y "atributo" se usan indistintamente):
```
Adecuación funcional 🟡 · Eficiencia de desempeño 🔴 · Compatibilidad 🟡 · Usabilidad 🟡
Fiabilidad 🔴 · Seguridad 🔴 · Mantenibilidad 🔴 · Portabilidad 🟡
```

## 3. Las 8 características

**3.1 Adecuación funcional · 🟡 📘** — proveer las funciones que satisfacen necesidades explícitas e implícitas. Subs: Completitud, Corrección, Pertinencia. Casi no se usa para comparar diseños (un software productivo la cumple); sirve para decidir reemplazar/rehacer un sistema.

**3.2 Eficiencia de desempeño · 🔴 🎯** — desempeño relativo a los recursos usados. Subs: Comportamiento temporal (performance, lag/latencia), Utilización de recursos, Capacidad (límite máx.). Ojo **dónde** está el recurso: cliente vs. servidor (mover procesamiento al servidor es decisión típica).

**3.3 Compatibilidad · 🟡 📘** — dos+ componentes conviven/intercambian en el **mismo entorno**. Subs: Coexistencia (ej. Office 32 y 64 bits no coexisten), Interoperabilidad. ⚠️ **Interoperabilidad ≠ integración**: interop = además de comunicarse, **modelan las mismas entidades bajo un estándar** (ej. HL7 FHIR), sin capa traductora.

**3.4 Usabilidad · 🟡 📘** — ser entendido, aprendido, usado y atractivo. Subs: Adecuación/Inteligibilidad, Aprendizaje, Operabilidad, Protección ante errores de usuario, Estética, Accesibilidad. ⚠️ La materia **no** evalúa diseño de UI; nivel esperado = **conocimiento**, no aplicación.

**3.5 Fiabilidad · 🔴 🎯** — desempeñar funciones bajo condiciones/tiempo dados (confianza). Subs: **Madurez** (evitar fallas por errores del propio software; visión moderna: controlarlas/monitorearlas), **Disponibilidad** (operativo cuando se lo requiere), Tolerancia a fallos, Recuperabilidad (ej. autosave → estado consistente). ⚠️ **Robustez ≠ Madurez**: "Robustez" es cualidad de diseño; el atributo ISO es Madurez. *(SLA / "nueves" → clase 13.)*

**3.6 Seguridad · 🔴 🎯** — proteger info/datos de acceso o modificación no autorizados. Subs: Confidencialidad (encriptar), Integridad (no alterado), No repudio (firma digital: clave privada/pública), Responsabilidad (auditoría), Autenticidad. ⚠️ **Tríada CIA** (Confidencialidad, Integridad, **Disponibilidad**): la disponibilidad es pilar de seguridad aunque en la ISO cuelgue de Fiabilidad → **romper seguridad suele romper disponibilidad**. *(seguridad formal → clase 14.)*

**3.7 Mantenibilidad · 🔴 🎯** — modificar efectiva/eficientemente por necesidad **evolutiva** (extensibilidad), **correctiva** o **perfectiva**. Subs: Modularidad, Reusabilidad, Analizabilidad, Modificabilidad, Testeabilidad. **Puente clave:** modularidad = **bajo acoplamiento + alta cohesión** (Parte 2). Es de los pocos atributos que se ven en el diagrama de clases → el más central del arranque del año.

**3.8 Portabilidad · 🟡 📘** — transferirse a otro entorno (hardware/software/plataforma). Subs: Adaptabilidad, Instalable, Reemplazable. Ej.: Java sobre la JVM. ⚠️ **≠ Compatibilidad**: portabilidad = **moverse** a otra plataforma; compatibilidad = **convivir** en el mismo entorno.

## 4. Para el parcial

**4.1 Regla de justificación** — definí con tus palabras, justificá contra esa definición, mostrá dónde/cómo se mide y qué decisión lo garantiza, y nombrá el trade-off. No se toma teoría de memoria: criterio aplicado.

**4.2 Los cuatro cruces que confunden:**

| Si el caso te lleva a… | El término correcto es… | Por qué |
|---|---|---|
| "Robustez" como atributo | **Madurez** (o Tolerancia a fallos) | Robustez es cualidad de diseño, no atributo ISO. |
| "Rompo seguridad y se cae el sistema" | **Seguridad + Disponibilidad** | Tríada CIA. |
| "Los dos sistemas se comunican" | **Integración**; con estándar compartido, **Interoperabilidad** | Interop es un paso arriba. |
| "El sistema se mueve de plataforma" | **Portabilidad**, no Compatibilidad | Convivir ≠ moverse. |

**4.3 Casos tipo (caso → justificación):**

- **Caso 1.** *500 recurrentes al enviar formularios con datos inválidos.* → **Madurez** (Fiabilidad). Un 500 es una excepción no manejada. ⚠️ No es Robustez (esa es cualidad de diseño). No es Seguridad ni Compatibilidad. Disponibilidad tienta, pero el sistema sigue respondiendo → decanta en Madurez.
- **Caso 2.** *`.properties` con credenciales productivas subido a GitHub; la base queda inaccesible y alterada.* → **Seguridad + Disponibilidad.** Integridad/confidencialidad comprometidas + base inaccesible; por CIA, romper seguridad arrastra disponibilidad.
- **Caso 3.** *REST `GET /users` sin paginación devuelve miles de registros.* → **Tiempo de respuesta/latencia + Utilización de recursos** (y **Experiencia de usuario**, discutible: la latencia puede darse en capas sin usuario). Portabilidad queda afuera.
- **Caso 4.** *Ventajas de REST sobre HTTPS.* → **Confidencialidad + Integridad + Interoperabilidad** (esta última, parcial). Accesibilidad queda afuera (requiere UI).

## 5. Checkpoint Parte 1
Para qué sirven los atributos · regla de justificación y por qué existe · "medible bajo condiciones" con ejemplo · por qué no se maximizan todos · tres atributos que no se ven en diagrama de clases y uno que sí · integración vs. interoperabilidad · 500 recurrentes: ¿por qué Madurez y no Robustez? · tríada CIA y el cruce con Fiabilidad · portabilidad vs. compatibilidad · por qué mantenibilidad es la más central.

---

# PARTE 2 — Cualidades de Diseño

## 0. Qué son · 🟡 📘
Criterios para comparar alternativas de diseño y decidir de forma más formada (no son los únicos: también pesa la experiencia). A diferencia de varios atributos, **acoplamiento y cohesión sí se ven en el diagrama de clases**.
```
Simplicidad 🔴 (KISS·YAGNI) · Robustez 🟡 · Flexibilidad 🔴 (→ Extensibilidad · Mantenibilidad) · Acoplamiento 🔴 · Cohesión 🔴
```

## 1. Acoplamiento · 🔴 🎯
**Grado de dependencia/conocimiento** que un componente tiene sobre otro (componente = clase, conjunto de clases, sistema). Más acoplamiento → los cambios/errores de uno impactan más en el otro = **malo**. Minimizarlo mejora **mantenibilidad, reutilización, analizabilidad** (buscás el fallo donde está) y **testeabilidad**. **Siempre existe un mínimo** si dos componentes se relacionan; se busca el mínimo necesario. Técnica que se usará mucho: **orientar a interfaces** (que las clases concretas no se conozcan, solo el contrato).

> 📌 **Para el parcial / TP (regla de modelado):** en un diagrama de clases no te deben quedar **dos clases "sueltas"** — toda clase se relaciona mínimamente con otra (si no, serían dos sistemas independientes).

## 2. Cohesión · 🔴 🎯
**Cantidad de responsabilidades** de un componente: cuantas más, **menos cohesivo**. Alta cohesión = hace algo específico y bien (elementos abocados al mismo problema). Anti-ejemplo: **god class** (hace de todo) = baja cohesión. **🔴 Lema (grabátelo, no está en el catálogo): alta cohesión, bajo acoplamiento.** ⚠️ **No siempre van juntas** (ejes independientes): god class = baja cohesión + bajo acoplamiento; clase muy cohesiva puede tener alto acoplamiento.

## 3. Simplicidad · 🔴 🎯
- **KISS** (*Keep it simple, stupid*): evitá cualquier complejidad innecesaria.
- **YAGNI** (*You aren't gonna need it*): no agregues funcionalidad que no apunte al problema **actual** ("no pongas lo que nadie pidió"). Sí conviene **diseñar para la extensibilidad**, pero sin construir hoy lo de mañana.

## 4. Robustez · 🟡 📘
Ante uso inadecuado o fallas: no generar comportamiento errático, **reportar el error y volver a un estado consistente** (no guardar parciales), y **facilitar la detección de la causa** (logs → analizabilidad). Prácticas: doble validación (interfaz + servidor), no dejar errores crudos ni genéricos. ⚠️ Es el mismo fenómeno que la **Madurez** (atributo) visto como cualidad de diseño.

## 5. Flexibilidad · 🔴 🎯
Capacidad de reflejar cambios del dominio de forma simple. Se abre en **Extensibilidad** (agregar, con poco impacto) + **Mantenibilidad** (modificar, con el menor esfuerzo).

| Cambio (Parte 1) | Cualidad |
|---|---|
| Evolutivo | **Extensibilidad** |
| Correctivo / Perfectivo | **Mantenibilidad** |

Extensibilidad es la que más vas a nombrar ("quiero que sea fácilmente extensible").

## 6. Trade-offs entre cualidades · 🎯
No se maximizan todas a la vez. **Simplicidad y mantenibilidad van de la mano**; la tensión real es **simplicidad ↔ extensibilidad** (y depende de cómo la plantees: mal hecha viola YAGNI/KISS).

## 7. Para el parcial

**7.1 🔴 El error más común de objetos: polimorfismo ≠ siempre más flexible.**
El polimorfismo sirve con **variación de comportamiento**; con **variación de datos** (mismo comportamiento, cambian datos) hace el diseño **menos** flexible.

```
// Variación de COMPORTAMIENTO → polimorfismo SÍ (cada uno calcula distinto):
Descuento (interfaz)
  ├─ DescuentoPorcentaje  → precio * (1 - %)
  ├─ DescuentoMontoFijo   → precio - monto
  └─ Descuento2x1         → precio / 2

// Variación de DATOS → polimorfismo NO:
❌ ProductoLeche, ProductoPan, ProductoAgua...  // una clase por producto
   // agregar un producto = tocar y recompilar código
✅ Producto("Leche", 1200); Producto("Pan", 900) // una clase Producto + instancias
   // agregar un producto = crear una instancia EN EJECUCIÓN, sin tocar código
```
**Porqué:** no extendés una clase en ejecución, pero sí instanciás objetos en ejecución → resolver variación de datos con polimorfismo te obliga a tocar en compilación = menos flexible. Abusar del polimorfismo → menos mantenible. (flexibilidad = extensibilidad + mantenibilidad.)

**7.2** V/F con **"siempre"/"nunca"** = sospechá trampa: en diseño casi todo *depende*.

**7.3 Casos tipo:**
- **Caso 5.** *Características deseables en un diseño.* → **Alta cohesión + bajo acoplamiento**.
- **Caso 6.** *V/F: el polimorfismo siempre implica mayor flexibilidad.* → **Falso** (ver 7.1; "siempre" ya es alerta).
- **Caso 7.** *V/F: un diseño altamente acoplado es más mantenible por ser más simple.* → **Falso** (alto acoplamiento va contra mantenibilidad).
- **Caso 8.** *Enfoque muy modular con Generics "por si en el futuro".* → ignora **YAGNI + KISS** (no REST ni HTTP).

## 8. Checkpoint Parte 2
Qué son las cualidades y si son el único criterio · definir acoplamiento y qué se gana al minimizarlo · ¿acoplamiento cero? y regla de "no clases sueltas" · cohesión y god class · el lema + ejemplo de alta cohesión con alto acoplamiento · KISS vs. YAGNI vs. "diseñar para extensibilidad" · robustez y su relación con Madurez · las dos ramas de flexibilidad · por qué "polimorfismo siempre flexibiliza" es falso · ¿simplicidad choca con extensibilidad? ¿y con mantenibilidad?

---

# PARTE 3 — Patrones de Diseño

## 1. Qué es un patrón · 🟡 📘
**Solución conocida a un problema conocido y frecuente.** Definiciones de libro: **GoF** (los cuatro autores de *Design Patterns*) y **Debrauwer**; quedate con "problema conocido y frecuente". Objetivo: **reutilizar experiencia**, no reinventar la rueda. Son **orientados a objetos** y son **más que un diagrama de objetos**. *(Diagrama de clases = relación estática; de objetos = instancias en un momento.)*

## 2. Anatomía · 🟡 📘
Estructura completa: Propósito, Motivación, Participantes, Colaboraciones, Consecuencias, Implementación, Usos conocidos, Patrones relacionados. **Esenciales (4):** **Nombre** (da vocabulario), **Problema** (qué/cuándo — sin reglas mecánicas "si X → patrón Y"; se vivencia con ejercicios), **Solución** (abstracta; el diagrama "frío" no sirve sin entender el problema), **🔴🎯 Consecuencias** (efectos; lo **más importante** — son las que te dejan justificar).

## 3. La regla de oro · 🔴 🎯
**Un patrón se usa ÚNICAMENTE cuando se presenta el problema que resuelve.** No "siempre que se pueda". Error a corregir: "meto tres patrones para demostrar que aprendí". Se evalúa **criterio**: aplicarlo bien **o evitarlo con fundamento**. Forzarlo = **sobrediseño** (viola KISS/YAGNI). **Salir del parcial sin ningún patrón puede estar perfecto.**

> 📌 **Para el parcial, si te preguntan:** ante la tentación de meter un patrón, preguntate *"¿aparece de verdad el problema que resuelve?"*. Si no, no lo pongas, y sabé justificar por qué NO (simplicidad/YAGNI).

## 4. Clasificación · 🟡 📘
Tres familias, según el **tipo de problema** (clasificación de GoF, vigente):
- **Creacionales** — crear/instanciar y configurar objetos (decisiones dinámicas en ejecución).
- **Comportamiento** — interacción entre objetos en ejecución (mnemotecnia: "interacción → comportamiento").
- **Estructurales** — armado/uso de estructuras o acoplarse a cosas de terceros sin tocarlas.

| Creacionales | Comportamiento | Estructurales |
|---|---|---|
| Factory Method, Simple Factory, Singleton, Abstract Factory, Builder, Prototype | State, Strategy, Observer, Command, Template Method, Iterator, Memento, Visitor, Interpreter, Chain of Responsibility, Mediator | Adapter, Composite, Facade, Decorator, Proxy, Flyweight, Bridge |

⚠️ **Singleton no se usa en esta materia** (se menciona para conocerlo). Primeros que se ven: comportamiento (State/Strategy/Observer).

## 5. Prerrequisito · 📘
Teoría de **objetos "10 puntos"** (instancia, clase, interfaz, herencia, polimorfismo, diagramas de clases). Los patrones se apoyan en esa base.

## 6. Para el parcial

**6.1 Casos tipo:**
- **Caso 9.** *Un patrón debe usarse…* → **Únicamente cuando se presenta el problema que resuelve.** El tipo (creacional/comportamiento/estructural) clasifica, no decide *cuándo*.
- **Caso 10.** *Los patrones favorecen…* → **Todas:** bajo acoplamiento, extensibilidad, mantenibilidad, reutilización. ⚠️ Salvedad: **abusarlos** los vuelve inmantenibles.

**6.2 Resumen de criterio:** receta conocida para problema conocido (ventajas/desventajas/consecuencias) · usar solo si el problema aparece (forzar = sobrediseño) · se evalúa criterio, no cantidad · lo clave son las **consecuencias** · el diagrama de memoria no sirve.

## 7. Checkpoint Parte 3
Qué es un patrón y su objetivo · por qué es más que un diagrama de objetos (clases vs. objetos) · las 4 partes esenciales y cuál es la clave para justificar · cuándo usar uno + refutar "meto tres patrones" · por qué forzar es error y con qué principios conecta · ¿está bien resolver sin patrón? · las 3 familias y qué problema ataca cada una · un ejemplo de cada familia · qué favorecen (y qué pasa si se abusa) · por qué objetos debe estar sólido.

---

## Cierre — el hilo de la unidad
**Atributos** (qué querés: los 8 grupos + regla de justificación + trade-offs) → **Cualidades** (cómo: alta cohesión/bajo acoplamiento, KISS/YAGNI, polimorfismo con criterio) → **Patrones** (soluciones probadas, solo cuando el problema aparece). Es el vocabulario transversal con el que vas a justificar en cada clase del año.

**FIN DEL RESUMEN — Unidad preclase 03**
