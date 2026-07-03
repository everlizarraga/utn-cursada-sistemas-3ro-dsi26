# 🛠️ Proyecto 0 — Etapa 3: Lombok (matar el boilerplate)

> **Objetivo:** reemplazar todos los getters, setters, constructor y `toString` que tipeaste a mano en `Pais.java` por **4 annotations**. Entender qué hace Lombok por debajo.
>
> **Tiempo estimado:** 30-45 minutos.
>
> **Pre-requisito:** Etapas 1 y 2 completas y funcionando.

---

## 🎯 Conceptos que vas a tocar

- Qué es **Lombok** y por qué se usa en casi todos los proyectos Java modernos.
- **Annotation processors** — cómo Lombok modifica tu código en **tiempo de compilación**.
- Las annotations principales: `@Data`, `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- Cómo agregar una **dependencia** al `pom.xml` (tu primer Maven dependency).
- Mantener compatibilidad entre tu código manual y el código generado.

---

## 🧠 Qué es Lombok (mini-charla previa)

**Lombok** es una librería que **genera código por vos en tiempo de compilación**.

Vos escribís:
```java
@Data
public class Pais {
    private String nombre;
    private String capital;
}
```

Lombok, durante la compilación, **modifica el bytecode** (`.class`) como si vos hubieras escrito:
```java
public class Pais {
    private String nombre;
    private String capital;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCapital() { return capital; }
    public void setCapital(String capital) { this.capital = capital; }
    public String toString() { return "Pais(nombre=" + nombre + ", capital=" + capital + ")"; }
    public boolean equals(Object o) { /* lógica completa */ }
    public int hashCode() { /* lógica completa */ }
}
```

### Punto clave

**Tu archivo `.java` se queda corto y limpio.** Pero **el `.class` que genera el compilador tiene todo el código completo**. La JVM cuando ejecuta no ve ninguna magia — ve métodos como si vos los hubieras escrito.

Esto es importante porque:
- Cualquier framework (Jackson, Spring, JPA) ve los getters/setters reales y funciona como esperás.
- Si abrís el `.class` con un descompilador, vas a ver todo el código generado.
- **No hay overhead en runtime.** Lombok no existe cuando tu app está corriendo.

> Vas a ver Lombok en TODO el código del profe. `@Data`, `@AllArgsConstructor`, `@NoArgsConstructor` están en cada DTO de `rest-paises`. No es opcional saberlo.

---

## 📦 Paso 1: Agregar Lombok al `pom.xml`

Abrí `pom.xml`. Hasta ahora estaba sin dependencias. Agregá un bloque `<dependencies>` adentro de `<project>` (después de `<properties>`):

```xml
    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.34</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
