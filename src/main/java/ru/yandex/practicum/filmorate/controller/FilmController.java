package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

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

    private boolean isFilmDataErrors(Film film) {
        boolean isNameErrors = film.getName().isBlank();
        boolean isDescriptionErrors = film.getDescription().length() > 200;
        LocalDate releaseDate = LocalDate.parse(film.getReleaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        boolean isDateErrors = releaseDate.isBefore(LocalDate.of(1895, 12, 28));
        boolean isDurationErrors = film.getDuration() <= 0;
        return isNameErrors || isDescriptionErrors || isDateErrors || isDurationErrors;
    }

    @PostMapping
    public Film addFilm(@RequestBody Film newFilm) {
        if (isFilmDataErrors(newFilm)) {
            log.warn("Фильм не был добавлен. В запросе указаны не корректные данные");
            throw new ValidationException();
        }
        newFilm.setId(++id);
        filmsById.put(newFilm.getId(), newFilm);
        log.info("В хранилище добавлен новый фильм: {}", newFilm.toString());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
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
        if (filmsById.isEmpty()) {
            log.warn("Запрос пустого списка фильмов");
            throw new NotFoundException();
        }
        return new ArrayList<>(filmsById.values());
    }
}
