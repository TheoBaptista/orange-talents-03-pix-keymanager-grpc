package br.com.zup.edu.pixkey.delete

import br.com.zup.edu.DeleteKeyGrpcRequest
import br.com.zup.edu.DeleteKeyGrpcResponse
import br.com.zup.edu.DeleteKeyGrpcServiceGrpc
import br.com.zup.edu.pixkey.PixRepository
import br.com.zup.edu.pixkey.client.bcb.BancoCentralClientCall
import br.com.zup.edu.shared.exceptions.KeyNotFoundException
import br.com.zup.edu.shared.handle.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Validated
@ErrorHandler
class DeleteKeyGrpcServer(
    @Inject val pixRepository: PixRepository,
    @Inject val bancoCentralClientCall: BancoCentralClientCall
    ) :
    DeleteKeyGrpcServiceGrpc.DeleteKeyGrpcServiceImplBase() {

    @Transactional
    override fun delete(
        request: DeleteKeyGrpcRequest,
        responseObserver: StreamObserver<DeleteKeyGrpcResponse>
    ) {

        val chaveQueSeraDeletada = pixRepository.findByIdAndClientId(request.pixId, request.clientId)
            ?: throw KeyNotFoundException("Chave pix não encontrada ou a chave não pertence a esse cliente")
        pixRepository.deleteById(request.pixId)

        bancoCentralClientCall.deleteKeyPixBCB(chaveQueSeraDeletada)

        responseObserver.onNext(
            DeleteKeyGrpcResponse.newBuilder().setClientId(request.clientId).setPixId(request.pixId).build()
        )
        responseObserver.onCompleted()
    }


}