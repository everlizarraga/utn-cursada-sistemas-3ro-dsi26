# 📗 APUNTE RESUMEN — Preclase 03
## Herencia, Interfaces, Polimorfismo y Composición

*Destilación del apunte maestro (4 partes) para repetición espaciada. Cobertura completa, al hueso.*

**Marcas — Sistema 1 (importancia):** 🔴 central · 🟡 secundario · 🟢 al pasar.
**Marcas — Sistema 2 (uso):** 🎯 esencial para aplicar · 📘 contexto para parcial.
*(Para un repaso relámpago tipo machete, leé solo los 🎯.)*

---

# PARTE 1 — Herencia

### 1. El problema 🔴 📘
Varias clases comparten casi todo y difieren en poco. Escribirlas por separado obliga a repetir lo común, y el código repetido es frágil (un cambio = editar en todos lados). La herencia escribe lo común **una vez** en un padre; cada caso lo hereda y agrega lo suyo.

### 2. Qué es la herencia 🔴 🎯
Relación donde una **subclase** (hija) recibe automáticamente atributos y métodos de una **superclase** (padre) y suma los propios. Expresa **"es-un"**: un `Perro` *es un* `Animal`. Esa frase es la prueba: si "X es un Y" es honesto, la herencia corresponde.
En UML: **línea llena + triángulo hueco** apuntando al padre (vs. la punteada de implementar interfaz).

### 3. Extender vs. redefinir 🔴 🎯
- **Extender:** mantener el método del padre y sumar métodos nuevos.
- **Redefinir:** reemplazar un método heredado por versión propia (mismo nombre/firma, distinto comportamiento). Se marca `@Override`.

```java
public class Animal {
    private String nombre;
    public Animal(String nombre) { this.nombre = nombre; }
    public void hacerSonido() {
        System.out.println("Hacer sonido");
    }
}

public class Perro extends Animal {
    public Perro(String nombre) {
        super(nombre);                      // llama al constructor del padre
    }
    @Override
    public void hacerSonido() {
        System.out.println("Guau!");        // REDEFINE: reemplaza al del padre
    }
}
// new Animal("bicho").hacerSonido();  → Hacer sonido
// new Perro("Firulais").hacerSonido();→ Guau!   (gana la versión de la subclase)
```

Para **agregar** al comportamiento del padre en vez de reemplazarlo:
```java
@Override
public void hacerSonido() {
    super.hacerSonido();                    // primero lo del padre
    System.out.println("Guau!");            // y le suma lo suyo → imprime las dos
}
```

### 4. Las herramientas 🔴 🎯
- **`extends`** — declara la herencia; la hija recibe todo lo del padre.
- **`@Override`** — marca que un método redefine uno heredado; el compilador chequea la firma. Opcional, ponela siempre.
- **`super`** — dos usos: **(a)** `super(...)` llama al constructor del padre, **debe ser la primera línea** del constructor de la hija; **(b)** `super.metodo()` llama al método del padre desde uno redefinido.
- **Herencia única** — una sola clase padre. `class Hija extends A, B` no existe (el porqué, en Parte 3).

### 5. Caso: puntaje de donaciones 🔴 🎯

```java
package ar.edu.utn.frba.dds.model.entities;

public class Donante {
    private String nombre;
    private String apellido;
    private String dni;

    public Donante(String nombre, String apellido, String dni) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
    }
}
```

```java
package ar.edu.utn.frba.dds.model.entities.donacion;

import ar.edu.utn.frba.dds.model.entities.Donante;
import java.time.LocalDate;

public class Donacion {                      // SUPERCLASE: lo común
    private LocalDate fecha;
    private Donante donante;
    private int cantidad;

    public Donacion(LocalDate fecha, Donante donante, int cantidad) {
        this.fecha = fecha;
        this.donante = donante;
        this.cantidad = cantidad;
    }

    public int obtenerPuntaje() {            // puntaje POR DEFECTO = cantidad
        return cantidad;
    }
    public LocalDate getFecha() { return fecha; }      // getters: los atributos son
    public int getCantidad()   { return cantidad; }    // private, la hija los lee por acá
}
```

```java
public class DonarDinero extends Donacion {  // ES-UNA Donacion
    private Double monto;

    public DonarDinero(LocalDate fecha, Donante donante, int cantidad, Double monto) {
        super(fecha, donante, cantidad);     // primera línea
        this.monto = monto;
    }
    // NO redefine obtenerPuntaje() → hereda "return cantidad" tal cual.
}
```

