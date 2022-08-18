package com.example.cubicasaplugin

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.google.ar.core.ArCoreApk
import cubi.casa.exampleproject.ScanActivity
import cubi.casa.exampleproject.permissionIsGranted
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "baunstrup.dk/cubicasa"
    private var mUserRequestedInstall = true

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            if (call.method == "getCCPayload") {
                val ccPayloadLocation = getCCPayload()

                if (ccPayloadLocation.isNotEmpty()) {
                    result.success(ccPayloadLocation)
                } else {
                    result.error("UNAVAILABLE", "Not possible to get CCPayload", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun getCCPayload(): String {

        /* Requesting 'ACCESS_FINE_LOCATION' permission on the app side because we've set
        * 'CubiCapture.trueNorth' to 'TrueNorth.ENABLED' in ScanActivity.kt */
        if (!permissionIsGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, 321, openSettings = false)
        }
        /* Check if 'CAMERA' permission is granted, and ARCore is installed and up-to-date.
         * You should also explain user why you need these permission,
         * and handle a case where user denies a permission. */
        if (!permissionIsGranted(Manifest.permission.CAMERA)) {
            requestPermission(Manifest.permission.CAMERA, 123, openSettings = true)
            return "No permission for cam"
        }
        if (!arCoreIsInstalledAndUpToDate()) {
            return "AR Core is not installed or UpToDate"
        }

        val RESULT = 1
        val scanIntent = Intent(baseContext, ScanActivity::class.java)
        scanIntent.putExtra("orderInfo", "Test")
        scanIntent.putExtra("propertyType", 3)
        startActivityForResult(scanIntent, RESULT);



        return "Hello from Android"
    }

    /* Checks if the ARCore installed and is up-to-date. Returns true if it is, false otherwise.
 * Also requests install or update if needed */
    private fun arCoreIsInstalledAndUpToDate(): Boolean {
        return when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                mUserRequestedInstall = false
                false
            }
            ArCoreApk.InstallStatus.INSTALLED -> { true }
            else -> { false }
        }
    }

    private fun requestPermission(permission: String, requestCode: Int, openSettings: Boolean) {
        if (!shouldShowRequestPermissionRationale(permission)) {
            // Permission can be requested, request it
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else if (openSettings) {
            // Permission can't be requested, open settings
            startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
            )
            Toast.makeText(baseContext, "Please grant permission", Toast.LENGTH_SHORT).show()
        }
    }

}
