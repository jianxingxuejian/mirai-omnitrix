package org.hff.miraiomnitrix.command.any

import com.microsoft.cognitiveservices.speech.ResultReason
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.config.AccountProperties


@Command(name = ["tts", "语音"])
class Tts(private val accountProperties: AccountProperties) : AnyCommand {

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

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        if (args.isEmpty()) return null
        if (speechRecognizer == null) {
            speechRecognizer = SpeechSynthesizer(
                SpeechConfig.fromSubscription(
                    accountProperties.azureSpeechKey,
                    accountProperties.azureSpeechRegion
                )
            )
        }

        val speechSynthesisResult = withContext(Dispatchers.IO) {
            speechRecognizer!!.SpeakSsml(speechConfigXml.format(args.joinToString(" ")))
        }
        if (speechSynthesisResult.reason == ResultReason.SynthesizingAudioCompleted) {
            speechSynthesisResult.audioData?.let {
                when (val subject = event.subject) {
                    is Group -> {
                        val audio = subject.uploadAudio((it.toExternalResource()))
                        subject.sendMessage(audio)
                    }

                    is Friend -> {
                        val audio = subject.uploadAudio((it.toExternalResource()))
                        subject.sendMessage(audio)
                    }

                    else -> {}
                }
            }
        }
        return null
    }
}