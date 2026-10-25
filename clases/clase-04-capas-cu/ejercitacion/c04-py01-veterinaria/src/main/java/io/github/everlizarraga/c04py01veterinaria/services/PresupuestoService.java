package io.github.everlizarraga.c04py01veterinaria.services;

import io.github.everlizarraga.c04py01veterinaria.dtos.PresupuestoRequest;
import io.github.everlizarraga.c04py01veterinaria.dtos.PresupuestoResponse;

public interface PresupuestoService {
  PresupuestoResponse cotizar(PresupuestoRequest request);
}
