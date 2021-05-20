package br.com.zup.edu.pixkey.client.itau.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class AccountBankInstitutionResponse(
    @JsonProperty val nome: String,
    @JsonProperty val ispb: String
)
