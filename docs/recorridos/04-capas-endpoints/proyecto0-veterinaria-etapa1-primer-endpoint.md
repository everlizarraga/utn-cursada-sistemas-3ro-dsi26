# 🌱 Proyecto 0 (clase 04) — Etapa 1: Tu primer endpoint

> **Objetivo:** que una request HTTP **entre** a tu aplicación, sea atendida por código tuyo, y vuelva con una respuesta — y que entiendas qué hizo cada una de las (pocas) piezas involucradas.
>
> **El momento clave:** en el proyecto-1 viste requests SALIR de tu app hacia APIs ajenas. Hoy se invierte el espejo: sos vos quien atiende. La puerta que Tomcat dejó abierta en la Etapa 0 recibe su primera visita.
>
> **Pre-requisito:** Etapa 0 completa (sondeo 🟢 o reactivado).
>
> **Tiempo estimado:** 40 minutos.

---

## 🧭 Mapa de esta etapa

1. El controller mínimo (2 anotaciones, 1 método).
2. Predicción + primera visita (navegador y Postman).
3. La revelación: qué pasó de verdad.
4. Experimento 1: sacar `@RestController`.
5. Experimento 2: sacar `@GetMapping`.
6. Experimento 3: dos rutas iguales.
7. Construcción: la ruta padre (`@RequestMapping`).
8. Experimento 4: la barra final (y el 404 que habla JSON).
9. Tu turno: un segundo endpoint, solo.
10. Criterios + checkpoint + registro + 🔗.

---

## 🛠️ Parte 1: El controller mínimo

Creá el package `controllers` (recién ahora — cada carpeta nace cuando su primera clase la necesita) y adentro la clase `PingController`. Lo mínimo indispensable son dos anotaciones y un método — tipealo:

```java
package com.practica.veterinaria.controllers;

// (dejá que el IDE te sugiera los imports de Spring)

@RestController
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
```

No lo corras todavía.

## 🔮 Parte 2: Predicción + primera visita

**Anotá tus predicciones** (en serio, por escrito — el contraste es el aprendizaje):

1. Con la app corriendo, ¿qué URL exacta vas a poner en el navegador para llegar a este método?
2. Cuando la request llegue y tu método se ejecute, ¿va a aparecer algo en la **consola** de la app?
3. ¿Qué status code va a tener la respuesta, si nadie lo configuró en ningún lado?

Ahora sí: levantá la app y visitá en el **navegador**: `http://localhost:8083/ping`

Deberías ver, pelado en la pantalla: `pong`

Contrastá con tus predicciones: **(1)** la URL es host + puerto + la ruta del `@GetMapping` — nada más, porque todavía no hay ruta padre; **(2)** la consola de la app **no mostró nada** — atendió en silencio (los logs de cada request existen pero vienen apagados por defecto); **(3)** el status… no lo viste en el navegador. Para eso está el segundo par de ojos:

**Postman:** `GET http://localhost:8083/ping` → mirá con lupa: **Status 200 OK** · body `pong` · y en los headers de la respuesta, `Content-Type: text/plain` — te devolvió *texto plano*. Guardá ese dato: va a cambiar solito en un par de etapas, y ese cambio va a ser importante.

*(Dato al paso: el navegador es una máquina de hacer GETs — cada URL que visitás es un GET. Por eso alcanzó para probar este endpoint… y por eso NO va a alcanzar cuando lleguen el POST y compañía. Postman es tu herramienta de acá en adelante.)*

🔧 **Truco de observabilidad artesanal:** ¿te molesta que la consola no muestre nada? Fabricale un testigo: un contador estático en el controller y un `System.out.println(">>> Atendió: " + contador)` adentro del método. Cada request queda registrada a tu manera. Es técnica legítima de exploración — cuando quieras ver si algo se ejecuta (y cuántas veces), plantale un testigo. Sacalo cuando termine de servir.

## 🤯 Parte 3: La revelación

Repasemos qué acaba de pasar, porque es el circuito que se repite en TODO lo que sigue:

1. El navegador armó una request HTTP (`GET /ping`) y la mandó al puerto 8083.
2. **Tomcat** la recibió (para eso estaba escuchando desde la Etapa 0).
3. Se la pasó a **Spring**, que mantiene una tabla de rutas: *"¿quién atiende GET en `/ping`?"* — y encontró tu método, porque las anotaciones se lo habían registrado al arrancar.
4. Ejecutó `ping()`. Tu `return "pong"` se convirtió en el **body** de la respuesta, con status **200** (el default cuando todo sale bien y nadie dice otra cosa).
5. La respuesta viajó de vuelta al navegador.

Y ahora sí, qué hace **de verdad** cada anotación:

- **`@RestController`** dice dos cosas a la vez: *"esta clase es un bean que atiende requests HTTP"* (Spring la construye, como a tu `Saludador` del sondeo — es un `@Component` especializado) y *"lo que retornen sus métodos va DIRECTO al body de la respuesta"*.
- **`@GetMapping("/ping")`** registra el método en la tabla de rutas: *"yo atiendo el verbo GET en la ruta /ping"*. La tabla se arma **al arrancar la app** — dato que el Experimento 3 va a volver importante.

