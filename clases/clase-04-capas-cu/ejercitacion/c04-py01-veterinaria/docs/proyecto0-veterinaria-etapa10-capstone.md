# 🌱 Proyecto 0 (clase 04) — Etapa 10: CAPSTONE — El vuelo solo

> **Objetivo:** un **tercer recurso completo**, de punta a punta, **sin pasos, sin ejemplos, sin hints precargados**. Solo requisitos, decisiones a documentar, y una batería de aceptación. Si la batería da verde y tus decisiones tienen porqué — la meta que declaraste al arrancar ("poder hacer YO mis endpoints y mis capas") está **cumplida y demostrada**.
>
> **Pre-requisito:** Etapa 9 con su 10/10.
>
> **Tiempo estimado:** 60-90 minutos. Sin apuro: no es una carrera, es una demostración.
>
> **Andamiaje:** 🟥 — ninguno. Ese es el punto. (Por eso esta etapa no tiene 📍 ni códigos: el mapa lo dibujás vos.)

---

## 🗺️ El mapa general — lo que vas a construir

```
   LO QUE YA EXISTE (intocable salvo             LO QUE CONSTRUÍS HOY
   lo que TUS decisiones exijan)                 (la columna entera, solo)
   ─────────────────────────────                 ────────────────────────
   PropietarioController  MascotaController      ★ TurnoController
          │                     │                       │
   PropietarioService     MascotaService         ★ TurnoService (+Impl)
          │                     │                       │
   PropietarioRepository  MascotaRepository      ★ TurnoRepository (+InMemory)
          │                     │                       │
      Propietario ◄──────── Mascota ◄─── ??? ──► ★ Turno (entidad)
                                          │
                              la relación con Mascota:
                              SU DIRECCIÓN ES TU DECISIÓN D1

   + ★ DTOs propios (create/response/update/patch)    + advice: SIN cambios
   + ★ semilla extendida (1 turno)                      (salvo que tu D4 pida
                                                         una excepción nueva)
```

## ✈️ Las reglas del vuelo

- **Podés consultar tu propio código** (Propietario, Mascota, advice, semilla) — es tu codebase; en el mundo real siempre está. NO es trampa: así se trabaja.
- **No releas las etapas ni el recorrido durante el intento.** Si necesitás volver al material, no es fracaso — es dato: anotá **qué** fuiste a buscar (te dice qué no decantó) y seguí.
- **Trabado en serio (+30 min en un mismo punto) → chat**, protocolo de siempre: hints escalonados, jamás la solución.
- **Cada decisión de diseño lleva su comentario de justificación** (2-3 líneas) en el código. La batería verifica el *qué*; los comentarios demuestran el *porqué* — la moneda de la materia.

---

## 📋 El recurso: Turno

*Poético: `Turno` fue tu juguete de la Etapa 3 (el POST del patio, cuando recién aprendías `@RequestBody`). Hoy vuelve como recurso de verdad — y el juguete del museo queda de recuerdo de cuánto pasó en el medio.*

### Requisitos funcionales

1. **Entidad `Turno`**: `id`, `fecha` (String, formato libre tipo `"2026-08-15"` — sin tipos de fecha: el foco no es ese), `motivo` (String), y **una relación con `Mascota`**: todo turno es *de* una mascota, siempre.
2. **API REST completa** bajo `/veterinaria/turnos`, canónica en todo lo que ya sabés: `GET` (todos), `GET /{id}`, `POST`, `PUT /{id}`, `PATCH /{id}`, `DELETE /{id}` — códigos de estado correctos, DTOs propios por interacción (la entidad jamás cruza la frontera), errores saliendo por tu `ErrorResponse` uniforme.
3. **Regla de negocio**: *una mascota no puede tener más de **3 turnos***. El cuarto → **400** con mensaje claro.
4. **Validaciones de flujo**: `fecha` y `motivo` obligatorios y no-blancos al crear; referencias inexistentes → **404** con mensaje específico.
5. **Semilla extendida**: el `DataInitializer` siembra además **1 turno** para una de las mascotas sembradas (tus tres reglas de la Etapa 9 siguen valiendo).
6. **Consistencia al borrar**: borrar un turno deja el sistema consistente; y **borrar una mascota con turnos** hace lo que TU decisión D4 documente.
7. **Coherencia de políticas**: tu política de PATCH-body-vacío y la regla de todo-wrapper aplican acá igual que en los otros recursos.

### Las cinco decisiones a documentar (comentario de justificación, sí o sí)

**D1 — La dirección de la relación.** Las tres formas posibles — el costo de cada flecha ya lo pagaste en algún momento del proyecto; reconocelo:

```
  OPCIÓN 1                    OPCIÓN 2                    OPCIÓN 3
  Turno ──► Mascota           Mascota ──► [turnos]        ambas flechas
  (el turno conoce            (la mascota tiene su
   a su mascota)               lista, como
                               Propietario→Mascota)

  ¿qué se vuelve trivial      ¿qué se vuelve trivial      ¿qué ganás…
   y qué se encarece?          y qué se encarece?          y qué mantenés
  (pensá: toResponse,         (pensá: dónde vive           sincronizado
   contar los 3,               la regla, el                a mano para
   huérfanos al borrar         findOwner O(n) que          siempre?)
   mascota)                    ya escribiste una vez)
```

