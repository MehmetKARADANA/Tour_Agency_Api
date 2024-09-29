package com.mehmetkaradana.java_api.service;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.yaml.snakeyaml.Yaml;

import com.mehmetkaradana.java_api.config.ServerConfig;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class TourismAgencyService {

    private final WebClient webClient;
    
    @Autowired
    public TourismAgencyService(WebClient.Builder webClientBuilder, ServerConfig serverConfig) {
        this.webClient = webClientBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadServersFromYaml() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new ClassPathResource("servers.yaml").getInputStream()) {
            Map<String, Object> obj = yaml.load(inputStream);
            return (List<Map<String, Object>>) obj.get("servers");
        } catch (Exception e) {
            throw new RuntimeException("Failed to read servers.yaml file", e);
        }
    }


    private long calculateResponseTime(int distanceKm) {
        return distanceKm / 10L; // 10 milliseconds for each kilometer, now 1 ms for every kilometer to understand
    }

    public Mono<String> getRoomsFromAgency(String agencyUrl) {

        List<Map<String, Object>> servers = loadServersFromYaml();

        // İlgili sunucuyu bul
        Map<String, Object> server = servers.stream()
                .filter(s -> s.get("url").equals(agencyUrl))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("not found server: " + agencyUrl));

        // Sunucu yapılandırmasından yanıt süresini hesapla
        long responseTimeMillis = calculateResponseTime((int) server.get("distance_km"));
        Duration delay = Duration.ofMillis(responseTimeMillis);
        System.out.println("Gecikme süresi: " + responseTimeMillis + " ms, Sunucu: " + server.get("url"));
        // Sunucuya istek gönder ve oda bilgilerini al
        return Mono.delay(delay)
                .then(this.webClient.get()
                        .uri(agencyUrl + "/rooms")
                        .retrieve()
                        .bodyToMono(String.class));

    }

    public Flux<String> getRoomsFromAllAgencies() {
        List<Map<String, Object>> servers = loadServersFromYaml(); // YAML dosyasından sunucu bilgilerini yükler
        ExecutorService executor = Executors.newFixedThreadPool(10); // Sabit bir thread pool kullanın

        return Flux.fromIterable(servers)
                .flatMap(server -> {
                    // Server yapılandırmasına göre response süresi hesapla
                    long responseTimeMillis = calculateResponseTime((int) server.get("distance_km"));
                    Duration delay = Duration.ofMillis(responseTimeMillis);

                    return Mono.delay(delay) // Gecikmeyi uygula
                            .then(Mono.fromCallable(() -> {
                                        try {
                                            return webClient.get()
                                                    .uri(server.get("url").toString() + "/rooms")
                                                    .retrieve()
                                                    .bodyToMono(String.class)
                                                    .block(); // Bloklayıcı çağrı
                                        } catch (Exception e) {
                                            throw new RuntimeException("Error: " + e.getMessage(), e);
                                        }
                                    }).subscribeOn(Schedulers.fromExecutor(executor)) // Executor ile çalıştır
                                    .onErrorResume(e -> {
                                        System.err.println("Error: " + e.getMessage());
                                        return Mono.empty();
                                    }));
                });
    }

/*
    public Flux<String> getRoomsFromAllAgencies() {
       List<Map<String, Object>> servers = loadServersFromYaml();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        return Flux.fromIterable(servers)
                .flatMap(server -> {
                    //Calculate response time from server configuration
                    long responseTimeMillis = calculateResponseTime((int) server.get("distance_km"));
                    Duration delay = Duration.ofMillis(responseTimeMillis);


                    return Mono.delay(delay) // apply delay
                            .then(Mono.fromCallable(() -> {
                                        try {
                                            return webClient.get()
                                                    .uri(server.get("url").toString() + "/rooms")
                                                    .retrieve()
                                                    .bodyToMono(String.class)
                                                    .block(); // Blocking
                                        } catch (Exception e) {
                                            throw new RuntimeException("Error: " + e.getMessage(), e);
                                        }
                                    }).subscribeOn(Schedulers.fromExecutor(executor)) // Run in virtual threads
                                    .onErrorResume(e -> {
                                        System.err.println("Error: " + e.getMessage());
                                        return Mono.empty();
                                    }));
                });

    }*/
}

