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
	      SELECT   NULL,    -1, 'category_internal', 'category_unknown'
	UNION SELECT   NULL,     0, 'category_uncategorized',                  'category_unknown'
	UNION SELECT   NULL,     1, 'category_group',                          'category_box'
	UNION SELECT   NULL,     2, 'category_images',                         'category_box'
	UNION SELECT   NULL,     3, 'category_parts',                          'category_box'
	UNION SELECT   NULL,  1000, 'category_storage',                        'category_box'
	UNION SELECT   1000,  1100,     'category_containers',                 'category_box'
	UNION SELECT   1000,  1200,     'category_bags',                       'category_box'
	UNION SELECT   1000,  1300,     'category_containers_liquid',          'category_box'
	UNION SELECT   1000,  1400,     'category_cases',                      'category_suitcase'
	UNION SELECT   1000,  1500,     'category_storage_special',            'category_box'
	UNION SELECT   1000,  1600,     'category_luggage',                    'category_suitcase'
	UNION SELECT   1000,  1700,     'category_packaging',                  'category_box'
	UNION SELECT   1000,  1800,     'category_compartments',               'category_box'
	UNION SELECT   NULL,  2000, 'category_furniture',                      'category_sofa'
	UNION SELECT   2000,  2100,     'category_fixtures',                   'category_hung_towel'
	UNION SELECT   2000,  2200,     'category_cabinetry',                  'category_sofa'
	UNION SELECT   2000,  2300,     'category_surfaces',                   'category_sofa'
	UNION SELECT   2000,  2400,     'category_resting',                    'category_sofa'
	UNION SELECT   2000,  2500,     'category_outdoor',                    'category_sofa'
	UNION SELECT   2000,  2600,     'category_pet',                        'category_sofa'
	UNION SELECT   NULL,  3000, 'category_furnishings',                    'category_hung_towel'
	UNION SELECT   3000,  3100,     'category_fittings',                   'category_hung_towel'
	UNION SELECT   3000,  3200,     'category_pillows',                    'category_hung_towel'
	UNION SELECT   3000,  3300,     'category_bedding',                    'category_hung_towel'
	UNION SELECT   3000,  3400,     'category_fabrics',                    'category_hung_towel'
	UNION SELECT   NULL,  4000, 'category_decorations',                    'category_balloon'
	UNION SELECT   4000,  4100,     'category_ornaments',                  'category_balloon'
	UNION SELECT   4000,  4200,     'category_cultural_decorations',       'category_balloon'
	UNION SELECT   4000,  4300,     'category_traditional_decorations',    'category_balloon'
	UNION SELECT   NULL,  5000, 'category_collectibles',                   'category_collectibles'
	UNION SELECT   5000,  5100,     'category_souvenirs',                  'category_collectibles'
	UNION SELECT   5000,  5200,     'category_artwork',                    'category_collectibles'
	UNION SELECT   5000,  5300,     'category_publications',               'category_collectibles'
	UNION SELECT   5000,  5400,     'category_recordings',                 'category_disc'
	UNION SELECT   NULL,  6000, 'category_clothing',                       'category_tshirt'
	UNION SELECT   6000,  6100,     'category_undress',                    'category_tshirt'
	UNION SELECT   6000,  6200,     'category_formal_clothing',            'category_tshirt'
	UNION SELECT   6000,  6300,     'category_outerwear',                  'category_tshirt'
	UNION SELECT   6000,  6400,     'category_footwear',                   'category_tshirt'
	UNION SELECT   6000,  6500,     'category_underwear',                  'category_tshirt'
	UNION SELECT   6000,  6600,     'category_headwear',                   'category_tshirt'
	UNION SELECT   6000,  6700,     'category_accessories',                'category_tshirt'
	UNION SELECT   6000,  6800,     'category_sports_clothing',            'category_soccer'
	UNION SELECT   6000,  6900,     'category_protective_clothing',        'category_tshirt'
	UNION SELECT   NULL,  7000, 'category_leisure',                        'category_soccer'
	UNION SELECT   7000,  7100,     'category_sports',                     'category_soccer'
	UNION SELECT   7000,  7200,     'category_instruments',                'category_musical_note'
	UNION SELECT   7000,  7300,     'category_organisms',                  'category_teddy'
	UNION SELECT   NULL,  8000, 'category_toys',                           'category_teddy'
	UNION SELECT   8000,  8100,     'category_model',                      'category_teddy'
	UNION SELECT   8000,  8200,     'category_games',                      'category_teddy'
	UNION SELECT   8000,  8300,     'category_creative_games',             'category_teddy'
	UNION SELECT   8000,  8400,     'category_novelties',                  'category_teddy'
	UNION SELECT   8000,  8500,     'category_activity_toys',              'category_teddy'
	UNION SELECT   NULL,  9000, 'category_machines',                       'category_plug'
	UNION SELECT   9000,  9100,     'category_appliances_major',           'category_plug'
	UNION SELECT   9000,  9200,     'category_appliances_minor',           'category_plug'
	UNION SELECT   9000,  9300,     'category_consumer_electronics',       'category_plug'
	UNION SELECT   9000,  9400,     'category_machine_parts',              'category_plug'
	UNION SELECT   9000,  9500,     'category_computers',                  'category_chip'
	UNION SELECT   9000,  9600,     'category_computer_parts',             'category_chip'
	UNION SELECT   9000,  9700,     'category_cables',                     'category_plug'
	UNION SELECT   9000,  9800,     'category_vehicles',                   'category_car_front'
	UNION SELECT   NULL, 10000, 'category_household',                      'category_tools'
	UNION SELECT  10000, 10100,     'category_gardening',                  'category_tools'
	UNION SELECT  10000, 10200,     'category_cleaning',                   'category_tools'
	UNION SELECT  10000, 10300,     'category_chemicals',                  'category_tools'
	UNION SELECT  10000, 10400,     'category_utensils_kitchen',           'room_kitchen'
	UNION SELECT  10000, 10500,     'category_cookware',                   'room_kitchen'
	UNION SELECT  10000, 10600,     'category_tableware',                  'room_kitchen'
	UNION SELECT   NULL, 11000, 'category_crafts',                         'category_tools'
	UNION SELECT  11000, 11100,     'category_tools',                      'category_tools'
	UNION SELECT  11000, 11200,     'category_materials',                  'category_tools'
	UNION SELECT  11000, 11300,     'category_materials_binding',          'category_tools'
	UNION SELECT   NULL, 12000, 'category_documents',                      'category_paper'
	UNION SELECT  12000, 12100,     'category_official',                   'category_paper'
	UNION SELECT  12000, 12200,     'category_archives',                   'category_paper'
	UNION SELECT  12000, 12300,     'category_money',                      'category_paper'
	UNION SELECT  12000, 12400,     'category_paper',                      'category_paper'
	UNION SELECT   NULL, 13000, 'category_consumables',                    'category_foods'
	UNION SELECT  13000, 13100,     'category_food',                       'category_foods'
	UNION SELECT  13000, 13200,     'category_liquids',                    'category_foods'
	UNION SELECT  13000, 13300,     'category_ingredients',                'category_foods'
	UNION SELECT  13000, 13400,     'category_plants',                     'category_foods'
	UNION SELECT   NULL, 14000, 'category_health',                         'category_foods'
	UNION SELECT  14000, 14100,     'category_medical',                    'category_foods'
	UNION SELECT  14000, 14200,     'category_cosmetics',                  'category_foods'
	UNION SELECT  14000, 14300,     'category_hygiene',                    'category_foods'
;
