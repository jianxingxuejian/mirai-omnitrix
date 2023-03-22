package org.hff.miraiomnitrix.command.any

import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.config.AccountProperties


@Command(name = ["tts", "语音"])
class Tts(accountProperties: AccountProperties) : AnyCommand {

    private val azureSpeechKey = accountProperties.azureSpeechKey
    private val azureSpeechRegion = accountProperties.azureSpeechRegion
    private var speechRecognizer: SpeechSynthesizer? = null

    val speechConfigXml = """
    <speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis'
           xmlns:mstts='https://www.w3.org/2001/mstts'
           xml:lang='en-US'>
        <voice name='zh-CN-XiaoShuangNeural'>
            <prosody volume='medium'>
                <mstts:express-as style='Chat'>
                    %s
                </mstts:express-as>
            </prosody>
        </voice>
    </speak>
""".trimIndent()

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        if (azureSpeechKey == null || azureSpeechRegion == null) return "未配置Azure Speech Key或Region".toPlainText()
        if (args.isEmpty()) return null
        if (speechRecognizer == null) {
            speechRecognizer = SpeechSynthesizer(SpeechConfig.fromSubscription(azureSpeechKey, azureSpeechRegion))
        }

        val speechSynthesisResult = withContext(Dispatchers.IO) {
            speechRecognizer!!.SpeakSsml(speechConfigXml.format(args.joinToString(" ")))
        }
        if (speechSynthesisResult.reason == ResultReason.SynthesizingAudioCompleted) {
            speechSynthesisResult.audioData?.let {
                return when (val subject = event.subject) {
                    is Group -> subject.uploadAudio(it.toExternalResource())
                    is Friend -> subject.uploadAudio(it.toExternalResource())
                    else -> null
                }
            }
        }
        return null
    }
}
