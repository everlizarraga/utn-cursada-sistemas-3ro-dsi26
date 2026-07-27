# 🌱 Proyecto 0 (clase 04) — Etapa 0: El proyecto + el sondeo

> **Objetivo:** dejar creado y corriendo el proyecto Spring de la veterinaria — **por la vía que elijas** — y sondear en 20 minutos cuánto quedó vivo de lo que construiste en el proyecto-1 (beans e inyección), de memoria, sin mirar nada.
>
> **El momento clave de esta etapa:** la primera corrida. Vas a predecir algo sobre ella… y lo que veas te va a decir exactamente en qué se diferencia este proyecto de todo lo que construiste antes.
>
> **Pre-requisito:** JDK 21 + IntelliJ + Maven funcionando. El proyecto-1 terminado alguna vez. Si vas por la Vía B: tu monorepo de cursada.
>
> **Tiempo estimado:** 30-40 minutos.

---

## 🧭 Mapa de esta etapa

1. Elegir tu vía y crear el proyecto.
2. La config mínima (`application.yaml`).
3. Primera corrida — **con predicción antes**.
4. El sondeo de reactivación (de memoria, sin mirar).
5. Autoevaluación honesta del sondeo.
6. Experimento: romperlo con el puerto.
7. Criterios de completada + checkpoint + registro.

---

## 🛠️ Parte 1: Elegí tu vía

Las dos llegan al mismo lugar; de la Etapa 1 en adelante **todo es idéntico**. La diferencia es dónde vive el proyecto y cómo se arranca.

### Vía A — Proyecto independiente (Initializr)

