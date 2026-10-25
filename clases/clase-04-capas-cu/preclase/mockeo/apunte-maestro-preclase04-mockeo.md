# 📘 Apunte Maestro — Preclase 04 · Mockeo de objetos con Mockito

> Parte del material previo de la clase 4 (Arquitectura de Software). Cubre **solo el tema Mockeo**. Los otros dos temas de esta preclase — *Inyección de dependencias* y *Biblioteca vs Framework* — van en sus propias partes.

---

## 1. El caso: "Cuidándonos", requerimiento 4 🔴

Estamos diseñando **Cuidándonos**, una app de seguridad personal: un *transeúnte* elige *cuidadores* que lo acompañan durante un trayecto. De todo el sistema, acá nos interesa **una sola pieza**, el requerimiento 4:

> Cuando el transeúnte arranca un viaje hacia un destino, el sistema debe **calcular el tiempo de demora aproximado** del recorrido. Para eso primero necesita la **distancia en metros** entre el punto de partida y el destino — y esa distancia la calcula la *Distance Matrix API* de Google (un servicio externo con interfaz REST).

De ahí salen dos cálculos encadenados:

1. **Distancia en metros** entre dos ubicaciones → la daría una API externa de Google.
2. **Demora aproximada en minutos** a partir de esos metros → con algún algoritmo que todavía no está definido.

Y acá aparece la tensión de diseño que dispara todo el tema: queremos **avanzar con el diseño y testearlo ya**, pero…

- **No nos queremos integrar todavía** con la API de Google (integrarse contra un servicio REST externo es trabajo aparte, y no lo vamos a hacer ahora).
- **No sabemos aún el algoritmo** que convierte metros en minutos.

La pregunta que organiza todo el apunte: **¿cómo diseño y testeo esta parte sin tener resueltas esas dos cosas?**

> 🕳️ **Madriguera — el resto de Cuidándonos**
> El enunciado completo tiene más requerimientos (notificaciones a cuidadores, reacción ante incidentes, cola de mensajes, persistencia). Nada de eso entra acá: trabajamos únicamente el req. 4.
> *Volvé al camino — el resto es de otras clases.*

---

## 2. El diseño: delegar en interfaces 🔴

La jugada es **no resolver los dos cálculos dentro de `Viaje`**, sino **delegarlos** en dos colaboradores descriptos por **interfaces**. Una interfaz es un contrato: dice *qué* método existe (su firma), no *cómo* se implementa.

> 🕳️ **Madriguera — interfaz**
> Es la firma de uno o más métodos sin cuerpo; quien la implemente decide el *cómo*. Se vio en la unidad anterior; acá la usamos como herramienta.
> *Volvé al camino.*

Diagrama de las clases del caso:

```
        «interface»                                  «interface»
   CalculadorDeDistancia                        CalculadorDeDemora
   + distanciaEnMetrosEntre(                     + demoraAproximadaEnMinsParaRecorrer(
        Ubicacion, Ubicacion) : float                 float) : double
              ▲                                              ▲
              │  recibe por parámetro                        │  recibe por parámetro
              └───────────────────┬──────────────────────────┘
                                  │
                               Viaje
                    - puntoDePartida : Ubicacion
                    - destino        : Ubicacion
                    - demoraAproximadaEnMins : double
                    + calcularDemoraAproximadaEnMins(
                         CalculadorDeDistancia,
                         CalculadorDeDemora)
                                  │  tiene 2
                                  ▼
                              Ubicacion
                    - latitud    : float
                    - longitud   : float
                    - referencia : String
```

Clave del diseño (y esto es lo que después habilita todo): **`Viaje` no crea sus calculadoras; las recibe como parámetros del método**. Depende de las **interfaces**, no de clases concretas. Eso es *inyección de dependencias* — el porqué se profundiza en la parte de DI de esta misma preclase; por ahora quedate con que el colaborador entra **desde afuera**.

### 2.1. Las clases del dominio

`Ubicacion` — un punto geográfico simple:

```java
package cuidandonos;

import lombok.Getter;
import lombok.Setter;

@Setter          // Lombok genera los setters de todos los campos en tiempo de compilación
@Getter          // Lombok genera los getters de todos los campos (así no escribimos accessors a mano)
public class Ubicacion {
    private float latitud;       // coordenada
    private float longitud;      // coordenada
    private String referencia;   // texto legible: "UTN BA Medrano", para saber de dónde/hacia dónde vamos

    // Constructor: una Ubicacion se crea siempre con sus tres datos
    public Ubicacion(float latitud, float longitud, String referencia) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.referencia = referencia;
    }
}
```

