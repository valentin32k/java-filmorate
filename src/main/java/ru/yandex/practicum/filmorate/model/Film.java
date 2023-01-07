package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    public Integer getLikesCount() {
        return likedUsersIds.size();
    }
}
