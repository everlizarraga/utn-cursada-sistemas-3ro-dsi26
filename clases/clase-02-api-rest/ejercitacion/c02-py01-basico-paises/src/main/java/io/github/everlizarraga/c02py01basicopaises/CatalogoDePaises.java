package io.github.everlizarraga.c02py01basicopaises;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Component
public class CatalogoDePaises {

    private final RestTemplate restTemplate;
    private final PaisesProperties properties;

    public CatalogoDePaises(RestTemplate restTemplate, PaisesProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public String traerArgentinaComoTexto() {
        String url = properties.getUrl() + "/name/argentina";
        return restTemplate.getForObject(url, String.class);
    }
}
