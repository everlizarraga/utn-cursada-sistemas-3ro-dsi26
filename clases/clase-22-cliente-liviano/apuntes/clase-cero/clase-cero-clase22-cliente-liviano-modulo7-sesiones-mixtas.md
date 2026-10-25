# Clase desde cero — Cliente Liviano — Módulo 7
# Sesiones mixtas: cómo se arma todo junto en DonaTrack

**Fase D — El TP** · Densidad general: 🔴 (es la arquitectura de la Entrega 5; cada pieza de acá se implementa)

---

## Sobre este documento

**Cubre:** la arquitectura de **sesiones mixtas** (sesión y cookie hacia la persona, tokens hacia los servicios) y por qué es mixta; las piezas exactas de cada lado y qué hace cada clase; el flujo de login cruzando las tres zonas; el flujo de una pantalla que trae datos de un servicio; el canje automático del refresh token; la protección en los dos lados; la configuración que hace que todo funcione o falle en silencio; cómo llega "quién es" hasta el service para sesgar resultados; y la traducción pieza por pieza a DonaTrack. Se cierran **H2**, **H6** y **H7**.

**NO cubre:** nada nuevo de Thymeleaf, sesiones, tokens ni Spring Security: todo eso está en los módulos anteriores y acá solo se combina. Tampoco cubre el despliegue en contenedores ni la comunicación entre servicios por colas: son unidades posteriores.

## De dónde venís

Todo. En particular:

- **Módulo 1:** el servidor de front desacoplado no tiene entidades ni repositorios: solo DTOs y services que llaman por HTTP.
- **Módulo 4:** `request.getSession().setAttribute(...)` guarda algo por persona entre pedidos.
- **Módulo 5:** `Authorization: Bearer <jwt>`, `JwtUtil`, access + refresh, la clave compartida, el filtro que valida en cada request.
- **Módulo 6:** `SecurityConfig` del front, `formLogin`, authorities con `ROLE_`, `@PreAuthorize`, `sec:authorize`.
- **De clases anteriores:** `WebClient` como cliente HTTP de Spring; `@RestController` y `ResponseEntity`; `@RequestBody`.

---

## 1. 🔴 El plano: por qué "mixtas"

```
    ┌─────────────┐        sesión + cookie        ┌──────────────────────┐       JWT (Bearer)       ┌──────────────────┐
    │  NAVEGADOR  │◄════════════════════════════►│   SERVIDOR DE FRONT  │◄═══════════════════════►│    SERVICIOS     │
    │  (persona)  │      STATEFUL (módulo 4)      │   (Spring + Thymeleaf)│     STATELESS (módulo 5) │  (APIs REST)     │
    └─────────────┘                               └──────────────────────┘                          └──────────────────┘
      guarda la cookie                             guarda la SESIÓN de cada persona:                 no guardan nada:
      JSESSIONID                                   username, accessToken, refreshToken, rol, permisos  validan la firma
```

Dos relaciones, dos arquitecturas, y cada una es la correcta para su tramo:

| Tramo | Arquitectura | Por qué |
|---|---|---|
| **Navegador ↔ front** | Stateful: sesión + cookie | Es una persona con un navegador. La cookie viaja sola, la sesión se puede cerrar de verdad, el login es un formulario. Todo lo cómodo del módulo 4. |
| **Front ↔ servicios** | Stateless: JWT | Son programas hablando con programas. Sin estado por usuario, cualquier instancia de cualquier servicio atiende cualquier pedido con solo tener la clave. Todo lo escalable del módulo 5. |

El servidor de front está **en el medio** y tiene los dos roles a la vez: es servidor stateful hacia adelante y cliente stateless hacia atrás. Y lo que engancha las dos mitades es una idea simple: **los tokens que emiten los servicios se guardan en la sesión del front.** La persona nunca ve un token; el front lo saca de su sesión cada vez que necesita hablar hacia atrás.

🎓 **Si te lo preguntan — ¿qué es una arquitectura de sesiones mixtas?**
Es la combinación de una relación stateful entre el navegador y el servidor de front (sesión y cookie) con una relación stateless entre el servidor de front y los servicios de aplicación (tokens JWT). El front autentica a la persona contra el servicio de autenticación, guarda los tokens obtenidos en la sesión de esa persona, y los adjunta como `Bearer` en cada llamada a los servicios.

---

## 2. 🔴 Las piezas y dónde vive cada una

Hay dos proyectos. Esta tabla es el mapa para leer el resto del módulo:

**Servidor de front** (puerto 8082 en el ejemplo)

| Clase | Qué hace | Módulo donde se explica |
|---|---|---|
| `SecurityConfig` | El portero del front: `formLogin`, rutas públicas, 401/403. **Más un `AuthenticationManager` que usa `CustomAuthProvider`** | 6 + sección 3 |
| `LoginController` | Solo el `GET /login` | 6 |
| **`CustomAuthProvider`** | Reemplaza al `UserDetailsService`: en vez de buscar el usuario en un repositorio, llama al servicio de autenticación, guarda los tokens en sesión y arma las authorities | **sección 3** |
| `GestionAlumnosApiService` | El service que habla con los servicios: `login`, `obtenerTodosLosAlumnos`, `crearAlumno`... por HTTP | secciones 3 y 4 |
| **`WebApiCallerService`** | Cliente HTTP genérico: adjunta el `Bearer` sacándolo de la sesión, y canjea el refresh si el access venció | **secciones 4 y 5** |
| `JwtClaimsReader` | Verifica la firma del access token y lee `rol` y `permisos` de sus claims | sección 3 |
| `AlumnoController`, `AlumnoService` | Igual que en el monolito, salvo que el service ya no tiene repositorio: delega en el `ApiService` | 2, 3 |
| `dto/` | `AlumnoDTO`, `ContactoDTO`, `AuthResponseDTO`, `RefreshTokenDTO`, `Rol`, `Permiso` | 1 |
| `templates/`, `static/` | Sin cambios | 2, 3, 6 |

**Servicio de aplicación** (puerto 8083; en el ejemplo es uno solo que hace de servicio de autenticación y de servicio de alumnos)

