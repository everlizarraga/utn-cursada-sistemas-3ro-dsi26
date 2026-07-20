# Kahoot — Repaso: atributos de calidad, cualidades, patrones

> Conversión fiel a Markdown de kahoot-repaso.pdf (26 páginas).
>
> **Notas de conversión:**
> - El PDF es una secuencia de **capturas de pantalla** de una partida de Kahoot (vista del docente/proyector). Cada página se transcribe en orden.
> - Convención de Kahoot para las opciones (forma + color): **▲ triángulo rojo · ◆ rombo azul · ● círculo amarillo · ■ cuadrado verde**. Se preserva la forma/color de cada opción.
> - En las páginas de **resultado** se marca la(s) respuesta(s) correcta(s) con ✓ y las incorrectas con ✗, y se transcriben los conteos de votos tal como aparecen en las barras.
> - Los números transitorios de la UI (temporizador y "Respuestas" en curso durante cada pregunta) no se transcriben por no ser contenido sustantivo; sí se transcriben los conteos finales de cada resultado.
> - **Página 26 (podio):** el nombre del 3.º puesto queda **tapado por el avatar y el confeti**; solo son legibles los fragmentos "…ti" / "…ola", que no se transcriben como nombre para no inventar. Los finalistas 4 y 5 aparecen **truncados por la propia UI de Kahoot** ("Uriel M…", "El desl…").

---

## Página 1 — Lobby (sala de espera)

Pantalla de ingreso de Kahoot.

- **PIN de juego:** 655 4484
- Únete en **www.kahoot.it** o con la **app de Kahoot!** (incluye código QR).
- Jugadores en la sala (nombres/apodos visibles): Facundo Zonis, TeamTurri, Pablo Rejas, Diego, Mati Di Mascio, Franco Ibañez, Valentin, Fernando, Pato, Mauro Schinca, Ignacio J, valen, Luca Vallazza, Jeronimo A, Franco Bidart, NicolasCerar, Cami :).
- Contador de participantes: **118**.

*(Captura de UI — no aporta contenido de estudio; se transcribe por cobertura.)*

---

## Página 2 — "Repaso lectura previa" (meme)

Diapositiva de sección con un meme.

**Meme:** un pingüino (estilo *Pudgy Penguins / Pengu*) con una lupa sobre un libro abierto, y el texto grande **"AI DETECTED"**.

*(El chiste ES contenido: alude en tono humorístico a "detectar IA" en la lectura previa antes de arrancar el repaso.)*

---

## Página 3 — Diapositiva: Atributos de calidad

**Título:** Atributos de calidad

**Texto:** Se presentan casos concretos, se deberá seleccionar el atributo/subatributo de calidad que más se adecue a la situación.

**Imagen:** una mano sostiene un globo terráqueo con el sello **ISO** e íconos de documentos con tildes de verificación (alusión a normas de calidad).

*(Diapositiva de consigna — introduce el bloque de preguntas siguiente.)*

---

## Página 4 — Pregunta 1 (selección simple)

**Consigna:** Seleccione qué atributo de calidad no se está cumpliendo

**Caso:** Los usuarios de la plataforma experimentan errores HTTP 500 (server error) de forma recurrente al enviar formularios con datos vacíos o inválidos.

**Opciones:**
- ▲ (rojo) Seguridad
- ◆ (azul) Compatibilidad
- ● (amarillo) Robustez
- ■ (verde) Disponibilidad

---

## Página 5 — Resultado Pregunta 1

**Respuesta correcta:** ● (amarillo) **Robustez** ✓

**Conteo de votos:**
- ▲ Seguridad — 7 ✗
- ◆ Compatibilidad — 13 ✗
- ● Robustez — 82 ✓
- ■ Disponibilidad — 40 ✗

---

## Página 6 — Pregunta 2 (selección múltiple)

**Consigna:** Mencione qué atributo(s) de calidad se ven afectados

**Caso:** Un colega sube accidentalmente a GitHub el archivo .properties con las credenciales de la base de datos. Poco después, la base de datos queda inaccesible y su contenido es alterado.

