# 🌱 Proyecto 1 — Etapa 6c: La llamada HTTP por dentro

> **Objetivo:** desarmar la llamada de la Etapa 6 pieza por pieza. Qué es un pedido HTTP y qué es una respuesta, qué representa cada objeto de Java (`RestTemplate`, `HttpHeaders`, `HttpEntity`, `ResponseEntity`), **en qué línea sale el pedido**, y **qué hace tu código mientras espera**. Con la comparación con `fetch` y `await` de JavaScript.
>
> **El momento clave:** vas a medir cuánto tiempo se queda **parado** tu programa en la línea del `exchange`, y vas a ver que un `401` no te llega como respuesta sino como **excepción**.
>
> **Pre-requisito:** Etapa 6 completa, por lo menos hasta la Parte 11 (la app trae Argentina). La 6b no hace falta. **Es opcional:** podés pasar a la Etapa 7 y volver cuando quieras. Ayuda haber usado `fetch` en JavaScript, pero no es obligatorio.
>
> **Tiempo estimado:** 50-60 minutos.
>
> **Versiones:** Java 21 · Spring Boot 4.0.x (Spring Framework 7) · Maven 3.9 (el del wrapper `mvnw`).
>
> **Los métodos que se usan acá** son los de `CatalogoDePaises` de la Etapa 6 (ver el cuadro "Cómo va quedando `CatalogoDePaises`", al final de su Parte 9): `traerArgentinaComoTexto()` (Parte 6), `buscarArgentina()` (Parte 9) y, si hiciste el Experimento 2, `buscarPorNombre(...)`. Si armaste tu catálogo de otra forma, aplicá todo a cualquier método tuyo donde hagas un `exchange`.

---

## 🧭 Mapa de esta etapa

1. Qué viaja por la red: un pedido y una respuesta HTTP, en texto.
2. Cada parte del pedido, en Java.
3. Cada parte de la respuesta, en Java.
4. Armar el pedido no lo manda: línea por línea, qué pasa en memoria y qué pasa en la red.
5. Adentro del `exchange`, paso a paso.
6. El tiempo: un diagrama de secuencia de la llamada completa.
7. Comparación con JavaScript: `fetch`, `await` y `exchange`.
8. Cuando algo sale mal: errores HTTP y errores de red.
9. Experimentos.
10. Checkpoint.

---

## 📨 Parte 1: Qué viaja por la red

Antes de los objetos de Java, lo que de verdad viaja. HTTP es un protocolo **de texto**: tu programa le manda a la API un mensaje con un formato fijo, y la API le contesta con otro.

### El pedido (*request*)

Esto es, en esencia, lo que sale de tu programa cuando `traerArgentinaComoTexto()` (el método de la Etapa 6 que trae el texto crudo) llama a la API:

```
GET /countries/v5?q=argentina HTTP/1.1          ← 1. línea de pedido: MÉTODO  RUTA  VERSIÓN
Host: api.restcountries.com                     ← 2. headers: "Nombre: valor", uno por línea
Authorization: Bearer rc_live_tu_token
Accept: text/plain, application/json, ...
                                                ← 3. una línea en blanco: "terminaron los headers"
                                                ← 4. body: vacío (un GET no manda cuerpo)
```

| Parte | Qué es | En tu pedido |
|---|---|---|
| **Método** | qué querés hacer con el recurso | `GET` = "traeme" (otros: `POST` crear, `PUT` reemplazar, `DELETE` borrar) |
| **URL** (host + ruta + parámetros) | a quién y qué le pedís (el *host* es el nombre del servidor) | `api.restcountries.com` + `/countries/v5` + `?q=argentina` |
| **Headers** | datos extra sobre el pedido | `Authorization` (quién sos), `Accept` (qué formato aceptás) |
| **Body** | el contenido que mandás | nada |

### La respuesta (*response*)

Y esto es lo que vuelve:

```
HTTP/1.1 200 OK                                 ← 1. línea de estado: VERSIÓN  CÓDIGO  TEXTO
Content-Type: application/json                  ← 2. headers
Content-Length: 48213
                                                ← 3. línea en blanco
{"data":{"objects":[{"names":{"common":"Argentina", ...    ← 4. body: el JSON
```