👀 *Otra forma que vas a ver:* en proyectos con páginas web renderizadas en el servidor existe `@Controller` (sin `Rest`): ahí los retornos no van al body sino que apuntan a vistas/templates. `@RestController` = `@Controller` + "todo al body". Si ves `@Controller` pelado en código ajeno, estás ante una app con vistas del lado del servidor.

## 🧨 Parte 4: Experimento 1 — sacá `@RestController`

Comentá la anotación `@RestController` (dejá `@GetMapping` intacto). **Predicción:** ¿la app va a arrancar? Y si arranca, ¿qué devuelve `/ping`?

Corré y probá. Resultado: la app **arranca perfecto**… y `/ping` devuelve **404 Not Found** (en el navegador, la "Whitelabel Error Page" de Spring; en Postman, un 404 con un body de error).

¿Por qué? Sin `@RestController`, la clase **no es un bean**: Spring jamás la escaneó, jamás leyó su `@GetMapping`, y la ruta **no existe en la tabla**. El 404 no significa "algo explotó" — significa *"no tengo a nadie registrado para esa ruta"*. Fijate además el contraste con el error del puerto (Etapa 0): aquel rompía **al arrancar**; este ni siquiera es un error de la app — es una respuesta correcta a una pregunta sin dueño.

Restaurá la anotación y verificá que `/ping` volvió.

## 🧨 Parte 5: Experimento 2 — sacá `@GetMapping`

Ahora al revés: `@RestController` queda, comentá `@GetMapping`. **Predicción antes:** ¿qué esperás?

Resultado: **el mismo 404**. Pero por la causa opuesta: el bean existe, Spring lo escaneó… y no encontró ningún método que registrar. **Mismo síntoma, dos causas distintas** — grabátelo, porque el día que un endpoint tuyo dé 404 "sin razón", tu checklist mental arranca acá: ¿la clase es un controller? ¿el método tiene su mapping? ¿la ruta que llamo es EXACTAMENTE la registrada?

Restaurá.

## 🧨 Parte 6: Experimento 3 — dos rutas iguales

Agregale al controller un segundo método, también anotado `@GetMapping("/ping")`, que retorne cualquier otra cosa. **Predicción:** ¿cuál de los dos va a atender? ¿O va a pasar otra cosa — y en qué momento?

Corré… y **la app no arranca**. Leé el error completo (tu reflejo de la Etapa 0):

```
... Ambiguous mapping. Cannot map 'pingController' method
com.practica.veterinaria.controllers.PingController#otroPing()
to {GET [/ping]}: There is already 'pingController' bean method
com.practica.veterinaria.controllers.PingController#ping() mapped.
```

La lección es doble: **(1)** una combinación (verbo, ruta) tiene UN solo dueño — no hay "gana el primero"; **(2)** el conflicto se detecta **al construir la tabla, al arrancar** — no cuando llega la request. Spring prefiere no arrancar antes que arrancar ambiguo. (Compará los tres errores que ya conocés: puerto ocupado = al arrancar; 404 = en la request; ambiguous mapping = al arrancar. Estás armando tu mapa de *cuándo* explota cada cosa.)

Borrá el método duplicado.

## 🏗️ Parte 7: Construcción — la ruta padre

Todos los endpoints de este proyecto van a vivir bajo un prefijo común. Agregá **sobre la clase**:

```java
@RestController
@RequestMapping("/veterinaria")
public class PingController {
```

**Predicción:** ¿qué le pasó a la URL de tu endpoint? Probá las dos: `/ping` y `/veterinaria/ping`.

Resultado: `/ping` → 404 (¡ya sabés leerlo!) · `/veterinaria/ping` → `pong`. **`@RequestMapping` en la clase define la ruta padre**: todo mapping de adentro cuelga de ella. Es cómo un proyecto agrupa sus endpoints por recurso — y desde ahora, `/veterinaria` es la base de todo lo que construyas acá.

## 🧨 Parte 8: Experimento 4 — la barra final (y el 404 que habla JSON)

Agregale al controller un método más, con `@GetMapping` **vacío** (sin ruta): retorna un saludo cualquiera. Un mapping vacío bajo la ruta padre significa: *"yo atiendo la ruta padre a secas"* — `/veterinaria`.

**Predicción, por escrito, para estas CUATRO URLs:** ¿qué devuelve cada una?

1. `http://localhost:8083/veterinaria`
2. `http://localhost:8083/veterinaria/`  *(con barra final)*
3. `http://localhost:8083`
4. `http://localhost:8083/`

Probalas en Postman. Resultado: **(1)** 200 con tu saludo · **(2) 404** · **(3)** y **(4)** exactamente la misma respuesta entre sí (un 404, no hay nada mapeado en la raíz). Dos fenómenos distintos disfrazados de uno:

**La raíz: nunca fueron dos URLs.** Una request HTTP *no puede no tener path* — la primera línea que viaja por el cable es sí o sí `GET / HTTP/1.1`. Cuando escribís `localhost:8083` sin barra, el **cliente** normaliza y manda `GET /` igual. Las URLs 3 y 4 producen **la misma request en el cable**; por eso jamás podrían comportarse distinto. *(Verificalo: en Postman, View → Show Postman Console, y mirá la request line real de ambos envíos.)*

