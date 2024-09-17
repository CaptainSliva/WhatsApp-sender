package com.example.whatsupsend

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Messages(
    @PrimaryKey val ID: Int,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "send") val flag: Boolean,
)
