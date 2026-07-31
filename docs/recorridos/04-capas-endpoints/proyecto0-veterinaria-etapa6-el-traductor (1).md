# 🌱 Proyecto 0 (clase 04) — Etapa 6: El traductor — excepciones propias y el manejador global

> **Objetivo:** que cada error salga al mundo con **el código HTTP que corresponde** y **un cuerpo uniforme** — sin un solo try/catch en los controllers.
>
> **Cómo trabaja esta etapa (v2):** primero el sistema **COMPLETO** — todas las piezas, el árbol, y la matriz de "cuándo se lanza cada cual" — y recién cuando tengas el mapa entero en la cabeza, construimos pieza por pieza. Nada provisorio, nada que después haya que desaprender.
>
> **Pre-requisito:** Etapa 5 completa.
>
> **Tiempo estimado:** 50-60 minutos.

---

## 🗺️ El mapa general — dónde estamos parados

Tu arquitectura hasta hoy, y lo que esta etapa le agrega (marcado con ★):

```
              REQUEST                                RESPONSE
                 │                                       ▲
                 ▼                                       │
        ┌─────────────────┐   si una excepción   ┌──────────────────────┐
        │   CONTROLLERS   │   escapa de acá ───► │ ★ GlobalException-   │
        │ (no se tocan    │                      │   Handler (advice)   │
        │  en toda la     │                      │   la traduce a:      │
        │  etapa)         │                      │   status correcto +  │
        └────────┬────────┘                      │ ★ ErrorResponse      │
                 ▼                               └──────────────────────┘
        ┌─────────────────┐
        │    SERVICES     │ ◄── acá se cambian ~6 líneas: lanzar
        │                 │     ★ excepciones PROPIAS en vez de genéricas
        └───┬─────────┬───┘
            ▼         ▼
   ┌────────────┐ ┌──────────────┐
   │  DOMINIO   │ │ REPOSITORIES │
   │ (NO se     │ │ (no se       │
   │  toca      │ │  tocan)      │
   │  JAMÁS)    │ └──────────────┘
   └────────────┘
```

Leelo dos veces, porque acota la etapa entera: **se crean 4 archivos nuevos** (2 excepciones + 1 DTO de error + el advice), **se retocan ~6 líneas en 2 services**, y **todo lo demás — controllers, dominio, repos — queda intacto**. Eso es todo el movimiento de hoy.

## 🗂️ Los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── controllers/
    │   ├── PropietarioController.java           (NO se toca)
    │   ├── MascotaController.java               (NO se toca)
    │   └── advice/                              (📦 NUEVO sub-package)
    │       └── GlobalExceptionHandler.java      ★ NUEVO
    ├── dtos/
    │   └── error/ErrorResponse.java             ★ NUEVO
    ├── exceptions/                              (📦 NUEVO package)
    │   ├── ResourceNotFoundException.java       ★ NUEVO
    │   └── BusinessException.java               ★ NUEVO
    ├── models/entities/                         (NO SE TOCA — importante, ver Parte 1)
    └── services/impl/
        ├── PropietarioServiceImpl.java          (SE TOCA: 2 líneas)
        └── MascotaServiceImpl.java              (SE TOCA: ~4 líneas — tu turno)
