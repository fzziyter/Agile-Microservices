package com.example.Backend;

import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.Backend.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("alice");
        sampleUser.setPassword("plaintext");
        sampleUser.setRole(Role.DEVELOPER);
    }

    // -----------------------------------------------------------------------
    // createUser
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("createUser encodes the password before saving")
    void createUser_encodesPassword() {
        when(passwordEncoder.encode("plaintext")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(sampleUser);

        assertThat(result.getPassword()).isEqualTo("hashed");
        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("createUser returns the saved user")
    void createUser_returnsSavedUser() {
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(sampleUser);

        User result = userService.createUser(sampleUser);
        assertThat(result).isSameAs(sampleUser);
    }

    // -----------------------------------------------------------------------
    // findAllUsers
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("findAllUsers delegates to repository and returns the list")
    void findAllUsers_returnsAllUsers() {
        User second = new User();
        second.setId(2L);
        second.setUsername("bob");

        when(userRepository.findAll()).thenReturn(List.of(sampleUser, second));

        List<User> result = userService.findAllUsers();

        assertThat(result).hasSize(2).contains(sampleUser, second);
    }

    @Test
    @DisplayName("findAllUsers returns empty list when no users exist")
    void findAllUsers_empty() {
        when(userRepository.findAll()).thenReturn(List.of());
        assertThat(userService.findAllUsers()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // updateUser
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("updateUser changes username when provided")
    void updateUser_changesUsername() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User patch = new User();
        patch.setUsername("alice_new");

        User result = userService.updateUser(1L, patch);
        assertThat(result.getUsername()).isEqualTo("alice_new");
    }

    @Test
    @DisplayName("updateUser changes role when provided")
    void updateUser_changesRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User patch = new User();
        patch.setRole(Role.SCRUM_MASTER);

        User result = userService.updateUser(1L, patch);
        assertThat(result.getRole()).isEqualTo(Role.SCRUM_MASTER);
    }

    @Test
    @DisplayName("updateUser encodes password when password field is provided")
    void updateUser_encodesNewPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("newpass")).thenReturn("newhash");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User patch = new User();
        patch.setPassword("newpass");

        User result = userService.updateUser(1L, patch);
        assertThat(result.getPassword()).isEqualTo("newhash");
    }

    @Test
    @DisplayName("updateUser skips null fields (partial update)")
    void updateUser_skipsNullFields() {
        sampleUser.setUsername("original");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // patch has all null fields
        User result = userService.updateUser(1L, new User());

        assertThat(result.getUsername()).isEqualTo("original");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("updateUser throws when user not found")
    void updateUser_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(99L, new User()))
                .isInstanceOf(NoSuchElementException.class);
    }

    // -----------------------------------------------------------------------
    // deleteUser
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("deleteUser delegates to repository.deleteById")
    void deleteUser_callsDeleteById() {
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }
}