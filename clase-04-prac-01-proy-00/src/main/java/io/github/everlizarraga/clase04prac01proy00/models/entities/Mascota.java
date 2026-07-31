package io.github.everlizarraga.clase04prac01proy00.models.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Mascota {
  @Setter private Long id;
  @Setter private String nombre;     // Setters: el PUT/PATCH de las
  @Setter private String especie;    // etapas 7-8 los van a necesitar.

  public Mascota(Long id, String nombre, String especie) {
    this.id = id;
    this.nombre = nombre;
    this.especie = especie;
  }
}
