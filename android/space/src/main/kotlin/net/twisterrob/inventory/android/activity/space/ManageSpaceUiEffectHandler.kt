package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ActivityContext
import net.twisterrob.inventory.android.activity.space.ManageSpaceUiEffect.ShowToast
import net.twisterrob.inventory.android.viewmodel.EffectHandler
import javax.inject.Inject

internal class ManageSpaceUiEffectHandler @Inject constructor(
	@ActivityContext private val context: Context
) : EffectHandler<ManageSpaceUiEffect> {

	override suspend fun handleEffect(effect: ManageSpaceUiEffect) {
		when (effect) {
			is ShowToast -> {
				Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
			}
		}
	}
}
