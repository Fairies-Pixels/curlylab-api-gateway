package com.curlylab.curlylabApiGateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CurlylabApiGatewayApplication

fun main(args: Array<String>) {
	runApplication<CurlylabApiGatewayApplication>(*args)
}
