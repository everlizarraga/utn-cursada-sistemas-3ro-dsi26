# 🌱 Proyecto 1 — Etapa 6b: Proteger el token (archivo `.env`)

> **Objetivo:** sacar tu token del `application.yml` y guardarlo en un archivo `.env` que **git no sube al repo**. El YAML queda con una **referencia** al token, no con el token.
>
> **El momento clave:** vas a borrar el token del `application.yml`, correr la app, y ver que sigue llegando a `PaisesProperties`, sin que esté escrito en ningún archivo que vaya al repo.
>
> **Pre-requisito:** Etapa 6 completa (o al menos hasta la Parte 6: `apiKey` en `PaisesProperties` y el chequeo `>>> API key cargada` en el constructor de `CatalogoDePaises`). Es opcional, pero **hacela antes de commitear si usás tu token real**.
>
> **Tiempo estimado:** 30-40 minutos.
>
> **Versiones:** Java 21 · Spring Boot 4.0.x · Maven 3.9 (el del wrapper `mvnw`).

---

## 🧭 Mapa de esta etapa

1. El problema: el token en un archivo que se sube.
2. Pieza 1: un *placeholder* en el YAML.
3. Pieza 2: el archivo `.env`, con el valor real.
4. La línea que conecta las dos: `spring.config.import`.
5. Cómo viaja el valor.
6. Lo que sí se sube (`.env.example`) y lo que no (`.gitignore`).
7. Comprobar que llegó.
8. Si algo falla (incluido el directorio de trabajo).
9. Variantes y otras formas.
10. Experimentos.
11. Checkpoint.

> 🔓 **Sin token (API pública).** Esta etapa no aplica: no hay nada que proteger.

---

## 🤔 Parte 1: El problema

En la Etapa 6 dejaste esto en el `application.yml`:

```yaml
paises:
    url: https://api.restcountries.com/countries/v5
    nombre-catalogo: Catálogo Mundial
    api-key: rc_live_tu_token_real
```

Funciona, pero el `application.yml` **se sube al repo**. Si hacés commit así:
- tu token queda a la vista de cualquiera que vea el repo;
- queda en el **historial de git**: aunque en el commit siguiente lo borres, sigue en los commits viejos.

Un token es una **credencial privada**, como una contraseña. Lo que queremos:

```
 application.yml  (se sube)        →  dice DÓNDE buscar el token
 .env             (NO se sube)     →  tiene el token
```

**La idea en una línea:** el `application.yml` deja de tener el token y pasa a tener una **referencia**; el valor real vive en un archivo `.env` que **no se sube al repo**.

De `PaisesProperties` en adelante **no cambia nada**: tu código sigue usando `properties.getApiKey()` sin saber de dónde salió el valor.

---

## 🕳️ Parte 2: Pieza 1, un *placeholder* en el YAML

Un **placeholder** es un hueco con nombre, `${NOMBRE}`, que Spring completa al arrancar buscando ese nombre en otro lado.

En el `application.yml`, reemplazá el token por un placeholder y agregá la línea `spring.config.import` (la explica la Parte 4):

```yaml
spring:
    config:
        import: file:.env[.properties]

paises:
    url: https://api.restcountries.com/countries/v5
    nombre-catalogo: Catálogo Mundial
    api-key: ${RESTCOUNTRIES_API_KEY}       # ← ya no está el token: está el nombre de dónde sacarlo
```

`RESTCOUNTRIES_API_KEY` es un nombre que elegís vos. Por convención, estos nombres van en mayúsculas y con guiones bajos.

> Si en tu `application.yml` ya tenías una sección `spring:` (por ejemplo, `spring.application.name`), agregá `config:` adentro de esa misma sección: no puede haber dos `spring:` en el mismo archivo.

---

## 📄 Parte 3: Pieza 2, el archivo `.env`

Creá un archivo llamado `.env` (con el punto adelante) **en la carpeta del proyecto, al lado del `pom.xml`**:

```
tu-proyecto/
├── .env              ← RESTCOUNTRIES_API_KEY=rc_live_tu_token_real   (NO se sube)
├── .env.example      ← RESTCOUNTRIES_API_KEY=                        (SÍ se sube; Parte 6)
├── pom.xml
└── src/main/resources/application.yml
```

Contenido del `.env`:

```properties
RESTCOUNTRIES_API_KEY=rc_live_tu_token_real
```

