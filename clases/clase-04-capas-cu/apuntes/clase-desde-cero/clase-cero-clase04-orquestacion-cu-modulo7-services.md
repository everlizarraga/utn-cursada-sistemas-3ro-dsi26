# 🧩 Clase desde cero — Clase 04 · Módulo 7: La capa intermedia. Services que orquestan el caso de uso

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 7 de 9.** Fase B (código). El módulo más denso de la clase.

## Sobre este documento

**Qué cubre:** el corazón del servicio de ventas: `ProductoService` / `ProductoServiceImpl` y `VentaService` / `VentaServiceImpl`. Acá se juntan todas las piezas: la **inyección de dependencias** que hace Spring, el **flujo de cada caso de uso** (validar, buscar, instanciar, asociar, guardar), qué se hace con un `Optional` vacío, y quién arma cada DTO de salida.
**Qué NO cubre:** quién llama a los services ni cómo sus DTOs se vuelven JSON (M8), ni de dónde salen el comercio y los tipos de producto con los que se trabaja (M9).

## De dónde venís

- De **M4**: qué es un service (dominio intermedio, reglas del flujo), las reglas **2** (las entidades de dominio las instancian los services) y **3** (un service conoce entidades, repositorios y otros services), y que "un service por entidad" es lo habitual.
- De **M5**: los cuatro repositorios, `Optional`, beans y `@Service`, y que el service iba a recibir un `ProductoRepository` sin saber qué hay detrás.
- De **M6**: los DTOs (records, con métodos de acceso `campo()`) y las excepciones propias (`ResourceNotFoundException`, `BusinessException`).
- De la **preclase 04**: inyección de dependencias por constructor.
- De la **clase 03**: `Comercio.agregarProducto`, `Comercio.agregarVenta` (valida y notifica), `Producto.totalImpuestos()` y `precioFinal()`, `Venta.agregarItem`, `ItemVenta`.

---

## 1. Del enunciado a los métodos 🔴

El servicio de ventas tiene tres casos de uso. Antes de abrir código, la pregunta de diseño: **¿qué métodos necesita cada service?** La respuesta sale del enunciado, no de una lista de "lo que suele tener un service":

| Caso de uso del enunciado | Lo que pide | Método |
|---|---|---|
| **Gestionar productos** | Dar de alta | `ProductoService.create` |
| | Modificar | `ProductoService.update` |
| | Eliminar | `ProductoService.deleteById` |
| | Listar | `ProductoService.findAll` |
| | Ver uno (implícito en gestionar) | `ProductoService.findById` |
| **Consultar precios** | Precio final de un producto | `ProductoService.getPrecio` |
| **Registrar ventas** | Dar de alta una venta | `VentaService.create` |

Fijate en `VentaService`: **un solo método**. El enunciado pide registrar ventas, no listarlas, ni modificarlas, ni eliminarlas. Entonces no se escriben.

> **Regla práctica (KISS y YAGNI):** un service tiene **los métodos que sus casos de uso piden, y ninguno más**. Si el caso de uso es solo "dar de alta", hay solo un `create`. Lo que no se va a usar no se escribe. (*KISS*: "mantenelo simple". *YAGNI*: "no lo vas a necesitar".)

Dos honestidades sobre la tabla. Leído al pie de la letra, "los comercios deben poder listar los productos **que venden**" pide los productos de *un* comercio; `findAll` lista todos. Para este ejemplo alcanza, y si hiciera falta se agrega el método que eso pide (por ejemplo, uno que reciba el `comercioId`). Y `findById` es el único que el enunciado no nombra: se suma porque modificar, eliminar y consultar el precio trabajan sobre un producto identificado, y poder verlo es parte de gestionarlo.

> **Para el parcial, si te preguntan:** *¿Cómo decidís qué métodos tiene un service?*
> Por los casos de uso que tiene que soportar: cada operación que el enunciado pide se vuelve un método, y nada más. Un service no lleva un ABM completo (alta, baja y modificación) "por las dudas": si el caso de uso solo pide dar de alta, solo hay un `create` (KISS, YAGNI).

---

## 2. Interfaz e implementación 🔴

Cada service tiene dos archivos: una **interfaz** en `services/` y su **implementación** en `services/impl/`.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.services;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.PrecioProductoResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoCreateRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoUpdateRequest;

import java.util.List;