**Opciones:**
- ▲ (rojo) Performance
- ◆ (azul) Seguridad
- ● (amarillo) Disponibilidad
- ■ (verde) Corrección funcional

---

## Página 7 — Resultado Pregunta 2

**Respuestas correctas:** ◆ (azul) **Seguridad** ✓ · ● (amarillo) **Disponibilidad** ✓

**Conteo de votos:**
- ▲ Performance — 17 ✗
- ◆ Seguridad — 133 ✓
- ● Disponibilidad — 89 ✓
- ■ Corrección funcional — 29 ✗

---

## Página 8 — Pregunta 3 (selección múltiple)

**Consigna:** Mencione qué atributo(s) de calidad se ven afectados

**Caso:** Una API REST permite consultar la lista de usuarios mediante el endpoint: GET /users

Inicialmente, con pocos registros, la respuesta es rápida y el sistema funciona correctamente. Sin embargo, a medida que la base de datos crece, el endpoint comienza a devolver miles de registros en una sola respuesta, ya que no implementa paginación.

**Opciones:**
- ▲ (rojo) Portabilidad
- ◆ (azul) Experiencia de usuario
- ● (amarillo) Tiempo de respuesta (latencia)
- ■ (verde) Utilización de recursos

---

## Página 9 — Resultado Pregunta 3

**Respuestas correctas:** ◆ (azul) **Experiencia de usuario** ✓ · ● (amarillo) **Tiempo de respuesta (latencia)** ✓ · ■ (verde) **Utilización de recursos** ✓

**Conteo de votos:**
- ▲ Portabilidad — 11 ✗
- ◆ Experiencia de usuario — 102 ✓
- ● Tiempo de respuesta (latencia) — 128 ✓
- ■ Utilización de recursos — 103 ✓

---

## Página 10 — Pregunta 4 (selección múltiple)

**Consigna:** Seleccione ventajas de implementar una API REST con el protocolo HTTPS

**Imagen:** ilustración de una API KEY, un candado/llave y un escudo con engranaje (alusión a seguridad de la comunicación).

**Opciones:**
- ▲ (rojo) Confidencialidad
- ◆ (azul) Integridad
- ● (amarillo) Accesibilidad
- ■ (verde) Interoperabilidad

---

## Página 11 — Resultado Pregunta 4

**Respuestas correctas:** ▲ (rojo) **Confidencialidad** ✓ · ◆ (azul) **Integridad** ✓ · ■ (verde) **Interoperabilidad** ✓

**Conteo de votos:**
- ▲ Confidencialidad — 131 ✓
- ◆ Integridad — 113 ✓
- ● Accesibilidad — 42 ✗
- ■ Interoperabilidad — 53 ✓

---

## Página 12 — Diapositiva: Cualidades de diseño

**Título:** Cualidades de diseño

**Imagen:** dos personas frente a monitores con software CAD y un modelo físico de un auto sobre la mesa (alusión a diseño e ingeniería).

*(Diapositiva de sección — abre el bloque de cualidades de diseño.)*

---

## Página 13 — Pregunta 5 (selección múltiple)

**Consigna:** Cuales de estas características son deseadas en un diseño

**Opciones:**
- ▲ (rojo) Alta cohesión
- ◆ (azul) Bajo acoplamiento
- ● (amarillo) Alto acoplamiento
- ■ (verde) Baja cohesión

---

## Página 14 — Resultado Pregunta 5

**Respuestas correctas:** ▲ (rojo) **Alta cohesión** ✓ · ◆ (azul) **Bajo acoplamiento** ✓

**Conteo de votos:**
- ▲ Alta cohesión — 118 ✓
- ◆ Bajo acoplamiento — 118 ✓
- ● Alto acoplamiento — 24 ✗
- ■ Baja cohesión — 22 ✗

---

## Página 15 — Pregunta 6 (verdadero/falso)

**Consigna:** El uso de polimorfismo en programación orientada a objetos siempre implica mayor flexibilidad.

**Imagen:** fotograma de claymation (una oveja estilo *Shaun the Sheep* hablando por un teléfono de tubo).

**Opciones:**
- ◆ (azul) Verdadero
- ▲ (rojo) Falso

