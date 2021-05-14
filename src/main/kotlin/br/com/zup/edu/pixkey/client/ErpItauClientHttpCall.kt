package br.com.zup.edu.pixkey.client

import br.com.zup.edu.AccountType
import br.com.zup.edu.pixkey.client.dto.AccountDetailsResponse
import br.com.zup.edu.pixkey.shared.exceptions.ClientNotFoundException
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErpItauClientHttpCall(@Inject val erpItauClient: ErpItauClientHttp) {

    fun searchAccountDetails(clientId:String, accountType: AccountType): AccountDetailsResponse {

        try {
            return erpItauClient.searchAccountDetails(clientId, accountType.toString()).body()!!
        }catch (e: HttpClientResponseException){
            throw ClientNotFoundException("Cliente com o id:${clientId} não encontrado!")
        }
    }

}