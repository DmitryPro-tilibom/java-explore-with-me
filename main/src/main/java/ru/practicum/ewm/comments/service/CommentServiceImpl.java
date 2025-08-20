package ru.practicum.ewm.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comments.Comment;
import ru.practicum.ewm.comments.CommentMapper;
import ru.practicum.ewm.comments.CommentRepository;
import ru.practicum.ewm.comments.dto.CommentDto;
import ru.practicum.ewm.comments.dto.NewCommentDto;
import ru.practicum.ewm.events.EventMapper;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.service.EventService;
import ru.practicum.ewm.exceptions.NotFoundException;
import ru.practicum.ewm.exceptions.ValidationException;
import ru.practicum.ewm.requests.service.RequestService;
import ru.practicum.ewm.users.User;
import ru.practicum.ewm.users.UserMapper;
import ru.practicum.ewm.users.dto.UserShortDto;
import ru.practicum.ewm.users.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.ewm.events.model.State.PUBLISHED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;
    private final RequestService requestService;

    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.info("Adding new comment by user ID {} for event ID {}", userId, eventId);
        User author = userService.getUserById(userId);
        Event event = eventService.getEventEntityById(eventId);
        if (event.getState() != PUBLISHED) {
            log.warn("Attempt to comment unpublished event ID {}", eventId);
            throw new ValidationException("Comments are available only for published events.");
        }
        Comment comment = commentRepository.save(CommentMapper.toComment(newCommentDto, author, event));
        UserShortDto userShort = UserMapper.toUserShortDto(author);
        EventShortDto eventShort = EventMapper.toEventShortDto(event,
                requestService.getConfirmedRequestsCountForEvent(eventId));
        log.info("Successfully added comment ID {}", comment.getId());
        return CommentMapper.toCommentDto(comment, userShort, eventShort);
    }

    @Override
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto) {
        log.info("Updating comment ID {} by user ID {} for event ID {}", commentId, userId, eventId);
        User author = userService.getUserById(userId);
        Event event = eventService.getEventEntityById(eventId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> {
            log.warn("Comment ID {} not found", commentId);
            return new NotFoundException("Comment with id=" + commentId + " was not found");
        });
        if (comment.getEvent() != event) {
            log.warn("Comment ID {} doesn't belong to event ID {}", commentId, eventId);
            throw new ValidationException("This comment is for other event.");
        }
        comment.setText(newCommentDto.getText());
        comment.setEdited(LocalDateTime.now());
        UserShortDto userShort = UserMapper.toUserShortDto(author);
        EventShortDto eventShort = EventMapper.toEventShortDto(event,
                requestService.getConfirmedRequestsCountForEvent(eventId));
        log.info("Successfully updated comment ID {}", commentId);
        return CommentMapper.toCommentDto(comment, userShort, eventShort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByAuthor(Long userId, Integer from, Integer size) {
        log.info("Getting comments by author ID {}, from {}, size {}", userId, from, size);
        User author = userService.getUserById(userId);
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, PageRequest.of(from / size, size));
        List<Long> eventIds = comments.stream().map(comment -> comment.getEvent().getId()).collect(Collectors.toList());
        Map<Long, Long> confirmedRequests = requestService.getConfirmedRequestsCountForEvents(eventIds);
        UserShortDto userShort = UserMapper.toUserShortDto(author);
        List<CommentDto> result = new ArrayList<>();
        for (Comment c : comments) {
            Long eventId  = c.getEvent().getId();
            EventShortDto eventShort = EventMapper.toEventShortDto(c.getEvent(), confirmedRequests.get(eventId));
            result.add(CommentMapper.toCommentDto(c, userShort, eventShort));
        }
        log.info("Found {} comments by author ID {}", result.size(), userId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(Long eventId, Integer from, Integer size) {
        log.info("Getting comments for event ID {}, from {}, size {}", eventId, from, size);
        Event event = eventService.getEventEntityById(eventId);
        EventShortDto eventShort = EventMapper.toEventShortDto(event,
                requestService.getConfirmedRequestsCountForEvent(eventId));
        List<CommentDto> result = commentRepository.findAllByEventId(eventId, PageRequest.of(from / size, size))
                .stream()
                .map(c -> CommentMapper.toCommentDto(c, UserMapper.toUserShortDto(c.getAuthor()), eventShort))
                .collect(Collectors.toList());
        log.info("Found {} comments for event ID {}", result.size(), eventId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(Long commentId) {
        log.info("Getting comment by ID {}", commentId);
        Comment comment = checkAndGetComment(commentId);
        UserShortDto userShort = UserMapper.toUserShortDto(comment.getAuthor());
        EventShortDto eventShort = EventMapper.toEventShortDto(comment.getEvent(),
                requestService.getConfirmedRequestsCountForEvent(comment.getEvent().getId()));
        log.info("Successfully retrieved comment ID {}", commentId);
        return CommentMapper.toCommentDto(comment, userShort, eventShort);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        log.info("Deleting comment ID {} by user ID {}", commentId, userId);
        User author = userService.getUserById(userId);
        Comment comment = checkAndGetComment(commentId);
        if (comment.getAuthor() != author) {
            log.warn("User ID {} is not author of comment ID {}", userId, commentId);
            throw new ValidationException("Only author can delete the comment.");
        }
        commentRepository.deleteById(commentId);
        log.info("Successfully deleted comment ID {} by user ID {}", commentId, userId);
    }

    @Override
    public void deleteComment(Long commentId) {
        log.info("Deleting comment ID {}", commentId);
        checkAndGetComment(commentId);
        commentRepository.deleteById(commentId);
        log.info("Successfully deleted comment ID {}", commentId);
    }

    private Comment checkAndGetComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> {
            log.warn("Comment ID {} not found", commentId);
            return new NotFoundException("Comment with id=" + commentId + " was not found");
        });
    }
}