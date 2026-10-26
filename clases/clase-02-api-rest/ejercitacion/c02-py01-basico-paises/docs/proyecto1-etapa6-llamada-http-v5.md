# 🌱 Proyecto 1 — Etapa 6: Llamada HTTP (`RestTemplate`) — API v5 con token

> **Objetivo:** hacer que tu app **salga a internet, se autentique con un token, llame a una API real y traiga datos**. Vas a usar todo lo aprendido: un bean con `@Bean` (el cliente HTTP), la config externa (`@ConfigurationProperties`) para la URL y el token, y la inyección.
>
> **El momento clave:** vas a ver datos reales —países de una API en internet— aparecer en tu consola, traídos y autenticados por tu propio código Spring.
>
> **Pre-requisito:** Etapas 1-5 completas. Necesitás internet y un token de restcountries.com (o la demo key `rc_live_demo`).
>
> **Tiempo estimado:** 60-75 minutos.

---

## ⚠️ Nota importante sobre esta etapa

La API `restcountries.com` **cambió** a mediados de 2026. Las versiones viejas (v1 a v4) eran abiertas, sin autenticación. La versión actual (**v5**) **requiere un token de API** y devuelve los datos con una estructura distinta (más anidada). Este material está adaptado a esa versión nueva.

Si encontrás material viejo (tutoriales, ejemplos) que usan `https://restcountries.com/v3.1` sin token, **ya no funciona**. Lo de acá es lo actual.

---

## 🧭 Mapa de esta etapa

1. Qué vamos a hacer.
2. Conseguir un token (o usar la demo key).
3. Agregar la dependencia web (y ver cómo cambia el arranque).
4. Poner URL y token en la config.
5. Crear el cliente HTTP (`RestTemplate`) con `@Bean`.
6. La primera llamada (texto crudo, con autenticación).
7. Entender la estructura de la respuesta (el monstruo anidado).
8. Crear los DTOs (decisión de diseño: mapear poco).
9. La llamada que devuelve objetos.
10. Disparar la llamada al arrancar (`CommandLineRunner`).
11. Experimentos.
12. Checkpoint.

---

## 🎯 Parte 1: Qué vamos a hacer

Tu app va a consultar la API de países `restcountries.com` (versión v5) y traer datos reales. Por ejemplo, pidiendo `https://api.restcountries.com/countries/v5?q=argentina` con tu token, te devuelve información de Argentina en JSON.

Para eso necesitás:
1. Un **token** de la API (la v5 lo exige).
2. Un **cliente HTTP** que sepa mandar el token y hacer el pedido (`RestTemplate`).
3. La **URL** y el **token** en tu config (Etapa 5).
4. Clases para **guardar los datos** que vuelven (DTOs).

---

## 🔑 Parte 2: Conseguir un token (o usar la demo key)

La v5 requiere autenticación. Tenés dos opciones:

### Opción A: La demo key (para empezar, sin cuenta)

Existe una key de demostración: **`rc_live_demo`**. Funciona sin crear cuenta, sirve para verificar que tu código conecta bien y que la API responde. **Pero** devuelve un aviso de demostración en vez de datos reales en algunos casos, así que no siempre vas a ver Argentina de verdad. Es ideal para probar la mecánica.

### Opción B: Tu token real (para datos de verdad)

Te registrás en restcountries.com (tier gratuito, sin tarjeta) y obtenés un token tipo `rc_live_...`. Con ese token sí ves los datos reales.

> **Regla de seguridad (en serio):** el token es una **credencial privada**. Nunca lo pegues en un chat, en un repo público, ni lo hardcodees en el código. Si alguna vez se te filtra, **revocalo y generá uno nuevo** desde la página de API Keys. En este material, el token va a vivir en `application.yml` y en los ejemplos vas a ver `TU_TOKEN` como placeholder — reemplazalo por el tuyo en tu archivo local.

**Para este material:** empezá con `rc_live_demo` para probar la mecánica, y cuando quieras ver datos reales, usá tu token. El código es idéntico — solo cambia el valor en la config.

---

## 📦 Parte 3: Agregar la dependencia web

Para hacer llamadas HTTP necesitás el **starter web**. Recordá de la Etapa 0 que creamos el proyecto sin dependencias a propósito. Ahora sumamos la primera.

Abrí el `pom.xml` y agregá dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Sin `<version>`** — el parent la define (Etapa 0). Después de agregarla, **recargá Maven** (notificación "Load Maven Changes" o el ícono de Maven con la flechita).

