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

/** Run manually against an older development build after a signed release is published. */
@RunWith(AndroidJUnit4::class)
class UpdateDownloadTest {
    @Test fun downloadsAndVerifiesPublishedRelease() {
        org.junit.Assume.assumeTrue("Manual live-release test", InstrumentationRegistry.getArguments().getString("liveUpdate") == "true")
        val inst = InstrumentationRegistry.getInstrumentation()
        val context = inst.targetContext
        val device = UiDevice.getInstance(inst)
        val config = Configurator.getInstance()
        val oldIdle = config.waitForIdleTimeout
        config.waitForIdleTimeout = 350
        val output = File(context.getExternalFilesDir(null), "update-review").apply { mkdirs() }
        try {
            device.wakeUp()
            context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            device.wait(Until.findObject(By.desc("Settings")), 6000)!!.click()
            device.wait(Until.findObject(By.text("Check for updates")), 6000)!!.click()
            val download = device.wait(Until.findObject(By.text("Download update")), 45000)
            assertNotNull("Install an older development build before this live-release test", download)
            device.takeScreenshot(File(output, "update-available.png"))
            download!!.click()
            val cancel = device.wait(Until.findObject(By.text("Cancel download")), 6000)
            assertNotNull(cancel)
            device.takeScreenshot(File(output, "update-downloading.png"))
            cancel!!.click()
            assertTrue(device.wait(Until.hasObject(By.text("Download update")), 6000))
            SystemClock.sleep(500)
            device.findObject(By.text("Download update")).click()
            assertTrue("Download did not pass verification", device.wait(Until.hasObject(By.text("Install update")), 240000))
            assertTrue(File(context.cacheDir, "updates/Waypoint.apk").length() > 1_000_000)
            assertFalse(File(context.cacheDir, "updates/update.part").exists())
            device.takeScreenshot(File(output, "update-ready.png"))
            // Opening Android's installation UI must never silently grant installation permission.
            device.findObject(By.text("Install update")).click()
            SystemClock.sleep(1000)
            assertFalse("Installer should hand control to Android", device.currentPackageName == context.packageName)
            device.takeScreenshot(File(output, "update-android-handoff.png"))
            device.pressBack()
            assertTrue(device.wait(Until.hasObject(By.text("Install update")), 6000))
            device.pressBack()
        } catch (error: Throwable) {
            device.takeScreenshot(File(output,"update-failure.png"))
            device.dumpWindowHierarchy(File(output,"update-failure.xml"))
            throw error
        } finally { config.waitForIdleTimeout = oldIdle }
    }
}
