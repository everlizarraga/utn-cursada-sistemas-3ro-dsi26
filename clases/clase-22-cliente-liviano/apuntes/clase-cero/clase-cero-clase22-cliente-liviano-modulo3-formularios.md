# Clase desde cero — Cliente Liviano — Módulo 3
# Formularios: cargar datos, validarlos, devolver errores

**Fase B — La herramienta** · Densidad general: 🔴 (todo lo que sea "la persona carga algo" en el TP pasa por acá, y el par `BindingResult` / atributos flash es material de examen)

---

## Sobre este documento

**Cubre:** cómo un formulario HTML viaja al servidor y por qué solo puede hacerlo con `GET` o `POST`; cómo se enlaza un formulario con un DTO (`th:object`, `th:field`); cómo el controller recibe ese DTO armado (`@ModelAttribute`); dónde se valida y cómo se devuelven los errores a la misma pantalla sin perder lo escrito (`BindingResult`, `th:errors`); y los **atributos flash**, que cierran el cabo del módulo 2: cómo un mensaje sobrevive a un `redirect`. Con eso se cierra el **hilo H3** completo.

**NO cubre:** quién es la persona que está cargando el formulario ni si tiene permiso (4, 5, 6); mandar lo cargado a otro servicio por HTTP (7: acá el service guarda en memoria). El JavaScript que agrega filas de contacto en el formulario queda como madriguera: es lógica del navegador.

## De dónde venís

- **Módulo 2:** `th:each`, `th:if`/`th:unless`, `@{}` con variables, `@PathVariable`, herencia de plantillas, y el cabo abierto: el `Model` se pierde en un `redirect`.
- **Módulo 1:** `redirect:` = "navegador, hacé otro pedido"; las acciones que modifican van por `POST` a rutas de acción.
- **De clases anteriores:** en una API REST, crear es `POST`, modificar es `PUT` y borrar es `DELETE`. Acá vas a ver por qué eso cambia. También: excepciones propias (`RuntimeException` con un mensaje) y `Optional`.

---

## 1. 🔴 Los formularios HTML solo hacen `GET` y `POST`

Primer choque con lo que sabías de REST. En una API, modificar un alumno es `PUT /alumnos/A001` y borrarlo es `DELETE /alumnos/A001`. Un formulario HTML **no puede** hacer ninguna de esas dos cosas: la etiqueta `<form>` solo admite `method="get"` y `method="post"`. No hay `PUT` ni `DELETE` en un formulario, y no es una limitación de Thymeleaf ni de Spring: es HTML.

Consecuencia: en un cliente liviano, **toda acción que modifica algo se transforma en un `POST` a una ruta que dice qué acción es.**

| Acción | En una API REST (lo que sabías) | En un cliente liviano (lo que hacés ahora) |
|---|---|---|
| Mostrar el formulario de alta | — | `GET /alumnos/nuevo` |
| Crear | `POST /alumnos` | `POST /alumnos/crear` |
| Mostrar el formulario de edición | — | `GET /alumnos/{legajo}/editar` |
| Modificar | `PUT /alumnos/{legajo}` | `POST /alumnos/{legajo}/actualizar` |
| Borrar | `DELETE /alumnos/{legajo}` | `POST /alumnos/{legajo}/eliminar` |

Y un patrón que se repite siempre: **un `GET` muestra el formulario, un `POST` lo procesa.** Son dos métodos distintos del controller, dos rutas distintas. El `GET` prepara la pantalla vacía (o cargada, si es edición); el `POST` recibe lo que la persona escribió.

🎓 **Si te lo preguntan — ¿por qué en un cliente liviano no se usan `PUT` ni `DELETE`?**
Porque los formularios HTML solo soportan los métodos `GET` y `POST`. Las operaciones de modificación y eliminación se implementan como `POST` a rutas de acción explícitas (`/actualizar`, `/eliminar`), mientras que `GET` se reserva para mostrar pantallas y formularios.

---

## 2. 🔴 El formulario de alta: enlazar la pantalla con un DTO

### 2.1 El `GET` que muestra el formulario

```java
@GetMapping("/nuevo")                                   // GET /alumnos/nuevo
public String mostrarFormularioCrear(Model model) {
    model.addAttribute("alumno", new AlumnoDTO());      // un DTO VACÍO bajo la clave "alumno":
                                                        // es el objeto al que el formulario se va a "pegar"
    model.addAttribute("titulo", "Crear nuevo alumno");
    return "alumnos/crear";
}
```

