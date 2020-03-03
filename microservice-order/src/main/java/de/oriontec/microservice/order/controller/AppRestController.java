package de.oriontec.microservice.order.controller;

import de.oriontec.microservice.order.dto.EngineResponseDTO;
import de.oriontec.microservice.order.dto.OrderDTO;
import de.oriontec.microservice.order.dto.OrderLineDTO;
import de.oriontec.microservice.order.exception.CustomerNotFoundException;
import de.oriontec.microservice.order.customer.CustomerRepository;
import de.oriontec.microservice.order.item.ItemRepository;
import de.oriontec.microservice.order.logic.Address;
import de.oriontec.microservice.order.logic.Order;
import de.oriontec.microservice.order.logic.OrderLine;
import de.oriontec.microservice.order.logic.OrderRepository;
import de.oriontec.microservice.order.logic.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.KafkaException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Api(value = "Backend Service", description = "Backend Service API", authorizations = {@Authorization(value = "basicAuth")})
class AppRestController {

	private OrderRepository orderRepository;

	private OrderService orderService;

	private CustomerRepository customerRepository;
	private ItemRepository itemRepository;

	@Autowired
	private AppRestController(OrderService orderService, OrderRepository orderRepository,
			CustomerRepository customerRepository, ItemRepository itemRepository) {
		super();
		this.orderRepository = orderRepository;
		this.customerRepository = customerRepository;
		this.itemRepository = itemRepository;
		this.orderService = orderService;
	}


	@RequestMapping(value = "/order", method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Create Order and write it to Kafka Topic")
	public EngineResponseDTO post(@RequestBody OrderDTO orderDTO) throws CustomerNotFoundException, KafkaException {

    List<OrderLine> orderLines = orderDTO.getOrderLines().stream()
        .map(orderLineDTO -> toEntity(orderLineDTO)).collect(Collectors.toList());

    Order order = new Order(customerRepository.findById(orderDTO.getCustomerId()).orElseThrow(() -> new CustomerNotFoundException("No user found with customer Id " + orderDTO.getCustomerId())));
    order.setShippingAddress(new Address(orderDTO.getShippingAddress().getStreet(),orderDTO.getShippingAddress().getZip(),orderDTO.getShippingAddress().getCity()));
    order.setBillingAddress(new Address(orderDTO.getBillingAddress().getStreet(),orderDTO.getBillingAddress().getZip(),orderDTO.getBillingAddress().getCity()));
    order.setOrderLine(orderLines);
    orderService.order(order);

    return EngineResponseDTO.of(true, 200, "Order Received Succesfully.");
	}

	@RequestMapping(value = "/order", method = RequestMethod.GET)
	@ApiOperation(value = "Verify Scenario Template")
	public EngineResponseDTO get(OrderDTO orderDTO) {

		return EngineResponseDTO.of(true, 200, "Success");
	}

	private OrderLine toEntity(OrderLineDTO orderLineDTO) {
	  return new OrderLine(orderLineDTO.getCount(),itemRepository.findById(orderLineDTO.getItemId()).get());

	}

  @ExceptionHandler({ CustomerNotFoundException.class })
  public ResponseEntity<String> handleException(CustomerNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);

  }

  @ExceptionHandler({ KafkaException.class })
  public ResponseEntity<String> handleException2(KafkaException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.GATEWAY_TIMEOUT);

  }


}
