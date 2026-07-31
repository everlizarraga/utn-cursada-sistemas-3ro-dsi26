package io.github.everlizarraga.clase04prac01proy00.services.impl;

import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaResponse;
import io.github.everlizarraga.clase04prac01proy00.models.entities.Mascota;
import io.github.everlizarraga.clase04prac01proy00.models.entities.Propietario;
import io.github.everlizarraga.clase04prac01proy00.repositories.MascotaRepository;
import io.github.everlizarraga.clase04prac01proy00.repositories.PropietarioRepository;
import io.github.everlizarraga.clase04prac01proy00.services.MascotaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
  public List<MascotaResponse> findall() {
    return this.mascotaRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
        //.collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public MascotaResponse findById(Long id) {
    Mascota mascota = this.mascotaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("No se encontró la mascota " + id));
    return this.toResponse(mascota);
  }

  @Override
  public MascotaResponse create(MascotaCreateRequest request) {
    // 1. Verificar la existencia dle propietario
    Propietario propietario = this.propietarioRepository.findById(request.propietarioId())
        .orElseThrow(() -> new RuntimeException("No se encontró el propietario " + request.propietarioId()));
    // 2. Crear la mascota
    Mascota mascota = new Mascota(null, request.nombre(), request.especie());
    // 3. Persistirla
    mascota = this.mascotaRepository.save(mascota);
    propietario.agregarMascota(mascota);
    this.propietarioRepository.save(propietario);
    // 4. Devolverla
    return this.toResponse(mascota, propietario);
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
            new RuntimeException("No se encontró el propietario para la mascota " + mascota.getId())
        );
  }

}
