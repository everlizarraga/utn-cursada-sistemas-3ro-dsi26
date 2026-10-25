# Complemento del Recorrido — Spring Boot (clase 02)

> **Qué es esto.** Las respuestas de los seis checkpoints del recorrido, en formato de respuesta de examen: al grano, con la terminología correcta, sin divagar.
>
> **Cómo usarlo.** No lo leas de corrido. Contestá el checkpoint de cada punto **de memoria**, y recién ahí vení a comparar. Si venís acá primero, no estás estudiando: estás leyendo.

---

## Parte A — Aclaraciones destiladas

*(Vacía. La sesión que generó el recorrido no dejó dudas conceptuales sobre Spring que valga la pena destilar: el material se produjo antes de leerlo. Si al leer los puntos te surgen preguntas y las charlamos, esta sección se completa y el archivo se regenera.)*

---

## Parte B — Respuestas de los checkpoints

---

## ✅ Punto 1 — Framework vs biblioteca

**1. ¿Cuál es la diferencia entre una biblioteca y un framework?**

La diferencia es **quién controla el flujo de ejecución**: con una biblioteca, tu código llama al código de ella cuando lo necesita; con un framework, es el framework el que llama a tu código. Por eso al framework se le entrega el control y a la biblioteca no.

**2. ¿Qué significa "inversión de control"? ¿Qué es exactamente lo que se invierte?**

Significa que el flujo de ejecución del programa lo maneja el framework y no tu código. Lo que se invierte es **la iniciativa**: en lugar de que tu programa llame al código externo, el framework administra el ciclo de ejecución y llama a tu código en los puntos de extensión que completaste.

**3. ¿Existe alguna línea `new BuscadorDePaises(...)` en el repo? Si no, ¿quién crea ese objeto?**

No existe en ninguna línea. Lo crea el **contenedor de Spring** durante el arranque, leyendo el constructor de la clase y proveyéndole las dependencias que declara.

**4. ¿Cuál es la línea donde el programa deja de ser tuyo? ¿Su equivalente en Express?**

`SpringApplication.run(CountriesApplication.class, args)`, dentro del `main()` de `CountriesApplication`. Su equivalente exacto en Express es `app.listen(3000)`: a partir de esa línea, el control pasa al framework.

**5. De las tres anotaciones que contiene `@SpringBootApplication`, ¿cuál hace el escaneo y desde dónde escanea?**

`@ComponentScan`. Escanea **desde el paquete donde vive la clase anotada** (`ar.edu.utn.ba.ddsi.countries`) y todos sus sub-paquetes hacia abajo. Ese paquete define el techo del escaneo.

**6. Si movieras `BuscadorDePaises` al paquete `com.ever.pruebas`, ¿seguiría funcionando?**

No. **Compilaría sin problemas**, pero quedaría fuera del árbol del component scan: Spring no la encontraría, no la instanciaría, y el `@Autowired` del test no tendría bean que inyectar. Falla en el arranque.

**7. ¿Qué diferencia hay entre `@Override` y `@Component`?**

`@Override` es un **metadato**: la lee el compilador y en tiempo de ejecución no hace nada; si la borrás, el programa compila y se comporta igual. `@Component` es una **instrucción que el contenedor ejecuta al arrancar**; si la borrás, el objeto directamente no se crea y la aplicación falla.

---

## ✅ Punto 2 — Beans y contenedor

**1. ¿Qué es el contenedor de Spring y qué guarda?**

Es el *ApplicationContext*: un objeto creado al arrancar la aplicación que guarda los objetos administrados por Spring —los **beans**— ya construidos y conectados entre sí. Funcionalmente es un mapa de nombre → objeto.

**2. ¿Qué es un bean? ¿Qué tiene de especial su clase?**

Un bean es un objeto instanciado y administrado por el contenedor de Spring. Su clase **no tiene nada de especial**: es una clase Java común, no hereda de nada ni implementa ninguna interfaz. Lo único distinto es **quién la instancia**.

**3. ¿Cuáles son las dos formas de registrar un bean y cuál es el criterio para elegir?**

