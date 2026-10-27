# 🌱 Proyecto 1 — Etapa 6: Llamada HTTP (`RestTemplate`) con token

> **Objetivo:** hacer que tu app **salga a internet, se autentique con un token, llame a una API real y traiga datos**. Vas a juntar todo lo aprendido: un bean con `@Bean` (el cliente HTTP), la config externa (`@ConfigurationProperties`) para la URL y el token, y la inyección.
>
> **El momento clave:** vas a ver datos reales —países de una API en internet— aparecer en tu consola, traídos y autenticados por tu propio código Spring.
>
> **Pre-requisito:** Etapas 1-5 completas (la 5b es opcional). Necesitás internet y un token de restcountries.com (o la demo key `rc_live_demo`).
>
> **Tiempo estimado:** 60-75 minutos.
>
> **Versiones:** Java 21 · Spring Boot 4.0.x · Maven 3.9 (el del wrapper `mvnw`) · API REST Countries v5.

---

## ⚠️ Nota importante sobre esta etapa

La API `restcountries.com` **cambió** a mediados de 2026:
- Las versiones viejas (v1 a v4; la más usada era la **v3.1**) eran abiertas: sin cuenta y sin token. La v3.1 es la que se usó cuando se dictó esta clase.
- La versión actual (**v5**) **exige un token** y devuelve los datos con una estructura más anidada.

**Esta etapa está escrita para la v5.** Si encontrás material (tutoriales, ejemplos) que usa `https://restcountries.com/v3.1` sin token, **ya no funciona**.

Igual, la forma "sin token" sigue siendo útil: es la que vas a usar con cualquier API pública que no pida key. Por eso, en los puntos donde cambia algo, vas a encontrar un cuadro así:

> 🔓 **Sin token (API pública).** Así se hacía con la v3.1, y así lo hacés con cualquier API que no pida key: …

El camino principal se lee de corrido sin entrar a esos cuadros.

---

## 🧭 Mapa de esta etapa

1. Qué vamos a hacer.
2. Conseguir un token (o usar la demo key).
3. Agregar la dependencia web (y ver cómo cambia el arranque).
4. Poner la URL y el token en la config.
5. Crear el cliente HTTP (`RestTemplate`) con `@Bean`.
6. La primera llamada (texto crudo, con autenticación).
7. Entender la estructura de la respuesta (el monstruo anidado).
8. Crear los DTOs (decisión de diseño: mapear poco).
9. La llamada que devuelve objetos.
10. Cómo se convierte el JSON en objetos.
11. Disparar la llamada al arrancar (`CommandLineRunner`).
12. Experimentos.
13. Checkpoint.

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

La v5 requiere autenticación. Tenés dos opciones.

### Opción A: la demo key (para empezar, sin cuenta)

Existe una key de demostración: **`rc_live_demo`**. Funciona sin crear cuenta y sirve para verificar que tu código conecta y que la API responde. **Pero** devuelve la respuesta con la forma normal y **datos de muestra** (con un aviso de demostración adentro), así que puede que no veas Argentina de verdad. Es ideal para probar la mecánica.

### Opción B: tu token real (para datos de verdad)

Te registrás en restcountries.com (tier gratuito, sin tarjeta) y obtenés un token tipo `rc_live_...`. Con ese token ves los datos reales.

> **Regla de seguridad (en serio):** el token es una **credencial privada**, como una contraseña. Nunca lo pegues en un chat, en un repo ni en el código. Si alguna vez se te filtra, **revocalo y generá uno nuevo** desde la página de API Keys. Cómo guardarlo en un archivo que **no se sube al repo**: Etapa 6b.

**Para este material:** empezá con `rc_live_demo` para probar la mecánica y, cuando quieras datos reales, usá tu token. El código es idéntico: solo cambia el valor.

> 🔓 **Sin token (API pública).** Esta parte no existe: no hay cuenta ni token. Tampoco hace falta la Etapa 6b (proteger el token).

---

## 📦 Parte 3: Agregar la dependencia web

Para hacer llamadas HTTP necesitás el **starter web** (un *starter* es una dependencia que trae agrupadas todas las librerías para una función). Recordá de la Etapa 0 que creamos el proyecto sin dependencias a propósito. Ahora sumamos la primera.

