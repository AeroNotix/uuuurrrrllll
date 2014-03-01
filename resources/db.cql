-- Keyspace -------------------------------------------------------------------

CREATE KEYSPACE uuuurrrrllll
WITH replication = {
    -- 'class': 'NetworkTopologyStrategy', 'replication_factor': '3'
    'class': 'SimpleStrategy', 'replication_factor': '1'
};

CREATE TABLE uuuurrrrllll.message (
    short_url text,
    channel   text,
    nick      text,
    url       text,
    PRIMARY KEY (url, short_url)
);