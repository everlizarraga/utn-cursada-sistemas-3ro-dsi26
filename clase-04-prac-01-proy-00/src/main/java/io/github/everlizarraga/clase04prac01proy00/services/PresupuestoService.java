package io.github.everlizarraga.clase04prac01proy00.services;

import io.github.everlizarraga.clase04prac01proy00.dtos.PresupuestoRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.PresupuestoResponse;

public interface PresupuestoService {
  PresupuestoResponse cotizar(PresupuestoRequest request);
}
