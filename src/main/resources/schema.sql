CREATE TABLE IF NOT EXISTS users (
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name    VARCHAR(255) NOT NULL,
    email   VARCHAR(512) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name            CHARACTER VARYING(255)   NOT NULL,
    description     CHARACTER VARYING(1000)  NOT NULL,
    available       BOOLEAN                  NOT NULL,
    owner_id        BIGINT                   NOT NULL,
    CONSTRAINT pk_item PRIMARY KEY (id),
    CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES users ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    start_date      TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
    end_date        TIMESTAMP WITHOUT TIME ZONE   NOT NULL,
    item_id         BIGINT                        NOT NULL,
    booker_id       BIGINT                        NOT NULL,
    status          VARCHAR(50)                   NOT NULL,
    CONSTRAINT pk_booking PRIMARY KEY (id),
    CONSTRAINT fk_item FOREIGN KEY (item_id) REFERENCES items ON DELETE CASCADE,
    CONSTRAINT fk_booker FOREIGN KEY (booker_id) REFERENCES users ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    text        VARCHAR(1000)                       NOT NULL,
    item_id     BIGINT                              NOT NULL,
    author_id   BIGINT                              NOT NULL,
    created     TIMESTAMP WITHOUT TIME ZONE         NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id),
    CONSTRAINT fk_item_comment FOREIGN KEY (item_id) REFERENCES items ON DELETE CASCADE,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES users ON DELETE CASCADE
);
