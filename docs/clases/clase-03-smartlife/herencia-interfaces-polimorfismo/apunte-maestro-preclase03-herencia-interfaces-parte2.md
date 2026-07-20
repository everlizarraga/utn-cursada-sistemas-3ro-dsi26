# 📘 APUNTE MAESTRO — Preclase 03 · Parte 2
## Interfaces y Polimorfismo

---

## 🧭 Cómo leer esto

Continúa la unidad de la **Parte 1 (Herencia)**. Se apoya en lo de esa parte y en las preclases anteriores (POO y Java), que se dan por sabidos. Todo el material está unificado acá: no remite a ningún video ni repo.

Cada bloque de código está comentado, con su **resultado esperado como comentario**.

**Leyenda:** 🔴 central/evaluable · 🟡 secundario · 🟢 al pasar · 🕳️ madriguera · 📌 respuesta modelo de parcial.

---

## 1. El punto de partida 🔴

En la Parte 1, este `for` recorría una lista de donaciones y, con **un solo llamado** —`d.obtenerPuntaje()`—, obtenía un resultado distinto según el tipo real de cada donación (dinero o vianda). Ahí lo dejamos nombrado sin explicar: eso era **polimorfismo**, y lo lográbamos por **herencia**.

En esta parte le ponemos nombre formal y lo conseguimos por el otro camino: las **interfaces**. El resultado es el mismo —un llamado que se resuelve distinto según el objeto—, pero la herramienta es otra, y sirve para casos donde la herencia no encaja.

---

## 2. Qué es el polimorfismo 🔴

**Polimorfismo** es poder tratar a objetos de clases distintas **como si fueran del mismo tipo**, e invocar el mismo método sobre todos ellos **sin conocer la clase concreta de cada uno**. Cada objeto responde según su propia implementación.

La palabra viene del griego: *poli* (muchas) + *morfo* (formas). **Muchas formas de responder al mismo mensaje.**

La condición para que dos objetos se puedan tratar igual es que **compartan un tipo común**. En la Parte 1 ese tipo común era la superclase `Donacion`. La otra manera de darles un tipo común es una **interfaz**:

```
                    ╔═══════════════════════╗
                    ║     «interface»       ║
                    ║       IAccion         ║
                    ╠═══════════════════════╣
                    ║  + ejecutar(): void   ║
                    ╚═══════════════════════╝
                              △
                              ┊  (implementan el contrato)
              ┌───────────────┼───────────────┐
     ┌───────────────┐ ┌───────────────┐ ┌───────────────┐
     │     Bicho     │ │     Robot     │ │    Mascota    │
     ├───────────────┤ ├───────────────┤ ├───────────────┤
     │ + ejecutar()  │ │ + ejecutar()  │ │ + ejecutar()  │
     └───────────────┘ └───────────────┘ └───────────────┘
```

`Bicho`, `Robot` y `Mascota` no tienen nada en común entre sí, salvo que **las tres implementan `IAccion`**. Eso alcanza para tratarlas por igual: podés pedirles `ejecutar()` sin saber cuál es cuál, y cada una responde a su modo.

> **La flecha** es **punteada** con triángulo hueco: significa **realización** ("implementa esta interfaz"). Distinta de la de herencia de la Parte 1, que era **llena**. Punteada = implementa una interfaz; llena = hereda de una clase.

---

## 3. Qué es una interfaz 🔴

Una **interfaz** es un **contrato**: declara **qué se puede hacer** (una lista de métodos), sin decir **cómo se hace**. Escribe solo la **firma** de cada método —nombre, parámetros, tipo que devuelve— y nada más.

Tres propiedades que la definen:

**a) Se implementa, no se hereda.** En la Parte 1, una clase heredaba de otra con `extends`. Una interfaz es distinta: una clase **la implementa** con `implements`, y eso la **obliga** a escribir el cuerpo de todos los métodos del contrato. Si falta uno, no compila.

**b) No tiene cuerpo.** Los métodos de una interfaz terminan en `;`, no en `{ }`. Declaran el "qué" y se callan el "cómo".

**c) No se instancia.** No podés hacer `new` de una interfaz: no hay nada que ejecutar, sus métodos están vacíos por diseño. Existe para que **otras clases** la cumplan.

*(Una clase puede, además, implementar varias interfaces a la vez; eso se ve en la Parte 3.)*

---

## 4. El caso: notificar por varios canales 🔴

El sistema tiene que **notificar** a un donante cuando pasa algo, y puede hacerlo por distintos canales —correo, SMS, WhatsApp—. Cada canal notifica a su manera, pero todos hacen lo mismo desde afuera: *notificar*. Es el caso de libro para una interfaz.

