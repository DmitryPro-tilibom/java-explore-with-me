package ru.practicum.ewm.events.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.practicum.ewm.locations.LocationDto;
import ru.practicum.ewm.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000)
    private String annotation;

    private  Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @Future
    @JsonFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
    private  LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private  Boolean requestModeration;

    private String stateAction;

    @Size(min = 3, max = 120)
    private String title;
}