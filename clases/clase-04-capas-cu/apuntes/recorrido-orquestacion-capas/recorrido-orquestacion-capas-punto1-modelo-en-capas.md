# 🧭 RECORRIDO — Orquestación de CU en Capas — PUNTO 1: Modelo en Capas

**Unidad:** clase04 · **Densidad global:** 🟡 (base conceptual que sostiene todo lo que sigue)

---

## Sobre este documento

**Qué cubre:** qué es dividir un sistema en capas y por qué se hace; las tres capas mínimas de un sistema de información (Presentación, Dominio/Negocio, Datos) con la responsabilidad de cada una; la regla de dirección entre capas y su porqué; cómo se ve esto en una arquitectura web real; la diferencia entre cliente liviano y cliente pesado; y qué se gana y qué se paga al diseñar en capas.

**Qué NO cubre:** qué clases concretas viven dentro de cada capa (eso es el corazón de la unidad y llega en los Puntos 4 a 6) · cómo se reparte un caso de uso entre capas (Punto 2 en adelante) · persistencia real en bases de datos e interfaces gráficas (se ven más adelante en la materia).

## De dónde venís

- **clase01:** un sistema tiene componentes — un frontend, un backend, una base de datos.
- **preclase03:** acoplamiento (cuánto depende una pieza de otra — menos es mejor) y cohesión (qué tan enfocada está una pieza en una sola cosa — más es mejor), y la idea de que todo diseño compra un atributo de calidad pagando con otro.
- **clase02:** HTTP, el modelo cliente-servidor y qué es una API REST.

Con eso alcanza. Empezamos.

---

## 1. El problema: un sistema entero es demasiado grande para pensarlo junto 🔴

Pensá en cualquier sistema que uses: el sistema de la facultad, un homebanking, una app de delivery. Detrás hay pantallas, reglas de negocio, validaciones, datos guardados, comunicación con otros sistemas. Si intentaras diseñar **todo eso junto, como una sola masa**, no podrías ni empezar: cada decisión tocaría todo lo demás.

**Dividir en capas (layering)** es una de las técnicas más comunes para resolver un problema de diseño complejo: partir el gran problema en niveles apilados, donde cada nivel se ocupa de UNA sola cosa.

> **Analogía — el restaurante.** Un restaurante también es un "sistema" partido en capas: el **salón** (mozos, carta, mesas) es la cara visible con la que interactuás; la **cocina** es donde pasan las reglas del negocio (recetas, tiempos, calidad); la **despensa** es donde se guardan y recuperan los insumos. Vos, cliente, hablás solo con el salón. El salón le pide a la cocina. La cocina le pide a la despensa. **Jamás entrás a la despensa a servirte**, y la despensa no sabe ni le importa qué mesa pidió qué.

Guardá la analogía: el modelo en capas es exactamente eso, con nombres técnicos.

Dos definiciones que arman el concepto:

- **Cada capa es un todo coherente**: tiene **alta cohesión** y cumple **un rol único** en el sistema. La cocina cocina; no cobra, no atiende mesas.
- **Cada capa es un componente** (o, dicho al revés: cada componente de software que diseñes va a pertenecer a alguna capa). Cuando en la clase 01 hablamos de componentes de un sistema, estas capas son la forma de organizarlos puertas adentro del backend.

---

## 2. Las tres capas mínimas 🔴

Un sistema de información puede dividirse, *mínimamente*, en tres capas. Son las que vas a encontrar en la mayoría de los sistemas y las que vas a implementar todo el año:

```
        (usuario)
            │
            ▼
┌───────────────────────────┐
│   PRESENTACIÓN            │  ← la cara del sistema
├───────────────────────────┤
│   DOMINIO / NEGOCIO       │  ← las reglas y las entidades
├───────────────────────────┤
│   DATOS                   │  ← el guardado
└─────────┬─────────┬───────┘
          ▼         ▼
     (base de   (otro medio
      datos)    persistente)
```

