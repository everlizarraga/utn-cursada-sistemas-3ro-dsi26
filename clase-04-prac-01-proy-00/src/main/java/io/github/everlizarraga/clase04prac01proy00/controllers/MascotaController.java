package io.github.everlizarraga.clase04prac01proy00.controllers;

import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaCreateRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.mascota.MascotaResponse;
import io.github.everlizarraga.clase04prac01proy00.services.MascotaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("veterinaria/mascotas")
public class MascotaController {

  private final MascotaService mascotaService;

  public MascotaController(MascotaService mascotaService) {
    this.mascotaService = mascotaService;
  }

  @GetMapping({"", "/"})
  public List<MascotaResponse> getAll() {
    return this.mascotaService.findall();
  }

  @GetMapping("/{id}")
  public MascotaResponse getById(@PathVariable(name = "id") Long mascotaId) {
    return this.mascotaService.findById(mascotaId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MascotaResponse create(@RequestBody MascotaCreateRequest request) {
    return this.mascotaService.create(request);
  }

}
