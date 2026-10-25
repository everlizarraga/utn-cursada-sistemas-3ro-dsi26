# 🛠️ Proyecto 0 — Etapa 1: La clase `Pais`

> **Objetivo:** crear tu primera clase Java de verdad, con atributos encapsulados, constructor y getters/setters. Usarla desde `Main` instanciando objetos.
>
> **Tiempo estimado:** 30-45 minutos.
>
> **Pre-requisito:** Etapa 0 completa (proyecto Maven funcional, sabés correr `Main`).

---

## 🎯 Conceptos que vas a tocar

- Encapsulamiento (atributos `private`)
- La palabra clave `this`
- El operador `new`
- Constructor explícito vs constructor default
- Getters y setters (convención de Java)
- `@Override` y `toString()`
- `long` primitivo vs `Long` wrapper (mini-recordatorio del Bloque 0)

---

## 🏗️ Lo que vas a construir

Una clase `Pais` con 4 atributos:

```
┌────────────────────────────┐
│         Pais               │
├────────────────────────────┤
│ - nombre: String           │
│ - capital: String          │
│ - region: String           │
│ - poblacion: long          │
├────────────────────────────┤
│ + Pais(...)  ← constructor │
│ + getNombre(): String      │
│ + getCapital(): String     │
│ + setCapital(String): void │
│ + getRegion(): String      │
│ + getPoblacion(): long     │
│ + setPoblacion(long): void │
│ + toString(): String       │
└────────────────────────────┘
```

Y vas a usarla desde `Main` creando 3-4 instancias.

> En notación UML: `-` significa `private`, `+` significa `public`.

---

## 📂 Paso 1: Crear el package `modelo`

Convención común: los DTOs / clases de dominio van en un sub-package llamado `modelo` (o `model`, o `domain`).

En IntelliJ:
- Click derecho sobre `ar.edu.utn.ba.proyecto0` (en el panel Project).
- **New → Package**.
- Nombre: `modelo`.

Te queda: `ar.edu.utn.ba.proyecto0.modelo`.

---

## ✍️ Paso 2: Crear la clase `Pais` vacía

- Click derecho sobre el package `modelo`.
- **New → Java Class**.
- Nombre: `Pais`.

Te genera:

```java
package ar.edu.utn.ba.proyecto0.modelo;

public class Pais {
}
```

---

## 🔐 Paso 3: Agregar los atributos privados

Tipeá (recordá: **a mano**, no copies y pegues):

```java
package ar.edu.utn.ba.proyecto0.modelo;

public class Pais {

    // Atributos (también llamados "campos" o "fields")
    private String nombre;
    private String capital;
    private String region;
    private long poblacion;

}
```

### Por qué los atributos son `private`

Esto es **encapsulamiento**, uno de los pilares de POO. La idea:

- **No querés** que cualquier código de afuera modifique los datos de un `Pais` directamente.
- **Sí querés** controlar quién y cómo modifica esos datos, vía métodos públicos (getters/setters).

**Mal (atributos públicos directos):**
```java
public String nombre;        // ❌ cualquiera puede hacer pais.nombre = null
```

**Bien (private + métodos públicos):**
```java
private String nombre;       // ✅ solo Pais puede tocar `nombre`
public String getNombre() { return nombre; }   // ✅ acceso controlado
```

Si el día de mañana querés que `getNombre()` haga algo más (loguear, validar, formatear), podés cambiarlo sin romper el código que usa `Pais`. Si fuera público directo, no podrías.

### Por qué `long` y no `Long`

Por ahora los datos son **hardcodeados** (45 millones, 210 millones). No hay riesgo de `null`. Usamos el primitivo `long`, que es más liviano.

> **Más adelante** (cuando integremos con datos externos como JSON), vas a usar `Long` (wrapper) porque la API podría no traer ese dato y necesitamos representar "no hay valor" como `null`. Eso conecta con lo que viste en el Bloque 0 sobre primitivos vs wrappers.

### Detalle de la `L` en `45000000L`

Cuando escribís un literal numérico en Java, el compilador lo asume como `int` por defecto. Si el número es muy grande para `int` (más de ~2.100 millones), o si querés forzarlo a `long`, le ponés `L` al final:

```java
long poblacion = 1500000000L;    // los 1.500 millones, con L
```

Sin la `L`, en algunos casos te tira error de compilación. Acordate de esto para el constructor.

---

## 🔧 Paso 4: Escribir el constructor

Agregá esto **debajo de los atributos**:

```java
    // Constructor
    public Pais(String nombre, String capital, String region, long poblacion) {
        this.nombre = nombre;
        this.capital = capital;
        this.region = region;
        this.poblacion = poblacion;
    }
```

### Qué dice cada parte

```
public Pais(String nombre, String capital, String region, long poblacion) {
   │     │         │                 │                                   │
   │     │         └──── parámetros (lo que recibe al crearse) ──────────┘
   │     │
   │     └──── nombre del constructor (DEBE ser igual al de la clase)
   │
   └──── public: cualquiera puede llamarlo para crear un Pais
```

### Qué hace `this`

`this` es una referencia al **objeto actual**. Sirve para diferenciar:

- El **atributo** de la clase: `this.nombre`
- El **parámetro** del método: `nombre`

```java
this.nombre = nombre;
// ↑       ↑
// │       └─ El parámetro que recibí
// └─ Mi atributo (de la instancia que estoy construyendo)
```

Es como decir: "asignale al atributo `nombre` de esta instancia el valor del parámetro `nombre` que me pasaron".

> **Si los parámetros tuvieran otro nombre** (ej: `nuevoNombre`), no necesitarías `this.`. Pero la convención de Java es usar el mismo nombre y desambiguar con `this.`. Es lo idiomático.

### Ojo: cuando escribís un constructor, perdés el "default"

Java te regala automáticamente un constructor **sin parámetros** si vos no escribís ninguno. Pero apenas escribís uno con parámetros, ese default **desaparece**. O sea:

```java
// Antes (sin constructor escrito):
Pais p = new Pais();   // ✅ funcionaba (constructor default)

// Después (con el constructor que escribiste):
Pais p = new Pais();                                // ❌ ERROR de compilación
Pais p = new Pais("Argentina", "BA", "Amer", 45000000L);   // ✅ 
```

> Recordá esto. Más adelante, cuando aparezca Lombok (Etapa 3), vas a ver `@NoArgsConstructor` y `@AllArgsConstructor` — son anotaciones que generan automáticamente esos dos constructores. Aparecen porque algunos frameworks (Jackson, JPA) **necesitan** el constructor sin parámetros para crear objetos vía reflexión.

---

## 📖 Paso 5: Escribir los getters

Convención de Java: para cada atributo, un método público `getXxx()` que devuelve su valor.

Agregá debajo del constructor:

```java
    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getCapital() {
        return capital;
    }

    public String getRegion() {
        return region;
    }

    public long getPoblacion() {
        return poblacion;
    }
```

### Por qué esta convención exacta

La convención `getXxx()` no es estética. **Toda la API de Java, Spring, Jackson, Lombok, JPA, y básicamente todo el ecosistema** asume que tus clases siguen este patrón. Por ejemplo:

- **Jackson** (serialización JSON) lee los métodos `getNombre()`, `getCapital()` para escribir el JSON.
- **Lombok** genera estos métodos automáticamente con `@Data` siguiendo esta convención.
- **Spring** lo usa para data binding (formularios, REST).

Si los llamás `obtenerNombre()` o `nombre()`, funcionan en tu código pero **ninguno de esos frameworks los va a reconocer**. Por eso vale la pena hacerlo bien desde el principio.

### Para `boolean`, la convención es `isXxx()`

```java
private boolean activo;

public boolean isActivo() {     // ← no getActivo()
    return activo;
}
```

No lo vas a necesitar ahora, pero anotalo.

---

## 🖊️ Paso 6: Escribir los setters (selectivamente)

Setters son los simétricos de los getters: permiten **modificar** el valor.

```java
    // Setters
    public void setCapital(String capital) {
        this.capital = capital;
    }

    public void setPoblacion(long poblacion) {
        this.poblacion = poblacion;
    }
```

### Decisión de diseño: ¿setter sí o setter no?

**No es obligatorio ponerle setter a todos los atributos.** Pensá:

- **Capital** → puede cambiar (un país puede mover su capital). Setter ✅
- **Población** → cambia con el tiempo. Setter ✅
- **Nombre** → un país no cambia su nombre todos los días. Setter ❌ (mejor inmutable)
- **Región** → tampoco cambia. Setter ❌

**Lo que dejás sin setter, queda inmutable** (solo se puede asignar en el constructor). Esto es bueno para el diseño porque:

- Menos chance de bugs raros (alguien le cambia el nombre a un país sin querer).
- Cuando ves un atributo sin setter, leés "este valor no cambia después de creado".

