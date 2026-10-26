# 🌱 Proyecto 1 — Etapa 7: Tests en Spring (`@SpringBootTest`) — adaptada a la API v5

> **Objetivo:** testear tu app Spring con el contexto del framework levantado. Reusar JUnit + AssertJ del Proyecto 0. Entender la diferencia entre testear **con** Spring y **sin** Spring.
>
> **Pre-requisito:** Etapas 1-6 completas (versión v5). Idealmente recordar la Etapa 7 del Proyecto 0.
>
> **Tiempo estimado:** 40-50 minutos. Necesitás internet y tu token para los tests que llaman a la API.

---

## ⚠️ Nota sobre esta versión

Esta etapa está adaptada a la **API v5** (la que usa token y devuelve la estructura anidada `data → objects`). Los **conceptos** de testing son idénticos a cualquier versión; lo que cambia son los **asserts concretos** (los caminos para llegar a los datos) y que los tests de la llamada real necesitan tu token configurado.

---

## 🧭 Mapa de esta etapa

1. Lo que ya tenés para testear (viene de fábrica).
2. El test que el Initializr ya generó.
3. Tu primer test con Spring (`@SpringBootTest` + `@Autowired`).
4. Testear la llamada HTTP real (adaptada a v5).
5. Qué significa `@SpringBootTest` y qué cuesta.
6. Tests con Spring vs tests sin Spring.
7. Experimentos.
8. Checkpoint.

---

## 🎁 Parte 1: Lo que ya tenés para testear

No tenés que agregar nada. El Initializr incluyó `spring-boot-starter-test` en el `pom.xml` (Etapa 0):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Ese starter trae **JUnit 5, AssertJ, Mockito y utilidades de test de Spring** — todo junto. **Reconocés JUnit y AssertJ del Proyecto 0.** Acá vienen incluidos automáticamente.

> En el Proyecto 0 los agregabas a mano con sus versiones. Acá el starter los trae todos, con versiones compatibles ya elegidas por el parent. La misma comodidad de los starters de la Etapa 0.

---

## 📄 Parte 2: El test que ya existe

El Initializr generó `PaisesApplicationTests.java`:

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

**Corré este test.** El método `contextLoads()` está vacío, pero **igual prueba algo**: que el contexto de Spring arranca sin errores.

`@SpringBootTest` hace que, antes del test, **Spring arranque toda la app** (crea todos los beans, lee la config). Si algo está mal configurado, el contexto no arranca y el test falla.

> Por eso un test vacío con `@SpringBootTest` no es inútil: verifica que toda tu app **se puede ensamblar**.

> **Ojo con la config:** como ahora tu app lee `api-key` del YAML, asegurate de tener un valor ahí (aunque sea la demo key) para que el contexto arranque sin problemas.

---

## ✍️ Parte 3: Tu primer test con Spring

Creá `CatalogoDePaisesTest` en `src/test/java/.../`:

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

### Qué tiene

- **`@SpringBootTest`** → arranca el contexto de Spring antes de los tests.
- **`@Autowired private CatalogoDePaises catalogo`** → le pide a Spring que **inyecte** el bean. En tests, `@Autowired` sobre atributo es la forma normal (los tests no tienen constructor donde inyectar).
- **`assertThat(catalogo).isNotNull()`** → verifica que se inyectó. **Reconocés `assertThat` de AssertJ.**

**Corré.** Pasa en verde. Comprobaste que Spring puede crear e inyectar tu `CatalogoDePaises`.

> **Sobre `@Autowired` en tests:** en la Etapa 3 dije que `@Autowired` sobre atributos era de código viejo. En **producción** sí. En **tests** es lo normal y aceptado, porque no hay constructor propio donde inyectar. Es la excepción a la regla.

---

## 🌐 Parte 4: Testear la llamada HTTP real (adaptada a v5)

Ahora testeamos que `buscarPorNombre` realmente trae datos. **Acá los asserts cambian respecto al material viejo**, porque la estructura de datos es distinta (anidada).

Agregá a `CatalogoDePaisesTest`:

```java
@Test
void buscarArgentinaTraeLosDatosCorrectos() {
    Pais argentina = catalogo.buscarPorNombre("argentina");

    assertThat(argentina).isNotNull();

    // Navegamos la estructura v5: el nombre está en names.common
    assertThat(argentina.getNames().getCommon()).isEqualTo("Argentina");
    assertThat(argentina.getNames().getOfficial()).isEqualTo("Argentine Republic");

    // La región
    assertThat(argentina.getRegion()).isEqualTo("Americas");
    assertThat(argentina.getSubregion()).isEqualTo("South America");

    // La capital ahora es una lista de objetos Capital; sacamos el name del primero
    assertThat(argentina.getCapitals().get(0).getName()).isEqualTo("Buenos Aires");

    // La población es un número grande; verificamos que sea positivo
    // (no ponemos el valor exacto porque la población cambia con el tiempo)
    assertThat(argentina.getPopulation()).isPositive();
}
```

(Si quedaste con `buscarArgentina()` en vez de `buscarPorNombre(...)`, ajustá el nombre del método.)

