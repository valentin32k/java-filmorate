package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;
@Value
public class Film {
    @With
    int id;
    @NotBlank String name;
    @Size(min = 0, max = 200) String description;
    String releaseDate;
    @DecimalMin("0") long duration;
    @With
    Set<Integer> likedUsersIds;

    public Integer getLikesCount() {
        return likedUsersIds.size();
    }

//    Jackson сериализует в том числе результаты getLikesCount(),
//    что мешает при тестировании создавать экземпляры классов Film (напр. метод void addValidFilm()).
//    Поэтому я использовал метод с аннотацией @JsonAnySetter, который вызывается на этапе десериализации
//    для всех параметров, которых нет в конструкторе класса. Наверняка существует более изящное решение, которое я не нашел)))
    @JsonAnySetter
    public void add(String key, String value) {
    }
}
