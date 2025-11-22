package com.curlylab.curlylabApiGateway

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
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

}