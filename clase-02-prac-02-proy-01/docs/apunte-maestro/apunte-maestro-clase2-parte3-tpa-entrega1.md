# 📘 Apunte Maestro — Clase 2 — Parte 3: TPA DonaTrack — Entrega 1

**Materia:** Diseño de Sistemas de Información (DSI) — UTN FRBA  
**Clase:** 2 — 01/04/2026  
**Fuente:** Transcripción de clase (bloque final con Ailu y Gonza) + PDF del TPA  

---

## Contexto

Al final de la clase 2 se presentó el TPA (Trabajo Práctico Anual): **DonaTrack**, un sistema de gestión y trazabilidad de donaciones basado en la iniciativa UTN Solidaria. Tiene 6 entregas a lo largo del año. Este apunte resume lo que dijeron en clase + lo que necesitás saber de la Entrega 1.

---

## 🔴 El sistema: DonaTrack

Sistema para organizar, registrar y monitorear donaciones desde su recepción en el depósito hasta su entrega a entidades beneficiarias. Arquitectura distribuida en servicios (microservicios).

### Los servicios del sistema

| Servicio | Qué hace |
|---|---|
| **Servicio de Donaciones** | Gestión de donantes (humanos y jurídicos), donaciones, entidades beneficiarias |
| **Servicio de Logística** | Gestión de camiones, rutas de entrega, trazabilidad de entregas |
| **Servicio de Incentivos** | Analítica de donantes, misiones, insignias, rankings, gamificación |
| **Servicio de Notificaciones** | Envío de notificaciones por email, SMS, WhatsApp |
| **Servicio de Autenticación** | Gestión de tokens y autenticación |
| **Frontend (SSR)** | Componente de presentación con server-side rendering (Entrega 5) |

### Tecnologías

Java 21 + Spring Boot + Maven. Cada servicio es un proyecto Spring Boot independiente. Base de datos MySQL (relacional) para la mayoría de los servicios, documental para Logística.

---

## 🔴 Entrega 1 — Lo que hay que presentar

**Fecha:** Semana del 20 de abril (entrega clase 2, defensa clase 5 el 22/04).

### Alcance

Solo dos servicios para esta entrega: **Donaciones** y **Notificaciones** (primera iteración).

### Entregables concretos

| # | Entregable | Detalle |
|---|---|---|
| 1 | **Diagrama de clases** | Un diagrama por servicio (Donaciones + Notificaciones). Modelo de dominio |
| 2 | **Diagrama de arquitectura** | Despliegue y/o componentes. Demostrar que entienden la arquitectura |
| 3 | **Justificaciones de diseño** | Documento explicando por qué tomaron las decisiones que tomaron |
| 4 | **Diagrama de casos de uso** | General, todos los actores, todas las funcionalidades |
| 5 | **Bocetos de interfaz** | De los 20 requerimientos de UI/UX. Pueden ser a mano, en Paint, lo que sea. No HTML, no Figma |
| 6 | **Implementación** | Código funcionando de los requerimientos |
| 7 | **Endpoint simple** | El Servicio de Donaciones debe exponer un GET que devuelva "Hola desde el servicio de Donaciones" |

### Pregunta de discusión (todos deben poder responderla)

> *"¿Un modelo de dominio rico es una inversión necesaria para capturar la complejidad del negocio o una sobreingeniería innecesaria frente a un modelo anémico más simple?"*

---

## 🔴 Orden de trabajo recomendado (lo que dijeron en clase)

Los ayudantes fueron explícitos con el orden:

1. **Primero:** Diagrama de casos de uso — "es lo primero que hagan, ya lo pueden hacer, no hay nada que les tengamos que enseñar para esto"
2. **Segundo:** Bocetos de interfaz — "tiene que ser simple, puede ser en papel, en Paint, desprolijo. Son descartables. No pierdan tiempo con esto"
3. **Tercero:** Diagrama de clases — "pensar el modelo entre todos, sin asistirse por IA. La parte de pensar háganla juntos"
4. **Cuarto:** Implementación (código) — "esto es lo último, no se tiren de lleno al código porque si no lo van a tener que modificar"

### Lo que dijeron que NO hagan todavía

- **No empezar por el código** — primero diseñar, después codear.
- **No adelantarse con HTML/Figma** — los bocetos son dibujitos, no maquetas. Las maquetas reales son para la Entrega 4.
- **No exponer endpoints REST todavía** (salvo el "Hola desde el servicio") — eso se ve la semana siguiente.
- **El endpoint simple** se va a explicar en la clase 3. Pueden esperar.

---

## 🟡 Dominio de la Entrega 1 — Resumen

### Donantes

- **Personas humanas:** nombre, apellido, edad, documento, género, dirección + al menos un medio de contacto (email obligatorio, teléfono y WhatsApp opcionales). Eligen medio de contacto predeterminado.
- **Personas jurídicas:** razón social, tipo (Gubernamental, ONG, Empresa, Institución), rubro + medio de contacto. Tienen personas representantes.

### Donaciones y segmentación

- El admin registra la donación en nombre del donante.
- Cada donación tiene: descripción general + bienes. Cada bien tiene descripción, foto (opcional), categoría → subcategoría, estado (nuevo/usado si aplica), fecha de vencimiento (si perecedero), cantidad + unidad.
- El sistema **segmenta automáticamente** la carga en múltiples donaciones independientes agrupadas por subcategoría. La subcategoría es la unidad mínima de asignación.

### Entidades beneficiarias

- Organizaciones sin fines de lucro (escuelas, comedores, etc.). Razón social, dirección, teléfono, personas representantes.
- Registran **necesidades materiales**: subcategoría + descripción.
  - **Extraordinarias:** surgen por situaciones excepcionales (inundación, incendio). Se cubren con donaciones parciales hasta alcanzar la cantidad.
  - **Recurrentes:** consumo habitual periódico (ej: 100 paquetes de fideos por semana).

### Importación masiva por CSV

- Importar donantes desde archivo CSV (puede tener 20.000+ filas).
- Si el email ya existe → actualizar datos. Si no existe → crear usuario y enviar credenciales.

### Notificaciones (primera iteración)

- Componente que recibe: destinatario + mensaje + medio (email, SMS, WhatsApp).
- En esta entrega: **simular** el envío (no integración real). Marcar como completada.

---

## 🟢 Info operativa de la clase

- **Tutores personales:** aún no asignados al momento de la clase. Cuando estén, avisan por Discord + se crea canal por equipo.
- **Canal general de Discord:** solo para dudas de enunciado ("no entiendo qué me piden"). Dudas de implementación → con el tutor asignado.
- **Repos:** la cátedra va a dar los repos para que empiecen a codear. Esperar el aviso.
- **IDE:** IntelliJ recomendado (Community Edition alcanza). Con cuenta estudiantil se puede sacar licencia Pro gratis.
- **Tip de IntelliJ:** consume bastante RAM. Si tienen varios proyectos abiertos a la vez, puede ponerse lento. El auto-save puede trabar si el disco es lento (se puede deshabilitar).

---

**Fin de la Parte 3 — Clase 2**
