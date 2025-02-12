package com.example.soundpool

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.example.soundpool.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager
    private lateinit var soundPool: SoundPool
    private lateinit var binding: ActivityMainBinding

    data class Sonido(val id: Int, var boton: Button? = null, var cargado: Boolean = false)

    private val sonidos = HashMap<Int, Sonido>()
    private val sonidosRes = arrayListOf(
        R.raw.animal_bark_and_growl,
        R.raw.afternoon_crickets_long,
        R.raw.bee_buzz,
        R.raw.cicada_chirp,
        R.raw.cat_purr,
        R.raw.cat_purr_close,
        R.raw.buzzing_fly,
        R.raw.animal_squealing,
    )

    private lateinit var botonesRes: ArrayList<Button>
    private var actVolume = 0f
    private var maxVolume = 0f
    private var volume = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        botonesRes = arrayListOf(
            binding.button1, binding.button2, binding.button3, binding.button4, binding.button5,
            binding.button6, binding.button7, binding.button8
        )
        binding.txtCanales.doOnTextChanged { text, start, before, count ->
            val canales = getCanalesFromUI()
            if (canales != null) {
                soundPool.release()
                initAudio(canales)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        initAudio(getCanalesFromUI() ?: 8)
    }
    override fun onPause() {
        soundPool.release()
        super.onPause()
    }
    private fun getCanalesFromUI(): Int? =
        binding.txtCanales.text.toString().toIntOrNull()
    private fun deshabilitarBotones() {
        binding.button1.isEnabled = false
        binding.button2.isEnabled = false
        binding.button3.isEnabled = false
        binding.button4.isEnabled = false
        binding.button5.isEnabled = false
        binding.button6.isEnabled = false
        binding.button7.isEnabled = false
        binding.button8.isEnabled = false
    }
    private fun initAudio(canales: Int) {
        deshabilitarBotones()
        audioManager =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager
        actVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        volume = actVolume / maxVolume
        volumeControlStream = AudioManager.STREAM_MUSIC
        val audioAttr = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(canales)
            .setAudioAttributes(audioAttr)
            .build()
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            sonidos[sampleId]?.cargado = status == 0
            sonidos [sampleId]?.boton?.isEnabled = status == 0
        }
        for ((boton, idRes) in sonidosRes.withIndex()) {
            val id = soundPool.load(this, idRes, 1)
            sonidos [id] = Sonido(id, botonesRes[boton])
        }
    }
    fun onClick(view: View?) {
        val i = view?.tag as String
        val sonido = sonidos[sonidos.keys.elementAt(i.toInt() - 1)]
        if (sonido != null) {
            if (binding.swtBucle.isChecked) playLoop(sonido)
            else
                play(sonido)
        }
    }
    private fun play(sonido: Sonido) {
        if (sonido.cargado) {
            soundPool.play(sonido.id, volume, volume, 1, 0, 1f)
        }
    }
    private fun playLoop(sonido: Sonido) {
        if (sonido.cargado) {
            soundPool.play(sonido.id, volume, volume, 1, -1, 1f)
        }
    }
}