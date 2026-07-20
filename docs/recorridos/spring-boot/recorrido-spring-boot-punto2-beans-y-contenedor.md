# Recorrido Spring Boot — Punto 2
## Beans: el contenedor instancia por vos

---

## 📄 Sobre este documento

**Qué cubre:** qué es el contenedor de Spring, qué es un **bean**, y las dos formas de meter un objeto adentro — `@Component` y `@Configuration` + `@Bean`. Incluye el archivo que el profe te dijo que no miraras.

**Qué NO cubre:** cómo los objetos del contenedor llegan a donde se necesitan (eso es el Punto 3: inyección). Acá solo los **creamos y guardamos**.

**Cuánto es:** el punto más denso del recorrido, y el que más rinde. Si entendés éste, el resto es cuesta abajo.

---

## 🎒 De dónde venís

- **Punto 1:** sabés qué es la inversión de control, que Spring toma el control en `SpringApplication.run(...)`, que hace un *component scan* desde el paquete de `CountriesApplication` hacia abajo, y que las anotaciones de Spring son **órdenes**, no etiquetas.
- **De Java:** `new`, constructores, `static`, interfaces, el concepto de "no puedo editar el código fuente de una biblioteca externa".
- **El hilo abierto:** *nadie hace `new BuscadorDePaises(...)` y sin embargo el objeto existe.* Acá empezamos a cerrarlo.

---

## 1. El contenedor: una bolsa de objetos ya hechos 🔴

Sacate de encima la palabra "contenedor" — no tiene nada que ver con Docker.

> **Contenedor de Spring** (su nombre técnico es *ApplicationContext*): un objeto grande, creado al arrancar, que **guarda adentro los objetos que tu aplicación necesita, ya construidos y ya conectados entre sí**. Es, literalmente, un mapa: nombre → objeto.

Y a cada objeto que vive adentro del contenedor se lo llama **bean**.

> **Bean:** un objeto que Spring creó y administra. Eso es todo. No es un tipo especial de clase, no implementa ninguna interfaz, no hereda de nada. Es un objeto Java común y silvestre — la única diferencia es **quién lo instanció**: lo instanció Spring, no vos.
>
> *(El nombre viene de "JavaBean", una convención vieja de Java. No le busques poesía.)*

**El contenedor de `rest-paises`, después del arranque, tiene exactamente esto adentro:**

```
┌─────────────── CONTENEDOR (ApplicationContext) ────────────────┐
│                                                                │
│   "restTemplate"            → un objeto RestTemplate           │
│   "restCountriesProperties" → un objeto RestCountriesProperties│
│   "buscadorDePaises"        → un objeto BuscadorDePaises       │
│                                (con los dos de arriba adentro) │
│                                                                │
│   (+ unos cuantos beans internos de Spring que no te importan) │
└────────────────────────────────────────────────────────────────┘
```

Tres beans tuyos. Esa es toda la "magia". **Una bolsa con tres objetos.**

La pregunta del punto es: **¿cómo entraron ahí?** Hay dos puertas.

---

## 2. Puerta 1 — `@Component`: "Spring, esta clase es tuya" 🔴

La más simple. Le ponés `@Component` a una clase tuya, y Spring la instancia en el arranque.

```java
package ar.edu.utn.ba.ddsi.countries.services;   // ← está DEBAJO del paquete raíz. Clave.

import org.springframework.stereotype.Component;  // ← el import que el profe destacó en clase:
                                                  //   "elijan el de org.springframework.stereotype"

@Component
// ↑ LA ORDEN: "Spring, en el arranque, hacé new de esta clase y guardá el objeto en el contenedor."
//   No es una descripción. Es un imperativo.
public class BuscadorDePaises {

    private final RestTemplate restTemplate;
    private final RestCountriesProperties propiedades;

    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }
    // ...
}
```

**Cómo lo encuentra Spring:** por el **component scan** del Punto 1. Spring arranca en `ar.edu.utn.ba.ddsi.countries` (el paquete de `CountriesApplication`), baja por todos los sub-paquetes, y cada clase con `@Component` que encuentra, la instancia.

