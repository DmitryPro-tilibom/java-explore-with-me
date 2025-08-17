package ru.practicum.ewm.requests.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.events.model.Event;

import ru.practicum.ewm.users.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "requests")
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    @PastOrPresent
    private LocalDateTime created;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @NotNull
    private Event event;

    @ManyToOne
    @JoinColumn(name = "requester_id",nullable = false)
    @NotNull
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private RequestStatus status;
}