```

## 🧭 Mapa de esta etapa

1. **EL SISTEMA COMPLETO** (todo el mapa mental, antes de una sola línea de código).
2. Construcción pieza 1: las excepciones propias.
3. Construcción pieza 2: el error uniforme.
4. Construcción pieza 3: el traductor.
5. El recableado (inventario exacto + Propietario guiado).
6. Experimento 1: la red de seguridad… ¿ya alcanza?
7. La traducción deliberada (el único try/catch del proyecto).
8. Experimento 2: el 500 opaco.
9. Tu turno: Mascota + la batería final.
10. Criterios + checkpoint + registro + 🔗.

---

## 🧠 Parte 1: EL SISTEMA COMPLETO (leé toda esta parte antes de tocar código)

### 1a. El problema que venimos a resolver

Las tres deudas que anotaste en la Etapa 5 — con la request exacta que provoca cada una, para que no dependas de la memoria:

| # | La provocás con… | Nace en… | Hoy sale | Debería salir |
|---|---|---|---|---|
| 1 | `POST /propietarios` con `{"nombre":""}` | validación del service | 500 | **400** (culpa del cliente) |
| 2 | `GET /propietarios/99` | el `orElseThrow()` pelado del service | 500 | **404** (no existe) |
| 3 | la 6ª mascota del mismo propietario | la regla en `Propietario.agregarMascota` | 500 | **400** (regla violada) |

Tres orígenes, una mentira: todo sale 500. Falta **el traductor**.

### 1b. El árbol de HERENCIA — de dónde viene cada excepción

```
Throwable
└── Exception ················· CHEQUEADAS: te obligan a try/catch o `throws`
    └── RuntimeException ······ NO CHEQUEADAS: vuelan libres hacia arriba
        ├── IllegalArgumentException      ← de Java: "argumento inaceptable"
        │                                    (la que lanza tu dominio)
        ├── NoSuchElementException        ← de Java: la de tu orElseThrow()
        │                                    pelado de la Etapa 5
        ├── ResourceNotFoundException   ★ ← TUYA (la creás hoy)
        └── BusinessException           ★ ← TUYA (la creás hoy)
```

Dato clave del árbol: **las cuatro son `RuntimeException`** → ninguna obliga a try/catch → los services pueden lanzarlas y **patear hacia arriba sin ensuciarse**. Si tus excepciones heredaran de `Exception` a secas, cada firma de cada capa cargaría un `throws` y el controller estaría obligado a atrapar. Por eso heredan de donde heredan.

### 1c. Las cuatro piezas y cómo juegan juntas

El sistema completo, funcionando (esto es lo que vas a tener al final de la etapa):

```
  SERVICE lanza                    NADIE la atrapa              ADVICE la traduce
  ─────────────                    en el camino                 ─────────────────
  ResourceNotFoundException ────────────────────────────────►  404 + ErrorResponse
  BusinessException         ────────────────────────────────►  400 + ErrorResponse
  (cualquier otra cosa)     ────────────────────────────────►  500 + ErrorResponse
                                                                     (mensaje opaco)
```

Las piezas: **dos excepciones propias** (el vocabulario), **un record `ErrorResponse`** (la forma única en que TODO error sale al mundo), y **el `GlobalExceptionHandler`** (el traductor: atrapa por tipo lo que escape de cualquier controller y fabrica la respuesta).

### 1d. LA MATRIZ — cuándo se lanza cada cual (el corazón de la etapa)

Esta tabla responde de una vez la pregunta "¿qué excepción va acá?". La regla madre:

> **La excepción no la elige el evento — la elige LA CAPA donde estás parado.** El mismo problema ("me pasaron basura") se expresa distinto según quién habla, porque cada capa tiene un vocabulario disponible distinto.

| ¿DÓNDE estás parado? | ¿Qué pasó? | Lanzás | Termina en |
|---|---|---|---|
| **ENTIDAD** (`models/`) | cualquier regla violada | `IllegalArgumentException` — **la única que puede**: el dominio no importa nada del sistema | 400 (vía traducción) |
| **SERVICE** | lo que la request *nombra* no existe (un id) | `ResourceNotFoundException` | **404** |
| **SERVICE** | los datos o una regla de flujo no valen | `BusinessException` | **400** |
| **SERVICE** | el dominio gritó (te llegó su `IllegalArgumentException`) | try/catch → relanzás **`BusinessException`** (la traducción — Parte 7) | **400** |
| **CONTROLLER** | — | **NADA. Jamás lanza, jamás atrapa.** | — |

Dos aclaraciones que evitan los nudos clásicos:

**«¿Por qué el service lanza `BusinessException` si "nombre vacío" ES literalmente un argumento ilegal?»** Porque semánticamente tenés razón — lo es — pero el service usa su vocabulario propio por **señalización**: `BusinessException` llegando al advice significa *"regla deliberada mía"*; `IllegalArgumentException` llegando significa *"algo se escapó sin traducir"* (por eso el advice la atrapa igual, como red). Si el service usara la genérica, esa distinción moriría. En comportamiento observable, hoy, ambas dan el mismo 400 — la diferencia es de **intención declarada**, no de resultado.

**«¿Por qué la entidad no lanza `BusinessException` directo y nos ahorramos el try/catch?»** Porque compilar, compila — pero mirá el árbol de CONOCIMIENTO (no confundir con el de herencia de arriba; son dos árboles distintos):

```
capa                    puede importar…
─────────────────────────────────────────────────────────────
controllers + advice →  services, dtos, exceptions
services             →  entities, repositories, dtos, exceptions
models/entities      →  NADA del sistema (ni siquiera exceptions/) ◄── acá
```

Para lanzar `BusinessException`, `Propietario.java` necesitaría importarla → el dominio pasaría a **conocer una pieza del sistema** → deja de ser portable y cada cambio en `exceptions/` tocaría entidades. El try/catch del service es **el precio de la pureza del dominio**. (La cátedra paga ese precio; existe la alternativa de una excepción que viva dentro del propio dominio — diseño válido que la cátedra no usa y nosotros tampoco.)

**Con este mapa completo en la cabeza, recién ahora: a construir.** Nada de lo que sigue te va a pedir desaprender nada de esta parte.

---

## 🛠️ Parte 2: Pieza 1 — las excepciones propias

📍 **Dónde estamos:**

```
Controllers ──► Services ──► Repositories
                   │
                   ▼                      exceptions/  ◄━━ ACÁ (package nuevo,
                Dominio                                     2 archivos de 6 líneas)
