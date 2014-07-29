CREATE TABLE IF NOT EXISTS Category (
	id          INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	parent      INTEGER          NULL
		CONSTRAINT fk_Category_parent
			REFERENCES Category(id),
	PRIMARY KEY(id AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS Item (
	id          INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	category    INTEGER      NOT NULL
		CONSTRAINT fk_Item_category
			REFERENCES Category(id),
	parent      INTEGER          NULL
		CONSTRAINT fk_Item_parent
			REFERENCES Item(id),
	PRIMARY KEY(id AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS PropertyType (
	id          INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL, -- drawable resource name
	PRIMARY KEY(id)
);
CREATE TABLE IF NOT EXISTS Property (
	id          INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Property_type
			REFERENCES PropertyType(id),
	PRIMARY KEY(id AUTOINCREMENT),
	UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS RoomTypeKind (
	id          INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL, -- drawable resource name
	PRIMARY KEY(id AUTOINCREMENT)
);
CREATE TABLE IF NOT EXISTS RoomType (
	id          INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	kind        INTEGER      NOT NULL
		CONSTRAINT fk_RoomType_kind
			REFERENCES RoomTypeKind(id),
	priority    INTEGER      NOT NULL,
	image       VARCHAR          NULL, -- drawable resource name
	PRIMARY KEY(id)
);
CREATE TABLE IF NOT EXISTS Room (
	id          INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Room_type
			REFERENCES RoomType(id),
	root        INTEGER
		CONSTRAINT fk_Room_root
			REFERENCES Item(id),
	property    INTEGER NOT NULL
		CONSTRAINT fk_Room_property
			REFERENCES Property(id),
	PRIMARY KEY(id AUTOINCREMENT),
	UNIQUE (property, name)
);

--CREATE VIEW IF NOT EXISTS LondonBoroughsByName AS
--	SELECT lbo.name, a.area_code
--	FROM LondonArea a, LBO lbo
--	WHERE a.district_code = lbo.id
--	ORDER BY 1, 2;
