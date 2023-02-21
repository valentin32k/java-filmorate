package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    void removeFilm(int id);

    Film updateFilm(Film film);

    Film getFilmById(int id);

    List<Film> getFilms();

    List<Film> getMostPopularFilms(int count);

    Mpa getMpaById(int id);

    List<Mpa> getAllMpa();

    Genre getGenreById(int id);

    List<Genre> getAllGenres();
}
