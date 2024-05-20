package ru.borshchevskiy.analyticsbuilderservice.integration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestRabbitConfig {

    private final String exchangeName = "scheduled-tasks-exchange";

    private final String queueName = "analytics-builder-scheduled-tasks-queue";

    private final String routingKey = "analytics-builder-task";

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchangeName, true, true);
    }

    @Bean
    public Queue analyticsBuilderQueue() {
        return new Queue(queueName, true, false, true);
    }

    @Bean
    public Binding analyticsBinding(DirectExchange directExchange, Queue analyticsBuilderQueue) {
        return BindingBuilder.bind(analyticsBuilderQueue)
                .to(directExchange)
                .with(routingKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory,
                                   DirectExchange exchange,
                                   Binding binding,
                                   Queue queue) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareExchange(exchange);
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        return rabbitAdmin;
    }
}

