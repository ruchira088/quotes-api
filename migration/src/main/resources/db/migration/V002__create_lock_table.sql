CREATE TABLE lock (
    id VARCHAR(36) NOT NULL,
    index INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    lock_type VARCHAR(64) NOT NULL,
    released_at TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uc_index_name UNIQUE (index, lock_type)
);