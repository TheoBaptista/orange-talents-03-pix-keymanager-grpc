package br.com.zup.edu.pixkey.register

import br.com.zup.edu.RegisterKeyGrpcRequest
import br.com.zup.edu.RegisterKeyGrpcResponse
import br.com.zup.edu.RegisterKeyGrpcServiceGrpc
import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.PixRepository
import br.com.zup.edu.pixkey.client.ErpItauClientHttpCall
import br.com.zup.edu.pixkey.shared.exceptions.KeyAlreadyExistException
import br.com.zup.edu.pixkey.shared.handle.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid


@Singleton
@ErrorHandler
@Validated
class RegisterKeyGrpcServer(
    @Inject val erpItauClientHttpCall: ErpItauClientHttpCall,
    @Inject val pixRepository: PixRepository
) : RegisterKeyGrpcServiceGrpc.RegisterKeyGrpcServiceImplBase() {


    override fun register(
        request: RegisterKeyGrpcRequest,
        response: StreamObserver<RegisterKeyGrpcResponse>
    ) {

        val requestDto = RegisterKeyRequest.convert(request)
        val novaChave = registerKey(validate(requestDto))
        response.onNext(
            RegisterKeyGrpcResponse.newBuilder().setPixId(novaChave.id).setClientId(novaChave.clientId).build()
        )
        response.onCompleted()



    }

    @Transactional
    private fun registerKey(request: RegisterKeyRequest,): Pix {

        if (pixRepository.existsByKeyValue(request.keyValue)) throw KeyAlreadyExistException("A chave pix ${request.keyValue} Já existe!")

        return pixRepository.save(
            request.toModel(
                erpItauClientHttpCall.searchAccountDetails(
                    request.clientId,
                    request.accountType
                )
            )
        )
    }

    fun validate(@Valid registerKeyRequest: RegisterKeyRequest): RegisterKeyRequest {
        return registerKeyRequest
    }

}



