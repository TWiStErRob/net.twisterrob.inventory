CREATE TABLE IF NOT EXISTS Category (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	parent      INTEGER          NULL
		CONSTRAINT fk_Category_parent
			REFERENCES Category(_id),
	PRIMARY KEY(_id AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS Item (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	image       NVARCHAR     NULL,     -- Google Drive ID
	category    INTEGER      NOT NULL
		CONSTRAINT fk_Item_category
			REFERENCES Category(_id),
	parent      INTEGER          NULL
		CONSTRAINT fk_Item_parent
			REFERENCES Item(_id),
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (parent, name)
);

CREATE TABLE IF NOT EXISTS PropertyType (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL, -- drawable resource name
	PRIMARY KEY(_id)
);
CREATE TABLE IF NOT EXISTS Property (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	image       NVARCHAR     NULL,     -- Google Drive ID
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Property_type
			REFERENCES PropertyType(_id),
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS RoomTypeKind (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL, -- drawable resource name
	PRIMARY KEY(_id AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS RoomType (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	kind        INTEGER      NOT NULL
		CONSTRAINT fk_RoomType_kind
			REFERENCES RoomTypeKind(_id),
	priority    INTEGER      NOT NULL,
	image       VARCHAR          NULL, -- drawable resource name
	PRIMARY KEY(_id)
);
CREATE TABLE IF NOT EXISTS Room (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	image       NVARCHAR     NULL,     -- Google Drive ID
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Room_type
			REFERENCES RoomType(_id),
	root        INTEGER
		CONSTRAINT fk_Room_root
			REFERENCES Item(_id),
	property    INTEGER      NOT NULL
		CONSTRAINT fk_Room_property
			REFERENCES Property(_id),
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (property, name)
);

CREATE TABLE IF NOT EXISTS Category_Descendant (
	category    INTEGER      NOT NULL
		CONSTRAINT fk_Category_Descendant_category
			REFERENCES Category(_id),
	level       INTEGER      NOT NULL,
	descendant  INTEGER      NOT NULL
		CONSTRAINT fk_Category_Descendant_descendant
			REFERENCES Category(_id),
	UNIQUE (category, descendant)
);
CREATE TRIGGER Category_Descendant_traverse
AFTER INSERT ON Category_Descendant BEGIN
	-- Go down in the Tree
	INSERT INTO Category_Descendant
		SELECT new.category, new.level + 1, c._id FROM Category c WHERE c.parent = new.descendant
	;--NOTEOS
END;

CREATE TABLE IF NOT EXISTS Paths (
	_id         INTEGER      NOT NULL,
	PRIMARY KEY(_id AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS Path (
	path        INTEGER      NOT NULL
		CONSTRAINT fk_Path_path
			REFERENCES Paths(_id)
			ON DELETE CASCADE,
	level       INTEGER      NOT NULL,
	node        INTEGER      NOT NULL,
	parent      INTEGER          NULL
);
CREATE TRIGGER find_path
AFTER INSERT ON Path BEGIN
	-- Go up in the Tree
	INSERT INTO Path
		SELECT new.path, new.level - 1, c._id, c.parent FROM Category c WHERE c._id = new.parent
	;--NOTEOS
	-- Equivalent to calling the following after the original INSERT:
	-- UPDATE Path p SET level = (select max(level) from Path where path = p.path) - level WHERE path = currentPath;
	UPDATE Path
		SET level = level + 1 WHERE path = new.path
	;--NOTEOS
END;

