# FUENTE-REPO — rest-paises

> **Repositorio:** `rest-paises` (carpeta raíz del zip: `rest-paises-main`)
> **Origen:** repo compartido por la cátedra tras la **clase 2** (01/04/2026) — DDSI, UTN FRBA. Ejemplo de consumo de API REST desarrollado en vivo durante la Parte II de la clase.
> **Fecha del snapshot:** 13 de julio de 2026
> **Commit / versión:** no identificable — el zip no incluye directorio `.git`. El sufijo `-main` de la carpeta raíz indica descarga del branch `main` desde GitHub.
> **Versión del artefacto (`pom.xml`):** `0.0.1-SNAPSHOT`

---

## Notas del snapshot

1. **Archivos no transcriptos verbatim (boilerplate generado, sin contenido propio de la cátedra).** Están listados en el árbol y se declaran acá para que el inventario quede completo:
   - `LICENSE` (201 líneas) — texto íntegro de la **Apache License 2.0**, sin modificaciones ni titular agregado.
   - `mvnw` (295 líneas) y `mvnw.cmd` (189 líneas) — scripts del **Maven Wrapper**, generados por Spring Initializr. Idénticos a los de cualquier proyecto Spring Boot.
   - `HELP.md` (26 líneas) — archivo de ayuda autogenerado por Spring Initializr: enlaces a documentación de Maven, Spring Boot Maven Plugin, Spring Web, Configuration Processor y DevTools, más una nota sobre los overrides vacíos de `<license>` y `<developers>` en el POM.

   Todos los demás archivos del repo se transcriben completos y verbatim.

2. **Falta el directorio `.mvn/`.** El zip trae los scripts `mvnw` y `mvnw.cmd`, pero no la carpeta `.mvn/wrapper/` con `maven-wrapper.properties`. Sin ella, el wrapper no puede resolver la versión de Maven a descargar. Se declara tal cual está en el zip; no se infiere el motivo.

3. **El repo no incluye `.idea/` ni `target/`** — coherente con el `.gitignore`, que los excluye explícitamente.

4. **Sin directorio `.git`:** no hay historial, ramas ni hash de commit disponibles.

---

## Árbol de estructura

```
rest-paises-main/
├── .gitignore
├── HELP.md
├── LICENSE
├── README.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── ar/edu/utn/ba/ddsi/countries/
    │   │       ├── CountriesApplication.java
    │   │       ├── config/
    │   │       │   ├── RestCountriesProperties.java
    │   │       │   └── RestTemplateConfig.java
    │   │       └── services/
    │   │           ├── BuscadorDePaises.java
    │   │           └── dto/
    │   │               ├── DetalleMoneda.java
    │   │               ├── NombrePais.java
    │   │               └── Pais.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/
            └── ar/edu/utn/ba/ddsi/countries/
                └── BuscadorDePaisesIT.java
```

**Total:** 15 archivos. 7 clases Java (6 de producción + 1 de test), 2 archivos de configuración del proyecto (`pom.xml`, `application.yml`), 1 `.gitignore`, 1 README, y 4 de boilerplate (LICENSE, HELP.md, mvnw, mvnw.cmd).

---

## Configuración

### `pom.xml`

> Define el proyecto: parent Spring Boot 4.0.5, Java 21, cuatro dependencias, y la configuración de `annotationProcessorPaths` para Lombok y el configuration-processor.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.0.5</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>ar.edu.utn.ba.ddsi</groupId>
	<artifactId>countries</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name/>
	<description/>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>21</java.version>
	</properties>
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
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
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
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<executions>
					<execution>
						<id>default-compile</id>
						<phase>compile</phase>
						<goals>
							<goal>compile</goal>
						</goals>
						<configuration>
							<annotationProcessorPaths>
								<path>
									<groupId>org.springframework.boot</groupId>
									<artifactId>spring-boot-configuration-processor</artifactId>
								</path>
								<path>
									<groupId>org.projectlombok</groupId>
									<artifactId>lombok</artifactId>
								</path>
							</annotationProcessorPaths>
						</configuration>
					</execution>
					<execution>
						<id>default-testCompile</id>
						<phase>test-compile</phase>
						<goals>
							<goal>testCompile</goal>
						</goals>
						<configuration>
							<annotationProcessorPaths>
								<path>
									<groupId>org.projectlombok</groupId>
									<artifactId>lombok</artifactId>
								</path>
							</annotationProcessorPaths>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

</project>
```

---

### `src/main/resources/application.yml`

> Único archivo de configuración de la aplicación. Declara la URL base de la API externa bajo el prefijo `rest-countries`.

```yaml
rest-countries:
    base-url: https://restcountries.com/v3.1
```

---

### `.gitignore`

> Plantilla estándar de Java, con dos agregados al final: `.idea/` y `/target/`.

```gitignore
# Compiled class file
*.class

