# 🌱 Proyecto 1 — Etapa 6: Llamada HTTP (`RestTemplate`)

> **Objetivo:** hacer que tu app **salga a internet, llame a una API real y traiga datos**. Vas a usar todo lo que aprendiste: un bean creado con `@Bean` (el cliente HTTP), la URL de configuración (`@ConfigurationProperties`), y la inyección.
>
> **El momento clave:** vas a ver datos **de verdad** —países reales de una API en internet— aparecer en tu consola, traídos por tu propio código Spring.
>
> **Pre-requisito:** Etapas 1-5 completas. Necesitás internet para esta etapa (es una llamada HTTP real).
>
> **Tiempo estimado:** 50-60 minutos.

---

## 🧭 Mapa de esta etapa

1. Qué vamos a hacer.
2. Agregar la dependencia web (y ver cómo cambia el arranque).
3. Crear el cliente HTTP (`RestTemplate`) con `@Bean`.
4. Hacer la primera llamada (traer texto crudo).
5. Crear las clases para los datos (DTOs).
6. Hacer la llamada que devuelve objetos Java.
7. Cómo se convierte el JSON en objetos.
8. Disparar la llamada al arrancar (`CommandLineRunner`).
9. Experimentos.
10. Checkpoint.

---

## 🎯 Parte 1: Qué vamos a hacer

Hasta ahora tu app no hace nada "útil" — crea beans, los conecta, lee config. Ahora le vamos a dar un trabajo real: **consultar una API de países en internet y traer datos**.

Vamos a usar la API pública `restcountries.com`, que devuelve información de países en formato JSON. Por ejemplo, si pedís `https://restcountries.com/v3.1/name/argentina`, te devuelve datos de Argentina.

Para eso necesitás:
1. Un **cliente HTTP** — un objeto que sabe hacer pedidos a internet. Spring tiene uno llamado `RestTemplate`.
2. La **URL** de la API — que ya tenés en tu config (Etapa 5).
3. Clases para **guardar los datos** que vuelven (DTOs).

Vamos pieza por pieza.

---

## 📦 Parte 2: Agregar la dependencia web

Para hacer llamadas HTTP, necesitás una dependencia que no tenés: el **"starter web"** de Spring. Recordá de la Etapa 0 que creamos el proyecto **sin dependencias** a propósito. Ahora sumamos la primera.

Abrí el `pom.xml` y agregá esta dependencia dentro de `<dependencies>` (junto a las que ya tenés):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Notá que NO tiene `<version>`** — el parent de Spring Boot la define (lo viste en la Etapa 0).

Después de agregarla, **recargá Maven**: IntelliJ te muestra una notificación "Load Maven Changes" (o el ícono de Maven con una flechita) → hacele click. Va a descargar el starter web y todo lo que trae.

### Qué trae el starter web

`spring-boot-starter-web` incluye un montón de cosas: el cliente HTTP (`RestTemplate`), un servidor web embebido (Tomcat), la librería para JSON (Jackson), y más. Por ahora vamos a usar **el cliente HTTP y Jackson**. El servidor web (Tomcat) viene de regalo aunque no lo usemos todavía.

---

## 🔬 Parte 2.5: Corré la app y observá un cambio importante

**Antes de seguir, corré la app.** Mirá el log con atención. Vas a ver algo nuevo:

```
... Tomcat started on port(s): 8080 (http) ...
... Started PaisesApplication in 1.5 seconds ...
```

Y lo más importante: **la app NO termina.** Queda corriendo. La consola queda "ocupada", el botón de stop (cuadrado rojo) sigue activo.

### ¿Te acordás de la Etapa 1?

En la Etapa 1, tu app **arrancaba y terminaba sola** (exit code 0), porque no había servidor web. Te dije: *"el servidor web es una pieza opcional que se agrega aparte, y es lo que mantiene la app viva"*.

**Acabás de agregar esa pieza.** El starter web trajo Tomcat (el servidor web), y ahora Tomcat queda **escuchando en el puerto 8080**, esperando pedidos HTTP. Eso es lo que mantiene la app viva — ya no termina sola.

> **Comprobalo con el experimento de la Etapa 1:** si agregaras un `System.out.println("...")` después del `run()` en el `main`, el mensaje se imprimiría igual, **pero la app no terminaría** (a diferencia de la Etapa 1). El servidor la mantiene viva. Esta es la diferencia que te anticipé. Verla ahora cierra el círculo.

