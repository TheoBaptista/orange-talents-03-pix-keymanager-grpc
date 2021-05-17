package br.com.zup.edu.pixkey.shared.handle.handlers


import br.com.zup.edu.pixkey.shared.exceptions.KeyAlreadyExistException
import br.com.zup.edu.pixkey.shared.handle.ExceptionHandler
import io.grpc.Status
import br.com.zup.edu.pixkey.shared.handle.ExceptionHandler.StatusWithDetails
import javax.validation.ConstraintViolationException

class DefaultExceptionHandler : ExceptionHandler<Exception> {
    override fun handle(e: Exception): StatusWithDetails {

        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is KeyAlreadyExistException -> Status.ALREADY_EXISTS.withDescription((e.message))

            else -> Status.UNKNOWN
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}