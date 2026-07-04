package io.github.everlizarraga.clase02prac01proy00.etapas;

import io.github.everlizarraga.clase02prac01proy00.catalogo.CatalogoPaises;
import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

import java.util.List;
import java.util.Optional;

public class Etapa04 implements EjecutarEtapa {

  @Override
  public void run() {
    System.out.println(":::::: Ejecutando etapa 04 ::::::");

    CatalogoPaises catalogo = new CatalogoPaises();

    // ============================================
    // FORMA 1: isPresent() + get() — la más obvia, no la más elegante
    // ============================================
    System.out.println("=== Forma 1: isPresent + get ===");
    Optional<Pais> resultado1 = catalogo.buscarPorNombre("Argentina");
    if (resultado1.isPresent()) {
      Pais pais = resultado1.get();
      System.out.println("Encontré: " + pais);
    } else {
      System.out.println("No existe.");
    }


    // ============================================
    // FORMA 2: ifPresent() — más limpia cuando solo querés "hacer algo si hay"
    // ============================================
    System.out.println("\n=== Forma 2: ifPresent ===");
    catalogo.buscarPorNombre("Brasil")
        .ifPresent(p -> System.out.println("Encontré: " + p));

    // No imprime nada — Atlantis no existe
    catalogo.buscarPorNombre("Atlantis")
        .ifPresent(p -> System.out.println("Encontré: " + p));


    // ============================================
    // FORMA 3: orElse() — valor por defecto si está vacío
    // ============================================
    System.out.println("\n=== Forma 3: orElse ===");
    Pais paisOrDefault = catalogo.buscarPorNombre("Atlantis")
        .orElse(new Pais("Desconocido", "(sin capital)", "(sin región)", 0L, null, null));
    System.out.println("Resultado: " + paisOrDefault);


    // ============================================
    // FORMA 4: orElseThrow() — tirar excepción si no hay
    // ============================================
    System.out.println("\n=== Forma 4: orElseThrow ===");
    try {
      Pais p = catalogo.buscarPorNombre("Atlantis")
          .orElseThrow(() -> new RuntimeException("País no encontrado: Atlantis"));
      System.out.println(p);
    } catch (RuntimeException e) {
      System.out.println("Capturé la excepción: " + e.getMessage());
    }


    // ============================================
    // FORMA 5: map() + orElse() — lo más parecido a ?. de JS
    // ============================================
    System.out.println("\n=== Forma 5: map + orElse ===");
    String capitalArgentina = catalogo.buscarPorNombre("Argentina")
        .map(Pais::getCapital)
        .orElse("(no encontrada)");
    System.out.println("Capital de Argentina: " + capitalArgentina);

    String capitalAtlantis = catalogo.buscarPorNombre("Atlantis")
        .map(Pais::getCapital)
        .orElse("(no encontrada)");
    System.out.println("Capital de Atlantis: " + capitalAtlantis);

    // ============================================
    // Mas pruebas
    // ============================================
    System.out.println("===============================");
    System.out.println("===============================");
    this.masPruebas(catalogo);

  }

  private void masPruebas(CatalogoPaises catalogo) {
    catalogo.buscarPorCapital("Madrid")
        .ifPresent(p -> System.out.println("Capital encontrada: " + p));

    System.out.println("\n=== Países de Americas ===");
    List<Pais> americanos = catalogo.buscarPorRegion("AMERICAS");
    System.out.println("Encontré " + americanos.size() + " países:");
    for (Pais p : americanos) {
      System.out.println("  - " + p.getNombre());
    }

    System.out.println("\n=== Países de Antarctica ===");
    List<Pais> antarticos = catalogo.buscarPorRegion("Antarctica");
    System.out.println("Encontré " + antarticos.size() + " países.");

    System.out.println("--------------------");
    System.out.println("Ejercicio 02:");
    long pob = catalogo.buscarPorNombre("Brasil")
        .map(Pais::getPoblacion)
        .orElse(-1L);
    System.out.println("Poblacion si existe: BRASIL: " + pob);

    System.out.println("--------------------");
    System.out.println("Ejercicio 03:");
    catalogo.primerPaisPoblacionMayorA(100_000_000L)
        .ifPresent(p -> System.out.println("Primero con >100M: " + p.getNombre()));

    System.out.println("--------------------");
    System.out.println("Ejercicio 04:");
    System.out.println(" > " + catalogo.buscarPorRegion("Marte"));

    System.out.println("--------------------");
    System.out.println("Ejercicio 05:");
    Optional<Pais> resultado = catalogo.buscarPorNombre("Argentina")
        .flatMap(p -> catalogo.buscarPorRegion(p.getRegion())
            .stream()
            .findFirst());
    System.out.println(" > " + resultado);

  }

}
