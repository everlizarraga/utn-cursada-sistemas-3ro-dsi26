# 🎓 APUNTE MAESTRO — Seminario "Proceso de diseño empleando IA Generativa" · Encuentro 1
## Parte 3 — Seguridad y riesgos

**Unidad:** `seminario-ia-encuentro1` · **Parte 3 de 6** · Diseño de Sistemas de Información — UTN FRBA, 2026

> Delegar exige poner límites. Esta parte es esa idea desarrollada. Primero lo que **sí controlamos** (qué le mandamos al agente y a qué le damos acceso) y después lo que **no controlamos** (con qué se entrenaron los modelos, dónde corren, quién los financia, qué cuestan de verdad). Cierra el Bloque 1 del seminario.
>
> Una simplificación que se usa en toda la parte: se habla de **agentes, chatbots e IA como si fueran lo mismo**. No lo son, pero para hablar de riesgos la distinción no cambia nada.

---

## 1. 🔴 Qué pasa con lo que le mandás

### 1.1 El caso: credenciales pegadas en un chat

Muchas empresas usan un chatbot de ventas llamado **Drift**. Para resolver problemas rápido, usuarios de esas empresas le pegaban al chatbot lo que tenían a mano: **tokens** y **contraseñas** de plataformas como AWS. El chatbot los ayudaba. Pero es un servicio de un tercero, y ese tercero guarda las conversaciones.

Después le robaron datos a ese proveedor. Y lo que había en esos datos no era solo información del proveedor: eran **las credenciales de todas las empresas** cuyos empleados habían pegado ahí lo que necesitaban para "resolver el laburo". Por eso el impacto fue enorme: no cayó una empresa, cayeron muchas.

> **Glosas —** *token de acceso / API key*: una clave que identifica y autoriza a un programa ante un servicio; quien la tiene, entra. *AWS*: la nube de Amazon.

### 1.2 La regla general

Todo lo que le mandás a un agente (el código que pegás en el chat, el código que lee de tu **file system**, los datos, las conversaciones) **va a un proveedor externo y queda fuera de tu control.**

> **Glosa —** *file system*: tu disco. Un agente de código con permisos amplios puede leer cualquier archivo que tengas.

¿Qué hacen las empresas con eso? Lo **guardan**, lo **analizan**, lo pueden **compartir**, y en la mayoría de los planes **entrenan con eso**. Vale para todas: OpenAI (ChatGPT), Anthropic (Claude), Google (Gemini), GitHub (Copilot), Cursor, Grok. **Por defecto, la mayoría entrena con tus datos**, salvo que tengas un **plan de empresa** o lo **configures** específicamente. Con un plan **personal o gratuito**, entrenan.

### 1.3 Dos casos más

- El **jefe de CISA** (la agencia de ciberseguridad de Estados Unidos) compartió información sensible **no clasificada** del gobierno con el ChatGPT gratuito, el común, aparentemente sin siquiera estar logueado. La persona que debería cuidar más la seguridad. Por suerte sus propias plataformas de seguridad lo bloquearon.
- **LayerX**, una empresa que vigila lo que los empleados mandan desde el navegador, midió que el **77 %** de quienes usan IA en el trabajo están mandando **datos de la empresa**. Y encima con **cuenta personal**.

La conclusión es incómoda: **por defecto, estamos usando mal.**

---

## 2. 🔴 Cómo protegerse: lo que sí controlás

Como estudiantes de ingeniería, y después como ingenieros, la seguridad tiene que estar siempre en la cabeza. Cuatro reglas:

### 2.1 No mandar nada sensible

Datos personales, contraseñas, API keys, tokens. **Nada.** Ni "solo esta vez para resolver rápido".

### 2.2 Permisos mínimos

Más allá de lo que pegues en el chat, importa **cómo está configurado** el agente: según la configuración, puede tener acceso a **todo lo que hay en tu máquina**. Y en tu máquina puede haber **credenciales** que dan acceso a bases de datos de producción o de clientes. La regla: darle permiso para **leer lo mínimo necesario y lo justo**.

Es la contracara de lo que vimos en la Parte 2, §2.4: el harness te permite controlar los accesos; **usalo**.

### 2.3 Aislar

Correr el agente en un **entorno virtualizado** o **dockerizado**: si algo sale mal, sale mal adentro de una caja, no en tu máquina real.

> **Glosa —** *dockerizar*: correr algo dentro de un contenedor Docker, un entorno aislado del sistema principal.

### 2.4 No acoplarse a un proveedor

Diseñar de forma que **migrar de servicio sea fácil**. Vale por **seguridad** y también por **costos**: si el proveedor cambia las reglas o los precios, tenés que poder irte. Es exactamente el criterio de **bajo acoplamiento** que ya se usa en diseño, aplicado al proveedor de IA.

