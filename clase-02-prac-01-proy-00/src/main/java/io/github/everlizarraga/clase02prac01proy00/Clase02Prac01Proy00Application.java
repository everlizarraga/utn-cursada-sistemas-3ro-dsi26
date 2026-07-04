package io.github.everlizarraga.clase02prac01proy00;

import io.github.everlizarraga.clase02prac01proy00.catalogo.CatalogoPaises;
import io.github.everlizarraga.clase02prac01proy00.etapas.Etapa01;
import io.github.everlizarraga.clase02prac01proy00.etapas.Etapa02;
import io.github.everlizarraga.clase02prac01proy00.etapas.Etapa03;
import io.github.everlizarraga.clase02prac01proy00.etapas.Etapa04;
import io.github.everlizarraga.clase02prac01proy00.etapas.Etapa05;
import io.github.everlizarraga.clase02prac01proy00.etapas.Etapa06;
import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class Clase02Prac01Proy00Application {

  public static void main(String[] args) {
    SpringApplication.run(Clase02Prac01Proy00Application.class, args);
    System.out.println("===========================");
    System.out.println("Proyecto 00. Andando !!!");
    System.out.println("===========================");

    //new Etapa01().run();
    //new Etapa02().run();
    //new Etapa03().run();
    //new Etapa04().run();
    //new Etapa05().run();
    new Etapa06().run();

  }


}