// El contrato del service de productos. Recibe DTOs de entrada y devuelve DTOs de salida:
// hacia arriba, nunca expone una entidad.
public interface ProductoService {

    List<ProductoResponse> findAll();

    ProductoResponse findById(Long id);

    ProductoResponse create(ProductoCreateRequest request);

    ProductoResponse update(Long id, ProductoUpdateRequest request);   // el id llega aparte (M8)

    void deleteById(Long id);

    PrecioProductoResponse getPrecio(Long id);
}
```

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.services;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta.VentaCreateRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta.VentaResponse;

public interface VentaService {

    VentaResponse create(VentaCreateRequest request);
}
```

¿Por qué una interfaz, si hay una sola implementación? Por lo mismo que el repositorio (M5 §5.5), una capa más arriba: **el controller (M8) va a conocer `ProductoService`, nunca `ProductoServiceImpl`.** Si mañana hace falta otra implementación con reglas distintas, se escribe otra clase que implemente la misma interfaz, se le pasa el `@Service` a esa en lugar de la vieja, y el controller no se entera. Es minimizar el acoplamiento entre capas, conociendo solo firmas.

⚠️ **Una sola implementación anotada por interfaz.** Si hubiera dos clases que implementan `ProductoService` y las dos tuvieran `@Service`, cuando alguien pida "un `ProductoService`" Spring no sabría cuál dar, y la aplicación no arranca. Por eso: **una sola** con `@Service`.

> 🕳️ **Madriguera — Profiles**
> Existe una forma de tener varias implementaciones y que Spring elija una según el ambiente en el que corre (desarrollo, pruebas, producción): se llama *profiles*. Es configuración más avanzada.
> *Volvé al camino.*

---

## 3. `ProductoServiceImpl`, completo 🔴

Primero el archivo entero, con comentarios por bloque. Después, en §4 a §6, cada parte en detalle.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.services.impl;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.PrecioProductoResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoCreateRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.producto.ProductoUpdateRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions.BusinessException;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions.ResourceNotFoundException;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.comercio.Comercio;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.TipoProducto;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.ComercioRepository;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.ProductoRepository;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.TipoProductoRepository;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.services.ProductoService;
import org.springframework.stereotype.Service;

import java.util.List;

// @Service: Spring crea la (única) instancia de esta clase al arrancar.
@Service
public class ProductoServiceImpl implements ProductoService {

    // ── Dependencias: los tres repositorios que este service usa ────────────────
    // Declarados por su INTERFAZ (no saben que detrás hay listas en memoria).
    // final: una vez asignados en el constructor, no cambian más.
    private final ProductoRepository productoRepository;
    private final ComercioRepository comercioRepository;
    private final TipoProductoRepository tipoProductoRepository;

    // ── Inyección por constructor ───────────────────────────────────────────────
    // Nadie de nuestro código llama a este constructor: lo llama Spring, y le pasa
    // los beans de repositorio que creó (M5).
    public ProductoServiceImpl(ProductoRepository productoRepository,
                               ComercioRepository comercioRepository,
                               TipoProductoRepository tipoProductoRepository) {
        this.productoRepository = productoRepository;
        this.comercioRepository = comercioRepository;
        this.tipoProductoRepository = tipoProductoRepository;
    }

    // ── Listar ─────────────────────────────────────────────────────────────────
    // Trae las entidades del repo y transforma cada una en su DTO de salida.
    @Override
    public List<ProductoResponse> findAll() {
        return productoRepository.findAll().stream().map(this::toResponse).toList();
    }

    // ── Ver uno ────────────────────────────────────────────────────────────────
    // Si no existe, getProductoOrThrow lanza la excepción y el método corta ahí.
    @Override
    public ProductoResponse findById(Long id) {
        return toResponse(getProductoOrThrow(id));
    }

    // ── Dar de alta ────────────────────────────────────────────────────────────
    // validar → buscar comercio y tipo → instanciar → asociar al comercio → guardar → DTO
    @Override
    public ProductoResponse create(ProductoCreateRequest request) {
        validateCreateRequest(request);
        Comercio comercio = getComercioOrThrow(request.comercioId());
        TipoProducto tipo = getTipoOrThrow(request.tipoProductoId());
        Producto producto = new Producto(null, tipo, request.precioBase(), request.descripcion().trim());
        comercio.agregarProducto(producto);
        productoRepository.save(producto);
        return toResponse(producto, comercio);
    }