```

```java
// 📁 src/main/java/.../veterinaria/exceptions/ResourceNotFoundException.java   (NUEVO)
package com.practica.veterinaria.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
```

```java
// 📁 src/main/java/.../veterinaria/exceptions/BusinessException.java   (NUEVO)
package com.practica.veterinaria.exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}
```

Seis líneas cada una: heredan de `RuntimeException` (el porqué está en el árbol 1b) y solo cargan un mensaje. ¿Por qué existir, si "no hacen nada"? Porque el traductor despacha **por tipo**: para responder 404 a una cosa y 400 a otra, tienen que SER tipos distintos. El nombre mismo **es** la información.

## 📦 Parte 3: Pieza 2 — el error uniforme

📍 Mismo mapa, ahora en `dtos/error/` — un record, tu especialidad desde la Etapa 3:

```java
// 📁 src/main/java/.../veterinaria/dtos/error/ErrorResponse.java   (NUEVO)
package com.practica.veterinaria.dtos.error;

import java.time.Instant;

public record ErrorResponse(String error, String message, Instant timestamp) { }
// error: etiqueta corta y estable ("not_found") — para que un PROGRAMA switchee.
// message: la frase — para HUMANOS.  timestamp: cuándo (Instant = "ahora" en UTC).
```

La decisión importante no es el record — es la política: **todos los errores del sistema, de cualquier tipo, salen con esta única forma.**

## 🛠️ Parte 4: Pieza 3 — el traductor

📍 **Dónde estamos:**

```
Controllers ──► Services ──► ...
     │
     └──► advice/GlobalExceptionHandler  ◄━━ ACÁ (lo que escape de CUALQUIER
                                              controller, cae en esta clase)
```

```java
// 📁 src/main/java/.../veterinaria/controllers/advice/GlobalExceptionHandler.java   (NUEVO)
package com.practica.veterinaria.controllers.advice;

import com.practica.veterinaria.dtos.error.ErrorResponse;
import com.practica.veterinaria.exceptions.BusinessException;
import com.practica.veterinaria.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice                // "atrapo lo que ESCAPE de cualquier
public class GlobalExceptionHandler {//  controller" — registro automático:
                                     //  nadie lo conecta, ningún controller
                                     //  sabe que existe.

