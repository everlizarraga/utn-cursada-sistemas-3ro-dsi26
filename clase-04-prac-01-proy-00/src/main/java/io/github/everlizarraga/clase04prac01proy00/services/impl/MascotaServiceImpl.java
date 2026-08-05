package io.github.everlizarraga.clase04prac01proy00.services.impl;

import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaResponse;
import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaUpdateRequest;
import io.github.everlizarraga.clase04prac01proy00.exceptions.BusinessException;
import io.github.everlizarraga.clase04prac01proy00.exceptions.ResourceNotFoundException;
import io.github.everlizarraga.clase04prac01proy00.models.entities.Mascota;
import io.github.everlizarraga.clase04prac01proy00.models.entities.Propietario;
import io.github.everlizarraga.clase04prac01proy00.repositories.MascotaRepository;
import io.github.everlizarraga.clase04prac01proy00.repositories.PropietarioRepository;
import io.github.everlizarraga.clase04prac01proy00.services.MascotaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MascotaServiceImpl implements MascotaService {

  private final MascotaRepository mascotaRepository;
  private final PropietarioRepository propietarioRepository;

  public MascotaServiceImpl(
      MascotaRepository mascotaRepository,
      PropietarioRepository propietarioRepository
  ) {
    this.mascotaRepository = mascotaRepository;
    this.propietarioRepository = propietarioRepository;
  }

  @Override
  public List<MascotaResponse> findAll() {
    return this.mascotaRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
    //.collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public MascotaResponse findById(Long id) {
    return this.toResponse(this.getMascotaOrThrow(id));
  }

  @Override
  public MascotaResponse create(MascotaCreateRequest request) {
    // ----- FASE 1: VALIDAR (puede fallar → 400; nada mutó todavía) -----
    if (request == null || request.nombre() == null || request.nombre().isBlank()) {
      throw new BusinessException("El nombre es obligatorio");
    }
    if (request.especie() == null || request.especie().isBlank()) {
      throw new BusinessException("La especie es requerida");
    }
    if (request.propietarioId() == null) {
      throw new BusinessException("El propietarioId es obligatorio");
    }

    // ----- FASE 2: RESOLVER (puede fallar → 404; nada mutó todavía) -----
    Propietario propietario = this.getPropietarioOrThrow(request.propietarioId());

    // ----- FASE 3: INTENTAR lo que puede fallar por reglas de negocio -----
    Mascota mascota = new Mascota(null, request.nombre().trim(), request.especie());
    // (instanciar no es mutar el sistema: este objeto todavía no existe
    //  para nadie — ni repo, ni dueño. Si algo falla ahora, muere solo.)
    try {
      propietario.agregarMascota(mascota);
      // ⚠️ TU BUG 1 ESTABA ACÁ: vos hacías save(mascota) ANTES de esta línea.
      //    Si la regla de las 5 explotaba, el 400 salía bien… pero la mascota
      //    YA estaba guardada en el repo: fantasma sin dueño, y su GET por id
      //    reventaba en el findPropietarioOwner. La asociación (lo que puede
      //    fallar) va ANTES de la persistencia — y no necesita el id.
    } catch (IllegalArgumentException ex) {
      throw new BusinessException(ex.getMessage());   // la traducción de la Etapa 6
    }

    // ----- FASE 4/5: de acá en adelante nada puede fallar → persistir -----
    mascota = this.mascotaRepository.save(mascota);   // acá recién nace el id
    this.propietarioRepository.save(propietario);
    return this.toResponse(mascota, propietario);
  }

  @Override
  public MascotaResponse update(Long id, MascotaUpdateRequest request) {
    // ----- FASE 1: VALIDAR -----
    if (request == null) {
      throw new BusinessException("El body de la request es requerido");
    }
    if (request.propietarioId() == null) {
      throw new BusinessException("El propietarioId es obligatorio");
    }
    if (request.nombre() == null || request.nombre().isBlank()) {
      throw new BusinessException("El nombre es obligatorio");
    }
    if (request.especie() == null || request.especie().isBlank()) {
      throw new BusinessException("La especie es obligatoria");
      // ↑ menor corregido: isBlank también acá (create y update ahora exigen
      //   lo mismo — la coherencia entre verbos es parte del contrato).
    }

    // ----- FASE 2: RESOLVER TODO lo que se nombra (puede fallar → 404) -----
    Mascota mascota = this.getMascotaOrThrow(id);
    Propietario propietarioActual = this.findPropietarioOwner(mascota);

    boolean esMudanza = !Objects.equals(propietarioActual.getId(), request.propietarioId());
    Propietario propietarioNuevo = esMudanza
        ? this.getPropietarioOrThrow(request.propietarioId())   // puede dar 404 — y está
        : propietarioActual;                                    // bien que sea ACÁ

    // ----- FASE 3: INTENTAR lo que puede fallar (la regla de las 5) -----
    if (esMudanza) {
      try {
        propietarioNuevo.agregarMascota(mascota);
      } catch (IllegalArgumentException ex) {
        throw new BusinessException(ex.getMessage());
      }
    }

    // ----- FASE 4: MUTAR (de acá en adelante nada puede fallar) -----
    if (esMudanza) {
      propietarioActual.eliminarMascota(mascota);     // ahora sí, sin riesgo
    }
    mascota.setNombre(request.nombre().trim());        // menor: trim, como el create
    mascota.setEspecie(request.especie());

    // ----- FASE 5: PERSISTIR + RESPONDER -----
    if (esMudanza) {
      this.propietarioRepository.save(propietarioActual);
      this.propietarioRepository.save(propietarioNuevo);
    }
    this.mascotaRepository.save(mascota);
    return this.toResponse(mascota, propietarioNuevo);
  }

  @Override
  public void deleteById(Long id) {
    // FASE 2 (no hay request que validar): resolver — puede fallar → 404
    Mascota mascota = this.getMascotaOrThrow(id);
    Propietario propietario = this.findPropietarioOwner(mascota);
    // FASE 4/5: mutar + persistir (nada puede fallar ya) — tu orden acá
    // siempre estuvo bien.
    propietario.eliminarMascota(mascota);
    this.propietarioRepository.save(propietario);
    // ↑ menor: este save es técnicamente redundante HOY (instancia viva),
    //   pero unifica el criterio con el update — y sobrevive mejor al día
    //   en que el repo sea una base de datos real.
    this.mascotaRepository.delete(mascota);
  }

  // ----- HELPERS ------

  private Propietario getPropietarioOrThrow(Long id) {
    return this.propietarioRepository.findById(id).orElseThrow(
        () -> new ResourceNotFoundException("No se encontró propietario con id " + id)
    );
  }

  private Mascota getMascotaOrThrow(Long id) {
    return this.mascotaRepository.findById(id).orElseThrow(
        () -> new ResourceNotFoundException("No se encontró mascota con id " + id)
    );
  }

  private MascotaResponse toResponse(Mascota mascota) {
    Propietario propietario = this.findPropietarioOwner(mascota);
    return this.toResponse(mascota, propietario);
  }

  private MascotaResponse toResponse(Mascota mascota, Propietario propietario) {
    return new MascotaResponse(
        mascota.getId(),
        propietario.getId(),
        mascota.getNombre(),
        mascota.getEspecie()
    );
  }

  private Propietario findPropietarioOwner(Mascota mascota) {
    return this.propietarioRepository.findAll().stream()
        .filter(p -> p.getMascotas().contains(mascota))
        .findFirst()
        .orElseThrow(() ->
            new ResourceNotFoundException("No se encontró el propietario para la mascota " + mascota.getId())
        );
  }

}
