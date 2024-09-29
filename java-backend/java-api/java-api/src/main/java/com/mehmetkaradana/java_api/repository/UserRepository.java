package com.mehmetkaradana.java_api.repository;

import java.util.Optional;

import com.mehmetkaradana.java_api.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends MongoRepository<User, String> {
  Optional<User> findByUsername(String username);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);
}
