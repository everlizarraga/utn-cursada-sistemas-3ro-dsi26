# 🧭 RECORRIDO — Orquestación de CU en Capas — PUNTO 2: La Orquestación — el Problema

**Unidad:** clase04 · **Densidad global:** 🔴 (acá arranca el corazón de la unidad)

---

## Sobre este documento

**Qué cubre:** el caso SIU Guaraní con sus casos de uso; el diseño que a casi todo el mundo se le ocurre primero (métodos con nombre de caso de uso dentro de clases que representan actores); por qué ese diseño está mal — desarmado con una sola pregunta; a dónde lleva insistir con él; el diagnóstico de fondo; y la conclusión que ordena toda la unidad.

**Qué NO cubre:** cómo se modela correctamente cada pieza (Puntos 3 y 4) · el código real (Puntos 5 y 6).

## De dónde venís

- **Punto 1:** las tres capas (Presentación, Dominio/Negocio, Datos), la regla de dirección, y que el frontend vive separado del backend.
- **Refresco de una línea:** un **caso de uso** es una funcionalidad completa que el sistema ofrece ("ver calificaciones", "registrar una venta"), y un **actor** es quien la ejecuta (un rol de persona u otro sistema); el **diagrama de casos de uso** dibuja actores como muñequitos conectados a óvalos con los casos de uso. Los venís usando en la carrera; acá se presentan completos con el ejemplo.

---

## 1. De dónde viene la confusión 🔴

Hasta ahora, cuando modelaste objetos —tanto en la carrera como en el servicio de ventas de la clase 03— los enunciados tenían algo en común que quizás nunca notaste: **no existían roles ni usuarios.** Había productos, comercios, impuestos… pero nadie preguntaba *quién* tenía permitido hacer qué.

En diseño de sistemas eso cambia: aparecen los **actores**. El enunciado del servicio de ventas ya lo hacía: *"los comercios deben poder dar de alta productos"* — ahí hay un actor (el comercio) ejecutando casos de uso (gestionar productos, registrar ventas, consultar precios).

Y acá está la trampa: si te quedás con la base del paradigma orientado a objetos — "todo sustantivo es candidato a clase, todo verbo a método" — la aparición de actores te empuja a una confusión conceptual muy natural. Tan natural que este punto entero está dedicado a desarmarla, porque **entender por qué está mal ES entender la unidad.**

---

## 2. El caso: SIU Guaraní 🔴

Tomemos el sistema de gestión educativa que usás todos los cuatrimestres, con dos requerimientos:

- El sistema debe permitir que los **alumnos visualicen** sus calificaciones.
- El sistema debe permitir la **administración** (alta, baja y modificación) de calificaciones por parte de los **docentes**.

El diagrama de casos de uso resultante:

```
            ┌─ pkg SIU ────────────────────────────────┐
     o      │        ╭──────────────────────╮          │
    /|\ ────┼────────│  Ver calificaciones  │          │
    / \     │        ╰──────────────────────╯          │
   Alumno   │                                          │
            │                                          │
     o      │    ╭──────────────────────────────╮      │
    /|\ ────┼────│  Administrar calificaciones  │      │
    / \     │    ╰──────────────────────────────╯      │
   Docente  └──────────────────────────────────────────┘
```

Dos actores, cada uno con su caso de uso. "Administrar", dicho completo, son cuatro operaciones: alta, baja, modificación y listado de calificaciones.

---

## 3. El diseño que se le ocurre a casi todos 🔴

Con el diagrama adelante, el diseño que brota solo es este:

```java
class Alumno {
    void verCalificaciones() { /* ??? */ }
}

class Docente {
    void administrarCalificaciones() { /* ??? */ }
}
```

Una clase por cada actor, y adentro de cada clase un método con el nombre del caso de uso que ese actor ejecuta. Prolijo, simétrico, calca el diagrama.

