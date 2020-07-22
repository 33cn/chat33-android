package com.fzm.chat33.core.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fzm.chat33.core.db.bean.SearchHistory

/**
 * @author zhengjy
 * @since 2019/09/17
 * Description:
 */
@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY searchTime DESC LIMIT 10")
    fun getSearchHistory(): LiveData<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(history: SearchHistory)

    @Query("DELETE FROM search_history WHERE keywords=:key")
    fun deleteHistory(key: String)

    @Query("DELETE FROM search_history")
    fun deleteAllHistory()
}