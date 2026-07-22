# 🧭 RECORRIDO — Orquestación de CU en Capas — PUNTO 3: Usuarios, Roles y Permisos

**Unidad:** clase04 · **Densidad global:** 🔴

---

## Sobre este documento

**Qué cubre:** la primera pieza correcta del rediseño (la entidad del dominio); el criterio para decidir qué actores merecen ser clases y cuáles no; el modelado de Usuario, Rol y Permiso con sus diagramas; por qué la herencia rompe este modelo y la composición lo salva; las variantes de diseño con sus trade-offs; y la conexión con los códigos de estado HTTP.

**Qué NO cubre:** en qué lugar del sistema se ejecuta el chequeo de permisos (queda abierto a propósito — se cierra en el Punto 4) · gestión de sesiones y login real (más adelante en la materia).

## De dónde venís

- **Punto 2:** un caso de uso se desglosa en clases repartidas por capas; los permisos del rol y la lógica de negocio no viven juntos.
- **clase03:** composición de objetos, y la idea de **extensibilidad en tiempo de ejecución** (poder agregar o cambiar comportamiento sin recompilar).
- **clase02:** códigos de estado HTTP (2xx éxito, 4xx error del cliente).

---

## 1. La primera pieza correcta: la entidad 🔴

Volvamos al SIU con la cabeza limpia. Si el caso de uso es "administrar calificaciones" (o "ver calificaciones"), ¿cuál es la clase que **no puede faltar**?

`Calificacion`. Sin ella, directamente no hay caso de uso que se cumpla: no podés ver ni administrar algo que no está modelado. Mínimamente:

```java
class Calificacion {
    double nota;          // el valor numérico
    LocalDateTime fecha;  // cuándo se registró
    Curso curso;          // asignatura + año + cuatrimestre + docente
    Alumno alumno;        // a quién pertenece
}
```

Esta clase pertenece a la **capa de dominio**, y desde ahí crece el modelado: ¿modelo la clase `Curso`? ¿Las materias son de este sistema o de otro? Esas respuestas salen de conversar con el cliente — pero la semilla estructural es esta.

Y fijate algo simple pero potente: **al instanciar `Calificacion`, ya estás permitiendo que "alguien" dé de alta calificaciones.** El `new` es el alta. Tan directo como eso.

*(Aclaración al paso, porque la duda aparece sola: ¿esta clase no pertenece también a la capa de datos, si al final se guarda? No — `Calificacion` es dominio. La capa de datos la va a **conocer** para traducirla a algo guardable — una fila de tabla, un archivo — pero la clase vive en dominio. Quién hace esa traducción lo vemos en el Punto 4.)*

## 2. Las dos preguntas que siguen abiertas 🔴

Con `Calificacion` modelada, quedan flotando exactamente las dos preguntas del cierre del Punto 2:

1. **¿Cómo me aseguro de que quien está dando de alta esa calificación es un docente y no un alumno?**
2. **¿Dónde — y quién — instancia a `Calificacion`?**

Este punto responde la mitad de la primera: va a dejar modelado *quién puede*. Dónde se ejecuta ese chequeo, y quién hace el `new`, se resuelven en el Punto 4. Tenelas presentes: son los hilos que estamos tirando.

---

## 3. ¿Qué actores merecen ser clases? 🔴

Compliquemos el caso, que la realidad siempre lo hace: además del docente, un **administrador de la plataforma** y un **bedel** también pueden administrar calificaciones (y el administrador, además, verlas):

```
            ┌─ pkg SIU ────────────────────────────────┐
     o      │        ╭──────────────────────╮          │        o
    /|\ ────┼────────│  Ver calificaciones  │──────────┼─────  /|\
    / \     │        ╰──────────╥───────────╯          │       / \
   Alumno   │                   ║ (también Administrador)  Administrador
            │                   ║                      │        │
     o      │    ╭──────────────╨───────────────╮      │        o
    /|\ ────┼────│  Administrar calificaciones  │──────┼─────  /|\
    / \     │    ╰──────────────────────────────╯      │       / \
   Docente  └──────────────────────────────────────────┘      Bedel
```

Cinco asociaciones: Alumno→Ver · Administrador→Ver · Administrador→Administrar · Docente→Administrar · Bedel→Administrar.

Pregunta clave: **¿nos interesa modelar como clases a Administrador, Bedel y Docente?** La respuesta no es "sí a todos" ni "no a ninguno" — hay un criterio, y vale para cualquier sistema:

> **Un actor merece ser entidad del dominio si el sistema necesita datos propios de él y/o tiene relaciones con otras entidades. Si lo único que hace es *acceder* al sistema, no es entidad: es solo un acceso.**