    // ── Modificar ──────────────────────────────────────────────────────────────
    // validar → buscar producto, comercio y tipo → verificar que el producto sea de
    // ese comercio → cambiar los datos → guardar → DTO
    @Override
    public ProductoResponse update(Long id, ProductoUpdateRequest request) {
        validateUpdateRequest(request);
        Producto producto = getProductoOrThrow(id);
        Comercio comercio = getComercioOrThrow(request.comercioId());
        TipoProducto tipo = getTipoOrThrow(request.tipoProductoId());

        if (!comercio.getProductos().contains(producto)) {
            throw new BusinessException("El producto no pertenece al comercio indicado");
        }

        producto.setTipo(tipo);
        producto.setPrecioBase(request.precioBase());
        producto.setDescripcion(request.descripcion().trim());
        productoRepository.save(producto);
        return toResponse(producto, comercio);
    }

    // ── Eliminar ───────────────────────────────────────────────────────────────
    // Lo saca de la lista de su comercio y del repositorio.
    @Override
    public void deleteById(Long id) {
        Producto producto = getProductoOrThrow(id);
        Comercio comercio = findComercioOwner(producto);
        comercio.getProductos().remove(producto);
        productoRepository.delete(producto);
    }

    // ── Consultar precio ───────────────────────────────────────────────────────
    // El cálculo es del dominio (clase 03); el service solo arma el DTO con los resultados.
    @Override
    public PrecioProductoResponse getPrecio(Long id) {
        Producto producto = getProductoOrThrow(id);
        return new PrecioProductoResponse(producto.getId(), producto.getPrecioBase(), producto.totalImpuestos(), producto.precioFinal());
    }

    // ── Mapeo entidad → DTO de salida ──────────────────────────────────────────
    // Dos versiones: si ya tengo el comercio, lo uso; si no, lo busco.
    private ProductoResponse toResponse(Producto producto) {
        Comercio comercio = findComercioOwner(producto);
        return toResponse(producto, comercio);
    }

    private ProductoResponse toResponse(Producto producto, Comercio comercio) {
        return new ProductoResponse(
            producto.getId(),
            comercio.getId(),
            producto.getTipo().getId(),
            producto.getTipo().getDescripcion(),
            producto.getPrecioBase(),
            producto.getDescripcion()
        );
    }

    // ── ¿De qué comercio es este producto? ─────────────────────────────────────
    // El producto no conoce a su comercio: hay que recorrer los comercios (§6).
    private Comercio findComercioOwner(Producto producto) {
        return comercioRepository.findAll().stream()
            .filter(c -> c.getProductos().contains(producto))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró comercio para el producto " + producto.getId()));
    }

    // ── Validaciones del pedido (a mano) ───────────────────────────────────────
    private void validateCreateRequest(ProductoCreateRequest request) {
        if (request == null) throw new BusinessException("El body del producto es obligatorio");
        if (request.comercioId() == null) throw new BusinessException("El comercioId es obligatorio");
        if (request.tipoProductoId() == null) throw new BusinessException("El tipoProductoId es obligatorio");
        validateCommonFields(request.precioBase(), request.descripcion());
    }

    private void validateUpdateRequest(ProductoUpdateRequest request) {
        if (request == null) throw new BusinessException("El body del producto es obligatorio");
        if (request.comercioId() == null) throw new BusinessException("El comercioId es obligatorio");
        if (request.tipoProductoId() == null) throw new BusinessException("El tipoProductoId es obligatorio");
        validateCommonFields(request.precioBase(), request.descripcion());
    }

    // Lo que piden igual el alta y la modificación.
    private void validateCommonFields(double precioBase, String descripcion) {
        if (descripcion == null || descripcion.isBlank()) throw new BusinessException("La descripcion del producto es obligatoria");
        if (precioBase <= 0) throw new BusinessException("El precio base debe ser mayor a cero");
    }

    // ── Buscar o cortar ────────────────────────────────────────────────────────
    // Piden al repo y, si el Optional viene vacío, lanzan ResourceNotFoundException.
    private Comercio getComercioOrThrow(Long comercioId) {
        return comercioRepository.findById(comercioId)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró comercio con id " + comercioId));
    }

