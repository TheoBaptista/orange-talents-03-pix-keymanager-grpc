package br.com.zup.edu.pixkey.load

import br.com.zup.edu.AccountType
import br.com.zup.edu.KeyType
import br.com.zup.edu.LoadKeyResponse
import com.google.protobuf.Timestamp
import java.time.ZoneId

fun convert(pixInfo: KeyPixInfo): LoadKeyResponse {
    return LoadKeyResponse.newBuilder().setClienteId(pixInfo.clientId ?: " ")
        .setPixId(pixInfo.pixId ?: " ")
        .setKey(
            LoadKeyResponse.PixKey.newBuilder().setType(KeyType.valueOf(pixInfo.type.name))
                .setKey(pixInfo.key)
                .setAccount(
                    LoadKeyResponse.PixKey.InfoAccount.newBuilder()
                        .setType(AccountType.valueOf(pixInfo.accountType.name))
                        .setInstitution(pixInfo.account.institution)
                        .setOwnerName(pixInfo.nameOwner)
                        .setCpf(pixInfo.cpf)
                        .setAgency(pixInfo.account.agency)
                        .setAccountNumber(pixInfo.account.numberAccount).build()
                ).setCriadaEm(pixInfo.registerAt.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
        ).build()
}