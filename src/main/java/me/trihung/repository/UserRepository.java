package me.trihung.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import me.trihung.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);

}