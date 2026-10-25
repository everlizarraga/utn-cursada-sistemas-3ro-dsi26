# Machete — Preclase 03: Atributos, Cualidades y Patrones

---

## P1 · ATRIBUTOS DE CALIDAD (ISO 25010)

**Para qué:** criterios objetivos → comparar diseños / justificar decisiones. Calidad (ISO 25000) = grado en que satisface a usuarios aportando valor; características + subcaracterísticas; desde el diseño, no al final.

**Regla de oro:** varias clasificaciones (ISO / IEEE / MITRE) → no de memoria. **Definí el atributo + justificá contra tu def + dónde se ve + cómo se mide + qué decisión lo garantiza + trade-off.**

**Valen para todos:**
- Cuantificables/medibles **bajo condiciones**.
- **Trade-offs**: no se maximizan todos.
- **Estático vs. ejecución** — no se ven en diagrama de clases: eficiencia, usabilidad, disponibilidad. Sí: acoplamiento/cohesión.

**Las 8 (característica → subcaracterísticas):**

| Característica | Subcaracterísticas | Clave |
|---|---|---|
| Adecuación funcional | Completitud · Corrección · Pertinencia | no se usa para comparar |
| Eficiencia desempeño | Comportamiento temporal (lag) · Utilización recursos · Capacidad | recurso: cliente vs. servidor |
| Compatibilidad | Coexistencia · Interoperabilidad | interop ≠ integración (estándar; HL7 FHIR) |
| Usabilidad | Adecuación · Aprendizaje · Operabilidad · Protección errores · Estética · Accesibilidad | no UI; nivel "conocimiento" |
| Fiabilidad | **Madurez** · **Disponibilidad** · Tolerancia fallos · Recuperabilidad | Robustez ≠ Madurez |
| Seguridad | Confidencialidad · Integridad · No repudio (firma digital) · Responsabilidad · Autenticidad | **tríada CIA** |
| Mantenibilidad | Modularidad · Reusabilidad · Analizabilidad · Modificabilidad · Testeabilidad | = bajo acopl. + alta cohesión |
| Portabilidad | Adaptabilidad · Instalable · Reemplazable | ≠ compatibilidad (mover vs. convivir) |

**Cruces:** Robustez → Madurez · Seguridad + Disponibilidad (CIA) · Integración vs. Interoperabilidad · Portabilidad vs. Compatibilidad.

**Auto-test:** para qué sirven · regla de justificación · medible+condiciones · trade-offs · estático/ejecución · interop vs. integración · 500 → ¿por qué Madurez? · tríada CIA · portabilidad vs. compatibilidad · por qué mantenibilidad central.

---

## P2 · CUALIDADES DE DISEÑO

`Simplicidad (KISS·YAGNI) · Robustez · Flexibilidad (→ Extensibilidad · Mantenibilidad) · Acoplamiento · Cohesión`

- **Acoplamiento** = grado de conocimiento/dependencia entre componentes. Alto = malo (propaga cambios/errores). Minimizar → mantenibilidad, reutilización, analizabilidad, testeabilidad. Siempre un mínimo. Técnica: **orientar a interfaces**. Regla TP/parcial: **no clases sueltas**.
- **Cohesión** = cantidad de responsabilidades. Alta = pocas y específicas. God class = baja. **Lema: alta cohesión, bajo acoplamiento** (ejes independientes: no siempre van juntos).
- **Simplicidad**: **KISS** = sin complejidad innecesaria; **YAGNI** = no agregar lo que no se pide ahora (sí diseñar para extensibilidad, sin construirla).
- **Robustez**: ante falla/uso inadecuado → no errático, reportar + volver a estado consistente, facilitar detección (logs), doble validación. = Madurez (atributo).
- **Flexibilidad** = reflejar cambios. Extensibilidad (agregar, poco impacto) + Mantenibilidad (modificar, menor esfuerzo). Evolutivo→extensibilidad; correctivo/perfectivo→mantenibilidad.
- **Trade-offs**: no se maximizan todas. Simplicidad + mantenibilidad de la mano; tensión: simplicidad ↔ extensibilidad.

**Polimorfismo (error top objetos):** flexibiliza con **variación de comportamiento**; con **variación de datos** → menos flexible.
```
Descuento (interfaz) → Porcentaje / MontoFijo / 2x1   // comportamiento varía → polimorfismo SÍ
Producto("Leche",1200), Producto("Pan",900)          // datos varían → 1 clase + instancias
```
Instanciás en ejecución; no extendés clases en ejecución → resolver datos con polimorfismo = tocar en compilación. Abusar → menos mantenible. V/F con "siempre/nunca" = trampa.

**Auto-test:** qué son · acoplamiento + beneficios de minimizar · ¿acoplamiento cero? / no clases sueltas · cohesión + god class · el lema + cohesión alta con acoplamiento alto · KISS vs. YAGNI vs. extensibilidad · robustez ↔ Madurez · ramas de flexibilidad · por qué "polimorfismo siempre flexibiliza" es falso · simplicidad vs. extensibilidad/mantenibilidad.

---

## P3 · PATRONES DE DISEÑO

- **Qué es**: solución conocida a problema conocido y frecuente (GoF, Debrauwer). Objetivo: reutilizar experiencia, no reinventar la rueda. OO. Más que un diagrama de objetos. (clases = estático; objetos = instancias/foto).
- **Anatomía** — completa: Propósito, Motivación, Participantes, Colaboraciones, Consecuencias, Implementación, Usos conocidos, Relacionados. **Esenciales:** Nombre · Problema (sin reglas "si X→patrón Y"; se vivencia) · Solución (diagrama frío no sirve) · **Consecuencias** (lo más importante → justifican).
- **Regla de oro**: usar **únicamente cuando aparece el problema que resuelve**. No "meter patrones para lucirse". Forzar = sobrediseño (viola KISS/YAGNI). Se evalúa criterio: aplicarlo bien **o evitarlo con fundamento**. Sin patrón puede estar OK.
- **Clasificación** (GoF, por tipo de problema):

| Creacionales (instanciar/configurar) | Comportamiento (interacción entre objetos) | Estructurales (estructuras / acoplarse a terceros) |
|---|---|---|
| Factory Method, Simple Factory, Singleton, Abstract Factory, Builder, Prototype | State, Strategy, Observer, Command, Template Method, Iterator, Memento, Visitor, Interpreter, Chain of Responsibility, Mediator | Adapter, Composite, Facade, Decorator, Proxy, Flyweight, Bridge |

  Singleton: **no se usa** (solo mencionado). Primeros: State/Strategy/Observer.
- **Prerrequisito**: objetos "10 puntos" (instancia, clase, interfaz, herencia, polimorfismo, diagramas).

**Auto-test:** qué es + objetivo · más que diagrama de objetos (clases vs. objetos) · 4 partes esenciales + la clave · cuándo usar + refutar "meto tres patrones" · por qué forzar es error · ¿resolver sin patrón? · 3 familias + problema de cada una · ejemplo de cada · qué favorecen / abuso · por qué objetos sólido.

---

**Hilo:** Atributos (qué) → Cualidades (cómo: alta cohesión/bajo acoplamiento, KISS/YAGNI) → Patrones (solo si aparece el problema).
