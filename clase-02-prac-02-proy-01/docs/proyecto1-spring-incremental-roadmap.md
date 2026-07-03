# 🌱 Proyecto 1 — Spring Boot Incremental (Learning by Doing)

> **Premisa:** vas a construir una app Spring Boot **desde cero**, agregando UNA pieza del framework por vez. Cada etapa: construís algo mínimo, lo **corrés**, ves el resultado **con tus ojos**, entendés por qué existe, y recién ahí avanzás.
>
> **La regla de oro de este proyecto:** nunca te voy a pedir que te "guardes" un concepto para después. Cada cosa se explica **cuando ya tenés todo lo necesario para entenderla**, no antes. Si menciono que algo más avanzado existe, es solo para que no te sorprenda — **no para que cargues con ello**.

---

## 🧭 Filosofía (la misma que te funcionó en el Proyecto 0)

- Cada etapa = un cambio incremental al mismo proyecto.
- Cada etapa introduce **una sola pieza nueva** de Spring (a veces dos si están muy relacionadas).
- **Construís → corrés → observás → entendés → avanzás.** En ese orden. Siempre.
- **Nada de teoría flotante.** Primero ves la cosa funcionando, después entendés el concepto.
- **Nada se descarta.** Todo lo que construís se reusa en etapas siguientes.
- Vos tipeás el código vos mismo. Yo te doy instrucciones + ejercicios, no código para copy-paste a ciegas.
- Si te trabás, preguntás por chat y desempantanamos.
- Cuando haya **varias formas de hacer algo**, te muestro el panorama, te marco **cuál usás ahora y por qué**, y señalo "el resto existe, lo vemos cuando lo necesites" — sin pedirte que lo memorices.

---

## 🎯 Qué vas a entender al terminar

No "qué hizo tal persona", sino **cómo funciona Spring por dentro**, construido por tus propias manos:

- Qué es Spring realmente (no la definición de manual — la intuición de haberlo usado).
- Qué es un **bean** y por qué dejás de usar `new`.
- Qué es la **inversión de control (IoC)** y la **inyección de dependencias**, viéndolas pasar en vivo.
- Cómo Spring **lee configuración** de un archivo externo y la mete en tu código.
- Cómo se hace una **llamada HTTP** a una API externa desde Spring.
- Cómo se **testea** una app Spring.
- El **ciclo de vida** de una app: qué pasa desde que apretás Run hasta que queda viva.

---

## 📦 Stack

| Tool | Versión | Cuándo entra |
|---|---|---|
| Java | 21 | Etapa 0 |
| Maven | el que viene con IntelliJ | Etapa 0 |
| Spring Boot | 3.x (última estable) | Etapa 0 |
| IntelliJ IDEA Community | Última | Etapa 0 |
| Lombok | Última | cuando lo necesitemos (lo traés del Proyecto 0) |
| JUnit 5 + AssertJ | Última | etapa de tests |

> **Lo que ya sabés y vas a reusar:** todo el Java moderno del Proyecto 0 (clases, Optional, streams, Maps, Lombok, tests con AssertJ). Spring **no reemplaza** nada de eso — lo **organiza**. Vas a ver que el Java de adentro es el mismo que ya dominás.

---

## 🗺️ Mapa de etapas

> Este es el mapa. Cada etapa se genera como su propio archivo `.md` cuando llegás a ella. **No las generamos todas de una** — vamos una por vez, la hacés, la ves funcionar, y seguimos.

