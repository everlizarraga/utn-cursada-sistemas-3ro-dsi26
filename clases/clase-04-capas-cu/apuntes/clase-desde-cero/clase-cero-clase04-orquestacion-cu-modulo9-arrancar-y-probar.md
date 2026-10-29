# 🧩 Clase desde cero — Clase 04 · Módulo 9: Arrancar y probar. Seeder, Postman y tests

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 9 de 9.** Fase B (código). Último módulo.

## Sobre este documento

**Qué cubre:** lo que falta para que el servicio de ventas funcione de punta a punta. El **seeder**, que carga los datos iniciales al arrancar; la prueba de la API con **Postman**, request por request; el **recorrido completo de un pedido** por todas las capas; y los **tests**: los del dominio y los de integración, que llaman a los endpoints. Al final, el proyecto queda igual al snapshot de referencia.
**Qué NO cubre:** nada nuevo de diseño. Es el módulo donde todo lo de M1 a M8 se ve funcionando.

## De dónde venís

- De **M8**: la API levanta, pero todo `POST` bien armado da **404 "No se encontró comercio con id 1"**: los repositorios arrancan vacíos y ningún caso de uso da de alta comercios ni tipos de producto.
- De **M5**: `InMemoryComercioRepository` no genera ids, y `TipoProducto` tiene el constructor `TipoProducto(Long id, String descripcion)`.
- De la **clase 02**: Postman, `@Configuration` + `@Bean`, y `@SpringBootTest` con `@Autowired` sobre un atributo.
- De la **clase 03**: `DominioSalesServiceTest` y los valores de los impuestos.

---

## 1. El seeder: los datos con los que se arranca 🔴

### 1.1. El problema

El servicio de ventas necesita que existan, antes de cualquier pedido, **un comercio** y **los dos tipos de producto** (Hogar y Electrónico, con sus impuestos). Ningún caso de uso los crea: se supone que ya están. Con una base de datos, estarían guardados ahí. Pero los repositorios son **en memoria**: cada vez que la aplicación arranca, empiezan vacíos.

Hace falta alguien que, **cada vez que el proyecto levanta**, cargue esos datos. Eso es un **seeder** (de *seed*, semilla): una clase que llena el medio persistente con un conjunto inicial de datos.

### 1.2. `DataInitializer`

Está en `config/`:

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.config;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.comercio.Comercio;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos.EI;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos.EO;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos.IVA;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.TipoProducto;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.ComercioRepository;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.TipoProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration: una clase de configuración de Spring (clase 02).
@Configuration
public class DataInitializer {

    // Los ids fijos de los datos iniciales: así se sabe de antemano que
    // Hogar es el 1, Electrónico el 2 y el comercio el 1.
    public static final long TIPO_HOGAR_ID = 1L;
    public static final long TIPO_ELECTRONICO_ID = 2L;
    public static final long COMERCIO_DEFAULT_ID = 1L;

