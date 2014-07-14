INSERT INTO PropertyType
                (id, priority, name,           image)
	      SELECT  0,     1000, 'property_other', 'property_home'
	UNION SELECT  1,        0, 'property_home', 'property_home'
	UNION SELECT  2,        0, 'property_workplace'   , 'property_home'
	UNION SELECT  3,       10, 'Apartment'   , 'property_home'
	UNION SELECT  4,       10, 'Townhouse'   , 'property_home'
	UNION SELECT  5,       15, 'Condo(minium)'   , 'property_home'
	UNION SELECT  6,       15, 'Cottage'   , 'property_home'
	UNION SELECT  7,       20, 'Farm'   , 'property_home'
	UNION SELECT  8,       20, 'Vacation Home'   , 'property_home'
	UNION SELECT  9,       20, 'Villa'   , 'property_home'
	UNION SELECT 10,       30, 'Camp'   , 'property_home'
	UNION SELECT 11,       40, 'Rent'   , 'property_home'
	UNION SELECT 12,       40, 'Storage'   , 'property_home'
;

INSERT INTO RoomTypeKind
                (id, priority, name,       image)
	      SELECT  0,     1000, 'Other', 'room_bedroom'
	UNION SELECT  1,      100, 'General', 'room_bedroom'
	UNION SELECT  2,      200, 'Storage', 'room_storage'
	UNION SELECT  3,      300, 'Common', 'room_bedroom'
	UNION SELECT  4,      400, 'Function', 'room_bedroom'
	UNION SELECT  5,      500, 'Space', 'room_bedroom'
;

INSERT INTO RoomType
               (id,  kind, priority, name, image)
          SELECT 0,      0,     1000, 'Other', NULL
-- General
	UNION SELECT 101,     1,        0, 'Bathroom', NULL
	UNION SELECT 102,     1,        0, 'Bedroom (Master)', 'room_bedroom'
	UNION SELECT 103,     1,        0, 'Bedroom', 'room_bedroom'
	UNION SELECT 104,     1,        0, 'Kitchen', 'room_kitchen'
	UNION SELECT 105,     1,        0, 'Restroom', NULL
-- Storage
	UNION SELECT 201,     2,        0, 'Storage Room', NULL
	UNION SELECT 202,     2,        0, 'Closet', NULL
	UNION SELECT 203,     2,        0, 'Garage', NULL
	UNION SELECT 204,     2,        0, 'Furnace Room', NULL
	UNION SELECT 205,     2,        0, 'Shed', NULL
	UNION SELECT 206,     2,        0, 'Attic', NULL
	UNION SELECT 207,     2,        0, 'Basement', NULL
	UNION SELECT 208,     2,        0, 'Cellar', NULL
	UNION SELECT 209,     2,        0, 'Wine Cellar', NULL
-- Common
	UNION SELECT 301,     3,        0, 'Living Room', NULL
	UNION SELECT 302,     3,        0, 'Family Room', NULL
	UNION SELECT 303,     3,        0, 'Play Room', NULL
-- Function
	UNION SELECT 401,     4,        0, 'Dining Room', NULL
	UNION SELECT 402,     4,        0, 'Library', NULL
	UNION SELECT 403,     4,        0, 'Office', NULL
	UNION SELECT 404,     4,        0, 'Gym', NULL
	UNION SELECT 405,     4,        0, 'TV Room', NULL
	UNION SELECT 406,     4,        0, 'Pool House', NULL
	UNION SELECT 407,     4,        0, 'Laundry Room', NULL
-- Spaces
	UNION SELECT 501,     5,        0, 'Balcony', NULL
	UNION SELECT 502,     5,        0, 'Garden', NULL
	UNION SELECT 503,     5,        0, 'Lobby', NULL
	UNION SELECT 504,     5,        0, 'Deck', NULL
	UNION SELECT 505,     5,        0, 'Patio', NULL
	UNION SELECT 506,     5,        0, 'Crawl Space', NULL
;


INSERT INTO Category
	               (id, parent, name)
