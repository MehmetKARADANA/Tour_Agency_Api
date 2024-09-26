package com.mehmetkaradana.java_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mehmetkaradana.java_api.config.ServerConfig;
import com.mehmetkaradana.java_api.service.TourismAgencyService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class TourismController {

    private final TourismAgencyService tourismAgencyService;
    private final ServerConfig serverConfig;

    public TourismController(TourismAgencyService tourismAgencyService ,ServerConfig serverConfig) {
        this.tourismAgencyService = tourismAgencyService;
        this.serverConfig = serverConfig;
    }

    @GetMapping("/fetch-rooms")
    @PreAuthorize("hasRole('USER')")
    public Mono<String> fetchRooms(@RequestParam String agencyUrl) {
        return tourismAgencyService.getRoomsFromAgency(agencyUrl).onErrorResume(e -> Mono.error(new RuntimeException("An error occurred:: " + e.getMessage())));

    }

    @GetMapping("/fetch-all-rooms")
    @PreAuthorize("hasRole('USER')")
    public Flux<String> fetchAllRooms() {
        return tourismAgencyService.getRoomsFromAllAgencies().onErrorResume(e -> Mono.error(new RuntimeException("An error occurred:: " + e.getMessage())));
    }

    @GetMapping("/list-servers")
    public ResponseEntity<List<ServerConfig.Server>> listServers() {
        List<ServerConfig.Server> servers = serverConfig.getServers();
        servers.forEach(server -> System.out.println("Server: " + server.getName() + ", URL: " + server.getUrl()));
        return ResponseEntity.ok(servers);
    }
}
