package net.twisterrob.inventory.android.arch

fun interface UiEffectHandler<Effect : UiEffect> {
	suspend fun launch(effect: Effect)
}
