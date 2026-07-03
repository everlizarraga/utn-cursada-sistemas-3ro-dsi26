# 🌱 Proyecto 1 — Etapa 5: Configuración externa (`application.yml` + `@ConfigurationProperties`)

> **Objetivo:** poner un valor de configuración en un archivo externo, leerlo desde tu código, y **comprobar con tus ojos que ese valor llegó a un objeto Java**. Acá respondés de una vez la duda: *"¿cómo se lee el `application.yml`?"*.
>
> **El momento clave:** vas a escribir un valor en un archivo de texto, y verlo aparecer dentro de un objeto de tu código — sin que vos lo hayas pasado por ningún lado. Spring hizo el puente.
>
> **Pre-requisito:** Etapa 4 completa (entendés `@Component`, `@Bean`, `@Configuration`, inyección).
>
> **Tiempo estimado:** 40-50 minutos.

---

## 🧭 Mapa de esta etapa

1. El problema: por qué la configuración no va en el código.
2. `.properties` vs `.yml` — los dos formatos.
3. Poner un valor en el archivo de config.
4. Crear la clase que va a recibir ese valor.
5. Conectar el archivo con la clase (`@ConfigurationProperties`).
6. Correr y comprobar que el valor llegó.
7. Cómo funciona el puente, por dentro.
8. Inyectar la config donde la necesites.
9. Experimentos.
10. Checkpoint.

---

## 🤔 Parte 1: El problema — por qué la config no va en el código

Imaginá que tu app necesita la URL de una API externa, por ejemplo `https://restcountries.com/v3.1`.

Podrías ponerla **hardcodeada** en el código:

```java
public class CatalogoDePaises {
    private final String url = "https://restcountries.com/v3.1";   // hardcodeado
}
```

**Funciona, pero tiene problemas:**

1. **Para cambiarla, tenés que tocar el código y recompilar.** Si mañana la URL cambia, editás Java, compilás de nuevo, redeployás. Tedioso y arriesgado.

2. **No podés tener valores distintos según el entorno.** En tu máquina querés una URL de prueba; en producción, la real. Con hardcodeo, es la misma siempre.

3. **Datos sensibles en el código.** Contraseñas, claves de API, credenciales — no querés eso escrito en tu código fuente (que va a Git, que ven otros, etc.).

**La solución:** poner esos valores en un **archivo de configuración separado del código.** Así los cambiás sin tocar Java, podés tener uno por entorno, y los datos sensibles quedan fuera del código fuente.

Ese archivo es el `application.properties` (o `application.yml`) que el Initializr te generó vacío en la Etapa 0. **Ahora lo vamos a usar de verdad.**

---

## 📄 Parte 2: `.properties` vs `.yml` — los dos formatos

Spring soporta **dos formatos** para ese archivo de config. Hacen lo mismo, cambia la sintaxis.

### Formato `.properties` (el que generó el Initializr)

Pares `clave=valor`, planos:

```properties
paises.url=https://restcountries.com/v3.1
paises.nombre-catalogo=Catálogo Mundial
```

### Formato `.yml` (YAML)

Basado en **indentación** (como Python), permite jerarquía:

```yaml
paises:
    url: https://restcountries.com/v3.1
    nombre-catalogo: Catálogo Mundial
```

**Las dos versiones de arriba significan exactamente lo mismo.** Fijate cómo `paises.url` (properties) se vuelve `paises:` → `url:` (yml). El punto se convierte en jerarquía con indentación.

### ¿Cuál usar?

| Formato | Cuándo conviene |
|---|---|
| `.properties` | Configs simples, planas. Más viejo, sigue vigente. |
| `.yml` | Configs con jerarquía o muchas opciones. Más legible cuando crece. |

**Para este proyecto vamos a usar `.yml`**, por dos razones: es más legible cuando hay jerarquía, y es muy común en proyectos Spring modernos. Pero **lo que aprendas funciona igual con `.properties`** — solo cambia la sintaxis del archivo, no el código Java.

### Convertir el archivo

El Initializr te creó `application.properties`. Vamos a cambiarlo a `.yml`:

1. En `src/main/resources/`, **renombrá** `application.properties` a `application.yml`.
   (Click derecho → Refactor → Rename, o simplemente borralo y creá uno nuevo llamado `application.yml`.)
2. Dejalo vacío por ahora. Vamos a llenarlo en el próximo paso.

> **Cuidado con la indentación en YAML:** YAML usa **espacios**, NO tabs. Y la indentación tiene que ser consistente (normalmente 2 espacios por nivel). Si mezclás tabs y espacios, YAML se rompe con errores raros. IntelliJ normalmente maneja esto bien, pero tenelo presente si algo falla.

