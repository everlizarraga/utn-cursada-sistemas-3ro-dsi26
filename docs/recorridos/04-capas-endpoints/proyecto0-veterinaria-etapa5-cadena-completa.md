# 🌱 Proyecto 0 (clase 04) — Etapa 5: La cadena completa — repositorios y el dominio de verdad

> **Objetivo:** que tu API **recuerde**: entra la capa de datos (repositorios en memoria), entra el **dominio real** de la veterinaria (se acabó el patio de juguetes), y una request atraviesa por primera vez **las cuatro capas** de punta a punta — la arquitectura entera del recorrido, corriendo en tu máquina.
>
> **El momento clave:** el POST que crea un propietario, el GET que lo encuentra… y el reinicio que lo mata. Vas a ver con tus ojos qué significa "persistencia en memoria" — y por qué cada pieza del viaje vive donde vive.
>
> **Pre-requisito:** Etapa 4 completa.
>
> **Tiempo estimado:** 60-75 minutos (la más larga hasta ahora — es la etapa donde todo se junta).
>
> **Andamiaje:** 🟨 — la parte guiada sigue detallada, pero tu turno ya no trae template: especificación + hints. El destete empezó en serio.

---

## 🗂️ Dónde estamos — los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── VeterinariaApplication.java              (no se toca)
    ├── controllers/
    │   ├── PingController.java                  (queda como museo — ver Parte 10)
    │   ├── PropietarioController.java           (NUEVO — Parte 5)
    │   └── MascotaController.java               (NUEVO — tu turno)
    ├── models/entities/                         (📦 NUEVO — el dominio)
    │   ├── Propietario.java                     (NUEVO — dado, Parte 1)
    │   └── Mascota.java                         (NUEVO — dado, Parte 1)
    ├── dtos/
    │   ├── propietario/                         (📦 NUEVO sub-package)
    │   │   ├── PropietarioCreateRequest.java    (NUEVO — Parte 4)
    │   │   └── PropietarioResponse.java         (NUEVO — Parte 4)
    │   └── mascota/                             (📦 NUEVO — tu turno)
    ├── repositories/                            (📦 NUEVO)
    │   ├── PropietarioRepository.java           (NUEVO — Parte 2)
    │   ├── MascotaRepository.java               (NUEVO — tu turno)
    │   └── inmemory/
    │       ├── InMemoryPropietarioRepository.java  (NUEVO — Parte 3)
    │       └── InMemoryMascotaRepository.java      (NUEVO — tu turno)
    ├── services/ (+ impl/)
    │   ├── PropietarioService.java (+Impl)      (NUEVO — Parte 4)
    │   └── MascotaService.java (+Impl)          (NUEVO — tu turno)
    └── utils/
        └── GeneradorIdSecuencial.java           (NUEVO — Parte 2)
