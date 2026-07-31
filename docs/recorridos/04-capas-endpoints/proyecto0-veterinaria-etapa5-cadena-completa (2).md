# 🌱 Proyecto 0 (clase 04) — Etapa 5: La cadena completa — repositorios y el dominio de verdad

> **Objetivo:** que tu API **recuerde**: entra la capa de datos (repositorios en memoria), entra el **dominio real** de la veterinaria (se acaba el patio de juguetes), y una request atraviesa por primera vez **las cuatro capas** de punta a punta.
>
> **Cómo trabaja esta etapa (v2):** el plano completo PRIMERO — todas las piezas, los principios que las gobiernan, y **los precios y límites anunciados de entrada** (incluido el estado honesto de los errores). Después se construye: Propietario guiado entero, Mascota tuya.
>
> **Pre-requisito:** Etapa 4 completa.
>
> **Tiempo estimado:** 60-75 minutos (la más larga hasta ahora — es donde todo se junta).
>
> **Andamiaje:** 🟨 — la parte guiada sigue detallada; tu turno ya no trae template: especificación + hints.

---

## 🗺️ El mapa general — hoy nace la arquitectura entera

Hasta ayer tenías controllers y services jugando con DTOs de juguete. Hoy se construye TODO esto (★ = nace en esta etapa):

```
              REQUEST                                RESPONSE
                 │                                       ▲
                 ▼                                       │
        ┌──────────────────────┐                 (errores: hoy salen 500
        │ ★ CONTROLLERS reales │                  FEOS a propósito — ver
        │   /propietarios      │                  el plano, punto 1c)
        │   /mascotas          │
        └──────────┬───────────┘
                   ▼
        ┌──────────────────────┐          ┌─────────────────────────┐
        │ ★ SERVICES reales    │─────────►│ ★ DTOs reales           │
        │   (orquestan, validan│          │   (Create/Response por  │
        │    instancian, mapean│          │    recurso — la entidad │
        └───┬──────────────┬───┘          │    JAMÁS cruza)         │
            ▼              ▼              └─────────────────────────┘
   ┌─────────────┐  ┌──────────────────┐
   │ ★ DOMINIO   │  │ ★ REPOSITORIES   │
   │  Propietario│  │   (interfaz +    │
   │  Mascota    │  │    InMemory +    │
   │  (dados —   │  │  ★ generador de  │
   │   no los    │  │    ids)          │
   │   diseñás)  │  └──────────────────┘
   └─────────────┘
```

## 🗂️ Los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── controllers/
    │   ├── PingController.java                  (queda como museo — Parte 11)
    │   ├── PropietarioController.java           ★ NUEVO — Parte 6
    │   └── MascotaController.java               ★ NUEVO — tu turno
    ├── models/entities/                         (📦 NUEVO — el dominio)
    │   ├── Propietario.java                     ★ dado — Parte 2
    │   └── Mascota.java                         ★ dado — Parte 2
    ├── dtos/
    │   ├── propietario/                         ★ 2 records — Parte 5
    │   └── mascota/                             ★ 2 records — tu turno
    ├── repositories/                            (📦 NUEVO)
    │   ├── PropietarioRepository.java           ★ interfaz — Parte 3
    │   ├── MascotaRepository.java               ★ tu turno
    │   └── inmemory/                            ★ las impls — Parte 4 / tu turno
    ├── services/ (+ impl/)
    │   ├── PropietarioService(+Impl).java       ★ NUEVO — Parte 5
    │   └── MascotaService(+Impl).java           ★ NUEVO — tu turno
    └── utils/
        └── GeneradorIdSecuencial.java           ★ NUEVO — Parte 3
