# 🌱 Proyecto 1 — Etapa 3: Inyección de dependencias

> **Objetivo:** hacer que un bean **use** a otro bean, y **ver con tus ojos cómo Spring conecta los dos automáticamente** — sin que vos hagas `new` ni le pases nada manualmente. Entender la inyección de dependencias por constructor.
>
> **El momento clave:** vas a ver un bean recibir a otro bean **ya construido**, sin que vos lo hayas pasado. Spring los enchufó solo.
>
> **Pre-requisito:** Etapa 2 completa (entendés qué es un bean y que Spring lo crea sin `new`).
>
> **Tiempo estimado:** 35-45 minutos.

---

## 🧭 Mapa de esta etapa

1. La pregunta que quedó abierta en la Etapa 2.
2. El problema: si yo no controlo el `new`, ¿cómo le paso un bean a otro?
3. Crear el segundo bean que recibe al primero.
4. Correr y observar — los dos mensajes, en orden.
5. **La revelación:** Spring conectó los dos beans.
6. Qué es la inyección de dependencias.
7. El experimento que prueba que es el mismo objeto.
8. El experimento que muestra qué pasa cuando pedís algo que no es un bean.
9. Checkpoint.

---

## 🤔 Parte 1: La pregunta que quedó abierta

En la Etapa 2 viste que Spring crea objetos (beans) sin que vos hagas `new`. Pusiste `@Component`, apareció el objeto.

Pero quedó una pregunta picando, que te dejé al final:

> Por ahora Spring crea objetos **sueltos**. ¿Cómo hago para que un bean **use** a otro bean?

Pensalo con el ejemplo de tu Proyecto 0. Ahí, cuando un objeto necesitaba a otro, vos se lo pasabas a mano:

```java
CatalogoDePaises catalogo = new CatalogoDePaises();
ServicioDePaises servicio = new ServicioDePaises(catalogo);   // vos le pasás el catálogo
```

Vos hacías los dos `new` y conectabas uno con otro.

**Pero ahora vos NO hacés `new`.** Spring lo hace. Entonces... ¿cómo se entera Spring de que `ServicioDePaises` necesita un `CatalogoDePaises`? ¿Y cómo se lo pasa?

Esa es la pregunta de esta etapa. Y la respuesta la vas a **ver pasar**.

---

## 💡 Parte 2: El truco — pedir lo que necesitás en el constructor

La idea es simple y elegante:

**Un bean declara lo que necesita poniéndolo como parámetro de su constructor. Spring lee ese constructor, ve qué necesita, y se lo provee.**

O sea: en vez de que vos le pases el `CatalogoDePaises` al `ServicioDePaises`, el `ServicioDePaises` **lo pide** en su constructor, y **Spring se lo da**.

```
ServicioDePaises dice: "para construirme, necesito un CatalogoDePaises"
                              ↓
Spring lee eso y dice: "ah, tengo un CatalogoDePaises en mi contenedor, te lo paso"
                              ↓
Spring hace: new ServicioDePaises(elCatalogoQueYaCree)
```

Eso de "Spring te provee lo que pediste" se llama **inyección**. Spring "inyecta" el bean que necesitás dentro del bean que lo pide.

Vamos a verlo.

---

## ✍️ Parte 3: Crear el segundo bean

### Primero, ajustá `CatalogoDePaises`

Abrí `CatalogoDePaises` y modificalo así (le cambiamos el mensaje para que imprima **su propia identidad**, y le agregamos un método que después vamos a usar):

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;

@Component
public class CatalogoDePaises {

    public CatalogoDePaises() {
        System.out.println(">>> Spring creó un CatalogoDePaises: " + this);
    }

    public int cantidadDePaises() {
        return 195;   // hardcodeado por ahora; después traerá datos de verdad
    }
}
```

> **¿Qué es ese `+ this`?** `this` es el objeto actual. Al concatenarlo con un String, Java llama su `toString()`, que por defecto imprime algo como `ar.edu.utn.ba.paises.CatalogoDePaises@1b2c3d`. Ese `@1b2c3d` es un identificador único del objeto. **Lo vamos a usar para comprobar identidad** en el Experimento 1. Guardá ese detalle.

### Ahora, creá `ServicioDePaises`

Creá una clase nueva `ServicioDePaises` en el mismo package:

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;

@Component
public class ServicioDePaises {

    private final CatalogoDePaises catalogo;

    public ServicioDePaises(CatalogoDePaises catalogo) {
        this.catalogo = catalogo;
        System.out.println(">>> Spring creó un ServicioDePaises y le inyectó: " + catalogo);
    }
}
```

