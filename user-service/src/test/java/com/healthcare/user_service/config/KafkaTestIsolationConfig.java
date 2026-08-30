package com.healthcare.user_service.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;

@TestConfiguration(proxyBeanMethods = false)
public class KafkaTestIsolationConfig {

    @Bean
    static BeanPostProcessor kafkaProducerFactoryIsolationPostProcessor(
            @Value("${test.kafka.transaction-id-prefix}") String transactionIdPrefix
    ) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(
                    Object bean,
                    String beanName
            ) {
                if (bean instanceof DefaultKafkaProducerFactory<?, ?> producerFactory
                        && producerFactory.transactionCapable()) {

                    producerFactory.setTransactionIdPrefix(
                            transactionIdPrefix + beanName + "-"
                    );
                }

                return bean;
            }
        };
    }
}
