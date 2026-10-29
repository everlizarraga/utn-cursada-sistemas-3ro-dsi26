# 🧩 Clase desde cero — Clase 04 · Módulo 5: El proyecto y la capa de datos

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 5 de 9.** Fase B (código). Primer módulo con código.

## Sobre este documento

**Qué cubre:** abrimos el proyecto del servicio de ventas de SmartLife y lo leemos con el mapa de M4 en la mano: qué versiones usa, cómo está armado, y cómo **cada paquete materializa una capa**. Después aparece por primera vez el mecanismo central de Spring Boot (los **beans**) y escribimos la capa de abajo entera: los **cuatro repositorios**, guardando en memoria.
**Qué NO cubre:** los DTOs y las excepciones (M6), los services (M7), los controllers (M8), ni cómo se cargan los datos iniciales (M9). Al final de este módulo el proyecto **levanta, pero no hace nada visible**: no hay ningún endpoint todavía.

## De dónde venís

- De **M3**: qué es un repositorio, su contrato típico, la **Regla 1** (el repositorio conoce a la entidad; la entidad no conoce al repositorio), que el contrato es una **interfaz** y que el servicio de ventas tiene **cuatro** repositorios.
- De **M4**: el mapa completo de clases por capa y la pregunta que quedó colgando: *¿quién crea a los repositorios?* (cómo llegan al service, en M7).
- De la **clase 02**: cómo se crea un proyecto Spring Boot, qué es el `pom.xml` y Maven, `application.yaml`, la clase con `@SpringBootApplication`, Lombok (`@Getter`, `@Setter`…), y `@Configuration` + `@Bean`.
- De la **preclase 04**: inyección de dependencias, y la diferencia entre biblioteca (vos la llamás) y framework (te llama a vos).
- De la **clase 03**: el dominio completo, que acá se reusa, con un ajuste en los ids (§2.4).

---

## 1. Ficha de versiones de este proyecto 🟡

| Tecnología | Versión en este proyecto |
|---|---|
| Java (JDK) | 21 |
| Spring Boot | 4.0.5 |
| Spring Cloud BOM | 2025.1.1 (lista de versiones de Spring Cloud, herramientas para sistemas de varios servicios; declarada en el padre, sin uso todavía en este servicio) |
| Lombok | 1.18.42 |
| Maven | 3.9 o superior |
| Starter web | `spring-boot-starter-webmvc` (así se llama en Spring Boot 4) |
| Puerto | 8082 |

> ⚠️ **Estas son las versiones de este ejemplo.** El template del TP de tu equipo puede usar otra versión de Java o de Spring Boot, u organizar el proyecto distinto: los ejemplos de la materia y el TP no siempre coinciden, y las versiones cambian a lo largo del año. **Los conceptos de esta clase valen igual en cualquiera de ellas**: capas, services, repositorios, controllers, DTOs, inyección de dependencias, manejo de errores. Lo que puede cambiar es algún detalle de configuración.

---

## 2. Cómo está armado 🟡

### 2.1. Un proyecto, dos módulos

El repositorio es un **proyecto multimódulo**: un `pom.xml` padre en la raíz y, adentro, dos módulos con su propio `pom.xml` cada uno.

```
smartlife-sales-service-orquestacion-cu/
├── pom.xml              ← padre: declara los módulos y fija las versiones para todos
├── common-lib/          ← módulo vacío: para código compartido entre servicios
│   └── pom.xml
└── sales-service/       ← el servicio de ventas: todo lo de este módulo en adelante
    ├── pom.xml
    └── src/...
```

**Multimódulo** quiere decir que un mismo proyecto Maven agrupa varios proyectos más chicos (los módulos) que se compilan juntos. El padre cumple la misma función que el `<parent>` que viste en el proyecto de la clase 02 (fijar las versiones de las dependencias para no repetirlas), pero para varios módulos a la vez. `common-lib` está vacío: es el lugar para el código que compartan varios servicios cuando aparezca (en el TP, por ejemplo). Tu TP también es multimódulo, siguiendo el template de la cátedra.

