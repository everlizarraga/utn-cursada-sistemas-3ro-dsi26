# FUENTE REPO — vuelos-aeropuertos

**Repo:** `vuelos-aeropuertos` (zip: `vuelos-aeropuertos-main.zip`, rama `main`)
**Fecha del snapshot:** 13 de julio de 2026
**Commit / versión:** no identificable — el zip es una descarga de código fuente y no incluye metadata de Git (`.git/`).
**Origen:** repositorio entregado por la cátedra en el material previo de la **clase 2** de DDSI (video "Parte 3 — Práctica Java", ejercicio "Vuelos y Aeropuertos"). Es la **solución completa** del ejercicio: el video implementa solo una parte y deja el resto en `// TODO`.

**Alcance del snapshot:** repo chico → **todo el código, verbatim**, archivo por archivo. Configs completas. Las notas de índice (1-3 líneas antes de cada archivo) son indexación, no reescritura: **el código no está modificado ni comentado por dentro**.

**Nota de verificación:** el código fue leído íntegramente, pero **no se compiló** (el entorno de conversión tenía JRE sin JDK). No se declara aquí si compila o no.

---

## 1. Árbol de estructura

```
vuelos-aeropuertos-main/
├── .gitignore
├── pom.xml
├── readme.md
└── src/
    ├── main/
    │   └── java/
    │       └── domain/
    │           ├── lugares/
    │           │   ├── Aeropuerto.java
    │           │   ├── Ciudad.java
    │           │   ├── Continente.java
    │           │   └── Pais.java
    │           ├── personas/
    │           │   ├── Cargo.java
    │           │   ├── Empleado.java
    │           │   ├── Pasajero.java
    │           │   ├── Persona.java
    │           │   ├── TipoDeDocumento.java
    │           │   └── Tripulacion.java
    │           └── viajes/
    │               ├── Aerolinea.java
    │               ├── Avion.java
    │               ├── Escala.java
    │               ├── Viaje.java
    │               └── Vuelo.java
    └── test/
        └── java/
            └── domain/
                ├── BaseTest.java
                ├── lugares/
                │   └── CiudadTest.java
                └── viajes/
                    ├── ViajeTest.java
                    └── VueloTest.java
```

**Totales:** 17 archivos `.java` (13 de dominio + 4 de test), 874 líneas de código Java, más `pom.xml`, `readme.md` y `.gitignore`.

**Archivos referenciados pero ausentes del zip:** el `readme.md` embebe dos imágenes, `Enunciado.PNG` y `diagrama_de_clases_propuesto.PNG`, que **no están incluidas** en el paquete descargado.

---

## 2. Archivos de configuración (completos y verbatim)

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>vuelos_y_aeropuertos</artifactId>
    <version>1.0-SNAPSHOT</version>

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
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
    </dependencies>

</project>
```

### `readme.md`

```markdown
![Enunciado](Enunciado.PNG)

![Diagrama de clases](diagrama_de_clases_propuesto.PNG)
```

### `.gitignore`

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
```

---

## 3. Código de dominio — `src/main/java/domain/`

### 3.1 Package `domain.lugares`

---

**`src/main/java/domain/lugares/Continente.java`**
*Enum de 5 constantes. Sin atributos ni métodos.*

```java
package domain.lugares;

public enum Continente {
    AMERICA,
    ASIA,
    EUROPA,
    AFRICA,
    OCEANIA
}
```

---

**`src/main/java/domain/lugares/Pais.java`**
*Nombre, continente y colección de ciudades. Expone `agregarCiudades` con parámetros variables. No implementa el método `nombre()` que figura en el diagrama de clases (usa `getNombre()`).*

```java
package domain.lugares;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pais {
    private String nombre;
    private Continente continente;
    private List<Ciudad> ciudades;

    public Pais(String nombre, Continente continente) {
        this.ciudades = new ArrayList<>();
        this.nombre = nombre;
        this.continente = continente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Continente getContinente() {
        return continente;
    }

    public void setContinente(Continente continente) {
        this.continente = continente;
    }

    public void agregarCiudades(Ciudad ... ciudades) {
        Collections.addAll(this.ciudades, ciudades);
    }
}
```

