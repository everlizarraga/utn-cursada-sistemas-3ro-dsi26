package io.github.everlizarraga.c04py01veterinaria.repositories;

import io.github.everlizarraga.c04py01veterinaria.models.entities.Mascota;

import java.util.List;
import java.util.Optional;

public interface MascotaRepository {
  List<Mascota> findAll();
  Optional<Mascota> findById(Long id);
  Mascota save(Mascota mascota);
  void delete(Mascota mascota);
}
