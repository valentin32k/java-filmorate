package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
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

    @PostMapping
    public User addUser(@Valid @RequestBody User newUser) {
        if (isUserDataErrors(newUser)) {
            log.warn("Пользователь не создан. В запросе указаны не корректные данные");
            throw new ValidationException();
        }
        String name = newUser.getName();
        if (StringUtils.isBlank(name)) {
            name = newUser.getLogin();
        }
        User tmpUser = new User(0, newUser.getEmail(), newUser.getLogin(), name, newUser.getBirthday());
        tmpUser = tmpUser.withId(++id);
        usersById.put(tmpUser.getId(), tmpUser);
        log.info("Создан новый пользователь: {}", newUser.getName());
        return tmpUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        if (!usersById.containsKey(newUser.getId())) {
            log.warn("Попытка обновления не существующего пользователя");
            throw new NotFoundException();
        }
        if (isUserDataErrors(newUser)) {
            log.warn("Сведения о пользователе {} не изменены. Был передан не корректный запрос", newUser.getLogin());
            throw new ValidationException();
        }
        String name = newUser.getName();
        if (StringUtils.isBlank(name)) {
            name = newUser.getLogin();
        }
        User tmpUser = new User(0, newUser.getEmail(), newUser.getLogin(), name, newUser.getBirthday());
        tmpUser = tmpUser.withId(newUser.getId());
        usersById.put(tmpUser.getId(), tmpUser);
        log.info("Сведения о пользователе {} успешно обновлены", newUser.toString());
        return tmpUser;
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(usersById.values());
    }

    private boolean isUserDataErrors(User user) {
        boolean isLoginErrors = user.getLogin().contains(" ");
        LocalDate birthday = LocalDate.parse(user.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        boolean isBirthdayErrors = birthday.isAfter(LocalDate.now());
        return isLoginErrors || isBirthdayErrors;
    }
}