---

## ✍️ Parte 3: Poner un valor en el archivo de config

Abrí `application.yml` y escribí esto:

```yaml
paises:
    url: https://restcountries.com/v3.1
    nombre-catalogo: Catálogo Mundial
```

**Qué pusimos:**
- Una sección `paises:`.
- Adentro, dos valores: `url` y `nombre-catalogo`.

Esto es **solo texto**. Spring todavía no lo está usando para nada. Es como tener una nota escrita: el valor existe, pero algo tiene que **leerlo** y meterlo en tu código. Eso es lo que vamos a armar ahora.

> **Sobre los nombres con guion:** usé `nombre-catalogo` (con guion). En el código Java, eso se va a mapear a `nombreCatalogo` (camelCase). Spring hace esa conversión automáticamente. Es una convención: en YAML se usa kebab-case (con guiones), en Java camelCase.

---

## 🏗️ Parte 4: Crear la clase que va a recibir esos valores

Acá viene una idea importante: **para usar esos valores en tu código, Spring los mete en un objeto Java.** Como en Java todo es objeto, necesitás una clase cuyos atributos correspondan a los valores del archivo.

Creá una clase `PaisesProperties` en el package `ar.edu.utn.ba.paises`:

```java
package ar.edu.utn.ba.paises;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paises")
public class PaisesProperties {

    private String url;
    private String nombreCatalogo;

    // Getters y setters (Spring los necesita para asignar los valores)

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNombreCatalogo() {
        return nombreCatalogo;
    }

    public void setNombreCatalogo(String nombreCatalogo) {
        this.nombreCatalogo = nombreCatalogo;
    }
}
```

### Qué tiene esta clase

- **`@ConfigurationProperties(prefix = "paises")`** → le dice a Spring "leé del archivo de config todo lo que esté bajo `paises:` y mapealo a los atributos de esta clase".
- **`private String url`** → corresponde a `paises.url` del YAML.
- **`private String nombreCatalogo`** → corresponde a `paises.nombre-catalogo` del YAML (notá la conversión kebab-case → camelCase).
- **Getters y setters** → **Spring los necesita** para poder asignar los valores. Sin setters, Spring no puede meter los valores en los atributos.

> **¿Te acordás de Lombok del Proyecto 0?** Podrías reemplazar todos esos getters/setters por `@Data`. Lo vas a hacer en un experimento al final. Por ahora los escribimos a mano para que veas que **Spring necesita los setters** — es el mecanismo real, Lombok solo lo abrevia.

### Una cosa más: registrar la clase

`@ConfigurationProperties` por sí sola no alcanza — Spring necesita que le digas "activá esta clase de propiedades". Hay dos formas; vamos con la más simple: agregar `@EnableConfigurationProperties` en tu clase principal.

Abrí `PaisesApplication` y agregá esto:

```java
package ar.edu.utn.ba.paises;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PaisesProperties.class)    // ← agregado
public class PaisesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaisesApplication.class, args);
    }

}
```

**`@EnableConfigurationProperties(PaisesProperties.class)`** le dice a Spring: "tomá `PaisesProperties`, leé el archivo de config, llenala con los valores, y registrala como un bean para que pueda inyectarse".

> **Por qué esta clase necesita un trato especial:** las clases con `@ConfigurationProperties` no llevan `@Component`. Son "POJOs de configuración" (objetos planos que solo guardan datos del archivo). `@EnableConfigurationProperties` es lo que las activa y las convierte en bean. Es una de las dos formas de hacerlo; existe otra (`@Component` directo en la clase de properties), pero esta es la más idiomática. No te detengas en la alternativa ahora.

---

## 🚀 Parte 6: Correr y comprobar que el valor llegó

Para ver que los valores del archivo llegaron al objeto, vamos a imprimirlos. Modificá `CatalogoDePaises` para que reciba las properties y las imprima:

```java
package ar.edu.utn.ba.paises;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CatalogoDePaises {

    private final Random random;
    private final PaisesProperties properties;

    public CatalogoDePaises(Random random, PaisesProperties properties) {
        this.random = random;
        this.properties = properties;
        System.out.println(">>> URL leída del archivo: " + properties.getUrl());
        System.out.println(">>> Nombre del catálogo: " + properties.getNombreCatalogo());
    }

    public int cantidadDePaises() {
        return 195;
    }

    public int numeroDePaisAlAzar() {
        return random.nextInt(195) + 1;
    }
}
```

**Corré la app.** En el log vas a ver:

```
>>> URL leída del archivo: https://restcountries.com/v3.1
>>> Nombre del catálogo: Catálogo Mundial
```

