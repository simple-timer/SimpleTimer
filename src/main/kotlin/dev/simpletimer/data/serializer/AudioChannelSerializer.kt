@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package dev.simpletimer.data.serializer

import dev.simpletimer.SimpleTimer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel

/**
 * [AudioChannel]のSerializer
 *
 */
object AudioChannelSerializer : KSerializer<Result<AudioChannel>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AudioChannel", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Result<AudioChannel>) {
        //idを取得
        val idLong = value.getOrNull()?.idLong ?: -1
        //エンコード
        encoder.encodeLong(idLong)
    }

    override fun deserialize(decoder: Decoder): Result<AudioChannel> {
        //値を取得
        val decodeLong = decoder.decodeLong()
        //適切な値化を確認
        if (decodeLong < 0) return Result.failure(NullPointerException())
        //すべてのShardを確認
        SimpleTimer.instance.shards.forEach { jda ->
            //long値からAudioChannelを取得
            return Result.success(
                jda.getVoiceChannelById(decodeLong) ?: jda.getStageChannelById(decodeLong)
                ?: return@forEach
            )
        }
        return Result.failure(NullPointerException())
    }
}