Lo raro es esa primera línea: ¿para qué poner un DTO vacío en el Model si no hay nada que mostrar? Porque el formulario **no se escribe campo por campo suelto**: se declara enlazado a un objeto, y cada `<input>` se enlaza a un campo de ese objeto. Para el alta, el objeto está vacío; para la edición (sección 6), viene cargado. La plantilla es la misma idea en los dos casos.

### 2.2 La plantilla: `th:object` y `th:field`

`templates/alumnos/crear.html` (hereda del layout como todas; solo el fragmento `contenido`):

```html
<html th:replace="~{layout/base :: html}">
<div th:fragment="contenido">

    <h2 th:text="${titulo}">Crear nuevo alumno</h2>
    <a th:href="@{/alumnos}">Volver a la lista</a>

    <!-- th:action="@{/alumnos/crear}": a dónde viaja el formulario al apretar "enviar".
         th:object="${alumno}": "este formulario está enlazado al objeto 'alumno' del Model".
         method="post": la única opción para una acción que modifica. -->
    <form th:action="@{/alumnos/crear}" th:object="${alumno}" method="post">

        <label for="legajo">Legajo *</label>
        <!-- th:field="*{legajo}": "este input es el campo 'legajo' DEL OBJETO de th:object".
             *{...} es la expresión relativa al objeto del formulario (a diferencia de ${...}, que
             busca en el Model). th:field genera solo tres atributos: id, name y value. -->
        <input type="text" th:field="*{legajo}" placeholder="Ej: A003" required>

        <label for="nombre">Nombre *</label>
        <input type="text" th:field="*{nombre}" placeholder="Ej: Juan" required>

        <label for="apellido">Apellido *</label>
        <input type="text" th:field="*{apellido}" placeholder="Ej: Pérez" required>

        <!-- los contactos van en la sección 7 -->

        <a th:href="@{/alumnos}">Cancelar</a>
        <button type="submit">Crear alumno</button>
    </form>
</div>
</html>
```

```
// ¿CÓMO FUNCIONA? (el GET, con el DTO vacío)
//  1. GET /alumnos/nuevo → el controller pone un AlumnoDTO vacío bajo "alumno" y devuelve la vista.
//  2. Thymeleaf ve th:object="${alumno}" y lo toma como "el objeto del formulario".
//  3. Cada th:field="*{legajo}" lo traduce a: id="legajo" name="legajo" value="<lo que tenga alumno.legajo>".
//     Como el DTO está vacío, value="".
//
//  Resultado esperado (lo que recibe el navegador):
//    <form action="/alumnos/crear" method="post">
//        <label for="legajo">Legajo *</label>
//        <input type="text" id="legajo" name="legajo" value="" placeholder="Ej: A003" required>
//        <label for="nombre">Nombre *</label>
//        <input type="text" id="nombre" name="nombre" value="" placeholder="Ej: Juan" required>
//        ...
//    </form>
//  Fijate el name="legajo": ese nombre es lo que viaja al servidor cuando se envía. Sale del *{legajo}.
```

Ese `name` es la clave de todo lo que sigue: cuando la persona aprieta "Crear alumno", el navegador manda un `POST /alumnos/crear` con el cuerpo `legajo=A003&nombre=Juan&apellido=Pérez`. Los nombres de los campos son exactamente los nombres de los atributos del DTO. **Eso no es casualidad: es lo que hace posible el paso siguiente.**

> 🕳️ **Madriguera — el atributo `required` y otras validaciones del navegador**
> `required`, `type="email"`, `maxlength` y similares son validaciones que hace **el navegador antes de enviar**. Sirven de ayuda, pero cualquiera las puede saltear (desde Postman no existen). La validación que vale es la del servidor, sección 4.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 3. 🔴 Recibir el `POST`: Spring arma el DTO solo

```java
@PostMapping("/crear")                                          // POST /alumnos/crear
public String crearAlumno(@ModelAttribute("alumno") AlumnoDTO alumnoDTO,   // ← ver abajo
                          BindingResult bindingResult,          // ← sección 4. Tiene que ir JUSTO después del DTO.
                          Model model,
                          RedirectAttributes redirectAttributes) {  // ← sección 5
    try {
        AlumnoDTO alumnoCreado = alumnoService.crearAlumno(alumnoDTO);   // el service valida y guarda

        redirectAttributes.addFlashAttribute("mensaje", "Alumno creado exitosamente");   // sección 5
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/alumnos/" + alumnoCreado.getLegajo();  // éxito: al detalle del recién creado
    }
    // ... los catch van en la sección 4
}
```

**`@ModelAttribute("alumno") AlumnoDTO alumnoDTO`** es la contraparte del `th:object`. Le dice a Spring: *"del cuerpo del `POST`, tomá cada campo por su `name`, buscá en `AlumnoDTO` el atributo con ese nombre, y llenalo. Dame el DTO armado. Y de paso, ponelo en el Model bajo la clave `alumno`."*

