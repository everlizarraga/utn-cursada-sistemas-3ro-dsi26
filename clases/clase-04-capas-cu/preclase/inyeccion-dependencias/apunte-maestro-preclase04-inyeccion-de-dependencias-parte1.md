# 📘 Apunte Maestro — Preclase 04 · Inyección de Dependencias (Parte 1: patrón de diseño)

> Segunda pieza del material previo de la clase 4 (viene de *Mockeo*). Esta parte cubre DI como **patrón de diseño**: el problema, las tres formas de obtener una dependencia y sus trade-offs. La Parte 2 cubre DI como patrón arquitectural y las alternativas en otras tecnologías.

---

## 1. El problema 🔴

Tenemos una `ListaDeCorreo`: su responsabilidad es mandarle un mail a cada uno de sus usuarios. Para *enviar* el mail no lo hace ella misma — se apoya en otro objeto, un `mailSender`:

```
clase ListaDeCorreo

  constructor(usuarios) { ... }

  metodo enviarMail(mail)
    usuarios.forEach(usuario => mailSender.enviarMail(usuario, mail))
    //                          └────────┬─────────┘
    //                          usa un objeto "mailSender" que sabe mandar mails
```

Todo bien, **salvo por una pregunta**: suponiendo que ese objeto `mailSender` existe, **¿cómo llega `ListaDeCorreo` a conocerlo?** ¿De dónde lo saca? Hay tres respuestas posibles, y elegir bien es de lo que trata todo este apunte:

1. Tener **una única instancia** de su clase, accesible **globalmente** (un Singleton).
2. Obtenerlo a través de **otro objeto que lo provea** (un Service Locator).
3. **Parametrizar** la clase para que el `mailSender` **le llegue desde afuera** (Inyección de Dependencias).

### 1.1. Un poco de terminología 🟡

Antes de comparar, dos palabras. Casi ningún componente resuelve todo solo: necesita a otros. A esos otros de los que depende para cumplir su responsabilidad los llamamos **dependencias**. En objetos, una dependencia es simplemente **otro objeto** al que se accede (típicamente por una variable de instancia o global).

Alguna bibliografía nombra las dos puntas así:

- **Cliente:** el objeto que necesita de otros para delegar parte de su trabajo → acá, `ListaDeCorreo`.
- **Servicio:** el objeto del que se depende (la dependencia) → acá, `mailSender`.

> 🕳️ **Madriguera — inversión de control (por ahora)**
> Vas a escuchar "DI es un caso de *inversión de control*". Es cierto y lo vemos en §4 (opción 3). Por ahora seguí con el problema concreto.
> *Volvé al camino.*

---

## 2. Opción 1 — Singleton (instancia global) 🔴

La primera salida: que exista **una sola** instancia del sender, accesible desde cualquier punto de la aplicación — un objeto **global**.

Según la tecnología esto es más o menos complejo. En Scala es una primitiva del lenguaje: alcanza con declarar un `object`:

```scala
object MailSenderPosta extends MailSender {
  // ...implementación...
}
// "object" en Scala = una única instancia global de la clase (un Singleton listo, sin escribir getInstance a mano).
```

Y `ListaDeCorreo` lo referencia directo, como una variable global:

```
metodo enviarMail(mail)
  usuarios.forEach(usuario => MailSenderPosta.enviarMail(usuario, mail))
  //                          └──────┬───────┘
  //                          va a buscar el objeto global por su nombre
```

En lenguajes sin esa primitiva (como Java), el mismo patrón se arma a mano con un método de clase que devuelve siempre la misma instancia:

```java
// >> WhatsAppSender  (el mismo patrón Singleton, escrito a mano)
private static WhatsAppSender instance = null;   // la única instancia, guardada en un campo estático

public static WhatsAppSender getInstance() {
    if (instance == null)                 // ¿todavía no se creó?
        instance = new WhatsAppSender();  // se crea una vez (más toda su configuración)
    return instance;                      // de acá en más, siempre devuelve la misma
}
// Uso desde el cliente:  WhatsAppSender.getInstance().enviar(interesado, "Ha ocurrido un incidente");
```

**Funciona y es sencillo. ¿Qué problemas trae?**