Abrí el `pom.xml` y agregá dentro de `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

**Sin `<version>`**: el parent la define (Etapa 0). Después de agregarla, **recargá Maven** (notificación "Load Maven Changes" o el ícono de Maven con la flechita).

### Qué trae el starter web

Incluye el cliente HTTP (`RestTemplate`), un servidor web embebido (Tomcat), la librería para JSON (Jackson) y más. Vamos a usar **el cliente HTTP y Jackson**. Tomcat viene de regalo aunque no lo usemos.

### Corré la app y observá un cambio importante

**Corré la app** y mirá el log:

```
... Tomcat started on port 8080 (http) ...
... Started PaisesApplication in 1.5 seconds ...
```

Y lo clave: **la app NO termina.** Queda corriendo.

¿Te acordás de la Etapa 1? Ahí la app **arrancaba y terminaba sola**, porque no había servidor web. Te dije: *"el servidor web es una pieza opcional que se agrega aparte, y es lo que mantiene la app viva"*. **Acabás de agregar esa pieza.** Tomcat quedó escuchando en el puerto 8080, y eso mantiene la app viva.

> **El círculo de la Etapa 1 se cierra acá:** lo que era una frase abstracta ("el servidor mantiene la app viva") ahora lo ves cumplirse. Para detener la app: botón rojo de stop, o `Ctrl + C` en la terminal.

> **Comprobalo con el experimento de la Etapa 1:** si tenés un `System.out.println("...")` después del `run()` en el `main`, el mensaje se imprime igual, **pero la app no termina**. `SpringApplication.run(...)` sigue sin bloquear; lo que mantiene vivo el proceso es Tomcat.

> Nuestra app no expone endpoints propios, así que ir a `http://localhost:8080` da 404. Tomcat está vivo pero no tiene nada que responder. No importa: nosotros vamos a **hacer** pedidos a otra API, no a **recibirlos**.

---

## ⚙️ Parte 4: La URL y el token en la config

El token es configuración, así que va en la config y **no en el código** (para eso sirve la Etapa 5).

### Paso 1: agregar `apiKey` a `PaisesProperties`

```java
@ConfigurationProperties(prefix = "paises")
@Data
public class PaisesProperties {
    private String url;
    private String nombreCatalogo;
    private String apiKey;          // ← nuevo: el token
}
// Resultado esperado: compila; apiKey se va a llenar con lo que haya bajo paises.api-key
```

(Si no usás Lombok, agregá el getter y el setter de `apiKey` a mano. Si en la Etapa 5b la pasaste a `record`, agregá el componente: `record PaisesProperties(String url, String nombreCatalogo, String apiKey)`, y leelo con `apiKey()` donde esta guía dice `getApiKey()`.)

> **Kebab-case, como en la Etapa 5:** en el YAML vas a escribir `api-key` (con guion) y en Java es `apiKey` (camelCase). Spring hace la conversión.

### Paso 2: la URL y el token en el YAML

```yaml
paises:
    url: https://api.restcountries.com/countries/v5
    nombre-catalogo: Catálogo Mundial
    api-key: rc_live_demo               # o tu token real (leé el aviso de abajo)
# Resultado esperado: properties.getApiKey() → "rc_live_demo"
```

**Notá la URL nueva:** `https://api.restcountries.com/countries/v5`. Cambió respecto de la v3.1 vieja: ahora hay un subdominio `api.` y la versión es `v5`.

> 🔓 **Sin token (API pública).** Solo va la URL, y `PaisesProperties` no necesita `apiKey`:
> ```yaml
> paises:
>     url: https://restcountries.com/v3.1        # la URL de la v3.1, cuando era pública
>     nombre-catalogo: Catálogo Mundial
> ```
> No hace falta la Etapa 6b.

### ⚠️ Antes de hacer commit: el token real no va en el repo

> 🔐 **Con la demo key, este YAML está bien:** `rc_live_demo` es pública.
>
> **Con tu token real, esto es solo para probar.** El `application.yml` se sube al repo: si hacés commit con tu token adentro, queda a la vista de cualquiera, y además en el historial de git aunque después lo borres. Antes de commitear:
> - volvé a poner `rc_live_demo` en el YAML, o
> - hacé la **Etapa 6b** (`proyecto1-etapa6b-proteger-el-token.md`): el token pasa a un archivo `.env` que git no sube, y el YAML queda con una referencia. Es corta y no cambia nada del resto de esta etapa.
>
> Si alguna vez se te escapa en un commit: **revocá el token** en restcountries.com y generá uno nuevo.

---

## 🛠️ Parte 5: Crear el cliente HTTP con `@Bean`

