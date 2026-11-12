package com.omerakpul.localpdf.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.omerakpul.localpdf.R

/**
 * Android 10+ saves to Downloads/Pictures through MediaStore without any permission. Android 8-9
 * still needs WRITE_EXTERNAL_STORAGE, so wrap such actions with the returned function: it asks for
 * the permission when needed and then runs the action.
 */
@Composable
fun rememberLegacyStorageGate(): (() -> Unit) -> Unit {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = pending
        pending = null
        if (granted) {
            action?.invoke()
        } else {
            Toast.makeText(context, R.string.storage_permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    return { action ->
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pending = action
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            action()
        }
    }
}
