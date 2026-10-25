# 🧱 Clase desde Cero — Clase 01 · Módulo 3: Entradas de la arquitectura y niveles de abstracción

**Unidad:** `clase01` — Arquitectura de Software (25/03/2026)
**Serie:** Módulo 3 de 5 — el mapa completo está en el roadmap de la serie.

---

## Sobre este documento

**Qué cubre:** las siete entradas de la arquitectura de software — con los requerimientos no funcionales y los atributos de calidad como protagonistas — y la jerarquía de niveles de abstracción: estilo arquitectónico, patrón arquitectónico y patrón de diseño.

**Qué NO cubre:** los componentes típicos de un sistema y sus diagramas (Módulo 4), el caso integrador (Módulo 5).

Es el módulo más corto de la serie — y el que más rinde por minuto: todo lo que nombra, el resto del año lo desarrolla.

## De dónde venís

- **Del Módulo 1:** RF ("qué hace el sistema") y RNF ("cómo funciona"), con las restricciones y reglas de negocio colgando de los no funcionales. Y un hilo sembrado: *"los RNF son la entrada principal de la arquitectura"* — acá se cobra.
- **Del Módulo 2:** la arquitectura toma decisiones tempranas, difíciles de cambiar, que gobiernan al sistema durante años. Y quedó una pregunta abierta: **¿con qué información se toman esas decisiones?** Este módulo la responde.

---

## 1. Las siete entradas de la arquitectura 🔴

Las decisiones arquitectónicas no se toman con intuición: se toman procesando **entradas** — la información que entra al proceso de arquitectura y de la que salen las decisiones. Son siete:

```
      Req.                          Req. No Funcionales
      Funcionales                   & Atributos de Calidad
            │                             │
   Estilos y│                             │Restricciones
   Patrones │    ┌─────────────────┐      │de Negocio
   Arquitec.┼────┤   ENTRADAS DE   ├──────┼──
            │    │   LA ARQ. DE    │      │
  Experiencia    │    SOFTWARE     │      │Restricciones
  del       ┼────┤                 ├──────┼──
  Arquitecto│    └────────┬────────┘      │Técnicas
            │             │               │
                    Futuros req.
```

Una por una, cada entrada con su ejemplo concreto:

| Entrada | Ejemplo concreto |
|---|---|
| **Requerimientos Funcionales** | "El sistema debe permitir registrar pedidos de compra" |
| **Requerimientos No Funcionales / Atributos de Calidad** | "El sistema debe responder en menos de 2 segundos con 10.000 usuarios simultáneos" |
| **Restricciones Técnicas** | "Debe correr en la nube de Azure" / "El cliente ya tiene licencias de Oracle" |
| **Restricciones de Negocio** | "Debe estar listo en 6 meses" / "El presupuesto es de $50.000" |
| **Requerimientos Futuros** | "En 2 años necesitaremos abrir el sistema a clientes externos vía API" |
| **Experiencia del Arquitecto** | "Ya resolvimos algo similar con microservicios, sabemos que escala bien" |
| **Estilos y Patrones Arquitectónicos** | Microservicios, MVC, Event-Driven, Monolítico… |

Cuatro comentarios que ordenan la tabla:

**Las primeras cinco vienen del problema; las últimas dos, del que resuelve.** RF, RNF, restricciones y requerimientos futuros los trae el cliente y su contexto. La experiencia del arquitecto y el catálogo de estilos y patrones los trae el equipo: las soluciones que ya funcionaron antes también son insumo — nadie diseña desde el vacío.

**Los requerimientos futuros son entrada aunque todavía no existan.** "En 2 años abriremos el sistema vía API" no es un requisito de hoy — pero si la arquitectura de hoy lo hace imposible, en 2 años vas a estar derrumbando paredes. Como las decisiones son difíciles de cambiar (Módulo 2), lo que se viene también entra al plano.

**Las restricciones no se debaten: acotan.** Que el cliente ya tenga licencias de Oracle o que el presupuesto sea $50.000 no es información a evaluar — es el borde de la cancha. Distinguí el rol: los requerimientos dicen qué lograr; las restricciones dicen dentro de qué límites.

