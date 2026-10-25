# 📦 Fuente de Repo — SmartLife · Sales Service (Clase 03)

**Repositorio:** `smartlife` — módulo `sales-service` (ejercicio *SmartLife – Servicio de Ventas*).
**Fecha del snapshot:** 2026-04-16 (fecha de los archivos en el zip; corresponde al commit de la descarga de la rama `main`).
**Commit:** no identificable desde el zip (descarga de rama `main`, sin metadatos `.git`).
**Origen:** repo de la **clase 03** de DDSI, compartido por la cátedra tras la clase.
**Stack:** Java 21 · Spring Boot · Maven (multi-módulo) · Lombok · JUnit 5.

> Snapshot **fiel**: el código va verbatim, sin marcas, sin comentarios agregados y sin interpretación (la explicación pedagógica vive en el apunte maestro/recorrido). Si la cátedra actualiza el repo, regenerar este archivo con el zip nuevo.

---

## Árbol del repositorio

```text
smartlife/
  common-lib/
    src/
      main/
        java/
          ar/
            edu/
              utn/
                frba/
                  ddsi/
                    common/
                      .gitkeep
    pom.xml
  sales-service/
    src/
      main/
        java/
          ar/
            edu/
              utn/
                ba/
                  ddsi/
                    smartlife/
                      sales_service/
                        models/
                          entities/
                            comercio/
                              Comercio.java
                            impuestos/
                              EI.java
                              EO.java
                              Impuesto.java
                              IVA.java
                            observers/
                              ObservadorSffa.java
                              ObservadorSvibaa.java
                              ObservadorVenta.java
                            productos/
                              Producto.java
                              TipoProducto.java
                            venta/
                              ItemVenta.java
                              Venta.java
                        utils/
                          GeneradorIdSecuencial.java
                        SalesServiceApplication.java
        resources/
          application.yaml
      test/
        java/
          ar/
            edu/
              utn/
                ba/
                  ddsi/
                    smartlife/
                      sales_service/
                        domain/
                          DominioSalesServiceTest.java
    Dockerfile
    pom.xml
  .dockerignore
  .gitignore
  Diagrama de clases.puml
  pom.xml
  Readme.md
```

---

## Archivos

### Configuración, build y documentación

#### `pom.xml`

POM padre del proyecto multi-módulo (reactor): declara los módulos y las versiones comunes.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>ar.edu.utn.ba.ddsi</groupId>
  <artifactId>smartlife</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <name>ddsi-smartlife</name>
  <url>https://github.com/dds-utn/smartlife</url>
  <modules>
    <module>common-lib</module>
    <module>sales-service</module>
  </modules>

  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <java.version>21</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <spring.boot.maven.plugin.version>4.0.5</spring.boot.maven.plugin.version>
    <spring.boot.dependencies.version>4.0.5</spring.boot.dependencies.version>
    <spring.cloud-version>2025.1.1</spring.cloud-version>
    <lombok.version>1.18.42</lombok.version>
  </properties>

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

#### `common-lib/pom.xml`

POM de la biblioteca común compartida (aún sin código; solo el marcador de carpeta).

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

    <artifactId>common-lib</artifactId>
    <packaging>jar</packaging>

    <dependencies>
    </dependencies>
</project>
```

#### `sales-service/pom.xml`

POM del microservicio de ventas: dependencias (Spring Boot, Lombok) y build.

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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

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

#### `sales-service/Dockerfile`

Imagen Docker del sales-service (etapas de build y runtime).

```dockerfile
# ==========================
# Etapa 1: Build de Maven
# ==========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# nombre del directorio físico del servicio,
ARG SERVICE_NAME=sales-service

# Copiar los archivos de configuración de Maven
COPY . .

# Compilar el proyecto (sin tests)
# -pl: Solo tu servicio
# -am: También sus dependencias (common-lib)
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests


# ==========================
# Etapa 2: Imagen final (solo el JAR)
# ==========================
FROM eclipse-temurin:21-jre
WORKDIR /app