Y no es un error exótico: es **el** error. Si estás pensando "yo lo hubiera hecho así" — perfecto, sos la persona a la que este punto le habla. Fijate además que el mismo reflejo aplica al caso propio: leés el enunciado del servicio de ventas y te sale `Comercio.consultarPrecioDeProducto()`. Misma trampa, otro disfraz.

En general, el patrón del error es: asignar métodos que tienen nombre de **funcionalidad completa**, de **botón de interfaz** o de **caso de uso**, a hipotéticas clases que representan **actores** — generalmente una clase por actor.

---

## 4. La pregunta que lo desarma 🔴

No hace falta teoría nueva para ver que está mal. Alcanza con una pregunta:

> **¿Qué haría ese método por dentro?**

Pensalo en serio. Llamás `alumno.verCalificaciones()`… ¿y qué pasa? ¿**Se abre una pantalla**? Sabés desde la clase 01 —y lo formalizaste en el Punto 1— que eso es imposible: la interfaz gráfica vive en otra capa, incluso en otro componente físico (el frontend), separada de toda la lógica. **Llamar a un método de una clase de dominio no puede abrir una pantalla mágicamente.** Si tu diseño necesita que eso pase, hay algo roto.

Vamos por partes, porque el método falla por dos lados:

**El nombre.** "Ver" es una acción que ocurre *en una pantalla* — y en esta clase ni siquiera sabés si hay una pantalla de por medio (¿y si el sistema solo expone una API?). Si lo que querés es que devuelva la lista de calificaciones del alumno, eso es un simple *getter* de una colección — `getCalificaciones()` — y no hay que disfrazarlo de caso de uso. El nombre confunde una operación de datos con una funcionalidad completa.

**El contenido.** Peor todavía con `administrarCalificaciones()`: "administrar" son cuatro operaciones distintas (alta, baja, modificación, listado). ¿Un método que hace las cuatro? ¿Que decide solo cuál de las cuatro te toca? El caso de uso, visto desde la implementación, involucra un montón de cosas distintas — y todas apretadas en un método no caben.

---

## 5. Insistamos igual: el experimento mental 🔴

Supongamos que no te convencí y seguís adelante con el diseño. Veamos a dónde lleva.

Mañana llega un requerimiento nuevo: *"los **bedeles** también pueden administrar calificaciones"*. (Un **bedel** es el personal administrativo de la facultad: gestiona las aulas, toma asistencia — la oficina de bedelía que ves al entrar al campus.)

Siguiendo tu propia lógica, el camino es único:

```
Paso 1: aparece Bedel                Paso 2: "¡para eso está la herencia!"

class Bedel {                             ┌──────────────────────────┐
  administrarCalificaciones()             │  ¿AdministradorDeNotas?  │  ← ¿cómo se llama
}                                         │  administrarCalifica...()│     esta superclase?
                                          └──────────┬───────────────┘
→ acabás de REPETIR                          ┌───────┴────────┐
  toda la lógica que ya                  ┌───┴────┐      ┌────┴───┐
  estaba en Docente                      │Docente │      │ Bedel  │
                                         └────────┘      └────────┘
```

Primer intento: crear la clase `Bedel` con el método repetido → **duplicaste lógica**. Segundo intento: "bueno, para eso existe la herencia" → una superclase de nombre dudoso que agrupe a Docente y Bedel por el solo hecho de compartir un método… y cuando aparezca el tercer actor con *otra* combinación de casos de uso, el árbol de herencia se retuerce de nuevo. Cada decisión te hunde un poco más: es un **enredo estructural del que es muy difícil salir**.

Cuando un diseño te obliga a elegir entre repetir lógica o forzar herencias, el problema no está en la elección: está **un paso antes**, en dónde pusiste la responsabilidad.

---

## 6. El diagnóstico real 🔴

Acá hay que afinar el oído, porque la discusión **no** pasa por donde parece.

No se trata de discutir *quién puede hacer qué* — que eventualmente solo los docentes administren y los alumnos visualicen es un hecho del negocio, y está bien que así sea. La falla es otra, y es doble:

1. **Se está mezclando la lógica de negocio con los permisos que un actor o rol tiene sobre el sistema.** Son dos cosas distintas: una cosa es *qué hace* el caso de uso (crear una calificación, con sus validaciones y sus datos), y otra es *quién tiene permitido* dispararlo. Al meter el método en la clase del actor, las dos preguntas quedaron pegoteadas en el mismo lugar — y por eso agregar un actor nuevo te obligaba a duplicar la lógica.

2. **Se están mezclando responsabilidades de capas distintas del sistema.** "Ver" es de la capa de presentación. Las reglas de la calificación son del dominio. Recuperar y guardar calificaciones es de la capa de datos. El método `verCalificaciones()` pretendía ser las tres capas a la vez, adentro de una clase de dominio.

---

## 7. La conclusión que ordena la unidad 🔴

De todo lo anterior sale la idea central — si te llevás una sola frase de este punto, que sea esta:

> **Un caso de uso no se resuelve en un método de una única clase. Un caso de uso se desglosa: desprende la creación de muchas clases, y cada una ocupa un rol distinto, en una capa distinta, con una responsabilidad distinta.**

Eso es **orquestar** un caso de uso: repartir sus responsabilidades entre las capas del sistema. Por eso los casos de uso "llevan tiempo" de implementar — no porque el código sea difícil, sino porque un CU bien puesto genera varias clases coordinadas.

> **Para el parcial, si te preguntan** *"¿por qué está mal una clase `Docente` con un método `administrarCalificaciones()`?"*:
> Porque asigna una funcionalidad completa (un caso de uso) a una clase que representa un actor, mezclando la lógica de negocio con los permisos del rol y mezclando responsabilidades de capas distintas (presentación, dominio y datos) en un solo método. Además no escala: cada actor nuevo que pueda ejecutar el mismo caso de uso obliga a repetir lógica o a forzar herencias. Lo correcto es desglosar el caso de uso en varias clases, cada una en su capa con su responsabilidad.

---

## ✅ Checkpoint del Punto 2

*Respondé sin releer. Sin respuestas a propósito — llegan con el complemento.*

1. En el enunciado del servicio de ventas aparece *"los comercios deben poder registrar ventas"*. Un compañero propone `Comercio.registrarVenta()` como resolución completa del caso de uso. ¿Qué le preguntás para hacerlo dudar, y qué dos mezclas le señalás?
2. ¿Por qué "llamar a un método no puede abrir una pantalla" es un argumento de **arquitectura** y no una limitación técnica de Java?
3. ¿Cuál es la diferencia entre un método `getCalificaciones()` en una clase y el caso de uso "ver calificaciones"? ¿Por qué uno es legítimo y el otro no cabe en un método?
4. Al sistema del SIU se agrega el actor "Secretario Académico", que puede administrar calificaciones y también ver estadísticas. Mostrá cómo el diseño de una-clase-por-actor se degrada con este agregado (las dos salidas posibles y por qué ambas son malas).
5. "El problema es que los alumnos no deberían poder administrar calificaciones." ¿Por qué esta frase, siendo cierta, NO es el diagnóstico del antipatrón? ¿Cuál es el diagnóstico real?
6. Enunciá con tus palabras la conclusión de la unidad: ¿qué le pasa a un caso de uso cuando se implementa bien?
7. ¿Qué dos preguntas quedan abiertas después de este punto? (Pista: una es sobre *quién puede*, la otra sobre *dónde va cada cosa*.)

---

## Qué viene en el Punto 3

Quedó establecido que los permisos del rol y la lógica del negocio no viven juntos. Entonces, ¿dónde viven los permisos? El Punto 3 modela la primera mitad de la respuesta: **Usuario, Rol y Permiso** — incluyendo una decisión de diseño (herencia vs composición) que es material directo de parcial, y el criterio para saber qué actores merecen ser clases y cuáles no.

---

**FIN DEL PUNTO 2**
