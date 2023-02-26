package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenresDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Genre getGenreById(int id) {
        String sqlQuery = "SELECT * FROM genre WHERE genre_id = ?";
        SqlRowSet genreSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!genreSet.next()) {
            throw new NotFoundException("Жанр фильма с id = " + id + " не найден");
        }
        return new Genre(genreSet.getInt("genre_id"),
                genreSet.getString("genre_name"));
    }

    public List<Genre> getAllGenres() {
        List<Genre> genres = new ArrayList<>();
        String sqlQuery = "SELECT * FROM genre";
        SqlRowSet genreSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (genreSet.next()) {
            genres.add(new Genre(genreSet.getInt("genre_id"),
                    genreSet.getString("genre_name")));
        }
        return genres;
    }
}
