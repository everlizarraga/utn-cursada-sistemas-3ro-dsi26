package io.github.everlizarraga.c02py01basicopaises;

import io.github.everlizarraga.c02py01basicopaises.DTOs.Pais;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Arranque implements CommandLineRunner {
    private final CatalogoDePaises catalogo;

    public Arranque(CatalogoDePaises catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public void run(String... args) {
//        this.buscadorArgentinaObject();
//        this.buscadorArgentinaString();
        this.buscadorPersonalizado();
    }

    private void buscadorArgentinaString() {
        System.out.println(">>> JSON crudo: " + this.catalogo.traerArgentinaComoTexto());
    }

    private void buscadorArgentinaObject() {
        System.out.println(">>> Llamando a la API...");
        Pais argentina = this.catalogo.buscarArgentina();
        System.out.println(">>> País traído: " + argentina.getNames().getCommon());
        System.out.println(">>> Nombre oficial: " + argentina.getNames().getOfficial());
        System.out.println(">>> Capital: " + argentina.getCapitals().get(0).getName());
        System.out.println(">>> Región: " + argentina.getRegion());
        System.out.println(">>> Subregión: " + argentina.getSubregion());
        System.out.println(">>> Población: " + argentina.getPopulation());
    }

    private void buscadorPersonalizado() {
        Pais brasil = catalogo.buscarPorNombre("brazil");
        System.out.println(">>> " + brasil.getNames().getCommon() + " - " + brasil.getCapitals().get(0).getName());

        Pais japon = catalogo.buscarPorNombre("japan");
        System.out.println(">>> " + japon.getNames().getCommon() + " - " + japon.getCapitals().get(0).getName());
    }
}
