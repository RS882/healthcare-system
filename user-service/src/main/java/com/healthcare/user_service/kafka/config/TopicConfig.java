package com.healthcare.user_service.kafka.config;

import com.healthcare.user_service.kafka.properties.KafkaCustomProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class TopicConfig {

    private final KafkaCustomProperties kafkaProperties;

    @Bean
    public NewTopic userRegisteredTopic() {
        return buildTopic(kafkaProperties.topics().userRegistered());
    }

    @Bean
    public NewTopic userUpdatedTopic() {
        return buildTopic(kafkaProperties.topics().userUpdated());
    }

    @Bean
    public NewTopic userDeletedTopic() {
        return buildTopic(kafkaProperties.topics().userDeleted());
    }

    private NewTopic buildTopic(KafkaCustomProperties.TopicProperties topic) {
        return TopicBuilder.name(topic.name())
                .partitions(topic.partitions())
                .replicas(topic.replicas())
                .build();
    }
}
