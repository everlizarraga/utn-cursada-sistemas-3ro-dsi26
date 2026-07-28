# 🌱 Proyecto 0 (clase 04) — Etapa 2B: El segundo canal — query params (y los gemelos falsos)

> **⭐ Extensión pedida por vos** — el proyecto de la clase no usa nada de esto (todavía). Entra por decisión tuya: completar los canales de entrada antes del body, y llevarte puntos para el TP.
>
> **Objetivo:** dominar el segundo canal por donde entra información en una request — el **query string** (`?especie=perro&limite=10`) — con sus superpoderes propios: parámetros opcionales y valores por defecto. Y de paso, vacunarte contra los **gemelos falsos** que el autocompletado te quiso vender.
>
> **El momento clave:** cuando descubras que el query puede hacer algo que la ruta NO puede — y entiendas por qué eso define cuándo usar cada canal.
>
> **Pre-requisito:** Etapa 2 completa.
>
> **Tiempo estimado:** 30 minutos.

---

## 🧭 Mapa de esta etapa

1. El query string: anatomía y primer `@RequestParam`.
2. El superpoder: opcionales, `defaultValue` y el atributo privilegiado.
3. Experimento 1: el requerido ausente.
4. Los gemelos falsos (⚠️ la vacuna).
5. Tu turno: un buscador con filtros, solo.
6. Criterios + checkpoint + registro + 🔗.

---

## 🛠️ Parte 1: El query string y tu primer `@RequestParam`

Anatomía primero. En `GET /veterinaria/buscar?texto=firulais&limite=5`:

```
/veterinaria/buscar        ?      texto=firulais   &   limite=5
└── la RUTA (identifica    └─ acá  └── clave=valor  └── separador
    QUÉ recurso/acción)       empieza                   entre pares
                              el QUERY
```

La ruta dice *qué*; el query trae *cómo*: filtros, opciones, cantidades. Agregá a tu patio de experimentos:

```java
@GetMapping("/buscar")
public String buscar(@RequestParam String texto) {
    return "Buscando: " + texto;
}
```

**Predicción, por escrito:** ¿qué devuelven *(a)* `GET /veterinaria/buscar?texto=firulais`, *(b)* `GET /veterinaria/buscar?texto=firulais gato` (con espacio — probalo desde Postman y mirá en qué se convierte la URL), *(c)* `GET /veterinaria/buscar` a secas?

Probá *(a)* y *(b)*, dejá *(c)* para el experimento. *(a)*: 200, "Buscando: firulais". *(b)*: 200… y mirá la URL que Postman mandó de verdad: el espacio viajó como **`%20`** (o `+`). Eso es **URL encoding**: en una URL solo viajan caracteres "seguros"; el resto se codifica y Spring te lo **decodifica solo** — tu parámetro llegó con el espacio de vuelta. Cuando armes URLs a mano y algo "se corte en el espacio", acordate de esta línea.

**La revelación (corta, porque ya casi todo lo sabías):** `@RequestParam` matchea **por nombre** con la clave del query (`texto` ↔ `?texto=`), convierte tipos igual que `@PathVariable` (un `@RequestParam int limite` rechaza `?limite=abc` con el 400 de siempre), y el nombre explícito funciona igual: `@RequestParam("texto") String t`. Todo tu entrenamiento de la Etapa 2, transferido gratis.

## 🛠️ Parte 2: El superpoder — opcionales y `defaultValue`

Acá está lo que la ruta **no puede hacer**. Acordate: `/eco` sin palabra era 404 — un hueco de ruta es obligatorio *por construcción*, no existe "segmento opcional". El query sí sabe de opcionales:

```java
@GetMapping("/buscar")
public String buscar(@RequestParam String texto,
                     @RequestParam(defaultValue = "3") int limite) {
    return "Buscando: " + texto + " (máximo " + limite + " resultados)";
}
```

**Predicción:** ¿qué devuelven `?texto=gato&limite=10`, y `?texto=gato` sin límite?