```
ar.edu.utn.ba.ddsi.countries          ← 📍 CountriesApplication vive acá. El escaneo arranca en este piso.
├── config/
│   ├── RestCountriesProperties       ← lo ve  ✅
│   └── RestTemplateConfig            ← lo ve  ✅
└── services/
    ├── BuscadorDePaises              ← lo ve  ✅  @Component
    └── dto/
        ├── Pais                      ← lo ve, pero NO tiene @Component → NO es bean
        ├── NombrePais                ← ídem
        └── DetalleMoneda             ← ídem
```

⚠️ **Fijate en los DTOs.** Están dentro del paquete escaneado, Spring los "ve" — y **no los instancia**, porque no tienen `@Component`. El escaneo no agarra todo: agarra **lo anotado**. En clase alguien preguntó justamente eso (*"¿a `Pais` no se le pone `@Component`?"*) y Escobar contestó *"no, todavía no"*. La respuesta completa la vas a tener en el Punto 5: un DTO es un molde de datos del que hay **miles de instancias** (una por país); un bean es un objeto de servicio del que hay **una sola**. Son cosas distintas.

⚠️ **Y fijate en el paquete.** Si `BuscadorDePaises` estuviera en `com.ever.pruebas`, el escaneo **nunca lo encontraría** — está fuera del árbol. La clase compilaría perfecto, el `@Component` estaría ahí, y el objeto simplemente **no existiría**. Es una de las causas más comunes de "me tira NullPointerException y no entiendo por qué". Esa es la respuesta a la pregunta 6 del checkpoint anterior.

---

## 3. Puerta 2 — `@Configuration` + `@Bean` 🔴

Ahora sí, **el archivo prohibido**. Escobar, textual: *"este código hoy no me importa que se entienda, así que no hace falta que lo miren, por eso copy-pasteale"*.

Son ocho líneas. Y son **la mejor ventana al contenedor que hay en todo el repo**:

```java
package ar.edu.utn.ba.ddsi.countries.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
// ↑ "Spring: esta clase no es un servicio. Es una FÁBRICA de beans.
//    Al arrancar, ejecutá los métodos que estén marcados con @Bean."
public class RestTemplateConfig {

    @Bean
    // ↑ "Este método FABRICA un bean. Llamalo vos, y guardá lo que devuelva en el contenedor."
    //    El nombre del bean será el nombre del método: "restTemplate".
    public RestTemplate restTemplate() {
        return new RestTemplate();
        // ↑ 👈 ACÁ ESTÁ EL new. El único new explícito de todo el repo.
        //   No lo ejecutás vos: lo ejecuta SPRING, una sola vez, en el arranque.
        //   Y el objeto que sale de acá NO se pierde: queda guardado en el contenedor.
    }
}
```

### `// ¿CÓMO FUNCIONA?`

```
 1. Component scan encuentra RestTemplateConfig (tiene @Configuration).
 2. Spring la instancia (es una clase suya ahora, como cualquier @Component).
 3. Spring mira sus métodos y encuentra restTemplate() marcado con @Bean.
 4. Spring EJECUTA ese método.       → se corre el new RestTemplate()
 5. Spring toma el objeto devuelto y lo guarda en el contenedor,
    bajo el nombre "restTemplate" (= el nombre del método).
 6. A partir de ahí, cualquiera que pida un RestTemplate, recibe ESE.

 Resultado en el contenedor:   "restTemplate" → [objeto RestTemplate]
```

**Traducción a castellano:** un método `@Bean` es una **receta**. Vos le explicás a Spring *cómo* construir un objeto, y Spring la ejecuta y se queda con el resultado.

---

## 4. La pregunta que casi nadie contesta 🔴

> **¿Por qué `RestTemplate` necesita todo ese lío del `@Bean`, si a `BuscadorDePaises` le alcanzó con un `@Component`?**

Volvé a mirar los dos y buscá la diferencia real. No está en lo que hacen. Está en **de quién es cada clase**.