| Clase | Qué hace | Módulo |
|---|---|---|
| `SecurityConfig` | Portero **stateless**: sin sesión, sin CSRF, con el filtro JWT adelante | sección 4 |
| **`JwtAuthenticationFilter`** | En cada request: lee `Authorization`, valida el JWT, arma las authorities | **sección 4** |
| `AuthController` | `POST /api/auth` (login → tokens) y `POST /api/auth/refresh` | sección 3 y 5 |
| `LoginService` | Busca el usuario, compara con BCrypt, pide los tokens a `JwtUtil` | 6, 5 |
| `JwtUtil` | Emite y valida JWT | 5 |
| `AlumnoApiController` | `@RestController` con `@PreAuthorize` en cada endpoint | 6 + sección 6 |
| `Usuario`, `Rol`, `Permiso`, `UsuariosRepository` | Los usuarios viven **acá**, no en el front | 6 |

Fijate lo que **no** está en el front: `Usuario`, `UsuariosRepository`, `Alumno`, `AlumnosRepository`. El front no sabe qué es un usuario más allá de "un nombre y un par de tokens en la sesión". Es la forma desacoplada del módulo 1, llevada hasta la seguridad.

---

## 3. 🔴 Flujo 1: el login cruzando las tres zonas

```
  CLIENTE            SERVIDOR DE FRONT                                                     SERVICIO DE AUTENTICACIÓN
  ───────            ─────────────────                                                     ─────────────────────────
  GET /login ──────► LoginController → login.html
  ◄──────────────── pantalla de login

  POST /login ─────► [Spring Security intercepta]
  user + pass          │
                       ▼
                    CustomAuthProvider.authenticate(user, pass)
                       │  ① login(user, pass)
                       ▼
                    GestionAlumnosApiService.login ── POST /api/auth {username, password} ──► AuthController.loginApi
                                                                                                │ LoginService.autenticarUsuario
                                                                                                │   (busca, BCrypt.matches)
                                                                                                │ JwtUtil.generarAccessToken (con rol+permisos)
                                                                                                │ JwtUtil.generarRefreshToken
                                                    ◄── { accessToken, refreshToken } ─────────┘
                       │  ② guardarEnSesion(accessToken, refreshToken, username)
                       │  ③ JwtClaimsReader.leerClaims(accessToken) → rol, permisos
                       │  ④ guardarEnSesion(rol, permisos)
                       │  ⑤ generarAuthorities(rol, permisos)
                       ▼
                    return UsernamePasswordAuthenticationToken(username, password, authorities)
                       │
                    [Spring Security crea la sesión, Set-Cookie, redirige]
  ◄──────────────── 302 /alumnos  +  Set-Cookie: JSESSIONID=...
```

### 3.1 El `SecurityConfig` del front: casi el del módulo 6, más una pieza

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // LA PIEZA NUEVA: "para autenticar, usá este proveedor". Reemplaza al UserDetailsService
    // del módulo 6. Spring Security va a llamar a CustomAuthProvider.authenticate() en cada POST /login.
    @Bean
    public AuthenticationManager authManager(HttpSecurity http, CustomAuthProvider provider) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(provider)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .defaultSuccessUrl("/alumnos", true))
            .logout(logout -> logout
                .logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> res.sendRedirect("/login?unauthorized"))
                .accessDeniedHandler((req, res, e) -> res.sendRedirect("/403")));
        return http.build();
    }
}
```

Todo lo demás es idéntico al módulo 6. El front **sigue siendo stateful**: `formLogin`, sesión, cookie, CSRF activo. Lo único que cambió es **cómo verifica las credenciales**.

### 3.2 `CustomAuthProvider`: el corazón del lado del front

```java
@Component
public class CustomAuthProvider implements AuthenticationProvider {   // la interfaz que Spring Security llama
    private final GestionAlumnosApiService externalAuthService;       // para hablar con el servicio de auth
    private final JwtClaimsReader jwtClaimsReader;                    // para leer el token que vuelve

    public CustomAuthProvider(GestionAlumnosApiService externalAuthService, JwtClaimsReader jwtClaimsReader) {
        this.externalAuthService = externalAuthService;
        this.jwtClaimsReader = jwtClaimsReader;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();                   // lo que escribió en el formulario
        String password = authentication.getCredentials().toString();

        try {
            // ① Delegar la verificación al servicio de autenticación. El front NO tiene usuarios.
            AuthResponseDTO authResponse = externalAuthService.login(username, password);
            if (authResponse == null) {
                throw new BadCredentialsException("Usuario o contraseña inválidos");   // → /login?error
            }

            // ② Guardar los tokens EN LA SESIÓN de esta persona (módulo 4). Acá se enganchan las dos mitades.
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();      // el request actual, obtenido "desde afuera"
                                                                       // porque este método no lo recibe como parámetro
            request.getSession().setAttribute("accessToken", authResponse.getAccessToken());
            request.getSession().setAttribute("refreshToken", authResponse.getRefreshToken());
            request.getSession().setAttribute("username", username);

            // ③ Leer rol y permisos DEL PROPIO TOKEN (vienen como claims: módulo 5). Sin otra llamada.
            Claims claims = jwtClaimsReader.leerClaims(authResponse.getAccessToken());
            Rol rol = Rol.valueOf(claims.get(JwtClaimsReader.CLAIM_ROL, String.class));
            List<Permiso> permisos = ((List<?>) claims.get(JwtClaimsReader.CLAIM_PERMISOS, List.class)).stream()
                    .map(String::valueOf)
                    .map(Permiso::valueOf)
                    .collect(Collectors.toList());

            // ④ También a la sesión, por si alguna pantalla los necesita
            request.getSession().setAttribute("rol", rol);
            request.getSession().setAttribute("permisos", permisos);

            // ⑤ Las authorities, EXACTAMENTE como en el módulo 6: permisos tal cual, rol con ROLE_.
            //    Así @PreAuthorize y sec:authorize del front funcionan sin cambios.
            List<GrantedAuthority> authorities = new ArrayList<>();
            permisos.forEach(permiso -> authorities.add(new SimpleGrantedAuthority(permiso.name())));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.name()));

