package io.github.everlizarraga.clase04prac01proy00.services;

import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaResponse;
import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaUpdateRequest;

import java.util.List;

public interface MascotaService {
  List<MascotaResponse> findAll();
  MascotaResponse findById(Long id);
  MascotaResponse create(MascotaCreateRequest request);
  MascotaResponse update(Long id, MascotaUpdateRequest request);
  void deleteById(Long id);
}
