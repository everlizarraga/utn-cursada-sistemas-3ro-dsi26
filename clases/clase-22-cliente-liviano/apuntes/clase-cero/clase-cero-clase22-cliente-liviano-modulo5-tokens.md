# Clase desde cero — Cliente Liviano — Módulo 5
# Tokens: cuando el servidor no se acuerda de nada

**Fase C — Identidad** · Densidad general: 🔴 (stateless vs stateful es definición de examen; JWT y el par access/refresh son el corazón de la Entrega 5)

---

## Sobre este documento

**Cubre:** las definiciones de **stateful** y **stateless**; qué es un **token** y en qué se diferencia de una cookie; la estructura interna de un **JWT** y qué garantiza su firma; cómo se genera y se valida uno en código; por qué existen un **access token** y un **refresh token**; qué se gana con esta arquitectura (escalar) y qué se pierde (cerrar sesión); cómo un servidor stateless procesa cada pedido; y la diferencia entre un token de usuario y un **API token** entre servicios. Se cierra la primera mitad del **hilo H7**.

**NO cubre:** cómo Spring Security intercepta pedidos y protege rutas (módulo 6); el código del filtro que valida el token en cada request del backend, ni cómo el servidor de front guarda y usa los tokens (7). Acá se ve el token en sí; en el 7, el token circulando por la arquitectura completa.

## De dónde venís

- **Módulo 4:** la sesión es estado del servidor por persona; la cookie lleva solo un ID; cerrar sesión es borrar la fila; y dos costos quedaron marcados: memoria por usuario, y estado atado a una máquina.
- **De clases anteriores:** cabeceras HTTP; JSON; qué es una función de hash (una función que produce una huella fija a partir de cualquier contenido, y cambia por completo si el contenido cambia una letra); la anotación `@Value` para leer un valor de `application.properties`.

---

## 1. 🔴 Stateful y stateless: las dos definiciones

Lo del módulo 4 tiene nombre:

> **Arquitectura web stateful:** el servidor **guarda información del contexto** de la conversación con cada cliente entre un pedido y el siguiente. Ese contexto es la sesión. Cada pedido se interpreta a la luz de lo que el servidor recuerda.

> **Arquitectura web stateless:** cada pedido se procesa **de forma independiente**, sin información de pedidos anteriores. El servidor **no guarda nada** entre requests. Toda la información necesaria para atender el pedido **viaja dentro del pedido**.

| | Stateful | Stateless |
|---|---|---|
| ¿El servidor recuerda al cliente entre pedidos? | Sí (sesión) | No |
| ¿Dónde está la información de quién sos? | En el servidor; el cliente lleva solo un ID | **En el pedido**, dentro de un token |
| ¿Qué guarda el servidor por cada persona conectada? | Una fila en la tabla de sesiones | **Nada** |
| Herramienta | Sesión + cookie | Token |

La diferencia se ve en qué hace el servidor cuando le llega `GET /alumnos`:

- **Stateful:** lee la cookie, **busca** en su tabla, encuentra quién sos.
- **Stateless:** lee el token que vino en el pedido, lo **verifica**, y **adentro del token** está quién sos. No busca nada. No tiene dónde buscar.

🎓 **Si te lo preguntan — ¿qué diferencia hay entre una arquitectura web stateful y una stateless?**
En una arquitectura stateful el servidor mantiene el estado de la conversación con el cliente entre requests (sesión), y el cliente envía un identificador (cookie) para que el servidor lo recupere. En una stateless el servidor no guarda información entre requests: cada pedido es independiente y lleva en sí toda la información necesaria, típicamente en un token.

---

## 2. 🔴 El token: la credencial que viaja con cada pedido

Un **token** es un dato que el servidor le entrega al cliente al loguearse y que el cliente **adjunta a cada pedido siguiente** para demostrar quién es. Hasta ahí se parece a la cookie. La diferencia está en el contenido:

| | Cookie (stateful) | Token (stateless) |
|---|---|---|
| Qué lleva | Un **ID** para buscar en el servidor | **La información misma**: quién sos, qué rol tenés, hasta cuándo vale |
| Qué hace el servidor al recibirla | Busca la sesión | Verifica que el token sea auténtico y **lee** lo que trae |
| Si el servidor se reinicia | Las sesiones en memoria se pierden | No pasa nada: el servidor no tenía nada que perder |
| Cómo viaja | Cabecera `Cookie`, automática | Cabecera `Authorization: Bearer <token>`, la manda el cliente explícitamente |

