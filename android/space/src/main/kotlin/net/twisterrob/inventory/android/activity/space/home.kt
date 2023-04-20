package net.twisterrob.inventory.android.activity.space

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltViewModel
internal class ManageSpaceViewModel @Inject constructor(
	effectHandler: ManageSpaceEffectHandler,
	getSizesActionHandler: GetSizesActionHandler
) : BaseViewModel<ManageSpaceAction, ManageSpaceState, ManageSpaceEffect, ManageSpaceReducer>(
	effectHandler,
	ManageSpaceState.Initial
) {
	override val actionHandlers: Map<KClass<out ManageSpaceAction>, ManageSpaceHandler<out ManageSpaceAction>> = mapOf(
		LoadSizes::class to getSizesActionHandler,
	)
}

internal typealias ManageSpaceHandler<T> = ActionHandler<T, ManageSpaceState, ManageSpaceEffect, ManageSpaceReducer>

internal sealed interface ManageSpaceAction

internal data class ManageSpaceState(
	val isLoading: Boolean = false,
	val sizes: SizesState? = null,
) {

	companion object {
		val Initial = ManageSpaceState()
	}
}

internal sealed class ManageSpaceEffect {
	data class ShowToast(
		val message: String
	) : ManageSpaceEffect()
}

internal sealed class ManageSpaceReducer constructor(
	reducer: Reducer<ManageSpaceState>
) : Reducer<ManageSpaceState> by reducer {

	object OnLoading : ManageSpaceReducer({
		it.copy(
			isLoading = true
		)
	})
}

internal class ManageSpaceEffectHandler @Inject constructor(
	@ApplicationContext private val context: Context
) : EffectHandler<ManageSpaceEffect>() {

	override suspend fun handleEffect(effect: ManageSpaceEffect) {
		when (effect) {
			is ManageSpaceEffect.ShowToast -> {
				Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
			}
		}
	}
}
