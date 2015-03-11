-- Usage: insert into Log(message) values ('Log message');
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
	UNIQUE(name),
	CHECK (_id <> parent)
);
CREATE INDEX Category_parent ON Category(parent);

CREATE TRIGGER Category_insert
AFTER INSERT ON Category BEGIN
	insert into Category_Name_Cache values(new.name, NULL);--NOTEOS
END;

-- Assumes internal (c0) -> root (c1) -> level1 (c2) -> level2 (c3) maximum depth
CREATE VIEW Category_Descendant AS
	select c0._id as category, 0 as level, c0._id as descendant
		from Category c0
    UNION ALL
	select c0._id as category, 1 as level, c1._id as descendant
		from Category c0
		join Category c1 ON c0._id = c1.parent
	UNION ALL
	select c0._id as category, 2 as level, c2._id as descendant
        from Category c0
        join Category c1 ON c0._id = c1.parent
        join Category c2 ON c1._id = c2.parent
	UNION ALL
    select c0._id as category, 3 as level, c3._id as descendant
        from Category c0
        join Category c1 ON c0._id = c1.parent
        join Category c2 ON c1._id = c2.parent
        join Category c3 ON c2._id = c3.parent
;

-- Assumes internal -> root -> level1 -> level2 maximum depth
CREATE VIEW Category_Tree AS
	select
		c0._id,
		c0.name,
		c0.image,
		2 - ((c1._id IS NULL) + (c2._id IS NULL)) as level,
		c0.parent,
		COALESCE(c2._id, c1._id) as root
	from Category c0
	left join Category c1 on c0.parent = c1._id and c1._id <> -1
	left join Category c2 on c1.parent = c2._id and c2._id <> -1
	order by c0.name, c1.name, c2.name
;

CREATE TABLE Category_Name_Cache (
	key         VARCHAR      NOT NULL, -- string resource name
	value       NVARCHAR         NULL, -- translated display name by Android resources
	PRIMARY KEY(key)
);
CREATE TRIGGER Category_Name_Cache_prevent_rename
AFTER UPDATE OF key ON Category_Name_Cache BEGIN
	select RAISE(ABORT, 'Cannot change Category_Name_Cache.key column');--NOTEOS
END;
CREATE TRIGGER Category_Name_Cache_insert
AFTER INSERT ON Category_Name_Cache BEGIN
	--insert into Log(message) values ('Category_Name_Cache_insert on (' || new.key || ', ' || ifNULL(new.value, 'NULL') || ')');--NOTEOS
	insert into Search_Refresher(_id) select itemID from Item_Path where categoryName = new.key;--NOTEOS
END;
CREATE TRIGGER Category_Name_Cache_update
AFTER UPDATE OF value ON Category_Name_Cache WHEN (ifNULL(old.value, '') <> ifNULL(new.value, '')) BEGIN
	--insert into Log(message) values ('Category_Name_Cache_update on (' || new.key || ', ' || ifNULL(old.value, 'NULL') || ' -> ' || ifNULL(new.value, 'NULL') || ')');--NOTEOS
	insert into Search_Refresher(_id) select itemID from Item_Path where categoryName = new.key;--NOTEOS
END;
CREATE TRIGGER Category_Name_Cache_delete
AFTER DELETE ON Category_Name_Cache BEGIN
	--insert into Log(message) values ('Category_Name_Cache_delete on (' || old.key || ', ' || ifNULL(old.value, 'NULL') || ')');--NOTEOS
	insert into Search_Refresher(_id) select itemID from Item_Path where categoryName = old.key;--NOTEOS
END;