> 🕳️ **Madriguera — El `pom` padre por dentro**
> El padre importa la "lista de versiones" de Spring Boot (un BOM: *Bill of Materials*) en lugar de heredarla, y Maven puede compilar un solo módulo con sus dependencias (`mvn -pl sales-service -am`). Para seguir esta clase no hace falta; el template del TP ya lo trae resuelto.
> *Volvé al camino.*

> 🕳️ **Madriguera — `Dockerfile`, `.dockerignore`, `Readme.md`**
> El repositorio también trae lo necesario para armar una imagen de Docker del servicio y un Readme con los comandos. Docker se ve más adelante en el año.
> *Volvé al camino.*

### 2.2. Los `pom.xml` (comentados por bloque) 🟢

El padre, en la raíz:

```xml
<!-- Encabezado estándar de Maven: siempre igual. -->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- Identidad del proyecto padre. packaging "pom": no genera un JAR, solo agrupa. -->
  <groupId>ar.edu.utn.ba.ddsi</groupId>
  <artifactId>smartlife</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <name>ddsi-smartlife</name>
  <url>https://github.com/dds-utn/smartlife</url>

  <!-- Los módulos que se compilan juntos. -->
  <modules>
    <module>common-lib</module>
    <module>sales-service</module>
  </modules>

  <!-- Versiones en un solo lugar: Java, Spring Boot, Spring Cloud, Lombok. -->
  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <java.version>21</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <spring.boot.maven.plugin.version>4.0.5</spring.boot.maven.plugin.version>
    <spring.boot.dependencies.version>4.0.5</spring.boot.dependencies.version>
    <spring.cloud-version>2025.1.1</spring.cloud-version>
    <lombok.version>1.18.42</lombok.version>
  </properties>

  <!-- La "lista de versiones" de Spring Boot y de Spring Cloud: gracias a esto,
       los módulos declaran dependencias de Spring SIN poner <version>. -->
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring.boot.dependencies.version}</version>
        <scope>import</scope>
        <type>pom</type>
      </dependency>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring.cloud-version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <!-- Dependencias que heredan TODOS los módulos: Lombok y el kit de tests. -->
  <dependencies>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>${lombok.version}</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <!-- Configura el plugin de Spring Boot sin activarlo: lo activa cada módulo que lo declara
         (sales-service). Su tarea "repackage" convierte el JAR común en uno ejecutable con java -jar. -->
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-maven-plugin</artifactId>
          <version>${spring.boot.maven.plugin.version}</version>
          <executions>
            <execution>
              <goals>
                <goal>repackage</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </pluginManagement>

    <!-- Compilador: Java 21 y Lombok como procesador de anotaciones
         (es lo que genera los getters/setters al compilar). -->
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <release>${java.version}</release>
          <parameters>true</parameters>
          <annotationProcessorPaths>
            <path>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
              <version>${lombok.version}</version>
            </path>
          </annotationProcessorPaths>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

El de `common-lib` solo declara quién es su padre:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <!-- Apunta al pom padre: de ahí hereda versiones y dependencias. -->
    <parent>
        <groupId>ar.edu.utn.ba.ddsi</groupId>
        <artifactId>smartlife</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>common-lib</artifactId>
    <packaging>jar</packaging>

    <dependencies>
    </dependencies>
</project>
```

Y el del servicio de ventas, el que importa:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>ar.edu.utn.ba.ddsi</groupId>
        <artifactId>smartlife</artifactId>
        <version>1.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <groupId>ar.edu.utn.ba.ddsi.smartlife</groupId>
    <artifactId>sales-service</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <!-- Lo que convierte al proyecto en una aplicación web: servidor embebido,
             controllers, conversión JSON. Sin <version>: la fija el padre. -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <!-- Reinicio automático al cambiar código, solo en desarrollo. -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <!-- Arma el JAR ejecutable, dejando afuera a Lombok (solo hace falta al compilar). -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2.3. Configuración y punto de entrada

`sales-service/src/main/resources/application.yaml`:

```yaml
# El nombre de la aplicación y el puerto donde escucha (por defecto sería 8080).
spring:
  application:
    name: sales-service
server:
  port: 8082
```

