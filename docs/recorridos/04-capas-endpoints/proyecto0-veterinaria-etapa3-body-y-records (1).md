# 🌱 Proyecto 0 (clase 04) — Etapa 3: El canal grande — el body y los records

> **Objetivo:** que tu API hable JSON en los dos sentidos: **devolver objetos** (y ver la mutación del `Content-Type` que te vengo prometiendo) y **recibir objetos** por el body con tu primer POST — con records haciendo de DTO, como en el proyecto de la clase.
>
> **El momento clave:** el instante en que devolvés tu primer record y el `text/plain` de siempre muta solo a `application/json` — ahí entendés quién estuvo traduciendo todo el tiempo.
>
> **Pre-requisito:** Etapa 2B completa.
>
> **Tiempo estimado:** 45-50 minutos.

---

## 🧭 Mapa de esta etapa

1. La mutación: tu primer objeto devuelto.
2. La revelación: Jackson, el traductor invisible.
3. Records en 5 líneas (refresco con lupa).
4. El canal 3: POST + `@RequestBody`.
5. Experimento 1: campos que faltan y campos que sobran.
6. Experimento 2: JSON roto.
7. Experimento 3: el Content-Type de la request.
8. Tu turno: un presupuesto, solo.
9. Criterios + checkpoint + registro + 🔗.

---

## 🛠️ Parte 1: La mutación

Creá el package `dtos` y adentro tu primer record:

```java
package com.practica.veterinaria.dtos;

public record InfoVeterinaria(String nombre, String direccion, boolean atiendeHoy) { }
```

Y en el controller, un endpoint que lo **devuelva** (convive con tu `/info` viejo, que devolvía String):

```java
@GetMapping("/info-completa")
public InfoVeterinaria infoCompleta() {
    return new InfoVeterinaria("Veterinaria Firulais", "Av. Siempreviva 742", true);
}
```

**Predicción, por escrito:** *(a)* ¿qué va a mostrar el body en Postman? *(b)* ¿y el header `Content-Type` de la respuesta — el `text/plain` de siempre, u otra cosa? *(c)* ¿escribiste vos algo, en algún lado, que convierta ese objeto a texto?

Probá `GET /veterinaria/info-completa` y mirá **las dos cosas**: el body —

```json
{ "nombre": "Veterinaria Firulais", "direccion": "Av. Siempreviva 742", "atiendeHoy": true }
```

— y el header: **`Content-Type: application/json`**. Mutó solo. Compará con `/info` (String): sigue en `text/plain`. Mismo controller, misma anotación, y el tipo de retorno decidió el idioma de la respuesta.

## 🤯 Parte 2: La revelación — Jackson, el traductor invisible

¿Quién convirtió tu objeto a JSON? Nadie que hayas llamado vos. El starter `webmvc` (Etapa 0, la "lista de compras") trajo entre sus paquetes a **Jackson**: la biblioteca que convierte objetos Java ↔ JSON. Y `@RestController` (Etapa 1: *"lo que retornen los métodos va directo al body"*) tenía la segunda mitad de su contrato escondida: **si el retorno es un objeto, Jackson lo serializa a JSON antes de que salga** — campo por campo, usando los **nombres de tus componentes como claves** (`nombre` → `"nombre"`). *Serializar* = convertir un objeto a un formato transportable; el viaje inverso (texto → objeto) es *deserializar*, y lo vas a ver en la Parte 4.

Por eso el 404 de fábrica de la Etapa 1 ya hablaba JSON: el manejador de errores de Spring devuelve objetos, y Jackson los traduce igual que a los tuyos. El traductor siempre estuvo; recién hoy le diste trabajo con material propio.

## 📦 Parte 3: Records en 5 líneas (con lupa)

Ya los conocés del recorrido — refresco con los detalles que importan al tipearlos:

- La declaración `record X(campo1, campo2...)` parece la firma de un constructor — **y lo es**: Java genera el constructor, los accessors, `equals`, `hashCode` y `toString`.
- **Inmutable**: sin setters, los valores se fijan al construir. Perfecto para un dato que solo viaja.
- Detalle de dedos: los accessors se llaman **como el componente, a secas** — `info.nombre()`, no `info.getNombre()`. (Jackson entiende ambos estilos; tus dedos van a tropezar con esto una semana.)
- Un record ES el DTO ideal — por eso el proyecto de la clase los usa para toda su frontera de datos.

👀 **Otra forma que vas a ver — el DTO como clase común.** El equivalente exacto de un record DTO, en los dos sabores que circulan:

```java
// Sabor 1 — a mano (proyectos pre-records, o sin Lombok):
public class TurnoRequest {
    private String mascota;
    private String dia;
    private int duracionMinutos;

    public TurnoRequest() { }                 // ← constructor vacío: Jackson lo NECESITA (abajo el porqué)

    public String getMascota() { return mascota; }
    public void setMascota(String mascota) { this.mascota = mascota; }
    // ... y los getters/setters de los otros dos campos, igual
}

// Sabor 2 — con Lombok (el más común en proyectos reales):
@Getter @Setter @NoArgsConstructor
public class TurnoRequest {
    private String mascota;
    private String dia;
    private int duracionMinutos;
}
// (también vas a ver @Data, el combo todo-en-uno de Lombok)
```

