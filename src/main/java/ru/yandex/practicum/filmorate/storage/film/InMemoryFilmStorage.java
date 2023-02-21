package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private static final Comparator<Film> filmComparator = Comparator
            .comparing(Film::getLikesCount, Comparator.reverseOrder())
            .thenComparing(Film::getId);
    private final Map<Integer, Film> filmsById = new HashMap<>();

    private int id = 0;

    @Override
    public Film addFilm(Film film) {
        film = film.withId(++id);
        film = film.withLikedUsersIds(new HashSet<>());
        filmsById.put(id, film);
        return film;
    }

    @Override
    public void removeFilm(int id) {
        filmsById.remove(id);
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getLikedUsersIds() == null) {
            film = film.withLikedUsersIds(new HashSet<>());
        }
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

    @Override
    public List<Film> getMostPopularFilms(int count) {
        return filmsById
                .values()
                .stream()
                .sorted(filmComparator)
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Mpa getMpaById(int id) {
        return null;
    }

    @Override
    public List<Mpa> getAllMpa() {
        return null;
    }

    @Override
    public Genre getGenreById(int id) {
        return null;
    }

    @Override
    public List<Genre> getAllGenres() {
        return null;
    }
}