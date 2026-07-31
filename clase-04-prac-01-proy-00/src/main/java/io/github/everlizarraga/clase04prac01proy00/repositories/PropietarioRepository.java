package io.github.everlizarraga.clase04prac01proy00.repositories;

import io.github.everlizarraga.clase04prac01proy00.models.entities.Propietario;

import java.util.List;
import java.util.Optional;

public interface PropietarioRepository {

  List<Propietario> findAll();

  Optional<Propietario> findById(Long id);

  Propietario save(Propietario propietario);

  void delete(Propietario propietario);
}
// ↑ El contrato clásico (nombres en inglés = convención universal de repos).
//   Optional en el findById: la forma prolija de "buscá, y puede que no esté"
//   — sin null pelado. Su gracia completa aparece en la Parte 4.