> ⚠️ **Sin espacios alrededor del `=` y sin comillas.** Si escribís `RESTCOUNTRIES_API_KEY="rc_live_..."`, las comillas pasan a ser parte del token y la API lo rechaza.

> ⚠️ **Si IntelliJ te pregunta si agregar el archivo a Git, respondé _No_.** Si le decís que sí, git empieza a seguirlo, y el `.gitignore` de la Parte 6 ya no lo frena. Lo más seguro: hacé primero el `.gitignore` de la Parte 6 y después creá el `.env`.

> **Los avisos de IntelliJ al crear el `.env`:**
> - *"You are editing a file that is ignored"* → **buena señal**: confirma que git lo ignora (si ya tenés el `.gitignore` de la Parte 6).
> - *"Plugins supporting .env files found"* → sugerencia opcional. El plugin `.env files` le da colores y autocompletado; no hace falta para que funcione.

---

## 🔗 Parte 4: La línea que conecta las dos (`spring.config.import`)

Spring **no lee archivos `.env` por su cuenta**: solo lee `application.yml`. La línea `import` le dice que también lea el `.env`.

```yaml
spring:
    config:
        import: file:.env[.properties]
```

| Pedazo | Qué significa |
|---|---|
| `file:` | buscalo en el disco, **relativo al directorio de trabajo** (la carpeta desde donde arranca la app), no en `src/main/resources` |
| `.env` | el nombre del archivo |
| `[.properties]` | "leelo con formato `.properties`". Spring decide cómo leer un archivo por su extensión, y `.env` no tiene una que conozca. El formato `CLAVE=valor` del `.env` es exactamente el de un `.properties` |

Sin `optional:` adelante (ver Parte 9), si el archivo no está, **la app no arranca** y te dice por qué. Con un token obligatorio, eso es lo que querés.

---

## 🧭 Parte 5: Cómo viaja el valor

```
 .env                            application.yml                         PaisesProperties
 ────                            ───────────────                         ────────────────
 RESTCOUNTRIES_API_KEY=rc_...    import: file:.env[.properties]
        │                               │
        │   Spring lo carga como una propiedad más, llamada RESTCOUNTRIES_API_KEY
        └──────────────────────────────►│
                                        │
                                 api-key: ${RESTCOUNTRIES_API_KEY}
                                        │   Spring completa el hueco
                                        ▼
                                 paises.api-key = rc_...  ──────────►  apiKey = "rc_..."
                                                                        (setApiKey, como en la Etapa 5)
```

Del lado de Java no tocaste nada: `PaisesProperties` recibe el mismo valor que antes, por el mismo camino de la Etapa 5.

---

## 🙈 Parte 6: Lo que sí se sube y lo que no

### El `.env.example`: lo que sí se sube

Quien clone tu repo necesita saber qué variables tiene que completar. Para eso, un `.env.example` con las mismas claves **sin valores**:

```properties
# Copiá este archivo como .env y poné tu token de https://restcountries.com
# (para probar sin registrarte: rc_live_demo)
RESTCOUNTRIES_API_KEY=
```

### El `.gitignore`: que git no suba el `.env`

En el `.gitignore` de tu repo (si todavía no las tiene), agregá:

```
.env
.env.*
!.env.example
```

- `.env` ignora el archivo con el token, en cualquier carpeta.
- `.env.*` ignora variantes como `.env.local`.
- `!.env.example` es la excepción: el `!` significa "este sí lo subís".

### Comprobalo antes de cada commit

```bash
git check-ignore -v .env
```

**✅ Deberías ver** la regla que lo ignora, algo como `.gitignore:NN:.env    .env`. Si no imprime nada, el `.env` **no** está ignorado: revisá el `.gitignore`. Además, con `git status --short` el `.env` no tiene que aparecer nunca.

> ⚠️ **Si ya habías commiteado el token** (en el `application.yml` o en un `.env`), el `.gitignore` no alcanza: el token quedó en el historial. **Revocalo** en restcountries.com y generá uno nuevo. Si el archivo commiteado era el `.env`, además sacalo del repo con `git rm --cached .env` (lo borra de git y lo deja en tu disco).

---

## ✅ Parte 7: Comprobar que llegó

En la Etapa 6 (Parte 6) el constructor de `CatalogoDePaises` ya tiene el chequeo:

```java
String key = properties.getApiKey();
System.out.println(">>> API key cargada: " +
        (key == null || key.isBlank()
                ? "NO (vacía)"
                : key.substring(0, Math.min(8, key.length())) + "... (" + key.length() + " caracteres)"));
```

Corré la app.