> 🕳️ **Madriguera — Lombok (`@Setter`/`@Getter`)**
> Librería que genera getters/setters (y más) por vos a partir de anotaciones, para no llenar la clase de código repetido. Se asume de la unidad anterior.
> *Volvé al camino.*

Las **dos interfaces** — solo la firma, sin implementación (justamente porque todavía no las queremos/podemos implementar):

```java
package cuidandonos.distancia;

import cuidandonos.Ubicacion;

public interface CalculadorDeDistancia {
    // Contrato: dadas dos ubicaciones, devolver la distancia en metros.
    // En la realidad, quien lo implemente pegaría contra la API de Google. Todavía no existe esa clase.
    public float distanciaEnMetrosEntre(Ubicacion unaUbicacion, Ubicacion otraUbicacion);
}
```

```java
package cuidandonos.demora;

public interface CalculadorDeDemora {
    // Contrato: dados unos metros, devolver la demora aproximada en minutos.
    // El algoritmo real no está definido. Tampoco existe la clase que lo implemente.
    public double demoraAproximadaEnMinsParaRecorrer(float unosMetros);
}
```

### 2.2. `Viaje`: la clase que queremos testear

```java
package cuidandonos;

import cuidandonos.demora.CalculadorDeDemora;
import cuidandonos.distancia.CalculadorDeDistancia;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Viaje {
    private Ubicacion puntoDePartida;
    private Ubicacion destino;
    private double demoraAproximadaEnMins;   // acá guardamos el resultado final del cálculo

    // Recibe sus dos colaboradores POR PARÁMETRO (no los instancia adentro).
    public void calcularDemoraAproximadaEnMins(CalculadorDeDistancia calculadorDeDistancia,
                                               CalculadorDeDemora calculadorDeDemora) {

        // Paso 1: le pido al calculador de distancia los metros entre partida y destino.
        float distanciaEnMetros = calculadorDeDistancia.distanciaEnMetrosEntre(this.puntoDePartida, this.destino);

        // Paso 2: con esos metros, le pido al calculador de demora los minutos, y los guardo en el atributo.
        this.demoraAproximadaEnMins = calculadorDeDemora.demoraAproximadaEnMinsParaRecorrer(distanciaEnMetros);
    }
}
```

```java
// ¿CÓMO FUNCIONA calcularDemoraAproximadaEnMins?
// 1. Toma sus colaboradores de los parámetros (no sabe ni le importa CÓMO calculan; confía en la interfaz).
// 2. Encadena: distancia → metros → demora. La salida del primer cálculo es la entrada del segundo.
// 3. Deja el resultado en this.demoraAproximadaEnMins (efecto de lado sobre el propio objeto; el método es void).
// La lógica que estamos probando es ESA orquestación: "pedir distancia, y con eso pedir demora, y guardarla".
```

> **Para el parcial, si te preguntan** — *¿Por qué `Viaje` depende de dos interfaces y recibe los colaboradores por parámetro, en lugar de calcular todo adentro?*
> Para desacoplar `Viaje` del *cómo* se calcula distancia y demora. `Viaje` solo orquesta; delega el algoritmo en colaboradores que entran desde afuera. Priorizo **testabilidad** y **mantenibilidad** (puedo cambiar la implementación de un calculador sin tocar `Viaje`); el costo es más piezas y una indirección extra. Esta decisión es la que, un paso después, hace posible el mockeo.

---

## 3. El problema al testear `Viaje` 🔴

Queremos un test así: *"un viaje de Medrano a Campus tiene una demora aproximada de 30 minutos"*. Armamos el escenario:

```java
Ubicacion medrano = new Ubicacion(-34.598450F, -58.420065F, "UTN BA Medrano");
Ubicacion campus  = new Ubicacion(-34.659277F, -58.4673392F, "UTN BA Campus");

Viaje viajeDeSedeASede = new Viaje();
viajeDeSedeASede.setPuntoDePartida(medrano);
viajeDeSedeASede.setDestino(campus);
// ...¿y ahora cómo llamo a calcularDemoraAproximadaEnMins?
```

`calcularDemoraAproximadaEnMins` pide **un `CalculadorDeDistancia` y un `CalculadorDeDemora`**. Pero **no existe ninguna clase que implemente esas interfaces**: la de distancia dependería de la API de Google (no nos integramos), y la de demora ni tiene algoritmo definido.

El bloqueo, en una línea: **para testear `Viaje` necesito objetos que cumplan esas dos interfaces, y no los tengo.** Y no los quiero construir de verdad solo para probar la orquestación de `Viaje`.

