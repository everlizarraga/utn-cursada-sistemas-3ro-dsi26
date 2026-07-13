package io.github.everlizarraga.clase02prac01proy00.etapas;

import io.github.everlizarraga.clase02prac01proy00.catalogo.CatalogoPaises;
import io.github.everlizarraga.clase02prac01proy00.io.CargadorDePaisesDesdeCSV;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Etapa09 implements EjecutarEtapa{
  @Override
  public void run() {
    System.out.println(":::::: Ejecutando etapa 09 ::::::");
    String path = "clase-02-prac-01-proy-00/src/main/resources/paises.csv";
    //this.cargaMuestra(path);
    //this.cargarComoUnSoloString(path);
    //this.cargarComoListaDeStrings(path);
    this.cargarComoStream(path);

  }

  private void cargaMuestra(String path) {
    try {
      // Es necesario poner el modulo delante de la ruta para que lo detecte
      // Porque esto es un pryecto multimodulo
      Path archivo = Path.of(path);
      CatalogoPaises catalogo = new CatalogoPaises(archivo);

      System.out.println("Cargué " + catalogo.cantidad() + " países desde el CSV:");
      catalogo.getTodos().forEach(p -> System.out.println("  - " + p.getNombre()));

      System.out.println("\n=== Países de Americas ===");
      catalogo.buscarPorRegion("Americas")
          .forEach(p -> System.out.println("  - " + p.getNombre()));

    } catch (IOException e) {
      System.err.println("Error leyendo el archivo: " + e.getMessage());
      System.out.println("> " + Arrays.toString(e.getStackTrace()));
    }
  }

  private void cargarComoUnSoloString(String path) {
    try {
      CargadorDePaisesDesdeCSV cargador = new CargadorDePaisesDesdeCSV();
      String todaLaCargaEnUnString = cargador.cargarFormaA(path);
      System.out.println("Cargando en un solo String: ");
      System.out.println("-----------------");
      System.out.println(" > " + todaLaCargaEnUnString);
      System.out.println("-----------------");
    } catch (IOException e) {
      System.err.println("Error leyendo el archivo: " + e.getMessage());
    }
  }

  private void cargarComoListaDeStrings(String path) {
    try {
      CargadorDePaisesDesdeCSV cargador = new CargadorDePaisesDesdeCSV();
      List<String> paisesCargados = cargador.cargarFormaB(path);
      System.out.println("Cargar como Lista de Strings: ");
      paisesCargados.forEach(p -> System.out.println(" > " + p));
    } catch (IOException e) {
      System.err.println("Error leyendo el archivo: " + e.getMessage());
    }
  }

  private void cargarComoStream(String path) {
    try {
      CargadorDePaisesDesdeCSV cargador = new CargadorDePaisesDesdeCSV();
      List<String> lista = cargador.cargarFormaC(path);
      lista.forEach(System.out::println);
    } catch (Exception e) {
      System.err.println("Error leyendo el archivo: " + e.getMessage());
    }
  }

}
