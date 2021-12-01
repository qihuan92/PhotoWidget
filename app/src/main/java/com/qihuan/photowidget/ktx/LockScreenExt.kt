package com.qihuan.photowidget.ktx

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.qihuan.photowidget.App
import com.qihuan.photowidget.ScreenLockAdminReceiver

/**
 * LockScreenExt
 * @author qi
 * @since 2021/12/1
 */
fun Context.lockScreen() {
    val devicePolicyManager = getSystemService(DevicePolicyManager::class.java)
    val lockReceiverComponentName = ComponentName(this, ScreenLockAdminReceiver::class.java)
    if (devicePolicyManager.isAdminActive(lockReceiverComponentName)) {
        devicePolicyManager.lockNow()
    }
}

fun Context.hasLockScreenPermission(): Boolean {
    val devicePolicyManager = getSystemService(DevicePolicyManager::class.java)
    val lockReceiverComponentName = ComponentName(this, ScreenLockAdminReceiver::class.java)
    return devicePolicyManager.isAdminActive(lockReceiverComponentName)
}

class LockScreenPermissionResultContract : ActivityResultContract<String, Boolean>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(
                DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                ComponentName(context, ScreenLockAdminReceiver::class.java)
            )
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        if (resultCode == Activity.RESULT_OK && App.context.hasLockScreenPermission()) {
            return true
        }
        return false
    }
}