# 🌱 Proyecto 0 (clase 04) — Etapa 4: El service en el medio

> **Objetivo:** sacarle al controller lo que usurpó — cálculo y (ahora sí) validaciones — y ponerlo donde corresponde: un **service** con contrato de interfaz, construido e inyectado por el framework. Tu DI del proyecto-1, con camiseta nueva.
>
> **El momento clave:** cuando tu controller quede en UNA línea por endpoint y entiendas que eso no es pereza — es su rol. Traducir y delegar; jamás pensar.
>
> **Pre-requisito:** Etapa 3 completa.
>
> **Tiempo estimado:** 45-55 minutos.

---

## 🗂️ Dónde estamos — los archivos de esta etapa

```
clase-04-prac-01-proy-00/
└── src/main/java/.../veterinaria/
    ├── VeterinariaApplication.java          (no se toca)
    ├── controllers/
    │   └── PingController.java              (SE TOCA: sus POST quedan en 1 línea)
    ├── dtos/                                (sin cambios — hoy solo se usan)
    └── services/                            (📦 NUEVO package)
        ├── PresupuestoService.java          (NUEVO — interfaz, Parte 2)
        ├── TurnoService.java                (NUEVO — tu turno, interfaz)
        └── impl/                            (📦 NUEVO sub-package)
            ├── PresupuestoServiceImpl.java  (NUEVO — Parte 3)
            └── TurnoServiceImpl.java        (NUEVO — tu turno)
```

---

## 🧭 Mapa de esta etapa

1. El problema, con nombre y apellido.
2. Contrato primero: la interfaz.
3. La implementación: `@Service` y la mudanza de la lógica.
4. La inyección: el controller pide, el framework alcanza.
5. Experimento 1: sacá `@Service` — el bean que falta.
6. Experimento 2: dos implementaciones — el bean que sobra.
7. Experimento 3: la validación en vivo (y el 500 que está MAL).
8. Tu turno: el service de turnos, solo.
9. Criterios + checkpoint + registro + 🔗.

---

## 🔍 Parte 1: El problema, con nombre y apellido

Abrí tu `PingController` y mirá el endpoint `/presupuestos` con los ojos del recorrido: hay un **cálculo de negocio** (`cantidad * precioUnitario`) viviendo en la capa de presentación. Y de la Etapa 3 quedó flotando que **nadie valida nada** (`cantidad: -5` entra campante). El recorrido (P4 §6) fue tajante: el controller *recibe, traduce, delega y responde* — **nunca piensa**. Hoy le devolvemos lo usurpado a su dueño legítimo: el **service**, el orquestador que focaliza las reglas del flujo.

## 📜 Parte 2: Contrato primero — la interfaz

Como en el proyecto de la clase, la capa se presenta al mundo por su **contrato**:

```java
// 📁 src/main/java/.../veterinaria/services/PresupuestoService.java   (archivo NUEVO)
package com.practica.veterinaria.services;

import com.practica.veterinaria.dtos.PresupuestoRequest;
import com.practica.veterinaria.dtos.PresupuestoResponse;

public interface PresupuestoService {

    PresupuestoResponse cotizar(PresupuestoRequest request);
}
```

Una interfaz, un método, cero anotaciones. ¿Por qué el doble trabajo de interfaz + clase, si "la interfaz sola no funciona"? Es la promesa del recorrido (P5 §5) que hoy tocás: **quien consuma este service va a conocer SOLO estas firmas** — y eso compra el reemplazo transparente. En dos experimentos lo vas a ver defendido por el propio framework.

## 🛠️ Parte 3: La implementación — `@Service` y la mudanza

```java
// 📁 src/main/java/.../veterinaria/services/impl/PresupuestoServiceImpl.java   (archivo NUEVO)
package com.practica.veterinaria.services.impl;

import com.practica.veterinaria.dtos.PresupuestoRequest;
import com.practica.veterinaria.dtos.PresupuestoResponse;
import com.practica.veterinaria.services.PresupuestoService;
import org.springframework.stereotype.Service;

@Service
public class PresupuestoServiceImpl implements PresupuestoService {

    @Override
    public PresupuestoResponse cotizar(PresupuestoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("El body del presupuesto es obligatorio");
        }
        if (request.servicio() == null || request.servicio().isBlank()) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }
        if (request.cantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (request.precioUnitario() <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a cero");
        }
        // ↑ Las validaciones huérfanas de la Etapa 3, POR FIN con dueño: reglas
        //   del flujo, en el service. Lanzan y no atrapan — el service patea
        //   hacia arriba (recorrido P4 §4). ¿Quién atrapa? Hoy, nadie tuyo:
        //   el Experimento 3 te muestra el (mal) resultado de eso.

        double total = request.cantidad() * request.precioUnitario();
        // ↑ El cálculo mudado desde el controller: la usurpación, revertida.

        return new PresupuestoResponse(request.servicio(), request.cantidad(),
                                       request.precioUnitario(), total);
    }
}
```

**`@Service`** — la anotación que ya conocés de la teoría, ahora en tus dedos: *"esta clase es un bean de la capa de servicios: construila vos, framework"*. Es un `@Component` estereotipado, primo hermano del que escribiste en el sondeo de la Etapa 0.

