package br.com.zup.edu.pixkey.load

import br.com.zup.edu.pixkey.Account
import br.com.zup.edu.pixkey.AccountType
import br.com.zup.edu.pixkey.KeyTypePix
import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.client.bcb.dto.Owner
import java.time.LocalDateTime

data class KeyPixInfo(
    val pixId: String? = null,
    val clientId: String? = null,
    val type: KeyTypePix,
    val key: String,
    val accountType: AccountType,
    val account: Account,
    val registerAt: LocalDateTime = LocalDateTime.now(),
    val nameOwner: String,
    val cpf: String,
){
    companion object{
        fun of(pix: Pix) : KeyPixInfo {
            return KeyPixInfo(
                    pixId = pix.id,
                    clientId = pix.clientId,
                    type = pix.keyType,
                    key = pix.keyValue,
                    accountType = pix.accountType,
                    account = pix.account,
                    registerAt = pix.createdAt,
                    nameOwner = pix.name,
                    cpf = pix.cpf
                    )
        }
    }
}
