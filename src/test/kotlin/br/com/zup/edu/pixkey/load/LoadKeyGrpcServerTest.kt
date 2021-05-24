package br.com.zup.edu.pixkey.load

import br.com.zup.edu.LoadKeyGrpcServiceGrpc
import br.com.zup.edu.LoadKeyRequest
import br.com.zup.edu.pixkey.*
import br.com.zup.edu.pixkey.client.bcb.BancoCentralClient
import br.com.zup.edu.pixkey.client.bcb.dto.BankAccount
import br.com.zup.edu.pixkey.client.bcb.dto.Owner
import br.com.zup.edu.pixkey.client.bcb.dto.OwnerType
import br.com.zup.edu.pixkey.client.bcb.dto.PixKeyDetailsResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class LoadKeyGrpcServerTest(
    val repository: PixRepository,
    val grpcClient: LoadKeyGrpcServiceGrpc.LoadKeyGrpcServiceBlockingStub,
) {


    @Inject
    lateinit var bcbClient: BancoCentralClient


    @BeforeEach
    fun setup() {
        repository.save(
            chave(
                tipo = KeyTypePix.EMAIL,
                chave = "joao@aguamineral.com.br",
                clienteId = UUID.randomUUID().toString()
            )
        )
        repository.save(chave(tipo = KeyTypePix.CPF, chave = "63657520325", clienteId = UUID.randomUUID().toString()))
        repository.save(chave(tipo = KeyTypePix.RANDOM, chave = " ", clienteId = UUID.randomUUID().toString()))
        repository.save(
            chave(
                tipo = KeyTypePix.CELLPHONE,
                chave = "+551155554321",
                clienteId = UUID.randomUUID().toString()
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve carregar chave por pixId e clienteId`() {
        // cenário
        val chaveExistente = repository.findByKeyValue("+551155554321").get()

        // ação
        val response = grpcClient.load(
            LoadKeyRequest.newBuilder()
                .setPixId(
                   LoadKeyRequest.WhitPixId.newBuilder().setClientId(chaveExistente.clientId)
                       .setPixId(chaveExistente.id)
                ).build()
        )

        // validação
        with(response) {
            assertEquals(chaveExistente.id, this.pixId)
            assertEquals(chaveExistente.clientId, this.clienteId)
            assertEquals(chaveExistente.keyType.name, this.key.type.name)
            assertEquals(chaveExistente.keyValue, this.key.key)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando filtro invalido`() {
        // ação
        val thrown = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.load(
                LoadKeyRequest.newBuilder()
                    .setPixId(
                        LoadKeyRequest.WhitPixId.newBuilder()
                            .setPixId("")
                            .setClientId("")
                            .build()
                    ).build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)

        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando registro nao existir`() {
        // ação
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val thrown = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.load(
                LoadKeyRequest.newBuilder()
                    .setPixId(
                        LoadKeyRequest.WhitPixId.newBuilder()
                            .setPixId(pixIdNaoExistente)
                            .setClientId(clienteIdNaoExistente)
                            .build()
                    ).build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro existir localmente`() {
        // cenário
        val chaveExistente = repository.findByKeyValue("joao@aguamineral.com.br").get()

        // ação
        val response = grpcClient.load(
            LoadKeyRequest.newBuilder()
                .setPixKey("joao@aguamineral.com.br")
                .build()
        )

        // validação
        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clientId, this.clienteId)
            assertEquals(chaveExistente.keyType.name, this.key.type.name)
            assertEquals(chaveExistente.keyValue, this.key.key)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro nao existir localmente mas existir no BCB`() {
        // cenário
        val bcbResponse = pixKeyDetailsResponse(KeyTypePix.CPF,key = "04452094019")
        `when`(bcbClient.findByKey(key = "user.from.another.bank@santander.com.br"))
            .thenReturn(HttpResponse.ok(pixKeyDetailsResponse(KeyTypePix.CPF,"04452094019")))

        // ação
        val response = grpcClient.load(
            LoadKeyRequest.newBuilder()
                .setPixKey("user.from.another.bank@santander.com.br")
                .build()
        )

        // validação
        with(response) {
            assertEquals(" ", this.pixId)
            assertEquals(" ", this.clienteId)
            assertEquals(bcbResponse.keyTypePix, this.key.type.name)
            assertEquals(bcbResponse.key, this.key.key)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando registro nao existir localmente nem no BCB`() {
        // cenário
        `when`(bcbClient.findByKey(key = "not.existing.user@santander.com.br"))
            .thenReturn(HttpResponse.notFound())

        // ação
        val thrown = assertThrows(StatusRuntimeException::class.java){
            grpcClient.load(
                LoadKeyRequest.newBuilder()
                    .setPixKey("not.existing.user@santander.com.br")
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada!", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave quando nao existe localmente e ao consultar o BCB der erro`() {
        // cenário
        `when`(bcbClient.findByKey(key = "not.existing.user@santander.com.br"))
            .thenThrow(HttpClientResponseException::class.java)

        // ação
        val thrown = assertThrows(StatusRuntimeException::class.java){
            grpcClient.load(
                LoadKeyRequest.newBuilder()
                    .setPixKey("not.existing.user@santander.com.br")
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao consultar o Banco Central do Brasil", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando filtro invalido`() {
        // ação
        val thrown = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.load(LoadKeyRequest.newBuilder().setPixKey("").build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("key: não deve estar em branco", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave quando filtro invalido`() {

        // ação
        val thrown = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.load(LoadKeyRequest.newBuilder().build())
        }

        // validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }


    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): LoadKeyGrpcServiceGrpc.LoadKeyGrpcServiceBlockingStub? {
            return LoadKeyGrpcServiceGrpc.newBlockingStub(channel)
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

    private fun pixKeyDetailsResponse(keyTypePix: KeyTypePix, key: String): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyTypePix = keyTypePix.name,
            key = key,
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now(),
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = "90400888",
            branch = "9871",
            accountNumber = "987654",
            accountType = br.com.zup.edu.pixkey.client.bcb.dto.AccountType.SVGS
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = OwnerType.NATURAL_PERSON,
            name = "Show me the true",
            taxIdNumber = "12345678901"
        )
    }

}