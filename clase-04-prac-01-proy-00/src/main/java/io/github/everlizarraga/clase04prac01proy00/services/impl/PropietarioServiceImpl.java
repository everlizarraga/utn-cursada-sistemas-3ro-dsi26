package io.github.everlizarraga.clase04prac01proy00.services.impl;

import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioResponse;
import io.github.everlizarraga.clase04prac01proy00.exceptions.BusinessException;
import io.github.everlizarraga.clase04prac01proy00.exceptions.ResourceNotFoundException;
import io.github.everlizarraga.clase04prac01proy00.models.entities.Propietario;
import io.github.everlizarraga.clase04prac01proy00.repositories.PropietarioRepository;
import io.github.everlizarraga.clase04prac01proy00.services.PropietarioService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropietarioServiceImpl implements PropietarioService {

  private final PropietarioRepository propietarioRepository;

  public PropietarioServiceImpl(PropietarioRepository propietarioRepository) {
    this.propietarioRepository = propietarioRepository;
  }
  // ↑ La Etapa 4, un piso más abajo: ahora es el SERVICE quien declara
  //   dependencias por constructor — y pide LA INTERFAZ del repo. El
  //   framework ya construyó el @Repository; se lo inyecta. Misma
  //   mecánica, nueva capa.

  @Override
  public List<PropietarioResponse> findAll() {
    //if (true) throw new RuntimeException("boom interno: db=10.0.0.5, password=secreta123");
    return propietarioRepository.findAll().stream()
        .map(this::toResponse)             // entidad → DTO, en masa
        .toList();                         // (this::toResponse = la
  }                                          //  referencia a método del seminario)

  @Override
  public PropietarioResponse findById(Long id) {
    return this.toResponse(getPropietarioOrThrow(id));
  }

  @Override
  public PropietarioResponse create(PropietarioCreateRequest request) {
    if (request == null || request.nombre() == null || request.nombre().isBlank()) {
      //throw new IllegalArgumentException("El nombre es obligatorio");
      throw new BusinessException("El nombre es obligatorio");
    }
    // ↑ Regla del FLUJO, en el service — Etapa 4. (Sigue saliendo como
    //   500 al mundo: deuda del traductor, la Etapa 6 se acerca.)

    Propietario propietario = new Propietario(null, request.nombre().trim(),
        request.telefono());
    // ↑ EL SERVICE INSTANCIA EL DOMINIO — la regla estrella del recorrido
    //   (P4 §4), por primera vez en tus dedos. Id en null: lo pone el repo.

    propietario = this.propietarioRepository.save(propietario);
    return this.toResponse(propietario);
  }

  // ——— piezas privadas ———

  private Propietario getPropietarioOrThrow(Long id) {
    return this.propietarioRepository.findById(id).orElseThrow(
        () -> new ResourceNotFoundException("No se encontró propietario con id " + id)
    );
    // ↑ Optional cerrando su ciclo: "dame el valor; si está vacío, lanzá".
    //   El orElseThrow() PELADO lanza NoSuchElementException — genérica,
    //   fea, y ya sabés qué le va a pasar al llegar arriba. Es A PROPÓSITO:
    //   el Experimento 1 la caza, y la Etapa 6 la jubila.
  }

  private PropietarioResponse toResponse(Propietario p) {
    return new PropietarioResponse(p.getId(), p.getNombre(), p.getTelefono(),
        p.getMascotas().size());
    // ↑ El "mapper": entidad → DTO, campo por campo. Vive acá (podría ser
    //   clase aparte — el 👀 del recorrido P6 §5).
  }
}
