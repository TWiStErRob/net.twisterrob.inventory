select '';
select '--------- Before --------';
--select * from Search order by _id;
select * from Item;

select '';
select '--------- Change stuff --------';
select * from Room where _id = 4;
delete from Room where _id = 4;

select '';
select '--------- After --------';
--select * from Search order by _id;
select * from Item;

select '';
select '--------- Check Log --------';
select * from Log
--where _id >= 20
order by _id;
