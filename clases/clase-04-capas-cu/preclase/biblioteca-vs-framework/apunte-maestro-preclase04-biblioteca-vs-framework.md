# 📘 Apunte Maestro — Preclase 04 · Biblioteca vs Framework

> Tercera y última pieza del material previo de la clase 4. Cierra la preclase (viene de *Mockeo* e *Inyección de Dependencias*). Cubre qué es una biblioteca, qué es un framework, y la diferencia de fondo entre ambos.

---

## 1. De qué va esto 🟡

**Biblioteca** y **framework** son dos herramientas casi omnipresentes en el desarrollo de software. A medida que uno construye piezas más grandes, empieza a usarlas — muchas veces **por inercia**, como oye que se usan y se nombran, sin entender del todo qué está usando. Tienen **muchos puntos en común** pero una serie de diferencias en su concepción y su uso que no siempre quedan claras. Este apunte las separa.

El camino para entenderlas es **por reutilización de código**: primero por qué nacieron las bibliotecas, después por qué las bibliotecas no alcanzaron y nacieron los frameworks.

---

## 2. La necesidad de reutilizar → nacen las bibliotecas 🔴

Al principio del desarrollo de software, **toda** funcionalidad que hiciera falta había que implementarla a mano. Por ejemplo, para conocer la longitud de un string:

```c
int string_length(char* unString){
  int i = 0, longitud = 0;
  while(unString[i] != '\0'){   // recorro hasta el '\0' (marca de fin de string en C)
    longitud++, i++;            // por cada caracter, sumo 1 a la longitud
  }
  return longitud;
}
```

Y se usaba:

```c
char* palabra = "Una palabra";
printf("La palabra es %s y tiene %d caracteres", palabra, string_length(palabra));
// Resultado esperado: La palabra es Una palabra y tiene 11 caracteres
```

Podías **reutilizarla muchas veces dentro del proyecto** — evitás repetir lógica y generás una abstracción nueva. Pero el proyecto termina, arranca otro, y lo más probable es que `string_length` haga falta de nuevo… y haya que **replicar** el código del proyecto anterior.

Para eso surgieron las **bibliotecas**: una forma de **compartir funciones y estructuras ya desarrolladas entre varios proyectos**. Ejemplos:

- En **C**, la biblioteca estándar trae funciones para strings (nuestra `string_length` es `strlen`) bajo el header `string.h`.
- En **Java**, [Commons Lang](https://commons.apache.org/proper/commons-lang/) trae, entre otras cosas, la clase `StringUtils`: con solo agregar la biblioteca al proyecto usás `difference` (diferencia de caracteres entre dos strings) o `countMatches` (cuántas veces aparece un caracter), **porque alguien más ya la hizo**.

No solo hay bibliotecas **genéricas** (strings, fechas, reutilizadas en muchos proyectos): también hay **específicas de un dominio** — por ejemplo, una que gestione los permisos internos de una aplicación, usada muchas veces **dentro** del mismo sistema aunque difícilmente cruce sus límites.

Detalle de cómo vive una biblioteca: **se desarrolla, compila y carga por separado** de tu aplicación, pero sus funcionalidades se usan **como si fueran parte de tu propio código**.

> 🔴 **Definición.** Las bibliotecas resuelven un problema de **reutilización de lógica** asociada a abstracciones, representada e implementada a través de **código**.

> 🕳️ **Madriguera — bibliotecas compartidas por dentro**
> Cómo el sistema operativo carga una biblioteca compartida (linking, memoria) es tema de Sistemas Operativos, no de acá. Alcanza con "se carga aparte y se usa como si fuera propio".
> *Volvé al camino.*

---

## 3. Cuando las bibliotecas no alcanzan → nacen los frameworks 🔴

Las bibliotecas reutilizan **código**, pero no resuelven **todos** los problemas de reutilización. Miralo con este caso.

Un equipo hace un proyecto y, para garantizar calidad, automatiza algunas pruebas. Arma este código:

```
void main(String[] args){
  if (args[1] == "run-tests"){   // si me pasan "run-tests" por línea de comandos...
    correrTests();               // ...corro los tests
  } else {
    ejecutarPrograma(args);      // si no, ejecuto la aplicación normal
  }
}

void correrTests(){
  test1();  test2();  test3();  // ...   ← la lista de tests, escrita a mano
}

void test1(){
  prepararTests();      // ← al inicio de CADA test: armar el escenario
  // (...) código del test1
  limpiarTodo();        // ← al final de CADA test: liberar recursos
}
void test2(){
  prepararTests();      // ← repetido
  // (...) código del test2
  limpiarTodo();        // ← repetido
}
```

Le funciona, y es feliz. Al mes siguiente, otro proyecto necesita lo mismo, y **reusa el esquema**: en el `main`, si le pasan `"run-tests"` corre los tests, si no ejecuta el programa; en `ejecutarPrograma` va el código posta; en `correrTests` la lista de tests; cada test llama a `prepararTests` al inicio y `limpiarTodo` al final; `prepararTests` arma el *fixture* (los datos de prueba) y `limpiarTodo` libera los recursos tomados.

Pasan los meses, llegan más proyectos, todos con el mismo esquema, y a la gente nueva se le enseña a "desarrollar siguiendo ese esquema".

Ese "esquema" provee algo valioso: **una estructura, una forma de trabajar ya definida**, que evita volver a pensar cómo estructurar estas tareas — descansás en algo que alguien ya pensó. Igual que una biblioteca, se reutiliza entre proyectos y representa abstracciones con lógica común (`correrTests`, `prepararTest`…).

**Pero hay una diferencia clave:** la biblioteca se reutilizaba **casi sin esfuerzo**; el esquema, en cambio, es **solo una lista de conceptos que hay que implementar una y otra vez**. ¿Y si, además de reutilizar la lógica de cada dominio, quisiéramos reutilizar también el **control de flujo** que el esquema provee?

Se puede. El equipo, cansado de reimplementar su esquema, escribe una pieza que aplique esa estructura por ellos:

```
// El FRAMEWORK: la estructura común, pensada UNA vez y reutilizable
abstract class AplicacionConTests {

  void main(String[] args){          // ← el framework define el main (no el cliente)
    if (args[1] == "run-tests"){
      correrTests();
    } else {
      ejecutarPrograma(args);
    }
  }

  abstract List<Test> tests;         // ← QUÉ tests hay: lo completa el cliente (cada test es una función)

  void correrTests(){
    tests.forEach(test -> correrTest(test));
  }

  void correrTest(Test test){
    prepararTest();                  // ← gancho: lo completa el cliente
    test.run();
    limpiarTodo();                   // ← gancho: lo completa el cliente
  }

  // Métodos ABSTRACTOS = los "huecos" que el cliente debe rellenar con lo suyo:
  abstract void prepararTest();
  abstract void limpiarTodo();
  abstract void ejecutarPrograma(String[] args);
}
```

Y el programa concreto **solo rellena los huecos** con lo propio de su dominio:

```
// El CLIENTE: extiende el framework y completa lo específico
class MiApp extends AplicacionConTests {

  void test1(){ /* código del test1 */ }
  void test2(){ /* código del test2 */ }

  override tests = List(test1, test2);              // qué tests correr

  override void prepararTest(){ /* crear fixture */ }
  override void limpiarTodo(){ /* liberar recursos */ }
  override void ejecutarPrograma(String[] args){ /* el programa posta */ }
}
```

```
// ¿CÓMO FUNCIONA?
// Ya no reimplementás la estructura en cada proyecto: extendés AplicacionConTests
// y redefinís SOLO los métodos que dependen del dominio (tests, prepararTest, limpiarTodo,
// ejecutarPrograma). El "cuándo se llama a cada cosa" (el main, el recorrido de tests,
// el prepararTest-antes / limpiarTodo-después) ya viene resuelto y NO lo tocás.
```

Esto es una **forma nueva de reutilizar**: no solo funciones y componentes, sino **piezas de software extensibles** que se completan para cada proyecto pero definen **una forma, un marco de trabajo común** para todos. Por eso se los llamó **frameworks**.

> 🔴 **Definición.** Los frameworks definen una **estructura, una forma de trabajar** siguiendo determinados lineamientos.

> 🕳️ **Madriguera — esto se parece al Template Method**
> Ese mecanismo —una parte del código ya definida (el flujo) que descansa en otra parte por definir para cada caso (los métodos abstractos)— es el patrón **Template Method**, que se ve más adelante en la materia. Por ahora, quedate con la idea del framework.
> *Volvé al camino.*

---

## 4. Bajando a lo concreto: control directo vs. control inverso 🔴

Acá está el corazón de la distinción.

**Las bibliotecas** definen funcionalidades y estructuras concretas que **el que programa usa o no, como le convenga**. Por ejemplo, podés elegir usar `LocalDate` o una clase propia que te ordene mes y año como se hace en español. **El control del flujo lo maneja quien programa**: instancia estructuras de la biblioteca o llama a sus operaciones, y **cada llamada hace su trabajo y le devuelve el control** al código que la llamó. *(Vos definís el `main` y vos llamás a `string_length`.)*

**Los frameworks** definen el código de las abstracciones y la lógica de una **estructura de trabajo**, y **obligan** a programar de una forma específica, siguiendo sus lineamientos. Por eso **condicionan fuertemente el diseño** del componente. Suelen:

- **manejar el flujo de ejecución** e **invertir el control**: son **ellos** los que deciden cuándo llamar a tu código *(vos jamás definís el `main`; lo define el framework y él llama a tu `ejecutarPrograma`)*;
- definir el **orden de ejecución** de ciertas tareas, la forma de escribir ciertas operaciones (**DSLs**), o hasta la **estructura de directorios** del código fuente;
- definir **clases y métodos abstractos** que tu código extiende e implementa con el comportamiento propio de la app *(el framework define `AplicacionConTests`; tu `MiApp` completa `prepararTest`, `limpiarTodo`, etc.)*.

> 🕳️ **Madriguera — DSL** *(la fuente lo define, así que va)*
> **DSL** = *domain-specific language*: un lenguaje chico enfocado en un aspecto particular. Ejemplo típico: **SQL** para consultar bases de datos relacionales.
> *Volvé al camino.*

Una forma linda de verlo (así lo define la gente de Akka HTTP, que aclara *no* ser un framework): un framework te da un **marco** con **decisiones ya pre-tomadas** y unos **cimientos** para arrancar rápido; es como un **esqueleto** sobre el que ponés la **"carne"** de tu aplicación. Y como tal, **funciona mejor si lo elegís antes de empezar** y te apegás a "su forma de hacer las cosas".

Diagrama de la diferencia de fondo:

```
   CONTROL DIRECTO (biblioteca)              CONTROL INVERSO (framework)
   ───────────────────────────              ────────────────────────────
      TU CÓDIGO                                  FRAMEWORK
         │ llama                                    │ llama (él decide cuándo)
         ▼                                          ▼
      biblioteca ──► hace algo                  TU CÓDIGO (los métodos que completaste)
         │ devuelve el control                     │
         ▼                                          ▼
      TU CÓDIGO sigue                           …y el framework sigue manejando el flujo

   → vos manejás el flujo                     → el framework maneja el flujo
```

> 🔗 **Acá se cierra el hilo de la preclase.** Ese "invertir el control" es la **inversión de control** que vimos en Inyección de Dependencias (el *principio de Hollywood*: "no nos llames, nosotros te llamamos"). Un framework es, en esencia, IoC llevada a toda la estructura de la app: **Spring** —el framework del TPA— es exactamente esto (define el flujo y llama a tu código, no al revés).

---

## 5. Diferencias y similitudes 🔴

**En común**, una biblioteca y un framework:

- ambos definen **abstracciones y lógica propias**;
- ambos las implementan en **código reutilizable**.

**Se diferencian** en:

| **Bibliotecas** | **Frameworks** |
|---|---|
| Quien programa decide **cómo y cuándo** usar los componentes. | Define **cómo se estructura** el código; quien programa **está obligado** a seguir esa forma. |
| Sus decisiones de diseño suelen tener **bajo impacto** en el código que la usa. | Sus decisiones de diseño pueden **condicionar fuertemente** el diseño del código cliente. |
| **Control directo:** el código cliente llama a las funciones de la biblioteca e instancia sus estructuras. | **Control inverso:** es el framework el que llama a las funciones abstractas que el cliente define en concreto. |

> **Para el parcial, si te preguntan** — *¿Cuál es la diferencia de fondo entre una biblioteca y un framework?*
> El **control del flujo**. Con una biblioteca, **vos llamás** a su código y ella te devuelve el control (control directo): decidís cómo y cuándo usarla. Con un framework, **él llama a tu código** (control inverso / inversión de control): el framework maneja el flujo y decide cuándo invocar los métodos que vos completaste. Por eso un framework condiciona mucho más el diseño de tu aplicación que una biblioteca.

> **Para el parcial, si te preguntan** — *¿Qué tienen en común?*
> Ambos definen abstracciones y lógica propias y las implementan en código reutilizable entre proyectos. La diferencia no está en *qué* reutilizan, sino en *quién controla el flujo*.

---

## Info operativa

- **Terminología del profe:** *biblioteca*, *framework*, *control directo / control inverso*, *inversión de control*, *DSL*.
- **Conexiones de esta preclase:** cierra el hilo de **Inyección de Dependencias** (la inversión de control es lo que distingue a un framework); el ejemplo de framework se apoya en la mecánica del **Template Method** (patrón que se ve más adelante); y aterriza en **Spring**, el framework del TPA.
- **Nota menor del material:** la implementación real de `string_length` (`strlen`) es mucho más concisa —y menos clara— que la del ejemplo; el ejemplo se escribió así para que se entienda.

---

## Checkpoint (sin respuestas)

1. ¿Qué problema de reutilización resuelve una biblioteca? Dala en una definición.
2. ¿Por qué el "esquema de tests" no se reutilizaba tan fácil como una biblioteca, si también encapsulaba lógica común?
3. En `AplicacionConTests`, ¿qué parte la pone el framework y qué parte la completa el cliente (`MiApp`)? ¿Qué rol cumplen los métodos **abstractos**?
4. Definí framework según lo visto.
5. ¿Qué significa que un framework **invierte el control**? Usá el ejemplo del `main`.
6. Nombrá tres cosas que un framework suele imponer además del flujo (más allá de "qué hacer").
7. ¿Cuál es la diferencia entre **control directo** y **control inverso**? ¿Cuál usa cada uno?
8. ¿Qué tienen en común una biblioteca y un framework?
9. Conectá con la Inyección de Dependencias: ¿qué concepto de aquel apunte reaparece acá, y por qué un framework es un caso de eso?

---

**FIN DEL APUNTE MAESTRO — Preclase 04 · Biblioteca vs Framework**