    // @Bean que devuelve un CommandLineRunner: Spring lo ejecuta UNA VEZ, apenas
    // termina de arrancar. Los dos repositorios llegan como parámetros (inyectados).
    @Bean
    public CommandLineRunner seedData(TipoProductoRepository tipoProductoRepository, ComercioRepository comercioRepository) {
        return args -> {
            // Tipo Hogar: IVA + EI. Solo si no existe todavía.
            if (tipoProductoRepository.findById(TIPO_HOGAR_ID).isEmpty()) {
                TipoProducto hogar = new TipoProducto(TIPO_HOGAR_ID, "Hogar");
                hogar.agregarImpuestos(new IVA(), new EI());
                tipoProductoRepository.save(hogar);
            }
            // Tipo Electrónico: IVA + EO.
            if (tipoProductoRepository.findById(TIPO_ELECTRONICO_ID).isEmpty()) {
                TipoProducto electronico = new TipoProducto(TIPO_ELECTRONICO_ID, "Electrónico");
                electronico.agregarImpuestos(new IVA(), new EO());
                tipoProductoRepository.save(electronico);
            }
            // El comercio 1 (sin observadores).
            if (comercioRepository.findById(COMERCIO_DEFAULT_ID).isEmpty()) {
                comercioRepository.save(new Comercio(COMERCIO_DEFAULT_ID));
            }
        };
    }
}
```

Términos nuevos:
- **Contexto de Spring**: el contenedor donde viven todos los beans.
- **`CommandLineRunner`**: una interfaz de Spring con un único método, `run(String... args)`, que Spring ejecuta automáticamente después de arrancar la aplicación. Cualquier bean de este tipo se corre al inicio.
- **Parámetros de un método `@Bean`**: Spring los completa con beans del contexto, igual que los parámetros de un constructor. Por eso `seedData` recibe los repositorios sin que nadie se los pase.
- **`args -> { ... }`**: una lambda que implementa ese único método. `args` son los argumentos con los que se lanzó la aplicación (acá no se usan).

```
// ¿CÓMO FUNCIONA?
//   1. Spring crea todos los beans. Uno de ellos es el que devuelve seedData(...):
//      para llamarlo, le pasa los dos repositorios (ya creados). seedData todavía
//      no carga nada: solo devuelve la lambda.
//   2. Cuando el contexto terminó de arrancar, Spring ejecuta todos los
//      CommandLineRunner: ahí corre la lambda, una vez.
//   3. Cada bloque pregunta si el dato ya existe; si no, lo crea y lo guarda.
//   Resultado esperado al terminar de arrancar:
//     tipos:      [ 1 Hogar (IVA, EI), 2 Electrónico (IVA, EO) ]
//     comercios:  [ 1 (sin productos, sin ventas, sin observadores) ]
```

Fijate en cinco cosas:

- **Se cierra el hilo de M5:** el comercio 1 sale de acá. Y como el `id` viene fijo, `InMemoryComercioRepository.save` no necesita generador: siempre recibe comercios con id.
- **Los `if (...isEmpty())`** hacen que el seeder no duplique datos que ya existen al arrancar. En este proyecto nunca pasa: con persistencia en memoria, cada arranque empieza con los repositorios vacíos. Con una base de datos, en cambio, los datos sobreviven al reinicio, y sin esos `if` cada arranque los volvería a insertar.
- **El comercio del seed no tiene observadores.** Por eso, en este proyecto, registrar una venta no notifica a nadie (M7 §7.1).
- **¿Y la Regla 2?** El seeder instancia entidades sin ser un service porque no ejecuta ningún caso de uso: es código de arranque, que nadie invoca desde afuera. La regla habla de quién instancia el dominio cuando se ejecuta un caso de uso. Lo mismo vale para los tests de dominio, que hacen "de service a mano" (M4 §2.2).
- **Se cierra el hilo de M8:** el mismo `POST` de producto que daba 404 "No se encontró comercio con id 1" ahora da 201 (§3.2, paso 1).

⚠️ **Un seeder así no va en un sistema real en producción.** Existe acá porque la persistencia es en memoria y, sin él, la aplicación arrancaría siempre vacía. Con una base de datos se usa para cargar datos de prueba (por ejemplo, un ambiente de desarrollo con algunos registros para probar).

> **Para el parcial, si te preguntan:** *¿Qué es un seeder y por qué no va en producción?*
> Es una clase que, cada vez que arranca la aplicación, carga en el medio persistente un conjunto inicial de datos. Acá hace falta porque la persistencia es en memoria y, sin él, la aplicación arrancaría siempre vacía. En un sistema real los datos ya están en la base: un seeder solo tiene sentido para cargar datos de prueba en un ambiente de desarrollo.

---

## 2. Levantar la aplicación 🟡

Con el seeder, el proyecto está completo. Al levantarlo (desde el IDE, ejecutando `SalesServiceApplication`), la consola muestra en qué puerto quedó escuchando. Acá es el **8082**, porque así está en `application.yaml` (por defecto Spring Boot usaría el 8080).

Si el puerto está ocupado, se cambia `server.port` en `application.yaml`, y también la variable `baseUrl` de la colección.

Recordá también el generador de ids (M5): el primer producto que se dé de alta va a recibir el **id 1**, el segundo el 2, y así.

💡 Si cambiás código con la aplicación levantada, devtools la reinicia: los repositorios vuelven a empezar vacíos (con lo del seeder) y los ids de producto arrancan otra vez en 1.

---

## 3. Probar la API con Postman 🔴

### 3.1. La colección

El repositorio trae una **colección de Postman** lista para importar, en `sales-service/postman/sales-service.postman_collection.json`. En Postman: **File → Import**, y se elige ese archivo. Aparecen todos los requests del servicio, organizados en dos carpetas.

La colección define dos **variables** (en la colección, pestaña *Variables*), para no repetir valores en cada request:

| Variable | Valor | Para qué |
|---|---|---|
| `baseUrl` | `http://localhost:8082` | El comienzo de todas las URLs |
| `productoId` | `1` | El producto sobre el que operan los requests que necesitan uno |

