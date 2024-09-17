package com.example.whatsupsend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

//@Dao
//abstract class MessagesDaoa {
//    @Query("SELECT * FROM Messages")
//    abstract suspend fun getAll(): List<Messages>
//
//    @Query("SELECT send FROM Messages WHERE ID == (:i)")
//    abstract suspend fun getFlag(i: Int): Boolean
//
//    @Query("SELECT * FROM Messages ORDER BY ID DESC LIMIT 1")
//    abstract suspend fun getLastFlag(): Boolean
//
//
////    @Query("INSERT INTO Messages VALUES (:message)")
////    fun insertMessage(message: Messages)
//
//    @Insert
//    abstract fun insertMessage(messages: Messages)
//}