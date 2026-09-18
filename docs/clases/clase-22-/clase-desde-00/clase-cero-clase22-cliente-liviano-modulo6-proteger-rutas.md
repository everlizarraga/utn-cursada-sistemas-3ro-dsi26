# Clase desde cero — Cliente Liviano — Módulo 6
# Proteger rutas y esconder botones según rol y permiso

**Fase C — Identidad** · Densidad general: 🔴 (los tres niveles de protección son definición de examen; el resto es lo que vas a escribir en el servidor de front del TP)

---

## Sobre este documento

**Cubre:** los tres niveles de protección de recursos (autenticación, roles, roles y permisos) y qué se responde cuando falla; qué es Spring Security y qué hace apenas lo agregás; cómo se configura qué rutas son públicas y cuáles no (`SecurityFilterChain`); cómo se modela un usuario con rol y permisos y por qué es una clase aparte; cómo Spring Security busca al usuario (`UserDetailsService`) y compara la contraseña sin guardarla en texto plano (`BCrypt`); el formulario de login que Spring procesa por vos; cómo se protege un método (`@PreAuthorize`) y cómo una plantilla esconde un botón (`sec:authorize`); y por qué esas dos últimas cosas van **siempre juntas**. Se cierra el **hilo H5**.

**NO cubre:** validar tokens en un servidor stateless (7: acá la identidad vive en la sesión del módulo 4); hablarle a otros servicios (7). Este módulo es la forma monolítica del módulo 1 con seguridad encima: usuarios y alumnos en el mismo proyecto.

## De dónde venís

- **Módulo 4:** el flujo de login con sesión y cookie, paso por paso. Acá Spring Security lo implementa.
- **Módulo 5:** un *filtro* es un componente que intercepta cada request antes de los controllers. Spring Security es una cadena de filtros.
- **Módulo 1:** recursos independientes y dependientes; y el hilo H5: hoy todas las rutas son públicas.
- **Módulo 2:** `th:if`, herencia de plantillas. **Módulo 3:** formularios `POST`.
- **De clases anteriores:** `@Bean` y `@Configuration` (una clase que declara objetos que Spring va a administrar), `@Service`, enums, `Optional.orElseThrow`.

---

## 1. 🔴 Los tres niveles de protección

Un recurso se puede proteger con tres niveles de exigencia creciente. Cada uno responde una pregunta distinta:

| Nivel | Pregunta | Ejemplo | Qué falta |
|---|---|---|---|
| **Autenticación** | ¿Estás logueado? | Cualquier usuario logueado ve `/alumnos` | No distingue entre personas: el docente y el administrador pueden lo mismo |
| **Roles** | ¿Quién sos? | Solo `ADMIN` puede entrar a `/alumnos/nuevo` | Es rígido: todos los `ADMIN` pueden exactamente lo mismo, y para dar una capacidad nueva hay que inventar un rol nuevo |
| **Roles y permisos** | ¿Qué podés hacer? | `ADMIN` con permiso `CREAR_ALUMNOS` puede crear; un `ADMIN` sin ese permiso, no | Lo más flexible: el rol dice qué sos, el permiso dice qué acción concreta tenés habilitada |

### 1.1 Por qué roles y permisos, y no solo roles

Con roles solos, la única forma de que dos administradores puedan cosas distintas es crear dos roles. Con permisos, el rol es la categoría y el permiso es la capacidad puntual: dos usuarios con el mismo rol pueden tener permisos distintos, y dos usuarios con roles distintos pueden compartir un permiso. Un docente con permiso `CREAR_ALUMNOS` no deja de ser docente, pero puede crear.

Y los permisos suelen venir con un **alcance**: sobre qué recursos aplica la acción.

| Alcance | Significa | Ejemplo |
|---|---|---|
| **Propio** | Solo el recurso que es de uno | Un alumno edita *su* ficha |
| **Propios** | Un subconjunto asignado | Un encargado ve *los empleados a su cargo* |
| **Todos** | Cualquier recurso | El administrador ve *todos* los alumnos |

El alcance no lo resuelve una anotación: lo resuelve el *service* filtrando según quién pide. Vuelve en la sección 10.

