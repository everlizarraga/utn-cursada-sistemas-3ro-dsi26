package io.github.everlizarraga.clase02prac01proy00.etapas;

import io.github.everlizarraga.clase02prac01proy00.catalogo.CatalogoPaises;
import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

import java.util.stream.Collectors;

public class Etapa06 implements EjecutarEtapa {

  @Override
  public void run() {
    System.out.println(":::::: Ejecutando etapa 06 ::::::");

    CatalogoPaises catalogo = new CatalogoPaises();

    System.out.println("\n=== Países que usan EUR ===");
    catalogo.buscarPorMoneda("EUR")
        .forEach(p -> System.out.println("  - " + p.getNombre()));

    System.out.println("\n=== Países que usan ARS ===");
    catalogo.buscarPorMoneda("ARS")
        .forEach(p -> System.out.println("  - " + p.getNombre()));

    System.out.println("\n=== Idiomas únicos en el catálogo ===");
    catalogo.todosLosIdiomas()
        .forEach(System.out::println);

    System.out.println("\n=== Países por moneda ===");
    catalogo.paisesAgrupadosPorMonedaPrincipal()
        .forEach((moneda, lista) -> {
          System.out.println(moneda + ":");
          lista.forEach(p -> System.out.println("  - " + p.getNombre()));
        });

    System.out.println("-------------------------------");
    System.out.println("Paises que hablan Spanish:");
    System.out.println(" > " + catalogo.paisesQueHablan("spa").stream()
        .map(Pais::getNombre)
        .collect(Collectors.joining(" | ")));

    System.out.println("-------------------------------");
    System.out.println("Cantidad de idiomas unicos:");
    System.out.println(" > " + catalogo.cantidadIdiomasUnicos());

    System.out.println("-------------------------------");
    System.out.println("Lista de simbolos de monedas");
    System.out.println(" > " + catalogo.simbolosDeMoneda());
    System.out.println(" > " + catalogo.simbolosDeMoneda2());

    System.out.println("-------------------------------");
    System.out.println("Pais con mas idiomas:");
    System.out.println(" > " + catalogo.paisConMasIdiomas());

    System.out.println("-------------------------------");
    System.out.println("Hay algún país plurilingüe?");
    System.out.println(" > " + catalogo.hayPaisPlurilingue());

    System.out.println("-------------------------------");
    System.out.println("Paises por idioma:");
    catalogo.paisesPorIdioma().forEach((key, value) ->
        System.out.println(" > " + key + ": " + value.stream()
            .map(Pais::getNombre)
            .collect(Collectors.joining(" | "))));
  }
}
