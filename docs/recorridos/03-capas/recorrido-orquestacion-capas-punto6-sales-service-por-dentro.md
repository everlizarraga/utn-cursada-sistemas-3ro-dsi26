# 🧭 RECORRIDO — Orquestación de CU en Capas — PUNTO 6: El Servicio de Ventas por Dentro

**Unidad:** clase04 · **Densidad global:** 🔴 (el punto más largo — es el proyecto entero; tomalo en dos sentadas si hace falta)

---

## Sobre este documento

**Qué cubre:** el proyecto del servicio de ventas de SmartLife completo, capa por capa y con el código comentado: repositorios en memoria, el service de productos punta a punta, los DTO y los records, los controllers con sus endpoints, las excepciones y el manejador global de errores, la venta como segunda orquestación, los datos semilla, y la API probada en vivo (con Postman y con un test de integración).

**Qué NO cubre:** persistencia en una base de datos real, interfaces gráficas, sesiones (todo eso llega más adelante en la materia).

## De dónde venís

- **Puntos 4-5:** las clases típicas, el árbol de conocimiento, y Spring Boot construyendo e inyectando todo.
- **clase03:** el dominio del servicio de ventas ya modelado y testeado.
- **clase02:** HTTP (verbos, rutas, status codes), JSON, y qué es consumir/exponer una API REST.
- **preclase02:** streams y lambdas de Java, excepciones checked/unchecked, JUnit, Lombok.

---

## 1. El mapa del proyecto 🔴

El punto anterior cerró con la estructura de packages. Este punto la camina entera, en este orden (de abajo hacia arriba en las capas, que es el orden en que las piezas se necesitan):

```
   RECORRIDO DE ESTE PUNTO
   ─────────────────────────
   §2  models/entities/   (repaso exprés — ya lo tuyo)
   §3  repositories/      (la capa de datos, en memoria)
   §4  services/          (el caso de uso punta a punta)
   §5  dtos/              (la frontera de los datos)
   §6  controllers/       (los endpoints reales)
   §7  exceptions/ + advice  (los errores, bien resueltos)
   §8  la venta           (segunda orquestación, con dominio exigente)
   §9  config/            (los datos semilla)
   §10 la API viva        (Postman + test de integración)
```

## 2. El dominio: repaso exprés 🟡

En `models/entities/` vive, intacto, todo lo que modelaste en la clase 03 — el proyecto creció *alrededor* del dominio, no encima:

- `Comercio` — con sus productos, sus ventas, sus observadores; valida que toda venta sea de productos propios (si no: `IllegalArgumentException`) y notifica a los observadores al registrar una venta. Guardá ese detalle de la excepción: reaparece en la sección 8.
- `Producto` y `TipoProducto` — el producto delega en su tipo el cálculo de impuestos; `precioFinal() = precioBase + totalImpuestos()`.
- `Impuesto` (interfaz) con `IVA`, `EI`, `EO` — cada uno con su fórmula.
- `Venta` e `ItemVenta` — la venta acumula ítems y sabe totalizar (precio base, impuestos, final); el ítem valida cantidad positiva.
- Los observadores de venta.

Las clases usan las anotaciones de Lombok que ya conocés (`@Getter`, `@Setter`, `@AllArgsConstructor`…) para no escribir getters/setters a mano. **Nada de esto conoce a nada de lo que sigue** — el dominio es agnóstico, como manda el árbol.

## 3. La capa de datos: repositorios en memoria 🔴

Cada entidad persistible tiene su par interfaz + implementación. La interfaz, primero — el contrato que el resto del sistema conoce:

```java
public interface ProductoRepository {

    List<Producto> findAll();          // traeme todos
    Optional<Producto> findById(Long id);  // buscá uno por id (puede no estar)
    Producto save(Producto producto);  // guardalo (nuevo o existente)
    void delete(Producto producto);    // eliminalo
}
// ↑ Los nombres en inglés (findAll, findById, save, delete) son la
//   convención habitual para repositorios. Es el mismo contrato
//   guardar/buscar/eliminar del Punto 4, con otro idioma.
```

Un tipo nuevo pide su línea: **`Optional<Producto>`** es un envoltorio que puede contener un valor **o estar vacío** — la forma prolija de decir "buscá, y puede que no esté", sin devolver `null` pelado. Su gracia aparece en la sección 4: permite encadenar *"y si está vacío, lanzá tal excepción"*.

La implementación — que persiste **en memoria**, en simples colecciones, porque la base de datos real llega recién en la segunda parte del año:

```java
@Repository                     // ① bean de la capa de datos: lo construye Spring
public class InMemoryProductoRepository implements ProductoRepository {

    private final List<Producto> productos = new ArrayList<>();
    // ↑ ② El "medio persistente" de hoy: una lista. Mientras la app viva,
    //   los datos viven; se apaga la app, se van. Suficiente para diseñar.

    private final GeneradorIdSecuencial generadorId = new GeneradorIdSecuencial();
    // ↑ ③ Utilitario propio (vive en utils/): entrega 1, 2, 3... Como no hay
    //   base de datos que genere ids, los generamos nosotros.

    @Override
    public List<Producto> findAll() {
        return new ArrayList<>(productos);
        // ↑ ④ Devuelve una COPIA de la lista, no la lista interna. Detalle
        //   defensivo: que nadie de afuera pueda modificar mi almacenamiento.
    }

    @Override
    public Optional<Producto> findById(Long id) {
        return productos.stream().filter(p -> p.getId().equals(id)).findFirst();
        // ↑ ⑤ Stream de la clase 02 del seminario: filtrá por id, devolvé el
        //   primero. findFirst() ya devuelve un Optional — si no hay match,
        //   viene vacío. Encaja perfecto con la firma.
    }

    @Override
    public Producto save(Producto producto) {
        if (producto.getId() == null) {      // ⑥ ¿Viene SIN id? → es NUEVO:
            producto.setId(generadorId.siguiente());  //    le asigno id...
            productos.add(producto);                  //    ...y lo agrego.
            return producto;
        }
        delete(producto);                    // ⑦ ¿Viene CON id? → es una
        productos.add(producto);             //    actualización: saco la
        return producto;                     //    versión vieja, meto la nueva.
    }
    // ↑ Un solo `save` que resuelve alta Y modificación, decidiendo por el id.

    @Override
    public void delete(Producto producto) {
        if (producto.getId() == null) return;         // sin id no hay qué borrar
        productos.removeIf(p -> p.getId().equals(producto.getId()));
    }
}

// ¿CÓMO FUNCIONA?
// El service llama save(productoSinId) → el repo le pone id 1, lo guarda,
// lo devuelve. Llama findById(1) → Optional con el producto. findById(99)
// → Optional vacío (y el service decide qué hacer con eso). Si mañana este
// repo persiste en una base de datos, save se traduce en un insert/update
// y NADIE más en el sistema cambia una línea: todos conocen la interfaz.
```

Los otros tres repositorios (`Comercio`, `TipoProducto`, `Venta`) son **análogos** — mismo contrato, misma mecánica de lista + generador. Es la repetición "aceptable" de la que hablaba el Punto 4: repos simples y parecidos antes que abstracciones prematuras.

## 4. El service punta a punta: dar de alta un producto 🔴🔴

La interfaz `ProductoService` declara las operaciones que el caso de uso pide — y fijate cómo el enunciado se lee en las firmas: *dar de alta* → `create`, *modificar* → `update`, *eliminar* → `deleteById`, *listar todos* → `findAll`, *consultar precios* → `getPrecio`. Ni una operación de más: **si el caso de uso no lo amerita, no se programa** (nadie te paga código que nadie pidió).

```java
public interface ProductoService {

    List<ProductoResponse> findAll();
    ProductoResponse findById(Long id);
    ProductoResponse create(ProductoCreateRequest request);
    ProductoResponse update(ProductoUpdateRequest request);
    void deleteById(Long id);
    PrecioProductoResponse getPrecio(Long id);
}
// ↑ Mirá los TIPOS de entrada y salida: ProductoCreateRequest,
//   ProductoResponse... No entra ni sale la entidad Producto. Esos tipos
//   son los DTO — la sección 5 explica por qué existen. Por ahora leelos
//   como "paquetes de datos que cruzan la frontera".
```

Y la implementación del método estrella — el constructor con los tres repos inyectados ya lo viste en el Punto 5; ahora, `create`:

```java
@Override
public ProductoResponse create(ProductoCreateRequest request) {
    validateCreateRequest(request);
    // ↑ PASO 1 — validar la entrada (método privado, abajo): que la request
    //   no sea nula, que traiga comercioId y tipoProductoId, que la
    //   descripción no esté vacía, que el precio sea > 0. Reglas del FLUJO.

    Comercio comercio = getComercioOrThrow(request.comercioId());
    TipoProducto tipo = getTipoOrThrow(request.tipoProductoId());
    // ↑ PASO 2 — resolver referencias: de afuera llegan IDs (números);
    //   yo trabajo con OBJETOS. Voy a los repos y los recupero — y si
    //   alguno no existe, se corta todo acá (ver los OrThrow abajo).

    Producto producto = new Producto(null, tipo, request.precioBase(), request.descripcion().trim());
    // ↑ PASO 3 — instanciar dominio: EL SERVICE hace el new (la regla del
    //   Punto 4 en vivo). El id va null: lo genera el repo al guardar —
    //   el id nunca viene de afuera. trim(): saco espacios sobrantes.

    comercio.agregarProducto(producto);
    // ↑ PASO 4 — asociar: le hablo al objeto de dominio, que hace lo suyo.

    productoRepository.save(producto);
    // ↑ PASO 5 — persistir.

    return toResponse(producto, comercio);
    // ↑ PASO 6 — responder: transformo la entidad en un DTO de salida
    //   (sección 5) y eso es lo que viaja hacia el controller.
}
```

