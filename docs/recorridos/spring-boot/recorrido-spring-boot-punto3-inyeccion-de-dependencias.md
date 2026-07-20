# Recorrido Spring Boot — Punto 3
## Inyección de dependencias: cómo llegan las piezas

---

## 📄 Sobre este documento

**Qué cubre:** cómo los beans salen del contenedor y llegan a donde se los necesita. Inyección por **constructor** (la del componente) e inyección por **campo** con `@Autowired` (la del test). Por qué el `final` no es decorativo.

**Qué NO cubre:** por qué la inyección de dependencias es *buen diseño* (testabilidad, desacoplamiento, mocking). Eso es la **clase 4** y su lectura previa. Acá vemos el **mecanismo**: qué hace Spring y cómo.

**Cuánto es:** corto. Es el punto donde todo lo anterior hace clic.

---

## 🎒 De dónde venís

- **Punto 1:** inversión de control. Spring maneja el flujo.
- **Punto 2:** el contenedor tiene tres beans adentro: `restTemplate`, `restCountriesProperties`, `buscadorDePaises`. Sabés cómo entraron.
- **El hilo abierto desde el Punto 1:** *nadie escribe `new BuscadorDePaises(...)`, y sin embargo el objeto existe con sus dos dependencias adentro.* **Hoy lo cerramos.**

---

## 1. El problema, antes de la solución 🔴

Imaginate por un segundo que Spring **no existe**. Escribiste `BuscadorDePaises` igual, con el mismo constructor. Para usarlo en el test tendrías que hacer esto:

```java
// ❌ SIN inyección de dependencias — a mano, como venías haciendo en Java puro

class BuscadorDePaisesTest {

    @Test
    void buscarTodosDevuelveVariosPaises() {

        // Paso 1: construir la primera dependencia
        RestTemplate rt = new RestTemplate();

        // Paso 2: construir la segunda dependencia... y CONFIGURARLA a mano
        RestCountriesProperties props = new RestCountriesProperties();
        props.setBaseUrl("https://restcountries.com/v3.1");   // 👈 la URL, hardcodeada acá

        // Paso 3: recién ahora puedo construir lo que quería
        BuscadorDePaises buscador = new BuscadorDePaises(rt, props);

        // Paso 4: y por fin, el test
        var lista = buscador.buscarTodos();
        assertThat(lista).isNotEmpty();
    }

    // Y ahora repetí los pasos 1, 2 y 3 en CADA test.  ← el dolor
    // Y si mañana BuscadorDePaises necesita una tercera dependencia,
    // volvé a TODOS los tests a agregarla.              ← el dolor real
}
```

**Tres problemas, en orden de gravedad:**

1. **El armado se repite.** Cada lugar que necesite un `BuscadorDePaises` tiene que saber cómo construirlo, y construir además todo lo que él necesita.
2. **La URL queda hardcodeada** en el test — distinta de la que usa producción. Dos verdades para la misma cosa.
3. **Un cambio en el constructor rompe a todos los que hacen `new`.** Agregás una dependencia y tenés que recorrer el proyecto entero.

Ahora mirá lo que realmente escribió el profe:

```java
// ✅ CON inyección de dependencias — el test real del repo

@SpringBootTest
class BuscadorDePaisesIT {

    @Autowired
    private BuscadorDePaises buscadorDePaises;   // 👈 lo declarás. Y ya está.

    @Test
    void buscarTodosDevuelveVariosPaises() {
        var lista = buscadorDePaises.buscarTodos();   // ya viene armado, con todo adentro
        assertThat(lista).isNotEmpty();
    }
}
```

**Cero `new`. Cero armado. Cero URL hardcodeada.** Eso es lo que compraste.

---

## 2. Inyección por constructor: lo que hace el componente 🔴

Volvamos a `BuscadorDePaises` y leámoslo ahora con los ojos correctos:

```java
@Component
// ↑ PUNTO 2: "Spring, instanciá esta clase y guardala en el contenedor."
public class BuscadorDePaises {

    private final RestTemplate restTemplate;
    private final RestCountriesProperties propiedades;
    // ↑ DOS DECLARACIONES DE NECESIDAD.
    //   Traducción: "yo, para funcionar, necesito un cliente HTTP y las propiedades.
    //                No sé de dónde salen. No es mi problema conseguirlos."

    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }
    // ↑ EL CONSTRUCTOR ES EL CONTRATO.
    //   Es la lista de compras que la clase le entrega a quien la quiera construir.
    //   Fijate que NO hace ningún new adentro: RECIBE, no CONSTRUYE.
    //   👈 ESA es la inversión: la clase declara lo que necesita, no lo va a buscar.
}
```

