package com.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}

@EnableKafka
@Configuration
class Config {

    @Value("${vcap.services.k1.credentials.bootstrap_servers:10.244.0.3:9092}")
    private String kafkaConnectionString;

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<String, String>(producerFactory());
    }

	private  DefaultKafkaProducerFactory<String, String> producerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionString);
		props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return new DefaultKafkaProducerFactory<>(props);
	}

	// Consumer

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        ContainerProperties props = factory.getContainerProperties();
        props.setAckMode(AckMode.MANUAL_IMMEDIATE);
        props.setIdleEventInterval(100L);
        factory.setRecordFilterStrategy(manualFilter());
        factory.setAckDiscarded(true);
        factory.setRetryTemplate(new RetryTemplate());
        factory.setRecoveryCallback(new RecoveryCallback<Void>() {

            @Override
            public Void recover(RetryContext context) throws Exception {
                return null;
            }

        });
        return factory;
    }

    @Bean
    public RecordFilterImpl manualFilter() {
        return new RecordFilterImpl();
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
         return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConnectionString);
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return propsMap;
    }

    @Bean
    public Listener listener() {
        return new Listener();
    }

}

class RecordFilterImpl implements RecordFilterStrategy<String, String> {

    final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();

    public String greet() {
        String name = "no greetings";
        if (records.peek() != null) {
            ConsumerRecord<String, String> record = records.poll();
            name = record.value();
            System.out.println(record);
        }
        return name;
    }

    @Override
    public boolean filter(ConsumerRecord<String, String> record) {
        System.out.println("Filtering Record: " + record);
        records.add(record);
        return false;
    }

}

@RestController
class DemoController {

	@Autowired
	KafkaTemplate kafkaTemplate;

    @Autowired
    RecordFilterImpl messages;

	@RequestMapping(method = RequestMethod.GET, value = "/hi/{name}")
	Map<String, Object> hi(@PathVariable String name) {
		kafkaTemplate.send("topic1", 0, name);
        kafkaTemplate.flush();
		return Collections.singletonMap("greeting", "Hi " + name + "!");
	}

	@RequestMapping(method = RequestMethod.GET, value = "/bye")
    Map<String, Object> bye() {
        return Collections.singletonMap("greeting", "Hi " + messages.greet() + "!");
 	}

}
