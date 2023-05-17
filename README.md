# BloctoSDK

![Maven Central](https://img.shields.io/maven-central/v/com.portto.sdk/core)
![Github Action](https://github.com/portto/blocto-android-sdk/actions/workflows/ci.yml/badge.svg)
![GitHub](https://img.shields.io/github/license/portto/blocto-android-sdk)

Integrate Blocto service into your dApp on Android.

Currently support

* Ethereum
* Arbitrum
* Optimism
* BNB Chain
* Polygon
* Avalanche
* Solana
* More blockchains are coming soon

> For Flow, it's recommended to use [fcl](https://github.com/portto/fcl-android). Check the [documents](https://docs.blocto.app/blocto-sdk/android-sdk/flow) for more info.

## Installation

Add the dependency below to your module's `build.gradle` file

```
dependencies {
    implementation "com.portto.sdk:solana:$bloctoSdkVersion"
    implementation "com.portto.sdk:evm:$bloctoSdkVersion"
}
```

## Usage

Please refer to [**documentation**](https://docs.blocto.app/blocto-sdk/android-sdk)

## Demo App

To run EVM demo, **Infura** id is needed to connect to RPC endpoint.

Put your id into `Config.kt`

```
object Config {
    const val INFURA_ID = "YOUR_INFURA_ID"
}
```

## Author

[Jack](mailto:jack.lai@portto.com), [Kihon](mailto:kihon@portto.com)


## License

BloctoSDK is available under the MIT license. See the LICENSE file for more info.
