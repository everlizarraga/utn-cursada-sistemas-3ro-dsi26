# 🛠️ Proyecto 0 — Países Local (Learning by Doing)

> **Premisa:** mientras leés los Bloques 1-5 del recorrido del código del profe (`rest-paises`), vos construís en paralelo una versión **simplificada y local** del mismo proyecto, sin Spring, sin API REST, solo Java + Maven. Cada etapa agrega conceptos nuevos y **reusa todo lo anterior**. Cuando llegues al código del profe, ya vas a tener la intuición construida.
>
> **Pensalo así:** el código del profe es el "Proyecto 1". Este es el "Proyecto 0" que te prepara para él.

---

## 🧭 Filosofía

- Cada etapa = un cambio incremental al mismo proyecto.
- Cada etapa introduce **uno o dos conceptos nuevos**.
- **Nada se descarta** — todo lo construido se reusa después.
- Vos codeás vos mismo. Yo te doy instrucciones + ejercicios, no código terminado para copy-paste.
- Si te trabás, preguntás por chat y desempantanamos.

---

## 📦 Stack mínimo

| Tool | Versión | Cuándo entra |
|---|---|---|
| Java | 21 | Etapa 0 |
| Maven | 3.x (el que viene con IntelliJ) | Etapa 0 |
| IntelliJ IDEA Community | Última | Etapa 0 |
| Lombok | Última | Etapa 3 |
| JUnit 5 | Última | Etapa 7 |

**Lo que NO entra todavía:** Spring, Spring Boot, RestTemplate, API REST, Jackson. Eso es para después, cuando "transformemos" el Proyecto 0 en algo cercano al Proyecto 1.

---

## 🗺️ Mapa de etapas

| Etapa | Concepto principal | Qué se construye |
|---|---|---|
| **0** | Maven + IntelliJ + estructura | Crear proyecto desde 0. Hola mundo. Entender `pom.xml` y `src/main/java`. |
| **1** | Clase, atributos, constructor, getters/setters | Clase `Pais` con nombre, capital, región, población. Constructor manual. |
| **2** | Listas, hardcodeo, iteración | Clase `CatalogoPaises` con `List<Pais>` hardcodeada. Método `getTodos()`. Recorrer con `for-each`. |
| **3** | Lombok | Limpiar boilerplate de `Pais` (getters/setters/constructor → `@Data`, `@AllArgsConstructor`). |
| **4** | Optional | Método `buscarPorNombre` que devuelve `Optional<Pais>`. Probar con nombre existente y no existente. |
| **5** | Streams + Lambdas | Métodos `filtrarPorRegion`, `filtrarPorPoblacionMinima`. Introducción a `.stream().filter().toList()`. |
| **6** | Map + estructuras anidadas | Agregar campo `Map<String, String> monedas` a `Pais`. Método `buscarPorMoneda`. |
| **7** | Tests con JUnit 5 | Tests unitarios del catálogo. Conceptos: `@Test`, `assertThat`, AAA pattern. |
| **8** | Excepciones | Manejar errores con `try/catch`. Diferenciar excepciones checked vs unchecked. |
| **9** | (opcional) IO de archivos | Leer países desde un CSV simple en vez de hardcodear. |

---

## 🔄 Cómo trabajamos juntos

1. Vos decís **"arranquemos etapa N"**.
2. Yo te genero el material de esa etapa: explicación + instrucciones paso a paso + ejercicios + criterios de "terminado".
3. **Vos codeás vos mismo.** El material te guía, pero el código lo tipeás vos.
4. Si te trabás, preguntás por chat. Resolvemos sin que yo te pase el código terminado a menos que sea inevitable.
5. Cuando completaste la etapa, decís **"listo, etapa N+1"** y seguimos.

Cada etapa va a tener su propio archivo `.md` en el proyecto, ej: `proyecto0-etapa1-clase-pais.md`.

---

## 🔗 Cómo encaja con los Bloques 1-5 del recorrido del código del profe

| Etapa Proyecto 0 | Concepto que prepara para... |
|---|---|
| 0 (Maven + IntelliJ) | Bloque 1 (`pom.xml`, estructura) |
| 1-3 (Clase Pais + Lombok) | Bloque 3 (DTOs del profe) |
| 4 (Optional) | Bloque 4 (`buscarPorNombre` del profe devuelve `Optional<Pais>`) |
| 5 (Streams) | Bloque 4 (lambdas que aparecen en el código) |
| 6 (Map) | Bloque 3 (campos `monedas`, `idiomas` del DTO `Pais`) |
| 7 (JUnit) | Bloque 5 (tests del profe) |

> **Idea:** podés alternar. Etapas 0-3 del Proyecto 0 + Bloque 1 del recorrido. Después Etapa 4 + Bloque 2. Etc. O hacés todo el Proyecto 0 primero y después los Bloques. Lo definimos sobre la marcha.

---

## 🎯 Dominio: Países (alineado con el código del profe)

Elegí "países" para que rime con el proyecto del profe. Los datos van a ser hardcodeados al principio: Argentina, Brasil, Chile, Perú, España, Francia, Alemania, etc. Cuando llegues al Bloque 4 del recorrido, vas a tener intuición construida desde 0 sobre la estructura.

**Si preferís otro dominio** (libros, canciones, productos, juegos, lo que sea), avisame antes de la Etapa 1 y lo cambiamos. La estructura del aprendizaje es la misma; solo cambia el dominio.

---

## 🌱 Estructura final esperada (al terminar la Etapa 7)

```
proyecto0-paises-local/
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/
│   │       └── ar/edu/tu_apellido/proyecto0/
│   │           ├── Main.java
│   │           ├── modelo/
│   │           │   └── Pais.java
│   │           └── catalogo/
│   │               └── CatalogoPaises.java
│   └── test/
│       └── java/
│           └── ar/edu/tu_apellido/proyecto0/
│               └── catalogo/
│                   └── CatalogoPaisesTest.java
```

Cuando termines, vas a haber tocado: estructura Maven, encapsulamiento, generics, Optional, streams, lambdas, Map, annotations (vía Lombok), y tests con JUnit. Es exactamente el toolbox que necesitás para leer el código del profe sin sufrir.

---

## ▶️ Próximo paso

Decime **"arranquemos etapa 0"** y te genero el material para crear el proyecto desde 0 en IntelliJ con Maven, sin Spring.

Si querés cambiar el dominio antes de empezar, decímelo ahora.
