# Clase desde cero — Cliente Liviano — Módulo 0
# Armar el proyecto y verlo levantar

**Fase A — Ver funcionar** · Densidad general: 🟡 (operativo: no te lo toman, pero sin esto no existe nada de lo que sigue)

---

## Sobre este documento

**Cubre:** qué es un servidor de front y qué es Thymeleaf de verdad (no cómo se usa: qué hace); qué dependencias necesita el proyecto y para qué sirve cada una; cómo crearlo, tanto suelto como módulo dentro de un proyecto multi-módulo; dónde va cada archivo; y cómo llegar a ver una primera pantalla armada por el servidor en tu navegador.

**NO cubre:** por qué esto se llama "cliente liviano" y cuál es la alternativa (módulo 1); la sintaxis de plantillas más allá de lo mínimo para la primera pantalla (módulo 2); formularios (3); seguridad (4, 5, 6); conectarse a otros servicios (7). Si algo de eso aparece acá, es como semilla, no como explicación.

## De dónde venís

Se asume lo que la materia ya dio en clases anteriores. Una línea de refresco de cada cosa, por si no lo tenés a mano:

- **Maven y el `pom.xml`:** Maven es la herramienta que construye el proyecto Java; el `pom.xml` es su archivo de configuración, donde se declaran las dependencias (librerías) que el proyecto usa.
- **Spring Boot:** el framework que arma un servidor Java con casi nada de configuración. Una clase anotada con `@SpringBootApplication` arranca todo.
- **`@RestController` y `@GetMapping`:** hasta ahora tus controllers respondían con JSON. Eso va a cambiar en este módulo, y vas a ver exactamente en qué.
- **Lombok:** anotaciones (`@Getter`, `@Setter`, `@Data`) que generan el código repetitivo de las clases.
- **DTO:** objeto simple que solo transporta datos entre capas. Lo que entra y sale de un *service* es un DTO.

Con eso alcanza. Todo lo demás se explica acá.

---

## 1. 🟡 Qué vamos a construir (el mapa en treinta segundos)

Hasta ahora, tus servicios hacen esto:

```
  Postman / otro programa  ──GET /alumnos──►  Servicio  ──JSON──►  Postman / otro programa
                                                                   (lo lee un programa)
```

Un **servidor de front** hace esto otro:

```
  Navegador  ──GET /alumnos──►  Servidor de front  ──HTML completo──►  Navegador
  (persona)                     (arma la pantalla)                     (solo la muestra)
```

La diferencia está en **qué viaja de vuelta**: antes, datos crudos (JSON) para que otro programa haga algo con ellos. Ahora, una **pantalla ya armada** (HTML) para que una persona la vea. El navegador no arma nada: recibe el HTML y lo dibuja.

Un **servidor de front** es, entonces, un servidor Spring Boot cuya única responsabilidad es **armar pantallas**. Eso es todo lo que hay que tener en la cabeza para este módulo.

---

## 2. 🔴 Thymeleaf: qué es de verdad

El servidor de front necesita una pieza que sepa fabricar HTML con datos adentro. Esa pieza es **Thymeleaf**, y es un **motor de plantillas** (*template engine*).

> **Motor de plantillas:** un programa que corre **en el servidor**, toma (a) un archivo HTML con huecos marcados y (b) un conjunto de datos, y produce (c) un HTML final sin huecos, listo para que el navegador lo muestre.

Una analogía exacta: la carta modelo con espacios en blanco.

```
   PLANTILLA (el molde)                 DATOS                        RESULTADO (lo que viaja)
   ─────────────────────                ─────                        ────────────────────────
   Estimado ______,           +   nombre = "Juan"         →          Estimado Juan,
   su legajo es ______.           legajo = "12345"                   su legajo es 12345.
```

La plantilla se escribe **una vez**. Los datos cambian en cada pedido. Thymeleaf hace el reemplazo cada vez que alguien pide la pantalla.

