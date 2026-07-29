# 🌱 Proyecto 0 (clase 04) — Etapa 8: PATCH — el arte de tocar solo lo que vino

> **⭐ Extensión pedida por vos** — el proyecto de la clase no usa PATCH. Lo cubrís por decisión propia: completar el set de verbos y llegar al TP con ventaja.
>
> **Objetivo:** la actualización **parcial**: cambiar solo lo que el cliente mandó, dejar intacto lo que no — con la decisión de diseño más fina del proyecto adentro: **¿qué significa exactamente que un campo "no haya venido"?** (Tu Etapa 3 tenía la mitad de la respuesta; hoy la cobrás entera.)
>
> **El momento clave:** cuando intentes *borrar* el teléfono de Ana vía PATCH… y descubras que no podés — y entiendas por qué esa imposibilidad es **el precio conocido y nombrado** del diseño que elegiste.
>
> **Pre-requisito:** Etapa 7 completa (la mutación es la base de todo lo de hoy).
>
> **Tiempo estimado:** 40-50 minutos.
>
> **Andamiaje:** 🟨 pleno — Propietario con guía mínima; Mascota entera tuya.

---

## 🗂️ Dónde estamos — los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── controllers/
    │   ├── PropietarioController.java              (SE TOCA: +PATCH)
    │   └── MascotaController.java                  (SE TOCA — tu turno)
    ├── dtos/
    │   ├── propietario/PropietarioPatchRequest.java   (NUEVO)
    │   └── mascota/MascotaPatchRequest.java           (NUEVO — tu turno)
    └── services/ (+ impl/)                         (SE TOCAN: +patch)
```

---

## 🧭 Mapa de esta etapa

1. La semántica: PUT vs PATCH, cara a cara.
2. El DTO del PATCH — y la regla de los wrappers.
3. La implementación: tocar solo lo que vino.
4. Experimento 1: la prueba de que no es un PUT disfrazado.
5. Experimento 2: el body vacío — tu política.
6. Experimento 3: ausente ≠ inválido, tercera visita.
7. Experimento 4: el null imposible (el precio, nombrado).
8. Tu turno: Mascota completa.
9. Criterios + checkpoint + registro + 🔗.

---

## 📜 Parte 1: La semántica — PUT vs PATCH, cara a cara

```
Ana hoy:  { nombre: "Ana María", telefono: "11-9999" }

PUT   /propietarios/1   {"nombre":"Ana"}
  → nombre: "Ana", telefono: null        ← REEMPLAZO: lo que no vino, no queda.

PATCH /propietarios/1   {"nombre":"Ana"}
  → nombre: "Ana", telefono: "11-9999"   ← PARCIAL: lo que no vino, NO SE TOCA.
```

Esa es toda la diferencia — y es enorme: el PUT exige el estado completo; el PATCH acepta **deltas** (solo los cambios). Para el cliente que "solo quería cambiar el nombre", PATCH es el verbo honesto.

La pregunta que define la implementación: **¿cómo sabe tu código si un campo "vino"?** Respuesta de tu propia Etapa 3: en un record, la clave ausente del JSON llega como **`null`** (si el componente es wrapper). De ahí la regla de implementación de hoy:

> **`null` = no vino = no tocar.** Campo presente = tocar (previa validación).

Simple, funcional… y con un precio escondido que el Experimento 4 te va a hacer pagar en persona. Paciencia.

## 📦 Parte 2: El DTO del PATCH — y la regla de los wrappers

```java
// 📁 src/main/java/.../veterinaria/dtos/propietario/PropietarioPatchRequest.java   (NUEVO)
package com.practica.veterinaria.dtos.propietario;

