package br.com.zup.edu.pixkey.register

import br.com.zup.edu.KeyType
import br.com.zup.edu.RegisterKeyGrpcRequest
import br.com.zup.edu.RegisterKeyGrpcServiceGrpc
import br.com.zup.edu.pixkey.*
import br.com.zup.edu.pixkey.AccountType
import br.com.zup.edu.pixkey.client.bcb.BancoCentralClient
import br.com.zup.edu.pixkey.client.bcb.dto.*
import br.com.zup.edu.pixkey.client.itau.ErpItauClient
import br.com.zup.edu.pixkey.client.itau.dto.AccountBankInstitutionResponse
import br.com.zup.edu.pixkey.client.itau.dto.AccountDetailsResponse
import br.com.zup.edu.pixkey.client.itau.dto.AccountOwnerResponse
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false, rollback = false)
internal class RegisterKeyGrpcServerTest(
    val pixRepository: PixRepository,
) {
    @Inject
    lateinit var erpItau: ErpItauClient;

    @Inject
    lateinit var bcbClient: BancoCentralClient

    @Inject
    lateinit var grpcClient: RegisterKeyGrpcServiceGrpc.RegisterKeyGrpcServiceBlockingStub

    private val accountResponse = AccountDetailsResponse(
        AccountType.CONTA_CORRENTE, AccountBankInstitutionResponse("Itau", "123465"), "01", "10",
        AccountOwnerResponse("0102f3d0-c211-436b-a3e9-da7c94441d29", "João", cpf = "84987668009")
    )

    @BeforeEach
    internal fun setUp() {
        pixRepository.deleteAll()
    }

    @Test
    internal fun `deve registrar chave pix do tipo cpf`() {
        //cenario
        val chave = "84987668009"
        val clientId = "0102f3d0-c211-436b-a3e9-da7c94441d29"
        val tipoDaChave = KeyTypePix.CPF

        val accountResponse =
            Mockito.`when`(erpItau.searchAccountDetails(clientId, "CONTA_CORRENTE"))
                .thenReturn(HttpResponse.ok(accountResponse))

        Mockito.`when`(
            bcbClient.register(
                createBcBPixRequest(
                    Pix(
                        tipoDaChave, "84987668009", AccountType.CONTA_CORRENTE,
                        Account("Itau", "123465", "01", "10"), "0102f3d0-c211-436b-a3e9-da7c94441d29", cpf = "84987668009", "João"
                    )
                )
            )
        ).thenReturn(
            HttpResponse.created(
                CreatePixKeyResponse(
                    tipoDaChave.name,
                    chave,
                    BankAccount(
                        accountNumber = "10",
                        accountType = br.com.zup.edu.pixkey.client.bcb.dto.AccountType.CACC
                    ),
                    Owner(OwnerType.NATURAL_PERSON, "João", "84987668009"),
                    LocalDateTime.now()

                )
            )
        )
        // acao

        val chamada = grpcClient.register(
            RegisterKeyGrpcRequest.newBuilder()
                .setKeyValue(chave)
                .setKeyType(KeyType.CPF)
                .setClientId(clientId)
                .setClientAccountType(br.com.zup.edu.AccountType.CONTA_CORRENTE)
                .build()
        )

        // validacao
        with(pixRepository) {
            assertTrue(existsByKeyValue(chave))
            assertEquals(clientId, chamada.clientId)
            assertEquals(1, pixRepository.count())

        }


    }

    @Test
    internal fun `deve registrar chave pix do tipo email`() {
        //cenario
        val chave = "joao.santos@joao.com.br"
        val clientId = "0102f3d0-c211-436b-a3e9-da7c94441d29"
        val tipoDaChave = KeyTypePix.EMAIL

        Mockito.`when`(erpItau.searchAccountDetails(clientId, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))

        Mockito.`when`(
            bcbClient.register(
                createBcBPixRequest(
                    Pix(
                        tipoDaChave, chave, AccountType.CONTA_CORRENTE,
                        Account("Itau", "123465", "01", "10"), clientId, cpf = "84987668009", "João"
                    )
                )
            )
        ).thenReturn(
            HttpResponse.created(
                CreatePixKeyResponse(
                    tipoDaChave.name,
                    chave,
                    BankAccount(
                        accountNumber = "10",
                        accountType = br.com.zup.edu.pixkey.client.bcb.dto.AccountType.CACC
                    ),
                    Owner(OwnerType.NATURAL_PERSON, "João", "84987668009"),
                    LocalDateTime.now()

                )
            )
        )
        // acao

        val chamada = grpcClient.register(
            RegisterKeyGrpcRequest.newBuilder()
                .setKeyValue(chave)
                .setKeyType(KeyType.EMAIL)
                .setClientId(clientId)
                .setClientAccountType(br.com.zup.edu.AccountType.CONTA_CORRENTE)
                .build()
        )

        // validacao
        with(pixRepository) {
            assertTrue(existsByKeyValue(chave))
            assertEquals(clientId, chamada.clientId)
            assertEquals(1, pixRepository.count())

        }


    }

    @Test
    internal fun `deve registrar chave pix do tipo celular`() {
        //cenario
        val chave = "+5551990237788"
        val clientId = "0102f3d0-c211-436b-a3e9-da7c94441d29"
        val tipoDaChave = KeyTypePix.CELLPHONE

        Mockito.`when`(erpItau.searchAccountDetails(clientId, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))

        Mockito.`when`(
            bcbClient.register(
                createBcBPixRequest(
                    Pix(
                        tipoDaChave, chave, AccountType.CONTA_CORRENTE,
                        Account("Itau", "123465", "01", "10"), clientId, cpf ="84987668009", "João"
                    )
                )
            )
        ).thenReturn(
            HttpResponse.created(
                CreatePixKeyResponse(
                    tipoDaChave.name,
                    chave,
                    BankAccount(
                        accountNumber = "10",
                        accountType = br.com.zup.edu.pixkey.client.bcb.dto.AccountType.CACC
                    ),
                    Owner(OwnerType.NATURAL_PERSON, "João", "84987668009"),
                    LocalDateTime.now()

                )
            )
        )
        // acao

        val chamada = grpcClient.register(
            RegisterKeyGrpcRequest.newBuilder()
                .setKeyValue(chave)
                .setKeyType(KeyType.CELLPHONE)
                .setClientId(clientId)
                .setClientAccountType(br.com.zup.edu.AccountType.CONTA_CORRENTE)
                .build()
        )

        // validacao
        with(pixRepository) {
            assertTrue(existsByKeyValue(chave))
            assertEquals(clientId, chamada.clientId)
            assertEquals(1, pixRepository.count())

        }


    }

    @Test //
    internal fun `deve registar chave pix do tipo aleatoria`() {
        //cenario

        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"
        val tipoDaChave = KeyTypePix.RANDOM

        Mockito.`when`(erpItau.searchAccountDetails(idClient, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))

        Mockito.`when`(
            bcbClient.register(
                createBcBPixRequest(
                    Pix(
                        tipoDaChave,
                        tipoDaChave.name,
                        AccountType.CONTA_CORRENTE,
                        Account("Itau", "123465", "01", "10"),
                        idClient,
                        "84987668009",
                        "João"
                    )
                )
            )
        ).thenReturn(
            HttpResponse.created(
                CreatePixKeyResponse(
                    tipoDaChave.name,
                    "0102f3d0-c211-436b-a3e9-da7c94441d29",
                    BankAccount(
                        accountNumber = "10",
                        accountType = br.com.zup.edu.pixkey.client.bcb.dto.AccountType.CACC
                    ),
                    Owner(OwnerType.NATURAL_PERSON, "João", "84987668009"),
                    LocalDateTime.now()
                )
            )
        )
        // acao

        val chamada = grpcClient.register(
            RegisterKeyGrpcRequest.newBuilder()
                .setKeyType(KeyType.RANDOM)
                .setClientId(idClient)
                .setClientAccountType(br.com.zup.edu.AccountType.CONTA_CORRENTE)
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

        Mockito.`when`(erpItau.searchAccountDetails(idClient, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))
        // acao
        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyValue(chaveRandom)
            .setKeyType(KeyType.UNKNOWN_KEY_TYPE)
            .setClientId(idClient)
            .setClientAccountType(br.com.zup.edu.AccountType.CONTA_CORRENTE)
            .build()

        val request2 = RegisterKeyGrpcRequest.newBuilder()
            .setKeyValue(chaveRandom)
            .setKeyType(KeyType.RANDOM)
            .setClientId(idClient)
            .setClientAccountType(br.com.zup.edu.AccountType.UNKNOWN_ACCOUNT_TYPE)
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
        assertEquals("Tipo de conta inválida", error2.status.description)
        assertEquals(0, pixRepository.count())

    }

    @Test
    internal fun `nao deve registrar chave e deve retornar erro not found ao consultar o erp itau com um client id invalido`() {
        //cenario
        val chave = "theoalfonso78@gmail.com"
        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"

        Mockito.`when`(erpItau.searchAccountDetails(idClient, "CONTA_POUPANCA"))
            .thenThrow(HttpClientResponseException::class.java)
        //acao
        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyValue(chave)
            .setKeyType(KeyType.EMAIL)
            .setClientId(idClient)
            .setClientAccountType(br.com.zup.edu.AccountType.CONTA_POUPANCA)
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
    internal fun `nao deve registrar chave e deve retornar erro de already created ao tentar cadastrar uma chave que ja foi cadastrada`() {

        //cenario
        pixRepository.save(
            Pix(
                KeyTypePix.EMAIL,
                "theoalfonso78@gmail.com",
                AccountType.CONTA_CORRENTE,
                Account("Itau", "12345", "001", "001"),
                "0102f3d0-c211-436b-a3e9-da7c94441d29",
                "0132323223",
                "João"
            )
        )

        // acao
        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyValue("theoalfonso78@gmail.com")
            .setKeyType(KeyType.EMAIL)
            .setClientId("0102f3d0-c211-436b-a3e9-da7c94441d29")
            .setClientAccountType(br.com.zup.edu.AccountType.CONTA_POUPANCA)
            .build()

        // validacao
        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }

        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
        assertEquals("A chave pix theoalfonso78@gmail.com Já existe!", error.status.description)
        assertEquals(1, pixRepository.count())
    }

    @Test
    internal fun `nao deve registrar chave e deve retornar erro ao chamar o client http do bcb`() {

        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"

        val accountResponse = AccountDetailsResponse(
            AccountType.CONTA_CORRENTE, AccountBankInstitutionResponse("Itau", "123465"), "01", "10",
            AccountOwnerResponse("0102f3d0-c211-436b-a3e9-da7c94441d29", "João", "25738449002")
        )
        Mockito.`when`(erpItau.searchAccountDetails(idClient, "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(accountResponse))

        Mockito.`when`(
            bcbClient.register(
                createBcBPixRequest(
                    Pix(
                        KeyTypePix.RANDOM,
                        KeyTypePix.RANDOM.toString(),
                        AccountType.CONTA_CORRENTE,
                        Account("Itau", "123465", "01", "10"),
                        "0102f3d0-c211-436b-a3e9-da7c94441d29",
                        "25738449002",
                        "João"
                    )
                )
            )
        ).thenThrow(HttpClientResponseException::class.java)
        // acao

        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyType(KeyType.RANDOM)
            .setClientId(idClient)
            .setClientAccountType(br.com.zup.edu.AccountType.CONTA_CORRENTE)
            .build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }
        // validacao
        assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
        assertEquals("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)", error.status.description)
        assertEquals(0, pixRepository.count())

    }

    @ParameterizedTest
    @ValueSource(strings = ["011320840145","0564209901","15151132321231564987","1"])
    internal fun `nao deve registrar chave pix ao passar uma chave invalida do tipo cpf`(chave:String) {
       //cenario
        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"
       //acao
        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyType(KeyType.CPF)
            .setKeyValue(chave)
            .setClientId(idClient)
            .setClientAccountType(br.com.zup.edu.AccountType.CONTA_POUPANCA)
            .build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }
        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("registerKey.request: Chave pix inválida", error.status.description)

    }

    @ParameterizedTest
    @ValueSource(strings = ["+5551910254646445463778","+12","55519102377","519102377"])
    internal fun `nao deve registrar chave pix ao passar uma chave invalida do tipo celular`(chave:String) {
        //cenario
        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"
        //acao
        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyType(KeyType.CELLPHONE)
            .setKeyValue(chave)
            .setClientId(idClient)
            .setClientAccountType(br.com.zup.edu.AccountType.CONTA_POUPANCA)
            .build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }
        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("registerKey.request: Chave pix inválida", error.status.description)

    }

    @ParameterizedTest
    @ValueSource(strings = ["joao.com.br","alberto@","@joao.com,br","bento"])
    internal fun `nao deve registrar chave pix ao passar uma chave invalida do tipo email`(chave:String) {
        //cenario
        val idClient = "0102f3d0-c211-436b-a3e9-da7c94441d29"
        //acao
        val request = RegisterKeyGrpcRequest.newBuilder()
            .setKeyType(KeyType.EMAIL)
            .setKeyValue(chave)
            .setClientId(idClient)
            .setClientAccountType(br.com.zup.edu.AccountType.CONTA_POUPANCA)
            .build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }
        //validacao
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("registerKey.request: Chave pix inválida", error.status.description)

    }

    @MockBean(ErpItauClient::class)
    fun erpItau(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class client {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegisterKeyGrpcServiceGrpc.RegisterKeyGrpcServiceBlockingStub? {
            return RegisterKeyGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}