### Qué trae el starter web

Incluye el cliente HTTP (`RestTemplate`), un servidor web embebido (Tomcat), la librería para JSON (Jackson), y más. Vamos a usar **el cliente HTTP y Jackson**. Tomcat viene de regalo aunque no lo usemos.

### Corré la app y observá un cambio importante

**Corré la app** y mirá el log:

```
... Tomcat started on port(s): 8080 (http) ...
... Started PaisesApplication in 1.5 seconds ...
```

Y lo clave: **la app NO termina.** Queda corriendo.

¿Te acordás de la Etapa 1? Ahí la app **arrancaba y terminaba sola**, porque no había servidor web. Te dije: *"el servidor web es una pieza opcional que se agrega aparte, y es lo que mantiene la app viva"*. **Acabás de agregar esa pieza.** Tomcat quedó escuchando en el puerto 8080, y eso mantiene la app viva. Ya no termina sola.

> **El círculo de la Etapa 1 se cierra acá:** lo que era una frase abstracta ("el servidor mantiene la app viva") ahora lo ves cumplirse. Para detener la app: botón rojo de stop, o `Ctrl + C`.

> Nuestra app no expone endpoints propios, así que ir a `http://localhost:8080` da 404. Tomcat está vivo pero no tiene nada que responder. No importa — nosotros vamos a **hacer** pedidos a otra API, no a **recibirlos**.

---

## ⚙️ Parte 4: Poner URL y token en la config

El token es una credencial → va en la config, **no hardcodeado** (justo para esto sirve la Etapa 5).

Abrí `application.yml` y poné:

```yaml
paises:
    url: https://api.restcountries.com/countries/v5
    api-key: TU_TOKEN
```

> Reemplazá `TU_TOKEN` por `rc_live_demo` (para probar) o tu token real (para datos reales). **Notá la URL nueva:** `https://api.restcountries.com/countries/v5` — cambió respecto a la v3.1 vieja (ahora hay un subdominio `api.` y la versión es `v5`).

Y agregá el campo `apiKey` a `PaisesProperties`:

```java
@ConfigurationProperties(prefix = "paises")
@Data
public class PaisesProperties {
    private String url;
    private String nombreCatalogo;
    private String apiKey;          // ← nuevo: el token
}
```

(Si no usás Lombok, agregá el getter/setter de `apiKey` a mano.)

> **Conexión con tu pregunta sobre kebab-case:** en el YAML escribís `api-key` (con guion), y en Java es `apiKey` (camelCase). Spring hace la conversión, como ya sabés.

---

## 🛠️ Parte 5: Crear el cliente HTTP con `@Bean`

`RestTemplate` es el objeto de Spring que hace llamadas HTTP. Es una clase **de Spring, no tuya** → usás `@Bean` (lo que aprendiste en la Etapa 4 con `Random`).

En `AppConfig`, agregá:

```java
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

(Si ya no usás el `Random`, podés sacarlo. Lo importante es el `RestTemplate`.)

> ¿Ves por qué la Etapa 4 usó `Random`? Era para que `RestTemplate` no fuera nada nuevo — el mecanismo de `@Bean` es idéntico. Solo cambia que `RestTemplate` hace algo más interesante.

---

## 📞 Parte 6: La primera llamada (texto crudo, autenticada)

Antes de pelear con los objetos, hagamos la llamada más simple: **traer la respuesta como texto crudo**, para ver qué devuelve la API nueva.

Acá aparece el cambio grande por la autenticación. Modificá `CatalogoDePaises`:

```java
package ar.edu.utn.ba.paises;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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
        String url = properties.getUrl() + "?q=argentina";

        // ─────────────────────────────────────────────────────────────
        // ANTES (API v3.1, sin token), la llamada era una sola línea:
        //
        //     return restTemplate.getForObject(url, String.class);
        //
        // getForObject es el atajo cómodo para un GET simple SIN headers.
        // Pero la API v5 exige un header de autenticación (Authorization),
        // y getForObject no permite agregar headers fácilmente.
        // Por eso ahora usamos exchange(...), que sí permite headers.
        // ─────────────────────────────────────────────────────────────

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());   // → "Authorization: Bearer <token>"

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            String.class
        );

        return response.getBody();
    }
}
```

### Qué hace cada pieza nueva

- **`HttpHeaders headers`** → un objeto que junta los headers del pedido.
- **`headers.setBearerAuth(token)`** → atajo que arma el header `Authorization: Bearer <token>` por vos (más limpio que escribirlo a mano).
- **`HttpEntity<Void> request`** → el "envoltorio" del pedido. Un GET no manda cuerpo (body), solo headers; por eso `Void`.
- **`restTemplate.exchange(url, HttpMethod.GET, request, String.class)`** → la versión completa de la llamada: URL, método HTTP, envoltorio con headers, y tipo de respuesta. Devuelve un `ResponseEntity<String>` (la respuesta + metadata como el status code).
- **`response.getBody()`** → de toda la respuesta, saca solo el cuerpo (el JSON como String).

> **La sutileza preservada en el comentario:** `getForObject(url, String.class)` era la forma vieja, cómoda para GETs simples sin headers. La dejé comentada para que recuerdes que existe y cuándo se usa (cuando NO necesitás autenticación). `exchange` es el "hermano completo" que te da control sobre los headers.

**Todavía no la podemos ver** porque nadie llama a este método. Lo disparamos en la Parte 10. Por ahora seguí construyendo.

---

## 🐉 Parte 7: La estructura de la respuesta (el monstruo anidado)

Antes de crear las clases, mirá **qué devuelve la API v5**. Es MUCHO más anidada que la v3.1 vieja. Si le pegás un GET a `?q=argentina`, te devuelve algo así (te muestro solo lo que nos importa, la respuesta real tiene decenas de campos más):

```json
{
    "data": {
        "objects": [
            {
                "names": {
                    "common": "Argentina",
                    "official": "Argentine Republic"
                },
                "capitals": [
                    {
                        "name": "Buenos Aires"
                    }
                ],
                "region": "Americas",
                "subregion": "South America",
                "population": 46735004
            }
        ]
    }
}
```

### Fijate en la estructura

Para llegar al nombre del país, tenés que atravesar **varios niveles**:

```
data → objects → [0] → names → common
 ↑       ↑        ↑      ↑        ↑
wrapper  lista   primer  objeto  el dato
         de      país    anidado
         países
```

**Tres tipos de anidación, todos juntos** (¿te acordás de tu pregunta sobre YAML/JSON?):
- **Objeto dentro de objeto:** `names` es un objeto con `common` y `official`.
- **Lista de objetos:** `capitals` es una lista, donde cada elemento es un objeto con `name` (ya no es una lista de strings como en la v3.1 vieja).
- **Wrapper de nivel superior:** todo viene envuelto en `data` → `objects`.

---

## 📋 Parte 8: Crear los DTOs (decisión de diseño importante)

Acá viene una **decisión de diseño real** que vale la pena entender, porque es lo que harías en un proyecto profesional.

### La respuesta tiene ~80 campos. NO los vamos a mapear todos.

La respuesta real trae traducciones a 25 idiomas, paletas de colores de la bandera, coeficientes económicos por año, husos horarios, fronteras, membresías a organismos... **un montón de cosas que no necesitás.**

**La regla profesional: solo modelás lo que vas a usar.** Lo demás, lo ignorás. No tiene sentido escribir clases para 80 campos si solo te interesan 5.

¿Cómo ignorás el resto sin que rompa? Con **`@JsonIgnoreProperties(ignoreUnknown = true)`** — le dice a Jackson "si el JSON trae campos que mi clase no tiene, ignoralos sin romper". Lo viste mencionado antes; acá es **esencial**, porque sin eso, la conversión fallaría al toparse con los 75 campos que no modelaste.

### Vamos a mapear solo estos campos

- `names.common` y `names.official` (el nombre)
- `region` y `subregion`
- `population`
- `capitals[].name` (la capital)

Para eso necesitás **cinco clases** (por la anidación). Las creamos de adentro hacia afuera.

### Clase `NombrePais` (representa `names`)

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NombrePais {

    private String common;
    private String official;

    public String getCommon() { return common; }
    public void setCommon(String common) { this.common = common; }
    public String getOfficial() { return official; }
    public void setOfficial(String official) { this.official = official; }
}
```

> **Cambio respecto al material viejo:** acá los nombres del JSON (`common`, `official`) **coinciden** con los atributos Java, así que **no necesitás `@JsonProperty`**. En la v3.1 los mapeábamos a español (`comun`, `oficial`) y hacía falta `@JsonProperty`. Para simplificar y como los nombres ya son claros, los dejamos igual que el JSON. (Si quisieras nombres en español, usarías `@JsonProperty("common")` sobre un campo `comun`, como en el material viejo.)

