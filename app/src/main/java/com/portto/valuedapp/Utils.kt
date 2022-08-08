package com.portto.valuedapp

object Utils {

    /**
     * Transform decimal integer to hex string
     */
    fun Int.toHexString(withPrefix: Boolean): String = toString(16).apply {
        if (withPrefix) this.withPrefix()
    }

    /**
     * Remove prefix from the address
     */
    fun String.sansPrefix(): String {
        if (isEmpty()) throw Exception("No address provided")
        return replace("0x", "")
    }

    /**
     * Add hex prefix (remove prefix if it exists beforehand)
     */
    fun String.withPrefix(): String {
        if (isEmpty()) throw Exception("No address provided")
        return "0x${sansPrefix()}"
    }

    /**
     * Make the provided address shorter for display
     */
    fun String.shortenAddress(): String = "${substring(0, 6)}...${substring(length - 6, length)}"
}