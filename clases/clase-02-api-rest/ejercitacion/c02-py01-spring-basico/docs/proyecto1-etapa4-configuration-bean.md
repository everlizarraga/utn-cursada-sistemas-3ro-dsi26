# 🌱 Proyecto 1 — Etapa 4: `@Configuration` y `@Bean`

> **Objetivo:** aprender a convertir en bean una clase que **no es tuya** (que viene de una librería y no le podés agregar `@Component`). Entender `@Configuration` y `@Bean`, y cuándo usar cada forma de declarar beans.
>
> **El momento clave:** vas a ver que podés transformar **cualquier** clase en un bean —incluso una del JDK— usando un método. Y que ese bean se inyecta exactamente igual que los de `@Component`.
>
> **Pre-requisito:** Etapa 3 completa (entendés beans e inyección por constructor).
>
> **Tiempo estimado:** 35-45 minutos.

---

## 🧭 Mapa de esta etapa

1. El problema que `@Component` no puede resolver.
2. La solución: un método que "produce" el bean.
3. Crear la clase de configuración con `@Bean`.
4. Correr y observar.
5. Inyectar el bean (igual que en la Etapa 3).
6. Qué son `@Configuration` y `@Bean`.
7. La pregunta del millón: `@Component` vs `@Bean`, ¿cuándo cada uno?
8. Experimentos.
9. Checkpoint.

---

## 🤔 Parte 1: El problema que `@Component` no puede resolver

Hasta ahora, para que Spring administre una clase, le ponés `@Component` arriba:

```java
@Component
public class CatalogoDePaises {
    // ...
}
```

Esto funciona porque `CatalogoDePaises` **es tu clase** — vos la escribiste, podés editarla, podés ponerle la annotation.

**Pero, ¿qué pasa cuando querés que Spring administre una clase que NO es tuya?**

Por ejemplo, una clase que viene de una librería externa, o del propio Java. No podés abrir `java.util.Random` y ponerle `@Component` arriba — **no es tu código.** No tenés acceso a editarla.

```java
// Esto es IMPOSIBLE — no podés editar Random, no es tu clase:
@Component                          // ❌ no podés agregar esto
public class Random { ... }         // ← esta clase es del JDK, no tuya
```

Entonces, si quisieras que Spring te administre un `Random` como bean (para inyectarlo donde lo necesites), `@Component` **no te sirve**. Necesitás otra forma.

**Esa otra forma es `@Bean`.**

> **Por qué esto importa de verdad:** en apps reales vas a querer que Spring administre un montón de objetos que vienen de librerías — clientes HTTP, conexiones a bases de datos, parsers, etc. Ninguna de esas clases es tuya. `@Bean` es la herramienta para todas ellas. De hecho, en la **Etapa 6** vas a usar `@Bean` para crear un cliente HTTP (que es una clase de Spring, no tuya). Lo que aprendés acá con `Random` es **exactamente** lo mismo que vas a hacer con cosas más serias.

---

## 💡 Parte 2: La solución — un método que produce el bean

La idea: en vez de ponerle la annotation a la clase (que no podés), **escribís un método que crea el objeto y lo devolvés**. A ese método le ponés `@Bean`, y Spring usa lo que el método devuelve como bean.

```
Vos escribís un método que hace "new Random()" y lo devuelve.
                    ↓
Le ponés @Bean al método.
                    ↓
Spring ejecuta ese método al arrancar.
                    ↓
Lo que el método devuelve (el Random) se guarda como bean.
```

**La diferencia clave con `@Component`:**

| | `@Component` | `@Bean` |
|---|---|---|
| Dónde va la annotation | Sobre la **clase** | Sobre un **método** |
| Quién hace el `new` | Spring (automático) | **Vos**, dentro del método |
| Para qué clases | Las **tuyas** | Las que **no son tuyas** (o necesitan construcción especial) |

Fijate la fila del medio: con `@Bean`, **el `new` lo escribís vos** dentro del método. Spring no instancia la clase directamente — ejecuta tu método y guarda lo que devolvés.

---

## ✍️ Parte 3: Crear la clase de configuración

Vamos a crear un bean de tipo `Random` (una clase del JDK, no nuestra) usando `@Bean`.

Creá una clase nueva `AppConfig` en el package `ar.edu.utn.ba.paises`:

```java
package ar.edu.utn.ba.paises;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class AppConfig {

    @Bean
    public Random random() {
        System.out.println(">>> El método @Bean random() se ejecutó");
        return new Random();
    }
}
```

