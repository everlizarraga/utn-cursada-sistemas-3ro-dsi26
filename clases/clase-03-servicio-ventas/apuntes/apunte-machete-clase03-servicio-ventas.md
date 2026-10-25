# ⚡ Machete — Clase 03: Servicio de Ventas (SmartLife)

`clase03` · Modelado de Dominio en Objetos. Pistas mínimas.

---

**1. El caso.** SmartLife = orientado a servicios. Servicio de Ventas = solo dominio (sin REST ni eventos). Precio final = base + impuestos.
- IVA (todos) = `0.21·base` → 21
- EO (electrónicos) = `0.5·base + 4·4` → 66
- EI (hogar) = `base/4 + 0.3·3.5` → 26.05
- Interesados: SFFA (SDK) · SVIBAA (REST).

**2. Diseñar.** = alternativas → ventajas/desventajas → elegir justificando. Al justificar, definir el atributo de calidad. Herramienta: PlantUML.

**3. Comercio.** Solo `id` (SOA + YAGNI). Colección → inicializada en el constructor, nunca por setter. Altas por método (varargs).

**4. Producto — 4 alternativas.**
1. Abstracta + subclases → tipo nuevo = clase; repetición.
2. Interfaz + concretas → peor: repetís lógica y atributos.
3. **✅ Producto + `TipoProducto` (ambos concretos)** → Hogar/Electrónico = **instancias**; tipo nuevo = instancia en runtime; Producto delega impuestos al tipo.
4. Tipo como enum → descartada: valores fijos, hardcodeo.

**Regla de oro:** heredar por tipificación = error grave. Heredar **solo si hay comportamiento distinto**. Producto → no hereda. Impuesto → sí se separa.

```
Producto: precioFinal() = base + tipo.totalImpuestos(this)
TipoProducto: totalImpuestos(p) = impuestos.stream().mapToDouble(i -> i.calcular(p)).sum()
```

**5. Impuesto.** Comportamiento distinto en cada uno → **interfaz** + IVA/EO/EI concretas. Interfaz > abstracta: (a) Java implementa N interfaces / hereda de 1; (b) nada común real arriba.

**6. Código impuestos.**
```
interface Impuesto { double calcular(Producto p); }
IVA:  0.21·base
EO:   0.5·base + 4·4
EI:   base/4 + 0.3·3.5
```
- `calcular` recibe el Producto entero (flexibilidad ↔ acopla).
- Parámetros `static` (atributo de clase, configurables) → tests deben resetearlos.

**7. Packages.** Por concepto: comercio / productos / impuestos / venta / observers / utils.

**8. Tests.** `@BeforeEach`: reset statics + crear tipos con impuestos.
```
electrónico(100): totalImpuestos=87 · precioFinal=187
assertEquals(esperado, real, 1)   // delta por double
```
Esperado calculado, nunca a ojo. Tipo nuevo en test = trivial.

**9. Venta / ItemVenta.** Venta → lista de `ItemVenta` (producto + cantidad), como renglón de factura. Totales delegan (Venta→Item→Producto, ×cantidad). `cantidad > 0`.

**10. Comercio completo.** Colecciones: productos, ventas, observadores.
```
agregarVenta(v): validar productos propios → ventas.add(v) → observadores.forEach(o -> o.serNotificadoDe(v))
```

**11. Observer.** Notificar interesados sin acoplar.
```
interface ObservadorVenta { void serNotificadoDe(Venta v); }
Comercio: List<ObservadorVenta> + forEach(serNotificadoDe)
ObservadorSffa / ObservadorSvibaa implements
```
Interesado nuevo = clase nueva + agregar a la lista, sin tocar Comercio. Analogía: newsletter.

**11.1. Paralelo vs secuencial.** Secuencial: simple, ordenado; uno lento/falla afecta. Paralelo: rápido pero concurrencia + sin orden. Abierto.

**12. SDK / biblioteca / framework.** SDK = kit de integración de un externo. Biblioteca la llamás vos ↔ framework te llama a vos (Spring).

**13. Cabos sueltos.** Congelar precio en ItemVenta (pend). `id` de Comercio se usa al conectar REST. PlantUML u otra.

**14. Modelo completo.**
```
Comercio ─* Producto ─ TipoProducto ─* «I»Impuesto (IVA/EO/EI)
   ├─* Venta ─* ItemVenta ─ Producto
   └─* «I»ObservadorVenta ◀ Sffa(SDK) / Svibaa(REST)
```
Delegación: impuestos (Prod→Tipo→Imp) · totales (Venta→Item→Prod). Herencia ≠ implementación (punteada).

**15. Test completo.**
```
comercio.agregarVenta(venta)
venta.totalFinal() == 187 · observadorSffa.getCantLlamadas() == 1
```
Casos: electrónico 187 · hogar 147.05 · 2 electrónicos ×2 · mixta.

**16. Recall.** Diseñar/alternativas · Comercio-id/SOA · tipificación · Tipo=instancia · interfaz>abstracta · calcular(Producto) · static+tests · Observer · Venta/ItemVenta · paralelo/secuencial.

---

**FIN DEL MACHETE — CLASE 03**
