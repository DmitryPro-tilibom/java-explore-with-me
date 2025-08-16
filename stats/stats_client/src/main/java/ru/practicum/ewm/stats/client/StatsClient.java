package ru.practicum.ewm.stats.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class StatsClient extends BaseClient {

    @Value("${client.url}")
    private String serverUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(EndpointHitDto.DATE_TIME_PATTERN);

    public ResponseEntity<Object> saveHit(EndpointHitDto hit) {
        return post(serverUrl + "/hit", hit);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end,
                                           List<String> uris, Boolean unique) {
        Objects.requireNonNull(start, "Start date cannot be null");
        Objects.requireNonNull(end, "End date cannot be null");

        StringBuilder url = new StringBuilder(serverUrl)
                .append("/stats?start=")
                .append(start.format(formatter))
                .append("&end=")
                .append(end.format(formatter));

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> url.append("&uris=").append(uri));
        }

        if (unique != null) {
            url.append("&unique=").append(unique);
        }

        return get(url.toString());
    }
}