### 1.2 Qué se responde cuando falla

Depende de qué tipo de capa de presentación tiene el sistema:

| Capa de presentación | No está logueado | Está logueado pero no tiene permiso |
|---|---|---|
| **Visual** (cliente liviano) | **Redirigir a la pantalla de login** | Redirigir a una pantalla de error |
| **API REST** (JSON) | Responder **401** *Unauthorized* | Responder **403** *Forbidden* |

Un servidor de front es del primer tipo. Un servicio con API es del segundo. En el TP tenés los dos, y en el módulo 7 vas a ver los dos comportamientos conviviendo.

🎓 **Si te lo preguntan — ¿cuáles son los niveles de protección de recursos y qué diferencia hay entre roles y permisos?**
Autenticación (verificar que el usuario está identificado), roles (verificar el tipo de usuario) y roles con permisos (verificar la acción concreta habilitada). Los roles solos son rígidos porque todos los usuarios de un rol pueden lo mismo; los permisos permiten que usuarios del mismo rol tengan capacidades distintas y que usuarios de roles distintos compartan una, y pueden tener alcance (propio, propios, todos).

---

## 2. 🔴 Spring Security: el portero

**Spring Security** es la librería que implementa todo lo anterior. Funciona como un **portero**: una cadena de filtros que se ejecuta **antes** de cualquier controller, en cada request, y decide si el pedido pasa, si va a la pantalla de login, o si se rechaza.

```
   Navegador ──GET /alumnos/nuevo──►  ┌──────────────────────────┐
                                      │  SPRING SECURITY (portero)│
                                      │  ¿está logueado?          │──no──► redirect /login
                                      │  ¿tiene el rol/permiso?   │──no──► redirect /403
                                      └──────────┬───────────────┘
                                                 │ sí
                                                 ▼
                                      AlumnoController.mostrarFormularioCrear()
```

Dos dependencias en el `pom.xml`:

```xml
<!-- el portero -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<!-- la integración con Thymeleaf: habilita sec:authorize en las plantillas (sección 9).
     El sufijo depende de la versión de Spring Security: 6 para Spring Boot 3. Para Spring Boot 4
     verificá el nombre generándolo con Spring Initializr, como en el módulo 0. -->
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

⚠️ **Apenas agregás `spring-boot-starter-security` y arrancás, TODO queda bloqueado.** Cada ruta, incluidos los CSS, pide login, con una pantalla genérica de Spring y una contraseña generada al azar que aparece en la consola. Es el comportamiento por defecto: "seguro hasta que me digas lo contrario". Por eso el paso siguiente es configurarlo.

---

## 3. 🔴 `SecurityConfig`: qué es público y qué no

```java
@Configuration                                 // clase de configuración: declara beans
@EnableMethodSecurity(prePostEnabled = true)   // habilita @PreAuthorize en los métodos (sección 8)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── QUÉ RUTAS EXIGEN QUÉ ───────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // públicas: el login y los archivos estáticos. Sin esto, ni el CSS de la
                // pantalla de login se puede cargar (¡la pantalla de login pediría login!).
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                // todo lo demás: hace falta estar logueado (nivel 1: autenticación)
                .anyRequest().authenticated()
            )
            // ── LOGIN POR FORMULARIO ───────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")                  // MI pantalla de login (sección 7), no la genérica
                .permitAll()
                .defaultSuccessUrl("/alumnos", true)  // después de loguearse bien: redirigir acá
            )
            // ── LOGOUT ─────────────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")                 // un POST a /logout cierra la sesión
                .logoutSuccessUrl("/login?logout")    // y redirige acá (el ?logout lo usa la pantalla)
                .permitAll()
            )
            // ── QUÉ HACER CUANDO FALLA (la tabla de 1.2, capa visual) ──────────────
            .exceptionHandling(ex -> ex
                // no logueado → a la pantalla de login
                .authenticationEntryPoint((request, response, authException) ->
                        response.sendRedirect("/login?unauthorized"))
                // logueado pero sin permiso → a la pantalla de error 403
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        response.sendRedirect("/403"))
            );

        return http.build();
    }
}
```

> **`SecurityFilterChain`:** el objeto que describe la cadena de filtros del portero. Se declara como `@Bean` y Spring Security lo toma como su configuración. `HttpSecurity` es el constructor con el que se arma, por bloques.

```
// ¿CÓMO FUNCIONA? (tres pedidos distintos)
//  GET /css/style.css  (sin estar logueado)
//   → requestMatchers("/css/**").permitAll() matchea → pasa sin preguntar nada. Resultado: 200, el CSS.
//
//  GET /alumnos  (sin estar logueado)
//   → no matchea nada público → anyRequest().authenticated() → ¿hay sesión con usuario? NO
//   → authenticationEntryPoint → 302 a /login?unauthorized. Resultado: la persona ve la pantalla de login.
//
//  GET /alumnos/nuevo  (logueado como docente, sin el permiso CREAR_ALUMNOS)
//   → anyRequest().authenticated() → ¿hay sesión? SÍ, pasa el portero
//   → llega al método del controller, que tiene @PreAuthorize (sección 8) → falla
//   → accessDeniedHandler → 302 a /403. Resultado: la pantalla "no tenés permiso".
```

Esto es el **nivel 1** completo: público o autenticado. Los niveles 2 y 3 (roles y permisos) se pueden poner acá también (`.requestMatchers("/alumnos/**").hasAnyRole("ADMIN", "DOCENTE")`), pero es más claro ponerlos **en cada método** del controller, que es lo que hace la sección 8.

---

## 4. 🔴 El usuario: una clase aparte, con rol y permisos

Hasta este punto de la materia se modelaron alumnos, docentes, donaciones... pero nunca un "usuario". Es a propósito: el usuario es **la identidad frente al sistema**, y es una cosa distinta de la entidad de negocio.

```java
public enum Rol { ADMIN, DOCENTE }

