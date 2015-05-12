INSERT INTO PropertyType
	           (_id, priority, name,                 image)
	      SELECT  0,       -1, 'property_other',     'property_unknown'
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
	      SELECT  0,       -1, 'room_group_other',    'room_unknown'
	UNION SELECT  1,       10, 'room_group_general',  'room_unknown'
	UNION SELECT  2,       20, 'room_group_storage',  'room_storage'
	UNION SELECT  3,       30, 'room_group_common',   'room_unknown'
	UNION SELECT  4,       40, 'room_group_function', 'room_unknown'
	UNION SELECT  5,       50, 'room_group_space',    'room_unknown'
;

INSERT INTO RoomType
	            (_id,  kind, priority, name,            image)
	      SELECT   0,     0,       -1, 'room_other',    NULL
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
	UNION SELECT     0,   NULL, 'category_uncategorized',                  'category_unknown'
	UNION SELECT     1,   NULL, 'category_group',                          'category_box'
	UNION SELECT  1000,   NULL, 'category_clothing',                       'category_tshirt'
	UNION SELECT  1100,   1000,     'category_undress',                    'category_tshirt'
	UNION SELECT  1200,   1000,     'category_formal_clothing',            'category_tshirt'
	UNION SELECT  1300,   1000,     'category_outerwear',                  'category_tshirt'
	UNION SELECT  1400,   1000,     'category_footwear',                   'category_tshirt'
	UNION SELECT  1500,   1000,     'category_underwear',                  'category_tshirt'
	UNION SELECT  1600,   1000,     'category_headwear',                   'category_tshirt'
	UNION SELECT  1700,   1000,     'category_accessories',                'category_tshirt'
	UNION SELECT  1800,   1000,     'category_sports_clothing',            'category_soccer'
	UNION SELECT  3000,   NULL, 'category_storage',                        'category_box'
	UNION SELECT  3100,   3000,     'category_boxes',                      'category_box'
	UNION SELECT  3200,   3000,     'category_packaging',                  'category_box'
	UNION SELECT  3300,   3000,     'category_containers',                 'category_box'
	UNION SELECT  3400,   3000,     'category_containers_bulk',            'category_box'
	UNION SELECT  3500,   3000,     'category_cases',                      'category_box'
	UNION SELECT  3600,   3000,     'category_luggage',                    'category_suitcase'
	UNION SELECT  4000,   NULL, 'category_collectibles',                   'category_collectibles'
	UNION SELECT  4100,   4000,     'category_souvenirs',                  'category_collectibles'
	UNION SELECT  4200,   4000,     'category_artwork',                    'category_collectibles'
	UNION SELECT  4300,   4000,     'category_publications',               'category_collectibles'
	UNION SELECT  4400,   4000,     'category_recordings',                 'category_disc'
	UNION SELECT  5000,   NULL, 'category_consumables',                    'category_foods'
	UNION SELECT  5100,   5000,     'category_food',                       'category_foods'
	UNION SELECT  5200,   5000,     'category_liquids',                    'category_foods'
	UNION SELECT  5300,   5000,     'category_ingredients',                'category_foods'
	UNION SELECT  6000,   NULL, 'category_health',                         'category_foods'
	UNION SELECT  6100,   6000,     'category_medical',                    'category_foods'
	UNION SELECT  6200,   6000,     'category_sanitation',                 'category_foods'
	UNION SELECT  6300,   6000,     'category_cosmetics',                  'category_foods'
	UNION SELECT  8000,   NULL, 'category_decorations',                    'category_balloon'
	UNION SELECT  8100,   8000,     'category_ornaments',                  'category_balloon'
	UNION SELECT  8200,   8000,     'category_cultural_decorations',       'category_balloon'
	UNION SELECT  8300,   8000,     'category_traditional_decorations',    'category_balloon'
	UNION SELECT  9000,   NULL, 'category_furnishings',                    'category_hung_towel'
	UNION SELECT  9100,   9000,     'category_fittings',                   'category_hung_towel'
	UNION SELECT  9200,   9000,     'category_pillows',                    'category_hung_towel'
	UNION SELECT  9300,   9000,     'category_bedding',                    'category_hung_towel'
	UNION SELECT  9400,   9000,     'category_fabrics',                    'category_hung_towel'
;

