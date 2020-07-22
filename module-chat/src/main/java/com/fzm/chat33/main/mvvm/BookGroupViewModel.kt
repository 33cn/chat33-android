package com.fzm.chat33.main.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fuzamei.common.net.Result
import com.fuzamei.componentservice.app.LoadingViewModel
import com.fzm.chat33.core.db.bean.RoomListBean
import com.fzm.chat33.core.repo.ContactsRepository
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * @author zhengjy
 * @since 2019/10/10
 * Description:
 */
class BookGroupViewModel @Inject constructor(
        private val repository: ContactsRepository
): LoadingViewModel() {

    private val _getRoomList by lazy { MutableLiveData<Result<RoomListBean.Wrapper>>() }
    val getRoomList: LiveData<Result<RoomListBean.Wrapper>>
        get() = _getRoomList

    val updateRoom: LiveData<List<RoomListBean>>
        get() = repository.updateRoom

    fun getRoomList(type: Int) {
        launch {
            _getRoomList.value = withContext(Dispatchers.IO) {
                repository.getRoomList(type)
            }
        }
    }

    fun getLocalRoomList() : List<RoomListBean> {
        return repository.getLocalRoomList()
    }
}