public enum Permiso { EDITAR_ALUMNOS, ELIMINAR_ALUMNOS, CREAR_ALUMNOS }

@Getter @Setter
public class Usuario {
    private Long id;
    private String nombre;
    private String apellido;
    private String nombreDeUsuario;         // lo que escribe en el login
    private String contrasenia;             // NUNCA en texto plano: sección 6
    private Rol rol;                        // UN rol
    private List<Permiso> permisos = new ArrayList<>();   // VARIOS permisos

    public void agregarPermiso(Permiso p) { this.permisos.add(p); }
}
```

```
   ┌──────────────────┐        rol (1)        ┌─────────┐
   │     Usuario      │──────────────────────►│  Rol    │  ADMIN | DOCENTE
   │──────────────────│                       └─────────┘
   │ nombreDeUsuario  │      permisos (*)     ┌─────────┐
   │ contrasenia      │──────────────────────►│ Permiso │  CREAR | EDITAR | ELIMINAR _ALUMNOS
   └──────────────────┘                       └─────────┘
```

**Por qué `Usuario` no es `Alumno` ni `Docente`:** dos razones.

1. **No repetir credenciales.** Si docentes y administradores fueran dos clases con usuario y contraseña cada una, la lógica de login estaría duplicada. Un solo `Usuario` con un `Rol` la centraliza.
2. **En el login no sabés quién viene.** Cuando alguien escribe usuario y contraseña, el sistema todavía no sabe si es un docente o un administrador. Necesita una entidad única donde buscar; recién después, por el rol, sabe qué es.

Los usuarios del ejemplo viven en un repositorio en memoria, precargados:

| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| `admin` | `1234` | `ADMIN` | `CREAR_ALUMNOS`, `EDITAR_ALUMNOS`, `ELIMINAR_ALUMNOS` |
| `docente` | `1234` | `DOCENTE` | ninguno |

Con esos dos alcanza para ver todos los casos: el admin puede todo, el docente solo mira.

---

## 5. 🔴 `UserDetailsService`: cómo Spring Security busca al usuario

Spring Security implementa el `POST /login` por vos (sección 7). Lo único que no puede saber es **dónde están tus usuarios**. Para eso te pide que implementes una interfaz con un solo método: "dame el usuario con este nombre".

```java
@Service
public class UsuariosService implements UserDetailsService {     // LA interfaz que Spring Security busca

