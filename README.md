# microservices-kafka-mq

This proof of concept application demonstrates how to utilize Apache Kafka as messaging queue/bus between Java Spring based microservices to achieve event-driven architecture. 

## spring-kafka

`spring-kafka` provides Kafka-based messaging solution development using the Spring concepts. High level abstraction for sending messages to Kafka is provided by `KafkaTemplate`. Application which will create work orders should insantiate `KafkaTemplate` by passing the message object and call send function. Other applications that are supposed to process work orders should register `@KafkaListener` callback. Spring Kafka support has the very similar model as the JMS support in the Spring Framework for RabbitMQ support in Spring AMQP.


## Publish-Subscribe Model
By means of Publish-Subscribe Model, multiple publishers publish messages to topics hosted by brokers. Those brokers can be subscribed to by multiple subscribers. A message is broadcasted to all the subscribers of a topic.

## Consumer Groups
Consumer groups give Kafka the flexibility to have the advantages of both message queuing and publish-subscribe models.Kafka consumers belonging to the same consumer group share a group id. The consumers in a group then divides the topic partitions among themselves. Each partition is only consumed by a single consumer from the group. When multiple consumer groups exist, the flow of the data consumption model aligns with the traditional publish-subscribe model, so messages are broadcasted to all consumer groups.

![Kafka Consumers](https://forum.huawei.com/enterprise/en/data/attachment/forum/201907/27/155302nzdauhnw1qdih001.png?image.png "Overall Architecture")

Photo credit: [link](https://forum.huawei.com/enterprise/en/profile/2966821?type=posts)

## Topics
Microservice generating the work order puts `order` topic. Topic has partitions to achieve the scability as explained above by means consumer groups.

## spring-kafka-test
The Spring Kafka project comes with a `spring-kafka-test` Maven library that contains a number of useful utilities such as 
embedded Kafka broker, static methods to setup consumers/producers and to fetch results.the `EmbeddedKafkaBroker` of this library is provided for creating an embedded Kafka and an embedded Zookeeper server. For tests, an embedded Kafka server is used. In the testing flow, before executing the any tests we call `EmbeddedKafkaRule` constructor, it takes the following values as parameters.

- The number of Kafka servers to start.
- Whether a controlled shutdown is required.
- Topic(s) that need to be created on the server.

`@BeforeClass` and `@ClassRule` JUnit annotations used. The BeforeClass Junit annotation indicates that the static method to which is attached must be executed once and before all tests in the class. `@BeforeClass` configures Spring Kafka to use the embedded Kafka server. We use `@ClassRule` to instantiate `EmbeddedKafkaRule` class.

## Message Object
The orders are written as serialized JSON and shared  between producer and consumer microservices. Each microservice populates fields of Order class that are in the scope of their responsbility, so there is one common object travels between them. Order object  contains all the data required by other consumer microservices. Flexibility of JSON serialization enables Invoice and Delivery services to read only releavant part of Order object which they are supposed to process.

## Acknowledge current offset in spring kafka by manual commit
In this demonstration we used manual commit on consumer side. In order to do this, we need to disable auto-commit and set ack-mode to manual in application.properties file 

Disable auto-commit:

`spring.kafka.consumer.enable-auto-commit=false`

Set the ack-mode to MANUAL:

`spring.kafka.listener.ack-mode=MANUAL`


Now we can commit the offset manually

```java
	@KafkaListener(topics = "order")
	public void order(Shipment shipment, Acknowledgment acknowledgment) {
		log.info("Received shipment " + shipment.getId());
		shipmentService.ship(shipment);
		acknowledgment.acknowledge();
	}

```
## JSON Serializer and Deserializer
Order Java Object as JSON byte[] is sent to a Kafka topic using Spring Kafka JsonSerializer on producer side. On consumer side, we use Spring Kafka JsonDeserializer to convert JSON byte[] to Java Object. Apache Kafka stores and transports byte[]. There are several built in serializers and deserializers but it doesn’t include any for JSON. To simply things, Spring Kafka created a JsonSerializer and JsonDeserializer which we can use to convert Java Objects to and from JSON.

```java
public class ShipmentDeserializer extends JsonDeserializer<Shipment> {

	public ShipmentDeserializer() {
		super(Shipment.class);
	}

}
```

## Flow details
When order is placed by calling `@RestController` Post API, orderService is called. Spring service `orderService` updates order object, persists it into the relational DB and calls `kafkaTemplate.send(...)` function. Callback method annoted with `@KafkaListener(topics = "order")` in Spring component of consuming microservice is called when the message is put to order topic. After consuming the order object, ShipmentService ship function is called and acknowledge is returned. 

## Difference between @Component, @Service, @Controller and @Repository annotations
Starting from Spring 2.5 annotation-based dependency injection introduced. Spring automatically scans and register classes as Spring bean which is annotated using @Component annotation. @Service, @Controller and @Repository are also Spring beans as specialized form of @Component annotation. Controller class of Spring MVC is annotated as @Controller to make it more readable. All classes annotated except Controller are Spring bean and are maintained by Spring ApplicationContext. For example DispatcherServlet looks for @RequestMapping on classes which are annotated using @Controller but not with @Component. @Service and @Repository are also component in service and persistence layer. A Spring bean in the service layer should be annotated using @Service instead of @Component annotation and a spring bean in the persistence layer should be annotated with @Repository annotation. For example, @Repository annotation also catches platform specific exceptions and re-throw them as one of Spring’s unified unchecked exceptions.





