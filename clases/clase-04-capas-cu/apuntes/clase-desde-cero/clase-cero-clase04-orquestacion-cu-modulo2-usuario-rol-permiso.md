# 🧩 Clase desde cero — Clase 04 · Módulo 2: Usuario, rol y permiso

**Unidad:** `clase04` — Arquitectura de Software
**Módulo 2 de 9.** Fase A (teoría).

## Sobre este documento

**Qué cubre:** cómo se modela *quién puede hacer qué* en un sistema: qué es un **usuario** (y qué no), qué es un **rol**, qué es un **permiso**, por qué la herencia `Usuario ← Alumno / Docente` no sirve, cómo se relaciona una entidad del dominio con su acceso, y qué códigos HTTP responde el sistema cuando alguien no puede hacer algo.
**Qué NO cubre:** **dónde** se verifica el permiso dentro del sistema (M4), ni cómo se gestionan usuarios, contraseñas y sesiones (eso es del segundo cuatrimestre). Hoy el objetivo es ordenar las ideas: que la clase `Docente` no cargue responsabilidades que no le corresponden.

## De dónde venís

- De **M1**, la pregunta abierta: *¿cómo me aseguro de que quien da de alta la calificación es un docente y no un alumno?*
- De la **clase 03**, dos cosas que vas a reusar: la diferencia entre resolver algo con **subclases** o con **instancias** (Hogar y Electrónico fueron instancias de `TipoProducto`, no subclases, para poder agregar tipos sin tocar código), y la idea de **extensibilidad en tiempo de ejecución**.
- De **Paradigmas**, herencia y composición, y que una instancia es de una clase para toda su vida.
- De **Análisis de Sistemas**, que un actor puede ejecutar muchos casos de uso.
- De la **clase 02**, los códigos de estado HTTP.

---

## 1. El caso: un acceso, dos perfiles 🔴

Pensá en el sistema de gestión académica otra vez, pero desde el lado de alguien que es **docente y alumno a la vez**: cursó la carrera en la misma facultad, hoy da clases, y todavía tiene alguna materia pendiente. Entra al sistema con **un solo usuario y una sola contraseña**, y adentro tiene un botón para cambiar de perfil:

```
   ┌──────────────────────────────────────────────┐
   │  Usuario: jperez            [Cambiar perfil] │
   │                                              │
   │  ● Perfil docente   → cursos, cargar notas   │
   │  ○ Perfil alumno    → historia académica,    │
   │                       inscripción a finales  │
   └──────────────────────────────────────────────┘
```

Una persona física, **un acceso**, **dos roles**. Cambia de uno a otro en tiempo de ejecución, sin volver a loguearse. ¿Cómo se modela eso?

### 1.1. El primer intento: herencia ❌

```
              ┌──────────────┐
              │   Usuario    │   «abstracta»
              ├──────────────┤
              │ - nombre     │
              │ - contrasenia│
              └──────▲───────┘
                     │
          ┌──────────┴──────────┐
   ┌──────┴───────┐     ┌───────┴──────┐
   │    Alumno    │     │   Docente    │
   └──────────────┘     └──────────────┘
```

Parece prolijo, y en el caso de arriba es **imposible**. Una instancia es de una clase para siempre: o es un `Alumno` o es un `Docente`. No existe "cambiar de clase padre en tiempo de ejecución". La persona del ejemplo tendría que ser dos objetos distintos con dos accesos distintos, y el sistema real demuestra que no es así. La herencia acá **no es flexible**: justo lo que el caso pide.

Entonces hay que separar dos cosas que el reflejo junta: **el acceso** y **lo que esa persona hace en el sistema**.

---

## 2. Las tres piezas 🔴

### 2.1. Usuario = los datos de acceso

Llamamos **Usuario** a la entidad que contiene los **datos de acceso** para que una persona física **u otro sistema** pueda identificarse en nuestro sistema al usarlo. Mínimamente: **nombre de usuario y contraseña**.