```

---

## 🧭 Mapa de esta etapa

1. El dominio entra en escena (dado — leelo, no lo diseñás).
2. El contrato de datos: la interfaz del repositorio.
3. La implementación en memoria: `@Repository`.
4. El recableado: service + DTOs reales.
5. El controller real: `/veterinaria/propietarios`.
6. La cadena viva: el viaje de cuatro capas, con tus ojos.
7. Experimento 1: el 99 que no existe.
8. Experimento 2: la memoria efímera.
9. Tu turno 🟨: Mascota completa — la relación, la regla, el trade-off.
10. La limpieza del patio + criterios + checkpoint + registro + 🔗.

---

## 🐾 Parte 1: El dominio entra en escena

El dominio te lo doy resuelto — **no es la fricción de este proyecto** (modelar ya sabés; es lo tuyo desde la clase 03). Leelo con un solo lente: *qué NO tiene*.

```java
// 📁 src/main/java/.../veterinaria/models/entities/Propietario.java   (archivo NUEVO)
package com.practica.veterinaria.models.entities;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Propietario {
    @Setter private Long id;
    private final String nombre;
    private final String telefono;
    private final List<Mascota> mascotas = new ArrayList<>();

    public Propietario(Long id, String nombre, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
    }

    public void agregarMascota(Mascota mascota) {
        if (mascotas.size() >= 5) {
            throw new IllegalArgumentException(
                "Un propietario no puede tener más de 5 mascotas");
        }
        mascotas.add(mascota);
    }
    // ↑ LA regla de negocio del dominio — vive en la entidad, habla el idioma
    //   genérico de Java (IllegalArgumentException): el dominio no conoce
    //   HTTP, ni Spring, ni excepciones "del sistema". Guardá el detalle:
    //   en la Etapa 6 alguien va a tener que TRADUCIR este grito.
}
```

```java
// 📁 src/main/java/.../veterinaria/models/entities/Mascota.java   (archivo NUEVO)
package com.practica.veterinaria.models.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Mascota {
    @Setter private Long id;
    @Setter private String nombre;     // Setters: el PUT/PATCH de las
    @Setter private String especie;    // etapas 7-8 los van a necesitar.

    public Mascota(Long id, String nombre, String especie) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
    }
}
```

Lo que NO tiene, y es todo el punto: **cero Spring**. Ni una anotación del framework, ni un import de `org.springframework`. El dominio es **agnóstico** — la regla más repetida del árbol del recorrido, cumplida a rajatabla: estas clases vivirían igual en un programa de consola. Y el segundo detalle: `Propietario` conoce sus mascotas; **`Mascota` no conoce a su propietario** — relación en un solo sentido, la misma decisión (y el mismo precio, que vas a pagar en la Parte 9) que `Comercio`→`Producto` en el proyecto de la clase.

## 📜 Parte 2: El contrato de datos

Primero el utilitario de ids (no hay base de datos que los genere — los generamos nosotros):

```java
// 📁 src/main/java/.../veterinaria/utils/GeneradorIdSecuencial.java   (archivo NUEVO)
package com.practica.veterinaria.utils;

public class GeneradorIdSecuencial {
    private long ultimo = 0;
    public long siguiente() { return ++ultimo; }   // 1, 2, 3...
}
```

Y el contrato de la capa de datos — interfaz, como siempre:

```java
// 📁 src/main/java/.../veterinaria/repositories/PropietarioRepository.java   (archivo NUEVO)
package com.practica.veterinaria.repositories;

import com.practica.veterinaria.models.entities.Propietario;
import java.util.List;
import java.util.Optional;

public interface PropietarioRepository {

    List<Propietario> findAll();
    Optional<Propietario> findById(Long id);
    Propietario save(Propietario propietario);
    void delete(Propietario propietario);
}
// ↑ El contrato clásico (nombres en inglés = convención universal de repos).
//   Optional en el findById: la forma prolija de "buscá, y puede que no esté"
//   — sin null pelado. Su gracia completa aparece en la Parte 4.
```

## 🛠️ Parte 3: La implementación en memoria — `@Repository`

```java
// 📁 src/main/java/.../veterinaria/repositories/inmemory/InMemoryPropietarioRepository.java   (NUEVO)
package com.practica.veterinaria.repositories.inmemory;

