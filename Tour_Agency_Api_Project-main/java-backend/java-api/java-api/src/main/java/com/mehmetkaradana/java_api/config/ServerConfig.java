package com.mehmetkaradana.java_api.config;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "servers")
public class ServerConfig {
    private List<Server> servers;

    public List<Server> getServers() {
       return Optional.ofNullable(servers).orElse(Collections.emptyList());
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public static class Server {
        private String name;
        private String url;
        private int distance_km;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getDistance_km(){
            return distance_km;
        }   
        
        public void SetDistance_km(int distance_km){
            this.distance_km=distance_km;
        }

        public long calculateResponseTime() {
            // Latency calculation per distance (for example, 1/10 ms)
            return distance_km / 10L;
        }
        
        @Override
        public String toString() {
            return "Server{name='" + name + "', url='" + url + "', distanceKm=" + distance_km + '}';
        }
    
    }
}
