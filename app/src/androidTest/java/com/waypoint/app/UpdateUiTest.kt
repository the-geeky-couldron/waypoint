package com.waypoint.app

import android.content.Intent
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class UpdateUiTest {
    @Test fun settingsShowsInstalledVersionAndCompletesManualCheck() {
        val inst = InstrumentationRegistry.getInstrumentation()
        val context = inst.targetContext
        val device = UiDevice.getInstance(inst)
        device.wakeUp()
        context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        val settings = device.wait(Until.findObject(By.desc("Settings")), 6000)
        assertNotNull(settings); settings!!.click()
        assertTrue(device.wait(Until.hasObject(By.text("App updates")), 5000))
        assertTrue(device.hasObject(By.text("Waypoint ${BuildConfig.VERSION_NAME}")))
        val output = File(context.getExternalFilesDir(null), "readme").apply { mkdirs() }
        device.takeScreenshot(File(output,"updates-settings.png"))
        val check = device.findObject(By.text("Check for updates"))
        assertNotNull(check); check.click()
        val deadline = SystemClock.elapsedRealtime() + 45000
        while (SystemClock.elapsedRealtime() < deadline && !device.hasObject(By.text("You're up to date.")) && !device.hasObject(By.text("Try again")) && !device.hasObject(By.text("Download update"))) SystemClock.sleep(200)
        assertTrue(device.hasObject(By.text("You're up to date.")) || device.hasObject(By.text("Try again")) || device.hasObject(By.text("Download update")))
        SystemClock.sleep(500);device.takeScreenshot(File(output,"updates-live-check.png"))
        device.pressBack()
        assertTrue(device.wait(Until.hasObject(By.text("Open map")), 4000))
    }
}