### Qué cambió respecto al material viejo

```java
// ANTES (v3.1, estructura plana):
//   assertThat(argentina.getNombre().getComun()).isEqualTo("Argentina");
//   assertThat(argentina.getCapitales()).contains("Buenos Aires");   // era List<String>

// AHORA (v5, estructura anidada):
   assertThat(argentina.getNames().getCommon()).isEqualTo("Argentina");
   assertThat(argentina.getCapitals().get(0).getName()).isEqualTo("Buenos Aires");  // List<Capital>
```

La diferencia: **antes la capital era un string directo en una lista; ahora es el `name` de un objeto `Capital`.** El assert refleja esa anidación extra. Es el mismo AssertJ, navegando una estructura más profunda.

> **Sobre la población:** usé `.isPositive()` en vez de un valor exacto. Razón: **la población de un país cambia con el tiempo** (la API se actualiza). Si pusiera `.isEqualTo(46735004)`, el test fallaría el día que la API actualice el dato, aunque tu código esté perfecto. **Regla de testing: no ates tus tests a datos que cambian fuera de tu control.** Verificá lo que es estable (que haya un número positivo), no lo volátil (el valor exacto).

**Corré el test (con internet y tu token).** Pasa en verde si la API responde.

> **Este test hace una llamada HTTP real** a `restcountries.com`, autenticada con tu token. Depende de internet, de que la API funcione, y de que tu token sea válido. Si algo de eso falla, el test falla **aunque tu código esté perfecto**. Es un "test de integración" — prueba tu código + la API juntos. Más sobre esto en la Parte 6.

---

## 🚀 Parte 5: Qué significa `@SpringBootTest` y qué cuesta

`@SpringBootTest` hace que **Spring arranque el contexto completo antes de los tests de esa clase**.

```
Antes de los tests de la clase:
        ↓
@SpringBootTest arranca Spring
        ↓
Crea TODOS los beans (CatalogoDePaises, RestTemplate, PaisesProperties...)
        ↓
Lee application.yml (incluido tu token)
        ↓
Inyecta los @Autowired del test
        ↓
Recién ahí corren los tests
```

### El beneficio

Tus tests corren contra los beans **reales**, configurados como en producción. Si hay un error de configuración, el test lo detecta.

### El costo

**Arrancar el contexto es lento** (uno o dos segundos, a veces más). Con 100 clases de test `@SpringBootTest`, son 100 arranques. Por eso en proyectos grandes no todos los tests lo usan — solo los que de verdad necesitan el contexto.

> Spring reutiliza el contexto entre clases de test que comparten configuración, pero igual `@SpringBootTest` es la opción "pesada".

---

## ⚖️ Parte 6: Tests con Spring vs tests sin Spring

Distinción importante para tu futuro.

### Test CON Spring (`@SpringBootTest`)

Lo que hiciste. Spring arranca, inyecta beans reales. Útil para verificar ensamblaje (`contextLoads`) y flujos reales end-to-end (la llamada HTTP). **Lento, depende de config real (y de internet + token, en tu caso).**

### Test SIN Spring (unitario puro)

Como en el Proyecto 0: creás los objetos a mano, sin framework.

```java
class AlgunaLogicaTest {
    @Test
    void ejemplo() {
        var algo = new MiClase(/* dependencias a mano o falsas */);
        // assert
    }
}
```

**No usa `@SpringBootTest`. No arranca Spring. Instantáneo.** Probás una clase aislada.

> ¿Te acordás que en la Etapa 3 dije que la inyección por constructor hace las clases "testeables sin Spring"? **Esto es a lo que me refería.** Como `CatalogoDePaises` recibe `RestTemplate` y `properties` por constructor, podés crear uno a mano en un test pasándole un `RestTemplate` falso (un mock), y probar su lógica sin levantar Spring ni llamar a la API real.

### La regla práctica (la "pirámide de tests")

| Tipo | Velocidad | Cuándo | Cuántos |
|---|---|---|---|
| **Sin Spring** (unitario) | Rapidísimo | Lógica de una clase aislada | Muchos |
| **Con Spring** (`@SpringBootTest`) | Lento | Ensamblaje, flujos completos | Pocos |

**La mayoría de tus tests deberían ser sin Spring.** Solo algunos con Spring.

> En este proyecto chico usamos `@SpringBootTest` para todo, por simplicidad. En proyectos grandes balanceás. Lo importante: **saber que las dos formas existen** y para qué sirve cada una. Los mocks (objetos falsos para tests sin Spring) son una madriguera para explorar más adelante.

> **Una nota sobre tu test de la API:** el test que llama a `restcountries.com` de verdad es lento y frágil (depende de red, API y token). En proyectos serios, este tipo de llamada externa se suele **mockear** (simular la respuesta sin llamar a internet), dejando los tests reales contra la API para casos contados. Por ahora, llamarla de verdad está bien para que veas el flujo completo, pero registrá que en producción se maneja distinto.

---

## 🧪 Parte 7: Experimentos

### Experimento 1: Hacé fallar un test a propósito

