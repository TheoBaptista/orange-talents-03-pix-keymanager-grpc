package br.com.zup.edu.pixkey.client.bcb.dto

import br.com.zup.edu.pixkey.Account
import br.com.zup.edu.pixkey.Institutions
import br.com.zup.edu.pixkey.KeyTypePix
import br.com.zup.edu.pixkey.load.KeyPixInfo
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyTypePix: String,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime,
){

    fun toModel(): KeyPixInfo{
        return KeyPixInfo(
            type = KeyTypePix.valueOf(this.keyTypePix),
            key = this.key,
            accountType = when(this.bankAccount.accountType){
                AccountType.CACC -> br.com.zup.edu.pixkey.AccountType.CONTA_CORRENTE
                AccountType.SVGS -> br.com.zup.edu.pixkey.AccountType.CONTA_POUPANCA
            },
            account = Account(
                institution = Institutions.nome(bankAccount.participant),
                isbp = bankAccount.participant,
                numberAccount = bankAccount.accountNumber,
                agency = bankAccount.branch,
                ),
            nameOwner = owner.name,
            cpf = owner.taxIdNumber
        )
    }


}
