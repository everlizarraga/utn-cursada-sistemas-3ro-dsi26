package io.github.everlizarraga.clase02prac01proy00.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleMoneda {
  private String nombre;
  private String simbolo;
}