El flujo, lado a lado con el del módulo 4:

```
   STATEFUL (módulo 4)                                    STATELESS (este módulo)
   ────────────────────                                   ─────────────────────
   POST /login  usuario+clave                             POST /login  usuario+clave
   ◄── 200 OK                                             ◄── 200 OK
       Set-Cookie: session=7f3a9c                             { "token": "eyJhbGciOi...JWT..." }
       (el servidor creó una fila)                            (el servidor NO creó nada)

   GET /alumnos                                           GET /alumnos
       Cookie: session=7f3a9c                                 Authorization: Bearer eyJhbGciOi...
       → el servidor BUSCA 7f3a9c                             → el servidor VERIFICA el token
       → encuentra {admin, ADMINISTRADOR}                     → LEE de adentro {admin, ADMINISTRADOR}
   ◄── HTML                                               ◄── respuesta
```

Fijate la cabecera: `Authorization: Bearer <token>`. *Bearer* significa "portador": quien porta este token tiene los derechos que el token dice. Es la forma estándar de mandar un token en HTTP, y es lo que el servidor de front va a poner en cada pedido hacia los servicios en el módulo 7.

Hay una pregunta obvia que este esquema plantea: si toda la información viaja en el token y el servidor no guarda nada, **¿cómo sabe que el token es auténtico y no uno que la persona escribió a mano diciendo "soy administrador"?** Esa es la sección 3.

---

## 3. 🔴 JWT por dentro: tres partes y una firma

**JWT** (*JSON Web Token*) es el formato de token estándar. Es un texto con **tres partes separadas por puntos**:

```
   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 . eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0 . hgql5fQjShY1eqAdLKj9TBMzi1t8rV6Csl2nEsvFlnk
   └────────────── ① ───────────────┘   └───────────────────────────────── ② ─────────────────────────────────────┘   └──────────────── ③ ────────────────┘
                HEADER                                                  PAYLOAD                                                        FIRMA
```

Ese token es real: pegalo en **jwt.io** (una página que decodifica y verifica JWT, y sirve para mirar cualquier token que te cruces) y vas a ver las tres partes separadas por color. Fue firmado con el secreto `secret?idontknowanysecret`; si escribís ese secreto en el campo *Secret*, la firma verifica; si cambiás una letra, deja de verificar. Es la sección 3.1 hecha a mano.

Las tres partes están codificadas en Base64 para que viajen como texto plano sin caracteres raros. Decodificadas, se ven así:

**① Header — cómo está firmado:**
```json
{ "alg": "HS256", "typ": "JWT" }
```
El algoritmo que se usó para la firma, y opcionalmente el tipo. Nada más. (El servidor de la sección 4 emite el header sin `typ`; es válido igual.)

**② Payload del ejemplo de jwt.io:**
```json
{ "sub": "1234567890", "name": "John Doe", "admin": true, "iat": 1516239022 }
```

**② Payload que emite el servidor de la sección 4** — los *claims*, la información. Es el que importa para el TP:
```json
{
  "sub": "admin",                      // subject: de quién es el token (el usuario)
  "iss": "gestion-alumnos-server",     // issuer: quién lo emitió
  "rol": "ADMIN",                      // claim propio: el rol
  "permisos": ["CREAR_ALUMNOS", "EDITAR_ALUMNOS", "ELIMINAR_ALUMNOS"],   // claim propio: los permisos
  "iat": 1726598100,                   // issued at: cuándo se emitió (fecha en segundos)
  "exp": 1726599000                    // expiration: hasta cuándo vale
}
```

> **Claim:** cada par clave-valor del payload. Hay claims estándar (`sub`, `iss`, `iat`, `exp`) y podés agregar los que necesites (`rol`, `permisos`, un `id`). Es la "información misma" de la sección 2: todo lo que el servidor va a necesitar para atender el pedido sin buscar nada.

**③ Firma — la garantía:**
```
HMAC-SHA256( base64(header) + "." + base64(payload), CLAVE_SECRETA )
```
Un hash del header y el payload, calculado con una **clave secreta que solo conoce el servidor**.

### 3.1 Qué garantiza la firma

Cuando el servidor recibe un JWT, vuelve a calcular la firma con **su** clave secreta sobre el header y el payload que llegaron, y compara con la firma que vino en el token:

- Si **coinciden**: el payload es exactamente el que se firmó (nadie tocó ni una letra) **y** lo firmó alguien que tiene la clave secreta, o sea, el servidor. Se confía en lo que dice.
- Si **no coinciden**: alguien modificó el payload, o el token lo fabricó alguien sin la clave. Se rechaza.