    @Autowired
    private UsuariosRepository usuariosRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. buscar en MI repositorio; si no está, la excepción que Spring Security espera
        var usuario = usuariosRepository.findByNombreDeUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // 2. traducir MIS roles y permisos a lo que Spring Security entiende: "authorities".
        //    Una authority es un String. Y acá está la convención que importa:
        List<GrantedAuthority> authorities = new ArrayList<>();
        usuario.getPermisos().forEach(permiso -> {
            authorities.add(new SimpleGrantedAuthority(permiso.name()));           // permiso: tal cual → "CREAR_ALUMNOS"
        });
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));  // rol: con prefijo → "ROLE_ADMIN"

        // 3. devolver el usuario en el formato de Spring Security (patrón Builder)
        return User.withUsername(usuario.getNombreDeUsuario())
                .password(usuario.getContrasenia())      // el hash, no la contraseña (sección 6)
                .authorities(authorities)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {           // sección 6: cómo se comparan las contraseñas
        return new BCryptPasswordEncoder();
    }
}
```

> **Authority:** para Spring Security, cada rol y cada permiso es un `String` llamado *authority*. No distingue conceptualmente entre unos y otros: la distinción la hace **el prefijo**.

⚠️ **El prefijo `ROLE_` es la convención con la que Spring Security separa roles de permisos.** Vos guardás el rol como `ADMIN`, pero la authority se llama `ROLE_ADMIN`. Después, en la sección 8, `hasRole('ADMIN')` busca `ROLE_ADMIN` (agrega el prefijo solo) y `hasAuthority('CREAR_ALUMNOS')` busca `CREAR_ALUMNOS` (literal). Si te olvidás el prefijo acá, `hasRole` nunca encuentra nada. Si lo escribís dos veces (`hasRole('ROLE_ADMIN')`), tampoco.

Para el admin, la lista de authorities queda: `["CREAR_ALUMNOS", "EDITAR_ALUMNOS", "ELIMINAR_ALUMNOS", "ROLE_ADMIN"]`. Para el docente: `["ROLE_DOCENTE"]`.

```
// ¿CÓMO FUNCIONA? (el login completo, con Spring Security en el medio)
//  1. La persona envía el formulario: POST /login  username=admin  password=1234.
//  2. NO llega a ningún controller tuyo. Lo intercepta Spring Security (formLogin de la sección 3).
//  3. Spring Security llama a usuariosService.loadUserByUsername("admin").
//     → tu método busca en el repositorio, arma las authorities, devuelve el UserDetails.
//  4. Spring Security llama a passwordEncoder.matches("1234", <hash guardado>).   ← sección 6
//     → si no coincide: 302 a /login?error. Fin.
//  5. Coincide: Spring Security CREA LA SESIÓN (módulo 4), guarda ahí quién es y sus authorities,
//     manda el Set-Cookie, y redirige a defaultSuccessUrl: 302 a /alumnos.
//  6. GET /alumnos, con la cookie. El portero encuentra la sesión → pasa.
//
//  Resultado esperado: escribís admin/1234, ves la lista de alumnos con los botones de admin.
//  Escribís admin/9999: volvés al login con "Usuario o contraseña incorrectos".
//  Vos escribiste: el GET /login, la plantilla, y loadUserByUsername. Todo lo demás es Spring Security.
```

---

## 6. 🔴 La contraseña nunca se guarda: `BCrypt`

`usuario.getContrasenia()` **no** devuelve `"1234"`. Devuelve algo como `$2a$10$N9qo8uLOickgx2ZMRZoMye...`: un **hash** de la contraseña.

> **Hash de contraseña:** una transformación de la contraseña que **no se puede revertir**. De `"1234"` se obtiene el hash; del hash no se puede volver a `"1234"`. Se guarda el hash. Para verificar, se vuelve a hashear lo que la persona escribió y se compara **hash contra hash**.

```java
// Al CREAR el usuario (en el repositorio precargado):
var encoder = new BCryptPasswordEncoder();
usuario.setContrasenia(encoder.encode("1234"));        // "1234" → "$2a$10$N9qo8uLO..."; ESO se guarda