---

## 4. La solución: objetos mockeados con Mockito 🔴

Un **mock** (objeto *mockeado*, *impostor*, "mentiroso") es un objeto **falso que cumple una interfaz** y al que vos le **dictás qué responder**. No tiene lógica real: devuelve lo que le programaste. Sirve exactamente para esto — parás una dependencia que no existe (o que es cara/externa) con un doble controlado, y podés testear la clase que te importa **de forma aislada**.

**Mockito** es la biblioteca que crea esos mocks en Java.

❌ **Sin mock:** no puedo instanciar un `CalculadorDeDistancia` → no puedo llamar a `calcularDemoraAproximadaEnMins` → **no puedo testear `Viaje`** hasta tener implementadas las calculadoras reales (y la integración con Google).
✅ **Con mock:** fabrico dos impostores que cumplen las interfaces y les digo qué devolver → llamo a `calcularDemoraAproximadaEnMins` con ellos → **testeo la orquestación de `Viaje` ya**, sin API real ni algoritmo real.

### 4.1. La dependencia (Maven) 🟡

Mockito entra al proyecto como dependencia de test:

```xml
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-core</artifactId>
  <version>5.3.0</version>
  <scope>test</scope>   <!-- scope test: solo se usa al compilar/correr tests, no en producción -->
</dependency>
```

Necesitás también **JUnit** (para poder escribir y correr los tests) y, en este proyecto, **Lombok** (para los accessors).

> ⚠️ **Nota de versiones (para el stack real de la materia).** En el TPA trabajás con **Spring Boot** (stack 2026). Ahí **no declarás `mockito-core` a mano**: llega por transitividad del *starter* de test (`spring-boot-starter-test`), igual que JUnit. Declararlo suelto como acá es lo correcto en un proyecto Maven **plano** como el de este ejemplo. La API de mockeo (`mock`, `when`, `thenReturn`) es idéntica en ambos casos.

### 4.2. El import estático 🔴

```java
import static org.mockito.Mockito.*;
```

```java
// ¿POR QUÉ "static" y con "*"?
// Los métodos de Mockito que vamos a usar (mock, when...) son ESTÁTICOS (métodos de clase).
// El import estático te deja llamarlos por su nombre pelado —  mock(...)  en vez de  Mockito.mock(...) —
// dejando el test más limpio y legible. El "*" importa todos esos métodos estáticos de la clase Mockito.
```

> 🕳️ **Madriguera — import estático**
> Un `import static` trae **miembros estáticos** (métodos/constantes) de una clase para usarlos sin prefijarlos con el nombre de la clase. Distinto del `import` normal, que trae el **tipo**.
> *Volvé al camino.*

---

## 5. El test, paso a paso 🔴

Este es el corazón de la preclase. El test crea los dos mocks, les programa la respuesta, ejecuta `Viaje` y verifica.

```java
package tests;

import cuidandonos.Ubicacion;
import cuidandonos.Viaje;
import cuidandonos.demora.CalculadorDeDemora;
import cuidandonos.distancia.CalculadorDeDistancia;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;   // trae mock(...) y when(...) sin prefijo

public class ViajeTest {

    @Test   // marca este método como un caso de test para JUnit
    public void demoraDe30MinsEnViaje() {

        // --- 1) ESCENARIO: datos reales de entrada -----------------------------
        Ubicacion medrano = new Ubicacion(-34.598450F, -58.420065F, "UTN BA Medrano");
        Ubicacion campus  = new Ubicacion(-34.659277F, -58.4673392F, "UTN BA Campus");
        // El sufijo F hace que el literal sea float (el tipo de latitud/longitud). Sin la F, Java lo lee como double.

        Viaje viajeDeSedeASede = new Viaje();
        viajeDeSedeASede.setPuntoDePartida(medrano);   // setters generados por Lombok
        viajeDeSedeASede.setDestino(campus);

        // --- 2) MOCK del calculador de distancia -------------------------------
        // mock(Interface.class) devuelve un objeto impostor que CUMPLE la interfaz.
        CalculadorDeDistancia calculadorDeDistancia = mock(CalculadorDeDistancia.class);
        // when(...).thenReturn(...) le dicta el comportamiento:
        // "cuando te llamen distanciaEnMetrosEntre(medrano, campus), devolvé 10100 metros".
        when(calculadorDeDistancia.distanciaEnMetrosEntre(medrano, campus)).thenReturn(10100F);

        // --- 3) MOCK del calculador de demora ----------------------------------
        CalculadorDeDemora calculadorDeDemora = mock(CalculadorDeDemora.class);
        // "cuando te pidan la demora para 10100 metros, devolvé 30.0 minutos".
        when(calculadorDeDemora.demoraAproximadaEnMinsParaRecorrer(10100F)).thenReturn(30.0);
        // 30.0 es un literal double (coincide con el tipo de retorno de la interfaz).

        // --- 4) EJECUCIÓN: corro el método real de Viaje, pasándole los mocks --
        viajeDeSedeASede.calcularDemoraAproximadaEnMins(calculadorDeDistancia, calculadorDeDemora);

        // --- 5) VERIFICACIÓN: el resultado guardado debe ser 30.0 --------------
        Assertions.assertEquals(30.0, viajeDeSedeASede.getDemoraAproximadaEnMins());
        // Resultado esperado: el test pasa (verde). demoraAproximadaEnMins quedó en 30.0.
    }
}
```

