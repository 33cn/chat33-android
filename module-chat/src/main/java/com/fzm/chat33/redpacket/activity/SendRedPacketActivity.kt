package com.fzm.chat33.redpacket.activity

import android.text.InputFilter
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
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
import com.fuzamei.common.utils.ArithUtils
import com.fuzamei.common.utils.FinanceUtils
import com.fuzamei.common.utils.ShowUtils
import com.fuzamei.common.utils.task.Task
import com.fuzamei.common.utils.task.TaskManager
import com.fuzamei.common.widget.GuideUserView
import com.fuzamei.componentservice.app.AppRoute
import com.fuzamei.componentservice.base.DILoadableActivity
import com.fuzamei.componentservice.ext.findViewModel
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.RedPacketCoin
import com.fzm.chat33.core.db.bean.ChatFile
import com.fzm.chat33.core.db.bean.ChatMessage
import com.fzm.chat33.core.global.Chat33Const
import com.fzm.chat33.core.provider.CoinManager
import com.fzm.chat33.core.request.SendRedPacketRequest
import com.fzm.chat33.core.utils.UserInfoPreference
import com.fzm.chat33.global.AppConst
import com.fzm.chat33.main.activity.PayPasswordActivity
import com.fzm.chat33.redpacket.mvvm.SendPacketViewModel
import com.fzm.chat33.utils.SimpleTextWatcher
import com.fzm.chat33.widget.ChatCodeView
import com.fzm.chat33.widget.PayPasswordDialog
import com.fuzamei.componentservice.widget.dialog.EasyDialog
import com.fzm.chat33.core.utils.UserInfoPreference.LAST_PACKET_COIN
import com.fzm.chat33.core.utils.UserInfoPreference.SHOW_SEND_PACKET_GUIDANCE
import kotlinx.android.synthetic.main.activity_send_red_packet.*
import kotlinx.android.synthetic.main.send_coin_drawer.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject
import kotlin.math.max

/**
 * @author zhengjy
 * @since 2019/03/11
 * Description:新版红包页面
 */
@Route(path = "/app/redPacket", extras = AppConst.NEED_LOGIN)
class SendRedPacketActivity : DILoadableActivity() {

    companion object {
        private const val TYPE_LUCKY = 1
        private const val TYPE_FAIR = 2

        private const val MODE_RED_PACKET = 0
        private const val MODE_TEXT_PACKET = 1
    }

    lateinit var chooseCoinAdapter: CommonAdapter<RedPacketCoin>
    var coinList: MutableList<RedPacketCoin> = mutableListOf()
    var payDialog: PayPasswordDialog? = null

    private var editAmount = false
    private var editNum = false

    @Autowired
    @JvmField
    var targetId: String? = null

    @Autowired
    @JvmField
    var isGroup: Boolean = true

    @Autowired
    @JvmField
    var mode: Int = MODE_RED_PACKET
    var packetSource: Int = 1

    private var firstLoad = true
    private var currentCoin: RedPacketCoin? = null

    private var guideView: GuideUserView? = null

    private var packetRemark: String? = null
    private var redPacketType = TYPE_LUCKY

    /**
     * 红包个数
     */
    private var packetNum = 0

    /**
     * 填写的红包金额（单个），用于计算和请求参数
     */
    private var packetAmount: Double = 0.0

    /**
     * 实际需要支付的金额，用于显示
     */
    private var coinAmount: Double = 0.0
        get() {
            // 群聊红包需要加上手续费金额
            return if (isGroup && packetAmount > 0) {
                if (redPacketType == TYPE_FAIR) {
                    ArithUtils.mul(packetAmount, packetNum.toDouble()) + (currentCoin?.fee ?: 0.0)
                } else {
                    packetAmount + (currentCoin?.fee ?: 0.0)
                }
            } else {
                packetAmount
            }
        }

    /**
     * 币种可用资产，减去手续费
     * 这个参数用于本地使用
     */
    private var availableAmount: Double = 0.0
        get() {
            return if (isGroup) {
                max((currentCoin?.amount ?: 0.0) - (currentCoin?.fee ?: 0.0), 0.0)
            } else {
                currentCoin?.amount ?: 0.0
            }
        }

