-- Keyspace -------------------------------------------------------------------

CREATE KEYSPACE uuuurrrrllll
WITH replication = {
    -- 'class': 'NetworkTopologyStrategy', 'replication_factor': '3'
    'class': 'SimpleStrategy', 'replication_factor': '1'
};

CREATE TABLE uuuurrrrllll.message (
    channel text,
    nick    text,
    url     text,
    PRIMARY KEY (url)
);