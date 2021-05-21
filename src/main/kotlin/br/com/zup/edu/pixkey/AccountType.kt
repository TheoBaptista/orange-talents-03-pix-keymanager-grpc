package br.com.zup.edu.pixkey

import br.com.zup.edu.shared.exceptions.InvalidArgumentException

enum class AccountType {

    CONTA_CORRENTE,
    CONTA_POUPANCA;

    companion object{
        fun convert(tipoDaConta :br.com.zup.edu.AccountType): AccountType {
           return when(tipoDaConta){
               br.com.zup.edu.AccountType.CONTA_CORRENTE -> CONTA_CORRENTE
               br.com.zup.edu.AccountType.CONTA_POUPANCA -> CONTA_POUPANCA
                else -> throw InvalidArgumentException(
                    "Tipo de conta inválida"
                )
            }
        }
    }

}