```

### Qué dice cada parte

| Línea | Significa |
|---|---|
| `<dependency>` | "Necesito esta librería" |
| `<groupId>org.projectlombok</groupId>` | Quién la mantiene (la organización) |
| `<artifactId>lombok</artifactId>` | El nombre del proyecto dentro de esa organización |
| `<version>1.18.34</version>` | Versión específica que querés usar |
| `<scope>provided</scope>` | **Importante** — ver explicación abajo |

### Qué significa `<scope>provided</scope>`

Maven tiene varios "scopes" para una dependencia:

| Scope | Cuándo está disponible |
|---|---|
| `compile` (default) | En compilación Y en runtime. **Va dentro del .jar final.** |
| `provided` | En compilación, pero **NO en runtime**. No va en el .jar. |
| `test` | Solo en tests. |
| `runtime` | Solo en runtime, no en compilación. |

**Lombok es `provided`** porque solo lo necesitás cuando **compilás**. Una vez compilado, los getters/setters/etc están en el bytecode como código normal — Lombok no se ejecuta más. Por eso no tiene sentido empaquetarlo en el `.jar` final.

### Recargar Maven

Después de editar el `pom.xml`, IntelliJ te va a mostrar una notificación arriba a la derecha que dice **"Load Maven Changes"** (o similar). Hacele click. Va a descargar Lombok.

**Si no aparece la notificación:** click derecho sobre `pom.xml` → **Maven → Reload Project**.

Verificá en el panel **External Libraries** (abajo en el panel Project): debería aparecer `Maven: org.projectlombok:lombok:1.18.34`.

---

## 🔧 Paso 2: Verificar Annotation Processing (probablemente ya está OK)

Lombok necesita que IntelliJ tenga **annotation processing habilitado**. En versiones modernas (2022+) viene activado por defecto.

Para verificar:
- `Ctrl + Alt + S` → Settings.
- **Build, Execution, Deployment → Compiler → Annotation Processors**.
- Tildá **"Enable annotation processing"** si no lo está.
- OK.

> **Por qué Lombok necesita esto:** es un "annotation processor" — un plugin del compilador de Java que reacciona a las annotations `@Data`, `@Getter`, etc. y genera el código extra durante la compilación. IntelliJ tiene su propio compilador interno que necesita activar este modo para que funcione.

### Plugin de Lombok en IntelliJ

En IntelliJ 2020+ el plugin de Lombok viene **built-in** (no hace falta instalar nada).

Para verificar: **Settings → Plugins → Installed →** buscá "Lombok". Debería aparecer y estar habilitado. Si no, instalalo desde el **Marketplace**.

---

## ✂️ Paso 3: Transformar `Pais.java` con Lombok

Abrí `Pais.java`. Borrá **todo el contenido** menos `package`, atributos privados y la declaración de clase. Te tiene que quedar más o menos esto:

```java
package ar.edu.utn.ba.proyecto0.modelo;

public class Pais {
    private String nombre;
    private String capital;
    private String region;
    private long poblacion;
}
```

Sí, **borrá los getters, setters, constructor y toString**. Asusta, pero es deliberado.

Ahora agregá las annotations de Lombok arriba de la clase:

```java
package ar.edu.utn.ba.proyecto0.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pais {
    private String nombre;
    private String capital;
    private String region;
    private long poblacion;
}
```

Los imports los agrega IntelliJ solo cuando apretás `Alt + Enter` sobre cada annotation roja, o solos al tipear.

### Qué hace cada annotation

| Annotation | Genera... |
|---|---|
| `@Data` | Getters para todos, setters para todos los no-`final`, `toString()`, `equals()`, `hashCode()`, y un constructor con los atributos `final` (en este caso, ninguno) |
| `@AllArgsConstructor` | Constructor con **todos** los atributos como parámetros — el `new Pais("Argentina", "BA", "Am", 45000000L)` que usás |
| `@NoArgsConstructor` | Constructor **vacío** sin parámetros — `new Pais()` |

### Por qué los tres juntos

- `@Data` por sí solo no genera el constructor con todos los argumentos — solo con los `final`. Necesitás `@AllArgsConstructor`.
- `@NoArgsConstructor` te asegura tener `new Pais()` también. **Lo necesitás** porque frameworks como Jackson o JPA crean objetos por reflexión llamando al constructor vacío y después invocando setters.

> **El patrón `@Data + @AllArgsConstructor + @NoArgsConstructor` es el combo más común en DTOs.** Es lo que vas a ver en el código del profe.

---

## ▶️ Paso 4: Correr y verificar que todo sigue funcionando

**Sin tocar nada más**, corré `Main.java`. Tiene que dar **exactamente la misma salida que en Etapa 2**:

```
Tengo 6 países en el catálogo.
---
Lista completa:
  Pais(nombre=Argentina, capital=Buenos Aires, region=Americas, poblacion=45000000)
  ...
