CREATE  TABLE IF NOT EXISTS videos (
	id                   VARCHAR(26)  NOT NULL     PRIMARY KEY,
	`status`             VARCHAR(20)       ,
	unlocked_id          VARCHAR(26)
 ) engine=InnoDB;
