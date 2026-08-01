# 🌱 Proyecto 0 (clase 04) — Etapa 6: El traductor — excepciones propias y el manejador global

> **Objetivo:** que cada error salga al mundo con **el código HTTP que corresponde** y **un cuerpo uniforme** — sin un solo try/catch en los controllers.
>
> **Cómo trabaja esta etapa:** primero el sistema **COMPLETO** — piezas, árboles, la matriz de "cuándo se lanza cada cual", y **los dos efectos secundarios conocidos del diseño**, anunciados de entrada. Después se construye. Nada provisorio, nada que desaprender.
>
> **Pre-requisito:** Etapa 5 completa.
>
> **Tiempo estimado:** 55-65 minutos.
>
> *(v3 — incluye dos correcciones de precisión salidas de TU evidencia empírica: el silencio del 500 manejado, y el JSON roto mal etiquetado. Ambos defectos existen también en el código de la cátedra — verificado. Las guías también se testean, y vos las testeás bien.)*

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
        │  en toda la     │                      │   traduce a: status  │
        │  etapa)         │                      │   correcto +         │
        └────────┬────────┘                      │ ★ ErrorResponse      │
                 ▼                               │   (+ log interno de  │
        ┌─────────────────┐                      │    lo imprevisto)    │
        │    SERVICES     │ ◄── ~6 líneas:       └──────────────────────┘
        │                 │     lanzar ★ excepciones PROPIAS
        └───┬─────────┬───┘     en vez de genéricas
            ▼         ▼
   ┌────────────┐ ┌──────────────┐
   │  DOMINIO   │ │ REPOSITORIES │
   │ (NO se     │ │ (no se       │
   │  toca      │ │  tocan)      │
   │  JAMÁS)    │ └──────────────┘
   └────────────┘
```

Movimiento total: **4 archivos nuevos** (2 excepciones + 1 DTO de error + el advice), **~6 líneas retocadas en 2 services**, y todo lo demás — controllers, dominio, repos — intacto.

## 🗂️ Los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── controllers/
    │   ├── PropietarioController.java           (NO se toca)
    │   ├── MascotaController.java               (NO se toca)
    │   └── advice/
    │       └── GlobalExceptionHandler.java      ★ NUEVO
    ├── dtos/
    │   └── error/ErrorResponse.java             ★ NUEVO
    ├── exceptions/                              (📦 NUEVO package)
    │   ├── ResourceNotFoundException.java       ★ NUEVO
    │   └── BusinessException.java               ★ NUEVO
    ├── models/entities/                         (NO SE TOCA — importante, ver Parte 1)
    └── services/impl/                           (SE TOCAN: ~6 líneas entre ambos)
```

## 🧭 Mapa de esta etapa

1. **EL SISTEMA COMPLETO** (todo el mapa mental, antes de una sola línea de código).
2. Construcción pieza 1: las excepciones propias.
3. Construcción pieza 2: el error uniforme.
4. Construcción pieza 3: el traductor.
5. El recableado (inventario exacto + Propietario guiado).
6. Experimento 1: la red de seguridad… ¿ya alcanza?
7. La traducción deliberada (el único try/catch del proyecto).
8. Experimento 2: el 500 opaco… y mudo (efecto secundario 1 → el logger).
9. Experimento 3: el JSON roto mal etiquetado (efecto secundario 2 → el handler que falta).
10. Tu turno: Mascota + la batería final.
11. Criterios + checkpoint + registro + 🔗.

---

## 🧠 Parte 1: EL SISTEMA COMPLETO (leé toda esta parte antes de tocar código)

### 1a. El problema que venimos a resolver

Las tres deudas que anotaste en la Etapa 5 — con la request exacta que provoca cada una:

| # | La provocás con… | Nace en… | Hoy sale | Debería salir |
|---|---|---|---|---|
| 1 | `POST /propietarios` con `{"nombre":""}` | validación del service | 500 | **400** (culpa del cliente) |
| 2 | `GET /propietarios/99` | el `orElseThrow()` pelado del service | 500 | **404** (no existe) |
| 3 | la 6ª mascota del mismo propietario | la regla en `Propietario.agregarMascota` | 500 | **400** (regla violada) |

Tres orígenes, una mentira: todo sale 500. Falta **el traductor**.

### 1b. El árbol de HERENCIA — de dónde viene cada excepción

