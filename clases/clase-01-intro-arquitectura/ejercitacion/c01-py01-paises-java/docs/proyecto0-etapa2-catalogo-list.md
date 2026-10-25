# 🛠️ Proyecto 0 — Etapa 2: El catálogo con `List<Pais>`

> **Objetivo:** crear una clase `CatalogoPaises` que mantenga una colección de países hardcodeados, y aprender a recorrerlos con `for-each` y `for` clásico.
>
> **Tiempo estimado:** 30-45 minutos.
>
> **Pre-requisito:** Etapa 1 completa (`Pais` con constructor, getters, setters, toString).

---

## 🎯 Conceptos que vas a tocar

- `List<T>` (interfaz) vs `ArrayList<T>` (clase concreta) — aplicación práctica del Anexo de Collections
- El **diamond operator** `<>`
- Inicializar listas con `new ArrayList<>()` + `.add()`
- Alternativas: `List.of(...)` y `Arrays.asList(...)`
- Iterar con **for-each** (`for (Pais p : lista)`)
- Iterar con **for clásico** (`for (int i = 0; ...)`)
- Métodos útiles de `List`: `add()`, `size()`, `get()`, `isEmpty()`
- Encapsulamiento de colecciones (mini-charla sobre lo defensivo)

---

## 🏗️ Lo que vas a construir

```
┌─────────────────────────────┐         ┌────────────────────────────┐
│      CatalogoPaises         │         │           Pais             │
├─────────────────────────────┤         ├────────────────────────────┤
│ - paises: List<Pais>        │ ◇────►  │ - nombre: String           │
├─────────────────────────────┤  tiene  │ - capital: String          │
│ + CatalogoPaises()          │  muchos │ - region: String           │
│ + getTodos(): List<Pais>    │         │ - poblacion: long          │
│ + cantidad(): int           │         │ ...                        │
└─────────────────────────────┘         └────────────────────────────┘
```

`CatalogoPaises` **tiene** una lista de `Pais` (relación de composición). En el constructor, vamos a llenarla con países hardcodeados.

---

## 📂 Paso 1: Crear el package `catalogo`

Igual que antes:
- Click derecho sobre `ar.edu.utn.ba.proyecto0` (en el panel Project).
- **New → Package**.
- Nombre: `catalogo`.

Te queda: `ar.edu.utn.ba.proyecto0.catalogo`.

> **Convención:** un package por responsabilidad. `modelo` para entidades, `catalogo` para colecciones gestionadas, después vendrán `servicios`, `repositorio`, etc. Esto rima con cómo está organizado el código del profe (`config`, `services`, `dto`).

---

## ✍️ Paso 2: Crear la clase `CatalogoPaises` vacía

- Click derecho sobre `catalogo`.
- **New → Java Class**.
- Nombre: `CatalogoPaises`.

Tipeá:

```java
package ar.edu.utn.ba.proyecto0.catalogo;

public class CatalogoPaises {

}
```

---

## 📋 Paso 3: Agregar el atributo `List<Pais>`

Tipeá adentro de la clase:

```java
    private List<Pais> paises;
```

IntelliJ te va a subrayar `List` y `Pais` en rojo (no los conoce todavía). Parate sobre cada uno y apretá **`Alt + Enter`** → elegí "Import class". Te va a agregar arriba:

```java
import ar.edu.utn.ba.proyecto0.modelo.Pais;
import java.util.List;
```

> **Punto importante:** `List` vive en `java.util` (la librería estándar de Java para collections). `Pais` vive en tu propio package `modelo`. IntelliJ te ayuda a importar lo correcto.

### Por qué `private List<Pais>` y no `private ArrayList<Pais>`

Esto es exactamente lo que viste en el Anexo de Collections:

```java
private List<Pais> paises;       // ✅ declarado como interfaz
```

Mañana si querés cambiar de `ArrayList` a `LinkedList`, **solo cambiás la línea donde instanciás**. Nada del resto del código de `CatalogoPaises` se rompe.

---

## 🏗️ Paso 4: Constructor con países hardcodeados