```
        ┌──────────────────────┐
        │  plantilla .html     │
        │  (HTML con huecos    │──┐
        │   marcados con th:)  │  │      ┌───────────┐       ┌──────────────────┐
        └──────────────────────┘  ├─────►│ THYMELEAF │──────►│  HTML puro,      │────► navegador
        ┌──────────────────────┐  │      │ (servidor)│       │  sin huecos      │
        │  datos               │──┘      └───────────┘       └──────────────────┘
        │  (el Model, sección 6)│
        └──────────────────────┘
```

Dos consecuencias que vas a usar todo el tiempo:

1. **El navegador nunca ve los huecos.** Le llega el HTML ya resuelto. Si alguna vez abrís "ver código fuente" en el navegador y ves algo con `th:` adentro, algo falló en el servidor (o abriste el archivo directamente en vez de pedírselo al servidor).
2. **Las plantillas son archivos `.html` normales** con unos atributos extra que empiezan con `th:`. Un `.html` que ya tenés maquetado se convierte en plantilla agregándole esos atributos donde haga falta, sin reescribirlo. Esa es la razón por la que las maquetas HTML previas no se tiran: **son el insumo**.

🎓 **Si te lo preguntan — ¿qué es un motor de plantillas y dónde se ejecuta?**
Es el componente que combina una plantilla HTML con datos y genera el HTML final. Se ejecuta **del lado del servidor**: el navegador recibe HTML ya resuelto y solo lo muestra.

---

## 3. 🟡 Las piezas del proyecto: dependencias y qué hace cada una

Un proyecto Spring Boot se arma sumando **starters**.

> **Starter:** una dependencia de Maven que, en vez de traer una sola librería, trae un **conjunto de librerías ya compatibles entre sí** para una función. En lugar de declarar quince `.jar` que tienen que llevarse bien, declarás un starter.

Para un servidor de front necesitás estos:

| Starter | Qué trae | Por qué lo necesitás |
|---|---|---|
| `spring-boot-starter-webmvc` *(Spring Boot 4)* / `spring-boot-starter-web` *(Spring Boot 3)* | Spring MVC + un **Tomcat embebido** | Para recibir pedidos HTTP y devolver respuestas. Tomcat es el servidor web que viene **adentro** del `.jar` y escucha en el puerto. |
| `spring-boot-starter-thymeleaf` | El motor Thymeleaf + su integración con Spring | Para que al devolver un nombre de pantalla, Spring sepa ir a buscar la plantilla y pasársela a Thymeleaf. |
| `lombok` | Lombok | Lo de siempre. En un multi-módulo suele venir heredado del `pom` padre. |
| `spring-boot-starter-test` | JUnit y compañía | Tests. También suele venir heredado. |

⚠️ **Fijate el nombre del starter web.** En Spring Boot **4** se llama `spring-boot-starter-webmvc`. En Spring Boot **3** se llamaba `spring-boot-starter-web`. Los dos hacen lo mismo; solo cambió el nombre. Si Maven te dice *"Could not find artifact spring-boot-starter-webmvc"*, estás en la versión 3 y va el nombre viejo. Si estás en la 4 y usás el nombre viejo, todavía funciona pero está marcado como obsoleto.

Más adelante, en el módulo 6, se suman dos starters más para seguridad. Hoy no: **hilo H5 abierto** — todas las rutas que hagas hoy son públicas, y cualquiera que conozca la URL entra.

> 🕳️ **Madriguera — `spring-boot-starter-devtools`**
> Un starter que reinicia el servidor solo cuando guardás un archivo, para no tener que pararlo y arrancarlo a mano. Cómodo, pero no forma parte de lo que la materia usa ni evalúa.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 4. 🟡 Crear el proyecto: dos caminos

Hay dos situaciones posibles, y las dos son válidas. Elegí una.

🔴 **Bifurcación — anotá qué elegiste.** Define cómo levantás el proyecto en la sección 6.

