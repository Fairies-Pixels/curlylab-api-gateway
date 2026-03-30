package com.curlylab.curlylabApiGateway

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException::class)
    fun handleBackendErrors(ex: RuntimeException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to (ex.message ?: "Unknown backend error")))
    }
}

@RestController
class ApiGatewayController (
    @Autowired
    val rabbitTemplate: RabbitTemplate,
    val webClient: WebClient,
    @Value("\${backend.uri:http://curlylab-backend-service:8080}")
    private val backendURI: String
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

    @PostMapping(value = ["/users/{id}/upload_image"], consumes = [MediaType.ALL_VALUE])
    fun uploadUserAvatar(@PathVariable id: UUID, @RequestPart("file") avatar: FilePart): Mono<ResponseEntity<String>> {
        println("upload_image")
        return DataBufferUtils.join(avatar.content())
            .flatMap { dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)

                val boundary = "----WebKitFormBoundary" + System.currentTimeMillis()

                val body = StringBuilder()
                    .append("--").append(boundary).append("\r\n")
                    .append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(avatar.filename()).append("\"\r\n")
                    .append("Content-Type: ")
                    .append(avatar.headers().contentType?.toString() ?: "image/png")
                    .append("\r\n\r\n")
                    .toString().toByteArray() + bytes +
                        "\r\n--${boundary}--\r\n".toByteArray()

                val headers = HttpHeaders().apply {
                    contentType = MediaType.parseMediaType("multipart/form-data; boundary=$boundary")
                }

                val entity = HttpEntity(body, headers)

                webClient.post()
                    .uri("$backendURI/users/$id/upload_image")
                    .header(HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=$boundary")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .map { ResponseEntity.ok(it) }
            }
    }

    @DeleteMapping("/users/{id}/avatar")
    fun deleteUserAvatar(@PathVariable id: UUID): Mono<ResponseEntity<String>> {
        return webClient.delete()
            .uri("$backendURI/users/$id/avatar")
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody)}
    }

    // HairTypes
    @GetMapping("/hairtypes/{userId}")
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

    // Auth
    @PostMapping("/auth/register")
    fun register(@RequestBody registerRequest: Map<String, Any>): Mono<ResponseEntity<String>> {
        return webClient.post()
            .uri("$backendURI/auth/register")
            .bodyValue(registerRequest)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody) }
    }

    @PostMapping("/auth/login")
    fun login(@RequestBody loginRequest: Map<String, Any>): Mono<ResponseEntity<String>> {
        return webClient.post()
            .uri("$backendURI/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(loginRequest)
            .retrieve()
            .onStatus({ status -> status == HttpStatus.BAD_REQUEST }) { response ->
                // ВАЖНО: получаем тело ошибки от backend
                response.bodyToMono(String::class.java)
                    .flatMap { errorBody ->
                        println("=== BACKEND 400 ERROR DETAILS ===")
                        println("Status: ${response.statusCode()}")
                        println("Headers: ${response.headers()}")
                        println("Body: $errorBody")
                        println("=== END ERROR DETAILS ===")

                        // Пробрасываем ошибку с деталями
                        Mono.error(RuntimeException("Backend validation failed: $errorBody"))
                    }
            }
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody) }
    }

    @PostMapping("/auth/google")
    fun google(@RequestBody googleRequest: Map<String, Any>): Mono<ResponseEntity<String>> {
        return webClient.post()
            .uri("$backendURI/auth/google")
            .bodyValue(googleRequest)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> ResponseEntity.ok(responseBody) }
    }

    // Composition AI
    @PostMapping("/composition/analyze")
    fun analyzeConsistenceOfProduct(
        @RequestPart(value = "file", required = false) file: FilePart?,
        @RequestPart(value = "text", required = false) text: String?
    ): Mono<ResponseEntity<Map<String, Any>>> {
        return Mono.defer {
           val hasFile = file != null
           var hasText = text != null

            when {
                (hasFile && hasText) || !(hasFile || hasText) -> {
                    return@defer Mono.just(
                        ResponseEntity.badRequest().body(
                            mapOf("error" to "Provide either file or text.")
                        )
                    )
                }
                hasFile -> {
                    return@defer file!!.content()
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

                                    rabbitMqPolling("consistence.responses")
                                }
                            } catch (e: Exception) {
                                Mono.just(
                                    ResponseEntity.status(500).body(mapOf("error" to "Failed to process image: ${e.message}"))
                                )
                            }
                        }
                }
                else -> {
                       return@defer try {
                            val request = HairTypeRequest(text = text)
                            rabbitTemplate.convertAndSend(
                                "consistence.exchange",
                                "consistence.request.bind",
                                request
                            )
                            rabbitMqPolling("consistence.responses")
                        } catch (e: Exception) {
                            Mono.just(
                                ResponseEntity.status(500).body(mapOf("error" to "Failed to process image: ${e.message}"))
                            )
                        }
                    }
            }
        }
    }

    // Hair's porosity AI
    @PostMapping("/analyze")
    fun analyzeHairPorosity(@RequestPart("file") file: FilePart): Mono<ResponseEntity<Map<String, Any>>> {
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
                            "hairType.exchange",
                            "hairType.request.bind",
                            request
                        )

                        rabbitMqPolling("hairType.responses")
                    }
                } catch (e: Exception) {
                    Mono.just(
                        ResponseEntity.status(500).body(mapOf("error" to "Failed to process image: ${e.message}"))
                    )
                }
            }
    }

    private fun rabbitMqPolling(responseQueueName: String): Mono<ResponseEntity<Map<String, Any>>> {
        return Flux.interval(Duration.ofMillis(500))
            .take(10)
            .flatMap { attempt ->
                Mono.fromCallable {
                    rabbitTemplate.receive(responseQueueName)
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
                        .then(rabbitMqPolling(responseQueueName))
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
