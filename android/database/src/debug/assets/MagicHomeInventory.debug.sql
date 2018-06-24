DROP VIEW IF EXISTS Items;
CREATE VIEW Items AS
	select
		p._id          as propertyID,
		p.name         as propertyName,
		r._id          as roomID,
		r.name         as roomName,
		r.root         as rootItemID,
		s._id          as parentID,
		s.name         as parentName,
		i._id          as itemID,
		i.name         as itemName,
		c._id          as categoryID,
		c.name         as categoryName
	from Item_Path_Node ipn
	join Item           i   ON ipn.item = i._id and ipn.node = ipn.item
	join Room           r   ON ipn.root = r.root
	join Property       p   ON r.property = p._id
	join Category       c   ON i.category = c._id
	left join Item      s   ON i.parent = s._id
;
--select * from Items;
