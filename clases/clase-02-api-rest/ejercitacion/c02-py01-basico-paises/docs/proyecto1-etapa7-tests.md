# 🌱 Proyecto 1 — Etapa 7: Tests en Spring (`@SpringBootTest`)

> **Objetivo:** testear tu app Spring con el contexto del framework levantado. Reusar JUnit + AssertJ del Proyecto 0. Entender la diferencia entre testear **con** Spring y **sin** Spring.
>
> **Pre-requisito:** Etapas 1-6 completas, incluido el Experimento 2 de la Etapa 6 (`buscarPorNombre`). Idealmente, recordar la Etapa 7 del Proyecto 0 (JUnit + AssertJ).
>
> **Tiempo estimado:** 40-50 minutos. Necesitás internet y tu token configurado (en el `application.yml`, o en el `.env` si hiciste la Etapa 6b) para correr los tests (todos: ver Parte 2).
>
> **Versiones:** Java 21 · Spring Boot 4.0.x · Maven 3.9 (el del wrapper `mvnw`) · JUnit Jupiter + AssertJ (los trae `spring-boot-starter-test`; Boot 4 trae JUnit 6, con la misma API e imports que la 5 del Proyecto 0) · API REST Countries v5.

---

## ⚠️ Nota sobre esta etapa

Igual que la Etapa 6, esta etapa está escrita para la **API v5**, la que pide token y devuelve la estructura anidada `data → objects`.

Los **conceptos** de testing son los mismos para cualquier API. Lo que cambia es:
- los **asserts concretos** (los caminos para llegar a los datos), y
- que los tests que llaman a la API necesitan el token, que tu app lee de la config (del `application.yml`, o del `.env` si hiciste la Etapa 6b).

Donde algo cambia con una API pública sin token (como era la v3.1 cuando se dictó la clase), vas a ver un cuadro así:

> 🔓 **Sin token (API pública).** …

---

## 🧭 Mapa de esta etapa

1. Lo que ya tenés para testear (viene de fábrica).
2. El test que el Initializr ya generó.
3. Tu primer test con Spring (`@SpringBootTest` + `@Autowired`).
4. Testear la llamada HTTP real.
5. Qué significa `@SpringBootTest` y qué cuesta.
6. Tests con Spring vs tests sin Spring.
7. Experimentos.
8. Checkpoint.

---

## 🎁 Parte 1: Lo que ya tenés para testear

