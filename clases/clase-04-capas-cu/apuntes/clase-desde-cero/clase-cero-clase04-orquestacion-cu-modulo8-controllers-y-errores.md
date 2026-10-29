# 🧩 Clase desde cero — Clase 04 · Módulo 8: La capa de presentación. Controllers y el manejo global de errores

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 8 de 9.** Fase B (código).

## Sobre este documento

**Qué cubre:** la capa de arriba. Los **controllers** que exponen los endpoints del servicio de ventas (`ProductoController` y `VentaController`), cómo se mapea cada verbo y cada ruta, dónde viaja el id del producto a modificar, cómo un DTO se vuelve JSON sin que nadie lo convierta a mano, y el **manejador global de errores** que convierte las excepciones de M6 y M7 en códigos de estado.
**Qué NO cubre:** los datos con los que se arranca (el comercio 1, los tipos de producto) ni las pruebas con Postman y los tests: eso es M9. Al final de este módulo la API **levanta y responde**, pero todavía no se puede dar de alta nada.

## De dónde venís

- De **M4**: qué es un controller (recibe, traduce, delega, responde; conoce el protocolo; nunca lógica de negocio) y la **Regla 4** (los controllers conocen solo a los services). Y dos preguntas abiertas: **¿quién llama al service?** y **¿a dónde van los errores que el service lanza?**
- De **M5**: beans; `@RestController` es la anotación de bean de la capa de presentación.
- De **M6**: los DTOs (el nombre de cada atributo es la clave del JSON), `ProductoUpdateRequest` sin id (¿dónde va?), las excepciones propias y `ErrorResponse`.
- De **M7**: `ProductoService` y `VentaService`, y qué excepción lanza cada caso de uso.
- De la **clase 02**: verbos HTTP, rutas, path params vs query params, códigos de estado, y Jackson.

---

## 1. El caso: un pedido que llega de afuera 🔴

Alguien, desde Postman o desde otro sistema, manda esto:

```
GET  http://localhost:8082/sales-service/productos/1
```

Del otro lado está `findById(Long id)` de `ProductoService` (lo implementa `ProductoServiceImpl`, M7), que devuelve un `ProductoResponse`. Entre los dos hacen falta tres traducciones:

1. **De la ruta al método:** "un `GET` a `/sales-service/productos/1`" tiene que terminar llamando a `findById`.
2. **De la ruta al parámetro:** el `1` del final tiene que llegar como `Long id`.
3. **Del objeto a la respuesta:** el `ProductoResponse` tiene que salir como JSON, con un código de estado.

Eso es lo que hace un controller. Y en Spring Boot, casi todo se declara con anotaciones.

---

## 2. `ProductoController` 🔴

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.controllers;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.PrecioProductoResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoCreateRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoUpdateRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.services.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// @RestController: Spring crea este bean y lo prepara para atender pedidos HTTP;
// lo que devuelven sus métodos sale como JSON en el cuerpo de la respuesta.
// @RequestMapping: la ruta padre. Todas las rutas de esta clase empiezan así.
@RestController
@RequestMapping("/sales-service/productos")
public class ProductoController {

    // La única dependencia: el service, por su INTERFAZ (Regla 4).
    // Inyectado por constructor, como en M7.
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // GET /sales-service/productos  → listar
    @GetMapping
    public List<ProductoResponse> getAll() {
        return productoService.findAll();
    }

    // GET /sales-service/productos/{id}  → ver uno; el {id} de la ruta llega como parámetro
    @GetMapping("/{id}")
    public ProductoResponse getById(@PathVariable Long id) {
        return productoService.findById(id);
    }