CREATE TABLE Item (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL, -- user entered
	description TEXT         NULL,     -- user entered
	image       VARCHAR      NULL,     -- relative path
	category    INTEGER      DEFAULT 0 -- uncategorized
		CONSTRAINT fk_Item_category
			REFERENCES Category(_id)
			ON UPDATE CASCADE
			ON DELETE SET DEFAULT,
	parent      INTEGER          NULL -- -1 -> ROOT item
		CONSTRAINT fk_Item_parent
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (parent, name),
	CHECK (_id <> parent)
);
CREATE INDEX Item_category ON Item(category);
CREATE TRIGGER Item_insert
AFTER INSERT ON Item BEGIN
	--insert into Log(message) values ('Item_insert on (' || new._id || ', ' || new.name || ', ' || ifNULL(new.image, 'NULL') || ', ' || new.category || ', ' || ifNULL(new.parent, 'NULL') || '): ' || 'started');--NOTEOS
	insert into Item_Path_Node_Refresher(_id)
		values (new._id)
	;--NOTEOS
	--insert into Log(message) values ('Item_insert on (' || new._id || ', ' || new.name || ', ' || ifNULL(new.image, 'NULL') || ', ' || new.category || ', ' || ifNULL(new.parent, 'NULL') || '): ' || 'finished');--NOTEOS
END;
CREATE TRIGGER Item_delete
AFTER DELETE ON Item BEGIN
	--insert into Log(message) values ('Item_delete on (' || old._id || '): '  || 'started');--NOTEOS
	insert into Item_Path_Node_Refresher(_id)
		values (old._id)
	;--NOTEOS
	--insert into Log(message) values ('Item_delete on (' || old._id || '): '  || 'finished');--NOTEOS
END;
CREATE TRIGGER Item_move
AFTER UPDATE OF parent ON Item BEGIN
	--insert into Log(message) values ('Item_move on (' || new._id || ', ' || old.parent || '->' || new.parent || '): '  || 'started');--NOTEOS
	insert into Item_Path_Node_Refresher(_id)
		select distinct item from Item_Path_Node
		where node = new._id
	;--NOTEOS
	--insert into Log(message) values ('Item_move on (' || new._id || ', ' || old.parent || '->' || new.parent || '): '  || 'finished');--NOTEOS
END;
CREATE TRIGGER Item_rename
AFTER UPDATE OF name ON Item BEGIN
	--insert into Log(message) values ('Item_rename on (' || new._id || ', ' || old.name || '->' || new.name || '): '  || 'started');--NOTEOS
	insert into Item_Path_Node_Refresher(_id)
		select distinct item from Item_Path_Node
		where node = new._id
	;--NOTEOS
	--insert into Log(message) values ('Item_rename on (' || new._id || ', ' || old.name || '->' || new.name || '): '  || 'finished');--NOTEOS
END;
CREATE TRIGGER Item_categoryChange
AFTER UPDATE OF category ON Item BEGIN
	--insert into Log(message) values ('Item_rename on (' || new._id || ', ' || old.name || '->' || new.name || ', ' || old.category || '->' || new.category || '): '  || 'started');--NOTEOS
	insert into Search_Refresher(_id) values (new._id);--NOTEOS
	--insert into Log(message) values ('Item_rename on (' || new._id || ', ' || old.name || '->' || new.name || ', ' || old.category || '->' || new.category || '): '  || 'finished');--NOTEOS