# nombre del directorio físico del servicio,
ARG SERVICE_NAME=sales-service

# Copiar el JAR compilado desde la etapa anterior
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### `sales-service/src/main/resources/application.yaml`

Configuración del servicio (nombre de aplicación, puerto).

```yaml
spring:
  application:
    name: sales-service
server:
  port: 8082
```

#### `Readme.md`

Instrucciones de build y ejecución del proyecto.

````markdown
# SmartLife (DDSI — UTN BA)

Implementación del caso práctico **SmartLife**. El alcance y las reglas de negocio del dominio siguen el enunciado oficial:

[**[DDSI UTN BA] SmartLife — Caso práctico** (Google Docs)](https://docs.google.com/document/d/1N7W2UuWuqmDRuR1pTH5QtjBoojgJwH9_ujmGvih9fmA/edit?tab=t.0#heading=h.9alaamq85m85)

El código de este repositorio está pensado para **respetar ese enunciado** (entidades, flujos y responsabilidades que allí se definen).

---

## Requisitos previos

- JDK 21
- Maven 3.9+
- Docker (opcional, solo para construir y ejecutar contenedores)

---

## Estructura del repositorio

```
smartlife/
├── pom.xml                 # POM padre: versiones y dependencyManagement
├── common-lib/             # Librería compartida (JAR), disponible en el reactor
└── sales-service/        # Servicio de ventas — puerto 8082 (ver application.yaml)
```

`common-lib` forma parte del reactor Maven; los builds Docker usan la raíz como contexto para resolver el POM padre y los módulos (`-pl sales-service -am`).

---

## Tecnologías

| Tecnología          | Versión       |
|---------------------|---------------|
| Java                | 21            |
| Spring Boot         | 4.0.5         |
| Spring Cloud BOM    | 2025.1.1      |
| Lombok              | 1.18.42       |
| Maven               | 3.9+          |

El BOM de Spring Cloud está declarado en el POM padre para que los módulos puedan incorporar dependencias de Spring Cloud sin fijar versión en cada uno.

---

## Desarrollo local (Maven)

Todos los comandos se ejecutan desde la **raíz del proyecto**.

### Compilar todos los módulos

```bash
mvn clean install
```

Esto construye `common-lib` y luego `sales-service` según el orden del reactor.

### Ejecutar el servicio de ventas

```bash
mvn spring-boot:run -pl sales-service
```

El puerto por defecto está definido en `sales-service/src/main/resources/application.yaml` (8082).

---

## Construcción de imágenes Docker

El proyecto es multi-módulo Maven. **El contexto de construcción debe ser la raíz del repositorio**; si se limita a la carpeta del servicio, Maven no encontrará el POM padre ni el resto del reactor.

### Construcción manual (CLI)

Desde la raíz del proyecto, usando el Dockerfile del servicio con `-f` y contexto `.`:

```bash
docker build -t sales-service-img -f sales-service/Dockerfile .
```

### Ejecutar el contenedor

Ajustá el mapeo de puertos al que exponga la aplicación dentro del contenedor (en `application.yaml` está **8082**):

```bash
docker run -p 8082:8082 sales-service-img
```

### Nota sobre `ARG SERVICE_NAME`

El Dockerfile define `ARG SERVICE_NAME` (por defecto `sales-service`). Solo hace falta sobreescribirlo si reutilizás el mismo patrón de build para otro módulo:

```bash
docker build --build-arg SERVICE_NAME=otro-service -f otro-service/Dockerfile .
```

---

## Estado del proyecto

`sales-service` concentra el dominio de ventas alineado al enunciado SmartLife (comercio, productos, ventas, impuestos, observadores, etc.). `common-lib` está preparada para código compartido entre servicios a medida que el trabajo práctico lo requiera.
````

#### `.gitignore`

Exclusiones de Git.

```text
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/
.kotlin
.idea/

### Logs y Temporales ###
*.log
*.tmp
/logs/
**/logs/

### IntelliJ IDEA ###
.idea/modules.xml
.idea/jarRepositories.xml
.idea/compiler.xml
.idea/libraries/
*.iws
*.iml
*.ipr

### Eclipse ###
.apt_generated
.classpath
.factorypath
.project
.settings
.springBeans
.sts4-cache

### NetBeans ###
/nbproject/private/
/nbbuild/
/dist/
/nbdist/
/.nb-gradle/
build/
!**/src/main/**/build/
!**/src/test/**/build/

### VS Code ###
.vscode/

### Mac OS ###
.DS_Store
```

#### `.dockerignore`

Exclusiones del contexto de build de Docker.

```text
# Build
target/
**/target/

# Git
.git
.gitignore

# IDE
.idea/
.vscode/
*.iml
*.iws
*.ipr

# Logs
*.log
**/logs/

# OS
.DS_Store
Thumbs.db

# Env / configs locales
.env
.env.*
application-local.yml
application-local.properties
```

#### `Diagrama de clases.puml`

Diagrama de clases del dominio en PlantUML (fuente de texto).

```text
@startuml
'https://plantuml.com/class-diagram

class Comercio {
    - id: long
    - productos: Producto[]
    - ventas: Ventas[]
    - observers: ObservadorVenta[]
}

class Producto {
    - id: long
    - tipo: TipoDeProducto
    - nombre: string
    -precioBase: double

    precioFinal(): double
    totalDeImpuestos(): double
}

class TipoDeProducto {
    - nombre: String
    - impuestos: Impuesto[]
}

interface Impuesto {
    calcularSobre(producto: Producto): double
}

class IVA implements Impuesto {
    calcularSobre(producto: Producto): double
}

class EO implements Impuesto {
    calcularSobre(producto: Producto): double
}

class EI implements Impuesto {
    calcularSobre(producto: Producto): double
}

class ItemVenta {
    - producto: Producto
    - cantidad: int

    + totalDeImpuestos(): double
    + totalPrecioBase(): double
}

class Venta {
    - id: long
    - fecha: LocalDate
    - items: ItemVenta[]
    + total(): double
    + totalDeImpuestos(): double
    + totalPrecioBase(): double
}

interface ObservadorVenta {
    serNotificadoDe(Venta venta): double
}

class SFFA implements ObservadorVenta {
    serNotificadoDe(Venta venta): double
}



Comercio ->"*" ObservadorVenta
Venta ->"*" ItemVenta
ItemVenta -> Producto
Comercio ->"*" Venta
Comercio ->"*" Producto
Producto -> TipoDeProducto
TipoDeProducto ->"*" Impuesto
Impuesto .> Producto
@enduml
```

#### `common-lib/src/main/java/ar/edu/utn/frba/ddsi/common/.gitkeep`

Marcador de carpeta vacía (common-lib todavía sin código).

```text
(archivo vacío)
```

### Código fuente — `sales-service/src/main`

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/SalesServiceApplication.java`

Clase main: arranque de la aplicación Spring Boot.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SalesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesServiceApplication.class, args);
	}

}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/comercio/Comercio.java`

Entidad Comercio: productos, ventas y observadores; registra ventas y notifica.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.comercio;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.observers.ObservadorVenta;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.ItemVenta;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.Venta;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Comercio {

	private long id;
	private List<Producto> productos;
    private List<Venta> ventas;
	private List<ObservadorVenta> observadores;

	public Comercio(long id) {
		this.id = id;
		this.productos = new ArrayList<>();
		this.observadores = new ArrayList<>();
        this.ventas = new ArrayList<>();
	}

    public void agregarProducto(Producto ... productos) {
        Collections.addAll(this.productos, productos);
    }

    public void agregarObservadores(ObservadorVenta ... observadores) {
        Collections.addAll(this.observadores, observadores);
    }

    public void agregarVenta(Venta venta) {
        if(!sonTodosProductosPropios(venta.getItems().stream().map(ItemVenta::getProducto).toList())) {
            throw new IllegalArgumentException("El comercio no puede registrar ventas de productos que no vende");
        }
        this.ventas.add(venta);
        this.observadores.forEach(o -> o.serNotificadoDe(venta));
    }

	public void eliminarObservador(ObservadorVenta observador) {
		this.observadores.remove(observador);
	}

    private boolean sonTodosProductosPropios(List<Producto> productos) {
        return this.productos.containsAll(productos);
    }
}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/productos/Producto.java`

Entidad Producto: delega el cálculo de impuestos en su TipoProducto.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

	private Long id;
	private TipoProducto tipo;
	private double precioBase;
	private String descripcion;

	public double totalImpuestos() {
		return this.tipo.totalImpuestos(this);
	}

	public double precioFinal() {
		return this.precioBase + this.totalImpuestos();
	}

}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/productos/TipoProducto.java`

Tipo de producto: lista de impuestos; suma los impuestos de un producto.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos.Impuesto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class TipoProducto {
    @Setter
	private String descripcion;
	private final List<Impuesto> impuestos;

	public TipoProducto(String descripcion) {
		this.descripcion = descripcion;
		this.impuestos = new ArrayList<>();
	}

	public void agregarImpuestos(Impuesto... impuestosNuevos) {
		Collections.addAll(this.impuestos, impuestosNuevos);
	}

	public double totalImpuestos(Producto producto) {
        return this.impuestos.stream().mapToDouble(i -> i.calcular(producto)).sum();
	}
}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/impuestos/Impuesto.java`

Interfaz Impuesto: contrato calcular(Producto).

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;

public interface Impuesto {

	double calcular(Producto producto);

}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/impuestos/IVA.java`

Implementación concreta del IVA.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import lombok.Getter;
import lombok.Setter;

public class IVA implements Impuesto {

	@Getter
    @Setter
	private static double porcentaje = 0.21;

	@Override
	public double calcular(Producto producto) {
		return IVA.porcentaje * producto.getPrecioBase();
	}
}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/impuestos/EO.java`

Implementación concreta del impuesto EO.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import lombok.Getter;
import lombok.Setter;

public class EO implements Impuesto {

	@Getter
	@Setter
	private static double factorPrecioBase = 0.5;

	@Getter
	@Setter
	private static double coeficienteGanancias = 4;

	@Getter
	@Setter
	private static double gananciasImpositivas = 4;

	@Override
	public double calcular(Producto producto) {
		return EO.factorPrecioBase * producto.getPrecioBase()
				+ EO.coeficienteGanancias * EO.gananciasImpositivas;
	}
}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/impuestos/EI.java`

Implementación concreta del impuesto EI.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import lombok.Getter;
import lombok.Setter;

public class EI implements Impuesto {

	@Getter
	@Setter
	private static double divisorPrecioBase = 4;

	@Getter
	@Setter
	private static double factorGanancias = 0.3;

	@Getter
	@Setter
	private static double gananciasImpositivas = 3.5;

	@Override
	public double calcular(Producto producto) {
		return producto.getPrecioBase() / EI.divisorPrecioBase
				+ EI.factorGanancias * EI.gananciasImpositivas;
	}
}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/venta/Venta.java`

Entidad Venta: ítems y totales (base, impuestos, final).

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta;

import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Venta {

	private final long id;
	private LocalDate fechaRegistro;
	private List<ItemVenta> items;

	public Venta(long id) {
		this.id = id;
		this.fechaRegistro = LocalDate.now();
		this.items = new ArrayList<>();
	}

    public void agregarItem(ItemVenta ... items) {
        Collections.addAll(this.items, items);
    }

	public double totalPrecioBase() {
		return this.items.stream().mapToDouble(ItemVenta::subtotalPrecioBase).sum();
	}

	public double totalImpuestos() {
		return this.items.stream().mapToDouble(ItemVenta::totalImpuestos).sum();
	}

	public double totalFinal() {
		return this.items.stream().mapToDouble(ItemVenta::totalFinal).sum();
	}
}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/venta/ItemVenta.java`

Renglón de venta: producto + cantidad; subtotales.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import lombok.Getter;

@Getter
public class ItemVenta {

	private final Producto producto;
	private final int cantidad;

	public ItemVenta(Producto producto, int cantidad) {
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
		}
		this.producto = producto;
		this.cantidad = cantidad;
	}

	public double subtotalPrecioBase() {
		return producto.getPrecioBase() * cantidad;
	}

	public double totalImpuestos() {
		return producto.totalImpuestos() * cantidad;
	}

	public double totalFinal() {
		return producto.precioFinal() * cantidad;
	}

}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/observers/ObservadorVenta.java`

Interfaz observador de venta: serNotificadoDe(Venta).

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.observers;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.Venta;

public interface ObservadorVenta {

	void serNotificadoDe(Venta venta);

}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/observers/ObservadorSffa.java`

