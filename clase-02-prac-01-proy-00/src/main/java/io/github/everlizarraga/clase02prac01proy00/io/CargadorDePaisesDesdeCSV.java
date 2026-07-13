package io.github.everlizarraga.clase02prac01proy00.io;

import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CargadorDePaisesDesdeCSV {
  // Cuando no se le especifica un constructor,
  // por defecto se le asigna un constructor vacío.

  public String cargarFormaA(String path) throws IOException {
    return Files.readString(Paths.get(path));
  }

  public List<String> cargarFormaB(String path) throws IOException {
    return Files.readAllLines(Paths.get(path)); // Path.of("ruta/al/archivo")
  }

  public List<String> cargarFormaC(String path) throws IOException {
    List<String> respuesta = new ArrayList<>();
    //Files.lines te da un Stream<String> perezoso —
    // lee línea por línea según las necesitás, sin cargar todo el archivo en memoria.
    //  Es lo más eficiente para archivos grandes.
    try(Stream<String> lineas = Files.lines(Path.of(path))) { // Paths.get("ruta/al/archivo")
      respuesta = lineas
          .skip(1)
          .map(s -> (" [Stream]> " + s))
          .toList();
      return  respuesta;
    }
  }

  public List<Pais> cargarDesde(Path archivo) throws IOException {
    try (Stream<String> lineas = Files.lines(archivo)) {
      return lineas
          .skip(1)                   // saltar el header
          .map(this::parsearLinea)
          .toList();
    }
  }

  private Pais parsearLinea(String linea) {
    String[] campos = linea.split(",");
    return new Pais(
        campos[0],                      // nombre
        campos[1],                      // capital
        campos[2],                      // region
        Long.parseLong(campos[3]),      // poblacion
        Map.of(),                       // monedas (vacío por ahora)
        Map.of()                        // idiomas (vacío por ahora)
    );
  }
}