| Parte | Qué es | En tu respuesta |
|---|---|---|
| **Código de estado** | cómo salió el pedido, en un número | `200` = salió bien. `401` = no autorizado, `404` = no existe, `500` = error del servidor |
| **Headers** | datos extra sobre la respuesta | `Content-Type: application/json` = "el body es JSON" |
| **Body** | el contenido | el JSON con Argentina |

> 🔍 **Esto lo conocés de JavaScript:** es lo mismo que ves en la pestaña *Network* del navegador cuando hacés un `fetch`. HTTP no depende del lenguaje: Java, JS o el navegador mandan y reciben exactamente este texto.

**La regla para todo lo que sigue:** cada objeto de Java que ves en la Etapa 6 representa **una parte de este texto**.

---

## 🧩 Parte 2: Cada parte del pedido, en Java

```
 Pedido HTTP                         Objeto Java en la Etapa 6
 ───────────                         ─────────────────────────
 GET                                 HttpMethod.GET
 https://api.restcountries.com/      String url = properties.getUrl() + "?q=argentina"
   countries/v5?q=argentina
 Authorization: Bearer rc_...  ┐     HttpHeaders headers   (headers.setBearerAuth(...))
 (otros headers)               ┘
 body (vacío)                        el Void de HttpEntity<Void>
 headers + body juntos               HttpEntity<Void> request = new HttpEntity<>(headers)
```

### `HttpMethod`

Una lista fija de los métodos HTTP: `HttpMethod.GET`, `HttpMethod.POST`, etc. Le dice al `exchange` qué palabra va al principio de la línea de pedido.

### `HttpHeaders`

Un objeto que guarda los headers como pares nombre → valor, con métodos de ayuda para los headers comunes. Se parece a un `Map` de Java, pero desde Spring 7 ya no lo es: los nombres se piden con `headerNames()`.

```java
HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth("rc_live_tu_token");      // agrega:  Authorization: Bearer rc_live_tu_token
// Equivalente "a mano":
// headers.set("Authorization", "Bearer rc_live_tu_token");
// Resultado esperado: un objeto en memoria con un header. Todavía no viajó nada.
```

### `HttpEntity<T>`: el sobre del pedido

Una **entidad HTTP** es "headers + body", las dos partes de un mensaje HTTP que no son la línea de pedido. `HttpEntity` las junta en un solo objeto, como un **sobre**: los headers son lo que escribís afuera y el body es lo que va adentro.

La `<T>` es el **tipo del body**:

```java
HttpEntity<Void> request = new HttpEntity<>(headers);
// Void = "no hay body". Solo headers: el sobre va vacío.
// Resultado esperado: un sobre en memoria con los headers adentro. Todavía no viajó nada.
```

> Si mandaras datos, por ejemplo con un `POST` para crear algo, el sobre llevaría un body:
> ```java
> HttpEntity<NuevoPais> request = new HttpEntity<>(nuevoPais, headers);
> // body = el objeto nuevoPais (Jackson lo convertiría a JSON), headers = los mismos
> ```

**Ni el `HttpMethod` ni la URL van dentro del sobre:** se los pasás aparte al `exchange`. El sobre es solo lo que va **en** el mensaje; el método y la URL dicen **a dónde y cómo** se manda.

---

## 📬 Parte 3: Cada parte de la respuesta, en Java

```
 Respuesta HTTP                      Objeto Java en la Etapa 6
 ──────────────                      ─────────────────────────
 HTTP/1.1 200 OK                     response.getStatusCode()   → 200 OK
 Content-Type: application/json      response.getHeaders()      → un HttpHeaders
 {"data":{"objects":[...]}}          response.getBody()         → String, o RespuestaPaises ya convertido
 todo junto                          ResponseEntity<T> response
```

### `ResponseEntity<T>`: el sobre de la respuesta

Es un `HttpEntity<T>` (headers + body) **más el código de estado**. Literalmente: en Spring, `ResponseEntity` es una clase hija de `HttpEntity` que le agrega el estado.

```
HttpEntity<T>                 ← headers + body
    └─ ResponseEntity<T>      ← headers + body + código de estado
```