Observador concreto SFFA (integración por SDK, pendiente).

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.observers;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.Venta;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ObservadorSffa implements ObservadorVenta {
    @Getter
    private long cantLlamadas;

	@Override
	public void serNotificadoDe(Venta venta) {
        //TODO: pendiente de integración real
		cantLlamadas++;
	}

}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/models/entities/observers/ObservadorSvibaa.java`

Observador concreto SVIBAA (integración por REST, pendiente).

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.observers;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.Venta;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ObservadorSvibaa implements ObservadorVenta {
    @Getter
    private long cantLlamadas = 0;

	@Override
	public void serNotificadoDe(Venta venta) {
        //TODO: pendiente de integración real
		cantLlamadas++;
	}
}
```

#### `sales-service/src/main/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/utils/GeneradorIdSecuencial.java`

Utilidad: generador de ids secuenciales.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.utils;

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

### Código fuente — `sales-service/src/test`

#### `sales-service/src/test/java/ar/edu/utn/ba/ddsi/smartlife/sales_service/domain/DominioSalesServiceTest.java`

Tests de dominio: casos de impuestos por tipo y verificación de notificación a observadores.

```java
package ar.edu.utn.ba.ddsi.smartlife.sales_service.domain;

import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.comercio.Comercio;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.observers.ObservadorSffa;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.observers.ObservadorSvibaa;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.utils.GeneradorIdSecuencial;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos.EI;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos.EO;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.impuestos.IVA;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.Producto;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.productos.TipoProducto;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.ItemVenta;
import ar.edu.utn.ba.ddsi.smartlife.sales_service.models.entities.venta.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DominioSalesServiceTest {

	private static final double PRECIO_BASE_EJEMPLO = 100;
	private static final double IVA_UNIT = 21;
	private static final double EO_UNIT = 66;
	private static final double EI_UNIT = 26.05;
	private static final double IMPUESTOS_ELECTRONICO_UNIT = IVA_UNIT + EO_UNIT;
	private static final double IMPUESTOS_HOGAR_UNIT = IVA_UNIT + EI_UNIT;
	private static final double FINAL_ELECTRONICO_UNIT = PRECIO_BASE_EJEMPLO + IMPUESTOS_ELECTRONICO_UNIT;
	private static final double FINAL_HOGAR_UNIT = PRECIO_BASE_EJEMPLO + IMPUESTOS_HOGAR_UNIT;

	private TipoProducto tipoElectronico;
	private TipoProducto tipoHogar;
    private GeneradorIdSecuencial generadorIdSecuencial;
    private ObservadorSffa observadorSffa;
    private ObservadorSvibaa observadorSvibaa;
    private Comercio comercio;

	@BeforeEach
	void setUp() {
		IVA.setPorcentaje(0.21);
		EO.setFactorPrecioBase(0.5);
		EO.setCoeficienteGanancias(4);
		EO.setGananciasImpositivas(4);
		EI.setDivisorPrecioBase(4);
		EI.setFactorGanancias(0.3);
		EI.setGananciasImpositivas(3.5);

		tipoElectronico = new TipoProducto("Electrónico");
		tipoElectronico.agregarImpuestos(new IVA(), new EO());

		tipoHogar = new TipoProducto("Hogar");
		tipoHogar.agregarImpuestos(new IVA(), new EI());

        generadorIdSecuencial = new GeneradorIdSecuencial();

        observadorSffa = new ObservadorSffa();
        observadorSvibaa = new ObservadorSvibaa();

        comercio = new Comercio(generadorIdSecuencial.siguiente());

        comercio.agregarObservadores(observadorSvibaa, observadorSffa);
	}

	@Test
	void ventaUnProductoElectronico_verificaIvaMasEo() {
		Producto tv = new Producto(null, tipoElectronico, PRECIO_BASE_EJEMPLO, "Smart TV 50");

		Venta venta = new Venta(generadorIdSecuencial.siguiente());
        venta.agregarItem(new ItemVenta(tv, 1));

        comercio.agregarProducto(tv);
        comercio.agregarVenta(venta);

		assertEquals(PRECIO_BASE_EJEMPLO, venta.totalPrecioBase(), 1);
		assertEquals(IMPUESTOS_ELECTRONICO_UNIT, venta.totalImpuestos(), 1);
		assertEquals(FINAL_ELECTRONICO_UNIT, venta.totalFinal(), 1);
        assertEquals(1, observadorSffa.getCantLlamadas());
        assertEquals(1, observadorSvibaa.getCantLlamadas());
	}

	@Test
	void ventaUnProductoHogar_verificaIvaMasEi() {
		Producto cafetera = new Producto(null, tipoHogar, PRECIO_BASE_EJEMPLO, "Cafetera Express");

		Venta venta = new Venta(generadorIdSecuencial.siguiente());
        venta.agregarItem(new ItemVenta(cafetera, 1));

		assertEquals(PRECIO_BASE_EJEMPLO, venta.totalPrecioBase(), 1);
		assertEquals(IMPUESTOS_HOGAR_UNIT, venta.totalImpuestos(), 1);
		assertEquals(FINAL_HOGAR_UNIT, venta.totalFinal(), 1);
	}

	@Test
	void ventaDosProductosElectronicos_impuestosPorProductoYTotal() {
		Producto tv1 = new Producto(null, tipoElectronico, PRECIO_BASE_EJEMPLO, "Smart TV 50");
		Producto tv2 = new Producto(null, tipoElectronico, PRECIO_BASE_EJEMPLO, "Smart TV 50");
		assertEquals(IMPUESTOS_ELECTRONICO_UNIT, tv1.totalImpuestos(), 1);
		assertEquals(IMPUESTOS_ELECTRONICO_UNIT, tv2.totalImpuestos(), 1);

		Venta venta = new Venta(generadorIdSecuencial.siguiente());
        venta.agregarItem(new ItemVenta(tv1, 1));
        venta.agregarItem(new ItemVenta(tv2, 1));

		assertEquals(200, venta.totalPrecioBase(), 1);
		assertEquals(2 * IMPUESTOS_ELECTRONICO_UNIT, venta.totalImpuestos(), 1);
		assertEquals(2 * FINAL_ELECTRONICO_UNIT, venta.totalFinal(), 1);
	}

	@Test
	void ventaMixtaElectronicoYHogar_aplicaImpuestosCorrespondientes() {
		Producto tv = new Producto(null, tipoElectronico, PRECIO_BASE_EJEMPLO, "Smart TV 50");
		Producto cafetera = new Producto(null, tipoHogar, PRECIO_BASE_EJEMPLO, "Cafetera Express");

		Venta venta = new Venta(generadorIdSecuencial.siguiente());
        venta.agregarItem(new ItemVenta(cafetera, 1));
        venta.agregarItem(new ItemVenta(tv, 1));

		assertEquals(200, venta.totalPrecioBase(), 1);
		assertEquals(IMPUESTOS_ELECTRONICO_UNIT + IMPUESTOS_HOGAR_UNIT, venta.totalImpuestos(), 1);
		assertEquals(FINAL_ELECTRONICO_UNIT + FINAL_HOGAR_UNIT, venta.totalFinal(), 1);
	}

}
```

---

**FIN DEL SNAPSHOT — repo clase 03 (SmartLife · Sales Service)**
