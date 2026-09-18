# 🗺️ Clase desde cero — Cliente Liviano (clase 22) — ROADMAP

**Materia:** Diseño de Sistemas de Información (DSI) · UTN FRBA · 2026
**Unidad:** clase 22 — *Cliente liviano, sesiones, cookies, stateless vs stateful, protección de recursos, sesiones mixtas*
**Tipo de material:** clase desde cero (material pedagógico extra, reemplaza a la clase original). Después de completarla, el apunte maestro oficial se lee como repaso.

---

## Sobre este material

Está escrito para alguien que **nunca escuchó hablar de nada de esto**. Cada término se explica antes de usarse. Nada se da por sabido salvo lo que la materia ya dio en clases anteriores (Java, Maven, Spring Boot básico, API REST, DTOs, patrones).

Son **ocho módulos, del 0 al 7**, ordenados por **dependencia de conceptos**, no por el orden en que se suelen dictar. Cada módulo se apoya en el anterior. Leerlos salteados no funciona.

## Por qué este orden

La unidad tiene un solo hilo conductor, aunque parezca que son dos temas ("hacer pantallas" y "seguridad"): **un servidor que arma pantallas para una persona**. Y en cuanto hay una persona del otro lado, aparecen tres preguntas en cadena:

1. ¿Cómo armo la pantalla y le pongo datos? → **Fase B** (módulos 1, 2, 3)
2. ¿Cómo sé quién es la persona que me está pidiendo la pantalla? → **Fase C** (módulos 4, 5, 6)
3. ¿Cómo hago todo eso cuando los datos no viven en mi servidor sino en otros? → **Fase D** (módulo 7)

Y antes de todo, **Fase A** (módulo 0): ver el asunto funcionando con tus propias manos, porque un concepto flotante no engancha y uno que ya viste andar sí.

## Los módulos

| # | Módulo | Fase | Densidad | Qué te llevás |
|---|---|---|---|---|
| **0** | Armar el proyecto y verlo levantar | A — Ver funcionar | 🟡 | Un servidor de front corriendo y una pantalla en el navegador. Qué es Thymeleaf de verdad. Dónde va cada archivo. |
| **1** | Qué es un cliente liviano y qué cambia | B — La herramienta | 🔴 | Liviano vs pesado. Monolítico vs desacoplado. Qué es un recurso. Por qué HTTP no se acuerda de vos. |
| **2** | Plantillas: mostrar datos sin repetir código | B | 🔴 | Listas, condicionales, URLs. Herencia de plantillas: el header se escribe una sola vez. |
| **3** | Formularios: cargar datos, validarlos, devolver errores | B | 🔴 | Que el formulario viaje al servidor y vuelva marcando qué estuvo mal sin perder lo escrito. |
| **4** | Sesiones y cookies: cómo el servidor se acuerda de quién sos | C — Identidad | 🔴 | Qué es una sesión, qué es una cookie, quién guarda qué, y cómo viaja. |
| **5** | Tokens: cuando el servidor no se acuerda de nada | C | 🔴 | Stateless vs stateful. JWT por dentro. Cuándo conviene cada uno. |
| **6** | Proteger rutas y esconder botones según rol y permiso | C | 🔴 | Autenticación, roles, permisos. Spring Security como portero. |
| **7** | Sesiones mixtas: cómo se arma todo junto en DonaTrack | D — El TP | 🔴 | El servidor de front desacoplado hablando con los servicios por tokens. El plano de la Entrega 5. |

**Densidad** = importancia para la materia: 🔴 central y evaluable · 🟡 necesario pero no te lo van a preguntar · 🟢 mencionado al pasar.

Estimación de lectura con la compu abierta: módulo 0 ≈ 1 h (es práctico), módulos 1 a 3 ≈ 40 min cada uno, módulos 4 a 6 ≈ 45 min cada uno, módulo 7 ≈ 1 h.

## Hilos que se cierran

Cada módulo deja **cabos abiertos a propósito**: un dolor que se siente ahora y que un módulo posterior cura. Cuando leas un "esto se resuelve en el módulo N", no es que falte contenido: es un hilo plantado. Este es el mapa:

