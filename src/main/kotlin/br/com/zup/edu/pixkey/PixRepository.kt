package br.com.zup.edu.pixkey

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface PixRepository : JpaRepository<Pix, String> {
    fun existsByKeyValue(keyValue : String) : Boolean
}