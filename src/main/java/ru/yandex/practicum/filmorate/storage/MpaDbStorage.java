package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Mpa getMpaById(int id) {
        String sqlQuery = "SELECT * FROM mpa WHERE mpa_id = ?";
        SqlRowSet mpaSet = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!mpaSet.next()) {
            throw new NotFoundException("Возрастное ограничение с id = " + id + " не найдено");
        }
        return new Mpa(mpaSet.getInt("mpa_id"),
                mpaSet.getString("mpa_name"),
                mpaSet.getString("mpa_description"));
    }

    public List<Mpa> getAllMpa() {
        List<Mpa> mpaList = new ArrayList<>();
        String sqlQuery = "SELECT * FROM mpa";
        SqlRowSet mpaSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (mpaSet.next()) {
            mpaList.add(new Mpa(mpaSet.getInt("mpa_id"),
                    mpaSet.getString("mpa_name"),
                    mpaSet.getString("mpa_description")));
        }
        return mpaList;
    }
}
