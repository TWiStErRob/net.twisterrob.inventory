INSERT INTO Property(id, type, name) VALUES(1, 1, 'Szentesi ház');
INSERT INTO Property(id, type, name) VALUES(2, 11, 'Szegedi albi');
INSERT INTO Property(id, type, name) VALUES(3, 11, 'Londoni albi');
INSERT INTO Property(id, type, name) VALUES(4, 12, 'Sarah storage');

INSERT INTO Item(id, category, name) VALUES(1, -1, 'ROOT');
INSERT INTO Room(id, property, type, root, name) VALUES(1, 1, 102, 1, 'Nagyszoba');

INSERT INTO Item(id, category, name) VALUES(2, -1, 'ROOT');
INSERT INTO Room(id, property, type, root,name) VALUES(2, 1, 102, 2, 'Robi szoba');

INSERT INTO Item(id, category, name) VALUES(3, -1, 'ROOT');
INSERT INTO Room(id, property, type, root,name) VALUES(3, 1, 103, 3, 'Konyha');

INSERT INTO Item(id, category, name) VALUES(4, -1, 'ROOT');
INSERT INTO Room(id, property, type, root,name) VALUES(4, 1, 201, 4, 'Spájz');

INSERT INTO Item(id, parent, category, name) VALUES(5, 4, 3100, 'Papírdoboz kék karikákkal');
	INSERT INTO Item(id, parent, category, name) VALUES(6, 5, 6200, 'Piros műanyag tányér');
	INSERT INTO Item(id, parent, category, name) VALUES(7, 5, 6400, 'Neon pohár');