package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.common.getImage
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.db.entity.AutoReply
import org.hff.miraiomnitrix.db.entity.ReplyEnum
import org.hff.miraiomnitrix.db.service.AutoReplyService
import org.hff.miraiomnitrix.utils.toImage

@Command(name = ["回复管理", "reply"])
class ReplyManager(
    private val permissionProperties: PermissionProperties,
    private val autoReplyService: AutoReplyService
) : AnyCommand {

    override suspend fun MessageEvent.execute(args: List<String>): Message? {
        if (!permissionProperties.admin.contains(sender.id)) return "没有权限".toPlainText()

        return when (args[0]) {
            "info", "get" -> {
                val num = args.getOrNull(1)?.toIntOrNull() ?: return "参数错误".toPlainText()
                val reply = autoReplyService.getById(num)
                if (reply.type == ReplyEnum.Text || reply.type == ReplyEnum.Reply) reply.content.toPlainText()
                else reply.content.toImage()
            }

            "add", "添加" -> {
                val type =
                    ReplyEnum.values().find { it.value == args[1].toIntOrNull() } ?: return "类型错误".toPlainText()
                val keyword = args[2]
                val content = args.getOrNull(3) ?: message.getImage()?.imageId ?: return null
                val autoReply = AutoReply(null, type, keyword, content)
                val save = autoReplyService.save(autoReply)
                (if (save) "添加成功" else "添加失败").toPlainText()
            }

            "edit", "修改" -> {
                val id = args.getOrNull(1)?.toIntOrNull() ?: return "id错误".toPlainText()
                val content = args.getOrNull(2) ?: return "参数错误".toPlainText()
                val update = autoReplyService.ktUpdate()
                    .set(AutoReply::content, content)
                    .eq(AutoReply::id, id)
                    .update()
                (if (update) "修改成功" else "修改失败").toPlainText()
            }

            "del", "删除" -> {
                val id = args.getOrNull(1)?.toIntOrNull() ?: return "id错误".toPlainText()
                val remove = autoReplyService.removeById(id)
                (if (remove) "删除成功" else "删除失败").toPlainText()
            }

            "list", "列表" -> {
                val keyword = args.getOrNull(1)
                val list = autoReplyService.ktQuery()
                    .eq(keyword != null, AutoReply::keyword, keyword)
                    .list()
                list.joinToString("\n") { "${it.id} ${it.type.value} ${it.keyword}" }.toPlainText()
            }

            else -> null
        }
    }
}
