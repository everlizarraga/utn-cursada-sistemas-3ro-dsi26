package io.github.everlizarraga.clase04prac01proy00.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
//@NoArgsConstructor
public class TurnoRequestClass {
  private String mascota;
  private String dia;
  @JsonProperty(value = "duracion-minutos")
  private Integer duracionMinutos;
}
