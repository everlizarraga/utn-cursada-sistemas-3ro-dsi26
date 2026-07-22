# 🧭 RECORRIDO — Orquestación de CU en Capas — PUNTO 5: El Puente a Spring Boot

**Unidad:** clase04 · **Densidad global:** 🔴

---

## Sobre este documento

**Qué cubre:** la respuesta a la pregunta pendiente del Punto 4 (¿quién instancia a los services y repositorios?); qué aporta un framework en este diseño; las anotaciones `@Service`, `@Repository` y `@RestController`; la inyección de dependencias por constructor funcionando; el patrón interfaz + implementación; y cómo la estructura de packages del proyecto materializa el modelo en capas.

**Qué NO cubre:** el recorrido completo del código del proyecto, endpoint por endpoint (Punto 6).

## De dónde venís

- **Punto 4:** los cuatro tipos de clase, el árbol de conocimiento, y el hilo abierto: los services instancian al dominio… ¿y a ellos quién?
- **preclase04:** inyección de dependencias, y la diferencia biblioteca vs framework — con la idea de **inversión de control**: a una biblioteca la llamás vos; un framework te llama a vos (vos escribís piezas, él decide cuándo y cómo usarlas).
- **preclase02:** Maven como herramienta de construcción del proyecto, y Lombok (las anotaciones que generan getters/setters por vos).

---

## 1. La pregunta pendiente 🔴

Repasemos la cadena de "quién construye a quién" que dejó el Punto 4:

```
  ¿¿¿ ??? ───instancia──►  CONTROLLERS
  ¿¿¿ ??? ───instancia──►  SERVICES    ───instancian──►  ENTIDADES ✓
  ¿¿¿ ??? ───instancia──►  REPOSITORIOS
```

Las entidades de dominio tienen dueño claro: las instancian los services. Pero ¿los services? ¿Los repositorios? ¿Los controllers? Nadie en nuestro diseño hace `new ProductoService()` — y sin embargo, en el boceto del `CalificacionesService`, los repositorios aparecían *usados* como si alguien ya los hubiera construido y alcanzado.

Ese "alguien" existe, y ya lo conocés de la lectura previa: **el framework**.

## 2. El framework toma el control 🔴

Acá la teoría de la preclase se vuelve carne. La inversión de control decía: *con un framework, vos no escribís el programa principal que llama a todo — escribís las piezas, y el framework las descubre, las construye y las conecta.*

El framework de esta materia es **Spring Boot** (sobre Java): el entorno con el que vas a implementar el trabajo práctico todo el año. Aplicado a nuestras capas, el reparto queda así:

> **Vos escribís las clases (services, repositorios, controllers) y declarás qué necesita cada una. Spring Boot las instancia a todas, les inyecta lo que necesitan, y las deja listas y conectadas antes de que llegue la primera petición.**

Los objetos que Spring Boot gestiona de esta manera tienen un nombre: **beans**. Un *bean* es simplemente un componente cuyo ciclo de vida (creación, conexión, destrucción) está en manos del framework en vez de en las tuyas. Tus services, repos y controllers van a ser beans — solo que "estereotipados": beans con un rol declarado.

¿Y cómo se le declara ese rol? Con anotaciones.

## 3. Las anotaciones: declarar en vez de construir 🔴

Una **anotación** en Java es una marca que se escribe arriba de una clase (o método, o atributo), con el formato `@Nombre`: no ejecuta nada por sí misma — es un **metadato**, información *sobre* el código que otro (acá, el framework) lee y usa para decidir qué hacer. Ya usaste anotaciones sin llamarlas así: `@Test` de JUnit y las de Lombok.

Spring Boot define una por cada tipo de clase del Punto 4:

| Anotación | Se pone sobre… | Le dice a Spring Boot… |
|---|---|---|
| `@Service` | una clase service | "esta clase es un service: **instanciala vos** y gestionala" |
| `@Repository` | una clase repositorio | ídem, para la capa de datos |
| `@RestController` | una clase controller | ídem — y además "va a exponer endpoints de una API REST" |

El detalle crucial: **sin la anotación, Spring Boot no se entera de que la clase existe.** No hay magia adivinadora — el framework recorre tu proyecto buscando clases anotadas, y esas (solo esas) las convierte en beans. La anotación es tu mitad del contrato: *"esta pieza es tuya, construila"*.

De acá sale la regla espejo de la del Punto 4 — **anotala junto a aquella**:

> **Nosotros nunca instanciamos los services, repositorios ni controllers con nuestras propias manos. El framework los instancia a todos.** (El dominio sí lo instanciamos nosotros — en los services, como quedó dicho.)

## 4. La inyección por constructor 🔴

"Instanciarlos" es la mitad del trabajo. La otra mitad: un service *necesita* repositorios para funcionar. ¿Cómo le llegan? Mirá el arranque real del service de productos del proyecto (el resto de la clase se recorre en el Punto 6):

```java
@Service                                        // ① "Spring: esta clase es un
public class ProductoServiceImpl implements ProductoService {   //  service, construila vos"

    private final ProductoRepository productoRepository;        // ② Lo que este service
    private final ComercioRepository comercioRepository;        //    NECESITA para vivir:
    private final TipoProductoRepository tipoProductoRepository;//    tres repositorios.
    // ↑ `final`: la referencia es inmutable — se asigna una vez (en el
    //   constructor) y no cambia durante toda la ejecución.
    // ↑ Fijate los TIPOS: son las INTERFACES de los repos, no clases concretas.
    //   El service ni sabe qué implementación le va a tocar. (Sección 5.)

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               ComercioRepository comercioRepository,
                               TipoProductoRepository tipoProductoRepository) {
        this.productoRepository = productoRepository;
        this.comercioRepository = comercioRepository;
        this.tipoProductoRepository = tipoProductoRepository;
    }
    // ↑ ③ El constructor DECLARA las dependencias: "para construirme,
    //   alcanzame estas tres cosas". ¿Y quién lo llama, y de dónde saca
    //   los tres parámetros? EL FRAMEWORK. Spring ya construyó los
    //   repositorios (estaban anotados con @Repository), y cuando le toca
    //   construir este service, ve el constructor, entiende qué pide,
    //   y le INYECTA las instancias que ya tiene.

    // ... (los métodos del service — Punto 6)
}

// ¿CÓMO FUNCIONA? (el arranque de la aplicación, a vuelo de pájaro)
// 1. Spring Boot arranca y escanea el proyecto buscando clases anotadas.
// 2. Encuentra los @Repository → construye los repositorios. Beans listos.
// 3. Encuentra este @Service → ve que su constructor pide tres repositorios
//    → le pasa los beans que construyó en el paso 2 → bean service listo.
// 4. Encuentra los @RestController → les inyecta los services → listos.
// 5. Recién ahí el sistema empieza a atender peticiones, con todo el
//    árbol de objetos armado y conectado. Vos no escribiste UN solo `new`
//    de toda esta cadena.
```

Esto — el framework alcanzándole a cada clase lo que su constructor declara — **es la inyección de dependencias de la lectura previa, ejecutándose**. Vos no vas a buscar tus dependencias ni las construís: las declarás, y te las inyectan. Por eso la lectura era prerrequisito de esta unidad: sin ese concepto, este código parece brujería; con él, es exactamente lo que esperabas ver.

*(Esta es la forma de inyección que vas a usar — por constructor. Existen otras variantes; irán apareciendo cuando hagan falta.)*

## 5. Interfaz + implementación: el par que se repite en todas las capas 🔴

Un detalle del código de arriba merece su propia sección: la clase se llama `ProductoService**Impl**` e *implementa* una interfaz `ProductoService`. Ese par — **interfaz pública + clase concreta escondida** — atraviesa todo el proyecto: cada service y cada repositorio lo tienen.

¿Por qué duplicar, si "la interfaz sola no funciona" (no tiene comportamiento)? Porque acá se cumple, en código, la promesa del Punto 1: **entre capas se conocen solo contratos.**

- El controller declara `private final ProductoService productoService` — **la interfaz**. No conoce (ni le importa) qué clase concreta hay detrás.
- Spring, en ejecución, le inyecta la implementación concreta que encontró anotada.
- Resultado: el acoplamiento entre capas queda reducido a las firmas.

¿Y qué compra eso, en concreto? El escenario de reemplazo: mañana necesitás **otra** implementación del service, con reglas distintas. Creás otra clase que implemente la misma interfaz, hacés que sea esa la que el framework inyecte… **y el controller no se entera: no se rompe nada, el código sigue funcionando.** La ventaja 3 del Punto 1 ("la implementación puede reemplazarse siendo transparente") dejó de ser teoría.

Una pregunta legítima que quizás te hiciste: si una interfaz puede tener varias implementaciones, ¿cómo sabe Spring **cuál** inyectar? Regla simple para esta etapa: tené **una sola** implementación anotada con `@Service` por interfaz — si hay una sola candidata, no hay ambigüedad.

