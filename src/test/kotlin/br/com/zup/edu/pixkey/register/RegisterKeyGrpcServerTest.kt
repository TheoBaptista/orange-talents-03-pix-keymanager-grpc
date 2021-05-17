package br.com.zup.edu.pixkey.register

import br.com.zup.edu.AccountType
import br.com.zup.edu.KeyType
import br.com.zup.edu.RegisterKeyGrpcRequest
import br.com.zup.edu.RegisterKeyGrpcServiceGrpc
import br.com.zup.edu.pixkey.Account
import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.PixRepository
import br.com.zup.edu.pixkey.client.ErpItauClientHttp
import br.com.zup.edu.pixkey.client.dto.AccountBankInstitutionResponse
import br.com.zup.edu.pixkey.client.dto.AccountDetailsResponse
import br.com.zup.edu.pixkey.client.dto.AccountOwnerResponse
import br.com.zup.edu.pixkey.shared.PixValidator
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false, rollback = false)
internal class RegisterKeyGrpcServerTest(
    val pixRepository: PixRepository,
) {
    @Inject
    lateinit var erpItau: ErpItauClientHttp;

    @Inject
    lateinit var grpcClient: RegisterKeyGrpcServiceGrpc.RegisterKeyGrpcServiceBlockingStub

    @BeforeEach
    internal fun setUp() {
        pixRepository.deleteAll()
    }

    @Test
    internal fun `deve registrar chave pix do tipo cpf`() {
        //cenario
        val chaveCpf = "84987668009"
        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"

        val accountResponse = AccountDetailsResponse(
            AccountType.CONTA_CORRENTE, AccountBankInstitutionResponse("Itau", "123465"), "01", "10",
            AccountOwnerResponse("0102f3d0-c211-436b-a3e9-da7c94441d29", "João", chaveCpf)
        )
        Mockito.`when`(erpItau.searchAccountDetails(idClient, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))
        // acao

        val chamada = grpcClient.register(
            RegisterKeyGrpcRequest.newBuilder()
                .setKeyValue(chaveCpf)
                .setPixKeyType(KeyType.CPF)
                .setClientId(idClient)
                .setClientAccountType(AccountType.CONTA_CORRENTE)
                .build()
        )

        // validacao
        with(pixRepository) {
            assertTrue(existsByKeyValue(chaveCpf))
            assertEquals(idClient, chamada.clientId)
            assertEquals(1, pixRepository.count())

        }


    }


    @Test //
    internal fun `deve cadastrar uma chave aleatoria`() {
        //cenario
        val chaveRandom = ""
        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"

        val accountResponse = AccountDetailsResponse(
            AccountType.CONTA_CORRENTE, AccountBankInstitutionResponse("Itau", "123465"), "01", "10",
            AccountOwnerResponse("0102f3d0-c211-436b-a3e9-da7c94441d29", "João", "25738449002")
        )
        Mockito.`when`(erpItau.searchAccountDetails(idClient, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))
        // acao

        val chamada = grpcClient.register(
            RegisterKeyGrpcRequest.newBuilder()
                .setKeyValue(chaveRandom)
                .setPixKeyType(KeyType.RANDOM)
                .setClientId(idClient)
                .setClientAccountType(AccountType.CONTA_CORRENTE)
                .build()
        )
        // validacao
        var optional = pixRepository.findById(chamada.pixId)

        with(chamada) {

            assertTrue(optional.isPresent)
            assertTrue(optional.get().clientId.equals(idClient))
            assertEquals(1, pixRepository.count())
        }


    }

    @Test
    internal fun `deve testar os if e elses do metodo convert da classe RegisterKeyRequest`() {

        //cenario
        val chaveRandom = ""
        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"

        val accountResponse = AccountDetailsResponse(
            AccountType.CONTA_CORRENTE, AccountBankInstitutionResponse("Itau", "123465"), "01", "10",
            AccountOwnerResponse("0102f3d0-c211-436b-a3e9-da7c94441d29", "João", "25738449002")
        )
        Mockito.`when`(erpItau.searchAccountDetails(idClient, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))
        // acao


        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyValue(chaveRandom)
            .setPixKeyType(KeyType.UNKNOWN_KEY_TYPE)
            .setClientId(idClient)
            .setClientAccountType(AccountType.CONTA_CORRENTE)
            .build()

        val request2 = RegisterKeyGrpcRequest.newBuilder()
            .setKeyValue(chaveRandom)
            .setPixKeyType(KeyType.RANDOM)
            .setClientId(idClient)
            .setClientAccountType(AccountType.UNKNOWN_ACCOUNT_TYPE)
            .build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }

        val error2 = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request2)
        }


        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Tipo de chave Pix inválido", error.status.description)

        assertEquals(Status.INVALID_ARGUMENT.code, error2.status.code)
        assertEquals("Tipo de conta inválido", error2.status.description)
        assertEquals(0, pixRepository.count())

    }

    @Test
    internal fun `deve retornar erro not found ao consultar o erp itau`() {
        //cenario
        val chave = "theoalfonso78@gmail.com"
        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"

        Mockito.`when`(erpItau.searchAccountDetails(idClient, "CONTA_POUPANCA"))
            .thenThrow(HttpClientResponseException::class.java)
        //acao
        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyValue(chave)
            .setPixKeyType(KeyType.EMAIL)
            .setClientId(idClient)
            .setClientAccountType(AccountType.CONTA_POUPANCA)
            .build()

        //validacao

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }

        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("Cliente com o id:${idClient} não encontrado!", error.status.description)
        assertEquals(0, pixRepository.count())


    }

    @Test
    internal fun `deve retornar erro de already created ao tentar cadastrar uma chave`() {

        //cenario
        pixRepository.save(
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

        // acao
        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyValue("theoalfonso78@gmail.com")
            .setPixKeyType(KeyType.EMAIL)
            .setClientId("0102f3d0-c211-436b-a3e9-da7c94441d29")
            .setClientAccountType(AccountType.CONTA_POUPANCA)
            .build()

        // validacao
        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }

        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
        assertEquals("A chave pix theoalfonso78@gmail.com Já existe!", error.status.description)
        assertEquals(1, pixRepository.count())
    }

    @MockBean(ErpItauClientHttp::class)
    fun erpItau(): ErpItauClientHttp? {
        return Mockito.mock(ErpItauClientHttp::class.java)
    }

    @Factory
    class client {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegisterKeyGrpcServiceGrpc.RegisterKeyGrpcServiceBlockingStub? {
            return RegisterKeyGrpcServiceGrpc.newBlockingStub((channel))
        }
    }
}