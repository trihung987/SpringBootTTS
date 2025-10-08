package me.trihung.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import me.trihung.entity.Role;
import me.trihung.repository.RoleRepository;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

    @Bean
    CommandLineRunner initRoles() {
        return args -> createRolesIfNotExist();
    }

    @Transactional
    void createRolesIfNotExist() {
        createRoleIfMissing("ADMIN");
        createRoleIfMissing("USER");
        createRoleIfMissing("MANAGER");
        System.out.println("✅ Default roles initialized successfully!");
    }

    private void createRoleIfMissing(String name) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(Role.builder().name(name).build());
            System.out.println("➕ Created role: " + name);
        }
    }
}
