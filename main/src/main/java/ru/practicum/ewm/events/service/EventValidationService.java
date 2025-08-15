package ru.practicum.ewm.events.service;

import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.locations.Location;

import java.time.LocalDateTime;

public interface EventValidationService {
    void validateEventTime(LocalDateTime eventTime);

    Event validateAndGetEvent(Long eventId);

    Event validateAndGetEvent(Long eventId, Long userId);

    Location validateAndGetLocation(Location location);
}