Apliquémoslo:

- **Alumno → SÍ.** El sistema necesita su nombre, apellido, legajo; y tiene relaciones: *sus* calificaciones están atadas a él, *sus* cursadas también.
- **Docente → SÍ.** Tiene nombre y apellido que mostrar, dicta cursadas, cargó calificaciones — relaciones por todos lados.
- **Administrador y Bedel → NO.** Del administrador no interesa ningún dato en particular; del bedel tampoco. Ninguno se relaciona con otras entidades del dominio. Simplemente **tienen un acceso al sistema, y nada más**.

Guardá este criterio: es exactamente el tipo de decisión-con-justificación que esta materia evalúa.

---

## 4. Usuario: la llave de acceso 🔴

Si Administrador y Bedel no son clases pero igual entran al sistema, algo tiene que representar ese "entrar". Ese algo es la entidad **Usuario**:

> **Usuario** es la entidad que contiene los **datos de acceso** para que una persona física — **u otro sistema** — pueda identificarse en nuestro sistema al usarlo.

Tres detalles de la definición que no son decorativos:

- **Datos de acceso**, compuestos mínimamente por **nombre de usuario y contraseña**. Usuario no es "la persona": es su llave.
- **U otro sistema**: quien se identifica no siempre es humano. Otro programa que consume tu API también entra con un usuario.
- Una misma persona física (o sistema) **podría cumplir varios roles** dentro del sistema y ejecutar diferentes casos de uso.

Ese último punto tiene un ejemplo perfecto en el propio SIU: una persona que cursó la carrera y ahora da clases tiene **un solo acceso** — un usuario — y desde ahí puede moverse entre **dos perfiles: alumno y docente**, cambiando de uno a otro cuando quiere. Un acceso, varios roles, en tiempo de ejecución. Retené este caso: en la sección 7 va a demoler una alternativa de diseño entera.

## 5. Roles y permisos: la estructura 🔴

Completemos las reglas del modelo (todas válidas a la vez):

- Cada **rol** puede ejecutar muchos casos de uso.
- Para ejecutar un caso de uso se necesita tener **uno o varios permisos**.
- Un mismo **permiso** puede estar adjudicado a varios roles distintos.

Una propuesta de diseño — considerando, por ahora, que un usuario tiene un único rol:

```
┌──────────────────────────┐      ┌──────────────────────────────────────────┐      ┌──────────────────────┐
│         Usuario          │      │                   Rol                    │      │       Permiso        │
├──────────────────────────┤      ├──────────────────────────────────────────┤  *   ├──────────────────────┤
│ -nombreDeUsuario: String │ ───► │ -nombre: String                          │ ───► │ -descripcion: String │
│ -contrasenia: String     │      │ -permisos: Collection<Permiso>           │      └──────────────────────┘
│ -rol: Rol                │      ├──────────────────────────────────────────┤
└──────────────────────────┘      │ +tenesPermiso(permiso: Permiso): boolean │
                                  └──────────────────────────────────────────┘
```

Leelo así: el Usuario **tiene** un Rol; el Rol tiene un nombre y una **colección de Permisos**, y sabe responder una única pregunta — `tenesPermiso(permiso)` → `true`/`false`. El Permiso, por su parte, tiene… una descripción. Y nada más.

Dos aclaraciones que evitan confusiones clásicas:

- **La clase Permiso es "tonta" a propósito.** Un permiso llamado "Crear calificaciones" NO contiene la lógica de crear calificaciones — no es el caso de uso disfrazado de clase. Solo sirve para **verificar**: si el chequeo da `true`, la acción se hace *después*, en otro lado. El permiso es una etiqueta, no un comportamiento.
- **Los permisos deben ser lo más atómicos posible.** Un permiso que habilite dos cosas a la vez ("ver y editar calificaciones") es una trampa: el día que quieras dar solo una de las dos, no podés. Un permiso = una capacidad precisa.

## 6. Las instancias en acción 🔴

El diagrama de clases dice qué formas existen; el de **objetos** (instancias concretas) muestra el sistema configurado. Para el SIU con sus cuatro actores:

```
                        ┌────────────────────┐
                        │ Administrador: Rol │
                        └─────┬────────┬─────┘
                              ▼        ▼
┌──────────────────────────────┐   ┌────────────────────────────┐
│ Crear calificaciones:        │   │ Ver calificaciones:        │
│ Permiso                      │   │ Permiso                    │
└──────────▲───────────────────┘   └────▲───────▲───────▲───────┘
           │        ┌───────────────────┘       │       │
    ┌──────┴────────┴──┐            ┌───────────┴──┐  ┌─┴──────────┐
    │   Docente: Rol   │            │  Alumno: Rol │  │ Bedel: Rol │
    └──────────────────┘            └──────────────┘  └────────────┘
```

