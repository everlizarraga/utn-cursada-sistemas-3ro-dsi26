# 🌱 Proyecto 0 (clase 04) — Etapa 7B: Identidad y futuro — equals, las dos listas, y la base de datos que viene

> **⭐ Extensión nacida de TUS preguntas** — dos ruidos que te hicieron los `contains(mascota)` y el objeto-en-dos-listas, más tu anticipación de "esto con una BD real va a ser otra historia". Los tres instintos eran correctos, y cada uno tiene doctrina con nombre. Acá se saldan.
>
> **Objetivo:** entender **cómo se comparan las entidades** (y por qué hoy funciona de casualidad estructural), instalar la doctrina de la **igualdad por identidad de dominio**, ver el **panorama honesto de la persistencia real** — y sellar con las manos una **fuga de implementación** que tu propio análisis destapó.
>
> **El momento clave:** cuando descubras que tu propio archivo ya contenía las dos filosofías de igualdad en tensión — una tuya, una heredada — sin que nadie te lo dijera.
>
> **Pre-requisito:** Etapa 7 completa (con tu `MascotaServiceImpl` en su versión de fases).
>
> **Tiempo estimado:** 30-35 minutos.

---

## 🗺️ El mapa general — qué se toca hoy

```
        Controllers ──► Services ──► Repositories
        (sin cambios)      │              │
                           │              ├── PropietarioRepository (interfaz)
              MascotaServiceImpl ★        │    ★ +findOwnerOf(mascota)  ← la
              (su findPropietarioOwner    │       fuga, sellada acá
               se muda detrás del         │
               contrato)                  └── InMemoryPropietarioRepository
                           │                   ★ implementa findOwnerOf
                           ▼
                        DOMINIO
                        ★ las entidades ganan una POLÍTICA de
                          identidad — cuál, es TU decisión (Parte 2)
```

Movimiento total: **2 entidades** ganan una anotación, **1 método nuevo** en el contrato del repo + su impl, y **1 método privado del service** se reduce a delegar. Nada más.

## 🧭 Mapa de esta etapa

1. **EL SISTEMA COMPLETO:** el mecanismo de la igualdad, la fragilidad, la doctrina, y el panorama de la persistencia.
2. Manos 1: la política de identidad — decisión con dos caminos.
3. Manos 2: sellar la fuga (`findOwnerOf` al contrato).
4. Verificación + la pregunta de la Etapa 5, revisitada.
5. Criterios + checkpoint + registro + 🔗.

---

## 🧠 Parte 1: EL SISTEMA COMPLETO (leé toda esta parte antes de codear)

### 1a. El mecanismo — por qué `contains(mascota)` funciona HOY

La cadena completa, que nadie te había mostrado: `List.contains(x)` recorre la lista preguntando `elemento.equals(x)` a cada uno → y como tus entidades **no redefinen `equals`**, heredan el de `Object` → que compara **por referencia**: *"¿sos literalmente EL MISMO objeto en memoria?"*.

¿Y por qué funciona, entonces? Por el lado B de la perla del save, jugando a favor — **no hay dos objetos con la misma info: hay UN objeto en dos listas**:

```
   MEMORIA (heap)                       Repo de Mascotas
                                        ┌───────────────────┐
   ┌──────────────────┐  ◄─────────────── [ ref, ref, ... ]
   │  Mascota id=1    │                 └───────────────────┘
   │  "Firulais"      │                 Lista de su Propietario
   │  (ÚNICA          │  ◄─────────────── [ ref, ... ]
   │   instancia)     │                 └───────────────────┘
   └──────────────────┘
        dos listas, dos referencias… UN solo objeto.
        contains por referencia → lo encuentra: es él mismo.
```

Tu ruido de *"esa mascota pudo venir del repo, o del propietario, o de otro lado — serían objetos diferentes"* era la pregunta correcta: hoy NO son diferentes **de casualidad estructural** (todo el sistema comparte instancias vivas). El día que algún camino *reconstruya* una mascota — y ya viste en el experimento de la Etapa 7 lo fácil que es — el `contains` por referencia **falla en silencio**: mismo id, misma info, objeto distinto → "no está".

