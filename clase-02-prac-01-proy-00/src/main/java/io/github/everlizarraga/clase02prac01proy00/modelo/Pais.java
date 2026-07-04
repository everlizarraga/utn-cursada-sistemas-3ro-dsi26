package io.github.everlizarraga.clase02prac01proy00.modelo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Builder
@ToString(exclude = "poblacion")
public class Pais {
  @NonNull
  private String nombre;
  @NonNull
  private String capital;
  private String region;
  private Long poblacion;
  private Map<String, DetalleMoneda> monedas;
  private Map<String, String> idiomas;
}

/*
@NonNull no es un recordatorio:
hace que Lombok genere chequeos automáticos de null en el constructor y
setter de ese campo. Lo pusiste sobre nombre y capital para marcar que
un país no puede existir sin ellos — si alguien intenta crear uno con
esos valores en null, explota al instante con un mensaje claro,
en vez de fallar confusamente después.

@NoArgsConstructor → ojo, este genera el constructor vacío SIN chequeos
(no puede chequear nada, no recibe argumentos). Así que con el constructor
vacío sí podrías tener nombre null momentáneamente.

@Data (que incluye @Setter) → los setters de nombre y capital también
van a chequear null.
pais.setNombre(null) explotaría.
* */

/*
 * LOMBOK - Constructores:
 * @NoArgsConstructor    → Pais()                          (vacío)
 * @RequiredArgsConstructor → Pais(nombre, capital)        (solo @NonNull y final)
 * @AllArgsConstructor   → Pais(nombre, capital, region...) (todos los campos)
 *
 * @NonNull → genera chequeo de null en constructor y setter (falla rápido si es null)
 *
 * Combinación específica de campos → escribir el constructor a mano (Lombok lo respeta)
 */

