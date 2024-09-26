package com.mehmetkaradana.java_api.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.Charset;

public class BloomFilterRename {
    private BloomFilter<String> bloomFilter;

    public BloomFilterRename() {
        // Estimate the expected number of elements and the false positive probability
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 1000, 0.01);
    }

    public void add(String value) {
        bloomFilter.put(value);
    }

    public boolean mightContain(String value) {
        return bloomFilter.mightContain(value);
    }
}

