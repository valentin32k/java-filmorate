package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenresService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenresController {
    private final GenresService genresService;

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable Integer id) {
        return genresService.getGenreById(id);
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        return genresService.getAllGenres();
    }
}
