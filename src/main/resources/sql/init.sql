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
    `keyword` TEXT    NOT NULL,
    `content` TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS `prompt`
(
    `id`      INTEGER PRIMARY KEY AUTOINCREMENT,
    `name`    TEXT NOT NULL,
    `content` TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS `vip`
(
    `id`          INTEGER PRIMARY KEY AUTOINCREMENT,
    `qq`          INTEGER NOT NULL,
    `free_tokens` INTEGER NOT NULL,
    `pay_tokens`  INTEGER NOT NULL,
    `cost_tokens` INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `bank`
(
    `id`    INTEGER PRIMARY KEY AUTOINCREMENT,
    `qq`    INTEGER NOT NULL,
    `money` INTEGER NOT NULL,
    `coin`  INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS `niuzi`
(
    `id`       INTEGER PRIMARY KEY AUTOINCREMENT,
    `qq`       INTEGER NOT NULL,
    `group_id` INTEGER NOT NULL,
    `length`   INTEGER NOT NULL,
    `is_day`   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `domain_name`
(
    `id`          INTEGER PRIMARY KEY AUTOINCREMENT,
    `domain_name` TEXT    NOT NULL,
    `state`       INTEGER NOT NULL
);