import com.practica.veterinaria.models.entities.Propietario;
import com.practica.veterinaria.repositories.PropietarioRepository;
import com.practica.veterinaria.utils.GeneradorIdSecuencial;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryPropietarioRepository implements PropietarioRepository {
    // ↑ @Repository: la ficha de @Service (Etapa 4) aplica tal cual —
    //   bean estereotipado de la capa de datos, un solo atributo `value`.

    private final List<Propietario> propietarios = new ArrayList<>();
    // ↑ El "medio persistente" de hoy: una lista. Mientras la app viva, los
    //   datos viven. El Experimento 2 te muestra el reverso de esa moneda.

    private final GeneradorIdSecuencial generadorId = new GeneradorIdSecuencial();

    @Override
    public List<Propietario> findAll() {
        return new ArrayList<>(propietarios);
        // ↑ Copia DEFENSIVA: devuelvo una lista nueva, no MI lista interna.
        //   Nadie de afuera puede tocar mi almacenamiento por accidente.
    }

    @Override
    public Optional<Propietario> findById(Long id) {
        return propietarios.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst();
        // ↑ findFirst() ya devuelve Optional: lleno si hubo match, vacío si
        //   no. Encaja exacto con la firma del contrato — cero conversión.
    }

    @Override
    public Propietario save(Propietario propietario) {
        if (propietario.getId() == null) {              // sin id → es NUEVO:
            propietario.setId(generadorId.siguiente()); //   le asigno id
            propietarios.add(propietario);              //   y lo agrego.
            return propietario;
        }
        delete(propietario);                            // con id → actualización:
        propietarios.add(propietario);                  //   fuera la vieja,
        return propietario;                             //   adentro la nueva.
    }
    // ↑ Un solo save que resuelve alta Y modificación, decidiendo por el id.
    //   El id NUNCA viene de afuera (tu decisión de DTO de la Etapa 3):
    //   acá, y solo acá, se fabrica.

    @Override
    public void delete(Propietario propietario) {
        if (propietario.getId() == null) return;
        propietarios.removeIf(p -> p.getId().equals(propietario.getId()));
    }
}
```

## 🔌 Parte 4: El recableado — service + DTOs reales

Los DTOs, con las decisiones de la Etapa 3 aplicadas a conciencia:

```java
// 📁 src/main/java/.../veterinaria/dtos/propietario/PropietarioCreateRequest.java   (NUEVO)
package com.practica.veterinaria.dtos.propietario;

public record PropietarioCreateRequest(String nombre, String telefono) { }
// ↑ SIN id (lo fabrica el repo — lo que no está en el DTO no puede entrar).

// 📁 src/main/java/.../veterinaria/dtos/propietario/PropietarioResponse.java   (NUEVO)
package com.practica.veterinaria.dtos.propietario;

public record PropietarioResponse(Long id, String nombre, String telefono,
                                  int cantidadMascotas) { }
// ↑ CON id (el mundo lo necesita para las próximas operaciones) + un dato
//   calculado de cortesía. La ENTIDAD jamás sale: sale esto.
```

El contrato del service y su implementación — fijate cómo cada pieza de las etapas anteriores encuentra su lugar:

```java
// 📁 src/main/java/.../veterinaria/services/PropietarioService.java   (NUEVO)
package com.practica.veterinaria.services;

import com.practica.veterinaria.dtos.propietario.*;
import java.util.List;

public interface PropietarioService {
    List<PropietarioResponse> findAll();
    PropietarioResponse findById(Long id);
    PropietarioResponse create(PropietarioCreateRequest request);
}
```

```java
// 📁 src/main/java/.../veterinaria/services/impl/PropietarioServiceImpl.java   (NUEVO)
package com.practica.veterinaria.services.impl;

