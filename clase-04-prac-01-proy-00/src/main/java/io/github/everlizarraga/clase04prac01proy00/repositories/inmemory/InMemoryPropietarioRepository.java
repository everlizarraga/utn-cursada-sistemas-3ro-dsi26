package io.github.everlizarraga.clase04prac01proy00.repositories.inmemory;

import io.github.everlizarraga.clase04prac01proy00.models.entities.Propietario;
import io.github.everlizarraga.clase04prac01proy00.repositories.PropietarioRepository;
import io.github.everlizarraga.clase04prac01proy00.utils.GeneradorIdSecuencial;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository //@Component
public class InMemoryPropietarioRepository implements PropietarioRepository {

  private final List<Propietario> propietarios;
  private final GeneradorIdSecuencial generadorId;

  public InMemoryPropietarioRepository() {
    this.propietarios = new ArrayList<>();
    this.generadorId = new GeneradorIdSecuencial();
  }

  @Override
  public List<Propietario> findAll() {
    return new ArrayList<>(this.propietarios);
  }

  @Override
  public Optional<Propietario> findById(Long id) {
    return this.propietarios.stream()
        .filter(p -> Objects.equals(p.getId(), id))
        .findFirst();
  }

  @Override
  public Propietario save(Propietario propietario) {
    if (propietario.getId() == null) {
      // Le asignamos ID
      propietario.setId(this.generadorId.sigueinte());
    } else {
      this.delete(propietario);
    }
    this.propietarios.add(propietario);
    return propietario;
  }

  @Override
  public void delete(Propietario propietario) {
    if (propietario.getId() == null) return;
    this.propietarios.removeIf(
        p -> Objects.equals(p.getId(), propietario.getId())
    );
  }
}
