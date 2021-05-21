package br.com.zup.edu.pixkey.register

import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.PixRepository
import br.com.zup.edu.pixkey.client.bcb.BancoCentralClient
import br.com.zup.edu.pixkey.client.bcb.BancoCentralClientCall
import br.com.zup.edu.pixkey.client.bcb.dto.CreatePixKeyRequest
import br.com.zup.edu.pixkey.client.bcb.dto.createBcBPixRequest
import br.com.zup.edu.pixkey.client.itau.ErpItauClientCall
import br.com.zup.edu.shared.exceptions.KeyAlreadyExistException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Singleton
@Validated
class RegisterKeyService(
    @Inject val erpItauClientCall: ErpItauClientCall,
    @Inject val pixRepository: PixRepository,
    @Inject val bancoCentralClientCall: BancoCentralClientCall
) {
    @Transactional
    fun registerKey(@Valid request: RegisterKeyRequest): Pix {

        if (pixRepository.existsByKeyValue(request.keyValue)) throw KeyAlreadyExistException("A chave pix ${request.keyValue} Já existe!")


        val chaveCriada = pixRepository.save(
            request.toModel(
                erpItauClientCall.searchAccountDetails(
                    request.clientId,
                    request.accountType
                )
            )
        )
        bancoCentralClientCall.createKeyPixBCB(chaveCriada)
        return chaveCriada
    }

}