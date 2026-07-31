# 🌱 Proyecto 0 (clase 04) — Etapa 8: PATCH — el arte de tocar solo lo que vino

> **⭐ Extensión pedida por vos** — el proyecto de la clase no usa PATCH. Lo cubrís por decisión propia: completar el set de verbos y llegar al TP con ventaja.
>
> **Objetivo:** la actualización **parcial** — cambiar solo lo que el cliente mandó, dejar intacto lo que no.
>
> **Cómo trabaja esta etapa (v2):** el diseño completo primero — la semántica, la regla de implementación **y su límite conocido, anunciado de entrada** (nada de sorpresas a mitad de camino). Los experimentos después **verifican** lo que ya sabés, no te lo revelan.
>
> **Pre-requisito:** Etapa 7 completa (la mutación es la base de todo lo de hoy).
>
> **Tiempo estimado:** 40-50 minutos.

---

## 🗺️ El mapa general — dónde estamos parados

```
              REQUEST                               RESPONSE
                 │                                      ▲
                 ▼                                      │
        ┌─────────────────┐                      ┌──────────────┐
        │   CONTROLLERS   │ ★ +PATCH en ambos    │  Advice      │
        └────────┬────────┘                      │ (sin cambios)│
                 ▼                               └──────────────┘
        ┌─────────────────┐
        │    SERVICES     │ ★ +patch(id, request) en ambos
        └───┬─────────┬───┘
            ▼         ▼
   ┌────────────┐ ┌──────────────┐        dtos/ ★ +2 PatchRequest
   │  DOMINIO   │ │ REPOSITORIES │
   │(sin cambios│ │ (sin cambios)│
   │ — los      │ └──────────────┘
   │ setters de │
   │ la E7 ya   │
   │ alcanzan)  │
   └────────────┘
```

Movimiento total de la etapa: **2 DTOs nuevos, 1 método nuevo por service, 1 endpoint nuevo por controller.** Dominio, repos, advice: intactos.

## 🗂️ Los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── controllers/                             (SE TOCAN: +@PatchMapping cada uno)
    ├── dtos/
    │   ├── propietario/PropietarioPatchRequest.java  ★ NUEVO
    │   └── mascota/MascotaPatchRequest.java          ★ NUEVO — tu turno
    └── services/ (+ impl/)                      (SE TOCAN: +patch)
```

## 🧭 Mapa de esta etapa

1. **EL DISEÑO COMPLETO primero:** semántica, regla, y el límite conocido.
2. El DTO — y la regla de los wrappers.
3. La implementación: mutación condicional.
4. Experimento 1: la prueba de que no es un PUT disfrazado.
5. Experimento 2: el body vacío — tu política.
6. Experimento 3: ausente ≠ inválido.
7. Experimento 4: la verificación del límite (el null imposible).
8. Tu turno: Mascota completa.
9. Criterios + checkpoint + registro + 🔗.

---

## 🧠 Parte 1: EL DISEÑO COMPLETO (leé toda esta parte antes de codear)

### 1a. La semántica — PUT vs PATCH, cara a cara

```
Ana hoy:  { nombre: "Ana María", telefono: "11-9999" }

PUT   /propietarios/1   {"nombre":"Ana"}
  → nombre: "Ana", telefono: null        ← REEMPLAZO: lo que no vino, no queda.

PATCH /propietarios/1   {"nombre":"Ana"}
  → nombre: "Ana", telefono: "11-9999"   ← PARCIAL: lo que no vino, NO SE TOCA.
```

El PUT exige el estado completo; el PATCH acepta **deltas**. Para el cliente que "solo quería cambiar el nombre" (la incomodidad con la que cerró la Etapa 7), PATCH es el verbo honesto.

### 1b. La regla de implementación

La pregunta que lo define todo: **¿cómo sabe tu código si un campo "vino"?** La respuesta la trajiste de la Etapa 3: en un record, la clave ausente del JSON llega como **`null`** (si el componente es wrapper). De ahí la regla:

> **`null` = no vino = no tocar. Campo presente = validar y tocar.**

Y el algoritmo por campo, que es toda la implementación:

```
        por cada campo del PatchRequest:
                    │
              ¿es null?
              ┌─────┴─────┐
             SÍ           NO
              │            │
        no se mira    ¿es válido? ──NO──► BusinessException (400)
        no se toca         │
        (la instancia     SÍ
         viva ya tiene     │
         el valor)     setear en la instancia viva (mutación — Etapa 7)
