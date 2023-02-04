package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import lombok.With;
import org.springframework.data.relational.core.sql.In;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
@Value
public class Film {
    @With
    int id;
    @NotBlank String name;
    @Size(min = 0, max = 200) String description;
    LocalDate releaseDate;
    @DecimalMin("0") long duration;
    Mpa mpa;
    @With
    Set<Integer> likedUsersIds;
    Set<Integer> genres;
    @JsonIgnore
    public Integer getLikesCount() {
        return likedUsersIds.size();
    }
}
