# 🧩 Clase desde cero — Clase 04 · Módulo 6: Lo que viaja y lo que falla. DTOs y excepciones propias

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 6 de 9.** Fase B (código).

## Sobre este documento

**Qué cubre:** dos piezas de apoyo que los services (M7) y los controllers (M8) van a usar todo el tiempo. Los **DTOs**: los objetos que transportan los datos entre el mundo exterior y el dominio, y por qué no se usan las entidades para eso. Y las **excepciones propias**: los errores del servicio, y por qué son del tipo que son.
**Qué NO cubre:** quién arma cada DTO (M7), quién lo recibe desde HTTP (M8) ni quién atrapa las excepciones (M8). Al final de este módulo el proyecto compila, pero nada usa todavía estas clases.

## De dónde venís

- De **M4**: `DataCalificacion`, los datos que el controller le pasa al service. Quedó dicho que "puede ser un value object" y que cómo se escribe uno en Java se veía acá.
- De **M4** también: el service no elige el código de estado; lanza una excepción y la presentación la traduce.
- De **M5**: el proyecto y sus paquetes, entre ellos `dtos/` y `exceptions/`.
- De la **clase 02**: JSON, y que Jackson convierte JSON en objetos Java y viceversa, emparejando nombres (y que `@JsonProperty` sirve cuando los nombres no coinciden).
- Del **seminario de Java** (preclase 02): las excepciones y `try/catch`, a alto nivel.

---

## 1. El caso: ¿qué devuelvo cuando me piden los usuarios? 🔴

Supongamos que el sistema tiene una entidad `Usuario` como la de M2: nombre de usuario, contraseña, rol. Alguien desde afuera pide *"dame todos los usuarios"*. El service los trae del repositorio. ¿Los devuelve tal cual?

**No.** Hay un atributo que jamás tiene que salir del sistema: **la contraseña**.

Ahora el caso al revés. Desde afuera alguien quiere *dar de alta un producto*. Tiene que mandar la descripción, el precio, el tipo. ¿Y el id? No: **el id lo genera el sistema** (M5: lo asigna el repositorio al guardar). Si lo dejaras venir de afuera, cualquiera podría elegirlo.

En los dos sentidos pasa lo mismo: **lo que entra, lo que sale y lo que hay en el dominio no son la misma cosa.** Para cada uno hace falta un objeto propio.

---

## 2. DTOs 🔴

### 2.1. Qué son

Un **DTO** (*Data Transfer Object*, objeto de transferencia de datos) es un objeto cuya única responsabilidad es **transportar datos desde y hacia el exterior**. Todo lo que viene de afuera en el cuerpo del pedido llega en un DTO; todo lo que sale hacia afuera sale en un DTO. Las entidades del dominio no cruzan esa frontera.

```
     afuera               el sistema
  ──────────────          ─────────────────────────────────────────
                          DTO de entrada             entidad de dominio
  { "descripcion":  ──►   ProductoCreateRequest ──►  Producto
    "Smart TV 50",        (sin id)                   (con id, tipo,
    "precioBase": 100,                                impuestos…)
    ... }
                          DTO de salida
  { "id": 1,         ◄──  ProductoResponse      ◄──  Producto
    "descripcion": ...    (solo lo que
    ... }                  elijo mostrar)
```

Para qué sirve, en tres frases:

- **Elegís qué sale.** El DTO de salida tiene solo los campos que querés exponer. La contraseña no está.
- **Elegís qué entra.** El DTO de entrada tiene solo lo que el exterior puede mandar. El id no está.
- **Separás la forma de afuera de la forma de adentro.** El dominio puede cambiar sin cambiar la API, y al revés.

⚠️ **¿Y si hoy devuelvo exactamente todo lo que tiene la entidad?** Igual conviene el DTO. Mañana te piden "sacá este campo de la respuesta, no deberíamos estar devolviéndolo": con un DTO ya armado, borrás un campo del DTO y listo, sin tocar el dominio. **Tener DTOs es siempre buena práctica.**