```

### 1c. El límite conocido — anunciado ANTES de escribir una línea

Este diseño tiene un precio, y lo vas a conocer ahora, no descubrirlo a mitad de camino:

> **Con "null = no tocar", PATCH no puede setear un campo EN null.** Si un cliente quisiera *borrar* el teléfono de Ana mandando `{"telefono": null}`, tu código no puede distinguirlo de `{}` (clave ausente): para Jackson, ambos producen exactamente el mismo record con `telefono = null`. Las dos intenciones — *"no opino"* y *"borralo"* — **colapsan en el mismo null**. Se llama **conflación del null**.

¿Y por qué aceptamos ese precio? Porque para este dominio (¿cuán seguido se "borra" un teléfono?) es bajo, y la simplicidad lo vale. **Se paga a sabiendas** — el Experimento 4 te lo hace verificar con tus manos, para que el límite sea vivido y no solo leído.

👀 *Cuando el precio NO es bajo, el mundo tiene soluciones — solo reconocerlas: el estándar **JSON Merge Patch** (`null` explícito = "borrá"; ausencia = "no toques" — exige distinguirlos al deserializar) y tipos como **`JsonNullable`** (envuelven el campo en tres estados: ausente / con-valor / presente-null). Si un requerimiento te exige borrar-vía-PATCH, esos son los nombres a googlear.*

### 1d. Inventario exacto

| Pieza | Qué pasa |
|---|---|
| `PropietarioPatchRequest` / `MascotaPatchRequest` | nuevos — **todo-wrapper** (regla en Parte 2) |
| Services | `+patch(id, request)`: mutación condicional (el algoritmo de 1b) |
| Controllers | `+@PatchMapping("/{id}")` — clan de siempre, ficha 🎛️ de la Etapa 3 |
| Dominio, repos, advice | **nada** |

---

## 📦 Parte 2: El DTO — y la regla de los wrappers

```java
// 📁 src/main/java/.../veterinaria/dtos/propietario/PropietarioPatchRequest.java   (NUEVO)
package com.practica.veterinaria.dtos.propietario;

public record PropietarioPatchRequest(String nombre, String telefono) { }
// ↑ Mismos campos que el UpdateRequest — pero acá TODOS son opcionales por
//   contrato: null = no vino. DTO separado aunque parezca gemelo: el
//   CONTRATO es otro (un PATCH vacío puede ser válido; un PUT sin nombre, no).
```

La regla que tu propio hallazgo de la Etapa 3 dejó lista para cobrar:

> **En un DTO de PATCH, TODO campo es wrapper — jamás primitivo.** Vos lo demostraste: en tu stack, un `int` en un record **rechaza la ausencia** (el "required implícito" que descubriste — Jackson no puede meter null en un primitivo del constructor). Y la ausencia es exactamente lo que PATCH necesita permitir en cada campo. Un primitivo en un PatchRequest es una contradicción: *"campo opcional que no se puede omitir"*. Números en PATCH: `Integer`, `Double` — siempre.

## 🛠️ Parte 3: La implementación — mutación condicional

📍 **Dónde estamos:**

```
Controllers ──► Services ◄━━ ACÁ ──► Repositories
                   │
                   ▼
                Dominio (sus setters de la Etapa 7 hacen el trabajo)
