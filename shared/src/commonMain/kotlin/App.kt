import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.seiko.avif.AvifDecoder
import com.seiko.avif.PlatformBitmap
import com.seiko.avif.compose.asImageBitmap
import com.seiko.avif.createPlatformBitmap
import com.seiko.avif.getBitmapResult
import com.seiko.avif.use
import io.github.qdsfdhvh.shared.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(AvifDecoder.versionString(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))

            var title1 by remember { mutableStateOf("") }
            Text(title1, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            val painter1 by produceState<Painter>(EmptyPainter) {
                val bytes = Res.readBytes("files/test.avif")
                title1 += "isAvif=${AvifDecoder.isAvifImage(bytes)}\n"

                AvifDecoder.create(bytes).use { decoder ->
                    decoder.nextFrame()

                    val frame = decoder.getFrame()
                    title1 += "width=${frame.getWidth()}, height=${frame.getHeight()}"

                    frame.getBitmapResult()
                        .onSuccess {
                            value = BitmapPainter(it.asImageBitmap())
                        }.onFailure {
                            println(it.message.orEmpty())
                        }
                }
            }
            Image(
                painter1,
                contentDescription = null,
                modifier = Modifier.size(200.dp),
            )
            Spacer(Modifier.height(8.dp))
            var title2 by remember { mutableStateOf("") }
            var durationText by remember { mutableStateOf("") }
            Text(title2, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            val painter2 by produceState<Painter>(EmptyPainter) {
                withContext(Dispatchers.Default) {
                    val bytes = Res.readBytes("files/test_anime.avif")
                    title2 += "isAvif=${AvifDecoder.isAvifImage(bytes)}\n"

                    AvifDecoder.create(bytes).use { decoder ->
                        title2 += "count=${decoder.getFrameCount()}\n"

                        var bitmap: PlatformBitmap? = null

                        // TODO: fix decode failure of avif anime after play 80~100 frames on android (Decoding of color planes failed avif)
                        while (decoder.nextFrame() || decoder.nthFrame(0)) {
                            val frame = decoder.getFrame()

                            if (bitmap == null) {
                                bitmap = frame.createPlatformBitmap()
                            }

                            frame.decodeFrame(bitmap)
                            value = BitmapPainter(bitmap.asImageBitmap())

                            durationText = "index=${decoder.getFrameIndex()}, " +
                                "duration=${decoder.getFrameDurationMs()}ms\n" +
                                "width=${frame.getWidth()}, height=${frame.getHeight()}"

                            delay(decoder.getFrameDurationMs().milliseconds)
                        }
                    }
                }
            }
            Image(
                painter2,
                contentDescription = null,
                modifier = Modifier.size(200.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(durationText, textAlign = TextAlign.Center)
        }
    }
}

object EmptyPainter : Painter() {
    override val intrinsicSize get() = Size.Unspecified
    override fun DrawScope.onDraw() = Unit
}
