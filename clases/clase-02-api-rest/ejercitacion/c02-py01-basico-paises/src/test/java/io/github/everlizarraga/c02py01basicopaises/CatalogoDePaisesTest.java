package io.github.everlizarraga.c02py01basicopaises;

import io.github.everlizarraga.c02py01basicopaises.DTOs.Pais;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CatalogoDePaisesTest {

    @Autowired // Le pide a Spring que inyecte el Bean
    private CatalogoDePaises catalogo;

    @Test
    void _01elCatalogoSeInyectaCorrectamente() {
        assertThat(catalogo).isNotNull();
    }

    @Test
    void _02buscarArgentinaTraeLosDatosCorrectos() {
        Pais argentina = catalogo.buscarPorNombre("argentina");

        assertThat(argentina).isNotNull();

        // El nombre está en names.common / names.official (estructura v5)
        assertThat(argentina.getNames().getCommon()).isEqualTo("Argentina");
        assertThat(argentina.getNames().getOfficial()).isEqualTo("Argentine Republic");

        assertThat(argentina.getRegion()).isEqualTo("Americas");
        assertThat(argentina.getSubregion()).isEqualTo("South America");

        // La capital es una lista de objetos Capital: sacamos el name del primero
        assertThat(argentina.getCapitals().get(0).getName()).isEqualTo("Buenos Aires");

        // La población: verificamos que sea positiva, no el valor exacto (ver abajo)
        assertThat(argentina.getPopulation()).isPositive();
    }
}