// Al LOGUEARSE (lo hace Spring Security por vos, con el @Bean PasswordEncoder de la sección 5):
encoder.matches("1234", usuario.getContrasenia());     // hashea "1234" y compara con lo guardado → true
encoder.matches("9999", usuario.getContrasenia());     // → false
```

Por qué así: si alguien accede a la base de usuarios, ve hashes, no contraseñas. Y como no se pueden revertir, no sirven para loguearse.

Dos preguntas que aparecen siempre:

- **"¿Pero la contraseña viaja en texto plano en el `POST`?"** Sí, en el cuerpo del pedido. Eso lo protege **HTTPS**, que cifra todo el tráfico en tránsito. El hash protege el almacenamiento; HTTPS protege el viaje. Son dos capas distintas.
- **"¿Y si algún día hay que cambiar el algoritmo de hash?"** No se puede "reconvertir" un hash a otro algoritmo, porque no se conoce la contraseña original. Dos salidas: guardar por usuario con qué algoritmo está hasheado y migrarlo en su próximo login (cuando la contraseña vuelve a pasar en texto plano), o resetear todas las contraseñas.

> 🕳️ **Madriguera — activar HTTPS**
> HTTPS se configura en el servidor (un certificado y un par de propiedades) o, más habitualmente, en lo que se pone delante del servidor al desplegar. No cambia una línea del código de este módulo y no forma parte de lo evaluado acá.
> *Volvé al camino — esto se profundiza aparte, otro día.*

🎓 **Si te lo preguntan — ¿cómo se almacena y verifica una contraseña?**
Se almacena el resultado de una función de hash (por ejemplo BCrypt), no la contraseña. Para verificar, se hashea la contraseña ingresada y se compara con el hash almacenado. El hash no es reversible, por lo que un acceso a la base no expone contraseñas; la protección en tránsito la da HTTPS.

---

## 7. 🔴 La pantalla de login: vos el `GET`, Spring el `POST`

El controller tiene **solo el `GET`**:

```java
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";        // templates/login.html
    }
    // NO HAY @PostMapping("/login"). El POST lo procesa Spring Security (sección 5).
}
```

Y la plantilla, `templates/login.html` (esta no hereda del layout: la persona todavía no está adentro, no tiene sentido mostrarle la barra de navegación):

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" lang="es">
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <link th:href="@{/css/style.css}" rel="stylesheet">
</head>
<body>
    <h1>Iniciar sesión</h1>

    <!-- th:action="@{/login}" + method="post": el POST que Spring Security intercepta.
         th:action además agrega solo un campo oculto anti-CSRF que Spring Security exige. -->
    <form th:action="@{/login}" method="post">
        <label for="username">Usuario</label>
        <!-- name="username" y name="password" son LOS NOMBRES QUE SPRING SECURITY ESPERA.
             Con otros nombres, no encuentra las credenciales y el login falla siempre. -->
        <input type="text" id="username" name="username" required autofocus>

        <label for="password">Contraseña</label>
        <input type="password" id="password" name="password" required>

        <button type="submit">Ingresar</button>
    </form>

    <!-- ${param.error}: "¿la URL trae ?error?". Spring Security redirige a /login?error si falló. -->
    <div th:if="${param.error}">Usuario o contraseña incorrectos</div>
    <!-- ${param.logout}: la URL de logoutSuccessUrl era /login?logout -->
    <div th:if="${param.logout}">Sesión cerrada correctamente</div>
    <!-- ${param.unauthorized}: el authenticationEntryPoint mandaba a /login?unauthorized -->
    <div th:if="${param.unauthorized}">Tenés que iniciar sesión para ver esa página</div>
</body>
</html>
```

> **`${param.x}`:** en Thymeleaf, `param` es el mapa de parámetros de la URL. `${param.error}` es verdadero si la URL tiene `?error`. Es la forma en que la misma pantalla de login muestra distintos mensajes según de dónde viene.

Y el logout, en la barra de navegación del layout: **es un `POST`**, no un link.