- **Camino A — Proyecto suelto.** Un repositorio nuevo, solo, con su propio `pom.xml`. Es el caso de un ejercicio o de un proyecto que no comparte nada con otros.
- **Camino B — Módulo dentro de un multi-módulo.** El proyecto ya existe, tiene un `pom.xml` padre que lista varios módulos (uno por servicio), y el servidor de front es **uno más de esos módulos**. Es el caso de un TP con varios servicios en el mismo repositorio.

### Camino A — Proyecto suelto con Spring Initializr

**Spring Initializr** es un generador oficial en `start.spring.io`: le decís qué versión y qué starters querés y te devuelve un `.zip` con el proyecto armado y el `pom.xml` correcto **para esa versión**. Es la forma más segura de no equivocarse con los nombres de los starters.

En la página elegís:

| Campo | Valor | Por qué |
|---|---|---|
| Project | **Maven** | Es lo que usa la materia. |
| Language | **Java** | — |
| Spring Boot | **La versión que uses en tu TP** (por ejemplo 4.0.x) | Para que lo que aprendas acá se lleve sin adaptar. Si el proyecto es suelto y no tiene que convivir con nada, cualquier versión reciente sirve. |
| Group | `ar.edu.utn.frba.ddsi` | Convención de paquete de la cátedra. |
| Artifact | `front-server` | El nombre del proyecto. |
| Packaging | **Jar** | Un `.jar` ejecutable que trae Tomcat adentro. |
| Java | **21** | La versión de Java del TP. |
| Dependencies | **Spring Web**, **Thymeleaf**, **Lombok** | Los starters de la sección 3. "Spring Web" en la versión 4 genera el starter `webmvc`. |

Botón **Generate** → baja un `.zip` → lo descomprimís → en IntelliJ: *File → Open* → elegís la carpeta que tiene el `pom.xml`. IntelliJ detecta que es Maven y baja las dependencias solo (tarda un minuto la primera vez).

✅ **Deberías ver:** en el panel de proyecto, `src/main/java`, `src/main/resources`, `pom.xml`; y adentro del `pom.xml`, los tres starters listados. Si el `pom.xml` aparece en rojo, IntelliJ todavía está bajando dependencias: esperá.

### Camino B — Módulo dentro de un multi-módulo

Primero, la idea de **multi-módulo**, en tres líneas:

> Un proyecto multi-módulo tiene un `pom.xml` **padre** en la raíz, con `<packaging>pom</packaging>` (no produce un `.jar`: solo coordina) y una lista `<modules>`. Cada módulo es una carpeta con su propio `pom.xml`, que declara al padre como `<parent>` y **hereda** de él: versiones, dependencias comunes y configuración de plugins. Por eso en los módulos las dependencias **no llevan `<version>`**: la fija el padre.

```
  mi-tp/
  ├── pom.xml                ← PADRE: packaging pom, <modules>, versiones
  ├── common-lib/
  │   └── pom.xml            ← módulo
  ├── donaciones-service/
  │   └── pom.xml            ← módulo
  ├── logistica-service/
  │   └── pom.xml            ← módulo
  └── front-server/          ← NUEVO módulo, hermano de los otros
      └── pom.xml
```

El servidor de front **no es un proyecto adentro de otro**. Es un hermano más, al lado de los servicios.

**Paso 1.** Creá la carpeta `front-server/` en la raíz, al lado de los otros módulos.