- **Acoplamiento fuerte.** `ListaDeCorreo` queda pegada a `MailSenderPosta` (la clase concreta). No hay forma de que use *otra cosa* en su lugar.
- **No es testeable unitariamente.** Como no podés sustituir el sender, no podés probar la lista **aislada** del comportamiento real del sender.
- **Peor: efectos reales en cada test.** El sender de verdad *manda mails de verdad*. Cada vez que corra una prueba automatizada, **llenás de spam** las casillas de gente real. Moraleja del propio ejemplo: **no es testeable.**

> **Para el parcial, si te preguntan** — *¿Por qué un Singleton dificulta el testing?*
> Porque acopla al cliente con la implementación concreta y global: no podés reemplazar la dependencia por un impostor para probar el cliente aislado. Y si esa dependencia tiene efectos reales (mandar un mail, pegarle a una API), cada corrida del test los ejecuta de verdad.

---

## 3. Opción 2 — Service Locator 🔴

Segunda salida: **delegar la provisión** de la dependencia en otro objeto cuya responsabilidad es conocer **todas** las dependencias del sistema. El cliente no conoce al sender: se lo **pide** al locator.

```
metodo enviarMail(mail)
  usuarios.forEach(usuario =>
    ServiceLocator.mailSender.enviarMail(usuario, mail)   // le pido al locator el mailSender
  )
```

A veces aparece con acceso por nombre (perdiendo algo de chequeo de tipos estático):

```
usuarios.forEach(usuario =>
  ServiceLocator.get("mailSender").enviarMail(usuario, mail)   // se lo pido por su nombre/clave
)
```

Lo que introdujimos es una **indirección** que **desacopla** a `ListaDeCorreo` de `MailSenderPosta`. Ahora **el Singleton es el `ServiceLocator`**, no cada dependencia: las clases de dominio ya no quedan pegadas a implementaciones concretas, y tenemos **un único punto** para cambiar la configuración de la app.

El locator expone métodos para registrarse/configurarse:

```
ServiceLocator.set("mailSender", new MailSenderPosta)   // registro qué objeto responde a "mailSender"
```

Es **stateful** (tiene estado mutable): acapara la configuración de la aplicación. La idea **no** es que los componentes muten esa config en runtime (sería una variable global con todos sus problemas), sino **fijarla una sola vez**: al iniciar la app, o al preparar el contexto de los tests.

Y acá está la ganancia clave: **permite mockeo**. Para testear, registrás un impostor en lugar del objeto real:

```
val mailSenderMock = mock(MailSenderPosta.class)   // creo un impostor (ver apunte de Mockeo)
ServiceLocator.set("mailSender", mailSenderMock)   // lo registro en el locator para el test
val listaDeCorreo = new ListaDeCorreo(...)
listaDeCorreo.enviarMail(...)
verify(mailSenderMock).enviarMail(...)             // verifico que la lista efectivamente lo llamó
```

> 🕳️ **Madriguera — `verify(...)`**
> Es de Mockito: chequea que al mock **se lo llamó** (y cuántas veces), en vez de comparar un valor devuelto. En el apunte de Mockeo trabajamos con `when/thenReturn`; `verify` es la otra cara (comprobar interacciones). No hace falta para seguir acá.
> *Volvé al camino.*

**¿Es buena solución?** Sí, y es **particularmente útil cuando las dependencias son propias del contexto de la aplicación** — por ejemplo, el motor de persistencia: casi siempre usás el mismo, y solo lo cambiás según el entorno (desarrollo, testing, producción).

**La contra:** ahora **muchos componentes quedan acoplados al contexto de la aplicación** — todos tienen que conocer al `ServiceLocator`, que termina siendo una **"bolsa de gatos"**, y trazar el grafo de dependencias de un objeto se vuelve menos evidente. No es la mejor opción cuando las dependencias son **más que solo** propias del contexto donde se usa el componente.

---

## 4. Opción 3 — Inyección de Dependencias 🔴

La solución **más simple de todas**: **parametrizar** aquello de lo que se depende. En vez de que el cliente vaya a *buscar* su dependencia (contra el Singleton o contra el locator), **alguien se la inyecta desde afuera**, y el cliente se la guarda.

```
clase ListaDeCorreo

  constructor(usuarios, mailSender) { ... }   // ← el mailSender ENTRA por parámetro

  metodo enviarMail(mail)
    usuarios.forEach(usuario =>
      mailSender.enviarMail(usuario, mail)    // usa el que le inyectaron; no lo busca en ningún lado
    )
```

