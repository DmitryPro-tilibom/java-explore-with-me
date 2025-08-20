package ru.practicum.ewm.comments.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.service.CommentService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentControllerPublic {
    private final CommentService commentService;

    @GetMapping("/event/{eventId}")
    List<CommentDto> getComments(@PathVariable Long eventId,
                                 @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                 @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Requesting comments for event {} with pagination (from={}, size={})", eventId, from, size);
        List<CommentDto> result = commentService.getComments(eventId, from, size);
        log.info("Found {} comments for event {}", result.size(), eventId);
        return result;
    }

    @GetMapping("/{commentId}")
    CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("Requesting comment with ID: {}", commentId);
        CommentDto result = commentService.getCommentById(commentId);
        log.info("Successfully retrieved comment {} for event {}", commentId, result.getEvent().getId());
        return result;
    }
}