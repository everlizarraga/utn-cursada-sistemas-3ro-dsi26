package io.github.everlizarraga.clase04prac01proy00.repositories;

import io.github.everlizarraga.clase04prac01proy00.models.entities.Mascota;

import java.util.List;
import java.util.Optional;

public interface MascotaRepository {
  List<Mascota> findAll();
  Optional<Mascota> findById(Long id);
  Mascota save(Mascota mascota);
  void delete(Mascota mascota);
}