`SalesServiceApplication.java`, igual que en la clase 02:

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada. @SpringBootApplication le dice a Spring: "arrancá desde acá
// y buscá todas las clases anotadas en este paquete y en sus subpaquetes".
@SpringBootApplication
public class SalesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesServiceApplication.class, args);
    }

}
```

Ese "en sus subpaquetes" va a importar enseguida: todas las clases del servicio viven **debajo** del paquete de esta clase.

### 2.4. Los paquetes son las capas 🔴

En M1 quedó dicho que una capa se **materializa** como paquete. Acá está, sobre el código real. Paquete base: `ar.edu.utn.ba.ddsi.smartlife.sales_service`.

```
sales_service/
├── controllers/            ── PRESENTACIÓN (no gráfica)             → M8
│   └── advice/                 manejo global de errores             → M8
├── services/               ── DOMINIO INTERMEDIO (las interfaces)   → M7
│   └── impl/                   las implementaciones                 → M7
├── models/
│   └── entities/           ── DOMINIO PURO (lo de la clase 03)
├── repositories/           ── DATOS (las interfaces)                → este módulo
│   └── inmemory/               las implementaciones en memoria      → este módulo
├── dtos/                   ── lo que viaja entre capas              → M6
├── exceptions/             ── los errores propios                   → M6
├── config/                 ── carga de datos iniciales              → M9
├── utils/                  ── utilitarios (generador de ids)        → este módulo
└── SalesServiceApplication.java
```

Las cuatro clases típicas de M3 y M4 tienen cada una su paquete. Los cuatro de abajo (`dtos`, `exceptions`, `config`, `utils`) son piezas de apoyo que vas a ir conociendo.

**El dominio de la clase 03** está en `models/entities`, sin cambios de diseño. No se re-explica; lo que hay:

| Paquete | Clases |
|---|---|
| `comercio` | `Comercio`: sus productos, sus ventas, sus observadores; `agregarVenta` valida que los productos sean propios y notifica |
| `impuestos` | `Impuesto` (interfaz), `IVA`, `EO`, `EI` |
| `observers` | `ObservadorVenta` (interfaz), `ObservadorSffa`, `ObservadorSvibaa` |
| `productos` | `Producto` (`totalImpuestos()`, `precioFinal()`), `TipoProducto` (con su lista de impuestos) |
| `venta` | `Venta` (sus ítems y los tres totales), `ItemVenta` (producto y cantidad) |

El código completo está en el snapshot de referencia. Respecto de la clase 03 cambian **solo los ids**, para que los repositorios puedan asignarlos y buscar por ellos: `Venta` pasa de `long id` (final, sin setter) a `Long id` con `@Setter`; `TipoProducto` gana un `Long id` con `@Setter` y un segundo constructor `TipoProducto(Long id, String descripcion)`; y el `id` de `Comercio` pasa de `long` a `Long`. `Producto` ya tenía `Long id` con setter. En `Producto`, `TipoProducto` y `Venta` tiene que ser `Long` (objeto) y no `long` (primitivo) porque el repositorio usa `null` para saber que una entidad todavía no se guardó (§5), y un `long` no puede valer `null`. En `Comercio`, cuyo repositorio no genera ids (§5.4), el cambio es por uniformidad con las demás entidades. Lo que eso obliga a cuidar al comparar ids, en §5.4.

---

## 3. Beans: quién crea los objetos 🔴

Antes de escribir el primer repositorio hace falta responder la pregunta que dejó M4: **¿quién crea a los repositorios?**

En un programa Java común, los objetos los creás vos: `new InMemoryProductoRepository()`. En Spring Boot, las clases de las capas **no las instanciás vos: las marcás**, y las instancia Spring:

```
   vos escribís                           Spring Boot, al arrancar
   ────────────                           ────────────────────────
   @Repository                            1. recorre el paquete y sus subpaquetes
   public class InMemoryProducto...  ──►  2. encuentra la clase anotada
                                          3. la instancia (una sola vez)
                                          4. la guarda y la administra
                                          5. se la da a quien la necesite
