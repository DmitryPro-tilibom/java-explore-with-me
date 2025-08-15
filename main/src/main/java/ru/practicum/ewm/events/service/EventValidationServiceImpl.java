package ru.practicum.ewm.events.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ru.practicum.ewm.events.EventRepository;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.locations.Location;
import ru.practicum.ewm.locations.LocationRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventValidationServiceImpl implements EventValidationService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;

    @Override
    public void validateEventTime(LocalDateTime eventTime) {
        log.info("Validating event time: {}", eventTime);
        if (eventTime.isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Event time validation failed: time must be at least 2 hours from now");
            throw new ValidationException("Event time must be at least 2 hours from now");
        }
        log.debug("Event time validation passed");
    }

    @Override
    public Event validateAndGetEvent(Long eventId) {
        log.info("Validating and getting event by ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new NotFoundException("Event with id=" + eventId + " was not found");
                });
        log.debug("Event validation passed for ID: {}", eventId);
        return event;
    }

    @Override
    public Event validateAndGetEvent(Long eventId, Long userId) {
        log.info("Validating and getting event ID: {} for user ID: {}", eventId, userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {} for user ID: {}", eventId, userId);
                    return new NotFoundException("Event with id=" + eventId + " was not found");
                });
        log.debug("Event validation passed for ID: {} and user ID: {}", eventId, userId);
        return event;
    }

    @Override
    public Location validateAndGetLocation(Location location) {
        log.info("Validating and getting location: lat={}, lon={}", location.getLat(), location.getLon());
        if (locationRepository.existsByLatAndLon(location.getLat(), location.getLon())) {
            Location existing = locationRepository.findByLatAndLon(location.getLat(), location.getLon());
            log.debug("Location already exists with ID: {}", existing.getId());
            return existing;
        }
        Location saved = locationRepository.save(location);
        log.info("New location saved with ID: {}", saved.getId());
        return saved;
    }
}