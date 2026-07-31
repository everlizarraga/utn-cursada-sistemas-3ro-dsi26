# 🌱 Proyecto 0 (clase 04) — Etapa 9: La semilla y el examen final

> **Objetivo:** que la app arranque **con datos** (el seeder, con piezas de tu proyecto-1) y rendir el **examen final**: la batería completa de tu API contra una app recién levantada.
>
> **Cómo trabaja esta etapa (v2):** los principios completos primero — incluida **la regla de la autoridad de ids**, anunciada ANTES del experimento que la verifica. La semilla es **tu turno directo**: especificación + hints, sin ejemplo resuelto (el molde ya lo leíste dos veces). Precalentamiento del vuelo solo.
>
> **Pre-requisito:** Etapa 8 completa.
>
> **Tiempo estimado:** 40-50 minutos.

---

## 🗺️ El mapa general — dónde estamos parados

```
      ARRANQUE DE LA APP                          EN VUELO
      ─────────────────                           ────────
      Spring construye                    Controllers ──► Services ──► Repos
      todos los beans                                                    ▲
            │                                                            │
            ▼                                     (nada de esta columna  │
   ┌─────────────────────┐                         se toca hoy — hoy     │
   │ ★ DataInitializer   │                         se EXAMINA)           │
   │   (config/)         │──── siembra datos ───────────────────────────┘
   │   corre UNA vez,    │     directo en los repos
   │   al terminar de    │
   │   levantar          │
   └─────────────────────┘
```

Movimiento total: **1 archivo nuevo** (`config/DataInitializer.java`). Todo lo demás queda quieto — y después rinde examen.

## 🗂️ Los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── config/                              (📦 NUEVO package)
    │   └── DataInitializer.java             ★ NUEVO — tu turno, Parte 3
    └── (todo lo demás: NO se toca)
```

## 🧭 Mapa de esta etapa

1. **LOS PRINCIPIOS COMPLETOS:** el seeder, las piezas, y la autoridad de ids.
2. La vuelta de tuerca nueva: inyección en parámetros de método.
3. Tu turno directo: la semilla.
4. Experimento: el choque de ids (verificación del principio).
5. El examen final: la batería completa.
6. Criterios + checkpoint + registro + 🔗.

---

## 🧠 Parte 1: LOS PRINCIPIOS COMPLETOS (leé toda esta parte antes de codear)

### 1a. El problema y la solución

Lo venís pagando desde la Etapa 5 (la memoria efímera: `Ctrl+C` y todos los datos mueren con la JVM): **cada arranque, la app nace vacía**, y pagás cinco requests de peaje antes de poder probar nada. La solución: un **seeder** — código que corre **al terminar de levantar la app** y deja los repos con datos de arranque. En producción real no va (los datos reales no se inventan al bootear — pregunta de checkpoint); en desarrollo con memoria pura, es lo que vuelve usable a la app.

### 1b. Las tres piezas — reencuentro con tu proyecto-1

No se re-enseñan; se saludan con su recap de una línea:

```
┌────────────────────┬────────────────────────────────────────────────────┐
│ @Configuration     │ clase que Spring lee al arrancar; su rol:          │
│ (sobre la clase)   │ DECLARAR beans a mano                              │
├────────────────────┼────────────────────────────────────────────────────┤
│ @Bean              │ "lo que retorna este método es un bean:            │
│ (sobre un método)  │  invocalo vos, framework" — la 3ª forma de         │
│                    │  fabricar beans que conocés (estereotipos,         │
│                    │  escaneo… y esta: la manual)                       │
├────────────────────┼────────────────────────────────────────────────────┤
│ CommandLineRunner  │ interfaz funcional (un método: run(String...));    │
│ (lo que retornás)  │ contrato especial: Spring ejecuta todos los        │
│                    │ beans de este tipo NI BIEN la app termina de       │
│                    │ levantar — tu "hacé esto al arrancar"              │
└────────────────────┴────────────────────────────────────────────────────┘
```

> 🎛️ **Ficha `@Configuration`:** sin atributos que necesites (existe `proxyBeanMethods` — solo reconocer). **Ficha `@Bean`:** `name`, `initMethod`/`destroyMethod` (hooks de ciclo de vida) — solo reconocer; pelada alcanza.

### 1c. LA REGLA de la autoridad de ids — antes de sembrar un solo dato

En tu sistema, los ids los fabrica **una sola autoridad**: el `GeneradorIdSecuencial`, adentro del `save` del repo (Etapa 5: "sin id → le asigno el siguiente"). **La semilla NO es excepción**:

> **Sembrá con id `null` — que el generador reparta.** Como el seeder corre primero, los ids son predecibles igual (1, 2, 3…), sin segunda autoridad.

¿Qué pasaría si la semilla trajera ids fijos (`new Propietario(1L, ...)`)? El generador — que arranca en cero y no sabe nada de tu semilla — le daría **1** al primer POST: dos entidades con el mismo id, una de ellas inalcanzable para siempre por `findById` (el stream devuelve la primera que matchea). Corrupción silenciosa, prima de la huerfanización de la Etapa 7: nada explota, todo queda roto. El Experimento de la Parte 4 te lo hace verificar.

*(¿Y por qué el `DataInitializer` de la cátedra SÍ usa ids fijos con constantes? Pregunta finísima — su respuesta está en QUÉ siembra él contra QUÉ crea su API. Va al checkpoint, y la 🔗 te la confirma con su código adelante.)*

### 1d. Las dos reglas restantes de un buen seeder

- **Asociar Y persistir son dos actos:** una mascota sembrada se agrega al propietario (`agregarMascota`) **y** se guarda en su propio repo. Si solo asociás, `GET /mascotas` no la ve; si solo guardás, `cantidadMascotas` da 0.
- **Idempotencia:** envolvé la siembra en "¿ya hay datos?" (`findAll().isEmpty()`). Con memoria pura parece redundante (siempre arranca vacía)… ¿cuándo dejaría de serlo? Pregunta de checkpoint — y el bonus del capstone la responde solo.

---

## 🔌 Parte 2: La vuelta de tuerca nueva — inyección en parámetros de método

La ÚNICA novedad técnica de la etapa. Ya conocés dos caras de la inyección; hoy la tercera:

```
  CARA 1 (Etapa 4)             CARA 2 (Etapa 5)            CARA 3 ★ (hoy)
  ────────────────             ────────────────            ──────────────
  constructor del              constructor del             PARÁMETROS de un
  controller pide              service pide                método @Bean:
  services                     repositories
                                                           @Bean
  Spring los busca             Spring los busca            CommandLineRunner seed(
  y los inyecta                y los inyecta                  PropietarioRepository pr,
                                                              MascotaRepository mr) {...}

                                                           Spring ve los parámetros,
                                                           busca esos beans (tus
                                                           @Repository) y los alcanza
                                                           al invocar el método.
