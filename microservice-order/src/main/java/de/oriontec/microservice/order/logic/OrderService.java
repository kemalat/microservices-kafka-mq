package de.oriontec.microservice.order.logic;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class OrderService {

	private OrderRepository orderRepository;

	private KafkaTemplate<String, Order> kafkaTemplate;

	@Autowired
	private OrderService(OrderRepository orderRepository, KafkaTemplate<String, Order> kafkaTemplate) {
		super();
		this.orderRepository = orderRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	public Order order(Order order) throws KafkaException {
		if (order.getNumberOfLines() == 0) {
			throw new IllegalArgumentException("No order lines!");
		}
		order.setUpdated(new Date());
		Order result = orderRepository.save(order);
		fireOrderCreatedEvent(order);
		return result;
	}

	private void fireOrderCreatedEvent(Order order) throws KafkaException {

		final Throwable[] result = {null};
		ListenableFuture<SendResult<String, Order>> future = kafkaTemplate
					.send("order", order.getId() + "created", order);

		future.addCallback(new ListenableFutureCallback<SendResult<String, Order>>() {

			@Override
			public void onSuccess(SendResult<String, Order> stringOrderSendResult) {
				System.out.println("onSuccess");
			}

			@Override
			public void onFailure(Throwable throwable) {
				System.out.println("onFailure");
				result[0] = throwable.getCause();
			}
		});

		if(result[0] != null)
			throw new KafkaException(result[0].getMessage(),result[0].getCause());

	}

	public double getPrice(long orderId) {
		return orderRepository.findById(orderId).get().totalPrice();
	}

}
