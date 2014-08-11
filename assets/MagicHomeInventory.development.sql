INSERT INTO Property(_id, type, name, image) VALUES(1, 1, 'Szentesi ház',  'DriveId:CAESHDBCNXFUTlBncV9sbDVXV3hsVEU0elJUSlhUMUUY7gYg5obA7aFR');
INSERT INTO Property(_id, type, name, image) VALUES(2, 11, 'Szegedi albi', 'DriveId:CAESHDBCNXFUTlBncV9sbDVXa1l3VnpKelNWcExaVkUY8AYg5obA7aFR');
INSERT INTO Property(_id, type, name, image) VALUES(3, 11, 'Londoni albi', 'DriveId:CAESHDBCNXFUTlBncV9sbDVPVGhGWmtsdll6VjBPR3MY8gYg5obA7aFR');
INSERT INTO Property(_id, type, name) VALUES(4, 12, 'Sarah storage');

INSERT INTO Item(_id, category, name) VALUES(0, -1, 'ROOT');
INSERT INTO Room(_id, property, type, root, name) VALUES(0, 1,   0, 0, '?');

INSERT INTO Item(_id, category, name) VALUES(1, -1, 'ROOT');
INSERT INTO Room(_id, property, type, root, name) VALUES(1, 1, 102, 1, 'Nagyszoba');

INSERT INTO Item(_id, category, name) VALUES(2, -1, 'ROOT');
INSERT INTO Room(_id, property, type, root, name) VALUES(2, 1, 102, 2, 'Robi szoba');

INSERT INTO Item(_id, category, name) VALUES(3, -1, 'ROOT');
INSERT INTO Room(_id, property, type, root, name) VALUES(3, 1, 103, 3, 'Konyha');

INSERT INTO Item(_id, category, name) VALUES(4, -1, 'ROOT');
INSERT INTO Room(_id, property, type, root, name) VALUES(4, 1, 201, 4, 'Spájz');

INSERT INTO Item(_id, category, name) VALUES(5, -1, 'ROOT');
INSERT INTO Room(_id, property, type, root, name) VALUES(5, 1, 101, 5, 'Fürdőszoba');

INSERT INTO Item(_id, category, name) VALUES(6, -1, 'ROOT');
INSERT INTO Room(_id, property, type, root, name) VALUES(6, 1, 104, 6, 'WC');

INSERT INTO Item(_id, parent, category, name) VALUES(100005, 4, 3100, 'Papírdoboz kék karikákkal');
	INSERT INTO Item(_id, parent, category, name) VALUES(100006, 100005, 6200, 'Piros műanyag tányér');
	INSERT INTO Item(_id, parent, category, name) VALUES(100007, 100005, 6400, 'Neon pohár');
