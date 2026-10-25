package io.github.everlizarraga.c04py01veterinaria.services;

import io.github.everlizarraga.c04py01veterinaria.dtos.mascota.MascotaCreateRequest;
import io.github.everlizarraga.c04py01veterinaria.dtos.mascota.MascotaResponse;
import io.github.everlizarraga.c04py01veterinaria.dtos.mascota.MascotaUpdateRequest;

import java.util.List;

public interface MascotaService {
  List<MascotaResponse> findAll();
  MascotaResponse findById(Long id);
  MascotaResponse create(MascotaCreateRequest request);
  MascotaResponse update(Long id, MascotaUpdateRequest request);
  void deleteById(Long id);
}
