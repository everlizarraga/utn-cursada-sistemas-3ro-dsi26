# Recorrido Spring Boot — Punto 1
## Framework vs biblioteca: quién llama a quién

---

## 📄 Sobre este documento

**Qué cubre:** el concepto que sostiene todo Spring — la **inversión de control**. Qué es un framework (y en qué se diferencia de una biblioteca), qué pasa realmente cuando corrés `main()` en un proyecto Spring Boot, y por qué las anotaciones de Spring no son como las que ya viste.

**Qué NO cubre:** cómo se instancian los objetos (Punto 2), cómo te llegan (Punto 3), ni nada de la API REST (Puntos 5 y 6). Acá solo levantamos el piso conceptual.

**Cuánto es:** un concepto grande y dos chicos. Lectura, sin código para escribir.

---

## 🎒 De dónde venís

Se asume que ya tenés (de `preclase02` y de tu entrenamiento previo):

- **Java:** clases, objetos, `new`, constructores, interfaces, herencia, modificadores de acceso.
- **`main()`:** sabés que todo programa Java arranca por ahí.
- **Maven:** sabés que el `pom.xml` declara dependencias y que Maven las baja.
- **Annotations:** las viste en preclase02 (`@Override`, `@Test`). Ojo con esto — en este punto vamos a matizarlo fuerte.
- **De tu experiencia con JS:** Express (rutas y handlers) y React. Los vamos a usar como ancla.

Nada más. Si tenés eso, arrancamos.

---

## 1. El enigma: el `new` que no está 🔴

Antes de definir nada, mirá el problema. Este es un recorte real de `BuscadorDePaises.java`, la clase central del repo del profe:

```java
@Component                                    // ← una anotación que nunca viste
public class BuscadorDePaises {

    private final RestTemplate restTemplate;          // dos atributos...
    private final RestCountriesProperties propiedades;

    // ...y un constructor que los pide obligatoriamente
    public BuscadorDePaises(RestTemplate restTemplate, RestCountriesProperties propiedades) {
        this.restTemplate = restTemplate;
        this.propiedades = propiedades;
    }

    // (los métodos de búsqueda van acá — los vemos en el Punto 6)
}
```

Y así se usa, en el test (`BuscadorDePaisesIT.java`):

```java
@SpringBootTest                              // ← otra anotación que nunca viste
class BuscadorDePaisesIT {

    @Autowired                               // ← y otra más
    private BuscadorDePaises buscadorDePaises;   // el atributo está declarado... y ya

    @Test
    void buscarTodosDevuelveVariosPaises() {
        var lista = buscadorDePaises.buscarTodos();   // ...y funciona
        // ...
    }
}
```

**Frená acá y mirá lo que falta.**

Ese constructor **exige dos parámetros**. Y en todo el repo — te lo firmo, buscalo — **no existe una sola línea que diga:**

```java
new BuscadorDePaises(algo, otraCosa);   // ❌ ESTA LÍNEA NO EXISTE EN NINGÚN LADO
```

Tampoco existe un `new RestTemplate()` en el `BuscadorDePaises`, ni un `new RestCountriesProperties()`.

Y sin embargo el test corre, el atributo `buscadorDePaises` no es `null`, y los métodos funcionan.

> 🧵 **Este es el hilo del recorrido.** Alguien está haciendo esos `new`. Alguien está creando el `RestTemplate`, creando el `RestCountriesProperties`, pasándoselos al constructor de `BuscadorDePaises`, y después metiendo el resultado en ese atributo del test.
>
> **Ese alguien es Spring.** El *cómo* se cierra en el Punto 2 y el Punto 3. Pero el *por qué* — por qué un framework se arroga el derecho de hacer los `new` que vos deberías hacer — se contesta acá, ahora.

Escobar, en clase, lo dijo así: *"Nosotros nunca vamos a hacer un `new` de esta clase, sino que lo va a hacer Springboot"*. Y después: *"acá hay magia de Springboot por detrás"*.

**No hay magia. Hay inversión de control.**

---

## 2. Biblioteca vs framework: la única diferencia que importa 🔴

Los dos son **código que otro escribió y vos usás**. La diferencia no está en qué son, sino en **quién llama a quién**.

### Biblioteca: vos tenés el volante

Una **biblioteca** es un cajón de herramientas. Vos escribís el programa, y cuando necesitás algo, lo llamás.

