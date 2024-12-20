package com.example.flo

import android.content.Context
import androidx.room.*
import com.example.flo.data.entities.Album
import com.example.flo.data.entities.Like
import com.example.flo.data.entities.Song
import com.example.flo.data.entities.User
import com.example.flo.data.local.AlbumDao
import com.example.flo.data.local.SongDao
import com.example.flo.data.local.UserDao


@Database(entities = [Album::class, Song::class, User::class, Like::class], version = 1)
abstract class SongDatabase: RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun songDao(): SongDao
    abstract fun userDao(): UserDao

    companion object {
        private var instance: SongDatabase? = null

        @Synchronized
        fun getInstance(context: Context): SongDatabase? {
            if(instance == null) {
                synchronized(SongDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SongDatabase::class.java,
                        "song-database"
                    ).allowMainThreadQueries().build()
                }
            }

            return instance
        }
    }
}