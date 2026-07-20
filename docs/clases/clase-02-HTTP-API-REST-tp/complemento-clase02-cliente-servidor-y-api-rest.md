# Complemento — Clase 02
## Cliente-Servidor, HTTP, API REST

> **Cómo usarlo.** Contestá cada checkpoint **de memoria** primero, y recién después vení a comparar. Si venís acá directo, no estás estudiando: estás leyendo.
>
> Cada respuesta trae su pregunta arriba, así el archivo se para solo.

---

## Parte A — Aclaraciones destiladas

*(Vacía. La sesión que generó el material no dejó dudas conceptuales sobre el contenido de la clase que valga la pena destilar. Las dos divergencias detectadas —el 401/403 invertido en la grabación, y la definición de REST de la cátedra frente a la de la literatura— ya están resueltas dentro del apunte maestro, con su marca ⚠️, así que repetirlas acá sería duplicar. Si al estudiar surgen dudas y las charlamos, esta sección se completa.)*

---

## Parte B — Respuestas de los checkpoints

---

## ✅ Checkpoint — Parte 1 · Cliente-Servidor

**1. ¿Por qué cliente-servidor no es un protocolo? ¿Qué es, entonces, y qué tres cosas define?**

No es un protocolo porque un protocolo es un idioma concreto de comunicación (HTTP, SMTP, IMAP), y cliente-servidor es un **esquema de conversación** dentro del cual pueden usarse distintos protocolos. Es un **patrón arquitectónico**, y define tres cosas: los **componentes** que participan, la **forma de conexión** entre ellos, y un **vocabulario** común.

**2. Enumerá las cuatro reglas del patrón. ¿Cuál tiene un matiz?**

(1) El cliente siempre inicia la comunicación, nunca el servidor; (2) el servidor escucha en un puerto determinado esperando solicitudes; (3) cada interacción sigue el ciclo solicitud → procesamiento → respuesta; (4) el servidor puede atender múltiples clientes simultáneamente. **La primera tiene el matiz:** las notificaciones push y el streaming existen, pero **requieren salir de HTTP/1.1** (HTTP/2, HTTP/3 o WebSockets). En el modelo clásico sobre HTTP/1.1, el cliente siempre inicia.

**3. Un componente de tu backend le pega a la API de otro sistema. ¿Qué rol cumple?**

Cumple el rol de **cliente** en esa relación. Cliente y servidor son **roles, no identidades**: el mismo componente puede ser servidor del frontend que lo consume y, al mismo tiempo, cliente de la base de datos o de otra API. El criterio es simple: el que expone hace de servidor, el que consume hace de cliente.

**4. ¿Qué diferencia hay entre cliente liviano y cliente pesado? ¿Qué decide cuál es cuál?**

En el **cliente liviano** (cliente pasivo, servidor activo) el servidor concentra la lógica de negocio y genera la vista; el cliente solo la presenta. En el **cliente pesado** (cliente activo, servidor activo) la lógica está distribuida: el cliente tiene la lógica de presentación e interacción. **Lo decide el diseño y las tecnologías elegidas, no el tipo de aplicación**: la misma aplicación puede resolverse de las dos formas.

**5. Nombrá las dos ventajas y las dos desventajas del cliente liviano, con el nombre técnico del atributo de calidad.**

Ventajas: **mantenibilidad** (cambios de funcionalidad centralizados) y **seguridad** (control de acceso centralizado). Desventajas: **eficiencia** (el servidor puede ser un cuello de botella en tiempo de respuesta) y **disponibilidad** (constituye un único punto de falla).

**6. ¿Por qué las desventajas llevan la aclaración "considerando un único servidor"?**

Porque ambas desventajas **existen precisamente porque hay un solo servidor**: el cuello de botella y el punto único de falla desaparecen al agregar servidores. Esa aclaración es la que abre la puerta al resto de la materia, donde la arquitectura se complejiza incorporando redundancia y balanceo de carga.

**7. Agregás servidores para mejorar la disponibilidad. ¿Qué dos cosas empeoran?**

Empeora la **mantenibilidad**, porque aumenta la cantidad de nodos y componentes a administrar; y aumenta el **costo** de infraestructura. Este último no figura en las slides pero está siempre en la ecuación, y su aceptación es una decisión de negocio, no técnica.

**8. Tenés dos servidores de aplicación y una sola base de datos. ¿Eliminaste el punto único de falla?**

