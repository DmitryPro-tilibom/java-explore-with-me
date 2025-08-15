package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.locations.LocationDto;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {

    @Size(min = 20, max = 2000)
    @NotBlank
    private String annotation;

    @NotNull
    private Long category;

    @Size(min = 20, max = 7000)
    @NotBlank
    private String description;

    @NotNull
    @Future
    @JsonFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
    private LocalDateTime eventDate;

    @NotNull
    @Valid
    private LocationDto location;

    private boolean paid = false;

    @PositiveOrZero
    private int participantLimit = 0;

    private boolean requestModeration = true;

    @Size(min = 3, max = 120)
    @NotBlank
    private String title;

    public Boolean getPaid() {
        return paid;
    }

    public Boolean getRequestModeration() {
        return requestModeration;
    }
}