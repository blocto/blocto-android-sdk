package com.portto.valuedapp.solana

import com.portto.sdk.core.BloctoSDK
import com.portto.solana.web3.AccountMeta
import com.portto.solana.web3.PublicKey
import com.portto.solana.web3.TransactionInstruction
import com.portto.solana.web3.programs.Program
import org.near.borshj.BorshBuffer

object ValueProgram : Program() {

    private val PROGRAM_ID_DEVNET = PublicKey("G4YkbRN4nFQGEUg4SXzPsrManWzuk8bNq9JaMhXepnZ6")
    private val ACCOUNT_PUBLIC_KEY_DEVNET = PublicKey("4AXy5YYCXpMapaVuzKkz25kVHzrdLDgKN3TiQvtf1Eu8")

    private val PROGRAM_ID_MAINNET_BETA = PublicKey("EN2Ln23fzm4qag1mHfx7FDJwDJog5u4SDgqRY256ZgFt")
    private val ACCOUNT_PUBLIC_KEY_MAINNET_BETA = PublicKey("EajAHVxAVvf4yNUu37ZEh8QS7Lk5bw9yahTGiTSL1Rwt")

    private const val INSTRUCTION_SET_VALUE = 0

    val programId get() = if (BloctoSDK.debug) {
        PROGRAM_ID_DEVNET
    } else {
        PROGRAM_ID_MAINNET_BETA
    }

    val accountPublicKey get() = if (BloctoSDK.debug) {
        ACCOUNT_PUBLIC_KEY_DEVNET
    } else {
        ACCOUNT_PUBLIC_KEY_MAINNET_BETA
    }

    fun createSetValueInstruction(
        value: Int,
        walletAddress: PublicKey
    ): TransactionInstruction {
        val buffer = BorshBuffer.allocate(Byte.SIZE_BYTES + Int.SIZE_BYTES)
        buffer.writeU8(INSTRUCTION_SET_VALUE)
        buffer.writeU32(value)
        return createTransactionInstruction(
            programId = programId,
            keys = listOf(
                AccountMeta(publicKey = accountPublicKey, isSigner = false, isWritable = true),
                AccountMeta(publicKey = walletAddress, isSigner = true, isWritable = false)
            ),
            data = buffer.toByteArray()
        )
    }
}
