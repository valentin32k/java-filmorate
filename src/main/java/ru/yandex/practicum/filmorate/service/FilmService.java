package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        Set<Integer> likes = film.getLikedUsersIds();
        likes.add(userId);
        film = film.withLikedUsersIds(likes);
        filmStorage.updateFilm(film);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        Set<Integer> likes = film.getLikedUsersIds();
        likes.remove(userId);
        film = film.withLikedUsersIds(likes);
        filmStorage.updateFilm(film);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getMostPopularFilms(count);
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    public Film addFilm(Film film) {
        if (isFilmDataErrors(film)) {
            throw new ValidationException();
        }
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        if (isFilmDataErrors(film)) {
            throw new ValidationException();
        }
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void removeFilm(int id) {
        if (filmStorage.getFilmById(id) != null) {
            filmStorage.removeFilm(id);
        } else {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    private boolean isFilmDataErrors(Film film) {
        LocalDate releaseDate = LocalDate.parse(film.getReleaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}
