package com.mehmetkaradana.java_api.repository;

import java.util.Optional;

import com.mehmetkaradana.java_api.models.ERole;
import com.mehmetkaradana.java_api.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {
  Optional<Role> findByName(ERole name);
}