**Paso 2.** Adentro, el `pom.xml` del módulo:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!-- QUIÉN ES MI PADRE: de acá heredo versiones, dependencias comunes y plugins.
       Los tres valores tienen que coincidir EXACTO con los del pom.xml de la raíz. -->
  <parent>
    <groupId>ar.edu.utn.frba.ddsi</groupId>
    <artifactId>ddsi-tp-template</artifactId>
    <version>1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>   <!-- dónde está el padre, relativo a esta carpeta -->
  </parent>

  <!-- QUIÉN SOY YO: solo el artifactId; groupId y version los heredo del padre. -->
  <artifactId>front-server</artifactId>
  <packaging>jar</packaging>                   <!-- este módulo SÍ produce un .jar ejecutable -->

  <dependencies>
    <!-- Spring MVC + Tomcat embebido: recibir HTTP, devolver respuestas.
         SIN <version>: la fija el padre a través del BOM de Spring Boot. -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-webmvc</artifactId>   <!-- Boot 4. En Boot 3: spring-boot-starter-web -->
    </dependency>

    <!-- El motor de plantillas + su integración con Spring. -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Lombok y spring-boot-starter-test NO se declaran: ya vienen del padre. -->
  </dependencies>

  <build>
    <plugins>
      <!-- El plugin que convierte este módulo en un .jar ejecutable y habilita "spring-boot:run".
           Sin <version> ni configuración: el padre ya lo definió en <pluginManagement>.
           Acá solo lo ACTIVÁS para este módulo. Si falta, el módulo compila pero no arranca. -->
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

> **BOM** (*Bill of Materials*): una lista maestra de versiones compatibles entre sí que el `pom` padre importa una sola vez. Gracias a eso, los módulos declaran dependencias sin versión y Maven la resuelve solo. Es por lo que `spring-boot-starter-thymeleaf` arriba no dice qué versión es.

**Paso 3.** En el `pom.xml` **padre**, agregá el módulo a la lista:

```xml
  <modules>
    <module>common-lib</module>
    <module>donaciones-service</module>
    <module>notificaciones-service</module>
    <module>incentivos-service</module>
    <module>logistica-service</module>
    <module>front-server</module>          <!-- ← la única línea nueva -->
  </modules>
```

**Paso 4.** En IntelliJ: panel *Maven* (a la derecha) → botón de recargar (🔄 *Reload All Maven Projects*).

✅ **Deberías ver:** `front-server` aparece en el panel Maven junto a los otros módulos, y su `pom.xml` sin nada en rojo.

🧩 **En tu TP:** si el módulo de front ya lo creó otra persona del equipo, no lo creás vos: **lo leés**. Abrí su `pom.xml` y buscá estas mismas piezas: el `<parent>`, los dos starters, el plugin. Ahora sabés qué hace cada línea y por qué está. Si falta alguna, ya sabés cuál y qué rompe.

---

## 5. 🔴 Dónde va cada cosa: la anatomía del módulo

Sea cual sea el camino, la estructura interna es la misma. Esto hay que sabérselo de memoria, porque Spring **busca cada tipo de archivo en un lugar fijo** y si lo ponés en otro, no lo encuentra.

```
front-server/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/ar/edu/utn/frba/ddsi/front/
    │   │   ├── FrontServerApplication.java   ← la clase que arranca todo
    │   │   ├── controllers/                  ← reciben el pedido, eligen qué pantalla devolver
    │   │   ├── dto/                          ← los datos que viajan a las pantallas (módulo 7)
    │   │   └── services/                     ← van a buscar los datos (módulos 2 y 7)
    │   └── resources/
    │       ├── application.properties        ← configuración: puerto, URLs de otros servicios
    │       ├── templates/                    ← LAS PLANTILLAS .html  ◄── acá van tus maquetas
    │       │   ├── layout/                   ← lo que se comparte entre pantallas (módulo 2)
    │       │   └── ...                       ← una carpeta por recurso: alumnos/, donaciones/...
    │       └── static/                       ← css/, js/, img/  ◄── se entregan tal cual, sin procesar
    │           ├── css/
    │           ├── js/
    │           └── img/
    └── test/java/...
```

**La regla que lo ordena todo:**

| Carpeta | Qué pasa con lo que ponés ahí |
|---|---|
| `templates/` | **Pasa por Thymeleaf.** Un controller devuelve el nombre de un archivo de acá, Thymeleaf lo procesa y lo llena con datos. No se accede por URL directa. |
| `static/` | **No pasa por nada.** Se entrega byte a byte cuando el navegador lo pide por URL: `/css/style.css` → `static/css/style.css`. CSS, JavaScript, imágenes. |