No: **lo moviste**. Si la base de datos cae, el sistema cae igual, sin importar cuántos servidores de aplicación tengas. Cualquier componente sin redundancia es, por sí solo, un punto único de falla, y la disponibilidad del sistema es la del componente más frágil de la cadena.

**9. Reconstruí el molde de una respuesta correcta en esta materia.**

*"Pongo [la lógica / el componente / la solución] **acá**, porque priorizo **[atributo de calidad]**, y estoy aceptando pagar con **[otro atributo de calidad]**."* La materia es de aplicación: no pide definiciones, pide que se decida y se justifique la decisión en términos de trade-offs entre atributos de calidad.

---

## ✅ Checkpoint — Parte 2 · HTTP y Arquitectura Web

**1. ¿Qué significa que HTTP sea sincrónico? ¿Qué limitación genera?**

Significa que el cliente envía una solicitud y **queda esperando la respuesta**: la comunicación es bloqueante, de a un pedido por vez. La limitación es que **no permite streaming ni comunicación continua**: para saber si hay novedades en el servidor habría que preguntarle repetidamente (*polling*), porque el servidor no puede avisar por iniciativa propia.

**2. ¿Qué significa *stateless*? Dame el ejemplo del cliente y las facturas.**

Significa que HTTP **no conserva ninguna información de las solicitudes anteriores**. Si se pide `GET /clientes/5` y luego `GET /facturas` esperando las facturas de ese cliente, el servidor no tiene forma de saber de qué cliente se trata: hay que volver a indicárselo, por ejemplo con `GET /clientes/5/facturas`. **Cada solicitud debe llevar toda la información necesaria para ser procesada.**

**3. Un alumno dice: "HTTP no es stateless, porque TCP retransmite los paquetes perdidos". ¿Qué le contestás?**

Que está describiendo un estado distinto. Hay que separar dos cosas: el **estado de la conexión** (retransmisión de paquetes, orden de llegada) lo maneja **TCP**, la capa inferior; el **estado de la operación** (qué se pidió en el request anterior) **no lo maneja nadie**. Cuando se dice que HTTP es *stateless* se habla del segundo.

**4. Enumerá las tres partes de una request y las tres de una response.**

**Request:** línea de inicio (método + URL + versión), headers (metadatos de la solicitud) y body (los datos enviados). **Response:** línea de estado (versión + código + descripción), headers (metadatos de la respuesta) y body (los datos devueltos).

**5. ¿En qué métodos HTTP hay body y en cuáles no? ¿Por qué?**

Hay body en **POST, PUT y PATCH**, y no lo hay en **GET ni DELETE**. La razón es que el body se usa **solo cuando se está enviando información** —al crear o modificar un recurso—; en un GET o un DELETE, la identificación del recurso viaja en la URL y no hay nada más que enviar.

**6. ¿Cuándo usás PUT y cuándo PATCH? (Según la norma.)**

Se usa **PUT** para actualizar el recurso **completo** y **PATCH** para actualizarlo **parcialmente**. En la industria es habitual ver PUT usado para ambos casos, pero el criterio de la materia es el de la norma.

**7. ¿Quién genera los códigos de estado? ¿Qué indica que un código sea 4xx?**

Los genera **siempre el servidor**; el cliente nunca genera un código de estado. Que un código sea 4xx **no indica quién lo emite, sino de quién fue la responsabilidad del error**: señala que la causa está en el cliente (por ejemplo, un 404 al solicitar un recurso inexistente), mientras que un 5xx señala una falla del servidor.

**8. ¿Qué código devuelve un POST exitoso? ¿Y un DELETE exitoso? ¿Por qué son distintos?**

Un POST exitoso devuelve **201 Created**, porque se creó un recurso nuevo. Un DELETE exitoso devuelve **204 No Content**, porque la operación se completó correctamente **y no hay contenido que devolver**. Son distintos porque el código de estado comunica **qué ocurrió**, no solo que salió bien.

**9. Diferencia entre 401 y 403.**

**401 Unauthorized** significa que **falta autenticación**: no se enviaron credenciales o son inválidas ("no sé quién sos"). **403 Forbidden** significa que la autenticación fue correcta pero **falta autorización**: el usuario está identificado pero no tiene permiso sobre ese recurso ("sé quién sos, y no podés").

**10. ¿Por qué los errores 500 se devuelven genéricos, sin detalle?**

