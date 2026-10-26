package io.github.everlizarraga.c02py01basicopaises;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Configuration
public class AppConfig {

    @Bean
    public Random random() {
        Random unRandom = new Random();
        return unRandom;
    }

    @Bean
    public RestTemplate restTemplate() {
        System.out.println(">>> Se creó el RestTemplate");
        return new RestTemplate();
    }
}
