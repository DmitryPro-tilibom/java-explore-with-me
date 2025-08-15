DELETE FROM comments;
DELETE FROM users;
DELETE FROM categories;
DELETE FROM locations;
DELETE FROM events;

INSERT INTO users (name, email) VALUES ('user1', 'user1@gmail.com');
INSERT INTO users (name, email) VALUES ('user2', 'user2@gmail.com');

INSERT INTO categories (name) VALUES ('activities');

INSERT INTO locations (lat, lon) VALUES ('55.75', '37.61');

INSERT INTO events (annotation, category_id, created_on, description, event_date, initiator_id, location_id, paid,
                    participant_limit, request_moderation, title)
VALUES ('защита диплома', 1, '2025-08-08 11:00:00', 'перед здоровой аудиторией',
        '2026-04-04 10:00:00', 2, 1, 'false', 0, 'true', 'event1');
INSERT INTO events (annotation, category_id, created_on, description, event_date, initiator_id, location_id, paid,
                    participant_limit, request_moderation, title)
VALUES ('прогулка по Москва', 1, '2025-08-09 12:00:00', 'на автобусе',
        '2026-07-07 11:00:00', 2, 1, 'false', 0, 'true', 'event2');

UPDATE events SET state = 'PUBLISHED' WHERE id = 1;