```
Throwable
└── Exception ················· CHEQUEADAS: obligan try/catch o `throws`
    └── RuntimeException ······ NO CHEQUEADAS: vuelan libres hacia arriba
        ├── IllegalArgumentException      ← de Java: "argumento inaceptable"
        │                                    (la que lanza tu dominio)
        ├── NoSuchElementException        ← de Java: la de tu orElseThrow()
        │                                    pelado de la Etapa 5
        ├── ResourceNotFoundException   ★ ← TUYA (la creás hoy)
        └── BusinessException           ★ ← TUYA (la creás hoy)

(y aparte, colgando de Exception por otra rama, las del framework — como
 HttpMessageNotReadableException, la del JSON roto de la Etapa 3: la comilla
 que le sacaste al body. Va a reaparecer en la Parte 9.)
```

Dato clave: **las cuatro tuyas/usadas son `RuntimeException`** → ninguna obliga a try/catch → los services pueden lanzarlas y **patear hacia arriba sin ensuciarse**. Si heredaran de `Exception` a secas, cada firma cargaría un `throws` y el controller estaría obligado a atrapar.

### 1c. Las piezas y cómo juegan juntas — y los dos efectos secundarios, anunciados

El sistema completo al final de la etapa:

```
  SERVICE lanza                    NADIE la atrapa              ADVICE la traduce
  ─────────────                    en el camino                 ─────────────────
  ResourceNotFoundException ────────────────────────────────►  404 + ErrorResponse
  BusinessException         ────────────────────────────────►  400 + ErrorResponse
  (body JSON ilegible)      ────────────────────────────────►  400 + ErrorResponse
  (cualquier otra cosa)     ────────────────────────────────►  500 + ErrorResponse
                                                                opaco afuera
                                                                + LOG adentro
```

Las piezas: **dos excepciones propias** (el vocabulario), **un record `ErrorResponse`** (la forma única de todo error), y el **`GlobalExceptionHandler`** (atrapa por tipo lo que escape de cualquier controller y fabrica la respuesta).

Y la parte que ningún tutorial te cuenta — **el catch-all (`Exception.class`) trae dos efectos secundarios conocidos**, que vas a verificar con tus manos y corregir en las Partes 8 y 9:

1. **El silencio:** una excepción *manejada* por el advice se considera *resuelta* → Spring **no la loguea** — el stacktrace automático que veías en las etapas 4-5 existía porque nadie atrapaba. Con el advice, un 500 manejado es un 500 **mudo**: incidente invisible. La cura: loguear vos, adentro del handler.
2. **El mal etiquetado:** el catch-all traga TODO lo no listado — incluidas excepciones del framework que son culpa del **cliente** (el JSON roto → `HttpMessageNotReadableException`) y las disfraza de 500 "culpa mía". La cura: un handler específico que la devuelva a su 400.

*(Los dos defectos existen en el advice de la cátedra — verificado en su código: no loguea nada y no maneja el JSON ilegible. Tus dos correcciones son extensiones conscientes que superan al material de la clase: sabé defenderlas.)*

### 1d. LA MATRIZ — cuándo se lanza cada cual (el corazón de la etapa)

*(Formulación nuestra, que sintetiza el patrón verificado en el código del repo de la cátedra.)* La regla madre:

> **La excepción no la elige el evento — la elige LA CAPA donde estás parado.** El mismo problema ("me pasaron basura") se expresa distinto según quién habla, porque cada capa tiene un vocabulario disponible distinto.

| ¿DÓNDE estás parado? | ¿Qué pasó? | Lanzás | Termina en |
|---|---|---|---|
| **ENTIDAD** (`models/`) | cualquier regla violada | `IllegalArgumentException` — **la única que puede**: el dominio no importa nada del sistema | 400 (vía traducción) |
| **SERVICE** | lo que la request *nombra* no existe (un id) | `ResourceNotFoundException` | **404** |
| **SERVICE** | los datos o una regla de flujo no valen | `BusinessException` | **400** |
| **SERVICE** | el dominio gritó (te llegó su `IllegalArgumentException`) | try/catch → relanzás **`BusinessException`** (la traducción — Parte 7) | **400** |
| **CONTROLLER** | — | **NADA. Jamás lanza, jamás atrapa.** | — |

Dos aclaraciones que evitan los nudos clásicos:

**«¿Por qué el service lanza `BusinessException` si "nombre vacío" ES literalmente un argumento ilegal?»** Porque semánticamente tenés razón — lo es — pero el service usa su vocabulario propio por **señalización**: `BusinessException` llegando al advice significa *"regla deliberada mía"*; `IllegalArgumentException` llegando significa *"algo se escapó sin traducir"*. Si el service usara la genérica, esa distinción moriría. En comportamiento observable, hoy, ambas dan el mismo 400 — la diferencia es de **intención declarada**, no de resultado.

