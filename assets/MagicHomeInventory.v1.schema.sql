CREATE TABLE Log (
	_id         INTEGER      NOT NULL,
	at          DATETIME     DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')),
	message     TEXT,
	PRIMARY KEY(_id AUTOINCREMENT)
);

CREATE TABLE Category (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL, -- string resource name
	image       VARCHAR      NOT NULL, -- raw resource name
	parent      INTEGER          NULL
		CONSTRAINT fk_Category_parent
			REFERENCES Category(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	PRIMARY KEY(_id AUTOINCREMENT)
);

CREATE TABLE Item (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL, -- user entered
	image       VARCHAR      NULL,     -- Google Drive ID
	category    INTEGER      DEFAULT 0 -- uncategorized
		CONSTRAINT fk_Item_category
			REFERENCES Category(_id)
			ON UPDATE CASCADE
			ON DELETE SET DEFAULT,
	parent      INTEGER          NULL
		CONSTRAINT fk_Item_parent
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (parent, name)
);

CREATE TABLE PropertyType (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL,
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL, -- raw resource name
	PRIMARY KEY(_id)
);
CREATE TABLE Property (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL, --user entered
	image       VARCHAR      NULL,     -- Google Drive ID
	type        INTEGER      DEFAULT 0 -- other
		CONSTRAINT fk_Property_type
			REFERENCES PropertyType(_id)
			ON UPDATE CASCADE
			ON DELETE SET DEFAULT,
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (name)
);

CREATE TABLE RoomTypeKind (
	_id         INTEGER      NOT NULL,
	name        VARCHAR      NOT NULL, -- string resource name
	image       VARCHAR      NOT NULL, -- raw resource name
	priority    INTEGER      NOT NULL,
	PRIMARY KEY(_id AUTOINCREMENT)
);
CREATE TABLE RoomType (
	_id         INTEGER      NOT NULL,
	name        VARCHAR      NOT NULL, -- string resource name
	image       VARCHAR          NULL, -- raw resource name
	priority    INTEGER      NOT NULL,
	kind        INTEGER      NOT NULL
		CONSTRAINT fk_RoomType_kind
			REFERENCES RoomTypeKind(_id)
			ON UPDATE RESTRICT
			ON DELETE RESTRICT,
	PRIMARY KEY(_id)
);
CREATE TABLE Room (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL, -- user entered
	image       VARCHAR      NULL,     -- Google Drive ID
	type        INTEGER      DEFAULT 0 -- other
		CONSTRAINT fk_Room_type
			REFERENCES RoomType(_id)
			ON UPDATE CASCADE
			ON DELETE SET DEFAULT,
	root        INTEGER      NOT NULL
		CONSTRAINT fk_Room_root
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE RESTRICT,
	property    INTEGER      NOT NULL
		CONSTRAINT fk_Room_property
			REFERENCES Property(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (property, name)
);
CREATE VIEW Room_Rooter AS select * from Room;
CREATE TRIGGER Room_Rooter_Auto
INSTEAD OF INSERT ON Room_Rooter WHEN (new.root IS NULL) BEGIN
    INSERT INTO Item(name, category, parent) VALUES ('ROOT', -1, NULL);--NOTEOS
    INSERT INTO Room VALUES (new._id, new.name, new.image, new.type, last_insert_rowid(), new.property);--NOTEOS
END;
CREATE TRIGGER Room_Rooter_Transparent
INSTEAD OF INSERT ON Room_Rooter WHEN (new.root IS NOT NULL) BEGIN
    INSERT INTO Room VALUES (new._id, new.name, new.image, new.type, new.root, new.property);--NOTEOS
END;


CREATE TABLE Category_Descendant (
	category    INTEGER      NOT NULL
		CONSTRAINT fk_Category_Descendant_category
			REFERENCES Category(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	level       INTEGER      NOT NULL,
	descendant  INTEGER      NOT NULL
		CONSTRAINT fk_Category_Descendant_descendant
			REFERENCES Category(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	UNIQUE (category, descendant)
);
CREATE TRIGGER Category_Descendant_traverse
AFTER INSERT ON Category_Descendant BEGIN
	-- Go down in the Tree
	insert into Category_Descendant
		select new.category, new.level + 1, c._id from Category c where c.parent = new.descendant
	;--NOTEOS
END;


CREATE TABLE Item_Path (
	item        INTEGER      NOT NULL
		CONSTRAINT fk_Item_Path_item
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	level       INTEGER      NOT NULL,
	node        INTEGER      NOT NULL
		CONSTRAINT fk_Item_Path_node
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE NO ACTION,
	root        INTEGER      NOT NULL
		CONSTRAINT fk_Item_Path_root
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE NO ACTION
);
CREATE TRIGGER Item_Path_traverse
AFTER INSERT ON Item_Path BEGIN
	insert into Log(message) values ('Item_Path_traverse on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '): '
		|| 'insert into Item_Path select ' || new.item || ', ' || (new.level - 1) || ', i.parent, i.parent, from Item i where i._id = ' || new.node || ' and i.parent IS NOT NULL'
	);--NOTEOS
	-- Go up in the Tree
	insert into Item_Path
		select new.item, new.level - 1, i.parent, i.parent from Item i where i._id = new.node and i.parent IS NOT NULL
	;--NOTEOS
	insert into Log(message) values ('Item_Path_traverse on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '): '
		|| 'update Item_Path set root = ' ||  (select node from Item_Path where item = new.item and level = new.level) || ' where item = ' || new.item || ' and node = ' || new.node
	);--NOTEOS
	-- Equivalent to calling the following after the original INSERT:
	-- UPDATE Item_Path p SET root = (select node from Item_Path where item = p.item and level = 0) WHERE item = inserted;
	update Item_Path
		set root = (select node from Item_Path where item = new.item and level = new.level)
		where item = new.item and node = new.node
	;--NOTEOS
	insert into Log(message) values ('Item_Path_traverse on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '): '
		|| 'update Item_Path set level = level + 1 where item = ' ||  new.item
		|| ' -- ' || (select group_concat(node || ':' || level, ', ') from Item_Path where item = new.item)
	);--NOTEOS
	-- Equivalent to calling the following after the original INSERT:
	-- UPDATE Item_Path p SET level = (select max(level) from Item_Path where item = p.item) - level WHERE item = inserted;
	update Item_Path
		set level = level + 1 where item = new.item
	;--NOTEOS
	insert into Log(message) values ('Item_Path_traverse on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '):'
		|| 'finished with ' || (select '(' || item || ', ' || level || ', ' || node || ', ' || root || ')' from Item_Path where item = new.item and node = new.node)
	);--NOTEOS

	insert into Log(message) values ('Item_Path_searchIndex on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '): '
		|| 'insert into Search select ' || IFNULL((select group_concat(_id || ', ' || name || ', ' || path) from Item_Paths where _id = new.item), 'null') || ' from Item_Paths where _id = ' || new.item
	);--NOTEOS
	insert into Search
		-- TODO concatenate resolved category to name
		select _id, name, path from Item_Paths where _id = new.item
	;--NOTEOS
	insert into Log(message) values ('Item_Path_searchIndex on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '): '
		|| 'finished'
	);--NOTEOS
