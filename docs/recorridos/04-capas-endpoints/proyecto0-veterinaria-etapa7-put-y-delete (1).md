# 🌱 Proyecto 0 (clase 04) — Etapa 7: Corregir y borrar — PUT y DELETE canónicos

> **Objetivo:** los verbos que faltan — **PUT** (reemplazo completo, id en la ruta) y **DELETE** (con su 204) — y dos decisiones de diseño que venían esperando: cómo actualizar **sin romper relaciones**, y qué hacer con los **huérfanos** al borrar.
>
> **Cómo trabaja esta etapa (v2):** el principio central completo PRIMERO — con sus diagramas — y recién después el código, que escribís **una sola vez y en su forma definitiva**. La versión rota del PUT existe solo como demostración opcional de 5 minutos, no como código tuyo.
>
> **Pre-requisito:** Etapa 6 completa.
>
> **Tiempo estimado:** 55-65 minutos.

---

## 🗺️ El mapa general — dónde estamos parados

```
              REQUEST                               RESPONSE
                 │                                      ▲
                 ▼                                      │
        ┌─────────────────┐                      ┌──────────────────┐
        │   CONTROLLERS   │ ★ +PUT y +DELETE     │  Advice           │
        │                 │   en ambos           │  (sin cambios —   │
        └────────┬────────┘                      │   ya traduce      │
                 ▼                               │   todo lo de hoy  │
        ┌─────────────────┐                      │   GRATIS)         │
        │    SERVICES     │ ★ +update, +deleteById   └──────────────┘
        └───┬─────────┬───┘
            ▼         ▼
   ┌────────────┐ ┌──────────────┐
   │  DOMINIO   │ │ REPOSITORIES │
   │ ★ Propie-  │ │ (sin cambios │
   │   tario:   │ │  — PERO su   │
   │   2 campos │ │  save "con   │
   │   pierden  │ │  id" POR FIN │
   │   el final │ │  se usa)     │
   └────────────┘ └──────────────┘
```

Lectura del mapa: hoy se agregan verbos en controllers y services, **la entidad Propietario se toca por primera y única vez** (dos campos cambian de contrato — abajo el porqué), y el traductor de la Etapa 6 trabaja gratis para todos los errores nuevos (el `PUT /99` va a dar 404 sin que escribas nada).

## 🗂️ Los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── controllers/
    │   ├── PropietarioController.java           (SE TOCA: +PUT, +DELETE)
    │   └── MascotaController.java               (SE TOCA — tu turno)
    ├── models/entities/
    │   ├── Propietario.java                     (SE TOCA: 2 campos — Parte 2)
    │   └── Mascota.java                         (se usa: sus setters esperaban este día)
    ├── dtos/
    │   ├── propietario/PropietarioUpdateRequest.java   ★ NUEVO
    │   └── mascota/MascotaUpdateRequest.java           ★ NUEVO — tu turno
    └── services/ (+ impl/)                      (SE TOCAN: +update, +deleteById)
```

## 🧭 Mapa de esta etapa

1. **EL PRINCIPIO COMPLETO primero:** semántica de los verbos + mutación vs reconstrucción.
2. El precio en la entidad (el único toque al dominio del proyecto).
3. Construcción: el PUT canónico de Propietario.
4. Experimento opcional: ver la trampa con tus ojos (5 minutos, reversible).
5. La semántica completa: reemplazo total + idempotencia.
6. DELETE de Propietario — y la decisión de los huérfanos (tuya).
7. Tu turno: Mascota — PUT con decisión, DELETE con sentencia.
8. Criterios + checkpoint + registro + 🔗.

---

## 🧠 Parte 1: EL PRINCIPIO COMPLETO (leé toda esta parte antes de codear)

### 1a. La semántica de los dos verbos

**PUT = reemplazo completo.** El body trae TODO lo editable, y lo que trae es lo que queda. PUT no completa huecos ni "cambia solo lo que vino" — eso es PATCH (Etapa 8). Mandás el PUT sin `telefono` → el teléfono queda `null`: pediste reemplazo, recibiste reemplazo.

**El id viaja en la RUTA**, no en el body:

```
        PUT /veterinaria/propietarios/1        { "nombre": "Ana", "telefono": ... }
                                      │          └──────────────┬────────────────┘
                             el QUIÉN ┘                el QUÉ ──┘
                        (la identidad del recurso       (los datos nuevos,
                         vive en la URL — Etapa 2)       completos)
