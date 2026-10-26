# 🌱 Proyecto 1 — Etapa 1: El arranque (correr Spring por primera vez)

> **Objetivo:** correr tu Spring Boot vacío, ver el log y el banner con tus ojos, y a partir de **lo que observás**, entender qué es Spring realmente, qué hace `SpringApplication.run(...)`, y por qué la app se comporta como se comporta.
>
> **La forma de trabajar acá:** primero corrés. Después observás. Recién después entendemos lo que viste. En ese orden.
>
> **Pre-requisito:** Etapa 0 completa (proyecto creado, sin dependencias extra).
>
> **Tiempo estimado:** 30-40 minutos.

---

## 🧭 Mapa de esta etapa

1. Correr la app (la acción).
2. Observar el log — qué apareció.
3. **La sorpresa:** ¿por qué la app terminó sola?
4. Qué es Spring realmente (a partir de lo que viste).
5. Qué hace `SpringApplication.run(...)`.
6. Qué es `@SpringBootApplication` (vista de alto nivel, sin abrumar).
7. Experimentos para fijar lo aprendido.
8. Checkpoint.

---

## 🚀 Parte 1: Corré la app

No vamos a tocar ni una línea de código todavía. Solo correr lo que el Initializr generó.

### Forma A: Desde IntelliJ (la más simple)

1. Abrí `PaisesApplication.java`.
2. Al lado del método `main` (o al lado del nombre de la clase) vas a ver un **triángulo verde** ▶️.
3. Click en el triángulo → **Run 'PaisesApplication'**.

### Forma B: Desde la consola

Si preferís la terminal (en la raíz del proyecto):

```bash
# Linux / Mac / Git Bash:
./mvnw spring-boot:run

# Windows (cmd / PowerShell):
mvnw.cmd spring-boot:run
```

**Corré la app ahora.** Después seguí leyendo.

---

## 👀 Parte 2: Observá el log

En la consola (abajo en IntelliJ, o en tu terminal) apareció algo parecido a esto:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.0.5)

2026-06-19T10:30:15 - Starting PaisesApplication using Java 21
2026-06-19T10:30:15 - No active profile set, falling back to 1 default profile: "default"
2026-06-19T10:30:16 - Started PaisesApplication in 0.842 seconds (process running for 1.1)

