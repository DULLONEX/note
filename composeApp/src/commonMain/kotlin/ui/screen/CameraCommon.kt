package ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.flash_off
import note.composeapp.generated.resources.flash_on
import note.composeapp.generated.resources.flip_camera
import org.jetbrains.compose.resources.vectorResource


@Composable
fun CameraCompose(
    modifier: Modifier = Modifier,
    back: () -> Unit,
    cameraPreview: @Composable () -> Unit = {},
    isBackFacing: Boolean = true,
    switchBackOrFacing: (Boolean) -> Unit = {},
    isOpenFlash: Boolean = false,
    switchOpenFlash: (Boolean) -> Unit = {},
    shoot: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(50.dp)
                .align(Alignment.TopEnd)
                .zIndex(1f)
        ) {
            IconButton({
                back()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                )
            }
        }
        // 预览
        cameraPreview()

        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(50.dp)
                .align(Alignment.BottomCenter).padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.Center,

            ) {
            IconButton({
                switchOpenFlash(!isOpenFlash)
            }) {
                Icon(
                    imageVector = if (!isOpenFlash) vectorResource(Res.drawable.flash_off) else vectorResource(
                        Res.drawable.flash_on
                    ), contentDescription = "flash"
                )
            }
            Button(onClick = {
                shoot()
            }) {
                Text(text = "Capture Image")
            }
            IconButton({
                switchBackOrFacing(!isBackFacing)
            }) {
                Icon(
                    imageVector = vectorResource(Res.drawable.flip_camera),
                    contentDescription = "flip"
                )
            }
        }

    }
}




@Composable
fun PictureViewerConfirm(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap,
    back: () -> Unit,
    saveImage: (ImageBitmap) -> Unit
) {
    Column(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxHeight(0.9f), contentAlignment = Alignment.Center) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
            ) {
                IconButton({
                    back()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            ) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // 使用 Crop 让图片按比例填充整个 Box
                )

            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f)
                .background(MaterialTheme.colorScheme.onBackground),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button({
                saveImage(bitmap)
            }, Modifier.padding(end = 18.dp)) {
                Text("保存")
            }
        }


    }
}
