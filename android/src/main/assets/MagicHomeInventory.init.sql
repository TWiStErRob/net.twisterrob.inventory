delete from Category_Descendant;
-- Assumes root -> level1 -> level2 -> level3 maximum depth
insert into Category_Descendant (category, level, descendant)
	select
		c0._id as category,
		0      as level,
		c0._id as descendant
	from Category c0;
insert into Category_Descendant (category, level, descendant)
	select
		c0._id as category,
		1      as level,
		c1._id as descendant
	from Category c0
		join Category c1 ON c0._id = c1.parent;
insert into Category_Descendant (category, level, descendant)
	select
		c0._id as category,
		2      as level,
		c2._id as descendant
	from Category c0
		join Category c1 ON c0._id = c1.parent
		join Category c2 ON c1._id = c2.parent;
insert into Category_Descendant (category, level, descendant)
	select
		c0._id as category,
		3      as level,
		c3._id as descendant
	from Category c0
		join Category c1 ON c0._id = c1.parent
		join Category c2 ON c1._id = c2.parent
		join Category c3 ON c2._id = c3.parent;
