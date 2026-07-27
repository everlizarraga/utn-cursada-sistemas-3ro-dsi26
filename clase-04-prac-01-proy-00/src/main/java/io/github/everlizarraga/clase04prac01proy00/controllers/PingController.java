package io.github.everlizarraga.clase04prac01proy00.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //@Component
@RequestMapping("/veterinaria")
public class PingController {

  private static int contadorSaludo = 0;

  @GetMapping({"", "/"})
  public String saludo() {
    contadorSaludo += 1;
    System.out.println(">>>  Atendio: " + contadorSaludo);
    return "Hola Spring-boot !!!";
  }

  @GetMapping("/ping")
  public String ping() {
    return "pong";
  }

  @GetMapping("/info")
  public String info() {
    return "Veterinaria Firulais";
  }
}
