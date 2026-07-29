# 🌱 Proyecto 0 (clase 04) — Etapa 6: El traductor — excepciones propias y el manejador global

> **Objetivo:** pagar las tres deudas: que cada error salga al mundo con **el código HTTP que corresponde** y **un cuerpo uniforme y útil** — sin un solo try/catch en los controllers. Excepciones con nombre propio + el `@RestControllerAdvice` que el recorrido te prometió.
>
> **El momento clave:** cuando la sexta mascota rebote con un **400** que dice *"Un propietario no puede tener más de 5 mascotas"* — y entiendas la cadena completa: el dominio gritó en su idioma, el service tradujo, el manejador convirtió, el cliente entendió. Cuatro piezas, cada una en su rol.
>
> **Pre-requisito:** Etapa 5 completa (con las tres deudas anotadas — son el plano de hoy).
>
> **Tiempo estimado:** 50-60 minutos.
>
> **Andamiaje:** 🟨 — el mecanismo nuevo va guiado; el recableado de tus services es tuyo, con especificación.

---

## 🗂️ Dónde estamos — los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── controllers/
    │   ├── PropietarioController.java           (NO se toca — y eso es el punto)
    │   ├── MascotaController.java               (NO se toca — ídem)
    │   └── advice/                              (📦 NUEVO sub-package)
    │       └── GlobalExceptionHandler.java      (NUEVO — Parte 4)
    ├── dtos/
    │   └── error/                               (📦 NUEVO sub-package)
    │       └── ErrorResponse.java               (NUEVO — Parte 3)
    ├── exceptions/                              (📦 NUEVO package)
    │   ├── ResourceNotFoundException.java       (NUEVO — Parte 2)
    │   └── BusinessException.java               (NUEVO — Parte 2)
    └── services/impl/
        ├── PropietarioServiceImpl.java          (SE TOCA — Parte 5)
        └── MascotaServiceImpl.java              (SE TOCA — tu turno)
```

Releé la primera anotación del árbol: **los controllers no se tocan**. Todo el sistema de errores se instala sin que la capa de presentación se entere — al final de la etapa vas a entender por qué eso es una victoria de diseño y no una casualidad.

---

## 🧭 Mapa de esta etapa

1. El plano: tus tres deudas.
2. Pieza 1: excepciones con nombre propio.
3. Pieza 2: el error uniforme (`ErrorResponse`).
4. Pieza 3: el traductor (`@RestControllerAdvice`).
5. El recableado de Propietario (guiado).
6. Experimento 1: la red de seguridad… ¿ya alcanza?
7. La traducción deliberada (la perla del recorrido, en tus manos).
8. Experimento 2: el 500 opaco.
9. Tu turno: Mascota recableada + la batería de las tres deudas.
10. Criterios + checkpoint + registro + 🔗.

---

## 🗺️ Parte 1: El plano — tus tres deudas

Tu respuesta a la pregunta 8 de la Etapa 5 ES la especificación de hoy:

| # | Qué pasa | Dónde nace | Sale como | Debería salir como |
|---|---|---|---|---|
| 1 | Nombre vacío / cantidad -5 | validación de **flujo** (service) | 500 | **400** — culpa del cliente |
| 2 | Propietario/mascota 99 | `orElseThrow()` pelado (service) | 500 | **404** — el recurso no existe |
| 3 | Sexta mascota | regla de **negocio** (dominio) | 500 | **400** — regla violada |

Tres orígenes distintos, una sola mentira: todo sale 500. El problema no es que falten validaciones — funcionan todas. Falta **el traductor**: la pieza que convierte cada grito interno en el idioma HTTP. Hoy la construís en tres piezas.

## 🧨 Parte 2: Pieza 1 — excepciones con nombre propio

```java
// 📁 src/main/java/.../veterinaria/exceptions/ResourceNotFoundException.java   (archivo NUEVO)
package com.practica.veterinaria.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
```

```java
// 📁 src/main/java/.../veterinaria/exceptions/BusinessException.java   (archivo NUEVO)
package com.practica.veterinaria.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
```

Seis líneas cada una, y dos decisiones adentro que valen oro:

**¿Por qué heredan de `RuntimeException`?** Porque son **no chequeadas** (seminario de Java, ahora con propósito): no obligan a try/catch ni a declararse en firmas — por eso tus services pueden lanzarlas y **patear hacia arriba sin ensuciarse**, que es exactamente su rol según el recorrido (P4 §4: *"el service lanza; no maneja"*). Si fueran chequeadas, cada firma de cada capa cargaría con `throws`, y el controller estaría obligado a atrapar: adiós delegación limpia.

**¿Por qué nombres propios, si `IllegalArgumentException` "funcionaba"?** Porque el traductor que viene despacha **por tipo**: para responder 404 a una cosa y 400 a otra, las cosas tienen que ser **tipos distintos**. Con excepciones genéricas, todos los errores son la misma cosa y no hay traducción posible. El nombre ES la información.

## 📦 Parte 3: Pieza 2 — el error uniforme

```java
// 📁 src/main/java/.../veterinaria/dtos/error/ErrorResponse.java   (archivo NUEVO)
package com.practica.veterinaria.dtos.error;