> 🎛️ **Ficha `@Service`:** un solo atributo — `value` (nombre custom del bean; casi nunca se setea: solo reconocer). Misma ficha exacta para `@Repository` (próxima etapa) y para el `@RestController` que venís usando pelado desde la Etapa 1.

## 🔌 Parte 4: La inyección — el controller pide, el framework alcanza

Ahora el controller. **Predicción antes de tocar nada:** vas a declarar un atributo y un constructor… ¿quién va a llamar a ese constructor, y de dónde va a sacar el parámetro? (Si tu sondeo B de la Etapa 0 salió 🟢, esto ya lo sabés.)

```java
// 📁 src/main/java/.../veterinaria/controllers/PingController.java   (SE TOCA)

@RestController
@RequestMapping("/veterinaria")
public class PingController {

    private final PresupuestoService presupuestoService;
    // ↑ Mirá el TIPO con lupa: la INTERFAZ. El controller no sabe (ni le
    //   importa) qué implementación existe detrás. `final`: se asigna una
    //   vez, en el constructor, y nunca más.

    public PingController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }
    // ↑ El constructor DECLARA la dependencia. Quien lo llama es Spring, al
    //   arrancar: ya construyó el @Service (bean), ve que este @RestController
    //   lo pide, y se lo INYECTA. Tu proyecto-1, con camiseta de capas.

    // ... (tus endpoints GET de siempre, intactos) ...

    @PostMapping("/presupuestos")
    @ResponseStatus(HttpStatus.CREATED)
    public PresupuestoResponse presupuesto(@RequestBody PresupuestoRequest request) {
        return presupuestoService.cotizar(request);
    }
    // ↑ UNA línea: traducir (lo hizo @RequestBody) y DELEGAR. El controller
    //   volvió a su rol de frontera. Esto no es pereza — es diseño.
}
```

Levantá y mandá el POST feliz de siempre: **201, mismo resultado exacto que en la Etapa 3**. Nada cambió afuera — todo cambió adentro. Refactor con red: el contrato externo intacto, las responsabilidades en su casa.

## 🧨 Parte 5: Experimento 1 — sacá `@Service` (el bean que falta)

Comentá la anotación `@Service` de la impl. **Predicción:** en la Etapa 1, sacar `@RestController` daba una app que arrancaba y un 404 silencioso. ¿Acá igual — arranca y el POST falla? ¿U otra cosa, en otro momento?

Corré: **la app NO arranca.** Leé el error completo (formato Description/Action, tu viejo conocido):

```
***************************
APPLICATION FAILED TO START
***************************

Description:
Parameter 0 of constructor in ...PingController required a bean of type
'...PresupuestoService' that could not be found.

Action:
Consider defining a bean of type '...PresupuestoService' in your configuration.
```

Espécimen **#8**, y el contraste con la Etapa 1 es la lección: el controller sin anotación fallaba *en silencio y tarde* (404 en la request) porque **nadie lo necesitaba**; el service sin anotación falla *a los gritos y temprano* porque **alguien declaró que lo necesita** — tu constructor. La inyección por constructor convierte las dependencias faltantes en errores de arranque: imposible llegar a producción con el cable suelto. (Otro punto para la filosofía "explícito y estricto": tercera vez que Spring prefiere no arrancar antes que arrancar roto.) Restaurá.

## 🧨 Parte 6: Experimento 2 — dos implementaciones (el bean que sobra)

Creá en `impl/` una `PresupuestoServiceImplV2` (implements la misma interfaz, `@Service`, mismo método pero con un recargo del 10% en el total — cualquier diferencia sirve). **Predicción:** el controller pide "un `PresupuestoService`" y ahora hay DOS candidatos anotados… ¿arranca y elige uno? ¿Cuál? ¿O ya sabés cómo piensa Spring?

Corré: **no arranca** — espécimen **#9**:

```
Parameter 0 of constructor in ...PingController required a single bean,
but 2 were found:
    - presupuestoServiceImpl: ...
    - presupuestoServiceImplV2: ...
```

La regla del recorrido (P5 §5), defendida por el framework en vivo: **una interfaz, UNA implementación anotada** — con ambigüedad, Spring no adivina: se niega a arrancar. Es el "Ambiguous mapping" de la Etapa 1, versión beans: misma filosofía, otra capa.

👀 *Existen `@Primary` (marcar la implementación preferida) y `@Qualifier` (pedir una por nombre) para convivir con múltiples impls — territorio de configuración avanzada, pariente de los profiles del recorrido. Reconocerlas; tu regla sigue siendo una-y-una.* Borrá la V2.

## 🧨 Parte 7: Experimento 3 — la validación en vivo (y el 500 que está MAL)

Ahora sí: mandá el POST con `"cantidad": -5`. **Predicción:** la validación nueva la rechaza… ¿con qué código de estado sale eso al mundo?

**500 Internal Server Error** — con el body de fábrica y, en consola, tu `IllegalArgumentException` viajando con stacktrace completo hasta la superficie. Leelo: nace en tu service, nadie la atrapa, y el manejador por defecto la trata como "explosión imprevista del servidor".

