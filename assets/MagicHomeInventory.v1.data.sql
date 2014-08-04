INSERT INTO PropertyType
               (_id, priority, name,                 image)
	      SELECT  0,     1000, 'property_other',     'property_home'
	UNION SELECT  1,        0, 'property_home',      'property_home'
	UNION SELECT  2,        0, 'property_workplace', 'property_home'
	UNION SELECT  3,       10, 'property_apartment', 'property_home'
	UNION SELECT  4,       10, 'property_house',     'property_home'
	UNION SELECT  5,       15, 'property_condo',     'property_home'
	UNION SELECT  6,       15, 'property_cottage',   'property_home'
	UNION SELECT  7,       20, 'property_farm',      'property_home'
	UNION SELECT  8,       20, 'property_vacation',  'property_home'
	UNION SELECT  9,       20, 'property_villa',     'property_home'
	UNION SELECT 10,       30, 'property_camp',      'property_home'
	UNION SELECT 11,       40, 'property_rent',      'property_home'
	UNION SELECT 12,       40, 'property_storage',   'property_home'
;

INSERT INTO RoomTypeKind
               (_id, priority, name,                  image)
	      SELECT  0,     1000, 'room_group_other',    'room_bedroom'
	UNION SELECT  1,      100, 'room_group_general',  'room_bedroom'
	UNION SELECT  2,      200, 'room_group_storage',  'room_storage'
	UNION SELECT  3,      300, 'room_group_common',   'room_bedroom'
	UNION SELECT  4,      400, 'room_group_function', 'room_bedroom'
	UNION SELECT  5,      500, 'room_group_space',    'room_bedroom'
;

INSERT INTO RoomType
                (_id,  kind, priority, name,            image)
          SELECT   0,     0,     1000, 'room_other',    NULL
-- General
	UNION SELECT 101,     1,        0, 'room_bath',     NULL
	UNION SELECT 102,     1,        0, 'room_bed',      'room_bedroom'
	UNION SELECT 103,     1,        0, 'room_kitchen',  'room_kitchen'
	UNION SELECT 104,     1,        0, 'room_WC',       NULL
-- Storage
	UNION SELECT 201,     2,        0, 'room_storage',  NULL
	UNION SELECT 202,     2,        0, 'room_closet',   NULL
	UNION SELECT 203,     2,        0, 'room_garage',   NULL
	UNION SELECT 204,     2,        0, 'room_furnace',  NULL
	UNION SELECT 205,     2,        0, 'room_shed',     NULL
	UNION SELECT 206,     2,        0, 'room_attic',    NULL
	UNION SELECT 207,     2,        0, 'room_basement', NULL
	UNION SELECT 208,     2,        0, 'room_cellar',   NULL
-- Common
	UNION SELECT 301,     3,        0, 'room_living',   NULL
	UNION SELECT 302,     3,        0, 'room_family',   NULL
	UNION SELECT 303,     3,        0, 'room_play',     NULL
-- Function
	UNION SELECT 401,     4,        0, 'room_dining',   NULL
	UNION SELECT 402,     4,        0, 'room_library',  NULL
	UNION SELECT 403,     4,        0, 'room_office',   NULL
	UNION SELECT 404,     4,        0, 'room_gym',      NULL
	UNION SELECT 405,     4,        0, 'room_TV',       NULL
	UNION SELECT 406,     4,        0, 'room_pool',     NULL
	UNION SELECT 407,     4,        0, 'room_laundy',   NULL
-- Spaces
	UNION SELECT 501,     5,        0, 'room_balcony',  NULL
	UNION SELECT 502,     5,        0, 'room_garden',   NULL
	UNION SELECT 503,     5,        0, 'room_lobby',    NULL
	UNION SELECT 504,     5,        0, 'room_deck',     NULL
	UNION SELECT 505,     5,        0, 'room_patio',    NULL
	UNION SELECT 506,     5,        0, 'room_crawl',    NULL
;