```
   Lo que manda el navegador          Lo que Spring construye
   ─────────────────────────          ───────────────────────
   legajo=A003            ───────►    alumnoDTO.setLegajo("A003")
   nombre=Juan            ───────►    alumnoDTO.setNombre("Juan")
   apellido=Pérez         ───────►    alumnoDTO.setApellido("Pérez")
                                      → un AlumnoDTO listo, sin que escribas ni un getParameter
```

Por eso los `name` del formulario y los atributos del DTO tienen que coincidir, y por eso `th:field` los genera a partir del DTO: para que no puedan no coincidir.

Con el DTO armado, el controller hace lo de siempre: **delega en el service**. Si el service lo crea, el controller redirige al detalle del alumno nuevo (`GET /alumnos/A003`, que ya conocés del módulo 2).

> 🕳️ **Madriguera — por qué redirigir después de un `POST` exitoso**
> Si el `POST` devolviera directamente la pantalla de detalle, el navegador quedaría "parado" en un `POST`, y apretar F5 volvería a enviar el formulario (creando el alumno dos veces). Redirigir a un `GET` evita eso. Es una práctica general de aplicaciones web, no algo de Spring.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 4. 🔴 Cuando algo está mal: validar y devolver los errores

### 4.1 Dónde se valida: en el service

El service es el dueño de las reglas. Antes de guardar, revisa; si algo no cierra, **lanza una excepción con los errores adentro**.

```java
public AlumnoDTO crearAlumno(AlumnoDTO alumnoDTO) {
    validarDatosBasicos(alumnoDTO);         // legajo, nombre, apellido obligatorios
    validarContactos(alumnoDTO);            // sección 7
    validarDuplicidadDeAlumno(alumnoDTO);   // el legajo no puede repetirse

    Alumno alumno = convertirDTOAEntity(alumnoDTO);
    alumnosRepository.save(alumno);
    return convertirADTO(alumno);
}

private void validarDatosBasicos(AlumnoDTO alumnoDTO) {
    ValidationException validationException = new ValidationException("Errores de validación");
    boolean tieneErrores = false;

    if (alumnoDTO.getLegajo() == null || alumnoDTO.getLegajo().trim().isEmpty()) {
        validationException.addFieldError("legajo", "El legajo es obligatorio");   // CAMPO + mensaje
        tieneErrores = true;
    }
    if (alumnoDTO.getNombre() == null || alumnoDTO.getNombre().trim().isEmpty()) {
        validationException.addFieldError("nombre", "El nombre es obligatorio");
        tieneErrores = true;
    }
    if (alumnoDTO.getApellido() == null || alumnoDTO.getApellido().trim().isEmpty()) {
        validationException.addFieldError("apellido", "El apellido es obligatorio");
        tieneErrores = true;
    }

    if (tieneErrores) {
        throw validationException;          // se acumulan TODOS los errores y se lanzan juntos:
    }                                       // la persona ve todo lo que está mal de una vez, no de a uno
}

private void validarDuplicidadDeAlumno(AlumnoDTO alumnoDTO) {
    Optional<Alumno> existente = alumnosRepository.findByLegajo(alumnoDTO.getLegajo().trim());
    if (existente.isPresent()) {
        throw new DuplicateLegajoException(alumnoDTO.getLegajo().trim());   // "El legajo A001 ya existe"
    }
}
```

La excepción que transporta errores por campo:

```java
public class ValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors = new HashMap<>();   // campo → mensaje

    public ValidationException(String message) { super(message); }

    public void addFieldError(String field, String error) { fieldErrors.put(field, error); }
    public boolean hasFieldErrors() { return !fieldErrors.isEmpty(); }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
```

Es un mapa `campo → mensaje`. `"legajo" → "El legajo es obligatorio"`. Ese formato es el que la pantalla necesita para marcar **cada input** con **su** error.

### 4.2 Cómo llegan los errores a la pantalla: `BindingResult`

`BindingResult` es un objeto que Spring crea junto con el DTO del `@ModelAttribute`. Es el **contenedor de errores del formulario**: cada error va asociado a un campo, y la plantilla sabe leerlo. Los `catch` del controller lo llenan:

