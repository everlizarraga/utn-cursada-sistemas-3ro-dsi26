# 🧩 Complemento — Clase 03: Servicio de Ventas (SmartLife)

**Unidad:** `clase03` — Modelado de Dominio en Objetos · Arquitectura de Software (08/04/2026)
Complementa al apunte maestro: resuelve su checkpoint. *(Esta sesión no dejó dudas de contenido para destilar, así que no hay Parte A — el complemento son las respuestas del checkpoint, y eso es válido.)*

---

## Respuestas del checkpoint del maestro

*(Formato examen: al grano, con la terminología de la materia. La primera oración ya responde.)*

**1. ¿Qué significa "diseñar" en esta materia, y por qué el apunte muestra alternativas descartadas?**
Diseñar es plantear varias alternativas, evaluar sus ventajas y desventajas, y elegir una justificando el porqué. Las descartadas se muestran porque la justificación de *por qué no* las otras es parte de lo que se evalúa: la materia mide la decisión y su fundamento, no la memorización de la solución final.

**2. El Comercio se modeló solo con un `id`. ¿Con qué criterio, y qué tiene que ver con la orientación a servicios?**
Con el criterio de modelar lo mínimo necesario. No hay caso de uso de gestión de comercios y, en una arquitectura orientada a servicios, ese dominio probablemente lo administra otro servicio; con el `id` alcanza para referenciarlo, y modelar de más acoplaría este servicio a datos que no le pertenecen (YAGNI).

**3. Enunciá la regla de tipificación. ¿Por qué `Producto` no se separa pero `Impuesto` sí?**
Se hereda (o se separa en clases) solo si hay comportamiento distinto —código distinto—, nunca por atributos compartidos ni por tipificación. `Producto` no se separa porque todos los productos calculan igual (delegan en su tipo); `Impuesto` sí, porque IVA, EO y EI se calculan de forma distinta.

**4. ¿Por qué `Hogar` y `Electrónico` son instancias de `TipoProducto` y no subclases? ¿Qué gana ese diseño?**
Porque los tipos no tienen comportamiento distinto entre sí: solo cambia qué impuestos llevan (dato), no cómo hacen algo. Como instancias de `TipoProducto`, el diseño es extensible en tiempo de ejecución: un tipo nuevo es una instancia nueva (dable de alta desde una UI), sin escribir ni recompilar código.

**5. `Impuesto` es una interfaz y no una clase abstracta. Las dos razones.**
Primera: en Java se implementan muchas interfaces pero se hereda de una sola clase, así que la interfaz no consume la herencia. Segunda: no hay estado ni comportamiento común genuino "arriba" que una clase abstracta deba factorizar; como solo comparten un contrato, la interfaz es la herramienta justa.

**6. ¿Por qué `calcular` recibe el `Producto` entero y no solo el precio base? ¿Qué se gana y qué se paga?**
Para poder contemplar reglas futuras que dependan de otros atributos del producto (por ejemplo, exenciones por tipo o por marca). Se gana flexibilidad/extensibilidad y se paga con acoplamiento de `Impuesto` a `Producto` (pasar solo el precio base desacoplaría, pero perdería esa capacidad).

**7. ¿Por qué los parámetros de los impuestos son `static`? ¿Qué problema genera en los tests y cómo se resuelve?**
Son `static` porque son atributos de clase: el valor (p. ej. el 21 % del IVA) es el mismo para todo el sistema, sin importar cuántas instancias existan, y se puede cambiar en un único lugar. El problema es que ese estado es compartido y persiste entre tests; se resuelve reseteándolos en `@BeforeEach` para que cada test arranque de un estado conocido.

**8. Explicá el patrón Observer en este caso: qué desacopla y cómo sumar un tercer interesado.**
El Observer desacopla al Comercio (sujeto) de los interesados: el Comercio mantiene una `List<ObservadorVenta>` y, al registrar una venta, notifica a cada uno mediante la interfaz `ObservadorVenta`, sin conocer sus clases concretas. Para sumar un tercer interesado se crea una clase nueva que implemente `ObservadorVenta` y se la agrega a la lista, sin tocar el Comercio.

**9. ¿Por qué `Venta` tiene `ItemVenta` en lugar de una lista de `Producto`?**
Porque una venta puede incluir varias unidades del mismo producto, y `ItemVenta` asocia producto + cantidad (como el renglón de una factura). Con una lista de `Producto` directa se perdería la cantidad.

**10. Notificar a los observadores, ¿en paralelo o secuencial? Dos trade-offs de cada opción.**
El código lo hace secuencial. Secuencial: a favor, es simple y garantiza orden/prioridad; en contra, si un observador tarda o falla, afecta a los siguientes. Paralelo: a favor, es más rápido con observadores lentos e independientes; en contra, Java no es multihilo por defecto y aparecen concurrencia y pérdida de orden. La elección depende de si importan el orden y el manejo de fallos.

---

**FIN DEL COMPLEMENTO — CLASE 03**