La `<T>` es, otra vez, el tipo del body. Y lo elegís vos con el último parámetro del `exchange`:

```java
ResponseEntity<String> r1 = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
// r1.getBody() → el JSON como texto, sin convertir

ResponseEntity<RespuestaPaises> r2 = restTemplate.exchange(url, HttpMethod.GET, request, RespuestaPaises.class);
// r2.getBody() → un RespuestaPaises, ya armado por Jackson
```

`String.class` o `RespuestaPaises.class` no es un detalle: le dice al `RestTemplate` **en qué querés que se convierta el body** antes de entregártelo (Parte 5, paso 8).

### `RestTemplate`: el que hace el viaje

`RestTemplate` es el **cliente HTTP**: el objeto que agarra tu método, tu URL y tu sobre, lo convierte en el texto de la Parte 1, lo manda por la red, espera, lee el texto que vuelve, y te lo entrega como `ResponseEntity`.

Es el equivalente a `fetch` en JavaScript. Por dentro usa un cliente HTTP de más bajo nivel (por defecto, el que trae el propio Java), y le agrega lo cómodo: armar la URL, poner headers, convertir JSON a objetos, y avisarte con una excepción si algo salió mal.

> **Por qué es un bean (Etapa 6, Parte 5):** el `RestTemplate` no guarda nada de un pedido en particular; se puede reusar para todos. Por eso se crea uno solo (`@Bean`) y se inyecta donde haga falta.

---

## ⏱️ Parte 4: Armar el pedido no lo manda

Esta es la pregunta clave: **¿en qué línea sale el pedido?** Recorramos `traerArgentinaComoTexto()`, el método de la Parte 6 de la Etapa 6, marcando qué pasa en memoria y qué pasa en la red:

```java
public String traerArgentinaComoTexto() {
    String url = properties.getUrl() + "?q=argentina";
    // [memoria] un String. Nada salió.

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(properties.getApiKey());
    // [memoria] un objeto con un header. Nada salió.

    HttpEntity<Void> request = new HttpEntity<>(headers);
    // [memoria] el sobre armado. Nada salió.

    ResponseEntity<String> response = restTemplate.exchange(   // ◄═══ ACÁ
        url, HttpMethod.GET, request, String.class
    );
    // [RED] Esta línea manda el pedido, ESPERA la respuesta, y recién
    //       cuando la tiene completa, la devuelve. Mientras tanto, tu
    //       programa está PARADO en esta línea.

    return response.getBody();
    // [memoria] la respuesta ya llegó: solo sacás el body del sobre.
}
```

**Todo lo anterior al `exchange` es preparar el sobre. El `exchange` es el viaje completo: ida, espera y vuelta.** No hay otra línea que toque la red.

Por eso, cuando `exchange` termina y pasás a la línea siguiente, la respuesta **ya está**. No hay que esperarla de nuevo, ni pedirla, ni "resolverla".

---

## 🔬 Parte 5: Adentro del `exchange`, paso a paso

```java
restTemplate.exchange(url, HttpMethod.GET, request, RespuestaPaises.class)
```

```
 1. Arma la URL final
      "https://api.restcountries.com/countries/v5?q=argentina"
      (si hubiera caracteres raros, los codifica)
        ↓
 2. Prepara el pedido: método GET + los headers de tu sobre
        ↓
 3. Agrega el header Accept, según el tipo que pediste
      (pediste RespuestaPaises → "acepto JSON")
        ↓
 4. Si el sobre tuviera body, lo convierte y lo escribe (acá: no hay)
        ↓
 5. Abre la conexión con api.restcountries.com y MANDA el pedido
      (para https, primero se negocia la conexión segura)
        ↓
 6. ⏳ ESPERA. El hilo de tu programa (la línea de ejecución que va
      recorriendo tu código; Parte 6) queda bloqueado acá hasta que
      la API conteste. Puede ser 100 ms o varios segundos.
        ↓
 7. Llega la respuesta. Lee el código de estado:
      ├─ 2xx (200, 201...) → sigue
      ├─ 4xx (401, 404...) → tira HttpClientErrorException   (Parte 8)
      └─ 5xx (500, 503...) → tira HttpServerErrorException   (Parte 8)
        ↓
 8. Convierte el body al tipo que pediste:
      String.class          → lo deja como texto
      RespuestaPaises.class → se lo pasa a Jackson (Etapa 6, Parte 10)
        ↓
 9. Arma el ResponseEntity: código de estado + headers + body convertido
        ↓
10. Lo devuelve. Tu programa sigue en la línea siguiente.
```

