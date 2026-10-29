package dev.simpletimer.component.select_menu

import dev.simpletimer.bcdice_client.GameSystem
import dev.simpletimer.data.lang.lang_data.LangData
import dev.simpletimer.dice.bcdice.GameSystemSelectView
import dev.simpletimer.extension.emoji
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu

/**
 * DiceBotを選択するセレクトメニュー
 *
 */
object DiceBotSelectMenu : SelectMenuManager.SelectMenu<ArrayList<GameSystem>>("dice_bot") {
    override fun run(event: StringSelectInteractionEvent) {
        //選択されているものを取得
        val id = event.selectedOptions[0]?.value ?: return

        //GameSystemSelectViewに処理を任せる
        GameSystemSelectView.selectFromInteraction(event, id)
    }

    override fun createSelectMenu(data: ArrayList<GameSystem>, langData: LangData): SelectMenu {
        //メニューを作成
        val selectionMenu = StringSelectMenu.create(name)
        selectionMenu.placeholder = langData.component.select.placeholder
        selectionMenu.setRequiredRange(1, 1)
        //GameManagerを選択肢に
        data.withIndex().forEach {
            selectionMenu.addOption(it.value.name, it.value.id, Emoji.fromUnicode((it.index + 1).emoji))
        }
        //作成して返す
        return selectionMenu.build()
    }
}