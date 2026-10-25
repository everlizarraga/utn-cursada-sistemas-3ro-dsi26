# 🌱 Proyecto 1 — Etapa 2: Tu primer bean (`@Component`)

> **Objetivo:** crear tu primera clase administrada por Spring, hacer que imprima algo cuando se crea, y **comprobar con tus ojos que Spring la instancia sin que vos hagas `new`**. A partir de eso, entender qué es un "bean".
>
> **El momento clave de esta etapa:** vas a ver un objeto aparecer **sin haberlo creado vos**. Ese es el corazón de Spring, hecho tangible.
>
> **Pre-requisito:** Etapa 1 completa (corriste la app vacía, entendés que Spring es un contenedor de objetos).
>
> **Tiempo estimado:** 30-40 minutos.

---

## 🧭 Mapa de esta etapa

1. Qué vamos a hacer.
2. Crear la clase con `@Component`.
3. Correr y observar — el mensaje aparece.
4. **La revelación:** ¿quién hizo `new`?
5. Qué es un bean (a partir de lo que viste).
6. Qué hace `@Component`.
7. Experimentos (el contraste que lo prueba todo).
8. Dónde busca Spring las clases.
9. Checkpoint.

---

## 🎯 Parte 1: Qué vamos a hacer

En la Etapa 1 corriste Spring **vacío** — el contenedor se armó pero quedó sin nada adentro, porque no le diste ninguna clase para administrar.

Ahora le vamos a dar **su primera clase**. Una clase mínima, cuyo único propósito por ahora es **demostrar que Spring la crea**.

El truco para "ver" que Spring la crea: vamos a poner un `System.out.println` **en el constructor**. Como sabés del Proyecto 0, el constructor se ejecuta **cuando se crea el objeto**. Entonces:

- Si el mensaje del constructor **aparece** en el log → el objeto se creó.
- Y como vos **no vas a hacer `new` en ningún lado** → quien lo creó fue **Spring**.

Esa es toda la jugada. Simple y reveladora.

---

## ✍️ Parte 2: Crear la clase

Vas a crear una clase nueva en el mismo package que `PaisesApplication`. La vamos a llamar `CatalogoDePaises` (con el tiempo va a ser un servicio de verdad; por ahora es un cascarón para la demostración).

### Pasos en IntelliJ

1. En el panel Project, click derecho sobre el package `ar.edu.utn.ba.paises` (el mismo donde está `PaisesApplication`).
2. **New → Java Class**.
3. Nombre: `CatalogoDePaises`.
4. Tipeá esto adentro:

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;

@Component
public class CatalogoDePaises {

    public CatalogoDePaises() {
        System.out.println(">>> Spring acaba de crear un CatalogoDePaises");
    }
}
```

IntelliJ te va a marcar `@Component` en rojo hasta que importes — `Alt + Enter` → importar `org.springframework.stereotype.Component`. (O tipealo y dejá que IntelliJ lo complete.)

### Qué tiene esta clase

- **`@Component`** arriba de la clase. Esta es la annotation nueva. Ya vamos a ver qué hace.
- **Un constructor** que solo imprime un mensaje. Nada más.
- **Ningún `new` en ningún lado.** Vos no vas a instanciar esta clase. Importante: **no la toques desde `PaisesApplication` ni desde ningún otro lado.**

> Fijate que la clase está en el **mismo package** que `PaisesApplication` (`ar.edu.utn.ba.paises`). Eso importa, y vamos a ver por qué en la Parte 8.

---

## 🚀 Parte 3: Correr y observar

**Corré la app** (triángulo verde en `PaisesApplication`, o `./mvnw spring-boot:run`).

Mirá el log. Entre las líneas de Spring Boot, vas a ver **tu mensaje**:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
...
 :: Spring Boot ::                (v4.0.5)

2026-06-19T10:30:15 - Starting PaisesApplication using Java 21
2026-06-19T10:30:15 - No active profile set...
>>> Spring acaba de crear un CatalogoDePaises          ← ¡TU MENSAJE!
2026-06-19T10:30:16 - Started PaisesApplication in 0.9 seconds

Process finished with exit code 0
```

**Ahí está.** Tu mensaje apareció. El constructor de `CatalogoDePaises` se ejecutó.

---

## 🤯 Parte 4: La revelación

Pará un segundo y pensá lo que acaba de pasar:

- Escribiste una clase `CatalogoDePaises` con un constructor.
- **En ningún lado escribiste `new CatalogoDePaises()`.**
- Corriste la app.
- **El constructor se ejecutó igual.** El mensaje apareció.

**Pregunta clave: ¿quién hizo `new CatalogoDePaises()`?**

Vos no fuiste. No hay `new` en tu código. **Fue Spring.**

Esto es exactamente lo que en la Etapa 1 te dije que Spring hace: *"crea los objetos por vos"*. Acabás de **verlo pasar**. No es una frase de manual — es ese `>>>` que apareció en tu log sin que vos hicieras `new`.

> **Este es EL momento.** Todo lo demás de Spring se construye sobre esta idea: vos declarás clases, Spring las instancia. Si esto te quedó claro, el resto del framework se va a ir cayendo solo.

