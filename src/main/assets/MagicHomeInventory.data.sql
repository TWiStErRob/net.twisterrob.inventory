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
	UNION SELECT  1, 'room_group_communal', 'room_unknown'
	UNION SELECT  2, 'room_group_storage',  'room_storage'
	UNION SELECT  3, 'room_group_function', 'room_unknown'
	UNION SELECT  4, 'room_group_space',    'room_unknown'
;

INSERT INTO RoomType
	            (_id, kind, name,              image)
	      SELECT   0,    0, 'room_other',      NULL
-- Communal
	UNION SELECT 101,    1, 'room_bed',        'room_bedroom'
	UNION SELECT 102,    1, 'room_living',     NULL
	UNION SELECT 103,    1, 'room_bath',       'room_bathroom'
	UNION SELECT 104,    1, 'room_toilet',     'room_toilet'
	UNION SELECT 105,    1, 'room_kitchen',    'room_kitchen'
	UNION SELECT 106,    1, 'room_dining',     'room_kitchen'
	UNION SELECT 107,    1, 'room_recreation', NULL
-- Storage & Utility
	UNION SELECT 201,    2, 'room_storage',    'room_storage'
	UNION SELECT 202,    2, 'room_shed',       NULL
	UNION SELECT 203,    2, 'room_garage',     NULL
	UNION SELECT 204,    2, 'room_attic',      NULL
	UNION SELECT 205,    2, 'room_basement',   NULL
	UNION SELECT 206,    2, 'room_laundry',    NULL
	UNION SELECT 207,    2, 'room_furnace',    NULL
-- Function
	UNION SELECT 302,    3, 'room_work',       NULL
	UNION SELECT 303,    3, 'room_gym',        NULL
	UNION SELECT 304,    3, 'room_greenhouse', NULL
	UNION SELECT 305,    3, 'room_animal',     NULL
-- Spaces
	UNION SELECT 401,    4, 'room_garden',     NULL
	UNION SELECT 402,    4, 'room_patio',      NULL
	UNION SELECT 403,    4, 'room_lobby',      NULL
	UNION SELECT 404,    4, 'room_balcony',    NULL
	UNION SELECT 405,    4, 'room_corridor',   NULL
	UNION SELECT 406,    4, 'room_stairs',     NULL
	UNION SELECT 407,    4, 'room_parking',    NULL
;
