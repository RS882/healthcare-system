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

    private static final String DLT_SUFFIX = ".DLT";

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

    @Bean
    public NewTopic userRegisteredDltTopic() {
        return buildDltTopic(kafkaProperties.topics().userRegistered());
    }

    private NewTopic buildTopic(KafkaCustomProperties.TopicProperties topic) {
        return buildTopic(topic, false);
    }

    private NewTopic buildDltTopic(KafkaCustomProperties.TopicProperties topic) {
        return buildTopic(topic, true);
    }

    private NewTopic buildTopic(KafkaCustomProperties.TopicProperties topic, boolean dlt) {
        String topicName = dlt ? topic.name() + DLT_SUFFIX : topic.name();

        return TopicBuilder.name(topicName)
                .partitions(topic.partitions())
                .replicas(topic.replicas())
                .build();
    }
}
