package br.com.zup.edu.pixkey.load

import br.com.zup.edu.LoadKeyRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun LoadKeyRequest.toModel(validator: Validator): Filter {

    val filter = when(filterCase!!){
        LoadKeyRequest.FilterCase.PIXID -> pixId.let {
            Filter.WhitPixId(clientId = it.clientId,pixId = it.pixId)
        }
        LoadKeyRequest.FilterCase.PIXKEY -> Filter.WhitKey(key = pixKey)
        LoadKeyRequest.FilterCase.FILTER_NOT_SET -> Filter.Invalided()
    }

    val violations = validator.validate(filter)
    if (violations.isNotEmpty()){
        throw ConstraintViolationException(violations)
    }
    return filter
}