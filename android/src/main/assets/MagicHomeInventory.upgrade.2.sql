DROP TRIGGER IF EXISTS Search_refresh;
CREATE TRIGGER Search_refresh
INSTEAD OF INSERT ON Search_Refresher
BEGIN
	--insert into Log(message) values ('Search_refresh on (' || new._id || ')');--NOTEOS
	delete from Search
	where rowid = new._id; --NOTEOS
	insert into Search (rowid, name, location)
		select
			i._id                                           as rowid,
			i.name || ' (' || ifNULL(cnc.value, '?') || ')' as name,
			group_concat(Path.part, ' ◀ ')                  as location
		from Item i
			join Category c ON i.category = c._id
			left join Category_Name_Cache cnc ON c.name = cnc.key
			left join (
				          select n.name as part
				          from Item_Path_Node ipn
					          join Item n ON ipn.node = n._id
				          where ipn.item = new._id and ipn.item <> ipn.node and ipn.node <> ipn.root
				          order by level
					          DESC
			          ) as Path
		where i._id = new._id and c._id <> -1 -- category_internal
	; --NOTEOS
END;

insert into Search_Refresher (_id)
	select _id
	from Item;