INSERT INTO Category
	              (_id, parent, name)
		--            (_id, parent, name)
			--            (_id, parent, name)
	      SELECT    -1,   NULL, 'INTERNAL'
	UNION SELECT     0,     -1, 'Uncategorized'
	UNION SELECT  1000,     -1, 'Clothing'
	UNION SELECT  1100,   1000,   'Clothes'
	UNION SELECT  1200,   1000,   'Underwear'
	UNION SELECT  1300,   1000,   'Footwear'
	UNION SELECT  1400,   1000,   'Coats'
	UNION SELECT  1500,   1000,   'Accessories'
	UNION SELECT  1510,   1500,     'Jewelry'
	UNION SELECT  1520,   1500,     'Hats'
	UNION SELECT  1530,   1500,     'Gloves'
	UNION SELECT  1540,   1500,     'Scarves'
	UNION SELECT  2000,     -1, 'Luggage'
		UNION SELECT  2100,   2000, 'Backpacks'
		UNION SELECT  2200,   2000, 'Bags'
		UNION SELECT  2300,   2000, 'Suitcases'
		UNION SELECT  2400,   2000, 'Trunks'
	UNION SELECT  3000,     -1, 'Storage'
		UNION SELECT  3100,   3000, 'Boxes'
		UNION SELECT  3200,   3000, 'Baskets'
		UNION SELECT  3300,   3000, 'Crates'
		UNION SELECT  3400,   3000, 'Disc Cases'
	UNION SELECT  4000,     -1, 'Collectibles'
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
	UNION SELECT  5000,     -1, 'Consumables'
		UNION SELECT  5100,   5000, 'Food'
		UNION SELECT  5200,   5000, 'Ingredients'
		UNION SELECT  5300,   5000, 'Seasoning'
		UNION SELECT  5400,   5000, 'Conserves'
		UNION SELECT  5500,   5000, 'Liquids'
	UNION SELECT  6000,     -1, 'Kitchenware'
		UNION SELECT  6100,   6000, 'Cutlery/Silverware'
		UNION SELECT  6200,   6000, 'Dishes'
		UNION SELECT  6300,   6000, 'Pans'
		UNION SELECT  6400,   6000, 'Glasses'
	UNION SELECT  7000,     -1, 'Cleaning'
		UNION SELECT  7100,   7000, 'Chemicals'
		UNION SELECT  7200,   7000, 'Equipment'
	UNION SELECT  8000,     -1, 'Decorations'
		UNION SELECT  8100,   8000, 'Lighting'
		UNION SELECT  8200,   8000, 'Vase'
		UNION SELECT  8300,   8000, 'Event'
			UNION SELECT  8310,  8300, 'Birthday'
			UNION SELECT  8320,  8300, 'Wedding'
		UNION SELECT  8400,   8000, 'Seasonal'
			UNION SELECT  8410,  8400, 'Easter'
			UNION SELECT  8420,  8400, 'Christmas'
		UNION SELECT  8500,   8000, 'Cultural'
	UNION SELECT  9000,     -1, 'Furnishings'
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
	              (_id, parent, name)
	      SELECT 10000,     -1, 'Furnitures' -- http://en.wikipedia.org/wiki/List_of_furniture_types
		UNION SELECT 10100,  10000, 'Cupboard/Cabinet/Wardrobe'
		UNION SELECT 10200,  10000, 'Desk/Table'
		UNION SELECT 10300,  10000, 'Stand'
		UNION SELECT 10400,  10000, 'Seating'
		UNION SELECT 10500,  10000, 'Garden'
		UNION SELECT 10600,  10000, 'Bathroom'
		UNION SELECT 10700,  10000, 'Kitchen'
		UNION SELECT 10800,  10000, 'Compartments'
			UNION SELECT 10810,  10800, 'Drawer'
			UNION SELECT 10810,  10800, 'Shelf'
			UNION SELECT 10810,  10800, 'Section'
			UNION SELECT 10810,  10800, 'Display'
	UNION SELECT 11000,     -1, 'Stationery'
		UNION SELECT 11100, 11000, 'Painting'
		UNION SELECT 11200, 11000, 'Drawing'
		UNION SELECT 11300, 11000, 'Writing'
		UNION SELECT 11400, 11000, 'Paper'
	UNION SELECT 12000,     -1, 'Tools'
		UNION SELECT 12100, 12000, 'Utility'
		UNION SELECT 12200, 12000, 'Gardening'
	UNION SELECT 13000,     -1, 'Toys'
		UNION SELECT 13100, 13000, 'Soft/Plush'
		UNION SELECT 13200, 13000, 'Dolls/Action Figures'
		UNION SELECT 13300, 13000, 'Tabletop Games'
		UNION SELECT 13400, 13000, 'Puzzlers'
		UNION SELECT 13500, 13000, 'Construction'
		UNION SELECT 13600, 13000, 'Outdoors'
	UNION SELECT 14000,    -1, 'Documents'
		UNION SELECT 14100, 14000, 'Archives'
		UNION SELECT 14200, 14000, 'Statements'
	UNION SELECT 15000,    -1, 'Sports'
		UNION SELECT 15100, 15000, 'Clothing'
		UNION SELECT 15200, 15000, 'Accessories'
		UNION SELECT 15300, 15000, 'Equipment'
	UNION SELECT 16000,    -1, 'Electronics'
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
	UNION SELECT 17000,     -1, 'Instruments'
	UNION SELECT 18000,     -1, 'Vehicles'
		UNION SELECT 18100, 18000, 'Car'
		UNION SELECT 18200, 18000, 'Bicycle'
		UNION SELECT 18300, 18000, 'Motocycle'
		UNION SELECT 18400, 18000, 'Large Vehicle'
;