```

```
┌─ 📁 services/impl/PropietarioServiceImpl.java ─────────────────┐
│  ...métodos existentes: sin cambios...                         │
│  ╔═══════════════════════════════════════════════════╗         │
│  ║ @Override                                         ║ ◄──     │
│  ║ public PropietarioResponse patch(Long id,         ║ método  │
│  ║         PropietarioPatchRequest request) {        ║ NUEVO   │
│  ║   if (request == null) {                          ║ entero  │
│  ║     throw new BusinessException(                  ║         │
│  ║         "El body es obligatorio");                ║         │
│  ║   }                                               ║         │
│  ║   Propietario existente =                         ║         │
│  ║       getPropietarioOrThrow(id);  // viva, o 404  ║         │
│  ║                                                   ║         │
│  ║   if (request.nombre() != null) {      // ¿vino?  ║         │
│  ║     if (request.nombre().isBlank()) {  // válido? ║         │
│  ║       throw new BusinessException(                ║         │
│  ║         "El nombre no puede quedar vacío");       ║         │
│  ║     }                                             ║         │
│  ║     existente.setNombre(request.nombre().trim()); ║         │
│  ║   }                       // ¿no vino? ni se mira ║         │
│  ║                                                   ║         │
│  ║   if (request.telefono() != null) {               ║         │
│  ║     existente.setTelefono(request.telefono());    ║         │
│  ║   }                                               ║         │
│  ║                                                   ║         │
│  ║   propietarioRepository.save(existente);          ║         │
│  ║   return toResponse(existente);                   ║         │
│  ║ }                                                 ║         │
│  ╚═══════════════════════════════════════════════════╝         │
└────────────────────────────────────────────────────────────────┘
   (+ la firma en la interfaz — y fijate: es el algoritmo de 1b,
    línea por línea, con la validación ADENTRO del "¿vino?")
```

Y el endpoint en el controller: `@PatchMapping("/{id}")` con la misma firma que tu PUT, delegando a `patch`. Una línea, como siempre.

## 🧨 Parte 4: Experimento 1 — la prueba de que no es un PUT disfrazado

**Predicción:** con Ana en `{nombre:"Ana María", telefono:"11-9999"}`, mandá `PATCH /propietarios/1` con **solo** `{"nombre":"Ana"}`. ¿El teléfono?

→ **200**, y el GET muestra el teléfono **intacto**. Esa supervivencia del campo no tocado ES la prueba. Contrastalo en vivo: mismo body por el **PUT** → teléfono `null`. Mismo body, dos verbos, dos contratos — vividos los dos.

*(Bonus de finura: tu PATCH así implementado es **idempotente** — repetirlo deja el mismo estado. Dato para presumir: la especificación HTTP se lo exige al PUT pero NO al PATCH; el tuyo la tiene de regalo por diseño. Hay PATCHs legales en el mundo que no la tienen.)*

## 🧨 Parte 5: Experimento 2 — el body vacío: tu política

**Predicción:** `PATCH /propietarios/1` con `{}` — ¿400? ¿200? ¿Qué cambia?

→ **200, cero cambios**: ningún `if` entró. Un no-op educado. ¿Tiene sentido? Dos posturas reales:

```
  POSTURA A — aceptarlo                POSTURA B — rechazarlo (400)
  "pedir nada y recibir nada           "un PATCH vacío es casi seguro
   es coherente; no-op válido"          un bug del cliente: avisale"