### Clase `Capital` (representa cada objeto de `capitals`)

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Capital {

    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

> **Esto es nuevo respecto al material viejo.** Antes, `capital` era una lista de strings (`["Buenos Aires"]`). Ahora es una lista de **objetos**, cada uno con `name` (y muchos campos más que ignoramos: coordenadas, atributos, etc.). Por eso necesita su propia clase.

### Clase `Pais` (representa cada objeto de `objects`)

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pais {

    private NombrePais names;
    private List<Capital> capitals;
    private String region;
    private String subregion;
    private Long population;

    public NombrePais getNames() { return names; }
    public void setNames(NombrePais names) { this.names = names; }
    public List<Capital> getCapitals() { return capitals; }
    public void setCapitals(List<Capital> capitals) { this.capitals = capitals; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getSubregion() { return subregion; }
    public void setSubregion(String subregion) { this.subregion = subregion; }
    public Long getPopulation() { return population; }
    public void setPopulation(Long population) { this.population = population; }
}
```

> **Por qué `population` es `Long` y no `long`:** si la API no manda la población de algún país, el campo queda `null`. Un `long` primitivo no puede ser `null`; un `Long` wrapper sí. Para datos de fuentes externas, siempre wrappers.

### Clase `Data` (representa el `data` → `objects`)

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data {

    private List<Pais> objects;

    public List<Pais> getObjects() { return objects; }
    public void setObjects(List<Pais> objects) { this.objects = objects; }
}
```

### Clase `RespuestaPaises` (el wrapper de más afuera)

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaPaises {

    private Data data;

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }
}
```

### El mapa de clases (para que veas la estructura)

```
RespuestaPaises          → el JSON completo
    └─ data: Data        → el "data" del JSON
         └─ objects: List<Pais>   → la lista de países
                └─ Pais            → cada país
                     ├─ names: NombrePais       → objeto anidado
                     ├─ capitals: List<Capital> → lista de objetos
                     ├─ region, subregion, population
```

**Cada clase tiene su `@JsonIgnoreProperties(ignoreUnknown = true)`** para tirar los campos que no mapeamos. Sin eso, Jackson explotaría con cualquiera de los 75 campos que ignoramos.

> **Sobre Lombok:** escribí todos los getters/setters a mano para que veas la estructura. Si usás Lombok, reemplazá cada clase por `@Data @NoArgsConstructor` y borrá el boilerplate. `@NoArgsConstructor` es necesario porque Jackson hace `new Pais()` (constructor vacío) antes de llenar con setters. Lo dejo como experimento al final.

---

## 🎯 Parte 9: La llamada que devuelve objetos

Ahora la llamada de verdad. Agregá a `CatalogoDePaises` un método que traiga un `Pais` ya convertido:

```java
public Pais buscarArgentina() {
    String url = properties.getUrl() + "?q=argentina";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(properties.getApiKey());
    HttpEntity<Void> request = new HttpEntity<>(headers);

    // ─────────────────────────────────────────────────────────────
    // Pedimos que la respuesta se convierta en RespuestaPaises
    // (el wrapper de más afuera). Jackson navega toda la anidación
    // y nos arma el árbol de objetos.
    //
    // ANTES (v3.1), la API devolvía DIRECTO un array de países, así que
    // se pedía Pais[].class y se tomaba [0]. Ahora hay dos capas de
    // wrapper (data → objects), por eso pedimos RespuestaPaises y
    // navegamos: getData().getObjects().get(0).
    // ─────────────────────────────────────────────────────────────

    ResponseEntity<RespuestaPaises> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        request,
        RespuestaPaises.class
    );

    return response.getBody().getData().getObjects().get(0);
}
```

### Qué cambió respecto a la llamada de texto

```java
restTemplate.exchange(url, HttpMethod.GET, request, RespuestaPaises.class);
//                                                  ↑
//          "convertí la respuesta en un RespuestaPaises" (no String crudo)
```

En vez de pedir `String.class` (texto), pedís `RespuestaPaises.class`. **Jackson lee el JSON y arma todo el árbol de objetos** automáticamente. Vos no parseás nada a mano.

Y para llegar al país, **navegás la anidación**:

```java
response.getBody()              // RespuestaPaises
        .getData()              // Data
        .getObjects()           // List<Pais>
        .get(0);                // el primer Pais
