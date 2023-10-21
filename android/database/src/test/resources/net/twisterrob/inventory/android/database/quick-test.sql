.mode column
.width 120
--.width auto

-- This file is used to simulate user actions to see the DB state before and after.
-- Some triggers populate the Log table if those lines are uncommented.

-- Fake some values for Category names.
update Category_Name_Cache
set value = replace(UPPER(substr(key, 10, 1)) || substr(key, 11), '_', ' ');

-- Helps to figure out problems with foreign key constraints.
-- Don't use it always, because it differs in behavior from production.
--PRAGMA defer_foreign_keys = 1;

.headers off
select '';
select '__________________________________.- Before -.__________________________________';
insert into Log (message) values ('Before');
select '--------------------------------------------------------------------------------';
.headers on

-- Verify preconditions or pre-state.

.headers off
select '';
select '_______________________________.- Change stuff -._______________________________';
insert into Log (message) values ('Change stuff');
select '--------------------------------------------------------------------------------';
.headers on

BEGIN TRANSACTION;

.headers off
-- Write your stuff here.
select 'Not changing anything, to be filled in when something is tested.';
.headers on

END TRANSACTION;

.headers off
select '';
select '__________________________________.- After -.___________________________________';
insert into Log (message) values ('After');
select '--------------------------------------------------------------------------------';
.headers on

-- Verify postconditions or post-state.

.headers off
select '';
select '__________________________________.- Log -._____________________________________';
.headers on

.width 10 23 80
select *
from Log
--where _id >= 217
order by _id;