| Etapa | Pieza nueva de Spring | Qué construís y qué COMPROBÁS con tus ojos |
|---|---|---|
| **0** | Crear el proyecto Spring | Crear un proyecto Spring Boot desde cero (varias formas). Entender qué te genera y por qué. Compararlo con tu Maven manual del Proyecto 0. |
| **1** | El arranque | Un Spring Boot **vacío** que arranca. Ver el log, el banner, el proceso que queda vivo. Entender qué es Spring y qué hace `SpringApplication.run`. Matarlo y arrancarlo. |
| **2** | Tu primer bean (`@Component`) | Una clase con `@Component` que imprime algo al arrancar. **Comprobás que Spring la creó sin que vos hicieras `new`.** Acá *ves* qué es un bean. |
| **3** | Inyección de dependencias | Un segundo componente que usa al primero. **Ves a Spring conectarlos solo.** Acá *ves* qué es la inyección por constructor. |
| **4** | `@Configuration` + `@Bean` | Declarar un bean con un método (no con `@Component`). Entender cuándo se usa cada forma. |
| **5** | Configuración externa (`application.yml` + `@ConfigurationProperties`) | Leer un valor de un archivo de config y comprobar que llegó a tu código. **Acá respondés "cómo carajo se lee ese archivo".** |
| **6** | Llamada HTTP (`RestTemplate`) | Hacer una llamada real a una API externa y traer datos. Deserializar JSON a objetos Java. |
| **7** | Tests en Spring (`@SpringBootTest`) | Testear la app con el contexto de Spring levantado. Reusás AssertJ del Proyecto 0. |
| **8** | (opcional / a definir) | Exponer un endpoint HTTP propio (`@RestController`), o profundizar en algo que haya quedado picando. Lo decidimos sobre la marcha. |

> El número final de etapas puede ajustarse. Si una pieza resulta más densa de lo esperado, la partimos en dos. Si algo fluye, lo juntamos. **El mapa sirve, pero el terreno manda.**

---

## 🔄 Cómo trabajamos juntos

1. Vos decís **"arranquemos etapa N"**.
2. Yo te genero el material de esa etapa: contexto mínimo + instrucciones paso a paso + **el momento exacto donde corrés y observás** + ejercicios + criterios de "terminado".
3. **Vos tipeás el código vos mismo.** El material te guía; el código lo escribís vos.
4. **Corrés y observás** en cada punto marcado. Si lo que ves no coincide con lo que esperabas, ahí preguntás.
5. Si te trabás, preguntás por chat. Resolvemos sin que yo te tire el código terminado salvo que sea inevitable.
6. Cuando completaste la etapa, decís **"listo, etapa N+1"** y seguimos.

Cada etapa tiene su archivo `.md`, ej: `proyecto1-etapa2-primer-bean.md`.

---

## 🎯 Dominio: lo definimos en la Etapa 0

El Proyecto 0 fue sobre "países". Para el Proyecto 1 podemos:

- **Seguir con países** (aprovechás la intuición ya construida y comparás directo).
- **Cambiar de dominio** (libros, tareas, productos, lo que te resulte más motivante).

Lo decidimos al arrancar la Etapa 0. La estructura del aprendizaje es la misma; solo cambia sobre qué datos trabajás.

---

## 🧠 Una nota sobre cómo está pensado este proyecto

Este Proyecto 1 existe porque **el mundo Spring es grande y nuevo para vos**, y la única forma sólida de entrarlo es **construyéndolo de a una pieza**, no mirándolo entero de golpe.

Vas a notar una diferencia con cualquier material que hayas visto antes: acá **el orden está al servicio de tu cabeza**, no al servicio de "mostrar el sistema terminado". Primero la pieza más chica que funcione. La corrés. La ves. Recién cuando la entendés, sumás la siguiente. Como armar con bloques: no te muestro la torre terminada y te pido que adivines cómo se sostiene — ponés un bloque, ves que se sostiene, ponés el siguiente.

Cuando termines, si en algún momento querés volver a mirar código Spring "del mundo real" (de la cursada, de un repo, de donde sea), **ya no va a ser un jeroglífico** — va a ser un repaso de piezas que construiste con tus manos.

---

## ▶️ Próximo paso

Decime **"arranquemos etapa 0"** y te genero el material para crear el proyecto Spring desde cero. Va a ser **completo y con todo el detalle**: qué formas hay de crear un proyecto Spring, qué hace cada una por debajo, qué te genera y por qué, y cómo se conecta con lo que ya sabés de Maven manual.

Si querés definir el dominio (seguir con países o cambiar) antes de arrancar, decímelo en ese mismo mensaje.
