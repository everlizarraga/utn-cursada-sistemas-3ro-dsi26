# 📘 APUNTE MAESTRO — Preclase 03 · Parte 3
## Múltiples interfaces y el contrato roto

---

## 🧭 Cómo leer esto

Continúa la unidad. Se apoya en las Partes 1 (Herencia) y 2 (Interfaces y polimorfismo), y en las preclases anteriores, que se dan por sabidos. Todo el material está unificado acá.

Cada bloque de código está comentado, con su **resultado esperado como comentario**.

**Leyenda:** 🔴 central/evaluable · 🟡 secundario · 🟢 al pasar · 🕳️ madriguera · 📌 respuesta modelo de parcial.

---

## 1. El agujero del ejemplo 🔴

En la Parte 2, un notificador recibía un **medio de contacto** y un **mensaje**, y enviaba. Pero mirá esta llamada:

```java
IEstrategiaNotificador sms = new NotificadorSms();
sms.notificarUsuario("ana.perez@mail.com", "Tu donación fue registrada!");
// Consola:  NotificadorSms ana.perez@mail.com
```

Le pasamos una **dirección de mail** a un canal de **SMS**, y lo mandó igual, sin quejarse. El notificador sabe enviar, pero no sabe si el dato que recibió tiene sentido para su canal. Falta una responsabilidad: **validar el medio de contacto** antes de enviar.

---

## 2. La decisión: una interfaz nueva, no un método más 🔴

Podríamos agregarle un método `esValido` a `IEstrategiaNotificador`. Sería un error. Lo correcto es una **interfaz aparte**:

```java
public interface IValidadorMedioDeContacto {
    boolean esValido(String medioDeContacto);
}
```

¿Por qué separada? Por **separación de responsabilidades**: *notificar* es una cosa (mandar un mensaje), *validar* es otra (decidir si un dato tiene la forma correcta). Son independientes. Si las metemos en un mismo contrato, toda clase que quiera notificar queda obligada a saber validar, y al revés. Separadas, cada clase elige qué contratos firma.

> 🕳️ **Madriguera.** "Un contrato, una responsabilidad" es una versión del *Principio de Responsabilidad Única*, parte de un conjunto de principios de diseño (SOLID) que se ven más adelante. *Por ahora alcanza la intuición.*

---

## 3. Implementar dos interfaces a la vez 🔴

Queremos que cada notificador **sepa notificar y validar**. En Java, una clase firma **varios contratos** listándolos tras `implements`, separados por coma:

```java
package ar.edu.utn.frba.dds.model.notificador.impl;

import ar.edu.utn.frba.dds.model.notificador.IEstrategiaNotificador;
import ar.edu.utn.frba.dds.model.notificador.IValidadorMedioDeContacto;

// 👇 DOS interfaces. La clase se compromete con AMBAS y debe implementar
//    todos los métodos de las dos: notificarUsuario Y esValido.
public class NotificadorWhatsapp
        implements IEstrategiaNotificador, IValidadorMedioDeContacto {

    @Override
    public boolean notificarUsuario(String medioDeContacto, String mensaje) {
        System.out.println("NotificadorWhatsapp " + medioDeContacto);
        return true;
    }

    @Override
    public boolean esValido(String medioDeContacto) {
        // Valida que tenga forma de teléfono (detalle en la sección 4).
        return medioDeContacto.matches("^\\+?\\d{10,15}$");
    }
}
```

Y `NotificadorMail` implementa las mismas dos, pero valida distinto (formato de mail en vez de teléfono). Cada clase valida **según su canal**.

### Múltiples interfaces, una sola clase padre 🔴

Acá se salda una deuda de la Parte 1, donde quedó dicho que la herencia es **única** (una sola clase padre) sin explicar por qué. El contraste ahora es visible:

```
   ✅ INTERFACES: sin límite            ❌ HERENCIA: una sola
   class X implements A, B, C {}        class X extends A, B {}  → no compila
```

La razón: una interfaz solo aporta **obligaciones** (métodos a cumplir, sin código). Podés acumular obligaciones sin conflicto. La herencia, en cambio, trae **código hecho**; si heredaras de dos padres y ambos tuvieran un método con el mismo nombre pero distinto cuerpo, no habría forma de saber cuál usar. Por eso Java permite implementar muchas interfaces, pero heredar de una sola clase.

> 🕳️ A ese conflicto irresoluble de "dos padres con el mismo método" se lo llama **problema del diamante**. Con interfaces no aparece, porque no traen código que pueda chocar.

---

## 4. La validación, en concreto 🟡

El método `esValido` de Whatsapp usaba `matches(...)` con un patrón. Eso es una **expresión regular** (*regex*): un molde que describe la forma que debe tener un texto. `texto.matches(patron)` devuelve `true` si el texto entero encaja.

El patrón del teléfono, desarmado:

```java
"^\\+?\\d{10,15}$"
```

