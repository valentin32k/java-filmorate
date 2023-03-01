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
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmDbStorage;
    private final UserStorage userDbStorage;

    public void addLike(int filmId, int userId) {
        Film film = filmDbStorage.getFilmById(filmId);
        User user = userDbStorage.getUserById(userId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        Set<Integer> likes = film.getLikedUsersIds();
        likes.add(userId);
        film = film.withLikedUsersIds(likes);
        filmDbStorage.updateFilm(film);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmDbStorage.getFilmById(filmId);
        User user = userDbStorage.getUserById(userId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        Set<Integer> likes = film.getLikedUsersIds();
        likes.remove(userId);
        film = film.withLikedUsersIds(likes);
        filmDbStorage.updateFilm(film);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmDbStorage.getMostPopularFilms(count);
    }

    public Film getFilmById(int id) {
        Film film = filmDbStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    public Film addFilm(Film film) {
        if (isFilmDataErrors(film)) {
            throw new ValidationException();
        }
        return filmDbStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (filmDbStorage.getFilmById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        if (isFilmDataErrors(film)) {
            throw new ValidationException();
        }
        return filmDbStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmDbStorage.getFilms();
    }

    public void removeFilm(int id) {
        if (filmDbStorage.getFilmById(id) == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        filmDbStorage.removeFilm(id);
    }

    private boolean isFilmDataErrors(Film film) {
        return film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28));
    }
}
