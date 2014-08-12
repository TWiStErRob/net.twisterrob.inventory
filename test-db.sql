select '-------- Search ---------';
select * from Search;

select '--------- Item_Paths --------';
select * from Item_Paths;

select '';

select '--------- Insert DOBOZ --------';
insert OR REPLACE into Category_Name_Cache(key, value) values('category_storage_boxes', 'DOBOZ');
select '--------- Item_Paths --------';
select * from Item_Paths;

select '--------- Search --------';
select * from Search;

select '--------- Log --------';
select * from Log where _id >= 128 order by _id;