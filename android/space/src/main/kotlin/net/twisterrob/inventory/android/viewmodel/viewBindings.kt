package net.twisterrob.inventory.android.viewmodel

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KMutableProperty0

/**
 * Returns a [Lazy] delegate to access the [ComponentActivity]'s root [ViewBinding].
 * This method will automatically inflate the [ViewBinding] when first accessed.
 *
 * ```kotlin
 * class MyComponentActivity : ComponentActivity() {
 *     private val binding: MyActivityBinding by viewBindingInflate()
 *     // or
 *     private val binding: MyActivityBinding by viewBindingInflate(MyActivityBinding::inflate)
 * }
 * ```
 *
 * @param VB the generated [ViewBinding] implementation to inflate.
 * @param factory by default it'll call the generated static [ViewBinding.inflate] method.
 * The reflection can be avoided by passing a method reference to a generated `inflate` method.
 * @param setContentView whether to set the Activity's content to the root of the inflated [ViewBinding].
 * @param autoDestroy whether to clear the delegate when the Activity is destroyed.
 *
 * @see androidx.activity.viewModels this approach is based on.
 * @see ComponentActivity.viewBinding for a version that binds to the existing inflated view.
 */
@MainThread
inline fun <reified VB : ViewBinding> ComponentActivity.viewBindingInflate(
	noinline factory: ((LayoutInflater) -> VB) = { layoutInflater ->
		VB::class.java
			.getMethod("inflate", LayoutInflater::class.java)
			.invoke(null, layoutInflater) as VB
	},
	setContentView: Boolean = true,
	autoDestroy: Boolean = true,
): Lazy<VB> =
	ComponentActivityViewBindingInflateDelegate(factory, this, setContentView, autoDestroy)

// Has to be public, so that inline works as expected.
class ComponentActivityViewBindingInflateDelegate<VB : ViewBinding>(
	private val inflate: (LayoutInflater) -> VB,
	private val thisRef: ComponentActivity,
	private val setContentView: Boolean,
	private val autoDestroy: Boolean,
) : Lazy<VB> {

	private var cached: VB? = null
	override fun isInitialized(): Boolean = cached != null

	override val value: VB
		get() = cached
			?: inflate(thisRef.layoutInflater)
				.also { cached = it }
				.also { binding ->
					if (setContentView) {
						thisRef.setContentView(binding.root)
					}
				}
				.also {
					if (autoDestroy) {
						thisRef.nullOutOnDestroy(::cached)
					}
				}
}

/**
 * Returns a [Lazy] delegate to access the [ComponentActivity]'s root [ViewBinding].
 * This method will automatically bind to the existing content [View] when first accessed.
 * Assumes that [ComponentActivity.setContentView] has already been called.
 *
 *  * manually right after `super.onCreate()` in `onCreate()`:
 *    ```kotlin
 *    class MyComponentActivity : ComponentActivity() {
 *        private val binding: MyActivityBinding by viewBinding()
 *        // or
 *        private val binding: MyActivityBinding by viewBinding(MyActivityBinding::bind)
 *
 *        override fun onCreate(savedInstanceState: Bundle?) {
 *            super.onCreate(savedInstanceState)
 *            setContentView(R.layout.my_activity)
 *        }
 *    }
 *    ```
 *  * automatically by using `ComponentActivity(@LayoutRes Int)` constructor:
 *    ```kotlin
 *    class MyComponentActivity : ComponentActivity(R.layout.my_activity) {
 *        private val binding: MyActivityBinding by viewBinding()
 *        // or
 *        private val binding: MyActivityBinding by viewBinding(MyActivityBinding::bind)
 *    }
 *    ```
 *
 * **WARNING**: this method can only be used when the [ComponentActivity]'s binding is not exposed
 * to other classes through Hilt that is also (potentially transitively) inject to the activity:
 * ```kotlin
 * @Provides
 * fun provideBinding(activity: MyActivity): MyActivityBinding = activity.binding
 *
 * class MyComponentActivity : ComponentActivity() {
 *     internal val binding: MyActivityBinding by viewBinding()
 *     @Inject lateinit var someDependency: SomeDependencyThatInjectsMyActivityBinding
 * }
 * ```
 * This is because the automatic [setContentView] and the automatic Hilt injection
 * are in the "wrong" order. To work around this issue, use [viewBindingInflate] instead.
 *
 * @param VB the generated [ViewBinding] implementation to bind to the [View].
 * @param factory by default it'll call the generated static [ViewBinding.bind] method.
 * The reflection can be avoided by passing a method reference to a generated `bind` method.
 * @param autoDestroy whether to clear the delegate when the Activity is destroyed.
 *
 * @see androidx.activity.viewModels this approach is based on.
 * @see ComponentActivity.viewBindingInflate for a version that inflates automatically.
 * @see Fragment.viewBinding for similar functionality in [Fragment]s.
 */
