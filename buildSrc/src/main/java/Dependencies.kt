/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

object Build {
    const val compile_sdk_version = 28
    const val build_tools_version = "28.0.3"
    const val min_sdk_version = 21
    const val target_sdk_version = 28
}

object AppVersions {
    const val version_name = "4.2.3"
    const val version_code = 400204
}

object Versions {
    const val androidX = "1.0.0"
    const val appCompat = "1.0.2"
    const val androidKTX = "1.0.1"
    const val support_fix_lib = androidX
    const val support_constraint_lib = "1.1.3"
    const val dagger = "2.21"
    const val kotshi = "1.0.6"
    const val okhttp = "3.13.1"
    const val okio = "2.2.2"
    const val kvs_schema = "5.1.0"
    const val kotlin = "1.3.21"
    const val kotlin_coroutines = "1.1.1"
    const val kotlin_anko = "0.10.8"
    const val junit = "4.12"
    const val assertk = "0.13"
    const val espresso = "3.1.1"
    const val runner = "1.1.0"
    const val header_decor = "0.2.8"
}

object Libs {
    //androidx libraries
    const val support_annotations = "androidx.annotation:annotation:${Versions.androidX}"
    const val support_appcompat_v7 = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val support_design = "com.google.android.material:material:${Versions.androidX}"
    const val support_recyclerview = "androidx.recyclerview:recyclerview:${Versions.androidX}"
    const val support_constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.support_constraint_lib}"
    const val support_fix_preference = "com.takisoft.preferencex:preferencex:${Versions.support_fix_lib}"
    const val androidKTX = "androidx.core:core-ktx:${Versions.androidKTX}"

    //JSON
    const val kotshi = "se.ansman.kotshi:api:${Versions.kotshi}"
    const val kotshi_compiler = "se.ansman.kotshi:compiler:${Versions.kotshi}"
    const val okio = "com.squareup.okio:okio:${Versions.okio}"

    //Kotlin
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val kotlin_android_extensions = "org.jetbrains.kotlin:kotlin-android-extensions:${Versions.kotlin}"
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlin_coroutines}"
    const val kotlin_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlin_coroutines}"
    const val kotlin_anko_commons = "org.jetbrains.anko:anko-commons:${Versions.kotlin_anko}"
    const val kotlin_anko_sql = "org.jetbrains.anko:anko-sqlite:${Versions.kotlin_anko}"

    const val kvs_schema = "com.rejasupotaro:kvs-schema:${Versions.kvs_schema}"
    const val kvs_schema_compiler = "com.rejasupotaro:kvs-schema-compiler:${Versions.kvs_schema}"

    //Dagger DI
    const val dagger2 = "com.google.dagger:dagger:${Versions.dagger}"
    const val dagger2_compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val dagger_android = "com.google.dagger:dagger-android:${Versions.dagger}"
    const val dagger_android_support = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    const val dagger_android_processor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"

    //Test
    const val junit = "junit:junit:${Versions.junit}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val androidTestCore = "androidx.test:core:${Versions.runner}"
    const val androidRunner = "androidx.test.ext:junit:${Versions.runner}"
    const val assertk = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}"

    //Other
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val header_decor = "ca.barrenechea.header-decor:header-decor:${Versions.header_decor}"
}

object Dependencies {
    const val kotlin_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}