1. [start.spring.io](https://start.spring.io) → **Maven** · **Java** · Spring Boot **4.x estable** · Group `com.practica` · Artifact `veterinaria` · **Java 21**.
2. Dependencias: **Spring Web** y **Lombok**. *(En Boot 4, esa dependencia se llama `spring-boot-starter-webmvc` en el pom — es la que trae todo el mundo HTTP-entrante, y es la misma que usa el proyecto de la clase.)*
3. Generate → descomprimí → abrilo en IntelliJ → dejá que importe el Maven.

### Vía B — Módulo en tu monorepo de cursada

Sin Initializr: un módulo se arma a mano, y su anatomía mínima es esta checklist:

- [ ] Carpeta del módulo en la raíz del monorepo (ej.: `clase-04-prac-01-proy-00/`).
- [ ] Su entrada en el `<modules>` del pom padre.
- [ ] El **pom hijo** con: `<parent>` apuntando al padre (con `relativePath`), su `artifactId`, la dependencia **`spring-boot-starter-webmvc`**, `spring-boot-starter-test` (scope test — la vas a usar recién en el bonus), y el plugin `spring-boot-maven-plugin` declarado en `<build>`. *(Lombok no va: se hereda del padre.)*
- [ ] La estructura `src/main/java/com/practica/veterinaria/` y `src/main/resources/`.
- [ ] La **main class** `VeterinariaApplication` — y acá tu primer mini-sondeo: **escribila de memoria** (son ~6 líneas: una anotación y un main con una línea adentro). Si no sale, está en el recorrido P5 §6 — pero intentá primero.

👀 *Dos variantes que acabás de pisar sin darte cuenta:* **(1)** el starter que en el 95% de los tutoriales del mundo vas a ver como `spring-boot-starter-web` — ese es su nombre clásico (Boot ≤3); en Boot 4 lo renombraron a `-webmvc`. Misma cosa, dos nombres: usá `webmvc` (el de la cátedra), reconocé `web` cuando lo veas. **(2)** Hay dos formas de "enchufar" un proyecto a Boot: heredar de `spring-boot-starter-parent` (lo que hace tu monorepo) o **importar el BOM** `spring-boot-dependencies` vía `dependencyManagement` (lo que hace el pom raíz del profe, porque su raíz no puede tener otro parent). Las dos gestionan las versiones por vos; te vas a cruzar ambas.

## ⚙️ Parte 2: La config mínima

En `src/main/resources/`, renombrá `application.properties` → `application.yaml` (o crealo, si sos Vía B) y dejalo así:

```yaml
spring:
  application:
    name: veterinaria
server:
  port: 8083
```

¿Por qué 8083? Para que pueda convivir levantado con el `sales-service` de la clase (8082) sin pelearse. Vas a querer los dos vivos a la vez cuando llegue el momento de comparar.

👀 *La forma `.properties` decía lo mismo con otra sintaxis (`server.port=8083`, claves planas con puntos). Las dos conviven en el ecosistema; misma información, distinto formato.*

## 🚀 Parte 3: Primera corrida — predecí antes

Antes de correr, **anotá tu predicción** (en papel, en serio):

> En tu proyecto-1, al principio (antes de la etapa de HTTP), corrías la app y el proceso **arrancaba, hacía lo suyo y TERMINABA** — `Process finished with exit code 0`, ¿te acordás? Pregunta: **¿este proyecto va a terminar igual, o va a pasar otra cosa? ¿Por qué?**

Ahora sí, arrancala:

- **Vía A:** play verde en `VeterinariaApplication`, o `mvn spring-boot:run` desde la carpeta del proyecto.
- **Vía B:** play verde en `VeterinariaApplication` (IntelliJ es idéntico en ambas vías), o desde la **raíz del monorepo**: `./mvnw -pl clase-04-prac-01-proy-00 spring-boot:run` *(el `-pl` que ya conocés del proyecto de la clase — y `./mvnw` porque tu repo trae el wrapper: usa la versión de Maven del proyecto, no la de tu máquina).*

**Observá el log** y contrastá con tu predicción:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
 ...
 :: Spring Boot ::                (v4.x.x)

... Starting VeterinariaApplication using Java 21 ...
... Tomcat initialized with port 8083 (http) ...
... Tomcat started on port(s): 8083 (http) ...      ← LA LÍNEA NUEVA
... Started VeterinariaApplication in 1.2 seconds
█                                                   ← ...¿y el exit code 0?
```

**No termina.** El proceso queda **vivo**, esperando. Eso es Tomcat: un servidor web que el starter `webmvc` trajo consigo y que Spring Boot arrancó solo, escuchando en el puerto 8083. Tu app dejó de ser un programa que corre-y-termina: **ahora es un servicio que espera que alguien le hable.** Nadie le va a hablar todavía (no tiene ningún endpoint) — pero la puerta ya está abierta. Toda esta etapa del andamiaje — este proyecto entero — vive en esa diferencia.

Frenala con el cuadrado rojo (o `Ctrl+C`). Arrancala de nuevo. Es tuya.

## 🧠 Parte 4: El sondeo de reactivación

Ahora, lo pactado: **sin abrir el proyecto-1, sin mirar el recorrido, sin buscar nada** — solo vos y lo que quedó. Dos tareas, en este mismo proyecto:

**Sondeo A — el bean.** Creá una clase `Saludador` (en el mismo package que `VeterinariaApplication`) que Spring construya solo al arrancar, y que lo demuestre imprimiendo `>>> Saludador listo` en el constructor. Corré y verificá que el mensaje aparece **sin ningún `new` tuyo en ningún lado**.

**Sondeo B — la inyección.** Creá una segunda clase `Recepcion`, también administrada por Spring, que **reciba al `Saludador` en su constructor** y desde ahí imprima `>>> Recepcion abierta, saludador inyectado`. Corré: los dos mensajes, ¿en qué orden aparecen y por qué ese orden?

*(Estas dos clases son descartables — las borrás al final de la etapa. Son el termómetro, no el edificio.)*

## 📊 Parte 5: Autoevaluación honesta

Sin trampa, porque el único estafado serías vos:

- **🟢 Fluido** (salieron las dos, quizás dudaste en el nombre exacto de una anotación y el IDE te la completó): perfecto — la base del proyecto-1 está viva. Borrá las dos clases y a la Etapa 1.
- **🟡 Con agujeros** (sabías QUÉ había que hacer pero no CÓMO — qué anotación va, cómo se pide la dependencia): decime por chat exactamente dónde te trabaste y lo reactivamos ahí mismo en cinco minutos — es óxido, no ausencia.
- **🔴 En blanco** (no supiste ni por dónde arrancar): también al chat, sin drama — significa que antes de la Etapa 1 hacemos un refresco exprés de beans+DI (una mini-sesión, no re-hacer el proyecto-1). Mejor saberlo HOY que descubrirlo en la Etapa 4 con la app a medio construir.

La regla de este sondeo: **el resultado no se juzga, se usa.** Para eso existe.

## 🧨 Parte 6: Experimento — el puerto ocupado

Con la app corriendo, **arrancala otra vez** (segundo play, o segunda terminal con el mismo comando de tu vía). Predicción: ¿qué va a pasar?

Va a explotar — y el punto del experimento es **leer el error entero**, porque es EL error más común de la vida real con Spring:

```
***************************
APPLICATION FAILED TO START
***************************

Description:
Web server failed to start. Port 8083 was already in use.

Action:
Identify and stop the process that's listening on port 8083 or configure
this application to listen on another port.
```

Fijate la estructura: Spring Boot te da **Description** (qué pasó) y **Action** (qué hacer). No todos los errores van a ser así de amables — pero este formato de leer primero el fondo del stacktrace buscando la descripción va a ser tu reflejo de acá en adelante. Matá una de las dos instancias y listo.

## ✅ Criterios de "Etapa 0 completa"

- [ ] El proyecto/módulo `veterinaria` existe, compila y corre (Vía B: figura en el `<modules>` del padre y el `-pl` lo encuentra).
- [ ] `application.yaml` con puerto 8083.
- [ ] Viste (y entendés) que el proceso queda vivo con Tomcat escuchando.
- [ ] Sondeo A y B intentados **de memoria**, y autoevaluación hecha (🟢/🟡/🔴 — cualquiera de los tres es "completo": el sondeo se completa haciéndolo, no aprobándolo).
- [ ] Provocaste y leíste el error de puerto ocupado.
- [ ] `Saludador` y `Recepcion` borrados (el termómetro ya midió).

## ✅ Checkpoint

*Recall:*
1. ¿Qué trajo el starter `webmvc` que hace que el proceso no termine?
2. ¿Quién ejecutó el constructor de `Saludador`, y qué le indicaba que debía hacerlo?

*Decidí y justificá:*
3. Tu compañero dice "el puerto lo elige Spring, no se puede cambiar". Refutalo señalando el archivo y la línea exactos — y explicá qué pasaría si dos apps tuyas quisieran el mismo.
4. En el sondeo B, ¿por qué el orden de los mensajes NO puede ser al revés? ¿Qué te dice eso sobre cómo Spring decide el orden de construcción?

## 📝 Registro de la etapa

Una línea, la que sea: ¿qué te sorprendió, costó o hizo clic? *(Va juntándose para el complemento del final.)*

## 🔗 Conexión con la clase

Abrí el `sales-service` y mirá tres cosas: su `application.yaml` (mismo esqueleto que el tuyo, puerto 8082), `SalesServiceApplication` (idéntica a tu `VeterinariaApplication`, letra por letra salvo el nombre), y — si fuiste por la Vía B — su **pom raíz**: fijate que no hereda de `starter-parent` sino que importa el BOM por `dependencyManagement` (la variante 👀 de la Parte 1, en vivo). Todo lo que la clase construyó arranca desde este mismo punto exacto en el que estás parado ahora. Releé el recorrido **P5 §6** — primera relectura del proyecto: debería sonarte a obviedad.

## ▶️ Próximo paso

Con el sondeo en 🟢 (o reactivado por chat): decime **"arranquemos etapa 1"** y va lo bueno — tu primer endpoint, y ver con tus ojos una request **entrar** a tu app por esa puerta que Tomcat dejó abierta.

---

**FIN DE LA ETAPA 0 — v2**
