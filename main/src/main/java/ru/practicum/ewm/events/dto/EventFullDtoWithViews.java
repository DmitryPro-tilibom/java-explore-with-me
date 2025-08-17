package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.locations.LocationDto;
import ru.practicum.ewm.users.dto.UserShortDto;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDtoWithViews {
    private   Long id;

    private  String annotation;

    private  CategoryDto category;

    private  Long confirmedRequests;

    @JsonFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
    private  LocalDateTime createdOn;

    private  String description;

    @JsonFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
    private   LocalDateTime eventDate;

    private   UserShortDto initiator;

    private  LocationDto location;

    private  boolean paid;

    private  Integer participantLimit;

    @JsonFormat(pattern = EndpointHitDto.DATE_TIME_PATTERN)
    private  LocalDateTime publishedOn;

    private  boolean requestModeration;

    private  State state;

    private  String title;

    private  Long views;
}