```
   ❌ acoplado                              ✅ desacoplado
   ─────────────────────────────            ──────────────────────────────
   el código habla directo con la           una capa propia habla con el
   API de UN proveedor; cambiar             proveedor; cambiarlo es tocar
   de proveedor es reescribir todo          esa capa y nada más
```

> **Para el parcial, si te preguntan:** *¿Qué medidas mínimas tomás para usar un agente de código sin exponer a tu equipo?*
> Cuatro: no mandar nada sensible (credenciales, tokens, datos personales); darle permisos mínimos, porque puede leer toda la máquina incluidas credenciales de producción; aislarlo en un entorno virtualizado o dockerizado; y no acoplar el sistema al proveedor, para poder migrar por seguridad o por costos. Todo lo que se le manda va a un tercero que, por defecto, lo guarda y entrena con eso.

---

## 3. 🟡 Lo que no controlás

Todo lo anterior depende de nosotros. Lo que sigue, no. Son riesgos que existen por cómo está armada la industria, y hay que conocerlos para usar la herramienta con criterio.

### 3.1 🟡 Con qué se entrenaron

¿De dónde salen los datos con los que se entrenaron estos modelos? Las propias empresas, tanto OpenAI como Anthropic, **admitieron** haber entrenado con material con **copyright**, sin pedir permiso ni pagar. Anthropic tuvo que pagar **1.500 millones de dólares** a autores de libros que se habían bajado de sitios piratas. Incluso compraron libros usados y los escanearon para entrenar. No son las empresas más éticas del mundo.

### 3.2 🟡 Soberanía digital

La mayoría de los data centers están en **Estados Unidos**, y ahora crece **China**. **El 90 % de la capacidad de cómputo está en dos países** y en muy pocas empresas. **Más de 150 países no tienen cómputo propio, Argentina incluida.**

Consecuencias:

- Tus datos están en otro país, bajo **otra soberanía y otras leyes**. Si un gobierno los pide, la empresa se los da, y no tenés control.
- **Las reglas las decide un tercero** y pueden cambiar mañana.
- Información y cómputo se concentran en muy pocas manos.

> **Glosa —** *soberanía digital*: la capacidad de un país de controlar sus propios datos e infraestructura.

### 3.3 🟡 ¿Es sostenible financieramente?

Son **empresas privadas**: no publican balances, solo lo que deciden compartir; no se puede validar lo que dicen. Con eso, OpenAI declaró **perder unos 14.000 millones de dólares** solo este año, y todas están yendo a pérdida.

Preguntas abiertas: para empezar a ganar plata, ¿nos van a cobrar más? ¿cuándo? ¿cuál es el costo real de esto? ¿está subsidiado? El gran ganador de la IA es **NVIDIA**, que fabrica el hardware para correr todo esto, e **invierte en sus propios clientes**. No sabemos cuánto sale hoy ni cuánto va a terminar saliendo.

### 3.4 🟡 Huella ambiental

Estos modelos son muy pesados de correr: necesitan mucha energía y muchos data centers.

- Para **2030** se estima que los data centers de IA van a consumir **945 TWh por año**: casi el **triple** de lo que consumen Pakistán, Bangladesh y Nigeria **juntos**.
- Para enfriar los equipos se usa **agua dulce**, que **compite con el agua de la población** de la zona.
- Suben las **tarifas** locales. Cuando la energía no alcanza, se usan **plantas fósiles**.
- Y no genera puestos de trabajo locales. Aun así, hay regiones que quieren tener sus propios data centers, Argentina entre ellas.

> **Glosa —** *TWh (teravatio-hora)*: unidad de energía; 1 TWh alcanza para abastecer una ciudad mediana durante meses.

### 3.5 🔴 Riesgo cognitivo: la deuda que acumulás vos

¿Qué dejamos de hacer nosotros cuando delegamos en la IA? **Estamos dejando de pensar.**

Estudios del MIT midieron que quienes escribían ensayos con ChatGPT **bajaban mucho su actividad cerebral** y después **ni siquiera podían citar su propio texto**. A eso se le llama **deuda cognitiva**: lo que dejás de ejercitar.

La ironía es grande: **es justo ahora cuando más tenemos que poder supervisar** lo que hacen estos modelos (Parte 2, §7 y §9). Si dejás de pensar, no podés validar. La pregunta que hay que hacerse cada vez: *¿qué es lo que estoy dejando de hacer yo?*

### 3.6 🟢 Riesgo político

Estas empresas **eligen a quién le dan servicio**; a sus enemigos políticos, no. Se hacen **campañas de espionaje con IA**. Todas firmaron contratos con el **Pentágono**; empresas como **Palantir** explotan la IA para espionaje digital; se usa en la guerra. Se pueden hacer muchas cosas lindas con IA, y también muchas negativas.

---

## 4. 🟡 Modelos privados y modelos de pesos abiertos

