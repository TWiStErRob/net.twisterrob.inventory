insert into Log(message)
	select 'Category_Related contains duplicates: '
			|| cr1.category || ' -> ' || cr1.related || (' in both ') || cr1.source || ' and ' || cr2.source
	from Category_Related cr1
	join Category_Related cr2 on cr1.category = cr2.category and cr1.related = cr2.related
	where cr1.source < cr2.source
;