| Hilo | El dolor | Se abre en | Se cierra en |
|---|---|---|---|
| **H1** | Cada pantalla repite el `<head>`, la barra de navegación y el pie. Con 30 pantallas, 30 copias. | 0 | 2 (herencia de plantillas) |
| **H2** | ¿De dónde salen los datos que le paso a la pantalla? Hoy están escritos a mano en el controller. | 0 | 2 (del *service*) → 7 (de otros servicios por HTTP) |
| **H3** | Sé mostrar datos. ¿Y cuando la persona tiene que cargarlos? ¿Y si carga mal? | 2 | 3 (formularios y validaciones) |
| **H4** | HTTP no tiene memoria: cada pedido llega solo, sin saber de dónde viene. ¿Cómo sabe el servidor que sos vos? | 1 | 4 (sesiones y cookies) |
| **H5** | Todas las rutas están abiertas: cualquiera que conozca la URL entra a cualquier pantalla. | 0 · 1 | 6 (protección de recursos) |
| **H6** | En el TP, el servidor de front no tiene los datos: viven en otros servicios. | 1 | 7 (sesiones mixtas) |
| **H7** | Con sesión, el servidor guarda algo por cada persona conectada. ¿Y si son un millón? ¿Y cómo se cierra una sesión de un token? | 4 | 5 (tokens) → 7 |

## Leyenda de bloques

| Marca | Qué es |
|---|---|
| 🔴 🟡 🟢 | Densidad de la sección (ver arriba). |
| **Sobre este documento** | Al inicio de cada módulo: qué cubre y qué NO cubre. |
| **De dónde venís** | Qué se asume sabido al entrar a ese módulo. |
| 🎓 **Si te lo preguntan** | Respuesta modelo lista para examen: corta, con la terminología de la materia. |
| 🕳️ **Madriguera** | Una tangente que existe, en dos líneas, para que no te la lleves puesta. Se saltea sin culpa. |
| 🔴 **Bifurcación — anotá qué elegiste** | Una decisión que define pasos posteriores. Ambas ramas quedan documentadas. |
| ✅ **Deberías ver** | Compuerta de verificación: la señal concreta de que un paso salió bien antes de seguir. |
| ⚠️ | Trampa: un error que te va a pasar si no lo ves venir, con la forma correcta al lado. |
| 🧩 **En tu TP** | Guiño corto al proyecto real: cómo se traduce lo del módulo a DonaTrack. Nunca más de unas líneas. |
| `// ¿CÓMO FUNCIONA?` | Al pie de un bloque de código: el flujo paso a paso, qué pasa al ejecutarse, resultado esperado. |
| **Checkpoint** | Cierre de cada módulo: 5-10 preguntas **sin respuesta**. Las respuestas se arman en el complemento, después de las dudas por chat. |

## Cómo usarlo

1. **En orden.** El módulo 0 con la compu abierta y el proyecto al lado; los demás se pueden leer sin ejecutar, porque todo código trae su resultado esperado como comentario.
2. **Un módulo por sesión** es un buen ritmo. Dos si vienen fáciles. No más.
3. Al terminar un módulo, **respondé el checkpoint mentalmente o en papel**. Si alguna no te sale, es exactamente la duda para llevar al chat antes de seguir.
4. **Las dudas se aclaran por chat, sin regenerar el módulo.** Cuando no queden dudas, se puede pedir rehacer ese módulo con lo aclarado. Recién ahí, el siguiente.
5. Si te encontrás con un `th:` que no conocés o una anotación que no viste, **no la busques afuera**: o está explicada más arriba en el mismo módulo, o en uno anterior, o es una madriguera. Si no está en ninguno de los tres lugares, es un error del material y hay que avisarlo.
6. Después del módulo 3 ya podés empezar a construir pantallas en el TP. Después del 6, protegerlas. Después del 7, conectar todo. No hace falta terminar los ocho para empezar a trabajar.

---

**FIN DEL ROADMAP — Clase desde cero: Cliente Liviano (clase 22)**
