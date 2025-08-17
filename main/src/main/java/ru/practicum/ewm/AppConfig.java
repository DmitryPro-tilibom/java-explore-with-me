package ru.practicum.ewm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.events.EventRepository;
import ru.practicum.ewm.events.service.EventInfoService;
import ru.practicum.ewm.events.service.EventInfoServiceImpl;

@Configuration
public class AppConfig {
    @Bean
    public EventInfoService eventInfoService(EventRepository eventRepository) {
        return new EventInfoServiceImpl(eventRepository);
    }
}