Buena noticia: **no tenés que agregar nada.** Cuando el Initializr creó el proyecto (Etapa 0), incluyó `spring-boot-starter-test` en el `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Ese starter trae **JUnit (Jupiter), AssertJ, Mockito y utilidades de test de Spring**, todo junto. **Reconocés JUnit y AssertJ del Proyecto 0.** Acá vienen incluidos, sin que los declares uno por uno.

> ¿Te acordás del Proyecto 0, Etapa 7? Tuviste que agregar JUnit y AssertJ a mano, cada uno con su versión. Acá el starter de test los trae todos, con versiones compatibles ya elegidas por el parent de Spring Boot. Es la misma comodidad de los starters que viste en la Etapa 0.

`<scope>test</scope>` significa "esta dependencia solo se usa para los tests": no viaja dentro de la app final.

---

## 📄 Parte 2: El test que ya existe

El Initializr generó un test básico en `src/test/java/.../PaisesApplicationTests.java`:

```java
package ar.edu.utn.ba.paises;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PaisesApplicationTests {

    @Test
    void contextLoads() {
    }

}
```

**Corré este test** (triángulo verde al lado de la clase o del método → *Run*).

**✅ Deberías ver** el test en verde.

¿Qué prueba? El método `contextLoads()` está **vacío**, pero el test **igual verifica algo**: que **el contexto de Spring arranca sin errores**.

`@SpringBootTest` hace que, antes de correr el test, **Spring arranque toda la app**: crea todos los beans, lee la config, etc. Si algo está mal configurado (un bean que no se puede crear, una dependencia que falta, un error en la config), **el contexto no arranca y el test falla**.

> Por eso un test vacío con `@SpringBootTest` no es inútil: es un **test de humo** (*smoke test*, una prueba mínima de que "enciende sin echar humo") que verifica que toda tu app **se puede ensamblar**. Si pasa, sabés que el andamiaje de beans está sano.

### Ojo: el contexto de los tests es tu app completa

Tres consecuencias que vienen de la Etapa 6:

1. **Necesita tu token.** El contexto lee el `application.yml` igual que la app. Si el token está ahí (Etapa 6), no hay nada que hacer. Si hiciste la **Etapa 6b**, el YAML importa el `.env` con `file:.env[.properties]`, y si no lo encuentra, **no arranca, y fallan todos los tests**:
   - Desde la terminal (`./mvnw test`, parado en la carpeta del proyecto) lo encuentra.
   - Desde IntelliJ, los tests tienen **su propia** configuración de ejecución (tipo **JUnit**), que por defecto arranca desde la carpeta que abriste. Si un test falla con `file [.env] ... does not exist`, poné `$MODULE_WORKING_DIR$` en el **Working directory** de esa configuración, y en la plantilla **JUnit** (*Edit configuration templates…*), igual que hiciste con la app en la Etapa 6b (Parte 8).
   - Si en la Etapa 6b usaste la variante `optional:` con `${RESTCOUNTRIES_API_KEY:rc_live_demo}`, no falla: arranca **con la demo key**, en silencio, y los tests que esperan datos de Argentina pueden fallar sin que quede claro por qué.

2. **Ejecuta tu `Arranque`.** Spring corre los `CommandLineRunner` cada vez que arranca el contexto, también en los tests. O sea: **cada vez que se levanta el contexto de test, tu `Arranque` llama a la API real**. Lo vas a ver en el log del test (`>>> Llamando a la API...`). Consecuencia: hasta el `contextLoads()` vacío depende de internet y del token.

3. **Usá tu token real, no la demo key.** `rc_live_demo` devuelve datos de muestra, que pueden no ser Argentina: el test de la Parte 4 y los Experimentos 1 y 3 pueden fallar con ella.

> 🕳️ **Madriguera — que el `Arranque` no corra en los tests.** Hay formas de decirle a Spring "este bean no, cuando estoy testeando" (por ejemplo, con *perfiles*, que son nombres de modo, como `test`, `dev` o `prod`, que prenden o apagan beans y config: `@Profile("!test")` en `Arranque` y `@ActiveProfiles("test")` en los tests). Para este proyecto no hace falta: alcanza con saber que pasa.
> *Volvé al camino — esto se profundiza aparte, otro día.*

> 🔓 **Sin token (API pública).** No hay token que configurar: los puntos 1 y 3 no aplican. El punto 2 sí: el `Arranque` igual llama a la API en cada contexto de test, y depende de internet.

---

## ✍️ Parte 3: Tu primer test con Spring

Ahora un test que verifique algo concreto: el `CatalogoDePaises`.

Creá la clase de test `CatalogoDePaisesTest` en `src/test/java/.../`:

```java
package ar.edu.utn.ba.paises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CatalogoDePaisesTest {

    @Autowired
    private CatalogoDePaises catalogo;

    @Test
    void elCatalogoSeInyectaCorrectamente() {
        assertThat(catalogo).isNotNull();
    }
}
// Resultado esperado: test en verde
```

### Qué tiene este test

- **`@SpringBootTest`** → arranca el contexto de Spring antes de los tests. Todos los beans se crean, como cuando corrés la app.
- **`@Autowired private CatalogoDePaises catalogo`** → le pide a Spring que **inyecte** el bean en el test. Es la misma inyección de siempre, pero sobre un atributo.
- **`assertThat(catalogo).isNotNull()`** → verifica que el catálogo no sea `null`. Si Spring lo inyectó bien, pasa. **Reconocés `assertThat` de AssertJ del Proyecto 0.**

**Corré el test.** Pasa en verde: comprobaste que Spring puede crear e inyectar tu `CatalogoDePaises`.

> **Sobre `@Autowired` en tests:** en la Etapa 3 dije que `@Autowired` sobre atributos era cosa de código viejo. En **producción**, sí. En **tests** es la forma habitual y aceptada: a una clase de test no la crea tu código, y lo cómodo es pedir los beans por atributo. (Se puede inyectar por constructor también en tests, pero es menos común.) Es la excepción a la regla.

---

## 🌐 Parte 4: Testear la llamada HTTP real

Ahora testeamos que `buscarPorNombre` (Experimento 2 de la Etapa 6) realmente trae datos. Agregá a `CatalogoDePaisesTest`:

```java
@Test
void buscarArgentinaTraeLosDatosCorrectos() {
    Pais argentina = catalogo.buscarPorNombre("argentina");

    assertThat(argentina).isNotNull();

    // El nombre está en names.common / names.official (estructura v5)
    assertThat(argentina.getNames().getCommon()).isEqualTo("Argentina");
    assertThat(argentina.getNames().getOfficial()).isEqualTo("Argentine Republic");

    assertThat(argentina.getRegion()).isEqualTo("Americas");
    assertThat(argentina.getSubregion()).isEqualTo("South America");

    // La capital es una lista de objetos Capital: sacamos el name del primero
    assertThat(argentina.getCapitals().get(0).getName()).isEqualTo("Buenos Aires");

    // La población: verificamos que sea positiva, no el valor exacto (ver abajo)
    assertThat(argentina.getPopulation()).isPositive();
}
// Resultado esperado (con internet y token real): test en verde
```

(Si en la Etapa 6 no hiciste el Experimento 2, hacelo ahora: este test y el Experimento 3 necesitan `buscarPorNombre`.)

### Qué verifica

- Que el `Pais` traído **no sea `null`**.
- Que el nombre común y el oficial sean exactamente los de Argentina.
- Que la región y la subregión sean las esperadas.
- Que la primera capital sea `"Buenos Aires"`: la capital es el `name` de un objeto `Capital`, así que el assert navega esa anidación.
- Que la población sea un número positivo.

> **Sobre la población:** se usa `.isPositive()` y no un valor exacto porque **la población de un país cambia con el tiempo** (la API se actualiza). Si pusieras `.isEqualTo(46735004)`, el test fallaría el día que la API actualice el dato, aunque tu código esté perfecto. **Regla de testing: no ates tus tests a datos que cambian fuera de tu control.** Verificá lo estable (que haya un número positivo), no lo volátil (el valor exacto).

**Corré el test, con internet y tu token.** Pasa en verde si la API responde.

> **Este test hace una llamada HTTP real** a `restcountries.com`, autenticada con tu token. Depende de internet, de que la API funcione y de que tu token sea válido. Si algo de eso falla, el test falla **aunque tu código esté perfecto**. Es un **test de integración**: prueba tu código y la API externa funcionando juntos. Más sobre esto en la Parte 6.

> 🔓 **Sin token (API pública).** El test es el mismo; cambian los caminos, porque la respuesta de la v3.1 era más plana (cuadro de la Parte 8 de la Etapa 6), sin nombre oficial ni subregión:
> ```java
> assertThat(argentina.getNombre().getComun()).isEqualTo("Argentina");
> assertThat(argentina.getRegion()).isEqualTo("Americas");
> assertThat(argentina.getCapitales()).contains("Buenos Aires");   // era List<String>: contains alcanza
> assertThat(argentina.getPoblacion()).isPositive();
> ```
> Con una lista de strings, `contains(...)` verifica directo. Con una lista de objetos, hay que llegar al dato (`get(0).getName()`). Y el test depende solo de internet y de la API, no de un token.

---

## 🚀 Parte 5: Qué significa `@SpringBootTest` y qué cuesta

`@SpringBootTest` hace que **Spring arranque el contexto completo antes de correr los tests de esa clase**.

```
Antes de los tests de la clase:
        ↓