```html
<form th:action="@{/logout}" method="post">
    <button type="submit">Cerrar sesión</button>
</form>
```

Un `<a href="/logout">` haría un `GET` y Spring Security lo rechazaría. Cerrar sesión modifica estado (borra la sesión, módulo 4), y por eso va por `POST`, igual que el módulo 3.

---

## 8. 🔴 `@PreAuthorize`: proteger cada método del controller

Con el nivel 1 resuelto en la config (estar logueado), los niveles 2 y 3 se declaran **método por método**, con una anotación que se evalúa **antes** de ejecutarlo:

```java
@Controller
@RequestMapping("/alumnos")
@RequiredArgsConstructor
public class AlumnoController {
    private final AlumnoService alumnoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMIN')")            // nivel 2: cualquiera de los dos roles ve la lista
    public String listarAlumnos(Model model) { /* ... módulo 2 ... */ }

    @GetMapping("/{legajo}")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMIN')")
    public String verDetalleAlumno(@PathVariable String legajo, Model model, RedirectAttributes ra) { /* ... */ }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CREAR_ALUMNOS')")   // nivel 3: rol Y permiso
    public String mostrarFormularioCrear(Model model) { /* ... módulo 3 ... */ }

    @PostMapping("/crear")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CREAR_ALUMNOS')")   // el POST también, siempre
    public String crearAlumno(/* ... */) { /* ... */ }

    @GetMapping("/{legajo}/editar")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('EDITAR_ALUMNOS')")
    public String mostrarFormularioEditar(/* ... */) { /* ... */ }

    @PostMapping("/{legajo}/actualizar")
    @PreAuthorize("hasAuthority('EDITAR_ALUMNOS')")            // solo el permiso: el rol no importa
    public String actualizarAlumno(/* ... */) { /* ... */ }

    @PostMapping("/{legajo}/eliminar")
    @PreAuthorize("hasAuthority('ELIMINAR_ALUMNOS')")
    public String eliminarAlumno(/* ... */) { /* ... */ }
}
```

Las expresiones, y qué authority buscan en la sesión (sección 5):

| Expresión | Busca | Verdadero si |
|---|---|---|
| `hasRole('ADMIN')` | `ROLE_ADMIN` (agrega el prefijo solo) | el usuario tiene ese rol |
| `hasAnyRole('DOCENTE', 'ADMIN')` | `ROLE_DOCENTE` o `ROLE_ADMIN` | tiene alguno |
| `hasAuthority('CREAR_ALUMNOS')` | `CREAR_ALUMNOS` (literal) | tiene ese permiso |
| `hasAnyAuthority('A', 'B')` | `A` o `B` | tiene alguno |
| `hasRole('ADMIN') and hasAuthority('CREAR_ALUMNOS')` | las dos | tiene el rol **y** el permiso |

```
// ¿CÓMO FUNCIONA?
//  docente (authorities: ["ROLE_DOCENTE"]) hace GET /alumnos/nuevo:
//   1. El portero (sección 3): ¿logueado? sí → pasa.
//   2. Antes de ejecutar mostrarFormularioCrear, Spring evalúa @PreAuthorize:
//      hasRole('ADMIN') → ¿tiene ROLE_ADMIN? no → false. El "and" ya es false.
//   3. Lanza AccessDeniedException → accessDeniedHandler → 302 a /403.
//   El método NUNCA se ejecutó. Resultado: la pantalla "no tenés permiso".
//
//  admin (authorities: ["CREAR_ALUMNOS","EDITAR_ALUMNOS","ELIMINAR_ALUMNOS","ROLE_ADMIN"]):
//   hasRole('ADMIN') → sí; hasAuthority('CREAR_ALUMNOS') → sí → true. Se ejecuta. Ve el formulario.
```

Notá que **el `POST` también está protegido**, no solo el `GET` que muestra el formulario. Si solo protegés el `GET`, alguien puede armar el `POST` a mano (con Postman) y crear un alumno sin nunca haber visto el formulario. Cada método que hace algo lleva su anotación.

---

## 9. 🔴 `sec:authorize`: esconder el botón

