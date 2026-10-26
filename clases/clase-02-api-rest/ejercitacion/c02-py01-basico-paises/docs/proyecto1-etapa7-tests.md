# 🌱 Proyecto 1 — Etapa 7: Tests en Spring (`@SpringBootTest`)

> **Objetivo:** testear tu app Spring con el contexto del framework levantado. Reusar JUnit + AssertJ del Proyecto 0. Entender la diferencia entre testear **con** Spring y **sin** Spring.
>
> **Pre-requisito:** Etapas 1-6 completas. Idealmente, recordar la Etapa 7 del Proyecto 0 (JUnit + AssertJ).
>
> **Tiempo estimado:** 40-50 minutos. Necesitás internet para los tests que llaman a la API.

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

Buena noticia: **no tenés que agregar nada.** Cuando el Initializr creó el proyecto (Etapa 0), incluyó el `spring-boot-starter-test` en el `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Ese starter trae **JUnit 5, AssertJ, Mockito y utilidades de test de Spring** — todo junto. **Reconocés JUnit y AssertJ del Proyecto 0.** Acá vienen incluidos automáticamente, sin que los declares uno por uno.

> ¿Te acordás del Proyecto 0, Etapa 7? Tuviste que agregar JUnit y AssertJ a mano, cada uno con su versión. Acá el starter de test los trae todos, con versiones compatibles ya elegidas por el parent de Spring Boot. Es la misma comodidad de los starters que viste en la Etapa 0.

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

**Corré este test** (triángulo verde al lado de la clase o del método, → Run).

¿Qué hace? El método `contextLoads()` está **vacío**, pero el test **igual prueba algo**: verifica que **el contexto de Spring arranca sin errores**.

Pensalo: `@SpringBootTest` hace que, antes de correr el test, **Spring arranque toda la app** (crea todos los beans, lee la config, etc.). Si algo está mal configurado —un bean que no se puede crear, una dependencia que falta, un error en la config— **el contexto no arranca y el test falla**.

> Por eso un test vacío con `@SpringBootTest` no es inútil: es un "humo test" que verifica que toda tu app **se puede ensamblar**. Si pasa, sabés que el andamiaje de beans está sano.

---

## ✍️ Parte 3: Tu primer test con Spring

Ahora un test que verifique algo concreto. Vamos a testear el `CatalogoDePaises`.

Creá una clase de test `CatalogoDePaisesTest` en `src/test/java/.../`:

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
```

### Qué tiene este test

- **`@SpringBootTest`** → arranca el contexto de Spring antes de los tests. Todos los beans se crean, como cuando corrés la app.

- **`@Autowired private CatalogoDePaises catalogo`** → le pide a Spring que **inyecte** el bean `CatalogoDePaises` en este test. Es la misma inyección de siempre, pero acá usás `@Autowired` sobre un atributo (en tests es la forma normal — los tests no tienen constructor donde inyectar).

- **`assertThat(catalogo).isNotNull()`** → verifica que el catálogo no sea null. Si Spring lo inyectó bien, no es null, el test pasa. **Reconocés `assertThat` de AssertJ del Proyecto 0.**

**Corré el test.** Pasa en verde. Acabás de comprobar que Spring puede crear e inyectar tu `CatalogoDePaises`.

> **Sobre `@Autowired` en tests:** ¿te acordás que en la Etapa 3 dije que `@Autowired` sobre atributos era cosa de código viejo? En **producción** sí. Pero en **tests** es la forma normal y aceptada, porque los tests no tienen un constructor propio donde inyectar. Es la excepción a la regla. No te preocupes por esto.

---

## 🌐 Parte 4: Testear la llamada HTTP real

Ahora testeamos que `buscarPorNombre` (de la Etapa 6) realmente trae datos. Agregá este test a `CatalogoDePaisesTest`:

```java
@Test
void buscarArgentinaTraeLosDatosCorrectos() {
    Pais argentina = catalogo.buscarPorNombre("argentina");

    assertThat(argentina).isNotNull();
    assertThat(argentina.getNombre().getComun()).isEqualTo("Argentina");
    assertThat(argentina.getRegion()).isEqualTo("Americas");
    assertThat(argentina.getCapitales()).contains("Buenos Aires");
}
```

(Si en la Etapa 6 quedaste con `buscarArgentina()` en vez de `buscarPorNombre(...)`, ajustá el nombre del método.)

### Qué verifica

- Que el `Pais` traído **no sea null**.
- Que el nombre común sea exactamente `"Argentina"`.
- Que la región sea `"Americas"`.
- Que las capitales **contengan** `"Buenos Aires"`.

**Corré el test (con internet).** Pasa en verde si la API responde correctamente.

