package com.curlylab.curlylabApiGateway

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class FavouriteIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var webTestClient: WebTestClient

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { "jdbc:postgresql://localhost:5432/curlylab" }
            registry.add("spring.datasource.username") { "postgres" }
            registry.add("spring.datasource.password") { "s1p2u3e4r" } // пароль из твоего файла
            registry.add("spring.datasource.driver-class-name") { "org.postgresql.Driver" }
            registry.add("backend.uri") { "http://localhost:8081" } // URL бэкенда
        }
    }

    @BeforeEach
    fun setUp() {
        webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }

    @Test
    fun `should add product to favourites and verify it appears in list`() {
        val userId = UUID.randomUUID()
        val productId = "a0997b2e-d322-487e-b99f-b23f4033ae5a"// Из имеющейся базы

        createTestUser(userId)

        val addFavouriteResponse = webTestClient.post()
            .uri("/users/$userId/favourites")
            .bodyValue(mapOf(
                "productId" to productId.toString()
            ))
            .exchange()
            .expectStatus().isOk
            .expectBody(String::class.java)
            .returnResult()
            .responseBody

        assert(addFavouriteResponse == "Favourite product has added!")

        val favouritesList = webTestClient.get()
            .uri("/users/$userId/favourites")
            .exchange()
            .expectStatus().isOk
            .expectBody(List::class.java)
            .returnResult()
            .responseBody

        assert(favouritesList != null)

        @Suppress("UNCHECKED_CAST")
        val favouriteProductIds = favouritesList
            ?.filterIsInstance<Map<String, Any>>()
            ?.map { it["productId"]?.toString() }

        assert(favouriteProductIds?.contains(productId.toString()) == true)
    }

    private fun createTestUser(userId: UUID) {
        webTestClient.post()
            .uri("/users")
            .bodyValue(mapOf(
                "id" to userId.toString(),
                "username" to "testuser_$userId",
                "createdAt" to "2024-01-01T00:00:00"
            ))
            .exchange()
            .expectStatus().isOk
    }

}