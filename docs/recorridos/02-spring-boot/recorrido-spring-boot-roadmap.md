# Recorrido Spring Boot — Roadmap

> **Qué es esto.** El puente entre el Java que ya sabés escribir y el proyecto Spring Boot que el profe codeó en vivo en la clase 2 (`rest-paises`). No es un curso de Spring: es exactamente lo que hace falta para que ese repo deje de ser magia.
>
> **Cuándo lo terminás:** leés `rest-paises` de punta a punta y no queda una sola línea que no sepas por qué está.

---

## 🧭 Cómo leer esto

Los puntos van **en orden**. Cada uno se apoya en el anterior; saltear rompe la cadena.

Cada punto arranca con **"De dónde venís"** (lo que se asume sabido) y cierra con un **checkpoint sin respuestas** — no busques las respuestas acá: si no las podés contestar, volvé a la sección. Las respuestas se destilan después, en el complemento.

**Leyenda de marcas:**

| Marca | Significa |
|---|---|
| 🔴 | Central. Sin esto no entendés el resto. |
| 🟡 | Secundario. Suma, pero no bloquea. |
| 🟢 | Mencionado al pasar. Para que sepas que existe. |
| 🕳️ | **Madriguera.** Un tema que asoma y que NO vamos a seguir acá. Trae la coordenada de dónde cae en la cursada. Leela y seguí de largo. |
| 📌 | "Para el parcial, si te preguntan" — respuesta modelo, formato examen. |

---

## El dimensionamiento honesto

**Esto no es un 10. Es un 4 o 5 disfrazado de 10.**

Hay **un solo concepto grande**: la inversión de control — quién hace el `new`. Todo lo demás cuelga de ahí y cae solo.

Se siente enorme por cuatro razones, ninguna de ellas "es difícil":

1. **Llegó todo junto.** Seis anotaciones nuevas en 40 minutos de live coding.
2. **Te dijeron "es magia, no lo mires".** Para tu cabeza esa es la peor instrucción posible. La fricción que sentís no es dificultad: es una deuda cognitiva impaga.
3. **Java es verboso.** Diez líneas para lo que en JS son dos hace que lo simple parezca complejo.
4. **Ya sabés casi todo, con otros nombres.** Express ya te invertía el control. React Context ya te inyectaba dependencias. `.env` ya era configuración externalizada. `JSON.parse` ya era deserialización. Lo nuevo no son los conceptos: es que **Java te obliga a nombrarlos y declararlos**.

---

## Los 6 puntos

| # | Punto | Densidad | Qué resuelve |
|---|---|---|---|
| **1** | Framework vs biblioteca: quién llama a quién | 🔴🔴🟡 | Inversión de control. Qué pasa realmente cuando corrés `main()`. **Abre el hilo: ¿quién hace el `new`?** |
| **2** | Beans: el contenedor instancia por vos | 🔴🔴🔴 | `@Component`, `@Configuration`+`@Bean`. Y **por qué `RestTemplate` necesita `@Bean` pero `BuscadorDePaises` no.** |
| **3** | Inyección de dependencias: cómo llegan las piezas | 🔴🔴🟡 | Constructor vs `@Autowired`. Por qué el `final` no es decorativo. **Cierra el hilo del punto 1.** |
| **4** | Configuración externalizada | 🔴🟡 | `application.yml`, `@ConfigurationProperties`, `@EnableConfigurationProperties`. |
| **5** | De JSON a objetos: Jackson y los DTOs | 🔴🔴🔴 | Por qué en JS el objeto viene gratis y en Java hay que declararlo. Los 3 DTOs del repo. **Y el bug que el profe cometió en vivo.** |
| **6** | Consumir la API: `RestTemplate`, `UriComponentsBuilder` y el test | 🔴🔴🔴 | El código completo, línea por línea. **Cierra el hilo del punto 2 y abre el que resuelve la clase 4.** |

---

## Hilos que se cierran

Un **hilo** es un dolor que se siembra a propósito en un punto y se resuelve varios puntos después. Cuando sientas la incomodidad, no la esquives: es intencional.

