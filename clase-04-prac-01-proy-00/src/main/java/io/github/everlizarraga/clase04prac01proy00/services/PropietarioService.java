package io.github.everlizarraga.clase04prac01proy00.services;

import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioResponse;
import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioUpdateRequest;

import java.util.List;

public interface PropietarioService {
  List<PropietarioResponse> findAll();
  PropietarioResponse findById(Long id);
  PropietarioResponse create(PropietarioCreateRequest request);
  PropietarioResponse update(Long id, PropietarioUpdateRequest request);
  void deleteById(Long id);
}
