package com.fzm.chat33.main.activity

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.fuzamei.common.recycleviewbase.CommonAdapter
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter
import com.fuzamei.common.recycleviewbase.ViewHolder
import com.fuzamei.common.utils.FinanceUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.fzm.chat33.R
import com.fzm.chat33.coin.CoinAmount
import com.fzm.chat33.coin.DEFAULT_AMOUNT
import com.fzm.chat33.coin.PreAmount
import com.fzm.chat33.coin.preCoinAmount
import com.fzm.chat33.core.bean.RedPacketCoin
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.provider.CoinManager
import com.fzm.chat33.core.request.SendRewardPacketRequest
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.core.utils.UserInfoPreference.LAST_PACKET_COIN
import com.fzm.chat33.redpacket.mvvm.SendPacketViewModel
import com.fzm.chat33.utils.PraiseUtil
import com.fzm.chat33.utils.SimpleTextWatcher
import com.fzm.chat33.widget.ChatCodeView
import com.fzm.chat33.widget.PayPasswordDialog
import kotlinx.android.synthetic.main.activity_send_reward_packet.*
import kotlinx.android.synthetic.main.send_coin_drawer.*
import javax.inject.Inject
import kotlin.math.max

/**
 * @author zhengjy
 * @since 2019/11/19
 * Description:奖励打赏页面
 */
@Route(path = AppRoute.REWARD_PACKET)
class SendRewardPacketActivity : DILoadableActivity() {

    @JvmField
    @Autowired
    var targetId: String? = ""

    @JvmField
    @Autowired
    var channelType: Int = Chat33Const.CHANNEL_ROOM

    @JvmField
    @Autowired
    var logId: String = ""

    lateinit var chooseCoinAdapter: CommonAdapter<RedPacketCoin>
    var coinList: MutableList<RedPacketCoin> = mutableListOf()

    lateinit var amountAdapter: CommonAdapter<PreAmount>
    var amountList: MutableList<PreAmount> = mutableListOf()

    var payDialog: PayPasswordDialog? = null

    private var editAmount = false
    private var firstLoad = true
    private var currentCoin: RedPacketCoin? = null
    private var currentCoinAmount: CoinAmount? = null

    /**
     * 填写的红包金额，用于计算和请求参数
     */
    private var packetAmount: Double = 0.0
    /**
     * 实际需要支付的金额，用于显示
     */
    private var coinAmount: Double = 0.0
        get() {
            return packetAmount + (currentCoin?.fee ?: 0.0)
        }

    /**
     * 币种可用资产，减去手续费
     * 这个参数用于本地使用
     */
    private var availableAmount: Double = 0.0
        get() {
            return max((currentCoin?.amount ?: 0.0) - (currentCoin?.fee ?: 0.0), 0.0)
        }

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: SendPacketViewModel

    override fun getLayoutId(): Int {
        return R.layout.activity_send_reward_packet
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
            drawer_layout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        ctb_title.tv_title_middle.text = getString(R.string.chat_title_send_reward_packet)
        viewModel.loading.observe(this, Observer { setupLoading(it) })
        viewModel.isSetPayPassword.observe(this, Observer {
            if (it?.state == 1) {
                showPayPwdDialog()
            } else {
                EasyDialog.Builder()
                        .setHeaderTitle(getString(R.string.chat_tips_tips))
                        .setContent(getString(R.string.chat_dialog_red_packet))
                        .setBottomLeftText(getString(R.string.chat_action_cancel))
                        .setBottomRightText(getString(R.string.chat_action_set))
                        .setBottomRightClickListener { dialog ->
                            dialog?.dismiss()
                            ARouter.getInstance().build(AppRoute.PAY_PASSWORD)
                                    .withInt("mode", PayPasswordActivity.SET_PASSWORD)
                                    .navigation()
                        }
                        .create(instance)
                        .show()
            }
        })
        viewModel.sendRewardPacket.observe(this, Observer {
            dismiss()
            payDialog?.dismiss()
            PraiseUtil.showReward(instance)
            finish()
        })
    }

