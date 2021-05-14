package br.com.zup.edu.pixkey.shared.handle.handlers

import br.com.zup.edu.pixkey.shared.exceptions.InvalidArgumentException
import br.com.zup.edu.pixkey.shared.handle.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class InvalidArgumentExceptionHandler : ExceptionHandler<InvalidArgumentException> {
    override fun handle(e: InvalidArgumentException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.INVALID_ARGUMENT.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is InvalidArgumentException
    }
}