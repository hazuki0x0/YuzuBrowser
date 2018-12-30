object Build {
    const val compile_sdk_version = 28
    const val build_tools_version = "28.0.3"
    const val min_sdk_version = 21
    const val target_sdk_version = 26
}

object AppVersions {
    const val version_name = "4.2.1"
    const val version_code = 400202
}

object Versions {
    const val support_lib = "28.0.0"
    const val support_fix_lib = "$support_lib.0"
    const val support_constraint_lib = "1.1.3"
    const val dagger2 = "2.20"
    const val jackson = "2.9.5"
    const val kotlin = "1.3.11"
    const val kotlin_coroutines = "1.1.0"
    const val kotlin_anko = "0.10.8"
    const val junit = "4.12"
    const val espresso = "3.0.2"
    const val runner = "1.0.2"
}

object Libs {
    val support_annotations = "com.android.support:support-annotations:${Versions.support_lib}"
    val support_appcompat_v7 = "com.android.support:appcompat-v7:${Versions.support_lib}"
    val support_design = "com.android.support:design:${Versions.support_lib}"
    val support_recyclerview = "com.android.support:recyclerview-v7:${Versions.support_lib}"
    val support_constraint_layout = "com.android.support.constraint:constraint-layout:${Versions.support_constraint_lib}"
    val support_fix_preference = "com.takisoft.fix:preference-v7:${Versions.support_fix_lib}"
    val jackson = "com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}"
    val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val kotlin_android_extensions = "org.jetbrains.kotlin:kotlin-android-extensions:${Versions.kotlin}"
    val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlin_coroutines}"
    val kotlin_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlin_coroutines}"
    val kotlin_anko_commons = "org.jetbrains.anko:anko-commons:${Versions.kotlin_anko}"
    val kotlin_anko_sql = "org.jetbrains.anko:anko-sqlite:${Versions.kotlin_anko}"
    val dagger2 = "com.google.dagger:dagger:${Versions.dagger2}"
    val dagger2_compiler = "com.google.dagger:dagger-compiler:${Versions.dagger2}"
    val junit = "junit:junit:${Versions.junit}"
    val espressoCore = "com.android.support.test.espresso:espresso-core:${Versions.espresso}"
    val androidRunner = "com.android.support.test:runner:${Versions.runner}"
}

object Dependencies {
    val kotlin_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}