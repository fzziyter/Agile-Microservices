package com.example.Backend;

import com.example.Backend.model.Role;
import com.example.Backend.model.User;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("loadUserByUsername returns UserDetails with ROLE_ prefix for ADMIN")
    void loadUserByUsername_adminUser_hasRoleAdminAuthority() {
        User user = buildUser("alice", "hashed", Role.ADMIN);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername returns correct authority for DEVELOPER role")
    void loadUserByUsername_developer_hasRoleDeveloperAuthority() {
        when(userRepository.findByUsername("bob"))
                .thenReturn(Optional.of(buildUser("bob", "pw", Role.DEVELOPER)));

        UserDetails details = userDetailsService.loadUserByUsername("bob");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_DEVELOPER");
    }

    @Test
    @DisplayName("loadUserByUsername throws UsernameNotFoundException when user does not exist")
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    private User buildUser(String username, String password, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole(role);
        return u;
    }
}