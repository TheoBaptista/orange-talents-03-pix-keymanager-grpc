package br.com.zup.edu.pixkey.client.itau.dto

import br.com.zup.edu.pixkey.Account
import br.com.zup.edu.pixkey.AccountType
import com.fasterxml.jackson.annotation.JsonProperty

data class AccountDetailsResponse(
    @JsonProperty val tipo: AccountType,
    @JsonProperty val instituicao: AccountBankInstitutionResponse,
    @JsonProperty val agencia: String,
    @JsonProperty val numero: String,
    @JsonProperty val titular: AccountOwnerResponse
    ){

    fun toAccount(): Account {
        return Account(institution = instituicao.nome,instituicao.ispb,agencia,numero)
    }



}