    // POST /sales-service/productos  → dar de alta; el body JSON llega como DTO.
    // Si sale bien, responde 201 Created en lugar del 200 por defecto.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse create(@RequestBody ProductoCreateRequest request) {
        return productoService.create(request);
    }

    // PUT /sales-service/productos/{id}  → modificar; el id va en la RUTA, los datos en el body
    @PutMapping("/{id}")
    public ProductoResponse update(@PathVariable Long id, @RequestBody ProductoUpdateRequest request) {
        return productoService.update(id, request);
    }

    // DELETE /sales-service/productos/{id}  → eliminar; no devuelve cuerpo: 204 No Content
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productoService.deleteById(id);
    }

    // GET /sales-service/productos/{id}/precio  → consultar precio
    @GetMapping("/{id}/precio")
    public PrecioProductoResponse getPrecio(@PathVariable Long id) {
        return productoService.getPrecio(id);
    }
}
```

Mirá el cuerpo de cada método: **una línea**, que le pasa la pelota al service y devuelve lo que el service devuelve. Esa es la **Regla 4** en el código: el controller conoce solo `ProductoService`. No hay un repositorio, no hay una entidad, no hay un `new`, no hay un `if`. Toda la lógica está debajo.

Y se cierra el hilo de M4: **¿quién llama al service?** El controller.

### 2.1. Rutas y verbos

`@RequestMapping("/sales-service/productos")` sobre la clase fija la **ruta padre**: todas las rutas de los métodos se cuelgan de ahí. Cada método agrega su verbo y, si hace falta, un pedazo más de ruta:

| Anotación | Verbo | Ruta completa | Método del service | Status si sale bien |
|---|---|---|---|---|
| `@GetMapping` | GET | `/sales-service/productos` | `findAll` | 200 OK |
| `@GetMapping("/{id}")` | GET | `/sales-service/productos/{id}` | `findById` | 200 OK |
| `@PostMapping` | POST | `/sales-service/productos` | `create` | **201 Created** |
| `@PutMapping("/{id}")` | PUT | `/sales-service/productos/{id}` | `update` | 200 OK |
| `@DeleteMapping("/{id}")` | DELETE | `/sales-service/productos/{id}` | `deleteById` | **204 No Content** |
| `@GetMapping("/{id}/precio")` | GET | `/sales-service/productos/{id}/precio` | `getPrecio` | 200 OK |

Cada vez que llega un pedido con **ese verbo** a **esa ruta**, Spring ejecuta **ese método**.

La ruta padre es una comodidad: se podría no poner `@RequestMapping` en la clase y escribir la ruta completa en cada método. Con la ruta padre no se repite.

⚠️ **Las rutas pueden chocar.** Si en el mismo controller hubiera un `@GetMapping("/{id}")` y otro `@GetMapping("/destacados")`, un `GET` a `/productos/destacados` encaja en las dos (`destacados` podría ser un `{id}`). En Spring no importa el orden en que escribas los métodos: elige la ruta más específica, y una parte fija (`/destacados`) es más específica que una variable (`/{id}`), así que gana `/destacados`. El choque de verdad es entre dos rutas igual de específicas (`/{id}` y `/{codigo}`): ahí Spring no tiene cómo elegir y el pedido falla. En otros frameworks el orden sí importa. En cualquier caso: diseñá las rutas para que no choquen.

### 2.2. `@PathVariable`: la ruta tiene partes variables

En `@GetMapping("/{id}")`, el `{id}` es una **parte variable de la ruta**: un `GET` a `/sales-service/productos/1`, a `/sales-service/productos/150` o a `/sales-service/productos/480` llega al mismo método. `@PathVariable Long id` le dice a Spring: *"agarrá lo que vino en el lugar de `{id}`, convertilo a `Long` y pasámelo en este parámetro"*.

El nombre entre llaves y el del parámetro **coinciden** (`id` e `id`): así Spring los empareja. Si querés que el parámetro se llame distinto, se lo indicás: `@PathVariable("id") Long identificador`.

> 🕳️ **Madriguera — Cómo sabe Spring que el parámetro se llama `id`**
> Normalmente el compilador de Java no guarda los nombres de los parámetros. Para que `@PathVariable Long id` empareje por nombre, el proyecto compila con la opción `-parameters` (en este repo, `<parameters>true</parameters>` en el `maven-compiler-plugin` del `pom` padre; un proyecto que hereda del `parent` de Spring Boot la trae activada). Si no la tiene, el pedido falla con un mensaje que menciona `-parameters`; la salida rápida es nombrarlo: `@PathVariable("id")`.
> *Volvé al camino.*

> 🕳️ **Madriguera — Query params**
> Lo que va después del `?` en una URL (`/productos?tipo=2&orden=precio`) no son variables de la ruta: son **query params** (clase 02), y se recuperan con otra anotación. En este servicio no hay ninguno.
> *Volvé al camino.*

### 2.3. `@RequestBody`: el body se vuelve un DTO

En `create(@RequestBody ProductoCreateRequest request)`, `@RequestBody` le dice a Spring: *"el cuerpo del pedido es un JSON; convertilo en un `ProductoCreateRequest` y pasámelo"*. Cada clave del JSON va al atributo del mismo nombre (M6 §2.4). El método recibe un objeto Java listo para usar.

### 2.4. El JSON de salida: nadie lo arma a mano

`getAll()` devuelve una `List<ProductoResponse>`. Por ser una API REST, lo que tiene que salir en el cuerpo de la respuesta es JSON. ¿Dónde está la conversión? **En ningún lado del código.** Como la clase es un `@RestController`, Spring toma lo que devuelve el método, lo **serializa** a JSON (con Jackson, la misma biblioteca de la clase 02) y lo pone en la respuesta HTTP:

```
   getAll() devuelve                              sale por HTTP
   ─────────────────                              ─────────────
   List.of(                                       200 OK
     new ProductoResponse(1L, 1L, 2L,             [
         "Electrónico", 100.0,          ──────►     { "id": 1, "comercioId": 1,
         "Smart TV 50"))                              "tipoProductoId": 2,
                                                      "tipoProductoDescripcion": "Electrónico",
                                                      "precioBase": 100.0,
                                                      "descripcion": "Smart TV 50" }
                                                  ]