Todo lo anterior habla de **modelos privados**: no conocemos su código, no podemos configurarlos por dentro ni correrlos nosotros.

Existen los **modelos abiertos**, que sí se pueden correr en **infraestructura propia**:

| | Privado | Abierto |
|---|---|---|
| **Dónde corre** | En la nube del proveedor | En tu infraestructura |
| **Tus datos** | Salen | **No salen** |
| **Dependencia del proveedor** | Alta | Menor |
| **Tamaño típico** | Enormes | Suelen ser más chicos y livianos |

Una aclaración importante: **no son "open source" como una librería open source.** Son modelos de **pesos abiertos**: lo que se publica son los pesos (los parámetros ya entrenados), **no el código**, y en varios casos tampoco se sabe bien cómo fueron entrenados.

> **Glosa —** *pesos*: los millones de números que resultan del entrenamiento; son "el modelo" en sí, no el programa que lo entrena.

Un caso regional: **Latam-GPT**, un modelo hecho en **Chile** junto con varios países de Latinoamérica, entrenado con contexto de toda la región.

---

## 5. 🔴 Cierre del Bloque 1: criterio y cautela

Después de una lista así, la sensación puede ser catastrófica. No es la idea. **La usamos.** Todos los días. Y está muy bien. Pero:

- Es **todo muy reciente**: empezó hace pocos años; no sabemos cómo va a evolucionar ni sus **efectos a largo plazo**. Mucho de lo que se dice sobre sus efectos son todavía teorías.
- Usamos IA **como usuarios** y, como ingenieros y futuros ingenieros, **construimos con ella**.
- En las dos cosas: **mirada crítica.**

Con eso cierra el primer bloque del seminario: qué es un modelo (Parte 1), qué lo convierte en agente y qué no sabe (Partes 1 y 2), y qué riesgos trae delegarle cosas (esta parte). El Bloque 2 empieza a construir: cómo diseñar un agente efectivo.

---

## Síntesis de la Parte 3

| Idea | En una línea |
|---|---|
| Lo que mandás | Va a un tercero; por defecto lo guarda y entrena con eso (salvo plan empresa / configuración). |
| Casos | Drift (credenciales de muchas empresas), el jefe de CISA, LayerX (77 % manda datos de la empresa). |
| Protección | Nada sensible · permisos mínimos · aislar (virtualizar/dockerizar) · no acoplarse al proveedor. |
| Entrenamiento | Con material con copyright sin permiso; Anthropic pagó 1.500 M USD. |
| Soberanía | 90 % del cómputo en dos países; 150+ países sin cómputo propio (Argentina). |
| Finanzas | Empresas privadas a pérdida (OpenAI: −14.000 M USD/año); costo real desconocido; NVIDIA gana. |
| Ambiente | 945 TWh/año en 2030; agua dulce; tarifas; plantas fósiles. |
| Cognitivo | Deuda cognitiva: dejar de pensar justo cuando más hay que supervisar. |
| Político | Espionaje, contratos militares, servicio selectivo. |
| Abiertos | Pesos abiertos ≠ open source; datos no salen; Latam-GPT. |
| Actitud | Usarla, sí. Con criterio, cautela y mirada crítica. |

---

## ✅ Checkpoint (sin respuestas)

1. Un compañero del TPA pega en el chat del agente el `application.properties` con la URL y la contraseña de la base de datos "para que entienda el proyecto". ¿Qué está mal, qué puede pasar y qué tendría que haber hecho?
2. ¿Por qué "permisos mínimos" es una regla de configuración del harness y no solo de sentido común del usuario?
3. Tu equipo decide usar un solo proveedor de IA como componente del sistema. ¿Qué decisión de diseño tomás para no quedar acoplado, y qué atributo de calidad estás priorizando?
4. ¿Qué diferencia hay entre un modelo open source y uno de pesos abiertos? ¿Cuál de los dos podés auditar?
5. ¿Por qué correr un modelo abierto en infraestructura propia resuelve el riesgo de soberanía pero no el de sostenibilidad ni el ambiental?
6. Explicá con tus palabras qué es la deuda cognitiva y por qué el seminario la llama "ironía".
7. Si el costo real de los modelos está subsidiado, ¿qué riesgo asume un sistema que depende de ellos y cómo se mitiga con lo visto en §2.4?

---

## Qué viene en la Parte 4

Arranca el Bloque 2: **cómo diseñar un agente efectivo**. La Parte 4 presenta el **context engineering** (qué contexto darle al agente, cuándo y cómo), el mapa de herramientas en cuatro propósitos (skills, MCP, rules, hooks, subagentes) y después las **skills** en profundidad: cómo nacen, cómo se estructuran, cómo se disparan y cuándo vale la pena escribir una.

---

**FIN DE LA PARTE 3 — Seguridad y riesgos**