    @ExceptionHandler(ResourceNotFoundException.class)   // este TIPO → este método
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage());      // → 404
    }

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage());  // → 400
    }
    // ↑ Las llaves = array (tu gramática, regla 3): UN handler para DOS tipos.
    //   Se agrupan POR RESPUESTA, no por origen: ambos merecen el mismo 400.
    //   ¿IllegalArgumentException acá? RED DE SEGURIDAD: si alguna se escapa
    //   sin traducir, al menos sale 400 (Parte 6 le saca punta a esto).

    @ExceptionHandler(Exception.class)                   // TODO lo no previsto:
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
                     "Ocurrió un error interno");        // mensaje OPACO a propósito
    }                                                    // (Parte 8 muestra por qué)

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(error, message, Instant.now()));
    }   // ↑ la fábrica: status elegido + tu forma uniforme como body
}
```

**¿Cómo elige handler cuando llega una excepción?** Por tipo, y si más de uno matchea, **gana el más específico** — mirá el caso que importa:

```
  llega una IllegalArgumentException
        │
        ├── ¿matchea handleBadRequest?  SÍ (está en las llaves, tipo exacto)
        ├── ¿matchea handleUnexpected?  SÍ (ES-UN Exception, por el árbol 1b)
        │
        └──► gana el EXACTO → handleBadRequest → 400
             (la misma regla de especificidad que las rutas de la Etapa 2:
              /eco/especial le ganaba a /eco/{palabra})
```

> 🎛️ **Ficha `@RestControllerAdvice`:** sin atributos necesarios — cubre todos los controllers (existen `basePackages`/`assignableTypes` para limitarlo: solo reconocer). **Ficha `@ExceptionHandler`:** `value` = `Class[]` — un tipo o varios con llaves; 👀 vacío no está roto: deduce el tipo del parámetro.

**`ResponseEntity`** (no es anotación — es el tipo de retorno): *"una respuesta HTTP completa armada a mano"* — `status(...).body(...)`. Acá es la herramienta correcta porque el status **depende** de qué excepción llegó (lo que `@ResponseStatus`, fijo por método, no puede). En endpoints felices seguís como siempre: DTO pelado + `@ResponseStatus`.

## 🔌 Parte 5: El recableado — inventario exacto primero

Todo lo que cambia en código EXISTENTE, completo, antes de tocar nada:

| Archivo | Cambia | Qué |
|---|---|---|
| `Propietario.java` / `Mascota.java` | **NADA — jamás** | su `IllegalArgumentException` es correcta y definitiva (matriz 1d, fila 1) |
| `PropietarioServiceImpl` | 2 líneas | validación → `BusinessException` · `orElseThrow()` → con `ResourceNotFoundException` |
| `MascotaServiceImpl` | ~4 líneas (tu turno) | ídem + envolver UNA línea en el try/catch |
| Controllers, repos, DTOs | **NADA** | — |

📍 **Propietario, retoque 1** — la validación del `create`:

```
┌─ 📁 services/impl/PropietarioServiceImpl.java ─────────────────┐
│  public PropietarioResponse create(...) {                      │
│  ╔════════════════════════════════════════════════╗           │
│  ║ if (request == null || ... isBlank()) {        ║ ◄── cambia │
│  ║     throw new BusinessException(               ║     SOLO   │
│  ║         "El nombre es obligatorio");           ║     esta   │
│  ║ }                                              ║     línea  │
│  ╚════════════════════════════════════════════════╝     (era   │
│     ...instanciar, save, toResponse (sin cambios)...  IllegalA.)│
│  }                                                             │
└────────────────────────────────────────────────────────────────┘
```

📍 **Propietario, retoque 2** — el `orElseThrow` pelado (el que en la Etapa 5 lanzaba `NoSuchElementException`, la genérica de Java que salía como 500):

```
┌─ 📁 services/impl/PropietarioServiceImpl.java ─────────────────┐
│  private Propietario getPropietarioOrThrow(Long id) {          │
│  ╔════════════════════════════════════════════════╗            │
│  ║ return propietarioRepository.findById(id)      ║            │
│  ║     .orElseThrow(() ->                         ║ ◄── el ()  │
│  ║         new ResourceNotFoundException(         ║     pelado │
│  ║           "No se encontró propietario con id " ║     gana   │
│  ║           + id));                              ║     lambda │
│  ╚════════════════════════════════════════════════╝     y      │
│  }                                                    mensaje  │
└────────────────────────────────────────────────────────────────┘
```

Levantá y **cobrá las dos primeras deudas** (verificalas con las requests exactas de la tabla 1a): el `GET /99` → **404** con tu `ErrorResponse` · el nombre vacío → **400**. Sin try/catch en ningún lado tuyo: el service lanzó, nadie atrapó, el advice tradujo.

## 🧨 Parte 6: Experimento 1 — la red de seguridad… ¿ya alcanza?

Queda la deuda 3 — la 6ª mascota, cuyo grito (`IllegalArgumentException`) nace en el dominio. **Predicción:** con el advice instalado y SIN tocar nada más… ¿sigue saliendo 500?

Probá (5 mascotas a Ana, mandá la 6ª): **400** — la atrapó la red (la `IllegalArgumentException` estaba en las llaves). ¿Deuda pagada? **A medias, y la diferencia es la lección:** funciona *por rebote*. Cualquier `IllegalArgumentException` de CUALQUIER origen — incluida la de un **bug** tuyo o de una biblioteca — va a salir etiquetada `bad_request`, culpando al cliente por errores tuyos. La red es para **escapes imprevistos**; las reglas **deliberadas** merecen traducción **deliberada**.

## 💎 Parte 7: La traducción deliberada — el único try/catch del proyecto

📍 **Dónde estamos:**

```
Controllers ──► Services ◄━━ ACÁ ──► Repositories
                   │
                   ▼
                Dominio  (de acá SUBE el grito que vamos a traducir)
