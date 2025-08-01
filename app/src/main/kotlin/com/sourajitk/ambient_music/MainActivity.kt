// SPDX-License-Identifier: MIT
// Copyright (c) 2025 Sourajit Karmakar

package com.sourajitk.ambient_music

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sourajitk.ambient_music.data.GitHubRelease
import com.sourajitk.ambient_music.ui.dialog.UpdateInfoDialog
import com.sourajitk.ambient_music.ui.navigation.MainAppNavigation
import com.sourajitk.ambient_music.ui.theme.AmbientMusicTheme
import com.sourajitk.ambient_music.util.InAppUpdateManager
import com.sourajitk.ambient_music.util.UpdateChecker

class MainActivity : ComponentActivity() {

  private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
      if (isGranted) {
        Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(
            this,
            "Notification permission denied. Features may be limited.",
            Toast.LENGTH_LONG,
          )
          .show()
      }
    }

  private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations("com.sourajitk.ambient_music")
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
  @SuppressLint("SourceLockedOrientationActivity")
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    setTheme(R.style.Theme_AmbientMusic)
    InAppUpdateManager.checkForUpdate(this)

    // Basically, this is not a "real" notification, it's for MediaSession.
    if (
      ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
      requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    setContent {
      val context = LocalContext.current
      if (!isIgnoringBatteryOptimizations(context)) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, ("package:" + BuildConfig.APPLICATION_ID).toUri())
        context.startActivity(intent)
      }
      AmbientMusicTheme {
        var updateInfo by remember { mutableStateOf<GitHubRelease?>(null) }
        val windowSizeClass = calculateWindowSizeClass(this)
        LaunchedEffect(key1 = true) {
          val update = UpdateChecker.checkForUpdate(context)
          update?.let { updateInfo = it }
        }
        MainAppNavigation(windowSizeClass = windowSizeClass)
        updateInfo?.let { release ->
          UpdateInfoDialog(releaseInfo = release, onDismissRequest = { updateInfo = null })
        }
      }
    }
  }
}
