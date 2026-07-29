# 🌱 Proyecto 0 (clase 04) — Etapa 3: El canal grande — el body y los records

> **Objetivo:** que tu API hable JSON en los dos sentidos: **devolver objetos** (y ver la mutación del `Content-Type`) y **recibir objetos** por el body con tu primer POST — con records haciendo de DTO, como en el proyecto de la clase, y su gemelo-clase construido al lado para comparar en vivo.
>
> **El momento clave:** descubrir que en tu stack **el estilo de DTO cambia la semántica de la ausencia** — un campo que falta no significa lo mismo en un record que en una clase. Eso convierte una elección "de gusto" en una decisión de contrato.
>
> **Pre-requisito:** Etapa 2B completa.
>
> **Tiempo estimado:** 60-70 minutos.
>
> *(v4 — reescrita con dos correcciones que salieron de TU teclado: el trato de primitivos ausentes en este stack, y el constructor implícito de Java. Las guías también se testean.)*

---

## 🗂️ Dónde estamos — los archivos de esta etapa

```
clase-04-prac-01-proy-00/                     ← tu módulo (Vía B; en Vía A es la raíz del proyecto)
└── src/main/
    ├── java/.../veterinaria/                 ← "..." = TU raíz de packages (ver ⚠️ abajo)
    │   ├── VeterinariaApplication.java          (ya existía — hoy no se toca)
    │   ├── controllers/
    │   │   └── PingController.java              (SE TOCA: hoy le sumás 4 endpoints)
    │   └── dtos/                                (📦 NUEVO package)
    │       ├── InfoVeterinaria.java             (NUEVO — Parte 1)
    │       ├── TurnoRequest.java                (NUEVO — Parte 4; evoluciona en la 5)
    │       └── TurnoRequestClass.java           (NUEVO — Experimento 4, el gemelo)
    └── resources/
        └── application.yaml                     (ya existía — hoy no se toca)
```

> ⚠️ **Traducción de raíz — una vez y para siempre:** la guía escribe la raíz de packages de la Vía A (`com.practica.veterinaria`). Si sos Vía B, tu raíz es la tuya (p. ej. `io.github.everlizarraga.clase04prac01proy00`). **Donde la guía diga `com.practica.veterinaria` — o `...` en un árbol — leé TU raíz.** Todo lo que cuelga de ahí es idéntico en las dos vías.

---

## 🧭 Mapa de esta etapa

1. La mutación: tu primer objeto devuelto.
2. La revelación: Jackson, el traductor invisible.
3. Records en 5 líneas + 📐 la gramática completa de las anotaciones.
4. El canal 3: POST + `@RequestBody` (con sus fichas 🎛️).
5. Experimento 1: la semántica de la ausencia (acá está la carne).
6. Experimento 2: JSON roto.
7. Experimento 3: el Content-Type de la request.
8. Experimento 4: el gemelo vivo — record vs clase, lado a lado.
9. Tu turno: un presupuesto, solo.
10. Criterios + checkpoint + registro + 🔗.

---

## 🛠️ Parte 1: La mutación

Tu primer record:

```java
// 📁 src/main/java/.../veterinaria/dtos/InfoVeterinaria.java   (archivo NUEVO)
package com.practica.veterinaria.dtos;

public record InfoVeterinaria(String nombre, String direccion, boolean atiendeHoy) { }
```

Y un endpoint que lo **devuelva** (convive con tu `/info` viejo, que devolvía String):

```java
// 📁 src/main/java/.../veterinaria/controllers/PingController.java   (agregar DENTRO de la clase)
@GetMapping("/info-completa")
public InfoVeterinaria infoCompleta() {
    return new InfoVeterinaria("Veterinaria Firulais", "Av. Siempreviva 742", true);
}
```

**Predicción, por escrito:** *(a)* ¿qué va a mostrar el body en Postman? *(b)* ¿y el header `Content-Type` de la respuesta? *(c)* ¿escribiste vos algo, en algún lado, que convierta ese objeto a texto?

