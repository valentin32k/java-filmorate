package ru.yandex.practicum.filmorate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
@RestControllerAdvice("ru.yandex.practicum.filmorate.controller")
public class ExceptionHandlers {
    @ExceptionHandler
    public ResponseEntity<Map<String, String>> notFoundExceptionHandler(final NotFoundException e) {
        return new ResponseEntity<>(Map.of("error: ", e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> validationExceptionHandler(final ValidationException e) {
        return new ResponseEntity<>(Map.of("error: ", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> validationModelExceptionHandler(final MethodArgumentNotValidException e) {
        return new ResponseEntity<>(Map.of("error: ", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> otherExceptionHandler(final Exception e) {
        return new ResponseEntity<>(Map.of("error: ", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