`RestTemplate` es el objeto de Spring que hace llamadas HTTP. Es una clase **de Spring, no tuya** → usás `@Bean` (lo que aprendiste en la Etapa 4 con `Random`).

En `AppConfig`, agregá:

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
// Resultado esperado al correr: ">>> Se creó el RestTemplate" en el log, una sola vez
```

(Si ya no usás el `Random`, podés sacarlo. Lo importante es el `RestTemplate`.)

> ¿Ves por qué la Etapa 4 usó `Random`? Era para que `RestTemplate` no fuera nada nuevo: el mecanismo de `@Bean` es idéntico. Solo cambia que `RestTemplate` hace algo más interesante.

---

## 📞 Parte 6: La primera llamada (texto crudo, autenticada)

Antes de pelear con los objetos, hagamos la llamada más simple: **traer la respuesta como texto crudo**, para ver qué devuelve la API.

Modificá `CatalogoDePaises`:

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
        String key = properties.getApiKey();                    // chequeo: qué token se cargó, sin mostrarlo entero
        System.out.println(">>> API key cargada: " +
                (key == null || key.isBlank()
                        ? "NO (vacía)"
                        : key.substring(0, Math.min(8, key.length())) + "... (" + key.length() + " caracteres)"));
    }

    public String traerArgentinaComoTexto() {
        String url = properties.getUrl() + "?q=argentina";

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
// Resultado esperado al arrancar: >>> API key cargada: rc_live_... (NN caracteres)
// Resultado esperado de traerArgentinaComoTexto() (se llama en el Experimento 1): el JSON de Argentina como un String gigante
```

> **El chequeo del constructor** muestra los primeros 8 caracteres del token y su largo, **nunca el token entero** (los logs se copian y se comparten). Como todos los tokens empiezan con `rc_live_`, el largo es lo que te dice cuál cargó: la demo key tiene 12 caracteres; tu token real, bastantes más.

> **Esta versión de `CatalogoDePaises` saca el `Random` y los métodos de las Etapas 4 y 5** (`cantidadDePaises()`, `numeroDePaisAlAzar()`), para enfocarse en la llamada HTTP. Si algo los sigue usando (por ejemplo, el `ServicioDePaises` del Experimento 3 de la Etapa 4), dejalos: no molestan.

### Qué hace cada pieza nueva

- **`HttpHeaders headers`** → un objeto que junta los *headers* del pedido (los datos extra que viajan con un pedido HTTP, además de la URL).
- **`headers.setBearerAuth(token)`** → atajo que arma el header `Authorization: Bearer <token>` por vos. "Bearer" es la forma estándar de mandar un token.
- **`HttpEntity<Void> request`** → el "envoltorio" del pedido. Un GET no manda cuerpo (body), solo headers; por eso `Void`.
- **`restTemplate.exchange(url, HttpMethod.GET, request, String.class)`** → la versión completa de la llamada: URL, método HTTP, envoltorio con headers y tipo de respuesta. Devuelve un `ResponseEntity<String>` (la respuesta más metadata, como el código de estado).
- **`response.getBody()`** → de toda la respuesta, saca solo el cuerpo (el JSON como String).

> 🔍 **¿Querés entender qué es cada pieza y qué pasa adentro del `exchange`?** Qué es un pedido y una respuesta HTTP, qué representan `HttpEntity`, `ResponseEntity` y `RestTemplate`, en qué momento sale el pedido y cuánto espera tu código (comparado con `fetch` y `await` de JavaScript): **Etapa 6c** (`proyecto1-etapa6c-la-llamada-por-dentro.md`). Es opcional; podés seguir y volver después.

> 🔓 **Sin token (API pública).** Sin header que mandar, la llamada es una sola línea:
> ```java
> public String traerArgentinaComoTexto() {
>     String url = properties.getUrl() + "/name/argentina";   // forma de la URL en la v3.1
>     return restTemplate.getForObject(url, String.class);
> }
> ```
> `getForObject` es el atajo cómodo para un GET simple, **sin headers**. Con la v5 no alcanza, porque no permite agregar el header `Authorization` fácilmente; por eso acá se usa `exchange`, su "hermano completo".
>
> (La v5 también acepta el token en la URL, `?api-key=...`, y con eso `getForObject` andaría. Pero su propia documentación lo desaconseja: las URLs quedan guardadas en logs e historiales, y el token con ellas.)