Eso responde la pregunta de la sección 2. Alguien puede escribir a mano un payload que diga `"rol": "ADMIN"`, pero **no puede producir la firma correcta sin la clave secreta**. Y el servidor no guardó nada: solo necesita su clave.

```
   token que llega:   header . payload . firma_recibida
                                    │
   el servidor:       HMAC(header.payload, CLAVE_SECRETA) = firma_calculada
                                    │
                      firma_calculada == firma_recibida  ?  → ✅ auténtico e íntegro
                                                          :  → ❌ rechazado
   Además:            ¿exp ya pasó?  → ❌ vencido
```

⚠️ **La firma garantiza que el contenido no fue modificado. No lo oculta.** El payload está en Base64, que es codificación, no cifrado: cualquiera que tenga el token puede decodificarlo y leer los claims. Por eso en un JWT **no van contraseñas ni datos que no puedan ser leídos**: va lo necesario para identificar y autorizar (usuario, rol, permisos, vencimiento). Un token es como un DNI: cualquiera que lo tenga en la mano lo puede leer; lo que no puede es falsificarlo.

🎓 **Si te lo preguntan — ¿qué es un JWT y qué garantiza su firma?**
Es un token con tres partes: un header (algoritmo), un payload con claims (usuario, rol, permisos, expiración) y una firma calculada con una clave secreta sobre header y payload. La firma garantiza que el token fue emitido por quien posee la clave y que su contenido no fue modificado. No cifra el contenido: el payload es legible.

---

## 4. 🔴 Generar y validar un JWT en código