| Símbolo | Significa |
|---|---|
| `^` … `$` | anclan inicio y fin: el texto **entero** tiene que encajar |
| `\\+?` | un `+` **opcional** (`?` = cero o una vez) |
| `\\d{10,15}` | entre 10 y 15 dígitos |

En criollo: **un `+` opcional seguido de entre 10 y 15 dígitos, y nada más.**

*(Las barras van dobles —`\\d`— porque en un texto de Java la barra `\` es especial y hay que escaparla. `\\d` es "un dígito".)*

⚠️ **Ojo, es más estricto de lo que parece:** no acepta espacios ni guiones. Un teléfono humano como `"+54 11 1234-5678"` **lo rechaza**. Si tu sistema recibe números escritos por personas, conviene **limpiar el dato** (sacarle espacios y guiones) antes de validarlo, o el validador te va a rechazar datos válidos. La validación "anda" en las pruebas solo si los datos de prueba vienen ya sin espacios.

---

## 5. 🔴 El antipatrón: `Object` + casts

Llegamos al punto más importante de la unidad. Vamos a ver **una forma de escribir el programa que compila, funciona, da el resultado correcto… y es un mal diseño.** Entender *por qué* es mal diseño es entender qué es el polimorfismo.

### El objetivo

Un método que, dado un notificador, **primero valide** el medio y **solo si es válido, notifique**.

### La forma que anda pero está mal

```java
public class Main {
    public static void main(String[] args) {
        notificar(new NotificadorWhatsapp(), "+5491123456789");
        notificar(new NotificadorMail(),     "+5491123456789");
    }

    // 👇 EL PROBLEMA EMPIEZA EN EL TIPO DEL PARÁMETRO: Object.
    static void notificar(Object n, String contacto) {

        // Como "n" es Object, Java no sabe que sabe validar ni notificar.
        // Para usarlo, se lo FUERZA a los dos tipos con casts:
        IValidadorMedioDeContacto v = (IValidadorMedioDeContacto) n;   // cast 1
        IEstrategiaNotificador    e = (IEstrategiaNotificador) n;      // cast 2

        if (v.esValido(contacto)) {
            e.notificarUsuario(contacto, "Tu pedido fue confirmado!");
        } else {
            System.out.println("Medio de Contacto inválido");
        }
    }
}

// Resultado esperado:
//     NotificadorWhatsapp +5491123456789     (un teléfono es válido para Whatsapp → notifica)
//     Medio de Contacto inválido             (ese teléfono NO es un mail válido → no notifica)
```

> **`Object`** es el tipo más general: **todo** en Java es un `Object`. Declarar un parámetro `Object` es decir "acá entra cualquier cosa" y renunciar a que el compilador sepa qué es.
> **Cast** — `(Tipo) objeto` — le dice al compilador "confiá, tratá esto como si fuera de este Tipo". Es una promesa **tuya** que el compilador **no verifica** en el momento.

### Por qué compila y por qué "anda"

Compila porque el cast es una promesa que el compilador acepta sin chequear. Y anda porque, en este caso, la promesa era cierta: los objetos que le pasamos sí implementan las dos interfaces.

### 🔴 Por qué está mal

El problema es el día que la promesa sea falsa:

```java
// Esto COMPILA PERFECTO. Un Donante es un Object, como todo.
notificar(new Donante("Ana", "Pérez", "12345678"), "+5491123456789");
```

Un `Donante` no implementa ninguna de las dos interfaces. Pero como el parámetro es `Object`, **el compilador no puede saberlo y deja pasar la línea**. El error no salta al compilar: salta **al ejecutar**, cuando se intenta el cast:

```
Exception in thread "main" java.lang.ClassCastException:
    class Donante cannot be cast to class IValidadorMedioDeContacto
```

> **`ClassCastException`:** el error que Java lanza en ejecución cuando un cast resulta mentira. Rompe el programa en el momento.

El núcleo, en términos de la materia:

> **Con `Object` + casts, le sacaste al compilador la capacidad de protegerte.** El compilador detecta errores **antes** de correr, gratis. Con `Object`, ese chequeo desaparece: cualquier objeto entra, y el error que debería haber saltado al compilar queda escondido hasta que alguien, en producción, dispara esa línea. **Moviste el error de compilación (barato, temprano) a ejecución (caro, tardío).**

En vocabulario de atributos de calidad: sacrificaste **seguridad de tipos** y **detección temprana de errores**, y no ganaste nada a cambio. Es un **trade-off perdido**.

Y hay algo más de fondo: esto **traiciona el polimorfismo**. La Parte 2 se trataba de "tratá a los objetos por su contrato, sin preguntar qué son". Acá recibimos algo sin tipo y lo forzamos a mano — exactamente lo contrario.

### Cómo se hace bien 🔴

**Forma A — pedir el tipo que se necesita, con un genérico.** Si el método necesita algo que valide *y* notifique, pedí exactamente eso:

```java
// <T extends A & B> = "T es un tipo que cumple AMBOS contratos".
static <T extends IEstrategiaNotificador & IValidadorMedioDeContacto>
void notificar(T n, String contacto) {
    // Sin casts. "n" YA es validador y notificador, garantizado.
    if (n.esValido(contacto)) {
        n.notificarUsuario(contacto, "Tu pedido fue confirmado!");
    } else {
        System.out.println("Medio de Contacto inválido");
    }
}

// Ahora esto NO COMPILA (y está bien que no):
//     notificar(new Donante(...), "...");
//     → error DE COMPILACIÓN: Donante no cumple los contratos.
// El error saltó temprano, gratis, antes de correr.
```

**Forma B — una interfaz que combine las dos.** Si "notificar y validar" es un rol con nombre propio, dáselo:

```java
public interface INotificadorValidable
        extends IEstrategiaNotificador, IValidadorMedioDeContacto { }

static void notificar(INotificadorValidable n, String contacto) { ... }
```

*(Una interfaz sí puede `extends` otras interfaces: es herencia de contratos, no de código.)*

| | `Object` + casts | Genérico / interfaz combinada |
|---|---|---|
| ¿Compila con algo inválido? | Sí 😬 | **No** ✅ |
| ¿Cuándo detectás el error? | En ejecución (tarde) | Al compilar (temprano) |
| Seguridad de tipos | Ninguna | Total |

---

## 📌 Para el parcial, si te preguntan

**▸ ¿Una clase puede implementar varias interfaces? ¿Y heredar de varias clases?**
Puede implementar todas las interfaces que quiera (aportan solo obligaciones, no chocan), pero hereda de una sola clase: la herencia múltiple de clases está prohibida para evitar el problema del diamante, que surgiría si dos padres aportaran implementaciones distintas del mismo método.

**▸ ¿Por qué separar "notificar" y "validar" en dos interfaces?**
Porque son responsabilidades independientes. Separadas, cada clase decide qué contratos firma y no queda obligada a implementar métodos que no le competen; el diseño resulta más flexible y mantenible.

**▸ ¿Qué problema tiene recibir un `Object` y hacer casts?**
Se pierde la seguridad de tipos: el compilador acepta cualquier objeto, incluso uno que no cumpla lo necesario, y el error recién aparece en ejecución como `ClassCastException`. Se traslada la detección del error de compilación —temprana y segura— a ejecución —tardía y peligrosa—, sin ningún beneficio a cambio.

**▸ ¿Cómo se pide bien un objeto que cumpla dos interfaces?**
Con un genérico de cota de intersección `<T extends A & B>`, o con una interfaz que extienda a las dos. En ambos casos el chequeo lo hace el compilador y un objeto inválido no llega a compilar.

**▸ Justificá la elección en términos de trade-off.**
Elijo el genérico (o la interfaz combinada) porque priorizo **seguridad de tipos** y **detección temprana de errores**: el compilador rechaza lo inválido antes de correr. El costo es una firma más verbosa, que vale la pena frente al riesgo de un `ClassCastException` en producción.

---

## ✅ Checkpoint — Parte 3

Respondelas **sin mirar el apunte**.

1. En la §1, un `NotificadorSms` mandó un mensaje a un mail sin quejarse. ¿Qué responsabilidad faltaba? *(§1)*
2. ¿Por qué conviene una interfaz nueva para validar, en vez de agregar `esValido` a la que ya existía? *(§2)*
3. ¿Cómo se escribe que una clase implementa dos interfaces? ¿A qué la obliga? *(§3)*
4. ¿Por qué se pueden implementar muchas interfaces pero heredar de una sola clase? *(§3)*
5. El regex `^\+?\d{10,15}$` rechaza `+54 11 1234-5678`. ¿Por qué? ¿Cómo lo resolverías? *(§4)*
6. `notificar(Object n, ...)` compila aunque le pases algo que no sea notificador. ¿Cuándo explota, y con qué error? *(§5)*
7. ¿Qué atributo de calidad se pierde con `Object` + casts? ¿Qué significa "mover el error de compilación a ejecución"? *(§5)*
8. Escribí la firma de un método que reciba algo que sepa notificar **y** validar, sin `Object` ni casts. *(§5)*
9. ¿Por qué se dice que el enfoque con `Object` "traiciona el polimorfismo"? *(§5)*
10. Justificá, en términos de trade-off, por qué el genérico es mejor que el `Object`. *(§5, 📌)*

*(Las respuestas van en el complemento de la unidad.)*

---

## Lo que viene en la Parte 4

Ya tenés las dos herramientas que dan polimorfismo —herencia e interfaces— y viste cuándo cada una encaja. La Parte 4 cierra la unidad con una **tercera** forma de relacionar clases, muchas veces mejor que heredar: la **composición** (una clase que *tiene* a otra, en vez de *ser* otra). Ahí aparece el criterio **"es-un" vs. "tiene-un"**, un caso donde heredar parece razonable y es un error, y —lo más importante para el parcial— cómo se **justifica una decisión de diseño**.

---

**FIN DE LA PARTE 3 — Múltiples interfaces y el contrato roto**
