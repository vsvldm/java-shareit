package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    private final User user = User.builder()
            .name("Name")
            .email("email@mail.ru")
            .build();

    @BeforeEach
    public void setUp() {
        userRepository.save(user);
    }

    @Test
    void existsByEmail() {
        assertTrue(userRepository.existsByEmail("email@mail.ru"));
        assertFalse(userRepository.existsByEmail("email@gmail.com"));
    }
}