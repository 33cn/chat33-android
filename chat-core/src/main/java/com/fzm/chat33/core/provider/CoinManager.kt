package com.fzm.chat33.core.provider

import com.fzm.chat33.core.bean.RedPacketCoin
import com.fzm.chat33.core.net.RequestManager
import kotlinx.coroutines.*
import java.lang.Exception
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author zhengjy
 * @since 2019/03/18
 * Description:
 */
object CoinManager : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = EmptyCoroutineContext

    private var refreshing = false

    fun init() {
        initMap { }
    }

    private var coins: MutableList<RedPacketCoin> = mutableListOf()
    private var coinsMap: MutableMap<String, RedPacketCoin> = HashMap()

    private fun initMap(callback: (MutableMap<String, RedPacketCoin>) -> Unit) {
        if (coinsMap.isEmpty()) {
            launch(Dispatchers.Main) {
                while (refreshing) {
                    delay(100)
                }
                if (coinsMap.isNotEmpty()) {
                    callback(coinsMap)
                    return@launch
                }
                refreshing = true
                refreshCoinMap(callback)
            }
        } else {
            callback(coinsMap)
        }
    }

    private fun initList(callback: (MutableList<RedPacketCoin>) -> Unit) {
        if (coins.isEmpty()) {
            launch(Dispatchers.Main) {
                while (refreshing) {
                    delay(100)
                }
                if (coins.isNotEmpty()) {
                    callback(coins)
                    return@launch
                }
                refreshing = true
                refreshCoinList(callback)
            }
        } else {
            callback(coins)
        }
    }

    fun getCoinByName(coinName: String, callback: (RedPacketCoin?) -> Unit) {
        initMap {
            callback(it[coinName])
        }
    }

    fun getCoinList(callback: (MutableList<RedPacketCoin>) -> Unit) {
        initList {
            callback(it)
        }
    }

    fun refreshCoinList(callback: (MutableList<RedPacketCoin>) -> Unit) = launch(Dispatchers.Main) {
        val response = withContext(Dispatchers.IO) {
            try {
                RequestManager.INS.packetBalanceSync().execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        if (response?.isSuccessful == true) {
            if (response.body()?.code == 0) {
                val data = response.body()?.data
                if (data?.balances != null) {
                    coins.clear()
                    coins.addAll(data.balances)
                    for (coin in coins) {
                        coinsMap[coin.coinName] = coin
                    }
                }
            }
        }
        callback(coins)
        refreshing = false
    }

    private fun refreshCoinMap(callback: (MutableMap<String, RedPacketCoin>) -> Unit) = launch(Dispatchers.Main) {
        val response = withContext(Dispatchers.IO) {
            try {
                RequestManager.INS.packetBalanceSync().execute()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        if (response?.isSuccessful == true) {
            if (response.body()?.code == 0) {
                val data = response.body()?.data
                if (data?.balances != null) {
                    coins.clear()
                    coins.addAll(data.balances)
                    for (coin in coins) {
                        coinsMap[coin.coinName] = coin
                    }
                }
            }
        }
        callback(coinsMap)
        refreshing = false
    }
}
