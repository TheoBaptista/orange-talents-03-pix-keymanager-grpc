package br.com.zup.edu.pixkey.client.itau.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class AccountOwnerResponse(
    @JsonProperty val id: String,
    @JsonProperty val nome: String,
    @JsonProperty val cpf: String
)
