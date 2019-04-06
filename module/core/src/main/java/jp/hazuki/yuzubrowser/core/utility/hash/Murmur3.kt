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

package jp.hazuki.yuzubrowser.core.utility.hash

private const val LONG_BYTES = 8
private const val INT_BYTES = 4
private const val SHORT_BYTES = 2

// from 64-bit linear congruential generator
const val NULL_HASHCODE = 2862933555777941757L

// Constants for 32 bit variant
private const val C1_32 = -0x3361d2af
private const val C2_32 = 0x1b873593
private const val R1_32 = 15
private const val R2_32 = 13
private const val M_32 = 5
private const val N_32 = -0x19ab949c

// Constants for 128 bit variant
private const val C1 = -0x783c846eeebdac2bL
private const val C2 = 0x4cf5ad432745937fL
private const val R1 = 31
private const val R2 = 27
private const val R3 = 33
private const val M = 5
private const val N1 = 0x52dce729
private const val N2 = 0x38495ab5

const val DEFAULT_SEED = 104729

/**
 * Murmur3 32-bit variant.
 */
fun Long.murmur3Hash32(seed: Int): Int {
    var hash = seed
    val r0 = java.lang.Long.reverseBytes(this)

    hash = mix32(r0.toInt(), hash)
    hash = mix32(r0.ushr(32).toInt(), hash)

    return fmix32(LONG_BYTES, hash)
}

/**
 * Murmur3 32-bit variant.
 */
fun murmur3Hash32(l0: Long, l1: Long, seed: Int): Int {
    var hash = seed
    val r0 = java.lang.Long.reverseBytes(l0)
    val r1 = java.lang.Long.reverseBytes(l1)

    hash = mix32(r0.toInt(), hash)
    hash = mix32(r0.ushr(32).toInt(), hash)
    hash = mix32(r1.toInt(), hash)
    hash = mix32(r1.ushr(32).toInt(), hash)

    return fmix32(LONG_BYTES * 2, hash)
}

/**
 * Murmur3 32-bit variant.
 *
 * @param offset - offset of data (default 0)
 * @param length - length of array  (default array size)
 * @param seed   - seed. (default DEFAULT_SEED)
 * @return - hashcode
 */
fun ByteArray.murmur3Hash32(offset: Int = 0, length: Int = size, seed: Int = DEFAULT_SEED): Int {
    var hash = seed
    val nBlocks = length shr 2

    // body
    for (i in 0 until nBlocks) {
        val i4 = i shl 2
        val k = (this[offset + i4].toInt() and 0xff
                or (this[offset + i4 + 1].toInt() and 0xff shl 8)
                or (this[offset + i4 + 2].toInt() and 0xff shl 16)
                or (this[offset + i4 + 3].toInt() and 0xff shl 24))

        hash = mix32(k, hash)
    }

    // tail
    val idx = nBlocks shl 2
    var k1 = 0
    when (length - idx) {
        3 -> {
            k1 = k1 xor (this[offset + idx + 2].toInt() shl 16)
            k1 = k1 xor (this[offset + idx + 1].toInt() shl 8)
            k1 = k1 xor this[offset + idx].toInt()

            // mix functions
            k1 *= C1_32
            k1 = Integer.rotateLeft(k1, R1_32)
            k1 *= C2_32
            hash = hash xor k1
        }
        2 -> {
            k1 = k1 xor (this[offset + idx + 1].toInt() shl 8)
            k1 = k1 xor this[offset + idx].toInt()
            k1 *= C1_32
            k1 = Integer.rotateLeft(k1, R1_32)
            k1 *= C2_32
            hash = hash xor k1
        }
        1 -> {
            k1 = k1 xor this[offset + idx].toInt()
            k1 *= C1_32
            k1 = Integer.rotateLeft(k1, R1_32)
            k1 *= C2_32
            hash = hash xor k1
        }
    }

    return fmix32(length, hash)
}

private fun mix32(k: Int, hash: Int): Int {
    var k1 = k
    var mHash = hash
    k1 *= C1_32
    k1 = Integer.rotateLeft(k1, R1_32)
    k1 *= C2_32
    mHash = mHash xor k1
    return Integer.rotateLeft(mHash, R2_32) * M_32 + N_32
}

private fun fmix32(length: Int, hash: Int): Int {
    var mHash = hash
    mHash = mHash xor length
    mHash = mHash xor mHash.ushr(16)
    mHash *= -0x7a143595
    mHash = mHash xor mHash.ushr(13)
    mHash *= -0x3d4d51cb
    mHash = mHash xor mHash.ushr(16)

    return mHash
}

