INSERT INTO PropertyType
	           (_id, priority, name,                 image)
	      SELECT  0,     1000, 'property_other',     'property_unknown'
	UNION SELECT  1,        0, 'property_home',      'property_home'
	UNION SELECT  2,        0, 'property_workplace', 'property_office'
	UNION SELECT  3,       10, 'property_apartment', 'property_office'
	UNION SELECT  4,       10, 'property_house',     'property_home'
	UNION SELECT  5,       15, 'property_condo',     'property_unknown'
	UNION SELECT  6,       15, 'property_cottage',   'property_home'
	UNION SELECT  7,       20, 'property_farm',      'property_unknown'
	UNION SELECT  8,       20, 'property_vacation',  'property_unknown'
	UNION SELECT  9,       20, 'property_villa',     'property_unknown'
	UNION SELECT 10,       30, 'property_camp',      'property_unknown'
	UNION SELECT 11,       40, 'property_rent',      'property_rent'
	UNION SELECT 12,       40, 'property_storage',   'room_storage'
;

INSERT INTO RoomTypeKind
	           (_id, priority, name,                  image)
	      SELECT  0,     1000, 'room_group_other',    'room_unknown'
	UNION SELECT  1,      100, 'room_group_general',  'room_unknown'
	UNION SELECT  2,      200, 'room_group_storage',  'room_storage'
	UNION SELECT  3,      300, 'room_group_common',   'room_unknown'
	UNION SELECT  4,      400, 'room_group_function', 'room_unknown'
	UNION SELECT  5,      500, 'room_group_space',    'room_unknown'
;

INSERT INTO RoomType
	            (_id,  kind, priority, name,            image)
	      SELECT   0,     0,     1000, 'room_other',    NULL
-- General
	UNION SELECT 101,     1,        0, 'room_bath',     'room_bathroom'
	UNION SELECT 102,     1,        0, 'room_bed',      'room_bedroom'
	UNION SELECT 103,     1,        0, 'room_kitchen',  'room_kitchen'
	UNION SELECT 104,     1,        0, 'room_toilet',   'room_toilet'
