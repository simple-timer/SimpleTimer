package dev.simpletimer.component.button

import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.dice.bcdice.GameSystemSelectView
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.components.buttons.Button

/**
 * ダイスボット選択時にページを変えるボタン
 *
 */
class DiceBotPageButton {
    /**
     * 次ページ
     *
     */
    object NextButton : ButtonManager.Button<Int>("page_next", deferReply = false) {
        override fun run(event: ButtonInteractionEvent) {
            //GameSystemSelectViewの処理に任せる
            GameSystemSelectView.changePageFromButtonInteraction(event)
        }

        override fun createButton(data: Int, langData: LangData): Button {
            //ボタンを作成して返す
            return Button.primary("${name}:${data}", Emoji.fromUnicode("➡️"))
        }
    }

    /**
     * 前ページ
     *
     */
    object BackButton : ButtonManager.Button<Int>("page_back", deferReply = false) {
        override fun run(event: ButtonInteractionEvent) {
            //GameSystemSelectViewの処理に任せる
            GameSystemSelectView.changePageFromButtonInteraction(event)
        }

        override fun createButton(data: Int, langData: LangData): Button {
            //ボタンを作成して返す
            return Button.primary("${name}:${data}", Emoji.fromUnicode("⬅️"))
        }
    }
}