**Todavía no la podés ver**, porque nadie llama a este método: lo vas a llamar en el Experimento 1 (Parte 12). En la Parte 11 disparamos la llamada que devuelve objetos. Por ahora seguí construyendo.

---

## 🐉 Parte 7: La estructura de la respuesta (el monstruo anidado)

Antes de crear las clases, mirá **qué devuelve la API v5**. Si le pegás un GET a `?q=argentina`, te devuelve algo así (solo lo que nos importa; la respuesta real tiene decenas de campos más):

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

**Tres tipos de anidación, todos juntos:**
- **Objeto dentro de objeto:** `names` es un objeto con `common` y `official`.
- **Lista de objetos:** `capitals` es una lista, donde cada elemento es un objeto con `name`.
- **Wrapper de nivel superior:** todo viene envuelto en `data` → `objects`. Un *wrapper* es un objeto que solo sirve para envolver a otro.

> 🔓 **Sin token (API pública).** La v3.1 devolvía algo más plano: **un array directo** de países, sin wrapper, y con `capital` como lista de strings:
> ```json
> [
>     {
>         "name": { "common": "Argentina", "official": "Argentine Republic" },
>         "capital": ["Buenos Aires"],
>         "region": "Americas",
>         "population": 45376763
>     }
> ]
> ```

**La forma de la respuesta la decide cada API:** lo primero, siempre, es mirarla (en el navegador o con el texto crudo de la Parte 6).

---

## 📋 Parte 8: Crear los DTOs (decisión de diseño importante)

Un **DTO** (*Data Transfer Object*) es una clase que solo sirve para transportar datos: acá, para recibir el JSON de la API. Viene una **decisión de diseño real**, de las que tomarías en un proyecto profesional.

### La respuesta tiene ~80 campos. NO los vamos a mapear todos.

La respuesta real trae traducciones a 25 idiomas, colores de la bandera, coeficientes económicos, husos horarios, fronteras… **un montón de cosas que no necesitás.**

**La regla: solo modelás lo que vas a usar.** No tiene sentido escribir clases para 80 campos si solo te interesan 5.

¿Cómo ignorás el resto? Con **`@JsonIgnoreProperties(ignoreUnknown = true)`** en cada clase. Le dice a **Jackson** (la librería que convierte JSON en objetos y viceversa, incluida en el starter web): "si el JSON trae campos que mi clase no tiene, ignoralos".

Con Jackson 3 (el que trae Spring Boot 4), ignorar campos desconocidos ya es el comportamiento por defecto. Pero con otra configuración (Jackson 2 usado directo, o un conversor configurado para fallar ante campos desconocidos) la conversión explota con `UnrecognizedPropertyException`. La anotación lo garantiza en la clase misma, sea cual sea la configuración, y deja escrito que ignorar el resto es a propósito.

### Vamos a mapear solo estos campos

- `names.common` y `names.official` (el nombre)
- `region` y `subregion`
- `population`
- `capitals[].name` (la capital)

Por la anidación necesitás **cinco clases**. Las creamos de adentro hacia afuera.

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

Los atributos se llaman **igual que en el JSON** (`common`, `official`), así que Jackson los empareja solo.

> **Si querés nombres en español:** se usa `@JsonProperty`, que conecta un atributo Java con un campo del JSON de otro nombre:
> ```java
> @JsonProperty("common")      // el campo del JSON se llama "common"...
> private String comun;        // ...y en Java lo llamás "comun"
> ```
> Acá no lo usamos porque los nombres del JSON ya son claros.

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

Cada capital es un **objeto** con `name` (y muchos campos más que ignoramos: coordenadas, atributos…). Por eso necesita su propia clase.

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

> **Por qué `population` es `Long` y no `long`:** un `long` primitivo no puede ser `null`; un `Long` (*wrapper*: la versión objeto de un primitivo) sí. Si la API manda `"population": null`, con `long` Jackson 3 tira error; y si el campo directamente no viene, un `long` queda en `0`, y no distinguís "no vino" de "cero". Con `Long` queda `null` en los dos casos. Para datos de fuentes externas, siempre wrappers. (En el Proyecto 0 usabas `long` porque los datos los escribías vos.)

