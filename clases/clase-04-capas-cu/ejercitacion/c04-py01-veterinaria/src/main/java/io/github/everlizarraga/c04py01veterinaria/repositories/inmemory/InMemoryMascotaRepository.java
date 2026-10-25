package io.github.everlizarraga.c04py01veterinaria.repositories.inmemory;

import io.github.everlizarraga.c04py01veterinaria.models.entities.Mascota;
import io.github.everlizarraga.c04py01veterinaria.repositories.MascotaRepository;
import io.github.everlizarraga.c04py01veterinaria.utils.GeneradorIdSecuencial;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryMascotaRepository implements MascotaRepository {

  private final List<Mascota> mascotas;
  private final GeneradorIdSecuencial generadorId;

  public InMemoryMascotaRepository() {
    this.mascotas = new ArrayList<>();
    this.generadorId = new GeneradorIdSecuencial();
  }

  @Override
  public List<Mascota> findAll() {
    return new ArrayList<>(this.mascotas);
  }

  @Override
  public Optional<Mascota> findById(Long id) {
    return this.mascotas.stream()
        .filter(m -> Objects.equals(m.getId(), id))
        .findFirst();
  }

  @Override
  public Mascota save(Mascota mascota) {
    if(mascota.getId() == null) {
      mascota.setId(this.generadorId.sigueinte());
    } else {
      this.delete(mascota);
    }
    this.mascotas.add(mascota);
    return mascota;
  }

  @Override
  public void delete(Mascota mascota) {
    if(mascota.getId() == null) return;
    this.mascotas.removeIf(
        p -> Objects.equals(p.getId(), mascota.getId())
    );
  }
}