Los siete requests:

| Carpeta | Request | Verbo y URL | Body |
|---|---|---|---|
| Productos | Crear producto | `POST {{baseUrl}}/sales-service/productos` | `{"comercioId": 1, "tipoProductoId": 2, "precioBase": 100.0, "descripcion": "Smart TV 50"}` |
| | Listar productos | `GET {{baseUrl}}/sales-service/productos` | — |
| | Obtener producto por id | `GET {{baseUrl}}/sales-service/productos/{{productoId}}` | — |
| | Actualizar producto | `PUT {{baseUrl}}/sales-service/productos/{{productoId}}` | `{"comercioId": 1, "tipoProductoId": 1, "precioBase": 120.0, "descripcion": "Smart TV 50 - Actualizado"}` |
| | Eliminar producto | `DELETE {{baseUrl}}/sales-service/productos/{{productoId}}` | — |
| | Consultar precio final | `GET {{baseUrl}}/sales-service/productos/{{productoId}}/precio` | — |
| Ventas | Registrar venta | `POST {{baseUrl}}/sales-service/ventas` | `{"comercioId": 1, "items": [{"productoId": {{productoId}}, "cantidad": 2}]}` |

### 3.2. Una secuencia para recorrer todo

Con la aplicación recién levantada, en este orden (no es el de la colección: eliminar va al final):

| # | Request | Respuesta esperada |
|---|---|---|
| 1 | **Crear producto** (Smart TV 50, tipo 2, 100) | **201**, `{"id": 1, "comercioId": 1, "tipoProductoId": 2, "tipoProductoDescripcion": "Electrónico", "precioBase": 100.0, "descripcion": "Smart TV 50"}` |
| 2 | **Listar productos** | **200**, un array con ese único producto |
| 3 | **Obtener producto** con `productoId` = 1 | **200**, el mismo producto |
| 4 | **Obtener producto** cambiando la URL a `/productos/3` (cualquier id que no exista) | **404**, `{"error": "not_found", "message": "No se encontró producto con id 3", "timestamp": "..."}` |
| 5 | **Consultar precio** del 1 | **200**, `{"productoId": 1, "precioBase": 100.0, "impuestos": 87.0, "precioFinal": 187.0}` |
| 6 | **Actualizar producto** 1 (pasa a tipo 1, Hogar, a 120) | **200**, `{"id": 1, "comercioId": 1, "tipoProductoId": 1, "tipoProductoDescripcion": "Hogar", "precioBase": 120.0, "descripcion": "Smart TV 50 - Actualizado"}` |
| 7 | **Consultar precio** del 1 | **200**, `{"productoId": 1, "precioBase": 120.0, "impuestos": 56.25, "precioFinal": 176.25}` |
| 8 | **Registrar venta** (producto 1, cantidad 2) | **201**, `{"id": 1, "comercioId": 1, "fechaRegistro": "<hoy>", "totalPrecioBase": 240.0, "totalImpuestos": 112.5, "totalFinal": 352.5}` |
| 9 | **Eliminar producto** 1 | **204**, sin cuerpo |
| 10 | **Listar productos** | **200**, `[]` |

De dónde salen los números (los cálculos son de la clase 03):

```
// Electrónico de 100:  IVA 0,21 × 100 = 21   ·  EO 0,5 × 100 + 4 × 4 = 66          →  impuestos 87,    final 187
// Hogar de 120:        IVA 0,21 × 120 = 25,2 ·  EI 120 / 4 + 0,3 × 3,5 = 31,05     →  impuestos 56,25, final 176,25
// Venta de 2 unidades del de 120:  base 240 · impuestos 112,5 · final 352,5
```

Si en el paso 1 creás más de un producto, los ids siguen 2, 3…; para operar sobre otro, cambiá la variable `productoId` de la colección.

> 💡 Mientras probás, mirá la consola del IDE. Si quisieras ver cada pedido que llega, podrías agregar una línea de log en el controller; acá no hay ninguna.

---

## 4. El recorrido de un pedido, de punta a punta 🔴

Este es el cierre de toda la clase: un pedido real atravesando todas las capas, con cada clase en su lugar. **Crear producto** (paso 1 de la secuencia):

