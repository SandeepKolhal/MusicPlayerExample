package com.sk.ithenaasssignment

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val musicUri: Uri by lazy {
        Uri.parse("android.resource://" + packageName + "/" + R.raw.namo_namo)
    }

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var runnable: Runnable
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var isPause: Boolean = false
    private var isStart: Boolean = false
    private var fullTime = "00:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startMediaPlayer(0, false)

        stopMusic.visibility = View.GONE

        playPause.setOnClickListener {
            if (!isStart) {
                if (!isPause) {
                    startMediaPlayer(0, true)
                } else {
                    mediaPlayer?.let {
                        it.start()
                        isStart = true
                        isPause = false
                        playPause.setImageResource(R.drawable.ic_pause)
                        stopMusic.visibility = View.VISIBLE
                    }
                }
            } else if (isStart && isPause) {
                mediaPlayer?.let {
                    it.seekTo(it.currentPosition)
                    it.start()
                    isPause = false
                    playPause.setImageResource(R.drawable.ic_pause)
                    stopMusic.visibility = View.VISIBLE
                }
            } else if (isStart && !isPause) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.pause()
                        isPause = true
                        playPause.setImageResource(R.drawable.ic_play)
                        stopMusic.visibility = View.GONE
                    }
                }
            }
        }

        songSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                val currentTime = getTimeString(i.toLong())
                musicPlayingTime.text = "${currentTime}/${fullTime}"

                if (b) {
                    if (mediaPlayer != null) {
                        mediaPlayer?.seekTo(i)
                    } else {
                        startMediaPlayer(i, false)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Nothing to do here
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Nothing to do here
            }
        })

        stopMusic.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying || isPause) {
                    isPause = false
                    isStart = false
                    songSeekBar.progress = 0
                    it.stop()
                    releaseMediaPlayer()
                    playPause.setImageResource(R.drawable.ic_play)
                    stopMusic.visibility = View.GONE
                    handler.removeCallbacks(runnable)
                }
            }
        }
    }

    private fun startMediaPlayer(seek: Int, isPlay: Boolean) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, musicUri)
            prepare()
            setOnPreparedListener {

                it.seekTo(seek)

                if (isPlay) {
                    start()
                    isStart = true
                    isPause = false
                    playPause.setImageResource(R.drawable.ic_pause)
                    stopMusic.visibility = View.VISIBLE
                } else {
                    isStart = false
                    isPause = true
                }
            }
            setOnCompletionListener {
                releaseMediaPlayer()
                isPause = false
                isStart = false
                songSeekBar.progress = 0
                playPause.setImageResource(R.drawable.ic_play)
                stopMusic.visibility = View.GONE
            }
        }

        initializeSeekBar()
    }

    private fun initializeSeekBar() {
        mediaPlayer?.let {
            val totalSec = it.duration
            songSeekBar.max = totalSec
            songSeekBar.progress = 0

            getTimeString(totalSec.toLong())?.let { time ->
                fullTime = time
            }

            runnable = Runnable {
                val currentSec = it.currentPosition
                if (currentSec >= totalSec) {
                    songSeekBar.progress = 0
                } else {
                    songSeekBar.progress = currentSec
                }
                handler.postDelayed(runnable, 1000)
            }
            handler.postDelayed(runnable, 1000)
        }
    }

    private fun getTimeString(millis: Long): String? {
        val time = StringBuffer()
        val minutes = (millis % (1000 * 60 * 60) / (1000 * 60)).toInt()
        val seconds = (millis % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        time
            .append(String.format("%02d", minutes))
            .append(":")
            .append(String.format("%02d", seconds))
        return time.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(runnable)
    }
}
