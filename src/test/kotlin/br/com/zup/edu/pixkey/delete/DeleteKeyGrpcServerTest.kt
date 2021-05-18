package br.com.zup.edu.pixkey.delete

import br.com.zup.edu.AccountType
import br.com.zup.edu.DeleteKeyGrpcRequest
import br.com.zup.edu.DeleteKeyGrpcServiceGrpc
import br.com.zup.edu.KeyType
import br.com.zup.edu.pixkey.Account
import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.PixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false, rollback = false)
internal class DeleteKeyGrpcServerTest (val pixRepository: PixRepository,){

    @Inject
    lateinit var grpcClient: DeleteKeyGrpcServiceGrpc.DeleteKeyGrpcServiceBlockingStub

    @BeforeEach
    fun setUp() {
        pixRepository.deleteAll()
    }

    @Test
    internal fun `deve deletar uma chave pix cadastrada no banco`() {
        //cenario

        val chaveCadastrada = pixRepository.save(
            Pix(
                KeyType.EMAIL,
                "theoalfonso78@gmail.com",
                AccountType.CONTA_POUPANCA,
                Account("Itau", "12345", "001", "001"),
                "0102f3d0-c211-436b-a3e9-da7c94441d29",
                "0132323223",
                "João"
            )
        )

        //acao
          val response = grpcClient.delete(DeleteKeyGrpcRequest.newBuilder().setClientId(chaveCadastrada.clientId).setPixId(chaveCadastrada.id).build())

        // validacao
        with(pixRepository){

            assertEquals(0,count())
            assertEquals(chaveCadastrada.clientId,response.clientId)
            assertEquals(chaveCadastrada.id,response.pixId)
            assertFalse(existsById(chaveCadastrada.id!!))

        }
    }

    @Test
    internal fun `nao deve deletar uma chave pix quando passado um pix id invalido`() {
        //cenario
        val chaveCadastrada = pixRepository.save(
            Pix(
                KeyType.EMAIL,
                "theoalfonso78@gmail.com",
                AccountType.CONTA_POUPANCA,
                Account("Itau", "12345", "001", "001"),
                "0102f3d0-c211-436b-a3e9-da7c94441d29",
                "0132323223",
                "João"
            )
        )

        //acao
        val error = Assertions.assertThrows(StatusRuntimeException::class.java) {
            grpcClient.delete(DeleteKeyGrpcRequest.newBuilder().setClientId(chaveCadastrada.clientId).setPixId("9f4eacf5-99ab-4f73-8f68-e699377b093c").build())
        }

        assertEquals(Status.NOT_FOUND.code,error.status.code)
        assertEquals("Chave pix não encontrada ou a chave não pertence a esse cliente",error.status.description)
        assertEquals(1,pixRepository.count())
        assertTrue(pixRepository.existsByKeyValue("theoalfonso78@gmail.com"))
    }

    @Test
    internal fun `nao deve deletar um chave pix passando o client id errado ou inexistente`() {
        val chaveCadastrada = pixRepository.save(
            Pix(
                KeyType.EMAIL,
                "theoalfonso78@gmail.com",
                AccountType.CONTA_POUPANCA,
                Account("Itau", "12345", "001", "001"),
                "0102f3d0-c211-436b-a3e9-da7c94441d29",
                "0132323223",
                "João"
            )
        )

        //acao
        val error = Assertions.assertThrows(StatusRuntimeException::class.java) {
            grpcClient.delete(DeleteKeyGrpcRequest.newBuilder().setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890abcdefgo").setPixId(chaveCadastrada.id).build())
        }

        assertEquals(Status.NOT_FOUND.code,error.status.code)
        assertEquals("Chave pix não encontrada ou a chave não pertence a esse cliente",error.status.description)
        assertEquals(1,pixRepository.count())
        assertTrue(pixRepository.existsByKeyValue("theoalfonso78@gmail.com"))

    }

    @Factory
    class client {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): DeleteKeyGrpcServiceGrpc.DeleteKeyGrpcServiceBlockingStub? {
            return DeleteKeyGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}