END;


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
	description TEXT         NULL,     -- user entered
	image       VARCHAR      NULL,     -- relative path
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
			ON UPDATE CASCADE
			ON DELETE RESTRICT,
	PRIMARY KEY(_id),
	UNIQUE (name)
);
CREATE TABLE Room (
	_id         INTEGER      NOT NULL,
	name        NVARCHAR     NOT NULL, -- user entered
	description TEXT         NULL,     -- user entered
	image       VARCHAR      NULL,     -- relative path
	type        INTEGER      DEFAULT 0 -- other
		CONSTRAINT fk_Room_type
			REFERENCES RoomType(_id)
			ON UPDATE CASCADE
			ON DELETE SET DEFAULT,
	root        INTEGER      NOT NULL
		CONSTRAINT fk_Room_root
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE RESTRICT, -- need to delete the room to delete the root
	property    INTEGER      NOT NULL
		CONSTRAINT fk_Room_property
			REFERENCES Property(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	PRIMARY KEY(_id AUTOINCREMENT),
	UNIQUE (property, name)
);
CREATE TRIGGER Room_delete_root
AFTER DELETE ON Room BEGIN
	--insert into Log(message) values ('Room_Delete_Root on (' || old._id || ', ' || old.root || '): '  || 'started');--NOTEOS
	delete from Item where _id = old.root;--NOTEOS
	--insert into Log(message) values ('Room_Delete_Root on (' || old._id || ', ' || old.root || '): '  || 'finished');--NOTEOS
END;
CREATE TRIGGER Room_move
AFTER UPDATE OF property ON Room BEGIN
	--insert into Log(message) values ('Room_Property_Move on (' || new._id || ', ' || old.property || '->' || new.property || ', ' || new.root || ', ' || new.name || '): ' || 'started');--NOTEOS
	insert into Search_Refresher(_id)
		select ip.itemID from Item_Path ip
		where ip.rootItemID <> ip.itemID and ip.roomID = new._id
	;--NOTEOS
	--insert into Log(message) values ('Room_Property_Move on (' || new._id || ', ' || old.property || '->' || new.property || ', ' || new.root || ', ' || new.name || '): ' || 'finished');--NOTEOS
END;

CREATE VIEW Room_Rooter AS select * from Room;
CREATE TRIGGER Room_Rooter_Auto
INSTEAD OF INSERT ON Room_Rooter WHEN (new.root IS NULL) BEGIN
	insert into Item(name, category, parent)
		values ('ROOT', -1, NULL)
	;--NOTEOS
	insert into Room(_id, name, image, type, root, property)
		values (new._id, new.name, new.image, new.type, last_insert_rowid(), new.property)
	;--NOTEOS
END;
CREATE TRIGGER Room_Rooter_Transparent
INSTEAD OF INSERT ON Room_Rooter WHEN (new.root IS NOT NULL) BEGIN
	insert into Room(_id, name, image, type, root, property)
		values (new._id, new.name, new.image, new.type, new.root, new.property)
	;--NOTEOS
END;


CREATE TABLE Recent (
	_id         INTEGER,
	visit       DATETIME DEFAULT (STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW'))
);
CREATE TRIGGER Recent_insert
AFTER INSERT ON Recent BEGIN
	delete from Recent where 100 < (
		select count() from Recent r where Recent.visit <= r.visit
	);--NOTEOS
END;

CREATE VIEW Recent_Stats AS
	select
		_id,
		count(1) as population,
		count(1)/cast(s.count as float) as percentage,
		max(visit) as visit,
		(julianday(max(visit)) - julianday(s.firstVisit)) / (julianday(s.lastVisit) - julianday(s.firstVisit)) as recency
	from Recent,
	(
		select
			min(visit) as firstVisit,
			max(visit) as lastVisit,
			--datetime(avg(julianday(visit))) as meanVisit,
			count() as count
		from Recent
	) s
	group by _id
;
CREATE VIEW Recents AS
	select
		r._id,
		r.population,
		r.percentage,
		count(distinct rp._id) as populationRank,
		r.visit,
		r.recency,
		count(distinct rr._id) as visitRank
	from Recent_Stats r
	join Recent_Stats rp on r.population <= rp.population
	join Recent_Stats rr on r.visit <= rr.visit
	group by r._id
;

CREATE TABLE List (
	_id         INTEGER      NOT NULL,
	name        VARCHAR      NOT NULL, -- user entered
	PRIMARY KEY(_id AUTOINCREMENT),
   	UNIQUE (name)
);
CREATE TABLE List_Entry (
	list        INTEGER      NOT NULL
		CONSTRAINT fk_List_Entry_list
			REFERENCES List(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	item        INTEGER      NOT NULL
		CONSTRAINT fk_List_Entry_item
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	UNIQUE (list, item)
);

CREATE TABLE Item_Path_Node (
	item        INTEGER      NOT NULL
		CONSTRAINT fk_Item_Path_Node_item
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE CASCADE,
	level       INTEGER      NOT NULL,
	node        INTEGER      NOT NULL
		CONSTRAINT fk_Item_Path_Node_node
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE NO ACTION,
	root        INTEGER      NOT NULL
		CONSTRAINT fk_Item_Path_Node_root
			REFERENCES Item(_id)
			ON UPDATE CASCADE
			ON DELETE NO ACTION
);
CREATE TRIGGER Item_Path_Node_traverse
AFTER INSERT ON Item_Path_Node BEGIN
	--insert into Log(message) values ('Item_Path_Node_traverse on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '): ' || 'started');--NOTEOS
	-- Go up in the Tree
	insert into Item_Path_Node
		select new.item, new.level + 1, i.parent, i.parent from Item i
		where i._id = new.node and i.parent IS NOT NULL
	;--NOTEOS
	--insert into Log(message) values ('Item_Path_Node_traverse on (' || new.item || ', ' || new.level || ', ' || new.node || ', ' || new.root || '): ' || 'finished');--NOTEOS
END;

CREATE VIEW Item_Path_Node_Refresher AS
	SELECT NULL as _id
;
CREATE TRIGGER Item_Path_Node_refresh
INSTEAD OF INSERT ON Item_Path_Node_Refresher BEGIN
	--insert into Log(message) values ('Item_Path_Node_refresh on (' || new._id || '): ' || 'started');--NOTEOS
	-- Go up in the Tree
	delete from Item_Path_Node where item = new._id;--NOTEOS
	insert into Item_Path_Node
		select new._id, 0, new._id, new._id
		from Item where _id = new._id -- need to restrict in case the item doesn't exist any more
	;--NOTEOS
	update Item_Path_Node
		set level = (select max(level) from Item_Path_Node where item = new._id) - level
		where item = new._id
	;--NOTEOS
	update Item_Path_Node
		set root = (select node from Item_Path_Node where item = new._id and level = 0) -- dependent on level being set: 0 as root .. n as leaf
		where item = new._id
	;--NOTEOS
	insert into Search_Refresher(_id)
		values (new._id)
	;--NOTEOS
	--insert into Log(message) values ('Item_Path_Node_refresh on (' || new._id || '): ' || 'finished');--NOTEOS
END;

-- WITH clause support was added in SQLite 3.8.3, first to support it is Android 5.0
CREATE VIEW Item_Path_WITH_Node_Name AS
	select
		ipn.*,
		n.name as nodeName
	from Item_Path_Node ipn
	join Item           n   ON ipn.node = n._id
	where ipn.item <> ipn.node and ipn.node <> ipn.root
;
CREATE VIEW Item_Path AS
	select
		p._id      as propertyID,
		p.name     as propertyName,
		r._id      as roomID,
		r.name     as roomName,
		r.root     as rootItemID,
		ipn.level  as itemLevel,
		i._id      as itemID,
		i.name     as itemName,
		c._id      as categoryID,
		c.name     as categoryName,
		Path.path  as path,
		p.name || ' > ' || r.name || ifNULL(' > ' || Path.path, '') as fullPath,
		rPath.path as reversePath,
		ifNULL(rPath.path || ' < ', '') || r.name || ' < ' || p.name as fullReversePath
	from Item           i
	left join (
		select item, group_concat(nodeName, ' > ') as path
		from (select * from Item_Path_WITH_Node_Name order by item, level)
		group by item
	)    Path               ON i._id = Path.item
	left join (
		select item, group_concat(nodeName, ' < ') as path
		from (select * from Item_Path_WITH_Node_Name order by item, level DESC)
		group by item
	)    rPath              ON i._id = rPath.item
	join Item_Path_Node ipn ON ipn.item = i._id and ipn.node = i._id
	join Room           r   ON ipn.root = r.root
	join Property       p   ON r.property = p._id
	join Category       c   ON i.category = c._id
;


CREATE VIRTUAL TABLE Search USING FTS3 (
	_id,
	name,
	location
);

CREATE VIEW Search_Refresher AS
	select NULL as _id
;
CREATE TRIGGER Search_refresh
INSTEAD OF INSERT ON Search_Refresher BEGIN
	--insert into Log(message) values ('Search_refresh on (' || new._id || ')');--NOTEOS
	delete from Search where _id MATCH new._id;--NOTEOS
	insert into Search
		select
			ip.itemID                                            as _id,
			ip.itemName || ' (' || ifNULL(cnc.value, '?') || ')' as name,
			ip.reversePath                                       as location
		from Item_Path                ip
		left join Category_Name_Cache cnc ON ip.categoryName = cnc.key
		where ip.itemID = new._id
	;--NOTEOS
END;
