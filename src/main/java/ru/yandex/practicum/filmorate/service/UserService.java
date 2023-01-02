package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(int id, int friendId) {
        if (getUserById(id) == null || getUserById(friendId) == null) {
            throw new NotFoundException();
        }
        User firstFriend = getUserById(id);
        User secondFriend = getUserById(friendId);
        Set<Integer> userFriendsIds = firstFriend.getFriendsIds();
        if (userFriendsIds == null) {
            userFriendsIds = new HashSet<>();
        }
        userFriendsIds.add(friendId);
        firstFriend = firstFriend.withFriendsIds(userFriendsIds);
        userFriendsIds = secondFriend.getFriendsIds();
        if (userFriendsIds == null) {
            userFriendsIds = new HashSet<>();
        }
        userFriendsIds.add(id);
        secondFriend = secondFriend.withFriendsIds(userFriendsIds);
        userStorage.updateUser(firstFriend);
        userStorage.updateUser(secondFriend);
    }

    public void removeFriend(int id, int friendId) {
        User firstFriend = getUserById(id);
        User secondFriend = getUserById(friendId);
        if (firstFriend == null || secondFriend == null) {
            throw new NotFoundException();
        }
        Set<Integer> userFriendsIds = firstFriend.getFriendsIds();
        userFriendsIds.remove(friendId);
        firstFriend = firstFriend.withFriendsIds(userFriendsIds);
        userFriendsIds = secondFriend.getFriendsIds();
        userFriendsIds.remove(id);
        secondFriend = secondFriend.withFriendsIds(userFriendsIds);
        userStorage.updateUser(firstFriend);
        userStorage.updateUser(secondFriend);
    }

    public List<User> getMutualFriends(int id, int friendId) {
        User firstUser = getUserById(id);
        User secondUser = getUserById(friendId);
        if (firstUser == null || secondUser == null) {
            return new ArrayList<>();
        }
        Set<Integer> firstUserFriends = firstUser.getFriendsIds();
        Set<Integer> secondUserFriends = secondUser.getFriendsIds();
        if (firstUserFriends == null || secondUserFriends == null) {
            return new ArrayList<>();
        }
        Set<Integer> mutual = new HashSet<>(firstUserFriends);
        mutual.retainAll(secondUserFriends);

        return mutual.stream().map(this::getUserById).collect(Collectors.toList());
    }

    public User getUserById(int id) {
        if (userStorage.getUserById(id) == null) {
            throw new NotFoundException();
        }
        return userStorage.getUserById(id);
    }

    public List<User> getFriends(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException();
        }
        return user.getFriendsIds().stream().map(this::getUserById).collect(Collectors.toList());
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
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (userStorage.getUserById(user.getId()) == null) {
            throw new NotFoundException();
        }
        if (isUserDataErrors(user)) {
            throw new ValidationException();
        }
        String name = user.getName();
        if (StringUtils.isBlank(name)) {
            name = user.getLogin();
        }
        user = user.withName(name);
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void removeUser(int id) {
        if (userStorage.getUserById(id) != null) {
            userStorage.removeUser(id);
        }
    }

    private boolean isUserDataErrors(User user) {
        boolean isLoginErrors = user.getLogin().contains(" ");
        LocalDate birthday = LocalDate.parse(user.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        boolean isBirthdayErrors = birthday.isAfter(LocalDate.now());
        return isLoginErrors || isBirthdayErrors;
    }
}