### Qué tiene esta clase

- **`@Configuration`** arriba de la clase → le dice a Spring "esta clase contiene métodos que producen beans, prestá atención a los `@Bean` de adentro".
- **El método `random()`** con **`@Bean`** → le dice a Spring "ejecutá este método al arrancar, y guardá lo que devuelve como un bean".
- **Adentro del método, `new Random()`** → acá **vos** hacés el `new`. Spring no instancia `Random` directamente; ejecuta tu método y toma el resultado.
- **El `println`** → para que veas cuándo se ejecuta el método.

> **Nota sobre el nombre del bean:** por convención, el bean se llama como el método (`random`). Eso importa solo si tuvieras dos métodos `@Bean` que devuelven el mismo tipo (ahí Spring los distingue por nombre). Por ahora, no te detengas en esto.

---

## 🚀 Parte 4: Correr y observar

**Corré la app.** En el log vas a ver:

```
>>> El método @Bean random() se ejecutó
>>> Spring creó un CatalogoDePaises: ...
>>> Spring creó un ServicioDePaises y le inyectó: ...
```

**Apareció `El método @Bean random() se ejecutó`.**

Eso significa que Spring, al arrancar, **ejecutó tu método `random()`** y guardó el `Random` que devolvió como un bean. Vos no llamaste a ese método en ningún lado — **Spring lo llamó solo**, igual que instanciaba las clases `@Component` solo.

**Acabás de crear un bean de una clase que no es tuya.** Tenés un `Random` viviendo en el contenedor de Spring, listo para inyectar donde lo necesites.

---

## 🔌 Parte 5: Inyectar el bean (igual que antes)

Ahora la prueba de que este bean es **igual** que los de `@Component`: lo vamos a inyectar.

Modificá `CatalogoDePaises` para que reciba el `Random` por constructor (igual que el `ServicioDePaises` recibía el `CatalogoDePaises` en la Etapa 3):

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CatalogoDePaises {

    private final Random random;

    public CatalogoDePaises(Random random) {
        this.random = random;
        System.out.println(">>> Spring creó un CatalogoDePaises y le inyectó un Random: " + random);
    }

    public int cantidadDePaises() {
        return 195;
    }

    public int numeroDePaisAlAzar() {
        return random.nextInt(195) + 1;   // un número entre 1 y 195
    }
}
```

**Corré la app.** Vas a ver:

```
>>> El método @Bean random() se ejecutó
>>> Spring creó un CatalogoDePaises y le inyectó un Random: java.util.Random@4f2b...
>>> Spring creó un ServicioDePaises y le inyectó: ...CatalogoDePaises@...
```

**El `CatalogoDePaises` recibió el `Random`** en su constructor. Exactamente igual que recibía un `CatalogoDePaises` el `ServicioDePaises`. **No importa si el bean se creó con `@Component` o con `@Bean` — se inyecta igual.**

> **Esto es importante:** un bean es un bean, sin importar cómo se declaró. `@Component` y `@Bean` son dos formas de **crear** beans, pero una vez creados, viven en el mismo contenedor y se inyectan de la misma manera. Mezclaste un bean de `@Bean` (Random) dentro de un bean de `@Component` (CatalogoDePaises), y funcionó sin fricción.

---

## 🔍 Parte 6: Qué son `@Configuration` y `@Bean`

Ahora que lo viste, pongámosle nombre.

### `@Configuration`

Marca una clase como **"fuente de beans"**. Le dice a Spring: "esta clase no es un bean común — es un lugar donde declaro beans con métodos. Revisá sus métodos `@Bean`".

Una clase `@Configuration` es donde juntás todas las declaraciones de beans que no podés hacer con `@Component`. Es como un "taller de fabricación de beans".

### `@Bean`

Marca un **método** dentro de una `@Configuration`. Le dice a Spring: "ejecutá este método al arrancar, y guardá lo que devuelve como un bean".

El método:
- Hace el `new` de la clase (que vos escribís).
- Puede configurarla antes de devolverla (setear cosas, pasarle parámetros, etc.).
- Devuelve el objeto ya armado.

Spring ejecuta el método **una sola vez** al arrancar, guarda el resultado, y lo reparte donde se necesite. (Sí, también es singleton, igual que `@Component`. Lo comprobás en el Experimento 1.)

### El flujo completo

```
Spring arranca
     ↓
Ve @Configuration en AppConfig
     ↓