Agregá esto debajo del atributo:

```java
    public CatalogoPaises() {
        this.paises = new ArrayList<>();
        this.paises.add(new Pais("Argentina", "Buenos Aires", "Americas", 45000000L));
        this.paises.add(new Pais("Brasil", "Brasilia", "Americas", 210000000L));
        this.paises.add(new Pais("Chile", "Santiago", "Americas", 19000000L));
        this.paises.add(new Pais("España", "Madrid", "Europe", 47000000L));
        this.paises.add(new Pais("Francia", "París", "Europe", 67000000L));
        this.paises.add(new Pais("Japón", "Tokio", "Asia", 125000000L));
    }
```

Otra vez `ArrayList` te va a aparecer en rojo. **`Alt + Enter`** → importar de `java.util.ArrayList`.

### Qué está pasando línea por línea

**Línea 1: el constructor**
```java
public CatalogoPaises() {
```
Constructor sin parámetros. Notá que no recibe nada — vamos a tener un catálogo "auto-armado" con datos hardcodeados.

**Línea 2: el diamond operator `<>`**
```java
this.paises = new ArrayList<>();
```
- `new ArrayList<>()` crea una nueva lista vacía.
- Los `<>` en el medio se llaman **diamond operator** (operador diamante).
- Le decís a Java "inferí el tipo del contexto". Como `paises` ya está declarado como `List<Pais>`, el compilador entiende que el `<>` significa `<Pais>`.
- Sin diamond: `new ArrayList<Pais>()` — más verboso pero idéntico.

**Líneas 3-8: agregar elementos**
```java
this.paises.add(new Pais("Argentina", "Buenos Aires", "Americas", 45000000L));
```
- `paises.add(x)` agrega `x` al final de la lista.
- Adentro del paréntesis hay un `new Pais(...)` — creamos el objeto al toque, sin guardarlo en una variable. Es **descartable**: no lo vamos a referenciar después porque vive adentro de la lista.

### Otras formas de inicializar una lista (para que conozcas)

Estas son **alternativas** que vas a encontrar en otro código. Las menciono para que las reconozcas, pero **no las uses todavía**:

**Alternativa A: `List.of(...)` — lista inmutable**
```java
this.paises = List.of(
    new Pais("Argentina", "Buenos Aires", "Americas", 45000000L),
    new Pais("Brasil", "Brasilia", "Americas", 210000000L)
);
```
- Más conciso.
- **PERO la lista es inmutable**: si después intentás `paises.add(...)` te tira excepción.
- Útil cuando sabés que no vas a modificarla.

**Alternativa B: `Arrays.asList(...)` — vista de array como lista**
```java
this.paises = Arrays.asList(
    new Pais("Argentina", "BA", "Am", 45000000L),
    new Pais("Brasil", "Br", "Am", 210000000L)
);
```
- Lista de tamaño fijo (no podés agregar o sacar, pero sí modificar elementos existentes).
- Es el equivalente a "envolver un array en lista".

**Nuestro `new ArrayList<>() + .add()`** es la opción más flexible (lista mutable, podés agregar/sacar libremente). Por eso la usamos en esta etapa.

---

## 📤 Paso 5: Método `getTodos()`

Agregá este método debajo del constructor:

```java
    public List<Pais> getTodos() {
        return this.paises;
    }
```

Es el getter del atributo `paises`. Devuelve la lista entera para que quien la pida pueda recorrerla.

### Cuidado: estás exponiendo tu lista interna

Hay un detalle de diseño acá. Cuando devolvés `this.paises`, **estás devolviendo la lista interna real**, no una copia. Eso significa que quien la reciba podría modificarla:

```java
// Desde afuera:
List<Pais> todos = catalogo.getTodos();
todos.add(new Pais("Atlantis", "...", "...", 0L));   // ¡Modificó la lista interna del catálogo!
todos.clear();                                         // ¡Vació el catálogo!
```

Para esta etapa **está bien** dejarlo así porque es lo más simple y replica el patrón del código del profe (que también devuelve listas directamente). Pero anotalo en algún rincón de tu cabeza:

> **Patrón defensivo (futuro):**
> ```java
> public List<Pais> getTodos() {
>     return new ArrayList<>(this.paises);   // copia
>     // o:
>     return List.copyOf(this.paises);        // copia inmutable
> }
> ```
> Esto evita que de afuera modifiquen tu lista. Lo dejamos para más adelante.

---

## 🔢 Paso 6: Método `cantidad()`

Agregá:

```java
    public int cantidad() {
        return this.paises.size();
    }
```

`paises.size()` devuelve la cantidad de elementos en la lista. Es el equivalente a `arr.length` de los arrays, pero como método (no atributo).

> Notá que **no se llama `getCantidad()` con la convención de getter**. Esto es deliberado: cuando un método **calcula** algo (en vez de devolver un atributo directo), la convención es nombrarlo con el verbo que describe la acción. `getCantidad()` también sería válido — es estilo.

---

## 🎬 Paso 7: Usar el catálogo desde `Main`

Volvé a `Main.java`. Vaciá el método `main` y reemplazalo por:

```java
package ar.edu.utn.ba.proyecto0;

import ar.edu.utn.ba.proyecto0.catalogo.CatalogoPaises;
import ar.edu.utn.ba.proyecto0.modelo.Pais;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Crear el catálogo (el constructor lo llena con países hardcodeados)
        CatalogoPaises catalogo = new CatalogoPaises();

        // 1. Imprimir cuántos hay
        System.out.println("Tengo " + catalogo.cantidad() + " países en el catálogo.");
        System.out.println("---");

        // 2. Recorrer con for-each — la forma idiomática
        System.out.println("Lista completa:");
        for (Pais p : catalogo.getTodos()) {
            System.out.println("  " + p);
        }
        System.out.println("---");

        // 3. Recorrer con for clásico (con índice)
        System.out.println("Solo nombres con número:");
        List<Pais> todos = catalogo.getTodos();
        for (int i = 0; i < todos.size(); i++) {
            System.out.println((i + 1) + ". " + todos.get(i).getNombre());
        }
    }
}
```

### Las dos formas de iterar

**For-each (la idiomática):**
```java
for (Pais p : catalogo.getTodos()) {
    System.out.println("  " + p);
}
```
- Se lee: "para cada `Pais p` en la lista..."
- Equivalente al `for (const p of array)` de JS.
- **Más limpia**, menos chance de error de índice.
- **No te da acceso al índice.** Si lo necesitás, usá el for clásico.

**For clásico (con índice):**
```java
for (int i = 0; i < todos.size(); i++) {
    System.out.println((i + 1) + ". " + todos.get(i).getNombre());
}
```
- Idéntico al de C/JS.
- Útil cuando necesitás el número de posición.
- Más verboso.

> **Regla práctica:** usá `for-each` por defecto. Pasate al for clásico solo cuando necesites el índice o querés iterar al revés (`for (int i = list.size() - 1; i >= 0; i--)`).

---

## ▶️ Correlo

Triángulo verde sobre `main`. Tenés que ver algo como:

```
Tengo 6 países en el catálogo.
---
Lista completa:
  Pais{nombre='Argentina', capital='Buenos Aires', region='Americas', poblacion=45000000}
  Pais{nombre='Brasil', capital='Brasilia', region='Americas', poblacion=210000000}
  ...
---
Solo nombres con número:
1. Argentina
2. Brasil
3. Chile
4. España
5. Francia
6. Japón

Process finished with exit code 0
```

---

## 🧪 Ejercicios

### Ejercicio 1: Agregar países
Agregá 3 países más al catálogo: uno de África (ej: Nigeria), uno de Oceanía (Australia), y uno más a tu gusto. Corré y verificá que `cantidad()` y la iteración los muestran.

### Ejercicio 2: Filtrar manualmente por región
En `Main`, agregá un loop que imprima **solo los países de la región "Americas"**:

```java
System.out.println("Solo de Americas:");
for (Pais p : catalogo.getTodos()) {
    if (p.getRegion().equals("Americas")) {
        System.out.println("  " + p.getNombre());
    }
}
```
> **Recordá del Bloque 0:** comparación de Strings con `.equals()`, no con `==`.

### Ejercicio 3: Filtrar por población
Imprimí solo los países con **más de 50 millones de habitantes**.

### Ejercicio 4: Encontrar el país con más población
Sin usar streams (eso es Etapa 5), encontrá manualmente el país con mayor `poblacion` y mostralo. Pista:

```java
Pais masGrande = null;
for (Pais p : catalogo.getTodos()) {
    if (masGrande == null || p.getPoblacion() > masGrande.getPoblacion()) {
        masGrande = p;
    }
}
System.out.println("El más poblado: " + masGrande);
```

### Ejercicio 5: Contar cuántos hay por región
Sin usar maps todavía, hacé un loop que imprima cuántos países hay en "Americas" y cuántos en "Europe". Pista: dos contadores `int` que vas incrementando dentro del loop.

### Ejercicio 6 (bonus): Romper el encapsulamiento aposta
Probá esto en `Main` y mirá qué pasa:

```java
List<Pais> tramposo = catalogo.getTodos();
tramposo.clear();                                    // ⚠️ vacía la lista interna
System.out.println("Cantidad ahora: " + catalogo.cantidad());
```

Vas a ver `Cantidad ahora: 0`. **El catálogo se vació desde afuera.** Esto es exactamente la fuga de encapsulamiento que mencionamos en el Paso 5. Anotalo mentalmente; lo vamos a abordar más adelante.

---

## ✅ Criterios de "Etapa 2 completa"

- [ ] Tenés el package `ar.edu.utn.ba.proyecto0.catalogo`.
- [ ] Tenés `CatalogoPaises.java` con `private List<Pais> paises`.
- [ ] El constructor inicializa la lista con `new ArrayList<>()` y agrega al menos 6 países.
- [ ] Tenés método `getTodos()` que devuelve la lista.
- [ ] Tenés método `cantidad()` que devuelve el `size()`.
- [ ] Desde `Main` instanciás el catálogo y lo recorrés con **ambas** formas (for-each y for clásico).
- [ ] Resolviste al menos los ejercicios 2 y 4 (filtros manuales con loop + if).

---

## ✅ Checkpoint

Si podés contestar estas, estás listo para Etapa 3:

1. ¿Por qué declaramos `List<Pais> paises` y no `ArrayList<Pais> paises`?
2. ¿Qué hace el diamond operator `<>` en `new ArrayList<>()`?
3. ¿Cuándo usarías `for-each` y cuándo `for` clásico?
4. ¿Por qué la comparación de regiones se hace con `.equals()` y no con `==`?
5. ¿Qué problema tiene exponer la lista interna vía `return this.paises`?
6. ¿Qué diferencia hay entre `new ArrayList<>()` y `List.of(...)`?

---

## 🔗 Conexión con el código del profe

Lo que hiciste rima con el código del profe en `BuscadorDePaises.java`:

```java
// Del código del profe:
public List<Pais> buscarTodos() {
    Pais[] cuerpo = restTemplate.getForObject(uri, Pais[].class);
    return Arrays.asList(cuerpo);
}
```

La diferencia: él **trae los países desde una API REST**, vos los tenés **hardcodeados en el constructor**. Pero la firma del método (`List<Pais>`), la idea de "una clase que sabe darte los países", y el patrón de devolución son los mismos.

Cuando lleguemos a las etapas más avanzadas (o cuando agreguemos Spring al Proyecto 0), reemplazaremos el hardcodeo por una fuente real. La estructura ya está.

---

## ▶️ Próximo paso

Cuando completes la etapa, decime **"arranquemos etapa 3"** y vamos a meter **Lombok** para limpiar todo el boilerplate de `Pais`. Vas a ver cómo `@Data` reemplaza 40 líneas tuyas con 1 sola línea. Va a doler de lo cómodo.

Si te trabás, preguntá por chat.