> **Para el parcial, si te preguntan:** *¿Qué es un DTO y por qué no se devuelve directamente la entidad?*
> Un DTO es un objeto cuya única responsabilidad es transportar datos entre el sistema y el exterior; con él se elige qué sale y qué entra sin exponer el dominio. Así no sale lo que no debe (la contraseña de un usuario) ni entra lo que genera el sistema (el id), y la forma de la API queda separada de la del dominio: si mañana hay que sacar un campo de la respuesta, se toca el DTO y no la entidad.

### 2.2. Un DTO por interacción

Cada interacción tiene **su propio** DTO de entrada y de salida, armado para lo que esa interacción necesita. Si varias devuelven exactamente lo mismo, comparten el de salida (`ProductoResponse` lo devuelven el listado, la consulta por id, el alta y la modificación); si una necesita otra cosa, tiene el suyo (`PrecioProductoResponse`). En particular, el DTO para **crear** y el DTO para **modificar** son distintos aunque se parezcan: no siempre se pide lo mismo para dar de alta que para actualizar.

**Nomenclatura**, la más estándar:

| Dirección | Terminación | Ejemplo |
|---|---|---|
| Entrada | `...Request` o `...Input` | `ProductoCreateRequest` |
| Salida | `...Response` o `...Output` | `ProductoResponse` |

Las dos variantes de cada lado son válidas. Lo que importa es usar una de forma consistente en todo el proyecto.

### 2.3. `record`: un DTO en una línea

En las versiones modernas de Java (es estándar desde Java 16) existe el **`record`**: una forma de declarar una clase **inmutable** (sus atributos no cambian después de crearla) que solo transporta datos. Es la forma natural de escribir un **value object** como `DataCalificacion` (M4) y de escribir un DTO.

```java
// Un record: se declara como una clase, pero los atributos van entre paréntesis,
// como si fueran los parámetros de un constructor.
public record ProductoResponse(
    Long id,
    Long comercioId,
    Long tipoProductoId,
    String tipoProductoDescripcion,
    double precioBase,
    String descripcion
) {
}
```

```
// ¿CÓMO FUNCIONA? Qué te da Java por escribir eso
//   · un constructor con los seis parámetros, en ese orden:
//       new ProductoResponse(1L, 1L, 2L, "Electrónico", 100.0, "Smart TV 50")
//   · un método de acceso por cada atributo, con el MISMO nombre (sin "get"):
//       response.id()           → 1
//       response.descripcion()  → "Smart TV 50"
//   · atributos final: no hay setters, no se pueden reasignar después de crearlo
//   · equals, hashCode y toString ya hechos, basados en los atributos
```

⚠️ Los métodos de acceso de un record **no se llaman `getId()`**: se llaman `id()`. Vas a ver `request.comercioId()` en M7.

Se cierra el hilo de M4: **`DataCalificacion` es un value object (inmutable, definido solo por sus datos) que cumple el papel de DTO de entrada.** El controller lo arma con el body del pedido y se lo pasa al service; en Java se escribiría como un `record` con los datos de la calificación (el legajo del alumno, la materia, la nota).

Si el `record` te resulta extraño, un DTO **también puede ser una clase común**, con sus atributos y sus getters (y setters, si hacen falta). Lo que se busca en un DTO, sea clase o record, es que sus datos **no varíen** una vez creado: por eso el record le queda justo.

> 🕳️ **Madriguera — Cuándo un DTO no puede ser record**
> Hay casos (por ejemplo, ciertos formularios web) en los que hace falta una clase con constructor vacío y setters en lugar de un record, y ahí se resigna la inmutabilidad. Se ven cuando aparezcan.
> *Volvé al camino.*

> 🕳️ **Madriguera — Inmutable, pero no tanto**
> Si un atributo es una lista, como `items` en `VentaCreateRequest` (§3.2), el record impide cambiar *qué* lista tiene, pero no agregarle o sacarle elementos. Para lo que se usa acá alcanza con no hacerlo.
> *Volvé al camino.*

### 2.4. El nombre del atributo es la clave del JSON

Cuando llega un pedido con este body:

```json
{
  "comercioId": 1,
  "tipoProductoId": 2,
  "precioBase": 100.0,
  "descripcion": "Smart TV 50"
}
```

el framework lo convierte en un `ProductoCreateRequest` **emparejando cada clave del JSON con el atributo del mismo nombre**. `"comercioId"` va a `comercioId`, `"precioBase"` va a `precioBase`. Y al revés al responder: cada atributo del DTO de salida se vuelve una clave del JSON con su mismo nombre.

⚠️ **Si el nombre no coincide, el dato no llega.** Un JSON con `"comercio_id"` no llena `comercioId`: la clave desconocida se ignora sin error y `comercioId` queda en `null`. Lo que vas a ver es al service quejándose de que falta el `comercioId` (M7), no un aviso de que la clave estaba mal escrita. Si necesitás que la clave del JSON y el atributo se llamen distinto, se mapean, como hiciste en la clase 02 al consumir una API.

> 🕳️ **Madriguera — `@JsonProperty` en un record**
> La misma anotación de la clase 02 (`@JsonProperty("comercio_id")`) se puede poner sobre un atributo de un record para emparejarlo con otra clave.
> *Volvé al camino.*

---

## 3. Los DTOs del servicio de ventas 🔴

Están en `dtos/`, subdivididos para que no sea una ensalada: `producto/`, `venta/`, `error/`. Son ocho, todos records.

### 3.1. Productos

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto;

// ENTRADA para dar de alta un producto. No trae id: lo genera el sistema.
// Trae los ids del comercio y del tipo (no los objetos): con esos ids el service
// va a buscar las entidades en sus repositorios (M7).
public record ProductoCreateRequest(
    Long comercioId,
    Long tipoProductoId,
    double precioBase,
    String descripcion
) {
}
```

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto;

// ENTRADA para modificar un producto. Tampoco trae id: el producto a modificar
// se identifica de otra forma (M8).
public record ProductoUpdateRequest(
    Long comercioId,
    Long tipoProductoId,
    double precioBase,
    String descripcion
) {
}
```

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto;

// SALIDA: un producto como lo ve el exterior. Ya tiene id. Del tipo de producto
// manda solo el id y la descripción, no la lista de impuestos: se elige qué se expone.
public record ProductoResponse(
    Long id,
    Long comercioId,
    Long tipoProductoId,
    String tipoProductoDescripcion,
    double precioBase,
    String descripcion
) {
}
```

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto;

// SALIDA específica del caso de uso "consultar precios": solo lo que tiene
// que ver con el precio de un producto.
public record PrecioProductoResponse(
    Long productoId,
    double precioBase,
    double impuestos,
    double precioFinal
) {
}
```

Fijate en `ProductoCreateRequest` y `ProductoUpdateRequest`: tienen **los mismos cuatro campos**, y aun así son dos DTOs. Hoy coinciden; si mañana modificar pide algo que dar de alta no, cada uno cambia por su lado.

Y fijate en `ProductoUpdateRequest`: **no trae el id del producto a modificar.** Pero para modificar un producto hay que saber cuál es. ¿De dónde sale ese id? No va en el body: va en otro lugar del pedido, y se ve en M8.

> ⚠️ **Objetos y primitivos.** Un atributo de tipo objeto (`Long`, `String`) que no viene en el JSON queda en `null`, y por eso en M7 el service pregunta, por ejemplo, si `comercioId` es `null`. `precioBase`, en cambio, es `double` (primitivo) y no puede valer `null`: cuando viene, lo que el service le controla es que sea mayor que cero. Si directamente no viene, lo decide el convertidor de JSON. Con la configuración de este proyecto (Jackson 3, que viene con Spring Boot 4) la conversión falla antes de llegar al service; con otra configuración podría ponerle `0`, y entonces el service lo rechazaría.

### 3.2. Ventas

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta;