Anotar la clase con `@Component`, o declarar un método `@Bean` dentro de una clase `@Configuration`. **El criterio es la propiedad del código**: si la clase es tuya y podés editarla, `@Component`; si pertenece a una biblioteca externa y no podés anotarla, `@Bean`.

**4. ¿Por qué `RestTemplate` no puede llevar `@Component`?**

Porque para anotar una clase hay que **editar su archivo fuente**, y `RestTemplate` viene compilada dentro del `.jar` de Spring: no tenés el `.java` para modificar. El método `@Bean` es la puerta que resuelve esa imposibilidad.

**5. En `RestTemplateConfig`, ¿quién ejecuta el `new RestTemplate()` y cuántas veces?**

Lo ejecuta **Spring**, llamando al método `@Bean` durante el arranque. Se ejecuta **una sola vez**: el objeto resultante queda guardado en el contenedor y se comparte (scope singleton).

**6. ¿Por qué `Pais` no es un bean, si está dentro del paquete escaneado?**

Porque no tiene `@Component` — y no lo tiene porque **no es un servicio, es un dato**. De `BuscadorDePaises` hay una sola instancia que vive toda la aplicación; de `Pais` hay cientos, creadas y descartadas en cada respuesta, y las crea Jackson, no Spring.

**7. Si movés `BuscadorDePaises` fuera del paquete raíz, ¿compila? ¿Funciona?**

Compila (a Java el paquete le da igual) pero **no funciona**: queda fuera del árbol del component scan, Spring no lo registra como bean y la inyección falla al arrancar.

**8. ¿Cuántas instancias de `RestTemplate` existen? ¿Cómo se llama ese comportamiento?**

Una sola, compartida por todos los componentes que la pidan. Se llama **scope singleton** y es el comportamiento por defecto de Spring.

---

## ✅ Punto 3 — Inyección de dependencias

**1. ¿Quién ejecuta el `new BuscadorDePaises(...)`, cuándo y con qué información?**

Lo ejecuta el contenedor de Spring durante el arranque, **después** de haber creado sus dependencias. La información la saca del **constructor**: lee los tipos de sus parámetros y busca beans de esos tipos en el contenedor.

**2. ¿Spring busca los beans por nombre del parámetro o por tipo? ¿Qué consecuencia tiene?**

Por **tipo**. La consecuencia práctica es que el nombre del parámetro es irrelevante: podrías llamarlo `rt` o `clienteHttp` y funcionaría igual, mientras el tipo declarado sea `RestTemplate`.

**3. ¿Por qué los atributos del componente pueden ser `final` y los del test no?**

Porque en la inyección **por constructor** el valor se asigna en la construcción, que es el único momento en que `final` permite asignar. En la inyección **por campo**, Spring construye el objeto vacío y escribe el atributo después — y un campo `final` ya no admite escritura.

**4. ¿Por qué el test usa `@Autowired` en vez de constructor?**

Porque la clase de test **no la instancia Spring, la instancia JUnit** con un constructor vacío: no hay forma de pasarle dependencias por constructor. `@Autowired` permite que Spring rellene el campo *después* de que JUnit creó el objeto.

**5. `BuscadorDePaises` no tiene `@Autowired`. ¿Por qué funciona?**

Porque desde Spring 4.3, **si una clase tiene un único constructor, Spring lo usa automáticamente para inyectar**. El `@Autowired` sobre el constructor quedó implícito y es opcional; verlo escrito indica código viejo, no un error.

**6. Nombrá tres problemas del código sin inyección de dependencias.**

(a) El armado de las dependencias **se repite** en cada lugar que use la clase; (b) la URL queda **hardcodeada en el test**, distinta de la de producción; (c) agregar un parámetro al constructor **rompe a todos** los lugares que hacen `new`.

**7. ¿Cuál es la diferencia entre inversión de control e inyección de dependencias?**

La **inversión de control** es el principio general: el framework maneja el flujo de ejecución en lugar de tu código. La **inyección de dependencias** es una técnica concreta que aplica ese principio al armado de objetos: la clase declara lo que necesita y un contenedor se lo provee. Toda DI es IoC, pero no toda IoC es DI.