---

**`src/main/java/domain/lugares/Ciudad.java`**
*Nombre, país y colección de aeropuertos. El constructor registra la ciudad en su país. Resuelve dos requerimientos del enunciado: cantidad de aeropuertos (6) y cantidad de pasajeros que llegaron un día (7 parcial), este último con `flatMap` + `mapToInt().sum()`.*

```java
package domain.lugares;

import domain.viajes.Vuelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Ciudad {
    private String nombre;
    private Pais pais;
    private List<Aeropuerto> aeropuertos;

    public Ciudad(String nombre, Pais pais) {
        this.aeropuertos = new ArrayList<>();
        this.pais = pais;
        this.nombre = nombre;
        pais.agregarCiudades(this);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Pais getPais() {
        return pais;
    }

    public void agregarAeropuertos(Aeropuerto ... aeropuertos) {
        Collections.addAll(this.aeropuertos, aeropuertos);
    }

    public Integer cantDeAeropuertos() {
        return this.aeropuertos.size();
    }

    public Integer cantPasajerosQueLlegaronElDia(LocalDate dia) {
        List<Vuelo> vuelosQueLlegaronEseDia = this.aeropuertos
                .stream()
                .flatMap(a -> a.vuelosQueLlegaronElDia(dia).stream())
                .collect(Collectors.toList());

        return vuelosQueLlegaronEseDia
                .stream()
                .mapToInt(v -> v.cantPasajeros())
                .sum();
    }
}
```

---

**`src/main/java/domain/lugares/Aeropuerto.java`**
*Nombre, código internacional, ciudad y colección de vuelos. El constructor registra el aeropuerto en su ciudad. Resuelve el requerimiento 3 (vuelos que partieron / llegaron un día) y expone `estasEn(Ciudad)`, usado por `Vuelo.tuDestinoEs`.*

```java
package domain.lugares;

import domain.viajes.Vuelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Aeropuerto {
    private String nombre;
    private String codigoInternacional;
    private Ciudad ciudad;
    private List<Vuelo> vuelos;

    public Aeropuerto(String nombre, String codigoInternacional, Ciudad ciudad) {
        this.ciudad = ciudad;
        this.nombre = nombre;
        this.codigoInternacional = codigoInternacional;
        this.vuelos = new ArrayList<>();
        ciudad.agregarAeropuertos(this);
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public Ciudad getCiudad() {
        return ciudad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoInternacional() {
        return codigoInternacional;
    }

    public void agregarVuelos(Vuelo ... vuelos) {
        Collections.addAll(this.vuelos, vuelos);
    }

    public Integer cantVuelosQuePartieronElDia(LocalDate dia) {
        return (int) this.vuelos
                .stream()
                .filter(v -> v.getFecha().toLocalDate().isEqual(dia))
                .count();
    }

    public List<Vuelo> vuelosQueLlegaronElDia(LocalDate dia) {
        return this.vuelos
                .stream()
                .filter(v -> v.fechaDeLlegadaAproximada().toLocalDate().isEqual(dia))
                .collect(Collectors.toList());
    }

    public Integer cantVuelosQueLlegaronElDia(LocalDate dia) {
        return this.vuelosQueLlegaronElDia(dia).size();
    }

    public boolean estasEn(Ciudad unaCiudad) {
        return this.ciudad.equals(unaCiudad);
    }
}
```

### 3.2 Package `domain.personas`

---

**`src/main/java/domain/personas/TipoDeDocumento.java`**
*Enum de 4 constantes. En el diagrama de clases figura como `TipoDocumento` (sin el "De").*

```java
package domain.personas;

public enum TipoDeDocumento {
    DNI,
    LIBRETA_CIVICA,
    LIBRETA_ENROLAMIENTO,
    CEDULA
}
```

---

**`src/main/java/domain/personas/Cargo.java`**
*Enum de 4 constantes, para los roles de la tripulación.*

```java
package domain.personas;

public enum Cargo {
    PILOTO,
    AZAFATA,
    COMISARIO,
    OPERADOR_COMUNICACIONES
}
```