Dos cosas que te ahorra el `exchange` y que en JavaScript hacés a mano: **el paso 7** (revisar si salió bien) y **el paso 8** (convertir el JSON). Las vas a ver en la Parte 7.

---

## 🕰️ Parte 6: El tiempo, en un diagrama de secuencia

Un **diagrama de secuencia** muestra quién le habla a quién y en qué orden, con el tiempo corriendo hacia abajo. Esta es la llamada completa de la Etapa 6, desde que Spring arranca tu `Arranque` hasta que se imprime Argentina:

```
 hilo "main"
 ┌─────────┐   ┌────────────────┐   ┌──────────────┐   ┌───────────┐      ┌──────────────────┐
 │Arranque │   │CatalogoDePaises│   │ RestTemplate │   │  Jackson  │      │ API restcountries│
 └────┬────┘   └───────┬────────┘   └──────┬───────┘   └─────┬─────┘      └────────┬─────────┘
      │ buscarArgentina()                  │                 │                     │
      │───────────────►│                   │                 │                     │
      │                │ arma url, headers,│                 │                     │
      │                │ sobre (memoria)   │                 │                     │
      │                │ exchange(...)     │                 │                     │
      │                │──────────────────►│                 │                     │
      │                │                   │ GET /countries/v5?q=argentina         │
      │                │                   │ Authorization: Bearer ...             │
      │                │                   │══════════════════════════════════════►│
      │                │                   │                 │                     │
      │   ⏳ el hilo "main" está PARADO desde acá...         │      la API busca,  │
      │                │                   │                 │      arma el JSON   │
      │                │                   │                 │                     │
      │                │                   │ 200 OK + JSON   │                     │
      │                │                   │◄══════════════════════════════════════│
      │   ...hasta acá ⏳                  │                 │                     │
      │                │                   │ convertí a      │                     │
      │                │                   │ RespuestaPaises │                     │
      │                │                   │────────────────►│                     │
      │                │                   │◄────────────────│                     │
      │                │ ResponseEntity    │                 │                     │
      │                │◄──────────────────│                 │                     │
      │                │ getBody().getData()                 │                     │
      │                │ .getObjects().get(0)                │                     │
      │ Pais argentina │                   │                 │                     │
      │◄───────────────│                   │                 │                     │
      │ println(...)   │                   │                 │                     │
```

- **Flechas finas (`──►`):** llamadas a métodos, dentro de tu programa. Son instantáneas.
- **Flechas dobles (`══►`):** el pedido y la respuesta, viajando por internet. Ahí está casi todo el tiempo.
- **Todo pasa en un solo hilo, `main`.** Un *hilo* (*thread*) es una línea de ejecución: el "dedo" que va recorriendo tu código. Mientras el pedido viaja, ese dedo está quieto en la línea del `exchange`: no ejecuta nada más.

A esto se le dice **llamada sincrónica y bloqueante**:
- **Sincrónica:** el resultado te llega como valor de retorno, en el mismo lugar donde llamaste.
- **Bloqueante:** mientras espera, el hilo no hace otra cosa.

> **¿Y por qué la app no se "cuelga"?** Porque es solo el hilo `main` el que está esperando. Tomcat, por ejemplo, tiene sus propios hilos y sigue escuchando el puerto 8080 mientras tanto. Con `RestTemplate` (y en el Java "clásico"), hacer varias cosas a la vez se logra con **varios hilos**, no con un solo hilo que salta entre tareas.

---

## 🟨 Parte 7: Comparación con JavaScript

Esta es la misma llamada, escrita con `fetch`:

```js
async function buscarArgentina() {
    const resp = await fetch(url + "?q=argentina", {          // 1. el pedido
        method: "GET",
        headers: { Authorization: `Bearer ${token}` }
    });
    if (!resp.ok) {                                          // 2. revisás vos si salió bien
        throw new Error("HTTP " + resp.status);
    }
    const json = await resp.json();                          // 3. convertís vos el body
    return json.data.objects[0];                             // 4. navegás
}
```

