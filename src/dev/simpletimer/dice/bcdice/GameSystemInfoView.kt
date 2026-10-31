package dev.simpletimer.dice.bcdice

import dev.simpletimer.bcdice_client.BCDiceClient
import dev.simpletimer.extension.getGuildData
import dev.simpletimer.extension.getLang
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import java.awt.Color

/**
 * 設定されているゲームシステムの説明画面を作成する
 */
object GameSystemInfoView {
    /**
     * ダイスの説明画面を作成
     * 長い場合は分割して先に送信し、最後の1つを返す
     *
     * @param channel [GuildMessageChannel] 送信するテキストチャンネル
     * @param guild [Guild] 対象のギルド
     */
    fun create(channel: GuildMessageChannel, guild: Guild): MessageEmbed {
        //ギルドのデータを取得
        val guildData = guild.getGuildData()

        //言語のデータ
        val langData = guild.getLang()

        //ギルドのデータから設定されているダイスボットを取得
        val id = guildData.diceBot

        //ダイスボットの詳細を取得
        val gameSystemInfo = BCDiceClient.instance.getGameSystem(id)

        //作成開始
        var builder = EmbedBuilder()
        builder.setTitle(gameSystemInfo.name)
        builder.setDescription(langData.dice.bcDice.diceDescription)
        builder.setColor(Color.BLUE)

        if (gameSystemInfo.helpMessage.length >= 1024) {
            val helpArray = gameSystemInfo.helpMessage.split("\n")
            var index = 0
            val buffer = StringBuilder()
            var isFirst = true

            while (index < helpArray.size) {
                while (index < helpArray.size && buffer.toString().length + helpArray[index].length + 2 <= 1024) {
                    buffer.append("\n")
                    buffer.append(helpArray[index])
                    index++
                }

                if (isFirst) {
                    builder.addField(langData.dice.bcDice.gameCommand, buffer.toString(), false)
                    isFirst = false
                } else {
                    builder.addField(" ", buffer.toString(), false)
                }

                channel.sendMessageEmbeds(builder.build()).queue()
                buffer.clear()

                builder = EmbedBuilder()
                builder.setColor(Color.BLUE)
            }
        } else {
            builder.addField(langData.dice.bcDice.gameCommand, gameSystemInfo.helpMessage, false)
        }

        //システム共通を張る
        if (id != "DiceBot") {
            val defaultGameSystem = BCDiceClient.instance.getGameSystem("DiceBot")
            builder.addField(langData.dice.bcDice.commonCommand, defaultGameSystem.helpMessage, false)
        }

        return builder.build()
    }
}