Elegí una y bancala — no hay correcta única; hay coherencias mejor y peor defendidas.

**D2 — La casa de la regla de los 3 turnos.** ¿Entidad o service? Ojo: tu respuesta puede depender de D1 (¿dónde está disponible la información para contar?). Si te queda en el service, justificá por qué no viola el "las reglas de negocio van en el dominio" — o reconocé honestamente la tensión: eso también es diseño.

**D3 — El PUT y la mudanza.** ¿Un PUT puede cambiar el turno de mascota? Coherente con tu decisión de la Etapa 7 (la mudanza de dueño) — o justificadamente distinto.

**D4 — Los huérfanos, tercera ronda.** Mascota borrada con turnos pendientes: ¿cascada, prohibición, u otra cosa? Coherente (o no, con porqué) con tu decisión de propietarios-con-mascotas.

**D5 — El PATCH.** Qué campos son patcheables, y tu política del body vacío, sostenida.

## 🏁 La batería de aceptación

App recién levantada, corrida en orden:

| # | Verificación | Esperado |
|---|---|---|
| 1 | `GET /turnos` | 200 · el turno sembrado, con su `mascotaId` |
| 2 | `POST /turnos` válido | **201** · id nuevo |
| 3 | `POST /turnos` con `mascotaId: 99` | **404** · mensaje específico |
| 4 | `POST /turnos` sin `motivo` | **400** |
| 5 | Turnos 2 y 3 para la misma mascota → el **cuarto** | 201, 201 → **400** · mensaje de la regla |
| 6 | `GET /turnos/99` | **404** · `ErrorResponse` uniforme |
| 7 | `PUT /turnos/1` completo, ×2 seguidas | 200 ×2 · mismo estado (idempotencia) |
| 8 | `PATCH /turnos/1` solo `motivo` | 200 · `fecha` **intacta** |
| 9 | `PATCH /turnos/1` body `{}` | según **TU política**, tal como está documentada |
| 10 | `DELETE /turnos/1` → `GET /turnos/1` | **204** → **404** |
| 11 | `DELETE` de una mascota **con** turnos | exactamente lo que **D4** documenta |
| 12 | **Regresión:** el examen de la Etapa 9, re-corrido | **10/10 sigue verde** — lo nuevo no rompió lo viejo |

**12/12 + cinco comentarios de justificación = proyecto-0 completado.** No terminaste una guía: construiste una API en capas, tres veces, la última sin red.

## 🎤 La defensa (simulacro — por escrito, 15 minutos, sin mirar nada)

Como la instancia oral del parcial:

1. Narrá el viaje completo de `POST /veterinaria/turnos` con un body válido: cada clase que interviene, en orden, qué hace y qué NO hace — hasta el 201.
2. Mañana llega la base de datos real. Recorré tu proyecto y marcá qué archivos cambian. Y la trampa: ¿tu respuesta depende de D1-D5? ¿Cuál de tus decisiones envejece mejor con una BD?
3. Defendé tu D1 contra la opción que descartaste — y después **cambiá de bando** y atacá tu propia elección con el mejor argumento del contrario. (Si podés hacer las dos cosas, entendés el trade-off; si solo podés defender la tuya, la elegiste por inercia.)

## ✅ Cierre del proyecto

- [ ] Batería 12/12 + regresión verde.
- [ ] D1-D5 documentadas en el código con su porqué.
- [ ] La defensa escrita, honesta, sin mirar.
- [ ] **El registro final:** juntá tus líneas de registro de las once etapas (0-10) — más lo anotado de "fui a buscar X al material" — y traelas al chat. Ese paquete alimenta el complemento del recorrido, la próxima parada del flujo grande.

## ▶️ Después del vuelo

**Bonus opcional (si quedó nafta):** el examen de la Etapa 9 y esta batería, ¿a mano cada vez? El proyecto de la clase te mostró la alternativa — decime **"vamos con el bonus"** y automatizás dos filas con `MockMvc` (`@SpringBootTest`), calcando la estructura de su `SalesServiceApiIntegrationTest`. Ahí cobra sentido retroactivo tu `isEmpty` del seeder (la pregunta 6 de la Etapa 9: los tests levantan la app… y la semilla corre).

**El flujo grande, recordatorio:** proyecto-0 ✓ → checkpoints del recorrido por escrito (van a salir solos: tenés las manos entrenadas) → dudas al chat → **complemento del recorrido** (Parte A: todas las perlas de este viaje, destiladas; Parte B: las respuestas modelo) → y recién ahí la cadena documental de la clase 04.

---

**FIN DE LA ETAPA 10 — v2. Y del andamiaje. Lo que sigue lo construís vos.**
