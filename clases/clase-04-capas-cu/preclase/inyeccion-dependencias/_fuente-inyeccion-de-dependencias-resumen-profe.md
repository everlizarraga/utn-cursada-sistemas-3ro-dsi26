# Inyección de Dependencias — Diseño de Sistemas (UTN.BA)

> Conversión fiel a Markdown de _Lectura__Inyeccion_de_Dependencias_Resumen.pdf (8 slides).

---

## Slide 1 — Diseño de Sistemas

**Diseño de Sistemas**

*(Portada de la materia. La franja superior muestra un boceto decorativo de diagrama de clases UML: cajas vacías conectadas con multiplicidades visibles —`1`, `1..*`, `0..*`, `*`— y una flecha de generalización/herencia con triángulo hueco. No hay nombres de clase ni atributos escritos en las cajas. A la derecha, íconos decorativos: una lámpara sobre un documento con un esquema jerárquico, y un ícono de ventana de código `</>`. Al pie, el logo UTN.BA — Cátedra Diseño de Sistemas de Información.)*

---

## Slide 2 — Inyección de Dependencias

**Inyección de Dependencias**

*(Portada de sección. Ilustración isométrica decorativa: personas trabajando frente a monitores, una plataforma con servidores/datos, y al fondo un diagrama de flujo/UML tenue. Pie: "Cátedra Diseño de Sistemas – UTN BA".)*

---

## Slide 3 — Inyección de Dependencias

Partamos del siguiente ejemplo:

`>> Incidente`

```java
public void enviarAvisos(){
  this.interesados.forEach(i -> whatsAppSender.enviar(i, “Ha ocurrido un incidente”));
}
```

Suponiendo que existe un objeto “whatsAppSender”, ¿cómo llegamos a conocerlo? Algunas opciones:

- Tener una única instancia de su clase accesible globalmente.
- Obtener ese objeto a través de otro que lo provea.
- Que a la clase Incidente le llegue por parámetro whatsAppSender

---

## Slide 4 — Inyección de Dependencias - Singleton

`>> Incidente`

```java
public void enviarAvisos(){
  this.interesados.forEach(i -> WhatsAppSender.getInstance().enviar(i, “Ha ocurrido un incidente”));
}
```

`>> WhatsAppSender`

```java
private static WhatsAppSender instance = null;
public static WhatsAppSender getInstance() {
  if(instance == null)
    instance = new WhatsAppSender(); // más toda la configuración
  return instance;
}
```

---

## Slide 5 — Inyección de Dependencias – Service Locator

`>> Incidente`

```java
public void enviarAvisos(){
  this.interesados.forEach(i -> ServiceLocator.get(“whatsAppSender”).enviar(i, “Ha ocurrido un incidente”));
}
```

---

## Slide 6 — Inyección de Dependencias – Inyectando la dependencia

`>> Incidente`

```java
public Incidente(WhatsAppSender whatsAppSender){…}
public void enviarAvisos(){
  this.interesados.forEach(i -> this.whatsAppSender.enviar(i, “Ha ocurrido un incidente”));
}
```

---

## Slide 7 — Inyección de Dependencias

Suponiendo que A, instancia de la clase 1, necesita a B, instancia de la clase 2, para poder realizar la tarea X…

**Singleton**

- A solicita a clase 2 una instancia, la cual devuelve siempre la instancia B.
- “B” es un objeto global, única instancia para toda la ejecución.
- Fuerte acoplamiento entre Clase 1 y Clase 2.
- Difícil de testear pues es complicado mockear a B

**Service Locator**

- A solicita al Service Locator alguien que sea capaz de realizar la tarea X y éste le devuelve la instancia B o algún otro objeto que cumpla con la misma interface.
- El Service Locator es un objeto global que permite generar distintas configuraciones.
- Permite el mockeo de objetos

**Inyección de Dependencias**

- A recibe como parámetro, en su constructor, a B o algún otro objeto que cumpla con la misma interface.
- A no solicita a nadie la instancia B (o algún otro similar), sino que “le llega desde afuera”.
- Es más testeable pues permite el mockeo de objetos.
- Se puede combinar con las anteriores.

---

## Slide 8 — Gracias

**Gracias**

*(Slide de cierre. Foto de una persona escribiendo con lápiz sobre un pupitre, en contexto de examen/clase. Al pie, logo UTN.BA — Cátedra Diseño de Sistemas de Información.)*

---

### Notas de conversión

- **Comillas del código:** los literales de string y las referencias entre comillas dentro del código usan comillas tipográficas curvas (`“ ”`, U+201C/U+201D), no comillas rectas. Se transcriben tal cual el original; como código Java literal no compilarían con esas comillas.
- La etiqueta `>> Incidente` / `>> WhatsAppSender` que precede a cada bloque de código indica la clase a la que pertenece ese código; se conserva como línea previa al bloque.
- Las slides 1, 2 y 8 son de portada/cierre con ilustraciones decorativas (no diagramas informativos): se describen textualmente sin reconstruir en ASCII. El boceto UML de la slide 1 tiene cajas vacías (sin nombres de clase), por lo que solo se describen las multiplicidades y la flecha de herencia visibles, sin inventar contenido.
- **Pie de página:** todas las slides de contenido (3–7) llevan el pie "Cátedra Diseño de Sistemas de Información – UTN BA" con el número de slide. La slide 2 varía levemente: "Cátedra Diseño de Sistemas – UTN BA" (sin "de Información").
- Sin hipervínculos en las anotaciones del PDF.

---

**FIN DEL ARCHIVO FUENTE — Inyección de Dependencias (Diseño de Sistemas)**
