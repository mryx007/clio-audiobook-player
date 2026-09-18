package de.clio.features.bookOverview.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Immutable
private class SmoothMenuPositionProvider(
  private val contentOffset: DpOffset,
  private val density: Density,
) : PopupPositionProvider {
  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize,
  ): IntOffset {
    val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
    val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

    val x = if (layoutDirection == LayoutDirection.Ltr) {
      val rightAligned = anchorBounds.right - popupContentSize.width + contentOffsetX
      if (rightAligned >= 0) rightAligned else anchorBounds.left + contentOffsetX
    } else {
      val leftAligned = anchorBounds.left + contentOffsetX
      if (leftAligned + popupContentSize.width <=
        windowSize.width
      ) {
        leftAligned
      } else {
        anchorBounds.right - popupContentSize.width + contentOffsetX
      }
    }.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))

    val y = if (anchorBounds.bottom + popupContentSize.height + contentOffsetY <= windowSize.height) {
      anchorBounds.bottom + contentOffsetY
    } else if (anchorBounds.top - popupContentSize.height - contentOffsetY >= 0) {
      anchorBounds.top - popupContentSize.height - contentOffsetY
    } else {
      anchorBounds.bottom + contentOffsetY
    }.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0))

    return IntOffset(x, y)
  }
}

@Composable
internal fun SmoothDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 0.dp),
  properties: PopupProperties = PopupProperties(focusable = true),
  content: @Composable ColumnScope.() -> Unit,
) {
  val transitionState = remember {
    MutableTransitionState(false)
  }

  LaunchedEffect(expanded) {
    transitionState.targetState = expanded
  }

  if (expanded || transitionState.currentState || transitionState.targetState) {
    val density = LocalDensity.current
    val positionProvider = remember(offset, density) {
      SmoothMenuPositionProvider(offset, density)
    }

    Popup(
      popupPositionProvider = positionProvider,
      onDismissRequest = onDismissRequest,
      properties = properties,
    ) {
      AnimatedVisibility(
        visibleState = transitionState,
        enter = fadeIn(animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)) +
          scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin(1f, 0f),
            animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing),
          ),
        exit = fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)) +
          scaleOut(
            targetScale = 0.8f,
            transformOrigin = TransformOrigin(1f, 0f),
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
          ),
      ) {
        Surface(
          modifier = modifier,
          shape = MaterialTheme.shapes.extraSmall,
          color = MaterialTheme.colorScheme.surfaceContainer,
          shadowElevation = 4.dp,
        ) {
          Column(
            modifier = Modifier
              .padding(vertical = 8.dp)
              .width(IntrinsicSize.Max)
              .verticalScroll(rememberScrollState()),
            content = content,
          )
        }
      }
    }
  }
}
