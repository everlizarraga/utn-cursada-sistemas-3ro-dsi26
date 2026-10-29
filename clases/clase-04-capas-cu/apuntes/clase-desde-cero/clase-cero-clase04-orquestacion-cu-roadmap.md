# 🗺️ Clase desde cero — Clase 04: Orquestación de casos de uso en capas · Roadmap

**Unidad:** `clase04` — Arquitectura de Software (15/04/2026)
**Qué es esto:** la clase 04 dada desde cero, en 9 módulos ordenados por dependencia de conceptos. Al terminarla, tenés que poder **tomar un caso de uso, repartirlo en clases de distintas capas y dejarlo expuesto como API REST con Spring Boot**, y explicar por qué cada clase está donde está.
**Código de referencia:** el servicio de ventas de SmartLife (`sales-service`), completo, en `fuente-repo-clase04-sales-service-final.md`. Los módulos de la Fase B lo construyen capa por capa; el estado final de M9 es exactamente ese código.

---

## 1. De dónde venís (lo que se asume, sin re-explicar)

| Ya sabés | De dónde |
|---|---|
| El dominio completo del servicio de ventas: `Comercio`, `Producto`, `TipoProducto`, `Impuesto` (IVA, EO, EI), `Venta`, `ItemVenta`, `ObservadorVenta` y sus tests | Clase 03 |
| HTTP, verbos, status codes, path params vs query params, Postman | Clase 02 |
| Java: lambdas, streams, excepciones y `try/catch` | Seminario de Java (preclase 02) |
| Herencia, composición, interfaces; actores y casos de uso | Paradigmas · Análisis de Sistemas |
| Crear un proyecto Spring Boot, `pom.xml`, Maven, Lombok, Jackson (`@JsonProperty`), `@Configuration` + `@Bean`, `@SpringBootTest`, `@Autowired` sobre un atributo | Clase 02 |
| Inyección de dependencias (por constructor), mocking con Mockito, biblioteca vs framework | Preclase 04 |
| Atributos de calidad, acoplamiento y cohesión, qué es un patrón | Preclase 03 |
| Componentes de un sistema (frontend, backend, base de datos), diagramas de componentes y despliegue, agregación vs composición | Clase 01 |

Si algo de la tabla no lo tenés fresco, no hace falta volver a leerlo: cada módulo trae una línea de recordatorio donde lo usa.

---

## 2. Por qué este orden

La clase tiene dos mitades. La primera es **conceptual**: qué es el modelo en capas, por qué un caso de uso no cabe en un método, cómo se modelan usuarios y permisos, y qué clases viven en cada capa. La segunda es **el código**: el servicio de ventas expuesto como API REST, con Spring Boot haciendo de orquestador.

El orden de los módulos sigue las **dependencias entre ideas**, no el orden en que uno las contaría de apuro:

```
 FASE A · TEORÍA                              FASE B · CÓDIGO
 ─────────────────                            ───────────────
 M1 El problema y las 3 capas ───┐
                                 ├─► M3 Entidades y repositorios ─┐
 M2 Usuario, rol y permiso ──────┘                                ├─► M4 Services y controllers (el mapa)
                                                                  │
      ┌───────────────────────────────────────────────────────────┘
      ▼
 M5 Proyecto + capa de datos ─► M6 DTOs y excepciones ─► M7 Services ─► M8 Controllers + errores ─► M9 Arrancar y probar
```

- **M1 antes que todo:** sin la tesis ("un caso de uso se desglosa en clases de varias capas") el resto son recetas.
- **M2 antes que M3/M4:** usuario, rol y permiso son modelado de dominio. Hay que tenerlos antes de ver dónde se verifica un permiso (M4).
- **M3 antes que M4:** los services orquestan **a** las entidades y **a** los repositorios; primero hay que conocer a los orquestados.
- **La Fase B construye en el mismo orden en que M3 y M4 presentan las clases**, de abajo hacia arriba: datos (M5) → lo que viaja entre capas (M6) → dominio intermedio (M7) → presentación (M8) → arranque y pruebas (M9). Cada módulo deja el proyecto en un estado que compila.

---

## 3. Los módulos

