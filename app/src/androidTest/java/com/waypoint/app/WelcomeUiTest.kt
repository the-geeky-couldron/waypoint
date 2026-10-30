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
class WelcomeUiTest {
    @Test fun welcomeSelectsAWorldBeforeOpeningTheMap() {
        val inst=InstrumentationRegistry.getInstrumentation();val context=inst.targetContext;val device=UiDevice.getInstance(inst)
        val output=File(context.getExternalFilesDir(null),"atlas-review").apply{mkdirs()}
        val config=Configurator.getInstance();val idle=config.waitForIdleTimeout;config.waitForIdleTimeout=350
        val font=device.executeShellCommand("settings get system font_scale").trim()
        val animation=device.executeShellCommand("settings get global animator_duration_scale").trim()
        try {
            inst.runOnMainSync{(context.applicationContext as WaypointApplication).navigation.clearDestination()}
            context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            assertTrue(device.wait(Until.hasObject(By.text("Open map")),10000));SystemClock.sleep(700)
            device.takeScreenshot(File(output,"welcome.png"))
            device.findObject(By.text("Explore all 27 game worlds")).click()
            val field=device.wait(Until.findObject(By.clazz("android.widget.EditText")),4000)!!
            field.text="Witcher 3";SystemClock.sleep(400)
            val choice=device.wait(Until.findObject(By.desc("Apply THE CONTINENT theme")),4000)
            assertNotNull(choice);device.takeScreenshot(File(output,"witcher-picker.png"));choice!!.click()
            assertTrue(device.wait(Until.hasObject(By.text("Open map")),4000))
            device.executeShellCommand("settings put system font_scale 1.3");SystemClock.sleep(1000)
            device.takeScreenshot(File(output,"welcome-large-text.png"))
            device.findObject(By.text("Open map")).click()
            assertTrue(device.wait(Until.hasObject(By.desc("Worlds")),5000))
            device.pressBack()
            assertTrue(device.wait(Until.hasObject(By.text("Open map")),4000))
            device.executeShellCommand("settings put global animator_duration_scale 0")
            device.executeShellCommand("settings put system font_scale 2.0")
            SystemClock.sleep(1000)
            device.takeScreenshot(File(output,"welcome-large-reduced-motion.png"))
            assertTrue(device.hasObject(By.text("Open map")))
        } catch(error:Throwable) {
            device.takeScreenshot(File(output,"welcome-failure.png"));device.dumpWindowHierarchy(File(output,"welcome-failure.xml"));throw error
        } finally {device.executeShellCommand("settings put system font_scale $font");device.executeShellCommand("settings put global animator_duration_scale $animation");config.waitForIdleTimeout=idle}
    }
}
