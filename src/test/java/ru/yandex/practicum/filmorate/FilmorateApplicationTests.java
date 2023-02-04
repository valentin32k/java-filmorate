package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import static org.assertj.core.api.Assertions.*;


import java.util.Optional;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;

    @Test
    void testFindUserById() {
        Optional<User> userOptional = userStorage.getUserById(1);

        assertThat(userOptional)//утверждаю, что userOptional
                .isPresent()    //существует
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("id",1));
    }

}
