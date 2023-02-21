package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Value
@Builder
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
    Set<Genre> genres;

    @JsonIgnore
    public Integer getLikesCount() {
        return likedUsersIds.size();
    }
}