### Clase `DatosPaises` (representa el `data`)

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatosPaises {

    private List<Pais> objects;

    public List<Pais> getObjects() { return objects; }
    public void setObjects(List<Pais> objects) { this.objects = objects; }
}
```

> **¿Por qué no se llama `Data`, como el campo del JSON?** Porque choca con Lombok. En `Data.java` no podrías escribir `import lombok.Data` (error de compilación: la clase y el import se llaman igual). Y en `RespuestaPaises`, al agregar `import lombok.Data`, el nombre `Data` pasaría a significar la anotación de Lombok y no tu clase, y el código deja de compilar. El nombre de la clase Java no tiene por qué coincidir con el JSON; lo que tiene que coincidir es el **nombre del atributo** (`data`, en la clase de abajo).

### Clase `RespuestaPaises` (el wrapper de más afuera)

```java
package ar.edu.utn.ba.paises;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaPaises {

    private DatosPaises data;     // ← el atributo se llama "data", como en el JSON

    public DatosPaises getData() { return data; }
    public void setData(DatosPaises data) { this.data = data; }
}
// Resultado esperado (las cinco clases): compilan; todavía nadie las usa
```

### El mapa de clases

```
RespuestaPaises                   → el JSON completo
    └─ data: DatosPaises          → el "data" del JSON
         └─ objects: List<Pais>   → la lista de países
                └─ Pais           → cada país
                     ├─ names: NombrePais        → objeto anidado
                     ├─ capitals: List<Capital>  → lista de objetos
                     └─ region, subregion, population
```

> **Sobre Lombok:** escribí los getters y setters a mano para que veas la estructura. Con Lombok, cada clase se reduce a `@Data @NoArgsConstructor` más los atributos (Experimento 3). Jackson crea cada objeto con el constructor vacío antes de llenarlo. `@Data` ya lo genera, pero conviene poner `@NoArgsConstructor` explícito: si después agregás `@AllArgsConstructor` o `@Builder`, el constructor vacío desaparece y Jackson falla.

> 🔓 **Sin token (API pública).** Con la respuesta plana de la v3.1 alcanzaban **dos clases**: `NombrePais` y `Pais`, sin wrappers. Y `capital` era una lista de strings, sin clase propia:
> ```java
> @JsonIgnoreProperties(ignoreUnknown = true)
> public class Pais {
>     @JsonProperty("name")    private NombrePais nombre;      // nombres en español con @JsonProperty
>     @JsonProperty("capital") private List<String> capitales; // lista de strings, no de objetos
>     private String region;
>     @JsonProperty("population") private Long poblacion;
>     // getters y setters
> }
> ```
> La cantidad de clases sale de la forma del JSON: **una clase por cada objeto anidado** que quieras usar.

---

## 🎯 Parte 9: La llamada que devuelve objetos

Ahora la llamada de verdad. Agregá a `CatalogoDePaises` un método que traiga un `Pais` ya convertido:

```java
public Pais buscarArgentina() {
    String url = properties.getUrl() + "?q=argentina";

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(properties.getApiKey());
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<RespuestaPaises> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        request,
        RespuestaPaises.class          // ← "convertí la respuesta en un RespuestaPaises"
    );

    return response.getBody().getData().getObjects().get(0);
}
// Resultado esperado (cuando se llame, en la Parte 11): el Pais de Argentina, ya lleno
```

### Qué cambió respecto de la llamada de texto

En vez de pedir `String.class` (texto), pedís `RespuestaPaises.class`. **Jackson lee el JSON y arma todo el árbol de objetos** automáticamente. Vos no parseás nada a mano.

Y para llegar al país, **navegás la anidación**:

```java
response.getBody()              // RespuestaPaises
        .getData()              // DatosPaises
        .getObjects()           // List<Pais>
        .get(0);                // el primer Pais
