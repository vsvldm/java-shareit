package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    private long id;
    private final String name;
    @NotNull
    @Email
    private final String email;
}