```
  PUNTO 1  ──────► "Nadie hace new. ¿Quién instancia BuscadorDePaises?"
                    │
                    ├──► PUNTO 2: el contenedor lo instancia
                    └──► PUNTO 3: y te lo entrega por constructor  ✅ CERRADO


  PUNTO 2  ──────► "¿Por qué RestTemplate necesita @Bean y BuscadorDePaises no?"
                    │
                    └──► PUNTO 6: porque RestTemplate no es tuyo — y ahí lo usás  ✅ CERRADO


  PUNTO 4  ──────► "¿Por qué tanto lío para no hardcodear una URL?"
                    │
                    └──► PUNTO 6: porque el test la usa igual que producción  ✅ CERRADO


  PUNTO 6  ──────► "Los tests le pegan a la API real. Si me quedo sin internet, fallan."
                    │
                    └──► 🚪 NO se cierra acá. Se cierra en la CLASE 4 (mocking).
                          Ese dolor es el motivo por el que existe el mocking.
                          Cuando llegue, va a caer solo.
```

---

## 🗺️ El mapa: dónde vuelve cada cosa

Esta tabla es el **cinturón**. Nada de esto se explica en el recorrido — solo se te dice que existe y **dónde cae**, para que cuando llegue tengas el gancho puesto. Está armada contra el cronograma de la cátedra.

| Lo que ves en el repo `rest-paises` | Dónde vuelve en DSI |
|---|---|
| `@Component` e inyección por constructor | **Clase 4** — orquestación de CU: capas, repositorios, DI. Sus primos `@Service` y `@Repository` aparecen ahí. Lectura previa: *Inyección de dependencias* + *Biblioteca vs Framework*. |
| Los tests `IT` pegándole a la API real | **Clase 4** — mocking (video previo + repo `dds-utn/ejemplo-mockeo`). |
| Consumir una API REST | **Clase 4, Parte II** — la vuelta de tuerca: **exponer** una API REST desde Spring. |
| `UriComponentsBuilder` ("hay un patrón de diseño metido", dijo el profe) | **Clase 8** — patrón **Builder**. |
| El header `Authorization: Bearer <token>` de las slides | **Clase 14** — seguridad de la información, autenticación, autorización. |
| `RestTemplate` como forma de integrar sistemas | **Clase 7** (sincronismo/asincronismo, Cron Tasks) y **Clase 12** (broker, RabbitMQ) — las otras formas de integrar. |
| Los DTOs anotados con Jackson | **Clases 18-21** — persistencia. `@Entity` de JPA/Hibernate usa exactamente el mismo truco de anotaciones sobre un objeto molde. |
| "No quiero spoilear la clase de cliente liviano" (Saclier) | **Clases 23 y 25** — cliente liviano, Thymeleaf, middlewares, SPA. |
| "Otra API que hace agregación de recursos y devuelve uno solo" (Escobar) | **Clase 30** — GraphQL. |
| `@Scheduled` / Cron Tasks (en el stack de la config, no en este repo) | **Clase 7**. |

**Cómo usar esta tabla:** cuando en una clase futura aparezca algo de la columna derecha, volvé una vez acá. No para releer el recorrido — para acordarte de que **ya lo tocaste con las manos**, y que lo que viene es la lección de diseño encima del mecanismo que ya conocés.

---

## Lo que este recorrido NO cubre (a propósito)

- **Exponer** endpoints (`@RestController`, `@GetMapping`) → clase 4. Este repo solo **consume**.
- **Mocking** de dependencias en tests → clase 4.
- **Capas** (`@Service`, `@Repository`) y arquitectura en capas → clase 4.
- **Persistencia**, JPA, Hibernate → clases 18-21.
- Todo el resto del universo Spring (Security, Actuator, AOP, perfiles, Data). No entra en DSI.

Si algo de esta lista te tironea mientras leés: es una **madriguera**. Está marcada 🕳️ en el punto donde asoma, con su coordenada. Anotá la curiosidad y seguí.

---

## Después del recorrido

1. **Complemento del recorrido** — las respuestas de los checkpoints + las dudas que hayan salido en el chat, destiladas.
2. **Apunte maestro de la clase 02** — el material oficial y permanente de la clase (cliente-servidor, HTTP, arquitectura web, API REST + la práctica). Se genera con cobertura plena, igual que siempre. Lo vas a *vivir* como repaso, porque el entendimiento ya ocurrió acá.
3. De ahí salen resumen, machete y autoevaluación cuando los pidas.

---

**FIN DEL ROADMAP**