public record PropietarioPatchRequest(String nombre, String telefono) { }
// ↑ Mismos campos que el UpdateRequest — pero acá TODOS son opcionales por
//   contrato: null = no vino. DTO separado aunque parezca gemelo: el
//   CONTRATO es otro (un PATCH vacío es válido; un PUT sin nombre, no).
```

Y la regla que tu Etapa 3 dejó lista para cobrar hoy:

> **En un DTO de PATCH, TODO campo es wrapper — jamás primitivo.** ¿Por qué? Vos mismo lo demostraste: en tu stack, un `int` en un record **rechaza la ausencia** (required implícito)… y la ausencia es exactamente lo que PATCH necesita permitir en cada campo. Un primitivo en un PatchRequest es una contradicción de contrato: un "campo opcional que no se puede omitir". Si mañana patcheás algo numérico: `Integer`, `Double` — siempre.

*(Ficha: `@PatchMapping` — clan de siempre, ficha 🎛️ de la Etapa 3, nada nuevo.)*

## 🛠️ Parte 3: La implementación — tocar solo lo que vino

Guía mínima (🟨): la interfaz suma `PropietarioResponse patch(Long id, PropietarioPatchRequest request);` y la impl aplica **mutación condicional** — la cura de la Etapa 7, con un `if` por campo:

```java
// 📁 services/impl/PropietarioServiceImpl.java   (agregar)
@Override
public PropietarioResponse patch(Long id, PropietarioPatchRequest request) {
    if (request == null) {
        throw new BusinessException("El body es obligatorio");
    }
    Propietario existente = getPropietarioOrThrow(id);      // la instancia viva

    if (request.nombre() != null) {                         // ¿vino nombre?
        if (request.nombre().isBlank()) {                   //   vino: entonces
            throw new BusinessException("El nombre no puede quedar vacío");
        }                                                   //   se valida...
        existente.setNombre(request.nombre().trim());       //   ...y se toca.
    }                                                       // ¿no vino? ni se mira.

    if (request.telefono() != null) {
        existente.setTelefono(request.telefono());
    }

    propietarioRepository.save(existente);
    return toResponse(existente);
}
// ↑ EL corazón de PATCH: campo por campo, "¿vino? → validar y setear;
//   ¿no vino? → la instancia viva ya tiene el valor — no se toca nada".
//   Fijate la validación ADENTRO del if: solo se valida lo que vino.
```

Y el endpoint: `@PatchMapping("/{id}")` con la misma firma que tu PUT, delegando a `patch`. Una línea, como siempre.

## 🧨 Parte 4: Experimento 1 — la prueba de que no es un PUT disfrazado

**Predicción:** con Ana en `{nombre: "Ana María", telefono: "11-9999"}`, mandá `PATCH /propietarios/1` con **solo** `{"nombre":"Ana"}`. Después `GET /propietarios/1` — ¿el teléfono?

→ **200**, y el GET muestra `{"nombre":"Ana","telefono":"11-9999",...}` — **el teléfono sobrevivió**. Esa supervivencia del campo no tocado ES la prueba de que tu PATCH es PATCH: mandá el mismo body por el PUT y compará (teléfono → null). Mismo body, dos verbos, dos contratos — ahora vividos los dos.

*(Bonus de finura: tu PATCH, así implementado, es **idempotente** — repetirlo N veces deja el mismo estado. Dato para presumir: la especificación HTTP exige idempotencia al PUT pero NO se la exige al PATCH — la tuya la tiene de regalo por cómo la diseñaste. Hay PATCHs en el mundo que no la tienen, y son legales.)*

## 🧨 Parte 5: Experimento 2 — el body vacío

**Predicción:** `PATCH /propietarios/1` con body `{}` — ¿400? ¿200? ¿Y qué cambia?

→ **200, cero cambios**: ningún campo vino, ningún `if` entró, el save re-guardó lo mismo. Un no-op perfectamente educado. **¿Tiene sentido?** Las dos posturas existen en el mundo real: *(a)* "un PATCH sin cambios es válido — pedir nada y recibir nada es coherente"; *(b)* "un PATCH vacío es casi seguro un bug del cliente — mejor 400 avisándole". Tu política, tu decisión: dejalo como está o rechazalo — **comentario de justificación** al lado de lo que elijas. (Lo evaluable, como siempre, es el porqué.)

## 🧨 Parte 6: Experimento 3 — ausente ≠ inválido, tercera visita

**Predicción:** `PATCH /propietarios/1` con `{"nombre":""}` — ¿no-op como el vacío, o rechazo?

→ **400**, *"El nombre no puede quedar vacío"*. La distinción de la Etapa 3, ahora en su forma final: **ausente** = no opinar (respetado) · **presente pero inválido** = opinar mal (rechazado). El `if` anidado de la Parte 3 codifica exactamente eso — la validación vive *adentro* del "¿vino?", nunca afuera.

## 🧨 Parte 7: Experimento 4 — el null imposible (el precio, nombrado)

Ahora sí, el momento clave. Escenario real: Ana ya no quiere su teléfono en el sistema — el cliente quiere **borrarlo** (dejarlo en null). **Predicción:** ¿cómo lo mandarías por tu PATCH… y qué va a pasar?

Probá el intento obvio: `PATCH /propietarios/1` con `{"telefono": null}` → **200… y el teléfono sigue ahí.** No-op. ¿Por qué? Porque para Jackson y para tu código, `{"telefono": null}` y `{}` (la clave ausente) producen **exactamente el mismo record**: `telefono = null`. Tu regla "null = no tocar" **no puede distinguir** *"no opino"* de *"quiero borrarlo"* — las dos intenciones colapsan en el mismo null. Eso se llama **conflación del null**, y es EL límite conocido de este diseño:

> **Con "null = no tocar", PATCH no puede setear un campo en null.** No es un bug tuyo — es el trade-off del esquema, y se paga a sabiendas: para este dominio (¿cuán seguido se "borra" un teléfono?), el precio es bajo y la simplicidad lo vale.

👀 *El mundo tiene soluciones para cuando el precio NO es bajo — solo reconocerlas: el estándar **JSON Merge Patch** (donde `null` explícito significa "borrá" y la ausencia significa "no toques" — requiere poder distinguirlos al deserializar) y tipos como `JsonNullable` (una biblioteca que envuelve el campo en tres estados: ausente / presente-con-valor / presente-null). Si un día un requerimiento te exige borrar-vía-PATCH, estos son los nombres a googlear. Hoy: saber que tu diseño tiene este límite, y poder nombrarlo, ES la madurez que se evalúa.*

## ✍️ Parte 8: Tu turno — Mascota completa

`MascotaPatchRequest` + `patch` en el service + `@PatchMapping("/{id}")` — todo tuyo. Dos decisiones en el camino: *(a)* los campos del DTO (¿`propietarioId` patcheable? **Coherencia**: lo que decidiste en el PUT de la Etapa 7 debería sostenerse acá — o justificá por qué un verbo lo permite y el otro no); *(b)* tu política del body vacío, consistente con la de Propietario.

**Resultado esperado:** `PATCH /mascotas/1` `{"nombre":"Michi"}` → **200**, nombre nuevo, **especie intacta** (verificada por GET) · `{"especie":""}` → **400** · `{}` → según TU política, comportándose como la documentaste · `PATCH /mascotas/99` → **404** · y si permitiste mudanza: la regla de las 5 explotando con **400** traducido cuando el dueño nuevo está lleno.

## ✅ Criterios de "Etapa 8 completa"

- [ ] PATCH de ambos recursos, por mutación condicional, con DTOs todo-wrapper.
- [ ] Los cuatro experimentos con predicción escrita — incluido el intento de borrar el teléfono.
- [ ] Podés nombrar la conflación del null, explicar por qué tu diseño la tiene, y citar las salidas que existen.
- [ ] Tu política del body vacío, decidida, implementada y justificada en comentario — consistente entre recursos.
- [ ] La coherencia PUT/PATCH sobre la mudanza de dueño, sostenida o justificadamente rota.

## ✅ Checkpoint

*Recall:*
1. ¿Por qué el DTO de PATCH es todo-wrapper? ¿Qué pasaría con un `int` ahí, en tu stack, y por qué eso contradice al verbo?
2. ¿Qué par de intenciones colapsa la conflación del null, y en qué línea exacta de tu código ocurre el colapso?
3. PUT exige idempotencia, PATCH no — pero el tuyo la tiene. ¿Por qué?

*Decidí y justificá:*
4. Guía para tu equipo de TP en tres renglones: ¿cuándo exponen PUT, cuándo PATCH, cuándo ambos? (Pensá en quiénes son los clientes de cada endpoint.)
5. Defendé la política de body-vacío que NO elegiste — su mejor versión, y qué tipo de cliente/sistema la preferiría.
6. Un compañero implementó PATCH reusando el método del PUT "porque es casi igual". ¿Qué se rompe primero, y con qué caso de prueba lo demolés en 30 segundos?
7. Llega el requerimiento: *"debe poder eliminarse el teléfono de un propietario"*. Tres caminos posibles con tu API actual (pensá: ¿solo PATCH puede resolverlo?). Costos y beneficios de cada uno — y cuál proponés.

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Hoy la conexión es **inversa y con trofeo**: buscá `Patch` en todo el `sales-service` — **cero resultados**. La cátedra no lo cubre (todavía); vos acabás de diseñarlo, implementarlo, y — más importante — **conocés su trade-off central con nombre y apellido**. El día que en el TP alguien proponga "un endpoint para editar solo el precio", vos tenés el verbo, la regla de los wrappers, la política del vacío y el límite del null listos para poner sobre la mesa — con justificación, que es la moneda de esta materia. La ausencia del PATCH en el proyecto de la clase también te enseña algo: se puede lanzar una API perfectamente digna solo con PUT — PATCH es un refinamiento, no una obligación.

## ▶️ Próximo paso

Mirá tu rutina de cada arranque: la app nace **vacía**, y antes de probar nada cargás propietarios y mascotas a mano, request por request. Ya lo sufriste en cada experimento desde la Etapa 5. Decime **"arranquemos etapa 9"** y llega la **semilla** — datos de arranque con el `CommandLineRunner` que ya construiste en tu proyecto-1 (reuso directo, vas a sonreír al verlo) — y el **examen final**: la batería completa de la API, corrida de punta a punta. Después de eso, solo queda el vuelo solo.

---

**FIN DE LA ETAPA 8**
