# 📘 Apunte Maestro — Preclase 04 · Inyección de Dependencias (Parte 2: arquitectural y alternativas)

> Cierre del tema DI. Viene de la Parte 1 (las tres formas de obtener una dependencia). Acá: DI como **patrón arquitectural** (el contenedor), las **alternativas** en otras tecnologías, y las conclusiones. El material de origen marca casi todo esto como *BONUS* / "no nos interesa tanto en diseño" — va **cubierto igual**, pero con importancia baja (🟡🟢) y sin bloques de parcial: es panorama, no núcleo evaluable.

---

## 1. Contexto: por qué tanto ruido con DI 🟡

Alrededor de Java se habla muchísimo de **inyección de dependencias** (DI, *Dependency Injection*) y, para sumar confusión, de **inversión de control**. Rara vez hay una definición aceptada por todos, ni dos ingenieros que usen el término igual. Aparecen frameworks llamados **contenedores** (por ejemplo, **Spring**) diseñados para soportarla, y sin embargo muchos que los usan no terminan de entender su utilidad. Y un vistazo a otros lenguajes sugiere que DI ahí es, o parece ser, inexistente.

De ahí las preguntas que ordenan el tema: ¿qué es DI?, ¿cómo se relaciona con la inversión de control?, ¿es realmente necesaria?, ¿qué es un contenedor? La Parte 1 respondió las primeras (DI como patrón de diseño); esta parte responde la del **contenedor**.

---

## 2. DI como patrón arquitectural 🟡

A veces la inyección de dependencias se usa no ya para un objeto puntual, sino como **patrón arquitectural** para vincular **todos** los componentes de la aplicación. En diseño esto es un **BONUS** — lo vemos breve.

El elemento central es el **contenedor de dependencias**: un componente arquitectural responsable de dos cosas:

- **guardar el estado de la aplicación** (parecido a un Service Locator), y
- **construir todos los componentes del sistema**, describiendo de forma **declarativa** cómo se instancian y qué dependencias tiene cada uno.

Ese contenedor lo provee un **framework de inyección de dependencias**. Los más comunes en la JVM (al momento de escribirse el material) son **Spring** y **Guice**, además de una **especificación formal que los estandariza**.

> 🕳️ **Madriguera — Guice y la spec JSR-330**
> Guice es otro contenedor DI de la JVM (de Google); y hay una especificación estándar de la API de inyección (`javax.inject`) que unifica el vocabulario entre contenedores. Solo nombrados en el material, no desarrollados.
> *Volvé al camino.*

Y un mito para matar: algunos contenedores usan **XML** para definir su configuración, pero eso es una **particularidad de esas implementaciones**. Dicho fuerte: **¡DI no tiene nada que ver con archivos XML!**

> 🔗 **Puente con el TPA.** El contenedor descripto acá es, en concreto, **lo que Spring hace por vos**: vos declarás los componentes y sus dependencias, y Spring los construye e inyecta. Es el mismo principio de la Parte 1 (la dependencia llega desde afuera), llevado a toda la app y automatizado por el framework. Lo vas a ver en acción en el stack del TPA.

---

## 3. Alternativas a la inyección de dependencias 🟢

DI **no es la única forma** de desacoplar dos componentes. El material menciona un camino conocido y muestra dos propios de otras tecnologías. Esto es **panorama**: no se evalúa en la materia, pero se cubre para que sepas que existe.

**Otra vía ya conocida — Observer.** El patrón **Observer** también reduce acoplamiento, mediante la metáfora de producir y capturar **eventos**. Es, eso sí, una solución **más compleja** que DI.

Y según la tecnología aparecen otras formas de vincular componentes. Dos ejemplos:

### 3.1. Scala — Cake Pattern 🟢

Se basa en **mixins** para introducir las dependencias. Cada mixin es responsable de construir la dependencia que provee (parecido a un *Abstract Factory*).

Se empieza definiendo la interfaz del proveedor (con un `trait` de Scala):

```scala
trait MailSenderComponent {
  def mailSender : MailSender      // firma para obtener un MailSender
}
```

Una implementación posible, que provee un `MailSenderPosta`:

```scala
class MailSenderComponentPosta extends MailSenderComponent {
  override val mailSender = new MailSenderPosta
}
```

Y la `ListaDeCorreo`: en vez de parametrizarla, se **declara que depende del mixin** `MailSenderComponent`:

