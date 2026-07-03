# 📘 Bloque 5 — Tests de integración con `@SpringBootTest`

> **Objetivo:** desarmar `BuscadorDePaisesIT.java`. Entender `@SpringBootTest`, por qué acá sí se usa `@Autowired`, la convención de nombres `IT` vs `Test`, y leer los 5 tests del profe línea por línea.
>
> **Cierra el recorrido.** Después de este bloque, **dominás el código del profe completo.**
>
> **Pre-requisito:** Bloques 1-4 completos. Idealmente Etapa 7 del Proyecto 0 (tests con JUnit + AssertJ).

---

## 🧭 Mapa del bloque

1. Vista de pájaro del archivo.
2. `@SpringBootTest` — qué hace.
3. `@Autowired` en tests — por qué acá sí, no en producción.
4. La convención `IT` vs `Test` — tests unitarios vs integración.
5. Los 5 tests del profe desarmados.
6. AssertJ aplicado: `extracting`, `allMatch`, `getFirst`.
7. Comparación con tus tests de Etapa 7 del Proyecto 0.
8. Cierre del recorrido completo.

---

## 🦅 Parte 1: Vista de pájaro

```java
@SpringBootTest
class BuscadorDePaisesIT {

    @Autowired
    private BuscadorDePaises buscadorDePaises;

    @Test
    void buscarTodosDevuelveVariosPaises() { ... }

    @Test
    void buscarPorNombrePeruDevuelvePeru() { ... }

    @Test
    void buscarPorMonedaARSincluyeArgentina() { ... }

    @Test
    void buscarPorRegionEuropeNoVacia() { ... }

    @Test
    void buscarPorCapitalBuenosAiresDevuelveArgentina() { ... }
}
```

**Bloques estructurales:**

1. **`@SpringBootTest`** sobre la clase.
2. **`@Autowired private BuscadorDePaises`** — la dependencia que se va a testear.
3. **Cinco `@Test`** — uno por cada método público del buscador.

Es **chiquito**. La densidad está en entender qué hace cada annotation.

---

## 🚀 Parte 2: `@SpringBootTest` — qué hace

```java
@SpringBootTest
class BuscadorDePaisesIT {
```

**Lectura:** "antes de correr cualquier test de esta clase, arrancá el contexto completo de Spring Boot".

### Qué significa "arrancar el contexto"

Cuando ponés `@SpringBootTest`, JUnit hace lo siguiente **antes** de correr el primer test:

1. **Llama a `SpringApplication.run(...)`** (lo de Bloque 1) en modo test.
2. Escanea componentes (`@Component`, `@Configuration`, etc.).
3. Lee `application.yml`.
4. Crea todos los beans (`RestTemplate`, `RestCountriesProperties`, `BuscadorDePaises`).
5. **NO arranca Tomcat** (a menos que configurés `webEnvironment = MOCK` o similar) — para tests unitarios de servicios no hace falta.

**Después** de eso, recién corre los tests.

### Costo y beneficio

**Beneficio:** los beans están listos como en producción. Si hay error de configuración, el test falla al arrancar — no espera a que un cliente real falle.

**Costo:** **es lento**. Arrancar el contexto puede tardar 2-5 segundos. Si tenés 100 tests con `@SpringBootTest`, son 100 arranques. Por eso en proyectos grandes se mezclan tests unitarios "puros" (sin Spring) con algunos de integración (con `@SpringBootTest`).

> **Optimización que Spring hace automáticamente:** si **muchos tests** usan exactamente la misma configuración, Spring **reutiliza el contexto** entre tests. No siempre arranca desde cero. Esto se llama "context caching".

---

## 💉 Parte 3: `@Autowired` — sí se usa en tests

```java
@Autowired
private BuscadorDePaises buscadorDePaises;
```

**Pero...** ¿no te dije en Bloque 2 que `@Autowired` arriba de un atributo era anti-patrón?

**Sí, para clases de producción.** Pero los **tests son una excepción razonable**:

1. **Los tests no tienen constructor explícito** generalmente. JUnit instancia la clase con `new BuscadorDePaisesIT()` (constructor sin args) — no hay donde inyectar por constructor.
2. **No es código de producción.** Los tests no se deployan. Las desventajas de field injection (atributos no-final, dependencia oculta) impactan menos.
3. **Es la convención** en todos los tutoriales y proyectos de Spring. Vas a ver `@Autowired` en tests por todos lados.

