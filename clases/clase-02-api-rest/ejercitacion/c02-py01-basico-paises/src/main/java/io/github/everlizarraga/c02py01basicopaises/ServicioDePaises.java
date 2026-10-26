package io.github.everlizarraga.c02py01basicopaises;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ServicioDePaises {

    private final CatalogoDePaises catalogo;

    public ServicioDePaises(CatalogoDePaises catalogo) {
        this.catalogo = catalogo;
        int alAzar = this.catalogo.numeroDePaisAlAzar();
        System.out.println(">>> País al azar elegido por el servicio: número " + alAzar);
    }
}
