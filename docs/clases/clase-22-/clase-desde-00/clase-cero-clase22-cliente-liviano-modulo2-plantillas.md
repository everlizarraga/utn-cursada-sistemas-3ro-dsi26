# Clase desde cero — Cliente Liviano — Módulo 2
# Plantillas: mostrar datos sin repetir código

**Fase B — La herramienta** · Densidad general: 🔴 (es la sintaxis que vas a escribir en cada pantalla, y la herencia de plantillas es pregunta de examen)

---

## Sobre este documento

**Cubre:** de dónde salen los datos que llegan a la plantilla (la capa de *service*); cómo la plantilla recorre una lista, muestra u oculta partes, y arma URLs con datos adentro; cómo el controller lee un dato de la URL; y la herramienta que cierra el **hilo H1**: la herencia de plantillas, para escribir el `<head>`, la barra de navegación y el pie **una sola vez**.

**NO cubre:** formularios, es decir, cómo la persona **manda** datos al servidor (módulo 3); cómo llega un mensaje a una pantalla después de un *redirect* (también módulo 3: acá queda plantado); nada de seguridad (4 a 6); traer los datos de otro servicio por HTTP (7: acá los datos viven en memoria en el mismo proyecto, forma monolítica del módulo 1).

## De dónde venís

- **Módulo 0:** `${clave}` lee una clave del `Model`; `th:text` reemplaza el texto de una etiqueta; `@{/ruta}` arma una URL; un `@Controller` devuelve el nombre de una plantilla.
- **Módulo 1:** el controller no calcula, delega en el *service*; `redirect:` le pide al navegador que haga otro pedido; un recurso independiente tiene rutas del estilo `/alumnos`, `/alumnos/{legajo}`.
- **De clases anteriores:** DTOs y la conversión entidad ↔ DTO en el *service*; inyección de dependencias por constructor (`@RequiredArgsConstructor` de Lombok con atributos `final`); `Optional` de Java.

Todo el código de este módulo es el sistema de gestión de alumnos de la forma monolítica: alumnos guardados en memoria, en el mismo proyecto que arma las pantallas. Las clases de estilo (Bootstrap) se omiten en los ejemplos por claridad; no cambian nada de lo que se explica.

---

## 1. 🔴 De dónde salen los datos ahora: el service

En el módulo 0 el controller escribía los datos a mano (`model.addAttribute("mensaje", "El servidor de front está vivo")`). Eso no sirve para nada real. **Hilo H2**, primera parte: los datos se los pide al *service*.

```
   Navegador ──GET /alumnos──►  Controller  ──►  Service  ──►  Repositorio  ──►  entidades (en memoria)
                                    │              │ convierte a DTO
                                    │◄─── List<AlumnoDTO> ────┘
                                    │ los mete en el Model
                                    ▼
                                Thymeleaf ──► HTML
```

### 1.1 Las entidades y el repositorio, en dos líneas

Un `Alumno` tiene `legajo`, `nombre`, `apellido`, una lista de `Contacto` (cada uno con `id`, `tipoContacto` que es EMAIL o TELEFONO, y `valor`), y un método `getNombreCompleto()`. El repositorio es una clase `@Repository` con una `List<Alumno>` en memoria, dos alumnos precargados al arrancar, y los métodos `findAll()`, `findByLegajo(legajo)`, `save(alumno)`, `deleteByLegajo(legajo)`. No hay base de datos: es a propósito, para que el foco esté en las pantallas.

### 1.2 El service: lo que sale es un DTO

```java
@Service
public class AlumnoService {

    @Autowired
    private AlumnosRepository alumnosRepository;

    public List<AlumnoDTO> obtenerTodosLosAlumnos() {
        return this.alumnosRepository.findAll()      // List<Alumno>: entidades
                .stream()
                .map(this::convertirADTO)             // cada entidad → su DTO
                .toList();                            // List<AlumnoDTO>: esto es lo que sale del service
    }

    public Optional<AlumnoDTO> obtenerAlumnoPorLegajo(String legajo) {
        Alumno alumno = intentarRecuperarAlumno(legajo);   // lanza NotFoundException si no existe
        return Optional.of(convertirADTO(alumno));
    }

    private AlumnoDTO convertirADTO(Alumno alumno) {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setLegajo(alumno.getLegajo());
        dto.setNombre(alumno.getNombre());
        dto.setApellido(alumno.getApellido());
        dto.setNombreCompleto(alumno.getNombreCompleto());  // en la entidad es un método; en el DTO, un campo
        dto.setContactos(alumno.getContactos().stream().map(this::convertirContactoADTO).toList());
        return dto;
    }
    // ... convertirContactoADTO, intentarRecuperarAlumno, y los métodos de crear/actualizar/eliminar (módulo 3)
}
```