```java
public class DonarVianda extends Donacion {  // ES-UNA Donacion
    private String tipoComida;

    public boolean estaVencida() {
        return getFecha().isBefore(LocalDate.now());   // usa getFecha(), no "fecha" (private)
    }
    public DonarVianda(LocalDate fecha, Donante donante, int cantidad, String tipoComida) {
        super(fecha, donante, cantidad);
        this.tipoComida = tipoComida;
    }
    @Override
    public int obtenerPuntaje() {            // REDEFINE
        if (estaVencida()) return 0;         // vencida → 0
        return getCantidad() * 3;            // en buen estado → cantidad * 3
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
        Donante donante = new Donante("Juan", "Swift", "44678356");

        List<Donacion> donaciones = List.of(  // lista del tipo del PADRE, subclases adentro
                new DonarDinero(LocalDate.now(), donante, 1, 5000.0),
                new DonarVianda(LocalDate.now(), donante, 3, "Arroz"),
                new DonarVianda(LocalDate.of(2023, 3, 4), donante, 7, "Fideos")
        );

        int puntajeTotal = 0;
        for (Donacion d : donaciones) {
            int puntaje = d.obtenerPuntaje();          // un llamado, 3 comportamientos
            puntajeTotal += puntaje;
            System.out.println(d.getClass().getSimpleName() + " -> Puntaje: " + puntaje);
        }
        System.out.println("Puntaje total: " + puntajeTotal);
    }
}
// SALIDA:
//   DonarDinero -> Puntaje: 1      (no redefine → cantidad = 1; el monto 5000 NO cuenta)
//   DonarVianda -> Puntaje: 9      (3 * 3, no vencida)
//   DonarVianda -> Puntaje: 0      (año 2023 → vencida)
//   Puntaje total: 10
```

**Puntos clave:** 🎯
- `private` del padre **se hereda pero no se accede** directo desde la hija → por eso los getters.
- **Heredar sin redefinir es una decisión:** `DonarDinero` se queda con la fórmula del padre; el `monto` no influye porque nada lo hizo influir.
- Un mismo llamado, `obtenerPuntaje()`, se resuelve distinto según el objeto real → esto es **polimorfismo por herencia** (nombre formal en la Parte 2).

### 📌 Para el parcial (Parte 1)
- **¿Qué es la herencia y qué relación expresa?** Relación donde una subclase recibe atributos y métodos de una superclase y agrega los propios; expresa "es-un" (`DonarVianda` es una `Donacion`).
- **¿Extender vs. redefinir?** Extender = mantener lo heredado y sumar; redefinir = reemplazar un método heredado por versión propia (`@Override`).
- **¿Para qué `super`?** `super(...)` llama al constructor del padre (primera línea); `super.metodo()` reutiliza el método del padre desde uno redefinido.
- **¿Si no redefine?** Se queda con la implementación del padre, tal cual.

---

# PARTE 2 — Interfaces y Polimorfismo

### 1. Polimorfismo 🔴 🎯
Tratar objetos de clases distintas **como si fueran del mismo tipo** e invocar el mismo método **sin conocer la clase concreta**; cada uno responde a su modo. Condición: **compartir un tipo común**, que puede ser una **superclase** (Parte 1) o una **interfaz**.

### 2. Qué es una interfaz 🔴 🎯
**Contrato**: declara *qué se puede hacer* (firmas de métodos), no *cómo*. Tres propiedades:
- **Se implementa, no se hereda** (`implements`, no `extends`); obliga a escribir todos sus métodos o no compila.
- **No tiene cuerpo**: los métodos terminan en `;`, no en `{ }`.
- **No se instancia** (`new` de una interfaz no existe).
*(Una clase puede implementar varias — Parte 3.)*
En UML: **línea punteada + triángulo hueco** (realización). Punteada = implementa interfaz; llena = hereda clase.

### 3. Caso: notificar por varios canales 🔴 🎯

```java
package ar.edu.utn.frba.dds.model.notificador;

public interface IEstrategiaNotificador {
    boolean notificarUsuario(String medioDeContacto, String mensaje);   // firma; termina en ";"
}
```

