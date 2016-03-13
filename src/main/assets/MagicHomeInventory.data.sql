INSERT INTO PropertyTypeKind
	           (_id, name,                        image)
	      SELECT  0, 'property_group_other',      'property_unknown'
	UNION SELECT  1, 'property_group_dwelling',   'property_home'
	UNION SELECT  2, 'property_group_commercial', 'property_office'
;
INSERT INTO PropertyType
	            (_id, kind, name,                 image)
	      SELECT   0,    0, 'property_other',     NULL
-- Residential
	UNION SELECT 101,    1, 'property_apartment', NULL
	UNION SELECT 102,    1, 'property_house',     NULL
	UNION SELECT 103,    1, 'property_estate',    NULL
	UNION SELECT 104,    1, 'property_vacation',  NULL
	UNION SELECT 105,    1, 'property_mobile',    NULL
-- Non-residential
	UNION SELECT 201,    2, 'property_storage',       'room_storage'
	UNION SELECT 202,    2, 'property_manufacturing', 'category_tools'
	UNION SELECT 203,    2, 'property_productivity',  NULL
	UNION SELECT 204,    2, 'property_retail',        'category_collectibles'
	UNION SELECT 205,    2, 'property_services',      NULL
	UNION SELECT 206,    2, 'property_recreation',    'category_soccer'
;

INSERT INTO RoomTypeKind
	           (_id, name,                  image)
	      SELECT  0, 'room_group_other',    'room_unknown'
	UNION SELECT  1, 'room_group_general',  'room_unknown'
	UNION SELECT  2, 'room_group_storage',  'room_storage'
	UNION SELECT  3, 'room_group_common',   'room_unknown'
	UNION SELECT  4, 'room_group_function', 'room_unknown'
	UNION SELECT  5, 'room_group_space',    'room_unknown'
;

INSERT INTO RoomType
	            (_id, kind, name,              image)
	      SELECT   0,    0, 'room_other',      NULL
-- General
	UNION SELECT 101,    1, 'room_bath',       'room_bathroom'
	UNION SELECT 102,    1, 'room_bed',        'room_bedroom'
	UNION SELECT 103,    1, 'room_kitchen',    'room_kitchen'
	UNION SELECT 104,    1, 'room_toilet',     'room_toilet'
-- Storage
	UNION SELECT 201,    2, 'room_storage',    'room_storage'
	UNION SELECT 202,    2, 'room_closet',     NULL
	UNION SELECT 203,    2, 'room_garage',     NULL
	UNION SELECT 204,    2, 'room_furnace',    NULL
	UNION SELECT 205,    2, 'room_shed',       NULL
	UNION SELECT 206,    2, 'room_attic',      NULL
	UNION SELECT 207,    2, 'room_basement',   NULL
	UNION SELECT 208,    2, 'room_cellar',     NULL
-- Common
	UNION SELECT 301,    3, 'room_living',     NULL
	UNION SELECT 302,    3, 'room_family',     NULL
	UNION SELECT 303,    3, 'room_play',       NULL
-- Function
	UNION SELECT 401,    4, 'room_dining',     NULL
	UNION SELECT 402,    4, 'room_library',    NULL
	UNION SELECT 403,    4, 'room_office',     NULL
	UNION SELECT 404,    4, 'room_gym',        NULL
	UNION SELECT 405,    4, 'room_TV',         NULL
	UNION SELECT 406,    4, 'room_pool',       NULL
	UNION SELECT 407,    4, 'room_laundry',    NULL
	UNION SELECT 408,    4, 'room_stable',     NULL
	UNION SELECT 409,    4, 'room_greenhouse', NULL
	UNION SELECT 410,    4, 'room_animal',     NULL
-- Spaces
	UNION SELECT 501,    5, 'room_balcony',    NULL
	UNION SELECT 502,    5, 'room_garden',     NULL
	UNION SELECT 503,    5, 'room_lobby',      NULL
	UNION SELECT 504,    5, 'room_deck',       NULL
	UNION SELECT 505,    5, 'room_patio',      NULL
	UNION SELECT 506,    5, 'room_crawl',      NULL
;
