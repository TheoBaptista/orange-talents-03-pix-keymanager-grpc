package br.com.zup.edu.pixkey.register

import br.com.zup.edu.KeyType
import br.com.zup.edu.RegisterKeyGrpcRequest
import br.com.zup.edu.pixkey.AccountType
import br.com.zup.edu.pixkey.KeyTypePix
import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.client.itau.dto.AccountDetailsResponse
import br.com.zup.edu.pixkey.shared.PixKey
import br.com.zup.edu.shared.exceptions.InvalidArgumentException
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Introspected
@PixKey
data class RegisterKeyRequest(
    @field:NotBlank val clientId: String,
    @field:Size(max = 77) @field:NotBlank val keyValue: String,
    @field:NotNull val keyType: KeyTypePix,
    @field:NotNull val accountType: AccountType
) {
    companion object {
        fun convert(request: RegisterKeyGrpcRequest): RegisterKeyRequest {
            return RegisterKeyRequest(
                clientId = request.clientId,
                keyValue = if (request.keyType.equals(KeyType.RANDOM)) KeyTypePix.RANDOM.toString()
                else request.keyValue,
                keyType = if (request.keyType.equals(KeyType.UNKNOWN_KEY_TYPE))
                    throw InvalidArgumentException("Tipo de chave Pix inválido")
                else KeyTypePix.convert(request.keyType),
                accountType = if (request.clientAccountType.equals(br.com.zup.edu.AccountType.UNKNOWN_ACCOUNT_TYPE)) throw InvalidArgumentException(
                    "Tipo de conta inválida"
                ) else AccountType.convert(request.clientAccountType)
            )
        }

    }

    fun toModel(accountDetailsResponse: AccountDetailsResponse): Pix {
        return Pix(
            keyType = this.keyType,
            keyValue = this.keyValue,
            accountType = this.accountType,
            accountDetailsResponse.toAccount(),
            clientId,
            accountDetailsResponse.titular.cpf,
            accountDetailsResponse.titular.nome
        )
    }

}