Otra forma de dibujar lo mismo — como anillos, con el usuario tocando solo el de afuera:

```
        ┌─────────────────────────────┐
        │  PRESENTACIÓN               │
        │   ┌───────────────────────┐ │
        │   │  DOMINIO / NEGOCIO    │ │
        │   │   ┌────────────────┐  │ │
        │   │   │     DATOS      │  │ │
        │   │   └────────────────┘  │ │
        │   └───────────────────────┘ │
        └──────────────▲──────────────┘
                       │
                   (usuario)
```

Los dos diagramas dicen lo mismo; el primero muestra **el flujo** (quién llama a quién) y el segundo muestra **el alcance** (el usuario solo toca la capa externa; el dato está protegido en el centro). Ahora sí, qué hace cada una:

### 2.1 Capa de Presentación 🔴

Es la capa encargada de **presentar los datos al usuario** — es con la que el usuario interactúa.

El detalle que más confusiones evita: **presentar datos no necesariamente es algo visual.** "Presentación" no significa "pantalla". La presentación de datos se divide, al menos, en dos formas:

1. **Mediante interfaz gráfica:** una interfaz de escritorio, una página web, una aplicación móvil.
2. **Mediante APIs:** por ejemplo, una API REST. El sistema "presenta" sus datos como respuestas HTTP con JSON, y quien los consume es otro programa — que quizás, a su vez, se los muestre a una persona en una pantalla.

Ya trabajaste con APIs REST en la clase 02, así que esta idea te cierra sola: un sistema puede no tener ni una sola pantalla propia y aun así tener capa de presentación, porque **expone sus datos hacia afuera**.

### 2.2 Capa de Dominio / Negocio 🔴

Es la capa encargada de **modelar las reglas de negocio y las entidades del dominio**. Contiene la parte estructural y de comportamiento que le da sostén a todos los posibles casos de uso del sistema.

Y acá viene la conexión más importante con lo que ya hiciste: **todo lo que modelaste en la clase 03 para el servicio de ventas de SmartLife es capa de dominio.** El comercio, el producto, los tipos de producto, los impuestos, los observadores de venta — todas esas clases, con sus reglas (calcular impuestos, validar que una venta solo tenga productos propios, notificar observadores), son exactamente esto. Es la capa que venís trabajando desde siempre sin haberle puesto este nombre.

Fijate la frase "le da sostén a los casos de uso": si el enunciado dice *"los comercios deben poder dar de alta productos"*, estructuralmente necesitás una clase `Producto` y una clase `Comercio` — sin ellas no hay caso de uso que se cumpla. De ese sostén hablamos.

### 2.3 Capa de Datos 🔴

Es la capa encargada de la **persistencia** de los datos del sistema — su guardado en un medio durable (que sobrevive a que el programa se apague).

Las preguntas que se responden en esta capa suenan así: *"¿de dónde obtengo todos los productos del sistema?"*, *"¿dónde guardo esta venta?"*, *"¿los tipos de producto ya existen o los tengo que crear?"*. Todo lo que sea traer, guardar, modificar o borrar datos de forma durable vive acá.

El medio persistente puede ser una base de datos, un archivo, incluso un servicio externo que guarde por vos. **Si hay una base de datos, esta capa es la única que se comunica con ella.** (En esta parte del año todavía no vas a trabajar contra una base de datos real — la capa existe igual, y más adelante vas a ver cómo persiste "de mentira" sin cambiar el diseño.)

---

## 3. La regla de dirección 🔴

Esta es la regla que le da forma a todo el modelo, y es simple de enunciar:

> **Las capas superiores usan los servicios de las inferiores — nunca al revés, y nunca salteando niveles.**

("Servicios" acá significa simplemente *las operaciones que una capa ofrece*. Anotá esta palabra: más adelante en la unidad va a aparecer con un segundo significado, más específico, y conviene no mezclarlos.)

Presentación puede llamar a Dominio. Dominio puede llamar a Datos. Pero Datos no llama a Dominio, Dominio no llama a Presentación, y Presentación no saltea a Dominio para tocar Datos directo.

