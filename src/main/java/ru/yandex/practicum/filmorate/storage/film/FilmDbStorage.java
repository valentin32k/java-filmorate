package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.GenresDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenresDbStorage genresDbStorage;

    @Override
    public Film addFilm(Film film) {
        if (film == null || mpaDbStorage.getMpaById(film.getMpa().getId()) == null) {
            throw new ValidationException();
        }
        KeyHolder holder = new GeneratedKeyHolder();
        PreparedStatementCreator prepareStatement = connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO films " +
                            "(film_name," +
                            "film_description," +
                            "release_date," +
                            "duration," +
                            "mpa_id) " +
                            "VALUES (?,?,?,?,?)",
                    new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setString(3, film.getReleaseDate().toString());
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        };
        jdbcTemplate.update(prepareStatement, holder);
        int id = Objects.requireNonNull(holder.getKey()).intValue();
        Set<Genre> genres = film.getGenres();
        if (genres == null) {
            genres = new HashSet<>();
        }
        genres.forEach(g -> jdbcTemplate.update("INSERT INTO film_genres " +
                        "(film_id, " +
                        "genre_id) " +
                        "VALUES(?,?)",
                id,
                g.getId()));
        return getFilmById(id);
    }

    @Override
    public void removeFilm(int id) {
        String sqlQuery = "DELETE FROM liked WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
        sqlQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
        sqlQuery = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public Film updateFilm(Film film) {
        if (film == null || mpaDbStorage.getMpaById(film.getMpa().getId()) == null) {
            throw new ValidationException();
        }
        String sql = "UPDATE films " +
                "SET film_name = ?, " +
                "film_description = ?, " +
                "release_date = ?," +
                "duration = ?," +
                "mpa_id = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate().toString(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getId());
        Set<Genre> genres = film.getGenres();
        if (genres == null) {
            genres = new HashSet<>();
        }
        String finalSql = "INSERT INTO film_genres (film_id, genre_id) VALUES(?,?)";
        genres.forEach(g -> jdbcTemplate.update(finalSql, film.getId(), g.getId()));
        sql = "DELETE FROM liked WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getId());
        Set<Integer> likeUsers = film.getLikedUsersIds();
        if (likeUsers == null) {
            likeUsers = new HashSet<>();
        }
        String finalSql1 = "INSERT INTO liked (user_id, film_id) VALUES(?,?)";
        likeUsers.forEach(l -> jdbcTemplate.update(finalSql1, l, film.getId()));
        return getFilmById(film.getId());
    }

    @Override
    public Film getFilmById(int id) {
        String sqlQuery = "SELECT f.FILM_ID, " +
                "f.FILM_NAME, " +
                "f.film_DESCRIPTION, " +
                "f.RELEASE_DATE, " +
                "f.DURATION, " +
                "f.MPA_ID, " +
                "m.MPA_NAME, " +
                "m.mpa_description, " +
                "l.USER_ID, " +
                "fg.GENRE_ID, " +
                "g.GENRE_NAME " +
                "FROM FILMS f " +
                "LEFT JOIN MPA m ON f.MPA_ID = m.MPA_ID " +
                "LEFT JOIN LIKED l ON f.FILM_ID = l.FILM_ID " +
                "LEFT JOIN FILM_GENRES fg ON f.FILM_ID = fg.FILM_ID " +
                "LEFT JOIN GENRE g ON fg.GENRE_ID = g.GENRE_ID " +
                "WHERE f.FILM_ID = ?";
        SqlRowSet filmSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!filmSet.next()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        Mpa mpa = new Mpa(filmSet.getInt("mpa_id"),
                filmSet.getString("mpa_name"),
                filmSet.getString("mpa_description"));
        filmSet.beforeFirst();
        Set<Integer> likedUsersIds = new HashSet<>();
        Set<Genre> tmpGenreSet = new HashSet<>();
        while (filmSet.next()) {
            int userId = filmSet.getInt("user_id");
            int genreId = filmSet.getInt("genre_id");
            if (userId != 0) {
                likedUsersIds.add(userId);
            }
            if (genreId != 0) {
                tmpGenreSet.add(new Genre(filmSet.getInt("genre_id"), filmSet.getString("genre_name")));
            }
        }
        Set<Genre> genreSet = new HashSet<>();
        tmpGenreSet
                .stream()
                .sorted(Comparator.comparing(Genre::getId, Comparator.naturalOrder()))
                .forEach(genreSet::add);
        filmSet.first();
        return Film.builder()
                .id(filmSet.getInt("film_id"))
                .name(filmSet.getString("film_name"))
                .description(filmSet.getString("film_description"))
                .releaseDate(Objects.requireNonNull(filmSet.getDate("release_date")).toLocalDate())
                .duration(filmSet.getLong("duration"))
                .mpa(mpa)
                .likedUsersIds(likedUsersIds)
                .genres(genreSet)
                .build();
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = new ArrayList<>();
        String sqlQuery = "SELECT film_id FROM films ORDER BY film_id";
        SqlRowSet filmsSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (filmsSet.next()) {
            films.add(getFilmById(filmsSet.getInt("film_id")));
        }
        return films;
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        List<Film> films = new ArrayList<>();
        String sqlQuery = "SELECT f.film_id, COUNT(l.user_id) " +
                "FROM films AS f " +
                "LEFT JOIN liked l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        SqlRowSet filmsSet = jdbcTemplate.queryForRowSet(sqlQuery, count);
        while (filmsSet.next()) {
            films.add(getFilmById(filmsSet.getInt("film_id")));
        }
        return films;
    }
}