```

Repetir el id en el body invita a la contradicción (ruta dice 1, body dice 7 — ¿cuál vale?). ⚠️ *Nota honesta: el proyecto de la clase lleva el id del PUT en el body — funciona, y el propio recorrido lo marca como mejorable. Vos escribís la canónica; sabé defender ambas.*

**DELETE**: baja del recurso identificado por la ruta → éxito = **204 No Content** (salió bien, nada que decir: `void`, sin body).

### 1b. El principio central: mutación vs reconstrucción

Un update se puede implementar de dos maneras — y la diferencia es TODO cuando la entidad tiene relaciones. El mecanismo lo conocés de la perla del `save` (Etapa 5: en Java viaja la **referencia** — mutar el objeto compartido se ve desde todos lados; crear uno nuevo, nadie más lo conoce):

```
CAMINO A — RECONSTRUCCIÓN (el intuitivo… y el roto para entidades con relaciones)

  service:  actualizado = new Propietario(id, nombreNuevo, telNuevo)
                                │
                                ▼
                     ┌──────────────────────┐        ┌──────────────────────┐
                     │ Propietario NUEVO    │        │ Propietario VIEJO    │
                     │  nombre: "Ana" ✓     │        │  nombre: viejo       │
                     │  mascotas: [] ✗✗✗    │        │  mascotas: [🐕,🐈]   │
                     └──────────┬───────────┘        └──────────┬───────────┘
                                │ save() lo guarda              │ el save lo DESCARTA
                                ▼                               ▼
                     queda en el repo,                 muere — y con él, LA ÚNICA
                     con la lista VACÍA                LISTA que sabía de las mascotas

  Resultado: datos simples ✓ · relaciones MUERTAS · y NADIE avisa (200 verde).


CAMINO B — MUTACIÓN (el canónico — el de la cátedra, verificado en su código)

  service:  existente = getPropietarioOrThrow(id)   ← recupero la instancia VIVA
            existente.setNombre(nombreNuevo)        ← muto TODO lo editable
            existente.setTelefono(telNuevo)            (PUT: reemplazo completo)
            save(existente)
                                ▼
                     ┌──────────────────────┐
                     │ el MISMO Propietario │   una sola instancia, la de siempre:
                     │  nombre: "Ana" ✓     │   los datos cambian, la lista de
                     │  mascotas: [🐕,🐈] ✓ │   mascotas VIAJA INTACTA — porque
                     └──────────────────────┘   nunca hubo otro objeto.
```

> **La regla, para siempre:** *reconstruir* una entidad con relaciones pierde todo lo que la instancia vieja "sabía" y el constructor nuevo no recibe. **Actualizar = recuperar la instancia viva y mutarla.**

### 1c. Inventario exacto de la etapa

| Pieza | Qué pasa |
|---|---|
| `Propietario.java` | `nombre` y `telefono` pierden el `final`, ganan `@Setter` (Parte 2 — el precio de la mutación) |
| DTOs Update ×2 | nuevos: solo lo editable, **sin id** (viaja en la ruta) |
| Services | `+update(id, request)` por mutación · `+deleteById(id)` |
| Controllers | `+@PutMapping("/{id}")` · `+@DeleteMapping("/{id}")` con 204 |
| Repos, advice, excepciones | **nada** — el save "con id" y el traductor ya estaban listos esperando esto |

Con el mapa completo en la cabeza: a construir. Una sola vez, en su forma definitiva.

---

## 🔧 Parte 2: El precio en la entidad

📍 **Dónde estamos:**

```
Controllers ──► Services ──► Repositories
                   │
                   ▼
                Dominio  ◄━━ ACÁ (único toque al dominio de todo el proyecto)
```

```
┌─ 📁 models/entities/Propietario.java ──────────────────────────┐
│  @Getter                                                       │
│  public class Propietario {                                    │
│      @Setter private Long id;                                  │
│  ╔══════════════════════════════════════════════╗              │
│  ║  @Setter private String nombre;              ║ ◄── eran     │
│  ║  @Setter private String telefono;            ║     `final`: │
│  ╚══════════════════════════════════════════════╝     cambian  │
│      private final List<Mascota> mascotas = ...;      SOLO     │
│      ...constructor y agregarMascota: sin cambios...  estos 2  │
│  }                                                             │
└────────────────────────────────────────────────────────────────┘
```

El trade-off, nombrado: la inmutabilidad era una garantía ("nadie cambia un propietario ya creado") y la entregás a cambio de poder mutar en el update — **la mutabilidad es el precio de la mutación**. Y el detalle fino: **la lista de mascotas NO paga ese precio** — sigue `final` y sin setter: nadie reemplaza la colección entera; se muta solo por los métodos del dominio (`agregarMascota`). Mutable no significa indefenso.

## 🛠️ Parte 3: El PUT canónico de Propietario

El DTO:

```java
// 📁 src/main/java/.../veterinaria/dtos/propietario/PropietarioUpdateRequest.java   (NUEVO)
package com.practica.veterinaria.dtos.propietario;

