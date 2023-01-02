package ru.yandex.practicum.filmorate.exceptions;

public class ValidationException extends RuntimeException {
    public ValidationException() {
        super("Передан запрос с некорректными данными");
    }
}