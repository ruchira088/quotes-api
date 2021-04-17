CREATE TABLE lock (
    id VARCHAR(36) NOT NULL,
    index INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    name VARCHAR(64) NOT NULL,
    released_at TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uc_index_name UNIQUE (index, name)
);