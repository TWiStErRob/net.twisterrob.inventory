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
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Room_type
			REFERENCES RoomType(_id),
	root        INTEGER
		CONSTRAINT fk_Room_root
			REFERENCES Item(_id),
	property    INTEGER NOT NULL
		CONSTRAINT fk_Room_property
			REFERENCES Property(_id),
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (property, name)
);

--CREATE VIEW IF NOT EXISTS LondonBoroughsByName AS
--	SELECT lbo.name, a.area_code
--	FROM LondonArea a, LBO lbo
--	WHERE a.district_code = lbo.id
--	ORDER BY 1, 2;
