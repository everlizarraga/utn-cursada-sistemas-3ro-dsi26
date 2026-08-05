package io.github.everlizarraga.clase04prac01proy00.controllers;

import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioResponse;
import io.github.everlizarraga.clase04prac01proy00.dtos.propietario.PropietarioUpdateRequest;
import io.github.everlizarraga.clase04prac01proy00.services.PropietarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/veterinaria/propietarios")   // ruta padre: servicio/recurso —
public class PropietarioController {           // el patrón del proyecto de la clase

  private final PropietarioService propietarioService;

  public PropietarioController(PropietarioService propietarioService) {
    this.propietarioService = propietarioService;
  }

  @GetMapping
  public List<PropietarioResponse> getAll() {
    return this.propietarioService.findAll();
  }

  @GetMapping("/{id}")
  public PropietarioResponse getById(@PathVariable Long id) {
    return this.propietarioService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PropietarioResponse create(@RequestBody PropietarioCreateRequest request) {
    return this.propietarioService.create(request);
  }

  @PutMapping("/{id}")
  public PropietarioResponse update(
      @PathVariable(name = "id") Long idPropietario,
      @RequestBody PropietarioUpdateRequest request
  ) {
    return this.propietarioService.update(idPropietario, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable(name = "id") Long propietarioId) {
    this.propietarioService.deleteById(propietarioId);
  }

}
// ↑ Cada línea de este archivo la construiste en alguna etapa. Ninguna
//   debería sorprenderte — si alguna lo hace, ahí está tu relectura.