Probá `GET /veterinaria/info-completa` y mirá **las dos cosas**: el body —

```json
{ "nombre": "Veterinaria Firulais", "direccion": "Av. Siempreviva 742", "atiendeHoy": true }
```

— y el header: **`Content-Type: application/json`**. Mutó solo. Compará con `/info` (String): sigue en `text/plain`. Mismo controller, misma anotación, y **el tipo de retorno decidió el idioma** de la respuesta.

## 🤯 Parte 2: La revelación — Jackson, el traductor invisible

¿Quién convirtió tu objeto a JSON? Nadie que hayas llamado vos. El starter `webmvc` (Etapa 0, la "lista de compras") trajo entre sus paquetes a **Jackson**: la biblioteca que convierte objetos Java ↔ JSON. Y `@RestController` (Etapa 1: *"lo que retornen los métodos va directo al body"*) tenía la segunda mitad de su contrato escondida: **si el retorno es un objeto, Jackson lo serializa a JSON antes de que salga** — campo por campo, con los **nombres de tus componentes como claves**. *Serializar* = objeto → formato transportable; el viaje inverso es *deserializar* (Parte 4).

Por eso el 404 de fábrica de la Etapa 1 ya hablaba JSON: el manejador de errores de Spring devuelve objetos, y Jackson los traduce igual que a los tuyos. El traductor siempre estuvo; recién hoy le das trabajo con material propio.

## 📦 Parte 3: Records en 5 líneas + la gramática completa

Refresco de records, con los detalles que importan al tipearlos: la declaración `record X(...)` **es** la firma del constructor (Java genera constructor, accessors, `equals`, `hashCode`, `toString`) · **inmutable** (sin setters) · accessors **sin `get`** — `info.nombre()`, no `info.getNombre()` (detalle de dedos que reaparece en el Experimento 4) · el record ES el DTO ideal, por eso la cátedra lo eligió.

### 📐 La gramática completa de las anotaciones — de una vez y para siempre

En la 2B aprendiste la regla del atributo privilegiado. Acá van **las tres reglas juntas** — con esto leés cualquier anotación que exista:

1. **El privilegiado:** si el único atributo que pasás es `value`, podés omitir su nombre → `@GetMapping("/ping")`.
2. **Varios atributos → nombrás todos:** `@RequestParam(name = "limite", defaultValue = "3")`. El atajo muere con el segundo atributo.
3. **Atributos-array llevan llaves:** cuando un atributo acepta *varios* valores, se pasan con `{}` → `@GetMapping({"", "/"})` registra DOS rutas. Y la perla escondida: **el `value` de los mappings siempre fue un array** (`String[]`) — tu `@GetMapping("/ping")` de toda la vida es el atajo de array-de-un-elemento, que permite omitir las llaves. Nunca lo supiste porque Java te dejó no saberlo.

Combinadas: `@PostMapping(value = {"/turnos", "/citas"}, consumes = "application/json")` — regla 2 (nombrados) + regla 3 (llaves) + un atributo nuevo que viene ahora.

*(Y la cuarta regla, que aprendiste ayer a los golpes: **qué atributos existen y qué hacen es de CADA anotación** — nombre igual no garantiza semántica igual. La ficha 🎛️ de cada anotación nueva existe para eso.)*

## 🛠️ Parte 4: El canal 3 — POST + `@RequestBody`

El caso de juguete: solicitar un turno. El DTO de entrada:

```java
// 📁 src/main/java/.../veterinaria/dtos/TurnoRequest.java   (archivo NUEVO)
package com.practica.veterinaria.dtos;

public record TurnoRequest(String mascota, String dia, int duracionMinutos) { }
// (sí, `int` a propósito — el Experimento 1 le va a sacar chispas)
```

Y el endpoint:

```java
// 📁 src/main/java/.../veterinaria/controllers/PingController.java   (agregar DENTRO de la clase)
@PostMapping("/turnos")
@ResponseStatus(HttpStatus.CREATED)
public String solicitarTurno(@RequestBody TurnoRequest request) {
    return "Turno para " + request.mascota() + " el " + request.dia()
         + " (" + request.duracionMinutos() + " min)";
}
```

Fijate el corazón del asunto, porque es LA idea del framework: **el tipo del parámetro ES el contrato de deserialización**. Vos declarás la forma destino (`TurnoRequest`); Jackson fabrica el objeto. No llamás a nadie, no parseás nada — declarás, y el framework cumple. Es el mismo principio de los beans (declarás `@Service`, él construye) y de la conversión de rutas (declarás `Long`, él convierte): **declarativo sobre imperativo**, en todos lados.

Las piezas, con sus fichas:

**`@PostMapping`** — mismo clan que `@GetMapping`, verbo POST: el de *crear/enviar*, con la información en el body.
> 🎛️ **Ficha (vale para todo el clan @GetMapping/@PostMapping/@PutMapping/...):** `value`/`path` (alias; `String[]` — una ruta o varias con `{}`) · `consumes` (qué `Content-Type` de entrada acepta — **el mecanismo detrás del 415** que vas a ver en el Experimento 3) · `produces` (qué `Content-Type` promete devolver) · `params` / `headers` (rutear según presencia de un query param o un header — solo reconocer). Combo típico: `@PostMapping(value = "/turnos", consumes = "application/json")`.

**`@ResponseStatus(HttpStatus.CREATED)`** — pisa el 200 por defecto: un POST que creó responde **201 Created**. El código de estado es parte del contrato.
> 🎛️ **Ficha:** `value`/`code` (el status, alias entre sí) · `reason` (String, un mensaje — usarlo cambia cómo se arma la respuesta; solo reconocer). Anotación chica.

**`@RequestBody`** — *"el body JSON, deserializámelo en una instancia de este tipo"*: matcheo de claves ↔ componentes **por nombre**, conversión de tipos incluida.
> 🎛️ **Ficha:** un solo atributo útil — `required` (default `true`). `@RequestBody(required = false)` → el body se vuelve opcional y, si no vino, el parámetro llega `null`.

**Probalo en Postman:** método **POST**, URL `/veterinaria/turnos`, **Body → raw → JSON** (al elegir JSON, Postman setea solo el `Content-Type: application/json` de la request — dato para el Experimento 3):

```json
{ "mascota": "Firulais", "dia": "lunes", "duracionMinutos": 30 }
```

**Resultado esperado:** **201 Created** + tu frase con los tres datos. Circuito completo: JSON entró, se volvió objeto, tu código lo usó.

👀 *Si la clave del JSON no coincide con tu componente (`"duracion_minutos"`, estilo snake_case — o kebab, `"duracion-minutos"`, menos común pero existe), el puente es `@JsonProperty` sobre el componente — P6 §6 (el gemelo, del lado del body, de tu hallazgo del `@PathVariable`; y viejo conocido tuyo de la clase 2). Dos precisiones de dirección: **(1)** la convención es que el lado Java quede en **camelCase** (así se nombra en Java) y la anotación cargue con la clave "rara" del mundo exterior — no al revés; **(2)** el puente vale **en ambos sentidos**: la misma anotación renombra la clave también al SERIALIZAR la salida — un response con `@JsonProperty("precio_final")` emite esa clave en su JSON.*
> 🎛️ **Ficha `@JsonProperty`:** `value` (la clave JSON — el privilegiado) · `required` (con records **se hace valer**: si la clave falta, la deserialización falla → 400) · ⚠️ **homónimos inertes:** `defaultValue` y `namespace` **existen pero no hacen lo que sus nombres sugieren** — son metadata informativa, no asignan defaults ni renombran claves. El `defaultValue` que sí funciona es el de `@RequestParam`; acá es decorativo. Cuarta regla de la gramática, en carne viva. Combo real: `@JsonProperty(value = "duracion_minutos", required = true)`.

## 🧨 Parte 5: Experimento 1 — la semántica de la ausencia

