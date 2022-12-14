package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final HashMap<Integer, User> usersById = new HashMap<>();
    private int id = 0;

    private boolean isUserDataErrors(User user) {
        boolean isEmailErrors = user.getEmail().isBlank() || !user.getEmail().contains("@");
        boolean isLoginErrors = user.getLogin().isBlank() || user.getLogin().contains(" ");
        LocalDate birthday = LocalDate.parse(user.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        boolean isBirthdayErrors = birthday.isAfter(LocalDate.now());
        return isEmailErrors || isLoginErrors || isBirthdayErrors;
    }

    @PostMapping
    public User addUser(@RequestBody User newUser) {
        if (isUserDataErrors(newUser)) {
            log.warn("Пользователь не создан. В запросе указаны не корректные данные");
            throw new ValidationException();
        }
        String name = newUser.getName();
        if (name == null || name.equals("")) {
            name = newUser.getLogin();
        }
        User tmpUser = new User(newUser.getEmail(), newUser.getLogin(), name, newUser.getBirthday());
        tmpUser.setId(++id);
        usersById.put(tmpUser.getId(), tmpUser);
        log.info("Создан новый пользователь: {}", newUser.getName());
        return tmpUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        if (!usersById.containsKey(newUser.getId())) {
            log.warn("Попытка обновления не существующего пользователя");
            throw new NotFoundException();
        }
        if (isUserDataErrors(newUser)) {
            log.warn("Сведения о пользователе {} не изменены. Был передан не корректный запрос", newUser.getLogin());
            throw new ValidationException();
        }
        String name = newUser.getName();
        if (name == null || name.equals("")) {
            name = newUser.getLogin();
        }
        User tmpUser = new User(newUser.getEmail(), newUser.getLogin(), name, newUser.getBirthday());
        tmpUser.setId(newUser.getId());
        usersById.put(tmpUser.getId(), tmpUser);
        log.info("Сведения о пользователе {} успешно обновлены", newUser.toString());
        return tmpUser;
    }

    @GetMapping
    public List<User> getUsers() {
        if (usersById.isEmpty()) {
            log.warn("Запрос пустого списка пользователей");
            throw new NotFoundException();
        }
        return new ArrayList<>(usersById.values());
    }
}
