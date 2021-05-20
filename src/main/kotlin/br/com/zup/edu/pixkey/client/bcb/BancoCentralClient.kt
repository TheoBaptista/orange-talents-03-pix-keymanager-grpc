package br.com.zup.edu.pixkey.client.bcb

import br.com.zup.edu.pixkey.client.bcb.dto.CreatePixKeyRequest
import br.com.zup.edu.pixkey.client.bcb.dto.CreatePixKeyResponse
import br.com.zup.edu.pixkey.client.bcb.dto.DeletePixKeyRequest
import br.com.zup.edu.pixkey.client.bcb.dto.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.pix.url}")
interface BancoCentralClient {

    @Post("/api/v1/pix/keys", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun register(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun delete(@PathVariable key: String,@Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>
}