**Predicción, por escrito:** mandá el POST *(a)* **sin** `"mascota"`, y *(b)* **sin** `"duracionMinutos"`. ¿Rebotan? ¿Entran? ¿Con qué valores? ¿Los dos igual?

Probá. *(a)* **entra**, 201, con la mascota en `null` — nadie la validó. *(b)* **REBOTA** — un error de la familia deserialización (esperá un **400**; verificá el código que te da y leé el mensaje en consola: habla de *no poder meter null en un `int`*).

¿Qué pasó? Dos mecánicas distintas para dos tipos distintos:

- `String` es objeto → clave ausente = `null`, y Jackson lo acepta: entra sin ruido.
- `int` es **primitivo** → no puede ser null. Y como los records se deserializan **por constructor canónico**, Jackson tiene que pasarle *algo* a ese parámetro… y con la clave ausente, ese algo sería null → **rechazo**. En tu stack (Jackson moderno, el de Boot 4), esto es estricto.

**La consecuencia de diseño, para anotar:**

> **En un record, un componente primitivo ES un "required" implícito.** `int cantidad` = "esto tiene que venir, sí o sí". Si la ausencia debe ser *válida* (y significar algo), el tipo correcto es el wrapper: `Integer` → llega `null` limpio, y tu código decide.

¿Y si además querés un **default** para la ausencia? Jackson no inyecta defaults — la herramienta nativa del record es el **constructor compacto** (perla de Java):

```java
// 📁 dtos/TurnoRequest.java — evolución del record (así queda de acá en adelante):
public record TurnoRequest(String mascota, String dia, Integer duracionMinutos) {
    public TurnoRequest {                       // ← sin paréntesis ni parámetros: "compacto".
        if (duracionMinutos == null) duracionMinutos = 30;   //   Retoca los valores ANTES de
    }                                                        //   que se asignen a los componentes.
}
```

Aplicá la evolución (int → `Integer` + compacto), y verificá: POST sin `"duracionMinutos"` → **201, con 30**. La ausencia pasó de rechazo a default — **por decisión tuya, explícita en el código**, no por piedad silenciosa del framework.

Ahora al revés — un campo **de más**: `"color": "azul"`. **Predicción primero.** Resultado: **ignorado en silencio**, 201. El default de Jackson es tolerante con lo desconocido: lo que no tiene componente, no existe. *(¿Y quién valida que `mascota` venga? Todavía NADIE — la incomodidad tiene dueño y llega en la próxima etapa. Aunque ya conociste un atajo: `@JsonProperty(required = true)` — probalo si querés; y pensá por qué igual no alcanza como estrategia general de validación.)*

## 🧨 Parte 6: Experimento 2 — JSON roto

Mandá el body con la sintaxis rota (sacale una comilla). **Predicción:** ¿400, 500, o entra mutilado?

**400 Bad Request** — el body ni llegó a ser JSON: request mal formada, culpa del cliente. En consola, espécimen **#5**: `HttpMessageNotReadableException`. Variante para predecir: JSON válido pero tipo imposible — `"duracionMinutos": "media hora"` → misma familia, 400. Contrastá con la Parte 5: ausente ≠ inválido — y ahora sabés que "ausente" además significa distinto según el tipo. La distinción vuelve con todo en la etapa del PATCH.

## 🧨 Parte 7: Experimento 3 — el Content-Type de la request

En Postman, cambiá el tipo del body de **JSON** a **Text** (mismo contenido) y mandá. **Predicción:** el contenido es idéntico… ¿pasa algo?

**415 Unsupported Media Type** — espécimen **#6**: el `Content-Type` de la request es la etiqueta que le dice al servidor cómo interpretar el body; con `text/plain`, Spring ni intenta deserializar. El contenido era perfecto; **la etiqueta lo condenó**. (Y ahora sabés dónde vive este mecanismo: el atributo `consumes` de la ficha del mapping.) Volvé a JSON y verificá que el turno volvió.