Nada más que eso. El usuario no es "la persona": es su **llave de acceso**. Fijate que la definición incluye a *otro sistema*: cuando una aplicación externa consume nuestra API, también se identifica con un usuario.

### 2.2. Rol = lo que puede hacer

Una misma persona o sistema puede cumplir **varios roles** y ejecutar distintos casos de uso. Los **actores** del diagrama de casos de uso son, en la práctica, los roles. Y las relaciones entre roles y casos de uso son estas, todas válidas a la vez:

- cada rol puede ejecutar **muchos** casos de uso;
- **distintos** roles pueden ejecutar el **mismo** caso de uso;
- para ejecutar un caso de uso se necesita tener **uno o varios permisos**;
- un mismo permiso puede estar adjudicado a **varios roles** distintos.

### 2.3. Permiso = la habilitación atómica

Un **Permiso** es una habilitación atómica que un caso de uso exige (un caso de uso puede exigir más de una): *crear calificaciones*, *ver calificaciones*. No tiene lógica: es una clase "tonta", con una descripción y nada más. Sirve para una sola cosa: que el sistema pueda **verificar** si quien intenta ejecutar un caso de uso lo tiene. La acción (instanciar la calificación, guardarla) la hace otro, después de la verificación, y nunca adentro del permiso.

⚠️ **Los permisos tienen que ser lo más atómicos posible.** "Ver calificaciones" y "editar calificaciones" son **dos** permisos. Un permiso que habilite dos cosas distintas es impreciso y se presta a confusión: separalos.

---

## 3. Una propuesta de diseño 🔴

Suponiendo, para empezar, que un usuario tiene **un único rol**:

```
┌──────────────────────────┐       ┌──────────────────────────────────────────┐       ┌──────────────────────┐
│         Usuario          │       │                   Rol                    │       │       Permiso        │
├──────────────────────────┤       ├──────────────────────────────────────────┤   *   ├──────────────────────┤
│ - nombreDeUsuario: String│ ────► │ - nombre: String                         │ ────► │ - descripcion: String│
│ - contrasenia: String    │       │ - permisos: Collection<Permiso>          │       ├──────────────────────┤
│ - rol: Rol               │       ├──────────────────────────────────────────┤       │                      │
├──────────────────────────┤       │ + tenesPermiso(permiso: Permiso): boolean│       └──────────────────────┘
│                          │       └──────────────────────────────────────────┘
└──────────────────────────┘

   ────►  asociación dirigida (el de la cola conoce al de la punta)
   *      multiplicidad: un rol tiene muchos permisos
```

Leelo de izquierda a derecha: el **usuario** tiene un rol; el **rol** tiene un nombre y una colección de permisos, y sabe responder `tenesPermiso(permiso)`; el **permiso** solo tiene una descripción.

Para el caso de arriba (una persona, dos roles) alcanza con una variante: `Usuario` tiene una **colección de roles** en lugar de uno. Es la misma estructura.

### 3.1. Las instancias: roles y permisos son datos, no clases

Esto es lo que más cuesta ver. Administrador, Docente, Alumno y Bedel **no son clases**: son **instancias de `Rol`**. Crear calificaciones y Ver calificaciones **no son métodos**: son **instancias de `Permiso`**. Un diagrama de objetos (los nombres van subrayados en UML porque son instancias):

```
                        ┌──────────────────────┐
                        │  Administrador : Rol │
                        └─────┬──────────┬─────┘
                              ▼          ▼
   ┌──────────────────────────────┐   ┌────────────────────────────┐
   │ Crear calificaciones :       │   │ Ver calificaciones :       │
   │ Permiso                      │   │ Permiso                    │
   └──────────▲───────────────────┘   └────▲───────▲───────▲───────┘
              │        ┌───────────────────┘       │       │
       ┌──────┴────────┴──┐            ┌───────────┴──┐  ┌─┴──────────┐
       │   Docente : Rol  │            │  Alumno : Rol│  │ Bedel : Rol│
       └──────────────────┘            └──────────────┘  └────────────┘
```