---

## 🫘 Parte 5: Qué es un bean

Ahora que lo viste, te puedo dar el término.

**Ese objeto `CatalogoDePaises` que Spring creó se llama un "bean".**

> **Un bean es un objeto que Spring crea y administra por vos.**

Eso es todo. Cuando alguien dice "esto es un bean", quiere decir "es un objeto que vive dentro del contenedor de Spring, que Spring instanció y del cual Spring se hace cargo".

### Lo que Spring hace con un bean

Cuando Spring crea un bean, no solo lo instancia — lo **guarda en su contenedor** (el ApplicationContext que mencionamos en la Etapa 1) y lo tiene disponible para usarlo donde haga falta.

```
Spring arranca
     ↓
Escanea tu proyecto
     ↓
Encuentra CatalogoDePaises (marcada con @Component)
     ↓
Hace new CatalogoDePaises()    ← acá se ejecutó tu constructor (el >>> que viste)
     ↓
Guarda ese objeto en el contenedor como un "bean"
     ↓
El bean queda disponible
```

### Cuántas veces se crea

Fijate un detalle en tu log: el mensaje `>>>` apareció **UNA sola vez**. No diez, no dos. Una.

Eso es porque **Spring crea UNA sola instancia de cada bean** por defecto. No importa cuántas veces lo necesites después — siempre usás el mismo objeto. (Esto tiene un nombre, "singleton", pero no te detengas en el término ahora. Lo vas a aprovechar en la Etapa 3 cuando ese bean se reuse.)

---

## 🏷️ Parte 6: Qué hace `@Component`

`@Component` es **la annotation que le dice a Spring "administrá esta clase"**.

Cuando ponés `@Component` arriba de una clase, le estás diciendo a Spring:

> "Che, cuando arranques, **creá un objeto de esta clase** y guardalo en tu contenedor como un bean. Yo no lo voy a instanciar con `new` — hacelo vos."

Sin `@Component`, Spring **ni se entera** de que tu clase existe. Con `@Component`, Spring la encuentra, la instancia, y la administra.

**Es así de directo:**
- `@Component` → "Spring, esta clase es tuya, creá un bean."
- Sin `@Component` → Spring la ignora por completo.

Lo vas a comprobar vos mismo en el Experimento 1.

> **Nota al pasar (no te detengas):** `@Component` tiene unos "primos" que hacen **exactamente lo mismo técnicamente** pero comunican intención distinta: `@Service`, `@Repository`, `@Controller`. Se usan para distintos tipos de clase (lógica de negocio, acceso a datos, endpoints web). Por ahora **usá `@Component`** y listo. Cuando lleguemos a casos donde los otros tengan sentido, los vemos. No los memorices.

---

## 🧪 Parte 7: Experimentos (acá se prueba todo)

Estos experimentos no son opcionales — son donde el concepto se graba a fuego. Hacelos.

### Experimento 1: Sacá `@Component` y mirá qué pasa

Borrá (o comentá) la línea `@Component`:

```java
// @Component                          ← comentado
public class CatalogoDePaises {

    public CatalogoDePaises() {
        System.out.println(">>> Spring acaba de crear un CatalogoDePaises");
    }
}
```

Corré la app.

**¿Qué pasó?** El mensaje `>>>` **NO apareció.**

¿Por qué? Porque sin `@Component`, Spring **no sabe que la clase existe**. No la escanea, no la instancia, el constructor nunca se ejecuta.

**Esto es la prueba directa de que es `@Component` lo que hace que Spring administre la clase.** Con la annotation → se crea. Sin la annotation → no se crea. Lo viste en ambos sentidos.

> **Volvé a poner `@Component`** antes de seguir.

### Experimento 2: Agregá un segundo `@Component`

Creá otra clase en el mismo package, `Saludador`:

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;

@Component
public class Saludador {

    public Saludador() {
        System.out.println(">>> Spring creó un Saludador");
    }
}
```

Corré la app.

**¿Qué pasó?** Aparecieron **los dos mensajes**:

```
>>> Spring acaba de crear un CatalogoDePaises
>>> Spring creó un Saludador
```

Spring encontró **ambas** clases marcadas con `@Component`, y creó un bean de **cada una**. Esto te muestra que Spring **escanea todo tu proyecto** buscando clases con `@Component` y las crea **todas**.

> ¿Te acordás que en la Etapa 1 dije que `@SpringBootApplication` "escanea tu proyecto buscando clases para administrar"? **Esto es ese escaneo, en acción.** Lo que era una frase abstracta ahora lo estás viendo: pusiste dos `@Component`, Spring encontró las dos.

### Experimento 3: ¿En qué orden se crean?

Corré varias veces y fijate el orden en que aparecen los mensajes. Spring decide el orden de creación (en este caso no importa porque son independientes). Más adelante, cuando un bean dependa de otro, **el orden va a importar** y Spring lo va a resolver solo. Por ahora solo observá que ambos se crean.

> Podés borrar `Saludador` después de este experimento, o dejarlo. No molesta. (Si lo dejás, vas a ver su mensaje en cada arranque — sin problema.)

---

## 📍 Parte 8: Dónde busca Spring las clases

Algo importante que conecta con la Etapa 1.

¿Te acordás que `SpringApplication.run(PaisesApplication.class, args)` recibe `PaisesApplication.class`? Te dije que eso le marca a Spring **"empezá a buscar desde acá"**.

Concretamente: **Spring escanea el package donde está `PaisesApplication`, y todos sus sub-packages.**

```
ar.edu.utn.ba.paises          ← package raíz (acá está PaisesApplication)
├── PaisesApplication.java     ← @SpringBootApplication
├── CatalogoDePaises.java      ← ✅ Spring la encuentra (mismo package)
├── Saludador.java             ← ✅ Spring la encuentra (mismo package)
└── services/                  ← sub-package
    └── OtraClase.java         ← ✅ Spring la encontraría (sub-package)
