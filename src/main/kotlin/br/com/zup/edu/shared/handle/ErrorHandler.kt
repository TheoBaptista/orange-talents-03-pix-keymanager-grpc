package br.com.zup.edu.shared.handle

import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE

@Target(
    CLASS,
    TYPE,
)
@Type(ExceptionHandlerInterceptor::class)
@Retention(RUNTIME)
@Around
annotation class ErrorHandler()