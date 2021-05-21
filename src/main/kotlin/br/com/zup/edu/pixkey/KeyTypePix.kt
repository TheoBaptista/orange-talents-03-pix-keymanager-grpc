package br.com.zup.edu.pixkey

import br.com.zup.edu.KeyType

enum class KeyTypePix {

    CPF,
    CELLPHONE,
    EMAIL,
    RANDOM;

    companion object{
        fun convert(keyType: KeyType): KeyTypePix {
            return when(keyType){
                KeyType.RANDOM ->  RANDOM
                KeyType.EMAIL -> EMAIL
                KeyType.CELLPHONE -> CELLPHONE
                KeyType.CPF -> CPF
                else -> throw IllegalStateException("Tipo de chave Pix inválida")
            }
        }
    }

}