Administrador, Docente, Alumno y Bedel apuntan **a la misma instancia** de "Ver calificaciones"; Administrador y Docente comparten "Crear calificaciones". Un permiso en varios roles, varios roles con el mismo permiso: todo lo de §2.2, en objetos.

Tres consecuencias que importan:

- **Nadie crea su propio rol al registrarse.** Estas instancias están **preconfiguradas**: un administrador de la plataforma las creó una vez y se las asigna a los usuarios. Y como tienen que sobrevivir a que el sistema se apague, **viven persistidas en algún lado** (hilo: en M4 vas a ver aparecer un **repositorio** de permisos, la clase que se ocupa de guardarlos; qué es un repositorio se explica en M3).
- **Un "service de permisos"** solo tiene sentido si el sistema ofrece una pantalla de alta, baja y modificación de permisos (*service*: el tipo de clase que orquesta un caso de uso; lo ves en M4). Lo normal es que los permisos sean pocos, fijos y se configuren una vez.
- En M1 el Bedel administraba calificaciones; en estas instancias solo las ve. Fijate que cambiar eso no es tocar código: es agregarle un permiso al rol. Justamente el punto.

### 3.2. Otras propuestas válidas 🟡

No es el único diseño posible. Dos variantes, con su costo:

- **Permisos como enumerados** (`enum`). Se puede, entendiendo las limitaciones: un enum **no es extensible en tiempo de ejecución** (agregar un permiso es tocar código y volver a compilar) y deja la configuración **hardcodeada**. Es la misma discusión que Hogar y Electrónico en la clase 03: subclases o enum cuando el conjunto es cerrado y chico; instancias cuando tiene que poder crecer sin recompilar. Si tu sistema va a tener más roles con el tiempo, el enum te queda chico.
- **Usuario con múltiples roles** (colección), como vimos. Es la que resuelve el caso de la persona con dos perfiles.

> **Para el parcial, si te preguntan:** *¿Se pueden modelar los permisos con un `enum`?*
> Sí, entendiendo que trae limitaciones: un enum no es extensible en tiempo de ejecución (agregar un permiso obliga a modificar código y recompilar) y deja la configuración hardcodeada. Modelar los permisos como instancias de una clase `Permiso` prioriza la extensibilidad; el enum prioriza la simplicidad cuando el conjunto es chico y cerrado.

---

## 4. ¿Qué actores son entidades del dominio? 🔴

Volvé a los cuatro roles. Si Administrador, Docente, Alumno y Bedel son instancias de `Rol`, ¿existe **además** una clase `Docente`? ¿Y una clase `Bedel`?

La respuesta no es "todos sí" ni "todos no". La pregunta correcta es: **¿me interesan datos propios de ese actor y sus relaciones con otras entidades?**

| Actor | ¿Entidad del dominio? | Por qué |
|---|---|---|
| **Alumno** | **Sí** | Tiene atributos propios (nombre, apellido, legajo) y relaciones: sus calificaciones, sus cursadas. El sistema necesita mostrar su nombre junto a sus notas |
| **Docente** | **Sí** | Nombre, apellido; qué cursos dicta, qué calificaciones cargó. Hay relaciones que al sistema le sirven |
| **Administrador** | **No** | No hay ningún dato suyo que al dominio le interese. Solo tiene **acceso** |
| **Bedel** | **No** | Ídem: entra al sistema, ejecuta lo que su rol le permite, y nada más |

Entonces conviven dos cosas que se parecen pero no son lo mismo: **el rol Docente** (una instancia de `Rol`, con permisos) y **la entidad `Docente`** (una clase del dominio, con nombre, apellido y cursos). El Administrador tiene el primero y no tiene la segunda: es una instancia de `Usuario` con el rol administrador y ninguna entidad asociada.