La regla, que ya conocías y acá se sostiene: **todo lo que entra y sale de un service es un DTO.** La plantilla nunca ve una entidad. Ve un `AlumnoDTO` con campos planos: `legajo`, `nombre`, `apellido`, `nombreCompleto`, `contactos`.

### 1.3 El controller: recibe, delega, arma el Model

```java
@Controller
@RequestMapping("/alumnos")               // PREFIJO para todos los métodos de esta clase:
                                          // un @GetMapping vacío responde a /alumnos,
                                          // un @GetMapping("/{legajo}") responde a /alumnos/{legajo}, etc.
@RequiredArgsConstructor                  // Lombok: genera el constructor con los atributos final → Spring inyecta
public class AlumnoController {
    private final AlumnoService alumnoService;

    @GetMapping                           // sin ruta = la del prefijo: GET /alumnos
    public String listarAlumnos(Model model) {
        List<AlumnoDTO> alumnos = alumnoService.obtenerTodosLosAlumnos();   // 1. pedir al service
        model.addAttribute("alumnos", alumnos);                              // 2. al Model: la lista
        model.addAttribute("titulo", "Listado de alumnos");                  //    y el título
        model.addAttribute("totalDeAlumnos", alumnos.size());                //    y el total
        return "alumnos/lista";           // 3. la plantilla: templates/alumnos/lista.html  ← CON la carpeta
    }
}
```

⚠️ **El `return` lleva la ruta relativa a `templates/`, carpeta incluida.** `"alumnos/lista"` → `templates/alumnos/lista.html`. Si devolvés `"lista"` a secas, Spring busca `templates/lista.html`, no lo encuentra, y explota al pedir la página. Organizar las plantillas en una carpeta por recurso (`alumnos/`, `donaciones/`) es lo que evita tener treinta archivos sueltos.

Sobre `alumnos.size()` en el controller: es un cálculo trivial y se tolera. Si fuera una estadística de verdad, la calcularía el *service* y el controller solo la pasaría. El controller **rellena el Model con lo que la vista necesita**, no procesa.

---

## 2. 🔴 Recorrer una lista: `th:each`

La plantilla recibe `alumnos` (una lista de DTOs) y tiene que armar una fila de tabla por cada uno. Para eso existe `th:each`.

`templates/alumnos/lista.html` (la parte de la tabla; el resto del archivo se ve en la sección 5):

```html
<table>
    <thead>
    <tr>
        <th>Legajo</th>
        <th>Nombre completo</th>
        <th>Contactos</th>
        <th>Acciones</th>
    </tr>
    </thead>
    <tbody>
    <!-- th:each="alumno : ${alumnos}"  →  "por cada elemento de la lista 'alumnos', llamalo 'alumno'
         y REPETÍ ESTA ETIQUETA (el <tr> completo, con todo lo que tiene adentro)". -->
    <tr th:each="alumno : ${alumnos}">

        <!-- ${alumno.legajo}: Thymeleaf llama a alumno.getLegajo(). El texto "A000" es relleno. -->
        <td th:text="${alumno.legajo}">A000</td>

        <!-- ${alumno.nombreCompleto} → alumno.getNombreCompleto(). Es el campo que el service armó. -->
        <td th:text="${alumno.nombreCompleto}">Nombre Apellido</td>

        <td>
            <!-- se completa en la sección 3 -->
        </td>
        <td>
            <!-- se completa en la sección 4 -->
        </td>
    </tr>
    </tbody>
</table>
```

```
// ¿CÓMO FUNCIONA?
//  1. El controller puso en el Model la clave "alumnos" con una lista de 2 AlumnoDTO.
//  2. Thymeleaf llega al <tr th:each="alumno : ${alumnos}">.
//  3. Toma el primer DTO, lo llama "alumno", y procesa el <tr> entero con esa variable:
//     ${alumno.legajo} → "A001", ${alumno.nombreCompleto} → "Juan Pérez".
//  4. Toma el segundo, repite: "A002", "María González".
//  5. Se acaba la lista: deja de repetir. th:each desaparece del HTML final.
//
//  Resultado esperado (lo que recibe el navegador):
//    <tr>
//        <td>A001</td>
//        <td>Juan Pérez</td>
//        ...
//    </tr>
//    <tr>
//        <td>A002</td>
//        <td>María González</td>
//        ...
//    </tr>
//  Dos <tr>, uno por alumno. Si la lista tuviera 40, serían 40. La plantilla no cambia.
```