            // Devolver "autenticado, con estas authorities". Spring Security hace el resto (sesión, cookie, redirect).
            return new UsernamePasswordAuthenticationToken(username, password, authorities);

        } catch (RuntimeException e) {
            throw new BadCredentialsException("Error en el sistema de autenticación: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {           // "sí, sé manejar login por usuario y contraseña"
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
```

> **`RequestContextHolder`:** una forma de obtener el request HTTP actual desde una clase que no lo recibe como parámetro. Spring lo guarda por hilo mientras atiende el pedido. Es lo que permite que `CustomAuthProvider` (que Spring Security llama sin pasarle el request) llegue igual a la sesión.

Y `JwtClaimsReader` es el `parsearClaims` del módulo 5, del lado del front, **con la misma clave**:

```java
@Component
public class JwtClaimsReader {
    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_PERMISOS = "permisos";
    private final Key key;

    public JwtClaimsReader(@Value("${jwt.secret}") String secret) {          // jwt.secret del front...
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));  // ...que TIENE que ser el del back
    }

    public Claims leerClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();  // verifica firma + lee
    }
}
```

### 3.3 La llamada al servicio de autenticación

```java
@Service
public class GestionAlumnosApiService {
    private final WebClient webClient;                 // el cliente HTTP
    private final WebApiCallerService webApiCallerService;   // sección 4
    private final String authServiceUrl;               // de application.properties
    private final String alumnosServiceUrl;

    @Autowired
    public GestionAlumnosApiService(WebApiCallerService webApiCallerService,
                                    @Value("${auth.service.url}") String authServiceUrl,
                                    @Value("${alumnos.service.url}") String alumnosServiceUrl) {
        this.webClient = WebClient.builder().build();
        this.webApiCallerService = webApiCallerService;
        this.authServiceUrl = authServiceUrl;
        this.alumnosServiceUrl = alumnosServiceUrl;
    }

    public AuthResponseDTO login(String username, String password) {
        try {
            return webClient
                    .post()
                    .uri(authServiceUrl + "/auth")                      // POST http://localhost:8083/api/auth
                    .bodyValue(Map.of("username", username, "password", password))   // el cuerpo, en JSON
                    .retrieve()
                    .bodyToMono(AuthResponseDTO.class)                  // la respuesta, convertida al DTO
                    .block();                                           // esperar (síncrono)
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;                                            // credenciales incorrectas → null → BadCredentials
            }
            throw new RuntimeException("Error en el servicio de autenticación: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con el servicio de autenticación: " + e.getMessage(), e);
        }
    }
    // ... obtenerTodosLosAlumnos, crearAlumno, etc.: sección 4
}
```

> **`WebClient`:** el cliente HTTP de Spring para hacer pedidos a otros servidores desde código Java. `.post().uri(...).bodyValue(...).retrieve().bodyToMono(Clase.class).block()` es "hacé un POST a esta URL con este cuerpo, y convertime la respuesta a esta clase".

⚠️ **Este login NO lleva token.** Es la única llamada hacia atrás sin `Bearer`, porque todavía no hay token: es justamente la que lo pide. Por eso `/api/auth` está en `permitAll()` del servicio y excluida del filtro (sección 4).

### 3.4 Del lado del servicio: emitir los tokens

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginService loginService;
    private final JwtUtil jwtUtil;

    @PostMapping                                                    // POST /api/auth
    public ResponseEntity<AuthResponseDTO> loginApi(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Usuario usuario = loginService.autenticarUsuario(username, password);   // busca + BCrypt (módulo 6)

            String accessToken  = loginService.generarAccessToken(usuario);         // con rol y permisos como claims
            String refreshToken = loginService.generarRefreshToken(username);

            return ResponseEntity.ok(AuthResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build());                                          // 200 + { accessToken, refreshToken }
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 404: usuario no existe o clave incorrecta
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    // /refresh: sección 5
}
```

```java
@Service
public class LoginService {
    private final UsuariosRepository usuariosRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Usuario autenticarUsuario(String username, String password) {
        Usuario usuario = usuariosRepository.findByNombreDeUsuario(username)
                .orElseThrow(() -> new NotFoundException("Usuario", username));
        if (!passwordEncoder.matches(password, usuario.getContrasenia())) {    // BCrypt, módulo 6
            throw new NotFoundException("Usuario", username);                    // misma excepción a propósito:
        }                                                                        // no revelar si falló el usuario o la clave
        return usuario;
    }

    public String generarAccessToken(Usuario usuario)   { return jwtUtil.generarAccessToken(usuario); }
    public String generarRefreshToken(String username)  { return jwtUtil.generarRefreshToken(username); }
    // ...
}
```

Nota de diseño: si el usuario no existe **o** la contraseña está mal, se devuelve lo mismo (404). Eso evita que alguien pruebe nombres de usuario y sepa cuáles existen.

```
// ¿CÓMO FUNCIONA? (login completo, admin/1234)
//  1. POST /login al FRONT. Spring Security del front lo intercepta y llama a CustomAuthProvider.authenticate.
//  2. El provider llama a apiService.login("admin","1234") → WebClient hace POST http://localhost:8083/api/auth.
//  3. En el SERVICIO: AuthController → LoginService busca "admin", BCrypt.matches("1234", hash) → ok.
//     JwtUtil emite access (con sub=admin, rol=ADMIN, permisos=[...], exp +15min) y refresh (exp +7d).
//     Responde 200 { accessToken: "eyJ...", refreshToken: "eyJ..." }.
//  4. De vuelta en el FRONT: el provider guarda los dos tokens y el username en la SESIÓN de esta persona.
//  5. Lee el access token con JwtClaimsReader (misma clave → firma ok) → rol=ADMIN, permisos=[CREAR..., EDITAR..., ELIMINAR...].
//  6. Arma authorities ["CREAR_ALUMNOS","EDITAR_ALUMNOS","ELIMINAR_ALUMNOS","ROLE_ADMIN"] y devuelve autenticado.
//  7. Spring Security del front crea la sesión (ya tiene los tokens adentro), manda Set-Cookie: JSESSIONID, 302 a /alumnos.
//
//  Resultado esperado: la persona ve la lista de alumnos. En su navegador hay UNA cookie (JSESSIONID).
//  Los tokens no viajaron nunca al navegador: están en la sesión del front.
//  Con admin/9999: el servicio responde 404 → login devuelve null → BadCredentials → /login?error.
```

---

## 4. 🔴 Flujo 2: una pantalla que trae datos de un servicio

