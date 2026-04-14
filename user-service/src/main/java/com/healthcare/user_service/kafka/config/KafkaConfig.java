package com.healthcare.user_service.kafka.config;

import com.healthcare.user_service.kafka.constant.OffsetResetPolicy;
import com.healthcare.user_service.kafka.event.DomainEvent;
import com.healthcare.user_service.kafka.event.UserDeletedEvent;
import com.healthcare.user_service.kafka.event.UserRegisteredEvent;
import com.healthcare.user_service.kafka.event.UserUpdatedEvent;
import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import com.healthcare.user_service.kafka.service.KafkaKeyMessageService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaCustomProperties kafkaProperties;
    private final KafkaKeyMessageService keyMessageService;

    @Bean
    public ConsumerFactory<String, UserRegisteredEvent> userRegisteredConsumerFactory() {
        return buildConsumerFactory(
                UserRegisteredEvent.class);
    }

    @Bean
    public ConsumerFactory<String, UserUpdatedEvent> userUpdatedConsumerFactory() {
        return buildConsumerFactory(
                UserUpdatedEvent.class
        );
    }

    @Bean
    public ConsumerFactory<String, UserDeletedEvent> userDeletedConsumerFactory() {
        return buildConsumerFactory(
                UserDeletedEvent.class
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent>
    userRegisteredKafkaListenerContainerFactory() {
        return buildListenerContainerFactory(userRegisteredConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserUpdatedEvent>
    userUpdatedKafkaListenerContainerFactory() {
        return buildListenerContainerFactory(userUpdatedConsumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserDeletedEvent>
    userDeletedKafkaListenerContainerFactory() {
        return buildListenerContainerFactory(userDeletedConsumerFactory());
    }


    private <T> ConsumerFactory<String, T> buildConsumerFactory(Class<T> clazz) {
        return buildConsumerFactory(clazz, kafkaProperties.groups().userService().id());
    }

    private <T> ConsumerFactory<String, T> buildConsumerFactory(Class<T> clazz, String groupId) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(clazz, false);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.bootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetPolicy.EARLIEST.value());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    private <T extends DomainEvent>
    ConcurrentKafkaListenerContainerFactory<String, T> buildListenerContainerFactory(
            ConsumerFactory<String, T> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setRecordFilterStrategy(record ->
                !keyMessageService.getKeys().contains(record.value().eventId()));
        return factory;
    }
}