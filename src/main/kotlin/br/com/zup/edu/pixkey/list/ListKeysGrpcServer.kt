package br.com.zup.edu.pixkey.list

import br.com.zup.edu.*
import br.com.zup.edu.pixkey.PixRepository
import br.com.zup.edu.shared.handle.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class ListKeysGrpcServer(@Inject val pixRepository: PixRepository) :
    ListAllKeysGrpcServiceGrpc.ListAllKeysGrpcServiceImplBase() {

    override fun listAll(request: ListAllRequest, responseObserver: StreamObserver<ListAllResponse>) {

        if (request.clientId.isNullOrBlank()) {
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio!")
        }

        val chaves = pixRepository.findAllByClientId(request.clientId).map {

            ListAllResponse.PixKey.newBuilder().setPixId(it.id)
                .setType(KeyType.valueOf(it.keyType.name))
                .setKey(it.keyValue)
                .setAccountType(AccountType.valueOf(it.accountType.name))
                .setCreatedAt(it.createdAt.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder().setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano).build()
                }).build()


        }

        responseObserver.onNext(
            ListAllResponse.newBuilder().setClientId(request.clientId).addAllKeys(chaves).build()
        )
        responseObserver.onCompleted()
    }
}