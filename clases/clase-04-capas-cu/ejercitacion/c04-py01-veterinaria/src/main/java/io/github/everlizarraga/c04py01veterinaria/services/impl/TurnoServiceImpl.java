package io.github.everlizarraga.c04py01veterinaria.services.impl;

import io.github.everlizarraga.c04py01veterinaria.dtos.TurnoRequest;
import io.github.everlizarraga.c04py01veterinaria.services.TurnoService;
import org.springframework.stereotype.Service;

@Service
public class TurnoServiceImpl implements TurnoService {
  @Override
  public String confirmar(TurnoRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("El body del turno es obligatorio");
    }
    if (request.mascota() == null ||
        request.mascota().isBlank()) {
      throw new IllegalArgumentException("La mascota es obligatoria");
    }

    return "Turno para " + request.mascota()
        + " el " + request.dia()
        + " (" + request.duracionMinutos() + " min)";

  }
}
