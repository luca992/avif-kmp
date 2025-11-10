package com.seiko.avif

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.cinterop.toKStringFromUtf8
import platform.avif.AVIF_RESULT_OK
import platform.avif.AVIF_TRUE
import platform.avif.avifCodecVersions
import platform.avif.avifDecoder
import platform.avif.avifDecoderCreate
import platform.avif.avifDecoderDestroy
import platform.avif.avifDecoderNextImage
import platform.avif.avifDecoderNthImage
import platform.avif.avifDecoderParse
import platform.avif.avifDecoderReset
import platform.avif.avifDecoderSetIOMemory
import platform.avif.avifLibYUVVersion
import platform.avif.avifPeekCompatibleFileType
import platform.avif.avifROData
import platform.avif.avifResultToString
import platform.avif.avifVersion
import platform.posix.snprintf
import platform.posix.sprintf
import platform.posix.uint8_tVar

@OptIn(ExperimentalForeignApi::class)
actual class AvifDecoder private constructor(
    private val decoderPtr: CPointer<avifDecoder>,
) : Closeable {
    actual companion object {

        @Suppress("UNCHECKED_CAST")
        actual fun isAvifImage(bytes: ByteArray): Boolean = memScoped {
            val avif = alloc<avifROData>()
            avif.data = bytes.refTo(0).getPointer(this) as CPointer<uint8_tVar>
            avif.size = bytes.size.convert()
            avifPeekCompatibleFileType(avif.ptr) == AVIF_TRUE
        }

        @Suppress("UNCHECKED_CAST")
        actual fun create(bytes: ByteArray, threads: Int): AvifDecoder = memScoped {
            val decoderPtr = requireNotNull(avifDecoderCreate())
            with(decoderPtr.pointed) {
                maxThreads = threads
                ignoreExif = AVIF_TRUE
                ignoreXMP = AVIF_TRUE
            }
            var result = avifDecoderSetIOMemory(
                decoderPtr,
                bytes.refTo(0).getPointer(this) as CValuesRef<uint8_tVar>,
                bytes.size.convert(),
            )
            if (result != AVIF_RESULT_OK) {
                avifDecoderDestroy(decoderPtr)
                error("Failed to set AVIF IO to a memory reader: ${avifResultToString(result)?.toKString()}.")
            }
            result = avifDecoderParse(decoderPtr)
            if (result != AVIF_RESULT_OK) {
                avifDecoderDestroy(decoderPtr)
                error("Failed to parse AVIF image: ${avifResultToString(result)?.toKString()}")
            }
            return AvifDecoder(decoderPtr)
        }

        actual fun versionString(): String = memScoped {
            val codecVersionsPtr = allocArray<ByteVar>(256)
            avifCodecVersions(codecVersionsPtr)

            val libyuvVersionPtr = allocArray<ByteVar>(64)
            if (avifLibYUVVersion() > 0u) {
                sprintf(libyuvVersionPtr, "%u", avifLibYUVVersion())
            } else {
                libyuvVersionPtr[0] = '\n'.code.toByte()
            }

            val versionStringPtr = allocArray<ByteVar>(512)
            snprintf(
                versionStringPtr,
                512u,
                "libavif: %s\nCodecs: %s\nlibyuv: %s",
                avifVersion(),
                codecVersionsPtr,
                libyuvVersionPtr,
            )
            versionStringPtr.toKStringFromUtf8()
        }
    }

    private val decoder: avifDecoder
        get() = decoderPtr.pointed

    actual fun reset(): Boolean {
        return avifDecoderReset(decoderPtr) == AVIF_RESULT_OK
    }

    actual fun nthFrame(index: Int): Boolean {
        return avifDecoderNthImage(decoderPtr, index.toUInt()) == AVIF_RESULT_OK
    }

    actual fun nextFrame(): Boolean {
        return avifDecoderNextImage(decoderPtr) == AVIF_RESULT_OK
    }

    actual fun getFrame(): AvifFrame {
        val avifImagePtr = requireNotNull(decoder.image)
        return AvifFrame(avifImagePtr)
    }

    actual fun getFrameIndex(): Int {
        return decoder.imageIndex
    }

    actual fun getFrameDurationMs(): Int {
        return (decoder.imageTiming.duration * 1000).toInt() // ms
    }

    actual fun getFrameCount(): Int {
        return decoder.imageCount
    }

    actual fun getAlphaPresent(): Boolean {
        return decoder.alphaPresent == AVIF_TRUE
    }

    actual fun getRepetitionCount(): Int {
        return decoder.repetitionCount
    }

    actual override fun close() {
        avifDecoderDestroy(decoderPtr)
    }
}
