package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userDbStorage;

    public void addFriend(int id, int friendId) {
        User firstFriend = getUserById(id);
        User secondFriend = getUserById(friendId);
        if (firstFriend == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        if (secondFriend == null) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
        Set<Integer> userFriendsIds = firstFriend.getFriendsIds();
        userFriendsIds.add(friendId);
        firstFriend = firstFriend.withFriendsIds(userFriendsIds);
        userDbStorage.updateUser(firstFriend);
    }

    public void removeFriend(int id, int friendId) {
        User firstFriend = getUserById(id);
        User secondFriend = getUserById(friendId);
        if (firstFriend == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        if (secondFriend == null) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
        Set<Integer> userFriendsIds = firstFriend.getFriendsIds();
        userFriendsIds.remove(friendId);
        firstFriend = firstFriend.withFriendsIds(userFriendsIds);
        userDbStorage.updateUser(firstFriend);
    }

    public List<User> getMutualFriends(int id, int friendId) {
        User firstUser = getUserById(id);
        User secondUser = getUserById(friendId);
        if (firstUser == null || secondUser == null) {
            return new ArrayList<>();
        }
        Set<Integer> firstUserFriends = firstUser.getFriendsIds();
        Set<Integer> secondUserFriends = secondUser.getFriendsIds();
        Set<Integer> mutual = new HashSet<>(firstUserFriends);
        mutual.retainAll(secondUserFriends);
        return mutual.stream().map(this::getUserById).collect(Collectors.toList());
    }

    public User getUserById(int id) {
        return userDbStorage.getUserById(id);
    }

    public List<User> getFriends(int id) {
        return userDbStorage.getUserById(id)
                .getFriendsIds()
                .stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public User addUser(User user) {
        if (isUserDataErrors(user)) {
            throw new ValidationException();
        }
        String name = user.getName();
        if (StringUtils.isBlank(name)) {
            name = user.getLogin();
        }
        user = user.withName(name);
        return userDbStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (userDbStorage.getUserById(user.getId()) == null) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        if (isUserDataErrors(user)) {
            throw new ValidationException();
        }
        String name = user.getName();
        if (StringUtils.isBlank(name)) {
            name = user.getLogin();
        }
        user = user.withName(name);
        return userDbStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userDbStorage.getUsers();
    }

    public void removeUser(int id) {
        if (userDbStorage.getUserById(id) == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        userDbStorage.removeUser(id);
    }

    private boolean isUserDataErrors(User user) {
        boolean isLoginErrors = user.getLogin().contains(" ");
        boolean isBirthdayErrors = user.getBirthday().isAfter(LocalDate.now());
        return isLoginErrors || isBirthdayErrors;
    }
}