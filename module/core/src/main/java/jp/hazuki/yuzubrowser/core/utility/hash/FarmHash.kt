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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.core.utility.hash

/**
 * Murmur3 is successor to Murmur2 fast non-crytographic hash algorithms.
 *
 * Murmur3 32 and 128 bit variants.
 * 32-bit Java port of https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#94
 * 128-bit Java port of https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#255
 *
 * This is a public domain code with no copyrights.
 * From homepage of MurmurHash (https://code.google.com/p/smhasher/),
 * "All MurmurHash versions are public domain software, and the author disclaims all copyright
 * to their code."
 */

// Some primes between 2^63 and 2^64 for various uses.
private const val K0 = -0x3c5a37a36834ced9L
private const val K1 = -0x4b6d499041670d8dL
private const val K2 = -0x651e95c4d06fbfb1L

fun ByteArray.farmHash(): Long {
    val length = size
    return if (length <= 32) {
        if (length <= 16) {
            farmHashUnder16(length)
        } else {
            farmHash17to32(length)
        }
    } else if (length <= 64) {
        farmHash33To64(length)
    } else {
        farmHashOver65(length)
    }
}

private fun ByteArray.farmHashUnder16(length: Int): Long {
    if (length >= 8) {
        val mul = K2 + length * 2
        val a = load64(0) + K2
        val b = load64(length - 8)
        val c = b.rotateRight(37) * mul + a
        val d = (a.rotateRight(25) + b) * mul
        return hashLength16(c, d, mul)
    }
    if (length >= 4) {
        val mul = K2 + length * 2
        val a = load32(0) and 0xFFFFFFFFL
        return hashLength16(length + (a shl 3), load32(length - 4) and 0xFFFFFFFFL, mul)
    }
    if (length > 0) {
        val a = this[0]
        val b = this[(length shr 1)]
        val c = this[(length - 1)]
        val y = (a.toInt() and 0xFF) + (b.toInt() and 0xFF shl 8)
        val z = length + (c.toInt() and 0xFF shl 2)
        return (y * K2 xor z * K0).shiftMix() * K2
    }
    return K2
}

private fun ByteArray.farmHash17to32(length: Int): Long {
    val mul = K2 + length * 2
    val a = load64(0) * K1
    val b = load64(8)
    val c = load64(length - 8) * mul
    val d = load64(length - 16) * K2
    return hashLength16(
            (a + b).rotateRight(43) + c.rotateRight(30) + d, a + (b + K2).rotateRight(18) + c, mul)
}

private fun ByteArray.farmHash33To64(length: Int): Long {
    val mul = K2 + length * 2
    val a = load64(0) * K2
    val b = load64(8)
    val c = load64(length - 8) * mul
    val d = load64(length - 16) * K2
    val y = (a + b).rotateRight(43) + c.rotateRight(30) + d
    val z = hashLength16(y, a + (b + K2).rotateRight(18) + c, mul)
    val e = load64(16) * mul
    val f = load64(24)
    val g = (y + load64(length - 32)) * mul
    val h = (z + load64(length - 24)) * mul
    return hashLength16(
            (e + f).rotateRight(43) + g.rotateRight(30) + h, e + (f + a).rotateRight(18) + g, mul)
}

private fun ByteArray.farmHashOver65(length: Int): Long {
    val seed = 81
    var x = seed.toLong()
    @Suppress("INTEGER_OVERFLOW")
    var y = seed * K1 + 113L
    var z = (y * K2 + 113L).shiftMix() * K2
    val v = LongArray(2)
    val w = LongArray(2)
    x = x * K2 + load64(0)

    var offset = 0
    val end = ((length - 1) / 64) * 64
    val last64Offset = end + ((length - 1) and 63) - 63
    do {
        x = (x + y + v[0] + load64(offset + 8)).rotateRight(37) * K1
        y = (y + v[1] + load64(offset + 48)).rotateRight(42) * K1
        x = x xor w[1]
        y += v[0]
        z = (z + w[0]).rotateRight(33) * K1
        weakHashLength32WithSeeds(offset, v[1] * K1, x + w[0], v)
        weakHashLength32WithSeeds(offset + 32, z + w[1], y + load64(offset + 16), w)
        val tmp = x
        x = z
        z = tmp
        offset += 64
    } while (offset != end)
    val mul = K1 + z.and(0xff).shl(1)
    offset = last64Offset
    w[0] = w[0] + (length - 1).and(63)
    v[0] = v[0] + w[0]
    w[0] = w[0] + v[0]
    x = (x + y + v[0] + load64(offset + 8)).rotateRight(37) * mul
    y = (y + v[1] + load64(offset + 48)).rotateRight(42) * mul
    x = x.xor(w[1] * 9)
    y += v[0] * 9 + load64(offset + 48)
    z = (z + w[0]).rotateRight(33) * mul
    weakHashLength32WithSeeds(offset, v[1] * mul, x + w[0], v)
    weakHashLength32WithSeeds(offset + 32, z + w[1], y + load64(offset + 16), w)
    return hashLength16(
            hashLength16(v[0], w[0], mul) + y.shiftMix() * K0 + x,
            hashLength16(v[1], w[1], mul) + z,
            mul)
}

private fun Long.shiftMix(): Long {
    return this xor this.ushr(47)
}

private fun Long.rotateRight(distance: Int): Long {
    return this.ushr(distance) or (this shl -distance)
}

private fun ByteArray.load64(offset: Int): Long {
    return this[offset + 7].toLong().and(0xff).shl(56)
            .or(this[offset + 6].toLong().and(0xff).shl(48))
            .or(this[offset + 5].toLong().and(0xff).shl(40))
            .or(this[offset + 4].toLong().and(0xff).shl(32))
            .or(this[offset + 3].toLong().and(0xff).shl(24))
            .or(this[offset + 2].toLong().and(0xff).shl(16))
            .or(this[offset + 1].toLong().and(0xff).shl(8))
            .or(this[offset].toLong().and(0xff))
}

private fun ByteArray.load32(offset: Int): Long {
    return this[offset + 3].toLong().and(0xff).shl(24)
            .or(this[offset + 2].toLong().and(0xff).shl(16))
            .or(this[offset + 1].toLong().and(0xff).shl(8))
            .or(this[offset].toLong().and(0xff))
}

private fun ByteArray.weakHashLength32WithSeeds(offset: Int, seedA: Long, seedB: Long, output: LongArray) {
    var seedAd = seedA
    var seedBd = seedB
    val part1 = load64(offset)
    val part2 = load64(offset + 8)
    val part3 = load64(offset + 16)
    val part4 = load64(offset + 24)

    seedAd += part1
    seedBd = (seedBd + seedAd + part4).rotateRight(21)
    val c = seedAd
    seedAd += part2
    seedAd += part3
    seedBd += seedAd.rotateRight(44)
    output[0] = seedAd + part4
    output[1] = seedBd + c
}

private fun hashLength16(u: Long, v: Long, mul: Long): Long {
    var a = (u xor v) * mul
    a = a xor a.ushr(47)
    var b = (v xor a) * mul
    b = b xor b.ushr(47)
    b *= mul
    return b
}