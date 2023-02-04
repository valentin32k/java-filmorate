package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film addFilm(Film film) {
        System.out.println(film);
        String sqlQuery = "INSERT INTO PUBLIC.\"films\" (\"name\",\"description\",\"release_date\",\"duration\",\"mpa_id\") VALUES (?,?,?,?,?)";
        jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getReleaseDate().toString(), film.getDuration(), film.getMpa().getId());
        sqlQuery = "SELECT \"film_id\" FROM PUBLIC.\"films\" WHERE \"name\" = ? AND \"description\" = ? AND \"release_date\" = ? AND \"duration\" = ? AND \"mpa_id\" = ?";
        SqlRowSet films = jdbcTemplate.queryForRowSet(sqlQuery, film.getName(), film.getDescription(), film.getReleaseDate().toString(), film.getDuration(), film.getMpa().getId());
        films.last();
        return film.withId(films.getInt("film_id"));
    }

    @Override
    public void removeFilm(int id) {

    }

    @Override
    public Film updateFilm(Film film) {
        if (film == null) {
            return null;
        }
        String sql = "UPDATE PUBLIC.\"films\" SET \"name\"=?, \"description\"=?, \"release_date\"=?,\"duration\"=?,\"mpa_id\" = ? WHERE \"film_id\"=?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate().toString(), film.getDuration(), film.getMpa().getId(), film.getId());
        sql = "DELETE FROM \"film_genres\" WHERE \"film_id\" = ?";
        jdbcTemplate.update(sql, film.getId());
        sql = "INSERT INTO PUBLIC.\"film_genres\" (\"film_id\", \"genre_id\") VALUES(?,?)";
        Set<Integer> genres = film.getGenres();
        if (genres == null) {
            genres = new HashSet<>();
        }
        for (Integer genreId : genres) {
            jdbcTemplate.update(sql, film.getId(), genreId);
        }
        sql = "DELETE FROM \"liked\" WHERE \"film_id\" = ?";
        jdbcTemplate.update(sql, film.getId());
        sql = "INSERT INTO PUBLIC.\"liked\" (\"user_id\", \"film_id\") VALUES(?,?)";
        Set<Integer> likeUsers = film.getLikedUsersIds();
        if (likeUsers == null) {
            likeUsers = new HashSet<>();
        }
        for (Integer userId : likeUsers) {
            jdbcTemplate.update(sql, userId, film.getId());
        }
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        String sqlQuery = "SELECT * FROM PUBLIC.\"films\" WHERE \"film_id\" = ?";
        SqlRowSet filmsSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (filmsSet.next()) {

            String sql = "SELECT * FROM PUBLIC.\"mpa\" WHERE \"mpa_id\" = ?";
            SqlRowSet mpaSet = jdbcTemplate.queryForRowSet(sql, filmsSet.getInt("mpa_id"));
            mpaSet.first();
            Mpa mpa = new Mpa(mpaSet.getInt("mpa_id"),
                    mpaSet.getString("name"),
                    mpaSet.getString("description"));

            Set<Integer> likedUsersIds = new HashSet<>();
            sql = "SELECT \"user_id\" FROM PUBLIC.\"liked\" WHERE \"film_id\" = ?";
            SqlRowSet likeSet = jdbcTemplate.queryForRowSet(sql, id);
            while (likeSet.next()) {
                likedUsersIds.add(likeSet.getInt("user_id"));
            }

            Set<Integer> genresIds = new HashSet<>();
            sql = "SELECT \"genre_id\" FROM PUBLIC.\"film_genres\" WHERE \"film_id\" = ?";
            SqlRowSet genreSet = jdbcTemplate.queryForRowSet(sql, id);
            while (genreSet.next()) {
                genresIds.add(genreSet.getInt("genre_id"));
            }
            Film film = new Film(filmsSet.getInt("film_id"),
                    filmsSet.getString("name"),
                    filmsSet.getString("description"),
                    Objects.requireNonNull(filmsSet.getDate("release_date")).toLocalDate(),
                    filmsSet.getLong("duration"),
                    mpa, likedUsersIds, genresIds);
            return film;
        } else {
            return null;
        }
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = new ArrayList<>();
        String sqlQuery = "SELECT \"film_id\" FROM PUBLIC.\"films\"";
        SqlRowSet filmsSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (filmsSet.next()) {
            films.add(getFilmById(filmsSet.getInt("film_id")));
        }
        return films;
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
//        List<Film> films = getFilms();
//        return films
//                .stream()
//                .sorted(filmComparator)
//                .limit(count)
//                .collect(Collectors.toList());
        return null;
    }
}