Encuentra el método random() con @Bean
     ↓
Ejecuta random()  →  corre tu "new Random()"  →  devuelve un Random
     ↓
Guarda ese Random en el contenedor como bean
     ↓
Cuando CatalogoDePaises pide un Random, Spring se lo inyecta
```

---

## 🧭 Parte 7: La pregunta del millón — `@Component` vs `@Bean`

Esta es la parte conceptual más importante de la etapa. ¿Cuándo usás cada uno?

### La regla simple

| Situación | Usá... | Por qué |
|---|---|---|
| Es **tu clase** (la escribiste vos) | `@Component` | Le ponés la annotation directo, es lo más simple |
| Es una clase de **librería** (no es tuya, no la podés editar) | `@Bean` | No podés ponerle `@Component`, así que la "envolvés" en un método |
| Necesitás **lógica especial** para construirla (configurarla, decidir cuál crear, etc.) | `@Bean` | El método te da lugar para esa lógica |

### En una frase

> **Si podés ponerle `@Component` a la clase, ponéselo. Si no podés (porque no es tuya) o necesitás controlar cómo se construye, usá `@Bean`.**

### Ejemplos concretos

```java
// TUS clases → @Component:
@Component
public class CatalogoDePaises { ... }    // la escribiste vos

@Component
public class ServicioDePaises { ... }    // la escribiste vos

// Clases de LIBRERÍA → @Bean dentro de @Configuration:
@Configuration
public class AppConfig {

    @Bean
    public Random random() {              // Random es del JDK, no tuya
        return new Random();
    }

    // En la Etapa 6 vas a ver algo así:
    // @Bean
    // public RestTemplate restTemplate() {  // RestTemplate es de Spring, no tuya
    //     return new RestTemplate();
    // }
}
```

### Por qué no usar `@Bean` para todo

Podrías declarar **todos** tus beans con `@Bean` (incluso los tuyos), pero sería más verboso: tendrías que escribir un método por cada uno. `@Component` es más directo para tus clases — le ponés la annotation y listo, Spring hace el `new` solo.

**La división tiene sentido:** `@Component` para lo tuyo (cómodo), `@Bean` para lo ajeno (necesario).

---

## 🧪 Parte 8: Experimentos

### Experimento 1: El método `@Bean` se ejecuta una sola vez

Agregá un segundo bean que también pida el `Random`. Modificá `ServicioDePaises` para que reciba un `Random` además del catálogo:

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ServicioDePaises {

    private final CatalogoDePaises catalogo;

    public ServicioDePaises(CatalogoDePaises catalogo, Random random) {
        this.catalogo = catalogo;
        System.out.println(">>> ServicioDePaises recibió el Random: " + random);
    }
}
```

Corré. Mirá los `Random@...` en el log:

```
>>> El método @Bean random() se ejecutó                          ← UNA sola vez
>>> Spring creó un CatalogoDePaises ... un Random: java.util.Random@4f2b...
>>> ServicioDePaises recibió el Random: java.util.Random@4f2b...
```

**El método `random()` apareció una sola vez**, y el **mismo `Random@4f2b...`** se inyectó en los dos lugares. Confirma que `@Bean` también es singleton: Spring ejecuta el método una vez, guarda el resultado, y lo reparte.

> Igual que con `@Component` en la Etapa 3. **Todos los beans son singleton por defecto**, sin importar cómo se declaren. Después de verlo, sacá el `Random` del `ServicioDePaises` si querés dejarlo limpio (o dejalo, no molesta).

### Experimento 2: Sacá `@Bean` y mirá el error

En `AppConfig`, comentá la línea `@Bean`:

```java
// @Bean                                ← comentado
public Random random() {
    System.out.println(">>> El método @Bean random() se ejecutó");
    return new Random();
}
```

Corré.

**Explota al arrancar**, con un error parecido a:

```
Parameter 0 of constructor in ar.edu.utn.ba.paises.CatalogoDePaises 
required a bean of type 'java.util.Random' that could not be found.
```

**¿Por qué?** Sin `@Bean`, el método `random()` es un método común — Spring no lo ejecuta, no se crea ningún bean `Random`. Entonces, cuando `CatalogoDePaises` pide un `Random` en su constructor, Spring no tiene ninguno para inyectar → se planta.

> ¿Te suena? Es **el mismo tipo de error** que viste en la Etapa 3 cuando pediste un `String` que no era bean. La lección es la misma: **Spring solo puede inyectar beans que conoce.** Y `@Bean` es lo que hace que ese `Random` sea conocido.

