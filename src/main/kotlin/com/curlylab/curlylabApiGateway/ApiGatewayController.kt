package com.curlylab.curlylabApiGateway

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

@RestController
class ApiGatewayController (
    @Autowired
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
            .bodyValue(loginRequest)
            .retrieve()
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

}