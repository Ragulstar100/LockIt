package com.compose.overlay


import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.view.Display
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.manway.lockit.overlay.LifeCycle

abstract class ComposeOverlayViewService : ViewReadyService() {

    private val layoutParams by lazy {
        WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
    }

    private var overlayOffset by mutableStateOf(Offset.Zero)

    private val windowManager by lazy {
        overlayContext.getSystemService(WindowManager::class.java)
    }

    private val composeView by lazy {
        ComposeView(overlayContext)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()

        LifeCycle.LifeCycleSet(composeView, viewModelStore, this)

        composeView.setViewTreeSavedStateRegistryOwner(this)

        composeView.setContent { Content() }


        windowManager.addView(composeView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(composeView)
    }

    @Composable
    abstract fun Content()


    @Composable
    internal fun OverlayDraggableContainer(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) =
        Box(modifier = modifier.fillMaxSize().pointerInput(Unit) {
//           detectDragGestures { change, dragAmount ->
//                    change.consumeAllChanges()
//
//                    val newOffset = overlayOffset + dragAmount
//                    overlayOffset = newOffset
//
//                    layoutParams.apply {
//                        x = overlayOffset.x.roundToInt()
//                        y = overlayOffset.y.roundToInt()
//                    }
//                    windowManager.updateViewLayout(composeView, layoutParams)
//                }
            },
            content = content
        )
}

abstract class ViewReadyService : LifecycleService(), SavedStateRegistryOwner, ViewModelStoreOwner {

    private val savedStateRegistryController: SavedStateRegistryController by lazy(
        LazyThreadSafetyMode.NONE
    ) {
        SavedStateRegistryController.create(this)
    }

    private val internalViewModelStore: ViewModelStore by lazy {
        ViewModelStore()
    }

    internal val overlayContext: Context by lazy {
        val defaultDisplay: Display =
            getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createDisplayContext(defaultDisplay)
                .createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null)
        } else {
            applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
}