---

## Página 16 — Resultado Pregunta 6

**Respuesta correcta:** ▲ (rojo) **Falso** ✓

**Conteo de votos:**
- ◆ Verdadero — 68 ✗
- ▲ Falso — 72 ✓

---

## Página 17 — Pregunta 7 (verdadero/falso)

**Consigna:** Un diseño altamente acoplado es más mantenible ya que posee mayor simplicidad

**Opciones:**
- ◆ (azul) Verdadero
- ▲ (rojo) Falso

---

## Página 18 — Resultado Pregunta 7

**Respuesta correcta:** ▲ (rojo) **Falso** ✓

**Conteo de votos:**
- ◆ Verdadero — 23 ✗
- ▲ Falso — 118 ✓

---

## Página 19 — Pregunta 8 (selección múltiple)

**Consigna:** ¿Qué principios de diseño se están ignorando?

**Caso:** Para diseñar el sistema, un colega propone un enfoque altamente modular utilizando Generics que leyó en un artículo, agregando múltiples niveles de abstracción y configuraciones genéricas "por si en el futuro se necesitan".

**Opciones:**
- ▲ (rojo) REST
- ◆ (azul) YAGNI
- ● (amarillo) KISS
- ■ (verde) HTTP

---

## Página 20 — Resultado Pregunta 8

**Respuestas correctas:** ◆ (azul) **YAGNI** ✓ · ● (amarillo) **KISS** ✓

**Conteo de votos:**
- ▲ REST — 23 ✗
- ◆ YAGNI — 104 ✓
- ● KISS — 101 ✓
- ■ HTTP — 18 ✗

---

## Página 21 — Diapositiva: Patrones de diseño (Intro)

**Título:** Patrones de diseño (Intro)

*(Diapositiva de sección — abre el bloque de patrones de diseño.)*

---

## Página 22 — Pregunta 9 (selección simple)

**Consigna:** Un patrón de diseño debe usarse…

**Opciones:**
- ▲ (rojo) Siempre que se pueda
- ◆ (azul) Lo menor posible
- ● (amarillo) Depende del tipo (estructural/comportamiento/creacional)
- ■ (verde) Únicamente cuando se presenta el problema que resuelve

---

## Página 23 — Resultado Pregunta 9

**Respuesta correcta:** ■ (verde) **Únicamente cuando se presenta el problema que resuelve** ✓

**Conteo de votos:**
- ▲ Siempre que se pueda — 24 ✗
- ◆ Lo menor posible — 1 ✗
- ● Depende del tipo (estructural/comportamiento/creacional) — 35 ✗
- ■ Únicamente cuando se presenta el problema que resuelve — 77 ✓

---

## Página 24 — Pregunta 10 (selección múltiple)

**Consigna:** Los patrones de diseño favorecen

**Opciones:**
- ▲ (rojo) Bajo acoplamiento
- ◆ (azul) Mayor extensibilidad
- ● (amarillo) Mayor mantenibilidad
- ■ (verde) Reutilización de lógica

---

## Página 25 — Resultado Pregunta 10

**Respuestas correctas:** las cuatro opciones ✓ (▲ Bajo acoplamiento · ◆ Mayor extensibilidad · ● Mayor mantenibilidad · ■ Reutilización de lógica)

**Conteo de votos:**
- ▲ Bajo acoplamiento — 85 ✓
- ◆ Mayor extensibilidad — 82 ✓
- ● Mayor mantenibilidad — 125 ✓
- ■ Reutilización de lógica — 121 ✓

---

## Página 26 — Podio final

**Título del Kahoot:** Repaso atributos de calidad, cualidades, patrones

**Podio:**
1. **Santiago Torres** — 8823
2. **Ignacio J** — 8681
3. *(nombre tapado por el avatar/confeti — no legible; ver Notas de conversión)*

**Finalistas:**
4. Uriel M… *(truncado por la UI de Kahoot)*
5. El desl… *(truncado por la UI de Kahoot)*

---

**FIN DEL ARCHIVO FUENTE — Kahoot: Repaso atributos de calidad, cualidades, patrones**