    private Producto getProductoOrThrow(Long productoId) {
        return productoRepository.findById(productoId)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró producto con id " + productoId));
    }

    private TipoProducto getTipoOrThrow(Long tipoProductoId) {
        return tipoProductoRepository.findById(tipoProductoId)
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró tipo de producto con id " + tipoProductoId));
    }
}
```

---

## 4. Las dependencias: cómo llegan los repositorios 🔴

Volvé a M4: en el código del `CalificacionesService`, los repositorios eran atributos y quedó pendiente *"cómo llegan ahí"*. Acá está:

```
   Spring, al arrancar
   ───────────────────
   1. crea los beans de repositorio (M5):    InMemoryProductoRepository, InMemoryComercioRepository,
                                             InMemoryTipoProductoRepository, ...
   2. encuentra @Service ProductoServiceImpl
   3. mira su constructor: pide un ProductoRepository, un ComercioRepository y un TipoProductoRepository
   4. busca, entre sus beans, uno que cumpla cada interfaz
   5. llama al constructor pasándoselos      →  new ProductoServiceImpl(prodRepo, comRepo, tipoRepo)
   6. guarda el service como bean (para dárselo después al controller, M8)
```

Eso es la **inyección de dependencias por constructor** de la preclase 04, solo que **la hace Spring**. El service **declara** lo que necesita (en su constructor) y **no lo crea**: nunca hay un `new InMemoryProductoRepository()` en este archivo. Por eso el service no sabe que detrás hay una lista en memoria, y por eso cambiar la persistencia no lo toca.

No hace falta ninguna anotación en el constructor: como la clase tiene uno solo, Spring lo usa directamente. (`@Autowired` haría falta para indicarle cuál usar si hubiera varios.)

Se cierran dos hilos: **¿quién instancia al service?** Spring, porque tiene `@Service`. **¿Cómo le llegan los repositorios?** Spring se los pasa al constructor.

El `final` en los tres atributos: una vez asignados en el constructor, no cambian durante toda la ejecución.

> 🕳️ **Madriguera — Otras formas de inyectar**
> La inyección por constructor no es la única que soporta Spring. Otra es poner `@Autowired` directamente sobre un atributo, como hiciste en el test de la clase 02. En esta materia: constructor en el código de las capas; `@Autowired` en atributos aparece en los tests (M9).
> *Volvé al camino.*

> **Para el parcial, si te preguntan:** *¿Quién crea los repositorios que usa un service en Spring Boot, y cómo le llegan?*
> Los crea Spring, porque sus clases están anotadas con `@Repository`, y se los pasa al service por su constructor cuando lo instancia (también lo crea Spring, por `@Service`): es inyección de dependencias hecha por el framework. El service los declara por su interfaz y nunca hace un `new`, así que no sabe qué implementación hay detrás.

---

## 5. Los casos de uso, uno por uno 🔴

### 5.1. Listar y ver uno

```java
return productoRepository.findAll().stream().map(this::toResponse).toList();
```

Trae todas las entidades, y a cada una la pasa por `toResponse` para convertirla en DTO. `this::toResponse` es una **referencia a método**: una forma corta de escribir la lambda `p -> this.toResponse(p)`. Se puede usar cuando la lambda no hace nada más que llamar a un método con su parámetro: pasándoselo tal cual (`p -> this.toResponse(p)` → `this::toResponse`) o llamando el método sobre él (`i -> i.getProducto()` → `ItemVenta::getProducto`, como en `Comercio`). Las dos formas son equivalentes; el IDE te sugiere la corta. Hay dos `toResponse` (§6): como `map` le pasa un solo elemento, Java usa el de un parámetro.

```java
return toResponse(getProductoOrThrow(id));
```

Y acá se cierra el hilo de M5: **¿qué se hace con un `Optional` vacío?**

```java
private Producto getProductoOrThrow(Long productoId) {
    return productoRepository.findById(productoId)
        .orElseThrow(() -> new ResourceNotFoundException("No se encontró producto con id " + productoId));
}
```

```
// ¿CÓMO FUNCIONA?
//   findById devuelve un Optional:
//     · con el producto adentro  → orElseThrow lo saca de la caja y lo devuelve
//     · vacío                    → orElseThrow ejecuta la lambda () -> new ... (sin parámetros:
//                                  solo fabrica la excepción) y la lanza
//   Resultado esperado (con los productos 1 y 2 cargados):
//     getProductoOrThrow(1L)  → el producto 1
//     getProductoOrThrow(3L)  → ResourceNotFoundException("No se encontró producto con id 3")
//                               (y el método del service que lo llamó corta ahí; afuera eso será un 404, M8)
```

Este patrón (pedir al repo; si no está, lanzar `ResourceNotFoundException`) es el paso 1 del flujo de M4 (*"validar que el alumno exista"*), escrito en Java. Hay uno por entidad: `getProductoOrThrow`, `getComercioOrThrow`, `getTipoOrThrow`.

### 5.2. Dar de alta

```java
validateCreateRequest(request);
Comercio comercio = getComercioOrThrow(request.comercioId());
TipoProducto tipo = getTipoOrThrow(request.tipoProductoId());
Producto producto = new Producto(null, tipo, request.precioBase(), request.descripcion().trim());
comercio.agregarProducto(producto);
productoRepository.save(producto);
return toResponse(producto, comercio);
```

Las siete líneas, en orden: 1. el pedido está bien formado · 2. el comercio existe · 3. el tipo existe · 4. instanciar · 5. asociar al comercio · 6. guardar (acá recibe su id) · 7. DTO de salida.

Es el flujo de M4, punto por punto. Y en el paso 4 está la **Regla 2** en acción: la entidad `Producto` la instancia **el service**. El DTO trajo **ids** (`comercioId`, `tipoProductoId`); con esos ids el service va a buscar las **entidades** a sus repositorios, y trabaja con objetos. El `null` del primer parámetro es el id: todavía no tiene, lo va a asignar el repositorio al guardar (M5). Ese constructor con todos los atributos es el que genera `@AllArgsConstructor` de Lombok en `Producto`. `trim()` le saca los espacios del principio y del final a la descripción.

El orden de los pasos 5 y 6 importa: **primero el dominio queda consistente** (el producto es del comercio), **después se persiste**.

> 🔧 **Versión floja — Guardar antes de asociar**
>
> ```java
> // ❌ compila y, hoy, anda
> Producto producto = new Producto(null, tipo, request.precioBase(), request.descripcion().trim());
> productoRepository.save(producto);       // queda guardado...
> comercio.agregarProducto(producto);      // ...y recién después tiene dueño
> ```
>
> **Dónde falla:** entre el `save` y la asociación, el producto está guardado **sin comercio**. Hoy entre esas dos líneas no hay nada que pueda fallar; el día que lo haya (otra validación, una excepción del dominio), queda guardado sin dueño. Y como el listado busca el dueño de cada producto (`findComercioOwner`, §6), con un solo producto así **el `GET` de productos responde 404 ("No se encontró comercio para el producto N") a todos**. Ni siquiera se puede borrar por la API: `deleteById` también busca su dueño. La regla: primero dejar el dominio consistente, después guardar.

Otra cosa: devolver el DTO completo del producto creado **no es obligatorio**. Un alta podría devolver solo el id, o nada. Acá se devuelve el producto como quedó, que le sirve a quien lo creó para ver el id asignado.

### 5.3. Modificar

Mismo esqueleto que el alta, con dos diferencias. El producto **ya existe**: se busca por el `id` (que llega aparte, como parámetro, y no dentro del DTO: M8 muestra de dónde sale). Y antes de modificar, se verifica una regla del flujo: **el producto tiene que ser del comercio que se indica**; si no, `BusinessException`. Después, los setters (que `Producto` tiene por `@Setter` de Lombok) y el `save`.

### 5.4. Eliminar y consultar precio

`deleteById` busca el producto, busca su comercio, lo saca de la lista del comercio y lo elimina del repositorio: deja consistentes las dos cosas.

`getPrecio` es el caso más claro de la división de responsabilidades: **el cálculo** (`totalImpuestos()`, `precioFinal()`) **es del dominio**, de la clase 03, y el service no calcula nada: solo le pide los resultados al producto y arma el `PrecioProductoResponse`. Regla de negocio en la entidad; flujo en el service.

### 5.5. Las validaciones

`validateCreateRequest`, `validateUpdateRequest` y `validateCommonFields` revisan el pedido **a mano**, antes de hacer nada: que venga el body, que vengan los ids, que la descripción no esté vacía, que el precio sea mayor que cero. Si algo falla, `BusinessException` (→ 400 en M8).

- `if (request == null)` nunca se cumple si el pedido llega por el controller (el framework rechaza antes un pedido sin body; a dónde va ese error, M8). Solo serviría si alguien llamara al service por otro camino; no molesta.
- Estas son las "validaciones mínimas" que M4 dijo que **podían** estar en el controller. Acá están en el service. Las dos ubicaciones son válidas.

> 🕳️ **Madriguera — Validaciones declarativas**
> En lugar de escribir los `if` a mano, se pueden poner anotaciones sobre los atributos del DTO (por ejemplo, "este campo es obligatorio", "este número tiene que ser positivo") y dejar que una biblioteca valide sola antes de llegar al service. Es más prolijo, y agrega otra capa de "magia" del framework.
> *Volvé al camino.*

---

## 6. `toResponse` y `findComercioOwner` 🔴

### 6.1. `toResponse`: el mapper

`toResponse` es un **mapper**: un método que transforma una entidad en su DTO de salida. Tiene dos versiones (sobrecarga):

- `toResponse(producto, comercio)`: cuando el comercio ya se tiene a mano (en `create` y `update`), lo usa directo.
- `toResponse(producto)`: cuando no se tiene (en `findAll` y `findById`), lo busca con `findComercioOwner`.

💡 Si en algún lado ves un `toResponse` que, para armar el DTO, vuelve a buscar el comercio y el tipo en sus repositorios, es equivalente: llega al mismo DTO. Cuando el objeto ya está a mano (`producto.getTipo()`, o el comercio en `create`), no hace falta ir a buscarlo.

> 🕳️ **Madriguera — El mapper afuera**
> `toResponse` se puede sacar del service a una clase propia (`ProductoMapper`) que solo se ocupe de transformar, para que el service quede más limpio. También existen bibliotecas que generan esos mappers automáticamente (por ejemplo, MapStruct). A veces igual hay que mapear a mano, cuando algún campo del DTO se calcula.
> *Volvé al camino.*

### 6.2. `findComercioOwner`: una decisión de alcance 🟡

¿Y por qué hay que buscar el comercio? Porque en el dominio de la clase 03, **el comercio conoce a sus productos, pero el producto no conoce a su comercio**: la flecha va en un solo sentido. Para saber de quién es un producto, la única forma es **recorrer todos los comercios** y preguntarle a cada uno si lo tiene:

```java
return comercioRepository.findAll().stream()
    .filter(c -> c.getProductos().contains(producto))
    .findFirst()
    .orElseThrow(...);