```

> 🔓 **Sin token (API pública).** Con la v3.1, una sola línea y una sola capa:
> ```java
> String url = properties.getUrl() + "/name/argentina?fields=name,capital,region,population";
> Pais[] resultado = restTemplate.getForObject(url, Pais[].class);   // la API devolvía un array
> return resultado[0];
> ```
> El `?fields=...` le pedía a la API solo esos campos, para traer menos datos.

**Lo que cambia entre una API y otra es la forma de la respuesta, no la mecánica.** Acá la API envuelve los datos, así que tu código refleja esa estructura. No es más difícil, solo más profundo.

> ⚠️ **`get(0)` supone que vino al menos un país.** Si la búsqueda no encuentra nada, `objects` viene vacía y `get(0)` tira `IndexOutOfBoundsException`. Para esta etapa alcanza; en un programa real se chequea antes.

> 🧰 **Cómo va quedando `CatalogoDePaises`: conservá los métodos.** Cada método hace una llamada distinta, y las etapas siguientes los vuelven a usar:
>
> | Método | Qué devuelve | Dónde se crea | Quién lo usa después |
> |---|---|---|---|
> | `traerArgentinaComoTexto()` | el JSON crudo, como `String` | Parte 6 | Experimento 1 · Etapa 6c |
> | `buscarArgentina()` | un `Pais` (Argentina) | Parte 9 | `Arranque` (Parte 11) · Etapa 6c (la explica) |
> | `buscarPorNombre(String nombre)` | un `Pais` (el que pidas) | Experimento 2 | Experimento 2 · Etapa 7 |
>
> Si querés, podés sacar el código repetido (armar los headers y el sobre) a un método privado que usen los tres: es una buena práctica, y no cambia nada de lo que sigue.

---

## 🔄 Parte 10: Cómo se convierte el JSON en objetos

El paso "mágico" es el `exchange(..., RespuestaPaises.class)`. Desarmemos qué pasa:

```
1. RestTemplate hace un GET a la URL, con el header Authorization: Bearer <token>.
        ↓
2. La API responde con un JSON (texto): {"data":{"objects":[{"names":{...}, ...}]}}
        ↓
3. RestTemplate ve que pediste RespuestaPaises.class.
        ↓
4. Le pasa el JSON a Jackson: "convertí esto en un RespuestaPaises".
        ↓
5. Jackson recorre el JSON de afuera hacia adentro:
   - new RespuestaPaises()
   - Lee "data"     → el atributo es DatosPaises → new DatosPaises() → setData(...)
   - Lee "objects"  → es una lista → por cada elemento, new Pais() → setObjects(...)
   - En cada Pais:
       - "names"      → new NombrePais(), lo llena → setNames(...)
       - "capitals"   → lista: new Capital() por cada una → setCapitals(...)
       - "region", "subregion", "population" → sus setters
   - Ignora todo lo que tus clases no tienen (@JsonIgnoreProperties)
        ↓
6. Devuelve el RespuestaPaises lleno.
```

**Jackson usa el constructor vacío y los setters de tus clases** para llenar los objetos. Por eso los DTOs los necesitan.

> **Conexión con la Etapa 5:** allá, Spring leía el YAML y llenaba `PaisesProperties` con setters. Acá, Jackson lee el JSON de la API y llena tus DTOs con setters. **Es el mismo concepto** (texto estructurado → objeto Java) aplicado a fuentes distintas: un archivo y una respuesta HTTP.

---

## 🏃 Parte 11: Disparar la llamada al arrancar (`CommandLineRunner`)

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

`Arranque` es un `@Component`: recibe el `CatalogoDePaises` inyectado (Etapa 3) y, al arrancar, lo usa para llamar a la API.

### Qué es `CommandLineRunner`

Es una **interfaz de Spring** con un solo método: `run(...)`. Spring **ejecuta el `run()` de todo bean que la implemente, automáticamente, después de arrancar**, cuando todos los beans están listos. Es el lugar correcto para "hacer algo al iniciar la app" (no el constructor, donde la lógica no va).

### Correr y ver datos reales

**Corré la app**, con internet y con tu token real en la config (la demo key devuelve datos de muestra). En el log, algo así:

```
>>> API key cargada: rc_live_... (NN caracteres)
>>> Llamando a la API...
>>> País traído: Argentina
>>> Nombre oficial: Argentine Republic
>>> Capital: Buenos Aires
>>> Región: Americas
>>> Subregión: South America
>>> Población: 46735004
```

(La población exacta depende de cuándo lo corras.)

**¡Datos reales, autenticados, traídos de internet por tu código!** Navegaste toda esa anidación y sacaste justo lo que querías.

> La app queda viva (por Tomcat). Detenela con el botón rojo o con `Ctrl + C`.

> 🔓 **Sin token (API pública).** Con los DTOs de la v3.1 (cuadro de la Parte 8), los getters cambian y no hay nombre oficial ni subregión:
> ```java
> Pais argentina = catalogo.buscarArgentina();
> System.out.println(">>> País traído: " + argentina.getNombre().getComun());
> System.out.println(">>> Capital: " + argentina.getCapitales().get(0));   // ya es un String
> System.out.println(">>> Región: " + argentina.getRegion());
> System.out.println(">>> Población: " + argentina.getPoblacion());
> // Resultado esperado (con la v3.1, cuando era pública): Argentina, Buenos Aires, Americas, 45376763
> ```

---

## 🧪 Parte 12: Experimentos

### Experimento 1: Mirá el texto crudo

Agregá al `run()` de `Arranque`:

```java
System.out.println(">>> JSON crudo: " + catalogo.traerArgentinaComoTexto());
```

Corré. **Resultado esperado:** el JSON **gigante** que devuelve la API, con los ~80 campos (traducciones, colores de bandera, etc.). Compará: el JSON tiene decenas de campos y tus DTOs guardan 6. **Eso es ignorar campos desconocidos en acción.** (Si querés, sacale la anotación a `Pais` y volvé a correr: con Jackson 3 sigue funcionando, porque ignorarlos ya es el comportamiento por defecto.)

### Experimento 2: Buscá otro país

Generalizá la búsqueda. **Agregá** a `CatalogoDePaises` este método, y dejá `buscarArgentina()` como está (la Etapa 6c la usa para explicar la llamada):

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

Y en `Arranque`, cambiá la llamada a `buscarArgentina()` por estas (el método queda en el catálogo):

```java
Pais brasil = catalogo.buscarPorNombre("brazil");
System.out.println(">>> " + brasil.getNames().getCommon() + " - " + brasil.getCapitals().get(0).getName());

