INSERT INTO Property(_id, type, name, image) VALUES(1, 1, 'Szentesi haz',  'DriveId:CAESHDBCNXFUTlBncV9sbDVXV3hsVEU0elJUSlhUMUUY7gYg5obA7aFR');
	INSERT INTO Room_Rooter(_id, property, type, name) VALUES(0, 1,   0, '?');
	INSERT INTO Room_Rooter(_id, property, type, name) VALUES(1, 1, 102, 'Nagyszoba');
	INSERT INTO Room_Rooter(_id, property, type, name) VALUES(2, 1, 102, 'Robi szoba');
	INSERT INTO Room_Rooter(_id, property, type, name) VALUES(3, 1, 103, 'Konyha');
	INSERT INTO Room_Rooter(_id, property, type, name) VALUES(4, 1, 201, 'Spajz');
		INSERT INTO Item(_id, parent, category, name) VALUES(100005, last_insert_rowid(), 3100, 'Papirdoboz kek karikakkal');
			INSERT INTO Item(_id, parent, category, name) VALUES(100006, 100005, 6200, 'Piros muanyag tanyer');
			INSERT INTO Item(_id, parent, category, name) VALUES(100007, 100005, 6400, 'Neon pohar');
	INSERT INTO Room_Rooter(_id, property, type, name) VALUES(5, 1, 101, 'Furdoszoba');
	INSERT INTO Room_Rooter(_id, property, type, name) VALUES(6, 1, 104, 'WC');

INSERT INTO Property(_id, type, name, image) VALUES(2, 11, 'Szegedi albi', 'DriveId:CAESHDBCNXFUTlBncV9sbDVXa1l3VnpKelNWcExaVkUY8AYg5obA7aFR');

INSERT INTO Property(_id, type, name, image) VALUES(3, 11, 'Londoni albi', 'DriveId:CAESHDBCNXFUTlBncV9sbDVPVGhGWmtsdll6VjBPR3MY8gYg5obA7aFR');

INSERT INTO Property(_id, type, name, image) VALUES(4, 12, 'Sarah storage', NULL);