---

**`src/main/java/domain/personas/Persona.java`**
*Clase abstracta, superclase de `Empleado` y `Pasajero`. Sus cuatro atributos son `private` con getters y setters públicos (en el diagrama de clases figuran como `#` protegidos).*

```java
package domain.personas;

public abstract class Persona {
    private String nombre;
    private String apellido;
    private Integer nroDeDocumento;
    private TipoDeDocumento tipoDeDocumento;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Integer getNroDeDocumento() {
        return nroDeDocumento;
    }

    public void setNroDeDocumento(Integer nroDeDocumento) {
        this.nroDeDocumento = nroDeDocumento;
    }

    public TipoDeDocumento getTipoDeDocumento() {
        return tipoDeDocumento;
    }

    public void setTipoDeDocumento(TipoDeDocumento tipoDeDocumento) {
        this.tipoDeDocumento = tipoDeDocumento;
    }
}
```

---

**`src/main/java/domain/personas/Empleado.java`**
*Hereda de `Persona`. Agrega cargo y empleador. Sin constructor propio ni métodos de negocio.*

```java
package domain.personas;

import domain.viajes.Aerolinea;

public class Empleado extends Persona {
    private Cargo cargo;
    private Aerolinea empleador;

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Aerolinea getEmpleador() {
        return empleador;
    }

    public void setEmpleador(Aerolinea empleador) {
        this.empleador = empleador;
    }
}
```

---

**`src/main/java/domain/personas/Pasajero.java`**
*Hereda de `Persona`. Su constructor usa `super.setNombre/setApellido` porque los atributos del padre son privados. Resuelve los requerimientos 5 (vuelos totales) y 8 (veces que visitó una ciudad).*

```java
package domain.personas;

import domain.lugares.Ciudad;
import domain.lugares.Pais;
import domain.viajes.Vuelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pasajero extends Persona {
    private List<Vuelo> vuelos;
    private Integer nroDePasaporte;
    private Pais nacionalidad;

    public Pasajero(String nombre, String apellido) {
        super.setNombre(nombre);
        super.setApellido(apellido);
        this.vuelos = new ArrayList<>();
    }

    public Integer getNroDePasaporte() {
        return nroDePasaporte;
    }

    public void setNroDePasaporte(Integer nroDePasaporte) {
        this.nroDePasaporte = nroDePasaporte;
    }

    public Pais getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(Pais nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void agregarVuelos(Vuelo ... vuelos) {
        Collections.addAll(this.vuelos, vuelos);
    }

    public Integer cantVuelosTotales() {
        return this.vuelos.size();
    }

    public Integer cantVecesQueVisitaste(Ciudad unaCiudad) {
        return (int) this.vuelos
                .stream()
                .filter(v -> v.tuDestinoEs(unaCiudad))
                .count();
    }
}
```

---

**`src/main/java/domain/personas/Tripulacion.java`**
*Colecciones de empleados y de vuelos. Resuelve el requerimiento 9 con un método privado auxiliar `vuelosEntre` que filtra por rango de fechas inclusive. En el diagrama el método público figura como `cantVuelosEntre`.*

```java
package domain.personas;

import domain.viajes.Vuelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tripulacion {
    private String nombre;
    private List<Empleado> empleados;
    private List<Vuelo> vuelos;

    public Tripulacion() {
        this.empleados = new ArrayList<>();
        this.vuelos    = new ArrayList<>();
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Empleado> getEmpleados() {
        return empleados;
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void agregarEmpleados(Empleado ... empleados) {
        Collections.addAll(this.empleados, empleados);
    }

    public void agregarVuelos(Vuelo ... vuelos) {
        Collections.addAll(this.vuelos, vuelos);
    }

    private List<Vuelo> vuelosEntre(LocalDate fechaInicial, LocalDate fechaFinal) {
        return this.vuelos
                .stream()
                .filter(
                        v ->
                                (
                                        v.getFecha().toLocalDate().isAfter(fechaInicial)
                                                || v.getFecha().toLocalDate().isEqual(fechaInicial)
                                )
                                        && (
                                        v.getFecha().toLocalDate().isBefore(fechaFinal)
                                                || v.getFecha().toLocalDate().isEqual(fechaFinal)
                                )
                )
                .collect(Collectors.toList());
    }

    public Integer cantVuelosEnMesesEntre(LocalDate fechaInicial, LocalDate fechaFinal) {
        return this.vuelosEntre(fechaInicial, fechaFinal).size();
    }
}
```

