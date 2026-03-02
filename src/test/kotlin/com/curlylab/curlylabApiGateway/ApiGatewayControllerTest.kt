package com.curlylab.curlylabApiGateway

import com.curlylab.curlylabApiGateway.config.RabbitConfig
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*
import kotlin.test.assertNotNull
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties

@SpringBootTest
@AutoConfigureWebTestClient
@Import(RabbitConfig::class)
class ApiGatewayControllerTest {
    private val gatewayURI = "http://localhost:8080"

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockkBean
    private lateinit var rabbitTemplate: RabbitTemplate

    @MockkBean
    private lateinit var webClient: WebClient

    @Autowired
    private lateinit var controller: ApiGatewayController

    @BeforeEach
    fun setUp() {
        rabbitTemplate = mockk()
        webClient = mockk()
        controller = ApiGatewayController(rabbitTemplate, webClient, gatewayURI)
        clearMocks(rabbitTemplate, webClient)
    }

    // Продукты - позитивные тесты
    @Test
    fun `получение всех продуктов - успешный сценарий`() {
        // Сценарий: успешное получение списка всех продуктов через backend
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        // Настраиваем поведение responseEntity
        every { responseEntity.statusCode } returns HttpStatus.OK

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/products") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        val result = controller.getAllProducts().block()

        assert(result != null)
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.get() }
        verify { requestHeadersUriSpec.uri("$gatewayURI/products") }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.toEntity(Any::class.java) }
    }

    @Test
    fun `получение продукта по ID - успешный сценарий`() {
        // Сценарий: успешное получение информации о конкретном продукте
        val productId = UUID.randomUUID()
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        // Настраиваем поведение responseEntity
        every { responseEntity.statusCode } returns HttpStatus.OK

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/products/$productId") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        val result = controller.getProduct(productId).block()

        assert(result != null)
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.get() }
        verify { requestHeadersUriSpec.uri("$gatewayURI/products/$productId") }
    }

    // Пользователи - позитивные тесты
    @Test
    fun `получение пользователя по ID - успешный сценарий`() {
        // Сценарий: успешное получение информации о пользователе
        val userId = UUID.randomUUID()
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        // Настраиваем поведение responseEntity
        every { responseEntity.statusCode } returns HttpStatus.OK

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/users/$userId") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        val result = controller.getUser(userId).block()

        assert(result != null)
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.get() }
        verify { requestHeadersUriSpec.uri("$gatewayURI/users/$userId") }
    }

    @Test
    fun `создание пользователя - успешный сценарий`() {
        // Сценарий: успешное создание нового пользователя
        val user = mapOf("name" to "John Doe", "email" to "john@example.com")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "User created"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/users") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(user) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.createUser(user).block()

        assert(result?.body == "User created")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.post() }
        verify { requestBodyUriSpec.uri("$gatewayURI/users") }
        verify { requestBodyUriSpec.bodyValue(user) }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    @Test
    fun `обновление пользователя - успешный сценарий`() {
        // Сценарий: успешное обновление данных пользователя
        val userId = UUID.randomUUID()
        val user = mapOf("name" to "Jane Doe")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        // Настраиваем поведение responseEntity
        every { responseEntity.statusCode } returns HttpStatus.OK

        every { webClient.put() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/users/$userId") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(user) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        val result = controller.updateUser(userId, user).block()

        assert(result != null)
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.put() }
    }

    @Test
    fun `удаление пользователя - успешный сценарий`() {
        // Сценарий: успешное удаление пользователя
        val userId = UUID.randomUUID()
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "User deleted"

        every { webClient.delete() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/users/$userId") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.deleteUser(userId).block()

        assert(result?.body == "User deleted")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.delete() }
        verify { requestHeadersUriSpec.uri("$gatewayURI/users/$userId") }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    @Test
    fun `загрузка аватара пользователя - успешный сценарий`() {
        // Сценарий: успешная загрузка аватара для пользователя
        val userId = UUID.randomUUID()
        val filePartMock = mockk<FilePart>()
        val dataBuffer = DefaultDataBufferFactory().wrap("test image content".toByteArray())

        every { filePartMock.content() } returns Flux.just(dataBuffer)
        every { filePartMock.filename() } returns "avatar.png"

        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_PNG
        every { filePartMock.headers() } returns headers

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Avatar uploaded"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/users/$userId/upload_image") } returns requestBodyUriSpec
        every { requestBodyUriSpec.header(any(), any()) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.uploadUserAvatar(userId, filePartMock).block()

        assert(result?.body == "Avatar uploaded")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.post() }
        verify { requestBodyUriSpec.uri("$gatewayURI/users/$userId/upload_image") }
        verify { requestBodyUriSpec.header(any(), any()) }
        verify { requestBodyUriSpec.bodyValue(any()) }
    }

    @Test
    fun `удаление аватара пользователя - успешный сценарий`() {
        // Сценарий: успешное удаление аватара пользователя
        val userId = UUID.randomUUID()
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Avatar deleted"

        every { webClient.delete() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/users/$userId/avatar") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.deleteUserAvatar(userId).block()

        assert(result?.body == "Avatar deleted")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.delete() }
        verify { requestHeadersUriSpec.uri("$gatewayURI/users/$userId/avatar") }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    // Негативные тесты для пользователей
    @Test
    fun `обновление пользователя - ошибка сервера`() {
        // Сценарий: ошибка при обновлении пользователя (500 Internal Server Error)
        val userId = UUID.randomUUID()
        val user = mapOf("name" to "Jane Doe")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val errorMessage = "Database connection failed"

        every { webClient.put() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/users/$userId") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(user) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.toEntity(Any::class.java) } returns Mono.error(RuntimeException(errorMessage))

        val result = controller.updateUser(userId, user).block()

        assertNotNull(result)
        assert(result.statusCode == HttpStatus.INTERNAL_SERVER_ERROR)
    }

    // Типы волос - позитивные тесты
    @Test
    fun `получение типа волос по ID пользователя - успешный сценарий`() {
        // Сценарий: успешное получение типа волос для пользователя
        val userId = UUID.randomUUID()
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        // Настраиваем поведение responseEntity
        every { responseEntity.statusCode } returns HttpStatus.OK

        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/hairtypes/$userId") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        val result = controller.getHairType(userId).block()

        assert(result != null)
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.get() }
    }

    @Test
    fun `создание типа волос - успешный сценарий`() {
        // Сценарий: успешное создание типа волос
        val hairType = mapOf("userId" to UUID.randomUUID(), "type" to "curly")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Hair type created"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/hairtypes") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(hairType) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.createHairType(hairType).block()

        assert(result?.body == "Hair type created")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.post() }
        verify { requestBodyUriSpec.uri("$gatewayURI/hairtypes") }
        verify { requestBodyUriSpec.bodyValue(hairType) }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    @Test
    fun `обновление типа волос - успешный сценарий`() {
        // Сценарий: успешное обновление типа волос
        val userId = UUID.randomUUID()
        val hairType = mapOf("type" to "wavy")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        // Настраиваем поведение responseEntity
        every { responseEntity.statusCode } returns HttpStatus.OK

        every { webClient.put() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/hairtypes/$userId") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(hairType) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        val result = controller.updateHairType(userId, hairType).block()

        assert(result != null)
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.put() }
    }

    @Test
    fun `удаление типа волос - успешный сценарий`() {
        // Сценарий: успешное удаление типа волос
        val userId = UUID.randomUUID()
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Hair type deleted"

        every { webClient.delete() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/hairtypes/$userId") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.deleteHairType(userId).block()

        assert(result?.body == "Hair type deleted")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.delete() }
        verify { requestHeadersUriSpec.uri("$gatewayURI/hairtypes/$userId") }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    // Отзывы - позитивные тесты
    @Test
    fun `получение всех отзывов о продукте - успешный сценарий`() {
        // Сценарий: успешное получение всех отзывов для продукта
        val productId = UUID.randomUUID()
        val requestHeadersUriSpecMock = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpecMock = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpecMock = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        every { webClient.get() } returns requestHeadersUriSpecMock
        every { requestHeadersUriSpecMock.uri("$gatewayURI/products/$productId/reviews") } returns requestHeadersSpecMock
        every { requestHeadersSpecMock.retrieve() } returns responseSpecMock
        every { responseSpecMock.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        webTestClient.get()
            .uri("$gatewayURI/products/$productId/reviews")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `создание отзыва - успешный сценарий`() {
        // Сценарий: успешное создание отзыва о продукте
        val productId = UUID.randomUUID()
        val review = mapOf("userId" to UUID.randomUUID(), "rating" to 5, "comment" to "Great product!")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Review created"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/products/$productId/reviews") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(review) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.createReview(productId, review).block()

        assert(result?.body == "Review created")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.post() }
        verify { requestBodyUriSpec.uri("$gatewayURI/products/$productId/reviews") }
        verify { requestBodyUriSpec.bodyValue(review) }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    @Test
    fun `обновление отзыва - успешный сценарий`() {
        // Сценарий: успешное обновление существующего отзыва
        val productId = UUID.randomUUID()
        val reviewId = UUID.randomUUID()
        val review = mapOf("rating" to 4)

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        // Настраиваем поведение responseEntity
        every { responseEntity.statusCode } returns HttpStatus.OK

        every { webClient.put() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/products/$productId/reviews/$reviewId") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(review) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        val result = controller.updateReview(productId, reviewId, review).block()

        assert(result != null)
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.put() }
    }

    @Test
    fun `удаление отзыва - успешный сценарий`() {
        // Сценарий: успешное удаление отзыва
        val productId = UUID.randomUUID()
        val reviewId = UUID.randomUUID()
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Review deleted"

        every { webClient.delete() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/products/$productId/reviews/$reviewId") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.deleteReview(productId, reviewId).block()

        assert(result?.body == "Review deleted")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.delete() }
        verify { requestHeadersUriSpec.uri("$gatewayURI/products/$productId/reviews/$reviewId") }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    // Избранное - позитивные тесты
    @Test
    fun `получение избранного пользователя - успешный сценарий`() {
        // Сценарий: успешное получение списка избранных продуктов пользователя
        val userId = UUID.randomUUID()
        val requestHeadersUriSpecMock = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpecMock = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpecMock = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Any>>()

        every { webClient.get() } returns requestHeadersUriSpecMock
        every { requestHeadersUriSpecMock.uri("$gatewayURI/users/$userId/favourites") } returns requestHeadersSpecMock
        every { requestHeadersSpecMock.retrieve() } returns responseSpecMock
        every { responseSpecMock.toEntity(Any::class.java) } returns Mono.just(responseEntity)

        webTestClient.get()
            .uri("/users/$userId/favourites")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `проверка наличия продукта в избранном - успешный сценарий`() {
        // Сценарий: успешная проверка, находится ли продукт в избранном у пользователя
        val userId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val requestHeadersUriSpecMock = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpecMock = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpecMock = mockk<WebClient.ResponseSpec>()
        val responseEntity = mockk<ResponseEntity<Boolean>>()

        every { webClient.get() } returns requestHeadersUriSpecMock
        every { requestHeadersUriSpecMock.uri("$gatewayURI/products/$productId/is_favourite/$userId") } returns requestHeadersSpecMock
        every { requestHeadersSpecMock.retrieve() } returns responseSpecMock
        every { responseSpecMock.toEntity(Boolean::class.java) } returns Mono.just(responseEntity)

        webTestClient.get()
            .uri("/products/$productId/is_favourite/$userId")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `добавление в избранное - успешный сценарий`() {
        // Сценарий: успешное добавление продукта в избранное пользователя
        val userId = UUID.randomUUID()
        val favourite = mapOf("productId" to UUID.randomUUID())

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Added to favourites"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/users/$userId/favourites") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(favourite) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.addToFavourites(userId, favourite).block()

        assert(result?.body == "Added to favourites")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.post() }
        verify { requestBodyUriSpec.uri("$gatewayURI/users/$userId/favourites") }
        verify { requestBodyUriSpec.bodyValue(favourite) }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    @Test
    fun `удаление из избранного - успешный сценарий`() {
        // Сценарий: успешное удаление продукта из избранного пользователя
        val userId = UUID.randomUUID()
        val productId = UUID.randomUUID()
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Removed from favourites"

        every { webClient.delete() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri("$gatewayURI/users/$userId/favourites/$productId") } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.deleteFavourite(userId, productId).block()

        assert(result?.body == "Removed from favourites")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.delete() }
        verify { requestHeadersUriSpec.uri("$gatewayURI/users/$userId/favourites/$productId") }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    // Аутентификация - позитивные тесты
    @Test
    fun `регистрация пользователя - успешный сценарий`() {
        // Сценарий: успешная регистрация нового пользователя
        val registerRequest = mapOf("email" to "test@example.com", "password" to "password123")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Registration successful"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/auth/register") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(registerRequest) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        val result = controller.register(registerRequest).block()

        assert(result?.body == "Registration successful")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.post() }
        verify { requestBodyUriSpec.uri("$gatewayURI/auth/register") }
        verify { requestBodyUriSpec.bodyValue(registerRequest) }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    @Test
    fun `вход в систему - успешный сценарий`() {
        // Сценарий: успешная аутентификация пользователя
        val loginRequest = mapOf("email" to "test@example.com", "password" to "password123")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Login successful"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/auth/login") } returns requestBodyUriSpec
        every { requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON) } returns requestBodyUriSpec
        every { requestBodyUriSpec.accept(MediaType.APPLICATION_JSON) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(loginRequest) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        // Вызываем метод контроллера напрямую, а не через webTestClient
        val result = controller.login(loginRequest).block()

        assert(result?.body == "Login successful")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.post() }
        verify { requestBodyUriSpec.uri("$gatewayURI/auth/login") }
        verify { requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON) }
        verify { requestBodyUriSpec.accept(MediaType.APPLICATION_JSON) }
        verify { requestBodyUriSpec.bodyValue(loginRequest) }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    @Test
    fun `вход через Google - успешный сценарий`() {
        // Сценарий: успешная аутентификация через Google
        val googleRequest = mapOf("token" to "google-token-123")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val responseBody = "Google login successful"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/auth/google") } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(googleRequest) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(responseBody)

        // Вызываем метод контроллера напрямую
        val result = controller.google(googleRequest).block()

        assert(result?.body == "Google login successful")
        assert(result?.statusCode == HttpStatus.OK)

        verify { webClient.post() }
        verify { requestBodyUriSpec.uri("$gatewayURI/auth/google") }
        verify { requestBodyUriSpec.bodyValue(googleRequest) }
        verify { requestHeadersSpec.retrieve() }
        verify { responseSpec.bodyToMono(String::class.java) }
    }

    // Негативные тесты для аутентификации
    @Test
    fun `вход в систему - ошибка валидации 400`() {
        // Сценарий: ошибка валидации при входе (неверные учетные данные)
        val loginRequest = mapOf("email" to "wrong@example.com", "password" to "wrong")

        val requestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        val errorBody = "Invalid credentials"

        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri("$gatewayURI/auth/login") } returns requestBodyUriSpec
        every { requestBodyUriSpec.contentType(org.springframework.http.MediaType.APPLICATION_JSON) } returns requestBodyUriSpec
        every { requestBodyUriSpec.accept(org.springframework.http.MediaType.APPLICATION_JSON) } returns requestBodyUriSpec
        every { requestBodyUriSpec.bodyValue(loginRequest) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec

        // Просто настраиваем onStatus без сложной логики
        every { responseSpec.onStatus(any(), any()) } returns responseSpec

        // Настраиваем bodyToMono на ошибку
        every { responseSpec.bodyToMono(String::class.java) } returns
                Mono.error(RuntimeException("Backend validation failed: $errorBody"))

        // Выполняем запрос
        val result = controller.login(loginRequest)

        // Проверяем результат
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable.message?.contains("Backend validation failed") == true
            }
            .verify()
    }

    // AI анализ состава - позитивные и негативные тесты
    @Test
    fun `анализ состава по тексту - успешный сценарий`() {
        // Сценарий: успешный анализ состава продукта по текстовому описанию
        val text = "Aqua, Sodium Laureth Sulfate, Cocamidopropyl Betaine"
        val mockMessage = mockk<Message>()
        val mockProps = MessageProperties()
        val responseMap = mapOf("result" to mapOf("harmful" to listOf("SLS"), "safe" to listOf("Aqua")))

        // Явно указываем тип для any
        every { rabbitTemplate.convertAndSend("consistence.exchange", "consistence.request.bind", any<HairTypeRequest>()) } returns Unit

        // Настраиваем последовательность возвратов
        every { rabbitTemplate.receive("consistence.responses") } returns null andThen mockMessage

        every { mockMessage.messageProperties } returns mockProps
        every { rabbitTemplate.messageConverter.fromMessage(mockMessage) } returns responseMap

        val result = controller.analyzeConsistenceOfProduct(null, text)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                assert(response.body?.get("status") == "completed")
            }
            .verifyComplete()
    }

    @Test
    fun `анализ состава по изображению - успешный сценарий`() {
        // Сценарий: успешный анализ состава продукта по изображению этикетки
        val filePartMock = mockk<FilePart>()
        val dataBuffer = DefaultDataBufferFactory().wrap("fake image bytes".toByteArray())
        val mockMessage = mockk<Message>()
        val mockProps = MessageProperties()
        val responseMap = mapOf("result" to mapOf("ingredients" to listOf("Aqua", "Glycerin")))

        every { filePartMock.content() } returns Flux.just(dataBuffer)
        every { filePartMock.filename() } returns "label.jpg"

        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_JPEG
        every { filePartMock.headers() } returns headers

        // Явно указываем тип для any
        every { rabbitTemplate.convertAndSend("consistence.exchange", "consistence.request.bind", any<HairTypeRequest>()) } returns Unit

        // Настраиваем последовательность возвратов
        every { rabbitTemplate.receive("consistence.responses") } returns null andThen mockMessage

        every { mockMessage.messageProperties } returns mockProps
        every { rabbitTemplate.messageConverter.fromMessage(mockMessage) } returns responseMap

        val result = controller.analyzeConsistenceOfProduct(filePartMock, null)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                assert(response.body?.get("status") == "completed")
            }
            .verifyComplete()
    }

    @Test
    fun `анализ состава - ошибка при передаче и файла и текста`() {
        // Сценарий: ошибка при одновременной передаче файла и текста для анализа
        val filePartMock = mockk<FilePart>()
        val text = "Some text"
        val result = controller.analyzeConsistenceOfProduct(filePartMock, text)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.BAD_REQUEST)
                assert(response.body?.get("error") == "Provide either file or text.")
            }
            .verifyComplete()
    }

    @Test
    fun `анализ состава - ошибка при отсутствии и файла и текста`() {
        // Сценарий: ошибка когда не переданы ни файл, ни текст для анализа
        val result = controller.analyzeConsistenceOfProduct(null, null)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.BAD_REQUEST)
                assert(response.body?.get("error") == "Provide either file or text.")
            }
            .verifyComplete()
    }

    @Test
    fun `анализ состава - ошибка при неверном формате файла`() {
        // Сценарий: ошибка при загрузке файла неподдерживаемого формата (не изображение)
        val filePartMock = mockk<FilePart>()
        val dataBuffer = DefaultDataBufferFactory().wrap("fake text content".toByteArray())

        every { filePartMock.content() } returns Flux.just(dataBuffer)
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_PLAIN
        every { filePartMock.headers() } returns headers

        val result = controller.analyzeConsistenceOfProduct(filePartMock, null)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.BAD_REQUEST)
            }
            .verifyComplete()
    }

    @Test
    fun `анализ состава - таймаут при ожидании ответа от RabbitMQ`() {
        // Сценарий: превышение времени ожидания ответа от RabbitMQ при анализе состава
        val text = "Aqua, Sodium Laureth Sulfate"

        // Явно указываем тип для any
        every { rabbitTemplate.convertAndSend("consistence.exchange", "consistence.request.bind", any<HairTypeRequest>()) } returns Unit
        every { rabbitTemplate.receive("consistence.responses") } returns null

        val result = controller.analyzeConsistenceOfProduct(null, text)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.ACCEPTED)
                assert(response.body?.get("message") == "Analysis takes too much time")
            }
            .verifyComplete()
    }

    // Анализ пористости волос - позитивные и негативные тесты
    @Test
    fun `анализ пористости волос по изображению - успешный сценарий`() {
        // Сценарий: успешный анализ пористости волос по загруженному изображению
        val filePartMock = mockk<FilePart>()
        val dataBuffer = DefaultDataBufferFactory().wrap("hair image bytes".toByteArray())
        val mockMessage = mockk<Message>()
        val mockProps = MessageProperties()
        val responseMap = mapOf("result" to mapOf("porosity" to "high"))

        every { filePartMock.content() } returns Flux.just(dataBuffer)
        every { filePartMock.filename() } returns "hair.jpg"

        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_JPEG
        every { filePartMock.headers() } returns headers

        // Явно указываем тип для any
        every { rabbitTemplate.convertAndSend("hairType.exchange", "hairType.request.bind", any<HairTypeRequest>()) } returns Unit

        // Настраиваем последовательность возвратов
        every { rabbitTemplate.receive("hairType.responses") } returns null andThen mockMessage

        every { mockMessage.messageProperties } returns mockProps
        every { rabbitTemplate.messageConverter.fromMessage(mockMessage) } returns responseMap

        val result = controller.analyzeHairPorosity(filePartMock)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                assert(response.body?.get("status") == "completed")
            }
            .verifyComplete()
    }

    @Test
    fun `анализ пористости волос - ошибка при неверном формате файла`() {
        // Сценарий: ошибка при загрузке файла неподдерживаемого формата для анализа пористости
        val filePartMock = mockk<FilePart>()
        val dataBuffer = DefaultDataBufferFactory().wrap("text content".toByteArray())

        every { filePartMock.content() } returns Flux.just(dataBuffer)
        val headers = HttpHeaders()
        headers.contentType = MediaType.TEXT_PLAIN
        every { filePartMock.headers() } returns headers

        val result = controller.analyzeHairPorosity(filePartMock)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.BAD_REQUEST)
            }
            .verifyComplete()
    }

    @Test
    fun `анализ пористости волос - таймаут при ожидании ответа от RabbitMQ`() {
        // Сценарий: превышение времени ожидания ответа от RabbitMQ при анализе пористости
        val filePartMock = mockk<FilePart>()
        val dataBuffer = DefaultDataBufferFactory().wrap("hair image bytes".toByteArray())

        every { filePartMock.content() } returns Flux.just(dataBuffer)

        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_JPEG
        every { filePartMock.headers() } returns headers

        // Явно указываем тип для any
        every { rabbitTemplate.convertAndSend("hairType.exchange", "hairType.request.bind", any<HairTypeRequest>()) } returns Unit
        every { rabbitTemplate.receive("hairType.responses") } returns null

        val result = controller.analyzeHairPorosity(filePartMock)

        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.ACCEPTED)
                assert(response.body?.get("message") == "Analysis takes too much time")
            }
            .verifyComplete()
    }

    @Test
    fun `анализ пористости волос - ошибка при обработке изображения`() {
        // Сценарий: ошибка при обработке изображения (например, при чтении данных)
        val filePartMock = mockk<FilePart>()

        // Настраиваем возврат Flux с ошибкой
        every { filePartMock.content() } returns Flux.error(RuntimeException("Failed to read file"))

        val headers = HttpHeaders()
        headers.contentType = MediaType.IMAGE_JPEG
        every { filePartMock.headers() } returns headers

        // Используем try-catch для отлова исключения
        try {
            val result = controller.analyzeHairPorosity(filePartMock).block()
            assert(false) { "Должна быть ошибка" }
        } catch (e: Exception) {
            assert(e.message?.contains("Failed to read file") == true)
        }
    }
}

// Вспомогательная аннотация для моков
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class MockkBean