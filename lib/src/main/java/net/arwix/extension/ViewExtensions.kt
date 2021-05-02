package net.arwix.extension

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

public suspend fun View.awaitOnNextLayout() = suspendCancellableCoroutine<Boolean> {
    val viewRef = this.weak()
    val listener =object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            view.removeOnLayoutChangeListener(this)
            it.resume(true)
        }
    }
    addOnLayoutChangeListener(listener)
    it.invokeOnCancellation { viewRef.runWeak { removeOnLayoutChangeListener(listener) } }
}

public suspend fun View.awaitOnLayout() {
    if (ViewCompat.isLaidOut(this) && !isLayoutRequested) return
    this.awaitOnNextLayout()
}

fun <T : View> T.visible(): T {
    visibility = View.VISIBLE
    return this
}

fun <T : View> T.gone(): T {
    visibility = View.GONE
    return this
}

fun <T : View> T.invisible(): T {
    visibility = View.INVISIBLE
    return this
}

fun View.setBackgroundDrawableCompat(idDrawable: Int) {
    this.background = DrawableCompat.wrap(ContextCompat.getDrawable(this.context, idDrawable)!!)
}

fun View.hideSoftInputFromWindow() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
        it as InputMethodManager
    } ?: return
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun TextView.setTextAppearanceCompat(resId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.setTextAppearance(resId)
    } else {
        this.setTextAppearance(this.context, resId)
    }
}

fun View.toSp(spValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, this.resources.displayMetrics)

fun View.toDp(dpValue: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, this.resources.displayMetrics)

fun View.toSp(spValue: Int) = toSp(spValue.toFloat())

fun View.toDp(dpValue: Int) = toDp(dpValue.toFloat())

class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val viewBindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null
    private val clearBindingHandler by lazy(LazyThreadSafetyMode.NONE) { Handler(Looper.getMainLooper()) }

    init {

        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observe(fragment) {
                    it.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        override fun onDestroy(owner: LifecycleOwner) {
                            // Lifecycle listeners are called before onDestroyView in a Fragment.
                            // However, we want views to be able to use bindings in onDestroyView
                            // to do cleanup so we clear the reference one frame later.
                            clearBindingHandler.post { binding = null }
                        }
                    })
                }
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        // onCreateView may be called between onDestroyView and next Main thread cycle.
        // In this case [binding] refers to the previous fragment view. Check that binding's root view matches current fragment view
        if (binding != null && binding?.root !== thisRef.view) {
            binding = null
        }

        val binding = binding
        if (binding != null) return binding
        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED))
            throw  IllegalStateException("bindings not allow when fragment views are destroyed")
        return viewBindingFactory(thisRef.requireView()).also { this.binding = it }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
    FragmentViewBindingDelegate(this, viewBindingFactory)

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) { bindingInflater(layoutInflater) }

inline fun <T: ViewBinding> RecyclerView.ViewHolder.viewBinding(
    crossinline viewBindingFactory: (View) -> T
) = lazy(LazyThreadSafetyMode.NONE) { viewBindingFactory(itemView) }

/**
 * Fade a view to visible or gone. This function is idempotent - it can be called over and over again with the same
 * value without affecting an in-progress animation.
 */
fun View.fadeTo(visible: Boolean, duration: Long = 500, startDelay: Long = 0, toAlpha: Float = 1f) {
    // Make this idempotent.
    val tagKey = "fadeTo".hashCode()
    if (visible == isVisible && animation == null && getTag(tagKey) == null) return
    if (getTag(tagKey) == visible) return

    setTag(tagKey, visible)
    setTag("fadeToAlpha".hashCode(), toAlpha)

    if (visible && alpha == 1f) alpha = 0f
    animate()
        .alpha(if (visible) toAlpha else 0f)
        .withStartAction {
            if (visible) isVisible = true
        }
        .withEndAction {
            setTag(tagKey, null)
            if (isAttachedToWindow && !visible) isVisible = false
        }
        .setInterpolator(FastOutSlowInInterpolator())
        .setDuration(duration)
        .setStartDelay(startDelay)
        .start()
}

/**
 * Cancels the animation started by [fadeTo] and jumps to the end of it.
 */
fun View.cancelFade() {
    val tagKey = "fadeTo".hashCode()
    val visible = getTag(tagKey)?.castOrNull<Boolean>() ?: return
    animate().cancel()
    isVisible = visible
    alpha = if (visible) getTag("fadeToAlpha".hashCode())?.castOrNull<Float>() ?: 1f else 0f
    setTag(tagKey, null)
}

/**
 * Cancels the fade for this view and any ancestors.
 */
fun View.cancelFadeRecursively() {
    cancelFade()
    castOrNull<ViewGroup>()?.children?.asSequence()?.forEach { it.cancelFade() }
}

private inline fun <reified T> Any.castOrNull(): T? {
    return this as? T
}