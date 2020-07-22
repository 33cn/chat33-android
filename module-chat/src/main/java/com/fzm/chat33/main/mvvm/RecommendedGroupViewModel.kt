package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.ext.request
import com.fuzamei.common.ext.result
import com.fuzamei.common.ext.start
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.bean.RecommendGroup
import com.fzm.chat33.core.bean.ResultList
import com.fzm.chat33.core.repo.ContactsRepository
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/22
 * Description:群成员列表操作
 */
class RecommendedGroupViewModel @Inject constructor(
        private val contactData: ContactsRepository
) : LoadingViewModel() {

    private val _recommendGroups by lazy { MutableLiveData<RecommendGroup.Wrapper>() }
    val recommendGroups: LiveData<RecommendGroup.Wrapper>
        get() = _recommendGroups

    private val _batchJoinRoomApply by lazy { MutableLiveData<ResultList>() }
    val batchJoinRoomApply: LiveData<ResultList>
        get() = _batchJoinRoomApply

    fun recommendGroups(times: Int) {
        request {
            contactData.recommendGroups(times)
        }.result{
            _recommendGroups.value = it
        }
    }

    fun batchJoinRoomApply(rooms: List<String>) {
        start {
            loading()
        }.request {
            contactData.batchJoinRoomApply(rooms)
        }.result(onSuccess = {
            _batchJoinRoomApply.value = it
        }, onComplete = {
            dismiss()
        })
    }

}