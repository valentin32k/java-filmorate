package ru.yandex.practicum.filmorate.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("Данные по запросу не найдены");
    }
}
