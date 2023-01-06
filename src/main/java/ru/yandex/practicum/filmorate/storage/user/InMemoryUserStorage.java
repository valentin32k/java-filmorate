package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> usersById = new HashMap<>();
    private int id = 0;

    @Override
    public User addUser(User user) {
        user = user.withId(++id);
//        Не придумал как сделать значение по умолчанию для final-переменной, поэтому добавил строчку ниже
        user = user.withFriendsIds(new HashSet<>());
        usersById.put(id, user);
        return user;
    }

    @Override
    public void removeUser(int id) {
        usersById.remove(id);
    }

    @Override
    public User updateUser(User user) {
//        Не придумал как сделать значение по умолчанию для final-переменной, поэтому добавил этот if()
        if (user.getFriendsIds() == null) {
            user = user.withFriendsIds(new HashSet<>());
        }
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