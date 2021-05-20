package br.com.zup.edu.pixkey.register

import br.com.zup.edu.AccountType
import br.com.zup.edu.KeyType
import br.com.zup.edu.RegisterKeyGrpcRequest
import br.com.zup.edu.RegisterKeyGrpcServiceGrpc
import br.com.zup.edu.pixkey.Account
import br.com.zup.edu.pixkey.Pix
import br.com.zup.edu.pixkey.PixRepository
import br.com.zup.edu.pixkey.client.bcb.BancoCentralClient
import br.com.zup.edu.pixkey.client.bcb.dto.*
import br.com.zup.edu.pixkey.client.itau.ErpItauClient
import br.com.zup.edu.pixkey.client.itau.dto.AccountBankInstitutionResponse
import br.com.zup.edu.pixkey.client.itau.dto.AccountDetailsResponse
import br.com.zup.edu.pixkey.client.itau.dto.AccountOwnerResponse
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.HttpResponseException
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
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

        Mockito.`when`(
            bcbClient.register(
                createBcBPixRequest(
                    Pix(
                        KeyType.CPF, "84987668009", AccountType.CONTA_CORRENTE,
                        Account("Itau", "123465", "01", "10"), "0102f3d0-c211-436b-a3e9-da7c94441d29", chaveCpf, "João"
                    )
                )
            )
        ).thenReturn(
            HttpResponse.created(
                CreatePixKeyResponse(
                    "CPF",
                    chaveCpf,
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
                        KeyType.RANDOM,
                        KeyType.RANDOM.toString(),
                        AccountType.CONTA_CORRENTE,
                        Account("Itau", "123465", "01", "10"),
                        "0102f3d0-c211-436b-a3e9-da7c94441d29",
                        "25738449002",
                        "João"
                    )
                )
            )
        ).thenReturn(
            HttpResponse.created(
                CreatePixKeyResponse(
                    "RANDOM",
                    "0102f3d0-c211-436b-a3e9-da7c94441d29",
                    BankAccount(
                        accountNumber = "10",
                        accountType = br.com.zup.edu.pixkey.client.bcb.dto.AccountType.CACC
                    ),
                    Owner(OwnerType.NATURAL_PERSON, "João", "25738449002"),
                    LocalDateTime.now()
                )
            )
        )
        // acao

        val chamada = grpcClient.register(
            RegisterKeyGrpcRequest.newBuilder()
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
    internal fun `deve retornar erro de already created ao tentar cadastrar uma chave que ja foi cadastrada`() {

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

    @Test
    internal fun `deve retornar erro ao chamar o client http do bcb`() {

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
                        KeyType.RANDOM,
                        KeyType.RANDOM.toString(),
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
            .setPixKeyType(KeyType.RANDOM)
            .setClientId(idClient)
            .setClientAccountType(AccountType.CONTA_CORRENTE)
            .build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.register(request)
        }



    // validacao

    assertEquals(Status.FAILED_PRECONDITION.code, error.status.code)
    assertEquals("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)", error.status.description)
    assertEquals(0, pixRepository.count())

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