Dos reglas de `${objeto.propiedad}`:

- **Es un getter.** `${alumno.legajo}` ejecuta `alumno.getLegajo()`. Si el DTO no tiene ese getter (o el campo se llama distinto), la página falla al renderizar con un error que menciona la propiedad. Lombok `@Data` o `@Getter` en el DTO te ahorra escribirlos.
- **Se puede encadenar.** `${alumno.contactos}` es la lista de `ContactoDTO`; si adentro de un `th:each` sobre esa lista escribís `${contacto.valor}`, es `contacto.getValor()`.

---

## 3. 🔴 Mostrar u ocultar: `th:if` y `th:unless`

¿Qué pasa si la lista está vacía? Una tabla sin filas queda fea. Mejor un mensaje. Para eso existe `th:if`: **si la condición es verdadera, la etiqueta se renderiza; si no, desaparece por completo** (no se oculta con CSS: directamente no está en el HTML).

```html
<!-- th:if="${alumnos.empty}": alumnos.isEmpty() → si es true, este div existe; si es false, no existe. -->
<div th:if="${alumnos.empty}">
    <h5>No hay alumnos registrados</h5>
    <p>Comenzá agregando el primero.</p>
    <a th:href="@{/alumnos/nuevo}">Agregar primer alumno</a>
</div>

<!-- th:unless: exactamente al revés. Si alumnos.isEmpty() es false, la tabla existe. -->
<div th:unless="${alumnos.empty}">
    <table>
        <!-- ... la tabla de la sección 2 ... -->
    </table>
</div>
```

`th:if` / `th:unless` es el par que reemplaza al `if / else` de Java dentro de la plantilla. Se usan sobre la misma condición, en dos etiquetas hermanas.

Un uso más, dentro de cada fila, para la columna "Contactos":

```html
<td>
    <!-- si el alumno no tiene contactos, este span -->
    <span th:if="${alumno.contactos.empty}">Sin contactos</span>

    <!-- si tiene, este otro, con la cantidad. #lists.size(...) es una función auxiliar de Thymeleaf
         que devuelve el tamaño de una lista (equivale a .size() en Java). -->
    <span th:unless="${alumno.contactos.empty}">
        <span th:text="${#lists.size(alumno.contactos)}">2</span> contacto(s)
    </span>
</td>
```

```
// Resultado esperado, para Juan Pérez con 2 contactos y un alumno sin ninguno:
//    <td><span>2 contacto(s)</span></td>
//    <td><span>Sin contactos</span></td>
```

> **Valor por defecto en una sola línea:** cuando una clave puede o no estar en el Model, en vez de un `th:if` se puede escribir `th:text="${mensaje} ?: 'El recurso no fue encontrado'"`. El operador `?:` devuelve la izquierda si existe, y si es nula devuelve la derecha. Es un atajo para el caso "mostrá esto o, si no hay nada, aquello".

---

## 4. 🔴 URLs con datos adentro, y cómo leerlos del otro lado

La columna "Acciones" tiene un botón "Ver" por fila, y cada uno tiene que llevar al detalle **de ese alumno**: `/alumnos/A001`, `/alumnos/A002`. La URL cambia por fila. Para armarla, `@{}` acepta **variables**:

```html
<td>
    <!-- @{/alumnos/{legajo}(legajo=${alumno.legajo})}
           ├─ /alumnos/{legajo}          la ruta con un HUECO llamado legajo
           └─ (legajo=${alumno.legajo})  con qué se llena el hueco: el legajo de ESTE alumno
         Resultado para Juan: href="/alumnos/A001". -->
    <a th:href="@{/alumnos/{legajo}(legajo=${alumno.legajo})}">Ver</a>

    <!-- mismo mecanismo, ruta de edición -->
    <a th:href="@{/alumnos/{legajo}/editar(legajo=${alumno.legajo})}">Editar</a>
</td>
```

