package br.com.zup.edu.pixkey

import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class Pix(
    @field:NotNull @field:Enumerated(EnumType.STRING) @Column(nullable = false) val keyType: KeyTypePix,
    @field:Column(unique = true, nullable = false) var keyValue: String, // tratar possivel exceção
    @field:NotNull @field:Enumerated(EnumType.STRING) @Column(nullable = false) val accountType: AccountType,
    @field:Embedded @field:Valid val account: Account,
    @field:NotBlank @Column(nullable = false) val clientId: String,
    @field:NotBlank @Column(nullable = false) val cpf: String,
    @field:NotBlank @Column(nullable = false) val name: String
) {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    val id: String? = null // ver se tem outra alternativa

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    fun updateRandomPixKey(randomUUID: String): Boolean{
        if(this.keyType== KeyTypePix.RANDOM){
            this.keyValue=randomUUID
            return true
        }
        return false
    }

    override fun toString(): String {
        return "Pix(keyType=$keyType, keyValue='$keyValue', clientId='$clientId', name='$name', id=$id, createdAt=$createdAt)"
    }



}