**Para detener la app:** botón rojo de stop en IntelliJ, o `Ctrl + C` en la consola.

> **Nota:** nuestra app no expone endpoints HTTP (no vamos a hacer eso en este proyecto), así que si vas a `http://localhost:8080` te va a dar un error 404. Tomcat está vivo, pero no tiene nada que responder. No importa — nosotros vamos a usar el cliente HTTP para **hacer** pedidos a otra API, no para **recibir** pedidos. El servidor queda ahí de regalo.

---

## 🛠️ Parte 3: Crear el cliente HTTP con `@Bean`

`RestTemplate` es el objeto de Spring que hace llamadas HTTP. Es una clase **de Spring, no tuya** — así que para tenerla como bean, usás `@Bean` (exactamente lo que aprendiste en la Etapa 4 con `Random`).

Abrí tu clase `AppConfig` (la de la Etapa 4) y agregá un método `@Bean` para el `RestTemplate`:

```java
package ar.edu.utn.ba.paises;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Configuration
public class AppConfig {

    @Bean
    public Random random() {
        return new Random();
    }

    @Bean
    public RestTemplate restTemplate() {
        System.out.println(">>> Se creó el RestTemplate");
        return new RestTemplate();
    }
}
```

**Es exactamente el mismo patrón que `Random`:** un método con `@Bean` que hace `new RestTemplate()` y lo devuelve. Spring lo ejecuta al arrancar, guarda el `RestTemplate` como bean, listo para inyectar.

> ¿Ves por qué la Etapa 4 usó `Random`? Era para que cuando llegaras acá, `RestTemplate` no fuera nada nuevo — el mecanismo es idéntico. Solo cambia que `RestTemplate` hace algo más interesante que `Random`.

---

## 📞 Parte 4: La primera llamada (texto crudo)

Antes de complicarnos con objetos, hagamos la llamada más simple posible: **traer la respuesta de la API como texto crudo**, para ver qué devuelve.