END;

CREATE TRIGGER Item_Path_calculate
AFTER INSERT ON Item BEGIN
	insert into Log(message) values ('Item_Path_calculate on (' || new._id || ', ' || new.name || ', ' || IFNULL(new.image, 'null') || ', ' || new.category || ', ' || IFNULL(new.parent, 'null') || '): '
		|| 'insert into Item_Path select ' || new._id || ', -1, ' || new._id || ', ' || new._id
	);--NOTEOS
	insert into Item_Path
		select new._id, -1, new._id, new._id
	;--NOTEOS
	insert into Log(message) values ('Item_Path_calculate on (' || new._id || ', ' || new.name || ', ' || IFNULL(new.image, 'null') || ', ' || new.category || ', ' || IFNULL(new.parent, 'null') || '): '
		|| 'finished with ' || (select '(' || _id || ', ' || name || ', ' || IFNULL(image, 'null') || ', ' || category || ', ' || IFNULL(parent, 'null') || ')' from Item where _id = new._id)
	);--NOTEOS
END;

CREATE VIEW Item_Paths AS
	select
		_id,
		name as name,
		category as category,
		property || ' > ' || room || IFNULL(' > ' || group_concat(path, ' > '), '') as path
	from (
		select
			i._id,
			i.name,
			CASE WHEN (ip.item <> ip.node) THEN e.name ELSE NULL END as path,
			c.name as category,
			p.name as property,
			r.name as room
		from Item_Path ip
		join Item      i  ON ip.item = i._id
		join Item      e  ON ip.node = e._id
		join Category  c  ON i.category = c._id
		join Room      r  ON ip.root = r.root
		join Property  p  ON r.property = p._id
		where ip.node <> ip.root
		order by i._id, ip.level
	)
	group by _id, name, category, property, room
	order by name asc
;

CREATE VIRTUAL TABLE Search USING FTS3 (
	_id,
	name,
	location
);