fun Long.murmur3Hash64(): Long {
    var hash = DEFAULT_SEED.toLong()
    var k = java.lang.Long.reverseBytes(this)
    // mix functions
    k *= C1
    k = java.lang.Long.rotateLeft(k, R1)
    k *= C2
    hash = hash xor k
    hash = java.lang.Long.rotateLeft(hash, R2) * M + N1
    // finalization
    hash = hash xor LONG_BYTES.toLong()
    hash = fmix64(hash)
    return hash
}

fun Int.murmur3Hash64(): Long {
    var k1 = Integer.reverseBytes(this).toLong() and (-1L).ushr(32)
    var hash = DEFAULT_SEED.toLong()
    k1 *= C1
    k1 = java.lang.Long.rotateLeft(k1, R1)
    k1 *= C2
    hash = hash xor k1
    // finalization
    hash = hash xor INT_BYTES.toLong()
    hash = fmix64(hash)
    return hash
}

fun Short.murmur3Hash64(): Long {
    var hash = DEFAULT_SEED.toLong()
    var k1: Long = 0
    k1 = k1 xor (this.toLong() and 0xff shl 8)
    k1 = k1 xor ((this.toLong() and 0xFF00 shr 8) and 0xff)
    k1 *= C1
    k1 = java.lang.Long.rotateLeft(k1, R1)
    k1 *= C2
    hash = hash xor k1

    // finalization
    hash = hash xor SHORT_BYTES.toLong()
    hash = fmix64(hash)
    return hash
}

/**
 * Murmur3 64-bit variant. This is essentially MSB 8 bytes of Murmur3 128-bit variant.
 *
 * @param length - length of array
 * @param seed   - seed. (default is 0)
 * @return - hashcode
 */
