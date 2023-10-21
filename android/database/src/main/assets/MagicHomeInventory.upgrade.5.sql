-- Context: 1.1.0 release had a backup import bug, hotfix in 1.1.1.
-- When importing a property with an image,
-- the image of the property ended up in the Items table on the same ID.
-- Sadly, that cannot be undone, what's overwritten is overwritten.

-- Best effort is to delete dangling images of Room roots.
update Item
set image = NULL
where name = 'ROOT' and category = -1;