### `// ¿CÓMO FUNCIONA?` — el arranque, esta vez completo

```
 1. Component scan encuentra las tres clases anotadas.

 2. Spring quiere instanciar BuscadorDePaises. Mira su constructor y ve:
       "pide un RestTemplate y un RestCountriesProperties"

 3. Spring va al contenedor y BUSCA POR TIPO:
       ¿tengo un bean de tipo RestTemplate?           → sí, el que fabricó @Bean   ✅
       ¿tengo un bean de tipo RestCountriesProperties? → sí                        ✅
    ⚠️ Ojo: busca por TIPO, no por el nombre del parámetro.
       Si el parámetro se llamara "rt" o "clienteHttp", funcionaría exactamente igual.

 4. Spring ejecuta:
       new BuscadorDePaises(elRestTemplate, lasPropiedades)
       👆 ACÁ ESTÁ EL new QUE BUSCABAS DESDE EL PUNTO 1.
          Lo hace Spring. Con los objetos que ya tenía en la bolsa.

 5. Guarda el resultado en el contenedor, bajo el nombre "buscadorDePaises".
```

> 🧵 **HILO CERRADO.** *"¿Quién hace el `new BuscadorDePaises(...)`?"*
> **Spring, en el paso 4, durante el arranque, leyendo el constructor y sirviendo los beans que le pide.**
> No hay magia. Hay un framework que sabe leer constructores.

**Notá el orden de construcción.** Spring **no puede** crear `BuscadorDePaises` antes de crear `RestTemplate` — necesita el segundo para construir el primero. Así que ordena solo el armado: primero las hojas, después lo que las usa.

```
      1º  [RestTemplate]        [RestCountriesProperties]     ← sin dependencias
                   └──────────────────┬───────────────┘
                                      ▼
      2º                    [BuscadorDePaises]                ← depende de los dos
                                      │
                                      ▼
      3º                   [BuscadorDePaisesIT]               ← depende del anterior
```

Eso se llama **grafo de dependencias**, y resolverlo es literalmente el trabajo principal del contenedor.

---

## 3. El `final` no es decorativo 🔴

Mirá otra vez los atributos:

```java
private final RestTemplate restTemplate;
private final RestCountriesProperties propiedades;
//      ^^^^^
```

Ese `final` **solo es posible gracias a la inyección por constructor**, y no es un detalle de estilo:

- `final` en un atributo significa: **se asigna una sola vez, en la construcción, y nunca más se puede cambiar**.
- El compilador te **obliga** a asignarlo en el constructor. Si te olvidás, no compila.
- Resultado: **es imposible** que `restTemplate` quede en `null` a mitad de camino. Si el objeto existe, sus dependencias existen. Garantizado por el compilador, no por tu memoria.

Con la otra forma de inyección (`@Autowired` sobre el campo, que vemos ahora), **no podés poner `final`** — el campo tiene que ser modificable para que Spring lo pueda escribir después de construir el objeto. Perdés esa garantía.

---

## 4. `@Autowired`: la otra forma, y por qué el test la usa 🟡

```java
@SpringBootTest
// ↑ "Levantá el contenedor completo de Spring antes de correr estos tests."
//    Sin esto, no habría contenedor, y no habría de dónde sacar nada.
//    (Lo desarmamos en el Punto 6.)
class BuscadorDePaisesIT {

    @Autowired
    // ↑ "Spring: buscá en el contenedor un bean de este tipo y METELO ACÁ ADENTRO."
    //    Literalmente: Spring construye el objeto de test vacío, y DESPUÉS
    //    le escribe el atributo desde afuera (usa reflexión — magia legítima de Java).
    private BuscadorDePaises buscadorDePaises;   // sin final, sin constructor, sin new

    @Test
    void buscarTodosDevuelveVariosPaises() {
        var lista = buscadorDePaises.buscarTodos();
        // ...
    }
}
```

**¿Por qué el test usa `@Autowired` y el componente usa constructor?**

Porque **la clase de test no la instanciás vos** — la instancia JUnit, con un constructor vacío. Vos no controlás ese `new`, así que no podés pasarle nada por constructor. Spring te da la puerta de servicio: *"dejame el campo declarado y yo te lo relleno después"*.

En clase Escobar mostró las dos y eligió una para cada lado. No fue casual, aunque no lo explicó:

| | Inyección por **constructor** | Inyección por **campo** (`@Autowired`) |
|---|---|---|
| Dónde se usa en el repo | `BuscadorDePaises` | `BuscadorDePaisesIT` (el test) |
| ¿Podés usar `final`? | ✅ **Sí** | ❌ No |
| ¿Puede quedar `null`? | ❌ Imposible — el compilador te cubre | ⚠️ Sí, si Spring no encontró el bean |
| ¿Se ve qué necesita la clase? | ✅ Sí — está en la firma del constructor | ❌ No — hay que leer todos los campos |
| ¿Se puede instanciar a mano en un test? | ✅ Sí (`new BuscadorDePaises(mock1, mock2)`) | ❌ No, necesitás Spring sí o sí |
| **Recomendación de la industria** | ✅ **Esta.** Es el default moderno. | Solo cuando no queda otra (tests, casos raros) |

**La última fila es la que importa a futuro:** en cualquier proyecto Spring profesional vas a ver inyección por constructor. `@Autowired` sobre campos se considera hoy una mala práctica *fuera de los tests*, justamente por las filas 3 y 5.

### 🤔 El detalle que te va a confundir cuando leas código ajeno

`BuscadorDePaises` **no tiene ningún `@Autowired`** sobre su constructor. ¿Por qué funciona igual?

Porque **desde Spring 4.3, si una clase tiene un solo constructor, Spring lo usa automáticamente para inyectar.** El `@Autowired` sobre el constructor es opcional y quedó implícito.

Vas a ver código viejo (y tutoriales viejos) que lo escribe así:

```java
@Component
public class BuscadorDePaises {
    // ...
    @Autowired   // ← REDUNDANTE en Spring moderno. Un solo constructor → ya se asume.
    public BuscadorDePaises(RestTemplate rt, RestCountriesProperties props) { ... }
}
```

Ambas versiones hacen **exactamente lo mismo**. La del profe (sin la anotación) es la moderna. Si ves la otra, no es un error: es un proyecto viejo o un dev con costumbres viejas.

---

## 5. El patrón, con nombre 🔴

> 🎯 **Inyección de Dependencias (DI — Dependency Injection)**
>
> **Qué es:** una clase **declara** las dependencias que necesita (en su constructor) en lugar de **construirlas** ella misma. Un tercero — el contenedor — se las provee ya armadas.
>
> **Por qué lo usamos:** la clase deja de estar atada a *implementaciones concretas* y pasa a depender de *lo que necesita*. Eso permite reemplazar una dependencia sin tocar la clase (por ejemplo: en un test, pasarle un `RestTemplate` falso que no llame a internet). También elimina el armado repetido y centraliza la configuración.
>
> **Dónde lo ves en ESTE código:** en el constructor de `BuscadorDePaises`, que **recibe** `RestTemplate` y `RestCountriesProperties` en lugar de hacerles `new`. Y en el `@Autowired` del test.
>
> **Analogía:** un restaurante. **Sin DI**, el cocinero sale a comprar los tomates, el aceite y la sal antes de cada plato — y si cambia el proveedor, hay que reentrenar al cocinero. **Con DI**, el cocinero **declara** su lista de ingredientes y alguien se los deja en la mesada. Al cocinero le da igual de dónde salieron. Y si querés probar la receta con tomates de plástico (un *mock*), se los dejás y él ni se entera.
>
> **Comparativo:**
>
> ```java
> // ❌ SIN DI — la clase construye lo que necesita
> public class BuscadorDePaises {
>     private final RestTemplate restTemplate = new RestTemplate();   // 👈 acoplado
>     private final String baseUrl = "https://restcountries.com/v3.1"; // 👈 hardcodeado
>     // → No podés testearlo sin internet.
>     // → No podés cambiar la URL sin recompilar.
>     // → La clase sabe DEMASIADO: qué cliente HTTP usar, y contra qué servidor.
> }
>
> // ✅ CON DI — la clase declara lo que necesita
> @Component
> public class BuscadorDePaises {
>     private final RestTemplate restTemplate;
>     private final RestCountriesProperties propiedades;
>
>     public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
>         this.restTemplate = restTemplate;
>         this.propiedades = propiedades;
>     }
>     // → La clase no sabe ni le importa de dónde salieron.
>     // → En un test podés pasarle un RestTemplate falso.
>     // → La URL vive afuera, en configuración.
> }
> ```

