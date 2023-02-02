package org.hff.miraiomnitrix.handler.core

import org.hff.miraiomnitrix.handler.type.AnyHandler

sealed class HandleChain<T> {

    private val chain = mutableListOf<T>()
    fun handleStart(){

    }
}