// ENTRADA: un renglón de la venta (qué producto y cuántos).
public record ItemVentaRequest(
    Long productoId,
    int cantidad
) {
}
```

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta;

import java.util.List;

// ENTRADA para registrar una venta: el comercio y la lista de renglones.
// Un DTO puede contener otros DTOs: acá, una lista de ItemVentaRequest.
public record VentaCreateRequest(
    Long comercioId,
    List<ItemVentaRequest> items
) {
}
```

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta;

import java.time.LocalDate;

// SALIDA: la venta registrada, con sus tres totales ya calculados.
// No devuelve los ítems: el DTO de salida lleva solo lo que se eligió exponer.
public record VentaResponse(
    Long id,
    Long comercioId,
    LocalDate fechaRegistro,
    double totalPrecioBase,
    double totalImpuestos,
    double totalFinal
) {
}
```

El JSON que corresponde a un `VentaCreateRequest`:

```json
{
  "comercioId": 1,
  "items": [
    { "productoId": 1, "cantidad": 1 },
    { "productoId": 2, "cantidad": 2 }
  ]
}
```

### 3.3. El DTO de los errores

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.error;

import java.time.Instant;

// SALIDA cuando algo falla: un código corto, un mensaje para leer, y el momento.
public record ErrorResponse(
    String error,
    String message,
    Instant timestamp
) {
}
```

`Instant` es un instante en el tiempo (fecha y hora exactas). Quién arma este DTO y cuándo, en M8.

---

## 4. Excepciones propias 🔴

### 4.1. El caso: el service tiene que decir "no existe"

En M4, el paso 1 de registrar una calificación era: *si el alumno no existe, corto*. En el servicio de ventas pasa lo mismo: si alguien pide el producto 3 y no existe, el service tiene que cortar y avisar. Pero el service **no conoce HTTP** (M4): no puede responder "404". Lo que hace es **lanzar una excepción**, y alguien más arriba la traduce.

Para eso conviene que la excepción diga **qué tipo de problema** fue: no es lo mismo "no existe" (404) que "el pedido está mal" (400). Por eso el servicio tiene sus propias excepciones.

### 4.2. Chequeadas y no chequeadas

En Java hay dos familias de excepciones:

| | **Chequeadas** | **No chequeadas** |
|---|---|---|
| Heredan de… | `Exception` (salvo la rama de `RuntimeException`) | `RuntimeException` |
| ¿Te obligan a algo? | **Sí**: o las atrapás con `try/catch`, o las declarás en la firma del método (`throws ...`). Si no, el código no compila y el IDE te lo marca | **No**: podés lanzarlas sin `try/catch` y sin declararlas |

Las excepciones propias del servicio **heredan de `RuntimeException`**: son no chequeadas. Así, el service puede lanzarlas en cualquier punto del flujo sin llenar cada método de `try/catch` o de `throws`, y la excepción "sube" sola hasta quien la vaya a traducir.

### 4.3. Las tres excepciones del servicio

Están en `exceptions/`. Las tres tienen la misma forma:

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions;

// "Lo que pediste no existe": no se encontró un producto, un comercio o un tipo de producto.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);          // el mensaje viaja con la excepción
    }
}
```

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions;

// "El pedido está mal o viola una regla": un campo obligatorio que falta,
// un precio que no es mayor a cero, un producto que no es del comercio.
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions;

// "Lo que pedís choca con el estado actual". Está definida y preparada,
// pero en este proyecto ninguna clase la lanza todavía.
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
```

| Excepción | Significa | Se va a traducir en (M8) |
|---|---|---|
| `ResourceNotFoundException` | No existe | 404 |
| `BusinessException` | Pedido inválido o regla violada | 400 |
| `ConflictException` | Conflicto con el estado actual (sin uso todavía) | 409 |

Hay una cuarta que no es propia pero va a aparecer: **`IllegalArgumentException`**, una no chequeada que ya trae Java. La lanza el **dominio** de la clase 03 (`ItemVenta` con cantidad `<= 0`, `Comercio.agregarVenta` con un producto que no es propio).

¿Quién atrapa todas estas excepciones y las convierte en un código de estado? Si nadie lo hiciera, cualquiera de estas excepciones terminaría en un **500** (error interno del servidor), que es justo lo que no se quiere. Eso se ve en M8.

