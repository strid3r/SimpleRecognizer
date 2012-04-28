// FOREIGN KEY
PRAGMA foreign_keys=ON;

// android_metadata
CREATE TABLE IF NOT EXISTS "android_metadata" (
	"locale" TEXT DEFAULT 'en_US'
);

INSERT INTO "android_metadata"
	VALUES ('en_US');

//--------------------------------------------------------------------------------------------------------//

// SimpleRecognizer.db
DROP TABLE IF EXISTS PHash;
DROP TABLE IF EXISTS Item;
DROP TABLE IF EXISTS Course;
DROP TRIGGER IF EXISTS fk_PHashItem_item_id;
DROP TRIGGER IF EXISTS fk_ItemCourse_course_id;

CREATE TABLE Course (
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	title TEXT NOT NULL,
	category TEXT,
	creator TEXT
);

CREATE TABLE Item (
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	title TEXT NOT NULL,
	content TEXT NOT NULL,
	course_id INTEGER NOT NULL,
	FOREIGN KEY (course_id) REFERENCES Course(_id) ON DELETE CASCADE
);

CREATE TABLE PHash (
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	hex_value TEXT NOT NULL,
	comment TEXT,
	item_id INTEGER NOT NULL,
	FOREIGN KEY (item_id) REFERENCES Item(_id) ON DELETE CASCADE
);

CREATE TRIGGER fk_ItemCourse_course_id
	BEFORE INSERT ON Item
	FOR EACH ROW BEGIN
		SELECT CASE WHEN ((SELECT _id FROM Course WHERE _id=new.course_id) IS NULL)
		THEN RAISE (ABORT, 'Foreign Key Violation') END;
	END;

CREATE TRIGGER fk_PHashItem_item_id
	BEFORE INSERT ON PHash
	FOR EACH ROW BEGIN
		SELECT CASE WHEN ((SELECT _id FROM Item WHERE _id=new.item_id) IS NULL)
		THEN RAISE (ABORT, 'Foreign Key Violation') END;
	END;

//--------------------------------------------------------------------------------------------------------//

// Course : Item : pHash
INSERT INTO Course(title, category, creator)
	VALUES ('Course', 'Category', 'strider.stankov@gmail.com');
INSERT INTO Item(title, content, course_id)
	VALUES ('Item', 'This is temp Item.<br><br>For more info blahblahblah...', 1);
INSERT INTO PHash(hex_value, comment, item_id)
	VALUES ('FFFFFFFFFFFF', 'This is temp pHash value.<br><br>For more info blahblahblah...', 1);

// Course : Item : pHash
INSERT INTO Course(title, category, creator)
	VALUES ('Temp', 'Main', 'strider.stankov@gmail.com');
INSERT INTO Item(title, content, course_id)
	VALUES ('Object', 'This is Object Item.<br><br>For more info blahblahblah...', 2);
INSERT INTO PHash(hex_value, comment, item_id)
	VALUES ('FFFFFFFFFFFF', 'This is Object pHash value.<br><br>For more info blahblahblah...', 2);


