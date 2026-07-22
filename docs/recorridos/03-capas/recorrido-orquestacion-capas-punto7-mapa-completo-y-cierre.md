# 🧭 RECORRIDO — Orquestación de CU en Capas — PUNTO 7: El Mapa Completo y el Cierre

**Unidad:** clase04 · **Densidad global:** 🟡 (consolidación — acá no hay conceptos nuevos: hay orden)

---

## Sobre este documento

**Qué cubre:** el viaje completo de una request dibujado de punta a punta (camino feliz y camino de error); las reglas-reflejo de la unidad consolidadas; qué implica todo esto para el trabajo práctico; y el cierre del recorrido — qué hacer ahora y qué viene después.

**Qué NO cubre:** contenido nuevo. Si algo de este punto te resulta desconocido, es la señal de qué punto anterior releer.

## De dónde venís

De los Puntos 1 a 6 — todos. Este punto los asume completos.

---

## 1. El viaje completo de una request 🔴

Todo lo que el recorrido construyó, en un solo dibujo. El caso: `POST /sales-service/productos` con su JSON.

```
 MUNDO EXTERIOR
 ───────────────
   POST /sales-service/productos
   body: { "comercioId":1, "tipoProductoId":2, "precioBase":100, ... }
        │
        ▼
 ┌─ PRESENTACIÓN ────────────────────────────────────────────────────┐
 │  ProductoController (@RestController)                             │
 │   · matchea ruta + verbo (@PostMapping sobre la ruta padre)       │
 │   · traduce: JSON del body ──► ProductoCreateRequest (DTO)        │
 │   · delega: productoService.create(request)  ── conoce SOLO       │
 │             la interfaz del service                               │
 └──────┬────────────────────────────────────────────────────────────┘
        ▼
 ┌─ DOMINIO intermedio ──────────────────────────────────────────────┐
 │  ProductoServiceImpl (@Service — lo construyó el framework,       │
 │   con los repos inyectados por constructor)                       │
 │   1· valida la entrada (reglas del FLUJO)      ── BusinessExc. ─┐ │
 │   2· resuelve ids → objetos vía repos          ── ResourceNot ──┤ │
 │   3· new Producto(...)   ← el service instancia el dominio      │ │
 │   4· comercio.agregarProducto(producto)                         │ │
 │   5· productoRepository.save(producto)                          │ │
 │   6· entidad ──► ProductoResponse (DTO de salida)               │ │
 └──────┬───────────────────────────┬──────────────────────────────┼─┘
        ▼                           ▼                              │
 ┌─ DOMINIO puro ─────┐   ┌─ DATOS ─────────────────────┐          │
 │ Producto, Comercio │◄──│ InMemoryProductoRepository  │          │
 │ (reglas de NEGOCIO;│   │ (@Repository) — asigna id,  │          │
 │  no conocen a nadie│   │ guarda en su lista; conoce  │          │
 │  de este dibujo)   │   │ al dominio, no al revés     │          │
 └────────────────────┘   └─────────────────────────────┘          │
        │                                                          │
        ▼  camino feliz                          camino de error   ▼
 el DTO vuelve al controller;            la excepción sube sin que nadie
 el framework lo serializa a JSON        la atrape ──► GlobalException-
        │                                Handler (@RestControllerAdvice)
        ▼                                la traduce a código + ErrorResponse
   HTTP 201 Created                             │
   { "id": 1, "comercioId": 1, ... }            ▼
                                     HTTP 404 / 400 / 409 / 500
                                     { "error": "...", "message": "...",
                                       "timestamp": "..." }
```

Si podés reconstruir este dibujo de memoria — con los dos caminos — la unidad es tuya.

> **Para el parcial, si te preguntan** *"describí qué ocurre desde que llega una petición HTTP hasta que sale la respuesta"*:
> El controller recibe la request (él expone el endpoint y conoce el protocolo), traduce el body JSON a un DTO de entrada y delega en el service por su interfaz. El service orquesta el flujo del caso de uso: valida la entrada, resuelve referencias contra los repositorios, instancia y configura las entidades de dominio, les delega las reglas de negocio, y persiste vía repositorio. Devuelve un DTO de salida, que el controller (mediante el framework) serializa a JSON con su código de estado. Si algo falla, el service lanza una excepción propia que el manejador global traduce al código HTTP correspondiente con un cuerpo de error uniforme.

## 2. Las reglas-reflejo de la unidad 🔴

Todo lo "anotá esto" del recorrido, junto. Son las respuestas que conviene tener automatizadas — no por memoria, sino porque a esta altura cada una tiene su porqué atrás (y si alguna no lo tiene, ahí está el link al punto):

1. **Un caso de uso no es un método: se desglosa** en varias clases, repartidas entre capas, cada una con su responsabilidad. *(P2)*
2. **Las capas superiores usan a las inferiores — nunca al revés, nunca salteando.** El conocimiento apunta hacia abajo. *(P1)*
3. **El árbol de conocimiento:** entidades no conocen a nadie · repos conocen entidades · services conocen entidades + repos + otros services · controllers conocen solo services (y no a otros controllers). *(P4)*
4. **Los services focalizan las reglas del flujo; las entidades, las reglas del negocio.** *(P4)*
5. **Las entidades de dominio las instancian los services.** *(P4)*
6. **A los services, repos y controllers los instancia el framework** — declarás con anotaciones, él construye e inyecta por constructor. *(P5)*
7. **Entre capas se conocen interfaces, no implementaciones** — por eso el par interfaz + Impl en services y repos. *(P5)*
8. **La entidad no cruza la frontera: entra y sale en DTO.** Lo que no debe salir no está en el DTO de salida; lo que no debe entrar no está en el de entrada. *(P6)*
9. **Los permisos son del rol del usuario, no de la clase del actor** — Usuario/Rol/Permiso por composición, jamás herencia; el chequeo, en el service. *(P3-P4)*
10. **El service lanza; el manejador global traduce.** Excepciones propias no chequeadas → códigos HTTP correctos con error uniforme. *(P6)*