```
  CLIENTE        SERVIDOR DE FRONT                                                         SERVICIO DE ALUMNOS
  ───────        ─────────────────                                                         ───────────────────
  GET /alumnos ─► [Spring Security: cookie → sesión → autenticado, ROLE_ADMIN ✓]
  Cookie:          AlumnoController.listarAlumnos            (@PreAuthorize ✓)
  JSESSIONID         │ alumnoService.obtenerTodosLosAlumnos()
                     │ apiService.obtenerTodosLosAlumnos()
                     │ webApiCallerService.getList("/alumnos", AlumnoDTO.class)
                     │    ├─ accessToken = sesión.get("accessToken")
                     │    └─ GET /api/alumnos  Authorization: Bearer <access> ───────────► [JwtAuthenticationFilter]
                     │                                                                        │ parsearClaims → sub, rol, permisos
                     │                                                                        │ authorities → contexto
                     │                                                                     AlumnoApiController.listar (@PreAuthorize ✓)
                     │                                                                        │ service → repositorio
                     │    ◄──────────────────────────── 200 [ {legajo, nombre,...}, ... ] ────┘
                     │ List<AlumnoDTO>
                     ▼
                   Model → Thymeleaf → alumnos/lista.html
  ◄────────────── HTML con la tabla
```

### 4.1 El front: el service ya no tiene repositorio

```java
@Service
public class AlumnoService {                                   // el mismo nombre que en el monolito...
    @Autowired
    private GestionAlumnosApiService gestionAlumnosApiService;   // ...pero en vez de un repositorio, el ApiService

    public List<AlumnoDTO> obtenerTodosLosAlumnos() {
        return gestionAlumnosApiService.obtenerTodosLosAlumnos();   // delega. No hay entidades, no hay conversión.
    }
    // crearAlumno, actualizarAlumno, eliminarAlumno: igual, delegando
}
```

Y en `GestionAlumnosApiService`, cada operación es una línea sobre el cliente genérico:

```java
public List<AlumnoDTO> obtenerTodosLosAlumnos() {
    List<AlumnoDTO> response = webApiCallerService.getList(alumnosServiceUrl + "/alumnos", AlumnoDTO.class);
    return response != null ? response : List.of();
}

public AlumnoDTO obtenerAlumnoPorLegajo(String legajo) {
    AlumnoDTO response = webApiCallerService.get(alumnosServiceUrl + "/alumnos/" + legajo, AlumnoDTO.class);
    if (response == null) throw new NotFoundException("Alumno", legajo);
    return response;
}

public AlumnoDTO crearAlumno(AlumnoDTO alumnoDTO) {
    return webApiCallerService.post(alumnosServiceUrl + "/alumnos", alumnoDTO, AlumnoDTO.class);
}

public AlumnoDTO actualizarAlumno(String legajo, AlumnoDTO alumnoDTO) {
    return webApiCallerService.put(alumnosServiceUrl + "/alumnos/" + legajo, alumnoDTO, AlumnoDTO.class);
}

public void eliminarAlumno(String legajo) {
    webApiCallerService.delete(alumnosServiceUrl + "/alumnos/" + legajo);
}
```

Fijate: el front le habla al servicio en **REST puro** (`POST`, `PUT`, `DELETE`), mientras que el navegador le habla al front en **GET/POST** (módulo 3). El front traduce. `POST /alumnos/{legajo}/actualizar` desde el navegador se convierte en `PUT /api/alumnos/{legajo}` hacia el servicio.

**Hilo H2 cerrado:** los datos que llegan a la plantilla vienen de otro servicio por HTTP, convertidos a DTOs, sin que el controller ni la plantilla hayan cambiado una línea respecto del monolito.

### 4.2 `WebApiCallerService`: el cliente HTTP con token

Esta es la clase más reutilizable de todo el material. Se escribe una vez y sirve para hablar con cualquier servicio.

```java
@Service
public class WebApiCallerService {
    private final WebClient webClient;
    private final String authServiceUrl;                          // para el refresh (sección 5)

    public WebApiCallerService(@Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = WebClient.builder().build();
        this.authServiceUrl = authServiceUrl;
    }

    // ── LAS OPERACIONES: cada una es "ejecutá esto con el token que corresponda" ────────────────

    public <T> T get(String url, Class<T> responseType) {
        return executeWithTokenRetry(accessToken ->
            webClient.get().uri(url)
                .header("Authorization", "Bearer " + accessToken)   // ← EL token, en la cabecera (módulo 5)
                .retrieve().bodyToMono(responseType).block());
    }

    public <T> List<T> getList(String url, Class<T> responseType) {
        return executeWithTokenRetry(accessToken ->
            webClient.get().uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve().bodyToFlux(responseType).collectList().block());   // Flux: una lista
    }

    public <T> T post(String url, Object body, Class<T> responseType) {
        return executeWithTokenRetry(accessToken ->
            webClient.post().uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(body)
                .retrieve().bodyToMono(responseType).block());
    }

    public <T> T put(String url, Object body, Class<T> responseType) { /* igual, con .put() */ }

    public void delete(String url) { /* igual, con .delete() y bodyToMono(Void.class) */ }

    // ── EL CORAZÓN: ejecutar con el token de la sesión, y reintentar si venció (sección 5) ────────

    public <T> T executeWithTokenRetry(ApiCall<T> apiCall) {
        String accessToken  = getAccessTokenFromSession();        // de la sesión de ESTA persona
        String refreshToken = getRefreshTokenFromSession();
        if (accessToken == null) throw new RuntimeException("No hay token de acceso disponible");

        try {
            return apiCall.execute(accessToken);                  // primer intento
        } catch (WebClientResponseException e) {
            if ((e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN)
                    && refreshToken != null) {
                AuthResponseDTO newTokens = refreshToken(refreshToken);   // canjear (sección 5)
                return apiCall.execute(newTokens.getAccessToken());       // segundo intento, token nuevo
            }
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) throw new NotFoundException(e.getMessage());
            throw new RuntimeException("Error en llamada al API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con el servicio: " + e.getMessage(), e);
        }
    }

    // ── LA SESIÓN: leer y actualizar tokens (módulo 4, con RequestContextHolder de la sección 3) ──

    private String getAccessTokenFromSession() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return (String) request.getSession().getAttribute("accessToken");
    }

    private String getRefreshTokenFromSession() { /* igual, con "refreshToken" */ }

    private void updateTokensInSession(String accessToken, String refreshToken) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        request.getSession().setAttribute("accessToken", accessToken);     // PISA los viejos
        request.getSession().setAttribute("refreshToken", refreshToken);
    }

    private AuthResponseDTO refreshToken(String refreshToken) { /* sección 5 */ }

    @FunctionalInterface
    public interface ApiCall<T> { T execute(String accessToken) throws Exception; }   // "una llamada que necesita un token"
}
```

