package com.whatever.caramel.common.global.annotation

/**
 * Swagger-Ui Api의 인증 버튼을 비활성화하는 Annotation
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DisableSwaggerAuthButton
