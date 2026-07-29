package io.github.everlizarraga.clase04prac01proy00.services.impl;

import io.github.everlizarraga.clase04prac01proy00.dtos.TurnoRequest;
import io.github.everlizarraga.clase04prac01proy00.services.TurnoService;
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