Es explícito a propósito: declarás el hueco entre llaves y después decís con qué lo llenás. No hay concatenación de strings a mano, y `@{}` se encarga de que la URL quede bien formada.

### 4.1 Del otro lado: `@PathVariable`

Alguien hace clic en "Ver" de Juan. El navegador pide `GET /alumnos/A001`. Del lado del servidor, el controller tiene que **leer** ese `A001` de la URL:

```java
@GetMapping("/{legajo}")                  // con el prefijo de la clase: GET /alumnos/{legajo}
                                          // {legajo} es un HUECO en la ruta: matchea /alumnos/A001, /alumnos/A002, /alumnos/lo-que-sea
public String verDetalleAlumno(@PathVariable String legajo,     // @PathVariable: "el valor que cayó en el hueco {legajo},
                                                                //  dámelo en este parámetro". Para /alumnos/A001, legajo = "A001".
                               Model model) {
    try {
        AlumnoDTO alumno = alumnoService.obtenerAlumnoPorLegajo(legajo).get();   // el service busca; .get() del Optional

        model.addAttribute("alumno", alumno);                                    // UN alumno, no una lista
        model.addAttribute("titulo", "Detalle del alumno");
        model.addAttribute("tieneContactos", !alumno.getContactos().isEmpty()); // un booleano para la vista

        return "alumnos/detalle";                                                // templates/alumnos/detalle.html
    }
    catch (NotFoundException ex) {        // el service no lo encontró: lanzó esta excepción
        return "redirect:/404";           // no devolvemos vista: mandamos al navegador a pedir /404
    }
}
```

Y el detalle, `templates/alumnos/detalle.html`, usa las mismas herramientas que ya conocés, ahora sobre **un** objeto en vez de una lista:

```html
<h1 th:text="${titulo}">Detalle del alumno</h1>

<p>Legajo: <span th:text="${alumno.legajo}">A000</span></p>
<p>Nombre completo: <span th:text="${alumno.nombreCompleto}">Nombre Apellido</span></p>

<!-- tieneContactos es el booleano que el controller calculó -->
<div th:if="${tieneContactos}">
    <h3>Contactos</h3>
    <ul>
        <!-- th:each sobre la lista ANIDADA del alumno: cada contacto es un ContactoDTO -->
        <li th:each="contacto : ${alumno.contactos}">
            <span th:text="${contacto.tipoContacto}">EMAIL</span>:
            <span th:text="${contacto.valor}">alguien@mail.com</span>
        </li>
    </ul>
</div>
<p th:unless="${tieneContactos}">Este alumno no tiene contactos cargados.</p>

<a th:href="@{/alumnos/{legajo}/editar(legajo=${alumno.legajo})}">Editar</a>
<a th:href="@{/alumnos}">Volver a la lista</a>
```

```
// Resultado esperado para GET /alumnos/A001 (Juan, con un email y un teléfono):
//    <h1>Detalle del alumno</h1>
//    <p>Legajo: <span>A001</span></p>
//    <p>Nombre completo: <span>Juan Pérez</span></p>
//    <div>
//        <h3>Contactos</h3>
//        <ul>
//            <li><span>EMAIL</span>: <span>juan.perez@mail.com</span></li>
//            <li><span>TELEFONO</span>: <span>11-1234-5678</span></li>
//        </ul>
//    </div>
//    <a href="/alumnos/A001/editar">Editar</a>
//    <a href="/alumnos">Volver a la lista</a>
//
// Para GET /alumnos/ZZZ (no existe): el service lanza NotFoundException, el controller
// responde "andá a /404", el navegador pide GET /404 y ve la pantalla de no encontrado.
```

Fijate cómo el módulo 1 se cumple al pie de la letra: `Contacto` es un recurso **dependiente**, no tiene ruta propia, y se muestra **adentro** del detalle de su alumno con un `th:each` anidado.

**Un cabo abierto a propósito.** Cuando el alumno no existe, el controller redirige a `/404`. La pantalla 404 tendría que decir *"No existe el alumno ZZZ"*, pero el `redirect:` es un pedido nuevo, con un `Model` nuevo y vacío: **lo que pongas en el Model antes del redirect se pierde.** ¿Cómo se le hace llegar un mensaje a la pantalla siguiente? Se resuelve en el módulo 3. **Hilo H3 abierto** (junto con el otro: ¿y cuando la persona tiene que *cargar* datos?).

---

## 5. 🔴 Herencia de plantillas: escribir el layout una sola vez