**¿Por qué?** Por algo que ya discutiste al modelar SmartLife: el **doble acoplamiento**. Si el objeto A conoce al objeto B *y* B conoce a A, quedaron atados: no podés tocar uno sin arriesgar al otro, no podés reusarlos por separado, no podés probarlos aislados.

```
   ❌ Doble acoplamiento          ✅ Conocimiento en un solo sentido
   ┌─────┐        ┌─────┐        ┌─────┐        ┌─────┐
   │  A  │ ◄────► │  B  │        │  A  │ ─────► │  B  │
   └─────┘        └─────┘        └─────┘        └─────┘
   se atan mutuamente:           A usa a B.
   tocar uno arriesga            B ni se entera de
   al otro                       quién lo está usando
```

Con las capas se busca exactamente lo de la derecha: que **la capa de arriba conozca a la de abajo, y la de abajo ni se entere de quién la está usando**. La despensa del restaurante no sabe qué mesa pidió el plato — y por eso podés remodelar el salón entero sin tocar la despensa.

**Una honestidad necesaria:** esta teoría, así enunciada, suena estricta — como una pirámide perfecta. En la práctica, cuando llegues a las capas concretas de un proyecto real, vas a ver que algunas tienen conexiones "hacia el costado". Se parte de la idea pura para razonar, y después la realidad la matiza sin romper el espíritu: **minimizar quién conoce a quién**.

---

## 4. Cómo se ve en una arquitectura web real 🟡

Bajemos el modelo a tierra con la arquitectura web tradicional — la clásica de manual:

```
┌────────────────┐      ┌───────────────────────────────────────────────┐
│  NAVEGADOR WEB │      │              APLICACIÓN WEB (backend)         │
│ ┌────────────┐ │      │ ┌──────────────┐ ┌───────────┐ ┌───────────┐ │      ┌───────────────────┐
│ │ HTML/CSS/JS│◄┼──────┼►│ Presentación │─│ Lógica de │─│ Acceso a  │─┼─────►│ Servicios Externos│
│ └────────────┘ │      │ └──────────────┘ │  Negocio  │ │  Datos    │ │      └───────────────────┘
└────────────────┘      │                  └───────────┘ └───────────┘ │      ┌───────────────────┐
                        │                  ┌───────────┐               │─────►│  Base de Datos    │
                        │                  │ Servicios │               │      └───────────────────┘
                        └──────────────────┴─────┬─────┴───────────────┘
                                                 │
                                       ┌─────────┴──────────┐
                                       │ Otras Aplicaciones │
                                       └────────────────────┘
```

Leámoslo por partes:

- **Navegador web (el cliente):** contiene HTML, CSS y JavaScript. Repaso relámpago de qué hace cada uno — **HTML** es un lenguaje (¡no de programación!) que describe el contenido que el navegador muestra; **CSS** le da estilo a ese contenido; **JavaScript** se ejecuta del lado del navegador y le da interactividad.
- **Aplicación web (el backend, en el servidor):** adentro viven las tres capas. *Presentación* genera lo que se le devuelve al cliente (vistas HTML, o datos por API). *Lógica de Negocio* contiene lo grueso del aplicativo: todas las reglas, las validaciones, las entidades de dominio. *Acceso a Datos* habla con la base de datos y con servicios externos que persisten cosas.
- **La caja "Servicios":** es una capa de **integración** — por ella el sistema brinda servicios a otras aplicaciones o consume los de ellas. Es la puerta para que *programas* hablen con tu sistema (una API, por ejemplo), así como Presentación gráfica es la puerta para *personas*.

🕳️ **Madriguera — Integración de sistemas.** Hay muchas formas de que dos sistemas se comuniquen (y cada una con sus ventajas y desventajas); en la segunda parte del año hay una clase dedicada solo a eso. *Volvé al camino — por ahora alcanza con saber que la puerta existe.*

---