```

```
┌─ 📁 services/impl/MascotaServiceImpl.java ──────────────────┐
│  public MascotaResponse create(MascotaCreateRequest r) {    │
│     ...validaciones (sin cambios)...                        │
│     ...resolver propietario por id (sin cambios)...         │
│     ...new Mascota (sin cambios)...                         │
│  ╔══════════════════════════════════════════╗               │
│  ║ try {                                    ║ ◄── SOLO esto │
│  ║     propietario.agregarMascota(mascota); ║     cambia:   │
│  ║ } catch (IllegalArgumentException ex) {  ║     la línea  │
│  ║     throw new BusinessException(         ║     que ya    │
│  ║         ex.getMessage());                ║     tenías,   │
│  ║ }                                        ║     envuelta  │
│  ╚══════════════════════════════════════════╝               │
│     ...save y return (sin cambios)...                       │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘
```

La cadena completa, que es la fila 4 de tu matriz funcionando:

```
Propietario.agregarMascota          MascotaServiceImpl              Advice
──────────────────────────          ──────────────────              ──────
throw IllegalArgumentException ──►  catch → throw            ──►    handler exacto
("6ª mascota" — idioma Java,        BusinessException                → 400 +
 lo único que el dominio habla)     (idioma del sistema:             ErrorResponse
                                     "regla deliberada")
```

Verificá: la 6ª → **400** con el mensaje de la regla. Mismo resultado visible que en el Experimento 1 — **pero ahora por diseño, no por rebote**. Deuda 3, pagada con honores.

## 🧨 Parte 8: Experimento 2 — el 500 opaco

Plantá una bomba temporal en cualquier service:

```java
// (temporal — primera línea del findAll de propietarios, por ejemplo)
if (true) throw new RuntimeException("boom interno: db=10.0.0.5, password=secreta123");
```

**Predicción:** ¿el cliente recibe tu mensaje con la bomba, o otra cosa? ¿Y la consola?

Probá el GET: el cliente recibe **500** con *"Ocurrió un error interno"* — **ni rastro de tu mensaje**; la consola tiene la verdad completa con stacktrace. Esa asimetría es la virtud: los detalles internos de un error imprevisto son munición para un atacante y ruido para un cliente — **verdad plena adentro (para depurar), opacidad cortés afuera**. Sacá la bomba.

👀 *Un fleco: el JSON con sintaxis rota (Etapa 3: la comilla que le sacaste al body — daba 400 con `HttpMessageNotReadableException`) SIGUE saliendo con el body de fábrica, no con tu `ErrorResponse`: esa excepción revienta ANTES de llegar a tus controllers y tu advice no la lista. Podés uniformarla con un handler más — la cátedra no lo hace; decisión tuya, extensión consciente.*

## ✍️ Parte 9: Tu turno — Mascota + la batería

Aplicá el inventario en `MascotaServiceImpl` con la matriz 1d al lado: validaciones → `BusinessException` · los `orElseThrow` (propietario al crear, `getMascotaOrThrow`, `findPropietarioOwner`) → `ResourceNotFoundException` con mensajes específicos · la traducción de la Parte 7 en su lugar.

**La batería de la etapa** (corrida completa):

| Request | Esperado |
|---|---|
| `GET /propietarios/99` | **404** · `not_found` · mensaje con el id |
| `POST /propietarios` `{"nombre":""}` | **400** · `bad_request` |
| `POST /mascotas` con `propietarioId: 99` | **404** · mensaje del propietario |
| `GET /mascotas/99` | **404** · mensaje de la mascota |
| 6ª mascota de Ana | **400** · mensaje de la regla |
| `POST /mascotas` sin `nombre` | **400** · tu validación |
| JSON con sintaxis rota | **400** · body de fábrica (o el tuyo, si hiciste la extensión) |
| Todo lo feliz de siempre | **200/201** intacto |

## ✅ Criterios de "Etapa 6 completa"

- [ ] Leíste la Parte 1 ENTERA antes de codear — y la matriz 1d te cierra sola.
- [ ] Las 4 piezas nuevas construidas; controllers y dominio, intactos.
- [ ] Los dos experimentos con predicción; la bomba plantada, observada, retirada.
- [ ] El único try/catch del proyecto en su lugar, y sabés defenderlo con los DOS árboles.
- [ ] Batería 8/8.

## ✅ Checkpoint

*Recall:*
1. Recitá la matriz: parado en la entidad / en el service (3 casos) / en el controller — ¿qué se lanza en cada situación y por qué?
2. ¿Cuáles son los DOS árboles de esta etapa, y qué pregunta responde cada uno?
3. ¿Cómo elige handler el advice cuando una excepción matchea dos, y de qué otra etapa conocés esa misma regla?

*Decidí y justificá:*
4. "El service debería lanzar `IllegalArgumentException` y listo — total da el mismo 400." Refutá con el argumento de la señalización, y da el caso concreto donde esa igualdad de resultado te costaría caro.
5. "La entidad debería lanzar `BusinessException` directo y nos ahorramos el try/catch." Refutá con el árbol de conocimiento — y concedé honestamente qué alternativa válida existe.
6. ¿Por qué el mensaje del 500 es opaco pero el del 400 lleva el detalle completo? ¿Qué diferencia a esas dos audiencias?
7. ¿Dónde mapearías una futura `ConflictException` ("el estado actual no permite esto") y con qué código? *(Existe en el proyecto de la clase — chequeá tu respuesta contra su advice.)*
8. Tu compañero puso try/catch en cada método del controller "para estar seguro". Funciona. Listale todo lo que perdió.

## 📝 Registro de la etapa

Tu línea — y esta etapa te debe una especial: ¿la matriz te ordenó lo que venía haciendo ruido?

## 🔗 Conexión con la clase

Abrí el `GlobalExceptionHandler` del `sales-service`: **es tu archivo con otros nombres** — mismos handlers, mismo `build`, misma opacidad, más el `ConflictException → 409` del checkpoint 7. Después buscá en `VentaServiceImpl` **el único try/catch de SU proyecto** (envuelve `comercio.agregarVenta` — su regla de "todos los productos deben ser del comercio", que grita igual que tu regla de las 5): tu Parte 7, en producción, estructura idéntica. Releé del recorrido **P6 §7 y §8**: deberían sonarte a espejo.

## ▶️ Próximo paso

Tu API crea, lista, busca, corrige, borra y se equivoca con elegancia — pero nace vacía en cada arranque y venís pagando ese peaje a mano desde la Etapa 5. Decime **"arranquemos etapa 9"** *(si ya hiciste la 7 y la 8; si no, seguí en orden)* — o retomá donde estabas.

---

**FIN DE LA ETAPA 6 — v2**