Cuatro instancias de `Rol`, dos de `Permiso`. Docente y Administrador apuntan a "Crear calificaciones"; los cuatro roles apuntan a "Ver calificaciones" — **a la misma instancia**, porque un permiso se comparte entre roles, no se duplica.

Detalle operativo importante: estas instancias están **preconfiguradas y persistidas** — las creó un administrador de la plataforma y se asignan a los usuarios. Nadie se fabrica su propio rol al registrarse.

**¿Y para qué sirve todo esto en vivo?** Cuando le llega una petición al sistema — "quiero ejecutar el caso de uso *crear calificación*" — el sistema razona así:

1. ¿Quién sos? → identifico al usuario.
2. ¿Tu rol tiene el permiso que este caso de uso exige? → `usuario.getRol().tenesPermiso(...)`.
3. **Sí** → adelante. **No** → rechazo, y respondo con un código de estado HTTP: **403** (*Forbidden* — sé quién sos, pero no tenés permiso). Distinto del **401** (*Unauthorized* — ni siquiera te identificaste: la petición llegó sin usuario).

**Dónde** ocurre exactamente este razonamiento dentro del sistema todavía no lo sabemos — para eso falta ordenar las capas. Es el hilo abierto de este punto, y se cierra en el próximo.

---

## 7. Por qué la herencia rompe y la composición salva 🔴

Ahora la decisión de diseño estrella del punto. Tal vez se te ocurrió en la sección 3: *"¿y si Administrador y Bedel (o Alumno y Docente) heredan de una clase Usuario?"* Una superclase `Usuario` abstracta, hijas por cada tipo. Suena elegante. **Está mal**, y el porqué es de manual:

> Acordate del caso del SIU: una misma persona con un acceso y **dos roles a la vez**, alumno y docente, cambiando entre ellos en ejecución. Con herencia, esa persona sería una instancia… ¿de qué clase? ¿`Alumno` o `Docente`? Tiene que ser de UNA — **y en tiempo de ejecución un objeto no puede cambiar de qué clase hereda.** La herencia congela la identidad al momento del `new`; la realidad pide flexibilidad en runtime. Diseño rígido para un requisito flexible: descartado.

La solución es la que ya conocés de la clase 03: **composición en vez de herencia**.

- La entidad `Docente` **tiene como atributo** un `Usuario`.
- Análogamente, `Alumno` **tiene** un `Usuario`.

¿Y la persona que es alumno y docente al mismo tiempo (de asignaturas que ya cursó)? Las **dos** instancias — la de `Alumno` y la de `Docente` que representan a esa misma persona — **referencian a la misma instancia de `Usuario`**:

```
┌──────────────────┐                        ┌──────────────────┐
│ unAlumno: Alumno │                        │unDocente: Docente│
│  nombre, legajo, │                        │ nombre, cursadas,│
│  calificaciones  │                        │ califs. cargadas │
└────────┬─────────┘                        └────────┬─────────┘
         │ usuario                                   │ usuario
         │        ┌───────────────────────┐          │
         └───────►│  elAcceso: Usuario    │◄─────────┘
                  │  nombreDeUsuario      │
                  │  contrasenia          │    ← UNA sola llave;
                  │  roles: [Alumno,      │      dos entidades
                  │          Docente]     │      la comparten
                  └───────────────────────┘
```

El Usuario representa **solo el acceso**; las entidades representan a la persona *en el dominio*, cada una con sus datos y relaciones. La hipotética herencia quedó reemplazada por una composición — y el requisito de los dos roles se cumple sin dolor.

Tres precisiones finas que suelen tomarse en preguntas:

- **La dirección de la relación importa.** Docente y Alumno conocen a su Usuario — **no al revés**. Si `Usuario` conociera a cada entidad que puede tener acceso (alumno, docente, y las 15 que aparezcan), se llenaría de atributos sin sentido; y además generaría el doble acoplamiento que venimos evitando desde el Punto 1. La relación va de la entidad hacia la llave.
- **¿Y el administrador, que no es entidad?** Simplemente existe una instancia de `Usuario` con el rol Administrador — sin ninguna entidad del dominio asociada, porque no hay "match" que hacer. Acceso sin persona modelada.
- **No confundas rol con entidad.** "Docente" aparece dos veces en este modelo y son cosas distintas: existe la **instancia de Rol** llamada Docente (sección 6) y existe la **entidad** `Docente` (esta sección). El rol dice qué se puede hacer; la entidad guarda los datos y relaciones. Administrador y Bedel tienen solo lo primero.