--		               (id, parent, name)
--			               (id, parent, name)
	      SELECT     0,   NULL, 'Uncategorized'
	UNION SELECT  1000,   NULL, 'Clothing'
	UNION SELECT  1100,   1000,   'Clothes'
	UNION SELECT  1200,   1000,   'Underwear'
	UNION SELECT  1300,   1000,   'Footwear'
	UNION SELECT  1400,   1000,   'Coats'
	UNION SELECT  1500,   1000,   'Accessories'
	UNION SELECT  1510,   1500,     'Jewelry'
	UNION SELECT  1520,   1500,     'Hats'
	UNION SELECT  1530,   1500,     'Gloves'
	UNION SELECT  1540,   1500,     'Scarves'
	UNION SELECT  2000,   NULL, 'Luggage'
		UNION SELECT  2100,   2000, 'Backpacks'
		UNION SELECT  2200,   2000, 'Bags'
		UNION SELECT  2300,   2000, 'Suitcases'
		UNION SELECT  2400,   2000, 'Trunks'
	UNION SELECT  3000,   NULL, 'Storage'
		UNION SELECT  3100,   3000, 'Boxes'
		UNION SELECT  3200,   3000, 'Baskets'
		UNION SELECT  3300,   3000, 'Crates'
		UNION SELECT  3400,   3000, 'Disc Cases'
	UNION SELECT  4000,   NULL, 'Collectibles'
		UNION SELECT  4100,   4000, 'Albums'
		UNION SELECT  4200,   4000, 'Artwork'
		UNION SELECT  4300,   4000, 'Antiques'
		UNION SELECT  4400,   4000, 'Magazines'
		UNION SELECT  4500,   4000, 'Books'
		UNION SELECT  4600,   4000, 'Recordings'
			UNION SELECT  4610,  4600, 'CDs'
			UNION SELECT  4620,  4600, 'DVDs'
			UNION SELECT  4630,  4600, 'Blu-Rays'
			UNION SELECT  4640,  4600, 'Tapes'
		UNION SELECT  4700,   4000, 'Souvenirs'
	UNION SELECT  5000,   NULL, 'Consumables'
		UNION SELECT  5100,   5000, 'Food'
		UNION SELECT  5200,   5000, 'Ingredients'
		UNION SELECT  5300,   5000, 'Seasoning'
		UNION SELECT  5400,   5000, 'Conserves'
		UNION SELECT  5500,   5000, 'Liquids'
	UNION SELECT  6000,   NULL, 'Kitchenware'
		UNION SELECT  6100,   6000, 'Cutlery/Silverware'
		UNION SELECT  6200,   6000, 'Dishes'
		UNION SELECT  6300,   6000, 'Pans'
		UNION SELECT  6400,   6000, 'Glasses'
	UNION SELECT  7000,   NULL, 'Cleaning'
		UNION SELECT  7100,   7000, 'Chemicals'
		UNION SELECT  7200,   7000, 'Equipment'
	UNION SELECT  8000,   NULL, 'Decorations'
		UNION SELECT  8100,   8000, 'Lighting'
		UNION SELECT  8200,   8000, 'Vase'
		UNION SELECT  8300,   8000, 'Event'
			UNION SELECT  8310,  8300, 'Birthday'
			UNION SELECT  8320,  8300, 'Wedding'
		UNION SELECT  8400,   8000, 'Seasonal'
			UNION SELECT  8410,  8400, 'Easter'
			UNION SELECT  8420,  8400, 'Christmas'
		UNION SELECT  8500,   8000, 'Cultural'
	UNION SELECT  9000,   NULL, 'Furnishings'
		UNION SELECT  9100,   9000, 'Fixtures'
		UNION SELECT  9200,   9000, 'Bedding'
			UNION SELECT  9210,  9200, 'Covers'
			UNION SELECT  9220,  9200, 'Douvets'
			UNION SELECT  9230,  9200, 'Bedsheets'
			UNION SELECT  9240,  9200, 'Blankets'
			UNION SELECT  9250,  9200, 'Pillows'
		UNION SELECT  9300,   9000, 'Curtains/Draperies'
		UNION SELECT  9400,   9000, 'Linens'
		UNION SELECT  9500,   9000, 'Towels'
		UNION SELECT  9600,   9000, 'Rugs'
;

INSERT INTO Category
	               (id, parent, name)
	      SELECT 10000,   NULL, 'Furnitures'
		UNION SELECT 10100,  10000, 'Cupboard/Cabinet/Wardrobe'
		UNION SELECT 10200,  10000, 'Desk/Table'
		UNION SELECT 10300,  10000, 'Stand'
		UNION SELECT 10400,  10000, 'Seating'
		UNION SELECT 10500,  10000, 'Garden'
		UNION SELECT 10600,  10000, 'Bathroom'
		UNION SELECT 10700,  10000, 'Kitchen'
	UNION SELECT 11000,   NULL, 'Stationery'
		UNION SELECT 11100, 11000, 'Painting'
		UNION SELECT 11200, 11000, 'Drawing'
		UNION SELECT 11300, 11000, 'Writing'
		UNION SELECT 11400, 11000, 'Paper'
	UNION SELECT 12000,   NULL, 'Tools'
		UNION SELECT 12100, 12000, 'Utility'
		UNION SELECT 12200, 12000, 'Gardening'
	UNION SELECT 13000,   NULL, 'Toys'
		UNION SELECT 13100, 13000, 'Soft/Plush'
		UNION SELECT 13200, 13000, 'Dolls/Action Figures'
		UNION SELECT 13300, 13000, 'Tabletop Games'
		UNION SELECT 13400, 13000, 'Puzzlers'
		UNION SELECT 13500, 13000, 'Construction'
		UNION SELECT 13600, 13000, 'Outdoors'
	UNION SELECT 14000,  NULL, 'Documents'
		UNION SELECT 14100, 14000, 'Archives'
		UNION SELECT 14200, 14000, 'Statements'
	UNION SELECT 15000,  NULL, 'Sports'
		UNION SELECT 15100, 15000, 'Clothing'
		UNION SELECT 15200, 15000, 'Accessories'
		UNION SELECT 15300, 15000, 'Equipment'
	UNION SELECT 16000,  NULL, 'Electronics'
		UNION SELECT 16100, 16000, 'Appliances'
		UNION SELECT 16200, 16000, 'Cables'
		UNION SELECT 16300, 16000, 'Cameras/Recorders'
		UNION SELECT 16400, 16000, 'PLayback Devices'
		UNION SELECT 16500, 16000, 'Displays'
			UNION SELECT 16510, 16500, 'Monitor'
			UNION SELECT 16520, 16500, 'TV'
			UNION SELECT 16530, 16500, 'Projector'
		UNION SELECT 16600, 16000, 'Computers'
			UNION SELECT 16610, 16600, 'Components'
			UNION SELECT 16620, 16600, 'Peripherials'
			UNION SELECT 16630, 16600, 'Data Storage'
		UNION SELECT 16700, 16000, 'Game Consoles'
	UNION SELECT 17000,   NULL, 'Instruments'
	UNION SELECT 18000,   NULL, 'Vehicles'
		UNION SELECT 18100, 18000, 'Car'
		UNION SELECT 18200, 18000, 'Bicycle'
		UNION SELECT 18300, 18000, 'Motocycle'
		UNION SELECT 18400, 18000, 'Large Vehicle'
;