```java
@PostMapping("/crear")
public String crearAlumno(@ModelAttribute("alumno") AlumnoDTO alumnoDTO,
                          BindingResult bindingResult,     // ⚠️ SIEMPRE el parámetro inmediatamente
                                                           //    posterior al @ModelAttribute. Si ponés
                                                           //    Model en el medio, Spring no lo asocia
                                                           //    al DTO y explota al pedir la página.
                          Model model,
                          RedirectAttributes redirectAttributes) {
    try {
        AlumnoDTO alumnoCreado = alumnoService.crearAlumno(alumnoDTO);
        redirectAttributes.addFlashAttribute("mensaje", "Alumno creado exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/alumnos/" + alumnoCreado.getLegajo();
    }
    catch (DuplicateLegajoException ex) {
        // rejectValue(campo, código, mensaje): "el campo 'legajo' tiene este error".
        bindingResult.rejectValue("legajo", "error.legajo", ex.getMessage());
        model.addAttribute("titulo", "Crear nuevo alumno");
        return "alumnos/crear";                 // LA MISMA VISTA. No redirect. Ver por qué abajo.
    }
    catch (ValidationException e) {
        convertirValidationExceptionABindingResult(e, bindingResult);   // pasa el mapa al BindingResult
        model.addAttribute("titulo", "Crear nuevo alumno");
        return "alumnos/crear";
    }
    catch (Exception e) {
        model.addAttribute("error", "Error al crear el alumno: " + e.getMessage());   // error general, sin campo
        model.addAttribute("titulo", "Crear nuevo alumno");
        return "alumnos/crear";
    }
}

private void convertirValidationExceptionABindingResult(ValidationException e, BindingResult bindingResult) {
    if (e.hasFieldErrors()) {
        // por cada (campo, mensaje) del mapa, un rejectValue
        e.getFieldErrors().forEach((field, error) -> bindingResult.rejectValue(field, "error." + field, error));
    }
}
```

**Por qué en el error se devuelve la misma vista y no un `redirect`:** porque el `Model` de esta request tiene dos cosas que hay que conservar: el `alumnoDTO` con lo que la persona escribió (Spring lo puso bajo `"alumno"` gracias al `@ModelAttribute`) y el `BindingResult` con los errores. Un `redirect` es un pedido nuevo con un `Model` nuevo: los dos se perderían. Devolviendo `"alumnos/crear"` directamente, la plantilla se renderiza con ese mismo `Model`, y pasan dos cosas a la vez:

1. `th:field="*{legajo}"` genera `value="A001"`: **el input se rellena con lo que la persona escribió**, porque el objeto del `th:object` ahora es el DTO recibido, no uno vacío.
2. La plantilla puede preguntar si ese campo tiene errores.

### 4.3 Mostrar el error al lado del campo

De vuelta en `crear.html`, debajo de cada input:

```html
<label for="legajo">Legajo *</label>
<input type="text" th:field="*{legajo}" placeholder="Ej: A003" required>

<!-- #fields.hasErrors('legajo'): "¿el BindingResult tiene errores para el campo 'legajo'?"
     Si no, este div no existe (th:if). -->
<div th:if="${#fields.hasErrors('legajo')}" class="text-danger">
    <!-- th:errors="*{legajo}": escribe el/los mensajes de error de ese campo -->
    <small th:errors="*{legajo}">Error en legajo</small>
</div>

<label for="nombre">Nombre *</label>
<input type="text" th:field="*{nombre}" placeholder="Ej: Juan" required>
<div th:if="${#fields.hasErrors('nombre')}" class="text-danger">
    <small th:errors="*{nombre}">Error en nombre</small>
</div>

<!-- y un bloque para el error general, que no es de ningún campo -->
<div th:if="${error}" class="alert alert-danger" th:text="${error}"></div>
```

```
// ¿CÓMO FUNCIONA? (el POST con un legajo que ya existe)
//  1. La persona escribe legajo=A001 (que ya está), nombre=Ana, apellido=Gómez. Envía.
//  2. POST /alumnos/crear. Spring arma el AlumnoDTO {A001, Ana, Gómez} y un BindingResult vacío.
//  3. El controller llama a alumnoService.crearAlumno(dto).
//  4. El service pasa validarDatosBasicos (todo completo), y en validarDuplicidadDeAlumno
//     encuentra A001 → lanza DuplicateLegajoException("El legajo A001 ya existe").
//  5. El catch hace bindingResult.rejectValue("legajo", ..., "El legajo A001 ya existe")
//     y devuelve "alumnos/crear" con el MISMO Model (dto + bindingResult).
//  6. Thymeleaf renderiza crear.html:
//     - th:field="*{legajo}" → value="A001"   (lo que escribió, no se perdió)
//     - th:field="*{nombre}" → value="Ana"
//     - #fields.hasErrors('legajo') → true → el div existe, th:errors escribe el mensaje
//     - #fields.hasErrors('nombre') → false → ese div no existe
//
//  Resultado esperado: el formulario vuelve con los tres campos cargados como los dejó,
//  y debajo del legajo, en rojo: "El legajo A001 ya existe". Nada más marcado.
//  La persona corrige solo el legajo y reenvía.
```

