-- Keyspace -------------------------------------------------------------------

CREATE KEYSPACE IF NOT EXISTS uuuurrrrllll
WITH replication = {
    -- 'class': 'NetworkTopologyStrategy', 'replication_factor': '3'
    'class': 'SimpleStrategy', 'replication_factor': '1'
};

CREATE TABLE IF NOT EXISTS uuuurrrrllll.message (
    short_url text,
    channel   text,
    nick      text,
    url       text,
    PRIMARY KEY (url, short_url)
);

CREATE TABLE IF NOT EXISTS uuuurrrrllll.pastes (
    short_code text,
    message    text,
    PRIMARY KEY (short_code)
);