**Y esto ya lo viviste.** En React, `useContext` te entrega algo que vos no creaste ni fuiste a buscar: alguien más arriba lo puso ahí y el componente solo lo *consume*. Mismo principio, otra sintaxis. Java simplemente te obliga a declararlo con más ceremonia.

> 📌 **Para el parcial, si te preguntan: "¿Qué es la inyección de dependencias?"**
>
> Es un mecanismo por el cual un objeto recibe sus dependencias desde afuera en lugar de construirlas él mismo. La clase declara qué necesita (típicamente en su constructor) y un contenedor se encarga de proveérselo. Es la aplicación concreta del principio de inversión de control al armado de objetos: reduce el acoplamiento entre componentes y permite sustituir una dependencia (por ejemplo, por un doble de prueba) sin modificar la clase que la usa.
>
> ⚠️ **Ojo con la terminología en el parcial:** *inversión de control* es el **principio general** (el framework maneja el flujo); *inyección de dependencias* es **una técnica concreta** que lo aplica al armado de objetos. No son sinónimos, aunque en la charla informal se usen casi como tales. Si te preguntan la diferencia, esa es la respuesta.

> 🕳️ **Madriguera — DI y testeo: por qué esto importa de verdad**
> El beneficio grande de la DI no es ahorrarte `new`. Es que, al **recibir** sus dependencias, podés pasarle a `BuscadorDePaises` un `RestTemplate` **falso** que devuelva datos inventados sin tocar internet. Eso se llama **mocking**, y convierte un test lento y frágil en uno instantáneo y confiable.
> **📍 Dónde cae:** **clase 4**. Tiene video previo dedicado y repo propio (`github.com/dds-utn/ejemplo-mockeo`).
> *Volvé al camino — pero acordate de esta madriguera cuando llegues al Punto 6. Vas a ver el problema con tus propios ojos.*

---

## 6. El cuadro completo 🔴

```
   ARRANQUE
      │
      ├─ 1. Escanea            → encuentra @Component, @Configuration
      │
      ├─ 2. Instancia          → hace los new  (PUNTO 2)
      │                            new RestTemplate()  ← vía @Bean
      │                            new RestCountriesProperties()
      │
      ├─ 3. INYECTA            → lee el constructor de BuscadorDePaises,
      │                          busca los beans POR TIPO en el contenedor,
      │                          y ejecuta:
      │                            new BuscadorDePaises(rt, props)   ← EL new QUE FALTABA
      │
      └─ 4. Guarda todo en el contenedor. La app está lista.


   AL CORRER EL TEST
      │
      ├─ @SpringBootTest  → levanta el contenedor (todo lo de arriba)
      │
      └─ @Autowired       → saca "buscadorDePaises" del contenedor
                            y lo escribe en el campo del test
```

**Eso es todo Spring, hasta acá.** Escanear, instanciar, inyectar. Las tres palabras que estaban detrás de la palabra "magia".

---

## ✅ Checkpoint — Punto 3

1. ¿Quién ejecuta el `new BuscadorDePaises(...)`? ¿En qué momento y con qué información?
2. Cuando Spring resuelve el constructor, ¿busca los beans por **nombre del parámetro** o por **tipo**? ¿Qué consecuencia práctica tiene eso?
3. ¿Por qué los atributos de `BuscadorDePaises` pueden ser `final` y los del test no?
4. ¿Por qué el test usa `@Autowired` en vez de inyección por constructor? Dalo con la razón real, no "porque sí".
5. `BuscadorDePaises` no tiene ningún `@Autowired`. ¿Por qué funciona igual?
6. Enumerá tres problemas concretos del código "sin DI" de la sección 1.
7. ¿Cuál es la diferencia entre **inversión de control** e **inyección de dependencias**? (Cuidado: no son sinónimos.)
8. Si `BuscadorDePaises` hiciera `new RestTemplate()` adentro en vez de recibirlo, ¿qué perdés? Nombrá al menos dos cosas.

---

## 🎯 Qué viene en el Punto 4

Ya sabés cómo Spring crea y conecta objetos. Falta una pieza del rompecabezas: **`RestCountriesProperties`**.

Ese bean no tiene lógica. Tiene un solo atributo, `baseUrl`, y aparece mágicamente **con la URL adentro** — una URL que no está escrita en ninguna clase Java. Está en un archivo `.yml` que nunca abriste.

En el Punto 4: cómo Spring lee un archivo de texto y lo convierte en un objeto Java tipado. Y por qué hacer todo ese lío es mejor que escribir la URL y listo.

---

**FIN DEL PUNTO 3**
