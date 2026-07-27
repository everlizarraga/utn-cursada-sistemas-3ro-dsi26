# 🌱 Proyecto 0 (clase 04) — API REST en Capas, Incremental

> **Premisa:** vas a construir una API REST **desde cero**, agregando UNA pieza del framework por vez. Cada etapa: construís algo mínimo, **predecís qué va a pasar**, lo corrés, lo comprobás **con tus ojos**, entendés por qué existe, y recién ahí avanzás.
>
> **La regla de oro (la de siempre):** nada se explica antes de que tengas todo lo necesario para entenderlo. Si algo más avanzado se menciona, es para que no te sorprenda — no para que cargues con ello.
>
> **La meta, con tus palabras:** que al terminar tengas la capacidad de hacer **vos** tus endpoints, tus 4 capas y todo el scope de la clase. No "seguir una guía": poder sin ella.

---

## 🧭 Filosofía (la que te funcionó en los proyectos 0 y 1 — más cuatro mejoras)

**Lo heredado, intacto:**
- Cada etapa = un cambio incremental al mismo proyecto. **Una sola pieza nueva por etapa.**
- **Construís → predecís → corrés → observás → entendés → avanzás.** La teoría llega DESPUÉS de la evidencia.
- **Vos tipeás todo.** Instrucciones y preguntas sí; código para copy-paste a ciegas, jamás.
- **Experimentos de contraste** en cada etapa: sacar la anotación, pedir lo que no existe, **romper a propósito y LEER el error** — el stacktrace es contenido, no ruido.
- **👀 Variantes al pie, cortitas, solo si suman:** cuando algo tiene más de una forma común en el mundo real, te la muestro en 1-3 líneas para que la reconozcas cuando te la cruces — la que producís es siempre UNA, la canónica, y te digo cuál y por qué.
- **🔗 Conexión por etapa:** cada etapa cierra comparando tu pieza contra la equivalente del `sales-service` de la clase, y señalando qué sección del recorrido releer — que ahora debería leerse distinto.
- Criterios de "etapa completa" (observables) separados del checkpoint (comprensión) · 🆘 problemas comunes · tiempo estimado · nada se descarta, todo se reusa.

**Las mejoras nuevas:**
1. **Andamiaje que se desvanece:** las primeras etapas te llevan de la mano (🟦); las del medio te dan el qué y vos diseñás el cómo (🟨); la última te da solo requisitos (🟥). El destete es el diseño, no un accidente.
2. **Capstone de vuelo solo (E10):** un segundo recurso completo, sin pasos, solo requisitos + batería de aceptación. Ahí se verifica la meta de verdad.
3. **Sondeo de reactivación (E0):** lo tuyo del proyecto-1 (beans, DI, config) no se re-enseña — pero tampoco se asume a ciegas: se sondea en 20 minutos antes de construir encima.
4. **Registro por etapa:** al cerrar cada etapa anotás en una línea qué te sorprendió o costó. Ese registro alimenta el complemento del recorrido al final.
5. **Checkpoint doble:** preguntas de recall (¿qué hace X?) + preguntas estilo cátedra (decidí y justificá).

---

## 🎯 Qué vas a PODER HACER al terminar

- Exponer endpoints REST propios con todos los verbos (GET, POST, PUT, PATCH, DELETE) — sabiendo qué hace **cada** anotación, no recitándolas.
- Cablear las 4 capas (controller → service → dominio → repositorio) con inyección por constructor, entendiendo quién construye qué.
- Diseñar la frontera de datos con DTOs y justificar qué entra, qué sale y qué jamás.
- Convertir errores en respuestas HTTP correctas con excepciones propias + manejador global.
- **Leer errores de Spring sin pánico** — saber dónde mirar en el stacktrace.
- Leer el `sales-service` de la clase (y código Spring ajeno) como repaso, no como jeroglífico.

## 📦 Stack

| Tool | Versión | Notas |
|---|---|---|
| Java | 21 | la de la cátedra |
| Maven + IntelliJ IDEA | los tuyos | ya instalados |
| Spring Boot | **4.x** (línea de la cátedra — el profe en 4.0.5) | starter: **`spring-boot-starter-webmvc`** (+ Lombok) |
| Postman | el tuyo | tu par de ojos sobre la API |
| JUnit 5 + AssertJ | última | recién en el bonus |

> **Lo que ya construiste y se reusa:** todo el Java del proyecto-0 (clases, Optional, streams, excepciones, tests) y todo el Spring-núcleo del proyecto-1 (beans, inyección, `@Configuration`/`@Bean`, config externa, `CommandLineRunner`). **Este proyecto es la capa que te falta: HTTP entrante.** En el proyecto-1 viste requests SALIR de tu app hacia una API ajena; acá las vas a ver ENTRAR a la tuya. Es el espejo exacto. Y el recorrido de la clase 04 es tu mapa conceptual: cada etapa le pone manos a una parte que ya leíste.

