package com.spendsense.user.repository;

import com.spendsense.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser(){
        User user = new User(
                "Swath",
                "swa2@gmail.com",
                "1234567",
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
                "swa@gmail.com",
                "1234567",
                "hashed-password"
        );

        User savedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("swa2@gmail.com");

        assertTrue(foundUser.isPresent());
        assertEquals("Swath",foundUser.get().getName());
        assertEquals("swa@gmail.com",foundUser.get().getEmail());
    }
}