```

Y lo mismo en la otra dirección con `@RequestBody`. Se cierra el hilo de M7: **¿quién vuelve JSON los DTOs?** El framework. Si no existiera esa "magia", convertir el JSON del pedido en un DTO y el DTO de la respuesta en JSON sería responsabilidad del controller: es la capa que conoce el protocolo.

### 2.5. El status code de la respuesta

Si no se dice nada, una respuesta exitosa sale con **200 OK**. Para cambiarlo, la forma más simple es `@ResponseStatus` sobre el método:

- `create` → `@ResponseStatus(HttpStatus.CREATED)` → **201**: se creó un recurso.
- `delete` → `@ResponseStatus(HttpStatus.NO_CONTENT)` → **204**: salió bien y no hay nada que devolver (el método es `void`).

Hay otras formas de elegir el código de estado; esta es la más sencilla.

### 2.6. El id del `PUT`: en la ruta

Se cierra el hilo de M6: `ProductoUpdateRequest` no trae el id del producto a modificar porque **el id va en la ruta**:

```
PUT /sales-service/productos/1
{
  "comercioId": 1,
  "tipoProductoId": 1,
  "precioBase": 120.0,
  "descripcion": "Smart TV 50 - Actualizado"
}
```

En REST, **la URL identifica el recurso** (*"el producto 1"*) y el body trae **los datos nuevos**. Por eso `update` recibe las dos cosas por separado: `@PathVariable Long id` y `@RequestBody ProductoUpdateRequest request`. Es el mismo criterio que `GET /{id}` y `DELETE /{id}`: los tres operan sobre el mismo recurso, identificado de la misma forma.

> ⚠️ **¿Se puede mandar el id en el body?** Técnicamente sí: el framework lo recibe igual y el código funciona. Pero no es REST: el recurso tiene que estar identificado en la URL. En las revisiones del TP, un `PUT` con el id en el body se marca.

> **Para el parcial, si te preguntan:** *¿Dónde va el id de un recurso en un `PUT` y por qué?*
> En la ruta (`PUT /productos/{id}`), y el body trae solo los datos nuevos: en REST la URL identifica el recurso sobre el que se opera, igual que en el `GET` y el `DELETE` del mismo recurso. Mandarlo en el body funciona, pero rompe esa convención.

---

## 3. `VentaController` 🔴

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.controllers;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta.VentaCreateRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta.VentaResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.services.VentaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Un controller por recurso: este expone las ventas.
@RestController
@RequestMapping("/sales-service/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // POST /sales-service/ventas  → registrar una venta (el único caso de uso de ventas)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaResponse create(@RequestBody VentaCreateRequest request) {
        return ventaService.create(request);
    }
}
```