@SpringBootTest arranca Spring
        ↓
Lee application.yml (con tu token; o, si hiciste la 6b, el .env que importa)
        ↓
Crea TODOS los beans (CatalogoDePaises, RestTemplate, PaisesProperties, Arranque…)
        ↓
Corre los CommandLineRunner (tu Arranque llama a la API)
        ↓
Inyecta los @Autowired del test
        ↓
Recién ahí corren los tests
```

### El beneficio

Tus tests corren contra los beans **reales**, configurados como en producción. Si hay un error de configuración, el test lo detecta. Probás tu app "de verdad", ensamblada por Spring.

### El costo

**Arrancar el contexto es lento** (uno o dos segundos, a veces más; en tu caso, más la llamada del `Arranque`). Con 100 clases de test con `@SpringBootTest` y configuraciones distintas, son 100 arranques. Por eso, en proyectos grandes, no todos los tests lo usan: solo los que de verdad necesitan el contexto. Los demás se testean "sin Spring" (Parte 6).

> Spring optimiza algo: si varias clases de test usan **la misma configuración**, reutiliza el contexto entre ellas en vez de arrancarlo de cero. Pero igual, `@SpringBootTest` es la opción "pesada".

---

## ⚖️ Parte 6: Tests con Spring vs tests sin Spring

Esta distinción es importante para tu futuro.

### Test CON Spring (`@SpringBootTest`)

Lo que hiciste. Spring arranca e inyecta beans reales. Útil para:
- verificar que la app se ensambla (`contextLoads`);
- probar el flujo real de punta a punta (la llamada HTTP de verdad).

**Lento, y depende de la configuración real** (y, en tu caso, de internet, de la API y del token).

### Test SIN Spring (unitario puro)

Como en el Proyecto 0: creás los objetos **a mano**, sin ningún framework.

```java
class AlgunaLogicaTest {