Modificá `CatalogoDePaises` para que reciba el `RestTemplate` y las `properties`, y agregá un método que haga la llamada:

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CatalogoDePaises {

    private final RestTemplate restTemplate;
    private final PaisesProperties properties;

    public CatalogoDePaises(RestTemplate restTemplate, PaisesProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String traerArgentinaComoTexto() {
        String url = properties.getUrl() + "/name/argentina";
        return restTemplate.getForObject(url, String.class);
    }
}
```

> **Nota:** saqué el `Random` del constructor para simplificar. Si lo tenías de la Etapa 4 y querés conservarlo, dejalo; no molesta. Lo importante acá es el `RestTemplate` y las `properties`.

### Qué hace `traerArgentinaComoTexto`

```java
String url = properties.getUrl() + "/name/argentina";
// url = "https://restcountries.com/v3.1/name/argentina"
//        ↑ la base viene de tu config (Etapa 5)

return restTemplate.getForObject(url, String.class);
//                  ↑              ↑    ↑
//          hace un GET a la URL   |    "devolveme la respuesta como String (texto crudo)"
```

`getForObject(url, String.class)` significa: "hacé un pedido HTTP GET a esa URL, y devolveme la respuesta como un `String`".

**Todavía no la podemos ver** porque nadie llama a este método. Lo vamos a disparar en la Parte 8. Por ahora seguí construyendo.

---

## 📋 Parte 5: Crear las clases para los datos (DTOs)

Traer texto crudo está bien para espiar, pero lo que querés son **objetos Java** con los datos, no un string gigante. Para eso, creás clases que representen la estructura de los datos que vuelven.

### Primero, mirá qué devuelve la API

Si vas en el navegador a `https://restcountries.com/v3.1/name/argentina?fields=name,capital,region,population`, vas a ver algo así (simplificado):

```json
[
    {
        "name": {
            "common": "Argentina",
            "official": "Argentine Republic"
        },
        "capital": ["Buenos Aires"],
        "region": "Americas",
        "population": 45376763
    }
]
```

Es un **array** (los `[...]`) con un objeto adentro. Ese objeto tiene un `name` (que a su vez es otro objeto con `common` y `official`), un `capital` (que es una lista), una `region` y una `population`.

### Crear las clases que representan eso

Vas a necesitar **dos clases**: una para el país, otra para el nombre (porque `name` es un objeto anidado).

**Clase `NombrePais`** (representa el objeto `name`):

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NombrePais {

    @JsonProperty("common")
    private String comun;

    @JsonProperty("official")
    private String oficial;

    public String getComun() { return comun; }
    public void setComun(String comun) { this.comun = comun; }
    public String getOficial() { return oficial; }
    public void setOficial(String oficial) { this.oficial = oficial; }
}
```

**Clase `Pais`** (representa el objeto país):

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pais {

    @JsonProperty("name")
    private NombrePais nombre;

    @JsonProperty("capital")
    private List<String> capitales;

    @JsonProperty("region")
    private String region;

    @JsonProperty("population")
    private Long poblacion;

    public NombrePais getNombre() { return nombre; }
    public void setNombre(NombrePais nombre) { this.nombre = nombre; }
    public List<String> getCapitales() { return capitales; }
    public void setCapitales(List<String> capitales) { this.capitales = capitales; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Long getPoblacion() { return poblacion; }
    public void setPoblacion(Long poblacion) { this.poblacion = poblacion; }
}
```

### Qué son esas annotations

- **`@JsonProperty("common")`** → conecta el campo Java (`comun`) con el campo del JSON (`common`). Como tienen nombres distintos (uno en español, otro en inglés), hay que decirle a la librería cuál es cuál.

- **`@JsonIgnoreProperties(ignoreUnknown = true)`** → "si el JSON trae campos que no están en mi clase, ignoralos sin romper". La API devuelve **muchísimos** campos; vos solo querés algunos. Sin esto, la conversión fallaría al encontrar campos que tu clase no tiene.

> **Estas annotations son de Jackson** (la librería de JSON que vino con el starter web). No tuviste que agregar Jackson — vino incluido.

> **Por qué wrappers (`Long`) y no primitivos (`long`):** si la API no manda la población de algún país, el campo queda `null`. Un `long` (primitivo) no puede ser `null`, pero un `Long` (wrapper) sí. Por eso, para datos que vienen de afuera, se usan wrappers. (Acordate del Proyecto 0: usabas `long` porque hardcodeabas; acá usás `Long` porque viene de una fuente externa que puede no traer el dato.)

> **Sobre los getters/setters:** los escribí a mano para que veas el mecanismo. Si tenés Lombok, podés reemplazarlos por `@Data` (más `@NoArgsConstructor`, porque Jackson lo necesita). Lo dejo como experimento al final.

---

## 🎯 Parte 6: La llamada que devuelve objetos

Ahora la llamada de verdad. Agregá un método a `CatalogoDePaises` que traiga un `Pais` ya convertido:

```java
public Pais buscarArgentina() {
    String url = properties.getUrl() + "/name/argentina?fields=name,capital,region,population";
    Pais[] resultado = restTemplate.getForObject(url, Pais[].class);
    return resultado[0];   // la API devuelve un array; tomamos el primero
}
```

### Qué cambió respecto a la llamada de texto

```java
restTemplate.getForObject(url, Pais[].class);
//                             ↑
//          "devolveme la respuesta convertida a un array de Pais"
```

En vez de pedir `String.class` (texto crudo), pedís `Pais[].class` (un array de `Pais`). **Spring (con Jackson por debajo) lee el JSON y lo convierte automáticamente en objetos `Pais`.** Vos no parseás nada a mano — la librería lo hace.

Como la API devuelve un **array** (aunque tenga un solo país), pedís `Pais[]` y tomás el primer elemento con `resultado[0]`.

> **El `?fields=...` en la URL** le pide a la API que devuelva solo esos campos (name, capital, region, population) en vez de todos. Es para traer menos datos. La API lo soporta como parámetro.

---

## 🔄 Parte 7: Cómo se convierte el JSON en objetos

El paso "mágico" es `getForObject(url, Pais[].class)`. Desarmemos qué pasa:

```
1. RestTemplate hace un GET HTTP a la URL.
        ↓
2. La API responde con un JSON (texto): [{"name":{...},"capital":[...],...}]
        ↓
3. RestTemplate ve que vos pediste Pais[].class.
        ↓
4. Le pasa el JSON a Jackson (la librería de JSON) y le dice
   "convertí esto en un array de Pais".
        ↓
5. Jackson, por cada objeto del JSON:
   - new Pais()
   - Lee "name" → ve que el campo Java es NombrePais → new NombrePais(), lo llena
   - Lee "capital" → es un array → lo mete en la List<String>
   - Lee "region" → setRegion(...)
   - Lee "population" → setPoblacion(...)
   - Ignora los campos que no están en tu clase (por @JsonIgnoreProperties)
        ↓
6. Devuelve el Pais[] lleno.
```

**Jackson usa los setters de tus clases** para llenar los objetos (igual que Spring usaba los setters de `PaisesProperties` para la config). Por eso las clases necesitan setters.

> **Conexión:** ¿ves el paralelo con la Etapa 5? Allá, Spring leía el archivo YAML y llenaba un objeto con setters. Acá, Jackson lee el JSON de la API y llena objetos con setters. **Es el mismo concepto** —texto estructurado → objeto Java vía setters— aplicado a fuentes distintas (archivo vs HTTP).

---

## 🏃 Parte 8: Disparar la llamada al arrancar (`CommandLineRunner`)

Necesitás algo que **ejecute** la llamada para ver el resultado. Hasta ahora, tus beans solo se creaban (constructor) pero no hacían trabajo. Spring tiene una herramienta para "ejecutar algo después de arrancar": `CommandLineRunner`.

Creá una clase `Arranque`:

```java
package ar.edu.utn.ba.paises;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Arranque implements CommandLineRunner {

    private final CatalogoDePaises catalogo;

    public Arranque(CatalogoDePaises catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> Llamando a la API...");
        Pais argentina = catalogo.buscarArgentina();
        System.out.println(">>> País traído: " + argentina.getNombre().getComun());
        System.out.println(">>> Capital: " + argentina.getCapitales().get(0));
        System.out.println(">>> Región: " + argentina.getRegion());
        System.out.println(">>> Población: " + argentina.getPoblacion());
    }
}
```

### Qué es `CommandLineRunner`

Es una **interfaz de Spring** con un solo método: `run(...)`. Cualquier bean que la implemente, Spring **ejecuta su método `run()` automáticamente después de arrancar**, una vez que todos los beans están listos.

Es el lugar correcto para "hacer algo al iniciar la app". Por eso ponemos la llamada acá, y no en un constructor (recordá que la lógica no va en constructores).

> `Arranque` es un `@Component` (bean), recibe el `CatalogoDePaises` inyectado (como ya sabés), e implementa `CommandLineRunner` para que su `run()` se ejecute al arrancar.

### Correr y ver datos reales

**Corré la app** (con internet). En el log vas a ver:

```
>>> Llamando a la API...
>>> País traído: Argentina
>>> Capital: Buenos Aires
>>> Región: Americas
>>> Población: 45376763
```

**¡Datos reales, traídos de internet, por tu código!** Argentina, su capital, su región, su población — todo vino de la API `restcountries.com`, convertido en un objeto `Pais` que tu código pudo usar.

> Como la app tiene el servidor web, va a quedar viva después de imprimir esto. Detenela con el botón rojo.

---

## 🧪 Parte 9: Experimentos

### Experimento 1: Mirá el texto crudo

Agregá al `run()` de `Arranque` una llamada al método de texto:

```java
System.out.println(">>> JSON crudo: " + catalogo.traerArgentinaComoTexto());
```

Corré. Vas a ver el JSON gigante que devuelve la API (con TODOS los campos, no solo los que pediste). Esto te muestra **qué recibe Jackson antes de convertirlo** en tu objeto `Pais`. Compará: el JSON tiene decenas de campos; tu `Pais` solo guarda cuatro (gracias a `@JsonIgnoreProperties`).

### Experimento 2: Buscá otro país

Generalizá el método. Cambiá `buscarArgentina()` por uno que reciba el nombre:

```java
public Pais buscarPorNombre(String nombre) {
    String url = properties.getUrl() + "/name/" + nombre + "?fields=name,capital,region,population";
    Pais[] resultado = restTemplate.getForObject(url, Pais[].class);
    return resultado[0];
}
```

Y en `Arranque`, probá con varios:

```java
Pais brasil = catalogo.buscarPorNombre("brasil");
System.out.println(">>> " + brasil.getNombre().getComun() + " - " + brasil.getCapitales().get(0));

Pais japon = catalogo.buscarPorNombre("japan");
System.out.println(">>> " + japon.getNombre().getComun() + " - " + japon.getCapitales().get(0));
```

Corré. Vas a ver datos de Brasil y Japón. **Tu código ahora consulta cualquier país de la API.**

> **Madriguera marcada (no la abras ahora):** concatenar el nombre directo en la URL (`"/name/" + nombre`) funciona para nombres simples, pero falla con espacios o tildes. Hay una forma correcta de construir URLs (`UriComponentsBuilder`) que maneja eso. Lo dejo anotado para cuando lo necesites — por ahora, con nombres simples, esto anda.

### Experimento 3: Reemplazá los DTOs con Lombok

Si tenés Lombok, reemplazá los getters/setters de `Pais` y `NombrePais`:

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Pais {
    @JsonProperty("name")
    private NombrePais nombre;
    @JsonProperty("capital")
    private List<String> capitales;
    @JsonProperty("region")
    private String region;
    @JsonProperty("population")
    private Long poblacion;
}
```

`@Data` genera getters/setters; `@NoArgsConstructor` genera el constructor vacío que **Jackson necesita** para hacer `new Pais()`. Corré: funciona idéntico, con mucho menos código.

### Experimento 4: Qué pasa sin internet

Desconectá tu internet (o pará el wifi) y corré la app. Vas a ver una excepción (`ResourceAccessException` o similar) cuando intente la llamada. **Esto te muestra que la llamada HTTP depende de la red** — si la API no está accesible, falla. (Volvé a conectar después.)

> Manejar esos errores correctamente (qué hacer si la API falla) es un tema en sí mismo. Por ahora, solo observá que la dependencia de red existe.

---

## ✅ Criterios de "Etapa 6 completa"

- [ ] Agregaste `spring-boot-starter-web` al `pom.xml` y recargaste Maven.
- [ ] Corriste la app y observaste que ahora **queda viva** (Tomcat en :8080), a diferencia de la Etapa 1.
- [ ] Creaste el bean `RestTemplate` con `@Bean` en `AppConfig`.
- [ ] Creaste los DTOs `Pais` y `NombrePais` con annotations de Jackson.
- [ ] Hiciste la llamada con `getForObject(url, Pais[].class)`.
- [ ] Creaste `Arranque` con `CommandLineRunner` para disparar la llamada.
- [ ] Corriste con internet y viste **datos reales** de Argentina en el log.
- [ ] Hiciste el Experimento 2 (buscar otros países).
- [ ] Entendés cómo el JSON se convierte en objetos (Jackson + setters).

---

## ✅ Checkpoint

1. ¿Qué dependencia agregaste y qué trajo consigo?
2. ¿Por qué ahora la app queda viva en vez de terminar (como en la Etapa 1)?
3. ¿Por qué `RestTemplate` se declara con `@Bean` y no con `@Component`?
4. ¿Qué hace `getForObject(url, Pais[].class)`?
5. ¿Quién convierte el JSON en objetos `Pais`? ¿Cómo (qué usa de tus clases)?
6. ¿Qué hace `@JsonProperty("common")`? ¿Y `@JsonIgnoreProperties(ignoreUnknown = true)`?
7. ¿Por qué `poblacion` es `Long` y no `long`?
8. ¿Qué es `CommandLineRunner` y para qué se usa?
9. ¿De dónde sale la URL base de la API? (pista: Etapa 5)

---

## 🎯 Una reflexión antes de seguir

Mirá todo lo que se juntó en esta etapa:

- El **bean con `@Bean`** (Etapa 4) → el `RestTemplate`.
- La **config externa** (Etapa 5) → la URL de la API.
- La **inyección** (Etapa 3) → el `RestTemplate` y las `properties` entrando al catálogo.
- Los **beans** (Etapa 2) → todo el andamiaje.

Ninguna pieza es nueva en concepto — todas las construiste antes. Lo nuevo fue **juntarlas para hacer algo real**: salir a internet y traer datos. Eso es lo que hace una app de verdad.

Y fijate el cierre del círculo de la Etapa 1: agregaste el servidor web y viste a la app **quedar viva**, confirmando con tus ojos lo que era una frase abstracta hace cinco etapas.

Solo falta una cosa para cerrar el panorama de Spring: **¿cómo se testea todo esto?** En el Proyecto 0 aprendiste a testear con JUnit y AssertJ. En la **Etapa 7** vas a ver cómo se testea una app Spring, con el contexto del framework levantado.

---

## ▶️ Próximo paso

Cuando termines, seguí con la **Etapa 7** (archivo `proyecto1-etapa7-tests.md`). Ahí vas a:
- Testear tu app con el contexto de Spring levantado (`@SpringBootTest`).
- Reusar AssertJ del Proyecto 0.
- Entender la diferencia entre testear con Spring y sin Spring.

Si algo de esta etapa no te cerró, anotá la duda para cuando vuelvas online.