```

A un objeto que Spring crea y administra se le llama **bean**. Y la anotación que le pusiste a la clase para que Spring la convierta en bean dice, además, **qué rol cumple**. Son tres, una por capa:

| Anotación | Capa | Dónde la vas a ver |
|---|---|---|
| `@Repository` | Datos | este módulo |
| `@Service` | Dominio intermedio | M7 |
| `@RestController` | Presentación | M8 |

Para crear el objeto, las tres funcionan igual: "Spring, esta clase es tuya; instanciala vos". El nombre deja escrito a qué capa pertenece cada clase (`@RestController`, además, prepara la clase para atender pedidos HTTP: M8). Esto es exactamente lo que viste en la preclase 04 como diferencia entre biblioteca y framework: **vos no llamás a Spring para que cree tus objetos; Spring llama a tu código** y lo arma.

¿Y "dárselo a quien lo necesite"? Eso es la **inyección de dependencias** de la preclase 04, hecha por Spring. Lo vas a ver en acción en M7, cuando el service reciba sus repositorios.

Fijate el contraste: `GeneradorIdSecuencial` (§5.1) **no** es un bean. No tiene anotación, y cada repositorio que lo usa (todos menos el de comercios, §5.4) lo crea con `new`.

> **Para el parcial, si te preguntan:** *¿Qué es un bean y quién lo crea?*
> Un objeto que crea y administra el framework, no el programador: al arrancar, Spring busca las clases anotadas (`@Repository`, `@Service`, `@RestController`) en el paquete de la aplicación y sus subpaquetes, crea una instancia de cada una (por defecto, una sola) y se la inyecta a quien la necesite. Es la inversión de control: el framework llama a tu código.

> 🕳️ **Madriguera — Una instancia sola**
> Por defecto, Spring crea **una única instancia** de cada bean y se la da a todos los que la piden. Por eso el repositorio en memoria funciona: todos los services comparten la misma lista.
> *Volvé al camino.*

---

## 4. La capa de datos: las interfaces 🔴

El contrato de M3 (guardar, modificar, eliminar, buscar, buscar todos) escrito en Java, para productos:

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;

import java.util.List;
import java.util.Optional;

// El contrato. Quien use el repositorio de productos conoce ESTO,
// nunca la clase que guarda de verdad.
// findAll = buscar todos; findById = buscar uno (puede no estar: Optional);
// save = guardar Y modificar (§5); delete = eliminar.
public interface ProductoRepository {

    List<Producto> findAll();

    Optional<Producto> findById(Long id);

    Producto save(Producto producto);

    void delete(Producto producto);
}
```

Los nombres están en inglés, como es habitual en el código (`findAll`, `findById`, `save`, `delete`), y hay cuatro métodos en lugar de cinco: `save` cumple el papel de **guardar** y de **modificar** a la vez. Cómo lo hace, en §5. Fijate también que acá los nombres van en singular (`ProductoRepository`) y en la teoría iban en plural (`AlumnosRepository`). Las dos convenciones existen; lo que importa es usar una sola en todo el proyecto.

**`Optional<Producto>`.** Un `Optional` es una "caja" que puede traer un objeto adentro o venir vacía. `findById` lo devuelve porque el producto con ese id **puede no existir**, y el `Optional` obliga a quien lo usa a hacerse cargo de ese caso en lugar de recibir un `null` silencioso. Qué hace el service cuando la caja viene vacía se ve en M7.

Las otras tres interfaces son **iguales**, cambiando el tipo:

| Interfaz | Entidad | Firmas |
|---|---|---|
| `ProductoRepository` | `Producto` | `findAll()`, `findById(Long)`, `save(Producto)`, `delete(Producto)` |
| `TipoProductoRepository` | `TipoProducto` | ídem, con `TipoProducto` |
| `VentaRepository` | `Venta` | ídem, con `Venta` |
| `ComercioRepository` | `Comercio` | ídem, con `Comercio` |

(el código exacto está en el snapshot). Es la repetición de M3 §4. Repetir cuatro interfaces casi idénticas está bien en esta etapa.

---

## 5. La capa de datos: las implementaciones en memoria 🔴

### 5.1. El generador de ids

