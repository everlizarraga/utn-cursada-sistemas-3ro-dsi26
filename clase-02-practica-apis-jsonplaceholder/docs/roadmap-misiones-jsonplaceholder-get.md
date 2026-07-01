# 🗺️ Roadmap de Misiones — Consumir JSONPlaceholder (GET)

> **Cómo funciona este roadmap:** cada etapa es una **misión**, no una solución. Te dice QUÉ hacer, POR QUÉ, CÓMO organizar el código, QUÉ esperar al correr, y QUÉ testear. **El código lo escribís vos.** Si te trabás, me pedís ayuda puntual de esa etapa o me mostrás tu código para feedback.
>
> **El andamiaje decrece:** las primeras etapas traen más pistas (fragmentos de recordatorio). Las últimas, solo la consigna — ahí ya volás solo.
>
> **La API:** JSONPlaceholder (`https://jsonplaceholder.typicode.com`), sin token. Recursos: `posts`, `users`, `comments`, `albums`, `photos`, `todos`. Solo vamos a hacer **GET** (traer datos).

---

## 📁 La estructura de packages (armala toda desde el inicio)

Dentro de tu package base (`io.github.everlizarraga.practicaapisjsonplaceholder`), creá estos sub-packages. Los vas a ir llenando a medida que avances:

```
io.github.everlizarraga.practicaapisjsonplaceholder/
│
├── PracticaApisJsonplaceholderApplication.java   ← el main (ya existe)
│
├── config/          ← configuración (beans como RestTemplate)
│
├── dto/             ← los DTOs (Post, User, etc.) que modelan el JSON
│
├── service/         ← los servicios que hacen las llamadas HTTP
│
└── runner/          ← los CommandLineRunner que disparan las llamadas
```

**Por qué esta estructura:**
- **`config/`** → todo lo que configura la app (beans). Separa "cómo se arma" de "qué hace".
- **`dto/`** → los objetos que representan los datos de la API. Agrupados, fáciles de encontrar.
- **`service/`** → la lógica de consumir la API. El corazón de la práctica.
- **`runner/`** → lo que ejecuta cosas al arrancar. Separado para no ensuciar el resto.

> **Convención de organización:** agrupar por "tipo de cosa" (dto, service, config) es una forma clásica y clara para proyectos chicos. La vas a ver mucho. (Existe otra forma —agrupar por "funcionalidad"— pero para esto, por tipo es lo más simple y didáctico.)

> **Tip:** en IntelliJ, click derecho en el package base → New → Package → escribí el nombre (`config`, `dto`, etc.).

---

## 🎯 ETAPA 1 — Traer UN post (objeto único)

### La misión
Pegarle a `GET /posts/1` y traer ese post **convertido en un objeto Java** (un DTO), no como texto crudo. Imprimir sus campos en consola.

### Por qué / qué practicás
El flujo base completo: bean `RestTemplate` → servicio que llama → DTO que modela la respuesta → runner que dispara. Es el "hola mundo" de consumir una API con objetos.

### El endpoint
```
GET https://jsonplaceholder.typicode.com/posts/1
```
Devuelve un objeto plano:
```json
{ "userId": 1, "id": 1, "title": "...", "body": "..." }
```

### Qué construir (en qué package va cada cosa)
1. **`config/AppConfig`** → un bean `RestTemplate`.
   - *Pista:* método con `@Bean` que hace `return new RestTemplate();`, dentro de una clase `@Configuration`. (Lo hiciste en el Proyecto 1.)
2. **`dto/Post`** → una clase con los cuatro campos (`userId`, `id`, `title`, `body`).
   - *Pista:* como los nombres del JSON coinciden con los de Java, NO necesitás `@JsonProperty`. Poné `@JsonIgnoreProperties(ignoreUnknown = true)` por las dudas. Si usás Lombok (lo heredás del padre), `@Data` + `@NoArgsConstructor` te ahorran los getters/setters.
3. **`service/ServicioPosts`** → un método `traerPost(int id)` que devuelve un `Post`.
   - *Pista:* como esta API NO usa token, podés usar el atajo `restTemplate.getForObject(url, Post.class)`. Recordá que `getForObject` hace el GET y convierte la respuesta al tipo que le pidas.
