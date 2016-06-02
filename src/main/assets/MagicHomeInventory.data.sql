INSERT INTO PropertyTypeKind
	           (_id, name,                        image)
	      SELECT  0, 'property_group_other',      'property_unknown'
	UNION SELECT  1, 'property_group_dwelling',   'property_house'
	UNION SELECT  2, 'property_group_commercial', 'property_office'
;
INSERT INTO PropertyType
	            (_id, kind, name,                     image)
	      SELECT   0,    0, 'property_other',         NULL
-- Residential    
	UNION SELECT 101,    1, 'property_apartment',     'property_condo'
	UNION SELECT 102,    1, 'property_house',         'property_house'
	UNION SELECT 103,    1, 'property_estate',        'property_mansion'
	UNION SELECT 104,    1, 'property_vacation',      'category_canopy'
	UNION SELECT 105,    1, 'property_mobile',        'category_trailer'
-- Non-residential
	UNION SELECT 201,    2, 'property_storage',       'room_storage'
	UNION SELECT 202,    2, 'property_manufacturing', 'property_factory'
	UNION SELECT 203,    2, 'property_productivity',  'property_office'
	UNION SELECT 204,    2, 'property_retail',        'property_shop'
	UNION SELECT 205,    2, 'property_services',      'property_shop'
	UNION SELECT 206,    2, 'property_recreation',    'category_soccer'
;

INSERT INTO RoomTypeKind
	           (_id, name,                  image)
	      SELECT  0, 'room_group_other',    'room_unknown'
	UNION SELECT  1, 'room_group_communal', 'room_community'
	UNION SELECT  2, 'room_group_storage',  'room_storage'
	UNION SELECT  3, 'room_group_function', 'room_function'
	UNION SELECT  4, 'room_group_space',    'room_sun'
	UNION SELECT  5, 'room_group_nature',   'room_sun'
;

INSERT INTO RoomType
	            (_id, kind, name,              image)
	      SELECT   0,    0, 'room_other',      NULL
-- Communal
	UNION SELECT 101,    1, 'room_bed',        'room_bedroom'
	UNION SELECT 102,    1, 'room_living',     NULL
	UNION SELECT 103,    1, 'room_bath',       'room_bathroom'
	UNION SELECT 104,    1, 'room_toilet',     'room_toilet'
	UNION SELECT 105,    1, 'room_kitchen',    'category_pot_hot'
	UNION SELECT 106,    1, 'room_dining',     'category_pot_hot'
	UNION SELECT 107,    1, 'room_recreation', NULL
-- Storage
	UNION SELECT 201,    2, 'room_storage',    'room_storage'
	UNION SELECT 202,    2, 'room_garage',     'room_garage'
	UNION SELECT 203,    2, 'room_outbuilding','room_shed'
	UNION SELECT 204,    2, 'room_attic',      'room_attic'
	UNION SELECT 205,    2, 'room_basement',   'room_basement'
-- Function
	UNION SELECT 301,    3, 'room_work',       NULL
	UNION SELECT 302,    3, 'room_utility',    NULL
	UNION SELECT 303,    3, 'room_equipment',  NULL
	UNION SELECT 304,    3, 'room_gym',        NULL
	UNION SELECT 305,    3, 'room_animal',     NULL
-- Spaces
	UNION SELECT 401,    4, 'room_yard',       NULL
	UNION SELECT 402,    4, 'room_shelter',    NULL
	UNION SELECT 403,    4, 'room_platform',   NULL
	UNION SELECT 404,    4, 'room_entrance',   NULL
	UNION SELECT 405,    4, 'room_access',     'room_stairs'
	UNION SELECT 406,    4, 'room_way',        NULL
	UNION SELECT 407,    4, 'room_parking',    NULL
-- Nature
	UNION SELECT 501,    5, 'room_plant',      NULL
	UNION SELECT 502,    5, 'room_water',      NULL
	UNION SELECT 503,    5, 'room_land',       NULL
;
