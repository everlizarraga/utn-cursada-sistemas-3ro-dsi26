package io.github.everlizarraga.c01py01paisesjava.catalogo;

public class PaisNoEncontradoException extends RuntimeException {
  private final String nombreBuscado;

  public PaisNoEncontradoException(String nombreBuscado) {
    super("País no encontrado: " + nombreBuscado);
    this.nombreBuscado = nombreBuscado;
  }

  public String getNombreBuscado() {
    return nombreBuscado;
  }
}