```

> **El contraste con el material viejo es revelador:** antes era `getForObject(url, Pais[].class)[0]` — una capa. Ahora son dos capas de wrapper más la navegación. **Esto es lo que pasa cuando una API decide envolver sus datos:** tu código tiene que reflejar esa estructura. No es más difícil, solo más profundo.

---

## 🏃 Parte 10: Disparar la llamada al arrancar (`CommandLineRunner`)

Necesitás algo que **ejecute** la llamada. Hasta ahora tus beans solo se creaban (constructor) pero no hacían trabajo. Spring tiene `CommandLineRunner` para "ejecutar algo después de arrancar".

Creá `Arranque`:

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
        System.out.println(">>> País traído: " + argentina.getNames().getCommon());
        System.out.println(">>> Nombre oficial: " + argentina.getNames().getOfficial());
        System.out.println(">>> Capital: " + argentina.getCapitals().get(0).getName());
        System.out.println(">>> Región: " + argentina.getRegion());
        System.out.println(">>> Subregión: " + argentina.getSubregion());
        System.out.println(">>> Población: " + argentina.getPopulation());
    }
}
```

### Qué es `CommandLineRunner`

Es una **interfaz de Spring** con un solo método: `run(...)`. Cualquier bean que la implemente, Spring **ejecuta su `run()` automáticamente después de arrancar**, cuando todos los beans están listos. Es el lugar correcto para "hacer algo al iniciar la app" (y no en un constructor, donde la lógica no va).

### Correr y ver datos reales

**Corré la app** (con internet y con tu token real en la config — la demo key puede no devolver datos reales). En el log:

```
>>> Llamando a la API...
>>> País traído: Argentina
>>> Nombre oficial: Argentine Republic
>>> Capital: Buenos Aires
>>> Región: Americas
>>> Subregión: South America
>>> Población: 46735004
```

**¡Datos reales, autenticados, traídos de internet, por tu código!** Navegaste toda esa anidación monstruosa y sacaste justo lo que querías.

> La app queda viva (por Tomcat). Detenela con el botón rojo.

---

## 🧪 Parte 11: Experimentos

### Experimento 1: Mirá el texto crudo

Agregá al `run()` de `Arranque`:

```java
System.out.println(">>> JSON crudo: " + catalogo.traerArgentinaComoTexto());
```

Corré. Vas a ver el JSON **gigante** que devuelve la API (con los ~80 campos: traducciones, colores de bandera, etc.). Compará: el JSON tiene decenas de campos; tus DTOs solo guardan 6. **Eso es `@JsonIgnoreProperties` en acción** — tira todo lo que no modelaste.

### Experimento 2: Buscá otro país

Generalizá el método. Cambiá `buscarArgentina()` por:

```java
public Pais buscarPorNombre(String nombre) {
    String url = properties.getUrl() + "?q=" + nombre;

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(properties.getApiKey());
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<RespuestaPaises> response = restTemplate.exchange(
        url, HttpMethod.GET, request, RespuestaPaises.class
    );

    return response.getBody().getData().getObjects().get(0);
}
```

Y en `Arranque`, probá varios:

```java
Pais brasil = catalogo.buscarPorNombre("brazil");
System.out.println(">>> " + brasil.getNames().getCommon() + " - " + brasil.getCapitals().get(0).getName());

Pais japon = catalogo.buscarPorNombre("japan");
System.out.println(">>> " + japon.getNames().getCommon() + " - " + japon.getCapitals().get(0).getName());
```

> **Madriguera marcada (no la abras ahora):** concatenar el nombre directo en la URL (`"?q=" + nombre`) funciona para nombres simples, pero falla con espacios o tildes. Hay una forma correcta de construir URLs (`UriComponentsBuilder`) que maneja eso. Lo dejo anotado para cuando lo necesites.

### Experimento 3: Reemplazá los DTOs con Lombok

