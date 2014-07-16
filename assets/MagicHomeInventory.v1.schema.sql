CREATE TABLE IF NOT EXISTS Category (
	id          INTEGER      PRIMARY KEY,
	name        NVARCHAR     NOT NULL,
	parent      INTEGER          NULL
		CONSTRAINT fk_Category_parent
			REFERENCES Category(id)
);

CREATE TABLE IF NOT EXISTS Item (
	id          INTEGER      PRIMARY KEY AUTOINCREMENT,
	name        NVARCHAR     NOT NULL,
	category    INTEGER      NOT NULL
		CONSTRAINT fk_Item_category
			REFERENCES Category(id),
	parent      INTEGER          NULL
		CONSTRAINT fk_Item_parent
			REFERENCES Item(id)
);

CREATE TABLE IF NOT EXISTS PropertyType (
	id          INTEGER      PRIMARY KEY,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL -- drawable resource name
);
CREATE TABLE IF NOT EXISTS Property (
	id          INTEGER      PRIMARY KEY AUTOINCREMENT,
	name        NVARCHAR     NOT NULL,
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Property_type
			REFERENCES PropertyType(id)
);

CREATE TABLE IF NOT EXISTS RoomTypeKind (
	id          INTEGER      PRIMARY KEY,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL -- drawable resource name
);
CREATE TABLE IF NOT EXISTS RoomType (
	id          INTEGER      PRIMARY KEY,
	name        NVARCHAR     NOT NULL,
	kind        INTEGER      NOT NULL
		CONSTRAINT fk_RoomType_kind
			REFERENCES RoomTypeKind(id),
	priority    INTEGER      NOT NULL,
	image       VARCHAR          NULL -- drawable resource name
);
CREATE TABLE IF NOT EXISTS Room (
	id          INTEGER      PRIMARY KEY AUTOINCREMENT,
	name        NVARCHAR     NOT NULL,
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Room_type
			REFERENCES RoomType(id),
	root        INTEGER
		CONSTRAINT fk_Room_root
			REFERENCES Item(id),
	property    INTEGER NOT NULL
		CONSTRAINT fk_Room_property
			REFERENCES Property(id)
);

--CREATE VIEW IF NOT EXISTS LondonBoroughsByName AS
--	SELECT lbo.name, a.area_code
--	FROM LondonArea a, LBO lbo
--	WHERE a.district_code = lbo.id
--	ORDER BY 1, 2;
