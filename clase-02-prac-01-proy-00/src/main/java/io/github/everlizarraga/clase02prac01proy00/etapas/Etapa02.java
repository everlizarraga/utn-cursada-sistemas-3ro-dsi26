package io.github.everlizarraga.clase02prac01proy00.etapas;

import io.github.everlizarraga.clase02prac01proy00.catalogo.CatalogoPaises;
import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

import java.util.List;

public class Etapa02 implements EjecutarEtapa {

  @Override
  public void run() {
    // Crear el catálogo
    CatalogoPaises catalogo = new CatalogoPaises();

    // 1. Imprimir cuántos hay
    System.out.println("Tengo " + catalogo.cantidad() + " países en el catálogo.");
    System.out.println("---");

    // 2. Recorrer con for-each — la forma idiomática
    System.out.println("Lista Completa:");
    for (Pais p : catalogo.getTodos()) {
      System.out.println(" > " + p);
    }
    System.out.println("---");

    // 3. Recorrer con for clásico (con índice)
    System.out.println("Solo nombres con número:");
    List<Pais> paises = catalogo.getTodos();
    for (int i = 0; i < paises.size(); i++) {
      System.out.println(" [" + (i + 1) + "]> " + paises.get(i).getNombre());
    }
  }
}
