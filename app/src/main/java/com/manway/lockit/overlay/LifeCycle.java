package com.manway.lockit.overlay;

import androidx.compose.ui.platform.ComposeView;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewTreeLifecycleOwner;
import androidx.lifecycle.ViewTreeViewModelStoreOwner;

public class LifeCycle {

    public static void LifeCycleSet(ComposeView composeView, androidx.lifecycle.ViewModelStore viewModelStore, LifecycleOwner viewTreeLifecycleOwner ) {
        ViewTreeLifecycleOwner.set(composeView,viewTreeLifecycleOwner);
        ViewTreeViewModelStoreOwner.set(composeView, () -> viewModelStore);
    }


}
