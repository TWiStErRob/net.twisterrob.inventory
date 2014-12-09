-- This file is used to simulate user actions to see the DB state before and after
-- Some triggers populate the Log table if those lines are uncommented.

-- fake some values for Category names
update Category_Name_Cache set value = UPPER(key);


select '';
select '__________________________________.- Before -.__________________________________';
insert into Log(message) values('Before');
select '--------------------------------------------------------------------------------';
select * from Item_Path order by itemID;
select '--------------------------------------------------------------------------------';
select * from Search order by _id;


select '';
select '_______________________________.- Change stuff -._______________________________';
insert into Log(message) values('Change stuff');
select '--------------------------------------------------------------------------------';
BEGIN TRANSACTION;

	update Item set name = '----------' where _id = 100007;

END TRANSACTION;


select '';
select '__________________________________.- After -.___________________________________';
insert into Log(message) values('After');
select '--------------------------------------------------------------------------------';
select * from Item_Path order by itemID;
select '--------------------------------------------------------------------------------';
select * from Search order by _id;

select '';
select '__________________________________.- Log -._____________________________________';
select * from Log
where _id >= 217
order by _id;
