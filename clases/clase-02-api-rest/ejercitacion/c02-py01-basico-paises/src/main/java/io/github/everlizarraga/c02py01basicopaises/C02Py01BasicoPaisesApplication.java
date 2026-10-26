package io.github.everlizarraga.c02py01basicopaises;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PaisesProperties.class)
//@ConfigurationPropertiesScan
public class C02Py01BasicoPaisesApplication {

  public static void main(String[] args) {
    SpringApplication.run(C02Py01BasicoPaisesApplication.class, args);
    System.out.println("Proyecto 01. Andando XD !!!");
  }

}