# Log file
*.log

# BlueJ files
*.ctxt

# Mobile Tools for Java (J2ME)
.mtj.tmp/

# Package Files #
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# virtual machine crash logs, see http://www.java.com/en/download/help/error_hotspot.xml
hs_err_pid*
replay_pid*

.idea/
/target/
```

*(El archivo original no termina en salto de línea: `/target/` es su última línea, sin newline final.)*

---

### `README.md`

> Documentación escrita por la cátedra. Declara el objetivo, el alcance de la demo y la tabla de tecnologías. Se transcribe completo y verbatim.

```markdown
# rest-paises

Ejemplo educativo de **consumo de una API REST** pública: [REST Countries](https://restcountries.com/). Los datos se obtienen de la versión documentada del sitio (por ejemplo `v3.1`), que expone información sobre países vía HTTP.

## Objetivo

Centralizar en un componente **`BuscadorDePaises`** una interfaz de uso en código para consultar países según distintos criterios alineados con la API:

- listado de todos los países (con filtro de campos requerido por el servicio),
- búsqueda por nombre,
- búsqueda por código de moneda,
- búsqueda por región,
- búsqueda por capital.

La URL base y el prefijo de configuración se parametrizan con propiedades Spring (`rest-countries.*`).

## Alcance de la demo

Este proyecto **no** incluye controladores REST propios ni interfaz de usuario: **solo pretende mostrar el uso del buscador mediante pruebas automatizadas** en `src/test`. Allí se levanta el contexto de Spring y se ejecutan las consultas contra el servicio externo (comportamiento típico de test de integración con API real).

## Tecnologías

| Tecnología | Uso |
|------------|-----|
| **Java 21** | Lenguaje y runtime |
| **Spring Boot** (Web MVC) | Aplicación, inyección de dependencias y cliente HTTP |
| **`RestTemplate`** | Llamadas GET y deserialización a DTOs |
| **`UriComponentsBuilder`** | Construcción de URIs con query params (p. ej. `fields=…`) |
| **Lombok** | Reducción de boilerplate en DTOs y propiedades (`@Data`, etc.) |
| **`@ConfigurationProperties`** | Configuración tipada (`RestCountriesProperties`) |
| **JUnit 5** | Tests |
| **Spring Boot Test** (`@SpringBootTest`) | Contexto Spring en tests |
| **AssertJ** | Aserciones legibles en tests |

## Consideraciones

Las pruebas dependen de **conectividad** hacia la API de REST Countries.
```

---

## Código fuente — `src/main`

### `ar/edu/utn/ba/ddsi/countries/CountriesApplication.java`

> Clase de arranque generada por Spring Initializr, con un agregado manual: `@EnableConfigurationProperties`.

```java
package ar.edu.utn.ba.ddsi.countries;

import ar.edu.utn.ba.ddsi.countries.config.RestCountriesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RestCountriesProperties.class)
public class CountriesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CountriesApplication.class, args);
	}

}
```

---

### `ar/edu/utn/ba/ddsi/countries/config/RestCountriesProperties.java`

> Clase de configuración tipada. Mapea el prefijo `rest-countries` del `application.yml` a atributos Java.

```java
package ar.edu.utn.ba.ddsi.countries.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rest-countries")
@Data
public class RestCountriesProperties {

    private String baseUrl;
}
```

---

### `ar/edu/utn/ba/ddsi/countries/config/RestTemplateConfig.java`

> Clase de configuración que expone una instancia de `RestTemplate`.

```java
package ar.edu.utn.ba.ddsi.countries.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

### `ar/edu/utn/ba/ddsi/countries/services/BuscadorDePaises.java`

> Componente central del repo. Expone cinco métodos de búsqueda contra la API de REST Countries. Recibe sus dos dependencias por constructor.

```java
package ar.edu.utn.ba.ddsi.countries.services;

import ar.edu.utn.ba.ddsi.countries.config.RestCountriesProperties;
import ar.edu.utn.ba.ddsi.countries.services.dto.Pais;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class BuscadorDePaises {

    private static final String CAMPOS =
            "name,capital,region,subregion,population,currencies,languages,area,cca2,cca3";

    private final RestTemplate restTemplate;
    private final RestCountriesProperties propiedades;

    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }

    public List<Pais> buscarTodos() {
        URI uri =
                UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                        .path("/all")
                        .queryParam("fields", CAMPOS)
                        .build()
                        .toUri();
        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
    }

    public Optional<Pais> buscarPorNombre(String nombre) {
        URI uri =
                UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                        .path("/name/{nombre}")
                        .queryParam("fields", CAMPOS)
                        .buildAndExpand(nombre)
                        .toUri();
        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        if (cuerpo == null || cuerpo.length == 0) {
            return Optional.empty();
        }
        return Optional.of(cuerpo[0]);
    }

    public List<Pais> buscarPorMoneda(String codigoMoneda) {
        URI uri =
                UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                        .path("/currency/{codigo}")
                        .queryParam("fields", CAMPOS)
                        .buildAndExpand(codigoMoneda)
                        .toUri();
        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
    }

    public List<Pais> buscarPorRegion(String region) {
        URI uri =
                UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                        .path("/region/{region}")
                        .queryParam("fields", CAMPOS)
                        .buildAndExpand(region)
                        .toUri();
        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
    }

    public List<Pais> buscarPorCapital(String capital) {
        URI uri =
                UriComponentsBuilder.fromUriString(propiedades.getBaseUrl())
                        .path("/capital/{capital}")
                        .queryParam("fields", CAMPOS)
                        .buildAndExpand(capital)
                        .toUri();
        Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
        return cuerpo == null || cuerpo.length == 0 ? List.of() : Arrays.asList(cuerpo);
    }
}
```

