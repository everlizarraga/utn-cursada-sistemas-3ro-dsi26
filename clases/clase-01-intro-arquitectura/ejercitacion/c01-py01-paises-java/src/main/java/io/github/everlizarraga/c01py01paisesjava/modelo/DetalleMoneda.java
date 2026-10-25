package io.github.everlizarraga.c01py01paisesjava.modelo;

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