Ya está protegida la ruta. Pero al docente le seguiría apareciendo el botón "Nuevo alumno", y al hacer clic caería en la 403. Feo. La plantilla tiene que **no renderizar** lo que la persona no puede usar.

Para eso está `sec:authorize`, que viene de la dependencia `thymeleaf-extras-springsecurity`. Se declara el espacio de nombres en el `<html>` y se usa como un `th:if` sobre las authorities de la sesión:

```html
<html lang="es" xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">   <!-- habilita sec: (opcional para
                                                                          funcionar, útil para el IDE) -->
...
<!-- en la barra de navegación del layout -->
<a sec:authorize="hasAuthority('CREAR_ALUMNOS')" th:href="@{/alumnos/nuevo}">Nuevo alumno</a>

<!-- en la fila de la lista -->
<a sec:authorize="hasAuthority('EDITAR_ALUMNOS')" th:href="@{/alumnos/{legajo}/editar(legajo=${alumno.legajo})}">Editar</a>
<button sec:authorize="hasAuthority('ELIMINAR_ALUMNOS')" type="button">Eliminar</button>
```

```
// Resultado esperado:
//  docente → la lista se renderiza SIN el link "Nuevo alumno", SIN "Editar", SIN "Eliminar".
//            No están ocultos: no existen en el HTML que le llegó.
//  admin   → todo visible.
```

Las mismas expresiones de la sección 8 (`hasRole`, `hasAuthority`, `hasAnyRole`...), en la plantilla en vez de en el método.

⚠️ **`sec:authorize` NO es seguridad. Es cortesía.** Esconde el botón, pero la ruta sigue ahí. Un docente que sepa que existe `/alumnos/nuevo` la escribe en la barra, o le pega con Postman, y si el método no tiene `@PreAuthorize`, **entra**. Por eso la regla es una sola:

> **Se protege la ruta (`@PreAuthorize`) Y se esconde el botón (`sec:authorize`). Siempre las dos.** La primera es la seguridad; la segunda, la experiencia. Ninguna reemplaza a la otra.

🎓 **Si te lo preguntan — ¿alcanza con ocultar en la vista las acciones que el usuario no puede hacer?**
No. Ocultar en la vista (`sec:authorize`) mejora la experiencia pero no impide que el usuario acceda a la ruta directamente. La protección real está en el servidor, sobre la ruta o el método (`@PreAuthorize` o la configuración de seguridad). Se aplican las dos: proteger la ruta y ocultar la acción.

---

## 10. 🟡 401, 403, y el alcance "propio"

Dos precisiones que cierran el módulo.

**No autenticado ≠ sin permiso.** Son dos situaciones distintas con dos respuestas distintas (tabla de 1.2). En este servidor de front: no logueado → `/login?unauthorized`; logueado sin permiso → `/403`, que es una plantilla más, heredando del layout:

```html
<html th:replace="~{layout/base :: html}">
<div th:fragment="contenido">
    <h1 th:text="${titulo}">Acceso denegado</h1>
    <p th:text="${mensaje}">No tenés permiso para acceder a este recurso.</p>
    <a th:href="@{/alumnos}">Volver a la lista</a>
</div>
</html>
```

(El controller de `/403` pone `titulo` y `mensaje` en el Model, como cualquier otra pantalla.)

**El alcance "propio" no lo resuelve ninguna anotación.** `@PreAuthorize("hasAuthority('VER_DONACIONES')")` dice *si puede ver donaciones*; no dice *cuáles*. Que una entidad beneficiaria vea solo **sus** donaciones lo hace el *service*: recibe quién es el usuario logueado y filtra. Identificar al usuario no sirve solo para dejarlo pasar o no: sirve para **sesgar los resultados**. Cómo llega "quién es" hasta el service: módulo 7.

---

## 11. 🟢 Cuando no anda: errores de seguridad

