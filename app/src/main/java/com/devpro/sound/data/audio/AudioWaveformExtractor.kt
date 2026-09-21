package com.devpro.sound.data.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class AudioWaveformExtractor(
    private val context: Context
) {
    suspend fun extract(uri: Uri): List<Float> = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        var decoder: MediaCodec? = null

        try {
            extractor.setDataSource(context, uri, null)

            val audioTrackIndex = (0 until extractor.trackCount)
                .firstOrNull { index ->
                    extractor.getTrackFormat(index)
                        .getString(MediaFormat.KEY_MIME)
                        ?.startsWith("audio/") == true
                }
                ?: throw IllegalArgumentException("File không chứa track audio")

            extractor.selectTrack(audioTrackIndex)
            val inputFormat = extractor.getTrackFormat(audioTrackIndex)
            val mime = inputFormat.getString(MediaFormat.KEY_MIME)
                ?: throw IllegalArgumentException("Không xác định được định dạng audio")

            decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()

            val accumulator = WaveformAccumulator(
                sampleRate = inputFormat.getIntegerOrDefault(MediaFormat.KEY_SAMPLE_RATE, 44_100),
                channelCount = inputFormat.getIntegerOrDefault(MediaFormat.KEY_CHANNEL_COUNT, 1)
            )
            val bufferInfo = MediaCodec.BufferInfo()
            var inputFinished = false
            var outputFinished = false

            while (!outputFinished) {
                if (!inputFinished) {
                    val inputBufferIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                    if (inputBufferIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                            ?: continue
                        inputBuffer.clear()
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)

                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(
                                inputBufferIndex,
                                0,
                                0,
                                0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            inputFinished = true
                        } else {
                            decoder.queueInputBuffer(
                                inputBufferIndex,
                                0,
                                sampleSize,
                                extractor.sampleTime.coerceAtLeast(0L),
                                0
                            )
                            extractor.advance()
                        }
                    }
                }

                when (val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val outputFormat = decoder.outputFormat
                        accumulator.updateFormat(
                            sampleRate = outputFormat.getIntegerOrDefault(
                                MediaFormat.KEY_SAMPLE_RATE,
                                accumulator.sampleRate
                            ),
                            channelCount = outputFormat.getIntegerOrDefault(
                                MediaFormat.KEY_CHANNEL_COUNT,
                                accumulator.channelCount
                            )
                        )
                    }

                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit

                    else -> if (outputBufferIndex >= 0) {
                        decoder.getOutputBuffer(outputBufferIndex)?.let { outputBuffer ->
                            consumePcmBuffer(outputBuffer, bufferInfo, accumulator)
                        }
                        decoder.releaseOutputBuffer(outputBufferIndex, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputFinished = true
                        }
                    }
                }
            }

            accumulator.finish()
        } finally {
            runCatching { decoder?.stop() }
            decoder?.release()
            extractor.release()
        }
    }

    private fun consumePcmBuffer(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo,
        accumulator: WaveformAccumulator
    ) {
        val start = bufferInfo.offset.coerceAtLeast(0)
        val end = (bufferInfo.offset + bufferInfo.size).coerceAtMost(outputBuffer.capacity())
        if (start >= end) return

        outputBuffer
            .duplicate()
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply {
                position(start)
                limit(end)
            }
            .let(accumulator::consume16BitPcm)
    }

    private class WaveformAccumulator(
        var sampleRate: Int,
        var channelCount: Int
    ) {
        private val rawBars = mutableListOf<Double>()
        private var sumSquares = 0.0
        private var sampleCount = 0L
        private var peakAmplitude = 0.0

        fun updateFormat(sampleRate: Int, channelCount: Int) {
            if (sampleRate > 0) this.sampleRate = sampleRate
            if (channelCount > 0) this.channelCount = channelCount
        }

        fun consume16BitPcm(buffer: ByteBuffer) {
            while (buffer.remaining() >= Short.SIZE_BYTES) {
                val sample = buffer.short.toDouble() / Short.MAX_VALUE
                peakAmplitude = maxOf(peakAmplitude, abs(sample))
                sumSquares += sample * sample
                sampleCount++

                if (sampleCount >= samplesPerBar()) {
                    addBar()
                }
            }
        }

        fun finish(): List<Float> {
            if (sampleCount > 0) addBar()
            if (rawBars.isEmpty()) return emptyList()

            val sortedBars = rawBars.sorted()
            val low = percentile(sortedBars, 0.08)
            val high = percentile(sortedBars, 0.92)
            val range = (high - low).takeIf { it > 0.0001 } ?: 1.0

            return rawBars.map { amplitude ->
                val normalized = ((amplitude - low) / range).coerceIn(0.02, 1.0)
                normalized.pow(0.72).toFloat().coerceIn(0f, 1f)
            }
        }

        private fun samplesPerBar(): Long {
            return (
                (
                    sampleRate.toLong().coerceAtLeast(1L) *
                        channelCount.toLong().coerceAtLeast(1L)
                    ) / BARS_PER_SECOND
                ).coerceAtLeast(1L)
        }

        private fun addBar() {
            val rms = sqrt(sumSquares / sampleCount.coerceAtLeast(1L))
            // RMS represents sustained loudness; peak keeps transients visible.
            rawBars += (rms * 0.7) + (peakAmplitude * 0.3)
            sumSquares = 0.0
            sampleCount = 0L
            peakAmplitude = 0.0
        }

        private fun percentile(values: List<Double>, fraction: Double): Double {
            val index = (fraction * (values.lastIndex)).toInt()
                .coerceIn(0, values.lastIndex)
            return values[index]
        }
    }

    private companion object {
        const val TIMEOUT_US = 10_000L
        const val BARS_PER_SECOND = 4L

        fun MediaFormat.getIntegerOrDefault(key: String, default: Int): Int {
            return if (containsKey(key)) getInteger(key) else default
        }
    }
}