```

> **Detalle:** notá que el `toString` generado por Lombok usa paréntesis y formato `nombre=valor` en vez de `{nombre='valor'}` que tenías antes. Es una diferencia estética. El que tenías a mano podés replicar con `@ToString` personalizado, pero para esta etapa déjalo así.

Si todo anduvo: **acabás de borrar como 40 líneas de código y nada se rompió.** Eso es Lombok.

---

## 🔍 Paso 5: Mirar qué generó Lombok por debajo

Hay dos formas de ver el código generado, **para curiosidad y entendimiento**.

### Forma 1: Delombok (IntelliJ)

Click derecho sobre `Pais.java` → **Refactor → Delombok → All Lombok annotations**.

> **⚠️ ATENCIÓN:** esto reescribe tu archivo `Pais.java` reemplazando las annotations por el código real generado. Es **destructivo** — perdés las annotations. **Hacé `Ctrl + Z`** inmediatamente después de mirar el resultado para volver atrás.

Vas a ver tu archivo expandido con todos los getters, setters, equals, hashCode y toString reales. **Eso es lo que el compilador efectivamente "ve" cuando compila tu clase.** El `.class` contiene exactamente ese código.

### Forma 2: Inspeccionar el `.class` compilado

Desde la terminal, después de compilar:

```bash
./mvnw clean compile
javap -p target/classes/ar/edu/utn/ba/proyecto0/modelo/Pais.class
```

`javap` es una herramienta que te muestra las firmas de los métodos de un `.class`. Vas a ver listados todos los `getNombre()`, `setNombre()`, `equals()`, etc. **Generados por Lombok pero presentes en el bytecode.**

Esto te confirma: **no es magia en runtime, es código generado en compilación.**

---

## 📚 Paso 6: Conocer las otras annotations comunes

Para que las reconozcas en código que vayas a leer:

| Annotation | Qué genera |
|---|---|
| `@Getter` | Solo getters (todos o por atributo si la ponés sobre uno) |
| `@Setter` | Solo setters |
| `@ToString` | Solo `toString()` |
| `@EqualsAndHashCode` | Solo `equals()` y `hashCode()` |
| `@NoArgsConstructor` | Constructor vacío |
| `@AllArgsConstructor` | Constructor con todos los atributos |
| `@RequiredArgsConstructor` | Constructor con los atributos `final` y los `@NonNull` |
| `@Data` | Combo de Getter + Setter + ToString + EqualsAndHashCode + RequiredArgsConstructor |
| `@Value` | Como `@Data` pero **inmutable** (todos los atributos `final`, no genera setters) |
| `@Builder` | Patrón builder (vas a verlo más adelante) |
| `@Slf4j` | Genera un logger `log` para loguear desde la clase |

### Granularidad

Podés ser más fino. Por ejemplo:

```java
@Getter                     // genera getters de TODOS
@Setter                     // genera setters de TODOS
public class Pais {
    @Setter(AccessLevel.PRIVATE)    // setter privado solo para este atributo
    private String nombre;
    
    private String capital;
    