Un solo endpoint, porque `VentaService` tiene un solo método (M7 §1). El mismo patrón: ruta padre, inyección del service por constructor, una línea por método.

---

## 4. El manejo global de errores 🔴

### 4.1. El problema

Volvé a M7. `getProductoOrThrow` lanza `ResourceNotFoundException`; las validaciones lanzan `BusinessException`. En ningún controller hay un `try/catch`. Entonces, cuando alguien pide el producto 3 y no existe, la excepción sube desde el service hasta el controller… y sigue subiendo. **¿Quién la atrapa?**

Si nadie la atrapa, Spring no sabe qué significa una excepción nuestra y responde **500 Internal Server Error**: "algo se rompió adentro". Pero que el producto 3 no exista no es un error interno: es un **404**. Y una descripción vacía es un **400**. Hace falta alguien que traduzca cada tipo de excepción a su código.

Se podría poner un `try/catch` en cada método de cada controller (M4 §4.3), pero se repetiría lo mismo en todos lados. La forma prolija: **un solo lugar** para todas las traducciones.

### 4.2. `GlobalExceptionHandler`

Está en `controllers/advice/`: es de la capa de presentación, porque traducir a códigos HTTP es conocer el protocolo.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.controllers.advice;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.error.ErrorResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions.BusinessException;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions.ConflictException;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

// @RestControllerAdvice: esta clase es un manejador de excepciones para TODOS los controllers.
// Cuando un método de cualquier controller termina con una excepción, Spring busca acá
// un método que la maneje. Es un bean más: no hay que registrarlo en ningún lado.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── "No existe" → 404 ────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage());
    }

    // ── Pedido inválido o regla violada → 400 ───────────────────────────────────
    // Incluye IllegalArgumentException, la que usa el dominio (ItemVenta, Comercio).
    // Hoy no llega: los services validan antes o la traducen (M7). Queda como red de seguridad.
    // Recibe RuntimeException porque es el tipo que tienen en común las dos.
    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage());
    }

    // ── Conflicto → 409 (preparado; hoy nadie la lanza) ─────────────────────────
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "conflict", ex.getMessage());
    }

    // ── Cualquier otra → 500, con un mensaje genérico ───────────────────────────
    // No devuelve ex.getMessage(): un error interno no tiene por qué mostrar detalles hacia afuera.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", "Ocurrió un error interno");
    }

    // ── Arma la respuesta: el código de estado y el ErrorResponse (M6) como cuerpo ──
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(error, message, Instant.now()));
    }
}
```

Términos nuevos:
- **`@ExceptionHandler(X.class)`** sobre un método: *"cuando un controller termine con una excepción de tipo X, ejecutá este método"*. Puede recibir varias (`{A.class, B.class}`).
- **`ResponseEntity<T>`**: una respuesta HTTP completa armada a mano: código de estado + cuerpo (+ headers, si hicieran falta). Acá se usa porque `build` es un solo método para todos los casos y recibe el código como parámetro. Es una de las "otras formas" de elegir el código de estado que anunciaba §2.5.
- **¿Y si una excepción encaja en dos?** Toda `ResourceNotFoundException` también es una `Exception`, así que encaja en `handleNotFound` y en `handleUnexpected`. Spring elige el del tipo más cercano en la herencia: `handleNotFound`. Por eso `handleUnexpected` recibe solo lo que no encaja en ningún otro, y el orden de los métodos no importa: como con las rutas, gana lo más específico.

### 4.3. La tabla completa

| Excepción | Quién la lanza | Status | `error` |
|---|---|---|---|
| `ResourceNotFoundException` | services (`...OrThrow`, `findComercioOwner`) | **404** Not Found | `not_found` |
| `BusinessException` | services (validaciones, reglas del flujo) | **400** Bad Request | `bad_request` |
| `IllegalArgumentException` | dominio (`ItemVenta`, `Comercio.agregarVenta`); hoy no llega: el service valida antes y el `try/catch` de la venta la traduce | **400** Bad Request | `bad_request` |
| `ConflictException` | nadie todavía (preparada) | **409** Conflict | `conflict` |
| cualquier otra | lo que sea | **500** Internal Server Error | `internal_error` |

Ahora queda a la vista algo de M7 §7.2: el `try/catch` de `VentaServiceImpl` convierte la `IllegalArgumentException` del dominio en `BusinessException`, pero hacia afuera no cambia nada: si alguna vez llegara sin traducir, este manejador también la mandaría a 400 con el mismo mensaje. Y en este proyecto no llega nunca: el service ya verificó lo mismo antes (la cantidad en `validateRequest`, la pertenencia en el loop).

### 4.4. Cómo se ve un error desde afuera

```
GET /sales-service/productos/3        (el producto 3 no existe)