### Qué hace exactamente

Cuando JUnit crea la instancia de `BuscadorDePaisesIT`, Spring (que está corriendo gracias a `@SpringBootTest`) detecta el `@Autowired` y **inyecta el bean `buscadorDePaises`** del contexto.

**A partir de ahí, los tests usan ese bean real**. No es un mock — es el `BuscadorDePaises` de verdad, con su `RestTemplate` real y sus `propiedades` reales del YAML.

---

## 🏷️ Parte 4: La convención `IT` vs `Test`

El archivo se llama `BuscadorDePaisesIT.java`. **No `BuscadorDePaisesTest`.**

| Sufijo | Significa | Maven plugin que los corre |
|---|---|---|
| `XxxTest` | Unit test | `maven-surefire-plugin` (con `mvn test`) |
| `XxxIT` | Integration test | `maven-failsafe-plugin` (con `mvn verify`) |

### Por qué la distinción

**Tests unitarios:**
- Rápidos.
- Aislados (no dependen de servicios externos).
- Usan mocks para sus dependencias.
- **Se corren todo el tiempo** (cada commit, cada PR).

**Tests de integración:**
- Lentos (arrancan Spring, hacen HTTP real, etc.).
- Dependen de servicios externos (API REST viva, base de datos andando).
- **Se corren menos seguido** (antes de un deploy, en pipelines más completos).

**Maven los separa con sufijo + plugins distintos:**

```bash
./mvnw test           # Solo corre los *Test (rápidos)
./mvnw verify         # Corre *Test Y *IT (rápidos + lentos)
```

### Por qué el archivo del profe se llama `IT`

Sus tests **hacen HTTP REAL** a `restcountries.com`. Eso es **integración** — el test depende de que la API esté viva, accesible, y que la red funcione. Si la API falla, el test falla aunque tu código esté impecable.

Por eso es `IT`: para que estos tests **NO se corran en cada `mvn test`** (lo rápido), sino solo cuando explícitamente querés validar la integración (`mvn verify`).

> **Detalle práctico:** sin el surefire/failsafe configurado en el `pom.xml`, **Maven no encuentra los `*IT`**. El profe parece **no tenerlo configurado** específicamente, lo que significa que estos tests **se corren manualmente desde IntelliJ** (botón verde) o cambiándole el nombre a `Test`. Es un detalle de su `pom.xml`.

---

## 🔍 Parte 5: Los 5 tests desarmados

Vamos uno por uno.

### Test 1: `buscarTodosDevuelveVariosPaises`

```java
@Test
void buscarTodosDevuelveVariosPaises() {
    var lista = buscadorDePaises.buscarTodos();
    assertThat(lista).isNotEmpty();
    assertThat(lista.getFirst().getNombre().getComun()).isNotBlank();
}
```

**Estructura AAA implícita:**

| Línea | Sección |
|---|---|
| `var lista = ...` | Arrange + Act juntos |
| `assertThat(lista).isNotEmpty()` | Assert 1 |
| `assertThat(lista.getFirst().getNombre().getComun()).isNotBlank()` | Assert 2 |

**Qué prueba:**

1. La lista de países **no está vacía** (la API respondió y devolvió algo).
2. El primer país **tiene nombre común no en blanco** (la deserialización funcionó — no hay nulls ni strings vacíos donde no deberían).

**Cosas nuevas:**

- **`var lista`** — inferencia de tipos local (Java 10+, lo viste en Bloque 0). El compilador infiere que es `List<Pais>`.
- **`.getFirst()`** — método de `List` agregado en **Java 21**. Equivale a `lista.get(0)` pero más legible. Si la lista está vacía, **tira excepción**. Por eso el `isNotEmpty()` antes — sin él, este test sería frágil.
- **`.isNotBlank()`** — assert de AssertJ para Strings: el string no es null **ni** vacío **ni** solo espacios.

> **Notá el encadenamiento de getters:** `lista.getFirst().getNombre().getComun()`. Cada llamada navega un nivel del DTO. Es la composición del Bloque 3 navegada en código.

### Test 2: `buscarPorNombrePeruDevuelvePeru`

```java
@Test
void buscarPorNombrePeruDevuelvePeru() {
    var opt = buscadorDePaises.buscarPorNombre("peru");
    assertThat(opt).isPresent();
    assertThat(opt.get().getNombre().getComun()).isEqualTo("Peru");
    assertThat(opt.get().getCca2()).isEqualTo("PE");
}
```