public record PropietarioUpdateRequest(String nombre, String telefono) { }
// ↑ SIN id: viaja en la ruta. Solo lo editable — completo, porque es PUT.
```

📍 El service — la mutación del Camino B, tal cual el diagrama:

```
┌─ 📁 services/impl/PropietarioServiceImpl.java ─────────────────┐
│  ...métodos existentes: sin cambios...                         │
│  ╔═══════════════════════════════════════════════════╗         │
│  ║ @Override                                         ║ ◄──     │
│  ║ public PropietarioResponse update(Long id,        ║ método  │
│  ║         PropietarioUpdateRequest request) {       ║ NUEVO   │
│  ║   if (request == null || request.nombre() == null ║ entero  │
│  ║       || request.nombre().isBlank()) {            ║         │
│  ║     throw new BusinessException(                  ║         │
│  ║         "El nombre es obligatorio");              ║         │
│  ║   }                                               ║         │
│  ║   Propietario existente =                         ║         │
│  ║       getPropietarioOrThrow(id);   // vivo, o 404 ║         │
│  ║   existente.setNombre(request.nombre().trim());   ║         │
│  ║   existente.setTelefono(request.telefono());      ║         │
│  ║   propietarioRepository.save(existente);          ║         │
│  ║   return toResponse(existente);                   ║         │
│  ║ }                                                 ║         │
│  ╚═══════════════════════════════════════════════════╝         │
└────────────────────────────────────────────────────────────────┘
   (+ la firma en la interfaz PropietarioService, claro)
```

**Predicción antes de correr:** el `save(existente)` — la entidad YA tiene id… ¿qué rama del save ejecuta? *(Recordá el save de la Etapa 5: sin id = alta con id nuevo; con id = saca la versión guardada y mete esta.)* ¿Y por qué las mascotas sobreviven?

📍 El controller:

```
┌─ 📁 controllers/PropietarioController.java ────────────────────┐
│  ...GET, GET/{id}, POST: sin cambios...                        │
│  ╔═══════════════════════════════════════════════════╗         │
│  ║ @PutMapping("/{id}")                              ║ ◄──     │
│  ║ public PropietarioResponse update(                ║ NUEVO   │
│  ║         @PathVariable Long id,                    ║         │
│  ║         @RequestBody PropietarioUpdateRequest r) {║         │
│  ║   return propietarioService.update(id, r);        ║         │
│  ║ }                                                 ║         │
│  ╚═══════════════════════════════════════════════════╝         │
└────────────────────────────────────────────────────────────────┘
   El controller junta el QUIÉN (ruta) con el QUÉ (body) y delega.
   Esta firma ES la forma canónica REST.
```

**Verificá la mutación completa** — la secuencia que importa, con Ana (id 1) teniendo 2 mascotas (`GET /propietarios/1` → `cantidadMascotas: 2` antes de empezar):

1. `PUT /veterinaria/propietarios/1` con `{"nombre":"Ana María","telefono":"11-9999"}` → **200**, datos nuevos.
2. `GET /propietarios/1` → datos nuevos **Y `cantidadMascotas: 2` intacto** ✓ — la relación viajó porque es la misma instancia.
3. `PUT /veterinaria/propietarios/99` → **404** con tu `ErrorResponse` — el traductor de la Etapa 6, trabajando gratis.

## 🧨 Parte 4: Experimento opcional — ver la trampa con tus ojos (5', reversible)

El diagrama del Camino A te contó el desastre; si querés **sentirlo** (recomendado, pero tu código canónico no se toca): comentá temporalmente las tres líneas de mutación del `update` y reemplazalas por la reconstrucción:

```java
// (TEMPORAL — el Camino A, solo para el experimento)
Propietario actualizado = new Propietario(id, request.nombre().trim(), request.telefono());
propietarioRepository.save(actualizado);
return toResponse(actualizado);
```

**Predicción:** con Ana y sus 2 mascotas — PUT, y después: *(a)* `cantidadMascotas`? *(b)* `GET /mascotas` — ¿existen? *(c)* `GET /mascotas/1` — ¿qué da?

Corré: *(a)* **0** — mascotas borradas de Ana · *(b)* siguen en su repo, **huérfanas** · *(c)* **404 absurdo** — *"no se encontró propietario para la mascota"*: la mascota existe, pero el `findPropietarioOwner` (el método de la Etapa 5 que averigua el dueño recorriendo las listas de todos los propietarios) ya no encuentra a nadie que la contenga. Y lo peor: **todo dio 200 verde** — corrupción silenciosa, el bug más caro de tu colección, invisible para el traductor porque no es un error: es un diseño obediente. **Restaurá la mutación** y verificá que el punto 2 de la Parte 3 vuelve a dar bien.

## 📐 Parte 5: La semántica completa — reemplazo total + idempotencia

**El reemplazo es total.** PUT sin `"telefono"` → 200, y el teléfono quedó **null**. No es bug — es PUT: *lo que no vino, no queda*. La incomodidad de "yo solo quería cambiar el nombre" tiene verbo propio: la Etapa 8.

**La perla del verbo: idempotencia.**

```
  PUT /propietarios/1 {"nombre":"Ana"}   ×3 veces  →  estado final: IGUAL tras 1, 2 o 3
  POST /propietarios  {"nombre":"Ana"}   ×3 veces  →  TRES Anas, ids 5, 6, 7...
