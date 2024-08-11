package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemRequestRepository requestRepository;
    private final User author = User.builder()
            .name("NameTest")
            .email("author@mail.com")
            .build();
    private final User owner = User.builder()
            .name("Owner")
            .email("owner@mail.com")
            .build();
    private final ItemRequest request = ItemRequest.builder()
            .description("Description")
            .requestor(owner)
            .created(LocalDateTime.now().minusDays(1))
            .build();
    private final Item item = Item.builder()
            .name("Name")
            .description("Description")
            .available(true)
            .request(request)
            .owner(owner)
            .build();
    private final Comment comment = Comment.builder()
            .author(author)
            .text("Comment")
            .item(item)
            .created(LocalDateTime.now())
            .build();
    private final Comment comment1 = Comment.builder()
            .author(author)
            .text("Comment1")
            .item(item)
            .created(LocalDateTime.now())
            .build();

    @BeforeEach
    public void setUp() {
        userRepository.saveAll(List.of(author, owner));
        requestRepository.save(request);
        itemRepository.save(item);
        commentRepository.saveAll(List.of(comment, comment1));
    }

    @Test
    void findByItem_whenNormallyInvoked_thenReturnCollectionComments() {
        Collection<Comment> comments = List.of(comment, comment1);

        assertEquals(comments, commentRepository.findByItem(item));
    }

    @Test
    void findByItem_whenInvokedWithUnknownItem_thenReturnEmptyList() {
        Item itemForTest = Item.builder()
                .id(100L)
                .name("Name2")
                .description("Description2")
                .available(true)
                .request(request)
                .owner(owner)
                .build();

        assertTrue(commentRepository.findByItem(itemForTest).isEmpty());
    }
}