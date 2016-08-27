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
	UNION SELECT 104,    1, 'property_vacation',      'item_canopy'
	UNION SELECT 105,    1, 'property_mobile',        'item_trailer'
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
	UNION SELECT  4, 'room_group_space',    'snippet_sun'
	UNION SELECT  5, 'room_group_nature',   'snippet_sun'
;

INSERT INTO RoomType
	            (_id, kind, name,              image)
	      SELECT   0,    0, 'room_other',      NULL
-- Communal
	UNION SELECT 101,    1, 'room_living',     'room_community'
	UNION SELECT 102,    1, 'room_bed',        'room_bedroom'
	UNION SELECT 103,    1, 'room_bath',       'room_bath'
	UNION SELECT 104,    1, 'room_dining',     'room_kitchen'
	UNION SELECT 105,    1, 'room_recreation', 'room_rec'
-- Storage
	UNION SELECT 201,    2, 'room_storage',    'room_storage'
	UNION SELECT 202,    2, 'room_parking',    'room_garage'
	UNION SELECT 203,    2, 'room_outbuilding','room_shed'
	UNION SELECT 204,    2, 'room_attic',      'room_attic'
	UNION SELECT 205,    2, 'room_basement',   'room_basement'
-- Function
	UNION SELECT 301,    3, 'room_work',       'room_study'
	UNION SELECT 302,    3, 'room_utility',    'room_laundry'
	UNION SELECT 303,    3, 'room_equipment',  'room_furnace'
	UNION SELECT 304,    3, 'room_gym',        'room_gym'
	UNION SELECT 305,    3, 'room_animal',     'room_animal'
-- Spaces
	UNION SELECT 401,    4, 'room_yard',       'item_fence'
	UNION SELECT 402,    4, 'room_shelter',    'room_empty'
	UNION SELECT 403,    4, 'room_platform',   'item_platform'
	UNION SELECT 404,    4, 'room_entrance',   'item_door'
	UNION SELECT 405,    4, 'room_access',     'item_stairs'
	UNION SELECT 406,    4, 'room_way',        'item_road'
-- Nature
	UNION SELECT 501,    5, 'room_plant',      'item_leaf'
	UNION SELECT 502,    5, 'room_water',      'snippet_water'
	UNION SELECT 503,    5, 'room_land',       'snippet_mountain'
;
