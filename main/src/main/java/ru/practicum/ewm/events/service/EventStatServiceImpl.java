package ru.practicum.ewm.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.events.EventMapper;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStatServiceImpl implements EventStatService {
    private final StatsClient statClient;
    private final ObjectMapper objectMapper;

    @Value("${app}")
    private String app;

    @Override
    public List<EventFullDtoWithViews> addViewsToEvents(List<Event> events, Map<Long, Long> confirmedRequests) {
        log.info("Starting to add views to {} events", events.size());

        if (events.isEmpty()) {
            log.warn("Empty events list provided for views calculation");
            return Collections.emptyList();
        }

        List<String> uris = events.stream()
                .map(event -> {
                    String uri = String.format("/events/%s", event.getId());
                    log.trace("Preparing URI for event {}: {}", event.getId(), uri);
                    return uri;
                })
                .collect(Collectors.toList());

        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> {
                    log.error("Could not determine start time for events");
                    return new NotFoundException("Start time was not found");
                });

        log.debug("Fetching stats from {} to now for URIs: {}", start, uris);
        ResponseEntity<Object> response = statClient.getStats(start, LocalDateTime.now(), uris, true);

        List<ViewStats> statsDto;
        try {
            statsDto = objectMapper.convertValue(response.getBody(), new TypeReference<>() {});
            log.debug("Received {} stats records", statsDto.size());
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse stats response", e);
            throw new ValidationException("Failed to parse stats response");
        }

        List<EventFullDtoWithViews> result = events.stream()
                .map(event -> {
                    long views = statsDto.stream()
                            .filter(stats -> stats.getUri().equals("/events/" + event.getId()))
                            .findFirst()
                            .map(ViewStats::getHits)
                            .orElse(0L);

                    long confirmed = confirmedRequests.getOrDefault(event.getId(), 0L);
                    log.trace("Event ID: {} - views: {}, confirmed requests: {}",
                            event.getId(), views, confirmed);

                    return EventMapper.toEventFullDtoWithViews(event, views, confirmed);
                })
                .collect(Collectors.toList());

        log.info("Successfully added views to {} events", result.size());
        return result;
    }

    @Override
    public EventFullDtoWithViews addViewsToEvent(Event event, Long confirmedRequests) {
        log.info("Adding views to event ID: {}", event.getId());

        String uri = "/events/" + event.getId();
        log.debug("Fetching stats for URI: {}", uri);

        ResponseEntity<Object> response = statClient.getStats(
                event.getCreatedOn(),
                LocalDateTime.now(),
                List.of(uri),
                true
        );

        List<ViewStats> statsDto;
        try {
            statsDto = objectMapper.convertValue(response.getBody(), new TypeReference<>() {});
            log.debug("Received stats response for event {}: {}", event.getId(), statsDto);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse stats response for event {}", event.getId(), e);
            throw new ValidationException("Failed to parse stats response");
        }

        long views = statsDto.isEmpty() ? 0L : statsDto.get(0).getHits();
        log.debug("Event ID: {} has {} views and {} confirmed requests",
                event.getId(), views, confirmedRequests);

        return EventMapper.toEventFullDtoWithViews(event, views, confirmedRequests);
    }

    @Override
    public void saveHit(HttpServletRequest request) {
        log.info("Saving hit for URI: {} from IP: {}",
                request.getRequestURI(), request.getRemoteAddr());

        EndpointHitDto hit = new EndpointHitDto(
                app,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now()
        );

        try {
            statClient.saveHit(hit);
            log.debug("Successfully saved hit for URI: {}", request.getRequestURI());
        } catch (Exception e) {
            log.error("Failed to save hit for URI: {}", request.getRequestURI(), e);
            throw new RuntimeException("Failed to save hit", e);
        }
    }
}