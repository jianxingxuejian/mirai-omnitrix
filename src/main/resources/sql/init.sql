CREATE TABLE IF NOT EXISTS `character`
(
    `id`          INTEGER PRIMARY KEY AUTOINCREMENT,
    `name`        TEXT NOT NULL,
    `external_id` TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS `live`
(
    `id`       INTEGER PRIMARY KEY AUTOINCREMENT,
    `qq`       INTEGER NOT NULL,
    `group_id` INTEGER NOT NULL,
    `uid`      INTEGER NOT NULL,
    `room_id`  INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `auto_reply`
(
    `id`      INTEGER PRIMARY KEY AUTOINCREMENT,
    `type`    INTEGER NOT NULL,
    `keyword` INTEGER NOT NULL,
    `content` INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `bank`
(
    `id`    INTEGER PRIMARY KEY AUTOINCREMENT,
    `qq`    INTEGER NOT NULL,
    `money` INTEGER NOT NULL,
    `coin`  INTEGER NOT NULL
);
