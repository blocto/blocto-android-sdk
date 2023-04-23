package com.portto.valuedapp.evm

import com.portto.sdk.core.Blockchain
import com.portto.valuedapp.Config

enum class EvmChain(
    val blockchain: Blockchain,
    val title: String,
    val symbol: String,
    val mainnetChainId: Int,
    val testnetChainId: Int,
    val mainnetContractAddress: String,
    val testnetContractAddress: String,
    val mainnetRpcUrl: String,
    val testnetRpcUrl: String,
    val mainnetExplorerDomain: String,
    val testnetExplorerDomain: String
) {

    ETHEREUM(
        blockchain = Blockchain.ETHEREUM,
        title = "Ethereum",
        symbol = "ETH",
        mainnetChainId = 1,
        testnetChainId = 5,
        mainnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        testnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        mainnetRpcUrl = "https://mainnet.infura.io/v3/${Config.INFURA_ID}",
        testnetRpcUrl = "https://goerli.infura.io/v3/${Config.INFURA_ID}",
        mainnetExplorerDomain = "etherscan.io",
        testnetExplorerDomain = "goerli.etherscan.io"
    ),
    BNB_CHAIN(
        blockchain = Blockchain.BNB_CHAIN,
        title = "BNB Chain",
        symbol = "BNB",
        mainnetChainId = 56,
        testnetChainId = 97,
        mainnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        testnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        mainnetRpcUrl = "https://bsc-dataseed.binance.org",
        testnetRpcUrl = "https://data-seed-prebsc-1-s1.binance.org:8545",
        mainnetExplorerDomain = "bscscan.com",
        testnetExplorerDomain = "testnet.bscscan.com"
    ),
    POLYGON(
        blockchain = Blockchain.POLYGON,
        title = "Polygon",
        symbol = "MATIC",
        mainnetChainId = 137,
        testnetChainId = 80001,
        mainnetContractAddress = "0xD76bAA840e3D5AE1C5E5C7cEeF1C1A238687860e",
        testnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        mainnetRpcUrl = "https://polygon-mainnet.infura.io/v3/${Config.INFURA_ID}",
        testnetRpcUrl = "https://polygon-mumbai.infura.io/v3/${Config.INFURA_ID}",
        mainnetExplorerDomain = "polygonscan.com",
        testnetExplorerDomain = "mumbai.polygonscan.com"
    ),
    AVALANCHE(
        blockchain = Blockchain.AVALANCHE,
        title = "Avalanche",
        symbol = "AVAX",
        mainnetChainId = 43114,
        testnetChainId = 43113,
        mainnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        testnetContractAddress = "0xD76bAA840e3D5AE1C5E5C7cEeF1C1A238687860e",
        mainnetRpcUrl = "https://api.avax.network/ext/bc/C/rpc",
        testnetRpcUrl = "https://api.avax-test.network/ext/bc/C/rpc",
        mainnetExplorerDomain = "snowtrace.io",
        testnetExplorerDomain = "testnet.snowtrace.io"
    ),
    ARBITRUM(
        blockchain = Blockchain.ARBITRUM,
        title = "Arbitrum",
        symbol = "ETH",
        mainnetChainId = 42161,
        testnetChainId = 421613,
        mainnetContractAddress = "0x806243c7368a90D957592B55875eF4C3353C5bEa",
        testnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        mainnetRpcUrl = "https://arb1.arbitrum.io/rpc",
        testnetRpcUrl = "https://arbitrum-goerli.public.blastapi.io",
        mainnetExplorerDomain = "arbiscan.io",
        testnetExplorerDomain = "goerli.arbiscan.io"
    ),
    OPTIMISM(
        blockchain = Blockchain.OPTIMISM,
        title = "Optimism",
        symbol = "ETH",
        mainnetChainId = 10,
        testnetChainId = 420,
        mainnetContractAddress = "0x806243c7368a90D957592B55875eF4C3353C5bEa",
        testnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        mainnetRpcUrl = "https://mainnet.optimism.io",
        testnetRpcUrl = "https://endpoints.omniatech.io/v1/op/goerli/public",
        mainnetExplorerDomain = "optimistic.etherscan.io",
        testnetExplorerDomain = "goerli-optimism.etherscan.io"
    ),
}
