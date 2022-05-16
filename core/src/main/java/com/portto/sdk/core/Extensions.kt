package com.portto.sdk.core

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }
    return this
        .chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun String.isValidHex(need0xPrefix: Boolean): Boolean {
    val regex = Regex("(0[xX])?[0-9a-fA-F]*")
    return regex.matches(this) &&
            if (need0xPrefix) this.startsWith("0x") else true
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toHexString(): String {
    return this
        .asUByteArray()
        .joinToString("") {
            it.toString(16).padStart(2, '0')
        }
}