```

## 🧭 Mapa de esta etapa

1. **EL PLANO COMPLETO:** principios, precios y el estado honesto de los errores.
2. El dominio (dado — se lee, no se diseña).
3. El contrato de datos: generador + interfaz del repo.
4. La implementación en memoria: `@Repository`.
5. El recableado: DTOs + service — con la perla del save adentro.
6. El controller real.
7. La cadena viva: el viaje de cuatro capas.
8. Experimento 1: el 99 que no existe (verificación de 1c).
9. Experimento 2: la memoria efímera.
10. Tu turno 🟨: Mascota — la relación, la regla, el precio anunciado.
11. La limpieza del patio + criterios + checkpoint + registro + 🔗.

---

## 🧠 Parte 1: EL PLANO COMPLETO (leé toda esta parte antes de codear)

### 1a. Los principios que gobiernan cada pieza

Seis reglas — todo el código de hoy es su aplicación literal:

```
┌──────────────────────────┬───────────────────────────────────────────────┐
│ 1. Dominio AGNÓSTICO     │ las entidades no importan NADA del sistema:   │
│                          │ ni Spring, ni tus packages — solo Java,       │
│                          │ Lombok y otras entidades. Vivirían igual en   │
│                          │ un programa de consola.                       │
├──────────────────────────┼───────────────────────────────────────────────┤
│ 2. Relación en UN sentido│ Propietario conoce sus mascotas; Mascota NO   │
│    …y su PRECIO          │ conoce a su dueño. El precio, anunciado:      │
│                          │ averiguar el dueño de una mascota va a costar │
│                          │ recorrer TODOS los propietarios (O(n)) — lo   │
│                          │ vas a escribir vos en la Parte 10, y es       │
│                          │ trade-off de parcial.                         │
├──────────────────────────┼───────────────────────────────────────────────┤
│ 3. UNA autoridad de ids  │ los ids los fabrica el generador, adentro del │
│                          │ save del repo. Nadie más: ni DTOs (no traen   │
│                          │ id — tu decisión de la Etapa 3), ni services. │
├──────────────────────────┼───────────────────────────────────────────────┤
│ 4. Optional = contrato   │ "buscá, y puede que no esté" sin null pelado. │
│    del "puede no estar"  │ El repo lo devuelve; el service decide qué    │
│                          │ hacer con el vacío.                           │
├──────────────────────────┼───────────────────────────────────────────────┤
│ 5. EL SERVICE instancia  │ la regla estrella del recorrido, hoy en tus   │
│    el dominio            │ dedos: new Propietario(...) se escribe en el  │
│                          │ service — nunca en el controller ni el repo.  │
├──────────────────────────┼───────────────────────────────────────────────┤
│ 6. La entidad JAMÁS cruza│ entra CreateRequest, sale Response. El mapeo  │
│                          │ entidad→DTO vive en el service.               │
└──────────────────────────┴───────────────────────────────────────────────┘
```

### 1b. El estado honesto de los errores — anunciado, no descubierto

Hoy tu sistema **no tiene traductor de errores** — ese es el proyecto entero de la Etapa 6. Consecuencia deliberada y conocida de antemano:

> **Todo error de hoy sale como 500 feo.** El id que no existe, el nombre vacío, la regla del dominio violada — todo 500, con stacktrace en consola. No es un bug tuyo: es el estado honesto de un sistema sin traductor. Tu trabajo de hoy es **anotarlos como deudas** (vas a juntar tres); la Etapa 6 las paga todas.

Y el aviso de mantenimiento, para que nada te sorprenda después: **dos detalles de hoy se refinan en la Etapa 6** — el `orElseThrow()` va pelado (lanza una genérica de Java) y las validaciones lanzan `IllegalArgumentException`; en la 6, ambos cambian a excepciones con nombre propio (~4 líneas en total). Lo escribís hoy sabiendo su destino.

### 1c. El plan de construcción

Propietario **guiado completo** (Partes 2-7: dominio → repo → service → controller → verificación) · dos experimentos que verifican 1b y el contrato del repo · Mascota **tuya** (Parte 10), donde la relación del principio 2 te cobra su precio anunciado.

---

## 🐾 Parte 2: El dominio entra en escena (dado)

El dominio te lo doy resuelto — modelar no es la fricción de este proyecto (es lo tuyo desde la clase 03). Leelo con un solo lente: **verificá el principio 1** (¿qué NO tiene?).

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
    private final String nombre;        // (estos final tienen fecha de
    private final String telefono;      //  vencimiento: el PUT de la Etapa 7)
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
    // ↑ LA regla de negocio del dominio, en la entidad — y habla el ÚNICO
    //   idioma que el principio 1 le permite: Java genérico
    //   (IllegalArgumentException). El dominio no conoce HTTP ni tus
    //   excepciones. En la Etapa 6, el service va a TRADUCIR este grito;
    //   la entidad no se toca jamás.
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
    @Setter private String nombre;     // setters: el PUT/PATCH de las
    @Setter private String especie;    // etapas 7-8 los van a necesitar

    public Mascota(Long id, String nombre, String especie) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
    }
}
```