```
 Postman
   POST /sales-service/productos
   { "comercioId": 1, "tipoProductoId": 2, "precioBase": 100.0, "descripcion": "Smart TV 50" }
   │
   ▼
 ┌─ PRESENTACIÓN ────────────────────────────────────────────────────────────────────────────┐
 │ Spring: POST + /sales-service/productos → ProductoController.create                       │
 │         body JSON → ProductoCreateRequest (Jackson)                                       │
 │ ProductoController.create(request)  ──► productoService.create(request)                   │
 └───────────────────────────────────────────────────────────────────────────────────────────┘
   ▼
 ┌─ DOMINIO ─────────────────────────────────────────────────────────────────────────────────┐
 │ intermedio: ProductoServiceImpl.create(request)                                           │
 │   1. validateCreateRequest(request)                        ✓ campos presentes, precio > 0 │
 │   2. getComercioOrThrow(1)   ──► comercioRepository.findById(1)      → comercio 1         │
 │   3. getTipoOrThrow(2)       ──► tipoProductoRepository.findById(2)  → Electrónico        │
 │   4. new Producto(null, Electrónico, 100.0, "Smart TV 50")                 (Regla 2)      │
 │   5. comercio.agregarProducto(producto)                                                   │
 │   6. productoRepository.save(producto)                               → id = 1             │
 │   7. toResponse(producto, comercio)                                  → ProductoResponse   │
 │ ┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄ │
 │ puro: TipoProducto (IVA + EO) · Producto · Comercio.agregarProducto                       │
 └───────────────────────────────────────────────────────────────────────────────────────────┘
   ▼  (los pasos 2, 3 y 6 bajan a…)
 ┌─ DATOS ───────────────────────────────────────────────────────────────────────────────────┐
 │ InMemoryComercioRepository · InMemoryTipoProductoRepository · InMemoryProductoRepository  │
 └───────────────────────────────────────────────────────────────────────────────────────────┘
   ▲  (y vuelve hacia arriba)
 ┌─ PRESENTACIÓN ────────────────────────────────────────────────────────────────────────────┐
 │ ProductoResponse → JSON (Jackson) · @ResponseStatus(CREATED) → 201                        │
 └───────────────────────────────────────────────────────────────────────────────────────────┘
   ▼
 Postman  ◄──  201 Created  { "id": 1, "comercioId": 1, "tipoProductoId": 2, ... }
```

Si en el paso 2 el comercio no existiera, `getComercioOrThrow` lanzaría `ResourceNotFoundException`, la excepción subiría sin que nadie la atrape en el service ni en el controller, y `GlobalExceptionHandler` la convertiría en un **404** con su `ErrorResponse` (M8).

Comparalo con el punto de partida de M1: alguien quería resolver "dar de alta un producto" con un método `comercio.darDeAltaProducto()`. Terminó siendo un controller, un service, tres repositorios, tres entidades, dos DTOs y un manejador de errores, cada uno con una responsabilidad.

> 🕳️ **Madriguera — El diagrama de secuencia**
> Este recorrido, dibujado con la notación UML formal (objetos arriba, líneas de vida, mensajes con flechas), es un **diagrama de secuencia**. Se ve en la clase 06.
> *Volvé al camino.*

**Registrar una venta** sigue el mismo camino con `VentaController` y `VentaServiceImpl`. La diferencia que importa acá está en el paso donde el service llama a `comercio.agregarVenta(venta)`: ahí el comercio recorre su lista de observadores para notificarlos, y en este proyecto esa lista está **vacía** (§1.2). Para ver la notificación funcionando, está el test de dominio (§5.1).

---

## 5. Tests 🟡

El proyecto tiene dos clases de test, en `sales-service/src/test/java/…`:

| Test | Qué prueba | Levanta Spring |
|---|---|---|
| `domain/DominioSalesServiceTest` | El dominio solo: impuestos, totales, notificación a observadores | No |
| `api/SalesServiceApiIntegrationTest` | La API entera: pedidos HTTP a los endpoints, de controller a repositorio | Sí |

Los asserts están escritos con **`assertEquals` de JUnit**, y así quedan. Para los asserts que se muestran, va también su equivalente con **AssertJ**, la biblioteca de aserciones que también entra con el kit de tests de Spring Boot: `assertThat(real).isEqualTo(esperado)`, que se lee como una oración. Las dos conviven sin problema.

### 5.1. Los tests del dominio

