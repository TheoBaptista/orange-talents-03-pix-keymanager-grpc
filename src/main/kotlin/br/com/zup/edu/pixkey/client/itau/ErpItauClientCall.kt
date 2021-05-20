package br.com.zup.edu.pixkey.client.itau

import br.com.zup.edu.AccountType
import br.com.zup.edu.pixkey.client.itau.dto.AccountDetailsResponse
import br.com.zup.edu.shared.exceptions.ClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErpItauClientCall(@Inject val erpItauClient: ErpItauClient) {

    fun searchAccountDetails(clientId:String, accountType: AccountType): AccountDetailsResponse {

        try { //deve haver um teste
            return erpItauClient.searchAccountDetails(clientId, accountType.toString()).body()!!
        } catch (e: HttpClientResponseException) { //deve haver um teste
            throw ClientNotFoundException("Cliente com o id:${clientId} não encontrado!")
        }
    }

}