package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> filmsById = new HashMap<>();
    private int id = 0;

    @Override
    public Film addFilm(Film film) {
        film = film.withId(++id);
        filmsById.put(id, film);
        return film;
    }

    @Override
    public void removeFilm(int id) {
        filmsById.remove(id);
    }

    @Override
    public Film updateFilm(Film film) {
        filmsById.put(film.getId(), film);
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        return filmsById.get(id);
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(filmsById.values());
    }
}