> **Volvé a poner `@Bean`** antes de seguir.

### Experimento 3 (opcional): Usar el Random de verdad

Hasta ahora el `Random` se inyecta pero no se usa. Para ver que **funciona de verdad**, hacé que el `ServicioDePaises` use el catálogo para sacar un país al azar. Modificá su constructor:

```java
public ServicioDePaises(CatalogoDePaises catalogo) {
    this.catalogo = catalogo;
    int alAzar = catalogo.numeroDePaisAlAzar();
    System.out.println(">>> País al azar elegido por el servicio: número " + alAzar);
}
```

Corré varias veces. Vas a ver que el número **cambia en cada arranque** (porque `Random` da números distintos). Eso prueba que toda la cadena funciona: `ServicioDePaises` → usa → `CatalogoDePaises` → usa → `Random`. Tres beans conectados, trabajando juntos.

> **Nota:** poner lógica en el constructor (como esta llamada a `numeroDePaisAlAzar()`) es solo para esta demostración. En código real, los constructores deberían limitarse a recibir las dependencias, no a ejecutar lógica. Lo marco para que lo sepas, pero para ver la cadena funcionar acá está bien.

---

## ✅ Criterios de "Etapa 4 completa"

- [ ] Creaste `AppConfig` con `@Configuration` y un método `@Bean` que devuelve un `Random`.
- [ ] Corriste la app y viste `El método @Bean random() se ejecutó`.
- [ ] Modificaste `CatalogoDePaises` para recibir el `Random` por constructor.
- [ ] Viste que el `Random` (bean de `@Bean`) se inyecta igual que cualquier bean de `@Component`.
- [ ] Hiciste el Experimento 1 y viste que el método `@Bean` se ejecuta una sola vez (singleton).
- [ ] Hiciste el Experimento 2 (sacar `@Bean`) y viste el error.
- [ ] Entendés cuándo usar `@Component` y cuándo `@Bean`.
- [ ] Volviste a poner `@Bean` (no lo dejaste comentado).

---

## ✅ Checkpoint

Si podés contestar estas mentalmente, estás listo para la Etapa 5:

1. ¿Por qué no podés usar `@Component` con una clase como `java.util.Random`?
2. ¿Dónde va `@Bean` — sobre una clase o sobre un método?
3. ¿Quién hace el `new` cuando usás `@Bean`?
4. ¿Qué hace `@Configuration`?
5. ¿Un bean creado con `@Bean` se inyecta distinto que uno de `@Component`?
6. ¿Cuántas veces ejecuta Spring un método `@Bean`?
7. ¿Cuándo usás `@Component` y cuándo `@Bean`?

---

## 🎯 Una reflexión antes de seguir

Ahora tenés **las dos formas de declarar beans**, y entendés cuándo usar cada una:

- **`@Component`** para tus clases (cómodo, directo).
- **`@Bean`** para clases ajenas o que necesitan construcción especial (un método dentro de `@Configuration`).

Y lo más importante: viste que **no importa cómo se declara un bean** — una vez en el contenedor, todos se comportan igual, se inyectan igual, son singleton igual. Mezclaste un `Random` (de `@Bean`) dentro de un `CatalogoDePaises` (de `@Component`) y la maquinaria de Spring no hizo distinción.

Hasta ahora, todos tus beans son **objetos**. Pero las apps reales necesitan también **configuración**: valores como URLs, puertos, credenciales, nombres de archivos. Esos valores no están en el código — están en un archivo aparte (`application.properties` o `application.yml`), justamente para poder cambiarlos sin tocar el código.

**¿Cómo hace Spring para leer ese archivo y meter esos valores en tu código?** Esa es la **Etapa 5**, y es donde vas a responder de una vez por todas esa duda que arrastrás: *"¿cómo carajo se lee el `application.yml`?"*. Lo vas a construir vos y verlo funcionar.

---

## ▶️ Próximo paso

Cuando hayas hecho los experimentos y los checkpoints te cierren, decime **"arranquemos etapa 5"**.

En la Etapa 5 vas a:
- Poner un valor de configuración en un archivo externo.
- Leerlo desde tu código con `@ConfigurationProperties`.
- **Comprobar con tus ojos que el valor del archivo llegó a tu objeto Java.**
- Entender cómo Spring conecta el archivo de config con tus clases.

Si algo de esta etapa no te cerró, preguntá antes de avanzar. **No acumules dudas.**