```java
package ar.edu.utn.frba.dds.model.notificador.impl;
import ar.edu.utn.frba.dds.model.notificador.IEstrategiaNotificador;

public class NotificadorMail implements IEstrategiaNotificador {
    @Override
    public boolean notificarUsuario(String medioDeContacto, String mensaje) {
        System.out.println("NotificadorMail " + mensaje);      // muestra el mensaje
        return true;
    }
}

public class NotificadorSms implements IEstrategiaNotificador {
    @Override
    public boolean notificarUsuario(String medioDeContacto, String mensaje) {
        System.out.println("NotificadorSms " + medioDeContacto); // muestra el medio
        return true;
    }
}

public class NotificadorWhatsapp implements IEstrategiaNotificador {
    @Override
    public boolean notificarUsuario(String medioDeContacto, String mensaje) {
        System.out.println("NotificadorWhatsapp " + medioDeContacto);
        return true;
    }
}
// Misma firma, distinto por dentro → corazón del polimorfismo.
```

```java
public class Main {
    public static void main(String[] args) {
        List<IEstrategiaNotificador> notificadores = List.of(  // lista del tipo de la INTERFAZ
                new NotificadorMail(),
                new NotificadorSms(),
                new NotificadorWhatsapp()
        );
        String medio   = "+5491123456789";
        String mensaje = "Tu donación fue registrada!";
        notificadores.forEach(n -> n.notificarUsuario(medio, mensaje));  // un llamado, 3 comportamientos
    }
}
// SALIDA:
//   NotificadorMail Tu donación fue registrada!
//   NotificadorSms +5491123456789
//   NotificadorWhatsapp +5491123456789
```

**Extensibilidad** 🎯: agregar un canal = una clase nueva que implemente la interfaz + sumarla a la lista. No se toca ni la interfaz ni lo existente ni el `forEach`.

### 4. Interfaz o herencia — intuición 🟡 📘
- **Herencia** cuando una clase *es un* caso particular de otra, con estado/código común (`DonarVianda` es una `Donacion`).
- **Interfaz** cuando lo único que comparten es *saber hacer algo* (los notificadores no son "tipos de" una cosa; solo saben notificar).

### 📌 Para el parcial (Parte 2)
- **¿Qué es una interfaz?** Contrato que declara firmas sin implementación; se implementa con `implements`, no se instancia ni se hereda; distintas clases la resuelven distinto.
- **¿Qué es el polimorfismo?** Tratar objetos de distintas clases como del mismo tipo (interfaz o superclase común) e invocar el mismo método sin conocer la clase concreta.
- **¿Por qué la interfaz habilita polimorfismo?** Garantiza en compilación que todo implementador responde al contrato; permitís listas del tipo de la interfaz y el objeto resuelve el comportamiento en ejecución.
- **¿`implements` vs. `extends`?** `implements`: interfaces, obliga a implementar métodos, se pueden varias. `extends`: clases, reutiliza código del padre, una sola.

---

# PARTE 3 — Múltiples interfaces y el contrato roto

### 1. El agujero 🔴 📘
El notificador sabe enviar pero no valida que el medio tenga sentido para su canal (le pasás un mail a un SMS y lo manda igual). Falta **validar el medio de contacto**.

### 2. Interfaz nueva, no método más 🔴 🎯
```java
public interface IValidadorMedioDeContacto {
    boolean esValido(String medioDeContacto);
}
```
**Separación de responsabilidades:** notificar y validar son independientes; en contratos separados, cada clase elige qué firma. *(Es la "S" de SOLID — más adelante.)*

### 3. Implementar dos interfaces 🔴 🎯
```java
public class NotificadorWhatsapp
        implements IEstrategiaNotificador, IValidadorMedioDeContacto {  // dos contratos, coma
    @Override
    public boolean notificarUsuario(String medioDeContacto, String mensaje) {
        System.out.println("NotificadorWhatsapp " + medioDeContacto);
        return true;
    }
    @Override
    public boolean esValido(String medioDeContacto) {
        return medioDeContacto.matches("^\\+?\\d{10,15}$");   // forma de teléfono
    }
}
```
**Múltiples interfaces vs. una sola clase padre** 🎯: interfaces solo aportan **obligaciones** → se acumulan sin conflicto. Herencia aporta **código** → dos padres con el mismo método = conflicto irresoluble (**problema del diamante**). Por eso muchas interfaces, un solo padre.

