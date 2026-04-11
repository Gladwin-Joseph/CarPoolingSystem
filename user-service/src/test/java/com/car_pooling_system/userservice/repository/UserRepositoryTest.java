package com.car_pooling_system.userservice.repository;

import com.car_pooling_system.userservice.model.Role;
import com.car_pooling_system.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        savedUser = userRepository.save(User.builder()
                .name("Jane Doe")
                .username("janedoe")
                .email("jane@example.com")
                .password("encodedPassword")
                .phoneNumber("0851234567")
                .role(Role.PASSENGER)
                .build());
    }

    @Test
    @DisplayName("findByEmail() - should find user by email")
    void findByEmail_success() {
        Optional<User> result = userRepository.findByEmail("jane@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("janedoe");
    }

    @Test
    @DisplayName("findByEmail() - should return empty when email not found")
    void findByEmail_returnsEmpty_whenNotFound() {
        Optional<User> result = userRepository.findByEmail("notfound@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsername() - should find user by username")
    void findByUsername_success() {
        Optional<User> result = userRepository.findByUsername("janedoe");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @DisplayName("existsByEmail() - should return true when email exists")
    void existsByEmail_returnsTrue() {
        boolean exists = userRepository.existsByEmail("jane@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail() - should return false when email does not exist")
    void existsByEmail_returnsFalse() {
        boolean exists = userRepository.existsByEmail("nope@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByUsername() - should return true when username exists")
    void existsByUsername_returnsTrue() {
        boolean exists = userRepository.existsByUsername("janedoe");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername() - should return false when username does not exist")
    void existsByUsername_returnsFalse() {
        boolean exists = userRepository.existsByUsername("unknown");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("save() - should persist user with auto-generated ID")
    void save_persistsUser() {
        User newUser = userRepository.save(User.builder()
                .name("Bob Driver")
                .username("bobdriver")
                .email("bob@example.com")
                .password("encoded")
                .role(Role.DRIVER)
                .build());

        assertThat(newUser.getId()).isNotNull();
        assertThat(userRepository.findById(newUser.getId())).isPresent();
    }

    @Test
    @DisplayName("delete() - should remove user from DB")
    void delete_removesUser() {
        userRepository.delete(savedUser);

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }
}