## 5. Cliente liviano vs cliente pesado 🟡

El diagrama de arriba es la variante **clásica** — el ejemplo perfecto es Wikipedia: el backend arma la página completa (rellena un *template*, una plantilla HTML con huecos) y le "escupe" al navegador el HTML ya cocinado. El navegador solo muestra. A ese cliente, que casi no ejecuta lógica propia, se lo llama **cliente liviano**.

En el otro extremo está el **cliente pesado**: una aplicación donde una parte importante de la lógica corre **del lado del cliente**. Pensá en un videojuego de disparos, o en muchas apps móviles que usás a diario: el grueso del código se ejecuta en tu dispositivo.

Estructuralmente, la diferencia se ve así:

```
   CLIENTE LIVIANO                        CLIENTE PESADO
   ┌─ Cliente ────────┐                   ┌─ Cliente ─────────────────┐
   │ muestra contenido│                   │ Presentación              │
   │ (HTML cocinado)  │                   │ Lógica de negocio (¡acá!) │
   └────────┬─────────┘                   │ Acceso a datos ───────────┼──┐
            │                            └───────────────────────────┘  │
            ▼                                                           ▼
   ┌─ Backend ────────────────┐           ┌─ Backend ────────────────────┐
   │ Presentación             │           │ (recibe del "acceso a datos" │
   │ Lógica de negocio        │           │  del cliente; tiene su       │
   │ Acceso a datos           │           │  propia lógica también)      │
   └──────────────────────────┘           └──────────────────────────────┘
```

Fijate el detalle fino del pesado: el cliente tiene **sus propias tres capas**, y su capa de acceso a datos es la que se comunica con el backend — que a su vez tiene lógica propia. Las capas quedan "como duplicadas" a ambos lados. Por eso decimos que las capas **siempre las vas a encontrar**; lo que varía es hacia qué lado se tira más responsabilidad.

Dos aclaraciones que suelen zanjar discusiones:

- El HTML/CSS/JS del navegador **puede considerarse parte de la capa de presentación** del sistema completo — pero presentación no es *exclusivamente* eso, porque presentar tiene muchas formas (§2.1).
- No es cierto que "el 100% del dominio siempre vive en el servidor": en un cliente pesado hay mucha lógica del lado del cliente. Depende del tipo de aplicación.

🕳️ **Madriguera — MVC.** Quizás escuchaste "Modelo-Vista-Controlador" y suena parecido a esto. No es lo mismo: MVC es un **patrón de interacción** (organiza cómo interactúan las piezas de la presentación), no un ordenamiento del sistema en capas. Se ve en el segundo cuatrimestre. *Volvé al camino.*

---

## 6. Qué ganás y qué pagás 🔴

Diseñar en capas es una decisión de diseño — y como toda decisión de diseño, se justifica con **qué atributo de calidad comprás y con cuál pagás**. Este es el molde con el que esta materia evalúa todo, así que leé esta sección con ese sombrero puesto.

### Lo que ganás (ventajas)

1. **Te mantiene enfocado en el problema a resolver.** Partiste el gran problema: cuando trabajás en la capa de datos pensás solo en cómo persistir; en presentación, solo en cómo mostrar. Un problema por vez.

2. **Cada capa esconde el detalle de cómo hace lo que hace.** Hacia afuera, una capa expone solo un **contrato** — una interfaz: la lista de operaciones que ofrece, sin revelar cómo las implementa. Esta es la ventaja más importante y la vas a ver hecha código más adelante en la unidad.

3. **La implementación puede reemplazarse siendo transparente para los consumidores.** Si la capa de arriba conoce solo el contrato, deberías poder cambiar la implementación de la capa de abajo por otra — y que todo siga funcionando sin que nadie se entere. ⚠️ Honestidad: esto es lo que *se espera*; que ocurra de verdad depende de qué tan bien implementado esté el proyecto. El diseño en capas te da la posibilidad — no la garantía.