### 4.1. Cómo se relaciona la entidad con su acceso

Si `Docente` y `Alumno` son entidades, ¿cómo se conectan con `Usuario`? **Por composición, no por herencia:** la entidad **tiene** un usuario como atributo. (Es composición de objetos, la de "preferir composición a herencia", no la composición UML con rombo relleno: el `Usuario` se puede compartir entre dos entidades, así que en el diagrama de clases es una asociación común.)

```
┌────────────────────┐                    ┌────────────────────┐
│      Docente       │                    │       Alumno       │
├────────────────────┤                    ├────────────────────┤
│ - nombre           │                    │ - nombre           │
│ - apellido         │                    │ - legajo           │
│ - cursos           │                    │ - calificaciones   │
│ - usuario: Usuario │──┐            ┌────│ - usuario: Usuario │
└────────────────────┘  │            │    └────────────────────┘
                        │  ┌─────────┴──┐
                        └─►│  Usuario   │◄── la MISMA instancia
                           │  jperez    │    si es la misma persona
                           └────────────┘
```

La herencia del primer intento se reemplazó por un atributo. Y el caso de la persona con dos perfiles se resuelve solo: hay una instancia de `Alumno` y una instancia de `Docente` que **apuntan a la misma instancia de `Usuario`**, porque hay un solo acceso.

⚠️ **La relación va de la entidad hacia el usuario, y no al revés.** Si `Usuario` conociera a su alumno, a su docente, a su bedel, a su administrador y a los quince roles que aparezcan después, se llenaría de atributos sin sentido, la mayoría en `null`. Y además sería doble acoplamiento. La entidad conoce su llave; la llave no sabe de quién es.

> **Para el parcial, si te preguntan:** *¿Por qué `Alumno` y `Docente` no heredan de `Usuario`?*
> Porque una misma persona puede ser alumno y docente a la vez con un único acceso, y una instancia no puede cambiar de clase en tiempo de ejecución: con herencia sería o una cosa o la otra. Se separa el **acceso** (`Usuario`, con nombre y contraseña) de las **entidades** (`Alumno`, `Docente`, con sus datos y relaciones), y cada entidad tiene un `Usuario` como atributo; si es la misma persona, las dos apuntan a la misma instancia. Prioriza flexibilidad (varios roles en tiempo de ejecución); el costo es más clases para orquestar el mismo caso de uso.

---

## 5. Qué pasa cuando llega un pedido 🔴

Ya tenés las piezas. Así se usan cuando alguien intenta ejecutar un caso de uso, por ejemplo, crear una calificación:

```
   llega un pedido: "crear calificación"
            │
            ▼
   ① ¿Quién sos?  →  id de usuario  →  ¿existe?  ── no viene / no existe ──►  401
            │
            ▼
   ② ¿Qué ROL tenés?   (se toma el rol de ese usuario)
            │
            ▼
   ③ ¿Tenés el PERMISO que este caso de uso exige?   (rol.tenesPermiso(crearCalificaciones))
            │
     ┌──────┴──────┐
    sí            no
     │             │
     ▼             ▼
  ejecutás      rechazás y respondés  403
```

Y los códigos HTTP con los que se responde cuando algo de eso falla:

| Código | Nombre | Cuándo |
|---|---|---|
| **403** | Forbidden | El pedido viene identificado, pero **no tiene el permiso** para ejecutar este caso de uso. "Sé quién sos, y no podés" |
| **401** | Unauthorized | El pedido llegó **sin identificarse** (sin usuario; en general, también cuando las credenciales no sirven). "No sé quién sos" |

Son parecidos y se confunden: 401 es *falta la identificación*; 403 es *la identificación está, pero no alcanza*. (El 404, que ya conocés de la clase 02, va a aparecer en M4 para otra cosa: el recurso que se pide no existe.)

