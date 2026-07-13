package io.github.everlizarraga.clase02prac01proy00.catalogo;

import io.github.everlizarraga.clase02prac01proy00.io.CargadorDePaisesDesdeCSV;
import io.github.everlizarraga.clase02prac01proy00.modelo.DetalleMoneda;
import io.github.everlizarraga.clase02prac01proy00.modelo.Pais;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CatalogoPaises {

  private List<Pais> paises;

  public CatalogoPaises() {
    this.paises = new ArrayList<>(); // Lista mutable
    //this.paises = List.of(...); // Lista vacia inmutable
    //this.paises = Arrays.asList(...) // Lista de tamaño fijo

    this.paises.add(new Pais(
        "Argentina",
        "Buenos Aires",
        "Americas",
        45000000L,
        Map.of("ARS", new DetalleMoneda("Argentine peso", "$")),
        Map.of("spa", "Spanish")
    ));

    this.paises.add(new Pais(
        "Brasil",
        "Brasilia",
        "Americas",
        210000000L,
        Map.of("BRL", new DetalleMoneda("Brazilian real", "R$")),
        Map.of("por", "Portuguese")
    ));

    this.paises.add(new Pais(
        "Chile",
        "Santiago",
        "Americas",
        19000000L,
        Map.of("CLP", new DetalleMoneda("Chilean peso", "$")),
        Map.of("spa", "Spanish")
    ));

    this.paises.add(new Pais(
        "España",
        "Madrid",
        "Europe",
        47000000L,
        Map.of("EUR", new DetalleMoneda("Euro", "€")),
        Map.of("spa", "Spanish")
    ));

    this.paises.add(new Pais(
        "Francia",
        "París",
        "Europe",
        67000000L,
        Map.of("EUR", new DetalleMoneda("Euro", "€")),
        Map.of("fra", "French", "spa", "Spanish")
    ));

    this.paises.add(new Pais(
        "Japón",
        "Tokio",
        "Asia",
        125000000L,
        Map.of("JPY", new DetalleMoneda("Japanese yen", "¥")),
        Map.of("jpn", "Japanese")
    ));
  }

  // Constructor nuevo — carga desde CSV
  public CatalogoPaises(Path archivoCSV) throws IOException {
    CargadorDePaisesDesdeCSV cargador = new CargadorDePaisesDesdeCSV();
    this.paises = new ArrayList<>(cargador.cargarDesde(archivoCSV));
  }

  public List<Pais> getTodos() {
    //return this.paises;
    //return List.copyOf(this.paises);   // Copia inmutable
    return new ArrayList<>(this.paises); // Copia
  }

  public int cantidad() {
    return this.paises.size();
  }

  public Optional<Pais> buscarPorNombre(String nombre) {
    return this.paises.stream()
        .filter(p -> nombre.equals(p.getNombre()))
        .findFirst();
  }

  public Pais buscarPorNombreObligatorio(String nombre) {
    /*if (nombre == null) {
      throw new IllegalArgumentException("El nombre no puede ser null");
    }*/
    Objects.requireNonNull(nombre, "El nombre no puede ser null");
    if (nombre.isBlank()) {
      throw new IllegalArgumentException("El nombre no puede estar vacío");
    }
    return this.buscarPorNombre(nombre)
        //.orElseThrow(() -> new RuntimeException("País no encontrado: " + nombre));
        .orElseThrow(() -> new PaisNoEncontradoException(nombre));
  }

  public Optional<Pais> buscarPorCapital(String capital) {
    return this.paises.stream()
        .filter(p -> p.getCapital().equals(capital))
        .findFirst();
  }

  public List<Pais> buscarPorRegion(String region) {
    return this.paises.stream()
        .filter(p -> region.equalsIgnoreCase(p.getRegion()))
        .toList();
  }

  /*public List<Pais> buscarPorRegion(String region) {
    var paisesPorRegion = this.paises.stream()
        .filter(p -> p.getRegion().equals(region))
        //.collect(Collectors.toList());
        .collect(Collectors.toCollection(ArrayList::new));
    if(paisesPorRegion.isEmpty()) { // paisesPorRegion.size() == 0
      List<String> regionesDisponibles = this.contarPorRegion()
          .keySet()
          .stream()
          .toList();
      //throw new RegionDesconocidaException(region, regionesDisponibles);
    }
    return paisesPorRegion;
  }*/

  public Optional<Pais> primerPaisPoblacionMayorA(long limite) {
    return this.paises.stream()
        .filter(p -> p.getPoblacion() > limite)
        .findFirst();
  }

  public Optional<Pais> paisMasPoblado() {
    return this.paises.stream()
        .max(Comparator.comparingLong(Pais::getPoblacion));
  }

  public long poblacionTotal() {
    return this.paises.stream()
        .mapToLong(Pais::getPoblacion)
        .sum();
  }

  public long poblacionTotal2() {
    return this.paises.stream()
        .map(Pais::getPoblacion)
        .reduce(0L, Long::sum);
  }

  public long poblacionTotal3() {
    return this.paises.stream()
        .map(Pais::getPoblacion)
        .reduce(0L, (acum, sig) -> acum + sig);
  }

  public String nombresConcatenados() {
    return this.paises.stream()
        .map(Pais::getNombre)
        .collect(Collectors.joining(", "));
  }

  public Map<String, Long> contarPorRegion() {
    return this.paises.stream()
        .collect(Collectors.groupingBy(
            Pais::getRegion,
            Collectors.counting()
        ));
  }

  public List<Pais> paisesOrdenadosAlfabeticamente() {
    return this.paises.stream()
        .sorted(Comparator.comparing(Pais::getNombre))
        .toList();
  }

  public List<Pais> paisesOrdenadosAlfabeticamenteDesc() {
    return this.paises.stream()
        .sorted(Comparator.comparing(Pais::getNombre).reversed())
        .toList();
  }

  public List<Pais> paisesOrdenadosAlfabeticamenteDesc2() {
    return this.paisesOrdenadosAlfabeticamente().stream()
        .sorted(Comparator.comparing(Pais::getNombre).reversed())
        .toList();
  }

  public List<String> capitalesDeTodosLosPaiese() {
    return this.getTodos().stream()
        .map(Pais::getCapital)
        .distinct()
        .toList();
  }

  public List<Pais> paisesConPoblacionMayorA(long limite) {
    return this.getTodos().stream()
        .filter(p -> p.getPoblacion() > limite)
        .toList();
  }

  public double promedioPoblacion() {
    return this.getTodos().stream()
        .mapToLong(Pais::getPoblacion)
        .average()
        .orElse(0.0);
  }

  public boolean hayPaisDe(String region) {
    return this.getTodos().stream()
        .anyMatch(p -> region.equalsIgnoreCase(p.getRegion()));
  }

  public boolean todosConPoblacionMayorA(long limite) {
    return this.getTodos().stream()
        .allMatch(p -> p.getPoblacion() > limite);
  }

  public Map<String, List<Pais>> agruparPorRegion() {
    return this.getTodos().stream()
        .collect(Collectors.groupingBy(Pais::getRegion));
  }

  // ==========================================
  public List<Pais> buscarPorMoneda(String codigoMoneda) {
    return this.paises.stream()
        .filter(p -> p.getMonedas().containsKey(codigoMoneda))
        .toList();
  }

  public Set<String> todosLosIdiomas() {
    return this.paises.stream()
        .flatMap(p -> p.getIdiomas().values().stream())
        .collect(Collectors.toSet());
  }

  public Map<String, List<Pais>> paisesAgrupadosPorMonedaPrincipal0() {
    return this.paises.stream()
        .collect(Collectors.groupingBy(
            p -> p.getMonedas().keySet()
                .iterator().next() // [...].get(0)
        ));
  }

  public Map<String, List<Pais>> paisesAgrupadosPorMonedaPrincipal() {
    return this.getTodos().stream()
        .collect(Collectors.groupingBy(
            p -> p.getMonedas().keySet().stream()
                .findFirst()
                .orElse("(sin moneda)")
        ));
    /*
    ARS:
      - Argentina
    BRL:
      - Brasil
    CLP:
      - Chile
    EUR:
      - España
      - Francia
    JPY:
      - Japón
    * */
  }

  public List<Pais> paisesQueHablan(String codigoIdioma) {
    return this.getTodos().stream()
        .filter(p -> p.getIdiomas().containsKey(codigoIdioma))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public int cantidadIdiomasUnicos() {
    return this.todosLosIdiomas().size();
  }

  public Set<String> simbolosDeMoneda() {
    return this.getTodos().stream()
        .flatMap(p -> p.getMonedas()
            .values()
            .stream()
            .map(DetalleMoneda::getSimbolo))
        .collect(Collectors.toSet());
  }

  public List<String> simbolosDeMoneda2() {
    return this.getTodos().stream()
        .flatMap(p -> p.getMonedas().values().stream())
        .map(DetalleMoneda::getSimbolo)
        .distinct()
        .collect(Collectors.toList());
  }

  public Optional<Pais> paisConMasIdiomas() {
    return this.getTodos().stream()
        .max(Comparator.comparing(p -> p.getIdiomas().size()));
  }

  public boolean hayPaisPlurilingue() {
    return this.getTodos().stream()
        .anyMatch(p -> p.getIdiomas().size() > 1);
  }

  public Map<String, List<Pais>> paisesPorIdioma() {
    return this.getTodos().stream()
        .flatMap(p -> p.getIdiomas()
            .keySet()
            .stream()
            .map(idioma -> Map.entry(idioma, p)))
        .collect(Collectors.groupingBy( // Cada elemento es una entry
            Map.Entry::getKey,
            Collectors.mapping( // Si o si porque tengo entries
                Map.Entry::getValue, // entry -> Pais
                Collectors.toList()
            )
        ));
  }

  /////////////////////////////////////////


}
