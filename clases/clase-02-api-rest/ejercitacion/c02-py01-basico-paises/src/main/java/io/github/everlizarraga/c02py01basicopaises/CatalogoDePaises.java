package io.github.everlizarraga.c02py01basicopaises;

import io.github.everlizarraga.c02py01basicopaises.DTOs.Pais;
import io.github.everlizarraga.c02py01basicopaises.DTOs.RespuestaPaises;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Component
public class CatalogoDePaises {

    private final RestTemplate restTemplate;
    private final PaisesProperties properties;

    public CatalogoDePaises(RestTemplate restTemplate, PaisesProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        String key = properties.getApiKey();                    // el chequeo de la Parte 4
        System.out.println(">>> API key cargada: " +
                (key == null || key.isBlank()
                        ? "NO (vacía)"
                        : key.substring(0, Math.min(8, key.length())) + "... (" + key.length() + " caracteres)"));
    }

    public String traerArgentinaComoTexto() {
        // https://api.restcountries.com/countries/v5?q=argentina
        String url = properties.getUrl() + "?q=argentina";

        HttpHeaders headers = new HttpHeaders();
        // → "Authorization: Bearer <token>"
        headers.setBearerAuth(properties.getApiKey());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            System.out.println(">>> La API contestó: " + e.getStatusCode());          // 401 UNAUTHORIZED
            System.out.println(">>> Body del error: " + e.getResponseBodyAsString()); // el mensaje de la API
            throw e;    // la volvemos a tirar: acá solo la miramos
        }
    }

    public Pais buscarArgentina() {
        String url = properties.getUrl() + "?q=argentina";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<RespuestaPaises> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                RespuestaPaises.class          // ← "convertí la respuesta en un RespuestaPaises"
        );

        return response.getBody()        // RespuestaPaises
                .getData()              // DatosPaises
                .getObjects()           // List<Pais>
                .get(0);                // el primer Pais
    }

    public Pais buscarPorNombre(String nombre) {
        String url = properties.getUrl() + "?q=" + nombre;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<RespuestaPaises> response = restTemplate.exchange(
                url, HttpMethod.GET, request, RespuestaPaises.class
        );

        return response.getBody().getData().getObjects().get(0);


    }

}