⚠️ **Lo que todavía no sabés es dónde ocurre esta verificación.** ¿En qué clase, en qué capa? Para responderlo hace falta primero ordenar las capas con clases concretas. Es el hilo que se cierra en M4.

> **Para el parcial, si te preguntan:** *¿Cuál es la diferencia entre un 401 y un 403?*
> 401 (Unauthorized) se responde cuando el pedido no viene identificado: no hay usuario. 403 (Forbidden) se responde cuando el usuario está identificado pero no tiene el permiso que el caso de uso exige. El 403 es el resultado de verificar `rol.tenesPermiso(...)` y que dé falso.

---

## 6. Molde para justificar esta decisión 🔴

Cuando te pidan justificar por qué modelaste el acceso aparte:

> **Molde:** *"Modelo el acceso (`Usuario`) separado de las entidades (`Alumno`, `Docente`) y los roles como instancias, **porque priorizo flexibilidad**: una persona puede tener varios roles y cambiar entre ellos en tiempo de ejecución, y se pueden agregar roles y permisos sin tocar código. **Pago con** más clases para orquestar el mismo caso de uso."*

---

## 7. Lo que te llevás de este módulo

- **Usuario** = datos de acceso (nombre y contraseña), de una persona o de otro sistema. No es "la persona".
- **Rol** = lo que ese acceso puede hacer; los actores del diagrama de casos de uso. **Permiso** = la habilitación atómica para un caso de uso. Son **instancias**, preconfiguradas y persistidas, no clases ni métodos.
- `Usuario → Rol → * Permiso`, con `tenesPermiso(permiso)`. Variantes: colección de roles; enum para permisos (con limitaciones).
- **Herencia no**: no se cambia de clase en tiempo de ejecución. **Composición sí**: la entidad tiene un `Usuario`; la misma persona, la misma instancia.
- No todo actor es entidad: lo es si tiene datos y relaciones propias (Alumno, Docente). Si solo tiene acceso (Bedel, Administrador), es un `Usuario` con un rol y nada más.
- Sin identificación → **401**. Identificado sin permiso → **403**.
- Queda abierto **dónde** se verifica (M4).

---

## ✅ Checkpoint del Módulo 2

Respondé de memoria. Las respuestas van al complemento.

1. ¿Qué es un usuario en este modelo y qué **no** es? ¿Por qué la definición incluye a "otro sistema"?
2. Una persona es alumna y docente a la vez. Explicá por qué la herencia `Usuario ← Alumno / Docente` no puede representarla, y cómo lo resuelve la composición.
3. Dibujá de memoria el diagrama `Usuario → Rol → Permiso`. ¿Qué método tiene `Rol` y qué devuelve?
4. "Docente" aparece dos veces en el modelo: como rol y como entidad. ¿Qué es cada cosa y por qué no es redundante?
5. ¿Por qué Bedel y Administrador no son entidades del dominio, y Alumno sí? ¿Cuál es el criterio?
6. ¿Por qué la relación va de `Docente` hacia `Usuario` y no de `Usuario` hacia `Docente`?
7. ¿Qué quiere decir que un permiso tiene que ser "atómico"? Da un ejemplo de un permiso mal definido.
8. ¿Cuándo un sistema responde 401 y cuándo 403? ¿Qué verificación concreta produce el 403?
9. Nombrá las limitaciones de modelar los permisos con un `enum`, y conectalo con una decisión de la clase 03.

---

## Qué viene después

**M3 — Las clases típicas (I): entidades y repositorios.** Con el acceso ordenado, empezamos a poblar las capas con clases concretas. Primero las dos de abajo: qué es exactamente una entidad de dominio (y la diferencia entre un modelo anémico y uno enriquecido), y qué objetos se encargan de guardar: los repositorios. Ahí se cierra la tercera pregunta de M1, *¿quién guarda?*, y aparece la primera de las cuatro reglas de la clase.

**FIN DEL MÓDULO 2**
