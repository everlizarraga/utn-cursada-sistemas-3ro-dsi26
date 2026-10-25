package io.github.everlizarraga.c04py01veterinaria.utils;

public class GeneradorIdSecuencial {
  private long ultimo = 0;
  public long sigueinte() {
    return ++ultimo;
  }
}