**«¿Por qué la entidad no lanza `BusinessException` directo y nos ahorramos el try/catch?»** Compilar, compila — pero mirá el árbol de CONOCIMIENTO (no confundir con el de herencia de 1b; son dos árboles distintos):

```
capa                    puede importar…
─────────────────────────────────────────────────────────────
controllers + advice →  services, dtos, exceptions
services             →  entities, repositories, dtos, exceptions
models/entities      →  NADA del sistema (ni siquiera exceptions/) ◄── acá
```

Para lanzar `BusinessException`, `Propietario.java` necesitaría importarla → el dominio **conocería una pieza del sistema** → deja de ser portable, y cada cambio en `exceptions/` tocaría entidades. El try/catch del service es **el precio de la pureza del dominio**. (La cátedra paga ese precio; existe la alternativa de una excepción que viva dentro del propio dominio — diseño válido que ni la cátedra ni nosotros usamos.)

**Con este mapa completo en la cabeza, recién ahora: a construir.** Nada de lo que sigue te pide desaprender nada de esta parte — solo agregar.

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

Seis líneas cada una: heredan de `RuntimeException` (el porqué está en 1b) y solo cargan un mensaje. ¿Por qué existir, si "no hacen nada"? Porque el traductor despacha **por tipo**: para responder 404 a una cosa y 400 a otra, tienen que SER tipos distintos. El nombre mismo **es** la información.

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

La decisión importante es la política: **todos los errores del sistema, de cualquier tipo, salen con esta única forma.**

## 🛠️ Parte 4: Pieza 3 — el traductor

📍 **Dónde estamos:**

```
Controllers ──► Services ──► ...
     │
     └──► advice/GlobalExceptionHandler  ◄━━ ACÁ (lo que escape de CUALQUIER
                                              controller, cae en esta clase)
```

El archivo completo, ya con tus dos correcciones incorporadas (en la 8 y la 9 las verificás con experimentos — acá van marcadas ⭐ para que sepas qué es base-cátedra y qué es extensión tuya):

```java
// 📁 src/main/java/.../veterinaria/controllers/advice/GlobalExceptionHandler.java   (NUEVO)
package com.practica.veterinaria.controllers.advice;

import com.practica.veterinaria.dtos.error.ErrorResponse;
import com.practica.veterinaria.exceptions.BusinessException;
import com.practica.veterinaria.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;

@RestControllerAdvice                // "atrapo lo que ESCAPE de cualquier
public class GlobalExceptionHandler {//  controller" — registro automático:
                                     //  nadie lo conecta, ningún controller
                                     //  sabe que existe.

    private static final Logger log =
        LoggerFactory.getLogger(GlobalExceptionHandler.class);
    // ↑ ⭐ (extensión tuya) el canal hacia TU consola — su porqué, en la Parte 8.

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
    //   ¿IllegalArgumentException acá? RED DE SEGURIDAD para las que se
    //   escapen sin traducir (la Parte 6 le saca punta).

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "bad_request",
                     "El body no es un JSON válido");                          // → 400
    }
    // ↑ ⭐ (extensión tuya) el JSON ilegible es culpa del CLIENTE: 400.
    //   Sin este handler se lo traga el catch-all y sale 500 — el defecto
    //   que descubriste. Verificación y porqué completo: Parte 9.

    @ExceptionHandler(Exception.class)                   // TODO lo no previsto:
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Error no previsto", ex);              // ⭐ la verdad, a TU consola
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
                     "Ocurrió un error interno");        // mensaje OPACO a propósito
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(error, message, Instant.now()));
    }   // ↑ la fábrica: status elegido + tu forma uniforme como body
}
```

**¿Cómo elige handler cuando llega una excepción?** Por tipo, y si más de uno matchea, **gana el más específico**:

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

**`ResponseEntity`** (no es anotación — es el tipo de retorno): *"una respuesta HTTP completa armada a mano"* — `status(...).body(...)`. Acá es la herramienta correcta porque el status **depende** de qué excepción llegó (lo que `@ResponseStatus`, fijo por método, no puede). En endpoints felices seguís como siempre: DTO pelado + `@ResponseStatus`. Atajos que vas a ver: `ResponseEntity.ok(dto)`, `.notFound().build()`.

👀 *El logger: `LoggerFactory.getLogger(...)` es SLF4J, la fachada de logging que Boot trae puesta. Como tenés Lombok, existe el atajo `@Slf4j` sobre la clase (te genera el campo `log` solo) — válido igual; acá va explícito para que veas la pieza. **Su historia completa — niveles, umbral, fachada, configuración y dónde más se usa — tiene etapa propia: la 6B.***

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