Las piezas privadas que el flujo usó — acá está el `Optional` cobrando sentido:

```java
private Comercio getComercioOrThrow(Long comercioId) {
    return comercioRepository.findById(comercioId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No se encontró comercio con id " + comercioId));
}
// ↑ Lectura corrida: "buscá el comercio; si el Optional viene vacío, lanzá
//   ResourceNotFoundException con este mensaje". Una línea que valida
//   existencia Y corta el flujo si falla. La excepción es propia (§7) y,
//   spoiler: se va a traducir en un 404. getTipoOrThrow y getProductoOrThrow
//   son idénticos con su repo y su mensaje.

private void validateCreateRequest(ProductoCreateRequest request) {
    if (request == null) throw new BusinessException("El body del producto es obligatorio");
    if (request.comercioId() == null) throw new BusinessException("El comercioId es obligatorio");
    if (request.tipoProductoId() == null) throw new BusinessException("El tipoProductoId es obligatorio");
    validateCommonFields(request.precioBase(), request.descripcion());
}

private void validateCommonFields(double precioBase, String descripcion) {
    if (descripcion == null || descripcion.isBlank())
        throw new BusinessException("La descripcion del producto es obligatoria");
    if (precioBase <= 0)
        throw new BusinessException("El precio base debe ser mayor a cero");
}
// ↑ Validaciones del flujo: datos mal formados → BusinessException (otra
//   excepción propia; futura respuesta 400). Fijate que el service NUNCA
//   maneja estas excepciones: las lanza y las patea hacia arriba.

// ¿CÓMO FUNCIONA? (create, camino feliz y caminos tristes)
// create({comercioId:1, tipoProductoId:2, precioBase:100, descripcion:"Smart TV 50"})
//   → valida ✓ → recupera comercio 1 y tipo 2 ✓ → new Producto (id null)
//   → comercio.agregarProducto → save (el repo le pone id) → devuelve el DTO.
// create({comercioId:99, ...}) → BOOM ResourceNotFoundException en el paso 2.
// create({precioBase:-5, ...}) → BOOM BusinessException en el paso 1.
// En ambos BOOM: nada se instanció de más, nada se guardó. El flujo corta temprano.
```

Un método más del service merece mirada, porque esconde una decisión de diseño finísima:

```java
private Comercio findComercioOwner(Producto producto) {
    return comercioRepository.findAll().stream()
        .filter(c -> c.getProductos().contains(producto))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(
            "No se encontró comercio para el producto " + producto.getId()));
}
// ↑ "¿De qué comercio es este producto?" — y la respuesta es... recorrer
//   TODOS los comercios preguntando quién lo tiene. ¿Por qué tan artesanal?
//   Porque Producto NO conoce a su Comercio: la relación va en un solo
//   sentido (Comercio → productos), evitando el doble acoplamiento.
//   Este método es el PRECIO de esa decisión: búsqueda lineal a cambio de
//   una relación limpia. Trade-off puro — y poder nombrarlo así es
//   exactamente lo que esta materia pide.
```

## 5. Los DTO: la frontera de los datos 🔴

Hora de resolver el misterio de los tipos `...Request` / `...Response`. **DTO** — anotalo — significa *Data Transfer Object*: objetos que existen **solo para transferir datos desde y hacia el exterior**. Todo lo que entra de afuera llega en un DTO; todo lo que sale, viaja en un DTO. La entidad de dominio no cruza la frontera jamás. ¿Por qué?

**Por la salida:** hay cosas que no querés devolver al mundo. El ejemplo definitivo: si devolvieras la entidad `Usuario` tal cual… viajaría **la contraseña**. Con un DTO de salida, vos elegís exactamente qué campos se exponen — y la contraseña no está invitada.

**Por la entrada:** hay cosas que el mundo no debe mandarte. Al crear un producto, ¿quién genera el id? **Vos** (el repo, en el save). Entonces el DTO de entrada del alta directamente **no tiene campo id** — lo que no está en el DTO, no puede venir de afuera. La forma del DTO es una validación estructural gratis.