En el servidor que emite los tokens hay una clase que encapsula todo lo de la sección 3. Usa la librería **jjwt** (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`, versión 0.11.5 en el `pom`).

### 4.1 La clave secreta, fija y en configuración

`application.properties`:

```properties
# Clave secreta para firmar los JWT. FIJA: si se genera al azar al arrancar, cada reinicio del
# servidor cambia la firma y todos los tokens emitidos antes dejan de validar.
# ${JWT_SECRET:...} = "leé la variable de entorno JWT_SECRET; si no existe, usá lo que sigue a los dos puntos".
jwt.secret=${JWT_SECRET:cambiar-esta-clave-por-una-larga-y-secreta-de-al-menos-32-caracteres}
```

⚠️ La librería del servidor exige que la clave tenga **al menos 32 caracteres** para HS256; con una más corta, el servidor no arranca (`WeakKeyException`). jwt.io acepta cualquier secreto porque es una herramienta de demostración; tu `application.properties`, no.

⚠️ Dos reglas sobre la clave, y las dos generan errores silenciosos si se rompen:

1. **Fija.** No se genera dinámicamente al arrancar. Se lee de configuración.
2. **La misma en todos los servidores que validen los tokens.** Si el servicio que emite firma con una clave y otro servicio verifica con otra, ese otro rechaza todos los tokens y no dice por qué. En el módulo 7 esto es central, porque hay varios servicios validando tokens que emitió uno solo.

### 4.2 `JwtUtil`: emitir y verificar

```java
@Component
public class JwtUtil {

    public static final String CLAIM_ROL = "rol";
    public static final String CLAIM_PERMISOS = "permisos";
    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_REFRESH = "refresh";

    private static final long ACCESS_TOKEN_VALIDITY  = 15 * 60 * 1000;            // 15 minutos, en milisegundos
    private static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000;   // 7 días

    private final Key key;      // la clave secreta, ya convertida al formato que la librería necesita

    public JwtUtil(@Value("${jwt.secret}") String secret) {           // @Value: inyecta jwt.secret del properties
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));   // texto → clave HMAC
    }

    // ── EMITIR ─────────────────────────────────────────────────────────────────────

    public String generarAccessToken(Usuario usuario) {
        List<String> permisos = usuario.getPermisos().stream()
                .map(Enum::name)                                         // los permisos son un enum: PERMISO.name()
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(usuario.getNombreDeUsuario())                // claim "sub": el usuario
                .setIssuer("gestion-alumnos-server")                     // claim "iss": quién emite
                .claim(CLAIM_ROL, usuario.getRol().name())               // claim propio "rol"
                .claim(CLAIM_PERMISOS, permisos)                         // claim propio "permisos"
                .setIssuedAt(new Date())                                 // claim "iat": ahora
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY))   // "exp": +15 min
                .signWith(key, SignatureAlgorithm.HS256)                 // la FIRMA con la clave secreta
                .compact();                                              // → "eyJ...": el string de tres partes
    }

    public String generarRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("gestion-alumnos-server")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))  // "exp": +7 días
                .claim(CLAIM_TYPE, TYPE_REFRESH)                         // marca: "este es un refresh, no un access"
                .signWith(key, SignatureAlgorithm.HS256)                 // MISMA clave
                .compact();
    }

    // ── VERIFICAR ──────────────────────────────────────────────────────────────────

    public Claims parsearClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)              // con esta clave recalculá la firma...
                .build()
                .parseClaimsJws(token)           // ...y compará. Si no coincide o si "exp" ya pasó: LANZA EXCEPCIÓN.
                .getBody();                      // si pasó: los claims, listos para leer
    }

    public String validarToken(String token) {
        return parsearClaims(token).getSubject();   // el usuario ("sub"), o excepción si el token no vale
    }
}
```

```
// ¿CÓMO FUNCIONA?
//  EMITIR (en el login exitoso):
//   1. generarAccessToken(usuario admin) arma un payload {sub: admin, iss: ..., rol: ADMIN, permisos: [...], iat, exp: +15min}
//   2. signWith(key, HS256) calcula la firma con la clave secreta.
//   3. compact() devuelve "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIs....SflKxwRJ..."
//   4. Lo mismo para el refresh, con exp +7 días y el claim type=refresh, sin rol ni permisos.
//   Resultado esperado: dos strings largos. El servidor NO guarda ninguno de los dos en ningún lado.
//
//  VERIFICAR (en cada pedido que llegue con Authorization: Bearer ...):
//   1. parsearClaims(token) separa las tres partes, recalcula la firma con `key`, compara.
//   2. Si la firma no coincide → excepción (token falso o modificado).
//      Si "exp" es anterior a ahora → excepción (token vencido).
//   3. Si pasa: devuelve los claims. getSubject() → "admin"; get("rol") → "ADMIN".
//   Resultado esperado: o los claims, o una excepción. No hay "no sé".
```

Dos observaciones sobre este código que son decisiones de diseño, no detalles:

- **El access token lleva rol y permisos como claims.** La alternativa sería que llevara solo el usuario y que cada servicio, al recibirlo, pidiera los roles a otro endpoint. Con los claims adentro se ahorra ese pedido en cada request. El costo: si a alguien le cambian los permisos, su token sigue diciendo los viejos hasta que venza (15 minutos). Es la clase de decisión que hay que poder justificar: *una llamada menos* contra *datos siempre frescos*.
- **El refresh token lleva un claim `type=refresh`.** Sirve para que nadie use un refresh token (que dura 7 días) como si fuera un access token. Quien verifique un access token tiene que rechazar cualquiera que traiga esa marca. Por qué hace falta esa distinción: sección 5.

---

## 5. 🔴 Access token y refresh token: por qué son dos

Un solo token con vencimiento corto sería incómodo: cada 15 minutos la persona tendría que volver a escribir usuario y contraseña. Un solo token con vencimiento largo sería peligroso: si se lo roban, sirve durante días. Los dos tokens resuelven esa tensión:

| | Access token | Refresh token |
|---|---|---|
| Para qué | **Acceder**: va en cada pedido, `Authorization: Bearer` | **Renovar**: se canjea por un access token nuevo cuando el anterior venció |
| Duración | Corta (15 minutos en el ejemplo) | Larga (7 días) |
| Qué lleva | Usuario, rol, permisos, vencimiento | Usuario, vencimiento, la marca `type=refresh` |
| Si lo roban | Sirve 15 minutos | Sirve para pedir access tokens hasta que venza |

La analogía es un DNI con vencimiento corto: el access token es el DNI que mostrás en cada ventanilla y que vence enseguida; el refresh token es el trámite que te da un DNI nuevo sin tener que probar tu identidad desde cero.

```
   LOGIN
   ─────
   POST /login  usuario+clave
   ◄── { accessToken (15 min), refreshToken (7 días) }

   USO NORMAL (los primeros 15 minutos)
   ─────────────────────────────────────
   GET /alumnos   Authorization: Bearer <access>   → ✅ 200

   MINUTO 16: el access venció
   ────────────────────────────
   GET /alumnos   Authorization: Bearer <access>   → ❌ 401 (vencido)
   POST /refresh  { refreshToken }                 → ◄── { accessToken NUEVO, el mismo refreshToken }
   GET /alumnos   Authorization: Bearer <access nuevo>  → ✅ 200

   La persona no vio nada de esto. Para ella, "la sesión nunca expira".
