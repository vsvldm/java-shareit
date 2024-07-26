package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingRepositoryTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void findAllByBookerOrderByStartDesc() {
    }

    @Test
    void testFindAllByBookerOrderByStartDesc() {
    }

    @Test
    void findAllCurrentByBooker() {
    }

    @Test
    void testFindAllCurrentByBooker() {
    }

    @Test
    void findAllByBookerAndEndBeforeOrderByStartDesc() {
    }

    @Test
    void testFindAllByBookerAndEndBeforeOrderByStartDesc() {
    }

    @Test
    void findAllByBookerAndStartAfterOrderByStartDesc() {
    }

    @Test
    void testFindAllByBookerAndStartAfterOrderByStartDesc() {
    }

    @Test
    void findAllByBookerAndStatusOrderByStartDesc() {
    }

    @Test
    void testFindAllByBookerAndStatusOrderByStartDesc() {
    }

    @Test
    void findAllByItemOwnerOrderByStartDesc() {
    }

    @Test
    void testFindAllByItemOwnerOrderByStartDesc() {
    }

    @Test
    void findAllCurrentByOwner() {
    }

    @Test
    void testFindAllCurrentByOwner() {
    }

    @Test
    void findAllByItemOwnerAndEndBeforeOrderByStartDesc() {
    }

    @Test
    void testFindAllByItemOwnerAndEndBeforeOrderByStartDesc() {
    }

    @Test
    void findAllByItemOwnerAndStartAfterOrderByStartDesc() {
    }

    @Test
    void testFindAllByItemOwnerAndStartAfterOrderByStartDesc() {
    }

    @Test
    void findAllByItemOwnerAndStatusOrderByStartDesc() {
    }

    @Test
    void testFindAllByItemOwnerAndStatusOrderByStartDesc() {
    }

    @Test
    void findTop1ByItemAndBookerAndEndBefore() {
    }

    @Test
    void findTop1ByItemAndStartAfterAndStatusOrderByStartAsc() {
    }

    @Test
    void findTop1ByItemAndStartBeforeAndStatusOrderByEndDesc() {
    }
}