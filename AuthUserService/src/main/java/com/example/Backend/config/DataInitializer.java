package com.example.Backend.config;

import com.example.Backend.model.User;
import com.example.Backend.model.Role;
import com.example.Backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // On vérifie si un admin existe déjà pour ne pas le créer en double
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Hachage auto
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            System.out.println("✅ Premier Administrateur créé : admin / admin123");
        }
    }
}