Ese es el estándar de formularios en cliente liviano: **misma vista, campos conservados, cada error al lado de su campo.**

🎓 **Si te lo preguntan — ¿qué es `BindingResult` y para qué sirve?**
Es el objeto que Spring asocia al DTO recibido con `@ModelAttribute` y que contiene los errores del formulario, asociados campo por campo. El controller los carga con `rejectValue` (a partir de las validaciones del service) y devuelve la misma vista; la plantilla los consulta con `#fields.hasErrors('campo')` y los muestra con `th:errors`, mientras `th:field` conserva lo que la persona ya escribió.

---

## 5. 🔴 Atributos flash: un mensaje que sobrevive a un redirect

Ahora el cabo del módulo 2. Cuando el alumno se crea bien, el controller redirige al detalle. Y cuando no existe, redirige a `/404`. En los dos casos queremos mostrar un mensaje en la pantalla siguiente — pero el `redirect` es un pedido nuevo, y el `Model` no viaja.

La solución es `RedirectAttributes.addFlashAttribute`:

```java
redirectAttributes.addFlashAttribute("mensaje", "Alumno creado exitosamente");
redirectAttributes.addFlashAttribute("tipoMensaje", "success");
return "redirect:/alumnos/" + alumnoCreado.getLegajo();
```

> **Atributo flash:** un valor que se guarda del lado del servidor **hasta la próxima request de esa persona**, se pone en el `Model` de esa request, y **se borra**. Sobrevive exactamente a un redirect. Si la persona refresca la pantalla, ya no está.

```
   POST /alumnos/crear                          GET /alumnos/A003  (el redirect)
   ┌──────────────────────────┐                 ┌──────────────────────────────┐
   │ flash: mensaje = "..."   │ ── se guarda ──►│ Model recibe mensaje = "..." │──► la vista lo muestra
   │ return redirect:/...     │   un instante   │ y el flash se borra          │
   └──────────────────────────┘                 └──────────────────────────────┘
                                                 F5 → GET /alumnos/A003 otra vez → sin mensaje
```

El caso del módulo 2, ahora completo:

```java
@GetMapping("/{legajo}")
public String verDetalleAlumno(@PathVariable String legajo, Model model, RedirectAttributes redirectAttributes) {
    try {
        AlumnoDTO alumno = alumnoService.obtenerAlumnoPorLegajo(legajo).get();
        model.addAttribute("alumno", alumno);
        model.addAttribute("titulo", "Detalle del alumno");
        model.addAttribute("tieneContactos", !alumno.getContactos().isEmpty());
        return "alumnos/detalle";
    }
    catch (NotFoundException ex) {
        redirectAttributes.addFlashAttribute("mensaje", ex.getMessage());   // ← ESTA era la línea que faltaba
        return "redirect:/404";
    }
}
```

Y la pantalla 404 del módulo 2 ya lo estaba esperando:

```html
<div th:if="${mensaje}">
    <span th:text="${mensaje}">El recurso solicitado no fue encontrado</span>
</div>
```

```
// Resultado esperado para GET /alumnos/ZZZ:
//  1. El service lanza NotFoundException("No se encontró Alumno con identificador ZZZ").
//  2. El controller guarda ese mensaje como flash y redirige.
//  3. El navegador hace GET /404. Spring pone "mensaje" en el Model de ESA request.
//  4. La 404 muestra: "No se encontró Alumno con identificador ZZZ".
//  5. F5 → GET /404 sin flash → la 404 sin el mensaje (el th:if lo esconde).
```

Para el mensaje de éxito ("Alumno creado exitosamente") el mecanismo es idéntico: llega al `Model` del detalle bajo `"mensaje"` y `"tipoMensaje"`. Para que se vea, la pantalla de detalle (o mejor, el layout, así sirve para todas) necesita un bloque `th:if="${mensaje}"` como el de la 404.

**¿Y dónde vive el flash ese instante entre el `POST` y el `GET`?** En un lugar que el servidor reserva para cada persona conectada y que se explica en el módulo 4. Por ahora: existe, y el flash lo usa.

🎓 **Si te lo preguntan — ¿qué es un atributo flash y cuándo se usa?**
Es un atributo que se agrega con `RedirectAttributes.addFlashAttribute` antes de un `redirect`: se conserva del lado del servidor hasta la request siguiente, se inyecta en su `Model` y se elimina. Se usa para mostrar mensajes (éxito, error) en la pantalla a la que se redirige, ya que el `Model` normal no sobrevive a un redirect.

---

