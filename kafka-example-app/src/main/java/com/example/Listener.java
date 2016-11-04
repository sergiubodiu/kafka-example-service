package com.example;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.concurrent.CountDownLatch;

public class Listener {

	public final CountDownLatch countDownLatch = new CountDownLatch(1);

//	@KafkaListener(id = "foo", topics = "topic1", group = "group1")
//    public void listen(ConsumerRecord<?, ?> record) {
//		System.out.println(record);
//		countDownLatch1.countDown();
//	}


	@KafkaListener(id = "foo", topics = "topic1", group = "group1")
	public void manualStart(@Payload String foo, Acknowledgment ack) {
		ack.acknowledge();
		System.out.println(foo);
		this.countDownLatch.countDown();
	}

}