### 3.3 Package `domain.viajes`

---

**`src/main/java/domain/viajes/Avion.java`**
*Solo cantidad de asientos, con constructor.*

```java
package domain.viajes;

public class Avion {
    private Integer cantAsientos;

    public Avion(Integer cantAsientos) {
        this.cantAsientos = cantAsientos;
    }

    public void setCantAsientos(Integer cantAsientos) {
        this.cantAsientos = cantAsientos;
    }

    public Integer getCantAsientos() {
        return cantAsientos;
    }
}
```

---

**`src/main/java/domain/viajes/Escala.java`**
*Aeropuerto y duración en minutos. Sin constructor: se puebla por setters.*

```java
package domain.viajes;

import domain.lugares.Aeropuerto;

public class Escala {
    private Aeropuerto aeropuerto;
    private Double duracionEnMins;

    public Aeropuerto getAeropuerto() {
        return aeropuerto;
    }

    public void setAeropuerto(Aeropuerto aeropuerto) {
        this.aeropuerto = aeropuerto;
    }

    public Double getDuracionEnMins() {
        return duracionEnMins;
    }

    public void setDuracionEnMins(Double duracionEnMins) {
        this.duracionEnMins = duracionEnMins;
    }
}
```

---

**`src/main/java/domain/viajes/Vuelo.java`**
*Clase central. `setDestino` y `setTripulacion` registran el vuelo en el otro extremo de la relación; `agregarPasajeros` no lo hace. Resuelve el requerimiento 1 (`capacidadRealOcupadaPorPasajeros`) y calcula la fecha de llegada sumando la duración a la fecha de salida.*

```java
package domain.viajes;

import domain.lugares.Aeropuerto;
import domain.lugares.Ciudad;
import domain.personas.Pasajero;
import domain.personas.Tripulacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Vuelo {
    private Aeropuerto origen;
    private Aeropuerto destino;
    private LocalDateTime fecha;
    private Double duracionEstimadaEnMins;
    private Integer cantAsientosOfrecidos;
    private Avion avion;
    private List<Pasajero> pasajeros;
    private Tripulacion tripulacion;

    public Vuelo() {
        this.pasajeros = new ArrayList<>();
    }

    public Aeropuerto getOrigen() {
        return origen;
    }

    public void setOrigen(Aeropuerto origen) {
        this.origen = origen;
    }

    public Aeropuerto getDestino() {
        return destino;
    }

    public void setDestino(Aeropuerto destino) {
        this.destino = destino;
        destino.agregarVuelos(this);
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getDuracionEstimadaEnMins() {
        return duracionEstimadaEnMins;
    }

    public void setDuracionEstimadaEnMins(Double duracionEstimadaEnMins) {
        this.duracionEstimadaEnMins = duracionEstimadaEnMins;
    }

    public Integer getCantAsientosOfrecidos() {
        return cantAsientosOfrecidos;
    }

    public void setCantAsientosOfrecidos(Integer cantAsientosOfrecidos) {
        this.cantAsientosOfrecidos = cantAsientosOfrecidos;
    }

    public Avion getAvion() {
        return avion;
    }

    public void setAvion(Avion avion) {
        this.avion = avion;
    }

    public List<Pasajero> getPasajeros() {
        return pasajeros;
    }

    public Tripulacion getTripulacion() {
        return tripulacion;
    }

    public void setTripulacion(Tripulacion tripulacion) {
        tripulacion.agregarVuelos(this);
        this.tripulacion = tripulacion;
    }

    public void agregarPasajeros(Pasajero ... pasajeros) {
        Collections.addAll(this.pasajeros, pasajeros);
    }

    public LocalDateTime fechaDeLlegadaAproximada() {
        return this.fecha.plusMinutes(this.duracionEstimadaEnMins.longValue());
    }

    public Double capacidadRealOcupadaPorPasajeros() {
        return (this.cantPasajeros() * 100.00) / this.avion.getCantAsientos();
    }

    public Integer cantPasajeros() {
        return this.pasajeros.size();
    }

    public boolean tuDestinoEs(Ciudad unaCiudad) {
        return this.destino.estasEn(unaCiudad);
    }
}
```