### El contrato

```java
package ar.edu.utn.frba.dds.model.notificador;

public interface IEstrategiaNotificador {
    // Firma y nada más. Termina en ";" → no hay cuerpo.
    //   boolean          → lo que promete devolver
    //   (String, String) → medio de contacto y mensaje
    boolean notificarUsuario(String medioDeContacto, String mensaje);
}
```

### Las clases que lo firman

Van en un sub-package `impl` (implementación). Cada una resuelve el contrato a su modo:

```java
package ar.edu.utn.frba.dds.model.notificador.impl;

import ar.edu.utn.frba.dds.model.notificador.IEstrategiaNotificador;

public class NotificadorMail implements IEstrategiaNotificador {
    @Override
    public boolean notificarUsuario(String medioDeContacto, String mensaje) {
        System.out.println("NotificadorMail " + mensaje);   // muestra el mensaje
        return true;
    }
}
```

```java
package ar.edu.utn.frba.dds.model.notificador.impl;

import ar.edu.utn.frba.dds.model.notificador.IEstrategiaNotificador;

public class NotificadorWhatsapp implements IEstrategiaNotificador {
    @Override
    public boolean notificarUsuario(String medioDeContacto, String mensaje) {
        System.out.println("NotificadorWhatsapp " + medioDeContacto);   // muestra el medio
        return true;
    }
}
```

```java
package ar.edu.utn.frba.dds.model.notificador.impl;

import ar.edu.utn.frba.dds.model.notificador.IEstrategiaNotificador;

public class NotificadorSms implements IEstrategiaNotificador {
    @Override
    public boolean notificarUsuario(String medioDeContacto, String mensaje) {
        System.out.println("NotificadorSms " + medioDeContacto);
        return true;
    }
}

// Las tres firman el MISMO contrato (misma firma) y resuelven DISTINTO por dentro:
// Mail muestra el mensaje; Whatsapp y Sms, el medio. Ese es el corazón del polimorfismo.
```

*(`@Override` es la misma annotation de la Parte 1: acá marca que el método cumple un método del contrato. Si te equivocás en la firma, el compilador te frena.)*

### El diagrama

```
        ╔═══════════════════════════════════════╗
        ║             «interface»               ║
        ║        IEstrategiaNotificador         ║
        ╠═══════════════════════════════════════╣
        ║ + notificarUsuario(String,            ║
        ║                    String): boolean   ║
        ╚═══════════════════════════════════════╝
                          △
          ┌───────────────┼───────────────┐
┌──────────────────┐ ┌───────────────┐ ┌──────────────────┐
│  NotificadorMail │ │NotificadorSms │ │NotificadorWhatsapp│
└──────────────────┘ └───────────────┘ └──────────────────┘
```

---

## 5. El polimorfismo en acción: el `Main` 🔴

```java
import ar.edu.utn.frba.dds.model.notificador.IEstrategiaNotificador;
import ar.edu.utn.frba.dds.model.notificador.impl.NotificadorMail;
import ar.edu.utn.frba.dds.model.notificador.impl.NotificadorSms;
import ar.edu.utn.frba.dds.model.notificador.impl.NotificadorWhatsapp;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Lista del tipo de LA INTERFAZ: adentro conviven tres clases distintas,
        // porque las tres implementan IEstrategiaNotificador.
        // (Compará con la Parte 1: allá la lista era del tipo de la SUPERCLASE.)
        List<IEstrategiaNotificador> notificadores = List.of(
                new NotificadorMail(),
                new NotificadorSms(),
                new NotificadorWhatsapp()
        );

        String medio   = "+5491123456789";
        String mensaje = "Tu donación fue registrada!";

        // forEach recorre la lista; por cada elemento ejecuta el mismo llamado.
        notificadores.forEach(notificador ->
                notificador.notificarUsuario(medio, mensaje)
        );
    }
}

// ¿CÓMO FUNCIONA?
// En "notificador.notificarUsuario(...)", lo único que se sabe es que
// "notificador" es un IEstrategiaNotificador. Cuál de las tres clases es,
// no importa: el contrato garantiza que el método existe. Al ejecutar,
// cada objeto corre SU versión. Un mismo llamado, tres comportamientos.
//
// Resultado esperado en la consola:
//     NotificadorMail Tu donación fue registrada!
//     NotificadorSms +5491123456789
//     NotificadorWhatsapp +5491123456789
```