```

Probá ambas secuencias. Eso es **idempotencia**: repetir la operación deja el mismo estado (PUT, DELETE y GET la tienen; POST no). ¿A quién le importa? A todo cliente con red inestable: si el PUT no respondió, **reintentar es seguro**; si el POST no respondió, reintentar duplica. Es de las propiedades más citadas del diseño REST — y vos ya la tenés vivida, no recitada.

## 🗑️ Parte 6: DELETE — y la decisión de los huérfanos (tuya)

📍 La mecánica es corta y va sin sorpresas:

```
┌─ service ──────────────────────────────┐  ┌─ controller ──────────────────────┐
│ @Override                              │  │ @DeleteMapping("/{id}")           │
│ public void deleteById(Long id) {      │  │ @ResponseStatus(                  │
│   Propietario existente =              │  │     HttpStatus.NO_CONTENT)        │
│       getPropietarioOrThrow(id);       │  │ public void delete(               │
│   propietarioRepository               │  │     @PathVariable Long id) {      │
│       .delete(existente);              │  │   propietarioService              │
│ }                                      │  │       .deleteById(id);            │
└────────────────────────────────────────┘  └───────────────────────────────────┘
```

*(👀 vas a ver APIs que devuelven 200 con el objeto borrado en el body — existe y es válido; el 204 + void es la convención dominante y la de la cátedra.)*

Verificá lo simple: `DELETE /propietarios/2` → **204** · `GET /propietarios/2` → **404**. Y ahora **la pregunta con dientes** — borrá un propietario **que tenga mascotas** y consultá `GET /mascotas`:

**Sus mascotas siguen ahí. Huérfanas.** Y cada `GET /mascotas/{id}` de ellas explota en el 404 absurdo del dueño inexistente. Es el espejo exacto de la pregunta que te dejé pensando en la Etapa 5 (checkpoint 8: *"borrás una mascota… ¿queda viva en la lista del dueño?"*) — ahora del otro lado de la relación. Tu decisión, **documentada en comentario de 2-3 líneas** (ambas defendibles; lo evaluable es el porqué):

```
   DELETE /propietarios/{id} con mascotas a cargo — ¿qué hace el service?

   OPCIÓN A — CASCADA                    OPCIÓN B — PROHIBICIÓN
   ─────────────────                     ──────────────────────
   borra al propietario                  rechaza la operación:
   Y a sus mascotas del                  "transferí o eliminá sus
   repo de mascotas                      mascotas primero"
   │                                     │
   ▼                                     ▼
   la mascota no existe                  el estado actual no permite
   sin dueño: ciclos de                  la operación → excepción de
   vida atados (¿te suena               negocio con mensaje claro
   composición vs agregación?            (👀 la cátedra tiene una
   Es ESO, decidido en código)           ConflictException → 409 para
                                         exactamente esto — si elegís B,
                                         es tu excusa para sumar la 4ª
                                         excepción y su handler)
```

Elegí, implementá, justificá. Verificá que tu elección se comporte como la escribiste.

## ✍️ Parte 7: Tu turno — Mascota

**PUT de mascota** — con SU decisión: `MascotaUpdateRequest` lleva `nombre` y `especie` seguro… ¿y `propietarioId`? ¿Un PUT puede **mudar** la mascota de dueño? Decidilo y justificalo en comentario. *(Si sí: sacarla de la lista del dueño viejo + meterla en la del nuevo — donde la regla de las 5 puede explotar y tu traducción de la Etapa 6 tiene trabajo nuevo. Si no: defendé por qué "mudar" no es una edición sino otra operación.)* Mecánica: **mutación** — los setters que `Mascota` trae desde la Etapa 5 ("el PUT/PATCH los van a necesitar": promesa cumplida).

**DELETE de mascota** — la **sentencia** de aquella pregunta de la Etapa 5: el delete deja el sistema **consistente** — la mascota sale de SU repo **y** de la lista de su dueño. Pensá con el árbol de capas: quién encuentra al dueño ya lo tenés escrito (`findPropietarioOwner`); ¿y quién la saca de la lista — el service metiendo mano con `getMascotas().remove(...)`, o un método de dominio `quitarMascota(...)`, hermano del `agregarMascota`? Elegí con criterio de capas.

**Resultado esperado:** `PUT /mascotas/1` → 200, mutada, dueño intacto · `PUT /mascotas/99` → 404 · `DELETE /mascotas/1` → **204**, y el `GET` del dueño muestra `cantidadMascotas` **decrementado** (consistencia verificada cruzado) · `GET /mascotas/1` → 404 limpio · tu decisión del PUT-mudanza comportándose exactamente como la justificaste.

**Cuando termines — y solo entonces:** abrí el `deleteById` de `ProductoServiceImpl` en el `sales-service` y compará con tu sentencia. *(Al final a propósito: primero decidís vos, después te medís contra la cátedra — en ese orden se aprende a diseñar.)*

## ✅ Criterios de "Etapa 7 completa"

- [ ] Leíste la Parte 1 entera antes de codear; el código canónico se escribió UNA vez.
- [ ] PUT por mutación en ambos recursos, relaciones sobrevivientes verificadas.
- [ ] (Opcional pero recomendado) la trampa de la reconstrucción, vista y revertida.
- [ ] Idempotencia vivida: triple-PUT vs triple-POST.
- [ ] Huérfanos del DELETE: TU decisión implementada y justificada en comentario.
- [ ] DELETE de mascota consistente, verificado cruzado — y recién después, comparado con la cátedra.

## ✅ Checkpoint

*Recall:*
1. ¿Qué rama del `save` ejecuta un update, y por qué la mutación preserva las relaciones donde la reconstrucción las mata? (Dibujá el diagrama de las dos instancias de memoria.)
2. ¿Qué es la idempotencia, cuáles de tus verbos la tienen, y a quién le resuelve qué problema concreto?
3. ¿Por qué el 204 va con `void` y no con el objeto borrado?

*Decidí y justificá:*
4. El id del PUT: ruta (tu diseño) vs body (el de la clase). Defendé ambos en dos renglones cada uno — y decí cuál elegirías en el TP.
5. ¿Qué perdiste exactamente al sacar los `final` de la entidad, y por qué la lista de mascotas NO pagó ese precio?
6. Defendé la opción de huérfanos que NO elegiste — su mejor versión. ¿Qué negocio la haría la correcta?
7. Tu compañero implementó el PUT haciendo "DELETE + POST adentro del service, total da igual". Funciona… casi. Listale todo lo que rompió (pista: id nuevo, relaciones, idempotencia, códigos).

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Abrí el `update` de `ProductoServiceImpl` en el `sales-service`: recuperar la instancia viva → `setTipo`/`setPrecioBase`/`setDescripcion` → save — **el Camino B, línea por línea** (con una validación extra de pertenencia que vale leer: chequea que el producto sea del comercio antes de tocar nada — ¿por qué?). Su `deleteById` ya lo comparaste al cerrar tu sentencia. La diferencia restante es la que sabés defender: su PUT lleva el id en el body; el tuyo, en la ruta. Releé del recorrido **P6 §6 el comentario ⑦** (la nota ⚠️ sobre el PUT del profe): ahora es tuyo con intereses.

## ▶️ Próximo paso

Quedó picando la incomodidad de la Parte 5: *"yo solo quería cambiar el nombre"* — y el PUT te obligó a mandar todo, so pena de null. Existe un verbo para eso, el proyecto de la clase no lo usa, y vos lo pediste para estar adelante: **"arranquemos etapa 8"** → **PATCH**, con la decisión de diseño más fina del proyecto (¿qué significa exactamente que un campo *no haya venido*? — tu Etapa 3 tiene la mitad de la respuesta).

---

**FIN DE LA ETAPA 7 — v2**
