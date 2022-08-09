package org.hff.miraiomnitrix.command

import org.hff.miraiomnitrix.command.any.AnyCommand
import org.reflections.Reflections

object CommandManager {
    val anyCommands = mapOf<String, AnyCommand>()

    init {
        val reflections = Reflections("org.hff.miraiomnitrix.command.any")
        val anyCommandClasses = reflections.getSubTypesOf(AnyCommand::class.java)
        print(anyCommandClasses.size)
    }
}