**Y la protagonista: los RNF / atributos de calidad.** No es una entrada más entre siete, y el porqué ya lo tenés de los módulos anteriores. En el Módulo 2 viste que la arquitectura de sistema *"contiene las estrategias para resolver los atributos de calidad"* — es decir: resolver esas propiedades **es su trabajo**. Los atributos de calidad son justamente esas propiedades emergentes que los RNF expresan — que sea rápido, que esté disponible, que sea seguro. Pensalo así: los RF casi no discriminan entre arquitecturas (un sistema "que registre pedidos" se puede construir con cualquier estructura); lo que **decide la estructura** es el "cómo": responder en menos de 2 segundos **con 10.000 usuarios simultáneos** exige una arquitectura muy distinta que con 10 usuarios. Por eso, cuando el resto de la materia estudie los atributos de calidad en profundidad y con nombre propio, ya vas a saber qué lugar ocupan: son el insumo número uno del arquitecto.

> **Para el parcial, si te preguntan — las entradas de la arquitectura.**
> Son siete: requerimientos funcionales; requerimientos no funcionales / atributos de calidad; restricciones técnicas; restricciones de negocio; requerimientos futuros; la experiencia del arquitecto; y los estilos y patrones arquitectónicos. Dada una frase, se clasifica por su rol: qué debe hacer (RF), cómo debe funcionar (RNF), límite impuesto por tecnología o negocio (restricción), necesidad anticipada (futuro), o conocimiento previo del equipo (experiencia / estilos y patrones).

---

## 2. Estilo, patrón arquitectónico y patrón de diseño 🔴

La séptima entrada merece módulo aparte dentro del módulo. "Microservicios, MVC, Monolítico…" no son cosas del mismo tamaño: viven en una **jerarquía de niveles de abstracción**, tres cajas anidadas donde la de afuera es la más abstracta y la de adentro la más concreta:

```
 ▲  ┌────────────────────────────────────────────────────┐
 │  │  ESTILO ARQUITECTÓNICO                             │
 │  │  el esqueleto general de la aplicación             │
 N  │                                                    │
 i  │   ┌──────────────────────────────────────────────┐ │
 v  │   │  PATRÓN ARQUITECTÓNICO                       │ │
 e  │   │  plantilla de construcción: subsistemas      │ │
 l  │   │  predefinidos con reglas de organización     │ │
    │   │                                              │ │
 d  │   │   ┌────────────────────────────────────────┐ │ │
 e  │   │   │  PATRÓN DE DISEÑO                      │ │ │
    │   │   │  solución para refinar componentes     │ │ │
 a  │   │   └────────────────────────────────────────┘ │ │
 b  │   └──────────────────────────────────────────────┘ │
 s  └────────────────────────────────────────────────────┘
 t
 r.
```

**Estilo arquitectónico** — el nivel más abstracto:
- Es la **descripción del esqueleto estructural y general** para aplicaciones.
- Es **independiente de otros estilos**.
- **Expresa componentes y sus relaciones.**

Es la decisión de forma más gruesa que existe: ¿el sistema es una sola pieza grande (**monolítico** — todo el sistema en una única unidad desplegable) o muchas piezas chicas independientes (**microservicios** — el sistema partido en servicios pequeños que se comunican entre sí)? Esa elección es un estilo: define el esqueleto de todo lo demás.

**Patrón arquitectónico** — el nivel intermedio:
- **Define la estructura básica de la aplicación.**
- **Puede contener o estar contenido en otros patrones.**
- Provee un **subconjunto de subsistemas predefinidos, con reglas y pautas para su organización**.
- Es una **plantilla de construcción**.

Dentro del esqueleto que fijó el estilo, el patrón arquitectónico te da la plantilla de las partes. Ejemplo con glosa mínima: **MVC** organiza la aplicación en tres subsistemas con roles fijos — Modelo (los datos y su lógica), Vista (la presentación) y Controlador (la coordinación entre ambos). Eso es exactamente "subsistemas predefinidos con reglas de organización". Y **Event-Driven** estructura la aplicación alrededor de componentes que reaccionan a eventos. Fijate el detalle de "puede contener o estar contenido": los patrones se combinan y anidan — no son excluyentes como los estilos.

**Patrón de diseño** — el nivel más concreto:
- Es una **solución para refinar componentes**.

