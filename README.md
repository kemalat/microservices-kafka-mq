# microservices-kafka-mq

This proof of concept application we use Apache Kafka as messaging queue/bus between Java Spring based microservices. 

## spring-kafka

`spring-kafka` provides Kafka-based messaging solution development using the Spring concepts. High level abstraction for sending messages to Kafka is provided by `KafkaTemplate`. Application which will create work orders should insantiate `KafkaTemplate` by passing the message object and call send function. Other applications that are supposed to process work orders should register `@KafkaListener` callback. Spring Kafka support has the very similar model as the JMS support in the Spring Framework for RabbitMQ support in Spring AMQP.


## Publish-Subscribe Model
By means of Publish-Subscribe Model, multiple publishers publish messages to topics hosted by brokers. Those brokers can be subscribed to by multiple subscribers. A message is broadcasted to all the subscribers of a topic.

## Consumer Groups
Consumer groups give Kafka the flexibility to have the advantages of both message queuing and publish-subscribe models.Kafka consumers belonging to the same consumer group share a group id. The consumers in a group then divides the topic partitions among themselves. Each partition is only consumed by a single consumer from the group. When multiple consumer groups exist, the flow of the data consumption model aligns with the traditional publish-subscribe model. The messages are broadcast to all consumer groups.

## Topics
Microservice generating the work order puts `order` topic. Topic has partitions to achieve the scability as explained above by means consumer groups.

## spring-kafka-test
The Spring Kafka project comes with a `spring-kafka-test` Maven library that contains a number of useful utilities such as 
embedded Kafka broker, static methods to setup consumers/producers and to fetch results.the EmbeddedKafkaBroker of this library is provided creates an embedded Kafka and an embedded Zookeeper server. For tests an embedded Kafka server is used. In the testing flow, before executing the any tests we call EmbeddedKafkaRule constructor, it takes the following values as parameters.

- The number of Kafka servers to start.
- Whether a controlled shutdown is required.
- Topic(s) that need to be created on the server.

BeforeClass and ClassRule JUnit annotations used. The BeforeClass Junit annotation indicates that the static method to which is attached must be executed once and before all tests in the class. @BeforeClass configures Spring Kafka to use the embedded Kafka server. We use @ClassRule to instantiate EmbeddedKafkaRule class.

## Message Object
The orders are serialized as JSON. So the Order object of the order microservice is serialized as a JSON data structure. The other two microservices just read the data they need for shipping and invoicing. So the invoicing microservices reads the Invoiceobject and the delivery microservice the Deliveryobject. This avoids code dependencies between the microservices. Order contains all the data for Invoice as well as Delivery. JSON serialization is flexible. So when an Order is deserialized into Invoice and Delivery just the needed data is read. The additional data is just ignored.