Son los de la clase 03, sobre las mismas entidades. Cuatro casos:

| Test | Qué verifica |
|---|---|
| `ventaUnProductoElectronico_verificaIvaMasEo` | Venta de un electrónico de 100: base 100, impuestos 87, final 187; y que **cada observador fue notificado una vez** |
| `ventaUnProductoHogar_verificaIvaMasEi` | Venta de un producto de hogar de 100: base 100, impuestos 47,05, final 147,05 |
| `ventaDosProductosElectronicos_impuestosPorProductoYTotal` | Impuestos por producto y totales de dos electrónicos |
| `ventaMixtaElectronicoYHogar_aplicaImpuestosCorrespondientes` | Una venta con un producto de cada tipo |

Fijate qué hace el primero por dentro: arma los objetos, los asocia (`comercio.agregarProducto`, `venta.agregarItem`), registra la venta en el comercio y verifica. **Es lo que M4 §2.2 llamó hacer de service a mano.** Ahora que existen los services, ese flujo tiene su clase, y se puede probar también por la API (§5.2). Los otros tres solo arman la venta y verifican sus totales.

En el `@BeforeEach`, el test fija los valores de los impuestos, crea el comercio y le registra dos observadores (`comercio.agregarObservadores(observadorSvibaa, observadorSffa)`). Las constantes valen 100 (`PRECIO_BASE_EJEMPLO`), 87 (`IMPUESTOS_ELECTRONICO_UNIT`, 21 + 66) y 187 (`FINAL_ELECTRONICO_UNIT`).

El primero, con sus asserts y el equivalente en AssertJ:

```java
@Test
void ventaUnProductoElectronico_verificaIvaMasEo() {
    Producto tv = new Producto(null, tipoElectronico, PRECIO_BASE_EJEMPLO, "Smart TV 50");

    Venta venta = new Venta(generadorIdSecuencial.siguiente());
    venta.agregarItem(new ItemVenta(tv, 1));

    // Asocia el producto al comercio y registra la venta: esto notifica a los dos observadores.
    comercio.agregarProducto(tv);
    comercio.agregarVenta(venta);

    assertEquals(PRECIO_BASE_EJEMPLO, venta.totalPrecioBase(), 1);
    assertEquals(IMPUESTOS_ELECTRONICO_UNIT, venta.totalImpuestos(), 1);
    assertEquals(FINAL_ELECTRONICO_UNIT, venta.totalFinal(), 1);
    assertEquals(1, observadorSffa.getCantLlamadas());
    assertEquals(1, observadorSvibaa.getCantLlamadas());
}
```

```java
// Equivalente AssertJ de los cinco asserts
// (con import static org.assertj.core.api.Assertions.assertThat; y ...api.Assertions.within;):
assertThat(venta.totalPrecioBase()).isCloseTo(PRECIO_BASE_EJEMPLO, within(1.0));
assertThat(venta.totalImpuestos()).isCloseTo(IMPUESTOS_ELECTRONICO_UNIT, within(1.0));
assertThat(venta.totalFinal()).isCloseTo(FINAL_ELECTRONICO_UNIT, within(1.0));
assertThat(observadorSffa.getCantLlamadas()).isEqualTo(1);
assertThat(observadorSvibaa.getCantLlamadas()).isEqualTo(1);
```

Las dos versiones dicen lo mismo. El tercer argumento de `assertEquals` (el `1`) y `within(1.0)` son la **tolerancia**: cuánto puede diferir el resultado de lo esperado (los cálculos con `double` pueden no dar exactos). En estos tests la tolerancia es grande; los de la API usan 0,01. Los otros tres tests usan la misma forma de assert, sin pasar por el comercio.

