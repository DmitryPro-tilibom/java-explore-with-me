package ru.practicum.ewm.requests.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.requests.model.RequestStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    private Long id;

    @JsonFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private RequestStatus status;
}