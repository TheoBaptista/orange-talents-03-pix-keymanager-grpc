package br.com.zup.edu.pixkey.client.itau

import br.com.zup.edu.pixkey.client.itau.dto.AccountDetailsResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("\${itau.contas.url}") // colocar variavel de ambiente
interface ErpItauClient {

    @Get("/api/v1/clientes/{id}/contas") // sempre importar o HttpResponse do micronaut senao vai dar erro
    fun searchAccountDetails(@PathVariable id: String, @QueryValue tipo: String): HttpResponse<AccountDetailsResponse>

}