### Qué tiene esta clase

- **`@Component`** → es un bean, Spring lo va a crear (ya lo sabés de la Etapa 2).
- **`private final CatalogoDePaises catalogo`** → un atributo donde va a guardar el catálogo que reciba. Es `final` porque una vez que lo recibe, no cambia.
- **El constructor recibe un `CatalogoDePaises`** → acá está el truco. El `ServicioDePaises` **pide** un catálogo para construirse.
- **El `println`** imprime el catálogo que recibió → para que veas qué le llegó.

**Fijate lo que NO hay:** en ningún lado escribiste `new ServicioDePaises(...)` ni `new CatalogoDePaises()`. Vos no instanciás ni conectás nada. Vos solo **declaraste que el servicio necesita un catálogo.**

---

## 🚀 Parte 4: Correr y observar

**Corré la app.** Mirá el log:

```
>>> Spring creó un CatalogoDePaises: ar.edu.utn.ba.paises.CatalogoDePaises@1b2c3d
>>> Spring creó un ServicioDePaises y le inyectó: ar.edu.utn.ba.paises.CatalogoDePaises@1b2c3d
```

(El `@1b2c3d` va a ser otro código en tu máquina, pero **fijate que es el mismo en las dos líneas**. Ya volvemos a eso.)

### Dos cosas para observar

**1. El orden.** El `CatalogoDePaises` se creó **primero**, el `ServicioDePaises` **después**. ¿Por qué ese orden y no al revés?

Porque el `ServicioDePaises` **necesita** un `CatalogoDePaises` para construirse. Spring se da cuenta de eso, y **crea primero lo que se necesita**. Resolvió el orden solo.

> ¿Te acordás que en la Etapa 2 (Experimento 3) te dije "cuando un bean dependa de otro, el orden va a importar y Spring lo va a resolver solo"? **Acá lo estás viendo.** Spring armó el orden correcto sin que vos le dijeras nada.

**2. El servicio recibió el catálogo.** El segundo mensaje muestra que el `ServicioDePaises` recibió un `CatalogoDePaises` en su constructor. **Lo recibió sin que vos se lo pasaras.**

---

## 🤯 Parte 5: La revelación

Pará y pensá lo que pasó:

- Escribiste `ServicioDePaises` con un constructor que pide un `CatalogoDePaises`.
- **No hiciste `new` de ninguno de los dos.**
- **No conectaste nada a mano.**
- Corriste la app.
- Spring creó el catálogo, creó el servicio, **y le pasó el catálogo al servicio.**

**Spring leyó el constructor del `ServicioDePaises`, vio que necesitaba un `CatalogoDePaises`, lo buscó en su contenedor, lo encontró, y se lo pasó.** Todo solo.

Eso es **inyección de dependencias**: Spring "inyecta" (provee) las dependencias que un bean necesita, sin que vos las pases manualmente.

> En el Proyecto 0 vos eras el cable que conectaba los objetos. Ahora **Spring es el cable.** Vos solo decís quién necesita qué (declarándolo en el constructor), y Spring hace todas las conexiones.

---

## 🧩 Parte 6: Qué es la inyección de dependencias

Pongámosle nombre a lo que viste.

**Una "dependencia" es un objeto que otro objeto necesita para funcionar.** El `ServicioDePaises` *depende del* `CatalogoDePaises` — no puede hacer su trabajo sin él. El catálogo es una dependencia del servicio.

**"Inyección de dependencias" (DI) es el acto de proveerle a un objeto sus dependencias desde afuera, en vez de que el objeto las cree por sí mismo.**

Comparalo:

```java
// SIN inyección (el objeto crea su dependencia adentro):
public class ServicioDePaises {
    private final CatalogoDePaises catalogo = new CatalogoDePaises();  // lo crea él mismo
}

// CON inyección (la dependencia llega desde afuera, por constructor):
public class ServicioDePaises {
    private final CatalogoDePaises catalogo;
    public ServicioDePaises(CatalogoDePaises catalogo) {   // alguien se lo pasa
        this.catalogo = catalogo;
    }
}
```

En la segunda versión, el `ServicioDePaises` **no sabe ni le importa cómo se construye el catálogo.** Solo dice "necesito uno, pásenmelo". **Quien se lo pasa es Spring.**

