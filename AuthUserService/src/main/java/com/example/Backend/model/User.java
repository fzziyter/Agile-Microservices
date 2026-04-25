package com.example.Backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users") // Nom de la table dans MySQL
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Stocké haché via BCrypt

    @Enumerated(EnumType.STRING)
    private Role role; // Association d'un rôle unique
}