> **Ojo:** este test hace una **llamada HTTP real** a `restcountries.com`. Eso significa que **depende de internet y de que la API esté funcionando**. Si la API está caída o no tenés red, el test falla **aunque tu código esté perfecto**. Esto se llama un "test de integración" — prueba que tu código + la API externa funcionan juntos. Volvé a esto en la Parte 6.

---

## 🚀 Parte 5: Qué significa `@SpringBootTest` y qué cuesta

`@SpringBootTest` es la annotation que hace que **Spring arranque el contexto completo antes de correr los tests de esa clase**.

```
Antes de correr los tests de la clase:
        ↓
@SpringBootTest hace que Spring arranque
        ↓
Crea TODOS los beans (CatalogoDePaises, RestTemplate, PaisesProperties, etc.)
        ↓
Lee application.yml
        ↓
Inyecta los @Autowired del test
        ↓
Recién ahí corren los tests
```

### El beneficio

Tus tests corren contra los beans **reales**, configurados como en producción. Si hay un error de configuración, el test lo detecta. Probás tu app "de verdad", ensamblada por Spring.

### El costo

**Arrancar el contexto de Spring es lento** (uno o dos segundos, a veces más). Si tenés 100 clases de test con `@SpringBootTest`, son 100 arranques. Por eso, en proyectos grandes, no todos los tests usan `@SpringBootTest` — solo los que de verdad necesitan el contexto. Los demás se testean "sin Spring" (ver Parte 6).

> Spring optimiza algo: si varias clases de test usan **la misma configuración**, reutiliza el contexto entre ellas (no arranca de cero cada vez). Pero igual, `@SpringBootTest` es la opción "pesada".

---

## ⚖️ Parte 6: Tests con Spring vs tests sin Spring

Esta distinción es importante para tu futuro.

### Test CON Spring (`@SpringBootTest`)

Lo que hiciste arriba. Spring arranca, inyecta beans reales. Útil para:
- Verificar que la app se ensambla (`contextLoads`).
- Probar el flujo real end-to-end (la llamada HTTP de verdad).

**Lento, depende de configuración real (y de internet, en tu caso).**

### Test SIN Spring (test unitario puro)

Acordate del Proyecto 0: ahí testeabas creando los objetos **a mano**, sin ningún framework:

```java
class CatalogoDePaisesTest {

    @Test
    void ejemplo() {
        // Creás el objeto vos mismo, sin Spring
        var catalogo = new CatalogoDePaises(/* dependencias a mano */);
        // ... assert
    }
}
```

**No usa `@SpringBootTest`. No arranca Spring. Es instantáneo.** Probás una clase aislada, pasándole sus dependencias vos mismo (o versiones "falsas" de ellas, llamadas mocks).

> ¿Te acordás que en la Etapa 3 dije que la inyección por constructor hace las clases "testeables sin Spring"? **Esto es a lo que me refería.** Como `CatalogoDePaises` recibe sus dependencias por constructor, podés crear uno a mano en un test, pasándole un `RestTemplate` falso, y probar su lógica sin levantar todo Spring.

### La regla práctica (la "pirámide de tests")

| Tipo de test | Velocidad | Cuándo usarlo | Cuántos tener |
|---|---|---|---|
| **Sin Spring** (unitario puro) | Rapidísimo | Probar lógica de una clase aislada | Muchos (la mayoría) |
| **Con Spring** (`@SpringBootTest`) | Lento | Verificar ensamblaje, flujos completos | Pocos |

**La mayoría de tus tests deberían ser sin Spring** (rápidos, enfocados). Solo algunos, con Spring (para lo que de verdad necesita el contexto).

> En este proyecto chico, usamos `@SpringBootTest` para todo porque es simple. En proyectos grandes, balanceás. Lo importante es que **sepas que las dos formas existen** y para qué sirve cada una. El detalle de mocks y tests unitarios puros de beans es una madriguera que podés explorar más adelante.

---

## 🧪 Parte 7: Experimentos

### Experimento 1: Hacé fallar un test a propósito

En `buscarArgentinaTraeLosDatosCorrectos`, cambiá el assert:

```java
assertThat(argentina.getRegion()).isEqualTo("Europa");   // mal a propósito
```

Corré. El test **falla**, y AssertJ te muestra un mensaje claro:

```
expected: "Europa"
 but was: "Americas"
```

**Confirma que tu test realmente verifica algo.** Un test que nunca puede fallar es inútil. Volvé a poner `"Americas"`.

### Experimento 2: El `contextLoads` rompible

Andá a `AppConfig` y comentá el `@Bean` del `RestTemplate`:

```java
// @Bean
public RestTemplate restTemplate() { ... }
```

Corré el test `contextLoads()`. **Falla**, porque sin el bean `RestTemplate`, el `CatalogoDePaises` no se puede crear (lo necesita en su constructor), entonces el contexto no arranca.

**Esto te muestra el valor del `contextLoads`:** detecta errores de ensamblaje sin que tengas que escribir asserts. Volvé a poner el `@Bean`.