---

**`src/main/java/domain/viajes/Viaje.java`**
*Agrupa vuelos y escalas. Resuelve el requerimiento 2 (duración total con escalas) descomponiendo en dos métodos privados. En `cantPasajerosTotales` el repo conserva, comentada, una variante alternativa con `Collectors.toSet()`.*

```java
package domain.viajes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Viaje {
    private List<Vuelo> vuelos;
    private List<Escala> escalas;
    private LocalDateTime fechaSalida;
    private Aerolinea aerolinea;

    public Viaje() {
        this.vuelos = new ArrayList<>();
        this.escalas = new ArrayList<>();
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public Aerolinea getAerolinea() {
        return aerolinea;
    }

    public void setAerolinea(Aerolinea aerolinea) {
        this.aerolinea = aerolinea;
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public List<Escala> getEscalas() {
        return escalas;
    }

    public void agregarVuelos(Vuelo ... vuelos) {
        Collections.addAll(this.vuelos, vuelos);
    }

    public void agregarEscalas(Escala ... escalas) {
        Collections.addAll(this.escalas, escalas);
    }

    public Integer cantPasajerosTotales() {
        /*this.vuelos
                .stream()
                .flatMap(v -> v.getPasajeros().stream())
                .collect(Collectors.toSet())
                .size(); -> Forma sin contar pasajeros repetidos */
        return this.vuelos.stream().mapToInt(Vuelo::cantPasajeros).sum();
    }

    private Double duracionTotalDeVuelosEnMins() {
        return this.vuelos.stream().mapToDouble(v -> v.getDuracionEstimadaEnMins()).sum();
    }

    private Double duracionTotalDeEscalasEnMins() {
        return this.escalas.stream().mapToDouble(Escala::getDuracionEnMins).sum();
    }

    public Double duracionTotalEnMins() {
        return this.duracionTotalDeEscalasEnMins() + this.duracionTotalDeVuelosEnMins();
    }
}
```

---

**`src/main/java/domain/viajes/Aerolinea.java`**
*Colecciones de aviones y viajes. `agregarViaje` setea la aerolínea en cada viaje antes de agregarlo. Resuelve el requerimiento 10; a diferencia del diagrama, su método público recibe mes **y** año.*

```java
package domain.viajes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Aerolinea {
    private String nombre;
    private List<Avion> aviones;
    private List<Viaje> viajes;

    public Aerolinea() {
        this.aviones = new ArrayList<>();
        this.viajes  = new ArrayList<>();
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Avion> getAviones() {
        return aviones;
    }

    public List<Viaje> getViajes() {
        return viajes;
    }

    public void agregarViaje(Viaje ... viajes) {
        for (Viaje viaje: viajes) {
            viaje.setAerolinea(this);
        }
        Collections.addAll(this.viajes, viajes);
    }

    public void agregarAviones(Avion ... aviones) {
        Collections.addAll(this.aviones, aviones);
    }

    private List<Viaje> viajesDelAnioMes(Integer anio, Integer mes) {
        return this.viajes
                .stream()
                .filter(v ->
                        v.getFechaSalida().getYear() == anio
                                && v.getFechaSalida().getMonthValue() == mes
                        )
                .collect(Collectors.toList());
    }

    public Integer cantPasajerosTotales(Integer mes, Integer anio) {
        return this.viajesDelAnioMes(anio, mes)
                .stream()
                .mapToInt(Viaje::cantPasajerosTotales)
                .sum();
    }
}
```

---

## 4. Tests — `src/test/java/domain/`