    @Test
    void ejemplo() {
        // Creás el objeto vos mismo, sin Spring
        var algo = new MiClase(/* dependencias a mano, o versiones falsas */);
        // ... assert
    }
}
// Resultado esperado: corre en milisegundos, sin levantar Spring
```

**No usa `@SpringBootTest`. No arranca Spring. Es instantáneo.** Probás una clase aislada, pasándole sus dependencias vos mismo, o versiones "falsas" de ellas, llamadas **mocks**.

> ¿Te acordás que en la Etapa 3 dije que la inyección por constructor hace las clases "testeables sin Spring"? **Esto es a lo que me refería.** Como `CatalogoDePaises` recibe `RestTemplate` y `properties` por constructor, podés crear uno a mano en un test, pasándole un `RestTemplate` falso, y probar su lógica sin levantar Spring ni llamar a la API real.

### La regla práctica (la "pirámide de tests")

| Tipo de test | Velocidad | Cuándo usarlo | Cuántos tener |
|---|---|---|---|
| **Sin Spring** (unitario puro) | Rapidísimo | Probar la lógica de una clase aislada | Muchos (la mayoría) |
| **Con Spring** (`@SpringBootTest`) | Lento | Verificar ensamblaje, flujos completos | Pocos |

**La mayoría de tus tests deberían ser sin Spring** (rápidos, enfocados). Solo algunos, con Spring.

> En este proyecto chico usamos `@SpringBootTest` para todo, por simplicidad. En proyectos grandes, balanceás. Lo importante es que **sepas que las dos formas existen** y para qué sirve cada una.

> **Sobre tu test de la API:** el test que llama a `restcountries.com` de verdad es lento y frágil (depende de red, de la API y del token). En proyectos serios, ese tipo de llamada externa se suele **mockear** (simular la respuesta sin llamar a internet), y los tests contra la API real quedan para casos contados. Por ahora, llamarla de verdad está bien para que veas el flujo completo.

> 🕳️ **Madriguera — mocks.** Mockito (que ya vino con el starter de test) crea objetos falsos: un `RestTemplate` falso al que le decís "cuando te llamen, devolvé esta respuesta" (un `RespuestaPaises` armado a mano). Spring también trae `MockRestServiceServer`, que simula la API devolviendo un JSON para cada URL. Con cualquiera de los dos, el test de `buscarPorNombre` no necesitaría internet ni token.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 🧪 Parte 7: Experimentos

### Experimento 1: Hacé fallar un test a propósito

En `buscarArgentinaTraeLosDatosCorrectos`, cambiá un assert:

```java
assertThat(argentina.getRegion()).isEqualTo("Europa");   // mal a propósito
```

Corré. **Resultado esperado:** el test **falla**, y AssertJ te muestra un mensaje claro:

```
expected: "Europa"
 but was: "Americas"
