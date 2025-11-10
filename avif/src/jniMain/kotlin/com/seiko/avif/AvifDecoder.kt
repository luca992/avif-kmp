package com.seiko.avif

actual class AvifDecoder private constructor(
    private var context: Long,
) : Closeable {
    actual companion object {
        init {
            loadNativeLibrary()
        }

        actual fun isAvifImage(bytes: ByteArray): Boolean {
            return isAvifImage(bytes, bytes.size)
        }

        @JvmStatic
        actual fun create(bytes: ByteArray, threads: Int): AvifDecoder {
            val context = createContext(bytes, bytes.size, threads)
            return AvifDecoder(context)
        }

        @JvmStatic
        actual external fun versionString(): String

        @JvmStatic
        external fun isAvifImage(bytes: ByteArray, length: Int): Boolean

        @JvmStatic
        external fun createContext(bytes: ByteArray, length: Int, threads: Int): Long
    }

    actual fun reset(): Boolean {
        return reset(context)
    }

    actual fun nthFrame(index: Int): Boolean {
        return nthFrame(context, index)
    }

    actual fun nextFrame(): Boolean {
        return nextFrame(context)
    }

    actual fun getFrame(): AvifFrame {
        return AvifFrame(getFrame(context))
    }

    actual fun getFrameIndex(): Int {
        return getFrameIndex(context)
    }

    actual fun getFrameDurationMs(): Int {
        return getFrameDurationMs(context)
    }

    actual fun getFrameCount(): Int {
        return getFrameCount(context)
    }

    actual fun getAlphaPresent(): Boolean {
        return getAlphaPresent(context)
    }

    actual fun getRepetitionCount(): Int {
        return getRepetitionCount(context)
    }

    actual override fun close() {
        val contextToClose = context
        if (contextToClose != 0L) {
            context = 0L
            destroyContext(contextToClose)
        }
    }

    protected fun finalize() {
        if (context != 0L) {
            println("AvifDecoder($context) leaked!")
        }
    }

    private external fun reset(context: Long): Boolean
    private external fun nthFrame(context: Long, index: Int): Boolean
    private external fun nextFrame(context: Long): Boolean
    private external fun getFrame(context: Long): Long
    private external fun getFrameIndex(context: Long): Int
    private external fun getFrameCount(context: Long): Int
    private external fun getFrameDurationMs(context: Long): Int
    private external fun getAlphaPresent(context: Long): Boolean
    private external fun getRepetitionCount(context: Long): Int
    private external fun destroyContext(context: Long)
}
