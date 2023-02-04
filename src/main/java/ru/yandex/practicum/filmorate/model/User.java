package ru.yandex.practicum.filmorate.model;

import lombok.Value;
import lombok.With;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Set;

@Value
public class User {
    @With
    @DecimalMin("0") int id;
    @Email
    @NotBlank String email;
    @NotBlank String login;
    @With
    String name;
    LocalDate birthday;
    @With
    Set<Integer> friendsIds;
}