    @Getter(AccessLevel.NONE)        // este NO tiene getter
    private String secreto;
}
```

No lo necesitás ahora, pero anotalo: las annotations son **componibles** y permiten control fino.

---

## 🧪 Ejercicios

### Ejercicio 1: Quitar `@Data` y poner solo `@Getter`

Cambiá la clase a esto:

```java
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Pais {
    // atributos
}
```

Corré `Main.java`. ¿Qué error te tira? Leelo. Es la línea de `argentina.setPoblacion(...)`. Sin `@Setter`, no se genera el método. **Después volvé a poner `@Data`.**

### Ejercicio 2: Crear `DetalleMoneda` con Lombok desde cero

Creá `DetalleMoneda.java` en el package `modelo`:

```java
package ar.edu.utn.ba.proyecto0.modelo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleMoneda {
    private String nombre;
    private String simbolo;
}
```

Probalo desde `Main`:

```java
DetalleMoneda peso = new DetalleMoneda("Peso argentino", "$");
System.out.println(peso);
System.out.println(peso.getNombre());
peso.setSimbolo("ARS");
System.out.println(peso);
```

> Esta clase, así escrita, es **idéntica** a la del código del profe (sin las annotations de Jackson, que verás en Bloque 3).

### Ejercicio 3: `@ToString` personalizado

Agregale a `Pais` esta annotation (sin sacar `@Data`):

```java
@ToString(exclude = "poblacion")
```

Corré `Main`. ¿Cómo cambió la salida? La población desapareció del `toString`. Útil cuando un atributo es muy largo o sensible y no querés mostrarlo.

> `@ToString` "gana" sobre el `toString` generado por `@Data` porque está más específico.

### Ejercicio 4: Mirar el código generado y volver

- Click derecho sobre `Pais.java` → **Refactor → Delombok → All Lombok annotations**.
- Mirá lo que generó.
- `Ctrl + Z` para revertir.

Ahora **sabés exactamente qué código tenés en tu clase** aunque no lo escribiste vos.

---

## ✅ Criterios de "Etapa 3 completa"

- [ ] `pom.xml` tiene la dependencia de Lombok con `<scope>provided</scope>`.
- [ ] `Pais.java` ahora tiene solo atributos + 3 annotations (`@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`).
- [ ] Tu programa sigue corriendo igual que antes — la salida es la misma.
- [ ] Creaste `DetalleMoneda.java` con Lombok (Ejercicio 2).
- [ ] Probaste el ejercicio 1 (sacar `@Data`) y entendiste el error.
- [ ] Usaste el delombok al menos una vez para ver qué se genera.

---

## ✅ Checkpoint

1. ¿Cuándo se ejecuta Lombok — en compilación o en runtime?
2. ¿Qué significa `<scope>provided</scope>` y por qué Lombok lo usa?
3. ¿Por qué `@Data` no es suficiente y necesitás también `@AllArgsConstructor` y `@NoArgsConstructor`?
4. Si descompilás el `.class` generado, ¿qué vas a ver?
5. ¿Por qué frameworks como Jackson siguen funcionando con clases Lombok aunque no escribas getters manuales?
6. ¿Qué pasa con tu `.jar` final cuando deployás — Lombok va adentro?

---

## 🔗 Conexión con el código del profe

Compará tu `DetalleMoneda` con la del profe:

**Tu versión (Etapa 3):**
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleMoneda {
    private String nombre;
    private String simbolo;
}
```

**Del profe (`rest-paises`):**
```java
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

**Las diferencias** son las annotations de **Jackson** (`@JsonIgnoreProperties`, `@JsonProperty`), que sirven para mapear JSON ↔ Java. La parte de Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`) es **idéntica**.

Cuando llegues al Bloque 3 del recorrido del código del profe, vas a leer esos archivos y ya vas a saber qué hace cada cosa. Eso es exactamente lo que estamos construyendo.

---

## 🤔 Una reflexión

Acabás de pasar de:

```java
public class Pais {
    private String nombre;
    private String capital;
    private String region;
    private long poblacion;

    public Pais(String nombre, String capital, String region, long poblacion) {
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
        this.poblacion = poblacion;
    }

    public Pais() {}

    public String getNombre() { return nombre; }
    public void setNombre(String n) { this.nombre = n; }
    public String getCapital() { return capital; }
    public void setCapital(String c) { this.capital = c; }
    public String getRegion() { return region; }
    public void setRegion(String r) { this.region = r; }
    public long getPoblacion() { return poblacion; }
    public void setPoblacion(long p) { this.poblacion = p; }

    @Override
    public String toString() { /* ... */ }

    @Override
    public boolean equals(Object o) { /* ... */ }

    @Override
    public int hashCode() { /* ... */ }
}
```

a:

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pais {
    private String nombre;
    private String capital;
    private String region;
    private long poblacion;
}
```

**Mismo bytecode. Mismo comportamiento.** Pero la intención queda al desnudo: "esto es un DTO con 4 campos". No más ruido.

Por eso Lombok es el estándar de facto en Java moderno. Y por eso entender qué hace por debajo es importante — porque en una entrevista o leyendo código ajeno, vas a tener que saber qué métodos están realmente generados.

---

## ▶️ Próximo paso

Cuando completes la etapa, decime **"arranquemos etapa 4"** y vamos a meter `Optional<Pais>` para implementar `buscarPorNombre()` — el método que devuelve un país encontrado, o "nada" si no existe. Vas a usar Optional en código real.

Si te trabás (especialmente con la configuración de Lombok), preguntá por chat.