Por **seguridad**: dar detalles de lo que falló internamente le permitiría a un atacante entender la estructura del sistema y buscar vulnerabilidades. Un 500 comunica que ocurrió un error del lado del servidor y nada más; el detalle queda en los logs internos, no en la respuesta al cliente.

**11. Tenés dos servicios Spring Boot y el segundo no arranca. ¿Cuál es la causa más probable?**

Que ambos estén intentando escuchar **en el mismo puerto**. En un mismo nodo (misma IP) no pueden correr dos aplicaciones escuchando en el mismo puerto: el segundo falla al levantar con un error de puerto ocupado. Se resuelve cambiándole el puerto a uno de los dos en el `application.yml`.

**12. En el recorrido de una URL, ¿qué tipo de cliente es el navegador que recibe un HTML ya armado?**

Un **cliente liviano** (cliente pasivo, servidor activo). El servidor genera la vista —arma el HTML— y el navegador se limita a renderizarla, sin contener lógica de negocio. Una SPA hecha con React sería el caso opuesto: el servidor envía datos crudos y el cliente construye la vista, lo que corresponde a un cliente pesado.

---

## ✅ Checkpoint — Parte 3 · API y API REST

**1. ¿Qué es una API? Dame dos ejemplos que no sean servicios remotos.**

Es una interfaz de programación de aplicaciones: el conjunto de definiciones y protocolos que un componente expone para que otros lo usen **sin conocer su implementación interna**. Ejemplos que no son servicios remotos: la **Stream API de Java** (una biblioteca del propio lenguaje) y la **Win32 API** de Windows (la interfaz del sistema operativo). También JPA, JDBC y la POSIX API de Linux.

**2. ¿Cuál es la relación entre "API" y "API REST"?**

**API es el concepto genérico:** cualquier interfaz que un componente expone. Puede ser una biblioteca, un framework o el sistema operativo. **API REST es un caso particular:** aquel en que la interfaz es **web y distribuida**, se expone sobre HTTP y quien la consume corre en otra máquina.

**3. Según la cátedra, ¿qué no es REST? ¿Y qué es?**

Según la cátedra, REST **no es un estilo arquitectónico ni un patrón de diseño**. **Es un estilo de diseño de comunicación** entre cliente y servidor, definido por una serie de criterios.

**4. Enumerá los cinco criterios de REST.**

(1) Se apoya sobre el protocolo HTTP; (2) cada solicitud es autocontenida: lleva toda la información necesaria para ser procesada; (3) se apoya en los verbos HTTP (POST, GET, PUT, PATCH, DELETE); (4) los recursos se identifican mediante URI; (5) el formato de intercambio es JSON (o XML).

**5. Si decís "te expongo una API REST", ¿qué cinco cosas quedaron acordadas sin decir una palabra más?**

Que la comunicación será por **HTTP**, que se usarán los **verbos HTTP** para las operaciones, que se devolverán **códigos de estado HTTP**, que los **recursos se identificarán por URI**, y que el **formato de intercambio será JSON**. Ese es el valor de un estándar: eliminar la negociación.

**6. ¿Por qué REST exige que cada request sea autocontenida?**

Porque REST se apoya sobre HTTP, que es un protocolo **sin estado**: el servidor no conserva información de las solicitudes previas. REST no combate esa característica, la **convierte en principio de diseño**: cada request debe incluir toda la información necesaria para ser procesada, sin depender de ningún contexto anterior.

**7. `GET /users/1` y `PUT /users/1` tienen la misma ruta. ¿Por qué está bien?**

Está bien porque en REST **la ruta identifica el recurso y el verbo indica la operación**. Un mismo recurso tiene una única URI, y las distintas operaciones sobre él se expresan cambiando el método HTTP. Eso es precisamente lo que hace a REST simple de diseñar y de consumir.

**8. ¿Por qué `POST /users/crearUsuario` está mal?**

Porque viola la regla de que **las rutas no deben contener verbos**: la acción ya está indicada por el verbo HTTP (POST). Las rutas deben llevar únicamente nombres de recursos, en sustantivo. La forma correcta es `POST /users`.

**9. ¿Qué códigos devuelven un POST y un DELETE exitosos? ¿Por qué son distintos?**

POST devuelve **201 Created** y DELETE devuelve **204 No Content**. Son distintos porque el código comunica **qué ocurrió**: en el primer caso se creó un recurso, en el segundo la operación se completó y no hay contenido que devolver.

