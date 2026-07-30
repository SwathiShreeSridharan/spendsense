package com.spendsense.user.repository;

import com.spendsense.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser(){
        User user = new User(
                "Swath",
                "swa2@gmail.com",
                "1234567890",
                "hashed-password"
        );

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getUserId());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    @Test
    void shouldFindUserByEmail(){
        User user = new User(
                "Swath",
                "swa2@gmail.com",
                "1234567890",
                "hashed-password"
        );

        User savedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("swa2@gmail.com");

        assertTrue(foundUser.isPresent());
        assertEquals("Swath",foundUser.get().getName());
        assertEquals("swa2@gmail.com",foundUser.get().getEmail());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> foundUser =
                userRepository.findByEmail("unknown@gmail.com");

        assertTrue(foundUser.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        User user = new User(
                "Swathi",
                "exists@gmail.com",
                "9876543210",
                "hashed-password"
        );

        userRepository.save(user);

        boolean exists =
                userRepository.existsByEmail("exists@gmail.com");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        boolean exists =
                userRepository.existsByEmail("unknown@gmail.com");

        assertFalse(exists);
    }
}
