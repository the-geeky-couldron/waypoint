package com.waypoint.app

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class LauncherIconTest {
    @Test fun adaptiveAndWallpaperTintedLayersRender() {
        val inst=InstrumentationRegistry.getInstrumentation()
        val output=File(inst.targetContext.getExternalFilesDir(null),"navigation-qa").apply{mkdirs()}
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                (activity.application as WaypointApplication).navigation.setVisible(false)
                val info=activity.packageManager.getApplicationInfo(activity.packageName,0)
                assertEquals(R.mipmap.ic_launcher,info.icon)
                // Samsung may wrap/theme getApplicationIcon(); inspect the declared resource.
                val original=activity.resources.getDrawable(info.icon,activity.theme)
                assertTrue("Declared icon is ${original.javaClass.name}",original is AdaptiveIconDrawable)
                if(Build.VERSION.SDK_INT>=33)assertNotNull((original as AdaptiveIconDrawable).monochrome)
                val light=if(Build.VERSION.SDK_INT>=31)dynamicLightColorScheme(activity) else lightColorScheme()
                val dark=if(Build.VERSION.SDK_INT>=31)dynamicDarkColorScheme(activity) else darkColorScheme()
                fun themed(scheme:ColorScheme)=AdaptiveIconDrawable(ColorDrawable(scheme.primaryContainer.toArgb()),
                    activity.getDrawable(R.drawable.ic_launcher_monochrome)!!.mutate().apply{setTint(scheme.onPrimaryContainer.toArgb())}).toBitmap(512,512)
                val icons=listOf("Original" to original.toBitmap(512,512),"Material You · light" to themed(light),"Material You · dark" to themed(dark))
                File(output,"launcher-icon.png").outputStream().use { icons[0].second.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
                activity.setContent { MaterialTheme(colorScheme=darkColorScheme()) {
                    Column(Modifier.fillMaxSize().background(Color(0xff141815)).statusBarsPadding().navigationBarsPadding().padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                        Text("Waypoint",color=Cream,style=MaterialTheme.typography.headlineLarge)
                        Text("Adaptive icon · native Android rendering",color=Cream,style=MaterialTheme.typography.bodyMedium)
                        icons.forEach { (label,bitmap) -> Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(24.dp)) {
                            Image(bitmap.asImageBitmap(),label,Modifier.size(112.dp))
                            Column(verticalArrangement=Arrangement.spacedBy(12.dp)) { Text(label,color=Cream);Image(bitmap.asImageBitmap(),null,Modifier.size(48.dp)) }
                        } }
                    }
                } }
            }
            SystemClock.sleep(1000)
            UiDevice.getInstance(inst).takeScreenshot(File(output,"launcher-variants.png"))
        }
    }
}
