package ru.practicum.ewm.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.ewm.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ApiError {

    private final List<String> errors;
    private final String message;
    private final String reason;
    private final String status;
    @JsonFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
    private final LocalDateTime timestamp;
}