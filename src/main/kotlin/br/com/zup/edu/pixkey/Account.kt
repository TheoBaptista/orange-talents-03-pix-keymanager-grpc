package br.com.zup.edu.pixkey

import io.micronaut.data.annotation.Embeddable

@Embeddable
data class Account(
    val institution: String = "",
    val isbp: String = "",
    val agency: String = "",
    val numberAccount: String =""
){
}
