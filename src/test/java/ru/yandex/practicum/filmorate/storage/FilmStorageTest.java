package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    Film validFilm = Film.builder()
            .id(0)
            .name("Название фильма")
            .description("Описание фильма")
            .releaseDate(LocalDate.parse("1999-01-01"))
            .duration(1500)
            .mpa(new Mpa(1, "G", "у фильма нет возрастных ограничений"))
            .likedUsersIds(new HashSet<>())
            .genres(new HashSet<>())
            .build();

    @BeforeEach
    void clearStorage() {
        filmStorage.getFilms().forEach(f -> filmStorage.removeFilm(f.getId()));
    }

    @Test
    void testAddingValidFilm() {
        filmStorage.addFilm(validFilm);
        int id = filmStorage.addFilm(validFilm).getId();
        Film addingFilm = filmStorage.getFilmById(id);
        assertThat(addingFilm.getId()).isNotZero();
        assertThat(addingFilm.getName()).isEqualTo("Название фильма");
        assertThat(addingFilm.getDescription()).isEqualTo("Описание фильма");
        assertThat(addingFilm.getReleaseDate()).isEqualTo(LocalDate.parse("1999-01-01"));
        assertThat(addingFilm.getDuration()).isEqualTo(1500);
        assertThat(addingFilm.getMpa()).isEqualTo(new Mpa(1, "G", "у фильма нет возрастных ограничений"));
        assertThat(addingFilm.getLikedUsersIds()).isEmpty();
        assertThat(addingFilm.getGenres()).isEmpty();
    }

    @Test
    void testAddingVoidBodyFilm() {
        try {
            filmStorage.addFilm(null);
        } catch (ValidationException ex) {
            assertThat(ex.getMessage()).isEqualTo("Передан запрос с некорректными данными");
        }
    }

    @Test
    void testRemoveFilm() {
        int id = filmStorage.addFilm(validFilm).getId();
        filmStorage.removeFilm(id);
        try {
            filmStorage.getFilmById(id);
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("error: Фильм с id = " + id + " не найден");
        }
    }

    @Test
    void testValidUpdateFilm() {
        int id = filmStorage.addFilm(validFilm).getId();
        Film newFilm = Film.builder()
                .id(id)
                .name("Название фильма2")
                .description("Описание фильма2")
                .releaseDate(LocalDate.parse("1999-02-02"))
                .duration(2500)
                .mpa(new Mpa(2, "PG", "детям рекомендуется смотреть фильм с родителями"))
                .likedUsersIds(new HashSet<>())
                .genres(new HashSet<>())
                .build();
        filmStorage.updateFilm(newFilm);
        assertThat(filmStorage.getFilmById(id)).isEqualTo(newFilm);
    }

    @Test
    void testUpdatingVoidBodyFilm() {
        try {
            filmStorage.updateFilm(null);
        } catch (ValidationException ex) {
            assertThat(ex.getMessage()).isEqualTo("Передан запрос с некорректными данными");
        }
    }

    @Test
    void testUpdatingFilmWithWrongId() {
        try {
            filmStorage.updateFilm(validFilm);
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("error: Фильм с id = 0 не найден");
        }
    }

    @Test
    void testFindCorrectFilmById() {
        Film tmpFilm = filmStorage.addFilm(validFilm);
        assertThat(filmStorage.getFilmById(tmpFilm.getId())).isEqualTo(tmpFilm);
    }

    @Test
    void testFindWrongFilmById() {
        try {
            filmStorage.getFilmById(999);
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("error: Фильм с id = 999 не найден");
        }
    }

    @Test
    void testGetFilms() {
        assertThat(filmStorage.getFilms()).isEmpty();
        Film newFilm = Film.builder()
                .id(0)
                .name("Фильм2")
                .description("Описание фильма")
                .releaseDate(LocalDate.parse("1999-01-01"))
                .duration(1500)
                .mpa(new Mpa(1, "G", "у фильма нет возрастных ограничений"))
                .likedUsersIds(new HashSet<>())
                .genres(new HashSet<>())
                .build();
        filmStorage.addFilm(validFilm);
        filmStorage.addFilm(newFilm);
        assertThat(filmStorage.getFilms().size()).isEqualTo(2);
    }

    @Test
    void testGetMostPopularFilms() {
        User user1 = new User(0,
                "email@mail.ru",
                "Login",
                "Name",
                LocalDate.parse("1990-01-01"),
                new HashSet<>());
        User user2 = new User(0,
                "email22@mail.ru",
                "Login22",
                "Name22",
                LocalDate.parse("1990-01-01"),
                new HashSet<>());
        user1 = userStorage.addUser(user1);
        user2 = userStorage.addUser(user2);
        Film film1 = Film.builder()
                .id(0)
                .name("Название фильма11")
                .description("Описание фильма")
                .releaseDate(LocalDate.parse("1999-02-02"))
                .duration(1500)
                .mpa(new Mpa(1, "G", "у фильма нет возрастных ограничений"))
                .likedUsersIds(new HashSet<>())
                .genres(new HashSet<>())
                .build();
        Film film2 = Film.builder()
                .id(0)
                .name("Название фильма22")
                .description("Описание фильма2")
                .releaseDate(LocalDate.parse("1999-02-02"))
                .duration(1500)
                .mpa(new Mpa(1, "G", "у фильма нет возрастных ограничений"))
                .likedUsersIds(new HashSet<>())
                .genres(new HashSet<>())
                .build();
        Film film3 = Film.builder()
                .id(0)
                .name("Название фильма33")
                .description("Описание фильма3")
                .releaseDate(LocalDate.parse("1999-03-03"))
                .duration(1500)
                .mpa(new Mpa(1, "G", "у фильма нет возрастных ограничений"))
                .likedUsersIds(new HashSet<>())
                .genres(new HashSet<>())
                .build();
        film1 = filmStorage.addFilm(film1);
        film2 = filmStorage.addFilm(film2);
        film3 = filmStorage.addFilm(film3);
        Set<Integer> likes2 = new HashSet<>(Arrays.asList(user1.getId()));
        Set<Integer> likes3 = new HashSet<>(Arrays.asList(user1.getId(), user2.getId()));
        film2 = film2.withLikedUsersIds(likes2);
        film3 = film3.withLikedUsersIds(likes3);
        filmStorage.updateFilm(film2);
        filmStorage.updateFilm(film3);
        assertThat(filmStorage.getMostPopularFilms(5)).isEqualTo(List.of(film3, film2, film1));
    }
}