Verificación del principio 1: **cero imports de Spring ni de tus packages** ✓. Y del principio 2: `Propietario` tiene su lista; `Mascota` **no tiene campo propietario** ✓ — la misma decisión (y el mismo precio) que `Comercio`→`Producto` en el proyecto de la clase.

## 📜 Parte 3: El contrato de datos

📍 **Dónde estamos:**

```
Controllers ──► Services ──► Repositories ◄━━ ACÁ (el contrato + el fabricante de ids)
                                │
                                ▼
                             Dominio
```

```java
// 📁 src/main/java/.../veterinaria/utils/GeneradorIdSecuencial.java   (archivo NUEVO)
package com.practica.veterinaria.utils;

public class GeneradorIdSecuencial {
    private long ultimo = 0;
    public long siguiente() { return ++ultimo; }   // 1, 2, 3...
}
// ↑ la ÚNICA autoridad de ids (principio 3). No hay BD que los genere:
//   los generamos nosotros — y solo acá.
```

```java
// 📁 src/main/java/.../veterinaria/repositories/PropietarioRepository.java   (archivo NUEVO)
package com.practica.veterinaria.repositories;

import com.practica.veterinaria.models.entities.Propietario;
import java.util.List;
import java.util.Optional;

public interface PropietarioRepository {

    List<Propietario> findAll();
    Optional<Propietario> findById(Long id);    // ← principio 4: el contrato
    Propietario save(Propietario propietario);  //   del "puede no estar"
    void delete(Propietario propietario);
}
// ↑ el contrato clásico (nombres en inglés = convención universal de repos).
```

## 🛠️ Parte 4: La implementación en memoria — `@Repository`

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
    // ↑ @Repository: bean estereotipado de la capa de datos — la ficha 🎛️
    //   de @Service (Etapa 4) aplica tal cual: solo atributo `value`.

    private final List<Propietario> propietarios = new ArrayList<>();
    // ↑ el "medio persistente" de hoy: una lista. Vive lo que vive la app —
    //   el Experimento 2 te muestra el reverso de esa moneda.

    private final GeneradorIdSecuencial generadorId = new GeneradorIdSecuencial();

    @Override
    public List<Propietario> findAll() {
        return new ArrayList<>(propietarios);
        // ↑ copia DEFENSIVA: devuelvo una lista nueva, no MI lista interna —
        //   nadie de afuera toca mi almacenamiento por accidente.
    }

    @Override
    public Optional<Propietario> findById(Long id) {
        return propietarios.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst();
        // ↑ findFirst() YA devuelve Optional (lleno si hubo match, vacío si
        //   no) — encaja exacto con el contrato, cero conversión.
    }

    @Override
    public Propietario save(Propietario propietario) {
        if (propietario.getId() == null) {              // sin id → es NUEVO:
            propietario.setId(generadorId.siguiente()); //   id fabricado ACÁ
            propietarios.add(propietario);              //   (principio 3)
            return propietario;
        }
        delete(propietario);                            // con id → actualización:
        propietarios.add(propietario);                  //   fuera la versión vieja,
        return propietario;                             //   adentro la nueva
    }
    // ↑ un solo save que resuelve alta Y modificación, decidiendo por el id.
    //   (La rama "con id" hoy no se usa — su día llega con el PUT, Etapa 7.)

    @Override
    public void delete(Propietario propietario) {
        if (propietario.getId() == null) return;
        propietarios.removeIf(p -> p.getId().equals(propietario.getId()));
    }
}
```

## 🔌 Parte 5: El recableado — DTOs + service

📍 **Dónde estamos:**

```
Controllers ──► Services ◄━━ ACÁ ──► Repositories
                   │
                   ▼
                Dominio
