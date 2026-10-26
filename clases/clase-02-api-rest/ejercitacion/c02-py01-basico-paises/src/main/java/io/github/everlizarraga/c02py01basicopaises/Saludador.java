package io.github.everlizarraga.c02py01basicopaises;

import org.springframework.stereotype.Component;

@Component
public class Saludador {
    public Saludador() {
        System.out.println(">>> Spring creó un Saludador");
    }
}