## 3. Qué implica para el trabajo práctico 🟡

Indicaciones operativas que la unidad dejó sobre el TPA — importan porque marcan **el ritmo de implementación por capas**:

- **Para la primera entrega NO se implementa lo que este recorrido mostró de Spring.** Nada de services, repositorios ni controllers reales: se implementa **la capa de dominio** del proyecto (las entidades con sus reglas) — más **un único controller trivial**: un endpoint GET que devuelve un mensaje fijo (un "hola desde el servicio"), solo para probar que el proyecto expone algo. El resto de las capas llega en entregas posteriores, con guía en cada una.
- **Lo que haría el service, por ahora lo hacen los tests.** Los flujos (instanciar, asociar, verificar) se ejercitan desde tests unitarios — que, como viste, son services artesanales. Cuando la capa de services entre en juego, ese paso a paso migra ahí.
- Las piezas con dependencia de afuera (por ejemplo, un importador de archivos) se modelan como **clases de dominio que devuelven objetos** — quién las llama (un service) y dónde se guarda lo que producen (un repo) se engancha en entregas siguientes; mientras tanto, se prueban desde los tests (y son candidatas ideales para el mockeo de la lectura previa).
- **La estructura del proyecto es multimódulo** (un proyecto padre con módulos adentro — el servicio como módulo, más una librería común preparada para código compartido entre servicios futuros), siguiendo el template provisto. El servicio de ventas que recorriste está armado exactamente así.
- Regla general que se desprende de todo: **no adelantarse**. Cada entrega indica qué capa toca; implementar de más es pagar complejidad que nadie pidió todavía.

## 4. El cierre 🟡

Mirá el camino andado. El recorrido arrancó con tres círculos concéntricos y una pregunta incómoda — *¿qué haría el método `verCalificaciones()`?* — y terminó con vos leyendo un `@RestControllerAdvice` sin pestañear. En el medio: el antipatrón desarmado, los permisos con casa propia, cuatro tipos de clase con su árbol de reglas, un framework que construye todo, y un proyecto real funcionando, probado por dos vías.

Los temas que la unidad dejó explícitamente para más adelante en la materia — la persistencia en bases de datos reales, la capa de presentación gráfica, las sesiones y otras casas posibles para el chequeo de permisos, los patrones de interacción — van a llegar a su tiempo, y van a caer sobre esta estructura: todo lo que viene se enchufa en alguna de estas capas.

**Qué hacer ahora, en este orden:**

1. **Terminá los checkpoints pendientes** de los siete puntos — por escrito, en formato examen (la primera oración responde). Los que no salgan son tu lista de relectura; las dudas van al chat, y de ahí sale el complemento del recorrido con las respuestas modelo.
2. **Ensuciate las manos con el proyecto:** levantalo, importá la colección de peticiones, jugá — creá productos, forzá los 404 y los 400, registrá una venta, corré los tests. Después tocá algo: agregá un log, cambiá una validación, rompé algo a propósito y mirá qué responde la API. Este contenido se termina de fijar tecleando.
3. **Seguí el flujo normal de la unidad:** el apunte maestro (con la cobertura completa de siempre) y, detrás, el resumen. Si el recorrido hizo su trabajo, el maestro se va a sentir como volver a ver una película que ya entendiste — notando detalles nuevos, sin perderte en la trama.

---

## ✅ Checkpoint del Punto 7 (integrador)

*El examen de fuego: preguntas que cruzan puntos. Respondé sin releer nada.*

1. Diseñá en papel la orquestación del caso de uso *"consultar los precios de los productos de un comercio"*: qué clases intervienen, en qué capa vive cada una, quién conoce a quién, y qué DTO entra y sale. (No hace falta código: nombres y flechas.)
2. Un requerimiento nuevo: *"los administradores de SmartLife también pueden dar de alta productos en cualquier comercio"*. Enumerá TODO lo que cambia en el sistema… y todo lo que no. Justificá por qué la lista de "no cambia" es tan larga.
3. Tu compañero de TP escribió un controller que: valida que el precio sea positivo, instancia el `Producto`, lo agrega al comercio y llama al repo para guardarlo. Funciona perfecto. Listale cada regla de la unidad que violó y a qué clase se muda cada pedazo.
4. Defendé esta afirmación o refutala: *"el dominio del servicio de ventas de la clase 03 no tuvo que modificarse para exponerlo como API"*. ¿Qué propiedad del diseño lo hizo posible (o lo impidió)?
5. ¿En cuántos lugares distintos del sistema hay "validaciones", qué tipo le corresponde a cada lugar, y qué pasa si las mezclás? Un ejemplo concreto de cada una con el caso de la venta.
6. Mañana la persistencia pasa de listas en memoria a una base de datos real. Recorré el árbol de clases del proyecto y marcá exactamente qué archivos se tocan y cuáles no — y qué decisiones de esta unidad hicieron posible esa cirugía tan chica.

---

**FIN DEL PUNTO 7 — FIN DEL RECORRIDO**

*Siguiente estación: tus checkpoints + dudas por chat → complemento del recorrido → apunte maestro de la unidad.*
