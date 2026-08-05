package io.github.everlizarraga.clase04prac01proy00.models.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter @Setter
public class Propietario {
  private Long id;
  private String nombre;
  private String telefono;
  @Setter(AccessLevel.NONE)
  private final List<Mascota> mascotas;

  public Propietario(Long id, String nombre, String telefono) {
    this.id = id;
    this.nombre = nombre;
    this.telefono = telefono;
    this.mascotas = new ArrayList<>();
  }

  public void agregarMascota(Mascota mascota) {
    if (mascotas.size() >= 5) {
      throw new IllegalArgumentException(
          "Un propietario no puede tener más de 5 mascotas");
    }
    mascotas.add(mascota);
  }
  // ↑ LA regla de negocio del dominio — vive en la entidad, habla el idioma
  //   genérico de Java (IllegalArgumentException): el dominio no conoce
  //   HTTP, ni Spring, ni excepciones "del sistema". Guardá el detalle:
  //   en la Etapa 6 alguien va a tener que TRADUCIR este grito.
  public void eliminarMascota(Mascota mascota) {
    this.mascotas.removeIf(m -> Objects.equals(m.getId(), mascota.getId()));
  }
}
