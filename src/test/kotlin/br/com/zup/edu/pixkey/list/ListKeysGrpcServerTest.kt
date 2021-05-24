package br.com.zup.edu.pixkey.list

import br.com.zup.edu.ListAllKeysGrpcServiceGrpc
import br.com.zup.edu.ListAllRequest
import br.com.zup.edu.pixkey.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class ListKeysGrpcServerTest(
    val repository: PixRepository,
    val grpcClient: ListAllKeysGrpcServiceGrpc.ListAllKeysGrpcServiceBlockingStub,
) {

    @BeforeEach
    fun setup() {
        repository.save(
            chave(
                tipo = KeyTypePix.EMAIL,
                chave = "joao@aguamineral.com.br",
                clienteId = "0102f3d0-c211-436b-a3e9-da7c94441d29"
            )
        )
        repository.save(chave(tipo = KeyTypePix.CPF, chave = "63657520325", clienteId = UUID.randomUUID().toString()))
        repository.save(
            chave(
                tipo = KeyTypePix.RANDOM,
                chave = " ",
                clienteId = "0102f3d0-c211-436b-a3e9-da7c94441d29"
            )
        )
        repository.save(
            chave(
                tipo = KeyTypePix.CELLPHONE,
                chave = "+555155554321",
                clienteId = "0102f3d0-c211-436b-a3e9-da7c94441d29"
            )
        )
        repository.save(
            chave(
                tipo = KeyTypePix.CELLPHONE,
                chave = "+5551980068046",
                clienteId = UUID.randomUUID().toString()
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente`() {
        // cenário
        val clienteId = "0102f3d0-c211-436b-a3e9-da7c94441d29"

        // ação
        val response = grpcClient.listAll(
            ListAllRequest.newBuilder()
                .setClientId(clienteId)
                .build()
        )

        // validação
        with(response.keysList) {
            assertEquals(3, response.keysCount)
        }
    }

    @Test
    fun `nao deve listar as chaves do cliente quando cliente nao possuir chaves`() {
        // cenário
        val clienteSemChaves = UUID.randomUUID().toString()

        // ação
        val response = grpcClient.listAll(
            ListAllRequest.newBuilder()
                .setClientId(clienteSemChaves)
                .build()
        )

        // validação
        assertEquals(0, response.keysCount)
    }

    @Test
    fun `nao deve listar todas as chaves do cliente quando clienteId for invalido`() {
        // cenário
        val clienteIdInvalido = "  "

        // ação
        val thrown = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.listAll(
                ListAllRequest.newBuilder()
                    .setClientId(clienteIdInvalido)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio!", status.description)
        }
    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListAllKeysGrpcServiceGrpc.ListAllKeysGrpcServiceBlockingStub? {
            return ListAllKeysGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: KeyTypePix,
        chave: String,
        clienteId: String,
    ): Pix {
        return Pix(
            clientId = clienteId,
            keyType = tipo,
            keyValue = chave,
            accountType = AccountType.CONTA_CORRENTE,
            account = Account(
                institution = "UNIBANCO ITAU",
                isbp = "001",
                agency = "001",
                numberAccount = "001"
            ), cpf = "01182036004", name = "Pedro Fontanella"
        )
    }
}