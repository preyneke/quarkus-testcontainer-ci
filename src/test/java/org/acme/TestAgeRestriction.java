package org.acme;

import java.util.Collections;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import org.junit.jupiter.api.Test;
import org.jboss.logging.Logger;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

@QuarkusTest
@QuarkusTestResource(TestResource.class)
public class TestAgeRestriction {

    private static final Logger logger = Logger.getLogger(AgeRestrict.class);
    private static final int MAX_FETCH_IF_ZERO = 5;

    @Test
    public void testUnderage() {

        Producer<Integer, Customer> producer = createCustomerProducer("customers");

        logger.info("Sending customer records");

        // Putting this thread to sleep to allow consumer thread to catch up.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        producer.send(new ProducerRecord<>("customers", 101, new Customer(101, "Abby", 17)));
        producer.send(new ProducerRecord<>("customers", 102, new Customer(202, "Brooke", 42)));
        producer.send(new ProducerRecord<>("customers", 103, new Customer(303, "Crystal", 31)));
        producer.send(new ProducerRecord<>("customers", 104, new Customer(404, "Diana", 51)));
        producer.send(new ProducerRecord<>("customers", 101, new Customer(505, "Ellis", 16)));
        producer.send(new ProducerRecord<>("customers", 102, new Customer(606, "Fiona", 22)));
        producer.send(new ProducerRecord<>("customers", 103, new Customer(707, "Gabby", 33)));
        producer.send(new ProducerRecord<>("customers", 104, new Customer(808, "Hannah", 29)));

        Consumer<Integer, Customer> underage_consumer = createConsumer("underage");
        logger.info("Consuming underage  customer records.....");
        List<ConsumerRecord<Integer, Customer>> underage_records = poll(underage_consumer, 2);

        logger.info("Underage consumed records:" + underage_records.size());
        underage_records.forEach((record) -> logger.info("--->" + record.value()));

        producer.close();
        underage_consumer.close();
å
        assertEquals(2, underage_records.size());

    }

    private static Producer<Integer, Customer> createCustomerProducer(String topicName) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, TestResource.getBootstrapServers());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "-customer-test");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 500);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ObjectMapperSerializer.class.getName());
        KafkaProducer<Integer, Customer> producer = new KafkaProducer<>(props);
        producer.partitionsFor(topicName);
        return producer;
    }

    private static KafkaConsumer<Integer, Customer> createConsumer(String topicName) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestResource.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, topicName + "-test");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomerDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<Integer, Customer> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topicName));
        return consumer;
    }

    private List<ConsumerRecord<Integer, Customer>> poll(Consumer<Integer, Customer> consumer,
            int expectedRecordCount) {
        int fetched = 0;
        int retry = 0;
        List<ConsumerRecord<Integer, Customer>> result = new ArrayList<>();
        while (fetched < expectedRecordCount) {
            ConsumerRecords<Integer, Customer> records = consumer.poll(Duration.ofMillis(1000));
            records.forEach(result::add);
            fetched = result.size();
            if (retry < MAX_FETCH_IF_ZERO) {
                retry++;
                logger.info("Retry fetch:[" + fetched + "] expected record:[" + expectedRecordCount + "]: retry:["
                        + retry + "]");
            } else
                break;
        }

        return result;
    }

    public static class CustomerDeserializer extends ObjectMapperDeserializer<Customer> {

        public CustomerDeserializer() {
            super(Customer.class);
        }
    }

}
