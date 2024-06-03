package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ResponseItemDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestDto {
    private long id;
    @NotNull
    private String description;
    private LocalDateTime created;
    private List<ResponseItemDto> items;
}