```

> ⚠️ **Nota de alcance.** Recorrer todos los comercios por cada producto funciona para este ejemplo, y está bien que esté así: el objetivo del ejemplo es **mostrar las capas**, no resolver eficiencia, y complicar el dominio no aportaba a eso. En un sistema real **no se hace así**: con miles de comercios y miles de productos, listar productos sería recorrer todo por cada uno. No escala. Se resolvería en el diseño (por ejemplo, haciendo que el producto conozca a su comercio) o en la persistencia, con una consulta.

Y un detalle de Java que hace que esto funcione: `contains(producto)` compara con `equals`, y `Producto` **no define `equals`**: entonces compara **si es el mismo objeto**. En memoria siempre lo es (§5.2: `create` agrega la misma instancia al comercio y al repositorio), y por eso anda. Con base de datos, el mismo producto puede llegar como dos objetos distintos (por ejemplo, leído en dos pedidos diferentes), y habría que definir `equals` (y su compañero `hashCode`) comparando, por ejemplo, el id.

---

## 7. `VentaServiceImpl` 🔴

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.services.impl;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta.ItemVentaRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta.VentaCreateRequest;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.dtos.venta.VentaResponse;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions.BusinessException;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.exceptions.ResourceNotFoundException;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.comercio.Comercio;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.ItemVenta;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.Venta;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.ComercioRepository;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.ProductoRepository;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.VentaRepository;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.services.VentaService;
import org.springframework.stereotype.Service;

@Service
public class VentaServiceImpl implements VentaService {

    // ── Dependencias, inyectadas por constructor (igual que en productos) ───────
    private final ComercioRepository comercioRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;

    public VentaServiceImpl(ComercioRepository comercioRepository,
                            ProductoRepository productoRepository,
                            VentaRepository ventaRepository) {
        this.comercioRepository = comercioRepository;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    public VentaResponse create(VentaCreateRequest request) {
        // ── 1. El pedido está bien formado; el comercio existe ─────────────────
        validateRequest(request);
        Comercio comercio = comercioRepository.findById(request.comercioId())
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró comercio con id " + request.comercioId()));

        // ── 2. Armar la venta: por cada renglón, buscar el producto, verificar que
        //       sea de este comercio, y agregar el ítem ──────────────────────────
        Venta venta = new Venta(null);
        for (ItemVentaRequest itemRequest : request.items()) {
            Producto producto = productoRepository.findById(itemRequest.productoId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró producto con id " + itemRequest.productoId()));
            if (!comercio.getProductos().contains(producto)) {
                throw new BusinessException("Todos los productos de la venta deben pertenecer al comercio indicado");
            }
            venta.agregarItem(new ItemVenta(producto, itemRequest.cantidad()));
        }

        // ── 3. Guardar (la venta recibe su id) y DESPUÉS registrarla en el
        //       comercio, que es lo que notifica a los observadores ─────────────
        ventaRepository.save(venta);
        try {
            comercio.agregarVenta(venta);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ex.getMessage());
        }

        // ── 4. DTO de salida con los totales (los calcula el dominio) ──────────
        return new VentaResponse(
            venta.getId(),
            comercio.getId(),
            venta.getFechaRegistro(),
            venta.totalPrecioBase(),
            venta.totalImpuestos(),
            venta.totalFinal()
        );
    }

    // ── Validaciones del pedido: body, comercio, al menos un ítem, y cada ítem
    //    con producto y cantidad positiva ───────────────────────────────────────
    private void validateRequest(VentaCreateRequest request) {
        if (request == null) throw new BusinessException("El body de la venta es obligatorio");
        if (request.comercioId() == null) throw new BusinessException("El comercioId es obligatorio");
        if (request.items() == null || request.items().isEmpty()) throw new BusinessException("La venta debe incluir al menos un item");
        for (ItemVentaRequest item : request.items()) {
            if (item.productoId() == null) throw new BusinessException("Cada item debe incluir productoId");
            if (item.cantidad() <= 0) throw new BusinessException("La cantidad de cada item debe ser mayor a cero");
        }
    }
}
```