📍 **Propietario, retoque 2** — el `orElseThrow` pelado (el que lanzaba `NoSuchElementException`, la genérica de Java, en el GET del 99):

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

Levantá y **cobrá las dos primeras deudas** (con las requests exactas de 1a): `GET /99` → **404** con tu `ErrorResponse` · nombre vacío → **400**. Sin try/catch en ningún lado tuyo: el service lanzó, nadie atrapó, el advice tradujo.

## 🧨 Parte 6: Experimento 1 — la red de seguridad… ¿ya alcanza?

Queda la deuda 3 — la 6ª mascota, cuyo grito (`IllegalArgumentException`) nace en el dominio. **Predicción:** con el advice instalado y SIN tocar nada más… ¿sigue saliendo 500?

Probá (5 mascotas a Ana, mandá la 6ª): **400** — la atrapó la red (estaba en las llaves). ¿Deuda pagada? **A medias:** funciona *por rebote*. Cualquier `IllegalArgumentException` de CUALQUIER origen — incluida la de un **bug** tuyo o de una biblioteca — saldría etiquetada `bad_request`, culpando al cliente por errores tuyos. La red es para **escapes imprevistos**; las reglas **deliberadas** merecen traducción **deliberada**.

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

La cadena completa — la fila 4 de tu matriz, funcionando:

```
Propietario.agregarMascota          MascotaServiceImpl              Advice
──────────────────────────          ──────────────────              ──────
throw IllegalArgumentException ──►  catch → throw            ──►    handler exacto
("6ª mascota" — idioma Java,        BusinessException                → 400 +
 lo único que el dominio habla)     (idioma del sistema:             ErrorResponse
                                     "regla deliberada")
```

Verificá: la 6ª → **400** con el mensaje de la regla. Mismo resultado visible que en el Experimento 1 — **pero ahora por diseño, no por rebote**. Deuda 3, pagada con honores.

## 🧨 Parte 8: Experimento 2 — el 500 opaco… y mudo (efecto secundario 1)

Plantá una bomba temporal en cualquier service:

```java
// (temporal — primera línea del findAll de propietarios, por ejemplo)
if (true) throw new RuntimeException("boom interno: db=10.0.0.5, password=secreta123");
```

**Dos predicciones:** *(a)* ¿qué recibe el cliente — tu mensaje con la bomba, o otra cosa? *(b)* ¿y qué aparece en la consola del servidor?

Probá el GET. *(a)* El cliente recibe **500** con *"Ocurrió un error interno"* — ni rastro de la bomba: la opacidad hacia afuera funciona (los detalles internos de un error imprevisto son munición para un atacante y ruido para un cliente). *(b)* Y en la consola… **el stacktrace de tu `log.error`** — pero prestale atención a POR QUÉ está ahí: **solo porque lo escribiste vos**. Si comentás temporalmente la línea `log.error(...)` y repetís el GET, la consola queda **muda** — 500 para el cliente, silencio absoluto para vos.

El mecanismo, que es el efecto secundario 1 anunciado en 1c: cuando un handler **maneja** una excepción, Spring la considera **resuelta** — y lo resuelto no se loguea. El stacktrace automático de las etapas 4-5 existía porque *nadie atrapaba*; instalar el catch-all te compró la respuesta prolija **al precio de la ceguera interna**. Un 500 mudo es un incidente invisible: el peor de los estados (el cliente sabe que algo falló; vos no). Por eso el `log.error` del catch-all **no es decoración: es el órgano sensorial** — opacidad cortés afuera, verdad plena adentro, *pero la verdad adentro la escribís vos*. Descomentá el log y sacá la bomba.

## 🧨 Parte 9: Experimento 3 — el JSON roto mal etiquetado (efecto secundario 2)

**Predicción con el árbol en la mano:** mandá el POST con la comilla rota (Etapa 3: el body con sintaxis inválida). En la Etapa 3 daba 400 de fábrica… pero ahora existe tu catch-all. Seguí el razonamiento: la `HttpMessageNotReadableException` revienta en la **traducción del body** (Jackson, ANTES de que tu método corra) → nadie la lista con nombre… salvo que SÍ la listaste (el handler ⭐ de la Parte 4). ¿Qué esperás — y qué esperarías SIN ese handler?

Probá: **400** con TU `ErrorResponse` — *"El body no es un JSON válido"*. Y ahora la contraprueba que revela el defecto (comentá temporalmente el handler ⭐ `handleUnreadable` y repetí): **500 `internal_error`** — el catch-all se la tragó y la etiquetó como culpa TUYA… cuando el body roto es culpa del **cliente**. Ese es el efecto secundario 2, el que descubriste con tu propia evidencia: **un catch-all pelado convierte errores 4xx del framework en 500 mentirosos**. Descomentá el handler.

