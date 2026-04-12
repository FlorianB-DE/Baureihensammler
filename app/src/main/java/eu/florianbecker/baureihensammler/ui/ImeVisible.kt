package eu.florianbecker.baureihensammler.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@Composable
fun rememberImeVisible(): Boolean {
    val view = LocalView.current
    var imeVisible by remember { mutableStateOf(false) }
    DisposableEffect(view) {
        val root = view.rootView
        fun update() {
            val insets = ViewCompat.getRootWindowInsets(root)
            imeVisible = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener { update() }
        root.viewTreeObserver.addOnGlobalLayoutListener(listener)
        root.post { update() }
        onDispose { root.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }
    return imeVisible
}
