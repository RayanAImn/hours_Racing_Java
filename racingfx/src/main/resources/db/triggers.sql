CREATE TABLE IF NOT EXISTS old_info (
  horseId VARCHAR(15) NOT NULL,
  horseName VARCHAR(15) NOT NULL,
  age INT,
  gender CHAR(1),
  registration INT NOT NULL,
  stableId VARCHAR(30),
  archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (horseId, archived_at)
);

DROP TRIGGER IF EXISTS trg_horse_archive;
DELIMITER $$
CREATE TRIGGER trg_horse_archive
AFTER DELETE ON Horse
FOR EACH ROW
BEGIN
  INSERT INTO old_info (horseId, horseName, age, gender, registration, stableId, archived_at)
  VALUES (OLD.horseId, OLD.horseName, OLD.age, OLD.gender, OLD.registration, OLD.stableId, NOW());
END$$
DELIMITER ;