Probá: la primera usa tu 10; la segunda **no falla** — usa el 3 del `defaultValue`. El parámetro se volvió opcional-con-respaldo. La variante sin respaldo también existe: `@RequestParam(required = false) String texto` — si no viene, llega `null` y tu código decide. Dos herramientas: **`defaultValue` cuando hay un valor sensato de fábrica; `required = false` cuando la ausencia significa algo** (por ejemplo, "sin filtro").

Y con esto, la regla de diseño de la etapa, para anotar:

> **La ruta identifica recursos (partes obligatorias de la identidad); el query trae filtros, opciones y paginación (cosas que pueden faltar).** El clásico de los clásicos que vas a ver en mil APIs: `GET /productos?page=0&size=10`.

**💎 La perla de la combinación.** ¿Y si querés nombre explícito Y default a la vez? `@RequestParam("limite", defaultValue = "3")` **ni compila**. La forma correcta:

```java
@RequestParam(name = "limite", defaultValue = "3") int limit
```

La regla detrás es de **Java**, no de Spring — y vale para TODAS las anotaciones que usaste y las que vienen: una anotación puede tener varios atributos, y uno puede llamarse `value`, que es *el privilegiado*: cuando pasás **un solo dato**, podés omitir su nombre. Lo venías usando sin saberlo:

```java
@GetMapping("/ping")        // atajo de → @GetMapping(value = "/ping")
@PathVariable("id")         // atajo de → @PathVariable(value = "id")
@RequestParam("limite")     // atajo de → @RequestParam(value = "limite")
```

El atajo vale **solo mientras sea el único atributo**: apenas sumás un segundo, Java te obliga a nombrarlos a todos — ya no tolera la ambigüedad. *(En `@RequestParam`, `name` y `value` son alias entre sí — vas a ver ambos en código ajeno.)*

Y un detalle fino que quizás te hizo ruido: `defaultValue = "3"` va **entre comillas aunque tu parámetro sea `int`**. No es descuido — los defaults de anotaciones se declaran siempre como String (limitación de las anotaciones de Java) y Spring los pasa por la misma conversión de tipos de siempre antes de entregártelos. Experimentito de dos minutos: poné `defaultValue = "abc"` con tu `int`… y **predecí en QUÉ requests explota el 400** (pista: no en todas).

## 🧨 Parte 3: Experimento 1 — el requerido ausente

Tu `texto` quedó sin `defaultValue` ni `required=false`: es **requerido**. **Predicción:** `GET /veterinaria/buscar` pelado — ¿404, 400, 500, o entra con `texto` en null?

Probá: **400 Bad Request** — y tu método nunca corrió (testigo, si dudás). En consola, el espécimen nuevo para tu colección: `MissingServletRequestParameterException` — *"Required request parameter 'texto' ... is not present"*. Fijate el patrón que ya se consolidó en tres etapas: **la ruta matcheó, el contrato de entrada no se cumplió, Spring rechaza con 400 por vos, antes de tu código.** Ruta rota = 404; contrato de entrada roto = 400; y tu código todavía ni se enteró.

## ⚠️ Parte 4: Los gemelos falsos — la vacuna

Esto salió de tu propio autocompletado, así que va con nombre y apellido. Cuando tipeaste, el IDE te ofreció **`@PathParam`**. Parece un pariente de `@PathVariable`. **No lo es: es de otro framework.**

Existe *otro* estándar de APIs REST en Java: **JAX-RS** (Jakarta REST — lo usan Jersey, Quarkus y otros). Tiene su espejo completo de anotaciones, con nombres parecidísimos:

| Mundo Spring MVC (el tuyo) | Mundo JAX-RS (el otro) |
|---|---|
| `@GetMapping("/x")` | `@GET` + `@Path("/x")` |
| `@PathVariable` | `@PathParam` |
| `@RequestParam` | `@QueryParam` |
| import `org.springframework.web...` | import `jakarta.ws.rs...` |

¿Qué pasa si usás el gemelo equivocado en tu controller de Spring? Dos escenarios, y ninguno bueno: si la biblioteca de JAX-RS **no está** en tu proyecto, el import no compila — ruido, pero honesto. Si **está** (llega de contrabando por alguna dependencia transitiva), **compila perfecto y Spring la ignora olímpicamente**: tu parámetro no se llena, y te quedás mirando un código "bien escrito que no anda". El bug más caro es el silencioso.