Process finished with exit code 0
```

(Los detalles exactos —fechas, tiempos— van a variar, pero la estructura es esa.)

### Qué cosas mirar acá

**1. El banner ASCII de Spring Boot.**
Ese dibujito raro con `Spring Boot` y la versión `(v4.0.5)`. Es solo decorativo — Spring Boot lo imprime al arrancar para que sepas que arrancó. (Se puede personalizar o quitar, pero no importa ahora.)

**2. `Starting PaisesApplication using Java 21`.**
Spring te avisa que está arrancando tu app, y con qué versión de Java.

**3. `Started PaisesApplication in 0.842 seconds`.**
Spring te avisa que **terminó de arrancar**. Todo lo que tenía que inicializar, lo inicializó. En menos de un segundo.

**4. `Process finished with exit code 0`.**
**Esto es lo más interesante, y donde quiero que frenes.** La app **terminó**. El proceso se cerró. `exit code 0` significa "terminó bien, sin errores".

---

## 🤔 Parte 3: La sorpresa — ¿por qué la app terminó sola?

Acá viene la primera lección importante, y sale de lo que **observaste**.

Tu app **arrancó y terminó casi inmediatamente.** No quedó "viva" esperando nada. Apareció el log, dijo "Started", y se cerró.

**¿Es eso un error? ¿Hiciste algo mal?**

**No. Está perfecto. Y entender POR QUÉ es la clave de toda esta etapa.**

### El razonamiento

Pensá qué le pediste a esta app que haga:

- ¿Le pediste que escuche pedidos web? **No.**
- ¿Le pediste que procese un archivo? **No.**
- ¿Le pediste que haga algún trabajo? **No.**

**No le pediste que haga NADA.** Entonces Spring:

1. Arrancó su "contenedor" interno (ya vamos a ver qué es eso).
2. Buscó si había algo que hacer.
3. No encontró nada.
4. **Terminó.** Como cualquier programa Java que llega al final de su `main` y no tiene más nada que hacer.

> **Es exactamente como tu Proyecto 0.** Si tu `Main.java` del Proyecto 0 tuviera el método `main` vacío, arrancaría y terminaría al instante. Acá pasa lo mismo: Spring arranca, no hay tarea, termina.

### La lección grande: Spring NO es un servidor web

Acá está el mito más común sobre Spring, y lo acabás de **desmentir con tus ojos**:

> **Mucha gente cree que "Spring = servidor web". ES FALSO.**

Spring por sí solo **no es un servidor**. No escucha en ningún puerto. No espera pedidos HTTP. Lo que acabás de correr es Spring **puro**, y como ves, **arranca y termina** como cualquier programa común.

**¿Y entonces de dónde sale eso de que las apps Spring "quedan vivas escuchando en el puerto 8080"?**

De que normalmente se le **agrega** un servidor web (con una dependencia llamada "starter web"). Ese servidor web es lo que mantiene la app viva escuchando pedidos. **Pero es algo que se suma — no es Spring en sí.**

Como nosotros creamos el proyecto **sin** esa dependencia (a propósito), tu app es Spring **pelado**: arranca, no tiene servidor, no tiene tarea, termina.

> **Esto es enorme para tu modelo mental.** Spring es **un contenedor de objetos** (ya vamos a ver qué significa). El servidor web es **opcional**, se agrega aparte. Separar estas dos ideas te va a ahorrar muchísima confusión más adelante.

---

## 🧠 Parte 4: Entonces, ¿qué es Spring realmente?

Ahora que viste arrancar Spring pelado, puedo darte la definición — y va a tener sentido porque ya lo viste en acción.

**Spring es un "contenedor de objetos".**

Esa es la esencia. Vamos a desarmarlo:

### El problema que Spring resuelve

En el Proyecto 0, vos creabas objetos con `new`:

```java
CatalogoPaises catalogo = new CatalogoPaises();
```

Vos controlabas: cuándo se crea, con qué, en qué orden. Para un proyecto chico, perfecto.

Pero en proyectos grandes, los objetos dependen unos de otros formando una red. El objeto A necesita un B, que necesita un C, que necesita config, que necesita... y crear todo eso a mano, en el orden correcto, conectándolo, se vuelve un infierno.

### Lo que hace Spring

Spring dice: **"dame tus clases, marcadas con annotations especiales, y YO me encargo de crear los objetos, en el orden correcto, conectándolos entre sí."**

Vos dejás de hacer `new`. Spring lo hace por vos. Esos objetos que Spring crea y administra viven adentro de un **"contenedor"** (técnicamente se llama *ApplicationContext*).

```
   Spring Boot arranca
         ↓
   Crea el CONTENEDOR (ApplicationContext)
         ↓
   Busca tus clases marcadas con annotations
         ↓
   Crea los objetos y los mete en el contenedor
         ↓
   Los conecta entre sí (los que se necesitan mutuamente)
         ↓
   Listo. El contenedor está armado.
```

**Eso es lo que pasó en menos de un segundo cuando corriste la app.** Spring armó su contenedor. Solo que como no le diste ninguna clase para administrar (el proyecto está vacío), el contenedor quedó vacío, no había nada que hacer, y terminó.

### En una frase

> **Spring es una herramienta que crea y conecta tus objetos por vos, para que no tengas que hacer `new` ni cablear todo a mano.**

Esa idea de "el framework crea y conecta los objetos en vez de vos" tiene un nombre técnico que vas a escuchar mucho: **Inversión de Control (IoC)**. Pero no te quedes con el término ahora — quedate con la **idea**: vos declarás qué clases querés, Spring las instancia y las conecta. **En la Etapa 2 vas a VER esto pasar** cuando creemos tu primer objeto administrado por Spring.

---

## 🔍 Parte 5: Qué hace `SpringApplication.run(...)`

Abrí `PaisesApplication.java` y mirá el `main`:

```java
public static void main(String[] args) {
    SpringApplication.run(PaisesApplication.class, args);
}
```

Ahora que entendés qué es Spring, esta línea tiene sentido.

**`SpringApplication.run(...)` es el botón de arranque.** Es la línea que dispara TODO lo que viste:

```
SpringApplication.run(PaisesApplication.class, args)
         ↓