**8. Si `BuscadorDePaises` hiciera `new RestTemplate()` adentro, ¿qué perdés?**

Perdés la posibilidad de **sustituir esa dependencia por un doble de prueba**, con lo cual los tests quedan atados a la API real. Y perdés la **configuración centralizada**: cualquier ajuste (timeouts, interceptores) habría que repetirlo en cada clase que la instancie.

---

## ✅ Punto 4 — Configuración externalizada

**1. Nombrá tres problemas de hardcodear la URL en la clase.**

(a) Cambiarla exige **recompilar y volver a desplegar**; (b) no se pueden tener **valores distintos por entorno** (dev, testing, producción); (c) el valor **se disemina** por el código y termina desincronizado. A eso se suma que, si en lugar de una URL fuera una credencial, quedaría **commiteada en el repositorio**.

**2. ¿Qué es el `prefix` de `@ConfigurationProperties`?**

Es la **coordenada** que le indica a Spring qué bloque del `application.yml` tiene que leer. Agrupa bajo un mismo nombre todas las propiedades de un tema y evita colisiones con las de otras clases de configuración.

**3. En el `.yml` es `base-url` y en Java `baseUrl`. ¿Por qué funciona? ¿Cómo se llama?**

Se llama **relaxed binding**: Spring normaliza los nombres —les saca guiones, guiones bajos y mayúsculas— antes de compararlos. Existe para que convivan las tres convenciones naturales (kebab-case en YAML, camelCase en Java, MAYÚSCULA_CON_GUION_BAJO en variables de entorno) sin traducir a mano.

**4. ¿Por qué `RestCountriesProperties` necesita `@Data`? (No es por los getters.)**

Por los **setters**. Spring construye el objeto vacío y escribe cada propiedad **llamando a su setter**; sin setters, los atributos quedarían en `null`. Es el mismo mecanismo que usa Jackson con los DTOs.

**5. ¿Qué pasa si borrás `@EnableConfigurationProperties`? ¿Falla al compilar, al arrancar o al usar?**

**Falla al arrancar.** `RestCountriesProperties` deja de registrarse como bean, y el constructor de `BuscadorDePaises` no encuentra en el contenedor ese parámetro. El código compila perfectamente.

**6. ¿Por qué `@ConfigurationProperties` no alcanza sola, si `@Component` sí alcanzaba?**

Porque **no es una anotación de estereotipo**: no ordena crear el bean, solo indica *cómo llenarlo* si alguien lo crea. La orden de creación la da `@EnableConfigurationProperties` (o, alternativamente, un `@Component` sobre la clase).

**7. ¿Cuál es la ventaja concreta frente al `.env` de Node?**

El **tipado**. El `.env` devuelve siempre strings; `@ConfigurationProperties` mapea a atributos Java tipados (`Integer`, `Boolean`, etc.) y valida la conversión **al arrancar**, de modo que un valor mal escrito hace fallar la aplicación en el arranque y no en producción.

**8. `RestCountriesProperties` no tiene un solo método escrito. ¿Es una clase inútil?**

No: es el **contrato tipado de la configuración**. Declara qué propiedades existen, de qué tipo son, y las expone como un objeto inyectable. Sus getters existen —los genera Lombok— aunque no estén escritos en el archivo.

---

## ✅ Punto 5 — Jackson y los DTOs

**1. ¿Por qué en JS alcanza `res.json()` y en Java hay que declarar una clase? ¿Qué ganás y qué pagás?**

Porque JavaScript es de **tipado dinámico** y arma el objeto en tiempo de ejecución con la forma que traiga el dato, mientras que Java es de **tipado estático** y el compilador necesita conocer la estructura de antemano. Pagás escribir el molde por adelantado; ganás que el compilador detecte errores de campo y que el IDE te autocomplete.

**2. ¿Qué es un DTO? Tres diferencias con un bean.**