```

Ese canje lo hace el cliente **automáticamente**, sin molestar a la persona: cuando un pedido rebota por token vencido, canjea el refresh, guarda el access nuevo, y reintenta. En el módulo 7 vas a ver el código exacto que lo hace en el servidor de front.

🎓 **Si te lo preguntan — ¿por qué se usan un access token y un refresh token?**
El access token, de corta duración, viaja en cada request y limita el daño si es robado; el refresh token, de larga duración, permite obtener un access token nuevo sin volver a pedir credenciales. Juntos logran que la persona no perciba que la sesión expira, sin sacrificar seguridad.

---

## 6. 🔴 Qué se gana y qué se pierde

### 6.1 Lo que se gana: escalar

Volvé al hilo del módulo 4: sesiones en memoria, un servidor pasa a ser dos, ¿cuál tiene tu sesión? Con tokens, la pregunta desaparece. **El servidor no guarda nada por persona**, así que cualquier servidor con la clave secreta puede verificar cualquier token. Podés poner diez servidores idénticos detrás de un reparto de carga y cada pedido puede caer en cualquiera.

```
   STATEFUL: el pedido TIENE que caer donde está la sesión     STATELESS: cualquiera sirve
   ┌─────────┐  session=7f3a9c  ┌───────────────┐             ┌─────────┐   Bearer <jwt>   ┌───────────┐
   │ cliente │─────────────────►│ servidor A    │ ✅          │ cliente │────────────────►│ servidor A │ ✅
   └─────────┘                  │ (tiene 7f3a9c)│             └─────────┘                 │ servidor B │ ✅
                                ├───────────────┤                                         │ servidor C │ ✅
                                │ servidor B    │ ❌ ¿quién sos?                          └───────────┘
                                └───────────────┘                                         (todos tienen la clave)
```

Esta es la diferencia de fondo entre las dos arquitecturas, y tiene consecuencias más amplias que se desarrollan en una clase posterior sobre escalabilidad. Acá basta con el mecanismo: **sin estado por usuario, no hay nada que compartir entre servidores.**

### 6.2 Lo que se pierde: cerrar sesión

En el módulo 4, cerrar sesión era borrar la fila: el servidor tenía el control. Con tokens, **el servidor no tiene ninguna fila que borrar.** Emitió un token válido hasta las 10:32; a las 10:20 la persona aprieta "cerrar sesión"... y el token sigue siendo válido hasta las 10:32, porque la firma sigue siendo correcta y la fecha no pasó. Si alguien lo copió, lo puede usar esos 12 minutos.

Dos formas de mitigarlo, ninguna perfecta:

| Mitigación | Cómo | Costo |
|---|---|---|
| **Vencimiento corto** | Access tokens de minutos; el daño posible es ese lapso | Hace falta el refresh token (sección 5) |
| **Lista negra** (*blacklist*) | El servidor anota los tokens "cerrados" y los rechaza aunque la firma valga | Volvés a guardar estado por usuario: es un poco de stateful otra vez |

Es un trade-off real, y poder enunciarlo es parte de saber el tema: **la arquitectura stateless escala mejor a cambio de perder control sobre las credenciales ya emitidas.**

🎓 **Si te lo preguntan — ¿qué ventaja y qué desventaja tiene la autenticación por tokens frente a la sesión con cookie?**
Ventaja: el servidor no guarda estado por usuario, por lo que cualquier instancia puede atender cualquier request y el sistema escala horizontalmente. Desventaja: el servidor no puede invalidar un token ya emitido (no hay sesión que cerrar); se mitiga con vencimientos cortos más refresh token, o con una lista negra que reintroduce algo de estado.

---

## 7. 🟡 Un servidor stateless, pedido por pedido

Cómo procesa un servidor stateless cada request, para que el módulo 7 se lea sin sorpresas:

```
   Llega:  GET /api/alumnos
           Authorization: Bearer eyJhbGciOi...

   1. Un FILTRO intercepta el pedido ANTES de que llegue a cualquier controller.
   2. Lee la cabecera Authorization. Si no está o no empieza con "Bearer ": sigue sin identidad (y va a rebotar).
   3. Saca el token. Llama a jwtUtil.parsearClaims(token).
        excepción → 401, el pedido muere acá.
        claims    → sub="admin", rol="ADMIN", permisos=[...]
   4. Con esos claims arma "quién es el que pide" y lo deja disponible para el resto del pedido.
   5. Recién ahora el pedido llega al controller, que ya sabe quién es y qué puede.
   6. Termina el pedido. El servidor NO guardó nada. El próximo pedido arranca en 1 otra vez.
