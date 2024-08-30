package org.onex.note

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ui.CenteredTextField
import ui.screen.chargeUp.AddChargeUpCompose
import ui.screen.chargeUp.AmountInputCompose


@Preview
@Composable
fun ImagePreview() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer, // 设置背景颜色为警告颜色
                    contentColor = MaterialTheme.colorScheme.error // 设置文字颜色为白色
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Show snackbar") },
                icon = { Icon(Icons.Filled.Add, contentDescription = "") },
                onClick = {
                    scope.launch {
                        // 如果 Snackbar 正在显示，则立即取消它
                        snackbarHostState.currentSnackbarData?.dismiss()

                        // 然后显示新的 Snackbar
                        snackbarHostState.showSnackbar("Snackbar")
                    }
                }
            )
        }
    ) { contentPadding ->
        // Screen content
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ChooseDateTimeComposePreview() {
    var text by remember { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    ModalBottomSheet(modifier = Modifier.heightIn(min = 300.dp), onDismissRequest = {
        show = !show
    }) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CenteredTextField(
                text = text, onTextChange = { text = it }, modifier = Modifier
            )
            Spacer(Modifier.heightIn(40.dp))
            Button({

            }) {
                Icon(Icons.Default.Done, "done")
            }
        }
    }
}

@Preview
@Composable
fun Add() {
    AddChargeUpCompose().AddChargeUpScreen()
}


@Preview
@Composable
fun AmountComposePreview() {
    AmountInputCompose(
        value = "111.1",
        onChange = {},
        modifier = Modifier.padding(horizontal = 16.dp),
        textStyle = MaterialTheme.typography.titleLarge
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun BasicPullToRefresh() {

    ModalBottomSheet(modifier = Modifier.heightIn(min = 200.dp), onDismissRequest = {}) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text("标题", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(50.dp)) {
                val aheadStart = TextButton({}) {
                    Text("提前开始")
                }
                val aheadFinish = TextButton({}) {
                    Text("提前完成")
                }
//                aheadStart
//                aheadFinish
            }

            FilledTonalButton({}) {
                Text("删除")
            }
        }
    }

}


@Composable
fun SelectImageFromGallery() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    //
    var imageBitmap = remember { mutableStateListOf<ImageBitmap?>(null) }

    val context = LocalContext.current

    // Launcher for selecting image
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // Convert Uri to ImageBitmap
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

            imageBitmap.add(bitmap?.asImageBitmap())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (imageBitmap.isNotEmpty()) {
            imageBitmap.forEach { bitmap ->
                if (bitmap is ImageBitmap) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(300.dp)
                            .padding(16.dp),
                        contentScale = ContentScale.Crop
                    )
                }

            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSelectImageFromGallery() {
    SelectImageFromGallery()
}
