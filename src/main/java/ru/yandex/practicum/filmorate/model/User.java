package ru.yandex.practicum.filmorate.model;

import lombok.Value;
import lombok.With;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
public class User {
    @With
    int id;
    @Email
    @NotBlank String email;
    @NotBlank String login;
    String name;
    String birthday;
}