4. **`runner/ArranquePost`** → un `CommandLineRunner` que llame al servicio e imprima el post.

### Qué esperar en consola
```
>>> Post 1: sunt aut facere repellat provident...
>>> Autor (userId): 1
>>> Body: quia et suscipit...
```
(Los textos exactos varían, pero deberías ver el título, el userId y el body del post 1.)

### Qué testear (con pistas)
Creá un test en `src/test/java/...` con `@SpringBootTest`.
- **Test 1:** que el servicio se inyecta (no es null).
  - *Pista:* `@Autowired` el `ServicioPosts`, `assertThat(servicio).isNotNull();`
- **Test 2:** que `traerPost(1)` devuelve un post con `id == 1` y `title` no vacío.
  - *Pista:* `assertThat(post.getId()).isEqualTo(1);` y `assertThat(post.getTitle()).isNotBlank();`
  - *Recordá:* no ates el test a textos exactos que podrían cambiar; verificá estructura (id correcto, título no vacío).

### Criterio de completa
Corre, ves el post 1 en consola, y los dos tests pasan en verde.

---

## 🎯 ETAPA 2 — Traer TODOS los posts (lista)

### La misión
Pegarle a `GET /posts` (sin id) y traer **los 100 posts** como una lista de objetos. Imprimir cuántos vinieron y los primeros títulos.

### Por qué / qué practicás
Deserializar un **array JSON** en una colección Java. Acá aparece el tema `Post[]` vs `List<Post>` que vimos en el machete.

### El endpoint
```
GET https://jsonplaceholder.typicode.com/posts
```
Devuelve un array de 100 posts: `[ {...}, {...}, ... ]`

### Qué construir
1. En **`service/ServicioPosts`**, agregá un método `traerTodos()` que devuelva `List<Post>`.
   - *Pista:* el atajo con `getForObject` deserializa cómodo a **array**: `Post[] arreglo = restTemplate.getForObject(url, Post[].class);`. Después convertís el array a `List` (¿te acordás del machete? `Arrays.asList(arreglo)` o `List.of(arreglo)`).
   - *Reto opcional:* si querés devolver `List` directo sin el array intermedio, se puede con `exchange` + `ParameterizedTypeReference`, pero es más verboso. Para esta etapa, el array + conversión está perfecto.
2. En un runner (podés reusar o crear **`runner/ArranquePosts`**), llamá `traerTodos()` e imprimí `.size()` y algunos títulos.

### Qué esperar en consola
```
>>> Cantidad de posts: 100
>>> Primer título: sunt aut facere...
>>> Segundo título: qui est esse...
```

### Qué testear (con pistas)
- **Test:** que `traerTodos()` devuelve una lista de tamaño 100 (JSONPlaceholder tiene exactamente 100 posts).
  - *Pista:* `assertThat(servicio.traerTodos()).hasSize(100);`
- **Test:** que ningún post de la lista tiene título vacío.
  - *Pista:* podés usar AssertJ con `.allSatisfy(...)` o recorrer y verificar. Investigá `allSatisfy` — es elegante para esto.

### Criterio de completa
Ves "Cantidad de posts: 100" en consola y los tests pasan.

---

## 🎯 ETAPA 3 — Filtrar posts por usuario (query param, PRIMERA forma)

### La misión
Pegarle a `GET /posts?userId=1` para traer **solo los posts del usuario 1**. Acá aparece el **query param**, pero lo vas a armar todavía "a mano" (concatenando el string), para que en la Etapa 4 sientas por qué `UriComponentsBuilder` es mejor.

### Por qué / qué practicás
Query params: el `?userId=1` que filtra. Primero a mano (concatenando), para entender el problema antes de la solución elegante.

### El endpoint
```
GET https://jsonplaceholder.typicode.com/posts?userId=1
```
Devuelve solo los posts cuyo `userId` es 1 (son 10).

### Qué construir
1. En **`service/ServicioPosts`**, un método `traerPorUsuario(int userId)` que devuelva `List<Post>`.
   - *Pista:* por ahora, armá la URL concatenando: `".../posts?userId=" + userId`. Reusá la lógica de deserialización a array de la Etapa 2.

