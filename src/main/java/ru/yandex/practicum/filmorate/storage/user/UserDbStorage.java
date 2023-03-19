package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
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
        KeyHolder holder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatement = con -> {
            PreparedStatement ps = con.prepareStatement("INSERT INTO users " +
                            "(email," +
                            "login," +
                            "user_name," +
                            "birthday) " +
                            "VALUES (?,?,?,?)",
                    new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setString(4, user.getBirthday().toString());
            return ps;
        };
        jdbcTemplate.update(preparedStatement, holder);
        return getUserById(Objects.requireNonNull(holder.getKey()).intValue());
    }

    @Override
    public void removeUser(int id) {
        String sqlQuery = "DELETE FROM friendship WHERE user_id = ? OR user_friend_id = ?";
        jdbcTemplate.update(sqlQuery, id, id);
        sqlQuery = "DELETE FROM users WHERE user_id = ?";
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
        String sql = "UPDATE users " +
                "SET email = ?, " +
                "birthday = ?, " +
                "login = ?, " +
                "user_name =? " +
                "WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getBirthday().toString(),
                user.getLogin(),
                user.getName(),
                user.getId());
        sql = "DELETE FROM friendship WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getId());
        sql = "INSERT INTO friendship (user_id, user_friend_id) VALUES(?,?)";
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
        String sqlQuery = "SELECT u.user_id," +
                "u.email," +
                "u.login," +
                "u.user_name," +
                "u.birthday," +
                "f.user_friend_id " +
                "FROM users u " +
                "LEFT JOIN friendship f ON u.user_id = f.user_id " +
                "WHERE u.user_id = ?";
        SqlRowSet users = jdbcTemplate.queryForRowSet(sqlQuery, id);
        if (!users.next()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        users.beforeFirst();
        Set<Integer> friendsSet = new HashSet<>();
        while (users.next()) {
            int friendId = users.getInt("user_friend_id");
            if (friendId != 0) {
                friendsSet.add(friendId);
            }
        }
        users.first();
        return new User(id,
                users.getString("email"),
                users.getString("login"),
                users.getString("user_name"),
                Objects.requireNonNull(users.getDate("birthday")).toLocalDate(),
                friendsSet);
    }

    @Override
    public List<User> getUsers() {
        String sqlQuery = "SELECT u.user_id," +
                "u.email," +
                "u.login," +
                "u.user_name," +
                "u.birthday," +
                "f.user_friend_id " +
                "FROM users u " +
                "LEFT JOIN friendship f ON u.user_id = f.user_id";
        SqlRowSet users = jdbcTemplate.queryForRowSet(sqlQuery);
        Map<Integer, User> usersById = new HashMap<>();
        while (users.next()) {
            User currUser = usersById.get(users.getInt("user_id"));
            if (currUser == null) {
                currUser = new User(users.getInt("user_id"),
                        users.getString("email"),
                        users.getString("login"),
                        users.getString("user_name"),
                        Objects.requireNonNull(users.getDate("birthday")).toLocalDate(),
                        new HashSet<>());
            }
            int friendId = users.getInt("user_friend_id");
            if (friendId != 0) {
                Set<Integer> friends = currUser.getFriendsIds();
                friends.add(friendId);
                currUser = currUser.withFriendsIds(friends);
            }
            usersById.put(currUser.getId(), currUser);
        }
        return new ArrayList<>(usersById.values());
    }
}
