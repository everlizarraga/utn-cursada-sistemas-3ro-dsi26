package io.github.everlizarraga.clase04prac01proy00.controllers;

import io.github.everlizarraga.clase04prac01proy00.dtos.InfoVeterinaria;
import io.github.everlizarraga.clase04prac01proy00.dtos.PresupuestoRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.PresupuestoResponse;
import io.github.everlizarraga.clase04prac01proy00.dtos.TurnoRequest;
import io.github.everlizarraga.clase04prac01proy00.dtos.TurnoRequestClass;
import io.github.everlizarraga.clase04prac01proy00.services.PresupuestoService;
import io.github.everlizarraga.clase04prac01proy00.services.TurnoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController //@Component
@RequestMapping("/veterinaria")
public class PingController {

  private static int contadorSaludo = 0;

  private final PresupuestoService presupuestoService;
  private final TurnoService turnoService;

  public PingController(
      PresupuestoService presupuestoService,
      TurnoService turnoService
  ) {
    this.presupuestoService = presupuestoService;
    this.turnoService = turnoService;
  }

  @GetMapping({"", "/"})
  public String saludo() {
    contadorSaludo += 1;
    System.out.println(">>>  Atendio: " + contadorSaludo);
    return "Hola Spring-boot !!!";
  }

  // ETAPA 01 ::::::::::::::::::::::::::::::::::
  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }

  @GetMapping("/info")
  public String info() {
    return "Veterinaria Firulais";
  }

  // ETAPA 02 ::::::::::::::::::::::::::::::::::
  @GetMapping("/eco/{palabra}")
  public String eco(@PathVariable String palabra) {
    return "Dijiste: " + palabra;
  }

  @GetMapping("/doble/{n}")
  public String doble(@PathVariable("n") Long numero) {
    return "El doble de " + numero + " es " + (numero * 2);
  }

  @GetMapping("/eco/especial")
  public String ecoEspecial() {
    return "¡Ruta VIP!";
  }

  @GetMapping("/tabla/{numero}/{multiplicador}")
  public String multiplicar(@PathVariable Long numero, @PathVariable Long multiplicador) {
    return "MULTIPLICAR: " + numero + " x " + multiplicador + " = " + (numero * multiplicador);
  }

  // ETAPA 02-B ::::::::::::::::::::::::::::::::::
  @GetMapping("/buscar")
  public String buscar(@RequestParam String texto) {
    return "Buscando: " + texto;
  }

  @GetMapping("/buscar2")
  public String buscar2(@RequestParam("texto") String t, @RequestParam Long limit) {
    return "Buscando: " + t + " - Limit: " + limit;
  }

  @GetMapping("/buscar3") // ?texto=...&limit=...&algo=...
  public String buscar3(
      @RequestParam(name = "texto") String t,
      @RequestParam(name = "limit", defaultValue = "3") int limite, // defaultValue -> Siempre se necesita un valor
      @RequestParam(name = "algo", required = false) String x // required=false -> Puedo omitirlo (x=null)
  ) {
    //if(x == null) x = "Ever";
    return "Buscando: " + t + " (máximo " + limite + " resultados) - algo:" + x;
  }

  @GetMapping("/agenda")
  public String agenda(
      @RequestParam String dia,
      @RequestParam(name = "especie", defaultValue = "todas") String esp,
      @RequestParam(defaultValue = "5") Long limite
  ) {
    return "[Agenda] <dia:" + dia + "|especie:" + esp + "|limite:" + limite + ">";
  }

  // ETAPA 03 ::::::::::::::::::::::::::::::::::
  @GetMapping("/info-completa")
  public InfoVeterinaria infoCompleta() {
    return new InfoVeterinaria(
        "Veterinaria Firulais",
        "Av. Corrientes 742",
        true
    );
  }

  @PostMapping("/turnos")
  @ResponseStatus(HttpStatus.CREATED)
  public String solicitarTurno(@RequestBody TurnoRequest request) {
    return this.turnoService.confirmar(request);
  }

  @PostMapping("/turnos-v2")
  @ResponseStatus(HttpStatus.CREATED)
  public String solicitarTurnoV2(@RequestBody TurnoRequestClass request) {
    return "Turno para " + request.getMascota()
        + " el " + request.getDia()
        + " (" + request.getDuracionMinutos() + " min)";
  }

  @PostMapping("/presupuestos")
  @ResponseStatus(HttpStatus.CREATED)
  public PresupuestoResponse presupuesto(@RequestBody PresupuestoRequest request) {
    return this.presupuestoService.cotizar(request);
  }


}