```

Los DTOs, con los principios 3 y 6 aplicados:

```java
// 📁 dtos/propietario/PropietarioCreateRequest.java   (NUEVO)
package com.practica.veterinaria.dtos.propietario;

public record PropietarioCreateRequest(String nombre, String telefono) { }
// ↑ SIN id (principio 3: lo fabrica el repo — lo que no está en el DTO
//   no puede entrar).

// 📁 dtos/propietario/PropietarioResponse.java   (NUEVO)
package com.practica.veterinaria.dtos.propietario;

public record PropietarioResponse(Long id, String nombre, String telefono,
                                  int cantidadMascotas) { }
// ↑ CON id (el mundo lo necesita) + un dato calculado de cortesía.
//   La ENTIDAD jamás sale: sale esto (principio 6).
```

El contrato y la implementación del service — cada pieza de las etapas anteriores encontrando su lugar:

```java
// 📁 services/PropietarioService.java   (NUEVO)
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
// 📁 services/impl/PropietarioServiceImpl.java   (NUEVO)
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
    // ↑ la Etapa 4, un piso más abajo: ahora es el SERVICE quien declara su
    //   dependencia por constructor — y pide LA INTERFAZ del repo.

    @Override
    public List<PropietarioResponse> findAll() {
        return propietarioRepository.findAll().stream()
            .map(this::toResponse)             // entidad → DTO, en masa
            .toList();
    }

    @Override
    public PropietarioResponse findById(Long id) {
        return toResponse(getPropietarioOrThrow(id));
    }

    @Override
    public PropietarioResponse create(PropietarioCreateRequest request) {
        if (request == null || request.nombre() == null || request.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        // ↑ regla del FLUJO en el service (Etapa 4). Sale 500 por ahora —
        //   deuda anunciada en 1b; en la Etapa 6 esta línea cambia a
        //   BusinessException.

        Propietario propietario = new Propietario(null, request.nombre().trim(),
                                                  request.telefono());
        // ↑ EL SERVICE INSTANCIA EL DOMINIO (principio 5) — por primera vez
        //   en tus dedos. Id en null: lo pone el repo (principio 3).

        propietarioRepository.save(propietario);
        return toResponse(propietario);
    }

    // ——— piezas privadas ———

    private Propietario getPropietarioOrThrow(Long id) {
        return propietarioRepository.findById(id).orElseThrow();
        // ↑ Optional cerrando su ciclo: "dame el valor; si está vacío, lanzá".
        //   El orElseThrow() PELADO lanza NoSuchElementException (genérica de
        //   Java) → 500 feo. Deuda anunciada en 1b — el Experimento 1 la
        //   verifica; en la Etapa 6, esta línea gana lambda y excepción propia.
    }

    private PropietarioResponse toResponse(Propietario p) {
        return new PropietarioResponse(p.getId(), p.getNombre(), p.getTelefono(),
                                       p.getMascotas().size());
        // ↑ el "mapper": entidad → DTO, campo por campo (principio 6).
    }
}
```

## 💎 La perla del save que nadie captura

Mirá dos líneas del `create` con lupa: `propietarioRepository.save(propietario);` **retorna** un `Propietario`… que nadie captura. ¿Y entonces cómo llega el id al `toResponse` de la línea siguiente? Porque en Java, al pasar un objeto como parámetro, **lo que viaja es una copia de la referencia** — la dirección donde vive el objeto — no una copia del objeto:

```
   SERVICE                       REPO (save)              MEMORIA
   propietario ─────┐            propietario ─────┐       ┌──────────────────┐
                    └─────────────────────────────┴─────► │ Propietario      │
                                                          │  id: null → 1 ✓  │
   (misma flecha,               (setId(1) muta            │  nombre: "Ana"   │
    misma dirección)             ESTE objeto)             └──────────────────┘