Y la de Java, para tenerlas lado a lado:

```java
public Pais buscarArgentina() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(properties.getApiKey());
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<RespuestaPaises> response = restTemplate.exchange(   // 1 + 2 + 3, todo junto
        properties.getUrl() + "?q=argentina", HttpMethod.GET, request, RespuestaPaises.class
    );
    return response.getBody().getData().getObjects().get(0);            // 4. navegás
}
```

### Pieza por pieza

| JavaScript (`fetch`) | Java (`RestTemplate`) |
|---|---|
| `fetch(url, opciones)` | `restTemplate.exchange(url, método, sobre, tipo)` |
| `method: "GET"` | `HttpMethod.GET` |
| `headers: { Authorization: ... }` | `HttpHeaders` + `setBearerAuth(...)` |
| `body: ...` (en las opciones) | el body dentro del `HttpEntity` |
| el objeto `Response` | `ResponseEntity<T>` |
| `resp.status` | `response.getStatusCode()` |
| `resp.headers` | `response.getHeaders()` |
| `await resp.json()` | ya hecho: el `RespuestaPaises.class` + `response.getBody()` |
| `if (!resp.ok) throw ...` | ya hecho: un 4xx o 5xx tira una excepción solo |
| `await` | **nada**: `exchange` ya espera |

### La diferencia de fondo: quién espera

```
 JavaScript                                     Java (RestTemplate)
 ──────────                                     ───────────────────
 fetch(...)  → devuelve una PROMESA ya,          exchange(...) → no devuelve nada hasta
               sin esperar la respuesta                          tener la respuesta
 await       → pausa ESTA función hasta          (no hace falta: el hilo ya está esperando
               que la promesa se resuelva         adentro del exchange)
 mientras tanto: el único hilo de JS sigue       mientras tanto: el hilo main está PARADO;
 atendiendo otras cosas (clicks, timers...)      otros hilos (Tomcat) siguen funcionando
```

- En JavaScript hay **un solo hilo**. Si se quedara parado esperando la red, se congelaría toda la página. Por eso `fetch` **no bloquea**: devuelve una promesa, y `await` "estaciona" tu función mientras el hilo atiende otras cosas. Cuando llega la respuesta, la función sigue desde el `await`.
- En Java hay **muchos hilos**. Que uno se quede parado esperando no congela el resto. Por eso `exchange` puede darse el lujo de **bloquear**: es más simple de leer, porque el código se ejecuta en el orden en que está escrito.

**La intuición que te sirve:** `restTemplate.exchange(...)` se comporta como un `await fetch(...)` que ya trae el `await` adentro, y que además hace el `resp.ok` y el `resp.json()` por vos. Tu código, en los dos casos, se lee de arriba hacia abajo y cada línea tiene el resultado de la anterior.

> **Una diferencia que muerde:** `fetch` **no** tira error por un 401 o un 404: la promesa se resuelve igual, y tenés que mirar `resp.ok`. `RestTemplate` **sí** tira una excepción. Si traés el hábito de JS de "revisar el status", en Java el status malo ni te llega como respuesta: te llega como excepción (Parte 8).

---

## 💥 Parte 8: Cuando algo sale mal

Hay dos tipos de fallas, y llegan distinto.

### La API contestó, pero con un error (4xx o 5xx)

Hubo respuesta HTTP: el pedido viajó, la API lo leyó y contestó "no". El `exchange` no te devuelve un `ResponseEntity`: **tira una excepción** (paso 7 de la Parte 5).

| Código | Excepción |
|---|---|
| 4xx (401, 403, 404…): el pedido estaba mal | `HttpClientErrorException` |
| 5xx (500, 503…): la API falló | `HttpServerErrorException` |

La excepción trae lo que contestó la API: el código de estado y el body del error.

### No hubo respuesta (error de red)

El pedido no llegó o la respuesta no volvió: sin internet, un host que no existe, la conexión cortada. No hay código de estado porque **no hay respuesta HTTP**. El `exchange` tira `ResourceAccessException`.

