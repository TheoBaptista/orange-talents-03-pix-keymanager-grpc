package br.com.zup.edu.pixkey

import br.com.zup.edu.AccountType
import br.com.zup.edu.KeyType
import org.hibernate.annotations.GenericGenerator
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class Pix(
    @field:NotNull @field:Enumerated(EnumType.STRING) val keyType: KeyType,
    @field:Column(unique = true) val keyValue: String,
    @field:NotNull @field:Enumerated(EnumType.STRING) val accountType: AccountType,
    @field:Embedded val account: Account,
    @field:NotBlank val clientId: String,
    @field:NotBlank val cpf: String,
    @field:NotBlank val name:String
) {
    @Id @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID",strategy = "org.hibernate.id.UUIDGenerator")
     val id: String? = null // ver se tem outra alternativa
}