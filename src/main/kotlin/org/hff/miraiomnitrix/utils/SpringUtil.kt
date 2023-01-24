package org.hff.miraiomnitrix.utils

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
object SpringUtil : ApplicationContextAware {

    private var context: ApplicationContext? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    fun <T : Any> getBean(beanClass: KClass<out T>) = context?.getBean(beanClass.java)

    fun getBeansWithAnnotation(annotationClass: KClass<out Annotation>): Map<String, Any>? {
        return context?.getBeansWithAnnotation(annotationClass.java)
    }

}