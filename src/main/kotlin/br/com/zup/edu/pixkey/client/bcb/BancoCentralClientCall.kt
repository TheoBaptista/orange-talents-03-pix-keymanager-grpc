package br.com.zup.edu.pixkey.client.bcb

import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.client.bcb.dto.DeletePixKeyRequest
import br.com.zup.edu.pixkey.client.bcb.dto.createBcBPixRequest
import br.com.zup.edu.pixkey.load.KeyPixInfo
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

    fun findPixInBcb(key: String): KeyPixInfo {
        try {
            val response = bancoCentralClient.findByKey(key)
            if (response.status().equals(HttpStatus.NOT_FOUND)) {
                throw KeyNotFoundException("Chave pix não encontrada!")
            }
            return response.body()?.toModel() ?: throw IllegalArgumentException("Erro ao consultar o Banco Central do Brasil")
        } catch (e: HttpClientResponseException) {
            throw IllegalStateException("Erro ao consultar o Banco Central do Brasil")
        }
    }
}