### Qué esperar en consola
```
>>> Posts del usuario 1: 10
```
(Cada usuario tiene 10 posts.)

### Qué testear (con pistas)
- **Test:** que `traerPorUsuario(1)` devuelve 10 posts.
- **Test:** que TODOS los posts devueltos tienen `userId == 1`.
  - *Pista:* `allSatisfy` de nuevo, o verificá que `.getUserId()` sea 1 en todos. Este test es importante: confirma que el filtro funcionó.

### Criterio de completa
Ves 10 posts del usuario 1, y el test confirma que todos son de ese usuario.

### 🕳️ Madriguera que vas a abrir en la Etapa 4
Concatenar `"?userId=" + userId` funciona con un número simple. ¿Pero qué pasa si el valor tuviera espacios, tildes, o símbolos raros? Se rompería. Guardate esa pregunta — la Etapa 4 la resuelve.

---

## 🎯 ETAPA 4 — Query params BIEN, con `UriComponentsBuilder`

### La misión
Rehacer la llamada con query params, pero ahora usando `UriComponentsBuilder` para construir la URL correctamente. **Esta es LA etapa que querías**: dominar la herramienta que "ves pero no sabés usar".

### Por qué / qué practicás
`UriComponentsBuilder`: construir URLs de forma segura, que maneja el "encoding" de caracteres especiales por vos. Es lo que se usa en proyectos reales en vez de concatenar strings.

### El concepto
`UriComponentsBuilder` es una herramienta de Spring para armar URLs paso a paso: le decís la base, los paths, los query params, y él arma la URL final bien formada (escapando espacios, tildes, etc.).

### Qué construir
1. Reescribí `traerPorUsuario(int userId)` (o hacé un método nuevo `traerPorUsuarioV2`) usando `UriComponentsBuilder` en vez de concatenar.
   - *Pista de la forma general* (para que arranques, no para copiar ciego):
     ```
     UriComponentsBuilder.fromHttpUrl("https://jsonplaceholder.typicode.com/posts")
         .queryParam("userId", userId)
         .toUriString()
     ```
     Eso te da la URL `.../posts?userId=1` bien armada. Después la usás igual que antes para el GET.
   - *Investigá vos:* `queryParam` se puede encadenar varias veces (para varios params). Probá agregar un segundo query param inventado y mirá cómo queda la URL (imprimila antes de llamar).

### Experimentos para entender la herramienta
- **Imprimí la URL** que genera `toUriString()` antes de hacer la llamada. Vas a ver cómo `UriComponentsBuilder` la arma.
- **Probá un valor con espacio:** pasá un query param con un espacio (ej: `.queryParam("q", "hola mundo")`) e imprimí la URL. Fijate cómo el espacio se convierte en `%20` o `+` automáticamente. **Eso es lo que a mano tendrías que hacer vos y olvidarías** — la herramienta lo hace sola. Ese es el "para qué sirve".

### Qué esperar en consola
```
>>> URL generada: https://jsonplaceholder.typicode.com/posts?userId=1
>>> Posts del usuario 1: 10
```

### Qué testear
- Mismo test que la Etapa 3 (10 posts, todos del usuario 1) — debe seguir pasando con la nueva implementación.
- **Test extra (opcional):** si separaste la construcción de la URL en un método propio, testealo: que dado `userId=1`, la URL generada contiene `userId=1`.

### Criterio de completa
La llamada funciona con `UriComponentsBuilder`, ves la URL bien formada en consola, y entendés (por el experimento del espacio) por qué es mejor que concatenar.

---

## 🎯 ETAPA 5 — Otro recurso: traer un usuario (DTO anidado)

### La misión
Pegarle a `GET /users/1` y traer un usuario. **Ojo:** el usuario tiene estructura **anidada** (address, company adentro). Vas a modelar solo una parte.

### Por qué / qué practicás
DTOs anidados de nuevo (como en el Proyecto 1, pero más suave), y la regla "modelá solo lo que usás". Además, organizar un segundo recurso en tu estructura.

### El endpoint
```
GET https://jsonplaceholder.typicode.com/users/1
```
Devuelve un usuario con campos planos (`id`, `name`, `username`, `email`, `phone`, `website`) y campos **anidados** (`address` que adentro tiene `street`, `city`, etc.; `company` que tiene `name`).