Este es el **hilo H1**, y es la parte más importante del módulo.

### 5.1 El problema, en concreto

Con lo visto hasta acá, `lista.html`, `detalle.html`, `404.html` y cada pantalla nueva tendrían que empezar con `<!DOCTYPE html>`, el `<head>` con sus `<link>` a los CSS, una barra de navegación, y terminar con el pie de página y los `<script>`. Todo **idéntico** en todas. Con 30 pantallas, 30 copias. Cambiás un link del menú: 30 archivos.

### 5.2 La solución: un padre con un hueco, hijos que llenan el hueco

Se escribe **un solo archivo** con todo lo común y un hueco en el medio. Cada pantalla declara "yo heredo de ese archivo, y esto es lo que va en el hueco". Es como una herencia de clases: el padre define la estructura, el hijo aporta su parte.

```
   templates/layout/base.html  (EL PADRE, se escribe una vez)
   ┌──────────────────────────────────────┐
   │ <!DOCTYPE html> <html> <head>...     │
   │ <nav> ... menú ... </nav>            │
   │ <header> título </header>            │
   │ <main>                               │
   │     ┌────────────────────────┐       │
   │     │   ▓▓▓ HUECO ▓▓▓        │◄──────┼──── acá se incrusta el fragmento "contenido" del hijo
   │     └────────────────────────┘       │
   │ </main>                              │
   │ <footer> ... </footer>               │
   │ <script src="bootstrap"> ...         │
   └──────────────────────────────────────┘

   templates/alumnos/lista.html  (UN HIJO)
   ┌──────────────────────────────────────┐
   │ "heredo de layout/base"              │
   │ fragmento "contenido":               │
   │     la tabla de alumnos              │
   └──────────────────────────────────────┘
```

### 5.3 El padre: `templates/layout/base.html`

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <!-- el título de la pestaña lo aporta cada hijo vía el Model, como en el módulo 0 -->
    <title th:text="${titulo} + ' - Gestión de Alumnos'">Gestión de Alumnos</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link th:href="@{/css/style.css}" rel="stylesheet">
</head>
<body>

    <!-- th:fragment="navegacion": le pone NOMBRE a este bloque. Sirve para poder referenciarlo
         desde otro lado. Acá el nav se usa en el mismo lugar donde está, así que el nombre es
         solo documentación; en la sección 6 vas a ver un fragmento que sí se trae desde otro archivo. -->
    <nav th:fragment="navegacion">
        <a th:href="@{/alumnos}">Gestión de Alumnos</a>
        <a th:href="@{/alumnos}">Lista de alumnos</a>
        <a th:href="@{/alumnos/nuevo}">Nuevo alumno</a>
    </nav>

    <header th:fragment="header">
        <h1 th:text="${titulo}">Título</h1>
    </header>

    <main>
        <!-- EL HUECO. th:replace="${contenido} :: contenido"  →  "reemplazá este div por el fragmento
             llamado 'contenido' de la plantilla ${contenido}". Como ninguna pantalla pone una clave
             'contenido' en el Model, ese nombre de plantilla queda VACÍO, y un nombre vacío significa
             "LA PLANTILLA QUE INICIÓ EL RENDER" (el hijo, no este archivo). Ver el mecanismo en 5.5. -->
        <div th:replace="${contenido} :: contenido"></div>
    </main>

    <footer th:fragment="footer">
        <p>Sistema de Gestión de Alumnos</p>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script th:src="@{/js/common.js}"></script>
</body>
</html>
```

> **`th:fragment="nombre"`:** le pone un nombre a un bloque de HTML para que pueda ser referenciado desde otra plantilla.
> **`th:replace="~{plantilla :: nombre}"`:** "reemplazá esta etiqueta por el fragmento `nombre` de la plantilla `plantilla`". Si la parte de la plantilla queda vacía (porque se omite, `~{:: nombre}`, o porque es una variable que no existe, `${contenido} :: nombre`), se busca en la plantilla que inició el renderizado.

### 5.4 Un hijo: `templates/alumnos/lista.html`, completo

```html
<!-- LÍNEA 1: "esta plantilla se reemplaza ENTERA por el <html> de layout/base".
     El selector ":: html" apunta a la etiqueta <html> de base.html por su nombre de etiqueta;
     no hace falta que base tenga un th:fragment="html". -->
<html th:replace="~{layout/base :: html}">