**Ahí está.** Los valores que escribiste en `application.yml` **aparecieron dentro de tu objeto Java.** Vos no los pasaste por ningún lado — Spring leyó el archivo, los metió en `PaisesProperties`, y te inyectó ese objeto en el `CatalogoDePaises`.

---

## 🌉 Parte 7: Cómo funciona el puente, por dentro

Ahora que lo viste, entendamos la cadena completa:

```
1. Spring arranca.
        ↓
2. Lee application.yml automáticamente (sin que vos le digas dónde está —
   lo busca en src/main/resources/ por convención).
        ↓
3. Ve @EnableConfigurationProperties(PaisesProperties.class).
        ↓
4. Crea un objeto PaisesProperties (new PaisesProperties()).
        ↓
5. Mira el prefix = "paises" y busca en el YAML todo lo que esté bajo "paises:".
        ↓
6. Por cada valor, llama el setter correspondiente:
   - paises.url → properties.setUrl("https://...")
   - paises.nombre-catalogo → properties.setNombreCatalogo("Catálogo Mundial")
        ↓
7. Guarda ese PaisesProperties (ya lleno) como un bean.
        ↓
8. Cuando CatalogoDePaises lo pide en su constructor, Spring se lo inyecta.
```

**Tres cosas que respondés con esto:**

- **"¿Cómo se lee el archivo?"** → Spring lo lee solo al arrancar, lo busca por convención en `src/main/resources/`. No tenés que llamar a ningún "leer archivo" — pasa automáticamente.

- **"¿Quién lo llama?"** → Nadie en tu código. Es parte de la autoconfiguración de Spring Boot. El framework lo hace al arrancar.

- **"¿Por qué necesita una clase?"** → Porque en Java los datos viven en objetos. Spring lee texto del archivo y lo "vuelca" en los atributos de un objeto, usando los setters. Ese objeto (`PaisesProperties`) es la representación Java de tu config.

> **El detalle del setter:** ¿ves por qué `PaisesProperties` necesita setters? Porque el paso 6 los usa. Spring hace `new PaisesProperties()` (constructor vacío) y después `setUrl(...)`, `setNombreCatalogo(...)`. Sin setters, no podría asignar nada.

---

## 🔌 Parte 8: Usar la config donde la necesites

Ya viste que `CatalogoDePaises` recibe las properties. Eso es lo normal: **inyectás `PaisesProperties` en cualquier bean que necesite valores de config**, igual que inyectabas el `Random` o el `CatalogoDePaises`.

Por ejemplo, cuando en la Etapa 6 hagas la llamada HTTP, vas a necesitar la URL. La vas a sacar de `properties.getUrl()`. La config ya va a estar disponible para inyectar donde la necesites.

**Es la misma mecánica de siempre:** un bean (`PaisesProperties`) que se inyecta en otros beans. Lo único nuevo es **de dónde salieron sus valores** (de un archivo, no de código).

---

## 🧪 Parte 9: Experimentos

### Experimento 1: Cambiá el valor en el archivo, sin tocar Java

En `application.yml`, cambiá la URL:

```yaml
paises:
    url: https://otra-url-de-prueba.com
    nombre-catalogo: Catálogo Mundial
```

Corré la app **sin tocar ni una línea de Java**. El log ahora muestra:

```
>>> URL leída del archivo: https://otra-url-de-prueba.com
```

**Ese es el punto de toda la etapa:** cambiaste el comportamiento de la app **editando solo un archivo de texto**, sin recompilar lógica. Eso es lo que la configuración externa te da. Volvé a poner la URL original después.

### Experimento 2: Quitá un valor del YAML

Comentá la línea `nombre-catalogo` en el YAML:

```yaml
paises:
    url: https://restcountries.com/v3.1
    # nombre-catalogo: Catálogo Mundial
```

Corré. El log muestra:

```
>>> Nombre del catálogo: null
```

**¿Por qué `null`?** Porque el valor no está en el archivo, entonces Spring nunca llamó a `setNombreCatalogo(...)`, y el atributo quedó en su valor por defecto (`null` para un String). **Esto te enseña que si un valor falta en el archivo, el atributo queda sin asignar.** Volvé a poner la línea después.

### Experimento 3: Reemplazá los getters/setters con Lombok

Si tenés Lombok disponible (lo trajiste del Proyecto 0; si no, agregá la dependencia al `pom.xml` como hiciste allá), reemplazá todo el boilerplate de `PaisesProperties`:

```java
package ar.edu.utn.ba.paises;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paises")
@Data
public class PaisesProperties {
    private String url;
    private String nombreCatalogo;
}
```

