package io.github.everlizarraga.clase02prac01proy00.etapas;

import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

public class Etapa03 implements EjecutarEtapa {

  @Override
  public void run() {
    //Pais unPais = new Pais("Argentina", "BsAs", null, null, null, null);
    Pais unPais = new Pais("Argentina", "BsAs");
    System.out.println("Etapa 03: " + unPais);

    Pais p = Pais.builder()
        .nombre("Argentina")
        .capital("Buenos Aires")
        .region("Americas")
        .poblacion(45000000L)
        .build();
    System.out.println(" > Builder: " + p);
  }
}