> **Dos vías de arranque (elegís en la Etapa 0):** **Vía A** — proyecto independiente vía Initializr · **Vía B** — módulo dentro de tu monorepo de cursada (la tuya). Solo difieren en la creación y el comando de arranque; de la Etapa 1 en adelante son idénticas.

## 🐾 Dominio: Veterinaria

**Propietarios** y sus **mascotas**. Regla de negocio única: *un propietario no puede tener más de 5 mascotas*. Relación en un solo sentido (Propietario conoce sus mascotas; Mascota no conoce a su propietario — mismo diseño, mismo precio que en la clase).

¿Por qué no ventas, como la cátedra? Porque copiar el dominio te dejaría transcribir sin pensar. Veterinaria es **distinto para obligarte a decidir, y paralelo para poder comparar**: tu `MascotaController` contra su `ProductoController`, pieza por pieza, en cada 🔗.

## 🗺️ Mapa de etapas

> El mapa sirve, **el terreno manda**: si una etapa resulta densa, se parte; si algo fluye, se junta. Cada etapa se genera como su propio `.md` cuando llegás — **no las generamos todas de una**.

| # | Pieza nueva | Qué construís y qué COMPROBÁS con tus ojos | Andamiaje | ⏱️ |
|---|---|---|---|---|
| **0** | — (sondeo) | Proyecto Spring nuevo + **auto-test de reactivación**: un bean y una inyección, de memoria, sin mirar. Fluye → seguimos; oxidado → refresco exprés antes de construir encima. | 🟦 | 30' |
| **1** | `@RestController` + `@GetMapping` | Tu primer endpoint. **Ver una request ENTRAR** a tu app (el espejo del proyecto-1) y a Tomcat quedarse vivo esperando más. Qué hace de verdad cada anotación. | 🟦 | 40' |
| **2** | `@PathVariable` | Rutas con partes variables (`/mascotas/{id}`). Experimento: dos rutas que compiten. | 🟦 | 30' |
| **3** | `@RequestBody` + records | La frontera de datos: JSON que entra → DTO. Tu primer POST. Por qué la entidad no cruza. | 🟦 | 45' |
| **4** | `@Service` | El orquestador en el medio: el controller deja de pensar. Acá tu DI del proyecto-1 se reusa tal cual — y comprobás que ya la sabías. | 🟦 | 40' |
| **5** | `@Repository` + `Optional` end-to-end | La cadena completa de 4 capas, recorrida por una request de punta a punta. El dominio (dado) entra en escena con su regla. | 🟨 | 50' |
| **6** | Excepciones propias + `@RestControllerAdvice` | Primero ROMPERLO: ver el 500 feo y **leer el stacktrace entero**. Después curarlo: 404/400 decentes con error uniforme. | 🟨 | 50' |
| **7** | `@PutMapping` + `@DeleteMapping` | Modificación y baja **canónicas** (id en la ruta) + `@ResponseStatus`. | 🟨 | 40' |
| **8** | `@PatchMapping` *(extensión pedida)* | Actualización parcial y SU decisión de diseño (campo ausente = no tocar). | 🟨 | 40' |
| **9** | Seeder + **examen** | `CommandLineRunner` (¡reuso directo del proyecto-1!) sembrando datos + la batería de 9 verificaciones, corrida completa. | 🟨 | 40' |
| **10** | **CAPSTONE** 🟥 | Un **segundo recurso completo** (te doy solo requisitos + batería de aceptación — cero pasos). Si sale, la meta está cumplida. | 🟥 | 60-90' |
| **B** | `MockMvc` *(bonus opcional)* | Automatizar dos filas del examen sin abrir Postman. | 🟨 | 40' |

**Governor honesto:** esto es más que las 1-2 sesiones que la skill vigente permite — estimo **3-4 sesiones**. Queda registrado como excepción acordada (la discusión de fondo va a tu chat del sistema). Cortes sugeridos: E0-E3 / E4-E6 / E7-E9 / E10+B.

## 🔄 Cómo trabajamos

1. Decís **"arranquemos etapa N"** → genero SOLO esa etapa.
2. La hacés tipeando vos, prediciendo antes de cada corrida, y anotando tu línea de registro al cerrar.
3. Trabas y sorpresas → al chat (sin código terminado de mi lado, salvo inevitable).
4. **"Listo, etapa N+1"** → seguimos. El recorrido y el `sales-service` quedan de material de consulta permanente.

## ▶️ Próximo paso

Decime **"arranquemos etapa 0"** y sale el material: proyecto nuevo + el sondeo de reactivación. Si el sondeo te sale fluido, en una hora ya tenés tu primer endpoint respondiendo.

---

**FIN DEL ROADMAP — Proyecto 0 (clase 04): API REST en Capas**