<!-- LÍNEA 2: el fragmento que el padre va a incrustar en su hueco. Tiene que llamarse
     exactamente como el padre lo busca: "contenido". -->
<div th:fragment="contenido">

    <h2 th:text="${titulo}">Listado de alumnos</h2>
    <a th:href="@{/alumnos/nuevo}">Nuevo alumno</a>

    <div th:if="${alumnos.empty}">
        <p>No hay alumnos registrados.</p>
    </div>

    <div th:unless="${alumnos.empty}">
        <table>
            <thead>
            <tr><th>Legajo</th><th>Nombre completo</th><th>Contactos</th><th>Acciones</th></tr>
            </thead>
            <tbody>
            <tr th:each="alumno : ${alumnos}">
                <td th:text="${alumno.legajo}">A000</td>
                <td th:text="${alumno.nombreCompleto}">Nombre Apellido</td>
                <td>
                    <span th:if="${alumno.contactos.empty}">Sin contactos</span>
                    <span th:unless="${alumno.contactos.empty}">
                        <span th:text="${#lists.size(alumno.contactos)}">2</span> contacto(s)
                    </span>
                </td>
                <td>
                    <a th:href="@{/alumnos/{legajo}(legajo=${alumno.legajo})}">Ver</a>
                    <a th:href="@{/alumnos/{legajo}/editar(legajo=${alumno.legajo})}">Editar</a>
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <!-- un script propio de ESTA pantalla: va adentro del fragmento, así viaja con él -->
    <script th:src="@{/js/alumnos/lista-alumnos.js}"></script>
</div>
</html>
```

Eso es **todo** el archivo. Ni `<head>`, ni `<nav>`, ni `<footer>`: los aporta el padre. El hijo solo tiene lo que le es propio.

### 5.5 El mecanismo: sube al padre, baja al hijo

```
// ¿CÓMO FUNCIONA?
//  1. El controller devuelve "alumnos/lista". Thymeleaf abre templates/alumnos/lista.html.
//  2. Primera etiqueta: <html th:replace="~{layout/base :: html}">.
//     → "Reemplazame por el <html> de layout/base." Thymeleaf SUBE a base.html y empieza a procesarlo.
//  3. Procesa base.html de arriba a abajo: head, nav, header... con el MISMO Model
//     (por eso ${titulo} funciona en el padre aunque lo puso el controller de la lista).
//  4. Llega a <div th:replace="${contenido} :: contenido">. ${contenido} no está en el Model → vacío.
//     → "Reemplazame por el fragmento 'contenido' de la plantilla que inició todo esto."
//     Thymeleaf BAJA a lista.html, busca <div th:fragment="contenido">, y lo incrusta acá.
//  5. Sigue con el resto de base.html: footer, scripts.
//  6. Termina. El HTML final tiene la estructura del padre con la tabla del hijo en el medio.
//
//  Resultado esperado (esqueleto de lo que recibe el navegador):
//    <!DOCTYPE html>
//    <html lang="es">
//    <head> <title>Listado de alumnos - Gestión de Alumnos</title> ... </head>
//    <body>
//        <nav> ... </nav>
//        <header><h1>Listado de alumnos</h1></header>
//        <main>
//            <div>                                     ← el fragmento "contenido" del hijo, incrustado
//                <h2>Listado de alumnos</h2>
//                <table> ... dos filas ... </table>
//            </div>
//        </main>
//        <footer> ... </footer>
//        <script ...></script>
//    </body>
//    </html>
//  Ni un th: en ningún lado.
```

Es la misma idea que la herencia de objetos: entrás por el hijo, vas al padre a buscar la estructura, y cuando el padre necesita la parte específica, vuelve al hijo a buscarla.

⚠️ **Hay dos formas de escribir el hueco del padre y las vas a ver las dos.** `th:replace="${contenido} :: contenido"` es la del código de referencia, y es la que conviene copiar tal cual. También aparece como `th:replace="~{:: contenido}"`, que escribe la misma idea sin la variable: nombre de plantilla vacío, "buscá en la que inició el render". Son equivalentes; si te encontrás con una, ya sabés que es la otra.

⚠️ **El nombre del fragmento tiene que coincidir en los dos archivos.** Si el padre busca `contenido` y el hijo declara `th:fragment="content"`, la página falla al renderizar. Lo mismo si el hijo hereda de `~{layout/base :: html}` pero el archivo está en `templates/base.html` en vez de `templates/layout/base.html`.

### 5.6 Un segundo hijo, para ver que el padre no cambia

`templates/404.html`, la pantalla de no encontrado, hereda del mismo padre:

```html
<html th:replace="~{layout/base :: html}">
<div th:fragment="contenido">

    <h1 th:text="${titulo}">No encontrado</h1>

    <!-- th:if sobre una clave que puede NO estar en el Model: si no está, el div no existe -->
    <div th:if="${mensaje}">
        <span th:text="${mensaje}">El recurso solicitado no fue encontrado</span>
    </div>

    <p>La página o recurso que buscás no existe o fue movido.</p>
    <a th:href="@{/alumnos}">Ir a la lista de alumnos</a>