-- Storage
	UNION SELECT 201,     2,        0, 'room_storage',  'room_storage'
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
	              (_id, parent, name,                                      image)
	      SELECT    -1,   NULL, 'category_internal',                       'category_unknown'
	UNION SELECT     0,     -1, 'category_uncategorized',                  'category_unknown'
	UNION SELECT  1000,     -1, 'category_clothing',                       'category_unknown'
	UNION SELECT  1100,   1000,     'category_clothing_clothes',           'category_unknown'
	UNION SELECT  1200,   1000,     'category_clothing_underwear',         'category_unknown'
	UNION SELECT  1300,   1000,     'category_clothing_footwear',          'category_unknown'
	UNION SELECT  1400,   1000,     'category_clothing_coats',             'category_unknown'
	UNION SELECT  1500,   1000,     'category_accessories',                'category_unknown'
	UNION SELECT  1510,   1500,         'category_accessory_jewelry',      'category_unknown'
	UNION SELECT  1520,   1500,         'category_accessory_hats',         'category_unknown'
	UNION SELECT  1530,   1500,         'category_accessory_gloves',       'category_unknown'
	UNION SELECT  1540,   1500,         'category_accessory_scarves',      'category_unknown'
	UNION SELECT  2000,     -1, 'category_luggage',                        'category_luggage_suitcase'
	UNION SELECT  2100,   2000,     'category_luggage_backpacks',          'category_luggage_suitcase'
	UNION SELECT  2200,   2000,     'category_luggage_bags',               'category_luggage_suitcase'
	UNION SELECT  2300,   2000,     'category_luggage_suitcases',          'category_luggage_suitcase'
	UNION SELECT  2400,   2000,     'category_luggage_trunks',             'category_luggage_suitcase'
	UNION SELECT  3000,     -1, 'category_storage',                        'category_storage_box'
	UNION SELECT  3100,   3000,     'category_storage_boxes',              'category_storage_box'
	UNION SELECT  3200,   3000,     'category_storage_baskets',            'category_storage_box'
	UNION SELECT  3300,   3000,     'category_storage_crates',             'category_storage_box'
	UNION SELECT  3400,   3000,     'category_storage_disccases',          'category_storage_box'
	UNION SELECT  4000,     -1, 'category_collectibles',                   'category_collectibles'
	UNION SELECT  4100,   4000,     'category_collectible_albums',         'category_collectibles'
	UNION SELECT  4200,   4000,     'category_collectible_artwork',        'category_collectibles'
	UNION SELECT  4300,   4000,     'category_collectible_antiques',       'category_collectibles'
	UNION SELECT  4400,   4000,     'category_collectible_magazines',      'category_collectibles'
	UNION SELECT  4500,   4000,     'category_collectible_books',          'category_collectibles'
	UNION SELECT  4600,   4000,     'category_collectible_recordings',     'category_disc'
	UNION SELECT  4610,   4600,         'category_collectible_cds',        'category_disc'
	UNION SELECT  4620,   4600,         'category_collectible_dvds',       'category_disc'
	UNION SELECT  4630,   4600,         'category_collectible_blurays',    'category_disc'
	UNION SELECT  4640,   4600,         'category_collectible_vinyls',     'category_disc'
	UNION SELECT  4650,   4600,         'category_collectible_tapes',      'category_tape'
	UNION SELECT  4660,   4600,         'category_collectible_cassettes',  'category_tape'
	UNION SELECT  4700,   4000,     'category_collectible_souvenirs',      'category_collectibles'
	UNION SELECT  5000,     -1, 'category_consumables',                    'category_unknown'
	UNION SELECT  5100,   5000,     'category_consumable_food',            'category_unknown'
	UNION SELECT  5200,   5000,     'category_consumable_ingredients',     'category_unknown'
	UNION SELECT  5300,   5000,     'category_consumable_seasoning',       'category_unknown'
	UNION SELECT  5400,   5000,     'category_consumable_conserves',       'category_unknown'
	UNION SELECT  5500,   5000,     'category_consumable_liquids',         'category_unknown'
	UNION SELECT  6000,     -1, 'category_kitchenware',                    'room_kitchen'
	UNION SELECT  6100,   6000,     'category_kitchen_cutlery',            'room_kitchen'
	UNION SELECT  6200,   6000,     'category_kitchen_dishes',             'room_kitchen'
	UNION SELECT  6300,   6000,     'category_kitchen_pans',               'room_kitchen'
	UNION SELECT  6400,   6000,     'category_kitchen_glasses',            'room_kitchen'
	UNION SELECT  7000,      -1, 'category_cleaning',                      'category_unknown'
	UNION SELECT  7100,   7000,     'category_cleaning_chemicals',         'category_unknown'
	UNION SELECT  7200,   7000,     'category_cleaning_equipment',         'category_unknown'
	UNION SELECT  8000,     -1, 'category_decorations',                    'category_unknown'
	UNION SELECT  8100,   8000,     'category_decor_lighting',             'category_unknown'
	UNION SELECT  8200,   8000,     'category_decor_vase',                 'category_unknown'
	UNION SELECT  8300,   8000,     'category_decor_event',                'category_unknown'
	UNION SELECT  8310,   8300,         'category_decor_birthday',         'category_unknown'
	UNION SELECT  8320,   8300,         'category_decor_wedding',          'category_unknown'
	UNION SELECT  8400,   8000,     'category_decor_seasonal',             'category_unknown'
	UNION SELECT  8410,   8400,         'category_decor_easter',           'category_unknown'
	UNION SELECT  8420,   8400,         'category_decor_christmas',        'category_unknown'
	UNION SELECT  8500,   8000,     'category_decor_cultural',             'category_unknown'
	UNION SELECT  9000,     -1, 'category_furnishings',                    'category_unknown'
	UNION SELECT  9100,   9000,     'category_furnishing_fixtures',        'category_unknown'
	UNION SELECT  9200,   9000,     'category_furnishing_bedding',         'category_unknown'
	UNION SELECT  9210,   9200,         'category_furnishing_covers',      'category_unknown'
	UNION SELECT  9220,   9200,         'category_furnishing_douvets',     'category_unknown'
	UNION SELECT  9230,   9200,         'category_furnishing_bedsheets',   'category_unknown'
	UNION SELECT  9240,   9200,         'category_furnishing_blankets',    'category_unknown'
	UNION SELECT  9250,   9200,         'category_furnishing_pillows',     'category_unknown'
	UNION SELECT  9300,   9000,     'category_furnishing_curtains',        'category_unknown'
	UNION SELECT  9400,   9000,     'category_furnishing_linens',          'category_unknown'
	UNION SELECT  9500,   9000,     'category_furnishing_towels',          'category_unknown'
	UNION SELECT  9600,   9000,     'category_furnishing_rugs',            'category_unknown'
;

