package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> usersById = new HashMap<>();
    private int id = 0;

    @Override
    public User addUser(User user) {
        user = user.withId(++id);
        usersById.put(id, user);
        return user;
    }

    @Override
    public void removeUser(int id) {
        usersById.remove(id);
    }

    @Override
    public User updateUser(User user) {
        usersById.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(int id) {
        return usersById.get(id);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(usersById.values());
    }
}
