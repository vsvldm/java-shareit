package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository requestRepository;
    private final User owner = User.builder()
            .name("OwnerName")
            .email("owner@email.com")
            .build();
    private final User requestor = User.builder()
            .name("RequestorName")
            .email("requestor@email.com")
            .build();
    private final ItemRequest request = ItemRequest.builder()
            .description("Description")
            .requestor(requestor)
            .created(LocalDateTime.now())
            .build();
    private final Item item = Item.builder()
            .name("Title")
            .description("ItemDescription")
            .available(true)
            .owner(owner)
            .request(request)
            .build();
    private final PageRequest pagination = PageRequest.of(0, 10);

    @BeforeEach
    public void setUp() {
        userRepository.save(owner);
        userRepository.save(requestor);
        requestRepository.save(request);
        itemRepository.save(item);
    }

    @Test
    void findAllByOwner_whenNormallyInvoked_thenReturnItemCollection() {
        List<Item> expectedCol = List.of(item);

        assertEquals(expectedCol, itemRepository.findAllByOwner(owner));
    }

    @Test
    void findAllByOwner_whenNotFoundItemByOwner_thenReturnEmptyCollection() {
        assertTrue(itemRepository.findAllByOwner(requestor).isEmpty());
    }

    @Test
    void findAllByOwner_whenNormallyInvokedWithPagination_thenReturnPage() {
        List<Item> expectedCol = List.of(item);

        assertEquals(expectedCol, itemRepository.findAllByOwner(owner, pagination).getContent());
    }

    @Test
    void findAllByNameOrDescription_whenNormallyInvokedFindByName_thenReturnItemList() {
        Item itemForTest = Item.builder()
                .name("ItemSearching")
                .description("ItemDescription")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        itemRepository.save(itemForTest);

        List<Item> expectedList = List.of(itemForTest);

        assertEquals(expectedList, itemRepository.findAllByNameOrDescription("SEARCHING"));
    }

    @Test
    void findAllByNameOrDescription_whenNormallyInvokedFindByDescription_thenReturnItemList() {
        Item itemForTest = Item.builder()
                .name("Title")
                .description("SearchingDescription")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        itemRepository.save(itemForTest);

        List<Item> expectedList = List.of(itemForTest);

        assertEquals(expectedList, itemRepository.findAllByNameOrDescription("SEARCHING"));
    }

    @Test
    void findAllByNameOrDescription_whenInvokedButNotFoundAnything_thenReturnEmptyList() {
        List<Item> expectedList = Collections.emptyList();

        assertEquals(expectedList, itemRepository.findAllByNameOrDescription("SEARCHING"));
    }

    @Test
    void findAllByNameOrDescription_whenNormallyInvokedFindByNameWithPagination_thenReturnItemList() {
        Item itemForTest = Item.builder()
                .name("ItemSearching")
                .description("ItemDescription")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        itemRepository.save(itemForTest);

        List<Item> expectedList = List.of(itemForTest);

        assertEquals(expectedList, itemRepository.findAllByNameOrDescription("SEARCHING", pagination).getContent());
    }

    @Test
    void findAllByNameOrDescription_whenNormallyInvokedFindByDescriptionWithPagination_thenReturnItemList() {
        Item itemForTest = Item.builder()
                .name("Title")
                .description("SearchingDescription")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        itemRepository.save(itemForTest);

        List<Item> expectedList = List.of(itemForTest);

        assertEquals(expectedList, itemRepository.findAllByNameOrDescription("SEARCHING", pagination).getContent());
    }

    @Test
    void findAllByRequest_whenNormallyInvoked_thenReturnItemList() {
        List<Item> expectedList = List.of(item);

        assertEquals(expectedList, itemRepository.findAllByRequest(request));
    }

    @Test
    void findAllByRequest_whenInvokedButNotFoundAnything_thenReturnEmptyList() {
        List<Item> expectedList = Collections.emptyList();
        ItemRequest requestForTest = ItemRequest.builder()
                .id(100L)
                .description("Description")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        assertEquals(expectedList, itemRepository.findAllByRequest(requestForTest));
    }
}