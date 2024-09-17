package com.example.whatsupsend

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessagesDao {

    @Query("SELECT * FROM Messages")
    suspend fun getAll(): List<Messages>

    @Query("SELECT send FROM Messages WHERE ID == (:i)")
    suspend fun getFlag(i: Int): Boolean?

    @Query("SELECT send FROM Messages ORDER BY ID DESC LIMIT 1")
    suspend fun getLastFlag(): Boolean

    @Query("SELECT Count(*) FROM Messages")
    suspend fun getNumberOfRows(): Int

    @Query("DELETE FROM Messages")
    suspend fun delete()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(messages: Messages)




//        @Query("SELECT * FROM Messages WHERE uid IN (:userIds)")
//    fun loadAllByIds(userIds: IntArray): List<Messages>
//
//    @Query("SELECT * FROM Messages WHERE send LIKE true")
//    fun findByFlag(send: Boolean): Messages
//
//    @Insert
//    fun insertMessage(vararg message: String?)
//
//    @Delete
//    suspend fun delete(user: Messages)
}