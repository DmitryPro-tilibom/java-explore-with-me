package ru.practicum.ewm.events.model;

import lombok.Builder;
import ru.practicum.ewm.categories.Category;
import ru.practicum.ewm.locations.Location;
import ru.practicum.ewm.users.User;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Builder
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    @NotBlank
    private String annotation;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "created_on", nullable = false, columnDefinition = "TIMESTAMP")
    @NotNull
    private LocalDateTime createdOn;

    @Column(nullable = false, length = 7000)
    @Size(min = 20, max = 7000)
    @NotBlank
    private String description;

    @Column(name = "event_date", nullable = false)
    @NotNull
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    @NotNull
    private User initiator;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    @NotNull
    private Location location;

    private Boolean paid;

    @Column(name = "participant_limit")
    @PositiveOrZero
    private  Integer participantLimit;

    @Column(name = "published_on")
    private  LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private  State state;

    @Column(nullable = false, length = 120)
    @NotBlank
    @Size(min = 3, max = 120)
    private  String title;
}