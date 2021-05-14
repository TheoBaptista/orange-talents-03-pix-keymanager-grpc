package br.com.zup.edu.pixkey.shared.handle.handlers

import br.com.zup.edu.pixkey.shared.exceptions.ClientNotFoundException
import br.com.zup.edu.pixkey.shared.handle.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ClientNotFoundExceptionHandler : ExceptionHandler<ClientNotFoundException> {
    override fun handle(e: ClientNotFoundException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(Status.NOT_FOUND.withDescription(e.message).withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ClientNotFoundException
    }
}