    override fun initData() {
        rv_coin.layoutManager = LinearLayoutManager(this)
        chooseCoinAdapter = object : CommonAdapter<RedPacketCoin>(this, R.layout.item_red_packet_coin, coinList) {
            override fun convert(holder: ViewHolder?, coin: RedPacketCoin?, position: Int) {
                holder?.setText(R.id.coin_name, "${coin?.coinName}/${coin?.coinNickname}")
                holder?.setText(R.id.coin_assets, FinanceUtils.getPlainNum(coin?.amount
                        ?: 0.0, coin?.decimalPlaces ?: 0))
                Glide.with(instance).load(coin?.iconUrl).into(holder?.getView(R.id.iv_coin) as ImageView)
            }
        }
        chooseCoinAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                if (position == -1) {
                    return
                }
                if (currentCoin?.coinId != coinList[position].coinId) {
                    currentCoin = coinList[position]
                    changeCoin()
                }
                drawer_layout.closeDrawer(GravityCompat.END)
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }
        })
        rv_coin.adapter = chooseCoinAdapter
        rv_packet_amount.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        amountAdapter = object : CommonAdapter<PreAmount>(this, R.layout.item_packet_amount, amountList) {
            override fun convert(holder: ViewHolder?, preAmount: PreAmount?, position: Int) {
                holder?.setText(R.id.tv_amount, preAmount?.amount)
                holder?.getView<View>(R.id.tv_amount)?.isSelected = preAmount?.selected ?: false
                if (preAmount?.selected == true) {
                    holder?.setTextColor(R.id.tv_amount, ContextCompat.getColor(instance, R.color.chat_color_accent))
                } else {
                    holder?.setTextColor(R.id.tv_amount, ContextCompat.getColor(instance, R.color.chat_text_grey_light))
                }
            }
        }
        amountAdapter.setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) {
                amountList.forEach { it.selected = false }
                amountList[position].selected = !et_coin_num.isFocused
                amountAdapter.notifyDataSetChanged()
                et_coin_num.setText(amountList[position].amount)
            }

            override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int): Boolean {
                return false
            }
        })
        rv_packet_amount.adapter = amountAdapter

        requestCoinList()
    }

    override fun setEvent() {
        ctb_title.tv_back.setOnClickListener { finish() }
        drawer_layout.setStatusBarBackground(R.color.chat_color_status_bg)
        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {

            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerOpened(drawerView: View) {
                requestCoinList()
            }
        })
        rl_coin_type.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.END)
        }
        send_red_packet.setOnClickListener {
            prepareSendRedPacket()
        }
        et_coin_num.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                clearSelected()
            }
        }
        et_coin_num.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (editAmount) {
                    return
                }
                editAmount = true
                var result: CharSequence = ""
                if (!TextUtils.isEmpty(s)) {
                    if ("." == s.toString()) {
                        et_coin_num.setText("0.")
                        et_coin_num.setSelection(2)
                        editAmount = false
                        return
                    } else {
                        result = FinanceUtils.formatString(s, currentCoin?.decimalPlaces ?: 2)
                        et_coin_num.setText(result)
                        et_coin_num.setSelection(Math.min(start + count, result.length))
                    }
                }
                packetAmount = if (TextUtils.isEmpty(result)) {
                    0.0
                } else {
                    result.toString().toDouble()
                }
                var tips: String? = null
                val min = if (availableAmount < currentCoin?.singleMax ?: 0.0) {
                    availableAmount
                } else {
                    tips = getString(R.string.chat_tips_coin_num_limit2, "${FinanceUtils.getPlainNum(
                            currentCoin?.singleMax ?: 0.0, currentCoin?.decimalPlaces
                            ?: 0)}${currentCoin?.coinName}")
                    currentCoin?.singleMax ?: 0.0
                }
                if (packetAmount > min) {
                    clearSelected()
                    packetAmount = min
                    et_coin_num.setText(FinanceUtils.getPlainNum(min, currentCoin?.decimalPlaces
                            ?: 0))
                    et_coin_num.setSelection(et_coin_num.text.length)
                    if (tips != null) {
                        ShowUtils.showToastNormal(instance, tips)
                    }
                }
                calculateTotal()
                editAmount = false
            }
        })
    }

    private fun clearSelected() {
        rv_packet_amount.postDelayed({
            amountList.forEach { it.selected = false }
            if (!rv_packet_amount.isComputingLayout) {
                amountAdapter.notifyDataSetChanged()
            }
        }, 200)
    }

    private fun calculateTotal() {
        send_red_packet.isEnabled = packetAmount > 0
        coin_total.text = FinanceUtils.getPlainNum(coinAmount, currentCoin?.decimalPlaces
                ?: 0)
    }

    private fun changeCoin() {
        coin_type.text = currentCoin?.coinName
        coin_unit1.text = currentCoin?.coinName
        coin_unit2.text = currentCoin?.coinName
        et_coin_num.setText("")
        et_coin_num.hint = getString(R.string.chat_tips_coin_num_limit1, FinanceUtils.getPlainNum(currentCoin?.singleMin
                ?: 0.0, currentCoin?.decimalPlaces ?: 0))
        coin_total.text = FinanceUtils.getPlainNum(0.0, currentCoin?.decimalPlaces ?: 0)
        coin_assets.text = getString(R.string.chat_tips_red_packet_assets, "${FinanceUtils.getPlainNum(currentCoin?.amount
                ?: 0.0, currentCoin?.decimalPlaces ?: 0)}${currentCoin?.coinName}")
        // 记录最后选择的币种
        UserInfoPreference.getInstance().setStringPref(LAST_PACKET_COIN, currentCoin?.coinName)
        // 刷新打赏数目预选项
        currentCoinAmount = preCoinAmount.find { it.coinName == currentCoin?.coinName }
        amountList.clear()
        amountList.addAll(currentCoinAmount?.preAmount ?: DEFAULT_AMOUNT)
        amountAdapter.notifyDataSetChanged()
        et_coin_num.setText(amountList.find { it.selected }?.amount)
    }

    private fun requestCoinList() {
        CoinManager.refreshCoinList {
            coinList.clear()
            coinList.addAll(it)
            chooseCoinAdapter.notifyDataSetChanged()
            if (firstLoad && coinList.size > 0) {
                val lastCoin = UserInfoPreference.getInstance().getStringPref(LAST_PACKET_COIN, "CCNY")
                var index = coinList.indexOfFirst { coin -> coin.coinName == lastCoin }
                if (index == -1) {
                    index = 0
                }
                currentCoin = coinList[index]
                firstLoad = false
                changeCoin()
            }
        }
    }

    private fun prepareSendRedPacket() {
        if (TextUtils.isEmpty(et_coin_num.text.trim())) {
            ShowUtils.showToastNormal(this, getString(R.string.chat_tips_red_packet_error1))
            return
        }
        if (packetAmount < currentCoin?.singleMin ?: 0.0) {
            ShowUtils.showToastNormal(this, getString(R.string.chat_tips_red_packet_error3, "${currentCoin?.singleMin
                    ?: 0.0}${currentCoin?.coinName ?: ""}"))
            return
        }
        val isSetPassword = UserInfoPreference.getInstance().getBooleanPref(UserInfoPreference.SET_PAY_PASSWORD, false)
        if (isSetPassword) {
            showPayPwdDialog()
        } else {
            viewModel.isSetPayPassword()
        }
    }

    private fun showPayPwdDialog() {
        val total = coinAmount
        payDialog = PayPasswordDialog.Builder(this)
                .setAmount("${FinanceUtils.getPlainNum(total, currentCoin?.decimalPlaces
                        ?: 2)} ${currentCoin?.coinName ?: ""}")
                .setOnCodeCompleteListener(object : ChatCodeView.OnCodeCompleteListener {
                    override fun onCodeComplete(view: View?, code: String) {
                        sendRewardPacket(code)
                    }
                })
                .show()
    }

    private fun sendRewardPacket(password: String) {
        if (targetId.isNullOrEmpty()) {
            viewModel.sendRewardPacket(SendRewardPacketRequest().apply {
                channelType = this@SendRewardPacketActivity.channelType
                logId = this@SendRewardPacketActivity.logId
                currency = currentCoin?.coinName
                amount = packetAmount
                this.password = password
            })
        } else {
            viewModel.sendRewardPacketToUser(SendRewardPacketRequest().apply {
                userId = targetId
                currency = currentCoin?.coinName
                amount = packetAmount
                this.password = password
            })
        }
    }
}