> **`ApiCall<T>`:** una interfaz funcional: un pedazo de código que recibe un token y devuelve un resultado. Cada `get`, `post`, etc. arma uno con una lambda y se lo pasa a `executeWithTokenRetry`, que es quien sabe **qué** token darle y **qué** hacer si falla. Separa el "qué pedir" del "cómo autenticarlo".

### 4.3 El servicio: el portero stateless y el filtro

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)                       // sin sesión no hay CSRF que proteger
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   // NUNCA crear sesión
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/api/auth", "/api/auth/refresh").permitAll();   // las dos únicas rutas sin token
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
            // ↑ MI filtro, antes del de Spring: es quien pone "quién es" a partir del token
        return http.build();
    }
}
```

Compará con el `SecurityConfig` del front: **no hay `formLogin`, no hay `logout`, no hay redirects.** Un servicio no tiene pantallas: responde 401/403 (tabla 1.2 del módulo 6, columna API). Y `STATELESS` significa lo que dice: aunque alguien mande una cookie, este servidor no la mira.

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {     // se ejecuta UNA vez por request
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");            // ① leer la cabecera
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);                        // ② sacar el token ("Bearer " son 7 caracteres)
            try {
                Claims claims = jwtUtil.parsearClaims(token);          // ③ validar firma y vencimiento (módulo 5)

                if (JwtUtil.TYPE_REFRESH.equals(claims.get(JwtUtil.CLAIM_TYPE))) {   // ④ ¿es un refresh? no sirve acá
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                    return;
                }

                String username = claims.getSubject();                 // ⑤ quién es
                List<GrantedAuthority> authorities = construirAuthorities(claims);   // ⑥ qué puede

                var auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);   // ⑦ "para ESTE request, el usuario es este"
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");   // firma mala o vencido → 401
                return;
            }
        }
        filterChain.doFilter(request, response);                       // ⑧ seguir: al controller (o al 401 si no hubo token)
    }

    private List<GrantedAuthority> construirAuthorities(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        String rol = claims.get(JwtUtil.CLAIM_ROL, String.class);
        if (rol != null) authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));          // el prefijo, otra vez
        List<?> permisos = claims.get(JwtUtil.CLAIM_PERMISOS, List.class);
        if (permisos != null) permisos.forEach(p -> authorities.add(new SimpleGrantedAuthority(String.valueOf(p))));
        return authorities;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {    // en estas rutas el filtro no corre
        String path = request.getRequestURI();
        return path.equals("/api/auth") || path.equals("/api/auth/refresh");
    }
}
```

> **`SecurityContextHolder`:** el lugar donde Spring Security guarda "quién está autenticado" **durante este request**. En el front lo llena la sesión; en el servicio lo llena este filtro a partir del token, y se vacía cuando el request termina. Es lo que hace que `@PreAuthorize` funcione igual en los dos lados.

Y el controller del servicio, con la misma anotación del módulo 6:

```java
@RestController
@RequestMapping("/api")
public class AlumnoApiController {

    @GetMapping("/alumnos")
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMIN')")                 // evalúa las authorities que puso el filtro
    public ResponseEntity<List<AlumnoDTO>> listar() { /* ... */ }

    @PostMapping("/alumnos")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('CREAR_ALUMNOS')")
    public ResponseEntity<AlumnoDTO> crear(@RequestBody AlumnoDTO dto) { /* ... */ }

    @PutMapping("/alumnos/{legajo}")
    @PreAuthorize("hasAuthority('EDITAR_ALUMNOS')")
    public ResponseEntity<AlumnoDTO> actualizar(@PathVariable String legajo, @RequestBody AlumnoDTO dto) { /* ... */ }

    @DeleteMapping("/alumnos/{legajo}")
    @PreAuthorize("hasAuthority('ELIMINAR_ALUMNOS')")
    public ResponseEntity<Void> eliminar(@PathVariable String legajo) { /* ... */ }
}
```

```
// ¿CÓMO FUNCIONA? (GET /alumnos, con el admin ya logueado)
//  FRONT:
//   1. Llega GET /alumnos con Cookie: JSESSIONID. Spring Security del front encuentra la sesión → ROLE_ADMIN → pasa.
//   2. AlumnoController (@PreAuthorize hasAnyRole ✓) → AlumnoService → ApiService → WebApiCallerService.getList.
//   3. getAccessTokenFromSession() saca "eyJ..." de la sesión. Hace GET http://localhost:8083/api/alumnos
//      con Authorization: Bearer eyJ...
//  SERVICIO:
//   4. JwtAuthenticationFilter: lee la cabecera, parsearClaims → sub=admin, rol=ADMIN, permisos=[...]. Firma ok, no vencido.
//      No es refresh. Arma authorities, las pone en el SecurityContext.
//   5. anyRequest().authenticated() ✓. AlumnoApiController.listar: @PreAuthorize hasAnyRole('DOCENTE','ADMIN') ✓.
//   6. Devuelve 200 con el JSON de alumnos. El servicio NO guardó nada.
//  FRONT:
//   7. bodyToFlux(AlumnoDTO.class).collectList() → List<AlumnoDTO>. Sube hasta el controller.
//   8. Model → Thymeleaf → lista.html → HTML → navegador.
//
//  Resultado esperado: la tabla con Juan Pérez y María González, exactamente como en el módulo 2.
//  Solo que ahora los datos cruzaron dos procesos y un token. Y si en el paso 4 el token estuviera
//  vencido: sección 5.
```

---

## 5. 🔴 El refresh automático: la persona nunca se entera

Pasaron 16 minutos. El access token venció. La persona hace clic en "Ver detalle".

```java
// En WebApiCallerService.executeWithTokenRetry (sección 4.2), el catch:
catch (WebClientResponseException e) {
    if ((e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN)
            && refreshToken != null) {
        AuthResponseDTO newTokens = refreshToken(refreshToken);     // ① canjear
        return apiCall.execute(newTokens.getAccessToken());         // ② reintentar con el nuevo
    }
    // ...
}

private AuthResponseDTO refreshToken(String refreshToken) {
    RefreshTokenDTO refreshRequest = RefreshTokenDTO.builder().refreshToken(refreshToken).build();
    AuthResponseDTO response = webClient
            .post()
            .uri(authServiceUrl + "/auth/refresh")                 // POST /api/auth/refresh { refreshToken }
            .bodyValue(refreshRequest)
            .retrieve()
            .bodyToMono(AuthResponseDTO.class)
            .block();
    updateTokensInSession(response.getAccessToken(), response.getRefreshToken());   // ③ pisar en la sesión
    return response;
}
```

