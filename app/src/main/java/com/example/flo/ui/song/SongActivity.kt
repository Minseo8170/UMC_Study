package com.example.flo.ui.song

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flo.R
import com.example.flo.SongDatabase
import com.example.flo.SongTitle
import com.example.flo.data.entities.Song
import com.example.flo.databinding.ActivitySongBinding
import com.example.flo.ui.main.MainActivity
import com.google.gson.Gson

class SongActivity : AppCompatActivity() {
    lateinit var binding : ActivitySongBinding
    lateinit var timer : Timer
    private var mediaPlayer: MediaPlayer? = null
    private var gson: Gson = Gson()

    val songs = arrayListOf<Song>()
    lateinit var songDB: SongDatabase
    var nowPos = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlayList()
        initSong()


        val songTitle = SongTitle(binding.songMusicTitleTv.text.toString())
        binding.songDownIb.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("songTitle", songTitle.songTitle)
            startActivity(intent)
            finish()
        }
        binding.songMiniplayerIv.setOnClickListener {
            setPlayerStatus(true)
        }
        binding.songPauseIv.setOnClickListener {
            setPlayerStatus(false)
        }

        binding.songNextIv.setOnClickListener {
            moveSong(+1)
        }
        binding.songPreviousIv.setOnClickListener {
            moveSong(-1)
        }
        binding.songLikeIv.setOnClickListener {
            setLike(songs[nowPos].isLike)
        }

        Log.d("SongTitle", songTitle.songTitle)

        var checkRepeat: Boolean = true
        var checkRandom: Boolean = true
        binding.songRepeatIv.setOnClickListener {
            if(checkRepeat) {
                binding.songRepeatIv.setColorFilter(Color.parseColor("#A8A8A8"))
                checkRepeat = false
            }
            else {
                restartTimer()
                binding.songRepeatIv.setColorFilter(Color.parseColor("#000000"))
                checkRepeat = true
            }
        }
        binding.songRandomIv.setOnClickListener {
            if(checkRandom) {
                binding.songRandomIv.setColorFilter(Color.parseColor("#A8A8A8"))
                checkRandom = false
            }
            else {
                binding.songRandomIv.setColorFilter(Color.parseColor("#000000"))
                checkRandom = true
            }
        }
    }

    private fun initSong() {
        val spf = getSharedPreferences("song", MODE_PRIVATE)
        val songId = spf.getInt("songId", 0)

        nowPos = getPlayingSongPosition(songId)

        Log.d("now Song ID", songs[nowPos].id.toString())
        startTimer()
        setPlayer(songs[nowPos])
    }

    private fun setLike(isLike: Boolean) {
        songs[nowPos].isLike = !isLike
        songDB.songDao().updateIsLikeById(!isLike, songs[nowPos].id)

        if(!isLike) {
            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_on)
            Toast.makeText(this,"좋아요", Toast.LENGTH_SHORT).show()
        } else {
            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_off)
            Toast.makeText(this,"좋아요 취소", Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveSong(direct: Int) {
        if (nowPos + direct < 0) {
            Toast.makeText(this, "first song", Toast.LENGTH_SHORT).show()
            return
        }
        if (nowPos + direct >= songs.size) {
            Toast.makeText(this, "last song", Toast.LENGTH_SHORT).show()
            return
        }

        nowPos += direct

        timer.interrupt()
        startTimer()

        mediaPlayer?.release()
        mediaPlayer = null

        setPlayer(songs[nowPos])
    }

    private fun getPlayingSongPosition(songId: Int): Int {
        for (i in 0 until songs.size) {
            if (songs[i].id == songId) {
                return i
            }
        }
        return 0
    }

    private fun setPlayer(song: Song) {
        binding.songMusicTitleTv.text = song.title
        binding.songSingerNameTv.text = song.singer
        binding.songStartTimeTv.text = String.format("%02d:%02d", song.second / 60, song.second % 60)
        binding.songEndTimeTv.text = String.format("%02d:%02d", song.playTime / 60, song.playTime % 60)
        binding.songAlbumIv.setImageResource(song.coverImg!!)
        binding.songProgressSb.progress = (song.second * 1000 / song.playTime)

        val music = resources.getIdentifier(song.music, "raw", this.packageName)
        mediaPlayer = MediaPlayer.create(this, music)

        if(song.isLike) {
            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_on)
        } else {
            binding.songLikeIv.setImageResource(R.drawable.ic_my_like_off)
        }

        setPlayerStatus(song.isPlaying)
    }

    private fun startTimer() {
        timer = Timer(songs[nowPos].playTime, songs[nowPos].isPlaying)
        timer.start()
    }

    private fun stopTimer() {
        timer.interrupt()
    }

    private fun restartTimer() {
        stopTimer()
        startTimer()
    }

    private fun setPlayerStatus(isPlaying : Boolean) {
        songs[nowPos].isPlaying = isPlaying
        timer.isPlaying = isPlaying

        if(isPlaying) {
            binding.songMiniplayerIv.visibility = View.GONE
            binding.songPauseIv.visibility = View.VISIBLE
        }
        else {
            binding.songMiniplayerIv.visibility = View.VISIBLE
            binding.songPauseIv.visibility = View.GONE
        }
    }

    inner class Timer(private val playTime : Int, var isPlaying : Boolean = true) : Thread() {
        private var second : Int = 0
        private var mills : Float = 0f

        override fun run() {
            super.run()
            try {
                while (true) {

                    if (second >= playTime) {
                        break
                    }

                    if (isPlaying) {
                        sleep(50)
                        mills += 50

                        runOnUiThread {
                            binding.songProgressSb.progress = ((mills / playTime) * 100).toInt()
                        }
                        if (mills % 1000 == 0f) {
                            runOnUiThread {
                                binding.songStartTimeTv.text = String.format("%02d:%02d", second / 60, second % 60)
                            }
                            second++
                        }
                    }
                }
            }catch (e: InterruptedException) {
                Log.d("Song", "쓰레드가 죽었습니다. ${e.message}")
            }

        }
    }

    //사용자가 포커스를 잃었을 때 음악 중지
    override fun onPause() {
        super.onPause()

        songs[nowPos].second = ((binding.songProgressSb.progress * songs[nowPos].playTime) / 100 ) / 1000
        songs[nowPos].isPlaying = false
        setPlayerStatus(false)

        // 내부 저장소에 데이터를 저장할 수 있도록 해주는 것, 앱이 종료되어도 저장해두었다가 꺼내서 사용할 수 있게 해줌
        // 자기 앱에서만 사용 가능하도록 해주는 MODE_PRIVATE
        // 요놈은 에디터라는 것을 사용해야만 가능함
        val sharedPreferences =  getSharedPreferences("song", MODE_PRIVATE)
        val editor = sharedPreferences.edit() // 에디터

        editor.putInt("songId", songs[nowPos].id)

        //git에서 commit과 push 역할
        editor.apply()
    }

    private fun initPlayList(){
        songDB = SongDatabase.getInstance(this)!!
        songs.addAll(songDB.songDao().getSongs())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()

        //여기 미디어 플레이어 리소스 해제
        //mediaPlayer?.release()
        //mediaPlayer = null
    }
}