</div>
</html>
```

Mismo padre, mismo `th:replace` en la primera línea, mismo nombre de fragmento. Solo cambia lo de adentro. Ahora tenés dos pantallas con navegación y pie idénticos y cero código repetido. **Hilo H1 cerrado.**

(El `${mensaje}` de ese `th:if` es el que hoy nunca llega, porque el redirect lo pierde. Módulo 3.)

🎓 **Si te lo preguntan — ¿cómo se evita repetir el HTML común entre pantallas en un cliente liviano?**
Con herencia de plantillas: se define una plantilla base (*layout*) con la estructura común (`head`, navegación, pie) y un hueco marcado con `th:replace` hacia un fragmento; cada pantalla declara con `th:replace` que hereda de la base y define ese fragmento con `th:fragment`, aportando solo su contenido propio.

---

## 6. 🟡 Fragmentos reutilizables entre archivos

El hueco del layout es un caso particular de algo más general: **cualquier bloque con `th:fragment` puede traerse desde cualquier otra plantilla**. Sirve para dos cosas: un pedazo que se repite en varias pantallas, o una pantalla larga que conviene partir.

En la lista de alumnos hay un panel de estadísticas arriba de la tabla. Vive en su propio archivo:

`templates/alumnos/fragments/estadisticas.html`

```html
<!-- Un archivo que SOLO contiene un fragmento. No es una pantalla: nadie lo devuelve desde un controller. -->
<div th:fragment="estadisticas">
    <div>
        <h4 th:text="${totalDeAlumnos}">0</h4>    <!-- la clave la puso el controller de la lista -->
        <p>Total de alumnos</p>
    </div>
    <div>
        <h4>Sistema activo</h4>
        <p>Funcionando correctamente</p>
    </div>