Del lado del servicio de autenticación:

```java
@PostMapping("/refresh")                                                // POST /api/auth/refresh
public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
    try {
        Claims claims = jwtUtil.parsearClaims(request.getRefreshToken());    // ¿firma ok? ¿no vencido (7 días)?
        if (!JwtUtil.TYPE_REFRESH.equals(claims.get(JwtUtil.CLAIM_TYPE))) {  // ¿es realmente un refresh?
            return ResponseEntity.badRequest().build();                       // un access no sirve para canjear
        }
        String username = claims.getSubject();
        String newAccessToken = loginService.generarAccessTokenPorUsername(username);   // ← relee el usuario
        return ResponseEntity.ok(new TokenResponse(newAccessToken, request.getRefreshToken()));
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
```

```java
// En LoginService:
public String generarAccessTokenPorUsername(String username) {
    Usuario usuario = usuariosRepository.findByNombreDeUsuario(username)
            .orElseThrow(() -> new NotFoundException("Usuario", username));
    return jwtUtil.generarAccessToken(usuario);        // rol y permisos ACTUALES: si cambiaron, el nuevo token los trae
}
```

```
// ¿CÓMO FUNCIONA? (minuto 16, GET /alumnos/A001)
//  1. Front: getAccessTokenFromSession → el access de hace 16 min. GET /api/alumnos/A001 con ese Bearer.
//  2. Servicio: el filtro hace parsearClaims → "exp" ya pasó → excepción → 401.
//  3. Front: WebClient lanza WebClientResponseException con 401. executeWithTokenRetry lo atrapa.
//     Hay refresh en sesión → refreshToken(refresh): POST /api/auth/refresh { refreshToken }.
//  4. Servicio: parsearClaims(refresh) → firma ok, no vencido (le quedan 6 días), type=refresh ✓.
//     Relee al usuario "admin", emite un access nuevo (+15 min). Responde { accessToken nuevo, el mismo refresh }.
//  5. Front: updateTokensInSession pisa el access viejo. apiCall.execute(access nuevo) → GET /api/alumnos/A001 otra vez.
//  6. Servicio: filtro → claims ok → controller → 200 con el alumno.
//  7. Front: renderiza el detalle.
//
//  Resultado esperado: la persona hizo un clic y vio el detalle. No supo que hubo dos pedidos y un canje.
//  La segunda mitad del hilo H7, cerrada: la sesión "no expira" aunque el access dure 15 minutos.
```

Notá el detalle de `generarAccessTokenPorUsername`: **relee al usuario**. Es la respuesta al trade-off del módulo 5 (claims en el token contra datos frescos): con claims, si le cambian los permisos a alguien, su token viejo sigue diciendo los permisos viejos... **pero solo hasta el próximo refresh**, que como máximo es en 15 minutos. Ese es el compromiso: una llamada menos por request, a cambio de hasta 15 minutos de desfase en los permisos. Justificable.

---

## 6. 🔴 Protección en los dos lados, siempre

Volvé al plano de la sección 1. Hay **dos porteros**, y los dos son obligatorios:

| Portero | Protege | Contra qué |
|---|---|---|
| **Front** (`SecurityConfig` + `@PreAuthorize` + `sec:authorize`, módulo 6) | Las pantallas | Una persona que escribe URLs en la barra |
| **Servicio** (`SecurityConfig` stateless + `JwtAuthenticationFilter` + `@PreAuthorize`) | Los endpoints REST | **Cualquiera** que le pegue directo al servicio |

Ese "cualquiera" es la razón de fondo. Si el servicio está desplegado en algún lado accesible, sus URLs son públicas: `http://tu-servicio/api/alumnos` la puede escribir cualquier persona en Postman, **sin pasar por tu front**. Si el servicio no valida el token, está abierto al mundo. Proteger solo el front es proteger la puerta de adelante con la de atrás sin llave.

> 🕳️ **Madriguera — red privada como alternativa**
> Otra forma de cerrar la puerta de atrás es que los servicios no sean accesibles desde internet, solo desde el front, dentro de una red privada. Es una decisión de despliegue, no de código, y se ve en las unidades de infraestructura. Aun así, validar el token es la práctica estándar.
> *Volvé al camino — esto se profundiza aparte, otro día.*

Y el tercer caso del módulo 5: cuando un servicio le habla a **otro servicio** por su cuenta (sin persona en el medio), no manda el JWT de nadie: manda su **API token**. Ese tramo no pasa por nada de este módulo.

---

## 7. 🔴 La configuración: donde todo funciona o falla en silencio

Dos archivos `application.properties`. Los errores acá no tiran excepción clara: simplemente los tokens "no validan".

**Front** (`front-server/src/main/resources/application.properties`):

```properties
spring.application.name=front-server
server.port=8082                                  # el puerto del FRONT en el ejemplo. En tu TP, uno que no
                                                  # choque con tus servicios (módulo 0: por ejemplo 8090)

auth.service.url=http://localhost:8083/api        # dónde está el servicio de autenticación
alumnos.service.url=http://localhost:8083/api     # dónde está el de alumnos (en el ejemplo, el mismo)

# TIENE QUE SER IDÉNTICA a la del servicio que emite los tokens. Con otra clave, JwtClaimsReader
# rechaza el access token en el login y nadie puede entrar. En producción: variable de entorno.
jwt.secret=${JWT_SECRET:una-clave-larga-secreta-y-compartida-de-al-menos-32-caracteres}
```

**Servicio** (`.../application.properties`):

```properties
spring.application.name=gestion-alumnos-server
server.port=8083

# LA MISMA. Exactamente la misma. Este es el que firma.
jwt.secret=${JWT_SECRET:una-clave-larga-secreta-y-compartida-de-al-menos-32-caracteres}
```

