package br.com.zup.edu.pixkey.shared.handle.handlers

import br.com.zup.edu.pixkey.shared.exceptions.KeyNotFoundException
import br.com.zup.edu.pixkey.shared.handle.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class KeyNotFoundExceptionHandler: ExceptionHandler<KeyNotFoundException> {
    override fun handle(e: KeyNotFoundException): ExceptionHandler.StatusWithDetails {
        return  ExceptionHandler.StatusWithDetails(Status.NOT_FOUND.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is KeyNotFoundException
    }


}