**`/veterinaria/`: sí son dos strings distintos** — `GET /veterinaria` y `GET /veterinaria/` — y tu tabla de rutas tiene registrada UNA entrada exacta: `/veterinaria`, a secas. La otra no matchea nada → 404 legítimo. **Spring no siempre fue así:** durante años el matcheo tolerante de barra final venía activado por defecto (un mapping atendía ambas formas); se deprecó en Spring Framework 6 y en la línea que usás ya no existe. ¿Por qué lo sacaron? Tratar dos URLs distintas como una genera ambigüedades reales (cachés y proxies las ven como recursos diferentes, y hay bugs de seguridad por "confusión de paths"). Es la misma filosofía del Experimento 3: **explícito y estricto le gana a mágico y tolerante.**

👀 *Dos cosas que vas a ver por esto:* **(1)** tutoriales viejos donde `/ruta/` "funciona solo" — no es magia, es versión (pre-Framework 6). **(2)** proyectos que registran ambas a mano: `@GetMapping({"", "/"})` — existe y es válido, pero casi siempre es mejor elegir la forma canónica (sin barra final) y documentar esa. Es lo que hace la cátedra.

**Ahora mirá el body del 404 con lupa.** Es **JSON**: `{"timestamp": ..., "status": 404, "error": "Not Found", "path": "/veterinaria/"}`. Dos perlas ahí: **(1)** ese formato no lo escribiste vos — es el **manejador de errores por defecto** de Spring Boot, que viene de fábrica con su propia "forma uniforme de error"… la que en la Etapa 6 vas a **reemplazar** por la tuya, como hace el proyecto de la clase. **(2)** Acabás de recibir la primera respuesta `application/json` de tu proyecto — y vino de un error, antes de que devuelvas tu primer objeto. Tus endpoints hablan `text/plain`; el error de fábrica habla `application/json`. Ese contraste es la semilla de la Etapa 3.

## ✍️ Parte 9: Tu turno — solo, sin pasos

Un segundo endpoint en el mismo controller: `GET /veterinaria/info` que devuelva un texto con el nombre de tu veterinaria (inventalo). Sin más indicaciones — ya tenés todo.

**Resultado esperado:** Postman → `GET http://localhost:8083/veterinaria/info` → 200, tu texto.

## ✅ Criterios de "Etapa 1 completa"

- [ ] `PingController` con ruta padre `/veterinaria` y sus endpoints GET funcionando (incluido el del mapping vacío).
- [ ] Los **cuatro** experimentos ejecutados, **con predicción previa escrita** y errores leídos.
- [ ] Podés explicar la diferencia entre los dos 404 (Exp. 1 vs Exp. 2) sin mirar.
- [ ] Entendés por qué `localhost:8083` y `localhost:8083/` son la misma request, pero `/veterinaria` y `/veterinaria/` son rutas distintas.
- [ ] Leíste el body JSON del 404 de fábrica y sabés quién lo generó.
- [ ] Viste el `Content-Type: text/plain` en Postman (lo vas a extrañar pronto).

## ✅ Checkpoint

*Recall:*
1. ¿Qué dos cosas declara `@RestController` a la vez?
2. ¿En qué momento se arma la tabla de rutas, y qué dos errores de hoy lo demuestran?
3. El 404 de `/veterinaria/` vino con un body JSON. ¿Quién lo generó, si vos no escribiste ningún manejador de errores?

*Decidí y justificá:*
4. Un compañero tiene un endpoint que da 404 y jura que "el código está perfecto, es un bug de Spring". Escribí tu checklist de diagnóstico, en orden, con el porqué de cada paso.
5. ¿Por qué es buena decisión que el conflicto de rutas duplicadas rompa el ARRANQUE en vez de resolverse "eligiendo uno" en runtime? ¿Qué atributo de calidad está comprando Spring ahí?
6. ¿Por qué `localhost:8083` y `localhost:8083/` no pueden comportarse distinto jamás, pero `/veterinaria` y `/veterinaria/` sí? ¿En qué lado del cable vive cada explicación?

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Abrí `ProductoController` del `sales-service`: mirá SOLO las primeras líneas — `@RestController` + `@RequestMapping("/sales-service/productos")`. Es exactamente tu Parte 7: su ruta padre es `servicio/recurso`, la tuya por ahora es solo `/veterinaria` (el recurso se le va a sumar cuando aparezcan los recursos de verdad). Del resto del archivo no mires nada todavía — cada pieza que te falta ahí es una etapa que viene. Y releé del recorrido **P6 §6 solo los comentarios ① y ②** del controller: deberían leerse como obviedades.

## ▶️ Próximo paso

Tu endpoint responde siempre lo mismo. La gracia empieza cuando la URL trae información — `/veterinaria/mascotas/7` — y tu código la usa. Decime **"arranquemos etapa 2"** y van las rutas con partes variables.

---

**FIN DE LA ETAPA 1**