| Síntoma | Causa | Dónde mirar |
|---|---|---|
| El login del front siempre da "usuario o contraseña inválidos", pero Postman contra `/api/auth` anda | `jwt.secret` distinto entre front y servicio: `JwtClaimsReader` rechaza la firma | Los dos `properties`, byte a byte |
| Todo pedido hacia atrás da 401, incluso recién logueado | El servicio tiene otra clave que el que emitió | Ídem, en cada servicio que valida |
| "Error de conexión con el servicio de autenticación" | La URL o el puerto están mal, o el servicio no está levantado | `auth.service.url`, y que el servicio esté corriendo |
| Anda en tu máquina y no en la del compañero | `JWT_SECRET` definida como variable de entorno en una sola de las dos | El `${JWT_SECRET:...}` toma la variable si existe |

⚠️ **En el TP hay un servicio que emite y cuatro que validan. Los cinco con la misma clave.** Una sola distinta y ese servicio rechaza todo, sin decir por qué.

---

## 8. 🟡 Sesgar resultados: cómo llega "quién es" hasta el service

Del módulo 6, sección 10: una entidad beneficiaria ve **sus** donaciones, no todas. `@PreAuthorize` no alcanza. Hace falta que el service sepa **quién pide**.

En el servicio, el filtro ya puso al usuario en el `SecurityContext` (sección 4.3). Un controller lo recibe pidiendo `Authentication` como parámetro:

```java
@GetMapping("/user/roles-permisos")
public ResponseEntity<UserRolesPermissionsDTO> getUserRolesAndPermissions(Authentication authentication) {
    String username = authentication.getName();          // el "sub" del token que el filtro validó
    return ResponseEntity.ok(loginService.obtenerRolesYPermisosUsuario(username));
}
```

Ese `authentication.getName()` es el eslabón: el controller se lo pasa al service, y el service filtra: *"dame las donaciones cuya beneficiaria sea la del usuario `username`"*. Identificar al usuario no es solo para dejarlo pasar: es para **que la respuesta sea la suya**. El mismo endpoint, el mismo `@PreAuthorize`, y dos beneficiarias reciben listas distintas.

(Ese endpoint concreto ya no hace falta llamarlo tras el login, porque rol y permisos vienen en los claims. Queda como ejemplo de cómo se lee la identidad en un servicio.)

---

## 9. 🔴 Traducción a DonaTrack

### 9.0 El diagrama de despliegue del TP, leído con este módulo

El enunciado del TP trae un diagrama de despliegue con cinco zonas. Es el plano de la sección 1 con los nombres de DonaTrack:

```
                       ┌─ Autenticación ─────────────────────┐
                       │  Servidor de Autenticación           │
                       │    (Servicio Autenticación)          │
                       └────────────────▲─────────────────────┘
                                        ┆ (el front depende de él: sección 3)
  ┌─ Computadora ──┐   ┌─ FrontEnd y Orquestación ─┐         ┌─ Dominio ────────────────────────────────┐
  │  Personal      │   │  Servidor Frontend         │  HTTP   │  Servidor Gestión de Donantes, Donaciones │
  │  Navegador Web ├─HTTPS─►  App Frontend          ├─API REST─►  y Entidades Beneficiarias                │──TCP/IP──┐
  └────────────────┘   │                            ├─API REST─►  Servidor Logística y Gestión de Camiones │──TCP/IP──┤   ┌─ Persistencia ──────┐
                       │                            ├─API REST─►  Servidor de Incentivos a Donantes        │──TCP/IP──┼──►│ Servidor de Base de  │
                       │                            ├─API REST─►  Servidor de Notificaciones               │──TCP/IP──┘   │ Datos (MySQL 8)      │
                       └────────────────────────────┘         └───────────────────────────────────────────┘             └──────────────────────┘
```

| Zona del diagrama | Qué es en este módulo |
|---|---|
| **Computadora Personal → Servidor Frontend**, por **HTTPS** | El tramo stateful: sesión y cookie (módulos 4 y 6). El HTTPS es lo que protege la cookie en tránsito. |
| **FrontEnd y Orquestación** | El servidor de front desacoplado. "Orquestación" es la palabra exacta: arma cada pantalla combinando lo que le devuelven varios servicios (módulo 1, sección 3.2). |
| **Servidor Frontend → Servidor de Autenticación** (línea de dependencia) | El `POST /auth` del login y el `POST /auth/refresh` del canje (secciones 3 y 5). Es la única dependencia del front que no es "API REST de dominio". |
| **Servidor Frontend → cada servidor de Dominio**, por **HTTP - API REST** | `WebApiCallerService` con `Bearer` (sección 4). Cuatro líneas, cuatro `ApiService`. |
| **Dominio → Persistencia** | Fuera de esta unidad. Pero notá lo que **no** está dibujado: ninguna línea de los servicios de Dominio hacia Autenticación. Validan el token **solos**, con la clave compartida (sección 7). El diagrama lo confirma. |

Todo el ejemplo, pieza por pieza, con los nombres del TP:

| En el ejemplo | En DonaTrack |
|---|---|
| `gestion-alumnos-front-server` | **`front-server`**, un módulo nuevo del multi-módulo (módulo 0) |
| `gestion-alumnos-server` haciendo de servicio de autenticación | **Servicio de autenticación**, un servicio **nuevo**: `Usuario`, `Rol`, `Permiso`, `UsuariosRepository`, `LoginService`, `JwtUtil`, `AuthController` con `/auth` y `/auth/refresh` |
| `gestion-alumnos-server` haciendo de servicio de alumnos | **Cada servicio existente** (donaciones, logística, incentivos, notificaciones): a cada uno se le agrega `SecurityConfig` stateless + `JwtAuthenticationFilter` + `JwtUtil` (solo para validar) + `@PreAuthorize` en sus endpoints. **Todos con la misma `jwt.secret`.** |
| `Rol { ADMIN, DOCENTE }` | Los roles del dominio: por ejemplo `DONANTE`, `ENTIDAD_BENEFICIARIA`, `ADMIN` |
| `Permiso { CREAR_ALUMNOS, ... }` | Las acciones concretas: `CREAR_NECESIDAD`, `CONFIRMAR_RECEPCION`, `ASIGNAR_DONACION`, `IMPORTAR_CSV`... |
| `GestionAlumnosApiService` | **Un `ApiService` por servicio de destino**: `DonacionesApiService`, `LogisticaApiService`... cada uno con su URL en `properties`. Todos usan el mismo `WebApiCallerService`. |
| `WebApiCallerService` | **Se copia tal cual.** Es genérico. |
| `CustomAuthProvider`, `JwtClaimsReader` | Se copian con los enums de DonaTrack |
| `AlumnoDTO`, `ContactoDTO` en el front | Un DTO por cada cosa que el front muestra o envía, **espejo** del que expone cada servicio |
| `templates/alumnos/` | Tus maquetas, una carpeta por recurso, con `sec:authorize` según rol |

