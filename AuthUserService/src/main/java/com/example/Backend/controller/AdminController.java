package com.example.Backend.controller;

import com.example.Backend.model.User;
import com.example.Backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")

public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint pour créer un utilisateur (Accessible uniquement par l'ADMIN)
     */
    @PostMapping("/users")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.status(201).body(userService.createUser(user));
    }

    // Lister tous les utilisateurs pour voir qui a accès à quoi
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    // Supprimer un accès (révoquer un utilisateur)
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Modifier un utilisateur existant (Rôle, mot de passe, etc.)
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }
}