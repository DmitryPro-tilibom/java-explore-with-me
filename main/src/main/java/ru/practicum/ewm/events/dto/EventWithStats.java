package ru.practicum.ewm.events.dto;

import ru.practicum.ewm.events.model.Event;
import lombok.Value;

@Value
public class EventWithStats {
    Event event;
    Long views;
    Long confirmedRequests;
}