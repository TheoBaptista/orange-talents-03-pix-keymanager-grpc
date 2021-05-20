package br.com.zup.edu.pixkey.client.bcb

import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.client.bcb.dto.DeletePixKeyRequest
import br.com.zup.edu.pixkey.client.bcb.dto.createBcBPixRequest
import br.com.zup.edu.shared.exceptions.KeyAlreadyExistException
import br.com.zup.edu.shared.exceptions.KeyNotFoundException
import br.com.zup.edu.shared.exceptions.NotPermitedException
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BancoCentralClientCall(@Inject val bancoCentralClient: BancoCentralClient) {

    fun createKeyPixBCB(pix: Pix) {
        try {
            val response = bancoCentralClient.register(createBcBPixRequest(pix))
            pix.updateRandomPixKey(response.body()!!.key)

        } catch (e: HttpClientResponseException) {
            throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")
        }
    }


    fun deleteKeyPixBCB(pix: Pix): Boolean {
        try {
            bancoCentralClient.delete(pix.keyValue, DeletePixKeyRequest(key = pix.keyValue))
            return true
        } catch (e: HttpClientResponseException) {
            throw IllegalStateException("Erro ao deletar a chave Pix no Banco Central do Brasil (BCB)")
        }

    }
}