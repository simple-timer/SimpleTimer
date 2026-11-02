package dev.simpletimer.listener

import dev.simpletimer.SimpleTimer
import dev.simpletimer.extension.getAudioPlayer
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * VCの更新に対して応答する
 *
 */
object GuildVoiceUpdate : ListenerAdapter() {
    /**
     * VCが更新されたときに呼び出される
     *
     * @param event [GuildVoiceUpdateEvent]
     */
    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val selfUserId = SimpleTimer.instance.shards.first().selfUser.idLong

        //Join時
        val channelJoined = event.channelJoined
        if (channelJoined != null && event.member.user.idLong == selfUserId) {
            //ステージチャンネルならスピーカーリクエストを送る
            if (channelJoined is StageChannel) {
                channelJoined.requestToSpeak().queue()
            }
        }

        //Left時
        val channelLeft = event.channelLeft
        if (channelLeft != null && !event.member.user.isBot) {
            //プレイヤーを取得
            val player = event.guild.getAudioPlayer()

            //接続を確認
            if (!player.isConnected()) return

            //VCの人数を確認
            if (channelLeft.members.any { !it.user.isBot }) return

            //切断
            player.disconnect()
        }
    }
}