@MainThread
inline fun <reified VB : ViewBinding> ComponentActivity.viewBinding(
	noinline factory: ((View) -> VB) = { layoutInflater ->
		VB::class.java
			.getMethod("bind", View::class.java)
			.invoke(null, layoutInflater) as VB
	},
	autoDestroy: Boolean = true,
): Lazy<VB> =
	ComponentActivityViewBindingDelegate(factory, this, autoDestroy)

// Has to be public, so that inline works as expected.
class ComponentActivityViewBindingDelegate<VB : ViewBinding>(
	private val bind: (View) -> VB,
	private val activity: ComponentActivity,
	private val autoDestroy: Boolean,
) : Lazy<VB> {

	private var cached: VB? = null
	override fun isInitialized(): Boolean = cached != null

	override val value: VB
		get() = cached
			?: bind(activity.contentView)
				.also { cached = it }
				.also {
					if (autoDestroy) {
						activity.nullOutOnDestroy(::cached)
					}
				}
}

private val Activity.contentView: View
	get() {
		val content = this.window.decorView.findViewById<View>(android.R.id.content)
			?: error("There's no android.R.id.content in ${this}.")
		val contentViewParent = content as? ViewGroup
			?: error("android.R.id.content is not a ViewGroup: ${content}.")
		return contentViewParent.getChildAt(0)
			?: error("android.R.id.content has no child in ${this}, did you call setContentView?")
	}

/**
 * Returns a [Lazy] delegate to access the [Fragment]'s root [ViewBinding].
 * This method will automatically bind to the [View] when first accessed.
 * Assumes that [Fragment.onCreateView] has already been called.
 *
 * ```kotlin
 * class MyFragment : Fragment(R.layout.my_fragment) {
 *     private val binding: MyFragmentBinding by viewBinding()
 *     // or
 *     private val binding: MyFragmentBinding by viewBinding(MyFragmentBinding::bind)
 * }
 * ```
 *
 * @param VB the generated [ViewBinding] implementation to bind to the [View].
 * @param factory by default it'll call the generated static [ViewBinding.bind] method.
 * The reflection can be avoided by passing a method reference to a generated `bind` method.
 * @param autoDestroy whether to clear the delegate when the [Fragment]'s view is destroyed.
 *
 * @see androidx.activity.viewModels this approach is based on.
 * @see ComponentActivity.viewBinding for similar functionality in [ComponentActivity]s.
 */
@MainThread
inline fun <reified VB : ViewBinding> Fragment.viewBinding(
	noinline factory: ((View) -> VB) = { view ->
		VB::class.java
			.getMethod("bind", View::class.java)
			.invoke(null, view) as VB
	},
	autoDestroy: Boolean = true,
): Lazy<VB> =
	FragmentViewBindingDelegate(factory, this, autoDestroy)

// Has to be public, so that inline works as expected.
class FragmentViewBindingDelegate<VB : ViewBinding>(
	private val bind: (View) -> VB,
	private val fragment: Fragment,
	private val autoDestroy: Boolean,
) : Lazy<VB> {

	private var cached: VB? = null
	override fun isInitialized(): Boolean = cached != null

	override val value: VB
		get() = cached
			?: bind(fragment.requireView())
				.also { cached = it }
				.also {
					if (autoDestroy) {
						fragment.viewLifecycleOwnerLiveData.observe(fragment) { owner ->
							owner.nullOutOnDestroy(::cached)
						}
					}
				}
}

private fun <VB> LifecycleOwner.nullOutOnDestroy(cached: KMutableProperty0<VB?>) {
	this.lifecycle.addObserver(object : DefaultLifecycleObserver {
		override fun onDestroy(owner: LifecycleOwner) {
			// Lifecycle notifies notifies the observers before the actual onDestroy() call.
			// Delay a frame so that main thread can finish Activity.onDestroy()/Fragment.onDestroyView().
			Handler(Looper.getMainLooper()).post {
				cached.set(null)
			}
		}
	})
}