**10. Explicá el problema N+1 con el ejemplo del alumno. ¿Qué regla de REST lo causa?**

Al pedir `GET /alumnos/12345`, la respuesta incluye las cursadas **solo como IDs**, no con su detalle. Para obtener el detalle hay que hacer **una solicitud adicional por cada cursada**: si el alumno cursó cuarenta materias, son cuarenta requests más. Lo causa la regla de que **REST no permite mezclar entidades**: cada recurso devuelve únicamente su propia información, y las cursadas son otro recurso con su propia URI.

**11. ¿Qué dijo la cátedra sobre respetar REST a rajatabla en el TPA?**

Que en el TPA se respeta **a rajatabla: no se mezclan entidades**. Es una instrucción directa. En la industria la regla se relaja habitualmente por simplicidad —de ahí la distinción entre REST y RESTful—, pero en el trabajo práctico no.

**12. Diferencia entre una API abierta y una gratuita. ¿Puede ser cerrada y gratuita?**

Son **dos ejes independientes**. **Abierta/cerrada** se refiere a si hace falta registrarse y obtener una credencial para usarla; **gratuita/paga**, a si se paga por su uso. **Sí puede ser cerrada y gratuita**: se exige registro y una API key, pero no se cobra. Es el caso más habitual de las APIs públicas serias, que registran a los usuarios para poder limitar el consumo.

**13. ¿Qué es OpenAPI y qué es Swagger? ¿Para qué los vas a necesitar en el TPA?**

**OpenAPI** es el **estándar** de documentación de APIs (versión actual 3.0); **Swagger** es la **herramienta que lo renderiza** de forma ordenada y navegable, mostrando rutas, parámetros, ejemplos y códigos de respuesta. En el TPA se necesitan **en las dos direcciones**: para **leer** la documentación de la API que provee la cátedra, y para **escribir** la documentación de las APIs propias cuando se las exponga.

---

## ✅ Checkpoint — Parte 4 · Práctica

**1. ¿Qué método HTTP usa el navegador al escribir una URL y apretar Enter? ¿Podés hacer un POST desde la barra?**

Usa **GET**, siempre. **No se puede hacer un POST desde la barra de direcciones**: para enviar un POST desde un navegador hace falta un formulario HTML o código JavaScript ejecutándose en la página, y para un PATCH o un DELETE ni siquiera eso alcanza.

**2. ¿Qué es Postman y qué limitación del navegador resuelve?**

Postman es un **cliente HTTP**: un programa que permite construir cualquier solicitud HTTP —con cualquier método, headers y body— y ver la respuesta completa. Resuelve la limitación del navegador, que desde su barra de direcciones solo puede emitir solicitudes GET.

**3. Diferencia entre path param y query param. Ejemplo de cada uno.**

El **path param identifica un recurso** y forma parte de la ruta: `/name/argentina`. El **query param filtra o modifica la consulta** y va después del signo de pregunta, con formato clave=valor y encadenado con `&`: `?fields=name,capital`. El primero determina *cuál* recurso se pide; el segundo, *cómo* se lo quiere.

**4. Quitás el `fields` de `/all` y la API devuelve 400. ¿Qué lección se saca?**

Que esa API **exige** el parámetro `fields` y sin él rechaza la solicitud, para no tener que devolver los 250 países completos. La lección general es que **cada API es distinta y siempre hay que leer su documentación**: más allá del estándar REST, no hay reglas universales sobre qué parámetros acepta o exige cada servicio.

**5. ¿Qué es un framework y en qué se diferencia de una biblioteca?**

Un framework es código que provee el **esqueleto de la aplicación ya construido**: el programador escribe las piezas y el framework las crea, las conecta y las ejecuta. La diferencia con una biblioteca está en **quién controla el flujo**: con una biblioteca, tu código llama al código externo; con un framework, es el framework el que llama a tu código.

**6. ¿Qué carpetas no van al repositorio y por qué?**

**`.idea/`**, porque contiene la configuración local de IntelliJ y genera conflictos permanentes entre integrantes con distintas versiones o preferencias del IDE. Y **`target/`**, porque contiene el código Java **compilado**, que se regenera automáticamente y no tiene sentido versionar. Ambas se agregan al `.gitignore`.

**7. ¿Qué es un DTO? ¿Por qué en Java hace falta y en JavaScript no?**

