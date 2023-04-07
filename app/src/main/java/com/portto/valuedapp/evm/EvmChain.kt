package com.portto.valuedapp.evm

import com.portto.valuedapp.Config

enum class EvmChain(
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
        title = "Ethereum",
        symbol = "ETH",
        mainnetChainId = 1,
        testnetChainId = 4,
        mainnetContractAddress = "0xfde90c9Bc193F520d119302a2dB8520D3A4408c8",
        testnetContractAddress = "0x58F385777aa6699b81f741Dd0d5B272A34C1c774",
        mainnetRpcUrl = "https://mainnet.infura.io/v3/${Config.INFURA_ID}",
        testnetRpcUrl = "https://rinkeby.blocto.app",
        mainnetExplorerDomain = "etherscan.io",
        testnetExplorerDomain = "rinkeby.etherscan.io"
    ),
    BNB_CHAIN(
        title = "BNB Chain",
        symbol = "BNB",
        mainnetChainId = 56,
        testnetChainId = 97,
        mainnetContractAddress = "0xfde90c9Bc193F520d119302a2dB8520D3A4408c8",
        testnetContractAddress = "0xfde90c9Bc193F520d119302a2dB8520D3A4408c8",
        mainnetRpcUrl = "https://bsc-dataseed.binance.org",
        testnetRpcUrl = "https://data-seed-prebsc-1-s1.binance.org:8545",
        mainnetExplorerDomain = "bscscan.com",
        testnetExplorerDomain = "testnet.bscscan.com"
    ),
    POLYGON(
        title = "Polygon",
        symbol = "MATIC",
        mainnetChainId = 137,
        testnetChainId = 80001,
        mainnetContractAddress = "0x009c403BdFaE357d82AAef2262a163287c30B739",
        testnetContractAddress = "0xfde90c9Bc193F520d119302a2dB8520D3A4408c8",
        mainnetRpcUrl = "https://polygon-mainnet.infura.io/v3/${Config.INFURA_ID}",
        testnetRpcUrl = "https://polygon-mumbai.infura.io/v3/${Config.INFURA_ID}",
        mainnetExplorerDomain = "polygonscan.com",
        testnetExplorerDomain = "mumbai.polygonscan.com"
    ),
    AVALANCHE(
        title = "Avalanche",
        symbol = "AVAX",
        mainnetChainId = 43114,
        testnetChainId = 43113,
        mainnetContractAddress = "0xfde90c9Bc193F520d119302a2dB8520D3A4408c8",
        testnetContractAddress = "0xfde90c9Bc193F520d119302a2dB8520D3A4408c8",
        mainnetRpcUrl = "https://api.avax.network/ext/bc/C/rpc",
        testnetRpcUrl = "https://api.avax-test.network/ext/bc/C/rpc",
        mainnetExplorerDomain = "snowtrace.io",
        testnetExplorerDomain = "testnet.snowtrace.io"
    )
}
