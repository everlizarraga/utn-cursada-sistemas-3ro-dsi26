# 🌱 Proyecto 0 (clase 04) — Etapa 7: Corregir y borrar — PUT y DELETE canónicos

> **Objetivo:** completar los verbos que faltan — **PUT** (reemplazo completo, con el id en la ruta como REST manda) y **DELETE** (con su 204) — y de paso enfrentar dos decisiones de diseño que venían esperando: cómo actualizar sin romper relaciones, y qué hacer con los huérfanos al borrar.
>
> **El momento clave:** el experimento donde tu primer PUT "perfecto" le borra las mascotas a Ana en silencio — y entendés por qué **mutación** le gana a **reconstrucción** cuando la entidad tiene relaciones.
>
> **Pre-requisito:** Etapa 6 completa (el traductor trabaja gratis para todo lo de hoy).
>
> **Tiempo estimado:** 55-65 minutos.
>
> **Andamiaje:** 🟨 fuerte — Propietario va guiado con trampas incluidas; Mascota es tuya con especificación, y las decisiones de diseño son tuyas con justificación.

---

## 🗂️ Dónde estamos — los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── controllers/
    │   ├── PropietarioController.java           (SE TOCA: +PUT, +DELETE)
    │   └── MascotaController.java               (SE TOCA — tu turno)
    ├── models/entities/
    │   ├── Propietario.java                     (SE TOCA: la mutabilidad tiene precio)
    │   └── Mascota.java                         (se usa — sus setters esperaban este día)
    ├── dtos/
    │   ├── propietario/PropietarioUpdateRequest.java   (NUEVO)
    │   └── mascota/MascotaUpdateRequest.java           (NUEVO — tu turno)
    └── services/ (+ impl/)
        ├── PropietarioService(+Impl).java       (SE TOCAN: +update, +deleteById)
        └── MascotaService(+Impl).java           (SE TOCAN — tu turno)
```

---

## 🧭 Mapa de esta etapa

1. La semántica del PUT (y dónde viaja el id).
2. El PUT ingenuo — construir la trampa.
3. Experimento 1: el PUT que huerfaniza en silencio.
4. La cura: mutación (y su precio en la entidad).
5. La semántica completa: reemplazo total + idempotencia.
6. DELETE de propietario — y la decisión de los huérfanos.
7. Tu turno: Mascota — PUT con decisión, DELETE con sentencia.
8. Criterios + checkpoint + registro + 🔗.

---

## 📜 Parte 1: La semántica del PUT

Dos reglas definen al verbo:

**PUT = reemplazo completo.** El body trae TODO lo editable, y lo que trae es lo que queda. PUT no "completa huecos" ni "cambia solo lo que vino" — eso es otro verbo (el de la próxima etapa). Si mandás el PUT sin `telefono`, el teléfono nuevo es `null`: pediste un reemplazo, te dieron un reemplazo.

**El id viaja en la RUTA:** `PUT /veterinaria/propietarios/1`. ¿Por qué ahí y no en el body? Porque el id **identifica al recurso** — dice *a quién* le pasa algo — y la identidad del recurso vive en la URL desde la Etapa 2. El body trae el *qué* (los datos nuevos); la ruta trae el *quién*. Repetir el id en el body invita a la contradicción (¿ruta dice 1, body dice 7 — cuál vale?).

⚠️ *Nota honesta que ya conocés del recorrido: el proyecto de la clase lleva el id del PUT en el body — funciona, y el propio material lo marca como mejorable. Vos escribís la forma canónica. Vas a terminar con un PUT más canónico que el del profe: sabé defender ambos diseños, que es lo que la materia evalúa.*

El DTO, con la regla aplicada:

```java
// 📁 src/main/java/.../veterinaria/dtos/propietario/PropietarioUpdateRequest.java   (NUEVO)
package com.practica.veterinaria.dtos.propietario;

