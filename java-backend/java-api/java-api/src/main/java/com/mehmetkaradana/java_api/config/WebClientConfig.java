package com.mehmetkaradana.java_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

   // @Bean
   // public BCryptPasswordEncoder passwordEncoder() {
   //     return new BCryptPasswordEncoder();
 //   }
}