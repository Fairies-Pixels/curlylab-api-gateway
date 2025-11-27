package com.curlylab.curlylabApiGateway

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@RestController
class ApiGatewayController (
    @Autowired
    val rabbitTemplate: RabbitTemplate,
    val webClient: WebClient,
    val backendURI: String = "http://localhost:8081"
) {
    // Products
    @GetMapping("/products")
    fun getAllProducts(): Mono<ResponseEntity<Any>> {
        return webClient.get()
            .uri("$backendURI/products")
            .retrieve()
            .toEntity(Any::class.java)
    }

    @GetMapping("/products/{id}")
    fun getProduct(@PathVariable id: UUID): Mono<ResponseEntity<Any>> {
        return webClient.get()
            .uri("$backendURI/products/$id")
            .retrieve()
            .toEntity(Any::class.java)
    }

    // Users
    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: UUID): Mono<ResponseEntity<Any>> {
        return webClient.get()
            .uri("$backendURI/users/$id")
            .retrieve()
            .toEntity(Any::class.java)
    }

    @PostMapping("/users")
    fun createUser(@RequestBody user: Map<String, Any>): Mono<ResponseEntity<String>> {
        return webClient.post()
            .uri("$backendURI/users")
            .bodyValue(user)
            .retrieve()
            .bodyToMono(String::class.java)
            .map {responseBody -> ResponseEntity.ok(responseBody)}
    }

    @PutMapping("/users/{id}")
    fun updateUser(@PathVariable id: UUID, @RequestBody user: Map<String, Any>): Mono<ResponseEntity<Any>> {
        return webClient.put()
            .uri("$backendURI/users/$id")
            .bodyValue(user)
            .retrieve()
            .toEntity(Any::class.java)
            .onErrorResume { error ->
                Mono.just(ResponseEntity.status(500).body(mapOf(
                    "error" to "Failed to update user (id=$id): ${error.message}"
                )))
            }
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: UUID): Mono<ResponseEntity<String>> {
        return webClient.delete()
            .uri("$backendURI/users/$id")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody)}
    }

    // HairTypes
    @GetMapping("/haritypes/{userId}")
    fun getHairType(@PathVariable userId: UUID): Mono<ResponseEntity<Any>> {
        return webClient.get()
            .uri("$backendURI/hairtypes/$userId")
            .retrieve()
            .toEntity(Any::class.java)
    }

    @PostMapping("/hairtypes")
    fun createHairType(@RequestBody hairType: Map<String, Any>): Mono<ResponseEntity<String>> {
        return webClient.post()
            .uri("$backendURI/hairtypes")
            .bodyValue(hairType)
            .retrieve()
            .bodyToMono(String::class.java)
            .map {responseBody -> ResponseEntity.ok(responseBody)}
    }

    @PutMapping("/hairtypes/{userId}")
    fun updateHairType(@PathVariable userId: UUID, @RequestBody hairType: Map<String, Any>): Mono<ResponseEntity<Any>> {
        return webClient.put()
            .uri("$backendURI/hairtypes/$userId")
            .bodyValue(hairType)
            .retrieve()
            .toEntity(Any::class.java)
    }

    @DeleteMapping("/hairtypes/{userId}")
    fun deleteHairType(@PathVariable userId: UUID): Mono<ResponseEntity<Any>> {
        return webClient.delete()
            .uri("$backendURI/hairtypes/$userId")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody)}
    }

    // Reviews
    @GetMapping("/products/{product_id}/reviews")
    fun getAllReviews(@PathVariable product_id: UUID): Mono<ResponseEntity<Any>> {
        return webClient.get()
            .uri("$backendURI/products/$product_id/reviews")
            .retrieve()
            .toEntity(Any::class.java)
    }

    @PostMapping("/products/{product_id}/reviews")
    fun createReview(@PathVariable product_id: UUID, @RequestBody review: Map<String, Any>): Mono<ResponseEntity<String>> {
        return webClient.post()
            .uri("$backendURI/products/$product_id/reviews")
            .bodyValue(review)
            .retrieve()
            .bodyToMono(String::class.java)
            .map {responseBody -> ResponseEntity.ok(responseBody)}
    }

    @PutMapping("/products/{product_id}/reviews/{review_id}")
    fun updateReview(@PathVariable product_id: UUID, @PathVariable review_id: UUID, @RequestBody review: Map<String, Any>): Mono<ResponseEntity<Any>> {
        return webClient.put()
            .uri("$backendURI/products/$product_id/reviews/$review_id")
            .bodyValue(review)
            .retrieve()
            .toEntity(Any::class.java)
    }

    @DeleteMapping("/products/{product_id}/reviews/{review_id}")
    fun deleteReview(@PathVariable product_id: UUID, @PathVariable review_id: UUID): Mono<ResponseEntity<String>> {
        return webClient.delete()
            .uri("$backendURI/products/$product_id/reviews/$review_id")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody)}
    }

    // Favourites
    @GetMapping("/users/{user_id}/favourites")
    fun getUserFavourites(@PathVariable user_id: UUID): Mono<ResponseEntity<Any>> {
        return webClient.get()
            .uri("$backendURI/users/$user_id/favourites")
            .retrieve()
            .toEntity(Any::class.java)
    }

    @GetMapping("/products/{product_id}/is_favourite/{user_id}")
    fun getProductUsers(@PathVariable user_id: UUID, @PathVariable product_id: UUID): Mono<ResponseEntity<Boolean>> {
        return webClient.get()
            .uri("$backendURI/products/$product_id/is_favourite/$user_id")
            .retrieve()
            .toEntity(Boolean::class.java)
    }

    @PostMapping("/users/{user_id}/favourites")
    fun addToFavourites(@PathVariable user_id: UUID, @RequestBody favourite: Map<String, Any>): Mono<ResponseEntity<String>> {
        return webClient.post()
            .uri("$backendURI/users/$user_id/favourites")
            .bodyValue(favourite)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody) }
    }

    @DeleteMapping("/users/{user_id}/favourites/{product_id}")
    fun deleteFavourite(@PathVariable user_id: UUID, @PathVariable product_id: UUID): Mono<ResponseEntity<String>> {
        return webClient.delete()
            .uri("$backendURI/users/$user_id/favourites/$product_id")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody)}
    }

    // Consistence AI
    @PostMapping("/composition/analyze")
    fun analyzeConsistenceOfProduct(@RequestPart("file") file: FilePart): Mono<ResponseEntity<Map<String, Any>>> {
        return file.content()
            .collectList()
            .flatMap { dataBuffers ->
                try {
                    val bytes = DataBufferUtils.join(Flux.fromIterable(dataBuffers))
                        .map { dataBuffer ->
                            val bytes = ByteArray(dataBuffer.readableByteCount())
                            dataBuffer.read(bytes)
                            DataBufferUtils.release(dataBuffer)
                            bytes
                        }
                    bytes.flatMap { imageBytes ->
                        file.headers().contentType?.toString()?.startsWith("image/")?.let {
                            if (!it == true) {
                                return@flatMap Mono.just(
                                    ResponseEntity.badRequest().body(mapOf("error" to "File must be an image"))
                                )
                            }
                        }

                        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
                        val request = HairTypeRequest(file = base64Image)

                        rabbitTemplate.convertAndSend(
                            "consistence.exchange",
                            "consistence.request.bind",
                            request
                        )

                        rabbitMqPulling()
                    }
                } catch (e: Exception) {
                    Mono.just(
                        ResponseEntity.status(500).body(mapOf("error" to "Failed to process image: ${e.message}"))
                    )
                }
            }

    }

    @GetMapping("/composition/analyze")
    fun getResponseConsistenceOfProduct(): ResponseEntity<Any> {
        return try {
            val message = rabbitTemplate.receive("consistence.responses")

            if (message != null) {
                val response = rabbitTemplate.messageConverter.fromMessage(message) as? Map<*,*>
                ResponseEntity.ok(mapOf(
                    "status" to "completed",
                    "result" to response
                ))
            } else {
                ResponseEntity.status(202).body(mapOf(
                    "status" to "processing",
                    "message" to "Analysis still in progress"
                ))
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf("error" to "Failed to get result: ${e.message}"))
        }
    }

    private fun rabbitMqPulling(): Mono<ResponseEntity<Map<String, Any>>> {
        return Flux.interval(Duration.ofMillis(500))
            .take(10)
            .flatMap { attempt ->
                Mono.fromCallable {
                    rabbitTemplate.receive("consistence.responses")
                }.map { message ->
                    message to attempt
                }
            }
            .takeUntil { (message, _) -> message != null }
            .last()
            .flatMap { (message, attempt) ->
                if (message != null) {
                    val response = rabbitTemplate.messageConverter.fromMessage(message) as? Map<*, *>
                    Mono.just(
                        ResponseEntity.ok(mapOf(
                            "status" to "completed",
                            "result" to response
                        ) as Map<String, Any>)
                    )
                } else {
                    Mono.delay(Duration.ofMillis(500))
                        .then(rabbitMqPulling())
                }
            }
            .timeout(Duration.ofSeconds(5))
            .onErrorResume { timeoutException ->
                Mono.just(
                    ResponseEntity.accepted().body(mapOf(
                        "status" to timeoutException,
                        "message" to "Analysis takes too much time"
                    ))
                )
            }
    }
}