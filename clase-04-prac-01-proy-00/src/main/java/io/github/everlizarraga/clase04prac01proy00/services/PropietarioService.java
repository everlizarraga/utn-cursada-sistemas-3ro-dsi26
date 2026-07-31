package io.github.everlizarraga.clase04prac01proy00.services;

import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioResponse;

import java.util.List;

public interface PropietarioService {
  List<PropietarioResponse> findAll();
  PropietarioResponse findById(Long id);
  PropietarioResponse create(PropietarioCreateRequest request);
}