4. **Minimiza la dependencia entre componentes.** Cada capa conoce, de la otra, solamente una interfaz. Acoplamiento reducido al mínimo posible.

5. **Facilita las pruebas.** Si las capas están desacopladas, podés probarlas por separado — sin levantar el sistema entero para verificar una regla de negocio.

### Lo que pagás (desventajas)

1. **Los cambios pueden generar efecto cascada.** Ejemplo concreto: te piden *sumar un campo más a un formulario*. Ese cambio "chiquito" toca la capa de **presentación** (hay que mostrar el campo), la de **negocio** (el campo nuevo trae reglas y validaciones nuevas) y la de **datos** (hay que guardarlo). Un requerimiento, tres capas. Ojo con la lectura fina: esto **no** significa que el acoplamiento sea alto — las capas se conocen por contratos, el acoplamiento es bajo — sino que un cambio *funcional* atraviesa el sistema de punta a punta.

2. **Demasiadas capas agregan complejidad y afectan el rendimiento.** Cada capa extra es un salto más que cada operación tiene que atravesar, y más estructura que mantener. En un problema sencillo, meter capas de más es sobre-diseño: pagás complejidad sin comprar nada. Por eso las capas que viste acá son *las típicas* — no un mandato de que estén todas, siempre, en todo proyecto.

> **Para el parcial, si te preguntan** *"¿por qué diseñarías este sistema en capas?"*:
> Porque divido un problema complejo en partes con alta cohesión y un rol único, donde cada capa conoce de las demás solo un contrato — priorizo **mantenibilidad y testeabilidad** (puedo cambiar o probar una capa sin tocar el resto), y estoy pagando con **complejidad estructural** y con que los cambios funcionales atraviesen varias capas en cascada. En un sistema trivial ese precio no se justifica; en un sistema de información real, sí.

---

## ✅ Checkpoint del Punto 1

*Respondé sin releer. Si alguna no te sale, esa sección se relee antes de seguir. (Sin respuestas a propósito — llegan con el complemento.)*

1. Te piden agregar el campo "teléfono alternativo" al alta de clientes de un sistema en capas. ¿Qué capas tocás y qué hacés en cada una? ¿Qué nombre tiene este fenómeno y en qué columna del análisis entra (ventaja o desventaja)?
2. ¿Por qué la capa de datos no debe conocer a la capa de dominio que la usa? Justificalo con el concepto de acoplamiento — y explicá qué podrías hacer mañana gracias a ese desconocimiento.
3. Un compañero de equipo propone que "todas las capas se conozcan entre sí, así tenemos flexibilidad total". Argumentá en contra usando atributos de calidad.
4. Estás haciendo una agenda personal de contactos, para vos solo. ¿La diseñarías con cinco capas? Justificá con la desventaja que corresponda.
5. ¿Qué significa que una capa "expone un contrato y esconde su implementación"? ¿Qué ventaja concreta te habilita eso a futuro — y por qué esa ventaja es una posibilidad y no una garantía?
6. Un sistema no tiene ninguna pantalla: solo expone una API REST que consumen otras aplicaciones. ¿Tiene capa de presentación? Justificá.
7. Explicá la diferencia estructural entre un cliente liviano y uno pesado: ¿dónde vive la lógica en cada caso, y qué pasa con las capas del lado del cliente en el pesado?
8. "Las capas bajan el acoplamiento" y "los cambios generan efecto cascada" parecen contradecirse. Explicá por qué las dos afirmaciones son ciertas a la vez.

---

## Qué viene en el Punto 2

Ya tenés el mapa de las tres capas. Ahora viene la pregunta que dispara toda la unidad: cuando el enunciado te da **casos de uso** — *"el alumno puede ver sus calificaciones"*, *"el docente puede administrarlas"* — ¿dónde los ponés? Hay una respuesta que se le ocurre a casi todo el mundo primero… y está mal por razones que enseñan muchísimo. En el Punto 2 la desarmamos pieza por pieza.

---

**FIN DEL PUNTO 1**
