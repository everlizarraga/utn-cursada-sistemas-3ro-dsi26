package io.github.everlizarraga.clase02prac01proy00.etapas;

import io.github.everlizarraga.clase02prac01proy00.catalogo.CatalogoPaises;
import io.github.everlizarraga.clase02prac01proy00.catalogo.PaisNoEncontradoException;
import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

public class Etapa08 implements EjecutarEtapa {
  @Override
  public void run() {
    System.out.println(":::::: Ejecutando etapa 08 ::::::");

    CatalogoPaises catalogo = new CatalogoPaises();

    try {
      Pais argentina = catalogo.buscarPorNombreObligatorio("Argentina");
      System.out.println("OK: " + argentina);

      Pais atlantis = catalogo.buscarPorNombreObligatorio("Atlantis");
      System.out.println("OK: " + atlantis);
    } catch (PaisNoEncontradoException e) {
      System.out.println("Error de búsqueda: " + e.getMessage());
      System.out.println("Buscábamos: " + e.getNombreBuscado());
    } catch (IllegalArgumentException e) {
      System.out.println("Argumento inválido: " + e.getMessage());
    }
  }
}
