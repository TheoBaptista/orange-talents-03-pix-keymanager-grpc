package br.com.zup.edu.pixkey.load

import br.com.zup.edu.LoadKeyGrpcServiceGrpc
import br.com.zup.edu.LoadKeyRequest
import br.com.zup.edu.LoadKeyResponse
import br.com.zup.edu.pixkey.PixRepository
import br.com.zup.edu.pixkey.client.bcb.BancoCentralClientCall
import br.com.zup.edu.shared.handle.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
@ErrorHandler
class LoadKeyGrpcServer(
    @Inject private val pixRepository: PixRepository,
    @Inject private val bancoCentralClientCall: BancoCentralClientCall,
    @Inject private val validator: Validator
) : LoadKeyGrpcServiceGrpc.LoadKeyGrpcServiceImplBase() {

    override fun load(request: LoadKeyRequest, responseObserver: StreamObserver<LoadKeyResponse>) {
        val filter = request.toModel(validator)
        val pixInfo = filter.filter(repository = pixRepository, bancoCentralClientCall = bancoCentralClientCall)

        responseObserver.onNext(convert(pixInfo))
        responseObserver.onCompleted()
    }
}