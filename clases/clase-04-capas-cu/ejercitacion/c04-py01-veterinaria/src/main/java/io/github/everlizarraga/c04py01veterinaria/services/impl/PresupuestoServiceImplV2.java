package io.github.everlizarraga.c04py01veterinaria.services.impl;

import io.github.everlizarraga.c04py01veterinaria.dtos.PresupuestoRequest;
import io.github.everlizarraga.c04py01veterinaria.dtos.PresupuestoResponse;
import io.github.everlizarraga.c04py01veterinaria.services.PresupuestoService;
import org.springframework.stereotype.Service;

//@Service
public class PresupuestoServiceImplV2 implements PresupuestoService {

  public static double recargo = 1.1;

  @Override
  public PresupuestoResponse cotizar(PresupuestoRequest request) {
    if(request == null) {
      throw new IllegalArgumentException("El body del presupuesto es obligatorio");
    }
    if(request.servicio() == null ||
    request.servicio().isBlank()) {
      throw new IllegalArgumentException("El servicio es obligatorio");
    }
    if(request.cantidad() <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
    }
    if(request.precioUnitario() <= 0) {
      throw new IllegalArgumentException("El precio unitario debe ser mayor a cero");
    }

    double total = request.cantidad() * request.precioUnitario();
    double totalConRecargo = total * recargo;

    return new PresupuestoResponse(
        request.servicio(),
        request.cantidad(),
        request.precioUnitario(),
        totalConRecargo
    );
  }
}