INSERT INTO Category
	              (_id, parent, name,                                      image)
	      SELECT 10000,   NULL, 'category_furniture',                      'category_sofa'
	UNION SELECT 10100,  10000,     'category_fixtures',                   'category_hung_towel'
	UNION SELECT 10200,  10000,     'category_cabinetry',                  'category_sofa'
	UNION SELECT 10300,  10000,     'category_surfaces',                   'category_sofa'
	UNION SELECT 10400,  10000,     'category_resting',                    'category_sofa'
	UNION SELECT 10500,  10000,     'category_outdoor',                    'category_sofa'
	UNION SELECT 10600,  10000,     'category_compartments',               'category_sofa'
	UNION SELECT 11000,   NULL, 'category_stationery',                     'category_paper'
	UNION SELECT 11100,  11000,     'category_stationery_desktop',         'category_paper'
	UNION SELECT 11200,  11000,     'category_stationery_paper',           'category_paper'
	UNION SELECT 11300,  11000,     'category_stationery_art',             'category_pen'
	UNION SELECT 11400,  11000,     'category_stationery_storage',         'category_box'
	UNION SELECT 12000,   NULL, 'category_tools',                          'category_tools'
	UNION SELECT 12100,  12000,     'category_diy',                        'category_tools'
	UNION SELECT 12200,  12000,     'category_gardening',                  'category_tools'
	UNION SELECT 12300,  12000,     'category_craft',                      'category_tools'
	UNION SELECT 12400,  12000,     'category_materials',                  'category_tools'
	UNION SELECT 12500,  12000,     'category_chemicals',                  'category_tools'
	UNION SELECT 12600,  12000,     'category_cleaning',                   'category_tools'
	UNION SELECT 12700,  12000,     'category_utensils_kitchen',           'room_kitchen'
	UNION SELECT 12800,  12000,     'category_cookware',                   'room_kitchen'
	UNION SELECT 12900,  12000,     'category_tableware',                  'room_kitchen'
	UNION SELECT 13000,   NULL, 'category_toys',                           'category_teddy'
	UNION SELECT 13100,  13000,     'category_toys_soft',                  'category_teddy'
	UNION SELECT 13200,  13000,     'category_toys_figures',               'category_teddy'
	UNION SELECT 13300,  13000,     'category_toys_tabletop',              'category_teddy'
	UNION SELECT 13400,  13000,     'category_toys_puzzlers',              'category_teddy'
	UNION SELECT 13500,  13000,     'category_toys_construction',          'category_teddy'
	UNION SELECT 13600,  13000,     'category_toys_outdoors',              'category_teddy'
	UNION SELECT 14000,   NULL, 'category_documents',                      'category_paper'
	UNION SELECT 14100,  14000,     'category_archives',                   'category_paper'
	UNION SELECT 14200,  14000,     'category_statements',                 'category_paper'
	UNION SELECT 14300,  14000,     'category_manuals',                    'category_paper'
	UNION SELECT 15000,   NULL, 'category_leisure',                        'category_soccer'
	UNION SELECT 15100,  15000,     'category_sport_accessories',          'category_soccer'
	UNION SELECT 15200,  15000,     'category_sport_equipment',            'category_soccer'
	UNION SELECT 15300,  15000,     'category_camping',                    'category_car_front'
	UNION SELECT 15400,  15000,     'category_vehicles',                   'category_car_front'
	UNION SELECT 15500,  15000,     'category_vehicles_parts',             'category_car_front'
	UNION SELECT 15600,  15000,     'category_instruments',                'category_musical_note'
	UNION SELECT 16000,   NULL, 'category_electronics',                    'category_plug'
	UNION SELECT 16100,  16000,     'category_appliances',                 'category_plug'
	UNION SELECT 16110,  16100,         'category_appliances_major',       'category_plug'
	UNION SELECT 16120,  16100,         'category_appliances_minor',       'category_plug'
	UNION SELECT 16130,  16100,         'category_appliances_pieces',      'category_plug'
	UNION SELECT 16200,  16000,     'category_recorders',                  'category_plug'
	UNION SELECT 16300,  16000,     'category_playbacks',                  'category_disc'
	UNION SELECT 16400,  16000,     'category_displays',                   'category_plug'
	UNION SELECT 16500,  16000,     'category_computers',                  'category_chip'
	UNION SELECT 16600,  16000,     'category_computer_peripherals',       'category_chip'
	UNION SELECT 16700,  16000,     'category_cables',                     'category_plug'
;
