package io.github.everlizarraga.c04py01veterinaria.services.impl;

import io.github.everlizarraga.c04py01veterinaria.dtos.PresupuestoRequest;
import io.github.everlizarraga.c04py01veterinaria.dtos.PresupuestoResponse;
import io.github.everlizarraga.c04py01veterinaria.services.PresupuestoService;
import org.springframework.stereotype.Service;

@Service
public class PresupuestoServiceImpl implements PresupuestoService {

  @Override
  public PresupuestoResponse cotizar(PresupuestoRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("El body del presupuesto es obligatorio");
    }
    if (request.servicio() == null || request.servicio().isBlank()) {
      throw new IllegalArgumentException("El servicio es obligatorio");
    }
    if (request.cantidad() <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
    }
    if (request.precioUnitario() <= 0) {
      throw new IllegalArgumentException("El precio unitario debe ser mayor a cero");
    }
    // ↑ Las validaciones huérfanas de la Etapa 3, POR FIN con dueño: reglas
    //   del flujo, en el service. Lanzan y no atrapan — el service patea
    //   hacia arriba (recorrido P4 §4). ¿Quién atrapa? Hoy, nadie tuyo:
    //   el Experimento 3 te muestra el (mal) resultado de eso.

    double total = request.cantidad() * request.precioUnitario();
    // ↑ El cálculo mudado desde el controller: la usurpación, revertida.

    return new PresupuestoResponse(
        request.servicio(),
        request.cantidad(),
        request.precioUnitario(),
        total
    );
  }
}
