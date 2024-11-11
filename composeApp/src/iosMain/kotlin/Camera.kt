import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.zIndex
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureStillImageOutput
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecJPEG
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.hasTorch
import platform.AVFoundation.position
import platform.AVFoundation.torchMode
import platform.CoreGraphics.CGRect
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIView
import ui.screen.CameraCompose
import ui.screen.PictureViewerConfirm
import ui.viewmodel.FileViewModel


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


@OptIn(ExperimentalForeignApi::class)
@Composable
fun CameraShoot(
    modifier: Modifier = Modifier,
    back: () -> Unit,
    shoot: (ImageBitmap) -> Unit
) {
    val platform: Platform = getKoin().get()

    val scope = rememberCoroutineScope()
    var isUsingBackCamera by remember { mutableStateOf(true) }      // 控制前置后置
    var isFlashOn by remember { mutableStateOf(false) }         // 控制闪光灯

    val session = remember { AVCaptureSession() }

    fun getCameraDevice(position: AVCaptureDevicePosition): AVCaptureDevice? {
        return AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo).firstOrNull { device ->
            (device as AVCaptureDevice).position == position
        } as? AVCaptureDevice
    }

    fun configureSession() {
        session.beginConfiguration()
        session.inputs.forEach { session.removeInput(it as AVCaptureInput) }

        val cameraDevice =
            getCameraDevice(if (isUsingBackCamera) AVCaptureDevicePositionBack else AVCaptureDevicePositionFront)
        val input =
            AVCaptureDeviceInput.deviceInputWithDevice(cameraDevice!!, null) as AVCaptureDeviceInput

        session.addInput(input)
        session.commitConfiguration()
        session.startRunning()
        if (cameraDevice.hasTorch) {
            cameraDevice.lockForConfiguration(null)
            cameraDevice.torchMode = if (isFlashOn) AVCaptureTorchModeOn else AVCaptureTorchModeOff
            cameraDevice.unlockForConfiguration()
        }
    }


    configureSession()
    val cameraPreviewLayer = remember { AVCaptureVideoPreviewLayer(session = session) }


    CameraCompose(modifier = modifier, back = back, cameraPreview = {
        { container: UIView, rect: CValue<CGRect> ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            container.layer.setFrame(rect)
            cameraPreviewLayer.setFrame(rect)
            CATransaction.commit()
        }
        UIKitView<UIView>(
            factory = {
                val container = UIView()
                container.layer.addSublayer(cameraPreviewLayer)
                cameraPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                session.startRunning()
                container
            },
            modifier = Modifier.fillMaxSize(),
            properties = UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true
            ),
            update = {
                CATransaction.begin()
                CATransaction.setValue(true, kCATransactionDisableActions)
                cameraPreviewLayer.frame = it.bounds
                CATransaction.commit()
            }
        )
    }, isBackFacing = isUsingBackCamera,
        switchBackOrFacing = { isUsingBackCamera = !isUsingBackCamera },
        isOpenFlash = isFlashOn,
        switchOpenFlash = { isFlashOn = !isFlashOn },
        shoot = {
            scope.launch {
                val output = AVCaptureStillImageOutput().apply {
                    outputSettings = mapOf(AVVideoCodecKey to AVVideoCodecJPEG)
                    session.addOutput(this)
                }
                val connection = output.connectionWithMediaType(AVMediaTypeVideo)
                output.captureStillImageAsynchronouslyFromConnection(connection!!) { buffer, error ->
                    if (buffer != null) {
                        val imageData =
                            AVCaptureStillImageOutput.jpegStillImageNSDataRepresentation(buffer)!!
                        shoot(imageData.toImageBitmap())
                    }
                }


            }
        }

    )


}