public record PropietarioUpdateRequest(String nombre, String telefono) { }
// ↑ SIN id: viaja en la ruta — no se repite, no se contradice. Solo lo editable.
```

## 🛠️ Parte 2: El PUT ingenuo — construir la trampa

Vamos a implementarlo **como se le ocurre a cualquiera** — incluido el vos de hace una hora. En el service (interfaz: `PropietarioResponse update(Long id, PropietarioUpdateRequest request);`) y su impl:

```java
// 📁 services/impl/PropietarioServiceImpl.java   (versión INGENUA — dura 10 minutos)
@Override
public PropietarioResponse update(Long id, PropietarioUpdateRequest request) {
    if (request == null || request.nombre() == null || request.nombre().isBlank()) {
        throw new BusinessException("El nombre es obligatorio");
    }
    getPropietarioOrThrow(id);              // existe, o 404 gratis del traductor

    Propietario actualizado = new Propietario(id, request.nombre().trim(),
                                              request.telefono());
    propietarioRepository.save(actualizado);   // mismo id → la rama "con id"
    return toResponse(actualizado);            // del save: fuera la vieja,
}                                              // adentro la nueva. Prolijo... ¿no?
```

Y el endpoint:

```java
// 📁 controllers/PropietarioController.java   (agregar)
@PutMapping("/{id}")
public PropietarioResponse update(@PathVariable Long id,
                                  @RequestBody PropietarioUpdateRequest request) {
    return propietarioService.update(id, request);
}
// ↑ El controller junta las dos mitades — el quién de la ruta, el qué del
//   body — y delega. Esta firma ES la forma canónica REST.
```

*(Ficha: `@PutMapping` y `@DeleteMapping` son del clan de siempre — misma ficha 🎛️ de la Etapa 3.)*

Probalo: `PUT /veterinaria/propietarios/1` con `{"nombre":"Ana María","telefono":"11-9999"}` → **200**, datos nuevos. Funciona. Compila. Los tests pasarían. **Está roto.**

## 🧨 Parte 3: Experimento 1 — el PUT que huerfaniza en silencio

Preparación: asegurate de que Ana (id 1) tenga **2 mascotas** cargadas. Verificá: `GET /propietarios/1` → `cantidadMascotas: 2`.

**Predicción, por escrito:** mandá el PUT de recién otra vez. Después: *(a)* `GET /propietarios/1` — ¿`cantidadMascotas`? *(b)* `GET /mascotas` — ¿las mascotas siguen? *(c)* `GET /mascotas/1` — ¿qué devuelve?

Probá y mirá el desastre en cámara lenta: *(a)* **`cantidadMascotas: 0`** — el PUT le borró las mascotas a Ana. *(b)* Las mascotas **siguen existiendo** en su repo… *(c)* pero `GET /mascotas/1` da un **404 absurdo**: *"No se encontró propietario para la mascota"* — la mascota existe, ¡pero tu `findPropietarioOwner` ya no encuentra a nadie que la contenga!

¿Qué pasó? Tu `new Propietario(...)` creó una instancia **nueva, con la lista de mascotas vacía** — y el save tiró a la basura la instancia vieja, la única que sabía de las mascotas. Los datos "simples" sobrevivieron; **la relación murió con el objeto**. Y lo peor: **nada avisó** — 200 verde, cero excepciones, corrupción silenciosa. El bug más caro de tu colección, y no lo atrapa ningún traductor porque no es un error: es un diseño que hace exactamente lo que le pediste.

**La lección con nombre:** *reconstruir* una entidad que tiene **relaciones** es jugar con fuego — todo lo que la instancia vieja "sabía" y el constructor nuevo no recibe, se pierde.

## 💊 Parte 4: La cura — mutación (y su precio)

La forma canónica (la de la cátedra, verificada en su código): **no fabricar una instancia nueva — recuperar la viva y mutarla.** Pero tu entidad se resiste: `nombre` y `telefono` son `final`. La inmutabilidad que era virtud en el DTO acá te cobra peaje — primer retoque, **con su trade-off nombrado**:

```java
// 📁 models/entities/Propietario.java   (SE TOCA — dos campos cambian de contrato)
@Getter
public class Propietario {
    @Setter private Long id;
    @Setter private String nombre;      // ← eran final. La mutabilidad es el
    @Setter private String telefono;    //   PRECIO del update-por-mutación:
    private final List<Mascota> mascotas = new ArrayList<>();   //   lo pagás acá,
    // ...constructor y agregarMascota, intactos...              //   lo cobrás abajo.
}
// (La lista sigue final y sin setter: NADIE reemplaza la colección entera —
//  se muta por los métodos del dominio. Mutable no significa indefenso.)
```

Y el update, curado:

```java
// 📁 services/impl/PropietarioServiceImpl.java   (la versión CANÓNICA — reemplaza a la ingenua)
@Override
public PropietarioResponse update(Long id, PropietarioUpdateRequest request) {
    if (request == null || request.nombre() == null || request.nombre().isBlank()) {
        throw new BusinessException("El nombre es obligatorio");
    }
    Propietario existente = getPropietarioOrThrow(id);   // la instancia VIVA —
                                                         // con sus mascotas adentro
    existente.setNombre(request.nombre().trim());        // muto lo editable,
    existente.setTelefono(request.telefono());           // TODO lo editable (PUT!)
    propietarioRepository.save(existente);
    return toResponse(existente);
}
```

**Predicción antes de correr:** el `save(existente)` — ¿qué rama del upsert ejecuta, y qué hace exactamente con la lista interna del repo? ¿Y las mascotas de Ana?

Reponé las mascotas de Ana (la app se reinició — memoria efímera, ya sabés) y repetí la secuencia del experimento: PUT → `GET /propietarios/1` → **datos nuevos Y `cantidadMascotas` intacto** · `GET /mascotas/1` → sano. La relación sobrevivió porque **es la misma instancia** — el save "con id" sacó y volvió a meter al mismo objeto, mascotas incluidas. Mutación 1, reconstrucción 0.

## 📐 Parte 5: La semántica completa — reemplazo total + idempotencia

Dos verificaciones cortas que fijan al verbo:

**El reemplazo es total.** Mandá el PUT **sin** `"telefono"`: → 200, y el teléfono quedó **`null`**. No es bug — es PUT: *lo que no vino, no queda*. Si eso te incomoda ("yo solo quería cambiar el nombre"), perfecto: esa incomodidad se llama PATCH y es toda la próxima etapa.

**La perla del verbo: idempotencia.** Mandá el MISMO PUT tres veces seguidas. **Predicción:** ¿el sistema queda igual después de 1 que después de 3? → Sí: mismo estado final, siempre. Ahora contrastá: mandá el mismo **POST** de un propietario tres veces → **tres Anas**, tres ids. Eso es **idempotencia**: PUT (y DELETE, y GET) repetidos dejan el mismo estado — POST no. ¿A quién le importa? A cualquier cliente con red inestable: si el PUT no respondió, **reintentar es seguro**; si el POST no respondió, reintentar puede duplicar. La idempotencia es de las propiedades más citadas del diseño REST — ya la tenés vivida, no recitada.

## 🗑️ Parte 6: DELETE — y la decisión de los huérfanos

La mecánica es corta:

```java
// 📁 services/ + impl/ + controller   (Propietario)
// interfaz:   void deleteById(Long id);
// impl:       getPropietarioOrThrow(id) → propietarioRepository.delete(existente)
// controller: @DeleteMapping("/{id}") + @ResponseStatus(HttpStatus.NO_CONTENT) + void
```

**204 No Content**: salió bien y no hay nada que decir — void, sin body. *(👀 vas a ver APIs que devuelven 200 con el objeto borrado adentro: existe, es válido; el 204 es la convención dominante y la de la cátedra.)*

Verificá lo simple: `DELETE /propietarios/2` → **204** · `GET /propietarios/2` → **404**. Y ahora, **la pregunta con dientes** — predición y experimento en uno: borrá un propietario **que tenga mascotas**. `GET /mascotas` — ¿sus mascotas…?

**Siguen ahí. Huérfanas.** Y cada `GET /mascotas/{id}` de ellas explota en el 404 absurdo del `findPropietarioOwner`. Tu DELETE creó fantasmas — el espejo exacto de la pregunta 7 de la Etapa 5, del otro lado de la relación.

**Decisión de diseño — TUYA, con justificación en comentario** (las dos son defendibles; lo evaluable es el porqué):

- **Opción A — cascada:** borrar al propietario borra también sus mascotas de `MascotaRepository` (la mascota no existe sin dueño → su ciclo de vida está atado: ¿te suena de las lecturas de agregación/composición? Es ESO, decidido en código).
- **Opción B — prohibición:** no se puede borrar un propietario con mascotas → excepción de negocio con mensaje claro ("transferí o eliminá sus mascotas primero"). *(👀 extensión opcional: el proyecto de la clase tiene una `ConflictException → 409` para "el estado actual no permite la operación" — si elegís B, es TU excusa perfecta para sumar la cuarta excepción y su handler. Tu checkpoint 6 de la Etapa 6 ya lo había pensado.)*

Elegí, implementá, justificá en un comentario de 2-3 líneas sobre el método. Verificá que tu elección se comporte como la escribiste.

## ✍️ Parte 7: Tu turno — Mascota

**PUT de mascota** — con SU decisión de diseño: `MascotaUpdateRequest` lleva `nombre` y `especie` seguro… ¿y `propietarioId`? ¿Un PUT puede **mudar** la mascota de dueño? Decidilo y justificalo en comentario. *(Si decís que sí, mirá todo lo que se te viene: sacarla de la lista del dueño viejo, meterla en la del nuevo — donde la regla de las 5 puede explotar y tu traducción de la Etapa 6 tiene trabajo nuevo. Si decís que no, defendé por qué "mudar de dueño" no es una edición sino otra operación.)* La mecánica: **mutación** — los setters que `Mascota` trae desde la Etapa 5 ("el PUT/PATCH los van a necesitar": promesa cumplida).

**DELETE de mascota** — la **sentencia** de tu pregunta 7 de la Etapa 5, por fin: el delete debe dejar el sistema **consistente** — la mascota sale del repo Y de la lista de su dueño. Pensá con el árbol: ¿quién encuentra al dueño (ya lo tenés escrito), y quién debería sacar la mascota de la lista — el service metiendo mano en `getMascotas().remove(...)`, o un método de dominio `quitarMascota(...)` como hermano del `agregarMascota`? Elegí con criterio de capas.

**Resultado esperado:** `PUT /mascotas/1` → 200, mutada, dueño intacto · `PUT /mascotas/99` → 404 · `DELETE /mascotas/1` → **204**, y `GET /propietarios/{dueño}` muestra `cantidadMascotas` **decrementado** (¡la consistencia, verificada cruzado!) · `GET /mascotas/1` → 404 limpio · tu decisión del PUT-mudanza comportándose exactamente como la justificaste.

**Cuando termines — y solo entonces:** abrí el `deleteById` de `ProductoServiceImpl` en el `sales-service` y compará con tu sentencia. *(Lo dejo para el final a propósito: primero decidís vos, después te medís contra la cátedra — en ese orden se aprende a diseñar.)*

## ✅ Criterios de "Etapa 7 completa"

- [ ] PUT canónico de ambos recursos (id en ruta), por **mutación**, con relaciones sobrevivientes verificadas.
- [ ] El experimento de la huerfanización hecho ANTES de la cura — viste el bug silencioso con tus ojos.
- [ ] Idempotencia vivida: el triple-PUT vs el triple-POST, contrastados.
- [ ] DELETE de propietario con TU decisión de huérfanos implementada y justificada en comentario.
- [ ] DELETE de mascota consistente — la pregunta 7 de la Etapa 5, sentenciada y verificada cruzado.
- [ ] Recién al final: tu sentencia comparada contra la de la cátedra.

## ✅ Checkpoint

*Recall:*
1. ¿Qué rama del `save` ejecuta un update, y por qué la mutación preserva las relaciones donde la reconstrucción las mata?
2. ¿Qué es la idempotencia, cuáles de tus verbos la tienen, y a quién le resuelve un problema concreto?
3. ¿Por qué el 204 va con `void` y no con el objeto borrado?

*Decidí y justificá:*
4. El id del PUT: en la ruta (tu diseño) vs en el body (el de la clase). Defendé **ambos** en dos renglones cada uno — y decí cuál elegirías en el TP y por qué.
5. La mutabilidad de la entidad fue "el precio de la mutación". ¿Qué perdiste exactamente al sacar los `final`, y por qué la lista de mascotas NO pagó ese precio?
6. Defendé la opción de huérfanos que NO elegiste — en serio, su mejor versión. ¿Qué tipo de sistema/negocio la haría la correcta?
7. Tu compañero implementó el PUT "haciendo DELETE + POST adentro del service, total da igual". Funciona… casi. Listale todo lo que rompió (pista: id nuevo, relaciones, idempotencia, códigos).

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Abrí el `update` de `ProductoServiceImpl`: recuperar la instancia viva → `setTipo`/`setPrecioBase`/`setDescripcion` → save — **tu cura de la Parte 4, línea por línea** (y con una validación extra de pertenencia que vale leer: ¿por qué chequea que el producto sea del comercio antes de tocar nada?). Su `deleteById` ya lo comparaste al cerrar tu sentencia. La diferencia que queda es la que ya sabés defender: su PUT lleva el id en el body; el tuyo, en la ruta. Releé del recorrido **P6 §6 el comentario ⑦** (el ⚠️ del PUT): lo escribiste antes de entenderlo del todo — ahora es tuyo con intereses.

## ▶️ Próximo paso

Quedó picando la incomodidad de la Parte 5: *"yo solo quería cambiar el nombre"* — y el PUT te obligó a mandar todo, so pena de null. Existe un verbo para eso, el proyecto de la clase no lo usa, y vos lo pediste explícitamente para estar adelante: decime **"arranquemos etapa 8"** y llega **PATCH** — la actualización parcial, con la decisión de diseño más fina del proyecto: ¿qué significa exactamente que un campo *no haya venido*? (Spoiler que ya viviste: tu Etapa 3 tiene la mitad de la respuesta.)

---

**FIN DE LA ETAPA 7**
