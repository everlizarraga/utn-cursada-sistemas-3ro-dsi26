# 🎯 MACHETE — Preclase 03 · Herencia · Interfaces · Polimorfismo · Composición

## HERENCIA
- Subclase recibe atributos/métodos del padre + suma propios. Relación **"es-un"**.
- UML: línea **llena** + triángulo hueco → al padre.
- **Extender** = mantener + sumar · **Redefinir** = reemplazar método heredado (`@Override`).
- `extends` · `@Override` · `super(...)` (constructor padre, 1ª línea) · `super.metodo()` (método padre) · **herencia única** (una sola clase padre).
- `private` del padre: se hereda, no se accede directo → getters.
- No redefinir = quedarse con el método del padre tal cual.

```java
class Perro extends Animal {
    Perro(String n){ super(n); }
    @Override void hacerSonido(){ System.out.println("Guau!"); }   // redefine
}
super.hacerSonido();   // extender: llama al padre y suma
```

### Caso donaciones
```java
class Donacion {                 // superclase
    int obtenerPuntaje(){ return cantidad; }        // default
}
class DonarDinero extends Donacion { Double monto; } // NO redefine → hereda cantidad
class DonarVianda extends Donacion {
    @Override int obtenerPuntaje(){ return estaVencida()? 0 : getCantidad()*3; }
}
List<Donacion> ds = List.of(new DonarDinero(...,1,5000.0), new DonarVianda(...,3,"Arroz"), ...);
for (Donacion d : ds) d.obtenerPuntaje();   // 1 llamado → N comportamientos (polimorfismo)
```
- Salida: DonarDinero→1 (monto NO cuenta) · Vianda hoy→9 · Vianda 2023→0 · total 10.

---

## INTERFACES + POLIMORFISMO
- **Polimorfismo**: mismo método, distinto objeto, sin conocer la clase concreta. Requiere **tipo común** (interfaz o superclase).
- **Interfaz** = contrato (qué, no cómo). Firmas terminan en `;`. Se **implementa** (`implements`), no se instancia, no tiene estado.
- UML: línea **punteada** + triángulo hueco (realización).

```java
interface IEstrategiaNotificador { boolean notificarUsuario(String medio, String msj); }

class NotificadorMail implements IEstrategiaNotificador {
    @Override public boolean notificarUsuario(String medio, String msj){
        System.out.println("NotificadorMail " + msj); return true; }
}
// + NotificadorSms, NotificadorWhatsapp (misma firma, distinto cuerpo)

List<IEstrategiaNotificador> ns = List.of(new NotificadorMail(), new NotificadorSms(), ...);
ns.forEach(n -> n.notificarUsuario(medio, msj));   // 1 llamado → N comportamientos
```
- Extensibilidad: canal nuevo = clase nueva + sumarla. No se toca lo existente.
- Herencia = "es un tipo de" · Interfaz = "sabe hacer".

---

## MÚLTIPLES INTERFACES + ANTIPATRÓN
- Falta validar el medio → **interfaz aparte** (separación de responsabilidades, "S" de SOLID).
```java
interface IValidadorMedioDeContacto { boolean esValido(String medio); }
class NotificadorWhatsapp implements IEstrategiaNotificador, IValidadorMedioDeContacto { ... }
```
- **Muchas interfaces / un solo padre**: interfaces = obligaciones (se acumulan); herencia = código (2 padres = **problema del diamante**).
- Regex teléfono `^\+?\d{10,15}$` → estricto, rechaza espacios/guiones (limpiar antes).

### Antipatrón: `Object` + casts
```java
static void notificar(Object n, String c){
    IValidadorMedioDeContacto v = (IValidadorMedioDeContacto) n;   // cast NO verificado
    IEstrategiaNotificador    e = (IEstrategiaNotificador) n;
    if (v.esValido(c)) e.notificarUsuario(c, "..."); else System.out.println("inválido");
}
notificar(new Donante(...), c);   // COMPILA → ClassCastException en EJECUCIÓN
```
- Mal: pierde seguridad de tipos · mueve error de compilación → ejecución · traiciona polimorfismo · trade-off perdido.

### Forma correcta
```java
static <T extends IEstrategiaNotificador & IValidadorMedioDeContacto>
void notificar(T n, String c){ if(n.esValido(c)) n.notificarUsuario(c,"..."); }   // sin casts
// o: interface INotificadorValidable extends IEstrategiaNotificador, IValidadorMedioDeContacto {}
```
- Error inválido → NO compila (temprano, gratis).

---

## COMPOSICIÓN

| | Interfaces | Herencia |
|--|--|--|
| define | contrato (qué) | relación (qué es) |
| código | no | sí |
| cuántas | múltiples | una |
| estado | no | sí |
| obliga | implementar todo | nada |

- **Composición** = clase **tiene** a otra (atributo) y le **delega**. Relación **"tiene-un"**.
```java
class Auto { Motor motor; void arrancar(){ motor.encender(); } }   // tiene-un + delega
```
- **Criterio**: "es-un" → herencia · "tiene-un" → composición.
  - vianda es-una donación ✅ · auto tiene-un motor ✅ · donante tiene-una donación ✅ · donante es-una donación ❌.

### Trampa: heredar donante por tipo de donación
```java
class DonanteDeDinero extends Donante {}     // ❌ compila pero mal
```
1. dona 2 cosas → no entra (herencia única) → explosión de clases.
2. cambia con el tiempo → habría que crear otro objeto.
3. acoplamiento rígido.

### Solución: composición + interfaz
```java
interface IDonacion { int obtenerPuntaje(); }
class DonarDinero implements IDonacion {      // CLASE (aporta código); no interfaz
    Double monto;
    @Override public int obtenerPuntaje(){ return (int)(monto/1000); }
}
class Donante {
    IDonacion donacion;                        // tiene-un
    void donar(){ donacion.obtenerPuntaje(); } // delega
}
```
- Tipo nuevo = clase nueva; `Donante` no se toca.
- Contrato → interfaz · lógica concreta → clase.
- **Favorecer composición sobre herencia** (no dogma): herencia OK si "es-un" genuino.
- Misma herramienta: `DonarVianda extends Donacion` ✅ vs `DonanteDeDinero extends Donante` ❌.
- Semilla de **Strategy**.
- Justificación de diseño: **"elijo X porque priorizo [atributo calidad], pago con [costo]".**

---

## MAPA
es-un = herencia · sabe-hacer = interfaz · tiene-un = composición. Herencia+interfaz → polimorfismo.
**Antes de heredar: ¿"es-un" es honesto? Si lo forzás, componé.**