404 Not Found
{
  "error": "not_found",
  "message": "No se encontró producto con id 3",
  "timestamp": "2026-04-15T22:14:03.512Z"
}
```

(El `timestamp` sale como texto en formato ISO 8601; la cantidad de decimales de los segundos puede variar.)

```
// ¿CÓMO FUNCIONA? el recorrido de ese error
//   1. ProductoController.getById(3) llama a productoService.findById(3).
//   2. getProductoOrThrow(3): el Optional viene vacío → lanza ResourceNotFoundException.
//   3. La excepción sube: sale del service, sale del controller (nadie la atrapa).
//   4. Spring busca en GlobalExceptionHandler el @ExceptionHandler más específico para
//      ResourceNotFoundException (también encaja en el de Exception, pero ese es más general).
//   5. handleNotFound arma un ResponseEntity con 404 y el ErrorResponse.
//   Resultado esperado: 404, con el JSON de arriba.
```

Se cierran tres hilos de M4 y M6: **¿a dónde van los errores que el service lanza?** Suben hasta acá. **¿Quién atrapa las excepciones propias?** `GlobalExceptionHandler`. **¿Por qué no hay `try/catch` en los controllers?** Porque esto los reemplaza.

> **Para el parcial, si te preguntan:** *¿Para qué sirve un manejador global de excepciones y en qué capa va?*
> Para traducir cada tipo de excepción que lanzan los services o el dominio a su código de estado HTTP y a un cuerpo de error uniforme, en un solo lugar, sin `try/catch` repetido en cada controller. Va en la capa de presentación, porque los códigos de estado son parte del protocolo, que solo conoce esa capa; sin él, cada excepción propia o del dominio terminaría en un 500.

> 🕳️ **Madriguera — Errores que arma el propio Spring**
> Hay errores que ocurren **antes** de llegar al service: un body con JSON mal formado, un pedido sin body, un id no numérico en la ruta (`/productos/abc`), una ruta que no existe, un verbo que la ruta no acepta (un `PATCH` a `/productos/1`), un campo numérico primitivo que no vino en el JSON (M6 §3.1). El framework los convierte en excepciones propias suyas; sin este manejador, el propio Spring les pondría 400, 404 o 405. Pero como este manejador tiene un caso para `Exception`, lo esperable es que terminen ahí y salgan como 500. Ese es el destino del pedido sin body que el `if (request == null)` de M7 §5.5 nunca llega a ver. No está verificado en ejecución; afinar ese manejo es una mejora posible.
> *Volvé al camino.*

---

## 5. Estado del proyecto 🟡

```
sales_service/
├── SalesServiceApplication.java    ✅
├── models/entities/...             ✅ (clase 03)
├── repositories/ + inmemory/       ✅ (M5)
├── utils/                          ✅ (M5)
├── dtos/ y exceptions/             ✅ (M6)
├── services/ + impl/               ✅ (M7)
└── controllers/                    ✅ ProductoController, VentaController
    └── advice/                     ✅ GlobalExceptionHandler