Es el mismo patrón que productos, con un caso de uso más rico: el service **instancia dos entidades** (`Venta`, y un `ItemVenta` por renglón), las **configura** (`agregarItem`), **persiste la venta** (los ítems van adentro: guardado en cascada, M3) y le pide al dominio que **registre** la venta.

### 7.1. El orden: guardar y después registrar

`comercio.agregarVenta(venta)` es el método de la clase 03 que, además de agregar la venta, **avisa a todos los observadores** del comercio. Por eso va **después** del `save`: el `save` es lo que le asigna el id a la venta. Al revés, los observadores recibirían una venta con id `null`. En este proyecto no se nota, porque el comercio que se carga al arrancar (M9) no tiene observadores; el orden está igual para que el día que los tenga, reciban la venta completa.

¿Contradice esto la regla de §5.2? No: la regla protege contra lo que muestra la 🔧, que algo quede guardado sin dueño. Acá ese riesgo se elimina antes: el loop ya verificó que todos los productos son del comercio, así que `agregarVenta` no va a rechazar la venta (§7.2). Lo que obliga a guardar primero es que asociar, en este caso, también **avisa a terceros**, y ese aviso necesita el id. Regla completa: **primero dejar el dominio consistente; si un paso dispara efectos hacia afuera que necesitan lo que da el guardado, se guarda antes, pero solo después de haber verificado todo lo que podría fallar.**

