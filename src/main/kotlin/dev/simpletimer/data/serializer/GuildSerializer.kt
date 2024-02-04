package dev.simpletimer.data.serializer

import dev.simpletimer.SimpleTimer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.dv8tion.jda.api.entities.Guild

/**
 * [Guild]のSerializer
 *
 */
object GuildSerializer : KSerializer<Result<Guild>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Guild", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Result<Guild>) {
        //idを取得
        val idLong = value.getOrNull()?.idLong ?: -1
        //エンコード
        encoder.encodeLong(idLong)
    }

    override fun deserialize(decoder: Decoder): Result<Guild> {
        //値を取得
        val decodeLong = decoder.decodeLong()
        //適切な値化を確認
        if (decodeLong < 0) return Result.failure(NullPointerException())
        //すべてのShardを確認
        SimpleTimer.instance.shards.forEach { jda ->
            //long値からGuildを取得
            return Result.success(jda.getGuildById(decodeLong) ?: return@forEach)
        }
        return Result.failure(NullPointerException())
    }
}