# microservices-kafka-mq

This proof of concept application demonstrates how to utilize Apache Kafka as messaging queue/bus between Java Spring based microservices to achieve event-driven architecture. 

## Publish-Subscribe Model
By means of Publish-Subscribe Model, multiple publishers publish messages to topics hosted by brokers. Those brokers can be subscribed to by multiple subscribers. A message is broadcasted to all the subscribers of a topic. In our demonstration we start only one broker. Subscribers of Kafka broker are our microservices(order and invocing).

![Kafka Consumers](https://forum.huawei.com/enterprise/en/data/attachment/forum/201907/27/155302nzdauhnw1qdih001.png?image.png "Overall Architecture")

Photo credit: [link](https://forum.huawei.com/enterprise/en/profile/2966821?type=posts)

## Topics
Order Microservice generating the work order puts `order` topic. Topic can has partitions to achieve the scability as explained above by means consumer groups. Number of partitions are given while creating the topic. 


## Application details
When order is placed by calling `@RestController` Post API, orderService is called. Spring service `orderService` updates order object, persists it into the relational DB and calls `kafkaTemplate.send(...)` function. Callback method annoted with `@KafkaListener(topics = "order")` in Spring component of consuming Invoice microservice is called when the message is put to order topic. After consuming the order object, ShipmentService ship function is called and acknowledge is returned. 

### spring-kafka-test
The Spring Kafka project comes with a `spring-kafka-test` Maven library that contains a number of useful utilities such as 
embedded Kafka broker, static methods to setup consumers/producers and to fetch results.the `EmbeddedKafkaBroker` of this library is provided for creating an embedded Kafka and an embedded Zookeeper server. For tests, an embedded Kafka server is used. In the testing flow, before executing the any tests we call `EmbeddedKafkaRule` constructor, it takes the following values as parameters.

- The number of Kafka servers to start.
- Whether a controlled shutdown is required.
- Topic(s) that need to be created on the server.

`@BeforeClass` and `@ClassRule` JUnit annotations used. The BeforeClass Junit annotation indicates that the static method to which is attached must be executed once and before all tests in the class. `@BeforeClass` configures Spring Kafka to use the embedded Kafka server. We use `@ClassRule` to instantiate `EmbeddedKafkaRule` class.

### spring-kafka
`spring-kafka` introduces Spring concepts to Kafka-based messaging solution development. High level abstraction for sending messages to Kafka is provided by `KafkaTemplate`. Application which will create work orders should insantiate `KafkaTemplate` by passing the message object and call send function. Other applications that are supposed to process work orders should register `@KafkaListener` callback. Spring Kafka support has the very similar model as the JMS support in the Spring Framework for RabbitMQ support in Spring AMQP.

### Message Object
The orders are written as serialized JSON and shared  between producer(order) and consumer(invoicing) microservices. Each microservice populates fields of Order class that are in the scope of their responsbility, so there is one common object travels between them. Order object  contains all the data required by other consumer microservice. Flexibility of JSON serialization enables Invoice service to read only releavant part of Order object which they are supposed to process.

### api/order
Producer application is exposing the api/order web service to accept orders. Swagger UI(`http://localhost:8080/swagger-ui.html`) was integrated to application to achieve simplicity of testing. API is protected with Basic Auth. Once the Order Producer App is executed, API user record is insterted to database by calling `UserTestDataGenerator.generateTestData()` function. User/Password for API authentication is `admin/admin`. Sample JSON data for OrderDTO :

```
{
  "billingAddress": {
    "city": "Denizli",
    "street": "Camlik 36",
    "zip": "35020"
  },
  "customerId": 1,
  "orderLines": [
    {
      "count": 2,
      "itemId": 4      
    }
  ],
  "shippingAddress": {
    "city": "Denizli",
    "street": "Camlik 36",
    "zip": "35020"
  }
}
```

### Request Timeout Handling
On producer microservice side, we decreased the request time in miliseconds to simulate the behaviour in case of unavailability of Kafka broker. onFailure callback is fired if send() does not return after 5 seconds. We handle this exception in RestController and return proper HTTP error code and messsage to API requstor. 

```java
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

```

### Acknowledge current offset in spring kafka by manual commit
In this demonstration we used manual commit on consumer side(Invoice microservice). In order to do this, we need to disable auto-commit and set ack-mode to manual in application.properties file 

Disable auto-commit:

`spring.kafka.consumer.enable-auto-commit=false`

Set the ack-mode to MANUAL:

`spring.kafka.listener.ack-mode=MANUAL`

Now we can commit the offset manually

```java
	@KafkaListener(topics = "order")
	public void order(Invoice invoice, Acknowledgment acknowledgment) {
		log.info("Received invoice " + invoice.getId());
		invoiceService.generateInvoice(invoice);
		acknowledgment.acknowledge();
	}

```
### JSON Serializer and Deserializer
Order Java Object as JSON byte[] is sent to a Kafka topic using Spring Kafka JsonSerializer on producer side. On consumer side, we use Spring Kafka JsonDeserializer to convert JSON byte[] to Java Object. Apache Kafka stores and transports byte[]. There are several built in serializers and deserializers but it doesn’t include any for JSON. To simply things, Spring Kafka created a JsonSerializer and JsonDeserializer which we can use to convert Java Objects to and from JSON.

```java
public class InvoiceDeserializer extends JsonDeserializer<Invoice> {

	public InvoiceDeserializer() {
		super(Invoice.class);
	}

}

```


## Notes about High Availability

### Consumer Groups
Consumer groups give Kafka the flexibility to have the advantages of both message queuing and publish-subscribe models.Kafka consumers belonging to the same consumer group share a group id. The consumers in a group then divides the topic partitions among themselves. Each partition is only consumed by a single consumer from the group. When multiple consumer groups exist, the flow of the data consumption model aligns with the traditional publish-subscribe model, so messages are broadcasted to all consumer groups.

### Multi-broker and Multi-node setup
For the high availability of the Kafka service, Kafka should run in cluster mode. Kafka multi-broker and replicated multi-node Zookeeper cluster setups should be implemented. If you configure Kafka for testing purposes you can run the different brokers on the same machine, however for redundancy it is recommended to run a production environment on multiple computers. To keep the cluster running even if one broker fails, it is advised to setup cluster with three Kafka brokers. Topics that to be created should be replicated on the three brokers using the `replication-factor`

```
/usr/local/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic replicated-topic
```
Here replication factor should be equal to the number of started Kafka Broker instances. Zookeeper keeps track of status of the Kafka cluster nodes and it also keeps track of Kafka topics, partitions etc. By means of Zookeeper multiple clients can perform simultaneous reads and writes and acts as a shared configuration service within the system. That's why availability of Zookeeper also must be secured using the cluster mode setup.

To check the status of the cluster:
```
/usr/local/kafka/bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic replicated-topic
Topic:replicated-topic    PartitionCount:1    ReplicationFactor:3    Configs:
Topic: replicated-topic    Partition: 0    Leader: 0    Replicas: 0,1,2    Isr: 0,1,2
```

Here the explanation for Leader, Replicas and Isr:

“Leader” is the responsible node for reads and writes on the given partition. Each node will be a leader for a randomly chosen part of partitions.

“Replicas” contains the list of nodes that replicate the log for this partition. This listing contains all nodes, no matter if they are the leader or if they are currently reachable (they might be out of sync).

“Isr” contains the set of “in-sync” replicas. This is the subset of replicas that are currently active and connected to the leader.

Apart from Kafka multi-broker setup, every production environment should have a replicated Zookepeer multi-node cluster. Nodes in the ZooKeeper cluster should work together as a quorum. Quorum refers to the minimum number of nodes that need to agree on a transaction before it’s committed. A quorum needs an odd number of nodes so that it can establish a majority. An even number of nodes may result in a tie, which would mean the nodes would not reach a majority or consensus.