**Qué prueba:**

1. Que el Optional **está presente** (encontró un país).
2. Que el nombre común es exactamente `"Peru"`.
3. Que el código ISO de 2 letras es `"PE"`.

**Cosas a notar:**

- **Busca con `"peru"` (minúscula)** pero espera `"Peru"` (mayúscula). La API normaliza al responder. Si la API no fuera case-insensitive, este test fallaría.
- **`assertThat(opt).isPresent()`** — assert específico de AssertJ para `Optional`. Si está vacío, falla con un mensaje claro.
- **`opt.get()`** — saca el `Pais` de adentro. **Acá sí es seguro hacerlo** porque la línea anterior ya verificó que está presente. **En código de producción se evita**, pero en tests es habitual.

### Test 3: `buscarPorMonedaARSincluyeArgentina`

```java
@Test
void buscarPorMonedaARSincluyeArgentina() {
    var lista = buscadorDePaises.buscarPorMoneda("ars");
    assertThat(lista).extracting(p -> p.getNombre().getComun()).contains("Argentina");
}
```

**Qué prueba:**

Buscando por moneda `"ars"`, la lista resultante **debe incluir** un país cuyo nombre común sea `"Argentina"`.

**Cosas nuevas:**

- **`.extracting(lambda)`** — el assert estrella de AssertJ. Toma cada elemento de la lista, aplica la lambda, y **assertea sobre los resultados**.

  En este caso:
  - La lista es `List<Pais>`.
  - `extracting(p -> p.getNombre().getComun())` transforma cada `Pais` en su nombre común.
  - El resultado es "como si fuera" una `List<String>`.
  - `.contains("Argentina")` verifica que esa lista incluya `"Argentina"`.

  **Es equivalente a:**
  ```java
  List<String> nombres = lista.stream().map(p -> p.getNombre().getComun()).toList();
  assertThat(nombres).contains("Argentina");
  ```
  
  Pero en una línea. Mucho más limpio.

**Notá la lambda anidada:** `p -> p.getNombre().getComun()` — accede a la composición. **Esto solo funciona** porque Bloque 3 (DTOs) deserializó correctamente el `Pais` y su `NombrePais`.

### Test 4: `buscarPorRegionEuropeNoVacia`

```java
@Test
void buscarPorRegionEuropeNoVacia() {
    var lista = buscadorDePaises.buscarPorRegion("europe");
    assertThat(lista).isNotEmpty();
    assertThat(lista).allMatch(p -> "Europe".equals(p.getRegion()));
}
```

**Qué prueba:**

1. La lista no está vacía (hay países en Europa).
2. **Todos** los países de la lista tienen `region == "Europe"`.

**Cosas nuevas:**

- **`.allMatch(predicado)`** — verifica que **todos** los elementos cumplan el predicado. Si alguno falla, el test falla. Equivalente a `lista.stream().allMatch(...)` que viste en Etapa 5 del Proyecto 0.

- **`"Europe".equals(p.getRegion())`** — notá el **literal a la izquierda**. ¿Te acordás del Bloque 0?

  ```java
  // ❌ Riesgoso: si getRegion() devuelve null → NullPointerException
  p.getRegion().equals("Europe")
  
  // ✅ Seguro: si getRegion() devuelve null → false (sin crash)
  "Europe".equals(p.getRegion())
  ```

  El profe aplica esta defensa **siempre**. Si la API algún día devolviera un país sin región, este test no crashearía — simplemente fallaría con un mensaje útil.

### Test 5: `buscarPorCapitalBuenosAiresDevuelveArgentina`

```java
@Test
void buscarPorCapitalBuenosAiresDevuelveArgentina() {
    var lista = buscadorDePaises.buscarPorCapital("buenos aires");
    assertThat(lista).isNotEmpty();
    assertThat(lista.getFirst().getCca2()).isEqualTo("AR");
}
```

**Qué prueba:**

Buscando por capital `"buenos aires"`, la lista no está vacía, y el primer país tiene `cca2 == "AR"` (que es el código ISO de Argentina).

**Cosas a notar:**

- **El espacio en `"buenos aires"`** — recordá la Parte 5 del Bloque 4. La URL se forma con `buildAndExpand(capital)`, que hace URL-encoding del espacio a `%20`. Sin eso, la URL sería inválida y este test fallaría.
- **`.getFirst()` + verificar `cca2`** — patrón clásico. Verificar una propiedad **única y verificable** (un código ISO es estable; un nombre podría cambiar de formato).