En una base de datos, el id de cada fila lo genera la base. En memoria no hay base, así que alguien tiene que generarlo. Para eso está esta clase utilitaria, en `utils/` (es la misma clase con la que los tests de la clase 03 numeraban el comercio y las ventas):

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.utils;

// Devuelve 1, 2, 3... cada vez que se le pide un id nuevo:
// por defecto arranca en 1, y cada llamada devuelve el actual y avanza.
public class GeneradorIdSecuencial {

    private long siguiente;

    public GeneradorIdSecuencial() {
        this(1L);
    }

    public GeneradorIdSecuencial(long valorInicial) {
        this.siguiente = valorInicial;
    }

    public long siguiente() {
        return siguiente++;
    }

}
```

### 5.2. El repositorio de productos, completo

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.inmemory;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.ProductoRepository;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.utils.GeneradorIdSecuencial;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// @Repository: Spring crea una única instancia de esta clase al arrancar (un bean de la
// capa de datos). Implementa el contrato: quien lo use solo va a ver ProductoRepository.
@Repository
public class InMemoryProductoRepository implements ProductoRepository {

    // El "medio persistente": una lista en memoria (se pierde al apagar la aplicación).
    // Cada repositorio que genera ids tiene su propio generador.
    private final List<Producto> productos = new ArrayList<>();
    private final GeneradorIdSecuencial generadorId = new GeneradorIdSecuencial();

    // Devuelve una COPIA de la lista: quien la recibe puede recorrerla,
    // pero no puede agregarle ni sacarle elementos a la lista del repositorio.
    // (Los productos de adentro son los mismos objetos: si modificás uno, se modifica el guardado.)
    @Override
    public List<Producto> findAll() {
        return new ArrayList<>(productos);
    }

    // Recorre la lista y devuelve el primero con ese id, envuelto en un Optional
    // (vacío si no hay ninguno).
    @Override
    public Optional<Producto> findById(Long id) {
        return productos.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    // save cumple dos papeles:
    //  - producto SIN id → es nuevo: le asigna el siguiente id y lo agrega (GUARDAR).
    //  - producto CON id → lo trata como existente: saca el que tenga ese id (si había)
    //                      y agrega este (MODIFICAR).
    @Override
    public Producto save(Producto producto) {
        if (producto.getId() == null) {
            producto.setId(generadorId.siguiente());
            productos.add(producto);
            return producto;
        }
        delete(producto);
        productos.add(producto);
        return producto;
    }

    // Recibe la entidad y la elimina por su id. Si no tiene id, nunca se guardó: no hace nada.
    @Override
    public void delete(Producto producto) {
        if (producto.getId() == null) {
            return;
        }
        productos.removeIf(p -> p.getId().equals(producto.getId()));
    }
}
```

```
// ¿CÓMO FUNCIONA?
//   Producto tv = new Producto(null, tipoElectronico, 100, "Smart TV 50");   // sin id
//   repo.save(tv);            → tv.getId() vale 1; la lista tiene [tv]
//   repo.findById(1L);        → Optional con tv adentro
//   repo.findById(7L);        → Optional vacío
//   tv.setPrecioBase(120);    → tv es el MISMO objeto que está en la lista: ya quedó modificado
//   repo.save(tv);            → tiene id: saca el que tiene id 1 y agrega tv. La lista sigue con 1 elemento
//                               (en memoria el save "no se nota"; con base de datos, sin él no se guarda el cambio)
//   repo.delete(tv);          → la lista queda vacía
```

Términos nuevos:
- **`stream().filter(...).findFirst()`**: recorre la colección, se queda con los que cumplen la condición y devuelve el primero como `Optional`. Es la API de streams del seminario de Java.
- **`removeIf(condición)`**: elimina de la lista todos los elementos que cumplen la condición.
- **`equals`** para comparar ids, no `==`. Por qué, en §5.4.

Fijate en la **Regla 1** en el código: el repositorio importa y usa `Producto`. `Producto` no tiene ninguna línea que mencione a un repositorio.

La pregunta de M1 (**¿quién guarda?**) ya tiene código: `InMemoryProductoRepository.save`. Quién lo llama (el service), en M7.

