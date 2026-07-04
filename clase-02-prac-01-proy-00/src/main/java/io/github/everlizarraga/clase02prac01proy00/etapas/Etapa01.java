package io.github.everlizarraga.clase02prac01proy00.etapas;

import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

public class Etapa01 implements EjecutarEtapa {
  @Override
  public void run() {
    Pais unPais = new Pais();
    unPais.setNombre("Argentina");
    unPais.setCapital("Buenos Aires");
    System.out.println("Etapa 01: " + unPais);
  }
}
