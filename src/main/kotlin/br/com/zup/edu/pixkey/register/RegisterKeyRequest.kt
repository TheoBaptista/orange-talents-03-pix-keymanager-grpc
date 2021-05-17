package br.com.zup.edu.pixkey.register

import br.com.zup.edu.AccountType
import br.com.zup.edu.KeyType
import br.com.zup.edu.RegisterKeyGrpcRequest
import br.com.zup.edu.pixkey.Account
import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.client.dto.AccountDetailsResponse
import br.com.zup.edu.pixkey.shared.PixKey
import br.com.zup.edu.pixkey.shared.exceptions.InvalidArgumentException
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Introspected
@PixKey
data class RegisterKeyRequest(
    @field:NotBlank val clientId: String,
    @field:Size(max = 77) @field:NotBlank  val keyValue: String,
    @field:NotNull val keyType: KeyType,
    @field:NotNull val accountType: AccountType
) {
    companion object {
        fun convert(request: RegisterKeyGrpcRequest): RegisterKeyRequest {
            return RegisterKeyRequest(
                clientId = request.clientId,
                keyValue = if (request.pixKeyType.equals(KeyType.RANDOM)) UUID.randomUUID()
                    .toString() else request.keyValue,
                keyType = if (request.pixKeyType.equals(KeyType.UNKNOWN_KEY_TYPE))
                           throw InvalidArgumentException("Tipo de chave Pix inválido")
                           else request.pixKeyType,
                accountType = if (request.clientAccountType.equals(AccountType.UNKNOWN_ACCOUNT_TYPE))
                               throw InvalidArgumentException("Tipo de conta inválido")
                               else request.clientAccountType
            )
        }

    }



    fun toModel(accountDetailsResponse: AccountDetailsResponse) : Pix{
        return Pix(keyType = this.keyType,keyValue = this.keyValue, accountType = this.accountType,accountDetailsResponse.toAccount(),
            clientId,accountDetailsResponse.titular.cpf,accountDetailsResponse.titular.nome)
    }

}