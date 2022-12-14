package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Film {
    private int id;
    private final String name;
    private final String description;
    private final String releaseDate;
    private final long duration;
}
