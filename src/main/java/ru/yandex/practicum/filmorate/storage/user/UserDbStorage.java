package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User addUser(User user) {
        String sqlQuery = "INSERT INTO PUBLIC.\"users\" (\"email\",\"login\",\"name\",\"birthday\") VALUES (?,?,?,?)";
        jdbcTemplate.update(sqlQuery, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday().toString());
        sqlQuery = "SELECT \"user_id\" " + "FROM PUBLIC.\"users\"\n" + "\tWHERE \"email\" = ? " + "AND \"login\" = ? " + "AND \"name\" = ? " + "AND \"birthday\" = ?";
        SqlRowSet users = jdbcTemplate.queryForRowSet(sqlQuery, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday().toString());
        users.last();
        return user.withId(users.getInt("user_id"));
    }

    @Override
    public void removeUser(int id) {
        String sqlQuery = "DELETE FROM PUBLIC.\"users\" WHERE \"user_id\" = ?";
        jdbcTemplate.update(sqlQuery, id);
        sqlQuery = "DELETE FROM PUBLIC.\"friendship\" WHERE \"user_id\" = ? OR \"user_friend_id\" = ?";
        jdbcTemplate.update(sqlQuery, id, id);
    }

    @Override
    public User updateUser(User user) {
        if (user == null) {
            return null;
        }
        if (user.getFriendsIds() == null) {
            user = user.withFriendsIds(new HashSet<>());
        }
        String sql = "UPDATE PUBLIC.\"users\"\n" + "\tSET \"email\"=?,\"birthday\"=?,\"login\"=?,\"name\"=?\n" + "\tWHERE \"user_id\"=?;";
        jdbcTemplate.update(sql, user.getEmail(), user.getBirthday().toString(), user.getLogin(), user.getName(), user.getId());
        sql = "DELETE FROM \"friendship\" WHERE \"user_id\" = ?";
        jdbcTemplate.update(sql, user.getId());
        sql = "INSERT INTO PUBLIC.\"friendship\" (\"user_id\", \"user_friend_id\") VALUES(?,?)";
        for (Integer friendId : user.getFriendsIds()) {
            if (getUserById(friendId) != null) {
                jdbcTemplate.update(sql, user.getId(), friendId);
            }
        }
        return user;
    }

    @Override
    public User getUserById(int id) {
        String sqlQuery = "SELECT * FROM PUBLIC.\"users\" WHERE \"user_id\" = ?";
        SqlRowSet users = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (users.next()) {
            Set<Integer> friendsSet = new HashSet<>();
            sqlQuery = "SELECT * FROM PUBLIC.\"friendship\" WHERE \"user_id\" = ?";
            SqlRowSet friends = jdbcTemplate.queryForRowSet(sqlQuery, id);
            while (friends.next()) {
                friendsSet.add(friends.getInt("user_friend_id"));
            }
            return new User(id, users.getString("email"), users.getString("login"), users.getString("name"), Objects.requireNonNull(users.getDate("birthday")).toLocalDate(), friendsSet);

        } else {
            return null;
        }
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        String sqlQuery = "SELECT * FROM PUBLIC.\"users\"";
        SqlRowSet userSet = jdbcTemplate.queryForRowSet(sqlQuery);
        while (userSet.next()) {
            users.add(new User(userSet.getInt("user_id"), userSet.getString("email"), userSet.getString("login"), userSet.getString("name"), Objects.requireNonNull(userSet.getDate("birthday")).toLocalDate(), new HashSet<>()));
        }
        return users;
    }
}
