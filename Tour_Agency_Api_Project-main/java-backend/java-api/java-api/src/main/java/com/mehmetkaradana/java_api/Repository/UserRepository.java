package com.mehmetkaradana.java_api.Repository;

import com.mehmetkaradana.java_api.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}