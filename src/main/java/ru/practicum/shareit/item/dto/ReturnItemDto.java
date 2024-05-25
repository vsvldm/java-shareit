package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Data
@Builder
public class ReturnItemDto {
    private long id;
    private String name;
    private String description;
    private boolean available;
    private User owner;
    private BookingDtoForItem lastBooking;
    private BookingDtoForItem nextBooking;
    private List<CommentDto> comments;
}
