CREATE TABLE quote (
    id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    author VARCHAR(256) NOT NULL,
    text TEXT NOT NULL,

    PRIMARY KEY (id)
);