fun ByteArray.murmur3Hash64(offset: Int = 0, length: Int = size, seed: Int = DEFAULT_SEED): Long {
    var hash = seed.toLong()
    val nBlocks = length shr 3

    // body
    for (i in 0 until nBlocks) {
        val i8 = i shl 3
        var k = (this[offset + i8].toLong() and 0xff
                or (this[offset + i8 + 1].toLong() and 0xff shl 8)
                or (this[offset + i8 + 2].toLong() and 0xff shl 16)
                or (this[offset + i8 + 3].toLong() and 0xff shl 24)
                or (this[offset + i8 + 4].toLong() and 0xff shl 32)
                or (this[offset + i8 + 5].toLong() and 0xff shl 40)
                or (this[offset + i8 + 6].toLong() and 0xff shl 48)
                or (this[offset + i8 + 7].toLong() and 0xff shl 56))

        // mix functions
        k *= C1
        k = java.lang.Long.rotateLeft(k, R1)
        k *= C2
        hash = hash xor k
        hash = java.lang.Long.rotateLeft(hash, R2) * M + N1
    }

    // tail
    var k1: Long = 0
    val tailStart = nBlocks shl 3
    when (length - tailStart) {
        7 -> {
            k1 = k1 xor (this[offset + tailStart + 6].toLong() and 0xff shl 48)
            k1 = k1 xor (this[offset + tailStart + 5].toLong() and 0xff shl 40)
            k1 = k1 xor (this[offset + tailStart + 4].toLong() and 0xff shl 32)
            k1 = k1 xor (this[offset + tailStart + 3].toLong() and 0xff shl 24)
            k1 = k1 xor (this[offset + tailStart + 2].toLong() and 0xff shl 16)
            k1 = k1 xor (this[offset + tailStart + 1].toLong() and 0xff shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            hash = hash xor k1
        }
        6 -> {
            k1 = k1 xor (this[offset + tailStart + 5].toLong() and 0xff shl 40)
            k1 = k1 xor (this[offset + tailStart + 4].toLong() and 0xff shl 32)
            k1 = k1 xor (this[offset + tailStart + 3].toLong() and 0xff shl 24)
            k1 = k1 xor (this[offset + tailStart + 2].toLong() and 0xff shl 16)
            k1 = k1 xor (this[offset + tailStart + 1].toLong() and 0xff shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            hash = hash xor k1
        }
        5 -> {
            k1 = k1 xor (this[offset + tailStart + 4].toLong() and 0xff shl 32)
            k1 = k1 xor (this[offset + tailStart + 3].toLong() and 0xff shl 24)
            k1 = k1 xor (this[offset + tailStart + 2].toLong() and 0xff shl 16)
            k1 = k1 xor (this[offset + tailStart + 1].toLong() and 0xff shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            hash = hash xor k1
        }
        4 -> {
            k1 = k1 xor (this[offset + tailStart + 3].toLong() and 0xff shl 24)
            k1 = k1 xor (this[offset + tailStart + 2].toLong() and 0xff shl 16)
            k1 = k1 xor (this[offset + tailStart + 1].toLong() and 0xff shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            hash = hash xor k1
        }
        3 -> {
            k1 = k1 xor (this[offset + tailStart + 2].toLong() and 0xff shl 16)
            k1 = k1 xor (this[offset + tailStart + 1].toLong() and 0xff shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            hash = hash xor k1
        }
        2 -> {
            k1 = k1 xor (this[offset + tailStart + 1].toLong() and 0xff shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            hash = hash xor k1
        }
        1 -> {
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            hash = hash xor k1
        }
    }

    // finalization
    hash = hash xor length.toLong()
    hash = fmix64(hash)

    return hash
}

private fun fmix64(h: Long): Long {
    var h1 = h
    h1 = h1 xor h1.ushr(33)
    h1 *= -0xae502812aa7333L
    h1 = h1 xor h1.ushr(33)
    h1 *= -0x3b314601e57a13adL
    h1 = h1 xor h1.ushr(33)
    return h1
}

/**
 * Murmur3 128-bit variant.
 *
 * @param offset - the first element of array
 * @param length - length of array
 * @param seed   - seed. (default is 0)
 * @return - hashcode (2 longs)
 */
fun ByteArray.murmur3Hash128(offset: Int = 0, length: Int = size, seed: Int = DEFAULT_SEED): LongArray {
    var h1 = seed.toLong()
    var h2 = seed.toLong()
    val nblocks = length shr 4

    // body
    for (i in 0 until nblocks) {
        val i16 = i shl 4
        var k1 = (this[offset + i16].toLong() and 0xff
                or (this[offset + i16 + 1].toLong() and 0xff shl 8)
                or (this[offset + i16 + 2].toLong() and 0xff shl 16)
                or (this[offset + i16 + 3].toLong() and 0xff shl 24)
                or (this[offset + i16 + 4].toLong() and 0xff shl 32)
                or (this[offset + i16 + 5].toLong() and 0xff shl 40)
                or (this[offset + i16 + 6].toLong() and 0xff shl 48)
                or (this[offset + i16 + 7].toLong() and 0xff shl 56))

        var k2 = (this[offset + i16 + 8].toLong() and 0xff
                or (this[offset + i16 + 9].toLong() and 0xff shl 8)
                or (this[offset + i16 + 10].toLong() and 0xff shl 16)
                or (this[offset + i16 + 11].toLong() and 0xff shl 24)
                or (this[offset + i16 + 12].toLong() and 0xff shl 32)
                or (this[offset + i16 + 13].toLong() and 0xff shl 40)
                or (this[offset + i16 + 14].toLong() and 0xff shl 48)
                or (this[offset + i16 + 15].toLong() and 0xff shl 56))

        // mix functions for k1
        k1 *= C1
        k1 = java.lang.Long.rotateLeft(k1, R1)
        k1 *= C2
        h1 = h1 xor k1
        h1 = java.lang.Long.rotateLeft(h1, R2)
        h1 += h2
        h1 = h1 * M + N1

        // mix functions for k2
        k2 *= C2
        k2 = java.lang.Long.rotateLeft(k2, R3)
        k2 *= C1
        h2 = h2 xor k2
        h2 = java.lang.Long.rotateLeft(h2, R1)
        h2 += h1
        h2 = h2 * M + N2
    }

    // tail
    var k1: Long = 0
    var k2: Long = 0
    val tailStart = nblocks shl 4
    when (length - tailStart) {
        15 -> {
            k2 = k2 xor ((this[offset + tailStart + 14].toLong() and 0xff) shl 48)
            k2 = k2 xor ((this[offset + tailStart + 13].toLong() and 0xff) shl 40)
            k2 = k2 xor ((this[offset + tailStart + 12].toLong() and 0xff) shl 32)
            k2 = k2 xor ((this[offset + tailStart + 11].toLong() and 0xff) shl 24)
            k2 = k2 xor ((this[offset + tailStart + 10].toLong() and 0xff) shl 16)
            k2 = k2 xor ((this[offset + tailStart + 9].toLong() and 0xff) shl 8)
            k2 = k2 xor (this[offset + tailStart + 8].toLong() and 0xff)
            k2 *= C2
            k2 = java.lang.Long.rotateLeft(k2, R3)
            k2 *= C1
            h2 = h2 xor k2
            k1 = k1 xor ((this[offset + tailStart + 7].toLong() and 0xff) shl 56)
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        14 -> {
            k2 = k2 xor ((this[offset + tailStart + 13].toLong() and 0xff) shl 40)
            k2 = k2 xor ((this[offset + tailStart + 12].toLong() and 0xff) shl 32)
            k2 = k2 xor ((this[offset + tailStart + 11].toLong() and 0xff) shl 24)
            k2 = k2 xor ((this[offset + tailStart + 10].toLong() and 0xff) shl 16)
            k2 = k2 xor ((this[offset + tailStart + 9].toLong() and 0xff) shl 8)
            k2 = k2 xor (this[offset + tailStart + 8].toLong() and 0xff)
            k2 *= C2
            k2 = java.lang.Long.rotateLeft(k2, R3)
            k2 *= C1
            h2 = h2 xor k2
            k1 = k1 xor ((this[offset + tailStart + 7].toLong() and 0xff) shl 56)
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        13 -> {
            k2 = k2 xor ((this[offset + tailStart + 12].toLong() and 0xff) shl 32)
            k2 = k2 xor ((this[offset + tailStart + 11].toLong() and 0xff) shl 24)
            k2 = k2 xor ((this[offset + tailStart + 10].toLong() and 0xff) shl 16)
            k2 = k2 xor ((this[offset + tailStart + 9].toLong() and 0xff) shl 8)
            k2 = k2 xor (this[offset + tailStart + 8].toLong() and 0xff)
            k2 *= C2
            k2 = java.lang.Long.rotateLeft(k2, R3)
            k2 *= C1
            h2 = h2 xor k2
            k1 = k1 xor ((this[offset + tailStart + 7].toLong() and 0xff) shl 56)
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        12 -> {
            k2 = k2 xor ((this[offset + tailStart + 11].toLong() and 0xff) shl 24)
            k2 = k2 xor ((this[offset + tailStart + 10].toLong() and 0xff) shl 16)
            k2 = k2 xor ((this[offset + tailStart + 9].toLong() and 0xff) shl 8)
            k2 = k2 xor (this[offset + tailStart + 8].toLong() and 0xff)
            k2 *= C2
            k2 = java.lang.Long.rotateLeft(k2, R3)
            k2 *= C1
            h2 = h2 xor k2
            k1 = k1 xor ((this[offset + tailStart + 7].toLong() and 0xff) shl 56)
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        11 -> {
            k2 = k2 xor ((this[offset + tailStart + 10].toLong() and 0xff) shl 16)
            k2 = k2 xor ((this[offset + tailStart + 9].toLong() and 0xff) shl 8)
            k2 = k2 xor (this[offset + tailStart + 8].toLong() and 0xff)
            k2 *= C2
            k2 = java.lang.Long.rotateLeft(k2, R3)
            k2 *= C1
            h2 = h2 xor k2
            k1 = k1 xor ((this[offset + tailStart + 7].toLong() and 0xff) shl 56)
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        10 -> {
            k2 = k2 xor ((this[offset + tailStart + 9].toLong() and 0xff) shl 8)
            k2 = k2 xor (this[offset + tailStart + 8].toLong() and 0xff)
            k2 *= C2
            k2 = java.lang.Long.rotateLeft(k2, R3)
            k2 *= C1
            h2 = h2 xor k2
            k1 = k1 xor ((this[offset + tailStart + 7].toLong() and 0xff) shl 56)
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        9 -> {
            k2 = k2 xor (this[offset + tailStart + 8].toLong() and 0xff)
            k2 *= C2
            k2 = java.lang.Long.rotateLeft(k2, R3)
            k2 *= C1
            h2 = h2 xor k2
            k1 = k1 xor ((this[offset + tailStart + 7].toLong() and 0xff) shl 56)
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }

        8 -> {
            k1 = k1 xor ((this[offset + tailStart + 7].toLong() and 0xff) shl 56)
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        7 -> {
            k1 = k1 xor ((this[offset + tailStart + 6].toLong() and 0xff) shl 48)
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        6 -> {
            k1 = k1 xor ((this[offset + tailStart + 5].toLong() and 0xff) shl 40)
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        5 -> {
            k1 = k1 xor ((this[offset + tailStart + 4].toLong() and 0xff) shl 32)
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        4 -> {
            k1 = k1 xor ((this[offset + tailStart + 3].toLong() and 0xff) shl 24)
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        3 -> {
            k1 = k1 xor ((this[offset + tailStart + 2].toLong() and 0xff) shl 16)
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        2 -> {
            k1 = k1 xor ((this[offset + tailStart + 1].toLong() and 0xff) shl 8)
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
        1 -> {
            k1 = k1 xor (this[offset + tailStart].toLong() and 0xff)
            k1 *= C1
            k1 = java.lang.Long.rotateLeft(k1, R1)
            k1 *= C2
            h1 = h1 xor k1
        }
    }

    // finalization
    h1 = h1 xor length.toLong()
    h2 = h2 xor length.toLong()

    h1 += h2
    h2 += h1

    h1 = fmix64(h1)
    h2 = fmix64(h2)

    h1 += h2
    h2 += h1

    return longArrayOf(h1, h2)
}