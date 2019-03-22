---
--- Users
---
CREATE TABLE IF NOT EXISTS `users` (
	`uid` INTEGER PRIMARY KEY,
	`username` TEXT UNIQUE NOT NULL,
	`password` TEXT NOT NULL
);