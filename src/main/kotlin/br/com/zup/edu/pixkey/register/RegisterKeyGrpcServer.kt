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
import javax.validation.Valid


@Singleton
@Validated
@ErrorHandler
class RegisterKeyGrpcServer(
    @Inject val erpItauClientHttpCall: ErpItauClientHttpCall,
    @Inject val pixRepository: PixRepository
) : RegisterKeyGrpcServiceGrpc.RegisterKeyGrpcServiceImplBase() {


    override fun register(
        request: RegisterKeyGrpcRequest,
        responseObserver: StreamObserver<RegisterKeyGrpcResponse>
    ) {

        val novaChave = validateAndRegister(RegisterKeyRequest.convert(request))
        responseObserver.onNext(RegisterKeyGrpcResponse.newBuilder().setPixId(novaChave.id).build())
        responseObserver.onCompleted()
    }
    // cuidar com as restricoes de visibilidade com o valid nao usar private.
    fun validateAndRegister(@Valid request: RegisterKeyRequest): Pix {

        if(pixRepository.existsByKeyValue(request.keyValue)) throw KeyAlreadyExistException("Chave Pix já existe : ${request.keyValue}")

        return pixRepository.save(
            request.toModel(
                erpItauClientHttpCall.searchAccountDetails(
                    request.clientId,
                    request.accountType
                )
            )
        )
    }

}