Los tests usan **JUnit 4** (`org.junit.Test`, `org.junit.Before`, `org.junit.Assert`), coherente con la dependencia `junit:junit:4.12` del `pom.xml`.

---

**`src/test/java/domain/BaseTest.java`**
*Clase abstracta de la que heredan los tres tests. Arma el fixture completo en `@Before` (8 países, 9 ciudades, 9 aeropuertos, 4 aviones, 7 pasajeros) y expone buscadores protegidos basados en `Optional` + `findFirst`.*

```java
package domain;

import domain.lugares.Aeropuerto;
import domain.lugares.Ciudad;
import domain.lugares.Continente;
import domain.lugares.Pais;
import domain.personas.Pasajero;
import domain.viajes.Avion;
import org.junit.Before;

import java.util.*;

public abstract class BaseTest {
    protected List<Pasajero> pasajeros;
    protected List<Avion> aviones;
    protected List<Ciudad> ciudades;
    protected List<Pais> paises;
    protected List<Aeropuerto> aeropuertos;

    protected BaseTest() {
        this.pasajeros      = new ArrayList<>();
        this.aviones        = new ArrayList<>();
        this.ciudades       = new ArrayList<>();
        this.paises         = new ArrayList<>();
        this.aeropuertos    = new ArrayList<>();
    }

    @Before
    public void inicializar() {
        this.inicializarPaises();
        this.inicializarCiudades();
        this.inicializarAeropuertos();
        this.inicializarAviones();
        this.inicializarPasajeros();
    }

    private void inicializarPaises() {
        Pais[] paises = {
                new Pais("Argentina", Continente.AMERICA),
                new Pais("Alemania", Continente.EUROPA),
                new Pais("Brasil", Continente.AMERICA),
                new Pais("Chile", Continente.AMERICA),
                new Pais("España", Continente.EUROPA),
                new Pais("Estados Unidos", Continente.AMERICA),
                new Pais("India", Continente.ASIA),
                new Pais("Japón", Continente.ASIA),
        };
        Collections.addAll(this.paises, paises);
    }

    private void inicializarCiudades() {
        Ciudad[] ciudades = {
                new Ciudad("Ciudad de Buenos Aires", this.buscarPais("Argentina")),
                new Ciudad("Ezeiza", this.buscarPais("Argentina")),
                new Ciudad("Berlín", this.buscarPais("Alemania")),
                new Ciudad("Brasilia", this.buscarPais("Brasil")),
                new Ciudad("Santiago de Chile", this.buscarPais("Chile")),
                new Ciudad("Madrid", this.buscarPais("España")),
                new Ciudad("Washington D. C.", this.buscarPais("Estados Unidos")),
                new Ciudad("Nueva Delhi", this.buscarPais("India")),
                new Ciudad("Tokio", this.buscarPais("Japón"))
        };
        Collections.addAll(this.ciudades, ciudades);
    }

    private void inicializarAeropuertos() {
        Aeropuerto[] aeropuertos = {
                new Aeropuerto("Aeroparque Jorge Newbery", "AEP", this.buscarCiudad("Ciudad de Buenos Aires")),
                new Aeropuerto("Aeropuerto Internacional Ministro Pistarini", "EZE", this.buscarCiudad("Ezeiza")),
                new Aeropuerto("Brandeburgo Willy Brandt", "BER", this.buscarCiudad("Berlín")),
                new Aeropuerto("Presidente Juscelino Kubitschek", "BSB", this.buscarCiudad("Brasilia")),
                new Aeropuerto("Arturo Merino Benítez", "SCL", this.buscarCiudad("Santiago de Chile")),
                new Aeropuerto("Adolfo Suárez Madrid-Barajas", "MAD", this.buscarCiudad("Madrid")),
                new Aeropuerto("Washington-Dulles", "IAD", this.buscarCiudad("Washington D. C.")),
                new Aeropuerto("Indira Gandhi", "DEL", this.buscarCiudad("Nueva Delhi")),
                new Aeropuerto("Aeropuerto Internacional de Haneda", "HND", this.buscarCiudad("Tokio")),
        };
        Collections.addAll(this.aeropuertos, aeropuertos);
    }


    private void inicializarAviones() {
        Avion[] aviones = {
                new Avion(100),
                new Avion(40),
                new Avion(80),
                new Avion(120),
        };
        Collections.addAll(this.aviones, aviones);
    }

    private void inicializarPasajeros() {
        Pasajero[] pasajeros = {
                new Pasajero("Angie", "Rodriguez"),
                new Pasajero("Carlos", "Polanco"),
                new Pasajero("Daniela", "Acero"),
                new Pasajero("Esteban", "Ortega"),
                new Pasajero("Juan sebastian", "Romero"),
                new Pasajero("Laura", "Puerto"),
                new Pasajero("Jorge", "Marin"),
        };
        Collections.addAll(this.pasajeros, pasajeros);
    }

    protected Pais buscarPais(String nombre) {
        Optional<Pais> supuestoPais = this.paises.stream().filter(p -> p.getNombre().equals(nombre)).findFirst();
        return supuestoPais.orElse(null);
    }

    protected Ciudad buscarCiudad(String nombre) {
        Optional<Ciudad> supuestaCiudad = this.ciudades
                .stream()
                .filter(c -> c.getNombre().equals(nombre))
                .findFirst();
        return supuestaCiudad.orElse(null);
    }

    protected Avion buscarAvionPorcantAsientos(Integer cantAsientos) {
        Optional<Avion> supuestoAvion = this.aviones
                .stream()
                .filter(a -> a.getCantAsientos().equals(cantAsientos))
                .findFirst();
        return supuestoAvion.orElse(null);
    }

    protected Pasajero buscarPasajero(String nombre, String apellido) {
        Optional<Pasajero> supuestoPasajero = this.pasajeros
                .stream()
                .filter(p -> p.getNombre().equals(nombre) && p.getApellido().equals(apellido))
                .findFirst();
        return supuestoPasajero.orElse(null);
    }

    protected Aeropuerto buscarAeropuerto(String codInternacional) {
        Optional<Aeropuerto> supuestoAeropuerto = this.aeropuertos
                .stream()
                .filter(a -> a.getCodigoInternacional().equals(codInternacional))
                .findFirst();
        return supuestoAeropuerto.orElse(null);
    }
}
```