No es nada del otro mundo: **pateamos la pelota**, delegando en otro componente el problema de proveer la dependencia. Por eso se dice que DI es un caso particular de la **inversión de control**, plasmada en el **principio de Hollywood**: *"no nos llames, nosotros te llamamos"*. En vez de que tu objeto salga a pedir lo que necesita, se lo entregan.

> 🕳️ **Madriguera — patrones creacionales**
> Estas ideas (quién construye los objetos y cómo) se retoman al ver **patrones creacionales** más adelante en la materia. Acá alcanza con "la dependencia entra desde afuera".
> *Volvé al camino.*

### 4.1. Por constructor o por setter 🔴

Hay **dos formas** de inyectar (lo señala Fowler). No dan lo mismo:

**Por setter** — muchas veces la más simple, y algunas tecnologías la fuerzan:

```
listaDeCorreo = new ListaDeCorreo()   // primero se crea el objeto...
listaDeCorreo.mailSender = ...         // ...y después se le van seteando las dependencias
listaDeCorreo.usuarios = ...
```

Problema: entre el `new` y los setters, **el objeto existe pero está a medio configurar** → se pueden instanciar **objetos inconsistentes**. Y es **incompatible con la inmutabilidad** (si vas a mutar el sender después, el objeto no puede ser inmutable). Pasa a ser responsabilidad del programador no dejar el objeto a medias.

**Por constructor** — la dependencia entra al construir:

```
listaDeCorreo = new ListaDeCorreo(usuarios, mailSender)   // nace completo y consistente
```

Asegura que **se creen objetos completos y consistentes** desde el vamos, y es **compatible con objetos inmutables**.

> **Para el parcial, si te preguntan** — *¿Inyección por constructor o por setter?*
> Por **constructor**: garantiza objetos completos y consistentes desde su creación y es compatible con la inmutabilidad. La inyección por **setter** es más simple (y a veces la tecnología la obliga), pero permite construir objetos a medio configurar —inconsistentes— y rompe la inmutabilidad. Se prefiere constructor salvo que la tecnología fuerce setter.

### 4.2. Y acá se cierra el círculo con el mockeo 🔴

Con la dependencia entrando desde afuera, el test es directo: le paso un **impostor** en lugar del sender real.

```
mailSenderMock = mock(MailSenderPosta.class)              // un impostor que cumple la interfaz
listaDeCorreo  = new ListaDeCorreo(.., mailSenderMock)    // se lo INYECTO por constructor
listaDeCorreo.enviarMail(...)
verify(mailSenderMock).enviarMail(...)                    // verifico la interacción
```

Esto es exactamente lo que hicimos en el apunte de Mockeo: ahí `Viaje` recibía sus dos calculadoras **como parámetros del método** y les pasábamos mocks. Es la misma idea —la dependencia **le llega desde afuera**— en su variante por **parámetro de método** en vez de por constructor. **Sin inyección, no hay mockeo posible:** por eso el Singleton (que va a buscar su dependencia solo) es tan difícil de testear.

### 4.3. ¿Qué *no* es la inyección de dependencias? 🟡

Cuidado con la definición fácil. La respuesta más votada de StackOverflow dice que DI es "pasar la referencia por parámetro al constructor en vez de obtenerla adentro, y listo". **Está incompleta:**

- Existe también la inyección **por setter** (no es solo "por parámetro del constructor").
- Hacer DI no se reduce a pasar la dependencia por parámetro, sino a **entender por qué** tiene que pasarse por parámetro.
- Sin tener en mente al **Singleton** y al **Service Locator** como alternativas, no se puede valorar de verdad *por qué* conviene inyectar.
- Si fuera tan trivial, no existirían **frameworks específicos** (y a veces complejos) para soportarla.

En una línea: DI es simple, pero "simple" no es lo mismo que "pasar un parámetro sin entender el porqué".

---

## 5. Las tres opciones, lado a lado 🔴

Diagrama de quién obtiene qué:

```
 SINGLETON            SERVICE LOCATOR              INYECCIÓN DE DEPENDENCIAS
 ─────────            ───────────────              ─────────────────────────
 Cliente                 Cliente                        (alguien externo)
    │                       │                                  │ inyecta
    │ va y busca            │ le pide a…                        ▼
    ▼                       ▼                               Cliente
 [instancia            ServiceLocator                         ▲
  global]                  │ devuelve                         │ ya la tiene guardada
                           ▼                              [dependencia]
                       [dependencia]

 el cliente SALE a buscar  │  el cliente PIDE          │  al cliente le LLEGA de afuera
```

Analogía rápida: es la diferencia entre **ir vos mismo a la despensa** a buscar el ingrediente (Singleton), **pedírselo a un mayordomo** que sabe dónde está todo (Service Locator), o que **alguien te lo ponga en la mano** justo antes de cocinar (Inyección).

Tabla de trade-offs (`A`, instancia de la clase 1, necesita a `B`, instancia de la clase 2, para hacer la tarea X):

| | **Singleton** | **Service Locator** | **Inyección de Dependencias** |
|---|---|---|---|
| ¿Cómo obtiene a `B`? | va y pide a la clase 2 su instancia global | le pide al locator alguien que haga X | la recibe **desde afuera**, típicamente en el constructor |
| Qué recibe | siempre la misma instancia `B` | `B` u otro objeto que cumpla la misma interfaz | `B` u otro que cumpla la misma interfaz |
| Acoplamiento | **fuerte** entre clase 1 y clase 2 | al **contexto** (todos conocen el locator) | **bajo** (a la interfaz, no a la implementación) |
| ¿Permite mockeo? | **difícil** de mockear `B` | **sí** (registrás un mock en el locator) | **sí** (le pasás el mock) |
| Contra principal | global, no testeable, efectos reales | "bolsa de gatos": componentes acoplados al contexto | a veces tedioso proveer la inyección |
| Combinable | — | se **complementa** con DI | se **combina** con las anteriores |

> **Para el parcial, si te preguntan** — *Compará las tres formas de obtener una dependencia.*
> **Singleton:** el cliente va a buscar una instancia global → acoplamiento fuerte, difícil de mockear, no testeable. **Service Locator:** se la pide a un objeto que conoce todas las dependencias → desacopla de la implementación concreta y permite mockeo, pero acopla al contexto de la app. **Inyección de Dependencias:** la dependencia le llega desde afuera (constructor/setter) → el menor acoplamiento y el más testeable; se combina con las anteriores. El criterio de decisión es **acoplamiento vs testeabilidad**, y DI gana cuando la dependencia no es puramente del contexto global.

---

## Info operativa

- **Terminología del profe:** *dependencia*, *cliente*/*servicio*, *Singleton*, *Service Locator*, *inyección por constructor/por setter*, *inversión de control*.
- **Conexiones de esta preclase:** hacia atrás, **Mockeo** (la inyección es lo que lo habilita); hacia adelante, **Biblioteca vs Framework** (la inversión de control / principio de Hollywood es la clave de esa distinción). La Parte 2 conecta con **Spring**, que es el stack del TPA.

---

## Checkpoint (sin respuestas)

1. `ListaDeCorreo` necesita un `mailSender`. Nombrá las tres formas de que llegue a conocerlo.
2. ¿Qué es una "dependencia"? ¿Y quién es el *cliente* y quién el *servicio* en el ejemplo?
3. ¿Por qué un `mailSender` obtenido como Singleton hace que la lista no sea testeable? Mencioná el problema de los *efectos*.
4. ¿Qué desacopla el Service Locator y qué nuevo acoplamiento introduce a cambio?
5. ¿En qué momento se fija la configuración del Service Locator, y por qué no en runtime?
6. ¿Qué principio sustenta a la Inyección de Dependencias y cómo se enuncia?
7. Inyección por constructor vs por setter: ¿qué garantiza cada una y por qué se prefiere una?
8. Explicá por qué "DI = pasar la dependencia por parámetro del constructor" es una definición incompleta.
9. Conectá con el apunte de Mockeo: ¿qué tienen que ver la inyección y la posibilidad de mockear? ¿Por qué el Singleton es el más difícil de los tres para testear?

---

**Lo que viene — Parte 2:** DI como **patrón arquitectural** (el contenedor de dependencias, Spring/Guice) y las **alternativas** en otras tecnologías (Cake Pattern de Scala, Open classes de Ruby/JS), más las conclusiones.

**FIN DEL APUNTE MAESTRO — Preclase 04 · Inyección de Dependencias (Parte 1)**