### 4. La validación (regex) 🟡
`^\+?\d{10,15}$` = `^`/`$` anclan inicio/fin · `\+?` un `+` opcional · `\d{10,15}` entre 10 y 15 dígitos. Barras dobles porque en Java se escapan.
⚠️ Es estricto: rechaza espacios/guiones (`+54 11 1234-5678` falla). Limpiar el dato antes de validar.

### 5. 🔴 Antipatrón: `Object` + casts 🎯

```java
static void notificar(Object n, String contacto) {         // Object = renunciás al tipo
    IValidadorMedioDeContacto v = (IValidadorMedioDeContacto) n;   // cast (promesa NO verificada)
    IEstrategiaNotificador    e = (IEstrategiaNotificador) n;

    if (v.esValido(contacto)) {
        e.notificarUsuario(contacto, "Tu pedido fue confirmado!");
    } else {
        System.out.println("Medio de Contacto inválido");
    }
}
```
**Compila y anda** (mientras la promesa del cast sea cierta). **Está mal** porque el día que le pasás algo que no implementa las interfaces:
```java
notificar(new Donante("Ana", "Pérez", "12345678"), "+549...");  // COMPILA...
// ...y explota en EJECUCIÓN: java.lang.ClassCastException
```
Le sacaste al compilador la capacidad de protegerte: **moviste el error de compilación (barato, temprano) a ejecución (caro, tardío)**. Sacrificaste **seguridad de tipos** y **detección temprana**, sin ganar nada = **trade-off perdido**. Además **traiciona el polimorfismo** (recibís algo sin tipo y lo forzás a mano).

**Cómo se hace bien** 🎯:
```java
// A) Genérico con cota de intersección: "T cumple AMBOS contratos". Sin casts.
static <T extends IEstrategiaNotificador & IValidadorMedioDeContacto>
void notificar(T n, String contacto) {
    if (n.esValido(contacto)) n.notificarUsuario(contacto, "...");
    else System.out.println("Medio de Contacto inválido");
}
// notificar(new Donante(...), ...) → NO COMPILA (error temprano, gratis).

// B) Interfaz que combina ambas (interfaz extends interfaces = herencia de contratos):
public interface INotificadorValidable
        extends IEstrategiaNotificador, IValidadorMedioDeContacto { }
static void notificar(INotificadorValidable n, String contacto) { ... }
```

| | `Object` + casts | Genérico / interfaz combinada |
|---|---|---|
| ¿Compila con algo inválido? | Sí 😬 | No ✅ |
| Detección del error | ejecución (tarde) | compilación (temprano) |
| Seguridad de tipos | ninguna | total |

### 📌 Para el parcial (Parte 3)
- **¿Varias interfaces? ¿Varias clases padre?** Todas las interfaces que quiera (solo obligaciones, no chocan); una sola clase (herencia múltiple prohibida → problema del diamante).
- **¿Por qué separar notificar y validar?** Responsabilidades independientes; cada clase elige qué contratos firma; más flexible y mantenible.
- **¿Problema de `Object` + casts?** Pierde seguridad de tipos; acepta cualquier objeto; error recién en ejecución (`ClassCastException`). Traslada la detección de compilación a ejecución sin beneficio.
- **¿Cómo pedir bien un objeto que cumpla dos interfaces?** Genérico `<T extends A & B>` o interfaz que extienda ambas; el compilador chequea.
- **Trade-off:** elijo genérico porque priorizo seguridad de tipos y detección temprana; pago con una firma más verbosa.

---

# PARTE 4 — Composición: cuándo NO heredar

### 1. Interfaces vs. herencia 🔴 🎯

| | Interfaces | Herencia |
|---|---|---|
| Qué define | contrato: *qué se puede hacer* | relación: *qué es* |
| ¿Aporta código? | no (solo firmas) | sí (reutiliza el del padre) |
| ¿Cuántas? | múltiples | una sola clase padre |
| Estado | no (a lo sumo constantes) | hereda y agrega atributos |
| Qué obliga | a implementar todo | a nada (heredar y/o redefinir) |

Interfaz = "sabe hacer X"; herencia = "es un tipo de Y".

