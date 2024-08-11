package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Email;

@Data
@Builder
public class UpdatedUserDto {
    private long id;
    private final String name;
    @Email
    private final String email;
}