```
 ¿La API contestó?
   ├─ SÍ, con 2xx   → ResponseEntity                     (el camino feliz)
   ├─ SÍ, con 4xx   → HttpClientErrorException           (tiene código de estado y body)
   ├─ SÍ, con 5xx   → HttpServerErrorException           (tiene código de estado y body)
   └─ NO            → ResourceAccessException            (no hay código: no hubo respuesta)
```

### La familia de excepciones

Todas las excepciones del `RestTemplate` son parientes, y eso te deja elegir qué tan fino atrapás:

```
RestClientException                      ← la madre: atrapa TODO lo que tire el RestTemplate
 │                                          (árbol simplificado: hay clases intermedias)
 ├─ HttpStatusCodeException              ← hubo respuesta: tiene código de estado y body
 │    ├─ HttpClientErrorException  (4xx)
 │    └─ HttpServerErrorException  (5xx)
 └─ ResourceAccessException              ← no hubo respuesta (red): sin código de estado
```

Atrapar una clase madre atrapa también a todas sus hijas. Así queda un `try/catch` que cubre los tres casos, separando "la API contestó con error" de "no hubo respuesta":

```java
// imports: org.springframework.web.client.HttpStatusCodeException
//          org.springframework.web.client.ResourceAccessException
try {
    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    return response.getBody();
} catch (HttpStatusCodeException e) {           // 4xx y 5xx: la API contestó con un error
    System.out.println(">>> La API contestó: " + e.getStatusCode());          // ej.: 401 UNAUTHORIZED
    System.out.println(">>> Body del error: " + e.getResponseBodyAsString()); // el mensaje de la API
    throw e;    // la volvemos a tirar: acá solo la miramos
} catch (ResourceAccessException e) {           // red: no hubo respuesta, no hay código
    System.out.println(">>> No hubo respuesta: " + e.getMessage());
    throw e;
}
// Resultado esperado con un token inválido: las dos líneas del primer catch, y después la excepción
// Resultado esperado con un host que no existe: la línea del segundo catch, y después la excepción
```

- **Los `catch` se leen de arriba hacia abajo, y entra en el primero que coincida.** En el ejemplo, el orden de los dos da igual: son **hermanas**, ninguna es madre de la otra. El orden importa cuando una es madre de otra: la hija tiene que ir primero. Si pusieras un `catch (RestClientException e)` arriba de los otros dos, esos nunca se ejecutarían, y Java ni siquiera compila.
- **Otras formas de agruparlas:**
  - si querés 4xx y 5xx por separado: un `catch (HttpClientErrorException e)` y otro `catch (HttpServerErrorException e)`;
  - si querés tratarlos igual pero nombrándolos: `catch (HttpClientErrorException | HttpServerErrorException e)` (el `|` es un *multi-catch*: "cualquiera de estas");
  - si te alcanza con uno solo para todo: `catch (RestClientException e)`. Ojo: `RestClientException` no tiene `getStatusCode()` (lo tienen solo las hijas con respuesta); para leerlo tendrías que preguntar `if (e instanceof HttpStatusCodeException h)` y usar `h.getStatusCode()`.
- **El `throw e` del final** vuelve a tirar la excepción después de mirarla. Acá el `try/catch` es para entender qué pasó, no para arreglarlo: el programa igual tiene que enterarse de que la llamada falló.

> **Por qué en la Etapa 6 la app se cerraba con un token inválido:** la excepción salía del `exchange`, subía por el método del catálogo que llamaba `Arranque` (`buscarArgentina()`, `buscarPorNombre(...)`…) y llegaba al `run()` sin que nadie la atrapara. Spring aborta el arranque si un `CommandLineRunner` tira una excepción.

> 🕳️ **Madriguera — esperar para siempre.** Por defecto, un `RestTemplate` creado con `new RestTemplate()` **no tiene límite de espera** (*timeout*): si la API no contesta nunca, tu hilo se queda parado en el `exchange` para siempre. En un programa real se configura un tiempo máximo de conexión y de lectura.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 🧪 Parte 9: Experimentos

