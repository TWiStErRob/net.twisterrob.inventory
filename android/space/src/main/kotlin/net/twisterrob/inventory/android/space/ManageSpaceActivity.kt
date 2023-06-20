package net.twisterrob.inventory.android.space

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent
import net.twisterrob.android.viewbinding.viewBindingInflate
import net.twisterrob.inventory.android.activity.BaseActivity
import net.twisterrob.inventory.android.space.databinding.ManageSpaceActivityBinding
import net.twisterrob.inventory.android.view.RecyclerViewController
import org.orbitmvi.orbit.viewmodel.observe
import javax.inject.Inject

@AndroidEntryPoint
class ManageSpaceActivity : BaseActivity() {
	private val binding: ManageSpaceActivityBinding by viewBindingInflate()
	private val viewModel: ManageSpaceViewModel by viewModels()
	@Inject internal lateinit var stateHandler: ManageSpaceUiStateHandler
	@Inject internal lateinit var effectHandler: ManageSpaceUiEffectHandler
	@Inject internal lateinit var eventHandler: ManageSpaceUiEventHandler

	@InstallIn(ActivityComponent::class)
	@Module
	internal class HiltModule {
		@Provides
		fun provideTypedActivity(activity: Activity): ManageSpaceActivity =
			activity as ManageSpaceActivity

		@Provides
		fun provideBinding(activity: ManageSpaceActivity): ManageSpaceActivityBinding =
			activity.binding

		@Provides
		fun provideActivityResultCaller(activity: ManageSpaceActivity): ActivityResultCaller =
			activity

		@Provides
		fun provideViewModel(activity: ManageSpaceActivity): ManageSpaceViewModel =
			activity.viewModel
		
		@Provides
		fun provideLifecycle(activity: ManageSpaceActivity): Lifecycle =
			activity.lifecycle
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setupUI()
		eventHandler.wireEvents()
		viewModel.observe(
			lifecycleOwner = this,
			state = stateHandler::render,
			sideEffect = effectHandler::launch
		)
	}

	private fun setupUI() {
		setIcon(ContextCompat.getDrawable(this, applicationInfo.icon))
		supportActionBar.setDisplayHomeAsUpEnabled(false)
		RecyclerViewController.initializeProgress(binding.refresher)
	}

	companion object {
		@JvmStatic
		fun launch(context: Context): Intent =
			Intent(context, ManageSpaceActivity::class.java)
	}
}