### 2. Composición 🔴 🎯
Una clase **tiene** a otra como parte (atributo) y le **delega** trabajo. No hereda ni firma contrato: la contiene. Expresa **"tiene-un"**.
```java
public class Auto {
    private Motor motor;                 // tiene-un
    public void arrancar() {
        motor.encender();                // delega
    }
}
```

### 3. Criterio "es-un" vs. "tiene-un" 🔴 🎯
- "X **es un** Y" honesto → **herencia**.
- "X **tiene un** Y" describe mejor → **composición**.
- vianda *es una* donación ✅ herencia · auto *tiene un* motor ✅ composición · donante *tiene una* forma de donar ✅ composición · donante *es una* forma de donar ❌ forzado.

### 4. La trampa: heredar cuando había que componer 🔴 🎯
Modelar `DonanteDeDinero extends Donante` / `DonanteDeViandas extends Donante` compila y funciona, pero:
1. **Donante que dona las dos cosas no entra** (herencia única) → habría que crear `DonanteDeDineroYViandas` → **explosión de clases**.
2. **Donante que cambia de tipo con el tiempo:** las personas no cambian de clase; habría que crear otro objeto.
3. **Acoplamiento rígido:** "cómo dona" soldado a la identidad del donante.
Raíz: "un donante *es una* forma de donar" es **falso** — donar es lo que *hace*, no lo que *es*.

### 5. La solución: composición 🔴 🎯
```java
public interface IDonacion {
    int obtenerPuntaje();
}

public class DonarDinero implements IDonacion {   // CLASE concreta (aporta el código)
    private Double monto;
    @Override
    public int obtenerPuntaje() { return (int) (monto / 1000); }
}

public class Donante {
    private String nombre, apellido, dni;
    private IDonacion donacion;            // TIENE una forma de donar
    public void donar() {
        int puntaje = donacion.obtenerPuntaje();   // DELEGA
        System.out.println("Puntaje de la donación: " + puntaje);
    }
}
```
Cambiar de tipo = setear otra `IDonacion`. Agregar un tipo = clase nueva que implementa `IDonacion`, **`Donante` no se toca**.
**Por qué `DonarDinero` es clase y no interfaz** 🎯: tiene que *aportar el código* de `obtenerPuntaje()`; la interfaz no tiene cuerpo. Contrato → interfaz; lógica concreta → clase.
🟡 En Parte 1 los *tipos de donación* se modelan con herencia; acá la relación *donante-donación* con composición. Ejes distintos que conviven.

### 6. La regla y su matiz 🔴 🎯
**"Favorecer composición sobre herencia":** ante la duda, componé (acopla menos, más flexible). **No es dogma:** herencia es correcta cuando "es-un" es genuino.
- `DonarVianda` es una `Donacion` → herencia **correcta**.
- `Donante` es una forma de donar → herencia **incorrecta**.
Misma herramienta, bien y mal usada. Decide si "es-un" es honesto.
🕳️ Es la semilla del patrón **Strategy** (comportamientos intercambiables tras una interfaz).

### 📌 Para el parcial (Parte 4) — el bloque clave
Molde de justificación: **"Elijo X porque priorizo [atributo de calidad], y pago con [costo]."**
- **¿Composición y no herencia del donante?** Elijo composición porque priorizo flexibilidad y bajo acoplamiento (separo quién es de cómo dona; agregar tipos no toca `Donante`); con herencia habría explosión de clases y jerarquía rígida. Pago con una indirección más y algo de código inicial.
- **¿Por qué `DonarVianda` sí hereda?** El "es-un" es genuino; comparte todo el estado y solo difiere el puntaje. Priorizo reutilización; el costo (atarse a la jerarquía) es aceptable porque una vianda siempre es una donación.
- **¿"es-un" vs "tiene-un"?** Herencia vs composición; criterio para elegir. Modelar con herencia un "tiene-un" → jerarquías rígidas y explosión de clases.
- **¿"Favorecer composición" es absoluto?** No; guía. Herencia sirve cuando el "es-un" es genuino y hay estado/código común real.

---

## Mapa de la unidad 🎯
Tres formas de relacionar clases: **es-un** (herencia), **sabe hacer** (interfaz), **tiene-un** (composición). Herencia e interfaz dan polimorfismo. Elegir bien entre ellas *es* el diseño.
**Brújula:** antes de heredar, preguntá si "es-un" es honesto; si tenés que forzarlo, componé.

---

**FIN DEL RESUMEN — Preclase 03**
