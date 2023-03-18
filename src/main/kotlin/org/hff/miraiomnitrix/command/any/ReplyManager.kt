package org.hff.miraiomnitrix.command.any

import net.mamoe.mirai.event.events.MessageEvent
import org.hff.miraiomnitrix.command.AnyCommand
import org.hff.miraiomnitrix.command.Command
import org.hff.miraiomnitrix.command.CommandResult
import org.hff.miraiomnitrix.command.result
import org.hff.miraiomnitrix.config.PermissionProperties
import org.hff.miraiomnitrix.db.entity.AutoReply
import org.hff.miraiomnitrix.db.entity.ReplyEnum
import org.hff.miraiomnitrix.db.service.AutoReplyService
import org.hff.miraiomnitrix.utils.getImage
import org.hff.miraiomnitrix.utils.getInfo
import javax.management.Query.eq

@Command(name = ["回复管理", "reply"])
class ReplyManager(
    private val permissionProperties: PermissionProperties,
    private val autoReplyService: AutoReplyService
) : AnyCommand {

    override suspend fun execute(args: List<String>, event: MessageEvent): CommandResult? {
        if (!permissionProperties.admin.contains(event.sender.id)) return result("没有权限")
        val message = event.message
        when (args[0]) {
            "info", "get" -> {}

            "add", "添加" -> {
                val type = ReplyEnum.values().find { it.value == args[1].toIntOrNull() } ?: return result("类型错误")
                val keyword = args[2]
                val content = args.getOrNull(3) ?: message.getImage()?.imageId ?: return null
                val autoReply = AutoReply(null, type, keyword, content)
                val save = autoReplyService.save(autoReply)
                if (!save) return result("添加失败")
                return result("添加成功")
            }

            "edit", "修改" -> {
                val id = args.getOrNull(1)?.toIntOrNull() ?: return result("id错误")
                val content = args.getOrNull(2) ?: return result("参数错误")
                val update = autoReplyService.ktUpdate()
                    .set(AutoReply::content, content)
                    .eq(AutoReply::id, id)
                    .update()
                if (!update) return result("修改失败")
                return result("修改成功")
            }

            "del", "删除" -> {
                val id = args.getOrNull(1)?.toIntOrNull() ?: return result("id错误")
                val remove = autoReplyService.removeById(id)
                if (!remove) return result("删除失败")
                return result("删除成功")
            }

            "list", "列表" -> {
                val keyword = args.getOrNull(1)
                val list = autoReplyService.ktQuery()
                    .eq(keyword != null, AutoReply::keyword, keyword)
                    .list()
                return result(list.joinToString("\n") { "${it.id} ${it.type.value} ${it.keyword}" })
            }

            else -> {}
        }
        return null
    }
}
