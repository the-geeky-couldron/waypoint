# Android and MapLibre ship their own component/JNI consumer rules.
# Keep useful line numbers for private device crash diagnosis.
-keepattributes SourceFile,LineNumberTable

# Instrumentation and the app share the Kotlin runtime. Preserve its entry
# points so the test runner cannot call a method optimized out of the app copy.
-keep class kotlin.** { *; }
-keep class androidx.tracing.** { *; }
# Public state inspected by connected-device regression tests. Rendering and
# navigation implementations remain eligible for normal R8 optimization.
-keep class com.waypoint.app.WaypointApplication { *; }
-keepclassmembers class com.waypoint.app.NavigationController { public *; }
-keepclassmembers class com.waypoint.app.NavState { public *; }
-keepclassmembers class com.waypoint.app.World { *; }
-keepclassmembers class com.waypoint.app.Place { public *; }
