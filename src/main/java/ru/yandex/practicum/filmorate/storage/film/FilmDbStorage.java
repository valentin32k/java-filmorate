package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        if (film == null || getMpaById(film.getMpa().getId()) == null) {
            throw new ValidationException();
        }
        String sqlGetQuery = "SELECT \"film_id\" " +
                "FROM \"films\" " +
                "WHERE \"name\" = ? " +
                "AND \"description\" = ? " +
                "AND \"release_date\" = ? " +
                "AND \"duration\" = ? " +
                "AND \"mpa_id\" = ?";
        String sqlAddQuery = "INSERT INTO \"films\" (\"name\",\"description\",\"release_date\",\"duration\",\"mpa_id\") " +
                "VALUES (?,?,?,?,?)";
        jdbcTemplate.update(sqlAddQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate().toString(),
                film.getDuration(),
                film.getMpa().getId());
        SqlRowSet films = jdbcTemplate.queryForRowSet(sqlGetQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate().toString(),
                film.getDuration(),
                film.getMpa().getId());
        films.next();
        int id = films.getInt("film_id");
        sqlAddQuery = "INSERT INTO \"film_genres\" (\"film_id\", \"genre_id\") VALUES(?,?)";
        Set<Genre> genres = film.getGenres();
        if (genres == null) {
            genres = new HashSet<>();
        }
        String finalSqlAddQuery = sqlAddQuery;
        genres.forEach(g -> jdbcTemplate.update(finalSqlAddQuery, id, g.getId()));
        return getFilmById(id);
    }

    @Override
    public void removeFilm(int id) {
        String sqlQuery = "DELETE FROM \"liked\" WHERE \"film_id\" = ?";
        jdbcTemplate.update(sqlQuery, id);
        sqlQuery = "DELETE FROM \"film_genres\" WHERE \"film_id\" = ?";
        jdbcTemplate.update(sqlQuery, id);
        sqlQuery = "DELETE FROM \"films\" WHERE \"film_id\" = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public Film updateFilm(Film film) {
        if (film == null || getMpaById(film.getMpa().getId()) == null) {
            throw new ValidationException();
        }
        String sql = "UPDATE \"films\" " +
                "SET \"name\" = ?, " +
                "\"description\" = ?, " +
                "\"release_date\" = ?," +
                "\"duration\" = ?," +
                "\"mpa_id\" = ? " +
                "WHERE \"film_id\" = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate().toString(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        Set<Genre> genres = film.getGenres();
        if (genres == null) {
            genres = new HashSet<>();
        }
        sql = "DELETE FROM \"film_genres\" WHERE \"film_id\" = ?";
        jdbcTemplate.update(sql, film.getId());
        sql = "INSERT INTO \"film_genres\" (\"film_id\", \"genre_id\") VALUES(?,?)";
        String finalSql = sql;
        genres.forEach(g -> jdbcTemplate.update(finalSql, film.getId(), g.getId()));
        Set<Integer> likeUsers = film.getLikedUsersIds();
        if (likeUsers == null) {
            likeUsers = new HashSet<>();
        }
        sql = "DELETE FROM \"liked\" WHERE \"film_id\" = ?";
        jdbcTemplate.update(sql, film.getId());
        sql = "INSERT INTO \"liked\" (\"user_id\", \"film_id\") VALUES(?,?)";
        String finalSql1 = sql;
        likeUsers.forEach(l -> jdbcTemplate.update(finalSql1, l, film.getId()));
        return getFilmById(film.getId());
    }

    @Override
    public Film getFilmById(int id) {
        String sqlQuery = "SELECT * FROM \"films\" WHERE \"film_id\" = ?";
        SqlRowSet filmsSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!filmsSet.next()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        String sql = "SELECT * FROM \"mpa\" WHERE \"mpa_id\" = ?";
        SqlRowSet mpaSet = jdbcTemplate.queryForRowSet(sql, filmsSet.getInt("mpa_id"));
        mpaSet.first();
        Mpa mpa = new Mpa(mpaSet.getInt("mpa_id"),
                mpaSet.getString("name"),
                mpaSet.getString("description"));
        sql = "SELECT \"user_id\" FROM \"liked\" WHERE \"film_id\" = ?";
        Set<Integer> likedUsersIds = new HashSet<>();
        SqlRowSet likeSet = jdbcTemplate.queryForRowSet(sql, id);
        while (likeSet.next()) {
            likedUsersIds.add(likeSet.getInt("user_id"));
        }

        sql = "SELECT \"genre_id\" FROM \"film_genres\" WHERE \"film_id\" = ? ORDER BY \"genre_id\"";
        Set<Genre> genresIds = new HashSet<>();
        SqlRowSet genreSet = jdbcTemplate.queryForRowSet(sql, id);
        while (genreSet.next()) {
            genresIds.add(getGenreById(genreSet.getInt("genre_id")));
        }
        return Film.builder()
                .id(filmsSet.getInt("film_id"))
                .name(filmsSet.getString("name"))
                .description(filmsSet.getString("description"))
                .releaseDate(Objects.requireNonNull(filmsSet.getDate("release_date")).toLocalDate())
                .duration(filmsSet.getLong("duration"))
                .mpa(mpa)
                .likedUsersIds(likedUsersIds)
                .genres(genresIds)
                .build();
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = new ArrayList<>();
        String sqlQuery = "SELECT \"film_id\" FROM \"films\" ORDER BY \"film_id\"";
        SqlRowSet filmsSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (filmsSet.next()) {
            films.add(getFilmById(filmsSet.getInt("film_id")));
        }
        return films;
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        List<Film> films = new ArrayList<>();
        String sqlQuery = "SELECT f.\"film_id\" , COUNT(l.\"like_id\") \n" +
                "FROM PUBLIC.\"films\" f\n" +
                "LEFT JOIN PUBLIC.\"liked\" l ON f.\"film_id\" = l.\"film_id\"\n" +
                "GROUP BY f.\"film_id\" \n" +
                "ORDER BY COUNT(l.\"like_id\") DESC " +
                "LIMIT ?";
        SqlRowSet filmsSet = jdbcTemplate.queryForRowSet(sqlQuery, count);
        while (filmsSet.next()) {
            films.add(getFilmById(filmsSet.getInt("film_id")));
        }
        return films;
    }

    @Override
    public Mpa getMpaById(int id) {
        String sqlQuery = "SELECT * FROM \"mpa\" WHERE \"mpa_id\" = ?";
        SqlRowSet mpaSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!mpaSet.next()) {
            throw new NotFoundException("Возрастное ограничение с id = " + id + " не найдено");
        }
        return new Mpa(mpaSet.getInt("mpa_id"),
                mpaSet.getString("name"),
                mpaSet.getString("description"));
    }

    @Override
    public List<Mpa> getAllMpa() {
        List<Mpa> mpaList = new ArrayList<>();
        String sqlQuery = "SELECT * FROM \"mpa\"";
        SqlRowSet mpaSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (mpaSet.next()) {
            mpaList.add(new Mpa(mpaSet.getInt("mpa_id"),
                    mpaSet.getString("name"),
                    mpaSet.getString("description")));
        }
        return mpaList;
    }

    @Override
    public Genre getGenreById(int id) {
        String sqlQuery = "SELECT * FROM \"genre\" WHERE \"genre_id\" = ?";
        SqlRowSet genreSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!genreSet.next()) {
            throw new NotFoundException("Жанр фильма с id = " + id + " не найден");
        }
        return new Genre(genreSet.getInt("genre_id"),
                genreSet.getString("name"));
    }

    @Override
    public List<Genre> getAllGenres() {
        List<Genre> genres = new ArrayList<>();
        String sqlQuery = "SELECT * FROM \"genre\"";
        SqlRowSet genreSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (genreSet.next()) {
            genres.add(new Genre(genreSet.getInt("genre_id"),
                    genreSet.getString("name")));
        }
        return genres;
    }
}