### Por qué esto es bueno (no solo "más elegante")

1. **El servicio no está atado a una forma específica de crear el catálogo.** Si mañana el catálogo se construye distinto (con configuración, con datos de una BD, etc.), el servicio no se entera. Solo recibe uno ya hecho.

2. **Es testeable.** En un test, podés crear un `ServicioDePaises` pasándole un catálogo "de prueba" a mano: `new ServicioDePaises(catalogoFalso)`. El servicio sigue siendo Java normal — Spring no es obligatorio para construirlo.

3. **Las dependencias quedan explícitas.** Mirando el constructor, sabés exactamente qué necesita el servicio para funcionar: un `CatalogoDePaises`. No hay sorpresas escondidas.

Esto que acabás de hacer — **pedir las dependencias en el constructor** — se llama **inyección por constructor**, y es **la forma recomendada** de hacer DI en Spring.

> **Nota al pasar (no te detengas):** existe una annotation llamada `@Autowired` que en código viejo se ponía para marcar la inyección. Hoy, **si la clase tiene un solo constructor, Spring lo usa automáticamente sin necesidad de `@Autowired`** — por eso no la pusiste y funcionó igual. Si en algún código ves `@Autowired`, es eso. No la necesitás. Lo marco y seguimos.

---

## 🔬 Parte 7: Experimento 1 — ¿es el mismo objeto?

Volvé a mirar tu log:

```
>>> Spring creó un CatalogoDePaises: ...CatalogoDePaises@1b2c3d
>>> Spring creó un ServicioDePaises y le inyectó: ...CatalogoDePaises@1b2c3d
```

**El `@1b2c3d` es idéntico en las dos líneas.**

Ese código es la "identidad" del objeto (te lo da el `toString()` por defecto). Que sea el mismo en ambas líneas significa:

> **El `CatalogoDePaises` que se creó (primera línea) es EXACTAMENTE el mismo objeto que recibió el `ServicioDePaises` (segunda línea).**

No son dos catálogos distintos. Es **uno solo**, reutilizado.

Esto confirma algo que mencioné en la Etapa 2: **Spring crea UNA sola instancia de cada bean** (el famoso "singleton"). Ese único `CatalogoDePaises` es el que se inyecta donde se necesite.

### Comprobalo aún más fuerte

Creá un segundo servicio que también pida el catálogo. Creá `OtroServicio`:

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;

@Component
public class OtroServicio {