Los DTO del producto, en el package `dtos/producto/`:

```java
public record ProductoCreateRequest(   // lo que ENTRA al crear:
    Long comercioId,                   //   referencias por id...
    Long tipoProductoId,
    double precioBase,                 //   ...y los datos propios.
    String descripcion                 //   SIN id: lo genera el sistema.
) { }

public record ProductoUpdateRequest(  // lo que ENTRA al modificar:
    Long id,                          //   acá SÍ viene el id (¿cuál modifico?)
    Long comercioId,
    Long tipoProductoId,
    double precioBase,
    String descripcion
) { }
// ↑ Crear y actualizar llevan DTOs DISTINTOS aunque se parezcan: no siempre
//   coinciden los datos de una y otra operación. Un DTO por interacción.

public record ProductoResponse(       // lo que SALE:
    Long id,                          //   ahora el id sí — el mundo lo necesita
    Long comercioId,                  //   para futuras operaciones.
    Long tipoProductoId,
    String tipoProductoDescripcion,   //   dato "aplanado" del tipo, cortesía
    double precioBase,                //   para el consumidor.
    String descripcion
) { }
```

¿Y esa palabra **`record`**? Es una forma de las versiones modernas de Java (posteriores a la 11) para definir clases **inmutables** en una línea: escribís solo los atributos (parece la firma de un constructor) y Java genera el constructor, los getters y todo lo demás. Un record ES un *value object* — el objeto de valor que el Punto 4 mencionó al pasar: transporta datos, no cambia una vez creado, no tiene comportamiento. Justo lo que un DTO quiere ser. (¿Los records te resultan raros? Un DTO también puede ser una clase común con getters — vale igual; lo importante es el rol, no la sintaxis. Convenciones de nombre estándar: entradas terminadas en `Request` o `Input`; salidas en `Response` u `Output`.)

Falta el traductor entidad → DTO, que viste llamado en el `create`:

```java
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
// ↑ Un "mapper": método que transforma la entidad en su DTO de salida,
//   campo por campo. Vive en el service (podría extraerse a una clase
//   ProductoMapper para un código aún más limpio — es válido también).

// Y en findAll() lo viste aplicado en masa:
//     productoRepository.findAll().stream().map(this::toResponse).toList();
// `this::toResponse` es una REFERENCIA A MÉTODO: equivale a la lambda
// `p -> toResponse(p)` — mismo efecto, sintaxis más corta. Solo se puede
// usar cuando el parámetro calza exacto; el IDE suele sugerirla solo.
```

🕳️ **Madriguera — Mapeo automático.** Existen dependencias que generan estos mappers por vos. A veces igual hay que mapear a mano (campos calculados); por ahora, a mano y sin magia extra. *Volvé al camino.*

> **Para el parcial, si te preguntan** *"¿por qué el service devuelve un DTO y no la entidad de dominio?"*:
> Porque la entidad no debe cruzar la frontera del sistema: con un DTO de salida selecciono exactamente qué datos expongo (nunca devolvería la contraseña de un usuario, por ejemplo) y con el DTO de entrada limito qué puede venir de afuera (el id de un alta no viene: lo genera el sistema). Además desacopla mi modelo interno del contrato externo: puedo cambiar la entidad sin romperle el formato a los consumidores, y viceversa. Los DTO son objetos de valor inmutables que solo transportan datos.

## 6. El controller: los endpoints reales 🔴

Subimos a la frontera. El controller del producto, completo:

```java
@RestController                              // ① bean de presentación + API REST
@RequestMapping("/sales-service/productos")  // ② la RUTA PADRE: todos los
public class ProductoController {            //    endpoints de esta clase cuelgan
                                             //    de esta ruta base.
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }
    // ↑ ③ Inyección por constructor otra vez — y el tipo es LA INTERFAZ.
    //   El controller no sabe qué implementación le tocó. Punto 5 en vivo.

    @GetMapping                                   // ④ GET /sales-service/productos
    public List<ProductoResponse> getAll() {
        return productoService.findAll();
        // ↑ Traduce y DELEGA. El controller no piensa: patea al service.
    }

    @GetMapping("/{id}")                          // ⑤ GET .../productos/7
    public ProductoResponse getById(@PathVariable Long id) {
        return productoService.findById(id);
    }
    // ↑ `{id}` en la ruta es una PARTE VARIABLE: /productos/1, /productos/150...
    //   @PathVariable matchea ese pedacito de la ruta con el parámetro `id`
    //   (matchean por nombre; si quisieras llamarlos distinto, se puede
    //   indicar explícitamente). Spring te lo entrega ya convertido a Long.

    @PostMapping                                  // ⑥ POST /sales-service/productos
    @ResponseStatus(HttpStatus.CREATED)           //    si sale bien → 201 Created
    public ProductoResponse create(@RequestBody ProductoCreateRequest request) {
        return productoService.create(request);
    }
    // ↑ @RequestBody: "el body JSON de la request, convertímelo en una
    //   instancia de ProductoCreateRequest". Los campos del JSON matchean
    //   por nombre con los del record. @ResponseStatus pisa el código de
    //   estado por defecto (que en los GET, sin decir nada, es 200 OK).

    @PutMapping                                   // ⑦ PUT /sales-service/productos
    public ProductoResponse update(@RequestBody ProductoUpdateRequest request) {
        // ⚠️ Acá el id del producto a modificar viaja en el BODY. Funciona —
        // pero el diseño REST canónico lo lleva en la RUTA: PUT /productos/{id},
        // porque el id identifica al RECURSO, no es un dato más. Tenelo como
        // la forma correcta a la hora de diseñar tus propios endpoints.
        return productoService.update(request);
    }

    @DeleteMapping("/{id}")                       // ⑧ DELETE .../productos/7
    @ResponseStatus(HttpStatus.NO_CONTENT)        //    éxito → 204 No Content:
    public void delete(@PathVariable Long id) {   //    salió bien y no hay nada
        productoService.deleteById(id);           //    que devolver (void).
    }

    @GetMapping("/{id}/precio")                   // ⑨ GET .../productos/7/precio
    public PrecioProductoResponse getPrecio(@PathVariable Long id) {
        return productoService.getPrecio(id);
        // ↑ El caso de uso "consultar precios": devuelve un DTO específico
        //   con precioBase, impuestos y precioFinal — solo lo del precio.
    }
}

// ¿CÓMO FUNCIONA? (la "magia" que no se ve)
// - Llega `GET /sales-service/productos/2` → Spring matchea ruta y verbo →
//   ejecuta getById(2L) → el service devuelve un ProductoResponse → Spring
//   SERIALIZA ese record a JSON automáticamente y lo mete en la response
//   con status 200. Vos nunca tocás JSON a mano: por ser @RestController,
//   la conversión objeto↔JSON (en ambos sentidos) la hace el framework.
// - ¿Y si dos rutas podrían matchear la misma petición? Cuidado con las
//   colisiones: alguna va a ganar por especificidad — mejor diseñar rutas
//   que no compitan.
```

El controller de la venta es mínimo — y esa mínima es una lección: el caso de uso solo pedía *registrar* ventas, así que hay **un** endpoint y ni uno más:

```java
@RestController
@RequestMapping("/sales-service/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) { this.ventaService = ventaService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaResponse create(@RequestBody VentaCreateRequest request) {
        return ventaService.create(request);
    }
}
```

## 7. Los errores, bien resueltos 🔴

Repasá el camino: el service **lanza** excepciones (`ResourceNotFoundException`, `BusinessException`) y no las atrapa nadie… ¿entonces? Si de verdad no las atrapara nadie, cada error terminaría en un **500** (error interno del servidor) — mintiéndole al consumidor, porque "no encontré el producto 99" no es un error del servidor: es un 404 del cliente. Veamos las piezas.

Primero, las excepciones propias (package `exceptions/`):

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
// BusinessException y ConflictException: idénticas, cambiando el nombre.
// ↑ Heredan de RuntimeException — excepciones NO CHEQUEADAS (preclase02):
//   no te obligan a try/catch ni a declararlas en las firmas. Por eso el
//   service puede lanzarlas y seguir limpio, sin try/catch por todos lados.
//   Tener excepciones PROPIAS (en vez de genéricas) es lo que permite el
//   paso siguiente: distinguir cada caso y responder el código correcto.
```

Y el que las atrapa a todas — una clase especial en `controllers/advice/`:

```java
@RestControllerAdvice        // ① "esta clase es un MANEJADOR GLOBAL de
public class GlobalExceptionHandler {  //  excepciones para todos los controllers"

    @ExceptionHandler(ResourceNotFoundException.class)   // ② toda excepción de
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage());
    }                                                    //    este tipo cae acá → 404

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "bad_request", ex.getMessage());
    }   // ↑ ③ datos inválidos o reglas de negocio violadas → 400. Fijate que
        //   también atrapa IllegalArgumentException — importa en la sección 8.

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "conflict", ex.getMessage());   // → 409
    }

    @ExceptionHandler(Exception.class)               // ④ la red de seguridad:
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error",
                     "Ocurrió un error interno");
    }   // ↑ cualquier cosa NO prevista → 500 con mensaje GENÉRICO a propósito:
        //   los detalles internos de un error inesperado no se le cuentan al
        //   mundo (podrían revelar cómo estás hecho por dentro).

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status)
            .body(new ErrorResponse(error, message, Instant.now()));
    }   // ↑ ⑤ arma la respuesta: el status elegido + un DTO de error uniforme.
        //   ResponseEntity = "una respuesta HTTP completa" (status + body).
}