### Qué construir
1. **`dto/User`** → modelá los campos planos (`id`, `name`, `username`, `email`) y, si querés practicar anidación, agregá `address` como un objeto.
2. **`dto/Address`** (si modelás la dirección) → con `street`, `city`, etc.
   - *Pista:* como en el Proyecto 1, un objeto anidado en el JSON = otra clase DTO. Recordá `@JsonIgnoreProperties` en cada una.
3. **`service/ServicioUsers`** → un servicio nuevo (separado de posts) con `traerUsuario(int id)`.
   - *Decisión de organización:* ¿un servicio por recurso (`ServicioPosts`, `ServicioUsers`)? Sí, es lo prolijo. Cada recurso, su servicio.
4. Un runner que lo imprima.

### Qué esperar en consola
```
>>> Usuario 1: Leanne Graham (Bret)
>>> Email: Sincere@april.biz
>>> Ciudad: Gwenborough      ← si modelaste address
```

### Qué testear
- Solo la consigna ahora (menos pistas): testeá que `traerUsuario(1)` devuelve un usuario con `id == 1`, `name` no vacío, y `email` no vacío. Si modelaste address, verificá que la ciudad no sea nula.

### Criterio de completa
Traés el usuario 1, ves sus datos (incluida la ciudad si modelaste address), y los tests pasan.

---

## 🎯 ETAPA 6 — Recurso relacionado: comentarios de un post

### La misión
Traer los comentarios de un post usando la relación anidada. Hay dos formas de pedirlos; probá las dos y compará.

### Por qué / qué practicás
Rutas anidadas y query params de nuevo, consolidando todo. Y elegir entre dos formas de pedir lo mismo.

### Los endpoints (dos formas de traer lo mismo)
```
Forma A (ruta anidada):  GET /posts/1/comments
Forma B (query param):   GET /comments?postId=1
```
Las dos devuelven los comentarios del post 1.

### Qué construir (solo la consigna — ya volás más solo)
1. **`dto/Comment`** → modelá los campos que te interesen (`id`, `name`, `email`, `body`, `postId`).
2. **`service/ServicioComments`** → con dos métodos: uno para cada forma (`traerPorPostRutaAnidada(int postId)` y `traerPorPostQueryParam(int postId)`). En el segundo, usá `UriComponentsBuilder`.
3. Runner que llame a las dos y muestre que devuelven lo mismo.

### Qué esperar
Las dos formas devuelven la misma cantidad de comentarios (5 por post).

### Qué testear
- Que ambas formas devuelven 5 comentarios.
- Que ambas devuelven comentarios cuyo `postId` es 1.
- *Reto:* un test que compare que las dos formas devuelven la misma cantidad.

### Criterio de completa
Las dos formas funcionan y devuelven lo mismo. Entendés que a veces hay varias rutas para el mismo dato.

---

## 🏁 Cierre del roadmap

Al terminar las 6 etapas vas a tener:
- Un módulo organizado en packages (`config`, `dto`, `service`, `runner`).
- Servicios para 3 recursos (posts, users, comments).
- Dominio de: traer uno, traer listas, filtrar con query params **a mano y con `UriComponentsBuilder`**, DTOs planos y anidados, y tests para todo.
- **Lo que buscabas:** que consumir APIs en Java te fluya como el fetch de JavaScript.

### Cómo trabajamos de acá en más
- Hacé las etapas **en orden**, una por vez.
- **Si te trabás en una:** pedime ayuda puntual de ESA etapa, o mostrame tu código para feedback.
- **Si una etapa te sale sola:** avanzá sin consultarme, y si querés, mostrame el resultado para que te confirme que quedó prolijo.
- Los tests: hacelos en cada etapa, no los dejes para el final.

### Después de este módulo
Cuando domines esto, el siguiente módulo es **otra API pública** (PokéAPI, Rick & Morty, la que quieras) — mismo estilo, para consolidar. Cada API nueva, un módulo nuevo en tu repo.

---

**FIN DEL ROADMAP**

Arrancá con la Etapa 1. Cuando la tengas (o si te trabás), avisame. El código es tuyo — yo estoy para las dudas puntuales y el feedback.
