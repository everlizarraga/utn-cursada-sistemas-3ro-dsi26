# utn-cursada-sistemas-3ro-dsi26

Aprendizaje y entrenamiento de Diseño de Sistemas de la Información (UTN FRBA, 3er año, 2026).

## Estructura

La raíz **no es un proyecto Maven**: es una carpeta común con una subcarpeta por clase. Cada proyecto de ejercitación es independiente y tiene su propio `pom.xml` y su wrapper de Maven (`mvnw`).

```
clases/
└── clase-NN-tema/
    ├── preclase/            material previo obligatorio de la clase
    ├── apuntes/             apuntes, clase desde cero, recorridos
    ├── material-adicional/  fuentes de la cátedra, diagramas, imágenes
    └── ejercitacion/
        └── cNN-pyNN-nombre/     un proyecto (simple o multimódulo)
            └── cNN-pyNN-modulo/ módulos, si el proyecto es multimódulo
```

Cada clase tiene solo las carpetas que usa.

## Convención de nombres

- **Carpeta = `artifactId`**, en proyectos y en módulos: `cNN-pyNN-nombre` (número de clase, número de proyecto dentro de la clase, nombre). Ej.: `c02-py02-apis-sin-key` y su módulo `c02-py02-jsonph`.
- **Paquete Java:** `io.github.everlizarraga.cNNpyNNnombre` (sin guiones). Ej.: `io.github.everlizarraga.c02py02jsonph`.
- **Clase principal:** `CNNPyNNNombreApplication`. Ej.: `C02Py02JsonphApplication`.

## Abrir en IntelliJ

1. `File → Open` sobre la carpeta raíz del repo.
2. En la ventana de Maven, `+` (Add Maven Project) y elegir el `pom.xml` de cada proyecto que quieras usar. En los multimódulo, el `pom.xml` de la carpeta del proyecto (trae sus módulos).

## Correr desde consola

```
cd clases/clase-04-capas-cu/ejercitacion/c04-py01-veterinaria
./mvnw spring-boot:run
```

En un multimódulo, desde la carpeta del proyecto: `./mvnw -pl c02-py02-jsonph spring-boot:run`.