```

**Por eso tu `CatalogoDePaises` funcionó:** está en el mismo package que `PaisesApplication`, así que cae dentro del área que Spring escanea.

**¿Qué pasaría si la pusieras FUERA de ese árbol?** Por ejemplo, en un package `ar.edu.utn.otracosa` (hermano, no debajo del raíz). Spring **no la escanearía**, no la encontraría, y no crearía el bean — aunque tenga `@Component`.

> **Por eso, por convención, la clase con `@SpringBootApplication` se pone en el package raíz** de tu proyecto: así todo lo que escribas debajo queda dentro del área de escaneo automáticamente. Es lo que el Initializr hizo por vos.

> **Esto es una madriguera opcional:** si querés comprobarlo, podés crear una clase con `@Component` en un package hermano (fuera del árbol) y ver que su mensaje NO aparece. No es necesario, pero si tu curiosidad lo pide, adelante. Lo marco y seguimos.

---

## ✅ Criterios de "Etapa 2 completa"

- [ ] Creaste `CatalogoDePaises` con `@Component` y un `println` en el constructor.
- [ ] Corriste la app y viste el mensaje `>>>` aparecer en el log.
- [ ] Entendés que **vos no hiciste `new`** — lo hizo Spring.
- [ ] Hiciste el Experimento 1 (sacar `@Component`) y viste que el mensaje **desaparece**.
- [ ] Hiciste el Experimento 2 (segundo `@Component`) y viste **ambos** mensajes.
- [ ] Entendés qué es un **bean** (un objeto que Spring crea y administra).
- [ ] Entendés que `@Component` es lo que le dice a Spring "administrá esta clase".
- [ ] Entendés que Spring escanea el package de `PaisesApplication` y sus sub-packages.
- [ ] Volviste a poner `@Component` en `CatalogoDePaises` (no lo dejes comentado).

---

## ✅ Checkpoint

Si podés contestar estas mentalmente, estás listo para la Etapa 3:

1. ¿Quién hizo `new CatalogoDePaises()`? ¿Cómo lo comprobaste?
2. ¿Qué es un bean?
3. ¿Qué hace `@Component`?
4. ¿Qué pasa si sacás `@Component` de una clase? ¿Por qué?
5. ¿Cuántas instancias de un bean crea Spring por defecto? ¿Cómo lo notaste en el log?
6. ¿Dónde busca Spring las clases con `@Component`?
7. ¿Por qué la clase `PaisesApplication` está en el package raíz?

---

## 🎯 Una reflexión antes de seguir

Lo que viste en esta etapa parece poco —un mensajito en el log— pero es **el concepto más importante de todo Spring**.

Pensalo así: en el Proyecto 0, vos eras el dueño de los `new`. Vos decidías cada objeto que se creaba. Ahora **le cediste ese control a Spring**: vos marcás las clases con `@Component`, y Spring se encarga de instanciarlas. Eso es lo que se llama **Inversión de Control** — el control sobre la creación de objetos se "invirtió", pasó de tus manos al framework.

Y lo viste con tus ojos: pusiste `@Component`, apareció el objeto. Lo sacaste, desapareció. **No hay magia. Hay un escaneo y un `new` que hace Spring en vez de vos.**

Pero falta la otra mitad de la historia. Por ahora Spring crea objetos sueltos. **La pregunta que sigue es: ¿cómo hago para que un bean USE a otro bean?** Si `CatalogoDePaises` necesitara al `Saludador`, ¿cómo se lo paso, si yo no controlo el `new`?

Esa es exactamente la **Etapa 3: inyección de dependencias**. Y la vas a ver pasar igual que viste esta: con un `println` que te muestra el momento exacto en que Spring **conecta** dos beans.

---

## ▶️ Próximo paso

Cuando hayas hecho los experimentos y los checkpoints te cierren, decime **"arranquemos etapa 3"**.

En la Etapa 3 vas a:
- Hacer que un bean **use** a otro.
- Ver a Spring **inyectar** un bean dentro de otro automáticamente.
- Entender la **inyección de dependencias por constructor** — la pieza que conecta el rompecabezas.

Si algo de esta etapa no te cerró, preguntá antes de avanzar. **No acumules dudas.**