> **Para el parcial, si te preguntan:** *¿Por qué las excepciones propias del servicio heredan de `RuntimeException`?*
> Porque las no chequeadas no obligan a atraparlas ni a declararlas en cada firma: el service las lanza donde detecta el problema y la excepción sube sola hasta la capa de presentación, que es la que conoce el protocolo y la traduce a un código de estado. Tener una excepción por tipo de problema (no existe, pedido inválido, conflicto) es lo que permite traducir cada una a un código distinto.

---

## 5. Estado del proyecto 🟡

```
sales_service/
├── SalesServiceApplication.java    ✅
├── models/entities/...             ✅ (clase 03)
├── repositories/ + inmemory/       ✅ (M5)
├── utils/                          ✅ (M5)
├── dtos/
│   ├── producto/                   ✅ 4 records
│   ├── venta/                      ✅ 3 records
│   └── error/                      ✅ ErrorResponse
└── exceptions/                     ✅ 3 excepciones
```

Compila. Todavía nadie usa estas clases.

---

## 6. Lo que te llevás de este módulo

- **DTO:** objeto que transporta datos desde y hacia el exterior. Las entidades no cruzan esa frontera. Sirve para elegir qué sale (no la contraseña), qué entra (no el id) y para separar la forma de afuera de la de adentro. Siempre es buena práctica.
- **Un DTO por interacción**; crear y modificar, separados. `...Request`/`...Input` para entrar, `...Response`/`...Output` para salir.
- **`record`:** clase inmutable de datos; constructor, métodos de acceso `campo()`, sin setters. Ideal para DTOs y value objects.
- **El nombre del atributo es la clave del JSON.** Si no coincide, el dato no llega.
- **Excepciones propias**, no chequeadas: `ResourceNotFoundException` (→ 404), `BusinessException` (→ 400), `ConflictException` (→ 409, sin uso). El dominio, además, lanza `IllegalArgumentException`.
- `DataCalificacion` (M4): un value object que hace de DTO de entrada, escrito como `record`.
- Quedan abiertos: dónde va el id del producto a modificar (M8) y quién atrapa las excepciones (M8).

---

## ✅ Checkpoint del Módulo 6

Respondé de memoria. Las respuestas van al complemento.

1. ¿Qué es un DTO? Da un ejemplo de algo que no tiene que salir en un DTO de salida y uno de algo que no tiene que venir en un DTO de entrada.
2. Si hoy el DTO de salida tiene exactamente los mismos campos que la entidad, ¿para qué tenerlo?
3. `ProductoCreateRequest` y `ProductoUpdateRequest` tienen los mismos campos. ¿Por qué son dos?
4. ¿Qué te da Java cuando declarás un `record`? ¿Cómo se llama el método que devuelve el atributo `comercioId`?
5. Llega un JSON con `"comercio_id": 1` para un `ProductoCreateRequest`, cuyo atributo se llama `comercioId`. ¿Con qué valor queda `comercioId`? ¿Te avisa algo el framework? ¿Cómo lo resolverías si no podés cambiar el JSON?
6. `ProductoResponse` manda `tipoProductoId` y `tipoProductoDescripcion`, pero no el `TipoProducto` con sus impuestos. ¿Qué idea de los DTOs muestra eso? Si alguien necesita saber cuánto paga de impuestos un producto, ¿qué DTO de este servicio le responde?
7. ¿Qué diferencia hay entre una excepción chequeada y una no chequeada? ¿De cuál heredan las del servicio, y por qué?
8. ¿Por qué el service lanza una excepción en lugar de devolver directamente un 404?

---

## Qué viene después

**M7 — La capa intermedia: services que orquestan el caso de uso.** Con repositorios, DTOs y excepciones listos, aparece el corazón del servicio: `ProductoServiceImpl` y `VentaServiceImpl`. Ahí se ven juntos la inyección de dependencias, el flujo de cada caso de uso, qué se hace con un `Optional` vacío y quién arma cada DTO.

**FIN DEL MÓDULO 6**
