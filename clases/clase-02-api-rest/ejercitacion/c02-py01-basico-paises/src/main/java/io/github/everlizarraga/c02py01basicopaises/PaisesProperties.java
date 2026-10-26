package io.github.everlizarraga.c02py01basicopaises;

import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

// INMUTABLE /////////////

//@ConfigurationProperties(prefix = "paises")          // sin @Component
//public record PaisesProperties(
//        String url,
//        @DefaultValue("Catálogo sin nombre") String nombreCatalogo
//) { }

@ConfigurationProperties(prefix = "paises")
@Value //Lombok: todos los campos private final, getters, y un constructor con todos los campos
public class PaisesProperties {
    String url;
    String nombreCatalogo;
}

// MUTABLE ////////////////

//@Data
//@ConfigurationProperties(prefix = "paises")
//public class PaisesProperties {
//
//    private String url;
//    private String nombreCatalogo;
//
//}
