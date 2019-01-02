# binder-cmq

binder-cmq是腾讯消息队列和spring-cloud-stream的整合。

 ![image](https://github.com/zhangzerui20/binder-cmq/blob/master/doc/images/binder-cmq.png)

消费者应用启动的时候，会在cmq上创建一个topic，topic的名字是，input上注解的destination。接着会创建一个队列，队列的名字包含三段，格式如下：
```
destination_group_partition
```

- group字段与spring-cloud-stream的group意义相同，即用消费者组来增加消费能力。
- partition字段与spring-cloud-stream的partition意义相同，用来在一个group内做routing。


### group的方案：

多个消费者属于一个group组成逻辑消费者。发到group中的消息，只会被group中的一个消费者消费。

- group对于生产端透明，生产者直接将消息发布到topic中。
- 对于消费者，一个消费者group只有一个queue，这个queue绑定在以destination为名的topic上。多个消费者同时消费一个队列，达到对消费者的扩展。


### partition的方案
partition是group下的路由。
routing的一般概念是：即消费者绑定topic时，会指定一个binding key；生产者发布消息时，会指定一个routing key。mq会判断该topic收到的消息中的routing key，将消息路由给不同的消费者。

spring-cloud-stream对partition做了规范。消费者需要指定partition的个数，和自己需要的partition的index。

cmq有binding key和routing key的概念。借助这个实现了partition功能。

- partitioned模式的消费者，创建的queue的名称的最后一段为partition的index。
- partitioned模式的生产者，发送消息需要指定routing key。针对这个key，spring-cloud-stream可以算出一个index。对应到不同的consumer的queue。

概括来说，一个consumer一个queue，通过routing key将消息发送给不同的consumer。

### 使用示例：

配置示例：
```
spring:
  cloud:
    stream:
      bindings:
        treChannel:
          destination: treChannel
          group: treGroup
          consumer:
            partitioned: true
          producer:
            partition-key-expression: headers['partitionKey']
            partition-count: 2
            required-groups: treGroup
      instanceIndex: 1
      instanceCount: 2
      cmq:
        bindings:
          treChannel:
            consumer:
              pollingWaitSeconds: 8
        binder:
          secretId: *****
          secretKey: *****
          endpoint: https://cmq-queue-gz.api.qcloud.com
```

消费者 demo示例：
```
@SpringBootApplication
@EnableBinding(TreGroupTester.class)
public class Tester {
    public static void main(String[] args) {
        SpringApplication.run(Tester.class, args);
    }

    @StreamListener(TreGroupTester.INPUT)
    public void handle(Person person) {
        System.out.println("Received: " + person);
    }

    public static class Person {
        private String name;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String toString() {
            return this.name;
        }
    }
}
```

producer demo如下：
```
@SpringBootApplication
@EnableBinding(SinkSender.class)
public class Sender implements CommandLineRunner {


    @Autowired
    SinkSender sender;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Sender.class)
                .run(args);
    }

    public void run(String... args) throws Exception {
        sender.channel().send(MessageBuilder.withPayload("{\"name\":\"test\"}").setHeader("partitionKey", 2).build());
    }
}
```