**✅ Deberías ver** `>>> API key cargada: rc_live_... (NN caracteres)`, con el largo de **tu token real**, y la app funcionando igual que al final de la Etapa 6. La demo key tiene 12 caracteres: si ves 12, se cargó la demo.

---

## 🩺 Parte 8: Si algo falla

| Ves esto | Qué pasa | Qué hacés |
|---|---|---|
| La app no arranca: `Config data resource 'file [.env]' ... does not exist` | Spring no encuentra el `.env` | ver "El directorio de trabajo", abajo |
| `>>> API key cargada: ${RESTCO...` | el placeholder no se completó: el `.env` no tiene esa clave, o está mal escrita | revisá el nombre en el `.env`: tiene que ser idéntico al del `${...}` |
| `>>> API key cargada: NO (vacía)` | la clave está, pero sin valor: típico de copiar el `.env.example` tal cual | completá el valor en el `.env` |
| `... (12 caracteres)` y querías tu token real | se cargó la demo key | revisá qué hay en el `.env` |
| `401 Unauthorized` al llamar a la API | la API rechazó el token (401 es el código HTTP de "no autorizado") | token mal copiado, con comillas o espacios, o revocado |

**Ojo con el segundo caso:** con `@ConfigurationProperties`, si un `${...}` no se puede completar, Spring **no falla**: deja el texto `${RESTCOUNTRIES_API_KEY}` tal cual, y eso es lo que le mandaría a la API como token. Por eso conviene el chequeo del constructor.

### El directorio de trabajo

`file:.env` se busca **desde la carpeta donde arrancó la app**, que se ve al final de la primera línea del log:

```
Starting PaisesApplication ... started by tu-usuario in /ruta/donde/arranco
```

- **Desde la terminal**, parado en la carpeta del proyecto (`./mvnw spring-boot:run`), arranca ahí: encuentra el `.env`.
- **Con el ▶ de IntelliJ**, arranca por defecto desde la **carpeta que abriste en IntelliJ**. Si es la del proyecto, anda. Si abriste una carpeta más arriba (un repo con varios proyectos, por ejemplo), no lo encuentra.

**Arreglo en IntelliJ:** *Run → Edit Configurations…* → tu configuración (`PaisesApplication`) → campo **Working directory** (si no lo ves, está en *Modify options*) → escribí `$MODULE_WORKING_DIR$` → **Apply**. Esa *macro* (una variable que IntelliJ reemplaza al ejecutar) significa "la carpeta del módulo que estás corriendo".

- Para que valga para todas las configuraciones nuevas, hacé lo mismo en *Edit configuration templates…* → **Spring Boot** (en IntelliJ Community, que no tiene esa plantilla: **Application**). Para los tests de la Etapa 7, también en la plantilla **JUnit**.
- La macro funciona si IntelliJ reconoce tu proyecto como módulo Maven. Si no, clic derecho en el `pom.xml` → *Add as Maven Project*, o escribí la ruta de la carpeta a mano.

---

## 🔀 Parte 9: Variantes y otras formas

### Variante: arrancar aunque no haya `.env`

Si querés que el proyecto corra recién clonado, sin crear nada, usá `optional:` y un valor por defecto después de `:`:

```yaml
spring:
    config:
        import: optional:file:.env[.properties]    # optional: si no está, no falla

paises:
    api-key: ${RESTCOUNTRIES_API_KEY:rc_live_demo} # si no encuentra la clave, usa la demo
# Resultado esperado sin .env: la app arranca con rc_live_demo (12 caracteres en el chequeo)
```

Es cómodo para quien prueba tu repo. La contra: si te olvidás el `.env`, **no te enterás**, y trabajás con la demo creyendo que es tu token. Por eso el camino principal va sin `optional:`: si falta el `.env`, la app no arranca y te lo dice.

> 🕳️ **Madriguera — otras formas de pasar el token.** El `${RESTCOUNTRIES_API_KEY}` también se completa con una **variable de entorno** (un valor con nombre que el sistema operativo le pasa a cada programa al arrancarlo), y esa le gana al `.env`. Se puede definir en la terminal (`RESTCOUNTRIES_API_KEY=rc_live_xxx ./mvnw spring-boot:run`) o en la configuración de IntelliJ (*Modify options → Environment variables*). En un servidor real se usan variables de entorno, no un `.env` copiado a mano. El `${...}` funciona con las dos; solo hay que pasar el import a `optional:file:.env[.properties]`, porque en el servidor no hay `.env` y sin `optional:` la app no arranca.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 🧪 Parte 10: Experimentos

