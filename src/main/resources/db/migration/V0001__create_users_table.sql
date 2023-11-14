CREATE TABLE users
(
    id         VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255),
    email      VARCHAR(255) NOT NULL,
    imageURL  VARCHAR(255),
    role       VARCHAR(255),
    createdAt TIMESTAMP    NOT NULL,
    updatedAt TIMESTAMP
);