1. Crea el contenedor (ApplicationContext).
2. Lee tu configuración.
3. Busca tus clases con annotations.
4. Crea y conecta los objetos.
5. Arranca lo que haya que arrancar (servidor web si lo hubiera — acá no hay).
6. Devuelve el control.
```

**Es la traducción de "Spring, arrancá y hacete cargo de todo".**

### Comparalo con tu Proyecto 0

| Proyecto 0 | Proyecto 1 (Spring) |
|---|---|
| `main` con tu lógica directa | `main` con `SpringApplication.run(...)` |
| Vos hacías `new` de tus objetos | Spring los crea por vos |
| Vos controlabas el flujo | Spring controla el arranque |

**El `main` sigue siendo el `main` de siempre.** La diferencia es que en vez de poner tu lógica ahí, le decís a Spring "arrancá vos" con esa única línea. Spring toma las riendas.

### El argumento `PaisesApplication.class`

¿Por qué le pasás `PaisesApplication.class`? Para decirle a Spring **"empezá a buscar clases desde acá"**. Spring usa esa clase como **punto de partida** para escanear tu proyecto buscando objetos que administrar. (Esto se vuelve relevante en la Etapa 2 — por ahora solo registralo.)

---

## 🏷️ Parte 6: Qué es `@SpringBootApplication`

Arriba de la clase está esta annotation:

```java
@SpringBootApplication
public class PaisesApplication {
```

**Es la annotation que marca esta clase como "el punto de entrada de una app Spring Boot".**

Por dentro, `@SpringBootApplication` activa tres comportamientos. Te los nombro **solo para que sepas que existen** — no necesitás entenderlos en profundidad ahora, cada uno se va a aclarar solo cuando lo usemos:

1. **Escanear tu proyecto buscando clases para administrar.** → Esto lo vas a ENTENDER en la **Etapa 2**, cuando creemos la primera clase y veas a Spring encontrarla.

2. **Autoconfiguración:** Spring mira qué dependencias tenés y configura cosas automáticamente. → Esto lo vas a ENTENDER más adelante, cuando agreguemos dependencias y veas la diferencia.

3. **Marcar esta clase como fuente de configuración.** → Relevante en la Etapa 4.

> **No te pido que memorices estos tres puntos.** Solo que sepas que `@SpringBootApplication` "enciende" Spring Boot. Cada una de sus partes se va a volver concreta cuando la usemos. **Por ahora: `@SpringBootApplication` = "esta es una app Spring Boot, arrancá el framework".** Suficiente.

---

## 🧪 Parte 7: Experimentos para fijar lo aprendido

Hacé estos para que el aprendizaje se grabe. La idea es **observar**, no solo leer.

### Experimento 1: Volvé a correr y cronometrá

Corré la app de nuevo. Mirá la línea `Started PaisesApplication in X seconds`. Fijate cuánto tarda. Es rapidísimo (menos de 1-2 segundos) porque no hay casi nada que inicializar.

> Guardá ese número en la cabeza. Más adelante, cuando agreguemos un servidor web y más cosas, vas a ver que el arranque tarda más. Esa diferencia te va a mostrar "cuánto trabajo extra" implica cada pieza que sumamos.

### Experimento 2: Comprobá que el main termina

Modificá temporalmente el `main` agregando un `println` **después** del `run`:

```java
public static void main(String[] args) {
    SpringApplication.run(PaisesApplication.class, args);
    System.out.println(">>> El main llegó hasta acá y la app va a terminar");
}
```

Corré. Vas a ver que tu mensaje **se imprime**, y después la app termina:

```
...
Started PaisesApplication in 0.8 seconds
>>> El main llegó hasta acá y la app va a terminar

Process finished with exit code 0
```

**¿Qué te dice esto?** Que `SpringApplication.run(...)` **NO bloquea** — arranca Spring, vuelve, y tu `main` sigue ejecutando la línea siguiente, llega al final, y termina. **Confirma que esta app no queda viva.** Cuando agreguemos un servidor web más adelante, vas a ver que el comportamiento cambia: el `println` igual se imprime, pero la app **NO termina** porque el servidor la mantiene viva. Compararlo va a ser revelador.

> Después de este experimento, **borrá el `println`** para dejar el `main` limpio.

### Experimento 3: Corré desde consola

Si corriste desde IntelliJ, ahora probá desde la terminal:

```bash
./mvnw spring-boot:run
```

Vas a ver el mismo banner y log, pero en tu terminal. Es **la misma app**, corrida de otra forma. Esto te muestra que la app no depende de IntelliJ — es un programa Java normal que se puede correr de varias maneras.

> Para detenerla desde consola (si quedara viva, que en este caso no): `Ctrl + C`. Como esta app termina sola, no hace falta.

### Experimento 4: Mirá el log con atención

Corré de nuevo y leé **cada línea** del log despacio. Tratá de explicarte a vos mismo qué dice cada una:
- El banner → "Spring Boot arrancando".
- `Starting PaisesApplication` → "empieza a arrancar".
- `No active profile set` → (esto es sobre "perfiles de configuración", lo vemos en la Etapa 5, ignoralo por ahora).
- `Started PaisesApplication in X seconds` → "terminó de arrancar".
- `Process finished` → "terminó".

---

## ✅ Criterios de "Etapa 1 completa"

- [ ] Corriste la app al menos una vez desde IntelliJ.
- [ ] Viste el banner de Spring Boot y el log de arranque.
- [ ] Observaste que la app **arranca y termina sola** (exit code 0).
- [ ] Entendés **por qué** termina (no hay servidor web, no hay tarea).
- [ ] Entendés que **Spring NO es un servidor web** — es un contenedor de objetos.
- [ ] Hiciste el Experimento 2 (el `println` después del run) y viste que el main termina.
- [ ] Corriste la app desde la consola con `./mvnw spring-boot:run` al menos una vez.
- [ ] Borraste el `println` de prueba.

---

## ✅ Checkpoint

Si podés contestar estas mentalmente, estás listo para la Etapa 2:

1. ¿Por qué tu app arrancó y terminó sola en vez de quedar viva?
2. ¿Spring es un servidor web? ¿De dónde sale la idea de que las apps Spring "escuchan en el puerto 8080"?
3. En una frase, ¿qué es Spring?
4. ¿Qué hace `SpringApplication.run(...)`?
5. ¿`SpringApplication.run(...)` bloquea el `main` o lo deja seguir? ¿Cómo lo comprobaste?
6. ¿Qué hace `@SpringBootApplication` a grandes rasgos? (No hace falta el detalle de sus partes.)
7. ¿En qué se parece y en qué se diferencia este `main` del `main` de tu Proyecto 0?

---

## 🎯 Una reflexión antes de seguir

Fijate lo que lograste en esta etapa: **viste Spring arrancar pelado** y, a partir de eso, entendiste que Spring **no es magia ni es un servidor** — es una herramienta que crea y administra objetos por vos.

También viste algo que casi nadie te explica: que una app Spring **puede arrancar y terminar como cualquier programa**, porque el "quedarse vivo escuchando" es una pieza opcional que se agrega aparte.

Ahora tenés la base correcta. En la próxima etapa, **vamos a darle a Spring su primera clase para administrar** y vas a VER, con un `println`, cómo Spring crea ese objeto **sin que vos hagas `new`**. Ahí es donde el concepto de "contenedor de objetos" deja de ser palabras y se vuelve algo que ves pasar.

---

## ▶️ Próximo paso

Cuando hayas corrido la app, hecho los experimentos, y los checkpoints te cierren, decime **"arranquemos etapa 2"**.

En la Etapa 2 vas a:
- Crear tu primera clase administrada por Spring (con `@Component`).
- Hacer que imprima algo cuando Spring la crea.
- **Comprobar con tus ojos que Spring la instancia sin que vos hagas `new`.**
- Entender qué es un "bean".

Si algo de esta etapa no te cerró, preguntá antes de avanzar. **No acumules dudas.**