```

El `setId(...)` de adentro del repo muta **el único objeto que existe** — y tu variable del service apunta ahí mismo: cuando ejecuta `toResponse(propietario)`, el id ya está adentro. El objeto retornado y el que ya tenías **son el mismo** (`return propietario;` devuelve la misma referencia que entró).

**Precisión de vocabulario** (fuente eterna de confusiones): Java es SIEMPRE **paso por valor** — solo que para objetos, *el valor que se copia es la referencia*. La diferencia se ve en un solo caso: si el save **reasignara** su parámetro (`propietario = new Propietario(...)`), tu variable del service NO se enteraría. **Mutar el objeto compartido se ve desde afuera; reasignar la variable local, no.**

**¿Y para qué existe el retorno, si se puede ignorar?** Contrato pensado para el futuro: hay implementaciones de repositorio (las de persistencia real) donde el objeto guardado **no es la misma instancia** que entró. La firma ya avisa: *"lo que vale es lo que te devuelvo"*. La forma defensiva `propietario = propietarioRepository.save(propietario);` funciona idéntico hoy y sobrevive mejor al cambio; la del proyecto (ignorar el retorno, igual que la cátedra) es válida **con un supuesto adentro**: *"sé que MI implementación muta lo que le paso"*. Decisión con supuesto, no descuido.

**Y guardá el mecanismo con las dos manos:** es exactamente el que va a hacer funcionar la cura del PUT en la Etapa 7 (mutar la instancia viva que todos comparten) — y el que explica su bug (una instancia nueva que nadie más conocía).

## 🚪 Parte 6: El controller real

📍 Archivo nuevo — los recursos de verdad no viven en el patio:

```java
// 📁 controllers/PropietarioController.java   (archivo NUEVO)
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
// ↑ cada línea la construiste en alguna etapa — si alguna sorprende,
//   ahí está tu relectura.
```

## 🤯 Parte 7: La cadena viva

**Predicción antes de correr:** al arrancar, ¿cuántos beans TUYOS construye Spring, y en qué orden obligado? (Contá: repos, services, controllers…)

Levantá y ejecutá en Postman: **(1)** `POST /veterinaria/propietarios` con `{"nombre":"Ana","telefono":"11-5555"}` → **201**, `{"id":1,...,"cantidadMascotas":0}` — ¿quién puso el 1? Recorré el viaje hasta encontrarlo · **(2)** segundo POST → id **2** · **(3)** `GET /propietarios` → los dos · **(4)** `GET /propietarios/1` → Ana.

Eso es **la arquitectura completa del recorrido, viva y tuya**:

```
POST {"nombre":"Ana",...}
   │
   ▼
PropietarioController      ← PRESENTACIÓN: tradujo el JSON (Jackson), delegó
   │
   ▼
PropietarioServiceImpl     ← ORQUESTADOR: validó (flujo), INSTANCIÓ el
   │            │            dominio, mandó a guardar, mapeó a DTO
   ▼            ▼
Propietario   InMemoryPropietarioRepository   ← DOMINIO / DATOS:
(la entidad,    (fabricó el id, guardó           cada uno en lo suyo,
 agnóstica)      en su lista)                    sin conocerse de más
   │
   ▼
