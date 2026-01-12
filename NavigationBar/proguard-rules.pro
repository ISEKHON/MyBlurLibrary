# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep all public classes and methods in navigationbar
-keep public class xyz.code.navigationbar.** {
    public *;
}

# Keep NavigationBar and NavBarContainer classes
-keep class xyz.code.navigationbar.NavigationBar { *; }
-keep class xyz.code.navigationbar.NavBarContainer { *; }