```scala
class ListaDeCorreo(usuarios: List[Usuario]) {
  self : MailSenderComponent =>                       // "dependo de este mixin"
  def enviarMail(mail: Mail) =
    usuarios.foreach { usuario =>
      mailSender.enviarMail(usuario, mail)            // lo provee el mixin
    }
}
```

Para incluir la dependencia, se **mezcla** el mixin al crear la instancia (en lugar de inyectar por constructor o setter):

```scala
new ListaDeCorreo() with MailSenderComponentPosta
```

Soluciones análogas valen para otros lenguajes con mixins, como Groovy o Ruby.

> 🕳️ **Madriguera — mixin / trait**
> Un mixin es un mecanismo para "mezclar" comportamiento de varias fuentes en una clase; en Scala se implementa con `trait`. El material lo usa sin desarrollar la teoría de mixins.
> *Volvé al camino.*

### 3.2. Ruby / JavaScript — Open classes 🟢

Se basa en **"pisar" métodos** —de una clase o de una instancia concreta— y se asocia a la idea de *partial mocking*. El ejemplo muestra cómo en JavaScript se puede pisar el método de una clase:

```javascript
class Mailer {
  send() {
    // envio mail
  }
}

class ListaDeCorreo {
  constructor(usuarios) {
    this.usuarios = usuarios
  }

  enviarMail(mail) {
    const mailer = new Mailer();
    usuarios.forEach(usuario =>
      mailer.enviarMail(usuario, mail)
    )
  }
}

var sent = false;
Mailer.prototype.send = function () {   // se "pisa" el método send de TODAS las instancias de Mailer
  sent = true
}

new ListaDeCorreo([new Persona()])
console.log(sent) // => true
```

La idea: al reemplazar (`Mailer.prototype.send`) el método por uno que solo marca `sent = true`, se puede observar el comportamiento sin ejecutar el envío real — un desacople logrado por la flexibilidad del lenguaje, no por inyección.

> ⚠️ El ejemplo, tal como viene, tiene inconsistencias propias (mezcla `usuarios` con `this.usuarios`, y define `Mailer.send()` mientras la lista llama `mailer.enviarMail(...)`). Se muestra para transmitir la **idea** de *open classes*, no como código a copiar.

---

## 4. Conclusiones 🟡

Recapitulando todo el tema:

- La inyección de dependencias es **una técnica de diseño más** para **desacoplar componentes**: parametrizás aquello de lo que un componente depende y proveés una forma de inyectarlo.
- Es **muy simple, y de ahí su poder** — aunque **no siempre es la mejor** (a veces el Service Locator, o un mixin, encajan mejor).
- En otras tecnologías más allá de Java hay técnicas que la **complementan** (Cake Pattern, open classes, etc.).
- Puede pensarse como patrón de diseño **y** como patrón arquitectural (con el concepto de **contenedor**). Su mayor utilidad es como **patrón de diseño**.
- **No tiene nada que ver con archivos de configuración XML.**

> **Para el parcial, si te preguntan** — *En una línea, ¿qué es la inyección de dependencias?*
> Una técnica de diseño para desacoplar componentes: el componente **parametriza** sus dependencias y estas **le llegan inyectadas desde afuera** (por constructor o setter), en lugar de salir a buscarlas él mismo. Es un caso de inversión de control, y su ganancia principal es **menor acoplamiento y mayor testeabilidad**.

---

## Checkpoint (sin respuestas)

1. ¿Cuál es la doble responsabilidad de un **contenedor de dependencias**?
2. ¿En qué se parece un contenedor a un Service Locator y qué agrega respecto de él?
3. Nombrá dos contenedores DI de la JVM. ¿Qué relación tiene esto con Spring y el TPA?
4. Verdadero o falso, y justificá: "para usar inyección de dependencias necesitás archivos XML".
5. Además de DI, ¿qué patrón de diseño ya visto permite desacoplar componentes, y a qué costo?
6. ¿En qué se basan el Cake Pattern (Scala) y las open classes (Ruby/JS) para desacoplar, y por qué son propios de esas tecnologías?
7. Según las conclusiones, ¿cuál es la mayor utilidad de DI: como patrón de diseño o arquitectural?

---

**FIN DEL APUNTE MAESTRO — Preclase 04 · Inyección de Dependencias (Parte 2)**