```

Misma inyección de siempre, tercer lugar donde aparece.

## ✍️ Parte 3: Tu turno directo — la semilla

📍 **Dónde va, y su forma exterior** (el interior es tuyo):

```
┌─ 📁 src/main/java/.../veterinaria/config/DataInitializer.java  (NUEVO) ─┐
│  @Configuration                                                         │
│  public class DataInitializer {                                         │
│                                                                         │
│      @Bean                                                              │
│      public CommandLineRunner seedData( ...los 2 repos... ) {           │
│          return args -> {                                               │
│              ╔═══════════════════════════════════╗                      │
│              ║   TU SEMILLA VA ACÁ               ║                      │
│              ║   (spec abajo — sin ejemplo:      ║                      │
│              ║    el molde ya lo leíste dos      ║                      │
│              ║    veces: tu proyecto-1 y el      ║                      │
│              ║    DataInitializer del recorrido  ║                      │
│              ║    P6 §9)                         ║                      │
│              ╚═══════════════════════════════════╝                      │
│          };                                                             │
│      }                                                                  │
│  }                                                                      │
└─────────────────────────────────────────────────────────────────────────┘
```

**Especificación:** 1 propietario (nombre y teléfono a tu gusto) con **2 mascotas** — aplicando las tres reglas de la Parte 1: ids en `null` (1c), asociar Y persistir (1d), idempotente (1d).

💡 **Hints escalonados (quemalos en orden):** H1 — releé el `DataInitializer` del recorrido P6 §9: tu estructura es esa, con tu dominio. · H2 — adentro de la lambda: crear propietario → `agregarMascota` ×2 → `save` en cada repo correspondiente. · H3 — el `isEmpty` envuelve todo; y cada mascota va a SU repo — la asociación al dueño no la persiste sola.

**Resultado esperado:** app limpia levantada → `GET /veterinaria/propietarios` → **200 con tu propietario, `cantidadMascotas: 2`** — sin haber posteado nada · `GET /veterinaria/mascotas` → las 2, con su `propietarioId` correcto. Peaje eliminado.

## 🧨 Parte 4: Experimento — el choque de ids (verificación de 1c, temporal y reversible)

La regla de 1c, con tus manos. Cambiá **temporalmente** la semilla del propietario a id fijo: `new Propietario(1L, ...)`.

**Predicción (ya sabés la teoría — el punto es verla):** arrancá, verificá que el sembrado está… y mandá un `POST /propietarios` nuevo. ¿Qué id recibe? ¿Y qué queda roto exactamente?

Verificá el desastre completo: el POST → **201 con id 1** (¡otra vez 1!) · `GET /propietarios` → **los dos**, mismo id · `GET /propietarios/1` → devuelve **uno solo** (el primero que el stream encuentra) — el otro quedó **inalcanzable**: ningún PUT, PATCH ni DELETE lo va a encontrar jamás. Todo con 200/201 verdes. **Restaurá el id `null`** y verificá que el resultado esperado de la Parte 3 vuelve a dar.

## 🏁 Parte 5: El examen final — la batería completa

App recién levantada (semilla puesta), corrida **en orden, sin saltear**. Examina TODO el proyecto:

| # | Request | Esperado |
|---|---|---|
| 1 | `GET /propietarios` | 200 · el sembrado, `cantidadMascotas: 2` |
| 2 | `POST /propietarios` (válido) | **201** · id nuevo del generador |
| 3 | `POST /mascotas` sobre el sembrado, ×3 | 201 ×3 (llega a 5) |
| 4 | `POST /mascotas` — la sexta | **400** · mensaje de la regla, traducido |
| 5 | `GET /mascotas/99` | **404** · tu `ErrorResponse` uniforme |
| 6 | `PUT /mascotas/1` completo, ×2 seguidas | 200 ×2 · **mismo estado final** (idempotencia) |
| 7 | `PATCH /mascotas/1` solo nombre | 200 · **especie intacta** |
| 8 | `PATCH /propietarios/{sembrado}` body `{}` | según **TU política**, tal como la documentaste |
| 9 | `DELETE /mascotas/1` → `GET /mascotas/1` → `GET` del dueño | **204** → **404** → `cantidadMascotas` **decrementado** |
| 10 | `POST /propietarios` `{"nombre":""}` | **400** · `bad_request` |

**Diez de diez en verde = la API está completa.** Verbos, capas, traductor, semilla, tus políticas — todo junto, verificado por vos. Tomate el minuto: hace nueve etapas esto era un `pong`.

## ✅ Criterios de "Etapa 9 completa"

- [ ] Leíste la Parte 1 entera — la regla de la autoridad de ids, ANTES del experimento.
- [ ] La semilla escrita por vos, sin ejemplo, cumpliendo las tres reglas.
- [ ] El choque de ids: verificado y revertido.
- [ ] Podés explicar por qué la cátedra puede ids fijos y vos no (o al menos, tenés tu hipótesis para la 🔗).
- [ ] Examen final: **10/10**, en orden, contra app limpia.

## ✅ Checkpoint

*Recall:*
1. ¿En qué momento exacto corre un `CommandLineRunner`, y qué garantiza eso sobre los beans que usa?
2. Las tres caras de la inyección que ya usaste — nombralas con tu ejemplo concreto de cada una.
3. ¿Por qué asociar (`agregarMascota`) y persistir (`save` en el repo de mascotas) son dos actos? ¿Qué se rompe si falta cada uno?

*Decidí y justificá:*
4. Tu seeder habla directo con repos y entidades, salteándose los services (¡y sus validaciones!). Defendé el diseño actual Y armá el mejor caso para sembrar vía services. ¿Cuál elegís?
5. La pregunta fina: ¿por qué el `DataInitializer` de la cátedra puede sembrar con ids fijos sin chocar con su generador? (Pista: compará qué entidades siembra él contra qué entidades crea su API.)
6. El `isEmpty` parece redundante con memoria pura. Da DOS escenarios futuros concretos donde deja de serlo — uno lo tenés a una etapa de distancia.
7. "El seeder va comentado con un TODO para borrarlo antes de producción." ¿Alcanza? ¿Qué harías vos para que datos de prueba jamás lleguen a un ambiente real?

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Abrí el `DataInitializer` del `sales-service` lado a lado con el tuyo: misma anatomía (`@Configuration` + `@Bean CommandLineRunner` + repos por parámetro + `isEmpty`), y **una diferencia deliberada** — sus ids fijos con constantes (`TIPO_HOGAR_ID = 1L`) contra tus ids `null`. Contestá la pregunta 5 con su código adelante: él siembra **tipos de producto y el comercio** — entidades que su API **jamás crea** (no existe POST de tipos ni de comercios) — y los **productos**, que SÍ se crean por API, **no los siembra**. Su lujo se sostiene en esa separación; tu veterinaria siembra lo mismo que crea, y por eso tu única autoridad es el generador. **Dos diseños correctos, cada uno coherente con su sistema** — poder explicar por qué cada uno es correcto en su casa es el músculo de la materia. Releé del recorrido **P6 §9**: última relectura — no te queda nada sin conquistar ahí.

## ▶️ Próximo paso

Se terminó el andamiaje. API completa, examinada 10/10, cada mecanismo con su porqué. Queda la pregunta del principio de todo: **¿podés hacerlo sin la guía?** Decime **"arranquemos etapa 10"** — el capstone: un tercer recurso completo, solo requisitos y batería de aceptación, cero pasos. El vuelo solo. *(Si preferís cortar acá la sesión: es el corte natural — el capstone merece energía fresca.)*

---

**FIN DE LA ETAPA 9 — v2**
