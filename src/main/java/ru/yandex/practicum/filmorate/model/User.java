package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class User {
    private int id;
    private final String email;
    private final String login;
    private final String name;
    private final String birthday;
}