Mismo rol, más ceremonia — **y una trampa que el record te ahorraba sin avisarte**: para *deserializar* (`@RequestBody`), Jackson construye la clase con el **constructor vacío** y la va llenando con los **setters**. Si falta alguno de los dos, explota con un error críptico famoso: *"Cannot construct instance... no Creators, like default constructor, exist"*. El record no sufre esto porque Jackson usa su constructor canónico directo, sin setters ni ceremonia. (*Serializar* — la salida — es menos exigente: con getters alcanza.) Conclusión: mismo rol, pero el record es el DTO con menos superficie para errores — por eso la cátedra lo eligió. Si un día te toca un DTO-clase que "no deserializa", ya sabés los dos sospechosos.

## 🛠️ Parte 4: El canal 3 — POST + `@RequestBody`

Ahora el sentido inverso: que un objeto **entre**. El caso de juguete: solicitar un turno. El DTO de entrada:

```java
public record TurnoRequest(String mascota, String dia, int duracionMinutos) { }
```

Y el endpoint — verbo nuevo, anotación nueva, y una más que ya viste en el recorrido:

```java
@PostMapping("/turnos")
@ResponseStatus(HttpStatus.CREATED)
public String solicitarTurno(@RequestBody TurnoRequest request) {
    return "Turno para " + request.mascota() + " el " + request.dia()
         + " (" + request.duracionMinutos() + " min)";
}
```

- **`@PostMapping`**: mismo clan que `@GetMapping`, otro verbo — POST es el verbo de *crear/enviar cosas*, y su información viaja en el body.
- **`@RequestBody`**: *"el body JSON de la request, deserializámelo en una instancia de este record"*. Jackson trabajando en reversa: matchea las claves del JSON con los componentes **por nombre**, convierte tipos, y tu método recibe el objeto armado.
- **`@ResponseStatus(HttpStatus.CREATED)`**: pisa el 200 por defecto — cuando un POST crea algo, la respuesta honesta es **201 Created**. Un GET que sale bien es 200; un POST que creó, 201. El código de estado es parte del contrato.

**Probalo en Postman — y acá cambia tu rutina:** método **POST**, URL `/veterinaria/turnos`, pestaña **Body → raw → JSON** (fijate que al elegir JSON, Postman setea solo el header `Content-Type: application/json` de la *request* — guardá ese detalle para el Experimento 3):

```json
{ "mascota": "Firulais", "dia": "lunes", "duracionMinutos": 30 }
```

**Resultado esperado:** **201 Created** + tu frase con los tres datos. Si lo ves: acabás de completar el circuito — JSON entró, se volvió objeto, tu código lo usó.

👀 *Del recorrido, refresco: si la clave del JSON no coincide con tu componente (`"duracion_minutos"` estilo snake_case), el puente es `@JsonProperty("duracion_minutos")` sobre el componente — P6 §6, tu propio hallazgo. Mismo patrón que el nombre explícito de `@PathVariable`: matcheo por nombre, con escape manual cuando los nombres difieren.*

## 🧨 Parte 5: Experimento 1 — campos que faltan y campos que sobran

**Predicción, por escrito:** mandá el POST *(a)* **sin** `"mascota"` en el JSON, y *(b)* **sin** `"duracionMinutos"`. ¿Rebota con 400? ¿Explota adentro? ¿Entra con qué valores? ¿Los dos casos se comportan igual?

Probá: **entran los dos, con 201.** Pero mirá los valores: la mascota ausente llegó como **`null`**… y la duración ausente llegó como **`0`**. La trampa: `String` es objeto (ausente → null), pero `int` es **primitivo** — no puede ser null, y Jackson le deja el default del tipo (`0`, `false`). Dos ausencias, dos resultados distintos — y **nadie rechazó nada**: matchear por nombre significa que lo que no está, simplemente no se asigna. *(¿Y quién valida entonces que la mascota venga? Por ahora: NADIE. Anotá la incomodidad — tiene dueño, y lo conocés del recorrido: llega en la próxima etapa.)*

Ahora al revés — mandá un campo **de más**: `"color": "azul"`. **Predicción primero.** Resultado: **ignorado en silencio**, 201 como si nada. El default de Jackson es tolerante con lo desconocido — cómodo para evolucionar APIs, y otro recordatorio de que la forma del DTO define qué entra: lo que no tiene componente, no existe.

## 🧨 Parte 6: Experimento 2 — JSON roto

Mandá el body con la sintaxis rota (sacale una comilla, dejá una coma colgada). **Predicción:** ¿400, 500, o entra mutilado?

