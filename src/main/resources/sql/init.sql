CREATE TABLE IF NOT EXISTS `bgm`
(
    `id`            INTEGER PRIMARY KEY AUTOINCREMENT,
    `name`          TEXT    NOT NULL,
    `name_original` TEXT    NOT NULL DEFAULT '',
    `rank`          INTEGER NOT NULL,
    `year`          INTEGER NOT NULL,
    `img_url`       TEXT    NOT NULL DEFAULT '',
    `rate`          REAL    NOT NULL,
    `rate_num`      INTEGER NOT NULL,
    `info`          TEXT    NOT NULL
);

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