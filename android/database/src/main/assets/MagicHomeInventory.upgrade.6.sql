DROP TABLE IF EXISTS Search;
CREATE VIRTUAL TABLE Search USING FTS4 (
	tokenize = unicode61 "remove_diacritics=2",
	name,
	location
);

INSERT INTO Search_Refresher (_id) SELECT _id FROM Item;
INSERT INTO Search(Search) VALUES('optimize');
