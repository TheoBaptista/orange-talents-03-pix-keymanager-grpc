package br.com.zup.edu.pixkey.client

import br.com.zup.edu.AccountType
import br.com.zup.edu.pixkey.client.dto.AccountDetailsResponse
import br.com.zup.edu.pixkey.shared.exceptions.ClientNotFoundException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.retry.intercept.RecoveryInterceptor
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErpItauClientHttpCall(@Inject val erpItauClient: ErpItauClientHttp) {

    fun searchAccountDetails(clientId:String, accountType: AccountType): AccountDetailsResponse {

        try { //deve haver um teste
            return erpItauClient.searchAccountDetails(clientId, accountType.toString()).body()!!
        } catch (e: HttpClientResponseException) { //deve haver um teste
            throw ClientNotFoundException("Cliente com o id:${clientId} não encontrado!")
        }
    }

}