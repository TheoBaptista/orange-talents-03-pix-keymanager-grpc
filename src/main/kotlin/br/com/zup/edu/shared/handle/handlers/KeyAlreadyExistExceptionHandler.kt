package br.com.zup.edu.shared.handle.handlers

import br.com.zup.edu.shared.exceptions.KeyAlreadyExistException
import br.com.zup.edu.shared.handle.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class KeyAlreadyExistExceptionHandler : ExceptionHandler<KeyAlreadyExistException> {
    override fun handle(e: KeyAlreadyExistException): ExceptionHandler.StatusWithDetails {
       return  ExceptionHandler.StatusWithDetails(Status.ALREADY_EXISTS.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is KeyAlreadyExistException
    }
}