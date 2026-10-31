package com.Lia.assistant.voice

import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class LiveAudioIO(
    private val onMicChunk: (ByteArray) -> Unit,
    private val onMicAmplitude: (Float) -> Unit
) {
    companion object {
        const val MIC_SAMPLE_RATE = 16000
        const val SPEAKER_SAMPLE_RATE = 24000
        const val MIC_CHUNK_SIZE = 2048

        fun calculateRms(pcmBytes: ByteArray): Float {
            if (pcmBytes.size < 2) return 0f
            var sumSquares = 0.0
            val sampleCount = pcmBytes.size / 2
            for (i in 0 until sampleCount) {
                val low = pcmBytes[i * 2].toInt() and 0xFF
                val high = pcmBytes[i * 2 + 1].toInt()
                val sample = (high shl 8) or low
                sumSquares += (sample * sample).toDouble()
            }
            val mean = sumSquares / sampleCount
            val rawRms = sqrt(mean) / 32768.0
            return max(0f, min(1f, rawRms.toFloat()))
        }
    }

    private var audioRecord: AudioRecord? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var gainControl: AutomaticGainControl? = null

    private var audioTrack: AudioTrack? = null
    private val isRecording = AtomicBoolean(false)
    private var recordThread: Thread? = null
    private val lock = Any()
    private var currentVolume = 1.0f

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording.get()) return

        val minBuf = AudioRecord.getMinBufferSize(
            MIC_SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufSize = max(minBuf, 4096)

        try {
            val record = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MIC_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufSize
            )

            val sessionId = record.audioSessionId
            if (sessionId != 0) {
                try {
                    if (AcousticEchoCanceler.isAvailable()) {
                        echoCanceler = AcousticEchoCanceler.create(sessionId)?.apply { enabled = true }
                    }
                } catch (_: Exception) {}

                try {
                    if (NoiseSuppressor.isAvailable()) {
                        noiseSuppressor = NoiseSuppressor.create(sessionId)?.apply { enabled = true }
                    }
                } catch (_: Exception) {}

                try {
                    if (AutomaticGainControl.isAvailable()) {
                        gainControl = AutomaticGainControl.create(sessionId)?.apply { enabled = true }
                    }
                } catch (_: Exception) {}
            }

            record.startRecording()
            audioRecord = record
            isRecording.set(true)

            recordThread = Thread({
                val buffer = ByteArray(MIC_CHUNK_SIZE)
                while (isRecording.get()) {
                    val read = record.read(buffer, 0, MIC_CHUNK_SIZE)
                    if (read > 0) {
                        val chunk = if (read == MIC_CHUNK_SIZE) buffer.clone() else buffer.copyOf(read)
                        val amp = calculateRms(chunk)
                        onMicAmplitude(amp)
                        onMicChunk(chunk)
                    }
                }
            }, "LiveAudioIO-MicThread").apply { start() }
        } catch (_: Exception) {
            isRecording.set(false)
        }
    }

    fun stopRecording() {
        isRecording.set(false)
        recordThread?.interrupt()
        recordThread = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        try { echoCanceler?.release() } catch (_: Exception) {}
        try { noiseSuppressor?.release() } catch (_: Exception) {}
        try { gainControl?.release() } catch (_: Exception) {}
        echoCanceler = null
        noiseSuppressor = null
        gainControl = null
    }

    private fun getOrCreateSpeaker(): AudioTrack {
        synchronized(lock) {
            audioTrack?.let { return it }

            val minBuf = AudioTrack.getMinBufferSize(
                SPEAKER_SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufSize = minBuf * 4

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SPEAKER_SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack(
                attributes,
                format,
                bufSize,
                AudioTrack.MODE_STREAM,
                0
            )
            track.setVolume(currentVolume)
            track.play()
            audioTrack = track
            return track
        }
    }

    fun playChunk(pcmBytes: ByteArray): Float {
        if (pcmBytes.isEmpty()) return 0f
        val track = getOrCreateSpeaker()
        track.write(pcmBytes, 0, pcmBytes.size)
        return calculateRms(pcmBytes)
    }

    fun flushPlayback() {
        synchronized(lock) {
            audioTrack?.let { track ->
                try {
                    track.pause()
                    track.flush()
                    track.play()
                } catch (_: Exception) {}
            }
        }
    }

    fun setVolume(volume: Float) {
        val clamped = max(0f, min(1f, volume))
        currentVolume = clamped
        synchronized(lock) {
            audioTrack?.setVolume(clamped)
        }
    }

    fun release() {
        stopRecording()
        synchronized(lock) {
            audioTrack?.let { track ->
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
            audioTrack = null
        }
    }
}