**400 Bad Request** — el body ni llegó a ser JSON, así que no hay objeto posible: request mal formada, culpa del cliente. En consola, el espécimen **#5** de tu colección: `HttpMessageNotReadableException`. Y una variante para predecir sin probar (o probando, son 30 segundos): JSON válido pero con **tipo imposible** — `"duracionMinutos": "media hora"` — ¿qué esperás? La misma familia: 400, no se pudo deserializar. Contrastalo con la Parte 5: campo *ausente* entra con null/default; campo *presente pero intraducible* rebota con 400. Ausente ≠ inválido — la distinción que va a volver con todo en la etapa del PATCH.

## 🧨 Parte 7: Experimento 3 — el Content-Type de la request

En Postman, cambiá el tipo del body de **JSON** a **Text** (mismo contenido, el JSON válido de siempre) y mandá. **Predicción:** el contenido es idéntico… ¿pasa algo?

**415 Unsupported Media Type** — espécimen **#6**, y de los buenos: el `Content-Type` de la **request** es la etiqueta que le dice al servidor cómo interpretar el body. Con `text/plain`, Spring ni intenta deserializar: *"me mandás algo que no sé (o no acepto) leer como lo que espero"*. El contenido era perfecto; **la etiqueta lo condenó**. Moraleja doble: el header es parte del mensaje tanto como el body, y cuando un POST tuyo dé 415 "inexplicable", ya sabés el primer lugar donde mirar. Volvé a JSON.

## ✍️ Parte 8: Tu turno — un presupuesto, solo

Endpoint `POST /veterinaria/presupuestos`: recibe un record con `servicio` (String), `cantidad` (int) y `precioUnitario` (double); **devuelve un record** (no un String — quiero tu JSON de salida) con los tres datos más el `total` calculado. Status correcto para una creación. Sin más indicaciones.

**Resultado esperado:** body `{"servicio":"vacuna","cantidad":3,"precioUnitario":1500.0}` → **201**, respuesta JSON con `"total": 4500.0` y `Content-Type: application/json` · sin `"cantidad"` → 201 con total 0.0 (¡y sabés exactamente por qué!) · JSON roto → **400** · body como Text → **415**.

## ✅ Criterios de "Etapa 3 completa"

- [ ] Viste la mutación: mismo controller, `/info` en `text/plain` y `/info-completa` en `application/json`.
- [ ] Tu POST de turnos funciona con su 201, y podés explicar quién deserializó el body.
- [ ] Los tres experimentos hechos con predicción escrita; especímenes #5 y #6 leídos en consola/Postman.
- [ ] Podés explicar la diferencia null vs 0 en campos ausentes — y por qué nadie validó nada.
- [ ] Tu `/presupuestos` cumple sus cuatro resultados esperados.

## ✅ Checkpoint

*Recall:*
1. ¿Quién convierte objetos ↔ JSON, de dónde salió, y qué anotación le da permiso de actuar sobre tus retornos?
2. ¿Por qué un campo `String` ausente llega null pero un `int` ausente llega 0?
3. ¿Qué diferencia al 400 del JSON roto del 415 del Experimento 3?

*Decidí y justificá:*
4. Un compañero dice: "el POST anda, devuelve 200, listo". Convencelo de que 200 está *mal* para su alta — ¿qué comunica el código de estado y a quién le importa?
5. Diseñá el DTO de entrada para "registrar una mascota": ¿pondrías un campo `id`? ¿Y `cantidadDeVacunas` como `int` o como `Integer`? Justificá las dos con lo aprendido hoy.
6. Tu `/presupuestos` acepta `cantidad: -5` y devuelve un total negativo con 201. ¿Está bien? ¿De quién debería ser la responsabilidad de rechazarlo — Jackson, el controller, u otra pieza? (Defendé con el recorrido en la mano.)

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Abrí en el `sales-service` el package `dtos/producto/` y leé `ProductoCreateRequest` y `ProductoResponse` completos: a esta altura son transparentes — records, entrada sin id, salida con id. Después mirá en `ProductoController` el método `create`: `@PostMapping` + `@ResponseStatus(CREATED)` + `@RequestBody` — **es tu endpoint de turnos con traje de trabajo**, línea por línea. Releé del recorrido **P6 §5 completo** (los DTO y la frontera — ahora que tipeaste records, el "por qué la entidad no cruza jamás" te va a caer distinto) y el comentario del `@RequestBody` en §6, con tu 👀 del `@JsonProperty`.

## ▶️ Próximo paso

Pará un segundo y mirá tu `/presupuestos` con ojo crítico: hay un **cálculo de negocio adentro del controller**. Y en la pregunta 6 del checkpoint quedó flotando quién debería validar. Las dos incomodidades tienen la misma respuesta — la pieza que orquesta, valida y decide, y que tu controller está usurpando sin permiso. Decime **"arranquemos etapa 4"** y ponemos el service en el medio: el controller vuelve a su rol de frontera, y tu DI del proyecto-1 entra a la cancha.

---

**FIN DE LA ETAPA 3**
