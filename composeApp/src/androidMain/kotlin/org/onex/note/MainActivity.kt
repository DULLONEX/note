package org.onex.note

import App
import Platform
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.compose.rememberNavController
import data.Database
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf
import org.onex.note.accredit.accreditList
import org.onex.note.accredit.showPermissionSettingsDialog
import org.onex.note.data.AndroidDatabaseDriverFactory


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 权限已授予
        } else {
            // 权限被拒绝
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CALENDAR)) {
                // 用户选择了“不再询问”，引导用户到设置页面
                showPermissionSettingsDialog(this)
            }
        }
    }

    private fun checkAndRequestPermission(accreditList: List<String>) {
        accreditList.forEach {
            when {
                ContextCompat.checkSelfPermission(
                    this, it
                ) == PermissionChecker.PERMISSION_GRANTED -> {
                    // 权限已经授予
                }

                !shouldShowRequestPermissionRationale(it) -> {
                    // 向用户解释为什么需要此权限，然后再次请求
                    // 向用户提交开启权限申请
                    requestPermissionLauncher.launch(it)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 首次申请权限 拒绝两次之后系统不再开启申请，需要用户执行转到设置进行开启
        checkAndRequestPermission(accreditList)
        setContent {
            val navController = rememberNavController()
            val platform = get<Platform> { parametersOf(navController, this) }
            platform.queryCalendarEvents()

            get<Database> { parametersOf(AndroidDatabaseDriverFactory(this)) }
            App()
        }
    }

}

