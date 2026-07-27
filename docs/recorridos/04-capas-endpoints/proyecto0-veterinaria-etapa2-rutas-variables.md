# 🌱 Proyecto 0 (clase 04) — Etapa 2: Rutas con partes variables

> **Objetivo:** que la URL deje de ser un cartel fijo y empiece a **traer información adentro** — `/veterinaria/eco/{palabra}` — y que tu código la capture, convertida al tipo que necesita.
>
> **El momento clave:** el experimento del `/doble/abc` — vas a ver a Spring rechazar solo lo que no puede convertir, y vas a entender quién valida qué antes de que tu código siquiera se ejecute.
>
> **Pre-requisito:** Etapa 1 completa (con el Experimento 4 — la barra final vuelve hoy).
>
> **Tiempo estimado:** 30-35 minutos.

---

## 🧭 Mapa de esta etapa

1. La ruta con hueco: `/eco/{palabra}`.
2. La revelación: captura y matcheo por nombre.
3. Conversión de tipos: `/doble/{numero}`.
4. Experimento 1: `/doble/abc` — leer el rechazo.
5. Experimento 2: ruta fija vs ruta variable — ¿quién gana?
6. Experimento 3: la barra final vuelve.
7. Tu turno: dos variables, solo.
8. Criterios + checkpoint + registro + 🔗.

*(Los endpoints de hoy son de juguete a propósito — el laboratorio de rutas. Viven en `PingController`, que ya es oficialmente tu patio de experimentos; cuando lleguen los recursos de verdad, este patio se limpia.)*

---

## 🛠️ Parte 1: La ruta con hueco

Agregá a `PingController`:

```java
@GetMapping("/eco/{palabra}")
public String eco(@PathVariable String palabra) {
    return "Dijiste: " + palabra;
}
```

**Predicción, por escrito:** ¿qué devuelven `GET /veterinaria/eco/hola` y `GET /veterinaria/eco/firulais`? ¿Y `GET /veterinaria/eco` — a secas, sin palabra?

Probá las tres. Las dos primeras: 200, con tu palabra devuelta. La tercera: **404** — y ya sabés leerlo: `{palabra}` es un hueco que **exige** un pedacito de ruta; sin pedacito, el patrón no matchea. (No es "parámetro opcional faltante": es *otra ruta que no existe*.)

## 🤯 Parte 2: La revelación

Qué acaba de pasar, pieza por pieza:

- **`{palabra}` en la ruta** declara una **parte variable**: ese segmento matchea cualquier valor (`hola`, `firulais`, `123`…), y el valor queda capturado con el nombre `palabra`.
- **`@PathVariable`** en el parámetro dice: *"llenáme este parámetro con esa parte capturada de la ruta"*. ¿Cómo sabe cuál? **Matchean por nombre**: la variable de ruta se llama `palabra`, el parámetro se llama `palabra` — se encuentran solos.
- La tabla de rutas de la Etapa 1 ahora tiene un patrón con comodín: un patrón, infinitas URLs.

👀 *Ya la conocés del recorrido, refresco de una línea: si el parámetro NO se llama como la variable de ruta, se lo indicás a la anotación — `@PathVariable("palabra") String otraCosa`. (P6 §6, comentario ⑤ — tu propio hallazgo.)*

👀 *Otra que vas a ver — query params:* información en la URL **después de un `?`**: `/buscar?especie=perro&limite=10`. Eso NO es `@PathVariable` — se captura con `@RequestParam`. El proyecto de la clase no los usa (todo viaja en la ruta o en el body), así que acá tampoco: reconocelos cuando los veas — la ruta identifica *recursos*, el query suele traer *filtros y opciones*.

## 🛠️ Parte 3: Conversión de tipos

Segundo juguete — fijate el **tipo** del parámetro:

```java
@GetMapping("/doble/{numero}")
public String doble(@PathVariable Long numero) {
    return "El doble de " + numero + " es " + (numero * 2);
}
```

**Predicción:** ¿`GET /veterinaria/doble/21` funciona? Pero pensá el detalle: en la URL, "21" es **texto** — todo lo que viaja en una URL es texto. Tu parámetro es `Long`. ¿Quién hizo la conversión?

Probá: 200, "El doble de 21 es 42". La conversión texto→`Long` la hizo **Spring, antes de llamar a tu método**. Tu código recibe el tipo listo — jamás parseás a mano un pedazo de ruta. Es el mismo servicio silencioso que viste en el `getById(@PathVariable Long id)` del proyecto de la clase.

## 🧨 Parte 4: Experimento 1 — `/doble/abc`

**Predicción:** si la conversión la hace Spring… ¿qué pasa cuando NO se puede? `GET /veterinaria/doble/abc` — ¿404? ¿500? ¿tu método se ejecuta y explota adentro?

Probá y mirá con lupa: **400 Bad Request**, con el body JSON de fábrica (tu conocido de la Etapa 1). Y el dato clave: **tu método nunca se ejecutó** — plantale un testigo (`println`) si querés comprobarlo. La ruta matcheó (por eso no es 404), pero la conversión falló *antes* de entrar a tu código, y Spring respondió por vos: *"tu request está mal formada"* — 400, error del cliente. En la consola de la app vas a ver el rastro (`MethodArgumentTypeMismatchException` — leelo, es tu tercer espécimen de error).