### 1b. Tu propio archivo, en tensión (el caso de estudio)

Lo mejor de esta historia: **las dos filosofías ya conviven en tu `MascotaServiceImpl` / `Propietario`**, sin que nadie lo planeara:

```
  tu eliminarMascota:                      el findPropietarioOwner (heredado
  removeIf(m -> Objects.equals(            del diseño de la cátedra):
      m.getId(), mascota.getId()))         .filter(p -> p.getMascotas()
                                                          .contains(mascota))
  → compara POR ID: desconfía de la        → compara POR REFERENCIA: confía
    referencia. Robusto ante                 en la instancia única. Frágil
    reconstrucciones. (Lo escribiste         ante reconstrucciones. (El
    así por instinto.)                       findComercioOwner del profe
                                             tiene exactamente la misma
                                             fragilidad.)
```

Tu instinto del `removeIf` tenía razón. La doctrina que lo formaliza viene ahora.

### 1c. La doctrina — igualdad por identidad de dominio

> **Dos entidades son "la misma" si tienen el mismo id — vengan de donde vengan, sean o no la misma instancia.** La identidad de una entidad es su id, no su dirección de memoria ni sus datos (Ana puede cambiar de teléfono y sigue siendo Ana).

Se implementa redefiniendo `equals` y `hashCode` **basados en el id** — y Lombok lo hace en una línea: `@EqualsAndHashCode(of = "id")`. Con eso, `contains`, `remove`, y todo lo que compare, pasa a preguntar por id.

> 🎛️ **Ficha `@EqualsAndHashCode`:** `of` (qué campos usar — para entidades: `"id"`) · `exclude` (el inverso) · `callSuper` (incluir la herencia — solo reconocer). ⚠️ **Sin `of`, usa TODOS los campos** — en `Propietario` eso metería la lista de mascotas adentro del equals: carísimo y conceptualmente mal (los datos no son la identidad).

**La sutileza obligatoria — el null:** dos entidades recién nacidas (`id = null`, todavía sin `save`) tendrían "el mismo id" → serían *"iguales"* entre sí para este equals. Regla práctica que lo desactiva: **las comparaciones de identidad se hacen entre entidades ya persistidas** (con id) — que es exactamente tu caso: todo lo que entra a un `contains` en tu sistema ya pasó por el save. Sabelo, nombralo si te lo preguntan, y listo.

*(¿Y los DTOs? NO se tocan: los records ya traen equals **por valor** — todos los campos — y para un DTO eso es lo correcto: un DTO no tiene identidad, ES sus datos. Entidad = igualdad por id; DTO = igualdad por valor. Dos mundos, dos doctrinas — pregunta de checkpoint.)*

**Y una aclaración de alcance:** esta doctrina responde *qué significa* que dos entidades sean la misma. *Cómo instalarla en el código* tiene DOS caminos defendibles — el equals de dominio es uno; la comparación explícita por id en cada punto es el otro. La decisión entre ambos es tuya, con sus trade-offs completos sobre la mesa: Parte 2.

### 1d. El panorama de la persistencia — tu modelo mental, verificado

Tu anticipación era correcta casi punto por punto. El contraste honesto:

```
  HOY (memoria)                          MAÑANA (base de datos relacional)
  ─────────────                          ─────────────────────────────────
  UN objeto vivo, compartido             FILAS en tablas — nada "contiene"
  en dos listas por referencia           a nada:

  Propietario ──► [Mascota,...]          tabla propietarios: id │ nombre │ tel
  Repo mascotas ─► [Mascota,...]         tabla mascotas:     id │ nombre │ especie │ propietario_id
                                                                              └──── clave FORÁNEA (FK):
                                                                                    la relación es un id,
                                                                                    tal como intuiste
  el "dueño de la mascota" se            el dueño se consulta POR FK
  averigua recorriendo listas            (una query directa)

  el objeto llega "entero"               al traer un propietario, alguien
                                         REHIDRATA su lista (busca las
                                         mascotas por su FK y las puebla)
```

