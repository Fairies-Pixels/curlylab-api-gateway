package com.curlylab.curlylabApiGateway.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {

    // Request Queues
    @Bean
    fun hairTypeQueue(): Queue {
        return Queue("hairType.requests", true)
    }
    @Bean
    fun consistenceQueue(): Queue {
        return Queue("consistence.requests", true)
    }

    // Response Queues
    @Bean
    fun hairTypeResponseQueue(): Queue {
        return Queue("hairType.responses", true)
    }
    @Bean
    fun consistenceResponseQueue(): Queue {
        return Queue("consistence.responses", true)
    }

    // Exchanges
    @Bean
    fun hairTypeDirectExchange(): DirectExchange {
        return DirectExchange("hairType.exchange")
    }
    @Bean
    fun consistenceDirectExchange(): DirectExchange {
        return DirectExchange("consistence.exchange")
    }

    // Request bindings
    @Bean
    fun hairTypeBinding(): Binding {
        return BindingBuilder.bind(hairTypeQueue())
            .to(hairTypeDirectExchange())
            .with("hairType.request.bind")
    }

    @Bean
    fun consistenceBinding(): Binding {
        return BindingBuilder.bind(consistenceQueue())
            .to(consistenceDirectExchange())
            .with("consistence.request.bind")
    }

    // Response bindings
    @Bean
    fun hairTypeResponseBinding(): Binding {
        return BindingBuilder.bind(hairTypeResponseQueue())
            .to(hairTypeDirectExchange())
            .with("hairType.response.bind")
    }

    @Bean
    fun consistenceResponseBinding(): Binding {
        return BindingBuilder.bind(consistenceResponseQueue())
            .to(consistenceDirectExchange())
            .with("consistence.response.bind")
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = Jackson2JsonMessageConverter()
        return template
    }
}