package org.hff.miraiomnitrix.utils

import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
object SpringUtil : ApplicationContextAware {

    private var context: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    fun getBean(T : Any): Any? {
        return context?.getBean(T)
    }

}