```java
// Vos escribiste este main. Vos decidís cuándo llamar a la biblioteca.
public static void main(String[] args) {
    List<String> nombres = new ArrayList<>();     // ← VOS llamás a la biblioteca de colecciones
    nombres.add("Argentina");                     // ← VOS decidís cuándo
    nombres.add("Perú");

    Collections.sort(nombres);                    // ← VOS llamás a Collections
    System.out.println(nombres);
    // Resultado esperado: [Argentina, Perú]
}
// El flujo de control es TUYO de punta a punta.
// La biblioteca es pasiva: espera que la llames.
```

**Ejemplos de bibliotecas que ya usaste:** `java.util` (List, Map, Optional), Lombok, Jackson, `Math`. En JS: lodash, `date-fns`.

### Framework: el volante lo tiene él

Un **framework** es un esqueleto de aplicación ya construido, con **huecos** donde vos metés tu código. Vos no lo llamás: **él te llama a vos**.

Y esto vos **ya lo viviste**. Mirá Express, de DdS:

```javascript
// Express — esto ya lo hiciste
const app = express();

app.get('/paises', (req, res) => {          // ← vos REGISTRÁS un handler
    res.json({ mensaje: 'hola' });          //   pero NUNCA lo llamás
});

app.listen(3000);                            // ← acá le entregás el control a Express
// A partir de esta línea, el programa NO es tuyo.
// Express se queda escuchando, y cuando llega un GET a /paises,
// EXPRESS llama a TU función. Vos no la llamaste nunca.
```

¿Cuándo se ejecuta tu callback? No lo sabés. No lo decidís. Lo decide Express, cuando llega el request.

**Eso es inversión de control.**

### El principio, con nombre

> 🎯 **Inversión de Control (IoC — Inversion of Control)**
>
> **Qué es:** el flujo de control del programa lo maneja el framework, no tu código. Vos aportás las piezas; el framework decide cuándo crearlas, cuándo llamarlas y cómo conectarlas.
>
> **Por qué lo usamos:** porque el 90% del andamiaje de una aplicación (arrancar, escuchar un puerto, crear objetos, conectarlos, manejar el ciclo de vida) es **siempre el mismo**. El framework lo trae hecho. Vos escribís solo lo que tu aplicación tiene de particular.
>
> **Dónde lo ves en ESTE código:** en que no hay ningún `new BuscadorDePaises(...)`. Vos declaraste la clase y sus necesidades; Spring la crea y la conecta.
>
> **Analogía:** una **biblioteca** es un martillo — lo agarrás vos, cuando querés. Un **framework** es una fábrica con una cinta transportadora — la cinta ya está andando, y vos ponés operarios en las estaciones. La cinta no espera que la muevas: te trae el trabajo.
>
> **El eslogan clásico:** *"Don't call us, we'll call you"* — no nos llames, nosotros te llamamos. Lo vas a leer en todos lados como **"principio de Hollywood"**.

**Comparativo directo:**

| | Biblioteca | Framework |
|---|---|---|
| ¿Quién llama a quién? | **Vos** llamás al código de otro | **El otro** llama a tu código |
| ¿De quién es el `main()`? | Tuyo, y ahí manda | Del framework (aunque el archivo esté en tu proyecto) |
| ¿Podés no usarla? | Sí, borrás la línea y listo | No: el proyecto **está construido encima** |
| Ejemplo que ya usaste | `java.util.List`, lodash | **Express**, React, **Spring Boot** |

> 📌 **Para el parcial, si te preguntan: "¿Cuál es la diferencia entre una biblioteca y un framework?"**
>
> La diferencia es **quién controla el flujo de ejecución**. Con una biblioteca, tu código llama al código de la biblioteca cuando lo necesita: el control es tuyo. Con un framework, es al revés: el framework controla el flujo y llama a tu código en los puntos de extensión que vos completaste. A eso se lo llama **inversión de control**.
>
> *(La cátedra tiene una lectura previa dedicada a esto — "Biblioteca vs Framework" — como material de la clase 4. Cuando llegues, esta respuesta ya va a estar.)*

---

## 3. Qué pasa realmente cuando corrés `main()` 🔴

Ahora sí, el archivo de arranque del repo. Son 15 líneas y **cada una importa**:

```java
package ar.edu.utn.ba.ddsi.countries;   // ← ACORDATE DE ESTE PAQUETE. Vuelve en el Punto 2.

import ar.edu.utn.ba.ddsi.countries.config.RestCountriesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
// ↑ LA anotación. Le dice a Spring: "esta es la clase raíz de la aplicación".
//   Hace tres cosas a la vez (las desarmamos abajo).

@EnableConfigurationProperties(RestCountriesProperties.class)
// ↑ Un agregado manual del profe. Lo entendés en el Punto 4. Ignoralo por ahora.

public class CountriesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CountriesApplication.class, args);
		// ↑ ESTA LÍNEA ES EL MOMENTO DE LA ENTREGA DEL CONTROL.
		//   Es el equivalente EXACTO del app.listen(3000) de Express.
		//   A partir de acá, el programa deja de ser tuyo.
	}
}
```

