package org.onex.note

// 用于相机的测试用例

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ui.viewmodel.FileViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
@Preview
fun CameraPreviewScreen() {
    CameraScreen(Modifier, {}, FileViewModel())
}

@Composable
@Preview
fun PictureViewerPreview() {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxHeight(0.9f), contentAlignment = Alignment.Center) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
            ) {
                IconButton({}) {
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



            }
        }
        Row(
            Modifier
                .fillMaxWidth().fillMaxHeight(1f)
                .background(MaterialTheme.colorScheme.onBackground),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button({},Modifier.padding(end = 18.dp)) {
                Text("保存")
            }
        }


    }


}


