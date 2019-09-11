CREATE TABLE IF NOT EXISTS highscores
    (uuid CHAR(36) NOT NULL,
    distance INT UNSIGNED,
    mapname VARCHAR(40));

CREATE TABLE IF NOT EXISTS namehistory
    (uuid CHAR(36) NOT NULL,
    username VARCHAR(16),
    PRIMARY KEY (uuid));

CREATE TABLE IF NOT EXISTS wins
    (uuid CHAR(36) NOT NULL,
    wins INTEGER,
    PRIMARY KEY (uuid));

CREATE TABLE IF NOT EXISTS tokens
    (uuid CHAR(36) NOT NULL,
    tokens INTEGER,
    PRIMARY KEY (uuid));

CREATE TABLE IF NOT EXISTS mapcolors
    (mapname VARCHAR(40) NOT NULL,
    display TEXT(60),
    PRIMARY KEY (mapname));