201 + PropietarioResponse como JSON
```

## 🧨 Parte 8: Experimento 1 — el 99 que no existe (verificación de 1b)

**Predicción (ya sabés la teoría — el punto es verla):** `GET /veterinaria/propietarios/99` — ¿qué código sale, y qué excepción vas a leer en consola?

→ **500**, con el `NoSuchElementException` del `orElseThrow()` pelado viajando sin que nadie lo atrape — tal como 1b anunció. **Anotá la deuda 2** (la 1 es la validación del nombre; probala si querés: `POST` con `{"nombre":""}` → 500 también). La tercera llega en la Parte 10. Las tres juntas son, literalmente, el plano de la Etapa 6.

## 🧨 Parte 9: Experimento 2 — la memoria efímera

Con tus dos propietarios cargados: **frená la app** (`Ctrl+C`) y levantala de nuevo. **Predicción:** `GET /propietarios` — ¿qué devuelve?

→ **`[]`**. Ana y compañía murieron con el proceso: la "persistencia" en memoria vive lo que vive la JVM — el contrato del repo de hoy, ahora *sentido*. Dos consecuencias: cada sesión de pruebas arranca de cero (molesto — la **semilla** de la Etapa 9 existe para eso), y la persistencia real es **otra implementación del MISMO contrato** `PropietarioRepository` — cuando llegue, ¿qué archivos tuyos cambian? (Guardá la respuesta: es del checkpoint.)

## ✍️ Parte 10: Tu turno 🟨 — Mascota completa

El salto grande, sin template — especificación + hints. Construí el recurso entero: **repo** (interfaz + in-memory, calco estructural, con su propio generador) · **DTOs** en `dtos/mascota/` (`MascotaCreateRequest`: `propietarioId`, `nombre`, `especie` — pensá los tipos con tu hallazgo de la Etapa 3 en la mano; `MascotaResponse`: `id`, `propietarioId`, `nombre`, `especie`) · **service** · **controller** (`/veterinaria/mascotas`: GET all, GET /{id}, POST).

El `create` del service es tu primera orquestación **con relación** — el flujo:

```
  1. validar entrada (flujo — IllegalArgumentException por ahora, deuda conocida)
  2. RESOLVER propietarioId → objeto Propietario   (¿de dónde? ¿y si no existe?)
  3. instanciar la Mascota (id null — principio 3)
  4. ASOCIAR: propietario.agregarMascota(mascota)  ⚠️ acá puede explotar la
                                                      regla de las 5 → DEUDA 3
  5. persistir la mascota en SU repo
  6. devolver el DTO… y acá EL PRECIO ANUNCIADO (principio 2) te cobra:
     MascotaResponse pide propietarioId — y la Mascota no conoce a su dueño.
