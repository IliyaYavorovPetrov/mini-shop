CREATE TABLE users
(
    id         VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255),
    email      VARCHAR(255) NOT NULL,
    image_url  VARCHAR(255),
    role       VARCHAR(255),
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP
);