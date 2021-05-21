package br.com.zup.edu.pixkey

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface PixRepository : JpaRepository<Pix, String> {
    fun existsByKeyValue(keyValue: String): Boolean

    fun findByIdAndClientId(keyValue: String, clientId: String): Pix?

    fun findByKeyValue(keyValue: String): Optional<Pix>

}