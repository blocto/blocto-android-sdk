package com.portto.sdk.core

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }
    return this
        .chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString(): String {
    return this
        .asUByteArray()
        .joinToString("") {
            it.toString(16).padStart(2, '0')
        }
}
