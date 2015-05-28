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
	UNION SELECT 407,     4,        0, 'room_laundry',   NULL
	UNION SELECT 408,     4,        0, 'room_stable',   NULL
	UNION SELECT 409,     4,        0, 'room_greenhouse', NULL
-- Spaces
	UNION SELECT 501,     5,        0, 'room_balcony',  NULL
	UNION SELECT 502,     5,        0, 'room_garden',   NULL
	UNION SELECT 503,     5,        0, 'room_lobby',    NULL
	UNION SELECT 504,     5,        0, 'room_deck',     NULL
	UNION SELECT 505,     5,        0, 'room_patio',    NULL
	UNION SELECT 506,     5,        0, 'room_crawl',    NULL
;

INSERT INTO Category
	           (parent,  _id, name,                                      image)
	      SELECT  NULL,   -1, 'category_internal',                       'category_unknown'
	UNION SELECT  NULL,    0, 'category_uncategorized',                  'category_unknown'
	UNION SELECT  NULL,    1, 'category_group',                          'category_box'
	UNION SELECT  NULL,    2, 'category_parts',                          'category_box'
	UNION SELECT  NULL,    3, 'category_images',                         'category_box'
	UNION SELECT  NULL,  100, 'category_storage',                        'category_box'
	UNION SELECT   100,  110,     'category_containers',                 'category_box'
	UNION SELECT   100,  120,     'category_containers_flexible',        'category_box'
	UNION SELECT   100,  130,     'category_containers_liquid',          'category_box'
	UNION SELECT   100,  140,     'category_cases',                      'category_suitcase'
	UNION SELECT   100,  150,     'category_storage_special',            'category_box'
	UNION SELECT   100,  160,     'category_luggage',                    'category_suitcase'
	UNION SELECT   100,  170,     'category_packaging',                  'category_box'
	UNION SELECT   100,  180,     'category_compartments',               'category_box'
	UNION SELECT  NULL,  200, 'category_furniture',                      'category_sofa'
	UNION SELECT   200,  210,     'category_fixtures',                   'category_hung_towel'
	UNION SELECT   200,  220,     'category_cabinetry',                  'category_sofa'
	UNION SELECT   200,  230,     'category_surfaces',                   'category_sofa'
	UNION SELECT   200,  240,     'category_resting',                    'category_sofa'
	UNION SELECT   200,  250,     'category_outdoor',                    'category_sofa'
	UNION SELECT   200,  260,     'category_pet',                        'category_sofa'
	UNION SELECT  NULL,  300, 'category_furnishings',                    'category_hung_towel'
	UNION SELECT   300,  310,     'category_fittings',                   'category_hung_towel'
	UNION SELECT   300,  320,     'category_pillows',                    'category_hung_towel'
	UNION SELECT   300,  330,     'category_bedding',                    'category_hung_towel'
	UNION SELECT   300,  340,     'category_fabrics',                    'category_hung_towel'
	UNION SELECT  NULL,  400, 'category_decorations',                    'category_balloon'
	UNION SELECT   400,  410,     'category_ornaments',                  'category_balloon'
	UNION SELECT   400,  420,     'category_cultural_decorations',       'category_balloon'
	UNION SELECT   400,  430,     'category_traditional_decorations',    'category_balloon'
	UNION SELECT  NULL,  500, 'category_collectibles',                   'category_collectibles'
	UNION SELECT   500,  510,     'category_souvenirs',                  'category_collectibles'
	UNION SELECT   500,  520,     'category_artwork',                    'category_collectibles'
	UNION SELECT   500,  530,     'category_publications',               'category_collectibles'
	UNION SELECT   500,  540,     'category_recordings',                 'category_disc'
	UNION SELECT  NULL,  600, 'category_clothing',                       'category_tshirt'
	UNION SELECT   600,  610,     'category_undress',                    'category_tshirt'
	UNION SELECT   600,  620,     'category_formal_clothing',            'category_tshirt'
	UNION SELECT   600,  630,     'category_outerwear',                  'category_tshirt'
	UNION SELECT   600,  640,     'category_footwear',                   'category_tshirt'
	UNION SELECT   600,  650,     'category_underwear',                  'category_tshirt'
	UNION SELECT   600,  660,     'category_headwear',                   'category_tshirt'
	UNION SELECT   600,  670,     'category_accessories',                'category_tshirt'
	UNION SELECT   600,  680,     'category_sports_clothing',            'category_soccer'
	UNION SELECT   600,  690,     'category_protective_clothing',        'category_tshirt'
	UNION SELECT  NULL,  700, 'category_leisure',                        'category_soccer'
	UNION SELECT   700,  720,     'category_sports',                     'category_soccer'
	UNION SELECT   700,  730,     'category_travel',                     'category_car_front'
	UNION SELECT   700,  740,     'category_vehicles',                   'category_car_front'
	UNION SELECT   700,  750,     'category_vehicles_parts',             'category_car_front'
	UNION SELECT   700,  760,     'category_instruments',                'category_musical_note'
	UNION SELECT   700,  770,     'category_organisms',                  'category_teddy'
	UNION SELECT  NULL,  800, 'category_toys',                           'category_teddy'
	UNION SELECT   800,  810,     'category_model',                      'category_teddy'
	UNION SELECT   800,  820,     'category_games',                      'category_teddy'
	UNION SELECT   800,  830,     'category_creative_games',             'category_teddy'
	UNION SELECT   800,  840,     'category_novelties',                  'category_teddy'
	UNION SELECT   800,  850,     'category_activity_toys',              'category_teddy'
	UNION SELECT  NULL,  900, 'category_electronics',                    'category_plug'
	UNION SELECT   900,  910,     'category_appliances_major',           'category_plug'
	UNION SELECT   900,  920,     'category_appliances_minor',           'category_plug'
	UNION SELECT   900,  930,     'category_consumer_electronics',       'category_plug'
	UNION SELECT   900,  940,     'category_machine_parts',              'category_plug'
	UNION SELECT   900,  950,     'category_computers',                  'category_chip'
	UNION SELECT   900,  960,     'category_computer_parts',             'category_chip'
	UNION SELECT   900,  970,     'category_cables',                     'category_plug'
	UNION SELECT  NULL, 1000, 'category_household',                      'category_tools'
	UNION SELECT  1000, 1010,     'category_gardening',                  'category_tools'
	UNION SELECT  1000, 1020,     'category_cleaning',                   'category_tools'
	UNION SELECT  1000, 1030,     'category_chemicals',                  'category_tools'
	UNION SELECT  1000, 1040,     'category_utensils_kitchen',           'room_kitchen'
	UNION SELECT  1000, 1050,     'category_cookware',                   'room_kitchen'
	UNION SELECT  1000, 1060,     'category_tableware',                  'room_kitchen'
	UNION SELECT  NULL, 1100, 'category_crafts',                         'category_tools'
	UNION SELECT  1100, 1110,     'category_tools',                      'category_tools'
	UNION SELECT  1100, 1120,     'category_materials',                  'category_tools'
	UNION SELECT  1100, 1130,     'category_materials_binding',          'category_tools'
	UNION SELECT  NULL, 1200, 'category_documents',                      'category_paper'
	UNION SELECT  1200, 1210,     'category_official',                   'category_paper'
	UNION SELECT  1200, 1220,     'category_archives',                   'category_paper'
	UNION SELECT  1200, 1230,     'category_money',                      'category_paper'
	UNION SELECT  1200, 1240,     'category_paper',                      'category_paper'
	UNION SELECT  NULL, 1300, 'category_consumables',                    'category_foods'
	UNION SELECT  1300, 1310,     'category_food',                       'category_foods'
	UNION SELECT  1300, 1320,     'category_liquids',                    'category_foods'
	UNION SELECT  1300, 1330,     'category_ingredients',                'category_foods'
	UNION SELECT  1300, 1340,     'category_plants',                     'category_foods'
	UNION SELECT  NULL, 1400, 'category_health',                         'category_foods'
	UNION SELECT  1400, 1410,     'category_medical',                    'category_foods'
	UNION SELECT  1400, 1420,     'category_cosmetics',                  'category_foods'
	UNION SELECT  1400, 1430,     'category_hygiene',                    'category_foods'
;