    @Inject
    lateinit var provider: ViewModelProvider.Factory
    lateinit var viewModel: SendPacketViewModel

    override fun getLayoutId(): Int {
        return R.layout.activity_send_red_packet
    }

    override fun onBackPressed() {
        if (guideView?.onBackPressed() == true) {
            return
        }
        if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
            drawer_layout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun initView() {
        ARouter.getInstance().inject(this)
        viewModel = findViewModel(provider)
        packetSource = if (isGroup) 1 else 0
        packetRemark = getString(R.string.chat_tips_default_remark)
        ctb_title.tv_title_middle.text = getString(R.string.chat_title_send_red_packet)
        ctb_title.tv_title_right.text = getString(R.string.chat_tips_red_packet1)
        if (isGroup) {
            tv_group_packet_tips.visibility = View.VISIBLE
            rl_packet_num.visibility = View.VISIBLE
            packet_type.visibility = View.VISIBLE
            coin_num_tips.text = getString(R.string.chat_tips_total_num)
        } else {
            tv_group_packet_tips.visibility = View.GONE
            rl_packet_num.visibility = View.GONE
            packet_type.visibility = View.GONE
            coin_num_tips.text = getString(R.string.chat_tips_single_num)
        }
        if (mode == MODE_RED_PACKET) {
            tv_remark_num.text = getString(R.string.chat_tips_num_20, 0)
            iv_mode.setImageResource(R.mipmap.ic_packet_mode)
            et_packet_remark.filters = arrayOf(InputFilter.LengthFilter(20))
            et_packet_remark.setHint(R.string.chat_tips_default_remark)
        } else {
            tv_remark_num.text = getString(R.string.chat_tips_num_500, 0)
            iv_mode.setImageResource(R.mipmap.ic_text_packet_mode)
            et_packet_remark.filters = arrayOf(InputFilter.LengthFilter(500))
            et_packet_remark.setHint(R.string.chat_tips_default_remark1)
        }
        TaskManager.create().addTask(object : Task() {
            override fun work() {
                if (UserInfoPreference.getInstance().getBooleanPref(SHOW_SEND_PACKET_GUIDANCE, true)) {
                    guideView = GuideUserView.show(R.id.confirm, View.OnClickListener {
                        UserInfoPreference.getInstance().setBooleanPref(SHOW_SEND_PACKET_GUIDANCE, false)
                        done()
                    }, instance, GuideUserView.ViewEntity(iv_mode,
                            R.layout.layout_send_packet_guidance, null))
                } else {
                    done()
                }
            }
        }).start()
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
        viewModel.sendRedPacket.observe(this, Observer {
            if(it == null) {
                payDialog?.clear()
            } else {
                val packet = it
                val chatFile = ChatFile().apply {
                    coin = packet.coin
                    coinName = currentCoin?.coinName
                    packetId = packet.packetId
                    packetUrl = packet.packetUrl
                    packetType = packet.packetType
                    redBagRemark = packet.remark
                    packetMode = mode
                }
                val message = ChatMessage.create(targetId,
                        if (isGroup) Chat33Const.CHANNEL_ROOM else Chat33Const.CHANNEL_FRIEND,
                        ChatMessage.Type.RED_PACKET, 2, chatFile)
                EventBus.getDefault().post(message)
                payDialog?.cancel()
                dismiss()
                finish()
            }
        })
    }

    private fun switchPacketMode() {
        if (mode == MODE_RED_PACKET) {
            mode = MODE_TEXT_PACKET
            tv_remark_num.text = getString(R.string.chat_tips_num_500, et_packet_remark.text?.length ?: 0)
            et_packet_remark.filters = arrayOf(InputFilter.LengthFilter(500))
            iv_mode.setImageResource(R.mipmap.ic_text_packet_mode)
            et_packet_remark.setHint(R.string.chat_tips_default_remark1)
        } else {
            mode = MODE_RED_PACKET
            if (et_packet_remark.text?.length ?: 0 > 20) {
                et_packet_remark.setText(et_packet_remark.text?.substring(0, 20))
                et_packet_remark.setSelection(20)
            }
            tv_remark_num.text = getString(R.string.chat_tips_num_20, et_packet_remark.text?.length ?: 0)
            et_packet_remark.filters = arrayOf(InputFilter.LengthFilter(20))
            iv_mode.setImageResource(R.mipmap.ic_packet_mode)
            et_packet_remark.setHint(R.string.chat_tips_default_remark)
        }
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
        requestCoinList()
    }

    override fun setEvent() {
        ctb_title.tv_back.setOnClickListener { finish() }
        ctb_title.tv_title_right.setOnClickListener {
            ARouter.getInstance().build(AppRoute.RED_PACKET_RECORDS).navigation()
        }
        iv_mode.setOnClickListener { switchPacketMode() }
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
        packet_type.setOnClickListener {
            if (redPacketType == TYPE_LUCKY) {
                redPacketType = TYPE_FAIR
                packet_type.setImageResource(R.mipmap.icon_packet_type_fair)
                coin_num_tips.text = getString(R.string.chat_tips_single_num)
            } else {
                redPacketType = TYPE_LUCKY
                packet_type.setImageResource(R.mipmap.icon_packet_type_lucky)
                coin_num_tips.text = getString(R.string.chat_tips_total_num)
            }
            calculateTotal()
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
        et_packet_num.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (editNum) {
                    return
                }
                editNum = true
                packetNum = if (TextUtils.isEmpty(s)) {
                    0
                } else {
                    s.toString().toInt()
                }
                calculateTotal()
                editNum = false
            }
        })
        et_packet_remark.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (mode == MODE_RED_PACKET) {
                    tv_remark_num.text = getString(R.string.chat_tips_num_20, s?.length ?: 0)
                } else {
                    tv_remark_num.text = getString(R.string.chat_tips_num_500, s?.length ?: 0)
                }
                packetRemark = s.toString()
            }
        })
        coin_assets.setOnClickListener {
            if (availableAmount > 0) {
                et_coin_num.setText(FinanceUtils.getPlainNum(availableAmount, currentCoin?.decimalPlaces ?: 0))
                et_coin_num.setSelection(et_coin_num.text.length)
            }
        }
        rl_coin_type.setOnClickListener {
            drawer_layout.openDrawer(GravityCompat.END)
        }
        send_red_packet.setOnClickListener {
            prepareSendRedPacket()
        }
    }

    private fun calculateTotal() {
        if (isGroup) {
            if (redPacketType == TYPE_LUCKY) {
                if (packetNum > 0) {
                    if (packetAmount > 0) {
                        send_red_packet.isEnabled = true
                        val avg = ArithUtils.div(packetAmount, packetNum.toDouble())
                        if (avg > 0 && avg < Math.pow(10.0, -(currentCoin?.decimalPlaces?.toDouble()
                                        ?: 0.0))) {
                            packetNum = ArithUtils.mul(packetAmount, Math.pow(10.0, currentCoin?.decimalPlaces?.toDouble()
                                    ?: 0.0)).toInt()
                            et_packet_num.setText("$packetNum")
                            et_packet_num.setSelection(et_packet_num.text.length)
                            ShowUtils.showToastNormal(getString(R.string.chat_tips_coin_num_limit3,
                                    "${FinanceUtils.getAccuracy(currentCoin?.decimalPlaces
                                            ?: 0)}${currentCoin?.coinName}"))
                        } else {
                            coin_total.text = FinanceUtils.getPlainNum(coinAmount, currentCoin?.decimalPlaces
                                    ?: 0)
                        }
                    } else {
                        send_red_packet.isEnabled = false
                    }
                } else {
                    send_red_packet.isEnabled = false
                    coin_total.text = FinanceUtils.getPlainNum(coinAmount, currentCoin?.decimalPlaces
                            ?: 0)
                }
            } else {
                if (packetAmount == 0.0 || packetNum == 0) {
                    coin_total.text = FinanceUtils.getPlainNum(0.0, currentCoin?.decimalPlaces ?: 0)
                    send_red_packet.isEnabled = false
                } else {
                    val total = coinAmount
                    val tips: String
                    // min代表资产和单次发送最大限额两者中的最小值
                    // 用户期望发送的红包总额total不能超过这个min，超过就会报响应的错误
                    val min = if (currentCoin?.amount ?: 0.0 < currentCoin?.singleMax ?: 0.0) {
                        tips = getString(R.string.chat_tips_coin_num_limit4)
                        currentCoin?.amount ?: 0.0
                    } else {
                        tips = getString(R.string.chat_tips_coin_num_limit2, "${FinanceUtils.getPlainNum(
                                currentCoin?.singleMax ?: 0.0, currentCoin?.decimalPlaces
                                ?: 0)}${currentCoin?.coinName}")
                        currentCoin?.singleMax ?: 0.0
                    }
                    if (total > min) {
                        packetNum = 0
                        et_packet_num.setText("")
                        ShowUtils.showToastNormal(tips)
                        send_red_packet.isEnabled = false
                        return
                    }
                    send_red_packet.isEnabled = true
                    coin_total.text = FinanceUtils.getPlainNum(Math.min(total, min), currentCoin?.decimalPlaces
                            ?: 0)
                }
            }
        } else {
            coin_total.text = FinanceUtils.getPlainNum(coinAmount, currentCoin?.decimalPlaces ?: 0)
            send_red_packet.isEnabled = packetAmount > 0
        }
    }

    private fun changeCoin() {
        coin_type.text = currentCoin?.coinName
        coin_unit1.text = currentCoin?.coinName
        coin_unit2.text = currentCoin?.coinName
        if (/*手续费大于0*/isGroup && currentCoin?.fee ?: 0.0 > 0) {
            tv_group_packet_tips.text = getString(R.string.chat_tips_packet_fee, FinanceUtils.getPlainNum(currentCoin?.fee
                    ?: 0.0, 2), currentCoin?.coinName)
            tv_group_packet_tips.visibility = View.VISIBLE
        } else {
            tv_group_packet_tips.visibility = View.GONE
        }
        et_coin_num.setText("")
        et_coin_num.hint = getString(R.string.chat_tips_coin_num_limit1, FinanceUtils.getPlainNum(currentCoin?.singleMin
                ?: 0.0, currentCoin?.decimalPlaces ?: 0))
        et_packet_num.setText("")
        coin_total.text = FinanceUtils.getPlainNum(0.0, currentCoin?.decimalPlaces ?: 0)
        coin_assets.text = getString(R.string.chat_tips_red_packet_assets, "${FinanceUtils.getPlainNum(currentCoin?.amount
                ?: 0.0, currentCoin?.decimalPlaces ?: 0)}${currentCoin?.coinName}")
        UserInfoPreference.getInstance().setStringPref(LAST_PACKET_COIN, currentCoin?.coinName)
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
        if (isGroup) {
            if (TextUtils.isEmpty(et_coin_num.text.trim()) || packetNum == 0) {
                ShowUtils.showToastNormal(this, getString(R.string.chat_tips_red_packet_error2))
                return
            }
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
                        sendRedPacket(code)
                    }
                })
                .show()
    }

    private fun sendRedPacket(password: String) {
        val request = SendRedPacketRequest()
        if (redPacketType == TYPE_LUCKY) {
            request.amount = packetAmount
        } else if (redPacketType == TYPE_FAIR) {
            request.amount = ArithUtils.mul(packetAmount, packetNum.toDouble())
        }
        if (isGroup) {
            request.size = packetNum
            request.type = redPacketType
        } else {
            // 私聊只能发普通红包
            request.type = TYPE_FAIR
        }
        request.coin = currentCoin?.coinId ?: 0
        request.coinName = currentCoin?.coinName
        request.toId = targetId
        request.cType = packetSource
        request.remark = packetRemark?.ifEmpty {
            getString(R.string.chat_tips_default_remark)
        }
        request.ext = SendRedPacketRequest.Ext(password)
        viewModel.sendRedPacket(request)
    }
}