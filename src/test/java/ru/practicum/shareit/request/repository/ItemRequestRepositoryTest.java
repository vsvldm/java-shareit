package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestRepositoryTest {
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;
    private final User requestor = User.builder()
            .name("NameTest")
            .email("requestor@mail.com")
            .build();
    private final User notRequestor = User.builder()
            .name("NotRequestor")
            .email("NotRequestor@mail.com")
            .build();
    private final ItemRequest request1 = ItemRequest.builder()
            .description("DescriptionTest1")
            .requestor(requestor)
            .created(LocalDateTime.now())
            .build();
    private final ItemRequest request2 = ItemRequest.builder()
            .description("DescriptionTest2")
            .requestor(requestor)
            .created(LocalDateTime.now().plusDays(1))
            .build();
    private final ItemRequest request3 = ItemRequest.builder()
            .description("DescriptionTest3")
            .requestor(notRequestor)
            .created(LocalDateTime.now())
            .build();

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(List.of(requestor, notRequestor));
        requestRepository.saveAll(List.of(request1, request2, request3));
    }

    @Test
    void findAllByRequestorOrderByCreatedDesc_whenNormallyInvoked_thenReturnListItemRequest() {
        List<ItemRequest> actualList = new ArrayList<>(requestRepository.findAllByRequestorOrderByCreatedDesc(requestor));

        assertFalse(actualList.isEmpty());
        assertEquals(2, actualList.size());
        assertEquals(request2, actualList.get(0));
        assertEquals(request1, actualList.get(1));
    }

    @Test
    void findAllByRequestorOrderByCreatedDesc_whenInvokedWithUnknownRequestor_thenReturnEmptyList() {
        User userForTest = User.builder()
                .id(3L)
                .name("AnyName")
                .email("any@email.com")
                .build();

        List<ItemRequest> actualList = new ArrayList<>(requestRepository.findAllByRequestorOrderByCreatedDesc(userForTest));

        System.out.println(actualList);
        assertTrue(actualList.isEmpty());
    }
}