import com.practica.veterinaria.dtos.propietario.*;
import com.practica.veterinaria.models.entities.Propietario;
import com.practica.veterinaria.repositories.PropietarioRepository;
import com.practica.veterinaria.services.PropietarioService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PropietarioServiceImpl implements PropietarioService {

    private final PropietarioRepository propietarioRepository;

    public PropietarioServiceImpl(PropietarioRepository propietarioRepository) {
        this.propietarioRepository = propietarioRepository;
    }
    // ↑ La Etapa 4, un piso más abajo: ahora es el SERVICE quien declara
    //   dependencias por constructor — y pide LA INTERFAZ del repo. El
    //   framework ya construyó el @Repository; se lo inyecta. Misma
    //   mecánica, nueva capa.

    @Override
    public List<PropietarioResponse> findAll() {
        return propietarioRepository.findAll().stream()
            .map(this::toResponse)             // entidad → DTO, en masa
            .toList();                         // (this::toResponse = la
    }                                          //  referencia a método del seminario)

    @Override
    public PropietarioResponse findById(Long id) {
        return toResponse(getPropietarioOrThrow(id));
    }

    @Override
    public PropietarioResponse create(PropietarioCreateRequest request) {
        if (request == null || request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        // ↑ Regla del FLUJO, en el service — Etapa 4. (Sigue saliendo como
        //   500 al mundo: deuda del traductor, la Etapa 6 se acerca.)

        Propietario propietario = new Propietario(null, request.nombre().trim(),
                                                  request.telefono());
        // ↑ EL SERVICE INSTANCIA EL DOMINIO — la regla estrella del recorrido
        //   (P4 §4), por primera vez en tus dedos. Id en null: lo pone el repo.

        propietarioRepository.save(propietario);
        return toResponse(propietario);
    }

    // ——— piezas privadas ———

    private Propietario getPropietarioOrThrow(Long id) {
        return propietarioRepository.findById(id).orElseThrow();
        // ↑ Optional cerrando su ciclo: "dame el valor; si está vacío, lanzá".
        //   El orElseThrow() PELADO lanza NoSuchElementException — genérica,
        //   fea, y ya sabés qué le va a pasar al llegar arriba. Es A PROPÓSITO:
        //   el Experimento 1 la caza, y la Etapa 6 la jubila.
    }

    private PropietarioResponse toResponse(Propietario p) {
        return new PropietarioResponse(p.getId(), p.getNombre(), p.getTelefono(),
                                       p.getMascotas().size());
        // ↑ El "mapper": entidad → DTO, campo por campo. Vive acá (podría ser
        //   clase aparte — el 👀 del recorrido P6 §5).
    }
}
```

## 🚪 Parte 5: El controller real

Archivo nuevo — los recursos de verdad no viven en el patio:

```java
// 📁 src/main/java/.../veterinaria/controllers/PropietarioController.java   (archivo NUEVO)
package com.practica.veterinaria.controllers;

import com.practica.veterinaria.dtos.propietario.*;
import com.practica.veterinaria.services.PropietarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/veterinaria/propietarios")   // ruta padre: servicio/recurso —
public class PropietarioController {           // el patrón del proyecto de la clase

    private final PropietarioService propietarioService;

    public PropietarioController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    @GetMapping
    public List<PropietarioResponse> getAll() { return propietarioService.findAll(); }

    @GetMapping("/{id}")
    public PropietarioResponse getById(@PathVariable Long id) {
        return propietarioService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropietarioResponse create(@RequestBody PropietarioCreateRequest request) {
        return propietarioService.create(request);
    }
}
// ↑ Cada línea de este archivo la construiste en alguna etapa. Ninguna
//   debería sorprenderte — si alguna lo hace, ahí está tu relectura.
```

## 🤯 Parte 6: La cadena viva

**Predicción antes de correr:** al arrancar, ¿cuántos beans TUYOS construye Spring, y en qué orden obligado? (Contá: repos, services, controllers…)

Levantá y ejecutá la secuencia en Postman:

1. `POST /veterinaria/propietarios` con `{"nombre":"Ana","telefono":"11-5555"}` → **201**, `{"id":1,"nombre":"Ana","telefono":"11-5555","cantidadMascotas":0}` — **¿quién puso el 1?** Recorré mentalmente el viaje hasta encontrarlo.
2. `POST` de un segundo propietario → id **2**.
3. `GET /veterinaria/propietarios` → **200**, array con los dos.
4. `GET /veterinaria/propietarios/1` → **200**, Ana.

Eso que acaba de pasar es **la arquitectura completa del recorrido, viva y tuya**:

```
POST {"nombre":"Ana",...}
   │
   ▼
PropietarioController      ← PRESENTACIÓN: tradujo el JSON (Jackson), delegó
   │
   ▼
PropietarioServiceImpl     ← DOMINIO intermedio: validó (flujo), INSTANCIÓ
   │            │             el dominio, orquestó, mapeó a DTO
   ▼            ▼
Propietario   InMemoryPropietarioRepository   ← DOMINIO puro / DATOS:
(la entidad,    (fabricó el id, guardó            cada uno en lo suyo,
 agnóstica)      en su lista)                     sin conocerse de más
   │
   ▼
201 + PropietarioResponse como JSON
```

Cuatro capas, cada pieza construida por vos, cada flecha respetando el árbol de conocimiento. La teoría del recorrido acaba de correr en tu puerto 8083.

## 🧨 Parte 7: Experimento 1 — el 99 que no existe

**Predicción:** `GET /veterinaria/propietarios/99` — ¿404? ¿400? ¿500? ¿Quién decide?

**500** — y en consola, el `NoSuchElementException` de tu `orElseThrow()` pelado, viajando sin que nadie lo atrape. Ya son **tres deudas del traductor** acumuladas, anotalas juntas: *(1)* validación de flujo → 500 (debería 400), *(2)* recurso inexistente → 500 (debería **404**), *(3)* y en la Parte 9 vas a sumar la regla del dominio → 500 (debería 400). Tres mentiras del mismo agujero. La Etapa 6 existe para las tres.

## 🧨 Parte 8: Experimento 2 — la memoria efímera

Con tus dos propietarios cargados: **frená la app** (`Ctrl+C`) y levantala de nuevo. **Predicción:** `GET /propietarios` — ¿qué devuelve?

**`[]`** — array vacío. Ana y compañía murieron con el proceso: la "persistencia" en memoria vive exactamente lo que vive la JVM. No es un bug — es el contrato del repo de hoy, y ahora lo *sentiste*. Dos consecuencias: cada sesión de pruebas arranca de cero (molesto — la **semilla** de la Etapa 9 existe para eso), y la persistencia real (base de datos) es otra implementación del MISMO contrato `PropietarioRepository` — llegará en la materia, y cuando llegue, ¿qué archivos tuyos cambian? (Guardá la respuesta: es pregunta de checkpoint.)

## ✍️ Parte 9: Tu turno 🟨 — Mascota completa

El salto grande de la etapa, sin template — especificación + hints. Construí el recurso Mascota entero: **repo** (interfaz + in-memory, calco estructural del de Propietario, con su propio generador) · **DTOs** en `dtos/mascota/` (`MascotaCreateRequest`: `propietarioId`, `nombre`, `especie` — pensá los tipos con la Etapa 3 en la mano; `MascotaResponse`: `id`, `propietarioId`, `nombre`, `especie`) · **service** (interfaz + impl) · **controller** (`/veterinaria/mascotas`: GET all, GET /{id}, POST).

El service es donde está la carne — su `create` es tu primera orquestación **con relación**:

1. Validar la entrada (reglas de flujo — `IllegalArgumentException` por ahora).
2. **Resolver** el `propietarioId` → objeto `Propietario` (¿de dónde lo sacás? ¿y si no existe?).
3. Instanciar la `Mascota` (id null).
4. **Asociar**: `propietario.agregarMascota(mascota)` — ⚠️ puede explotar la regla de las 5.
5. Persistir la mascota en SU repo.
6. Devolver el DTO… y acá el hueso: `MascotaResponse` pide `propietarioId`, **y la Mascota no conoce a su propietario**.

💡 **Hints del hueso (escalonados — quemalos en orden):**
- **H1:** el proyecto de la clase resolvió exactamente esto — un objeto cuyo dueño hay que averiguar sin que el objeto lo sepa. ¿Cómo se llamaba ese método y qué recorría?
- **H2:** necesitás el repo de propietarios (tu service va a inyectar **dos** repos) y preguntarle a cada propietario si su lista `getMascotas()` contiene a esta mascota.
- **H3:** `propietarioRepository.findAll().stream().filter(p -> p.getMascotas().contains(mascota)).findFirst().orElseThrow()` — búsqueda lineal. Ese costo O(n) es **el precio de la relación unidireccional**: nombralo así, con todas las letras — es trade-off de parcial (P6 §4 del recorrido).

**Resultado esperado:** `POST /veterinaria/mascotas` `{"propietarioId":1,"nombre":"Firulais","especie":"perro"}` → **201** con `propietarioId: 1` · `GET /veterinaria/propietarios/1` → `cantidadMascotas: 1` (¡tu asociación, verificada por el otro recurso!) · `propietarioId: 99` → **500** (deuda 2, `NoSuchElementException`) · la **sexta** mascota de Ana → **500** con *"Un propietario no puede tener más de 5 mascotas"* en consola (deuda 3: la regla del dominio gritando en su idioma, sin traductor) · JSON roto → 400 (ese sí, de fábrica).

## 🧹 Parte 10: La limpieza del patio

`PingController` cumplió su ciclo: los recursos reales ya tienen casa propia. Decisión tuya: podarlo, o dejarlo como museo de las etapas 1-3 (con `TurnoRequestClass` y compañía). Si lo dejás, una línea de comentario arriba declarándolo laboratorio — que el vos-del-futuro no lo confunda con producción.

## ✅ Criterios de "Etapa 5 completa"

- [ ] El dominio está y podés explicar sus dos "no tiene": ni Spring, ni relación inversa.
- [ ] Propietario completo y probado: POST con id fabricado, GET all, GET by id.
- [ ] Los dos experimentos hechos; las **tres deudas del traductor** anotadas juntas, con el código que debería corresponder a cada una.
- [ ] Mascota completa: la relación resuelta, la regla de las 5 explotando en la sexta, y el `findPropietarioOwner` escrito y **nombrado como trade-off**.
- [ ] `cantidadMascotas` del propietario refleja las asociaciones — verificado cruzado.

## ✅ Checkpoint

*Recall:*
1. ¿Quién fabrica los ids, en qué método exacto, y por qué ahí y no en el service o el DTO?
2. ¿Por qué `findById` devuelve `Optional` y no la entidad o null? ¿Qué te permitió encadenar en el service?
3. Recitá el viaje del POST de la Parte 6: las cuatro capas, qué hace cada una, y las dos cosas que el service (y solo él) hizo.

*Decidí y justificá:*
4. El dominio no tiene ni un import de Spring. ¿Qué compra esa pureza? Da dos escenarios concretos donde la pagarías caro si `Propietario` fuera un bean con anotaciones.
5. El día que llegue la base de datos real: recorré TU árbol de archivos y marcá exactamente cuáles cambian y cuáles no. ¿Qué decisiones de hoy hicieron la lista de cambios tan corta?
6. `findPropietarioOwner` recorre todo por cada mapeo. Un compañero propone "agregale `propietario` como atributo a Mascota y listo". Defendé el diseño actual — y concedé honestamente qué ganaría él. (Las dos posturas valen; lo evaluable es el porqué.)
7. Borrás una mascota del `MascotaRepository`… ¿quedó viva en la lista de su propietario? Investigalo en TU código y decidí: ¿es un bug, o una decisión pendiente? ¿Qué haría falta para que el delete sea consistente? *(No lo arregles todavía — llega con el DELETE real de la Etapa 7. Pero pensalo hoy.)*
8. Las tres deudas del traductor: listalas con el código HTTP que le corresponde a cada una y DÓNDE nace cada excepción. (Es, literalmente, el plano de la próxima etapa.)

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Hoy la conexión es un espejo de cuerpo entero. Abrí lado a lado tu `InMemoryPropietarioRepository` y el `InMemoryProductoRepository` del `sales-service`: son la misma criatura (lista + generador + copia defensiva + save-que-decide). Después tu `MascotaServiceImpl.create` contra su `ProductoServiceImpl.create`: validar → resolver por id → instanciar → asociar → guardar → mapear — **el mismo flujo, paso por paso**, y su `findComercioOwner` contra tu `findPropietarioOwner`: el mismo trade-off, con otros nombres. La única diferencia estructural que te queda por conquistar: sus excepciones tienen nombre propio (`ResourceNotFoundException`, `BusinessException`) y las tuyas son genéricas. Releé del recorrido **P6 §3 y §4 completos** — a esta altura no deberían explicarte nada: deberían *confirmarte*.

## ▶️ Próximo paso

Tenés la arquitectura entera funcionando… y **tres mentiras saliendo por el puerto 8083**: todo error, sea culpa de quién sea, sale disfrazado de 500. El cliente que manda un propietario inexistente, el que rompe la regla de las 5 y el que manda el nombre vacío reciben la misma respuesta inútil. Decime **"arranquemos etapa 6"** y construimos el traductor: excepciones con nombre propio + el manejador global que convierte cada grito interno en el código HTTP que corresponde — el `@RestControllerAdvice` que el recorrido te prometió y tu API está pidiendo a gritos (literalmente).

---

**FIN DE LA ETAPA 5**
