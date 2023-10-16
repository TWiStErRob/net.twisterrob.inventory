-- Context: See v3, where the new triggers were added.
-- This v4 cleans up dangling images left after deleting things.

-- The default vacuuming strategy should be FULL, downgrade to INCREMENTAL so the following delete can complete fast.
-- With FULL mode during the commit a full copy of the deleted pages would be created, possibly requiring 3x disk space.
-- To work around this, INCREMENTAL only marks the pages as deleted, but that's fast to commit.
PRAGMA auto_vacuum = INCREMENTAL;

-- Make sure secure_delete is off, otherwise there's no point in incremental vacuuming,
-- because the secure_delete would make the transaction make a copy of much of the DB for commit. 
PRAGMA secure_delete = FALSE;

-- Clean up unused images
-- These should have been deleted when their belongings were deleted.
delete from Image where _id IN (
	select im._id
	from      Image    im
	left join Property p  on p.image = im._id
	left join Room     r  on r.image = im._id
	left join Item     i  on i.image = im._id
	where p._id IS NULL and r._id IS NULL and i._id IS NULL
);

-- Tested on 1.1G file with 800M worth superfluous images.
-- Secure delete required at least 750M free disk space.
-- With FULL mode the upgrade takes 3 minutes and ~400M free space (vacuuming was fluctuating).
-- With INCREMENTAL mode the upgrade takes ~15 seconds and then DatabaseService will take care of the rest later.
