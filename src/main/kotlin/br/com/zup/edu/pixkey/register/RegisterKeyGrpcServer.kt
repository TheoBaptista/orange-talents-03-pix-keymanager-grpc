package br.com.zup.edu.pixkey.register

import br.com.zup.edu.RegisterKeyGrpcRequest
import br.com.zup.edu.RegisterKeyGrpcResponse
import br.com.zup.edu.RegisterKeyGrpcServiceGrpc
import br.com.zup.edu.shared.handle.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton


@ErrorHandler
@Singleton
class RegisterKeyGrpcServer(
    @Inject private val service: RegisterKeyService,
) : RegisterKeyGrpcServiceGrpc.RegisterKeyGrpcServiceImplBase() {


    override fun register(
        request: RegisterKeyGrpcRequest,
        responseObserver: StreamObserver<RegisterKeyGrpcResponse>
    ) {
        val requestDto = RegisterKeyRequest.convert(request)
        val novaChave = service.registerKey(requestDto)



        responseObserver.onNext(
            RegisterKeyGrpcResponse.newBuilder().setPixId(novaChave.id).setClientId(novaChave.clientId).build()
        )
        responseObserver.onCompleted()


    }
}