## 6. 🟡 Editar y eliminar: el mismo patrón, dos veces más

### 6.1 Editar: el `GET` carga el DTO, el `POST` lo actualiza

```java
@GetMapping("/{legajo}/editar")                          // GET /alumnos/A001/editar
public String mostrarFormularioEditar(@PathVariable String legajo, Model model, RedirectAttributes redirectAttributes) {
    try {
        AlumnoDTO alumnoDTO = alumnoService.obtenerAlumnoPorLegajo(legajo).get();
        model.addAttribute("alumno", alumnoDTO);         // el DTO CARGADO: th:field va a rellenar cada input
        model.addAttribute("titulo", "Editar alumno");
        return "alumnos/editar";
    }
    catch (NotFoundException ex) {
        redirectAttributes.addFlashAttribute("mensaje", ex.getMessage());
        return "redirect:/404";
    }
}

@PostMapping("/{legajo}/actualizar")                     // POST /alumnos/A001/actualizar
public String actualizarAlumno(@PathVariable String legajo,                  // el legajo ORIGINAL, de la URL
                               @ModelAttribute("alumno") AlumnoDTO alumnoDTO, // el DTO con los cambios, del formulario
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
    try {
        AlumnoDTO actualizado = alumnoService.actualizarAlumno(legajo, alumnoDTO);
        redirectAttributes.addFlashAttribute("mensaje", "Alumno actualizado exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/alumnos/" + actualizado.getLegajo();
    }
    catch (NotFoundException ex) {
        redirectAttributes.addFlashAttribute("mensaje", ex.getMessage());
        return "redirect:/404";
    }
    catch (DuplicateLegajoException ex) {
        bindingResult.rejectValue("legajo", "error.legajo", ex.getMessage());
        model.addAttribute("titulo", "Editar alumno");
        return "alumnos/editar";
    }
    catch (ValidationException e) {
        convertirValidationExceptionABindingResult(e, bindingResult);
        model.addAttribute("titulo", "Editar alumno");
        return "alumnos/editar";
    }
}
```

Fijate que el método recibe **dos legajos**: el de la URL (`@PathVariable`, el original) y el del DTO (el que puede haber cambiado en el formulario). El service los necesita a los dos:

```java
public AlumnoDTO actualizarAlumno(String legajo, AlumnoDTO alumnoDTO) {
    intentarRecuperarAlumno(legajo);               // ¿existe el original? si no, NotFoundException
    validarDatosBasicos(alumnoDTO);
    validarContactos(alumnoDTO);

    if (!legajo.equals(alumnoDTO.getLegajo().trim())) {   // ¿cambió el legajo?
        validarDuplicidadDeAlumno(alumnoDTO);             // solo entonces: ¿el nuevo ya existe?
    }                                                     // (sin este if, editar sin tocar el legajo
                                                          //  daría "ya existe"... contra sí mismo)
    Alumno alumno = convertirDTOAEntity(alumnoDTO);
    alumnosRepository.save(alumno);
    return convertirADTO(alumno);
}
```

La plantilla `editar.html` es la de `crear.html` con dos diferencias: el `th:action` apunta a `@{/alumnos/{legajo}/actualizar(legajo=${alumno.legajo})}` y el botón dice "Guardar cambios". Todo lo demás (`th:object`, `th:field`, los bloques de error) es idéntico — y como el DTO del Model viene cargado, `th:field` rellena cada input con el valor actual. **El mismo formulario sirve vacío para crear y cargado para editar.**

### 6.2 Eliminar: un `POST` sin formulario visible

```java
@PostMapping("/{legajo}/eliminar")                       // POST /alumnos/A001/eliminar
public String eliminarAlumno(@PathVariable String legajo, RedirectAttributes redirectAttributes) {
    try {
        alumnoService.eliminarAlumno(legajo);
        redirectAttributes.addFlashAttribute("mensaje", "Alumno eliminado exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/alumnos";                      // a la lista, que ya no lo tiene
    }
    catch (NotFoundException ex) {
        redirectAttributes.addFlashAttribute("mensaje", ex.getMessage());
        return "redirect:/404";
    }
}
```

No hay pantalla de "eliminar": es una acción. Lo mínimo del lado de la plantilla es un formulario con solo un botón, en la fila de la lista:

```html
<form th:action="@{/alumnos/{legajo}/eliminar(legajo=${alumno.legajo})}" method="post">
    <button type="submit">Eliminar</button>
</form>
```

> 🕳️ **Madriguera — el cuadro de confirmación**
> En la pantalla real, el botón no envía directo: un JavaScript en el navegador abre un cuadro "¿Seguro que querés eliminar a Juan Pérez?" y recién al confirmar envía el `POST`. Es lógica de cliente (los `th:data-*` del módulo 2 le pasan el legajo y el nombre). El servidor no se entera de nada hasta el `POST`.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 7. 🟡 Listas dentro del formulario: los contactos

