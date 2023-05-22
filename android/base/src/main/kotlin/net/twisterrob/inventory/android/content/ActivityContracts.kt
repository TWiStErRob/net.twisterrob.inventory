package net.twisterrob.inventory.android.content

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.annotation.RequiresApi

@RequiresApi(VERSION_CODES.KITKAT)
open class CreateOpenableDocument(mimeType: String) : CreateDocument(mimeType) {
	override fun createIntent(context: Context, input: String): Intent =
		super
			.createIntent(context, input)
			.addCategory(Intent.CATEGORY_OPENABLE)
}

@RequiresApi(VERSION_CODES.KITKAT)
open class OpenOpenableDocument : OpenDocument() {
	override fun createIntent(context: Context, input: Array<String>): Intent =
		super
			.createIntent(context, input)
			.addCategory(Intent.CATEGORY_OPENABLE)
}
