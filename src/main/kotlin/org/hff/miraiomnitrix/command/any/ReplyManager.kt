package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.db.entity.AutoReply
import org.hff.miraiomnitrix.db.entity.ReplyEnum
import org.hff.miraiomnitrix.db.service.AutoReplyService
import org.hff.miraiomnitrix.utils.getImage

@Command(name = ["回复管理", "reply"])
class ReplyManager(
    private val permissionProperties: PermissionProperties,
    private val autoReplyService: AutoReplyService
) : AnyCommand {

    override suspend fun execute(args: List<String>, event: MessageEvent): Message? {
        if (!permissionProperties.admin.contains(event.sender.id)) return "没有权限".toPlainText()

        val message = event.message

        when (args[0]) {
            "info", "get" -> {}

            "add", "添加" -> {
                val type =
                    ReplyEnum.values().find { it.value == args[1].toIntOrNull() } ?: return "类型错误".toPlainText()
                val keyword = args[2]
                val content = args.getOrNull(3) ?: message.getImage()?.imageId ?: return null
                val autoReply = AutoReply(null, type, keyword, content)
                val save = autoReplyService.save(autoReply)
                return (if (save) "添加成功" else "添加失败").toPlainText()
            }

            "edit", "修改" -> {
                val id = args.getOrNull(1)?.toIntOrNull() ?: return "id错误".toPlainText()
                val content = args.getOrNull(2) ?: return "参数错误".toPlainText()
                val update = autoReplyService.ktUpdate()
                    .set(AutoReply::content, content)
                    .eq(AutoReply::id, id)
                    .update()
                return (if (update) "修改成功" else "修改失败").toPlainText()
            }

            "del", "删除" -> {
                val id = args.getOrNull(1)?.toIntOrNull() ?: return "id错误".toPlainText()
                val remove = autoReplyService.removeById(id)
                return (if (remove) "删除成功" else "删除失败").toPlainText()
            }

            "list", "列表" -> {
                val keyword = args.getOrNull(1)
                val list = autoReplyService.ktQuery()
                    .eq(keyword != null, AutoReply::keyword, keyword)
                    .list()
                return list.joinToString("\n") { "${it.id} ${it.type.value} ${it.keyword}" }.toPlainText()
            }

            else -> {}
        }
        return null
    }
}