Un alumno puede tener varios contactos, y el formulario tiene que permitir cargar varios. El DTO ya lo prevé: `AlumnoDTO` tiene `List<ContactoDTO> contactos`, y cada `ContactoDTO` tiene `tipoContacto` (EMAIL o TELEFONO) y `valor`.

El problema es el `name` de los inputs: para que Spring arme la lista, el tercer contacto tiene que llegar como `contactos[2].tipoContacto` y `contactos[2].valor`. Con índice. Y ese índice lo genera el `th:each`:

```html
<div id="contactos-container">
    <!-- th:each="contacto, iterStat : *{contactos}"
           ├─ contacto: cada ContactoDTO
           └─ iterStat: un objeto con datos de la iteración; iterStat.index es 0, 1, 2... -->
    <div th:each="contacto, iterStat : *{contactos}">

        <label>Tipo de contacto</label>
        <!-- *{contactos[__${iterStat.index}__].tipoContacto}
             Los __...__ son PREPROCESAMIENTO: Thymeleaf evalúa lo de adentro ANTES que el resto.
             Para el primer contacto, ${iterStat.index} es 0, y la expresión queda *{contactos[0].tipoContacto}.
             th:field genera entonces name="contactos[0].tipoContacto". -->
        <select th:field="*{contactos[__${iterStat.index}__].tipoContacto}">
            <option value="">Seleccionar...</option>
            <option value="EMAIL">Email</option>
            <option value="TELEFONO">Teléfono</option>
        </select>
        <div th:if="${#fields.hasErrors('contactos[' + iterStat.index + '].tipoContacto')}" class="text-danger">
            <small th:errors="*{contactos[__${iterStat.index}__].tipoContacto}">Error en tipo</small>
        </div>

        <label>Valor</label>
        <input type="text" th:field="*{contactos[__${iterStat.index}__].valor}" placeholder="juan@mail.com o 11-1234-5678">
        <div th:if="${#fields.hasErrors('contactos[' + iterStat.index + '].valor')}" class="text-danger">
            <small th:errors="*{contactos[__${iterStat.index}__].valor}">Error en valor</small>
        </div>

        <button type="button" onclick="eliminarContacto(this)">Quitar</button>
    </div>
</div>
<button type="button" onclick="agregarContacto()">Agregar contacto</button>
```

```
// Resultado esperado con dos contactos ya cargados (edición):
//    <select id="contactos0.tipoContacto" name="contactos[0].tipoContacto"> ... <option value="EMAIL" selected> ...
//    <input  id="contactos0.valor"        name="contactos[0].valor" value="juan.perez@mail.com">
//    <select id="contactos1.tipoContacto" name="contactos[1].tipoContacto"> ... <option value="TELEFONO" selected> ...
//    <input  id="contactos1.valor"        name="contactos[1].valor" value="11-1234-5678">
//  Al enviar, el POST lleva contactos[0].tipoContacto=EMAIL&contactos[0].valor=...&contactos[1]...
//  y Spring arma la List<ContactoDTO> con dos elementos, en orden.
```

Y del lado del service, la validación por índice, con el mismo formato `campo → mensaje` de la sección 4 — solo que ahora el campo lleva el índice adentro, para que el error caiga en **la fila correcta**:

```java
private void validarContactos(AlumnoDTO alumnoDTO) {
    if (alumnoDTO.getContactos().size() == 0) return;            // sin contactos es válido: son opcionales

    ValidationException validationException = new ValidationException("Errores de validación");
    boolean tieneErrores = false;

    for (int i = 0; i < alumnoDTO.getContactos().size(); i++) {
        ContactoDTO contacto = alumnoDTO.getContactos().get(i);
        boolean tieneTipo  = contacto.getTipoContacto() != null;
        boolean tieneValor = contacto.getValor() != null && !contacto.getValor().trim().isEmpty();

        if (tieneTipo && !tieneValor) {                          // eligió tipo pero no escribió valor
            validationException.addFieldError("contactos[" + i + "].valor", "El valor del contacto es obligatorio");
            tieneErrores = true;
        }
        if (tieneValor && !tieneTipo) {                          // escribió valor pero no eligió tipo
            validationException.addFieldError("contactos[" + i + "].tipoContacto", "El tipo de contacto es obligatorio");
            tieneErrores = true;
        }
        if (tieneTipo && tieneValor && contacto.getTipoContacto().equals(TipoContacto.EMAIL)) {
            if (!contacto.getValor().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {   // formato de email
                validationException.addFieldError("contactos[" + i + "].valor", "El formato del email no es válido");
                tieneErrores = true;
            }
        }
    }
    if (tieneErrores) throw validationException;
}
```

