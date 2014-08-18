select '-------- Search ---------';
select * from Search;

select '--------- Item_Path --------';
select * from Item_Path;

select '';

select '--------- Insert DOBOZ --------';
insert OR REPLACE into Category_Name_Cache(key, value) values('category_storage_boxes', 'DOBOZ');
select '--------- Item_Path --------';
select * from Item_Path;

select '--------- Search --------';
select * from Search;

select '--------- Log --------';
select * from Log where _id >= 128 order by _id;