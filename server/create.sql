---
--- Users
---
CREATE TABLE IF NOT EXISTS `users` (
	`uid` INTEGER PRIMARY KEY,
	`username` TEXT UNIQUE NOT NULL,
	`password` TEXT NOT NULL,
	`pub_key` TEXT NOT NULL,
	`enc_priv_key` TEXT NOT NULL
);

---
--- Albums
---
CREATE TABLE IF NOT EXISTS `albums` (
	`aid` INTEGER PRIMARY KEY,
	`name` TEXT UNIQUE NOT NULL,
	`owner_id` INTEGER,
	FOREIGN KEY(`owner_id`) REFERENCES `users`(`uid`)
);

---
--- Album Slices
---
CREATE TABLE IF NOT EXISTS `album_slices` (
	`aid` INTEGER,
	`uid` INTEGER,
	`url` TEXT UNIQUE NOT NULL,
	`key` TEXT NOT NULL,
	FOREIGN KEY(`aid`) REFERENCES `albums`(`aid`),
	FOREIGN KEY(`uid`) REFERENCES `users`(`uid`),
	PRIMARY KEY(`aid`, `uid`)
);