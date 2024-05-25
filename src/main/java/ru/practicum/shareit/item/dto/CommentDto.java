package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private long id;
    @NotNull
    @NotEmpty
    private String text;
    private String authorName;
    private LocalDateTime created;
}
