package dev.simpletimer.dice.bcdice

import dev.simpletimer.bcdice_client.BCDiceClient
import dev.simpletimer.bcdice_client.GameSystem
import dev.simpletimer.component.button.DiceBotInfoButton
import dev.simpletimer.component.button.DiceBotPageButton
import dev.simpletimer.component.select_menu.DiceBotSelectMenu
import dev.simpletimer.data.enum.DiceMode
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.extension.*
import dev.simpletimer.util.Log
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import java.awt.Color

/**
 * BCDiceのダイスボット選択・説明のDiscord上の画面処理
 */
object GameSystemSelectView {
    //1ページに表示するゲームシステムの数
    private const val PAGE_SIZE = 9

    /**
     * ゲームシステムの一覧をページで分けて取得する
     */
    private fun getPages(): List<ArrayList<GameSystem>> {
        return BCDiceClient.instance.getGameSystems().chunked(PAGE_SIZE).map { ArrayList(it) }
    }

    /**
     * ダイス選択画面のEmbedを作成する
     *
     * @param page [Int]　ページ
     * @param pages ページごとのゲームシステム
     * @return [MessageEmbed] メッセージの埋め込み
     */
    private fun createView(langData: LangData, page: Int, pages: List<ArrayList<GameSystem>>): MessageEmbed? {
        //ページを取得
        val gameSystems = pages.getOrNull(page - 1) ?: return null

        //作成開始
        val builder = EmbedBuilder()
        builder.setTitle(langData.dice.bcDice.botSelect, "https://bcdice.org/systems/")
        builder.setDescription(langData.dice.bcDice.botSelectDescription)
        builder.setColor(Color.BLUE)

        //ページ内のgameSystemを貼り付けていく
        for ((count, gameSystem) in gameSystems.withIndex()) {
            val emoji = (count + 1).emoji
            builder.addField(emoji, "**${gameSystem.name}**", false)
        }

        //ページ数の表示
        builder.setFooter(langData.dice.bcDice.page.langFormat(page, pages.size))

        return builder.build()
    }

    /**
     * ダイス選択画面を送る
     *
     * @param channel [GuildMessageChannel] 対象のチャンネル
     */
    fun openSelectView(channel: GuildMessageChannel) {
        val langData = channel.guild.getLang()
        val pages = getPages()

        //embedを作成
        val embed = createView(langData, 1, pages) ?: return

        //メッセージを送信
        channel.sendMessageEmbeds(embed).setComponents(
            ActionRow.of(DiceBotSelectMenu.createSelectMenu(pages[0], langData)),
            ActionRow.of(
                DiceBotPageButton.BackButton.createButton(0, langData),
                DiceBotPageButton.NextButton.createButton(2, langData)
            )
        ).queue()
    }

    /**
     * ボタンに応答してページを変更する
     *
     * @param event [ButtonInteractionEvent]ボタンのイベント
     */
    fun changePageFromButtonInteraction(event: ButtonInteractionEvent) {
        //ボタンのIDを取得
        val buttonID = event.button.customId ?: return

        //ページを取得
        val page = buttonID.split(":")[1].toInt()

        val pages = getPages()

        //ページの上下限を確認
        if (page < 1 || page > pages.size) {
            //空白を送信
            event.deferReply().queue {
                event.hook.sendEmpty()
            }
            return
        }

        val langData = event.guild!!.getLang()

        //メッセージを更新
        event.message.editMessageEmbeds(createView(langData, page, pages)).queue {
            //コンポーネント類を編集
            event.editComponents(
                ActionRow.of(DiceBotSelectMenu.createSelectMenu(pages[page - 1], langData)),
                ActionRow.of(
                    DiceBotPageButton.BackButton.createButton(page - 1, langData),
                    DiceBotPageButton.NextButton.createButton(page + 1, langData)
                ),
            ).queue()
        }
    }

    /**
     * 選択メニューに応じてBotを選択
     *
     * @param event [IReplyCallback]対象のイベントのコールバック
     * @parm id BotのID
     */
    fun selectFromInteraction(event: IReplyCallback, id: String) {
        //チャンネルを取得
        val channel = event.guildChannel as GuildMessageChannel

        try {
            //ゲームシステムを取得
            val gameSystem = BCDiceClient.instance.getGameSystem(id)

            //言語のデータ
            val langData = channel.guild.getLang()

            //メッセージを送信
            event.hook.sendMessage(
                langData.dice.bcDice.changeDiceBot.langFormat(
                    "**${gameSystem.name}**",
                    gameSystem.id
                )
            )
                .setComponents(ActionRow.of(DiceBotInfoButton.createButton(0, langData))).queue()

            //ギルドのデータを取得
            val guildData = channel.guild.getGuildData()

            //ダイスモードを自動的に変更する
            if (guildData.diceMode == DiceMode.Default) {
                event.hook.sendMessage(langData.dice.changeDiceMode.langFormat("**BCDice**")).queue()
            }

            //データを保存
            guildData.diceBot = gameSystem.id
            guildData.diceMode = DiceMode.BCDice
            channel.guild.setGuildData(guildData)
        } catch (e: Exception) {
            //権限関係が原因の物は排除
            if (e is ErrorResponseException && (e.errorCode == 50001 || e.errorCode == 10008)) {
                return
            }
            //Botが見つからなかった時
            if (e is IllegalArgumentException) {
                event.hook.sendMessage(event.guild!!.getLang().dice.bcDice.unknownBot).queue()
                return
            }
            Log.sendLog(e.stackTraceToString(), true)
        }
    }
}