⚠️ Un `.html` puesto en `static/` se entrega tal cual, **con los `th:` sin resolver**. Si ves atributos `th:` en el navegador, primero revisá en qué carpeta está el archivo.

🧩 **En tu TP:** tus maquetas de la entrega anterior tienen una carpeta con `.html` sueltos y subcarpetas `css/`, `js/`, `assets/`. La traducción es directa: los `.html` van a `templates/`, y `css/`, `js/` y `assets/` van a `static/`, sin tocarlos. Lo único que desaparece es cualquier archivo de datos de prueba escritos a mano (un `datos.js`, por ejemplo): esos datos ahora los va a poner el servidor.

---

## 6. 🔴 La primera pantalla: del controller al navegador

Ahora sí, a ver algo andar. Cuatro archivos y un comando.

### 6.1 La clase que arranca

`src/main/java/ar/edu/utn/frba/ddsi/front/FrontServerApplication.java`

```java
package ar.edu.utn.frba.ddsi.front;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication          // "esto es una app Spring Boot": activa la autoconfiguración y
                                // escanea este paquete y sus subpaquetes buscando @Controller, @Service, etc.
public class FrontServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrontServerApplication.class, args);   // arranca el Tomcat embebido y carga todo
    }
}
```

⚠️ La clase con `@SpringBootApplication` tiene que estar en el **paquete raíz** (`...ddsi.front`), no adentro de `controllers/`. Spring escanea *hacia abajo* desde donde está esta clase: si la ponés en un subpaquete, no ve a los hermanos.

### 6.2 La configuración

`src/main/resources/application.properties`

```properties
spring.application.name=front-server
server.port=8090
```

`server.port` es el puerto en el que Tomcat escucha. Si no lo ponés, es `8080`.

⚠️ **Elegí un puerto que no esté ocupado.** Si tenés varios servicios corriendo en la misma máquina, cada uno tiene su puerto y no pueden compartirlo. Un `docker-compose` típico de TP ya usa `8080` a `8083` para los servicios; el servidor de front va a otro, `8090` por ejemplo. Si arrancás y ves *"Port 8090 was already in use"*, alguien más está ahí: cambiá el número.

### 6.3 El controller

`src/main/java/ar/edu/utn/frba/ddsi/front/controllers/HomeController.java`

```java
package ar.edu.utn.frba.ddsi.front.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller                                   // OJO: @Controller, NO @RestController. Ver la diferencia abajo.
public class HomeController {

    @GetMapping("/")                          // responde al GET de la raíz: http://localhost:8090/
    public String home(Model model) {         // Model: Spring te lo inyecta. Es la "bolsa" de datos
                                              // que le vas a pasar a la plantilla.
        model.addAttribute("titulo", "DonaTrack");                      // clave "titulo"  → valor
        model.addAttribute("mensaje", "El servidor de front está vivo"); // clave "mensaje" → valor

        return "index";                       // el NOMBRE de la plantilla: templates/index.html
                                              // Sin ".html": Spring lo agrega solo.
    }
}
```

**La diferencia entre `@RestController` y `@Controller`, que es el corazón de este módulo:**

| | `@RestController` (lo que usabas) | `@Controller` (lo que usás ahora) |
|---|---|---|
| Qué es el `return` | **El cuerpo de la respuesta.** Si devolvés un objeto, Spring lo convierte a JSON y lo manda. | **El nombre de una plantilla.** Spring va a `templates/`, busca ese archivo, se lo da a Thymeleaf. |
| Qué recibe el cliente | Datos (JSON) | Una pantalla (HTML) |
| `return "index"` significa | Mandá el texto `index` como respuesta | Renderizá `templates/index.html` |

❌ Con `@RestController`, `return "index"` devuelve literalmente la palabra *index* en el cuerpo. El navegador muestra "index" y nada más.
✅ Con `@Controller`, `return "index"` devuelve la pantalla completa.