```

**La API levanta y responde.** Si probás ahora:

| Pedido | Respuesta |
|---|---|
| `GET /sales-service/productos` | **200**, `[]` (no hay productos) |
| `GET /sales-service/productos/1` | **404**, "No se encontró producto con id 1" |
| `POST /sales-service/productos` con `{"comercioId": 1, "tipoProductoId": 2, "precioBase": 100.0, "descripcion": "Smart TV 50"}` | **404**, "No se encontró comercio con id 1" |
| `POST /sales-service/ventas` con `{"comercioId": 1, "items": [{"productoId": 1, "cantidad": 2}]}` | **404**, "No se encontró comercio con id 1" |

Los dos últimos son el problema: **todo `POST` bien armado da 404**, porque no existe ningún comercio ni ningún tipo de producto. Los repositorios arrancan vacíos, y ningún caso de uso da de alta comercios ni tipos. ¿De dónde salen el comercio 1, el tipo 1 (Hogar) y el tipo 2 (Electrónico)? Eso es lo primero de M9.

---

## 6. Lo que te llevás de este módulo

- **`@RestController`** + **`@RequestMapping`** (ruta padre). Cada método: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`, con su pedazo de ruta.
- **`@PathVariable`**: parte variable de la ruta → parámetro. **`@RequestBody`**: body JSON → DTO.
- **El JSON de salida lo arma Spring** (Jackson). El controller no serializa nada.
- **Status:** 200 por defecto; `@ResponseStatus(CREATED)` → 201; `NO_CONTENT` → 204.
- **El id del `PUT` va en la ruta**; el body trae los datos nuevos.
- **Regla 4 en el código:** cada método del controller es una línea que delega en el service.
- **`@RestControllerAdvice`** + **`@ExceptionHandler`**: un solo lugar que traduce excepciones a status (404, 400, 409, 500) con un `ErrorResponse`, usando `ResponseEntity`.
- Queda abierto: de dónde salen el comercio y los tipos de producto (M9).

---

## ✅ Checkpoint del Módulo 8

Respondé de memoria. Las respuestas van al complemento.

1. Escribí la tabla verbo + ruta + método + status de `ProductoController` sin mirar.
2. ¿Qué hace `@RequestMapping` sobre la clase? ¿Es obligatorio?
3. En `getById(@PathVariable Long id)`, ¿cómo sabe Spring qué parte de la ruta va en `id`? ¿Cómo harías si el parámetro se llamara `identificador`?
4. `getAll()` devuelve una `List<ProductoResponse>`. ¿Dónde se convierte en JSON? ¿Qué pasaría, en términos de responsabilidades, si el framework no lo hiciera?
5. ¿Por qué el `PUT` lleva el id en la ruta y no en el body? ¿Funcionaría en el body?
6. ¿Qué status devuelve `create` si sale bien, y cómo se configura? ¿Y `delete`?
7. Describí el recorrido de un `GET /sales-service/productos/3` cuando el producto no existe, desde el controller hasta la respuesta.
8. ¿Por qué `GlobalExceptionHandler` está en la capa de presentación y no en la de dominio?
9. ¿Qué devuelve `handleUnexpected` como mensaje, y por qué no el de la excepción?
10. Con la API levantada y los repositorios vacíos, ¿qué responde un `POST` de producto, y por qué?

---

## Qué viene después

**M9 — Arrancar y probar: seeder, Postman y tests.** El último módulo. Se cargan los datos iniciales al arrancar (el comercio 1 y los dos tipos de producto), se prueba la API de punta a punta con Postman, se sigue el recorrido completo de un pedido por todas las capas, y se ven los tests: los del dominio y los de integración, que llaman a los endpoints.

**FIN DEL MÓDULO 8**
