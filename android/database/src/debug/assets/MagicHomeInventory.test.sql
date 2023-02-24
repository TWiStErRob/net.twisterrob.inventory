-- All Rooms with distinct icons
INSERT INTO Property (_id, name) VALUES (0, '!All Rooms');
INSERT INTO Room_Rooter (property, type, name)
	select
		0                                            as property,
		_id                                          as type,
		upper(substr(name, 6, 1)) || substr(name, 7) as name
	from RoomType pt
	join (
		select
			MIN(pt._id)                 as representative,
			IFNULL(pt.image, ptk.image) as image
		from RoomType     pt
		join RoomTypeKind ptk on pt.kind = ptk._id
		group by 2
		order by 1
	) im on pt._id = im.representative
;

-- All Categories with distinct icons
INSERT INTO Property (_id, name) VALUES (1, '!Test');
INSERT INTO Room_Rooter (property, type, name) VALUES (1, 0, '!All Categories');
INSERT INTO Item (parent, category, name)
	select
		(select root from Room where name = '!All Categories')            as parent,
		c._id                                                             as category,
		replace(upper(substr(name, 10, 1)) || substr(name, 11), '_', ' ') as name
	from Category c
	join (
		select
			MIN(_id) as representative,
			image    as image
		from Category
		group by 2
		order by 1
	) im on c._id = im.representative
;

-- All Properties with distinct icons
INSERT INTO Property (type, name)
	select
		pt._id                                         as type,
		upper(substr(name, 10, 1)) || substr(name, 11) as name
	from PropertyType pt
	join (
		select
			MIN(pt._id)                 as representative,
			IFNULL(pt.image, ptk.image) as image
		from PropertyType     pt
		join PropertyTypeKind ptk on pt.kind = ptk._id
		group by 2
		order by 1
	) im on pt._id = im.representative
;

-- Other test items in !Test property
INSERT INTO Room_Rooter (property, type, name) VALUES (1, 0, '!Test Hierarchy');
	INSERT INTO Item(_id, parent, category, name)
		VALUES(10001, (select root from Room where name = '!Test Hierarchy'), (select _id from Category where name = 'category_internal'), 'Stuff');
	INSERT INTO Item(_id, parent, category, name)
		VALUES(10002, (select root from Room where name = '!Test Hierarchy'), (select _id from Category where name = 'category_drinkware'), 'Glass');
		INSERT INTO Item(_id, parent, category, name)
			VALUES(10100, 10002, (select _id from Category where name = 'category_liquid'), 'Water');
			INSERT INTO Item(_id, parent, category, name)
				VALUES(10101, 10100, (select _id from Category where name = 'category_part'), 'H x2');
			INSERT INTO Item(_id, parent, category, name)
				VALUES(10102, 10100, (select _id from Category where name = 'category_part'), 'O');
	INSERT INTO Item(_id, parent, category, name)
		VALUES(10010, (select root from Room where name = '!Test Hierarchy'), (select _id from Category where name = 'category_group'), 'Items');
		INSERT INTO Item(_id, parent, category, name) VALUES(10011, 10010, 0, 'Item 10011');
		INSERT INTO Item(_id, parent, category, name) VALUES(10012, 10010, 0, 'Item 10012');
		INSERT INTO Item(_id, parent, category, name) VALUES(10013, 10010, 0, 'Item 10013');
		INSERT INTO Item(_id, parent, category, name) VALUES(10014, 10010, 0, 'Item 10014');
		INSERT INTO Item(_id, parent, category, name) VALUES(10015, 10010, 0, 'Item 10015');
		INSERT INTO Item(_id, parent, category, name) VALUES(10016, 10010, 0, 'Item 10016');
		INSERT INTO Item(_id, parent, category, name) VALUES(10017, 10010, 0, 'Item 10017');
		INSERT INTO Item(_id, parent, category, name) VALUES(10018, 10010, 0, 'Item 10018');
		INSERT INTO Item(_id, parent, category, name) VALUES(10019, 10010, 0, 'Item 10019');
