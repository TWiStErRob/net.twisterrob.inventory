DROP TRIGGER IF EXISTS Property_image;
CREATE TRIGGER Property_image
AFTER UPDATE OF image ON Property
	WHEN old.image <> new.image or ((old.image IS NULL) <> (new.image IS NULL))
BEGIN
	delete from Image where _id = old.image;--NOTEOS
END;

DROP TRIGGER IF EXISTS Property_delete;
CREATE TRIGGER Property_delete
AFTER DELETE ON Property
BEGIN
	delete from Image where _id = old.image;--NOTEOS
END;

DROP TRIGGER IF EXISTS Room_image;
CREATE TRIGGER Room_image
AFTER UPDATE OF image ON Room
	WHEN old.image <> new.image or ((old.image IS NULL) <> (new.image IS NULL))
BEGIN
	delete from Image where _id = old.image;--NOTEOS
END;

DROP TRIGGER IF EXISTS Room_delete_root;
CREATE TRIGGER Room_delete
AFTER DELETE ON Room
BEGIN
	delete from Item where _id = old.root;--NOTEOS
	delete from Image where _id = old.image;--NOTEOS
END;

DROP TRIGGER IF EXISTS Item_delete; 
CREATE TRIGGER Item_delete
AFTER DELETE ON Item
BEGIN
	delete from Image where _id = old.image;--NOTEOS
	insert into Item_Path_Node_Refresher(_id) values (old._id);--NOTEOS
END;

-- Deleting 2000 belongings takes a long time, now with this new trigger:
-- 38 seconds with secure_delete off.
-- 30 seconds with secure_delete on. (weird)
-- Before it was 2 seconds for the same amount, that's what we get for removing images
