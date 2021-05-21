package br.com.zup.edu.pixkey.client.bcb.dto

import br.com.zup.edu.pixkey.KeyTypePix
import br.com.zup.edu.pixkey.Pix
import java.time.LocalDateTime
import javax.validation.Valid

data class CreatePixKeyRequest(
    val keyType: KeyTypePix,
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
        fun convert(tipoDaConta: br.com.zup.edu.pixkey.AccountType): AccountType {
            return when (tipoDaConta) {
                br.com.zup.edu.pixkey.AccountType.CONTA_CORRENTE -> CACC
                br.com.zup.edu.pixkey.AccountType.CONTA_POUPANCA -> SVGS
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