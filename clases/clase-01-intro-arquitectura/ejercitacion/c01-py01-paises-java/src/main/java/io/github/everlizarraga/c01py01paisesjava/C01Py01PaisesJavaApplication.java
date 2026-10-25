package io.github.everlizarraga.c01py01paisesjava;

import io.github.everlizarraga.c01py01paisesjava.catalogo.CatalogoPaises;
import io.github.everlizarraga.c01py01paisesjava.etapas.Etapa01;
import io.github.everlizarraga.c01py01paisesjava.etapas.Etapa02;
import io.github.everlizarraga.c01py01paisesjava.etapas.Etapa03;
import io.github.everlizarraga.c01py01paisesjava.etapas.Etapa04;
import io.github.everlizarraga.c01py01paisesjava.etapas.Etapa05;
import io.github.everlizarraga.c01py01paisesjava.etapas.Etapa06;
import io.github.everlizarraga.c01py01paisesjava.etapas.Etapa08;
import io.github.everlizarraga.c01py01paisesjava.etapas.Etapa09;
import io.github.everlizarraga.c01py01paisesjava.modelo.Pais;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class C01Py01PaisesJavaApplication {

  public static void main(String[] args) {
    SpringApplication.run(C01Py01PaisesJavaApplication.class, args);
    System.out.println("===========================");
    System.out.println("Proyecto 00. Andando !!!");
    System.out.println("===========================");

    //new Etapa01().run();
    //new Etapa02().run();
    //new Etapa03().run();
    //new Etapa04().run();
    //new Etapa05().run();
    //new Etapa06().run();
    //new Etapa08().run();
    new Etapa09().run();

  }


}
