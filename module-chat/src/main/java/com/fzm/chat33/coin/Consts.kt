package com.fzm.chat33.coin

/**
 * @author zhengjy
 * @since 2019/11/26
 * Description:
 */
val DEFAULT_AMOUNT = listOf(
        PreAmount(amount = "2", selected = true),
        PreAmount(amount = "5"),
        PreAmount(amount = "8"),
        PreAmount(amount = "10"),
        PreAmount(amount = "20"),
        PreAmount(amount = "50")
)

val preCoinAmount: List<CoinAmount> = listOf(
        CoinAmount(
                coinName = "BTC",
                preAmount = listOf(
                        PreAmount(amount = "0.0002", selected = true),
                        PreAmount(amount = "0.0005"),
                        PreAmount(amount = "0.001"),
                        PreAmount(amount = "0.002"),
                        PreAmount(amount = "0.005")
                )
        ),
        CoinAmount(
                coinName = "BTY",
                preAmount = DEFAULT_AMOUNT
        ),
        CoinAmount(
                coinName = "ETH",
                preAmount = listOf(
                        PreAmount(amount = "0.002", selected = true),
                        PreAmount(amount = "0.005"),
                        PreAmount(amount = "0.01"),
                        PreAmount(amount = "0.02"),
                        PreAmount(amount = "0.05")
                )
        ),
        CoinAmount(
                coinName = "YCC",
                preAmount = DEFAULT_AMOUNT
        ),
        CoinAmount(
                coinName = "DCR",
                preAmount = listOf(
                        PreAmount(amount = "0.02", selected = true),
                        PreAmount(amount = "0.05"),
                        PreAmount(amount = "0.08"),
                        PreAmount(amount = "0.1"),
                        PreAmount(amount = "0.2"),
                        PreAmount(amount = "0.5")
                )
        ),
        CoinAmount(
                coinName = "CCNY",
                preAmount = DEFAULT_AMOUNT
        ),
        CoinAmount(
                coinName = "BOSS",
                preAmount = DEFAULT_AMOUNT
        ),
        CoinAmount(
                coinName = "SFT",
                preAmount = DEFAULT_AMOUNT
        )
)