```

Tu política, tu decisión, **comentario de justificación** al lado. (Lo evaluable, como siempre, es el porqué.)

## 🧨 Parte 6: Experimento 3 — ausente ≠ inválido

**Predicción:** `PATCH /propietarios/1` con `{"nombre":""}` — ¿no-op como el vacío, o rechazo?

→ **400**, *"El nombre no puede quedar vacío"*. La distinción de la Etapa 3 en su forma final: **ausente** = no opinar (respetado) · **presente pero inválido** = opinar mal (rechazado). Por eso la validación vive *adentro* del "¿vino?", nunca afuera — mirá tu propio código de la Parte 3.

## 🧨 Parte 7: Experimento 4 — la verificación del límite

El límite de 1c, ahora con tus manos. **Predicción (ya sabés la respuesta — el punto es verla):** `PATCH /propietarios/1` con `{"telefono": null}` — ¿borra el teléfono?

→ **200… y el teléfono sigue ahí.** No-op, exactamente como 1c anunció: `{"telefono": null}` y `{}` producen el mismo record, tu regla no puede distinguir *"no opino"* de *"borralo"*. Conflación del null, verificada. Saber que tu diseño tiene este límite — y poder **nombrarlo, explicarlo y citar las salidas** (Merge Patch, JsonNullable) — es la madurez que la materia evalúa.

## ✍️ Parte 8: Tu turno — Mascota completa

`MascotaPatchRequest` + `patch` en el service + `@PatchMapping("/{id}")` — todo tuyo, con dos decisiones: *(a)* los campos del DTO — ¿`propietarioId` patcheable? **Coherencia con tu decisión del PUT** (Etapa 7: la mudanza de dueño) — sostenela, o justificá por qué un verbo la permite y el otro no; *(b)* tu política del body vacío, consistente con la de Propietario.

**Resultado esperado:** `PATCH /mascotas/1` `{"nombre":"Michi"}` → **200**, nombre nuevo, **especie intacta** (verificada por GET) · `{"especie":""}` → **400** · `{}` → según TU política documentada · `PATCH /mascotas/99` → **404** · si permitiste mudanza: la regla de las 5 explotando con **400** traducido cuando el dueño nuevo está lleno.

## ✅ Criterios de "Etapa 8 completa"

- [ ] Leíste la Parte 1 entera antes de codear — incluido el límite, ANTES de encontrarlo.
- [ ] PATCH de ambos recursos por mutación condicional, DTOs todo-wrapper.
- [ ] Los cuatro experimentos con predicción — el 4 como verificación, no como sorpresa.
- [ ] Podés nombrar la conflación del null, explicar por qué tu diseño la tiene, y citar las dos salidas.
- [ ] Política del body vacío: decidida, implementada, justificada, consistente entre recursos.
- [ ] Coherencia PUT/PATCH sobre la mudanza: sostenida o justificadamente rota.

## ✅ Checkpoint

*Recall:*
1. Recitá el algoritmo por campo (el diagrama de 1b) — ¿dónde vive la validación y por qué ahí?
2. ¿Por qué el DTO de PATCH es todo-wrapper? ¿Qué pasaría con un `int`, en tu stack, y qué contradice?
3. ¿Qué dos intenciones colapsa la conflación del null, y en qué comparación exacta de tu código ocurre?

*Decidí y justificá:*
4. Guía para tu equipo de TP en tres renglones: ¿cuándo exponen PUT, cuándo PATCH, cuándo ambos?
5. Defendé la política de body-vacío que NO elegiste — su mejor versión, y qué cliente la preferiría.
6. Un compañero implementó PATCH reusando el método del PUT "porque es casi igual". ¿Qué se rompe primero, y con qué caso de prueba lo demolés en 30 segundos?
7. Requerimiento nuevo: *"debe poder eliminarse el teléfono de un propietario"*. Tres caminos posibles con tu API actual (¿solo PATCH puede resolverlo?), costos de cada uno, y cuál proponés.

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Hoy la conexión es **inversa y con trofeo**: buscá `Patch` en todo el `sales-service` — **cero resultados**. La cátedra no lo cubre; vos lo diseñaste, lo implementaste y conocés su trade-off central con nombre. El día que en el TP alguien proponga "un endpoint para editar solo el precio", tenés el verbo, la regla de wrappers, la política del vacío y el límite del null listos — con justificación, la moneda de la materia. Y la ausencia también enseña: se puede lanzar una API digna solo con PUT — PATCH es refinamiento, no obligación.

## ▶️ Próximo paso

Mirá tu rutina de cada arranque: la app nace **vacía** (la memoria efímera de la Etapa 5 — `Ctrl+C` y Ana muere), y venís pagando el peaje de recargar a mano antes de cada prueba. **"Arranquemos etapa 9"** → la **semilla** (con el `CommandLineRunner` que ya construiste en tu proyecto-1 — reuso directo) y el **examen final** de la API completa.

---

**FIN DE LA ETAPA 8 — v2**