import java.time.Instant;

public record ErrorResponse(String error, String message, Instant timestamp) { }
// ↑ error: una etiqueta corta y estable ("not_found", "bad_request") que un
//   programa puede switchear · message: la frase para humanos · timestamp:
//   cuándo (Instant = el "ahora" universal de Java, en UTC).
```

Un record más — la Etapa 3 trabajando. La decisión importante es la política: **TODOS los errores del sistema van a salir con esta única forma.** Consumir una API cuyos errores son uniformes es un placer; consumir una donde cada error tiene su formato es un via crucis. Vos ya lo padeciste como consumidor en la clase 2 — hoy elegís el placer para tus clientes.

## 🛠️ Parte 4: Pieza 3 — el traductor

```java
// 📁 src/main/java/.../veterinaria/controllers/advice/GlobalExceptionHandler.java   (archivo NUEVO)
package com.practica.veterinaria.controllers.advice;

import com.practica.veterinaria.dtos.error.ErrorResponse;
import com.practica.veterinaria.exceptions.BusinessException;
import com.practica.veterinaria.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // ↑ "Esta clase atrapa lo que ESCAPE de cualquier controller." Registro
    //   automático: la anotación alcanza — nadie la conecta a mano, ningún
    //   controller sabe que existe. Por eso hoy no tocaste ni un controller.

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage());
    }
    // ↑ "Toda excepción DE ESTE TIPO que escape, cae acá" → 404. El despacho
    //   es POR TIPO: por eso las excepciones necesitaban nombre propio.

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage());
    }
    // ↑ ¡Las LLAVES de la gramática, regla 3! El atributo es Class[] — este
    //   handler atrapa DOS tipos. ¿Por qué también IllegalArgumentException?
    //   Red de seguridad: si alguna escapa sin traducir, al menos sale 400.
    //   (El Experimento 1 le saca punta a esta decisión.)

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
                     "Ocurrió un error interno");
    }
    // ↑ La red final: TODO lo no previsto → 500 con mensaje OPACO a propósito.
    //   El Experimento 2 te muestra por qué la opacidad acá es una virtud.

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(error, message, Instant.now()));
    }
    // ↑ La fábrica de respuestas: status elegido + tu DTO uniforme como body.
}
```

Las fichas del día:

> 🎛️ **Ficha `@RestControllerAdvice`:** sin atributos que necesites — por defecto cubre **todos** los controllers. Existen `basePackages` / `assignableTypes` para limitar su alcance a algunos (solo reconocer).

> 🎛️ **Ficha `@ExceptionHandler`:** `value` = `Class[]` — un tipo (`X.class`) o varios con llaves (`{X.class, Y.class}`). 👀 Si lo ves **vacío** (`@ExceptionHandler` pelado), no está roto: deduce el tipo del parámetro del método.

Y una pieza que no es anotación pero pide su minuto — **`ResponseEntity`**: el 👀 del recorrido, hoy ascendido a canónico. Es *"una respuesta HTTP completa, armada a mano"*: `ResponseEntity.status(404).body(miDto)` controla status + body por camino de ejecución — lo que `@ResponseStatus` (fijo por método) no puede. En los endpoints felices seguís con el DTO pelado + `@ResponseStatus`; acá, donde el status **depende** de qué excepción llegó, `ResponseEntity` es la herramienta correcta. Atajos que vas a ver: `ResponseEntity.ok(dto)`, `.notFound().build()`.

## 🔌 Parte 5: El recableado de Propietario (guiado)

Dos retoques quirúrgicos en `PropietarioServiceImpl` — **predicción antes:** ¿algún import nuevo? ¿alguna firma cambia?

```java
// 📁 services/impl/PropietarioServiceImpl.java   (SE TOCA — dos retoques)

// Retoque 1 — la validación del create, con nombre propio:
if (request == null || request.nombre() == null || request.nombre().isBlank()) {
    throw new BusinessException("El nombre es obligatorio");     // era IllegalArgument...
}

