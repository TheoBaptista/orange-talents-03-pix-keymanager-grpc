package br.com.zup.edu.pixkey.client.bcb.dto

import java.time.LocalDateTime

data class DeletePixKeyRequest(
    val key: String,
    val participant: String = "60701190"
)

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)
