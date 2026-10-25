# FUENTE-REPO — ejemplo-mockeo

> **Repo:** `ejemplo-mockeo` · github.com/dds-utn/ejemplo-mockeo (branch `main`)
> **Commit:** 5334a11eaff5a16ec41769606e298541054df08a
> **Fecha del snapshot:** 21/07/2026 · **Fecha del repo (último commit):** 20/04/2023
> **Origen:** material previo obligatorio de la **clase 4** (preclase 04), tema *Mocking*. Es la "Implementación Mocking" que enlaza el cronograma de la cátedra.
>
> **Notas del snapshot:**
> - En `pom.xml`, el elemento del nombre del proyecto aparece como `<n>mock-example</n>` (se esperaría `<name>`). Se transcribe **tal cual**; es un artefacto del original, no un error de transcripción.
> - Proyecto Maven plano (`packaging jar`), sin Spring Boot. Versiones 2023 (Java 8, JUnit Jupiter 5.9.2, Mockito 5.3.0, Lombok 1.18.26).

## Árbol

```
ejemplo-mockeo-main/
├── .gitignore
├── README.md
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── cuidandonos/
    │           ├── Ubicacion.java
    │           ├── Viaje.java
    │           ├── demora/
    │           │   └── CalculadorDeDemora.java
    │           └── distancia/
    │               └── CalculadorDeDistancia.java
    └── test/
        └── java/
            └── tests/
                └── ViajeTest.java
```

---

## `.gitignore`

```
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

.idea
```

## `README.md`

```markdown
### Ejemplo de Mockeo

Para este ejemplo se utilizó [Mockito](https://site.mockito.org/ "Mockito") como Framework de Mockeo de Objetos. 
La propuesta de solución corresponde al requerimiento n°4 del final de [Cuidándonos (27/2/2019)](https://bit.ly/dds-utnba-final-cuidandonos "Cuidándonos (27/2/2019)") de la Cátedra de Diseño de Sistemas.
```

## `pom.xml`

*Define el proyecto: dependencias de test (JUnit Jupiter + Mockito) y Lombok en scope `provided`; compilador Java 1.8.*

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>mock-example</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>

  <n>mock-example</n>
  <url>http://maven.apache.org</url>

  <build>
    <plugins>
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.1</version>
        <configuration>
          <source>1.8</source>
          <target>1.8</target>
        </configuration>
      </plugin>
    </plugins>
  </build>

  <dependencies>
    <!-- https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
      <version>5.9.2</version>
      <scope>test</scope>
    </dependency>

    <!-- https://mvnrepository.com/artifact/org.mockito/mockito-core -->
    <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-core</artifactId>
      <version>5.3.0</version>
      <scope>test</scope>
    </dependency>

    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.26</version>
      <scope>provided</scope>
    </dependency>

  </dependencies>

</project>
```

---

## `src/main/java/cuidandonos/Ubicacion.java`

*Objeto de valor con `latitud`, `longitud` y `referencia`. Setters/getters generados por Lombok.*

```java
package cuidandonos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Ubicacion {
    private float latitud;
    private float longitud;
    private String referencia;

    public Ubicacion(float latitud, float longitud, String referencia) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.referencia = referencia;
    }
}
```

## `src/main/java/cuidandonos/Viaje.java`

*Clase bajo test. `calcularDemoraAproximadaEnMins` recibe los dos calculadores por parámetro y guarda el resultado en el atributo `demoraAproximadaEnMins`.*

```java
package cuidandonos;

import cuidandonos.demora.CalculadorDeDemora;
import cuidandonos.distancia.CalculadorDeDistancia;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Viaje {
    private Ubicacion puntoDePartida;
    private Ubicacion destino;
    private double demoraAproximadaEnMins;

    public void calcularDemoraAproximadaEnMins(CalculadorDeDistancia calculadorDeDistancia, CalculadorDeDemora calculadorDeDemora) {
        float distanciaEnMetros = calculadorDeDistancia.distanciaEnMetrosEntre(this.puntoDePartida, this.destino);
        this.demoraAproximadaEnMins = calculadorDeDemora.demoraAproximadaEnMinsParaRecorrer(distanciaEnMetros);
    }
}
```

## `src/main/java/cuidandonos/demora/CalculadorDeDemora.java`

*Interfaz. Un método: dado un valor en metros, devuelve la demora aproximada en minutos (`double`). Sin implementación en el repo.*

```java
package cuidandonos.demora;

public interface CalculadorDeDemora {
    public double demoraAproximadaEnMinsParaRecorrer(float unosMetros);
}
```

## `src/main/java/cuidandonos/distancia/CalculadorDeDistancia.java`

*Interfaz. Un método: distancia en metros (`float`) entre dos `Ubicacion`. Sin implementación en el repo (en el dominio real la resolvería una API externa).*

```java
package cuidandonos.distancia;

import cuidandonos.Ubicacion;

public interface CalculadorDeDistancia {
    public float distanciaEnMetrosEntre(Ubicacion unaUbicacion, Ubicacion otraUbicacion);
}
```

## `src/test/java/tests/ViajeTest.java`

*Test JUnit 5. Crea mocks de las dos interfaces con `mock(...)`, les programa la respuesta con `when(...).thenReturn(...)`, ejecuta `calcularDemoraAproximadaEnMins` y verifica el resultado con `assertEquals`.*

```java
package tests;

import cuidandonos.Ubicacion;
import cuidandonos.Viaje;
import cuidandonos.demora.CalculadorDeDemora;
import cuidandonos.distancia.CalculadorDeDistancia;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ViajeTest {

    @Test
    public void demoraDe10MinsEnViaje() {
        Ubicacion medrano = new Ubicacion(-34.598450F, -58.420065F, "UTN BA Medrano");
        Ubicacion campus = new Ubicacion(-34.659277F, -58.4673392F, "UTN BA Campus");

        Viaje viajeDeSedeASede = new Viaje();
        viajeDeSedeASede.setPuntoDePartida(medrano);
        viajeDeSedeASede.setDestino(campus);

        CalculadorDeDistancia calculadorDeDistancia = mock(CalculadorDeDistancia.class);
        when(calculadorDeDistancia.distanciaEnMetrosEntre(medrano, campus)).thenReturn(10100F);

        CalculadorDeDemora calculadorDeDemora = mock(CalculadorDeDemora.class);
        when(calculadorDeDemora.demoraAproximadaEnMinsParaRecorrer(10100F)).thenReturn(30.0);

        viajeDeSedeASede.calcularDemoraAproximadaEnMins(calculadorDeDistancia, calculadorDeDemora);

        Assertions.assertEquals(30.0, viajeDeSedeASede.getDemoraAproximadaEnMins());
    }
}
```

---

**FIN DEL FUENTE-REPO — ejemplo-mockeo**