### 7.2. Dos chequeos y un `try/catch` 🟡

Que los productos sean del comercio se verifica **dos veces**: en el loop del service (`if (!comercio.getProductos().contains(producto))`) y adentro de `Comercio.agregarVenta`, que lanza `IllegalArgumentException` si no lo son (es una regla de negocio de la clase 03). El `try/catch` traduce esa excepción del dominio a la propia del servicio (`BusinessException`). Como el chequeo del service ya corrió, en la práctica `agregarVenta` no va a fallar acá; y eso es también lo que hace seguro haber guardado la venta antes: no queda una venta guardada que el comercio después rechace.

### 7.3. ¿Y los permisos? 🟡

En este proyecto **no hay usuarios ni permisos**: el ejemplo se concentra en las capas. Si los hubiera, una opción es la del `CalificacionesService` de M4: **la primera línea de cada método del service que ejecuta un caso de uso protegido** (`create`, `update`, `deleteById`), antes de tocar nada. Las otras dos (controller o middleware) siguen valiendo (M4 §3.1). Con eso se cierra el último tramo del hilo que abrió M1 (*¿quién verifica que puede hacerlo?*).

---

## 8. Estado del proyecto 🟡

```
sales_service/
├── SalesServiceApplication.java    ✅
├── models/entities/...             ✅ (clase 03)
├── repositories/ + inmemory/       ✅ (M5)
├── utils/                          ✅ (M5)
├── dtos/ y exceptions/             ✅ (M6)
└── services/                       ✅ 2 interfaces
    └── impl/                       ✅ 2 implementaciones (@Service)
```