En `buscarArgentinaTraeLosDatosCorrectos`, cambiá un assert:

```java
assertThat(argentina.getRegion()).isEqualTo("Europa");   // mal a propósito
```

Corré. Falla, y AssertJ muestra:

```
expected: "Europa"
 but was: "Americas"
```

Confirma que tu test verifica de verdad. Volvé a poner `"Americas"`.

### Experimento 2: El `contextLoads` rompible

En `AppConfig`, comentá el `@Bean` del `RestTemplate`. Corré `contextLoads()`. **Falla**, porque sin ese bean el `CatalogoDePaises` no se puede crear (lo necesita), y el contexto no arranca. Esto muestra el valor del `contextLoads`: detecta errores de ensamblaje sin asserts. Volvé a poner el `@Bean`.

### Experimento 3: Test parametrizado (varios países)

Como en el Proyecto 0:

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
```

Corre el mismo test 3 veces, una por país. **Reusás exactamente lo del Proyecto 0** — JUnit es JUnit, con o sin Spring. (Notá el assert adaptado: `getNames().getCommon()` por la estructura v5.)

### Experimento 4: Token inválido en el test

Esto es interesante. Si cambiás el `api-key` del YAML por uno inválido y corrés el test de la API, vas a ver que **falla con un error de autenticación** (no de assert). Eso te muestra que el test de integración depende del token, no solo de tu código. Volvé a poner el token bueno.

---

## ✅ Criterios de "Etapa 7 completa"

- [ ] Corriste el `contextLoads()` y entendés qué prueba.
- [ ] Tenés un `api-key` en el YAML para que el contexto arranque.
- [ ] Creaste `CatalogoDePaisesTest` con `@SpringBootTest` y `@Autowired`.
- [ ] Escribiste un test que verifica que el catálogo se inyecta.
- [ ] Escribiste un test de la llamada HTTP real, con asserts adaptados a la estructura v5.
- [ ] Usaste `.isPositive()` para la población (no un valor exacto) y entendés por qué.
- [ ] Usaste `assertThat` de AssertJ (reusando lo del Proyecto 0).
- [ ] Hiciste el Experimento 1 (test que falla) y viste el mensaje de AssertJ.
- [ ] Hiciste el Experimento 2 (romper el contexto) y entendés qué prueba `contextLoads`.
- [ ] Entendés la diferencia entre tests con Spring y sin Spring.
- [ ] Dejaste todo en verde.

---

## ✅ Checkpoint

1. ¿Tuviste que agregar JUnit y AssertJ? ¿De dónde salieron?
2. ¿Qué prueba `contextLoads()` aunque esté vacío?
3. ¿Qué hace `@SpringBootTest`?
4. ¿Por qué `@Autowired` sobre atributo está bien en tests pero no en producción?
5. ¿Por qué el assert de la población usa `.isPositive()` y no el valor exacto?
6. ¿Por qué el assert de la capital ahora es `getCapitals().get(0).getName()` y no `contains("Buenos Aires")`?
7. ¿Cuál es el costo de `@SpringBootTest`?
8. ¿Qué diferencia hay entre un test con Spring y uno sin Spring?
9. ¿Por qué el test de la llamada HTTP depende de internet **y del token**?

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
| 6 | Llamadas HTTP reales, **autenticadas con token**, contra una API que devuelve estructuras complejas |
| 7 | Tests con `@SpringBootTest` y AssertJ |

**Lo importante no es la lista — es CÓMO la aprendiste.** No te tiraron el código terminado para que adivines. Construiste cada pieza, la corriste, la viste pasar, entendiste por qué existe, y recién ahí sumaste la siguiente.

Y te tocó algo que la mayoría de los tutoriales evita: **una API del mundo real que cambió, que exige autenticación, y que devuelve un monstruo anidado.** No te quedaste con el `getForObject` de juguete — aprendiste a mandar tokens con `exchange`, a modelar estructuras anidadas con DTOs, y a quedarte solo con los datos que te importan. Eso es más valioso que cualquier ejemplo de laboratorio, porque es lo que de verdad te vas a encontrar.

Ahora, si abrís cualquier código Spring del mundo real, ya no es un jeroglífico. Reconocés las piezas porque las armaste con tus manos. **Eso es pararte sobre bases sólidas.** Y lo lograste construyendo.

---

## ▶️ Hacia dónde seguir (cuando vuelvas online)

Direcciones posibles:

- **Exponer tus propios endpoints HTTP** (`@RestController`) — que tu app **reciba** pedidos, no solo que los haga.
- **Manejo de errores** en las llamadas (qué hacer cuando la API falla, el token expira, o el país no existe).
- **Construcción correcta de URLs** (`UriComponentsBuilder`, la madriguera de la Etapa 6).
- **Mockear la API en tests** (tests rápidos sin llamar a internet).
- **Persistencia** (guardar datos en una base con Spring Data JPA).

Cuando vuelvas, traé cualquier duda que te haya surgido offline, o decime hacia dónde querés seguir. **Tenés la base. El resto es construir sobre ella.**