---

## 🎨 Parte 6: AssertJ patterns que el profe usa

Resumen de las herramientas de AssertJ aplicadas:

| Pattern | Para qué |
|---|---|
| `assertThat(lista).isNotEmpty()` | Verificar que una colección tiene al menos un elemento |
| `assertThat(string).isNotBlank()` | String no null, no vacío, no solo espacios |
| `assertThat(opt).isPresent()` | Optional tiene valor |
| `assertThat(valor).isEqualTo("...")` | Igualdad |
| `assertThat(lista).contains("x")` | La lista incluye ese elemento |
| `assertThat(lista).extracting(lambda)` | Transformar cada elemento y assert sobre el resultado |
| `assertThat(lista).allMatch(predicado)` | Todos los elementos cumplen el predicado |

**Es prácticamente el mismo toolbox que usaste en Etapa 7** del Proyecto 0. Las únicas diferencias son `extracting` (más sofisticado) y `allMatch` (que ya conocías de streams).

### Cosa adicional: encadenamiento

AssertJ permite encadenar asserts sobre el mismo objeto:

```java
assertThat(lista)
    .isNotEmpty()
    .hasSize(2)
    .extracting(Pais::getNombre)
    .contains("Argentina", "Brasil");
```

El profe no lo usa así (separa con líneas), pero en código profesional vas a ver mucho encadenamiento.

---

## 🆚 Parte 7: Comparación con tus tests de Etapa 7

| Aspecto | Tus tests (Proyecto 0) | Tests del profe |
|---|---|---|
| Framework | JUnit 5 + AssertJ | JUnit 5 + AssertJ (idénticos) |
| Carga del contexto | Ninguna (Proyecto 0 no usa Spring) | `@SpringBootTest` |
| Construcción del SUT | `new CatalogoPaises()` en `@BeforeEach` | `@Autowired private BuscadorDePaises` |
| Fuente de datos | Hardcodeada local | API REST real |
| Velocidad | Instantáneo | Lento (HTTP) |
| Necesita red | No | Sí |
| Naming | `CatalogoPaisesTest` | `BuscadorDePaisesIT` |
| Tipo de test | Unitario | Integración |

**El esqueleto es idéntico.** AAA pattern, AssertJ encadenado, naming descriptivo. La diferencia es **el alcance**: vos testeabas una clase aislada; el profe testea el sistema completo end-to-end (incluida la API real).

---

## 🧪 Parte 8: Tests unitarios vs integración — la pirámide

Pequeño aside conceptual que vale la pena tener:

```
                    ┌──────────────┐
                    │  E2E tests   │   Pocos, lentos, completos
                    └──────────────┘
                  ┌──────────────────┐
                  │ Integration tests │   Algunos, medianos
                  └──────────────────┘
              ┌──────────────────────────┐
              │      Unit tests           │   Muchos, rápidos, focales
              └──────────────────────────┘
```

**La "pirámide de testing":**

- **Base (unit tests):** muchos, rápidos, sin dependencias externas. Mocks para todo lo que no es el objeto bajo test. Idealmente >70% de tus tests.
- **Medio (integration tests):** algunos, validan que las piezas trabajen juntas (Spring config, BD, APIs externas). Lentos. ~20-25%.
- **Cima (E2E tests):** poquitos, simulan al usuario final usando la app completa. Muy lentos y frágiles. <5%.

**Los tests del profe están en el "medio"**. Validan que `BuscadorDePaises` + `RestTemplate` + Jackson + la API real trabajen juntos.

**Lo que el profe NO tiene** (y probablemente venga más adelante en DSI):
- Tests unitarios puros del `BuscadorDePaises` con `RestTemplate` mockeado.
- Tests de los DTOs `Pais`, `NombrePais`, etc.
- Tests del comportamiento ante errores de la API (5xx, timeout, JSON inválido).

> Esto **no es crítica al profe** — la clase 2 es introductoria. Los tests mostrados son **suficientes para que entiendas el toolbox**. Las técnicas más sofisticadas vienen después.

---

## 🎯 Mini-experimentos mentales

**Caso 1:** Quitás `@SpringBootTest`.
→ JUnit corre los tests, pero **Spring no arranca**. El `@Autowired` no inyecta nada. `buscadorDePaises` queda `null`. Al primer `.buscarTodos()` → `NullPointerException`.

