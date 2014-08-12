-- insert into Log(message) values('Log message');
CREATE TABLE Log (
	_id         INTEGER      NOT NULL,
	at          DATETIME     DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW')),
	message     TEXT,
	PRIMARY KEY(_id AUTOINCREMENT)
);

CREATE TABLE Category (
	_id         INTEGER      NOT NULL,
	name        VARCHAR      NOT NULL, -- string resource name
	image       VARCHAR      NOT NULL, -- raw resource name
	parent      INTEGER          NULL
		CONSTRAINT fk_Category_parent
			REFERENCES Category(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE(name)
);
CREATE TABLE Category_Name_Cache (
	key         VARCHAR      NOT NULL, -- string resource name
	value       NVARCHAR         NULL, -- translated display name by Android resources
	PRIMARY KEY(key)
);
CREATE TRIGGER Category_Name_Cache_prevent_rename
AFTER UPDATE OF key ON Category_Name_Cache BEGIN
	select RAISE(ABORT, 'Cannot change Category_Name_Cache.key column');--NOTEOS
END;

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
	name        VARCHAR      NOT NULL, -- string resource name
	priority    INTEGER      NOT NULL,
	image       VARCHAR      NOT NULL, -- raw resource name
	PRIMARY KEY(_id),
	UNIQUE (name)
);
CREATE TABLE Property (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL, -- user entered
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
	PRIMARY KEY(_id),
	UNIQUE (name)
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
	PRIMARY KEY(_id),
	UNIQUE (name)
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
    insert into Item(name, category, parent) values ('ROOT', -1, NULL);--NOTEOS
    insert into Room values (new._id, new.name, new.image, new.type, last_insert_rowid(), new.property);--NOTEOS
END;
CREATE TRIGGER Room_Rooter_Transparent
INSTEAD OF INSERT ON Room_Rooter WHEN (new.root IS NOT NULL) BEGIN
    insert into Room values (new._id, new.name, new.image, new.type, new.root, new.property);--NOTEOS
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
	--insert into Log(message) values ('Item_Path_traverse on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '): ' || 'started');--NOTEOS
	-- Go up in the Tree
	insert into Item_Path
		select new.item, new.level + 1, i.parent, i.parent from Item i where i._id = new.node and i.parent IS NOT NULL
	;--NOTEOS
	--insert into Log(message) values ('Item_Path_traverse on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '):' || 'finished');--NOTEOS
END;


CREATE VIEW Item_Paths AS
	select
		e._id,
		e.level,
		e.name,
		e.category,
		e.name || ' (' || ifNULL(cnc.value, '?') || ')' as nameWithCategory,
		e.property || ' > ' || e.room || ifNULL(' > ' || group_concat(e.node, ' > '), '') as path
	from (
		select
			i._id,
			i.name,
			ip.level,
			CASE WHEN (ip.item <> ip.node) THEN e.name ELSE NULL END as node,
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
	) e
	left join Category_Name_Cache cnc ON e.category = cnc.key
	group by _id, name, category, property, room
	order by name asc
;

CREATE VIRTUAL TABLE Search USING FTS3 (
	_id,
	name,
	location
);

CREATE VIEW Search_View AS
	select NULL as _id
;

CREATE TRIGGER Search_insert INSTEAD OF INSERT ON Search_View BEGIN
	--insert into Log(message) values ('Search_insert on (' || new._id || ')');--NOTEOS
	delete from Search where _id MATCH new._id;--NOTEOS
	insert into Search
		select
			_id              as _id,
			nameWithCategory as name,
			path             as location
		from Item_Paths where _id = new._id
	;--NOTEOS
END;

CREATE TRIGGER Item_Path_calculate
AFTER INSERT ON Item BEGIN
	--insert into Log(message) values ('Item_Path_calculate on (' || new._id || ', ' || new.name || ', ' || ifNULL(new.image, 'NULL') || ', ' || new.category || ', ' || ifNULL(new.parent, 'NULL') || '): ' || 'started');--NOTEOS
	insert into Item_Path
		select new._id, 0, new._id, new._id
	;--NOTEOS
	update Item_Path
		set level = (select max(level) from Item_Path where item = new._id) - level
		where item = new._id
	;--NOTEOS
	update Item_Path
		set root = (select node from Item_Path where item = new._id and level = 0) -- dependent on level being set: 0 as root .. n as leaf
		where item = new._id
	;--NOTEOS
	insert into Search_View select new._id;--NOTEOS
	--insert into Log(message) values ('Item_Path_calculate on (' || new._id || ', ' || new.name || ', ' || ifNULL(new.image, 'NULL') || ', ' || new.category || ', ' || ifNULL(new.parent, 'NULL') || '): ' || 'finished');--NOTEOS
END;

CREATE TRIGGER Category_Name_Cache_insert
AFTER INSERT ON Category_Name_Cache BEGIN
	--insert into Log(message) values ('Category_Name_Cache_insert on (' || new.key || ', ' || ifNULL(new.value, 'NULL') || ')');--NOTEOS
	insert into Search_View(_id) select _id from Item_Paths where category = new.key;--NOTEOS
END;
CREATE TRIGGER Category_Name_Cache_update
AFTER UPDATE OF value ON Category_Name_Cache WHEN (ifNULL(old.value, '') <> ifNULL(new.value, '')) BEGIN 
	--insert into Log(message) values ('Category_Name_Cache_update on (' || new.key || ', ' || ifNULL(old.value, 'NULL') || ' -> ' || ifNULL(new.value, 'NULL') || ')');--NOTEOS
	insert into Search_View(_id) select _id from Item_Paths where category = new.key;--NOTEOS
END;
CREATE TRIGGER Category_Name_Cache_delete
AFTER DELETE ON Category_Name_Cache BEGIN
	--insert into Log(message) values ('Category_Name_Cache_delete on (' || old.key || ', ' || ifNULL(old.value, 'NULL') || ')');--NOTEOS
	insert into Search_View(_id) select _id from Item_Paths where category = old.key;--NOTEOS
END;