> **En el código del profe**, los DTOs usan Lombok `@Data` que genera setters para **todos** los atributos. Es un trade-off: más cómodo pero menos seguro. En Etapa 3 vamos a ver eso.

---

## 🏷️ Paso 7: Override de `toString()`

Cuando hacés `System.out.println(pais)`, Java internamente llama a `pais.toString()`. **Sin override, te devuelve algo feo como `Pais@1b6d3586`** (el hash de memoria del objeto). Sobrescribilo para que muestre algo útil:

```java
    // toString
    @Override
    public String toString() {
        return "Pais{" +
                "nombre='" + nombre + '\'' +
                ", capital='" + capital + '\'' +
                ", region='" + region + '\'' +
                ", poblacion=" + poblacion +
                '}';
    }
```

### Qué es `@Override`

Es una **annotation** (recordá del Bloque 0). Dice: "este método está sobrescribiendo uno que ya existe en una clase padre".

¿Por qué importa? Porque **TODA clase en Java hereda implícitamente de `Object`**, que ya tiene un método `toString()`. Vos lo estás reemplazando con tu versión.

`@Override` no es obligatorio, pero **es buena práctica** porque:
- Si te equivocás (escribís `toStrng`), el compilador te lo marca: "no podés override algo que no existe en el padre".
- Avisa al lector "estoy sobrescribiendo, no creando algo nuevo".

### La sintaxis fea con `'\''`

`'\''` es una comilla simple escapada — un carácter `'`. Lo usamos para envolver los strings en comillas simples adentro del toString, así se distingue visualmente del resto:

```
Pais{nombre='Argentina', capital='Buenos Aires', region='Americas', poblacion=45000000}
```

> **Atajo del IDE:** IntelliJ puede generar `toString()` automáticamente. Click derecho dentro de la clase → **Generate (Alt+Insert) → toString()**. Después de hacerlo a mano una vez, podés usar el atajo para las siguientes clases. Lo mismo aplica a getters y setters.

---

## 🎬 Paso 8: Usar la clase desde `Main`

Volvé a `Main.java` (en el package `ar.edu.utn.ba.proyecto0`). Borrá todo el contenido del método `main` (los ejercicios previos) y reemplazalo por:

```java
package ar.edu.utn.ba.proyecto0;

import ar.edu.utn.ba.proyecto0.modelo.Pais;

public class Main {

    public static void main(String[] args) {
        // Crear instancias con new + constructor
        Pais argentina = new Pais("Argentina", "Buenos Aires", "Americas", 45000000L);
        Pais brasil = new Pais("Brasil", "Brasilia", "Americas", 210000000L);
        Pais espana = new Pais("España", "Madrid", "Europe", 47000000L);

        // Imprimir cada uno (usa toString internamente)
        System.out.println(argentina);
        System.out.println(brasil);
        System.out.println(espana);

        System.out.println("---");

        // Acceder a atributos vía getters
        System.out.println("La capital de " + argentina.getNombre() + " es " + argentina.getCapital());
        System.out.println(brasil.getNombre() + " tiene " + brasil.getPoblacion() + " habitantes.");

        // Modificar vía setter
        argentina.setPoblacion(46000000L);
        System.out.println("Población actualizada de Argentina: " + argentina.getPoblacion());
    }
}
```

### Cosas que pasan acá

**1. El `import`**
```java
import ar.edu.utn.ba.proyecto0.modelo.Pais;
```
Tenés que traer la clase `Pais` desde otro package. IntelliJ lo agrega automáticamente cuando empezás a tipear `Pais` (te lo sugiere). Si no, presioná `Alt+Enter` con el cursor sobre `Pais`.

**2. El operador `new`**
```java
Pais argentina = new Pais("Argentina", "Buenos Aires", "Americas", 45000000L);
```
`new Pais(...)`:
- Reserva memoria para un nuevo `Pais`.
- Llama al constructor con los argumentos que pasás.
- Devuelve la referencia al objeto creado.

`Pais argentina = ...` declara una variable de tipo `Pais` que apunta a ese objeto.

**3. `System.out.println(argentina)` llama implícitamente a `argentina.toString()`**

Cuando le pasás cualquier objeto a `println()`, Java automáticamente llama a `.toString()` sobre él para convertirlo a String.

**4. Notación punto para acceder**

`argentina.getCapital()` → "del objeto argentina, llamá al método getCapital".

---

## ▶️ Correlo

Apretá el triángulo verde sobre `main`. Tenés que ver algo como:

```
Pais{nombre='Argentina', capital='Buenos Aires', region='Americas', poblacion=45000000}
Pais{nombre='Brasil', capital='Brasilia', region='Americas', poblacion=210000000}
Pais{nombre='España', capital='Madrid', region='Europe', poblacion=47000000}
---
La capital de Argentina es Buenos Aires
Brasil tiene 210000000 habitantes.
Población actualizada de Argentina: 46000000

Process finished with exit code 0
```

Si querés también: corré desde consola con `./mvnw compile exec:java` (necesitás el plugin `exec` del anexo anterior).

---

## 🧪 Ejercicios

Hacelos uno por uno, corriendo después de cada uno.

### Ejercicio 1: Agregar otro atributo
- Agregale a `Pais` un atributo `private double superficie` (en km²).
- Actualizá el constructor para que lo reciba.
- Agregale getter (sin setter — la superficie no cambia).
- Actualizá el `toString` para incluirlo.
- Actualizá `Main` para pasar la superficie de cada país (Argentina: 2780400, etc.).

### Ejercicio 2: Crear más países
Agregá 2 países más en `Main`: Francia, Alemania, Chile, México, lo que quieras. Probá imprimirlos.

### Ejercicio 3: Probar el constructor con `null`
```java
Pais misterioso = new Pais(null, null, null, 0L);
System.out.println(misterioso);
```
¿Qué imprime? ¿Crashea? ¿Por qué?

### Ejercicio 4: Intentar acceder a un atributo directamente
En `Main`, intentá escribir:
```java
argentina.nombre = "ARGENTINA";
```
¿Qué pasa? Leé el error. **No lo arregles** — entendé por qué Java no te deja.

### Ejercicio 5 (bonus): Generador del IDE
- Borrá los getters/setters/toString de `Pais` que escribiste a mano.
- Click derecho dentro de `Pais` → **Generate (Alt+Insert)**.
- Elegí **Getter and Setter** → seleccioná los 4 atributos.
- Después **Generate → toString** → seleccioná los atributos.
- Compará con lo que tipeaste a mano. ¿Hay diferencias?

> Ahora que viviste el dolor de escribirlos a mano, podés usar el generador sin culpa.

---

## ✅ Criterios de "Etapa 1 completa"

- [ ] Tenés el package `ar.edu.utn.ba.proyecto0.modelo`.
- [ ] Tenés `Pais.java` con 4 atributos `private` (al menos: `nombre`, `capital`, `region`, `poblacion`).
- [ ] Tenés un constructor que recibe los 4 valores.
- [ ] Tenés getters para los 4 atributos.
- [ ] Tenés al menos un setter (para los que decidiste que deberían ser mutables).
- [ ] Tenés un `toString()` con `@Override` que muestra todos los atributos.
- [ ] Desde `Main`, creás 3 o más instancias de `Pais` y las imprimís.
- [ ] Probaste acceder a atributos vía getters y modificarlos vía setters.
- [ ] Podés explicar (a vos mismo) qué hace `this`, qué hace `new`, y por qué `nombre` es `private` en vez de `public`.

---

## 🔗 Conexión con el código del profe

Cuando llegues al **Bloque 3** del recorrido del código del profe (`Pais.java`, `NombrePais.java`, `DetalleMoneda.java`), vas a ver clases muy parecidas a la tuya — pero **mucho más cortas** porque usan Lombok para generar getters/setters/toString automáticamente.

```java
// Del código del profe (te lo adelanto):
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetalleMoneda {
    @JsonProperty("name")
    private String nombre;

    @JsonProperty("symbol")
    private String simbolo;
}
```

Eso es **exactamente** lo que vos hiciste a mano, pero condensado con annotations. En la Etapa 3 vamos a transformar tu `Pais.java` para usar Lombok y vas a apreciar el ahorro.

---

## ✅ Checkpoint

Si podés contestar estas, estás listo para Etapa 2:

1. ¿Por qué los atributos son `private` y los métodos son `public`?
2. ¿Qué hace `this.nombre = nombre` exactamente?
3. ¿Qué pasa si escribís un constructor con parámetros pero no agregás uno sin parámetros?
4. ¿Por qué la convención es `getNombre()` y no `nombre()` u `obtenerNombre()`?
5. ¿Qué hace `@Override` y cuándo es buena práctica usarlo?
6. ¿Qué hace `new Pais(...)` por dentro?

---

## ▶️ Próximo paso

Cuando completes la etapa, decime **"arranquemos etapa 2"** y vamos a crear un **catálogo** que tenga muchos países — primer encuentro con `List<Pais>`.

Si te trabás con algo, preguntá por chat.