```java
// ¿CÓMO FUNCIONA EL TEST COMPLETO?
// Los mocks NO calculan nada: solo repiten lo que les programamos. Entonces, al correr el test:
//  1. Viaje.calcularDemoraAproximadaEnMins llama a calculadorDeDistancia.distanciaEnMetrosEntre(medrano, campus)
//     → el mock devuelve 10100F (lo que dictamos).
//  2. Viaje toma esos 10100 y llama a calculadorDeDemora.demoraAproximadaEnMinsParaRecorrer(10100F)
//     → el mock devuelve 30.0 (lo que dictamos).
//  3. Viaje guarda 30.0 en su atributo demoraAproximadaEnMins.
//  4. assertEquals(30.0, ...) compara esperado vs obtenido → coinciden → VERDE.
// Que el test pase confirma que la ORQUESTACIÓN de Viaje es correcta: encadena bien los dos cálculos
// y guarda el resultado. No probamos el cómo de las calculadoras (eso lo fingimos).
```

**El patrón que se repite** (te va a servir para cualquier mock):

```
1. crear el mock          →  Tipo x = mock(Tipo.class);
2. programar la respuesta  →  when( x.metodo(args) ).thenReturn( valor );
3. usar el mock            →  se lo paso al objeto bajo test
4. verificar              →  assertEquals(esperado, resultadoObtenido);
```

> **Para el parcial, si te preguntan** — *¿Qué es un objeto mockeado y para qué sirve?*
> Un objeto falso que cumple una interfaz y devuelve respuestas programadas (`when(...).thenReturn(...)`), sin lógica real. Sirve para testear una clase **de forma aislada**, reemplazando colaboradores que no están implementados o que son externos/costosos (una API de terceros). Permite avanzar el diseño y probarlo sin depender de esas piezas.

### 5.1. La misma verificación, con AssertJ 🟡

El test de arriba usa `Assertions.assertEquals(...)`, que es de **JUnit**. En la materia, sin embargo, las aserciones se escriben con **AssertJ** — una biblioteca de aserciones más expresiva, **montada encima** de JUnit. Se vio en la clase 2 y entra sola con el *starter* de test.

Cuidá la división de roles: **JUnit sigue siendo el motor** (corre los tests, entiende `@Test`); **AssertJ solo reemplaza la línea de verificación**. Y — clave — **AssertJ es ortogonal a Mockito**: los mocks no cambian en nada. Lo único que cambia es un import y la última línea:

```java
import static org.assertj.core.api.Assertions.assertThat;   // en vez de org.junit.jupiter.api.Assertions
```

```java
// La verificación del test, en los dos estilos:

// JUnit (tal cual viene en este ejemplo):
Assertions.assertEquals(30.0, viajeDeSedeASede.getDemoraAproximadaEnMins());
//                       └ esperado          └ real

// AssertJ (como lo escribe la cátedra):
assertThat(viajeDeSedeASede.getDemoraAproximadaEnMins()).isEqualTo(30.0);
//         └ real (arranca por el valor obtenido)          └ esperado
```

Fijate que el orden se **invierte**: AssertJ arranca por el valor obtenido y se lee casi como una frase — *"afirmá que \[esto que obtuve\] es igual a \[esto\]"*. Esa legibilidad es toda su gracia.

Equivalencias que te van a aparecer:

| Verificar | JUnit 5 | AssertJ |
|---|---|---|
| Son iguales | `assertEquals(esperado, real)` | `assertThat(real).isEqualTo(esperado)` |
| `double` con tolerancia | `assertEquals(esp, real, delta)` | `assertThat(real).isCloseTo(esp, within(delta))` |
| Es verdadero | `assertTrue(cond)` | `assertThat(cond).isTrue()` |
| No está vacía | `assertFalse(l.isEmpty())` | `assertThat(l).isNotEmpty()` |

