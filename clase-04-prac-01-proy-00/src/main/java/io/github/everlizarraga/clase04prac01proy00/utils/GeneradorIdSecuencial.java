package io.github.everlizarraga.clase04prac01proy00.utils;

public class GeneradorIdSecuencial {
  private long ultimo = 0;
  public long sigueinte() {
    return ++ultimo;
  }
}