Es lo mismo que viste en la Parte 1 con `List<Donacion>` y `obtenerPuntaje()` — un llamado, varios comportamientos, elegidos por el objeto real. Allá el tipo común lo daba la **superclase**; acá, la **interfaz**. Dos caminos, un mismo polimorfismo.

### Qué gana el diseño 🟡

Agregar un canal nuevo (Telegram, push, el que sea) es escribir **una clase nueva** que implemente `IEstrategiaNotificador` y sumarla a la lista. **No se toca** ni la interfaz ni las clases que ya andaban ni el `forEach`. Extendés el sistema sin modificar lo que ya funcionaba — eso es lo que la materia llama buena **extensibilidad**.

---

## 6. Interfaz o herencia: una primera intuición 🟡

Ya tenés las dos herramientas que dan polimorfismo. La diferencia de fondo, que se desarrolla en la Parte 4:

- La **herencia** (Parte 1) sirve cuando una clase **es un** caso particular de otra y comparte su estado y código (`DonarVianda` **es una** `Donacion`).
- La **interfaz** sirve cuando lo único que comparten es **saber hacer algo**, sin ser parientes ni tener nada más en común (`NotificadorMail` y `NotificadorSms` no son "tipos de" una misma cosa; solo **saben notificar**).

Cuando lo que une a un grupo de clases es un **comportamiento**, no un parentesco, la interfaz es la herramienta.

---

## 📌 Para el parcial, si te preguntan

**▸ ¿Qué es una interfaz?**
Un contrato que declara un conjunto de métodos que una clase se compromete a implementar. Define sus firmas —nombre, parámetros y tipo de retorno— sin la implementación, para que distintas clases compartan el mismo comportamiento resolviéndolo cada una a su modo. No se instancia y no se hereda: se implementa, con `implements`.

**▸ ¿Qué es el polimorfismo?**
Poder tratar a objetos de distintas clases como si fueran del mismo tipo, siempre que compartan un tipo común (una interfaz o una superclase), e invocar el mismo método sin conocer la clase concreta: cada objeto responde según su propia implementación.

**▸ ¿Por qué una interfaz habilita el polimorfismo?**
Porque garantiza en compilación que todo objeto que la implemente responde a los métodos del contrato. Eso permite declarar variables y listas con el tipo de la interfaz y operar sobre ellas sin saber qué clase concreta hay detrás; la elección del comportamiento la resuelve el objeto en ejecución.

**▸ Diferencia entre `implements` y `extends`.**
`implements` se usa con interfaces: obliga a escribir el cuerpo de todos sus métodos, y una clase puede implementar varias. `extends` se usa con clases: la subclase reutiliza el código del padre, y solo puede tener uno.

---

## ✅ Checkpoint — Parte 2

Respondelas **sin mirar el apunte**.

1. ¿Qué tienen que compartir dos objetos para poder tratarse polimórficamente? Nombrá las dos formas de lograrlo. *(§2)*
2. ¿Por qué una interfaz no se puede instanciar? *(§3)*
3. ¿En qué se diferencia `implements` de `extends`? *(§3)*
4. En el contrato, el método termina en `;`. ¿Qué significa eso? *(§3, §4)*
5. Las tres clases notificadoras tienen la misma firma. ¿Por qué imprimen cosas distintas? *(§4)*
6. La lista es `List<IEstrategiaNotificador>` y contiene tres clases distintas. ¿Por qué Java lo permite? *(§5)*
7. `notificador.notificarUsuario(...)` es una línea; da tres salidas. ¿Quién decide cuál corre, y cuándo? *(§5)*
8. ¿Qué hay que tocar para agregar un canal nuevo? ¿Qué no? *(§5)*
9. En la Parte 1 el polimorfismo salía por herencia; acá, por interfaz. ¿Qué aportaba el tipo común en cada caso? *(§2, §5)*
10. ¿Cuándo conviene una interfaz antes que herencia, según la intuición de la §6? *(§6)*

*(Las respuestas van en el complemento de la unidad.)*

---

## Lo que viene en la Parte 3

El ejemplo tiene un agujero: al `NotificadorMail` se le puede pasar un teléfono, y al `NotificadorSms` un mail, y nadie se queja. En la Parte 3 aparece una **segunda interfaz** para validar, la posibilidad de que una clase **implemente varias interfaces a la vez**, y —lo más importante— una forma de escribir el programa que **compila, funciona y es un mal diseño**: recibir un `Object` y hacer *casts*. Ese caso es el mejor ejemplo de un **trade-off entre atributos de calidad**, que es el eje de la clase 3.

---

**FIN DE LA PARTE 2 — Interfaces y Polimorfismo**