// El DTO de error (dtos/error/): un record más —
//     public record ErrorResponse(String error, String message, Instant timestamp) { }
// TODOS los errores del sistema salen con esta misma forma. Consumir una
// API cuyos errores son uniformes es un placer; recordalo cuando diseñes.

// ¿CÓMO FUNCIONA?
// GET /productos/99 → controller → service → getProductoOrThrow → BOOM
// ResourceNotFoundException → nadie la atrapa en el camino → el framework
// la deriva al @RestControllerAdvice → handleNotFound → responde:
//   HTTP 404 { "error": "not_found",
//              "message": "No se encontró producto con id 99",
//              "timestamp": "..." }
// Sin try/catch en NINGÚN controller ni service. Registrar el manejador no
// requiere nada más: la anotación alcanza, el framework lo conecta solo.
```

Con esto quedó cerrado el hilo de los errores: **el service lanza, el manejador global traduce, el consumidor recibe el código y el mensaje correctos.** Y de paso entendés por qué el Punto 4 decía que los try/catch en controllers "se pueden evitar con algo más prolijo" — esto era lo más prolijo.

## 8. La venta: la segunda orquestación 🔴

El `VentaServiceImpl` repite la receta del producto — pero con un ingrediente nuevo: acá **el dominio tiene reglas propias que se hacen oír**. Mirá el flujo completo:

```java
@Service
public class VentaServiceImpl implements VentaService {

    private final ComercioRepository comercioRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    // (+ el constructor de inyección, como siempre)

    @Override
    public VentaResponse create(VentaCreateRequest request) {
        validateRequest(request);
        // ↑ Flujo, paso 1: body no nulo, comercioId presente, al menos un
        //   ítem, cada ítem con productoId y cantidad > 0. Todo BusinessException.

        Comercio comercio = comercioRepository.findById(request.comercioId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró comercio con id " + request.comercioId()));
        // ↑ Paso 2: existencia del comercio (si no → 404).

        Venta venta = new Venta(null);                    // ← el service instancia
        for (ItemVentaRequest itemRequest : request.items()) {
            Producto producto = productoRepository.findById(itemRequest.productoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No se encontró producto con id " + itemRequest.productoId()));
            // ↑ Paso 3: cada producto del pedido debe EXISTIR (404 si no).

            if (!comercio.getProductos().contains(producto)) {
                throw new BusinessException(
                    "Todos los productos de la venta deben pertenecer al comercio indicado");
            }
            // ↑ Paso 4: regla del flujo — chequeo temprano de pertenencia,
            //   para cortar ANTES de armar nada (con el ítem exacto que falla).

            venta.agregarItem(new ItemVenta(producto, itemRequest.cantidad()));
            // ↑ Paso 5: armar la venta ítem a ítem (más instanciación de dominio).
        }

        try {
            comercio.agregarVenta(venta);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ex.getMessage());
        }
        // ↑ Paso 6 — EL DETALLE PARA ENMARCAR. comercio.agregarVenta valida
        //   (regla de NEGOCIO, en la entidad, desde la clase 03) que la venta
        //   sea de productos propios — y si no, lanza IllegalArgumentException:
        //   el dominio habla SU idioma, genérico de Java, porque no conoce el
        //   mundo HTTP ni las excepciones del sistema (¡árbol!). El service la
        //   TRADUCE a BusinessException — el idioma del flujo. Cada capa habla
        //   su lengua; el orquestador interpreta. (¿Y por qué el handler global
        //   igual atrapa IllegalArgumentException? Red de seguridad por si
        //   alguna se escapa sin traducir: también termina en 400.)

        ventaRepository.save(venta);                      // paso 7: persistir

        return new VentaResponse(venta.getId(), comercio.getId(),
            venta.getFechaRegistro(), venta.totalPrecioBase(),
            venta.totalImpuestos(), venta.totalFinal());
        // ↑ Paso 8: responder — los totales los calcula LA VENTA (negocio,
        //   clase 03); el service solo los empaqueta en el DTO.
    }
}

// ¿CÓMO FUNCIONA?
// create({comercioId:1, items:[{productoId:1, cantidad:2}]})
//   → valida ✓ → comercio ✓ → producto 1 ✓, es del comercio ✓ → ItemVenta
//   → comercio.agregarVenta (valida negocio ✓, NOTIFICA OBSERVADORES — la
//     clase 03 sigue viva acá adentro) → save → DTO con totales.
// items:[] → 400 "La venta debe incluir al menos un item".
// productoId inexistente → 404. Producto de OTRO comercio → 400.
```

Releé el flujo con los ojos del Punto 4 y está todo: validaciones de flujo en el service, existencias contra repos, instanciación de dominio en el service, reglas de negocio en las entidades, persistencia al final, DTO de salida. **La teoría entera cabe en un método.**

## 9. Los datos semilla 🟡

Problema práctico: los repos son listas en memoria — cada vez que levantás la aplicación, **arrancan vacías**. No podrías crear un producto (¿de qué comercio? ¿de qué tipo?) sin cargar antes esos datos a mano. La solución vive en `config/`:

```java
@Configuration                    // ① clase de configuración: Spring la lee al arrancar
public class DataInitializer {

