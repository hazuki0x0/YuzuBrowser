/*
 * Copyright (C) 2017-2021 Hazuki
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
    const val compile_sdk_version = 30
    const val build_tools_version = "30.0.3"
    const val min_sdk_version = 23
    const val target_sdk_version = 30
}

object AppVersions {
    const val version_name = "6.0.2"
    const val version_code = 410021
}

object Versions {
    const val androidX = "1.0.0"
    const val appCompat = "1.2.0"
    const val activityX = "1.2.0"
    const val androidKTX = "1.3.2"
    const val fragmentKtx = "1.3.0"
    const val recyclerView = "1.1.0"
    const val androidxRoom = "2.2.6"
    const val lifeCycle = "2.3.0"
    const val material = "1.3.0"
    const val support_fix_lib = "1.1.1"
    const val support_constraint_lib = "2.0.4"
    const val documentFile = "1.0.1"
    const val print = "1.0.0"
    const val swipeRefreshLayout = "1.1.0"
    const val dagger = "2.33"
    const val moshi = "1.11.0"
    const val okhttp = "4.9.1"
    const val okio = "2.10.0"
    const val kvs_schema = "5.1.0"
    const val kotlin = "1.4.31"
    const val kotlin_coroutines = "1.4.2"
    const val junit = "4.13.2"
    const val assertk = "0.23.1"
    const val espresso = "3.3.0"
    const val testCore = "1.3.0"
    const val runner = "1.1.2"
    const val header_decor = "0.2.8"
    const val materialprogressbar = "1.6.1"
    const val mockito = "3.8.0"
    const val powermock = "2.0.9"
    const val jsoup = "1.13.1"
    const val re2j = "1.5"
    const val webkit = "1.4.0"
}

object AndroidX {
    const val annotations = "androidx.annotation:annotation:${Versions.androidX}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val design = "com.google.android.material:material:${Versions.material}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.support_constraint_lib}"
    const val fix_preference = "androidx.preference:preference-ktx:${Versions.support_fix_lib}"
    const val KTX = "androidx.core:core-ktx:${Versions.androidKTX}"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}"
    const val activty = "androidx.activity:activity:${Versions.activityX}"
    const val room = "androidx.room:room-runtime:${Versions.androidxRoom}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.androidxRoom}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.androidxRoom}"
    const val documentFile = "androidx.documentfile:documentfile:${Versions.documentFile}"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefreshLayout}"
    const val lifecycle = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifeCycle}"
    const val print = "androidx.print:print:${Versions.print}"
    const val webkit = "androidx.webkit:webkit:${Versions.webkit}"
}

object Libs {
    //androidx libraries
    const val support_annotations = AndroidX.annotations
    const val support_appcompat_v7 = AndroidX.appcompat
    const val support_design = AndroidX.design
    const val support_recyclerview = AndroidX.recyclerView
    const val support_constraint_layout = AndroidX.constraintLayout
    const val support_fix_preference = AndroidX.fix_preference
    const val androidKTX = AndroidX.KTX

    //JSON
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val okio = "com.squareup.okio:okio:${Versions.okio}"

    //Kotlin
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val kotlin_android_extensions = "org.jetbrains.kotlin:kotlin-android-extensions:${Versions.kotlin}"
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlin_coroutines}"
    const val kotlin_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlin_coroutines}"

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
    const val androidTestCore = "androidx.test:core:${Versions.testCore}"
    const val androidRunner = "androidx.test.ext:junit:${Versions.runner}"
    const val assertk = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}"
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    const val powerMockJunit = "org.powermock:powermock-module-junit4:${Versions.powermock}"
    const val powerMockMockito = "org.powermock:powermock-api-mockito2:${Versions.powermock}"

    //Other
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val header_decor = "ca.barrenechea.header-decor:header-decor:${Versions.header_decor}"
    const val materialProgressBar = "me.zhanghai.android.materialprogressbar:library:${Versions.materialprogressbar}"
    const val jsoup = "org.jsoup:jsoup:${Versions.jsoup}"
    const val re2j = "com.google.re2j:re2j:${Versions.re2j}"
}

object Dependencies {
    const val kotlin_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}
