package net.twisterrob.inventory.android.viewmodel

import android.app.Activity
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

fun <T : ViewBinding> Activity.setContentView(inflate: (LayoutInflater) -> T): T {
	val binding = inflate(layoutInflater)
	setContentView(binding.root)
	return binding
}
