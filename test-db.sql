.mode column

-- This file is used to simulate user actions to see the DB state before and after
-- Some triggers populate the Log table if those lines are uncommented.

-- fake some values for Category names
update Category_Name_Cache set value = UPPER(key);

-- Helps to figure out problems with foreign key constraints
--PRAGMA defer_foreign_keys = 1;

.headers off
select '';
select '__________________________________.- Before -.__________________________________';
insert into Log(message) values('Before');
.headers on

select * from Item order by _id;


.headers off
select '';
select '_______________________________.- Change stuff -._______________________________';
insert into Log(message) values('Change stuff');
select '--------------------------------------------------------------------------------';
.headers on

BEGIN TRANSACTION;


-- write your stuff here


END TRANSACTION;

.headers off
select '';
select '__________________________________.- After -.___________________________________';
insert into Log(message) values('After');
select '--------------------------------------------------------------------------------';
.headers on

select * from Item order by _id;

.headers off
select '';
select '__________________________________.- Log -._____________________________________';
.headers on

.width 3 23 200
select * from Log
--where _id >= 217
order by _id
;