Tres confirmaciones a tus preguntas: **(1)** sí — la BD guardaría la relación como ids (FK), no objetos adentro de objetos; **(2)** "DAO" es esencialmente **otro nombre histórico del mismo rol** que tu repositorio (Data Access Object — misma idea, otra época; los vas a ver usados casi como sinónimos); **(3)** la rehidratación que describís ("pegarle al repo de mascotas y poblarlas") es exactamente lo que automatiza un **ORM** (JPA/Hibernate en el mundo Spring): mapeás las entidades con anotaciones y él arma el grafo por vos — con sus propios trade-offs, que la materia cubrirá cuando toque. Tu cabeza ya está donde el programa va a estar en unas clases. Y una confirmación más, de tu propia pregunta: **la base de datos compara por id, SIEMPRE** — claves primarias y foráneas, `WHERE propietario_id = ?`; no existe "comparar objetos" en SQL. Con el refactor de 1e, este debate entero queda **confinado a la implementación en memoria**: la impl de BD no va a comparar objetos jamás.

### 1e. La fuga de implementación — lo que tu análisis destapó

Y acá la consecuencia filosa de tu razonamiento. Mirá DÓNDE vive el scan O(n):

```
  findPropietarioOwner  =  "recorrer TODOS los propietarios preguntando
                            por contains"
                        =  el CÓMO-se-busca-un-dueño... EN MEMORIA.
                           Es un detalle de implementación del medio.

  ¿Y dónde vive?  →  EN EL SERVICE.   ← la fuga.
```

El service se supone agnóstico del medio de persistencia — pero tiene incrustada la estrategia de búsqueda *de la memoria*. Con BD, buscar al dueño sería una query por FK… y habría que reescribir **el service**, no solo el repo: la promesa de la Etapa 5 ("con BD solo cambian las impl de los repos" — tu checkpoint 6) está **rota exactamente ahí**. La cura es chica y elegante: **empujar la búsqueda detrás del contrato** — que "encontrá al dueño de esta mascota" sea una pregunta que se le hace AL REPO, y que cada implementación la responda a su manera (la de memoria con su stream; la futura de BD con su query). El service no se entera jamás. Manos a la obra.

---

## 🔧 Parte 2: Manos 1 — la política de identidad (decisión con dos caminos)

El problema de 1a-1b tiene dos curas defendibles. **Decisión de diseño TUYA** — del calibre de tus huérfanos y tu PUT-mudanza: elegís, implementás, y justificás en comentario. Los trade-offs completos, cara a cara:

```
  CAMINO A — EQUALS DE DOMINIO           CAMINO B — COMPARACIÓN EXPLÍCITA
  (la identidad se declara UNA vez)      (cada comparación dice qué compara)
  ─────────────────────────────────      ──────────────────────────────────
  @EqualsAndHashCode(of = "id")          las entidades no se tocan; TODA
  sobre cada entidad — y contains,       comparación de entidades se
  remove, Set, Map, distinct()...        escribe por id, a mano:
  TODO el ecosistema de Java respeta       Objects.equals(a.getId(),
  tu identidad GRATIS, para siempre.                     b.getId())

  SU RIESGO (concentrado): la            SU RIESGO (distribuido): la
  sutileza del null — dos entidades      decisión vive repetida en cada
  pre-save (id=null) dan "iguales".      punto. Un contains/remove
  Se desactiva con UNA regla,            "inocente" escrito mañana compila
  documentable: la identidad se          perfecto… y compara por
  compara entre persistidas.             REFERENCIA: falla en silencio.
                                         Y límite duro: en un Set o Map de
                                         entidades NO alcanza — esas
                                         colecciones usan equals/hashCode
                                         por adentro, no tu lambda.
```

Fijate la simetría honesta: **los dos caminos tienen un modo de falla por olvido** — A lo concentra en una sutileza única; B lo reparte por todo el código. No hay opción sin riesgo; hay riesgos de forma distinta.