### 9.1 El desafío propio del TP: usuario ↔ entidad de negocio

En el ejemplo, un `Usuario` es solo un login con rol. En DonaTrack, el usuario con rol `DONANTE` **es** un donante que existe en el servicio de donaciones, y el usuario `ENTIDAD_BENEFICIARIA` **es** una entidad beneficiaria. Hace falta un vínculo: el `Usuario` del servicio de autenticación tiene que saber **a qué donante o entidad corresponde** (un identificador que apunte al servicio dueño de esa entidad), y ese identificador conviene que viaje como claim en el access token. Es lo que permite, en la sección 8, que el servicio de donaciones filtre "las donaciones de *esta* beneficiaria" a partir del token. Diseñar ese vínculo, y justificarlo, es parte de la entrega.

### 9.2 Orden de construcción sugerido

1. El módulo `front-server` levantado, con las maquetas como plantillas heredando de un layout (módulos 0, 2).
2. Las pantallas conectadas a los servicios **sin seguridad**: `ApiService` + `WebApiCallerService` sin token, o con los servicios temporalmente abiertos. Que el punta a punta ande.
3. Sesión y cookie en el front (`SecurityConfig` + `login.html`, módulo 6) contra el servicio de autenticación nuevo (`CustomAuthProvider`).
4. Tokens hacia atrás: `JwtAuthenticationFilter` en cada servicio, `Bearer` en `WebApiCallerService`, `@PreAuthorize` en los endpoints, `sec:authorize` en las plantillas.
5. Recién al final, lo opcional (más pantallas, detalles).

Cada paso deja algo que funciona. No se pasa al siguiente hasta que el anterior anda.

🧩 **En tu TP** — prioridad: **el punta a punta antes que la cantidad de pantallas.** Una pantalla de lista y una de alta que crucen navegador → front (con login) → servicio (con token) → y vuelvan, valen más que veinte pantallas sin el circuito completo. Y todo el material de estos ocho módulos está en ese circuito.

---

## 10. 🟢 Cuando no anda: errores de sesiones mixtas

| Lo que ves | Qué es | Qué revisar |
|---|---|---|
| Login del front falla, Postman contra `/api/auth` anda | Claves JWT distintas | Sección 7 |
| 401 en todo lo que va hacia atrás | Clave distinta en ese servicio, o el filtro no está en `addFilterBefore`, o `/api/auth` no está en `shouldNotFilter` | `SecurityConfig` y filtro del servicio |
| El servicio "pide login" con una pantalla de Spring | Falta `SessionCreationPolicy.STATELESS` o hay `formLogin` en el servicio | Un servicio no tiene formLogin |
| 403 en un `POST` del front hacia el servicio, con el token correcto | CSRF activo en el servicio | `.csrf(AbstractHttpConfigurer::disable)` en el servicio (**no** en el front) |
| `Rol.valueOf` explota en el login | El claim `rol` del token no coincide con el enum del front | Los enums del front y del servicio tienen que tener los mismos nombres |
| Después de 15 minutos todo da error en vez de refrescar | El `catch` no atrapa 401/403, o no hay refresh en sesión, o `/api/auth/refresh` no está en `permitAll()` | `executeWithTokenRetry` y la config del servicio |
| Se pierde el login al cambiar de pantalla | La sesión del front no se está creando: `STATELESS` puesto por error en el front | El front es stateful: sin esa línea |

---

## Checkpoint del módulo 7

Sin respuestas.

1. Dibujá el plano de sesiones mixtas con las tres zonas. ¿Qué viaja en cada tramo y quién guarda qué?
2. ¿Por qué el tramo navegador ↔ front es stateful y el tramo front ↔ servicios es stateless? Una razón para cada uno.
3. ¿Dónde se guardan el access token y el refresh token? ¿Los ve alguna vez el navegador?
4. ¿Qué reemplaza `CustomAuthProvider` respecto del módulo 6, y en qué se parece?
5. ¿Por qué el `POST /api/auth` es la única llamada hacia atrás sin `Bearer`? ¿Qué dos lugares de la config del servicio lo permiten?
6. Explicá qué hace `executeWithTokenRetry` cuando el servicio responde 401. ¿Cuántos pedidos HTTP termina haciendo?
7. ¿Qué hace `JwtAuthenticationFilter` en cada request y dónde deja el resultado? ¿Por qué después `@PreAuthorize` funciona igual que en el front?
8. Un compañero protege el front con `@PreAuthorize` y deja los servicios sin filtro "porque nadie los conoce". ¿Qué está mal?
9. ¿Por qué la clave JWT tiene que ser idéntica en el servicio de autenticación, en los cuatro servicios y en el front? ¿Qué pasa en cada lugar si no lo es?
10. En DonaTrack, ¿cómo hacés que una entidad beneficiaria vea solo sus donaciones? Nombrá las piezas que intervienen, desde el token hasta el service.

---

## Cómo seguir

No hay módulo 8. La clase desde cero termina acá, y con ella los siete hilos están cerrados:

| Hilo | Cerrado en |
|---|---|
| H1 — HTML repetido | 2 |
| H2 — de dónde salen los datos | 2 → 7 |
| H3 — formularios y el mensaje que se perdía | 3 |
| H4 — HTTP no tiene memoria | 4 |
| H5 — rutas públicas | 6 |
| H6 — el front no tiene los datos | 7 |
| H7 — el costo de la sesión y la alternativa | 5 → 7 |

Lo que sigue, en orden:

1. **Los checkpoints.** Las preguntas de los ocho módulos, respondidas. Lo que no salga es la duda para el chat; lo que sí salga, va al complemento.
2. **El TP.** El orden de construcción de 9.2, con el material al lado. Cada pieza que escribas tiene su módulo.
3. **El apunte maestro**, que se genera después de esto: se va a leer como repaso, porque el entendimiento ya está.

---

**FIN DEL MÓDULO 7**
