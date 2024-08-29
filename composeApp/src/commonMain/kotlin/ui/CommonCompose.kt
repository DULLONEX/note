package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.confirm
import note.composeapp.generated.resources.demo
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.theme.TransparentOutlinedTextFieldColors
import ui.theme.timeShadow

/**
 * 用于水平显示的动画
 *
 * @param isShow 是否显示
 * @param inOffsetX 进x计算
 * @param outOffsetX 出x计算
 * @param durationMillis 持续时间
 * @param context compose
 */
@Composable
fun HorizontalAnimatedVisibility(
    isShow: Boolean = true,
    inOffsetX: (Int) -> Int = { it / 2 },
    outOffsetX: (Int) -> Int = { it / 2 },
    durationMillis: Int = 300,
    context: @Composable () -> Unit
) {
    AnimatedVisibility(
        isShow, enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> inOffsetX(fullWidth) },
            animationSpec = tween(durationMillis = durationMillis)
        ) + fadeIn(), exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> outOffsetX(fullWidth) },
            animationSpec = tween(durationMillis = durationMillis)
        ) + fadeOut()
    ) {
        context()
    }
}

@Composable
fun AnimationTextShow(
    modifier: Modifier = Modifier,
    text: String = "",
    style: TextStyle = LocalTextStyle.current,
    isFirst: Boolean = true
) {
    // State to track if it is the first time or not
    var isFirstTime by remember { mutableStateOf(isFirst) }
    var visible by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    // LaunchedEffect to handle the message update and animation logic
    LaunchedEffect(text) {
        // If it's the first time, set the message directly
        if (isFirstTime) {
            message = text
            isFirstTime = false
        } else {
            // Otherwise, perform the animation
            // First, make it disappear
            visible = false
            delay(500)
            // Update the message
            message = text
            // Make it appear again
            visible = true
        }
    }

    // Use AnimatedVisibility to handle the visibility animation
    Box {
        AnimatedVisibility(
            visible = visible,
            enter = if (isFirstTime) fadeIn(animationSpec = tween(durationMillis = 0)) else slideInVertically(
                initialOffsetY = { it / 10 }, // 从上进来
                animationSpec = tween(durationMillis = 350)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = if (isFirstTime) fadeOut(animationSpec = tween(durationMillis = 0)) else slideOutVertically(
                targetOffsetY = { -it / 10 }, // 从下消失
                animationSpec = tween(durationMillis = 350)
            ) + fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            Text(message, modifier = modifier, style = style)
        }
    }
}


fun Modifier.fadingEdge(brush: Brush) =
    this.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }


@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    numbers: IntRange = (0..100),
    defaultSelectedNumber: Int = 0,
    onChange: (Int) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val outline = MaterialTheme.colorScheme.outline

    val fadingEdgeGradient = remember {
        Brush.verticalGradient(
            0f to Color.Transparent, 0.5f to Color.Black, 1f to Color.Transparent
        )
    }
    LaunchedEffect(defaultSelectedNumber) {
        listState.scrollToItem(defaultSelectedNumber)
    }


    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }.filter { !it } // 仅在滚动停止时触发
            .collect {
                // 在滚动停止时计算需要的行为
                if (listState.firstVisibleItemScrollOffset > 50) {
                    listState.animateScrollToItem(listState.firstVisibleItemIndex + 1, 0)
                } else {
                    listState.animateScrollToItem(listState.firstVisibleItemIndex, 0)
                }
                onChange(listState.firstVisibleItemIndex)
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.height(120.dp)  // Height to fit exactly 3 items
            .width(100.dp).fadingEdge(fadingEdgeGradient).drawWithContent {
                drawContent()

                // Draw a horizontal line
                drawLine(
                    color = outline,
                    start = Offset(x = 0f, y = 40.dp.toPx()),
                    end = Offset(x = size.width, y = 40.dp.toPx()),
                    strokeWidth = 2f
                )

                drawLine(
                    color = outline,
                    start = Offset(x = 0f, y = 80.dp.toPx()),
                    end = Offset(x = size.width, y = 80.dp.toPx()),
                    strokeWidth = 2f
                )
            },
    ) {

        item {
            Box(
                modifier = Modifier.height(40.dp)  // Each item has a height of 40.dp
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "", fontSize = 24.sp, textAlign = TextAlign.Center
                )
            }
        }
        items(numbers.toList(), key = {it}) { value ->
            Box(
                modifier = Modifier.height(40.dp)  // Each item has a height of 40.dp
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
        item {
            Box(
                modifier = Modifier.height(40.dp)  // Each item has a height of 40.dp
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "", fontSize = 24.sp, textAlign = TextAlign.Center
                )
            }
        }

    }


}


@Composable
fun LookOut(
    modifier: Modifier = Modifier,
    message: StringResource,
    isShow: Boolean = false,
    onShowChange: (Boolean) -> Unit = {}
) {
    if (isShow) {
        AlertDialog(
            onDismissRequest = { onShowChange(!isShow) },
            text = { Text(stringResource(message)) },
            confirmButton = {
                Button(onClick = {
                    onShowChange(!isShow)
                }) {
                    Text(stringResource(Res.string.confirm))
                }
            },
            modifier = modifier.width(700.dp).timeShadow()
        )
    }

}

@Composable
fun MyNewIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(5.dp).background(
            color = MaterialTheme.colorScheme.secondaryContainer,
            RoundedCornerShape(20.dp),
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {}
}


@Composable
fun TextInputCompose(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    placeholderCompose: @Composable () -> Unit = {},
    leadingIconCompose: @Composable () -> Unit = {},
    textStyle: TextStyle = LocalTextStyle.current,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onChange,
        placeholder = {
            placeholderCompose()
        },
        leadingIcon = {
            leadingIconCompose()
        },
        textStyle = textStyle,
        singleLine = singleLine,
        colors = MaterialTheme.colorScheme.TransparentOutlinedTextFieldColors
    )
}


@Composable
fun CenteredTextField(
    text: String, onTextChange: (String) -> Unit, modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val textFieldValue = remember { mutableStateOf(TextFieldValue(text)) }

    LaunchedEffect(Unit) {
        textFieldValue.value = textFieldValue.value.copy(
            selection = TextRange(textFieldValue.value.text.length) // 光标始终在文本末尾
        )

        focusRequester.requestFocus() // 请求焦点
        keyboardController?.show() // 显示软键盘
    }
    // todo 没有文字的时候光标不显示在中心
    Box(
        contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth() // 使Box填满父容器宽度
    ) {
        OutlinedTextField(
            value = textFieldValue.value,
            onValueChange = {
                textFieldValue.value = it
                onTextChange(it.text)
            },
            modifier = Modifier.align(Alignment.Center) // 确保TextField居中对齐
                .widthIn(min = 100.dp)  // 设置最小宽度，宽度可以随输入内容扩展
                .padding(horizontal = 16.dp).focusRequester(focusRequester), // 给TextField一些水平内边距
            textStyle = TextStyle(textAlign = TextAlign.Center) // 文本居中对齐
        )
    }

}


@Composable
fun PictureViewer(
    modifier: Modifier = Modifier, onDismissRequest: () -> Unit, bitmap: ImageBitmap
) {
    Dialog(
        onDismissRequest = onDismissRequest, DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.5f)).zIndex(10F),
            contentAlignment = Alignment.Center
        ) {
            Dialog(onDismissRequest = onDismissRequest) {
                Image(bitmap, "")
            }
        }
    }
}


class NoRippleInteractionSource : MutableInteractionSource {

    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true

}