🕳️ **Madriguera — Sesiones y gestión de usuarios.** ¿Cómo se hace el login real? ¿Cómo "recuerda" el sistema quién sos entre una petición y la siguiente? Eso es gestión de sesiones, y se discute a fondo en el segundo cuatrimestre. Hoy el objetivo es ordenar el modelo: hay una llave de acceso, y las entidades no cargan con esa responsabilidad. *Volvé al camino.*

## 8. Las variantes y sus trade-offs 🟡

El modelo de las secciones 5-7 **no es la única propuesta válida** — y saber defender la variante elegida vale tanto como conocerla:

**Variante A — Permisos como enumerados.** En vez de una clase `Permiso`, un `enum` (un tipo con valores fijos definidos en el código: `CREAR_CALIFICACIONES`, `VER_CALIFICACIONES`…). Se puede — entendiendo lo que se paga: un enum **no es extensible en tiempo de ejecución** (agregar un permiso = tocar código y recompilar — la extensibilidad que discutiste en la clase 03 brilla por su ausencia) y deja la configuración *hardcodeada* (cableada en el código en vez de ser datos). Si el sistema jamás va a crecer en permisos, la simplicidad del enum puede justificarse; si hay chance de que crezcan, es una mala compra.

**Variante B — Usuarios con múltiples roles.** En vez de `rol: Rol`, una **colección**: `roles: Collection<Rol>`. Es la sutileza exacta que el caso del SIU pedía (una persona, dos roles). El diagrama de la sección 5 mostraba la versión de un-solo-rol por simplicidad; la colección es la generalización natural.

> **Para el parcial, si te preguntan** *"¿modelarías los tipos de usuario con herencia (Usuario ← Alumno, Docente…)? Justificá."*:
> No. Una misma persona puede cumplir varios roles a la vez y cambiar entre ellos en ejecución, y una instancia no puede cambiar su clase padre en runtime — la herencia congela lo que el requisito pide flexible. Modelo el acceso como una entidad `Usuario` (datos de acceso + roles con permisos) y las entidades del dominio que lo ameriten (`Docente`, `Alumno`) **tienen** un `Usuario` por composición — incluso compartiendo la misma instancia si es la misma persona. Priorizo flexibilidad en tiempo de ejecución; la herencia acá no compra nada que la composición no dé mejor.

---

## ✅ Checkpoint del Punto 3

*Respondé sin releer. Sin respuestas a propósito — llegan con el complemento.*

1. En el servicio de ventas de SmartLife, ¿el actor "Comercio" merece ser entidad del dominio? Aplicá el criterio de la sección 3 y justificá con los datos y relaciones que el enunciado le pide.
2. Un sistema de biblioteca tiene actores Socio, Bibliotecario y Auditor (el auditor solo entra a mirar reportes). Decidí cuáles modelás como entidades y cuáles quedan como puro acceso. Justificá cada uno.
3. ¿Por qué la clase `Permiso` no tiene lógica? ¿Qué pasaría —qué confusión del Punto 2 estarías repitiendo— si el permiso "Crear calificaciones" contuviera el código que crea la calificación?
4. Definí un permiso "Gestionar calificaciones" que habilita ver, crear y borrar. ¿Qué principio rompe y qué problema concreto te genera el día de mañana?
5. Una persona es socia del club y también empleada de secretaría; el sistema modela ambas entidades. ¿Cuántas instancias de `Usuario` hay para esa persona y cómo se relacionan las entidades con ella? Dibujalo.
6. ¿Por qué la relación es "Docente tiene un Usuario" y no "Usuario tiene un Docente"? Dos argumentos.
7. Tu equipo propone permisos como `enum` "porque es más simple". ¿En qué escenario les das la razón y en cuál los frenás? Nombrá el atributo de calidad en juego.
8. Llega una petición de un usuario identificado cuyo rol no tiene el permiso exigido. ¿Qué código de estado respondés y en qué se diferencia del caso en que la petición llega sin identificar?

---

## Qué viene en el Punto 4

Tenés la entidad (`Calificacion`), tenés el modelo de quién-puede (`Usuario`–`Rol`–`Permiso`)… y tenés dos preguntas todavía sin dueño: **¿dónde se ejecuta el chequeo del permiso?** y **¿quién hace el `new Calificacion()`?** El Punto 4 — el corazón del recorrido — presenta los cuatro tipos de clase que pueblan las capas de un sistema, el árbol de quién-conoce-a-quién, y el código donde las dos preguntas por fin encuentran su lugar.

---

**FIN DEL PUNTO 3**
