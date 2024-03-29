package net.twisterrob.inventory.android.space.sizes

import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.bumptech.glide.Glide
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.twisterrob.android.utils.tools.DatabaseTools
import net.twisterrob.android.utils.tools.IOTools
import net.twisterrob.inventory.android.arch.UseCase
import net.twisterrob.inventory.android.content.Database
import net.twisterrob.inventory.android.logger
import java.io.File
import javax.inject.Inject

private val LOG = logger<GetSizesUseCase>()

internal class GetSizesUseCase @Inject constructor(
	@ApplicationContext
	private val context: Context,
	private val database: Database,
) : UseCase<Unit, SizesDomain> {

	override suspend fun execute(input: Unit): SizesDomain = withContext(Dispatchers.IO) {
		SizesDomain(
			imageCache = safe {
				fileSystemSizes(
					// This is the same as the default InternalCacheDiskCacheFactory
					// used in com.bumptech.glide.GlideBuilder.createGlide.
					Glide.getPhotoCacheDir(context)
				)
			},
			database = safe {
				fileSystemSizes(
					context.getDatabasePath(database.helper.databaseName)
				)
			},
			freelist = safe {
				database.readableDatabase.let { it.freelistCount * it.pageSize }
			},
			searchIndex = safe {
				database.searchSize
			},
			allData = safe {
				fileSystemSizes(
					File(context.applicationInfo.dataDir),
					context.externalCacheDir,
					context.getExternalFilesDir(null),
				)
			},
		)
	}

	private fun fileSystemSizes(vararg dirsOrFiles: File?): Long =
		dirsOrFiles.sumOf { IOTools.calculateSize(it) }

	private val SQLiteDatabase.freelistCount: Long
		get() = DatabaseUtils.longForQuery(
			this,
			"PRAGMA freelist_count;",
			DatabaseTools.NO_ARGS
		)

	private fun safe(block: () -> Long): Result<Long> =
		try {
			Result.success(block())
		} catch (@Suppress("TooGenericExceptionCaught") ex: Exception) {
			LOG.error("Cannot get size", ex)
			Result.failure(ex)
		}
}