Mismo `return`, resultados completamente distintos. Es **la** anotación que convierte un servicio que devolvía JSON en un servidor que devuelve pantallas.

> **`Model`:** un objeto que Spring crea y te pasa como parámetro. Todo lo que metés con `addAttribute("clave", valor)` queda visible en la plantilla bajo esa clave. Es el único puente entre el Java del controller y el HTML de la plantilla.

⚠️ Si el nombre que devolvés no coincide con un archivo en `templates/`, el error aparece **al pedir la página**, no al compilar: *"Error resolving template [index]"*. Revisá nombre y carpeta.

### 6.4 La plantilla

`src/main/resources/templates/index.html`

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">   <!-- declara el "idioma" th:. No es obligatorio para
                                                            que funcione, pero sin esto el IDE marca en rojo
                                                            cada th: que escribas. -->
<head>
    <meta charset="UTF-8">

    <!-- th:text="${titulo}": "reemplazá el CONTENIDO de esta etiqueta por el valor de la clave 'titulo'
         del Model". Lo que está escrito entre <title> y </title> ("Título de prueba") es un relleno:
         se ve solo si abrís el archivo suelto, y desaparece cuando lo sirve el servidor. -->
    <title th:text="${titulo}">Título de prueba</title>

    <!-- @{/css/style.css}: "armá la URL de este recurso". Como termina en .css, Thymeleaf entiende
         que es un archivo de static/ y arma /css/style.css. th:href reemplaza el atributo href. -->
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <!-- Mismo mecanismo: el h1 se rellena con la clave 'mensaje' del Model. -->
    <h1 th:text="${mensaje}">Mensaje de prueba</h1>

    <p>Si estás leyendo esto en el navegador, el servidor armó la pantalla y el navegador la mostró.</p>
</body>
</html>
```

Tres cosas nuevas, y son las únicas que este módulo necesita de Thymeleaf:

- **`${clave}`** — "buscá `clave` en el Model". Es la forma de leer lo que el controller puso.
- **`th:text`** — "reemplazá el texto interior de esta etiqueta por lo que dé la expresión".
- **`@{/ruta}`** — "armá una URL". Cuando la ruta termina en una extensión de archivo (`.css`, `.js`, `.png`), Thymeleaf la trata como recurso de `static/`; cuando no tiene extensión, la trata como una ruta del servidor (eso se usa en el módulo 2).

El resto de la sintaxis (listas, condicionales, formularios) viene en los módulos 2 y 3.

### 6.5 Un CSS mínimo, para ver que los estáticos también llegan

`src/main/resources/static/css/style.css`

```css
body {
    font-family: system-ui, sans-serif;
    background-color: #f8f9fa;
    padding: 2rem;
}
h1 {
    color: #0d6efd;      /* si el h1 sale azul, el CSS llegó desde static/ */
}
```

### 6.6 Levantarlo

**Desde IntelliJ (recomendado):** abrí `FrontServerApplication.java` y hacé clic en el ▶ verde al lado de `main`. IntelliJ resuelve solo las dependencias entre módulos.

**Desde terminal, Camino A (proyecto suelto):**

```bash
./mvnw spring-boot:run
```

> **`mvnw`** es el *Maven wrapper*: un script que viene con el proyecto generado por Initializr y baja la versión correcta de Maven si no la tenés. Si tenés Maven instalado, `mvn spring-boot:run` hace lo mismo.

**Desde terminal, Camino B (multi-módulo):**

```bash
cd front-server
mvn spring-boot:run
```

Si Maven se queja de que no encuentra otro módulo del mismo proyecto (por ejemplo `common-lib`), es porque ese módulo nunca se instaló en tu repositorio local. Una sola vez, desde la **raíz**:

```bash
mvn install -DskipTests      # construye todos los módulos y los deja disponibles para los hermanos
```

y volvés a intentar.

✅ **Deberías ver en la consola**, entre muchas líneas, estas dos:

```
Tomcat started on port 8090 (http) with context path '/'
Started FrontServerApplication in 2.3 seconds
```

Abrí el navegador en `http://localhost:8090/`.

