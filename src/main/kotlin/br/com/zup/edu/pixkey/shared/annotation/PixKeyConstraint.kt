package br.com.zup.edu.pixkey.shared

import br.com.zup.edu.KeyType
import br.com.zup.edu.pixkey.register.RegisterKeyRequest
import br.com.zup.edu.pixkey.shared.exceptions.InvalidArgumentException
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
    val message: String = "chave pix inválida",
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

        // deve ter um teste aqui tambem
        if (value?.keyType == null) {
            return false
        }

       // para cada opção do when deve ter um teste
       val result = when (value.keyType) {
            KeyType.CPF -> value.keyValue.matches("^[0-9]{11}\$".toRegex())
            KeyType.CELLPHONE -> value.keyValue.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
            KeyType.EMAIL -> value.keyValue.matches("^[A-Za-z0-9+_.-]+@(.+)\$".toRegex())
            KeyType.RANDOM -> true
            else -> throw InvalidArgumentException("O tipo da chave é inválido")
        }


        // deve ter um teste muito cuidado aqui!!!!
        if(!result) throw InvalidArgumentException("Chave Pix inválida ${value.keyType} : ${value.keyValue}")

        return result
    }


}

