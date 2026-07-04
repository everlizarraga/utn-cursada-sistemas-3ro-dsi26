package io.github.everlizarraga.clase02prac01proy00.etapas;

import io.github.everlizarraga.clase02prac01proy00.catalogo.CatalogoPaises;
import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

import java.util.stream.Collectors;

public class Etapa05 implements EjecutarEtapa {

  @Override
  public void run() {
    System.out.println(":::::: Ejecutando etapa 05 ::::::");

    CatalogoPaises catalogo = new CatalogoPaises();

    System.out.println("-------------------------------");
    System.out.println("Poblacion Total 1: " + catalogo.poblacionTotal());
    System.out.println("Poblacion Total 2: " + catalogo.poblacionTotal2());
    System.out.println("Poblacion Total 3: " + catalogo.poblacionTotal3());

    System.out.println("-------------------------------");
    System.out.println("Nombre Concatenados: " + catalogo.nombresConcatenados());

    System.out.println("-------------------------------");
    System.out.println("Contar por región:");
    catalogo.contarPorRegion()
        .forEach((key, value) ->
            System.out.println(" [" + key + "]: " + value));
    catalogo.contarPorRegion()
        .entrySet()
        .forEach(e ->
            System.out.println(" <" + e.getKey() + "> " + e.getValue()));

    System.out.println("-------------------------------");
    System.out.println("Países ordenados alfabéticamente:");
    System.out.println(" > " +
        catalogo.paisesOrdenadosAlfabeticamente().stream()
            .map(Pais::getNombre)
            .collect(Collectors.joining(", ")));
    System.out.println(" > " +
        catalogo.paisesOrdenadosAlfabeticamenteDesc().stream()
            .map(Pais::getNombre)
            .collect(Collectors.joining(", ")));
    System.out.println(" > " +
        catalogo.paisesOrdenadosAlfabeticamenteDesc2().stream()
            .map(Pais::getNombre)
            .collect(Collectors.joining(", ")));

    System.out.println("-------------------------------");
    System.out.println("Capitales de todos los países:");
    System.out.println(" > " + catalogo.capitalesDeTodosLosPaiese());

    System.out.println("-------------------------------");
    System.out.println("Paises con población mayor a 45000000L");
    System.out.println(" > " + catalogo.paisesConPoblacionMayorA(45000000L)
        .stream()
        .map(p -> "[" + p.getNombre() + "," + p.getPoblacion() + "]")
        .collect(Collectors.joining(" | ")));

    System.out.println("-------------------------------");
    System.out.println("Promedio de población:");
    System.out.println(" > " + catalogo.promedioPoblacion());

    System.out.println("-------------------------------");
    System.out.println("Hay país de asia?");
    System.out.println(" > " + catalogo.hayPaisDe("asia"));

    System.out.println("-------------------------------");
    System.out.println("Todos los países mayor a 1M");
    System.out.println(" > " + catalogo.todosConPoblacionMayorA(1000000L));

    System.out.println("-------------------------------");
    System.out.println("Agrupar por region:");
    catalogo.agruparPorRegion().entrySet().forEach(e ->
        System.out.println(" <" + e.getKey() + "> " +
            e.getValue().stream()
                .map(Pais::getNombre)
                .collect(Collectors.joining(" | "))));

  }
}