| | `BuscadorDePaises` | `RestTemplate` |
|---|---|---|
| ¿Quién la escribió? | **Vos** (bueno, el profe) | **Spring** — viene en el `.jar` de la dependencia |
| ¿Dónde vive el `.java`? | En tu `src/main/java` | Dentro de una biblioteca, **compilada** |
| ¿Podés abrirla y editarla? | Sí | **No.** Es de solo lectura. |
| ¿Podés escribirle `@Component` arriba? | Sí | **❌ IMPOSIBLE.** No tenés el código fuente. |

**Ahí está.** Para poner `@Component` sobre una clase, necesitás **editar el archivo de esa clase**. `RestTemplate` no es tuyo — vive adentro de un `.jar` que bajó Maven. No podés tocarlo.

Entonces Spring te da la puerta trasera: **si no podés anotar la clase, escribí un método que la fabrique, y anotá el método.**

> 🎯 **La regla, para no dudar nunca más:**
>
> | Situación | Herramienta |
> |---|---|
> | La clase es **tuya** (podés editarla) | ✅ `@Component` directo sobre la clase |
> | La clase es de **terceros** (biblioteca, no la podés editar) | ✅ Método `@Bean` dentro de una clase `@Configuration` |
>
> **Comparativo:**
>
> ```java
> // ❌ SIN @Bean — lo que te gustaría hacer y NO PODÉS:
> //    Abrir el código fuente de Spring y escribir esto. No tenés el archivo.
> @Component
> public class RestTemplate { ... }   // ← esta clase no es tuya. No la podés tocar.
>
> // ✅ CON @Bean — la puerta trasera. Anotás un método TUYO que la construye:
> @Configuration
> public class RestTemplateConfig {
>     @Bean
>     public RestTemplate restTemplate() {
>         return new RestTemplate();   // el new lo escribís vos, lo ejecuta Spring
>     }
> }
> ```

Y ahora **el `@Bean` deja de ser una molestia y se vuelve una ventaja**: como vos escribís el método, podés configurar el objeto antes de entregarlo. Si mañana quisieras que **todas** las llamadas HTTP tengan un timeout de 5 segundos, lo ponés en ese método una vez, y todo el proyecto lo hereda. Ese archivo de 8 líneas es el **punto de control** de tu cliente HTTP.

Por eso mismo la línea del profe (*"no lo miren"*) era pedagógicamente razonable para la clase 2 — y por eso mismo era la que más te iba a picar.

> 🧵 **Hilo abierto.** Ya sabés que existe un bean `RestTemplate` en el contenedor. Todavía no sabés **qué hace** — es un cliente HTTP, el equivalente Java del `fetch()` que usás en JS. Se cierra en el **Punto 6**, cuando lo usemos para pegarle a la API.

---

## 5. Un bean, una instancia 🟡

Por defecto, **Spring crea UNA sola instancia de cada bean** y se la da a todo el que la pida. Compartida.

```
                       ┌─────────────────┐
   BuscadorDePaises ──►│                 │
                       │  RestTemplate   │   ← EL MISMO objeto para todos.
   (mañana: otro ─────►│   (1 instancia) │      No se crea uno por cada quien lo pide.
    servicio)          └─────────────────┘
```

Esto se llama **scope singleton**, y es el default. Tiene sentido: `RestTemplate` no guarda estado propio (no se "ensucia" entre llamadas), así que compartirlo es gratis y ahorra memoria.

**Lo importante que se deduce de esto:** un bean **no** es un objeto que se crea y se descarta todo el tiempo. Es un objeto de **servicio**, de larga vida, que existe una vez y trabaja para toda la aplicación. Por eso los DTOs (`Pais`, `NombrePais`) **no** son beans: de esos hay 250 instancias, una por país, creadas y descartadas en cada llamada. Son datos, no servicios.

> 🕳️ **Madriguera — Otros scopes**
> Spring soporta más scopes además de singleton (`prototype` crea uno nuevo en cada pedido; `request` y `session` viven lo que dura un request HTTP o una sesión web). Existen, se configuran con `@Scope`, y en DSI no los vas a necesitar.
> *Volvé al camino — en toda la materia vas a trabajar con el default (singleton).*

