package com.seiko.avif

expect class AvifDecoder : Closeable {
    companion object {

        /**
         * Returns true if the bytes seem like an AVIF image.
         */
        fun isAvifImage(bytes: ByteArray): Boolean

        /**
         * Create and return an AvifDecoder.
         */
        fun create(bytes: ByteArray, threads: Int = 1): AvifDecoder

        /**
         * Returns a String that contains information about the libavif version, underlying codecs and
         * libyuv version (if available).
         */
        fun versionString(): String
    }

    /**
     * Reset decode
     */
    fun reset(): Boolean

    /**
     * Decode index frame.
     */
    fun nthFrame(index: Int): Boolean

    /**
     * Decode next frame.
     */
    fun nextFrame(): Boolean

    /**
     * Get current frame in the image.
     */
    fun getFrame(): AvifFrame

    /**
     * Get current frame index in the image.
     */
    fun getFrameIndex(): Int

    /**
     * Get current frame duration in the image.
     */
    fun getFrameDurationMs(): Int

    /**
     * Get the number of frames in the image.
     */
    fun getFrameCount(): Int

    /**
     * Returns true if the image contains a transparency/alpha channel, false otherwise.
     */
    fun getAlphaPresent(): Boolean

    /**
     * Get the number of repetitions for an animated image (see repetitionCount in avif.h for
     * details).
     */
    fun getRepetitionCount(): Int

    override fun close()
}

fun AvifDecoder.hasNext(): Boolean {
    return getFrameIndex() < getFrameCount() - 1
}
