package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {
    private long id;
    private final String name;
    private final String description;
    private final boolean available;
    private final long ownerId;
}