Hacelos sobre tu `CatalogoDePaises` de la Etapa 6. **Antes de empezar, en el `run()` de `Arranque` dejá solo la llamada a `traerArgentinaComoTexto()`** (comentá las demás: `buscarArgentina()`, `buscarPorNombre(...)`). Así lo que medís y lo que atrapás es esa llamada y no otra. Al terminar, dejá todo como estaba.

### Experimento 1: Medí la espera

Envolvé el `exchange` de `traerArgentinaComoTexto()` con dos mediciones de tiempo:

```java
System.out.println(">>> Hilo: " + Thread.currentThread().getName());
long antes = System.currentTimeMillis();

ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

long despues = System.currentTimeMillis();
System.out.println(">>> El exchange tardó " + (despues - antes) + " ms");
```

En el `run()` de `Arranque`, llamá a `traerArgentinaComoTexto()` **dos o tres veces seguidas** y corré.

**Resultado esperado**, algo como:
```
>>> Hilo: main
>>> El exchange tardó 620 ms
>>> Hilo: main
>>> El exchange tardó 180 ms
```
Los números dependen de tu conexión: pueden ir de decenas a miles de milisegundos. Esos milisegundos son el paso 6 de la Parte 5: tu hilo `main`, parado en una sola línea. **La primera llamada suele tardar más**, porque además abre la conexión segura; las siguientes la reutilizan. (Si tenés *devtools* en el proyecto, el hilo se llama `restartedMain`.)

### Experimento 2: Abrí el sobre de la respuesta

Antes del `return response.getBody();`, agregá:

```java
System.out.println(">>> Estado: " + response.getStatusCode());
System.out.println(">>> Content-Type: " + response.getHeaders().getContentType());
System.out.println(">>> Largo del body: " + response.getBody().length() + " caracteres");
// Resultado esperado: >>> Estado: 200 OK
//                     >>> Content-Type: application/json (puede traer más datos, como el charset: la codificación de los caracteres)
//                     >>> Largo del body: decenas de miles de caracteres
```

Ahí están las tres partes de la respuesta de la Parte 1: estado, headers, body.

### Experimento 3: Mirá lo que mandás (sin mostrar el token)

Antes del `exchange`, agregá:

```java
System.out.println(">>> Headers que mando: " + request.getHeaders().headerNames());
// Resultado esperado: >>> Headers que mando: [Authorization]
```

Se imprimen solo los **nombres** de los headers, no sus valores, para no mostrar el token. Fijate que el `Accept` **no aparece**: lo agrega el `RestTemplate` adentro del `exchange` (paso 3 de la Parte 5), después de que ya le diste el sobre.

### Experimento 4: El 401 llega como excepción

1. Agregá el `try/catch` de la Parte 8 en `traerArgentinaComoTexto()`.
2. Poné un token inválido en la config (o en el `.env`, si hiciste la 6b), por ejemplo `rc_live_xxxxx`.
3. Corré.

**Resultado esperado:**
```
>>> La API contestó: 401 UNAUTHORIZED
>>> Body del error: {...el mensaje que manda la API...}
```
y después la excepción, que cierra la app. Compará con `fetch`: en JS, este 401 te habría llegado como una respuesta normal con `resp.ok === false`. Volvé a poner tu token bueno.

### Experimento 5: Error de red, sin código de estado

En la config, cambiá la URL por un host que no existe, por ejemplo `https://api.restcountries-no-existe.com/countries/v5`, y corré.

**Resultado esperado:** entra en el **segundo** `catch` del Experimento 4, algo como:
```
>>> No hubo respuesta: I/O error on GET request for "https://api.restcountries-no-existe.com/countries/v5": api.restcountries-no-existe.com
```
**sin** código de estado, y después la `ResourceAccessException` cierra la app. La causa real aparece más abajo, en la traza del error, como `Caused by: java.net.UnknownHostException` (el host no existe). El primer `catch` no la atrapa: no es un error HTTP, porque nunca hubo respuesta. Volvé a poner la URL buena.

---

