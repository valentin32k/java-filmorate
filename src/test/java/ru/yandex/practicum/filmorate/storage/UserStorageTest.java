package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserStorageTest {
    private final UserDbStorage userStorage;
    User validUser = new User(0,
            "email@mail.ru",
            "Login",
            "Name",
            LocalDate.parse("1990-01-01"),
            new HashSet<>());

    @Test
    void testAddingValidUser() {
        userStorage.addUser(validUser);
        User returnedUser = userStorage.getUsers().get(0);
        assertThat(returnedUser.getId()).isNotZero();
        assertThat(returnedUser.getEmail()).isEqualTo("email@mail.ru");
        assertThat(returnedUser.getLogin()).isEqualTo("Login");
        assertThat(returnedUser.getName()).isEqualTo("Name");
        assertThat(returnedUser.getBirthday()).isEqualTo(LocalDate.parse("1990-01-01"));
        assertThat(returnedUser.getFriendsIds()).isEmpty();
    }

    @Test
    void testAddingVoidBodyUser() {
        try {
            userStorage.addUser(null);
        } catch (ValidationException ex) {
            assertThat(ex.getMessage()).isEqualTo("Передан запрос с некорректными данными");
        }
    }

    @Test
    void testRemoveUser() {
        User user = userStorage.addUser(validUser);
        int id = user.getId();
        userStorage.removeUser(id);
        try {
            userStorage.getUserById(id);
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("error: Пользователь с id = " + id + " не найден");
        }
    }

    @Test
    void testValidUpdateUser() {
        User user = userStorage.addUser(validUser);
        User newUser = new User(user.getId(),
                "email2@mail.ru",
                "Login2",
                "Name2",
                LocalDate.parse("1992-02-02"),
                new HashSet<>());
        userStorage.updateUser(newUser);
        assertThat(userStorage.getUserById(user.getId())).isEqualTo(newUser);
    }

    @Test
    void testUpdatingVoidBodyUser() {
        try {
            userStorage.updateUser(null);
        } catch (ValidationException ex) {
            assertThat(ex.getMessage()).isEqualTo("Передан запрос с некорректными данными");
        }
    }

    @Test
    void testUpdatingUserWithWrongId() {
        try {
            userStorage.updateUser(validUser);
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("error: Пользователь с id = 0 не найден");
        }
    }

    @Test
    void testFindCorrectUserById() {
        User tmpUser = userStorage.addUser(new User(0,
                "email@mail.ru",
                "Login",
                "Name",
                LocalDate.parse("1990-01-01"),
                new HashSet<>()));
        Optional<User> userOptional = Optional.of(userStorage.getUserById(tmpUser.getId()));
        assertThat(userOptional)//утверждаю, что userOptional
                .isPresent()    //существует
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id", tmpUser.getId()));
    }

    @Test
    void testFindWrongUserById() {
        try {
            userStorage.getUserById(999);
        } catch (NotFoundException ex) {
            assertThat(ex.getMessage()).isEqualTo("error: Пользователь с id = 999 не найден");
        }
    }

    @Test
    void testGetUsers() {
        assertThat(userStorage.getUsers()).isEmpty();
        userStorage.addUser(validUser);
        userStorage.addUser(validUser.withName("Вася"));
        userStorage.addUser(validUser.withName("Петя"));
        assertThat(userStorage.getUsers().size()).isEqualTo(3);
    }
}
