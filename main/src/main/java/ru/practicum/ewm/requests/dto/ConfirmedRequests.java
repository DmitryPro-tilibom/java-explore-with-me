package ru.practicum.ewm.requests.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ConfirmedRequests {
    @PositiveOrZero
    private long count;

    @PositiveOrZero
    private Long event;
}