```

**Confirma que tu test realmente verifica algo.** Un test que nunca puede fallar es inútil. Volvé a poner `"Americas"`.

### Experimento 2: El `contextLoads` rompible

En `AppConfig`, comentá el `@Bean` del `RestTemplate`:

```java
// @Bean
public RestTemplate restTemplate() { ... }
```

Corré `contextLoads()`. **Resultado esperado:** falla, porque sin el bean `RestTemplate` el `CatalogoDePaises` no se puede crear (lo pide en su constructor), y el contexto no arranca. En el error vas a ver algo como:

```
Parameter 0 of constructor in ar.edu.utn.ba.paises.CatalogoDePaises required a bean of type
'org.springframework.web.client.RestTemplate' that could not be found.
```

(Falla antes de llegar al `Arranque`: este error no necesita la API.)

**Esto muestra el valor del `contextLoads`:** detecta errores de ensamblaje sin que escribas un solo assert. Volvé a poner el `@Bean`.

### Experimento 3: Test parametrizado (varios países)

Probá varios países en un solo test con `@ParameterizedTest`, como en el Proyecto 0:

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ParameterizedTest
@ValueSource(strings = {"argentina", "brazil", "japan"})
void todosEstosPaisesSeEncuentran(String nombre) {
    Pais pais = catalogo.buscarPorNombre(nombre);
    assertThat(pais).isNotNull();
    assertThat(pais.getNames().getCommon()).isNotBlank();
    assertThat(pais.getCapitals()).isNotEmpty();
}
// Resultado esperado: el mismo test corre 3 veces, una por país, las 3 en verde
```

**Reusás exactamente lo del Proyecto 0:** JUnit es JUnit, con o sin Spring.

> 🔓 **Sin token (API pública).** Mismo test, con los getters de la v3.1: `pais.getNombre().getComun()` y `pais.getCapitales()`.

### Experimento 4: Token inválido

En la config (o en el `.env`, si hiciste la Etapa 6b), cambiá el token por uno inválido (por ejemplo `rc_live_xxxxx`) y corré `CatalogoDePaisesTest`.

**Resultado esperado:** fallan **todos** los tests de la clase, incluso `elCatalogoSeInyectaCorrectamente`, que no llama a la API. ¿Por qué? Por el punto 2 de la Parte 2: al arrancar el contexto, tu `Arranque` llama a la API, la API responde `401 Unauthorized`, y **el contexto no llega a arrancar**. Buscá el 401 en el error: es un error de **autenticación**, no de un assert (compará con el Experimento 1).

Esto muestra que un test de integración depende de cosas fuera de tu código: acá, del token. Volvé a poner el token bueno.

> 🔓 **Sin token (API pública).** Este experimento no aplica. El equivalente es cortar internet: falla igual, por el mismo motivo (el `Arranque` no puede llamar a la API).

---

## ✅ Criterios de "Etapa 7 completa"

- [ ] Corriste el `contextLoads()` que ya venía y entendés qué prueba.
- [ ] Los tests encuentran tu token (y, si hiciste la 6b, tu `.env`, desde IntelliJ y con `./mvnw test`).
- [ ] Entendés que el contexto de test ejecuta tu `Arranque` (y por qué eso hace que dependa de la API).
- [ ] Creaste `CatalogoDePaisesTest` con `@SpringBootTest` y `@Autowired`.
- [ ] Escribiste un test que verifica que el catálogo se inyecta.
- [ ] Escribiste un test de la llamada HTTP real, con asserts que navegan la estructura de la v5.
- [ ] Usaste `.isPositive()` para la población (no un valor exacto) y entendés por qué.
- [ ] Usaste `assertThat` de AssertJ (reusando lo del Proyecto 0).
- [ ] Hiciste el Experimento 1 (test que falla) y viste el mensaje de AssertJ.
- [ ] Hiciste el Experimento 2 (romper el contexto) y entendés qué prueba `contextLoads`.
- [ ] Entendés la diferencia entre tests con Spring y sin Spring.
- [ ] Dejaste todo en verde (sin asserts rotos, sin `@Bean` comentados, con tu token bueno en la config).

---

## ✅ Checkpoint