### 5.2. Los tests de integración de la API

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest: antes de los tests, levanta la aplicación ENTERA (todos los beans,
// y también el seeder). Es un test de integración: prueba las capas juntas.
@SpringBootTest
class SalesServiceApiIntegrationTest {

    // El "contexto" de Spring: el contenedor con todos los beans. Inyectado con
    // @Autowired sobre el atributo (la forma de inyectar que se usa en los tests).
    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    // Antes de cada test: arma un MockMvc sobre ese contexto. MockMvc permite
    // mandarle pedidos HTTP a los controllers SIN levantar un servidor de verdad.
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── Test 1: alta de un electrónico de 100 y consulta de su precio ──────────────
    @Test
    void altaProductoYConsultaPrecioFinal_ok() throws Exception {
        String requestBody = """
            {
              "comercioId": 1,
              "tipoProductoId": 2,
              "precioBase": 100.0,
              "descripcion": "Smart TV 50"
            }
            """;

        // POST con el body → espera 201 y verifica campos del JSON de respuesta.
        MvcResult altaProducto = mockMvc.perform(post("/sales-service/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.comercioId").value(1))
            .andExpect(jsonPath("$.tipoProductoId").value(2))
            .andExpect(jsonPath("$.tipoProductoDescripcion").value("Electrónico"))
            .andReturn();

        // Lee el id que asignó el sistema (no lo asume: ver §5.3).
        String productoBody = altaProducto.getResponse().getContentAsString();
        Number productoIdNumero = JsonPath.read(productoBody, "$.id");
        long productoId = productoIdNumero.longValue();

        // GET del precio de ese producto → 100 de base, 87 de impuestos, 187 final.
        mockMvc.perform(get("/sales-service/productos/{id}/precio", productoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productoId").value(productoId))
            .andExpect(jsonPath("$.precioBase").value(100.0))
            .andExpect(jsonPath("$.impuestos").value(87.0))
            .andExpect(jsonPath("$.precioFinal").value(187.0));
    }

    // ── Test 2: venta de dos productos de Hogar (200 × 1 y 100 × 2) ─────────────────
    @Test
    void altaVentaConDosItems_ok() throws Exception {
        long producto1 = crearProducto("Cafetera Express", 1, 200.0);
        long producto2 = crearProducto("Licuadora", 1, 100.0);

        String requestBody = """
            {
              "comercioId": 1,
              "items": [
                {"productoId": %d, "cantidad": 1},
                {"productoId": %d, "cantidad": 2}
              ]
            }
            """.formatted(producto1, producto2);

        MvcResult ventaResult = mockMvc.perform(post("/sales-service/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.comercioId").value(1))
            .andReturn();

        // Lee los totales del JSON y los compara con tolerancia de 0,01.
        String ventaBody = ventaResult.getResponse().getContentAsString();
        double totalPrecioBase = JsonPath.read(ventaBody, "$.totalPrecioBase");
        double totalImpuestos = JsonPath.read(ventaBody, "$.totalImpuestos");
        double totalFinal = JsonPath.read(ventaBody, "$.totalFinal");

        assertEquals(400.0, totalPrecioBase, 0.01);
        assertEquals(187.15, totalImpuestos, 0.01);
        assertEquals(587.15, totalFinal, 0.01);
    }

    // ── Test 3: una venta sin ítems tiene que dar 400 ──────────────────────────────
    @Test
    void altaVentaSinItems_badRequest() throws Exception {
        String requestBody = """
            {
              "comercioId": 1,
              "items": []
            }
            """;

        mockMvc.perform(post("/sales-service/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("bad_request"));
    }

    // ── Ayudante: da de alta un producto por la API y devuelve el id asignado ──────
    private long crearProducto(String descripcion, long tipoProductoId, double precioBase) throws Exception {
        String requestBody = """
            {
              "comercioId": 1,
              "tipoProductoId": %d,
              "precioBase": %s,
              "descripcion": "%s"
            }
            """.formatted(tipoProductoId, precioBase, descripcion);

        MvcResult result = mockMvc.perform(post("/sales-service/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andReturn();

        String productoBody = result.getResponse().getContentAsString();
        Number productoId = JsonPath.read(productoBody, "$.id");
        return productoId.longValue();
    }
}
```

Términos nuevos:
- **`MockMvc`**: una herramienta de Spring para mandarle pedidos HTTP a los controllers **sin levantar un servidor real**: el pedido entra por el mismo camino (rutas, conversión del JSON, controller, service, repositorios, manejador de errores), pero no viaja por la red. La ventaja: el test no necesita la aplicación corriendo ni un puerto libre, y es más rápido. Postman, en cambio, necesita la aplicación levantada.
- **`perform(post(...).contentType(...).content(...))`**: arma y manda el pedido. **`andExpect(...)`**: verifica algo de la respuesta (`status().isCreated()` → 201; `isOk()` → 200; `isBadRequest()` → 400).
- **`.andReturn()`**: termina la cadena y devuelve el `MvcResult`.
- **`jsonPath("$.id")`**: una forma de señalar un valor dentro del JSON de respuesta. `$` es la raíz; `$.id` es el campo `id`. `jsonPath(...).isNumber()` verifica que sea un número; `.value(x)`, que valga `x`. `JsonPath.read(json, "$.id")` lo saca para usarlo en el código (JsonPath es una biblioteca que viene en el kit de tests).
- **`get("/…/{id}/precio", productoId)`**: el `{id}` se reemplaza por el valor que viene después, como una variable de ruta.
- **`Number` y `.longValue()`**: `JsonPath.read` devuelve un `Integer` para números chicos, así que guardarlo directo en un `Long` fallaría. Se lee como `Number` y se convierte.
- **`MvcResult`**: el resultado completo de un pedido (para leer el cuerpo de la respuesta con `getResponse().getContentAsString()`).
- **`WebApplicationContext`**: el contenedor de Spring con todos los beans de la aplicación web.
- **`"""..."""`**: un *text block* de Java: un string de varias líneas, escrito tal como se ve. Ideal para un JSON.
- **`.formatted(a, b)`**: reemplaza en orden los `%d` (números enteros) y los `%s` (cualquier valor, convertido a texto: el `double` 200.0 queda `200.0`). No se usa `%f` porque depende del idioma de la máquina: con configuración en español escribe `200,000000`, que no es un JSON válido.

```
// ¿CÓMO FUNCIONA? el test 2, por dentro
//   crearProducto("Cafetera Express", 1, 200.0)   → POST → 201 → id del producto (Hogar, 200)
//   crearProducto("Licuadora", 1, 100.0)          → POST → 201 → id del producto (Hogar, 100)
//   POST /ventas con 1 cafetera y 2 licuadoras    → 201
//   Totales:
//     base       200 × 1 + 100 × 2                                   = 400
//     impuestos  (42 + 51,05) × 1 + (21 + 26,05) × 2 = 93,05 + 94,10 = 187,15
//     final      400 + 187,15                                        = 587,15
//   Con double, Java da 187,14999999999998 y 587,1500000000001: por eso la tolerancia de 0,01.
```

Los tres `assertEquals` del test 2, con su equivalente AssertJ:

```java
// JUnit (como está)                              // AssertJ (equivalente)
assertEquals(400.0, totalPrecioBase, 0.01);       assertThat(totalPrecioBase).isCloseTo(400.0, within(0.01));
assertEquals(187.15, totalImpuestos, 0.01);       assertThat(totalImpuestos).isCloseTo(187.15, within(0.01));
assertEquals(587.15, totalFinal, 0.01);           assertThat(totalFinal).isCloseTo(587.15, within(0.01));
```

(Con `import static org.assertj.core.api.Assertions.assertThat;` y `import static org.assertj.core.api.Assertions.within;`.) Los `andExpect(jsonPath(...))` no cambian: son de MockMvc, no de JUnit ni de AssertJ.

### 5.3. Un detalle que explica dos cosas

Los tres tests de `SalesServiceApiIntegrationTest` **comparten el mismo contexto de Spring**: Spring Test lo guarda y lo reutiliza durante toda la corrida, en vez de levantarlo una vez por test. Consecuencias:

- **El seeder corre una sola vez**, al levantar. El comercio 1 y los tipos 1 y 2 están para los tres tests.
- **Los repositorios en memoria acumulan** lo que van creando los tests. El test 1 crea un producto; el test 2 crea dos más. JUnit no garantiza que los tests corran en el orden en que están escritos: en este proyecto, el producto del test 1 puede tener el id 1 o el 3. **Por eso los tests leen el id de la respuesta** (`JsonPath.read(..., "$.id")`) en lugar de asumir que es 1.

> **Para el parcial, si te preguntan:** *¿Qué diferencia hay entre un test de dominio y uno de integración?*
> El de dominio prueba las entidades solas, sin framework: arma los objetos y verifica su comportamiento. El de integración levanta la aplicación (`@SpringBootTest`) y prueba las capas juntas, mandando pedidos HTTP a los endpoints con `MockMvc`, desde el controller hasta los repositorios.

### 5.4. Alcance

En la Entrega 1 del TP estas capas todavía no se construyen (solo hay un controller con el GET de saludo; ver el roadmap, §7), así que tampoco se testean. Los tests de esa entrega son de dominio, más el importador CSV y las notificaciones simuladas. Los de integración de este módulo sirven de ejemplo de cómo se prueba la API completa.

---

## 6. Estado del proyecto: completo 🟡

```
sales_service/
├── SalesServiceApplication.java
├── config/DataInitializer                          seeder                       (este módulo)
├── controllers/                                    PRESENTACIÓN                 (M8)
│   ├── ProductoController · VentaController
│   └── advice/GlobalExceptionHandler
├── services/ ProductoService · VentaService        DOMINIO INTERMEDIO           (M7)
│   └── impl/ ProductoServiceImpl · VentaServiceImpl
├── models/entities/                                DOMINIO PURO                 (clase 03)
├── repositories/ 4 interfaces                      DATOS                        (M5)
│   └── inmemory/ 4 implementaciones
├── dtos/ 8 records · exceptions/ 3 excepciones     apoyo                        (M6)
└── utils/GeneradorIdSecuencial                     apoyo                        (M5)

test/
├── domain/DominioSalesServiceTest
└── api/SalesServiceApiIntegrationTest
```

Es exactamente el código del snapshot de referencia del proyecto.

---

## 7. Lo que te llevás de este módulo (y de la clase)

- **Seeder:** `@Configuration` + `@Bean` que devuelve un `CommandLineRunner`; Spring lo ejecuta una vez al arrancar. Carga el comercio 1 y los tipos 1 (Hogar) y 2 (Electrónico). Existe porque la persistencia es en memoria; no va así en producción.
- **Postman:** la colección del repo, con variables `baseUrl` y `productoId`; la secuencia crear → listar → obtener → 404 → precio → actualizar → precio → venta → eliminar recorre toda la API.
- **El recorrido de un pedido** atraviesa presentación (controller, conversión JSON), dominio intermedio (el service: validar, buscar, instanciar, asociar, guardar), dominio puro (las entidades que instancia y asocia), datos (repositorios) y vuelve. Los errores suben al manejador global.
- **Tests:** los de dominio prueban las entidades solas (y hacen "de service a mano"); los de integración prueban la API entera con `MockMvc`, sin servidor real. Comparten contexto: por eso leen el id de la respuesta. `assertEquals` y su equivalente AssertJ dicen lo mismo.
- **La clase entera, en una línea:** un caso de uso no es un método; es un controller que recibe, un service que orquesta, entidades que tienen las reglas y repositorios que guardan, cada uno conociendo solo lo que le corresponde.

---

## ✅ Checkpoint del Módulo 9

Respondé de memoria. Las respuestas van al complemento.

1. ¿Por qué hace falta el seeder en este proyecto? ¿Por qué no iría así en un sistema real en producción?
2. ¿Cómo hace Spring para ejecutar `seedData` al arrancar? ¿Qué es un `CommandLineRunner`?
3. ¿Para qué sirven los `if (...isEmpty())` del seeder?
4. Con la aplicación recién levantada, creás un electrónico de 100 y consultás su precio. ¿Qué devuelve? Lo pasás a Hogar a 120 y volvés a consultar. ¿Y ahora?
5. Describí el recorrido de un `POST /sales-service/productos` por todas las capas, nombrando cada clase que interviene.
6. ¿Por qué en este proyecto registrar una venta no notifica a ningún observador? ¿Dónde se puede ver la notificación funcionando?
7. ¿Qué diferencia hay entre `DominioSalesServiceTest` y `SalesServiceApiIntegrationTest`? ¿Cuál levanta Spring?
8. ¿Qué es `MockMvc` y qué ventaja tiene sobre levantar el servidor y llamarlo de verdad?
9. ¿Por qué el test de integración lee el id del producto de la respuesta en lugar de asumir que es 1?
10. Escribí el equivalente AssertJ de `assertEquals(187.15, totalImpuestos, 0.01)`. ¿Por qué hace falta la tolerancia?

---

## Qué viene después

Con este módulo termina la clase desde cero. Antes de seguir, practicá con el proyecto: levantalo, importá la colección y hacé los requests; cambiá rutas, agregá comportamiento en un service, tocá un repositorio y fijate qué se rompe y dónde. Es la forma de que las piezas terminen de encajar. Para saber qué de todo esto se pide en la primera entrega del TP, mirá el recuadro de alcance del roadmap (§7).

Lo que sigue es el **complemento** de la unidad (las respuestas de los nueve checkpoints y las dudas que hayan aparecido) y, si te sirve, un **resumen** para la repetición espaciada.

**FIN DEL MÓDULO 9 — FIN DE LA CLASE DESDE CERO DE LA CLASE 04**