Un **DTO** (*Data Transfer Object*) es un objeto sin lógica cuya única responsabilidad es transportar datos. Diferencias con un bean: (a) no tiene comportamiento, solo atributos; (b) hay **muchas instancias**, creadas y descartadas en cada llamada, contra una sola del bean; (c) lo instancia **Jackson**, no el contenedor de Spring.

**3. ¿Por qué `Pais` no lleva `@Component`?**

Porque `@Component` significa "creá **una** instancia al arrancar y compartila con todos", que es exactamente lo contrario de lo que un DTO necesita: un `Pais` nuevo por cada país de cada respuesta. Los DTOs los crea Jackson en cada llamada.

**4. Explicá el algoritmo de Jackson en tres pasos.**

(1) Crea el objeto **vacío** usando el constructor sin argumentos. (2) Recorre el JSON campo por campo y, por cada uno, **llama al setter** del atributo correspondiente. (3) Devuelve el objeto ya lleno, bajando recursivamente a los DTOs anidados.

**5. ¿Por qué `Pais` necesita `@NoArgsConstructor` **y** `@Data`? ¿Qué pasa si sacás cada una?**

Necesita `@NoArgsConstructor` porque Jackson **construye el objeto con el constructor vacío**: sin él lanza una excepción y no puede crear la instancia. Necesita `@Data` por los **setters**: sin ellos Jackson crea el objeto pero no puede llenarlo, y todos los atributos quedan en `null`.

**6. Si sacás `@NoArgsConstructor` y dejás `@AllArgsConstructor`, ¿por qué se rompe?**

Por una regla de **Java**, no de Lombok: el lenguaje solo regala el constructor vacío a las clases que **no declaran ningún constructor**. `@AllArgsConstructor` declara uno con parámetros y eso **elimina el implícito**, dejando a Jackson sin constructor sin argumentos.

**7. ¿Qué hace `@JsonIgnoreProperties(ignoreUnknown = true)` y qué pasaría sin ella?**

Le ordena a Jackson **ignorar los campos del JSON que el DTO no declara**. Sin ella, Jackson lanza una excepción ante el primer campo desconocido: si la API externa agrega un campo nuevo, la aplicación empieza a fallar en producción sin que hayas tocado una línea de código.

**8. Escribís `@JsonProperty("populaton")` (con typo). ¿Explota? ¿Qué valor queda?**

**No explota.** Jackson busca un campo `populaton` en el JSON, no lo encuentra, y por `ignoreUnknown = true` no se queja: el atributo queda en su valor por defecto, **`null`**. Es exactamente el bug del `comon` de la clase, y su síntoma —un campo en `null` con todos los demás correctos— es la firma de un `@JsonProperty` mal escrito.

**9. ¿Por qué `currencies` se mapea a un `Map` y no a una clase con un atributo por moneda?**

Porque las **claves del objeto JSON son impredecibles**: `"ARS"` para Argentina, `"PEN"` para Perú, una por cada moneda del mundo. No se puede declarar un atributo por cada una; `Map<String, DetalleMoneda>` es precisamente la estructura para claves que no se conocen de antemano.

**10. ¿Por qué `capital` es una `List<String>` y no un `String`?**

Porque la API lo devuelve como un **array**: un país puede tener más de una capital (Bolivia, Sudáfrica). El DTO tiene que **espejar la estructura del JSON**, no la que a uno le resultaría más cómoda.

---

## ✅ Punto 6 — Consumir la API y el test

**1. ¿Qué hace `getForObject(uri, Pais[].class)`? Los cuatro pasos.**

(1) Abre la conexión y **envía un GET** a la URI. (2) Recibe la **respuesta HTTP**. (3) Le entrega el cuerpo (texto JSON) a **Jackson**, que lo deserializa. (4) Devuelve el resultado **tipado** como `Pais[]`.

**2. ¿Por qué `Pais[].class` y no `List<Pais>`? ¿Qué hay que hacer después?**

Por el **type erasure** de Java: los genéricos se borran en tiempo de compilación, así que `List<Pais>.class` no existe y Jackson no sabría a qué tipo mapear. Se pide un **array**, que sí conserva el tipo, y después se convierte con `Arrays.asList(...)`.