---

**`src/test/java/domain/lugares/CiudadTest.java`**
*Un test: 4 pasajeros llegan a Madrid el 20/05/2021 en un vuelo EZE→MAD de 780 minutos. Ejercita el camino `Ciudad → Aeropuerto → Vuelo → Pasajero`.*

```java
package domain.lugares;

import domain.BaseTest;
import domain.viajes.Vuelo;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CiudadTest extends BaseTest {

    @Test
    public void llegan4PasajerosAMadridEl20DeMayo() {
        Vuelo vueloEzeMad = new Vuelo();
        vueloEzeMad.setDuracionEstimadaEnMins(780.00);
        vueloEzeMad.setFecha(LocalDateTime.of(2021, 5, 20, 8, 35, 0));
        vueloEzeMad.setAvion(super.buscarAvionPorcantAsientos(40));
        vueloEzeMad.setOrigen(super.buscarAeropuerto("EZE"));
        vueloEzeMad.setDestino(super.buscarAeropuerto("MAD"));
        vueloEzeMad.agregarPasajeros(
                super.buscarPasajero("Angie", "Rodriguez"),
                super.buscarPasajero("Carlos", "Polanco"),
                super.buscarPasajero("Laura", "Puerto"),
                super.buscarPasajero("Jorge", "Marin")
        );
        Ciudad madrid = super.buscarCiudad("Madrid");
        Assert.assertEquals(4, madrid.cantPasajerosQueLlegaronElDia(LocalDate.of(2021,5,20)).intValue());
    }
}
```

---

**`src/test/java/domain/viajes/VueloTest.java`**
*Un test: 4 pasajeros en un avión de 40 asientos → 10% de capacidad ocupada. Usa delta `0.0` para comparar `Double`.*

