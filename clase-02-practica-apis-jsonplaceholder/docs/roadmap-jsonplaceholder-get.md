# 🗺️ Roadmap — Consumir JSONPlaceholder (GET)

**API:** `https://jsonplaceholder.typicode.com` · sin token · solo GET.
**Convención:** la del profe (`services/` plural, `dto/` adentro de `services/`).
**Cómo se juega:** cada etapa es una misión, el código lo escribís vos. Si te trabás, me pedís esa etapa puntual.

---

## 📁 Estructura de packages

```
<package base>/
├── ...Application.java   ← el main (ya existe)
├── config/               ← bean RestTemplate
├── services/             ← los servicios
│   └── dto/              ← los DTOs
└── runner/               ← CommandLineRunner (para ver salida en consola)
```

**Sobre `runner/` (lo único nuevo):** creás una clase `XxxRunner`, `@Component`, `implements CommandLineRunner`, inyectás el servicio por constructor, y sobreescribís `run()`. Igual que en el Proyecto 1, pero ahora en una clase y package dedicados. Adentro del `run()` va tu código de prueba (llamar al servicio + imprimir).

---

## 🎯 ETAPA 1 — Traer UN post

**Endpoint:** `GET /posts/1` → `{ userId, id, title, body }`

**Qué crear:**
- `config/AppConfig` → bean `RestTemplate` (`@Bean`, como Proyecto 1).
- `services/dto/Post` → 4 campos (`userId`, `id`, `title`, `body`). `@JsonIgnoreProperties(ignoreUnknown=true)` + `@Data` `@NoArgsConstructor`. Sin `@JsonProperty` (nombres coinciden).
- `services/ServicioPosts` → método `traerPost(int id)` → `Post`. Usá `getForObject(url, Post.class)` (sin token).
- `runner/PostRunner` → llama `traerPost(1)` e imprime.

**Consola esperada:** el título, userId y body del post 1.

**Test (`@SpringBootTest`):**
- `traerPost(1)` → `id == 1` y `title` no vacío. Pistas: `isEqualTo(1)`, `isNotBlank()`.

---

## 🎯 ETAPA 2 — Traer TODOS los posts

**Endpoint:** `GET /posts` → array de 100.

**Qué crear/agregar:**
- En `ServicioPosts` → `traerTodos()` → `List<Post>`. Deserializá a `Post[]` con `getForObject(url, Post[].class)`, después a List. Patrón del profe: `arreglo == null ? List.of() : Arrays.asList(arreglo)`.
- Runner → imprime `.size()` y algún título.

**Consola esperada:** `Cantidad: 100`.

**Test:** `traerTodos()` → `hasSize(100)`. Extra: ningún título vacío (`allSatisfy`).

---

## 🎯 ETAPA 3 — Filtrar por usuario (query param a mano)

**Endpoint:** `GET /posts?userId=1` → 10 posts.

**Qué agregar:**
- En `ServicioPosts` → `traerPorUsuario(int userId)` → `List<Post>`. URL concatenando: `".../posts?userId=" + userId`.
- Runner que lo muestre.

**Consola esperada:** `Posts del usuario 1: 10`.

**Test:** devuelve 10; todos con `userId == 1` (`allSatisfy`).

> Madriguera → Etapa 4: concatenar falla con espacios/tildes. Ahí lo arreglamos.

---

## 🎯 ETAPA 4 — Query param con `UriComponentsBuilder`

**Endpoint:** mismo que Etapa 3, pero armando la URL bien.

**Qué hacer:**
- Reescribí `traerPorUsuario` con `UriComponentsBuilder`. Forma (como el profe):
  ```
  URI uri = UriComponentsBuilder
      .fromUriString(".../posts")
      .queryParam("userId", userId)
      .build().toUri();
  ```
  Después `getForObject(uri, Post[].class)`.

**Experimento:** imprimí la `uri`. Probá `.queryParam("q", "hola mundo")` y mirá cómo el espacio se escapa solo. Ese es el "para qué sirve".

**Consola esperada:** la URI bien formada + los 10 posts.

**Test:** mismo de Etapa 3, debe seguir pasando.

---

## 🎯 ETAPA 5 — Un usuario (path variable + DTO anidado)

**Endpoint:** `GET /users/1` → campos planos + `address` (anidado).

**Qué crear:**
- `services/dto/User` → `id`, `name`, `username`, `email` (+ `address` si querés anidación).
- `services/dto/Address` → `street`, `city` (si modelás dirección).
- `services/ServicioUsers` → `traerUsuario(int id)`. Path variable con `UriComponentsBuilder`: `.path("/users/{id}").buildAndExpand(id)`.
- Runner.

**Consola esperada:** nombre, email (y ciudad si modelaste address).

**Test:** `traerUsuario(1)` → `id == 1`, `name`/`email` no vacíos.

---

## 🎯 ETAPA 6 — Comentarios de un post (dos formas)

**Endpoints:** `GET /posts/1/comments` (ruta anidada) y `GET /comments?postId=1` (query param). Ambos → 5 comentarios.

**Qué crear:**
- `services/dto/Comment` → `id`, `name`, `email`, `body`, `postId`.
- `services/ServicioComments` → dos métodos, uno por forma, ambos con `UriComponentsBuilder`.
- Runner que muestre que dan lo mismo.

**Test:** ambas dan 5; ambas con `postId == 1`. Reto: comparar que dan igual cantidad.

---

**Arrancá con la Etapa 1.** Trabado → pedime esa etapa. Te sale sola → avanzá.
