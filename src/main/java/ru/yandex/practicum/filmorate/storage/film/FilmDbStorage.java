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
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;

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
                "mpa_id = ?," +
                "likes_count = ? " +
                "WHERE film_id = ?";
        int likesCount = 0;
        if (film.getLikedUsersIds() != null) {
            likesCount = film.getLikedUsersIds().size();
        }
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate().toString(),
                film.getDuration(),
                film.getMpa().getId(),
                likesCount,
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
        String sqlQuery = "SELECT f.film_id, " +
                "f.film_name, " +
                "f.film_description, " +
                "f.release_date, " +
                "f.duration, " +
                "f.mpa_id, " +
                "f.likes_count," +
                "m.mpa_name, " +
                "m.mpa_description, " +
                "l.user_id, " +
                "fg.genre_id, " +
                "g.genre_name " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN liked l ON f.film_id = l.film_id " +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genre g ON fg.genre_id = g.genre_id " +
                "WHERE f.film_id = ?";
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
                .likesCount(filmSet.getInt("likes_count"))
                .likedUsersIds(likedUsersIds)
                .genres(genreSet)
                .build();
    }

    @Override
    public List<Film> getFilms() {
        String sqlQuery = "SELECT f.film_id, " +
                "f.film_name, " +
                "f.film_description, " +
                "f.release_date, " +
                "f.duration, " +
                "f.mpa_id, " +
                "f.likes_count," +
                "m.mpa_name, " +
                "m.mpa_description, " +
                "l.user_id, " +
                "fg.genre_id, " +
                "g.genre_name " +
                "FROM films f " +
                "LEFT JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN liked l ON f.film_id = l.film_id " +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genre g ON fg.genre_id = g.genre_id";
        SqlRowSet filmSet = jdbcTemplate.queryForRowSet(sqlQuery);
        Map<Integer, Film> filmsById = new HashMap<>();
        while (filmSet.next()) {
            Film currFilm = filmsById.get(filmSet.getInt("film_id"));
            if (currFilm == null) {
                Mpa mpa = new Mpa(filmSet.getInt("mpa_id"),
                        filmSet.getString("mpa_name"),
                        filmSet.getString("mpa_description"));
                currFilm = Film.builder()
                        .id(filmSet.getInt("film_id"))
                        .name(filmSet.getString("film_name"))
                        .description(filmSet.getString("film_description"))
                        .releaseDate(Objects.requireNonNull(filmSet.getDate("release_date")).toLocalDate())
                        .duration(filmSet.getLong("duration"))
                        .mpa(mpa)
                        .likesCount(filmSet.getInt("likes_count"))
                        .likedUsersIds(new HashSet<>())
                        .genres(new HashSet<>())
                        .build();
            }
            int likeUserId = filmSet.getInt("user_id");
            if (likeUserId != 0) {
                Set<Integer> likes = currFilm.getLikedUsersIds();
                likes.add(likeUserId);
                currFilm = currFilm.withLikedUsersIds(likes);
            }
            int genreId = filmSet.getInt("genre_id");
            if (genreId != 0) {
                Genre genre = new Genre(genreId,
                        filmSet.getString("genre_name"));
                Set<Genre> genres = currFilm.getGenres();
                genres.add(genre);
                currFilm = currFilm.withGenres(genres);
            }
            filmsById.put(currFilm.getId(), currFilm);
        }
        return new ArrayList<>(filmsById.values());
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        List<Film> films = new ArrayList<>();
        String sqlQuery = "SELECT film_id " +
                "FROM films " +
                "ORDER BY likes_count " +
                "DESC LIMIT ?";
        SqlRowSet filmsSet = jdbcTemplate.queryForRowSet(sqlQuery, count);
        while (filmsSet.next()) {
            films.add(getFilmById(filmsSet.getInt("film_id")));
        }
        return films;
    }
}
