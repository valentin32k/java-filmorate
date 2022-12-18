package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> filmsById = new HashMap<>();
    private int id = 0;


    @PostMapping
    public Film addFilm(@Valid @RequestBody Film newFilm) {
        if (isFilmDataErrors(newFilm)) {
            log.warn("Фильм не был добавлен. В запросе указаны не корректные данные");
            throw new ValidationException();
        }
        newFilm = newFilm.withId(++id);
        filmsById.put(newFilm.getId(), newFilm);
        log.info("В хранилище добавлен новый фильм: {}", newFilm.toString());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        if (!filmsById.containsKey(newFilm.getId())) {
            log.warn("Попытка обновления не существующего фильма");
            throw new NotFoundException();
        }
        if (isFilmDataErrors(newFilm)) {
            log.warn("Фильм не был обновлен. В запросе указаны не корректные данные");
            throw new ValidationException();
        }
        filmsById.put(newFilm.getId(), newFilm);
        log.info("Сведения о фильме {} изменены на: {}", newFilm.getName(), newFilm.toString());
        return newFilm;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(filmsById.values());
    }

    private boolean isFilmDataErrors(Film film) {
        LocalDate releaseDate = LocalDate.parse(film.getReleaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}