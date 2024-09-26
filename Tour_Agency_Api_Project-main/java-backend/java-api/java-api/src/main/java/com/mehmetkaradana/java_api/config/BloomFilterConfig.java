package com.mehmetkaradana.java_api.config;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;

@Configuration
public class BloomFilterConfig {

    @Bean
    public BloomFilter<String> userBloomFilter() {
        // Bloom Filter'ı oluşturun (örn. 1000 kullanıcı kapasiteli, %1 yanlış pozitif olasılığı ile)
        return BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 1000, 0.01);
    }
}