## 🧨 Parte 8: Experimento 4 — el gemelo vivo

*(Este diseño es tuyo: en vez de convertir ida y vuelta, construimos el gemelo AL LADO y comparamos en vivo. Mejor experimento.)*

**Paso 1 — el gemelo-clase.** Archivo nuevo, nombre distinto (dos tipos con el mismo nombre no conviven en un package — por eso es gemelo y no reemplazo):

```java
// 📁 src/main/java/.../veterinaria/dtos/TurnoRequestClass.java   (archivo NUEVO)
package com.practica.veterinaria.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TurnoRequestClass {
    private String mascota;
    private String dia;
    private Integer duracionMinutos;
}
// (el sabor a mano, sin Lombok, es esto + constructor vacío + cada getter/setter
//  a pulmón: ~20 líneas de ceremonia. También circula @Data, el combo todo-en-uno.)
```

**Paso 2 — la ruta gemela.** En el controller, un `POST /turnos-v2` idéntico al de turnos pero recibiendo `TurnoRequestClass` — y ojo con los dedos: acá los accessors son `getMascota()`, `getDia()`, `getDuracionMinutos()`. El detalle-de-dedos de la Parte 3, pagado en vivo: **cambiar el estilo de DTO cambia la sintaxis de TODOS sus consumidores.**

**Paso 3 — comparación en vivo.** Mandá el mismo JSON a `/turnos` y a `/turnos-v2`: **201 idéntico, comportamiento idéntico**. Evidencia propia de que *el rol importa más que la sintaxis* — Jackson deserializa a los dos.

**Paso 4 — la semántica de la ausencia, segunda vuelta.** Cambiale a la clase el tipo: `Integer duracionMinutos` → `int`. **Predicción:** el record con `int` rechazaba la ausencia… ¿la clase también? Mandá el POST a `/turnos-v2` **sin** `"duracionMinutos"`: entra, **201, con 0 silencioso**. ¿Por qué la diferencia? Mecanismos de fabricación distintos: al record Jackson lo construye **por constructor** (null en un parámetro `int` = rechazo); a la clase la construye vacía y la llena **por setters** — clave ausente = setter jamás llamado = el primitivo queda en su 0 de fábrica. **El estilo de DTO cambia la semántica de la ausencia: record/constructor = estricto; clase/setters = silencioso.** En tu stack, esta es LA razón de fondo para elegir con cuidado. (Volvé la clase a `Integer` al terminar.)

**Paso 5 — el constructor que nadie escribió.** Pregunta: la clase no tiene `@NoArgsConstructor` ni constructor alguno… ¿cómo pudo Jackson construirla vacía? Por el **constructor implícito** de Java: toda clase sin NINGÚN constructor declarado recibe uno vacío de regalo del compilador. Por eso un `@NoArgsConstructor` acá sería **redundante**. La regla completa: el implícito **muere en cuanto declarás cualquier otro constructor** — ahí sí, si Jackson necesita el vacío y ya no existe, aparece el críptico famoso. Espécimen **#7, de vitrina** (reconocimiento — reproducirlo a propósito en Boot es escurridizo porque Boot trae salvavidas de detección de parámetros): si un día ves *"Cannot construct instance of X (no Creators, like default constructor, exist)"*, ya sabés leerlo — *esa clase se quedó sin camino de construcción usable* — y conocés a los sospechosos: un constructor declarado que mató al implícito, sin dejar vacío ni setters suficientes.

*(El proyecto sigue con records — son la canónica de la cátedra. `TurnoRequestClass` y `/turnos-v2` quedan en el patio como pieza de museo comparativa, y se limpian con el patio cuando lleguen los recursos reales.)*

## ✍️ Parte 9: Tu turno — un presupuesto, solo

Endpoint `POST /veterinaria/presupuestos`: recibe un record con `servicio` (String), `cantidad` (int) y `precioUnitario` (double); **devuelve un record** (no un String — quiero tu JSON de salida) con los tres datos más el `total` calculado. Status correcto para una creación. Vos decidís nombres y ubicaciones — ya conocés las convenciones. Y una decisión consciente: dejaste `cantidad` como `int`… ¿qué contrato acabás de declarar con eso?

