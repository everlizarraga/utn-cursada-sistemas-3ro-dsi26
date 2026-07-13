package io.github.everlizarraga.clase02prac01proy00;

import io.github.everlizarraga.clase02prac01proy00.catalogo.CatalogoPaises;
import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class Clase02Prac01Proy00ApplicationTests {

  private CatalogoPaises catalogo;

  @BeforeEach
  void setUp() {
    // Arrange General
    catalogo = new CatalogoPaises();
  }

  @Test
  void elCatalogoTieneSeisPaises() {
    // Arrange — preparar

    // Act — ejecutar
    int cantidad = catalogo.cantidad();

    // Assert — verificar
    assertEquals(6, cantidad);
  }

  @Test
  void elCatalogoTieneSeisPaisesPro() {
    assertThat(catalogo.cantidad()).isEqualTo(6);
  }

  @Test
  void buscarArgentinaDevuelveResultadoPresente() {
    // Act
    Optional<Pais> resultado = catalogo.buscarPorNombre("Argentina");

    // Assert
    assertThat(resultado).isPresent();
    assertThat(resultado.get().getCapital()).isEqualTo("Buenos Aires");
  }

  @Test
  void buscarPaisInexistenteDevuelveOptionalVacio() {
    // Act
    Optional<Pais> resultado = catalogo.buscarPorNombre("Atlantis");

    // Assert
    assertThat(resultado).isEmpty();
  }

  @Test
  void paisesDeAmericasContienenLosTresEsperados() {
    // Act
    List<Pais> americanos = catalogo.buscarPorRegion("Americas");

    // Assert
    assertThat(americanos)
        .hasSize(3)
        .extracting(Pais::getNombre)
        .containsExactlyInAnyOrder("Argentina", "Brasil", "Chile");
  }

  @Test
  void buscarPorRegionInexistenteDevuelveListaVacia() {
    // Act
    List<Pais> resultados = catalogo.buscarPorRegion("Antartica");

    // Assert
    assertThat(resultados).isEmpty();
  }

  @Test
  void elPaisMasPobladoEsBrasil() {
    // Act
    Optional<Pais> masPoblado = catalogo.paisMasPoblado();

    // Assert
    assertThat(masPoblado)
        .isPresent()
        .map(Pais::getNombre)
        .hasValue("Brasil");
  }

  @Test
  void hayDosPaisesUsandoEuro() {
    // Act
    List<Pais> pagaEuro = catalogo.buscarPorMoneda("EUR");

    // Assert
    assertThat(pagaEuro)
        .hasSize(2)
        .extracting(Pais::getNombre)
        .containsExactlyInAnyOrder("España", "Francia");
  }

  /////////////////////////////////////

  @Test
  @DisplayName("1. Al buscar por la capital Madrid debe dar España")
  void buscarAEspaniaPorSuCapitalMadrid() {
    // Act
    Optional<Pais> espania = catalogo.buscarPorCapital("Madrid");

    // Assert
    assertThat(espania)
        .isPresent()
        .map(Pais::getNombre)
        .hasValue("España");
  }

  @ParameterizedTest
  @ValueSource(strings = {"Argentina", "Brasil", "Chile", "España"})
  @DisplayName("2. Todos los paises listados deberian existir en el catalogo")
  void todosEstosPaisesExistenEnElCatalogo(String nombre) {
    assertThat(catalogo.buscarPorNombre(nombre)).isPresent();
  }

}
