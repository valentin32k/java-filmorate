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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        if (film == null || user == null) {
            throw new NotFoundException();
        }
        Set<Integer> likes = film.getLikedUsersIds();
        if (likes == null) {
            likes = new HashSet<>();
        }
        likes.add(userId);
        film = film.withLikedUsersIds(likes);
        filmStorage.updateFilm(film);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        if (film == null || user == null) {
            throw new NotFoundException();
        }
        Set<Integer> likes = film.getLikedUsersIds();
        if (likes == null) {
            likes = new HashSet<>();
        }
        likes.remove(userId);
//        Вот здесь у меня небольшой вопрос как правильно делать.
//        Строки ниже, строго говоря, не нужны, но без них код выглядит не логичным.
//        Оставить как есть или это только мне кажется что логика не видна?
        film = film.withLikedUsersIds(likes);
        filmStorage.updateFilm(film);
    }

    public List<Film> getMostPopularFilms(int count) {
        Comparator<Film> filmComparator = Comparator
                .comparing(Film::getLikesCount, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Film::getId);
        return filmStorage
                .getFilms()
                .stream()
                .sorted(filmComparator)
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilmById(int id) {
        if (filmStorage.getFilmById(id) == null) {
            throw new NotFoundException();
        }
        return filmStorage.getFilmById(id);
    }

    public Film addFilm(Film film) {
        if (isFilmDataErrors(film)) {
            throw new ValidationException();
        }
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()) == null) {
            throw new NotFoundException();
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
        }
    }

    private boolean isFilmDataErrors(Film film) {
        LocalDate releaseDate = LocalDate.parse(film.getReleaseDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}