| Módulo | Archivo | Densidad | Qué deja |
|---|---|---|---|
| **M1** — Un caso de uso no es un método: el modelo en capas | `…-modulo1-capas.md` | 🔴 | La tesis de la clase. Las tres capas y su regla. Ventajas y desventajas. Tres preguntas abiertas |
| **M2** — Usuario, rol y permiso | `…-modulo2-usuario-rol-permiso.md` | 🔴 | `Usuario → Rol → * Permiso`; roles y permisos como instancias, no clases. La entidad tiene un `Usuario` (composición, no herencia). 401 vs 403 |
| **M3** — Las clases típicas (I): entidades y repositorios | `…-modulo3-entidades-y-repositorios.md` | 🔴 | Regla 1: el repo conoce al dominio, nunca al revés. No toda entidad tiene repo: solo las persistentes, y algunas se guardan en cascada |
| **M4** — Las clases típicas (II): services y controllers. El mapa completo | `…-modulo4-services-y-controllers.md` | 🔴 | Reglas 2, 3 y 4. El flujo de un caso de uso. El mapa de quién conoce a quién |
| **M5** — El proyecto y la capa de datos | `…-modulo5-proyecto-y-repositorios.md` | 🔴 | El proyecto multimódulo, paquetes = capas, beans, los cuatro repositorios en memoria |
| **M6** — Lo que viaja y lo que falla: DTOs y excepciones propias | `…-modulo6-dtos-y-excepciones.md` | 🔴 | Los 8 `record` y las 3 excepciones |
| **M7** — La capa intermedia: services que orquestan el caso de uso | `…-modulo7-services.md` | 🔴 | `ProductoServiceImpl` y `VentaServiceImpl` completos; inyección por constructor en acción |
| **M8** — La capa de presentación: controllers y el manejo global de errores | `…-modulo8-controllers-y-errores.md` | 🔴 | Los dos controllers y el `GlobalExceptionHandler`. La API levanta |
| **M9** — Arrancar y probar: seeder, Postman y tests | `…-modulo9-arrancar-y-probar.md` | 🔴 (tests 🟡) | El seeder, la colección de Postman, el recorrido de una request punta a punta, los tests |

**Densidad:** 🔴 central y evaluable · 🟡 secundario · 🟢 mencionado al pasar. Vale para los módulos y para las secciones de cada módulo.

---

## 4. Hilos que se abren y se cierran

Varios módulos dejan preguntas abiertas a propósito. Es intencional: cuando llegue la respuesta, ya vas a haber sentido el problema. Este es el mapa.

| Se abre en | Pregunta | Se cierra en |
|---|---|---|
| M1 | ¿Quién instancia a `Calificacion`? | M4 (regla 2) → M7 (en código) |
| M1 | ¿Quién verifica que quien crea la calificación es un docente? | M2 (con qué) → M4 (dónde) → M7 (en este proyecto no hay; dónde iría) |
| M1 | ¿Quién guarda? | M3 → M5 |
| M1 | La regla entre capas, ¿es tan estricta? | M4 (Regla 3: services hermanos) |
| M1 | Una capa, ¿cómo se ve en Java? | M5 (paquetes = capas) |
| M2 | Roles y permisos viven persistidos, ¿dónde? | M4 (`PermisosRepository`) |
| M3 | Si el dominio no conoce al repositorio, ¿quién llama al repositorio? | M4 |
| M3 | ¿Por qué los repositorios son interfaces? | M5 (el service conoce `ProductoRepository`, no `InMemoryProductoRepository`) → M7 (el controller conoce `ProductoService`, no `ProductoServiceImpl`) |
| M4 | `DataCalificacion`, ¿qué es? | M6 (`record`) |
| M4 | ¿Quién instancia a los services y a los repositorios? | M5 (repositorios) → M7 (services) |
| M4 | ¿Quién llama al service? | M8 |
| M4 | Los services "patean" los errores, ¿a dónde? | M8 |
| M5 | ¿Qué se hace con un `Optional` vacío? | M7 |
| M5 | ¿De dónde sale el comercio con id 1? | M9 (seeder) |
| M6 | Si `ProductoUpdateRequest` no trae el id, ¿dónde va? | M8 |
| M6 | ¿Quién atrapa las excepciones propias? | M8 |
| M7 | El service devuelve DTOs, ¿quién los vuelve JSON? | M8 |
| M8 | Todo `POST` da 404 porque no hay comercio ni tipos de producto: ¿quién los carga? | M9 (seeder) |

---

## 5. Leyenda de bloques

