package org.hff.miraiomnitrix.utils

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
object SpringUtil : ApplicationContextAware {

    private var context: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    fun <T> getBean(beanClass: Class<out T>) = context?.getBean(beanClass)

    fun getBeansWithAnnotation(annotationClass: Class<out Annotation>): Map<String, Any>? {
        return context?.getBeansWithAnnotation(annotationClass)
    }

}