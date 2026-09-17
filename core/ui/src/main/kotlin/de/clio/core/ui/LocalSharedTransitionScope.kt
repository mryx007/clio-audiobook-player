@file:Suppress("ktlint:compose:compositionlocal-allowlist")

package de.clio.core.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import de.clio.core.data.BookId

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalAppReady = staticCompositionLocalOf<() -> Unit> { {} }

fun sharedCoverKey(bookId: BookId): String = "book-cover-${bookId.value}"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedCoverElementModifier(bookId: BookId): Modifier {
  val sharedTransitionScope = LocalSharedTransitionScope.current
    ?: return this
  return with(sharedTransitionScope) {
    sharedElement(
      sharedContentState = rememberSharedContentState(key = sharedCoverKey(bookId)),
      animatedVisibilityScope = LocalNavAnimatedContentScope.current,
    )
  }
}