La moraleja de las Partes 8 y 9 juntas: `@ExceptionHandler(Exception.class)` es necesario (siempre puede pasar lo imprevisto) pero **nunca va solo** — va con su logger (o es ciego) y con handlers específicos para las excepciones conocidas del framework que no son culpa tuya (o es mentiroso). La cátedra no hace ninguna de las dos cosas — su advice comparte ambos defectos, verificado en su código. Tus dos ⭐ son mejoras conscientes: en una defensa, poder decir *"agregué esto porque un 500 manejado no se loguea solo, y esto otro porque el catch-all mal-etiqueta los errores de deserialización"* vale oro.

## ✍️ Parte 10: Tu turno — Mascota + la batería

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
| JSON con sintaxis rota | **400** · **TU** `ErrorResponse`: "El body no es un JSON válido" |
| La bomba (temporal) | **500** opaco al cliente + **stacktrace en TU consola** |
| Todo lo feliz de siempre | **200/201** intacto |

## ✅ Criterios de "Etapa 6 completa"

- [ ] Leíste la Parte 1 ENTERA antes de codear — matriz y efectos secundarios incluidos.
- [ ] Las piezas construidas (con las dos ⭐); controllers y dominio, intactos.
- [ ] Los tres experimentos con predicción — incluidas las dos contrapruebas (el log comentado, el handler comentado).
- [ ] El único try/catch del proyecto en su lugar, y sabés defenderlo con los DOS árboles.
- [ ] Podés explicar los dos efectos secundarios del catch-all y por qué tus ⭐ los curan.
- [ ] Batería 9/9.

## ✅ Checkpoint

*Recall:*
1. Recitá la matriz: parado en la entidad / en el service (3 casos) / en el controller — ¿qué se lanza en cada situación y por qué?
2. ¿Cuáles son los DOS árboles de esta etapa, y qué pregunta responde cada uno?
3. ¿Por qué una excepción manejada por el advice NO aparece sola en consola, y qué pieza tuya recupera esa verdad? ¿Desde cuándo (qué etapa) cambió ese comportamiento y por qué?
4. ¿Cómo elige handler el advice cuando una excepción matchea dos, y de qué otra etapa conocés esa regla?

*Decidí y justificá:*
5. "El service debería lanzar `IllegalArgumentException` y listo — total da el mismo 400." Refutá con la señalización, y da el caso donde esa igualdad te costaría caro.
6. "La entidad debería lanzar `BusinessException` directo y nos ahorramos el try/catch." Refutá con el árbol de conocimiento — y concedé qué alternativa válida existe.
7. El catch-all `Exception.class`: sus dos efectos secundarios, tus dos curas — y por qué igual lo conservás en vez de borrarlo.
8. ¿Dónde mapearías una futura `ConflictException` ("el estado actual no permite esto") y con qué código? *(Existe en el proyecto de la clase — chequeá contra su advice.)*
9. Tu compañero puso try/catch en cada método del controller "para estar seguro". Funciona. Listale todo lo que perdió.

## 📝 Registro de la etapa

Tu línea — y esta etapa te debe DOS entradas con nombre propio: el 500 mudo y el JSON mal etiquetado los descubriste vos, contra el material y contra la cátedra. A la bitácora van.

## 🔗 Conexión con la clase

Abrí el `GlobalExceptionHandler` del `sales-service`: la base **es tu archivo con otros nombres** — mismos tres handlers troncales, mismo `build`, misma opacidad del 500 — más un `ConflictException → 409` que pensaste en el checkpoint 8. Y ahora leelo con tus ojos nuevos: **no tiene logger, no maneja `HttpMessageNotReadableException`** — los dos defectos que verificaste, en el código de la cátedra, tal cual. Tu advice es hoy estrictamente mejor que el del material de la clase, y sabés explicar por qué con experimentos propios. Después buscá en `VentaServiceImpl` **el único try/catch de SU proyecto** (envuelve `comercio.agregarVenta` — su regla que grita igual que tu regla de las 5): tu Parte 7 en producción, estructura idéntica. Releé del recorrido **P6 §7 y §8**: deberían sonarte a espejo — con dos mejoras de tu lado.

## ▶️ Próximo paso

Tu API crea, lista, busca y se equivoca con elegancia (y ahora, sin mentir ni quedarse ciega). Retomá donde estabas del camino: si venís en orden, **"arranquemos etapa 7"** — corregir y borrar; si ya la hiciste, seguí con la que toque.

---

**FIN DE LA ETAPA 6 — v3**