✅ **Deberías ver:** la pestaña dice **DonaTrack**, el título grande y azul dice **El servidor de front está vivo**, y abajo el párrafo. Si el título está en negro y sin estilo, el CSS no llegó: revisá que esté en `static/css/`.

✅ **Y en las herramientas de desarrollador** (`F12` o `Cmd+Option+I`), pestaña *Network*, recargá la página. Tenés que ver **dos requests**: primero el documento (`localhost`, tipo *document*, 200) y después `style.css` (tipo *stylesheet*, 200). Con una página real vas a ver más filas (un CSS de Bootstrap, dos o tres `.js`, una fuente), pero siempre la misma forma: **una fila para el HTML y una por cada archivo que el HTML referencia.** Esa lista es el paso 10 hecho visible.

✅ **Y una verificación más, que vale oro:** en el navegador, *Ver código fuente* (`Cmd+Option+U` en Mac, `Ctrl+U` en Windows/Linux). Tenés que ver:

```html
<title>DonaTrack</title>
...
<h1>El servidor de front está vivo</h1>
```

**Ni un `th:` a la vista.** Ese es el motor de plantillas haciendo su trabajo: el navegador recibió HTML resuelto.

```
// ¿CÓMO FUNCIONA?
//
//  1. El navegador manda: GET http://localhost:8090/
//  2. Tomcat (embebido en tu .jar) recibe el pedido y se lo pasa a Spring.
//  3. Spring busca un método anotado con @GetMapping("/"). Encuentra HomeController.home().
//  4. Lo ejecuta. El método mete dos claves en el Model y devuelve el String "index".
//  5. Como la clase es @Controller (no @RestController), Spring interpreta "index" como
//     NOMBRE DE PLANTILLA: va a src/main/resources/templates/index.html.
//  6. Le pasa a Thymeleaf la plantilla + el Model.
//  7. Thymeleaf recorre el HTML: donde ve th:text="${titulo}" pone "DonaTrack";
//     donde ve th:text="${mensaje}" pone "El servidor de front está vivo";
//     donde ve th:href="@{/css/style.css}" pone href="/css/style.css".
//  8. Devuelve un HTML puro. Spring lo manda como respuesta: 200 OK, Content-Type: text/html.
//  9. El navegador lo dibuja.
// 10. Al dibujarlo, el navegador se encuentra con <link href="/css/style.css"> y hace
//     OTRO pedido: GET /css/style.css. Spring lo sirve desde static/ tal cual. Se aplica el estilo.
//
//  Resultado esperado: pestaña "DonaTrack", h1 azul "El servidor de front está vivo".
//  Dos requests en total: una para la página, otra para el CSS.
```

Ese paso 10 es importante y se olvida: **el HTML viaja primero, y los archivos que referencia (CSS, JS, imágenes) el navegador los pide después, uno por uno.** El JavaScript que incluyas en una plantilla **no corre en el servidor**: corre en el navegador, después de que la página llegó.

Dibujado:

```
  Navegador                                         Servidor de front
     │                                                     │
     │──── GET / ─────────────────────────────────────────►│
     │                                                     │ controller → Model → Thymeleaf
     │◄─── 200 OK  text/html  (index.html ya resuelto) ────│
     │                                                     │
     │ (lee el HTML, ve el <link>)                         │
     │──── GET /css/style.css ────────────────────────────►│
     │◄─── 200 OK  text/css  (tal cual, desde static/) ────│
     │                                                     │
     │ (dibuja la página con estilo)                       │
```

🎓 **Si te lo preguntan — ¿qué diferencia hay entre `@RestController` y `@Controller`?**
En un `@RestController` el valor que devuelve el método **es la respuesta** (se serializa a JSON). En un `@Controller` el `String` que devuelve el método **es el nombre de una plantilla**, que se renderiza y se devuelve como HTML. Por eso un servidor de front usa `@Controller`.

---

