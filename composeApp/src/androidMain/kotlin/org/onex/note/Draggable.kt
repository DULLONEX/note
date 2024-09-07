package org.onex.note

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

enum class DragAnchors {
    Start, Center, End,
}


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun BehindMotionSwipeDemo() {
    val density = LocalDensity.current

    val defaultActionSize = 80.dp

    val actionSizePx = with(density) { defaultActionSize.toPx() }
    val endActionSizePx = with(density) { (defaultActionSize).toPx() }

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Center,
            anchors = DraggableAnchors {
                //  DragAnchors.Start at actionSizePx
                DragAnchors.Center at 0f
                DragAnchors.End at -endActionSizePx
            },
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(),
        )
    }
    DraggableItem(state = state, content = {
        Text(
            text = "Item",
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
        )
    }, startAction = {

    }, endAction = {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd),
        ) {
            DelAction(
                Modifier
                    .width(defaultActionSize)
                    .fillMaxHeight()
                    .offset {
                        IntOffset(
                            ((state.requireOffset() + endActionSizePx)).roundToInt(), 0
                        )
                    })
        }
    })
}


@Composable
fun DelAction(modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .padding(horizontal = 20.dp)
                    .size(22.dp),
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color.White
            )

            Text(
                text = "Save",
                color = Color.White,
                fontSize = 12.sp,
            )
        }
    }

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableItem(
    state: AnchoredDraggableState<DragAnchors>,
    content: @Composable BoxScope.() -> Unit,
    startAction: @Composable (BoxScope.() -> Unit)? = {},
    endAction: @Composable (BoxScope.() -> Unit)? = {}
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectangleShape)
    ) {

        endAction?.let {
            endAction()
        }

        startAction?.let {
            startAction()
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.CenterStart)
            .offset {
                IntOffset(
                    x = state
                        .requireOffset()
                        .roundToInt(),
                    y = 0,
                )
            }
            .anchoredDraggable(state, Orientation.Horizontal, reverseDirection = false),
            content = content)
    }
}