package io.github.everlizarraga.c04py01veterinaria.services;

import io.github.everlizarraga.c04py01veterinaria.dtos.propietario.PropietarioCreateRequest;
import io.github.everlizarraga.c04py01veterinaria.dtos.propietario.PropietarioResponse;
import io.github.everlizarraga.c04py01veterinaria.dtos.propietario.PropietarioUpdateRequest;

import java.util.List;

public interface PropietarioService {
  List<PropietarioResponse> findAll();
  PropietarioResponse findById(Long id);
  PropietarioResponse create(PropietarioCreateRequest request);
  PropietarioResponse update(Long id, PropietarioUpdateRequest request);
  void deleteById(Long id);
}