Pais japon = catalogo.buscarPorNombre("japan");
System.out.println(">>> " + japon.getNames().getCommon() + " - " + japon.getCapitals().get(0).getName());
// Resultado esperado, algo como: >>> Brazil - Brasília
//                                >>> Japan - Tokyo
```

> 🕳️ **Madriguera — armar URLs bien.** Concatenar el nombre directo en la URL (`"?q=" + nombre`) funciona con nombres simples (incluso con espacios o tildes, que `RestTemplate` codifica solo). Lo que lo rompe son caracteres con significado en una URL, como `&` o `#`: buscá "Bosnia & Herzegovina" y la API recibe otra cosa. La forma correcta de construir URLs es `UriComponentsBuilder`, que se encarga de eso.
> *Volvé al camino — esto se profundiza aparte, otro día.*

### Experimento 3: Reemplazá los DTOs con Lombok

Si tenés Lombok, reemplazá los getters y setters de las cinco clases (`RespuestaPaises`, `DatosPaises`, `Pais`, `NombrePais`, `Capital`). Por ejemplo:

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
// Resultado esperado: funciona idéntico, con muchísimo menos código
```

`@Data` genera getters y setters; `@NoArgsConstructor`, el constructor vacío que Jackson necesita. Con cinco clases anidadas, Lombok te ahorra un montón de *boilerplate* (código repetitivo sin lógica).

### Experimento 4: Token inválido

En la config, cambiá el token por algo inválido (por ejemplo `rc_live_xxxxx`). Si hiciste la Etapa 6b, cambialo en el `.env`. Corré.

**Resultado esperado:** un error HTTP `401 Unauthorized` (o similar) cuando la API rechaza el token, y **la app se cierra**: la excepción ocurre dentro del `run()` de `Arranque`, y Spring aborta el arranque (compará con la Parte 3, donde quedaba viva). **La autenticación es real:** sin token válido, la API no te da nada. Volvé a poner tu token bueno.

### Experimento 5: Qué pasa sin internet

Desconectá tu red y corré.

**Resultado esperado:** una excepción (`ResourceAccessException` o similar) al intentar la llamada, y la app se cierra, igual que en el Experimento 4. La llamada HTTP depende de la red. Volvé a conectar.

> 🕳️ **Madriguera — manejar los errores.** Acá, cualquier error de la API tira abajo la app. Manejarlos bien (reintentar, avisar con un mensaje claro, seguir funcionando sin ese dato) es un tema en sí mismo.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## ✅ Criterios de "Etapa 6 completa"

- [ ] Conseguiste un token (demo key o real).
- [ ] Agregaste `spring-boot-starter-web` y recargaste Maven.
- [ ] Observaste que la app ahora **queda viva** (Tomcat), a diferencia de la Etapa 1.
- [ ] Agregaste `apiKey` a `PaisesProperties`.
- [ ] Pusiste la URL y el token en el `application.yml`, y entendés por qué tu token real no puede quedar ahí al hacer commit (Etapa 6b).
- [ ] Viste `>>> API key cargada: rc_live_...` (el token enmascarado, nunca entero).
- [ ] Creaste el bean `RestTemplate` con `@Bean`.
- [ ] Implementaste la llamada con `exchange` + `Authorization: Bearer` (no `getForObject`).
- [ ] Creaste los cinco DTOs (`RespuestaPaises`, `DatosPaises`, `Pais`, `NombrePais`, `Capital`).
- [ ] Hiciste la llamada que devuelve objetos y navegaste la anidación.
- [ ] Hiciste el Experimento 2 (`buscarPorNombre`): lo vas a usar en la Etapa 7.
- [ ] Creaste `Arranque` con `CommandLineRunner`.
- [ ] Corriste con tu token y viste datos reales de Argentina.
- [ ] Entendés por qué solo mapeamos algunos campos y no todos.
- [ ] Sabés qué cambiaría con una API sin token (los cuadros 🔓).

---

## ✅ Checkpoint

1. ¿Por qué ahora hace falta un token? ¿Qué cambió en la API?
2. ¿Qué dependencia agregaste, qué trajo, y por qué ahora la app queda viva en vez de terminar?
3. ¿Por qué `RestTemplate` se declara con `@Bean` y no con `@Component`?
4. ¿Por qué el token va en la config y no en el código? ¿Y por qué tu token real no debería quedar en el `application.yml` al hacer commit?
5. ¿Por qué el chequeo del constructor no imprime el token entero?
6. ¿Por qué usamos `exchange` en vez de `getForObject`? ¿Cuándo alcanza `getForObject`?
7. ¿Qué hace `headers.setBearerAuth(token)`?
8. ¿Por qué necesitamos cinco clases para mapear la respuesta? ¿Cuántas harían falta con una respuesta plana como la de la v3.1?
9. ¿Por qué `capitals` necesita su propia clase `Capital`, si en la v3.1 era una lista de strings?
10. ¿Cómo llegás del `RespuestaPaises` al primer país? (la cadena de navegación)
11. ¿Por qué NO mapeamos los 80 campos? ¿Qué anotación nos deja ignorar el resto?
12. ¿Qué hace `@JsonProperty("common")`? ¿Cuándo la necesitás?
13. ¿Por qué `population` es `Long` y no `long`?
14. ¿Quién convierte el JSON en objetos, y qué usa de tus clases para hacerlo?
15. ¿De dónde sale la URL base de la API? (pista: Etapa 5)
16. ¿Qué es `CommandLineRunner`?

---

## 🎯 Una reflexión antes de seguir

Mirá todo lo que se juntó acá:

- El **bean con `@Bean`** (Etapa 4) → el `RestTemplate`.
- La **config externa** (Etapa 5) → la URL y el token.
- La **inyección** (Etapa 3) → el `RestTemplate` y las `properties` entrando al catálogo.
- Los **DTOs anidados** → reflejan la estructura del JSON.

Ninguna pieza es nueva en concepto: todas las construiste antes. Lo nuevo fue **juntarlas para hacer algo real y autenticado**.

Y aprendiste algo que los tutoriales suelen esconder: **las APIs del mundo real cambian, exigen autenticación y devuelven estructuras complicadas.** Esta misma API era abierta y plana cuando se dio la clase; hoy pide token y envuelve todo. Con lo que armaste sabés encarar las dos: `getForObject` y pocas clases cuando la API es pública y plana; `exchange` con headers, DTOs que reflejan la estructura y el token bien guardado cuando no lo es.

Solo falta cerrar el panorama: **¿cómo se testea todo esto?** Eso es la **Etapa 7**.

---

## ▶️ Próximo paso

Antes de la Etapa 7, dos etapas **opcionales** que profundizan esta:
- **Etapa 6b** (`proyecto1-etapa6b-proteger-el-token.md`): sacar el token del `application.yml` y guardarlo en un `.env` que git no sube. **Hacela antes de commitear si usás tu token real.**
- **Etapa 6c** (`proyecto1-etapa6c-la-llamada-por-dentro.md`): qué es cada pieza de la llamada (`RestTemplate`, `HttpEntity`, `ResponseEntity`…), qué pasa adentro del `exchange` y en qué momento espera tu código.

Después, seguí con la **Etapa 7** (`proyecto1-etapa7-tests.md`). Ahí vas a:
- Testear tu app con el contexto de Spring levantado (`@SpringBootTest`).
- Reusar AssertJ del Proyecto 0.
- Ajustar los asserts a la estructura de datos de la v5.

Si algo de esta etapa no te cerró, anotá la duda para traerla cuando vuelvas.