| Bloque | Qué es |
|---|---|
| 🔴 🟡 🟢 | Importancia de la sección (ver arriba) |
| **Regla N (para anotar)** | Las cuatro reglas de quién conoce a quién. Son lo más evaluable de la clase |
| ⚠️ | Aviso: una confusión típica, un error que se comete al escribir el primer código |
| 🕳️ **Madriguera** | Una tangente que existe pero no entra en la materia. Se lee en 30 segundos y se vuelve al camino. No es tarea |
| 🔧 **Versión floja** | Un fragmento que compila y anda, pero está mal decidido. Va después de la versión buena, con un "Dónde falla" que dice qué ve la persona cuando se rompe |
| **Para el parcial, si te preguntan** | Pregunta probable y respuesta modelo: la primera oración ya responde, con la terminología de la materia, en 1 a 3 oraciones |
| **Molde** | La frase tipo para justificar una decisión: "priorizo X; pago con Y" |
| `// ¿CÓMO FUNCIONA?` | Debajo de un bloque de código: el paso a paso de lo que pasa al ejecutarse y el resultado esperado |
| 💡 | Nota al margen: una conexión con algo que ya hiciste o una variante equivalente que podés cruzarte |
| **Términos nuevos** | Lista de una línea por término de Java o Spring que aparece por primera vez debajo de un bloque de código |
| **Estado del proyecto** | Al cierre de M5 a M9: qué hay y qué hace el proyecto si lo levantás |
| ✅ **Checkpoint** | 5 a 10 preguntas al cierre de cada módulo, **sin respuestas**. Las respuestas van al complemento de la unidad |

El código va comentado **por bloque**, no línea por línea: cada comentario dice qué hace ese bloque y por qué está así. Si querés el detalle de una línea, se pide y se agrega.

---

## 6. Cómo usarlo

- **Fase A (M1–M4):** se lee de corrido, con lápiz. Son módulos de lectura; el único "código" es un fragmento ilustrativo en M4. Los cuatro juntos llevan una sesión larga o dos cortas.
- **Fase B (M5–M9):** con el proyecto abierto en el IDE y Postman a mano. Cada módulo agrega una capa; conviene tipear o pegar el código y levantar el proyecto al final de cada uno, aunque hasta M8 no haya nada que llamar. El código de cada paso está en el módulo, salvo lo que es repetición literal: tres interfaces y dos implementaciones de repositorio (M5 §4 y §5.3), las entidades de la clase 03, el resto de `DominioSalesServiceTest` y la colección de Postman. Eso, y el proyecto final completo, se toma del snapshot de referencia.
- **Los checkpoints se responden de memoria** al cerrar cada módulo. Si una pregunta te traba, ese es el tema a releer antes de seguir.
- **Las cuatro reglas** (M3 y M4) conviene tenerlas en una hoja aparte hasta que salgan solas.
- **Los hilos abiertos no se persiguen:** cuando un módulo dice "esto se cierra en M7", se sigue leyendo.

---

## 7. Alcance de la Entrega 1 del TPA (vigente al cursar esta unidad)

Lo que se aprende en esta unidad **no** se pide todavía en la primera entrega. Para la Entrega 1:

- **Solo la capa de dominio** del servicio de donaciones (`Donante`, `Donacion`, etc., según el enunciado), implementada y probada con tests. Dar de alta un donante es un `new Donante(...)` dentro de un test: no hay service ni repositorio que lo haga.
- **Un único controller** con un endpoint `GET` que devuelve un mensaje fijo ("Hola desde el servicio de Donaciones."), para probar que el servicio responde: un **health check**. Sin lógica.
- **Sin services, sin repositorios**, sin más controllers que ese.
- **No modelar `Usuario` todavía.** Si hace falta el donante como entidad, se modela el donante; la clave de acceso y la verificación de permisos no se hacen aún.
- **La importación masiva de donantes desde CSV** se resuelve con una clase propia (por ejemplo, `ImportadorDeDonantes`) que recibe la ruta del archivo y devuelve una colección de donantes. Quién la llama después es un service; dónde se guardan, un repositorio. Eso es de la entrega siguiente: ahora alcanza con la clase, probada por tests. Hay un CSV de prueba junto al enunciado.
- **Mockear está permitido** (preclase 04), por ejemplo para probar el importador o las notificaciones.
- **Las notificaciones** se envían de forma simulada, también probadas por tests.
- **Aplicación multimódulo**, siguiendo el template que entregó la cátedra para cada equipo.
- **El diagrama de arquitectura** debe describir lo que efectivamente se entrega en la Entrega 1. Si el diagrama de despliegue que ya tienen lo representa bien, alcanza con una lectura e interpretación escrita de ese diagrama.
- El enunciado trae una pregunta de discusión sobre modelo rico vs. anémico: la base para responderla está en M3 §1.3.
- Logística de la cursada 2026: la entrega se coordina con el ayudante del equipo (día y horario pueden no ser los de la clase) y la semana de la entrega no hay clase.

---

## 8. Qué viene después de la clase desde cero

1. **Complemento** de la unidad: las respuestas de los 9 checkpoints y las dudas que hayan salido en el camino.
2. **Apunte resumen** (opcional) para la repetición espaciada.
3. **Bitácora** de la unidad.

---

**FIN DEL ROADMAP — Clase desde cero · Clase 04**
