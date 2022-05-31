# BloctoSDK

![Maven Central](https://img.shields.io/maven-central/v/com.portto.sdk/core)
![CircleCI](https://img.shields.io/circleci/build/gh/portto/blocto-android-sdk/main)
![GitHub](https://img.shields.io/github/license/portto/blocto-android-sdk)

Integrate Blocto service into your dApp on Android.

Currently support

* Solana
* Ethereum
* BNB Chain
* Polygon
* Avalanche
* More blockchains are coming soon

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