1. ¿Tuviste que agregar JUnit y AssertJ? ¿De dónde salieron?
2. ¿Qué prueba el test `contextLoads()` aunque esté vacío?
3. ¿Qué hace `@SpringBootTest`? ¿Qué pasa con tu `Arranque` cuando arranca el contexto de test?
4. (Si hiciste la 6b) Un test falla con `file [.env] ... does not exist` desde IntelliJ, pero con `./mvnw test` pasa. ¿Qué está pasando?
5. ¿Por qué `@Autowired` sobre un atributo está bien en tests pero no en producción?
6. ¿Por qué el assert de la población usa `.isPositive()` y no el valor exacto?
7. ¿Por qué el assert de la capital es `getCapitals().get(0).getName()`? ¿Cuándo alcanzaría con `contains("Buenos Aires")`?
8. ¿Cuál es el costo de `@SpringBootTest`?
9. ¿Qué diferencia hay entre un test con Spring y uno sin Spring?
10. ¿Por qué el test de la llamada HTTP depende de internet **y del token**? ¿Qué riesgo tiene eso?
11. Con un token inválido, ¿por qué falla también el test que solo verifica la inyección?
12. ¿Cuál es la regla práctica sobre cuántos tests con Spring vs sin Spring tener?

---

## 🎯 Una reflexión — y el cierre del Proyecto 1

Llegaste al final. Mirá todo lo que construiste, pieza por pieza, viendo cada una funcionar:

| Etapa | Qué dominás |
|---|---|
| 0 | Crear un proyecto Spring desde cero (y entender qué es) |
| 1 | El arranque, qué es Spring, qué hace `SpringApplication.run` |
| 2 | Beans y `@Component`: Spring crea objetos sin `new` |
| 3 | Inyección de dependencias por constructor |
| 4 | `@Configuration` + `@Bean`: beans de clases ajenas |
| 5 | Configuración externa con `@ConfigurationProperties` (y la 5b, a fondo) |
| 6 | Llamadas HTTP reales con `RestTemplate` + Jackson, **autenticadas con token**, contra una API que devuelve estructuras complejas |
| 6b · 6c | (Opcionales) El token protegido en un `.env`, y la llamada HTTP por dentro: qué viaja, qué representa cada objeto y cuándo espera tu código |
| 7 | Tests con `@SpringBootTest` y AssertJ |

**Lo importante no es la lista: es CÓMO la aprendiste.** No te tiraron el código terminado para que adivines cómo funciona. Construiste cada pieza, la corriste, la viste pasar, entendiste por qué existe, y recién ahí sumaste la siguiente. Beans que aparecen sin `new`, el mismo objeto inyectándose en varios lados, la config saltando del archivo a tu código, datos reales llegando de internet. **Todo lo viste con tus ojos.**

Y te tocó algo que la mayoría de los tutoriales evita: **una API del mundo real que cambió, que exige autenticación y que devuelve un monstruo anidado.** No te quedaste con el `getForObject` de juguete: aprendiste a mandar tokens con `exchange`, a modelar estructuras anidadas con DTOs y a quedarte solo con los datos que te importan. Y sabés cómo sería con una API abierta.

Ahora, si abrís cualquier código Spring del mundo real (de tu cursada, de un repo, de donde sea) ya no es un jeroglífico. Cuando veas un `@Component`, un `@Bean`, una inyección por constructor, un `@ConfigurationProperties`, un `RestTemplate`, vas a saber qué hace cada uno y por qué está ahí. **Eso es pararse sobre bases sólidas.** Y lo lograste construyendo.

---

## ▶️ Hacia dónde seguir

Algunas direcciones posibles, según lo que necesites:

- **Exponer tus propios endpoints HTTP** (`@RestController`): que tu app **reciba** pedidos, no solo que los haga. Es el paso natural si querés hacer una API web.
- **Manejo de errores** en las llamadas (qué hacer cuando la API falla, el token expira o el país no existe).
- **Construcción correcta de URLs** (`UriComponentsBuilder`, la madriguera de la Etapa 6).
- **Tests más finos:** mocks y tests unitarios puros de beans, sin levantar Spring ni llamar a internet.
- **Persistencia** (guardar datos en una base con Spring Data JPA).

Si te quedó alguna duda en el camino, anotala y traela. **Tenés la base. El resto es construir sobre ella.**