| Lo que ves | Qué es | Qué revisar |
|---|---|---|
| Todo redirige a login, hasta el login mismo, y aparece sin estilo | Faltan los estáticos en `permitAll()` | `.requestMatchers("/login", "/css/**", "/js/**").permitAll()` |
| El login siempre falla con usuario y contraseña correctos | Los `name` del formulario no son `username`/`password`, o la contraseña está guardada sin hashear | Revisá los `name`; y que al crear el usuario se use `encoder.encode(...)` |
| *"There is no PasswordEncoder mapped for the id 'null'"* | Falta el `@Bean PasswordEncoder` o la contraseña guardada no es un hash | Sección 5 y 6 |
| `hasRole('ADMIN')` nunca da verdadero | Falta el prefijo `ROLE_` en `loadUserByUsername`, o lo pusiste en la expresión | La authority es `ROLE_ADMIN`; la expresión es `hasRole('ADMIN')` |
| `sec:authorize` no esconde nada: los botones aparecen para todos | Falta la dependencia `thymeleaf-extras-springsecurity` | Sin ella, el atributo se ignora en silencio |
| 403 al cerrar sesión | El logout es un `<a href>` (GET) | Tiene que ser un `<form method="post">` |
| Un `POST` de un formulario da 403 aunque estés logueado con permiso | Falta el token CSRF: el `<form>` tiene `action` en vez de `th:action` | Con `th:action`, Thymeleaf agrega el token solo |
| Un docente entra a `/alumnos/nuevo` escribiendo la URL | El método no tiene `@PreAuthorize`; solo escondiste el botón | Sección 9, en negrita |

---

🧩 **En tu TP** — este módulo es el servidor de front de DonaTrack **tal cual**, con otros roles (donante, entidad beneficiaria, administrador) y otros permisos. La única diferencia de fondo es de dónde salen los usuarios: acá, de un repositorio en memoria del mismo proyecto; en tu TP, del servicio de autenticación, por HTTP. Eso cambia la sección 5 (el `UserDetailsService` se reemplaza por otra pieza) y se ve en el módulo 7. Todo lo demás —`SecurityConfig`, `login.html`, `@PreAuthorize` en cada controller, `sec:authorize` en cada plantilla, `/403`— se copia con los nombres cambiados. Y el "sesgar resultados" de la sección 10 (una beneficiaria ve solo lo suyo) es una de las cosas que hay que poder justificar en la entrega.

---

## Checkpoint del módulo 6

Sin respuestas.

1. Nombrá los tres niveles de protección y qué pregunta responde cada uno. ¿Cuál es el problema de usar solo roles?
2. Un sistema tiene capa visual. ¿Qué hace cuando alguien no logueado pide una ruta protegida? ¿Y si es una API REST?
3. ¿Qué pasa apenas agregás `spring-boot-starter-security` sin configurar nada?
4. ¿Por qué `/css/**` tiene que estar en `permitAll()`? ¿Qué se rompe si no?
5. ¿Por qué `Usuario` es una clase separada de `Alumno` y `Docente`? Dos razones.
6. ¿Qué método implementás para que Spring Security encuentre tus usuarios, y qué devuelve?
7. ¿Qué diferencia hay entre la authority `ROLE_ADMIN` y la authority `CREAR_ALUMNOS`? ¿Qué expresión busca a cada una?
8. ¿Qué se guarda en `usuario.contrasenia`? ¿Cómo se verifica un login sin conocer la contraseña original?
9. ¿Escribís el `@PostMapping("/login")`? ¿Quién lo procesa? ¿Qué nombres tienen que tener los inputs?
10. Un compañero protege todas las pantallas con `sec:authorize` y dice que ya está seguro. ¿Qué le falta y por qué?

---

## Qué viene en el módulo 7

Tenés todas las piezas: pantallas (2, 3), sesión (4), tokens (5), portero (6). El módulo 7 las arma en la arquitectura del TP: **sesiones mixtas**. El navegador habla con el servidor de front por sesión y cookie; el servidor de front habla con los servicios por JWT. Vas a ver el login completo cruzando las tres zonas, cómo el front guarda los tokens en la sesión y los adjunta a cada pedido hacia atrás, el filtro que los valida del lado de los servicios, el canje automático del refresh, y la traducción pieza por pieza a DonaTrack. Se cierran **H2**, **H6** y **H7**.

---

**FIN DEL MÓDULO 6**