La lección de diseño: **el tipo del parámetro ES una validación gratis**. Declarar `Long id` te regala el rechazo automático de cualquier basura no-numérica, sin un if tuyo. Elegir bien los tipos de la firma es tu primera línea de defensa.

## 🧨 Parte 5: Experimento 2 — ruta fija vs ruta variable

Agregá, junto al eco:

```java
@GetMapping("/eco/especial")
public String ecoEspecial() {
    return "¡Ruta VIP!";
}
```

**Predicción doble:** *(a)* ¿la app arranca — o es el "Ambiguous mapping" del Experimento 3 de la Etapa 1? *(b)* Si arranca: `GET /veterinaria/eco/especial` matchea con los DOS patrones (`/eco/{palabra}` con palabra="especial", y `/eco/especial` exacto)… ¿cuál atiende?

Probá: **arranca sin drama**, y `/eco/especial` responde **"¡Ruta VIP!"** — ganó la fija. `/eco/hola` sigue yendo al eco. Dos lecciones:

1. **Patrones distintos que se superponen NO son ambigüedad** — coexisten. El "Ambiguous mapping" era por dos patrones *idénticos*. Superposición ≠ duplicación.
2. Cuando una URL matchea varios patrones, **gana el más específico**: la ruta exacta le gana al comodín. Spring resuelve por especificidad en el momento de la request — no "por orden en el archivo" ni "el primero que encuentre".

*(¿Te suena de algo? En la Etapa 1 el recorrido del proyecto real ya te avisaba "cuidado con las colisiones: alguna va a ganar por especificidad". Acabás de verla ganar.)*

## 🧨 Parte 6: Experimento 3 — la barra vuelve

Rapidito, cerrando el círculo de tu propio hallazgo: **predicción** — ¿`GET /veterinaria/eco/hola/` (barra final) qué da?

**404.** El patrón `/eco/{palabra}` espera exactamente esos segmentos después de la base; `eco/hola/` con su barra final es otro string, y el matcheo estricto de la Etapa 1 no perdona ni con comodines. Tu sutileza, confirmada en territorio nuevo.

## ✍️ Parte 7: Tu turno — dos variables, solo

Un endpoint `GET /veterinaria/tabla/{numero}/{multiplicador}` (ambos numéricos) que devuelva el resultado de la multiplicación en una frase. Sin más indicaciones.

**Resultado esperado:** `GET /veterinaria/tabla/7/8` → 200, algo como "7 x 8 = 56" · `GET /veterinaria/tabla/7/x` → **400** (¡y sabés exactamente por qué y quién lo rechazó!).

## ✅ Criterios de "Etapa 2 completa"

- [ ] `/eco/{palabra}` y `/doble/{numero}` funcionando, con predicciones escritas y contrastadas.
- [ ] Los tres experimentos hechos; el rastro del error de conversión leído en la consola.
- [ ] Podés explicar sin mirar: por qué `/doble/abc` da 400 y no 404 ni 500 — y por qué tu método no se ejecutó.
- [ ] Tu `/tabla/{numero}/{multiplicador}` anda, incluido su 400.

## ✅ Checkpoint

*Recall:*
1. ¿Cómo se encuentran la variable de ruta y el parámetro del método? ¿Y cuándo hace falta decírselo explícitamente?
2. ¿Quién convierte el texto de la URL al tipo del parámetro, y en qué momento exacto (antes/durante/después de tu método)?

*Decidí y justificá:*
3. Diferenciá los tres finales que ya conocés para una request rara: 404, 400 por conversión, y "Ambiguous mapping". ¿Cuál ocurre en qué momento, y qué te dice cada uno sobre DÓNDE está el problema?
4. Un compañero declara `@PathVariable String id` y adentro hace `Long.parseLong(id)` con su try/catch. Funciona. ¿Qué le señalás, qué se está perdiendo, y qué pasaría con `/productos/abc` en su versión vs la tuya?
5. ¿Por qué está bien que `/eco/especial` y `/eco/{palabra}` convivan, pero dos `/eco/{palabra}` idénticos rompan el arranque? ¿Qué regla profunda hay detrás de las dos decisiones?

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Abrí `ProductoController` del `sales-service` y mirá ahora **dos métodos**: `getById` (`@GetMapping("/{id}")` + `@PathVariable Long id`) y `getPrecio` (`@GetMapping("/{id}/precio")`). El primero es tu `/doble/{numero}` con traje de trabajo; el segundo es un patrón mixto — variable en el medio, fijo al final — que con lo de hoy leés de corrido. Y notá el tipo: `Long id` — la validación gratis del Experimento 1, trabajando en producción. Releé del recorrido **P6 §6 el comentario ⑤ completo** (con tu 👀 del nombre explícito): a esta altura es tuyo.

## ▶️ Próximo paso

Tus URLs ya traen información — pero un alta de mascota no viaja en la URL: viaja en el **body**, como JSON. Decime **"arranquemos etapa 3"** y llega el POST, los records como DTO… y el momento en que tu `Content-Type` cambia solo de `text/plain` a `application/json`.

---

**FIN DE LA ETAPA 2**
