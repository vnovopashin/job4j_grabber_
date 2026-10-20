CREATE TABLE post (
    id SERIAL PRIMARY KEY,
    title TEXT,
    link TEXT UNIQUE,
    description TEXT,
    time TIMESTAMPTZ
);