> 🕳️ **Madriguera — otras formas de hacer la llamada.**
> - **`RestClient`** es el cliente HTTP más nuevo de Spring. Es síncrono y bloqueante como `RestTemplate`, pero con una forma de escribir encadenada (`restClient.get().uri(...).header(...).retrieve().body(RespuestaPaises.class)`). Para código nuevo, Spring lo recomienda por encima de `RestTemplate`. En Spring 7.0 `RestTemplate` todavía no está deprecado (marcado como "no usar más") y es el que se usa en la cursada, pero Spring ya anunció que lo va a deprecar en la 7.1 y a quitar en una versión futura (8.0).
> - **`WebClient`** es el cliente **no bloqueante**: devuelve algo parecido a una promesa de JS, en vez de esperar. Necesita otra dependencia (WebFlux) y otro estilo de programación.
> - Con Java puro también se puede hacer en paralelo: varios hilos, o `CompletableFuture` (lo más parecido a una `Promise`).
>
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## ✅ Criterios de "Etapa 6c completa"

- [ ] Podés escribir de memoria las partes de un pedido HTTP (método, URL, headers, body) y de una respuesta (código de estado, headers, body).
- [ ] Sabés qué parte del mensaje representa `HttpMethod`, `HttpHeaders`, `HttpEntity`, `ResponseEntity` y `RestTemplate`.
- [ ] Podés señalar la línea exacta en la que sale el pedido, y explicar qué hace tu programa mientras espera.
- [ ] Podés explicar la diferencia entre `await fetch(...)` y `restTemplate.exchange(...)`.
- [ ] Sabés distinguir un error HTTP (4xx/5xx) de un error de red, qué excepción tira cada uno, y cómo atraparlos con `HttpStatusCodeException` y `ResourceAccessException`.
- [ ] Hiciste los Experimentos 1, 2 y 4.
- [ ] Dejaste `CatalogoDePaises` como estaba, con tu token bueno.

---

## ✅ Checkpoint

1. ¿Cuáles son las cuatro partes de un pedido HTTP? ¿Y las tres de una respuesta?
2. ¿Qué representa `HttpEntity<Void>`? ¿Por qué `Void`? ¿Qué pondrías en lugar de `Void` para un `POST`?
3. ¿Por qué el método y la URL no van dentro del `HttpEntity`?
4. ¿Qué agrega `ResponseEntity` a `HttpEntity`?
5. ¿Para qué sirve el último parámetro del `exchange` (`String.class` o `RespuestaPaises.class`)?
6. En `traerArgentinaComoTexto()`, ¿en qué línea sale el pedido? ¿Qué pasó en las líneas anteriores?
7. ¿Qué significa que la llamada sea sincrónica y bloqueante? ¿Qué hace el hilo `main` mientras la API contesta?
8. Si `exchange` bloquea el hilo `main`, ¿por qué Tomcat sigue respondiendo en el 8080 mientras tanto?
9. ¿Por qué en JavaScript necesitás `await` y en Java no?
10. Con un 401, ¿qué te devuelve `fetch`? ¿Y `exchange`?
11. ¿Qué diferencia hay entre `HttpClientErrorException` y `ResourceAccessException`? ¿Cuál trae código de estado y por qué?
12. Querés atrapar en un solo `catch` los 4xx y los 5xx, y en otro los errores de red. ¿Qué dos clases usás? ¿Importa el orden de esos dos `catch`? ¿Y si agregás un `catch (RestClientException e)`: dónde tiene que ir?
13. ¿Qué te pasaría si la API nunca contestara?

---

## 🎯 Una reflexión antes de seguir

Los nombres de la Etapa 6 ya no son mágicos: cada uno es una parte del texto que viaja por la red.

```
 sobre de ida:     HttpMethod + URL + HttpEntity(headers, body)
 el viaje:         RestTemplate.exchange  (manda, espera, recibe, revisa, convierte)
 sobre de vuelta:  ResponseEntity(estado, headers, body)
```

Y la pregunta "¿en qué momento se espera?" tiene una respuesta precisa: **adentro del `exchange`, con el hilo parado**. Lo que en JavaScript escribís en tres pasos (`fetch`, revisar `ok`, `json()`) y con un `await` en cada espera, en Java está en una sola línea que no devuelve hasta tener todo.

---

## ▶️ Próximo paso

- Si todavía no la hiciste y usás tu token real: **Etapa 6b** (`proyecto1-etapa6b-proteger-el-token.md`).
- Si no: **Etapa 7** (`proyecto1-etapa7-tests.md`).