### Experimento 1: Sin `.env`

Renombrá tu `.env` a `.env.tmp` y corré.

**Resultado esperado:** la app **no arranca**, y el error dice que `file [.env]` no existe. Volvé a renombrarlo a `.env`.

### Experimento 2: Clave mal escrita

En el `.env`, cambiá el nombre a `RESTCOUNTRIES_APIKEY` (sin el guion bajo del medio) y corré.

**Resultado esperado:** Spring **no se queja** al leer la config: el chequeo del constructor muestra `>>> API key cargada: ${RESTCO...`. Recién cuando el `Arranque` llama a la API, la API responde `401 Unauthorized` (o un error similar) y la app se cierra. Es el caso silencioso de la Parte 8: el problema está en la config, pero aparece más tarde, en la llamada. Volvé a poner el nombre bien.

### Experimento 3: La variante `optional:`

1. Pasá el import a `optional:file:.env[.properties]` y el placeholder a `${RESTCOUNTRIES_API_KEY:rc_live_demo}`.
2. Renombrá el `.env` a `.env.tmp` y corré.

**Resultado esperado:** la app **arranca**, y el chequeo muestra `(12 caracteres)`: está usando la demo key, en silencio. Compará con el Experimento 1. Volvé a dejar todo como en la Parte 2 y renombrá el archivo a `.env`.

### Experimento 4: Que git no lo vea

Corré `git status --short` y `git check-ignore -v .env`.

**Resultado esperado:** en `git status`, el `.env.example` aparece (si es nuevo) y el `.env` no; `check-ignore` muestra la regla del `.gitignore` que lo ignora.

---

## ✅ Criterios de "Etapa 6b completa"

- [ ] El `application.yml` tiene `api-key: ${RESTCOUNTRIES_API_KEY}` y el `spring.config.import`, y **ya no tiene el token**.
- [ ] El token está en un `.env` al lado del `pom.xml`, sin espacios ni comillas.
- [ ] Creaste el `.env.example` con la clave sin valor.
- [ ] El `.gitignore` tiene `.env`, `.env.*` y `!.env.example`, y `git check-ignore -v .env` lo confirma.
- [ ] Viste `>>> API key cargada: rc_live_... (NN caracteres)` con el largo de tu token real.
- [ ] La app funciona igual que al final de la Etapa 6.
- [ ] Hiciste los Experimentos 1 y 2, y entendés por qué uno falla al arrancar y el otro no.

---

## ✅ Checkpoint

1. ¿Por qué el token real no puede quedar en el `application.yml`, si la URL sí?
2. ¿Qué es un placeholder? ¿Qué hace `${RESTCOUNTRIES_API_KEY}` en el YAML?
3. ¿Qué hace la línea `spring.config.import: file:.env[.properties]`? ¿Para qué está el `[.properties]`?
4. ¿Dónde va el `.env` y por qué justo ahí?
5. Corrés con el ▶ y la app dice que `file [.env]` no existe, aunque el archivo está. ¿Qué está pasando y cómo lo arreglás?
6. Si el `.env` no tiene la clave `RESTCOUNTRIES_API_KEY`, ¿qué valor llega a `apiKey`? ¿Cómo te das cuenta?
7. ¿Para qué sirve el `.env.example`, si no tiene ningún valor?
8. Ya habías commiteado el token una vez. ¿Alcanza con agregar el `.env` al `.gitignore`? ¿Qué más tenés que hacer?
9. ¿Qué ganás y qué perdés con la variante `optional:` + valor por defecto?

---

## 🎯 Una reflexión antes de seguir

Separaste dos cosas que hasta ahora vivían juntas:
- **qué configuración necesita la app** (el `application.yml`, que se comparte), y
- **los valores secretos** (el `.env`, que es tuyo).

Y lo hiciste sin tocar una línea de Java: el placeholder y el import trabajan antes de que `PaisesProperties` reciba el valor. Es la misma idea que vas a ver en cualquier proyecto con credenciales, en el TP incluido: la configuración se sube, los secretos no.

---

## ▶️ Próximo paso

- Si querés: **Etapa 6c** (`proyecto1-etapa6c-la-llamada-por-dentro.md`), opcional: qué pasa adentro del `exchange`.
- Después (o si preferís saltearla): **Etapa 7** (`proyecto1-etapa7-tests.md`). Los tests levantan la app completa, así que también leen tu `.env`.
