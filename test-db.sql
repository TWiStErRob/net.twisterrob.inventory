-- This file is used to simulate user actions to see the DB state before and after
-- Some triggers populate the Log table if those lines are uncommented.

-- fake some values for Category names
update Category_Name_Cache set value = UPPER(key);

-- Helps to figure out problems with foreign key constraints
--PRAGMA defer_foreign_keys = 1;

select '';
select '__________________________________.- Before -.__________________________________';
insert into Log(message) values('Before');
select '--------------------------------------------------------------------------------';
select * from Room order by _id;
select '--------------------------------------------------------------------------------';
select * from Item where name = 'ROOT' order by _id;


select '';
select '_______________________________.- Change stuff -._______________________________';
insert into Log(message) values('Change stuff');
select '--------------------------------------------------------------------------------';
BEGIN TRANSACTION;

	--delete from Item where _id = 1;
	delete from Room where _id = 4;

END TRANSACTION;


select '';
select '__________________________________.- After -.___________________________________';
insert into Log(message) values('After');
select '--------------------------------------------------------------------------------';
select * from Room order by _id;
select '--------------------------------------------------------------------------------';
select * from Item where name = 'ROOT' order by _id;

select '';
select '__________________________________.- Log -._____________________________________';
select * from Log
--where _id >= 217
order by _id;