Si tenés Lombok, reemplazá los getters/setters de las cinco clases (`RespuestaPaises`, `Data`, `Pais`, `NombrePais`, `Capital`):

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class Pais {
    private NombrePais names;
    private List<Capital> capitals;
    private String region;
    private String subregion;
    private Long population;
}
```

`@Data` genera getters/setters; `@NoArgsConstructor` el constructor vacío que Jackson necesita. Corré: funciona idéntico, con muchísimo menos código. (Con cinco clases anidadas, Lombok te ahorra un montón de boilerplate.)

### Experimento 4: Token inválido

Cambiá el `api-key` en el YAML por algo inválido (ej: `rc_live_xxxxx`). Corré. Vas a ver un error HTTP (401 Unauthorized o similar) cuando la API rechace el token. **Esto te muestra que la autenticación es real** — sin token válido, la API no te da nada. Volvé a poner tu token bueno.

### Experimento 5: Qué pasa sin internet

Desconectá tu red y corré. Vas a ver una excepción (`ResourceAccessException` o similar) al intentar la llamada. La llamada HTTP depende de la red. Volvé a conectar.

---

## ✅ Criterios de "Etapa 6 completa"

- [ ] Conseguiste un token (demo key o real) y lo pusiste en `application.yml`.
- [ ] Agregaste `spring-boot-starter-web` y recargaste Maven.
- [ ] Observaste que la app ahora **queda viva** (Tomcat), a diferencia de la Etapa 1.
- [ ] Agregaste `apiKey` a `PaisesProperties`.
- [ ] Creaste el bean `RestTemplate` con `@Bean`.
- [ ] Implementaste la llamada con `exchange` + `Authorization: Bearer` (no `getForObject`).
- [ ] Creaste los cinco DTOs adaptados a la estructura v5 (`RespuestaPaises`, `Data`, `Pais`, `NombrePais`, `Capital`).
- [ ] Hiciste la llamada que devuelve objetos y navegaste la anidación.
- [ ] Creaste `Arranque` con `CommandLineRunner`.
- [ ] Corriste con tu token y viste datos reales de Argentina.
- [ ] Entendés por qué solo mapeamos algunos campos y no todos.

---

## ✅ Checkpoint

1. ¿Por qué ahora hace falta un token? ¿Qué cambió en la API?
2. ¿Por qué usamos `exchange` en vez de `getForObject`? ¿Qué tenía de limitado `getForObject`?
3. ¿Qué hace `headers.setBearerAuth(token)`?
4. ¿Por qué el token va en `application.yml` y no en el código?
5. ¿Por qué necesitamos cinco clases para mapear la respuesta? ¿Qué representa cada una?
6. ¿Por qué NO mapeamos los 80 campos de la respuesta? ¿Qué annotation nos deja ignorar el resto?
7. ¿Cómo llegás del `RespuestaPaises` al primer país? (la cadena de navegación)
8. ¿Por qué `capitals` necesita su propia clase `Capital`, si antes era una lista de strings?
9. ¿Qué es `CommandLineRunner`?

---

## 🎯 Una reflexión antes de seguir

Mirá todo lo que se juntó acá:

- El **bean con `@Bean`** (Etapa 4) → el `RestTemplate`.
- La **config externa** (Etapa 5) → la URL y el token.
- La **inyección** (Etapa 3) → el `RestTemplate` y las `properties` entrando al catálogo.
- Los **DTOs anidados** → reflejan la estructura del JSON, como en tu pregunta sobre anidación.

Ninguna pieza es nueva en concepto — todas las construiste antes. Lo nuevo fue **juntarlas para hacer algo real y autenticado**: salir a internet, mandar un token, traer datos, y navegar una respuesta compleja.

Y aprendiste algo que los tutoriales suelen esconder: **las APIs del mundo real cambian, exigen autenticación, y devuelven estructuras complicadas.** No siempre es el `getForObject` de una línea de los ejemplos de juguete. Cuando una API te tira un monstruo anidado con token, ahora sabés cómo domarlo: headers con `exchange`, DTOs que reflejan la estructura, y `@JsonIgnoreProperties` para quedarte solo con lo que te importa.

Solo falta cerrar el panorama: **¿cómo se testea todo esto?** En la **Etapa 7** vas a ver cómo testear una app Spring, reusando JUnit y AssertJ del Proyecto 0.

---

## ▶️ Próximo paso

Cuando termines, seguí con la **Etapa 7** (`proyecto1-etapa7-tests.md`), que también está actualizada para la API v5. Ahí vas a:
- Testear tu app con el contexto de Spring levantado (`@SpringBootTest`).
- Reusar AssertJ del Proyecto 0.
- Ajustar los asserts a la nueva estructura de datos.

Si algo de esta etapa no te cerró, anotá la duda para cuando vuelvas online.