```java
package domain.viajes;

import domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class VueloTest extends BaseTest {

    @Test
    public void vueloOcupadoAl10() {
        Vuelo vueloArgMad = new Vuelo();
        vueloArgMad.setAvion(super.buscarAvionPorcantAsientos(40));
        vueloArgMad.setOrigen(super.buscarAeropuerto("EZE"));
        vueloArgMad.setDestino(super.buscarAeropuerto("MAD"));
        vueloArgMad.agregarPasajeros(
                super.buscarPasajero("Angie", "Rodriguez"),
                super.buscarPasajero("Carlos", "Polanco"),
                super.buscarPasajero("Laura", "Puerto"),
                super.buscarPasajero("Jorge", "Marin")
                );
        Assert.assertEquals(10.00, vueloArgMad.capacidadRealOcupadaPorPasajeros(), 0.0);
    }
}
```

---

**`src/test/java/domain/viajes/ViajeTest.java`**
*Un test: 3 vuelos (60 + 45 + 75 min) y 2 escalas (60 + 30 min) → 270 minutos totales. No setea aeropuerto en las escalas.*

```java
package domain.viajes;

import domain.BaseTest;
import org.junit.Assert;
import org.junit.Test;

public class ViajeTest extends BaseTest {

    @Test
    public void viajeDura270Mins() {
        Vuelo vuelo1 = new Vuelo();
        vuelo1.setDuracionEstimadaEnMins(60.00);

        Escala escala1 = new Escala();
        escala1.setDuracionEnMins(60.00);

        Vuelo vuelo2 = new Vuelo();
        vuelo2.setDuracionEstimadaEnMins(45.00);

        Escala escala2 = new Escala();
        escala2.setDuracionEnMins(30.00);

        Vuelo vuelo3 = new Vuelo();
        vuelo3.setDuracionEstimadaEnMins(75.00);

        Viaje unViaje = new Viaje();
        unViaje.agregarVuelos(vuelo1, vuelo2, vuelo3);
        unViaje.agregarEscalas(escala1, escala2);

        Assert.assertEquals(270.00, unViaje.duracionTotalEnMins(), 0.0);
    }
}
```

---

## 5. Cobertura de los requerimientos del enunciado

Correspondencia entre los 10 requerimientos del enunciado y los métodos que existen en este repo. Es un mapeo de **presencia**, no una evaluación de corrección.

| # | Requerimiento | Método en el repo |
|---|---|---|
| 1 | Capacidad de un vuelo ocupada por pasajeros | `Vuelo.capacidadRealOcupadaPorPasajeros()` |
| 2 | Duración total aproximada de un vuelo (con escalas) | `Viaje.duracionTotalEnMins()` |
| 3 | Vuelos que partieron / llegaron a un aeropuerto en un día | `Aeropuerto.cantVuelosQuePartieronElDia()` · `Aeropuerto.cantVuelosQueLlegaronElDia()` |
| 4 | Aeropuerto que recibió menos vuelos en escalas | **sin método correspondiente** |
| 5 | Vuelos totales que realizó un pasajero | `Pasajero.cantVuelosTotales()` |
| 6 | Cantidad de aeropuertos que tiene una ciudad | `Ciudad.cantDeAeropuertos()` |
| 7 | Ciudad que más pasajeros recibió en un día | **sin método correspondiente** (existe `Ciudad.cantPasajerosQueLlegaronElDia()`, que calcula el dato de una ciudad, pero no hay nada que compare ciudades entre sí) |
| 8 | Veces que un pasajero visitó una ciudad | `Pasajero.cantVecesQueVisitaste(Ciudad)` |
| 9 | Vuelos que realizó una tripulación en un período | `Tripulacion.cantVuelosEnMesesEntre(LocalDate, LocalDate)` |
| 10 | Aerolínea con mayor cantidad de pasajeros en un mes | `Aerolinea.cantPasajerosTotales(Integer mes, Integer anio)` calcula el total de una aerolínea; no hay nada que compare aerolíneas entre sí |

---

**FIN DEL ARCHIVO FUENTE-REPO — vuelos-aeropuertos**
