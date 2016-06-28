INSERT INTO Property(name, type) VALUES (
	'Home', (select _id from PropertyType where name = 'property_house'));
INSERT INTO Room_Rooter(property, name, type) VALUES ((select _id from Property where name = 'Home'),
		'My room', (select _id from RoomType where name = 'room_bed'));
	INSERT INTO Item(parent, name, category) VALUES((select root from Room where name = 'My room'),
		'Bed', (select _id from Category where name = 'category_lounging'));
	INSERT INTO Item(parent, name, category) VALUES((select root from Room where name = 'My room'),
		'Desk', (select _id from Category where name = 'category_surface_flat'));
	INSERT INTO Item(parent, name, category) VALUES((select root from Room where name = 'My room'),
		'Chair', (select _id from Category where name = 'category_seating'));
	INSERT INTO Item(parent, name, category) VALUES((select root from Room where name = 'My room'),
		'Wardrobe', (select _id from Category where name = 'category_cabinetry'));
INSERT INTO Room_Rooter(property, name, type) VALUES ((select _id from Property where name = 'Home'),
	'Kitchen', (select _id from RoomType where name = 'room_dining'));
	INSERT INTO Item(parent, name, category) VALUES((select root from Room where name = 'Kitchen'),
		'Fridge', (select _id from Category where name = 'category_appliance_storage'));
	INSERT INTO Item(parent, name, category) VALUES((select root from Room where name = 'Kitchen'),
		'Sink', (select _id from Category where name = 'category_fixture_wet'));
	INSERT INTO Item(parent, name, category) VALUES((select _id from Item where name = 'Sink' and parent = (select root from Room where name = 'Kitchen')),
		'Yellow kitchen sponge', (select _id from Category where name = 'category_cleaning_wet'));
INSERT INTO Room_Rooter(property, name, type) VALUES ((select _id from Property where name = 'Home'),
	'Bathroom', (select _id from RoomType where name = 'room_bath'));
	INSERT INTO Item(parent, name, category) VALUES((select root from Room where name = 'Bathroom'),
		'Sink', (select _id from Category where name = 'category_fixture_wet'));
		INSERT INTO Item(parent, name, category) VALUES((select _id from Item where name = 'Sink' and parent = (select root from Room where name = 'Bathroom')),
			'Toothbrush holder', (select _id from Category where name = 'category_fixture_storage'));
			INSERT INTO Item(parent, name, category) VALUES((select _id from Item where name = 'Toothbrush holder'),
				'Toothbrush', (select _id from Category where name = 'category_hygiene_oral'));
	INSERT INTO Item(parent, name, category) VALUES((select root from Room where name = 'Bathroom'),
		'Bathroom cabinet', (select _id from Category where name = 'category_cabinetry'));
		INSERT INTO Item(parent, name, category) VALUES((select _id from Item where name = 'Bathroom cabinet'),
			'Top shelf', (select _id from Category where name = 'category_compartment'));
		INSERT INTO Item(parent, name, category) VALUES((select _id from Item where name = 'Bathroom cabinet'),
			'Bottom shelf', (select _id from Category where name = 'category_compartment'));

INSERT INTO Recent(visit, _id) VALUES (datetime(CURRENT_TIMESTAMP),
	(select _id from Item where name = 'Sink' and parent = (select root from Room where name = 'Bathroom')));
INSERT INTO Recent(visit, _id) VALUES (datetime(CURRENT_TIMESTAMP, '-7 minutes'),
	(select _id from Item where name = 'Bed'));
INSERT INTO Recent(visit, _id) VALUES (datetime(CURRENT_TIMESTAMP, '-2 days'),
	(select _id from Item where name = 'Desk'));
INSERT INTO Recent(visit, _id) VALUES (datetime(CURRENT_TIMESTAMP, '-1 hours'),
	(select _id from Item where name = 'Wardrobe'));
INSERT INTO Recent(visit, _id) VALUES (datetime(CURRENT_TIMESTAMP, '-1 days'),
	(select _id from Item where name = 'Wardrobe'));