**Y eso está DOBLEMENTE mal — anotalo como incomodidad oficial:** *(1)* el código miente: `cantidad: -5` es culpa del **cliente** — le corresponde un **400**, no un 500 que dice "el roto soy yo"; *(2)* el body de fábrica no cuenta nada útil. La validación funciona; la **traducción del error al mundo HTTP no existe todavía**. Ese traductor tiene nombre, lo conocés del recorrido (P6 §7), y es la Etapa 6 completa. Dos etapas de tensión sostenida, a propósito: cuando llegue el `@RestControllerAdvice`, vas a saber exactamente qué dolor cura.

## ✍️ Parte 8: Tu turno — el service de turnos, solo

Repetí la cirugía completa para `/turnos`: interfaz `TurnoService` (un método `confirmar(TurnoRequest)` que devuelve el String de siempre) + `TurnoServiceImpl` con la validación que la Etapa 3 dejó huérfana (mascota obligatoria: ni null ni en blanco → `IllegalArgumentException`) + el armado de la frase mudado + el controller delegando en una línea. Ubicaciones y nombres: ya conocés las convenciones del proyecto. *(El `/turnos-v2` del museo no se toca — quedó como pieza comparativa.)*

**Resultado esperado:** POST feliz → **201**, frase idéntica a la de siempre · sin `"mascota"` → **500** con tu `IllegalArgumentException` en consola (correcto por ahora — y sabés explicar por qué está mal igual) · el constructor del controller ahora pide **dos** services y Spring inyecta ambos sin que hagas nada especial.

## ✅ Criterios de "Etapa 4 completa"

- [ ] `/presupuestos` y `/turnos` responden EXACTAMENTE igual que antes por fuera — con controller de una línea por dentro.
- [ ] Los tres experimentos hechos con predicción escrita; especímenes #8 y #9 provocados y leídos.
- [ ] Podés explicar por qué el bean faltante rompe el arranque pero el controller sin anotación daba 404 tardío.
- [ ] La incomodidad del 500-que-debería-ser-400 quedó anotada con nombre.
- [ ] Tu `TurnoService` completo, con el controller pidiendo dos services por constructor.

## ✅ Checkpoint

*Recall:*
1. ¿Quién instancia al service, quién al controller, y en qué orden? ¿Qué pieza de tu sondeo de la Etapa 0 era exactamente esto?
2. ¿Por qué el atributo del controller es de tipo `PresupuestoService` (interfaz) y no `PresupuestoServiceImpl`?
3. ¿Qué dos errores de arranque nuevos conociste hoy, y qué declara cada uno que está roto?

*Decidí y justificá:*
4. La pregunta 8 de la Etapa 3, ahora con respuesta tuya: ¿por qué la validación de `cantidad` va en el service y no en el controller ni en Jackson? ¿Qué tipo de regla es, en el vocabulario del recorrido?
5. Tu compañero valida en el controller "porque así corta antes y es más rápido". Funciona. Dale los dos argumentos de diseño en contra — y decile qué le va a pasar el día que exponga el mismo caso de uso por un segundo canal.
6. El framework se negó a arrancar tres veces en tu proyecto (rutas duplicadas, bean faltante, bean duplicado). ¿Qué filosofía común hay detrás, y qué atributo de calidad compra? ¿Preferirías que "elija algo y arranque"?
7. El 500 del Experimento 3: explicá la cadena completa (dónde nace la excepción, por qué llega a la superficie, quién la convierte en 500) y qué DOS cosas habría que cambiar para que el mundo reciba un 400 con mensaje útil.

## 📝 Registro de la etapa

Tu línea: ¿qué te sorprendió, costó o hizo clic?

## 🔗 Conexión con la clase

Abrí en el `sales-service` el par `services/ProductoService.java` + `services/impl/ProductoServiceImpl.java` y leé: la interfaz (contrato puro, como la tuya), el `@Service`, el constructor con **tres** repositorios inyectados (vos hoy inyectaste services en el controller; él inyecta repos en el service — misma mecánica, un piso más abajo: tu próxima etapa), y las validaciones privadas (`validateCreateRequest` — tus ifs de hoy, con excepciones propias en vez de `IllegalArgumentException`: la diferencia es la Etapa 6). Después el `ProductoController`: constructor + una línea por endpoint — tu cirugía de hoy, en producción. Releé del recorrido **P5 §4-5 completos** y **P6 §4** (el create paso a paso): a esta altura deberían leerse como un espejo.

## ▶️ Próximo paso

Probá algo: creá un presupuesto… y ahora **buscalo**. No podés — no hay dónde: cada request nace y muere sola, nada se guarda, y `Ctrl+C` borra el universo. Tu API calcula pero no **recuerda**. Decime **"arranquemos etapa 5"** y llega la capa de datos: el repositorio, el `Optional` de punta a punta, el dominio de verdad (adiós patio de juguetes) — y la cadena completa de cuatro capas recorrida por una request real.

---

**FIN DE LA ETAPA 4**
