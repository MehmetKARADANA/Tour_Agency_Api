package com.mehmetkaradana.java_api.service;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.mehmetkaradana.java_api.Repository.UserRepository;
import com.mehmetkaradana.java_api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.Charset;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private BloomFilter<String> userBloomFilter;



    public void registerUser(String username, String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        User newUser = new User(username, hashedPassword);
        userRepository.save(newUser);
        userBloomFilter.put(username);
    }

    public boolean authenticateUser(String username, String plainPassword) {
        if (userBloomFilter.mightContain(username)) { User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, user.getPassword());
    }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in Bloom Filter");
    }
}