**3. ¿Cuál es la diferencia entre `build()` y `buildAndExpand(...)`?**

`build()` se usa cuando la ruta es **fija**, sin placeholders. `buildAndExpand(valores)` se usa cuando la ruta tiene **placeholders** `{...}`: los rellena con los valores y, al hacerlo, **los escapa** (un espacio se convierte en `%20`).

**4. ¿Por qué no se concatena la URL con `+`? Tres cosas que se rompen.**

(a) Los **espacios** en el valor producen una URL inválida (hay que codificarlos); (b) los **caracteres especiales** (`&`, `?`, acentos) parten la URL al medio; (c) si la base termina en `/` y el path empieza con `/`, queda una **barra doble**.

**5. Path param vs query param: ¿cuál identifica y cuál filtra?**

El **path param identifica** un recurso y va en la ruta: `/name/{nombre}` → `/name/argentina`. El **query param filtra o modifica** la consulta y va después del `?`: `?fields=name,capital,region,...`.

**6. ¿Por qué existe `CAMPOS`? ¿Qué pasa si no la mandás? ¿Y si agregás un atributo al DTO y no acá?**

Existe porque **la API exige el parámetro `fields`** en el endpoint `/all`; sin él responde **400 Bad Request**. Si agregás un atributo al DTO pero no a `CAMPOS`, la API **no envía ese campo** y el atributo queda en `null` — el mismo síntoma que un `@JsonProperty` mal escrito.

**7. ¿Por qué `buscarPorNombre` devuelve `Optional<Pais>` y `buscarPorMoneda` devuelve `List<Pais>`?**

Porque **un nombre identifica a un único país**: existe o no existe, y `Optional` expresa exactamente esa ausencia posible. Una **moneda, en cambio, la comparten varios países** (el euro, veinte), así que el resultado es una colección por naturaleza. La firma comunica el contrato sin abrir el método.

**8. ¿Por qué los métodos devuelven `List.of()` en vez de `null`?**

Para que quien los llame pueda **operar sobre la lista directamente** (`isEmpty()`, iterar) sin chequear `null` primero. Devolver `null` obliga a defenderse en cada uso y es una fuente clásica de `NullPointerException`.

**9. ¿Qué hace `@SpringBootTest`? ¿Por qué sin ella no funcionaría el `@Autowired`?**

**Levanta el contenedor de Spring completo** antes de correr los tests: escanea, instancia e inyecta. Sin ella no habría contenedor y `@Autowired` no tendría de dónde sacar el bean: el campo quedaría en `null`.

**10. ¿Qué es AssertJ? ¿Es alternativa a JUnit o conviven?**

AssertJ es una **biblioteca de aserciones fluidas**. **Conviven**: JUnit es el motor que ejecuta los tests (entiende `@Test`) y AssertJ solo reemplaza las aserciones, aportando expresividad sobre colecciones (`contains`, `allMatch`, `extracting`).

**11. ¿Qué significa el sufijo `IT`? Tres razones de falla con el código perfecto.**

`IT` significa **Integration Test**: el test no está aislado, sino que **pega contra la API real** por HTTP. Puede fallar por (a) falta de conectividad, (b) caída o lentitud del servidor externo, o (c) un cambio en los datos que devuelve la API o un bloqueo por *rate limit*.

**12. ¿Cómo lo arreglarías? ¿Qué decisión de diseño te lo permite?**

Reemplazando el `RestTemplate` real por un **doble de prueba (mock)** que devuelva una respuesta fija sin salir a internet. Lo **permite la inyección por constructor**: como `BuscadorDePaises` **recibe** su `RestTemplate` desde afuera en lugar de hacerle `new` adentro, se lo puede sustituir sin modificar la clase. Ese es, precisamente, el motivo profundo por el que existe la inyección de dependencias — y el tema de la **clase 4**.

---

**FIN DEL COMPLEMENTO DEL RECORRIDO — Spring Boot (clase 02)**