INSERT INTO Category
	              (_id, parent, name,                                      image)
	      SELECT 10000,     -1, 'category_furniture',                      'category_unknown' -- http://en.wikipedia.org/wiki/List_of_furniture_types
	UNION SELECT 10100,  10000,     'category_furniture_cabinetry',        'category_unknown'
	UNION SELECT 10200,  10000,     'category_furniture_table',            'category_unknown'
	UNION SELECT 10300,  10000,     'category_furniture_stand',            'category_unknown'
	UNION SELECT 10400,  10000,     'category_furniture_seating',          'category_unknown'
	UNION SELECT 10500,  10000,     'category_furniture_garden',           'category_unknown'
	UNION SELECT 10600,  10000,     'category_furniture_bathroom',         'category_unknown'
	UNION SELECT 10700,  10000,     'category_furniture_kitchen',          'category_unknown'
	UNION SELECT 10800,  10000,     'category_furniture_compartments',     'category_unknown'
	UNION SELECT 10810,  10800,         'category_furniture_drawer',       'category_unknown'
	UNION SELECT 10820,  10800,         'category_furniture_shelf',        'category_unknown'
	UNION SELECT 10830,  10800,         'category_furniture_section',      'category_unknown'
	UNION SELECT 10840,  10800,         'category_furniture_display',      'category_unknown'
	UNION SELECT 11000,     -1, 'category_stationery',                     'category_paper'
	UNION SELECT 11100,  11000,     'category_stationery_painting',        'category_paint'
	UNION SELECT 11200,  11000,     'category_stationery_drawing',         'category_pencil'
	UNION SELECT 11300,  11000,     'category_stationery_writing',         'category_pen'
	UNION SELECT 11400,  11000,     'category_stationery_paper',           'category_paper'
	UNION SELECT 12000,     -1, 'category_tools',                          'category_unknown'
	UNION SELECT 12100,  12000,     'category_tools_utility',              'category_unknown'
	UNION SELECT 12200,  12000,     'category_tools_gardening',            'category_unknown'
	UNION SELECT 13000,     -1, 'category_toys',                           'category_unknown'
	UNION SELECT 13100,  13000,     'category_toys_soft',                  'category_unknown'
	UNION SELECT 13200,  13000,     'category_toys_dolls',                 'category_unknown'
	UNION SELECT 13300,  13000,     'category_toys_tabletop',              'category_unknown'
	UNION SELECT 13400,  13000,     'category_toys_puzzlers',              'category_unknown'
	UNION SELECT 13500,  13000,     'category_toys_construction',          'category_unknown'
	UNION SELECT 13600,  13000,     'category_toys_outdoors',              'category_unknown'
	UNION SELECT 14000,     -1, 'category_documents',                      'category_paper'
	UNION SELECT 14100,  14000,     'category_document_archives',          'category_paper'
	UNION SELECT 14200,  14000,     'category_document_statements',        'category_paper'
	UNION SELECT 15000,     -1, 'category_sports',                         'category_unknown'
	UNION SELECT 15100,  15000,     'category_sport_clothing',             'category_unknown'
	UNION SELECT 15200,  15000,     'category_sport_accessories',          'category_unknown'
	UNION SELECT 15300,  15000,     'category_sport_equipment',            'category_unknown'
	UNION SELECT 16000,     -1, 'category_electronics',                    'category_electric'
	UNION SELECT 16100,  16000,     'category_electronic_appliances',      'category_electric'
	UNION SELECT 16200,  16000,     'category_electronic_cables',          'category_electric'
	UNION SELECT 16300,  16000,     'category_electronic_recorders',       'category_electric'
	UNION SELECT 16400,  16000,     'category_electronic_playbacks',       'category_disc'
	UNION SELECT 16500,  16000,     'category_electronic_displays',        'category_electric'
	UNION SELECT 16510,  16500,         'category_electronic_monitors',    'category_electric'
	UNION SELECT 16520,  16500,         'category_electronic_tv',          'category_electric'
	UNION SELECT 16530,  16500,         'category_electronic_projector',   'category_electric'
	UNION SELECT 16600,  16000,     'category_computers',                  'category_electric'
	UNION SELECT 16610,  16600,         'category_computer_components',    'category_electric'
	UNION SELECT 16620,  16600,         'category_computer_peripherials',  'category_electric'
	UNION SELECT 16630,  16600,         'category_computer_datastorage',   'category_electric'
	UNION SELECT 16700,  16000,     'category_computer_gameconsoles',      'category_electric'
	UNION SELECT 17000,     -1, 'category_instruments',                    'category_unknown'
	UNION SELECT 18000,     -1, 'category_vehicles',                       'category_unknown'
	UNION SELECT 18100,  18000,     'category_vehicle_cars',               'category_unknown'
	UNION SELECT 18200,  18000,     'category_vehicle_bicycles',           'category_unknown'
	UNION SELECT 18300,  18000,     'category_vehicle_motocycles',         'category_unknown'
	UNION SELECT 18400,  18000,     'category_vehicle_lgv',                'category_unknown'
;

-- TODO create a trigger on Category to insert these automatically (also care for update/delete)
insert into Category_Descendant select c._id, 0, c._id from Category c;
insert into Category_Name_Cache select DISTINCT name, NULL from Category order by name;