> 🕳️ **Madriguera — `@Service`, `@Repository`, `@Controller`**
> Vas a ver estas tres anotaciones por todos lados en cualquier proyecto Spring. **Las tres son `@Component` por dentro** — literalmente: si abrís su código fuente, están anotadas con `@Component`. Hacen exactamente lo mismo (crear un bean); cambian solo en la **intención** que declaran: `@Service` = lógica de negocio, `@Repository` = acceso a datos, `@Controller` = punto de entrada web. Son documentación ejecutable, para que al leer el proyecto sepas qué hace cada capa.
> **📍 Dónde cae:** **clase 4** — *"Orquestación de CU: capas, repositorios"*. Cuando aparezcan, ya vas a saber que son primos de `@Component`.
> *Volvé al camino — acá, con `@Component`, alcanza.*

---

## 6. Las dos puertas, lado a lado 🔴

```
  ┌──────────────────────── ARRANQUE ─────────────────────────┐
  │                                                           │
  │   SpringApplication.run(CountriesApplication.class)       │
  │                          │                                │
  │                          ▼                                │
  │              🔍 COMPONENT SCAN                            │
  │       (desde ar.edu.utn.ba.ddsi.countries hacia abajo)    │
  │                          │                                │
  │          ┌───────────────┴────────────────┐               │
  │          ▼                                ▼               │
  │  PUERTA 1: @Component            PUERTA 2: @Configuration │
  │  "la clase es mía"               "la clase es ajena"      │
  │          │                                │               │
  │  Spring hace new                 Spring ejecuta el método │
  │  de la clase                     @Bean y guarda el return │
  │          │                                │               │
  │          ▼                                ▼               │
  │  ┌─────────────────────────────────────────────────────┐  │
  │  │                  CONTENEDOR                         │  │
  │  │  [BuscadorDePaises]  [RestTemplate]  [Properties]   │  │
  │  └─────────────────────────────────────────────────────┘  │
  └───────────────────────────────────────────────────────────┘
                              │
                              ▼
                  ¿Y cómo salen de ahí y llegan
                  a donde se necesitan?  →  PUNTO 3
```

> 📌 **Para el parcial, si te preguntan: "¿Qué es un bean de Spring?"**
>
> Un bean es un objeto instanciado y administrado por el contenedor de Spring, no por el programador. Se registra en el contenedor de dos formas: anotando la clase con `@Component` (cuando la clase es propia) o declarando un método `@Bean` dentro de una clase `@Configuration` (cuando la clase pertenece a una biblioteca externa y no se puede anotar). Por defecto se instancia una única vez (scope singleton) y se comparte con todos los componentes que la requieran.

---

## ✅ Checkpoint — Punto 2

1. ¿Qué es el contenedor de Spring y qué guarda adentro?
2. ¿Qué es un bean? ¿Qué tiene de especial la clase de un bean respecto de cualquier otra clase Java?
3. Nombrá las dos formas de registrar un bean. ¿Cuál es el **criterio exacto** para elegir una u otra?
4. ¿Por qué `RestTemplate` no puede llevar `@Component`? Contestá con la razón técnica, no con "porque es de Spring".
5. En `RestTemplateConfig`, ¿quién ejecuta el `new RestTemplate()`? ¿Cuántas veces se ejecuta?
6. La clase `Pais` está dentro del paquete escaneado. ¿Por qué **no** es un bean? ¿Qué la diferencia de `BuscadorDePaises`?
7. ¿Qué pasaría si movieras `BuscadorDePaises` a un paquete fuera de `ar.edu.utn.ba.ddsi.countries`? ¿Compilaría? ¿Funcionaría?
8. ¿Cuántas instancias de `RestTemplate` existen mientras la aplicación corre? ¿Cómo se llama ese comportamiento?

---

## 🎯 Qué viene en el Punto 3

El contenedor ya tiene los tres objetos adentro. Pero `BuscadorDePaises` **necesita** dos de ellos, y su constructor los exige.

¿Cómo salen del contenedor y llegan al constructor? ¿Y por qué el test escribe `@Autowired` mientras que `BuscadorDePaises` no escribe nada?

Ahí **se cierra el hilo del Punto 1**: vas a poder responder, con nombre y apellido, quién hace el `new`.

---

**FIN DEL PUNTO 2**