Un **DTO** (*Data Transfer Object*) es una clase sin lógica cuyo único fin es **transportar datos**; en el consumo de una API actúa como molde del JSON. Hace falta en Java porque es un lenguaje de **tipado estático**: el compilador necesita conocer la estructura del dato antes de ejecutar. JavaScript, de tipado dinámico, construye el objeto en tiempo de ejecución con la forma que traiga la respuesta.

**8. ¿Por qué `Pais` necesita `@NoArgsConstructor` y `@Data`? Explicá el algoritmo de Jackson.**

Jackson **crea el objeto vacío** con el constructor sin argumentos y luego **lo llena llamando a los setters**, campo por campo. Por lo tanto necesita **`@NoArgsConstructor`** (que aporta el constructor vacío, sin el cual no puede instanciar la clase) y **`@Data`** (que aporta los setters, sin los cuales crea el objeto pero no puede completarlo, dejando todos los atributos en `null`).

**9. ¿Para qué sirve `@JsonProperty`? ¿Cuándo no haría falta?**

Sirve como **puente entre el nombre del campo en el JSON y el nombre del atributo en Java**, que suelen estar en idiomas distintos (`name` → `nombre`). **No haría falta si ambos nombres coincidieran**: en ese caso Jackson los asocia automáticamente.

**10. ¿Qué hace `@JsonIgnoreProperties(ignoreUnknown = true)`? ¿Qué te protege y qué te esconde?**

Le indica a Jackson que **ignore los campos del JSON que el DTO no declara**. **Protege** de que la API externa agregue campos nuevos y la aplicación empiece a fallar sin haber cambiado una línea de código. **Esconde** los errores propios: si se escribe mal un `@JsonProperty`, el campo no matchea, no se lanza ninguna excepción y el atributo queda silenciosamente en `null`.

**11. ¿Por qué `buscarPorNombre` devuelve `Optional<Pais>` y `buscarPorMoneda` devuelve `List<Pais>`?**

Porque **un nombre identifica a un único país**: existe o no existe, y `Optional` expresa explícitamente esa ausencia posible. **Una moneda, en cambio, la comparten varios países** (el euro, veinte), de modo que el resultado es una colección por naturaleza. La firma del método comunica el contrato sin necesidad de leer su cuerpo.

**12. ¿Por qué no se concatena la URL con `+`? ¿Diferencia entre `build()` y `buildAndExpand()`?**

No se concatena porque **los espacios y los caracteres especiales rompen la URL**: un espacio la vuelve inválida y hay que codificarlo. `UriComponentsBuilder` arma la URL por partes y **escapa los valores**. **`build()`** se usa cuando la ruta es fija, sin placeholders; **`buildAndExpand(valores)`** cuando la ruta tiene placeholders `{...}`: los rellena con los valores y, al hacerlo, los escapa.

**13. ¿Por qué la URL base vive en el `application.yml` y no en el código?**

Para no tener que **recompilar la aplicación** si esa URL cambia, y para poder usar **valores distintos según el entorno** sin modificar el código. Es el mismo principio del `.env` en Node: la configuración vive fuera del código compilado. Además evita que el valor se duplique en varios puntos y se desincronice.

**14. Un campo del DTO viene siempre en `null` y los demás llegan bien. ¿Cuál es la causa más probable?**

Un **`@JsonProperty` mal escrito**. Jackson busca ese nombre en el JSON, no lo encuentra, y por `ignoreUnknown = true` no lanza ninguna excepción: deja el atributo en su valor por defecto, `null`. (La otra causa posible, con el mismo síntoma, es haber agregado el atributo al DTO sin agregarlo a la lista de campos que se le piden a la API.)

**15. TPA: ¿en qué orden recomienda la cátedra encarar la Entrega 1?**

Primero los **bocetos de interfaz**, después el **diagrama de casos de uso**, después el **diagrama de clases**, y **el código al final**. La razón explícita es que empezar por el código lleva a tener que rehacerlo: el modelado tiene que estar pensado antes.

**16. TPA: ¿qué se pregunta en el foro general y qué al tutor personal?**

En el **foro general** se consultan **únicamente dudas de enunciado**: qué es lo que hay que hacer, qué se está pidiendo. Al **tutor personal** se le consultan las **decisiones de implementación y de diseño** ("encaramos esto de tal forma, ¿qué opinan?"). En el canal general no responden preguntas del tipo "esto lo hice así, ¿está bien?".

---

**FIN DEL COMPLEMENTO — CLASE 02**
