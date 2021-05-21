package br.com.zup.edu.pixkey.shared

import br.com.zup.edu.pixkey.KeyTypePix
import br.com.zup.edu.pixkey.register.RegisterKeyRequest
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS,AnnotationTarget.FUNCTION,AnnotationTarget.CONSTRUCTOR,AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PixValidator::class])
annotation class PixKey(
    val message: String = "Chave pix inválida",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class PixValidator : ConstraintValidator<PixKey, RegisterKeyRequest> {
    override fun isValid(
        value: RegisterKeyRequest?,
        annotationMetadata: AnnotationValue<PixKey>,
        context: ConstraintValidatorContext
    ): Boolean {


        if (value?.keyType == null) {
            return false
        }


        return when (value.keyType) {
             KeyTypePix.CPF -> value.keyValue.matches("^[0-9]{11}\$".toRegex())
             KeyTypePix.CELLPHONE -> value.keyValue.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
             KeyTypePix.EMAIL -> value.keyValue.matches("^[A-Za-z0-9+_.-]+@(.+)\$".toRegex())
             KeyTypePix.RANDOM -> true
        }
    }


}

