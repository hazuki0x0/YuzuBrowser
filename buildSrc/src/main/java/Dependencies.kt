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
    const val androidX = "1.0.0"
    const val support_fix_lib = androidX
    const val support_constraint_lib = "1.1.3"
    const val dagger2 = "2.20"
    const val jackson = "2.9.5"
    const val kotlin = "1.3.11"
    const val kotlin_coroutines = "1.1.0"
    const val kotlin_anko = "0.10.8"
    const val junit = "4.12"
    const val espresso = "3.1.0"
    const val runner = "1.1.0"
}

object Libs {
    val support_annotations = "androidx.annotation:annotation:${Versions.androidX}"
    val support_appcompat_v7 = "androidx.appcompat:appcompat:${Versions.androidX}"
    val support_design = "com.google.android.material:material:${Versions.androidX}"
    val support_recyclerview = "androidx.recyclerview:recyclerview:${Versions.androidX}"
    val support_constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.support_constraint_lib}"
    val support_fix_preference = "com.takisoft.preferencex:preferencex:${Versions.support_fix_lib}"
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
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    val androidRunner = "androidx.test:runner:${Versions.runner}"
}

object Dependencies {
    val kotlin_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}