---

### `ar/edu/utn/ba/ddsi/countries/services/dto/Pais.java`

> DTO principal. Mapea la respuesta JSON de la API a un objeto Java. Diez atributos, dos de ellos anidados (`NombrePais` y `Map<String, DetalleMoneda>`).

```java
package ar.edu.utn.ba.ddsi.countries.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pais {

    @JsonProperty("name")
    private NombrePais nombre;

    @JsonProperty("capital")
    private List<String> capitales;

    @JsonProperty("region")
    private String region;

    @JsonProperty("subregion")
    private String subregion;

    @JsonProperty("area")
    private Double superficie;

    @JsonProperty("population")
    private Long poblacion;

    @JsonProperty("currencies")
    private Map<String, DetalleMoneda> monedas;

    @JsonProperty("languages")
    private Map<String, String> idiomas;

    @JsonProperty("cca2")
    private String cca2;

    @JsonProperty("cca3")
    private String cca3;
}
```

---

### `ar/edu/utn/ba/ddsi/countries/services/dto/NombrePais.java`

> DTO anidado. Mapea el objeto `name` del JSON, que tiene dos formas del nombre del país.

```java
package ar.edu.utn.ba.ddsi.countries.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NombrePais {

    @JsonProperty("common")
    private String comun;

    @JsonProperty("official")
    private String oficial;
}
```

---

### `ar/edu/utn/ba/ddsi/countries/services/dto/DetalleMoneda.java`

> DTO anidado. Mapea el valor de cada entrada del objeto `currencies` del JSON.

```java
package ar.edu.utn.ba.ddsi.countries.services.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleMoneda {

    @JsonProperty("name")
    private String nombre;

    @JsonProperty("symbol")
    private String simbolo;
}
```

---

## Código fuente — `src/test`

### `ar/edu/utn/ba/ddsi/countries/BuscadorDePaisesIT.java`

> Única clase de test del repo. Cinco tests, uno por cada método de `BuscadorDePaises`. Levanta el contexto de Spring (`@SpringBootTest`) y ejecuta contra la API real. Aserciones con AssertJ.

```java
package ar.edu.utn.ba.ddsi.countries;

import ar.edu.utn.ba.ddsi.countries.services.BuscadorDePaises;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BuscadorDePaisesIT {

    @Autowired
    private BuscadorDePaises buscadorDePaises;

    @Test
    void buscarTodosDevuelveVariosPaises() {
        var lista = buscadorDePaises.buscarTodos();
        assertThat(lista).isNotEmpty();
        assertThat(lista.getFirst().getNombre().getComun()).isNotBlank();
    }

    @Test
    void buscarPorNombrePeruDevuelvePeru() {
        var opt = buscadorDePaises.buscarPorNombre("peru");
        assertThat(opt).isPresent();
        assertThat(opt.get().getNombre().getComun()).isEqualTo("Peru");
        assertThat(opt.get().getCca2()).isEqualTo("PE");
    }

    @Test
    void buscarPorMonedaARSincluyeArgentina() {
        var lista = buscadorDePaises.buscarPorMoneda("ars");
        assertThat(lista).extracting(p -> p.getNombre().getComun()).contains("Argentina");
    }

    @Test
    void buscarPorRegionEuropeNoVacia() {
        var lista = buscadorDePaises.buscarPorRegion("europe");
        assertThat(lista).isNotEmpty();
        assertThat(lista).allMatch(p -> "Europe".equals(p.getRegion()));
    }

    @Test
    void buscarPorCapitalBuenosAiresDevuelveArgentina() {
        var lista = buscadorDePaises.buscarPorCapital("buenos aires");
        assertThat(lista).isNotEmpty();
        assertThat(lista.getFirst().getCca2()).isEqualTo("AR");
    }
}
```

---

**FIN DEL ARCHIVO FUENTE-REPO — rest-paises (clase 2)**