```

💡 **Hints del precio (escalonados — quemalos en orden):** H1 — el proyecto de la clase resolvió exactamente esto: un objeto cuyo dueño hay que averiguar sin que el objeto lo sepa. ¿Cómo se llamaba ese método y qué recorría? · H2 — necesitás el repo de propietarios (tu service inyecta **dos** repos) y preguntarle a cada propietario si su lista contiene a esta mascota. · H3 — `propietarioRepository.findAll().stream().filter(p -> p.getMascotas().contains(mascota)).findFirst().orElseThrow()` — búsqueda lineal: **el O(n) del principio 2, cobrado**. Nombralo así en un comentario: es trade-off de parcial.

**Resultado esperado:** `POST /mascotas` `{"propietarioId":1,"nombre":"Firulais","especie":"perro"}` → **201** con `propietarioId: 1` · `GET /propietarios/1` → `cantidadMascotas: 1` (tu asociación, verificada por el otro recurso) · `propietarioId: 99` → **500** (deuda 2 en su versión mascota) · la **sexta** mascota de Ana → **500** con el mensaje de la regla en consola — **deuda 3 anotada: la regla del dominio gritando en su idioma, sin traductor** · JSON roto → 400 (ese sí, de fábrica — Etapa 3: la comilla rota).

## 🧹 Parte 11: La limpieza del patio

`PingController` cumplió su ciclo. Decisión tuya: podarlo, o dejarlo como museo de las etapas 1-3 (con `TurnoRequestClass` y compañía). Si queda, una línea de comentario arriba declarándolo laboratorio — que el vos-del-futuro no lo confunda con producción.

## ✅ Criterios de "Etapa 5 completa"

- [ ] Leíste el plano entero antes de codear; los seis principios te cierran solos.
- [ ] Propietario completo y probado: POST con id fabricado, GET all, GET by id.
- [ ] Las **tres deudas del traductor** anotadas juntas, con el código que debería corresponder a cada una.
- [ ] Mascota completa: la relación resuelta, la regla explotando en la sexta, y el precio O(n) escrito y **nombrado como trade-off**.
- [ ] `cantidadMascotas` refleja las asociaciones — verificado cruzado.

## ✅ Checkpoint

*Recall:*
1. ¿Quién fabrica los ids, en qué método exacto, y por qué ahí y no en el service o el DTO?
2. ¿Por qué `findById` devuelve `Optional` y no la entidad o null? ¿Qué te permitió encadenar en el service?
3. Recitá el viaje del POST de la Parte 7: las cuatro capas, qué hace cada una, y las dos cosas que el service (y solo él) hizo.
4. El service ignora el retorno del `save` y aun así el DTO sale con id. ¿Por qué funciona — y en qué escenario futuro dejaría de funcionar ignorarlo?

*Decidí y justificá:*
5. El dominio no tiene ni un import de Spring. ¿Qué compra esa pureza? Da dos escenarios concretos donde la pagarías caro si `Propietario` fuera un bean con anotaciones.
6. El día que llegue la base de datos real: recorré TU árbol de archivos y marcá exactamente cuáles cambian y cuáles no. ¿Qué decisiones de hoy hicieron la lista tan corta?
7. `findPropietarioOwner` recorre todo por cada mapeo. Un compañero propone "agregale `propietario` como atributo a Mascota y listo". Defendé el diseño actual — y concedé honestamente qué ganaría él.
8. Borrás una mascota del `MascotaRepository`… ¿quedó viva en la lista de su propietario? Investigalo en TU código y decidí: ¿bug, o decisión pendiente? ¿Qué haría falta para un delete consistente? *(No lo arregles — llega con el DELETE real de la Etapa 7. Pero pensalo hoy.)*
9. Las tres deudas del traductor: listalas con el código HTTP que le corresponde a cada una y DÓNDE nace cada excepción. (Es el plano de la próxima etapa.)

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Hoy la conexión es un espejo de cuerpo entero. Abrí lado a lado tu `InMemoryPropietarioRepository` y el `InMemoryProductoRepository` del `sales-service`: la misma criatura (lista + generador + copia defensiva + save-que-decide). Después tu `MascotaServiceImpl.create` contra su `ProductoServiceImpl.create`: validar → resolver por id → instanciar → asociar → guardar → mapear — el mismo flujo, paso por paso; y su `findComercioOwner` contra tu `findPropietarioOwner`: **el mismo trade-off, con otros nombres**. La única diferencia estructural que te queda por conquistar: sus excepciones tienen nombre propio y las tuyas son genéricas — exactamente tus tres deudas. Releé del recorrido **P6 §3 y §4 completos**: no deberían explicarte nada — deberían *confirmarte*.

## ▶️ Próximo paso

Tenés la arquitectura entera funcionando… y **tres mentiras saliendo por el puerto**: todo error, sea culpa de quien sea, sale disfrazado de 500. Decime **"arranquemos etapa 6"** y construimos el traductor — con el sistema completo primero, como corresponde.

---

**FIN DE LA ETAPA 5 — v2**