## 7. 🟢 Cuando no anda: los errores de la primera hora

| Lo que ves | Qué es | Qué revisar |
|---|---|---|
| Página blanca con *"Whitelabel Error Page"* y 404 | Ningún controller respondió a esa ruta | ¿La clase tiene `@Controller`? ¿El método tiene `@GetMapping` con esa ruta? ¿La clase está bajo el paquete raíz? |
| *"Error resolving template [index]"* | Spring encontró el controller pero no la plantilla | El `return` tiene que coincidir con `templates/index.html`. Sin `.html`, y en la carpeta correcta. |
| *"Port 8090 was already in use"* | Otro proceso escucha en ese puerto | Cambiá `server.port`, o cerrá lo que esté usando el puerto. |
| Ves `th:text` en el navegador | El HTML no pasó por Thymeleaf | ¿Abriste el archivo con doble clic (`file://`) en vez de ir a `localhost`? ¿El `.html` está en `static/` en vez de `templates/`? |
| *"Could not find artifact spring-boot-starter-webmvc"* | Nombre de starter de Boot 4 en un proyecto Boot 3 | Cambiá a `spring-boot-starter-web`. |
| El título sale negro y sin estilo | El CSS no llegó | ¿Está en `static/css/style.css`? ¿El `th:href` dice `@{/css/style.css}`? Mirá la pestaña *Network* del navegador: el `GET /css/style.css` tiene que dar 200. |

---

🧩 **En tu TP** — con lo que acabás de ver, cuando abras la branch del módulo de front de tu equipo vas a poder reconocer: el `pom.xml` con sus starters y su plugin; la clase `@SpringBootApplication`; `application.properties` con el puerto (chequeá que no choque con los del `docker-compose`); `templates/` con las pantallas que ya estén y `static/` con el CSS. Todo lo que no reconozcas de ahí en adelante es materia de los módulos 2 en adelante, no de este.

---

## Checkpoint del módulo 0

Sin respuestas. Si alguna no te sale, es la duda para llevar al chat antes del módulo 1.

1. ¿Dónde se ejecuta Thymeleaf: en el navegador o en el servidor? ¿Qué recibe el navegador?
2. ¿Qué es un *starter* y por qué es mejor que declarar las librerías una por una?
3. Sin mirar: ¿en qué carpeta va un archivo `.html` que Thymeleaf tiene que procesar, y en qué carpeta va un `.css`? ¿Qué pasa si los invertís?
4. Un método de un `@RestController` devuelve `"index"`. Un método de un `@Controller` devuelve `"index"`. ¿Qué recibe el navegador en cada caso?
5. ¿Qué es el `Model` y quién lo crea?
6. ¿Por qué las dependencias del `pom.xml` de un módulo no llevan `<version>`? ¿Quién la fija?
7. ¿Qué diferencia hay entre `spring-boot-starter-web` y `spring-boot-starter-webmvc`?
8. Después de que el navegador recibe el HTML, ¿cuántas requests más hace, y para qué?
9. ¿Por qué la clase con `@SpringBootApplication` tiene que estar en el paquete raíz y no adentro de `controllers/`?
10. Un compañero te dice que su JavaScript "corre en el servidor porque está en el proyecto Spring". ¿Tiene razón?

---

## Qué viene en el módulo 1

Ya viste un HTML salir del servidor y llegar al navegador. Ahora toca ponerle nombre a lo que hiciste: eso se llama **cliente liviano**, y existe una alternativa, el **cliente pesado**, que hace lo contrario. Vas a ver la diferencia entre las dos, qué significa que el servidor de front sea **desacoplado** (spoiler: los datos no van a estar en tu controller escritos a mano por mucho tiempo — **hilo H2**), qué es un **recurso** y por qué define qué pantallas hacés, y una pregunta incómoda que va a quedar abierta hasta el módulo 4: ¿cómo sabe el servidor quién es la persona que le está pidiendo la pantalla?

---

**FIN DEL MÓDULO 0**