    public OtroServicio(CatalogoDePaises catalogo) {
        System.out.println(">>> OtroServicio recibió: " + catalogo);
    }
}
```

Corré. Vas a ver:

```
>>> Spring creó un CatalogoDePaises: ...CatalogoDePaises@1b2c3d
>>> Spring creó un ServicioDePaises y le inyectó: ...CatalogoDePaises@1b2c3d
>>> OtroServicio recibió: ...CatalogoDePaises@1b2c3d
```

**Las tres líneas tienen el mismo `@1b2c3d`.** El `CatalogoDePaises` se creó **una sola vez**, y **el mismo objeto** se inyectó en `ServicioDePaises` y en `OtroServicio`.

> **Esto es importante:** no importa cuántos beans necesiten el catálogo — **todos reciben la misma instancia.** Spring no anda creando un catálogo nuevo cada vez. Crea uno, lo guarda, y lo reparte. Eficiente y predecible.

> Podés borrar `OtroServicio` después del experimento, o dejarlo. No molesta.

---

## 🧨 Parte 8: Experimento 2 — pedir algo que NO es un bean

Acá vas a ver qué pasa cuando le pedís a Spring algo que no puede inyectar.

Modificá temporalmente el constructor de `ServicioDePaises` para que pida también un `String`:

```java
public ServicioDePaises(CatalogoDePaises catalogo, String algo) {   // ← agregamos String algo
    this.catalogo = catalogo;
    System.out.println(">>> Spring creó un ServicioDePaises y le inyectó: " + catalogo);
}
```

Corré la app.

**Explota al arrancar.** Vas a ver un error parecido a:

```
Parameter 1 of constructor in ar.edu.utn.ba.paises.ServicioDePaises 
required a bean of type 'java.lang.String' that could not be found.
```

**¿Qué dice ese error?** "El parámetro 1 del constructor (el `String algo`) necesita un bean de tipo `String`, pero no encontré ninguno."

**¿Por qué?** Porque Spring **solo puede inyectar beans que conoce** — objetos que él mismo administra. Un `String` cualquiera no es un bean (no tiene `@Component`, Spring no lo creó). Entonces Spring no sabe qué pasarle ahí, y se planta.

**Esto te enseña algo importante:** la inyección funciona porque Spring **conoce** el `CatalogoDePaises` (es un bean). Si le pedís algo que no es un bean, no puede ayudarte.

> **Sacá el `String algo`** y dejá el constructor como estaba antes de seguir.

> **Conexión:** ¿te acordás de la Etapa 0, cuando creamos el proyecto sin dependencias? Más adelante (Etapa 5) vamos a aprender cómo hacer que valores "simples" como Strings o números **sí** estén disponibles para inyectar, usando configuración. Por ahora, registrá la regla: **Spring inyecta beans, no valores sueltos.**

---

## ✅ Criterios de "Etapa 3 completa"

- [ ] Modificaste `CatalogoDePaises` para imprimir su identidad (`this`).
- [ ] Creaste `ServicioDePaises` que recibe un `CatalogoDePaises` por constructor.
- [ ] Corriste la app y viste **los dos mensajes**, con el catálogo creado **antes** que el servicio.
- [ ] Comprobaste que **no hiciste `new`** de ninguno — Spring los creó y conectó.
- [ ] Hiciste el Experimento 1 y viste que el **mismo `@hash`** aparece en ambos lados (es el mismo objeto).
- [ ] Hiciste el Experimento 2 (pedir un `String`) y viste el error que explica que Spring solo inyecta beans.
- [ ] Entendés qué es una **dependencia** y qué es **inyección de dependencias**.
- [ ] Entendés que la **inyección por constructor** es la forma recomendada.
- [ ] Dejaste el constructor de `ServicioDePaises` limpio (sin el `String` de prueba).

---

## ✅ Checkpoint

Si podés contestar estas mentalmente, estás listo para la Etapa 4:

1. ¿Cómo le decís a Spring que un bean necesita otro bean?
2. ¿Quién le pasó el `CatalogoDePaises` al `ServicioDePaises`? ¿Vos hiciste algún `new`?
3. ¿Por qué el `CatalogoDePaises` se crea **antes** que el `ServicioDePaises`?
4. ¿Qué significa que el `@hash` sea el mismo en todas las líneas del log?
5. ¿Cuántas instancias de `CatalogoDePaises` crea Spring, aunque tres beans lo necesiten?
6. ¿Qué pasa si pedís en el constructor algo que no es un bean (como un `String`)? ¿Por qué?
7. ¿Qué es la "inyección por constructor" y por qué es buena para testear?

---

## 🎯 Una reflexión antes de seguir

Ahora tenés las **dos mitades** del corazón de Spring:

1. **Etapa 2:** Spring crea los objetos por vos (sin `new`).
2. **Etapa 3:** Spring conecta esos objetos entre sí por vos (inyección).

Juntas, estas dos ideas son lo que se llama **Inversión de Control + Inyección de Dependencias** — el núcleo de Spring. Y no te lo aprendiste de memoria: **lo viste pasar.** Viste el objeto aparecer sin `new`, viste el mismo objeto inyectarse en varios lugares, viste a Spring resolver el orden de creación solo.

Pensá en lo que esto significa para una app grande: podés tener 50 beans que dependen unos de otros formando una red complejísima, y **Spring arma todo ese rompecabezas solo** al arrancar. Vos solo declarás "esta clase necesita aquella", y Spring se encarga del resto. Eso es lo que hace a Spring tan poderoso para apps grandes.

Hasta ahora, todos tus beans los creás con `@Component`. Pero hay un caso donde `@Component` **no alcanza**: cuando querés que Spring administre una clase que **no es tuya** (que viene de una librería, y no le podés agregar `@Component`). Para eso existe otra forma de declarar beans, y es la **Etapa 4**.

---

## ▶️ Próximo paso

Cuando hayas hecho los experimentos y los checkpoints te cierren, decime **"arranquemos etapa 4"**.

En la Etapa 4 vas a:
- Aprender a declarar un bean **sin** `@Component`, usando un método.
- Entender `@Configuration` y `@Bean`.
- Entender **cuándo** se usa cada forma (`@Component` vs `@Bean`).

Si algo de esta etapa no te cerró, preguntá antes de avanzar. **No acumules dudas.**