> 💡 Si en algún lado ves una versión de estos repositorios que guarda en un `Map` (un diccionario id → producto) y tiene métodos separados `saveNew` y `saveExisting` en lugar de un único `save`, y `deleteById(id)` en lugar de `delete(producto)`, es equivalente: cambia la estructura en memoria y cómo se reparte guardar/modificar, no la idea.

### 5.3. Los otros tres

`InMemoryTipoProductoRepository` y `InMemoryVentaRepository` son **idénticos** al de productos, cambiando el tipo de la entidad, el nombre de la lista, el del parámetro y la variable de la lambda (el código exacto está en el snapshot):

| Clase | Implementa | Lista | Parámetro | Lambda | Generador |
|---|---|---|---|---|---|
| `InMemoryProductoRepository` | `ProductoRepository` | `productos` | `producto` | `p` | sí |
| `InMemoryTipoProductoRepository` | `TipoProductoRepository` | `tipos` | `tipoProducto` | `t` | sí |
| `InMemoryVentaRepository` | `VentaRepository` | `ventas` | `venta` | `v` | sí |
| `InMemoryComercioRepository` | `ComercioRepository` | `comercios` | `comercio` | `c` | **no** (ver abajo) |

### 5.4. El de comercios: el que es distinto

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.inmemory;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.comercio.Comercio;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.repositories.ComercioRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryComercioRepository implements ComercioRepository {

    private final List<Comercio> comercios = new ArrayList<>();

    @Override
    public List<Comercio> findAll() {
        return new ArrayList<>(comercios);
    }

    @Override
    public Optional<Comercio> findById(Long id) {
        return comercios.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    // Sin generador de ids: el comercio YA viene con id. Siempre saca la versión
    // vieja (si había) y agrega la nueva.
    @Override
    public Comercio save(Comercio comercio) {
        delete(comercio);
        comercios.add(comercio);
        return comercio;
    }

    @Override
    public void delete(Comercio comercio) {
        comercios.removeIf(c -> c.getId().equals(comercio.getId()));
    }
}
```

Una diferencia con los otros tres, y una trampa que vale para los cuatro:

- **No genera ids.** Los comercios no se dan de alta por la API del servicio de ventas (ningún caso de uso lo pide): ya existen, y llegan con su id. Pero entonces, **¿de dónde sale el comercio con id 1** con el que vas a trabajar? Alguien tiene que cargarlo al arrancar. Se responde en M9.
- **Compara ids con `equals`, como todos.** Con `==` también compila y parece andar; vale la pena ver por qué no.

> 🔧 **Versión floja — Comparar ids `Long` con `==`**
>
> ```java
> // ❌ compila, y con ids chicos anda
> return comercios.stream().filter(c -> c.getId() == id).findFirst();
> ```
>
> `Long` (con mayúscula) es un **objeto**, no un número primitivo. Entre dos objetos, `==` no compara valores: compara si son **el mismo objeto en memoria**. Los dos ids que se comparan nacen por separado: el guardado, cuando se creó la entidad (escribir `1L` donde va un `Long` hace que Java lo convierta en objeto: eso se llama *autoboxing*); el buscado, cuando llega en un pedido y Spring lo convierte. Cada conversión da, en principio, un objeto nuevo, salvo para los valores de −128 a 127: para esos Java devuelve siempre el mismo objeto, que tiene guardado de antemano. Por eso con ids 1, 2, 3 los dos lados del `==` son el mismo objeto y la comparación "anda".
>
> **Dónde falla:** a partir del 128. El día que exista el comercio 200 (o se cree el producto 128), buscarlo devuelve un `Optional` vacío aunque esté en la lista, y quien lo pidió recibe un "no existe". Anda en todas las pruebas con ids chicos (las de este proyecto usan el comercio 1) y se rompe recién cuando los datos crecen.
>
> Con `long` (minúscula, primitivo) el `==` sí compara valores. Por eso, al pasar el id de `Comercio` a `Long` para que sea igual al del resto de las entidades, la comparación tiene que ser con `equals`.

---

### 5.5. ¿Por qué interfaz? La respuesta

La pregunta de M3 tiene ahora una respuesta concreta: el service que use los productos va a recibir un `ProductoRepository`, y **nunca va a saber** que detrás hay un `InMemoryProductoRepository`. El día que la persistencia sea en una base de datos, se escribe otra clase que implemente `ProductoRepository`, y el service no cambia una línea. Lo vas a ver del lado del service en M7.

> **Para el parcial, si te preguntan:** *¿Por qué el service depende de `ProductoRepository` y no de `InMemoryProductoRepository`?*
> Porque entre capas se conocen contratos, no implementaciones: así la persistencia se reemplaza (de memoria a base de datos) escribiendo otra clase que implemente la misma interfaz, sin tocar el service. Priorizo bajo acoplamiento entre dominio y datos; pago con una interfaz más por repositorio.

---

## 6. Estado del proyecto 🟡

```
sales_service/
├── SalesServiceApplication.java    ✅
├── models/entities/...             ✅ (clase 03)
├── repositories/                   ✅ 4 interfaces
│   └── inmemory/                   ✅ 4 implementaciones (@Repository)
└── utils/GeneradorIdSecuencial     ✅
```

Si lo levantás ahora, Spring arranca en el puerto 8082, crea los cuatro beans de repositorio… y no pasa nada más: no hay ningún endpoint, y nadie usa los repositorios todavía.

---

## 7. Lo que te llevás de este módulo

- El proyecto es **multimódulo**: padre + `common-lib` (vacío) + `sales-service`. Las versiones son las de este ejemplo; el TP puede tener otras, los conceptos son los mismos.
- **Cada paquete materializa una capa**: `controllers`, `services`, `models/entities`, `repositories`, más `dtos`, `exceptions`, `config`, `utils`.
- **Bean:** objeto que Spring crea y administra. `@Repository`, `@Service`, `@RestController` son el mismo mecanismo, con el nombre de la capa.
- **Repositorios:** cuatro interfaces (`findAll`, `findById` → `Optional`, `save`, `delete`) y cuatro implementaciones en memoria. `save` guarda y modifica. Los ids, con `equals`.
- **¿Por qué interfaz?** El service va a conocer `ProductoRepository`, nunca `InMemoryProductoRepository`: cambiar la persistencia es escribir otra implementación, sin tocar el service.
- Los ids de las entidades son `Long`: en las que el repositorio numera, `null` significa "todavía no se guardó".
- Quedan abiertos: qué se hace con un `Optional` vacío (M7) y de dónde sale el comercio 1 (M9).

---

## ✅ Checkpoint del Módulo 5

Respondé de memoria. Las respuestas van al complemento.

1. ¿Qué es un proyecto multimódulo? ¿Qué módulos tiene este y para qué está `common-lib`?
2. Ubicá cada capa de M4 en un paquete del proyecto.
3. ¿Qué es un bean? ¿Qué tienen en común y en qué se diferencian `@Repository`, `@Service` y `@RestController`?
4. ¿Por qué que Spring cree los repositorios anotados con `@Repository` es un ejemplo de "el framework te llama a vos"?
5. ¿Por qué `findById` devuelve un `Optional` y no directamente un `Producto`?
6. `save` cumple dos papeles. ¿Cuáles, y cómo decide cuál ejecutar?
7. ¿Por qué `findAll` devuelve `new ArrayList<>(productos)` y no la lista misma?
8. ¿En qué se diferencia el repositorio de comercios de los otros tres, y por qué? ¿Por qué los ids de las entidades son `Long` y no `long`?
9. Explicá por qué `c.getId() == id` con ids `Long` funciona con el comercio 1 y falla con el comercio 200.
10. El service de productos va a recibir un `ProductoRepository`. ¿Por qué no un `InMemoryProductoRepository`? ¿Qué cambia el día que los productos se guarden en una base de datos?

---

## Qué viene después

**M6 — Lo que viaja y lo que falla: DTOs y excepciones propias.** Antes de escribir los services hacen falta dos piezas de apoyo que ellos usan: los objetos que transportan los datos entre el mundo exterior y el dominio (y por qué no se usan las entidades directamente), y los errores propios del servicio.

**FIN DEL MÓDULO 5**
