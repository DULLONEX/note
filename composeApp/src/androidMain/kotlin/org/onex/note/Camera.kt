package org.onex.note

import Platform
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import note.composeapp.generated.resources.Res
import note.composeapp.generated.resources.flash_off
import note.composeapp.generated.resources.flash_on
import note.composeapp.generated.resources.flip_camera
import org.jetbrains.compose.resources.vectorResource
import org.koin.java.KoinJavaComponent.getKoin
import org.koin.java.KoinJavaComponent.inject
import ui.screen.CameraCompose
import ui.screen.PictureViewerConfirm
import ui.viewmodel.FileViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier, back: () -> Unit, fileViewModel: FileViewModel
) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }


    if (bitmap != null) {
        PictureViewerConfirm(
            Modifier.zIndex(10f),
            bitmap!!,
            back = { bitmap = null },
            saveImage = {
                fileViewModel.saveImage(it)
                back()
            }
        )
    } else {
        CameraShoot(
            modifier = modifier, // 隐藏 CameraShoot
            shoot = {
                bitmap = it
            },
            back = back
        )
    }

}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun CameraShoot(
    modifier: Modifier = Modifier, shoot: (ImageBitmap) -> Unit, back: () -> Unit
) {

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }

    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }

    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }
    var sensorRotationDegrees by remember { mutableIntStateOf(0) }   // a
    val activityRotation = rotationDegrees(context.display?.rotation!!)  // b
    LaunchedEffect(lensFacing, isFlashMode) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        imageCapture.flashMode = isFlashMode
        val camera =
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
        sensorRotationDegrees = camera.cameraInfo.sensorRotationDegrees
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }


    // 照相页面


    CameraCompose(modifier = modifier.fillMaxSize(), back = back, cameraPreview = {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
    }, isBackFacing = lensFacing == CameraSelector.LENS_FACING_BACK, switchBackOrFacing = {
        lensFacing = if (it) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
    }, isOpenFlash = isFlashMode == ImageCapture.FLASH_MODE_ON, switchOpenFlash = {
        isFlashMode = if (it) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }, shoot = {
        scope.launch {
            // 计算旋转角度
            /**
             * @see rotationDegrees https://juejin.cn/post/6976220868003233829#heading-11
             */
            val rotationDegrees = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                (sensorRotationDegrees - activityRotation + 360) % 360
            } else {
                // 前置 默认会进行反转(反转为正常显示页面) (a+b)%360
                (sensorRotationDegrees + activityRotation) % 360
            }
            Log.d("CameraX", "rotationDegrees: $rotationDegrees   lensFacing: $lensFacing")
            shoot(captureImageAsImageBitmap(imageCapture, context, rotationDegrees))
        }
    })

}



// 捕获到图像
@RequiresApi(Build.VERSION_CODES.R)
private suspend fun captureImageAsImageBitmap(
    imageCapture: ImageCapture, context: Context, rotationDegrees: Int
): ImageBitmap {
    return suspendCancellableCoroutine { continuation ->
        imageCapture.takePicture(ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                @OptIn(ExperimentalGetImage::class)
                override fun onCaptureSuccess(image: ImageProxy) {

                    image.imageInfo.rotationDegrees
                    Log.d("CameraX", "Photo capture succeeded")
                    // 在这里可以处理 image 数据
                    continuation.resume(
                        image.toBitmap(rotationDegrees)
                            .asImageBitmap()
                    )
                    image.close()  // 记得在处理完后关闭 image
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                }
            })
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                // 将可能阻塞的调用移到IO线程
                val cameraProvider = runBlocking {
                    withContext(Dispatchers.IO) {
                        cameraProviderFuture.get()  // 阻塞操作
                    }
                }
                continuation.resume(cameraProvider)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

fun ImageProxy.toBitmap(rotationDegrees: Int): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    // 使用旋转角度旋转图像
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees.toFloat())

    // 返回旋转后的 Bitmap
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}


fun rotationDegrees(index: Int): Int {
    return when (index) {
        ROTATION_0 -> {
            0
        }

        ROTATION_90 -> {
            90
        }

        ROTATION_180 -> {
            180
        }

        ROTATION_270 -> {
            270
        }

        else -> {
            0
        }
    }
}

