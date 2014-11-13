INSERT INTO Property(_id, type, name, image) VALUES(1, 1, 'Szentesi haz',  '/sdcard/DCIM/Camera/20141113_124030.jpg');
INSERT INTO Property(_id, type, name, image) VALUES(2, 11, 'Szegedi albi', '/sdcard/DCIM/Camera/20141113_124033_Áchim András St.jpg');
INSERT INTO Property(_id, type, name, image) VALUES(3, 11, 'Londoni albi', '/sdcard/DCIM/Camera/20141113_124035_Áchim András St.jpg');
INSERT INTO Property(_id, type, name, image) VALUES(4, 12, 'Sarah storage', NULL);

INSERT INTO Room_Rooter(_id, property, type, name) VALUES(0, 1,   0, '?');
INSERT INTO Room_Rooter(_id, property, type, name) VALUES(1, 1, 102, 'Nagyszoba');
INSERT INTO Room_Rooter(_id, property, type, name) VALUES(2, 1, 102, 'Robi szoba');
INSERT INTO Room_Rooter(_id, property, type, name) VALUES(3, 1, 103, 'Konyha');
INSERT INTO Room_Rooter(_id, property, type, name) VALUES(4, 1, 201, 'Spajz');
	INSERT INTO Item(_id, parent, category, name) VALUES(100005, (select root from Room where _id = 4), 3100, 'Papirdoboz kek karikakkal');
		INSERT INTO Item(_id, parent, category, name) VALUES(100006, 100005, 6200, 'Piros muanyag tanyer');
		INSERT INTO Item(_id, parent, category, name) VALUES(100007, 100005, 6400, 'Neon pohar');
			INSERT INTO Item(_id, parent, category, name) VALUES(100008, 100007, 5500, 'Viz');
INSERT INTO Room_Rooter(_id, property, type, name) VALUES(5, 1, 101, 'Furdoszoba');
INSERT INTO Room_Rooter(_id, property, type, name) VALUES(6, 1, 104, 'WC');
