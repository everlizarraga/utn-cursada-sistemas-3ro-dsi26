package io.github.everlizarraga.c02py01basicopaises.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pais {
    private NombrePais names;
    private List<Capital> capitals;
    private String region;
    private String subregion;
    private Long population;
}

/*
RespuestaPaises                   → el JSON completo
    └─ data: DatosPaises          → el "data" del JSON
         └─ objects: List<Pais>   → la lista de países
                └─ Pais           → cada país
                     ├─ names: NombrePais        → objeto anidado
                     ├─ capitals: List<Capital>  → lista de objetos
                     └─ region, subregion, population
* */

/*
data → objects → [0] → names → common
 ↑       ↑        ↑      ↑        ↑
wrapper  lista   primer  objeto  el dato
         de      país    anidado
         países

* {
    "data": {
        "objects": [
            {
                "names": {
                    "common": "Argentina",
                    "official": "Argentine Republic"
                },
                "capitals": [
                    {
                        "name": "Buenos Aires"
                    }
                ],
                "region": "Americas",
                "subregion": "South America",
                "population": 46735004
            }
        ]
    }
}
* */