### Experimento 3: Test parametrizado (varios países)

Si querés, probá testear varios países en un solo test usando `@ParameterizedTest` (lo viste en el Proyecto 0):

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ParameterizedTest
@ValueSource(strings = {"argentina", "brazil", "japan"})
void todosEstosPaisesSeEncuentran(String nombre) {
    Pais pais = catalogo.buscarPorNombre(nombre);
    assertThat(pais).isNotNull();
    assertThat(pais.getNombre().getComun()).isNotBlank();
}
```

Corre el mismo test 3 veces, una por país. **Reusás exactamente lo del Proyecto 0** — JUnit es JUnit, con o sin Spring.

---

## ✅ Criterios de "Etapa 7 completa"

- [ ] Corriste el `contextLoads()` que ya venía y entendés qué prueba.
- [ ] Creaste `CatalogoDePaisesTest` con `@SpringBootTest` y `@Autowired`.
- [ ] Escribiste un test que verifica que el catálogo se inyecta.
- [ ] Escribiste un test que verifica la llamada HTTP real (con internet).
- [ ] Usaste `assertThat` de AssertJ (reusando lo del Proyecto 0).
- [ ] Hiciste el Experimento 1 (test que falla a propósito) y viste el mensaje de AssertJ.
- [ ] Hiciste el Experimento 2 (romper el contexto) y entendés qué prueba `contextLoads`.
- [ ] Entendés la diferencia entre tests con Spring y sin Spring.
- [ ] Dejaste todo en verde (sin asserts rotos ni `@Bean` comentados).

---

## ✅ Checkpoint

1. ¿Tuviste que agregar JUnit y AssertJ? ¿De dónde salieron?
2. ¿Qué prueba el test `contextLoads()` aunque esté vacío?
3. ¿Qué hace `@SpringBootTest`?
4. ¿Por qué `@Autowired` sobre un atributo está bien en tests pero no en producción?
5. ¿Cuál es el costo de `@SpringBootTest`?
6. ¿Qué diferencia hay entre un test con Spring y uno sin Spring?
7. ¿Por qué el test de la llamada HTTP "depende de internet"? ¿Qué riesgo tiene eso?
8. ¿Cuál es la regla práctica sobre cuántos tests con Spring vs sin Spring tener?

---

## 🎯 Una reflexión — y el cierre del Proyecto 1

Llegaste al final. Mirá todo lo que construiste, pieza por pieza, viendo cada una funcionar:

| Etapa | Qué dominás |
|---|---|
| 0 | Crear un proyecto Spring desde cero (y entender qué es) |
| 1 | El arranque, qué es Spring, qué hace `SpringApplication.run` |
| 2 | Beans y `@Component` — Spring crea objetos sin `new` |
| 3 | Inyección de dependencias por constructor |
| 4 | `@Configuration` + `@Bean` — beans de clases ajenas |
| 5 | Configuración externa con `@ConfigurationProperties` |
| 6 | Llamadas HTTP reales con `RestTemplate` + Jackson |
| 7 | Tests con `@SpringBootTest` y AssertJ |

**Lo importante no es la lista — es CÓMO la aprendiste.** No te tiraron el código terminado para que adivines cómo funciona. Construiste cada pieza, la corriste, la viste pasar, entendiste por qué existe, y recién ahí sumaste la siguiente. Beans que aparecen sin `new`, el mismo objeto inyectándose en varios lados, la config saltando del archivo a tu código, datos reales llegando de internet. **Todo lo viste con tus ojos.**

Ahora, si abrís cualquier código Spring del mundo real —de tu cursada, de un repo, de donde sea— ya no es un jeroglífico. Vas a reconocer las piezas porque las armaste con tus manos. Cuando veas un `@Component`, un `@Bean`, una inyección por constructor, un `@ConfigurationProperties`, un `RestTemplate` — vas a saber exactamente qué hace cada uno y por qué está ahí.

**Eso es pararse sobre bases sólidas.** Y lo lograste vos, construyendo.

---

## ▶️ Hacia dónde seguir (cuando vuelvas online)

Algunas direcciones posibles, según lo que necesites:

- **Exponer tus propios endpoints HTTP** (`@RestController`) — que tu app **reciba** pedidos, no solo que los haga. Es el siguiente paso natural si querés hacer una API web.
- **Persistencia** (guardar datos en una base de datos con Spring Data JPA).
- **Manejo de errores** en las llamadas HTTP (qué hacer cuando la API falla).
- **Construcción correcta de URLs** (`UriComponentsBuilder`, la madriguera que marqué en la Etapa 6).
- **Tests más finos** (mocks, tests unitarios puros de beans sin levantar Spring).

Cuando vuelvas, traé cualquier duda que te haya surgido offline, o decime hacia dónde querés seguir. **Tenés la base. El resto es construir sobre ella.**