```

Ese filtro es una clase concreta que se escribe una vez por servidor, y es una de las piezas del módulo 7. Lo que importa acá es el punto 6: **al terminar, no queda rastro.** Cada pedido trae su identidad, se verifica, se usa, se descarta.

> **Filtro:** un componente que se ejecuta antes (y/o después) de cada request, sobre todos los pedidos, sin que los controllers sepan que existe. Spring Security, que se ve en el módulo 6, es una cadena de filtros; el filtro concreto que valida tokens se escribe en el módulo 7.

---

## 8. 🟡 Token de usuario y API token: no es lo mismo

Todo lo anterior es un token que representa **a una persona**. Cuando dos servicios se hablan directamente entre ellos, sin ninguna persona en el medio (el servicio de donaciones avisándole al de notificaciones), no hay usuario que representar. Para eso se usa un **API token**:

| | JWT de usuario | API token |
|---|---|---|
| A quién representa | A una persona logueada | A un **servicio** (una máquina) |
| Qué lleva | Usuario, rol, permisos, vencimiento | Una credencial fija que identifica al servicio que llama |
| Cuándo | Pedidos originados por una persona | Comunicación *machine to machine* |

Si en el TP un servicio llama a otro por su cuenta, no le manda el JWT de una persona (¿de cuál?): le manda su API token. Los dos mecanismos conviven en el mismo sistema.

> 🕳️ **Madriguera — SSO y "iniciar sesión con Google"**
> Existe una forma de delegar el login en otro proveedor (Google, GitHub) que te devuelve un token ya emitido por ellos. Se llama *single sign-on* y es un extra opcional del TP, no parte del contenido evaluado.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

🧩 **En tu TP** — la relación *servidor de front ↔ servicios* va a ser **stateless con JWT**: el servicio de autenticación emite el par access/refresh, el front lo guarda (en su sesión, módulo 4) y lo manda como `Bearer` en cada pedido hacia donaciones, logística, incentivos y notificaciones. Los cuatro tienen que validar con **la misma clave** que usó el de autenticación. Y el canje automático del refresh cuando el access vence es una clase que vas a copiar casi tal cual. Todo eso es el módulo 7; lo de este módulo es la moneda que circula.

---

## Checkpoint del módulo 5

Sin respuestas.

1. Definí stateful y stateless en una oración cada una. ¿Dónde está la información del usuario en cada caso?
2. ¿Qué lleva una cookie de sesión y qué lleva un token? ¿Qué hace el servidor con cada uno al recibirlo?
3. ¿Cuáles son las tres partes de un JWT y qué contiene cada una?
4. Alguien decodifica el payload de tu token, cambia `"rol": "DOCENTE"` por `"rol": "ADMIN"` y lo vuelve a codificar. ¿Qué pasa cuando lo manda? ¿Por qué?
5. ¿Se puede guardar la contraseña en un claim del JWT? ¿Por qué?
6. ¿Por qué la clave secreta tiene que ser fija y no generarse al arrancar el servidor? ¿Y por qué tiene que ser la misma en todos los servicios que validan?
7. ¿Qué diferencia hay entre `generarAccessToken` y `generarRefreshToken` en el código? Nombrá tres.
8. ¿Qué problema resuelve tener dos tokens en vez de uno solo? ¿Qué pasaría con uno solo corto, y con uno solo largo?
9. ¿Por qué con tokens "cerrar sesión" no cierra nada del lado del servidor? ¿Qué dos formas hay de mitigarlo y qué cuesta cada una?
10. ¿Qué es un API token y en qué se diferencia de un JWT de usuario? Dá un ejemplo del TP donde iría cada uno.

---

## Qué viene en el módulo 6

Sabés qué es una sesión y qué es un token. Falta la herramienta que usa las dos para **proteger rutas y esconder botones**: Spring Security. En el módulo 6 vas a ver cómo se declara qué rutas son públicas y cuáles no, cómo se define un usuario con roles y permisos, cómo se compara una contraseña sin guardarla en texto plano, cómo se protege un método con una anotación, y cómo una plantilla decide si muestra o no un botón según quién esté logueado. Se cierra el **hilo H5**: hoy todas tus rutas son públicas; después del 6, no.

---

**FIN DEL MÓDULO 5**