`"contactos[1].valor" → "El formato del email no es válido"` cae exactamente debajo del segundo input de valor. Mismo mecanismo que un campo simple, con índice.

> 🕳️ **Madriguera — "Agregar contacto" sin recargar**
> El botón `agregarContacto()` es JavaScript en el navegador: clona una fila del formulario y le corrige los índices en los `name` (`contactos[2]...`) para que Spring los reciba en orden. No toca el servidor hasta que se envía. Para el alta con el DTO vacío, hay cero filas al principio y todas las agrega ese script.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## 8. 🟢 Cuando no anda: errores de formularios

| Lo que ves | Qué es | Qué revisar |
|---|---|---|
| *"Neither BindingResult nor plain target object for bean name 'alumno' available"* | La vista usa `th:object="${alumno}"` pero el Model no tiene esa clave | El `GET` tiene que hacer `model.addAttribute("alumno", ...)`. En el `POST`, el `@ModelAttribute("alumno")` lo pone solo. |
| Error 500 al enviar, mencionando `BindingResult` | El parámetro `BindingResult` no está justo después del `@ModelAttribute` | Reordená: DTO, luego `BindingResult`, luego lo demás. |
| El DTO llega con todo en `null` | Los `name` del formulario no coinciden con los atributos del DTO | Usá `th:field="*{campo}"`, no `name` escrito a mano. |
| El formulario vuelve sin lo que escribí | En el error hiciste `redirect` en vez de devolver la vista | En los `catch` de validación, `return "alumnos/crear"`, no `redirect:`. |
| El mensaje flash no aparece | La pantalla destino no tiene el `th:if="${mensaje}"`, o refrescaste | Agregá el bloque; y recordá que el flash dura una sola request. |
| Error 405 *"Method Not Allowed"* | Un `<form method="post">` apunta a una ruta que solo tiene `@GetMapping` (o al revés) | El `th:action` tiene que ir a la ruta del `@PostMapping`. |

---

🧩 **En tu TP** — cada pantalla de tus maquetas que tenga un formulario (registro, alta de necesidad, confirmar recepción con fotos, importación) se traduce igual: un `GET` que muestra el formulario con un DTO en el Model, un `POST` a una ruta de acción que recibe el DTO con `@ModelAttribute` y delega en un service, `BindingResult` para devolver los errores a la misma pantalla, y flash + redirect cuando sale bien. Esto es, exactamente, "la conectividad de los formularios". La única diferencia con este módulo es que tu service, en vez de guardar en memoria, va a mandar el DTO a otro servicio por HTTP: módulo 7.

---

## Checkpoint del módulo 3

Sin respuestas.

1. ¿Por qué modificar un alumno es `POST /alumnos/{legajo}/actualizar` y no `PUT /alumnos/{legajo}`?
2. ¿Qué diferencia hay entre `${alumno.legajo}` y `*{legajo}`? ¿Cuándo se puede usar la segunda?
3. ¿Qué tres atributos HTML genera `th:field="*{nombre}"` y cuál de ellos es indispensable para que Spring arme el DTO?
4. ¿Qué hace `@ModelAttribute("alumno")` en el parámetro del `POST`? Nombrá las dos cosas.
5. ¿Dónde se validan los datos: en el controller o en el service? ¿Por qué se acumulan los errores en vez de lanzar el primero?
6. En un error de validación, ¿por qué el controller devuelve `"alumnos/crear"` y no `"redirect:/alumnos/nuevo"`? ¿Qué se perdería?
7. ¿Qué hace `#fields.hasErrors('legajo')` y de dónde saca la información?
8. ¿Cuánto dura un atributo flash? ¿Qué pasa si la persona refresca la pantalla?
9. ¿Por qué `actualizarAlumno` recibe el legajo dos veces (URL y DTO)? ¿Qué pasaría sin el `if` que los compara?
10. ¿Qué son los `__${iterStat.index}__` en el formulario de contactos y por qué hacen falta?

---

## Qué viene en el módulo 4

Ya podés mostrar y cargar. Ahora la pregunta que quedó abierta desde el módulo 1: **¿quién es la persona que está cargando esto?** HTTP no lo dice. El módulo 4 explica la **sesión** (el espacio que el servidor reserva para cada persona conectada, el mismo donde el flash vivió un instante) y la **cookie** (el hilo que conecta cada request con su sesión). Se cierra el **hilo H4**.

---

**FIN DEL MÓDULO 3**
