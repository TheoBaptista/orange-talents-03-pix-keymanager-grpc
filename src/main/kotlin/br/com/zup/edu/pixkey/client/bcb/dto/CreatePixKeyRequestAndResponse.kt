package br.com.zup.edu.pixkey.client.bcb.dto

import br.com.zup.edu.KeyType
import br.com.zup.edu.pixkey.Pix
import java.time.LocalDateTime
import javax.validation.Valid
import br.com.zup.edu.AccountType as TipoDaConta

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
)

data class CreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

data class BankAccount(
    val participant: String = "60701190",
    val branch: String = "0001",
    val accountNumber: String,
    val accountType: AccountType,
)

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String,
)

enum class AccountType() {
    CACC,
    SVGS;

    companion object {
        fun convert(tipoDaConta: TipoDaConta): AccountType {
            return when (tipoDaConta) {
                TipoDaConta.CONTA_CORRENTE -> CACC
                TipoDaConta.CONTA_POUPANCA -> SVGS
                else -> throw IllegalArgumentException("Tipo de conta inválido")
            }
        }
    }

}

enum class OwnerType {
    NATURAL_PERSON,
}

fun createBcBPixRequest(@Valid chavePix: Pix): CreatePixKeyRequest {
    return CreatePixKeyRequest(
        chavePix.keyType, chavePix.keyValue, BankAccount(accountNumber = chavePix.account.numberAccount,
           accountType = AccountType.convert(chavePix.accountType)
        ), Owner(OwnerType.NATURAL_PERSON, chavePix.name, chavePix.cpf)
    )
}