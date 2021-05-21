package br.com.zup.edu.pixkey.load

import br.com.zup.edu.pixkey.PixRepository
import br.com.zup.edu.pixkey.client.bcb.BancoCentralClientCall
import br.com.zup.edu.shared.exceptions.KeyNotFoundException
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filter {

    abstract fun filter(repository: PixRepository, bancoCentralClientCall: BancoCentralClientCall): KeyPixInfo

    @Introspected
    data class WhitPixId(
        @field:NotBlank val clientId: String,
        @field:NotBlank val pixId: String
    ) : Filter() {
        override fun filter(repository: PixRepository, bancoCentralClientCall: BancoCentralClientCall): KeyPixInfo {
            return repository.findById(pixId).filter { it.clientId == clientId }.map(KeyPixInfo::of).orElseThrow{ KeyNotFoundException("Chave pix não encontrada") }
        }
    }

    @Introspected
    data class WhitKey(@field:NotBlank @Size(max = 77) val key: String): Filter(){
        override fun filter(repository: PixRepository, bancoCentralClientCall: BancoCentralClientCall): KeyPixInfo {
             val  keyOptinal = repository.findByKeyValue(key)
             if (keyOptinal.isEmpty){
                  return bancoCentralClientCall.findPixInBcb(key)
             }
              return KeyPixInfo.of(pix = keyOptinal.get())
            }
        }

    @Introspected
    class Invalided(): Filter(){
        override fun filter(repository: PixRepository, bancoCentralClientCall: BancoCentralClientCall): KeyPixInfo {
         throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }


    }