**Fijate la trampa:** el archivo se llama `CountriesApplication`, tiene un `main()`, está en tu carpeta `src/main/java`. Parece tu programa. **No lo es.** Es el interruptor de encendido del framework. La única línea del `main` no hace nada tuyo: le entrega el control a Spring.

### `// ¿CÓMO FUNCIONA?` — el arranque, paso a paso

Cuando apretás ▶️ en IntelliJ, esto es lo que ocurre — **en este orden**:

```
 1. La JVM arranca y ejecuta main().

 2. main() llama a SpringApplication.run(CountriesApplication.class, args)
    y le pasa su propia clase como referencia. Traducido:
    "Spring, arrancá vos. Empezá a mirar desde acá."

 3. Spring crea el CONTENEDOR (una gran bolsa de objetos, vacía por ahora).
    → Qué es exactamente: PUNTO 2.

 4. Spring hace el COMPONENT SCAN: mira el paquete de CountriesApplication
    (ar.edu.utn.ba.ddsi.countries) y TODOS los sub-paquetes hacia abajo,
    buscando clases anotadas. Encuentra:
         · config/RestTemplateConfig        → @Configuration
         · config/RestCountriesProperties   → @ConfigurationProperties
         · services/BuscadorDePaises        → @Component
    ⚠️ Por eso el paquete de esta clase importa: define el TECHO del escaneo.
       Una clase fuera de ar.edu.utn.ba.ddsi.countries sería INVISIBLE para Spring.

 5. Spring INSTANCIA esas clases (acá se hacen los new que vos no escribiste)
    y las mete en el contenedor.
    → Cómo: PUNTO 2.

 6. Spring CONECTA los objetos entre sí: ve que BuscadorDePaises pide
    un RestTemplate y un RestCountriesProperties en su constructor,
    los busca en el contenedor, y se los pasa.
    → Cómo: PUNTO 3.

 7. Recién ahora la aplicación está viva y lista para trabajar.
```

**Los pasos 3 a 6 son la totalidad de la "magia".** Eso es todo. No hay más. Los tres puntos siguientes de este recorrido son, literalmente, desarmar los pasos 4, 5 y 6.

### ¿Y qué hace `@SpringBootApplication`? 🟡

Es **tres anotaciones en una**. No hace falta que las memorices — sí que sepas que existen, porque el nombre de la tercera va a aparecer en tu cabeza cuando algo no funcione:

| Contiene | Qué hace |
|---|---|
| `@SpringBootConfiguration` | Marca esta clase como fuente de configuración. |
| `@ComponentScan` | **El escaneo del paso 4.** Arranca desde el paquete de esta clase y baja. |
| `@EnableAutoConfiguration` | Configura automáticamente lo que detecta en el classpath. Si ve Jackson en las dependencias, lo configura solo. Si ve un servidor web, lo levanta. |

> 🕳️ **Madriguera — Auto-configuración y "starters"**
> Esa tercera pieza es la razón por la que Spring Boot se llama *Boot* y no solo *Spring*: el Spring clásico exigía cientos de líneas de XML configurando todo a mano; Boot lo deduce de las dependencias del `pom.xml`. Por eso la dependencia se llama `spring-boot-**starter**-webmvc`: un *starter* es un paquete que trae un conjunto de bibliotecas **y su configuración por defecto**.
> *Volvé al camino — no se profundiza en DSI. Si en algún momento algo "funciona solo" y no sabés por qué, la respuesta suele ser: auto-configuración.*

---

## 4. Ojo con las anotaciones: no son lo que viste en preclase02 🟡

Esto es importante porque **te puede pasar de largo**.

En `preclase02` viste anotaciones. Pero eran de otra especie:

```java
@Override                       // ← le avisa AL COMPILADOR que estás sobreescribiendo.
public String toString() {      //   Si te equivocás en la firma, no compila.
    return "...";               //   En RUNTIME no hace absolutamente nada.
}

@Test                           // ← le avisa a JUnit que este método es un test.
void miTest() { ... }           //   Es una etiqueta. No cambia el comportamiento del método.
```

Esas anotaciones son **metadatos**: información pegada al código, para que otro la lea. Inertes.

**Las de Spring no son inertes. Son instrucciones ejecutables.**