</div>
```

Y en `lista.html`, adentro del fragmento `contenido`, donde tiene que aparecer:

```html
<!-- "reemplazá este div por el fragmento 'estadisticas' del archivo alumnos/fragments/estadisticas" -->
<div th:replace="~{alumnos/fragments/estadisticas :: estadisticas}"></div>
```

```
// Resultado esperado: en el lugar de ese <div> aparece el bloque de estadísticas, con el 2
// en lugar del ${totalDeAlumnos}. El fragmento ve el mismo Model que la pantalla que lo incluye.
```

Cuándo separar algo en un fragmento aparte: si lo vas a usar en más de una pantalla (una tarjeta de resumen, un bloque de alertas), o si la pantalla se vuelve tan larga que cuesta leerla. Si es una sola pantalla y es corta, no hace falta.

> 🕳️ **Madriguera — Bootstrap y los `<link>` externos**
> Bootstrap es un framework de CSS que se enlaza desde internet en el `<head>` del layout y da estilos listos (tablas, botones, grillas) con clases como `btn` o `table`. No es parte de Thymeleaf ni de Spring: es maquetado, y va donde iría en cualquier HTML. Lo mismo el `.js` de Bootstrap al final del `<body>`.
> *Volvé al camino — esto se profundiza aparte, otro día.*

> 🕳️ **Madriguera — atributos `th:data-*` y el botón de eliminar**
> En la fila de la tabla real hay un botón "Eliminar" con `th:data-legajo="${alumno.legajo}"` y un `onclick`. Thymeleaf rellena atributos `data-*` igual que cualquier otro, y un JavaScript en el navegador los lee para abrir un cuadro de confirmación. El eliminar en sí se ve en el módulo 3; el JavaScript del cuadro es lógica de cliente y queda fuera de este material.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 7. 🟢 Cuando no anda: errores de plantillas

| Lo que ves | Qué es | Qué revisar |
|---|---|---|
| *"Error resolving template [lista]"* | El `return` no tiene la carpeta | `"alumnos/lista"`, no `"lista"`. |
| *"Error resolving template [layout/base]"* | El hijo hereda de un padre que no está donde dice | El padre tiene que estar en `templates/layout/base.html`. |
| *"Error during execution of processor 'th:replace'"* con `contenido` | El padre busca un fragmento que el hijo no declara con ese nombre | Que `th:fragment="contenido"` en el hijo y el `:: contenido` del hueco en el padre digan lo mismo. |
| *"Property or field 'nombreCompleto' cannot be found"* | El DTO no tiene el getter | Que el DTO tenga el campo y `@Getter`/`@Data`, o el método `getNombreCompleto()`. |
| *"Cannot iterate over null"* o similar en `th:each` | La clave no está en el Model | El controller tiene que hacer `model.addAttribute("alumnos", ...)`. |
| El link va a `/alumnos/${alumno.legajo}` literal | Usaste `${}` donde iba `@{}` con variable | La forma es `@{/alumnos/{legajo}(legajo=${alumno.legajo})}`. |
| Ves `th:each` en el navegador | El archivo está en `static/` | A `templates/`. |

---

🧩 **En tu TP** — esto es, literalmente, el trabajo de "pasar las maquetas a plantillas", en tres pasos:

1. De una maqueta cualquiera, cortá todo lo que se repite (`<head>`, barra de navegación, pie, `<script>` de Bootstrap) y pegalo en `templates/layout/base.html` con el hueco `~{:: contenido}` en el medio. Una vez.
2. Cada maqueta queda reducida a `<html th:replace="~{layout/base :: html}">` + `<div th:fragment="contenido">` + lo suyo. Lo que antes eran 200 líneas por archivo pasa a ser 40.
3. Donde la maqueta tenía datos escritos a mano ("Juan Pérez", "3 donaciones"), van `th:text` y `th:each`, leyendo del Model. Los datos hardcodeados de prueba desaparecen: los pone el controller (hoy desde un service en memoria, en el módulo 7 desde los servicios reales).

Si tus maquetas ya separaban "componentes" (una barra, una tarjeta) en su propio CSS, ya pensaste en fragmentos sin saberlo: cada uno de esos es candidato a `th:fragment` en un archivo aparte.

---

## Checkpoint del módulo 2

Sin respuestas.

1. Un controller devuelve `"lista"` y la plantilla está en `templates/alumnos/lista.html`. ¿Qué pasa?
2. ¿Qué hace exactamente `th:each="alumno : ${alumnos}"` y sobre qué etiqueta actúa?
3. ¿Qué ejecuta Thymeleaf cuando escribís `${alumno.nombreCompleto}`? ¿Qué pasa si el DTO no tiene ese getter?
4. ¿Cuál es la diferencia entre `th:if` y esconder algo con CSS? ¿Qué queda en el HTML final en cada caso?
5. Escribí de memoria el `th:href` que lleva al detalle del alumno de cada fila. ¿Por qué no se concatena a mano?
6. ¿Qué hace `@PathVariable` y de dónde saca el valor?
7. Explicá el mecanismo de herencia de plantillas con las palabras "sube" y "baja". ¿Qué archivo tiene el hueco y qué archivo tiene el fragmento?
8. ¿Qué diferencia hay entre `~{layout/base :: html}` y `~{:: contenido}`? ¿Qué significa que en el segundo no haya nombre de plantilla?
9. ¿Cuándo conviene sacar un bloque a un archivo de fragmentos aparte, y cuándo no?
10. Un alumno no existe, el controller hace `redirect:/404`, y la pantalla 404 no muestra el mensaje aunque el controller lo puso en el Model. ¿Por qué se pierde?

---

## Qué viene en el módulo 3

Hasta acá la persona solo **mira**. En el módulo 3 **carga**: un formulario de alta de alumno viaja al servidor con `POST`, el servidor lo valida, y si algo está mal devuelve **la misma pantalla con los errores marcados y lo que la persona ya escribió**, sin que se pierda nada. Se cierra el **hilo H3** completo: el `th:object` / `th:field` que enlaza el formulario con un DTO, el `BindingResult` que transporta los errores, y los **atributos flash**, que son la respuesta a la pregunta 10: cómo hacer que un mensaje sobreviva a un redirect.

---

**FIN DEL MÓDULO 2**
