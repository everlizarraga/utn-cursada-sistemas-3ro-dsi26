package io.github.everlizarraga.clase04prac01proy00.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
//@NoArgsConstructor
public class TurnoRequestClass {
  /*
   * IMPORTANTE sobre @JsonProperty(required = true):
   * - En clases, Jackson instancia el objeto con el constructor vacío y luego setea los valores.
   * - Debido a este proceso en dos pasos, Jackson ignora la restricción de obligatoriedad;
   *   si el JSON no trae el campo, simplemente queda en 'null' sin lanzar error.
   * - En este contexto, 'required=true' solo sirve para generar documentación (ej. Swagger/OpenAPI).
   * - (Nota: En los Records sí funciona porque Jackson usa el constructor parametrizado directamente).
   *
   * Cómo procesa Jackson los Records (Un solo paso) <<<<<<<<<<<<<<<
   * Los Records en Java son inmutables. No tienen constructor vacío ni setters.
   * Solo tienen el constructor principal donde se deben proveer todos los valores al mismo tiempo.
   * Para poder instanciar un Record,
   * Jackson está obligado a usar ese constructor mediante un mecanismo interno que llama Creator.
   * El proceso cambia:
   * > Jackson escanea el JSON y recolecta todos los valores.
   * > Instancia el objeto de un solo golpe llamando al constructor: new TurnoRequest(mascota, dia, duracion).

   * * Cuando Jackson evalúa los parámetros de un "Creator"
   * y detecta la anotación @JsonProperty(required = true),
   * su motor de deserialización sí hace cumplir la regla antes de intentar crear el objeto.
   * Si el dato falta, Jackson no puede llamar al constructor correctamente y
   * lanza una excepción (MismatchedInputException).
   *
   * -> Para validación real en Spring Boot usar: @NotNull o @NotBlank de Jakarta Validation.
   */
  @JsonProperty(required = true)
  private String mascota;
  private String dia;
  @JsonProperty(value = "duracion-minutos")
  private Integer duracionMinutos;
}
