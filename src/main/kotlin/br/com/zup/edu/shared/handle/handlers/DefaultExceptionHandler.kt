package br.com.zup.edu.shared.handle.handlers


import br.com.zup.edu.shared.exceptions.NotPermitedException
import br.com.zup.edu.shared.handle.ExceptionHandler
import br.com.zup.edu.shared.handle.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.validation.ConstraintViolationException

class DefaultExceptionHandler : ExceptionHandler<Exception> {
    override fun handle(e: Exception): StatusWithDetails {

        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is ConstraintViolationException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is NotPermitedException -> Status.PERMISSION_DENIED.withDescription(e.message)

            else -> Status.UNKNOWN
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}