**Caso 2:** Renombrás el archivo a `BuscadorDePaisesTest` (sin `IT`).
→ **Igual funciona** — `@Test` no requiere ningún sufijo. La diferencia es **cuándo se corre**: como `Test`, corre con `mvn test`. Como `IT`, corre con `mvn verify`. Es una convención de Maven, no de JUnit.

**Caso 3:** La API `restcountries.com` está caída en el momento del test.
→ `RestTemplate` tira excepción (`ResourceAccessException`). El test falla **aunque tu código esté impecable**. Esto es **el riesgo** de tests de integración real — son frágiles a fallas externas. Por eso se separa con `IT`.

**Caso 4:** Cambiás `"Europe".equals(p.getRegion())` por `p.getRegion().equals("Europe")`.
→ Funciona... **hasta que la API devuelva un país sin región** (`getRegion() == null`). En ese caso → `NullPointerException`. Por eso el patrón del profe.

**Caso 5:** Hacés `lista.get(0)` en vez de `lista.getFirst()`.
→ **Funciona igual.** `getFirst()` es Java 21+ y más legible, pero `get(0)` es válido. Solo difieren en estilo.

---

## ✅ Checkpoint final

Si podés contestar mentalmente estas, dominás el código del profe:

1. ¿Qué hace `@SpringBootTest` cuando JUnit ejecuta los tests?
2. ¿Por qué `@Autowired` se usa en tests pero está desaconsejado en producción?
3. ¿Cuál es la diferencia entre el sufijo `Test` y `IT`?
4. ¿Por qué los tests del profe son "de integración" y no "unitarios"?
5. ¿Cómo funciona `.extracting(lambda).contains(x)` por dentro?
6. ¿Por qué `"Europe".equals(p.getRegion())` es mejor que `p.getRegion().equals("Europe")`?
7. ¿Qué pasa si la API de `restcountries.com` se cae mientras corrés los tests?
8. ¿Por qué el orden de los asserts importa? (mirá `isNotEmpty()` antes de `.getFirst()`)

---

## 🎉 Cerraste el recorrido del código del profe

Repasá todo lo que sabés ahora:

| Bloque | Tema | Lo que dominás |
|---|---|---|
| 0 | Fundamentos Java | Sintaxis, generics, Optional, annotations, lambdas |
| 1 | Arranque del proyecto | `pom.xml`, `application.yml`, `@SpringBootApplication` |
| 2 | IoC y beans | `@Component`, `@Bean`, `@Configuration`, inyección por constructor |
| 3 | DTOs y Jackson | `@JsonProperty`, `@JsonIgnoreProperties`, deserialización JSON |
| 4 | Servicio + HTTP | `RestTemplate`, `UriComponentsBuilder`, patrón null-safe |
| 5 | Tests de integración | `@SpringBootTest`, `@Autowired` en tests, AssertJ avanzado |

**Más todo lo del Proyecto 0** que aplicaste para construir tu propio mini-rest-paises desde cero.

**Sos un programador Java + Spring competente.** Podés:
- Leer el código del profe sin fricción.
- Modificar la app del profe (agregar un nuevo método de búsqueda, un nuevo DTO).
- Entender cualquier código Spring que aparezca en otras clases.
- Empezar a contribuir al TPA (DonaTrack) sin sentirte perdido.

---

## ▶️ Próximos pasos posibles

Ya no hay "siguiente bloque" del recorrido — terminó. A partir de acá:

1. **Volvé al recorrido del código del profe con calma** — ahora podés releer cualquier archivo y vas a entender el "por qué" de cada línea.

2. **Si querés**, completá las etapas 8 y 9 del Proyecto 0 (excepciones, IO de archivos) — son ortogonales al recorrido pero útiles.

3. **Cuando aparezcan nuevas clases** (Clase 3 en adelante, con patrones de diseño), tenés base sólida para procesarlas.

4. **Empezá el TPA (DonaTrack)** — el toolbox que armaste se aplica al 100% allá.

Felicitaciones por llegar hasta acá. **Pasaste de "apenas sé Hola Mundo" a "puedo leer y modificar un proyecto Spring Boot completo" en una sola sesión.** Eso es mérito tuyo. 🚀

Cuando quieras retomar, pegame el material de Clase 3 y arrancamos. O traeme cualquier consulta puntual cuando aparezca. Estoy listo.