**Camino A, el código** (una anotación por entidad — la ficha 🎛️ está en 1c):

```java
// 📁 models/entities/Mascota.java  y  Propietario.java   (sobre la clase)
@EqualsAndHashCode(of = "id")        // import lombok.EqualsAndHashCode
```

**Camino B, el código** (las entidades quedan; las comparaciones cambian — TODAS):

```java
// el patrón, donde sea que se comparen entidades:
.anyMatch(m -> Objects.equals(m.getId(), mascota.getId()))
// (tu eliminarMascota con removeIf por id YA es este camino — lo
//  escribiste por instinto antes de que la decisión tuviera nombre.)
```

**La exigencia común a ambos: CONSISTENCIA.** Tu estado actual — `removeIf` por id conviviendo con `contains` por referencia — es exactamente la mezcla que ningún camino tolera. Elegí uno, aplicalo en **todos** los puntos de comparación de entidades, y dejá el comentario del porqué.

**Predicción, cualquiera sea tu camino:** con la política instalada, ¿algún comportamiento observable cambia HOY? → **No** — la instancia única hace coincidir referencia e id, siempre. Lo que instalaste es la **red para el futuro**: la reconstrucción que algún día se cuele por cualquier camino.

## 🔧 Parte 3: Manos 2 — sellar la fuga

📍 **Dónde** — tres archivos, en cadena: el contrato gana la pregunta, la impl la responde, el service se olvida del cómo:

```
┌─ 📁 repositories/PropietarioRepository.java ───────────────┐
│  ··· findAll, findById, save, delete: sin cambios ···      │
│  ╔═ MÉTODO NUEVO en el contrato ═══════════════╗           │
│  ║ Optional<Propietario> findOwnerOf(Mascota)  ║           │
│  ╚═════════════════════════════════════════════╝           │
└────────────────────────────────────────────────────────────┘
```

```java
// El contrato (la interfaz):
Optional<Propietario> findOwnerOf(Mascota mascota);
// ↑ Optional, como el findById: "buscá al dueño — y puede que no haya".
//   QUÉ significa "no hay dueño" (404, error interno, da igual) NO lo
//   decide el repo: lo decide quien pregunta. Capas.
```

```java
// 📁 repositories/inmemory/InMemoryPropietarioRepository.java   (implementar)
// — elegí LA VARIANTE de tu camino de la Parte 2:

// Variante Camino A (equals de dominio — el contains ya compara por id):
@Override
public Optional<Propietario> findOwnerOf(Mascota mascota) {
    return propietarios.stream()
        .filter(p -> p.getMascotas().contains(mascota))
        .findFirst();
}

// Variante Camino B (explícito — la comparación dice qué compara):
@Override
public Optional<Propietario> findOwnerOf(Mascota mascota) {
    return propietarios.stream()
        .filter(p -> p.getMascotas().stream()
            .anyMatch(m -> Objects.equals(m.getId(), mascota.getId())))
        .findFirst();
}

// ↑ En ambas: EL MISMO scan de siempre — pero ahora vive donde corresponde:
//   esta ES la estrategia de búsqueda DE LA MEMORIA. La futura impl con BD
//   hará su query por FK acá adentro, y nadie más se entera.
```

```java
// 📁 services/impl/MascotaServiceImpl.java   (el privado se reduce a delegar)
private Propietario findPropietarioOwner(Mascota mascota) {
    return this.propietarioRepository.findOwnerOf(mascota)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No se encontró el propietario para la mascota " + mascota.getId()));
    // ↑ El service conserva SU parte: decidir qué significa el vacío
    //   (la excepción y su mensaje son política del flujo, no del repo).
    //   El CÓMO se busca, ya no es asunto suyo.
}
```

**Inventario del refactor:** 1 firma en la interfaz, 1 método en la impl, 1 cuerpo reescrito en el service (sus llamadores no cambian — la firma privada es la misma). Diez líneas, fuga sellada.

