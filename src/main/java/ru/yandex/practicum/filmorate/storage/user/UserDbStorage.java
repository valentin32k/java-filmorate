package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User addUser(User user) {
        if (user == null) {
            throw new ValidationException();
        }
        String sqlQuery = "INSERT INTO \"users\" (\"email\",\"login\",\"name\",\"birthday\") " +
                "VALUES (?,?,?,?)";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday().toString());
        sqlQuery = "SELECT \"user_id\" " +
                "FROM \"users\" " +
                "WHERE \"email\" = ? " +
                "AND \"login\" = ? " +
                "AND \"name\" = ? " +
                "AND \"birthday\" = ?";
        SqlRowSet users = jdbcTemplate.queryForRowSet(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday().toString());
        users.last();
        return getUserById(users.getInt("user_id"));
    }

    @Override
    public void removeUser(int id) {
        String sqlQuery = "DELETE FROM \"friendship\" WHERE \"user_id\" = ? OR \"user_friend_id\" = ?";
        jdbcTemplate.update(sqlQuery, id, id);
        sqlQuery = "DELETE FROM \"users\" WHERE \"user_id\" = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public User updateUser(User user) {
        if (user == null || getUserById(user.getId()) == null) {
            throw new ValidationException();
        }
        if (user.getFriendsIds() == null) {
            user = user.withFriendsIds(new HashSet<>());
        }
        String sql = "UPDATE \"users\" " +
                "SET \"email\" = ?, " +
                "\"birthday\" = ?, " +
                "\"login\" = ?, " +
                "\"name\" =? " +
                "WHERE \"user_id\" = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getBirthday().toString(),
                user.getLogin(),
                user.getName(),
                user.getId());
        sql = "DELETE FROM \"friendship\" WHERE \"user_id\" = ?";
        jdbcTemplate.update(sql, user.getId());
        sql = "INSERT INTO \"friendship\" (\"user_id\", \"user_friend_id\") VALUES(?,?)";
        String finalSql = sql;
        User finalUser = user;
        user.getFriendsIds()
                .forEach(i -> {
                    if (getUserById(i) != null) {
                        jdbcTemplate.update(finalSql, finalUser.getId(), i);
                    }
                });
        return user;
    }

    @Override
    public User getUserById(int id) {
        String sqlQuery = "SELECT * FROM \"users\" WHERE \"user_id\" = ?";
        SqlRowSet users = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!users.next()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        Set<Integer> friendsSet = new HashSet<>();
        sqlQuery = "SELECT * FROM \"friendship\" WHERE \"user_id\" = ?";
        SqlRowSet friends = jdbcTemplate.queryForRowSet(sqlQuery, id);
        while (friends.next()) {
            friendsSet.add(friends.getInt("user_friend_id"));
        }
        return new User(id,
                users.getString("email"),
                users.getString("login"),
                users.getString("name"),
                Objects.requireNonNull(users.getDate("birthday")).toLocalDate(),
                friendsSet);
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        String sqlQuery = "SELECT \"user_id\" FROM \"users\"";
        SqlRowSet userSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (userSet.next()) {
            users.add(getUserById(userSet.getInt("user_id")));
        }
        return users;
    }
}