Corré. **Funciona idéntico.** `@Data` generó los getters y setters que Spring necesita. Esto te muestra que Lombok y Spring se llevan bien: Lombok genera los setters, Spring los usa. (Acordate del Proyecto 0: `@Data` genera setters solo para campos no-`final`. Por eso acá los campos no son `final` — Spring necesita poder asignarlos.)

> Para Lombok en un proyecto Spring, recordá agregar la dependencia al `pom.xml`. Como tu proyecto Spring hereda del parent de Spring Boot, **no necesitás poner la versión de Lombok** — el parent ya la define. Solo el `groupId` y `artifactId`.

### Experimento 4: Mirá qué pasa con un valor mal escrito

En el YAML, escribí mal una clave a propósito:

```yaml
paises:
    urlllll: https://restcountries.com/v3.1     # mal escrito
    nombre-catalogo: Catálogo Mundial
```

Corré. El log muestra:

```
>>> URL leída del archivo: null
```

**`urlllll` no coincide con ningún atributo de `PaisesProperties`** (que tiene `url`, no `urlllll`). Entonces Spring no encuentra dónde meter ese valor, y el atributo `url` queda `null`. **Lección: los nombres del YAML tienen que coincidir con los atributos de la clase** (con la conversión kebab→camel). Arreglá el typo después.

---

## ✅ Criterios de "Etapa 5 completa"

- [ ] Convertiste `application.properties` a `application.yml`.
- [ ] Escribiste valores bajo `paises:` en el YAML.
- [ ] Creaste `PaisesProperties` con `@ConfigurationProperties(prefix = "paises")`.
- [ ] Agregaste `@EnableConfigurationProperties(PaisesProperties.class)` en `PaisesApplication`.
- [ ] Inyectaste `PaisesProperties` en `CatalogoDePaises` y viste los valores en el log.
- [ ] Hiciste el Experimento 1 (cambiar el valor sin tocar Java).
- [ ] Hiciste el Experimento 2 (quitar un valor → null).
- [ ] Entendés cómo Spring lee el archivo y lo vuelca en el objeto (los pasos de la Parte 7).
- [ ] Entendés por qué `PaisesProperties` necesita setters.
- [ ] Dejaste el YAML y las clases en estado correcto (sin typos ni líneas comentadas).

---

## ✅ Checkpoint

1. ¿Por qué la configuración (URLs, credenciales) no se pone hardcodeada en el código?
2. ¿Qué diferencia hay entre `.properties` y `.yml`? ¿Hacen lo mismo?
3. ¿Quién lee el `application.yml`? ¿Vos tenés que llamar a algo para leerlo?
4. ¿Por qué `PaisesProperties` necesita setters?
5. ¿Cómo se mapea `nombre-catalogo` (YAML) a `nombreCatalogo` (Java)?
6. ¿Qué hace `@ConfigurationProperties(prefix = "paises")`?
7. ¿Qué pasa si un valor del YAML está mal escrito o falta? ¿Por qué?
8. Una vez que `PaisesProperties` es un bean, ¿cómo lo usás en otros beans?

---

## 🎯 Una reflexión antes de seguir

Acabás de resolver, **construyéndola con tus manos**, la duda que arrastrabas: *cómo se lee el `application.yml`*. Y la respuesta resultó ser una cadena clara, sin magia:

1. Spring lee el archivo al arrancar (automático, por convención).
2. Crea un objeto de tu clase de properties.
3. Llena los atributos con los valores del archivo, usando los setters.
4. Te entrega ese objeto como un bean, para inyectar donde quieras.

El archivo de texto → un objeto Java → inyectado donde lo necesites. Ese es todo el truco.

Ahora tenés **todas las piezas de Spring que necesitás** para hacer algo de verdad útil: sabés crear beans (`@Component`, `@Bean`), conectarlos (inyección), y configurarlos desde afuera (`@ConfigurationProperties`). En la **Etapa 6** vas a juntar todo para hacer que tu app **salga a internet, llame a una API real, y traiga datos** — usando la URL que acabás de aprender a configurar.

---

## ▶️ Próximo paso

Cuando termines, seguí con la **Etapa 6** (archivo `proyecto1-etapa6-llamada-http.md`). Ahí vas a:
- Agregar la dependencia web al `pom.xml` (y ver cómo cambia el arranque de la app).
- Crear un cliente HTTP (`RestTemplate`) con `@Bean` (lo que aprendiste en la Etapa 4, ahora con algo real).
- Hacer una llamada a una API y traer datos de verdad.

Si algo de esta etapa no te cerró, anotá la duda para traerla cuando vuelvas online.