**Resultado esperado:** body `{"servicio":"vacuna","cantidad":3,"precioUnitario":1500.0}` → **201**, JSON con `"total": 4500.0` · **sin** `"cantidad"` → **rechazo de la familia deserialización** (¡tu primitivo trabajando de required implícito!) · JSON roto → **400** · body como Text → **415**.

## ✅ Criterios de "Etapa 3 completa"

- [ ] Viste la mutación: `/info` en `text/plain` y `/info-completa` en `application/json`.
- [ ] Tu POST de turnos anda con su 201, y podés explicar quién deserializó y por qué "declarar el tipo" alcanza.
- [ ] Las tres reglas de la gramática de anotaciones, recitables — incluida la de las llaves.
- [ ] Los cuatro experimentos hechos con predicción escrita; especímenes #5 y #6 provocados, #7 en vitrina.
- [ ] El record evolucionó a `Integer` + constructor compacto, y el gemelo-clase demostró la semántica opuesta de la ausencia.
- [ ] Tu `/presupuestos` cumple sus cuatro resultados esperados.

## ✅ Checkpoint

*Recall:*
1. ¿Quién convierte objetos ↔ JSON, de dónde salió, y qué anotación le da permiso de actuar sobre tus retornos?
2. Un `int` ausente: ¿por qué rebota en el record pero entra como 0 en la clase? Nombrá los dos mecanismos de fabricación.
3. Las tres reglas de la gramática de anotaciones — y por qué `@GetMapping("/ping")` es un array disfrazado.
4. ¿Cuándo existe el constructor implícito de Java, cuándo muere, y qué tiene que ver con el espécimen #7?

*Decidí y justificá:*
5. Un compañero dice: "el POST anda, devuelve 200, listo". Convencelo de que 200 está *mal* para su alta.
6. `int` vs `Integer` en un DTO de entrada ya no es una cuestión de gusto: ¿qué contrato declara cada uno en tu stack? Elegí para el campo `cantidad` de un alta de venta, y para `telefonoAlternativo` de un alta de cliente. Justificá ambas.
7. Diseñá el DTO de entrada para "registrar una mascota": ¿pondrías un campo `id`? Justificá con la frontera de datos.
8. Tu `/presupuestos` acepta `cantidad: -5` y devuelve total negativo con 201. ¿De quién debería ser la responsabilidad de rechazarlo — Jackson, el controller, u otra pieza? (Defendé con el recorrido en la mano.)

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic? *(Esta etapa ya te debe dos entradas: el drift de los primitivos y el constructor implícito — las descubriste vos.)*

## 🔗 Conexión con la clase

Abrí en el `sales-service` el package `dtos/producto/` y leé `ProductoCreateRequest` y `ProductoResponse`: transparentes a esta altura — records, entrada sin id, salida con id. Y ahora con ojo nuevo: `precioBase` es `double` **primitivo** — según lo de hoy, ¿qué contrato implícito declara eso en un alta? Después mirá en `ProductoController` el método `create` (`@PostMapping` + `@ResponseStatus(CREATED)` + `@RequestBody`) — tu endpoint de turnos con traje de trabajo. Releé del recorrido **P6 §5 completo** y el comentario del `@RequestBody` en §6.

## ▶️ Próximo paso

Mirá tu `/presupuestos` con ojo crítico: hay un **cálculo de negocio adentro del controller**. Y en la pregunta 8 quedó flotando quién valida. Las dos incomodidades tienen la misma respuesta — la pieza que orquesta, valida y decide, y que tu controller está usurpando. Decime **"arranquemos etapa 4"** y ponemos el service en el medio: el controller vuelve a su rol de frontera, y tu DI del proyecto-1 entra a la cancha.

---

**FIN DE LA ETAPA 3 — v4**