> **Nota sobre el `double`.** Acá `isEqualTo(30.0)` **exacto** está bien, porque el 30.0 lo devuelve el mock tal cual — no sale de ninguna cuenta con decimales. Si el valor viniera de una operación (divisiones, sumas de `float`), usarías `isCloseTo(30.0, within(0.001))` para no romperte con los redondeos del punto flotante.

> **Para el parcial, si te preguntan** — *¿AssertJ reemplaza a JUnit?*
> No. JUnit es el **motor** de tests (corre `@Test`, `@BeforeEach`, entiende las anotaciones); AssertJ es solo una **biblioteca de aserciones** que se usa en la fase de verificación, más legible que `Assertions`. Conviven: en un mismo test podés tener Mockito para los dobles, JUnit como motor y AssertJ para las aserciones.

---

## 6. Qué probamos y qué NO probamos 🟡

Con este test logramos testear `Viaje` **sin llamar a la API de Google y sin saber el algoritmo de demora**. Pero hay que tener claro el límite:

- ✅ **Probamos:** que `Viaje` **orquesta bien** — pide la distancia, usa ese resultado para pedir la demora, y guarda el valor final.
- ❌ **No probamos:** que la distancia real entre Medrano y Campus sea 10100 m, ni que el algoritmo de demora dé 30 min. Eso lo **fingimos** con los mocks.

Cuando existan las implementaciones reales de las calculadoras, se escriben **otros tests** (que sí ejerciten esa lógica) para **complementar** este. El mock no reemplaza esos tests: te desbloquea *mientras tanto*.

> **Para el parcial, si te preguntan** — *¿Qué atributo de calidad favorece mockear y qué trade-off pagás?*
> Favorece la **testabilidad**: podés probar una unidad aislada y avanzar el diseño sin las dependencias reales (externas o inexistentes). El trade-off: el test **no valida el comportamiento real** del colaborador (asumís que devuelve lo que vos dictaste), así que necesitás **complementarlo** con tests de la implementación real / de integración una vez que existan.

> 🕳️ **Madriguera — Mockito da para mucho más**
> Más allá de `mock` + `when/thenReturn`, Mockito tiene `verify(...)` (chequear que un método se llamó y cuántas veces), *argument matchers* (`any()`, `eq()`), anotaciones `@Mock` + extensión de JUnit, y la distinción formal *mock* vs *stub*. Nada de eso hace falta acá: con crear el impostor y dictarle la respuesta alcanza.
> *Volvé al camino — esto se profundiza aparte, otro día.*

---

## Info operativa

- **Herramientas del tema:** Mockito (`org.mockito:mockito-core`) + JUnit 5 (Jupiter) para correr los tests. En el TPA (Spring Boot) ambos vienen por el *starter* de test.
- **Conexión con esta misma preclase:** el diseño "recibir colaboradores por parámetro" es *inyección de dependencias* → parte propia de la preclase 04. Y las interfaces vienen de la clase anterior.

---

## Checkpoint (sin respuestas)

1. ¿Por qué no se puede testear `Viaje` con las clases reales de cálculo, en el estado en que está el diseño?
2. ¿Qué es un objeto mockeado y en qué se diferencia de una implementación real de la interfaz?
3. ¿Qué hacen, respectivamente, `mock(...)` y `when(...).thenReturn(...)`?
4. ¿Qué condición del diseño de `Viaje` es la que **habilita** poder mockear sus colaboradores? ¿Qué pasaría si `Viaje` instanciara las calculadoras adentro?
5. En el test, ¿qué está verificando realmente `assertEquals(30.0, viajeDeSedeASede.getDemoraAproximadaEnMins())`? ¿Prueba que la demora real sea 30?
6. ¿Por qué el import de Mockito es `static`?
7. ¿Qué atributo de calidad ganás mockeando y qué límite/trade-off tiene esa técnica?
8. Una vez implementadas las calculadoras reales, ¿este test se tira a la basura? Justificá.
9. La cátedra escribe la aserción como `assertThat(...).isEqualTo(...)` en lugar de `assertEquals(...)`. ¿Qué biblioteca es esa, reemplaza a JUnit como motor, y qué parte del test cambia al usarla? ¿Cambian los mocks?

---

**FIN DEL APUNTE MAESTRO — Preclase 04 · Mockeo**