**La vacuna es un reflejo, y vale más que la anotación:** cuando algo anotado "está perfecto y no funciona", **mirá el import**. `org.springframework.*` = tu mundo. `jakarta.ws.rs.*` = te colaste en el framework del vecino. Este reflejo te va a salvar con código de Stack Overflow, con proyectos ajenos, y con respuestas de IA que mezclan frameworks sin avisar.

👀 *Ya que estamos con canales de entrada, el que falta conocer de vista: `@RequestHeader` lee **headers** de la request (`@RequestHeader("Authorization") String token`) — lo vas a necesitar de verdad cuando lleguen tokens y sesiones, más adelante en la materia. Hoy: saber que existe.*

## ✍️ Parte 5: Tu turno — un buscador con filtros, solo

Un endpoint `GET /veterinaria/agenda` que simule buscar turnos: parámetro **requerido** `dia` (String), opcional `especie` (si no viene, significa "todas"), y opcional `limite` (numérico, default 5). Devolvé una frase que refleje los tres. Elegí vos la herramienta correcta para cada opcional — y prepará una justificación de por qué.

**Resultado esperado:** `?dia=lunes&especie=gato&limite=2` → 200 con los tres reflejados · `?dia=lunes` → 200, "todas las especies", límite 5 · sin `dia` → **400** (y sabés leer cuál excepción) · `?dia=lunes&limite=abc` → **400** (¡y también sabés cuál!).

## ✅ Criterios de "Etapa 2B completa"

- [ ] `/buscar` con requerido + default funcionando, predicciones escritas y contrastadas.
- [ ] Viste el `%20` viajar y volver decodificado.
- [ ] El 400 del requerido ausente, provocado y leído en consola.
- [ ] Podés recitar la tabla de gemelos falsos y el reflejo del import.
- [ ] Sabés combinar nombre explícito + `defaultValue` — y por qué el atajo de `value` deja de valer.
- [ ] Tu `/agenda` anda con sus cuatro resultados esperados.

## ✅ Checkpoint

*Recall:*
1. ¿Qué puede hacer un `@RequestParam` que un `@PathVariable` no puede jamás, y por qué?
2. ¿Cuándo usás `defaultValue` y cuándo `required = false`?
3. ¿De qué framework es `@QueryParam`, y qué le pasa a tu endpoint de Spring si lo usás?
4. ¿Por qué `@RequestParam("limite", defaultValue = "3")` no compila, y cuál es la forma correcta? ¿Qué atributo es "el privilegiado" y hasta cuándo podés omitir su nombre?

*Decidí y justificá:*
5. Diseñá la URL para "listar las mascotas del propietario 4, solo perros, de a 10 por página": ¿qué va en la ruta y qué en el query? Justificá cada decisión con la regla de la etapa.
6. Un compañero tiene `@PathParam Long id` en su controller Spring: compila, corre, y el id "llega siempre null o explota raro". Contale el diagnóstico completo — incluido por qué compiló.
7. Ya conocés cuatro finales para una request: 404, 400 por conversión, 400 por parámetro ausente, y "Ambiguous mapping" al arrancar. Armá tu tabla mental: momento en que ocurre + qué contrato se rompió en cada uno.

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Esta vez la conexión es **inversa**, y es un ejercicio de lectura fina: abrí el `sales-service` y **verificá que nada de esto está** (buscá `RequestParam` en el proyecto: cero resultados). No es descuido — es decisión: en esa API todo viaja por ruta o por body, porque sus casos de uso no piden filtros ni opciones. **Leer qué NO usa un proyecto, y entender por qué, también es leer el proyecto.** El día que el profe agregue un listado con filtros, vas a estar dos etapas adelante.

## ▶️ Próximo paso

Canal 1 (ruta) ✓ · canal 2 (query) ✓ — queda el canal grande: el **body**. Un alta de mascota no viaja en una URL: viaja como JSON adentro de la request. Decime **"arranquemos etapa 3"** y llegan el POST, los records como DTO, y la mutación del `Content-Type` que vengo anunciando hace dos etapas.

---

**FIN DE LA ETAPA 2B**