Una sola línea en la jerarquía… y el eje de gran parte del año. Cuando el esqueleto está elegido (estilo) y las partes están planteadas (patrón arquitectónico), falta resolver bien el **interior de cada componente**: cómo organizar sus clases y objetos para que el diseño sea flexible y mantenible. Esas soluciones con nombre propio — los patrones de diseño — son el casillero que este módulo deja armado: la materia lo empieza a llenar muy pronto, y no para hasta fin de año.

La regla mnemotécnica de la jerarquía: **el estilo decide el esqueleto, el patrón arquitectónico decide las partes, el patrón de diseño refina cada parte por dentro.** De mayor a menor abstracción, de decisión más gruesa a más fina.

> **Para el parcial, si te preguntan — estilo vs. patrón arquitectónico vs. patrón de diseño.**
> Son tres niveles de abstracción anidados, de mayor a menor: el **estilo arquitectónico** describe el esqueleto estructural general de la aplicación y es independiente de otros estilos; el **patrón arquitectónico** es una plantilla de construcción que provee subsistemas predefinidos con reglas de organización, y puede contener o estar contenido en otros patrones; el **patrón de diseño** es una solución para refinar componentes. Ejemplo de clasificación: monolítico o microservicios son estilos; MVC es un patrón arquitectónico.

---

## ✅ Checkpoint — Módulo 3

*Sin respuestas acá: intentalas sin mirar el material, y las dudas se resuelven por chat.*

1. Clasificá cada frase en su entrada, justificando en una línea: **a)** "La app debe seguir funcionando aunque se caiga uno de los servidores" · **b)** "El equipo ya construyó tres sistemas de facturación con esta estructura" · **c)** "El año que viene la empresa abre sucursales en Chile y el sistema deberá soportar otra moneda" · **d)** "Todo debe estar operativo antes del Hot Sale" · **e)** "El cliente exige que corra sobre los servidores Linux que ya tiene".
2. ¿Por qué los RNF/atributos de calidad son la entrada protagonista y no los RF? Explicalo con el argumento de "qué discrimina entre arquitecturas" y con la característica de la arquitectura de sistema que viste en el Módulo 2.
3. Los requerimientos futuros todavía no existen como requisitos. ¿Por qué igual son entrada de la arquitectura? Conectalo con una característica de las decisiones arquitectónicas.
4. "La experiencia del arquitecto es una entrada válida" vs. "guiarse por la experiencia es repetir siempre la misma solución". ¿Dónde está el límite? ¿Qué gana y qué arriesga un arquitecto que pesa mucho esta entrada?
5. Ordená de mayor a menor nivel de abstracción y decí qué decide cada uno: patrón de diseño, estilo arquitectónico, patrón arquitectónico.
6. Te describen tres cosas; identificá cuál es cuál y justificá con sus propiedades: **a)** "una plantilla que organiza la aplicación en subsistemas predefinidos con reglas, combinable con otras" · **b)** "una solución para refinar el interior de un componente" · **c)** "el esqueleto general de la aplicación, independiente de otras opciones de su tipo".
7. Un equipo con presupuesto chico y 3 meses de plazo debe elegir entre monolítico y microservicios. ¿Qué entradas están empujando la decisión, y hacia dónde? Armá la respuesta con el molde *"elegiría [X] porque [entrada] pide [Y], aunque pago con [Z]"*. *(No hay una única respuesta correcta — lo que se evalúa es la justificación.)*
8. ¿En qué sentido los "estilos y patrones arquitectónicos" son a la vez una **entrada** de la arquitectura y una **salida** de arquitecturas anteriores?

---

## Qué viene en el Módulo 4

Ya está el proceso completo: entradas → decisiones → estructura. Falta responder algo muy concreto: en un Sistema de Información típico, ¿**cuáles** son los componentes que casi siempre aparecen — y cómo se **dibuja** todo esto? El Módulo 4 presenta al trío Frontend / Backend / Base de Datos y las dos herramientas UML para comunicar la arquitectura: el diagrama de componentes y el de despliegue. Ahí también se cierra el hilo pendiente del Módulo 2: cómo se dibuja una interfaz.

---

**FIN DEL MÓDULO 3**