## ✅ Parte 4: Verificación — y la pregunta de la Etapa 5, revisitada

**Regresión:** corré tu lista de verificación de la Etapa 7 completa (PUT, mudanza, DELETE, consistencia cruzada) → **todo sigue verde** — refactor con red: comportamiento idéntico, estructura mejor.

Y el cierre conceptual: volvé a tu respuesta del checkpoint 6 de la Etapa 5 (*"el día que llegue la BD, ¿qué archivos cambian?"*). Antes de hoy, la respuesta honesta tenía un asterisco vergonzante (…"y también el pedazo del service que escanea propietarios"). **Ahora es limpia:** cambian las implementaciones de los repos — punto. Ese asterisco que borraste es la medida exacta del valor de esta etapa.

## ✅ Criterios de "Etapa 7B completa"

- [ ] Podés explicar la cadena `contains` → `equals` → referencia, y por qué funcionaba "de casualidad estructural".
- [ ] Tu política de identidad: **elegida, aplicada con consistencia en TODOS los puntos, justificada en comentario** — y sabés recitar el riesgo del camino que elegiste Y el del que descartaste.
- [ ] Entidad = igualdad por id · DTO = igualdad por valor — y el porqué de cada una.
- [ ] La fuga sellada: `findOwnerOf` en el contrato, el scan en la impl (en la variante de tu camino), la excepción en el service.
- [ ] Regresión verde + la respuesta del "¿qué cambia con BD?" sin asteriscos.

## ✅ Checkpoint

*Recall:*
1. ¿Qué compara el `equals` heredado de `Object`, qué política instala cada uno de los dos caminos, y por qué HOY no hay diferencia observable entre nada de esto?
2. ¿Por qué `@EqualsAndHashCode` pelado (sin `of`) sería un error en `Propietario`? Dos razones de naturalezas distintas.
3. En el refactor: ¿por qué el repo devuelve `Optional` y la excepción quedó en el service? ¿Qué regla de capas codifica ese reparto?

*Decidí y justificá:*
4. ¿Por qué los records-DTO NO llevan `@EqualsAndHashCode(of = "id")`? Formulá la doctrina completa de los dos mundos.
5. Mañana piden "todas las mascotas del propietario 4". ¿Qué firma nueva le darías a qué contrato, y qué haría cada implementación (memoria vs BD)? — Estás diseñando con la fuga en mente: esa es la gracia.
6. Un compañero dice: "el equals por id es peligroso — dos mascotas distintas con datos distintos serían 'iguales' si un bug les da el mismo id". Tiene un punto. Respondele distinguiendo qué protege cada capa (¿de quién es la culpa si dos entidades comparten id?).

## 📝 Registro de la etapa

Tu línea — y esta etapa nació entera de tu registro anterior: tus tres ruidos eran tres doctrinas. Anotá eso también.

## 🔗 Conexión con la clase

Abrí el `findComercioOwner` de `ProductoServiceImpl` en el `sales-service` y auditalo con tus ojos de hoy: **misma fragilidad** (contains por referencia, sin política de identidad alguna — ni equals de dominio ni comparaciones explícitas: verificalo en sus entidades) y **misma fuga** (el scan vive en el service). Tu proyecto, después de esta etapa, está estructuralmente un paso adelante del material de la clase en identidad Y en el contrato de búsqueda — tercera vez que pasa (advice con logger, handler del JSON, y ahora esto). En una defensa, poder decir *"moví la búsqueda del dueño al contrato del repositorio para que el cambio de medio de persistencia no toque el service"* es una frase de diseño de las que dejan marca.

## ▶️ Próximo paso

Volvé al camino: **"arranquemos etapa 8"** (PATCH) si venís en orden — con las fases y la identidad ya en la mochila. Todo lo de hoy reaparece en el capstone: las fases en cada método de escritura, y la decisión D1 (la dirección de la relación de Turno) ahora la vas a mirar con ojos de FK y rehidratación.

---

**FIN DE LA ETAPA 7B**