🕳️ **Madriguera — Profiles.** Existe una técnica (perfiles) para tener varias implementaciones y que Spring inyecte una u otra según el ambiente de ejecución (desarrollo, producción…). Es configuración más avanzada; no la necesitás hoy. *Volvé al camino.*

## 6. Los packages son las capas 🟡

Cerrá el punto mirando el proyecto desde arriba. La estructura de packages del servicio de ventas:

```
sales_service/
├── controllers/        ← capa de PRESENTACIÓN (los @RestController)
├── services/           ← capa de DOMINIO intermedia (interfaces @Service)
│   └── impl/           ←   ...y sus implementaciones concretas
├── models/entities/    ← capa de DOMINIO pura (todo lo de la clase 03)
│   ├── comercio/ · productos/ · venta/ · impuestos/ · observers/
├── repositories/       ← capa de DATOS (interfaces)
│   └── inmemory/       ←   ...implementaciones @Repository (en memoria, por ahora)
├── dtos/               ← la frontera de datos (Punto 6)
├── exceptions/         ← las excepciones propias (Punto 6)
├── config/ · utils/    ← configuración y utilitarios (Punto 6)
└── SalesServiceApplication.java
```

La organización de carpetas **ES** el modelo en capas hecho visible: podés señalar con el dedo dónde vive cada tipo de clase del Punto 4. Y el último archivo es la puerta de entrada de todo:

```java
@SpringBootApplication          // "acá arranca una aplicación Spring Boot:
public class SalesServiceApplication {  //  escaneá desde este package hacia
                                        //  abajo buscando beans"
    public static void main(String[] args) {
        SpringApplication.run(SalesServiceApplication.class, args);
        // ↑ El único main del proyecto: le cede el control al framework.
        //   Inversión de control en una línea — de acá en adelante,
        //   manda Spring.
    }
}
```

> **Para el parcial, si te preguntan** *"¿quién instancia a los services y repositorios, y cómo obtiene un service sus dependencias?"*:
> Los instancia el framework (Spring Boot): las clases se marcan con `@Service` / `@Repository` para que el framework las gestione como beans, y cada clase declara en su constructor qué dependencias necesita; el framework se las **inyecta** al construirla (inyección de dependencias por constructor). Nosotros nunca hacemos `new` de services ni repositorios — solo de las entidades de dominio, dentro de los services. Se declaran dependencias por **interfaz**, no por clase concreta, para minimizar el acoplamiento entre capas y poder reemplazar implementaciones de forma transparente.

---

## ✅ Checkpoint del Punto 5

*Respondé sin releer. Sin respuestas a propósito — llegan con el complemento.*

1. Le sacás la anotación `@Service` a `ProductoServiceImpl` y levantás la aplicación. ¿Qué esperás que pase y por qué? ¿En qué momento explota: al compilar o al arrancar?
2. Explicá con tus palabras la cadena completa de construcción al arrancar la app: ¿en qué orden se vuelven beans los repos, services y controllers, y por qué ese orden no puede ser otro?
3. ¿Por qué el atributo del controller es de tipo `ProductoService` (interfaz) y no `ProductoServiceImpl` (clase)? ¿Qué escenario futuro te habilita, y qué atributo de calidad estás comprando?
4. Un compañero escribe `new ProductoServiceImpl(new InMemoryProductoRepository(), ...)` adentro del controller "para no depender de magia". Nombrá al menos dos cosas que acaba de romper del diseño de la unidad.
5. ¿Qué diferencia hay entre "el service instancia a `Producto`" y "el framework instancia al service"? ¿Por qué el dominio NO es un bean?
6. ¿Qué es un bean, en una oración tuya?
7. Relacioná la anotación `@Service` con la inversión de control de la lectura previa: ¿qué mitad del contrato pone cada parte (vos / el framework)?
8. Mirando la estructura de packages: ¿dónde ubicarías físicamente una clase nueva `VentasService`? ¿Y su implementación? ¿Y la entidad `Venta`? ¿Y su repositorio en memoria?

---

## Qué viene en el Punto 6

Ya sabés quién construye qué y cómo se conecta todo. Falta lo mejor: **caminar el proyecto real completo** — el servicio de ventas de SmartLife con su API viva. El caso de uso "dar de alta un producto" punta a punta, los DTO que custodian la frontera, el manejo de errores que convierte excepciones en respuestas HTTP decentes, la venta como segunda orquestación, y la API probada en vivo. Todo el código, comentado línea por línea.

---

**FIN DEL PUNTO 5**