    public static final long TIPO_HOGAR_ID = 1L;          // ids fijos y con nombre,
    public static final long TIPO_ELECTRONICO_ID = 2L;    // para que las pruebas
    public static final long COMERCIO_DEFAULT_ID = 1L;    // sepan a qué apuntar

    @Bean   // ② "este método fabrica un bean": Spring lo invoca al arrancar
    public CommandLineRunner seedData(TipoProductoRepository tipoProductoRepository,
                                      ComercioRepository comercioRepository) {
        // ↑ ③ ¡inyección hasta en los parámetros del método! Spring le alcanza
        //   los repos que necesita. CommandLineRunner = "código a ejecutar ni
        //   bien la aplicación termina de levantar".
        return args -> {
            if (tipoProductoRepository.findById(TIPO_HOGAR_ID).isEmpty()) {
                TipoProducto hogar = new TipoProducto(TIPO_HOGAR_ID, "Hogar");
                hogar.agregarImpuestos(new IVA(), new EI());     // Hogar paga IVA + EI
                tipoProductoRepository.save(hogar);
            }
            if (tipoProductoRepository.findById(TIPO_ELECTRONICO_ID).isEmpty()) {
                TipoProducto electronico = new TipoProducto(TIPO_ELECTRONICO_ID, "Electrónico");
                electronico.agregarImpuestos(new IVA(), new EO()); // Electrónico: IVA + EO
                tipoProductoRepository.save(electronico);
            }
            if (comercioRepository.findById(COMERCIO_DEFAULT_ID).isEmpty()) {
                comercioRepository.save(new Comercio(COMERCIO_DEFAULT_ID));
            }
            // ↑ ④ los `if isEmpty()` hacen la carga IDEMPOTENTE: si el dato ya
            //   está, no lo duplica — correr la semilla dos veces no rompe nada.
        };
    }
}
// Esto es un SEEDER (sembrador): rellena el medio persistente con un set de
// datos de prueba al arrancar. En un sistema productivo real esto no va —
// acá es lo que hace usable a la persistencia en memoria. Y ojo al dato:
// la semilla también corre cuando ejecutás los tests.
```

## 10. La API viva: Postman y el test de integración 🔴

Hora de encender todo. La aplicación levanta en el puerto **8082** (está configurado en `application.yaml`, el archivo de configuración del proyecto — y al arrancar, la consola te confirma el puerto). Desde Postman (la herramienta para dispararle peticiones a una API que conocés de la clase 02 — el proyecto incluye una colección lista para importar y jugar):

**1 — Crear un producto.** `POST http://localhost:8082/sales-service/productos`, body JSON:

```json
{ "comercioId": 1, "tipoProductoId": 2, "precioBase": 100.0, "descripcion": "Smart TV 50" }
```

Comercio 1 y tipo 2 existen gracias a la semilla (tipo 2 = Electrónico). Respuesta: **201 Created** con el `ProductoResponse` — id generado, datos completos.

**2 — Listar.** `GET .../productos` → **200** con un array JSON de productos (el que acabás de crear, adentro).

**3 — Buscar por id.** `GET .../productos/1` → **200** con la Smart TV. ¿Y `GET .../productos/3`, que no existe? → **404** con el `ErrorResponse` uniforme: `{ "error": "not_found", "message": "No se encontró producto con id 3", "timestamp": "..." }`. Todo el circuito de la sección 7, verificado en vivo.

**4 — El precio.** `GET .../productos/1/precio` → el DTO específico. Y acá el dominio de la clase 03 rinde examen — verifiquemos la cuenta a mano para el producto Electrónico de base 100:

```
IVA = 21% de 100            = 21.00
EO  = 0.5 × 100 + 4 × 4     = 66.00
impuestos                   = 87.00
precioFinal = 100 + 87      = 187.00   ✓  ← exactamente lo que responde la API
```

**El mismo circuito, sin herramienta externa.** ¿Y si quisieras verificar la API automáticamente, cada vez, sin abrir Postman? El proyecto trae un **test de integración**: un test que, en vez de probar una clase aislada (como los unitarios de JUnit que venís haciendo), levanta la aplicación completa y le dispara peticiones de mentira-pero-en-serio:

```java
@SpringBootTest        // ① "para este test, levantá la aplicación entera"
class SalesServiceApiIntegrationTest {

    @Autowired                              // ② inyección en el test (variante
    private WebApplicationContext context;  //    por atributo): pedime el
    private MockMvc mockMvc;                //    contexto de la app levantada.

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // ↑ ③ MockMvc: un "Postman programático" — dispara requests HTTP
        //   simuladas contra los controllers reales, sin red de por medio.
    }

    @Test
    void altaProductoYConsultaPrecioFinal_ok() throws Exception {
        String requestBody = """
            { "comercioId": 1, "tipoProductoId": 2,
              "precioBase": 100.0, "descripcion": "Smart TV 50" }
            """;

        MvcResult altaProducto = mockMvc.perform(post("/sales-service/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))                  // ④ el POST del paso 1...
            .andExpect(status().isCreated())            //   ...esperá un 201...
            .andExpect(jsonPath("$.tipoProductoDescripcion").value("Electrónico"))
            .andReturn();                               //   ...y campos correctos.
        // ↑ jsonPath("$.campo") navega el JSON de la respuesta y afirma valores.

        long productoId = ((Number) JsonPath.read(
            altaProducto.getResponse().getContentAsString(), "$.id")).longValue();
        // ↑ ⑤ del response del alta saco el id generado...

        mockMvc.perform(get("/sales-service/productos/{id}/precio", productoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.impuestos").value(87.0))     // ⑥ ...y verifico
            .andExpect(jsonPath("$.precioFinal").value(187.0)); //   LA MISMA CUENTA
    }                                                           //   de recién. ✓
}
// El archivo trae dos tests más con la misma mecánica: una venta de dos ítems
// verificando los totales, y una venta sin ítems esperando el 400 con
// "bad_request" — el camino triste también se testea. Punta a punta:
// controller → service → dominio → repos → respuesta, todo verificado
// en un test que corre solo.
```

Con esto, el viaje quedó completo y **comprobado**: viste entrar una petición por el endpoint, atravesar cada capa del Punto 4, y volver como JSON con su código de estado — tanto a mano (Postman) como automatizado (el test).

---

## ✅ Checkpoint del Punto 6

*Respondé sin releer. Sin respuestas a propósito — llegan con el complemento.*

1. Seguí el viaje completo de `POST /sales-service/ventas` con un ítem válido: nombrá cada clase que interviene, en orden, y qué hace cada una — desde que llega el JSON hasta que sale el 201.
2. ¿Por qué `ProductoCreateRequest` no tiene campo `id` y `ProductoUpdateRequest` sí? ¿Qué te protege esa diferencia?
3. Mandás `POST /productos` con `precioBase: -10`. ¿Qué excepción se lanza, dónde, quién la atrapa y qué recibe exactamente el consumidor (código + forma del body)?
4. ¿Por qué `findComercioOwner` recorre todos los comercios en vez de preguntarle al producto por su comercio? Nombrá la decisión de diseño, qué compra y qué paga.
5. En el service de ventas hay un `try/catch` — el único del proyecto. ¿Qué traduce, de qué idioma a cuál, y por qué esa traducción es coherente con el árbol de conocimiento?
6. ¿Qué pasaría con los errores del sistema si borraras la clase `GlobalExceptionHandler`? ¿Qué recibiría el consumidor ante un producto inexistente y por qué eso está mal?
7. ¿Por qué el manejador del caso `Exception` genérica responde un mensaje opaco en vez del mensaje real de la excepción?
8. El repo en memoria devuelve `new ArrayList<>(productos)` en `findAll` en vez de `productos`. ¿Qué está protegiendo?
9. ¿Para qué existe el seeder y por qué sus cargas están envueltas en `if isEmpty()`? ¿Qué pasaría sin el seeder al hacer el primer `POST /productos` tras levantar la app?
10. ¿Qué verifica un test de integración que un test unitario del dominio no puede verificar? Da un ejemplo concreto de este proyecto.

---

## Qué viene en el Punto 7

Ya está todo sobre la mesa: teoría, reglas, código y comprobación. El último punto es la vista de helicóptero — el viaje de una request dibujado completo, las reglas-reflejo consolidadas para el parcial, qué significa esto para el trabajo práctico, y el aterrizaje final del recorrido.

---

**FIN DEL PUNTO 6**