// Retoque 2 — el orElseThrow, jubilado de su versión pelada:
private Propietario getPropietarioOrThrow(Long id) {
    return propietarioRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No se encontró propietario con id " + id));
    // ↑ La lambda fabrica TU excepción, con TU mensaje, solo si el Optional
    //   vino vacío. La línea completa del recorrido, por fin en tu archivo.
}
```

Firmas intactas, controllers intactos. Levantá y cobrá las dos primeras deudas:

- `GET /veterinaria/propietarios/99` → **404** `{"error":"not_found","message":"No se encontró propietario con id 99","timestamp":"..."}` — la deuda 2, **pagada**.
- `POST` con `{"nombre":""}` → **400** `bad_request` con tu mensaje — la deuda 1, **pagada**.

Mirá lo que acaba de pasar en cámara lenta: la excepción nació en el service, **nadie** la atrapó en el camino, el framework la derivó al advice, el handler del tipo correcto la tradujo, y salió con tu forma uniforme. Sin try/catch en ningún lado tuyo.

## 🧨 Parte 6: Experimento 1 — la red de seguridad… ¿ya alcanza?

Queda la deuda 3 — la sexta mascota, cuyo grito (`IllegalArgumentException`) nace en el **dominio**. **Predicción:** con el advice instalado, y SIN tocar nada más… ¿la sexta mascota sigue saliendo 500, o algo cambió?

Probá (cargale 5 mascotas a Ana y mandá la sexta): **400** — ¡la atrapó la red de seguridad! El handler de las llaves incluía `IllegalArgumentException`, y el grito del dominio cayó ahí de rebote. Deuda 3… ¿pagada?

**A medias — y acá está la lección fina.** Funciona, pero por accidente estructural: cualquier `IllegalArgumentException` de CUALQUIER origen — incluida una que revele un **bug** tuyo o de una biblioteca — va a salir etiquetada `bad_request`, culpando al cliente por errores que son tuyos. La red existe para los **escapes imprevistos**; las reglas **deliberadas** del negocio merecen traducción **deliberada**. Que es exactamente lo que hace el proyecto de la clase — y lo que hacés ahora.

## 💎 Parte 7: La traducción deliberada

La perla del recorrido (P6 §8), en tus manos — el **único try/catch de todo tu proyecto**, en el `create` de `MascotaServiceImpl`:

```java
// 📁 services/impl/MascotaServiceImpl.java   (el paso "asociar" del create)
try {
    propietario.agregarMascota(mascota);
} catch (IllegalArgumentException ex) {
    throw new BusinessException(ex.getMessage());
}
// ↑ LA TRADUCCIÓN: el dominio habló su idioma (IllegalArgumentException —
//   no conoce las excepciones del sistema: ¡árbol!); el service, que es
//   quien SABE que este grito es una regla de negocio esperada, lo traduce
//   al idioma del flujo (BusinessException). Cada capa su lengua; el
//   orquestador interpreta. La red de seguridad queda para lo que es:
//   imprevisto de verdad.
```

Verificá: sexta mascota → **400** `bad_request`, *"Un propietario no puede tener más de 5 mascotas"*. Mismo resultado visible que en el Experimento 1 — **pero ahora por diseño, no por rebote**. La cadena completa del momento clave: dominio grita → service traduce → advice convierte → cliente entiende. Deuda 3: pagada con honores.

*(Observación al margen que ya viviste sin nombrarla: cuando tiraste la `IllegalArgumentException` cruda, la atrapó el handler de las llaves y NO el de `Exception` — aunque ambos matcheaban. El despacho del advice elige **el tipo más específico**: la misma regla de especificidad que las rutas de la Etapa 2. Tercera aparición de la misma filosofía.)*

## 🧨 Parte 8: Experimento 2 — el 500 opaco

Falta probar la red final. Plantá una bomba temporal en cualquier service:

```java
// (temporal — en el findAll de propietarios, por ejemplo)
if (true) throw new RuntimeException("boom interno: conexionDB=10.0.0.5, password=secreta123");
```

**Predicción:** ¿qué recibe el cliente — tu mensaje con la bomba, o otra cosa? ¿Y la consola?

Probá el GET: el cliente recibe **500** `{"error":"internal_error","message":"Ocurrió un error interno",...}` — **ni rastro de tu mensaje**. La consola, en cambio, tiene la verdad completa con stacktrace. Esa asimetría es la virtud: los detalles internos de un error imprevisto (rutas, IPs, credenciales, estructura de tu código) **no se le cuentan al mundo** — son munición para un atacante y ruido para un cliente. Adentro, verdad plena para depurar; afuera, opacidad cortés. Sacá la bomba.

👀 *Un fleco que quizás notaste: el JSON roto (Etapa 3) sigue dando 400 con el body **de fábrica** — dos errores 400 con formas distintas, porque esa excepción (`HttpMessageNotReadableException`) revienta ANTES de tus controllers y tu advice no la lista. Podés uniformarla sumándole un handler — el proyecto de la clase no lo hace; queda como decisión tuya, extensión consciente si la querés.*

## ✍️ Parte 9: Tu turno — Mascota recableada + la batería

Terminá el recableado de `MascotaServiceImpl`: todas las validaciones de flujo → `BusinessException` · los dos `orElseThrow` (el del propietario al crear, el del `getMascotaOrThrow` y el del `findPropietarioOwner`) → `ResourceNotFoundException` con mensajes específicos · la traducción de la Parte 7 en su lugar. Sin más indicaciones.

**La batería de verificación** (corrida completa — el examen de la etapa):

| Request | Esperado |
|---|---|
| `GET /propietarios/99` | **404** · `not_found` · mensaje con el id |
| `POST /propietarios` `{"nombre":""}` | **400** · `bad_request` |
| `POST /mascotas` con `propietarioId: 99` | **404** · mensaje del propietario |
| `GET /mascotas/99` | **404** · mensaje de la mascota |
| Sexta mascota de Ana | **400** · mensaje de la regla |
| `POST /mascotas` sin `nombre` | **400** · tu validación |
| JSON roto | **400** · body de fábrica (o el tuyo, si hiciste la extensión) |
| Todo lo feliz de siempre | sigue **200/201** intacto |

Ocho de ocho = traductor operativo, deudas saldadas.

## ✅ Criterios de "Etapa 6 completa"

- [ ] Las tres piezas construidas: excepciones propias, `ErrorResponse`, advice — y **ningún controller tocado**.
- [ ] Los dos experimentos hechos con predicción; la bomba plantada, observada y retirada.
- [ ] Podés explicar la diferencia entre "la red lo atrapó" y "lo traduje a propósito" — y por qué importa.
- [ ] El único try/catch del proyecto está donde está, y sabés defenderlo con el árbol.
- [ ] La batería de ocho, en verde.

## ✅ Checkpoint

*Recall:*
1. ¿Por qué las excepciones propias son no-chequeadas? ¿Qué se rompería del diseño si fueran chequeadas?
2. ¿Cómo decide el advice qué handler ejecutar, y qué regla aplica cuando más de uno matchea?
3. ¿Por qué los controllers no se enteraron de NADA de lo de hoy? ¿Qué mecanismo lo hizo posible?

*Decidí y justificá:*
4. "El mensaje opaco del 500 es mala experiencia de usuario — mostrale el error real y listo." Refutá con los dos argumentos (seguridad y utilidad), y explicá dónde SÍ vive la verdad completa.
5. La red atrapaba la regla de las 5 y daba el 400 correcto. ¿Por qué igual escribiste la traducción? Da el escenario concreto donde la red-sin-traducción te etiqueta mal un bug.
6. ¿Dónde mapearías una futura `PermisoInsuficienteException` (recorrido P4 §5) y con qué código? ¿Y una `ConflictException` para "el nombre de usuario ya existe"? *(La segunda existe en el proyecto de la clase — chequeá tu respuesta contra su advice.)*
7. Tu compañero puso try/catch en cada método del controller "para estar seguro". Funciona. Listale todo lo que perdió respecto de tu diseño de hoy.
8. El `ErrorResponse` lleva `error` (etiqueta) Y `message` (frase). ¿Por qué los dos, si "dicen lo mismo"? ¿Quién consume cada uno?

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Abrí el `GlobalExceptionHandler` del `sales-service`: a esta altura **es tu archivo con otros nombres** — mismos handlers, mismo build, misma opacidad del 500, más un `ConflictException → 409` que hoy pensaste en el checkpoint. Después andá a `VentaServiceImpl` y buscá **el único try/catch de SU proyecto**: la traducción de `comercio.agregarVenta` — tu Parte 7, en producción, con la misma estructura exacta. Releé del recorrido **P6 §7 y §8 completos**: ya no deberían explicarte — deberían sonarte a espejo. La diferencia estructural entre tu proyecto y el de la cátedra, a partir de hoy: **ninguna que importe.**

## ▶️ Próximo paso

Tu API crea, lista, busca y se equivoca con elegancia… pero nada se puede **corregir** ni **borrar**: un propietario mal tipeado es eterno (hasta el próximo `Ctrl+C`, al menos). Decime **"arranquemos etapa 7"** y llegan **PUT y DELETE canónicos** — con el id en la ruta como REST manda, el 204 sin cuerpo, y una deuda vieja esperándote: la pregunta 7 de la Etapa 5 (la mascota borrada que sigue viva en la lista de su dueño) por fin se decide.

---

**FIN DE LA ETAPA 6**
