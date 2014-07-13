CREATE TABLE IF NOT EXISTS Category (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	parent      INTEGER          NULL
		CONSTRAINT fk_Category_parent
			REFERENCES Category(_id),
	PRIMARY KEY(_id)
);

CREATE TABLE IF NOT EXISTS BuildingType (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL, -- drawable resource name
	PRIMARY KEY(_id)
);
CREATE TABLE IF NOT EXISTS Building (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Building_type
			REFERENCES BuildingType(_id),
	PRIMARY KEY(_id)
);

CREATE TABLE IF NOT EXISTS RoomTypeKind (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	PRIMARY KEY(_id)
);
CREATE TABLE IF NOT EXISTS RoomType (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	kind        INTEGER      NOT NULL
		CONSTRAINT fk_RoomType_kind
			REFERENCES RoomTypeKind(_id),
	priority    INTEGER      NOT NULL,
	PRIMARY KEY(_id)
);
CREATE TABLE IF NOT EXISTS Room (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	type        INTEGER      NOT NULL
		CONSTRAINT fk_Room_type
			REFERENCES RoomType(_id),
	building	INTEGER NOT NULL
		CONSTRAINT fk_county_code
			REFERENCES Building(_id),
	PRIMARY KEY(_id)
);

--CREATE VIEW IF NOT EXISTS LondonBoroughsByName AS
--	SELECT lbo.name, a.area_code
--	FROM LondonArea a, LBO lbo
--	WHERE a.district_code = lbo._id
--	ORDER BY 1, 2;