```java
@Component                            // ← esto NO es una etiqueta descriptiva.
public class BuscadorDePaises { ... } //   Es una ORDEN: "Spring, en el arranque,
                                      //   instanciá esta clase y guardala en el contenedor."
                                      //   Si la borrás, el objeto NO EXISTE y el test explota.
```

**La diferencia práctica:**

| | Anotación-metadato | Anotación de Spring |
|---|---|---|
| Ejemplos | `@Override`, `@Test`, `@JsonProperty` | `@Component`, `@Bean`, `@Autowired` |
| ¿Quién la lee? | El compilador, o el framework de test | **El contenedor de Spring, al arrancar** |
| Si la borrás | Compila igual (o falla un chequeo) | **El objeto no se crea. NullPointerException.** |
| Analogía | Un post-it en una caja | Un botón que enciende una máquina |

**Regla mental para todo el resto del recorrido:** cada vez que veas una `@` de Spring, preguntate *"¿qué le está ordenando esto al contenedor?"*. Nunca *"¿qué describe esto?"*. Son verbos, no adjetivos.

---

## 5. El resumen en una imagen 🔴

```
        ┌──────────── SIN FRAMEWORK (lo que venías haciendo) ────────────┐
        │                                                                │
        │   main()                                                       │
        │     │                                                          │
        │     ├──► new Servicio(new Dependencia(), new OtraCosa())       │
        │     ├──► servicio.hacerAlgo()                                  │
        │     └──► System.out.println(...)                               │
        │                                                                │
        │   El control es TUYO. Vos hacés todos los new.                 │
        └────────────────────────────────────────────────────────────────┘

        ┌──────────── CON SPRING BOOT (lo que hace el repo) ─────────────┐
        │                                                                │
        │   main()                                                       │
        │     └──► SpringApplication.run(...)   ← entregás el control    │
        │              │                                                 │
        │              ▼                                                 │
        │      ┌───────────────────────────────────┐                     │
        │      │   CONTENEDOR DE SPRING            │                     │
        │      │                                   │                     │
        │      │   1. escanea tus clases anotadas  │                     │
        │      │   2. las instancia  (los new)     │                     │
        │      │   3. las conecta entre sí         │                     │
        │      │                                   │                     │
        │      │   [RestTemplate] [Propiedades]    │                     │
        │      │            └─────┬─────┘          │                     │
        │      │                  ▼                │                     │
        │      │        [BuscadorDePaises]         │                     │
        │      └───────────────────────────────────┘                     │
        │                                                                │
        │   El control es de SPRING. Vos aportás las piezas.             │
        └────────────────────────────────────────────────────────────────┘
```

Todo lo que sigue en este recorrido es zoom sobre esa caja.

---

## ✅ Checkpoint — Punto 1

Sin mirar arriba. Si alguna no sale, volvé a la sección.

1. ¿Cuál es la diferencia entre una biblioteca y un framework? Dala en una sola oración.
2. ¿Qué significa "inversión de control"? ¿Qué es exactamente lo que se invierte?
3. En el repo `rest-paises`, ¿existe alguna línea que diga `new BuscadorDePaises(...)`? Si no, ¿quién crea ese objeto?
4. ¿Cuál es la línea exacta de `CountriesApplication` donde tu programa deja de ser tuyo? ¿Cuál es su equivalente en Express?
5. `@SpringBootApplication` contiene tres anotaciones. Nombrá la que hace el escaneo y explicá **desde dónde** escanea.
6. Si movieras `BuscadorDePaises` al paquete `com.ever.pruebas`, ¿seguiría funcionando el test? ¿Por qué?
7. ¿Qué diferencia hay entre `@Override` y `@Component`? No respondas "una es de Java y la otra de Spring" — la diferencia está en **cuándo actúan y qué pasa si las borrás**.

---

## 🎯 Qué viene en el Punto 2

Ya sabés que hay un **contenedor** que instancia tus clases. Ahora vamos adentro: qué es exactamente esa bolsa, qué es un **bean**, y cómo se le dice a Spring "esta clase es tuya, instanciala".

Y de paso vamos a abrir el archivo que el profe te dijo textualmente que **no mirases** (`RestTemplateConfig` — *"este código hoy no me importa que se entienda, copy-pasteale"*). Son ocho líneas, y son la mejor ventana al contenedor que hay en todo el repo.

Ahí también aparece la pregunta que casi nadie sabe contestar: **¿por qué `RestTemplate` necesita un `@Bean`, pero `BuscadorDePaises` se arregla con `@Component`?**

---

**FIN DEL PUNTO 1**
