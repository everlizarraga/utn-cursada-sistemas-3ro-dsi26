package io.github.everlizarraga.clase04prac01proy00.services;

import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaResponse;

import java.util.List;

public interface MascotaService {
  List<MascotaResponse> findall();
  MascotaResponse findById(Long id);
  MascotaResponse create(MascotaCreateRequest request);
}
