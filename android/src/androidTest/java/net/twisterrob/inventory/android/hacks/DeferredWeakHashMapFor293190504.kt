package net.twisterrob.inventory.android.hacks

import java.util.LinkedList
import java.util.WeakHashMap
import kotlin.collections.MutableMap.MutableEntry

private typealias Change<K, V> = DeferredWeakHashMapFor293190504<K, V>.() -> Unit

/**
 * See https://issuetracker.google.com/issues/293190504
 *
 * This is a workaround for not allowing concurrent modifications
 * to [androidx.core.view.ViewCompat.AccessibilityPaneVisibilityManager]'s internal state.
 *
 * Any structure-modifying changes made to the map while iterating
 * is deferred until the iteration is over.
 *
 * This prevents the nested `put` call in `checkPaneVisibility`
 * from cleaning up the weak queue mid-iteration.
 */
internal class DeferredWeakHashMapFor293190504<K, V> : WeakHashMap<K, V>() {
	@Volatile
	private var locked = false
	private val changes: MutableList<Change<K, V>> = LinkedList()

	override fun put(key: K, value: V): V? {
		return if (!locked) {
			super.put(key, value)
		} else {
			changes.add { put(key, value) }
			// Should be super.get(key), but reads also mutate the map because it's weak.
			null
		}
	}

	override fun remove(key: K?): V? {
		return if (!locked) {
			super.remove(key)
		} else {
			changes.add { remove(key) }
			// Should be super.get(key), but reads also mutate the map because it's weak.
			null
		}
	}

	override val entries: MutableSet<MutableEntry<K, V>>
		get() = DeferringEntrySet(super.entries)

	private inner class DeferringEntrySet(
		private val backingSet: MutableSet<MutableEntry<K, V>>
	) : MutableSet<MutableEntry<K, V>> by backingSet {

		override fun iterator(): MutableIterator<MutableEntry<K, V>> {
			check(!locked) { "Already iterating, only one supported." }
			locked = true
			val iterator = backingSet.iterator()
			return object : MutableIterator<MutableEntry<K, V>> by iterator {
				override fun hasNext(): Boolean {
					val hasNext = iterator.hasNext()
					// Assuming a for(:) loop will not do anything after this.
					if (!hasNext) {
						locked = false
						changes.forEach { it(this@DeferredWeakHashMapFor293190504) }
					}
					return hasNext
				}
			}
		}
	}
}
