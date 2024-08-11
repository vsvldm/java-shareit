package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommentMapperTest {

    private CommentMapper commentMapper;
    private Comment comment;
    private CommentDto commentDto;
    private User user;
    private Item item;

    @BeforeEach
    public void setUp() {
        commentMapper = new CommentMapper();
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
        item = Item.builder()
                .id(1L)
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .owner(user)
                .build();
        comment = Comment.builder()
                .id(1L)
                .text("This is a comment")
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();
        commentDto = CommentDto.builder()
                .id(1L)
                .text("This is a comment")
                .authorName("John Doe")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    public void testToCommentDto() {
        CommentDto mappedCommentDto = commentMapper.toCommentDto(comment);

        assertEquals(comment.getId(), mappedCommentDto.getId());
        assertEquals(comment.getText(), mappedCommentDto.getText());
        assertEquals(comment.getAuthor().getName(), mappedCommentDto.getAuthorName());
        assertEquals(comment.getCreated(), mappedCommentDto.getCreated());
    }

    @Test
    public void testToComment() {
        Comment mappedComment = commentMapper.toComment(commentDto, user, item);

        assertEquals(commentDto.getId(), mappedComment.getId());
        assertEquals(commentDto.getText(), mappedComment.getText());
        assertEquals(user, mappedComment.getAuthor());
        assertEquals(item, mappedComment.getItem());
        assertEquals(commentDto.getCreated(), mappedComment.getCreated());
    }
}