Si lo levantás ahora, Spring crea los cuatro repositorios y los dos services, y les pasa a los services sus repositorios… y no pasa nada más: todavía no hay ningún endpoint que los llame.

---

## 9. Lo que te llevás de este módulo

- **Métodos del service = operaciones que piden los casos de uso.** Ni uno más (KISS, YAGNI). `VentaService` tiene uno solo.
- **Interfaz + implementación**: el controller va a conocer la interfaz. Una sola clase con `@Service` por interfaz.
- **Inyección por constructor, hecha por Spring**: el service declara sus repositorios (por interfaz, `final`) y Spring se los pasa. Nunca un `new` de un repositorio.
- **`Optional` vacío → `orElseThrow` → `ResourceNotFoundException`** (será 404).
- **El flujo de cada caso de uso**: validar, buscar entidades por id, instanciar (Regla 2), asociar, guardar, devolver DTO. Primero el dominio consistente, después persistir; si la asociación notifica a otros (la venta), se guarda antes, con todo ya verificado.
- **Las reglas de negocio quedan en el dominio** (`precioFinal`, `agregarVenta`); el service no calcula.
- **`findComercioOwner`**: decisión de alcance del ejemplo; no escala. `Producto` sin `equals`: anda porque en memoria son los mismos objetos.
- Quedan abiertos: quién llama al service y quién convierte sus DTOs en JSON (M8).

---

## ✅ Checkpoint del Módulo 7

Respondé de memoria. Las respuestas van al complemento.

1. ¿De dónde salen los métodos de `ProductoService`? ¿Por qué `VentaService` tiene uno solo?
2. ¿Por qué el controller va a conocer `ProductoService` y no `ProductoServiceImpl`? ¿Qué pasa si dos clases que implementan `ProductoService` tienen `@Service`?
3. Explicá, paso a paso, qué hace Spring para que `ProductoServiceImpl` tenga sus tres repositorios. ¿Por qué el service nunca hace `new InMemoryProductoRepository()`?
4. ¿Qué hace `orElseThrow` con un `Optional` lleno y con uno vacío? ¿En qué termina, afuera, un producto que no existe?
5. Enumerá los pasos de `create` de productos. ¿Dónde está la Regla 2?
6. ¿Por qué hay que asociar el producto al comercio antes de guardarlo? Describí qué pasaría con el listado si quedara un producto guardado sin comercio.
7. En `getPrecio`, ¿quién calcula el precio final? ¿Qué muestra eso sobre reglas de negocio y reglas del flujo?
8. ¿Por qué existe `findComercioOwner`? ¿Por qué está bien en este ejemplo y no en un sistema real?
9. ¿Por qué `contains(producto)` funciona aunque `Producto` no defina `equals`? ¿Cuándo dejaría de funcionar?
10. En `VentaServiceImpl`, ¿por qué el `save` va antes de `agregarVenta`? ¿Por qué eso no contradice tu respuesta a la pregunta 6?

---

## Qué viene después

**M8 — La capa de presentación: controllers y el manejo global de errores.** Los services ya hacen todo el trabajo, pero nadie los puede llamar desde afuera. Aparecen los controllers (los endpoints HTTP), se resuelve dónde viaja el id del producto a modificar, cómo un DTO se vuelve JSON, y quién atrapa todas las excepciones de este módulo para convertirlas en 404 y 400.

**FIN DEL MÓDULO 7**
