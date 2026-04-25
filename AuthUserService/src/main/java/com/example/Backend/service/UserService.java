package com.example.Backend.service;

import com.example.Backend.model.User;
import com.example.Backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User details) {
        User user = userRepository.findById(id).orElseThrow();
        if(details.getUsername() != null) user.setUsername(details.getUsername());
        if(details.getRole() != null) user.setRole(details.getRole());
        if(details.getPassword() != null) user.setPassword(passwordEncoder.encode(details.getPassword()));
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}